package com.truelogic.snarfer;

// Java imports
import java.sql.*;
import java.util.*;
import java.io.*;
import java.awt.*;
import java.awt.image.*;
import javax.imageio.*;
import javax.imageio.stream.*;

// Projects imports
import com.truelogic.common.*;
import com.truelogic.snarfer.config.*;
import com.truelogic.snarfer.db.*;

public class DbToFileFlash 
{
    private Connection oDB = null;
    private java.sql.Date oDate;
    private String strOutputDir;
    private int iLimit;
    private int iImageWidth;
    private int iImageHeight;
    private int iImageQuality;
    
    public DbToFileFlash(java.sql.Date oDate, ConfigDb oConfigDb, String strOutputDir, int iLimit, 
                         int iImageWidth, int iImageHeight, int iImageQuality) 
                         throws ClassNotFoundException, SQLException
    {
        Class.forName(oConfigDb.getDriver());

        oDB = DriverManager.getConnection(oConfigDb.getConnect(), oConfigDb.getUser(), oConfigDb.getPassword());
        oDB.setAutoCommit(false);

        this.oDate = oDate;
        this.strOutputDir = strOutputDir;
        this.iLimit = iLimit;
        this.iImageWidth = iImageWidth;
        this.iImageHeight = iImageHeight;
        this.iImageQuality = iImageQuality;
    }
    
    private String padInt(int iInt, int iPad)
    {
        String strInt = "" + iInt;

        while (strInt.length() < iPad)
            strInt = "0" + strInt;

        return(strInt);
    }
    
    public void run() throws SQLException, Exception
    {
        int iBatchID = getBatch(oDate);
        Vector<DbSource> oSources = getSourceList(iBatchID);
        FileWriter oTextWriter;

        String strSourceOutputDir = strOutputDir + "00.tmp";
        FileUtil.removeDir(new File(strSourceOutputDir));
        (new File(strSourceOutputDir)).mkdir();
        strSourceOutputDir += "/flash/";
        (new File(strSourceOutputDir)).mkdir();

        for (int iSourceIdx = 0; iSourceIdx < oSources.size(); iSourceIdx++)
        {
            int iArticleIdx = 0;
            DbSource oSource = oSources.get(iSourceIdx);

            String strSource = 
                "content=\r\n" +
                "id=" + oSource.getID() + "\r\n" +
                "text_id=" + oSource.getTextID() + "\r\n" +
                "name=" + oSource.getName() + "\r\n" +
                "url_id=" + oSource.getUrlID() + "\r\n" +
                "url=" + oSource.getUrl();
                    
            oTextWriter = new FileWriter(strSourceOutputDir + oSource.getTextID() + ".cnt");            
            oTextWriter.write(strSource);
            oTextWriter.close();
            
            for (DbArticle oArticle : oSource.getArticles())
            {
                String strFileName = oSource.getTextID() + "-" + padInt(iArticleIdx, 3);
            
                oTextWriter = new FileWriter(strSourceOutputDir + strFileName + ".txt");
                
                String strText = oArticle.getText().replaceAll("\\&\\#039\\;", "'");
                strText = strText.replaceAll("\\&\\#[0-9]+\\;", "");
                strText = strText.replaceAll("\\&[a-z]+\\;", "");

                oTextWriter.write("content=" + strText.trim());
                oTextWriter.close();
                
                FileOutputStream oImageWriter = new FileOutputStream(strSourceOutputDir + strFileName + ".jpg");

                oImageWriter.write(resizeImage(oArticle.getImage(), iImageWidth, iImageHeight, iImageQuality));
                oImageWriter.close();

                String strContent = 
                    "content=\r\n" +
                    "sequence=" + iArticleIdx + "\r\n" +
                    "id=" + oArticle.getID() + "\r\n" +
                    "source_url_id=" + oArticle.getSourceUrlID() + "\r\n" +
                    "random_id=" + oArticle.getRandomID() + "\r\n" +
                    "tier=" + oArticle.getTier() + "\r\n" +
                    "size=" + oArticle.getTextSize() + "\r\n" +
                    "hash=" + oArticle.getTextHash() + "\r\n" +
                    "url=" + oArticle.getTextUrl() + "\r\n" +
                    "batch_id=" + oArticle.getBatchID();

                oTextWriter = new FileWriter(strSourceOutputDir + strFileName + ".txt.cnt");

                oTextWriter.write(strContent);
                oTextWriter.close();

                strContent = 
                    "content=\r\n" +
                    "sequence=" + iArticleIdx + "\r\n" +
                    "id=" + oArticle.getImageID() + "\r\n" +
                    "size=" + oArticle.getImageSize() + "\r\n" +
                    "hash=" + oArticle.getImageHash() + "\r\n" +
                    "url=" + oArticle.getImageUrl() + "\r\n" +
                    "batch_id=" + oArticle.getBatchID();

                oTextWriter = new FileWriter(strSourceOutputDir + strFileName + ".jpg.cnt");

                oTextWriter.write(strContent);
                oTextWriter.close();
                
                iArticleIdx++;
            }
        }
        
        FileUtil.removeDir(new File(strOutputDir + "00.old"));
        (new File(strOutputDir + "00")).renameTo(new File(strOutputDir + "00.old"));
        (new File(strOutputDir + "00.tmp")).renameTo(new File(strOutputDir + "00"));
    }
    
    private byte[] resizeImage(byte[] tyImage, int iWidth, int iHeight, int iQuality) throws IOException
    {
        ByteArrayInputStream oInput = new ByteArrayInputStream(tyImage);
        BufferedImage oImage = ImageIO.read(oInput);
        
        BufferedImage oImageNew = new BufferedImage(iWidth, iHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D oGraphics = oImageNew.createGraphics();
        oGraphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                                    RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        oGraphics.drawImage(oImage, 0, 0, iWidth, iHeight, null);
        
        ByteArrayOutputStream oOutput = new ByteArrayOutputStream();
        MemoryCacheImageOutputStream oImageOutput = new MemoryCacheImageOutputStream(oOutput);
        Iterator<ImageWriter> oImageIterator = ImageIO.getImageWritersByFormatName("jpeg");
        ImageWriter oImageWriter = (ImageWriter)oImageIterator.next();
        ImageWriteParam oImageWriterParam = oImageWriter.getDefaultWriteParam();
        oImageWriterParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        oImageWriterParam.setCompressionQuality((float) .9);   // an integer between 0 and 1
        
        oImageWriter.setOutput(oImageOutput);
        IIOImage oImageTemp = new IIOImage(oImageNew, null, null);
        oImageWriter.write(null, oImageTemp, oImageWriterParam);
        oImageWriter.dispose();
                
        return(oOutput.toByteArray());
    }
    
    private int getBatch(java.sql.Date oDate) throws SQLException, Exception
    {
        PreparedStatement oStatement = null;
        int iBatchID = 0;
        
        try
        {
            String strSQL = "";
            
            if (oDate == null)
                strSQL = 
                    "select id from batch where day in (select max(day) from batch)";
            else
                strSQL = 
                    "select id from batch where day = ?";
            
           oStatement = oDB.prepareStatement(strSQL);
           if (oDate != null)
               oStatement.setDate(1, oDate);
           ResultSet oResult = oStatement.executeQuery();
           
           if (oResult.next())
               iBatchID = oResult.getInt("id");
           else
               throw new Exception("Unable to find the batch");
        }
        finally
        {
            if (oStatement != null) oStatement.close();
        }
        
    return(iBatchID);
    }
    
    private Vector<DbSource> getSourceList(int iBatchID) throws SQLException, Exception
    {
        
        PreparedStatement oStatement = null;
        Vector<DbSource> oSources = new Vector<DbSource>();
        
        try
        {
            String strSQL = 
                "select source.id, source.text_id, source.name, source_url.id as url_id, source_url.url\n" +
                " from source, source_url\n" +
                "where source.id = source_url.source_id\n" +
                "  and source_url.id in\n" +
                "          (\n" +
                "          select distinct(article.source_url_id)\n" +
                "          from article, batch_article\n" +
                "          where batch_article.batch_id = ?\n" +
                "            and batch_article.article_id = article.id\n" +
                "          )";
            
           oStatement = oDB.prepareStatement(strSQL);
           oStatement.setInt(1, iBatchID);
           ResultSet oResult = oStatement.executeQuery();
           
           while (oResult.next())
           {
               DbSource oSource = new DbSource(oResult.getInt("id"),
                                               oResult.getString("text_id"),
                                               oResult.getString("name"),
                                               oResult.getInt("url_id"),
                                               oResult.getString("url"),
                                               getArticleList(oResult.getInt("id"), iBatchID, iLimit));
               
               oSources.add(oSource);
           }
        }
        finally
        {
            if (oStatement != null) oStatement.close();
        }
        
        return(oSources);
    }
    
   private Vector<DbArticle> getArticleList(int iSourceID, int iBatchID, int iLimit) throws SQLException, Exception
    {
        PreparedStatement oStatement = null;
        Vector<DbArticle> oArticleList = new Vector<DbArticle>();

        try
        {
            String strSQL = 
                "select * from article_list_get(?, ?, ?)";
            
           oStatement = oDB.prepareStatement(strSQL);
           
           oStatement.setInt(1, iBatchID);
           oStatement.setInt(2, iSourceID);
           oStatement.setInt(3, iLimit);
           ResultSet oResult = oStatement.executeQuery();
           
           while(oResult.next())
           {
               ByteArrayOutputStream oImage = new ByteArrayOutputStream();
               byte[] tyByte = new byte[1];
               
               InputStream oInput = oResult.getBinaryStream("image_data");
               
               while(oInput.read(tyByte, 0, 1) != -1)
                   oImage.write(tyByte, 0, 1);
               
               DbArticle oArticle = new DbArticle(oResult.getInt("id"),
                                                  oResult.getInt("source_url_id"),
                                                  oResult.getString("random_id"),
                                                  oResult.getInt("tier"),
                                                  oResult.getInt("size"),
                                                  oResult.getString("hash"),
                                                  oResult.getString("url"),
                                                  oResult.getString("data"),
                                                  oResult.getInt("image_id"),
                                                  oResult.getInt("image_size"),
                                                  oResult.getString("image_hash"),
                                                  oResult.getString("image_url"),
                                                  oImage.toByteArray(),
                                                  oResult.getInt("batch_id"));
                              
               oArticleList.add(oArticle);
           }
        }
        finally
        {
            if (oStatement != null) oStatement.close();
        }
        
    return(oArticleList);
    }
}        
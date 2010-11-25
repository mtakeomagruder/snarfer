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

public class DbToFileFlash 
{
    private class Article
    {
        int iID;
        int iSourceURLID;
        String strRandomID;
        int iTier;
        int iSize;
        String strTextHash;
        String strTextURL;
        String strText;
        int iImageID;
        int iImageSize;
        String strImageHash;
        String strImageURL;
        byte[] tyImage;
        int iBatchID;
    }
    
    private class Source
    {
        int iID;
        String strTextID;
        String strName;
        int iURLID;
        String strURL;
        Vector<Article> oArticleList;
    }
    
    private Connection oDB = null;
    private java.sql.Date oDate;
    private String strOutputDir;
    private int iLimit;
    private int iImageWidth;
    private int iImageHeight;
    private int iImageQuality;
    
    public DbToFileFlash(java.sql.Date oDate, String strDriver, String strConnect, String strUser, 
                         String strPassword, String strOutputDir, int iLimit, 
                         int iImageWidth, int iImageHeight, int iImageQuality) 
                         throws ClassNotFoundException, SQLException
    {
        Class.forName(strDriver);

        oDB = DriverManager.getConnection(strConnect, strUser, strPassword);
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
        Vector<Source> oSourceList = getSourceList(iBatchID);
        FileWriter oTextWriter;

        String strSourceOutputDir = strOutputDir + "00.tmp";
        FileUtil.removeDir(new File(strSourceOutputDir));
        (new File(strSourceOutputDir)).mkdir();
        strSourceOutputDir += "/flash/";
        (new File(strSourceOutputDir)).mkdir();

        for (int iSourceIdx = 0; iSourceIdx < oSourceList.size(); iSourceIdx++)
        {
            Source oSource = oSourceList.get(iSourceIdx);

            String strSource = 
                "content=\r\n" +
                "id=" + oSource.iID + "\r\n" +
                "text_id=" + oSource.strTextID + "\r\n" +
                "name=" + oSource.strName + "\r\n" +
                "url_id=" + oSource.iURLID + "\r\n" +
                "url=" + oSource.strURL;
                    
            oTextWriter = new FileWriter(strSourceOutputDir + oSource.strTextID + ".cnt");            
            oTextWriter.write(strSource);
            oTextWriter.close();
            
            for (int iArticleIdx = 0; iArticleIdx < oSource.oArticleList.size(); iArticleIdx++)
            {
                Article oArticle = oSource.oArticleList.get(iArticleIdx);
                
                String strFileName = oSource.strTextID + "-" + padInt(iArticleIdx, 3);
            
                oTextWriter = new FileWriter(strSourceOutputDir + strFileName + ".txt");
                
                String strText = oArticle.strText.replaceAll("\\&\\#039\\;", "'");
                strText = strText.replaceAll("\\&\\#[0-9]+\\;", "");
                strText = strText.replaceAll("\\&[a-z]+\\;", "");

                oTextWriter.write("content=" + strText.trim());
                oTextWriter.close();
                
                FileOutputStream oImageWriter = new FileOutputStream(strSourceOutputDir + strFileName + ".jpg");

                oImageWriter.write(resizeImage(oArticle.tyImage, iImageWidth, iImageHeight, iImageQuality));
                oImageWriter.close();

                String strContent = 
                    "content=\r\n" +
                    "sequence=" + iArticleIdx + "\r\n" +
                    "id=" + oArticle.iID + "\r\n" +
                    "source_url_id=" + oArticle.iSourceURLID + "\r\n" +
                    "random_id=" + oArticle.strRandomID + "\r\n" +
                    "tier=" + oArticle.iTier + "\r\n" +
                    "size=" + oArticle.iSize + "\r\n" +
                    "hash=" + oArticle.strTextHash + "\r\n" +
                    "url=" + oArticle.strTextURL + "\r\n" +
                    "batch_id=" + oArticle.iBatchID;

                oTextWriter = new FileWriter(strSourceOutputDir + strFileName + ".txt.cnt");

                oTextWriter.write(strContent);
                oTextWriter.close();

                strContent = 
                    "content=\r\n" +
                    "sequence=" + iArticleIdx + "\r\n" +
                    "id=" + oArticle.iImageID + "\r\n" +
                    "size=" + oArticle.iImageSize + "\r\n" +
                    "hash=" + oArticle.strImageHash + "\r\n" +
                    "url=" + oArticle.strImageURL + "\r\n" +
                    "batch_id=" + oArticle.iBatchID;

                oTextWriter = new FileWriter(strSourceOutputDir + strFileName + ".jpg.cnt");

                oTextWriter.write(strContent);
                oTextWriter.close();
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
    
    private Vector<Source> getSourceList(int iBatchID) throws SQLException, Exception
    {
        
        PreparedStatement oStatement = null;
        Vector<Source> oSourceList = new Vector<Source>();
        
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
               Source oSource = new Source();
               oSource.iID = oResult.getInt("id");
               oSource.strTextID = oResult.getString("text_id");
               oSource.strName = oResult.getString("name");
               oSource.iURLID = oResult.getInt("url_id");
               oSource.strURL = oResult.getString("url");
               oSource.oArticleList = getArticleList(oSource.iID, iBatchID, iLimit);
               
               oSourceList.add(oSource);
           }
        }
        finally
        {
            if (oStatement != null) oStatement.close();
        }
        
    return(oSourceList);
    }
    
   private Vector<Article> getArticleList(int iSourceID, int iBatchID, int iLimit) throws SQLException, Exception
    {
        PreparedStatement oStatement = null;
        Vector<Article> oArticleList = new Vector<Article>();

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
               Article oArticle = new Article();
               
               oArticle.iID = oResult.getInt("id");
               oArticle.strRandomID = oResult.getString("random_id");
               oArticle.iSourceURLID = oResult.getInt("source_url_id");
               oArticle.iTier = oResult.getInt("tier");
               oArticle.iSize = oResult.getInt("size");
               oArticle.strTextHash = oResult.getString("hash");
               oArticle.strTextURL = oResult.getString("url");
               oArticle.strText = oResult.getString("data");

               oArticle.iImageID = oResult.getInt("image_id");
               oArticle.iImageSize = oResult.getInt("image_size");
               oArticle.strImageHash = oResult.getString("image_hash");
               oArticle.strImageURL = oResult.getString("image_url");
               
               ByteArrayOutputStream oImage = new ByteArrayOutputStream();
               byte[] tyByte = new byte[1];
               
               InputStream oInput = oResult.getBinaryStream("image_data");
               
               while(oInput.read(tyByte, 0, 1) != -1)
                   oImage.write(tyByte, 0, 1);
               
               oArticle.tyImage = oImage.toByteArray();

               oArticle.iBatchID = oResult.getInt("batch_id");
                              
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
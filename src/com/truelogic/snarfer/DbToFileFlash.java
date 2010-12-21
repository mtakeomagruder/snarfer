package com.truelogic.snarfer;

// Java imports
import java.sql.*;
import java.util.*;
import java.io.*;
import java.awt.*;
import java.awt.image.*;
import javax.imageio.*;
import javax.imageio.stream.*;

//Third Party imports
import org.apache.log4j.Logger;

// Projects imports
import com.truelogic.common.*;
import com.truelogic.snarfer.config.*;
import com.truelogic.snarfer.db.*;
import com.truelogic.snarfer.exception.*;

/***********************************************************************************************************************
* Loads news sources and articles from the database and outputs them in a flash-friendly format.
* 
* @author David Steele
***********************************************************************************************************************/
public class DbToFileFlash extends Db
{
    static Logger oLogger = Logger.getLogger(DbToFileFlash.class);

    private java.sql.Date oDate;            // Date of the batch to output
    private ConfigOutput oConfigOutput;     // Output configuration
    
    /*******************************************************************************************************************
    * Initialize DbFileToFlash.
    * 
    * @param oDate           Date of the batch to output
    * @param oConfigDb       Database configuration
    * @param oConfigOutput   Output configuration
    *******************************************************************************************************************/
    public DbToFileFlash(java.sql.Date oDate, ConfigDb oConfigDb, ConfigOutput oConfigOutput) 
                         throws ClassNotFoundException, SQLException
    {
        super(oConfigDb);
        
        this.oDate = oDate;
        this.oConfigOutput = oConfigOutput;
    }
    
    /*******************************************************************************************************************
    * Outputs the batch to flash-friendly files.
    *******************************************************************************************************************/
    public void run() throws SQLException, SnarferException, IOException
    {
        /***************************************************************************************************************
        * Get list of news sources to output 
        ***************************************************************************************************************/
        oLogger.info("Loading news sources");
        
        int iBatchID = getBatch(oDate);
        Vector<DbSource> oSources = getSourceList(iBatchID);

        /***************************************************************************************************************
        * Create the temporary output directory (remove it if it already exists) 
        ***************************************************************************************************************/
        oLogger.info("Creating temp directory");
        
        String strSourceOutputDir = oConfigOutput.getOutputDir() + "00.tmp";
        FileUtil.removeDir(new File(strSourceOutputDir));
        (new File(strSourceOutputDir)).mkdir();
        strSourceOutputDir += "/flash/";
        (new File(strSourceOutputDir)).mkdir();

        /***************************************************************************************************************
        * Loop through the sources 
        ***************************************************************************************************************/
        for (DbSource oSource : oSources)
        {
            /***********************************************************************************************************
            * Output information about the source 
            ***********************************************************************************************************/
            oLogger.info("Saving source: " + oSource.getName());

            String strSource = 
                "content=\r\n" +
                "id=" + oSource.getID() + "\r\n" +
                "text_id=" + oSource.getTextID() + "\r\n" +
                "name=" + oSource.getName() + "\r\n" +
                "url_id=" + oSource.getUrlID() + "\r\n" +
                "url=" + oSource.getUrl();
                    
            FileWriter oTextWriter = new FileWriter(strSourceOutputDir + oSource.getTextID() + ".cnt");            
            oTextWriter.write(strSource);
            oTextWriter.close();

            /***************************************************************************************************************
            * Loop through the articles
            ***************************************************************************************************************/
            int iArticleIdx = 0;
            
            for (DbArticle oArticle : oSource.getArticles())
            {
                oLogger.info("Loading article " + oSource.getTextID() + "-" + 
                             StringUtil.leftPad(iArticleIdx, 3, '0') + ": " + oArticle.getTextUrl());

                /*******************************************************************************************************
                * Replace all strings defined in the rules   
                *******************************************************************************************************/
                oLogger.info("Replacing article text based on rules");

                String strText = oArticle.getText();
                
                for (String strReplace : oConfigOutput.getReplace().getOrderedKeys())
                    strText = strText.replaceAll(strReplace, oConfigOutput.getReplace().get(strReplace));

                /*******************************************************************************************************
                * Output the article text   
                *******************************************************************************************************/
                oLogger.info("Writing article text");
                 
                String strFileName = oSource.getTextID() + "-" + StringUtil.leftPad(iArticleIdx, 3, '0');

                oTextWriter = new FileWriter(strSourceOutputDir + strFileName + ".txt");
                oTextWriter.write("content=" + strText.trim());
                oTextWriter.close();
                
                /*******************************************************************************************************
                * Output the JPEG image
                *******************************************************************************************************/
                FileOutputStream oImageWriter = new FileOutputStream(strSourceOutputDir + strFileName + ".jpg");

                oLogger.info("Writing JPEG");

                oImageWriter.write(resizeImage(oArticle.getImage(), oConfigOutput.getImageWidth(), 
                                               oConfigOutput.getImageHeight(), oConfigOutput.getImageQuality()));
                oImageWriter.close();

                /*******************************************************************************************************
                * Output information about the article
                *******************************************************************************************************/
                oLogger.info("Writing article information");

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

                /*******************************************************************************************************
                * Output information about the image
                *******************************************************************************************************/
                oLogger.info("Writing JPEG information");

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
        
        /***************************************************************************************************************
        * Move the current directory to old and the temp directory to current 
        ***************************************************************************************************************/
        oLogger.info("Backup up old directory and rename temp directory");

        FileUtil.removeDir(new File(oConfigOutput.getOutputDir() + "00.old"));
        (new File(oConfigOutput.getOutputDir() + "00")).renameTo(new File(oConfigOutput.getOutputDir() + "00.old"));
        (new File(oConfigOutput.getOutputDir() + "00.tmp")).renameTo(new File(oConfigOutput.getOutputDir() + "00"));
    }
    
    /*******************************************************************************************************************
    * Resizes a JPEG image.
    * 
    * @param tyImage  JPEG image
    * @param iWidth   Width of the new image
    * @param iHeight  Height of the new image
    * @param iQuality Quality of the new image
    * 
    * @return Resized JPEG image
    *******************************************************************************************************************/
    private byte[] resizeImage(byte[] tyImage, int iWidth, int iHeight, int iQuality) throws IOException
    {
        /***************************************************************************************************************
        * Load the JPEG image into a BufferedImage 
        ***************************************************************************************************************/
        ByteArrayInputStream oInput = new ByteArrayInputStream(tyImage);
        BufferedImage oImage = ImageIO.read(oInput);
        
        /***************************************************************************************************************
        * Resize the image 
        ***************************************************************************************************************/
        BufferedImage oImageNew = new BufferedImage(iWidth, iHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D oGraphics = oImageNew.createGraphics();
        oGraphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                                    RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        oGraphics.drawImage(oImage, 0, 0, iWidth, iHeight, null);
        
        /***************************************************************************************************************
        * Convert the resized image to JPEG 
        ***************************************************************************************************************/
        ByteArrayOutputStream oOutput = new ByteArrayOutputStream();
        MemoryCacheImageOutputStream oImageOutput = new MemoryCacheImageOutputStream(oOutput);
        Iterator<ImageWriter> oImageIterator = ImageIO.getImageWritersByFormatName("jpeg");
        ImageWriter oImageWriter = (ImageWriter)oImageIterator.next();
        ImageWriteParam oImageWriterParam = oImageWriter.getDefaultWriteParam();
        oImageWriterParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        oImageWriterParam.setCompressionQuality((float) .9);
        
        /***************************************************************************************************************
        * Write the image to a byte array 
        ***************************************************************************************************************/
        oImageWriter.setOutput(oImageOutput);
        IIOImage oImageTemp = new IIOImage(oImageNew, null, null);
        oImageWriter.write(null, oImageTemp, oImageWriterParam);
        oImageWriter.dispose();
                
        return(oOutput.toByteArray());
    }
    
    /*******************************************************************************************************************
    * Gets a batch from the database.
    * 
    * @param oDate  Batch date (If null then max day is used)
    * 
    * @return Batch DB ID
    *******************************************************************************************************************/
    private int getBatch(java.sql.Date oDate) throws SQLException, SnarferException
    {
        String strSQL = "";
        
        if (oDate == null)
            strSQL = 
                "select id from batch where day in (select max(day) from batch)";
        else
            strSQL = 
                "select id from batch where day = ?";
        
        PreparedStatement oStatement = getDb().prepareStatement(strSQL);
        int iBatchID = 0;
        
        try
        {
            if (oDate != null)
                oStatement.setDate(1, oDate);
            
            ResultSet oResult = oStatement.executeQuery();
           
            if (oResult.next())
                iBatchID = oResult.getInt("id");
            else
                throw new SnarferException("Unable to find the batch");
        }
        finally
        {
            oStatement.close();
        }
        
    return(iBatchID);
    }
    
    /*******************************************************************************************************************
    * Gets batch news sources from the database.
    * 
    * @param iBatchID  Batch DB ID
    * 
    * @return List of news sources
    *******************************************************************************************************************/
    private Vector<DbSource> getSourceList(int iBatchID) throws SQLException, IOException
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
        
        PreparedStatement oStatement = getDb().prepareStatement(strSQL);
        Vector<DbSource> oSources = new Vector<DbSource>();
        
        try
        {
            oStatement.setInt(1, iBatchID);
            ResultSet oResult = oStatement.executeQuery();
           
            while (oResult.next())
            {
                oLogger.info("Loading source: " + oResult.getString("text_id"));
                
                DbSource oSource = new DbSource(oResult.getInt("id"),
                                                oResult.getString("text_id"),
                                                oResult.getString("name"),
                                                oResult.getInt("url_id"),
                                                oResult.getString("url"),
                                                getArticleList(oResult.getInt("id"), iBatchID, 
                                                               oConfigOutput.getLimit()));
               
                oSources.add(oSource);
            }
        }
        finally
        {
            oStatement.close();
        }
        
        return(oSources);
    }
    
    /*******************************************************************************************************************
    * Gets a list of articles from a batch and news source.
    * 
    * @param iSourceID  Source DB ID
    * @param iBatchID   Batch DB ID
    * 
    * @return List of articles
    *******************************************************************************************************************/
    private Vector<DbArticle> getArticleList(int iSourceID, int iBatchID, int iLimit) throws SQLException, IOException
    {
        String strSQL = 
            "select * from article_list_get(?, ?, ?)";
        
        PreparedStatement oStatement = getDb().prepareStatement(strSQL);
        Vector<DbArticle> oArticleList = new Vector<DbArticle>();

        try
        {
           
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
                                                  oResult.getString("hash"),
                                                  oResult.getString("url"),
                                                  oResult.getString("data"),
                                                  oResult.getInt("image_id"),
                                                  oResult.getString("image_hash"),
                                                  oResult.getString("image_url"),
                                                  oImage.toByteArray(),
                                                  oResult.getInt("batch_id"));
                              
               oArticleList.add(oArticle);
           }
        }
        finally
        {
            oStatement.close();
        }
        
    return(oArticleList);
    }
}
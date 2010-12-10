package com.truelogic.snarfer;

// Java imports
import java.io.*;
import java.sql.*;

// Project imports
import com.truelogic.snarfer.config.*;
import com.truelogic.snarfer.db.*;
import com.truelogic.snarfer.exception.*;

/***********************************************************************************************************************
* Stores articles and images from the snarfer object into the database.
* 
* @author David Steele
***********************************************************************************************************************/
public class SnarferToDb extends Db 
{
    private Snarfer oSnarfer;   // Snarfer object with all sources loaded
    
    /*******************************************************************************************************************
    * Initializes Snarfer2Db.
    * 
    * @param oSnarfer   Snarfer object with all sources loaded
    * @param oConfigDb  Database configuration
    *******************************************************************************************************************/
    public SnarferToDb(Snarfer oSnarfer, ConfigDb oConfigDb) throws ClassNotFoundException, SQLException
    {
        super(oConfigDb);
        
        this.oSnarfer = oSnarfer;
    }
    
    /*******************************************************************************************************************
    * Stores all the news sources in the database.
    *******************************************************************************************************************/
    public java.sql.Date run() throws SQLException, SnarferException
    {
        java.sql.Date oDate = null;
        
        try
        {
            int iBatchID = 0;
            int iSourceID = 0;
            
            /***********************************************************************************************************
            * Get the current date from the database.  
            ***********************************************************************************************************/
            oDate = getDate();

            /***********************************************************************************************************
            * Store the batch that will contain all the articles
            ***********************************************************************************************************/
            iBatchID = storeBatch(oDate);
            
            /***********************************************************************************************************
            * Store the sources
            ***********************************************************************************************************/
            for (Source oSource : oSnarfer)
            {
                iSourceID = storeSource(oSource.getConfig().getID(), oSource.getConfig().getName(), 
                                        oSource.getConfig().getURLs().get(0));

                /*******************************************************************************************************
                * Store the articles
                *******************************************************************************************************/
                for (Article oArticle : oSource)
                    storeArticle(iBatchID, iSourceID, oArticle.getTier(), oArticle.getText(), oArticle.getURL(), 
                                 oArticle.getImage(), oArticle.getImageURL());
            }
            
            //getDb().commit();
        }
        finally
        {
            getDb().rollback();
        }
     
        return(oDate);
    }
    
    /***********************************************************************************************************
    * Get the current date from the database.  This program could be running on a system in any time zone, but
    * the database is the final authority on all dates.
    * 
    * @returns Current date according to the database
    ***********************************************************************************************************/
    private java.sql.Date getDate() throws SQLException, SnarferException
    {
        String strSQL = 
            "select date(now()) as date";
        
        PreparedStatement oStatement = getDb().prepareStatement(strSQL);
        
        try
        {
           ResultSet oResult = oStatement.executeQuery();
           
           if (oResult.next())
               return(oResult.getDate("date"));
           else
               throw new SnarferException("Unable to get date");
        }
        finally
        {
            oStatement.close();
        }
    }

    /***********************************************************************************************************
    * Store the batch which will contain all the news sources that are being pulled by the snarfer.
    * 
    * @param oDate  Date of the batch (should be supplied by getDate()
    * 
    * @returns Batch DB ID
    ***********************************************************************************************************/
    private int storeBatch(java.sql.Date oDate) throws SQLException, SnarferException
    {
        String strSQL = 
            "select batch_insert(?) as batch_id";
        
        PreparedStatement oStatement = getDb().prepareStatement(strSQL);
        
        try
        {
           oStatement.setDate(1, oDate);

           ResultSet oResult = oStatement.executeQuery();
           
           if (oResult.next())
               return(oResult.getInt("batch_id"));
           else
               throw new SnarferException("Unable to add a batch");
        }
        finally
        {
            oStatement.close();
        }
    }
    
    /***********************************************************************************************************
    * Store the source which will contain news articles.
    * 
    * @param strTextID  Source Text ID (Numeric ID is assigned by the database)
    * @param strName    Diplay name
    * @param strUrl     Source URL
    * 
    * @returns Source DB ID
    ***********************************************************************************************************/
    private int storeSource(String strTextID, String strName, String strURL) 
                throws SQLException, SnarferException
    {
        String strSQL = 
            "select source_insert(?, ?, ?) as source_id";

        PreparedStatement oStatement = getDb().prepareStatement(strSQL);;
        
        try
        {
           oStatement.setString(1, strTextID);
           oStatement.setString(2, strName);
           oStatement.setString(3, strURL);
           
           ResultSet oResult = oStatement.executeQuery();
           
           if (oResult.next())
               return(oResult.getInt("source_id"));
           else
               throw new SnarferException("Unable to add a source");
        }
        finally
        {
            oStatement.close();
        }
    }
    
    /***********************************************************************************************************
    * Store the article.
    * 
    * @param iBatchID     Batch DB ID
    * @param iSourceID    Source DB ID
    * @param iTier        Article tier (1 for headline, 2 or more for all else)
    * @param strText      Article Text
    * @param strTextURL   Article URL
    * @param tyImage      Image in JPEG format as a byte array
    * @param strImageURL  Image URL
    * 
    * @returns Article DB ID
    ***********************************************************************************************************/
    private int storeArticle(int iBatchID, int iSourceID, int iTier, 
                             String strText, String strTextURL, byte[] tyImage, 
                             String strImageURL) throws SQLException, SnarferException
    {
        String strSQL = 
            "select article_insert(?, ?, ?, ?, ?, ?, ?) as article_id";
        
        PreparedStatement oStatement = getDb().prepareStatement(strSQL);
        
        try
        {
            oStatement.setInt(1, iBatchID);
            oStatement.setInt(2, iSourceID);
            oStatement.setInt(3, iTier);
            oStatement.setString(4, strText);
            oStatement.setString(5, strTextURL);

            ByteArrayInputStream oImage = new ByteArrayInputStream(tyImage);
            oStatement.setBinaryStream(6, oImage, tyImage.length);

            oStatement.setString(7, strImageURL);
           
            ResultSet oResult = oStatement.executeQuery();
           
            if (oResult.next())
                return(oResult.getInt("article_id"));
            else
                throw new SnarferException("Unable to add an article");
        }
        finally
        {
            oStatement.close();
        }
    }
}
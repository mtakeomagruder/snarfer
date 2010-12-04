package com.truelogic.snarfer;

// Java imports
import java.io.*;
import java.sql.*;

// Project imports
import com.truelogic.snarfer.config.*;
import com.truelogic.snarfer.db.*;

/***********************************************************************************************************************
* Stores articles and images from the snarfer object into the database.
* 
* @author David Steele
***********************************************************************************************************************/
public class SnarferToDb extends Db 
{
    private Snarfer oSnarfer;   // Snarfer object with all articles
    
    /*******************************************************************************************************************
    * Initializes Snarfer2DB.
    * 
    * @param strArgs  Arguments passed on the command line
    *******************************************************************************************************************/
    public SnarferToDb(Snarfer oSnarfer, ConfigDb oConfigDb) throws ClassNotFoundException, SQLException
    {
        super(oConfigDb);
        
        this.oSnarfer = oSnarfer;
    }
    
    public java.sql.Date run() throws SQLException, Exception
    {
        java.sql.Date oDate = null;
        
        try
        {
            int iBatchID = 0;
            int iSourceID = 0;
            
            oDate = getDate();
            iBatchID = storeBatch(oDate);
            
            for (int iSourceIdx = 0; iSourceIdx < oSnarfer.sourceSize(); iSourceIdx++)
            {
                Source oSource = oSnarfer.sourceGet(iSourceIdx);

                iSourceID = storeSource(oSource.getConfig().getID(), oSource.getConfig().getName(), 
                                        oSource.getConfig().getURLs().get(0));

                for (int iArticleIdx = 0; iArticleIdx < oSource.size(); iArticleIdx++)
                {
                    Article oArticle = oSource.get(iArticleIdx);

                    storeArticle(iBatchID, iSourceID, oArticle.getTier(), 
                                 oArticle.getText(), oArticle.getURL(), 
                                 oArticle.getImage(), oArticle.getImageURL());
                }
            }
            
            //getDb().commit();
        }
        finally
        {
            getDb().rollback();
        }
     
        return(oDate);
    }
    
    private java.sql.Date getDate() throws SQLException, Exception
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
               throw new Exception("Unable to get date");
        }
        finally
        {
            oStatement.close();
        }
    }
    
    private int storeSource(String strTextID, String strName, String strURL) throws SQLException, Exception
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
               throw new Exception("Unable to add a source");
        }
        finally
        {
            oStatement.close();
        }
    }
    
    private int storeBatch(java.sql.Date oDate) throws SQLException, Exception
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
               throw new Exception("Unable to add a batch");
        }
        finally
        {
            oStatement.close();
        }
    }
    
    private int storeArticle(int iBatchID, int iSourceID, int iTier, 
                             String strText, String strTextURL, byte[] tyImage, 
                             String strImageURL) throws SQLException, Exception
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
                throw new Exception("Unable to add a source");
        }
        finally
        {
            oStatement.close();
        }
    }
}
package com.truelogic.snarfer;

// Java imports
import java.io.*;
import java.sql.*;

public class SnarferToDb 
{
    private Connection oDB = null;
    private Snarfer oSnarfer = null;
    
    public SnarferToDb(Snarfer oSnarfer, String strDriver, String strConnect, String strUser, String strPassword) throws ClassNotFoundException, SQLException
    {
        Class.forName(strDriver);

        oDB = DriverManager.getConnection(strConnect, strUser, strPassword);
        oDB.setAutoCommit(false);
        
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

                iSourceID = storeSource(oSource.getData().getID(), oSource.getData().getName(), oSource.getData().getURLs().get(0));

                for (int iArticleIdx = 0; iArticleIdx < oSource.size(); iArticleIdx++)
                {
                    Article oArticle = oSource.get(iArticleIdx);

                    storeArticle(iBatchID, iSourceID, oArticle.getTier(), 
                                 oArticle.getText(), oArticle.getURL(), 
                                 oArticle.getImage(), oArticle.getImageURL());
                }
            }
            oDB.commit();
        }
        finally
        {
            oDB.rollback();
        }
     
        return(oDate);
    }
    
    private java.sql.Date getDate() throws SQLException, Exception
    {
        PreparedStatement oStatement = null;
        java.sql.Date oDate;
        
        try
        {
            String strSQL = 
                "select date(now()) as date";
            
           oStatement = oDB.prepareStatement(strSQL);
           
           ResultSet oResult = oStatement.executeQuery();
           
           if (oResult.next())
               oDate = oResult.getDate("date");
           else
               throw new Exception("Unable to get date");
        }
        finally
        {
            if (oStatement != null) oStatement.close();
        }
        
    return(oDate);
    }
    
    private int storeSource(String strTextID, String strName, String strURL) throws SQLException, Exception
    {
        PreparedStatement oStatement = null;
        int iSourceID = 0;
        
        try
        {
            String strSQL = 
                "select source_insert(?, ?, ?) as source_id";
            
           oStatement = oDB.prepareStatement(strSQL);
           
           oStatement.setString(1, strTextID);
           oStatement.setString(2, strName);
           oStatement.setString(3, strURL);
           
           ResultSet oResult = oStatement.executeQuery();
           
           if (oResult.next())
               iSourceID = oResult.getInt("source_id");
           else
               throw new Exception("Unable to add a source");
        }
        finally
        {
            if (oStatement != null) oStatement.close();
        }
        
    return(iSourceID);
    }
    
    private int storeBatch(java.sql.Date oDate) throws SQLException, Exception
    {
        PreparedStatement oStatement = null;
        int iBatchID = 0;
        
        try
        {
            String strSQL = 
                "select batch_insert(?) as batch_id";
            
           oStatement = oDB.prepareStatement(strSQL);
           oStatement.setDate(1, oDate);
           ResultSet oResult = oStatement.executeQuery();
           
           if (oResult.next())
               iBatchID = oResult.getInt("batch_id");
           else
               throw new Exception("Unable to add a batch");
        }
        finally
        {
            if (oStatement != null) oStatement.close();
        }
        
    return(iBatchID);
    }
    
    private int storeArticle(int iBatchID, int iSourceID, int iTier, 
                             String strText, String strTextURL, byte[] tyImage, 
                             String strImageURL) throws SQLException, Exception
    {
        PreparedStatement oStatement = null;
        int iArticleID = 0;
        
        try
        {
            String strSQL = 
                "select article_insert(?, ?, ?, ?, ?, ?, ?) as article_id";
            
           oStatement = oDB.prepareStatement(strSQL);
           
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
               iArticleID = oResult.getInt("article_id");
           else
               throw new Exception("Unable to add a source");
        }
        finally
        {
            if (oStatement != null) oStatement.close();
        }
        
    return(iArticleID);
    }
}
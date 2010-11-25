package com.truelogic.snarfer;

import java.util.Vector;

public class DbSource 
{
    private int iID;
    private String strTextID;
    private String strName;
    private int iUrlID;
    private String strUrl;
    private Vector<DbArticle> oArticles;
    
    public DbSource(int iID, String strTextID, String strName, int iUrlID, String strUrl, 
                    Vector<DbArticle> oArticles)
    {
        this.iID = iID;
        this.strTextID = strTextID;
        this.strName = strName;
        this.iUrlID = iUrlID;
        this.strUrl = strUrl;
        this.oArticles = oArticles;
    }
    
    public int getID() 
    {
        return(iID);
    }

    public String getTextID() 
    {
        return(strTextID);
    }

    public String getName() 
    {
        return(strName);
    }

    public int getUrlID() 
    {
        return(iUrlID);
    }

    public String getUrl() 
    {
        return(strUrl);
    }

    public Vector<DbArticle> getArticles() 
    {
        return(oArticles);
    }
}
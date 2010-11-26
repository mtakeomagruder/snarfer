package com.truelogic.snarfer.db;

// Java imports
import java.util.Vector;

/***********************************************************************************************************************
* Represents a news source loaded from the database (denormalized).
* 
* @author David Steele
***********************************************************************************************************************/
public class DbSource 
{
    private int iID;                        // Synthetic DB ID     
    private String strTextID;               // Source Text ID
    private String strName;                 // Source Name (for display)
    private int iUrlID;                     // Source URL DB ID
    private String strUrl;                  // Source URL
    private Vector<DbArticle> oArticles;    // List of source articles (for the current URL)
    
    /*******************************************************************************************************************
    * Initializes the DbSource.
    * 
    * @param iID        Synthetic DB ID
    * @param strTextID  Source Text ID
    * @param strName    Source Name (for display)
    * @param iUrlID     Source URL DB ID
    * @param strURL     Source URL
    * @param oArticles  List of source articles (for the current URL)
    *******************************************************************************************************************/
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
    
    /*******************************************************************************************************************
    * @return Synthetic DB ID
    *******************************************************************************************************************/
    public int getID() 
    {
        return(iID);
    }

    /*******************************************************************************************************************
    * @return Source Text ID
    *******************************************************************************************************************/
    public String getTextID() 
    {
        return(strTextID);
    }

    /*******************************************************************************************************************
    * @return Source Name (for display)
    *******************************************************************************************************************/
    public String getName() 
    {
        return(strName);
    }

    /*******************************************************************************************************************
    * @return Source URL DB ID
    *******************************************************************************************************************/
    public int getUrlID() 
    {
        return(iUrlID);
    }

    /*******************************************************************************************************************
    * @return Source URL
    *******************************************************************************************************************/
    public String getUrl() 
    {
        return(strUrl);
    }

    /*******************************************************************************************************************
    * @return List of source articles (for the current URL)
    *******************************************************************************************************************/
    public Vector<DbArticle> getArticles() 
    {
        return(oArticles);
    }
}
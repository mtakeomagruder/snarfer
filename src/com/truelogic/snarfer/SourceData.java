package com.truelogic.snarfer;

import java.util.*;

/***********************************************************************************************************************
* Contains information about each news source listed in the INI file.
* 
* @author David Steele
***********************************************************************************************************************/
public class SourceData 
{
    private String strID;                                   // The source ID (short text string)
    private Vector<String> strURLs = new Vector<String>();  // URLs for the RSS feeds
    private String strName;                                 // The source name
    private int iImageWidthMin;                             // The minimum width for the article image 
    private int iAspectRatioMax;                            // Max image aspect ratio
    private int iArticleSizeMin;                            // Minimum size for the article text
    private int iArticleChunkSizeMin;                       // Minimum chunk size for each section of the article
    private ArticleReplace oArticleReplace;                 // Replacement rules
    
    /*******************************************************************************************************************
    * Initializes the SourceData object.
    * 
    * @param strID                 The source ID (short text string - letters and underscore only)
    * @param strURLS               A list of RSS URLs for the source
    * @param strName               The source name (for display purposes - all characters allowed)
    * @param iImageWidthMin        The minimum width for the article image
    * @param iAspectRatioMax       Max image aspect ratio
    * @param iArticleSizeMin       Minimum size for the article text
    * @param iArticleChunkSizeMin  Minimum chunk size for each section of the article
    *******************************************************************************************************************/
    public SourceData(String strID, Vector<String> strURLs, String strName, int iImageWidthMin, int iAspectRatioMax,
                      int iArticleSizeMin, int iArticleChunkSizeMin, ArticleReplace oArticleReplace)
    {
        this.strID = strID;
        this.strURLs = strURLs;
        this.strName = strName;
        this.iImageWidthMin = iImageWidthMin;
        this.iAspectRatioMax = iAspectRatioMax;
        this.iArticleSizeMin = iArticleSizeMin;
        this.iArticleChunkSizeMin = iArticleChunkSizeMin;
        this.oArticleReplace = oArticleReplace;
    }

    /*******************************************************************************************************************
    * @return The source ID (for internal use).
    *******************************************************************************************************************/
    public String getID() 
    {
        return(strID);
    }

    /*******************************************************************************************************************
    * @return The list of RSS URLs for the source.
    *******************************************************************************************************************/
    public Vector<String> getURLs() 
    {
        return(strURLs);
    }

    /*******************************************************************************************************************
    * @return The source name (for display).
    *******************************************************************************************************************/
    public String getName() 
    {
        return(strName);
    }

    /*******************************************************************************************************************
    * @return The minimum article image width (or height if portrait orientation). 
    *******************************************************************************************************************/
    public int getImageWidthMin() 
    {
        return(iImageWidthMin);
    }

    /*******************************************************************************************************************
    * @return The maximum aspect ratio for the article image (long side / short side) 
    *******************************************************************************************************************/
    public int getAspectRatioMax() 
    {
        return(iAspectRatioMax);
    }

    /*******************************************************************************************************************
    * @return The minimum size for the article (all chunks together) 
    *******************************************************************************************************************/
    public int getArticleSizeMin() 
    {
        return(iArticleSizeMin);
    }

    /*******************************************************************************************************************
    * @return The minimum size for an article chunk 
    *******************************************************************************************************************/
    public int getArticleChunkSizeMin() 
    {
        return(iArticleChunkSizeMin);
    }
    
    /*******************************************************************************************************************
    * @return The article replacement rules 
    *******************************************************************************************************************/
    public ArticleReplace getArticleReplace() 
    {
        return(oArticleReplace);
    }
}
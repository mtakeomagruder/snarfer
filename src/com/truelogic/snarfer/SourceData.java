package com.truelogic.snarfer;

import java.util.*;

public class SourceData 
{
    private String strID;
    private Vector<String> strURLs = new Vector<String>();
    private String strName;
    private int iImageWidthMin;
    private int iAspectRatioMax;
    private int iArticleSizeMin;
    private int iArticleChunkSizeMin;
    
    public SourceData(String strID, Vector<String> strURLs, String strName, int iImageWidthMin, int iAspectRatioMax,
                      int iArticleSizeMin, int iArticleChunkSizeMin)
    {
        this.strID = strID;
        this.strURLs = strURLs;
        this.strName = strName;
        this.iImageWidthMin = iImageWidthMin;
        this.iAspectRatioMax = iAspectRatioMax;
        this.iArticleSizeMin = iArticleSizeMin;
        this.iArticleChunkSizeMin = iArticleChunkSizeMin;
    }

    public String getID() 
    {
        return(strID);
    }

    public Vector<String> getURLs() 
    {
        return(strURLs);
    }

    public String getName() 
    {
        return(strName);
    }

    public int getImageWidthMin() 
    {
        return(iImageWidthMin);
    }

    public int getAspectRatioMax() 
    {
        return(iAspectRatioMax);
    }

    public int getArticleSizeMin() 
    {
        return(iArticleSizeMin);
    }

    public int getArticleChunkSizeMin() 
    {
        return(iArticleChunkSizeMin);
    }
}
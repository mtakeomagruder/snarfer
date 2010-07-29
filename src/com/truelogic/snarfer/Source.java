package com.truelogic.snarfer;

import java.util.*;
import java.net.*;

import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;

public class Source
{
    Vector<Article> oArticleList = new Vector<Article>();
    private String strID;
    private String strName;
    private Vector<String> strURLs;
    private int iImageWidthMin;
    private int iAspectRatioMax;
    private int iArticleSizeMin;
    private int iArticleChunkSizeMin;
    
    public Source(String strID, String strName, Vector<String> strURLs, int iImageWidthMin, int iAspectRatioMax, int iArticleSizeMin, int iArticleChunkSizeMin) throws Exception
    {
        if (strID == null || strURLs == null || strURLs.size() == 0)
            throw new Exception("strID and strURL must be set");
        
        this.strID = strID;
        this.strName = strName;
        this.strURLs = strURLs;
        
        this.iImageWidthMin = iImageWidthMin;
        this.iAspectRatioMax = iAspectRatioMax;
        this.iArticleSizeMin = iArticleSizeMin;
        this.iArticleChunkSizeMin = iArticleChunkSizeMin;
    }
    
    public int articleSize()
    {
        return(oArticleList.size());
    }
    
    public Article articleGet(int iIndex)
    {
        return(oArticleList.get(iIndex));
    }
    
    public String getID()
    {
        return(strID);
    }

    public String getName()
    {
        return(strName);
    }
    
    public Vector<String> getURLs()
    {
        return(strURLs);
    }

    public int getImageWidthMin()
    {
        return(iImageWidthMin);
    }

    public int getAspectRatioMax()
    {
        return(iAspectRatioMax);
    }
    
    public int getArticleChunkSizeMin()
    {
        return(iArticleChunkSizeMin);
    }

    public int getArticleSizeMin()
    {
        return(iArticleSizeMin);
    }
    
    public void run()
    {
        oArticleList = getArticleList();
        System.out.println("Articles Found: " + oArticleList.size());
    }

    private Vector<Article> getArticleList()
    {
        Hashtable<String, Article> oArticleHash = new Hashtable<String, Article>();
        return(getArticleList(oArticleHash, getURLs()));
    }
    
    @SuppressWarnings("unchecked")
    private Vector<Article> getArticleList(Hashtable<String, Article> oArticleHash, 
                                           Vector<String> strURLs)
    {
        Vector<Article> oArticleList = new Vector<Article>();
        int iDepth = 1;
        
        /***********************************************************************
        * Attempt to load the URL, create the parser, and create the node list.
        * If any of these operations fail, the the URL is useless and must be
        * discarded.
        ***********************************************************************/
        SyndFeedInput oFeedInput = new SyndFeedInput();
        SyndFeed oFeed; 
            
        for (String strURL : strURLs)
        {
            try
            {
                oFeed = oFeedInput.build(new XmlReader(new URL(strURL)));
                
                List<SyndEntry> oEntries = (List<SyndEntry>)oFeed.getEntries();
                
                oFeed.getEntries();
                
                for (SyndEntry oEntry : oEntries)
                {
                    /*******************************************************************
                     * Make sure the URL has not already been found
                     *******************************************************************/
                     if (oArticleHash.get(oEntry.getUri()) != null)
                         continue;

                     /*******************************************************************
                      * Attempt to retrieve the article and store it if it looks good
                      *******************************************************************/
                      Article oArticle = new Article(this, iDepth, oEntry.getUri(), getImageWidthMin(), getAspectRatioMax(), getArticleSizeMin(), getArticleChunkSizeMin());

                      if (oArticle.retrieve())
                      {
                          if (iDepth == 1)
                              oArticleList.add(0, oArticle);
                          else
                              oArticleList.add(oArticle);
                          
                          oArticleHash.put(oEntry.getUri(), oArticle);
                       
                          System.out.println(oEntry.getUri() + " (" + iDepth + "," + oArticle.getText().length() + ")");
                      }

                      if (iDepth == 1)
                          iDepth = 2;
                }
            }
            catch (Exception oException)
            {
                logError(strURL, oException.getClass().getName(), oException.getMessage());
            }
        }
        
        return(oArticleList);
    }

    private void logError(String strURL, String strExceptionType, String strExceptionText)
    {
        System.out.println(strURL + ": " + strExceptionText);
    }
}
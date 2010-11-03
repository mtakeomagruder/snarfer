package com.truelogic.snarfer;

import java.util.*;
import java.net.*;

import com.sun.syndication.feed.synd.*;
import com.sun.syndication.io.*;

public class Source
{
    private Vector<Article> oArticleList = new Vector<Article>();
    private SourceData oData;
    
    public Source(SourceData oData) throws Exception
    {
        if (oData.getID() == null || oData.getURLs() == null || oData.getURLs().size() == 0)
            throw new Exception("strID and strURL must be set");
        
        this.oData = oData;
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
        return(oData.getID());
    }

    public String getName()
    {
        return(oData.getName());
    }
    
    public Vector<String> getURLs()
    {
        return(oData.getURLs());
    }

    public void run()
    {
        oArticleList = getArticleList();
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
            
        for (String strURL : strURLs)
        {
            try
            {
                SyndFeed oFeed = oFeedInput.build(new XmlReader(new URL(strURL)));
                
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
                      Article oArticle = new Article(this, iDepth, oEntry.getUri(), oData.getImageWidthMin(), 
                                                     oData.getAspectRatioMax(), oData.getArticleSizeMin(), 
                                                     oData.getArticleChunkSizeMin());

                      if (oArticle.retrieve())
                      {
                          if (iDepth == 1)
                              oArticleList.add(0, oArticle);
                          else
                              oArticleList.add(oArticle);
                          
                          oArticleHash.put(oEntry.getUri(), oArticle);
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
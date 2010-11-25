package com.truelogic.snarfer;

// Java imports
import java.util.*;
import java.net.*;

// Third party imports
import org.apache.log4j.Logger;
import com.sun.syndication.feed.synd.*;
import com.sun.syndication.io.*;

public class Source extends Vector<Article>
{
    private static final long serialVersionUID = 1L;
    static Logger oLogger = Logger.getLogger(Source.class);
    
    private SourceData oData;
    
    public Source(SourceData oData) throws Exception
    {
        if (oData.getID() == null || oData.getURLs() == null || oData.getURLs().size() == 0)
            throw new Exception("strID and strURL must be set");
        
        this.oData = oData;
    }
    
    public void run()
    {
        getArticleList(getData().getURLs());
    }

    @SuppressWarnings("unchecked")
    private void getArticleList(Vector<String> strURLs)
    {
        Hashtable<String, Article> oArticleHash = new Hashtable<String, Article>();
        int iDepth = 1;
        
        /***********************************************************************
        * Attempt to load the URL, create the parser, and create the node list.
        * If any of these operations fail, then the URL is useless and must be
        * discarded.
        ***********************************************************************/
        oLogger.info("Initializing RSS reader: " + getData().getName());
        SyndFeedInput oFeedInput = new SyndFeedInput();
            
        for (String strURL : strURLs)
        {
            try
            {
                oLogger.info("Loading RSS feed: " + strURL);
                SyndFeed oFeed = oFeedInput.build(new XmlReader(new URL(strURL)));
                
                List<SyndEntry> oEntries = (List<SyndEntry>)oFeed.getEntries();
                
                oFeed.getEntries();
                
                for (SyndEntry oEntry : oEntries)
                {
                    /***************************************************************************************************
                    * Make sure the URL has not already been found
                    ***************************************************************************************************/
                    if (oArticleHash.get(oEntry.getUri()) != null)
                        continue;

                     /*******************************************************************
                     * Attempt to retrieve the article and store it if it looks good
                     *******************************************************************/
                      Article oArticle = new Article(this, iDepth, oEntry.getUri());

                      if (oArticle.retrieve())
                      {
                          if (iDepth == 1)
                              add(0, oArticle);
                          else
                              add(oArticle);
                          
                          oArticleHash.put(oEntry.getUri(), oArticle);
                      }
                }

                if (iDepth == 1)
                    iDepth = 2;
            }
            catch (Exception oException)
            {
                oLogger.error(strURL, oException);
            }
        }
    }
    
    public SourceData getData()
    {
        return(oData);
    }
}
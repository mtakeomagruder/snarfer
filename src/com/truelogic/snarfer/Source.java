package com.truelogic.snarfer;

// Java imports
import java.util.*;
import java.net.*;

// Third party imports
import org.apache.log4j.*;
import com.sun.syndication.feed.synd.*;
import com.sun.syndication.io.*;

// Project imports
import com.truelogic.snarfer.config.*;
import com.truelogic.snarfer.exception.*;

/***********************************************************************************************************************
* <p>Retrieve articles from each news source.</p>
* 
* This used to be a complicated parsing and guessing process, but now RSS feeds are used to give a discrete list of
* articles.
* 
* @author David Steele
***********************************************************************************************************************/
public class Source extends Vector<Article>
{
    private static final long serialVersionUID = 1L;
    static Logger oLogger = Logger.getLogger(Source.class);
    
    private ConfigSource oConfig;       // Source configuration
    
    /*******************************************************************************************************************
    * Initializes the new source.
    * 
    * @param oConfig  Source configuration
    *******************************************************************************************************************/
    public Source(ConfigSource oConfig) throws SnarferException
    {
        if (oConfig.getID() == null || oConfig.getURLs() == null || oConfig.getURLs().size() == 0)
            throw new SnarferException("strID and strURL must be set");
        
        this.oConfig = oConfig;
    }
    
    /*******************************************************************************************************************
    * Retrieve all the source articles.
    *******************************************************************************************************************/
    public void run()
    {
        getArticleList(getConfig().getURLs());
    }

    /*******************************************************************************************************************
    * Initializes the news source.
    * 
    * @param oConfig  Source configuration
    *******************************************************************************************************************/
    private void getArticleList(Vector<String> strURLs)
    {
        /***************************************************************************************************************
        * This hash table removes duplicate links.  This was a much larger problem when HTML pages were being parsed
        * but has been left in just in case since different RSS feeds might have duplicates.   
        ***************************************************************************************************************/
        Hashtable<String, Article> oArticleHash = new Hashtable<String, Article>();
        int iDepth = 1;
        
        /***************************************************************************************************************
        * Initialize the RSS reader
        ***************************************************************************************************************/
        oLogger.info("Initializing RSS reader: " + getConfig().getName());
        SyndFeedInput oFeedInput = new SyndFeedInput();
            
        /***************************************************************************************************************
        * Loop through the RSS feeds
        ***************************************************************************************************************/
        for (String strURL : strURLs)
        {
            try
            {
                /*******************************************************************************************************
                * Load the feed (this library is Java 1.4 so warnings need to be suppressed when casting)
                *******************************************************************************************************/
                oLogger.info("Loading RSS feed: " + strURL);
                SyndFeed oFeed = oFeedInput.build(new XmlReader(new URL(strURL)));
                
                @SuppressWarnings("unchecked")
                List<SyndEntry> oEntries = (List<SyndEntry>)oFeed.getEntries();
                
                /*******************************************************************************************************
                * Loop through the entries
                *******************************************************************************************************/
                for (SyndEntry oEntry : oEntries)
                {
                    /***************************************************************************************************
                    * Make sure the URL has not already been found
                    ***************************************************************************************************/
                    if (oArticleHash.get(oEntry.getUri()) != null)
                        continue;

                    /***************************************************************************************************
                    * Attempt to retrieve the article
                    ***************************************************************************************************/
                    Article oArticle = new Article(this, iDepth, oEntry.getUri());
                    
                    /***************************************************************************************************
                    * If the article is valid then store it
                    ***************************************************************************************************/
                    if (oArticle.retrieve())
                    {
                        add(oArticle);
                        oArticleHash.put(oEntry.getUri(), oArticle);
                    }
                }

                /*******************************************************************************************************
                * In the old version when HTML articles were being parsed depth was just the number of levels from the
                * site root.  Now the first RSS feed is assumed to be the home page or headline articles and is 
                * assigned a level of 1.  All subsequent feeds are given a level of 2 since RSS does not have a proper
                * hierarchy. 
                *******************************************************************************************************/
                if (iDepth == 1)
                    iDepth = 2;
            }
            catch (Exception oException)
            {
                oLogger.error(strURL, oException);
            }
        }
    }
    
    /*******************************************************************************************************************
    * @returns Source configuration
    *******************************************************************************************************************/
    public ConfigSource getConfig()
    {
        return(oConfig);
    }
}
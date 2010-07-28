package com.truelogic.snarfer;

import java.util.*;
import java.net.*;

import org.htmlparser.*;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.nodes.*;
import org.htmlparser.util.*;
import org.htmlparser.tags.*;

public class Source
{
    Vector<Article> oArticleList = new Vector<Article>();
    private String strID;
    private String strName;
    private String strURL;
    private int iDepthMax;
    private int iImageWidthMin;
    private int iAspectRatioMax;
    private int iArticleSizeMin;
    private int iArticleChunkSizeMin;
    
    public Source(String strID, String strName, String strURL, int iDepthMax, int iImageWidthMin, int iAspectRatioMax, int iArticleSizeMin, int iArticleChunkSizeMin) throws Exception
    {
        if (strID == null || strURL == null)
            throw new Exception("strID and strURL must be set");
        
        this.strID = strID;
        this.strName = strName;
        this.strURL = strURL;
        
        this.iDepthMax = iDepthMax;
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
    
    public String getURL()
    {
        return(strURL);
    }

    public int getDepthMax()
    {
        return(iDepthMax);
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
        return(getArticleList(1, oArticleHash, getURL(), getURL()));
    }
    
    private Vector<Article> getArticleList(int iDepth, 
                                           Hashtable<String, Article> oArticleHash, 
                                           String strBase, 
                                           String strURL)
    {
        NodeList oLinkNodeList = null;
        Vector<Article> oArticleList = new Vector<Article>();
        
        /***********************************************************************
        * Attempt to load the URL, create the parser, and create the node list.
        * If any of these operations fail, the the URL is useless and must be
        * discarded.
        ***********************************************************************/
        try
        {
            URL oURL = new URL(strURL);
            org.htmlparser.Parser oParser = new org.htmlparser.Parser(oURL.openConnection());
            oLinkNodeList = oParser.extractAllNodesThatMatch(new TagNameFilter ("a"));
        }
        catch (Exception oException)
        {
            logError(strURL, oException.getClass().getName(), oException.getMessage());
            return(oArticleList);
        }
        
        /***********************************************************************
        * Now loop through all the link nodes and try to find ones that look 
        * like they lead to articles.
        ***********************************************************************/
        for (int iIdx = 0; iIdx < oLinkNodeList.size (); iIdx++)
        {
            LinkTag oLink = (LinkTag)oLinkNodeList.elementAt(iIdx);
            String strLink = oLink.getLink();

            /*******************************************************************
            * Skip it if:
            * 1) it is not an http link
            * 2) there is a target...probably refers to the same page
            * 3) the link length is 0
            * 4) The link ends with /...probably a section not an article
            * 5) The link does not contain the base link
            *******************************************************************/
            if ((!oLink.isHTTPLink()) || 
                (strLink.indexOf("#") != -1) ||
                (strLink.length() == 0) ||
                (strLink.endsWith(".png")) ||
                (strLink.endsWith(".jpg")) ||
                (strLink.endsWith(".pdf")) ||
                (strLink.endsWith(".ram")) ||
                (strLink.endsWith(".gif")) ||
                (strLink.endsWith("/")) ||
                (!strLink.toLowerCase().startsWith(strBase.toLowerCase())))
                continue;

            /*******************************************************************
            * See if there are any images contained in the link.  If there is 
            * more than one image, it's probably no good, so skip it. If the 
            * link is a gif, also skip it.
            *******************************************************************/
            NodeList oImageNodeList = oLink.searchFor((new ImageTag()).getClass(), true);
            
            if (oImageNodeList.size() == 1)
            {
                ImageTag oImage = (ImageTag)oImageNodeList.elementAt(0);

                if (oImage.getImageURL().indexOf(".gif") != -1)
                    continue;
            }
            else if (oImageNodeList.size() > 1)
                continue;

            /*******************************************************************
            * Make sure the URL has not already been found
            *******************************************************************/
            if (oArticleHash.get(strLink) != null)
                continue;

            /*******************************************************************
            * Attempt to retrieve the article and store it if it looks good
            *******************************************************************/
            Article oArticle = new Article(this, iDepth, strLink, getImageWidthMin(), getAspectRatioMax(), getArticleSizeMin(), getArticleChunkSizeMin());
            
            if (oArticle.retrieve())
            {
                if (iDepth == 1)
                    oArticleList.add(0, oArticle);
                else
                    oArticleList.add(oArticle);
                
                oArticleHash.put(strLink, oArticle);
             
                System.out.println(strLink + " (" + iDepth + "," + oArticle.getText().length() + ")");
            }
        }

        /***********************************************************************
        * Now loop through all the link nodes and try to find ones that look 
        * like they lead to other pages with articles.
        ***********************************************************************/
        for (int iIdx = 0; iIdx < oLinkNodeList.size (); iIdx++)
        {
            LinkTag oLink = (LinkTag)oLinkNodeList.elementAt(iIdx);
            String strLink = oLink.getLink();

            /*******************************************************************
            * Skip it if:
            * 1) it is not an http link
            * 2) there is a target...probably refers to the same page
            * 3) the link length is 0
            * 4) The link ends with /...probably a section not an article
            *******************************************************************/
            if ((!oLink.isHTTPLink()) || 
                (oLink.getLink().indexOf("#") != -1) ||
                (strLink.endsWith(".png")) ||
                (strLink.endsWith(".jpg")) ||
                (strLink.endsWith(".pdf")) ||
                (strLink.endsWith(".ram")) ||
                (strLink.endsWith(".gif")) ||
                (oLink.getLink().length() == 0) ||
                (!strURL.toLowerCase().startsWith(strBase.toLowerCase())))
                continue;
            
            /*******************************************************************
            * Recurse to the next level
            *******************************************************************/
            if (iDepth < getDepthMax())
                oArticleList.addAll(getArticleList(iDepth + 1, oArticleHash, strBase, strLink));
        }
        
        return(oArticleList);
    }

    private void logError(String strURL, String strExceptionType, String strExceptionText)
    {
    }
}
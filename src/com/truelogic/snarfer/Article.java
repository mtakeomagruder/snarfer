package com.truelogic.snarfer;

// Java imports
import java.util.*;
import java.net.*;
import java.io.*;
import java.awt.image.*;
import javax.imageio.*;
import javax.imageio.stream.*;

// Third Party imports
import org.apache.log4j.*;
import org.htmlparser.*;
import org.htmlparser.filters.*;
import org.htmlparser.nodes.*;
import org.htmlparser.util.*;
import org.htmlparser.tags.*;

/***********************************************************************************************************************
* <p>This class attempts to parse an article from a URL.</p>
* 
* <p>News sites format articles in a variety of ways.  There are four major parameters (from the Source object) input 
* to this function to help identify real news article:</p>
* 
* <ul>
* <li>ImageWidthMin - Real articles generally have a decent sized image associated with them.  Since the purpose of the
* snarfer is to get articles <b>and</b> images at least one image of specified size must be found</li>
* <li>AspectRatioMax - Advertisements tend to be very long or very tall.  This parameter specifies the maximum ratio of
* the longest side / the shortest side to eliminate obvious ad images.</li>
* <li>ArticleChunkSizeMin - Articles are often broken into individual DIVs (often by paragrah).  This parameter
* specifies the minimum size that will be included in the article.  This tends to eliminate section headers and other
* cruft.</li>
* </li>ArticleSizeMin - When all the chunks have been combined the article must be of a certain length to be considered
* valid.</li>
* </ul>
* 
* <p>These parameters may be set to make either images or text irrelevant if that is the desired effect.</p>
* 
* @author David Steele
***********************************************************************************************************************/
public class Article 
{
    static Logger oLogger = Logger.getLogger(Article.class);

    private Source oSource;                 // The source news feed
    private String strURL;                  // The article URL
    private int iTier;                      // 1 if the first RSS feed, 2 otherwise

    private boolean bGood = false;          // Is the article good - were all text/image requirements met?
    private byte[] oImageBuffer = null;     // Byte array containing the raw image article
    private String strImageURL = null;      // The image URL
    private String strText = null;          // The article text
    
    /*******************************************************************************************************************
    * Initalizes the article object.
    * 
    * @param oSource  The news source object
    * @param iTier    The tier (1 is top level, 2 or greater for all else)
    * @param strURL   The article URL
    *******************************************************************************************************************/
    public Article(Source oSource, int iTier, String strURL)
    {
        oLogger.info("Loading article: " + strURL + " (" + iTier + ")");

        this.oSource = oSource;
        
        this.strURL = strURL;
        this.iTier = iTier;
    }
    
    /*******************************************************************************************************************
    * Marks this article as having a valid image and text.
    *******************************************************************************************************************/
    private void setGood()
    {
        oLogger.info("Article is good");
        bGood = true;
    }
    
    /*******************************************************************************************************************
    * Marks this article as having a valid image and text.
    * 
    * @return Is the article valid?
    *******************************************************************************************************************/
    public boolean retrieve()
    {
        org.htmlparser.Parser oParser;
        
        try
        {
            /***********************************************************************************************************
            * Parse the article HTML
            ***********************************************************************************************************/
            URL oURL = new URL(strURL);
            oParser = new org.htmlparser.Parser(oURL.openConnection());
            
            /***********************************************************************************************************
            * Attempt to retrieve the image
            ***********************************************************************************************************/
            oImageBuffer = retrieveImage(oParser);
            
            if (oImageBuffer == null)
                return(isGood());
            
            oParser.reset();
            
            /***********************************************************************************************************
            * Attempt to retrieve the text
            ***********************************************************************************************************/
            strText = retrieveText(oParser);
            
            if (strText == null)
                return(isGood());
            
            setGood();
        }
        catch (Exception oException)
        {
            oLogger.error(strURL, oException);
            return(isGood());
        }
        
        /***************************************************************************************************************
        * At this point the article should be valid
        ***************************************************************************************************************/
        return(isGood());
    }

    /*******************************************************************************************************************
    * Retrieves the article body element.
    * 
    * @param oParser A parsed representation of the HTML page
    * 
    * @return The article text if it is valid, null otherwise
    *******************************************************************************************************************/
    private String retrieveText(org.htmlparser.Parser oParser)
    {
        NodeList oBodyNodeList = null;
        String strText = null;

        oLogger.info("Retrieving article text");
        
        /***************************************************************************************************************
        * Search for the BODY element
        ***************************************************************************************************************/
        try
        {
            oBodyNodeList = oParser.extractAllNodesThatMatch(new TagNameFilter("body"));
        }
        catch (Exception oException)
        {
            oLogger.error(getURL(), oException);
            return(null);
        }
        
        /***************************************************************************************************************
        * If no body element then return error
        ***************************************************************************************************************/
        if (oBodyNodeList.size() != 1)
            return(null);
        
        /***************************************************************************************************************
        * Search the BODY element for a valid article 
        ***************************************************************************************************************/
        strText = retrieveText(1, oBodyNodeList.elementAt(0));

        /***************************************************************************************************************
        * If the article does not look valid then return error 
        ***************************************************************************************************************/
        if ((strText != null) && (strText.length() < oSource.getConfig().getArticleSizeMin()))
            strText = null;
            
        /***************************************************************************************************************
        * Return the valid article 
        ***************************************************************************************************************/
        return(strText);
    }
    
    /*******************************************************************************************************************
    * Retrieves the article from the body element.
    * 
    * @param oParser A parsed representation of the BODY element
    * 
    * @return The article text if it is valid, null otherwise
    *******************************************************************************************************************/
    private String retrieveText(int iDepth, Node oTextNode)
    {
        NodeList oTextNodeList = null;
        String strText = "";
        
        /***************************************************************************************************************
        * If there is a problem getting the BODY nodes then return error 
        ***************************************************************************************************************/
        try
        {
            oTextNodeList = oTextNode.getChildren();
        }
        catch (Exception oException)
        {
            oLogger.error(getURL(), oException);
            return(null);
        }
        
        /***************************************************************************************************************
        * If there is only a single test node process it
        ***************************************************************************************************************/
        if (oTextNodeList == null)
        {
            String strTemp = "";

            /***********************************************************************************************************
            * If the node can contain text then continue processing 
            ***********************************************************************************************************/
            if (oTextNode instanceof TextNode)
            {
                strTemp = oTextNode.getText();
                
                /*******************************************************************************************************
                * If there are no periods then this is probably not an article, return error 
                *******************************************************************************************************/
                if (strTemp.indexOf(".") == -1)
                    return(null);

                /*******************************************************************************************************
                * If there is an ellipsis then return error (can't remember why I did this...)  
                *******************************************************************************************************/
                if (strTemp.indexOf("...") != -1)
                    return(null);

                /*******************************************************************************************************
                * If an HTML comment is found then return error  
                *******************************************************************************************************/
                if (strTemp.indexOf("<!--") != -1)
                    return(null);
                
                /*******************************************************************************************************
                * Replace all strings defined in the rules   
                *******************************************************************************************************/
                for (String strReplace : oSource.getConfig().getArticleReplace().getOrderedKeys())
                    strTemp = strTemp.replaceAll(strReplace, oSource.getConfig().getArticleReplace().get(strReplace));

                strTemp = strTemp.trim();
                
                /*******************************************************************************************************
                * Replace double (or greater) spaces with single spaces.   
                *******************************************************************************************************/
                while (strTemp.indexOf("  ") != -1)
                    strTemp = strTemp.replaceAll("  ", " ");

                if (strTemp.length() < oSource.getConfig().getArticleChunkSizeMin())
                    return(null);
            }

            /***********************************************************************************************************
            * If nothing is left of the test return error 
            ***********************************************************************************************************/
            if (strTemp.equals(""))
                return(null);
            /***********************************************************************************************************
            * Else return the string 
            ***********************************************************************************************************/
            else
                return(strTemp);
        }

        /***************************************************************************************************************
        * Process the list of text nodes 
        ***************************************************************************************************************/
        for (int iIndex = 0; iIndex < oTextNodeList.size(); iIndex++)
        {
            Node oChildNode = oTextNodeList.elementAt(iIndex);

            /***********************************************************************************************************
            * Process tags that might have text 
            ***********************************************************************************************************/
            if ((oChildNode instanceof Div) || (oChildNode instanceof TableTag) ||
                (oChildNode instanceof TableRow) || (oChildNode instanceof TableColumn) ||
                (oChildNode instanceof TextNode) || (oChildNode instanceof Span)  || 
                (oChildNode instanceof ParagraphTag))
            {
                String strTemp = retrieveText(iDepth + 1, oChildNode);

                if (strTemp != null)
                    strText += " " + strTemp.trim();
            }
        }

        /***********************************************************************************************************
        * If the test length is zero then return error 
        ***********************************************************************************************************/
        if (strText.length() == 0)
            return(null);

        /***********************************************************************************************************
        * Return the article text 
        ***********************************************************************************************************/
        return(strText.trim());
    }
    
    /*******************************************************************************************************************
    * Retrieves the article image.
    * 
    * @param oParser A parsed representation of the HTML page
    * 
    * @return The article image if it is valid, null otherwise
    *******************************************************************************************************************/
    private byte[] retrieveImage(org.htmlparser.Parser oParser) 
    {
        NodeList oImageNodeList = null;
        String strImageURL = null;
        int iImageHeight = 0;
        int iImageWidth = 0;
        byte[] oBuffer = null;
        
        oLogger.info("Retrieving article image");

        /***************************************************************************************************************
        * Try to find any nodes that have IMG tags 
        ***************************************************************************************************************/
        try
        {
            oImageNodeList = oParser.extractAllNodesThatMatch (new TagNameFilter ("img"));
        }
        catch (Exception oException)
        {
            oLogger.error(getURL(), oException);
            return(null);
        }
        
        /***************************************************************************************************************
        * Iterate through the image nodes 
        ***************************************************************************************************************/
        for (int iIdx = 0; iIdx < oImageNodeList.size (); iIdx++)
        {
            ImageTag oImage = (ImageTag)oImageNodeList.elementAt (iIdx);

            /***********************************************************************************************************
            * Get the image height and width 
            ***********************************************************************************************************/
            float iAspectRatio = 0;
            int iHeight = getAttributeInt(oImage, "height", 0);
            int iWidth = getAttributeInt(oImage, "width", 0);

            /***********************************************************************************************************
            * Skip this image if the height or the width are not specified 
            ***********************************************************************************************************/
            if ((iWidth == 0) || (iHeight == 0))
                continue;

            /***********************************************************************************************************
            * Skip this image if the width does not meet the minumum 
            ***********************************************************************************************************/
            if (iWidth < oSource.getConfig().getImageWidthMin())
                continue;

            /***********************************************************************************************************
            * Skip this image if it is not a JPEG 
            ***********************************************************************************************************/
            if (!oImage.getImageURL().toLowerCase().endsWith(".jpg"))
                continue;

            /***********************************************************************************************************
            * Skip this image if it is greater than the maximum aspect ratio 
            ***********************************************************************************************************/
            iAspectRatio = (float)iWidth / (float)iHeight;

            if ((iAspectRatio > oSource.getConfig().getAspectRatioMax()) || (iAspectRatio < 1))
                continue;

            /***********************************************************************************************************
            * Keep this image if it is bigger than the previous image 
            ***********************************************************************************************************/
            if ((strImageURL == null) || ((iWidth * iHeight) > (iImageHeight * iImageWidth)))
            {
                strImageURL = oImage.getImageURL();
                iImageWidth = iWidth;
                iImageHeight = iHeight;
            }
        }
        
        /***************************************************************************************************************
        * If a suitable image was found, retrieve and process it 
        ***************************************************************************************************************/
        if (strImageURL != null)
        {
            URL oURL = null;
            HttpURLConnection oHTTP = null;
            InputStream oInput = null;

            try
            {
                /*******************************************************************************************************
                * Get the image 
                *******************************************************************************************************/
                oURL = new URL(strImageURL);
                oHTTP = (HttpURLConnection) oURL.openConnection();
                
                oInput = oHTTP.getInputStream();
                
                /*******************************************************************************************************
                * Strip the border 
                *******************************************************************************************************/
                BufferedImage oImage = ImageIO.read(oInput);
                BufferedImage oNewImage = oImage.getSubimage(oSource.getConfig().getAspectRatioMax(), 
                                                             oSource.getConfig().getAspectRatioMax(), 
                                                             oImage.getWidth() - 
                                                             (oSource.getConfig().getAspectRatioMax() * 2), 
                                                             oImage.getHeight() - 
                                                             (oSource.getConfig().getAspectRatioMax() * 2));

                /*******************************************************************************************************
                * Convert the image to JPEG 
                *******************************************************************************************************/
                ByteArrayOutputStream oOutput = new ByteArrayOutputStream();
                MemoryCacheImageOutputStream oImageOutput = new MemoryCacheImageOutputStream(oOutput);
                Iterator<ImageWriter> oImageIterator = ImageIO.getImageWritersByFormatName("jpeg");
                ImageWriter oImageWriter = (ImageWriter)oImageIterator.next();
                ImageWriteParam oImageWriterParam = oImageWriter.getDefaultWriteParam();
                oImageWriterParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                oImageWriterParam.setCompressionQuality((float)0.9);
                
                oImageWriter.setOutput(oImageOutput);
                IIOImage oImageTemp = new IIOImage(oNewImage, null, null);
                oImageWriter.write(null, oImageTemp, oImageWriterParam);
                oImageWriter.dispose();
                
                /*******************************************************************************************************
                * Store the image in a byte array 
                *******************************************************************************************************/
                oBuffer = oOutput.toByteArray();
                this.strImageURL = strImageURL;
            }
            catch (Exception oException)
            {
                oLogger.error(getURL(), oException);
                return(null);
            }
        }

        /***************************************************************************************************************
        * Return the image 
        ***************************************************************************************************************/
        return(oBuffer);
    }

    /*******************************************************************************************************************
    * Converts an HTML attribute to an integer.
    * 
    * @param oNode The node containing an integer attribute
    * 
    * @return The integer value of the attribute 
    *******************************************************************************************************************/
    private static int getAttributeInt(TagNode oNode, String strAttribute)
    {
        return((new Integer(oNode.getAttribute(strAttribute))).intValue());
    }

    /*******************************************************************************************************************
    * Converts an HTML attribute to an integer.
    * 
    * @param oNode     The node containing an integer attribute
    * @param iDefault  The default to use if the integer is invalid
    * 
    * @return The integer value of the attribute (or the default) 
    *******************************************************************************************************************/
    private static int getAttributeInt(TagNode oNode, String strAttribute, int iDefault)
    {
        try
        {
            return(getAttributeInt(oNode, strAttribute));
        }
        catch (Exception oException)
        {
            return(iDefault);
        }
    }
    
    /*******************************************************************************************************************
    * @return Are the article image and text valid? 
    *******************************************************************************************************************/
    public boolean isGood()
    {
        return(bGood);
    }

    /*******************************************************************************************************************
    * @return The article URL 
    *******************************************************************************************************************/
    public String getURL()
    {
        return(strURL);
    }

    /*******************************************************************************************************************
    * @return The article image URL
    *******************************************************************************************************************/
    public String getImageURL()
    {
        return(strImageURL);
    }

    /*******************************************************************************************************************
    * @return The article text
    *******************************************************************************************************************/
    public String getText()
    {
        return(strText);
    }
    
    /*******************************************************************************************************************
    * @return The raw article image
    *******************************************************************************************************************/
    public byte[] getImage()
    {
        return(oImageBuffer);
    }
    
    /*******************************************************************************************************************
    * @return The article tier (1 is top level, 2 or greater for all else) 
    *******************************************************************************************************************/
    public int getTier()
    {
        return(iTier);
    }
}
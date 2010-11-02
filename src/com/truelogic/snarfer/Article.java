package com.truelogic.snarfer;

import java.util.*;
import java.net.*;
import java.io.*;
import java.awt.image.*;
import javax.imageio.*;
import javax.imageio.stream.MemoryCacheImageOutputStream;

import org.htmlparser.*;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.nodes.*;
import org.htmlparser.util.*;
import org.htmlparser.tags.*;

public class Article 
{
    private String strURL;
    private int iTier;
    private boolean bGood = false;
    private int iImageWidthMin;
    private int iAspectRatioMax;
    private int iArticleChunkSizeMin;
    private int iArticleSizeMin;
    private byte[] oImageBuffer = null;
    private String strText = null;
    private String strImageURL = null;
    
    public Article(Source oSource, int iTier, String strURL, int iImageWidthMin, int iAspectRatioMax, 
                   int iArticleSizeMin, int iArticleChunkSizeMin)
    {
        this.strURL = strURL;
        this.iTier = iTier;
        
        this.iImageWidthMin = iImageWidthMin;
        this.iAspectRatioMax = iAspectRatioMax;
        this.iArticleSizeMin = iArticleSizeMin;
        this.iArticleChunkSizeMin = iArticleChunkSizeMin;
    }
    
    public boolean isGood()
    {
        return(bGood);
    }

    private void setGood()
    {
        bGood = true;
    }
    
    public String getURL()
    {
        return(strURL);
    }

    public String getImageURL()
    {
        return(strImageURL);
    }

    public void setImageURL(String strImageURL)
    {
        this.strImageURL = strImageURL;
    }

    public String getText()
    {
        return(strText);
    }
    
    public byte[] getImage()
    {
        return(oImageBuffer);
    }
    
    public int getTier()
    {
        return(iTier);
    }

    public int getImageWidthMin()
    {
        return(iImageWidthMin);
    }

    public int getArticleChunkSizeMin()
    {
        return(iArticleChunkSizeMin);
    }

    public int getArticleSizeMin()
    {
        return(iArticleSizeMin);
    }

    public boolean retrieve()
    {
        org.htmlparser.Parser oParser;
        
        try
        {
            URL oURL = new URL(strURL);
            oParser = new org.htmlparser.Parser(oURL.openConnection());
            
            oImageBuffer = retrieveImage(oParser);
            
            if (oImageBuffer == null)
                return(isGood());
            
            oParser.reset();
            
            strText = retrieveText(oParser);
            
            if (strText == null)
                return(isGood());
            
            setGood();
        }
        catch (Exception oException)
        {
            logError(getURL(), oException.getClass().getName(), oException.getMessage());
            return(isGood());
        }
        
        
        return(isGood());
    }

    private String retrieveText(org.htmlparser.Parser oParser)
    {
        NodeList oBodyNodeList = null;
        String strText = null;
        
        try
        {
            oBodyNodeList = oParser.extractAllNodesThatMatch(new TagNameFilter("body"));
        }
        catch (Exception oException)
        {
            logError(getURL(), oException.getClass().getName(), oException.getMessage());
            return(null);
        }
        
        if (oBodyNodeList.size() != 1)
            return(null);
        
        strText = retrieveText(1, oBodyNodeList.elementAt(0));

        if ((strText != null) && (strText.length() < getArticleSizeMin()))
            strText = null;
            
        return(strText);
    }
    
    private String retrieveText(int iDepth, Node oTextNode)
    {
        NodeList oTextNodeList = null;
        String strText = "";
        
        try
        {
            oTextNodeList = oTextNode.getChildren();
        }
        catch (Exception oException)
        {
            logError(getURL(), oException.getClass().getName(), oException.getMessage());
            return(null);
        }
        
        if (oTextNodeList == null)
        {
            String strTemp = "";

            if (oTextNode instanceof TextNode)
            {
                strTemp = oTextNode.getText();
                
                if (strTemp.indexOf(".") == -1)
                    return(null);

                if (strTemp.indexOf("...") != -1)
                    return(null);

                if (strTemp.indexOf("<!--") != -1)
                    return(null);

                strTemp = strTemp.replaceAll("&nbsp;", " ");
                strTemp = strTemp.replaceAll("&quot;", "\"");
                strTemp = strTemp.replaceAll("&#151;", "[");
                strTemp = strTemp.replaceAll("&#93;", "]");
                strTemp = strTemp.replaceAll("\n\r", " ");
                strTemp = strTemp.replaceAll("\r\n", " ");
                strTemp = strTemp.replaceAll("\r", " ");
                strTemp = strTemp.replaceAll("\n", " ");
                strTemp = strTemp.trim();
                
                while (strTemp.indexOf("  ") != -1)
                    strTemp = strTemp.replaceAll("  ", " ");

                if (strTemp.length() < getArticleChunkSizeMin())
                    return(null);
            }

            if (strTemp.equals(""))
                return(null);
            else
                return(strTemp);
        }

        for (int iIndex = 0; iIndex < oTextNodeList.size(); iIndex++)
        {
            Node oChildNode = oTextNodeList.elementAt(iIndex);

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

        if (strText.length() == 0)
            return(null);
        else
            return(strText.trim());
    }
    
    private byte[] retrieveImage(org.htmlparser.Parser oParser) 
    {
        NodeList oImageNodeList = null;
        String strImageURL = null;
        int iImageHeight = 0;
        int iImageWidth = 0;
        byte[] oBuffer = null;
        
        try
        {
            oImageNodeList = oParser.extractAllNodesThatMatch (new TagNameFilter ("img"));
        }
        catch (Exception oException)
        {
            logError(getURL(), oException.getClass().getName(), oException.getMessage());
            return(null);
        }
        
        for (int iIdx = 0; iIdx < oImageNodeList.size (); iIdx++)
        {
            ImageTag oImage = (ImageTag)oImageNodeList.elementAt (iIdx);

            float iAspectRatio = 0;
            int iHeight = getAttributeInt(oImage, "height", 0);
            int iWidth = getAttributeInt(oImage, "width", 0);

            if ((iWidth == 0) || (iHeight == 0))
                continue;

            if (iWidth < iImageWidthMin)
                continue;

            if (!oImage.getImageURL().toLowerCase().endsWith(".jpg"))
                continue;

            iAspectRatio = (float)iWidth / (float)iHeight;

            if ((iAspectRatio > iAspectRatioMax) || (iAspectRatio < 1))
                continue;

            if ((strImageURL == null) || ((iWidth * iHeight) > (iImageHeight * iImageWidth)))
            {
                strImageURL = oImage.getImageURL();
                iImageWidth = iWidth;
                iImageHeight = iHeight;
            }
        }
        
        if (strImageURL != null)
        {
            URL oURL = null;
            HttpURLConnection oHTTP = null;
            InputStream oInput = null;

            try
            {
                oURL = new URL(strImageURL);
                oHTTP = (HttpURLConnection) oURL.openConnection();
                int iBorder = 1;
                
                oInput = oHTTP.getInputStream();
                
                BufferedImage oImage = ImageIO.read(oInput);
                BufferedImage oNewImage = oImage.getSubimage(iBorder, iBorder, oImage.getWidth() - 
                                                             (iBorder * 2), oImage.getHeight() - (iBorder * 2));

                ByteArrayOutputStream oOutput = new ByteArrayOutputStream();
                MemoryCacheImageOutputStream oImageOutput = new MemoryCacheImageOutputStream(oOutput);
                Iterator<ImageWriter> oImageIterator = ImageIO.getImageWritersByFormatName("jpeg");
                ImageWriter oImageWriter = (ImageWriter)oImageIterator.next();
                ImageWriteParam oImageWriterParam = oImageWriter.getDefaultWriteParam();
                oImageWriterParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                oImageWriterParam.setCompressionQuality((float) .9);   // an integer between 0 and 1
                
                oImageWriter.setOutput(oImageOutput);
                IIOImage oImageTemp = new IIOImage(oNewImage, null, null);
                oImageWriter.write(null, oImageTemp, oImageWriterParam);
                oImageWriter.dispose();
                
                oBuffer = oOutput.toByteArray();
                setImageURL(strImageURL);
            }
            catch (Exception oException)
            {
                logError(getURL(), oException.getClass().getName(), oException.getMessage());
                return(null);
            }
        }

    return(oBuffer);
    }

    private void logError(String strURL, String strExceptionType, String strExceptionText)
    {
    }
    
    private static int getAttributeInt(TagNode oNode, String strAttribute)
    {
        return((new Integer(oNode.getAttribute(strAttribute))).intValue());
    }

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
    
}

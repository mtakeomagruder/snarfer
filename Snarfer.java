/*******************************************************************************
* Imports
*******************************************************************************/
import java.net.*;
import java.lang.*;
import java.io.*;
import java.util.*;

import org.htmlparser.*;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.nodes.*;
import org.htmlparser.util.*;
import org.htmlparser.tags.*;

import com.truelogic.common.*;

/*******************************************************************************
* Snarfer Class
* All the code has been written into this class in an attempt to make
* things simple for those who would attempt modification.  It's not
* the ideal structure, but it is easy to read.
*******************************************************************************/
public class Snarfer
{
    private static class Source
    {
        public String strID;
        public String strURL;
        public int iArticleCount = 0;
    }

    private static Vector oSources = new Vector();

    private static int iImageWidthMin = 150;
    private static int iAspectRatioMax = 2;
    private static int iArticleSizeMin = 1000;
    private static int iArticleChunkSizeMin = 100;

    private static String strOutputDir = "";

    public static void writeLog(String strEntry)
    {
        writeLog(strEntry, false);
    }

    public static void writeLog(String strEntry, boolean bError)
    {
        String strPreface = "";

        if (bError)
            strPreface = "ERROR: ";

        System.out.println(strPreface + strEntry);
    }

    public static boolean loadParams() throws IOException
    {
        try
        {
            IniFile oIni = new IniFile("snarfer.ini");

            int iSourceCount = oIni.IntGet("sources", "count", 0);

            for (int iSourceIdx = 0; iSourceIdx < iSourceCount; iSourceIdx++)
            {
                Source oSource = new Source();

                oSource.strID = oIni.StringGet("sources", "source" + (iSourceIdx + 1));
                oSource.strURL = oIni.StringGet(oSource.strID, "url");

                oSources.add(oSource);
            }

            iImageWidthMin = oIni.IntGet("defaults", "image_width_min", iImageWidthMin);
            iAspectRatioMax = oIni.IntGet("defaults", "aspect_ratio_max", iAspectRatioMax);
            iArticleSizeMin = oIni.IntGet("defaults", "article_size_min", iArticleSizeMin);
            iArticleChunkSizeMin = oIni.IntGet("defaults", "article_chunk_size_min", iArticleChunkSizeMin);
            strOutputDir = oIni.StringGet("defaults", "output_dir", strOutputDir);
        }
        catch (Exception oException)
        {
            writeLog("Unable to read ini file (" + oException.getMessage() + ")", true);
            return(false);
        }
        return(true);
    }

    public static void main(String [ ] args) throws IOException, MalformedURLException, org.htmlparser.util.ParserException
    {
        /***********************************************************************
        * Load the parameters
        ***********************************************************************/
        if (loadParams() == false)
            System.exit(-1);

        /***********************************************************************
        * Delete old files
        ***********************************************************************/
/*       try
       {
           System.out.println("rm -f " + strOutputDir + getDate() + "*");
           System.out.println(Runtime.getRuntime().exec("arm -f " + strOutputDir + getDate() + "*").waitFor() + "");
       }
       catch (Exception oException) {}*/

        FileWriter oControl = new FileWriter(strOutputDir + getDate() + ".txt");

        oControl.write("sources:" + oSources.size() + "\n");

        for (int iSourceIdx = 0; iSourceIdx < oSources.size(); iSourceIdx++)
        {
            Source oSource = (Source)oSources.get(iSourceIdx);

            Vector oArticles = getArticleList(oSource);

            for (int iArticleIndex = 0; iArticleIndex < oArticles.size(); iArticleIndex++)
            {
                String strURL = (String)oArticles.get(iArticleIndex);
                getArticle(oSource, strURL);
            }

            oControl.write(oSource.strID + ":" + oSource.iArticleCount + "\n");
        }

        oControl.close();
    }

    private static Vector getArticleList(Source oSource) throws IOException, MalformedURLException, org.htmlparser.util.ParserException
    {
    Vector oList = new Vector();

    URL oURL = new URL(oSource.strURL);
//    HttpURLConnection oHTTP = (HttpURLConnection) oURL.openConnection();
    org.htmlparser.Parser oParser = new org.htmlparser.Parser(oURL.openConnection());

    NodeList list = oParser.extractAllNodesThatMatch (new TagNameFilter ("a"));

    for (int i = 0; i < list.size (); i++)
        {
        LinkTag oLink = (LinkTag)list.elementAt (i);

        if (!oLink.isHTTPLink())
            continue;

        if (oLink.getLink().indexOf("#") != -1)
            continue;

        NodeList oImages = oLink.searchFor((new ImageTag()).getClass(), true);

        if (oImages.size() == 1)
        {
            ImageTag oImage = (ImageTag)oImages.elementAt(0);

            if (oImage.getImageURL().indexOf(".gif") != -1)
                continue;
        }
        else if (oImages.size() > 1)
            continue;

        if ((oLink.getLink().length() == 0) || (oLink.getLink().endsWith("/")))
            continue;

        boolean bFound = false;

        for (int iIndex = 0; iIndex < oList.size(); iIndex++)
        {
        String strInList = (String)oList.get(iIndex);
        String strNew = oLink.getLink();

        if (strInList.equals(strNew) == true)
            bFound = true;
        }

        if (bFound == false)
        {
            oList.add(oLink.getLink());
 //           System.out.println("link: " + oLink.getLink());
        }

        }

    return(oList);
    }

    private static void getArticle(Source oSource, String strURL) throws IOException, MalformedURLException, org.htmlparser.util.ParserException
    {
        String strArticleText = null;
        String strImageURL = null;
        int iImageWidth = 0;
        int iImageHeight = 0;

        org.htmlparser.Parser oParser;

        try
        {
        URL oURL = new URL(strURL);

//        HttpURLConnection oHTTP = (HttpURLConnection) oURL.openConnection();
        oParser = new org.htmlparser.Parser(oURL.openConnection());
        }
        catch (Exception oException)
        {
            return;
        }


        NodeList list = oParser.extractAllNodesThatMatch (new TagNameFilter ("img"));

        for (int i = 0; i < list.size (); i++)
            {
            ImageTag oImage = (ImageTag)list.elementAt (i);

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
            oParser.reset();
            NodeList oList = oParser.extractAllNodesThatMatch (new TagNameFilter ("body"));

            if (oList.size() == 1)
            {
                strArticleText = getText(oList.elementAt(0));

            if ((strArticleText != null) && (strArticleText.length() < iArticleSizeMin))
                strArticleText = null;
            }
        }

        if (strArticleText != null)
        {
//            System.out.println("\nURL: " + strURL + ", IMG: " + strImageURL + ", AR: " + (float)iImageWidth /(float)iImageHeight);
//            System.out.println("Text (" + strArticleText.length() + "): " + strArticleText);
            saveArticle(oSource, strImageURL, strArticleText);
        }
    }

    public static String getDate()
    {
        Calendar oDate = Calendar.getInstance();

        return(oDate.get(Calendar.YEAR) + padInt(oDate.get(Calendar.MONTH) + 1, 2) + padInt(oDate.get(Calendar.DAY_OF_MONTH), 2));
    }

    public static String padInt(int iInt, int iPad)
    {
        String strInt = "" + iInt;

        while (strInt.length() < iPad)
            strInt = "0" + strInt;

        return(strInt);
    }

    private static boolean saveArticle(Source oSource, String strImageURL, String strArticleText)
    {
        try
        {
            Calendar oDate = Calendar.getInstance();

            String strDate = getDate();
            String strFileName = strDate + "-" + oSource.strID + "-" + padInt(oSource.iArticleCount, 3);

            FileWriter oWriter = new FileWriter(strOutputDir + strFileName + ".txt");

            oWriter.write(strArticleText);
            oWriter.close();

            URL oURL = new URL(strImageURL);
            HttpURLConnection oHTTP = (HttpURLConnection) oURL.openConnection();

            FileOutputStream oStream = new FileOutputStream(strOutputDir + strFileName + ".jpg");
            InputStream oInput = oHTTP.getInputStream();

            int c;

            while ((c = oInput.read()) != -1)
                oStream.write(c);

            oStream.close();
            oHTTP.disconnect();
        }
        catch (Exception oException)
        {
            writeLog("Unable to save article (" + oException.getMessage() + ")", true);
            return(false);
        }
        oSource.iArticleCount++;
        return(true);
    }

    private static String getText(Node oNode)
    {
        String strText = "";

        NodeList oList = oNode.getChildren();

        if (oList == null)
        {
            String strTemp = "";

//            System.out.println(oNode.getClass().toString());

            if (oNode instanceof TextNode)
            {
//                CompositeTag oTag = (CompositeTag)oNode;
                strTemp = oNode.getText();

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

                if (strTemp.length() < iArticleChunkSizeMin)
                    return(null);
            }

            return(strTemp);
        }

        for (int iIndex = 0; iIndex < oList.size(); iIndex++)
        {
            Node oChildNode = oList.elementAt(iIndex);

            if ((oChildNode instanceof Div) || (oChildNode instanceof TableTag) ||
                (oChildNode instanceof TableRow) || (oChildNode instanceof TableColumn) ||
                (oChildNode instanceof TextNode))
            {
                String strTemp = getText(oChildNode);
                if (strTemp != null)
                    strText += " " + strTemp.trim();
            }
//            else
//                System.out.println(oChildNode.getClass().toString());
        }

        if (strText.length() == 0)
            return(null);
        else
            return(strText.trim());
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



/*    private int stringToInt(String strInt, int iDefault)
    {
    try
    {
        return(new Integer().valueOf(strInt));
    }
    catch (Exception oException)
    {
        return(iDefault);
    }
    }*/
}

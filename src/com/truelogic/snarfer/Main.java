package com.truelogic.snarfer;

import java.util.*;

import com.truelogic.common.*;

public class Main 
{
    private static class Source
    {
        String strID;
        Vector<String> strURLs = new Vector<String>();
        String strName;
        int iImageWidthMin;
        int iAspectRatioMax;
        int iArticleSizeMin;
//        int iArticleCountMin;
        int iArticleChunkSizeMin;
//        int iDepthMax;
    }

    private static String strConnect;
    private static String strUser;
    private static String strPassword;
    private static String strOutputDir;
    private static int iArticleCount;
    private static int iImageWidth;
    private static int iImageHeight;
    private static int iImageQuality;
    
    private static Vector<Source> oSourceList = new Vector<Source>();
    
    public static void loadParams() throws Exception
    {
        try
        {
            IniFile oIni = new IniFile("snarfer.ini");

            strOutputDir = oIni.StringGet("output", "dir");
            iArticleCount = oIni.IntGet("output", "article_count", 100);
            iImageWidth = oIni.IntGet("output", "image_width", 320);
            iImageHeight = oIni.IntGet("output", "image_height", 240);
            iImageQuality = oIni.IntGet("output", "image_quality", 80);

            strConnect = oIni.StringGet("db", "connect");
            strUser = oIni.StringGet("db", "user");
            strPassword = oIni.StringGet("db", "password", "");
            
            int iSourceCount = oIni.IntGet("source", "count", 0);

            for (int iSourceIdx = 0; iSourceIdx < iSourceCount; iSourceIdx++)
            {
                Source oSource = new Source();

                oSource.strID = oIni.StringGet("source", "source" + (iSourceIdx + 1));

                int iIndex = 1;
                String strURL = oIni.StringGet(oSource.strID, "url" + iIndex, null);
                
                while (strURL != null)
                {
                    oSource.strURLs.add(strURL);

                    iIndex++;
                    strURL = oIni.StringGet(oSource.strID, "url" + iIndex, null);
                }
                
                oSource.strName = oIni.StringGet(oSource.strID, "name");
                
                oSource.iImageWidthMin = oIni.IntGet(oSource.strID, "image_width_min", 
                                             oIni.IntGet("source_default", "image_width_min", 150));
                oSource.iAspectRatioMax = oIni.IntGet(oSource.strID, "aspect_ratio_max", 
                                              oIni.IntGet("source_default", "aspect_ratio_max", 2));
                oSource.iArticleSizeMin = oIni.IntGet(oSource.strID, "article_size_min", 
                                              oIni.IntGet("source_default", "article_size_min", 1000));
                oSource.iArticleChunkSizeMin = oIni.IntGet(oSource.strID, "article_chunk_size_min", 
                                                   oIni.IntGet("source_default", "article_chunk_size_min", 100));
//                oSource.iDepthMax = oIni.IntGet(oSource.strID, "depth_max", 
//                                        oIni.IntGet("source_default", "depth_max", 2));

                if (oSource.strID != null)
                    oSourceList.add(oSource);
            }
        }
        catch (Exception oException)
        {
            throw new Exception("Unable to read snarfer.ini");
        }
    }
    
    public static void main(String[] args) throws Exception
    {
        loadParams();
        java.sql.Date oDate = null;//new java.sql.Date(110, 6, 28);
        Snarfer oSnarfer = new Snarfer();
        
        if ((args.length == 0) || (!args[0].equalsIgnoreCase("dump")))
        {
            for (int iSourceIdx = 0; iSourceIdx < oSourceList.size(); iSourceIdx++)
            {
                Source oSource = oSourceList.get(iSourceIdx);

                oSnarfer.sourceAdd(oSource.strID, oSource.strName, oSource.strURLs, oSource.iImageWidthMin, oSource.iAspectRatioMax, oSource.iArticleSizeMin, oSource.iArticleChunkSizeMin);
            }

            oSnarfer.run();

            SnarferToDB oSnarferToDB = new SnarferToDB(oSnarfer, strConnect, strUser, strPassword);
            oDate = oSnarferToDB.run();
        }

        DBToFile oDBToFile = new DBToFile(oDate, strConnect, strUser, strPassword, strOutputDir, iArticleCount, iImageWidth, iImageHeight, iImageQuality);
        oDBToFile.run();
    }
}
package com.truelogic.snarfer;

import java.util.*;

import com.truelogic.common.*;

public class Main 
{
    private String strConnect;
    private String strUser;
    private String strPassword;
    private String strOutputDir;
    private int iArticleCount;
    private int iImageWidth;
    private int iImageHeight;
    private int iImageQuality;
    
    private Vector<SourceData> oSourceList = new Vector<SourceData>();
    
    public void loadParams() throws Exception
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
                String strID = oIni.StringGet("source", "source" + (iSourceIdx + 1));

                int iIndex = 1;
                String strURL = oIni.StringGet(strID, "url" + iIndex, null);
                
                Vector<String> strURLs = new Vector<String>();
                
                while (strURL != null)
                {
                    strURLs.add(strURL);

                    iIndex++;
                    strURL = oIni.StringGet(strID, "url" + iIndex, null);
                }
                
                String strName = oIni.StringGet(strID, "name");
                
                int iImageWidthMin = oIni.IntGet(strID, "image_width_min", 
                                             oIni.IntGet("source_default", "image_width_min", 150));
                int iAspectRatioMax = oIni.IntGet(strID, "aspect_ratio_max", 
                                              oIni.IntGet("source_default", "aspect_ratio_max", 2));
                int iArticleSizeMin = oIni.IntGet(strID, "article_size_min", 
                                              oIni.IntGet("source_default", "article_size_min", 1000));
                int iArticleChunkSizeMin = oIni.IntGet(strID, "article_chunk_size_min", 
                                                   oIni.IntGet("source_default", "article_chunk_size_min", 100));

                if (strID != null)
                    oSourceList.add(new SourceData(strID, strURLs, strName, iImageWidthMin, iAspectRatioMax,
                                    iArticleSizeMin, iArticleChunkSizeMin));
            }
        }
        catch (Exception oException)
        {
            throw new Exception("Unable to read snarfer.ini");
        }
    }
    
    public void run(String[] stryArgs) throws Exception
    {
        loadParams();
        java.sql.Date oDate = null;
        Snarfer oSnarfer = new Snarfer();
        
        if ((stryArgs.length == 0) || (!stryArgs[0].equalsIgnoreCase("dump")))
        {
            for (int iSourceIdx = 0; iSourceIdx < oSourceList.size(); iSourceIdx++)
            {
                SourceData oData = oSourceList.get(iSourceIdx);

                oSnarfer.sourceAdd(oData);
            }

            oSnarfer.run();

            SnarferToDB oSnarferToDB = new SnarferToDB(oSnarfer, strConnect, strUser, strPassword);
            oDate = oSnarferToDB.run();
        }

        DBToFile oDBToFile = new DBToFile(oDate, strConnect, strUser, strPassword, strOutputDir, iArticleCount, 
                                          iImageWidth, iImageHeight, iImageQuality);
        oDBToFile.run();
    }
    
    public static void main(String[] stryArgs) throws Exception
    {
        Main oMain = new Main();
        oMain.run(stryArgs);
    }
}
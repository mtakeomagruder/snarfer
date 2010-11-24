package com.truelogic.snarfer;

import java.io.*;
import java.util.*;

import org.apache.log4j.*;

import com.truelogic.common.*;

public class Config 
{
    static Logger oLogger = Logger.getLogger(Config.class);
    
    private String strDbConnect;    // A JDBC connect string for the DB
    private String strDbUser;       // The user name to log on as
    private String strDbPassword;   // The password for the user account (may be missing or blank)

    private String strOutputDir;  // The output directory for the flash images and text

    private int iArticleCount;      // The number of articles to output in the flash directory
    private int iImageWidth;        // The width of the output flash images
    private int iImageHeight;       // The height of the output flash images
    private int iImageQuality;      // The JPEG quality of the output flash images
    
    private Vector<SourceData> oSourceList = new Vector<SourceData>(); // The list of news sources and RSS feeds
    
    public Config(String strIniFile) throws IniException, IOException
    {
        loadParams(strIniFile);
    }

    /*******************************************************************************************************************
     * Loads the snarfer parameters.
     * @throws IOException 
     *******************************************************************************************************************/
     private void loadParams(String strIniFile) throws IniException, IOException
     {
         /***********************************************************************************************************
          * Load the INI file
          ***********************************************************************************************************/
          oLogger.info("Loading " + strIniFile);
          IniFile oIni = new IniFile(strIniFile);

          /***********************************************************************************************************
          * Load the flash output parameters 
          ***********************************************************************************************************/
          oLogger.info("Loading general properties");
          strOutputDir = oIni.StringGet("output", "dir");
          iArticleCount = oIni.IntGet("output", "article_count", 100);
          iImageWidth = oIni.IntGet("output", "image_width", 320);
          iImageHeight = oIni.IntGet("output", "image_height", 240);
          iImageQuality = oIni.IntGet("output", "image_quality", 80);

          /***********************************************************************************************************
          * Load the DB parameters 
          ***********************************************************************************************************/
          oLogger.info("Loading DB properties");
          strDbConnect = oIni.StringGet("db", "connect");
          strDbUser = oIni.StringGet("db", "user");
          strDbPassword = oIni.StringGet("db", "password", "");
          
          /***********************************************************************************************************
          * Get the source count 
          ***********************************************************************************************************/
          oLogger.info("Loading sources");
          int iSourceCount = oIni.IntGet("source", "count", 0);
          oLogger.info(iSourceCount + " source(s) found");

          /***********************************************************************************************************
          * Load each source 
          ***********************************************************************************************************/
          for (int iSourceIdx = 0; iSourceIdx < iSourceCount; iSourceIdx++)
          {
              /*******************************************************************************************************
              * Get the source ID 
              *******************************************************************************************************/
              String strID = oIni.StringGet("source", "source" + (iSourceIdx + 1));
              oLogger.info("Loading source " + (iSourceIdx + 1) + ": " + strID);

              /*******************************************************************************************************
               * Get the list of source RSS URLs  
               *******************************************************************************************************/
              Vector<String> strURLs = new Vector<String>();
              int iIndex = 1;
              
              String strURL = oIni.StringGet(strID, "url" + iIndex, null);
              
              while (strURL != null)
              {
                  strURLs.add(strURL);

                  iIndex++;
                  strURL = oIni.StringGet(strID, "url" + iIndex, null);
              }

              oLogger.info((iIndex - 1) + " URLs found for source " + (iSourceIdx + 1));
              
              /*******************************************************************************************************
              * Get the rest of the source parameters  
              *******************************************************************************************************/
              oLogger.info("Loading parameters for source " + (iSourceIdx + 1));

              String strName = oIni.StringGet(strID, "name");
              
              int iImageWidthMin = oIni.IntGet(strID, "image_width_min", 
                                           oIni.IntGet("source_default", "image_width_min", 150));
              int iAspectRatioMax = oIni.IntGet(strID, "aspect_ratio_max", 
                                            oIni.IntGet("source_default", "aspect_ratio_max", 2));
              int iArticleSizeMin = oIni.IntGet(strID, "article_size_min", 
                                            oIni.IntGet("source_default", "article_size_min", 1000));
              int iArticleChunkSizeMin = oIni.IntGet(strID, "article_chunk_size_min", 
                                                 oIni.IntGet("source_default", "article_chunk_size_min", 100));

              /*******************************************************************************************************
              * Save the source if not null  
              *******************************************************************************************************/
              if (strID != null)
                  oSourceList.add(new SourceData(strID, strURLs, strName, iImageWidthMin, iAspectRatioMax,
                                  iArticleSizeMin, iArticleChunkSizeMin));
          }
     }
     
     public String getDbConnect() 
     {
         return(strDbConnect);
     }

     public String getDbUser() 
     {
         return(strDbUser);
     }

     public String getDbPassword() 
     {
         return(strDbPassword);
     }

     public String getOutputDir() 
     {
         return(strOutputDir);
     }

     public int getArticleCount() 
     {
         return(iArticleCount);
     }

     public int getImageWidth() 
     {
         return(iImageWidth);
     }

     public int getImageHeight() 
     {
         return(iImageHeight);
     }

     public int getImageQuality() 
     {
         return(iImageQuality);
     }

     public Vector<SourceData> getSourceList() 
     {
         return(oSourceList);
     }
}
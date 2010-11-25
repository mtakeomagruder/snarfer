package com.truelogic.snarfer.config;

// Java imports
import java.io.*;
import java.util.*;

// Third party imports
import org.apache.log4j.*;

// Project imports
import com.truelogic.common.*;
import com.truelogic.snarfer.exception.*;

/***********************************************************************************************************************
* This class loads and parses the snarfer.ini file.
* 
* @author David Steele
***********************************************************************************************************************/
public class Config 
{
    static Logger oLogger = Logger.getLogger(Config.class);
    
    private String strOutputDir;            // The output directory for the flash images and text
    
    private ConfigDb oDb;                   // Database configuration

    private int iArticleCount;              // The number of articles to output in the flash directory
    private int iImageWidth;                // The width of the output flash images
    private int iImageHeight;               // The height of the output flash images
    private int iImageQuality;              // The JPEG quality of the output flash images
    
    private ConfigReplace oConfigReplace;   // The replacement rules for an article
    
    private Vector<ConfigSource> oConfigSources = new Vector<ConfigSource>(); // The list of news sources and RSS feeds
    
    /*******************************************************************************************************************
    * Initalizes the snarfer config.
    * 
    * @param strIniFile  The full path and name of the ini file
    *******************************************************************************************************************/
    public Config(String strIniFile) throws IniException, IOException, SnarferException
    {
        loadParams(strIniFile);
    }

    /*******************************************************************************************************************
    * Loads the snarfer parameters.
    * 
    * @param strIniFile  The full path and name of the ini file
    *******************************************************************************************************************/
    private void loadParams(String strIniFile) throws IniException, IOException, SnarferException
    {
        /***************************************************************************************************************
        * Load the INI file
        ***************************************************************************************************************/
        oLogger.info("Loading " + strIniFile);
        IniFile oIni = new IniFile(strIniFile);

        /***************************************************************************************************************
        * Load the flash output parameters 
        ***************************************************************************************************************/
        oLogger.info("Loading general properties");
        strOutputDir = oIni.StringGet("output", "dir");
        iArticleCount = oIni.IntGet("output", "article_count", 100);
        iImageWidth = oIni.IntGet("output", "image_width", 320);
        iImageHeight = oIni.IntGet("output", "image_height", 240);
        iImageQuality = oIni.IntGet("output", "image_quality", 80);

        /***************************************************************************************************************
        * Load the DB parameters 
        ***************************************************************************************************************/
        oLogger.info("Loading DB properties");
        
        oDb = new ConfigDb(oIni.StringGet("db", "driver"),
                           oIni.StringGet("db", "connect"), 
                           oIni.StringGet("db", "user"), 
                           oIni.StringGet("db", "password", ""));
          
        /***************************************************************************************************************
        * Get the source count 
        ***************************************************************************************************************/
        oLogger.info("Loading sources");
        int iSourceCount = oIni.IntGet("source", "count", 0);
        oLogger.info(iSourceCount + " source(s) found");

        /***************************************************************************************************************
        * Load each source 
        ***************************************************************************************************************/
        for (int iSourceIdx = 0; iSourceIdx < iSourceCount; iSourceIdx++)
        {
            /***********************************************************************************************************
            * Get the source ID 
            ***********************************************************************************************************/
            String strID = oIni.StringGet("source", "source" + (iSourceIdx + 1));
            oLogger.info("Loading source " + (iSourceIdx + 1) + ": " + strID);

            /***********************************************************************************************************
            * Get the list of source RSS URLs  
            ***********************************************************************************************************/
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
            
            /***********************************************************************************************************
            * Get the rest of the source parameters  
            ***********************************************************************************************************/
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
            int iBorderWidth = oIni.IntGet(strID, "border_width", 
                                           oIni.IntGet("source_default", "border_width", 1));

            /***********************************************************************************************************
            * Read the article replacement rules  
            ***********************************************************************************************************/
            iIndex = 1;
            String strRules = "";
            
            String strRule = oIni.StringGet("replace_default", "rule" + iIndex, null);
            
            while (strRule != null)
            {
                strRules += strRule;

                strRule = oIni.StringGet("replace_default", "rule" + iIndex, null);
                
                if (strRule != null)
                    strRules += "\n";
                
                iIndex += 1;
            }
            
            oConfigReplace = new ConfigReplace(strRules);
            
            /***********************************************************************************************************
            * Save the source if not null  
            ***********************************************************************************************************/
            if (strID != null)
                oConfigSources.add(new ConfigSource(strID, strURLs, strName, iImageWidthMin, iAspectRatioMax,
                                   iArticleSizeMin, iArticleChunkSizeMin, iBorderWidth, oConfigReplace));
        }
    }
     
    /******************************************************************************************************************
     * @return JDBC driver
     ******************************************************************************************************************/
     public ConfigDb getDb() 
     {
         return(oDb);
     }
    
     /******************************************************************************************************************
     * @return Directory where the flash output files are written
     ******************************************************************************************************************/
     public String getOutputDir() 
     {
         return(strOutputDir);
     }

     /******************************************************************************************************************
     * @return Maximum number of articles that will be written to the flash directory
     ******************************************************************************************************************/
     public int getArticleCount() 
     {
         return(iArticleCount);
     }

     /******************************************************************************************************************
     * @return Width of the images that will be written to the flash directory
     ******************************************************************************************************************/
     public int getImageWidth() 
     {
         return(iImageWidth);
     }

     /******************************************************************************************************************
     * @return Height of the images that will be written to the flash directory
     ******************************************************************************************************************/
     public int getImageHeight() 
     {
         return(iImageHeight);
     }

     /******************************************************************************************************************
     * @return JPEG quality (0-100) of the images that will be written to the flash directory
     ******************************************************************************************************************/
     public int getImageQuality() 
     {
         return(iImageQuality);
     }

     /******************************************************************************************************************
     * @return Default list of replacement rules for articles
     ******************************************************************************************************************/
      public ConfigReplace getArticleReplace() 
      {
          return(oConfigReplace);
      }

     /******************************************************************************************************************
     * @return List of news sources that will be scanned for valid articles
     ******************************************************************************************************************/
     public Vector<ConfigSource> getSources() 
     {
         return(oConfigSources);
     }
}
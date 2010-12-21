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
    
    private ConfigDb oDb;                   // Database configuration
    private ConfigOutput oOutput;           // Output directory and files parameters
    private ConfigReplace oArticleReplace;  // Replacement rules for an article
    private ConfigReplace oOutputReplace;   // Replacement rules for output
    
    private Vector<ConfigSource> oSources;  // List of news sources and RSS feeds
    
    /*******************************************************************************************************************
    * Initializes the snarfer configuration.
    * 
    * @param strIniFile  The full path and name of the INI file
    *******************************************************************************************************************/
    public Config(String strIniFile) throws IniException, IOException, SnarferException
    {
        oSources = new Vector<ConfigSource>();
        
        loadParams(strIniFile);
    }

    /*******************************************************************************************************************
    * Loads the snarfer parameters.
    * 
    * @param strIniFile  The full path and name of the INI file
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
        
        oOutput = new ConfigOutput(oIni.StringGet("output", "dir"), 
                                   oIni.IntGet("output", "article_count"),
                                   oIni.IntGet("output", "image_width"),
                                   oIni.IntGet("output", "image_height"),
                                   oIni.IntGet("output", "image_quality"),
                                   oOutputReplace = new ConfigReplace(oIni, "output_replace"));

        /***************************************************************************************************************
        * Load the DB parameters 
        ***************************************************************************************************************/
        oLogger.info("Loading DB properties");
        
        oDb = new ConfigDb(oIni.StringGet("db", "driver"),
                           oIni.StringGet("db", "connect"), 
                           oIni.StringGet("db", "user"), 
                           oIni.StringGet("db", "password", ""));
          
        /***************************************************************************************************************
        * Load each source 
        ***************************************************************************************************************/
        int iSourceIdx = 1;
        
        while (oIni.StringGet("source", "source" + iSourceIdx, null) != null)
        {
            /***********************************************************************************************************
            * Get the source ID 
            ***********************************************************************************************************/
            String strID = oIni.StringGet("source", "source" + iSourceIdx);
            oLogger.info("Loading source " + iSourceIdx + ": " + strID);

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

            oLogger.info((iIndex - 1) + " URLs found for source " + iSourceIdx);
            
            /***********************************************************************************************************
            * Get the rest of the source parameters  
            ***********************************************************************************************************/
            oLogger.info("Loading parameters for source " + iSourceIdx);

            String strName = oIni.StringGet(strID, "name");
            
            int iImageWidthMin = oIni.IntGet(strID, "image_width_min", 
                                             oIni.IntGet("source_default", "image_width_min"));
            int iAspectRatioMax = oIni.IntGet(strID, "aspect_ratio_max", 
                                              oIni.IntGet("source_default", "aspect_ratio_max"));
            int iArticleSizeMin = oIni.IntGet(strID, "article_size_min", 
                                              oIni.IntGet("source_default", "article_size_min"));
            int iArticleChunkSizeMin = oIni.IntGet(strID, "article_chunk_size_min", 
                                                   oIni.IntGet("source_default", "article_chunk_size_min"));
            int iBorderWidth = oIni.IntGet(strID, "border_width", 
                                           oIni.IntGet("source_default", "border_width"));

            /***********************************************************************************************************
            * Read the article replacement rules  
            ***********************************************************************************************************/
            oArticleReplace = new ConfigReplace(oIni, "replace_default");

            /***********************************************************************************************************
            * Save the source if not null  
            ***********************************************************************************************************/
            if (strID != null)
                oSources.add(new ConfigSource(strID, strURLs, strName, iImageWidthMin, iAspectRatioMax,
                                              iArticleSizeMin, iArticleChunkSizeMin, iBorderWidth, oArticleReplace));
            
            iSourceIdx++;
        }

        oLogger.info((iSourceIdx - 1) + " source(s) found");
    }
     
    /*******************************************************************************************************************
    * @return JDBC driver
    *******************************************************************************************************************/
    public ConfigDb getDb() 
    {
        return(oDb);
    }
    
    /*******************************************************************************************************************
    * @return Output directory and files parameters
    *******************************************************************************************************************/
     public ConfigOutput getOutput() 
     {
         return(oOutput);
     }
     
    /*******************************************************************************************************************
    * @return Default list of replacement rules for articles
    *******************************************************************************************************************/
    public ConfigReplace getArticleReplace() 
    {
        return(oArticleReplace);
    }

    /*******************************************************************************************************************
    * @return Default list of replacement rules for output
    *******************************************************************************************************************/
    public ConfigReplace getOutputReplace() 
    {
        return(oOutputReplace);
    }

    /*******************************************************************************************************************
    * @return List of news sources that will be scanned for valid articles
    *******************************************************************************************************************/
    public Vector<ConfigSource> getSources() 
    {
        return(oSources);
    }
}
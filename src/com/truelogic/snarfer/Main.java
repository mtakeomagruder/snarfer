package com.truelogic.snarfer;

import java.util.*;

import com.truelogic.common.*;

/***********************************************************************************************************************
* <p>This class is the entry point for the program and runs the snarfer.</p>
* 
* <p>A snarfer is a program that pulls data from some other source, generally not in the way that it was intended to be
* accessed.  In this case, the snarfer pulls articles and images from news sites such as CNN and BBC.  News sources
* generally do not provide any programmatic way to get this data, so we parse the html to get the correct image and
* the associated text for any given article.</p>
* 
* <p>The first revision of the program (2005) followed the link tree to find articles.  This was error prone and 
* eventually produced fewer articles as sites started to incorporate Javascript links.  In 2010 the indexing code was 
* changed to use RSS feeds.  In 2005 these had not produced sufficient data, but by 2010 they listed more articles than 
* we were getting from walking the html pages.</p>
* 
* <p>The current version is consists of these steps:</p>
* <p>1. A list of articles is created from the provided RSS feeds for each site.</p>
* <p>2. Each html page is loaded and the article text is scraped off.  The article image is identified by its 
* proportions and size.<p>
* <p>3. The data is saved into the DB from the internal structures in a single transaction.</p>
* <p>4. The data is then exported from the DB to disk files as needed.</p>
* 
* <p>The snarfer runs daily and if it runs twice it overwrites the data for that day.  Initialization parameters are
* stored in the SNARFER.INI file which follows a standard windows INI format.</p>
* 
* <p>The idea behind the snarfer is simple - create a large archive of images and articles that are time specific to be
* used in artworks that change each day to reflect current events.  Most news organizations do not provide a consistent
* view of what their sites looked like in the past.  The snarfer keeps a record so that we can recreate any day that
* the snarfer ran and show how the artworks would have looked on that day</p> 
***********************************************************************************************************************/
public class Main 
{
    private String strConnect;      // A JDBC connect string for the DB
    private String strUser;         // The user name to log on as
    private String strPassword;     // The password for the user account (may be missing or blank)
    private String strOutputDir;    // The output directory for the flash images and text
    private int iArticleCount;      // The number of articles to output in the flash directory
    private int iImageWidth;        // The width of the output flash images
    private int iImageHeight;       // The height of the output flash images
    private int iImageQuality;      // The JPEG quality of the output flash images
    
    private Vector<SourceData> oSourceList = new Vector<SourceData>(); // The list of news sources and RSS feeds
    
    /*******************************************************************************************************************
    * Loads the snarfer parameters.
    *******************************************************************************************************************/
    public void loadParams() throws Exception
    {
        try
        {
            /***********************************************************************************************************
            * Load the INI file
            ***********************************************************************************************************/
            IniFile oIni = new IniFile("snarfer.ini");

            /***********************************************************************************************************
            * Load the flash output parameters 
            ***********************************************************************************************************/
            strOutputDir = oIni.StringGet("output", "dir");
            iArticleCount = oIni.IntGet("output", "article_count", 100);
            iImageWidth = oIni.IntGet("output", "image_width", 320);
            iImageHeight = oIni.IntGet("output", "image_height", 240);
            iImageQuality = oIni.IntGet("output", "image_quality", 80);

            /***********************************************************************************************************
            * Load the DB parameters 
            ***********************************************************************************************************/
            strConnect = oIni.StringGet("db", "connect");
            strUser = oIni.StringGet("db", "user");
            strPassword = oIni.StringGet("db", "password", "");
            
            /***********************************************************************************************************
            * Get the source count 
            ***********************************************************************************************************/
            int iSourceCount = oIni.IntGet("source", "count", 0);

            /***********************************************************************************************************
            * Load each source 
            ***********************************************************************************************************/
            for (int iSourceIdx = 0; iSourceIdx < iSourceCount; iSourceIdx++)
            {
                /*******************************************************************************************************
                * Get the source ID 
                *******************************************************************************************************/
                String strID = oIni.StringGet("source", "source" + (iSourceIdx + 1));

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
                
                /*******************************************************************************************************
                * Get the rest of the source parameters  
                *******************************************************************************************************/
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
        catch (Exception oException)
        {
            throw new Exception("Unable to read snarfer.ini");
        }
    }
    
    /*******************************************************************************************************************
    * Loads the snarfer parameters.
    * 
    * @param strArgs  Arguments passed on the command line
    *******************************************************************************************************************/
    public void run(String[] stryArgs) throws Exception
    {
        /***************************************************************************************************************
        * Load parameters and initialize the snarfer
        ***************************************************************************************************************/
        loadParams();
        java.sql.Date oDate = new java.sql.Date(System.currentTimeMillis());
        Snarfer oSnarfer = new Snarfer();
        
        /***************************************************************************************************************
        * Skip the snarfer if only a DB dump is requested
        ***************************************************************************************************************/
        if ((stryArgs.length == 0) || (!stryArgs[0].equalsIgnoreCase("dump")))
        {
            /***********************************************************************************************************
            * Add all news sources to the snarfer
            ***********************************************************************************************************/
            for (int iSourceIdx = 0; iSourceIdx < oSourceList.size(); iSourceIdx++)
            {
                SourceData oData = oSourceList.get(iSourceIdx);

                oSnarfer.sourceAdd(oData);
            }

            /***********************************************************************************************************
            * Run the snarfer
            ***********************************************************************************************************/
            oSnarfer.run();

            /***********************************************************************************************************
            * Save the snarfer data to the DB 
            ***********************************************************************************************************/
            SnarferToDB oSnarferToDB = new SnarferToDB(oSnarfer, strConnect, strUser, strPassword);
            oDate = oSnarferToDB.run();
        }

        /***************************************************************************************************************
        * Save data from the DB into files on disk
        ***************************************************************************************************************/
        DBToFile oDBToFile = new DBToFile(oDate, strConnect, strUser, strPassword, strOutputDir, iArticleCount, 
                                          iImageWidth, iImageHeight, iImageQuality);
        oDBToFile.run();
    }
    
    /*******************************************************************************************************************
    * Entry point for the snarfer program.
    * 
    * @param strArgs  Arguments passed on the command line
    *******************************************************************************************************************/
    public static void main(String[] stryArgs) throws Exception
    {
        Main oMain = new Main();
        oMain.run(stryArgs);
    }
}
package com.truelogic.snarfer;

// Third party imports
import org.apache.log4j.*;

// Project imports
import com.truelogic.snarfer.config.*;

/***********************************************************************************************************************
* <p>Entry point for the snarfer program.</p>
* 
* <p>A snarfer is a program that pulls data from some other source, generally not in the way that it was intended to be
* accessed.  In this case, the snarfer pulls articles and images from news sites such as CNN and BBC.  News sources
* generally do not provide any programmatic way to get this data, so we parse the HTML to get the correct image and
* the associated text for any given article.</p>
* 
* <p>The first revision of the program (2005) followed the link tree to find articles.  This was error prone and 
* eventually produced fewer articles as sites started to incorporate Javascript links.  In 2010 the indexing code was 
* changed to use RSS feeds.  In 2005 these had not produced sufficient data, but by 2010 they listed more articles than 
* we were getting from walking the HTML pages.</p>
* 
* <p>The current version is consists of these steps:</p>
* <p>1. A list of articles is created from the provided RSS feeds for each site.</p>
* <p>2. Each HTML page is loaded and the article text is scraped off.  The article image is identified by its 
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
* 
* @author David Steele
* @version 2.5
***********************************************************************************************************************/
public class Main 
{
    static Logger oLogger = Logger.getLogger(Main.class);
    
    Config oConfig;
    
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
        oConfig = new Config("snarfer.ini");
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
            oLogger.info("Inserting sources into the snarfer");
            
            for (ConfigSource oData : oConfig.getSources())
                oSnarfer.sourceAdd(oData);
            
            /***********************************************************************************************************
            * Run the snarfer
            ***********************************************************************************************************/
            oLogger.info("Running the snarfer");
            oSnarfer.run();

            /***********************************************************************************************************
            * Save the snarfer data to the DB 
            ***********************************************************************************************************/
            oLogger.info("Saving snarfer data to the DB");
            SnarferToDb oSnarferToDb = new SnarferToDb(oSnarfer, oConfig.getDb());
//            oDate = oSnarferToDb.run();
        }

        /***************************************************************************************************************
        * Save data from the DB into files on disk
        ***************************************************************************************************************/
        oLogger.info("Saving snarfer data to files (flash)");
        DbToFileFlash oDbToFile = new DbToFileFlash(oDate, oConfig.getDb(), oConfig.getOutput());
//        oDbToFile.run();
    }
    
    /*******************************************************************************************************************
    * Entry point for the snarfer program.
    * 
    * @param strArgs  Arguments passed on the command line
    *******************************************************************************************************************/
    public static void main(String[] stryArgs) throws Exception
    {
        oLogger.info("------------------------------------------------------------");
        oLogger.info("Snarfer started");
        
        Main oMain = new Main();
        oMain.run(stryArgs);
    }
}
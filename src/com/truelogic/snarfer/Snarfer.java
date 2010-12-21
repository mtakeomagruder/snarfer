package com.truelogic.snarfer;

// Java imports
import java.util.*;

// Project imports
import com.truelogic.snarfer.config.*;

/***********************************************************************************************************************
* This class loads articles from all the new sources.
* 
* @author David Steele
***********************************************************************************************************************/
public class Snarfer extends Vector<Source> 
{
    private static final long serialVersionUID = 1L;

    /*******************************************************************************************************************
    * Adds a news source to snarfer.
    * 
    * @param oConfig  Configuration for the news source
    *******************************************************************************************************************/
    public void add(ConfigSource oConfig) throws Exception
    {
        Source oSource = new Source(oConfig);
        add(oSource);
    }
    
    /*******************************************************************************************************************
    * Iterate through all the news sources and load the articles. 
    *******************************************************************************************************************/
    public void run()
    {
        for (Source oSource : this)
            oSource.run();
    }
}
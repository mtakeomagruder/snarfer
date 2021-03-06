package com.truelogic.snarfer.config;

// Java imports
import java.io.*;
import java.util.*;

// Third party imports
import au.com.bytecode.opencsv.*;

// Project imports
import com.truelogic.common.*;
import com.truelogic.snarfer.exception.*;

/***********************************************************************************************************************
* This class loads a set of replacement rules from a CSV string.
* 
* <p>The strRules parameter passed to the constructor should have the following format:</p>
* 
* <p>"replace1","replacement1"<br/>
* "quote",""""</br>
* "\\n","linefeed"</p>
* 
* <p>Per standard CSV format, all quotes must be doubled.  All escaped characters must have a double forward 
* slash or they will be treated as normal characters.  See examples above.  Standard Java regular expressions 
* can be used if proper double escaping rules are followed.</p>
* 
* @author David Steele
***********************************************************************************************************************/
public class ConfigReplace extends HashMap<String, String> 
{
    private static final long serialVersionUID = 1L;
    
    Vector<String> oKeys;                               // Replacement keys in order;

    /*******************************************************************************************************************
     * Initializes ConfigReplace.
     * 
     * @param oIni        Windows standard INI file to load replacement rules from
     * @param strSection  Section where the replacement rules are stored
     *******************************************************************************************************************/
     public ConfigReplace(IniFile oIni, String strSection) throws IOException, SnarferException
     {
         int iIndex = 1;
         String strRules = "";
         
         String strRule = oIni.StringGet(strSection, "rule" + iIndex, null);
         
         while (strRule != null)
         {
             strRules += strRule;

             strRule = oIni.StringGet(strSection, "rule" + iIndex, null);
             
             if (strRule != null)
                 strRules += "\n";
             
             iIndex += 1;
         }

         parse(strRules);
     }
    
    /*******************************************************************************************************************
    * Initializes ConfigReplace.
    * 
    * @param strRules  The CSV string that defines the rules
    *******************************************************************************************************************/
    public ConfigReplace(String strRules) throws IOException, SnarferException
    {
        parse(strRules);
    }
    
    /*******************************************************************************************************************
    * Parses the rules.
    * 
    * @param strRules  The CSV string that defines the rules
    *******************************************************************************************************************/
    private void parse(String strRules) throws IOException, SnarferException
    {
        oKeys = new Vector<String>();
        CSVReader oReader = new CSVReader(new StringReader(strRules));
        
        List<String[]> oRules = oReader.readAll();
        
        for (String[] stryRule : oRules)
        {
            if (stryRule.length != 2)
                throw new SnarferException ("Article replace rule does not have two parts");
    
            oKeys.add(stryRule[0]);
            put(stryRule[0], stryRule[1]);
        }
    }
    
    /*******************************************************************************************************************
    * <p>Returns the hash keys in an ordered fashion (the order from the CSV file).</p>
    * 
    * <p>This object is extended from HashMap so keySet() can also be used to extract the keys.  However, keySet() is 
    * not ordered and for replacement rules order may be important.</p>   
    *******************************************************************************************************************/ 
    public Vector<String> getOrderedKeys()
    {
        return(oKeys);
    }
}
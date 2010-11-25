package com.truelogic.common;

// Java imports
import java.io.*;
import java.util.*;

/***********************************************************************************************************************
* This class reads Windows-style INI files.
*
* [SECTION1]
* key1=value1
* key2=value2
*
* [SECTION2]
* key1=value1
* key2=value2
* 
* @author David Steele
***********************************************************************************************************************/
public class IniFile
{
    /*******************************************************************************************************************
    * Local Variables
    *******************************************************************************************************************/
    private HashMap<String, HashMap<String, String>> oSections = new HashMap<String, HashMap<String, String>>();

    /*******************************************************************************************************************
    * Loads an INI file into memory
    * 
    * @param strFileName The INI file to load
    *******************************************************************************************************************/
    public IniFile(String strFileName) throws IOException
    {
        String strLine;                         // Temp storage for file lines
        HashMap<String, String> oKeys = null;   // Key storage for the current section

        /***************************************************************************************************************
        * Open the INI file
        ***************************************************************************************************************/
        BufferedReader oFile = new BufferedReader(new FileReader(strFileName));

        try
        {
            /***********************************************************************************************************
            * Read each line in the INI file
            ***********************************************************************************************************/
            while ((strLine = oFile.readLine()) != null)
            {
                strLine = strLine.trim();

                /*******************************************************************************************************
                * Lines less than two characters are garbage, so ignore them
                *******************************************************************************************************/
                if (strLine.length() > 1)
                {
                    /***************************************************************************************************
                    * If this is a section header, create a new section
                    ***************************************************************************************************/
                    if ((strLine.charAt(0) == '[') && (strLine.charAt(strLine.length() - 1) == ']'))
                    {
                        String strSection = strLine.substring(1, strLine.length() - 1).toUpperCase();
                        
                        oKeys = oSections.get(strSection);
                        
                        if (oKeys == null)
                            oKeys = new HashMap<String, String>();
                        
                        oSections.put(strSection, oKeys);
                    }
                    /***************************************************************************************************
                    * Else if this is a key/value pair, create a new key/value
                    ***************************************************************************************************/
                    else if ((oKeys != null) && (strLine.indexOf("=") != -1))
                    {
                        int iEqualIdx = strLine.indexOf("=");
                        oKeys.put(strLine.substring(0, iEqualIdx).toUpperCase(), 
                                  strLine.substring(iEqualIdx + 1, strLine.length()));
                    }
                }
            }
        }
        finally
        {
            oFile.close();
        }
    }

    /*******************************************************************************************************************
    * Retrieves a string value from the INI file.
    * 
    * @param strSection  The section to retrieve from
    * @param strKey      The key to retrieve from
    * @param strDefault  A default value in case the section/key does not exist (null is OK)
    * 
    * @return The retrieved or default value
    *******************************************************************************************************************/
    public String StringGet(String strSection, String strKey, String strDefault)
    {
        /***************************************************************************************************************
        * Search for the section and value.  If not found, return the default.
        ***************************************************************************************************************/
        HashMap<String, String> oKeys = oSections.get(strSection.toUpperCase());
        
        if (oKeys == null || oKeys.get(strKey.toUpperCase()) == null)
            return(strDefault);

        /***************************************************************************************************************
        * Return the found value.
        ***************************************************************************************************************/
        return(oKeys.get(strKey.toUpperCase()));
    }

    /*******************************************************************************************************************
    * Retrieves a string value from the INI file.
    * 
    * @param strSection  The section to retrieve from
    * @param strKey      The key to retrieve from
    * 
    * @return The retrieved value
    *******************************************************************************************************************/
    public String StringGet(String strSection, String strKey) throws IniException
    {
        String strValue = StringGet(strSection, strKey, null); 
        
        if (strValue == null)
            throw new IniException("No value found for section " + strSection + ", key " + strKey);
        
        return(strValue);
    }

    /*******************************************************************************************************************
     * Retrieves an integer value from the INI file.
     * 
     * @param strSection  The section to retrieve from
     * @param strKey      The key to retrieve from
     * @param iDefault    A default value in case the section/key does not exist (null is OK)
     * 
     * @return The retrieved or default value
     *******************************************************************************************************************/
    public Integer IntGet(String strSection, String strKey, Integer iDefault)
    {
        try
        {
            String strValue = StringGet(strSection, strKey, iDefault == null ? null : iDefault.toString());
            
            if (strValue == null)
                return(null);
            
            return(new Integer(strValue));
        }
        catch (NumberFormatException oException)
        {
            return(iDefault);
        }
    }

    /*******************************************************************************************************************
     * Retrieves an integer value from the INI file.
     * 
     * @param strSection  The section to retrieve from
     * @param strKey      The key to retrieve from
     * 
     * @return The retrieved or default value
     *******************************************************************************************************************/
    public Integer IntGet(String strSection, String strKey) throws IniException
    {
        return(new Integer(StringGet(strSection, strKey)));
    }
}
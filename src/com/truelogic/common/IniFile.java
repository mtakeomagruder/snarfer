/*******************************************************************************
*                               General Utility Code
*
*                                   IniFile.java
********************************************************************************
* $Archive: /TrueLogic/QkWebPTS/2.00/Classes/com/truelogic/common/IniFile.java $
* $Author: Dsteele $
* $Modtime: 11/10/01 6:37p $
* $Revision: 3 $
*
* This module creates and maintains ini files.  These files are structured
* exactly like windows ini files:
*
* [SECTION1]
* key1=value1
* key2=value2
*
* [SECTION2]
* key1=value1
* key2=value2
********************************************************************************
* $Log: /TrueLogic/QkWebPTS/2.00/Classes/com/truelogic/common/IniFile.java $
* 
* 3     12/29/01 3:01a Dsteele
* Fixed exception handling
*******************************************************************************/
package com.truelogic.common;

/*******************************************************************************
* Imported java classes
*******************************************************************************/
import java.io.*;
import java.util.*;

/*******************************************************************************
* IniFile class implementation - This class loads and provides access to windows
* style ini files. 
*******************************************************************************/
public class IniFile
    {
    /***************************************************************************
    * IniSection class implementation - This class stores an ini file section
    ***************************************************************************/
    private class IniSection
        {
        public String strName;
        public Vector oValues; 
        }

    /***************************************************************************
    * IniValue class implementation - This class stores an ini file key-value
    ***************************************************************************/
    private class IniValue
        {
        public String strKey;
        public String strValue;
        }

    private BufferedReader oFile;               // Ini file 
    private Vector oSections = new Vector();    // Ini file section list

    /***************************************************************************
    *                               IniFile
    ****************************************************************************
    * Parameters:
    *    strFileName - The filename of the inifile to load
    ****************************************************************************
    * This contructor loads an ini file into memory.
    ***************************************************************************/
    public IniFile(String strFileName) throws IOException
        {
        String strLine;                 // Temp storage for file lines
        IniSection oSection = null;     // Temp storage for sections

        /***********************************************************************
        * Open the ini file
        ***********************************************************************/
        oFile = new BufferedReader(new FileReader(strFileName));

        try
            {
            /*******************************************************************
            * Read each line in the ini file
            *******************************************************************/
            while ((strLine = oFile.readLine()) != null)
                {
                strLine = strLine.trim();

                /***************************************************************
                * Lines less than two characters are garbage, so ignore them
                ***************************************************************/
                if (strLine.length() > 1)
                    {
                    /***********************************************************
                    * If this is a section header, create a new section
                    ***********************************************************/
                    if ((strLine.charAt(0) == '[') &&
                        (strLine.charAt(strLine.length() - 1) == ']'))
                        {
                        oSection = new IniSection();

                        oSection.strName = strLine.substring(1,
                                           strLine.length() - 1);
                        oSection.oValues = new Vector(); 
                        oSections.add(oSection);
                        }
                    /***********************************************************
                    * Else if this is a key/value pair, create a new key/value
                    ***********************************************************/
                    else if ((oSection != null) && (strLine.indexOf("=") != -1))
                        {
                        IniValue oValue = new IniValue();
                        int iEqualIdx = strLine.indexOf("=");

                        oValue.strKey = strLine.substring(0, iEqualIdx);
                        oValue.strValue = strLine.substring(iEqualIdx + 1,
                                                            strLine.length());

                        oSection.oValues.add(oValue);
                        }
                    }
                }
            }
        finally
            {
            oFile.close();
            }
        }

    /***************************************************************************
    *                         StringGet (plus overloads)
    ****************************************************************************
    * Parameters:
    *    strSection - The section of the ini file where the key is stored
    *    strKey - The key used to retrieve the value
    *    strDefault - A default value in case the value is not found
    *
    * Returns:
    *    String - the retrieved or default value
    ****************************************************************************
    * This method attempts to retrieve a string value from the ini file.
    ***************************************************************************/
    public String StringGet(String strSection, String strKey, String strDefault)
        {
        IniSection oSection;            // Temp storage for sections
        IniValue oValue;                // Temp storage for values
        String strValue;                // The value to be returned
        int iSectionIdx, iValueIdx;     // Section and value index variables

        /***********************************************************************
        * First, set the value to the default value in case the real value is
        * never found.
        ***********************************************************************/
        strValue = strDefault;

        /***********************************************************************
        * Next, search each section and value for the key.  If the key is found,
        * overwrite the default value with the value found.
        ***********************************************************************/
        for (iSectionIdx = 0; iSectionIdx < oSections.size(); iSectionIdx++)
            {
            oSection = (IniSection)oSections.get(iSectionIdx);

            if (oSection.strName.equalsIgnoreCase(strSection))
                for (iValueIdx = 0; iValueIdx < oSection.oValues.size(); iValueIdx++)
                    {
                    oValue = (IniValue)oSection.oValues.get(iValueIdx);

                    if (oValue.strKey.equalsIgnoreCase(strKey))
                        strValue = oValue.strValue;
                    }
            }

        /***********************************************************************
        * Return the value, default or retrieved, to the user
        ***********************************************************************/
        return(strValue);
        }

    public String StringGet(String strSection, String strKey)
        {
        return(StringGet(strSection, strKey, ""));
        }

    /***************************************************************************
    *                             IntGet (plus overloads)
    ****************************************************************************
    * Parameters:
    *    strSection - The section of the ini file where the key is stored
    *    strKey - The key used to retrieve the value
    *    strDefault - A default value in case the value is not found
    *
    * Returns:
    *    int - the retrieved or default value
    ****************************************************************************
    * This method attempts to retrieve an integer value from the ini file.
    ***************************************************************************/
    public int IntGet(String strSection, String strKey, int iDefault)
        {
        try
            {
            return(Integer.valueOf(StringGet(strSection, strKey,
                                   String.valueOf(iDefault))).intValue());
            }
        catch (NumberFormatException oException)
            {
            return(iDefault);
            }
        }

    public int IntGet(String strSection, String strKey)
        {
        return(IntGet(strSection, strKey, 0));
        }
    }

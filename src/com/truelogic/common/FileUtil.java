package com.truelogic.common;

// Java imports
import java.io.*;

/***********************************************************************************************************************
* This class contains various file utility functions.
* 
* @author David Steele
***********************************************************************************************************************/
public class FileUtil 
{
    /*******************************************************************************************************************
    * Removes a file or directory all sub files and directories. 
    * 
    * @param oDirectory  The file or directory to remove
    *******************************************************************************************************************/
    public static void removeDir(File oDirectory)
    {
        if (oDirectory.isDirectory())      
        {
            String[] stryFiles = oDirectory.list ();          

            for (int iIndex = 0; iIndex < stryFiles.length; iIndex++)
            {                        
                File oSubDirectory = new File (oDirectory, stryFiles[iIndex]);
                removeDir(oSubDirectory);            
            }
        }

        oDirectory.delete();                           
    }    
}
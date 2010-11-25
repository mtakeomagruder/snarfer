package com.truelogic.common;

import java.io.File;

public class FileUtil 
{
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
        return;
    }    
}

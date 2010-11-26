package com.truelogic.snarfer.config;

/***********************************************************************************************************************
* Contains information about the output directory and files.
* 
* @author David Steele
***********************************************************************************************************************/
public class ConfigOutput 
{
    private String strOutputDir;    // The output directory to write the files
    private int iLimit;             // Maximum number of output files 
    private int iImageWidth;        // Output image width
    private int iImageHeight;       // Output image height
    private int iImageQuality;      // Output image JPEG quality

    /*******************************************************************************************************************
    * Initializes ConfigOutput.
    * 
    * @param strOutputDir   Output directory to write the files
    * @param iLimit         Maximum number of output files
    * @param iImageWidth    Output image width
    * @param iImageHeight   Output image height
    * @param iImageQuality  Output image JPEG quality
    *******************************************************************************************************************/
    public ConfigOutput(String strOutputDir, int iLimit, int iImageWidth, int iImageHeight, int iImageQuality)
    {
        this.strOutputDir = strOutputDir;
        this.iLimit = iLimit;
        this.iImageWidth = iImageWidth;
        this.iImageHeight = iImageHeight;
        this.iImageQuality = iImageQuality;
    }
    
    /*******************************************************************************************************************
    * @return Output directory to write the files
    *******************************************************************************************************************/
    public String getOutputDir() 
    {
        return(strOutputDir);
    }

    /*******************************************************************************************************************
    * @return Maximum number of output files
    *******************************************************************************************************************/
    public int getLimit() 
    {
        return(iLimit);
    }

    /*******************************************************************************************************************
    * @return Output image width
    *******************************************************************************************************************/
    public int getImageWidth() 
    {
        return(iImageWidth);
    }

    /*******************************************************************************************************************
    * @return Output image height
    *******************************************************************************************************************/
    public int getImageHeight() 
    {
        return(iImageHeight);
    }

    /*******************************************************************************************************************
    * @return Output image JPEG quality
    *******************************************************************************************************************/
    public int getImageQuality() 
    {
        return(iImageQuality);
    }
}
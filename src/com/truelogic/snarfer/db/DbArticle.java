package com.truelogic.snarfer.db;

/***********************************************************************************************************************
* Represents an article loaded from the database (denormalized).
* 
* @author David Steele
***********************************************************************************************************************/
public class DbArticle 
{
    private int iID;                // Source DB ID
    private int iSourceUrlID;       // Source URL DB ID
    private String strRandomID;     // Source random DB text ID
    private int iTier;              // Article Tier (1 for primary, 2 for all else)
    private String strTextHash;     // MD5 hash of article text
    private String strTextUrl;      // Article URL
    private String strText;         // Article text
    private int iImageID;           // Image DB ID
    private String strImageHash;    // MD5 hash of image
    private String strImageUrl;     // Image URL
    private byte[] tyImage;         // JPEG image stored in a byte array
    private int iBatchID;           // Batch DB ID

    /*******************************************************************************************************************
    * Initializes the DbArticle.
    * 
    * @param iID           Source DB ID          
    * @param iSourceUrlID  Source URL DB ID
    * @param strRandomID   Source random DB text ID
    * @param iTier         Article Tier (1 for primary, 2 for all else)
    * @param strTextHash   MD5 hash of article text
    * @param strTextURL    Article URL
    * @param strText       Article text
    * @param iImageID      Image DB ID
    * @param strImageHash  MD5 hash of image
    * @param strImageUrl   Image URL
    * @param tyImage       JPEG image stored in a byte array
    * @param iBatchID      Batch DB ID
    *******************************************************************************************************************/
    public DbArticle(int iID, int iSourceUrlID, String strRandomID, int iTier, String strTextHash, String strTextUrl, 
                     String strText, int iImageID, String strImageHash, String strImageUrl, byte[] tyImage, 
                     int iBatchID)
    {
        this.iID = iID;
        this.iSourceUrlID = iSourceUrlID;
        this.strRandomID = strRandomID;
        this.iTier = iTier;
        this.strTextHash = strTextHash;
        this.strTextUrl = strTextUrl;
        this.strText = strText;
        this.iImageID = iImageID;
        this.strImageHash = strImageHash;
        this.strImageUrl = strImageUrl;
        this.tyImage = tyImage;
        this.iBatchID = iBatchID;
    }
    
    /*******************************************************************************************************************
    * @return Source DB ID
    *******************************************************************************************************************/
    public int getID() 
    {
        return(iID);
    }

    /*******************************************************************************************************************
    * @return Source URL DB ID
    *******************************************************************************************************************/
    public int getSourceUrlID() 
    {
        return(iSourceUrlID);
    }

    /*******************************************************************************************************************
    * @return Source random DB text ID
    *******************************************************************************************************************/
    public String getRandomID() 
    {
        return(strRandomID);
    }

    /*******************************************************************************************************************
    * @return Article Tier (1 for primary, 2 for all else)
    *******************************************************************************************************************/
    public int getTier() 
    {
        return(iTier);
    }

    /*******************************************************************************************************************
    * @return Article size in bytes
    *******************************************************************************************************************/
    public int getTextSize() 
    {
        return(strText.length());
    }

    /*******************************************************************************************************************
    * @return MD5 hash of article text
    *******************************************************************************************************************/
    public String getTextHash() 
    {
        return(strTextHash);
    }

    /*******************************************************************************************************************
    * @return Article URL
    *******************************************************************************************************************/
    public String getTextUrl() 
    {
        return(strTextUrl);
    }

    /*******************************************************************************************************************
    * @return Article text
    *******************************************************************************************************************/
    public String getText() 
    {
        return(strText);
    }

    /*******************************************************************************************************************
    * @return Image DB ID
    *******************************************************************************************************************/
    public int getImageID() 
    {
        return(iImageID);
    }

    /*******************************************************************************************************************
    * @return Size of image in bytes
    *******************************************************************************************************************/
    public int getImageSize() 
    {
        return(tyImage.length);
    }

    /*******************************************************************************************************************
    * @return MD5 hash of image
    *******************************************************************************************************************/
    public String getImageHash() 
    {
        return(strImageHash);
    }

    /*******************************************************************************************************************
    * @return Image URL
    *******************************************************************************************************************/
    public String getImageUrl() 
    {
        return(strImageUrl);
    }

    /*******************************************************************************************************************
    * @return JPEG image stored in a byte array
    *******************************************************************************************************************/
    public byte[] getImage() 
    {
        return(tyImage);
    }

    /*******************************************************************************************************************
    * @return Batch DB ID 
    *******************************************************************************************************************/
    public int getBatchID() 
    {
        return(iBatchID);
    }
}
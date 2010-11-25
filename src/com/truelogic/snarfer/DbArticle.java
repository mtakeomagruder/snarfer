package com.truelogic.snarfer;

public class DbArticle 
{
    private int iID;
    private int iSourceUrlID;
    private String strRandomID;
    private int iTier;
    private int iTextSize;
    private String strTextHash;
    private String strTextUrl;
    private String strText;
    private int iImageID;
    private int iImageSize;
    private String strImageHash;
    private String strImageUrl;
    private byte[] tyImage;
    private int iBatchID;
    
    public DbArticle(int iID, int iSourceUrlID, String strRandomID, int iTier, int iTextSize, String strTextHash, 
                     String strTextUrl, String strText, int iImageID, int iImageSize, String strImageHash,
                     String strImageUrl, byte[] tyImage, int iBatchID)
    {
        this.iID = iID;
        this.iSourceUrlID = iSourceUrlID;
        this.strRandomID = strRandomID;
        this.iTier = iTier;
        this.iTextSize = iTextSize;
        this.strTextHash = strTextHash;
        this.strTextUrl = strTextUrl;
        this.strText = strText;
        this.iImageID = iImageID;
        this.iImageSize = iImageSize;
        this.strImageHash = strImageHash;
        this.strImageUrl = strImageUrl;
        this.tyImage = tyImage;
        this.iBatchID = iBatchID;
    }
    
    public int getID() 
    {
        return(iID);
    }

    public int getSourceUrlID() 
    {
        return(iSourceUrlID);
    }

    public String getRandomID() 
    {
        return(strRandomID);
    }

    public int getTier() 
    {
        return(iTier);
    }

    public int getTextSize() 
    {
        return(iTextSize);
    }

    public String getTextHash() 
    {
        return(strTextHash);
    }

    public String getTextUrl() 
    {
        return(strTextUrl);
    }

    public String getText() 
    {
        return(strText);
    }

    public int getImageID() 
    {
        return(iImageID);
    }

    public int getImageSize() 
    {
        return(iImageSize);
    }

    public String getImageHash() 
    {
        return(strImageHash);
    }

    public String getImageUrl() 
    {
        return(strImageUrl);
    }

    public byte[] getImage() 
    {
        return(tyImage);
    }

    public int getBatchID() 
    {
        return(iBatchID);
    }
}
package com.truelogic.snarfer;

import java.util.*;

public class Snarfer 
{
    Vector<Source> oSourceList = new Vector<Source>();
    
    public void sourceAdd(String strID, String strName, String strURL, int iDepthMax, int iImageWidthMin, int iAspectRatioMax, int iArticleSizeMin, int iArticleChunkSizeMin) throws Exception
    {
        Source oSource = new Source(strID, strName, strURL, iDepthMax, iImageWidthMin, iAspectRatioMax, iArticleSizeMin, iArticleChunkSizeMin);
        oSourceList.add(oSource);
    }
    
    public int sourceSize()
    {
        return(oSourceList.size());
    }
    
    public Source sourceGet(int iIndex)
    {
        return(oSourceList.get(iIndex));
    }
    
    public void run()
    {
        for (int iIndex = 0; iIndex < sourceSize(); iIndex++)
        {
            sourceGet(iIndex).run();
        }
    }
}

package com.truelogic.snarfer;

import java.util.*;

public class Snarfer 
{
    Vector<Source> oSourceList = new Vector<Source>();
    
    public void sourceAdd(SourceData oData) throws Exception
    {
        Source oSource = new Source(oData);
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
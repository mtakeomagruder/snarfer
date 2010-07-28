package com.truelogic.snarfer;

import java.util.*;

public class SourceList 
    {
    Vector<Source> oList = new Vector<Source>();
    
    public void add(Source oSource)
    {
        oList.add(oSource);
    }
    
    public int size()
    {
        return(oList.size());
    }
    
    public Source get(int iIndex)
    {
        return(oList.get(iIndex));
    }
}

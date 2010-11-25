package com.truelogic.snarfer;

// Java imports
import java.util.*;

import com.truelogic.snarfer.config.ConfigSource;

public class Snarfer 
{
    Vector<Source> oSourceList = new Vector<Source>();
    
    public void sourceAdd(ConfigSource oData) throws Exception
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
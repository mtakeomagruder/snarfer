package com.truelogic.snarfer;

// Java imports
import java.util.*;

// Project imports
import com.truelogic.snarfer.config.*;

public class Snarfer 
{
    Vector<Source> oSourceList = new Vector<Source>();
    
    public void sourceAdd(ConfigSource oConfig) throws Exception
    {
        Source oSource = new Source(oConfig);
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
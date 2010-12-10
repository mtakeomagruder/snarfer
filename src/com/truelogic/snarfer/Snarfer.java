package com.truelogic.snarfer;

// Java imports
import java.util.*;

// Project imports
import com.truelogic.snarfer.config.*;

public class Snarfer extends Vector<Source> 
{
    private static final long serialVersionUID = 1L;

    public void add(ConfigSource oConfig) throws Exception
    {
        Source oSource = new Source(oConfig);
        add(oSource);
    }
    
    public void run()
    {
        for (int iIndex = 0; iIndex < size(); iIndex++)
        {
            get(iIndex).run();
        }
    }
}
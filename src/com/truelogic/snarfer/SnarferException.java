package com.truelogic.snarfer;

/***********************************************************************************************************************
* Exception handler for the Snarfer project.
* 
* @author David Steele
***********************************************************************************************************************/
public class SnarferException extends Exception 
{
    private static final long serialVersionUID = 1L;

    /*******************************************************************************************************************
    * Throws an INI file exception
    * 
    * @param strDescription  Description of the INI file exception
    *******************************************************************************************************************/
    public SnarferException(String strDescription)
    {
        super(strDescription);
    }
}

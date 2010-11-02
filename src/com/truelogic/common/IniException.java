package com.truelogic.common;

/***********************************************************************************************************************
* Exception handler for the IniFile class.
***********************************************************************************************************************/
public class IniException extends Exception 
{
    private static final long serialVersionUID = 1L;

    /*******************************************************************************************************************
    * Throws an INI file exception
    * 
    * @param strDescription  Description of the INI file exception
    *******************************************************************************************************************/
    public IniException(String strDescription)
    {
        super(strDescription);
    }
}

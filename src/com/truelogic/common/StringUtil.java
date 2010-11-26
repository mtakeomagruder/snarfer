package com.truelogic.common;

/***********************************************************************************************************************
* This class contains various string utility functions.
* 
* @author David Steele
***********************************************************************************************************************/
public class StringUtil 
{
    /*******************************************************************************************************************
    * Left pads an integer to the specified length with the specified padding character.
    * 
    * @param iInt     Integer to be padded
    * @param iLength  Length of the final string
    * @param cPad     Character to use for padding
    *******************************************************************************************************************/
    public static String leftPad(int iInt, int iLength, char cPad)
    {
        String strInt = "" + iInt;

        while (strInt.length() < iLength)
            strInt = cPad + strInt;

        return(strInt);
    }
}

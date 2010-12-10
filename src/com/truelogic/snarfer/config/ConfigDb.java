package com.truelogic.snarfer.config;

/***********************************************************************************************************************
* Contains information about the database.
* 
* @author David Steele
***********************************************************************************************************************/
public class ConfigDb 
{
    private String strDriver;           // JDBC driver
    private String strConnect;          // JDBC connect string
    private String strUser;             // DB user name
    private String strPassword;         // User password (may be missing or blank)
    
    /*******************************************************************************************************************
    * Initializes ConfigDb.
    * 
    * @param strDriver    JDBC driver
    * @param strConnect   JDBC connect string
    * @param strUser      DB user name
    * @param strPassword  User password (may be missing or blank)
    *******************************************************************************************************************/
    public ConfigDb(String strDriver, String strConnect, String strUser, String strPassword)
    {
        this.strDriver = strDriver;
        this.strConnect = strConnect;
        this.strUser = strUser;
        this.strPassword = strPassword;
    }

    /*******************************************************************************************************************
    * @return JDBC driver
    *******************************************************************************************************************/
    public String getDriver() 
    {
        return(strDriver);
    }

    /*******************************************************************************************************************
    * @return JDBC connect string
    *******************************************************************************************************************/
    public String getConnect() 
    {
        return(strConnect);
    }

    /*******************************************************************************************************************
    * @return DB user name
    *******************************************************************************************************************/
    public String getUser() 
    {
        return(strUser);
    }

    /*******************************************************************************************************************
    * @return User password (may be missing or blank)
    *******************************************************************************************************************/
    public String getPassword() 
    {
        return(strPassword);
    }
}
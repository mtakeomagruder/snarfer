package com.truelogic.snarfer.config;

public class ConfigDb 
{
    private String strDriver;           // JDBC driver
    private String strConnect;          // JDBC connect string
    private String strUser;             // The DB user name
    private String strPassword;         // The user password (may be missing or blank)
    
    public ConfigDb(String strDriver, String strConnect, String strUser, String strPassword)
    {
        this.strDriver = strDriver;
        this.strConnect = strConnect;
        this.strUser = strUser;
        this.strPassword = strPassword;
    }

    public String getDriver() 
    {
        return(strDriver);
    }

    public String getConnect() 
    {
        return(strConnect);
    }

    public String getUser() 
    {
        return(strUser);
    }

    public String getPassword() 
    {
        return(strPassword);
    }
}
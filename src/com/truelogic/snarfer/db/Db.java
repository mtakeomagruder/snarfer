package com.truelogic.snarfer.db;

// Java imports
import java.sql.*;

// Project imports
import com.truelogic.snarfer.config.*;

/***********************************************************************************************************************
* Db class to handle database connections.
* 
* @author David Steele
***********************************************************************************************************************/
public class Db 
{
    private Connection oDb = null;  // The JDBC database connection
    
    /*******************************************************************************************************************
    * Initializes Db and connects to the JDBC database.
    *******************************************************************************************************************/
    public Db(ConfigDb oConfigDb) throws ClassNotFoundException, SQLException
    {
        Class.forName(oConfigDb.getDriver());

        oDb = DriverManager.getConnection(oConfigDb.getConnect(), oConfigDb.getUser(), oConfigDb.getPassword());
        oDb.setAutoCommit(false);
    }
    
    /*******************************************************************************************************************
    * @return JDBC database connection
    *******************************************************************************************************************/
    protected Connection getDb()
    {
        return(oDb);
    }
}
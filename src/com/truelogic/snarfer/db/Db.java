package com.truelogic.snarfer.db;

// Java imports
import java.sql.*;

// Project imports
import com.truelogic.snarfer.config.*;

public class Db 
{
    private Connection oDb = null;
    
    public Db(ConfigDb oConfigDb) throws ClassNotFoundException, SQLException
    {
        Class.forName(oConfigDb.getDriver());

        oDb = DriverManager.getConnection(oConfigDb.getConnect(), oConfigDb.getUser(), oConfigDb.getPassword());
        oDb.setAutoCommit(false);
    }
    
    protected Connection getDb()
    {
        return(oDb);
    }
}

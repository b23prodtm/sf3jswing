package thegame.rem;

import java.sql.Statement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import net.sf.jiga.xtended.kernel.JXAenvUtils;

public class SQLconnection {

    static {
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        }
    }
    Connection jdbc = null;
    public String sqlUrl = "mysql://mysql-s.sourceforge.net:4040/s173003_thegame";
    public String sqlUserRW = "s173003rw";
    public String sqlPassRW = "ksameht";
    public String sqlUserRO = "s173003ro";
    public String sqlPassRO = "password";
    private boolean write = false;

    /**@param write if true, a Read/Write will be opened upon connecting, if false, a Read-Only connection will be made*/
    public SQLconnection(boolean write) {
        super();
        this.write = write;
    }

    /** creates a read-only connection handle */
    public SQLconnection() {
        this(false);
    }

    public void connect() throws SQLException {
        jdbc = DriverManager.getConnection("jdbc:" + sqlUrl, write ? sqlUserRW : sqlUserRO, write ? sqlPassRW : sqlPassRO);
    }

    public void disconnect() throws SQLException {
        if (jdbc != null) {
            jdbc.close();
        }
    }

    /** @param sqlQuery SELECT query string*/
    public ResultSet query(String sqlQuery) throws SQLException {
        Statement query = jdbc.createStatement();
        System.out.print(JXAenvUtils._JXAEnvOutput("SQL query : " + sqlQuery, JXAenvUtils.APP_NOTICE));
        return query.executeQuery(sqlQuery);
    }

    /** @param sqlUpdate UPDATE, INSERT or DELETE query string
     * @return affected rows count  */
    public int update(String sqlUpdate) throws SQLException {
        Statement query = jdbc.createStatement();
        System.out.print(JXAenvUtils._JXAEnvOutput("SQL update : " + sqlUpdate, JXAenvUtils.APP_NOTICE));
        return query.executeUpdate(sqlUpdate);
    }
}

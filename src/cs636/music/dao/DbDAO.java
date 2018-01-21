package cs636.music.dao;

import java.util.Date;
import java.text.SimpleDateFormat;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static cs636.music.dao.DBConstants.*;
import static cs636.music.dao.DBConstants.SYS_TABLE;

/**
 * Database connection and initialization.
 * Implemented singleton on this class.
 * 
 * @author Chung-Hsien (Jacky) Yu
 * 
 */
public class DbDAO {
	   
	private Connection connection;
	// used for insert command when type timestamp
	private String timestampstr; 
	
	/**
	 *  Use to connect to databases through JDBC drivers
	 *  @param dbUrl connection string
	 *  @param usr  user name
	 *  @param passwd password
	 *  @throws  SQLException
	 */
	public DbDAO(String dbUrl, String usr, String passwd) throws SQLException {
			String driver = null;
			if (dbUrl == null)
				dbUrl = HSQLDB_URL;  // default to HSQLDB
				timestampstr = "";
			if (dbUrl.contains("oracle")) {
				driver = ORACLE_DRIVER;
				timestampstr = "timestamp";
			} else if (dbUrl.contains("mysql")) {
				driver = MYSQL_DRIVER;
				timestampstr = "timestamp";
			} else if (dbUrl.contains("hsqldb")) {
				driver = HSQLDB_DRIVER;
				timestampstr = "";
				usr = "sa";
				passwd = "";
			} else throw new SQLException("Unknown DB URL pattern in DbDAO constructor");
			System.out.println("Connecting using driver "+ driver+ ", DB URL " + dbUrl);
			try { 
				Class.forName(driver);
			} catch (Exception e) {
				throw new SQLException("Problem with loading driver: " + e);
			}
			connection = DriverManager.getConnection(dbUrl, usr, passwd);
	   }
	/**
	 *  Return the built connection
	 *  @return  Connection object established by DbDAO
	 */
	public Connection getConnection() {
		return connection;
	}
	
	/**
	 *  Terminate the built connection
	 *  @throws  SQLException
	 */
	public void close() throws SQLException {
		connection.close();  // this object opened it, so it gets to close it
	}
	
	/**
	*  bring DB back to original state
	*  @throws  SQLException
	**/
	public void initializeDb() throws SQLException {
		clearTable(DOWNLOAD_TABLE);
		clearTable(LINEITEM_TABLE);
		clearTable(INVOICE_TABLE);
		clearTable(USER_TABLE);
		clearTable(SYS_TABLE);
		initSysTable();		
	}

	/**
	*  Delete all records from the given table
	*  @param tableName table name from which to delete records
	*  @throws  SQLException
	**/
	private void clearTable(String tableName) throws SQLException {
		Statement stmt = connection.createStatement();
		try {
			stmt.execute("delete from " + tableName);
		} finally {
			stmt.close();
		}
	}
	
	/**
	*  Set all the index number used in other tables back to 1
	*  @throws  SQLException
	**/
	private void initSysTable() throws SQLException {
		Statement stmt = connection.createStatement();
		try {
			stmt.execute("insert into " + SYS_TABLE + " values (1,1,1,1)");
		} finally {
			stmt.close();
		}
	}
	/**
	 * format a date type date into appropriate string base on database 
	 * that current connection connects to.  Using package protection
	 * to indicate this is for DAO use--it's specific to DB needs.
	 * @param date
	 * @return time stamp as string
	 */
    String formatTimestamp(Date date) {
    	String outstr="";
    	
    	SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
    	outstr = timestampstr + " '" + formatter.format(date)+"'";
    	
    	return outstr;
    }
    
	// Note package scope: no need to call this from service layer
	void advanceId(String columnName) throws SQLException
	{
		Statement stmt = connection.createStatement();
		try {
			stmt.executeUpdate(" update " + SYS_TABLE
					+ " set " + columnName + " = " + columnName + " + 1");
		} finally {
			stmt.close();
		}
	}

	// This shows one good way to produce new primary key value--use another
	// table's data to specify the next value
	// Here we use SYS_TABLE columns next_order_id, etc.
	int findNextId(String columnName) throws SQLException
	{
		int nextId;
		Statement stmt = connection.createStatement();
		try {
			ResultSet set = stmt.executeQuery(" select " + columnName + " from " + SYS_TABLE);
			set.next();
			nextId = set.getInt(columnName);
		} finally {
			stmt.close();
		}
		advanceId(columnName);
		return nextId;
	}

}

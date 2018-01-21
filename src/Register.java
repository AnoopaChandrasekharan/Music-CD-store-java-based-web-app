
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.DriverManager;

public class Register {
	public static final String MYSQL_DRIVER = "com.mysql.jdbc.Driver";
	public static final String ORACLE_DRIVER = "oracle.jdbc.OracleDriver";
	public static final String HSQLDB_DRIVER = "org.hsqldb.jdbcDriver";
	public static final String HSQLDB_URL = "jdbc:hsqldb:hsql://localhost/";

	public static final String DOWNLOAD_TABLE = "download";
	public static final String SYS_TABLE = "music_sys_tab";
	public static final String INVOICE_TABLE = "invoice";
	public static final String LINEITEM_TABLE = "lineitem";
	public static final String PRODUCT_TABLE = "product";
	public static final String TRACK_TABLE = "track";
	public static final String USER_TABLE = "site_user";
	public static final String ADMIN_TABLE = "userpass";
	private Connection connection;
	
	public Register(String dbUrl, String user, String pass)
			throws Exception {
		connection = getConnection(dbUrl, user, pass);
		initializeDb();
	}
	
	public void initializeDb() throws SQLException {
		clearTable(DOWNLOAD_TABLE);
		clearTable(LINEITEM_TABLE);
		clearTable(INVOICE_TABLE);
		clearTable(USER_TABLE);
		clearTable(SYS_TABLE);
		initSysTable();		
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
	
	//clear all tables
	private void clearTable(String tableName) throws SQLException {
		Statement stmt = connection.createStatement();
		try {
			stmt.execute("delete from " + tableName);
		} finally {
			stmt.close();
		}
	}
	
	private Connection getConnection(String dbUrl, String user, String pass) throws SQLException{
		String driver = null;
		if (dbUrl == null)
			dbUrl = HSQLDB_URL;  // default to HSQLDB
		if (dbUrl.contains("oracle")) {
			driver = ORACLE_DRIVER;
		} else if (dbUrl.contains("mysql")) {
			driver = MYSQL_DRIVER;
		} else if (dbUrl.contains("hsqldb")) {
			driver = HSQLDB_DRIVER;
			user = "sa";
			pass = "";
		} else throw new SQLException("Unknown DB URL pattern in DbDAO constructor");
		System.out.println("Connecting using driver "+ driver+ ", DB URL " + dbUrl);
		try { 
			Class.forName(driver);
		} catch (Exception e) {
			throw new SQLException("Problem with loading driver: " + e);
		}
		return DriverManager.getConnection(dbUrl, user, pass);
	}
	
	public void close() throws SQLException{
		connection.close();
	}
	
	//add user
	public void addUser(String fName, 
			String lName, 
			String email,
			String company,
			String add1,
			String add2, 
			String city,
			String state, 
			String zip,
			String country,
			String ccType,
			String ccNum,
			String ccExp
			) throws Exception{
		//Get max user id
		Statement stmt = connection.createStatement();
		try {
			int newUserId = findNextId();
			stmt.executeUpdate(
					"insert into " + USER_TABLE + " (user_id, firstname, lastname, email_address, company_name, address1, address2, city, state, zip, country, creditcard_type, creditcard_number, creditcard_expirationdate) values (" 
			         + newUserId + ", '" 
					 + fName + "', '" 
			         + lName + "', '" 
			         + email + "', '" 
			         + company + "', '" 
			         + add1 + "', '" 
			         + add2 + "', '" 
			         + city + "', '" 
			         + state + "', '" 
			         + zip + "', '" 
			         + country + "', '" 
			         + ccType + "', '" 
			         + ccNum + "', '" 
					 + ccExp + "')");

		} finally {
			stmt.close();
		}
	}
	
	//get next userid
	void advanceId() throws SQLException
	{
		Statement stmt = connection.createStatement();
		try {
			stmt.executeUpdate(" update " + SYS_TABLE
					+ " set USER_ID  = USER_ID + 1");
		} finally {
			stmt.close();
		}
	}


	int findNextId() throws SQLException
	{
		int nextId;
		Statement stmt = connection.createStatement();
		try {
			ResultSet set = stmt.executeQuery(" select USER_ID from " + SYS_TABLE);
			set.next();
			nextId = set.getInt("USER_ID");
		} finally {
			stmt.close();
		}
		advanceId();
		return nextId;
	}
	
	public static void main(String[] args) {
		String dbUrl = null;
		String usr = null;
		String pw = null;
		if (args.length == 3) {
			dbUrl = args[0];
			usr = args[1];
			pw = args[2];
		} else {
			System.out
					.println("usage:java <dbURL> <user> <passwd> ");
			return;
		}
		try{
			Register register = new Register(dbUrl, usr, pw);
			register.addUser("Anoopa", 
					"C", 
					"anoopa@umb.edu",
					"UMASS",
					"123 Comm Ave",
					"Apt 1",
					"Boston",
					"MA",
					"02210",
					"USA",
					"VISA",
					"1234567890901234",
					"0920");
			register.close();
		}catch(Exception e){
			System.out.println("Exception occured :" + e.getMessage());
			e.printStackTrace();
		}
		
	}
}

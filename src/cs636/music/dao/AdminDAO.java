package cs636.music.dao;

import static cs636.music.dao.DBConstants.ADMIN_TABLE;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;


/**
 * 
 * Access admin related tables through this class.
 * @author Chung-Hsien (Jacky) Yu
 */
public class AdminDAO {

	private Connection connection;
	
	public AdminDAO (DbDAO db) {
		connection = db.getConnection();
	}
	
	
	/**
	 * check login user name and password
	 * @param uid
	 * @param pwd
	 * @return
	 * @throws SQLException
	 */
	public Boolean findAdminUser(String uid, String pwd) throws SQLException {
		Statement stmt = connection.createStatement();
		try {
			ResultSet set = stmt.executeQuery(" select * from " + ADMIN_TABLE +
					" where username = '" + uid + "'" +
					" and password = '" + pwd + "'");
			if (set.next()){ // if the result is not empty
				set.close();
				return true;
			}
		} finally {
			stmt.close();
		}
		return false;
	}
}

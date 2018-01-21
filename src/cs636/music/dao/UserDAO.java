package cs636.music.dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import cs636.music.domain.User;

import static cs636.music.dao.DBConstants.USER_TABLE;

public class UserDAO {

	private Connection connection;
	private DbDAO dbDao;
	
	public UserDAO(DbDAO db){
		dbDao = db;
		connection = dbDao.getConnection();
	}
	
	
	//add user
	public User addUser(User user) throws SQLException{
		//Get max user id
		Statement stmt = connection.createStatement();
		try {
			User u = this.getUser(user.getEmailAddress());
			if(u != null){
				System.out.println("User with this email already exists");
				return u;
			}
			int newUserId = dbDao.findNextId("USER_ID");
			user.setId(newUserId);
			stmt.executeUpdate(
					"insert into " + USER_TABLE + " (user_id, firstname, lastname, email_address) values (" 
			         + newUserId + ", '" 
					 + user.getFirstname() + "', '" 
			         + user.getLastname() + "', '" 
			         + user.getEmailAddress()  + "')");

		} finally {
			stmt.close();
		}
		return user;
	}
	
	public User getUser(String email) throws SQLException{
		User u = null;
		Statement stmt = connection.createStatement();
		try {
			ResultSet set = stmt.executeQuery("select user_id, firstname, lastname from " + USER_TABLE + " where email_address = '" + email + "'");
			while(set.next()){
				u=new User();
				u.setId(set.getInt("user_id"));
				u.setFirstname(set.getString("firstname"));
				u.setLastname(set.getString("lastname"));
				u.setEmailAddress(email);
			}
		} finally {
			stmt.close();
		}
		return u;
	}
	
	public User findUserByID(int userId) throws SQLException{
		User u = null;
		Statement stmt = connection.createStatement();
		try {
			ResultSet set = stmt.executeQuery("select firstname, lastname, email_address from " + USER_TABLE + " where user_id = " + userId );
			while(set.next()){
				u=new User();
				u.setId(userId);
				u.setFirstname(set.getString("firstname"));
				u.setLastname(set.getString("lastname"));
				u.setEmailAddress(set.getString("email_address"));
			}
		} finally {
			stmt.close();
		}
		return u;
	}
}

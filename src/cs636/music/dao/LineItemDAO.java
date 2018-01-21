package cs636.music.dao;

import static cs636.music.dao.DBConstants.LINEITEM_TABLE;
import static cs636.music.dao.DBConstants.SYS_TABLE;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import cs636.music.domain.LineItem;

/**
 * 
 * Access line item table through this class. 
 * This code could be moved into InvoiceDAO.
 * @author Chung-Hsien (Jacky) Yu
 */
public class LineItemDAO {
	
	private Connection connection;
	
	/**
	 * An Data Access Object for LineItem table
	 * @param db the database connection
	 * @throws SQLException
	 */
	public LineItemDAO(DbDAO db) {
		connection = db.getConnection();
	}
	
	/**
	 * Insert a line item into an given (by invoice id) invoice
	 * @param invoiceID invoice id
	 * @param item new line item
	 * @throws SQLException
	 */
	public void insertLineItem(long invoiceID, LineItem item) throws SQLException{
		Statement stmt = connection.createStatement();
		int lineitem_id = getNextLineItemID();
		item.setId(lineitem_id);
		try {
			String sqlString = "insert into " + LINEITEM_TABLE + 
			" (lineitem_id, invoice_id, product_id, quantity) values ("
			+ item.getId() + ", " + invoiceID + ", "
			+ item.getProduct().getId() + ", " + item.getQuantity() + ") ";
			stmt.execute(sqlString);
		} finally {
			stmt.close();
		}
	}
	
	/**
	 * Increase lineitem_id by 1 in the system table
	 * @throws SQLException
	 */
	private void advanceLineItemID() throws SQLException
	{
		Statement stmt = connection.createStatement();
		try {
			stmt.executeUpdate(" update " + SYS_TABLE
					+ " set lineitem_id = lineitem_id + 1");
		} finally {
			stmt.close();
		}
	}
	
	/**
	 * Get the available line item id 
	 * @return the line item id available 
	 * @throws SQLException
	 */
	private int getNextLineItemID() throws SQLException
	{
		int nextLID;
		Statement stmt = connection.createStatement();
		try {
			ResultSet set = stmt.executeQuery(" select lineitem_id from " + SYS_TABLE);
			set.next();
			nextLID = set.getInt("lineitem_id");
		} finally {
			stmt.close();
		}
		advanceLineItemID(); // the id has been taken, so set +1 for next one
		return nextLID;
	}
}

package cs636.music.dao;

import static cs636.music.dao.DBConstants.INVOICE_TABLE;
import static cs636.music.dao.DBConstants.LINEITEM_TABLE;
import static cs636.music.dao.DBConstants.SYS_TABLE;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;

import cs636.music.domain.Invoice;
import cs636.music.domain.LineItem;
import cs636.music.domain.Product;


/**
 * 
 * Access Invoice table through this class. 
 * @author Chung-Hsien (Jacky) Yu
 */
public class InvoiceDAO {
	private Connection connection;
	private LineItemDAO lineitemdb;
//  TODO: implement UserDAO and ProductDAO, uncomment the following
	private UserDAO userdb;
	private ProductDAO proddb;
	
	/**
	 * An Data Access Object for Invoice table table
	 * @param db the database connection 
	 * @throws SQLException
	 */
	public InvoiceDAO(DbDAO db, LineItemDAO linedb, UserDAO udb, ProductDAO prddb)  {
		connection = db.getConnection();
		lineitemdb = linedb;
		userdb = udb;
		proddb = prddb;
	}

	/**
	 * Increase invoice_id by 1 in the system table
	 * @throws SQLException
	 */
	private void advanceInvoiceID() throws SQLException
	{
		Statement stmt = connection.createStatement();
		try {
			stmt.executeUpdate(" update " + SYS_TABLE
					+ " set invoice_id = invoice_id + 1");
		} finally {
			stmt.close();
		}
	}
	
	/**
	 * Get the available invoice id 
	 * @return the invoice id available 
	 * @throws SQLException
	 */
	private int getNextInvoiceID() throws SQLException
	{
		int nextIID;
		Statement stmt = connection.createStatement();
		try {
			ResultSet set = stmt.executeQuery(" select invoice_id from " + SYS_TABLE);
			set.next();
			nextIID = set.getInt("invoice_id");
		} finally {
			stmt.close();
		}
		advanceInvoiceID(); // the id has been taken, so set +1 for next one
		return nextIID;
	}
	
	
	/**
	 * Insert invoice data into invoice and lineitme table
	 * uses current_time to set invoice_date timestamp
	 * @param invoice
	 * @throws SQLException
	 */
	public void insertInvoice(Invoice invoice)throws SQLException{
		Statement stmt = connection.createStatement();
		long invoiceID =  getNextInvoiceID();
		String YN = "";
		if (invoice.isProcessed()){
			YN = "y";
		}else{
			YN = "n";
		}
		try{
			String sqlString = "insert into "+ INVOICE_TABLE + " values (" +
			invoiceID + ", " + 
			invoice.getUser().getId()+ " , " + "current_timestamp, " +
			//dbdao.formatTimestamp(invoice.getInvoiceDate()) + ", " + 
			invoice.getTotalAmount().toPlainString() + ", " +
			"'" + YN +"')";
			stmt.execute(sqlString);
			for (LineItem item: invoice.getLineItems()){
			   lineitemdb.insertLineItem(invoiceID, item);
			}
		} finally {
			stmt.close();
		}
	}
	
	
	/**
	 * find one invoice and its items by given invoice id
	 * @param invoiceId
	 * @return an invoice data with its items, null if not found
	 * @throws SQLException
	 */
	public Invoice findInvoice(long invoiceId) throws SQLException{
		Invoice invoice = null;
		Statement stmt = connection.createStatement();
		try 
		{
			String sqlString =  " select * from " + 
			INVOICE_TABLE + " i, " +
			LINEITEM_TABLE + " l " +		
			" where i.invoice_id = " + invoiceId + 
			" and i.invoice_id = l.invoice_id ";
			ResultSet set = stmt.executeQuery(sqlString);
			if (set.next()){ // if the result is not empty
				// first row: set up Invoice from invoice columns	
	//TODO: implement UserDAO and ProductDAO
				
				invoice= new Invoice(set.getInt("invoice_id"),
						userdb.findUserByID(set.getInt("user_id")),
						set.getTimestamp("invoice_date"),
						set.getString("is_processed") == "y",
						null,// items added below
						set.getBigDecimal("total_amount"));
				Set<LineItem> items = new HashSet<LineItem>();
				Product product = proddb.findProductByPID(set.getInt("product_id"));
				LineItem item = new LineItem(set.getInt("lineitem_id"), product, invoice, set.getInt("quantity"));
				items.add(item);
				while (set.next()){ 
					product = proddb.findProductByPID(set.getInt("product_id"));
					item = new LineItem(set.getInt("lineitem_id"), product, invoice, set.getInt("quantity"));
					items.add(item);
				}
				invoice.setLineItems(items);
			}
			set.close();
		} finally {
			stmt.close();
		}
		
		return invoice;
	}
	
	/**
	 * find all unprocessed invoice
	 * @return all unprocessed invoice in db
	 * @throws SQLException
	 */
	public Set<Invoice> findAllUnprocessedInvoice() throws SQLException{
		Set<Invoice> invoices = new HashSet<Invoice>();
		Statement stmt = connection.createStatement();
		Invoice invoice;
		
		String sqlString =  " select invoice_id from " + INVOICE_TABLE  +		
		" where is_processed = 'n'";
		try{
			ResultSet set = stmt.executeQuery(sqlString);
			while (set.next()){
				invoice = this.findInvoice(set.getInt("invoice_id"));
				invoices.add(invoice);
			}
			set.close();
		}finally{
			stmt.close();
		}
		return invoices;
	}
	
	/**
	 * find all invoices
	 * @return all invoices in db
	 * @throws SQLException
	 */
	public Set<Invoice> findAllInvoices() throws SQLException{
		Set<Invoice> invoices = new HashSet<Invoice>();
		Statement stmt = connection.createStatement();
		Invoice invoice;
		
		String sqlString =  " select invoice_id from " + INVOICE_TABLE ;
		try{
			// if performance matters, we could do this retrieval all
			// in one SQL statement, rather than in a programmed loop of them
			ResultSet set = stmt.executeQuery(sqlString);
			while (set.next()){
				invoice = this.findInvoice(set.getInt("invoice_id"));
				invoices.add(invoice);
			}
			set.close();
		}finally{
			stmt.close();
		}
		return invoices;
	}
	
	/**
	 * update the is_processed attribute of the invoice
	 * @param i  Invoice to update
	 * @throws SQLException
	 */
	public void updateInvoice(Invoice i)throws SQLException{
		Statement stmt = connection.createStatement();
		try{
			String sqlString = "update "+ INVOICE_TABLE + " set is_processed = 'y'" +
			" where invoice_id = " + i.getInvoiceId() ;
			System.out.println(sqlString);
			stmt.execute(sqlString);
		} finally {
			stmt.close();
		}
	}
}

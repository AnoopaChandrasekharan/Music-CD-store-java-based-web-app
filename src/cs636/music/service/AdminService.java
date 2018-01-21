package cs636.music.service;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import cs636.music.dao.AdminDAO;
import cs636.music.dao.DbDAO;
import cs636.music.dao.DownloadDAO;
import cs636.music.dao.InvoiceDAO;
import cs636.music.dao.ProductDAO;
import cs636.music.domain.Download;
import cs636.music.domain.Invoice;
import cs636.music.service.data.DownloadData;
import cs636.music.service.data.InvoiceData;

/**
 * 
 * Provide admin level services to user app through accessing DAOs 
 * @author Chung-Hsien (Jacky) Yu
 */
public class AdminService {
	
	private DbDAO db;
	private AdminDAO adminDb;
	private InvoiceDAO invoiceDb;
	private ProductDAO productDb;
	private DownloadDAO downloadDb;
	
	/**
	 * construct a admin service provider 
	 * @param dbDao
	 */
	public AdminService(DbDAO dbDao, AdminDAO adminDao, InvoiceDAO invoiceDao, ProductDAO productDao, DownloadDAO downloadDao) {
		db = dbDao;	
		adminDb = adminDao;
		invoiceDb = invoiceDao;
		productDb = productDao;
		downloadDb = downloadDao;
	}
	
	/**
	 * Clean all user table, not product and system table to empty
	 * and then set the index numbers back to 1
	 * @throws ServiceException
	 */
	public void initializeDB()throws ServiceException {
		try {
			db.initializeDb();
		} catch (SQLException e)
		{
			throw new ServiceException("Can't initialize DB: ", e);
		}	
	}
	
	/**
	 * process the invoice
	 * @param invoiceId
	 * @throws ServiceException
	 */
	public void processInvoice(long invoiceId) throws ServiceException {
		try {
			Invoice i = invoiceDb.findInvoice(invoiceId);
			if(i != null){
				invoiceDb.updateInvoice(i);
			}else{
				System.out.println("Invoice not found");;
			}
		} catch (SQLException e) {
			throw new ServiceException("Process invoice failed ", e);
		}
		
		System.out.println("TEMP: processing invoice");
	}

	/**
	 * Get a list of all invoices
	 * @return list of all invoices
	 * @throws ServiceException
	 */
	public Set<InvoiceData> getListofInvoices() throws ServiceException {
		System.out.println("TEMP: getting invoices");
		Set <InvoiceData> invoiceDs = new HashSet<InvoiceData>();
		Set<Invoice> invoices;
		try {
			invoices = invoiceDb.findAllInvoices();
		} catch (SQLException e) {
			throw new ServiceException("Get all invoices failed ", e);
		}
		InvoiceData id;
		for(Invoice i: invoices){
			id = new InvoiceData(i);
			invoiceDs.add(id);
		}
		return invoiceDs;
	}
	
	/**
	 * Get a list of all unprocessed invoices
	 * @return list of all unprocessed invoices
	 * @throws ServiceException
	 */
	public Set<InvoiceData> getListofUnprocessedInvoices() throws ServiceException {
		System.out.println("TEMP: getting unprocessed invoices");
		Set <InvoiceData> invoiceDs = new HashSet<InvoiceData>();
		Set<Invoice> invoices;
		try {
			invoices = invoiceDb.findAllUnprocessedInvoice();
		} catch (SQLException e) {
			throw new ServiceException("Get processed invoices failed ", e);
		}
		InvoiceData id;
		for(Invoice i: invoices){
			id = new InvoiceData(i);
			invoiceDs.add(id);
		}
		return invoiceDs;
	}
	
	/**
	 * Get a list of all downloads
	 * @return list of all downloads
	 * @throws ServiceException
	 */
	public Set<DownloadData> getListofDownloads() throws ServiceException {
		System.out.println("TEMP: getting downloads");
		Set <DownloadData> downloadDs = new HashSet<DownloadData>();
		Set<Download> downloads;
		try {
			downloads = downloadDb.findAllDownloads();
		} catch (SQLException e) {
			throw new ServiceException("Get downloads failed ", e);
		}
		DownloadData dd;
		for(Download d: downloads){
			dd = new DownloadData(d);
			downloadDs.add(dd);
		}
		return downloadDs;
	}
	
	
	/**
	 * Check login user
	 * @param username
	 * @param password
	 * @return true if useranme and password exist, otherwise return false
	 * @throws ServiceException
	 */
	public Boolean checkLogin(String username,String password) throws ServiceException {
		try {
			return adminDb.findAdminUser(username, password);
		} catch (SQLException e)
		{
			throw new ServiceException("Check login error: ", e);
		}
	}
	
}

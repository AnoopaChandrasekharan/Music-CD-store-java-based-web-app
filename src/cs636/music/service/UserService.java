package cs636.music.service;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;


import cs636.music.dao.DbDAO;
import cs636.music.dao.DownloadDAO;
import cs636.music.dao.InvoiceDAO;
import cs636.music.dao.LineItemDAO;
import cs636.music.dao.ProductDAO;
import cs636.music.dao.UserDAO;
import cs636.music.domain.User;
import cs636.music.domain.Cart;
import cs636.music.domain.Download;
import cs636.music.domain.Invoice;
import cs636.music.domain.LineItem;
import cs636.music.domain.Track;
import cs636.music.domain.Product;
import cs636.music.service.data.UserData;

public class UserService {
	private UserDAO userDb;
	private ProductDAO productDb;
	private DownloadDAO downloadDb;
	private LineItemDAO lineItemDb;
	private InvoiceDAO invoiceDb;
	
	/**
	 * construct a user service provider 
	 * @param dbDao
	 */

	public UserService(ProductDAO productDao, UserDAO userDao, DownloadDAO downloadDao, LineItemDAO lineItemDao, InvoiceDAO invoiceDao) {
		userDb = userDao;
		productDb = productDao;
		downloadDb = downloadDao;
		lineItemDb = lineItemDao;
		invoiceDb = invoiceDao;
	}
	
	public UserData addUser(String fName, 
			String lName, 
			String email
			) throws ServiceException{
		User user = new User();
		try{
			user.setFirstname(fName);
			user.setLastname(lName);
			user.setEmailAddress(email);
			user = userDb.addUser(user);
		}catch(SQLException e){
			throw new ServiceException("User register failed", e);
		}
		return new UserData(user);
	}
	
	public UserData getUserInfo(String email) throws ServiceException{
		UserData ud = null;
		try{
			ud = new UserData(userDb.getUser(email));
		}catch(SQLException e){
			throw new ServiceException("User fetch failed", e);
		}
		return ud;
	}
	
	public Set<Product> getProducts()throws ServiceException{
		Set<Product> products = null;
		try{
			products=productDb.getAllProducts();
		}catch(SQLException e){
			throw new ServiceException("product fetch failed", e);
		}
		return products;
	}
	
	public Product findProductByCode(String code) throws ServiceException{
		Product p=null;
		try{
			p=productDb.findProductByCode(code);
		}catch(SQLException e){
			throw new ServiceException("product fetch code failed", e);
		}
		return p;
	}
	
	public Product findProductById(int productId) throws ServiceException{
		Product p=null;
		try{
			p=productDb.findProductByPID(productId);
		}catch(SQLException e){
			throw new ServiceException("product fetch by id failed", e);
		}
		return p;
	}
	
	public Cart getNewCart(){
		return new Cart();
	}
	
	public void insertDownload(UserData ud, Track t) throws ServiceException{
		Download d = new Download();
		d.setTrack(t);
		d.setUser(this.convertUserDataToUser(ud));
		try{
			downloadDb.insertDownload(d);
		}catch(SQLException e){
			throw new ServiceException("insert download failed", e);
		}
		
	}
	
	public void addProductToCart(Product p, Cart c, int q){
		LineItem li = new LineItem();
		li.setProduct(p);
		li.setQuantity(q);
		c.addItem(li);
	}
	
	public void checkoutCart(UserData ud, Cart c) throws ServiceException{
		if(c.getItems().isEmpty()){
			System.out.println("No item in cart");
		}else{
			User u = this.convertUserDataToUser(ud);
			Invoice i = new Invoice();
			Set<LineItem> lis = c.getItems();
			BigDecimal total = new BigDecimal(0);
			for(LineItem li : lis){
				total = total.add(li.calculateItemTotal());
			}
			i.setLineItems(c.getItems());
			i.setProcessed(false);
			i.setTotalAmount(total);
			i.setInvoiceDate(new Date());
			i.setUser(u);
			try {
				invoiceDb.insertInvoice(i);
			} catch (SQLException e) {
				throw new ServiceException("insert invoice failed", e);
			}
		}

	}
	
	private User convertUserDataToUser(UserData ud){
		User u = new User();
		u.setId(ud.getId());
		u.setFirstname(ud.getFirstname());
		u.setLastname(ud.getLastname());
		u.setEmailAddress(ud.getEmailAddress());
		return u;
	}
}

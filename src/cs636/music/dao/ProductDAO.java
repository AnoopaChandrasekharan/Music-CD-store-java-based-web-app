package cs636.music.dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;

import cs636.music.domain.Invoice;
import cs636.music.domain.Product;
import cs636.music.domain.Track;

import static cs636.music.dao.DBConstants.INVOICE_TABLE;
import static cs636.music.dao.DBConstants.PRODUCT_TABLE;
import static cs636.music.dao.DBConstants.TRACK_TABLE;

public class ProductDAO {

	DbDAO db;
	private Connection connection;
	
	public ProductDAO(DbDAO dbDao){
		db = dbDao;
		connection= dbDao.getConnection();
	}
	
	public Track findTrackByTID(int trackId) throws SQLException{
		Track track = null;
		Statement stmt=  connection.createStatement();
		try{
			ResultSet set= stmt.executeQuery("select  product_id, track_number,title, sample_filename from " + TRACK_TABLE + " where track_id= "+ trackId +" ");
			while(set.next()){
				track = new Track();
				track.setId(trackId);
				track.setSampleFilename(set.getString("sample_filename"));
				track.setTrackNumber(set.getInt("track_number"));
				track.setTitle(set.getString("title"));
				track.setProduct(getProductInfoForTrack(set.getInt("product_id")));
			}
		}finally{
			stmt.close();
		}
		return track;
	}
	
	public Set<Track> findTracksForProduct(int productId) throws SQLException{
		Set<Track> tracks = new HashSet<Track>();
		Statement stmt=  connection.createStatement();
		String sqlString = "Select track_id from " + TRACK_TABLE + " where product_id = '" + productId + "'";
		Track track;
		try{
			ResultSet set = stmt.executeQuery(sqlString);
			while (set.next()){
				track = this.findTrackByTID(set.getInt("track_id"));
				tracks.add(track);
			}
			set.close();
		}finally{
			stmt.close();
		}
		return tracks;
	}
	
	public Product getProductInfoForTrack(int productId) throws SQLException{
		Product p= null;
		Statement stmt=  connection.createStatement();
		try{
			ResultSet set= stmt.executeQuery("select  product_code, product_description,product_price from " + PRODUCT_TABLE + " where product_id= "+productId+" ");
		    while(set.next()){
		    	Set <Track> tracks = new HashSet<Track> ();
		    	p= new Product(productId, set.getString("product_code"),set.getString("product_description"),set.getBigDecimal("product_price"), tracks);
		    }
		}finally{
			stmt.close();
		}
		
		return p;
	
	}	
	
	public Product findProductByPID(int productId) throws SQLException{
		Product p= null;
		Statement stmt=  connection.createStatement();
		try{
			ResultSet set= stmt.executeQuery("select  product_code, product_description,product_price from " + PRODUCT_TABLE + " where product_id= "+productId+" ");
		    while(set.next()){
		    	Set <Track> tracks = this.findTracksForProduct(productId);
		    	p= new Product(productId, set.getString("product_code"),set.getString("product_description"),set.getBigDecimal("product_price"), tracks);
		    }
		}finally{
			stmt.close();
		}
		
		return p;
	
	}
	
	public Product findProductByCode(String code) throws SQLException{
		Product p= null;
		Statement stmt=  connection.createStatement();
		try{
			ResultSet set= stmt.executeQuery("select  product_id, product_description,product_price from " + PRODUCT_TABLE + " where product_code= '"+code+"'");
		    while(set.next()){
		    	Set <Track> tracks = this.findTracksForProduct(set.getInt(1));
		    	p= new Product(set.getInt("product_id"),code ,set.getString("product_description"),set.getBigDecimal("product_price"), tracks);
		    }		
		}finally{
			stmt.close();
		}
		
		return p;
	}	
	
	
	public Set<Product> getAllProducts() throws SQLException{
		Set<Product> products= new HashSet<>();
		Statement stmt=connection.createStatement();
		Product p;
		try{
			ResultSet set= stmt.executeQuery("select product_id  from " + PRODUCT_TABLE + "");
			while(set.next()){
				p=this.findProductByPID(set.getInt("product_id"));
				products.add(p);
			}
			set.close();
		}finally{
		
			stmt.close();
		}
		return products;
	}
}

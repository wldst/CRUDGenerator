package generator.util;

import java.sql.Connection;
import java.sql.SQLException;

public class DbConnectionFactory {
	private String dbType="";
	
	public DBConnections getMysqlConnection(String url,String name,String pass){
		DBConnections connection=new DBConnections(url, name, pass);
		connection.setDriver("com.mysql.jdbc.Driver");
		return connection;
	}
	
	public DBConnections getOracleConnection(String url,String name,String pass){
		DBConnections connection=new DBConnections(url, name, pass);
		connection.setDriver("oracle.jdbc.driver.OracleDriver");
		return connection;
	}
	
	public Connection getConnection(String url,String name,String pass) throws ClassNotFoundException,SQLException{
		if(dbType.equals("mysql")){
			return getMysqlConnection(url, name, pass).getConn();
		}else if(dbType.equals("oracle")){
			return getOracleConnection(url, name, pass).getConn();
		}else{
			return null;
		}
	}

	public String getDbType() {
		return dbType;
	}

	public void setDbType(String dbType) {
		this.dbType = dbType;
	}
	
	

}

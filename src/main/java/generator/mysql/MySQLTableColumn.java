package generator.mysql;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 读取mysql某数据库下表的注释信息
 * 
 * @author xxx
 */
public class MySQLTableColumn {
	private static String dbName="akcome_dbuser";
	public static Connection getMySQLConnection() throws Exception {
		Class.forName("com.mysql.jdbc.Driver");
		Connection conn  = DriverManager.getConnection("jdbc:mysql://180.76.242.202:3306/"+dbName+"?characterEncoding=UTF-8&useUnicode=true&zeroDateTimeBehavior=convertToNull",
				"root", "Tdcq_ak&2017");
		return conn;
	}
	

	/**
	 * 获取当前数据库下的所有表名称
	 * @return
	 * @throws Exception
	 */
	public static List<String> getAllTableName() throws Exception {
		List<String> tables = new ArrayList<>();
		Connection conn = getMySQLConnection();
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery("SHOW TABLES ");
		while (rs.next()) {
			String tableName = rs.getString(1);
			tables.add(tableName);
		}
		rs.close();
		stmt.close();
		conn.close();
		return tables;
	}
	

	/**
	 * 获得某表的建表语句
	 * @param tableName
	 * @return
	 * @throws Exception
	 */
	public static Map<String,String> getCommentByTableName(List<String> tableName) throws Exception {
		Map<String,String> map = new HashMap<>();
		Connection conn = getMySQLConnection();
		Statement stmt = conn.createStatement();
		for (int i = 0; i < tableName.size(); i++) {
			String table = (String) tableName.get(i);
			ResultSet rs = stmt.executeQuery("SHOW CREATE TABLE " + table);
			if (rs != null && rs.next()) {
				String createDDL = rs.getString(2);
				String comment = parse(createDDL);
				map.put(table, comment);
			}
			rs.close();
		}
		stmt.close();
		conn.close();
		return map;
	}
	/**
	 * 获得某表中所有字段的注释
	 * @param tableName
	 * @return
	 * @throws Exception
	 */
	public static void compareColumn() throws Exception {
		List<String> columns = getColumns("ec_partner");
		dbName="acloud";
		List<String> columns2 = getColumns("acloud_user");

		System.out.println("");
		for(String ci:columns){
			if(columns2.contains(ci)){
//				System.out.print(ci+",");
				System.out.println(ci+"=new."+ci+",");
			}
			
		}
	}


	private static List<String> getColumns(String tableName) throws Exception, SQLException {
		List<String> list = new ArrayList<String>();
		
		Connection conn = getMySQLConnection();		
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery("show full columns from " + tableName);
		System.out.println("【"+tableName+"】");
	    while (rs.next()) { 
	    	String ci = rs.getString("Field");
			list.add(ci);
			System.out.print(ci+",");
		} 
		rs.close();
		stmt.close();
		conn.close();
		return list;
	}

	

	/**
	 * 返回注释信息
	 * @param all
	 * @return
	 */
	
	public static String parse(String all) {
		String comment = null;
		int index = all.indexOf("COMMENT='");
		if (index < 0) {
			return "";
		}
		comment = all.substring(index + 9);
		comment = comment.substring(0, comment.length() - 1);
		return comment;
	}

	public static void main(String[] args) throws Exception {
		compareColumn();
	}
}
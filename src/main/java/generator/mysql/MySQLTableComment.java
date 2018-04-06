package generator.mysql;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
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
public class MySQLTableComment {
	public static Connection getMySQLConnection() throws Exception {
		Class.forName("com.mysql.jdbc.Driver");
		Connection conn  = DriverManager.getConnection("jdbc:mysql://180.76.242.202:3306/akcome_dbuser?characterEncoding=UTF-8&useUnicode=true&zeroDateTimeBehavior=convertToNull",
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
	public static void getColumnCommentByTableName(List<String> tableName) throws Exception {
		Map<String,String>  map = new HashMap<>();
		Connection conn = getMySQLConnection();
		Statement stmt = conn.createStatement();
		for (int i = 0; i < tableName.size(); i++) {
			String table = (String) tableName.get(i);
			ResultSet rs = stmt.executeQuery("show full columns from " + table);
			System.out.println("【"+table+"】");
//			if (rs != null && rs.next()) {
				//map.put(rs.getString("Field"), rs.getString("Comment"));
		    while (rs.next()) {   
//			    System.out.println("字段名称：" + rs.getString("Field") + "\t"+ "字段注释：" + rs.getString("Comment") );
			    System.out.println(rs.getString("Field") + "\t:\t"+  rs.getString("Comment") );
			} 
//			}
			rs.close();
		}
		stmt.close();
		conn.close();
//		return map;
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
		List<String> tables = getAllTableName();
		Map<String,String>  tablesComment = getCommentByTableName(tables);
		Set<String>  names = tablesComment.keySet();
		Iterator iter = names.iterator();
		while (iter.hasNext()) {
			String name = (String) iter.next();
			System.out.println("Table Name: " + name + ", Comment: " + tablesComment.get(name));
		}
		
		getColumnCommentByTableName(tables);
	}
}
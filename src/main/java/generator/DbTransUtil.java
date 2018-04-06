package generator;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import generator.util.DBConnections;

/**
 * ****************************************************************
 *
 * Package:     work
 * Filename:    DbTransUtil.java
 * Description: 工具类,处理数据库字段和表名转换为类名,类属性.
 * Copyright:   Copyright (c) liuqiang 2017
 * @author:     liuqiang_pc
 * @version:    1.0.0
 * Create at:   2017年12月10日 下午9:59:18
 * 2017年3月9日 上午10:45:55 - first revision
 *
 ****************************************************************
 */
public class DbTransUtil {

	/**
	 * 功能：将输入字符串的首字母改成大写,驼峰，命名
	 * 
	 * @param str
	 * @return
	 */
	public static String initcap(String str) {
		String rename = str;
		if (rename.contains("_")) {
			StringBuilder sbBuilder = new StringBuilder();
			for (String stri : rename.trim().split("_")) {
				if(!"".equals(stri)){
					sbBuilder.append(firstBig(stri));
				}
			}
			;
			return firstBig(sbBuilder.toString());
		}
		return firstBig(rename.toLowerCase());
	}

	/**
	 * 属性名称
	 * 
	 * @param str
	 * @return
	 */
	public static String initAttrcap(String str) {
		String rename = str.toLowerCase();
		if (rename.contains("_")) {
			StringBuilder sbBuilder = new StringBuilder();
			int i = 0;
			String[] splits = rename.split("_");
			for (String stri : splits) {
				if (i > 0) {
					if(!"".equals(stri)){
						sbBuilder.append(firstBig(stri));
					}
				}
				i++;
			}
			return splits[0] + sbBuilder.toString();
		}
		return rename;
	}

	/**
	 * 首字母大写
	 * 
	 * @param rename
	 * @return
	 */
	public static String firstBig(String rename) {
		if(rename==null||rename.equals("")){
			return "";
		}
		char[] ch = rename.toCharArray();
		if (ch[0] >= 'a' && ch[0] <= 'z') {
			ch[0] = (char) (ch[0] - 32);
		}

		return new String(ch);
	}
	
	public static String generateFilename(String t) {
		String filename = "/media/liuqiang/repo/docker/NoSql/neo4j/import/ec";
//		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		filename += t;
//		filename += "_";
//		filename += sdf.format(new Date());
		filename += ".csv";
		return filename;
	}
    /**
     *  
     * @param tabList
     */
	public static List<String> readMySqlTable(Connection conn,String dbName,String filter) {
		List<String> tabList =new ArrayList<String>();
		Statement stmt = null;
		String sql = "SHOW TABLES FROM "+dbName;
		try {
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(sql);
			while (rs.next()) {
				String table = rs.getString(1);
				if(filter!=null&&!"".equals(filter)){
					boolean filtered=false;
					String[] split = filter.split(",");
					for(String si:split){
						if(table.toLowerCase().contains(si)&&table.toLowerCase().indexOf(si)==0){
							filtered=true;
						}
					}
					if(!filtered){
						tabList.add(table);
					}
				}else{
					tabList.add(table);
				}
			}
			stmt.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return tabList;
	}
	
	
}

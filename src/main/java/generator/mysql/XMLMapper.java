package generator.mysql;

import static generator.DbTransUtil.initAttrcap;
import static generator.DbTransUtil.initcap;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import generator.DbTransUtil;
/**
 * POJO Product
 * 
 * @author qliu 日期：2016-10-10
 */
public class XMLMapper {

	private String tablename = "";// 表名
	private String domianDao = "";
	private String domain = "";
	private String domainAttr = "";
	private String packageOutPath;
	private String entityPackage;
	private String surfix="Mapper";
	private String[] colnames; // 列名数组
	private String[] colTypes; // 列名类型数组
	private int[] colSizes; // 列名大小数组
	private String primaryKey;
	private String srcPath = "/src/main/java/";
	
	// 数据库连接
	private static final String URL = "jdbc:mysql://180.76.242.202:3306/akcome_dbuser?characterEncoding=UTF-8&useUnicode=true&zeroDateTimeBehavior=convertToNull";
	private static final String NAME = "root";
	private static final String PASS = "Tdcq_ak&2017";
	private static final String DRIVER = "com.mysql.jdbc.Driver";

	/*
	 * 构造函数
	 */
	public XMLMapper() {
	}
	
	

	public XMLMapper(String packageOutPath) {
		this.packageOutPath = packageOutPath;
	}



	private void init() {
		// 创建连接
		Connection con;
		// 查要生成实体类的表
		String sql = "select * from " + tablename;
		PreparedStatement pStemt = null;
		try {
			try {
				Class.forName(DRIVER);
			} catch (ClassNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			con = DriverManager.getConnection(URL, NAME, PASS);
			pStemt = con.prepareStatement(sql);
			ResultSetMetaData rsmd = pStemt.getMetaData();
			int size = rsmd.getColumnCount(); // 统计列
			colnames = new String[size];
			colTypes = new String[size];
			colSizes = new int[size];
			for (int i = 0; i < size; i++) {
				colnames[i] = rsmd.getColumnName(i + 1);
				colTypes[i] = rsmd.getColumnTypeName(i + 1);
			}
				createXml(colnames, colTypes, tablename);
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			// try {
			// con.close();
			// } catch (SQLException e) {
			// // TODO Auto-generated catch block
			// e.printStackTrace();
			// }
		}
	}

	public void createXml(String[] colnames, String[] colTypes,String tablename) {
		this.colnames=colnames;
		this.colTypes=colTypes;
		this.tablename = tablename;
		this.domianDao = packageOutPath+"."+initcap(tablename.toLowerCase())+surfix;
		this.domain = initcap(tablename.toLowerCase());
		this.domainAttr = initAttrcap(tablename.toLowerCase());
		String content = parse(colnames, colTypes, tablename);

		try {
			File directory = new File("");

			String fileName = directory.getAbsolutePath() + getSrcPath() 
					+packageOutPath.replace(".", File.separator)+File.separator+ domain
					+ surfix+".xml";
//			System.out.println(fileName);
			FileWriter fw = new FileWriter(fileName);
			PrintWriter pw = new PrintWriter(fw);
			pw.println(content);
			pw.flush();
			pw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 功能：生成实体类Dao的主要Mapper文件
	 * 
	 * @param colnames
	 * @param colTypes
	 * @param colSizes
	 * @return
	 */
	private String parse(String[] colnames, String[] colTypes,String tablename) {
		StringBuffer sb = new StringBuffer();
		sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n");
		sb.append(
				" <!DOCTYPE mapper PUBLIC \"-//mybatis.org//DTD Mapper 3.0//EN\" \"http://mybatis.org/dtd/mybatis-3-mapper.dtd\">\n");

		sb.append("<mapper namespace=\"" + domianDao + "\">\n");
		sqlColumns(sb);
		if(primaryKey==null||"".equals(primaryKey)){
			findbySQL(sb);
			whereCondition(sb);
			insertSQL(sb);
		}else{
			findbySQL(sb);
			whereCondition(sb);
			insertSQL(sb);
			updateSQL(sb);
		}
		
		sb.append("	 </mapper>\n");
		return sb.toString();
	}

	private void sqlColumns(StringBuffer sb) {
		sb.append("<sql id=\"" + domainAttr + "Columns\">\n");
		String column=processAllColumn().trim();
		column=	column.substring(0,column.length()-1);
		sb.append(column);
		sb.append("</sql>\n");
	}

	private void findbySQL(StringBuffer sb) {
		sb.append("	 <select id=\"findBy\" resultType=\""+entityPackage+"."+domain+"\" parameterType=\""+entityPackage+"."+domain+"\">\n");
		sb.append("	 SELECT\n");
		sb.append("	 	<include refid=\""+domainAttr+"Columns\" />\n");
		sb.append("	 FROM "+tablename+" a\n");
		sb.append("	 	<include refid=\"whereCondition\" />\n");
		sb.append("		ORDER BY ID DESC\n");
		sb.append("	</select>\n\n");
	}

	
	private void whereCondition(StringBuffer sb) {
		sb.append("	<sql id=\"whereCondition\"> \n");
		sb.append("	<where> 1=1 \n");
		
		for (int i = 0; i < colnames.length; i++) {
			String attri = initAttrcap(colnames[i].toLowerCase());	
			
			boolean timeAndBy = attri.equals("createBy")||attri.equals("createDate")||attri.equals("updateBy")||attri.equals("updateDate");
			if(timeAndBy){
				continue;
			}
			if(colTypes[i].equals("VARCHAR2")||colTypes[i].equals("VARCHAR")  ){
				sb.append("	 	<if test=\""+attri+" != null and "+attri+" != ''\">\n");				
				sb.append(" 	AND "+colnames[i]+" = #{"+attri+",jdbcType=VARCHAR}\n");				
				sb.append("	 </if>\n");
			}else if(colTypes[i].equals("CHAR")) {
				sb.append("	 	<if test=\""+attri+" != null \">\n");				
				sb.append("		AND "+colnames[i]+" = #{"+attri+",jdbcType=CHAR} \n");				
				sb.append("	 </if>\n");
			}else if(colTypes[i].equals("TIMESTAMP")) {
				sb.append("	 	<if test=\""+attri+" != null \">\n");				
				sb.append(" 	AND 	"+colnames[i]+" = #{"+attri+",jdbcType=TIMESTAMP}\n");				
				sb.append("	 </if>\n");
			}else if(colTypes[i].equals("NUMBER")) {
				if(colnames[i].toLowerCase().contains("id")){
					sb.append("	 	<if test=\""+attri+" != null \">\n");				
					sb.append(" 	AND	 "+colnames[i]+" = #{"+attri+",jdbcType=VARCHAR}\n");				
					sb.append("	 </if>\n");
				}else {
					sb.append("	 	<if test=\""+attri+" != null \">\n");				
					sb.append("		AND "+colnames[i]+" = #{"+attri+",jdbcType=NUMERIC}\n");				
					sb.append("	 </if>\n");
				}
			}
			
		}
		sb.append("	</where>\n</sql>");
	}


	private void insertSQL(StringBuffer sb) {
		sb.append("	<insert id=\"save\" parameterType=\""+entityPackage+"."+domain+"\">\n");
		sb.append("		INSERT INTO " + tablename + "(\n");		
		sb.append(getAllColumn());	
		sb.append("			) VALUES (\n");
		sb.append(getAllColumnValue());
		sb.append("			)\n");
		sb.append("		 	</insert>\n");
	}

	private void updateSQL(StringBuffer sb) {
		sb.append("	<update id=\"update\">\n");
		sb.append("	UPDATE " + tablename + " \n\t<set>");
		sb.append(getAllColumnSetValue());
		sb.append("</set>\n");
		appendPrimaryKey(sb);
		sb.append("		</update>\n");
	}



	private void appendPrimaryKey(StringBuffer sb) {
		if(!primaryKey.contains(",")){
			sb.append("			WHERE "+primaryKey+" = #{"+DbTransUtil.initAttrcap(primaryKey)+",jdbcType=VARCHAR}\n");
		}else{
			String[] ids = primaryKey.split(",");
			sb.append("			WHERE 1=1 ");
			for(String idi:ids){
				sb.append(" AND "+idi+" = #{"+DbTransUtil.initAttrcap(idi)+",jdbcType=VARCHAR} ");
			}
			sb.append("\n");
		}
	}

/**
 * 获取查询列
 * @return
 */
	private String processAllColumn() {
        StringBuilder sb = new StringBuilder();
		for (int i = 0; i < colnames.length; i++) {
			String attri = initAttrcap(colnames[i].toLowerCase());
			if (!colnames[i].toLowerCase().equals(attri)) {
				sb.append("\t  a." + colnames[i] + " AS "+  attri + ",\r\n");
			} else {
				sb.append("\t  a." + colnames[i] + ",\r\n");
			}
		}
		return sb.toString();
	}
	
	private String getAllColumnValue() {
        StringBuilder sb = new StringBuilder();
		for (int i = 0; i < colnames.length; i++) {		
			String attri = initAttrcap(colnames[i].toLowerCase());
				sb.append("#{" +attri + ",jdbcType="+sqlType2JDBCType(colTypes[i])+"},\r\n");
		}
		String columns=sb.toString();
		columns=	columns.substring(0,columns.lastIndexOf(","));
		return columns;
	}
	
	private String getAllColumnSetValue() {
        StringBuilder sb = new StringBuilder();
		for (int i = 0; i < colnames.length; i++) {
			if(colnames[i].toLowerCase().equals("id")){
				continue;
			}			
			String attri = initAttrcap(colnames[i].toLowerCase());
			sb.append("<if test=\""+attri +" != null \">\r\n");
				sb.append(colnames[i]+" = #{" +attri + ",jdbcType="+sqlType2JDBCType(colTypes[i])+"},\r\n");
				sb.append("</if>\r\n");
		}
		String columns=sb.toString();
		return columns;
	}
	
	private String getAllColumn() {
        StringBuilder sb = new StringBuilder();
		for (int i = 0; i < colnames.length; i++) {			
				sb.append( colnames[i] + ",");
		}
		String columns=sb.toString();
		columns=	columns.substring(0,columns.length()-1);
		return columns;
	}



	
	
	
	private String sqlType2JDBCType(String sqlType) {

		if (sqlType.equalsIgnoreCase("binary_double")) {
			return "NUMERIC";
		} else if (sqlType.equalsIgnoreCase("binary_float")) {
			return "NUMERIC";
		} else if (sqlType.equalsIgnoreCase("blob")) {
			return "BLOB";
		} else if (sqlType.equalsIgnoreCase("clob")) {
			return "CLOB";
		} else if (sqlType.equalsIgnoreCase("char") || sqlType.equalsIgnoreCase("nvarchar2")
				|| sqlType.equalsIgnoreCase("varchar2")) {
			return "VARCHAR";
		} else if (sqlType.equalsIgnoreCase("date") || sqlType.equalsIgnoreCase("timestamp")
				|| sqlType.equalsIgnoreCase("timestamp with local time zone")
				|| sqlType.equalsIgnoreCase("timestamp with time zone")) {
			return "TIMESTAMP";
		} else if (sqlType.equalsIgnoreCase("number")) {
			return "NUMERIC";
		}
		return "VARCHAR";
	}
	
	

	public String getSurfix() {
		return surfix;
	}



	public void setSurfix(String surfix) {
		this.surfix = surfix;
	}



	public String getEntityPackage() {
		return entityPackage;
	}



	public void setEntityPackage(String entityPackage) {
		this.entityPackage = entityPackage;
	}



	public String getPrimaryKey() {
		return primaryKey;
	}



	public void setPrimaryKey(String primaryKeys) {
		this.primaryKey = primaryKeys;
	}



	public String getSrcPath() {
		return srcPath;
	}



	public void setSrcPath(String srcPath) {
		this.srcPath = srcPath;
	}



	/**
	 * 出口 TODO
	 * 
	 * @param args
	 */
	public static void main(String[] args) {

		new XMLMapper().init();;

	}

}

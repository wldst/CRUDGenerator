package generator.mysql;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.Calendar;

import com.wldst.utils.FileUtils;

import generator.DbTransUtil;
/**
 * ****************************************************************
 *
 * Package:     work.mysql
 * Filename:    EntityMysql.java
 * Description: 
 * Copyright:   Copyright (c) liuqiang 2017
 * @author:     liuqiang_pc
 * @version:    1.0.0
 * Create at:   2017年12月7日 下午3:34:08
 * 2017年3月9日 上午10:45:55 - first revision
 *
 ****************************************************************
 */
public class EntityMysql {

	private String packageOutPath = "com.aec.entitys.aec";
	private String tablename = "AEC_PARTNER_EARN";
	private String tableComment="";
	private String[] colnames;
	private String[] colTypes;
	private String[] colMarks;
	private boolean util = true;
	private boolean sql = false;
	private String srcPath = "/src/main/java/";
	
	public EntityMysql() {
		
	}
	
	
	
	public EntityMysql(String packageOutPath, boolean util, boolean sql) {
		super();
		this.packageOutPath = packageOutPath;
		this.util = util;
		this.sql = sql;
	}



	public void dowork(){
		try {
			Class.forName("com.mysql.jdbc.Driver");
			Connection con = DriverManager.getConnection("jdbc:mysql://180.76.242.202:3306/akcome_dbuser?characterEncoding=UTF-8&useUnicode=true&zeroDateTimeBehavior=convertToNull",
					"root", "Tdcq_ak&2017");
			PreparedStatement pStemt = con.prepareStatement("select * from " + tablename);
			ResultSetMetaData rsmd = pStemt.getMetaData();
			int size = rsmd.getColumnCount();
			colnames = new String[size];
			colTypes = new String[size];
			colMarks = new String[size];
			for (int i = 0; i < size; i++) {
				colnames[i] = rsmd.getColumnName(i + 1);
				colTypes[i] = rsmd.getColumnTypeName(i + 1);
				if (colTypes[i].equalsIgnoreCase("datetime")) {
					util = true;
				}
				if (colTypes[i].equalsIgnoreCase("image") || colTypes[i].equalsIgnoreCase("text")) {
					sql = true;
				}
			}
			ResultSet rs = pStemt.executeQuery("show full columns from " + tablename);
			int marki=0;
			while(rs.next()){
				String string = rs.getString("Comment");
				if(string!=null){
					colMarks[marki]=string;
				}
				marki++;
			}
			createEntity(colnames, colTypes,colMarks,tablename);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public  void createEntity(String[] colnames,String[] colTypes,String[] colMarks,String tablename) throws IOException {
		this.colnames=colnames;
		this.colTypes=colTypes;
		this.colMarks=colMarks;
		String content = parse(colnames, colTypes,tablename);
		File directory = new File("");
		String packageDir = directory.getAbsolutePath() + "/src/"
						+this.packageOutPath.replace(".", "/");
		String fileName = packageDir+"/"
				+DbTransUtil.initcap(tablename.toLowerCase()) + ".java";
		File file = new File(fileName);
//		System.out.println(fileName);
		FileWriter fw = new FileWriter(file);
		PrintWriter pw = new PrintWriter(fw);
		pw.print(content);
		pw.flush();
		pw.close();
	}

	public String parse(String[] colnames, String[] colTypes,String tablename) {
		StringBuffer sb = new StringBuffer();
		sb.append("package " + this.packageOutPath + ";\n");
		if (util) {
			sb.append("import java.util.Date;\nimport java.io.Serializable;\n");
		}
		if (sql) {
			sb.append("import java.sql.*;\n");
		}
		sb.append("/**\n\t*\n\t*\n\t* @comment" + getTableComment()+ "\n"
				+ "\t* @Author liuqiang "+ Calendar.getInstance().getTime().toLocaleString()
				+ "\n\t* @Email 1721903353@qq.com "
				+ "\n**/ \n");
		sb.append("public class " + DbTransUtil.initcap(tablename.toLowerCase()) + " implements Serializable {\n");
		 processAllAttrs(sb);//属性
		 processAllMethod(sb);//get set方法
		 toStringAllAttrs(sb);
		sb.append("}");
		return sb.toString();
	}
	/**
	 * 功能：生成所有属性
	 * @param sb
	 */
	private void processAllAttrs(StringBuffer sb) {
	 
	 for (int i = 0; i < colnames.length; i++) {
	  sb.append("\tprivate " + sqlType2JavaType(colTypes[i]) + " " + DbTransUtil.initAttrcap(colnames[i].toLowerCase()) + ";");
	  
	  String comment = colMarks[i].replaceAll("\n", "\n\t // ");
	  sb.append("\t //"+comment+"\r\n");
	 }
	 
	}
	
	/**
	 * 功能：生成所有属性
	 * @param sb
	 */
	private void toStringAllAttrs(StringBuffer sb) {
		
		sb.append("\t@Override\n");
		sb.append("\tpublic String toString() {\n");
		sb.append("\t\tStringBuilder sb = new StringBuilder();\n");
		sb.append("\t\tsb.append(getClass().getSimpleName());\n");
		sb.append("\t\tsb.append(\" [\");\n");
		sb.append("\t\tsb.append(\"Hash = \").append(hashCode()+\",\");\n");
	 for (int i = 0; i < colnames.length; i++) {
		 sb.append("\t\tsb.append(\""+DbTransUtil.initAttrcap(colnames[i].toLowerCase()) + "=\"+get" + DbTransUtil.initcap(colnames[i].toLowerCase()) + "()+\",\");\n");
	 }
	 sb.append("\t\tsb.append(\"]\");\n");
	 sb.append("\treturn sb.toString();\n\t}\n");
	}

	/**
	 * 功能：生成所有方法
	 * @param sb
	 */
	private void processAllMethod(StringBuffer sb) {
	 
	 for (int i = 0; i < colnames.length; i++) {
	  sb.append("\tpublic void set" + DbTransUtil.initcap(colnames[i].toLowerCase()) + "(" + sqlType2JavaType(colTypes[i]) + " " +
			  DbTransUtil.initAttrcap(colnames[i].toLowerCase()) + "){\r\n");
	  sb.append("\t\tthis." + DbTransUtil.initAttrcap(colnames[i].toLowerCase()) + "=" + DbTransUtil.initAttrcap(colnames[i].toLowerCase()) + ";\r\n");
	  sb.append("\t}\r\n");
	  sb.append("\tpublic " + sqlType2JavaType(colTypes[i]) + " get" + DbTransUtil.initcap(colnames[i].toLowerCase()) + "(){\r\n");
	  sb.append("\t\treturn " + DbTransUtil.initAttrcap(colnames[i].toLowerCase()) + ";\r\n");
	  sb.append("\t}\r\n");
	 }
	 
	}

	private String sqlType2JavaType(String sqlType) {
		if (sqlType.equalsIgnoreCase("bit")) {
			return "boolean";
		} else if (sqlType.equalsIgnoreCase("tinyint")) {
			return "byte";
		} else if (sqlType.equalsIgnoreCase("smallint")) {
			return "short";
		} else if (sqlType.equalsIgnoreCase("int")) {
			return "int";
		} else if (sqlType.equalsIgnoreCase("bigint")) {
			return "long";
		} else if (sqlType.equalsIgnoreCase("float")) {
			return "float";
		} else if (sqlType.equalsIgnoreCase("decimal") || sqlType.equalsIgnoreCase("numeric")
				|| sqlType.equalsIgnoreCase("real") || sqlType.equalsIgnoreCase("money")
				|| sqlType.equalsIgnoreCase("smallmoney")) {
			return "double";
		} else if (sqlType.equalsIgnoreCase("varchar") || sqlType.equalsIgnoreCase("char")
				|| sqlType.equalsIgnoreCase("nvarchar") || sqlType.equalsIgnoreCase("nchar")
				|| sqlType.equalsIgnoreCase("text")||sqlType.toLowerCase().contains("blob")) {
			return "String";
		}  else if (sqlType.equalsIgnoreCase("date") || sqlType.equalsIgnoreCase("timestamp")
				|| sqlType.equalsIgnoreCase("timestamp with local time zone")
				|| sqlType.equalsIgnoreCase("timestamp with time zone")) {
			return "Date";
		} else if (sqlType.equalsIgnoreCase("datetime")) {
			return "Date";
		} else if (sqlType.equalsIgnoreCase("image")) {
			return "Blod";
		}
		return null;
	}




	public String getTableComment() {
		return tableComment;
	}



	public void setTableComment(String tableComment) {
		this.tableComment = tableComment;
	}



	public static void main(String[] args) {
		new EntityMysql().dowork();
	}
}

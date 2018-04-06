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
import java.util.Calendar;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Select;

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
public class EntityMysqlJavaMapper {

	private String packageOutPath = "com.aec.entitys.aec";
	private String entityPath = "com.aec.entitys.aec";
	private String tablename = "AEC_PARTNER_EARN";
	private String primaryKey = "";
	private String[] colnames;
	private String[] colTypes;
	private boolean util = false;
	private boolean sql = false;
	private String srcPath = "/src/main/java/";
	
	public EntityMysqlJavaMapper() {
	}
	

	public EntityMysqlJavaMapper(String packageOutPath, boolean util, boolean sql) {
		super();
		this.packageOutPath = packageOutPath;
		this.util = util;
		this.sql = sql;
	}


	private void dowork() {
		try {
			Class.forName("com.mysql.jdbc.Driver");
			Connection con = DriverManager.getConnection("jdbc:mysql://180.76.242.202:3306/akcome_dbuser?characterEncoding=UTF-8&useUnicode=true&zeroDateTimeBehavior=convertToNull",
					"root", "Tdcq_ak&2017");
			PreparedStatement pStemt = con.prepareStatement("select * from " + tablename);
			ResultSetMetaData rsmd = pStemt.getMetaData();
			int size = rsmd.getColumnCount();
			colnames = new String[size];
			colTypes = new String[size];
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
			createMapper(colnames, colTypes,tablename);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void createMapper(String[] colnames, String[] colTypes,String tablename) throws IOException {
		this.colnames=colnames;
		this.colTypes=colTypes;
		this.tablename=tablename;
		String content = parse(colnames, colTypes,tablename);
		File directory = new File("");
		String fileName = directory.getAbsolutePath() + getSrcPath()
				+packageOutPath.replace(".", File.separator)+File.separator+ initcap(tablename.toLowerCase()) + "Mapper.java";
//		System.out.println(fileName);
		FileWriter fw = new FileWriter(fileName);
		PrintWriter pw = new PrintWriter(fw);
		pw.print(content);
		pw.flush();
		pw.close();
	}

	public String parse(String[] colnames, String[] colTypes,String tablename) {
		StringBuffer sb = new StringBuffer();
		sb.append("package " + this.packageOutPath + ";\n");
		sb.append("import "+getEntityPath()+"."+DbTransUtil.initcap(tablename.toLowerCase())+";\n");
		sb.append("import org.apache.ibatis.annotations.Mapper;\nimport org.apache.ibatis.annotations.Delete;\n");
		sb.append("import org.apache.ibatis.annotations.Select;\n");
		sb.append("import java.util.List;\n");
		
		
		
		
		if (util) {
			sb.append("import java.util.Date;");
		}
		if (sql) {
			sb.append("import java.sql.*;\n");
		}	
		
		sb.append("\n@Mapper\npublic interface " + DbTransUtil.initcap(tablename.toLowerCase()) + "Mapper{\n");
		 processAllMethod(sb);//get set方法
		sb.append("}");
		return sb.toString();
	}
	
	/**
	 * 功能：生成所有方法
	 * @param sb
	 */
	private void processAllMethod(StringBuffer sb) {
		if(primaryKey==null||"".equals(primaryKey)){
			  sb.append("\tpublic int save("+initcap(tablename.toLowerCase())+" data);\n ");		  
			  sb.append("\tpublic List<"+initcap(tablename.toLowerCase())+"> findBy("+initcap(tablename.toLowerCase())+" data);\n ");		  
		}else if(primaryKey.toLowerCase().equals("id")){
				  sb.append("\t @Select(value = { \"select * from "+tablename+" WHERE id=#{id,jdbcType=VARCHAR}\" })\n");	
				  sb.append("\tpublic "+DbTransUtil.initcap(tablename.toLowerCase())+" get(String id);\n ");
				  sb.append("\t @Select(value = { \"select * from "+tablename+" WHERE id=#{id,jdbcType=VARCHAR}\" })\n");	
				  sb.append("\tpublic "+initcap(tablename.toLowerCase())+" findById(String id);\n ");		  
				  sb.append("\tpublic int save("+initcap(tablename.toLowerCase())+" data);\n ");		  
				  sb.append("\tpublic List<"+initcap(tablename.toLowerCase())+"> findBy("+initcap(tablename.toLowerCase())+" data);\n ");		  
				  sb.append("\tpublic int update("+initcap(tablename.toLowerCase())+" data);\n ");		  
				  sb.append("\t @Delete(value=\"delete from "+tablename+" WHERE id = #{id,jdbcType=VARCHAR}\")\n");
				  sb.append("\tpublic int deleteById(String id);\n ");
		}else if(!primaryKey.toLowerCase().contains(",")){
			
		  sb.append("\t @Select(value = { \"select * from "+tablename+" WHERE "+primaryKey+"=#{"+initAttrcap(primaryKey)+",jdbcType=VARCHAR}\" })\n");
		  sb.append("\tpublic "+DbTransUtil.initcap(tablename.toLowerCase())+" get(String "+initAttrcap(primaryKey)+");\n ");
		  
		  sb.append("\t @Select(value = { \"select * from "+tablename+" WHERE "+primaryKey+"=#{"+initAttrcap(primaryKey)+",jdbcType=VARCHAR}\" })\n");
		  sb.append("\tpublic "+initcap(tablename.toLowerCase())+" findById(String "+initAttrcap(primaryKey)+");\n ");	
		  
		  sb.append("\tpublic int save("+initcap(tablename.toLowerCase())+" data);\n ");
		  
		  sb.append("\tpublic List<"+initcap(tablename.toLowerCase())+"> findBy("+initcap(tablename.toLowerCase())+" data);\n ");
		  
		  sb.append("\tpublic int update("+initcap(tablename.toLowerCase())+" data);\n ");		  
		  
		  sb.append("\t @Delete(value=\"delete from "+tablename+" WHERE "+primaryKey+" = #{"+initAttrcap(primaryKey)+",jdbcType=VARCHAR}\")\n");
		  sb.append("\tpublic int deleteById(String "+initAttrcap(primaryKey)+");\n ");
		}else if(primaryKey.toLowerCase().contains(",")){
			String pks[]=primaryKey.split(",");
			StringBuilder ids = new StringBuilder();
			  if(pks.length>1){
				  for(String pki:pks){
					  ids.append("String "+initAttrcap(pki)+", ");
				  }
			  }
			  String idsString = ids.substring(0, ids.lastIndexOf(", ")); 
			  
			  StringBuilder where = new StringBuilder();
			  if(pks.length>1){
				  for(String pki:pks){
					  where.append(pki+"=#{"+initAttrcap(pki)+",jdbcType=VARCHAR} AND ");
				  }
			  }
			  String whereStr = where.substring(0, where.lastIndexOf("AND"));
			  
			  sb.append("\t @Select(value = { \"select * from "+tablename+" WHERE "+whereStr+"\" })\n");
			  sb.append("\tpublic "+initcap(tablename.toLowerCase())+" findById("+idsString+");\n ");	
			  
			  sb.append("\tpublic int save("+initcap(tablename.toLowerCase())+" data);\n ");
			  
			  sb.append("\tpublic List<"+initcap(tablename.toLowerCase())+"> findBy("+initcap(tablename.toLowerCase())+" data);\n ");
			  
			  sb.append("\tpublic int update("+initcap(tablename.toLowerCase())+" data);\n ");		  
			  
			  sb.append("\t @Delete(value=\"delete from "+tablename+" WHERE "+whereStr+"\")\n");
			  sb.append("\tpublic int deleteById("+idsString+");\n ");
			}
	  	
	}


	public String getEntityPath() {
		return entityPath;
	}


	public void setEntityPath(String entityPath) {
		this.entityPath = entityPath;
	}


	public String getPrimaryKey() {
		return primaryKey;
	}


	public void setPrimaryKey(String primaryKey) {
		this.primaryKey = primaryKey;
	}


	public String getSrcPath() {
		return srcPath;
	}


	public void setSrcPath(String srcPath) {
		this.srcPath = srcPath;
	}


	public static void main(String[] args) {
		Long start = Calendar.getInstance().getTimeInMillis();
		
		new EntityMysqlJavaMapper();
		Long end = Calendar.getInstance().getTimeInMillis();
System.out.println("耗时"+(end-start)/1000+"秒");
	}
}

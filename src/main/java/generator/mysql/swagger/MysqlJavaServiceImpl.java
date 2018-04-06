package generator.mysql.swagger;

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
public class MysqlJavaServiceImpl {

	private String packageOutPath = "";
	private String entityPath = "";
	private String servicePath = "";
	private String mapperPath = "";
	private String mapperSufix = "";
	private String tablename = "";
	private String[] colnames;
	private String[] colTypes;
	private boolean util = false;
	private boolean sql = false;
	private String primaryKey;
	private String primaryKeys;
	private String srcPath = "/src/main/java/";
	
	public MysqlJavaServiceImpl() {
	}
	

	public MysqlJavaServiceImpl(String packageOutPath, boolean util, boolean sql) {
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
			createServiceImpl(colnames, colTypes,tablename);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void createServiceImpl(String[] colnames, String[] colTypes,String tablename) throws IOException {
		this.colnames=colnames;
		this.colTypes=colTypes;
		this.tablename=tablename;
		String content = parse(colnames, colTypes,tablename);
		File directory = new File("");
		String fileName = directory.getAbsolutePath() + getSrcPath()
				+packageOutPath.replace(".", File.separator)+File.separator+ initcap(tablename.toLowerCase()) + "ServiceImpl.java";
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
		sb.append("import org.springframework.stereotype.Service;\n");
		sb.append("import "+getMapperPath()+"."+DbTransUtil.initcap(tablename.toLowerCase())+getMapperSufix()+";\n");
		sb.append("import "+getServicePath()+".I"+DbTransUtil.initcap(tablename.toLowerCase())+"Service;\n");
		sb.append("import java.util.List;\n");
		sb.append("import java.util.Date;\n");
		sb.append("import org.springframework.beans.factory.annotation.Autowired;\n");
		
		
		if (util) {
			sb.append("import java.util.Date;");
		}
		if (sql) {
			sb.append("import java.sql.*;\n");
		}	
		
		sb.append("\n@Service\npublic class " + DbTransUtil.initcap(tablename.toLowerCase()) + "ServiceImpl implements I" + DbTransUtil.initcap(tablename.toLowerCase()) + "Service {\n");
		sb.append("\t@Autowired\n");
		sb.append("\tprivate "+DbTransUtil.initcap(tablename.toLowerCase())+mapperSufix+" "+DbTransUtil.initAttrcap(tablename.toLowerCase())+mapperSufix+";\n");
		
		 processAllMethod(sb);//get set方法
		sb.append("}");
		return sb.toString();
	}
	
	/**
	 * 功能：生成所有方法
	 * @param sb
	 */
	private void processAllMethod(StringBuffer sb) {
		if(primaryKey.contains(",")){
			sb.append("\t@Override\n");
			  sb.append("\tpublic "+initcap(tablename.toLowerCase())+" findById("+primaryKeys+"){\n ");		  
			  sb.append("\t\treturn "+DbTransUtil.initAttrcap(tablename.toLowerCase())+"Mapper.findById("+primaryKey+");\n");
			  sb.append("\t}\n\n");
			  
			  sb.append("\t@Override\n");
			  sb.append("\tpublic int save("+initcap(tablename.toLowerCase())+" data){\n ");		  
			  sb.append("\t\treturn "+DbTransUtil.initAttrcap(tablename.toLowerCase())+"Mapper.save(data);\n");
			  sb.append("\t}\n\n");
			  
			  sb.append("\t@Override\n");
			  sb.append("\tpublic List<"+initcap(tablename.toLowerCase())+"> findBy("+initcap(tablename.toLowerCase())+" data){\n ");		  
			  sb.append("return "+DbTransUtil.initAttrcap(tablename.toLowerCase())+"Mapper.findBy(data);\n");
			  sb.append("\t}\n\n");
			  sb.append("\t@Override\n");
			  sb.append("\tpublic int update("+initcap(tablename.toLowerCase())+" data){\n ");		  
			  sb.append("\t\t return "+DbTransUtil.initAttrcap(tablename.toLowerCase())+"Mapper.update(data);\n");
			  sb.append("\t}\n\n");
			  
			  sb.append("\t@Override\n");
			  sb.append("\tpublic int deleteById("+primaryKeys+"){\n ");
			  sb.append("\t\t return "+DbTransUtil.initAttrcap(tablename.toLowerCase())+"Mapper.deleteById("+primaryKey+");\n");
			  sb.append("\t}\n\n");
		}else{
			sb.append("\t@Override\n");
			  sb.append("\tpublic "+initcap(tablename.toLowerCase())+" findById(String id){\n ");		  
			  sb.append("\t\treturn "+DbTransUtil.initAttrcap(tablename.toLowerCase())+"Mapper.findById(id);\n");
			  sb.append("\t}\n\n");
			  
			  sb.append("\t@Override\n");
			  sb.append("\tpublic int save("+initcap(tablename.toLowerCase())+" data){\n ");		  
			  sb.append("\t\treturn "+DbTransUtil.initAttrcap(tablename.toLowerCase())+"Mapper.save(data);\n");
			  sb.append("\t}\n\n");
			  
			  sb.append("\t@Override\n");
			  sb.append("\tpublic List<"+initcap(tablename.toLowerCase())+"> findBy("+initcap(tablename.toLowerCase())+" data){\n ");		  
			  sb.append("return "+DbTransUtil.initAttrcap(tablename.toLowerCase())+"Mapper.findBy(data);\n");
			  sb.append("\t}\n\n");
			  sb.append("\t@Override\n");
			  sb.append("\tpublic int update("+initcap(tablename.toLowerCase())+" data){\n ");		  
			  sb.append("\t\t return "+DbTransUtil.initAttrcap(tablename.toLowerCase())+"Mapper.update(data);\n");
			  sb.append("\t}\n\n");
			  
			  sb.append("\t@Override\n");
			  sb.append("\tpublic int deleteById(String id){\n ");
			  sb.append("\t\t return "+DbTransUtil.initAttrcap(tablename.toLowerCase())+"Mapper.deleteById(id);\n");
			  sb.append("\t}\n\n");
		}
	  
	  
	}


	public String getEntityPath() {
		return entityPath;
	}


	public void setEntityPath(String entityPath) {
		this.entityPath = entityPath;
	}


	public String getPackageOutPath() {
		return packageOutPath;
	}


	public void setPackageOutPath(String packageOutPath) {
		this.packageOutPath = packageOutPath;
	}


	public String getMapperPath() {
		return mapperPath;
	}


	public void setMapperPath(String mapperPath) {
		this.mapperPath = mapperPath;
	}


	public String getTablename() {
		return tablename;
	}


	public void setTablename(String tablename) {
		this.tablename = tablename;
	}


	public boolean isUtil() {
		return util;
	}


	public void setUtil(boolean util) {
		this.util = util;
	}


	public boolean isSql() {
		return sql;
	}


	public void setSql(boolean sql) {
		this.sql = sql;
	}


	public String getMapperSufix() {
		return mapperSufix;
	}


	public void setMapperSufix(String mapperSufix) {
		this.mapperSufix = mapperSufix;
	}


	public String getPrimaryKey() {
		return primaryKey;
	}


	public void setPrimaryKey(String primaryKey) {
		this.primaryKey = primaryKey;
	}


	public String getServicePath() {
		return servicePath;
	}


	public void setServicePath(String servicePath) {
		this.servicePath = servicePath;
	}


	public String getSrcPath() {
		return srcPath;
	}


	public void setSrcPath(String srcPath) {
		this.srcPath = srcPath;
	}


	public String getPrimaryKeys() {
		return primaryKeys;
	}


	public void setPrimaryKeys(String primaryKeys) {
		this.primaryKeys = primaryKeys;
	}


	public static void main(String[] args) {
		Long start = Calendar.getInstance().getTimeInMillis();
		
		new MysqlJavaServiceImpl();
		Long end = Calendar.getInstance().getTimeInMillis();
System.out.println("耗时"+(end-start)/1000+"秒");
	}
}

package generator.mysql;

import static generator.DbTransUtil.initcap;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
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
public class MysqlJavaService {

	private String packageOutPath = "com.aec.service.aec";
	private String entityPath = "com.aec.entitys.aec";
	private String tablename = "AEC_PARTNER_EARN";
	private String[] colnames;
	private String[] colTypes;
	private boolean util = false;
	private boolean sql = false;
	private String srcPath = "/src/main/java/";
	
	public MysqlJavaService() {
	}
	

	public MysqlJavaService(String packageOutPath, boolean util, boolean sql) {
		super();
		this.packageOutPath = packageOutPath;
		this.util = util;
		this.sql = sql;
	}




	public void createMapper(String[] colnames, String[] colTypes,String tablename) throws IOException {
		this.colnames=colnames;
		this.colTypes=colTypes;
		this.tablename=tablename;
		String content = parse(colnames, colTypes,tablename);
		File directory = new File("");
		String fileName = directory.getAbsolutePath() + "/src/"
				+packageOutPath.replace(".", File.separator)+File.separator+ "I"+initcap(tablename.toLowerCase()) + "Service.java";
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
		sb.append("import java.util.List;\n");
		
		if (util) {
			sb.append("import java.util.Date;");
		}
		if (sql) {
			sb.append("import java.sql.*;\n");
		}	
		
		sb.append("\n\npublic interface I" + DbTransUtil.initcap(tablename.toLowerCase()) + "Service{\n");
		 processAllMethod(sb);//get set方法
		sb.append("}");
		return sb.toString();
	}
	
	/**
	 * 功能：生成所有方法
	 * @param sb
	 */
	private void processAllMethod(StringBuffer sb) {
//	  sb.append("\tpublic "+DbTransUtil.initcap(tablename.toLowerCase())+" get(String id);\n ");
	  sb.append("\tpublic "+initcap(tablename.toLowerCase())+" findById(String id);\n ");		  
	  sb.append("\tpublic int save("+initcap(tablename.toLowerCase())+" data);\n ");		  
	  sb.append("\tpublic List<"+initcap(tablename.toLowerCase())+"> findBy("+initcap(tablename.toLowerCase())+" data);\n ");		  
	  sb.append("\tpublic void update("+initcap(tablename.toLowerCase())+" data);\n ");		  
	  sb.append("\tpublic void updateById("+initcap(tablename.toLowerCase())+" data);\n ");
	  sb.append("\tpublic int deleteById(String id);\n ");	
	}


	public String getEntityPath() {
		return entityPath;
	}


	public void setEntityPath(String entityPath) {
		this.entityPath = entityPath;
	}


	public static void main(String[] args) {
		Long start = Calendar.getInstance().getTimeInMillis();
		
		new MysqlJavaService();
		Long end = Calendar.getInstance().getTimeInMillis();
System.out.println("耗时"+(end-start)/1000+"秒");
	}
}

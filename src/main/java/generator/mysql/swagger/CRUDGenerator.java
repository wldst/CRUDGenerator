package generator.mysql.swagger;

import static generator.DbTransUtil.initAttrcap;
import static generator.DbTransUtil.readMySqlTable;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import generator.mysql.EntityMysqlJavaMapper;
import generator.mysql.XMLMapper;;
public class CRUDGenerator {
	
	private String entityPath = "com.aec.entitys.aec";
	private String mapperPath = "com.aec.opt.mapper.install";
	private String servicePath = "com.aec.opt.service.api.install";
	private String serviceImplPath = "com.aec.opt.service.impl.install";
	private String controllerPath = "com.aec.opt.controller.install";
	private String mapperSufix = "Mapper";
	
	private String user = "account";
	private String password = "password";
	private  String dbName="dbName";
	private  String tableFilter = "act_";
	private String ipPort ="mysql.server:3306";
	private String srcPath = "/src/main/java/";
	private Connection con =null;
	private boolean util = true;
	private boolean sql = false;
	private String[] colnames;
	private String[] colTypes;
	private String[] colMarks;
	
	private static Map<String, String> commentByTableName;
	public CRUDGenerator() {
	}
	public CRUDGenerator(String ipHostPort,String userName,String password,String dbName) {
		this.ipPort=ipHostPort;
		this.user=userName;
		this.password=password;
		this.dbName=dbName;
	}

	private void initConnection() {
		try {
			Class.forName("com.mysql.jdbc.Driver");
			String url = "jdbc:mysql://"+ipPort+"/"+dbName+"?characterEncoding=UTF-8&useUnicode=true&zeroDateTimeBehavior=convertToNull";
			con = DriverManager.getConnection(url,
					user, password);
		} catch (ClassNotFoundException | SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void workDone(){
		if(con!=null){
			try {
				con.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void dowork(String tablename,String commentTable,String primaryKey) {
		try {
			PreparedStatement pStemt = con.prepareStatement("select * from " + tablename);
			ResultSetMetaData rsmd = pStemt.getMetaData();
			int size = rsmd.getColumnCount();
			colnames = new String[size];
			colTypes = new String[size];
			colMarks = new String[size];
			for (int i = 0; i < size; i++) {
				colnames[i] = rsmd.getColumnName(i + 1);
				colTypes[i] = rsmd.getColumnTypeName(i + 1);
				colMarks[i] = rsmd.getColumnLabel(i+1);
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
			buildMapperEntity(tablename,commentTable,primaryKey.toLowerCase());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	/**
	 * 获得某表的建表语句
	 * @param tableName
	 * @return
	 * @throws Exception
	 */
	public  Map<String,String> getCommentByTableName(List<String> tableName) throws Exception {
		Map<String,String> map = new HashMap<>();
		Statement stmt = con.createStatement();
		for (int i = 0; i < tableName.size(); i++) {
			String table = (String) tableName.get(i);
			ResultSet rs = stmt.executeQuery("SHOW CREATE TABLE " + table);
			
			if (rs != null && rs.next()) {
				String createDDL = rs.getString(2);
				String comment = parse(createDDL);
				map.put(table, comment);
				//正则匹配数据  
	            Pattern pattern = Pattern.compile("PRIMARY KEY \\(\\`(.*)\\`\\)");  
	            Matcher matcher =pattern.matcher(rs.getString(2));  
	            matcher.find();  
	            String data="";
				try {
					data = matcher.group();
					//过滤对于字符  
		            data=data.replaceAll("\\`|PRIMARY KEY \\(|\\)", "");  
		            //拆分字符  
		            map.put(table+"pk", data);
				} catch (Exception e) {
				}  
	            
			}
			rs.close();
		}
		stmt.close();
		return map;
	}
	
	/**
	 * 返回注释信息
	 * @param all
	 * @return
	 */
	
	public String parse(String all) {
		String comment = null;
		int index = all.indexOf("COMMENT='");
		if (index < 0) {
			return "";
		}
		comment = all.substring(index + 9);
		comment = comment.substring(0, comment.length() - 1);
		return comment;
	}

	private void buildMapperEntity(String tablename,String commentTable,String primaryKey) throws IOException {
		if(primaryKey.contains(",")){
			String pks[]=primaryKey.split(",");
			StringBuilder ids = new StringBuilder();
			StringBuilder idHandles = new StringBuilder();
			if(pks.length>1){
			 for(String pki:pks){
					  ids.append("String "+initAttrcap(pki)+", ");
					  idHandles.append(initAttrcap(pki)+", ");
			 }
			}
			String idsP=ids.substring(0, ids.lastIndexOf(", "));
			String pkhandle=idHandles.substring(0, idHandles.lastIndexOf(", "));
			
			EntityMysql entityMysql = new EntityMysql(entityPath,util,sql);
			entityMysql.setTableComment(commentTable);
			entityMysql.createEntity(colnames, colTypes,colMarks,tablename);
			
			EntityMysqlJavaMapper entityMysqlJavaMapper = new EntityMysqlJavaMapper(mapperPath,util,sql);
			entityMysqlJavaMapper.setEntityPath(entityPath);
			entityMysqlJavaMapper.setPrimaryKey(primaryKey);
			entityMysqlJavaMapper.createMapper(colnames, colTypes, tablename);
			
			XMLMapper xmlMapper = new XMLMapper(mapperPath);
			
			xmlMapper.setSurfix(mapperSufix);
			xmlMapper.setEntityPackage(entityPath);
			xmlMapper.setPrimaryKey(primaryKey);
			xmlMapper.createXml(colnames, colTypes, tablename);
			
			MysqlJavaService service = new MysqlJavaService(servicePath,util,sql);
			service.setEntityPath(entityPath);
			service.setPrimaryKey(pkhandle);
			service.setPrimaryKeys(idsP);
			service.createMapper(colnames, colTypes, tablename);

			MysqlJavaServiceImpl serviceImpl = new MysqlJavaServiceImpl(serviceImplPath,util,sql);
			serviceImpl.setMapperSufix(mapperSufix);
			serviceImpl.setEntityPath(entityPath);
			serviceImpl.setMapperPath(mapperPath);
			serviceImpl.setServicePath(servicePath);
			serviceImpl.setPrimaryKey(pkhandle);
			serviceImpl.setPrimaryKeys(idsP);
			serviceImpl.createServiceImpl(colnames, colTypes, tablename);
			
			
			MysqlJavaController controller = new MysqlJavaController(controllerPath,util,sql);
			controller.setServicePath(servicePath);
			controller.setEntityPath(entityPath);
			controller.setPrimaryKey(pkhandle);
			controller.setPrimaryKeys(idsP);
			controller.create(colnames, colTypes, tablename);
		}else{
			EntityMysql entityMysql = new EntityMysql(entityPath,util,sql);
			entityMysql.setTableComment(commentTable);
			entityMysql.createEntity(colnames, colTypes,colMarks,tablename);
			
			EntityMysqlJavaMapper entityMysqlJavaMapper = new EntityMysqlJavaMapper(mapperPath,util,sql);
			entityMysqlJavaMapper.setEntityPath(entityPath);
			entityMysqlJavaMapper.setPrimaryKey(primaryKey);
			entityMysqlJavaMapper.createMapper(colnames, colTypes, tablename);
			
			XMLMapper xmlMapper = new XMLMapper(mapperPath);
			
			xmlMapper.setSurfix(mapperSufix);
			xmlMapper.setEntityPackage(entityPath);
			xmlMapper.setPrimaryKey(primaryKey);
			xmlMapper.createXml(colnames, colTypes, tablename);
			
			MysqlJavaService service = new MysqlJavaService(servicePath,util,sql);
			service.setEntityPath(entityPath);
			service.setPrimaryKey(primaryKey);
			service.createMapper(colnames, colTypes, tablename);

			MysqlJavaServiceImpl serviceImpl = new MysqlJavaServiceImpl(serviceImplPath,util,sql);
			serviceImpl.setMapperSufix(mapperSufix);
			serviceImpl.setEntityPath(entityPath);
			serviceImpl.setMapperPath(mapperPath);
			serviceImpl.setServicePath(servicePath);
			serviceImpl.setPrimaryKey(primaryKey);
			serviceImpl.createServiceImpl(colnames, colTypes, tablename);
			
			
			MysqlJavaController controller = new MysqlJavaController(controllerPath,util,sql);
			controller.setServicePath(servicePath);
			controller.setEntityPath(entityPath);
			controller.setPrimaryKey(primaryKey);
			controller.create(colnames, colTypes, tablename);
		}
	}
	
	public String run() {
		Long start = Calendar.getInstance().getTimeInMillis();
		initConnection();
		 
		 mkGenDir();
		 List<String> readMySqlTable = readMySqlTable(con,dbName,tableFilter);
		 try {
			commentByTableName = getCommentByTableName(readMySqlTable);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
			for(String tablename:readMySqlTable){
				String commentTable = commentByTableName.get(tablename);
				String primaryKey =commentByTableName.get(tablename+"pk");
				 dowork(tablename,commentTable,primaryKey);
			 }
		workDone();
		Long end = Calendar.getInstance().getTimeInMillis();
		String seconds = "耗时"+(end-start)/1000+"秒";
		return seconds;
	}
	
	public String run(String tableName) {
		Long start = Calendar.getInstance().getTimeInMillis();
		initConnection();
		 
		 mkGenDir();
		 List<String> readMySqlTable = new ArrayList<>();
		 readMySqlTable.add(tableName);
		 try {
			commentByTableName = getCommentByTableName(readMySqlTable);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
			for(String tablename:readMySqlTable){
				String commentTable = commentByTableName.get(tablename);
				String primaryKey =commentByTableName.get(tablename+"pk");
				 dowork(tablename,commentTable,primaryKey);
			 }
		workDone();
		Long end = Calendar.getInstance().getTimeInMillis();
		String seconds = "耗时"+(end-start)/1000+"秒";
		return seconds;
	}

	private void mkGenDir() {
		mkdir(entityPath);
		mkdir(mapperPath);
		mkdir(servicePath);
		mkdir(serviceImplPath);
		mkdir(controllerPath);
	}
	public static void main(String args[]){
		 CRUDGenerator aecGenerator = new CRUDGenerator();
//		 aecGenerator.run();
		 aecGenerator.run("aec_address_limit");
	}

	private void mkdir(String packageOutPath2) {
		File directory = new File("");
		String packageDir = directory.getAbsolutePath() + srcPath
		 				+packageOutPath2.replace(".", File.separator);
		 File directoryA = new File(packageDir);
		 directoryA.mkdirs();
	}
	public String getEntityPath() {
		return entityPath;
	}
	public void setEntityPath(String entityPath) {
		this.entityPath = entityPath;
	}
	public String getMapperPath() {
		return mapperPath;
	}
	public void setMapperPath(String mapperPath) {
		this.mapperPath = mapperPath;
	}
	public String getServicePath() {
		return servicePath;
	}
	public void setServicePath(String servicePath) {
		this.servicePath = servicePath;
	}
	public String getServiceImplPath() {
		return serviceImplPath;
	}
	public void setServiceImplPath(String serviceImplPath) {
		this.serviceImplPath = serviceImplPath;
	}
	public String getControllerPath() {
		return controllerPath;
	}
	public void setControllerPath(String controllerPath) {
		this.controllerPath = controllerPath;
	}
	public String getMapperSufix() {
		return mapperSufix;
	}
	public void setMapperSufix(String mapperSufix) {
		this.mapperSufix = mapperSufix;
	}
	public String getUser() {
		return user;
	}
	public void setUser(String user) {
		this.user = user;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getDbName() {
		return dbName;
	}
	public void setDbName(String dbName) {
		this.dbName = dbName;
	}
	public String getTableFilter() {
		return tableFilter;
	}
	public void setTableFilter(String tableFilter) {
		this.tableFilter = tableFilter;
	}
	public String getIpPort() {
		return ipPort;
	}
	public void setIpPort(String ipPort) {
		this.ipPort = ipPort;
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
	public String getSrcPath() {
		return srcPath;
	}
	public void setSrcPath(String srcPath) {
		this.srcPath = srcPath;
	}


}

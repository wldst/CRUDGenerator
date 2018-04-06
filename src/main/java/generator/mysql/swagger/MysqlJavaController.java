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
import com.github.pagehelper.PageHelper;

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
public class MysqlJavaController {
	
	private String srcPath = "/src/main/java/";
	
	private String packageOutPath = "";
	private String entityPath = "";
	private String servicePath = "";
	private String mapperPath = "";
	private String mapperSufix = "";
	private String tablename = "";
	private String tableComments = "";
	private String[] colnames;
	private String[] colTypes;
	private String primaryKey;
	private String primaryKeys;
	private boolean util = false;
	private boolean sql = false;

	public MysqlJavaController() {
	}
	

	public MysqlJavaController(String packageOutPath, boolean util, boolean sql) {
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
			create(colnames, colTypes,tablename);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void create(String[] colnames, String[] colTypes,String tablename) throws IOException {
		this.colnames=colnames;
		this.colTypes=colTypes;
		this.tablename=tablename;
		String content = parse(colnames, colTypes,tablename);
		File directory = new File("");
		String fileName = directory.getAbsolutePath() + getSrcPath()
				+packageOutPath.replace(".", File.separator)+File.separator+ initcap(tablename.toLowerCase()) + "Controller.java";
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
		sb.append("import org.springframework.web.bind.annotation.RequestMapping;\n");
		sb.append("import org.springframework.web.bind.annotation.RequestBody;\n");
		sb.append("import org.springframework.web.bind.annotation.RestController;\n");
		sb.append("import org.springframework.beans.factory.annotation.Autowired;\n");
		sb.append("import org.springframework.web.bind.annotation.RequestMethod;\n");
		sb.append("import org.springframework.web.bind.annotation.RequestParam;\n");
		sb.append("import org.springframework.web.client.RestTemplate;\n");
		sb.append("import com.github.pagehelper.PageHelper;\n");
		sb.append("import com.github.pagehelper.PageInfo;\n");
		sb.append("import io.swagger.annotations.Api;\n");
		sb.append("import io.swagger.annotations.ApiImplicitParam;\n");
		sb.append("import io.swagger.annotations.ApiImplicitParams;\n");
		sb.append("import io.swagger.annotations.ApiOperation;\n");

		sb.append("import cn.com.wldst.config.MessageInfo;\n");
		sb.append("import "+getServicePath()+".I"+DbTransUtil.initcap(tablename.toLowerCase())+"Service;\n");
		sb.append("import java.util.List;\n");
		sb.append("import java.util.Date;\n");
		sb.append("import org.springframework.beans.factory.annotation.Autowired;\n");
		
		

		
		if (util) {
			sb.append("import java.util.Date;\n");
		}
		if (sql) {
			sb.append("import java.sql.*;\n");
		}	
		sb.append("\t@RestController\n@RequestMapping(\"/"+DbTransUtil.initAttrcap(tablename.toLowerCase())+"\")\n");
		sb.append("\t@Api(value = \""+DbTransUtil.initAttrcap(tablename.toLowerCase())+"\", tags = \""+getTableComments()+"\")\n");
		
		sb.append("public class " + DbTransUtil.initcap(tablename.toLowerCase()) + "Controller{\n");
		sb.append("\t@Autowired\n");
		sb.append("\tprivate I"+DbTransUtil.initcap(tablename.toLowerCase())+"Service "+DbTransUtil.initAttrcap(tablename.toLowerCase())+"Service;\n");
		
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
			StringBuilder ids=new StringBuilder();
			StringBuilder idsAno=new StringBuilder();
			for(String pki:primaryKey.split(",")){
				ids.append("@RequestParam(value = \""+pki+"\", required = true) String "+pki+", ");
				idsAno.append("\t\t@ApiImplicitParam(name = \""+pki+"\", value = \""+getTableComments()+"主键"+pki+"\", required = true, dataType = \"String\", paramType = \"query\"),\n\t");
			  }
			String idsP=ids.substring(0, ids.lastIndexOf(", "));
			String idAnoP=idsAno.substring(0, idsAno.lastIndexOf(",\n\t"));
			
			 sb.append("\t@ApiOperation(value = \""+getTableComments()+"根据"+primaryKey+"查找\", notes = \""+getTableComments()+"根据Id查找\")\n");
			  sb.append("\t@RequestMapping(value = \"/findById\",method = {RequestMethod.GET})\n");
			  sb.append("\t@ApiImplicitParams({\n");
			  sb.append(idAnoP.toString()+"})\n\t");
			  sb.append("\tpublic MessageInfo findById(");
			  sb.append(idsP);
			  sb.append("){\n ");		  
			  sb.append("\t\t "+DbTransUtil.initcap(tablename.toLowerCase())+" data="+DbTransUtil.initAttrcap(tablename.toLowerCase())+"Service.findById("+primaryKey+");\n");
			  sb.append("\t\treturn new MessageInfo(MessageInfo.getOk(), data);\n");
			  sb.append("\t}\n\n");
			  
			  sb.append("\t@ApiOperation(value = \""+getTableComments()+"保存\", notes = \""+getTableComments()+"保存\")\n");
			  sb.append("\t@RequestMapping(value = \"/save\",method = {RequestMethod.PUT})\n");
			  sb.append("\tpublic int save(");
			  sb.append("@RequestBody "+DbTransUtil.initcap(tablename.toLowerCase())+" "+DbTransUtil.initAttrcap(tablename.toLowerCase()));
			  sb.append("){\n ");		  
			  sb.append("\t\treturn "+DbTransUtil.initAttrcap(tablename.toLowerCase())+"Service.save("+DbTransUtil.initAttrcap(tablename.toLowerCase())+");\n");
			  sb.append("\t}\n\n");
			  
			  sb.append("\t@ApiOperation(value = \""+getTableComments()+"查询\", notes = \""+getTableComments()+"查询\")\n");
			  sb.append("\t@RequestMapping(value = \"/findBy\",method = {RequestMethod.GET})\n");
			  sb.append("\tpublic MessageInfo findBy(");
			  sb.append(DbTransUtil.initcap(tablename.toLowerCase())+" "+DbTransUtil.initAttrcap(tablename.toLowerCase())+",");
			  sb.append("@RequestParam(value = \"pageNum\", required = false, defaultValue = \"1\") Integer pageNum,");
			  sb.append("@RequestParam(value = \"pageSize\", required = false, defaultValue = \"10\") Integer pageSize");
			  sb.append("){\n ");
			  sb.append("\t\tPageHelper.startPage(pageNum, pageSize);\n");
			  sb.append("\t\t List<"+DbTransUtil.initcap(tablename.toLowerCase())+"> list= "+DbTransUtil.initAttrcap(tablename.toLowerCase())+"Service.findBy("+DbTransUtil.initAttrcap(tablename.toLowerCase())+");\n");
			  sb.append("\t\tPageInfo<"+DbTransUtil.initcap(tablename.toLowerCase())+"> page = new PageInfo<"+DbTransUtil.initcap(tablename.toLowerCase())+">(list);\n");
			  sb.append("\t\treturn new MessageInfo(MessageInfo.getOk(), page);\n");
			  sb.append("\t}\n\n");
			  
			  sb.append("\t@ApiOperation(value = \""+getTableComments()+"修改\", notes = \""+getTableComments()+"修改\")\n");
			  sb.append("\t@RequestMapping(value = \"/update\",method = {RequestMethod.POST})\n");
			  sb.append("\tpublic MessageInfo update(");
			  sb.append("@RequestBody "+DbTransUtil.initcap(tablename.toLowerCase())+" "+DbTransUtil.initAttrcap(tablename.toLowerCase()));  
			  sb.append(" ){\n ");		  
			  sb.append("\t\t"+DbTransUtil.initAttrcap(tablename.toLowerCase())+"Service.update("+DbTransUtil.initAttrcap(tablename.toLowerCase())+");\n");
			  sb.append("\t\treturn new MessageInfo(MessageInfo.getOk(), \"更新操作成功\");\n");
			  sb.append("\t}\n\n");
			  
			  sb.append("\t@ApiOperation(value = \""+getTableComments()+"删除\", notes = \""+getTableComments()+"删除\")\n");
			  sb.append("\t@RequestMapping(value = \"/delete\",method = {RequestMethod.DELETE})\n");
			  sb.append("\t@ApiImplicitParams({\n");
			  sb.append(idAnoP.toString()+"})\n\t");
			  sb.append("\tpublic MessageInfo deleteByPrimaryKey(");
			  sb.append(idsP);
			  sb.append("){\n ");	  
			  sb.append("\t\t int deleteById = "+DbTransUtil.initAttrcap(tablename.toLowerCase())+"Service.deleteById("+primaryKey+");\n");
			  sb.append("\tif(deleteById<=0){");
			  sb.append("\treturn new MessageInfo(MessageInfo.getError(), \"删除发生异常\");");
			  
			  sb.append("\t}\n");
				  sb.append("\t\treturn new MessageInfo(MessageInfo.getOk(), \"删除操作成功\");\n");
			  sb.append("\t}\n\n");
		}else{
			 sb.append("\t@ApiOperation(value = \""+getTableComments()+"根据Id查找\", notes = \""+getTableComments()+"根据Id查找\")\n");
			  sb.append("\t@RequestMapping(value = \"/findById\",method = {RequestMethod.GET})\n");
			  sb.append("\t@ApiImplicitParams({\n");
			  sb.append("\t\t@ApiImplicitParam(name = \"id\", value = \""+getTableComments()+"ID\", required = true, dataType = \"String\", paramType = \"query\")\n\t})\n");
			  sb.append("\tpublic MessageInfo findById(");
			  sb.append("@RequestParam(value = \"id\", required = true) String id");
			  sb.append("){\n ");		  
			  sb.append("\t\t "+DbTransUtil.initcap(tablename.toLowerCase())+" data="+DbTransUtil.initAttrcap(tablename.toLowerCase())+"Service.findById(id);\n");
			  sb.append("\t\treturn new MessageInfo(MessageInfo.getOk(), data);\n");
			  sb.append("\t}\n\n");
			  
			  sb.append("\t@ApiOperation(value = \""+getTableComments()+"保存\", notes = \""+getTableComments()+"保存\")\n");
			  sb.append("\t@RequestMapping(value = \"/save\",method = {RequestMethod.PUT})\n");
			  sb.append("\tpublic int save(");
			  sb.append("@RequestBody "+DbTransUtil.initcap(tablename.toLowerCase())+" "+DbTransUtil.initAttrcap(tablename.toLowerCase()));
			  sb.append("){\n ");		  
			  sb.append("\t\treturn "+DbTransUtil.initAttrcap(tablename.toLowerCase())+"Service.save("+DbTransUtil.initAttrcap(tablename.toLowerCase())+");\n");
			  sb.append("\t}\n\n");
			  
			  sb.append("\t@ApiOperation(value = \""+getTableComments()+"查询\", notes = \""+getTableComments()+"查询\")\n");
			  sb.append("\t@RequestMapping(value = \"/findBy\",method = {RequestMethod.GET})\n");
			  sb.append("\tpublic MessageInfo findBy(");
			  sb.append(DbTransUtil.initcap(tablename.toLowerCase())+" "+DbTransUtil.initAttrcap(tablename.toLowerCase())+",");
			  sb.append("@RequestParam(value = \"pageNum\", required = false, defaultValue = \"1\") Integer pageNum,");
			  sb.append("@RequestParam(value = \"pageSize\", required = false, defaultValue = \"10\") Integer pageSize");
			  sb.append("){\n ");
			  sb.append("\t\tPageHelper.startPage(pageNum, pageSize);\n");
			  sb.append("\t\t List<"+DbTransUtil.initcap(tablename.toLowerCase())+"> list= "+DbTransUtil.initAttrcap(tablename.toLowerCase())+"Service.findBy("+DbTransUtil.initAttrcap(tablename.toLowerCase())+");\n");
			  sb.append("\t\tPageInfo<"+DbTransUtil.initcap(tablename.toLowerCase())+"> page = new PageInfo<"+DbTransUtil.initcap(tablename.toLowerCase())+">(list);\n");
			  sb.append("\t\treturn new MessageInfo(MessageInfo.getOk(), page);\n");
			  sb.append("\t}\n\n");
			  
			  sb.append("\t@ApiOperation(value = \""+getTableComments()+"修改\", notes = \""+getTableComments()+"修改\")\n");
			  sb.append("\t@RequestMapping(value = \"/update\",method = {RequestMethod.POST})\n");
			  sb.append("\tpublic MessageInfo update(");
			  sb.append("@RequestBody "+DbTransUtil.initcap(tablename.toLowerCase())+" "+DbTransUtil.initAttrcap(tablename.toLowerCase()));  
			  sb.append(" ){\n ");		  
			  sb.append("\t\t"+DbTransUtil.initAttrcap(tablename.toLowerCase())+"Service.update("+DbTransUtil.initAttrcap(tablename.toLowerCase())+");\n");
			  sb.append("\t\treturn new MessageInfo(MessageInfo.getOk(), \"更新操作成功\");\n");
			  sb.append("\t}\n\n");
			  
			  sb.append("\t@ApiOperation(value = \""+getTableComments()+"删除\", notes = \""+getTableComments()+"删除\")\n");
			  sb.append("\t@RequestMapping(value = \"/delete\",method = {RequestMethod.DELETE})\n");
			  sb.append("\t@ApiImplicitParams({\n");
			  sb.append("\t\t@ApiImplicitParam(name = \"id\", value = \""+getTableComments()+"id\", required = true, dataType = \"String\", paramType = \"query\")\n\t})\n");
			  sb.append("\tpublic MessageInfo deleteById(");
			  sb.append("@RequestParam(value = \"id\", required = true) String id");
			  sb.append("){\n ");	  
			  sb.append(" String[] split = id.split(\",\");\n ");
			  sb.append("for(String idi:split){ ");	  
			  sb.append("\t\t int deleteById = "+DbTransUtil.initAttrcap(tablename.toLowerCase())+"Service.deleteById(idi);\n");
			  sb.append("\tif(deleteById<=0){");
			  sb.append("\treturn new MessageInfo(MessageInfo.getError(), \"删除发生异常\");");
			  
			  sb.append("\t}\n\t\t}");
				  sb.append("\t\treturn new MessageInfo(MessageInfo.getOk(), \"删除操作成功\");\n");
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


	public String getServicePath() {
		return servicePath;
	}


	public void setServicePath(String servicePath) {
		this.servicePath = servicePath;
	}


	public String getTableComments() {
		return tableComments;
	}


	public void setTableComments(String tableComments) {
		this.tableComments = tableComments;
	}


	public String getSrcPath() {
		return srcPath;
	}


	public void setSrcPath(String srcPath) {
		this.srcPath = srcPath;
	}


	public String getPrimaryKey() {
		return primaryKey;
	}


	public void setPrimaryKey(String primaryKey) {
		this.primaryKey = primaryKey;
	}


	public String getPrimaryKeys() {
		return primaryKeys;
	}


	public void setPrimaryKeys(String primaryKeys) {
		this.primaryKeys = primaryKeys;
	}


	public static void main(String[] args) {
		Long start = Calendar.getInstance().getTimeInMillis();
		
		new MysqlJavaController();
		Long end = Calendar.getInstance().getTimeInMillis();
System.out.println("耗时"+(end-start)/1000+"秒");
	}
}

# CRUDGenerator
基于MYSQL,SpringBoot,Swagger的代码生成器。根据数据库表的metadata生成从mapper,Service,Controller的代码。
可在Swagger中浏览接口。

使用方法：
配置属性
	private String entityPath = "com.aec.entitys.aec";
	private String mapperPath = "com.aec.opt.mapper.install";
	private String servicePath = "com.aec.opt.service.api.install";
	private String serviceImplPath = "com.aec.opt.service.impl.install";
	private String controllerPath = "com.aec.opt.controller.install";
	private String mapperSufix = "Mapper";
	
	private String user = "account";
	private String password = "password";
	private  String dbName="db_name";
	private  String tableFilter = "act_";
	private String ipPort ="mysql.server.com:3306";
	private String srcPath = "/src/main/java/";


 CRUDGenerator aecGenerator = new CRUDGenerator();
//		 aecGenerator.run();1全库生成相关的代码。
		 aecGenerator.run("aec_address_limit");//2单表生成CURD的相关方法。

简化程序员的基础编码时间。



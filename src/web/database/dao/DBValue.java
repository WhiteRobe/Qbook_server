package web.database.dao;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public final class DBValue {
	/**
	 * 封装了数据库的值
	 * 1.数据库驱动
	 * 2.数据库链接
	 * 3.登陆名
	 * 4.登录密码
	 */
	public String DRIVER;
	public String URL;
	public String DBADMIN;
	public String DBPW;

	public DBValue(){}

	/**
	 * 字符串复制
	 * @param driver 驱动
	 * @param url 链接
	 * @param dbadmin 管理员名
	 * @param dbpw 管理员密码
	 */
	public DBValue(String driver,String url,String dbadmin,String dbpw){
		setValue(driver,url,dbadmin,dbpw);
	}

	/**
	 * 从配置文件初始化
	 * @param p 配置文件
	 */
	public DBValue(Properties p){
		setValue(p);
	}

//	/**
//	 * 从指定路径的配置文件初始化值
//	 * @param propertiesURL 配置文件路径
//	 */
//	public DBValue(String propertiesURL) throws java.io.IOException{
//		setValue(propertiesURL);
//	}

	/**
	 * 从默认配置文件路径初始化DBValue
	 */
	public void initDBValueDefault() throws java.io.IOException {
		Properties p = new Properties();
		File file = new File(this.getClassPath()+"DBValue.properties");
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8));
		p.load(reader);
		setValue(p);
	}

	/**
	 * 从指定配置文件初始化值
	 * @param p 配置文件
	 */
	public void setValue(Properties p){
		this.DRIVER = p.getProperty("DRIVER");
		this.URL = p.getProperty("URL");
		this.DBADMIN = p.getProperty("DBADMIN");
		this.DBPW = p.getProperty("DBPW");
	}

//	/**
//	 * 从指定路径的配置文件初始化值
//	 * @param propertiesURL 配置文件路径
//	 */
//	public void setValue(String propertiesURL) throws java.io.IOException{
//		setValue(OriEncodingFilter.getProperties(propertiesURL));
//	}

	/**
	 * 从指定配置文件初始化值
	 * @param driver 数据库驱动
	 * @param url 数据库链接
	 * @param dbadmin 登陆名
	 * @param dbpw 登陆密码
	 */
	public void setValue(String driver,String url,String dbadmin,String dbpw){
		this.DRIVER = driver;
		this.URL = url;
		this.DBADMIN = dbadmin;
		this.DBPW = dbpw;
	}

	/**
	 * 获取类的路径
	 * @return 类的路径
	 */
	public String getClassPath(){
		return this.getClass().getResource("").toString().replaceAll("%20"," ").replace("file:/","");
	}
}

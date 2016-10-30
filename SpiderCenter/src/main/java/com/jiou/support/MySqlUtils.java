package com.jiou.support;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MySqlUtils {
	protected static Logger logger = LoggerFactory.getLogger(MySqlUtils.class);
	
	private static String fileName="/config.properties";//这里是指放在classes下，如果有包的话，前面加包名即可。例：/com/web/db.properties  
    private static String url = "";  
    private static String username ="";  
    private static String password = "";  
    static Connection  conn=null;  
    
  
    public static  Connection  getConn(String dataSource){  
        Properties p = new Properties();  
        try {  
            InputStream in = MySqlUtils.class.getResourceAsStream(fileName);//这里有人用new FileInputStream(fileName),不过这种方式找不到配置文件。有人说是在classes下，我调过了，不行。  
            p.load(in);  
            in.close();  
            if(p.containsKey("jdbc.url"+dataSource)){  
                url = p.getProperty("jdbc.url"+dataSource);  
            }  
            if(p.containsKey("jdbc.user"+dataSource)){  
                username = p.getProperty("jdbc.user"+dataSource);  
            }  
            if(p.containsKey("jdbc.password"+dataSource)){  
                password = p.getProperty("jdbc.password"+dataSource);  
            }  
        } catch (IOException ex) {  
        	ex.printStackTrace();  
        	logger.error("获取sql配置文件信息异常", ex);
        }  
        
      
        try {
				conn = DriverManager.getConnection(url,username,password);
			} catch (SQLException e) {
				e.printStackTrace();
				logger.error("获取连接异常", e);
			}  
       
        return conn;  
    }  
    
    public static void main(String[] args) {
		System.out.println(MySqlUtils.getConn(".test"));
	}

}

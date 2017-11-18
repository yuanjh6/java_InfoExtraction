package MyClass;

import java.io.IOException;
import java.sql.Connection;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import jeasy.analysis.MMAnalyzer;

public class MDataBase {
	private static Logger logger = LogManager.getLogger(MDataBase.class
			.getName());

	private String driver;
	private String url;
	private Connection conn;

	/**
	 * @param args
	 * @return 
	 * @throws ClassNotFoundException
	 *             驱动类名：com.mysql.jdbc.Driver
	 *             URL格式：jdbc:mysql://servername:port/database
	 * @throws SQLException
	 */

	public MDataBase(String dbDriver, String dbUrl)
			throws ClassNotFoundException, SQLException {
		if (!checkStr(dbDriver) || !checkStr(dbUrl)) {
			logger.error("传入参数不合法");
		}
		driver = dbDriver.trim();
		url = dbUrl.trim();

		Class.forName(driver);
		conn = DriverManager.getConnection(url);
	}

	public void close() throws SQLException {
		this.conn.close();
	}

	/*
	 * 将Map转化为string返回的字符数组格式为
	 * s[0]:key1,key2,key3
	 * s[1]:"value1","value2","value3"
	 */
	public String[] mapToStr(Map<String, String> map) {
		String[] tmpret = new String[2];
		if (map != null) {
			Set<Map.Entry<String, String>> entrySet = map.entrySet();
			String tmp1 = "", tmp2 = "";
			for (Map.Entry<String, String> entry : entrySet) {
				tmp1 += "," + entry.getKey();
				tmp2 += "," + "\"" + entry.getValue() + "\"";
			}
			tmp1 = tmp1.substring(1);
			tmp2 = tmp2.substring(1);
			tmpret[0] = tmp1;
			tmpret[1] = tmp2;
		} else {
			System.out.println("ERROR:DataBase.mapToStr map 空指针");
		}
		return tmpret;
	}
	/**
	 * @return the conn
	 */
	public Connection getConn() {
		return conn;
	}

	public ArrayList<Map<String, String>> RSToMap(ResultSet rs)
			throws SQLException {
		ArrayList<Map<String, String>> tmpret = new ArrayList<Map<String, String>>();

		ResultSetMetaData rsMeta = rs.getMetaData();
		int b = rsMeta.getColumnCount();
		String[] fieldList = new String[b];
		for (int i = 1; i <= b; i++) {
			fieldList[i - 1] = rsMeta.getColumnName(i);
		}

		while (rs.next()) {
			Map<String, String> map = new HashMap<String, String>();
			for (String field : fieldList) {
				map.put(field, rs.getString(field));
			}
			tmpret.add(map);
		}
		return tmpret;
	}

	private  boolean checkStr(String str) {
		if (str == null)
			return false;
		str = str.trim();
		if (str.length() <= 0)
			return false;
		return true;
	}
	
	/*
	 * 对数据库进行分词的方法
	 */
	public void JeAnalysis(String tableName,String fieldStr) throws SQLException, IOException{
		if(!checkStr(tableName)||!checkStr(fieldStr)){
			logger.error("parameters illeage");
			return;
		}
		MMAnalyzer analyzer=new MMAnalyzer();
		String[] fieldArr=fieldStr.split(",");
		Statement sta=this.conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,ResultSet.CONCUR_UPDATABLE); 
		String sql="select id,"+fieldStr+" from "+tableName;
		ResultSet rs=sta.executeQuery(sql);
		while(rs.next()){
			for(String s:fieldArr){
				if(!checkStr(s)){
					logger.warn("fieldStr contains empty fieldname after split,skip");
					continue;
				}
				String tmps=rs.getString(s);
				if(checkStr(tmps)){
					rs.updateString(s,analyzer.segment(tmps,","));
				}
				
			}
			rs.updateRow();
		}
		rs.close();
		sta.close();
	}

}


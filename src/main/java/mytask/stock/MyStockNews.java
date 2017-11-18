package mytask.stock;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;



import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class MyStockNews {
	private static Logger logger = LogManager.getLogger(MyStockNews.class
			.getName());
	static String news_table = "mystock_news";
	static String stockIdField = "mystock_stockinfo,stockid";
	static String dbDriver = "com.mysql.jdbc.Driver";
	static String dbUrl = "jdbc:mysql://localhost:3306/mysite?useUnicode=true&characterEncoding=utf8&user=root&password=";
//	static final String dbUrl ="jdbc:mysql://sql206.byetcluster.com:3306/b18_11341106_84205?useUnicode=true&characterEncoding=utf8&user=b18_11341106&password=hahaaa";
	public static void main(String[] args) {
		// test part
		// String html=null;
		// html =
		// HtmlUnitTool.getPageAsXml("http://cpro.baidu.com/cpro/ui/uijs.php?rs=4&u=http%3A%2F%2Fwww%2Eyingjiesheng%2Ecom%2Fcommend-fulltime-1%2Ehtml&p=baidu&c=news&n=10&t=tpclicked3_hc&q=yingjiesheng_cpr&k0=&k1=&k2=&k3=&k4=&k5=&sid=93904c6f62e5058f&ch=0&tu=u1168282&jk=2995dec35a0c06d4&cf=4&fv=11&stid=0&urlid=0&k=%E5%BA%94%E5%B1%8A%E7%94%9F");
		// System.out.println( Jsoup.parse(html).text());
		// String eleSelect = "div.adBlockL";
		// Map<String,String> attrMap=new HashMap<String,String>();
		// attrMap.put("pic", "div.adBlockLMulti>a>img,attr,src");
		// attrMap.put("txt", "div.adBlockLInfo>div>a,text");
		// attrMap.put("turl", "div.adBlockLInfo>div.adBlockLSurl>a,text");
		// // attrMap.put("info", "div.aspdesc,text");
		// List<Map<String, String>> results = new ArrayList<Map<String,
		// String>>();
		// results = JsoupTool.itemsInfo(html, eleSelect, attrMap);
		// eleSelect ="div.asp>div.aspblock";
		// attrMap.clear();
		// attrMap.put("txt", "a.asptit,text");
		// attrMap.put("turl", "a.aspurl,text");
		// List<Map<String, String>> result=JsoupTool.itemsInfo(html, eleSelect,
		// attrMap);
		// if(result!=null){
		// results.addAll(result);
		// }
		// for(Map<String,String> map:results){
		// System.out.println(map);
		// }

		try {
			Class.forName(dbDriver);
			Connection conn = DriverManager.getConnection(dbUrl);
			Statement sta = conn.createStatement();

			// 需要获取股票列表
			String[] tstockIdField = stockIdField.split(",", 2);
			if (tstockIdField.length < 2) {
				logger.error("无法获取股票id列表");
				return;
			}
			String tableName = tstockIdField[0].trim();
			String tableField = tstockIdField[1].trim();
			String sql = "select " + tableField + " from " + tableName;

			ResultSet rs = sta.executeQuery(sql);
			Set<String> stockIdSet = new HashSet<String>();
			while (rs.next()) {
				stockIdSet.add(rs.getString(tableField));
			}

			// 得到所有新闻数据
			for (String stockId : stockIdSet) {
				if (stockId == null || stockId.isEmpty())
					continue;
				stockId = stockId.trim();
				List<Map<String, String>> result = getNews(stockId);
				if (result == null || result.isEmpty())
					continue;
				for (Map<String, String> item : result) {
					if (item == null || item.isEmpty())
						continue;
					item.put("stockid", stockId);
					System.out.println(item);

					String[] s = mapToStr(item);
					if (s == null)
						continue;
					sql = "insert into " + news_table + "(" + s[0]
							+ ") values " + "(" + s[1] + ");";
					try {
						if (sta.executeUpdate(sql) == 0) {
							logger.debug("sql语句执行失败:" + sql);
						}
					} catch (SQLException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			}

			sta.close();
			conn.close();
		} catch (ClassNotFoundException | SQLException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
			logger.error("数据库连接出错，请检查");
		}
		logger.info("ok!");
	}

	public static List<Map<String, String>> getNews(String stockId) {
		String url = null;
		String html = null;
		String encode = null;
		String eleSelect;
		Map<String, String> attrMap;
		List<Map<String, String>> results = new ArrayList<Map<String, String>>();
		List<Map<String, String>> result;
		attrMap = new HashMap<String, String>();

		// 搜狐财经
		url = "http://q.stock.sohu.com/news/cn/#TMP#/#SSTOCKID#/cmp_news.shtml";
		url = url.replace("#TMP#", stockId.subSequence(5, 8));
		url = buildStockUrl(url, stockId);
		if (url == null) {
			return null;
		}
		encode = "gb2312";
		html = HttpClientTool.getMethod(url, encode);
		eleSelect = "div.newslist>ul>li";
		attrMap.clear();
		attrMap.put("title", "a,text");
		attrMap.put("url", "a,attr,href");
		attrMap.put("pubdate", "span,text");
		result = JsoupTool.itemsInfo(html, eleSelect, attrMap);
		if (result != null) {
			for (Map<String, String> item : result) {
				item.put("url", "http://q.stock.sohu.com/" + item.get("url"));
				item.put("site", "sohu");
				if (item.containsKey("pubdate")) {
					if (item.get("pubdate") != null
							&& item.get("pubdate").trim().length() >= 12){
						item.put("pubdate", item.get("pubdate").trim()
								.substring(1, 10));
				}
					}
			}
			// 最新的放下面
			Collections.reverse(result);
			results.addAll(result);
		}

		// 和讯财经
		 url = "http://stockdata.stock.hexun.com/ggzx/#SSTOCKID#_1_1.shtml";
		 url = buildStockUrl(url, stockId);
		 if (url == null) {
		 return null;
		 }
		 encode = "gb2312";
		 html = HttpClientTool.getMethod(url, encode);
		 eleSelect = "div.wrap_conL>ul.list_news>li";
		 attrMap.clear();
		 attrMap.put("title", "a[title],text");
		 attrMap.put("url", "a[title],attr,href");
		 attrMap.put("pubdate", "span,text");
		 result = JsoupTool.itemsInfo(html, eleSelect, attrMap);
		 if (result != null) {
		 for (Map<String, String> item : result) {
		 item.put("site", "hexun");
		 if(!item.containsKey("title"))continue;
		 item.put("title", item.get("title").replace("/n", "").trim());
		 }
			Collections.reverse(result);
		 results.addAll(result);
		 }

		// 新浪财经
		 url =
		 "http://vip.stock.finance.sina.com.cn/corp/go.php/vCB_AllNewsStock/symbol/#STOCKID#.phtml";
		 url = buildStockUrl(url, stockId);
		 if (url == null) {
		 return null;
		 }
		 encode = "gb2312";
		 html = HttpClientTool.getMethod(url, encode);
		 eleSelect = "div.datelist>ul>a";
		 attrMap.clear();
		 attrMap.put("title", "a,text");
		 attrMap.put("url", "a,attr,href");
		 
		 SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");//设置日期格式
		 attrMap.put("pubdate", df.format(new Date()));
		 result = JsoupTool.itemsInfo(html, eleSelect, attrMap);
		 if (result != null) {
		 for (Map<String, String> item : result) {
		 item.put("site", "sina");
		 }
			Collections.reverse(result);
		 results.addAll(result);
		 }

		return results;

	}

	private static String[] mapToStr(Map<String, String> map) {
		String[] tmpret = new String[2];
		if (map == null) {
			logger.error("参数Map为空指针");
			return null;
		}

		Set<Map.Entry<String, String>> entrySet = map.entrySet();
		String tmp1 = "", tmp2 = "";
		for (Map.Entry<String, String> entry : entrySet) {

			if (entry.getKey().trim().length() == 0 || entry.getValue() == null
					|| entry.getValue().trim().length() == 0)
				continue;
			tmp1 += "," + entry.getKey();
			tmp2 += "," + "\"" + entry.getValue().replace("\"", "'") + "\"";
		}
		if (tmp1.length() < 1 || tmp2.length() < 1)
			return null;

		tmpret[0] = tmp1.substring(1);
		tmpret[1] = tmp2.substring(1);

		return tmpret;
	}

	private static String buildStockUrl(String url, String stockId) {
		if (url == null || url.isEmpty() || stockId == null
				|| stockId.isEmpty()) {
			return null;
		}
		stockId = stockId.trim();
		if (url.contains("#STOCKID#")) {
			return url.replace("#STOCKID#", stockId);
		} else if (url.contains("#SSTOCKID#")) {
			return url.replace("#SSTOCKID#", stockId.subSequence(2, 8));
		}
		return null;
	}
}

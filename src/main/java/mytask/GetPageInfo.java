package mytask;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Whitelist;
import org.jsoup.select.Elements;

import proxy.MProxy;
import MyClass.HttpClientTool;

public class GetPageInfo {
	static final String dbUrl = "jdbc:mysql://localhost:3306/adver?useUnicode=true&characterEncoding=utf8&user=root&password="; // 数据库信息
	static final String dbDriver = "com.mysql.jdbc.Driver";
	static final String tableName = "siteinfo_13w"; // 数据表名称
	static final String urlField="turl";
	static final String keyField = "id"; // 数据表主键，用于对数据表进行修改操作
	static final int timeoutMillis=5000;
	static MProxy mproxy;

	public static void main(String[] args) {
		System.out.println("start!");
		GetPageInfo test = new GetPageInfo();
		mproxy = new MProxy();
//		System.out.println(test.urlInfo("http://www.lvse.com/site/kdslife-com-3465.html"));
//
		try {
			test.run();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void run() throws ClassNotFoundException {
		Class.forName(dbDriver);
		Connection conn=null;
		ResultSet rs = null;
		try {
			conn = DriverManager.getConnection(dbUrl);

			Statement sta = conn
					.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,
							ResultSet.CONCUR_UPDATABLE);
			String sql = "select * from " + tableName + " where STATUS IS NULL order by id";
			System.out.println(sql);
			rs = sta.executeQuery(sql);

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		}
		if (rs == null) {
			System.out.println("结果集合获取失败");
			return;
		}

		Map<String, String> tmap = new HashMap<String, String>();

		while (true) {
			try {
				if (rs.next() == false) {
					break;
				}

				// 获取所在地址locurl的相关信息
				tmap = this.urlInfo(rs.getString(urlField));
				if (tmap == null)
					continue;
				
				if (tmap.containsKey("detail")) {
					tmap.put("detail", tmap.get("detail").replace("/n", ""));
					if(tmap.get("detail").length()>2048)tmap.put("detail",tmap.get("detail").substring(0, 2047));
					
					rs.updateString("TDETAIL", tmap.get("detail"));
				}
				if(tmap.containsKey("keywords")){
					rs.updateString("TKEYWORDS", tmap.get("keywords"));
				}
				
				if(tmap.containsKey("description")){
					rs.updateString("TDESCRIPTION", tmap.get("description"));
				}
				if(tmap.containsKey("title")){
					rs.updateString("TTITLE", tmap.get("title"));
				}
			
				rs.updateInt("STATUS", 1);
				System.out.println("upate id:"+rs.getString("id"));
				
				rs.updateRow();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
		try {
		
			rs.close();
			conn.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("ok!");
	}

	/**
	 * 功能：对url属性进行扩展 返回:url 分词,url domain，title,keywords,description
	 */
	public Map<String, String> urlInfo(String url) {
		if(url==null)return null;
		if (!url.startsWith("http://")) {
			url = "http://" + url;
		}
		Map<String, String> tmap = new HashMap<String, String>();
		// 获取网页内容
		String proxy = mproxy.getNext();
		String html = HttpClientTool.getMethod(url, "null");
		if (html == null) {
			mproxy.addFail(proxy);
			return null;
		}

		// jsoup解析网页

		Document doc =null;

			doc = Jsoup.parse(html);
		
		
		if(doc==null)return null;
//		System.out.println(html);
		Element ele;
		Elements eles;

		eles = doc.select("div#detail_all");
		String detail= "";
		for (Element e : eles) {
			if (e != null && e.text() != null) {
				detail += "," + e.text().trim();
			}
		}
		if (detail.length() > 0)
			tmap.put("detail", Jsoup.clean(detail.substring(1),Whitelist.none()));
		
		ele=doc.select("meta[name=keywords]").first();
		if(ele!=null)tmap.put("keywords", ele.attr("content").toString());
		
		ele=doc.select("meta[name=description]").first();
		if(ele!=null)tmap.put("description", ele.attr("content").toString());
		
		ele=doc.select("title").first();
		if(ele!=null)tmap.put("title", ele.text());
		
//		eles = doc.select("div.url_list>div>a");
//		String relate = "";
//		for (Element e : eles) {
//			if (e != null && e.text() != null) {
//				relate += "," + e.text().trim();
//			}
//		}
//		if (relate.length() > 0)
//			tmap.put("relate", relate.substring(1));
//
//		eles = doc.select("div.info_desc>div>div.cont>p");
//		String info = "";
//		for (Element e : eles) {
//			if (e != null && e.text() != null) {
//				info += "," + e.text().trim();
//			}
//		}
//		if (info.length() > 0)
//			tmap.put("info", info.substring(1));

		return tmap;

	}

	public static String urlFormat(String url) {

		if (url == null || url.length() == 0)
			return null;
		url = url.trim();
		if (url.startsWith("http://"))
			url = url.substring(7);
		if (url.contains("/"))
			url = url.substring(0, url.indexOf("/"));
		if (url.contains("?"))
			url = url.substring(0, url.indexOf("?"));
		return url;
	}

}

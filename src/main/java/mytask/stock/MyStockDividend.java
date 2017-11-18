package mytask.stock;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.text.html.parser.Element;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import MyClass.HttpClientTool;

public class MyStockDividend {
	private static Logger logger = LogManager.getLogger(MyStockDividend.class
			.getName());
	static final String dbUrl = "jdbc:mysql://localhost:3306/mysite?useUnicode=true&characterEncoding=utf8&user=root&password="; // 数据库信息
	//static final String dbUrl ="jdbc:mysql://sql206.byetcluster.com:3306/b18_11341106_84205?useUnicode=true&characterEncoding=utf8&user=b18_11341106&password=hahaaa";
	static final String dbDriver = "com.mysql.jdbc.Driver";
	static String stockIdField = "mystock_stockinfo,stockid";
	static String dividend_table = "mystock_sharebonus";

	public static void main(String[] args) {
		MyStockDividend mstock = new MyStockDividend();
		mstock.updateDiviInfo();
		System.out.println("ok");
	}

	private static List<Map<String, String>> getDividend(String stockId) {
		String url = "http://vip.stock.finance.sina.com.cn/corp/go.php/vISSUE_ShareBonus/stockid/#SSTOCKID#.phtml";
		if (stockId == null || stockId.length() != 8) {
			return null;
		}
		stockId = stockId.substring(2);
		url = url.replace("#SSTOCKID#", stockId);
		System.out.println(url);
		String html = HttpClientTool.getMethod(url, "gb2312");
		Document doc = Jsoup.parse(html);
		if (doc == null) {
			return null;
		}
		Elements items = doc.select("div#con02-0>table>tbody>tr");

		List<Map<String,String>> results = new ArrayList<Map<String, String>>();
		for (int i = 0; i < items.size(); i++) {
			org.jsoup.nodes.Element ele = items.get(i);
			Elements elements=ele.select("td");
//			System.out.println(elements.text());
			Map<String, String> result = new HashMap<String, String>();
			try {
				if(elements.size()<=8)continue;
				result.put("pubdate", elements.get(0).text().trim());
				result.put("content",elements.get(3).text().trim());
				result.put("divdate", elements.get(5).text().trim());
				result.put("regdate",elements.get(6).text().trim().substring(0, 10));
				result.put("url","http://vip.stock.finance.sina.com.cn"+
						elements.get(8).select("a").attr("href").toString().trim());
				
				if (result != null && !result.isEmpty()) {
					results.add(result);
				}
			} catch (NumberFormatException |IndexOutOfBoundsException e) {
				e.printStackTrace();
			}
		}

		return results;
	}

	public boolean updateDiviInfo() {

		try {
			Class.forName(dbDriver);
			Connection conn = null;
			ResultSet rs = null;
			conn = DriverManager.getConnection(dbUrl);
			Statement sta = conn.createStatement();

			// 需要获取股票列表
			String[] tstockIdField = stockIdField.split(",", 2);
			if (tstockIdField.length < 2) {
				logger.error("无法获取股票id列表");
				return false;
			}
			String tableName = tstockIdField[0].trim();
			String tableField = tstockIdField[1].trim();
			String sql = "select " + tableField + " from " + tableName;

			rs = sta.executeQuery(sql);
			Set<String> stockIdSet = new HashSet<String>();
			while (rs.next()) {
				stockIdSet.add(rs.getString(tableField));
			}
			rs.close();

			for (String stockId : stockIdSet) {
				if (stockId == null || stockId.isEmpty())
					continue;
				stockId = stockId.trim();
				List<Map<String, String>> result = getDividend(stockId);
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
					sql = "insert into " + dividend_table + "(" + s[0]
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

		} catch (ClassNotFoundException | SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return false;
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

}

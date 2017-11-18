package mytask.stock;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
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

public class MyStockBaseInfo {
	private static Logger logger = LogManager
			.getLogger(MyStockBaseInfo.class.getName());
	static final String dbUrl = "jdbc:mysql://localhost:3306/mysite?useUnicode=true&characterEncoding=utf8&user=root&password="; // 数据库信息
	static final String dbDriver = "com.mysql.jdbc.Driver";

	public static void main(String[] args) {
		MyStockBaseInfo mstock=new MyStockBaseInfo();
		mstock.updateBaseInfo();
		System.out.println("ok");
	}
	

	private static Map<String, String> getBaseInfo(String stockId) {
		String url = "http://stockhtm.finance.qq.com/sstock/ggcx/#SSTOCKID#.shtml?pgv_ref=fi_smartbox&_ver=2.0";
		if (stockId == null || stockId.length() != 8) {
			return null;
		}
		stockId = stockId.substring(2);
		url = url.replace("#SSTOCKID#", stockId);
		System.out.println(url);
		String html = HttpClientTool.getMethod(url, "utf-8");
		Document doc = Jsoup.parse(html);
		if (doc == null) {
			return null;
		}
		Elements elements = doc.select("div#mod-gsgk>table.data>tbody>tr>td");

		Map<String, String> result = new HashMap<String, String>();
		try{
		result.put("area", elements.get(0).text());
		result.put("timetomark", elements.get(3).text());
		result.put("totalcapital", Float.valueOf(elements.get(1).text()).toString());
		result.put("tradableshare", Float.valueOf(elements.get(4).text()).toString());

		result.put("earnpershare", Float.valueOf(elements.get(2).text()).toString());
		result.put("netassetsps", Float.valueOf(elements.get(5).text()).toString());
		result.put("cashflowps", Float.valueOf(elements.get(6).text()).toString());
		result.put("providentfundps", Float.valueOf(elements.get(7).text()).toString());

		result.put("roe", Float.valueOf(elements.get(8).text()).toString());
		result.put("netprofitgrowth", Float.valueOf(elements.get(9).text()).toString());
		result.put("retainedearningps", Float.valueOf(elements.get(10).text()).toString());
		result.put("revenuegrowth", Float.valueOf(elements.get(11).text()).toString());
		}catch(NumberFormatException e){
			
		}
		if (result == null || result.isEmpty()) {
			return null;
		}
		return result;
	}

	public  boolean updateBaseInfo() {
		String tableName = "mystock_stockinfo";
		String sql = "select * from " + tableName + " where status is NULL";
		try {
			Class.forName(dbDriver);
			Connection conn = null;
			ResultSet rs = null;
			conn = DriverManager.getConnection(dbUrl);
			Statement sta = conn
					.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,
							ResultSet.CONCUR_UPDATABLE);
			rs = sta.executeQuery(sql);
	
			while (rs.next()) {
				String stockId = rs.getString("stockid");
				if (stockId == null || stockId.length() != 8) {
					continue;
				}
				Map<String, String> baseInfo = getBaseInfo(stockId);
				baseInfo.put("status", "T");
				for (Map.Entry<String, String> entry : baseInfo.entrySet()) {
					if (entry == null || entry.getKey().isEmpty()
							|| entry.getValue() == null
							|| entry.getValue().isEmpty())
						continue;
					try {
						rs.updateString(entry.getKey().trim(), entry.getValue()
								.trim());
						System.out.println(entry.getKey()+":"+entry.getValue());
					} catch (SQLException e) {
					}
				}
				rs.updateRow();
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

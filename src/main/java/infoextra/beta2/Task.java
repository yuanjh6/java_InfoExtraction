package infoextra.beta2;


import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Task {
	private static Logger logger = LogManager.getLogger(Task.class.getName());
	private String taskConfig;
	private XmlConfig config;
	private List<Map<String, String>> resultsList;
	private List<Seed> seeds;
	private String LOGFILE = "config/task_log";

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Task task = new Task("config/ddbooks.xml");
		task.storeUpdateResults();
//		test();

	}
	public static void test(){
		//遗漏项目处理
		Map<String, String> configMap = new HashMap<String, String>();
		configMap.put("name", "lvselost");
		configMap
				.put("buildUrl",
						"http://www.lvse.com/zhongguo/p#PAGE#/");
		configMap.put("charset", "utf-8");
		configMap.put("elementSelect", "div.info");
		configMap.put("staticMap", "");
		configMap
				.put("attrMap",
						"{\"name\"=\"a,text\",\"url\"=\"a.visit,attr,href\",\"relate\"=\"a,attr,href\"}");
		configMap.put("markField", "url");

		configMap.put("markValue", "");

	
		Seed seed = new Seed(configMap);
		String pageRange="2203,2203";
		String failPage="";
		List<Map<String, String>> resultsList = new ArrayList<Map<String, String>>();
		List<Map<String, String>> results;
		for (String spage:pageRange.split(",")) {
			int i=Integer.parseInt(spage);
			results = seed.results(i);

			if (results == null||results.size()==0){
				failPage+=Integer.toString(i)+",";
				continue;
			}
			logger.info("seed:" + seed.name() + "\tpage:" + i + "\t获取结果数目:"
					+ results.size());
			if(results.size()==0)failPage+=i+",";

			for (Map<String, String> map : results) {
				resultsList.add(map);
			}
		}
		
		Task task=new Task("config/siteinfo.xml");
		task.storeResults(resultsList);
		
		BufferedWriter bw;
		try {
			bw = new BufferedWriter(new FileWriter("config/task_log",true));
			bw.write(seed.name() + ":" + failPage + "\n");
			bw.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	public Task(String taskConfig) {
		this.resultsList = new ArrayList<Map<String, String>>();
		this.seeds = new ArrayList<Seed>();
		this.taskConfig = taskConfig;

		// 载入配置文件
		this.config = new XmlConfig(this.taskConfig);
		// 载入种子信息到种子map

		for (Map<String, String> seedConfig : this.config.getSeeds()) {
			this.seeds.add(new Seed(seedConfig));
		}
		// 去除无效的种子

		for (int i = 0; i < this.seeds.size(); i++) {
			if (this.seeds.get(i).enable() == false
					|| this.seeds.get(i).finish() == true) {
				this.seeds.remove(i);
				i--;
			}
		}
	}

	public void run() {
		logger.info("task读入配置文件完成");
		this.resultsList = this.getUpdateResults();
		this.cleanResults(this.resultsList);
		if (this.storeResults(this.resultsList)) {
			this.updateMark();
		}

		this.config.save();
		logger.info("任务执行完成");
	}

	private void storeUpdateResults() {
		BufferedWriter bw;
		try {
			bw = new BufferedWriter(new FileWriter(LOGFILE, true));

			for (Seed seed : this.seeds) {
				List<Map<String, String>> result = seed.updateResults();
				if (result != null) {
//					this.cleanResults(result);
					this.storeResults(result);
					String markValue = seed.updateMark();
					bw.write(seed.name() + ":" + seed.failPage() + "\n");
					bw.flush();
					this.config.update(seed.name(), markValue);
					this.config.save();
				}
			}
			bw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private List<Map<String, String>> getUpdateResults() {
		List<Map<String, String>> resultsList = new ArrayList<Map<String, String>>();
		for (Seed seed : this.seeds) {
			List<Map<String, String>> result = seed.updateResults();
			if (result != null)
				resultsList.addAll(result);
		}
		return resultsList;
	}

	private void cleanResults(List<Map<String, String>> resultsList) {
		for (Map<String, String> map : resultsList) {
			map.put("url", urlFormat(map.get("url")));
		}

	}

	private boolean storeResults(List<Map<String, String>> resultsList) {
		try {
			Class.forName(this.config.dbDriver());
			Connection conn = DriverManager.getConnection(this.config.dbUrl());
			Statement sta = conn.createStatement();
			String sql;
			int e = 0, r = 0;
			for (Map<String, String> map : resultsList) {
				if (map == null)
					continue;
				String[] s = mapToStr(map);
				if (s == null)
					continue;
				sql = "insert into " + this.config.tableName() + "(" + s[0]
						+ ") values " + "(" + s[1] + ");";
				// System.out.println(sql);

				try {
					if (sta.executeUpdate(sql) > 0) {
						r++;
					} else {
						logger.debug("sql语句执行失败:" + sql);
						e++;
					}
				} catch (SQLException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
					e++;
				}

			}
			logger.info("成功插入数据:" + r + "\t插入数据失败:" + e);
			sta.close();
			conn.close();
			return true;
		} catch (ClassNotFoundException | SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}

	}

	private void updateMark() {
		for (Seed seed : this.seeds) {
			// xml文件中的mark字段更新
			String markValue = seed.updateMark();

			// seed-class中的变量更新
			this.config.update(seed.name(), markValue);

		}

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
			tmp2 += "," + "\"" + entry.getValue() + "\"";
		}
		if (tmp1.length() < 1 || tmp2.length() < 1)
			return null;

		tmpret[0] = tmp1.substring(1);
		tmpret[1] = tmp2.substring(1);

		return tmpret;
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

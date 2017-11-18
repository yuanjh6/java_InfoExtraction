package infoextra.rss;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
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
	private List<Seed> seedsList;
	// private String LOGFILE = "config/task_log";

	public static void main(String[] args) {
		Task task = new Task();
		if (!task.setConfig("mytasks/newtext.xml")) {
			logger.error("task配置文件读取失败");
			return;
		}
		logger.info("task读入配置文件完成");
		task.runAll();
		logger.info("完成任务");
	}

	public Task() {
		this.resultsList = new ArrayList<Map<String, String>>();
		this.seedsList = new ArrayList<Seed>();
	}

	public boolean setConfig(String taskConfig) {
		if (taskConfig == null || taskConfig.isEmpty()) {
			logger.error("配置文件路径为空，请检查");
			return false;
		}
		if (!new File(taskConfig).exists()) {
			logger.error("配置文件不存在，请检查");
			return false;
		}
		// 载入配置文件
		this.taskConfig = taskConfig;
		this.config = new XmlConfig(this.taskConfig);

		// 载入种子信息到种子map
		int count=0;
		for (Map<String, String> seedConfig : this.config.getPages()) {
			Seed seed=new Page();
			if (seed != null && seed.setConfig(seedConfig)) {
				this.seedsList.add(seed);
				count++;
			}
		}
		logger.info("Page种子数量:"+count);
		
		count=0;
		for (Map<String, String> seedConfig : this.config.getRsss()) {
			Seed seed = new Rss();
			if (seed != null && seed.setConfig(seedConfig)) {
				this.seedsList.add(seed);
				count++;
			}
		}
		logger.info("RSS种子数量:"+count);
		return true;
	}

	public void runAll() {
		this.resultsList = this.getUpdateResults();
		this.cleanResults(this.resultsList);
		if (this.storeResults(this.resultsList)>0) {
			this.updateMark();
			this.config.save();
		}	
	}
	public void runOneByOne() {
		List<Map<String, String>> resultsList = new ArrayList<Map<String, String>>();
		int count=0;
		for (Seed seed: this.seedsList) {
			List<Map<String, String>> result = seed.getUpdateResults();
			
			if (result == null||result.isEmpty()){
				logger.debug("种子:"+seed.name()+" 更新结果条数:0");
				continue;
			}
			logger.info("种子:"+seed.name()+" 更新结果条数:"+result.size());
			
			this.cleanResults(result);
			int i=this.storeResults(result);
			logger.info("种子:"+seed.name()+" 插入结果:"+i);
			
			String markValue = seed.updateMark();
			if(markValue!=null&&!markValue.isEmpty()){
				this.config.update(seed.getType(),seed.name(), markValue);
			}
			count+=i;
		}
		this.config.save();
		logger.info("总插入结果数量:"+count);
		
	}


	/**
	 * @return 获取更新的结果结合
	 */
	private List<Map<String, String>> getUpdateResults() {
		List<Map<String, String>> resultsList = new ArrayList<Map<String, String>>();
		for (Seed seed: this.seedsList) {
			List<Map<String, String>> result = seed.getUpdateResults();
			if (result != null){
				resultsList.addAll(result);
				logger.info("种子:"+seed.name()+" 更新结果条数:"+result.size());
			}
		}
		logger.info("总更新结果数量:"+resultsList.size());
		return resultsList;
	}

	/**
	 * @param resultsList
	 *            带处理的结果集合
	 */
	private void cleanResults(List<Map<String, String>> resultsList) {
		
		//清除已经抓取但是数据库中不存在的字段(只保留需要写入数据库的字段)
		String tableField=this.config.getTableField();
		Set<String> tableFieldSet=new HashSet<String>();
		if(tableField!=null&&!tableField.isEmpty()){
			for(String s:tableField.split(",")){
				if(s!=null&&!s.isEmpty()){
					tableFieldSet.add(s.trim());
				}
			}
			//清除map中不存在于tableFieldSet的字段
			for(Map<String,String> map:resultsList){
				if(map==null||map.isEmpty())continue;
				//求差集
				Set<String> difSet=new HashSet<String>(map.keySet());
				difSet.removeAll(tableFieldSet);
				for(String s:difSet){
					map.remove(s);
				}
			}
		}
		
		
		
	}

	private int storeResults(List<Map<String, String>> resultsList) {
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
			sta.close();
			conn.close();
			return r;
		} catch (ClassNotFoundException | SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return 0;
		}

	}

	/**
	 * 调用config方法更新配置文件中的结果最新标记
	 */
	private void updateMark() {
		for (Seed seed : this.seedsList) {
			// xml文件中的mark字段更新
			String markValue = seed.updateMark();
			// seed-class中的变量更新
			if(markValue!=null&&!markValue.isEmpty()){
				this.config.update(seed.getType(),seed.name(), markValue);
			}
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
			tmp2 += "," + "\"" + entry.getValue().replace("\"", "'") + "\"";
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

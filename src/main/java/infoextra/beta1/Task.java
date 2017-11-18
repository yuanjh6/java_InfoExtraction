package infoextra.beta1;

import java.io.IOException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import MyClass.Config;
import MyClass.DataBase;
import MyClass.Function;
import MyClass.RegExp;
import MyClass.WebPage;

public class Task {
	/*
	 * 说明：给定一个任务，通过名称查找xml树得到此网页切分等需要的信息， 通过网页内的页码格式结合任务给定的页码范围得到需要抓去的url序列，
	 * 依次抓取出数据后以map数组的形式返回得到数据。
	 */

	private static Logger logger = LogManager.getLogger(Task.class.getName());
	String seedDir, seedSuffix;
	ArrayList<Map<String, String>> rsMapList;

	public Task(String seedDir, String seedSuffix)
			throws ClassNotFoundException, SQLException {
		// TODO Auto-generated constructor stub
		// step-1:参数检查
		if (!checkStr(seedDir) || !checkStr(seedSuffix)) {
			logger.error("参数非法");
			return;
		}
		this.seedSuffix = seedSuffix;
		this.seedDir = seedDir;

	}
	
	
	public static void main(String[] agrv) throws SQLException, IOException, ClassNotFoundException{
		logger.entry();
		// TODO Auto-generated method stub
		
		// step-1:读取配置文件,初始化数据库连接
		Config.init("config/infocatch.cfg");
		DataBase.init(Config.getConfig("dbDriver"), Config.getConfig("dbUrl"));
		String tablePrefix=Config.getConfig("tablePrefix");
		String taskTable=tablePrefix+Config.getConfig("taskTable");
		String seedDir=Config.getConfig("seedDir");
		String seedSuffix=Config.getConfig("seedSuffix");

		// step-2:读取任务列表
		String sql = "select * from " + taskTable + " where enable=\"1\" order by id;";
		Statement sta1=DataBase.con.createStatement();
		
		ResultSet rs = sta1.executeQuery(sql);
		

		// step-3:获取结果并且过滤旧结果，保存结果，修改任务列表
		Task task = new Task(seedDir,seedSuffix);
		String spage,slastMark,type;
		String[] page;
		String[] lastMark;
		String newLastMark;
		String seedName;
		//创建结果保存数列
		ArrayList<Map<String, String>> mapList= new ArrayList<Map<String, String>>();
		// 根据最新的标志位去除已经提取过的数据				
		ArrayList<Map<String, String>> tmapList = new ArrayList<Map<String, String>>();
		//对每一任务依次执行
		Statement sta2=DataBase.con.createStatement();
		while (rs.next()) {
			mapList.clear();
			tmapList.clear();
			//获取任务信息
			seedName = rs.getString("seedname");
			spage = rs.getString("pagerange");
			slastMark = rs.getString("lastmark");
			type = rs.getString("type");

			if(!checkStr(spage)||!checkStr(slastMark)||!checkStr(seedName)||!checkStr(type)){
			logger.error("任务信息错误");
			return ;
			}
			page=spage.split(",",2);
			lastMark=slastMark.split("=",2);
			
			logger.info("now seedname:" + seedName);
			 mapList = task.run(seedName,
						Integer.parseInt(page[0]), Integer.parseInt(page[1]));
			 
			 
			//lastMark值为2说明过滤旧信息的字段有值，否则，认为所以数据均为有效数据无需过滤
			//当lastmark中不含有=时，split后length属性为1
			if (lastMark.length==2&&checkStr(lastMark[1])) {
				for (Map<String, String> map : mapList) {
					if (map.get(lastMark[0]).equals(lastMark[1]))
						break;
					tmapList.add(map);
				}
				if (tmapList.size() <= 0) {
					logger.info("没有数据的更新,进行下一个任务");
					continue;
				}
				mapList = tmapList;
			}
			
			Collections.reverse(mapList);
			//将得到的更新后数据写入到数据库中
			sql = "";
			String[] s = new String[2];
			int i = 0,j=0;
			logger.info("过滤更新后的结果的总数" + mapList.size());
			for (Map<String, String> map : mapList) {
				map.put("seed", seedName);
				s = DataBase.mapToStr(map);
				sql = "insert into " + tablePrefix+type + "(" + s[0] + ") values " + "("
						+ s[1] + ");";
//				System.out.println(sql);
				i = sta2.executeUpdate(sql);
				if (i <= 0) {
					logger.debug("上一条sql语句执行失败");
				}else{
				j++;
				}
			}
			logger.info("执行成功sql语句数量："+j);
			//更新最新task表中的最新标记位
			
			if (mapList.size() > 0) {
				newLastMark = lastMark[0]
						+ "="
						+ mapList.get(mapList.size()-1).get(lastMark[0])
								;
				sql = "update " + taskTable + " set lastmark=\"" + newLastMark
						+ "\",status=\"T\"" + " where seedname=\"" + seedName
						+ "\" and type=\"" + type + "\";";
				logger.debug("更新任务标记的sql:" + sql);
				i = sta2.executeUpdate(sql);
				logger.debug("执行返回值:" + i);
			}

		}
		rs.close();
		DataBase.end();
		logger.exit();
		
	}

	public ArrayList<Map<String, String>> run(String seedname, int startPage,
			int endPage) throws ClassNotFoundException, SQLException,
			IOException {
		// STEP-1:传入参数检查
		if (!checkStr(seedname) || startPage < 0 || endPage < 0) {
			logger.error("参数非法");
			return null;
		}
	

		// STEP-0:从文件读入种子参数
		String fileName = this.seedDir + seedname + this.seedSuffix;
		Map<String, String> map = Function.fileToMap(fileName);
		String firstUrl = "", baseUrl = "", pageCharset = "", headTag = "", footTag = "", splitTag = "", reg = "", regInfo = "";

		if (!map.get("name").equals(seedname)) {
			seedname=map.get("name");
			logger.warn("种子文件内部名称和文件名不一致，采用内部文件名 :"+seedname);
		}

		firstUrl = map.get("firstUrl");
		baseUrl = map.get("baseUrl");// 必须使用pageid代替页码变量
		pageCharset = map.get("charSet");
		headTag = map.get("headTag");
		footTag = map.get("footTag");
		splitTag = map.get("splitTag");
		reg = map.get("reg");
		regInfo = map.get("regInfo");

		logger.debug("seedName:" + seedname + " firsturl:" + firstUrl
				+ " baseurl:" + baseUrl + " charset:" + pageCharset);

		// STEP-1:构造需要探测的url序列
		int pageNum = endPage - startPage + 1;// 纯数字构造的页面数量
		if (pageNum < 0) {
			logger.error("待提取页数为负值:" + pageNum);
			return null;
		}
		logger.info("需要处理的页面数量:" + Integer.toString(pageNum + 1));
		String[] urlList = new String[pageNum + 1];// 加上初始的firsturl没有计算到页面数量中
		urlList[0] = firstUrl;
		for (int i = 0; i < pageNum; i++) {
			String url = "";
			url = baseUrl.replace("pageid", Integer.toString(startPage + i));
			urlList[i + 1] = url;
		}

		// STEP-2:获得需要匹配的单个条目序列
		WebPage webPage = new WebPage();
		ArrayList<String> itemList = new ArrayList<String>();
		int i = 0;
		for (String url : urlList) {
			ArrayList<String> tmpitemList = webPage.getItem(url, pageCharset,
					headTag, footTag, splitTag);
			if (tmpitemList == null) {
				i++;
			}
			for (String str : tmpitemList) {
				itemList.add(str);
				// System.out.println(str);
			}
		}
		logger.info("获取到item总数为:" + itemList.size() + " 失败跳过条目数量:" + i);

		// STEP-3:匹配单个条目的正则表达式内容
		// 构造正则匹配的方法
		RegExp regexp = new RegExp(reg);
		String[] regItem = regInfo.split(",");
		String[] t;
		for (String s : regItem) {
			t = s.split(":", 2);
			regexp.addItem(t[0], Integer.parseInt(t[1]));
		}

		// Set<Map.Entry<String, Integer>> entrySet = regexp.getItemMap()
		// .entrySet();
		// for (Map.Entry<String, Integer> entry : entrySet) {
		// System.out.println(entry.getKey() + ":" + entry.getValue());
		// }
		logger.debug("构造的正则表达式匹配对象" + regexp.printRE());

		// 依次匹配条目列表中的每一项
		ArrayList<Map<String, String>> itemsInfo = new ArrayList<Map<String, String>>();
		Map<String, String> tmap = new HashMap<String, String>();
		i = 0;
		for (String item : itemList) {
			tmap = regexp.getItemInfo(item);
			if (tmap != null) {
				itemsInfo.add(tmap);
			} else {
				i++;
			}
		}
		logger.info("提取失败跳过总条目：" + i);
		return itemsInfo;
	}

	public static boolean checkStr(String str) {
		if (str == null)
			return false;
		str = str.trim();
		if (str.length() <= 0)
			return false;
		return true;
	}

}

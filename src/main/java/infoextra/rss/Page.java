package infoextra.rss;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;

import MyClass.HttpClientTool;
import MyClass.JsoupTool;

public class Page extends Seed{
	private static Logger logger = LogManager.getLogger(Page.class.getName());
	private String name;
	private String url;
	private boolean enableJs;
	private String charset;
	private String elementSelect;
	private Map<String, String> elementInfo;
	private String markValue;
	private String newMarkValue;
	private boolean finish;
	private String groupName;
	private String type="page";
	private String baseUrl=null;

	public String getType() {
		return type;
	}

	public Page() {

	}

	public boolean setConfig(Map<String, String> configMap) {
		if (!configMap.containsKey("name")||!configMap.containsKey("url")){
			logger.error("重要参数缺失，请检查");
			return false;
		}
		this.name=configMap.get("name").trim();
		this.url=configMap.get("url").trim();
		if(this.name==null||this.name.isEmpty()||this.url==null||this.url.isEmpty()){
			logger.error("重要参数为空,请检查");
			return false;
		}
	
		// 当且仅当enable为true时启用js
		if (configMap.containsKey("enableJs")) {
			this.enableJs = configMap.get("enableJs").trim().equalsIgnoreCase("true");
		} else {
			this.enableJs = false;
		}

		if (configMap.containsKey("charset")
				&& !configMap.get("charset").isEmpty()) {
			this.charset = configMap.get("charset").trim();
		} else {
			charset = null;
		}

		if (configMap.containsKey("elementSelect"))
			this.elementSelect = configMap.get("elementSelect").trim();
		if (configMap.containsKey("elementInfo")) {
			this.elementInfo = toMap(configMap.get("elementInfo"));
		}

		if (configMap.containsKey("markValue"))
			this.markValue = configMap.get("markValue").trim();
		if(configMap.containsKey("baseUrl"))
			this.baseUrl=configMap.get("baseUrl").trim();

		if (configMap.containsKey("finish")
				&& configMap.get("finish").trim().equalsIgnoreCase("true")) {
			this.finish = true;
		} else {
			this.finish = false;
		}

		if(configMap.containsKey("groupname")){
			this.groupName=configMap.get("groupname").trim();;
		}
		
		return true;
	}

	public static void main(String[] argv) {
	test();

	}
	private static void test() {
//		Page page = new Page();
		String url = "http://student.fenzhi.com/company/bank/";
		String charset = "utf-8";
		String elementSelect = "div#Results>table>tbody>tr";
		Map<String, String> elementInfo = new HashMap<String, String>();
		elementInfo.put("title", "td>div.JobsTitle>a,text");
		elementInfo.put("link", "td>div.JobsTitle>a,attr,href");
		elementInfo.put("pubdate", "td:contains(-),text");
		
		String html = HttpClientTool.getMethod(url, charset);
		System.out.println(Jsoup.parse(html));
		List<Map<String, String>> list ;
		list = JsoupTool.itemsInfo(html, elementSelect, elementInfo);
		System.out.println(list.size());
		if (list != null && !list.isEmpty()) {
			for (Map<String, String> map : list) {
				System.out.println(map);
			}
		}
	}
	

	// 获取page页面结果
	private List<Map<String, String>> getResults(String html) {
		if (html == null)
			return null;
		// 返回网页解析结果
		return JsoupTool.itemsInfo(html, this.elementSelect, this.elementInfo);
	}

	// 返回html页面结果
	private String getPage(String url, String charset) {
		if (url == null || url.isEmpty()) {
			return null;
		}
		return HttpClientTool.getMethod(url, charset);
	}

	// 返回js页面xml形式结果
	private String getJsPageAsXml(String url) {
		if (url == null || url.isEmpty()) {
			return null;
		}
		return HtmlUnitTool.getPageAsXml(url);
	}

	

	/**
	 * @return 得到实际更新的结果集合
	 */
	public List<Map<String, String>> getUpdateResults() {
		List<Map<String, String>> resultsList = new ArrayList<Map<String, String>>();
		List<Map<String, String>> results;
		String[] urlList=this.url.split(",");

		boolean ok = false;// 是否找到重复的标记字段
		for (String url:urlList) {
			if(ok==true)break;
			// 获取单个页面结果集合
			if(url==null||url.isEmpty())continue;
			String html=null;
			// 获取网页
			if (this.enableJs) {
				html = this.getJsPageAsXml(url);
			} else {
				html = this.getPage(url, this.charset);
			}
			// 获取页面结果信息
			results = this.getResults(html);
//			logger.info("种子:" + this.name +" 获取结果数目:"
//					+ results.size());

			// 结果处理url转化，查找是否与标记的字段重复,以及固定信息添加
			for (Map<String, String> map : results) {
				if(map.containsKey("link")&&!map.get("link").isEmpty()&&this.baseUrl!=null&&!this.baseUrl.isEmpty()){
					map.put("link", this.baseUrl+map.get("link"));
				}
				// 判断最新标记位
				if (map.containsKey("link")&&map.get("link").trim().equals(this.markValue)) {
					ok = true;
					break;
				}

				// 其他信息添加
				if (this.name != null) {
					map.put("name", this.name);
				}
				if (this.groupName!= null) {
					map.put("groupname", this.groupName);
				}
				resultsList.add(map);
			}

		}
//		logger.info("seed:" + this.name + " 得到更新数量:" + resultsList.size());

		// 没有任何更新
		if (resultsList.size() < 1)
			return null;
		this.newMarkValue = resultsList.get(0).get("link");
		Collections.reverse(resultsList);
		return resultsList;
	}

	/**
	 * @return 更新标记，最新记录的标记以及完成任务的标记
	 */
	public String updateMark() {
		if (this.newMarkValue == null || this.newMarkValue.isEmpty())
			return null;
		this.markValue = this.newMarkValue;
		this.finish = true;
		return this.markValue;
	}

	public String name() {
		return this.name;
	}

	/**
	 * @return 返回任务是否完成的洗信息
	 */
	public boolean finish() {
		return this.finish;
	}

	private static Map<String, String> toMap(String str) {
		Map<String, String> map = new HashMap<String, String>();
		if (str == null || str.isEmpty())
			return null;

		for (String item : str.split("\",\"")) {
			String[] tmpitem = item.split("=", 2);
			if (tmpitem.length < 2)
				continue;
			map.put(tmpitem[0].replaceAll("\"|\\{|\\}", ""),
					tmpitem[1].replaceAll("\"|\\{|\\}", ""));
		}
		return map;
	}

	

}

package infoextra.group;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import MyClass.HttpClientTool;
import MyClass.JsoupTool;

public class Page extends Seed {
	private static Logger logger = LogManager.getLogger(Page.class.getName());
	private static final String PAGERANGE = "1,2";
	private static final String type="page";
	private static int sleepWait=5000;
	private String name;
	private String firstUrl;
	private boolean enableJs;
	private String pageUrl;
	private String baseUrl;
	private String charset;
	private String elementSelect;
	private Map<String, String> elementInfo;
	private Map<String, String> otherInfo;
	private Map<String, String> pageInfo;
	private String pageRange;
	private String markField;
	private String markValue;
	private String newMarkValue;
	private boolean finish;
	private String failPage = "";

	public Page() {

	}

	public boolean setConfig(Map<String, String> configMap) {
		if (configMap.containsKey("name"))
			this.name = configMap.get("name").trim();
		if (configMap.containsKey("firstUrl"))
			this.firstUrl = configMap.get("firstUrl").trim();
		if (configMap.containsKey("pageUrl"))
			this.pageUrl = configMap.get("pageUrl").trim();

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

		if (configMap.containsKey("baseUrl")) {
			this.baseUrl = configMap.get("baseUrl").trim();
		}

		if (configMap.containsKey("elementSelect"))
			this.elementSelect = configMap.get("elementSelect").trim();

		if (configMap.containsKey("markField"))
			this.markField = configMap.get("markField").trim();
		if (configMap.containsKey("markValue"))
			this.markValue = configMap.get("markValue").trim();

		if (configMap.containsKey("pageRange")) {
			this.pageRange = configMap.get("pageRange").trim();
		} else {
			this.pageRange = PAGERANGE;
		}

		if (configMap.containsKey("finish")
				&& configMap.get("finish").trim().equalsIgnoreCase("true")) {
			this.finish = true;
		} else {
			this.finish = false;
		}

		// 获取map配置文件
		if (configMap.containsKey("pageInfo")) {
			this.pageInfo = toMap(configMap.get("pageInfo"));
		}

		if (configMap.containsKey("elementInfo")) {
			this.elementInfo = toMap(configMap.get("elementInfo"));
		}

		if (configMap.containsKey("otherInfo")) {
			this.otherInfo = toMap(configMap.get("otherInfo"));
		}
		return true;
	}

	public static void main(String[] argv) {

		test();
	}

	private static void test() {
//		Page page = new Page();
		String url = "http://www.3158.cn/list/------------1.html";
		String charset = "utf-8";
		String elementSelect = "div>form>div>div.info";
		Map<String, String> elementInfo = new HashMap<String, String>();
		elementInfo.put("txt", "div.info>p>strong>a,text");
		elementInfo.put("turl", "div.info>p>strong>a,attr,href");
		elementInfo.put("mtype", "div.info>p:contains(行业)>span>a,text");
		
		String html = HttpClientTool.getMethod(url, charset);
		System.out.println(html);
		List<Map<String, String>> list = new ArrayList<Map<String, String>>();
		System.out.println(list.size());
		list = JsoupTool.itemsInfo(html, elementSelect, elementInfo);
		if (list != null && !list.isEmpty()) {
			for (Map<String, String> map : list) {
				System.out.println(map);
			}
		}
	}

	private String constructUrl(int page) {
		if (page <= 0) {
			return null;
		} else if (page == 1 && this.firstUrl != null
				&& !this.firstUrl.isEmpty()) {
			return this.firstUrl;
		}
		return this.pageUrl.replace("#PAGE#", Integer.toString(page));
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
	 * @return 返回页面的相关信息
	 */
	private Map<String, String> getPageInfo(String html) {
		if (this.pageInfo == null || this.pageInfo.isEmpty()) {
			return null;
		}
		return JsoupTool.htmlInfo(html, this.pageInfo);
	}

	/**
	 * @return 得到实际更新的结果集合
	 */
	public List<Map<String, String>> getUpdateResults() {
		List<Map<String, String>> resultsList = new ArrayList<Map<String, String>>();
		List<Map<String, String>> results;
		// 获取页面页码范围参数
		Integer minPage, maxPage;
		String[] pagerange = this.pageRange.split(",", 2);
		if (pagerange.length < 2) {
			minPage = 1;
			maxPage = Integer.parseInt(pagerange[0]);
		} else {
			minPage = Integer.parseInt(pagerange[0]);
			maxPage = Integer.parseInt(pagerange[1]);
		}

		boolean ok = false;// 是否找到重复的标记字段
		for (int i = minPage; i <= maxPage && ok == false; i++) {
			// 获取单个页面结果集合
			String url = this.constructUrl(i);
			String html;
			// 获取网页
			try {
				Thread.sleep(sleepWait);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				logger.error("程序睡眠错误");
			}
			
			if (this.enableJs) {
				html = this.getJsPageAsXml(url);
			} else {
				html = this.getPage(url, this.charset);
			}
			// 获取pageInfo信息
			Map<String, String> pageinfo = this.getPageInfo(html);
			// 获取页面结果信息
			results = this.getResults(html);
			// 获取失败，记录失败页面
			if (results == null || results.size() == 0) {
				this.failPage += Integer.toString(i) + ",";
				continue;
			}
			logger.info("种子:" + this.name + " 页面:" + i + " 获取结果数目:"
					+ results.size());

			// 结果处理url转化，查找是否与标记的字段重复,以及固定信息添加
			for (Map<String, String> map : results) {
				// 结果处理,相对url变为绝对url，必须在比较更新比较 更新标记之前进行
				if (this.baseUrl != null && !this.baseUrl.isEmpty()) {
					map.put("link", this.baseUrl + map.get("link").trim());
				}
				// 判断最新标记位
				if (this.markValue != null && map.containsKey(this.markField)
						&& map.get(this.markField).equals(this.markValue)) {
					ok = true;
					break;
				}

				// 其他信息添加
				if (this.otherInfo != null)
					map.putAll(this.otherInfo);
				
				if (pageinfo != null) {
					map.putAll(pageinfo);
				}
				if (this.name != null) {
					map.put("name", this.name);
				}
				resultsList.add(map);
			}

		}
		logger.info("种子:" + this.name + " 得到更新数量:" + resultsList.size());

		// 没有任何更新
		if (resultsList.size() < 1)
			return null;
		this.newMarkValue = resultsList.get(0).get(this.markField);
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

	/**
	 * @return 失败页面url记录
	 */
	public String failPage() {
		return this.failPage;
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

	@Override
	public String getType() {
		// TODO Auto-generated method stub
		return type;
	}

}

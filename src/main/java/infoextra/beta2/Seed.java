package infoextra.beta2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import proxy.MProxy;

import MyClass.HttpClientTool;
import MyClass.JsoupTool;

public class Seed {
	private static Logger logger = LogManager.getLogger(Seed.class.getName());
	private static final String PAGERANGE = "1,2";
	private String name;
	private String firstUrl;
	private String buildUrl;
	private String baseUrl;

	private String charset;
	private String elementSelect;
	private Map<String, String> attrMap;
	private Map<String, String> staticMap;
	private String pageRange;
	private String markField;
	private String markValue;
	private String newMarkValue;
	private MProxy mproxy;
	private boolean enable;
	private boolean finish;
	private String failPage="";

	public Seed(Map<String, String> configMap) {
		// TODO Auto-generated constructor stub

		logger.debug("初始化seed类，开始解析初始化参数 ");
		if (configMap.containsKey("name"))
			this.name = configMap.get("name");
		if (configMap.containsKey("firstUrl"))
			this.firstUrl = configMap.get("firstUrl");
		if (configMap.containsKey("buildUrl"))
			this.buildUrl = configMap.get("buildUrl");
		if (configMap.containsKey("baseUrl"))
			this.baseUrl = configMap.get("baseUrl");

		if (configMap.containsKey("charset")
				&& !configMap.get("charset").equals("")) {
			this.charset = configMap.get("charset");
		} else {
			charset = null;
		}

		if (configMap.containsKey("elementSelect"))
			this.elementSelect = configMap.get("elementSelect");

		if (configMap.containsKey("markField"))
			this.markField = configMap.get("markField");
		if (configMap.containsKey("markValue"))
			this.markValue = configMap.get("markValue");

		if (configMap.containsKey("pageRange")) {
			this.pageRange = configMap.get("pageRange");
		} else {
			this.pageRange = PAGERANGE;
		}

		if (configMap.containsKey("staticMap")) {
			this.staticMap = toMap(configMap.get("staticMap"));
		}

		if (configMap.containsKey("attrMap"))
			this.attrMap = toMap(configMap.get("attrMap"));
		this.mproxy = new MProxy();

		if (configMap.containsKey("enable")
				&& configMap.get("enable").trim().equalsIgnoreCase("false")) {
			this.enable = false;
		} else {
			this.enable = true;
		}

		if (configMap.containsKey("finish")
				&& configMap.get("finish").trim().equalsIgnoreCase("true")) {
			this.finish = true;
		} else {
			this.finish = false;
		}

		logger.info("seed初始化结束");

	}

	public static void main(String[] argv) {
	

	}
	public static void test(){
		
		
	}

	public boolean enable() {
		return this.enable;
	}

	private String buildUrl(int page) {
		if (page <= 0) {
			return null;
		} else if (page == 1 && this.firstUrl != null
				&& !this.firstUrl.equals("")) {
			return this.firstUrl;
		}
		return this.buildUrl.replace("#PAGE#", Integer.toString(page));
	}

	// 获取page页面结果
	List<Map<String, String>> results(int page) {

		// 获取网页
		String url = buildUrl(page);
		if (url == null)
			return null;
		String proxy=this.mproxy.getNext();
		String html = HttpClientTool.getMethod(url, this.charset,
				proxy);
		//认为代理问题，登记此代理错误次数
		if(html==null){
			this.mproxy.addFail(proxy);
			return null;
		}
		// 返回网页解析结果
		return JsoupTool.itemsInfo(html, this.elementSelect, this.attrMap);
	}

	public List<Map<String, String>> updateResults() {		
		List<Map<String, String>> resultsList = new ArrayList<Map<String, String>>();
		List<Map<String, String>> results;
		Integer minPage, maxPage;
		String[] pagerange = this.pageRange.split(",", 2);
		if (pagerange.length < 2) {
			minPage = 1;
			maxPage = Integer.parseInt(pagerange[0]);
		} else {
			minPage = Integer.parseInt(pagerange[0]);
			maxPage = Integer.parseInt(pagerange[1]);
		}

		boolean ok = false;
		for (int i = minPage; i <= maxPage && ok == false; i++) {
			results = this.results(i);

			if (results == null||results.size()==0){
				this.failPage+=Integer.toString(i)+",";
				continue;
			}
			logger.info("seed:" + this.name + "\tpage:" + i + "\t获取结果数目:"
					+ results.size());

			for (Map<String, String> map : results) {
				// 注意这一步和下一步顺序不能反了，否则两个Url永远不会相等
				// map信息规范， 相对地址转化为绝对地址，以及添加种子名称
				if (this.baseUrl != null && !this.baseUrl.equals("")) {
					map.put("url", this.baseUrl + map.get("url"));
				}

				if (this.markValue != null && map.containsKey(this.markField)
						&& map.get(this.markField).equals(this.markValue)) {
					ok = true;
					break;
				}
				if (this.staticMap != null)
					map.putAll(this.staticMap);

				resultsList.add(map);
			}

		}
		logger.info("seed:" + this.name + " 得到更新数量:" + resultsList.size());

		// 没有任何更新
		if (resultsList.size() < 1)
			return null;
		this.newMarkValue = resultsList.get(0).get(this.markField);
		Collections.reverse(resultsList);
		return resultsList;
	}

	public String updateMark() {
		if (this.newMarkValue == null || this.newMarkValue.equals(""))
			return null;
		this.markValue = this.newMarkValue;
		this.finish=true;
		return this.markValue;
	}
	

	public String baseUrl() {
		return this.baseUrl;
	}

	public String name() {
		return this.name;
	}
	public boolean finish(){
		return this.finish;
	}

	public String failPage(){
		return this.failPage;
	}

	private static Map<String, String> toMap(String str) {
		Map<String, String> map = new HashMap<String, String>();
		if (str == null || str.equals(""))
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

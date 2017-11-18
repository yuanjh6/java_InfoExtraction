package mytask.stock;

import java.util.ArrayList;
import java.util.HashMap;

import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class JsoupTool {
	private static Logger logger = LogManager.getLogger(JsoupTool.class
			.getName());

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		test();
	}

	public static void test() {

		String html = HttpClientTool.getMethod("http://www.baidu.com/s?wd=ip", null);
		if(html==null)System.out.println("erro html =null");

		Map<String, String> attrMap = new HashMap<String, String>();
		 String eleSelect = "td.f";
		 attrMap.put("url", "a,attr,href");
		 attrMap.put("title", "a,text");
		 attrMap.put("info", "font,text");
		 attrMap.put("time", "span.g,text");
		 
		
		 for (Map<String, String> tmap : itemsInfo(html, eleSelect,
		 attrMap)) {
		 System.out.println(tmap);
		 }
		//测试元素抽取器
//		Element ele=Jsoup.parse(html);
//		System.out.println(elementInfo(ele,attrMap));
		//测试网页内容抽取
//		System.out.println(webpageInfo(html, attrMap));

	}

	/*
	 * 提取网页中的元信息 参数说明：html文档, eleSelect:元素选择规则，参照jsoup的元素选择器
	 * .MAP:attrSelect:key:提取内容名称,value:子元素选择器参数,函数名称[,参数] 百度搜索结果页面eleSelect =
	 * "td.f" attrMap.put("url", "a,attr,href"); attrMap.put("title", "a,text");
	 * attrMap.put("info", "font,text"); attrMap.put("time", "span.g,text");
	 */
	public static List<Map<String, String>> itemsInfo(String html,
			String eleSelect, Map<String, String> attrMap) {
		Document doc = Jsoup.parse(html);

		// 获取元素
		Elements elements = doc.select(eleSelect);
		if (elements == null) {
			logger.error("元素选择失败，返回null");
			return null;
		}

		List<Map<String, String>> itemList = new ArrayList<Map<String, String>>();
		for (Element element : elements) {
			itemList.add(elementInfo(element,attrMap));
		}

		return itemList;
	}

	private static Map<String, String> elementInfo(Element element,
			Map<String, String> attrMap) {
		Map<String, String> item = new HashMap<String, String>();
		Element childElement;
		String key = null, value = null;
		String[] tmpvalue;

		for (Map.Entry<String, String> entry : attrMap.entrySet()) {
			//待抽取信息参数提取
			key = entry.getKey();
			tmpvalue = entry.getValue().split(",");
			if (tmpvalue.length < 2 || tmpvalue[0] == null
					|| tmpvalue[1] == null) {
				continue;
			}
			
			//带抽取信息所在子元素选择
			childElement = element.select(tmpvalue[0]).first();
			if (childElement == null)
				continue;
			
			// 长度为二第二项意义为函数,长度为第三项意义为函数参数
			if (tmpvalue[1].equalsIgnoreCase("text")) {
				value = childElement.text();
			} else if (tmpvalue[1].equalsIgnoreCase("outerHtml")) {
				value = childElement.outerHtml();
			}else if(tmpvalue[1].equalsIgnoreCase("html")){
				value = childElement.html();
			}else if(tmpvalue[1].equalsIgnoreCase("attr")){
				value = childElement.attr(tmpvalue[2]);
			}

			if (value != null) {
				item.put(key.trim(), value.trim());
			}
		}

		return item;
	}

	public static Map<String, String> htmlInfo(String html,
			Map<String, String> attrMap) {
		Element element=Jsoup.parse(html);
		return elementInfo(element,attrMap);
	}

	public static Map<String, String> getXpathInfo(String html,
			Map<String, String> itemMap) {
		return null;
	}

}

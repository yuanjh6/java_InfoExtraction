package infoextra.beta1;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import MyClass.Function;
import MyClass.RegExp;
import MyClass.WebPage;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TestSeed {
	private static Logger logger = LogManager.getLogger(TestSeed.class
			.getName());

	static String firstUrl = "", baseUrl = "", pageCharset = "", headTag = "",
			footTag = "", splitTag = "", reg = "", regInfo = "", base = "";

	public TestSeed() throws IOException {
		// TODO Auto-generated constructor stub
		String seedPath = "seed/";
		String seedName = "baidunews";
		String seedSuffix = ".seed";

		Map<String, String> map = Function.fileToMap(seedPath + seedName
				+ seedSuffix);
		seedName=map.get("name");
		firstUrl = map.get("firstUrl");
		baseUrl = map.get("baseUrl");// 必须使用pageid代替页码变量
		pageCharset = map.get("charSet");
		headTag = map.get("headTag");
		footTag = map.get("footTag");
		splitTag = map.get("splitTag");
		reg = map.get("reg");
		regInfo = map.get("regInfo");
		
		logger.info(reg);
		logger.info(regInfo);

	}

	
	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		TestSeed testSeed = new TestSeed();
		testSeed.run();

	}

	public void run() throws IOException {
		logger.entry();

		WebPage webPage = new WebPage();
		ArrayList<String> itemList = webPage.getItem(firstUrl, pageCharset,
				headTag, footTag, splitTag);
//		for(String s:itemList){
//			logger.debug(s);
//		}
		if (itemList.size() <= 0) {
			logger.error("获取网页条目信息失败!");
			return;
		}
		logger.info("获取的切分条目为:"+itemList.size());
		RegExp regexp = new RegExp(reg);
		String[] mapItem = regInfo.split(",");
		String[] t;
		for (String s : mapItem) {
			t = s.split(":");
			regexp.addItem(t[0].trim(), Integer.parseInt(t[1]));
		}

		ArrayList<Map<String, String>> itemsInfo = new ArrayList<Map<String, String>>();
		Map<String, String> tmap = new HashMap<String, String>();
		for (String item : itemList) {
			if (!Function.checkStr(item)) {
				logger.debug("匹配出的条目中含空值,跳过 ");
				continue;
			}
			tmap = regexp.getItemInfo(item);
			if (tmap != null) {
				logger.debug("提取出的信息为:" + Function.ptMap(tmap));
				itemsInfo.add(tmap);
			} else {
				logger.debug("匹配失败,跳过");
			}
		}
		logger.exit();
	}

}

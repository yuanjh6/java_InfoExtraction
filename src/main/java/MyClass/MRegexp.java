package MyClass;
/*
 * 获取正则表达式相关操作 
 * 
 */
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MRegexp {
	// private static Logger logger = LogManager.getLogger(MRegexp.class
	// .getName());
	private Pattern pattern;
	private Map<String, Integer> regexpInfo;

	public MRegexp(String regexp, String sRegexpInfo) {
		// TODO Auto-generated constructor stub
		this.pattern = Pattern.compile(regexp);

		String[] tregexpInfo = sRegexpInfo.split(",|，");
		String[] t;

		for (String s : tregexpInfo) {
			if (!checkStr(s)) {
				t = s.split(":", 2);
				if(t.length==2)this.regexpInfo.put(t[0].trim(), Integer.parseInt(t[1]));
			} 
			
		}
	}
	public Map<String, String> getRegexpInfo(String item) {
		// 利用本身的正则表达式信息以及字段对应的id表提取出item的具体信息。
		Map<String, String> itemInfo = new HashMap<String, String>();
		;
		// 内容匹配以及提取
		Matcher matcher = pattern.matcher(item);
		if (matcher.find()) {
			for (String skey : regexpInfo.keySet()) {
				itemInfo.put(skey, matcher.group(regexpInfo.get(skey)));
			}
		} else {
			return null;
		}
		return itemInfo;
	}

	/*
	 * 检查字符串是否合法，（不为空并且除去收尾空白字符后长度大于o），合法返回true
	 */
	public static boolean checkStr(String str) {
		if (str == null || str.trim().length() <= 0) {
			return false;
		}
		return true;
	}

}

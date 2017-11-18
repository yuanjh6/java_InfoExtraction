package MyClass;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Function {

	public Function() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
	public static boolean checkNull(Object object) {
		if (object == null) {
			return false;
		}
		return true;
	}
	
	public static boolean checkNull(Object objecta,Object objectb) {
		if (objecta == null ||objectb==null) {
			return false;
		}
		return true;
	}
	
	public static boolean checkNull(Object objecta,Object objectb,Object objectc) {
		if (objecta == null || objectb==null || objectc==null) {
			return false;
		}
		return true;
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


	/*
	 * 返回map的string形式
	 * {key1="value1",key2="value2",key3="value3"}
	 */
	public static String mapToStr(Map<String, String> map) {
		
		if (map == null) {
			return null;
		}
		
		String tmpret = "";
		Set<Map.Entry<String, String>> entrySet = map.entrySet();
		for (Map.Entry<String, String> entry : entrySet) {
			tmpret += "," + entry.getKey() + "=" + "\"" + entry.getValue()
					+ "\"";
		}
		tmpret = tmpret.substring(1);
		return "{" + tmpret + "}";
	}

	
	/*
	 * 功能说明：输出返回map的字符串形式用于打印输出,返回格式为 
	 * {key1='value1',key2='value2',key3='value3'}
	 */
	public static String ptMap(Map<String, String> map) {

		Set<Map.Entry<String, String>> entrySet = map.entrySet();
		String tmpstr = "";
		for (Map.Entry<String, String> entry : entrySet) {
			tmpstr += "," + entry.getKey() + "='" + entry.getValue()+"'";
		}
		tmpstr = tmpstr.substring(1);
		return "{" + tmpstr + "}";
	}
	
	/*
	 * 功能说明：将文件读入到map当中，
	 *文件中数据保存格式为：
	 *;打头为注释内容，
	 *key=value
	 */
	public static Map<String,String> fileToMap(String file) throws IOException{
		//说明，注释标志位行首;开头，内容分隔符为=
		BufferedReader rd=new BufferedReader(new FileReader(file));
		HashMap<String,String> map=new HashMap<String,String>();
		
		String line;
		String[] s=new String[2];
		while((line=rd.readLine())!=null){
			if(!line.startsWith(";")){
				s=line.split("=",2);
				if(!checkStr(s[0]))continue;
				map.put(s[0].trim(), s[1].trim());
			}
		}
		rd.close();
		return map;
	}

}

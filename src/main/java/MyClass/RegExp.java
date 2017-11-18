package MyClass;


import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegExp {
	public static boolean debug=false;
	public String reg;
	Pattern pattern;
	Map<String, Integer> regInfo=new HashMap<String,Integer>();
	public RegExp(String reg){
		this.reg=reg;
		this.pattern=Pattern.compile(reg);
	}
	public void addItem(String itemName,int itemId){
		//添加提取元素的id
		regInfo.put(itemName,itemId);
	}
	public void setItemMap(Map<String,Integer> map){
		this.regInfo=map;
	}
	public Map<String,Integer> getItemMap(){
		//获取正则表达式id对照表信息
		return regInfo;
	}
	public Map<String,String> getItemInfo(String item){
		//利用本身的正则表达式信息以及字段对应的id表提取出item的具体信息。
		Map<String,String> itemInfo=new HashMap<String,String>();
		//内容匹配以及提取
		Matcher matcher=pattern.matcher(item);
		if(matcher.find()){
			for(String skey: regInfo.keySet()){ 
				itemInfo.put(skey,matcher.group(regInfo.get(skey))); 
			}
		}else{
			if(debug)System.out.println("匹配失败");
			return null;
		}
		return itemInfo;
	}
	
	public String printRE(){
		String ret=null;
		
		
		String s="";
		Set<Map.Entry<String,Integer>> entrySet=this.regInfo.entrySet();
		for(Map.Entry<String, Integer> entry:entrySet){
			s+=","+entry.getKey()+"="+entry.getValue();
		}
		if(!checkStr(s)){
			return null;
		}
		s.substring(1);
		ret=this.reg+","+"{"+s+"}";
		return ret;
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

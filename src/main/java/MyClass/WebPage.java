package MyClass;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import info.monitorenter.cpdetector.io.CodepageDetectorProxy;


public class WebPage {
	public boolean debug=true;
	
	private static CodepageDetectorProxy detector = CodepageDetectorProxy
			.getInstance();
	static {
/*
		detector.add(new HTMLCodepageDetector(false));
		detector.add(JChardetFacade.getInstance());
*/
	}
	/**	测试用例
	 * @param args
	 * @throws IOException 
	 */
	
	public static void main(String[] args) throws IOException {


	}
	/**
	 * @param strurl
	 *            页面url地址,需要以 http://开始，例：http://www.pujia.com
	 * @return
	 * @throws IOException
	 */
	public String getCharset(String strurl) throws IOException {
		// 定义URL对象
		URL url = new URL(strurl);
		// 获取http连接对象
		HttpURLConnection urlConnection = (HttpURLConnection) url
				.openConnection();
		;
		urlConnection.connect();
		// 网页编码
		String strencoding = "";

		/**
		 * 首先根据header信息，判断页面编码
		 */
		// map存放的是header信息(url页面的头信息)
		Map<String, List<String>> map = urlConnection.getHeaderFields();
		Set<String> keys = map.keySet();
		Iterator<String> iterator = keys.iterator();

		// 遍历,查找字符编码
		String key = "";
		String tmp = "";
		while (iterator.hasNext()) {
			key = iterator.next();
			tmp = map.get(key).toString().toLowerCase();
			// 获取content-type charset
			if (key != null && key.equals("Content-Type")) {
				int m = tmp.indexOf("charset=");
				if (m != -1) {
					strencoding = tmp.substring(m + 8).replace("]", "");
					return strencoding;
				}
			}
		}

		/**
		 * 通过解析meta得到网页编码
		 */
		// 获取网页源码(英文字符和数字不会乱码，所以可以得到正确<meta/>区域)
		StringBuffer sb = new StringBuffer();
		String line;
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(url
					.openStream()));
			while ((line = in.readLine()) != null) {
				sb.append(line);
			}
			in.close();
		} catch (Exception e) { // Report any errors that arise
			System.err.println(e);
			System.err
					.println("Usage:   java   HttpClient   <URL>   [<filename>]");
		}
		String htmlcode = sb.toString();
		// 解析html源码，取出<meta />区域，并取出charset
		String strbegin = "<meta";
		String strend = ">";
		String strtmp;
		int begin = htmlcode.indexOf(strbegin);
		int end = -1;
		int inttmp;
		while (begin > -1) {
			end = htmlcode.substring(begin).indexOf(strend);
			if (begin > -1 && end > -1) {
				strtmp = htmlcode.substring(begin, begin + end).toLowerCase();
				inttmp = strtmp.indexOf("charset");
				if (inttmp > -1) {
					strencoding = strtmp.substring(inttmp + 7, end).replace(
							"=", "").replace("/", "").replace("\"", "")
							.replace("\'", "").replace(" ", "");
					return strencoding;
				}
			}
			htmlcode = htmlcode.substring(begin);
			begin = htmlcode.indexOf(strbegin);
		}

		/**
		 * 分析字节得到网页编码
		 */
		strencoding = getFileEncoding(url);

		// 设置默认网页字符编码
		if (strencoding == null) {
			strencoding = "GBK";
		}

		return strencoding;
	}

	/**
	 * 
	 *<br>
	 * 方法说明：通过网页内容识别网页编码
	 * 
	 *<br>
	 * 输入参数：strUrl 网页链接; timeout 超时设置
	 * 
	 *<br>
	 * 返回类型：网页编码
	 */
	public static String getFileEncoding(URL url) {
		java.nio.charset.Charset charset = null;
		try {
			charset = detector.detectCodepage(url);
		} catch (Exception e) {
			System.out.println(e.getClass() + "分析" + "编码失败");
		}
		if (charset != null)
			return charset.name();
		return null;
	}
	


	public  String getPage(String purl, String pcharSet) {
		// 爬取网页
		// 参数检测
		if(!purl.startsWith("http"))purl="http://"+purl;
		
		if(purl==null||purl.length()<=0){
			System.out.println("classErrorReturn:"+this.getClass().getName()+"reason:Method getPage purl is null");
			return null;
		}
		if(pcharSet==null||pcharSet.length()<=0){
			if(debug)System.out.println("classWarning:"+this.getClass().getName()+"reason:method pcharSet is null default set utf-8");
			pcharSet="utf-8";
		}
		
		String line = "", content = "";
		try {
			URL url = new URL(purl);
			BufferedReader in = new BufferedReader(new InputStreamReader(
					url.openStream(), pcharSet));
			while ((line = in.readLine()) != null) {
				content += line;
			}
			in.close();
		} catch (MalformedURLException e) {
		} catch (IOException e) {
		}
//		System.out.println("content:"+content+"url:"+purl+"pcharset:"+pcharSet);
		return content;
		
	}
	
	public String getPage(String purl) throws IOException{
		String pcharSet=this.getCharset(purl);
		String content="",line="";
		try {
			URL url = new URL(purl);
			BufferedReader in = new BufferedReader(new InputStreamReader(
					url.openStream(),pcharSet));
			while ((line = in.readLine()) != null) {
				content += line;
			}
			in.close();
		} catch (MalformedURLException e) {
		} catch (IOException e) {
		}
		return content;
	}
	
	public ArrayList<String> getItem(String url,String charSet,String headTag, String footTag,String splitTag) throws IOException{
		if(url==null||url.length()<=0){
			System.out.println("classErrorReturn:"+this.getClass().getName()+" reason:Method getRegInfo url is null");
			return null;
		}
		
		
		//获取网页内容
		String pageContent="";
		if(charSet==null||charSet.length()<=0){
			pageContent=this.getPage(url);
		}else{
			pageContent=this.getPage(url, charSet);
		}
		
		if(pageContent==null||pageContent.length()<=0){
			System.out.println("classErrorReturn:"+this.getClass().getName()+"reason:Method getRegInfo pageContent is null(can't get the pageContent)");
			return null;
		}
		
		//网页清理
		String[] tmp = pageContent.split(headTag,2);
		pageContent = tmp[1];
		tmp = pageContent.split(footTag,2);
		pageContent = tmp[0];
		if(pageContent==null||pageContent.length()<=0){
			System.out.println("classErrorReturn:"+this.getClass().getName()+"reason:Method getRegInfo pageContent is null(can't get the pageContent)");
			return null;
		}
		
		//网页内容切分
		String[] tmpitemList = pageContent.split(splitTag);
		ArrayList<String> itemList=new ArrayList<String>();
		for(String strtmp:tmpitemList){
			itemList.add(strtmp);
		}
		return itemList;
	}
	
	
}

package mytask.stock;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.util.List;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController;
import com.gargoylesoftware.htmlunit.ScriptResult;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlDivision;
import com.gargoylesoftware.htmlunit.html.HtmlOption;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSelect;

public class HtmlUnitTool {

	public static void main(String[] args){
		String url="http://list.lufax.com/list/listing";
		getPageAsXml(url);

	}
	
	public static  String getPageAsXml(String url){
		
		// 模拟一个浏览器
		WebClient webClient = new WebClient();
		// 设置webClient的相关参数
		webClient.setJavaScriptEnabled(true);
		webClient.setAjaxController(new NicelyResynchronizingAjaxController());
		webClient.setTimeout(35000);
		webClient.setThrowExceptionOnScriptError(false);
		// 模拟浏览器打开一个目标网址
		HtmlPage rootPage;
		try {
			rootPage = webClient.getPage(url);
			return(rootPage.getPage().asXml());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
}
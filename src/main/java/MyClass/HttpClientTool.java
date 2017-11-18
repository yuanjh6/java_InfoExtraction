package MyClass;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.SocketTimeoutException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class HttpClientTool {
	private static Logger logger = LogManager.getLogger(HttpClientTool.class
			.getName());
	/**
	 * @param args
	 */
	public static void main(String[] args) throws IOException {
		System.out
				.println(HttpClientTool
						.getMethod(
								"http://www.baidu.com/s?ie=utf-8&bs=IP&f=8&rsv_bp=1&wd=IP&inputT=0",
								"utf-8", "58.53.192.218:8123"));
	}

	// 获得ConnectionManager，设置相关参数
	private static MultiThreadedHttpConnectionManager manager = new MultiThreadedHttpConnectionManager();
	private static int connectionTimeOut = 8000;
	private static int socketTimeOut = 8000;
	private static int maxConnectionPerHost = 5;
	private static int maxTotalConnections = 40;

	// 标志初始化是否完成的flag
	private static boolean initialed = false;

	// 初始化ConnectionManger的方法
	private static void SetPara() {
		manager.getParams().setConnectionTimeout(connectionTimeOut);
		manager.getParams().setSoTimeout(socketTimeOut);
		manager.getParams().setDefaultMaxConnectionsPerHost(
				maxConnectionPerHost);
		manager.getParams().setMaxTotalConnections(maxTotalConnections);
		initialed = true;
	}

	public static String getMethod(String url, String encode) {
		return getMethod(url, encode, null);
	}

	public static String getMethod(String url, String encode, String proxy) {
		// 代理等的配置
		HttpClient client = new HttpClient(manager);
		if (!initialed) {
			HttpClientTool.SetPara();
		}
		System.getProperties().setProperty("httpclient.useragent",
				"Mozilla/4.0");
		if (proxy != null && proxy.split(":", 2).length == 2) {
			client.getHostConfiguration().setProxy(
					proxy.split(":", 2)[0].trim(),
					Integer.parseInt(proxy.split(":", 2)[1].trim()));
			client.getParams().setAuthenticationPreemptive(true);
			// //如果代理需要密码验证，这里设置用户名密码
			// client.getState().setProxyCredentials(AuthScope.ANY, new
			// UsernamePasswordCredentials("llying.iteye.com","llying"));
		}
		// 构造参数
		if (encode == null||encode.trim().length()==0) {
			encode = getCharset(url);
			if(encode == null||encode.trim().length()==0) {
				encode="utf-8";
			}
		}

		// get方法配置
		GetMethod get = new GetMethod(url);
		get.setFollowRedirects(false);

		String result = null;
		StringBuffer resultBuffer = new StringBuffer();

		try {
			client.executeMethod(get);
			// 在目标页面情况未知的条件下，不推荐使用getResponseBodyAsString()方法
			// String strGetResponseBody = post.getResponseBodyAsString();
			BufferedReader in = new BufferedReader(new InputStreamReader(
					get.getResponseBodyAsStream(), get.getResponseCharSet()));

			String inputLine = null;

			while ((inputLine = in.readLine()) != null) {
				resultBuffer.append(inputLine);
				resultBuffer.append("/n");
			}

			in.close();
		} catch (SocketTimeoutException e) {
			e.printStackTrace();
			logger.debug("请求超时");
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			logger.debug("网页获取失败");
		} finally {
			get.releaseConnection();
		}
		// iso-8859-1 is the default reading encode
		result = HttpClientTool.ConverterStringCode(resultBuffer.toString(),
				get.getResponseCharSet(), encode);
		
		return result;

	}

	public static String postMethod(String url, String encode) {
		return postMethod(url, encode, null, null);
	}

	public static String postMethod(String url, String encode, String proxy,
			NameValuePair[] nameValuePair) {
		HttpClient client = new HttpClient(manager);
		if (!initialed) {
			HttpClientTool.SetPara();
		}
		System.getProperties().setProperty("httpclient.useragent",
				"Mozilla/4.0");
		if (proxy != null && proxy.split(":", 2).length == 2) {
			client.getHostConfiguration().setProxy(
					proxy.split(":", 2)[0].trim(),
					Integer.parseInt(proxy.split(":", 2)[1].trim()));
			client.getParams().setAuthenticationPreemptive(true);
			// //如果代理需要密码验证，这里设置用户名密码
			// client.getState().setProxyCredentials(AuthScope.ANY, new
			// UsernamePasswordCredentials("llying.iteye.com","llying"));
		}
		// 构造参数
		if (encode == null||encode.trim().length()==0) {
			encode = getCharset(url);
			if(encode == null||encode.trim().length()==0) {
				encode="utf-8";
			}
		}

		PostMethod post = new PostMethod(url);
		if (nameValuePair != null)
			post.setRequestBody(nameValuePair);

		post.setFollowRedirects(false);

		String result = null;
		StringBuffer resultBuffer = new StringBuffer();

		try {
			client.executeMethod(post);
			BufferedReader in = new BufferedReader(new InputStreamReader(
					post.getResponseBodyAsStream(), post.getResponseCharSet()));
			String inputLine = null;

			while ((inputLine = in.readLine()) != null) {
				resultBuffer.append(inputLine);
				resultBuffer.append("/n");
			}
			in.close();		
			
		} catch (SocketTimeoutException e) {
			e.printStackTrace();
			System.out.println("请求超时");
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("网页获取失败");
		}finally{
			post.releaseConnection();
		}
		result = HttpClientTool.ConverterStringCode(
				resultBuffer.toString(), post.getResponseCharSet(), encode);
		
		return result;
	}

	private static String ConverterStringCode(String source, String srcEncode,
			String destEncode) {
		if (source != null) {
			try {
				return new String(source.getBytes(srcEncode), destEncode);
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return "";
			}
		} else {
			return "";
		}
	}

	private static String getCharset(String url) {
		String content = null, charset = null;

		HttpClient client = new HttpClient();
		HttpMethod method = new GetMethod(url);
		try {
			client.executeMethod(method);
			content = method.getResponseBodyAsString();
		} catch (HttpException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		method.releaseConnection();
		if (content == null) {
			return null;
		}
		int j = content.indexOf("charset=") + 8;
		if (content.length() > j) {
			String Scharset = content.substring(j, j + 8);
			if (Scharset.startsWith("utf-8") || Scharset.startsWith("UTF-8")) {
				charset = "utf-8";
			} else if (Scharset.startsWith("gbk") || Scharset.startsWith("GBK")) {
				charset = "gbk";
			} else if (Scharset.startsWith("gb2312")
					|| Scharset.startsWith("GB2312")) {
				charset = "gb2312";
			}
		}
		return charset;
	}

}

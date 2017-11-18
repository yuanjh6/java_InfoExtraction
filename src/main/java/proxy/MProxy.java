package proxy;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.SocketAddress;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MProxy {
	private static Logger logger = LogManager.getLogger(MProxy.class.getName());
	private Map<Integer, String> proxyMap;
	private Map<Integer, Integer> failTimes;
	private int maxIp;
	private int proxyTimes;
	private final int MAXFAILTIMES = 5;
	private String proxyFile = "config/ipproxy.conf";

	public MProxy() {
		// TODO Auto-generated constructor stub
		this.proxyTimes = 0;
		this.maxIp=0;
		this.proxyMap = new HashMap<Integer, String>();
		this.failTimes = new HashMap<Integer, Integer>();
		try {
			this.load(proxyFile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		// 设置代理服务

		String content = "", line = "";
		String purl = "http://www.baidu.com/s?ie=utf-8&bs=adsf&f=8&rsv_bp=1&rsv_spt=3&wd=IP&rsv_sug3=2&inputT=695";
		int urlTimeout = 15000;
		String charset = "utf-8";
		try {
			URL url = new URL(purl);
			SocketAddress add = new InetSocketAddress("110.4.12.170", 83);
			Proxy p = new Proxy(Proxy.Type.HTTP, add);
			URLConnection turl = url.openConnection(p);

			turl.setConnectTimeout(urlTimeout);
			turl.setReadTimeout(urlTimeout);
			BufferedReader in = new BufferedReader(new InputStreamReader(
					turl.getInputStream(), charset));
			while ((line = in.readLine()) != null) {
				content += line;
			}
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println(content);

	}
	public String getProxy(String url) {
		return this.getNext();
	}

	public String getNext() {
		// 搜索失败次数小于阀值的代理,i防止死循环
		for(int i=0;i<maxIp;i++){
			if(this.failTimes.get(proxyTimes % maxIp) < MAXFAILTIMES){
				return this.proxyMap.get(proxyTimes++ % maxIp + 1);
			}
			proxyTimes++;
		}
		return null;
	}

	public boolean addFail(String sProxy) {
		if(sProxy==null||sProxy.trim().length()==0)return false;
		for (Map.Entry<Integer, String> entry : this.proxyMap.entrySet()) {
			if (entry.getValue().equals(sProxy)) {
				// 更新failtimes失败计数的计数器
				this.failTimes.put(entry.getKey(),
						this.failTimes.get(entry.getKey()) + 1);
				logger.debug("代理:"+entry.getValue()+"\t错误次数:"+this.failTimes.get(entry.getKey()));
				return true;
			}
		}
		return false;
	}

	private boolean load(String ipFile) throws IOException {
		Set<String> ipset = new HashSet<String>();
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(ipFile));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		String line = null;

		while ((line = br.readLine()) != null) {
			line = line.trim();
			// 过滤注释及不规范内容
			if (!line.startsWith(";") && line.contains(":")
					&& !ipset.contains(line)) {

				failTimes.put(maxIp, 0);
				proxyMap.put(maxIp++, line);

				ipset.add(line);
			}
		}
		br.close();
		return true;

	}

}

package infoextra.group;

import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;

public class Rss extends Seed {
	private static Logger logger = LogManager.getLogger(Rss.class.getName());
	private String name;
	private String url;
	private String markValue;
	private String newmarkValue;
	private String groupName;
	private String type="rss";

	public Rss() {

	}

	public String getType() {
		return type;
	}
	public boolean setConfig(Map<String, String> config) {
		if (!config.containsKey("name") || !config.containsKey("url")) {
			logger.error("必须参数缺失，请检查");
			return false;
		}
		this.name = config.get("name");
		this.url = config.get("url");
		if (this.name == null || this.name.isEmpty() || this.url == null || this.url.isEmpty()) {
			logger.error("参数错误，请检查" + " name:" + name + " url:" + url);
			return false;
		}
		this.name = this.name.trim();
		this.url = this.fillUrl(url);
		
		String markValue = null;
		if (config.containsKey("markValue"))
			markValue = config.get("markValue");
		
		if (markValue != null && !markValue.isEmpty()) {
			markValue = markValue.trim();
		}
		this.markValue = markValue;
		
		if (config.containsKey("groupname")) {
			this.groupName = config.get("groupname");
		}
		return true;
	}

	public List<Map<String, String>> getUpdateResults() {
		List<Map<String, String>> results = this.getResults();
		if (results == null || results.isEmpty()) {
			logger.error("获取结果集合为空，返回");
			return null;
		}
		// 保存新结果
		List<Map<String, String>> newResults = new ArrayList<Map<String, String>>();
		// 标记为空，保留所有结果
		if (this.markValue == null || this.markValue.isEmpty()) {
			newResults = results;
		} else {
			for (Map<String, String> result : results) {
				if (result.containsKey("link")
						&& result.get("link").equalsIgnoreCase(this.markValue)) {
					break;
				}
				newResults.add(result);
			}

		}
		for (Map<String, String> result : newResults) {
			// 添加部分私有信息
			result.put("name", this.name);
			result.put("groupname", this.groupName);
		}

		if (!newResults.isEmpty()) {
			this.newmarkValue = newResults.get(0).get("link");
		}
		Collections.reverse(newResults);
		return newResults;
	}

	public String updateMark() {
		if (this.newmarkValue == null || this.newmarkValue.isEmpty())
			return null;
		this.markValue = this.newmarkValue;
		return this.markValue;
	}


	private List<Map<String, String>> getResults() {
		List<Map<String, String>> results = new ArrayList<Map<String, String>>();
		try {
			URLConnection  feedUrl = new URL(this.url).openConnection(); 
			feedUrl.setConnectTimeout(5000);
			feedUrl.setRequestProperty("User-Agent",  
			        "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)"); 
			SyndFeedInput input = new SyndFeedInput();
			SyndFeed feed = input.build(new XmlReader(feedUrl));
			
			// 从feed中得到entry
			@SuppressWarnings("unchecked")
			List<SyndEntry> list = feed.getEntries();
			for (int i = 0; i < list.size(); i++) {
				SyndEntry entry = (SyndEntry) list.get(i);
				Map<String, String> result = new HashMap<String, String>();
				result.put("title", entry.getTitle());
				result.put("link", entry.getLink());
				if (entry.getPublishedDate() != null) {
					result.put("pubdate", new SimpleDateFormat(
							"yyyy-MM-dd hh:mm:ss").format(entry
							.getPublishedDate().getTime()));
				}
				if (entry.getDescription() != null) {
					result.put("description", entry.getDescription().getValue());
				}
				result.put("author", entry.getAuthor());
				results.add(result);
			}
			return results;
		} catch (Exception ex) {
			ex.printStackTrace();
			logger.error("获取结果失败，返回空结果集合"+"name:"+this.name);
			return null;
		}
	}

	private String fillUrl(String url) {
		if (url == null || url.isEmpty()) {
			return null;
		}
		if (!url.startsWith("http://"))
			url = "http://" + url;
		return url;

	}

	@Override
	public String name() {
		// TODO Auto-generated method stub
		return this.name;
	}

}

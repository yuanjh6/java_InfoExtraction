package infoextra.rss;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.XMLWriter;

import MyClass.XmlTool;

public class XmlConfig {
	private static Logger logger = LogManager.getLogger(XmlConfig.class
			.getName());
	String configFile;
	private Document doc;
	// root element of the document
	private Element root;

	private String taskName;
	private String dbUrl;
	private String dbDriver;
	private String tableName;
	private String tableField;

	public XmlConfig(String config) {
		// TODO Auto-generated constructor stub
		this.configFile = config;
		boolean bl = this.readConfig();
		if (bl) {
			logger.info("配置文件读取成功");
		} else {
			logger.error("配置文件读取失败 ");
		}

	}

	private boolean readConfig() {
		try {
			// 读取config文件，获得文件名字
			// config 指的是具体的文件名字，如keyword.xml 或者是rule.xml
			// SAXReader reader = new SAXReader();
			// Document doc= reader.read();

			StringBuffer xmlContent = new StringBuffer();
			try {
				BufferedReader br = new BufferedReader(new FileReader(
						this.configFile));
				String line;
				while ((line = br.readLine()) != null) {
					xmlContent.append(line + "\n");
				}
				br.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				logger.error("文件读入出错");
				return false;
			}

			if (xmlContent.length() == 0) {
				logger.error("文件内容为空");
				return false;
			}

			// 注意对xml文件格式的清理
			Document doc = DocumentHelper.parseText(xmlContent.toString());
			this.doc = doc;
			this.root = doc.getRootElement();
			Map<String, String> configMap = XmlTool.getChild(this.root);
			this.taskName = configMap.get("taskName");
			this.dbDriver = configMap.get("dbDriver");
			this.dbUrl = configMap.get("dbUrl");
			this.tableName = configMap.get("tableName");
			this.tableField = configMap.get("tableField");
			configMap.remove("seeds");
			return true;
		} catch (DocumentException e) {
			e.printStackTrace();
			return false;
		}

	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		// XmlConfig xml = new XmlConfig("config/works.xml");
	}

	public String name() {
		return this.taskName;
	}

	public String dbUrl() {
		return this.dbUrl;
	}

	public String dbDriver() {
		return this.dbDriver;
	}

	public String tableName() {
		return this.tableName;
	}

	public void update(String type, String seedName, String markValue) {
		if (markValue == null)
			return;
		Element ele = (Element) this.root.selectSingleNode("//group/" + type
				+ "[name=\'" + seedName + "\']");// 用xpath查找节点book的内容
		if (ele == null)
			return;
		ele.element("markValue").setText(markValue);// 设置相应的内容
		ele.element("finish").setText("true");
	}

	protected void save() {
		try {
			XMLWriter dWriter = new XMLWriter(new FileOutputStream(
					this.configFile));
			dWriter.write(doc);
			dWriter.flush();
			dWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public List<Map<String, String>> getPages() {
		List<Map<String, String>> seeds = new ArrayList<Map<String, String>>();
		@SuppressWarnings("unchecked")
		List<Node> glist = this.root.selectNodes("//group[enable=\'true\']");
		if (glist == null || glist.isEmpty())
			return seeds;
		for (Node group : glist) {
			if (group == null)
				continue;
			@SuppressWarnings("unchecked")
			List<Node> slist = group.selectNodes("page[enable=\'true\']");
			String groupname = null;
			if (group.selectSingleNode("groupName") != null) {
				groupname = (String)(group.selectSingleNode("groupName")).getStringValue();
			}
			for (Node ele : slist) {
				Map<String, String> seedconfig = XmlTool
						.getChild((Element) ele);
				seedconfig.put("groupname", groupname);
				seeds.add(seedconfig);
			}
		}

		return seeds;
	}

	public List<Map<String, String>> getRsss() {
		List<Map<String, String>> seeds = new ArrayList<Map<String, String>>();
		@SuppressWarnings("unchecked")
		List<Node> glist = this.root.selectNodes("//group[enable=\'true\']");
		if (glist == null || glist.isEmpty())
			return seeds;
		for (Node group : glist) {
			if (group == null)
				continue;
			@SuppressWarnings("unchecked")
			List<Node> slist = group.selectNodes("rss[enable=\'true\']");
			String groupname = null;
			if (group.selectSingleNode("groupName") != null) {
				groupname = (String)(group.selectSingleNode("groupName")).getStringValue();
			}
			for (Node ele : slist) {
				Map<String, String> seedconfig = XmlTool
						.getChild((Element) ele);
				seedconfig.put("groupname", groupname);
				seeds.add(seedconfig);
			}
		}
		return seeds;
	}

	public String getTableField() {
		return this.tableField;
	}

}

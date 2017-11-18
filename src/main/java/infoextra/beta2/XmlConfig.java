package infoextra.beta2;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

import MyClass.XmlTool;

public class XmlConfig {
	private static Logger logger = LogManager.getLogger(XmlConfig.class
			.getName());
	String configFile;
	private Document doc;
	// root element of the document
	private Element root;

	private String name;
	private String dbUrl;
	private String dbDriver;
	private String tableName;

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
			SAXReader reader = new SAXReader();
			Document doc = reader.read(this.configFile);
			this.doc = doc;
			this.root = doc.getRootElement();
			Map<String, String> configMap = XmlTool.getChild(this.root);
			this.name = configMap.get("name");
			this.dbDriver = configMap.get("dbDriver");
			this.dbUrl = configMap.get("dbUrl");
			this.tableName = configMap.get("tableName");
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
		test();
	}

	public static void test() {
		XmlConfig xml = new XmlConfig("config/works.xml");
	}

	public String name() {
		return this.name;
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

	public void update(String seedName, String markValue) {
		if(markValue==null)return;
		Element ele= (Element) this.root.selectSingleNode("//seed[name=\'"+seedName+"\']");// 用xpath查找节点book的内容
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

	public List<Map<String, String>> getSeeds() {
		List<Map<String, String>> seeds = new ArrayList<Map<String, String>>();
		for (Element ele : XmlTool.getList(this.root.element("seeds"))) {
			seeds.add(XmlTool.getChild(ele));
		}
		return seeds;
	}

}

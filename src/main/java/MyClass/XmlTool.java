package MyClass;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

public class XmlTool {


	public static Document readFile(String filename){
		try {
			// 读取config文件，获得文件名字
			// config 指的是具体的文件名字，如keyword.xml 或者是rule.xml
			SAXReader reader = new SAXReader();
			Document doc = reader.read(filename);
			return doc;
		} catch (DocumentException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static Map<String, String> getAttrib(Element element) {
		Map<String, String> elementAttrib = new HashMap<String, String>();
		for (Iterator i = element.attributeIterator(); i.hasNext();) {
			Attribute attr = (Attribute) ((Iterator) i).next();
			elementAttrib.put(attr.getName(),
					attr.getStringValue());
		}
		return elementAttrib;
	}
	//获取子节点内容，两种形式第一种为节点名称:内容，第二种节点名称：{"key"="value",**}
	public static Map<String, String> getChild(Element element) {
		Map<String, String> elementChild = new HashMap<String, String>();
//		for (Iterator i = element.elementIterator(); i.hasNext();) {
//			Element child = (Element) ((Iterator) i).next();
//	
//			elementChild.put(child.getName(),
//					child.getNodeTypeName());
//		}
//		
		List<Element> childList=element.elements();
		for(Element child:childList){
			//构造孙子节点组，判断是否还有子节点map:{}
			List<Element> grandsonList=child.elements();
			String value="";
			if(grandsonList.size()>0){
				//以map形式保存子节点的信息
				for(Element grandson:grandsonList){
					if(grandson==null||grandson.getName()==null||grandson.getStringValue()==null)continue;
					value+=",\""+grandson.getName().trim()+"\"=\""+grandson.getStringValue().trim()+"\"";
				}
				if(value.length()>0)value="{"+value.substring(1,value.length())+"}";
			}else{
				value=child.getStringValue();
			}
			elementChild.put(child.getName(),value);
			
		}
		return elementChild;
	}
	
	public static Map<String, String> getChildXml(Element element) {
		Map<String, String> elementChild = new HashMap<String, String>();
		for (Iterator i = element.elementIterator(); i.hasNext();) {
			Element childElement = (Element) ((Iterator) i).next();
			
			elementChild.put(childElement.getName(),
					childElement.asXML());
		}
		return elementChild;
		
	}
	
	public static List<Element> getList(Element element) {
		List<Element> elementChild = new ArrayList<Element>();
		for (Iterator i = element.elementIterator(); i.hasNext();) {
			Element childElement = (Element) ((Iterator) i).next();
			elementChild.add(childElement);
		}
		return elementChild;
	}

	

	public static Map<String,Element> childElement(Element element){
		Map<String,Element> childEle=new HashMap<String,Element>();
		for (Iterator i = element.elementIterator(); i.hasNext();) {
			Element childElement = (Element) ((Iterator) i).next();
			childEle.put(childElement.getName(),
					childElement);
		}
		return childEle;
	}
	
	public static String xmlValue(String xmlStr){
		return getElement(xmlStr).getStringValue();
	}
	
	
	private static Element getElement(String xmlText){
		Document document;
		try {
			document = DocumentHelper.parseText(xmlText);
			return document.getRootElement();
		} catch (DocumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}      
	}


}

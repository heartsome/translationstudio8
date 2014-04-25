package net.heartsome.cat.document;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XCSUtility {

	private static Logger logger = LoggerFactory.getLogger(XCSUtility.class);
	private Element root;
	
	public XCSUtility(File file){
		SAXReader reader = new SAXReader();
		try {
			Document doc = reader.read(file);
			root = doc.getRootElement();
		} catch (DocumentException e) {
			logger.warn("", e);
		}
	}
	
	/**
	 * 返回 picklist 中提供的选择类型
	 * @param node 节点的名称 对应 XCS 中 levels 节点的内容
	 * @param propName 属性的名称 对应 XCS 中 datCatSet 下所有元素的名称
	 * @param typeName 属性的类型 对应 XCS 中 name 属性的值
	 * @return 返回供选择的类型，如果是文本内容，则返回空的 List，如果不支持则返回 null;
	 */
	public List<String> pickList(String node, String propName, String typeName){
		if(root == null){
			return null;
		}
		Element element = getElement(node, propName, typeName);
		if(element == null){
			return null;
		}
		List<String> result = new ArrayList<String>();
		Element constants = element.element("contents");
		if(constants == null){
			return null;
		}
		String dataType = constants.attributeValue("datatype");
		if("picklist".equals(dataType)){
			String text = constants.getText();
			if(null == text || "".equals(text.trim())){
				return null;
			}
			else{
				String[] textArray = text.split(" ");
				for(String i : textArray){
					result.add(i.trim());
				}
				return result;
			}
		}
		else{
			return result;
		}
	}
	
	private Element getElement(String node, String propName, String typeName){
		Element datCatSetElement = root.element("datCatSet");
		if(datCatSetElement == null){
			return null;
		}
		@SuppressWarnings("unchecked")
		List<Element> elements = datCatSetElement.elements();
		List<Element> propElements = new ArrayList<Element>();
		for(Element i : elements){
			String name = i.getName();
			if((propName + "Spec").equals(name)){
				propElements.add(i);
			}
		}
		if(propElements.size() == 0){
			return null;
		}
		for(Element i : propElements){
			if(typeName.equals(i.attributeValue("name"))){
				Element levels = i.element("levels");
				if(levels == null){
					return i;
				}
				else{
					String levelsStr = levels.getText().trim();
					String[] levelsArray = levelsStr.split(" ");
					for(String level : levelsArray){
						if(node.equals(level.trim())){
							return i;
						}
					}
				}
			}
		}
		return null;
	}
}

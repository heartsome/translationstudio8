package net.heartsome.cat.database.ui.tm.dialog;

import java.util.ArrayList;
import java.util.HashMap;

import de.jaret.util.misc.PropertyObservableBase;
import de.jaret.util.ui.table.model.IRow;

/**
 * 创建相关搜索的行所需的类
 * @author  peason
 * @version 
 * @since   JDK1.6
 */
public class XPropRow extends PropertyObservableBase implements IRow {

	private String source;

	private ArrayList<String> lstTarget;

	private String target;
	
	private String attribute;

	private HashMap<String, String> dataMap;

	private HashMap<String, Object> map;
	
	/** 标记 */
	private boolean flag;

	public XPropRow(boolean flag, String source, ArrayList<String> lstTarget, String attribute) {
		this.flag = flag;
		this.source = source;
		this.lstTarget = lstTarget;
		this.attribute = attribute;
	}


	public XPropRow(boolean flag, String source, ArrayList<String> lstTarget, String target, String attribute) {
		this.flag = flag;
		this.source = source;
		this.lstTarget = lstTarget;
		this.target = target;
		this.attribute = attribute;
	}

	public boolean getFlag() {
		return flag;
	}

	public void setFlag(boolean flag) {
		this.flag = flag;
		firePropertyChange("Flag", null, flag);
	}

	public String getId() {
		return Integer.toString(hashCode());
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
		firePropertyChange("Source", null, source);
	}

	public ArrayList<String> getLstTarget() {
		return lstTarget;
	}

	public void setLstTarget(ArrayList<String> lstTarget) {
		this.lstTarget = lstTarget;
		for (String lang : lstTarget) {
			firePropertyChange(lang, null, lang);
		}
	}

	public String getTarget() {
		return target;
	}

	public void setTarget(String target) {
		this.target = target;
		firePropertyChange("Target", null, target);
	}

	public String getAttribute() {
		return attribute;
	}

	public void setAttribute(String attribute) {
		this.attribute = attribute;
		firePropertyChange("Attribute", null, attribute);
	}

	public HashMap<String, String> getDataMap() {
		return dataMap;
	}

	public void setDataMap(HashMap<String, String> dataMap) {
		this.dataMap = dataMap;
	}

	public void setData(String key, Object obj) {
		if (map == null) {
			map = new HashMap<String, Object>();
		}
		map.put(key, obj);
	}

	public Object getData(String key) {
		if (map == null) {
			return null;
		} else {
			return map.get(key);
		}
	}
}

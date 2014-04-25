package net.heartsome.cat.ts.core.bean;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import net.heartsome.xml.vtdimpl.VTDUtils;

public class PropGroupBean implements IXMLBean {
	/**
	 * 属性组节点的 name 属性。无此属性为 null。
	 */
	private String name;

	/**
	 * 属性组节点中的所有属性。无属性为 null。
	 */
	private Vector<PropBean> props;

	/**
	 * 获取属性组名属性。
	 */
	public String getName() {
		return name;
	}

	/**
	 * 设置属性组名属性。
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * 获取属性组中的全部属性。无属性返回 null。
	 */
	public List<PropBean> getProps() {
		return props;
	}

	/**
	 * 设置属性组名中的全部属性。
	 */
	public void setProps(Vector<PropBean> props) {
		this.props = props;
	}

	/**
	 * 构建指定名称和属性集合的属性组。
	 * @param name
	 *            属性组名。
	 * @param props
	 *            属性集合。
	 */
	public PropGroupBean(String name, Vector<PropBean> props) {
		this.name = name;
		this.props = props;
	}

	/**
	 * 构建指定属性集合的属性组。
	 * @param props
	 *            属性集合。
	 */
	public PropGroupBean(Vector<PropBean> props) {
		this.props = props;
	}

	/**
	 * 添加一个属性到属性组。
	 * @param prop
	 *            要添加的属性。
	 */
	public void addProp(PropBean prop) {
		if (props == null) {
			props = new Vector<PropBean>();
		}

		props.add(prop);
	}

	/**
	 * 获取属性组中指定属性类型的属性集合。无此类型属性时返回一个空集合。
	 * @param proptype
	 *            属性类型。
	 */
	public Vector<PropBean> getProps(String proptype) {
		Vector<PropBean> result = new Vector<PropBean>();
		Iterator<PropBean> it = props.iterator();
		while (it.hasNext()) {
			PropBean prop = it.next();
			if (proptype.equals(prop.getProptype())) {
				result.add(prop);
			}

		}
		return result;
	}

	/**
	 * 获取属性组中指定属性类型的第一个属性。无此类型属性时返回 null。
	 * @param proptype
	 *            属性类型。
	 */
	public PropBean getProp(String proptype) {
		Iterator<PropBean> it = props.iterator();
		while (it.hasNext()) {
			PropBean prop = it.next();
			if (proptype.equals(prop.getProptype())) {
				return prop;
			}
		}
		return null;
	}

	public String toXMLString() {
		String prop = "";
		for (PropBean propBean : props) {
			prop += propBean.toXMLString();
		}
		if (name == null) {
			return VTDUtils.getNodeXML("hs:prop-group", prop, null);
		} else {
			Hashtable<String, String> pro = new Hashtable<String, String>();
			pro.put("name", name);
			return VTDUtils.getNodeXML("hs:prop-group", prop, pro);
		}
	}
}

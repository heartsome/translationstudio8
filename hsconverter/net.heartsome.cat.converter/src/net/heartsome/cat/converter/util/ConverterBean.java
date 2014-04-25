/**
 * ConverterBean.java
 *
 * Version information :
 *
 * Date:Jan 14, 2010
 *
 * Copyright notice :
 */
package net.heartsome.cat.converter.util;

/**
 * The Class ConverterBean.
 * @author John Zhu
 * @version
 * @since JDK1.6
 */
public class ConverterBean {

	/** The name. */
	private final String name;

	/** The description. */
	private final String description;

	/** The filter extensions */
	// TODO 此初始化值应当被删除，并且删除 ConverterBean(String name, String description) 构造方法。
	// 在删除之前应该将每个转换器插件中调用的 ConverterBean 构造方法改为 ConverterBean(String name, String description, String[]
	// filterExtensions)，这可能需要在转换器接口添加一个字段
	private String[] extensions = {};

	/**
	 * Instantiates a new converter bean.
	 * @param name
	 *            the name
	 * @param description
	 *            the description
	 */
	public ConverterBean(String name, String description) {
		this.name = name;
		this.description = description;
	}

	/**
	 * Instantiates a new converter bean.
	 * @param name
	 *            名字
	 * @param description
	 *            描述
	 * @param filterExtensions
	 *            过滤后缀名
	 */
	public ConverterBean(String name, String description, String[] extensions) {
		this.name = name;
		this.description = description;
		this.extensions = extensions;
	}

	/**
	 * Gets the name.
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Gets the description.
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}
	
	/**
	 * Gets the extensions.
	 * @return the description
	 */
	public String[] getExtensions() {
		return extensions;
	}

	/**
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 * @return
	 */
	@Override
	public String toString() {
		return description;
	}

	/**
	 * (non-Javadoc)
	 * @see java.lang.Object#equals()
	 * @return
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof ConverterBean)) {
			return false;
		}
		ConverterBean bean = (ConverterBean) obj;
		if (name == null) {
			if (bean.name != null) {
				return false;
			}
		} else if (!name.equals(bean.name)) {
			return false;
		}
		return false;
	}

	/**
	 * 得到文件过滤条件名。
	 * @return ;
	 */
	public String getFilterNames() {
		String temp = getFilterExtensionString(",");
		return description + " Files [" + temp + "]";
	}

	/**
	 * 得到文件过滤拓展名。
	 * @return ;
	 */
	public String getFilterExtensions() {
		return getFilterExtensionString(";");
	}

	/**
	 * 得到文件过滤拓展名的字符串。
	 * @param sep
	 *            分隔符
	 * @return ;
	 */
	private String getFilterExtensionString(String sep) {
		String temp = "";
		for (int i = 0; i < extensions.length; i++) {
			temp += extensions[i];
			if (i != extensions.length - 1) {
				temp += sep;
			}
		}
		return temp;
	}

}

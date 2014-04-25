/**
 * EqFilter.java
 *
 * Version information :
 *
 * Date:Jan 14, 2010
 *
 * Copyright notice :
 */
package net.heartsome.cat.converter.util;

/**
 * The Class EqFilter.
 * @author John Zhu
 * @version
 * @since JDK1.6
 */
public class EqFilter extends FilterBuilder {

	/** The name. */
	private final String name;

	/** The value. */
	private final String value;

	/**
	 * Instantiates a new eq filter.
	 * @param name
	 *            the name
	 * @param value
	 *            the value
	 */
	public EqFilter(String name, String value) {
		this.name = name;
		this.value = value;
	}

	/**
	 * (non-Javadoc)
	 * @see net.heartsome.cat.converter.util.FilterBuilder#append(java.lang.StringBuilder)
	 * @param builder
	 * @return
	 */
	@Override
	public StringBuilder append(StringBuilder builder) {
		return builder.append('(').append(name).append('=').append(value).append(')');
	}

}

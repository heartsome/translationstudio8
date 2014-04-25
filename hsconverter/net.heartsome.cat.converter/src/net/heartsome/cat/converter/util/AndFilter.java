/**
 * AndFilter.java
 *
 * Version information :
 *
 * Date:Jan 14, 2010
 *
 * Copyright notice :
 */
package net.heartsome.cat.converter.util;

/**
 * The Class AndFilter.
 * @author John Zhu
 * @version
 * @since JDK1.6
 */
public class AndFilter extends FilterBuilder {

	/** The filters. */
	private final FilterBuilder[] filters;

	/**
	 * Instantiates a new and filter.
	 * @param filters
	 *            the filters
	 */
	public AndFilter(FilterBuilder... filters) {
		this.filters = filters;
	}

	/**
	 * (non-Javadoc)
	 * @see net.heartsome.cat.converter.util.FilterBuilder#append(java.lang.StringBuilder)
	 * @param buffer
	 * @return
	 */
	@Override
	public StringBuilder append(StringBuilder buffer) {
		StringBuilder builder = new StringBuilder();
		builder.append("(&");
		for (int i = 0; i < filters.length; i++) {
			filters[i].append(builder);
		}
		builder.append(')');
		return builder;
	}

	/**
	 * The main method.
	 * @param args
	 *            the arguments
	 */
	public static void main(String[] args) {
		System.out.println(new AndFilter(new EqFilter("name", "value"), new EqFilter("key1", "value1")).toString());
	}

}

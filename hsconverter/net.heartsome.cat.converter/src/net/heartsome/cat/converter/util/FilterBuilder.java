/**
 * FilterBuilder.java
 *
 * Version information :
 *
 * Date:Jan 14, 2010
 *
 * Copyright notice :
 */
package net.heartsome.cat.converter.util;

/**
 * The Class FilterBuilder.
 * @author John Zhu
 * @version
 * @since JDK1.6
 */
public abstract class FilterBuilder {

	/**
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 * @return
	 */
	@Override
	public final String toString() {
		return append(new StringBuilder()).toString();
	}

	/**
	 * Append.
	 * @param builder
	 *            the builder
	 * @return the string builder
	 */
	public abstract StringBuilder append(StringBuilder builder);

}

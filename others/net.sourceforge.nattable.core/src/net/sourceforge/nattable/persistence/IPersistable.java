package net.sourceforge.nattable.persistence;

import java.util.Properties;

/**
 * Instances implementing this interface can save and load their
 * state from a properties file.
 */
public interface IPersistable {

	/**
	 * Separator used for properties. Example: .BODY.columnWidth.resizableByDefault
	 */
	public static final String DOT = ".";

	/**
	 * Separator used for values. Example: 0,1,2,3,4
	 */
	public static final String VALUE_SEPARATOR = ",";

	/**
	 * Save state. The prefix must to be prepended to the property key. 
	 */
	public void saveState(String prefix, Properties properties);

	/**
	 * Restore state. The prefix must to be prepended to the property key.
	 * 
	 */
	public void loadState(String prefix, Properties properties);
	
}

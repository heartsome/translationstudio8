/**
 * Constants.java
 *
 * Version information :
 *
 * Date:Jan 13, 2010
 *
 * Copyright notice :
 */
package cn.org.tools.utils.constant;

/**
 * The Class Constants.
 * @author Terry
 * @version
 * @since JDK1.6
 */
public class Constants {
	
	/**
	 * 构造方法.
	 */
	protected Constants() {
        throw new UnsupportedOperationException(); // prevents calls from subclass
    }

	/** The DEBUG. */
	public static final boolean DEBUG = true;

	/** The DATABAS e_ type. */
	public static final int DATABASE_TYPE = 0;

	/** The Constant DATABASE_TYPE_MYSQL. */
	public static final int DATABASE_TYPE_MYSQL = 0;

	/** The Constant DATABASE_TYPE_MSSQL. */
	public static final int DATABASE_TYPE_MSSQL = 1;

	/** The Constant DATABASE_TYPE_OROCALE. */
	public static final int DATABASE_TYPE_OROCALE = 2;

	/** The Constant DATABASE_TYPE_CONFIG. */
	public static final String DATABASE_TYPE_CONFIG = "mysql";
}

/**
 * PPOPConnection.java
 *
 * Version information :
 *
 * Date:Jan 14, 2010
 *
 * Copyright notice :
 */
package net.heartsome.cat.converter.ooconnect;

/**
 * The Class PPOPConnection.
 * @author John Zhu
 * @version
 * @since JDK1.6
 */
public class PPOPConnection extends OPConnection {

	/** The Constant DEFAULT_PIPE_NAME. */
	public static final String DEFAULT_PIPE_NAME = "heartsomeconverter"; //$NON-NLS-1$

	/**
	 * Instantiates a new pPOP connection.
	 */
	public PPOPConnection() {
		this(DEFAULT_PIPE_NAME);
	}

	/**
	 * Instantiates a new pPOP connection.
	 * @param pipeName
	 *            the pipe name
	 */
	public PPOPConnection(String pipeName) {
		super("pipe,name=" + pipeName); //$NON-NLS-1$
	}

}

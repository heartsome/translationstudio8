/**
 * SocketOPConnection.java
 *
 * Version information :
 *
 * Date:Jan 14, 2010
 *
 * Copyright notice :
 */
package net.heartsome.cat.converter.ooconnect;

/**
 * The Class SocketOPConnection.
 * @author John Zhu
 * @version
 * @since JDK1.6
 */
public class SocketOPConnection extends OPConnection {

	/** The Constant DEFAULT_HOST. */
	public static final String DEFAULT_HOST = "localhost"; //$NON-NLS-1$

	/** The Constant DEFAULT_PORT. */
	public static final int DEFAULT_PORT = 8100;

	/**
	 * Instantiates a new socket op connection.
	 */
	public SocketOPConnection() {
		this(DEFAULT_HOST, DEFAULT_PORT);
	}

	/**
	 * Instantiates a new socket op connection.
	 * @param port
	 *            the port
	 */
	public SocketOPConnection(int port) {
		this(DEFAULT_HOST, port);
	}

	/**
	 * Instantiates a new socket op connection.
	 * @param host
	 *            the host
	 * @param port
	 *            the port
	 */
	public SocketOPConnection(String host, int port) {
		super("socket,host=" + host + ",port=" + port + ",tcpNoDelay=1"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}
}
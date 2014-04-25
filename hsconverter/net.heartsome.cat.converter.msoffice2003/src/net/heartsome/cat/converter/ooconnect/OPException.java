/**
 * OPException.java
 *
 * Version information :
 *
 * Date:Jan 14, 2010
 *
 * Copyright notice :
 */
package net.heartsome.cat.converter.ooconnect;

/**
 * The Class OPException.
 * @author John Zhu
 * @version
 * @since JDK1.6
 */
public class OPException extends RuntimeException {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/**
	 * Instantiates a new oP exception.
	 * @param message
	 *            the message
	 */
	public OPException(String message) {
		super(message);
	}

	/**
	 * Instantiates a new oP exception.
	 * @param message
	 *            the message
	 * @param cause
	 *            the cause
	 */
	public OPException(String message, Throwable cause) {
		super(message, cause);
	}
}
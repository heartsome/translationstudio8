/**
 * CloseOOconnection.java
 *
 * Version information :
 *
 * Date:Jan 14, 2010
 *
 * Copyright notice :
 */
package net.heartsome.cat.converter.ms2oo;

/**
 * The Class CloseOOconnection.
 * @author John Zhu
 * @version
 * @since JDK1.6
 */
public class CloseOOconnection {

	/**
	 * Instantiates a new data constant.
	 */
	protected CloseOOconnection() {
		throw new UnsupportedOperationException(); // prevents calls from subclass
	}

	/** The linux. */
	private static int linux = 1;

	/** The windows. */
	private static int windows = 2;

	/** The mac. */
	private static int mac = 3;

	/** The status. */
	private static int status = 0;

	/**
	 * Close service.
	 * @param port
	 *            the port
	 * @return the int
	 */
	public static int closeService(String port) {

		if (getSystemCode() == linux) {
			CallOO4Linux.close(port);
			setStauts(0);
		} else if (getSystemCode() == windows) {
			CallService4Windows.close(port);
			setStauts(0);
		} else if (getSystemCode() == mac) {
			CallService4Mac.close(port);
			setStauts(0);
		}
		return status;
	}

	/**
	 * Gets the system code.
	 * @return the system code
	 */
	public static int getSystemCode() {
		int systemcode = 0;
		String systemname = System.getProperty("os.name").toUpperCase(); //$NON-NLS-1$
		if (systemname.contains("LINUX")) { //$NON-NLS-1$
			systemcode = linux;
		} else if (systemname.contains("WINDOW")) { //$NON-NLS-1$
			systemcode = windows;
		} else if (systemname.contains("MAC OS X")) { //$NON-NLS-1$
			systemcode = mac;
		}
		return systemcode;
	}

	/**
	 * Sets the stauts.
	 * @param statuscode
	 *            the new stauts
	 */
	public static void setStauts(int statuscode) {
		status = statuscode;
	}

	/**
	 * Gets the status.
	 * @return the status
	 */
	public static int getStatus() {

		return status;
	}
}

/**
 * OpenConnection.java
 *
 * Version information :
 *
 * Date:Jan 14, 2010
 *
 * Copyright notice :
 */
package net.heartsome.cat.converter.ms2oo;

/**
 * The Class OpenConnection.
 * @author John Zhu
 * @version
 * @since JDK1.6
 */
public class OpenConnection {

	/**
	 * Instantiates a new data constant.
	 */
	protected OpenConnection() {
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
	 * Open service.
	 * @param path
	 *            the path
	 * @param port
	 *            the port
	 * @return the int
	 */
	public static int openService(String path, String port) {

		if (getSystemCode() == linux) {

			CallOO4Linux.start(path, port);
			setStauts(CallOO4Linux.getStatus());

		} else if (getSystemCode() == windows) {
			CallService4Windows.start(path, port);
			setStauts(CallService4Windows.getStatus());
		} else if (getSystemCode() == mac) {
			System.out.println("---------"); //$NON-NLS-1$
			CallService4Mac.start(path, port);
			setStauts(CallService4Mac.getStatus());
			System.out.println("\\\\\\\\\\\\\\\\\\\\"); //$NON-NLS-1$
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

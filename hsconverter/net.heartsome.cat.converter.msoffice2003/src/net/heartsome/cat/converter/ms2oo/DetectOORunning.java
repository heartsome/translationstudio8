/**
 * DetectOORunning.java
 *
 * Version information :
 *
 * Date:Jan 14, 2010
 *
 * Copyright notice :
 */
package net.heartsome.cat.converter.ms2oo;

import java.io.IOException;

import net.heartsome.cat.converter.Converter;

/**
 * The Class DetectOORunning.
 * @author John Zhu
 * @version
 * @since JDK1.6
 */
public class DetectOORunning {

	/**
	 * Instantiates a new data constant.
	 */
	protected DetectOORunning() {
		throw new UnsupportedOperationException(); // prevents calls from subclass
	}

	/** The Constant linux. */
	private static final int LINUX = 1;

	/** The Constant windows. */
	private static final int WINDOWS = 2;

	/** The Constant mac. */
	private static final int MAC = 3;

	/** The username. */
	private static String username;

	/** The is runn. */
	private static boolean isRunn = false;

	/**
	 * Checks if is running.
	 * @return true, if is running
	 */
	public static boolean isRunning() {
		username = System.getProperty("user.name"); //$NON-NLS-1$
		int systemcode = DetectOORunning.getSystemCode();
		switch (systemcode) {
		case LINUX:
			isRunn = detectLinux();
			break;
		case WINDOWS:
			isRunn = detectWins();
			break;
		case MAC:
			isRunn = detectMac();

			break;
		default:
			break;
		}
		return isRunn;
	}

	/**
	 * Gets the checks if is running.
	 * @return the checks if is running
	 */
	public static boolean getIsRunning() {
		return DetectOORunning.isRunn;
	}

	/**
	 * Sets the checks if is running.
	 * @param isrun
	 *            the new checks if is running
	 */
	public static void setIsRunning(boolean isrun) {
		DetectOORunning.isRunn = isrun;
	}

	/**
	 * Detect linux.
	 * @return true, if successful
	 */
	private static boolean detectLinux() {
		boolean isrun = false;
		Runtime runtime = Runtime.getRuntime();
		String execStr = "ps -U " + username + "|grep soffice"; //$NON-NLS-1$ //$NON-NLS-2$
		String[] args1 = new String[] { "/bin/sh", "-c", execStr }; //$NON-NLS-1$ //$NON-NLS-2$
		runtime.availableProcessors();
		try {
			System.out.println(System.getProperty("user.name") + "Linux"); //$NON-NLS-1$ //$NON-NLS-2$
			Process proc = runtime.exec(args1);
			StreamGobbler errorGobbler = new StreamGobbler(proc.getInputStream(), "info"); //$NON-NLS-1$
			errorGobbler.start();
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				if (Converter.DEBUG_MODE) {
					e.printStackTrace();
				}
			}
			String msg = errorGobbler.getMsg();
			isrun = msg.toLowerCase().contains("soffice"); //$NON-NLS-1$
		} catch (IOException e1) {
			if (Converter.DEBUG_MODE) {
				e1.printStackTrace();
			}
		}
		return isrun;
	}

	/**
	 * Detect wins.
	 * @return true, if successful
	 */
	private static boolean detectWins() {
		boolean isrun = false;
		Runtime runtime = Runtime.getRuntime();
		String cmdstr = "tasklist /FI \"IMAGENAME eq soffice.exe\""; //$NON-NLS-1$
		try {
			Process proc = runtime.exec(cmdstr);
			StreamGobbler errorGobbler = new StreamGobbler(proc.getInputStream(), "info"); //$NON-NLS-1$
			errorGobbler.start();
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				if (Converter.DEBUG_MODE) {
					e.printStackTrace();
				}

			}
			String msg = errorGobbler.getMsg();
			isrun = msg.toLowerCase().contains("soffice"); //$NON-NLS-1$
		} catch (IOException e1) {

			if (Converter.DEBUG_MODE) {
				e1.printStackTrace();
			}
		}
		return isrun;
	}

	/**
	 * Detect mac.
	 * @return true, if successful
	 */
	private static boolean detectMac() {
		boolean isrun = false;
		Runtime runtime = Runtime.getRuntime();
		String execStr = "ps -U " + username + " -c|grep soffice"; //$NON-NLS-1$ //$NON-NLS-2$
		String[] args1 = new String[] { "/bin/sh", "-c", execStr }; //$NON-NLS-1$ //$NON-NLS-2$
		runtime.availableProcessors();
		try {
			Process proc = runtime.exec(args1);
			StreamGobbler errorGobbler = new StreamGobbler(proc.getInputStream(), "info"); //$NON-NLS-1$
			errorGobbler.start();
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				if (Converter.DEBUG_MODE) {
					e.printStackTrace();
				}

			}
			String msg = errorGobbler.getMsg();
			isrun = msg.toLowerCase().contains("soffice"); //$NON-NLS-1$

		} catch (IOException e1) {
			if (Converter.DEBUG_MODE) {
				e1.printStackTrace();
			}

		}
		return isrun;
	}

	/**
	 * Gets the system code.
	 * @return the system code
	 */
	private static int getSystemCode() {
		int systemcode = 0;
		String systemname = System.getProperty("os.name").toUpperCase(); //$NON-NLS-1$
		if (systemname.contains("LINUX")) { //$NON-NLS-1$
			systemcode = LINUX;
		} else if (systemname.contains("WINDOW")) { //$NON-NLS-1$
			systemcode = WINDOWS;
		} else if (systemname.contains("MAC OS X")) { //$NON-NLS-1$
			systemcode = MAC;
		}
		return systemcode;
	}

	/**
	 * The main method.
	 * @param args
	 *            the arguments
	 */
	public static void main(String[] args) {
		System.out.println(isRunning());
	}

}

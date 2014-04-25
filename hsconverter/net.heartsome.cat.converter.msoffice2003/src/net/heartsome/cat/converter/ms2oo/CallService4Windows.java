/**
 * CallService4Windows.java
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
import net.heartsome.cat.converter.ooconnect.TerminationOpenoffice;

/**
 * The Class CallService4Windows.
 * @author John Zhu
 * @version
 * @since JDK1.6
 */
public class CallService4Windows {

	/**
	 * Instantiates a new data constant.
	 */
	protected CallService4Windows() {
		throw new UnsupportedOperationException(); // prevents calls from subclass
	}

	/** The status. */
	private static int status = 0;

	/** The proc. */
	private static Process proc;

	/**
	 * Start.
	 * @param path
	 *            the path
	 * @param port
	 *            the port
	 */
	public static void start(String path, String port) {
		Runtime runtime = Runtime.getRuntime();
		String cmdstr = path + " -headless -norestore -invisible" //$NON-NLS-1$
				+ " -accept=socket,host=localhost,port=" + port + ";urp; "; //$NON-NLS-1$ //$NON-NLS-2$
		try {
			proc = runtime.exec(cmdstr);
			StreamGobbler errorGobbler = new StreamGobbler(proc.getErrorStream(), "ERROR"); //$NON-NLS-1$
			errorGobbler.start();
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				if (Converter.DEBUG_MODE) {
					e.printStackTrace();
				}
			}
			String msg = errorGobbler.getMsg();
			if (!msg.equals("")) {
				CallService4Windows.setStatus(0);
			} else {
				CallService4Windows.setStatus(1);
			}
		} catch (IOException e1) {
			if (Converter.DEBUG_MODE) {
				e1.printStackTrace();
			}

			setStatus(0);
			return;
		}
	}

	/**
	 * Gets the process.
	 * @return the process
	 */
	public static Process getProcess() {
		return proc;
	}

	/**
	 * Gets the status.
	 * @return the status
	 */
	public static int getStatus() {
		return status;
	}

	/**
	 * Sets the status.
	 * @param i
	 *            the new status
	 */
	public static void setStatus(int i) {
		CallService4Windows.status = i;
	}

	/**
	 * Close.
	 * @param port
	 *            the port
	 */
	public static void close(String port) {
		TerminationOpenoffice.closeOpenoffice(port);
	}

}

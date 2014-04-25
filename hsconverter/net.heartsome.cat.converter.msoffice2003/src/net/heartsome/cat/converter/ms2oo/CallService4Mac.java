/**
 * CallService4Mac.java
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
 * The Class CallService4Mac.
 * @author John Zhu
 * @version
 * @since JDK1.6
 */
public class CallService4Mac {

	/**
	 * Instantiates a new data constant.
	 */
	protected CallService4Mac() {
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
		if (path.indexOf("/Contents/MacOS/soffice") == -1) { //$NON-NLS-1$
			path = path + "/Contents/MacOS/soffice"; //$NON-NLS-1$
		}
		// "/Applications/OpenOffice.org\ 2.4.app/Contents/MacOS/soffice"
		path = path.trim();
		path = path.replace(" ", "\\ "); //$NON-NLS-1$ //$NON-NLS-2$
		// path =path.substring(0,path.length()-1);
		// path ="."+path;
		System.out.println(path);

		String execStr = path + " -headless -norestore -invisible" //$NON-NLS-1$
				+ " \"-accept=socket,host=localhost,port=" + port + ";urp;\" &"; //$NON-NLS-1$ //$NON-NLS-2$
		System.out.println(execStr);
		String[] args1 = new String[] { "/bin/sh", "-c", execStr }; //$NON-NLS-1$ //$NON-NLS-2$
		runtime.availableProcessors();
		// /Applications/OpenOffice.org\ 2.4.app/Contents/MacOS/soffice
		// -headless -norestore -invisible
		// "-accept=socket,host=localhost,port=9000;urp;" &
		// cmdstr="./Applications/OpenOffice.org 2.4.app/Contents/MacOS/soffice.bin";

		try {
			proc = runtime.exec(args1);
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

			if (!msg.equals("") && msg.indexOf("No such file or directory") == -1) { //$NON-NLS-1$ //$NON-NLS-2$
				// CallService4Mac.setStatus(1);
				msg = ""; //$NON-NLS-1$
			}
			System.out.println("Msg:" + msg); //$NON-NLS-1$
			System.out.println(msg.equals("")); //$NON-NLS-1$
			if (!msg.equals("")) {
				CallService4Mac.setStatus(0);
			} else {
				CallService4Mac.setStatus(1);
			}
			System.out.println(CallService4Mac.getStatus());
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
		CallService4Mac.status = i;
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

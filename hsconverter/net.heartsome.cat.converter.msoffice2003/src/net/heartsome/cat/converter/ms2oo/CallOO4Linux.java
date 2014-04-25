/**
 * CallOO4Linux.java
 *
 * Version information :
 *
 * Date:Jan 14, 2010
 *
 * Copyright notice :
 */
package net.heartsome.cat.converter.ms2oo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import net.heartsome.cat.converter.Converter;
import net.heartsome.cat.converter.ooconnect.TerminationOpenoffice;

/**
 * The Class CallOO4Linux.
 * @author John Zhu
 * @version
 * @since JDK1.6
 */
public class CallOO4Linux {

	/**
	 * Instantiates a new data constant.
	 */
	protected CallOO4Linux() {
		throw new UnsupportedOperationException(); // prevents calls from subclass
	}

	/** The status. */
	private static int status = 0;

	/** The proc. */
	private static Process proc;

	/** The br. */
	@SuppressWarnings("unused")
	private static BufferedReader br = null;

	/** The in. */
	@SuppressWarnings("unused")
	private static InputStream in = null;

	/** The isr. */
	@SuppressWarnings("unused")
	private static InputStreamReader isr = null;

	/**
	 * Start.
	 * @param path
	 *            the path
	 * @param port
	 *            the port
	 */
	public static void start(String path, String port) {
		Runtime runtime = Runtime.getRuntime();
		String execStr = path + " -headless -norestore -invisible" //$NON-NLS-1$
				+ " \"-accept=socket,host=localhost,port=" + port + ";urp;\" &"; //$NON-NLS-1$ //$NON-NLS-2$
		System.out.println(execStr);
		String[] args1 = new String[] { "/bin/sh", "-c", execStr }; //$NON-NLS-1$ //$NON-NLS-2$
		runtime.availableProcessors();
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
			System.out.println("msg:" + msg); //$NON-NLS-1$
			if (!msg.equals("")) {
				CallOO4Linux.setStatus(0);
			} else {
				CallOO4Linux.setStatus(1);
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
	 * Gets the porc.
	 * @return the porc
	 */
	public static Process getPorc() {
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
		status = i;
	}

	/**
	 * Close.
	 * @param port
	 *            the port
	 */
	public static void close(String port) {
		TerminationOpenoffice.closeOpenoffice(port);

	}

	/**
	 * The main method.
	 * @param args
	 *            the arguments
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public static void main(String[] args) throws IOException {
		CallOO4Linux.start("/usr/bin/soffice", "8000"); //$NON-NLS-1$ //$NON-NLS-2$
		System.out.println(CallOO4Linux.getStatus());

	}
}

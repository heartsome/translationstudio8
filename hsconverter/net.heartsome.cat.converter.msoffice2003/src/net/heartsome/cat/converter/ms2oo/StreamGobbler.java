/**
 * StreamGobbler.java
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

/**
 * The Class StreamGobbler.
 * @author John Zhu
 * @version
 * @since JDK1.6
 */
class StreamGobbler extends Thread {

	/** The is. */
	InputStream is;

	/** The type. */
	String type; // 输出流的类型ERROR或OUTPUT

	/** The msg. */
	String msg = ""; //$NON-NLS-1$

	/** The sb. */
	StringBuffer sb;

	/**
	 * Instantiates a new stream gobbler.
	 * @param is
	 *            the is
	 * @param type
	 *            the type
	 */
	StreamGobbler(InputStream is, String type) {
		this.is = is;
		this.type = type;
		sb = new StringBuffer();
	}

	/**
	 * (non-Javadoc)
	 * @see java.lang.Thread#run()
	 */
	public void run() {
		try {
			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(isr);
			String line = null;
			while ((line = br.readLine()) != null) {
				sb.append(line);
				System.out.flush();
			}
			setMsg(sb.toString());
		} catch (IOException ioe) {
			if (Converter.DEBUG_MODE) {
				ioe.printStackTrace();
			}
		}
	}

	/**
	 * Sets the msg.
	 * @param msgs
	 *            the new msg
	 */
	public void setMsg(String msgs) {
		this.msg = msgs;
	}

	/**
	 * Gets the msg.
	 * @return the msg
	 */
	public String getMsg() {
		return msg;
	}
}

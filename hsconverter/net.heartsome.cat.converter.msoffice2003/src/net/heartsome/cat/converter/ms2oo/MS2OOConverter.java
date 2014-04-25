/**
 * MS2OOConverter.java
 *
 * Version information :
 *
 * Date:Jan 14, 2010
 *
 * Copyright notice :
 */
package net.heartsome.cat.converter.ms2oo;

import java.io.File;
import java.util.Map;

import net.heartsome.cat.converter.ooconnect.SocketOPConnection;
import net.heartsome.cat.converter.ooconverter.OpenOfficeDocumentConverter;

/**
 * The Class MS2OOConverter.
 * @author John Zhu
 * @version
 * @since JDK1.6
 */
public class MS2OOConverter {

	/** The path. */
	private String path = ""; //$NON-NLS-1$

	/** The port. */
	private String port = ""; //$NON-NLS-1$

	/** The connection. */
	private SocketOPConnection connection = null;

	/**
	 * Instantiates a new m s2 oo converter.
	 * @param args
	 *            the args
	 */
	public MS2OOConverter(Map<String, String> args) {
		path = args.get("ooPath");
		port = args.get("port");
	}

	/**
	 * Startconvert.
	 * @param inputFile
	 *            the input file
	 * @param outputFile
	 *            the output file
	 * @param openbyuser
	 *            the openbyuser
	 * @throws Exception
	 *             the exception
	 */
	public void startconvert(File inputFile, File outputFile, boolean openbyuser) throws Exception {
		boolean isrun = DetectOORunning.isRunning();
		if (!isrun) {
			OpenConnection.openService(path, port);
			openbyuser = false;
		}

		connection = new SocketOPConnection(Integer.parseInt(port));
		connection.connect();
		convert(inputFile, outputFile, openbyuser);
	}

	/**
	 * Convert.
	 * @param inputFile
	 *            the input file
	 * @param outputFile
	 *            the output file
	 * @param openbyuser
	 *            the openbyuser
	 * @throws Exception
	 *             the exception
	 */
	public void convert(File inputFile, File outputFile, boolean openbyuser) throws Exception {
		System.out.println("start to convert..."); //$NON-NLS-1$
		OpenOfficeDocumentConverter converter = new OpenOfficeDocumentConverter(connection);
		converter.convert(inputFile, outputFile);
		System.out.println("end convert..."); //$NON-NLS-1$
		connection.closeconnect();
		if (!openbyuser) {
			CloseOOconnection.closeService(port);
		}
	}

	/**
	 * Gets the port.
	 * @return the port
	 */
	public String getPort() {
		return port;
	}
}

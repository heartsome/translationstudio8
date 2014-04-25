/**
 * StartupOO.java
 *
 * Version information :
 *
 * Date:Jan 14, 2010
 *
 * Copyright notice :
 */
package net.heartsome.cat.converter.ms2oo;

import java.util.Hashtable;

/**
 * The Class StartupOO.
 * @author John Zhu
 * @version
 * @since JDK1.6
 */
public class StartupOO {

	/** The pretable. */
	private Hashtable<String, String> pretable = null;

	/** The oo service. */
	private boolean ooService = false;

	/**
	 * Instantiates a new startup oo.
	 * @param args
	 *            the args
	 */
	public StartupOO(Hashtable<String, String> args) {
		pretable = args;
	}

	/**
	 * Detect conf oo.
	 * @return true, if successful
	 */
	public boolean detectConfOO() {
		String path = pretable.get("ooPath"); //$NON-NLS-1$
		String port = pretable.get("port"); //$NON-NLS-1$
		OpenConnection.openService(path, port);
		if (OpenConnection.getStatus() == 1) {
			setOoService(true);
		}
		return ooService;
	}

	/**
	 * Can start oo.
	 */
	public void canStartOO() {
		String path = pretable.get("ooPath"); //$NON-NLS-1$
		String port = pretable.get("port"); //$NON-NLS-1$

		/**
		 * 判断是否配置正确，如果配置正确和用户手动启动正在运行，那么将不关闭正在运行的OO 否则将将关闭正在运行的OO
		 */

		boolean openbyuser = DetectOORunning.isRunning();
		OpenConnection.openService(path, port);
		if (OpenConnection.getStatus() == 1) {
			setOoService(true);
		}

		if (!openbyuser) {
			CloseOOconnection.closeService(port);
		}
	}

	/**
	 * Sets the oo service.
	 * @param status
	 *            the new oo service
	 */
	public void setOoService(boolean status) {
		this.ooService = status;
	}

	/**
	 * Gets the oo service.
	 * @return the oo service
	 */
	public boolean getOoService() {
		return this.ooService;
	}
}
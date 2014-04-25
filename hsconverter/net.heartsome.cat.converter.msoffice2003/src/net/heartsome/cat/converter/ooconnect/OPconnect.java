/**
 * OPconnect.java
 *
 * Version information :
 *
 * Date:Jan 14, 2010
 *
 * Copyright notice :
 */
package net.heartsome.cat.converter.ooconnect;

import java.net.ConnectException;

import com.sun.star.bridge.XBridge;
import com.sun.star.frame.XComponentLoader;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.ucb.XFileIdentifierConverter;
import com.sun.star.uno.XComponentContext;

/**
 * The Interface OPconnect.
 * @author John Zhu
 * @version
 * @since JDK1.6
 */
public interface OPconnect {

	/**
	 * Connect.
	 * @throws ConnectException
	 *             the connect exception
	 */
	void connect() throws ConnectException;

	/**
	 * Closeconnect.
	 */
	void closeconnect();

	/**
	 * Checks if is connected.
	 * @return true, if is connected
	 */
	boolean isConnected();

	/**
	 * Gets the desktop object.
	 * @return the desktop object
	 * @返回 com.sun.star.frame.Desktop service
	 */
	XComponentLoader getDesktopObject();

	/**
	 * Gets the file content provider.
	 * @return the com.sun.star.ucb.FileContentProvider service
	 */
	XFileIdentifierConverter getFileContentProvider();

	/**
	 * Gets the bridge.
	 * @return the bridge
	 */
	XBridge getBridge();

	/**
	 * Gets the remote service manager.
	 * @return the remote service manager
	 */
	XMultiComponentFactory getRemoteServiceManager();

	/**
	 * Gets the component context.
	 * @return the component context
	 */
	XComponentContext getComponentContext();
}

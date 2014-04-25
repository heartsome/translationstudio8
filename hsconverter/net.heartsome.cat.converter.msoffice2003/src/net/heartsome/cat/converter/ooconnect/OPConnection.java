/**
 * OPConnection.java
 *
 * Version information :
 *
 * Date:Jan 14, 2010
 *
 * Copyright notice :
 */
package net.heartsome.cat.converter.ooconnect;

import java.net.ConnectException;
import java.text.MessageFormat;

import net.heartsome.cat.converter.Converter;
import net.heartsome.cat.converter.msoffice2003.resource.Messages;

import com.sun.star.beans.XPropertySet;
import com.sun.star.bridge.XBridge;
import com.sun.star.bridge.XBridgeFactory;
import com.sun.star.comp.helper.Bootstrap;
import com.sun.star.connection.NoConnectException;
import com.sun.star.connection.XConnection;
import com.sun.star.connection.XConnector;
import com.sun.star.frame.XComponentLoader;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.ucb.XFileIdentifierConverter;
import com.sun.star.uno.Exception;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;

/**
 * The Class OPConnection.
 * @author John Zhu
 * @version
 * @since JDK1.6
 */
public abstract class OPConnection implements OPconnect {

	/** The str connection. */
	private String strConnection;

	/** The bg component. */
	private XComponent bgComponent;

	/** The service mg. */
	private XMultiComponentFactory serviceMg;

	/** The component context. */
	private XComponentContext componentContext;

	/** The bridge. */
	private XBridge bridge;

	/** The connected. */
	private boolean connected = false;

	/** The expecting disconnection. */
	private boolean expectingDisconnection = false;

	/**
	 * Instantiates a new oP connection.
	 * @param connectionStr
	 *            the connection str
	 */
	protected OPConnection(String connectionStr) {
		this.strConnection = connectionStr;
	}

	/**
	 * (non-Javadoc)
	 * @see net.heartsome.cat.converter.ooconnect.OPconnect#connect()
	 * @throws ConnectException
	 */
	public void connect() throws ConnectException {
		try {
			XComponentContext localContext;

			localContext = Bootstrap.createInitialComponentContext(null);

			XMultiComponentFactory localServiceManager = localContext.getServiceManager();
			XConnector connector = (XConnector) UnoRuntime.queryInterface(XConnector.class, localServiceManager
					.createInstanceWithContext("com.sun.star.connection.Connector", localContext)); //$NON-NLS-1$
			XConnection connection = connector.connect(strConnection);
			XBridgeFactory bridgeFactory = (XBridgeFactory) UnoRuntime.queryInterface(XBridgeFactory.class,
					localServiceManager.createInstanceWithContext("com.sun.star.bridge.BridgeFactory", localContext)); //$NON-NLS-1$
			bridge = bridgeFactory.createBridge("ms2ooBridge", "urp", connection, null); //$NON-NLS-1$ //$NON-NLS-2$
			bgComponent = (XComponent) UnoRuntime.queryInterface(XComponent.class, bridge);
			// bgComponent.addEventListener(this);
			serviceMg = (XMultiComponentFactory) UnoRuntime.queryInterface(XMultiComponentFactory.class, bridge
					.getInstance("StarOffice.ServiceManager")); //$NON-NLS-1$
			XPropertySet properties = (XPropertySet) UnoRuntime.queryInterface(XPropertySet.class, serviceMg);
			componentContext = (XComponentContext) UnoRuntime.queryInterface(XComponentContext.class, properties
					.getPropertyValue("DefaultContext")); //$NON-NLS-1$
			connected = true;
			if (connected) {
				System.out.println("has already connected"); //$NON-NLS-1$
			} else {
				System.out.println("connect to Openoffice fail,please check OpenOffice service that have to open"); //$NON-NLS-1$
			}

		} catch (NoConnectException connectException) {
			throw new ConnectException(MessageFormat.format(Messages.getString("ooconnect.OPConnection.msg"), strConnection + ": " + connectException.getMessage())); //$NON-NLS-1$ //$NON-NLS-2$
		} catch (Exception exception) {
			throw new OPException(MessageFormat.format(Messages.getString("ooconnect.OPConnection.msg"), strConnection), exception); //$NON-NLS-1$
		} catch (java.lang.Exception e) {
			if (Converter.DEBUG_MODE) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Disposing.
	 */
	public void disposing() {
		expectingDisconnection = false;
	}

	/**
	 * (non-Javadoc)
	 * @see net.heartsome.cat.converter.ooconnect.OPconnect#closeconnect()
	 */
	public synchronized void closeconnect() {
		if (expectingDisconnection) {
			System.out.print("");
		}
		expectingDisconnection = true;
		bgComponent.dispose();
	}

	/**
	 * (non-Javadoc)
	 * @see net.heartsome.cat.converter.ooconnect.OPconnect#getBridge()
	 * @return
	 */
	public XBridge getBridge() {

		return bridge;
	}

	/**
	 * (non-Javadoc)
	 * @see net.heartsome.cat.converter.ooconnect.OPconnect#getComponentContext()
	 * @return
	 */
	public XComponentContext getComponentContext() {

		return componentContext;
	}

	/**
	 * Gets the service.
	 * @param className
	 *            the class name
	 * @return the service
	 * @throws Exception
	 *             the exception
	 */
	private Object getService(String className) throws Exception {

		return serviceMg.createInstanceWithContext(className, componentContext);

	}

	/**
	 * (non-Javadoc)
	 * @see net.heartsome.cat.converter.ooconnect.OPconnect#getDesktopObject()
	 * @return
	 */
	public XComponentLoader getDesktopObject() {

		XComponentLoader xc = null;
		try {
			xc = (XComponentLoader) UnoRuntime.queryInterface(XComponentLoader.class,
					getService("com.sun.star.frame.Desktop")); //$NON-NLS-1$
		} catch (Exception e) {
			if (Converter.DEBUG_MODE) {
				e.printStackTrace();
			}
		}
		return xc;
	}

	/**
	 * (non-Javadoc)
	 * @see net.heartsome.cat.converter.ooconnect.OPconnect#getFileContentProvider()
	 * @return
	 */
	public XFileIdentifierConverter getFileContentProvider() {
		XFileIdentifierConverter xf = null;
		try {
			xf = (XFileIdentifierConverter) UnoRuntime.queryInterface(XFileIdentifierConverter.class,
					getService("com.sun.star.ucb.FileContentProvider")); //$NON-NLS-1$
		} catch (Exception e) {
			if (Converter.DEBUG_MODE) {
				e.printStackTrace();
			}
		}
		return xf;
	}

	/**
	 * (non-Javadoc)
	 * @see net.heartsome.cat.converter.ooconnect.OPconnect#getRemoteServiceManager()
	 * @return
	 */
	public XMultiComponentFactory getRemoteServiceManager() {
		return serviceMg;
	}

	/**
	 * (non-Javadoc)
	 * @see net.heartsome.cat.converter.ooconnect.OPconnect#isConnected()
	 * @return
	 */
	public boolean isConnected() {
		return connected;
	}
}

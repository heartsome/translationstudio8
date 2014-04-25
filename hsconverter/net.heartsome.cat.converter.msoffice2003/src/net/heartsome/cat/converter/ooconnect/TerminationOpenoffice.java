/**
 * TerminationOpenoffice.java
 *
 * Version information :
 *
 * Date:Jan 14, 2010
 *
 * Copyright notice :
 */
package net.heartsome.cat.converter.ooconnect;

import net.heartsome.cat.converter.Converter;

import com.sun.star.beans.XPropertySet;
import com.sun.star.bridge.XUnoUrlResolver;
import com.sun.star.frame.XDesktop;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;

/**
 * The Class TerminationOpenoffice.
 * @author John Zhu
 * @version
 * @since JDK1.6
 */
public class TerminationOpenoffice extends java.lang.Object {

	/** The at work. */
	private static boolean atWork = false;

	// public static void main(String[] args) {
	/**
	 * Close openoffice.
	 * @param port
	 *            the port
	 */
	public static void closeOpenoffice(String port) {

		XComponentContext xRemoteContext = null;
		XMultiComponentFactory xRemoteServiceManager = null;
		XDesktop xDesktop = null;

		try {

			XComponentContext xLocalContext = com.sun.star.comp.helper.Bootstrap.createInitialComponentContext(null);
			XMultiComponentFactory xLocalServiceManager = xLocalContext.getServiceManager();
			Object urlResolver = xLocalServiceManager.createInstanceWithContext(
					"com.sun.star.bridge.UnoUrlResolver", xLocalContext); //$NON-NLS-1$
			XUnoUrlResolver xUnoUrlResolver = (XUnoUrlResolver) UnoRuntime.queryInterface(XUnoUrlResolver.class,
					urlResolver);

			Object initialObject = xUnoUrlResolver
					.resolve("uno:socket,host=localhost,port=" + port + ";urp;StarOffice.ServiceManager"); //$NON-NLS-1$ //$NON-NLS-2$
			// Object initialObject =
			// xUnoUrlResolver.resolve("uno:pipe,name=my_app;urp;StarOffice.ServiceManager"
			// );

			XPropertySet xPropertySet = (XPropertySet) UnoRuntime.queryInterface(XPropertySet.class, initialObject);

			Object context = xPropertySet.getPropertyValue("DefaultContext"); //$NON-NLS-1$

			xRemoteContext = (XComponentContext) UnoRuntime.queryInterface(XComponentContext.class, context);

			xRemoteServiceManager = xRemoteContext.getServiceManager();

			// get Desktop instance

			Object desktop = xRemoteServiceManager.createInstanceWithContext(
					"com.sun.star.frame.Desktop", xRemoteContext); //$NON-NLS-1$

			xDesktop = (XDesktop) UnoRuntime.queryInterface(XDesktop.class, desktop);
			TerminateListener terminateListener = new TerminateListener();

			xDesktop.addTerminateListener(terminateListener);

			// try to terminate while we are at work
			atWork = true;
			boolean terminated = xDesktop.terminate();

			System.out.println("The Office " + //$NON-NLS-1$
					(terminated ? "has been terminated" : "is still running, we are at work")); //$NON-NLS-1$ //$NON-NLS-2$

			// no longer at work
			atWork = false;

			// once more: try to terminate
			terminated = xDesktop.terminate();

			System.out.println("The Office " + //$NON-NLS-1$
					(terminated ? "has been terminated" : //$NON-NLS-1$
							"is still running. Someone else prevents termination, e.g. the quickstarter")); //$NON-NLS-1$
		} catch (java.lang.Exception e) {
			if (Converter.DEBUG_MODE) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Checks if is at work.
	 * @return true, if is at work
	 */
	public static boolean isAtWork() {
		return atWork;
	}

	/**
	 * Gets the current component.
	 * @return the current component
	 * @throws Exception
	 *             the exception
	 */
	public static XComponent getCurrentComponent() throws Exception {
		XComponentContext xRemoteContext = com.sun.star.comp.helper.Bootstrap.createInitialComponentContext(null);
		// XComponentContext xRemoteContext =
		// com.sun.star.comp.helper.Bootstrap.bootstrap();

		XMultiComponentFactory xRemoteServiceManager = xRemoteContext.getServiceManager();

		Object desktop = xRemoteServiceManager.createInstanceWithContext("com.sun.star.frame.Desktop", xRemoteContext); //$NON-NLS-1$

		XDesktop xDesktop = (XDesktop) UnoRuntime.queryInterface(XDesktop.class, desktop);

		XComponent currentComponent = xDesktop.getCurrentComponent();

		return currentComponent;
	}
}
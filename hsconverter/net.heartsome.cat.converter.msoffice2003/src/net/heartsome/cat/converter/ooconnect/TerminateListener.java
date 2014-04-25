/**
 * TerminateListener.java
 *
 * Version information :
 *
 * Date:Jan 14, 2010
 *
 * Copyright notice :
 */
package net.heartsome.cat.converter.ooconnect;

import com.sun.star.frame.TerminationVetoException;
import com.sun.star.frame.XTerminateListener;

/**
 * The listener interface for receiving terminate events. The class that is interested in processing a terminate event
 * implements this interface, and the object created with that class is registered with a component using the
 * component's <code>addTerminateListener</code> method. When the terminate event occurs, that object's appropriate
 * method is invoked.
 * @see TerminateEvent
 */
public class TerminateListener implements XTerminateListener {

	/**
	 * (non-Javadoc).
	 * @param eventObject
	 *            the event object
	 * @see com.sun.star.frame.XTerminateListener#notifyTermination(com.sun.star.lang.EventObject)
	 */
	public void notifyTermination(com.sun.star.lang.EventObject eventObject) {
		System.out.println("about to terminate..."); //$NON-NLS-1$
	}

	/**
	 * (non-Javadoc).
	 * @param eventObject
	 *            the event object
	 * @throws TerminationVetoException
	 *             the termination veto exception
	 * @see com.sun.star.frame.XTerminateListener#queryTermination(com.sun.star.lang.EventObject)
	 */
	public void queryTermination(com.sun.star.lang.EventObject eventObject) throws TerminationVetoException {
		if (TerminationOpenoffice.isAtWork()) {
			System.out.println("Terminate while we are at work? No way!"); //$NON-NLS-1$
			throw new TerminationVetoException(); // this will veto the
			// termination,
		}
	}

	/**
	 * (non-Javadoc).
	 * @param eventObject
	 *            the event object
	 * @see com.sun.star.lang.XEventListener#disposing(com.sun.star.lang.EventObject)
	 */
	public void disposing(com.sun.star.lang.EventObject eventObject) {

	}
}
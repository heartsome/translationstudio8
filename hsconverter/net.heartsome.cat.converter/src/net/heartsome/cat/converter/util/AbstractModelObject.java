/**
 * AbstractModelObject.java
 *
 * Version information :
 *
 * Date:Jan 14, 2010
 *
 * Copyright notice :
 */
package net.heartsome.cat.converter.util;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

// Minimal JavaBeans support
/**
 * The Class AbstractModelObject.
 * @author John Zhu
 * @version
 * @since JDK1.6
 */
public abstract class AbstractModelObject {

	/** The property change support. */
	private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

	/**
	 * Adds the property change listener.
	 * @param listener
	 *            the listener
	 */
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		propertyChangeSupport.addPropertyChangeListener(listener);
	}

	/**
	 * Adds the property change listener.
	 * @param propertyName
	 *            the property name
	 * @param listener
	 *            the listener
	 */
	public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
		propertyChangeSupport.addPropertyChangeListener(propertyName, listener);
	}

	/**
	 * Removes the property change listener.
	 * @param listener
	 *            the listener
	 */
	public void removePropertyChangeListener(PropertyChangeListener listener) {
		propertyChangeSupport.removePropertyChangeListener(listener);
	}

	/**
	 * Removes the property change listener.
	 * @param propertyName
	 *            the property name
	 * @param listener
	 *            the listener
	 */
	public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
		propertyChangeSupport.removePropertyChangeListener(propertyName, listener);
	}

	/**
	 * Fire property change.
	 * @param propertyName
	 *            the property name
	 * @param oldValue
	 *            the old value
	 * @param newValue
	 *            the new value
	 */
	protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
		propertyChangeSupport.firePropertyChange(propertyName, oldValue, newValue);
	}
}

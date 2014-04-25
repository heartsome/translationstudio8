/**
 * Activator.java
 *
 * Version information :
 *
 * Date:Jan 14, 2010
 *
 * Copyright notice :
 */
package net.heartsome.cat.converter.msoffice2003;

import java.util.Properties;

import net.heartsome.cat.converter.Converter;
import net.heartsome.cat.converter.openoffice.OpenOffice2Xliff;
import net.heartsome.cat.converter.openoffice.Xliff2OpenOffice;
import net.heartsome.cat.converter.util.AndFilter;
import net.heartsome.cat.converter.util.ConverterRegister;
import net.heartsome.cat.converter.util.EqFilter;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

/**
 * The Class Activator. The activator class controls the plug-in life cycle.
 * @author John Zhu
 * @version
 * @since JDK1.6
 */
public class Activator implements BundleActivator {

	// The plug-in ID
	/** The Constant PLUGIN_ID. */
	public static final String PLUGIN_ID = "net.heartsome.cat.converter.msoffice2003";

	// The shared instance
	/** The plugin. */
	private static Activator plugin;

	/** The bundle context. */
	private static BundleContext bundleContext;

	/** The positive tracker. */
	private static ServiceTracker positiveTracker;

	/** The reverse tracker. */
	private static ServiceTracker reverseTracker;

	/**
	 * The constructor.
	 */
	public Activator() {
	}

	/**
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 * @param context
	 * @throws Exception
	 */
	public void start(BundleContext context) throws Exception {
		plugin = this;
		bundleContext = context;
		// tracker the open office converter service
		String positiveFilter = new AndFilter(new EqFilter(Constants.OBJECTCLASS, Converter.class.getName()),
				new EqFilter(Converter.ATTR_TYPE, OpenOffice2Xliff.TYPE_VALUE), new EqFilter(Converter.ATTR_DIRECTION,
						Converter.DIRECTION_POSITIVE)).toString();
		positiveTracker = new ServiceTracker(context, context.createFilter(positiveFilter),
				new OpenOfficePositiveCustomizer());
		positiveTracker.open();

		String reverseFilter = new AndFilter(new EqFilter(Constants.OBJECTCLASS, Converter.class.getName()),
				new EqFilter(Converter.ATTR_TYPE, Xliff2OpenOffice.TYPE_VALUE), new EqFilter(Converter.ATTR_DIRECTION,
						Converter.DIRECTION_REVERSE)).toString();
		reverseTracker = new ServiceTracker(context, context.createFilter(reverseFilter),
				new OpenOfficeReverseCustomize());
		reverseTracker.open();
	}

	/**
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 * @param context
	 * @throws Exception
	 */
	public void stop(BundleContext context) throws Exception {
		positiveTracker.close();
		reverseTracker.close();
		plugin = null;
		bundleContext = null;
	}

	/**
	 * Returns the shared instance.
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

	// just for test
	/**
	 * Gets the open office converter.
	 * @param direction
	 *            the direction
	 * @return the open office converter
	 */
	public static Converter getOpenOfficeConverter(String direction) {
		if (Converter.DIRECTION_POSITIVE.equals(direction)) {
			return new OpenOffice2Xliff();
		} else {
			return new Xliff2OpenOffice();
		}
	}

	/**
	 * The Class OpenOfficePositiveCustomizer.
	 * @author John Zhu
	 * @version
	 * @since JDK1.6
	 */
	private class OpenOfficePositiveCustomizer implements ServiceTrackerCustomizer {

		/**
		 * (non-Javadoc).
		 * @param reference
		 *            the reference
		 * @return the object
		 * @see org.osgi.util.tracker.ServiceTrackerCustomizer#addingService(org.osgi.framework.ServiceReference)
		 */
		public Object addingService(ServiceReference reference) {
			Converter converter = (Converter) bundleContext.getService(reference);
			Converter inx2Xliff = new MSOffice2Xliff(converter);
			Properties properties = new Properties();
			properties.put(Converter.ATTR_NAME, MSOffice2Xliff.NAME_VALUE);
			properties.put(Converter.ATTR_TYPE, MSOffice2Xliff.TYPE_VALUE);
			properties.put(Converter.ATTR_TYPE_NAME, MSOffice2Xliff.TYPE_NAME_VALUE);
			ServiceRegistration registration = ConverterRegister.registerPositiveConverter(bundleContext, inx2Xliff,
					properties);
			return registration;
		}

		/**
		 * (non-Javadoc).
		 * @param reference
		 *            the reference
		 * @param service
		 *            the service
		 * @see org.osgi.util.tracker.ServiceTrackerCustomizer#modifiedService(org.osgi.framework.ServiceReference,
		 *      java.lang.Object)
		 */
		public void modifiedService(ServiceReference reference, Object service) {

		}

		/**
		 * (non-Javadoc).
		 * @param reference
		 *            the reference
		 * @param service
		 *            the service
		 * @see org.osgi.util.tracker.ServiceTrackerCustomizer#removedService(org.osgi.framework.ServiceReference,
		 *      java.lang.Object)
		 */
		public void removedService(ServiceReference reference, Object service) {
			ServiceRegistration registration = (ServiceRegistration) service;
			registration.unregister();
			bundleContext.ungetService(reference);
		}

	}

	/**
	 * The Class OpenOfficeReverseCustomize.
	 * @author John Zhu
	 * @version
	 * @since JDK1.6
	 */
	private class OpenOfficeReverseCustomize implements ServiceTrackerCustomizer {

		/**
		 * (non-Javadoc).
		 * @param reference
		 *            the reference
		 * @return the object
		 * @see org.osgi.util.tracker.ServiceTrackerCustomizer#addingService(org.osgi.framework.ServiceReference)
		 */
		public Object addingService(ServiceReference reference) {
			Converter converter = (Converter) bundleContext.getService(reference);
			Converter xliff2Inx = new Xliff2MSOffice(converter);
			Properties properties = new Properties();
			properties.put(Converter.ATTR_NAME, Xliff2MSOffice.NAME_VALUE);
			properties.put(Converter.ATTR_TYPE, Xliff2MSOffice.TYPE_VALUE);
			properties.put(Converter.ATTR_TYPE_NAME, Xliff2MSOffice.TYPE_NAME_VALUE);
			ServiceRegistration registration = ConverterRegister.registerReverseConverter(bundleContext, xliff2Inx,
					properties);
			return registration;
		}

		/**
		 * (non-Javadoc).
		 * @param reference
		 *            the reference
		 * @param service
		 *            the service
		 * @see org.osgi.util.tracker.ServiceTrackerCustomizer#modifiedService(org.osgi.framework.ServiceReference,
		 *      java.lang.Object)
		 */
		public void modifiedService(ServiceReference reference, Object service) {

		}

		/**
		 * (non-Javadoc).
		 * @param reference
		 *            the reference
		 * @param service
		 *            the service
		 * @see org.osgi.util.tracker.ServiceTrackerCustomizer#removedService(org.osgi.framework.ServiceReference,
		 *      java.lang.Object)
		 */
		public void removedService(ServiceReference reference, Object service) {
			ServiceRegistration registration = (ServiceRegistration) service;
			registration.unregister();
			bundleContext.ungetService(reference);
		}

	}
}

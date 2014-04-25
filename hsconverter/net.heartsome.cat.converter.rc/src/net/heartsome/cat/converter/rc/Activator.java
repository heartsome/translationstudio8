/**
 * Activator.java
 *
 * Version information :
 *
 * Date:Jan 14, 2010
 *
 * Copyright notice :
 */
package net.heartsome.cat.converter.rc;

import java.util.Properties;

import net.heartsome.cat.converter.Converter;
import net.heartsome.cat.converter.util.ConverterRegister;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

/**
 * The Class Activator. The activator class controls the plug-in life cycle.
 * @author John Zhu
 * @version
 * @since JDK1.6
 */
public class Activator implements BundleActivator {

	// The plug-in ID
	/** The Constant PLUGIN_ID. */
	public static final String PLUGIN_ID = "net.heartsome.cat.converter.rc";

	// The shared instance
	/** The plugin. */
	private static Activator plugin;

	/** The rc2 xliff sr. */
	private ServiceRegistration rc2XliffSR;

	/** The xliff2 rc sr. */
	private ServiceRegistration xliff2RcSR;

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
		// register the converter services
		Converter rc2Xliff = new Rc2Xliff();
		Properties properties = new Properties();
		properties.put(Converter.ATTR_NAME, Rc2Xliff.NAME_VALUE);
		properties.put(Converter.ATTR_TYPE, Rc2Xliff.TYPE_VALUE);
		properties.put(Converter.ATTR_TYPE_NAME, Rc2Xliff.TYPE_NAME_VALUE);
		rc2XliffSR = ConverterRegister.registerPositiveConverter(context, rc2Xliff, properties);

		Converter xliff2Rc = new Xliff2Rc();
		properties = new Properties();
		properties.put(Converter.ATTR_NAME, Xliff2Rc.NAME_VALUE);
		properties.put(Converter.ATTR_TYPE, Xliff2Rc.TYPE_VALUE);
		properties.put(Converter.ATTR_TYPE_NAME, Xliff2Rc.TYPE_NAME_VALUE);
		xliff2RcSR = ConverterRegister.registerReverseConverter(context, xliff2Rc, properties);
	}

	/**
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 * @param context
	 * @throws Exception
	 */
	public void stop(BundleContext context) throws Exception {
		if (rc2XliffSR != null) {
			rc2XliffSR.unregister();
		}
		if (xliff2RcSR != null) {
			xliff2RcSR.unregister();
		}
		plugin = null;
	}

	/**
	 * Returns the shared instance.
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

}

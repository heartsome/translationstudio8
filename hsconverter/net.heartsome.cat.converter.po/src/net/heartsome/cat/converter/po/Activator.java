/**
 * Activator.java
 *
 * Version information :
 *
 * Date:Jan 14, 2010
 *
 * Copyright notice :
 */
package net.heartsome.cat.converter.po;

import java.util.Properties;

import net.heartsome.cat.converter.Converter;
import net.heartsome.cat.converter.util.ConverterRegister;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

/**
 * The Class Activator.The activator class controls the plug-in life cycle.
 * @author John Zhu
 * @version
 * @since JDK1.6
 */
public class Activator implements BundleActivator {

	// The plug-in ID
	/** The Constant PLUGIN_ID. */
	public static final String PLUGIN_ID = "net.heartsome.cat.converter.po";

	// The shared instance
	/** The plugin. */
	private static Activator plugin;

	/** The po2 xliff sr. */
	private ServiceRegistration po2XliffSR;

	/** The xliff2 po sr. */
	private ServiceRegistration xliff2PoSR;

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
		Converter po2Xliff = new Po2Xliff();
		Properties properties = new Properties();
		properties.put(Converter.ATTR_NAME, Po2Xliff.NAME_VALUE);
		properties.put(Converter.ATTR_TYPE, Po2Xliff.TYPE_VALUE);
		properties.put(Converter.ATTR_TYPE_NAME, Po2Xliff.TYPE_NAME_VALUE);
		po2XliffSR = ConverterRegister.registerPositiveConverter(context, po2Xliff, properties);

		Converter xliff2Po = new Xliff2Po();
		properties = new Properties();
		properties.put(Converter.ATTR_NAME, Xliff2Po.NAME_VALUE);
		properties.put(Converter.ATTR_TYPE, Xliff2Po.TYPE_VALUE);
		properties.put(Converter.ATTR_TYPE_NAME, Xliff2Po.TYPE_NAME_VALUE);
		xliff2PoSR = ConverterRegister.registerReverseConverter(context, xliff2Po, properties);
	}

	/**
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 * @param context
	 * @throws Exception
	 */
	public void stop(BundleContext context) throws Exception {
		if (po2XliffSR != null) {
			po2XliffSR.unregister();
		}
		if (xliff2PoSR != null) {
			xliff2PoSR.unregister();
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

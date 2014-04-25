/**
 * Activator.java
 *
 * Version information :
 *
 * Date:Jan 14, 2010
 *
 * Copyright notice :
 */
package net.heartsome.cat.converter.javaproperties;

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

	/** The Constant PLUGIN_ID. */
	public static final String PLUGIN_ID = "net.heartsome.cat.converter.javaproperties";

	/** The plugin. The shared instance */
	private static Activator plugin;

	/** The properties2 xliff sr. */
	private ServiceRegistration properties2XliffSR;

	/** The xliff2 properties sr. */
	private ServiceRegistration xliff2PropertiesSR;

	/**
	 * The constructor.
	 */
	public Activator() {
	}

	/**
	 * (non-Javadoc).
	 * @param context
	 *            the context
	 * @throws Exception
	 *             the exception
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		plugin = this;
		// register the converter services
		Converter properties2Xliff = new Properties2Xliff();
		Properties properties = new Properties();
		properties.put(Converter.ATTR_NAME, Properties2Xliff.NAME_VALUE);
		properties.put(Converter.ATTR_TYPE, Properties2Xliff.TYPE_VALUE);
		properties.put(Converter.ATTR_TYPE_NAME, Properties2Xliff.TYPE_NAME_VALUE);
		properties2XliffSR = ConverterRegister.registerPositiveConverter(context, properties2Xliff, properties);

		Converter xliff2Properties = new Xliff2Properties();
		properties = new Properties();
		properties.put(Converter.ATTR_NAME, Xliff2Properties.NAME_VALUE);
		properties.put(Converter.ATTR_TYPE, Xliff2Properties.TYPE_VALUE);
		properties.put(Converter.ATTR_TYPE_NAME, Xliff2Properties.TYPE_NAME_VALUE);
		xliff2PropertiesSR = ConverterRegister.registerReverseConverter(context, xliff2Properties, properties);
	}

	/**
	 * (non-Javadoc).
	 * @param context
	 *            the context
	 * @throws Exception
	 *             the exception
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		if (properties2XliffSR != null) {
			properties2XliffSR.unregister();
		}
		if (xliff2PropertiesSR != null) {
			xliff2PropertiesSR.unregister();
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

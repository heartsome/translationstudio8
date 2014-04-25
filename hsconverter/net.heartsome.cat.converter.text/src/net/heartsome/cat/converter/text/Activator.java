/**
 * Activator.java
 *
 * Version information :
 *
 * Date:Jan 14, 2010
 *
 * Copyright notice :
 */
package net.heartsome.cat.converter.text;

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
	public static final String PLUGIN_ID = "net.heartsome.cat.converter.text";

	// The shared instance
	/** The plugin. */
	private static Activator plugin;

	/** The text2 xliff sr. */
	private ServiceRegistration text2XliffSR;

	/** The xliff2 text sr. */
	private ServiceRegistration xliff2TextSR;

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
		// register the convert service
		Converter text2Xliff = new Text2Xliff();
		Properties properties = new Properties();
		properties.put(Converter.ATTR_NAME, Text2Xliff.NAME_VALUE);
		properties.put(Converter.ATTR_TYPE, Text2Xliff.TYPE_VALUE);
		properties.put(Converter.ATTR_TYPE_NAME, Text2Xliff.TYPE_NAME_VALUE);
		text2XliffSR = ConverterRegister.registerPositiveConverter(context, text2Xliff, properties);

		Converter xliff2Text = new Xliff2Text();
		properties = new Properties();
		properties.put(Converter.ATTR_NAME, Xliff2Text.NAME_VALUE);
		properties.put(Converter.ATTR_TYPE, Xliff2Text.TYPE_VALUE);
		properties.put(Converter.ATTR_TYPE_NAME, Xliff2Text.TYPE_NAME_VALUE);
		xliff2TextSR = ConverterRegister.registerReverseConverter(context, xliff2Text, properties);
	}

	/**
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 * @param context
	 * @throws Exception
	 */
	public void stop(BundleContext context) throws Exception {
		if (text2XliffSR != null) {
			text2XliffSR.unregister();
		}
		if (xliff2TextSR != null) {
			xliff2TextSR.unregister();
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

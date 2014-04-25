/**
 * Activator.java
 *
 * Version information :
 *
 * Date:Jan 14, 2010
 *
 * Copyright notice :
 */
package net.heartsome.cat.converter.rtf;

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
	public static final String PLUGIN_ID = "net.heartsome.cat.converter.rtf";

	// The shared instance
	/** The plugin. */
	private static Activator plugin;

	/** The rtf2 xliff sr. */
	private ServiceRegistration rtf2XliffSR;

	/** The xliff2 rtf sr. */
	private ServiceRegistration xliff2RtfSR;

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
		Converter rtf2Xliff = new Rtf2Xliff();
		Properties properties = new Properties();
		properties.put(Converter.ATTR_NAME, Rtf2Xliff.NAME_VALUE);
		properties.put(Converter.ATTR_TYPE, Rtf2Xliff.TYPE_VALUE);
		properties.put(Converter.ATTR_TYPE_NAME, Rtf2Xliff.TYPE_NAME_VALUE);
		rtf2XliffSR = ConverterRegister.registerPositiveConverter(context, rtf2Xliff, properties);

		Converter xliff2Rtf = new Xliff2Rtf();
		properties = new Properties();
		properties.put(Converter.ATTR_NAME, Xliff2Rtf.NAME_VALUE);
		properties.put(Converter.ATTR_TYPE, Xliff2Rtf.TYPE_VALUE);
		properties.put(Converter.ATTR_TYPE_NAME, Xliff2Rtf.TYPE_NAME_VALUE);
		xliff2RtfSR = ConverterRegister.registerReverseConverter(context, xliff2Rtf, properties);
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
		if (rtf2XliffSR != null) {
			rtf2XliffSR.unregister();
		}
		if (xliff2RtfSR != null) {
			xliff2RtfSR.unregister();
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

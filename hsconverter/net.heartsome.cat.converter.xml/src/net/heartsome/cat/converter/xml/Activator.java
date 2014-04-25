/**
 * Activator.java
 *
 * Version information :
 *
 * Date:Jan 14, 2010
 *
 * Copyright notice :
 */
package net.heartsome.cat.converter.xml;

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
	public static final String PLUGIN_ID = "net.heartsome.cat.converter.xml";

	// The shared instance
	/** The plugin. */
	private static Activator plugin;

	/** The xml2 xliff sr. */
	private ServiceRegistration xml2XliffSR;

	/** The xliff2 xml sr. */
	private ServiceRegistration xliff2XmlSR;

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
		// register the convert service
		Converter xml2Xliff = new Xml2Xliff();
		Properties properties = new Properties();
		properties.put(Converter.ATTR_NAME, Xml2Xliff.NAME_VALUE);
		properties.put(Converter.ATTR_TYPE, Xml2Xliff.TYPE_VALUE);
		properties.put(Converter.ATTR_TYPE_NAME, Xml2Xliff.TYPE_NAME_VALUE);
		xml2XliffSR = ConverterRegister.registerPositiveConverter(context, xml2Xliff, properties);

		Converter xliff2Xml = new Xliff2Xml();
		properties = new Properties();
		properties.put(Converter.ATTR_NAME, Xliff2Xml.NAME_VALUE);
		properties.put(Converter.ATTR_TYPE, Xliff2Xml.TYPE_VALUE);
		properties.put(Converter.ATTR_TYPE_NAME, Xliff2Xml.TYPE_NAME_VALUE);
		xliff2XmlSR = ConverterRegister.registerReverseConverter(context, xliff2Xml, properties);
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
		if (xml2XliffSR != null) {
			xml2XliffSR.unregister();
		}
		if (xliff2XmlSR != null) {
			xliff2XmlSR.unregister();
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

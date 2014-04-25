/**
 * Activator.java
 *
 * Version information :
 *
 * Date:Jan 14, 2010
 *
 * Copyright notice :
 */
package net.heartsome.cat.converter.html;

import java.util.Properties;

import net.heartsome.cat.converter.Converter;
import net.heartsome.cat.converter.util.ConverterRegister;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

/**
 * The activator class controls the plug-in life cycle.
 */
public class Activator implements BundleActivator {

	// The plug-in ID
	/** The Constant PLUGIN_ID. */
	public static final String PLUGIN_ID = "net.heartsome.cat.converter.html";

	// The shared instance
	/** The plugin. */
	private static Activator plugin;

	/** The html2 xliff sr. */
	private ServiceRegistration html2XliffSR;

	/** The xliff2 html sr. */
	private ServiceRegistration xliff2HtmlSR;

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
		// register the converter service
		Converter html2Xliff = new Html2Xliff();
		Properties properties = new Properties();
		properties.put(Converter.ATTR_NAME, Html2Xliff.NAME_VALUE);
		properties.put(Converter.ATTR_TYPE, Html2Xliff.TYPE_VALUE);
		properties.put(Converter.ATTR_TYPE_NAME, Html2Xliff.TYPE_NAME_VALUE);
		html2XliffSR = ConverterRegister.registerPositiveConverter(context, html2Xliff, properties);

		Converter xliff2Html = new Xliff2Html();
		properties = new Properties();
		properties.put(Converter.ATTR_TYPE, Xliff2Html.TYPE_VALUE);
		properties.put(Converter.ATTR_TYPE_NAME, Xliff2Html.TYPE_NAME_VALUE);
		properties.put(Converter.ATTR_NAME, Xliff2Html.NAME_VALUE);
		xliff2HtmlSR = ConverterRegister.registerReverseConverter(context, xliff2Html, properties);
	}

	/**
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 * @param context
	 * @throws Exception
	 */
	public void stop(BundleContext context) throws Exception {
		if (html2XliffSR != null) {
			html2XliffSR.unregister();
			html2XliffSR = null;
		}
		if (xliff2HtmlSR != null) {
			xliff2HtmlSR.unregister();
			xliff2HtmlSR = null;
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

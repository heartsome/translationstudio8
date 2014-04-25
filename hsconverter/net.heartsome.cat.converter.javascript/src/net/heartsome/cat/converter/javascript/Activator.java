/**
 * Activator.java
 *
 * Version information :
 *
 * Date:Jan 14, 2010
 *
 * Copyright notice :
 */
package net.heartsome.cat.converter.javascript;

import java.util.Properties;

import net.heartsome.cat.converter.Converter;
import net.heartsome.cat.converter.util.ConverterRegister;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

/**
 * The Class Activator.The activator class controls the plug-in life cycle
 * @author John Zhu
 * @version
 * @since JDK1.6
 */
public class Activator implements BundleActivator {

	// The plug-in ID
	/** The Constant PLUGIN_ID. */
	public static final String PLUGIN_ID = "net.heartsome.cat.converter.javascript";

	// The shared instance
	/** The plugin. */
	private static Activator plugin;

	/** The java script2 xliff sr. */
	private ServiceRegistration javaScript2XliffSR;

	/** The xliff2 java script sr. */
	private ServiceRegistration xliff2JavaScriptSR;

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
		Converter javaScript2Xliff = new JavaScript2Xliff();
		Properties properties = new Properties();
		properties.put(Converter.ATTR_NAME, JavaScript2Xliff.NAME_VALUE);
		properties.put(Converter.ATTR_TYPE, JavaScript2Xliff.TYPE_VALUE);
		properties.put(Converter.ATTR_TYPE_NAME, JavaScript2Xliff.TYPE_NAME_VALUE);
		javaScript2XliffSR = ConverterRegister.registerPositiveConverter(context, javaScript2Xliff, properties);

		Converter xliff2JavaScript = new Xliff2JavaScript();
		properties = new Properties();
		properties.put(Converter.ATTR_NAME, Xliff2JavaScript.TYPE_NAME_VALUE);
		properties.put(Converter.ATTR_TYPE, Xliff2JavaScript.TYPE_VALUE);
		properties.put(Converter.ATTR_TYPE_NAME, Xliff2JavaScript.TYPE_NAME_VALUE);
		xliff2JavaScriptSR = ConverterRegister.registerReverseConverter(context, xliff2JavaScript, properties);
	}

	/**
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 * @param context
	 * @throws Exception
	 */
	public void stop(BundleContext context) throws Exception {
		if (javaScript2XliffSR != null) {
			javaScript2XliffSR.unregister();
		}
		if (xliff2JavaScriptSR != null) {
			xliff2JavaScriptSR.unregister();
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

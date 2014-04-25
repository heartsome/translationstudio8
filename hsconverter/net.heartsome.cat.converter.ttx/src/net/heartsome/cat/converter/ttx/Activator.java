/**
 * Activator.java
 *
 * Version information :
 *
 * Date:Jan 14, 2010
 *
 * Copyright notice :
 */
package net.heartsome.cat.converter.ttx;

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
	public static final String PLUGIN_ID = "net.heartsome.cat.converter.ttx";

	// The shared instance
	/** The plugin. */
	private static Activator plugin;

	/** The ttx2 xliff sr. */
	private ServiceRegistration ttx2XliffSR;

	/** The xliff2 ttx sr. */
	private ServiceRegistration xliff2TtxSR;

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
		Converter ttx2Xliff = new Ttx2Xliff();
		Properties properties = new Properties();
		properties.put(Converter.ATTR_NAME, Ttx2Xliff.NAME_VALUE);
		properties.put(Converter.ATTR_TYPE, Ttx2Xliff.TYPE_VALUE);
		properties.put(Converter.ATTR_TYPE_NAME, Ttx2Xliff.TYPE_NAME_VALUE);
		ttx2XliffSR = ConverterRegister.registerPositiveConverter(context, ttx2Xliff, properties);

		Converter xliff2Ttx = new Xliff2Ttx();
		properties = new Properties();
		properties.put(Converter.ATTR_NAME, Xliff2Ttx.NAME_VALUE);
		properties.put(Converter.ATTR_TYPE, Xliff2Ttx.TYPE_VALUE);
		properties.put(Converter.ATTR_TYPE_NAME, Xliff2Ttx.TYPE_NAME_VALUE);
		xliff2TtxSR = ConverterRegister.registerReverseConverter(context, xliff2Ttx, properties);
	}

	/**
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 * @param context
	 * @throws Exception
	 */
	public void stop(BundleContext context) throws Exception {
		if (ttx2XliffSR != null) {
			ttx2XliffSR.unregister();
			ttx2XliffSR = null;
		}
		if (xliff2TtxSR != null) {
			xliff2TtxSR.unregister();
			xliff2TtxSR = null;
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

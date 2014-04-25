/**
 * Activator.java
 *
 * Version information :
 *
 * Date:Jan 14, 2010
 *
 * Copyright notice :
 */
package net.heartsome.cat.converter.mif;

import java.util.Properties;

import net.heartsome.cat.converter.Converter;
import net.heartsome.cat.converter.util.ConverterRegister;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

/**
 * The activator class controls the plug-in life cycle.
 */
public class Activator extends AbstractUIPlugin implements BundleActivator {

	// The plug-in ID
	/** The Constant PLUGIN_ID. */
	public static final String PLUGIN_ID = "net.heartsome.cat.converter.MIF";

	// The shared instance
	/** The plugin. */
	private static Activator plugin;

	/** The mif2 xliff sr. */
	@SuppressWarnings("rawtypes")
	private ServiceRegistration mif2XliffSR;

	/** The xliff2 mif sr. */
	@SuppressWarnings("rawtypes")
	private ServiceRegistration xliff2MifSR;

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
		super.start(context);
		plugin = this;
		// register the converter services
		Converter mif2Xliff = new Mif2Xliff();
		Properties properties = new Properties();
		properties.put(Converter.ATTR_NAME, Mif2Xliff.NAME_VALUE);
		properties.put(Converter.ATTR_TYPE, Mif2Xliff.TYPE_VALUE);
		properties.put(Converter.ATTR_TYPE_NAME, Mif2Xliff.TYPE_NAME_VALUE);
		mif2XliffSR = ConverterRegister.registerPositiveConverter(context, mif2Xliff, properties);

		Converter xliff2Mif = new Xliff2Mif();
		properties = new Properties();
		properties.put(Converter.ATTR_NAME, Xliff2Mif.NAME_VALUE);
		properties.put(Converter.ATTR_TYPE, Xliff2Mif.TYPE_VALUE);
		properties.put(Converter.ATTR_TYPE_NAME, Xliff2Mif.TYPE_NAME_VALUE);
		xliff2MifSR = ConverterRegister.registerReverseConverter(context, xliff2Mif, properties);
	}

	/**
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 * @param context
	 * @throws Exception
	 */
	public void stop(BundleContext context) throws Exception {
		if (mif2XliffSR != null) {
			mif2XliffSR.unregister();
		}
		if (xliff2MifSR != null) {
			xliff2MifSR.unregister();
		}
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance.
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}
}

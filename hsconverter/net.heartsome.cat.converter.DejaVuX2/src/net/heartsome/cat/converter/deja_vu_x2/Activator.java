package net.heartsome.cat.converter.deja_vu_x2;

import java.util.Properties;

import net.heartsome.cat.converter.Converter;
import net.heartsome.cat.converter.util.ConverterRegister;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator implements BundleActivator {

	// The plug-in ID
	public static final String PLUGIN_ID = "net.heartsome.cat.converter.Deja_Vu_X2"; //$NON-NLS-1$

	// The shared instance
	private static Activator plugin;
	/** deja vu x2文件转换至xliff文件的服务注册器 */
//	@SuppressWarnings("rawtypes")
	private ServiceRegistration du2XliffSR;
	/** xliff文件转换至deja vu x2文件的服务注册器 */
//	@SuppressWarnings("rawtypes")
	private ServiceRegistration xliff2DuSR;
	/**
	 * The constructor
	 */
	public Activator() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		plugin = this;
		
		Converter du2Xliff = new Du2Xliff();
		Properties properties = new Properties();
		properties.put(Converter.ATTR_NAME, Du2Xliff.NAME_VALUE);
		properties.put(Converter.ATTR_TYPE, Du2Xliff.TYPE_VALUE);
		properties.put(Converter.ATTR_TYPE_NAME, Du2Xliff.TYPE_NAME_VALUE);
		du2XliffSR = ConverterRegister.registerPositiveConverter(context, du2Xliff, properties);
		
		Converter xliff2Du = new Xliff2Du();
		properties = new Properties();
		properties.put(Converter.ATTR_NAME, Xliff2Du.NAME_VALUE);
		properties.put(Converter.ATTR_TYPE, Xliff2Du.TYPE_VALUE);
		properties.put(Converter.ATTR_TYPE_NAME, Xliff2Du.TYPE_NAME_VALUE);
		xliff2DuSR = ConverterRegister.registerReverseConverter(context, xliff2Du, properties);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		if (du2XliffSR != null) {
			du2XliffSR.unregister();
			du2XliffSR = null;
		}
		if (xliff2DuSR != null) {
			xliff2DuSR.unregister();
			xliff2DuSR = null;
		}
		plugin = null;
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

}

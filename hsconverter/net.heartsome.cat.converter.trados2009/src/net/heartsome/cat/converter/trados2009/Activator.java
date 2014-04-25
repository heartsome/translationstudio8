package net.heartsome.cat.converter.trados2009;

import java.util.Properties;

import net.heartsome.cat.converter.Converter;
import net.heartsome.cat.converter.util.ConverterRegister;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

/**
 * The activator class controls the plug-in life cycle
 * trados 2009 转换器的 Activator。 --robert 2012-06-27
 */
public class Activator implements BundleActivator {

	// The plug-in ID
	public static final String PLUGIN_ID = "net.heartsome.cat.converter.trados2009"; //$NON-NLS-1$

	// The shared instance
	private static Activator plugin;
	/** trados 2009文件转换至xliff文件的服务注册器 */
//	@SuppressWarnings("rawtypes")
	private ServiceRegistration sdl2XliffSR;
	/** xliff文件转换至trados 2009文件的服务注册器 */
//	@SuppressWarnings("rawtypes")
	private ServiceRegistration xliff2SdlSR;
	
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

		Converter sdl2Xliff = new Sdl2Xliff();
		Properties properties = new Properties();
		properties.put(Converter.ATTR_NAME, Sdl2Xliff.NAME_VALUE);
		properties.put(Converter.ATTR_TYPE, Sdl2Xliff.TYPE_VALUE);
		properties.put(Converter.ATTR_TYPE_NAME, Sdl2Xliff.TYPE_NAME_VALUE);
		sdl2XliffSR = ConverterRegister.registerPositiveConverter(context, sdl2Xliff, properties);
		
		Converter xliff2Sdl = new Xliff2Sdl();
		properties = new Properties();
		properties.put(Converter.ATTR_NAME, Xliff2Sdl.NAME_VALUE);
		properties.put(Converter.ATTR_TYPE, Xliff2Sdl.TYPE_VALUE);
		properties.put(Converter.ATTR_TYPE_NAME, Xliff2Sdl.TYPE_NAME_VALUE);
		xliff2SdlSR = ConverterRegister.registerReverseConverter(context, xliff2Sdl, properties);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		if (sdl2XliffSR != null) {
			sdl2XliffSR.unregister();
			sdl2XliffSR = null;
		}
		if (xliff2SdlSR != null) {
			xliff2SdlSR.unregister();
			xliff2SdlSR = null;
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

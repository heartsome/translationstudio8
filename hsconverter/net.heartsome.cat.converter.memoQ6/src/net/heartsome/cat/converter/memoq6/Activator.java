package net.heartsome.cat.converter.memoq6;

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
	public static final String PLUGIN_ID = "net.heartsome.cat.converter.memoQ6"; //$NON-NLS-1$

	// The shared instance
	private static Activator plugin;

	/** trados 2009文件转换至xliff文件的服务注册器 */
//	@SuppressWarnings("rawtypes")
	private ServiceRegistration mq2XliffSR;
	/** xliff文件转换至trados 2009文件的服务注册器 */
//	@SuppressWarnings("rawtypes")
	private ServiceRegistration xliff2MqSR;
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
		
		Converter mq2Xliff = new Mq2Xliff();
		Properties properties = new Properties();
		properties.put(Converter.ATTR_NAME, Mq2Xliff.NAME_VALUE);
		properties.put(Converter.ATTR_TYPE, Mq2Xliff.TYPE_VALUE);
		properties.put(Converter.ATTR_TYPE_NAME, Mq2Xliff.TYPE_NAME_VALUE);
		mq2XliffSR = ConverterRegister.registerPositiveConverter(context, mq2Xliff, properties);
		
		Converter xliff2Mq = new Xliff2Mq();
		properties = new Properties();
		properties.put(Converter.ATTR_NAME, Xliff2Mq.NAME_VALUE);
		properties.put(Converter.ATTR_TYPE, Xliff2Mq.TYPE_VALUE);
		properties.put(Converter.ATTR_TYPE_NAME, Xliff2Mq.TYPE_NAME_VALUE);
		xliff2MqSR = ConverterRegister.registerReverseConverter(context, xliff2Mq, properties);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		if (mq2XliffSR != null) {
			mq2XliffSR.unregister();
			mq2XliffSR = null;
		}
		if (xliff2MqSR != null) {
			xliff2MqSR.unregister();
			xliff2MqSR = null;
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

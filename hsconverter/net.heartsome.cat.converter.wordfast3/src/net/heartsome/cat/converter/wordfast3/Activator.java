package net.heartsome.cat.converter.wordfast3;

import java.util.Properties;

import net.heartsome.cat.converter.Converter;
import net.heartsome.cat.converter.util.ConverterRegister;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

/**
 * Wordfast Pro 双语 XML 文件 (TXML)	Wordfast Pro bilingual XML file (TXML)
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "net.heartsome.cat.converter.wordfast3"; //$NON-NLS-1$

	// The shared instance
	private static Activator plugin;

	/** wordFast3文件转换至xliff文件的服务注册器 */
	@SuppressWarnings("rawtypes")
	private ServiceRegistration wf2XliffSR;
	/** xliff文件转换至wordFast3文件的服务注册器 */
	@SuppressWarnings("rawtypes")
	private ServiceRegistration xliff2WfSR;
	
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
		
		Converter wf2Xliff = new Wf2Xliff();
		Properties properties = new Properties();
		properties.put(Converter.ATTR_NAME, Wf2Xliff.NAME_VALUE);
		properties.put(Converter.ATTR_TYPE, Wf2Xliff.TYPE_VALUE);
		properties.put(Converter.ATTR_TYPE_NAME, Wf2Xliff.TYPE_NAME_VALUE);
		wf2XliffSR = ConverterRegister.registerPositiveConverter(context, wf2Xliff, properties);
		
		Converter xliff2Wf = new Xliff2Wf();
		properties = new Properties();
		properties.put(Converter.ATTR_NAME, Xliff2Wf.NAME_VALUE);
		properties.put(Converter.ATTR_TYPE, Xliff2Wf.TYPE_VALUE);
		properties.put(Converter.ATTR_TYPE_NAME, Xliff2Wf.TYPE_NAME_VALUE);
		xliff2WfSR = ConverterRegister.registerReverseConverter(context, xliff2Wf, properties);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		if (wf2XliffSR != null) {
			wf2XliffSR.unregister();
			wf2XliffSR = null;
		}
		if (xliff2WfSR != null) {
			xliff2WfSR.unregister();
			xliff2WfSR = null;
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

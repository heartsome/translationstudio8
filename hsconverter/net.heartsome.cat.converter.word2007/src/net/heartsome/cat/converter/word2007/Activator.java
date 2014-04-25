package net.heartsome.cat.converter.word2007;

import java.util.Properties;

import net.heartsome.cat.converter.Converter;
import net.heartsome.cat.converter.util.ConverterRegister;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

/**
 * The activator class controls the plug-in life cycle
 * word 2007 转换器的 Activator。 --robert 2012-08-28
 */
public class Activator extends AbstractUIPlugin implements BundleActivator {

	// The plug-in ID
	public static final String PLUGIN_ID = "net.heartsome.cat.converter.trados2009"; //$NON-NLS-1$

	// The shared instance
	private static Activator plugin;
	/** trados 2009文件转换至xliff文件的服务注册器 */
	@SuppressWarnings("rawtypes")
	private ServiceRegistration docx2XliffSR;
	/** xliff文件转换至trados 2009文件的服务注册器 */
	@SuppressWarnings("rawtypes")
	private ServiceRegistration xliff2DocxSR;
	
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

		Converter docx2Xliff = new Docx2Xliff();
		Properties properties = new Properties();
		properties.put(Converter.ATTR_NAME, Docx2Xliff.NAME_VALUE);
		properties.put(Converter.ATTR_TYPE, Docx2Xliff.TYPE_VALUE);
		properties.put(Converter.ATTR_TYPE_NAME, Docx2Xliff.TYPE_NAME_VALUE);
		docx2XliffSR = ConverterRegister.registerPositiveConverter(context, docx2Xliff, properties);
		
		Converter xliff2Docx = new Xliff2Docx();
		properties = new Properties();
		properties.put(Converter.ATTR_NAME, Xliff2Docx.NAME_VALUE);
		properties.put(Converter.ATTR_TYPE, Xliff2Docx.TYPE_VALUE);
		properties.put(Converter.ATTR_TYPE_NAME, Xliff2Docx.TYPE_NAME_VALUE);
		xliff2DocxSR = ConverterRegister.registerReverseConverter(context, xliff2Docx, properties);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		if (docx2XliffSR != null) {
			docx2XliffSR.unregister();
			docx2XliffSR = null;
		}
		if (xliff2DocxSR != null) {
			xliff2DocxSR.unregister();
			xliff2DocxSR = null;
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

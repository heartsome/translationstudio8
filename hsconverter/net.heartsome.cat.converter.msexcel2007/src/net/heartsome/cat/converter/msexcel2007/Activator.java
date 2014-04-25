package net.heartsome.cat.converter.msexcel2007;

import java.util.Properties;

import net.heartsome.cat.converter.Converter;
import net.heartsome.cat.converter.util.ConverterRegister;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

public class Activator extends AbstractUIPlugin implements BundleActivator {

	public static final String PLUGIN_ID = "net.heartsome.cat.converter.msexcel2007";

	private ServiceRegistration<?> excel2XLIFFSR;

	private ServiceRegistration<?> xliff2excelSR;

	private static BundleContext context;
	
	private static Activator plugin;

	static BundleContext getContext() {
		return context;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext bundleContext) throws Exception {
		super.start(bundleContext);
		Activator.context = bundleContext;

		Converter excel2xliff = new MsExcel2Xliff();
		Properties properties = new Properties();
		properties.put(Converter.ATTR_NAME, MsExcel2Xliff.NAME_VALUE);
		properties.put(Converter.ATTR_TYPE, MsExcel2Xliff.TYPE_VALUE);
		properties.put(Converter.ATTR_TYPE_NAME, MsExcel2Xliff.TYPE_NAME_VALUE);
		excel2XLIFFSR = ConverterRegister.registerPositiveConverter(context, excel2xliff, properties);

		Converter xliff2excel = new Xliff2MsExcel();
		properties = new Properties();
		properties.put(Converter.ATTR_NAME, Xliff2MsExcel.NAME_VALUE);
		properties.put(Converter.ATTR_TYPE, Xliff2MsExcel.TYPE_VALUE);
		properties.put(Converter.ATTR_TYPE_NAME, Xliff2MsExcel.TYPE_NAME_VALUE);
		xliff2excelSR = ConverterRegister.registerReverseConverter(context, xliff2excel, properties);
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext bundleContext) throws Exception {
		plugin = null;
		if (excel2XLIFFSR != null) {
			excel2XLIFFSR.unregister();
			excel2XLIFFSR = null;
		}

		if (xliff2excelSR != null) {
			xliff2excelSR.unregister();
			xliff2excelSR = null;
		}

		Activator.context = null;
		super.stop(bundleContext);
	}
	
	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}
	
	public static Activator getDefault() {
		return plugin;
	}

}

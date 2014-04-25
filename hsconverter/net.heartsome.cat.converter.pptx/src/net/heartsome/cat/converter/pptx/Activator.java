package net.heartsome.cat.converter.pptx;

import java.util.Properties;

import net.heartsome.cat.converter.Converter;
import net.heartsome.cat.converter.util.ConverterRegister;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

public class Activator extends AbstractUIPlugin implements BundleActivator {
	
	/** The Constant PLUGIN_ID. */
	public static final String PLUGIN_ID = "net.heartsome.cat.converter.pptx";

	private static BundleContext context;
	
	@SuppressWarnings("rawtypes")
	private ServiceRegistration pptx2XLIFFSR;
	
	@SuppressWarnings("rawtypes")
	private ServiceRegistration xliff2PPTXSR;
	
	private static Activator plugin;
	
	public Activator() {
	}

	static BundleContext getContext() {
		return context;
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext bundleContext) throws Exception {
		super.start(bundleContext);
		Activator.context = bundleContext;
		
		Converter pptx2XLIFF = new PPTX2XLIFF();
		Properties properties = new Properties();
		properties.put(Converter.ATTR_NAME, PPTX2XLIFF.NAME_VALUE);
		properties.put(Converter.ATTR_TYPE, PPTX2XLIFF.TYPE_VALUE);
		properties.put(Converter.ATTR_TYPE_NAME, PPTX2XLIFF.TYPE_NAME_VALUE);
		pptx2XLIFFSR = ConverterRegister.registerPositiveConverter(context, pptx2XLIFF, properties);
		
		Converter xliff2PPTX = new XLIFF2PPTX();
		properties = new Properties();
		properties.put(Converter.ATTR_NAME, XLIFF2PPTX.NAME_VALUE);
		properties.put(Converter.ATTR_TYPE, XLIFF2PPTX.TYPE_VALUE);
		properties.put(Converter.ATTR_TYPE_NAME, XLIFF2PPTX.TYPE_NAME_VALUE);
		xliff2PPTXSR = ConverterRegister.registerReverseConverter(context, xliff2PPTX, properties);
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext bundleContext) throws Exception {
		plugin = null;
		if (pptx2XLIFFSR != null) {
			pptx2XLIFFSR.unregister();
			pptx2XLIFFSR = null;
		}
		if (xliff2PPTXSR != null) {
			xliff2PPTXSR.unregister();
			xliff2PPTXSR = null;
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

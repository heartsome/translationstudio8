package net.heartsome.cat.converter.idml;

import java.util.Properties;

import net.heartsome.cat.converter.Converter;
import net.heartsome.cat.converter.util.ConverterRegister;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

public class Activator implements BundleActivator {
	
	/** The Constant PLUGIN_ID. */
	public static final String PLUGIN_ID = "net.heartsome.cat.converter.idml";

	private static BundleContext context;
	
	@SuppressWarnings("rawtypes")
	private ServiceRegistration idml2XLIFFSR;
	
	@SuppressWarnings("rawtypes")
	private ServiceRegistration xliff2IDMLSR;

	static BundleContext getContext() {
		return context;
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext bundleContext) throws Exception {
		Activator.context = bundleContext;
		
		Converter idml2XLIFF = new IDML2XLIFF();
		Properties properties = new Properties();
		properties.put(Converter.ATTR_NAME, IDML2XLIFF.NAME_VALUE);
		properties.put(Converter.ATTR_TYPE, IDML2XLIFF.TYPE_VALUE);
		properties.put(Converter.ATTR_TYPE_NAME, IDML2XLIFF.TYPE_NAME_VALUE);
		idml2XLIFFSR = ConverterRegister.registerPositiveConverter(context, idml2XLIFF, properties);
		
		Converter xliff2IDML = new XLIFF2IDML();
		properties = new Properties();
		properties.put(Converter.ATTR_NAME, XLIFF2IDML.NAME_VALUE);
		properties.put(Converter.ATTR_TYPE, XLIFF2IDML.TYPE_VALUE);
		properties.put(Converter.ATTR_TYPE_NAME, XLIFF2IDML.TYPE_NAME_VALUE);
		xliff2IDMLSR = ConverterRegister.registerReverseConverter(context, xliff2IDML, properties);
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext bundleContext) throws Exception {
		if (idml2XLIFFSR != null) {
			idml2XLIFFSR.unregister();
			idml2XLIFFSR = null;
		}
		if (xliff2IDMLSR != null) {
			xliff2IDMLSR.unregister();
			xliff2IDMLSR = null;
		}
		Activator.context = null;
	}
}

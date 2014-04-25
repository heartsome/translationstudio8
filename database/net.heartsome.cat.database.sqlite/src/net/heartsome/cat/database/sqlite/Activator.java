package net.heartsome.cat.database.sqlite;

import net.heartsome.cat.database.DBServiceProvider;

import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

public class Activator extends Plugin {

	/** The plug-in ID */
	public static final String PLUGIN_ID = "net.heartsome.cat.database.sqlite";

	/** The shared instance */
	private static Activator plugin;
	
	private ServiceRegistration<DBServiceProvider> sqliteServiceRegistration;

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
		super.start(context);
		plugin = this;
		
		DbServiceProviderImpl service = new DbServiceProviderImpl();
		sqliteServiceRegistration = context.registerService(DBServiceProvider.class, service, null);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
		
		sqliteServiceRegistration.unregister();
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

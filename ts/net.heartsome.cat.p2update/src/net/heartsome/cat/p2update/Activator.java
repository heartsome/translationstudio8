package net.heartsome.cat.p2update;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.equinox.internal.p2.core.helpers.ServiceHelper;
import org.eclipse.equinox.p2.core.IProvisioningAgent;
import org.eclipse.equinox.p2.repository.artifact.IArtifactRepositoryManager;
import org.eclipse.equinox.p2.repository.metadata.IMetadataRepositoryManager;
import org.eclipse.equinox.p2.ui.Policy;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	public static final Logger logger = LoggerFactory.getLogger(Activator.class);

	// The plug-in ID
	public static final String PLUGIN_ID = "net.heartsome.cat.ts.ui.p2update"; //$NON-NLS-1$

	// The shared instance
	private static Activator plugin;
	public static BundleContext bundleContext;

	@SuppressWarnings("rawtypes")
	ServiceRegistration policyRegistration;
	UpdatePolicy policy;

	/**
	 * The constructor
	 */
	public Activator() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		bundleContext = context;

		// register the p2 UI policy
		registerP2Policy(context);
		loadUpdateSite();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		bundleContext = null;

		// unregister the UI policy
		policyRegistration.unregister();
		policyRegistration = null;

		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

	private void registerP2Policy(BundleContext context) {
		policy = new UpdatePolicy();
		policy.updateForPreferences();
		policyRegistration = context.registerService(Policy.class.getName(), policy, null);
	}

	private void loadUpdateSite() throws InvocationTargetException {
		// get the agent
		final IProvisioningAgent agent = (IProvisioningAgent) ServiceHelper.getService(Activator.bundleContext,
				IProvisioningAgent.SERVICE_NAME);

		// get the repository managers and define our repositories
		IMetadataRepositoryManager manager = (IMetadataRepositoryManager) agent
				.getService(IMetadataRepositoryManager.SERVICE_NAME);
		if (manager == null) {
			logger.error("When load repository,Metadata manager was null");
			return;
		}

		// Load artifact manager
		IArtifactRepositoryManager artifactManager = (IArtifactRepositoryManager) agent
				.getService(IArtifactRepositoryManager.SERVICE_NAME);
		if (artifactManager == null) {
			logger.error("When load repository,Artifact manager was null");
			return;
		}

		// Load repository
		try {
			String url = getUrlString();
			if (url == null) {
				return;
			}
			URI repoLocation = new URI(url);
			URI[] ex = manager.getKnownRepositories(IMetadataRepositoryManager.REPOSITORIES_ALL);
			for(URI e : ex){
				manager.removeRepository(e);
				artifactManager.removeRepository(e);
			}
			manager.addRepository(repoLocation);
			artifactManager.addRepository(repoLocation);
		} catch (URISyntaxException e) {
			logger.error("Caught URI syntax exception " + e.getMessage(), e);
			throw new InvocationTargetException(e);
		}
	}

	private String getUrlString() {	
		String version = System.getProperty("TSEdition");
		SAXReader saxReader = new SAXReader();
		Document document = null;
		try {
			String siteFile = getUpdateSiteFile();
			if (siteFile == null) {
				logger.error("Can not get the update site config file");
				return null;
			}
			document = saxReader.read(new File(siteFile));
			Element root = document.getRootElement();
			Iterator<?> it = root.elementIterator("site");
			String name;
			while (it.hasNext()) {
				Element e = (Element) it.next();
				name = e.attributeValue("name");
				if (name.equals(version))
					return e.attributeValue("url");
			}
		} catch (Exception e) {
			logger.error("", e);
		}
		return null;
	}

	private String getUpdateSiteFile() {
		try {
			String bundlePath = FileLocator.toFileURL(Activator.getDefault().getBundle().getEntry("")).getPath();
			return bundlePath + "/configuration/p2config.xml";
		} catch (IOException e) {
			logger.error("", e);
		}
		return null;
	}
}

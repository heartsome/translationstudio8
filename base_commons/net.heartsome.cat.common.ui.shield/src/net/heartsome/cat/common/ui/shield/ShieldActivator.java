package net.heartsome.cat.common.ui.shield;

import java.io.File;
import java.net.URL;
import java.text.MessageFormat;

import net.heartsome.cat.common.ui.shield.resource.Messages;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *此插件的 Activator 类
 * @author cheney
 * @since JDK1.6
 */
public class ShieldActivator extends AbstractUIPlugin {

	public final static String PLUGIN_ID = "net.heartsome.cat.common.ui.shield";

	private final static Logger LOGGER = LoggerFactory.getLogger(ShieldActivator.class);

	private static ShieldActivator plugin;

	public ShieldActivator() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 * @return the shared instance
	 */
	public static ShieldActivator getDefault() {
		return plugin;
	}

	/**
	 * 根据相对于插件根目录的相对路径，在插件中查找相应的文件，查找成功则返回对应的文件对象
	 * @param relativePath
	 *            对于插件根目录的相对路径
	 * @return 查找成功则返回对应的文件对象，否则返回 NULL;
	 */
	public static File getFile(String relativePath) {
		File file = null;
		URL langCodesURL = plugin.getBundle().getEntry(relativePath);
		if (langCodesURL != null) {
			try {
				file = new File(FileLocator.toFileURL(langCodesURL).getPath());
			} catch (Exception e) {
				if (LOGGER.isErrorEnabled()) {
					String msg = Messages.getString("shield.ShieldActivator.logger1");
					Object[] args = { relativePath };
					LOGGER.error(new MessageFormat(msg).format(args), e);
				}
			}
		}
		return file;
	}

}

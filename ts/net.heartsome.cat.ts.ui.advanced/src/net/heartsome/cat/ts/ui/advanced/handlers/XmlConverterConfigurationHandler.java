package net.heartsome.cat.ts.ui.advanced.handlers;

import java.io.File;

import net.heartsome.cat.common.resources.ResourceUtils;
import net.heartsome.cat.ts.ui.advanced.ADConstants;
import net.heartsome.cat.ts.ui.advanced.dialogs.XmlConverterConfigurationDialog;
import net.heartsome.cat.ts.ui.advanced.resource.Messages;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 配置XML转换器的handler类
 * @author robert 2012-02-22
 * @version
 * @since JDK1.6
 */
public class XmlConverterConfigurationHandler extends AbstractHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(XmlConverterConfigurationHandler.class);

	private IWorkspaceRoot root;

	public Object execute(ExecutionEvent event) throws ExecutionException {
		root = ResourcesPlugin.getWorkspace().getRoot();
		String configFileLocation = root.getLocation().append(ADConstants.AD_xmlConverterConfigFolder).toOSString();
		Shell shell = HandlerUtil.getActiveShell(event);
		// 首先验证安装文件中本次所需要的文件是否转存至工作工间，如果没有，就转过去。
		File xmlConfigFolderFile = new File(configFileLocation);
		String tgtLocation = root.getLocation().append(ADConstants.AD_xmlConverterConfigFolder).toOSString();
		// 如果不存在，则将安装文件中的相关配置文件复制到工作工间
		if (!xmlConfigFolderFile.exists() || !xmlConfigFolderFile.isDirectory()
				|| new File(tgtLocation).list().length <= 0) {
			String srcLocation = Platform.getConfigurationLocation().getURL().getPath() + "net.heartsome.cat.converter"
					+ System.getProperty("file.separator") + "ini";
			try {
				ResourceUtils.copyDirectory(new File(srcLocation), new File(tgtLocation));
			} catch (Exception e) {
				LOGGER.error("", e);
			}

			File _xmlConfigFolderFile = new File(configFileLocation);
			if (!_xmlConfigFolderFile.exists() || !_xmlConfigFolderFile.isDirectory()) {
				MessageDialog.openInformation(shell,
						Messages.getString("handlers.XmlConverterConfigurationHandler.msgTitle"),
						Messages.getString("handlers.XmlConverterConfigurationHandler.msg"));
				return null;
			}

		}
		XmlConverterConfigurationDialog dialog = new XmlConverterConfigurationDialog(shell, configFileLocation);
		dialog.open();
		return null;
	}

}

package net.heartsome.cat.ts.ui.advanced.handlers;

import java.io.File;

import net.heartsome.cat.common.resources.ResourceUtils;
import net.heartsome.cat.ts.ui.advanced.ADConstants;
import net.heartsome.cat.ts.ui.advanced.dialogs.CatalogManagerDialog;
import net.heartsome.cat.ts.ui.advanced.resource.Messages;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * 配置目录管理的handler
 * @author robert 2012-02-17
 * @version
 * @since JDK1.6
 */
public class CatalogManagerHandler extends AbstractHandler {
	private IWorkspaceRoot root;
	public Object execute(ExecutionEvent event) throws ExecutionException {
		root = ResourcesPlugin.getWorkspace().getRoot();
		//先检查目录管理器配置所需要的文件是否都存在于工作空间
		File catalogXmlFile = root.getLocation().append(ADConstants.catalogueXmlPath).toFile();
		// 如果不存在，就将net.heartsome.cat.ts.configurationfile.feature插件的net.heartsome.cat.converter里的catalogue.xml拷到工作空间
		if (!catalogXmlFile.exists() || new File(catalogXmlFile.getParent()).list().length <= 0) {	
			//这是产品打包后，catalogue.xml所在的路径
			String srcLocation = Platform.getConfigurationLocation().getURL().getPath()
					+ "net.heartsome.cat.converter"
					+ System.getProperty("file.separator") + "catalogue" + System.getProperty("file.separator")
					+ "catalogue.xml";
			String tagLoaction = catalogXmlFile.getParent();
			
			try {
				ResourceUtils.copyDirectory(new File(srcLocation).getParentFile(), new File(tagLoaction));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		catalogXmlFile = root.getLocation().append(ADConstants.catalogueXmlPath).toFile();
		if (!catalogXmlFile.exists()) {
			MessageDialog.openInformation(HandlerUtil.getActiveSite(event).getShell(), Messages.getString("handlers.CatalogManagerHandler.msgTitle"), Messages.getString("handlers.CatalogManagerHandler.msg"));
			return null;
		}
		
		CatalogManagerDialog dialog = new CatalogManagerDialog(HandlerUtil.getActiveSite(event).getShell(), root);
		dialog.open();
		return null;
	}

}

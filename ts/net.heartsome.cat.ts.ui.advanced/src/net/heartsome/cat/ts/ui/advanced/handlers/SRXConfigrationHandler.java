package net.heartsome.cat.ts.ui.advanced.handlers;

import net.heartsome.cat.ts.ui.advanced.dialogs.srx.SrxConfigurationDialog;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * 高级菜单下配置分段规则子菜单的handler
 * @author robert 2012-02-28
 * @version
 * @since JDK1.6
 */
public class SRXConfigrationHandler extends AbstractHandler {
	public Object execute(ExecutionEvent event) throws ExecutionException {
//		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		Shell shell = HandlerUtil.getActiveShell(event);
//		String srxConfigLocation = ADConstants.configLocation + File.separator + ADConstants.AD_SRXConfigFolder;
//		// 首先验证本次所要用到的SRX（分段规则）文件是否存在于工作空间中。如果没有，那么将相关文件拷贝到指定工作空间的目录
//		File srxConfigFolderFile = new File(srxConfigLocation);
//		// 如果不存在，则将安装文件中的相关配置文件复制到工作工间
//		if (!srxConfigFolderFile.exists() || !srxConfigFolderFile.isDirectory()
//				|| new File(srxConfigLocation).list().length <= 0) {
//			String srcLocation = Platform.getConfigurationLocation().getURL().getPath() + "net.heartsome.cat.converter"
//					+ System.getProperty("file.separator") + "srx";
//			try {
//				ResourceUtils.copyDirectory(new File(srcLocation), new File(srxConfigLocation));
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//
//			File _srxConfigFolderFile = new File(srxConfigLocation);
//			if (!_srxConfigFolderFile.exists() || !_srxConfigFolderFile.isDirectory()) {
//				MessageDialog.openInformation(shell, Messages.getString("handlers.SRXConfigrationHandler.msgTitle"),
//						Messages.getString("handlers.SRXConfigrationHandler.msg"));
//				return null;
//			}
//		}

		SrxConfigurationDialog dialog = new SrxConfigurationDialog(shell);
		dialog.open();

		return null;
	}
}

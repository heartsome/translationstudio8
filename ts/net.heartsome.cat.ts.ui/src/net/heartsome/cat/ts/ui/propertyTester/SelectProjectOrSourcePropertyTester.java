package net.heartsome.cat.ts.ui.propertyTester;

import java.util.List;

import net.heartsome.cat.common.core.Constant;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

/**
 * 验证所选是否是项目，Source 文件夹文件。
 * @author peason
 * @version
 * @since JDK1.6
 */
public class SelectProjectOrSourcePropertyTester extends PropertyTester {

	public static final String PROPERTY_NAMESPACE = "navigatorPopup";
	public static final String PROPERTY_ENABLED = "projectOrSource";

	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if (window != null) {
			IWorkbenchPage page = window.getActivePage();
			if (page != null) {
				if (page.getActivePartReference() == null) {
					return false;
				}
				String partId = page.getActivePartReference().getId();
				if (partId.equals("net.heartsome.cat.common.ui.navigator.view")) {
					// 导航视图处于激活状态
					IViewPart viewPart = page.findView("net.heartsome.cat.common.ui.navigator.view");
					StructuredSelection selection = (StructuredSelection) viewPart.getSite().getSelectionProvider()
							.getSelection();
					if (selection != null && !selection.isEmpty() && selection instanceof IStructuredSelection) {
						List<?> lstObj = ((IStructuredSelection) selection).toList();
						for (Object obj : lstObj) {
							if (obj instanceof IFile) {
								IFile file = (IFile) obj;
								String xlfFolderPath = file.getProject().getFullPath().append(Constant.FOLDER_SRC)
										.toOSString();
								if (!file.getFullPath().toOSString().startsWith(xlfFolderPath)) {
									return false;
								}
							} else if (obj instanceof IFolder) {
								IFolder folder = (IFolder) obj;
								String xlfFolderPath = folder.getProject().getFullPath().append(Constant.FOLDER_SRC)
										.toOSString();
								if (!folder.getFullPath().toOSString().startsWith(xlfFolderPath)) {
									return false;
								}
							}
						}
					} else {
						return false;
					}
				} else {
					return false;
				}
			} else {
				return false;
			}
		} else {
			return false;
		}
		return true;
	}

}

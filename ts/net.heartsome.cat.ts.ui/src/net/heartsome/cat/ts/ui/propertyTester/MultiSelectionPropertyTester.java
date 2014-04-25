package net.heartsome.cat.ts.ui.propertyTester;

import java.util.ArrayList;
import java.util.List;

import net.heartsome.cat.common.core.Constant;
import net.heartsome.cat.common.resources.ResourceUtils;
import net.heartsome.cat.common.util.CommonFunction;
import net.heartsome.cat.ts.ui.resource.Messages;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 针对合并打开的选择
 * @author robert	2012-06-15
 */
public class MultiSelectionPropertyTester extends PropertyTester{
	public static final String PROPERTY_NAMESPACE = "navigatorPopup";
	public static final String PROPERTY_ENABLED = "multiSelection";
	public final static Logger logger = LoggerFactory.getLogger(MultiSelectionPropertyTester.class.getName());
	
	public boolean test(Object receiver, String property, Object[] args,
			Object expectedValue) {
		IProject curProject = null;
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
					ArrayList<IFile> selectedIFileList = new ArrayList<IFile>();
					if (selection != null && !selection.isEmpty() && selection instanceof IStructuredSelection) {
						List<?> lstObj = ((IStructuredSelection) selection).toList();
						for (Object obj : lstObj) {
							if (obj instanceof IFile) {
								IFile file = (IFile) obj;
								// Linux 下的文本文件无扩展名，因此要先判断扩展名是否为空
								if (file.getFileExtension() == null || !CommonFunction.validXlfExtension(file.getFileExtension())) {
									return false;
								} else {
									String xlfFolderPath = file.getProject().getFullPath().append(Constant.FOLDER_XLIFF).toOSString();
									if (!file.getFullPath().toOSString().startsWith(xlfFolderPath)) {
										return false;
									}
									if (curProject == null) {
										curProject = file.getProject();
									}else {
										if (curProject != file.getProject()) {
											return false;
										}
									}
									selectedIFileList.add(file);
								}
							} else if (obj instanceof IFolder) {
								IFolder folder = (IFolder) obj;
								String xlfFolderPath = folder.getProject().getFullPath().append(Constant.FOLDER_XLIFF).toOSString();
								if (!folder.getFullPath().toOSString().startsWith(xlfFolderPath)) {
									return false;
								}
								try {
									ResourceUtils.getXliffs(folder, selectedIFileList);
								} catch (CoreException e) {
									logger.error(Messages.getString("propertyTester.MultiSelectionPropertyTester.logger1"), folder.getFullPath().toOSString(), e);
								}
								if (curProject == null) {
									curProject = folder.getProject();
								}else {
									if (curProject != folder.getProject()) {
										return false;
									}
								}
							}
						}
						CommonFunction.removeRepeateSelect(selectedIFileList);
						if (selectedIFileList.size() < 2) {
							return false;
						}
					} else {
						return false;
					}
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

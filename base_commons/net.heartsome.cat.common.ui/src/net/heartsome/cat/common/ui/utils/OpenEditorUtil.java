package net.heartsome.cat.common.ui.utils;

import java.io.File;
import java.net.URI;

import org.eclipse.core.resources.IFile;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;

/**
 * 打开系统编辑器的工具类
 * @author weachy
 * @version
 * @since JDK1.5
 */
public class OpenEditorUtil {

	/**
	 * 使用系统默认编辑器打开文件
	 * @param filePath
	 *            文件路径;
	 */
	public static void OpenFileWithSystemEditor(String filePath) {
		OpenFileWithSystemEditor(new File(filePath).toURI());
	}

	/**
	 * 使用系统默认编辑器打开文件
	 * @param page
	 *            IWorkbenchPage 对象
	 * @param filePath
	 *            文件路径;
	 */
	public static void OpenFileWithSystemEditor(IWorkbenchPage page, String filePath) {
		OpenFileWithSystemEditor(page, new File(filePath).toURI());
	}

	/**
	 * 使用系统默认编辑器打开文件
	 * @param uri
	 *            ;
	 */
	public static void OpenFileWithSystemEditor(URI uri) {
		OpenFileWithSystemEditor(getCurrentPage(), uri);
	}

	/**
	 * 使用系统默认编辑器打开文件
	 * @param page
	 *            IWorkbenchPage 对象
	 * @param uri
	 *            ;
	 */
	public static void OpenFileWithSystemEditor(IWorkbenchPage page, URI uri) {
		try {
			IDE.openEditor(page, uri, IEditorRegistry.SYSTEM_EXTERNAL_EDITOR_ID, true);
		} catch (PartInitException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 使用系统默认编辑器打开文件
	 * @param file
	 *            IFile 对象（工作空间内的文件）;
	 */
	public static void OpenFileWithSystemEditor(IFile file) {
		try {
			IDE.openEditor(getCurrentPage(), file, IEditorRegistry.SYSTEM_EXTERNAL_EDITOR_ID);
		} catch (PartInitException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 得到当前的 IWorkbenchPage 对象
	 * @return ;
	 */
	private static IWorkbenchPage getCurrentPage() {
		return PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
	}
}

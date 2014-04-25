package net.heartsome.cat.common.resources;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import net.heartsome.cat.common.util.CommonFunction;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResourceUtils {
	private static final int DEFAULT_BUFFER_SIZE = 16 * 1024;
	private static IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();

	private static Logger LOGGER = LoggerFactory.getLogger(ResourceUtils.class);

	/**
	 * 得到某容器下（IContainer 对象）的指定后缀名的所有文件（包括子文件夹中的文件）；
	 * <p>
	 * example：
	 * <li>getFiles(folder, files, "xlf", ".sdlxliff"); // 得到指定文件夹下的所有 XLIFF 文件</li>
	 * <li>getFiles(folder, files, "xlf", "tmx", "tbx"); // 得到指定文件夹下的所有 XLIFF、TMX、TBX 文件</li>
	 * </p>
	 * @param container
	 *            容器（IContainer 对象）
	 * @param files
	 *            files 文件集合
	 * @param fileExtensions
	 *            文件后缀名
	 * @throws CoreException
	 *             ;
	 */
	public static void getFiles(IContainer container, ArrayList<IFile> files, String... fileExtensions)
			throws CoreException {
		if (files == null) {
			files = new ArrayList<IFile>();
		}
		IResource[] members = container.members();
		for (IResource r : members) {
			if (r instanceof IFile) {
				if (fileExtensions == null || fileExtensions.length == 0
						|| CommonFunction.containsIgnoreCase(fileExtensions, r.getFileExtension())) {
					files.add((IFile) r);
				}
			} else if (r instanceof IFolder) {
				getFiles((IFolder) r, files, fileExtensions);
			}
		}
	}

	/**
	 * 获取当前文件夹或项目下的所有xliff文件
	 * @param container
	 * @param files
	 * @throws CoreException
	 */
	public static void getXliffs(IContainer container, ArrayList<IFile> files) throws CoreException {
		getFiles(container, files, CommonFunction.xlfExtesionArray);
	}

	/**
	 * 将指定的IFile转换成系统的完整路径
	 * @param iFile
	 * @return ;
	 */
	public static String iFileToOSPath(IFile iFile) {
		return iFile.getLocation().toOSString();
	}

	/**
	 * 将一组IFile对象转换为系统的完整路径
	 * @param iFiles
	 * @return ;
	 */
	public static List<String> IFilesToOsPath(List<IFile> iFiles) {
		List<String> list = new ArrayList<String>();
		for (int i = 0; i < iFiles.size(); i++) {
			list.add(iFileToOSPath(iFiles.get(i)));
		}
		return list;
	}

	/**
	 * 将 {@link org.eclipse.core.resources.IFile} 对象转换为 {@link java.io.File} 对象
	 * @param iFile
	 *            {@link org.eclipse.core.resources.IFile} 对象
	 * @return {@link java.io.File} 对象;
	 */
	public static File iFileToFile(IFile iFile) {
		return iFile.getLocation().toFile();
	}

	/**
	 * 将一组 {@link org.eclipse.core.resources.IFile} 对象转换为 {@link java.io.File} 对象
	 * @param iFiles
	 *            {@link org.eclipse.core.resources.IFile} 对象集合
	 * @return {@link java.io.File} 对象集合;
	 */
	public static List<File> iFilesToFiles(List<IFile> iFiles) {
		ArrayList<File> list = new ArrayList<File>();
		for (int i = 0; i < iFiles.size(); i++) {
			list.add(iFileToFile(iFiles.get(i)));
		}
		return list;
	}

	/**
	 * 将文件从一个地方复制到另一个地方 peason 2012-02-17
	 * @param srcPath
	 * @param dstPath
	 * @throws IOException
	 *             ;
	 */
	public static void copyDirectory(File srcPath, File dstPath) throws IOException {
		if (srcPath.isDirectory()) {
			if (!dstPath.exists()) {
				dstPath.mkdirs();
			}
			String files[] = srcPath.list();
			for (int i = 0; i < files.length; i++) {
				copyDirectory(new File(srcPath, files[i]), new File(dstPath, files[i]));
			}
		} else {
			if (srcPath.exists()) {
				InputStream in = new FileInputStream(srcPath);
				OutputStream out = new FileOutputStream(dstPath);
				byte[] buf = new byte[1024];
				int len;
				while ((len = in.read(buf)) > 0) {
					out.write(buf, 0, len);
				}
				in.close();
				out.close();
			}
		}
	}

	/**
	 * 复制一个文件到另一个文件 --Robert 2013-04-10
	 * @param in
	 *            要复制的源文件
	 * @param out
	 *            要复制的目标文件
	 * @throws IOException
	 */
	public static void copyFile(File in, File out) throws IOException {
		BufferedInputStream bis = null;
		BufferedOutputStream bos = null;
		try {
			bis = new BufferedInputStream(new FileInputStream(in));
			bos = new BufferedOutputStream(new FileOutputStream(out));
			int available = bis.available();
			available = available <= 0 ? DEFAULT_BUFFER_SIZE : available;
			int chunkSize = Math.min(DEFAULT_BUFFER_SIZE, available);
			byte[] buffer = new byte[chunkSize];
			int byteread = 0;
			while ((byteread = bis.read(buffer)) > 0) {
				bos.write(buffer, 0, byteread);
			}
		} finally {
			if (bis != null) {
				bis.close();
			}
			if (bos != null) {
				bos.close();
			}
		}
	}

	/**
	 * 得到工作空间中的路径。
	 * @param conversionItem
	 * @return ;
	 */
	public static String toWorkspacePath(String localPath) {
		IFile file = root.getFileForLocation(new Path(localPath));
		return file != null ? file.getFullPath().toOSString() : localPath;
	}

	/**
	 * 得到工作空间中的路径。
	 * @param conversionItem
	 * @return ;
	 */
	public static String toWorkspacePath(IPath localPath) {
		IFile file = root.getFileForLocation(localPath);
		return file != null ? file.getFullPath().toOSString() : localPath.toOSString();
	}

	public static IFile fileToIFile(String filePath) {
		IPath path = Path.fromOSString(filePath);
		IFile iFile = root.getFileForLocation(path);
		return iFile;
	}

	/**
	 * 将一个file集合转换成IFile集合 robert 2012-05-06
	 * @param filePath
	 * @return
	 */
	public static List<IFile> filesToIFiles(List<File> fileList) {
		List<IFile> iFileList = new ArrayList<IFile>();
		for (File file : fileList) {
			IPath path = Path.fromOSString(file.getAbsolutePath());
			iFileList.add(root.getFileForLocation(path));
		}
		return iFileList;
	}

	/**
	 * 将　相对于工作空间的路径转换成　绝对路径 robert 2013-08-01
	 * @param fullpath
	 * @return
	 */
	public static String fullPathToLoction(String fullpath) {
		return root.getLocation().append(fullpath).toOSString();
	}

	/**
	 * 刷新当前选择项目 ;
	 */
	public static void refreshCurentSelectProject() {
		Display.getDefault().syncExec(new Runnable() {

			
			@SuppressWarnings("unchecked")
			public void run() {
				IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
				if (null == page) {
					return;
				}
				IViewPart viewPart = page.findView("net.heartsome.cat.common.ui.navigator.view");
				List<IProject> tempProjects = new ArrayList<IProject>();
				if (null != viewPart) {
					StructuredSelection selection = (StructuredSelection) viewPart.getSite().getSelectionProvider()
					.getSelection();
					if (null != selection && !selection.isEmpty()) {
						List<Object> selects = selection.toList();
						for (Object obj : selects) {
							if (obj instanceof IResource) {
								IResource resource = (IResource) obj;
								IProject project = resource.getProject();
								if (tempProjects.contains(project)) {
									continue;
								}
								tempProjects.add(project);
							}
						}
					}
				}
				IEditorPart activeEditor = page.getActiveEditor();
				if (null != activeEditor) {
					IEditorInput editorInput = activeEditor.getEditorInput();
					if (null != editorInput && editorInput instanceof FileEditorInput) {
						FileEditorInput fileEditorInput = (FileEditorInput) editorInput;
						IFile file = fileEditorInput.getFile();
						IProject project = file.getProject();
						if (!tempProjects.contains(project)) {
							tempProjects.add(project);
						}
					}
				}
				try {
					for (IProject project : tempProjects) {
						project.refreshLocal(IResource.DEPTH_INFINITE, null);
					}
				} catch (CoreException e) {
					LOGGER.error("", e);
				}
			}
		});

	}

	/**
	 * 刷新工作空间 ;
	 */
	public static void refreshWorkSpace() {
		try {
			root.refreshLocal(IResource.DEPTH_INFINITE, null);
		} catch (CoreException e) {
			LOGGER.error("", e);
		}
	}

}

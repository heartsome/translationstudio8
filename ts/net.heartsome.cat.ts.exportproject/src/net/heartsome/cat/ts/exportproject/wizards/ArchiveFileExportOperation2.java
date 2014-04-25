package net.heartsome.cat.ts.exportproject.wizards;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.heartsome.cat.common.bean.DatabaseModelBean;
import net.heartsome.cat.common.bean.ProjectInfoBean;
import net.heartsome.cat.ts.core.file.ProjectConfiger;
import net.heartsome.cat.ts.core.file.ProjectConfigerFactory;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.operation.ModalContext;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.internal.wizards.datatransfer.DataTransferMessages;
import org.eclipse.ui.internal.wizards.datatransfer.IFileExporter;
import org.eclipse.ui.internal.wizards.datatransfer.TarFileExporter;

/**
 * 此类与 org.eclipse.ui.internal.wizards.datatransfer 大部分代码一样，区别为调用的生成 ZIP 压缩包的类为
 * net.heartsome.cat.ts.ui.wizards.ZipFileExporter2
 * @author peason
 * @version
 * @since JDK1.6
 */
@SuppressWarnings("restriction")
public class ArchiveFileExportOperation2 implements IRunnableWithProgress {

	private IFileExporter exporter;

	private String destinationFilename;

	private IProgressMonitor monitor;

	private List resourcesToExport;

	private IResource resource;

	private List errorTable = new ArrayList(1); // IStatus

	private boolean useCompression = true;

	private boolean useTarFormat = false;

	private boolean createLeadupStructure = true;

	private boolean isExportTm = false;;

	private boolean isExportTb = false;

	/** @return the isExportTm */
	public boolean isExportTm() {
		return isExportTm;
	}

	/**
	 * @param isExportTm
	 *            the isExportTm to set
	 */
	public void setExportTm(boolean isExportTm) {
		this.isExportTm = isExportTm;
	}

	/** @return the isExportTb */
	public boolean isExportTb() {
		return isExportTb;
	}

	/**
	 * @param isExportTb
	 *            the isExportTb to set
	 */
	public void setExportTb(boolean isExportTb) {
		this.isExportTb = isExportTb;
	}

	/**
	 * Create an instance of this class. Use this constructor if you wish to export specific resources without a common
	 * parent resource
	 * @param resources
	 *            java.util.Vector
	 * @param filename
	 *            java.lang.String
	 */
	public ArchiveFileExportOperation2(List resources, String filename) {
		super();

		// Eliminate redundancies in list of resources being exported
		Iterator elementsEnum = resources.iterator();
		while (elementsEnum.hasNext()) {
			IResource currentResource = (IResource) elementsEnum.next();
			if (isDescendent(resources, currentResource)) {
				elementsEnum.remove(); // Removes currentResource;
			}
		}

		resourcesToExport = resources;
		destinationFilename = filename;
	}

	/**
	 * Create an instance of this class. Use this constructor if you wish to recursively export a single resource.
	 * @param res
	 *            org.eclipse.core.resources.IResource;
	 * @param filename
	 *            java.lang.String
	 */
	public ArchiveFileExportOperation2(IResource res, String filename) {
		super();
		resource = res;
		destinationFilename = filename;
	}

	/**
	 * Create an instance of this class. Use this constructor if you wish to export specific resources with a common
	 * parent resource (affects container directory creation)
	 * @param res
	 *            org.eclipse.core.resources.IResource
	 * @param resources
	 *            java.util.Vector
	 * @param filename
	 *            java.lang.String
	 */
	public ArchiveFileExportOperation2(IResource res, List resources, String filename) {
		this(res, filename);
		resourcesToExport = resources;
	}

	/**
	 * Add a new entry to the error table with the passed information
	 */
	protected void addError(String message, Throwable e) {
		errorTable.add(new Status(IStatus.ERROR, IDEWorkbenchPlugin.IDE_WORKBENCH, 0, message, e));
	}

	/**
	 * Answer the total number of file resources that exist at or below self in the resources hierarchy.
	 * @return int
	 * @param checkResource
	 *            org.eclipse.core.resources.IResource
	 */
	protected int countChildrenOf(IResource checkResource) throws CoreException {
		if (checkResource.getType() == IResource.FILE) {
			return 1;
		}

		int count = 0;
		if (checkResource.isAccessible()) {
			IResource[] children = ((IContainer) checkResource).members();
			for (int i = 0; i < children.length; i++) {
				count += countChildrenOf(children[i]);
			}
		}

		return count;
	}

	/**
	 * Answer a boolean indicating the number of file resources that were specified for export
	 * @return int
	 */
	protected int countSelectedResources() throws CoreException {
		int result = 0;
		Iterator resources = resourcesToExport.iterator();
		while (resources.hasNext()) {
			result += countChildrenOf((IResource) resources.next());
		}

		return result;
	}

	/**
	 * Export the passed resource to the destination .zip. Export with no path leadup
	 * @param exportResource
	 *            org.eclipse.core.resources.IResource
	 */
	protected void exportResource(IResource exportResource) throws InterruptedException {
		exportResource(exportResource, 1);
	}

	/**
	 * Creates and returns the string that should be used as the name of the entry in the archive.
	 * @param exportResource
	 *            the resource to export
	 * @param leadupDepth
	 *            the number of resource levels to be included in the path including the resourse itself.
	 */
	private String createDestinationName(int leadupDepth, IResource exportResource) {
		IPath fullPath = exportResource.getFullPath();
		if (createLeadupStructure) {
			return fullPath.makeRelative().toString();
		}
		return fullPath.removeFirstSegments(fullPath.segmentCount() - leadupDepth).toString();
	}

	/**
	 * Export the passed resource to the destination .zip
	 * @param exportResource
	 *            org.eclipse.core.resources.IResourcep
	 * @param leadupDepth
	 *            the number of resource levels to be included in the path including the resourse itself.
	 */
	protected void exportResource(IResource exportResource, int leadupDepth) throws InterruptedException {
		if (!exportResource.isAccessible()) {
			return;
		}
			exportProjectSQLiteFiles(exportResource.getProject());	// 导出文件数据库文件	
		
		if (exportResource.getType() == IResource.FILE) {
			String destinationName = createDestinationName(leadupDepth, exportResource);
			monitor.subTask(destinationName);

			try {
				exporter.write((IFile) exportResource, destinationName);
			} catch (IOException e) {
				addError(NLS.bind(DataTransferMessages.DataTransfer_errorExporting, exportResource.getFullPath()
						.makeRelative(), e.getMessage()), e);
			} catch (CoreException e) {
				addError(NLS.bind(DataTransferMessages.DataTransfer_errorExporting, exportResource.getFullPath()
						.makeRelative(), e.getMessage()), e);
			}

			monitor.worked(1);
			ModalContext.checkCanceled(monitor);
		} else {
			
			IResource[] children = null;

			try {
				children = ((IContainer) exportResource).members();
			} catch (CoreException e) {
				// this should never happen because an #isAccessible check is done before #members is invoked
				addError(NLS.bind(DataTransferMessages.DataTransfer_errorExporting, exportResource.getFullPath()), e);
			}

			if (children.length == 0) { // create an entry for empty containers, see bug 278402
				String destinationName = createDestinationName(leadupDepth, exportResource);
				try {
					exporter.write((IContainer) exportResource, destinationName + "/");
				} catch (IOException e) {
					addError(NLS.bind(DataTransferMessages.DataTransfer_errorExporting, exportResource.getFullPath()
							.makeRelative(), e.getMessage()), e);
				}
			}

			for (int i = 0; i < children.length; i++) {
				exportResource(children[i], leadupDepth + 1);
			}
		

		}
	}

	/**
	 * Export the resources contained in the previously-defined resourcesToExport collection
	 */
	protected void exportSpecifiedResources() throws InterruptedException {
		Iterator resources = resourcesToExport.iterator();

		while (resources.hasNext()) {
			IResource currentResource = (IResource) resources.next();
			exportResource(currentResource);
		}
	}

	/**
	 * Returns the status of the operation. If there were any errors, the result is a status object containing
	 * individual status objects for each error. If there were no errors, the result is a status object with error code
	 * <code>OK</code>.
	 * @return the status
	 */
	public IStatus getStatus() {
		IStatus[] errors = new IStatus[errorTable.size()];
		errorTable.toArray(errors);
		return new MultiStatus(IDEWorkbenchPlugin.IDE_WORKBENCH, IStatus.OK, errors,
				DataTransferMessages.FileSystemExportOperation_problemsExporting, null);
	}

	/**
	 * Initialize this operation
	 * @exception java.io.IOException
	 */
	protected void initialize() throws IOException {
		if (useTarFormat) {
			exporter = new TarFileExporter(destinationFilename, useCompression);
		} else {
			exporter = new ZipFileExporter2(destinationFilename, useCompression);
		}
	}

	/**
	 * Answer a boolean indicating whether the passed child is a descendent of one or more members of the passed
	 * resources collection
	 * @return boolean
	 * @param resources
	 *            java.util.Vector
	 * @param child
	 *            org.eclipse.core.resources.IResource
	 */
	protected boolean isDescendent(List resources, IResource child) {
		if (child.getType() == IResource.PROJECT) {
			return false;
		}

		IResource parent = child.getParent();
		if (resources.contains(parent)) {
			return true;
		}

		return isDescendent(resources, parent);
	}

	/**
	 * Export the resources that were previously specified for export (or if a single resource was specified then export
	 * it recursively)
	 */
	public void run(IProgressMonitor progressMonitor) throws InvocationTargetException, InterruptedException {
		this.monitor = progressMonitor;

		try {
			initialize();
		} catch (IOException e) {
			throw new InvocationTargetException(e, NLS.bind(DataTransferMessages.ZipExport_cannotOpen, e.getMessage()));
		}

		try {
			// ie.- a single resource for recursive export was specified
			int totalWork = IProgressMonitor.UNKNOWN;
			try {
				if (resourcesToExport == null) {
					totalWork = countChildrenOf(resource);
				} else {
					totalWork = countSelectedResources();
				}
			} catch (CoreException e) {
				// Should not happen
			}
			monitor.beginTask(DataTransferMessages.DataTransfer_exportingTitle, totalWork);
			if (resourcesToExport == null) {
				exportResource(resource);
			} else {
				// ie.- a list of specific resources to export was specified
				exportSpecifiedResources();
			}

			try {
				exporter.finished();
			} catch (IOException e) {
				throw new InvocationTargetException(e, NLS.bind(DataTransferMessages.ZipExport_cannotClose,
						e.getMessage()));
			}
		} finally {
			monitor.done();
		}
	}

	/**
	 * Set this boolean indicating whether each exported resource's path should include containment hierarchies as
	 * dictated by its parents
	 * @param value
	 *            boolean
	 */
	public void setCreateLeadupStructure(boolean value) {
		createLeadupStructure = value;
	}

	/**
	 * Set this boolean indicating whether exported resources should be compressed (as opposed to simply being stored)
	 * @param value
	 *            boolean
	 */
	public void setUseCompression(boolean value) {
		useCompression = value;
	}

	/**
	 * Set this boolean indicating whether the file should be output in tar.gz format rather than .zip format.
	 * @param value
	 *            boolean
	 */
	public void setUseTarFormat(boolean value) {
		useTarFormat = value;
	}

	private final int TM = 0;

	private final int TB = 1;

	/**
	 * @param project
	 * @param type
	 * @return ;
	 */
	private List<File> getProjectSQLiteFiles(IProject project, int type) {
		ProjectConfiger projectConfiger = ProjectConfigerFactory.getProjectConfiger(project);
		ProjectInfoBean currentProjectConfig = projectConfiger.getCurrentProjectConfig();
		List<DatabaseModelBean> sqlDbs = null;
		if (TM == type) {
			sqlDbs = currentProjectConfig.getTmDb();
		} else if (TB == type) {
			sqlDbs = currentProjectConfig.getTbDb();
		}
		List<File> files = new ArrayList<File>();
		for (DatabaseModelBean bean : sqlDbs) {
			if ("SQLite".equals(bean.getDbType())) {
				File file = new File(getSqliteFilePath(bean));
				if (file.exists() && file.isFile()) {
					files.add(file);
				}
			}
		}

		return files;
	}

	private String getSqliteFilePath(DatabaseModelBean bean) {
		return bean.getItlDBLocation() + File.separator + bean.getDbName();
	}

	private List<IProject> exprotedProject = new ArrayList<IProject>();

	private void exportProjectSQLiteFiles(IProject project) {
		if (exprotedProject.contains(project)) {
			return;
		} else {
			exprotedProject.add(project);
		}
		List<File> projectSQLiteTmFiles = getProjectSQLiteFiles(project, TM);
		List<File> projectSQLiteTbFiles = getProjectSQLiteFiles(project, TB);
		String fileSeparator = "/";// just support char '/' , use System.getProperty("file.separator") maybe has a bug;
		if (exporter instanceof ZipFileExporter2) {
			ZipFileExporter2 zipFileExporter2 = (ZipFileExporter2) exporter;
			String tmParent = project.getName() + fileSeparator + "TM" + fileSeparator;
			String tbParent = project.getName() + fileSeparator + "TB" + fileSeparator;
			try {
				// 导出SQLITE TM File
				if (isExportTm() && null != projectSQLiteTmFiles && !projectSQLiteTmFiles.isEmpty()) {
					zipFileExporter2.addDbFolder(tmParent);
					int index = 0;
					for (File file : projectSQLiteTmFiles) {
						String exportDBName = getExportDBName(project, file, TM, index);
						if (null != exportDBName) {
							zipFileExporter2.addDbFile(file, tmParent + exportDBName);
							index++;
						}
					}
				}
				// 导出SQLITE TB File
				int index = 0;
				if (isExportTb() && null != projectSQLiteTbFiles && !projectSQLiteTbFiles.isEmpty()) {
					zipFileExporter2.addDbFolder(tbParent);
					for (File file : projectSQLiteTbFiles) {
						String exportDBName = getExportDBName(project, file, TB, index);
						if (null != exportDBName) {
							zipFileExporter2.addDbFile(file, tbParent + exportDBName);
							index++;
						}
					}
				}

			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private String getExportDBName(IProject project, File dbfile, int type, int index) {
		String fileName = dbfile.getName();
		String exportName = fileName;
		String filePath = dbfile.getAbsolutePath();
		if (TM == type) {
			IFolder folder = project.getFolder("TM");
			if (folder.exists()) {
				IFile file = folder.getFile(fileName);
				if (file.exists() && !equalPath(filePath, file.getLocation().toOSString())) {
					exportName = "Exported_" + index + "_" + fileName;
				}

				if (resourcesToExport.contains(project) && equalPath(filePath, file.getLocation().toOSString())) {
					return null;
				}
			}
		} else if (TB == type) {
			IFolder folder = project.getFolder("TB");
			if (folder.exists()) {
				IFile file = folder.getFile(fileName);
				if (file.exists() && !equalPath(filePath, file.getLocation().toOSString())) {
					exportName = "Exported_" + index + "_" + fileName;
				}

				if (resourcesToExport.contains(project) && equalPath(filePath, file.getLocation().toOSString())) {
					return null;
				}
			}
		}
		return exportName;
	}

	private boolean equalPath(String path1, String path2) {
		String pathOne = path1.replaceAll("\\\\", "/");
		String pathTwo = path2.replaceAll("\\\\", "/");
		return pathOne.equals(pathTwo);
	}

}

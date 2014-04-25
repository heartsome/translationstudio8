package net.heartsome.cat.ts.ui.util;

import java.io.File;
import java.io.FileOutputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import net.heartsome.cat.common.resources.ResourceUtils;
import net.heartsome.cat.common.util.TextUtil;
import net.heartsome.cat.ts.ui.editors.IXliffEditor;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 合并打开XLIFF文件的处理
 * @author  robert	2012-04-23
 * @version 
 * @since   JDK1.6
 */
public class MultiFilesOper {
	private static final String _TEMPFOLDER = ".TEMP";
	private static final String _XLP = ".xlp";
	private static final String XLIFF_EDITOR_ID = "net.heartsome.cat.ts.ui.xliffeditor.nattable.editor";
	/** 当前合并打开的所有文件所在的项目 */
	private IProject selectedProject;
	/** 当前合并打开的所有文件 */
	private ArrayList<IFile> selectIFiles;
	/** 当前合并打开的临时文件 */
	private IFile curMultiTempFile;
	
	private static final Logger LOGGER = LoggerFactory.getLogger(MultiFilesOper.class.getName()); 
	
	public MultiFilesOper(IProject selectedProject, ArrayList<IFile> selectIFiles){
		this.selectedProject = selectedProject;
		this.selectIFiles = selectIFiles;
	}
	
	public MultiFilesOper(IProject selectedProject, ArrayList<IFile> selectIFiles, IFile curMultiTempFile){
		this.selectedProject = selectedProject;
		this.selectIFiles = selectIFiles;
		this.curMultiTempFile = curMultiTempFile;
	}
	
	/**
	 * 创建合并打开XLIFF文件的临时文件
	 */
	public IFile createMultiTempFile(){
		if (!selectedProject.getFolder(_TEMPFOLDER).exists()) {
			File tempFold = selectedProject.getLocation().append(_TEMPFOLDER).toFile();
			tempFold.mkdirs();
		}
		
		File tempFile = null;
		try {
			tempFile = createTempFile();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		
		if (tempFile == null) {
			return null;
		}
		
		IFile multiFile = selectedProject.getFolder(_TEMPFOLDER).getFile(tempFile.getName());
		try {
			selectedProject.refreshLocal(2, null);
		} catch (CoreException e1) {
			e1.printStackTrace();
		}
		return multiFile;
	}
	
	/**
	 * 验证该合并打开的文件是否已经重复被打开，如果被合并打开，则返回true,未被打开，返回false.
	 * @param selectIFiles
	 * @return ;
	 */
	public boolean validExist(){
		IEditorReference[] editorRes = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getEditorReferences();
		for (int i = 0; i < editorRes.length; i++) {
			if (editorRes[i].getId().equals(XLIFF_EDITOR_ID)) {
				
				try {
					IFile iFile = ((FileEditorInput)editorRes[i].getEditorInput()).getFile();
					if (!"xlp".equals(iFile.getFileExtension())) {
						continue;
					}
				} catch (Exception e) {
					LOGGER.error("", e);
				}
				boolean isRepeat = true;
				
				IXliffEditor xlfEditor =  (IXliffEditor) editorRes[i].getEditor(true);
				List<IFile> mergerIFileList = ResourceUtils.filesToIFiles(xlfEditor.getMultiFileList());
				if (mergerIFileList.size() == selectIFiles.size()) {
					for (IFile curIFile : selectIFiles) {
						if (mergerIFileList.indexOf(curIFile) < 0) {
							isRepeat = false;
							break;
						}
					}
				}else {
					isRepeat = false;
				}
				if (isRepeat) {
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * 获取当前所打开的所有文件
	 * @return
	 */
	public List<IFile> getAllOpenedIFiles(){
		IEditorReference[] editorRes = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getEditorReferences();
		List<IFile> isOpenedXlfList = new ArrayList<IFile>();
		try {
			IXliffEditor xlfEditor;
			for (int i = 0; i < editorRes.length; i++) {
				if (editorRes[i].getId().equals(XLIFF_EDITOR_ID)) {
					IFile iFile = ((FileEditorInput)editorRes[i].getEditorInput()).getFile();
					//合并打开的情况
					if ("xlp".equals(iFile.getFileExtension())) {
						xlfEditor = (IXliffEditor) editorRes[i].getEditor(true);
						isOpenedXlfList.addAll(ResourceUtils.filesToIFiles(xlfEditor.getMultiFileList()));
					}else {
						try {
							isOpenedXlfList.add(((FileEditorInput)editorRes[i].getEditorInput()).getFile());
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			}
		} catch (Exception e) {
			LOGGER.error("", e);
		}
		return isOpenedXlfList;
	}
	
	/**
	 * 验证当前要合并打开的文件是否有文件存在已经打开的情况，并从当前要合并打开的文件中删除已经打开的文件
	 * <div style='color:red'>这个方法一是验证 所选要打开的文件 中是否已经有被打开了的，第二是，会删除已经打开的文件，因此慎用。<br>
	 *  getOpenedIfile 方法也有验证所选择文件是否有打开的情况</div>
	 * @return
	 */
	public boolean hasOpenedIFile(){
		IEditorReference[] editorRes = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getEditorReferences();
		List<IFile> isOpenedXlfList = new ArrayList<IFile>();
		IXliffEditor xlfEditor;
		boolean hasOpenedIFile = false;
		
		try {
			for (int i = 0; i < editorRes.length; i++) {
				if (editorRes[i].getId().equals(XLIFF_EDITOR_ID)) {
					IFile iFile = ((FileEditorInput)editorRes[i].getEditorInput()).getFile();
					//合并打开的情况
					if ("xlp".equals(iFile.getFileExtension())) {
						System.out.println("---------------");
						xlfEditor = (IXliffEditor) editorRes[i].getEditor(true);
						isOpenedXlfList.addAll(ResourceUtils.filesToIFiles(xlfEditor.getMultiFileList()));
					}else {
						try {
							isOpenedXlfList.add(((FileEditorInput)editorRes[i].getEditorInput()).getFile());
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			}
			
			if (isOpenedXlfList.size() > 0) {
				for(IFile iFile : isOpenedXlfList){
					if (selectIFiles.indexOf(iFile) >= 0) {
						hasOpenedIFile = true;
						selectIFiles.remove(iFile);
					}
				}
			}
		} catch (Exception e) {
			LOGGER.error("", e);
		}
		
		return hasOpenedIFile;
	}
	
	/**
	 * 获取所选择的要合并打开的文件中已经被打开的文件
	 * @return
	 */
	public List<IFile> getOpenedIfile(){
		IEditorReference[] editorRes = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getEditorReferences();
		List<IFile> isOpenedXlfList = new ArrayList<IFile>();
		IXliffEditor xlfEditor;
		
		try {
			for (int i = 0; i < editorRes.length; i++) {
				if (editorRes[i].getId().equals(XLIFF_EDITOR_ID)) {
					IFile iFile = ((FileEditorInput)editorRes[i].getEditorInput()).getFile();
					//合并打开的情况
					if ("xlp".equals(iFile.getFileExtension())) {
						xlfEditor = (IXliffEditor) editorRes[i].getEditor(true);
						isOpenedXlfList.addAll(ResourceUtils.filesToIFiles(xlfEditor.getMultiFileList()));
					}else {
						try {
							isOpenedXlfList.add(((FileEditorInput)editorRes[i].getEditorInput()).getFile());
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			}
		} catch (Exception e) {
			LOGGER.error("", e);
		}
		
		return isOpenedXlfList;
	}
	
	/**
	 * 根据指定要合并打开的文件，获取其配置文件
	 * @param selectIFiles
	 * @param isActive	如果找到了符合的合并打开临时文件，是否激活当前nattable编辑器
	 * @return ;
	 */
	public IFile getMultiFilesTempIFile(boolean isActive){
		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		IEditorReference[] editorRes = page.getEditorReferences();
		
		for (int i = 0; i < editorRes.length; i++) {
			if (editorRes[i].getId().equals(XLIFF_EDITOR_ID)) {
				try {
					IXliffEditor xlfEditor = (IXliffEditor) editorRes[i].getEditor(true);
					IFile multiTempFile = ((FileEditorInput) editorRes[i].getEditorInput()).getFile();
					List<File> openedFileList = xlfEditor.getMultiFileList();
					
					boolean isExist = false;
					if (selectIFiles.size() == openedFileList.size()) {
						isExist = true;
						for (IFile iFile : selectIFiles) {
							if (openedFileList.indexOf(iFile.getFullPath().toFile()) == -1) {
								continue;
							}
						}
					}
					
//					Map<String, Object> resultMap = handler.openFile(multiTempFile.getLocation().toOSString());
//					if (resultMap == null
//							|| Constant.RETURNVALUE_RESULT_SUCCESSFUL != (Integer) resultMap.get(Constant.RETURNVALUE_RESULT)) {
//						continue;
//					}
//					List<String> mergerFileList = handler.getMultiFiles(multiTempFile);
//					if (mergerFileList.size() == selectIFiles.size()) {
//						for (IFile iFile : selectIFiles) {
//							if (mergerFileList.indexOf(iFile.getLocation().toOSString()) < 0) {
//								continue;
//							}
//						}
//					}
					if (isActive) {
						page.activate(editorRes[i].getEditor(true));
					}
					if (isExist) {
						return multiTempFile;
					}
				} catch (PartInitException e) {
					e.printStackTrace();
				}
			}
		}
		return null;
	}
	
	
	
	/**
	 * 创建临时文件，里面保存合并打开的文件路径
	 * @param selectIFiles
	 * @throws Exception
	 */
	private File createTempFile() throws Exception {
		String tempFile = selectedProject.getLocation().append(_TEMPFOLDER).append(System.currentTimeMillis() + _XLP).toOSString();
		FileOutputStream output = new FileOutputStream(tempFile);
		StringBuffer dataSB = new StringBuffer();
		dataSB.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		dataSB.append("<mergerFiles>\n");
		for (IFile iFile : selectIFiles) {
			dataSB.append(MessageFormat.format("\t<mergerFile filePath=\"{0}\"/>\n", TextUtil.cleanSpecialString(iFile.getLocation().toOSString())));
		}
		dataSB.append("</mergerFiles>\n");
		output.write(dataSB.toString().getBytes("UTF-8"));
		output.close();
		File file = new File(tempFile);
		return file;
	}

	public IProject getSelectedProject() {
		return selectedProject;
	}

	public void setSelectedProject(IProject selectedProject) {
		this.selectedProject = selectedProject;
	}

	public ArrayList<IFile> getSelectIFiles() {
		return selectIFiles;
	}

	public void setSelectIFiles(ArrayList<IFile> selectIFiles) {
		this.selectIFiles = selectIFiles;
	}

	public IFile getCurMultiTempFile() {
		return curMultiTempFile;
	}

	public void setCurMultiTempFile(IFile curMultiTempFile) {
		this.curMultiTempFile = curMultiTempFile;
	}
}
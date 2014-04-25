package net.heartsome.cat.ts.core.qa;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * 文件分析类，是字数分析，翻译进度分析，编辑进度分析的父类
 * 它的具体实现在net.heartsome.cat.ts.ui.qa插件里面
 * @author robert	2011-12-07
 */
public class FileAnalysis {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(FileAnalysis.class);
	
	private FAModel model;
	
	/**
	 * 开始分析
	 * return int
	 * -1:	文件分析失败
	 * 0 :	退出操作
	 * 1 :操作成功
	 */
	public int beginAnalysis(FAModel model, IProgressMonitor monitor, QAXmlHandler handler){
		this.model = model;
		return QAConstant.QA_ZERO;
	};
	
	public int beginAnalysis_1(FAModel model, IProgressMonitor monitor, QAXmlHandler handler){
		this.model = model;
		return QAConstant.QA_ZERO;
	}
	
	/**
	 * 获取所有包括分析文件的文件夹(直接包括或间接包括都可)
	 * @param rootFolder	起始文件夹
	 * @param allFolderList	承装所有文件夹的集合
	 */
	public void getAllFolder(IContainer rootFolder, List<IContainer> allFolderList){
		
		if (allFolderList == null) {
			allFolderList = new LinkedList<IContainer>();
		}
		IResource[] members;
		try {
			members = rootFolder.members();
			for (IResource resource : members) {
				if (resource instanceof IContainer) {
					boolean faIFilesExsit = false;
					
					//循环判断所有的要分析的文件，检查当前容器下是否包括要分析的文件
					for (int fileIndex = 0; fileIndex < model.getAnalysisIFileList().size(); fileIndex++) {
						IFile ifile = model.getAnalysisIFileList().get(fileIndex);
						
						IContainer iFileParent = ifile.getParent();
						while (iFileParent != null ) {
							if (iFileParent.equals((IContainer)resource) ) {
								faIFilesExsit = true;
								break;
							}else {
								iFileParent = iFileParent.getParent();
							}
						}
						
						//如果当前容器下存在分析的文件，将该容器加载到缓存中，并停止循环其他分析的文件
						if (faIFilesExsit) {
							allFolderList.add((IContainer)resource);
							break;
						}
					}
					
					getAllFolder((IContainer)resource, allFolderList);
				}
			}
		} catch (CoreException e) {
			LOGGER.error("", e);
			e.printStackTrace();
		}
	}
	
	/**
	 * 验证提供文件夹的直接子文件是否包括本次分析的文件
	 * @param curContainer
	 * @return
	 */
	public boolean hasFAIFiles(IContainer curContainer){
		for (int i = 0; i < model.getAnalysisIFileList().size(); i++) {
			if (model.getAnalysisIFileList().get(i).getParent().equals(curContainer)) {
				return true;
			}
		}
		return false;
	}

	
	public void setModel(FAModel model) {
		this.model = model;
	}
	
}

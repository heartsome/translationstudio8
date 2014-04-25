package net.heartsome.cat.ts.importproject.widgiet;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.ui.internal.wizards.datatransfer.ILeveledImportStructureProvider;

import net.heartsome.cat.common.core.Constant;
import net.heartsome.cat.ts.importproject.wizards.ImportProjectWizardPage2.ProjectRecord;


/**
 * 这是树的内容类
 * @author robert
 */
@SuppressWarnings("restriction")
public class ProjectResource {
	private Object element;
	private ProjectResource parent;
	private ProjectRecord proRecord;
	private boolean isFolder;
	/** 是否是项目 */
	private boolean isProject;
	private List<ProjectResource> childrenList;
	private String projectName;
	/** 项目是否重复 */
	private boolean isProjectRepeat;
	/** 当前节点是否重复 */
	private boolean isElementRepeat;
	private IWorkspaceRoot root;
	private ILeveledImportStructureProvider structureProvider;
	/** 标识当前重复的节点是不是需要覆盖已经存在的文件 */
	private boolean needCover;
	
	/** 设置过滤文件的条件 */
	private Set<String> FILE_FILTER = new HashSet<String>();
	
	
	/**
	 * 该构造函数主要是针对项目用的
	 * @param entry
	 * @param proRecord
	 */
	public ProjectResource(Object entry, ProjectRecord proRecord, ILeveledImportStructureProvider structureProvider){
		this.element = entry;
		this.proRecord = proRecord;
		this.projectName = this.proRecord.getProjectName();
		this.structureProvider = structureProvider;
		// 备注，下面这四个初始化的方法，位置不能更换。
		root = ResourcesPlugin.getWorkspace().getRoot();
		isProject = true;
		initIsProjectRepeat();
		initData();
		initChildren();
	}
	
	/**
	 * 该构造函数主要针对项目下的文件夹或者文件
	 * @param entry
	 * @param parent
	 * @param projectName
	 * @param isProjectRepeat
	 * @param structureProvider
	 */
	public ProjectResource(Object entry, ProjectResource parent, String projectName, boolean isProjectRepeat, ILeveledImportStructureProvider structureProvider){
		this.element = entry;
		this.parent = parent;
		this.projectName = projectName;
		this.isProjectRepeat = isProjectRepeat;
		this.structureProvider = structureProvider;
		// 备注，下面这四个初始化的方法，位置不能更换。
		root = ResourcesPlugin.getWorkspace().getRoot();
		isProject = false;
		initIsElementRepeat();
		initData();
		initChildren();
	}
	
	/**
	 * 初始化相关数据
	 */
	private void initData(){
		isFolder = structureProvider.isFolder(element);
		needCover = !isElementRepeat;
		
		FILE_FILTER.add(".project");
		FILE_FILTER.add(Constant.FILE_CONFIG);
		FILE_FILTER.add(".TEMP");
	}
	
	
	@SuppressWarnings("unchecked")
	private void initChildren(){
		childrenList = new ArrayList<ProjectResource>();
		if (isFolder()) {
			List<Object> childrenObjList = structureProvider.getChildren(element);
			if (childrenObjList == null || childrenObjList.size() <= 0) {
				return;
			}
			for(Object curObj : childrenObjList){
				String fileName = structureProvider.getLabel(curObj);
				if (FILE_FILTER.contains(fileName)) {
					continue;
				}
				childrenList.add(new ProjectResource(curObj, this, projectName, isProjectRepeat, structureProvider));
			}
		}
	}
	
	/**
	 * 初始化项目是否重复 
	 */
	private void initIsProjectRepeat(){
		isElementRepeat = isProjectRepeat = root.getLocation().append(projectName).toFile().exists();
	}
	
	/**
	 * 初始化当前节点是否重复
	 */
	private void initIsElementRepeat(){
		// 如果项目名称不重复，那么里面的内容也不会重复的。
		if (isProjectRepeat) {
			// UNDO 这里要注意下，当项目文件与根目录文件不相符时，这里是否合法？我猜是不能的。
			isElementRepeat = root.getLocation().append(structureProvider.getFullPath(element)).toFile().exists();
		}else {
			isElementRepeat = false;
		}
	}
	
	public String getLabel(){
		if (proRecord != null) {
			return projectName;
		}else {
			return structureProvider.getLabel(element);
		}
	}
	
	/**
	 * 将 needCover 属性恢复默认
	 */
	public void restoreNeedCoverDefault(){
		needCover = !isElementRepeat;
	} 
	
	/**
	 * 获取项目下 .config 文件的输入流
	 * <div style='color:red'>备注: 该方法只针对项目进行调用</div>
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public InputStream getConfigFileContent(){
		if (!isProject) {
			return null;
		}
		
		InputStream stream = null;
		List<Object> childrenObjList = structureProvider.getChildren(element);
		for(Object curObj : childrenObjList){
			String fileName = structureProvider.getLabel(curObj);
			if (Constant.FILE_CONFIG.contains(fileName)) {
				stream = structureProvider.getContents(curObj);
			}
		}
		return stream;
	}
	
	/**
	 * 是否有孩子节点
	 * @return
	 */
	public boolean hasChildren(){
		return structureProvider.isFolder(element);
	}
	
	public boolean isProjectRepeat() {
		return isProjectRepeat;
	}

	public boolean isElementRepeat() {
		return isElementRepeat;
	}

	/**
	 * 是否是文件夹
	 * @return
	 */
	public boolean isFolder(){
		return isFolder;
	}
	
	public List<ProjectResource> getChildren(){
		return childrenList;
	}

	public ProjectRecord getProRecord() {
		return proRecord;
	}
	
	public ProjectResource getParent(){
		return parent;
	}

	public String getProjectName() {
		return projectName;
	}

	public boolean isNeedCover() {
		return needCover;
	}

	public void setNeedCover(boolean needCover) {
		this.needCover = needCover;
	}
	
	
	public InputStream getInputStream(){
		if (isFolder) {
			return null;
		}
		return structureProvider.getContents(element);
	}

	public boolean isProject() {
		return isProject;
	}
	
	
	
}

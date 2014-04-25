package net.heartsome.cat.ts.importproject.widgiet;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;

/**
 * 显示带有项目，项目文件的 树
 * @author robert	2013-03-06
 */
public class ResourceTree extends CheckboxTreeViewer implements ICheckStateListener, IDoubleClickListener{
	private Tree tree;
	/** 当前树的内容提供者 */
	private ITreeContentProvider contentProvider;
	/** 当前树的标签提供者 */
	private ILabelProvider  labelProvider;
	/** 保存半选中状态的节点 */
	private Set<Object> grayStoreSet = new HashSet<Object>();
	private ProjectResource[] root;
	
	
	
	public ResourceTree(Composite parent, ITreeContentProvider contentProvider, ILabelProvider labelProvider) {
		super(parent);
		
		this.contentProvider = contentProvider;
		this.labelProvider = labelProvider;
		
		tree = this.getTree();
		tree.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		this.setLabelProvider(this.labelProvider);
		this.setContentProvider(this.contentProvider);
		this.addCheckStateListener(this);
		this.addDoubleClickListener(this);
		
	}

	public void setRoot(ProjectResource[] root) {
		this.root = root;
	}

	
	/**
	 * 设置选择全部
	 */
	public void setCheckedAll(boolean checkState){
		for(ProjectResource projRoot : root){
			setSubtreeChecked(projRoot, checkState);
		}
		// 让缓存全部删除
		setGrayedElements(new Object[0]);
		grayStoreSet.clear();
	}
	
	
	/**
	 * 获取所有的节点
	 * @return
	 */
	public List<ProjectResource> getAllElement(){
		List<ProjectResource> allElements = new ArrayList<ProjectResource>();
		for(ProjectResource proRoot : root){
			allElements.add(proRoot);
			Object[] objArray = contentProvider.getChildren(proRoot);
			for(Object obj : objArray){
				if (obj instanceof ProjectResource) {
					ProjectResource childProResource = (ProjectResource) obj;
					allElements.add(childProResource);
					
					getAllElementImpl(childProResource, allElements);
				}
			}
		}
		return allElements;
	}
	
	private void getAllElementImpl(ProjectResource parentProjRes, List<ProjectResource> allElements){
		for(Object obj : contentProvider.getChildren(parentProjRes)){
			if (obj instanceof ProjectResource) {
				ProjectResource childProResource = (ProjectResource) obj;
				allElements.add(childProResource);
				getAllElementImpl(childProResource, allElements);
			}
		}
	}
	
	
	/**
	 * 设置所有子节点是否选中
	 * @param element	要选中与否的 节点
	 * @param state		当前节点是否要选中
	 */
	private void setAllChildCheck(Object element, boolean state){
		setSubtreeChecked(element, state);
		setAllChildCheckImp(element, state);
	}
	
	private void setAllChildCheckImp(Object element, boolean state){
		Object[] childList = contentProvider.getChildren(element);
		if (childList == null) {
			return;
		}
		for(Object childElement : childList){
			if (getGrayed(childElement)) {
				grayStoreSet.remove(childElement);
			}
			setAllChildCheckImp(childElement, state);
		}
	}

	/**
	 * 双击事件，若双击，自动展开该文件夹
	 */
	public void doubleClick(final DoubleClickEvent event) {
		BusyIndicator.showWhile(getControl().getDisplay(), new Runnable() {
			public void run() {
				ISelection selection = event.getSelection();
				IStructuredSelection struSelection = (IStructuredSelection) selection;
				Object obj = struSelection.getFirstElement();
				if (getExpandedState(obj)) {
					collapseToLevel(obj, 1);
				}else {
					expandToLevel(obj, 1);
				}
			}
		});
	}

	
	public void checkStateChanged(final CheckStateChangedEvent event) {
		BusyIndicator.showWhile(getControl().getDisplay(), new Runnable() {
			public void run() {
				Object element = event.getElement();
				if (getChecked(element)) {
					setAllChildCheck(element, true);
					// 让父文件夹处于半选中状态
					Object parent = contentProvider.getParent(element);
					while(parent != null){
						if (!getChecked(parent)) {
							grayStoreSet.add(parent);
						}
						parent = contentProvider.getParent(parent);
					}
				}else {
					if (getGrayed(element)) {
						grayStoreSet.remove(element);
					}
					
					setAllChildCheck(element, false);
					Object parent = contentProvider.getParent(element);
					while(parent != null){
						if (hasCheckedChildren(parent)) {
							grayStoreSet.add(parent);
						}else {
							if (getGrayed(parent)) {
								grayStoreSet.remove(parent);
							}
							setChecked(parent, false);
						}
						parent = contentProvider.getParent(parent);
					}
				}
				
				setGrayedElements(new Object[0]);
				setGrayedElements(grayStoreSet.toArray());
				for (Object obj : grayStoreSet) {
					setChecked(obj, true);
				}
			}
		});
	}
	
	
	/**
	 * 判断当前父目录是否有子文件或子文件夹被选中
	 * @param parent
	 * @return
	 */
	private boolean hasCheckedChildren(Object parent){
		Object[] childArray = contentProvider.getChildren(parent);
		if (childArray == null || childArray.length <= 0) {
			return false;
		}
		for(Object child : childArray){
			if (getChecked(child)) {
				return true;
			}
			if (hasCheckedChildren(child)) {
				return true;
			}
		}
		return false;
	}
	
	

}

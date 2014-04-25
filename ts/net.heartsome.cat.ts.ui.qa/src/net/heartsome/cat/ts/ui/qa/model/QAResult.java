package net.heartsome.cat.ts.ui.qa.model;

import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.heartsome.cat.ts.ui.qa.resource.Messages;
import net.heartsome.cat.ts.ui.qa.views.QAResultViewPart;
import net.heartsome.cat.ts.ui.util.MultiFilesOper;

public class QAResult {
	/** 是否被品质检查视图注册 */
	private boolean isRegist;
	private List<QAResultBean> rowIdDataList = new ArrayList<QAResultBean>();
	/** 合并文件的处理类 */
	private MultiFilesOper multiOper;
	/** 是否是自动品质检查 */
	private boolean isAutoQA = false;
	public final static Logger logger = LoggerFactory.getLogger(QAResult.class.getName());
	private QAResult curElement = this;
	private List<String> filePathList = new ArrayList<String>();
	/** 针对自动品质检查。返回当前自动品质检查与品质检查结果所处理的对象是否同一个 */
	private boolean isSameOperObjForAuto;
	private IViewPart qaResultViewer = null;
	/** 缓存数量单位，当缓存达到这个数量时，开始传送至结果视图 */
	private final static int dataUnit = 200;
	
	
	public QAResult(){}
	
	public PropertyChangeSupport listeners = new PropertyChangeSupport(this);
	

	public void firePropertyChange(QAResultBean bean){
		rowIdDataList.add(bean);
	}
	
	
	public void firePropertyChange(boolean isMultiFiles, MultiFilesOper oper){
		listeners.firePropertyChange("isMultiFiles", null, new Object[]{isMultiFiles, oper});
	}

	
	/**
	 * 发送数据至品质检查结果视图
	 * @param rowId	该参数只用于　自动品质检查，用途就是，当一个文本段没有错误时，会将此 rowID 传过来，让　结果视图清除该文本段的结果报告。
	 */
	public void sendDataToViewer(String rowId){
		if (!isRegist) {
			// 先调用方法，查看品质检查结果视图是否处于显示状态，如果没有显示，那么先显示它
			Display.getDefault().syncExec(new Runnable() {
				public void run() {
					IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
					if (window == null) {
						return;
					}
					IWorkbenchPage workbenchPage = window.getActivePage();
					qaResultViewer = workbenchPage.findView(QAResultViewPart.ID);

					if (qaResultViewer == null) {
						try {
							if (isAutoQA) {
								workbenchPage.showView(QAResultViewPart.ID, null, IWorkbenchPage.VIEW_CREATE);
							}else {
								workbenchPage.showView(QAResultViewPart.ID);
							}
							qaResultViewer = workbenchPage.findView(QAResultViewPart.ID);
						} catch (PartInitException e) {
							e.printStackTrace();
							logger.error(Messages.getString("qa.handlers.BatchQAHandler.log2"), e);
						}
					} else {
						if (!isAutoQA) {
							if (!window.getActivePage().isPartVisible(qaResultViewer)) {
								if ((isAutoQA && rowIdDataList.size() > 0) || (!isAutoQA)) {
									window.getActivePage().activate(qaResultViewer);
								}
							}
							
							// 若不是自动品质检查，运行时，将结果视图中列表的数据清除
							((QAResultViewPart) qaResultViewer).clearTableData();
						}
					}
					// 注册
					((QAResultViewPart) qaResultViewer).registLister(curElement);
					isRegist = true;
					if (multiOper != null) {
						firePropertyChange(true, multiOper);
					}else {
						firePropertyChange(false, null);
					}
				}
			});
		}
		
		if (isAutoQA) {
			if (rowIdDataList.size() <= 0) {
				listeners.firePropertyChange("printData", null, rowId);
			}else {
				listeners.firePropertyChange("printData", null, rowIdDataList);
			}
			rowIdDataList.clear();
		}else {
			if (rowIdDataList.size() >= dataUnit) {
				beginSendData();
			}
		}
	}
	
	private void beginSendData(){
		if (rowIdDataList.size() > 0) {
			if (qaResultViewer != null) {
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
						if (window == null) {
							return;
						}
						if (!window.getActivePage().isPartVisible(qaResultViewer)) {
							window.getActivePage().activate(qaResultViewer);
						}
					}
				});
			}
			
			listeners.firePropertyChange("printData", null, rowIdDataList);
			rowIdDataList.clear();
		}
	}
	
	
	/**
	 * 标志品质检查结束，并将缓存内的数据全部传送到结果视图。
	 */
	public void informQAEndFlag(){
		beginSendData();
		if (qaResultViewer != null) {
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					listeners.firePropertyChange("informQAEndFlag", null, null);
				}
			});
		}
	}
	
	/**
	 * 激活　品质检查结果视图。
	 */
	public void bringQAResultViewerToTop(){
		if (qaResultViewer != null) {
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
					if (window == null) {
						return;
					}
					if (!window.getActivePage().isPartVisible(qaResultViewer)) {
						window.getActivePage().bringToTop(qaResultViewer);
					}
				}
			});
		}
	}
	
	
	public boolean isRegist() {
		return isRegist;
	}
	public void setRegist(boolean isRegist) {
		this.isRegist = isRegist;
	}

	public boolean isAutoQA() {
		return isAutoQA;
	}

	public void setAutoQA(boolean isAutoQA) {
		this.isAutoQA = isAutoQA;
	}

	public MultiFilesOper getMultiOper() {
		return multiOper;
	}

	public void setMultiOper(MultiFilesOper multiOper) {
		this.multiOper = multiOper;
	}

	public List<String> getFilePathList() {
		return filePathList;
	}

	public void setFilePathList(List<String> filePathList) {
		this.filePathList = filePathList;
	}

	public boolean isSameOperObjForAuto() {
		return isSameOperObjForAuto;
	}

	public void setSameOperObjForAuto(boolean isSameOperObjForAuto) {
		this.isSameOperObjForAuto = isSameOperObjForAuto;
	}
	
	
	
}

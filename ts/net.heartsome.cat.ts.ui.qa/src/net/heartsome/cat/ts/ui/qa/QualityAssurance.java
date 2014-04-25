package net.heartsome.cat.ts.ui.qa;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.heartsome.cat.common.resources.ResourceUtils;
import net.heartsome.cat.common.util.CommonFunction;
import net.heartsome.cat.ts.core.file.RowIdUtil;
import net.heartsome.cat.ts.core.qa.QAConstant;
import net.heartsome.cat.ts.core.qa.QATUDataBean;
import net.heartsome.cat.ts.core.qa.QAXmlHandler;
import net.heartsome.cat.ts.ui.qa.model.QAModel;
import net.heartsome.cat.ts.ui.qa.model.QAResult;
import net.heartsome.cat.ts.ui.qa.resource.Messages;
import net.heartsome.cat.ts.util.ProgressIndicatorManager;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 品质检查类 处理所有的品质检查
 * @author robert 2011-12-08
 */
public class QualityAssurance {
	private QAModel model;
	private Shell shell;
	/** 保存某个检查项的实例 */
	private Map<String, QARealization> qaItemClassMap = new HashMap<String, QARealization>();
	private QAXmlHandler handler;
	/** 解析出错时是否继续执行，0为继续，1为出错时继续执行，2为出错时退出执行 */
	private int continuResponse;
	/** 这是进度条的前进间隔，也就是当循环多少个trans-unit节点后前进一格 */
	private static int workInterval = 1;
	/** 处理品质检查结果的model */
	private QAResult qaResult;
	private static String _INFO = Messages.getString("qa.all.dialog.info");
	private static String _ERROR = Messages.getString("qa.all.dialog.error");
	/** 连接符号，用于连接源语言和目标语言的种类，例如“zh-CN -&gt; en” ，这个要与nattable界面上的过滤条件保持一致*/
	private static final String Hyphen = " -> ";
	public final static Logger logger = LoggerFactory.getLogger(QualityAssurance.class.getName());

	public QualityAssurance(QAModel model) {
		this.model = model;
		this.shell = model.getShell();
		init();
	}

	/**
	 * 初始化相关实例
	 */
	public void init() {

	}

	//------------------下面是优化品质检查的试用代码
	public void beginQA(final QAResult qaResult) {
		this.qaResult = qaResult;
		handler = new QAXmlHandler();
		
		Job job = new Job(Messages.getString("qa.all.qa")) {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				long time1 = System.currentTimeMillis();
				// 品质检查项的总数
				int fileNum = model.getQaXlfList().size();
				// 定义的进度条总共五格，其中，解析文件一格，进行检查四格
				monitor.beginTask(Messages.getString("qa.QualityAssurance.tip1"), fileNum * 5);
				
				//先将所有的文件进行解析
				if (!openFile(monitor)) {
					monitor.done();
					return Status.CANCEL_STATUS;
				}
				
				if (model.getQaXlfList().size() == 0) {
					MessageDialog.openInformation(shell, _INFO, Messages.getString("qa.QualityAssurance.tip2"));
					return Status.CANCEL_STATUS;
				}
				
				initWorkInterval();
				
				QARealization realization = null;
				
				//先遍历每个文件
				for (int fileIndex = 0; fileIndex < model.getQaXlfList().size(); fileIndex++) {
					final IFile iFile = model.getQaXlfList().get(fileIndex);
					String xlfPath = iFile.getLocation().toOSString();
					String iFileFullPath = iFile.getFullPath().toOSString();
					
					IProgressMonitor subMonitor = new SubProgressMonitor(monitor, 4,
							SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK);
					
					int allTuSum = handler.getNodeCount(xlfPath, "/xliff/file/body//trans-unit");
					subMonitor.beginTask("", allTuSum % workInterval == 0 ? (allTuSum / workInterval) : (allTuSum / workInterval) + 1 );
					subMonitor.setTaskName(MessageFormat.format(Messages.getString("qa.QualityAssurance.tip3"),
							new Object[] { Messages.getString("qa.all.qa"), iFileFullPath }));
					
					int fileNodeSum = handler.getNodeCount(xlfPath, "/xliff/file");
					int lineNumber = 0;			//行号
					int traversalTuIndex = 0;	//遍历tu节点的序列号
					
					for (int fileNodeIdx = QAConstant.QA_FIRST; fileNodeIdx <= fileNodeSum; fileNodeIdx++) {
						//获取语言对
						String srcLang = handler.getNodeAttribute(xlfPath, "/xliff/file[" + fileNodeIdx + "]", "source-language");
						String tgtLang = handler.getNodeAttribute(xlfPath, "/xliff/file[" + fileNodeIdx + "]", "target-language");
						String langPair = srcLang + Hyphen + tgtLang;
						
						int curFileTUNodeSum = handler.getNodeCount(xlfPath, "/xliff/file[" + fileNodeIdx + "]/body//trans-unit");
						for (int tuIndex = QAConstant.QA_FIRST; tuIndex <= curFileTUNodeSum; tuIndex++) {
							traversalTuIndex ++;
							String tuXpath = "/xliff/file[" + fileNodeIdx + "]/body/descendant::trans-unit[" + tuIndex + "]";
							QATUDataBean dataBean = handler.getFilteredTUText(xlfPath, tuXpath, model.getNotInclude());
							
							//如果返回的map为null，则进行下一循环，行号也不加自加，这样可以保持这里的行号与界面上的行号一致性，方便定位，出现这种情况的可能很小，因为　rowID 的过滤已经处理过了。
							if (dataBean == null) {
								if (!monitorWork(subMonitor, traversalTuIndex, false)) {
									closeDB();
									return Status.CANCEL_STATUS;
								}
								continue;
							}
							
							lineNumber ++;
							if (lineNumber == 73) {
								System.out.println("开始处理。。。。");
							}
							// 未通过　过滤器过滤的情况，
							if (!dataBean.isPassFilter()) {
								if (!monitorWork(subMonitor, traversalTuIndex, false)) {
									closeDB();
									return Status.CANCEL_STATUS;
								}
								continue;
							}
							
							dataBean.setLineNumber(lineNumber + "");
							dataBean.setFileName(iFile.getName());
							dataBean.setSrcLang(srcLang);
							dataBean.setTgtLang(tgtLang);
							dataBean.setXlfPath(xlfPath);
							
							for (int i = 0; i < model.getBatchQAItemIdList().size(); i++) {
								final String qaItemId = model.getBatchQAItemIdList().get(i);
								realization = getClassInstance(qaItemId);
								// 若没有该项检查的实例，提示出错
								if (realization == null) {
									Display.getDefault().asyncExec(new Runnable() {
										public void run() {
											MessageDialog.openError(shell, _ERROR,
													MessageFormat.format(Messages.getString("qa.QualityAssurance.tip4"), new Object[] { model.getQaItemId_Name_Class()
															.get(qaItemId).get(QAConstant.QA_ITEM_NAME) }));
										}
									});
									closeDB();
									return Status.CANCEL_STATUS;
								}
								// 开始进行该项文件的该项检查
								final String result = realization.startQA(model, subMonitor, iFile, handler, dataBean);
								// 当未设置术语库、拼写检查词典配置 错误时才会返回 null，所以这时可以直接删。
								if (result == null) {
									model.getBatchQAItemIdList().remove(qaItemId);
									i --;
								}
								if (monitor.isCanceled()) {
									return Status.CANCEL_STATUS;
								}
							}
							// UNDO 发现这里很耗时，需要处理。
							qaResult.sendDataToViewer(null);
							if (!monitorWork(subMonitor, traversalTuIndex, false)) {
								closeDB();
								return Status.CANCEL_STATUS;
							}
						}
						if (!monitorWork(subMonitor, traversalTuIndex, true)) {
							closeDB();
							return Status.CANCEL_STATUS;
						}
					}
					subMonitor.done();
				}
				closeDB();
				qaResult.informQAEndFlag();
				monitor.done();
				System.out.println("所用时间为" + (System.currentTimeMillis() - time1));
				return Status.OK_STATUS;
			}
		};
		
		// 当程序退出时，检测当前　job 是否正常关闭
		CommonFunction.jobCantCancelTip(job);
		job.addJobChangeListener(new JobChangeAdapter(){
			@Override
			public void running(IJobChangeEvent event) {
				ProgressIndicatorManager.displayProgressIndicator();
				super.running(event);
			}
			@Override
			public void done(IJobChangeEvent event) {
				ProgressIndicatorManager.hideProgressIndicator();
				super.done(event);
			}
		});
		
		job.setUser(true);
		job.schedule();
	}
	
	/**
	 * 开始处理多个合并打开文件的品质检查，由于合并打开文件的处理，是个很特殊的例子，因为处理对像不再是单个文件，而是针对语言对。
	 * @param qaResult ;
	 */
	public void beginMultiFileQA(final QAResult qaResult) {
		this.qaResult = qaResult;
		handler = new QAXmlHandler();
		Job job = new Job(Messages.getString("qa.all.qa")) {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				long time1 = System.currentTimeMillis();
				int fileNum = model.getQaXlfList().size();
				// 定义的进度条总共五格，其中，解析文件1格，进行检查9格
				monitor.beginTask(Messages.getString("qa.QualityAssurance.tip1"), fileNum * 10);
				
				//先将所有的文件进行解析
				if (!openFile(monitor)) {
					monitor.done();
					return Status.CANCEL_STATUS;
				}
				
				if (model.getQaXlfList().size() == 0) {
					MessageDialog.openInformation(shell, _INFO, Messages.getString("qa.QualityAssurance.tip2"));
					return Status.CANCEL_STATUS;
				}
				
				initWorkInterval();
				int allTUSize = handler.getAllTUSize();
				int traversalTuIndex = 0;	//遍历tu节点的序列号
				IProgressMonitor subMonitor = new SubProgressMonitor(monitor, fileNum * 9,
						SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK);
				subMonitor.beginTask("", allTUSize % workInterval == 0 ? (allTUSize / workInterval) : (allTUSize / workInterval) + 1);
				QARealization realization = null;
				Map<String, ArrayList<String>> languageList = handler.getLanguages();
				int lineNumber = 0;
				IFile iFile;
				for(Entry<String, ArrayList<String>> langEntry : languageList.entrySet()){
					String srcLang = langEntry.getKey();
					for(String tgtLang : langEntry.getValue()){
						List<String> rowIdsList = handler.getAllRowIdsByLanguages(srcLang.toUpperCase(), tgtLang.toUpperCase());
						model.setRowIdsList(rowIdsList);
						//开始针对每一个文本段进行检查
						for(String rowId : rowIdsList){
							traversalTuIndex ++;
							lineNumber = rowIdsList.indexOf(rowId) + 1;	//行号
							String filePath = RowIdUtil.getFileNameByRowId(rowId);
							iFile = ResourceUtils.fileToIFile(filePath);
							String langPair = srcLang + Hyphen + tgtLang;
							QATUDataBean tuDataBean = handler.getFilteredTUText(filePath,
									RowIdUtil.parseRowIdToXPath(rowId), model.getNotInclude());
							if (tuDataBean == null) {
								if (!monitorWork(subMonitor, traversalTuIndex, false)) {
									closeDB();
									return Status.CANCEL_STATUS;
								}
								continue;
							}
							
							if (!tuDataBean.isPassFilter()) {
								if (!monitorWork(subMonitor, traversalTuIndex, false)) {
									closeDB();
									return Status.CANCEL_STATUS;
								}
								continue;
							}
							
							tuDataBean.setLineNumber(lineNumber + "");
							tuDataBean.setFileName(iFile.getName());
							tuDataBean.setSrcLang(srcLang);
							tuDataBean.setTgtLang(tgtLang);
							
							for (int i = 0; i < model.getBatchQAItemIdList().size(); i++) {
								final String qaItemId = model.getBatchQAItemIdList().get(i);
								realization = getClassInstance(qaItemId);
								// 若没有该项检查的实例，提示出错
								if (realization == null) {
									Display.getDefault().asyncExec(new Runnable() {
										public void run() {
											MessageDialog.openError(shell, _ERROR,
													MessageFormat.format(Messages.getString("qa.QualityAssurance.tip4"), new Object[] { model.getQaItemId_Name_Class()
															.get(qaItemId).get(QAConstant.QA_ITEM_NAME) }));
										}
									});
									closeDB();
									return Status.CANCEL_STATUS;
								}
								// 开始进行该项文件的该项检查
								final String result = realization.startQA(model, subMonitor, iFile, handler, tuDataBean);
								// // 当未设置术语库、拼写检查词典配置 错误时才会返回 null，所以这时可以直接删。
								if (result == null) {
									model.getBatchQAItemIdList().remove(qaItemId);
									i --;
								}
								if (monitor.isCanceled()) {
									return Status.CANCEL_STATUS;
								}
							}
							qaResult.sendDataToViewer(null);
							
							if (!monitorWork(subMonitor, traversalTuIndex, false)) {
								closeDB();
								return Status.CANCEL_STATUS;
							}
						}
					}
					if (!monitorWork(subMonitor, traversalTuIndex, true)) {
						closeDB();
						return Status.CANCEL_STATUS;
					}
					
				}
				closeDB();
				qaResult.informQAEndFlag();
				subMonitor.done();
				monitor.done();
				System.out.println("所用时间为" + (System.currentTimeMillis() - time1));
				return Status.OK_STATUS;
			}
		};
		
		// 当程序退出时，检测当前　job 是否正常关闭
		CommonFunction.jobCantCancelTip(job);
		job.addJobChangeListener(new JobChangeAdapter(){
			@Override
			public void running(IJobChangeEvent event) {
				ProgressIndicatorManager.displayProgressIndicator();
				super.running(event);
			}
			@Override
			public void done(IJobChangeEvent event) {
				ProgressIndicatorManager.hideProgressIndicator();
				super.done(event);
			}
		});
		
		job.setUser(true);
		job.schedule();
	}

	/**
	 * 获取某个检查项实现类的实例
	 * @param qaItemId
	 * @return
	 */
	public QARealization getClassInstance(String qaItemId) {
		if (qaItemClassMap.get(qaItemId) != null) {
			return (QARealization) qaItemClassMap.get(qaItemId);
		}

		try {
			HashMap<String, String> valueMap = model.getQaItemId_Name_Class().get(qaItemId);
			Object obj = null;
			try {
				obj = Class.forName(valueMap.get(QAConstant.QA_ITEM_CLASSNAME)).newInstance();
			} catch (Exception e) {
				e.printStackTrace();
				logger.error(
						MessageFormat.format(Messages.getString("qa.QualityAssurance.log1"),
								valueMap.get(QAConstant.QA_ITEM_NAME)), e);
			}
			if (QARealization.class.isInstance(obj)) {
				qaItemClassMap.put(qaItemId, (QARealization) obj);
				((QARealization) obj).setParentQaResult(qaResult);
				
				return (QARealization) obj;
			}
		} catch (Exception e) {
			logger.error(Messages.getString("qa.all.qaError"), e);
		}

		return null;
	}

	/**
	 * 解析该文件，若返回false,则标志退出程序的执行
	 */
	public boolean openFile(IProgressMonitor monitor) {
		
		for (int fileIndex = 0; fileIndex < model.getQaXlfList().size(); fileIndex++) {
			IProgressMonitor subMonitor = new SubProgressMonitor(monitor, 1,
					SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK);
			final IFile iFile = model.getQaXlfList().get(fileIndex);
			subMonitor.setTaskName(MessageFormat.format(Messages.getString("qa.QualityAssurance.tip5"), new Object[] {
					Messages.getString("qa.all.qa"), iFile.getFullPath().toString() }));
			continuResponse = QAConstant.QA_ZERO;
			
			try {
				Map<String, Object> newResultMap = handler.openFile(iFile.getLocation().toOSString(), subMonitor);
				// 针对退出解析
				if (newResultMap != null
						&& QAConstant.RETURNVALUE_RESULT_RETURN.equals(newResultMap.get(QAConstant.RETURNVALUE_RESULT))) {
					return false;
				}
				
				if (newResultMap == null
						|| QAConstant.RETURNVALUE_RESULT_SUCCESSFUL != (Integer) newResultMap
								.get(QAConstant.RETURNVALUE_RESULT)) {
					// 针对文件解析出错
					Display.getDefault().syncExec(new Runnable() {
						public void run() {
							boolean response = MessageDialog.openConfirm(shell, _ERROR, MessageFormat.format(
									Messages.getString("qa.QualityAssurance.tip6"), new Object[] { iFile.getFullPath().toOSString() }));
							if (response) {
								continuResponse = QAConstant.QA_FIRST;
							} else {
								continuResponse = QAConstant.QA_TWO;
							}
						}
					});
				}
				
				if (continuResponse == QAConstant.QA_FIRST) {
					model.getQaXlfList().remove(fileIndex);
					fileIndex--;
					continue;
				} else if (continuResponse == QAConstant.QA_TWO) {
					return false;
				}
				
			} catch (Exception e) {
				e.printStackTrace();
				logger.error(Messages.getString("qa.all.log.openXmlError"), e);
			}
		}
		return true;
	}
	
	/**
	 * 进度条前进处理类，若返回false,则标志退出程序的执行
	 * @param monitor
	 * @param traversalTuIndex
	 * @param last
	 * @return ;
	 */
	public boolean monitorWork(IProgressMonitor monitor, int traversalTuIndex, boolean last){
		if (last) {
			if (traversalTuIndex % workInterval != 0) {
				if (monitor.isCanceled()) {
					return false;
				}
				monitor.worked(1);
			}
		}else {
			if (traversalTuIndex % workInterval == 0) {
				if (monitor.isCanceled()) {
					return false;
				}
				monitor.worked(1);
			}
		}
		return true;
		
	}
	
	/**
	 * 关闭数据库，只针对术语一致性查
	 *  ;
	 */
	public void closeDB(){
		System.out.println("关闭数据库");
		if (qaItemClassMap.get(QAConstant.QA_TERM) != null) {
			QARealization termRealize = qaItemClassMap.get(QAConstant.QA_TERM);
			termRealize.closeDB();
		}
	}
	
	/**
	 * 初始化进度条前进前隔值，使之总值不大于五百。
	 */
	private int initWorkInterval(){
		int allTUSize = 0;
		for (IFile iFile : model.getQaXlfList()) {
			allTUSize += handler.getTuSizeMap().get(iFile.getLocation().toOSString());
		}
		if (allTUSize > 500) {
			workInterval = allTUSize / 500;
		}
		return allTUSize;
	}
}

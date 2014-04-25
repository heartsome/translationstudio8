package net.heartsome.cat.ts.ui.qa.views;

import static net.heartsome.cat.ts.ui.Constants.SEGMENT_LINE_SPACING;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.heartsome.cat.common.innertag.InnerTagBean;
import net.heartsome.cat.common.innertag.factory.PlaceHolderEditModeBuilder;
import net.heartsome.cat.common.innertag.factory.XliffInnerTagFactory;
import net.heartsome.cat.common.resources.ResourceUtils;
import net.heartsome.cat.common.ui.innertag.InnerTagRender;
import net.heartsome.cat.common.ui.utils.InnerTagUtil;
import net.heartsome.cat.ts.core.file.RowIdUtil;
import net.heartsome.cat.ts.core.qa.QAConstant;
import net.heartsome.cat.ts.ui.Constants;
import net.heartsome.cat.ts.ui.bean.XliffEditorParameter;
import net.heartsome.cat.ts.ui.editors.IXliffEditor;
import net.heartsome.cat.ts.ui.qa.Activator;
import net.heartsome.cat.ts.ui.qa.export.ExportQAResult;
import net.heartsome.cat.ts.ui.qa.export.ExportQAResultDialog;
import net.heartsome.cat.ts.ui.qa.model.QAResult;
import net.heartsome.cat.ts.ui.qa.model.QAResultBean;
import net.heartsome.cat.ts.ui.qa.resource.Messages;
import net.heartsome.cat.ts.ui.util.MultiFilesOper;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseWheelListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.GlyphMetrics;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.TextLayout;
import org.eclipse.swt.graphics.TextStyle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.part.ViewPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.kupzog.ktable.KTable;
import de.kupzog.ktable.KTableCellEditor;
import de.kupzog.ktable.KTableCellRenderer;
import de.kupzog.ktable.KTableCellResizeListener;
import de.kupzog.ktable.KTableDefaultModel;
import de.kupzog.ktable.KTableModel;
import de.kupzog.ktable.SWTX;
import de.kupzog.ktable.renderers.FixedCellRenderer;
import de.kupzog.ktable.renderers.TextCellRenderer;

/**
 * 品质检查结果视图
 * @author robert 2011-11-12
 */
public class QAResultViewPart extends ViewPart implements PropertyChangeListener {

	/** 常量，视图ID。 */
	public static final String ID = "net.heartsome.cat.ts.ui.qa.views.QAResultViewPart";
	private Composite parent;
	private KTable table;
	private KtableModel tableModel;
	private List<QAResultBean> dataList = new ArrayList<QAResultBean>();
	private QAResult qaResult;
	private IWorkbenchWindow window;
	private Image exportImg;
	private boolean isMultiFile;
	private MultiFilesOper oper;
	private final static String XLIFF_EDITOR_ID = "net.heartsome.cat.ts.ui.xliffeditor.nattable.editor";
	public final static Logger logger = LoggerFactory.getLogger(QAResultViewPart.class.getName());
	/** 标识当前品质检查结果视图所处理的文件路径的集合 */
	private List<String> filePathList = null;
	private MenuItem exportItem;
	/** 标志品质检查是否结束，若没有结束，导出品质检查结果视图时，需等待 */
	private boolean isQAEnd = true;

	private Image errorImg;
	private Image warningImg; 
	private Image deleteImage;
	
	public QAResultViewPart() {
		errorImg = Activator.getImageDescriptor("icons/error.png").createImage();
		warningImg = Activator.getImageDescriptor("icons/warning.png").createImage();
		exportImg = Activator.getImageDescriptor("images/export.png").createImage();
		deleteImage = Activator.getImageDescriptor("images/delete.png").createImage();
	}

	@Override
	public void createPartControl(Composite parent) {
		this.parent = parent;
		window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		createTable();
	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}

	@Override
	public void dispose() {
		if (qaResult != null) {
			qaResult.listeners.removePropertyChangeListener(this);
		}
		
		if (errorImg != null && !errorImg.isDisposed()) {
			errorImg.dispose();
		}
		
		if (warningImg != null && !warningImg.isDisposed()) {
			warningImg.dispose();
		}
		
		if (deleteImage != null && !deleteImage.isDisposed()) {
			deleteImage.dispose();
		}
		if (exportImg != null && !exportImg.isDisposed()) {
			exportImg.dispose();
		}
		
		super.dispose();
	}


	public void createTable() {
		table = new KTable(parent, SWT.FULL_SELECTION | SWT.V_SCROLL | SWT.H_SCROLL | SWTX.FILL_WITH_LASTCOL | SWT.WRAP);
		tableModel = new KtableModel();
		table.setModel(tableModel);
		table.setLayoutData(new GridData(GridData.FILL_BOTH));
		table.setColorRightBorder(Display.getCurrent().getSystemColor(SWT.COLOR_LIST_BACKGROUND));
		table.setColorLeftBorder(Display.getCurrent().getSystemColor(SWT.COLOR_LIST_BACKGROUND));
		table.setColorTopBorder(Display.getCurrent().getSystemColor(SWT.COLOR_LIST_BACKGROUND));
		table.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_LIST_BACKGROUND));
		
		table.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDoubleClick(MouseEvent e) {
				locationRow();
			}
		});

		table.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				if (e.keyCode == QAConstant.QA_CENTERKEY_1 || e.keyCode == QAConstant.QA_CENTERKEY_2) {
					locationRow();
				}
			}
		});
		table.addCellResizeListener(new KTableCellResizeListener() {
			
			public void rowResized(int arg0, int arg1) {
				tableModel.textRenderer.clearRowHeiMap();
				table.redraw();
			}
			
			public void columnResized(int arg0, int arg1) {
				tableModel.textRenderer.clearRowHeiMap();
				table.redraw();
			}
		});
		
		table.addControlListener(new ControlAdapter() {
			@Override
			public void controlResized(ControlEvent e) {
				tableModel.textRenderer.clearRowHeiMap();
				table.redraw();
			}
		});
		
		table.addMouseWheelListener(new MouseWheelListener() {
			
			public void mouseScrolled(MouseEvent e) {
				tableModel.textRenderer.clearRowHeiMap();
				table.redraw();
			}
		});

		createPropMenu();
	}
	
	

	/**
	 * 创建右键参数
	 */
	private void createPropMenu() {
		Menu propMenu = new Menu(table);
		table.setMenu(propMenu);

		MenuItem deletWarnItem = new MenuItem(propMenu, SWT.NONE);
		deletWarnItem.setText(Messages.getString("views.QAResultViewPart.deletWarnItem"));
		deletWarnItem.setImage(deleteImage);
		deletWarnItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				QAResultBean bean;
				for (int i = 0; i < dataList.size(); i++) {
					bean = dataList.get(i);
					// 0为错误，1为警告
					if (1 == bean.getLevel()) {
						dataList.remove(bean);
						i--;
					}
				}
				tableModel.textRenderer.clearRowHeiMap();
				table.redraw();
			}
		});

		MenuItem deleteAllItem = new MenuItem(propMenu, SWT.NONE);
		deleteAllItem.setText(Messages.getString("views.QAResultViewPart.deleteAllItem"));
		deleteAllItem.setImage(deleteImage);
		deleteAllItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				dataList.clear();
				tableModel.textRenderer.clearRowHeiMap();
				table.redraw();
			}
		});
		
		// 导出品质检查报告
		exportItem = new MenuItem(propMenu, SWT.NONE);
		exportItem.setText(Messages.getString("qa.views.QAResultViewPart.exportPopMenu"));
		exportItem.setImage(exportImg);
		exportItem.setEnabled(false);
		exportItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				ExportQAResultDialog dialog = new ExportQAResultDialog(getSite().getShell());
				int result = dialog.open();
				if (result == IDialogConstants.OK_ID) {
					final String exportFilePath = dialog.getExportFilePath();
					if (exportFilePath == null || exportFilePath.isEmpty()) {
						MessageDialog.openInformation(getSite().getShell(), Messages.getString("qa.all.dialog.info"),
								Messages.getString("qa.views.QAResultViewPart.msg.nullFilePath"));
						return;
					}
					
					IRunnableWithProgress runnable = new IRunnableWithProgress() {
						public void run(IProgressMonitor monitor) throws InvocationTargetException,
								InterruptedException {
							monitor.beginTask("", 1);
							while(!isQAEnd){
								try {
									Thread.sleep(500);
								} catch (Exception e2) {
									logger.error("", e2);
								}
								if (monitor.isCanceled()) {
									return;
								}
							}
							List<QAResultBean> exportDataList = new ArrayList<QAResultBean>();
							exportDataList.addAll(dataList);
							ExportQAResult export = new ExportQAResult(isMultiFile, exportFilePath);
							export.beginExport(dataList, filePathList, monitor);
							monitor.done();
						}
					};
					
					try {
						new ProgressMonitorDialog(getSite().getShell()).run(true, true, runnable);
					} catch (Exception e1) {
						logger.error("", e1);
					}
				}
			}
		});
		
		
		propMenu.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				if(deleteImage!= null && !deleteImage.isDisposed()){
					deleteImage.dispose();
				}
			}
		});
	}


	public void propertyChange(final PropertyChangeEvent evt) {
		/*
		 * 备注，传过来的数据是一个 ArrayList<QAResultBean>, 每组数据都是相同的 rowId
		 */
		if ("printData".equals(evt.getPropertyName())) {
			try {
				Display.getDefault().syncExec(new Runnable() {
					@SuppressWarnings("unchecked")
					public void run() {
						Object obj = evt.getNewValue();
						if (obj instanceof List) {
							List<QAResultBean> objList = (List<QAResultBean>) obj;
							if (objList.size() <= 0) {
								return;
							}
							isQAEnd = false;
							String rowId = objList.get(0).getRowId();
							// 如果是自动检查。那么要删除之前的记录
							int addIndex = -1;
							if (qaResult.isAutoQA()) {
								if (qaResult.isSameOperObjForAuto()) {
									for(int i = 0; i < dataList.size(); i ++){
										QAResultBean bean = dataList.get(i);
										if (rowId.equals(bean.getRowId())) {
											dataList.remove(bean);
											addIndex = i;
											i --;
										}
									}
									if (addIndex == -1) {
										addIndex = 0;
									}
									dataList.addAll(addIndex, objList);
									tableModel.textRenderer.clearRowHeiMap();
									table.redraw();
								}else {
									dataList.clear();
									tableModel.textRenderer.clearRowHeiMap();
									table.redraw();
									
									filePathList = qaResult.getFilePathList();
									qaResult.setSameOperObjForAuto(true);
								}
							}else {
								dataList.addAll(objList);
								table.redraw();
							}
							
							if (qaResult.isAutoQA()) {
								if (addIndex > 0) {
									table.setSelection(0, addIndex + 1, true);
								}
							}
						}else if (obj instanceof String) {
							// 这是针对自动品质检查，若一个文本段没有错误，那么就将这个文本段之前的提示进行清除
							if (qaResult.isAutoQA()) {
								if (qaResult.isSameOperObjForAuto()) {
									String rowId = (String) obj;
									for(int i = 0; i < dataList.size(); i ++){
										QAResultBean bean = dataList.get(i);
										if (rowId.equals(bean.getRowId())) {
											dataList.remove(bean);
											i --;
										}
									}
								}else {
									dataList.clear();
									tableModel.textRenderer.clearRowHeiMap();
									table.redraw();
									
									filePathList = qaResult.getFilePathList();
									qaResult.setSameOperObjForAuto(true);
								}
							}
							
							table.redraw();
						}
					}
				});
			} catch (Exception e) {
				e.printStackTrace();
				logger.error(Messages.getString("qa.views.QAResultViewPart.log1"), e);
			}
		} else if ("isMultiFiles".equals(evt.getPropertyName())) {
			try {
				Display.getCurrent().syncExec(new Runnable() {
					public void run() {
						isMultiFile = (Boolean) ((Object[]) evt.getNewValue())[0];
						if (isMultiFile) {
							oper = (MultiFilesOper) ((Object[]) evt.getNewValue())[1];
						}else {
							oper = null;
						}
					}
				});
			} catch (Exception e) {
				e.printStackTrace();
				logger.error(Messages.getString("qa.views.QAResultViewPart.log1"), e);
			}
		}else if ("informQAEndFlag".equals(evt.getPropertyName())) {
			// 通知品质检查已经结束
			isQAEnd = true;
		}
	}

	public void registLister(QAResult qaResult) {
		this.qaResult = qaResult;
		this.qaResult.listeners.addPropertyChangeListener(this);
		
		if (filePathList != null && filePathList.size() > 0) {
			// 自动品质检查这里是不能保存相关信息的
			if (!qaResult.isAutoQA()) {
				filePathList = this.qaResult.getFilePathList();
			}else {
				boolean isSameOperObj = true;
				List<String> curFilePathList = this.qaResult.getFilePathList();
				if (curFilePathList.size() == filePathList.size()) {
					for(String filePath : filePathList){
						if (curFilePathList.contains(filePath)) {
							curFilePathList.remove(filePath);
						}else {
							isSameOperObj = false;
							break;
						}
					}
				}else {
					isSameOperObj = false;
				}
				this.qaResult.setSameOperObjForAuto(isSameOperObj);
			}
		}else {
			filePathList = this.qaResult.getFilePathList();
		}
		if (dataList.size() > 0 || (filePathList != null && filePathList.size() > 0)) {
			exportItem.setEnabled(true);
		}else {
			exportItem.setEnabled(false);
		}
		isQAEnd = false;
	}

	/**
	 * 双击或按回车键，将品质检查结果中的数据定位到翻译界面上去。
	 */
	public void locationRow() {
		int[] selectRow = table.getRowSelection();
		
		if (selectRow.length <= 0) {
			return;
		}
		// 获取第一行选择的值
		QAResultBean bean = dataList.get(selectRow[0] - 1);
		
		// 如果是合并打开的文件
		if (isMultiFile) {
			IXliffEditor xliffEditor = openMultiFilesEditor();
			if (xliffEditor == null) {
				return;
			}
			int lineNumber = xliffEditor.getXLFHandler().getRowIndex(bean.getRowId());
			// 跳转到错误行
			xliffEditor.setFocus();
			xliffEditor.jumpToRow(lineNumber, true);
			return;
		} else {
			// 检查该文件是否已经打开，如果没有打开，就在界面上打开,再返回这个
			IXliffEditor xliffEditor = openEditor(RowIdUtil.getFileNameByRowId(bean.getRowId()));
			if (xliffEditor == null) {
				return;
			}
			int lineNumber = xliffEditor.getXLFHandler().getRowIndex(bean.getRowId());
			// 跳转到错误行
			xliffEditor.setFocus();
			xliffEditor.jumpToRow(lineNumber, false);
		}
	}

	public IXliffEditor openEditor(String filePath) {
		IFile ifile = ResourceUtils.fileToIFile(filePath);
		FileEditorInput fileInput = new FileEditorInput(ifile);

		IEditorReference[] editorRefer = window.getActivePage().findEditors(fileInput, XLIFF_EDITOR_ID,
				IWorkbenchPage.MATCH_INPUT | IWorkbenchPage.MATCH_ID);

		IEditorPart editorPart = null;

		IXliffEditor xliffEditor = null;
		if (editorRefer.length >= 1) {
			editorPart = editorRefer[0].getEditor(true);
			xliffEditor = (IXliffEditor) editorPart;
			// 若该文件未激活，激活此文件
			if (window.getActivePage().getActiveEditor() != editorPart) {
				window.getActivePage().activate(editorPart);
			}
			// 对于已经打开过的文件，进行重排序
			xliffEditor.resetOrder();
		} else { // 如果文件没有打开，那么先打开文件
			try {
				if(!validateXliffCanOpen(ifile)){
					return null;
				}
				xliffEditor = (IXliffEditor) window.getActivePage().openEditor(fileInput, XLIFF_EDITOR_ID, true,
						IWorkbenchPage.MATCH_INPUT | IWorkbenchPage.MATCH_ID);
			} catch (PartInitException e) {
				e.printStackTrace();
				logger.error(Messages.getString("qa.views.QAResultViewPart.log2"), e);
			}
		}

		return xliffEditor;
	}

	/**
	 * 处理合并打开文件 nattable editor的相关问题
	 * @return ;
	 */
	public IXliffEditor openMultiFilesEditor() {
		IXliffEditor xliffEditor = null;
		FileEditorInput fileInput = new FileEditorInput(oper.getCurMultiTempFile());

		IEditorReference[] editorRefer = window.getActivePage().findEditors(fileInput, XLIFF_EDITOR_ID,
				IWorkbenchPage.MATCH_INPUT | IWorkbenchPage.MATCH_ID);

		IEditorPart editorPart = null;

		if (editorRefer.length >= 1) {
			editorPart = editorRefer[0].getEditor(true);
			xliffEditor = (IXliffEditor) editorPart;
			// 若该文件未激活，激活此文件
			if (window.getActivePage().getActiveEditor() != editorPart) {
				window.getActivePage().activate(editorPart);
			}
			// 对于已经打开过的文件，进行重排序
			xliffEditor.resetOrder();
		} else { // 如果文件没有打开，那么先打开文件
			try {
				// 如果保存合并打开所有信息的临时文件已经被删除，那么，重新生成临时文件
				if (!oper.getCurMultiTempFile().getLocation().toFile().exists()) {
					// 检查这两个文件是否重新进行合并打开了的。
					IFile findMultiTempIfile = oper.getMultiFilesTempIFile(true);
					if (findMultiTempIfile != null) {
						fileInput = new FileEditorInput(findMultiTempIfile);
						oper.setCurMultiTempFile(findMultiTempIfile);
					} else {
						//先验证这些所处理的文件是否有已经被打开的
						List<IFile> openedFileList = oper.getOpenedIfile();
						if (openedFileList.size() > 0) {
							String openFileStr = "";
							for(IFile ifile : openedFileList){
								openFileStr += "\t" + ifile.getFullPath().toOSString() + "\n";
							}
							MessageDialog.openInformation(getSite().getShell(), Messages.getString("views.QAResultViewPart.msgTitle"), 
									MessageFormat.format(Messages.getString("qa.views.QAResultViewPart.addTip1"), openFileStr));
							return null;
						}
						
						// 如果选中的文件没有合并打开，那么就重新打开它们
						IFile multiIFile = oper.createMultiTempFile();
						if (multiIFile != null && multiIFile.exists()) {
							fileInput = new FileEditorInput(multiIFile);
							oper.setCurMultiTempFile(multiIFile);
						} else {
							MessageDialog.openInformation(getSite().getShell(),
									Messages.getString("views.QAResultViewPart.msgTitle"),
									Messages.getString("views.QAResultViewPart.msg1"));
							return null;
						}
						
						xliffEditor = (IXliffEditor) window.getActivePage().openEditor(fileInput, XLIFF_EDITOR_ID, true,
								IWorkbenchPage.MATCH_INPUT | IWorkbenchPage.MATCH_ID);
					}
					
				}
			} catch (PartInitException e) {
				e.printStackTrace();
				logger.error(Messages.getString("qa.views.QAResultViewPart.log2"), e);
			}
		}

		return xliffEditor;
	}

	/**
	 * 验证当前要单个打开的文件是否已经被合并打开，针对单个文件的品质检查点击结果进行定位
	 * @return
	 */
	public boolean validateXliffCanOpen(IFile iFile){
		IEditorReference[] editorRes = window.getActivePage().getEditorReferences();
		for (int i = 0; i < editorRes.length; i++) {
			IXliffEditor editor = (IXliffEditor) editorRes[i].getEditor(true);
			if (editor.isMultiFile()) {
				if (editor.getMultiFileList().indexOf(iFile.getLocation().toFile()) != -1) {
					MessageDialog.openInformation(getSite().getShell(), Messages.getString("views.QAResultViewPart.msgTitle"), 
						MessageFormat.format(Messages.getString("qa.views.QAResultViewPart.addTip2"), iFile.getFullPath().toOSString()));
					return false;
				}
			}
		}
		
		return true;
	}
	

	/**
	 * 清除结果显示视图的列表中的数据
	 */
	public void clearTableData() {
		dataList.clear();
		tableModel.textRenderer.clearRowHeiMap();
		table.redraw();
	}
	
	
	
	
	/**
	 * 控制 ktable 的数据显示
	 * @author robert
	 *
	 */
	private class KtableModel extends KTableDefaultModel{
		private static final int levelColWidth = 35;
		private Map<String, Integer> belongMap = new HashMap<String, Integer>();
		
		private final FixedCellRenderer fixedRenderer = new FixedCellRenderer(
				FixedCellRenderer.STYLE_FLAT | TextCellRenderer.INDICATION_FOCUS_ROW);
		
		public final TextRenderer textRenderer = new TextRenderer();
		
		public KtableModel() {
			initialize();
		}
		
		public int getFixedHeaderColumnCount() {
			return 0;
		}

		public int getFixedHeaderRowCount() {
			return 1;
		}

		public int getFixedSelectableColumnCount() {
			return 0;
		}

		public int getFixedSelectableRowCount() {
			return 0;
		}

		public int getRowHeightMinimum() {
			return 20;
		}

		public boolean isColumnResizable(int col) {
			// 第一列不允许更改列宽
			return !(col == 0);
		}

		public boolean isRowResizable(int arg0) {
			return false;
		}

		public KTableCellEditor doGetCellEditor(int arg0, int arg1) {
			return null;
		}
		
		public int getInitialRowHeight(int row) {
			if (row==0) {
				return 22;
			}else {
				return 50;
			}
		}

		public KTableCellRenderer doGetCellRenderer(int col, int row) {
			if (isFixedCell(col, row)) {
				return fixedRenderer;
			} else {
				return textRenderer;
			}
		}
		
		public void doSetContentAt(int arg0, int arg1, Object arg2) {
			// do nothing
		}

		public int doGetColumnCount() {
			return 5;
		}

		public Object doGetContentAt(int col, int row) {
			if (row == 0) {
				switch (col) {
				case 0:
					return Messages.getString("qa.views.QAResultViewPart.columnTipLevel");
				case 1:
					return Messages.getString("qa.views.QAResultViewPart.columnQAType");
				case 2:
					return Messages.getString("qa.views.QAResultViewPart.location");
				case 3:
					return Messages.getString("qa.views.QAResultViewPart.source");
				case 4:
					return Messages.getString("qa.views.QAResultViewPart.target");
				default:
					return "";
				}
			}else {
				if (dataList.size() > 0) {
					QAResultBean bean = dataList.get(row - 1);
					switch (col) {
					case 0:
						if (0 == bean.getLevel()) {
							return errorImg;
						} else if (1 == bean.getLevel()) {
							return warningImg;
						}
					case 1:
						return bean.getQaTypeText();
					case 2:
						return bean.getFileName() + " [" + bean.getLineNumber() + "]";
					case 3:
						return bean.getSrcContent() == null ? "" : bean.getSrcContent();
					case 4:
						return bean.getTgtContent() == null ? "" : bean.getTgtContent();
					default:
						return "";
					}
				}else {
					return "";
				}
			}
		}

		@Override
		public int doGetRowCount() {
			return dataList.size() + 1;
		}

		@Override
		public int getInitialColumnWidth(int col) {
			table.getVerticalBar().getSize();
			int lastWidth = table.getBounds().width - levelColWidth - table.getVerticalBar().getSize().x;
			switch (col) {
			case 0:
				return levelColWidth;
			case 1:
				return (int)(lastWidth * 0.08);
			case 2:
				return (int)(lastWidth * 0.12);
			case 3:
				return (int)(lastWidth * 0.4);
			case 4:
				return (int)(lastWidth * 0.4);
			default:
				return 0;
			}
		}


		@Override
		public Point doBelongsToCell(int col, int row) {
	        if (isFixedCell(col, row)){
	        	return new Point(col, row);
	        }
	        QAResultBean bean = dataList.get(row - 1);
	        
	        if (bean.getMergeId() != null && !bean.getMergeId().isEmpty()) {
	        	if (belongMap.get(bean.getMergeId()) != null) {
	        		if (col <= 1) {
	        			if (row <= belongMap.get(bean.getMergeId())) {
	        				belongMap.put(bean.getMergeId(), row);
						}else {
							return new Point(col, belongMap.get(bean.getMergeId()));
						}
					}
				}else {
					belongMap.put(bean.getMergeId(), row);
				}
			}
	        return new Point(col, row);
		}
	}
	
	
	/**
	 * 级别列的　renderer,主要用于绘画图片。	
	 * @author robert	2013-10-24
	 */
	private class TextRenderer implements KTableCellRenderer {
		protected Display display;
		protected PlaceHolderEditModeBuilder placeHolderBuilder = new PlaceHolderEditModeBuilder();
		protected XliffInnerTagFactory innerTagFactory = new XliffInnerTagFactory(placeHolderBuilder);
		protected InnerTagRender tagRender = new InnerTagRender();

		private final int topPadding = 2;
		private final int rightPadding = 2;
		private final int bottomPadding = 2;
		private final int leftPadding = 2;
		
		private Map<Integer, Integer> rowHeightMap = new HashMap<Integer, Integer>();
		
		public TextRenderer() {
			display = Display.getCurrent();
		}

		public int getOptimalWidth(GC gc, int col, int row, Object content,
				boolean fixed, KTableModel model) {
			// UNDO 这个方法有什么用？到现在为止还不晓得，我哪个去。
//			if (col == 2) {
//				System.out.println(Math.max(gc.stringExtent(content.toString()).x + 8, 20));
//				
//				String text = SWTX.wrapText(gc, content.toString(), model.getRowHeight(row)-6);
//		        int w =  SWTX.getCachedStringExtent(gc, text).y;
//		        w+=6;
//		        System.out.println(w);
//		        return w;
//		        
//			}
//			return Math.max(gc.stringExtent(content.toString()).x + 8, 20);
			return 100;
		}

		public void drawCell(GC gc, Rectangle rect, int col, int row,
				Object content, boolean focus, boolean fixed, boolean clicked,
				KTableModel model) {
			Color backColor;
			Color borderColor = display.getSystemColor(SWT.COLOR_GRAY);
			Color oldForgeColor = gc.getForeground();
			
			if (focus) {
				backColor = display.getSystemColor(SWT.COLOR_LIST_SELECTION);
			} else {
				backColor = display.getSystemColor(SWT.COLOR_LIST_BACKGROUND);
			}

			if (col == 0) {
				gc.setBackground(backColor);
				gc.fillRectangle(rect);
				gc.drawImage(((Image) content), rect.x, rect.y + (rect.height - ((Image) content).getBounds().height) / 2);
				gc.setForeground(borderColor);
				gc.drawLine(rect.x + rect.width, rect.y, rect.x + rect.width, rect.height + rect.y);
				gc.drawLine(rect.x, rect.y + rect.height, rect.x + rect.width, rect.y + rect.height);
				gc.setForeground(oldForgeColor);
			}else {
				if (col == 3 || col == 4) {
					innerTagFactory.reset();
					TextLayout layout = new TextLayout(display);
					layout.setWidth(model.getColumnWidth(col) - leftPadding - rightPadding);
					
					String displayText = InnerTagUtil.resolveTag(innerTagFactory.parseInnerTag((String) content));
					if (XliffEditorParameter.getInstance().isShowNonpirnttingCharacter()) {
						displayText = displayText.replaceAll("\\n", Constants.LINE_SEPARATOR_CHARACTER + "\n");
						displayText = displayText.replaceAll("\\t", Constants.TAB_CHARACTER + "\u200B");
						displayText = displayText.replaceAll(" ", Constants.SPACE_CHARACTER + "\u200B");
					}
					
					gc.setBackground(backColor);
					gc.setForeground(borderColor);
					gc.fillRectangle(rect);
					if (col == 3) {
						gc.drawLine(rect.x + rect.width, rect.y, rect.x + rect.width, rect.height + rect.y);
					}
					gc.drawLine(rect.x, rect.y + rect.height, rect.x + rect.width, rect.y + rect.height);
					gc.setForeground(oldForgeColor);
					layout.setText(displayText);
					
					List<InnerTagBean> innerTagBeans = innerTagFactory.getInnerTagBeans();
					for (InnerTagBean innerTagBean : innerTagBeans) {
						String placeHolder = placeHolderBuilder.getPlaceHolder(innerTagBeans, innerTagBeans.indexOf(innerTagBean));
						int start = displayText.indexOf(placeHolder);
						if (start == -1) {
							continue;
						}
						TextStyle style = new TextStyle();
						Point point = tagRender.calculateTagSize(innerTagBean);
						style.metrics = new GlyphMetrics(point.y, 0, point.x + SEGMENT_LINE_SPACING * 2);
						layout.setStyle(style, start, start + placeHolder.length() - 1);
					}
					layout.draw(gc, rect.x + leftPadding, rect.y + topPadding);

					int curHeight = layout.getBounds().height + topPadding + bottomPadding;
					if (rowHeightMap.get(row) == null || (rowHeightMap.get(row) != null && curHeight > rowHeightMap.get(row))) {
						rowHeightMap.put(row, curHeight);
					}
					
					// UNDO 这里控制自动换行的，还需要更好的设计模式。
					if (col == 4) {
						if (rowHeightMap.get(row) != model.getRowHeight(row)) {
							model.setRowHeight(row, rowHeightMap.get(row));
							table.redraw();
						}
					}
					
					for (InnerTagBean innerTagBean : innerTagBeans) {
						String placeHolder = placeHolderBuilder.getPlaceHolder(innerTagBeans,
								innerTagBeans.indexOf(innerTagBean));
						int start = displayText.indexOf(placeHolder);
						if (start == -1) {
							continue;
						}
						Point p = layout.getLocation(start, false);
						int x = rect.x + p.x + leftPadding;
						x += SEGMENT_LINE_SPACING;

						Point tagSize = tagRender.calculateTagSize(innerTagBean);
						int lineIdx = layout.getLineIndex(start);
						Rectangle r = layout.getLineBounds(lineIdx);
						int y = rect.y + p.y + topPadding + r.height / 2 - tagSize.y / 2;
						tagRender.draw(gc, innerTagBean, x, y);
					}
					
					layout.dispose();
				}else {
					gc.setBackground(backColor);
					gc.fillRectangle(rect);
					gc.setForeground(borderColor);
					gc.drawLine(rect.x + rect.width, rect.y, rect.x + rect.width, rect.height + rect.y);
					gc.drawLine(rect.x, rect.y + rect.height, rect.x + rect.width, rect.y + rect.height);
					gc.setForeground(oldForgeColor);
					
					String text = SWTX.wrapText(gc, content.toString(), model.getColumnWidth(col) - leftPadding - rightPadding);
					int textHeight = gc.textExtent(text).y;
					gc.drawText(text, rect.x + leftPadding, rect.y + (rect.height - textHeight) / 2, true);
					int curHeight = textHeight + topPadding + bottomPadding;
					
					if (rowHeightMap.get(row) == null || (rowHeightMap.get(row) != null && curHeight > rowHeightMap.get(row))) {
						rowHeightMap.put(row, curHeight);
					}
				}
			}
		}
		
		public void clearRowHeiMap(){
			rowHeightMap.clear();
		}
	}
	
	public static void main(String[] args) {
		String text = "this 1 is 5 a 6 test 7 for 8 match.";
		Pattern pattern = Pattern.compile("\\d+");
		Matcher matcher = pattern.matcher(text);
		while (matcher.find()) {
			System.out.println(matcher.group());
			text = matcher.replaceFirst("{" + "a" + "}");
			System.out.println(text);
			matcher = pattern.matcher(text);
		}
	}
	
	
}

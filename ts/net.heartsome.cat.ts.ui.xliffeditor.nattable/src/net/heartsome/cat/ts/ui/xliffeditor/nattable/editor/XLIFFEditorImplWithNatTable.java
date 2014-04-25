package net.heartsome.cat.ts.ui.xliffeditor.nattable.editor;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Vector;

import net.heartsome.cat.common.core.Constant;
import net.heartsome.cat.common.innertag.TagStyle;
import net.heartsome.cat.common.locale.Language;
import net.heartsome.cat.common.locale.LocaleService;
import net.heartsome.cat.common.resources.ResourceUtils;
import net.heartsome.cat.common.util.CommonFunction;
import net.heartsome.cat.common.util.TextUtil;
import net.heartsome.cat.ts.core.bean.NoteBean;
import net.heartsome.cat.ts.core.bean.TransUnitBean;
import net.heartsome.cat.ts.core.file.RowIdUtil;
import net.heartsome.cat.ts.core.file.XLFHandler;
import net.heartsome.cat.ts.ui.Activator;
import net.heartsome.cat.ts.ui.Constants;
import net.heartsome.cat.ts.ui.editors.IXliffEditor;
import net.heartsome.cat.ts.ui.preferencepage.IPreferenceConstants;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.UpdateDataBean;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.config.VerticalNatTableConfig;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.config.XLIFFEditorCompositeLayerConfiguration;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.config.XLIFFEditorSelectionLayerConfiguration;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.dataprovider.VerticalLayerBodyDataProvider;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.dataprovider.XliffEditorDataProvider;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.dialog.CustomFilterDialog;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.exception.UnexpectedTypeExcetion;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.handler.AutoResizeCurrentRowsCommand;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.handler.AutoResizeCurrentRowsCommandHandler;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.handler.UpdateDataAndAutoResizeCommandHandler;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.layer.HorizontalViewportLayer;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.layer.LayerUtil;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.layer.RowHeightCalculator;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.layer.VerticalViewportLayer;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.menu.BodyMenuConfiguration;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.painter.LineNumberPainter;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.painter.StatusPainter;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.painter.XliffEditorGUIHelper;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.painter.XliffEditorGUIHelper.ImageName;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.propertyTester.AddSegmentToTMPropertyTester;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.propertyTester.SignOffPropertyTester;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.propertyTester.UnTranslatedPropertyTester;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.propertyTester.XLIFFEditorSelectionPropertyTester;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.resource.Messages;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.search.command.FindReplaceCommandHandler;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.selection.HorizontalRowSelectionModel;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.selection.IRowIdAccessor;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.selection.RowSelectionProvider;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.selection.VerticalRowSelectionModel;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.sort.NatTableSortModel;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.undoable.UpdateSegmentsOperation;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.utils.NattableUtil;
import net.sourceforge.nattable.NatTable;
import net.sourceforge.nattable.columnRename.RenameColumnHeaderCommand;
import net.sourceforge.nattable.config.AbstractRegistryConfiguration;
import net.sourceforge.nattable.config.CellConfigAttributes;
import net.sourceforge.nattable.config.ConfigRegistry;
import net.sourceforge.nattable.config.DefaultNatTableStyleConfiguration;
import net.sourceforge.nattable.config.IConfigRegistry;
import net.sourceforge.nattable.config.IConfiguration;
import net.sourceforge.nattable.config.IEditableRule;
import net.sourceforge.nattable.coordinate.PositionCoordinate;
import net.sourceforge.nattable.data.IDataProvider;
import net.sourceforge.nattable.data.ISpanningDataProvider;
import net.sourceforge.nattable.data.ReflectiveColumnPropertyAccessor;
import net.sourceforge.nattable.edit.EditConfigAttributes;
import net.sourceforge.nattable.grid.GridRegion;
import net.sourceforge.nattable.grid.command.ClientAreaResizeCommand;
import net.sourceforge.nattable.grid.data.DefaultColumnHeaderDataProvider;
import net.sourceforge.nattable.grid.layer.ColumnHeaderLayer;
import net.sourceforge.nattable.hideshow.ColumnHideShowLayer;
import net.sourceforge.nattable.layer.AbstractLayerTransform;
import net.sourceforge.nattable.layer.CompositeLayer;
import net.sourceforge.nattable.layer.DataLayer;
import net.sourceforge.nattable.layer.ILayer;
import net.sourceforge.nattable.layer.SpanningDataLayer;
import net.sourceforge.nattable.layer.cell.ColumnOverrideLabelAccumulator;
import net.sourceforge.nattable.layer.cell.LayerCell;
import net.sourceforge.nattable.layer.config.DefaultColumnHeaderStyleConfiguration;
import net.sourceforge.nattable.painter.cell.ICellPainter;
import net.sourceforge.nattable.resize.command.MultiColumnResizeCommand;
import net.sourceforge.nattable.search.command.SearchCommand;
import net.sourceforge.nattable.selection.ISelectionModel;
import net.sourceforge.nattable.selection.SelectionLayer;
import net.sourceforge.nattable.selection.command.SelectAllCommand;
import net.sourceforge.nattable.selection.command.SelectCellCommand;
import net.sourceforge.nattable.selection.command.SelectColumnCommand;
import net.sourceforge.nattable.sort.SortHeaderLayer;
import net.sourceforge.nattable.sort.config.SingleClickSortConfiguration;
import net.sourceforge.nattable.style.CellStyleAttributes;
import net.sourceforge.nattable.style.CellStyleUtil;
import net.sourceforge.nattable.style.DisplayMode;
import net.sourceforge.nattable.style.HorizontalAlignmentEnum;
import net.sourceforge.nattable.style.Style;
import net.sourceforge.nattable.util.GUIHelper;
import net.sourceforge.nattable.viewport.ViewportLayer;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.IOperationHistory;
import org.eclipse.core.commands.operations.IUndoContext;
import org.eclipse.core.commands.operations.ObjectUndoContext;
import org.eclipse.core.commands.operations.OperationHistoryFactory;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.util.Util;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.window.DefaultToolTip;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.IURIEditorInput;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.ide.FileStoreEditorInput;
import org.eclipse.ui.ide.ResourceUtil;
import org.eclipse.ui.operations.UndoRedoActionGroup;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.part.FileEditorInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ximpleware.NavException;
import com.ximpleware.XPathEvalException;
import com.ximpleware.XPathParseException;

/**
 * 使用 Nat Table 实现的 XLIFF 编辑器
 * @author Cheney,Weachy,Leakey
 * @since JDK1.5
 */
public class XLIFFEditorImplWithNatTable extends EditorPart implements IXliffEditor {

	private static final Logger LOGGER = LoggerFactory.getLogger(XLIFFEditorImplWithNatTable.class);

	/** 常量，编辑器ID。 */
	public static final String ID = "net.heartsome.cat.ts.ui.xliffeditor.nattable.editor.XLIFFEditorImplWithNatTable";

	/** 标签列的标记 */
	public static final String FLAG_CELL_LABEL = "FLAG_Cell_LABEL";

	/** 可编辑单元格的标记 */
	// public static final String EDIT_CELL_LABEL = "EDIT_Cell_LABEL";
	public static final String SOURCE_EDIT_CELL_LABEL = "SOURCE_EDIT_CELL_LABEL";
	public static final String TARGET_EDIT_CELL_LABEL = "TARGET_EDIT_CELL_LABEL";

	/** 连接符号，用于连接源语言和目标语言的种类，例如“zh-CN -&gt; en” */
	private static final String Hyphen = " -> ";

	/** 编辑器的标题图 */
	private Image titleImage;

	/** 为 NatTable 提供内容的数据提供者 */
	private XliffEditorDataProvider<TransUnitBean> bodyDataProvider;

	/** 需要在 NatTable 中显示的数据对象的属性值 */
	private String[] propertyNames;

	/** 数据对象的属性值对应在 NatTable 中显示的列头名称 */
	private Map<String, String> propertyToLabels;

	/** 数据对象的属性值对应的列在 NatTable 中显示的列宽 */
	private Map<String, Double> propertyToColWidths;

	/** NatTable 中的 Body Region 的 Layout Stack */
	private BodyLayerStack bodyLayer;

	/** NatTable */
	private NatTable table;

	private Combo cmbFilter;

	/** source 列的列名 */
	private String srcColumnName;

	public String getSrcColumnName() {
		return srcColumnName;
	}

	/** target 列的列名 */
	private String tgtColumnName;

	/**
	 * 获取目标语言列名
	 */
	public String getTgtColumnName() {
		return tgtColumnName;
	}

	/** 是否为水平布局 */
	private boolean isHorizontalLayout = true;

	/** NatTable所在的Composite */
	private Composite parent;

	/** XLIFF 文件处理 */
	private XLFHandler handler = new XLFHandler();

	/** The editor's property change listener. */
	// private IPropertyChangeListener fPropertyChangeListener = new PropertyChangeListener();

	/** The editor's font properties change listener. */
	private IPropertyChangeListener fFontPropertyChangeListener = new FontPropertyChangeListener();

	/** 底部状态栏管理器。 */
	private IStatusLineManager statusLineManager;

	/** 显示在状态栏处的条目。 */
	private static XLIFFEditorStatusLineItemWithProgressBar translationItem;
	private static XLIFFEditorStatusLineItemWithProgressBar approveItem;

	private static Image statusLineImage = net.heartsome.cat.ts.ui.xliffeditor.nattable.Activator.getImageDescriptor(
			"icons/fileInfo.png").createImage();

	/** 打开文件成功 */
	private boolean openFileSucceed;

	/** 语言过滤条件 */
	private String langFilterCondition = "";

	/**
	 * 获得语言过滤条件
	 * @return ;
	 */
	public String getLangFilterCondition() {
		return langFilterCondition;
	}

	/** 此编辑器的内部标记显示状态 */
	private TagStyleManager tagStyleManager = new TagStyleManager();

	/**
	 * 得到内部标记样式管理器
	 * @return ;
	 */
	public TagStyleManager getTagStyleManager() {
		return tagStyleManager;
	}

	private boolean multiFile = false;
	/** 当前editor所打开的多个文件的集合 --robert */
	private List<File> multiFileList = new ArrayList<File>();
	/** 是否保存该编辑器，下次系统打开时会重新加载 --robert */
	private boolean isStore = false;

	/** Xliff切割点的list , 其保存的是切割点的rowID, <div style="color:red">这个未排序，请注意</div> --robert */
	private List<String> splitXliffPoints = new ArrayList<String>();

	/**
	 * 是否打开了多文件
	 * @return ;
	 */
	public boolean isMultiFile() {
		return multiFile;
	}

	public XLIFFEditorImplWithNatTable() {
	}

	/**
	 * (non-Javadoc)
	 * @see net.heartsome.cat.ts.ui.editors.IXliffEditor#getXLFHandler()
	 */
	public XLFHandler getXLFHandler() {
		return handler;
	}

	public NatTable getTable() {
		return table;
	}

	public void setSplitXliffPoints(List<String> splitXliffPoints) {
		this.splitXliffPoints = splitXliffPoints;
	}

	/**
	 * 是否是水平布局显示
	 * @return ;
	 */
	public boolean isHorizontalLayout() {
		return isHorizontalLayout;
	}

	/** “重做”、“撤销”操作的历史记录 */
	private static final IOperationHistory HISTORY = OperationHistoryFactory.getOperationHistory();

	/** 绑定到此编辑器实例的“重做”上下文 */
	private IUndoContext undoContext;

	/** “撤销”、“重做” ActionGroup */
	private UndoRedoActionGroup undoRedoGroup;

	/*
	 * Initialize the workbench operation history for our undo context.
	 */
	private void initializeOperationHistory() {
		// create a unique undo context to
		// represent this view's undo history
		undoContext = new ObjectUndoContext(this);

		// set the undo limit for this context based on the preference
		HISTORY.setLimit(undoContext, 99);

		// 初始化“重做、”“撤销”菜单项
		undoRedoGroup = new UndoRedoActionGroup(getSite(), undoContext, true);
	}

	/**
	 * 设置全局的 Action：CUT、COPY、PASTE、SELECT_ALL、DELETE ;
	 */
	private void setGlobalActionHandler() {
		IActionBars actionBars = getEditorSite().getActionBars();
		if (undoRedoGroup != null) {
			undoRedoGroup.fillActionBars(actionBars); // 设置“重做”、“撤销”菜单项
		}
		actionBars.setGlobalActionHandler(ActionFactory.SELECT_ALL.getId(), tableSelectAllAction); // 设置“全选”菜单项
		actionBars.updateActionBars();
	}

	/*
	 * Get the operation history from the workbench.
	 */
	// private IOperationHistory getOperationHistory() {
	// return OperationHistoryFactory.getOperationHistory();
	// // 或者使用：PlatformUI.getWorkbench().getOperationSupport().getOperationHistory();
	// }

	@Override
	public void doSave(IProgressMonitor monitor) {
		// Add By Leakey 释放资源
		handler = null;
		table = null;
		System.gc();
	}

	@Override
	public void doSaveAs() {
		performSaveAs(getProgressMonitor());
	}

	/**
	 * 执行另存为
	 * @param progressMonitor
	 *            进度条;
	 */
	private void performSaveAs(IProgressMonitor progressMonitor) {
		Shell shell = getSite().getShell();
		final IEditorInput input = getEditorInput();
		final IEditorInput newInput; // 新的EditorInput
		final File oldFile; // 原始的file
		// if (input instanceof IURIEditorInput && !(input instanceof IFileEditorInput)) { // 外部文件
		FileDialog dialog = new FileDialog(shell, SWT.SAVE);
		URI uri = ((IURIEditorInput) input).getURI();
		IPath oldPath = URIUtil.toPath(uri);
		if (oldPath != null) {
			dialog.setFileName(oldPath.lastSegment());
			dialog.setFilterPath(oldPath.removeLastSegments(1).toOSString()); // 设置所在文件夹
			oldFile = oldPath.toFile();
		} else {
			oldFile = new File(uri);
		}

		String newPath = dialog.open(); // 得到保存路径
		if (newPath == null) {
			if (progressMonitor != null)
				progressMonitor.setCanceled(true);
			return;
		}

		// 检查文件是否存在，如果存在则确认是否覆盖
		final File localFile = new File(newPath);
		if (localFile.exists()) {
			String msg = MessageFormat.format(Messages.getString("editor.XLIFFEditorImplWithNatTable.msg1"), newPath);
			MessageDialog overwriteDialog = new MessageDialog(shell,
					Messages.getString("editor.XLIFFEditorImplWithNatTable.overwriteDialog"), null, msg,
					MessageDialog.WARNING, new String[] { IDialogConstants.YES_LABEL, IDialogConstants.NO_LABEL }, 1
			/* 'No' is the default */);
			if (overwriteDialog.open() != MessageDialog.OK) {
				if (progressMonitor != null) {
					progressMonitor.setCanceled(true);
					return;
				}
			}
		}

		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IFile file = root.getFileForLocation(URIUtil.toPath(localFile.toURI())); // 得到新文件
		if (file != null) { // 是“WorkSpace”内的文件
			newInput = new FileEditorInput(file);
		} else { // 不是“WorkSpace”内的文件
			try {
				IFileStore fileStore = EFS.getStore(localFile.toURI());
				newInput = new FileStoreEditorInput(fileStore);
			} catch (CoreException ex) {
				// EditorsPlugin.log(ex.getStatus());
				LOGGER.error("", ex);
				String title = Messages.getString("editor.XLIFFEditorImplWithNatTable.msgTitle1");
				String msg = MessageFormat.format(Messages.getString("editor.XLIFFEditorImplWithNatTable.msg2"),
						ex.getMessage());
				MessageDialog.openError(shell, title, msg);
				return;
			}
		}
		// } else {
		// SaveAsDialog dialog = new SaveAsDialog(shell);
		// // 源文件
		// IFile original = (input instanceof IFileEditorInput) ? ((IFileEditorInput) input).getFile() : null;
		// if (original != null) {
		// dialog.setOriginalFile(original); // 添加源文件信息
		// oldFile = original.getLocation().toFile();
		// if ((!oldFile.exists() || !oldFile.canRead()) && original != null) {
		// String message = MessageFormat.format(
		// "The original file ''{0}'' has been deleted or is not accessible.", original.getName());
		// dialog.setErrorMessage(null);
		// dialog.setMessage(message, IMessageProvider.WARNING);
		// }
		// } else {
		// oldFile = null;
		// }
		// dialog.create();
		//
		// if (dialog.open() == MessageDialog.CANCEL) { // 打开“另存为”对话框，用户点击了“取消”按钮
		// if (progressMonitor != null)
		// progressMonitor.setCanceled(true);
		// return;
		// }
		//
		// IPath filePath = dialog.getResult(); // 获得用户选择的路径
		// if (filePath == null) { // 检查路径
		// if (progressMonitor != null)
		// progressMonitor.setCanceled(true);
		// return;
		// }
		//
		// IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		// IFile file = root.getFile(filePath);
		// newInput = new FileEditorInput(file);
		// }

		saveAs(newInput, oldFile, progressMonitor);
	}

	/**
	 * 另存文件
	 * @param newInput
	 * @param oldFile
	 * @param monitor
	 *            ;
	 */
	private void saveAs(IEditorInput newInput, File oldFile, IProgressMonitor monitor) {
		if (newInput == null || oldFile == null) {
			return;
		}
		boolean success = false;
		try {
			if (newInput instanceof FileEditorInput) {
				IFile newFile = (IFile) newInput.getAdapter(IFile.class);
				if (newFile != null) {
					FileInputStream fis = new FileInputStream(oldFile);
					BufferedInputStream bis = new BufferedInputStream(fis);
					if (newFile.exists()) {
						newFile.setContents(bis, false, true, monitor);
					} else {
						newFile.create(bis, true, monitor);
					}
					bis.close();
					fis.close();
				}
			} else if (newInput instanceof FileStoreEditorInput) {
				FileStoreEditorInput storeEditorInput = (FileStoreEditorInput) newInput;
				File newFile = new File(storeEditorInput.getURI());
				copyFile(oldFile, newFile);
			}
			success = true;
		} catch (CoreException e) {
			LOGGER.error("", e);
			final IStatus status = e.getStatus();
			if (status == null || status.getSeverity() != IStatus.CANCEL) {
				String title = Messages.getString("editor.XLIFFEditorImplWithNatTable.msgTitle1");
				String msg = MessageFormat.format(Messages.getString("editor.XLIFFEditorImplWithNatTable.msg2"),
						e.getMessage());
				MessageDialog.openError(getSite().getShell(), title, msg);
			}
		} catch (FileNotFoundException e) {
			LOGGER.error("", e);
			e.printStackTrace();
		} catch (IOException e) {
			LOGGER.error("", e);
			e.printStackTrace();
		} finally {
			if (success) {
				setInput(newInput);
			}
		}

		if (monitor != null) {
			monitor.setCanceled(!success);
		}
	}

	/**
	 * 复制单个文件
	 * @param oldPath
	 *            String 原文件路径 如：c:/fqf.txt
	 * @param newPath
	 *            String 复制后路径 如：f:/fqf.txt
	 * @return boolean
	 */
	private void copyFile(File oldFile, File newFile) {
		try {
			if (oldFile.exists()) { // 原文件存在时
				ResourceUtils.copyFile(oldFile, newFile);
			} else {
				throw new Exception(MessageFormat.format(Messages.getString("editor.XLIFFEditorImplWithNatTable.msg3"),
						oldFile.getAbsolutePath()));
			}
		} catch (Exception e) {
			MessageDialog.openError(getSite().getShell(),
					Messages.getString("editor.XLIFFEditorImplWithNatTable.msgTitle2"), e.getMessage());
			LOGGER.debug(e.getMessage());
			e.printStackTrace();
		}
	}

	@Override
	protected void setInput(IEditorInput input) {
		super.setInput(input);
		setPartName(input.getName());
	}

	/**
	 * 返回与此编辑器相关的进度监视器。
	 * @return 与此编辑器相关的进度监视器
	 */
	protected IProgressMonitor getProgressMonitor() {
		IProgressMonitor pm = null;
		IStatusLineManager manager = getStatusLineManager();
		if (manager != null)
			pm = manager.getProgressMonitor();
		return pm != null ? pm : new NullProgressMonitor();
	}

	/**
	 * 获取此编辑器的状态栏管理者。
	 * @return 状态栏的管理者
	 */
	protected IStatusLineManager getStatusLineManager() {
		return getEditorSite().getActionBars().getStatusLineManager();
	}

	private String titleToolTip;

	@Override
	public String getTitleToolTip() {
		if (titleToolTip != null) {
			return titleToolTip;
		}
		return super.getTitleToolTip();
	}

	// /**
	// * 处理首选项改变事件
	// * @see
	// org.eclipse.ui.texteditor.AbstractTextEditor#handlePreferenceStoreChanged(org.eclipse.jface.util.PropertyChangeEvent)
	// */
	// protected void handlePreferenceStoreChanged(org.eclipse.jface.util.PropertyChangeEvent event) {
	// String property = event.getProperty();
	//
	// if (IPreferenceConstants.CONTEXTBG1.equals(property) || IPreferenceConstants.CONTEXTBG2.equals(property)) {
	// CompositeLayer compositeLayer = (CompositeLayer) table.getLayer();
	// // compositeLayer.clearConfiguration();
	// addRowBackgroundColor(compositeLayer);
	// table.configure();
	// }
	// }

	/**
	 * 添加行背景色（奇数行和偶数行不同）
	 * @param compositeLayer
	 *            ;
	 */
	private void addRowBackgroundColor(CompositeLayer compositeLayer) {
		// Color evenRowBgColor = GUIHelper.getColor(238, 248, 255);
		// Color oddRowBgColor = GUIHelper.getColor(255, 255, 255);
		Style oddStyle = new Style();
		oddStyle.setAttributeValue(CellStyleAttributes.BACKGROUND_COLOR, GUIHelper.COLOR_WHITE);
		Style evenStyle = new Style();
		evenStyle.setAttributeValue(CellStyleAttributes.BACKGROUND_COLOR, GUIHelper.COLOR_WHITE);
		XLIFFEditorCompositeLayerConfiguration compositeLayerConfiguraion = new XLIFFEditorCompositeLayerConfiguration(
				compositeLayer, oddStyle, evenStyle, this);
		compositeLayer.addConfiguration(compositeLayerConfiguraion);
	}

	@Override
	public boolean isDirty() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isSaveAsAllowed() {
		return true; // 允许“另存为”
	}

	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(Messages.getString("editor.XLIFFEditorImplWithNatTable.logger1"));
		}
		setSite(site);
		setInput(input);

		List<File> files = null;
		if (input instanceof FileStoreEditorInput) {
			FileStoreEditorInput editorInput = (FileStoreEditorInput) input;
			files = Arrays.asList(new File(editorInput.getURI()));
			setPartName(input.getName());
		} else if (input instanceof FileEditorInput) {
			FileEditorInput editorInput = (FileEditorInput) input;
			try {
				files = getFilesByFileEditorInput(editorInput);
			} catch (CoreException e) {
				throw new PartInitException(e.getMessage(), e);
			}
			if (files != null) {
				if (files instanceof ArrayList<?>) { // “打开项目”的情况，会返回 java.util.ArrayList<?> 对象。
					if (files.size() <= 0) {
						close(); // 关闭此编辑器。
						return;
					}

					// 设置Editor标题栏的显示名称，否则名称用plugin.xml中的name属性
					StringBuffer nameSB = new StringBuffer();
					for (File file : files) {
						nameSB.append(file.getName() + "、");
					}
					nameSB.deleteCharAt(nameSB.length() - 1);
					String partName = "";
					if (nameSB.length() > 17) {
						partName = nameSB.substring(0, 17) + "...";
					} else {
						partName = nameSB.toString();
					}

					setPartName(partName);
					titleToolTip = nameSB.toString();
					multiFile = true;
					multiFileList = files;
				} else { // 打开文件的一般情况
					setPartName(input.getName());

					String name = input.getName().toLowerCase();
					if (!CommonFunction.validXlfExtensionByFileName(name)) {
						// IFile file = (IFile) input.getAdapter(IFile.class);
						// 打开正转换对话框
						// ConverterCommandTrigger.openConversionDialog(site.getWorkbenchWindow(), file);
						close(); // 关闭此编辑器。
						return;
					}
				}
			}
		}
		// if (files == null || !XLFValidator.validateXlifFiles(files)) {
		// close(); // 关闭此编辑器。
		// return;
		// }
		openFile(files, input);

	}

	/**
	 * 打开文件
	 * @param files
	 * @param input
	 * @throws PartInitException
	 *             ;
	 */
	private void openFile(List<File> files, IEditorInput input) throws PartInitException {
		OpenFile of = new OpenFile(files);
		try {
			// TODO 此处偶尔会出现一个异常 PartInitException，但并不影响正常运行。目前原因不明。
			/**
			 * 异常信息：<br />
			 * Warning: Detected recursive attempt by part net.heartsome.cat.ts.ui.xliffeditor.nattable.editor to create
			 * itself (this is probably, but not necessarily, a bug)
			 */
			if (!PlatformUI.getWorkbench().isStarting()) {
				new ProgressMonitorDialog(getSite().getShell()).run(false, true, of);
			} else {
				of.run(null);
			}
			// of.run(null);
		} catch (InvocationTargetException e) {
			throw new PartInitException(e.getMessage(), e);
		} catch (InterruptedException e) {
			throw new PartInitException(e.getMessage(), e);
		}
		Map<String, Object> result = of.getOpenFileResult();

		if (result == null
				|| Constant.RETURNVALUE_RESULT_SUCCESSFUL != (Integer) result.get(Constant.RETURNVALUE_RESULT)) {
			openFileSucceed = false;
			Throwable e = (Throwable) result.get(Constant.RETURNVALUE_EXCEPTION);
			if (e != null) {
				throw new PartInitException(e.getMessage(), e);
			}
			String msg = (String) result.get(Constant.RETURNVALUE_MSG);
			if (msg == null || msg.length() == 0) {
				msg = Messages.getString("editor.XLIFFEditorImplWithNatTable.msg4");
			}
			MessageDialog.openError(getSite().getShell(),
					Messages.getString("editor.XLIFFEditorImplWithNatTable.msgTitle3"), msg);
			close(); // 关闭此编辑器。
		} else { // 成功打开文件时
			openFileSucceed = true;

			// 判断所打开的文件是否为空，如果为空，进行提示，并关闭编辑器, robert 2013-04-01
			int tuSize = handler.countTransUnit();
			if (tuSize <= 0) {
				MessageDialog.openWarning(getSite().getShell(),
						Messages.getString("dialog.UpdateNoteDialog.msgTitle1"),
						Messages.getString("editor.XLIFFEditorImplWithNatTable.cantOpenNullFile"));
				close();
			}

			Image oldTitleImage = titleImage;
			if (input != null) {
				IEditorRegistry editorRegistry = PlatformUI.getWorkbench().getEditorRegistry();
				IEditorDescriptor editorDesc = editorRegistry.findEditor(getSite().getId());
				ImageDescriptor imageDesc = editorDesc != null ? editorDesc.getImageDescriptor() : null;
				titleImage = imageDesc != null ? imageDesc.createImage() : null;
			}
			// 如果是合并打开，设置不一样的标志
			if (multiFile) {
				setTitleImage(net.heartsome.cat.ts.ui.xliffeditor.nattable.Activator.getImageDescriptor(
						"icons/multiFiles.png").createImage());
			} else {
				setTitleImage(titleImage);
			}
			if (oldTitleImage != null && !oldTitleImage.isDisposed()) {
				oldTitleImage.dispose();
			}

			JFaceResources.getFontRegistry().addListener(fFontPropertyChangeListener);
		}
	}

	/**
	 * 通过 FileEditorInput 得到当前要打开的文件
	 * @param input
	 *            FileEditorInput 对象
	 * @return ;
	 * @throws CoreException
	 */
	private List<File> getFilesByFileEditorInput(FileEditorInput input) throws CoreException {
		List<File> files = null;

		IFile file = (IFile) input.getAdapter(IFile.class);
		if (file == null) {
			throw new CoreException(new Status(Status.WARNING,
					net.heartsome.cat.ts.ui.xliffeditor.nattable.Activator.PLUGIN_ID,
					Messages.getString("editor.XLIFFEditorImplWithNatTable.msg5")));
		} else {
			if ("xlp".equals(file.getFileExtension()) && ".TEMP".equals(file.getParent().getName())) {
				List<String> multiFiles = new XLFHandler().getMultiFiles(file);
				files = new ArrayList<File>();
				File mergerFile;
				for (String multiFileLC : multiFiles) {
					if (CommonFunction.validXlfExtensionByFileName(multiFileLC)) {
						mergerFile = new File(multiFileLC);
						files.add(mergerFile);
					}
				}
			} else {
				files = Arrays.asList(new File(input.getURI()));
			}
		}
		return files;
	}

	@Override
	public void createPartControl(Composite parent) {
		if (openFileSucceed) { // 成功打开文件时
			GridLayoutFactory.fillDefaults().numColumns(1).spacing(0, 0).applyTo(parent);
			parent.setLayoutData(new GridData(GridData.FILL_BOTH));
			// 增加过滤器和定位器 Add By Leakey
			addFilterComposite(parent);
			// 添加下面的面板
			addBottomComposite(parent);
			if (statusLineManager == null) {
				statusLineManager = getStatusLineManager();
				if (statusLineManager.find("translationProgress") == null) {
					translationItem = new XLIFFEditorStatusLineItemWithProgressBar("translationProgress",
							Messages.getString("editor.XLIFFEditorImplWithNatTable.translationItem"));
					statusLineManager.add(translationItem);
				}
				if (statusLineManager.find("approvedProgress") == null) {
					approveItem = new XLIFFEditorStatusLineItemWithProgressBar("approvedProgress",
							Messages.getString("editor.XLIFFEditorImplWithNatTable.approveItem"));
					statusLineManager.add(approveItem);
				}
				translationItem.setProgressValue(0);
				approveItem.setProgressValue(0);
			}
			changeLayout(isHorizontalLayout);
		}
	}

	/**
	 * 关闭此编辑器。 <li>当编辑器关闭自己会抛出 PartInitException 异常警告，因此此方法最适合在 init(IEditorSite site, IEditorInput input) 方法中调用</li>
	 */
	private void close() throws PartInitException {
		Display display = getSite().getShell().getDisplay();
		display.asyncExec(new Runnable() {
			public void run() {
				getSite().getPage().closeEditor(XLIFFEditorImplWithNatTable.this, false);
				// Add By Leakey 释放资源
				if (titleImage != null && !titleImage.isDisposed()) {
					titleImage.dispose();
					titleImage = null;
				}
				if (statusLineImage != null && !statusLineImage.isDisposed()) {
					statusLineImage.dispose();
					statusLineImage = null;
				}
				if (table != null && !table.isDisposed()) {
					table.dispose();
					table = null;
				}
				handler = null;
				System.gc();
				JFaceResources.getFontRegistry().removeListener(fFontPropertyChangeListener);
			}
		});
	}

	/**
	 * 添加填充过滤器面板内容的面板
	 * @param parent
	 * @return 过滤器面板;
	 */
	private void addFilterComposite(Composite main) {
		Composite top = new Composite(main, SWT.NONE);
		GridLayoutFactory.fillDefaults().numColumns(3).equalWidth(false).margins(0, 0).applyTo(top);
		top.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		// 输入行号进行定位
		final String rowLocationStr = Messages.getString("editor.XLIFFEditorImplWithNatTable.rowLocationStr");
		Text txtRowLocation = new Text(top, SWT.BORDER);
		txtRowLocation.setText(rowLocationStr);
		int width = 40;
		if (Util.isLinux()) {
			width = 35;
		}
		GridDataFactory.swtDefaults().hint(width, SWT.DEFAULT).applyTo(txtRowLocation);

		txtRowLocation.addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
				Text text = (Text) e.widget;
				if (rowLocationStr.equals(text.getText())) {
					text.setText("");
				}
			}

			@Override
			public void focusLost(FocusEvent e) {
				Text text = (Text) e.widget;
				if ("".equals(text.getText())) {
					text.setText(rowLocationStr);
				}
			}
		});
		txtRowLocation.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent event) {
				if (event.keyCode == SWT.CR || event.keyCode == SWT.KEYPAD_CR) {
					String rowNumString = ((Text) event.widget).getText().trim();
					if (rowNumString != null && !"".equals(rowNumString)) {
						int rowPosition;
						try {
							rowPosition = Integer.parseInt(rowNumString) - 1;
							jumpToRow(rowPosition, false);
							updateStatusLine();
						} catch (NumberFormatException e) {
							Text text = (Text) event.widget;
							text.setText("");
						}
					}
				}
			}
		});
		txtRowLocation.addVerifyListener(new VerifyListener() {

			public void verifyText(VerifyEvent event) {
				if (event.keyCode == 0 && event.stateMask == 0) { // 文本框得到焦点时

				} else if (Character.isDigit(event.character) || event.character == '\b' || event.keyCode == 127) { // 输入数字，或者按下Backspace、Delete键
					if ("".equals(((Text) event.widget).getText().trim()) && event.character == '0') {
						event.doit = false;
					} else {
						event.doit = true;
					}
				} else {
					event.doit = false;
				}
			}
		});

		cmbFilter = new Combo(top, SWT.BORDER | SWT.READ_ONLY);
		cmbFilter.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		// TODO 完善过滤器初始化数据。
		// cmbFilter.add("所有文本段");
		// cmbFilter.add("未翻译文本段");
		// cmbFilter.add("已翻译文本段");
		// cmbFilter.add("未批准文本段");
		// cmbFilter.add("已批准文本段");
		// cmbFilter.add("有批注文本段");
		// cmbFilter.add("锁定文本段");
		// cmbFilter.add("未锁定文本段");
		// cmbFilter.add("重复文本段");
		// cmbFilter.add("疑问文本段");
		// cmbFilter.add("上下文匹配文本段");
		// cmbFilter.add("完全匹配文本段");
		// cmbFilter.add("模糊匹配文本段");
		// cmbFilter.add("快速翻译文本段");
		// cmbFilter.add("自动繁殖文本段");
		// cmbFilter.add("错误标记文本段");
		// cmbFilter.add("术语不一致文本段");
		// cmbFilter.add("译文不一致文本段");
		// cmbFilter.add("带修订标记文本段");

		final Set<String> filterNames = XLFHandler.getFilterNames();
		for (String filterName : filterNames) {
			cmbFilter.add(filterName);
		}

		// 添加选项改变监听
		cmbFilter.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {

				// Fixed Bug #2243 by Jason 当鼠标焦点在源文单元框中使用过滤器，对过滤后的译文进行操作会提示该行锁定不能操作
				// ActiveCellEditor.commit();
				HsMultiActiveCellEditor.commit(true);
				Combo cmbFilter = (Combo) e.widget;
				boolean isUpdated = handler.doFilter(cmbFilter.getText(), langFilterCondition);
				if (isUpdated) {
					if (table != null) {
						bodyLayer.getSelectionLayer().clear();
						if (bodyLayer.selectionLayer.getRowCount() > 0) {
							// 默认选中第一行
							HsMultiActiveCellEditor.commit(true);
							bodyLayer.selectionLayer.doCommand(new SelectCellCommand(bodyLayer.getSelectionLayer(),
									getTgtColumnIndex(), isHorizontalLayout ? 0 : 1, false, false));
							HsMultiCellEditorControl.activeSourceAndTargetCell(XLIFFEditorImplWithNatTable.this);
						}
						table.setFocus();
					}
					autoResize(); // 自动调整 NatTable 大小 ;
					updateStatusLine(); // 更新状态栏
					NattableUtil.refreshCommand(XLIFFEditorSelectionPropertyTester.PROPERTY_NAMESPACE,
							XLIFFEditorSelectionPropertyTester.PROPERTY_ENABLED);
				}
			}
		});

		Button btnSaveFilter = new Button(top, SWT.NONE);

		// TODO 考虑换成图片显示。
		btnSaveFilter.setText(Messages.getString("editor.XLIFFEditorImplWithNatTable.btnAddFilter"));
		btnSaveFilter.setToolTipText(Messages.getString("editor.XLIFFEditorImplWithNatTable.btnAddFilterTooltip"));
		btnSaveFilter.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				CustomFilterDialog dialog = new CustomFilterDialog(table.getShell(), cmbFilter);
				dialog.open();
				// int res = dialog.open();
				// if (res == CustomFilterDialog.OK) {
				// cmbFilter.select(cmbFilter.getItemCount() - 1); // 选中最后一行数据
				// cmbFilter.notifyListeners(SWT.Selection, null);
				// }
			}
		});

		cmbFilter.select(0); // 默认选中第一行数据
		cmbFilter.notifyListeners(SWT.Selection, null);

		// 更新nattable的列名为语言对
		renameColumn();
		top.pack();
	}

	/**
	 * 刷新编辑器 ;
	 */
	public void refresh() {
		if (table != null && !table.isDisposed()) {
			ViewportLayer viewportLayer = bodyLayer.getViewportLayer();
			viewportLayer.invalidateVerticalStructure();
			viewportLayer.recalculateScrollBars();
			table.redraw();
		}
	}

	public void reloadXliff() {
		Set<String> filePaths = handler.getVnMap().keySet();
		ArrayList<File> files = new ArrayList<File>();
		for (String filePath : filePaths) {
			files.add(new File(filePath));
		}
		try {
			openFile(files, null);
		} catch (PartInitException e) {
			LOGGER.error("", e);
			e.printStackTrace();
		}
	}

	/**
	 * 修改 NatTable 某列的列名
	 * @param columnPosition
	 *            列Position
	 * @param newColumnName
	 *            新列名 ;
	 */
	private void renameColumn() {
		Map<String, ArrayList<String>> languages = handler.getLanguages();
		for (Entry<String, ArrayList<String>> entry : languages.entrySet()) {
			String srcLanguage = entry.getKey();
			srcColumnName = srcLanguage;
			for (String tgtLanguage : entry.getValue()) {
				tgtColumnName = tgtLanguage;
				break;
			}
		}

		if (table != null && !table.isDisposed()) { // 更新列首名
			if (isHorizontalLayout) {
				// 列Position
				int srcColumnIdx = LayerUtil.getColumnPositionByIndex(table, 1);
				int tgtColumnIdx = LayerUtil.getColumnPositionByIndex(table, 3);
				table.doCommand(new RenameColumnHeaderCommand(table, srcColumnIdx, srcColumnName));
				table.doCommand(new RenameColumnHeaderCommand(table, tgtColumnIdx, tgtColumnName));
			} else {
				String langPairStr = srcColumnName + Hyphen + tgtColumnName;
				int langPairIdx = VerticalNatTableConfig.SOURCE_COL_INDEX;
				table.doCommand(new RenameColumnHeaderCommand(table, langPairIdx, langPairStr));
			}
		}
	}

	/**
	 * 跳转到指定行的文本段
	 * @param rowId
	 *            行的唯一标识;
	 */
	public void jumpToRow(String rowId) {

		int rowPosition = handler.getRowIndex(rowId);
		if (rowPosition > -1) {
			jumpToRow(rowPosition, false);
		}
	}

	/**
	 * 跳转到指定行的文本段
	 * @param rowPosition
	 *            行号，从0开始 ;
	 */
	public void jumpToRow(int rowPosition) {
		if (rowPosition < 0) {
			return;
		}
		int[] selectedRows = getSelectedRows();
		if (selectedRows.length == 1 && selectedRows[0] == rowPosition) { // 如果已经选中此行
			return;
		}

		// TODO 已在 target 内容修改的时候判断并将 state 属性值做修改，此处理论上无需再做处理。
		// updateCurrentSegmentTranslateProp(); // 若当前目标文本段内容不为空，则自动将其 state 属性值设为“translated”

		int maxRowNum = handler.countEditableTransUnit() - 1;
		rowPosition = rowPosition > maxRowNum ? maxRowNum : rowPosition;

		if (!isHorizontalLayout) { // 处理垂直布局下的行号
			rowPosition = rowPosition * VerticalNatTableConfig.ROW_SPAN;
		}

		ViewportLayer viewportLayer = bodyLayer.getViewportLayer();
		// 先记录下可见区域的范围
		HsMultiActiveCellEditor.commit(true);
		viewportLayer.doCommand(new SelectCellCommand(bodyLayer.getSelectionLayer(), getTgtColumnIndex(), rowPosition,
				false, false));
		HsMultiCellEditorControl.activeSourceAndTargetCell(this);
	}

	/**
	 * 得到当前选中的行的唯一标识
	 * @return ;
	 */
	public List<String> getSelectedRowIds() {
		SelectionLayer selectionLayer = bodyLayer.getSelectionLayer();
		int[] rowPositions = selectionLayer.getFullySelectedRowPositions();
		Set<String> rowIds = handler.getRowIds(rowPositions);
		return new ArrayList<String>(rowIds);
	}

	/**
	 * 得到当前选中的行
	 * @return ;
	 */
	public int[] getSelectedRows() {
		SelectionLayer selectionLayer = bodyLayer.getSelectionLayer();
		return selectionLayer.getFullySelectedRowPositions();
	}

	/**
	 * 添加下面的面板
	 * @param parent
	 * @return 下面的面板;
	 */
	private void addBottomComposite(Composite main) {
		final Composite bottom = new Composite(main, SWT.NONE);
		GridLayoutFactory.fillDefaults().numColumns(1).margins(0, 0).applyTo(bottom);
		bottom.setLayoutData(new GridData(GridData.FILL_BOTH));

		this.parent = bottom;
	}

	/**
	 * 给 NatTable 添加可编辑单元格的配置
	 * @return ;
	 */
	private IConfiguration editableGridConfiguration() {
		return new AbstractRegistryConfiguration() {

			public void configureRegistry(IConfigRegistry configRegistry) {
				TextPainterWithPadding painter = new TextPainterWithPadding(true, Constants.SEGMENT_TOP_MARGIN,
						Constants.SEGMENT_RIGHT_MARGIN, Constants.SEGMENT_BOTTOM_MARGIN, Constants.SEGMENT_LEFT_MARGIN,
						XLIFFEditorImplWithNatTable.this,
						JFaceResources.getFont(net.heartsome.cat.ts.ui.Constants.XLIFF_EDITOR_TEXT_FONT));
				configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_PAINTER, painter, DisplayMode.NORMAL,
						SOURCE_EDIT_CELL_LABEL);
				configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_PAINTER, painter, DisplayMode.NORMAL,
						TARGET_EDIT_CELL_LABEL);

				// configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_PAINTER, new StyledTextPainter(
				// table), DisplayMode.NORMAL, GridRegion.BODY);
				configRegistry.registerConfigAttribute(CellConfigAttributes.DISPLAY_CONVERTER, new TagDisplayConverter(
						XLIFFEditorImplWithNatTable.this), DisplayMode.NORMAL, SOURCE_EDIT_CELL_LABEL);
				configRegistry.registerConfigAttribute(CellConfigAttributes.DISPLAY_CONVERTER, new TagDisplayConverter(
						XLIFFEditorImplWithNatTable.this), DisplayMode.NORMAL, TARGET_EDIT_CELL_LABEL);

				configRegistry.registerConfigAttribute(EditConfigAttributes.CELL_EDITABLE_RULE,
						IEditableRule.ALWAYS_EDITABLE, DisplayMode.EDIT, SOURCE_EDIT_CELL_LABEL);
				configRegistry.registerConfigAttribute(EditConfigAttributes.CELL_EDITABLE_RULE,
						IEditableRule.ALWAYS_EDITABLE, DisplayMode.EDIT, TARGET_EDIT_CELL_LABEL);

				configRegistry.registerConfigAttribute(EditConfigAttributes.CELL_EDITOR, new StyledTextCellEditor(
						XLIFFEditorImplWithNatTable.this), DisplayMode.EDIT, SOURCE_EDIT_CELL_LABEL);
				configRegistry.registerConfigAttribute(EditConfigAttributes.CELL_EDITOR, new StyledTextCellEditor(
						XLIFFEditorImplWithNatTable.this), DisplayMode.EDIT, TARGET_EDIT_CELL_LABEL);

				// configRegistry.registerConfigAttribute(CellConfigAttributes.DISPLAY_CONVERTER, new
				// TagDisplayConverter(
				// XLIFFEditorImplWithNatTable.this, innerTagUtil), DisplayMode.EDIT, EDIT_CELL_LABEL);
			}
		};
	}

	/* NatTable 可编辑规则定制方法，将来可能会用到，暂时保留 */
	// protected IEditableRule getEditRule(final IDataProvider dataProvider) {
	// return new IEditableRule() {
	// public boolean isEditable(int columnIndex, int rowIndex) {
	// if (isHorizontalLayout) {
	// return true;
	// } else {
	// return VerticalNatTableConfig.isTarget(columnIndex, rowIndex);
	// }
	// }
	// };
	// }

	/**
	 * NatTable 全选的 Action。
	 */
	private Action tableSelectAllAction = new Action() {
		public void runWithEvent(Event event) {
			if (table != null && !table.isDisposed()) {
				table.doCommand(new SelectAllCommand());
			}
		};
	};

	@Override
	public void setFocus() {
		if (table != null && !table.isDisposed()) {
			table.setFocus();
		}
		setGlobalActionHandler(); // 设置全局的菜单项

		updateStatusLine(); // 更新状态栏显示信息
	}

	/**
	 * 在状态栏上显示被编辑文件的信息。
	 */
	public void updateStatusLine() {
		if (table == null || table.isDisposed()) {
			return;
		}
		SelectionLayer selectionLayer = bodyLayer.getSelectionLayer();
		ViewportLayer viewportLayer = bodyLayer.getViewportLayer();
		PositionCoordinate cellPosition = selectionLayer.getLastSelectedCellPosition();
		if (cellPosition == null) {
			return;
		}
		// int order = LayerUtil.convertRowPosition(selectionLayer, cellPosition.rowPosition, viewportLayer);
		// Bug #2317：选中文本段后排序，不会刷新状态栏中的序号
		int[] selectedRowPostions = selectionLayer.getFullySelectedRowPositions();
		if (selectedRowPostions.length <= 0) {
			return;
		}

		// 刷新选中行的术语，使其排序后保持高亮显示
		// if (!isHorizontalLayout()) {
		// int rowPosition = selectedRowPostions[0];
		// rowPosition *= VerticalNatTableConfig.ROW_SPAN;
		// cellPosition.set(rowPosition, cellPosition.getColumnPosition());
		// } else {
		// cellPosition.set(selectedRowPostions[0], cellPosition.getColumnPosition());
		// }

		// if (!FindReplaceDialog.isOpen) {
		// CellRegion cellRegion = new CellRegion(cellPosition, new Region(0, selectionLayer.getWidth()));
		// ActiveCellRegion.setActiveCellRegion(cellRegion);
		// }

		int order = LayerUtil.convertRowPosition(selectionLayer, selectedRowPostions[0], viewportLayer);
		order += viewportLayer.getOriginRowPosition() + 1;

		// 垂直布局时order需要进行两行递增的处理
		if (!isHorizontalLayout) {
			order = (int) Math.ceil(order / 2.0);
		}

		MessageFormat messageFormat = null;
		if (order > 0) {
			/* 一个Xliff文件，可能有多个File节点，这里使用File结点的original属性 */
			/* 当前文件：{0} | 顺序号:{1} | 可见文本段数:{2} | 文本段总数:{3} | 当前用户名" */
			messageFormat = new MessageFormat(Messages.getString("editor.XLIFFEditorImplWithNatTable.messageFormat1"));
		} else {
			messageFormat = new MessageFormat(Messages.getString("editor.XLIFFEditorImplWithNatTable.messageFormat2"));
		}
		String fileName = "";
		// 添加 Project Name
		IEditorInput editorInput = getEditorInput();
		String filePath = "";
		if (isMultiFile()) {
			if (getSelectedRowIds().size() > 0) {
				filePath = RowIdUtil.getFileNameByRowId(getSelectedRowIds().get(0));
				fileName = ResourceUtils.toWorkspacePath(filePath);
			}
		} else {
			fileName = getEditorInput().getName();
			if (editorInput instanceof FileEditorInput) {
				FileEditorInput fileEditorInput = (FileEditorInput) editorInput;
				filePath = fileEditorInput.getFile().getLocation().toOSString();
				fileName = fileEditorInput.getFile().getFullPath().toOSString();
			}
		}

		String systemUser = Activator.getDefault().getPreferenceStore().getString(IPreferenceConstants.SYSTEM_USER);
		int editableTuSum = handler.countEditableTransUnit();
		int tuSum = handler.countTransUnit();
		// int translatedSum1 = handler
		// .getNodeCount(filePath,
		// "/xliff/file/body/trans-unit[@approved = 'yes' and target/@state != 'translated' and target/@state != 'signed-off']");
		// int translatedSum2 = handler.getNodeCount(filePath,
		// "/xliff/file/body/trans-unit/target[@state = 'translated' or @state = 'signed-off']");
		// int approveSum1 = handler.getNodeCount(filePath,
		// "/xliff/file/body/trans-unit[not(@approved='yes') and target/@state='signed-off']");
		// int approveSum2 = handler.getNodeCount(filePath, "/xliff/file/body/trans-unit[@approved = 'yes']");
		int translatedSum = handler.getTranslatedCount();
		int approveedSum = handler.getApprovedCount();

		int approveP = (int) Math.floor(approveedSum / (double) tuSum * 100.00);
		int translatedP = (int) Math.floor(translatedSum / (double) tuSum * 100.00);

		translationItem.setProgressValue(translatedP);
		approveItem.setProgressValue(approveP);
		// 将信息显示在状态栏
		String message = messageFormat.format(new String[] { fileName, String.valueOf(order),
				String.valueOf(editableTuSum), String.valueOf(tuSum), systemUser });
		statusLineManager.setMessage(statusLineImage, message);
	}

	/**
	 * 设置状态栏信息
	 * @param message
	 *            状态栏信息 ;
	 */
	public void setStatusLine(String message) {
		getStatusLineManager().setMessage(message);
	}

	/**
	 * 构建 NatTable 的数据提供者
	 * @param isHorizontalLayout
	 *            true 为水平布局，false 为垂直布局。
	 * @return 数据提供者;
	 */
	private XliffEditorDataProvider<TransUnitBean> setupBodyDataProvider(boolean isHorizontalLayout) {
		XliffEditorDataProvider<TransUnitBean> result = null;
		if (isHorizontalLayout) {
			result = setupHorizontalLayoutBodyDataProvider();
		} else {
			result = setupVerticalLayoutBodyDataProvider();
		}
		return result;
	}

	/**
	 * 构建水平布局的 body data provider
	 * @return 数据提供者;
	 */
	private XliffEditorDataProvider<TransUnitBean> setupHorizontalLayoutBodyDataProvider() {
		propertyToLabels = new HashMap<String, String>();
		propertyToLabels.put("id", Messages.getString("editor.XLIFFEditorImplWithNatTable.idColumn"));
		// Edit By Leakey 动态设置列名
		propertyToLabels.put("srcContent", srcColumnName);
		propertyToLabels.put("flag", Messages.getString("editor.XLIFFEditorImplWithNatTable.statusColumn"));
		propertyToLabels.put("tgtContent", tgtColumnName);

		propertyToColWidths = new HashMap<String, Double>();
		propertyToColWidths.put("id", 45.0); // 大于1的值为像素值，小于等于1的值为除了像素值剩下部分的百分比（例如0.5，表示50％）
		propertyToColWidths.put("srcContent", 0.5);
		propertyToColWidths.put("flag", 85.0);
		propertyToColWidths.put("tgtContent", 0.5);

		propertyNames = new String[] { "id", "srcContent", "flag", "tgtContent" };

		// Edit by Leakey 在转换布局时需要将修改过的值保存下来
		XliffEditorDataProvider<TransUnitBean> dtProvider = new XliffEditorDataProvider<TransUnitBean>(handler,
				new ReflectiveColumnPropertyAccessor<TransUnitBean>(propertyNames));
		return dtProvider;
	}

	/**
	 * 构建垂直布局的 body data provider
	 * @param dataList2
	 *            数据列表 ;
	 */
	private XliffEditorDataProvider<TransUnitBean> setupVerticalLayoutBodyDataProvider() {
		propertyToLabels = new HashMap<String, String>();
		propertyToLabels.put("id", Messages.getString("editor.XLIFFEditorImplWithNatTable.idColumn"));
		propertyToLabels.put("flag", Messages.getString("editor.XLIFFEditorImplWithNatTable.statusColumn"));
		// Edit By Leakey 动态设置列名
		propertyToLabels.put("properties", srcColumnName + Hyphen + tgtColumnName);

		propertyToColWidths = new HashMap<String, Double>();
		propertyToColWidths.put("id", 45.0);
		propertyToColWidths.put("flag", 85.0);
		propertyToColWidths.put("properties", 1.0);

		propertyNames = new String[] { "id", "flag", "properties" };

		// Edit by Leakey 在转换布局时需要将修改过的值保存下来
		VerticalLayerBodyDataProvider<TransUnitBean> dtProvider = new VerticalLayerBodyDataProvider<TransUnitBean>(
				handler, new ReflectiveColumnPropertyAccessor<TransUnitBean>(propertyNames));
		return dtProvider;
	}

	/**
	 * 改变当前布局 ;
	 */
	public void changeLayout() {
		isHorizontalLayout = !isHorizontalLayout;
		changeLayout(isHorizontalLayout);
		// 刷新工具栏的改变布局按钮的图片
		ICommandService commandService = (ICommandService) PlatformUI.getWorkbench().getService(ICommandService.class);
		commandService.refreshElements("net.heartsome.cat.ts.ui.handlers.ChangeXliffEditorModelCommand", null);
	}

	public void changeLayout(boolean isHorizontalLayout) {
		this.isHorizontalLayout = isHorizontalLayout; // 同步布局状态

		// 如果当前的 table 已经存在，销毁后再重新创建。
		if (table != null && !table.isDisposed()) {
			table.dispose();
			for (Control control : parent.getChildren()) {
				control.dispose();
			}
		}

		// 构建 NatTable 的数据提供者
		bodyDataProvider = setupBodyDataProvider(isHorizontalLayout);

		// 构建 NatTable 列头的数据提供者
		DefaultColumnHeaderDataProvider colHeaderDataProvider = new DefaultColumnHeaderDataProvider(propertyNames,
				propertyToLabels);

		// 构建 NatTable 的 body layer stack
		bodyLayer = new BodyLayerStack(bodyDataProvider, isHorizontalLayout);

		// 构建 NatTable 的 column header layout stack
		ColumnHeaderLayerStack columnHeaderLayer = new ColumnHeaderLayerStack(colHeaderDataProvider);

		// 构建 NatTable 之下的 composite layer，不使用默认的 configuration（默认的 configuration 是在点击可编辑单元格时，直接进入编辑状态）。
		CompositeLayer compositeLayer = new CompositeLayer(1, 2);
		compositeLayer.setChildLayer(GridRegion.COLUMN_HEADER, columnHeaderLayer, 0, 0);
		compositeLayer.setChildLayer(GridRegion.BODY, bodyLayer, 0, 1);

		LayerUtil.setBodyLayerPosition(0, 1); // 标识 BodyLayer 在 CompositeLayer 上的位置

		/* 给 composite layer 添加编辑相关的命令和 handler */
		// 添加行背景色（奇数行和偶数行不同）
		addRowBackgroundColor(compositeLayer);

		// 构建 NatTable
		table = new NatTable(parent, compositeLayer, false);
		Language srcLang = LocaleService.getLanguageConfiger().getLanguageByCode(srcColumnName);
		Language tgtLang = LocaleService.getLanguageConfiger().getLanguageByCode(tgtColumnName);
		if (srcLang.isBidi() || tgtLang.isBidi()) {
			table.setOrientation(SWT.RIGHT_TO_LEFT);
		}
		table.removePaintListener(table); // 去除默认绘画器
		table.addPaintListener(paintListenerWithAutoRowSize); // 使用自定义绘画器，此绘画器，具有自动计算行高功能。

		Listener[] ls = table.getListeners(SWT.Resize);
		for (Listener l : ls) {
			table.removeListener(SWT.Resize, l);
		}
		table.addListener(SWT.Resize, resizeListenerWithColumnResize);
		table.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				// ActiveCellEditor.commit(); // 关闭编辑时，先提交未提交的单元格，避免造成内容丢失。Bug #2685
				HsMultiActiveCellEditor.commit(true);
				table.removeListener(SWT.Resize, resizeListenerWithColumnResize);
				table.removePaintListener(paintListenerWithAutoRowSize);
			}
		});

		table.setLayoutData(new GridData(GridData.FILL_BOTH));

		// 给 NatTable 添加相应的配置
		DefaultNatTableStyleConfiguration configuration = new DefaultNatTableStyleConfiguration();
		configuration.hAlign = HorizontalAlignmentEnum.LEFT;
		table.addConfiguration(configuration);

		// To be changed 给NatTable添加选择列的功能 Add By Leakey
		/*
		 * ColumnGroupModel columnGroupModel = new ColumnGroupModel(); DisplayColumnChooserCommandHandler
		 * columnChooserCommandHandler = new DisplayColumnChooserCommandHandler( bodyLayer.getSelectionLayer(),
		 * bodyLayer.getColumnHideShowLayer(), columnHeaderLayer.getColumnHeaderLayer(),
		 * columnHeaderLayer.getColumnHeaderDataLayer(), columnHeaderLayer.getColumnGroupHeaderLayer(), columnGroupModel
		 * ); bodyLayer.registerCommandHandler(columnChooserCommandHandler);
		 */

		/*
		 * 不使用默认的表头菜单，使用自定义的菜单，因此自定义菜单在 corner 中添加了相应的菜单项，所以需要指定这些添加的 command 在哪一层进一处理
		 */
		// 表头中添加自定义菜单会引发一些不可预料的问题，故先去掉
		// table.addConfiguration(new HeaderMenuConfiguration(table));

		/*
		 * 增加表格的自定义右键菜单
		 */
		table.addConfiguration(new BodyMenuConfiguration(this));

		// 注册列头点击监听（处理排序）
		table.addConfiguration(new SingleClickSortConfiguration());

		/*
		 * 初始化“撤销/重做”历史
		 */
		initializeOperationHistory();
		table.setData(IUndoContext.class.getName(), undoContext);

		/* Weachy - 注册修改后保存内容并自适应大小的处理 handler（覆盖默认的handler：UpdateDataCommandHandler） */
		bodyLayer.getBodyDataLayer().registerCommandHandler(
				new UpdateDataAndAutoResizeCommandHandler(table, bodyLayer.getBodyDataLayer()));

		/* Weachy - 注册当前显示行的行高自适应处理 handler */
		compositeLayer.registerCommandHandler(new AutoResizeCurrentRowsCommandHandler(compositeLayer));

		/* Weachy - 移除系统默认的查找 handler，添加自定义的查找替换 handler */
		bodyLayer.getSelectionLayer().unregisterCommandHandler(SearchCommand.class);
		bodyLayer.getSelectionLayer().registerCommandHandler(
				new FindReplaceCommandHandler(bodyLayer.getSelectionLayer()));

		/*
		 * 下面给 NatTable 添加可编辑单元格的配置
		 */
		table.addConfiguration(editableGridConfiguration());

		// 添加标记的自定义显示样式
		IConfigRegistry configRegistry = new ConfigRegistry();
		/*
		 * 如果是水平布局，则使用 ColumnOverrideLabelAccumulator 实现指定列都使用相同的显示样式；否则使用 CellOverrideLabelAccumulator 实现根据显示的内容来显示样式。
		 */
		if (isHorizontalLayout) {
			// 第一步：创建一个标签累加器，给需要绘制会不同效果的 cells 添加自定义的标签。在这里是第三列的标签列。
			ColumnOverrideLabelAccumulator columnLabelAccumulator = new ColumnOverrideLabelAccumulator(bodyLayer);
			columnLabelAccumulator.registerColumnOverrides(0, "LINENUMBER_CELL_LABEL");
			columnLabelAccumulator.registerColumnOverrides(1, SOURCE_EDIT_CELL_LABEL);
			columnLabelAccumulator.registerColumnOverrides(2, FLAG_CELL_LABEL);
			columnLabelAccumulator.registerColumnOverrides(3, TARGET_EDIT_CELL_LABEL);

			// 第二步：注册这个标签累加器。
			bodyLayer.setConfigLabelAccumulator(columnLabelAccumulator);

			// 第三步：把自定义的 cell painter，cell style 与自定义的标签进行关联。
			addFlagLableToColumn(configRegistry);
			addLineNumberToColumn(configRegistry);
		} else {
			ColumnOverrideLabelAccumulator columnLabelAccumulator = new ColumnOverrideLabelAccumulator(bodyLayer);
			columnLabelAccumulator.registerColumnOverrides(0, "LINENUMBER_CELL_LABEL");
			columnLabelAccumulator.registerColumnOverrides(1, FLAG_CELL_LABEL);
			columnLabelAccumulator.registerColumnOverrides(VerticalNatTableConfig.TARGET_COL_INDEX,
					SOURCE_EDIT_CELL_LABEL);
			columnLabelAccumulator.registerColumnOverrides(VerticalNatTableConfig.TARGET_COL_INDEX,
					TARGET_EDIT_CELL_LABEL);
			bodyLayer.setConfigLabelAccumulator(columnLabelAccumulator);

			// CellOverrideLabelAccumulator<TransUnitDummy> cellLabelAccumulator = new
			// CellOverrideLabelAccumulator<TransUnitDummy>(
			// (IRowDataProvider) bodyDataProvider);
			// CellOverrideLabelAccumulator<TransUnitBean> cellLabelAccumulator = new
			// CellOverrideLabelAccumulator<TransUnitBean>(
			// (IRowDataProvider) bodyDataProvider);
			// cellLabelAccumulator.registerOverride("flag", VerticalNatTableConfig.SOURCE_COL_INDEX,
			// FOCUS_CELL_LABEL);
			//
			// bodyLayer.getBodyDataLayer().setConfigLabelAccumulator(cellLabelAccumulator);

			addFlagLableToColumn(configRegistry);
			addLineNumberToColumn(configRegistry);
		}
		table.setConfigRegistry(configRegistry);

		// configure manually
		table.configure();

		/* Weachy - 垂直布局下，注册使键盘方向键以 2 行为一个单位移动选中行的处理 handler（覆盖默认的handler：MoveRowSelectionCommandHandler） */
		if (!isHorizontalLayout) {
			// SelectionLayer selectionLayer = bodyLayer.getSelectionLayer();
			// selectionLayer.registerCommandHandler(new VerticalMoveRowSelectionCommandHandler(selectionLayer));
		}

		if (bodyLayer.selectionLayer.getRowCount() > 0) {
			// 默认选中第一行
			HsMultiActiveCellEditor.commit(true);
			bodyLayer.selectionLayer.doCommand(new SelectCellCommand(bodyLayer.getSelectionLayer(),
					getTgtColumnIndex(), isHorizontalLayout ? 0 : 1, false, false));
			HsMultiCellEditorControl.activeSourceAndTargetCell(this);
		}

		IWorkbenchPage page = getSite().getPage();
		IViewReference[] viewReferences = page.getViewReferences();
		IViewPart view;
		for (int i = 0; i < viewReferences.length; i++) {
			view = viewReferences[i].getView(false);
			if (view == null) {
				continue;
			}
			view.setFocus(); // 切换到其他视图，再切换回来，解决NatTable改变布局后其他视图无法监听到的问题。
			// break;
		}

		// 改变布局方式后，把焦点给 NatTable
		table.setFocus();
		RowHeightCalculator rowHeightCalculator = new RowHeightCalculator(bodyLayer, table, 32);
		ILayer lay = bodyLayer.getViewportLayer();
		if (lay instanceof HorizontalViewportLayer) {
			((HorizontalViewportLayer) bodyLayer.getViewportLayer()).setRowHeightCalculator(rowHeightCalculator);
		} else if (lay instanceof VerticalViewportLayer) {
			((VerticalViewportLayer) bodyLayer.getViewportLayer()).setRowHeightCalculator(rowHeightCalculator);
		}
		parent.layout();

		NoteToolTip toolTip = new NoteToolTip(table);
		toolTip.setPopupDelay(10);
		toolTip.activate();
		toolTip.setShift(new Point(10, 10));

		StateToolTip stateTip = new StateToolTip(table);
		stateTip.setPopupDelay(10);
		stateTip.activate();
		stateTip.setShift(new Point(10, 10));

		NotSendToTmToolTip notSendToTMToolTip = new NotSendToTmToolTip(table);
		notSendToTMToolTip.setPopupDelay(10);
		notSendToTMToolTip.activate();
		notSendToTMToolTip.setShift(new Point(10, 10));

		HasQustionToolTip hasqustionTooltip = new HasQustionToolTip(table);
		hasqustionTooltip.setPopupDelay(10);
		hasqustionTooltip.activate();
		hasqustionTooltip.setShift(new Point(10, 10));
		// 在状态栏上显示当前文本段的信息。
		updateStatusLine();
	}

	/**
	 * 鼠标放在批注图片上时显示批注的 Tooltip
	 * @author peason
	 * @version
	 * @since JDK1.6
	 */
	class NoteToolTip extends DefaultToolTip {

		private Cursor clickCusor;

		public NoteToolTip(NatTable table) {
			super(table, ToolTip.RECREATE, true);
		}

		protected Object getToolTipArea(Event event) {
			int col = table.getColumnPositionByX(event.x);
			int row = table.getRowPositionByY(event.y);

			return new Point(col, row);
		}

		protected String getText(Event event) {
			Image image = XliffEditorGUIHelper.getImage(ImageName.HAS_NOTE);
			int columnPosition = table.getColumnPositionByX(event.x);
			int rowPosition = table.getRowPositionByY(event.y);
			LayerCell cell = table.getCellByPosition(columnPosition, rowPosition);
			Rectangle imageBounds = image.getBounds();
			Rectangle cellBounds = cell.getBounds();
			int x = cellBounds.x + imageBounds.width * 3 + 20;
			int y = cellBounds.y
					+ CellStyleUtil
							.getVerticalAlignmentPadding(CellStyleUtil.getCellStyle(cell, table.getConfigRegistry()),
									cellBounds, imageBounds.height);
			String text = null;
			XLIFFEditorImplWithNatTable xliffEditor = XLIFFEditorImplWithNatTable.getCurrent();
			if (columnPosition == xliffEditor.getStatusColumnIndex() && event.x >= x
					&& event.x <= (x + imageBounds.width) && event.y >= y && event.y <= (y + imageBounds.height)) {
				Vector<NoteBean> noteBeans = null;
				try {
					int rowIndex = table.getRowIndexByPosition(rowPosition);
					if (!isHorizontalLayout) {
						rowIndex = rowIndex / VerticalNatTableConfig.ROW_SPAN;
					}
					String rowId = xliffEditor.getXLFHandler().getRowId(rowIndex);
					noteBeans = xliffEditor.getXLFHandler().getNotes(rowId);
				} catch (NavException e) {
					LOGGER.error("", e);
					e.printStackTrace();
				} catch (XPathParseException e) {
					LOGGER.error("", e);
					e.printStackTrace();
				} catch (XPathEvalException e) {
					LOGGER.error("", e);
					e.printStackTrace();
				}
				if (noteBeans != null && noteBeans.size() > 0) {
					StringBuffer sb = new StringBuffer();
					for (int i = 0; i < noteBeans.size(); i++) {
						NoteBean bean = noteBeans.get(i);
						String strNote = bean.getNoteText();
						if (noteBeans.size() > 1) {
							sb.append((i + 1) + ". ");
						}
						if (strNote != null) {
							strNote = TextUtil.resetSpecialString(strNote);
							if (strNote.indexOf(":") != -1) {
								sb.append(strNote.substring(strNote.indexOf(":") + 1));
							} else {
								sb.append(strNote);
							}
							sb.append("\n");
						}
					}
					text = sb.toString();
				}
			}
			return text;
		}

		@Override
		protected boolean shouldCreateToolTip(Event event) {
			boolean flag = super.shouldCreateToolTip(event);
			if (!flag) {
				return flag;
			}
			Image image = XliffEditorGUIHelper.getImage(ImageName.HAS_NOTE);
			int columnPosition = table.getColumnPositionByX(event.x);
			int rowPosition = table.getRowPositionByY(event.y);
			LayerCell cell = table.getCellByPosition(columnPosition, rowPosition);
			Rectangle imageBounds = image.getBounds();
			if (cell == null) {
				return false;
			}
			Rectangle cellBounds = cell.getBounds();
			int x = cellBounds.x + imageBounds.width * 3 + 20;
			int y = cellBounds.y
					+ CellStyleUtil
							.getVerticalAlignmentPadding(CellStyleUtil.getCellStyle(cell, table.getConfigRegistry()),
									cellBounds, imageBounds.height);
			XLIFFEditorImplWithNatTable xliffEditor = XLIFFEditorImplWithNatTable.getCurrent();
			if (xliffEditor == null) {
				return false;
			}
			if (columnPosition == xliffEditor.getStatusColumnIndex() && event.x >= x
					&& event.x <= (x + imageBounds.width) && event.y >= y && event.y <= (y + imageBounds.height)) {
				Vector<NoteBean> noteBeans = null;
				try {
					int rowIndex = table.getRowIndexByPosition(rowPosition);
					if (!isHorizontalLayout) {
						rowIndex = rowIndex / VerticalNatTableConfig.ROW_SPAN;
					}

					String rowId = xliffEditor.getXLFHandler().getRowId(rowIndex);
					noteBeans = xliffEditor.getXLFHandler().getNotes(rowId);
				} catch (NavException e) {
					LOGGER.error("", e);
					e.printStackTrace();
				} catch (XPathParseException e) {
					LOGGER.error("", e);
					e.printStackTrace();
				} catch (XPathEvalException e) {
					LOGGER.error("", e);
					e.printStackTrace();
				}
				if (noteBeans != null && noteBeans.size() > 0) {
					clickCusor = new Cursor(Display.getCurrent(), SWT.CURSOR_HAND);
					setDisplayCursor(clickCusor);
					return true;
				}
			}
			setDisplayCursor(null);
			return false;
		}

		/**
		 * (non-Javadoc)
		 * @see org.eclipse.jface.window.ToolTip#afterHideToolTip(org.eclipse.swt.widgets.Event)
		 */
		@Override
		protected void afterHideToolTip(Event event) {
			setDisplayCursor(null);
			if (null != clickCusor) {
				clickCusor.dispose();
				clickCusor = null;
			}
			super.afterHideToolTip(event);
		}

		private void setDisplayCursor(Cursor c) {
			Shell[] shells = Display.getCurrent().getShells();
			for (int i = 0; i < shells.length; i++) {
				shells[i].setCursor(c);
			}
		}
	}

	/**
	 * 鼠标放在翻译状态图片上时显示批注的 Tooltip
	 * @author yule
	 * @version
	 * @since JDK1.6
	 */
	class StateToolTip extends DefaultToolTip {

		public StateToolTip(NatTable table) {
			super(table, ToolTip.RECREATE, true);
		}

		protected Object getToolTipArea(Event event) {
			int col = table.getColumnPositionByX(event.x);
			int row = table.getRowPositionByY(event.y);
			return new Point(col, row);
		}

		protected String getText(Event event) {
			Image image = XliffEditorGUIHelper.getImage(ImageName.HAS_NOTE);
			int columnPosition = table.getColumnPositionByX(event.x);
			int rowPosition = table.getRowPositionByY(event.y);
			LayerCell cell = table.getCellByPosition(columnPosition, rowPosition);
			Rectangle imageBounds = image.getBounds();
			Rectangle cellBounds = cell.getBounds();
			int x = cellBounds.x;
			int y = cellBounds.y
					+ CellStyleUtil
							.getVerticalAlignmentPadding(CellStyleUtil.getCellStyle(cell, table.getConfigRegistry()),
									cellBounds, imageBounds.height);
			String text = null;
			XLIFFEditorImplWithNatTable xliffEditor = XLIFFEditorImplWithNatTable.getCurrent();
			if (columnPosition == xliffEditor.getStatusColumnIndex() && event.x >= x
					&& event.x <= (x + imageBounds.width) && event.y >= y && event.y <= (y + imageBounds.height)) {
				int rowIndex = table.getRowIndexByPosition(rowPosition);
				if (!isHorizontalLayout) {
					rowIndex = rowIndex / VerticalNatTableConfig.ROW_SPAN;
				}
				TransUnitBean tu = xliffEditor.getXLFHandler().getTransUnit(rowIndex);
				text = getStateText(tu);
			}
			return text;
		}

		@Override
		protected boolean shouldCreateToolTip(Event event) {
			boolean flag = super.shouldCreateToolTip(event);
			if (!flag) {
				return flag;
			}
			int columnPosition = table.getColumnPositionByX(event.x);
			int rowPosition = table.getRowPositionByY(event.y);
			LayerCell cell = table.getCellByPosition(columnPosition, rowPosition);
			Image image = XliffEditorGUIHelper.getImage(ImageName.EMPTY);
			Rectangle imageBounds = image.getBounds();
			if (cell == null) {
				return false;
			}
			Rectangle cellBounds = cell.getBounds();
			int x = cellBounds.x;
			int y = cellBounds.y
					+ CellStyleUtil
							.getVerticalAlignmentPadding(CellStyleUtil.getCellStyle(cell, table.getConfigRegistry()),
									cellBounds, imageBounds.height);
			XLIFFEditorImplWithNatTable xliffEditor = XLIFFEditorImplWithNatTable.getCurrent();
			if (xliffEditor == null) {
				return false;
			}
			if (columnPosition == xliffEditor.getStatusColumnIndex() && event.x >= x
					&& event.x <= (x + imageBounds.width) && event.y >= y && event.y <= (y + imageBounds.height)) {
				return true;
			} else {
				return false;
			}
		}

		public String getStateText(TransUnitBean tu) {
			String approved = null;
			String translate = null;
			String state = null;
			if (tu != null && tu.getTuProps() != null) {
				approved = tu.getTuProps().get("approved");
				translate = tu.getTuProps().get("translate");
				if (tu.getTgtProps() != null) {
					state = tu.getTgtProps().get("state");
				}
			}
			if (translate != null && "no".equals(translate)) { // 已锁定
				return Messages.getString("editor.XLIFFEditorImplWithNatTable.stateToolLocked");
			} else if (state != null && "signed-off".equals(state)) { // 已签发
				return Messages.getString("editor.XLIFFEditorImplWithNatTable.stateToolSignedoff");
			} else if (approved != null && "yes".equals(approved)) { // 已批准
				return Messages.getString("editor.XLIFFEditorImplWithNatTable.stateToolTipapproved");
			} else if (state != null && "translated".equals(state)) { // 已翻译
				return Messages.getString("editor.XLIFFEditorImplWithNatTable.stateToolTipTranslated");
			} else if (state != null && "new".equals(state)) { // 草稿
				return Messages.getString("editor.XLIFFEditorImplWithNatTable.stateToolTipDraft");
			} else {
				return Messages.getString("editor.XLIFFEditorImplWithNatTable.stateToolTipNew");
			}
		}

	}

	/**
	 * @author yule
	 * @version
	 * @since JDK1.6
	 */
	abstract class StatusToolTips extends DefaultToolTip {

		protected final int IMG_WIDTH = 16;

		protected final int MATCH_QUALITY = 20;

		public StatusToolTips(NatTable table) {// the constructor
			super(table, ToolTip.RECREATE, true);
		}

		protected Object getToolTipArea(Event event) { // tooltips Area
			int col = table.getColumnPositionByX(event.x);
			int row = table.getRowPositionByY(event.y);
			return new Point(col, row);
		}

		protected String getText(Event event) { // ToolTips Text
			Image image = XliffEditorGUIHelper.getImage(ImageName.HAS_NOTE);
			int columnPosition = table.getColumnPositionByX(event.x);
			int rowPosition = table.getRowPositionByY(event.y);
			LayerCell cell = table.getCellByPosition(columnPosition, rowPosition);
			Rectangle imageBounds = image.getBounds();
			Rectangle cellBounds = cell.getBounds();
			int x = cellBounds.x + getStatusStartX();
			int y = cellBounds.y
					+ CellStyleUtil
							.getVerticalAlignmentPadding(CellStyleUtil.getCellStyle(cell, table.getConfigRegistry()),
									cellBounds, imageBounds.height);
			String text = null;
			XLIFFEditorImplWithNatTable xliffEditor = XLIFFEditorImplWithNatTable.getCurrent();
			if (columnPosition == xliffEditor.getStatusColumnIndex() && event.x >= x
					&& event.x <= (x + imageBounds.width) && event.y >= y && event.y <= (y + imageBounds.height)) {
				int rowIndex = table.getRowIndexByPosition(rowPosition);
				if (!isHorizontalLayout) {
					rowIndex = rowIndex / VerticalNatTableConfig.ROW_SPAN;
				}
				TransUnitBean tu = xliffEditor.getXLFHandler().getTransUnit(rowIndex);
				text = getStateText(tu);
			}
			return text;
		}

		@Override
		protected boolean shouldCreateToolTip(Event event) {
			boolean flag = super.shouldCreateToolTip(event);
			if (!flag) {
				return flag;
			}
			int columnPosition = table.getColumnPositionByX(event.x);
			int rowPosition = table.getRowPositionByY(event.y);
			LayerCell cell = table.getCellByPosition(columnPosition, rowPosition);
			Image image = XliffEditorGUIHelper.getImage(ImageName.EMPTY);
			Rectangle imageBounds = image.getBounds();
			if (cell == null) {
				return false;
			}
			Rectangle cellBounds = cell.getBounds();
			int x = cellBounds.x + getStatusStartX();
			int y = cellBounds.y
					+ CellStyleUtil
							.getVerticalAlignmentPadding(CellStyleUtil.getCellStyle(cell, table.getConfigRegistry()),
									cellBounds, imageBounds.height);
			XLIFFEditorImplWithNatTable xliffEditor = XLIFFEditorImplWithNatTable.getCurrent();
			if (xliffEditor == null) {
				return false;
			}
			if (columnPosition == xliffEditor.getStatusColumnIndex() && event.x >= x
					&& event.x <= (x + imageBounds.width) && event.y >= y && event.y <= (y + imageBounds.height)) {
				int rowIndex = table.getRowIndexByPosition(rowPosition);
				if (!isHorizontalLayout) {
					rowIndex = rowIndex / VerticalNatTableConfig.ROW_SPAN;
				}
				TransUnitBean tu = xliffEditor.getXLFHandler().getTransUnit(rowIndex);
				return null != getStateText(tu);

			} else {
				return false;
			}
		}

		protected abstract int getStatusStartX();

		protected abstract String getStateText(TransUnitBean tu);

	}

	/**
	 * @author yule
	 * @version
	 * @since JDK1.6
	 */
	class NotSendToTmToolTip extends StatusToolTips {

		/**
		 * @param table
		 */
		public NotSendToTmToolTip(NatTable table) {
			super(table);
			// TODO Auto-generated constructor stub
		}

		/**
		 * (non-Javadoc)
		 * @see net.heartsome.cat.ts.ui.xliffeditor.nattable.editor.XLIFFEditorImplWithNatTable.StatusToolTips#getStatusStartX()
		 */
		@Override
		protected int getStatusStartX() {
			// TODO Auto-generated method stub
			return IMG_WIDTH + MATCH_QUALITY;
		}

		/**
		 * (non-Javadoc)
		 * @see net.heartsome.cat.ts.ui.xliffeditor.nattable.editor.XLIFFEditorImplWithNatTable.StatusToolTips#getStateText(net.heartsome.cat.ts.core.bean.TransUnitBean)
		 */
		@Override
		protected String getStateText(TransUnitBean tu) {
			// TODO Auto-generated method stub
			if (null == tu) {
				return null;
			}
			String sendToTm = tu.getTuProps().get("hs:send-to-tm");
			if (sendToTm != null && ("no").equals(sendToTm)) {
				return Messages.getString("editor.XLIFFEditorImplWithNatTable.notSendToTm");
			}
			return null;
		}

	}

	/**
	 * @author yule
	 * @version
	 * @since JDK1.6
	 */
	class HasQustionToolTip extends StatusToolTips {

		/**
		 * @param table
		 */
		public HasQustionToolTip(NatTable table) {
			super(table);
			// TODO Auto-generated constructor stub
		}

		/**
		 * (non-Javadoc)
		 * @see net.heartsome.cat.ts.ui.xliffeditor.nattable.editor.XLIFFEditorImplWithNatTable.StatusToolTips#getStatusStartX()
		 */
		@Override
		protected int getStatusStartX() {
			// TODO Auto-generated method stub
			return 2 * IMG_WIDTH + MATCH_QUALITY;
		}

		/**
		 * (non-Javadoc)
		 * @see net.heartsome.cat.ts.ui.xliffeditor.nattable.editor.XLIFFEditorImplWithNatTable.StatusToolTips#getStateText(net.heartsome.cat.ts.core.bean.TransUnitBean)
		 */
		@Override
		protected String getStateText(TransUnitBean tu) {
			// TODO Auto-generated method stub
			if (null == tu) {
				return null;
			}
			String needReview = tu.getTuProps().get("hs:needs-review");
			if (needReview != null && "yes".equals(needReview)) {
				return Messages.getString("editor.XLIFFEditorImplWithNatTable.hasQustion");
			}
			return null;
		}

	}

	private Listener resizeListenerWithColumnResize = new Listener() {
		public void handleEvent(Event event) {
			HsMultiActiveCellEditor.commit(false);
			NatTable table = (NatTable) event.widget;
			if (table == null || table.isDisposed()) {
				return;
			}
			table.doCommand(new ClientAreaResizeCommand(table));
			int clientAreaWidth = table.getClientArea().width;
			if (clientAreaWidth <= 0) {
				return;
			}
			int count = propertyNames.length; // 编辑器中的列数
			if (count <= 0) {
				return;
			}

			Collection<Integer> hiddenColumnIndexes = bodyLayer.getColumnHideShowLayer().getHiddenColumnIndexes();

			int shownColumnCount = count - hiddenColumnIndexes.size();
			int[] columnPositions = new int[shownColumnCount]; // 需要修改的列的列号数组
			int[] columnWidths = new int[shownColumnCount]; // 需要修改的列对应的宽度
			double shownPercentage = 1; // 显示的百分比，原始为1（即 100%，表示所有列显示，后面要减去被隐藏的列所占的百分比）

			for (int i = 0, j = 0; i < count; i++) {
				double width = propertyToColWidths.get(propertyNames[i]);
				if (!hiddenColumnIndexes.contains(i)) { // 如果没有被隐藏
					columnPositions[j] = i;
					if (width > 1) { // 如果指定的是像素值
						columnWidths[j] = (int) width;
						clientAreaWidth -= (int) width; // 从总宽度中除去明确指定像素的列宽
					}
					j++;
				}
				if (hiddenColumnIndexes.contains(i) && width <= 1) {
					shownPercentage -= width;
				}
			}
			for (int i = 0, j = 0; i < count; i++) {
				double width = propertyToColWidths.get(propertyNames[i]);
				if (!hiddenColumnIndexes.contains(i)) { // 如果没有被隐藏
					if (width <= 1) { // 如果指定的是百分比
						columnWidths[j] = (int) (clientAreaWidth * (width / shownPercentage)); // 按指定百分比计算像素
					}
					j++;
				}
			}
			paintListenerWithAutoRowSize.resetArea();
			table.doCommand(new MultiColumnResizeCommand(bodyLayer.getBodyDataLayer(), columnPositions, columnWidths));

			// if (HsMultiActiveCellEditor.getSourceEditor() == null) {
			// HsMultiCellEditorControl.activeSourceAndTargetCell(XLIFFEditorImplWithNatTable.this);
			// }
			HsMultiActiveCellEditor.recalculateCellsBounds();
		}
	};

	/**
	 * 重绘监听，处理 NatTable 自适应大小.
	 */
	class PaintListenerWithAutoRowSize implements PaintListener {

		// private Rectangle area = new Rectangle(-1, -1, -1, -1);

		private int columnCount = 0;
		private List<Integer> hasComputedRow = new ArrayList<Integer>();
		private int currentRowPosition = -1;

		public void paintControl(PaintEvent e) {
			ViewportLayer viewportLayer = bodyLayer.getViewportLayer();
			int rowPosition = viewportLayer.getOriginRowPosition() + 1; // 起始行
			if (currentRowPosition == -1 || currentRowPosition != rowPosition) {
				currentRowPosition = rowPosition;
			}
			int rowCount = viewportLayer.getRowCount(); // 总行数
			List<Integer> rowPositions = new ArrayList<Integer>();
			for (int i = 0; i < rowCount; i++) {
				int rowp = i + rowPosition;
				if (!hasComputedRow.contains(rowp)) {
					rowPositions.add(rowp);
					hasComputedRow.add(rowp);
				}
			}
			if (rowPositions.size() != 0) {
				int[] temp = new int[rowPositions.size()];
				for (int i = 0; i < temp.length; i++) {
					temp[i] = rowPositions.get(i);
				}
				table.doCommand(new AutoResizeCurrentRowsCommand(table, temp, table.getConfigRegistry()));
				HsMultiActiveCellEditor.recalculateCellsBounds();
				// HsMultiActiveCellEditor.synchronizeRowHeight();
			}

			// int width = parent.getClientArea().width;
			// if (!new Rectangle(rowPosition, rowCount, width, 0).equals(area)) {
			// area = new Rectangle(rowPosition, rowCount, width, 0);
			// int[] rowPositions = new int[rowCount];
			// for (int j = 0; j < rowPositions.length; j++) {
			// rowPositions[j] = j + rowPosition;
			// }
			// table.doCommand(new AutoResizeCurrentRowsCommand(table, rowPositions, table.getConfigRegistry(), e.gc));
			// }

			if (columnCount != table.getColumnCount()) {
				columnCount = table.getColumnCount();
				table.notifyListeners(SWT.Resize, null);
			}
			table.getLayerPainter().paintLayer(table, e.gc, 0, 0, new Rectangle(e.x, e.y, e.width, e.height),
					table.getConfigRegistry());
		}

		public void resetArea() {
			// area = new Rectangle(-1, -1, -1, -1);
			hasComputedRow.clear();
		}

		public void resetColumnCount() {
			columnCount = 0;
		}
	}

	/**
	 * 重绘监听，处理 NatTable 自适应大小.
	 */
	private PaintListenerWithAutoRowSize paintListenerWithAutoRowSize = new PaintListenerWithAutoRowSize();

	/**
	 * 自动调整 NatTable 行高;
	 * @see net.heartsome.cat.ts.ui.editors.IXliffEditor#autoResize()
	 */
	public void autoResize() {
		if (table != null) {
			paintListenerWithAutoRowSize.resetArea();
			// paintListenerWithAutoRowSize.resetColumnCount();
			if (!table.isDisposed()) {
				table.redraw();
			}
		}
	}

	/**
	 * 自动调整 NatTable 大小 ，只调整行高。不调整列宽 robert 2012-11-21
	 */
	public void autoResizeNotColumn() {
		if (table != null) {
			paintListenerWithAutoRowSize.resetArea();
			table.redraw();
		}
	}

	/**
	 * 配置标签列的显示效果
	 * @param configRegistry
	 *            配置注册表
	 */
	private void addFlagLableToColumn(IConfigRegistry configRegistry) {
		// Edit by Leakey 实现状态图片的动态显示
		StatusPainter painter = new StatusPainter(bodyDataProvider);
		// CellPainterDecorator flagPainter = new CellPainterDecorator(new BackgroundPainter(), CellEdgeEnum.RIGHT,
		// painter);

		configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_PAINTER, painter, DisplayMode.NORMAL,
				FLAG_CELL_LABEL);

		// Set the color of the cell. This is picked up by the button painter to style the button
		Style style = new Style();
		FontData fd = GUIHelper.DEFAULT_FONT.getFontData()[0];
		fd.setStyle(SWT.BOLD);
		style.setAttributeValue(CellStyleAttributes.FONT, GUIHelper.getFont(fd));

		configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, style, DisplayMode.NORMAL,
				FLAG_CELL_LABEL);
		configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, style, DisplayMode.SELECT,
				FLAG_CELL_LABEL);
	}

	/**
	 * 配置行号列的显示效果
	 * @param configRegistry
	 *            配置注册表
	 */
	private void addLineNumberToColumn(IConfigRegistry configRegistry) {
		configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_PAINTER, new LineNumberPainter(),
				DisplayMode.NORMAL, "LINENUMBER_CELL_LABEL");
		Style style = new Style();
		style.setAttributeValue(CellStyleAttributes.HORIZONTAL_ALIGNMENT, HorizontalAlignmentEnum.CENTER);
		style.setAttributeValue(CellStyleAttributes.FONT, GUIHelper.DEFAULT_FONT);

		configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, style, DisplayMode.NORMAL,
				"LINENUMBER_CELL_LABEL");
		configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, style, DisplayMode.SELECT,
				"LINENUMBER_CELL_LABEL");
	}

	/**
	 * 定义 Body Layer Stack 中包含的 layers
	 * @author cheney
	 * @since JDK1.5
	 */
	public class BodyLayerStack extends AbstractLayerTransform {
		private DataLayer bodyDataLayer;

		private SelectionLayer selectionLayer;

		private ColumnHideShowLayer columnHideShowLayer;

		private ViewportLayer viewportLayer;

		public BodyLayerStack(IDataProvider dataProvider, boolean isHorizontalLayout) {
			if (isHorizontalLayout) {
				bodyDataLayer = new DataLayer(dataProvider, 244, 32);
			} else {
				// 垂直布局时，期望数据提供者为 ISpanningDataProvider，否则抛出 UnexpectedTypeExcetion。
				if (dataProvider instanceof ISpanningDataProvider) {
					bodyDataLayer = new SpanningDataLayer((ISpanningDataProvider) dataProvider, 244, 32);
				} else {
					throw new UnexpectedTypeExcetion(MessageFormat.format(Messages
							.getString("editor.XLIFFEditorImplWithNatTable.msg6"), dataProvider.getClass().toString()));
				}
			}
			// 取消调整列的功能，因为此功能会引发一些问题，如跳转，设置分割点等
			// ColumnReorderLayer columnReorderLayer = new ColumnReorderLayer(bodyDataLayer);
			columnHideShowLayer = new ColumnHideShowLayer(bodyDataLayer);
			selectionLayer = new SelectionLayer(columnHideShowLayer, false);

			if (isHorizontalLayout) { // 两种布局采用不同的 ViewportLayer
				viewportLayer = new HorizontalViewportLayer(selectionLayer);
			} else {
				viewportLayer = new VerticalViewportLayer(selectionLayer);
			}
			setUnderlyingLayer(viewportLayer);

			setupSelectionLayer(); // 设置SelectionLayer
		}

		/**
		 * 设置SelectionLayer ;
		 */
		private void setupSelectionLayer() {
			IRowIdAccessor rowIdAccessor = new IRowIdAccessor() {

				public Serializable getRowIdByPosition(int rowPosition) {
					int rowIndex = selectionLayer.getRowIndexByPosition(rowPosition);
					return handler.getRowId(rowIndex);
				}

				public Set<? extends Serializable> getRowIdsByPositionRange(int rowPosition, int length) {
					int[] rowIndexs = new int[length];
					for (int i = 0; i < rowIndexs.length; i++) {
						rowIndexs[i] = selectionLayer.getRowIndexByPosition(rowPosition + i);
					}
					return handler.getRowIds(rowIndexs);
				}

				public ArrayList<? extends Serializable> getRowIds() {
					return handler.getRowIds();
				}
			};
			ISelectionModel rowSelectionModel;
			if (isHorizontalLayout) {
				rowSelectionModel = new HorizontalRowSelectionModel(selectionLayer);
			} else {
				rowSelectionModel = new VerticalRowSelectionModel<TransUnitBean>(selectionLayer, rowIdAccessor);
			}

			// Preserve selection on updates and sort
			selectionLayer.setSelectionModel(rowSelectionModel);
			selectionLayer.addConfiguration(new XLIFFEditorSelectionLayerConfiguration());

			// 移除点击列头触发全选所有行的 Handler
			selectionLayer.unregisterCommandHandler(SelectColumnCommand.class);

			// Provides rows where any cell in the row is selected
			ISelectionProvider selectionProvider = new RowSelectionProvider(selectionLayer, true/* 只在整行选中时触发 */);

			// NatTable 选中行改变时触发：
			selectionProvider.addSelectionChangedListener(new ISelectionChangedListener() {
				public void selectionChanged(SelectionChangedEvent event) {
					updateStatusLine(); // 更新状态栏显示的文本段信息。

					// 设置添加文本段到记忆库的可用状态
					// NattableUtil.refreshCommand(AddSegmentToTMPropertyTester.PROPERTY_NAMESPACE,
					// AddSegmentToTMPropertyTester.PROPERTY_ENABLED);
					// NattableUtil.refreshCommand(SignOffPropertyTester.PROPERTY_NAMESPACE,
					// SignOffPropertyTester.PROPERTY_ENABLED);
					// NattableUtil.refreshCommand(UnTranslatedPropertyTester.PROPERTY_NAMESPACE,
					// UnTranslatedPropertyTester.PROPERTY_ENABLED);

				}
			});

			getSite().setSelectionProvider(selectionProvider);
		}

		/**
		 * 获得 body layer stack 中的 SelectionLayer
		 * @return ;
		 */
		public SelectionLayer getSelectionLayer() {
			return selectionLayer;
		}

		/**
		 * 获得 body layer stack 中的 BodyDataLayer
		 * @return ;
		 */
		public DataLayer getBodyDataLayer() {
			return bodyDataLayer;
		}

		/**
		 * 获得 body layer stack 中的 ColumnHideShowLayer
		 * @return ;
		 */
		public ColumnHideShowLayer getColumnHideShowLayer() {
			return columnHideShowLayer;
		}

		/**
		 * 获得 body layer stack 中的 ViewportLayer
		 * @return ;
		 */
		public ViewportLayer getViewportLayer() {
			return viewportLayer;
		}

	}

	/**
	 * 定义 Column Header Layer Stack 所包含的 layers
	 * @author cheney
	 * @since JDK1.5
	 */
	public class ColumnHeaderLayerStack extends AbstractLayerTransform {

		public ColumnHeaderLayerStack(IDataProvider dataProvider) {
			DataLayer dataLayer = new DataLayer(dataProvider, 244, 25);

			/**
			 * Edit By Leakey 不使用默认配置
			 */
			ColumnHeaderLayer colHeaderLayer = new ColumnHeaderLayer(dataLayer, bodyLayer,
					bodyLayer.getSelectionLayer(), false);

			/**
			 * Add By Leakey 添加默认列头的样式（前景色、背景色等）
			 */
			colHeaderLayer.addConfiguration(new DefaultColumnHeaderStyleConfiguration() {
				public void configureRegistry(IConfigRegistry configRegistry) {
					configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_PAINTER, cellPainter,
							DisplayMode.NORMAL, GridRegion.COLUMN_HEADER);
					configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_PAINTER, cellPainter,
							DisplayMode.NORMAL, GridRegion.CORNER);

					// Normal
					Style cellStyle = new Style();
					cellStyle
							.setAttributeValue(CellStyleAttributes.BACKGROUND_COLOR, GUIHelper.COLOR_WIDGET_BACKGROUND);
					cellStyle.setAttributeValue(CellStyleAttributes.FOREGROUND_COLOR, GUIHelper.COLOR_BLACK);
					cellStyle.setAttributeValue(CellStyleAttributes.HORIZONTAL_ALIGNMENT, hAlign);
					cellStyle.setAttributeValue(CellStyleAttributes.VERTICAL_ALIGNMENT, vAlign);
					cellStyle.setAttributeValue(CellStyleAttributes.BORDER_STYLE, borderStyle);
					cellStyle.setAttributeValue(CellStyleAttributes.FONT, GUIHelper.DEFAULT_FONT);

					configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, cellStyle,
							DisplayMode.NORMAL, GridRegion.COLUMN_HEADER);
					configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, cellStyle,
							DisplayMode.NORMAL, GridRegion.CORNER);
					configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, cellStyle,
							DisplayMode.SELECT, GridRegion.COLUMN_HEADER);
					configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, cellStyle,
							DisplayMode.SELECT, GridRegion.CORNER);
					configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, cellStyle,
							DisplayMode.SELECT, GridRegion.ROW_HEADER);
				}
			});

			SortHeaderLayer<TransUnitBean> sortHeaderLayer = new SortHeaderLayer<TransUnitBean>(colHeaderLayer,
					new NatTableSortModel(handler), false);

			setUnderlyingLayer(sortHeaderLayer);
		}
	}

	/**
	 * Internal property change listener for handling workbench font changes.
	 */
	class FontPropertyChangeListener implements IPropertyChangeListener {
		/*
		 * @see IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
		 */
		public void propertyChange(PropertyChangeEvent event) {
			if (table == null || table.isDisposed()) {
				return;
			}
			String property = event.getProperty();

			if (net.heartsome.cat.ts.ui.Constants.XLIFF_EDITOR_TEXT_FONT.equals(property)) {
				Font font = JFaceResources.getFont(net.heartsome.cat.ts.ui.Constants.XLIFF_EDITOR_TEXT_FONT);
				ICellPainter cellPainter = table.getConfigRegistry().getConfigAttribute(
						CellConfigAttributes.CELL_PAINTER, DisplayMode.NORMAL, SOURCE_EDIT_CELL_LABEL);

				if (cellPainter instanceof TextPainterWithPadding) {
					TextPainterWithPadding textPainter = (TextPainterWithPadding) cellPainter;
					if (textPainter.getFont() == null || !textPainter.getFont().equals(font)) {
						HsMultiActiveCellEditor.commit(true);
						textPainter.setFont(font);
						autoResize();
						// HsMultiCellEditorControl.activeSourceAndTargetCell(XLIFFEditorImplWithNatTable.this);
					}
				}
			}
		}
	}

	/**
	 * 支持进度条显示的打开文件
	 * @author leakey
	 * @version
	 * @since JDK1.5
	 */
	class OpenFile implements IRunnableWithProgress {

		public OpenFile(List<File> files) {
			this.files = files;
		}

		private List<File> files;

		private Map<String, Object> openFileResult;

		public Map<String, Object> getOpenFileResult() {
			return openFileResult;
		}

		public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
			openFileResult = handler.openFiles(files, monitor);
		}
	}

	/**
	 * 释放编辑器，同时释放其他相关资源。
	 * @see org.eclipse.ui.part.WorkbenchPart#dispose()
	 */
	public void dispose() {
		// 当该编辑器被释放资源时，检查该编辑器是否被保存，并且是否是同时打开多个文件，若成立，则删除合并打开所产生的临时文件--robert 2012-03-30
		if (!isStore && isMultiFile()) {
			try {
				IEditorInput input = getEditorInput();
				IProject multiProject = ResourceUtil.getResource(input).getProject();
				ResourceUtil.getResource(input).delete(true, null);
				multiProject.refreshLocal(IResource.DEPTH_INFINITE, null);

				CommonFunction.refreshHistoryWhenDelete(input);
			} catch (CoreException e) {
				LOGGER.error("", e);
				e.printStackTrace();
			}
		}

		if (titleImage != null && !titleImage.isDisposed()) {
			titleImage.dispose();
			titleImage = null;
		}
		if (table != null && !table.isDisposed()) {
			table.dispose();
			table = null;
		}

		if (statusLineImage != null && !statusLineImage.isDisposed()) {
			statusLineImage.dispose();
			statusLineImage = null;
		}
		handler = null;
		JFaceResources.getFontRegistry().removeListener(fFontPropertyChangeListener);
		NattableUtil.getInstance(this).releaseResource();
		super.dispose();
		System.gc();
	}

	/**
	 * 得到当前活动的 XLIFF 编辑器实例
	 * @return ;
	 */
	public static XLIFFEditorImplWithNatTable getCurrent() {
		try {
			IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
			if (window != null) {
				IWorkbenchPage page = window.getActivePage();
				if (page != null) {
					IEditorPart editor = page.getActiveEditor();
					if (editor != null && editor instanceof XLIFFEditorImplWithNatTable) {
						return (XLIFFEditorImplWithNatTable) editor;
					}
				}
			}
		} catch (NullPointerException e) {
			LOGGER.error("", e);
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * (non-Javadoc)
	 * @see net.heartsome.cat.ts.ui.editors.IXliffEditor#updateCell(int, int, java.lang.String)
	 */
	public void updateCell(int row, int columnIndex, String newValue, String matchType, String quality)
			throws ExecutionException {
		StyledTextCellEditor editor = HsMultiActiveCellEditor.getTargetStyledEditor();
		if (editor != null && editor.getRowIndex() == (isHorizontalLayout ? row : row * 2 + 1)
				&& editor.getColumnIndex() == columnIndex) {
			editor.viewer.getTextWidget().forceFocus();
		}
		ArrayList<String> rowIds = new ArrayList<String>(handler.getRowIds(new int[] { row }));
		updateSegments(rowIds, columnIndex, newValue, false, matchType, quality);
	}

	/**
	 * (non-Javadoc)
	 * @see net.heartsome.cat.ts.ui.editors.IXliffEditor#updateCells(int[], int, java.lang.String)
	 */
	public void updateCells(int[] rows, int columnIndex, String newValue) throws ExecutionException {
		ArrayList<String> rowIds = new ArrayList<String>(handler.getRowIds(rows));
		updateSegments(rowIds, columnIndex, newValue, false, null, null);
	}

	/**
	 * 修改文本段。（改成不同的值）
	 * @param map
	 *            key：rowId；value：新值。
	 * @param column
	 *            列索引。
	 * @throws ExecutionException
	 *             ;
	 */
	public void updateSegments(Map<String, String> map, int columnIndex, String matchType, String quality)
			throws ExecutionException {
		updateSegments(map, columnIndex, false, matchType, quality);
	}

	/**
	 * 修改文本段。（改成相同的值）
	 * @param rowIds
	 *            行唯一标识的集合
	 * @param columnIndex
	 *            列索引
	 * @param newValue
	 *            新值
	 * @param approved
	 *            是否改为已批准
	 * @throws ExecutionException
	 *             ;
	 */
	public void updateSegments(List<String> rowIds, int columnIndex, String newValue, boolean approved,
			String matchType, String quality) throws ExecutionException {
		updateCellEditor(columnIndex, newValue, matchType, quality);
		HISTORY.execute(new UpdateSegmentsOperation(this, handler, rowIds, columnIndex, newValue, approved, matchType,
				quality), null, null);
		NattableUtil.refreshCommand(AddSegmentToTMPropertyTester.PROPERTY_NAMESPACE,
				AddSegmentToTMPropertyTester.PROPERTY_ENABLED);
		NattableUtil.refreshCommand(SignOffPropertyTester.PROPERTY_NAMESPACE, SignOffPropertyTester.PROPERTY_ENABLED);
		NattableUtil.refreshCommand(UnTranslatedPropertyTester.PROPERTY_NAMESPACE,
				UnTranslatedPropertyTester.PROPERTY_ENABLED);
	}

	/**
	 * 修改文本段。（改成不同的值）
	 * @param map
	 *            key：rowId；value：新值。
	 * @param columnIndex
	 *            列索引。
	 * @param approved
	 *            是否改为已批准
	 * @throws ExecutionException
	 *             ;
	 */
	public void updateSegments(Map<String, String> map, int columnIndex, boolean approved, String matchType,
			String quality) throws ExecutionException {
		int rowIndex = HsMultiActiveCellEditor.sourceRowIndex;
		if (rowIndex != -1) {
			if (!isHorizontalLayout) {
				rowIndex = VerticalNatTableConfig.getRealRowIndex(rowIndex);
			}
			String rowId = handler.getRowId(rowIndex);
			if (!updateCellEditor(columnIndex, map.get(rowId), matchType, quality)) {
				return;
			}
		}
		HISTORY.execute(new UpdateSegmentsOperation(this, handler, map, columnIndex, approved, matchType, quality),
				null, null);
		NattableUtil.refreshCommand(AddSegmentToTMPropertyTester.PROPERTY_NAMESPACE,
				AddSegmentToTMPropertyTester.PROPERTY_ENABLED);
		NattableUtil.refreshCommand(SignOffPropertyTester.PROPERTY_NAMESPACE, SignOffPropertyTester.PROPERTY_ENABLED);
		NattableUtil.refreshCommand(UnTranslatedPropertyTester.PROPERTY_NAMESPACE,
				UnTranslatedPropertyTester.PROPERTY_ENABLED);
	}

	/**
	 * 修改单元格编辑器中的文本。
	 * @param columnIndex
	 *            列索引
	 * @param newValue
	 *            新值
	 * @return ;
	 */
	private boolean updateCellEditor(int columnIndex, String newValue, String matchType, String quality) {
		StyledTextCellEditor cellEditor = HsMultiActiveCellEditor.getFocusCellEditor();
		if (cellEditor == null) {
			return false;
		}
		// 如果当前的文本是锁定状态，不改变值
		String tgt = handler.getCaseTgtContent(handler.getRowId(cellEditor.getRowIndex()));
		if (null != tgt) {
			if (tgt.equals("no")) {
				return true;
			}
		}
		int rowIndex = cellEditor.getRowIndex();
		int activeCellEditorColumnIndex = cellEditor.getColumnIndex();

		if (rowIndex == -1) {
			return false;
		}
		if (activeCellEditorColumnIndex == -1) {
			return false;
		}
		if (!isHorizontalLayout) {
			// 垂直布局
			int[] selecteds = getSelectedRows();
			int seled = getSelectedRows()[selecteds.length - 1];
			if ((seled * 2) + 1 != rowIndex) {
				return false;
			}
		} else {
			if (activeCellEditorColumnIndex != columnIndex) {
				return false;
			}
		}
		// 解决“修改源或者目标文本时，总是优先修改处于编辑模式的列”的问题。

		/** burke 修改复制来源到目标中，当光标在src单元格中，点击复制来源到目标应该不起作用 修改代码 添加合并判断条件 !isHorizontalLayout */
		if (activeCellEditorColumnIndex == -1 || (activeCellEditorColumnIndex != columnIndex && !isHorizontalLayout)) {
			return false;
		}

		UpdateDataBean bean = new UpdateDataBean(newValue, matchType, quality);
		cellEditor.setCanonicalValue(bean);
		return true;
	}

	/**
	 * (non-Javadoc)
	 * @see net.heartsome.cat.ts.ui.editors.IXliffEditor#getSrcColumnIndex()
	 */
	public int getSrcColumnIndex() {
		return isHorizontalLayout ? 1 : 2;
	}

	public int getStatusColumnIndex() {
		return isHorizontalLayout ? 2 : 1;
	}

	/**
	 * (non-Javadoc)
	 * @see net.heartsome.cat.ts.ui.editors.IXliffEditor#getTgtColumnIndex()
	 */
	public int getTgtColumnIndex() {
		return isHorizontalLayout ? 3 : 2;
	}

	public List<String> getSplitXliffPoints() {
		return splitXliffPoints;
	}

	/**
	 * 点击品质检查结果视图的列表项时，重置nattable的排序 robert 2011-11-24
	 */
	public void resetOrder() {
		if (cmbFilter.getSelectionIndex() != 0) {
			cmbFilter.select(0);
			boolean isUpdated = handler.doFilter(cmbFilter.getText(), langFilterCondition);
			if (isUpdated) {
				autoResizeNotColumn(); // 自动调整 NatTable 大小 ;
				updateStatusLine(); // 更新状态栏
				refresh();
			}
		} else {
			// handler.doFilter(cmbFilter.getText(), langFilterCondition);
			handler.resetRowIdsToUnsorted(); // 重置布局
			autoResizeNotColumn();
			updateStatusLine();
		}
	}

	public void reloadData() {
		boolean isUpdated = handler.doFilter(cmbFilter.getText(), langFilterCondition);
		if (isUpdated) {
			autoResizeNotColumn(); // 自动调整 NatTable 大小 ;
			updateStatusLine(); // 更新状态栏
			refresh();
		}

	}

	public void insertCell(int rowIndex, int columnIndex, String insertText) throws ExecutionException {
		StyledTextCellEditor editor = HsMultiActiveCellEditor.getTargetStyledEditor();
		int editorRowIndex = editor.getRowIndex();
		if (!isHorizontalLayout) {
			editorRowIndex = editorRowIndex / 2;
		}
		if (editor != null && editorRowIndex == rowIndex && editor.getColumnIndex() == columnIndex) {
			editor.insertCanonicalValue(insertText);
			editor.viewer.getTextWidget().forceFocus();
		}
	}

	public void setStore(boolean isStore) {
		this.isStore = isStore;
	}

	public List<File> getMultiFileList() {
		return multiFileList;
	}

	public void jumpToRow(int position, boolean isMultiFiles) {
		// if (position == 0) {
		// ViewportLayer viewportLayer = bodyLayer.getViewportLayer();
		// HsMultiActiveCellEditor.commit(true);
		// viewportLayer.doCommand(new SelectCellCommand(bodyLayer.getSelectionLayer(), getTgtColumnIndex(), -1,
		// false, false));
		// HsMultiCellEditorControl.activeSourceAndTargetCell(XLIFFEditorImplWithNatTable.this);
		// }
		jumpToRow(position);
	}

	/**
	 * (non-Javadoc)
	 * @see net.heartsome.cat.ts.ui.editors.IXliffEditor#getSelectPureText()
	 */
	public String getSelectPureText() {
		StyledTextCellEditor cellEditor = HsMultiActiveCellEditor.getFocusCellEditor();
		String selectionText = null;
		if (cellEditor != null && !cellEditor.isClosed()) {
			StyledText styledText = cellEditor.getSegmentViewer().getTextWidget();
			Point p = styledText.getSelection();
			if (p != null) {
				if (p.x != p.y) {
					selectionText = cellEditor.getSelectedPureText();
				} else {
					selectionText = "";
				}
			}
		}

		if (selectionText != null) {
			// 将换行符替换为空
			selectionText = selectionText.replaceAll("\n", "");
		}
		return selectionText;
	}

	/**
	 * (non-Javadoc)
	 * @see net.heartsome.cat.ts.ui.editors.IXliffEditor#getSelectSrcOrTgtPureText(java.lang.StringBuffer,
	 *      java.lang.StringBuffer)
	 */
	public void getSelectSrcOrTgtPureText(StringBuffer src, StringBuffer tgt) {
		if (src == null || tgt == null) {
			return;
		}
		HsMultiCellEditor hsCellEditor = HsMultiActiveCellEditor.getSourceEditor();
		if (hsCellEditor != null && hsCellEditor.getCellEditor() != null) {
			StyledTextCellEditor cellEditor = hsCellEditor.getCellEditor();
			if (!cellEditor.isClosed()) {
				StyledText styledText = cellEditor.getSegmentViewer().getTextWidget();
				Point p = styledText.getSelection();
				if (p != null) {
					if (p.x != p.y) {
						// String selectionText = styledText.getSelectionText();
						String selectionText = cellEditor.getSelectedPureText();
						src.append(selectionText);
					}
				}
			}
		}

		hsCellEditor = HsMultiActiveCellEditor.getTargetEditor();
		if (hsCellEditor != null && hsCellEditor.getCellEditor() != null) {
			StyledTextCellEditor cellEditor = hsCellEditor.getCellEditor();
			if (!cellEditor.isClosed()) {
				StyledText styledText = cellEditor.getSegmentViewer().getTextWidget();
				Point p = styledText.getSelection();
				if (p != null) {
					if (p.x != p.y) {
						// String selectionText = styledText.getSelectionText();
						String selectionText = cellEditor.getSelectedPureText();
						tgt.append(selectionText);
					}
				}
			}
		}
	}

	public void redraw() {
		table.redraw();
	}

	public TransUnitBean getRowTransUnitBean(int rowIndex) {
		if (!isHorizontalLayout) {
			rowIndex *= 2;
		}
		return bodyDataProvider.getRowObject(rowIndex);
	}

	public void addPropertyListener(IPropertyListener l) {
		if (tagStyleManager != null) {
			tagStyleManager.setTagStyle(TagStyle.curStyle);
			autoResize();
			refresh();
		}
		super.addPropertyListener(l);
	}

	/**
	 * (non-Javadoc)
	 * @see net.heartsome.cat.ts.ui.editors.IXliffEditor#affterFuzzyMatchApplayTarget(int, String, String, String)
	 */
	public void affterFuzzyMatchApplayTarget(int rowIndex, String targetContent, String matchType, String quality) {
		StyledTextCellEditor cellEditor = HsMultiActiveCellEditor.getTargetStyledEditor();
		if (cellEditor != null && cellEditor.getRowIndex() == rowIndex) {
			String currentText = cellEditor.getSegmentViewer().getText();
			if (currentText == null || currentText.trim().equals("")) {
				UpdateDataBean bean = new UpdateDataBean(targetContent, matchType, quality);
				cellEditor.setCanonicalValue(bean);
			}
		}
	}

	private Map<Integer, List<String>> termsCache = new HashMap<Integer, List<String>>();

	/**
	 * (non-Javadoc)
	 * @see net.heartsome.cat.ts.ui.editors.IXliffEditor#highlightedTerms(java.util.List)
	 */
	public void highlightedTerms(int rowIndex, List<String> terms) {
		if (terms == null || terms.size() == 0) {
			// terms = HsMultiActiveCellEditor.getCacheSegementTerms(cellEditor.getRowIndex());
			// if(terms != null && terms.size() != 0){
			// cellEditor.highlightedTerms(terms);
			// }
			// HsMultiActiveCellEditor.cacheSegementTerms(HsMultiActiveCellEditor.sourceRowIndex, new
			// ArrayList<String>());
			return;
		}
		HsMultiCellEditor cellEditor = HsMultiActiveCellEditor.getSourceEditor();
		// HsMultiActiveCellEditor.cacheSegementTerms(HsMultiActiveCellEditor.sourceRowIndex, terms);
		termsCache.clear();
		if (rowIndex == -1 || terms == null) {
			return;
		}
		this.termsCache.put(rowIndex, terms);
		if (cellEditor != null) {
			cellEditor.highlightedTerms(terms);
		} else {
			table.redraw();
		}
	}

	public Map<Integer, List<String>> getTermsCache() {
		return termsCache;
	};

	/**
	 * (non-Javadoc)
	 * @see net.heartsome.cat.ts.ui.editors.IXliffEditor#refreshWithNonprinttingCharacter(boolean)
	 */
	public void refreshWithNonprinttingCharacter(boolean isShow) {
		HsMultiActiveCellEditor.commit(true);
		Activator.getDefault().getPreferenceStore()
				.setValue(IPreferenceConstants.XLIFF_EDITOR_SHOWHIDEN_NONPRINTCHARACTER, isShow);
		HsMultiCellEditorControl.activeSourceAndTargetCell(this);
		this.refresh();
	}

	public Combo getFilterCombo() {
		return this.cmbFilter;
	}

	public int getRowCount() {
		return bodyLayer.getRowCount();
	}

	public int getAllRowCount() {
		int rowCount = bodyLayer.getSelectionLayer().getRowCount();
		return isHorizontalLayout() ? rowCount : rowCount / 2;
	}
}

/**
 * TerminologyViewPart.java
 *
 * Version information :
 *
 * Date:Jan 27, 2010
 *
 * Copyright notice :
 */
package net.heartsome.cat.ts.ui.term.view;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

import net.heartsome.cat.common.core.Constant;
import net.heartsome.cat.common.locale.Language;
import net.heartsome.cat.common.locale.LocaleService;
import net.heartsome.cat.common.ui.listener.PartAdapter2;
import net.heartsome.cat.database.bean.TBPreferenceConstants;
import net.heartsome.cat.ts.core.bean.TransUnitBean;
import net.heartsome.cat.ts.tb.match.TbMatcher;
import net.heartsome.cat.ts.ui.Constants;
import net.heartsome.cat.ts.ui.editors.IXliffEditor;
import net.heartsome.cat.ts.ui.grid.GridCopyEnable;
import net.heartsome.cat.ts.ui.term.Activator;
import net.heartsome.cat.ts.ui.term.ImageConstants;
import net.heartsome.cat.ts.ui.term.resource.Messages;
import net.heartsome.cat.ts.ui.view.ITermViewPart;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.nebula.widgets.grid.Grid;
import org.eclipse.nebula.widgets.grid.GridColumn;
import org.eclipse.nebula.widgets.grid.GridItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.part.ViewPart;

/**
 * 术语视图
 * @author stone,Weachy,robert 2011-12-22(实现术语的查询，填充)
 * @version
 * @since JDK1.5
 */
public class TerminologyViewPart extends ViewPart implements ISelectionListener, ITermViewPart {

	/** 常量，视图ID。 */
	public static final String ID = "net.heartsome.cat.ts.ui.term.view.termView";

	/** 源语言 */
	String srcLang;

	/** 目标语言 */
	String tgtLang;

	/** 源语言列 */
	private GridColumn srcTableColumn;

	/** 目标语言列 */
	private GridColumn tgtTableColumn;

	/** 插入术语。 */
	private Action firstAction;

	/** 项目的配置文件的路径 */
	private String curProConfigPath = "";

	private IXliffEditor tempEditor;

	/** 当前行的索引 */
	private int rowIndex;

	private Composite parent;

	private Grid gridTable;
	private GridCopyEnable copyEnable;

	private CLabel tipLabel;
	private Image tipLabelImage;

	private TbMatcher matcher = new TbMatcher();

	private CellRenderer idColumnCellRenderer = new CellRenderer();
	private CellRenderer srcColumnCellRenderer = new CellRenderer();
	private CellRenderer tgtColumnCellRenderer = new CellRenderer();
	private CellRenderer propColumnCellRenderer = new CellRenderer();
	private Color selectedBgColor;

	private FontPropertyChangeListener fontChangeListener = new FontPropertyChangeListener();

	public TerminologyViewPart() {
		JFaceResources.getFontRegistry().addListener(fontChangeListener);
		selectedBgColor = new Color(Display.getDefault(), 210, 210, 240);
		tipLabelImage = Activator.getImageDescriptor("images/status/Loading.png").createImage();
	}

	@Override
	public void init(IViewSite site, IMemento memento) throws PartInitException {
		init(site);

		final IWorkbenchPage page = site.getPage();
		page.addPostSelectionListener(this);
		page.addPartListener(new PartAdapter2() {
			@Override
			public void partClosed(IWorkbenchPartReference partRef) {
				if (gridTable == null || gridTable.isDisposed()) {
					page.removePartListener(this); // 关闭视图后，移除此监听
				} else {
					if ("net.heartsome.cat.ts.ui.xliffeditor.nattable.editor".equals(partRef.getId())) {
						IEditorReference[] editorReferences = page.getEditorReferences();
						if (editorReferences.length == 0) { // 所有编辑器全部关闭的情况下。
							matcher.clearResources();
							firstAction.setEnabled(false);
							copyEnable.resetSelection();
							gridTable.removeAll();
						}
					}
				}
			}
		});
		site.getActionBars().getStatusLineManager()
				.setMessage(Messages.getString("view.TerminologyViewPart.statusLine"));
	}

	/**
	 * 创建控件。
	 * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createPartControl(Composite parent) {
		this.parent = parent;
		createAction();
		GridLayout parentGl = new GridLayout(1, false);
		parentGl.marginWidth = 0;
		parentGl.marginHeight = 0;
		parent.setLayout(parentGl);

		final Composite contentPanel = new Composite(parent, SWT.NONE);
		GridLayout secondPageCompositeGl = new GridLayout(1, false);
		secondPageCompositeGl.marginBottom = -1;
		secondPageCompositeGl.marginLeft = -1;
		secondPageCompositeGl.marginRight = -1;
		secondPageCompositeGl.marginTop = -1;
		secondPageCompositeGl.marginWidth = 0;
		secondPageCompositeGl.marginHeight = 0;
		contentPanel.setLayout(secondPageCompositeGl);
		contentPanel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		contentPanel.setLayout(secondPageCompositeGl);
		contentPanel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		// firstPageComposite = new Composite(contentPanel, SWT.NONE);
		// firstPageComposite.setLayout(new GridLayout(1, false));

		// secondPageComposite = new Composite(contentPanel, SWT.NONE);
		// GridLayout secondPageCompositeGl = new GridLayout(1, false);
		// secondPageCompositeGl.marginBottom = -1;
		// secondPageCompositeGl.marginLeft = -1;
		// secondPageCompositeGl.marginRight = -1;
		// secondPageCompositeGl.marginTop = -1;
		// secondPageCompositeGl.marginWidth = 0;
		// secondPageCompositeGl.marginHeight = 0;
		// secondPageComposite.setLayout(secondPageCompositeGl);
		// secondPageComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		gridTable = new Grid(contentPanel, SWT.BORDER | SWT.V_SCROLL);
		gridTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		gridTable.setHeaderVisible(true);
		gridTable.setAutoHeight(true);
		gridTable.setRowsResizeable(true);
		gridTable.setData("selectedBgColor", selectedBgColor);

		final GridColumn idItem = new GridColumn(gridTable, SWT.NONE);
		idItem.setText(Messages.getString("view.TerminologyViewPart.idItem"));
		idColumnCellRenderer.setFont(JFaceResources.getFont(Constants.MATCH_VIEWER_TEXT_FONT));
		idColumnCellRenderer.setVerticalAlignment(SWT.CENTER);
		idItem.setCellRenderer(idColumnCellRenderer);
		idItem.setWordWrap(true);

		srcTableColumn = new GridColumn(gridTable, SWT.NONE);
		srcTableColumn.setText(Messages.getString("view.TerminologyViewPart.srcTableColumn"));
		srcColumnCellRenderer.setFont(JFaceResources.getFont(Constants.MATCH_VIEWER_TEXT_FONT));
		srcTableColumn.setCellRenderer(srcColumnCellRenderer);
		srcTableColumn.setWordWrap(true);

		tgtTableColumn = new GridColumn(gridTable, SWT.NONE);
		tgtTableColumn.setText(Messages.getString("view.TerminologyViewPart.tgtTableColumn"));
		tgtColumnCellRenderer.setFont(JFaceResources.getFont(Constants.MATCH_VIEWER_TEXT_FONT));
		tgtTableColumn.setCellRenderer(tgtColumnCellRenderer);
		tgtTableColumn.setWordWrap(true);

		final GridColumn propertyColumn = new GridColumn(gridTable, SWT.NONE);
		propertyColumn.setText(Messages.getString("view.TerminologyViewPart.propertyColumn"));
		propColumnCellRenderer.setFont(JFaceResources.getFont(Constants.MATCH_VIEWER_TEXT_FONT));
		propertyColumn.setCellRenderer(propColumnCellRenderer);
		propertyColumn.setWordWrap(true);

		copyEnable = new GridCopyEnable(gridTable);
		srcColumnCellRenderer.setCopyEnable(copyEnable);
		tgtColumnCellRenderer.setCopyEnable(copyEnable);

		// 设置列宽按比例
		contentPanel.addControlListener(new ControlAdapter() {
			public void controlResized(ControlEvent e) {
				Rectangle area = contentPanel.getClientArea();
				Point preferredSize = gridTable.computeSize(SWT.DEFAULT, SWT.DEFAULT);
				int width = area.width; // - 2 * gridTable.getBorderWidth();
				if (preferredSize.y > area.height + gridTable.getHeaderHeight()) {
					Point vBarSize = gridTable.getVerticalBar().getSize();
					width -= vBarSize.x;
				}
				gridTable.setSize(area.width, area.height);
				width -= 45;
				idItem.setWidth(45);
				srcTableColumn.setWidth((int) (width * 0.4));
				tgtTableColumn.setWidth((int) (width * 0.4));
				propertyColumn.setWidth((int) (width * 0.2));
			}
		});
		Composite statusComposite = new Composite(contentPanel, SWT.NONE);
		GridLayout statusComptGridLayout = new GridLayout(2, false);
		statusComptGridLayout.marginBottom = -1;
		statusComptGridLayout.marginLeft = -1;
		statusComptGridLayout.marginRight = -1;
		statusComptGridLayout.marginTop = -1;
		statusComptGridLayout.marginWidth = 0;
		statusComptGridLayout.marginHeight = 0;
		statusComposite.setLayout(statusComptGridLayout);
		statusComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		tipLabel = new CLabel(statusComposite, SWT.NONE);
		tipLabel.setAlignment(SWT.LEFT);

		CLabel label = new CLabel(statusComposite, SWT.None);
		GridData gd = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
		gd.heightHint = 20;
		label.setLayoutData(gd);

		gridTable.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				selectItem();
			}
		});

		gridTable.addListener(SWT.MouseDoubleClick, new Listener() {
			public void handleEvent(Event event) {
				firstAction.run();
			}
		});
		initHookMenu();
	}

	private void initHookMenu() {
		TermViewerBodyMenu bodyMenuManager = new TermViewerBodyMenu(this);
		Action[] action = new Action[1];
		action[0]=firstAction;
		bodyMenuManager.createMenu(action);
		getGridTable().setMenu(bodyMenuManager.getBodyMenu());
	}

	public void selectItem() {
		if (gridTable.getSelection().length <= 0) {
			firstAction.setEnabled(false);
		} else {
			firstAction.setEnabled(true);
		}
	}

	/**
	 * 创建视图工具栏的按钮。
	 */
	private void createAction() {
		firstAction = new Action() {
			@Override
			public void run() {
				if (rowIndex < 0) {
					return;
				}
				if (tempEditor == null || rowIndex < 0) {
					return;
				}
				TransUnitBean transUnit = tempEditor.getRowTransUnitBean(rowIndex);
				Hashtable<String, String> tuProp = transUnit.getTuProps();
				if (tuProp != null) {
					String translate = tuProp.get("translate");
					if (translate != null && translate.equalsIgnoreCase("no")) {
						MessageDialog.openInformation(getSite().getShell(),
								Messages.getString("view.TerminologyViewPart.msgTitle"),
								Messages.getString("view.TerminologyViewPart.msg1"));
						return;
					}
				}

				String tarTerm = "";
				GridItem[] items = gridTable.getSelection();
				if (items.length <= 0) {
					return;
				} else {
					tarTerm = items[0].getText(2);
				}

				try {
					tempEditor.insertCell(rowIndex, tempEditor.getTgtColumnIndex(), tarTerm);
					// tempEditor.setFocus(); // 焦点给回编辑器
				} catch (ExecutionException e) {
					if (Constant.RUNNING_MODE == Constant.MODE_DEBUG) {
						e.printStackTrace();
					}
					MessageDialog.openInformation(parent.getShell(),
							Messages.getString("view.TerminologyViewPart.msgTitle"),
							Messages.getString("view.TerminologyViewPart.msg2") + e.getMessage());
				}
			}
		};
		firstAction.setText(Messages.getString("view.TerminologyViewPart.menu.inserttermtarget"));
		firstAction.setImageDescriptor(Activator.getIconDescriptor(ImageConstants.ACCPTE_TERM));
		firstAction.setToolTipText(Messages.getString("view.TerminologyViewPart.firstAction"));
		firstAction.setEnabled(false);
		//getViewSite().getActionBars().getToolBarManager().add(firstAction);
	}

	public void acceptTermByIndex(int index) {
		if (index < 0 || index + 1 > gridTable.getItemCount()) {
			return;
		}
		gridTable.select(index);
		firstAction.run();
	}

	/**
	 * 获得焦点。
	 * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
	 */
	@Override
	public void setFocus() {
		gridTable.setFocus();
	}

	/**
	 * 监听来自IHSEditor的选中改变事件。
	 * @see org.eclipse.ui.ISelectionListener#selectionChanged(org.eclipse.ui.IWorkbenchPart,
	 *      org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		// UNDO 每次启动程序后，术语匹配面板无法识别出术语。 2012-06-21
		if (part == null || selection == null) {
			return;
		}
		if (part instanceof IXliffEditor) {
			if (!part.equals(tempEditor)) {
				IXliffEditor editor = (IXliffEditor) part;
				tempEditor = editor;

				tempEditor = (IXliffEditor) part;
				FileEditorInput input = (FileEditorInput) getSite().getPage().getActiveEditor().getEditorInput();
				IProject currProject = input.getFile().getProject();
				matcher.setCurrentProject(currProject);
			}
		} else {
			firstAction.setEnabled(false);
			return;
		}

		if (selection.isEmpty() || !(selection instanceof IStructuredSelection)) {
			firstAction.setEnabled(false);
			return;
		}

		IStructuredSelection structuredSelecion = (IStructuredSelection) selection;
		Object object = structuredSelecion.getFirstElement();
		if (object instanceof Integer) {
			rowIndex = (Integer) object;
			IXliffEditor editor = (IXliffEditor) part;
			TransUnitBean bean = editor.getRowTransUnitBean(rowIndex);// handler.getTransUnit(rowId);
			String pureText = bean.getSrcText();
			srcLang = bean.getSrcLang();
			tgtLang = bean.getTgtLang();
			tgtLang = tgtLang == null || tgtLang.equals("") ? editor.getTgtColumnName() : tgtLang;
			srcLang = srcLang == null || "".equals(srcLang) ? editor.getSrcColumnName() : srcLang;
			if (srcLang == null || "".equals(srcLang) || tgtLang == null || "".equals(tgtLang)) {
				return;
			}
			Language srcLangL = LocaleService.getLanguageConfiger().getLanguageByCode(srcLang);
			Language tgtLangL = LocaleService.getLanguageConfiger().getLanguageByCode(tgtLang);
			if (srcLangL.isBidi() || tgtLangL.isBidi()) {
				gridTable.setOrientation(SWT.RIGHT_TO_LEFT);
			} else {
				gridTable.setOrientation(SWT.LEFT_TO_RIGHT);
			}
			srcTableColumn.setText(srcLang);
			tgtTableColumn.setText(tgtLang);

			loadData(pureText, srcLang, tgtLang, true);
		}

	}

	/**
	 * 销毁视图时被调用，移出监听器。
	 * @see org.eclipse.ui.part.WorkbenchPart#dispose()
	 */
	@Override
	public void dispose() {
		getSite().getPage().removePostSelectionListener(this);
		this.tempEditor = null;
		matcher.clearResources();
		JFaceResources.getFontRegistry().removeListener(fontChangeListener);
		if (selectedBgColor != null && !selectedBgColor.isDisposed()) {
			selectedBgColor.dispose();
		}
		if (tipLabelImage != null && !tipLabelImage.isDisposed()) {
			tipLabelImage.dispose();
		}
		super.dispose();
	}

	private TermSearchThread currentThread;

	/**
	 * 获取tableViewer的填充内容 robert
	 * @return
	 */
	private void loadData(String pureText, String srcLang, String tgtLang, boolean isSort) {
		copyEnable.resetSelection();
		gridTable.removeAll();
		if (currentThread != null) {
			currentThread.interrupt();
			currentThread.setStop(true);
		}
		currentThread = new TermSearchThread(pureText, srcLang, tgtLang, isSort);
		currentThread.start();
	}

	class TermSearchThread extends Thread {
		private String pureText;
		private String srcLanguage;
		private String targetLanguage;
		private boolean isSort;
		private boolean stop;

		TermSearchThread(String pureText, String srcLang, String tgtLang, boolean isSort) {
			this.pureText = pureText;
			this.srcLanguage = srcLang;
			this.targetLanguage = tgtLang;
			this.isSort = isSort;
			this.stop = false;
		}

		public void setStop(boolean stop) {
			this.stop = stop;
		}

		@Override
		public void run() {
			updateStatusInfo(Messages.getString("view.TerminologyViewPart.processInfo"));
			Vector<Hashtable<String, String>> terms = matcher.serachTransUnitTerms(pureText, srcLanguage,
					targetLanguage, isSort);
			if (stop) {
				updateStatusInfo("");
				return;
			}
			updateUI(pureText, terms);
		}

		private void updateUI(final String pureText, final Vector<Hashtable<String, String>> terms) {
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					List<String> temp = new ArrayList<String>();
					for (int i = 0; i < terms.size(); i++) {
						GridItem item = new GridItem(gridTable, SWT.NONE);
						item.setData("DBID", terms.get(i).get("tuid"));
						item.setText(0, (i + 1) + "");
						item.setText(1, terms.get(i).get("srcWord"));
						item.setText(2, terms.get(i).get("tgtWord"));
						item.setText(3, terms.get(i).get("property") == null ? "" : terms.get(i).get("property"));
						temp.add(terms.get(i).get("srcWord"));
					}
					if (terms.size() > 0) {
						firstAction.setEnabled(true);
						gridTable.select(0);
					}else{
						firstAction.setEnabled(false);
					}
					updateStatusInfo("");
					tempEditor.highlightedTerms(rowIndex, getHighlightWord(pureText, temp));

				}
			});
		}

		private List<String> getHighlightWord(String pureText, List<String> temp) {
			boolean b = PlatformUI.getPreferenceStore().getBoolean(TBPreferenceConstants.TB_CASE_SENSITIVE);
			if (!b) {
				return temp;
			}
			List<String> rs = new ArrayList<String>();
			String ignoreCaseMathText = null;
			for (String word : temp) {
				ignoreCaseMathText = getIgnoreCaseMathText(pureText, word);
				if (null != ignoreCaseMathText) {
					rs.add(ignoreCaseMathText);
				}
			}
			return rs;
		}

		private String getIgnoreCaseMathText(String src, String contanStr) {
			String temp = src.toUpperCase(Locale.US);
			contanStr = contanStr.toUpperCase(Locale.US);
			int index = temp.indexOf(contanStr);
			if (index < 0) {
				return null;
			} else {
				return src.substring(index, index + contanStr.length());
			}
		}

		/**
		 * 更新状态信息
		 * @param content
		 *            ;
		 */
		private void updateStatusInfo(final String content) {
			Display.getDefault().syncExec(new Runnable() {

				public void run() {
					if (content != null && content.length() != 0) {
						setProcessMessage(tipLabelImage, content, "");
					} else {
						setProcessMessage(null, content, "");
					}
				}
			});
		}
	};

	public void refresh() {
		// FIXME when the program started ,select nothing.tempEditor is null.
		if (null == tempEditor) {
			return;
		}
		TransUnitBean bean = tempEditor.getRowTransUnitBean(rowIndex);// handler.getTransUnit(rowId);
		String pureText = bean.getSrcText();
		String srcLang = bean.getSrcLang();
		String tgtLang = bean.getTgtLang();
		tgtLang = tgtLang == null || tgtLang.equals("") ? tempEditor.getTgtColumnName() : tgtLang;
		srcLang = srcLang == null || "".equals(srcLang) ? tempEditor.getSrcColumnName() : srcLang;
		loadData(pureText, srcLang, tgtLang, true);
	}

	/**
	 * tableViewer的标签提供器
	 * @author robert
	 */
	class TableViewerLabelProvider extends LabelProvider implements ITableLabelProvider {
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}

		public String getColumnText(Object element, int columnIndex) {
			if (element instanceof String[]) {
				String[] array = (String[]) element;
				return array[columnIndex];
			}
			return null;
		}
	}

	// public TermOperator getTermOper() {
	// return termOper;
	// }

	public String getCurProConfigPath() {
		return curProConfigPath;
	}

	public Grid getGridTable() {
		return gridTable;
	}

	class FontPropertyChangeListener implements IPropertyChangeListener {

		public void propertyChange(PropertyChangeEvent event) {
			if (gridTable == null || gridTable.isDisposed()) {
				return;
			}
			String property = event.getProperty();
			if (Constants.MATCH_VIEWER_TEXT_FONT.equals(property)) {
				Font font = JFaceResources.getFont(Constants.MATCH_VIEWER_TEXT_FONT);
				idColumnCellRenderer.setFont(font);
				srcColumnCellRenderer.setFont(font);
				tgtColumnCellRenderer.setFont(font);
				propColumnCellRenderer.setFont(font);
				gridTable.redraw();
			}
		}
	}

	private void setProcessMessage(Image image, String message, String tooltip) {
		if (image != null && !image.isDisposed()) {
			tipLabel.setImage(image);
		} else {
			tipLabel.setImage(null);
		}
		tipLabel.setText(message);
		tipLabel.setToolTipText(tooltip);
		tipLabel.pack();
	}

	IXliffEditor getXliffEditor() {
		return tempEditor;
	}
}

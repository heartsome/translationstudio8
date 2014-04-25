package net.heartsome.cat.ts.ui.translation.view;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import net.heartsome.cat.common.locale.Language;
import net.heartsome.cat.common.locale.LocaleService;
import net.heartsome.cat.common.ui.listener.PartAdapter2;
import net.heartsome.cat.common.util.CommonFunction;
import net.heartsome.cat.common.util.DateUtils;
import net.heartsome.cat.ts.core.bean.AltTransBean;
import net.heartsome.cat.ts.core.bean.Constants;
import net.heartsome.cat.ts.core.bean.PropBean;
import net.heartsome.cat.ts.core.bean.PropGroupBean;
import net.heartsome.cat.ts.core.bean.TransUnitBean;
import net.heartsome.cat.ts.core.file.XLFHandler;
import net.heartsome.cat.ts.tm.bean.TransUnitInfo2TranslationBean;
import net.heartsome.cat.ts.tm.complexMatch.ComplexMatcherFactory;
import net.heartsome.cat.ts.tm.complexMatch.IComplexMatch;
import net.heartsome.cat.ts.tm.match.TmMatcher;
import net.heartsome.cat.ts.tm.simpleMatch.ISimpleMatcher;
import net.heartsome.cat.ts.tm.simpleMatch.SimpleMatcherFactory;
import net.heartsome.cat.ts.ui.bean.TranslateParameter;
import net.heartsome.cat.ts.ui.editors.IXliffEditor;
import net.heartsome.cat.ts.ui.grid.GridCopyEnable;
import net.heartsome.cat.ts.ui.innertag.SegmentViewer;
import net.heartsome.cat.ts.ui.translation.Activator;
import net.heartsome.cat.ts.ui.translation.bean.TmConstants;
import net.heartsome.cat.ts.ui.translation.resource.Messages;
import net.heartsome.cat.ts.ui.util.TmUtils;
import net.heartsome.cat.ts.ui.view.IMatchViewPart;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.nebula.widgets.grid.Grid;
import org.eclipse.nebula.widgets.grid.GridColumn;
import org.eclipse.nebula.widgets.grid.GridItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.StyledText;
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
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.part.ViewPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MatchViewPart extends ViewPart implements ISelectionListener, IMatchViewPart {

	public static final Logger LOGGER = LoggerFactory.getLogger(MatchViewPart.class);

	public static final String ID = "net.heartsome.cat.ts.ui.translation.view.matchview";
	/** 当前编辑器中对应的XLIFF文件Handler */
	// private XLFHandler handler;
	/** 被监听的编辑器。 */
	// IXliffEditor editor;
	/** 编辑器中选中的行标识 */
	// int rowIndex = -1;
	// private IProject currentProject;
	// TransUnitBean currentTransUnitBean;

	SegmentViewer sourceText;
	Grid gridTable;
	private SourceColunmCellRenderer sourceColunmCellRenderer = new SourceColunmCellRenderer();
	private TypeColunmCellRenderer typeColumnCellRenderer = new TypeColunmCellRenderer();
	private TargetColunmCellRenderer targetColumnCellRenderer = new TargetColunmCellRenderer();
	GridCopyEnable copyEnable;

	private CLabel infoLabel;
	private Image infoLabelImage;
	private CLabel tipLabel;
	private Image tipLabelImage;

	MatchViewerBodyMenu menuMgr;
	TmMatcher tmMatcher;

	// private TransUnitInfo2TranslationBean tuInfoBean;

	TranslateParameter transParameter;

	private Image tmImage;
	private Image googleImage;
	private Image qtImage;
	private Image bingImage;
	private Image otherImage;

	private Color selectedBgColor;

	private List<AltTransBean> altTransCacheList = new ArrayList<AltTransBean>();

	private FontPropertyChangeListener fontChangeListener = new FontPropertyChangeListener();
	private ExecuteMatchThread matcherThread;
	private ManualTranslationTread manualTranslationThread;
	private TranslationTaskContainer manualTranslationTaskContainer;

	public MatchViewPart() {
		tmMatcher = new TmMatcher();
		transParameter = TranslateParameter.getInstance();

		tmImage = Activator.getImageDescriptor("images/match-type/tm.png").createImage();
		qtImage = Activator.getImageDescriptor("images/match-type/qt.png").createImage();
		googleImage = Activator.getImageDescriptor("images/match-type/google.png").createImage();
		bingImage = Activator.getImageDescriptor("images/match-type/bing.png").createImage();
		otherImage = Activator.getImageDescriptor("images/match-type/others.png").createImage();
		selectedBgColor = new Color(Display.getDefault(), 210, 210, 240);
		JFaceResources.getFontRegistry().addListener(fontChangeListener);
		tipLabelImage = Activator.getImageDescriptor("images/status/Loading.png").createImage();

		// 初始化自动任务线程在SelectionChange事件时，自动任务线程在任务到达时再启动，完成后任务后线程停止

		// 初始化手动任务线程，手动任务线程在界面创建时启动，启动后等待任务的到达，到达后还需要等待自动任务线程结束才执行
		manualTranslationTaskContainer = new TranslationTaskContainer();
		manualTranslationThread = new ManualTranslationTread(manualTranslationTaskContainer);
		manualTranslationThread.start();

	}

	@Override
	public void init(IViewSite site, IMemento memento) throws PartInitException {
		init(site);
		site.getPage().addPostSelectionListener(this);
		site.getPage().addPartListener(new PartAdapter2() {
			@Override
			public void partClosed(IWorkbenchPartReference partRef) {
				if (gridTable == null || gridTable.isDisposed()) {
					getSite().getPage().removePartListener(this); // 关闭视图后，移除此监听
				} else {
					if ("net.heartsome.cat.ts.ui.xliffeditor.nattable.editor".equals(partRef.getId())) {
						IEditorReference[] editorReferences = getSite().getPage().getEditorReferences();
						if (editorReferences.length == 0) { // 所有编辑器全部关闭的情况下。
							synchronized (matchDataContainer) {
								matchDataContainer.clear();
								if (matcherThread != null) {
									matcherThread.interrupt();
								}
							}
							manualTranslationThread.interruptCurrentTask();

							tmMatcher.clearDbResources();
							copyEnable.resetSelection();
							gridTable.removeAll();
							sourceText.setText("");
							setMatchMessage(null, "", "");
							setProcessMessage(null, "", "");
						}
					}
				}
			}
		});
		site.getActionBars().getStatusLineManager().setMessage(Messages.getString("view.MatchViewPart.statusLine"));
	}

	@Override
	public void createPartControl(Composite parent) {
		GridLayout parentGl = new GridLayout(1, false);
		parentGl.marginWidth = 0;
		parentGl.marginHeight = 0;
		parent.setLayout(parentGl);

		final Composite composite = new Composite(parent, SWT.NONE);
		GridLayout compositeGl = new GridLayout(1, false);
		compositeGl.marginBottom = -1;
		compositeGl.marginLeft = -1;
		compositeGl.marginRight = -1;
		compositeGl.marginTop = 0;
		compositeGl.marginWidth = 0;
		compositeGl.marginHeight = 0;
		compositeGl.verticalSpacing = 0;
		composite.setLayout(compositeGl);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		// sourceText = new StyledText(composite, SWT.BORDER | SWT.V_SCROLL | SWT.READ_ONLY);
		// GridData sTextGd = new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1);
		// Font f = JFaceResources.getFont(net.heartsome.cat.ts.ui.Constants.MATCH_VIEWER_TEXT_FONT);
		// sourceText.setFont(f);
		// int lineH = sourceText.getLineHeight() * 3;
		// sTextGd.heightHint = lineH;
		// sTextGd.minimumHeight = lineH;
		// sourceText.setLayoutData(sTextGd);
		SashForm sashForm = new SashForm(composite, SWT.VERTICAL);
		sashForm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		sourceText = new SegmentViewer(sashForm, SWT.MULTI | SWT.WRAP | SWT.BORDER | SWT.READ_ONLY | SWT.V_SCROLL, null);
		StyledText srcTextControl = sourceText.getTextWidget();
		srcTextControl.setLineSpacing(net.heartsome.cat.ts.ui.Constants.SEGMENT_LINE_SPACING);
		srcTextControl.setLeftMargin(net.heartsome.cat.ts.ui.Constants.SEGMENT_LEFT_MARGIN);
		srcTextControl.setRightMargin(net.heartsome.cat.ts.ui.Constants.SEGMENT_RIGHT_MARGIN);
		srcTextControl.setTopMargin(net.heartsome.cat.ts.ui.Constants.SEGMENT_TOP_MARGIN);
		srcTextControl.setBottomMargin(net.heartsome.cat.ts.ui.Constants.SEGMENT_TOP_MARGIN);
		srcTextControl.setFont(JFaceResources.getFont(net.heartsome.cat.ts.ui.Constants.MATCH_VIEWER_TEXT_FONT));
		sourceText.setSource("");
		sourceColunmCellRenderer.setSegmentViewer(sourceText);
		GridData sTextGd = new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1);
		Font f = JFaceResources.getFont(net.heartsome.cat.ts.ui.Constants.MATCH_VIEWER_TEXT_FONT);
		srcTextControl.setFont(f);
		int lineH = srcTextControl.getLineHeight() * 2;
		sTextGd.heightHint = lineH;
		sTextGd.minimumHeight = lineH;
		srcTextControl.setLayoutData(sTextGd);
		net.heartsome.cat.ts.ui.innertag.tagstyle.TagStyleConfigurator.configure(sourceText);

		gridTable = new Grid(sashForm, SWT.BORDER | SWT.V_SCROLL);
		gridTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		gridTable.setHeaderVisible(false);
		gridTable.setAutoHeight(true);
		gridTable.setRowsResizeable(true);
		gridTable.setData("selectedBgColor", selectedBgColor);

		final GridColumn sourceCln = new GridColumn(gridTable, SWT.NONE);
		sourceColunmCellRenderer.setFont(JFaceResources
				.getFont(net.heartsome.cat.ts.ui.Constants.MATCH_VIEWER_TEXT_FONT));
		sourceCln.setCellRenderer(sourceColunmCellRenderer);
		sourceCln.setText(Messages.getString("view.MatchViewPart.sourceCln"));
		sourceCln.setWordWrap(true);
		sourceCln.setAlignment(SWT.CENTER);
		sourceCln.setResizeable(false);

		final GridColumn typeCln = new GridColumn(gridTable, SWT.NONE);

		typeColumnCellRenderer.setVerticalAlignment(SWT.CENTER);
		typeCln.setCellRenderer(typeColumnCellRenderer);
		typeCln.setText(Messages.getString("view.MatchViewPart.typeCln"));
		typeCln.setWordWrap(true);
		typeCln.setAlignment(SWT.CENTER);
		typeCln.setResizeable(false);

		final GridColumn targetCln = new GridColumn(gridTable, SWT.NONE);
		targetColumnCellRenderer.setFont(JFaceResources
				.getFont(net.heartsome.cat.ts.ui.Constants.MATCH_VIEWER_TEXT_FONT));
		targetCln.setCellRenderer(targetColumnCellRenderer);
		targetCln.setText(Messages.getString("view.MatchViewPart.targetCln"));
		targetCln.setWordWrap(true);
		targetCln.setAlignment(SWT.CENTER);
		targetCln.setResizeable(false);

		// 设置可复制功能
		copyEnable = new GridCopyEnable(gridTable);
		sourceColunmCellRenderer.setCopyEnable(copyEnable);
		targetColumnCellRenderer.setCopyEnable(copyEnable);

		Composite statusComposite = new Composite(composite, SWT.NONE);
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

		infoLabel = new CLabel(statusComposite, SWT.NONE);
		GridData gd = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd.heightHint = 20;
		infoLabel.setLayoutData(gd);
		infoLabel.setAlignment(SWT.RIGHT);
		// 设置列宽按比例4.5:1:4.5
		composite.addControlListener(new ControlAdapter() {
			public void controlResized(ControlEvent e) {
				Rectangle area = composite.getClientArea();
				Point preferredSize = gridTable.computeSize(SWT.DEFAULT, SWT.DEFAULT);
				int width = area.width;// - 2 * gridTable.getBorderWidth();
				if (preferredSize.y > area.height + gridTable.getHeaderHeight()) {
					Point vBarSize = gridTable.getVerticalBar().getSize();
					width -= vBarSize.x;
				}
				gridTable.setSize(area.width, area.height);
				width = width - 42;
				sourceCln.setWidth((int) (width * 0.5));
				typeCln.setWidth(42);
				targetCln.setWidth((int) (width * 0.5));
			}
		});
		gridTable.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				StyledText text = sourceText.getTextWidget();
				text.setText(text.getText());
				updateActionState();
				GridItem[] selItems = gridTable.getSelection();
				if (selItems.length != 1) {
					return;
				}
				GridItem item = selItems[0];
				setMatchMessage(infoLabelImage, item.getData("info").toString(), item.getData("infoTooltip").toString());
				composite.layout();
			}
		});
		gridTable.addListener(SWT.MouseDoubleClick, new Listener() {
			public void handleEvent(Event event) {
				menuMgr.acceptMatchAction.run();
			}
		});
		createActions();
		sashForm.setWeights(new int[] { 3, 8 });
	}

	/**
	 * 监听XLFEditor的选择事件
	 */
	public void selectionChanged(final IWorkbenchPart part, final ISelection selection) {
		if (part == null || selection == null) {
			return;
		}
		if (!(part instanceof IEditorPart)) {
			updateActionState();
			return;
		}
		if (selection.isEmpty() || !(selection instanceof IStructuredSelection)) {
			updateActionState();
			return;
		}
		IXliffEditor editor = (IXliffEditor) part;
		IStructuredSelection structuredSelecion = (IStructuredSelection) selection;
		final Object object = structuredSelecion.getFirstElement();
		if (object instanceof Integer) {
			int rowIndex = -1;
			int selRowIndex = (Integer) object;
			if (rowIndex == selRowIndex) {
				if (gridTable.getItemCount() != 0) {
					updateActionState();
				}
				return;
			} else {
				rowIndex = selRowIndex;
			}

			XLFHandler handler = editor.getXLFHandler();

			String rowId = handler.getRowId(rowIndex);
			TransUnitBean transUnit = editor.getRowTransUnitBean(rowIndex);// handler.getTransUnit(rowId);
			if (transUnit == null) {
				updateActionState();
				return;
			}
			TransUnitInfo2TranslationBean tuInfoBean = getTuInfoBean(transUnit, handler, rowId);
			FileEditorInput input = (FileEditorInput) getSite().getPage().getActiveEditor().getEditorInput();
			IProject currentProject = input.getFile().getProject();
			copyEnable.resetSelection();
			gridTable.removeAll();
			altTransCacheList.clear();
			menuMgr.setEditor(editor);
			menuMgr.setRowIndex(rowIndex);
			executeMatch(editor, rowId, transUnit, tuInfoBean, currentProject);
		}
	}

	private void executeMatch(IXliffEditor editor, String rowId, TransUnitBean transUnit,
			TransUnitInfo2TranslationBean tuInfo, IProject project) {
		if (matcherThread == null) {
			matcherThread = new ExecuteMatchThread();
			matcherThread.start();
		}
		synchronized (matchDataContainer) {
			matchDataContainer.clear();
			matchDataContainer.add(new MatchData(transUnit, tuInfo, project, rowId, editor));
			matchDataContainer.notify();
		}
	}

	public void refreshTable() {
		gridTable.redraw();
	}

	public void reLoadMatches(IXliffEditor editor, int rowIndex) {
		// 修复 Bug #3064 编辑匹配--更换记忆库后再编辑原记忆库匹配，出现异常.刷新问题
		TransUnitBean transUnit = editor.getRowTransUnitBean(rowIndex);// handler.getTransUnit(rowId);
		if (transUnit == null) {
			return;
		}
		XLFHandler handler = editor.getXLFHandler();
		if (handler == null) {
			return;
		}

		IProject prj = null;
		if (editor instanceof IEditorPart) {
			IEditorPart p = (IEditorPart) editor;
			FileEditorInput input = (FileEditorInput) p.getEditorInput();
			prj = input.getFile().getProject();
		}
		if (prj == null) {
			return;
		}
		String rowId = handler.getRowId(rowIndex);
		TransUnitInfo2TranslationBean tuInfoBean = getTuInfoBean(transUnit, handler, rowId);
		executeMatch(editor, rowId, transUnit, tuInfoBean, prj);
	}

	public void acceptMatchByIndex(int index) {
		if (index < 0 || index + 1 > gridTable.getItemCount()) {
			return;
		}
		gridTable.select(index);
		menuMgr.acceptMatchAction.run();
	}

	public void manualExecComplexTranslation(int rowIndex, IXliffEditor editor, IComplexMatch complexMatcher) {
		if (rowIndex == -1) {
			return;
		}
		TransUnitBean transUnit = editor.getRowTransUnitBean(rowIndex);// handler.getTransUnit(rowId);
		if (transUnit == null) {
			return;
		}
		XLFHandler handler = editor.getXLFHandler();
		if (handler == null) {
			return;
		}

		IProject prj = null;
		if (editor instanceof IEditorPart) {
			IEditorPart p = (IEditorPart) editor;
			FileEditorInput input = (FileEditorInput) p.getEditorInput();
			prj = input.getFile().getProject();
		}
		if (prj == null) {
			return;
		}
		String rowId = handler.getRowId(rowIndex);
		TransUnitInfo2TranslationBean tuInfo = getTuInfoBean(transUnit, handler, rowId);
		TranslationTaskData data = new TranslationTaskData(complexMatcher, transUnit, tuInfo, editor, rowIndex, prj);
		synchronized (manualTranslationTaskContainer) {
			manualTranslationTaskContainer.pushTranslationTask(data);
			manualTranslationTaskContainer.notify();
		}
	}

	public void manualExecSimpleTranslation(int rowIndex, IXliffEditor editor, ISimpleMatcher simpleMatcher) {
		if (rowIndex == -1) {
			return;
		}
		TransUnitBean transUnit = editor.getRowTransUnitBean(rowIndex);// handler.getTransUnit(rowId);
		if (transUnit == null) {
			return;
		}
		XLFHandler handler = editor.getXLFHandler();
		if (handler == null) {
			return;
		}

		IProject prj = null;
		if (editor instanceof IEditorPart) {
			IEditorPart p = (IEditorPart) editor;
			FileEditorInput input = (FileEditorInput) p.getEditorInput();
			prj = input.getFile().getProject();
		}
		if (prj == null) {
			return;
		}
		String rowId = handler.getRowId(rowIndex);
		TransUnitInfo2TranslationBean tuInfo = getTuInfoBean(transUnit, handler, rowId);
		TranslationTaskData data = new TranslationTaskData(simpleMatcher, transUnit, tuInfo, editor, rowIndex, prj);
		synchronized (manualTranslationTaskContainer) {
			manualTranslationTaskContainer.pushTranslationTask(data);
			manualTranslationTaskContainer.notify();
		}
	}

	Vector<MatchData> matchDataContainer = new Vector<MatchData>();

	/**
	 * 自动任务线程
	 * @author Jason
	 * @version
	 * @since JDK1.6
	 */
	class ExecuteMatchThread extends Thread {
		private TransUnitBean transUnit;
		private TransUnitInfo2TranslationBean tuInfoBean;
		private IProject project;
		private String rowId;
		private XLFHandler handler;
		private IXliffEditor editor;
		private boolean stop;

		public ExecuteMatchThread() {
		}

		/**
		 * stop current thread
		 */
		public void setStop() {
			this.stop = true;
		}

		@Override
		public void run() {
			while (!stop) {
				try {
					// 无新数据到达前进行等待状态
					synchronized (matchDataContainer) {
						if (matchDataContainer.isEmpty()) {
							try {
								matchDataContainer.wait();
							} catch (InterruptedException e) {
							}
						}
					}
					if (matchDataContainer.size() == 0) {
						continue;
					}
					// 查找匹配前先清除当前界面中的内容
					Display.getDefault().syncExec(new Runnable() {

						public void run() {
							copyEnable.resetSelection();
							if (!gridTable.isDisposed()) {
								gridTable.removeAll();
							}
							altTransCacheList.clear();
						}
					});
					MatchData d = matchDataContainer.remove(matchDataContainer.size() - 1);
					this.rowId = d.getRowId();
					this.transUnit = d.getTransUnit();
					this.project = d.getProject();
					this.editor = d.getEditor();
					this.handler = this.editor.getXLFHandler();
					this.tuInfoBean = d.getTuInfo();
					// -- execute translation memory match
					manualTranslationThread.interruptCurrentTask();
					manualTranslationThread.setLock(true);
					updateStatusInfo(Messages.getString("view.MatchViewPart.processInfo.loadHSMatch"));
					Vector<AltTransBean> complexMatches = null;
					if (!CommonFunction.checkEdition("L")) {
						Vector<AltTransBean> fuzzy = TmUtils.fuzzyResult2Alttransbean(tmMatcher.executeFuzzySearch(
								project, tuInfoBean));
						transUnit.updateMatches(Constants.TM_TOOLID, fuzzy);
						complexMatches = executeComplexMatch(transUnit, project);
					}
					final Vector<AltTransBean> tmAltTrans = transUnit.getMatchesByToolId(Constants.TM_TOOLID);
					altTransCacheList.addAll(tmAltTrans);
					if (complexMatches != null && complexMatches.size() > 0) {
						altTransCacheList.addAll(complexMatches);
						complexMatches.clear();
						complexMatches = null;
					}
					loadData2UI(altTransCacheList);
					// 加载Simple Match,如google ,bing
					final List<String> needClearToolId = new ArrayList<String>(); // 需要清掉文件中原有的匹配
					final List<AltTransBean> needSaveAltTransList = new ArrayList<AltTransBean>();
					List<AltTransBean> needLoadAltTransList = new ArrayList<AltTransBean>();
					executeSimpleMatch(tuInfoBean, transUnit, needClearToolId, needSaveAltTransList,
							needLoadAltTransList);
					loadData2UI(needLoadAltTransList);
					altTransCacheList.addAll(needLoadAltTransList);
					needLoadAltTransList.clear();
					// 加载文件中的其他匹配
					Vector<AltTransBean> cm = new Vector<AltTransBean>();
					if (transUnit.getMatches() != null) {
						cm.addAll(transUnit.getMatches());
						cm.removeAll(altTransCacheList);
						altTransCacheList.addAll(cm);
						if (cm.size() > 0) {
							loadData2UI(cm);
						}
						cm.clear();
					}
					Display.getDefault().syncExec(new Runnable() {

						public void run() {
							updateUI(tmAltTrans, handler.getRowIndex(rowId));
							setProcessMessage(null, "", "");
						}
					});
					if (needSaveAltTransList.size() > 0 && handler != null) {
						Display.getDefault().syncExec(new Runnable() {

							public void run() {
								// 重新写入altTrans
								handler.updateAltTrans(rowId, needSaveAltTransList, needClearToolId);
								needSaveAltTransList.clear();
								needClearToolId.clear();
							}
						});
					}
					manualTranslationThread.setLock(false);
				} catch (Exception e) {
					continue;
				}
			}
		}

		/**
		 * 将数据加载到界面中
		 * @param altTransVector
		 *            ;
		 */
		private void loadData2UI(final List<AltTransBean> altTransVector) {
			Display.getDefault().syncExec(new Runnable() {

				public void run() {
					sourceText.setText(transUnit.getSrcContent());
					loadData(altTransVector);
				}
			});
		}

		/**
		 * 更新界面，执行默认选中以及应用最大匹配、复制源到目标自动策略
		 * @param tmAltTrans
		 *            ;
		 */
		private void updateUI(Vector<AltTransBean> tmAltTrans, int rowIndex) {
			if (gridTable.isDisposed()) {
				return;
			}
			int itemSize = gridTable.getItemCount();
			String tgt = transUnit.getTgtContent();
			if (itemSize > 0) {
				gridTable.setSelection(0);

				// 在编辑器中切换文本段时刷新 infoLabel 的内容
				GridItem selItem = gridTable.getItem(0);
				setMatchMessage(infoLabelImage, selItem.getData("info").toString(), selItem.getData("infoTooltip")
						.toString());
				infoLabel.getParent().layout();

				updateActionState();

				// 无译文时，应用最大记忆库匹配
				if ((tgt == null || tgt.equals("")) && transParameter.isApplyTmMatch() && tmAltTrans.size() > 0) {
					menuMgr.acceptMatchAction.run();
				}

			} else {
				// 在编辑器中切换文本段时刷新 infoLabel 的内容
				setMatchMessage(null, "", "");
				infoLabel.getParent().layout();

				// 无译文，无匹配时，复制来源到目标
				if (tgt == null || tgt.equals("") && transParameter.isApplySource()) {
					editor.affterFuzzyMatchApplayTarget(rowIndex, transUnit.getSrcContent(), null, null);
				}
				updateActionState();
			}
			tmAltTrans.clear();
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
						setProcessMessage(null, "", "");
					}
				}
			});
		}

		private Vector<AltTransBean> executeComplexMatch(TransUnitBean transUnitBean, IProject currentProject) {
			List<IComplexMatch> matchers = ComplexMatcherFactory.getInstance().getCuurentMatcher();
			Vector<AltTransBean> allMatchs = new Vector<AltTransBean>();
			for (IComplexMatch matcher : matchers) {
				String toolId = matcher.getToolId();
				if (!(toolId.equals(Constants.QT_TOOLID) && transParameter.isAutoQuickTrans())) {
					continue;
				}
				Vector<AltTransBean> result = matcher.executeTranslation(transUnitBean, currentProject);
				if (result.size() > 0) {
					allMatchs.addAll(result);
				}
			}
			return allMatchs;
		}

		private void executeSimpleMatch(TransUnitInfo2TranslationBean tuInfo, TransUnitBean transUnit,
				List<String> needClearToolId, List<AltTransBean> needSaveAltTransList,
				List<AltTransBean> needLoadAltTransList) {
			// 如果忽略锁定的文本，不进行机器翻译
			if (TranslateParameter.getInstance().isIgnoreLock()) {
				if ("no".equals(transUnit.getTuProps().get("translate"))) {
					return;
				}
			}
			// 如果忽略上下文匹配和完全匹配，不翻译
			if (TranslateParameter.getInstance().isIgnoreExactMatch()) {
				if ("100".equals(transUnit.getTgtProps().get("hs:quality"))
						|| "101".equals(transUnit.getTgtProps().get("hs:quality"))) {
					return;
				}
			}
			List<ISimpleMatcher> simpleMatchers = SimpleMatcherFactory.getInstance().getCuurentMatcher();
			for (ISimpleMatcher matcher : simpleMatchers) {
				String toolId = matcher.getMathcerToolId();
				String matcherType = matcher.getMatcherType();
				Vector<AltTransBean> currentMatch = transUnit.getMatchesByToolId(toolId);
				boolean isOverwrite = matcher.isOverwriteMatch();
				if (!matcher.matchChecker()) {
					needLoadAltTransList.addAll(currentMatch);
					continue;
				}
				if (currentMatch.size() > 0 && !isOverwrite) {
					needLoadAltTransList.addAll(currentMatch);
					continue;
				} else {
					String tgtText = matcher.executeMatch(tuInfo);
					if (tgtText.equals("")) {
						continue;
					}

					AltTransBean bean = new AltTransBean(tuInfo.getSrcPureText(), tgtText, tuInfo.getSrcLanguage(),
							tuInfo.getTgtLangugage(), matcher.getMathcerOrigin(), toolId);
					bean.getMatchProps().put("match-quality", "100");
					bean.setSrcContent(tuInfo.getSrcPureText());
					bean.setTgtContent(tgtText);
					bean.getMatchProps().put("hs:matchType", matcherType);

					currentMatch.clear();
					currentMatch.add(bean);
					needLoadAltTransList.addAll(currentMatch);

					if (CommonFunction.checkEdition("U") && matcher.isSuportPreTrans()) {
						needSaveAltTransList.add(bean);
						transUnit.updateMatches(toolId, currentMatch);
						if (currentMatch.size() > 0) {
							needClearToolId.add(toolId);
						}
					}
				}
			}
		}
	};

	/**
	 * 手动任务线程
	 * @author Jason
	 * @version
	 * @since JDK1.6
	 */
	class ManualTranslationTread extends Thread {

		private boolean isLocked;

		private boolean stop;

		private boolean interrupt;

		private TranslationTaskContainer container;

		private MessageFormat msgFormat;

		private TransUnitBean transUnitBean;
		private TransUnitInfo2TranslationBean tuInfoBean;
		private IXliffEditor editor;
		private int rowIndex;
		private IProject project;

		public ManualTranslationTread(TranslationTaskContainer container) {
			this.container = container;
			isLocked = false;
			stop = false;
			interrupt = false;
			msgFormat = new MessageFormat(Messages.getString("view.MatchViewPart.processInfo.manualProcess"));
		}

		@Override
		public void run() {
			while (!stop) {
				synchronized (container) {
					if (container.isEmpty()) {
						try {
							container.wait();
						} catch (InterruptedException e) {
						}
					}
				}
				if (isLocked) {
					synchronized (container) {
						try {
							container.wait();
						} catch (InterruptedException e) {
						}
					}
				}
				TranslationTaskData data = container.popTranslationTask();
				if (data == null) {
					interrupt = false;
					continue;
				}
				transUnitBean = data.getTransUnit();
				tuInfoBean = data.getTuInfo();
				editor = data.getEditor();
				Object matcher = data.getMatcher();
				rowIndex = data.getRowIndex();
				project = data.getProject();
				if (TranslateParameter.getInstance().isIgnoreLock()) {
					if ("no".equals(transUnitBean.getTuProps().get("translate"))) {
						continue;
					}
				}
				// 如果忽略上下文匹配和完全匹配，不翻译
				if (TranslateParameter.getInstance().isIgnoreExactMatch()) {
					if ("100".equals(transUnitBean.getTgtProps().get("hs:quality"))
							|| "101".equals(transUnitBean.getTgtProps().get("hs:quality"))) {
						continue;
					}
				}

				if (matcher instanceof ISimpleMatcher) {
					if (rowIndex == -1 || tuInfoBean == null) {
						continue;
					}
					if (interrupt) {
						interrupt = false; // reset for next task
						continue;
					}
					final ISimpleMatcher simpleMatcher = (ISimpleMatcher) matcher;
					updateStatusInfo(msgFormat.format(new String[] { simpleMatcher.getMathcerToolId() }));
					String tgtText = simpleMatcher.executeMatch(tuInfoBean);
					if (tgtText.equals("")) {
						updateStatusInfo(null);
						continue;
					}
					if (interrupt) {
						interrupt = false; // reset for next task
						updateStatusInfo(null);
						continue;
					}
					AltTransBean bean = new AltTransBean(tuInfoBean.getSrcPureText(), tgtText,
							tuInfoBean.getSrcLanguage(), tuInfoBean.getTgtLangugage(),
							simpleMatcher.getMathcerOrigin(), simpleMatcher.getMathcerToolId());
					bean.getMatchProps().put("match-quality", "100");
					bean.setSrcContent(tuInfoBean.getSrcPureText());
					bean.setTgtContent(tgtText);
					bean.getMatchProps().put("hs:matchType", simpleMatcher.getMatcherType());

					// first refresh cache, then use cache refresh UI
					final List<AltTransBean> newAltTrans = new ArrayList<AltTransBean>();
					newAltTrans.add(bean);
					if (interrupt) {
						updateStatusInfo(null);
						interrupt = false; // reset for next task
						continue;
					}
					refreshAltTransCache(newAltTrans);
					refreshUI();
					final XLFHandler handler = editor.getXLFHandler();
					if (CommonFunction.checkEdition("U") && simpleMatcher.isSuportPreTrans() && handler != null) {

						Display.getDefault().syncExec(new Runnable() {

							public void run() {
								// 重新写入altTrans
								List<String> toolIdList = new ArrayList<String>();
								toolIdList.add(simpleMatcher.getMathcerToolId());
								handler.updateAltTrans(handler.getRowId(rowIndex), newAltTrans, toolIdList);
								toolIdList.clear();
								newAltTrans.clear();
							}
						});
					}
				} else if (matcher instanceof IComplexMatch) {
					IComplexMatch complexMatch = (IComplexMatch) matcher;
					updateStatusInfo(msgFormat.format(new String[] { complexMatch.getToolId() }));
					if (rowIndex != -1 && transUnitBean != null && transUnitBean != null) {
						if (interrupt) {
							interrupt = false; // reset for next task
							updateStatusInfo(null);
							continue;
						}
						Vector<AltTransBean> result = complexMatch.executeTranslation(transUnitBean, project);
						if (interrupt) {
							interrupt = false; // reset for next task
							updateStatusInfo(null);
							continue;
						}
						if (result.size() > 0) {
							// first refresh cache, then use cache refresh UI
							refreshAltTransCache(result);
							refreshUI();
						} else {
							updateStatusInfo(null);
						}
					}
				}
			}
		}

		/**
		 * 中断当前任务，停止当前任务的执行，并清空任务列表，线程进入等待新任务状态 <br>
		 * 如果当前任务已经进入阻塞状态，则不会产生任何效果
		 */
		public void interruptCurrentTask() {
			this.interrupt = true;
			synchronized (container) {
				container.clearContainer();
			}
		}

		public void setLock(boolean isLock) {
			this.isLocked = isLock;
			if (!isLocked) {
				// 取消锁定则恢复打断状态
				interrupt = false;

				// 取消锁定则唤醒线程，此时可能container为空时再次进入阻塞
				synchronized (container) {
					container.notify();
				}
			}
		}

		/**
		 * 停止线程 ;
		 */
		public void setStop() {
			this.stop = true;
			container.clearContainer();
			interruptCurrentTask();
			interrupt();
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
						setProcessMessage(null, "", "");
					}
				}
			});
		}

		/**
		 * 将matches添加到needLoadAltTransList中
		 * @param matches
		 *            ;
		 */
		private void refreshAltTransCache(List<AltTransBean> matches) {
			if (matches.size() == 0) {
				return;
			}
			if (altTransCacheList.size() == 0) {
				altTransCacheList.addAll(matches);
				return;
			}
			int currIndex = -1;
			String type = matches.get(0).getMatchProps().get("hs:matchType");
			for (int i = 0; i < altTransCacheList.size(); i++) {
				AltTransBean b = altTransCacheList.get(i);
				String t = b.getMatchProps().get("hs:matchType");
				if (t != null && t.equals(type)) {
					if (currIndex == -1) {
						currIndex = i + 1;
					}
					altTransCacheList.remove(i);
					i--;
				}
			}
			if (currIndex > altTransCacheList.size() - 1 || currIndex == -1) {
				currIndex = altTransCacheList.size();
			}

			altTransCacheList.addAll(currIndex, matches);
		}

		private void refreshUI() {
			Display.getDefault().asyncExec(new Runnable() {

				public void run() {
					copyEnable.resetSelection();
					gridTable.removeAll();
					loadData(altTransCacheList);
					int itemSize = gridTable.getItemCount();
					if (itemSize > 0) {
						gridTable.setSelection(0);
					}
					updateStatusInfo(null);
				}
			});
		}
	}

	/**
	 * (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
	 */
	@Override
	public void setFocus() {
		gridTable.setFocus();
	}

	private void setMatchMessage(Image image, String message, String tooltip) {
		infoLabel.setImage(image);
		infoLabel.setText(message);
		infoLabel.setToolTipText(tooltip);
	}

	private void setProcessMessage(Image image, String message, String tooltip) {
		if (!tipLabel.isDisposed()) {
			tipLabel.setImage(image);
			tipLabel.setText(message);
			tipLabel.setToolTipText(tooltip);
			tipLabel.pack();
		}
	}

	@Override
	public void dispose() {
		getSite().getPage().removePostSelectionListener(this);
		tmMatcher.clearResources();

		if (tmImage != null && !tmImage.isDisposed()) {
			tmImage.dispose();
		}
		if (qtImage != null && !qtImage.isDisposed()) {
			qtImage.dispose();
		}
		if (bingImage != null && !bingImage.isDisposed()) {
			bingImage.dispose();
		}
		if (googleImage != null && !googleImage.isDisposed()) {
			googleImage.dispose();
		}
		if (otherImage != null && !otherImage.isDisposed()) {
			otherImage.dispose();
		}
		if (selectedBgColor != null && !selectedBgColor.isDisposed()) {
			selectedBgColor.dispose();
		}
		if (tipLabelImage != null && !tipLabelImage.isDisposed()) {
			tipLabelImage.dispose();
		}
		JFaceResources.getFontRegistry().removeListener(fontChangeListener);

		manualTranslationThread.setStop();
		manualTranslationThread.interrupt();
		if (matcherThread != null) {
			matcherThread.setStop();
			matcherThread.interrupt();
		}
		matcherThread = null;
		sourceColunmCellRenderer.dispose();
		super.dispose();
	}

	public Image getImageByType(String type) {

		// if (type.equals(TmConstants.MATCH_TYPE_TM)) {
		// return tmImage;
		// }

		if (type.equals(TmConstants.MATCH_TYPE_QT)) {
			return qtImage;
		}

		if (type.equals("Bing")) {
			return bingImage;
		}

		if (type.equals("Google")) {
			return googleImage;
		}

		if (type.equals("others")) {
			return otherImage;
		}
		return null;
	}

	class FontPropertyChangeListener implements IPropertyChangeListener {

		public void propertyChange(PropertyChangeEvent event) {
			if (gridTable == null || gridTable.isDisposed()) {
				return;
			}
			String property = event.getProperty();
			if (net.heartsome.cat.ts.ui.Constants.MATCH_VIEWER_TEXT_FONT.equals(property)) {
				Font font = JFaceResources.getFont(net.heartsome.cat.ts.ui.Constants.MATCH_VIEWER_TEXT_FONT);
				sourceColunmCellRenderer.setFont(font);
				targetColumnCellRenderer.setFont(font);
				GridData sTextGd = new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1);
				sourceText.getTextWidget().setFont(font);
				int lineH = sourceText.getTextWidget().getLineHeight() * 2;
				sTextGd.heightHint = lineH;
				sTextGd.minimumHeight = lineH;
				sourceText.getTextWidget().setLayoutData(sTextGd);
				gridTable.redraw();
				sourceText.getTextWidget().getParent().layout();
			}
		}
	}

	/**
	 * 创建视图工具栏的按钮。
	 */
	private void createActions() {
		menuMgr = new MatchViewerBodyMenu(this);
		gridTable.setMenu(menuMgr.getBodyMenu());

		IToolBarManager toolBarManager = getViewSite().getActionBars().getToolBarManager();
		toolBarManager.add(menuMgr.editAction);
		toolBarManager.add(menuMgr.deleteAction);
		toolBarManager.add(menuMgr.acceptMatchAction);
		// toolBarManager.add(menuMgr.acceptMatchPureTextAction);
		menuMgr.updateActionState();
	}

	/**
	 * Sets the enabled state of this View.
	 * @param enabled
	 *            ;
	 */
	private void updateActionState() {
		menuMgr.updateActionState();
	}

	private synchronized void loadData(List<AltTransBean> matches) {
		if (matches == null) {
			return;
		}
		IEditorReference[] editorReferences = getSite().getPage().getEditorReferences();
		if (editorReferences.length == 0) {
			return;
		}
		for (AltTransBean altTransBean : matches) {
			String type = altTransBean.getMatchProps().get("hs:matchType");
			if (type == null || type.equals("")) {
				type = "others";
			}
			String orgin = altTransBean.getMatchOrigin();
			if (orgin == null) {
				// Fixed bug 2258 by Jason 翻译匹配面板不支持非 HS 工具匹配信息的显示
				orgin = "";
			}

			String srcContent = altTransBean.getSrcContent();
			if (srcContent == null || srcContent.equals("")) {
				continue;
			}

			String tgtContent = altTransBean.getTgtContent();
			if (tgtContent == null || tgtContent.equals("")) {
				continue;
			}

			String quality = altTransBean.getMatchProps().get("match-quality").trim();
			if (quality == null) {
				quality = "";
			} else {
				if (!quality.endsWith("%")) {
					quality += "%";
				}
			}

			String changeDate = null;
			String changeid = null;
			String creationDate = null;
			String creationid = null;

			Vector<PropGroupBean> propGroups = altTransBean.getPropGroups();
			StringBuffer toolTipBfTemp = new StringBuffer();
			if (propGroups != null) {
				for (PropGroupBean propGroupBean : propGroups) {
					List<PropBean> propBeans = propGroupBean.getProps();
					for (PropBean bean : propBeans) {
						String ptype = bean.getProptype();
						String pVal = bean.getValue();
						if (ptype.equals("changeDate")) {
							if (pVal != null && !pVal.equals("")) {
								changeDate = DateUtils.formatDateFromUTC(pVal);
							}
						} else if (ptype.equals("changeId")) {
							changeid = pVal;
						} else if (ptype.equals("creationId")) {
							creationid = pVal;
						} else if (ptype.equals("creationDate")) {
							if (pVal != null && !pVal.equals("")) {
								creationDate = DateUtils.formatDateFromUTC(pVal);
							}
						} else {
							toolTipBfTemp.append(ptype).append(" : ").append(pVal).append("\n");
						}
					}
				}
			}
			StringBuffer toolTipBf = new StringBuffer();
			if (creationid != null && !creationid.equals("")) {
				toolTipBf.append(Messages.getString("view.MatchViewPart.info.tooltip.creationId")).append(creationid)
						.append("\n");
			}
			if (creationDate != null && !creationDate.equals("")) {
				toolTipBf.append(Messages.getString("view.MatchViewPart.info.tooltip.creationDate"))
						.append(creationDate).append("\n");
			}
			toolTipBf.append(toolTipBfTemp);

			StringBuffer msgBf = new StringBuffer();
			if (changeDate != null && !changeDate.equals("")) {
				msgBf.append(changeDate);
			}
			if (changeid != null && !changeid.equals("")) {
				if (msgBf.length() != 0) {
					msgBf.append("  |  ");
				}
				msgBf.append(changeid);
			}
			if (orgin != null && !orgin.equals("")) {
				if (msgBf.length() != 0) {
					msgBf.append("  |  ");
				}
				msgBf.append(orgin);
			}
			if (gridTable.isDisposed()) {
				return;
			}
			String toolId = altTransBean.getMatchProps().get("tool-id");
			GridItem gridItem = new GridItem(gridTable, SWT.NONE);
			gridItem.setText(0, srcContent);
			gridItem.setText(1, quality);
			gridItem.setText(2, tgtContent);
			gridItem.setToolTipText(0, toolId);
			gridItem.setToolTipText(1, toolId);
			gridItem.setToolTipText(2, toolId);
			gridItem.setData("info", resetSpecialString(msgBf.toString())); // 保存信息
			gridItem.setData("infoTooltip", resetSpecialString(toolTipBf.toString()));
			gridItem.setData("tgtText", altTransBean.getTgtText()); // 保存目标纯文本
			gridItem.setData("tgtContent", tgtContent); // 保存目标纯文本
			gridItem.setData("matchType", type);
			gridItem.setData("quality", quality.substring(0, quality.lastIndexOf('%')));
			gridItem.setData("typeImage", getImageByType(type));
			gridItem.setData("tmFuzzyInfo", altTransBean.getFuzzyResult());
		}
	}

	private String resetSpecialString(String input) {
		input = input.replaceAll("&lt;", "<"); //$NON-NLS-1$ //$NON-NLS-2$
		input = input.replaceAll("&gt;", ">"); //$NON-NLS-1$ //$NON-NLS-2$
		input = input.replaceAll("&quot;", "\"");
		input = input.replaceAll("&apos;", "'");
		input = input.replaceAll("&amp;", "&"); //$NON-NLS-1$ //$NON-NLS-2$
		return input;
	}

	private TransUnitInfo2TranslationBean getTuInfoBean(TransUnitBean transUnit, XLFHandler handler, String rowId) {
		String srcFullText = transUnit.getSrcContent();
		String srcPureText = transUnit.getSrcText();
		if ("".equals(srcFullText.trim()) || "".equals(srcPureText.trim())) {
			return null;
		}
		sourceColunmCellRenderer.setTuSrcText(srcFullText);
		String srcLanguage = transUnit.getSrcLang();
		String tgtLanguage = transUnit.getTgtLang();
		tgtLanguage = handler.getNatTableColumnName().get("target");
		Language srcLang = LocaleService.getLanguageConfiger().getLanguageByCode(srcLanguage);
		Language tgtLang = LocaleService.getLanguageConfiger().getLanguageByCode(tgtLanguage);
		if (srcLang.isBidi() || tgtLang.isBidi()) {
			gridTable.setOrientation(SWT.RIGHT_TO_LEFT);
		} else {
			gridTable.setOrientation(SWT.LEFT_TO_RIGHT);
		}
		TransUnitInfo2TranslationBean tuInfoBean = new TransUnitInfo2TranslationBean();
		tuInfoBean.setSrcFullText(srcFullText);
		tuInfoBean.setSrcPureText(srcPureText);
		tuInfoBean.setSrcLanguage(srcLanguage);
		tuInfoBean.setTgtLangugage(tgtLanguage);
		int contextSize = tmMatcher.getContextSize();
		if (contextSize != 0) {
			Map<String, String> context = handler.getTransUnitContext(rowId, contextSize);
			tuInfoBean.setPreContext(context.get("x-preContext"));
			tuInfoBean.setNextContext(context.get("x-nextContext"));
		} else {
			tuInfoBean.setPreContext("");
			tuInfoBean.setNextContext("");
		}
		return tuInfoBean;
	}

}

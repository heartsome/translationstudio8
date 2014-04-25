package net.heartsome.cat.ts.ui.xliffeditor.nattable.search.dialog;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.heartsome.cat.common.ui.listener.PartAdapter;
import net.heartsome.cat.ts.core.bean.TransUnitBean;
import net.heartsome.cat.ts.ui.Constants;
import net.heartsome.cat.ts.ui.bean.XliffEditorParameter;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.Activator;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.config.VerticalNatTableConfig;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.editor.HsMultiActiveCellEditor;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.editor.StyledTextCellEditor;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.editor.XLIFFEditorImplWithNatTable;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.layer.LayerUtil;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.resource.Messages;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.search.command.FindReplaceCommand;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.search.coordinate.ActiveCellRegion;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.search.coordinate.CellRegion;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.search.event.FindReplaceEvent;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.search.strategy.ColumnSearchStrategy;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.search.strategy.ICellSearchStrategy;
import net.sourceforge.nattable.NatTable;
import net.sourceforge.nattable.coordinate.PositionCoordinate;
import net.sourceforge.nattable.layer.DataLayer;
import net.sourceforge.nattable.layer.ILayer;
import net.sourceforge.nattable.layer.ILayerListener;
import net.sourceforge.nattable.layer.event.ILayerEvent;
import net.sourceforge.nattable.selection.SelectionLayer;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 查找对话框
 * @author weachy
 * @version
 * @since JDK1.5
 */
public class FindReplaceDialog extends Dialog {

	private static final Logger LOGGER = LoggerFactory.getLogger(FindReplaceDialog.class);

	public static boolean isOpen = false;

	private Combo cmbFind;

	/**  */
	private Combo cmbReplace;

	/** 查找 */
	private Button findButton;

	/** 查找下一个 */
	private Button findNextButton;

	/** 替换 */
	private Button replaceButton;

	/** 替换全部 */
	private Button replaceAllButton;

	/** 大小写敏感按钮 */
	private Button caseSensitiveButton;

	/** 状态信息 */
	private Label statusLabel;

	/** 循环查找 */
	/** burke 修改find/replace界面框 注释 */
	// private Button wrapSearchButton;

	/** 正向查找按钮 */
	private Button forwardButton;

	/** 反向向查找按钮 */
	private Button backwardButton;

	/** 查找源语言按钮 */
	private Button sourceButton;

	/** 查找目标语言按钮 */
	private Button targetButton;

	/** 搜索策略 */
	private ColumnSearchStrategy searchStrategy;

	/** 比较器 */
	private ICellSearchStrategy cellSearchStrategy;

	/** 匹配整词 */
	private Button wholeWordButton;

	/** 模糊搜索 */
	/** burke 修改find/replace界面框 注释 */
	// private Button fuzzySearchButton;

	/** 正则表达式 */
	private Button regExButton;

	private String msg = Messages.getString("dialog.FindReplaceDialog.status1");

	private final int HISTORY_SIZE = 5;

	private List<String> lstFindHistory;

	private List<String> lstReplaceHistory;

	IPartListener partListener = new PartAdapter() {
		@Override
		public void partActivated(IWorkbenchPart part) {
			clearActiveCellRegion(part);
		}

		public void partOpened(IWorkbenchPart part) {
			clearActiveCellRegion(part);
		};

		private void clearActiveCellRegion(IWorkbenchPart part) {
			if (part instanceof XLIFFEditorImplWithNatTable) {
				CellRegion cellRegion = ActiveCellRegion.getActiveCellRegion();
				if (cellRegion == null) {
					return;
				}
				ILayer layer = cellRegion.getPositionCoordinate().getLayer();
				NatTable table = ((XLIFFEditorImplWithNatTable) part).getTable();
				ILayer layer1 = LayerUtil.getLayer(table, layer.getClass());
				if (!layer.equals(layer1)) {
					ActiveCellRegion.setActiveCellRegion(null); // 清空查找结果
				}
			}
		}
	};

	private FindReplaceDialog(Shell shell) {
		super(shell);
		setShellStyle(getShellStyle() ^ SWT.APPLICATION_MODAL | SWT.CLOSE | SWT.MODELESS | SWT.BORDER | SWT.TITLE);
		setBlockOnOpen(false);
		isOpen = true;
		PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().addPartListener(partListener);

		lstFindHistory = new ArrayList<String>(HISTORY_SIZE - 1);
		lstReplaceHistory = new ArrayList<String>(HISTORY_SIZE - 1);
	}

	public static FindReplaceDialog createDialog(Shell shell) {
		return new FindReplaceDialog(shell);
	}

	public void setSearchStrategy(ColumnSearchStrategy searchStrategy, ICellSearchStrategy cellSearchStrategy) {
		this.searchStrategy = searchStrategy;
		this.cellSearchStrategy = cellSearchStrategy;
		// ActiveCellRegion.setActiveCellRegion(null);
	}

	@Override
	public void create() {
		super.create();
		isOpen = true;
		getShell().setText(Messages.getString("dialog.FindReplaceDialog.Title"));
		updateCombo(cmbFind, lstFindHistory);
		updateCombo(cmbReplace, lstReplaceHistory);
		// 打开了查找替换框后，判断查找输入框中是否存在查找替换内容，判断查找，查找下一个，替换，替换所有按钮的可用性
		setEnable();
	}

	@Override
	protected boolean isResizable() {
		return true;
	}

	protected Point getInitialSize() {
		// Point initialSize = super.getInitialSize();
		// Point minSize = getShell().computeSize(SWT.DEFAULT, SWT.DEFAULT);

		// if (initialSize.x < minSize.x)
		// return minSize;
		// return initialSize;
		return new Point(350, 435);
	}

	protected IDialogSettings getDialogBoundsSettings() {
		String sectionName = getClass().getName() + "_dialogBounds"; //$NON-NLS-1$
		IDialogSettings settings = Activator.getDefault().getDialogSettings();
		IDialogSettings section = settings.getSection(sectionName);
		if (section == null)
			section = settings.addNewSection(sectionName);
		return section;
	}

	@Override
	public boolean close() {
		ActiveCellRegion.setActiveCellRegion(null); // 清空查找结果
		PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().removePartListener(partListener);
		isOpen = false;
		writeDialogSettings();
		return super.close();
	}

	@Override
	protected void buttonPressed(int buttonID) {
		if (buttonID == IDialogConstants.CLOSE_ID) {
			close();
		}
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		return super.createDialogArea(parent);
	}

	@Override
	protected Control createContents(final Composite parent) {

		final Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(1, true));
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(composite);

		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, false).applyTo(createInputPanel(composite));

		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(createConfigPanel(composite));

		Composite buttonPanel = createButtonSection(composite);
		GridDataFactory.swtDefaults().align(SWT.RIGHT, SWT.BOTTOM).grab(true, false).applyTo(buttonPanel);

		Composite statusBar = createStatusAndCloseButton(composite);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.BOTTOM).grab(true, false).applyTo(statusBar);

		readDialogSettings();

		return composite;
	}

	/**
	 * Creates the options configuration section of the find replace dialog.
	 * @param parent
	 *            the parent composite
	 * @return the options configuration section
	 */
	private Composite createConfigPanel(Composite parent) {
		Composite panel = new Composite(parent, SWT.NONE);
		panel.setLayout(new GridLayout(1, true));

		Composite rangePanel = createRangePanel(panel);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(rangePanel);

		Composite optionPanel = createOptionsPanel(panel);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(optionPanel);

		return panel;
	}

	private Composite createButtonSection(Composite composite) {
		Composite panel = new Composite(composite, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = -2; // this is intended
		panel.setLayout(layout);

		findButton = createButton(panel, IDialogConstants.CLIENT_ID,
				Messages.getString("dialog.FindReplaceDialog.findButton"), false);
		GridDataFactory.swtDefaults().align(SWT.RIGHT, SWT.BOTTOM).grab(false, false).hint(95, SWT.DEFAULT)
				.applyTo(findButton);
		findButton.setEnabled(false);

		findButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				doFind();
				updateFindHistory();
			}
		});

		findNextButton = createButton(panel, IDialogConstants.CLIENT_ID,
				Messages.getString("dialog.FindReplaceDialog.findNextButton"), false);
		GridDataFactory.swtDefaults().align(SWT.RIGHT, SWT.BOTTOM).grab(false, false).hint(95, SWT.DEFAULT)
				.applyTo(findNextButton);
		findNextButton.setEnabled(false);
		findNextButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				doFindNext();
				updateFindHistory();
			}
		});

		replaceButton = createButton(panel, IDialogConstants.CLIENT_ID,
				Messages.getString("dialog.FindReplaceDialog.replaceButton"), false);
		GridDataFactory.swtDefaults().align(SWT.RIGHT, SWT.BOTTOM).grab(false, false).hint(95, SWT.DEFAULT)
				.applyTo(replaceButton);
		replaceButton.setEnabled(false);
		replaceButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				doReplace();
				updateFindAndReplaceHistory();
			}
		});

		replaceAllButton = createButton(panel, IDialogConstants.CLIENT_ID,
				Messages.getString("dialog.FindReplaceDialog.replaceAllButton"), false);
		GridDataFactory.swtDefaults().align(SWT.RIGHT, SWT.BOTTOM).grab(false, false).hint(95, SWT.DEFAULT)
				.applyTo(replaceAllButton);
		replaceAllButton.setEnabled(false);
		replaceAllButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				doReplaceAll();
				updateFindAndReplaceHistory();
			}
		});

		getShell().setDefaultButton(findNextButton); // 设置默认按钮

		return panel;
	}

	/**
	 * Creates the status and close section of the dialog.
	 * @param parent
	 *            the parent composite
	 * @return the status and close button
	 */
	private Composite createStatusAndCloseButton(Composite parent) {
		statusLabel = new Label(parent, SWT.LEFT);
		statusLabel.setForeground(statusLabel.getDisplay().getSystemColor(SWT.COLOR_RED));
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(statusLabel);

		Composite panel = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout(0, false);
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		panel.setLayout(layout);
		panel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Button closeButton = createButton(panel, IDialogConstants.CLOSE_ID, IDialogConstants.CLOSE_LABEL, false);
		GridDataFactory.fillDefaults().align(SWT.RIGHT, SWT.BOTTOM).grab(true, false).hint(90, SWT.DEFAULT)
				.applyTo(closeButton);

		return parent;
	}

	private Composite createInputPanel(final Composite composite) {
		final Composite row = new Composite(composite, SWT.NONE);
		row.setLayout(new GridLayout(2, false));

		final Label findLabel = new Label(row, SWT.NONE);
		findLabel.setText(Messages.getString("dialog.FindReplaceDialog.findLabel"));
		GridDataFactory.fillDefaults().align(SWT.RIGHT, SWT.CENTER).applyTo(findLabel);

		cmbFind = new Combo(row, SWT.DROP_DOWN | SWT.BORDER);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(cmbFind);

		cmbFind.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				/*
				 * boolean enabled = findText.getText().length() > 0; findButton.setEnabled(enabled);
				 * findNextButton.setEnabled(enabled); replaceAllButton.setEnabled(!sourceButton.getSelection() &&
				 * enabled); if (!enabled) { replaceButton.setEnabled(false); }
				 */
				setEnable();
			}
		});
		cmbFind.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				if (findButton.isEnabled()) {
					doFindNext();
				}
			}
		});

		Label replaceWithLabel = new Label(row, SWT.NONE);
		replaceWithLabel.setText(Messages.getString("dialog.FindReplaceDialog.replaceWithLabel"));
		GridDataFactory.fillDefaults().align(SWT.RIGHT, SWT.CENTER).applyTo(replaceWithLabel);

		cmbReplace = new Combo(row, SWT.DROP_DOWN | SWT.BORDER);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(cmbReplace);

		return row;
	}

	/**
	 * 范围面板
	 * @param composite
	 * @return ;
	 */
	private Composite createRangePanel(Composite composite) {
		final Composite row = new Composite(composite, SWT.NONE);
		row.setLayout(new GridLayout(2, true));
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(row);

		final Group directionGroup = new Group(row, SWT.SHADOW_ETCHED_IN);
		directionGroup.setText(Messages.getString("dialog.FindReplaceDialog.directionGroup"));
		directionGroup.setLayout(new GridLayout(1, true));
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(directionGroup);

		forwardButton = new Button(directionGroup, SWT.RADIO);
		forwardButton.setText(Messages.getString("dialog.FindReplaceDialog.forwardButton"));
		forwardButton.setSelection(true);
		GridDataFactory.fillDefaults().align(SWT.LEFT, SWT.CENTER).grab(false, false).applyTo(forwardButton);

		backwardButton = new Button(directionGroup, SWT.RADIO);
		backwardButton.setText(Messages.getString("dialog.FindReplaceDialog.backwardButton"));
		GridDataFactory.fillDefaults().align(SWT.LEFT, SWT.CENTER).grab(false, false).applyTo(backwardButton);

		final Group rangeGroup = new Group(row, SWT.SHADOW_ETCHED_IN);
		rangeGroup.setText(Messages.getString("dialog.FindReplaceDialog.rangeGroup"));
		rangeGroup.setLayout(new GridLayout(1, true));
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(rangeGroup);

		/** 搜索范围：源语言 */
		sourceButton = new Button(rangeGroup, SWT.RADIO);
		sourceButton.setText(Messages.getString("dialog.FindReplaceDialog.sourceButton"));
		sourceButton.setSelection(true);
		sourceButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				XLIFFEditorImplWithNatTable editor = XLIFFEditorImplWithNatTable.getCurrent();
				if (editor != null) {
					replaceButton.setEnabled(false);
					replaceAllButton.setEnabled(false);
				}
			}
		});
		GridDataFactory.fillDefaults().align(SWT.LEFT, SWT.CENTER).grab(false, false).applyTo(sourceButton);

		targetButton = new Button(rangeGroup, SWT.RADIO);
		targetButton.setText(Messages.getString("dialog.FindReplaceDialog.targetButton"));
		targetButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				XLIFFEditorImplWithNatTable editor = XLIFFEditorImplWithNatTable.getCurrent();
				if (editor != null) {
					boolean enabled = cmbFind.getText().length() > 0;
					replaceAllButton.setEnabled(enabled);
				}
			}
		});
		GridDataFactory.fillDefaults().align(SWT.LEFT, SWT.CENTER).grab(false, false).applyTo(targetButton);

		return row;
	}

	private Composite createOptionsPanel(final Composite composite) {
		final Group optionsGroup = new Group(composite, SWT.SHADOW_ETCHED_IN);
		optionsGroup.setText(Messages.getString("dialog.FindReplaceDialog.optionsGroup"));
		final GridLayout gridLayout = new GridLayout(2, true);
		optionsGroup.setLayout(gridLayout);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(optionsGroup);
		// optionsGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		caseSensitiveButton = new Button(optionsGroup, SWT.CHECK);
		caseSensitiveButton.setText(Messages.getString("dialog.FindReplaceDialog.caseSensitiveButton"));
		GridDataFactory.fillDefaults().align(SWT.LEFT, SWT.CENTER).grab(false, false).applyTo(caseSensitiveButton);
		/** burke 修改find/replace界面框 注释 */
		/*
		 * wrapSearchButton = new Button(optionsGroup, SWT.CHECK); wrapSearchButton.setText("&Wrap Search");
		 * wrapSearchButton.setSelection(true); GridDataFactory.fillDefaults().align(SWT.LEFT, SWT.CENTER).grab(false,
		 * false).applyTo(wrapSearchButton);
		 */

		wholeWordButton = new Button(optionsGroup, SWT.CHECK);
		wholeWordButton.setText(Messages.getString("dialog.FindReplaceDialog.wholeWordButton"));
		GridDataFactory.fillDefaults().align(SWT.LEFT, SWT.CENTER).grab(false, false).applyTo(wholeWordButton);
		/** burke 修改find/replace界面框 注释 */
		/*
		 * fuzzySearchButton = new Button(optionsGroup, SWT.CHECK); fuzzySearchButton.setText("Fu&zzy Search");
		 * GridDataFactory.fillDefaults().align(SWT.LEFT, SWT.CENTER).grab(false, false).applyTo(fuzzySearchButton);
		 */

		regExButton = new Button(optionsGroup, SWT.CHECK);
		regExButton.setText(Messages.getString("dialog.FindReplaceDialog.regExButton"));
		regExButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (regExButton.getSelection()) {
					wholeWordButton.setEnabled(false);
					/** burke 修改find/replace界面框 注释 */
					// fuzzySearchButton.setEnabled(false);
				} else {
					wholeWordButton.setEnabled(true);
					/** burke 修改find/replace界面框 注释 */
					// fuzzySearchButton.setEnabled(true);
				}
			}
		});
		GridDataFactory.fillDefaults().align(SWT.LEFT, SWT.CENTER).grab(false, false).span(2, 1).applyTo(regExButton);

		return optionsGroup;
	}

	private CellRegion searchResultCellRegion = null;

	private CellRegion find(final int startingRowPosition, final int startOffset) {
		// 查找前选提交编辑模式的单元格
		BusyIndicator.showWhile(super.getShell().getDisplay(), new Runnable() {

			public void run() {
				XLIFFEditorImplWithNatTable editor = XLIFFEditorImplWithNatTable.getCurrent();
				if (editor == null) {
					return;
				}

				NatTable natTable = editor.getTable();

				searchResultCellRegion = null;
				statusLabel.setText("");

				boolean searchForward = forwardButton.getSelection();
				boolean searchSource = sourceButton.getSelection();
				boolean caseSensitive = caseSensitiveButton.getSelection();
				/** burke 修改find/replace界面框 注释 使得find/replace默认为循环查找 */
				// boolean wrapSearch = wrapSearchButton.getSelection();
				boolean wrapSearch = true;
				boolean wholeWord = wholeWordButton.getEnabled() && wholeWordButton.getSelection();
				boolean regExSearch = regExButton.getEnabled() && regExButton.getSelection();
				/** burke 修改find/replace界面框 注释 */
				// boolean fuzzySearch = fuzzySearchButton.getSelection();

				if (searchSource) {
					int columnPosition = LayerUtil.getColumnPositionByIndex(natTable, editor.getSrcColumnIndex());
					searchStrategy.setColumnPositions(new int[] { columnPosition });
				} else {
					int columnPosition = LayerUtil.getColumnPositionByIndex(natTable, editor.getTgtColumnIndex());
					searchStrategy.setColumnPositions(new int[] { columnPosition });
				}
				searchStrategy.setStartingRowPosition(startingRowPosition);
			
				cellSearchStrategy.init(searchForward, caseSensitive, wholeWord, regExSearch, startOffset);

				final FindReplaceCommand command = new FindReplaceCommand(cmbFind.getText(), natTable, searchStrategy,
						cellSearchStrategy);	

				final ILayerListener searchEventListener = initSearchEventListener();
				command.setSearchEventListener(searchEventListener);

				try {
					// Fire command
					natTable.doCommand(command);
					// 如果未找到
					if (searchResultCellRegion == null) {
						if (wrapSearch) {
							if (forwardButton.getSelection()) {
								int rowPositionFlag = 0;
								// 解决垂直布局执行查找下一个或者替换所有时，如果没有匹配，界面会卡住一会儿并且后台会报异常的问题
								if (!editor.isHorizontalLayout()) {
									rowPositionFlag = 1;
								}
								if (startingRowPosition > rowPositionFlag) {
									int rowPosition = 0; // 默认从第一行（索引为 0）开始
									if (!editor.isHorizontalLayout()) { // 如果是垂直布局并且是
										if (!sourceButton.getSelection()) {
											rowPosition = 1;
										}
									}
									find(rowPosition, 0);
								} else {
									refreshMsgAndTable();
								}
							} else {
								DataLayer dataLayer = LayerUtil.getLayer(natTable, DataLayer.class);
								int lastRowPosition = dataLayer.getRowCount() - 1;
								SelectionLayer selectionLayer = LayerUtil.getLayer(natTable, SelectionLayer.class);
								lastRowPosition = LayerUtil.convertRowPosition(dataLayer, lastRowPosition,
										selectionLayer);

								if (startingRowPosition < lastRowPosition) {
									find(lastRowPosition, -1);
								} else {
									refreshMsgAndTable();
								}
							}
						} else {
							refreshMsgAndTable();
						}
					}

				} finally {
					command.getContext().removeLayerListener(searchEventListener);
				}
			}

			private ILayerListener initSearchEventListener() {
				// Register event listener
				final ILayerListener searchEventListener = new ILayerListener() {
					public void handleLayerEvent(ILayerEvent event) {
						if (event instanceof FindReplaceEvent) {
							FindReplaceEvent searchEvent = (FindReplaceEvent) event;
							searchResultCellRegion = searchEvent.getCellRegion();
							if (searchResultCellRegion != null) {
								ActiveCellRegion.setActiveCellRegion(searchResultCellRegion);
							}
						}
					}
				};
				return searchEventListener;
			}
		});
		return searchResultCellRegion;
	}

	private boolean doFind() {
		CellRegion cellRegion;
		XLIFFEditorImplWithNatTable editor = XLIFFEditorImplWithNatTable.getCurrent();
		if (editor == null) {
			return false;
		}
		if (forwardButton.getSelection()) {
			if (editor.isHorizontalLayout()) {
				cellRegion = find(0, 0);
			} else {
				// 解决在垂直布局查找 Source 时，如果 Source 没有匹配而 Target 有匹配，会找到 Target 中的匹配文本
				if (sourceButton.getSelection()) {
					cellRegion = find(0, 0);
				} else {
					cellRegion = find(1, 0);
				}
			}
		} else {
			NatTable natTable = editor.getTable();
			int sourceRowPosition = natTable.getRowCount() - 1;
			int row = LayerUtil.getLowerLayerRowPosition(natTable, sourceRowPosition, SelectionLayer.class);

			if (!editor.isHorizontalLayout()) {
				row *= VerticalNatTableConfig.ROW_SPAN;
				if (!sourceButton.getSelection()) {
					row++;
				}
			}
			cellRegion = find(row, -1);
		}
		replaceButton.setEnabled(!sourceButton.getSelection() && cellRegion != null);
		if (cellRegion == null) {
			refreshMsgAndTable();
		} 
		return cellRegion != null;
	}

	/**
	 * 查找下一个 ;
	 * @return
	 */
	private boolean doFindNext() {
		XLIFFEditorImplWithNatTable editor = XLIFFEditorImplWithNatTable.getCurrent();
		if (editor == null) {
			return false;
		}
		int[] selectedRows = editor.getSelectedRows();
		int startingRowPosition;
		if (selectedRows.length > 0) {
			Arrays.sort(selectedRows);
			if (forwardButton.getSelection()) {
				startingRowPosition = selectedRows[selectedRows.length - 1] /* 最大行数 */; // 从当前选中行中最大的行开始找。
			} else {
				startingRowPosition = selectedRows[0] /* 最小行数 */; // 从当前选中行中最大的行开始找。
			}
			if (!editor.isHorizontalLayout()) {
				startingRowPosition *= VerticalNatTableConfig.ROW_SPAN;
				if (!sourceButton.getSelection()) {
					startingRowPosition++;
				}
			}
			int startOffset;
			CellRegion cellRegion = ActiveCellRegion.getActiveCellRegion();
			if (cellRegion == null || cellRegion.getPositionCoordinate().getRowPosition() != startingRowPosition) { // 起始行不一致
				if (forwardButton.getSelection()) {
					startOffset = 0;
				} else {
					startOffset = -1;
				}
			} else {
				PositionCoordinate coordinate = cellRegion.getPositionCoordinate();
				int columnIndex = coordinate.getLayer().getColumnIndexByPosition(coordinate.getColumnPosition()); // 得到上次查找的列
				if (columnIndex != (sourceButton.getSelection() ? editor.getSrcColumnIndex() : editor
						.getTgtColumnIndex())) {// 如果所查找的列改变了，赋为初始值
					if (forwardButton.getSelection()) {
						startOffset = 0;
					} else {
						startOffset = -1;
					}
				} else {
					if (forwardButton.getSelection()) {
						startOffset = cellRegion.getRegion().getOffset() + cellRegion.getRegion().getLength();
					} else {
						startOffset = cellRegion.getRegion().getOffset() - 1;
						if (startOffset == -1) {
							// 解决在垂直布局时，选择向后查找在查找到某一行后会返回到最后一行继续查找的问题。
							if (editor.isHorizontalLayout()) {
								startingRowPosition--;
							} else {
								startingRowPosition -= 2;
							}
						}
					}
				}
			}
			cellRegion = find(startingRowPosition, startOffset);
			replaceButton.setEnabled(!sourceButton.getSelection() && cellRegion != null);
			if (cellRegion == null) {
				refreshMsgAndTable();
			}
			return cellRegion != null;
		} else {
			return doFind();
		}
	}

	/**
	 * 替换 ;
	 */
	private void doReplace() {
		CellRegion activeCellRegion = ActiveCellRegion.getActiveCellRegion();
		if (activeCellRegion == null) {
			return;
		}
		StyledTextCellEditor cellEditor = HsMultiActiveCellEditor.getTargetStyledEditor();
		if(cellEditor != null){
			StyledText text = cellEditor.getSegmentViewer().getTextWidget();
			String sleText = text.getSelectionText();
			String findStr = cmbFind.getText();
			if (XliffEditorParameter.getInstance().isShowNonpirnttingCharacter()) {
				findStr = findStr.replaceAll("\\n", Constants.LINE_SEPARATOR_CHARACTER + "\n");
				findStr = findStr.replaceAll("\\t", Constants.TAB_CHARACTER + "\u200B");
				findStr = findStr.replaceAll(" ", Constants.SPACE_CHARACTER + "\u200B");
			}
			if( sleText != null  && sleText.toLowerCase().equals(findStr.toLowerCase())){
				Point p = text.getSelection();
				text.replaceTextRange(p.x, p.y - p.x, cmbReplace.getText());
			}
		}
	}

	/**
	 * 替换全部 ;
	 */
	private void doReplaceAll() {
		XLIFFEditorImplWithNatTable editor = XLIFFEditorImplWithNatTable.getCurrent();
		if (editor == null) {
			return;
		}

		CellRegion cellRegion = null;
		if (editor.isHorizontalLayout()) {
			cellRegion = find(0, 0);
		} else {
			cellRegion = find(1, 0);
		}

		if (cellRegion == null) { // 无查找结果
			return;
		}
		
		boolean forward = forwardButton.getSelection();
		if(!forward){
			forwardButton.setSelection(true);
		}
		
		int firstRowPosition = cellRegion.getPositionCoordinate().getRowPosition();
		HashMap<String, String> segments = new HashMap<String, String>();
		int count = 0;
		String findStr = cmbFind.getText();
		String replaceStr = cmbReplace.getText();
		do {
			PositionCoordinate coordinate = cellRegion.getPositionCoordinate();
			int rowPosition = coordinate.rowPosition;
			int columnPosition = coordinate.columnPosition;
			int rowIndex = coordinate.getLayer().getRowIndexByPosition(rowPosition);
			if (!editor.isHorizontalLayout()) {
				rowIndex = rowIndex / VerticalNatTableConfig.ROW_SPAN;
			}

			// 判断锁定
			TransUnitBean transUnit = editor.getRowTransUnitBean(rowIndex);
			String translate = transUnit.getTuProps().get("translate");
			if (translate != null && "no".equalsIgnoreCase(translate)) {
				rowPosition++;
				cellRegion = find(rowPosition, 0);
				continue;
			}
			String cellValue = (String) coordinate.getLayer().getDataValueByPosition(columnPosition, rowPosition);
			StringBuffer cellValueBf = new StringBuffer(cellValue);
			int start = cellValue.toUpperCase().indexOf(findStr.toUpperCase());
			while (start != -1) {
				cellValueBf.replace(start, start + findStr.length(), replaceStr);
				start = cellValueBf.indexOf(findStr, start);
				count++;
			}
			segments.put(editor.getXLFHandler().getRowId(rowIndex), cellValueBf.toString());
			rowPosition++;
			if(!editor.isHorizontalLayout()){
				rowPosition++;
			}
			cellRegion = find(rowPosition, 0);
		} while (cellRegion.getPositionCoordinate().getRowPosition() != firstRowPosition);
		if(!forward){
			forwardButton.setSelection(false);
			backwardButton.setSelection(true);
		}
		int columnIndex = 0;
		if (sourceButton.getSelection()) {
			columnIndex = editor.getSrcColumnIndex();
		} else {
			columnIndex = editor.getTgtColumnIndex();
		}
		try {
			editor.updateSegments(segments, columnIndex, null, null);
		} catch (ExecutionException e) {
			LOGGER.error(Messages.getString("dialog.FindReplaceDialog.logger1"), e);
		}
		String msg = Messages.getString("dialog.FindReplaceDialog.status3");
		statusLabel.setText(MessageFormat.format(msg, count));
		ActiveCellRegion.setActiveCellRegion(null);
	}

	/**
	 * 刷新提示信息和 Nattable
	 */
	private void refreshMsgAndTable() {
		// 更换条件查找，当查找不到结果时，清除之前标记的红色文本
		ActiveCellRegion.setActiveCellRegion(null);
		statusLabel.setText(msg);
		XLIFFEditorImplWithNatTable editor = XLIFFEditorImplWithNatTable.getCurrent();
		editor.setFocus();
		if (editor != null) {
			editor.refresh();
		}
	}

	// private String replaceText(String initValue, String text, String replaceText, List<IRegion> regions) {
	// IInnerTagFactory innerTagFactory = new XliffInnerTagFactory(initValue, new PlaceHolderNormalModeBuilder());
	// List<InnerTagBean> innerTagBeans = innerTagFactory.getInnerTagBeans();
	// int wave = 0;
	// StringBuffer sb = new StringBuffer(initValue);
	// // 解决向后查找替换时替换位置会出现错误的问题。
	// if (!forwardButton.getSelection()) {
	// Collections.reverse(regions);
	// }
	// StringBuffer sbText = new StringBuffer(text);
	// for (IRegion region : regions) {
	// // 由于可能含有标记，因此先找到 region 的实际位置
	// int caretOffset = region.getOffset() + wave;
	// int offset = caretOffset;
	// Matcher matcher = PATTERN.matcher(sbText);
	// int index = 0;
	// while (matcher.find()) {
	// String placeHolder = matcher.group();
	// int start = matcher.start();
	// if (start >= offset) {
	// break;
	// }
	// caretOffset += innerTagBeans.get(index++).getContent().replaceAll("&amp;", "&").length() - placeHolder.length();
	// }
	// sb.replace(caretOffset, caretOffset + region.getLength(), replaceText);
	// int start = region.getOffset() + wave;
	// sbText.replace(start, start + region.getLength(), replaceText);
	// wave += replaceText.length() - region.getLength();
	// }
	// return sb.toString();
	// }

	/**
	 * 通过每次打开查找替换框或者修改查找框中内容，判断查找，查找下一个，替换，替换所有按钮的可用性 修改查找替换BUG时添加 burke
	 */
	private void setEnable() {
		boolean enabled = cmbFind.getText().length() > 0;
		findButton.setEnabled(enabled);
		findNextButton.setEnabled(enabled);
		replaceAllButton.setEnabled(!sourceButton.getSelection() && enabled);
		if (!enabled) {
			replaceButton.setEnabled(false);
		}
	}

	public void setSearchText(String text) {
		if (cmbFind != null && !cmbFind.isDisposed()) {
			if (!text.equals("")) {
				cmbFind.setText(text);
			} else if (lstFindHistory != null && lstFindHistory.size() > 0){
				cmbFind.setText(lstFindHistory.get(0));
			}
			cmbFind.setSelection(new Point(0, cmbFind.getText().length()));
		}
	}

	private void readDialogSettings() {
		IDialogSettings ids = getDialogSettings();
		boolean blnDirection = ids.getBoolean("nattable.FindReplaceDialog.direction");
		forwardButton.setSelection(!blnDirection);
		backwardButton.setSelection(blnDirection);
		boolean blnRange = ids.getBoolean("nattable.FindReplaceDialog.range");
		sourceButton.setSelection(!blnRange);
		targetButton.setSelection(blnRange);

		caseSensitiveButton.setSelection(ids.getBoolean("nattable.FindReplaceDialog.caseSensitive"));
		wholeWordButton.setSelection(ids.getBoolean("nattable.FindReplaceDialog.wholeWord"));
		regExButton.setSelection(ids.getBoolean("nattable.FindReplaceDialog.regEx"));

		String[] arrFindHistory = ids.getArray("nattable.FindReplaceDialog.findHistory");
		if (arrFindHistory != null) {
			lstFindHistory.clear();
			for (int i = 0; i < arrFindHistory.length; i++) {
				lstFindHistory.add(arrFindHistory[i]);
			}
		}

		String[] arrReplaceHistory = ids.getArray("nattable.FindReplaceDialog.replaceHistory");
		if (arrReplaceHistory != null) {
			lstReplaceHistory.clear();
			for (int i = 0; i < arrReplaceHistory.length; i++) {
				lstReplaceHistory.add(arrReplaceHistory[i]);
			}
		}
	}

	private void writeDialogSettings() {
		IDialogSettings ids = getDialogSettings();
		ids.put("nattable.FindReplaceDialog.direction", backwardButton.getSelection());
		ids.put("nattable.FindReplaceDialog.range", targetButton.getSelection());
		ids.put("nattable.FindReplaceDialog.caseSensitive", caseSensitiveButton.getSelection());
		ids.put("nattable.FindReplaceDialog.wholeWord", wholeWordButton.getSelection());
		ids.put("nattable.FindReplaceDialog.regEx", regExButton.getSelection());
		if (okToUse(cmbFind)) {
			String findString = cmbFind.getText();
			if (findString.length() > 0) {
				lstFindHistory.add(0, findString);
			}
			writeHistory(lstFindHistory, ids, "nattable.FindReplaceDialog.findHistory");
		}
		if (okToUse(cmbReplace)) {
			String replaceString = cmbReplace.getText();
			if (replaceString.length() > 0) {
				lstReplaceHistory.add(0, replaceString);
			}
			writeHistory(lstReplaceHistory, ids, "nattable.FindReplaceDialog.replaceHistory");
		}
	}
	
	/**
	 * Writes the given history into the given dialog store.
	 *
	 * @param history the history
	 * @param settings the dialog settings
	 * @param sectionName the section name
	 * @since 3.2
	 */
	private void writeHistory(List<String> history, IDialogSettings settings, String sectionName) {
		int itemCount= history.size();
		Set<String> distinctItems= new HashSet<String>(itemCount);
		for (int i= 0; i < itemCount; i++) {
			String item= (String)history.get(i);
			if (distinctItems.contains(item)) {
				history.remove(i--);
				itemCount--;
			} else {
				distinctItems.add(item);
			}
		}

		while (history.size() > 8) {
			history.remove(8);
		}

		String[] names= new String[history.size()];
		history.toArray(names);
		settings.put(sectionName, names);

	}

	private IDialogSettings getDialogSettings() {
		IDialogSettings settings = Activator.getDefault().getDialogSettings();
		IDialogSettings fDialogSettings = settings.getSection(getClass().getName());
		if (fDialogSettings == null)
			fDialogSettings = settings.addNewSection(getClass().getName());
		return fDialogSettings;
	}

	/**
	 * Updates the given combo with the given content.
	 * @param combo
	 *            combo to be updated
	 * @param content
	 *            to be put into the combo
	 */
	private void updateCombo(Combo combo, List<String> content) {
		combo.removeAll();
		for (int i = 0; i < content.size(); i++) {
			combo.add(content.get(i));
		}
	}

	/**
	 * Returns <code>true</code> if control can be used.
	 * @param control
	 *            the control to be checked
	 * @return <code>true</code> if control can be used
	 */
	private boolean okToUse(Control control) {
		return control != null && !control.isDisposed();
	}

	/**
	 * Called after executed find/replace action to update the history.
	 */
	private void updateFindAndReplaceHistory() {
		updateFindHistory();
		if (okToUse(cmbReplace)) {
			updateHistory(cmbReplace, lstReplaceHistory);
		}

	}

	/**
	 * Called after executed find action to update the history.
	 */
	private void updateFindHistory() {
		if (okToUse(cmbFind)) {
			updateHistory(cmbFind, lstFindHistory);
		}
	}

	/**
	 * Updates the combo with the history.
	 * @param combo
	 *            to be updated
	 * @param history
	 *            to be put into the combo
	 */
	private void updateHistory(Combo combo, List<String> history) {
		String findString = combo.getText();
		int index = history.indexOf(findString);
		if (index != 0) {
			if (index != -1) {
				history.remove(index);
			}
			history.add(0, findString);
			Point selection = combo.getSelection();
			updateCombo(combo, history);
			combo.setText(findString);
			combo.setSelection(selection);
		}
	}
}
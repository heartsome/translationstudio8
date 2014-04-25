package net.heartsome.cat.database.ui.tb.dialog;

import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import net.heartsome.cat.common.bean.DatabaseModelBean;
import net.heartsome.cat.common.bean.MetaData;
import net.heartsome.cat.common.core.Constant;
import net.heartsome.cat.common.util.CommonFunction;
import net.heartsome.cat.database.DBOperator;
import net.heartsome.cat.database.service.DatabaseService;
import net.heartsome.cat.database.ui.tb.Activator;
import net.heartsome.cat.database.ui.tb.resource.Messages;
import net.heartsome.cat.ts.core.bean.TransUnitBean;
import net.heartsome.cat.ts.core.file.ProjectConfiger;
import net.heartsome.cat.ts.core.file.ProjectConfigerFactory;
import net.heartsome.cat.ts.ui.editors.IXliffEditor;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TrayDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.nebula.widgets.grid.Grid;
import org.eclipse.nebula.widgets.grid.GridColumn;
import org.eclipse.nebula.widgets.grid.GridItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 搜索术语对话框
 * @author peason
 * @version
 * @since JDK1.6
 */
public class TermBaseSearchDialog extends TrayDialog {

	private static final Logger LOGGER = LoggerFactory.getLogger(TermBaseSearchDialog.class.getName());

	/** 当前项目术语库中的语言集合 */
	private ArrayList<String> lstLangs = new ArrayList<String>();

	/** 当前项目的记忆库集合 */
	private List<DatabaseModelBean> lstDatabase = new ArrayList<DatabaseModelBean>();

	/** 当前文件的源语言 */
	private String strSrcLang;

	/** 当前文件的目标语言 */
	private String strTgtLang;

	/** 要搜索的文本 */
	private String strSearchText;

	/** 搜索下拉框 */
	private Combo cmbSearch;

	/** 搜索按钮 */
	private Button btnSearch;

	/** 是否区分大小写复选框 */
	private Button btnIsCaseSensitive;

	/** 是否应用正则表达式复选框 */
	private Button btnApplyRegularExpression;

	/** 是否忽略标记复选框 */
	private Button btnIsIgnoreMark;

	/** 术语相似度设置按钮 */
	private Spinner spiMatchQuality;

	/** 术语库下拉框 */
	private Combo cmbDatabase;

	private Grid grid;

	/** 选择语言按钮 */
	private Button btnSelectLang;

	/** 选择语言菜单 */
	private Menu menu;

	private GridColumn columnSrcLang;

	private TBSearchCellRenderer srcCellRenderer = new TBSearchCellRenderer();

	// private ToolBar selLangBar;

	private final int HISTORY_SIZE = 5;

	private List<String> lstSearchHistory;

//	private IPreferenceStore preferenceStore;
	/**
	 * 构造方法
	 * @param parentShell
	 * @param project
	 *            当前项目
	 * @param strSrcLang
	 *            当前文件的源语言
	 * @param strTgtLang
	 *            当前文件的目标语言
	 * @param strSearchText
	 *            搜索文本
	 */
	public TermBaseSearchDialog(Shell parentShell, IProject project, String strSrcLang, String strTgtLang,
			String strSearchText) {
		super(parentShell);
		this.strSrcLang = strSrcLang;
		this.strTgtLang = strTgtLang;
		this.strSearchText = strSearchText;
	//	this.preferenceStore=Activator.getDefault().getPreferenceStore();
		ProjectConfiger projectConfig = ProjectConfigerFactory.getProjectConfiger(project);
		lstDatabase = projectConfig.getTermBaseDbs(false);
		setHelpAvailable(true);
		setBlockOnOpen(false);
		lstSearchHistory = new ArrayList<String>(HISTORY_SIZE - 1);
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(Messages.getString("dialog.TermBaseSearchDialog.title"));
	}

	/**
	 * 添加帮助按钮 robert 2012-09-06
	 */
	@Override
	protected Control createHelpControl(Composite parent) {
		// ROBERTHELP 搜索术语
		String language = CommonFunction.getSystemLanguage();
		final String helpUrl = MessageFormat.format(
				"/net.heartsome.cat.ts.ui.help/html/{0}/ch05s04.html#search-terminology", language);
		Image helpImage = JFaceResources.getImage(DLG_IMG_HELP);
		ToolBar toolBar = new ToolBar(parent, SWT.FLAT | SWT.NO_FOCUS);
		((GridLayout) parent.getLayout()).numColumns++;
		toolBar.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_CENTER));
		final Cursor cursor = new Cursor(parent.getDisplay(), SWT.CURSOR_HAND);
		toolBar.setCursor(cursor);
		toolBar.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				cursor.dispose();
			}
		});
		ToolItem helpItem = new ToolItem(toolBar, SWT.NONE);
		helpItem.setImage(helpImage);
		helpItem.setToolTipText(JFaceResources.getString("helpToolTip")); //$NON-NLS-1$
		helpItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				PlatformUI.getWorkbench().getHelpSystem().displayHelpResource(helpUrl);
			}
		});
		return toolBar;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite tparent = (Composite) super.createDialogArea(parent);
		tparent.setLayout(new GridLayout());
		GridData parentData = new GridData(GridData.FILL_BOTH);
		parentData.heightHint = 600;
		parentData.widthHint = 775;
		tparent.setLayoutData(parentData);

		Group groupSearch = new Group(tparent, SWT.None);
		groupSearch.setText(Messages.getString("dialog.TermBaseSearchDialog.groupSearch"));
		GridLayoutFactory.swtDefaults().margins(5, 5).numColumns(3).equalWidth(false).applyTo(groupSearch);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(groupSearch);
		Label lblSearch = new Label(groupSearch, SWT.NONE);
		lblSearch.setText(Messages.getString("dialog.TermBaseSearchDialog.lblSearch"));
		GridDataFactory.fillDefaults().align(SWT.RIGHT, SWT.CENTER).applyTo(lblSearch);
		cmbSearch = new Combo(groupSearch, SWT.DROP_DOWN | SWT.BORDER);
		cmbSearch.setText(strSearchText == null ? "" : strSearchText);
		GridData txtData = new GridData();
		// 解决在 Windows 下文本框高度太小的问题
		// txtData.heightHint = 20;
		txtData.widthHint = 590;
		cmbSearch.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		btnSearch = new Button(groupSearch, SWT.PUSH);
		btnSearch.setText(Messages.getString("dialog.TermBaseSearchDialog.btnSearch"));

		new Label(groupSearch, SWT.NONE);
		Composite compCondition = new Composite(groupSearch, SWT.NONE);
		GridLayoutFactory.fillDefaults().spacing(8, 0).numColumns(4).equalWidth(false).applyTo(compCondition);
		GridDataFactory.fillDefaults().span(2, 1).grab(true, false).applyTo(compCondition);

		btnIsCaseSensitive = new Button(compCondition, SWT.CHECK);
		btnIsCaseSensitive.setText(Messages.getString("dialog.TermBaseSearchDialog.btnIsCaseSensitive"));
		GridDataFactory.swtDefaults().applyTo(btnIsCaseSensitive);

		btnIsIgnoreMark = new Button(compCondition, SWT.CHECK);
		btnIsIgnoreMark.setText(Messages.getString("dialog.TermBaseSearchDialog.btnIsIgnoreMark"));
		btnIsIgnoreMark.setSelection(true);
		GridDataFactory.swtDefaults().applyTo(btnIsIgnoreMark);

		btnApplyRegularExpression = new Button(compCondition, SWT.CHECK);
		btnApplyRegularExpression.setText(Messages.getString("dialog.TermBaseSearchDialog.btnApplyRegularExpression"));
		GridDataFactory.swtDefaults().applyTo(btnApplyRegularExpression);

		Composite compMatchQuality = new Composite(compCondition, SWT.NONE);
		GridLayoutFactory.fillDefaults().extendedMargins(0, 0, 0, 0).margins(0, 0).spacing(0, 0).numColumns(3)
				.equalWidth(false).applyTo(compMatchQuality);
		GridDataFactory.fillDefaults().align(SWT.LEFT, SWT.CENTER).grab(true, false).applyTo(compMatchQuality);

		new Label(compMatchQuality, SWT.NONE).setText(Messages.getString("dialog.TermBaseSearchDialog.lblQuality"));
		spiMatchQuality = new Spinner(compMatchQuality, SWT.BORDER);
		spiMatchQuality.setMaximum(100);
		spiMatchQuality.setMinimum(30);
		spiMatchQuality.setIncrement(5);
		spiMatchQuality.setSelection(100);
		GridData spinnaData = new GridData();
		spinnaData.widthHint = 23;
		spiMatchQuality.setLayoutData(spinnaData);
		new Label(compMatchQuality, SWT.NONE).setText("%");

		Label lblTB = new Label(groupSearch, SWT.NONE);
		lblTB.setText(Messages.getString("dialog.TermBaseSearchDialog.lblTB"));
		GridDataFactory.fillDefaults().align(SWT.RIGHT, SWT.CENTER).applyTo(lblTB);
		Composite cmpTB = new Composite(groupSearch, SWT.NONE);
		GridLayoutFactory.fillDefaults().spacing(8, 0).numColumns(2).equalWidth(false).applyTo(cmpTB);
		GridDataFactory.fillDefaults().span(2, 1).grab(true, false).applyTo(cmpTB);
		cmbDatabase = new Combo(cmpTB, SWT.READ_ONLY);
		GridDataFactory.swtDefaults().hint(120, SWT.DEFAULT).applyTo(cmbDatabase);
		initDatabaseCombo();
		btnSelectLang = new Button(cmpTB, SWT.RIGHT);
		// GridData data = new GridData();
		// data.widthHint = 150;
		// data.heightHint = 27;
		// btnSelectLang.setLayoutData(data);
		// btnSelectLang.setImage(Activator.getImageDescriptor(ImageConstants.CONCORDANCE_SELECT_LANG).createImage());
		// btnSelectLang.addPaintListener(new PaintListener() {
		// public void paintControl(PaintEvent e) {
		// e.gc.drawText(Messages.getString("dialog.TermBaseSearchDialog.btnSelectLang"), 5, 5,
		// SWT.DRAW_TRANSPARENT);
		// }
		// });
		btnSelectLang.setText(Messages.getString("dialog.TermBaseSearchDialog.btnSelectLang"));
		initLanguageMenu();

		Group groupTable = new Group(tparent, SWT.NONE);
		GridLayoutFactory.swtDefaults().margins(10, 10).applyTo(groupTable);
		groupTable.setLayoutData(new GridData(GridData.FILL_BOTH));
		// GridDataFactory.fillDefaults().hint(700, 440).applyTo(groupTable);
		groupTable.setText(Messages.getString("dialog.TermBaseSearchDialog.groupTable"));

		grid = new Grid(groupTable, SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER | SWT.FULL_SELECTION);
		grid.setHeaderVisible(true);
		grid.setLinesVisible(true);
		grid.setLayoutData(new GridData(GridData.FILL_BOTH));
		grid.setAutoHeight(true);
		grid.setRowsResizeable(true);
		grid.setWordWrapHeader(true);

		if (strSrcLang != null) {
			columnSrcLang = new GridColumn(grid, SWT.NONE);
			srcCellRenderer.setVerticalAlignment(SWT.CENTER);
			srcCellRenderer.setStyleColumn(0);
			columnSrcLang.setCellRenderer(srcCellRenderer);
			columnSrcLang.setText(strSrcLang);
			columnSrcLang.setWidth(365);
			columnSrcLang.setWordWrap(true);
		}

		if (strTgtLang != null) {
			GridColumn columnTgtLang = new GridColumn(grid, SWT.NONE);
			TBSearchCellRenderer cellRenderer = new TBSearchCellRenderer();
			cellRenderer.setVerticalAlignment(SWT.CENTER);
			columnTgtLang.setCellRenderer(cellRenderer);
			columnTgtLang.setText(strTgtLang);
			columnTgtLang.setWidth(365);
			columnTgtLang.setWordWrap(true);
		}

		for (String strLang : lstLangs) {
			final GridColumn column = new GridColumn(grid, SWT.NONE);
			TBSearchCellRenderer cellRenderer = new TBSearchCellRenderer();
			cellRenderer.setVerticalAlignment(SWT.CENTER);
			column.setCellRenderer(cellRenderer);
			column.setText(strLang);
			column.setWidth(0);
			column.setWordWrap(true);
		}

		readDialogSettings();
		setEnabled();
		updateCombo(cmbSearch, lstSearchHistory);
		if (!strSearchText.equals("")) {
			cmbSearch.setText(strSearchText);
		} else if (lstSearchHistory != null && lstSearchHistory.size() > 0) {
			cmbSearch.setText(lstSearchHistory.get(0));
		}
		cmbSearch.setSelection(new Point(0, cmbSearch.getText().length()));
		initListener();

		return parent;
	}

	/**
	 * 初始化术语库下拉框 ;
	 */
	private void initDatabaseCombo() {
		List<String> lstItem = new ArrayList<String>();
		lstItem.add(Messages.getString("dialog.TermDbManagerDialog.lstItem"));
		// int index = 0;
		for (int i = 0; i < lstDatabase.size(); i++) {
			DatabaseModelBean model = lstDatabase.get(i);
			// if (model.isDefault()) {
			// index = i;
			// }
			lstItem.add(model.getDbName());
		}
		cmbDatabase.setItems((String[]) lstItem.toArray(new String[lstItem.size()]));
		cmbDatabase.select(0);
	}

	/**
	 * 初始化语言菜单 ;
	 */
	private void initLanguageMenu() {
		Set<String> set = new HashSet<String>();
		for (DatabaseModelBean model : lstDatabase) {
			MetaData metaData = model.toDbMetaData();
			DBOperator dbop = DatabaseService.getDBOperator(metaData);
			if(null == dbop){
				continue;
			}
			try {
				dbop.start();
				set.addAll(dbop.getLanguages());
			} catch (SQLException e) {
				LOGGER.error(Messages.getString("dialog.TermBaseSearchDialog.logger1"), e);
			} catch (ClassNotFoundException e) {
				LOGGER.error(Messages.getString("dialog.TermBaseSearchDialog.logger1"), e);
			} finally {
				try {
					if (dbop != null) {
						dbop.end();
					}
				} catch (SQLException e) {
					LOGGER.error("", e);
				}
			}
		}
		set.remove(strSrcLang);
		set.remove(strTgtLang);

		lstLangs = new ArrayList<String>(set);
		Collections.sort(lstLangs);
		// cmbLang.setItems((String[]) langs.toArray(new String[langs.size()]));

		menu = new Menu(getShell(), SWT.POP_UP);
		// if (strSrcLang != null) {
		// MenuItem itemSrc = new MenuItem(menu, SWT.CHECK);
		// itemSrc.setText(strSrcLang);
		// itemSrc.setSelection(true);
		// itemSrc.setEnabled(false);
		// }
		if (strTgtLang != null) {
			MenuItem itemTgt = new MenuItem(menu, SWT.CHECK);
			itemTgt.setText(strTgtLang);
			itemTgt.setSelection(true);
			itemTgt.setEnabled(false);
		}
		for (final String lang : lstLangs) {
			final MenuItem itemLang = new MenuItem(menu, SWT.CHECK);
			itemLang.setText(lang);
			itemLang.addListener(SWT.Selection, new Listener() {

				public void handleEvent(Event event) {
					ArrayList<GridColumn> lstShowColumn = new ArrayList<GridColumn>();
					// 每增加一列，除标记列外的其他列的和加100，然后平均分配给各个语言列，删除一列则做相反的操作
					if (itemLang.getSelection()) {
						int totalWidth = 0;
						boolean blnIsResetWidth = false;
						for (int index = 0; index < grid.getColumnCount(); index++) {
							GridColumn column = grid.getColumn(index);

							if (column.getText().equals(lang) && column.getWidth() == 0) {
								lstShowColumn.add(column);
								blnIsResetWidth = true;
							} else if (column.getWidth() > 0) {
								totalWidth += column.getWidth();
								lstShowColumn.add(column);
							}
						}
						if (blnIsResetWidth) {
							int width = (totalWidth + 100) / lstShowColumn.size();
							for (GridColumn column : lstShowColumn) {
								column.setWidth(width);
							}
						}
						// if (grid.getItemCount() > 0) {
						// search();
						// }
					} else {
						GridColumn deleteColumn = null;
						for (int index = 0; index < grid.getColumnCount(); index++) {
							GridColumn column = grid.getColumn(index);

							if (column.getWidth() > 0) {
								lstShowColumn.add(column);
							}
							if (column.getText().equals(lang)) {
								deleteColumn = column;
								// 将删除列中的数据清空，以保证行高正常调整
								for (GridItem item : grid.getItems()) {
									item.setText(index, "");
								}
							}
						}

						int width = (deleteColumn.getWidth() * lstShowColumn.size() - 100) / (lstShowColumn.size() - 1);
						deleteColumn.setWidth(0);
						lstShowColumn.remove(deleteColumn);
						for (GridColumn column : lstShowColumn) {
							column.setWidth(width);
						}
						// search();
					}
				}
			});
		}
	}

	/**
	 * 初始化各控件的监听 ;
	 */
	private void initListener() {

		// 当点击添加/删除语言按钮时，菜单要显示在按钮下方
		btnSelectLang.addListener(SWT.Selection, new Listener() {

			public void handleEvent(Event event) {
				Rectangle rect = btnSelectLang.getBounds();
				// Point pt = selLangBar.toDisplay(new Point(event.x + rect.width, event.y + rect.height));
				// menu.setOrientation(SWT.RIGHT_TO_LEFT);
				Point pt = btnSelectLang.toDisplay(new Point(event.x, event.y + rect.height));
				menu.setLocation(pt.x, pt.y);
				menu.setVisible(true);
			}

		});

		btnApplyRegularExpression.addSelectionListener(new SelectionListener() {

			public void widgetSelected(SelectionEvent e) {
				setEnabled();
			}

			public void widgetDefaultSelected(SelectionEvent e) {

			}

		});

		btnSearch.addSelectionListener(new SelectionListener() {

			public void widgetSelected(SelectionEvent e) {
				search();
			}

			public void widgetDefaultSelected(SelectionEvent e) {

			}

		});

		cmbSearch.addKeyListener(new KeyListener() {

			public void keyPressed(KeyEvent e) {
				if (e.character == SWT.CR) {
					search();

				}
			}

			public void keyReleased(KeyEvent e) {

			}

		});
		/**
		 * 添加双击插入术语到编辑区
		 */
		grid.addListener(SWT.MouseDoubleClick, new Listener() {

			public void handleEvent(Event event) {
				// TODO Auto-generated method stub
				InsertGridTgtToEditor();

			}
		});
	}

	private void InsertGridTgtToEditor() {
		GridItem[] selection = grid.getSelection();
		if (null == selection || selection.length == 0) {
			return;
		}

		IXliffEditor tempEditor = null;
		IEditorPart activeEditor = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
				.getActiveEditor();
		if (activeEditor instanceof IXliffEditor) {
			IXliffEditor editor = (IXliffEditor) activeEditor;
			tempEditor = editor;

		}
		if (tempEditor == null) {
			return;
		}
		int[] selectedRows = tempEditor.getSelectedRows();
		if (null == selectedRows || selectedRows.length == 0) {
			return;
		}
		int rowIndex = selectedRows[0];
		if (tempEditor == null || rowIndex < 0) {
			return;
		}
		TransUnitBean transUnit = tempEditor.getRowTransUnitBean(rowIndex);
		Hashtable<String, String> tuProp = transUnit.getTuProps();
		if (tuProp != null) {
			String translate = tuProp.get("translate");
			if (translate != null && translate.equalsIgnoreCase("no")) {
				MessageDialog.openInformation(tempEditor.getSite().getShell(),
						Messages.getString("view.TerminologyViewPart.msgTitle"),
						Messages.getString("view.TerminologyViewPart.msg1"));
				return;
			}
		}

		String tarTerm = selection[0].getText(1);
		if (null == tarTerm || tarTerm.isEmpty()) {
			return;
		}

		try {
			tempEditor.insertCell(rowIndex, tempEditor.getTgtColumnIndex(), tarTerm);
			// tempEditor.setFocus(); // 焦点给回编辑器
		} catch (ExecutionException e) {
			if (Constant.RUNNING_MODE == Constant.MODE_DEBUG) {
				e.printStackTrace();
			}
			MessageDialog.openInformation(tempEditor.getSite().getShell(),
					Messages.getString("view.TerminologyViewPart.msgTitle"),
					Messages.getString("view.TerminologyViewPart.msg2") + e.getMessage());
		}

	}

	/**
	 * 搜索术语库 ;
	 */
	@SuppressWarnings("unchecked")
	public void search() {
		updateHistory(cmbSearch, lstSearchHistory);
		String searchText = cmbSearch.getText();
		searchText = cleanString(searchText);
		if (searchText == null || searchText.trim().equals("")) {
			MessageDialog.openInformation(getShell(), Messages.getString("dialog.TermBaseSearchDialog.msgTitle"),
					Messages.getString("dialog.TermBaseSearchDialog.msg1"));
			return;
		}

		if (lstDatabase.size() == 0) {
			MessageDialog.openInformation(getShell(), Messages.getString("dialog.TermBaseSearchDialog.msgTitle"),
					Messages.getString("dialog.TermBaseSearchDialog.msg2"));
			return;
		}

		ArrayList<String> lstSelLangs = new ArrayList<String>();
		lstSelLangs.add(strSrcLang);
		for (MenuItem item : menu.getItems()) {
			if (item.getSelection()) {
				lstSelLangs.add(item.getText());
			}
		}

		LinkedHashMap<MetaData, HashMap<String, IdentityHashMap<String, String>>> mapResult = new LinkedHashMap<MetaData, HashMap<String, IdentityHashMap<String, String>>>();
		ArrayList<DatabaseModelBean> lstDB = new ArrayList<DatabaseModelBean>();
		if (cmbDatabase.getSelectionIndex() == 0) {
			lstDB.addAll(lstDatabase);
		} else {
			DatabaseModelBean model = lstDatabase.get(cmbDatabase.getSelectionIndex() - 1);
			lstDB.add(model);
		}
		for (DatabaseModelBean model : lstDB) {
			MetaData metaData = model.toDbMetaData();
			DBOperator dbop = DatabaseService.getDBOperator(metaData);
			try {
				dbop.start();
				HashMap<String, IdentityHashMap<String, String>> mapTermBase = dbop.getTermBaseResult(searchText,
						!(btnIsCaseSensitive.getSelection()), btnApplyRegularExpression.getSelection(),
						btnIsIgnoreMark.getSelection(), strSrcLang, lstSelLangs, spiMatchQuality.getSelection());
				if (mapTermBase != null && mapTermBase.size() > 0) {
					mapResult.put(metaData, mapTermBase);
				}
			} catch (SQLException e1) {
				LOGGER.error(Messages.getString("dialog.TermBaseSearchDialog.logger2"), e1);
			} catch (ClassNotFoundException e1) {
				LOGGER.error(Messages.getString("dialog.TermBaseSearchDialog.logger3"), e1);
			} finally {
				try {
					if (dbop != null) {
						dbop.end();
					}
				} catch (SQLException e) {
					LOGGER.error("", e);
				}
			}
		}
		Rectangle rect = grid.getBounds();
		grid.removeAll();
		grid.pack();
		grid.setHeaderVisible(true);
		grid.setBounds(rect);
		if (mapResult.size() == 0) {
			MessageDialog.openInformation(getShell(), Messages.getString("dialog.TermBaseSearchDialog.msgTitle"),
					Messages.getString("dialog.TermBaseSearchDialog.msg3"));
			return;
		}

		srcCellRenderer.setStrText(searchText);
		srcCellRenderer.setBlnIsCaseSensitive(btnIsCaseSensitive.getSelection());
		srcCellRenderer.setBlnIsApplyRegular(btnApplyRegularExpression.getSelection());

		if (mapResult.size() > 0) {

			if (btnApplyRegularExpression.getSelection()) {
				Iterator<Entry<MetaData, HashMap<String, IdentityHashMap<String, String>>>> iterator = mapResult
						.entrySet().iterator();
				while (iterator.hasNext()) {
					Entry<MetaData, HashMap<String, IdentityHashMap<String, String>>> entry = iterator.next();
					MetaData metaData1 = entry.getKey();
					HashMap<String, IdentityHashMap<String, String>> map = entry.getValue();
					Iterator<Entry<String, IdentityHashMap<String, String>>> it = map.entrySet().iterator();
					while (it.hasNext()) {
						Entry<String, IdentityHashMap<String, String>> e = it.next();
						createGridItem(metaData1, e.getValue());
					}
				}
			} else {
				LinkedHashMap<String, Object[]> map = sortMap(mapResult);
				Iterator<Entry<String, Object[]>> it = map.entrySet().iterator();
				while (it.hasNext()) {
					Entry<String, Object[]> entry = it.next();
					Object[] arrObj = entry.getValue();
					createGridItem((MetaData) arrObj[0], (IdentityHashMap<String, String>) arrObj[1]);
				}
			}

		}
	}

	/**
	 * 向表格中添加记录
	 * @param metaData
	 * @param map
	 *            ;
	 */
	private void createGridItem(MetaData metaData, IdentityHashMap<String, String> map) {
		// Bug #2232
		List<HashMap<String, String>> lstMap = new ArrayList<HashMap<String, String>>();
		Iterator<Entry<String, String>> it = map.entrySet().iterator();
		String srcText = null;
		while (it.hasNext()) {
			Entry<String, String> e = (Entry<String, String>) it.next();
			String key = e.getKey();
			if (key.equalsIgnoreCase(strSrcLang)) {
				srcText = resetCleanString(e.getValue());
				continue;
			}
			boolean isContain = false;
			for (HashMap<String, String> hashMap : lstMap) {
				if (!hashMap.containsKey(key)) {
					hashMap.put(key, e.getValue());
					isContain = true;
				}
			}
			if (!isContain) {
				HashMap<String, String> hashMap = new HashMap<String, String>();
				hashMap.put(key, e.getValue());
				lstMap.add(hashMap);
			}
		}

		for (HashMap<String, String> hashMap : lstMap) {
			GridItem item = new GridItem(grid, SWT.NONE);

			hashMap.put("dbName", metaData.getDatabaseName());
			hashMap.put("dbType", metaData.getDbType());
			hashMap.put("severName", metaData.getServerName());

			int arrIndex = 0;

			if (strSrcLang != null) {
				item.setText(arrIndex++, srcText);
			}
			if (strTgtLang != null) {
				String tgtText = hashMap.get(strTgtLang) == null ? "" : hashMap.get(strTgtLang);
				tgtText = resetCleanString(tgtText);
				item.setText(arrIndex++, tgtText);
			}
			item.setData(hashMap);
			item.setData("metaData", metaData);

			for (int i = 0; i < lstLangs.size(); i++) {
				item.setText((arrIndex + i), hashMap.get(lstLangs.get(i)) == null ? "" : hashMap.get(lstLangs.get(i)));
			}
		}
	}

	/**
	 * 对搜索结果按相似度进行排序
	 * @param mapItem
	 * @return ;
	 */
	private LinkedHashMap<String, Object[]> sortMap(
			LinkedHashMap<MetaData, HashMap<String, IdentityHashMap<String, String>>> mapItem) {
		Iterator<Entry<MetaData, HashMap<String, IdentityHashMap<String, String>>>> it = mapItem.entrySet().iterator();
		LinkedHashMap<String, Object[]> mapData = new LinkedHashMap<String, Object[]>();
		while (it.hasNext()) {
			Entry<MetaData, HashMap<String, IdentityHashMap<String, String>>> entry = it.next();
			MetaData metaData = entry.getKey();
			HashMap<String, IdentityHashMap<String, String>> mapTerm = entry.getValue();
			Iterator<Entry<String, IdentityHashMap<String, String>>> iterator = mapTerm.entrySet().iterator();
			while (iterator.hasNext()) {
				Map.Entry<String, IdentityHashMap<String, String>> e = (Map.Entry<String, IdentityHashMap<String, String>>) iterator
						.next();
				String strGroupIdString = e.getKey();
				IdentityHashMap<String, String> map = e.getValue();
				String key = metaData.getDatabaseName() + "_" + metaData.getServerName() + "_" + strGroupIdString;
				Object[] arrObj = new Object[] { metaData, map };
				mapData.put(key, arrObj);
			}
		}
		ArrayList<Entry<String, Object[]>> entryList = new ArrayList<Entry<String, Object[]>>(mapData.entrySet());
		Collections.sort(entryList, new Comparator<Map.Entry<String, Object[]>>() {

			@SuppressWarnings("unchecked")
			public int compare(Map.Entry<String, Object[]> arg0, Map.Entry<String, Object[]> arg1) {
				IdentityHashMap<String, String> map0 = (IdentityHashMap<String, String>) arg0.getValue()[1];
				IdentityHashMap<String, String> map1 = (IdentityHashMap<String, String>) arg1.getValue()[1];

				String strSimilarity0 = map0.get("similarity");
				String strSimilarity1 = map1.get("similarity");
				if (strSimilarity0 == null || strSimilarity1 == null) {
					if (strSimilarity0 != null) {
						return -1;
					} else if (strSimilarity1 != null) {
						return 1;
					} else {
						return 0;
					}
				} else {
					int intData0 = Integer.parseInt(strSimilarity0);
					int intData1 = Integer.parseInt(strSimilarity1);
					if (intData0 > intData1) {
						return -1;
					} else if (intData0 < intData1) {
						return 1;
					} else {
						return 0;
					}
				}
			}
		});
		mapData.clear();
		for (Entry<String, Object[]> entry : entryList) {
			String key = entry.getKey();
			Object[] obj = entry.getValue();
			mapData.put(key, obj);
		}
		return mapData;
	}

	protected void createButtonsForButtonBar(Composite parent) {
		getShell().setDefaultButton(btnSearch);
		Composite content = parent.getParent();
		// parent.dispose();
		content.layout();
	}

	private static String cleanString(String string) {
		string = string.replaceAll("&", "&amp;");
		string = string.replaceAll("<", "&lt;");
		string = string.replaceAll(">", "&gt;");
		// string = string.replaceAll("\"", "&quot;");
		return string;
	}

	private static String resetCleanString(String string) {
		string = string.replaceAll("&lt;", "<");
		string = string.replaceAll("&gt;", ">");
		// string = string.replaceAll("&quot;", "\"");
		string = string.replaceAll("&amp;", "&");
		return string;
	}

	private IDialogSettings getDialogSettings() {
		IDialogSettings settings = Activator.getDefault().getDialogSettings();
		IDialogSettings fDialogSettings = settings.getSection(getClass().getName());
		if (fDialogSettings == null)
			fDialogSettings = settings.addNewSection(getClass().getName());
		return fDialogSettings;
	}

	private void setEnabled() {
		if (btnApplyRegularExpression.getSelection()) {
			spiMatchQuality.setEnabled(false);
		} else {
			spiMatchQuality.setEnabled(true);
		}

	}

	private void readDialogSettings() {
		IDialogSettings ids = getDialogSettings();
		btnIsCaseSensitive.setSelection(ids
				.getBoolean("net.heartsome.cat.database.ui.tb.dialog.TermBaseSearchDialog.caseSensitive"));
	//	btnIsCaseSensitive.setSelection(preferenceStore.getBoolean(TBPreferenceConstants.TB_CASE_SENSITIVE));
		btnIsIgnoreMark.setSelection(!ids
				.getBoolean("net.heartsome.cat.database.ui.tb.dialog.TermBaseSearchDialog.ignoreMark"));
		btnApplyRegularExpression.setSelection(ids
				.getBoolean("net.heartsome.cat.database.ui.tb.dialog.TermBaseSearchDialog.regEx"));

		try {
			spiMatchQuality.setSelection(ids
					.getInt("net.heartsome.cat.database.ui.tb.dialog.TermBaseSearchDialog.matchQuality"));
		} catch (NumberFormatException e) {
			spiMatchQuality.setSelection(100);
		}
		String[] arrSearchHistory = ids
				.getArray("net.heartsome.cat.database.ui.tb.dialog.TermBaseSearchDialog.searchHistory");
		if (arrSearchHistory != null) {
			lstSearchHistory.clear();
			for (int i = 0; i < arrSearchHistory.length; i++) {
				lstSearchHistory.add(arrSearchHistory[i]);
			}
		}
		String selTM = ids.get("net.heartsome.cat.database.ui.tb.dialog.TermBaseSearchDialog.selTM");
		int selIndex = 0;
		if (selTM != null) {
			for (int i = 0; i < cmbDatabase.getItemCount(); i++) {
				if (selTM.equals(cmbDatabase.getItem(i))) {
					selIndex = i;
					break;
				}
			}
		}
		cmbDatabase.select(selIndex);

		String[] arrTarget = ids.getArray("net.heartsome.cat.database.ui.tb.dialog.TermBaseSearchDialog.selTgt");
		List<String> lstSelItem = new ArrayList<String>();
		if (arrTarget != null) {
			for (int i = 0; i < menu.getItemCount(); i++) {
				MenuItem item = menu.getItem(i);
				for (String target : arrTarget) {
					if (item.getText().equals(target)) {
						item.setSelection(true);
						break;
					}
				}
				if (item.getSelection()) {
					lstSelItem.add(item.getText());
				}
			}
		}

		// 重设表格列宽
		int totalWidth = 0;
		List<GridColumn> lstShowColumn = new ArrayList<GridColumn>();
		for (int index = 0; index < grid.getColumnCount(); index++) {
			GridColumn column = grid.getColumn(index);
			if (column.getWidth() > 0) {
				totalWidth += column.getWidth();
			}
			if (column.getWidth() > 0 || lstSelItem.indexOf(column.getText()) >= 0) {
				lstShowColumn.add(column);
			}
		}
		int width = (totalWidth + 100) / lstShowColumn.size();
		for (GridColumn column : lstShowColumn) {
			column.setWidth(width);
		}
	}

	@Override
	public boolean close() {
		writeDialogSettings();
		return super.close();
	}

	private void writeDialogSettings() {
		IDialogSettings ids = getDialogSettings();
		ids.put("net.heartsome.cat.database.ui.tb.dialog.TermBaseSearchDialog.caseSensitive",
				btnIsCaseSensitive.getSelection());
	//	preferenceStore.setDefault(TBPreferenceConstants.TB_CASE_SENSITIVE,btnIsCaseSensitive.getSelection());
		ids.put("net.heartsome.cat.database.ui.tb.dialog.TermBaseSearchDialog.ignoreMark",
				!btnIsIgnoreMark.getSelection());
		ids.put("net.heartsome.cat.database.ui.tb.dialog.TermBaseSearchDialog.regEx",
				btnApplyRegularExpression.getSelection());
		ids.put("net.heartsome.cat.database.ui.tb.dialog.TermBaseSearchDialog.matchQuality",
				spiMatchQuality.getSelection());
		if (okToUse(cmbSearch)) {
			String searchString = cmbSearch.getText();
			if (searchString.length() > 0) {
				lstSearchHistory.add(0, searchString);
			}
			writeHistory(lstSearchHistory, ids,
					"net.heartsome.cat.database.ui.tb.dialog.TermBaseSearchDialog.searchHistory");
		}
		ids.put("net.heartsome.cat.database.ui.tb.dialog.TermBaseSearchDialog.selTM", cmbDatabase.getText());

		List<String> lstTgt = new ArrayList<String>();
		for (MenuItem item : menu.getItems()) {
			if (item.getSelection()) {
				lstTgt.add(item.getText());
			}
		}
		String[] arrTgt = new String[lstTgt.size()];
		lstTgt.toArray(arrTgt);
		ids.put("net.heartsome.cat.database.ui.tb.dialog.TermBaseSearchDialog.selTgt", arrTgt);
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
	 * Writes the given history into the given dialog store.
	 * @param history
	 *            the history
	 * @param settings
	 *            the dialog settings
	 * @param sectionName
	 *            the section name
	 * @since 3.2
	 */
	private void writeHistory(List<String> history, IDialogSettings settings, String sectionName) {
		int itemCount = history.size();
		Set<String> distinctItems = new HashSet<String>(itemCount);
		for (int i = 0; i < itemCount; i++) {
			String item = (String) history.get(i);
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

		String[] names = new String[history.size()];
		history.toArray(names);
		settings.put(sectionName, names);
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
}

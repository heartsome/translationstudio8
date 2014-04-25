package net.heartsome.cat.database.ui.tm.dialog;

import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import net.heartsome.cat.common.bean.DatabaseModelBean;
import net.heartsome.cat.common.bean.MetaData;
import net.heartsome.cat.common.bean.TmxProp;
import net.heartsome.cat.common.innertag.InnerTagBean;
import net.heartsome.cat.common.innertag.TagStyle;
import net.heartsome.cat.common.ui.utils.InnerTagUtil;
import net.heartsome.cat.common.util.CommonFunction;
import net.heartsome.cat.common.util.TextUtil;
import net.heartsome.cat.database.DBOperator;
import net.heartsome.cat.database.service.DatabaseService;
import net.heartsome.cat.database.tmx.ConcordanceBean;
import net.heartsome.cat.database.tmx.LanguageTMX;
import net.heartsome.cat.database.ui.tm.Activator;
import net.heartsome.cat.database.ui.tm.ImageConstants;
import net.heartsome.cat.database.ui.tm.resource.Messages;
import net.heartsome.cat.ts.core.file.ProjectConfiger;
import net.heartsome.cat.ts.core.file.ProjectConfigerFactory;
import net.heartsome.cat.ts.ui.jaret.renderer.ImageCellRender;
import net.heartsome.cat.ts.ui.jaret.renderer.StyleTextCellRenderer;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TrayDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.Util;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.TextStyle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.events.IExpansionListener;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jaret.util.ui.table.JaretTable;
import de.jaret.util.ui.table.model.IColumn;
import de.jaret.util.ui.table.model.IRow;
import de.jaret.util.ui.table.model.PropCol;
import de.jaret.util.ui.table.model.PropListeningTableModel;
import de.jaret.util.ui.table.renderer.DefaultTableHeaderRenderer;
import de.jaret.util.ui.table.renderer.TextCellRenderer;

/**
 * 相关搜索对话框
 * @author peason
 * @version
 * @since JDK1.6
 */
public class ConcordanceSearchDialog extends TrayDialog {

	private static final Logger LOGGER = LoggerFactory.getLogger(LoggerFactory.class.getName());

	/** 当前项目记忆库中的语言集合 */
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

	/** 记忆库下拉框 */
	private Combo cmbDatabase;

	private JaretTable jTable;

	PropListeningTableModel tableModel;

	/** 文本段信息 */
	private String strMsg = Messages.getString("dialog.ConcordanceSearchDialog.strMsg");

	/** 选择语言按钮 */
	private Button btnSelectLang;

	/** 选择语言菜单 */
	private Menu menu;

	/** 每页显示的行数 */
	private int rowNumPerPage = 20;

	/** 首页按钮 */
	private ToolItem btnFirst;

	/** 上一页按钮 */
	private ToolItem btnPre;

	/** 下一页按钮 */
	private ToolItem btnNext;

	/** 最后一页按钮 */
	private ToolItem btnLast;

	/** 输入页号文本框 */
	private Text txtPage;

	/** 记录当前页号 */
	private int curPageNum;

	/** 页数 */
	private int amountPage;

	/** 根据搜索条件得到的 GroupId 集合（已按修改日期排序） */
	// private ArrayList<Integer> lstGroupId = new ArrayList<Integer>();

	/** 根据搜索条件得到的 GroupId 集合 */
	private LinkedHashMap<MetaData, ArrayList<Integer>> mapGroupId = new LinkedHashMap<MetaData, ArrayList<Integer>>();

	/** 页号文本框中显示当前页号与总页数之间的分隔符 */
	private String splitPageSeparator = "/";

	private ExpandableComposite cmpExpandableFilter;

	/** 过滤条件中选择源文或译文的下拉框 */
	private Combo cmbSrcOrTgt;

	/** 过滤条件中选择包含或不包含的下拉框 */
	private Combo cmbContain;

	/** 过滤条件中输入过滤内容的文本框 */
	private Combo cmbFilter;

	// private ToolBar selLangBar;

	private int totalWidth = 950;

	private int colCount;

	private int size = 0;

	private Image firstImage = Activator.getImageDescriptor(ImageConstants.PAGE_FIRST).createImage();
	private Image preImage = Activator.getImageDescriptor(ImageConstants.PAGE_PRE).createImage();
	private Image nextImage = Activator.getImageDescriptor(ImageConstants.PAGE_NEXT).createImage();
	private Image lastImage = Activator.getImageDescriptor(ImageConstants.PAGE_LAST).createImage();

	// private Color color = new Color(Display.getDefault(), 150, 100, 100);
	private Font font;
	private TextStyle style;

	private final int HISTORY_SIZE = 5;

	private List<String> lstSearchHistory;

	private List<String> lstFilterHistory;

	private Group groupTable;

	private Composite tparent;

	/**
	 * 搜索结果背景色
	 */
	private Color background;
	/**
	 * 搜索结果前景色
	 */
	private Color foreground;
	/**
	 * 搜索结果字体
	 */
	private Font rsFont;
	/**
	 * 构造方法
	 * @param parentShell
	 * @param file
	 *            当前文件
	 * @param strSrcLang
	 *            当前文件的源语言
	 * @param strTgtLang
	 *            当前文件的目标语言
	 * @param strSearchText
	 *            搜索文本
	 */
	public ConcordanceSearchDialog(Shell parentShell, IFile file, String strSrcLang, String strTgtLang,
			String strSearchText) {
		super(parentShell);

		FontData fontData = JFaceResources.getDefaultFont().getFontData()[0];
		fontData.setStyle(fontData.getStyle() | SWT.BOLD);
		font = new Font(Display.getDefault(), fontData);
		style = new TextStyle(font, null, null);

		this.strSrcLang = strSrcLang;
		this.strTgtLang = strTgtLang;
		this.strSearchText = strSearchText;
		ProjectConfiger projectConfig = ProjectConfigerFactory.getProjectConfiger(file.getProject());
		lstDatabase = projectConfig.getAllTmDbs();
		filterUnAvaliableDatabase();
		setHelpAvailable(true);
		setBlockOnOpen(false);
		lstSearchHistory = new ArrayList<String>(HISTORY_SIZE - 1);
		lstFilterHistory = new ArrayList<String>(HISTORY_SIZE - 1);
		if (!Util.isLinux()) {
			totalWidth = 910;
		}
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(Messages.getString("dialog.ConcordanceSearchDialog.title"));
	}
	public Set<String> getSysAvailableDatabase() {
		Map<String, MetaData> systemSuportDbMetaData = DatabaseService.getSystemSuportDbMetaData();
		return systemSuportDbMetaData.keySet();
	}
	/**
	 * 过滤掉系统不支持的数据库
	 *  ;
	 */
	public void filterUnAvaliableDatabase(){
		Set<String> sysAvailableDatabase = getSysAvailableDatabase();
		List<DatabaseModelBean> rs = new ArrayList<DatabaseModelBean> (3);
		for(DatabaseModelBean dmb :lstDatabase ){
			if(sysAvailableDatabase.contains(dmb.getDbType())){
				//lstDatabase.remove(dmb);
				rs.add(dmb);
			}
		}
		lstDatabase =rs;
	}
	/**
	 * 添加帮助按钮 robert 2012-09-06
	 */
	@Override
	protected Control createHelpControl(Composite parent) {
		// ROBERTHELP 相关搜索
		String language = CommonFunction.getSystemLanguage();
		final String helpUrl = MessageFormat.format(
				"/net.heartsome.cat.ts.ui.help/html/{0}/ch05s04.html#concordance-search", language);
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
		tparent = (Composite) super.createDialogArea(parent);
		GridLayoutFactory.swtDefaults().spacing(0, 0).extendedMargins(SWT.DEFAULT, SWT.DEFAULT, 0, 0).applyTo(tparent);
		// tparent.setLayout(new GridLayout());
		GridData parentData = new GridData(GridData.FILL_BOTH);
		parentData.widthHint = 1058;
		tparent.setLayoutData(parentData);

		Group groupSearch = new Group(tparent, SWT.NONE);
		GridLayoutFactory.swtDefaults().margins(5, 5).numColumns(3).equalWidth(false).applyTo(groupSearch);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(groupSearch);
		groupSearch.setText(Messages.getString("dialog.ConcordanceSearchDialog.groupSearch"));
		Label lblSearch = new Label(groupSearch, SWT.NONE);
		lblSearch.setText(Messages.getString("dialog.ConcordanceSearchDialog.lblSearch"));
		GridDataFactory.fillDefaults().align(SWT.RIGHT, SWT.CENTER).applyTo(lblSearch);
		cmbSearch = new Combo(groupSearch, SWT.BORDER | SWT.DROP_DOWN);
		cmbSearch.setText(strSearchText == null ? "" : InnerTagUtil.resolveTag(strSearchText));
		GridData txtData = new GridData();
		// 解决在 Windows 下文本框高度太小的问题
		// txtData.heightHint = 20;
		txtData.widthHint = 610;
		cmbSearch.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		btnSearch = new Button(groupSearch, SWT.PUSH);
		btnSearch.setText(Messages.getString("dialog.ConcordanceSearchDialog.btnSearch"));

		new Label(groupSearch, SWT.NONE);
		Composite compCondition = new Composite(groupSearch, SWT.NONE);
		GridLayoutFactory.fillDefaults().numColumns(3).equalWidth(false).applyTo(compCondition);
		GridDataFactory.fillDefaults().span(2, 1).grab(true, false).applyTo(compCondition);

		btnIsCaseSensitive = new Button(compCondition, SWT.CHECK);
		btnIsCaseSensitive.setText(Messages.getString("dialog.ConcordanceSearchDialog.btnIsCaseSensitive"));
		GridDataFactory.swtDefaults().applyTo(btnIsCaseSensitive);

		btnIsIgnoreMark = new Button(compCondition, SWT.CHECK);
		btnIsIgnoreMark.setText(Messages.getString("dialog.ConcordanceSearchDialog.btnIsIgnoreMark"));
		btnIsIgnoreMark.setSelection(true);
		GridDataFactory.swtDefaults().applyTo(btnIsIgnoreMark);

		btnApplyRegularExpression = new Button(compCondition, SWT.CHECK);
		btnApplyRegularExpression.setText(Messages
				.getString("dialog.ConcordanceSearchDialog.btnApplyRegularExpression"));
		GridDataFactory.swtDefaults().applyTo(btnApplyRegularExpression);

		Label lblTM = new Label(groupSearch, SWT.NONE);
		lblTM.setText(Messages.getString("dialog.ConcordanceSearchDialog.lblDB"));
		GridDataFactory.fillDefaults().align(SWT.RIGHT, SWT.CENTER).applyTo(lblTM);
		Composite compDB = new Composite(groupSearch, SWT.NONE);
		GridLayoutFactory.fillDefaults().numColumns(2).equalWidth(false).applyTo(compDB);
		GridDataFactory.fillDefaults().span(2, 1).grab(true, false).applyTo(compDB);
		cmbDatabase = new Combo(compDB, SWT.READ_ONLY);
		GridDataFactory.swtDefaults().hint(150, SWT.DEFAULT).applyTo(cmbDatabase);
		initDatabaseCombo();
		btnSelectLang = new Button(compDB, SWT.RIGHT);
		// GridData data = new GridData();
		// data.widthHint = 150;
		// data.heightHint = 27;
		// btnSelectLang.setLayoutData(data);
		// btnSelectLang.setImage(Activator.getImageDescriptor(ImageConstants.CONCORDANCE_SELECT_LANG).createImage());
		// btnSelectLang.addPaintListener(new PaintListener() {
		// public void paintControl(PaintEvent e) {
		// e.gc.drawText(Messages.getString("dialog.ConcordanceSearchDialog.btnSelectLang"), 5, 5,
		// SWT.DRAW_TRANSPARENT);
		// }
		// });
		btnSelectLang.setText(Messages.getString("dialog.ConcordanceSearchDialog.btnSelectLang"));
		initLanguageMenu();

		FormToolkit toolkit = new FormToolkit(parent.getDisplay());
		Group groupFilter = new Group(tparent, SWT.None);
		GridLayoutFactory.swtDefaults().margins(5, 5).applyTo(groupFilter);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(groupFilter);
		groupFilter.setText(Messages.getString("dialog.ConcordanceSearchDialog.groupFilter"));
		cmpExpandableFilter = toolkit.createExpandableComposite(groupFilter, ExpandableComposite.TITLE_BAR
				| ExpandableComposite.TWISTIE);
		cmpExpandableFilter.setText(Messages.getString("dialog.ConcordanceSearchDialog.cmpExpandableFilter"));
		Composite cmpFilter = toolkit.createComposite(cmpExpandableFilter);
		cmpFilter.setLayout(new GridLayout(3, false));
		GridDataFactory.fillDefaults().grab(true, false).applyTo(cmpFilter);
		cmpExpandableFilter.setBackground(tparent.getBackground());
		cmpExpandableFilter.setClient(cmpFilter);
		cmpFilter.setBackground(tparent.getBackground());

		cmbSrcOrTgt = new Combo(cmpFilter, SWT.READ_ONLY);
		GridDataFactory.swtDefaults().hint(100, SWT.DEFAULT).applyTo(cmbSrcOrTgt);
		cmbSrcOrTgt.setItems(new String[] { Messages.getString("dialog.ConcordanceSearchDialog.cmbSrcOrTgt1"),
				Messages.getString("dialog.ConcordanceSearchDialog.cmbSrcOrTgt2") });
		cmbSrcOrTgt.setData(Messages.getString("dialog.ConcordanceSearchDialog.cmbSrcOrTgt1"), strSrcLang);
		cmbSrcOrTgt.setData(Messages.getString("dialog.ConcordanceSearchDialog.cmbSrcOrTgt2"), strTgtLang);

		cmbContain = new Combo(cmpFilter, SWT.READ_ONLY);
		GridDataFactory.swtDefaults().hint(100, SWT.DEFAULT).applyTo(cmbContain);
		cmbContain.setItems(new String[] { Messages.getString("dialog.ConcordanceSearchDialog.cmbContain1"),
				Messages.getString("dialog.ConcordanceSearchDialog.cmbContain2") });
		cmbContain.setData(Messages.getString("dialog.ConcordanceSearchDialog.cmbContain1"), "LIKE");
		cmbContain.setData(Messages.getString("dialog.ConcordanceSearchDialog.cmbContain2"), "NOT LIKE");

		cmbFilter = new Combo(cmpFilter, SWT.BORDER | SWT.DROP_DOWN);
		GridDataFactory.swtDefaults().hint(410, SWT.DEFAULT).applyTo(cmbFilter);
		cmpExpandableFilter.setExpanded(false);

		groupTable = new Group(tparent, SWT.None);
		GridLayoutFactory.swtDefaults().margins(5, 5).spacing(0, 2).numColumns(1).equalWidth(false).applyTo(groupTable);
		GridDataFactory.fillDefaults().hint(740, 450).applyTo(groupTable);
		groupTable.setText(Messages.getString("dialog.ConcordanceSearchDialog.groupTable"));
		groupTable.setBackground(groupTable.getParent().getBackground());
		groupTable.setBackgroundMode(SWT.INHERIT_FORCE);

		cmpExpandableFilter.addExpansionListener(new IExpansionListener() {

			public void expansionStateChanging(ExpansionEvent e) {
				layoutExpandable();
			}

			public void expansionStateChanged(ExpansionEvent e) {
				layoutExpandable();
			}
		});

		jTable = new JaretTable(groupTable, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL) {
			public void rowSelectionAdded(IRow row) {
				super.rowSelectionAdded(row);
				// XPropRow propRow = (XPropRow) row;
				// HashMap<String, String> map = (HashMap<String, String>) propRow.getDataMap();
				// String strChangeDate = map.get("changeDate");
				// // strChangeDate = checkString(strChangeDate == null || strChangeDate.equals("") ? "" :
				// CommonFunctions
				// // .retGMTdate(strChangeDate));
				// strChangeDate = checkString(strChangeDate == null || strChangeDate.equals("") ? "" : strChangeDate);
				// String strChangeId = checkString(map.get("changeId"));
				// String strDbInfo = checkString(map.get("dbType")) + "/" + checkString(map.get("severName")) + "/"
				// + checkString(map.get("dbName"));
				// String strProjectInfo = checkString(map.get("projectRef"));
				// String strJobInfo = checkString(map.get("jobRef"));
				// MessageFormat mf = new MessageFormat(strMsg);
				// lblInfo.setText(mf.format(new String[] { strChangeDate, strChangeId, strDbInfo, strProjectInfo,
				// strJobInfo }));

			}
		};
		jTable.setLayoutData(new GridData(GridData.FILL_BOTH));
		((DefaultTableHeaderRenderer) jTable.getHeaderRenderer())
				.setAlignment(DefaultTableHeaderRenderer.Alignment.LEFT);
		jTable.setHeaderResizeAllowed(false);
		jTable.setAllowSorting(false);
		jTable.registerCellEditor(String.class, new ReadOnlyTextCellEditor(true));

		PropListeningTableModel model = new PropListeningTableModel();

		ListPropCol colTag = new ListPropCol("Flag", Messages.getString("dialog.ConcordanceSearchDialog.colTag"),
				"Flag", -1);
		model.addColumn(colTag);
		jTable.getTableViewState().setColumnWidth(colTag, 55);
		if (strSrcLang != null) {
			PropCol ct1 = new PropCol("Source", strSrcLang, "Source");
			ct1.setEditable(false);
			model.addColumn(ct1);
			jTable.getTableViewState().setColumnWidth(ct1, 325);
		}

		if (strTgtLang != null) {
			PropCol col = new PropCol("Target", strTgtLang, "Target");
			model.addColumn(col);
			jTable.getTableViewState().setColumnWidth(col, 325);
		}

		for (int i = 0; i < lstLangs.size(); i++) {
			String strLang = lstLangs.get(i);
			ListPropCol col = new ListPropCol("Target", strLang, "LstTarget", i);
			col.setEditable(true);
			model.addColumn(col);
			jTable.getTableViewState().setColumnWidth(col, 0);

		}
		PropCol attrCol = new PropCol("Attribute", "Attribute", "Attribute");
		attrCol.setEditable(false);
		model.addColumn(attrCol);
		jTable.getTableViewState().setColumnWidth(attrCol, 325);

		tableModel = model;
		jTable.setHeaderHeight(20);
		jTable.setTableModel(tableModel);
		jTable.setDrawHeader(true);

		// jTable.getTableViewState().setRowHeightMode(ITableViewState.RowHeightMode.VARIABLE);
		jTable.registerCellRenderer(tableModel.getColumn(2), new TextCellRenderer());
		colCount = jTable.getColumnCount();
		for (int colNum = colCount - 2; colNum >= 3; colNum--) {
			IColumn column = jTable.getColumn(colNum);
			jTable.registerCellRenderer(tableModel.getColumn(colNum), new TextCellRenderer());
			jTable.getTableViewState().setColumnVisible(column, false);
		}
		ImageCellRender imgRender = new ImageCellRender();
		// 表示需要删除标记(记录有标记时要显示的图片)
		imgRender
				.addImageDescriptorMapping(Boolean.FALSE, "1", Activator.getImageDescriptor(ImageConstants.TAG_DELETE));
		// 表示需要添加标记(记录有标记时要显示的图片)
		imgRender.addImageDescriptorMapping(Boolean.TRUE, "2", Activator.getImageDescriptor(ImageConstants.TAG_ADD));
		jTable.registerCellRenderer(tableModel.getColumn(0), imgRender);
		jTable.getSelectionModel().setOnlyRowSelectionAllowed(true);
		jTable.getSelectionModel().setMultipleSelectionAllowed(false);

		Composite cmpPage = new Composite(groupTable, SWT.NONE);
		GridLayoutFactory.fillDefaults().spacing(3, 0).extendedMargins(0, 5, 0, 0).numColumns(3).equalWidth(false)
				.applyTo(cmpPage);
		cmpPage.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		cmpPage.setBackground(cmpPage.getParent().getBackground());
		cmpPage.setBackgroundMode(SWT.INHERIT_FORCE);
		new Label(cmpPage, SWT.None).setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		ToolBar toolBar = new ToolBar(cmpPage, SWT.NO_FOCUS | SWT.FLAT);
		btnFirst = new ToolItem(toolBar, SWT.PUSH);
		btnFirst.setImage(firstImage);
		btnPre = new ToolItem(toolBar, SWT.NONE);
		btnPre.setImage(preImage);
		btnNext = new ToolItem(toolBar, SWT.NONE);
		btnNext.setImage(nextImage);
		btnLast = new ToolItem(toolBar, SWT.NONE);
		btnLast.setImage(lastImage);
		txtPage = new Text(cmpPage, SWT.BORDER);
		GridDataFactory.fillDefaults().hint(80, SWT.DEFAULT).applyTo(txtPage);

		readDialogSettings();
		updateCombo(cmbSearch, lstSearchHistory);
		updateCombo(cmbFilter, lstFilterHistory);
		if (!strSearchText.equals("")) {
			cmbSearch.setText(strSearchText);
		} else if (lstSearchHistory != null && lstSearchHistory.size() > 0) {
			cmbSearch.setText(lstSearchHistory.get(0));
		}
		cmbSearch.setSelection(new Point(0, cmbSearch.getText().length()));
		if (lstFilterHistory != null && lstFilterHistory.size() > 0) {
			cmbFilter.setText(lstFilterHistory.get(0));
			cmbFilter.setSelection(new Point(0, cmbFilter.getText().length()));
		}
		initListener();

		return parent;
	}

	private void layoutExpandable() {
		if (cmpExpandableFilter.isExpanded()) {
			GridDataFactory.fillDefaults().hint(740, 410).applyTo(groupTable);
		} else {
			GridDataFactory.fillDefaults().hint(740, 450).applyTo(groupTable);
		}
		groupTable.pack();
		tparent.layout();
	}

	/**
	 * 初始化记忆库下拉框 ;
	 */
	private void initDatabaseCombo() {
		List<String> lstItem = new ArrayList<String>();
		lstItem.add(Messages.getString("dialog.ConcordanceSearchDialog.lstItem"));
		// int selIndex = 0;
		for (int i = 0; i < lstDatabase.size(); i++) {
			DatabaseModelBean model = lstDatabase.get(i);
			// if (model.isDefault()) {
			// selIndex = i;
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
			if(null ==dbop){
				continue;
			}
			try {
				dbop.start();
				set.addAll(dbop.getLanguages());
			} catch (SQLException e) {
				LOGGER.error(Messages.getString("dialog.ConcordanceSearchDialog.logger1"), e);
			} catch (ClassNotFoundException e) {
				LOGGER.error(Messages.getString("dialog.ConcordanceSearchDialog.logger1"), e);
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
					ArrayList<IColumn> lstShowColumn = new ArrayList<IColumn>();

					// for (int colNum = 3; colNum < colCount; colNum++) {
					// IColumn column = jTable.getColumn(colNum);
					// jTable.getTableViewState().setColumnVisible(column, true);
					// }
					if (itemLang.getSelection()) {
						// int totalWidth = 0;
						boolean blnIsResetWidth = false;
						// tableModel.getColumnCount()
						for (int index = 1; index < tableModel.getColumnCount(); index++) {
							IColumn column = tableModel.getColumn(index);
							int width = jTable.getTableViewState().getColumnWidth(column);
							if (column.getHeaderLabel().equals(lang) && width == 0) {
								jTable.getTableViewState().setColumnVisible(column, true);
								jTable.updateColumnList();
								jTable.redraw();
								// jTable.getTableViewState().setColumnWidth(column, width0);
								lstShowColumn.add(column);
								blnIsResetWidth = true;
							} else if (width > 0) {
								// totalWidth += width;
								lstShowColumn.add(column);
								// jTable.columnWidthChanged(column, width0);
							}
						}
						if (blnIsResetWidth) {
							int width = totalWidth / lstShowColumn.size() + 1;
							for (int colNum = 1; colNum < jTable.getColumnCount(); colNum++) {
								jTable.getTableViewState().setColumnWidth(jTable.getColumn(colNum), width);
							}
							// for (IColumn column : lstShowColumn) {
							// // jTable.getTableViewState().setColumnVisible(column, true);
							// jTable.getTableViewState().setColumnWidth(column, width);
							// }
						}
						// initGroupIdAndSearch();
					} else {
						// int totalWidth = 0;
						IColumn deleteColumn = null;
						for (int index = 1; index < jTable.getColumnCount(); index++) {
							IColumn column = jTable.getColumn(index);
							int width = jTable.getTableViewState().getColumnWidth(column);
							if (width > 0) {
								// totalWidth += width;
								lstShowColumn.add(column);
							}
							if (column.getHeaderLabel().equals(lang)) {
								deleteColumn = column;
								// 将删除列中的数据清空，以保证行高正常调整
								// for (GridItem item : tableModel.getRowCount()) {
								// item.setText(index, "");
								// }
							}
						}

						// int width = (jTable.getTableViewState().getColumnWidth(deleteColumn) * lstShowColumn.size() -
						// 100)
						// / (lstShowColumn.size() - 1);
						int width = totalWidth / (lstShowColumn.size() - 1) + 1;
						jTable.getTableViewState().setColumnWidth(deleteColumn, 0);
						jTable.getTableViewState().setColumnVisible(deleteColumn, false);
						lstShowColumn.remove(deleteColumn);
						for (IColumn column : lstShowColumn) {
							jTable.getTableViewState().setColumnWidth(column, width);
						}
						// initGroupIdAndSearch();
					}
				}
			});
		}
		btnSelectLang.addListener(SWT.Selection, new Listener() {

			public void handleEvent(Event event) {
				Rectangle rect = btnSelectLang.getBounds();
				Point pt = btnSelectLang.toDisplay(new Point(event.x, event.y + rect.height));
				// Point pt = btnSelectLang.toDisplay(new Point(event.x + rect.width, event.y + rect.height));
				menu.setOrientation(getShell().getOrientation());
				menu.setLocation(pt.x, pt.y);
				menu.setVisible(true);
			}

		});

	}

	/**
	 * 当 str 为 null 或 空串时，返回 * ,否则返回 str
	 * @param str
	 * @return ;
	 */
	private String checkString(String str) {
		return str == null || str.equals("") ? "N/A" : str.replaceAll("'", "''").replaceAll("&", "&&");
	}

	/**
	 * 初始化各控件的监听 ;
	 */
	private void initListener() {
		btnSearch.addSelectionListener(new SelectionListener() {

			public void widgetSelected(SelectionEvent e) {
				initGroupIdAndSearch();
			}

			public void widgetDefaultSelected(SelectionEvent e) {

			}

		});

		cmbSearch.addKeyListener(new KeyListener() {

			public void keyPressed(KeyEvent e) {
				if (e.character == SWT.CR) {
					initGroupIdAndSearch();
				}
			}

			public void keyReleased(KeyEvent e) {

			}

		});

		btnFirst.addSelectionListener(new SelectionListener() {

			public void widgetSelected(SelectionEvent e) {
				if (curPageNum > 0 && amountPage > 0) {
					curPageNum = 1;
					if (search()) {
						refreshPageNumText();
					}
				}
			}

			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});

		btnPre.addSelectionListener(new SelectionListener() {

			public void widgetDefaultSelected(SelectionEvent e) {

			}

			public void widgetSelected(SelectionEvent e) {
				if (curPageNum > 0) {
					curPageNum--;
					if (search()) {
						refreshPageNumText();
					}
				}
			}
		});

		btnNext.addSelectionListener(new SelectionListener() {

			public void widgetDefaultSelected(SelectionEvent e) {

			}

			public void widgetSelected(SelectionEvent e) {
				if (curPageNum < amountPage) {
					curPageNum++;
					if (search()) {
						refreshPageNumText();
					}
				}
			}
		});

		btnLast.addSelectionListener(new SelectionListener() {

			public void widgetDefaultSelected(SelectionEvent e) {

			}

			public void widgetSelected(SelectionEvent e) {
				if (curPageNum > 0 && amountPage > 0) {
					curPageNum = amountPage;
					if (search()) {
						refreshPageNumText();
					}
				}
			}
		});

		txtPage.addKeyListener(new KeyListener() {

			public void keyReleased(KeyEvent e) {

			}

			public void keyPressed(KeyEvent e) {
				if (e.character == SWT.CR) {
					String pageNum = txtPage.getText();
					try {
						curPageNum = Integer.parseInt(pageNum);
					} catch (NumberFormatException e1) {
						// LOGGER.error("NumberFormatException", e1);
						txtPage.setText(String.valueOf(curPageNum) + splitPageSeparator + amountPage);
						return;
					}
					if (curPageNum > amountPage) {
						curPageNum = amountPage;
					}
					if (curPageNum < 1) {
						curPageNum = 1;
					}
					search();
					txtPage.setText(String.valueOf(curPageNum));
				}
			}
		});

		txtPage.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				refreshPageNumText();
			}

			@Override
			public void focusGained(FocusEvent e) {
				txtPage.setText("");
			}
		});
	}

	private void clearTable() {
		int rowCount = tableModel.getRowCount();
		for (int i = 0; i < rowCount; i++) {
			tableModel.remRow(tableModel.getRow(0));
		}
	}

	public void initGroupIdAndSearch() {
		updateHistory(cmbSearch, lstSearchHistory);
		updateHistory(cmbFilter, lstFilterHistory);
		String searchText = cmbSearch.getText();
		if (searchText == null || searchText.trim().equals("")) {
			txtPage.setText("");
			MessageDialog.openInformation(getShell(), Messages.getString("dialog.ConcordanceSearchDialog.msgTitle"),
					Messages.getString("dialog.ConcordanceSearchDialog.msg2"));
			return;
		}

		StringBuffer searchTextBf = new StringBuffer(searchText);
		TreeMap<String, InnerTagBean> tags = InnerTagUtil.parseXmlToDisplayValue(searchTextBf,
				TagStyle.getDefault(false));
		searchText = InnerTagUtil.resolveTag(searchTextBf.toString());
		searchText = InnerTagUtil.escapeTag(searchText);
		searchText = InnerTagUtil.parseDisplayToXmlValue(tags, searchText);

		clearTable();
		mapGroupId.clear();
		curPageNum = 1;
		String[] arrFilter = null;
		String strFilter = cmbFilter.getText();
		if (cmpExpandableFilter.isExpanded() && strFilter != null && !strFilter.equals("")) {
			String srcOrTgt = (String) cmbSrcOrTgt.getData(cmbSrcOrTgt.getText());
			String contain = (String) cmbContain.getData(cmbContain.getText());
			arrFilter = new String[] { srcOrTgt, contain, strFilter };
		}
		ArrayList<String> lstSelLangs = new ArrayList<String>();
		for (MenuItem item : menu.getItems()) {
			if (item.getSelection()) {
				lstSelLangs.add(item.getText());
			}
		}
		List<MetaData> lstMetaData = new ArrayList<MetaData>();
		if (cmbDatabase.getSelectionIndex() == 0) {
			for (DatabaseModelBean model : lstDatabase) {
				lstMetaData.add(model.toDbMetaData());
			}
		} else {
			DatabaseModelBean model = lstDatabase.get(cmbDatabase.getSelectionIndex() - 1);
			MetaData metaData = model.toDbMetaData();
			lstMetaData.add(metaData);
		}
		size = 0;
		for (MetaData metaData : lstMetaData) {
			DBOperator dbOp = DatabaseService.getDBOperator(metaData);
			if(null == dbOp){
				continue;
			}

			try {
				dbOp.start();
				ArrayList<Integer> lstGroupId = dbOp.getConcordanceGroupId(searchText,
						btnIsCaseSensitive.getSelection(), btnApplyRegularExpression.getSelection(),
						btnIsIgnoreMark.getSelection(), strSrcLang, arrFilter, lstSelLangs);
				if (lstGroupId.size() != 0) {
					mapGroupId.put(metaData, lstGroupId);
				}
				size += lstGroupId.size();
			} catch (SQLException e1) {
				LOGGER.error(Messages.getString("dialog.ConcordanceSearchDialog.logger2"), e1);
			} catch (ClassNotFoundException e1) {
				LOGGER.error(Messages.getString("dialog.ConcordanceSearchDialog.logger3"), e1);
			} finally {
				try {
					if (dbOp != null) {
						dbOp.end();
					}
				} catch (SQLException e) {
					LOGGER.error("", e);
				}
			}
		}
		if (size == 0) {
			clearTable();
			txtPage.setText("");
			MessageDialog.openInformation(getShell(), Messages.getString("dialog.ConcordanceSearchDialog.msgTitle"),
					Messages.getString("dialog.ConcordanceSearchDialog.msg1"));
			return;
		}
		amountPage = size / rowNumPerPage;
		if (size % rowNumPerPage != 0) {
			amountPage += 1;
		}
		if (search()) {
			refreshPageNumText();
		}
	}

	/**
	 * 搜索记忆库 ;
	 */
	private boolean search() {
		String searchText = cmbSearch.getText();
		if (searchText == null || searchText.trim().equals("")) {
			txtPage.setText("");
			MessageDialog.openInformation(getShell(), Messages.getString("dialog.ConcordanceSearchDialog.msgTitle"),
					Messages.getString("dialog.ConcordanceSearchDialog.msg2"));
			return false;
		}

		StringBuffer searchTextBf = new StringBuffer(searchText);
		TreeMap<String, InnerTagBean> tags = InnerTagUtil.parseXmlToDisplayValue(searchTextBf,
				TagStyle.getDefault(false));
		searchText = InnerTagUtil.resolveTag(searchTextBf.toString());
		searchText = InnerTagUtil.escapeTag(searchText);
		searchText = InnerTagUtil.parseDisplayToXmlValue(tags, searchText);

		// 此处的判断已加在 ConcordanceSearchHandler 中
		// if (lstDatabase.size() == 0) {
		// txtPage.setText("");
		// MessageDialog.openInformation(getShell(), "提示", "请为项目指定翻译记忆库");
		// return false;
		// }

		ArrayList<String> lstSelLangs = new ArrayList<String>();
		lstSelLangs.add(strSrcLang);
		for (MenuItem item : menu.getItems()) {
			if (item.getSelection()) {
				lstSelLangs.add(item.getText());
			}
		}
		LinkedHashMap<MetaData, List<Integer>> mapSub = getCurPageMap();
		LinkedHashMap<MetaData, List<ConcordanceBean>> mapResult = new LinkedHashMap<MetaData, List<ConcordanceBean>>();
		Iterator<Entry<MetaData, List<Integer>>> iterator = mapSub.entrySet().iterator();
		while (iterator.hasNext()) {
			Entry<MetaData, List<Integer>> entry = (Entry<MetaData, List<Integer>>) iterator.next();
			MetaData metaData = entry.getKey();

			DBOperator dbop = DatabaseService.getDBOperator(metaData);
			try {
				dbop.start();
				List<ConcordanceBean> lstConcordance = dbop.getConcordanceSearchResult(cmbSearch.getText(),
						btnIsCaseSensitive.getSelection(), btnApplyRegularExpression.getSelection(),
						btnIsIgnoreMark.getSelection(), strSrcLang, lstSelLangs, entry.getValue());
				if (lstConcordance != null && lstConcordance.size() > 0) {
					mapResult.put(metaData, lstConcordance);
				}
			} catch (SQLException e1) {
				LOGGER.error(Messages.getString("dialog.ConcordanceSearchDialog.logger2"), e1);
			} catch (ClassNotFoundException e1) {
				LOGGER.error(Messages.getString("dialog.ConcordanceSearchDialog.logger3"), e1);
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
		if (mapResult.size() == 0) {
			clearTable();
			MessageDialog.openInformation(getShell(), Messages.getString("dialog.ConcordanceSearchDialog.msgTitle"),
					Messages.getString("dialog.ConcordanceSearchDialog.msg1"));
			txtPage.setText("");
			return false;
		}
		// 当有多个数据库的数据时要对所有数据按 changedate 重新排序，然后取前 spiResultCount.getSelection() 数量的记录
		int rowNum = 0;
		HashMap<IRow, ArrayList<int[]>> mapStyle = new HashMap<IRow, ArrayList<int[]>>();
		if (mapResult.size() > 1) {
			LinkedHashMap<String, Object[]> map = sortMap(mapResult);
			Iterator<Entry<String, Object[]>> it = map.entrySet().iterator();
			while (it.hasNext()) {
				Entry<String, Object[]> entry = it.next();
				Object[] arrObj = entry.getValue();
				createItem((MetaData) arrObj[0], (ConcordanceBean) arrObj[1], rowNum++, mapStyle);
			}
		} else {
			Iterator<Entry<MetaData, List<ConcordanceBean>>> it = mapResult.entrySet().iterator();
			while (it.hasNext()) {
				Entry<MetaData, List<ConcordanceBean>> entry = it.next();
				MetaData metaData2 = entry.getKey();
				List<ConcordanceBean> lstBean = entry.getValue();
				for (ConcordanceBean bean : lstBean) {
					createItem(metaData2, bean, rowNum++, mapStyle);
				}
				if (lstBean.size() < rowNumPerPage) {
					for (int row = lstBean.size(); row < rowNumPerPage; row++) {
						if (rowNum < tableModel.getRowCount() && tableModel.getRow(rowNum) != null) {
							tableModel.remRow(tableModel.getRow(rowNum));
						}
					}
				}
			}
		}
		jTable.registerCellRenderer(
				tableModel.getColumn(1),
				new StyleTextCellRenderer(searchText, btnIsCaseSensitive.getSelection(), btnApplyRegularExpression
						.getSelection(), createResultsStyle()));
		jTable.registerCellRenderer(tableModel.getColumn(tableModel.getColumnCount() - 1),
				new AttributeTextCellRenderer(mapStyle, style));
		jTable.setTableModel(tableModel);
		return true;
	}

	private LinkedHashMap<MetaData, List<Integer>> getCurPageMap() {
		int startIndex = (curPageNum - 1) * rowNumPerPage;
		int endIndex = curPageNum * rowNumPerPage;
		if (curPageNum == amountPage) {
			endIndex = size;
		}
		Iterator<Entry<MetaData, ArrayList<Integer>>> it = mapGroupId.entrySet().iterator();
		int index = 0;
		LinkedHashMap<MetaData, List<Integer>> mapSub = new LinkedHashMap<MetaData, List<Integer>>();
		while (it.hasNext()) {
			Entry<MetaData, ArrayList<Integer>> entry = (Entry<MetaData, ArrayList<Integer>>) it.next();
			ArrayList<Integer> lstGroupId = entry.getValue();
			if (index <= startIndex && index + lstGroupId.size() >= startIndex) {
				List<Integer> subList = null;
				if (index + lstGroupId.size() - 1 >= endIndex) {
					subList = lstGroupId.subList(startIndex - index, endIndex - index);
					mapSub.put(entry.getKey(), subList);
					break;
				} else {
					subList = lstGroupId.subList(startIndex - index, lstGroupId.size());
					index += lstGroupId.size();
					startIndex += lstGroupId.size();
					mapSub.put(entry.getKey(), subList);
				}
			} else {
				index += lstGroupId.size();
			}
		}
		return mapSub;
	}

	private void refreshPageNumText() {
		if (curPageNum > 0 && amountPage > 0) {
			txtPage.setText(String.valueOf(curPageNum) + splitPageSeparator + amountPage);
		}
	}

	/**
	 * 向表格中添加记录
	 * @param metaData
	 * @param map
	 *            ;
	 */
	private void createItem(MetaData metaData, ConcordanceBean bean, int rowNum,
			HashMap<IRow, ArrayList<int[]>> mapStyle) {
		HashMap<String, String> map = new HashMap<String, String>();

		map.put("id", String.valueOf(bean.getId()));
		map.put("changeDate", bean.getChangeDate());
		map.put("changeId", bean.getChangeId());
		map.put("creationDate", bean.getCreationDate());
		map.put("creationId", bean.getCreationId());
		map.put("flag", bean.isBlnIsFlag() ? "1" : "0");
		for (LanguageTMX lang : bean.getLanguageList()) {
			map.put(lang.getLanguageCode(), lang.getText());
		}
		map.put("dbName", metaData.getDatabaseName());
		map.put("dbType", metaData.getDbType());
		map.put("severName", metaData.getServerName());
		List<TmxProp> lstAttr = bean.getAttributeList();
		StringBuffer sbAttr = new StringBuffer();
		ArrayList<int[]> lstAttrIndex = new ArrayList<int[]>();
		for (TmxProp attr : lstAttr) {
			if (attr.getName() != null && !attr.getName().trim().equals("") && !attr.getName().equals("x-flag")) {
				String attrName = TextUtil.xmlToString(attr.getName());
				int index = attrName.indexOf("::");
				if(index != -1){
					attrName = attrName.substring(index+2, attrName.length());
				}
				sbAttr.append("   ").append(attrName).append(Messages.getString("dialog.ConcordanceSearchDialog.colon"));
				int startIndex = sbAttr.length();
				sbAttr.append(TextUtil.xmlToString(attr.getValue()));
				int endIndex = sbAttr.length();
				lstAttrIndex.add(new int[] { startIndex, endIndex });
			}
		}
		String creationDate = checkString(bean.getCreationDate());
		String creationId = checkString(bean.getCreationId());
		String changeDate = checkString(bean.getChangeDate());
		String changeId = checkString(bean.getChangeId());
		String attribute = MessageFormat.format(strMsg, creationDate, creationId, changeDate, changeId,
				sbAttr.toString());
		ArrayList<int[]> lstFieldIndex = new ArrayList<int[]>();
		int creationStart = attribute.indexOf(creationDate);
		lstFieldIndex.add(new int[] { creationStart, creationStart + creationDate.length() });
		int creationIdStart = attribute.indexOf(creationId, creationStart);
		lstFieldIndex.add(new int[] { creationIdStart, creationIdStart + creationId.length() });
		int changeDateStart = attribute.indexOf(changeDate, creationIdStart);
		lstFieldIndex.add(new int[] { changeDateStart, changeDateStart + changeDate.length() });
		int changeIdStart = attribute.indexOf(changeId, changeDateStart);
		lstFieldIndex.add(new int[] { changeIdStart, changeIdStart + changeId.length() });
		if (lstAttrIndex.size() > 0) {
			int index = attribute.indexOf(sbAttr.toString());
			for (int[] attrIndex : lstAttrIndex) {
				lstFieldIndex.add(new int[] { attrIndex[0] + index, attrIndex[1] + index });
			}
		}

		String source = map.get(strSrcLang) == null ? "" : map.get(strSrcLang);

		StringBuffer sourceBf = new StringBuffer(source);
		TreeMap<String, InnerTagBean> sourceTags = InnerTagUtil.parseXmlToDisplayValue(sourceBf,
				TagStyle.getDefault(false));
		source = InnerTagUtil.resolveTag(sourceBf.toString());
		source = InnerTagUtil.parseDisplayToXmlValue(sourceTags, source);

		String target = map.get(strTgtLang) == null ? "" : map.get(strTgtLang);

		StringBuffer tgtBf = new StringBuffer(target);
		TreeMap<String, InnerTagBean> tgtTags = InnerTagUtil.parseXmlToDisplayValue(tgtBf, TagStyle.getDefault(false));
		target = InnerTagUtil.resolveTag(tgtBf.toString());
		target = InnerTagUtil.parseDisplayToXmlValue(tgtTags, target);

		ArrayList<String> lstTarget = new ArrayList<String>();
		for (String lang : lstLangs) {
			String _tgt = map.get(lang);
			_tgt = _tgt == null ? "" : _tgt;

			StringBuffer _tgtBf = new StringBuffer(_tgt);
			TreeMap<String, InnerTagBean> _tgtTags = InnerTagUtil.parseXmlToDisplayValue(_tgtBf,
					TagStyle.getDefault(false));
			_tgt = InnerTagUtil.resolveTag(_tgtBf.toString());
			_tgt = InnerTagUtil.parseDisplayToXmlValue(_tgtTags, _tgt);

			lstTarget.add(_tgt);
		}
		try {
			if (tableModel.getRow(rowNum) == null) {
				XPropRow row = new XPropRow(bean.isBlnIsFlag(), source, lstTarget, target, attribute);
				row.setDataMap(map);
				if (metaData != null) {
					row.setData("metaData", metaData);
				}
				tableModel.addRow(row);
				mapStyle.put(row, lstFieldIndex);
			} else {
				tableModel.setValue(tableModel.getRow(rowNum), tableModel.getColumn(0), bean.isBlnIsFlag());
				tableModel.setValue(tableModel.getRow(rowNum), tableModel.getColumn(1), source);
				tableModel.setValue(tableModel.getRow(rowNum), tableModel.getColumn(2), target);
				int i = 0;
				for (; i < lstTarget.size(); i++) {
					tableModel.setValue(tableModel.getRow(rowNum), tableModel.getColumn(3 + i), lstTarget.get(i));
				}
				tableModel.setValue(tableModel.getRow(rowNum), tableModel.getColumn(3 + i), attribute);
				XPropRow row = (XPropRow) tableModel.getRow(rowNum);
				row.setDataMap(map);
				if (metaData != null) {
					row.setData("metaData", metaData);
				}
				mapStyle.put(row, lstFieldIndex);
			}
		} catch (Exception e) {
			XPropRow row = new XPropRow(bean.isBlnIsFlag(), source, lstTarget, target, attribute);
			row.setDataMap(map);
			if (metaData != null) {
				row.setData("metaData", metaData);
			}
			tableModel.addRow(row);
			mapStyle.put(row, lstFieldIndex);
		}
	}

	/**
	 * 对搜索结果按修改日期进行排序
	 * @param mapItem
	 * @return ;
	 */
	private LinkedHashMap<String, Object[]> sortMap(LinkedHashMap<MetaData, List<ConcordanceBean>> mapItem) {
		Iterator<Entry<MetaData, List<ConcordanceBean>>> it = mapItem.entrySet().iterator();
		LinkedHashMap<String, Object[]> map = new LinkedHashMap<String, Object[]>();
		while (it.hasNext()) {
			Entry<MetaData, List<ConcordanceBean>> entry = it.next();
			MetaData metaData = entry.getKey();
			List<ConcordanceBean> lstBean = entry.getValue();
			for (ConcordanceBean bean : lstBean) {
				String key = metaData.getDatabaseName() + "_" + bean.getId() + "_" + bean.getChangeDate();
				Object[] arrObj = new Object[] { metaData, bean };
				map.put(key, arrObj);
			}
		}
		// 按最新需求，先不排序
		// ArrayList<Entry<String, Object[]>> entryList = new ArrayList<Entry<String, Object[]>>(map.entrySet());
		// Collections.sort(entryList, new Comparator<Map.Entry<String, Object[]>>() {
		//
		// public int compare(Map.Entry<String, Object[]> arg0, Map.Entry<String, Object[]> arg1) {
		// String key0 = arg0.getKey();
		// String key1 = arg1.getKey();
		//
		// String strDate0 = key0.substring(key0.lastIndexOf("_") + 1);
		// String strDate1 = key1.substring(key1.lastIndexOf("_") + 1);
		// if (!strDate0.equals("") && !strDate1.equals("")) {
		// return strDate1.compareTo(strDate0);
		// } else if (!strDate0.equals("")) {
		// return -1;
		// } else if (!strDate1.equals("")) {
		// return 1;
		// } else {
		// return 0;
		// }
		// }
		// });
		// map.clear();
		// int count = 0;
		// for (Entry<String, Object[]> entry : entryList) {
		// String key = entry.getKey();
		// Object[] obj = entry.getValue();
		// map.put(key, obj);
		// count++;
		// if (rowNumPerPage <= count) {
		// break;
		// }
		// }
		return map;
	}

	protected void createButtonsForButtonBar(Composite parent) {
		// getShell().setDefaultButton(btnSearch);
		Composite content = parent.getParent();
		// parent.dispose();
		content.layout();
	}

	@Override
	public boolean close() {
		if (firstImage != null && !firstImage.isDisposed()) {
			firstImage.dispose();
		}
		if (preImage != null && !preImage.isDisposed()) {
			preImage.dispose();
		}
		if (nextImage != null && !nextImage.isDisposed()) {
			nextImage.dispose();
		}
		if (lastImage != null && !lastImage.isDisposed()) {
			lastImage.dispose();
		}

		if (font != null && !font.isDisposed()) {
			font.dispose();
		}
		writeDialogSettings();
		disposeResultsStyle();
		return super.close();
	};
	private TextStyle createResultsStyle() {
		background = new Color(Display.getCurrent(), 0x19, 0x19, 0x70);
		foreground = new Color(Display.getCurrent(), 0xff, 0xff, 0xff);
		FontData fontData = JFaceResources.getDefaultFont().getFontData()[0];
		fontData.setStyle(fontData.getStyle());
		rsFont = new Font(Display.getDefault(), fontData);
		TextStyle style = new TextStyle(rsFont, foreground, background);
		return style;
	}

	/**
	 * 销毁查询结果样式资源 ;
	 */
	private void disposeResultsStyle() {
		if (background != null && !background.isDisposed()) {
			background.dispose();
		}
		if (foreground != null && !foreground.isDisposed()) {
			foreground.dispose();
		}
		if (rsFont != null && !rsFont.isDisposed()) {
			rsFont.dispose();
		}
	}
	private IDialogSettings getDialogSettings() {
		IDialogSettings settings = Activator.getDefault().getDialogSettings();
		IDialogSettings fDialogSettings = settings.getSection(getClass().getName());
		if (fDialogSettings == null)
			fDialogSettings = settings.addNewSection(getClass().getName());
		return fDialogSettings;
	}

	private void readDialogSettings() {
		IDialogSettings ids = getDialogSettings();
		String[] arrSearchHistory = ids
				.getArray("net.heartsome.cat.database.ui.tm.dialog.ConcordanceSearchDialog.searchHistory");
		if (arrSearchHistory != null) {
			lstSearchHistory.clear();
			for (int i = 0; i < arrSearchHistory.length; i++) {
				lstSearchHistory.add(arrSearchHistory[i]);
			}
		}

		btnIsCaseSensitive.setSelection(ids
				.getBoolean("net.heartsome.cat.database.ui.tm.dialog.ConcordanceSearchDialog.caseSensitive"));
		btnIsIgnoreMark.setSelection(!ids
				.getBoolean("net.heartsome.cat.database.ui.tm.dialog.ConcordanceSearchDialog.ignoreMark"));
		btnApplyRegularExpression.setSelection(ids
				.getBoolean("net.heartsome.cat.database.ui.tm.dialog.ConcordanceSearchDialog.regEx"));

		String selTB = ids.get("net.heartsome.cat.database.ui.tm.dialog.ConcordanceSearchDialog.selTB");
		int selIndex = 0;
		if (selTB != null) {
			for (int i = 0; i < cmbDatabase.getItemCount(); i++) {
				if (selTB.equals(cmbDatabase.getItem(i))) {
					selIndex = i;
					break;
				}
			}
		}
		cmbDatabase.select(selIndex);

		String[] arrTarget = ids.getArray("net.heartsome.cat.database.ui.tm.dialog.ConcordanceSearchDialog.selTgt");
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

		List<IColumn> lstShowColumn = new ArrayList<IColumn>();
		boolean blnIsResetWidth = false;
		for (int index = 1; index < tableModel.getColumnCount(); index++) {
			IColumn column = tableModel.getColumn(index);
			int width = jTable.getTableViewState().getColumnWidth(column);
			if (lstSelItem.indexOf(column.getHeaderLabel()) != -1 && width == 0) {
				jTable.getTableViewState().setColumnVisible(column, true);
				jTable.updateColumnList();
				jTable.redraw();
				lstShowColumn.add(column);
				blnIsResetWidth = true;
			} else if (width > 0) {
				lstShowColumn.add(column);
			}
		}
		if (blnIsResetWidth) {
			int width = totalWidth / lstShowColumn.size() + 1;
			for (int colNum = 1; colNum < jTable.getColumnCount(); colNum++) {
				jTable.getTableViewState().setColumnWidth(jTable.getColumn(colNum), width);
			}
		}

		cmpExpandableFilter.setExpanded(ids
				.getBoolean("net.heartsome.cat.database.ui.tm.dialog.ConcordanceSearchDialog.isExpandFilter"));
		try {
			cmbSrcOrTgt.select(ids
					.getInt("net.heartsome.cat.database.ui.tm.dialog.ConcordanceSearchDialog.selSrcOrTgt"));
		} catch (NumberFormatException e) {
			cmbSrcOrTgt.select(1);
		}
		try {
			cmbContain.select(ids.getInt("net.heartsome.cat.database.ui.tm.dialog.ConcordanceSearchDialog.contain"));
		} catch (NumberFormatException e) {
			cmbContain.select(1);
		}

		String[] arrFilterHistory = ids
				.getArray("net.heartsome.cat.database.ui.tm.dialog.ConcordanceSearchDialog.filterHistory");
		if (arrFilterHistory != null) {
			lstFilterHistory.clear();
			for (int i = 0; i < arrFilterHistory.length; i++) {
				lstFilterHistory.add(arrFilterHistory[i]);
			}
		}

		layoutExpandable();
	}

	private void writeDialogSettings() {
		IDialogSettings ids = getDialogSettings();
		if (okToUse(cmbSearch)) {
			String searchString = cmbSearch.getText();
			if (searchString.length() > 0) {
				lstSearchHistory.add(0, searchString);
			}
			writeHistory(lstSearchHistory, ids,
					"net.heartsome.cat.database.ui.tm.dialog.ConcordanceSearchDialog.searchHistory");
		}
		ids.put("net.heartsome.cat.database.ui.tm.dialog.ConcordanceSearchDialog.caseSensitive",
				btnIsCaseSensitive.getSelection());
		ids.put("net.heartsome.cat.database.ui.tm.dialog.ConcordanceSearchDialog.ignoreMark",
				!btnIsIgnoreMark.getSelection());
		ids.put("net.heartsome.cat.database.ui.tm.dialog.ConcordanceSearchDialog.regEx",
				btnApplyRegularExpression.getSelection());

		ids.put("net.heartsome.cat.database.ui.tm.dialog.ConcordanceSearchDialog.selTB", cmbDatabase.getText());

		List<String> lstTgt = new ArrayList<String>();
		for (MenuItem item : menu.getItems()) {
			if (item.getSelection()) {
				lstTgt.add(item.getText());
			}
		}
		String[] arrTgt = new String[lstTgt.size()];
		lstTgt.toArray(arrTgt);
		ids.put("net.heartsome.cat.database.ui.tm.dialog.ConcordanceSearchDialog.selTgt", arrTgt);

		ids.put("net.heartsome.cat.database.ui.tm.dialog.ConcordanceSearchDialog.isExpandFilter",
				cmpExpandableFilter.isExpanded());
		ids.put("net.heartsome.cat.database.ui.tm.dialog.ConcordanceSearchDialog.selSrcOrTgt",
				cmbSrcOrTgt.getSelectionIndex());
		ids.put("net.heartsome.cat.database.ui.tm.dialog.ConcordanceSearchDialog.contain",
				cmbContain.getSelectionIndex());
		if (okToUse(cmbFilter)) {
			String filterString = cmbFilter.getText();
			if (filterString.length() > 0) {
				lstFilterHistory.add(0, filterString);
			}
			writeHistory(lstFilterHistory, ids,
					"net.heartsome.cat.database.ui.tm.dialog.ConcordanceSearchDialog.filterHistory");
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

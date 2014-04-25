/**
 * ExportTbxDialog.java
 *
 * Version information :
 *
 * Date:Dec 7, 2011
 *
 * Copyright notice :
 * 本文件及其附带的相关文件包含机密信息，仅限瀚特盛科技有限公司指定的，与本项目有关的内部人员和客户联络人员查阅和使用。 
 * 如果您不是本保密声明中指定的收件者，请立即销毁本文件，禁止对本文件或根据本文件中的内容采取任何其他行动， 
 * 包括但不限于：泄露本文件中的信息、以任何方式制作本文件全部或部分内容之副本、将本文件或其相关副本提供给任何其他人。
 */
package net.heartsome.cat.database.ui.tb.dialog;

import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.heartsome.cat.common.bean.DatabaseModelBean;
import net.heartsome.cat.common.locale.LocaleService;
import net.heartsome.cat.common.ui.HSDropDownButton;
import net.heartsome.cat.common.util.CommonFunction;
import net.heartsome.cat.database.bean.ExportDatabaseBean;
import net.heartsome.cat.database.bean.ExportFilterBean;
import net.heartsome.cat.database.service.DatabaseService;
import net.heartsome.cat.database.ui.core.ExportFilterStoreConfiger;
import net.heartsome.cat.database.ui.dialog.ExportFilterSettingDialog;
import net.heartsome.cat.database.ui.tb.Utils;
import net.heartsome.cat.database.ui.tb.resource.Messages;
import net.heartsome.cat.document.ExportAbstract;
import net.heartsome.cat.document.ExportTbxImpl;
import net.heartsome.cat.ts.util.ProgressIndicatorManager;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TrayDialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.PlatformUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 从库中导出为TBX文件
 * @author Jason
 * @version
 * @since JDK1.6
 */
public class ExportTbxDialog extends TrayDialog {
	Logger logger = LoggerFactory.getLogger(ExportTbxDialog.class);
	private TableViewer dbListViewer;
	private ListViewer hasSelLangListViewer;
	private ComboViewer srcLangcomboViewer;
	private Button hasChangedCodingCbtn;
	private ComboViewer encodingComboViewer;
	private Button hasFilterChangedBtn;
	private ComboViewer filterComboViewer;
	private Button filterSettingBtn;
	private Button filterDeleteBtn;
	private Text tbxFileText;
	private Button browserBtn;
	private Button deleteDbBtn;
	private List<ExportFilterBean> filterList; // 过滤规则

	private List<ExportDatabaseBean> dbList;
	private ExportDatabaseBean currentDatabase;
	private String[] pageCodes;

	private ExportFilterStoreConfiger filterStore;

	/**
	 * Create the dialog.
	 * @param parentShell
	 */
	public ExportTbxDialog(Shell parentShell) {
		super(parentShell);
		this.dbList = new ArrayList<ExportDatabaseBean>();
		this.pageCodes = LocaleService.getPageCodes();
		this.filterStore = new ExportFilterStoreConfiger();
		this.initFilterStore();
		setHelpAvailable(true);
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(Messages.getString("dialog.ExportTbxDialog.title"));
	}

	/**
	 * 添加帮助按钮 robert 2012-09-06
	 */
	@Override
	protected Control createHelpControl(Composite parent) {
		// ROBERTHELP 导出 tbx
		String language = CommonFunction.getSystemLanguage();
		final String helpUrl = MessageFormat.format("/net.heartsome.cat.ts.ui.help/html/{0}/ch06s05.html#export-tbx",
				language);
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

	/**
	 * Create contents of the dialog.
	 * @param parent
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		container.setLayout(new GridLayout(1, false));

		Group dbListGroup = new Group(container, SWT.NONE);
		GridLayout glDbListGroup = new GridLayout(2, false);
		glDbListGroup.horizontalSpacing = 0;
		glDbListGroup.marginHeight = 0;
		glDbListGroup.marginWidth = 0;
		dbListGroup.setLayout(glDbListGroup);
		dbListGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		dbListGroup.setText(Messages.getString("dialog.ExportTbxDialog.dbListGroup"));

		Composite leftComposite = new Composite(dbListGroup, SWT.NONE);
		GridLayout glLeftComposite = new GridLayout(1, false);
		glLeftComposite.verticalSpacing = 0;
		glLeftComposite.marginHeight = 0;
		leftComposite.setLayout(glLeftComposite);
		leftComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));

		// 列表和语言设置
		Composite dbListComposite = new Composite(leftComposite, SWT.NONE);
		GridLayout glTopLeftComposite = new GridLayout(1, false);
		glTopLeftComposite.marginHeight = 0;
		glTopLeftComposite.marginWidth = 0;
		dbListComposite.setLayout(glTopLeftComposite);
		dbListComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		dbListViewer = new TableViewer(dbListComposite, SWT.BORDER | SWT.FULL_SELECTION);
		Table table = dbListViewer.getTable();
		GridData gd_table = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd_table.heightHint = 100;
		table.setLayoutData(gd_table);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		dbListViewer.setContentProvider(new ArrayContentProvider());
		dbListViewer.setInput(dbList);
		createColumn(dbListViewer);
		dbListViewer.addSelectionChangedListener(new ISelectionChangedListener() {

			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection selection = (IStructuredSelection) event.getSelection();
				currentDatabase = (ExportDatabaseBean) selection.getFirstElement();
				loadData();
			}
		});

		Composite langSetComposite = new Composite(leftComposite, SWT.NONE);
		GridLayout gl_langSetComposite = new GridLayout(2, false);
		gl_langSetComposite.marginWidth = 0;
		langSetComposite.setLayout(gl_langSetComposite);
		langSetComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

		Label langSelLabel = new Label(langSetComposite, SWT.NONE);
		langSelLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		langSelLabel.setText(Messages.getString("dialog.ExportTbxDialog.langSelLabel"));

		hasSelLangListViewer = new ListViewer(langSetComposite, SWT.BORDER | SWT.V_SCROLL | SWT.MULTI);
		org.eclipse.swt.widgets.List list = hasSelLangListViewer.getList();
		GridData glLangList = new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1);
		glLangList.heightHint = 76;
		list.setLayoutData(glLangList);
		hasSelLangListViewer.setContentProvider(new ArrayContentProvider());
		hasSelLangListViewer.addSelectionChangedListener(new ISelectionChangedListener() {

			@SuppressWarnings("unchecked")
			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection sel = (IStructuredSelection) event.getSelection();
				if (sel.isEmpty()) {
					return;
				}
				if (currentDatabase != null) {
					currentDatabase.getHasSelectedLangs().clear();
					currentDatabase.getHasSelectedLangs().addAll(sel.toList());

					List<String> canSelSrcLangs = new ArrayList<String>();
					canSelSrcLangs.addAll(sel.toList());

					currentDatabase.setCanSelSrcLangs(canSelSrcLangs);
					srcLangcomboViewer.setInput(canSelSrcLangs);
					if (canSelSrcLangs.contains(currentDatabase.getSrcLang())) {
						String srcLang = currentDatabase.getSrcLang();
						if (srcLang != null && !srcLang.equals("")) {
							for (int i = 0; i < canSelSrcLangs.size(); i++) {
								if (canSelSrcLangs.get(i).equals(srcLang)) {
									srcLangcomboViewer.getCombo().select(i);
									break;
								}
							}
						}
					} else {
						srcLangcomboViewer.getCombo().select(0);
					}

				}
			}
		});

		Label srcLangSelLabel = new Label(langSetComposite, SWT.NONE);
		srcLangSelLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		srcLangSelLabel.setBounds(0, 0, 79, 19);
		srcLangSelLabel.setText(Messages.getString("dialog.ExportTbxDialog.srcLangSelLabel"));

		srcLangcomboViewer = new ComboViewer(langSetComposite, SWT.NONE | SWT.READ_ONLY);
		Combo combo = srcLangcomboViewer.getCombo();
		GridData gd_combo = new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1);
		gd_combo.widthHint = 197;
		combo.setLayoutData(gd_combo);
		srcLangcomboViewer.setContentProvider(new ArrayContentProvider());
		srcLangcomboViewer.addSelectionChangedListener(new ISelectionChangedListener() {

			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection sel = (IStructuredSelection) event.getSelection();
				if (sel.isEmpty()) {
					return;
				}
				if (currentDatabase != null) {
					currentDatabase.setSrcLang((String) sel.getFirstElement());
				}
			}
		});
		// 操作库列的按钮区域
		Composite rightComposite = new Composite(dbListGroup, SWT.NONE);
		GridLayout gl_rightComposite = new GridLayout(1, false);
		gl_rightComposite.marginRight = 5;
		gl_rightComposite.marginHeight = 0;
		gl_rightComposite.marginWidth = 0;
		rightComposite.setLayout(gl_rightComposite);
		rightComposite.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));

		HSDropDownButton addBtn = new HSDropDownButton(rightComposite, SWT.NONE);
		addBtn.setText(Messages.getString("dialog.ExportTbxDialog.AddDbBtn"));
		Menu addMenu = addBtn.getMenu();
		MenuItem item = new MenuItem(addMenu, SWT.PUSH);
		item.setText(Messages.getString("tb.dialog.addTb.DropDownButton.AddFileTb"));
		item.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent e) {
				FileDialog fileDialg = new FileDialog(getShell());
				fileDialg.setFilterExtensions(new String[] { "*.hstb", "*.*" });
				String result = fileDialg.open();
				if (result == null) {
					return;
				}
				File f = new File(result);
				if (!f.exists()) {
					return;
				}
				Map<DatabaseModelBean, String> r = null;
				try {
					r = Utils.convertFile2TbModel(f, true);
				} catch (Exception e1) {
					MessageDialog.openError(getShell(), Messages.getString("tb.dialog.addFileTb.errorTitle"),
							e1.getMessage());
				}
				if (r == null) {
					return;
				}
				Iterator<DatabaseModelBean> it = r.keySet().iterator();
				if (it.hasNext()) {
					DatabaseModelBean selectedVal = it.next();
					ExportDatabaseBean bean = new ExportDatabaseBean(selectedVal.toDbMetaData(), r.get(selectedVal));
					if (!dbList.contains(bean)) { // 实现: 重写equals方法
						dbList.add(bean);
						bean.setIndex(dbList.size() + "");
					}
					dbListViewer.getTable().removeAll();
					dbListViewer.setInput(dbList);
					if (dbList.size() != 0) {
						deleteDbBtn.setEnabled(true);
						browserBtn.setEnabled(true);
						selectCurrentDb(currentDatabase);
					} else {
						deleteDbBtn.setEnabled(false);
						browserBtn.setEnabled(false);
					}
				}
			}
		});
		MenuItem serverItem = new MenuItem(addMenu, SWT.PUSH);
		serverItem.setText(Messages.getString("tb.dialog.addTb.DropDownButton.AddServerTb"));
		serverItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				TermDbManagerDialog dialog = new TermDbManagerDialog(getShell());
				dialog.setDialogUseFor(TermDbManagerDialog.TYPE_DBSELECTED);

				if (dialog.open() == Window.OK) {
					Map<DatabaseModelBean, String> selDb = dialog.getHasSelectedDatabase();
					Iterator<Entry<DatabaseModelBean, String>> entryIt = selDb.entrySet().iterator();
					while (entryIt.hasNext()) {
						Entry<DatabaseModelBean, String> entry = entryIt.next();
						ExportDatabaseBean bean = new ExportDatabaseBean(entry.getKey().toDbMetaData(), entry
								.getValue());
						if (!dbList.contains(bean)) { // 实现: 重写equals方法
							dbList.add(bean);
							bean.setIndex(dbList.size() + "");
						}
					}
					dbListViewer.getTable().removeAll();
					dbListViewer.setInput(dbList);
				}

				if (dbList.size() != 0) {
					deleteDbBtn.setEnabled(true);
					browserBtn.setEnabled(true);
					selectCurrentDb(currentDatabase);
				} else {
					deleteDbBtn.setEnabled(false);
					browserBtn.setEnabled(false);
				}
			}
		});

		deleteDbBtn = new Button(rightComposite, SWT.NONE);
		deleteDbBtn.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		deleteDbBtn.setBounds(0, 0, 104, 31);
		deleteDbBtn.setText(Messages.getString("dialog.ExportTbxDialog.deleteDbBtn"));
		deleteDbBtn.setEnabled(false);
		deleteDbBtn.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				IStructuredSelection sel = (IStructuredSelection) dbListViewer.getSelection();
				if (sel.isEmpty()) {
					MessageDialog.openInformation(getShell(), Messages.getString("dialog.ExportTbxDialog.msgTitle"),
							Messages.getString("dialog.ExportTbxDialog.msg1"));
					return;
				}
				dbList.removeAll(sel.toList());
				dbListViewer.remove(sel.toArray());
				if (dbList.size() != 0) {
					deleteDbBtn.setEnabled(true);
					browserBtn.setEnabled(true);
					selectCurrentDb(currentDatabase);
				} else {
					currentDatabase = null;
					deleteDbBtn.setEnabled(false);
					browserBtn.setEnabled(false);
				}
			}
		});

		Composite tbxTemplateComp = new Composite(container, SWT.NONE);
		GridLayout glTbxTemplateComp = new GridLayout(1, false);
		glTbxTemplateComp.marginWidth = 0;
		glTbxTemplateComp.marginHeight = 0;
		tbxTemplateComp.setLayout(glTbxTemplateComp);

		Composite encodingComposite = new Composite(container, SWT.NONE);
		GridLayout glEncodingComposite = new GridLayout(2, false);
		glEncodingComposite.marginWidth = 0;
		glEncodingComposite.marginHeight = 0;
		encodingComposite.setLayout(glEncodingComposite);
		encodingComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		hasChangedCodingCbtn = new Button(encodingComposite, SWT.CHECK);
		hasChangedCodingCbtn.setText(Messages.getString("dialog.ExportTbxDialog.hasChangedCodingCbtn"));
		hasChangedCodingCbtn.setSelection(false);
		hasChangedCodingCbtn.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				encodingComboViewer.getCombo().setEnabled(hasChangedCodingCbtn.getSelection());
			}
		});
		encodingComboViewer = new ComboViewer(encodingComposite, SWT.NONE | SWT.READ_ONLY);
		Combo encodingCombo = encodingComboViewer.getCombo();
		GridData gdEncodingCombo = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
		gdEncodingCombo.widthHint = 279;
		encodingCombo.setLayoutData(gdEncodingCombo);
		encodingCombo.setEnabled(hasChangedCodingCbtn.getSelection());
		encodingComboViewer.setContentProvider(new ArrayContentProvider());
		encodingComboViewer.setInput(pageCodes);

		Composite filterComposite = new Composite(container, SWT.NONE);
		filterComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		GridLayout glFilterComposite = new GridLayout(4, false);
		glFilterComposite.marginWidth = 0;
		glFilterComposite.marginHeight = 0;
		filterComposite.setLayout(glFilterComposite);

		hasFilterChangedBtn = new Button(filterComposite, SWT.CHECK);
		hasFilterChangedBtn.setText(Messages.getString("dialog.ExportTbxDialog.button"));
		hasFilterChangedBtn.setSelection(false);
		hasFilterChangedBtn.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				filterComboViewer.getCombo().setEnabled(hasFilterChangedBtn.getSelection());
				filterSettingBtn.setEnabled(hasFilterChangedBtn.getSelection());
				filterDeleteBtn.setEnabled(hasFilterChangedBtn.getSelection());
			}
		});

		filterComboViewer = new ComboViewer(filterComposite, SWT.NONE);
		Combo filterCombo = filterComboViewer.getCombo();
		filterCombo.setEnabled(false);
		filterCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		filterComboViewer.setContentProvider(new ArrayContentProvider());
		filterComboViewer.setLabelProvider(new FilterLabelProvider());
		filterComboViewer.setInput(filterList);
		filterCombo.select(0); // 有一个空的过滤器

		filterSettingBtn = new Button(filterComposite, SWT.NONE);
		filterSettingBtn.setText(Messages.getString("dialog.ExportTbxDialog.filterSettingBtn"));
		filterSettingBtn.setEnabled(false);
		filterSettingBtn.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				IStructuredSelection sel = (IStructuredSelection) filterComboViewer.getSelection();
				ExportFilterBean bean = (ExportFilterBean) sel.getFirstElement();
				if (bean.equals(filterList.get(0))) { // 0位置的始终存在 Empty
					// 新建
					filterSetting(null);
				} else {
					// 编辑
					filterSetting(bean);
				}
			}
		});

		filterDeleteBtn = new Button(filterComposite, SWT.NONE);
		filterDeleteBtn.setText(Messages.getString("dialog.ExportTbxDialog.filterDeleteBtn"));
		filterDeleteBtn.setEnabled(false);
		filterDeleteBtn.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				IStructuredSelection sel = (IStructuredSelection) filterComboViewer.getSelection();
				ExportFilterBean bean = (ExportFilterBean) sel.getFirstElement();
				if (bean.equals(filterList.get(0))) { // 总是存在一个空的filter,显示为"无"
					return;
				}

				if (MessageDialog.openConfirm(getShell(), Messages.getString("dialog.ExportTbxDialog.msgTitle"),
						Messages.getString("dialog.ExportTbxDialog.msg2"))) {
					filterStore.deleteFilterRuleByName(bean.getFilterName(), "TBX");
					int i = filterList.indexOf(bean);
					filterList.remove(i);
					filterComboViewer.setInput(filterList);
					filterComboViewer.getCombo().select(0);
				}
			}
		});

		Composite tbxFileSetComposite = new Composite(container, SWT.NONE);
		GridLayout glTbxFileSetComposite = new GridLayout(3, false);
		glTbxFileSetComposite.marginWidth = 0;
		glTbxFileSetComposite.marginHeight = 0;
		tbxFileSetComposite.setLayout(glTbxFileSetComposite);
		tbxFileSetComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		Label tbxFileLabel = new Label(tbxFileSetComposite, SWT.NONE);
		tbxFileLabel.setText(Messages.getString("dialog.ExportTbxDialog.tbxFileLabel"));

		tbxFileText = new Text(tbxFileSetComposite, SWT.BORDER);
		tbxFileText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		tbxFileText.setEnabled(false);

		browserBtn = new Button(tbxFileSetComposite, SWT.NONE);
		browserBtn.setText(Messages.getString("dialog.ExportTbxDialog.browserBtn"));
		browserBtn.setEnabled(false);
		browserBtn.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (dbList.size() > 1) {
					DirectoryDialog dlg = new DirectoryDialog(getParentShell());
					String filePath = dlg.open();
					if (filePath != null) {
						tbxFileText.setText(filePath);
					}
				} else {
					FileDialog dlg = new FileDialog(getShell(), SWT.SAVE);
					String[] filterExt = { "*.tbx" };
					dlg.setFilterExtensions(filterExt);
					String filePath = dlg.open();
					if (filePath != null) {
						tbxFileText.setText(filePath);
					}
				}
			}
		});

		return container;
	}

	/**
	 * 选择当前选中的数据库 ;
	 */
	private void selectCurrentDb(ExportDatabaseBean bean) {
		if (bean != null) {
			dbListViewer.setSelection(new StructuredSelection(bean));
		} else {
			if (dbList.size() != 0) {
				dbListViewer.setSelection(new StructuredSelection(dbList.get(0)));
			}
		}
		dbListViewer.getTable().setFocus();
	}

	private void filterSetting(ExportFilterBean filter) {

		ExportFilterSettingDialog filterDlg = new ExportFilterSettingDialog(getShell(), "TBX");
		filterDlg.setFilterStore(filterStore); // 两个对话框共用一个store
		if (filter != null) { // 编辑当前过滤条件
			filterDlg.setCurrentFilter(filter);
			if (filterDlg.open() == Window.OK) {
				int i = filterList.indexOf(filter);
				filterList.remove(i);
				filterList.add(i, filterDlg.getSettingResult());
				filterComboViewer.setInput(filterList);
				filterComboViewer.getCombo().select(i);
			}
		} else {
			if (filterDlg.open() == Window.OK) {
				filterList.add(1, filterDlg.getSettingResult());
				filterComboViewer.setInput(filterList);
				filterComboViewer.getCombo().select(1);
			}
		}
	}

	/**
	 * 初始化过滤器列表 ;
	 */
	private void initFilterStore() {
		this.filterList = filterStore.getFilterRule("TBX");
		ExportFilterBean empty = new ExportFilterBean();
		empty.setFilterName(Messages.getString("dialog.ExportTbxDialog.empty"));
		filterList.add(0, empty);
	}

	/**
	 * 加载语言数据 ;
	 */
	private void loadData() {
		if (currentDatabase == null) { // 清理数据
			hasSelLangListViewer.getList().removeAll();
			srcLangcomboViewer.getCombo().removeAll();
			encodingComboViewer.getCombo().setText("");
			return;
		}

		// 加载所有语言
		hasSelLangListViewer.setInput(currentDatabase.getExistLangs().split(","));
		hasSelLangListViewer.setSelection(new StructuredSelection(currentDatabase.getHasSelectedLangs()));

		// 加载源语言;
		srcLangcomboViewer.setInput(currentDatabase.getCanSelSrcLangs());
		srcLangcomboViewer.setSelection(new StructuredSelection(currentDatabase.getCanSelSrcLangs().get(0)));

		// 加载编码列表,默认为UTF-8
		encodingComboViewer.setSelection(new StructuredSelection("UTF-8"));

	}

	/**
	 * Create contents of the button bar.
	 * @param parent
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, Messages.getString("dialog.ExportTbxDialog.ok"), true);
		createButton(parent, IDialogConstants.CANCEL_ID, Messages.getString("dialog.ExportTbxDialog.cancel"), false);
	}

	/**
	 * 创建Table列
	 * @param viewer
	 *            ;
	 */
	private void createColumn(final TableViewer viewer) {
		String[] clmnTitles = { Messages.getString("dialog.ExportTbxDialog.clmnTitles1"),
				Messages.getString("dialog.ExportTbxDialog.clmnTitles2"),
				Messages.getString("dialog.ExportTbxDialog.clmnTitles3"),
				Messages.getString("dialog.ExportTbxDialog.clmnTitles4") };
		int[] clmnBounds = { 50, 80, 100, 100 };

		TableViewerColumn col = createTableViewerColumn(viewer, clmnTitles[0], clmnBounds[0], 0);
		col.setLabelProvider(new ColumnLabelProvider() {

			public String getText(Object element) {
				ExportDatabaseBean bean = (ExportDatabaseBean) element;
				return bean.getIndex();
			}
		});

		col = createTableViewerColumn(viewer, clmnTitles[1], clmnBounds[1], 1);
		col.setLabelProvider(new ColumnLabelProvider() {

			public String getText(Object element) {
				ExportDatabaseBean bean = (ExportDatabaseBean) element;
				return bean.getDbBean().getDbType();
			}
		});

		col = createTableViewerColumn(viewer, clmnTitles[2], clmnBounds[2], 2);
		col.setLabelProvider(new ColumnLabelProvider() {

			public String getText(Object element) {
				ExportDatabaseBean bean = (ExportDatabaseBean) element;
				return bean.getDbBean().getDatabaseName();
			}
		});
		col = createTableViewerColumn(viewer, clmnTitles[3], clmnBounds[3], 3);
		col.setLabelProvider(new ColumnLabelProvider() {

			public String getText(Object element) {
				ExportDatabaseBean bean = (ExportDatabaseBean) element;
				return bean.getExistLangs();
			}
		});

	}

	/**
	 * 设置TableViewer 列属性
	 * @param viewer
	 * @param title
	 *            列标题
	 * @param bound
	 *            列宽
	 * @param colNumber
	 *            列序号
	 * @return {@link TableViewerColumn};
	 */
	private TableViewerColumn createTableViewerColumn(TableViewer viewer, String title, int bound, final int colNumber) {
		final TableViewerColumn viewerColumn = new TableViewerColumn(viewer, SWT.NONE | SWT.Resize);
		final TableColumn column = viewerColumn.getColumn();
		column.setText(title);
		column.setWidth(bound);
		column.setResizable(true);
		column.setMoveable(true);
		return viewerColumn;
	}

	/**
	 * Return the initial size of the dialog.
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(584, 559);
	}

	private final class FilterLabelProvider extends LabelProvider {

		@Override
		public Image getImage(Object element) {
			return null;
		}

		@Override
		public String getText(Object element) {
			if (element instanceof ExportFilterBean) {
				return ((ExportFilterBean) element).getFilterName();
			}
			return null;
		}
	}

	@Override
	protected void okPressed() {
		String encoding = "UTF-8";
		if (hasChangedCodingCbtn.getSelection()) {
			encoding = this.encodingComboViewer.getCombo().getText();
		}
		String exportPath = this.tbxFileText.getText();
		ExportFilterBean filterBean = null;
		if (hasFilterChangedBtn.getSelection()) {
			IStructuredSelection sel = (IStructuredSelection) filterComboViewer.getSelection();
			filterBean = (ExportFilterBean) sel.getFirstElement();
			if (filterBean.equals(filterList.get(0))) {
				filterBean = null;
			}
		}
		if (this.dbList.size() == 0) {
			MessageDialog.openInformation(getShell(), Messages.getString("dialog.ExportTbxDialog.msgTitle"),
					Messages.getString("dialog.ExportTbxDialog.msg3"));
			return;
		}
		for (Iterator<ExportDatabaseBean> iterator = dbList.iterator(); iterator.hasNext();) {
			ExportDatabaseBean dbBean = iterator.next();
			if (dbBean.getHasSelectedLangs().size() < 2) {
				String dbType = dbBean.getDbBean().getDbType();
				String name = dbBean.getDbBean().getDatabaseName();
				MessageDialog.openInformation(getShell(), Messages.getString("dialog.ExportTbxDialog.msgTitle"),
						MessageFormat.format(Messages.getString("dialog.ExportTbxDialog.msg4"), dbType, name));
				return;
			}
		}

		if (exportPath == null || exportPath.equals("")) {
			MessageDialog.openInformation(getShell(), Messages.getString("dialog.ExportTbxDialog.msgTitle"),
					Messages.getString("dialog.ExportTbxDialog.msg5"));
			return;
		}

		if (this.dbList.size() > 1) {
			File f = new File(exportPath);
			if (!f.isDirectory()) {
				MessageDialog.openInformation(getShell(), Messages.getString("dialog.ExportTbxDialog.msgTitle"),
						Messages.getString("dialog.ExportTbxDialog.msg7"));
				return;
			}
		}

		if (this.dbList.size() == 1) {
			File f = new File(exportPath);
			if (f.isDirectory()) {
				MessageDialog.openInformation(getShell(), Messages.getString("dialog.ExportTbxDialog.msgTitle"),
						Messages.getString("dialog.ExportTbxDialog.msg8"));
				return;
			}
		}

		if (this.dbList.size() == 1) {
			dbList.get(0).setExportFilePath(exportPath);
			File file = new File(exportPath);
			if (file.exists()) {
				if (!MessageDialog.openConfirm(getShell(), Messages.getString("dialog.ExportTbxDialog.msgTitle"),
						MessageFormat.format(Messages.getString("dialog.ExportTbxDialog.msg6"), exportPath))) {
					return;
				}
			}
		} else {
			for (Iterator<ExportDatabaseBean> iterator = dbList.iterator(); iterator.hasNext();) {
				ExportDatabaseBean db = iterator.next();
				String databaseName = db.getDbBean().getDatabaseName();
				String path = exportPath + System.getProperty("file.separator") + databaseName
						+ db.getDbBean().getDbType() + ".tbx";
				File file = new File(path);
				if (file.exists()) {
					if (!MessageDialog.openConfirm(getShell(), Messages.getString("dialog.ExportTbxDialog.msgTitle"),
							MessageFormat.format(Messages.getString("dialog.ExportTbxDialog.msg6"), path))) {
						return;
					}
				}
				db.setExportFilePath(path);
			}
		}
		final ExportAbstract exportor = new ExportTbxImpl(this.dbList, filterBean, encoding);
		Job job = new Job(Messages.getString("dialog.ExportTbxDialog.job")) {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				final String result = DatabaseService.executeExport(exportor, monitor);
				Display.getDefault().syncExec(new Runnable() {
					public void run() {
						MessageDialog.openInformation(getShell(),
								Messages.getString("dialog.ExportTbxDialog.msgTitle"), result);
					}
				});
				return Status.OK_STATUS;
			}
		};
		
		// 当程序退出时，检测当前　job 是否正常关闭
		CommonFunction.jobCantCancelTip(job);
		job.addJobChangeListener(new JobChangeAdapter() {
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
		super.okPressed();
	}
}

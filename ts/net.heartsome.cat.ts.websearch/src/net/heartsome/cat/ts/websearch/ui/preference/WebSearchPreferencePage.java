/**
 * WebSearchPreferencePage.java
 *
 * Version information :
 *
 * Date:2013-9-18
 *
 * Copyright notice :
 * 本文件及其附带的相关文件包含机密信息，仅限瀚特盛科技有限公司指定的，与本项目有关的内部人员和客户联络人员查阅和使用。 
 * 如果您不是本保密声明中指定的收件者，请立即销毁本文件，禁止对本文件或根据本文件中的内容采取任何其他行动， 
 * 包括但不限于：泄露本文件中的信息、以任何方式制作本文件全部或部分内容之副本、将本文件或其相关副本提供给任何其他人。
 */
package net.heartsome.cat.ts.websearch.ui.preference;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import net.heartsome.cat.common.ui.HsImageLabel;
import net.heartsome.cat.ts.websearch.Activator;
import net.heartsome.cat.ts.websearch.bean.SearchEntry;
import net.heartsome.cat.ts.websearch.config.WebSearchPreferencStore;
import net.heartsome.cat.ts.websearch.resource.Messages;
import net.heartsome.cat.ts.websearch.ui.dialog.AddSearchEntryDialog;
import net.heartsome.cat.ts.websearch.ui.dialog.AddSearchEntryDialog.OKHandler;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ICheckStateProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author yule
 * @version
 * @since JDK1.6
 */
public class WebSearchPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

	public final static Logger logger = LoggerFactory.getLogger(WebSearchPreferencePage.class);

	public static final String ID = "net.heartsome.cat.ts.websearch.ui.preference.WebSearchPreferencePage";

	private static final String[] tittles = new String[] {
			Messages.getString("Websearch.WebSearcPreferencePage.tableTilte1"),
			Messages.getString("Websearch.WebSearcPreferencePage.tableTilte2"),
			Messages.getString("Websearch.WebSearcPreferencePage.tableTilte3") };

	private static final String APP_PROP = "APP_PROP";

	private static final String NAME_PROP = "NAME_PROP";

	private static final String URL_PROP = "URL_PROP";

	private CheckboxTableViewer checkboxTableViewer;

	private List<SearchEntry> cache;

	private boolean isDirty = false;

	/** @return the isDirty */
	public boolean isDirty() {
		return isDirty;
	}

	/**
	 * @param isDirty
	 *            the isDirty to set
	 */
	public void setDirty(boolean isDirty) {
		this.isDirty = isDirty;
	}

	/**
	 * (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	@Override
	public void init(IWorkbench workbench) {
		// TODO Auto-generated method stub

	}

	/**
	 * (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createContents(Composite parent) {

		Composite tparent = new Composite(parent, SWT.NONE);
		tparent.setLayout(new GridLayout(1, false));
		tparent.setLayoutData(new GridData(GridData.FILL_BOTH));

		Group group = new Group(tparent, SWT.NONE);
		group.setText(Messages.getString("Websearch.WebSearcPreferencePage.urlSet"));
		group.setLayout(new GridLayout(1, false));
		group.setLayoutData(new GridData(GridData.FILL_BOTH));

		// Composite composite = new Composite(group, SWT.NONE);
		createTitleArea(group);

		Composite urlArea = new Composite(group, SWT.NONE);
		GridLayout urlArea_layout = new GridLayout(2, false);
		urlArea.setLayout(urlArea_layout);
		urlArea.setLayoutData(new GridData(GridData.FILL_BOTH));
		createTableArea(urlArea);
		createTableCmdArea(urlArea);
		installListeners();
		return group;
	}

	private Composite createTitleArea(Composite parent) {
		HsImageLabel paraConsisLbl = new HsImageLabel(Messages.getString("Websearch.WebSearcPreferencePage.setInfo"),
				Activator.getImageDescriptor("image/websearch32.png"));
		paraConsisLbl.createControl(parent);
		paraConsisLbl.computeSize();
		return parent;
	}

	private Table table;

	private Composite createTableArea(Composite parent) {
		Composite tableArea = new Composite(parent, SWT.NONE);
		GridLayout gridLayout = new GridLayout(1, false);
		gridLayout.marginWidth = 0;
		tableArea.setLayout(gridLayout);
		GridData gridData = new GridData(GridData.FILL_BOTH);
		tableArea.setLayoutData(gridData);
		checkboxTableViewer = CheckboxTableViewer.newCheckList(tableArea, SWT.BORDER | SWT.FULL_SELECTION);
		table = checkboxTableViewer.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(false);
		TableLayout tableLayout = new TableLayout();
		tableLayout.addColumnData(new ColumnPixelData(40));
		tableLayout.addColumnData(new ColumnWeightData(50, 50, true));
		tableLayout.addColumnData(new ColumnWeightData(70, 50, true));
		table.setLayout(tableLayout);
		GridData tableGridData = new GridData(GridData.FILL_BOTH);
		table.setLayoutData(tableGridData);
		WebSearchLableProvider webSearchLableProvider = new WebSearchLableProvider();
		webSearchLableProvider.createColumns(checkboxTableViewer);
		checkboxTableViewer.setContentProvider(new WebSearchContentProvider());
		checkboxTableViewer.setLabelProvider(webSearchLableProvider);
		checkboxTableViewer.setCheckStateProvider(new CheckProvider());
		checkboxTableViewer.addCheckStateListener(new CheckListener());
		// checkboxTableViewer.setCellEditors(new CellEditor[] { null, new TextCellEditor(table),
		// new TextCellEditor(table) });
		// checkboxTableViewer.setCellModifier(new NameModifier());
		checkboxTableViewer.setColumnProperties(new String[] { APP_PROP, NAME_PROP, URL_PROP });
		cache = WebSearchPreferencStore.getIns().getSearchConfig();
		checkboxTableViewer.setInput((Object) cache);
		return tableArea;
	}

	private Button addItemBtn;

	private Button editItemBtn;

	private Button deleteItemBtn;

	private Button upItemBtn;

	private Button downItemBtn;

	private Button importItemsBtn;

	private Button exportItemsBtn;

	private Composite createTableCmdArea(Composite parent) {
		Composite urlCmdArea = new Composite(parent, SWT.NONE);
		GridLayout urlCmdArea_layout = new GridLayout(1, true);
		urlCmdArea.setLayout(urlCmdArea_layout);
		urlCmdArea.setLayoutData(new GridData(GridData.FILL_VERTICAL));

		addItemBtn = new Button(urlCmdArea, SWT.NONE);
		addItemBtn.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		addItemBtn.setText(Messages.getString("Websearch.WebSearcPreferencePage.Add"));

		editItemBtn = new Button(urlCmdArea, SWT.NONE);
		editItemBtn.setText(Messages.getString("Websearch.WebSearcPreferencePage.edit"));
		editItemBtn.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		editItemBtn.setEnabled(false);

		deleteItemBtn = new Button(urlCmdArea, SWT.NONE);
		deleteItemBtn.setText(Messages.getString("Websearch.WebSearcPreferencePage.delete"));
		deleteItemBtn.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		deleteItemBtn.setEnabled(false);

		upItemBtn = new Button(urlCmdArea, SWT.NONE);
		upItemBtn.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		upItemBtn.setText(Messages.getString("Websearch.WebSearcPreferencePage.upitem"));
		upItemBtn.setEnabled(false);

		downItemBtn = new Button(urlCmdArea, SWT.NONE);
		downItemBtn.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		downItemBtn.setText(Messages.getString("Websearch.WebSearcPreferencePage.downitem"));
		downItemBtn.setEnabled(false);

		importItemsBtn = new Button(urlCmdArea, SWT.NONE);
		importItemsBtn.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		importItemsBtn.setText(Messages.getString("Websearch.WebSearcPreferencePage.import"));

		exportItemsBtn = new Button(urlCmdArea, SWT.NONE);
		exportItemsBtn.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		exportItemsBtn.setText(Messages.getString("Websearch.WebSearcPreferencePage.export"));

		return urlCmdArea;
	}

	private class NameModifier implements ICellModifier {

		@Override
		public void modify(Object element, String property, Object value) {
			if (element instanceof Item) {
				Item item = (Item) element;
				SearchEntry data = (SearchEntry) item.getData();
				if (NAME_PROP.equals(property)) {
					if (!((String) value).equals(data.getSearchName())) {
						data.setSearchName((String) value);
						checkboxTableViewer.update(data, null);
						setDirty(true);
					}
				} else if (URL_PROP.equals(property)) {
					if (!((String) value).equals(data.getSearchUrl())) {
						data.setSearchUrl((String) value);
						checkboxTableViewer.update(data, null);
						setDirty(true);
					}
				}

			}
		}

		@Override
		public Object getValue(Object element, String property) {
			if (element instanceof SearchEntry) {
				SearchEntry searchEntry = (SearchEntry) element;
				if (NAME_PROP.equals(property)) {

					return searchEntry.getSearchName();
				} else if (URL_PROP.equals(property)) {
					return searchEntry.getSearchUrl();
				}
			}
			return "";
		}

		@Override
		public boolean canModify(Object element, String property) {
			if (NAME_PROP.equals(property) || URL_PROP.equals(property)) {
				return true;
			}
			return false;
		}
	}

	private class CheckListener implements ICheckStateListener {

		/**
		 * (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ICheckStateListener#checkStateChanged(org.eclipse.jface.viewers.CheckStateChangedEvent)
		 */
		@Override
		public void checkStateChanged(CheckStateChangedEvent event) {
			Object element = event.getElement();
			if (element instanceof SearchEntry) {
				SearchEntry searchEntry = (SearchEntry) element;
				if (searchEntry.isChecked() != event.getChecked()) {
					searchEntry.setChecked(event.getChecked());
					setDirty(true);
				}
			}
		}

	}

	private class WebSearchLableProvider implements ITableLabelProvider {

		private List<Image> cache = new ArrayList<Image>();

		/**
		 * (non-Javadoc)
		 * @see org.eclipse.jface.viewers.IBaseLabelProvider#addListener(org.eclipse.jface.viewers.ILabelProviderListener)
		 */
		@Override
		public void addListener(ILabelProviderListener listener) {
			// TODO Auto-generated method stub

		}

		/**
		 * (non-Javadoc)
		 * @see org.eclipse.jface.viewers.IBaseLabelProvider#dispose()
		 */
		@Override
		public void dispose() {
			// TODO Auto-generated method stub
			for (Image e : cache) {
				e.dispose();
			}

		}

		/**
		 * (non-Javadoc)
		 * @see org.eclipse.jface.viewers.IBaseLabelProvider#isLabelProperty(java.lang.Object, java.lang.String)
		 */
		@Override
		public boolean isLabelProperty(Object element, String property) {
			// TODO Auto-generated method stub
			return false;
		}

		/**
		 * (non-Javadoc)
		 * @see org.eclipse.jface.viewers.IBaseLabelProvider#removeListener(org.eclipse.jface.viewers.ILabelProviderListener)
		 */
		@Override
		public void removeListener(ILabelProviderListener listener) {
			// TODO Auto-generated method stub

		}

		/**
		 * (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java.lang.Object, int)
		 */
		@Override
		public Image getColumnImage(Object element, int columnIndex) {
			SearchEntry searchEntry = null;
			if (element instanceof SearchEntry) {
				searchEntry = (SearchEntry) element;
			} else {
				return null;
			}
			switch (columnIndex) {
			case 1:
				String imagePath = searchEntry.getImagePath();
				if (null == imagePath) {
					return null;
				}
				Image e = Activator.getImageDescriptor(imagePath).createImage();
				cache.add(e);
				return e;

			default:
				return null;
			}
		}

		/**
		 * (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object, int)
		 */
		@Override
		public String getColumnText(Object element, int columnIndex) {
			SearchEntry searchEntry = null;
			if (element instanceof SearchEntry) {
				searchEntry = (SearchEntry) element;
			} else {
				return null;
			}
			switch (columnIndex) {
			case 1:
				return searchEntry.getSearchName();
			case 2:
				return searchEntry.getSearchUrl();
			default:
				return null;
			}
		}

		/**
		 * (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ColumnLabelProvider#getText(java.lang.Object)
		 */
		public void createColumns(TableViewer viewer) {
			for (int i = 0; i < tittles.length; i++) {
				TableColumn column = new TableViewerColumn(viewer, SWT.NONE).getColumn();
				column.setText(tittles[i]);
				column.setResizable(true);
			}
		}
	}

	private class WebSearchContentProvider implements IStructuredContentProvider {

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
		 */
		@Override
		public Object[] getElements(Object inputElement) {
			if (inputElement instanceof List<?>) {
				@SuppressWarnings("unchecked")
				List<SearchEntry> list = (List<SearchEntry>) inputElement;
				return list.toArray(new SearchEntry[list.size()]);
			}
			return null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
		 */
		@Override
		public void dispose() {

		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer,
		 * java.lang.Object, java.lang.Object)
		 */
		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {

		}

	}

	private class CheckProvider implements ICheckStateProvider {

		/**
		 * (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ICheckStateProvider#isChecked(java.lang.Object)
		 */
		@Override
		public boolean isChecked(Object element) {
			if (element instanceof SearchEntry) {
				SearchEntry searchEntry = (SearchEntry) element;
				if (searchEntry.isChecked()) {
					return true;
				}
			}
			return false;
		}

		/**
		 * (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ICheckStateProvider#isGrayed(java.lang.Object)
		 */
		@Override
		public boolean isGrayed(Object element) {
			return false;
		}

	}

	/**
	 * (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#performOk()
	 */
	@Override
	public boolean performOk() {
		if (isDirty()) {
			WebSearchPreferencStore.getIns().storeConfig(cache);
			setDirty(false);
		}
		return super.performOk();
	}

	/**
	 * (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#performDefaults()
	 */
	@Override
	protected void performDefaults() {
		cache = WebSearchPreferencStore.getIns().getDefaluSearchConfig();
		refreshTable(cache);
		WebSearchPreferencStore.getIns().storeConfig(cache);
		setDirty(false);
		super.performDefaults();
	}

	public void refreshTable(Object input) {
		checkboxTableViewer.setInput(input);
		checkboxTableViewer.refresh();
	}

	private void installListeners() {
		checkboxTableViewer.addSelectionChangedListener(new ISelectionChangedListener() {

			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				updateState();
			}
		});

		upItemBtn.addSelectionListener(new SelectionAdapter() {
			/**
			 * (non-Javadoc)
			 * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
			 */
			@Override
			public void widgetSelected(SelectionEvent e) {
				upSelectItem();

			}
		});

		downItemBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				downSelectItem();

			}
		});

		deleteItemBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				deleteSelectItem();

			}
		});

		addItemBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				addItem();

			}
		});

		editItemBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				editItem();

			}
		});

		importItemsBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				importConfig();

			}
		});
		exportItemsBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				exportConfig();

			}
		});

	}

	public SearchEntry getSelectTableItem() {
		int selectionIndex = getSelectIndex();
		if (-1 == selectionIndex) {
			return null;
		}
		return (SearchEntry) table.getItem(selectionIndex).getData();

	}

	private int getSelectIndex() {
		return table.getSelectionIndex();
	}

	public void upSelectItem() {
		int selectIndex = getSelectIndex();
		if (-1 == selectIndex || 0 == selectIndex) {
			return;
		}
		SearchEntry currentSelect = cache.get(selectIndex);
		SearchEntry lastData = cache.get(selectIndex - 1);
		cache.set(selectIndex - 1, currentSelect);
		cache.set(selectIndex, lastData);
		checkboxTableViewer.refresh();
		setDirty(true);
		updateState();
	}

	public void downSelectItem() {
		int selectIndex = getSelectIndex();
		if (-1 == selectIndex || cache.size() - 1 == selectIndex) {
			return;
		}
		SearchEntry currentSelect = cache.get(selectIndex);
		SearchEntry lastData = cache.get(selectIndex + 1);
		cache.set(selectIndex + 1, currentSelect);
		cache.set(selectIndex, lastData);
		checkboxTableViewer.refresh();
		setDirty(true);
		updateState();
	}

	public void deleteSelectItem() {
		int selectIndex = getSelectIndex();
		if (-1 == selectIndex) {
			return;
		}
		SearchEntry searchEntry = cache.get(selectIndex);
		boolean config = MessageDialog.openConfirm(
				getShell(),
				Messages.getString("Websearch.WebSearcPreferencePage.tipTitle"),
				MessageFormat.format(Messages.getString("Websearch.WebSearcPreferencePage.deleteConfig"),
						searchEntry.getSearchName()));
		if (config) {
			cache.remove(selectIndex);
			checkboxTableViewer.refresh();
			setDirty(true);
			updateState();
		}
	}

	public void addItem() {
		final SearchEntry entry = new SearchEntry();
		AddSearchEntryDialog addDilog = new AddSearchEntryDialog(getShell(), entry, AddSearchEntryDialog.ADD);
		addDilog.setHandler(new OKHandler(){
			@Override
			public boolean doOk() {
				if (null == entry.getSearchName()) {
					return false;
				}
				if (hasDisplayNameDulicate(cache, entry)) {
					MessageDialog.openInformation(getShell(),
							Messages.getString("Websearch.WebSearcPreferencePage.tipTitle"),
							Messages.getString("Websearch.WebSearcPreferencePage.hasDuplicateName"));
					return false;
				}

				if (hasDupliacateUrl(cache, entry)) {
					MessageDialog.openInformation(getShell(),
							Messages.getString("Websearch.WebSearcPreferencePage.tipTitle"),
							Messages.getString("Websearch.WebSearcPreferencePage.hasDuplicateUrl"));
					return false;
				}

				cache.add(entry);
				setDirty(true);
				checkboxTableViewer.refresh();
				updateState();
				return true;
			}
			
		});
		addDilog.open();
	}

	private boolean hasDupliacateUrl(List<SearchEntry> cacheParam, SearchEntry entryParam) {
		for (SearchEntry temp : cacheParam) {
			if (temp == entryParam) {
				continue;
			}
			if (temp.getSearchUrl().equals(entryParam.getSearchUrl())) {
				return true;
			}
		}
		return false;
	}

	private boolean hasDisplayNameDulicate(List<SearchEntry> cacheParam, SearchEntry entryParam) {
		for (SearchEntry temp : cacheParam) {
			if (temp == entryParam) {
				continue;
			}
			if (temp.getSearchName().equals(entryParam.getSearchName())) {
				return true;
			}
		}
		return false;
	}

	public void editItem() {
		final SearchEntry selectTableItem = getSelectTableItem();

		if (null == selectTableItem) {
			return;
		}

		final String oldName = selectTableItem.getSearchName();
		final String oldUrl = selectTableItem.getSearchUrl();
		final boolean isChecked = selectTableItem.isChecked();
		AddSearchEntryDialog addDilog = new AddSearchEntryDialog(getShell(), selectTableItem, AddSearchEntryDialog.EDIT);
		addDilog.setHandler(new OKHandler() {		
			@Override
			public boolean doOk() {
				if (oldName.equals(selectTableItem.getSearchName()) //
						&& isChecked == selectTableItem.isChecked()//
						&& oldUrl.equals(selectTableItem.getSearchUrl())) {
					return false;
				}
				if (hasDisplayNameDulicate(cache, selectTableItem)) {
					selectTableItem.setChecked(isChecked);
					selectTableItem.setSearchName(oldName);
					selectTableItem.setSearchUrl(oldUrl);
					MessageDialog.openInformation(getShell(),
							Messages.getString("Websearch.WebSearcPreferencePage.tipTitle"),
							Messages.getString("Websearch.WebSearcPreferencePage.hasDuplicateName"));
					return false;
				}
				if (hasDupliacateUrl(cache, selectTableItem)) {
					selectTableItem.setChecked(isChecked);
					selectTableItem.setSearchName(oldName);
					selectTableItem.setSearchUrl(oldUrl);
					MessageDialog.openInformation(getShell(),
							Messages.getString("Websearch.WebSearcPreferencePage.tipTitle"),
							Messages.getString("Websearch.WebSearcPreferencePage.hasDuplicateUrl"));
					return false;
				}
				setDirty(true);
				checkboxTableViewer.refresh();
				updateState();
				return true;
			
			}
		});
		addDilog.open();

	}

	private void updateState() {
		if (getSelectIndex() == -1) {
			upItemBtn.setEnabled(false);
			downItemBtn.setEnabled(false);
			deleteItemBtn.setEnabled(false);
			editItemBtn.setEnabled(false);
		} else {
			deleteItemBtn.setEnabled(true);
			editItemBtn.setEnabled(true);
			if (getSelectIndex() == 0) {
				upItemBtn.setEnabled(false);
			} else {
				upItemBtn.setEnabled(true);
			}

			if (getSelectIndex() == cache.size() - 1) {
				downItemBtn.setEnabled(false);
			} else {
				downItemBtn.setEnabled(true);
			}
		}
	}

	public void importConfig() {
		boolean config = MessageDialog.openConfirm(getShell(),
				Messages.getString("Websearch.WebSearcPreferencePage.tipTitle"),
				Messages.getString("Websearch.WebSearcPreferencePage.importConfig"));
		if (!config) {
			return;
		}
		FileDialog dialog = new FileDialog(getShell(), SWT.OPEN);
		dialog.setFilterExtensions(new String[] { "*.xml" });
		dialog.open();
		String filterPath = dialog.getFilterPath();
		if (null == filterPath || filterPath.isEmpty()) {
			return;
		}
		String fileName = dialog.getFileName();
		if (fileName == null) {
			return;
		}
		String filePath = filterPath + File.separator + fileName;
		List<SearchEntry> importSearchConfig = WebSearchPreferencStore.getIns().importSearchConfig(filePath);
		if (null == importSearchConfig || importSearchConfig.isEmpty()) {
			MessageDialog.openError(getShell(), Messages.getString("Websearch.WebSearcPreferencePage.errorTitle"),
					Messages.getString("Websearch.WebSearcPreferencePage.importError"));
			return;
		} else {
			cache = importSearchConfig;
			refreshTable(cache);
			setDirty(true);
		}

	}

	public void exportConfig() {
		FileDialog dialog = new FileDialog(getShell(), SWT.SAVE);
		dialog.setFileName("Web_Config.xml");
		dialog.open();
		String filterPath = dialog.getFilterPath();
		if (null == filterPath || filterPath.isEmpty()) {
			return;
		}
		File file = new File(filterPath + File.separator + dialog.getFileName());

		boolean config = true;

		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
				logger.error("", e);
			}
		} else {
			config = MessageDialog.openConfirm(getShell(),
					Messages.getString("Websearch.WebSearcPreferencePage.tipTitle"),
					Messages.getString("Websearch.WebSearcPreferencePage.ConfirmInfo"));
		}
		if (config) {
			boolean exportSearchConfig = WebSearchPreferencStore.getIns().exportSearchConfig(file.getAbsolutePath());
			if (exportSearchConfig) {
				MessageDialog.openInformation(getShell(),
						Messages.getString("Websearch.WebSearcPreferencePage.tipTitle"),
						Messages.getString("Websearch.WebSearcPreferencePage.exportFinish"));
			} else {
				MessageDialog.openInformation(getShell(),
						Messages.getString("Websearch.WebSearcPreferencePage.tipTitle"),
						Messages.getString("Websearch.WebSearcPreferencePage.failexport"));
			}

		}

	}
}

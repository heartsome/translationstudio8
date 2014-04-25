package net.heartsome.cat.ts.ui.advanced.dialogs.srx;

import java.io.File;
import java.io.FilenameFilter;
import java.text.MessageFormat;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.heartsome.cat.common.util.CommonFunction;
import net.heartsome.cat.ts.core.qa.QAConstant;
import net.heartsome.cat.ts.ui.advanced.ADConstants;
import net.heartsome.cat.ts.ui.advanced.TableViewerLabelProvider;
import net.heartsome.cat.ts.ui.advanced.dialogs.XmlConverterConfigurationDialog.XmlConvertOrder;
import net.heartsome.cat.ts.ui.advanced.handlers.ADXmlHandler;
import net.heartsome.cat.ts.ui.advanced.resource.Messages;

import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TrayDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.PlatformUI;

/**
 * 配置分段规则的第一个对话框，即显示出所有分段规则文件的对话框
 * @author robert 2012-02-28
 * @version
 * @since JDK1.6
 */
public class SrxConfigurationDialog extends TrayDialog {
	private Button addBtn;
	private Button editBtn;
	private Button deleteBtn;
	private TableViewer tableViewer;
	private Table table;
	private IWorkspaceRoot root;
	private ADXmlHandler handler = new ADXmlHandler();
	
	/** 系统默认的分段规则名称 */
	private static final String[] systemSrxName = new String[]{"default_rules.srx", "trados_rules.srx"};

	private Cursor cursorWait = new Cursor(Display.getDefault(), SWT.CURSOR_WAIT);
	private Cursor cursorArrow = new Cursor(Display.getDefault(), SWT.CURSOR_ARROW);
	public SrxConfigurationDialog(Shell parentShell) {
		super(parentShell);
		root = ResourcesPlugin.getWorkspace().getRoot();
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(Messages.getString("srx.SrxConfigurationDialog.title"));
	}

	@Override
	protected boolean isResizable() {
		return true;
	}
	
	/**
	 * 添加帮助按钮，备注，这里的配置与其他的不一样
	 * robert	2012-09-06
	 */
	protected Control createHelpToolItem(Composite parent) {
		// ROBERTHELP 分段规则管理器
		String language = CommonFunction.getSystemLanguage();
		final String helpUrl = MessageFormat.format(
				"/net.heartsome.cat.ts.ui.help/html/{0}/ch05s03#segmentation-rule-manager", language);
		
		Image helpImage = JFaceResources.getImage(DLG_IMG_HELP);
		ToolBar toolBar = new ToolBar(parent, SWT.FLAT | SWT.NO_FOCUS);
		toolBar.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));
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
	protected Control createButtonBar(Composite parent) {
		Composite buttonCmp = new Composite(parent, SWT.NONE);

		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.makeColumnsEqualWidth = false;
		layout.marginWidth = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
		layout.marginHeight = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
		layout.horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
		layout.verticalSpacing = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
		buttonCmp.setLayout(layout);
		GridData data = new GridData(SWT.FILL, SWT.FILL, true, false);
		buttonCmp.setLayoutData(data);
		buttonCmp.setFont(parent.getFont());

		Composite leftCmp = new Composite(buttonCmp, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(leftCmp);
		GridLayoutFactory.fillDefaults().extendedMargins(0, 0, 0, 0).numColumns(3).equalWidth(false).applyTo(leftCmp);

		addBtn = createButton(leftCmp, IDialogConstants.CLIENT_ID,
				Messages.getString("srx.SrxConfigurationDialog.addBtn"), false);
		editBtn = createButton(leftCmp, IDialogConstants.CLIENT_ID,
				Messages.getString("srx.SrxConfigurationDialog.editBtn"), false);
		deleteBtn = createButton(leftCmp, IDialogConstants.CLIENT_ID,
				Messages.getString("srx.SrxConfigurationDialog.deleteBtn"), false);

		Composite rightCmp = new Composite(buttonCmp, SWT.NONE);
		GridLayoutFactory.fillDefaults().extendedMargins(0, 0, 0, 0).numColumns(1).equalWidth(false).applyTo(rightCmp);

		new Label(rightCmp, SWT.NONE);

		Label separatorLbl = new Label(buttonCmp, SWT.HORIZONTAL | SWT.SEPARATOR);
		GridDataFactory.fillDefaults().span(2, SWT.DEFAULT).applyTo(separatorLbl);

//		new Label(buttonCmp, SWT.NONE);
		createHelpToolItem(buttonCmp);
		Composite bottomCmp = new Composite(buttonCmp, SWT.NONE);
		GridDataFactory.fillDefaults().grab(false, false).applyTo(bottomCmp);
		GridLayoutFactory.fillDefaults().extendedMargins(0, 0, 0, 0).numColumns(1).applyTo(bottomCmp);

		createButton(bottomCmp, IDialogConstants.CANCEL_ID, Messages.getString("srx.SrxConfigurationDialog.cancel"),
				true).setFocus();

		initListener();
		return buttonCmp;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite tparent = (Composite) super.createDialogArea(parent);
		GridDataFactory.fillDefaults().grab(true, true).hint(400, 400).minSize(400, 400).applyTo(tparent);

		tableViewer = new TableViewer(tparent, SWT.FULL_SELECTION | SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL
				| SWT.BORDER);
		table = tableViewer.getTable();
		table.setLinesVisible(true);
		table.setHeaderVisible(true);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(table);
		tableViewer.setLabelProvider(new TableViewerLabelProvider());
		tableViewer.setContentProvider(new ArrayContentProvider());

		String[] columnNames = new String[] { Messages.getString("srx.SrxConfigurationDialog.columnNames1"),
				Messages.getString("srx.SrxConfigurationDialog.columnNames2") };
		int[] columnAlignments = new int[] { SWT.LEFT, SWT.LEFT };
		for (int i = 0; i < columnNames.length; i++) {
			TableColumn tableColumn = new TableColumn(table, columnAlignments[i]);
			tableColumn.setText(columnNames[i]);
			tableColumn.setWidth(50);

			// 处理排序的问题
			switch (i) {
			case 0:
				tableColumn.addSelectionListener(new SelectionAdapter() {
					boolean asc = true; // 升序

					@Override
					public void widgetSelected(SelectionEvent e) {
						tableViewer.setSorter(asc ? XmlConvertOrder.index_ASC : XmlConvertOrder.index_DESC);
						asc = !asc;
					}
				});
				break;
			case 1:
				tableColumn.addSelectionListener(new SelectionAdapter() {
					boolean asc = true; // 升序

					@Override
					public void widgetSelected(SelectionEvent e) {
						tableViewer.setSorter(asc ? XmlConvertOrder.xmlName_ASC : XmlConvertOrder.xmlName_DESC);
						asc = !asc;
					}
				});
				break;
			default:
				break;
			}
		}
		tableViewer.setInput(getSRXConfigFilesInfo());

		// 让列表列宽动态变化
		table.addListener(SWT.Resize, new Listener() {
			public void handleEvent(Event event) {
				final Table table = ((Table) event.widget);
				final TableColumn[] columns = table.getColumns();
				event.widget.getDisplay().syncExec(new Runnable() {
					public void run() {
						double[] columnWidths = new double[] { 0.1, 0.85 };
						for (int i = 0; i < columns.length; i++)
							columns[i].setWidth((int) (table.getBounds().width * columnWidths[i]));
					}
				});
			}
		});

		tableViewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				editSrx();
			}
		});
		refreshTable(null);
		return tparent;
	}

	/**
	 * 给增删改三个按钮提示添加事件 ;
	 */
	private void initListener() {
		addBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				CreateOrUpdataSRXDialog createDialog = new CreateOrUpdataSRXDialog(getShell(), true);
				int createResult = createDialog.open();
				if (IDialogConstants.OK_ID == createResult) {
					String addedSrxName = createDialog.getCurSrxName();
					// 添加完成该文件后，解析该文件
					boolean openResult = openSrx(ADConstants.configLocation + ADConstants.AD_SRXConfigFolder + File.separator + addedSrxName);
					if (!openResult) {
						return;
					}
					AddOrEditSrxConfigDialog addDialog = new AddOrEditSrxConfigDialog(getShell(), addedSrxName, handler);
					addDialog.open();
					refreshTable(addedSrxName);
				}
			}
		});
		editBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				editSrx();
			}
		});
		deleteBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				ISelection selection = tableViewer.getSelection();
				if (!selection.isEmpty() && selection != null && selection instanceof StructuredSelection) {
					boolean response = MessageDialog.openConfirm(getShell(),
							Messages.getString("srx.SrxConfigurationDialog.msgTitle1"),
							Messages.getString("srx.SrxConfigurationDialog.msg1"));
					if (!response) {
						return;
					}

					StructuredSelection structSelection = (StructuredSelection) selection;
					@SuppressWarnings("unchecked")
					Iterator<String[]> it = structSelection.iterator();
					File deleteSrx;
					boolean isTiped = false;
					while (it.hasNext()) {
						String srxName = it.next()[1];
						if (isSystemSrx(srxName)) {
							if (!isTiped) {
								MessageDialog.openInformation(getShell(), Messages.getString("dialogs.CatalogManagerDialog.msgTitle2"),
										Messages.getString("srx.SrxConfigurationDialog.msg4"));
								isTiped = true;
							}
							continue;
						}
						deleteSrx = new File(ADConstants.configLocation + ADConstants.AD_SRXConfigFolder + File.separator + srxName);
						if (!deleteSrx.delete()) {
							MessageDialog.openInformation(getShell(), Messages
									.getString("srx.SrxConfigurationDialog.msgTitle2"), MessageFormat.format(
									Messages.getString("srx.SrxConfigurationDialog.msg2"), srxName));
						}
					}
					refreshTable(null);
				} else {
					MessageDialog.openInformation(getShell(), Messages.getString("srx.SrxConfigurationDialog.msgTitle2"),
							Messages.getString("srx.SrxConfigurationDialog.msg3"));
				}
			}
		});
	}

	@Override
	protected void okPressed() {
		super.okPressed();
	}

	/**
	 * 编辑SRX文件 ;
	 */
	private void editSrx() {
		ISelection selection = tableViewer.getSelection();
		if (!selection.isEmpty() && selection != null && selection instanceof StructuredSelection) {
			StructuredSelection structSelection = (StructuredSelection) selection;
			@SuppressWarnings("unchecked")
			Iterator<String[]> it = structSelection.iterator();
			if (it.hasNext()) {
				String srxName = it.next()[1];
				if (isSystemSrx(srxName)) {
					MessageDialog.openInformation(getShell(), Messages.getString("dialogs.CatalogManagerDialog.msgTitle2"),
							Messages.getString("srx.SrxConfigurationDialog.msg4"));
					return;
				}
				
				CreateOrUpdataSRXDialog editDialog = new CreateOrUpdataSRXDialog(getShell(), false);
				editDialog.create();
				editDialog.setEditInitData(srxName);
				int result = editDialog.open();
				if (result == IDialogConstants.OK_ID) {
					String editedSrxName = editDialog.getCurSrxName();
					if (!openSrx(ADConstants.configLocation + ADConstants.AD_SRXConfigFolder + File.separator + editedSrxName)) {
						return;
					}

					AddOrEditSrxConfigDialog addDialog = new AddOrEditSrxConfigDialog(getShell(), editedSrxName,
							handler);
					addDialog.open();
					refreshTable(editedSrxName);
				}
			}
		}
	}
	
	/**
	 * 判断指定的　分段规则是否是　系统默认的分段规则，因为系统默认的分段规则是不充许修改与删除的
	 * @param srxName
	 * @return true 是系统默认的分段规则		false 不是系统默认的分段规则
	 */
	private boolean isSystemSrx(String srxName){
		boolean isSystem = false;
		for(String curSrxName : systemSrxName){
			if (curSrxName.equals(srxName)) {
				isSystem = true;
			}
		}
		return isSystem;
	}

	public String[][] getSRXConfigFilesInfo() {
		getShell().setCursor(cursorWait);
		String srxConfigLocation = ADConstants.configLocation + ADConstants.AD_SRXConfigFolder;
		File[] array = new File(srxConfigLocation).listFiles(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				if (name.endsWith(".srx")) {return true;} //$NON-NLS-1$ //$NON-NLS-2$
				return false;
			}
		});
		List<String[]> srxFileInfoList = new LinkedList<String[]>();
		// 解析每个文件，查看其是否符合SRX标准
		for (int i = 0; i < array.length; i++) {
			File srxFile = array[i];
			Map<String, Object> newResultMap = handler.openFile(srxFile.getAbsolutePath());
			// 文件解析出错
			if (newResultMap == null
					|| QAConstant.RETURNVALUE_RESULT_SUCCESSFUL != (Integer) newResultMap
							.get(QAConstant.RETURNVALUE_RESULT)) {
				continue;
			}
			if (handler.validSrx(srxFile.getAbsolutePath())) {
				srxFileInfoList.add(new String[] { "" + (srxFileInfoList.size() + 1), srxFile.getName() });
			}
		}
		getShell().setCursor(cursorArrow);
		return srxFileInfoList.toArray(new String[][] {});

	}

	/**
	 * 刷新列表，如果srxName(SRX文件名)不为空，还要定位到当前文件名
	 * @param srxName
	 *            ;
	 */
	public void refreshTable(String srxName) {
		tableViewer.setInput(getSRXConfigFilesInfo());
		if (srxName != null) {
			TableItem[] items = table.getItems();
			for (int i = 0; i < items.length; i++) {
				if (srxName.equals(items[i].getText(1))) {
					table.setSelection(i);
				}
			}
		}
	}

	private boolean openSrx(String srxLocation) {
		Map<String, Object> newResultMap = handler.openFile(srxLocation);
		// 文件解析出错
		if (newResultMap == null
				|| QAConstant.RETURNVALUE_RESULT_SUCCESSFUL != (Integer) newResultMap
						.get(QAConstant.RETURNVALUE_RESULT)) {
			return false;
		}
		return true;
	}
	
	@Override
	public boolean close() {
		if(cursorArrow != null && !cursorArrow.isDisposed()){
			cursorArrow.dispose();
		}
		if(cursorWait != null && !cursorWait.isDisposed()){
			cursorWait.dispose();
		}
		return super.close();
	}
}

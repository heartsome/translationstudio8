package net.heartsome.cat.ts.ui.advanced.dialogs;

import java.io.File;
import java.io.FilenameFilter;
import java.text.MessageFormat;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import net.heartsome.cat.common.util.CommonFunction;
import net.heartsome.cat.ts.ui.advanced.ADConstants;
import net.heartsome.cat.ts.ui.advanced.TableViewerLabelProvider;
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
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
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
 * XML转换器配制器的主窗口
 * @author robert 2012-02-22
 * @version
 * @since JDK1.6
 */
public class XmlConverterConfigurationDialog extends TrayDialog {
	private Button addBtn;
	private Button editBtn;
	private Button deleteBtn;
	private Button analysisBtn;
	/** xml转换器配置文件所在的目录 */
	private String configXmlFolderLocation;
	private TableViewer tableViewer;
	private Table table;
	private IWorkspaceRoot root;

	public XmlConverterConfigurationDialog(Shell parentShell, String configXmlFolderLocation) {
		super(parentShell);
		this.configXmlFolderLocation = configXmlFolderLocation;
		root = ResourcesPlugin.getWorkspace().getRoot();
	}

	@Override
	protected boolean isResizable() {
		return true;
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(Messages.getString("dialogs.XmlConverterConfigurationDialog.title"));
	}
	
	/**
	 * 添加帮助按钮，备注，这里的配置与其他的不一样
	 * robert	2012-09-06
	 */
	protected Control createHelpToolItem(Composite parent) {
		// ROBERTHELP xml 转换器配置
		String language = CommonFunction.getSystemLanguage();
		final String helpUrl = MessageFormat.format(
				"/net.heartsome.cat.ts.ui.help/html/{0}/ch08.html#configure-xml-converter", language);
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
				Messages.getString("dialogs.XmlConverterConfigurationDialog.addBtn"), false);
		editBtn = createButton(leftCmp, IDialogConstants.CLIENT_ID,
				Messages.getString("dialogs.XmlConverterConfigurationDialog.editBtn"), false);
		deleteBtn = createButton(leftCmp, IDialogConstants.CLIENT_ID,
				Messages.getString("dialogs.XmlConverterConfigurationDialog.deleteBtn"), false);

		Composite rightCmp = new Composite(buttonCmp, SWT.NONE);
		GridLayoutFactory.fillDefaults().extendedMargins(0, 0, 0, 0).numColumns(1).equalWidth(false).applyTo(rightCmp);

		analysisBtn = createButton(rightCmp, IDialogConstants.CLIENT_ID,
				Messages.getString("dialogs.XmlConverterConfigurationDialog.analysisBtn"), false);

		Label separatorLbl = new Label(buttonCmp, SWT.HORIZONTAL | SWT.SEPARATOR);
		GridDataFactory.fillDefaults().span(2, SWT.DEFAULT).applyTo(separatorLbl);
		
		createHelpToolItem(buttonCmp);
		
		Composite bottomCmp = new Composite(buttonCmp, SWT.NONE);
		GridDataFactory.fillDefaults().grab(false, false).align(SWT.RIGHT, SWT.CENTER).applyTo(bottomCmp);
		GridLayoutFactory.fillDefaults().extendedMargins(0, 0, 0, 0).numColumns(1).applyTo(bottomCmp);

		createButton(bottomCmp, IDialogConstants.CANCEL_ID,
				Messages.getString("dialogs.XmlConverterConfigurationDialog.cancel"), true).setFocus();

		initListener();

		return buttonCmp;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite tparent = (Composite) super.createDialogArea(parent);
		GridDataFactory.fillDefaults().grab(true, true).hint(400, 450).minSize(400, 450).applyTo(tparent);

		tableViewer = new TableViewer(tparent, SWT.FULL_SELECTION | SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL
				| SWT.BORDER);
		table = tableViewer.getTable();
		table.setLinesVisible(true);
		table.setHeaderVisible(true);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(table);
		tableViewer.setLabelProvider(new TableViewerLabelProvider());
		tableViewer.setContentProvider(new ArrayContentProvider());

		String[] columnNames = new String[] {
				Messages.getString("dialogs.XmlConverterConfigurationDialog.columnNames1"),
				Messages.getString("dialogs.XmlConverterConfigurationDialog.columnNames2") };
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
		tableViewer.setInput(getXmlConfigFilesInfo());

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
				editConfigXml();
			}
		});

		return tparent;
	}

	public void initListener() {
		addBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				AddOrEditXmlConvertConfigDialog dialog = new AddOrEditXmlConvertConfigDialog(getShell(), true);
				int result = dialog.open();
				if (result == IDialogConstants.OK_ID) {
					String curentConvertXMl = dialog.getCurentConverXML();
					refreshTable();
					setTableSelection(curentConvertXMl);
				}
			}
		});

		editBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				editConfigXml();
			}
		});

		deleteBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				ISelection selection = tableViewer.getSelection();
				if (!selection.isEmpty() && selection != null && selection instanceof IStructuredSelection) {
					IStructuredSelection structuredSelection = (IStructuredSelection) selection;
					@SuppressWarnings("unchecked")
					Iterator<String[]> iter = structuredSelection.iterator();
					while (iter.hasNext()) {
						String convertXmlName = iter.next()[1];
						String convertXmlLoaction = root.getLocation().append(ADConstants.AD_xmlConverterConfigFolder)
								.append(convertXmlName).toOSString();
						File convertXml = new File(convertXmlLoaction);
						convertXml.delete();
					}
					refreshTable();
				} else {
					MessageDialog.openInformation(getShell(),
							Messages.getString("dialogs.XmlConverterConfigurationDialog.msgTitle"),
							Messages.getString("dialogs.XmlConverterConfigurationDialog.msg1"));
				}
			}
		});

		analysisBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				AnalysisXmlConvertConfigDialg dialog = new AnalysisXmlConvertConfigDialg(getShell());
				int result = dialog.open();
				if (result == IDialogConstants.OK_ID) {
					String curentConvertXMl = dialog.getCurentConverXML();
					refreshTable();
					setTableSelection(curentConvertXMl);
				}
			}
		});
	}

	public void refreshTable() {
		tableViewer.setInput(getXmlConfigFilesInfo());
	}

	/**
	 * 查询位于configXmlFolderLocation目录下的所有xml转换器的配置文件
	 * @return ;
	 */
	public String[][] getXmlConfigFilesInfo() {
		List<String[]> infoList = new LinkedList<String[]>();
		File[] array = new File(configXmlFolderLocation).listFiles(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				if (name.startsWith("config_") && name.endsWith(".xml")) {return true;} //$NON-NLS-1$ //$NON-NLS-2$
				return false;
			}
		});

		for (int i = 0; i < array.length; i++) {
			infoList.add(new String[] { "" + (i + 1), array[i].getName() });
		}
		return infoList.toArray(new String[][] {});
	}

	/**
	 * 编辑转换配置XML文件 ;
	 */
	public void editConfigXml() {
		ISelection selection = tableViewer.getSelection();
		if (!selection.isEmpty() && selection != null && selection instanceof IStructuredSelection) {
			IStructuredSelection structuredSelection = (IStructuredSelection) selection;
			@SuppressWarnings("unchecked")
			Iterator<String[]> iter = structuredSelection.iterator();
			String convertXml = iter.next()[1];

			AddOrEditXmlConvertConfigDialog dialog = new AddOrEditXmlConvertConfigDialog(getShell(), false);
			dialog.create();
			String convertXmlLoaction = root.getLocation().append(ADConstants.AD_xmlConverterConfigFolder)
					.append(convertXml).toOSString();
			if (dialog.setInitEditData(convertXmlLoaction)) {
				int result = dialog.open();
				// 如果点击的是确定按钮，那么更新列表
				if (result == IDialogConstants.OK_ID) {
					String curentConvertXMl = dialog.getCurentConverXML();
					refreshTable();
					setTableSelection(curentConvertXMl);
				}
			}
		} else {
			MessageDialog.openInformation(getShell(),
					Messages.getString("dialogs.XmlConverterConfigurationDialog.msgTitle"),
					Messages.getString("dialogs.XmlConverterConfigurationDialog.msg2"));
		}
	}

	/**
	 * 选中列中表中已经编辑或已经添加的文件
	 * @param curentConvertXMl
	 *            ;
	 */
	public void setTableSelection(String curConvertXMl) {
		File convertXml = new File(curConvertXMl);
		String convertXmlName = convertXml.getName();
		TableItem[] items = table.getItems();
		for (int i = 0; i < items.length; i++) {
			if (items[i].getText(1).equals(convertXmlName)) {
				table.setSelection(i);
				return;
			}
		}
	}

	/**
	 * XML转换器列表排序类
	 * @author robert
	 * @version
	 * @since JDK1.6
	 */
	public static class XmlConvertOrder extends ViewerSorter {
		private static final int index_ID = 1; // 第一行，序列
		private static final int xmlName_ID = 2; // 第二行，XML转换文件名称

		public static final XmlConvertOrder index_ASC = new XmlConvertOrder(index_ID);
		public static final XmlConvertOrder index_DESC = new XmlConvertOrder(-index_ID);

		public static final XmlConvertOrder xmlName_ASC = new XmlConvertOrder(xmlName_ID);
		public static final XmlConvertOrder xmlName_DESC = new XmlConvertOrder(-xmlName_ID);

		private int sortType;

		private XmlConvertOrder(int sortType) {
			this.sortType = sortType;
		}

		@Override
		public int compare(Viewer viewer, Object e1, Object e2) {
			String[] data1 = (String[]) e1;
			String[] data2 = (String[]) e2;
			switch (sortType) {
			case index_ID: {
				int lineNumber1 = Integer.parseInt(data1[index_ID - 1]);
				int lineNumber2 = Integer.parseInt(data2[index_ID - 1]);
				return lineNumber1 > lineNumber2 ? 1 : -1;
			}
			case -index_ID: {
				int lineNumber1 = Integer.parseInt(data1[index_ID - 1]);
				int lineNumber2 = Integer.parseInt(data2[index_ID - 1]);
				return lineNumber1 > lineNumber2 ? -1 : 1;
			}
			case xmlName_ID: {
				String xmlName1 = data1[xmlName_ID - 1];
				String xmlName2 = data2[xmlName_ID - 1];
				return xmlName1.compareToIgnoreCase(xmlName2);
			}
			case -xmlName_ID: {
				String xmlName1 = data1[xmlName_ID - 1];
				String xmlName2 = data2[xmlName_ID - 1];
				return xmlName2.compareToIgnoreCase(xmlName1);
			}
			}
			return 0;
		}
	}

}
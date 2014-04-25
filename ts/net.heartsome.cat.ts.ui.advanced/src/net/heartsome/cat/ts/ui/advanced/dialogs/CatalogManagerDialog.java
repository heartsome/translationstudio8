package net.heartsome.cat.ts.ui.advanced.dialogs;

import java.util.LinkedList;
import java.util.List;

import net.heartsome.cat.ts.ui.advanced.ADConstants;
import net.heartsome.cat.ts.ui.advanced.TableViewerLabelProvider;
import net.heartsome.cat.ts.ui.advanced.handlers.ADXmlHandler;
import net.heartsome.cat.ts.ui.advanced.resource.Messages;

import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

public class CatalogManagerDialog extends Dialog {
	private Button addBtn;
	private Button deleteBtn;
	private Button editBtn;
	private TableViewer tableViewer;
	private IWorkspaceRoot root;
	private ADXmlHandler adHandler;
	private Table table;
	private String catalogXmlLocation;

	public CatalogManagerDialog(Shell parentShell, IWorkspaceRoot root) {
		super(parentShell);
		this.root = root;
		catalogXmlLocation = root.getLocation().append(ADConstants.catalogueXmlPath).toOSString();
	}

	@Override
	protected boolean isResizable() {
		return true;
	}

	@Override
	protected void configureShell(Shell newShell) {
		// TODO Auto-generated method stub
		super.configureShell(newShell);
		newShell.setText(Messages.getString("dialogs.CatalogManagerDialog.title"));
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		addBtn = createButton(parent, IDialogConstants.CLIENT_ID,
				Messages.getString("dialogs.CatalogManagerDialog.addBtn"), false);
		editBtn = createButton(parent, IDialogConstants.CLIENT_ID,
				Messages.getString("dialogs.CatalogManagerDialog.editBtn"), false);
		deleteBtn = createButton(parent, IDialogConstants.CLIENT_ID,
				Messages.getString("dialogs.CatalogManagerDialog.deleteBtn"), false);
		createButton(parent, IDialogConstants.CANCEL_ID, Messages.getString("dialogs.CatalogManagerDialog.cancel"),
				true);

		initListener();
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite tparent = (Composite) super.createDialogArea(parent);
		GridData parentData = new GridData(SWT.FILL, SWT.FILL, true, true);
		parentData.widthHint = 700;
		parentData.heightHint = 400;
		tparent.setLayoutData(parentData);

		tableViewer = new TableViewer(tparent, SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER
				| SWT.MULTI);
		table = tableViewer.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(table);

		String[] columnNames = new String[] { Messages.getString("dialogs.CatalogManagerDialog.columnNames1"),
				Messages.getString("dialogs.CatalogManagerDialog.columnNames2"),
				Messages.getString("dialogs.CatalogManagerDialog.columnNames3"),
				Messages.getString("dialogs.CatalogManagerDialog.columnNames4") };
		int[] columnAlignments = new int[] { SWT.LEFT, SWT.LEFT, SWT.LEFT, SWT.LEFT };
		for (int i = 0; i < columnNames.length; i++) {
			TableColumn tableColumn = new TableColumn(table, columnAlignments[i]);
			tableColumn.setText(columnNames[i]);
			tableColumn.setWidth(50);
		}

		tableViewer.setLabelProvider(new TableViewerLabelProvider());
		tableViewer.setContentProvider(new ArrayContentProvider());
		tableViewer.setInput(getCatalogValue());
		// 让列表列宽动态变化
		table.addListener(SWT.Resize, new Listener() {
			public void handleEvent(Event event) {
				final Table table = ((Table) event.widget);
				final TableColumn[] columns = table.getColumns();
				event.widget.getDisplay().syncExec(new Runnable() {
					public void run() {
						double[] columnWidths = new double[] { 0.08, 0.15, 0.36, 0.36 };
						for (int i = 0; i < columns.length; i++)
							columns[i].setWidth((int) (table.getBounds().width * columnWidths[i]));
					}
				});
			}
		});
		return tparent;
	}

	/**
	 * 获取目录数据，从catalogue.xml文件中获取
	 * @return ;
	 */
	public String[][] getCatalogValue() {
		adHandler = new ADXmlHandler();
		adHandler.openFile(catalogXmlLocation);
		return adHandler.getCatalogValueList(catalogXmlLocation).toArray(new String[][] {});
	}

	/**
	 * 给三个按钮添加事件 ;
	 */
	public void initListener() {
		addBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				AddOrEditCatalogDialog addDialog = new AddOrEditCatalogDialog(getShell(), root, adHandler, true);
				// 如果是点击的确定按钮，那么更新数据
				if (addDialog.open() == IDialogConstants.OK_ID) {
					String[][] cataValue = getCatalogValue();
					tableViewer.setInput(cataValue);
					tableViewer.getTable().setSelection(cataValue.length - 1);
					tableViewer.getTable().showSelection();
				}
			}
		});

		// <public publicId="-//W3C//DTD XMLSCHEMA 200102//EN" uri="xml/XMLSchema.dtd"/>
		// <system systemId="xliff.dtd" uri="xliff/xliff.dtd"/>
		// <uri name="http://www.heartsome.net.cn/2008/XLFExtension" uri="heartsome/XLFExtension.xsd"/>
		// <nextCatalog catalog="docbook5.0/catalog.xml"/>
		deleteBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (table.getSelectionCount() >= 1) {
					boolean response = MessageDialog.openConfirm(getShell(),
							Messages.getString("dialogs.CatalogManagerDialog.msgTitle"),
							Messages.getString("dialogs.CatalogManagerDialog.msg1"));
					if (!response) {
						return;
					}

					TableItem[] selectItems = table.getSelection();
					List<String> xpathList = new LinkedList<String>();
					List<String[]> dataList = new LinkedList<String[]>();
					for (int i = 0; i < selectItems.length; i++) {
						String name = selectItems[i].getText(1);
						String id = selectItems[i].getText(2);
						String url = selectItems[i].getText(3);
						StringBuffer xpathSB = new StringBuffer();
						if ("public".equalsIgnoreCase(name)) {
							xpathSB.append("/catalog/public[@publicId='" + id + "' and @uri='" + url + "']");
						} else if ("system".equalsIgnoreCase(name)) {
							xpathSB.append("/catalog/system[@systemId='" + id + "' and @uri='" + url + "']");
						} else if ("uri".equalsIgnoreCase(name)) {
							xpathSB.append("/catalog/uri[@name='" + id + "' and @uri='" + url + "']");
						} else if ("nextCatalog".equalsIgnoreCase(name)) {
							xpathSB.append("/catalog/nextCatalog[@catalog='" + url + "']");
						}
						xpathList.add(xpathSB.toString());
						dataList.add(new String[] { name, id, url });
					}
					adHandler.deleteCatalog(catalogXmlLocation, xpathList, dataList);

					tableViewer.setInput(getCatalogValue()); // 更新列表
				} else {
					MessageDialog.openInformation(getShell(),
							Messages.getString("dialogs.CatalogManagerDialog.msgTitle2"),
							Messages.getString("dialogs.CatalogManagerDialog.msg2"));
				}

			}
		});

		editBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				editCatalog();
			}
		});

		table.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDoubleClick(MouseEvent e) {
				editCatalog();
			}
		});
	}

	public void editCatalog() {
		if (table.getSelectionCount() >= 1) {
			TableItem item = table.getSelection()[0];
			String name = item.getText(1);
			String id = item.getText(2);
			String url = item.getText(3);

			String xpath = "";
			if ("public".equalsIgnoreCase(name)) {
				xpath = ("/catalog/public[@publicId='" + id + "' and @uri='" + url + "']");
			} else if ("system".equalsIgnoreCase(name)) {
				xpath = ("/catalog/system[@systemId='" + id + "' and @uri='" + url + "']");
			} else if ("uri".equalsIgnoreCase(name)) {
				xpath = ("/catalog/uri[@name='" + id + "' and @uri='" + url + "']");
			} else if ("nextCatalog".equalsIgnoreCase(name)) {
				xpath = ("/catalog/nextCatalog[@catalog='" + url + "']");
			}

			AddOrEditCatalogDialog dialog = new AddOrEditCatalogDialog(getShell(), root, adHandler, false);
			dialog.create();
			dialog.setInitData(name, id, url, xpath);
			int result = dialog.open();
			if (result == IDialogConstants.OK_ID) {
				tableViewer.setInput(getCatalogValue()); // 更新列表
			}
		} else {
			MessageDialog.openInformation(getShell(), Messages.getString("dialogs.CatalogManagerDialog.msgTitle2"),
					Messages.getString("dialogs.CatalogManagerDialog.msg3"));
		}
	}
}

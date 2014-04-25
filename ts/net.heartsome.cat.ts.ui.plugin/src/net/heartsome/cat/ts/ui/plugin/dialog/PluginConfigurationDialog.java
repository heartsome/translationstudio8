package net.heartsome.cat.ts.ui.plugin.dialog;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.util.Iterator;

import net.heartsome.cat.ts.ui.plugin.PluginConfigManage;
import net.heartsome.cat.ts.ui.plugin.PluginConstants;
import net.heartsome.cat.ts.ui.plugin.bean.PluginConfigBean;
import net.heartsome.cat.ts.ui.plugin.resource.Messages;

import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ximpleware.AutoPilot;
import com.ximpleware.VTDGen;
import com.ximpleware.VTDNav;
import com.ximpleware.XMLModifier;

public class PluginConfigurationDialog extends Dialog {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(PluginConfigurationDialog.class);
	
	private Button addBtn;
	private Button editBtn;
	private Button deleteBtn;
	private TableViewer tableViewer;
	private Table table;
	private String pluginXmlLocation;
	private PluginConfigManage manage;

	public PluginConfigurationDialog(Shell parentShell) {
		super(parentShell);
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		pluginXmlLocation = root.getLocation().append(PluginConstants.PC_pluginConfigLocation).toOSString();
		manage = new PluginConfigManage(pluginXmlLocation);
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(Messages.getString("dialog.PluginConfigurationDialog.title"));
	}

	@Override
	protected boolean isResizable() {
		return true;
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
				Messages.getString("dialog.PluginConfigurationDialog.addBtn"), false);
		editBtn = createButton(leftCmp, IDialogConstants.CLIENT_ID,
				Messages.getString("dialog.PluginConfigurationDialog.editBtn"), false);
		deleteBtn = createButton(leftCmp, IDialogConstants.CLIENT_ID,
				Messages.getString("dialog.PluginConfigurationDialog.deleteBtn"), false);

		Composite rightCmp = new Composite(buttonCmp, SWT.NONE);
		GridLayoutFactory.fillDefaults().extendedMargins(0, 0, 0, 0).numColumns(1).equalWidth(false).applyTo(rightCmp);

		new Label(rightCmp, SWT.NONE);

		Label separatorLbl = new Label(buttonCmp, SWT.HORIZONTAL | SWT.SEPARATOR);
		GridDataFactory.fillDefaults().span(2, SWT.DEFAULT).applyTo(separatorLbl);

		new Label(buttonCmp, SWT.NONE);
		Composite bottomCmp = new Composite(buttonCmp, SWT.NONE);
		GridDataFactory.fillDefaults().grab(false, false).applyTo(bottomCmp);
		GridLayoutFactory.fillDefaults().extendedMargins(0, 0, 0, 0).numColumns(1).applyTo(bottomCmp);

		createButton(bottomCmp, IDialogConstants.CANCEL_ID,
				Messages.getString("dialog.PluginConfigurationDialog.cancel"), true).setFocus();

		initListener();
		return buttonCmp;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite tparent = (Composite) super.createDialogArea(parent);
		GridDataFactory.fillDefaults().hint(500, 400).minSize(500, 400).applyTo(tparent);

		tableViewer = new TableViewer(tparent, SWT.FULL_SELECTION | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL
				| SWT.MULTI);
		table = tableViewer.getTable();
		table.setLinesVisible(true);
		table.setHeaderVisible(true);

		GridDataFactory.fillDefaults().span(4, SWT.DEFAULT).grab(true, true).applyTo(table);
		tableViewer.setLabelProvider(new TViewerLabelProvider());
		tableViewer.setContentProvider(new ArrayContentProvider());

		String[] columnNames = new String[] { Messages.getString("dialog.PluginConfigurationDialog.columnNames1"),
				Messages.getString("dialog.PluginConfigurationDialog.columnNames2"),
				Messages.getString("dialog.PluginConfigurationDialog.columnNames3"),
				Messages.getString("dialog.PluginConfigurationDialog.columnNames4") };
		int[] columnAlignments = new int[] { SWT.LEFT, SWT.LEFT, SWT.LEFT, SWT.LEFT };
		for (int i = 0; i < columnNames.length; i++) {
			TableColumn tableColumn = new TableColumn(table, columnAlignments[i]);
			tableColumn.setText(columnNames[i]);
			tableColumn.setWidth(50);
		}
		// 让列表列宽动态变化
		table.addListener(SWT.Resize, new Listener() {
			public void handleEvent(Event event) {
				final Table table = ((Table) event.widget);
				final TableColumn[] columns = table.getColumns();
				event.widget.getDisplay().syncExec(new Runnable() {
					public void run() {
						double[] columnWidths = new double[] { 0.28, 0.28, 0.20, 0.20 };
						for (int i = 0; i < columns.length; i++)
							columns[i].setWidth((int) (table.getBounds().width * columnWidths[i]));
					}
				});
			}
		});

		tableViewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				editPuginConfig();
			}
		});
		refreshTable(null);

		return tparent;
	}

	public void initListener() {
		addBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				PluginConfigManageDialog dialog = new PluginConfigManageDialog(getShell(), true);
				int result = dialog.open();
				if (result == IDialogConstants.OK_ID) {
					refreshTable(dialog.getCurPluginBean());
				}
			}
		});

		editBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				editPuginConfig();
			}
		});

		deleteBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				deletePluginData();
			}
		});
	}

	@Override
	protected void okPressed() {
		super.okPressed();
	}

	public void editPuginConfig() {
		ISelection selection = tableViewer.getSelection();
		if (selection != null && !selection.isEmpty() && selection instanceof StructuredSelection) {
			StructuredSelection structuredSelection = (StructuredSelection) selection;
			Iterator<PluginConfigBean> it = structuredSelection.iterator();
			if (it.hasNext()) {
				PluginConfigBean configBean = it.next();
				PluginConfigManageDialog dialog = new PluginConfigManageDialog(getShell(), false);
				dialog.create();
				dialog.setEditInitData(configBean);
				int result = dialog.open();
				if (result == IDialogConstants.OK_ID) {
					refreshTable(dialog.getCurPluginBean());
				}
			}
		} else {
			MessageDialog.openInformation(getShell(), Messages.getString("dialog.PluginConfigurationDialog.msgTitle"),
					Messages.getString("dialog.PluginConfigurationDialog.msg1"));
		}
	}

	private void refreshTable(PluginConfigBean bean) {
		tableViewer.setInput(manage.getPluginCofigData());
		if (bean != null) {
			tableViewer.setSelection(new StructuredSelection(bean));
		}
	}

	private void deletePluginData() {
		ISelection selection = tableViewer.getSelection();
		if (selection != null && !selection.isEmpty() && selection instanceof StructuredSelection) {
			boolean response = MessageDialog.openConfirm(getShell(),
					Messages.getString("dialog.PluginConfigurationDialog.msgTitle2"),
					Messages.getString("dialog.PluginConfigurationDialog.msg2"));
			if (!response) {
				return;
			}

			StructuredSelection structuredSelection = (StructuredSelection) selection;
			@SuppressWarnings("unchecked")
			Iterator<PluginConfigBean> it = structuredSelection.iterator();

			VTDGen vg = new VTDGen();
			vg.parseFile(pluginXmlLocation, true);
			VTDNav vn = vg.getNav();
			AutoPilot ap = new AutoPilot(vn);

			try {
				XMLModifier xm = new XMLModifier(vn);

				while (it.hasNext()) {
					PluginConfigBean configBean = it.next();
					String xpath = manage.buildXpath(configBean);
					ap.selectXPath(xpath);
					while (ap.evalXPath() != -1) {
						xm.remove();
						manage.deletePluginMenu(configBean.getId());
					}
					ap.resetXPath();
				}
				FileOutputStream fos = new FileOutputStream(pluginXmlLocation);
				BufferedOutputStream bos = new BufferedOutputStream(fos);
				xm.output(bos); // 写入文件
				bos.close();
				fos.close();

				refreshTable(null);
			} catch (Exception e) {
				LOGGER.error("", e);
			}
		} else {
			MessageDialog.openInformation(getShell(), Messages.getString("dialog.PluginConfigurationDialog.msgTitle"),
					Messages.getString("dialog.PluginConfigurationDialog.msg3"));
		}
	}

	/**
	 * 语言规则列表标签提供器
	 * @author robert 2012-02-29
	 * @version
	 * @since JDK1.6
	 */
	private class TViewerLabelProvider extends LabelProvider implements ITableLabelProvider {
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}

		public String getColumnText(Object element, int columnIndex) {
			if (element instanceof PluginConfigBean) {
				PluginConfigBean bean = (PluginConfigBean) element;
				switch (columnIndex) {
				case 0:
					return bean.getName();
				case 1:
					return bean.getCommandLine();
				case 2:
					return bean.getOutput();
				case 3:
					return bean.getInput();
				default:
					return null;
				}
			}
			return null;
		}
	}
}

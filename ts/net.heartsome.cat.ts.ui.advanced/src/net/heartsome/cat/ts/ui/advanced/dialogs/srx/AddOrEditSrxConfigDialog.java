package net.heartsome.cat.ts.ui.advanced.dialogs.srx;

import java.io.File;
import java.text.MessageFormat;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import net.heartsome.cat.ts.ui.advanced.ADConstants;
import net.heartsome.cat.ts.ui.advanced.TableViewerLabelProvider;
import net.heartsome.cat.ts.ui.advanced.handlers.ADXmlHandler;
import net.heartsome.cat.ts.ui.advanced.resource.Messages;

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
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

/**
 * 添加或修改srx文件
 * @author robert
 * @version
 * @since JDK1.6
 */
public class AddOrEditSrxConfigDialog extends Dialog {
	private String curSrxName;
	private TableViewer langTableViewer;
	private Table langTable;
	private Button langAddBtn;
	private Button langEditBtn;
	private Button langDeleteBtn;
	private TableViewer mapTableViewer;
	private Table mapTable;
	private Button mapAddBtn;
	private Button mapEditBtn;
	private Button mapDeleteBtn;
	private ADXmlHandler handler;
	private IWorkspaceRoot root;
	private String srxLocation;

	public AddOrEditSrxConfigDialog(Shell parentShell, String curSrxName, ADXmlHandler handler) {
		super(parentShell);
		this.curSrxName = curSrxName;
		this.handler = handler;
		root = ResourcesPlugin.getWorkspace().getRoot();
		srxLocation = ADConstants.configLocation + ADConstants.AD_SRXConfigFolder + File.separator + curSrxName;
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(Messages.getString("srx.AddOrEditSrxConfigDialog.title"));
	}

	@Override
	protected boolean isResizable() {
		return true;
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.CANCEL_ID, Messages.getString("srx.AddOrEditSrxConfigDialog.cancel"),
				true);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite tparent = (Composite) super.createDialogArea(parent);
		GridDataFactory.fillDefaults().grab(true, true).hint(500, 500).minSize(500, 500).applyTo(tparent);

		Composite nameCmp = new Composite(tparent, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(nameCmp);
		GridLayoutFactory.fillDefaults().numColumns(2).applyTo(nameCmp);

		Label nameLbl = new Label(nameCmp, SWT.NONE);
		nameLbl.setText(Messages.getString("srx.AddOrEditSrxConfigDialog.nameLbl"));

		Text nameTxt = new Text(nameCmp, SWT.BORDER);
		nameTxt.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		nameTxt.setText(curSrxName);
		nameTxt.setEnabled(false);

		GridData groupData = new GridData(SWT.FILL, SWT.FILL, true, true);
		GridLayout groupLayout = new GridLayout(4, false);

		createLanguageGroup(tparent, groupData, groupLayout);
		createMapGroup(tparent, groupData, groupLayout);
		initListener();

		refreshLangTable(null);
		refreshMapTable(null);

		return tparent;
	}

	/**
	 * 创建语言规则配置
	 * @param tparent
	 *            ;
	 */
	private void createLanguageGroup(Composite tparent, GridData groupData, GridLayout groupLayout) {
		Group group = new Group(tparent, SWT.BORDER);
		group.setLayoutData(groupData);
		group.setLayout(groupLayout);
		group.setText(Messages.getString("srx.AddOrEditSrxConfigDialog.groupLang"));

		langTableViewer = new TableViewer(group, SWT.FULL_SELECTION | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL
				| SWT.MULTI);
		langTable = langTableViewer.getTable();
		langTable.setLinesVisible(true);
		langTable.setHeaderVisible(true);

		GridDataFactory.fillDefaults().span(4, SWT.DEFAULT).grab(true, true).applyTo(langTable);
		langTableViewer.setLabelProvider(new TableViewerLabelProvider());
		langTableViewer.setContentProvider(new ArrayContentProvider());

		String[] columnNames = new String[] { Messages.getString("srx.AddOrEditSrxConfigDialog.langColumnNames1"),
				Messages.getString("srx.AddOrEditSrxConfigDialog.langColumnNames2") };
		int[] columnAlignments = new int[] { SWT.LEFT, SWT.LEFT };
		for (int i = 0; i < columnNames.length; i++) {
			TableColumn tableColumn = new TableColumn(langTable, columnAlignments[i]);
			tableColumn.setText(columnNames[i]);
			tableColumn.setWidth(50);
		}
		refreshTableWidth(langTable);

		langAddBtn = new Button(group, SWT.NONE);
		langAddBtn.setText(Messages.getString("srx.AddOrEditSrxConfigDialog.langAddBtn"));
		setButtonLayoutData(langAddBtn);

		langEditBtn = new Button(group, SWT.NONE);
		langEditBtn.setText(Messages.getString("srx.AddOrEditSrxConfigDialog.langEditBtn"));
		setButtonLayoutData(langEditBtn);

		langDeleteBtn = new Button(group, SWT.NONE);
		langDeleteBtn.setText(Messages.getString("srx.AddOrEditSrxConfigDialog.langDeleteBtn"));
		setButtonLayoutData(langDeleteBtn);

		new Label(group, SWT.NONE);

		langTableViewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				editLangRules();
			}
		});
	}

	/**
	 * 创建映身
	 * @param tparent
	 *            ;
	 */
	private void createMapGroup(Composite tparent, GridData groupData, GridLayout groupLayout) {
		Group group = new Group(tparent, SWT.BORDER);
		group.setLayoutData(groupData);
		group.setLayout(groupLayout);
		group.setText(Messages.getString("srx.AddOrEditSrxConfigDialog.group"));

		mapTableViewer = new TableViewer(group, SWT.FULL_SELECTION | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL
				| SWT.MULTI);
		mapTable = mapTableViewer.getTable();
		mapTable.setLinesVisible(true);
		mapTable.setHeaderVisible(true);

		GridDataFactory.fillDefaults().span(4, SWT.DEFAULT).grab(true, true).applyTo(mapTable);
		mapTableViewer.setLabelProvider(new TableViewerLabelProvider());
		mapTableViewer.setContentProvider(new ArrayContentProvider());

		String[] columnNames = new String[] { Messages.getString("srx.AddOrEditSrxConfigDialog.columnNames1"),
				Messages.getString("srx.AddOrEditSrxConfigDialog.columnNames2") };
		int[] columnAlignments = new int[] { SWT.LEFT, SWT.LEFT };
		for (int i = 0; i < columnNames.length; i++) {
			TableColumn tableColumn = new TableColumn(mapTable, columnAlignments[i]);
			tableColumn.setText(columnNames[i]);
			tableColumn.setWidth(50);
		}
		refreshTableWidth(mapTable);

		mapAddBtn = new Button(group, SWT.NONE);
		mapAddBtn.setText(Messages.getString("srx.AddOrEditSrxConfigDialog.mapAddBtn"));
		setButtonLayoutData(mapAddBtn);

		mapEditBtn = new Button(group, SWT.NONE);
		mapEditBtn.setText(Messages.getString("srx.AddOrEditSrxConfigDialog.mapEditBtn"));
		setButtonLayoutData(mapEditBtn);

		mapDeleteBtn = new Button(group, SWT.NONE);
		mapDeleteBtn.setText(Messages.getString("srx.AddOrEditSrxConfigDialog.mapDeleteBtn"));
		setButtonLayoutData(mapDeleteBtn);

		new Label(group, SWT.NONE);

		mapTableViewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				editMapRules();
			}
		});
	}

	@Override
	protected void okPressed() {
		// TODO Auto-generated method stub
		super.okPressed();
	}

	public void initListener() {
		// 语言规则下添加按钮
		langAddBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				SrxLanguageRulesManageDialog dialog = new SrxLanguageRulesManageDialog(getShell(), true, handler,
						srxLocation);
				int result = dialog.open();
				if (result == IDialogConstants.OK_ID) {
					refreshLangTable(dialog.getCurLanguageRuleName());
				}
			}
		});

		langEditBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				editLangRules();
			}
		});
		langDeleteBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				ISelection selection = langTableViewer.getSelection();
				if (!selection.isEmpty() && selection != null && selection instanceof StructuredSelection) {
					boolean response = MessageDialog.openConfirm(getShell(),
							Messages.getString("srx.AddOrEditSrxConfigDialog.msgTitle1"),
							Messages.getString("srx.AddOrEditSrxConfigDialog.msg1"));
					if (!response) {
						return;
					}
					StructuredSelection struSelection = (StructuredSelection) selection;
					@SuppressWarnings("unchecked")
					Iterator<String[]> it = struSelection.iterator();
					List<String> deleRuleNameList = new LinkedList<String>();
					List<String> mapedRuleNameList = new LinkedList<String>();
					while (it.hasNext()) {
						// 获取所选中的语言规则的名称
						String langRuleName = it.next()[1];

						if (handler.checkLangRuleNameMaped(srxLocation, langRuleName)) {
							mapedRuleNameList.add(langRuleName);
							continue;
						}
						deleRuleNameList.add(langRuleName);
					}

					// 如果要删除的语言规则已经被映射，提示是否删除
					if (mapedRuleNameList.size() > 0) {
						String mapedNameStr = "";
						for (int i = 0; i < mapedRuleNameList.size(); i++) {
							mapedNameStr += " '" + mapedRuleNameList.get(i) + "'、";
						}
						mapedNameStr = mapedNameStr.substring(0, mapedNameStr.length() - 1);

						boolean deleResponse = MessageDialog.openConfirm(getShell(), Messages
								.getString("srx.AddOrEditSrxConfigDialog.msgTitle1"), MessageFormat.format(
								Messages.getString("srx.AddOrEditSrxConfigDialog.msg2"), mapedNameStr));
						if (deleResponse) {
							for (int i = 0; i < mapedRuleNameList.size(); i++) {
								String langRuleName = mapedRuleNameList.get(i);
								handler.deleteNode(srxLocation,
										"/srx/body/languagerules/languagerule[@languagerulename='" + langRuleName
												+ "']");
								handler.deleteNode(srxLocation,
										"/srx/body/maprules/maprule/languagemap[@languagerulename='" + langRuleName
												+ "']");
							}
						}
						refreshMapTable(null);
					}

					for (int i = 0; i < deleRuleNameList.size(); i++) {
						String langRuleName = deleRuleNameList.get(i);
						handler.deleteNode(srxLocation, "/srx/body/languagerules/languagerule[@languagerulename='"
								+ langRuleName + "']");
					}

					refreshLangTable(null);
				} else {
					MessageDialog.openInformation(getShell(),
							Messages.getString("srx.AddOrEditSrxConfigDialog.msgTitle2"),
							Messages.getString("srx.AddOrEditSrxConfigDialog.msg3"));
				}
			}
		});

		mapAddBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				SrxMapRulesManageDialog dialog = new SrxMapRulesManageDialog(getShell(), true, handler, srxLocation);
				int result = dialog.open();
				if (result == IDialogConstants.OK_ID) {
					refreshMapTable(dialog.getCurMapRuleName());
				}
			}
		});
		mapEditBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				editMapRules();
			}
		});
		mapDeleteBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				ISelection selection = mapTableViewer.getSelection();
				if (!selection.isEmpty() && selection != null && selection instanceof StructuredSelection) {
					boolean response = MessageDialog.openConfirm(getShell(),
							Messages.getString("srx.AddOrEditSrxConfigDialog.msgTitle1"),
							Messages.getString("srx.AddOrEditSrxConfigDialog.msg4"));
					if (!response) {
						return;
					}
					StructuredSelection struSelection = (StructuredSelection) selection;
					@SuppressWarnings("unchecked")
					Iterator<String[]> it = struSelection.iterator();
					while (it.hasNext()) {
						// 获取所选中的语言规则的名称
						String mapRuleName = it.next()[1];
						handler.deleteNode(srxLocation, "/srx/body/maprules/maprule[@maprulename='" + mapRuleName
								+ "']");
					}
					refreshMapTable(null);
				} else {
					MessageDialog.openInformation(getShell(),
							Messages.getString("srx.AddOrEditSrxConfigDialog.msgTitle2"),
							Messages.getString("srx.AddOrEditSrxConfigDialog.msg5"));
				}
			}
		});

	}

	/**
	 * 动态改变两个列表的列宽
	 * @param table
	 *            ;
	 */
	private void refreshTableWidth(Table table) {
		table.addListener(SWT.Resize, new Listener() {
			public void handleEvent(Event event) {
				final Table table = ((Table) event.widget);
				final TableColumn[] columns = table.getColumns();
				event.widget.getDisplay().syncExec(new Runnable() {
					public void run() {
						double[] columnWidths = new double[] { 0.25, 0.7 };
						for (int i = 0; i < columns.length; i++)
							columns[i].setWidth((int) (table.getBounds().width * columnWidths[i]));
					}
				});
			}
		});
	}

	/**
	 * 更新语言规则列表 ;
	 */
	private void refreshLangTable(String langRuleName) {
		langTableViewer.setInput(handler.getLanguageRuleNamesOfSrx_1(srxLocation).toArray(new String[][] {}));
		if (langRuleName != null) {
			TableItem[] items = langTable.getItems();
			for (int i = 0; i < items.length; i++) {
				if (langRuleName.equals(items[i].getText(1))) {
					langTable.setSelection(i);
				}
			}
		}
	}

	/**
	 * 更新映射规则列表 ;
	 */
	private void refreshMapTable(String mapRuleName) {
		mapTableViewer.setInput(handler.getMapRuleNames(srxLocation).toArray(new String[][] {}));
		if (mapRuleName != null) {
			TableItem[] items = mapTable.getItems();
			for (int i = 0; i < items.length; i++) {
				if (mapRuleName.equals(items[i].getText(1))) {
					mapTable.setSelection(i);
				}
			}
		}
	}

	/**
	 * 修改语言规则，备注，在修改语言规则名称时，也会同步修改映射中语言规则的名称 ;
	 */
	private void editLangRules() {
		ISelection selection = langTableViewer.getSelection();
		if (!selection.isEmpty() && selection != null && selection instanceof StructuredSelection) {
			StructuredSelection struSelection = (StructuredSelection) selection;
			@SuppressWarnings("unchecked")
			Iterator<String[]> it = struSelection.iterator();
			if (it.hasNext()) {
				// 获取所选中的语言规则的名称
				String langRuleName = it.next()[1];
				SrxLanguageRulesManageDialog dialog = new SrxLanguageRulesManageDialog(getShell(), false, handler,
						srxLocation);
				dialog.create();
				dialog.setEditInitData(langRuleName);
				int result = dialog.open();
				if (result == IDialogConstants.OK_ID) {
					refreshLangTable(dialog.getCurLanguageRuleName());
				}
			}
		} else {
			MessageDialog.openInformation(getShell(), Messages.getString("srx.AddOrEditSrxConfigDialog.msgTitle2"),
					Messages.getString("srx.AddOrEditSrxConfigDialog.msg6"));
		}
	}

	public void editMapRules() {
		ISelection selection = mapTableViewer.getSelection();
		if (!selection.isEmpty() && selection != null && selection instanceof StructuredSelection) {
			StructuredSelection struSelection = (StructuredSelection) selection;
			@SuppressWarnings("unchecked")
			Iterator<String[]> it = struSelection.iterator();
			if (it.hasNext()) {
				// 获取所选中的映射规则的名称
				String mapRuleName = it.next()[1];
				SrxMapRulesManageDialog dialog = new SrxMapRulesManageDialog(getShell(), false, handler, srxLocation);
				dialog.create();
				dialog.setEditInitData(mapRuleName);
				int result = dialog.open();
				if (result == IDialogConstants.OK_ID) {
					refreshMapTable(dialog.getCurMapRuleName());
				}
			}
		} else {
			MessageDialog.openInformation(getShell(), Messages.getString("srx.AddOrEditSrxConfigDialog.msgTitle2"),
					Messages.getString("srx.AddOrEditSrxConfigDialog.msg7"));
		}
	}
}

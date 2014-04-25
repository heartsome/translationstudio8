package net.heartsome.cat.ts.ui.advanced.dialogs.srx;

import java.text.MessageFormat;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import net.heartsome.cat.ts.ui.advanced.handlers.ADXmlHandler;
import net.heartsome.cat.ts.ui.advanced.model.MapRuleBean;
import net.heartsome.cat.ts.ui.advanced.resource.Messages;

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
import org.eclipse.swt.widgets.Text;

public class SrxMapRulesManageDialog extends Dialog {
	private Button addBtn;
	private Button editBtn;
	private Button deleteBtn;
	private Text nameTxt;
	private TableViewer tableViewer;
	private Table table;
	private List<MapRuleBean> mapRulesList = new LinkedList<MapRuleBean>();
	private boolean isAdd;
	private ADXmlHandler handler;
	private String srxLocation;
	private String curMapRuleName;

	public SrxMapRulesManageDialog(Shell parentShell, boolean isAdd, ADXmlHandler handler, String srxLocation) {
		super(parentShell);
		this.isAdd = isAdd;
		this.handler = handler;
		this.srxLocation = srxLocation;
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(Messages.getString("srx.SrxMapRulesManageDialog.title"));
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
				Messages.getString("srx.SrxMapRulesManageDialog.addBtn"), false);
		editBtn = createButton(leftCmp, IDialogConstants.CLIENT_ID,
				Messages.getString("srx.SrxMapRulesManageDialog.editBtn"), false);
		deleteBtn = createButton(leftCmp, IDialogConstants.CLIENT_ID,
				Messages.getString("srx.SrxMapRulesManageDialog.deleteBtn"), false);

		Composite rightCmp = new Composite(buttonCmp, SWT.NONE);
		GridLayoutFactory.fillDefaults().extendedMargins(0, 0, 0, 0).numColumns(1).equalWidth(false).applyTo(rightCmp);

		new Label(rightCmp, SWT.NONE);

		Label separatorLbl = new Label(buttonCmp, SWT.HORIZONTAL | SWT.SEPARATOR);
		GridDataFactory.fillDefaults().span(2, SWT.DEFAULT).applyTo(separatorLbl);

		new Label(buttonCmp, SWT.NONE);
		Composite bottomCmp = new Composite(buttonCmp, SWT.NONE);
		GridDataFactory.fillDefaults().grab(false, false).applyTo(bottomCmp);
		GridLayoutFactory.fillDefaults().extendedMargins(0, 0, 0, 0).numColumns(2).applyTo(bottomCmp);

		createButton(bottomCmp, IDialogConstants.OK_ID, Messages.getString("srx.SrxMapRulesManageDialog.ok"), false);
		createButton(bottomCmp, IDialogConstants.CANCEL_ID, Messages.getString("srx.SrxMapRulesManageDialog.cancel"),
				true).setFocus();

		initListener();

		return buttonCmp;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite tparent = (Composite) super.createDialogArea(parent);
		GridDataFactory.fillDefaults().grab(true, true).hint(600, 500).minSize(600, 500).applyTo(tparent);
		Composite nameCmp = new Composite(tparent, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(nameCmp);
		GridLayoutFactory.fillDefaults().numColumns(2).applyTo(nameCmp);
		Label nameLbl = new Label(nameCmp, SWT.NONE);
		nameLbl.setText(Messages.getString("srx.SrxMapRulesManageDialog.nameLbl"));
		nameTxt = new Text(nameCmp, SWT.BORDER);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(nameTxt);
		createTable(tparent);
		return tparent;
	}

	private void createTable(Composite tparent) {
		tableViewer = new TableViewer(tparent, SWT.FULL_SELECTION | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL
				| SWT.MULTI);
		table = tableViewer.getTable();
		table.setLinesVisible(true);
		table.setHeaderVisible(true);

		GridDataFactory.fillDefaults().span(4, SWT.DEFAULT).grab(true, true).applyTo(table);
		tableViewer.setLabelProvider(new TViewerLabelProvider());
		tableViewer.setContentProvider(new ArrayContentProvider());

		String[] columnNames = new String[] { Messages.getString("srx.SrxMapRulesManageDialog.columnNames1"),
				Messages.getString("srx.SrxMapRulesManageDialog.columnNames2") };
		int[] columnAlignments = new int[] { SWT.LEFT, SWT.LEFT };
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
						double[] columnWidths = new double[] { 0.2, 0.77 };
						for (int i = 0; i < columns.length; i++)
							columns[i].setWidth((int) (table.getBounds().width * columnWidths[i]));
					}
				});
			}
		});

		tableViewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				editMapRule();
			}
		});
	}

	public void initListener() {
		addBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				AddOrEditMapRuleOfSrxDialog dialog = new AddOrEditMapRuleOfSrxDialog(getShell(), true, mapRulesList,
						handler, srxLocation);
				int result = dialog.open();
				if (result == IDialogConstants.OK_ID) {
					refreshTable(dialog.getCurMapRuleBean());
				}
			}
		});
		editBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				editMapRule();
			}
		});
		deleteBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				ISelection selection = tableViewer.getSelection();
				if (!selection.isEmpty() && selection != null && selection instanceof StructuredSelection) {
					boolean respose = MessageDialog.openConfirm(getShell(),
							Messages.getString("srx.SrxMapRulesManageDialog.msgTitle"),
							Messages.getString("srx.SrxMapRulesManageDialog.msg1"));
					if (!respose) {
						return;
					}

					StructuredSelection structSelection = (StructuredSelection) selection;
					@SuppressWarnings("unchecked")
					Iterator<MapRuleBean> it = structSelection.iterator();
					while (it.hasNext()) {
						mapRulesList.remove(it.next());
					}
					refreshTable(null);
				} else {
					MessageDialog.openInformation(getShell(), Messages.getString("srx.SrxMapRulesManageDialog.msgTitle2"),
							Messages.getString("srx.SrxMapRulesManageDialog.msg2"));
				}
			}
		});
	}

	/**
	 * 更新语言列表
	 * @param bean
	 *            ;
	 */
	private void refreshTable(MapRuleBean bean) {
		tableViewer.setInput(mapRulesList);
		if (bean != null) {
			// 定位操作
			tableViewer.setSelection(new StructuredSelection(bean));
		}
	}

	@Override
	protected void okPressed() {
		String mapRuleName = nameTxt.getText().trim();
		if (mapRuleName == null || "".equals(mapRuleName)) {
			MessageDialog.openInformation(getShell(), Messages.getString("srx.SrxMapRulesManageDialog.msgTitle2"),
					Messages.getString("srx.SrxMapRulesManageDialog.msg3"));
			nameTxt.setFocus();
			return;
		}

		if (isAdd) {
			// 先验证所添加的语言规则名是否存在，如果存在就提示
			boolean isExist = handler.validNodeExist(srxLocation, "/srx/body/maprules/maprule[@maprulename='"
					+ mapRuleName + "']");
			if (isExist) {
				MessageDialog.openInformation(getShell(), Messages.getString("srx.SrxMapRulesManageDialog.msgTitle2"),
						MessageFormat.format(Messages.getString("srx.SrxMapRulesManageDialog.msg4"), mapRuleName));
				return;
			}

			String languageRulesData = buildLangRules(mapRuleName);
			boolean addResult = handler.addMapRules(srxLocation, languageRulesData);
			if (!addResult) {
				MessageDialog.openInformation(getShell(), Messages.getString("srx.SrxMapRulesManageDialog.msgTitle2"),
						Messages.getString("srx.SrxMapRulesManageDialog.msg5"));
			}

		} else {
			// 在编辑的情况
			// 首先判断语言规则节点名是否被修改，如果被修改，要判断其修改后的节点名是否已经存在，如果存在，则提示是否覆盖
			if (!curMapRuleName.equals(mapRuleName)) {
				boolean isExist = handler.validNodeExist(srxLocation, "/srx/body/maprules/maprule[@maprulename='"
						+ mapRuleName + "']");
				if (isExist) {
					boolean response = MessageDialog.openConfirm(getShell(),
							Messages.getString("srx.SrxMapRulesManageDialog.msgTitle"),
							MessageFormat.format(Messages.getString("srx.SrxMapRulesManageDialog.msg6"), mapRuleName));
					// 如果选择覆盖，那么先删除与修改后同节点名的那个节点，再修改相对应的数据
					if (response) {
						handler.deleteNode(srxLocation, "/srx/body/maprules/maprule[@maprulename='" + mapRuleName
								+ "']");
					} else {
						return;
					}
				}
			}
			String newData = buildLangRules(mapRuleName);
			handler.updataDataToXml(srxLocation, "/srx/body/maprules/maprule[@maprulename='" + curMapRuleName + "']",
					newData);
		}

		curMapRuleName = mapRuleName;
		super.okPressed();
	}

	public void editMapRule() {
		ISelection selection = tableViewer.getSelection();
		if (!selection.isEmpty() && selection != null && selection instanceof StructuredSelection) {
			StructuredSelection structSelection = (StructuredSelection) selection;
			@SuppressWarnings("unchecked")
			Iterator<MapRuleBean> it = structSelection.iterator();
			if (it.hasNext()) {
				MapRuleBean selectBean = it.next();
				AddOrEditMapRuleOfSrxDialog dialog = new AddOrEditMapRuleOfSrxDialog(getShell(), false, mapRulesList,
						handler, srxLocation);
				dialog.create();
				dialog.setEditInitData(selectBean);
				int result = dialog.open();
				if (result == IDialogConstants.OK_ID) {
					refreshTable(dialog.getCurMapRuleBean());
				}

			}
			refreshTable(null);
		} else {
			MessageDialog.openInformation(getShell(), Messages.getString("srx.SrxMapRulesManageDialog.msgTitle2"),
					Messages.getString("srx.SrxMapRulesManageDialog.msg2"));
		}
	}

	public String getCurMapRuleName() {
		return curMapRuleName;
	}

	public void setEditInitData(String mapRuleName) {
		curMapRuleName = mapRuleName;
		nameTxt.setText(mapRuleName);
		mapRulesList = handler.getMapRulesByName(srxLocation, mapRuleName);
		refreshTable(null);

	}

	private String buildLangRules(String mapRuleName) {
		StringBuffer mapRuleSB = new StringBuffer();
		mapRuleSB.append(MessageFormat.format("<maprule maprulename=\"{0}\">\n", mapRuleName));
		for (int i = 0; i < mapRulesList.size(); i++) {
			MapRuleBean mapRuleBean = mapRulesList.get(i);
			String languageModel = mapRuleBean.getLanguageModel();
			String languageRuleName = mapRuleBean.getLangRuleName();

			mapRuleSB.append(MessageFormat.format(
					"\t<languagemap languagepattern=\"{0}\" languagerulename=\"{1}\"/>\n", new Object[] {
							languageModel, languageRuleName }));
		}
		mapRuleSB.append(MessageFormat.format("</maprule>\n", mapRuleName));

		return mapRuleSB.toString();
	}

	/**
	 * 印射规则列表标签提供器
	 * @author robert 2012-03-02
	 * @version
	 * @since JDK1.6
	 */
	private class TViewerLabelProvider extends LabelProvider implements ITableLabelProvider {
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}

		public String getColumnText(Object element, int columnIndex) {
			if (element instanceof MapRuleBean) {
				MapRuleBean bean = (MapRuleBean) element;
				switch (columnIndex) {
				case 0:
					return bean.getLanguageModel();
				case 1:
					return bean.getLangRuleName();
				default:
					return null;
				}
			}
			return null;
		}
	}
}

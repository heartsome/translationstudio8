/**
 * ExportFilterSettingDialog.java
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
package net.heartsome.cat.database.ui.dialog;

import java.util.ArrayList;
import java.util.List;

import net.heartsome.cat.database.bean.ExportFilterBean;
import net.heartsome.cat.database.bean.ExportFilterComponentBean;
import net.heartsome.cat.database.ui.core.ExportFilterStoreConfiger;
import net.heartsome.cat.database.ui.resource.Messages;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExportFilterSettingDialog extends Dialog {
	public static final Logger logger = LoggerFactory.getLogger(ExportFilterSettingDialog.class);
	private Text filterNameText;
	private Composite dynaComposite;
	private Button isAllCbtn;
	private Button isAnyCbtn;
	private String ruleType = "TMX"; // 默认为TMX

	private ExportFilterBean currentFilter;
	private ExportFilterStoreConfiger filterStore;

	/**
	 * 创建对话框
	 * @param parentShell
	 * @param ruleType
	 *            过滤规则设置用途,必须为"TMX"或者"TBX"
	 */
	public ExportFilterSettingDialog(Shell parentShell, String ruleType) {
		super(parentShell);
		Assert.isLegal(ruleType.equals("TMX") || ruleType.equals("TBX"),
				Messages.getString("dialog.ExportFilterSettingDialog.msg1"));
		this.ruleType = ruleType;
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(Messages.getString("dialog.ExportFilterSettingDialog.title"));
	}

	protected boolean isResizable() {
		return true;
	}

	/**
	 * Create contents of the dialog.
	 * @param parent
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		container.setLayout(new GridLayout(1, false));

		Composite composite = new Composite(container, SWT.NONE);
		composite.setLayout(new GridLayout(2, false));
		composite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		Label filterNameLabel = new Label(composite, SWT.NONE);
		filterNameLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		filterNameLabel.setText(Messages.getString("dialog.ExportFilterSettingDialog.filterNameLabel"));

		filterNameText = new Text(composite, SWT.BORDER);
		filterNameText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		Composite optionComposite = new Composite(container, SWT.NONE);
		optionComposite.setLayout(new GridLayout(2, false));
		optionComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		isAllCbtn = new Button(optionComposite, SWT.RADIO);
		isAllCbtn.setSize(152, 26);
		isAllCbtn.setText(Messages.getString("dialog.ExportFilterSettingDialog.isAllCbtn"));
		isAllCbtn.setSelection(true);

		isAnyCbtn = new Button(optionComposite, SWT.RADIO);
		isAnyCbtn.setText(Messages.getString("dialog.ExportFilterSettingDialog.isAnyCbtn"));

		ScrolledComposite scrolledComposite = new ScrolledComposite(container, SWT.V_SCROLL | SWT.BORDER);
		scrolledComposite.setAlwaysShowScrollBars(false);
		scrolledComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		scrolledComposite.setExpandHorizontal(true);
		scrolledComposite.setExpandVertical(true);

		dynaComposite = new Composite(scrolledComposite, SWT.NONE);
		dynaComposite.setBackground(Display.getDefault().getSystemColor((SWT.COLOR_WHITE)));
		scrolledComposite.setContent(dynaComposite);
		scrolledComposite.setMinSize(dynaComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		GridLayout gl_dynaComposite = new GridLayout(1, false);
		dynaComposite.setLayout(gl_dynaComposite);

		ExportFilterComposite exportFilterComponent = new ExportFilterComposite(dynaComposite, SWT.None, ruleType);
		exportFilterComponent.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		exportFilterComponent.setDeleteButtonEnabled(false);
		dynaComposite.setData("currentNumber", 1);

		scrolledComposite.setMinSize(dynaComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT));

		initData();

		return container;
	}

	private void initData() {
		if (this.currentFilter != null) {
			Control[] c = dynaComposite.getChildren();
			for (int i = 0; i < c.length; i++) {
				c[i].dispose();
			}
			filterNameText.setText(currentFilter.getFilterName());
			String filterConnector = currentFilter.getFilterConnector();

			if (filterConnector.equals("AND")) {
				isAllCbtn.setSelection(true);
				isAnyCbtn.setSelection(false);
			} else {
				isAllCbtn.setSelection(false);
				isAnyCbtn.setSelection(true);
			}

			List<ExportFilterComponentBean> optionList = currentFilter.getFilterOption();
			if (optionList.size() > 0) {
				ExportFilterComponentBean bean = optionList.get(0);
				ExportFilterComposite exportFilterComponent = new ExportFilterComposite(dynaComposite, SWT.None,
						ruleType, bean);
				exportFilterComponent.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
				exportFilterComponent.setDeleteButtonEnabled(false);
				dynaComposite.setData("currentNumber", 1);
				for (int i = 1; i < optionList.size(); i++) {
					bean = optionList.get(i);
					exportFilterComponent.addComponent(dynaComposite, bean);
				}
			}
		}
	}

	@Override
	protected void okPressed() {
		String filterName = this.filterNameText.getText();
		if (filterName == null || filterName.equals("")) {
			MessageDialog.openInformation(getShell(), Messages.getString("dialog.ExportFilterSettingDialog.msgTitle"),
					Messages.getString("dialog.ExportFilterSettingDialog.msg2"));
			return;
		}

		String filterConnector = "AND";
		if (isAnyCbtn.getSelection()) {
			filterConnector = "OR";
		}

		List<ExportFilterComponentBean> filterList = new ArrayList<ExportFilterComponentBean>();
		Control[] c = dynaComposite.getChildren();
		for (int i = 0; i < c.length; i++) {
			if (c[i] instanceof ExportFilterComposite) {
				ExportFilterComposite comp = (ExportFilterComposite) c[i];
				ExportFilterComponentBean bean = comp.getValue();
				if (bean.getFilterVlaue() != null && !bean.getFilterVlaue().equals("")) {
					filterList.add(bean);
				}
			}
		}

		if (currentFilter != null) { // 编辑状态,策略:删除原有的内容,重新执行添加
			filterStore.deleteFilterRuleByName(currentFilter.getFilterName(), currentFilter.getFilterType());
		}

		if (filterStore.isFilterNameExist(filterName, getRuleType())) {
			MessageDialog.openInformation(getShell(), Messages.getString("dialog.ExportFilterSettingDialog.msgTitle"),
					Messages.getString("dialog.ExportFilterSettingDialog.msg3"));
			return;
		}

		if (filterList.size() == 0) {
			logger.error(Messages.getString("dialog.ExportFilterSettingDialog.logger1"));
			MessageDialog.openInformation(getShell(), Messages.getString("dialog.ExportFilterSettingDialog.msgTitle"),
					Messages.getString("dialog.ExportFilterSettingDialog.msg4"));
			return;
		}

		currentFilter = new ExportFilterBean();
		currentFilter.setFilterName(filterName);
		currentFilter.setFilterOption(filterList);
		currentFilter.setFilterType(getRuleType());
		currentFilter.setFilterConnector(filterConnector);
		filterStore.saveFilterRule(currentFilter);
		super.okPressed();
	}

	/**
	 * 获取设置结果
	 */
	public ExportFilterBean getSettingResult() {
		return this.currentFilter;
	}

	/**
	 * 设置当前需要编辑的Filter;
	 * @param currentFilter
	 *            ;
	 */
	public void setCurrentFilter(ExportFilterBean currentFilter) {
		this.currentFilter = currentFilter;
	}

	/**
	 * 获取过滤规则类型TMX/TBX
	 * @return "TMX" - 为TMX过滤规则 "TBX"-为TBX过滤规则
	 */
	public String getRuleType() {
		return ruleType;
	}

	/**
	 * @param filterStore
	 *            the filterStore to set
	 */
	public void setFilterStore(ExportFilterStoreConfiger filterStore) {
		this.filterStore = filterStore;
	}

	/**
	 * Create contents of the button bar.
	 * @param parent
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	}

	/**
	 * Return the initial size of the dialog.
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(580, 448);
	}
}

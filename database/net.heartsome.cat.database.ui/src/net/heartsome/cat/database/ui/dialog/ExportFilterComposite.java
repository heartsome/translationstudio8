package net.heartsome.cat.database.ui.dialog;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import net.heartsome.cat.database.bean.ExportFilterComponentBean;
import net.heartsome.cat.database.ui.resource.Messages;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

public class ExportFilterComposite extends Composite implements Listener {

	private ExportFilterComponentBean baseDataBean;
	private String[] filterNames;
	private Control valueText;
	private ComboViewer conditionComboViewer;
	private ComboViewer opratorComboViewer;
	private Button addButton;
	private Button deleteButton;
	private Composite dynaComposite;

	private String ruleType;

	/**
	 * Create the composite.
	 * @param parent
	 * @param style
	 */
	public ExportFilterComposite(Composite parent, int style, String ruleType) {
		super(parent, style);
		this.ruleType = ruleType;
		this.baseDataBean = new ExportFilterComponentBean(ruleType);
		this.filterNames = baseDataBean.getFilterNames();
		this.createContent();
		this.initData();
	}

	public ExportFilterComposite(Composite parent, int style, String ruleType, ExportFilterComponentBean baseDataBean) {
		super(parent, style);
		this.ruleType = ruleType;
		this.baseDataBean = baseDataBean;
		this.filterNames = baseDataBean.getFilterNames();
		this.createContent();
		this.initData();
	}

	public void handleEvent(Event event) {
		Composite parent = this.getParent();
		Composite topParent = parent.getParent();
		if (event.widget == addButton) { // add button
			ExportFilterComponentBean bean = new ExportFilterComponentBean(this.ruleType);
			bean.setOptionName(this.baseDataBean.getOptionName());
			bean.setCurrentExpression(this.baseDataBean.getCurrentExpression());

			addComponent(parent, bean);

		} else if (event.widget == deleteButton) { // delete button
			this.dispose();
			Integer number = (Integer) parent.getData("currentNumber") - 1;
			parent.setData("currentNumber", number);
			if (number == 1) {
				Control c = parent.getChildren()[0];
				if (c instanceof ExportFilterComposite) {
					ExportFilterComposite temp = (ExportFilterComposite) c;
					temp.setDeleteButtonEnabled(false);
				}
			}
		}

		if (topParent instanceof ScrolledComposite) {
			((ScrolledComposite) topParent).setMinSize(parent.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		}
		parent.layout(true);
	}

	/**
	 * 增加一个组件
	 * @param parent
	 *            组件容器
	 * @param bean
	 *            {@link ExportFilterComponentBean};
	 */
	public void addComponent(Composite parent, ExportFilterComponentBean bean) {
		Integer number = (Integer) parent.getData("currentNumber") + 1;
		parent.setData("currentNumber", number);
		if (number == 2) {
			Control c = parent.getChildren()[0];
			if (c instanceof ExportFilterComposite) {
				ExportFilterComposite temp = (ExportFilterComposite) c;
				temp.setDeleteButtonEnabled(true);
			}
		}

		ExportFilterComposite tempComponent = new ExportFilterComposite(parent, SWT.None, this.ruleType, bean);
		tempComponent.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
	}

	/**
	 * 初始化数据
	 */
	private void initData() {
		// 过滤规则名称 此方法设置选中将不会触发viewer的selectionChanged事件
		String[] options = baseDataBean.getFilterNames();
		String currentOption = baseDataBean.getOptionName();
		for (int i = 0; i < options.length; i++) {
			if (currentOption.equals(options[i])) {
				conditionComboViewer.getCombo().select(i);
				break;
			}
		}
		// conditionComboViewer.setSelection(new StructuredSelection(baseDataBean.getOptionName()));

		// 过滤条件
		opratorComboViewer.setSelection(new StructuredSelection(baseDataBean.getCurrentExpression()));

		handlerFilterChangedEvent();
	}

	/** 创建控件 */
	private void createContent() {
		setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
		GridLayout gridLayout = new GridLayout(5, false);
		gridLayout.horizontalSpacing = 2;
		gridLayout.marginWidth = 0;
		gridLayout.marginHeight = 0;
		setLayout(gridLayout);

		conditionComboViewer = new ComboViewer(this, SWT.NONE | SWT.READ_ONLY);
		Combo conditionCombo = conditionComboViewer.getCombo();
		GridData gdConditionCombo = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
		gdConditionCombo.widthHint = 200;
		conditionCombo.setLayoutData(gdConditionCombo);
		conditionComboViewer.setContentProvider(new ArrayContentProvider());
		conditionComboViewer.setInput(filterNames);

		conditionComboViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection sel = (IStructuredSelection) event.getSelection();
				if (sel.isEmpty()) {
					return;
				}
				baseDataBean.setOptionName((String) sel.getFirstElement());

				handlerFilterChangedEvent();
				opratorComboViewer.setInput(baseDataBean.getCurrentFilterExpressions());
				opratorComboViewer.getCombo().select(0); // 默认选中第一个
				baseDataBean.setCurrentExpression(opratorComboViewer.getCombo().getText());
			}
		});
		opratorComboViewer = new ComboViewer(this, SWT.NONE | SWT.READ_ONLY);
		Combo opratorCombo = opratorComboViewer.getCombo();
		GridData gd_opratorCombo = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
		gd_opratorCombo.widthHint = 100;
		opratorCombo.setLayoutData(gd_opratorCombo);
		opratorComboViewer.setContentProvider(new ArrayContentProvider());
		opratorComboViewer.setInput(this.baseDataBean.getCurrentFilterExpressions());
		opratorComboViewer.addSelectionChangedListener(new ISelectionChangedListener() {

			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection sel = (IStructuredSelection) event.getSelection();
				if (sel.isEmpty()) {
					return;
				}
				baseDataBean.setCurrentExpression((String) sel.getFirstElement());
			}
		});

		dynaComposite = new Composite(this, SWT.NONE);
		dynaComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		GridLayout gldynaComposite = new GridLayout(1, false);
		gldynaComposite.marginWidth = 0;
		gldynaComposite.marginHeight = 0;
		dynaComposite.setLayout(gldynaComposite);
		valueText = new Text(dynaComposite, SWT.BORDER);
		valueText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		addButton = new Button(this, SWT.NONE);
		GridData gdAddButton = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gdAddButton.widthHint = 25;
		addButton.setLayoutData(gdAddButton);
		addButton.setText("+");
		addButton.addListener(SWT.Selection, this);

		deleteButton = new Button(this, SWT.NONE);
		GridData gdDeletebutton = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gdDeletebutton.widthHint = 25;
		deleteButton.setLayoutData(gdDeletebutton);
		deleteButton.setText("-");
		deleteButton.addListener(SWT.Selection, this);
	}

	/**
	 * 处理过滤条件发生变化时动态创建组件 ;
	 */
	private void handlerFilterChangedEvent() {
		String optionName = baseDataBean.getOptionName();
		String value = baseDataBean.getFilterVlaue();
		if (optionName.equals(Messages.getString("dialog.ExportFilterComposite.creationDate"))
				|| optionName.equals(Messages.getString("dialog.ExportFilterComposite.changeDate"))) {
			if (!valueText.isDisposed()) {
				valueText.dispose();
			}
			DateTime valueTextDateTime = new DateTime(dynaComposite, SWT.DATE | SWT.BORDER);
			if (value != null) {
				SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
				try {
					Calendar c = Calendar.getInstance();
					c.setTime(df.parse(value));
					valueTextDateTime.setDate(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DATE));
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
			valueText = valueTextDateTime;
		} else {
			if (!valueText.isDisposed()) {
				valueText.dispose();
			}
			valueText = new Text(dynaComposite, SWT.BORDER);
			if (value != null) {
				((Text) valueText).setText(value);
			}
		}
		valueText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		dynaComposite.layout(true);
	}

	/**
	 * 设置增加按钮是否可用
	 * @param enabled
	 *            true-可用,false-不可用;
	 */
	public void setAddButtonEnabled(boolean enabled) {
		this.addButton.setEnabled(enabled);
	}

	/**
	 * 设置删除按钮是否可用
	 * @param enabled
	 *            true-可用,false-不可用;
	 */
	public void setDeleteButtonEnabled(boolean enabled) {
		this.deleteButton.setEnabled(enabled);
	}

	/**
	 * 获取当前过滤条件的值
	 * @return ;
	 */
	public ExportFilterComponentBean getValue() {
		if (valueText instanceof Text) {
			this.baseDataBean.setFilterVlaue(((Text) valueText).getText());
		}

		else if (valueText instanceof DateTime) {
			DateTime temp = (DateTime) valueText;
			StringBuffer bf = new StringBuffer();
			bf.append(temp.getYear());
			bf.append("-");
			DecimalFormat df = new DecimalFormat("00");
			bf.append(df.format(temp.getMonth() + 1));
			bf.append("-");
			bf.append(df.format(temp.getDay()));
			bf.append(" 00:00:00"); // 补全时间
			this.baseDataBean.setFilterVlaue(bf.toString());
		}

		return this.baseDataBean;
	}
}

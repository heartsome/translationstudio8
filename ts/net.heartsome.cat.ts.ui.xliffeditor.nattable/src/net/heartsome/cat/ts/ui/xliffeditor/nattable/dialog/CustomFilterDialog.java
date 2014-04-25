package net.heartsome.cat.ts.ui.xliffeditor.nattable.dialog;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import net.heartsome.cat.ts.core.file.IPreferenceConstants;
import net.heartsome.cat.ts.core.file.PreferenceStore;
import net.heartsome.cat.ts.core.file.XLFHandler;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.editor.XLIFFEditorImplWithNatTable;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.resource.Messages;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

/**
 * 定义自定义条件，生成动态的自定义条件的Xpath
 * @author Leakey
 * @version
 * @since JDK1.6
 */
public class CustomFilterDialog extends Dialog {
	/** 自定义过滤器列表. */
	private List customFilterList;
	/** 过滤器列表. */
	private Combo cmbFilter;
	/** 对话框内容区域. */
	private Composite tParent;

	/** 自定义控件滚动条容器. */
	private ScrolledComposite scroll;
	/** 自定义控件容器. */
	private Composite dynaComp;

	/** 过滤器名称文本框. */
	private Text filterNameTxt;
	/** 满足所有条件按钮. */
	private Button andBtn;
	/** 满足任一条件按钮. */
	private Button orBtn;

	/** 灰色. */
	private Color gray;
	/** 黑色. */
	private Color black;

	/** 属性名提醒. */
	private final String initPropName = Messages.getString("dialog.CustomFilterDialog.initPropName");
	/** 属性值提醒. */
	private final String initPropValue = Messages.getString("dialog.CustomFilterDialog.initPropValue");
	/** 一般文本框输入提醒. */
	private final String initValue = Messages.getString("dialog.CustomFilterDialog.initValue");
	/** 验证失败. */
	private static final String RESULT_FAILED = "FAILED";

	/** 首选项中保存的已有的自定义过滤条件. */
	private LinkedHashMap<String, String> customFilters = XLFHandler.getCustomFilterMap();

	/** 首选项中保存的已有的自定义过滤条件附加信息. */
	private LinkedHashMap<String, String> customFiltersAddition = XLFHandler.getCustomFilterAdditionMap();

	/** 首选项中保存的已有的自定义过滤条件（刷新界面使用）. */
	private LinkedHashMap<String, ArrayList<String[]>> customFiltersIndex = XLFHandler.getCustomFilterIndexMap();

	/**
	 * @param shell
	 * @param cmbFilter
	 */
	public CustomFilterDialog(Shell shell, Combo cmbFilter) {
		super(shell);
		this.cmbFilter = cmbFilter;
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(Messages.getString("dialog.CustomFilterDialog.title"));
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		gray = parent.getDisplay().getSystemColor(SWT.COLOR_GRAY);
		black = parent.getDisplay().getSystemColor(SWT.COLOR_BLACK);
		tParent = (Composite) super.createDialogArea(parent);
		GridLayoutFactory.swtDefaults().extendedMargins(5, 5, 10, 0).numColumns(2).equalWidth(false).applyTo(tParent);
		tParent.setLayoutData(new GridData(GridData.FILL_BOTH));

		Composite cmpLeft = new Composite(tParent, SWT.BORDER);
		cmpLeft.setLayout(new GridLayout(2, false));
		GridDataFactory.swtDefaults().applyTo(cmpLeft);
		Composite cmpList = new Composite(cmpLeft, SWT.NONE);
		cmpList.setLayout(new GridLayout(1, true));
		cmpList.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		new Label(cmpList, SWT.NONE).setText(Messages.getString("dialog.CustomFilterDialog.c1Lbl"));
		initCustomFilterList(cmpList);

		Composite cmpBtn = new Composite(cmpLeft, SWT.None);
		cmpBtn.setLayout(new GridLayout());
		cmpBtn.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		
		Button addCustom = new Button(cmpBtn, SWT.PUSH);
		addCustom.setText(Messages.getString("dialog.CustomFilterDialog.addCustom"));
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).applyTo(addCustom);
		addCustom.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (isChange()) {
					if (!MessageDialog.openConfirm(getShell(), "", Messages.getString("dialog.CustomFilterDialog.msg1"))) {
						return;
					}
				}
				refresh();
			}
		});
		Button delCustom = new Button(cmpBtn, SWT.PUSH);
		delCustom.setText(Messages.getString("dialog.CustomFilterDialog.delCustom"));
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.FILL).applyTo(delCustom);
		delCustom.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				String[] filters = customFilterList.getSelection();
				if (filters != null && filters.length > 0) {
					if (MessageDialog.openConfirm(getShell(), "", Messages.getString("dialog.CustomFilterDialog.msg2"))) {
						for (int i = 0; i < filters.length; i++) {
							customFilters.remove(filters[i]);
							customFiltersAddition.remove(filters[i]);
							customFiltersIndex.remove(filters[i]);
							IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
							if (window != null) {
								IWorkbenchPage page = window.getActivePage();
								if (page != null) {
									IEditorReference[] editors = page.getEditorReferences();
									for(IEditorReference ef : editors){
										IEditorPart editor  = ef.getEditor(false);
										if (editor != null && editor instanceof XLIFFEditorImplWithNatTable) {
											Combo cb = ((XLIFFEditorImplWithNatTable) editor).getFilterCombo();
											if(cb != null && !cb.isDisposed()){
												cb.remove(filters[i]);
											}
										}
									}
								}
							}
//							cmbFilter.remove(filters[i]);
							customFilterList.remove(filters[i]);
							XLFHandler.getFilterMap().remove(filters[i]);
						}
						PreferenceStore.saveMap(IPreferenceConstants.FILTER_CONDITION, customFilters);
						PreferenceStore.saveCustomCondition(IPreferenceConstants.FILTER_CONDITION_INDEX,
								customFiltersIndex);
					}
				} else {
					MessageDialog.openInformation(getShell(), "", Messages.getString("dialog.CustomFilterDialog.msg3"));
				}
			}

		});

		Button editCustom = new Button(cmpBtn, SWT.PUSH);
		editCustom.setText(Messages.getString("dialog.CustomFilterDialog.editCustom"));
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.FILL).applyTo(editCustom);
		editCustom.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				edit();
			}
		});

		Composite cmpRight = new Composite(tParent, SWT.NONE);
		cmpRight.setLayout(new GridLayout(1, true));
		cmpRight.setLayoutData(new GridData(GridData.FILL_BOTH));

		Composite top = new Composite(cmpRight, SWT.NONE);
		top.setLayout(new GridLayout(2, false));
		top.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		new Label(top, SWT.NONE).setText(Messages.getString("dialog.CustomFilterDialog.topLbl"));
		filterNameTxt = new Text(top, SWT.BORDER);
		filterNameTxt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		filterNameTxt.forceFocus();
		top = new Composite(cmpRight, SWT.NONE);
		top.setLayout(new GridLayout(2, false));
		top.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		andBtn = new Button(top, SWT.RADIO);
		andBtn.setText(Messages.getString("dialog.CustomFilterDialog.andBtn"));
		andBtn.setSelection(true);
		orBtn = new Button(top, SWT.RADIO);
		orBtn.setText(Messages.getString("dialog.CustomFilterDialog.orBtn"));
		scroll = new ScrolledComposite(cmpRight, SWT.V_SCROLL | SWT.BORDER);
		scroll.setAlwaysShowScrollBars(true);
		scroll.setLayoutData(new GridData(GridData.FILL_BOTH));
		scroll.setExpandHorizontal(true);
		scroll.setExpandVertical(true);
//		scroll.setSize(500, 200);
		dynaComp = new Composite(scroll, SWT.None);
		scroll.setContent(dynaComp);
		dynaComp.setLayout(new GridLayout(1, true));
		new DynaComposite(dynaComp, SWT.NONE);
		return parent;
	}

	/**
	 * 编辑
	 */
	private void edit() {
		String[] filters = customFilterList.getSelection();
		if (filters.length > 0) {
			if (isChange()) {
				if (!MessageDialog.openConfirm(getShell(), "", Messages.getString("dialog.CustomFilterDialog.msg1"))) {
					return;
				}
			}
			String key = filters[0];
			filterNameTxt.setText(key);
			String link = XLFHandler.getCustomFilterAdditionMap().get(key);
			if (link.indexOf("and") != -1) {
				andBtn.setSelection(true);
				orBtn.setSelection(false);
			} else {
				andBtn.setSelection(false);
				orBtn.setSelection(true);
			}

			for (Control ctl : conditionList) {
				if (!ctl.isDisposed()) {
					ctl.dispose();
				}
			}
			conditionList.clear();
			ArrayList<String[]> tmpList = XLFHandler.getCustomFilterIndexMap().get(key);
			for (String[] tempIndex : tmpList) {
				String filterIndex = tempIndex[0];
				DynaComposite dyna = new DynaComposite(dynaComp, SWT.NONE);
				dyna.getFilterName().select(Integer.parseInt(filterIndex));
				dyna.getFilterName().notifyListeners(SWT.Selection, null);
				if ("0".equals(filterIndex) || "1".equals(filterIndex)) { // 关键字、批注
					dyna.getConditions().select(Integer.parseInt(tempIndex[1]));
					dyna.getValue().setText(tempIndex[2].replace("0x0020", " "));
					dyna.getValue().setForeground(black);
				} else if ("2".equals(filterIndex)) { // 属性
					dyna.getPropName().setText(tempIndex[1]);
					dyna.getPropValue().setText(tempIndex[2]);
					dyna.getPropName().setForeground(black);
					dyna.getPropValue().setForeground(black);
				}
			}
			scroll.setMinSize(dynaComp.computeSize(SWT.DEFAULT, SWT.DEFAULT));
			dynaComp.layout();
		} else {
			MessageDialog.openInformation(getShell(), "", Messages.getString("dialog.CustomFilterDialog.msg4"));
		}
	}

	/**
	 * 查看当前状态是否为新增
	 * @return 当前状态是否为新增
	 */
	private boolean isAdd() {
		return !XLFHandler.getCustomFilterMap().containsKey(filterNameTxt.getText());
	}

	/**
	 * 刷新过滤器设置组件
	 */
	private void refresh() {
		filterNameTxt.setText("");
		andBtn.setSelection(true);
		orBtn.setSelection(false);
		for (Control ctl : conditionList) {
			if (!ctl.isDisposed()) {
				ctl.dispose();
			}
		}
		conditionList.clear();
		new DynaComposite(dynaComp, SWT.NONE);
		scroll.setMinSize(dynaComp.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		dynaComp.layout();

	}

	/**
	 * 初始化自定义过滤器列表
	 * @param comp
	 *            父容器
	 */
	private void initCustomFilterList(Composite comp) {
		customFilterList = new List(comp, SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER);
		GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gridData.widthHint = 110;
		gridData.heightHint = 250;
		customFilterList.setLayoutData(gridData);
		setListData(customFilterList, customFilters);
		customFilterList.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDoubleClick(MouseEvent e) {
				edit();
			}

		});
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		super.createButtonsForButtonBar(parent);
		Button okBtn = getButton(IDialogConstants.OK_ID);
		okBtn.setText(Messages.getString("dialog.CustomFilterDialog.okBtn"));
		Button cancelBtn = getButton(IDialogConstants.CANCEL_ID);
		cancelBtn.setText(Messages.getString("dialog.CustomFilterDialog.cancelBtn"));
	}

	@Override
	protected void cancelPressed() {
		close();
	}

	@Override
	public boolean close() {
		if (isChange()) {
			if (MessageDialog.openConfirm(getShell(), "", Messages.getString("dialog.CustomFilterDialog.msg5"))) {
				return super.close();
			}
		} else {
			return super.close();
		}
		return false;
	}

	/**
	 * 查看在新增和编辑状态下内容是否有变化
	 * @return 内容是否有变化
	 */
	private boolean isChange() {
		boolean result = false;
		if (isAdd()) {
			if (!filterNameTxt.getText().trim().equals("")) {
				result = true;
			}
			if (andBtn.getSelection()) {
				if (conditionList.size() > 1) {
					result = true;
				} else {
					DynaComposite comp = conditionList.get(0);
					if (comp.getFilterName().getSelectionIndex() != 0) {
						result = true;
					} else {
						if (comp.getConditions().getSelectionIndex() != 0) {
							result = true;
						} else {
							if (!comp.getValue().getText().equals(initValue)) {
								result = true;
							}
						}
					}
				}
			} else {
				result = true;
			}
		} else {
			String orignXpath = XLFHandler.getCustomFilterMap().get(filterNameTxt.getText());
			StringBuilder xpath = new StringBuilder();
			String link = andBtn.getSelection() ? " and " : " or ";
			for (DynaComposite comp : conditionList) { // 得到所有自定义条件组合的xpath
				String tempXpath = comp.getXpath(false);
				if (RESULT_FAILED.equals(tempXpath)) {
					result = true;
				}
				xpath.append(tempXpath).append(link);
			}
			if (xpath.length() > 0) {
				xpath.delete(xpath.length() - link.length(), xpath.length());
				result = !orignXpath.equals(xpath.toString());
			}

		}
		return result;
	}

	@Override
	protected void okPressed() {
		String filterNameStr = filterNameTxt.getText();
		if (filterNameStr == null || "".equals(filterNameStr)) {
			MessageDialog.openInformation(getShell(), "", Messages.getString("dialog.CustomFilterDialog.msg6"));
			return;
		}
		StringBuilder xpath = new StringBuilder();
		String link = andBtn.getSelection() ? " and " : " or ";
		ArrayList<String[]> tempValue = new ArrayList<String[]>();
		for (DynaComposite comp : conditionList) { // 得到所有自定义条件组合的xpath
			String tempXpath = comp.getXpath(true);
			if (RESULT_FAILED.equals(tempXpath)) {
				return;
			}
			xpath.append(tempXpath).append(link);
			tempValue.add(comp.getTempIndex());
		}
		if (xpath.length() > 0) {
			if (isAdd()) {
				IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
				if (window != null) {
					IWorkbenchPage page = window.getActivePage();
					if (page != null) {
						IEditorReference[] editors = page.getEditorReferences();
						for(IEditorReference e : editors){
							IEditorPart editor  = e.getEditor(false);
							if (editor != null && editor instanceof XLIFFEditorImplWithNatTable) {
								Combo cb = ((XLIFFEditorImplWithNatTable) editor).getFilterCombo();
								if(cb != null && !cb.isDisposed()){
									cb.add(filterNameTxt.getText());
								}
							}
						}
					}
				}
//				cmbFilter.add(filterNameTxt.getText());
			} else {
				XLFHandler.getFilterMap().put(filterNameTxt.getText(), xpath.substring(0, xpath.lastIndexOf(link)));
			}
			customFilters.put(filterNameStr, xpath.substring(0, xpath.lastIndexOf(link)));
			customFiltersAddition.put(filterNameStr, link.trim());
			customFiltersIndex.put(filterNameStr, tempValue);
			PreferenceStore.saveMap(IPreferenceConstants.FILTER_CONDITION, new TreeMap<String, String>(customFilters));
			PreferenceStore.saveMap(IPreferenceConstants.FILTER_CONDITION_ADDITION, customFiltersAddition);
			PreferenceStore.saveCustomCondition(IPreferenceConstants.FILTER_CONDITION_INDEX, customFiltersIndex);
			reload();
		}
	}

	/**
	 * 重新加载自定义过滤器列表数据和刷新右侧过滤器设置组件
	 */
	private void reload() {
		setListData(customFilterList, XLFHandler.getCustomFilterMap());
		refresh();
	}

	/** 自定义条件组件列表. */
	private ArrayList<DynaComposite> conditionList = new ArrayList<DynaComposite>();

	/** 过滤器条件数据. */
	static LinkedHashMap<Integer, String> filterNameData = new LinkedHashMap<Integer, String>();
	/** 条件数据. */
	static LinkedHashMap<Integer, String> conditionsData = new LinkedHashMap<Integer, String>();
	/** 只包含相等与不相等的条件数据. */
	static LinkedHashMap<Integer, String> equalsConditionsData = new LinkedHashMap<Integer, String>();
	static {
		String[] tmpValue = new String[] { Messages.getString("dialog.CustomFilterDialog.tmpValue1"),
				Messages.getString("dialog.CustomFilterDialog.tmpValue2"),
				Messages.getString("dialog.CustomFilterDialog.tmpValue3") };
		for (int i = 0; i < tmpValue.length; i++) {
			filterNameData.put(i, tmpValue[i]);
		}
		conditionsData.put(0, Messages.getString("dialog.CustomFilterDialog.contain"));
		conditionsData.put(1, Messages.getString("dialog.CustomFilterDialog.uncontain"));
		conditionsData.put(2, Messages.getString("dialog.CustomFilterDialog.eq"));
		conditionsData.put(3, Messages.getString("dialog.CustomFilterDialog.neq"));
		equalsConditionsData.put(0, Messages.getString("dialog.CustomFilterDialog.eq"));
		equalsConditionsData.put(1, Messages.getString("dialog.CustomFilterDialog.neq"));
		// String[] states = { "new", "final", "translated", "signed-off", "needs-adaptation",
		// "needs-review-adaptation",
		// "needs-l10n", "needs-review-l10n", "needs-translation", "needs-review-translation" };
		// for (int i = 0; i < states.length; i++) {
		// stateData.put(i, states[i]);
		// }

	}

	/**
	 * 动态自定义条件组件
	 * @author Leakey
	 * @version
	 * @since JDK1.6
	 */
	class DynaComposite extends Composite {

		/** 过滤器列表控件. */
		private Combo filterName;
		/** 判断条件列表控件. */
		private Combo conditions;
		/** 一般文本框. */
		private Text value;
		/** +按钮. */
		private Button addBtn;
		/** -按钮. */
		private Button reduceBtn;

		/** 属性名与值文本框容器. */
		private Composite propComp;
		/** 属性名文本框. */
		private Text propName;
		/** 属性值文本框. */
		private Text propValue;

		/** 当前选中的过滤器是否是（关键字、批注等）. */
		private boolean isOther = true;

		/** 当前选中的过滤器是否是属性. */
		private boolean isProp = false;

		private String[] tempIndex = null;

		/**
		 * 获取当前设置情况（用来保存在首选项中刷新时候用）
		 */
		public String[] getTempIndex() {
			return tempIndex;
		}

		/**
		 * 获取子过滤器名称列表组件
		 */
		public Combo getFilterName() {
			return filterName;
		}

		/**
		 * 获取条件组合
		 */
		public Combo getConditions() {
			return conditions;
		}

		/**
		 * 获取属性名组件
		 */
		public Text getPropName() {
			return propName;
		}

		/**
		 * 获取属性值组件
		 */
		public Text getPropValue() {
			return propValue;
		}

		/**
		 * 获取一般值组件
		 */
		public Text getValue() {
			return value;
		}

		/**
		 * @param parent
		 * @param style
		 */
		public DynaComposite(Composite parent, int style) {
			super(parent, style);
			init();
			conditionList.add(this);
		}

		/**
		 * 销毁除了“过滤器列表控件”之外的其它控件
		 */
		private void disposeChild() {
			for (Control ctl : this.getChildren()) {
				if (!"filterName".equals(ctl.getData()) && ctl != null && !ctl.isDisposed()) {
					ctl.dispose();
				}
			}
		}

		/**
		 * 初始化自定义条件组件
		 */
		private void init() {
			this.setLayout(new GridLayout(5, false));
			this.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			initFilterName();
			initConditions(true);
			initTxt();
			initBtn();
		}

		/**
		 * 初始化“+”，“－”按钮
		 */
		private void initBtn() {
			addBtn = new Button(this, SWT.None);
			addBtn.setText("+");
			addBtn.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					new DynaComposite(dynaComp, SWT.NONE);
					scroll.setMinSize(dynaComp.computeSize(SWT.DEFAULT, SWT.DEFAULT));
					dynaComp.layout();
				}
			});
			reduceBtn = new Button(this, SWT.None);
			reduceBtn.setText("-");
			reduceBtn.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					conditionList.remove(DynaComposite.this);
					DynaComposite.this.dispose();
					scroll.setMinSize(dynaComp.computeSize(SWT.DEFAULT, SWT.DEFAULT));
					dynaComp.layout();
				}
			});
			if (conditionList.size() > 0) {
				if (conditionList.get(0).equals(this)) {
					reduceBtn.setEnabled(false);
				} else {
					reduceBtn.setEnabled(true);
				}
			} else {
				reduceBtn.setEnabled(false);
			}
		}

		/**
		 * 初始化一般文本框
		 */
		private void initTxt() {
			value = new Text(this, SWT.BORDER);
			if(filterName.getSelectionIndex() == 1){
				setGray(value, initValue);
				value.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
				return;
			}
			value.setForeground(gray);
			value.setText(initValue);
			value.addFocusListener(new FocusListener() {
				public void focusLost(FocusEvent arg0) {
					if (value.getText().equals("")) {
						if (!value.getForeground().equals(gray)) {
							value.setForeground(gray);
						}
						value.setText(initValue);
					} else {
						if (!value.getForeground().equals(black)) {
							value.setForeground(black);
						}
					}
				}

				public void focusGained(FocusEvent arg0) {
					if (value.getText().equals(initValue)) {
						value.setText("");
						value.setForeground(black);
					} else {
						if (!value.getForeground().equals(black)) {
							value.setForeground(black);
						}
					}
				}
			});
			value.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		}

		/**
		 * 初始化属性名和属性值两个文本框
		 * @param comp
		 *            文本框父容器
		 */
		private void initPropTxt(Composite comp) {
			propName = new Text(comp, SWT.BORDER);
			GridData data = new GridData(115, SWT.DEFAULT);
			propName.setLayoutData(data);
			setGray(propName, initPropName);
			propValue = new Text(comp, SWT.BORDER);
			setGray(propValue, initPropValue);
			propValue.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		}

		/**
		 * 初始化判断条件列表控件
		 * @param isContain
		 *            是否具有包含与不包含条件
		 */
		private void initConditions(boolean isContain) {
			conditions = new Combo(this, SWT.READ_ONLY);
			conditions.setLayoutData(new GridData(100, 35));

			if (isContain) {
				setComboData(conditions, conditionsData);
			} else {
				setComboData(conditions, equalsConditionsData);
			}
			conditions.select(0);

		}

		/**
		 * 初始化过滤器列表控件
		 */
		private void initFilterName() {
			filterName = new Combo(this, SWT.READ_ONLY);
			filterName.setLayoutData(new GridData(80, 35));
			setComboData(filterName, filterNameData);
			filterName.setData("filterName");
			filterName.select(0);
			filterName.addSelectionListener(new SelectionAdapter() {
				private boolean isState() {
					return filterName.getSelectionIndex() == 1;
				}

				private boolean isProp() {
					return filterName.getSelectionIndex() == 2;
				}

				private boolean isOther() {
					return !isState() && !isProp();
				}

				private void createProp() {
					disposeChild();
					initTempComp();
					initPropTxt(propComp);
					initBtn();
				}

				private void createOther() {
					disposeChild();
					initConditions(true);
					initTxt();
					initBtn();
				}

				private void initTempComp() {
					propComp = new Composite(DynaComposite.this, SWT.READ_ONLY);
					GridData gd = new GridData(GridData.FILL_HORIZONTAL);
					gd.horizontalSpan = 2;
					propComp.setLayoutData(gd);
					GridLayoutFactory.fillDefaults().numColumns(2).applyTo(propComp);
				}

				@Override
				public void widgetSelected(SelectionEvent e) {
					if (!isProp && isProp()) {
						createProp();
						isProp = true;
						isOther = false;
					} else/* if (!isOther && isOther())*/ {
						createOther();
						isOther = true;
						isProp = false;
					} 
					DynaComposite.this.layout();
				}
			});
		}

		/**
		 * 取得xpath查询语句
		 * @return xpath查询语句
		 */
		public String getXpath(boolean withNotice) {
			StringBuilder re = new StringBuilder();
			int contain = 0;
			int notContain = 1;
			int equal = 2;
			int notEqual = 3;
			int condition = -1;
			if (conditions != null && !conditions.isDisposed()) {
				String temp = conditions.getItem(conditions.getSelectionIndex());
				// if (temp.indexOf("不") != -1) {
				// if (temp.indexOf("包含") != -1) {
				// condition = notContain;
				// } else {
				// condition = notEqual;
				// }
				// } else {
				// if (temp.indexOf("包含") != -1) {
				// condition = contain;
				// } else {
				// condition = equal;
				// }
				// }
				// modify by peason --->2012-07-07
				if (temp.equals(Messages.getString("dialog.CustomFilterDialog.uncontain"))) {
					condition = notContain;
				} else if (temp.equals(Messages.getString("dialog.CustomFilterDialog.neq"))) {
					condition = notEqual;
				} else if (temp.equals(Messages.getString("dialog.CustomFilterDialog.contain"))) {
					condition = contain;
				} else if (temp.equals(Messages.getString("dialog.CustomFilterDialog.eq"))) {
					condition = equal;
				}
			}

			if (isOther) {
				String txt = value.getText().trim();
				if (filterName.getSelectionIndex() == 0) { // 关键字
					txt = value.getText();
					if (txt.equals("") || txt.equals(initValue)) {
						if (withNotice) {
							MessageDialog.openInformation(getShell(), "",
									Messages.getString("dialog.CustomFilterDialog.msg7"));
						}
						return RESULT_FAILED;
					}
					if (condition == contain) {
						re.append("contains(source/text(), '").append(txt).append("')");
					} else if (condition == notContain) {
						re.append("not(contains(source/text(), '").append(txt).append("'))");
					} else if (condition == equal) {
						re.append("source/text()='").append(txt).append("'");
					} else if (condition == notEqual) {
						re.append("source/text()!='").append(txt).append("'");
					}
					txt = txt.replace(" ", "0x0020");
				} else if (filterName.getSelectionIndex() == 1) { // 批注
					if (txt.equals("") || txt.equals(initValue)) {
						if (withNotice) {
							MessageDialog.openInformation(getShell(), "",
									Messages.getString("dialog.CustomFilterDialog.msg8"));
						}
						return RESULT_FAILED;
					}

					if (condition == contain) {
						re.append("contains(note/text(), '").append(txt).append("')");
					} else if (condition == notContain) {
						re.append("not(contains(note/text(), '").append(txt).append("'))");
					} else if (condition == equal) {
						re.append("note/text()='").append(txt).append("'");
					} else if (condition == notEqual) {
						re.append("not(note/text()='").append(txt).append("'))");
					}
				}
				tempIndex = new String[] { filterName.getSelectionIndex() + "", conditions.getSelectionIndex() + "",
						txt };
			} else if (isProp) { // 属性
				String propNameStr = propName.getText().trim();
				String propValueStr = propValue.getText().trim();
				if (propNameStr.equals("") || propNameStr.equals(initPropName)) {
					if (withNotice) {
						MessageDialog.openInformation(getShell(), "",
								Messages.getString("dialog.CustomFilterDialog.initPropName"));
					}
					return RESULT_FAILED;
				}
				if (propValueStr.equals("") || propValueStr.equals(initPropValue)) {
					if (withNotice) {
						MessageDialog.openInformation(getShell(), "",
								Messages.getString("dialog.CustomFilterDialog.initPropValue"));
					}
					return RESULT_FAILED;
				}
				re.append("hs:prop-group/hs:prop[@prop-type='").append(propNameStr).append("']='").append(propValueStr)
						.append("'");
				tempIndex = new String[] { filterName.getSelectionIndex() + "", propNameStr, propValueStr };
			}
			return re.toString();
		}
	}

	/**
	 * 设置Combo下拉列表中的数据
	 * @param combo
	 *            下拉列表
	 * @param data
	 *            数据、key=索引,value=显示的文字
	 */
	private void setComboData(Combo combo, Map<Integer, String> data) {
		for (Entry<Integer, String> entry : data.entrySet()) {
			combo.add(entry.getValue(), entry.getKey());
		}
	}

	/**
	 * 为List组件设置数据
	 * @param list
	 * @param data
	 */
	private void setListData(List list, Map<String, String> data) {
		list.removeAll();
		if (data == null || data.size() == 0) {
			return;
		}
		for (Entry<String, String> entry : data.entrySet()) {
			list.add(entry.getKey());
		}
	}

	/**
	 * 为Text控件增加灰色提醒，获得焦点时自动清除灰色提醒
	 * @param text
	 *            Text控件
	 * @param initTxt
	 *            灰色提醒字符串
	 */
	private void setGray(final Text text, final String initTxt) {
		text.setForeground(gray);
		text.setText(initTxt);
		text.addFocusListener(new FocusListener() {
			public void focusLost(FocusEvent arg0) {
				if (text.getText().trim().equals("")) {
					if (!text.getForeground().equals(gray)) {
						text.setForeground(gray);
					}
					text.setText(initTxt);
				} else {
					if (!text.getForeground().equals(black)) {
						text.setForeground(black);
					}
				}
			}

			public void focusGained(FocusEvent arg0) {
				if (text.getText().trim().equals(initTxt)) {
					text.setText("");
					text.setForeground(black);
				} else {
					if (!text.getForeground().equals(black)) {
						text.setForeground(black);
					}
				}
			}
		});
	}

}

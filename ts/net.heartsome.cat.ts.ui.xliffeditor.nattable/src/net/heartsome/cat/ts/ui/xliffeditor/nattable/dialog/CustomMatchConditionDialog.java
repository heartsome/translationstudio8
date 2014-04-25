package net.heartsome.cat.ts.ui.xliffeditor.nattable.dialog;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import net.heartsome.cat.ts.core.bean.Constants;
import net.heartsome.cat.ts.core.file.IPreferenceConstants;
import net.heartsome.cat.ts.core.file.PreferenceStore;
import net.heartsome.cat.ts.core.file.XLFHandler;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * 定制匹配条件对话框(此类未使用，因此未做国际化)
 * @author Leakey
 * @version
 * @since JDK1.5
 */
public class CustomMatchConditionDialog extends Dialog {

	/** 自定义过滤器列表. */
	private List customFilterList;
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

	/** 首选项中保存的已有的自定义过滤条件. */
	private LinkedHashMap<String, String> customFilters = XLFHandler.getCustomMatchFilterMap();

	/** 首选项中保存的已有的自定义过滤条件（附加信息）. */
	LinkedHashMap<String, ArrayList<String[]>> customFiltersAddtion = XLFHandler.getCustomMatchFilterAdditionMap();

	/** 首选项中保存的已有的自定义过滤条件（刷新界面使用）. */
	private LinkedHashMap<String, ArrayList<String[]>> customFiltersIndex = XLFHandler.getCustomMatchFilterIndexMap();
	/** 标记的匹配按钮. */
	private Button btnIsTagged;
	/** 快速翻译匹配按钮. */
	private Button btnQT;
	/** 自动繁殖翻译匹配按钮. */
	private Button btnPT;
	/** 从来源中删除按钮. */
	private Button btnIsRemoveFromSrc;
	/** 选中“标记的匹配按钮”的值. */
	private int btnIsTaggedCheck = 1 << 1;
	/** 选中“快速翻译匹配按钮”的值. */
	private int btnQTCheck = 1 << 2;
	/** 选中“自动繁殖翻译匹配按钮”的值. */
	private int btnPTCheck = 1 << 3;
	/** 选中“从来源中删除按钮”的值. */
	private int btnIsRemoveFromSrcCheck = 1 << 4;

	/**
	 * 得到标记的匹配、快速翻译匹配、自动繁殖翻译匹配、从来源中删除四个按钮的选择情况
	 * @return 选择组合的结果
	 */
	private int getCheck() {
		int result = 1 << 5;
		if (btnIsTagged != null && btnIsTagged.getSelection()) {
			result = result | btnIsTaggedCheck;
		}
		if (btnQT != null && btnQT.getSelection()) {
			result = result | btnQTCheck;
		}
		if (btnPT != null && btnPT.getSelection()) {
			result = result | btnPTCheck;
		}
		if (btnIsRemoveFromSrc != null && btnIsRemoveFromSrc.getSelection()) {
			result = result | btnIsRemoveFromSrcCheck;
		}
		return result;
	}

	/** 灰色. */
	private Color gray;
	/** 黑色. */
	private Color black;

	/** 自定义条件组件列表. */
	private ArrayList<DynaComposite> conditionList = new ArrayList<DynaComposite>();

	/** 一般文本框输入提醒. */
	private final String initValue = "请输入值";

	/** 验证失败. */
	private static final String RESULT_FAILED = "FAILED";

	/**
	 * @param shell
	 */
	public CustomMatchConditionDialog(Shell shell) {
		super(shell);
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("自定义删除匹配条件");
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		super.createButtonsForButtonBar(parent);
		Button okBtn = getButton(IDialogConstants.OK_ID);
		okBtn.setText("保存");
		Button cancelBtn = getButton(IDialogConstants.CANCEL_ID);
		cancelBtn.setText("关闭");
	}

	/**
	 * 删除保存在首选项中的所有与指定条件相关的数据
	 * @param key 条件名
	 */
	private void deletePreference(String[] key) {
		if (key == null || key.length == 0) {
			return;
		}
		for (int i = 0; i < key.length; i++) {
			customFilters.remove(key[i]);
			customFiltersAddtion.remove(key[i]);
			customFiltersIndex.remove(key[i]);
			customFilterList.remove(key[i]);
		}
		PreferenceStore.saveMap(IPreferenceConstants.MATCH_CONDITION, customFilters);
		PreferenceStore.saveCustomCondition(IPreferenceConstants.MATCH_CONDITION_ADDITION, customFiltersAddtion);
		PreferenceStore.saveCustomCondition(IPreferenceConstants.MATCH_CONDITION_INDEX, customFiltersIndex);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		gray = parent.getDisplay().getSystemColor(SWT.COLOR_GRAY);
		black = parent.getDisplay().getSystemColor(SWT.COLOR_BLACK);
		tParent = (Composite) super.createDialogArea(parent);
		tParent.setLayout(new GridLayout(2, false));
		tParent.setLayoutData(new GridData(720, 410));

		Composite c = new Composite(tParent, SWT.BORDER);
		c.setLayout(new GridLayout(2, false));
		c.setLayoutData(new GridData(200, 388));
		Composite c1 = new Composite(c, SWT.NONE);
		c1.setLayout(new GridLayout(1, true));
		c1.setLayoutData(new GridData(150, 380));
		new Label(c1, SWT.NONE).setText("自定义过滤器：");
		initCustomFilterList(c1);

		Composite c2 = new Composite(c, SWT.NONE);
		RowLayout rowLayout = new RowLayout(SWT.VERTICAL);
		rowLayout.marginLeft = 0;
		rowLayout.marginTop = 150;
		rowLayout.spacing = 5;
		c2.setLayout(rowLayout);
		c2.setLayoutData(new GridData(50, 380));
		Button addCustom = new Button(c2, SWT.PUSH);
		addCustom.setText("新增");
		addCustom.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// 新增自定义过滤器方法
				if (isChange()) {
					if (!MessageDialog.openConfirm(getShell(), "", "不保存当前内容吗？")) {
						return;
					}
				}
				refresh();
			}
		});
		Button delCustom = new Button(c2, SWT.PUSH);
		delCustom.setText("删除");
		delCustom.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// 删除已保存的自定义过滤器方法
				String[] filters = customFilterList.getSelection();
				if (filters != null && filters.length > 0) {
					if (MessageDialog.openConfirm(getShell(), "", "确定要删除选中的自定义过滤器吗？")) {
						deletePreference(filters);
					}
				} else {
					MessageDialog.openInformation(getShell(), "", "请选择要删除的自定义过滤器。");
				}
			}

		});

		Button editCustom = new Button(c2, SWT.PUSH);
		editCustom.setText("编辑");
		editCustom.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// 编辑已有的自定义过滤器方法
				edit();
			}
		});

		Composite c3 = new Composite(tParent, SWT.NONE);
		c3.setLayout(new GridLayout(1, true));
		c3.setLayoutData(new GridData(500, 400));

		Composite top = new Composite(c3, SWT.NONE);
		top.setLayout(new GridLayout(2, false));
		new Label(top, SWT.NONE).setText("过滤器名称：");
		filterNameTxt = new Text(top, SWT.BORDER);
		filterNameTxt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		andBtn = new Button(top, SWT.RADIO);
		andBtn.setText("满足所有条件");
		andBtn.setSelection(true);
		orBtn = new Button(top, SWT.RADIO);
		orBtn.setText("满足以下任一条件");
		scroll = new ScrolledComposite(c3, SWT.V_SCROLL | SWT.BORDER);
		scroll.setAlwaysShowScrollBars(true);
		scroll.setLayoutData(new GridData(470, 250));
		scroll.setExpandHorizontal(true);
		scroll.setExpandVertical(true);
		dynaComp = new Composite(scroll, SWT.BORDER);
		scroll.setContent(dynaComp);
		dynaComp.setLayout(new GridLayout(1, true));
		new DynaComposite(dynaComp, SWT.NONE);

		Composite c4 = new Composite(c3, SWT.BORDER);
		c4.setLayout(new GridLayout(2, true));
		c4.setLayoutData(new GridData(470,60));

		btnIsTagged = new Button(c4, SWT.CHECK);
		btnIsTagged.setText("标记的匹配");
		btnQT = new Button(c4, SWT.CHECK);
		btnQT.setText("快速翻译匹配");
		btnPT = new Button(c4, SWT.CHECK); // propagate translation
		btnPT.setText("自动繁殖翻译匹配");
		if (isTEInstalled()) {
			btnIsRemoveFromSrc = new Button(c4, SWT.CHECK);
			btnIsRemoveFromSrc.setText("从来源中删除");
		}

		return parent;
	}

	/**
	 * 编辑
	 */
	private void edit() {
		String[] filters = customFilterList.getSelection();
		if (filters.length > 0) {
			if (isChange()) {
				if (!MessageDialog.openConfirm(getShell(), "", "不保存当前内容吗？")) {
					return;
				}
			}
			String key = filters[0];
			filterNameTxt.setText(key);

			String[] tempArray = XLFHandler.getCustomMatchFilterAdditionMap().get(key).get(0);
			if (tempArray[0].indexOf("and") != -1) {
				andBtn.setSelection(true);
				orBtn.setSelection(false);
			} else {
				andBtn.setSelection(false);
				orBtn.setSelection(true);
			}
			int check = Integer.parseInt(tempArray[1]);

			if (btnIsTagged != null) {
				if ((check & btnIsTaggedCheck) != 0) {
					btnIsTagged.setSelection(true);
				} else {
					btnIsTagged.setSelection(false);
				}
			}

			if (btnQT != null) {
				if ((check & btnQTCheck) != 0) {
					btnQT.setSelection(true);
				} else {
					btnQT.setSelection(false);
				}
			}
			if (btnPT != null) {
				if ((check & btnPTCheck) != 0) {
					btnPT.setSelection(true);
				} else {
					btnPT.setSelection(false);
				}
			}
			if (btnIsRemoveFromSrc != null) {
				if ((check & btnIsRemoveFromSrcCheck) != 0) {
					btnIsRemoveFromSrc.setSelection(true);
				} else {
					btnIsRemoveFromSrc.setSelection(false);
				}
			}

			for (Control ctl : conditionList) {
				if (!ctl.isDisposed()) {
					ctl.dispose();
				}
			}
			conditionList.clear();
			ArrayList<String[]> tmpList = XLFHandler.getCustomMatchFilterIndexMap().get(key);
			for (String[] tempIndex : tmpList) {
				String filterIndex = tempIndex[0];
				DynaComposite dyna = new DynaComposite(dynaComp, SWT.NONE);
				dyna.getFilterName().select(Integer.parseInt(filterIndex));
				dyna.getFilterName().notifyListeners(SWT.Selection, null);
				if ("0".equals(filterIndex)) { // 匹配率
					dyna.getConditions().select(Integer.parseInt(tempIndex[1]));
					dyna.getValue().setText(tempIndex[2]);
				} else if ("1".equals(filterIndex)) { // 关键字
					dyna.getConditions().select(Integer.parseInt(tempIndex[1]));
					dyna.getValue().setText(tempIndex[2]);
				} else if ("2".equals(filterIndex)) { // 来源
					dyna.getValue().setText(tempIndex[1]);
				}
				dyna.getValue().setForeground(black);
			}
			scroll.setMinSize(dynaComp.computeSize(SWT.DEFAULT, SWT.DEFAULT));
			dynaComp.layout();
		} else {
			MessageDialog.openInformation(getShell(), "", "请选择一个自定义过滤器。");
		}
	}

	/**
	 * 初始化已保存的条件列表
	 * @param comp 父容器
	 */
	private void initCustomFilterList(Composite comp) {
		customFilterList = new List(comp, SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER);
		customFilterList.setLayoutData(new GridData(GridData.FILL_BOTH));
		setListData(customFilterList, customFilters);
		customFilterList.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDoubleClick(MouseEvent e) {
				// 调编辑的方法
				edit();
			}

		});
	}

	/**
	 * 是否是UE版本
	 * @return ;
	 */
	private boolean isUEVersion() {
		// TODO 实现是否是UE版本的判断
		return false;
	}

	/**
	 * 是否安装TE
	 * @return ;
	 */
	private boolean isTEInstalled() {
		// TODO 实现是否安装TE的判断
		return true;
	}

	/**
	 * (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
	 */
	@Override
	protected void okPressed() {
		// 保存自定义条件
		String filterNameStr = filterNameTxt.getText();
		if (filterNameStr == null || "".equals(filterNameStr)) {
			MessageDialog.openInformation(getShell(), "", "请输入过滤器名称！");
			return;
		}
		if (btnIsRemoveFromSrc != null) { // 未安装TE时，该功能被屏蔽，按钮未被初始化。
			if (btnIsRemoveFromSrc.getSelection()) {
				// TODO 将符合条件的匹配从可连接的记忆库中同时删除。该功能在与匹配来源的记忆库无法连接时禁用。
			}
		}
		StringBuilder xpath = new StringBuilder();
		String link = andBtn.getSelection() ? " and " : " or ";
		ArrayList<String[]> tempValue = new ArrayList<String[]>();
		for (DynaComposite comp : conditionList) { // 得到所有自定义条件组合的xpath
			String tempXpath = comp.getXpath(true);
			if (RESULT_FAILED.equals(tempXpath)) {
				return;
			}
			xpath.append(link).append(tempXpath);
			tempValue.add(comp.getTempIndex());
		}

		appendCheckCondition(xpath);

		if (xpath.length() > 0) {
			xpath.delete(0, xpath.indexOf(link) + link.length());
			customFilters.put(filterNameStr, xpath.toString());
			String[] additions = { link.trim(), "" + getCheck() };
			customFiltersIndex.put(filterNameStr, tempValue);
			ArrayList<String[]> tempValue1 = new ArrayList<String[]>();
			tempValue1.add(additions);
			customFiltersAddtion.put(filterNameStr, tempValue1);
			PreferenceStore.saveMap(IPreferenceConstants.MATCH_CONDITION, new TreeMap<String, String>(customFilters));
			PreferenceStore.saveCustomCondition(IPreferenceConstants.MATCH_CONDITION_INDEX, customFiltersIndex);
			PreferenceStore.saveCustomCondition(IPreferenceConstants.MATCH_CONDITION_ADDITION, customFiltersAddtion);
			reload();
		}
	}

	/**
	 * 追加标记的匹配、快速翻译匹配、自动繁殖翻译匹配、从来源中删除四个按钮组合的xpath
	 * @param xpath 需要追加的xpath
	 */
	private void appendCheckCondition(StringBuilder xpath) {
		if (btnIsTagged.getSelection()) { // 是否是带标记的匹配
			if (xpath.length() > 0) {
				xpath.append(" and ");
			}
			xpath.append("(source/* or target/*)");
		}
		if (btnQT.getSelection()) { // 是否是快速翻译匹配
			if (xpath.length() > 0) {
				xpath.append(" and ");
			}
			xpath.append("@tool-id='").append(Constants.QT_TOOLID).append("'");
		}
		if (btnPT.getSelection()) { // 是否是繁殖翻译匹配
			if (xpath.length() > 0) {
				xpath.append(" and ");
			}
			xpath.append("contains(@origin, 'autoFuzzy_')");
		}
	}

	/**
	 * 重新加载自定义过滤器列表数据和刷新右侧过滤器设置组件
	 */
	private void reload() {
		setListData(customFilterList, XLFHandler.getCustomMatchFilterMap());
		refresh();
	}

	/**
	 * 刷新过滤器设置组件
	 */
	private void refresh() {
		filterNameTxt.setText("");
		andBtn.setSelection(true);
		orBtn.setSelection(false);
		btnIsTagged.setSelection(false);
		btnQT.setSelection(false);
		btnPT.setSelection(false);
		if (btnIsRemoveFromSrc != null) {
			btnIsRemoveFromSrc.setSelection(false);
		}
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
	 * 查看当前状态是否为新增
	 * @return 当前状态是否为新增
	 */
	private boolean isAdd() {
		return !XLFHandler.getCustomMatchFilterMap().containsKey(filterNameTxt.getText());
	}

	@Override
	protected void cancelPressed() {
		close();
	}

	@Override
	public boolean close() {
		if (isChange()) {
			if (MessageDialog.openConfirm(getShell(), "", "确定不保存就关闭吗？")) {
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
			String orignXpath = XLFHandler.getCustomMatchFilterMap().get(filterNameTxt.getText());
			StringBuilder xpath = new StringBuilder();
			String link = andBtn.getSelection() ? " and " : " or ";
			for (DynaComposite comp : conditionList) { // 得到所有自定义条件组合的xpath
				String tempXpath = comp.getXpath(false);
				if (RESULT_FAILED.equals(tempXpath)) {
					result = true;
				}
				xpath.append(link).append(tempXpath);
			}
			appendCheckCondition(xpath);
			if (xpath.length() > 0) {
				xpath.delete(0, xpath.indexOf(link) + link.length());
				result = !orignXpath.equals(xpath.toString());
			}

		}
		return result;
	}

	/**
	 * @param args ;
	 */
	public static void main(String[] args) {
		Display display = new Display();
		final Shell shell = new Shell(display);
		shell.setLayout(new GridLayout(1, false));

		final Combo combo = new Combo(shell, SWT.READ_ONLY);
		combo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		Button button = new Button(shell, SWT.PUSH);
		button.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		button.setText("OK");
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				CustomMatchConditionDialog dialog = new CustomMatchConditionDialog(shell);
				int res = dialog.open();
				if (res == CustomMatchConditionDialog.OK) {

				}
			}
		});

		shell.open();

		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

	private static String[] filterData = new String[] { "匹配率", "关键字", "来源" };
	private static String[] conditionData1 = new String[] { "小于", "大于", "等于", "不等于" };
	private static String[] conditionData2 = new String[] { "包含", "不包含", "开始于", "结束于", "等于", "不等于", "匹配正则" };
	private static LinkedHashMap<String, String> mqOperators = new LinkedHashMap<String, String>();
	{
		mqOperators.put("小于", "<");
		mqOperators.put("大于", ">");
		mqOperators.put("等于", "=");
		mqOperators.put("不等于", "!=");
	}

	/**
	 * 动态条件组合
	 * @author  Leakey
	 * @version 
	 * @since   JDK1.6
	 */
	private class DynaComposite extends Composite {

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

		/** 当前选中的过滤器是否是（来源）. */
		private boolean isSource = false;
		/** 当前选中的过滤器是否是匹配率. */
		private boolean isMatchQt = true;
		/** 当前选中的过滤器是否是关键字. */
		private boolean isKeyword = false;

		private String[] tempIndex = null;

		/**
		 * 取得xpath查询语句
		 * @return xpath查询语句
		 */
		public String getXpath(boolean withNotice) {
			StringBuilder re = new StringBuilder();
			String txt = value.getText().trim();
			if (isSource) { // 来源
				if (txt.equals("") || txt.equals(initValue)) {
					if (withNotice) {
						MessageDialog.openInformation(getShell(), "", "请输入来源！");
					}
					return RESULT_FAILED;
				}
				re.append("@origin='").append(txt).append("'");
				tempIndex = new String[] { filterName.getSelectionIndex() + "", txt };
			} else if (isMatchQt) { // 匹配率
				if (txt.equals("") || txt.equals(initValue)) {
					if (withNotice) {
						MessageDialog.openInformation(getShell(), "", "请输入匹配率！");
					}
					return RESULT_FAILED;
				}
				String operator = conditions.getText().trim();
				if (operator != null && !"".equals(operator)) {
					operator = mqOperators.get(operator);
					re.append("translate(@match-quality, '%', '')").append(operator).append(txt);
				}
				tempIndex = new String[] { filterName.getSelectionIndex() + "", conditions.getSelectionIndex() + "",
						txt };
			} else if (isKeyword) { // 关键字

				if (txt.equals("") || txt.equals(initValue)) {
					if (withNotice) {
						MessageDialog.openInformation(getShell(), "", "请输入关键字！");
					}
					return RESULT_FAILED;
				}

				String operator = conditions.getText().trim();
				if (operator != null && !"".equals(operator)) {
					if ("包含".equals(operator)) {
						re.append("(contains(source, '").append(txt).append("') or contains(target, '").append(txt)
								.append("'))");
					} else if ("不包含".equals(operator)) {
						re.append("not(contains(source, '").append(txt).append("') or contains(target, '").append(txt)
								.append("'))");
					} else if ("开始于".equals(operator)) {
						re.append("(starts-with(source, '").append(txt).append("') or starts-with(target, '")
								.append(txt).append("'))");
					} else if ("结束于".equals(operator)) {
						re.append("(ends-with(source, '").append(txt).append("') or ends-with(target, '").append(txt)
								.append("'))");
					} else if ("等于".equals(operator)) {
						re.append("(source='").append(txt).append("' or target='").append(txt).append("')");
					} else if ("不等于".equals(operator)) {
						re.append("not(source='").append(txt).append("' or target='").append(txt).append("')");
					}
					// else if ("匹配正则".equals(operator)) {
					// re.append("(matches(source, '").append(keyWord).append("') or matches(target, '").append(keyWord)
					// .append("'))");
					// }
					tempIndex = new String[] { filterName.getSelectionIndex() + "",
							conditions.getSelectionIndex() + "", txt };
				}
			}
			return re.toString();
		}

		/**
		 * 得到选择的索引或者文本框的值
		 */
		public String[] getTempIndex() {
			return tempIndex;
		}

		/**
		 * 当前选择是否为“来源”
		 */
		private boolean isSource() {
			if (filterName.getSelectionIndex() == 2) {
				isMatchQt = false;
				isKeyword = false;
				isSource = true;
			} else {
				isSource = false;
			}
			return isSource;
		}

		/**
		 * 当前选择是否为“匹配率”
		 */
		private boolean isMatchQt() {
			if (filterName.getSelectionIndex() == 0) {
				isSource = false;
				isKeyword = false;
				isMatchQt = true;
			} else {
				isMatchQt = false;
			}
			return isMatchQt;
		}

		/**
		 * 当前选择是否为“关键字”
		 */
		private boolean isKeyword() {
			if (filterName.getSelectionIndex() == 1) {
				isSource = false;
				isMatchQt = false;
				isKeyword = true;
			} else {
				isKeyword = false;
			}
			return isKeyword;
		}

		/**
		 * 获取过滤器指定名称下拉框控件
		 */
		public Combo getFilterName() {
			return filterName;
		}

		/**
		 * 获取过滤器指定条件下拉框控件
		 */
		public Combo getConditions() {
			return conditions;
		}

		/**
		 * 获取文本框控件
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
		 * 初始化
		 */
		public void init() {
			this.setLayout(new GridLayout(5, false));
			this.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			initFilterName();
			initConditions(conditionData1);
			initTxt(1);
			initMatchQtTextListener();
			initBtn();

		}

		/**
		 * 初始化“+”，“－”按钮
		 */
		private void initBtn() {
			addBtn = new Button(this, SWT.BORDER);
			addBtn.setText("+");
			addBtn.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					new DynaComposite(dynaComp, SWT.NONE);
					scroll.setMinSize(dynaComp.computeSize(SWT.DEFAULT, SWT.DEFAULT));
					dynaComp.layout();
				}
			});
			reduceBtn = new Button(this, SWT.BORDER);
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
		private void initTxt(int span) {
			value = new Text(this, SWT.BORDER);
			setGray(value, initValue);
			GridData data = new GridData(GridData.FILL_HORIZONTAL);
			data.horizontalSpan = span;
			value.setLayoutData(data);
		}

		/**
		 * 初始化条件下拉框
		 */
		private void initConditions(String[] data) {
			conditions = new Combo(this, SWT.BORDER);
			conditions.setLayoutData(new GridData(100, 35));
			if (data == null || data.length == 0) {
				return;
			}
			setComboData(conditions, data);
			conditions.select(0);

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
		 * 初始化匹配率文本框的监听，只允许输入数字，最大值为100或者101
		 */
		private void initMatchQtTextListener() {
			value.addVerifyListener(new VerifyListener() {

				public void verifyText(VerifyEvent event) {
					if (!value.getText().equals(initValue)) {
						if (event.keyCode == 0 && event.stateMask == 0) { // 文本框得到焦点时

						} else if (Character.isDigit(event.character) || event.character == '\b'
								|| event.keyCode == 127) { // 输入数字，或者按下Backspace、Delete键
							Text txt = (Text) event.widget;
							if ("0".equals(txt.getText().trim()) && event.character == '0') {
								event.doit = false;
								return;
							}
							event.doit = true;
						} else {
							event.doit = false;
						}
					}
				}
			});

			value.addModifyListener(new ModifyListener() {
				final int max = isUEVersion() ? 101 : 100; // 最大值

				public void modifyText(ModifyEvent e) {
					if (!value.getText().equals(initValue) && !value.getText().equals("")) {
						Text txt = (Text) e.widget;
						String text = txt.getText().trim();
						if (text.length() == 2 && text.charAt(0) == '0') {
							txt.setText(text.charAt(1) + "");
							txt.setSelection(1);
						} else if (Integer.parseInt(text) > max) {
							txt.setText("100");
							txt.setSelection(3);
						}
					}
				}
			});
		}

		/**
		 * 初始化过滤器列表控件
		 */
		private void initFilterName() {
			filterName = new Combo(this, SWT.BORDER);
			filterName.setLayoutData(new GridData(80, 35));
			setComboData(filterName, filterData);
			filterName.setData("filterName");
			filterName.select(0);
			filterName.addSelectionListener(new SelectionAdapter() {
				private void createSource() {
					disposeChild();
					initTxt(2);
					initBtn();
				}

				private void createKeyword() {
					disposeChild();
					initConditions(conditionData2);
					initTxt(1);
					initBtn();
				}

				private void createMatchQt() {
					disposeChild();
					initConditions(conditionData1);
					initTxt(1);
					initMatchQtTextListener();
					initBtn();
				}

				@Override
				public void widgetSelected(SelectionEvent e) {
					if (isMatchQt()) {
						createMatchQt();
					}
					if (isKeyword()) {
						createKeyword();
					}
					if (isSource()) {
						createSource();
					}
					DynaComposite.this.layout();
				}
			});
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

	/**
	 * 设置Combo下拉列表中的数据
	 */
	private void setComboData(Combo combo, String[] data) {
		if (combo == null || data == null || data.length == 0) {
			return;
		}
		combo.clearSelection();
		combo.removeAll();
		int i = 0;
		for (String temp : data) {
			combo.add(temp, i++);
		}
	}

	/**
	 * 为List组件设置数据
	 * @param list
	 * @param data
	 */
	private void setListData(List list, Map<String, String> data) {
		list.removeAll();
		if (data == null) {
			return;
		}
		for (Entry<String, String> entry : data.entrySet()) {
			list.add(entry.getKey());
		}
	}

}

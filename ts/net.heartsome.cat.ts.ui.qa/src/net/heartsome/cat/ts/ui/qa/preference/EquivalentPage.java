package net.heartsome.cat.ts.ui.qa.preference;

import java.text.MessageFormat;

import net.heartsome.cat.common.ui.HsImageLabel;
import net.heartsome.cat.common.util.CommonFunction;
import net.heartsome.cat.ts.core.qa.QAConstant;
import net.heartsome.cat.ts.ui.qa.Activator;
import net.heartsome.cat.ts.ui.qa.resource.Messages;
import net.heartsome.cat.ts.ui.resource.ImageConstant;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * 等效系统的首选项设置 存储在PreferenceStore中的格式为"internalRepeat:0.50;external101:0.50;external100:0.50;95-99:0.60;85-94:0.70;"
 * @author robert 2011-12-17

加权字数	Weighted Word Count
加权系数	Weighted factor
等效字数 Equivalent wordcount to be paid at full word rate
等效系数 Percentage Payment of Full Word Rate
 */
public class EquivalentPage extends PreferencePage implements IWorkbenchPreferencePage {

	public static final String ID = "net.heartsome.cat.ts.ui.qa.preference.EquivalentPage";

	IPreferenceStore preferenceStore;
	private int defaultValue = 95;
	private Image addImage;
	private Image deleteimage;
	private int index = 1;
	private int selectionvalue = 0;
	private int space_Number = 10;
	private GridData btn_data;
	private GridData spinnerdata;
	private GridData cmpData;
	/** 匹配区间所在的父面板 */
	private Composite matchParent;
	/** 重复文本段的加权系数设置所在的面板 */
	private Composite repeatCmp;
	private Composite equiCmp;
	private GridData equiTxtData;
	private GridData phLblData;
	/** 两个按钮所在小面板的长度 */
	private int btnCmpWidth = 100;
	/** 组件是否初始化 */
	private boolean isInit = false;
	private boolean isUltimate = CommonFunction.checkEdition("U");
	
	/**　最低匹配率常量，可以修改 */
	private static final int MINRATE = 1;

	public void init(IWorkbench workbench) {
	}

	public EquivalentPage() {
		setTitle(Messages.getString("preference.EquivalentPage.title"));
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		preferenceStore = getPreferenceStore();

		spinnerdata = new GridData();
		spinnerdata.widthHint = 30;
		equiTxtData = new GridData(SWT.CENTER, SWT.CENTER, false, false);
		equiTxtData.widthHint = 50;
		
		phLblData = new GridData(SWT.FILL, SWT.CENTER, true, false);
		
		addImage = Activator.getImageDescriptor("images/addSign.png").createImage();
		deleteimage = Activator.getImageDescriptor("images/deleteSign.png").createImage();
	}

	@Override
	protected Control createContents(Composite parent) {
		isInit = true;

		Composite tparent = new Composite(parent, SWT.NONE);
		tparent.setLayout(new GridLayout());
		tparent.setLayoutData(new GridData(GridData.FILL_BOTH));

		Group group = new Group(tparent, SWT.NONE);
		group.setText(Messages.getString("preference.EquivalentPage.group"));

		group.setLayout(new GridLayout(1, false));
		GridData groupData = new GridData(GridData.FILL_BOTH);
		groupData.widthHint = 500;
		groupData.heightHint = 460;
		group.setLayoutData(groupData);

		String tip = Messages.getString("preference.EquivalentPage.equiImgLbl");
		HsImageLabel equiImgLbl = new HsImageLabel(tip,
				Activator.getImageDescriptor(ImageConstant.PREFERENCE_FA_equivalent));
		equiCmp = equiImgLbl.createControl(group);
		equiCmp.setLayout(new GridLayout());
		equiCmp.setLayoutData(new GridData(GridData.FILL_BOTH));

		equiImgLbl.computeSize();

		// 设置重复文本段的加权系统
		createRepeatCmp();
		
		// 内部匹配加权系数
		createEqui(equiCmp);
		createMatchEqui(preferenceStore.getString(QAConstant.FA_PREF_equivalent));

		// scroll.setSize(scroll.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		return parent;
	}
	
	private void createRepeatCmp(){
		repeatCmp = new Composite(equiCmp, SWT.NONE);
		repeatCmp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		GridLayoutFactory.fillDefaults().margins(0, 0).numColumns(1).equalWidth(false).applyTo(repeatCmp);
		
		GridData btnPhLblData = new GridData(SWT.CENTER, SWT.CENTER, false, false);
		btnPhLblData.widthHint = btnCmpWidth;
		
		// 内部重复面板
		Composite internalRepeatCmp = new Composite(repeatCmp, SWT.NONE);
		internalRepeatCmp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		GridLayoutFactory.fillDefaults().margins(0, 0).numColumns(4).equalWidth(false).applyTo(internalRepeatCmp);
		
		Label matchLbl = new Label(internalRepeatCmp, SWT.NONE);
		matchLbl.setText(Messages.getString("preference.EquivalentPage.msg2"));
		
		Label phLbl = new Label(internalRepeatCmp, SWT.NONE);	// 这个label 是占位用的
		phLbl.setLayoutData(phLblData);
		
		
		Text equiTxt = new Text(internalRepeatCmp, SWT.BORDER);
		equiTxt.setLayoutData(equiTxtData);
		validEquiTxt(equiTxt);
		
		Label btnPhLbl = new Label(internalRepeatCmp, SWT.NONE);	// 为了与下面的对齐，这里是两个按钮的占位。
		btnPhLbl.setLayoutData(btnPhLblData);
		
		// 外部 101% 匹配面板
		if (isUltimate) {
			Composite exter101Cmp = new Composite(repeatCmp, SWT.NONE);
			exter101Cmp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			GridLayoutFactory.fillDefaults().margins(0, 0).numColumns(4).equalWidth(false).applyTo(exter101Cmp);
			
			matchLbl = new Label(exter101Cmp, SWT.NONE);
			matchLbl.setText(Messages.getString("preference.EquivalentPage.msg3"));
			
			phLbl = new Label(exter101Cmp, SWT.NONE);	// 这个label 是占位用的
			phLbl.setLayoutData(phLblData);
			
			
			equiTxt = new Text(exter101Cmp, SWT.BORDER);
			equiTxt.setLayoutData(equiTxtData);
			validEquiTxt(equiTxt);
			
			btnPhLbl = new Label(exter101Cmp, SWT.NONE);
			btnPhLbl.setLayoutData(btnPhLblData);
		}

		
		// 外部 100% 匹配面板
		Composite exter100Cmp = new Composite(repeatCmp, SWT.NONE);
		exter100Cmp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		GridLayoutFactory.fillDefaults().margins(0, 0).numColumns(4).equalWidth(false).applyTo(exter100Cmp);
		
		matchLbl = new Label(exter100Cmp, SWT.NONE);
		matchLbl.setText(Messages.getString("preference.EquivalentPage.msg4"));
		
		phLbl = new Label(exter100Cmp, SWT.NONE);	// 这个label 是占位用的
		phLbl.setLayoutData(phLblData);
		
		
		equiTxt = new Text(exter100Cmp, SWT.BORDER);
		equiTxt.setLayoutData(equiTxtData);
		validEquiTxt(equiTxt);
		
		btnPhLbl = new Label(exter100Cmp, SWT.NONE);
		btnPhLbl.setLayoutData(btnPhLblData);
		
		GridData separatorLblData = new GridData(SWT.FILL, SWT.CENTER, true, false);
		separatorLblData.horizontalSpan = 4;
		
		Label separatorLbl = new Label(repeatCmp, SWT.SEPARATOR | SWT.HORIZONTAL);
		separatorLbl.setLayoutData(separatorLblData);
	}
	
	/**
	 * 验证用户输入的加权系数的正确性
	 * @param equiTxt
	 */
	private void validEquiTxt(final Text equiTxt){
		final String defaultStr = "0.50";
		equiTxt.setText(defaultStr);
		equiTxt.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				String textStr = equiTxt.getText().trim();
				if (textStr == null || textStr.trim().length() == 0) {
					equiTxt.setText(defaultStr);
				}else {
					String regular = "1\\.(0){0,2}|0\\.\\d{0,2}";
					if (!textStr.matches(regular)) {
						MessageDialog.openInformation(getShell(), Messages.getString("preference.EquivalentPage.msgTitle"), 
							Messages.getString("preference.EquivalentPage.msg5"));
						equiTxt.setText(defaultStr);
					}
				}
			}
		});
	}

	public void createEqui(Composite equiCmp) {
		cmpData = new GridData(GridData.FILL_HORIZONTAL);
		cmpData.widthHint = 570;

		matchParent = new Composite(equiCmp, SWT.NONE);
		matchParent.setLayoutData(new GridData(GridData.FILL_BOTH));
		GridLayoutFactory.fillDefaults().margins(0, 0).spacing(0, 2).applyTo(matchParent);

		Composite match = new Composite(matchParent, SWT.NONE);
		match.setLayoutData(cmpData);
		GridLayoutFactory.fillDefaults().margins(0, 0).numColumns(8).equalWidth(false).applyTo(match);

		Spinner mininSpinner = new Spinner(match, SWT.BORDER);

		int minSpinnerIndex = 55;
		while (minSpinnerIndex <= 95) {
			mininSpinner.setSelection(minSpinnerIndex);
			minSpinnerIndex = minSpinnerIndex + 5;
		}

		mininSpinner.setMinimum(35);
		mininSpinner.setMaximum(99);
		mininSpinner.setIncrement(5);
		mininSpinner.setSelection(defaultValue);
		mininSpinner.setLayoutData(spinnerdata);
		mininSpinner.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				selected(e);
			}
		});

		Label perLabel1 = new Label(match, SWT.NONE);
		perLabel1.setText("%");

		Label waveLabel = new Label(match, SWT.NONE);
		waveLabel.setText("~");

		Spinner maxSpinner = new Spinner(match, SWT.BORDER);
		maxSpinner.setSelection(99);
		maxSpinner.setEnabled(false);
		maxSpinner.setLayoutData(spinnerdata);

		Label perLabel2 = new Label(match, SWT.NONE);
		perLabel2.setText("%");
		
		Label phLbl = new Label(match, SWT.NONE);
		phLbl.setLayoutData(phLblData);

		Text equiTxt = new Text(match, SWT.BORDER);
		equiTxt.setLayoutData(equiTxtData);
		validEquiTxt(equiTxt);

		btn_data = new GridData();
		btn_data.widthHint = 40;
		btn_data.heightHint = 26;
		btn_data.horizontalIndent = 5;

		Composite btnCmp = new Composite(match, SWT.NONE);
		GridLayoutFactory.swtDefaults().numColumns(2).margins(0, 0).applyTo(btnCmp);
		GridDataFactory.swtDefaults().hint(btnCmpWidth, SWT.DEFAULT).applyTo(btnCmp);
		
		Button addBtn = new Button(btnCmp, SWT.NONE);
		addBtn.setLayoutData(btn_data);
		addBtn.setImage(addImage);

		addBtn.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (index < 11) {
					addEquiCmp(e, null);
				}
			}
		});

		Label spaceLbl = new Label(btnCmp, SWT.NONE);
		spaceLbl.setLayoutData(btn_data);

		
		match.getParent().setData(String.valueOf(index), match);
		match.setData("index", index);

		match.pack();
		match.layout();
		match.redraw();
	}

	/**
	 * 点击添加按钮时，添加加权系数匹配面板
	 * @param e
	 */
	public void addEquiCmp(SelectionEvent e, Button button) {

		Composite cp;
		if (button == null) {
			Button btn = (Button) e.getSource();
			cp = btn.getParent().getParent();
		} else {
			cp = button.getParent().getParent();
		}

		// 获取加权系数面板的父面板，即group
		Composite cp_parent = cp.getParent();
		int temp_index = cp_parent.getChildren().length;
		Composite temp_cp = (Composite) cp_parent.getData(String.valueOf(temp_index));
		Control[] ctrls = temp_cp.getChildren();
		// 获取最小匹配率
		Spinner tempMinSp = (Spinner) ctrls[0];
		selectionvalue = tempMinSp.getSelection() - 1;

		if (selectionvalue < MINRATE) {
			MessageDialog.openInformation(tempMinSp.getShell(),
					Messages.getString("preference.EquivalentPage.msgTitle"),
					MessageFormat.format(Messages.getString("preference.EquivalentPage.msg1"), MINRATE));
			return;
		}

		Composite new_cp = new Composite(cp_parent, SWT.NONE);
		new_cp.setLayoutData(cmpData);
		GridLayoutFactory.fillDefaults().margins(0, 0).numColumns(8).equalWidth(false).applyTo(new_cp);

		Spinner minSp = new Spinner(new_cp, SWT.BORDER);
		minSp.setMinimum(0);
		minSp.setMaximum(selectionvalue);
		minSp.setIncrement(5);
		minSp.setLayoutData(spinnerdata);

		minSp.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				selected(e);
			}
		});

		if (tempMinSp.getSelection() - space_Number >= 0) {
			minSp.setSelection(tempMinSp.getSelection() - space_Number);
		} else {
			minSp.setSelection(0);
		}

		Label perLabel1 = new Label(new_cp, SWT.NONE);
		perLabel1.setText("%");

		Label waveLabel = new Label(new_cp, SWT.NONE);
		waveLabel.setText("~");

		Spinner maxSp = new Spinner(new_cp, SWT.BORDER);
		maxSp.setSelection(tempMinSp.getSelection() - 1);
		maxSp.setEnabled(false);
		maxSp.setLayoutData(spinnerdata);

		Label perLabel2 = new Label(new_cp, SWT.NONE);
		perLabel2.setText("%");
		
		Label phLbl = new Label(new_cp, SWT.NONE);
		phLbl.setLayoutData(phLblData);

		Text equiTxt = new Text(new_cp, SWT.BORDER);
		equiTxt.setLayoutData(equiTxtData);
		validEquiTxt(equiTxt);

		Composite btnCmp = new Composite(new_cp, SWT.NONE);
		GridLayoutFactory.swtDefaults().numColumns(2).margins(0, 0).applyTo(btnCmp);
		GridDataFactory.swtDefaults().hint(btnCmpWidth, SWT.DEFAULT).applyTo(btnCmp);
		
		// 增加加权系数面板的按钮及其事件
		Button addBtn = new Button(btnCmp, SWT.NONE);
		addBtn.setImage(addImage);
		addBtn.setLayoutData(btn_data);
		addBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (index < 11) {
					addEquiCmp(e, null);
				}
			}
		});

		// 删除加权系数的按钮及其事件
		Button deleteBtn = new Button(btnCmp, SWT.NONE);
		deleteBtn.setImage(deleteimage);
		deleteBtn.setLayoutData(btn_data);
		deleteBtn.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				deleteEquiCmp(e);
			}
		});

		index = index + 1;
		cp_parent.setData(String.valueOf(index), new_cp);
		new_cp.setData("index", String.valueOf(index)); //$NON-NLS-1$

//		new_cp.pack();
//		new_cp.layout();

//		cp_parent.layout();
//		equiCmp.getParent().layout();
//		equiCmp.getParent().getParent().layout();
		equiCmp.getParent().getParent().getParent().layout();
	}

	/**
	 * 删除加权系数面板
	 * @param e
	 */
	public void deleteEquiCmp(SelectionEvent e) {
		Button deleteBtn = (Button) e.getSource();
		Composite cp = deleteBtn.getParent().getParent();
		Composite cp_parent = cp.getParent();
		Control[] ctr_cp = cp_parent.getChildren();
		Composite currentCp;
		Composite nextCp;
		Control[] currentCtrls;
		Control[] nextCtrls;
		Spinner cuSp;
		Spinner nextSp;
		int next = 0;
		int current = 0;
		int uplimit = 0;

		// get current rows index,we can get Composite by current rows index
		current = Integer.parseInt(cp.getData("index").toString());

		// if current composite is not the last Component
		if (current < ctr_cp.length) {
			next = current + 1;
			currentCp = (Composite) ctr_cp[current - 1];
			nextCp = (Composite) ctr_cp[next - 1];
			currentCtrls = currentCp.getChildren();
			nextCtrls = nextCp.getChildren();
			cuSp = (Spinner) currentCtrls[3];
			nextSp = (Spinner) nextCtrls[3];
			cuSp.setEnabled(true);
			nextSp.setEnabled(true);
			nextSp.setSelection(cuSp.getSelection());
			nextSp.setEnabled(false);
		}
		ctr_cp[current - 1].dispose(); // delete current composite

		// reset index of whole container
		uplimit = ctr_cp.length;
		for (int j = current; j < uplimit; j++) {
			Composite tempcp = (Composite) ctr_cp[j];
			tempcp.setData("index", String.valueOf(j)); //$NON-NLS-1$
			cp_parent.setData(String.valueOf(j), tempcp);
		}

		index = ctr_cp.length - 1;

		// 设置每一个匹配区间的最小匹配率
		for (int i = 1; i < matchParent.getChildren().length; i++) {
			Composite _cmp = (Composite) matchParent.getChildren()[i];
			Spinner _minSp = (Spinner) _cmp.getChildren()[0];
			_minSp.setMinimum(matchParent.getChildren().length - i - 1);
			Spinner _maxSp = (Spinner) _cmp.getChildren()[3];
			_minSp.setMaximum(Integer.parseInt(_maxSp.getText()));
		}

//		cp_parent.pack();
//		cp_parent.layout();

//		cp_parent.getParent().layout();
//		equiCmp.getParent().layout();
//		equiCmp.getParent().getParent().layout();
		equiCmp.getParent().getParent().getParent().layout();
	}

	/**
	 * 改变相对最小匹配率的大小时，动态改变下一行相对最大匹配率的大小
	 * @param arg0
	 */
	private void selected(SelectionEvent e) {
		Composite currentCp, nextCp, parentCp;
		int currentIndex = 0;
		int currentValue = 0;
		int upValue = 0;
		int nextMinValue = 0;
		Control[] paCtrls;
		Control[] cuCtrls;
		Control[] nextCtrls;

		Spinner sp3 = (Spinner) e.getSource();
		// 当前相对最小匹配值
		currentValue = sp3.getSelection();
		currentCp = sp3.getParent();

		parentCp = currentCp.getParent();

		paCtrls = parentCp.getChildren();

		cuCtrls = currentCp.getChildren();

		Spinner cuMinSp = (Spinner) cuCtrls[0];
		cuMinSp.setSelection(currentValue);

		Spinner cuMaxSp = (Spinner) cuCtrls[3];
		upValue = cuMaxSp.getSelection();

		currentIndex = Integer.parseInt(currentCp.getData("index").toString());
		cuMinSp.setMinimum(paCtrls.length - currentIndex + MINRATE);

		if (currentIndex >= paCtrls.length) {
			if (currentValue > upValue) {
				cuMinSp.setSelection(upValue);
			}
			parentCp.layout();
			parentCp.redraw();
			return;
		}

		// 从当前加权设置的下一条设置开始，直到结束，
		for (int i = currentIndex + 1; i <= paCtrls.length; i++) {
			int remain = paCtrls.length - i + MINRATE;
			// 获取下一个加权设置面板
			nextCp = (Composite) paCtrls[i - 1];
			nextCtrls = nextCp.getChildren();
			Spinner nextMinSp = (Spinner) nextCtrls[0];
			// 最低值不能小于这个匹配之下所有匹配区间的总数
			nextMinSp.setMinimum(remain);

			nextMinValue = nextMinSp.getSelection();
			Spinner nextMaxSp = (Spinner) nextCtrls[3];

			// 如果当前循环的相对最小匹配值小于它的最大值，设成等于最大值
			if (nextMinValue > (currentValue - 1)) {
				nextMinSp.setSelection(currentValue - 1);
			}

			nextMaxSp.setSelection(currentValue - 1);
			nextMinSp.setMaximum(nextMaxSp.getSelection());

			currentValue = nextMinSp.getSelection();
		}

		parentCp.layout();
		parentCp.redraw();
	}

	@Override
	protected void performDefaults() {
		String equivalStr = preferenceStore.getDefaultString(QAConstant.FA_PREF_equivalent);
		createMatchEqui(equivalStr);
	}

	@Override
	public boolean performOk() {
		if (!isInit) {
			return true;
		}

		StringBuffer equivalSB = new StringBuffer();
		// 先获取重复文本段的加权系数
		Composite interRepeatCmp = (Composite) repeatCmp.getChildren()[0];
		Text text = (Text) interRepeatCmp.getChildren()[2];
		equivalSB.append(QAConstant._InternalRepeat + ":" + text.getText() + ";");
		
		if (isUltimate) {
			Composite exter101Cmp = (Composite) repeatCmp.getChildren()[1];
			text = (Text) exter101Cmp.getChildren()[2];
			equivalSB.append(QAConstant._External101 + ":" + text.getText() + ";");
		}
		
		Composite exter100Cmp = (Composite) repeatCmp.getChildren()[2 - (isUltimate ? 0 : 1)];
		text = (Text) exter100Cmp.getChildren()[2];
		equivalSB.append(QAConstant._External100 + ":" + text.getText() + ";");
		
		for (int i = 0; i < matchParent.getChildren().length; i++) {
			Composite cmp = (Composite) matchParent.getChildren()[i];
			Control[] ctr_cmp = cmp.getChildren();
			text = (Text) ctr_cmp[6];
			Spinner minSp = (Spinner) ctr_cmp[0];
			Spinner maxSp = (Spinner) ctr_cmp[3];

			equivalSB.append(minSp.getSelection() + "-" + maxSp.getSelection() + ":" + text.getText() + ";");
		}

		preferenceStore.setValue(QAConstant.FA_PREF_equivalent, equivalSB.toString());

		return true;
	}

	/**
	 * 创建加权系统设置区间
	 */
	public void createMatchEqui(String equivalStr) {
		boolean isDelete = false;
		String[] equivalArray = equivalStr.split(";");
		for (int i = 0; i < equivalArray.length; i++) {
			String equival = equivalArray[i];
			String matchStr = equival.substring(0, equival.indexOf(":"));
			String equiValue = equival.substring(equival.indexOf(":") + 1, equival.length());
			// 先处理所有重复加权系数
			if (QAConstant._InternalRepeat.equals(matchStr)) {
				Composite interRepeatCmp = (Composite) repeatCmp.getChildren()[0];
				Text text = (Text) interRepeatCmp.getChildren()[2];
				text.setText(equiValue);
			}else if (QAConstant._External101.equals(matchStr)) {
				if (isUltimate) {
					Composite exter101Cmp = (Composite) repeatCmp.getChildren()[1];
					Text text = (Text) exter101Cmp.getChildren()[2];
					text.setText(equiValue);
				}
			}else if (QAConstant._External100.equals(matchStr)) {
				Composite exter100Cmp = (Composite) repeatCmp.getChildren()[2 - (isUltimate ? 0 : 1)];
				Text text = (Text) exter100Cmp.getChildren()[2];
				text.setText(equiValue);
			}else {
				int minMacth = Integer.parseInt(equival.substring(0, equival.indexOf("-")));
				int maxMatch = Integer.parseInt(equival.substring(equival.indexOf("-") + 1, equival.indexOf(":")));
				if (maxMatch == 99) {
					// 如果最大匹配率是99，那么就是模糊匹配的第一个设置
					Composite macthCmp = (Composite) matchParent.getChildren()[0];
					Control[] ctls = macthCmp.getChildren();
					Text text = (Text) ctls[6];
					Spinner minSp = (Spinner) ctls[0];
					Spinner maxSp = (Spinner) ctls[3];

					text.setText(equiValue);
					minSp.setSelection(minMacth);
					maxSp.setSelection(maxMatch);
				} else {
					if (!isDelete) {
						// 若模糊匹配区间有两个或两个以上的匹配，那么先删除所有除第一个以外的其他设置
						if (matchParent.getChildren().length > 1) {
							for (int j = matchParent.getChildren().length - 1; j >= 1; j--) {
								Control[] ctls = matchParent.getChildren();
								ctls[j].dispose();
							}
							index = 1;
						}

						// 创建除第一个模糊匹配之外的其他匹配段
						int fixItemNum = equivalStr.indexOf("external101") == -1 ? 3 : 4;
						for (int j = 0; j < equivalStr.split(";").length - fixItemNum; j++) {
							Composite cmp = (Composite) ((Composite) matchParent.getChildren()[j]).getChildren()[7];
							Button addBtn = (Button) cmp.getChildren()[0];
							addEquiCmp(null, addBtn);
						}
						isDelete = true;
					}
					
					int fixItemNum = equivalStr.indexOf("external101") == -1 ? 2 : 3;
					Composite macthCmp = (Composite) matchParent.getChildren()[i - fixItemNum];
					Control[] ctls = macthCmp.getChildren();
					Text text = (Text) ctls[6];
					Spinner minSp = (Spinner) ctls[0];
					Spinner maxSp = (Spinner) ctls[3];

					text.setText(equiValue);
					minSp.setSelection(minMacth);
					maxSp.setSelection(maxMatch);
				}
			}
		}
//		matchParent.pack();
		matchParent.layout();
		// matchParent.getParent().pack();
//		matchParent.getParent().layout();
	}

	@Override
	public void dispose() {
		if(addImage != null && !addImage.isDisposed()){
			addImage.dispose();
		}
		if(deleteimage != null && !deleteimage.isDisposed()){
			deleteimage.dispose();
		}
		super.dispose();
	}
}

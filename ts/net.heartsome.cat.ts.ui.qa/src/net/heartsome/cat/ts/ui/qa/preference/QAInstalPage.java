package net.heartsome.cat.ts.ui.qa.preference;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.heartsome.cat.ts.core.qa.QAConstant;
import net.heartsome.cat.common.ui.HsImageLabel;
import net.heartsome.cat.ts.ui.qa.Activator;
import net.heartsome.cat.ts.ui.qa.model.QAModel;
import net.heartsome.cat.ts.ui.qa.resource.Messages;
import net.heartsome.cat.ts.ui.resource.ImageConstant;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.nebula.jface.tablecomboviewer.TableComboViewer;
import org.eclipse.nebula.widgets.tablecombo.TableCombo;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QAInstalPage extends PreferencePage implements IWorkbenchPreferencePage, Listener {

	public static final String ID = "net.heartsome.cat.ts.ui.qa.preference.QAInstalPage";
	public final static Logger logger = LoggerFactory.getLogger(QAInstalPage.class.getName());
	private IPreferenceStore preferenceStore;
	private Map<String, HashMap<String, String>> qaItemId_Name_Class;
	
	private Button batchTermBtn;
	private Button batchParaBtn;
	private Button batchNumberBtn;
	private Button batchTagBtn;
	private Button batchNonTransBtn;
	private Button batchSpaceOfParaBtn;
	private Button batchParaCompleteBtn;
	private Button batchTgtLengthLimitBtn;
	private Button batchSpellBtn;
	
	private Button autoTermBtn;
	//private Button autoParaBtn;
	private Button autoNumberBtn;
	private Button autoTagBtn;
	private Button autoNonTransBtn;
	private Button autoSpaceOfParaBtn;
	private Button autoParaCompleteBtn;
	private Button autoTgtLengthLimitBtn;
	private Button autoSpellBtn;
	
	private TableComboViewer termCmb;
	private TableComboViewer paraCmb;
	private TableComboViewer numberCmb;
	private TableComboViewer tagCmb;
	private TableComboViewer nonTransCmb;
	private TableComboViewer spaceOfParaCmb;
	private TableComboViewer paraCompleteCmb;
	private TableComboViewer tgtLengthLimitCmb;
	private TableComboViewer spellCmb;
	
	/** 组件是否初始化 */
	private boolean isInit = false;
	/** 批量检查按钮的值－－批量检查 */
	private static final String CONSTANT_BATCHQA = Messages.getString("qa.preference.QAInstalPage.batchQA");
	/** 自动检查按钮的值－－自动检查 */
	private static final String CONSTANT_AUTOQA = Messages.getString("qa.preference.QAInstalPage.autoQA");
	/** 提示级别按钮的值－－提示级别 */
	private static final String CONSTANT_TIPLEVEL = Messages.getString("qa.preference.QAInstalPage.tipLevel");
	private static final String errorTip = Messages.getString("qa.all.tipLevel.error");
	private static final String warTip = Messages.getString("qa.all.tipLevel.warning");
	private final List<String> CONSTANT_COMBOVALUE = new ArrayList<String>();
	private Image errorImg;
	private Image warnImg;
	
	private Button whenApprovalBtn;
	private Button whenAddToDbBtn;
	
	
	public QAInstalPage() {
		setTitle(Messages.getString("qa.all.qa"));
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		preferenceStore = getPreferenceStore();
		QAModel qaModel = new QAModel();
		qaItemId_Name_Class = qaModel.getQaItemId_Name_Class();
		CONSTANT_COMBOVALUE.add(errorTip);
		CONSTANT_COMBOVALUE.add(warTip);
	}

	public void init(IWorkbench workbench) {
		// TODO Auto-generated method stub

	}

	@Override
	protected Control createContents(Composite parent) {
		isInit = true;
		try {
			String bundlePath = FileLocator.toFileURL(Platform.getBundle("net.heartsome.cat.ts.ui.qa").getEntry("")).getPath();
			errorImg = new Image(getShell().getDisplay(), bundlePath + "icons/error.png");
			warnImg = new Image(getShell().getDisplay(), bundlePath + "icons/warning.png");
			
		} catch (IOException e) {
			e.printStackTrace();
			logger.error(Messages.getString("qa.preference.QAInstalPage.log1"), e);
		}
		
		Composite tparent = new Composite(parent, SWT.NONE);
		tparent.setLayout(new GridLayout());
		tparent.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		Group qaItemInstalGroup = new Group(tparent, SWT.NONE);
		qaItemInstalGroup.setLayout(new GridLayout());
		GridDataFactory.fillDefaults().grab(true, false).hint(600, SWT.DEFAULT).applyTo(qaItemInstalGroup);
		qaItemInstalGroup.setText(Messages.getString("qa.preference.QAInstalPage.qaInstal"));
		
		String tip = Messages.getString("preference.QAInstalPage.itemInstalLbl");
		HsImageLabel itemInstalLbl = new HsImageLabel(tip, 
				Activator.getImageDescriptor(ImageConstant.PREFERENCE_QA_instal_itemsChoose));
		Composite instalCmp = itemInstalLbl.createControl(qaItemInstalGroup);
		GridLayoutFactory.fillDefaults().numColumns(4).spacing(20, 2)
				.applyTo(instalCmp);
		instalCmp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		GridLayout lCmpLayout = new GridLayout(2, false);
		lCmpLayout.marginWidth = 0;
		lCmpLayout.marginHeight = 0;
		
		GridData separatorLblData = new GridData(SWT.FILL, SWT.CENTER, true, false);
		separatorLblData.horizontalSpan = 4;
		
		// 术语一致性检查
		Label termLbl = new Label(instalCmp, SWT.NONE);
		termLbl.setText(qaItemId_Name_Class.get(QAConstant.QA_TERM).get(QAConstant.QA_ITEM_NAME));
		
		batchTermBtn = new Button(instalCmp, SWT.CHECK);
		batchTermBtn.setText(CONSTANT_BATCHQA);
		
		autoTermBtn = new Button(instalCmp, SWT.CHECK);
		autoTermBtn.setText(CONSTANT_AUTOQA);
		
		Composite levelCmp = new Composite(instalCmp, SWT.NONE);
		levelCmp.setLayout(lCmpLayout);
		new Label(levelCmp, SWT.NONE).setText(CONSTANT_TIPLEVEL);
		termCmb = new TableComboViewer(levelCmp, SWT.READ_ONLY|SWT.BORDER);	
		createTableCombo(termCmb);
		
		Label separatorLbl = new Label(instalCmp, SWT.SEPARATOR | SWT.HORIZONTAL);
		separatorLbl.setLayoutData(separatorLblData);
		
		// 数字一致性检查
		Label numberLbl = new Label(instalCmp, SWT.NONE);
		numberLbl.setText(qaItemId_Name_Class.get(QAConstant.QA_NUMBER).get(QAConstant.QA_ITEM_NAME));
		
		batchNumberBtn = new Button(instalCmp, SWT.CHECK);
		batchNumberBtn.setText(CONSTANT_BATCHQA);
		
		autoNumberBtn = new Button(instalCmp, SWT.CHECK);
		autoNumberBtn.setText(CONSTANT_AUTOQA);

		levelCmp = new Composite(instalCmp, SWT.NONE);
		levelCmp.setLayout(lCmpLayout);
		new Label(levelCmp, SWT.NONE).setText(CONSTANT_TIPLEVEL);
		numberCmb = new TableComboViewer(levelCmp, SWT.READ_ONLY|SWT.BORDER);	
		createTableCombo(numberCmb);
		
		separatorLbl = new Label(instalCmp, SWT.SEPARATOR | SWT.HORIZONTAL);
		separatorLbl.setLayoutData(separatorLblData);
		
		
		// 标记一致性检查
		Label tagLbl = new Label(instalCmp, SWT.NONE);
		tagLbl.setText(qaItemId_Name_Class.get(QAConstant.QA_TAG).get(QAConstant.QA_ITEM_NAME));
		
		batchTagBtn = new Button(instalCmp, SWT.CHECK);
		batchTagBtn.setText(CONSTANT_BATCHQA);
		
		autoTagBtn = new Button(instalCmp, SWT.CHECK);
		autoTagBtn.setText(CONSTANT_AUTOQA);
		
		levelCmp = new Composite(instalCmp, SWT.NONE);
		levelCmp.setLayout(lCmpLayout);
		new Label(levelCmp, SWT.NONE).setText(CONSTANT_TIPLEVEL);
		tagCmb = new TableComboViewer(levelCmp, SWT.READ_ONLY|SWT.BORDER);	
		createTableCombo(tagCmb);
		
		separatorLbl = new Label(instalCmp, SWT.SEPARATOR | SWT.HORIZONTAL);
		separatorLbl.setLayoutData(separatorLblData);
		
		// 非译元素检查
		Label nonTransLbl = new Label(instalCmp, SWT.NONE);
		nonTransLbl.setText(qaItemId_Name_Class.get(QAConstant.QA_NONTRANSLATION).get(QAConstant.QA_ITEM_NAME));
		
		batchNonTransBtn = new Button(instalCmp, SWT.CHECK);
		batchNonTransBtn.setText(CONSTANT_BATCHQA);
		
		autoNonTransBtn = new Button(instalCmp, SWT.CHECK);
		autoNonTransBtn.setText(CONSTANT_AUTOQA);
		
		levelCmp = new Composite(instalCmp, SWT.NONE);
		levelCmp.setLayout(lCmpLayout);
		new Label(levelCmp, SWT.NONE).setText(CONSTANT_TIPLEVEL);
		nonTransCmb = new TableComboViewer(levelCmp, SWT.READ_ONLY|SWT.BORDER);	
		createTableCombo(nonTransCmb);
		
		separatorLbl = new Label(instalCmp, SWT.SEPARATOR | SWT.HORIZONTAL);
		separatorLbl.setLayoutData(separatorLblData);
		
		
		//段首段末空格检查
		Label spaceOfParaLbl = new Label(instalCmp, SWT.NONE);
		spaceOfParaLbl.setText(qaItemId_Name_Class.get(QAConstant.QA_SPACEOFPARACHECK).get(QAConstant.QA_ITEM_NAME));
		
		batchSpaceOfParaBtn = new Button(instalCmp, SWT.CHECK);
		batchSpaceOfParaBtn.setText(CONSTANT_BATCHQA);
		
		autoSpaceOfParaBtn = new Button(instalCmp, SWT.CHECK);
		autoSpaceOfParaBtn.setText(CONSTANT_AUTOQA);
		
		levelCmp = new Composite(instalCmp, SWT.NONE);
		levelCmp.setLayout(lCmpLayout);
		new Label(levelCmp, SWT.NONE).setText(CONSTANT_TIPLEVEL);
		spaceOfParaCmb = new TableComboViewer(levelCmp, SWT.READ_ONLY|SWT.BORDER);	
		createTableCombo(spaceOfParaCmb);
		
		separatorLbl = new Label(instalCmp, SWT.SEPARATOR | SWT.HORIZONTAL);
		separatorLbl.setLayoutData(separatorLblData);

		
		//文本段完整性检查
		Label paraCompleteLbl = new Label(instalCmp, SWT.NONE);
		paraCompleteLbl.setText(qaItemId_Name_Class.get(QAConstant.QA_PARACOMPLETENESS).get(QAConstant.QA_ITEM_NAME));
		
		batchParaCompleteBtn = new Button(instalCmp, SWT.CHECK);
		batchParaCompleteBtn.setText(CONSTANT_BATCHQA);
		
		autoParaCompleteBtn = new Button(instalCmp, SWT.CHECK);
		autoParaCompleteBtn.setText(CONSTANT_AUTOQA);
		
		levelCmp = new Composite(instalCmp, SWT.NONE);
		levelCmp.setLayout(lCmpLayout);
		new Label(levelCmp, SWT.NONE).setText(CONSTANT_TIPLEVEL);
		paraCompleteCmb = new TableComboViewer(levelCmp, SWT.READ_ONLY|SWT.BORDER);	
		createTableCombo(paraCompleteCmb);
		
		separatorLbl = new Label(instalCmp, SWT.SEPARATOR | SWT.HORIZONTAL);
		separatorLbl.setLayoutData(separatorLblData);
		
		
		//目标文本段长度限制检查 
		Label tgtLengthLimitLbl = new Label(instalCmp, SWT.NONE);
		tgtLengthLimitLbl.setText(qaItemId_Name_Class.get(QAConstant.QA_TGTTEXTLENGTHLIMIT).get(QAConstant.QA_ITEM_NAME));
		
		batchTgtLengthLimitBtn = new Button(instalCmp, SWT.CHECK);
		batchTgtLengthLimitBtn.setText(CONSTANT_BATCHQA);
		
		autoTgtLengthLimitBtn = new Button(instalCmp, SWT.CHECK);
		autoTgtLengthLimitBtn.setText(CONSTANT_AUTOQA);
		
		levelCmp = new Composite(instalCmp, SWT.NONE);
		levelCmp.setLayout(lCmpLayout);
		new Label(levelCmp, SWT.NONE).setText(CONSTANT_TIPLEVEL);
		tgtLengthLimitCmb = new TableComboViewer(levelCmp, SWT.READ_ONLY|SWT.BORDER);	
		createTableCombo(tgtLengthLimitCmb);
		
		separatorLbl = new Label(instalCmp, SWT.SEPARATOR | SWT.HORIZONTAL);
		separatorLbl.setLayoutData(separatorLblData);
		
		
		//拼写检查
		Label spellLbl = new Label(instalCmp, SWT.NONE);
		spellLbl.setText(qaItemId_Name_Class.get(QAConstant.QA_SPELL).get(QAConstant.QA_ITEM_NAME));
		
		batchSpellBtn = new Button(instalCmp, SWT.CHECK);
		batchSpellBtn.setText(CONSTANT_BATCHQA);
		
		autoSpellBtn = new Button(instalCmp, SWT.CHECK);
		autoSpellBtn.setText(CONSTANT_AUTOQA);
		
		levelCmp = new Composite(instalCmp, SWT.NONE);
		levelCmp.setLayout(lCmpLayout);
		new Label(levelCmp, SWT.NONE).setText(CONSTANT_TIPLEVEL);
		spellCmb = new TableComboViewer(levelCmp, SWT.READ_ONLY|SWT.BORDER);	
		createTableCombo(spellCmb);
		
		separatorLbl = new Label(instalCmp, SWT.SEPARATOR | SWT.HORIZONTAL);
		separatorLbl.setLayoutData(separatorLblData);
		
		// 文本段一致性检查
		Label paraLbl = new Label(instalCmp, SWT.NONE);
		paraLbl.setText(qaItemId_Name_Class.get(QAConstant.QA_PARAGRAPH).get(QAConstant.QA_ITEM_NAME));
		
		batchParaBtn = new Button(instalCmp, SWT.CHECK);
		batchParaBtn.setText(CONSTANT_BATCHQA);
		
		new Label(instalCmp, SWT.NONE);
		
		levelCmp = new Composite(instalCmp, SWT.NONE);
		levelCmp.setLayout(lCmpLayout);
		new Label(levelCmp, SWT.NONE).setText(CONSTANT_TIPLEVEL);
		paraCmb = new TableComboViewer(levelCmp, SWT.READ_ONLY|SWT.BORDER);	
		createTableCombo(paraCmb);
		
		separatorLbl = new Label(instalCmp, SWT.SEPARATOR | SWT.HORIZONTAL);
		separatorLbl.setLayoutData(separatorLblData);
		
		itemInstalLbl.computeSize();
		// ---------------------------------自动检查策略设置
		Group autoIntalGroup = new Group(tparent, SWT.NONE);
		autoIntalGroup.setLayout(new GridLayout());
		autoIntalGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		autoIntalGroup.setText(Messages.getString("qa.preference.QAInstalPage.autoQaInstal"));

		// 入库时执行
		whenAddToDbBtn = new Button(autoIntalGroup, SWT.CHECK);
		whenAddToDbBtn.setText(Messages.getString("qa.preference.QAInstalPage.autoQaWhenAddToDb"));
		whenAddToDbBtn.addListener(SWT.Selection, this);
		
		// 批准文本段后执行
		whenApprovalBtn = new Button(autoIntalGroup, SWT.CHECK);
		whenApprovalBtn.setText(Messages.getString("qa.preference.QAInstalPage.autoQaWhenApproval"));
		whenApprovalBtn.addListener(SWT.Selection, this);
		
		initValue();
		return parent;
	}
	
	public void handleEvent(Event event) {
		Widget widge = event.widget;
		if (widge.equals(whenAddToDbBtn) || widge.equals(whenApprovalBtn)) {
			setAutoItemSelected();
		}
	}

	/**
	 * 初始化值
	 */
	public void initValue() {
		// 默认情况下所有的品质检查项全部选中，在QAInitializer类中初始化

		String batchItemsValue = preferenceStore.getString(QAConstant.QA_PREF_BATCH_QAITEMS);
		List<String> batchItemsValList = new ArrayList<String>();
		String[] batchItemsValArray = batchItemsValue.split(",");
		for (int index = 0; index < batchItemsValArray.length; index++) {
			batchItemsValList.add(batchItemsValArray[index]);
		}
		
		batchTermBtn.setSelection(batchItemsValList.indexOf(QAConstant.QA_TERM) >= 0);
		batchParaBtn.setSelection(batchItemsValList.indexOf(QAConstant.QA_PARAGRAPH) >= 0);
		batchNumberBtn.setSelection(batchItemsValList.indexOf(QAConstant.QA_NUMBER) >= 0);
		batchTagBtn.setSelection(batchItemsValList.indexOf(QAConstant.QA_TAG) >= 0);
		batchNonTransBtn.setSelection(batchItemsValList.indexOf(QAConstant.QA_NONTRANSLATION) >= 0);
		//段首段末空格检查
		batchSpaceOfParaBtn.setSelection(batchItemsValList.indexOf(QAConstant.QA_SPACEOFPARACHECK) >= 0);
		//文本段完整性检查
		batchParaCompleteBtn.setSelection(batchItemsValList.indexOf(QAConstant.QA_PARACOMPLETENESS) >= 0);
		batchTgtLengthLimitBtn.setSelection(batchItemsValList.indexOf(QAConstant.QA_TGTTEXTLENGTHLIMIT) >= 0);
		batchSpellBtn.setSelection(batchItemsValList.indexOf(QAConstant.QA_SPELL) >= 0);
		
		//下面是自动检查的值的初始化情况
		String autoItemsValue = preferenceStore.getString(QAConstant.QA_PREF_AUTO_QAITEMS);
		List<String> autoItemsValList = new ArrayList<String>();
		String[] autoItemsValArray = autoItemsValue.split(",");
		for (int index = 0; index < autoItemsValArray.length; index++) {
			autoItemsValList.add(autoItemsValArray[index]);
		}
		
		autoTermBtn.setSelection(autoItemsValList.indexOf(QAConstant.QA_TERM) >= 0);
		//autoParaBtn.setSelection(autoItemsValList.indexOf(QAConstant.QA_PARAGRAPH) >= 0);
		autoNumberBtn.setSelection(autoItemsValList.indexOf(QAConstant.QA_NUMBER) >= 0);
		autoTagBtn.setSelection(autoItemsValList.indexOf(QAConstant.QA_TAG) >= 0);
		autoNonTransBtn.setSelection(autoItemsValList.indexOf(QAConstant.QA_NONTRANSLATION) >= 0);
		//段首段末空格检查
		autoSpaceOfParaBtn.setSelection(autoItemsValList.indexOf(QAConstant.QA_SPACEOFPARACHECK) >= 0);
		//文本段完整性检查
		autoParaCompleteBtn.setSelection(autoItemsValList.indexOf(QAConstant.QA_PARACOMPLETENESS) >= 0);
		autoTgtLengthLimitBtn.setSelection(autoItemsValList.indexOf(QAConstant.QA_TGTTEXTLENGTHLIMIT) >= 0);
		autoSpellBtn.setSelection(autoItemsValList.indexOf(QAConstant.QA_SPELL) >= 0);

		// 下面是设置提示级别
		termCmb.getTableCombo().select(preferenceStore.getInt(QAConstant.QA_PREF_term_TIPLEVEL));
		paraCmb.getTableCombo().select(preferenceStore.getInt(QAConstant.QA_PREF_para_TIPLEVEL));
		numberCmb.getTableCombo().select(preferenceStore.getInt(QAConstant.QA_PREF_number_TIPLEVEL));
		tagCmb.getTableCombo().select(preferenceStore.getInt(QAConstant.QA_PREF_tag_TIPLEVEL));
		nonTransCmb.getTableCombo().select((preferenceStore.getInt(QAConstant.QA_PREF_nonTrans_TIPLEVEL)));
		spaceOfParaCmb.getTableCombo().select(preferenceStore.getInt(QAConstant.QA_PREF_spaceOfPara_TIPLEVEL));
		paraCompleteCmb.getTableCombo().select(preferenceStore.getInt(QAConstant.QA_PREF_paraComplete_TIPLEVEL));
		tgtLengthLimitCmb.getTableCombo().select(preferenceStore.getInt(QAConstant.QA_PREF_tgtLengthLimit_TIPLEVEL));
		spellCmb.getTableCombo().select(preferenceStore.getInt(QAConstant.QA_PREF_spell_TIPLEVEL));
		
		//等于0时：为从不执行;等于1时，为入库时执行;等于2时，为批准文本段时执行;等于3时，为都进行检查。
		int autoTag = preferenceStore.getInt(QAConstant.QA_PREF_AUTO_QARUNTIME);
		whenAddToDbBtn.setSelection(autoTag == QAConstant.QA_FIRST || autoTag == QAConstant.QA_THREE);
		whenApprovalBtn.setSelection(autoTag == QAConstant.QA_TWO || autoTag == QAConstant.QA_THREE);
		
		setAutoItemSelected();
		
	}

	@Override
	protected void performDefaults() {
		// 默认情况下所有的品质检查项全部选中，在QAInitializer类中初始化

		String batchItemsValue = preferenceStore.getDefaultString(QAConstant.QA_PREF_BATCH_QAITEMS);
		List<String> batchItemsValList = new ArrayList<String>();
		String[] batchItemsValArray = batchItemsValue.split(",");
		for (int index = 0; index < batchItemsValArray.length; index++) {
			batchItemsValList.add(batchItemsValArray[index]);
		}
		
		batchTermBtn.setSelection(batchItemsValList.indexOf(QAConstant.QA_TERM) >= 0);
		batchParaBtn.setSelection(batchItemsValList.indexOf(QAConstant.QA_PARAGRAPH) >= 0);
		batchNumberBtn.setSelection(batchItemsValList.indexOf(QAConstant.QA_NUMBER) >= 0);
		batchTagBtn.setSelection(batchItemsValList.indexOf(QAConstant.QA_TAG) >= 0);
		batchNonTransBtn.setSelection(batchItemsValList.indexOf(QAConstant.QA_NONTRANSLATION) >= 0);
		//段首段末空格检查
		batchSpaceOfParaBtn.setSelection(batchItemsValList.indexOf(QAConstant.QA_SPACEOFPARACHECK) >= 0);
		//文本段完整性检查
		batchParaCompleteBtn.setSelection(batchItemsValList.indexOf(QAConstant.QA_PARACOMPLETENESS) >= 0);
		batchTgtLengthLimitBtn.setSelection(batchItemsValList.indexOf(QAConstant.QA_TGTTEXTLENGTHLIMIT) >= 0);
		batchSpellBtn.setSelection(batchItemsValList.indexOf(QAConstant.QA_SPELL) >= 0);
		
		//下面是自动检查的值的初始化情况
		String autoItemsValue = preferenceStore.getDefaultString(QAConstant.QA_PREF_AUTO_QAITEMS);
		List<String> autoItemsValList = new ArrayList<String>();
		String[] autoItemsValArray = autoItemsValue.split(",");
		for (int index = 0; index < autoItemsValArray.length; index++) {
			autoItemsValList.add(autoItemsValArray[index]);
		}
		
		autoTermBtn.setSelection(autoItemsValList.indexOf(QAConstant.QA_TERM) >= 0);
		//autoParaBtn.setSelection(autoItemsValList.indexOf(QAConstant.QA_PARAGRAPH) >= 0);
		autoNumberBtn.setSelection(autoItemsValList.indexOf(QAConstant.QA_NUMBER) >= 0);
		autoTagBtn.setSelection(autoItemsValList.indexOf(QAConstant.QA_TAG) >= 0);
		autoNonTransBtn.setSelection(autoItemsValList.indexOf(QAConstant.QA_NONTRANSLATION) >= 0);
		//段首段末空格检查
		autoSpaceOfParaBtn.setSelection(autoItemsValList.indexOf(QAConstant.QA_SPACEOFPARACHECK) >= 0);
		//文本段完整性检查
		autoParaCompleteBtn.setSelection(autoItemsValList.indexOf(QAConstant.QA_PARACOMPLETENESS) >= 0);
		autoTgtLengthLimitBtn.setSelection(autoItemsValList.indexOf(QAConstant.QA_TGTTEXTLENGTHLIMIT) >= 0);
		autoSpellBtn.setSelection(autoItemsValList.indexOf(QAConstant.QA_SPELL) >= 0);
		
		// 下面是设置提示级别
		termCmb.getTableCombo().select(preferenceStore.getDefaultInt(QAConstant.QA_PREF_term_TIPLEVEL));
		paraCmb.getTableCombo().select(preferenceStore.getDefaultInt(QAConstant.QA_PREF_para_TIPLEVEL));
		numberCmb.getTableCombo().select(preferenceStore.getDefaultInt(QAConstant.QA_PREF_number_TIPLEVEL));
		tagCmb.getTableCombo().select(preferenceStore.getDefaultInt(QAConstant.QA_PREF_tag_TIPLEVEL));
		nonTransCmb.getTableCombo().select(preferenceStore.getDefaultInt(QAConstant.QA_PREF_nonTrans_TIPLEVEL));
		spaceOfParaCmb.getTableCombo().select(preferenceStore.getDefaultInt(QAConstant.QA_PREF_spaceOfPara_TIPLEVEL));
		paraCompleteCmb.getTableCombo().select(preferenceStore.getDefaultInt(QAConstant.QA_PREF_paraComplete_TIPLEVEL));
		tgtLengthLimitCmb.getTableCombo().select(preferenceStore.getDefaultInt(QAConstant.QA_PREF_tgtLengthLimit_TIPLEVEL));
		spellCmb.getTableCombo().select(preferenceStore.getDefaultInt(QAConstant.QA_PREF_spell_TIPLEVEL));
		
		//等于0时：为从不执行;等于1时，为入库时执行;等于2时，为批准文本段时执行;等于3时，为都进行检查。
		int autoTag = preferenceStore.getDefaultInt(QAConstant.QA_PREF_AUTO_QARUNTIME);
		whenAddToDbBtn.setSelection(autoTag == QAConstant.QA_FIRST || autoTag == QAConstant.QA_THREE);
		whenApprovalBtn.setSelection(autoTag == QAConstant.QA_TWO || autoTag == QAConstant.QA_THREE);
		
		setAutoItemSelected();
	}

	@Override
	public boolean performOk() {
		if (!isInit) {
			return true;
		}
		
		//先处理批量检查的情况
		String batchItemsValue = "";
		if (batchTermBtn.getSelection()) {
			batchItemsValue += QAConstant.QA_TERM + ",";
		}
		if (batchParaBtn.getSelection()) {
			batchItemsValue += QAConstant.QA_PARAGRAPH + ",";
		}
		if (batchNumberBtn.getSelection()) {
			batchItemsValue += QAConstant.QA_NUMBER + ",";
		}
		if (batchTagBtn.getSelection()) {
			batchItemsValue += QAConstant.QA_TAG + ",";
		}
		if (batchNonTransBtn.getSelection()) {
			batchItemsValue += QAConstant.QA_NONTRANSLATION + ",";
		}
		if (batchSpaceOfParaBtn.getSelection()) {
			batchItemsValue += QAConstant.QA_SPACEOFPARACHECK + ",";
		}
		if (batchParaCompleteBtn.getSelection()) {
			batchItemsValue += QAConstant.QA_PARACOMPLETENESS + ",";
		}
		if (batchTgtLengthLimitBtn.getSelection()) {
			batchItemsValue += QAConstant.QA_TGTTEXTLENGTHLIMIT + ",";
		}
		if (batchSpellBtn.getSelection()) {
			batchItemsValue += QAConstant.QA_SPELL ;
		}
		
		//处理自动检查的情况
		String autoItemsValue = "";
		if (autoTermBtn.getSelection()) {
			autoItemsValue += QAConstant.QA_TERM + ",";
		}
		/*if (autoParaBtn.getSelection()) {
			autoItemsValue += QAConstant.QA_PARAGRAPH + ",";
		}*/
		if (autoNumberBtn.getSelection()) {
			autoItemsValue += QAConstant.QA_NUMBER + ",";
		}
		if (autoTagBtn.getSelection()) {
			autoItemsValue += QAConstant.QA_TAG + ",";
		}
		if (autoNonTransBtn.getSelection()) {
			autoItemsValue += QAConstant.QA_NONTRANSLATION + ",";
		}
		if (autoSpaceOfParaBtn.getSelection()) {
			autoItemsValue += QAConstant.QA_SPACEOFPARACHECK + ",";
		}
		if (autoParaCompleteBtn.getSelection()) {
			autoItemsValue += QAConstant.QA_PARACOMPLETENESS + ",";
		}
		if (autoTgtLengthLimitBtn.getSelection()) {
			autoItemsValue += QAConstant.QA_TGTTEXTLENGTHLIMIT + ",";
		}
		if (autoSpellBtn.getSelection()) {
			autoItemsValue += QAConstant.QA_SPELL ;
		}
		
		//等于0时：为从不执行;等于1时，为入库时执行;等于2时，为批准文本段时执行;等于3时，为都执行
		int runtime = 0;
		if (whenAddToDbBtn.getSelection() && whenApprovalBtn.getSelection()) {
			runtime = 3;
		}else if (whenAddToDbBtn.getSelection()) {
			runtime = 1;
		}else if (whenApprovalBtn.getSelection()) {
			runtime = 2;
		}
		
		// 储存品质检查项
		preferenceStore.setValue(QAConstant.QA_PREF_BATCH_QAITEMS, batchItemsValue);
		preferenceStore.setValue(QAConstant.QA_PREF_AUTO_QAITEMS, autoItemsValue);
		preferenceStore.setValue(QAConstant.QA_PREF_AUTO_QARUNTIME, runtime);
		
		// 下面是设置提示级别
		preferenceStore.setValue(QAConstant.QA_PREF_term_TIPLEVEL, termCmb.getTableCombo().getSelectionIndex());
		preferenceStore.setValue(QAConstant.QA_PREF_para_TIPLEVEL, paraCmb.getTableCombo().getSelectionIndex());
		preferenceStore.setValue(QAConstant.QA_PREF_number_TIPLEVEL, numberCmb.getTableCombo().getSelectionIndex());
		preferenceStore.setValue(QAConstant.QA_PREF_tag_TIPLEVEL, tagCmb.getTableCombo().getSelectionIndex());
		preferenceStore.setValue(QAConstant.QA_PREF_nonTrans_TIPLEVEL, nonTransCmb.getTableCombo().getSelectionIndex());
		preferenceStore.setValue(QAConstant.QA_PREF_spaceOfPara_TIPLEVEL, spaceOfParaCmb.getTableCombo().getSelectionIndex());
		preferenceStore.setValue(QAConstant.QA_PREF_paraComplete_TIPLEVEL, paraCompleteCmb.getTableCombo().getSelectionIndex());
		preferenceStore.setValue(QAConstant.QA_PREF_tgtLengthLimit_TIPLEVEL, tgtLengthLimitCmb.getTableCombo().getSelectionIndex());
		preferenceStore.setValue(QAConstant.QA_PREF_spell_TIPLEVEL, spellCmb.getTableCombo().getSelectionIndex());
		
		return true;
	}
	
	public static void main(String[] args) {
		
	}
	
	class QATipsLabelProvider extends LabelProvider implements ITableLabelProvider {
		private Map<String, Image> imageCache = new HashMap<String, Image>();
		public QATipsLabelProvider() {
			
		}
		public Image getColumnImage(Object element, int columnIndex) {
			if (element instanceof String) {
				String tip = (String) element;
				return errorTip.equals(tip) ? errorImg : warnImg;
			}
			return null;
			
		}
		public String getColumnText(Object element, int columnIndex) {
			if (element instanceof String) {
				String array = (String) element;
				return array;
			}
			return null;
		}

		public void dispose(){
			for (String code : imageCache.keySet()) {
				Image im = imageCache.get(code);
				if (im != null && !im.isDisposed()) {
					im.dispose();
				}
			}
			imageCache.clear();
			super.dispose();
		}
	}
	
	private void createTableCombo(TableComboViewer tCmbViewer){
		TableCombo tableCombo = tCmbViewer.getTableCombo();
		tableCombo.setShowTableLines(false);
		tableCombo.setShowTableHeader(false);
		tableCombo.setDisplayColumnIndex(-1);
		tableCombo.setShowImageWithinSelection(true);
		tableCombo.setShowColorWithinSelection(false);
		tableCombo.setShowFontWithinSelection(false);
		tableCombo.setVisibleItemCount(2);	
		GridDataFactory.swtDefaults().hint(100, SWT.DEFAULT).applyTo(tableCombo);
		
		tCmbViewer.setLabelProvider(new QATipsLabelProvider());
		tCmbViewer.setContentProvider(new ArrayContentProvider());
		tCmbViewer.setInput(CONSTANT_COMBOVALUE);
	}
	
	private void setAutoItemSelected(){
		if (!whenApprovalBtn.getSelection() && !whenAddToDbBtn.getSelection()) {
			setAutoItemEnable(false);
		}else {
			setAutoItemEnable(true);
		}
	}
	
	private void setAutoItemEnable(boolean enable){
		autoTermBtn.setEnabled(enable);
//		autoParaBtn.setEnabled(enable);
		autoNumberBtn.setEnabled(enable);
		autoTagBtn.setEnabled(enable);
		autoNonTransBtn.setEnabled(enable);
		autoSpaceOfParaBtn.setEnabled(enable);
		autoParaCompleteBtn.setEnabled(enable);
		autoTgtLengthLimitBtn.setEnabled(enable);
		autoSpellBtn.setEnabled(enable);
	}


}

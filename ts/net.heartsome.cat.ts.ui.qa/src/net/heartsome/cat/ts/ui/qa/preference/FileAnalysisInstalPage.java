package net.heartsome.cat.ts.ui.qa.preference;

import net.heartsome.cat.common.ui.HsImageLabel;
import net.heartsome.cat.common.util.CommonFunction;
import net.heartsome.cat.ts.core.qa.QAConstant;
import net.heartsome.cat.ts.ui.qa.Activator;
import net.heartsome.cat.ts.ui.qa.resource.Messages;
import net.heartsome.cat.ts.ui.resource.ImageConstant;

import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * 文件分析的参数设置，针对于字数分析
 * @author robert 2012-05-04
 * @version
 * @since JDK1.6
 */
public class FileAnalysisInstalPage extends PreferencePage implements IWorkbenchPreferencePage {
	public static final String ID = "net.heartsome.cat.ts.ui.qa.preference.FileAnalysisInstalPage";
	private IPreferenceStore preferenceStore;
	/** 是否忽略大小写 */
	private Button ignoreCaseBtn;
	/** 是否忽略标记 */
	private Button ignoreTagBtn;
	/** 上下文个数 */
	private Spinner contextSpinner;
	
	private Label tagPenaltyLbl;
	private Spinner tagPenaltySpi;
	
	/** 是否进行内部重复 */
	private Button interRepateBtn;
	/** 是否进行内部匹配 */
	private Button interMatchBtn;
	
	private boolean isInit = false;

	public void init(IWorkbench workbench) {

	}

	public FileAnalysisInstalPage() {
		setTitle(Messages.getString("preference.FileAnalysisInstalPage.title"));
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		preferenceStore = getPreferenceStore();
	}

	@Override
	protected Control createContents(Composite parent) {
		isInit = true;
		Composite tparent = new Composite(parent, SWT.NONE);
		tparent.setLayout(new GridLayout());
		tparent.setLayoutData(new GridData(GridData.FILL_BOTH));

		Group faGroup = new Group(tparent, SWT.NONE);
		faGroup.setLayout(new GridLayout());
		faGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		faGroup.setText(Messages.getString("preference.FileAnalysisInstalPage.faGroup"));

		String tip = Messages.getString("preference.FileAnalysisInstalPage.faImgLbl");
		HsImageLabel faImgLbl = new HsImageLabel(tip,
				Activator.getImageDescriptor(ImageConstant.PREFERENCE_FA_analysis));
		Composite faLblCmp = faImgLbl.createControl(faGroup);
		faLblCmp.setLayout(new GridLayout());
		faLblCmp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		ignoreCaseBtn = new Button(faLblCmp, SWT.CHECK);
		ignoreCaseBtn.setText(Messages.getString("preference.FileAnalysisInstalPage.ignoreCaseBtn"));

		
		ignoreTagBtn = new Button(faLblCmp, SWT.CHECK);
		ignoreTagBtn.setText(Messages.getString("preference.FileAnalysisInstalPage.ignoreTagBtn"));
		ignoreTagBtn.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				setTagPenaltyEnable();
			}
			public void widgetDefaultSelected(SelectionEvent e) {
				setTagPenaltyEnable();
			}
		});
		
		GridData spinnerData = new GridData(30, SWT.DEFAULT);
		
		
		// 罚分制度
		Composite tagPenaltyCmp = new Composite(faLblCmp, SWT.NONE);
		GridLayoutFactory.fillDefaults().extendedMargins(0, 0, 0, 0).numColumns(3).applyTo(tagPenaltyCmp);
		tagPenaltyCmp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		tagPenaltyLbl = new Label(tagPenaltyCmp, SWT.NONE);
		tagPenaltyLbl.setText(Messages.getString("preference.FileAnalysisInstalPage.tagPenalty"));
		
		tagPenaltySpi = new Spinner(tagPenaltyCmp, SWT.BORDER);
		tagPenaltySpi.setLayoutData(spinnerData);
		GridLayout pGl = new GridLayout(2, false);
		pGl.marginHeight = 0;
		pGl.marginWidth = 0;
		tagPenaltySpi.setMinimum(1);
		tagPenaltySpi.setMaximum(100);
		

		if (CommonFunction.checkEdition("U")) {
			// 上下文个数
			Composite contextCmp = new Composite(faLblCmp, SWT.NONE);
			GridLayoutFactory.fillDefaults().extendedMargins(0, 0, 0, 0).numColumns(3).applyTo(contextCmp);
			contextCmp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

			Label contextLbl = new Label(contextCmp, SWT.NONE);
			contextLbl.setText(Messages.getString("preference.FileAnalysisInstalPage.contextLbl"));

			contextSpinner = new Spinner(contextCmp, SWT.BORDER);
			contextSpinner.setLayoutData(spinnerData);
			contextSpinner.setMinimum(1);
			contextSpinner.setMaximum(100);
			contextSpinner.setIncrement(1); // 步值为1
			contextSpinner.setSelection(1);
			Label unitLbl = new Label(contextCmp, SWT.NONE);
			unitLbl.setText(Messages.getString("preference.FileAnalysisInstalPage.unitLbl"));
		}
		faImgLbl.computeSize();
		

		// 分析文件设置
		Group faFileGroup = new Group(tparent, SWT.NONE);
		faFileGroup.setLayout(new GridLayout());
		faFileGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		faFileGroup.setText(Messages.getString("preference.FileAnalysisInstalPage.faFileGroup"));

		String faFileTip = Messages.getString("preference.FileAnalysisInstalPage.faFileTip");
		HsImageLabel faFileImgLbl = new HsImageLabel(faFileTip,
				Activator.getImageDescriptor(ImageConstant.PREFERENCE_FA_fileAnalysis));
		
		Composite faFileLblCmp = faFileImgLbl.createControl(faFileGroup);
		faFileLblCmp.setLayout(new GridLayout());
		faFileLblCmp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		interRepateBtn = new Button(faFileLblCmp, SWT.CHECK);
		interRepateBtn.setText(Messages.getString("preference.FileAnalysisInstalPage.faFileInterRepeate"));
		
		interMatchBtn = new Button(faFileLblCmp, SWT.CHECK);
		interMatchBtn.setText(Messages.getString("preference.FileAnalysisInstalPage.faFileInterMatch"));
		
		faFileImgLbl.computeSize();
		initListener();
		initValue();
		return parent;
	}
	
	private void initListener(){
		interRepateBtn.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent arg0) {
				if (interRepateBtn.getSelection()) {
					interMatchBtn.setEnabled(true);
				}else {
					interMatchBtn.setSelection(false);
					interMatchBtn.setEnabled(false);
				}
			}
			public void widgetDefaultSelected(SelectionEvent arg0) {
				if (interRepateBtn.getSelection()) {
					interMatchBtn.setEnabled(true);
				}else {
					interMatchBtn.setSelection(false);
					interMatchBtn.setEnabled(false);
				}
			}
		});
	}

	private void initValue() {
		ignoreCaseBtn.setSelection(preferenceStore.getBoolean(QAConstant.FA_PREF_ignoreCase));
		ignoreTagBtn.setSelection(preferenceStore.getBoolean(QAConstant.FA_PREF_ignoreTag));
		if (CommonFunction.checkEdition("U")) {
			contextSpinner.setSelection(preferenceStore.getInt(QAConstant.FA_PREF_contextNum));
		}
		tagPenaltySpi.setSelection(preferenceStore.getInt(QAConstant.FA_PREF_tagPenalty));

		interRepateBtn.setSelection(preferenceStore.getBoolean(QAConstant.FA_PREF_interRepeate));
		interMatchBtn.setSelection(preferenceStore.getBoolean(QAConstant.FA_PREF_interMatch));
		if (interRepateBtn.getSelection()) {
			interMatchBtn.setEnabled(true);
		}else {
			interMatchBtn.setSelection(false);
			interMatchBtn.setEnabled(false);
		}
		
		setTagPenaltyEnable();
	}
	
	private void setTagPenaltyEnable(){
		boolean ignoreTag = ignoreTagBtn.getSelection();
		tagPenaltyLbl.setEnabled(!ignoreTag);
		tagPenaltySpi.setEnabled(!ignoreTag);
	}

	@Override
	protected void performDefaults() {
		ignoreCaseBtn.setSelection(preferenceStore.getDefaultBoolean(QAConstant.FA_PREF_ignoreCase));
		ignoreTagBtn.setSelection(preferenceStore.getDefaultBoolean(QAConstant.FA_PREF_ignoreTag));
		if (CommonFunction.checkEdition("U")) {
			contextSpinner.setSelection(preferenceStore.getDefaultInt(QAConstant.FA_PREF_contextNum));
		}
		tagPenaltySpi.setSelection(preferenceStore.getDefaultInt(QAConstant.FA_PREF_tagPenalty));
		
		interRepateBtn.setSelection(preferenceStore.getDefaultBoolean(QAConstant.FA_PREF_interRepeate));
		interMatchBtn.setSelection(preferenceStore.getDefaultBoolean(QAConstant.FA_PREF_interMatch));
		setTagPenaltyEnable();
	}

	@Override
	public boolean performOk() {
		if (!isInit) {
			return true;
		}
		preferenceStore.setValue(QAConstant.FA_PREF_ignoreCase, ignoreCaseBtn.getSelection());
		preferenceStore.setValue(QAConstant.FA_PREF_ignoreTag, ignoreTagBtn.getSelection());
		if (CommonFunction.checkEdition("U")) {
			preferenceStore.setValue(QAConstant.FA_PREF_contextNum, contextSpinner.getSelection());
		}
		preferenceStore.setValue(QAConstant.FA_PREF_tagPenalty, tagPenaltySpi.getSelection());

		preferenceStore.setValue(QAConstant.FA_PREF_interRepeate, interRepateBtn.getSelection());
		preferenceStore.setValue(QAConstant.FA_PREF_interMatch, interMatchBtn.getSelection());
		return true;
	}
}

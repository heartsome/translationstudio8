package net.heartsome.cat.ts.ui.wizards;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import net.heartsome.cat.common.locale.Language;
import net.heartsome.cat.common.locale.LocaleService;
import net.heartsome.cat.ts.ui.Activator;
import net.heartsome.cat.ts.ui.composite.LanguageLabelProvider;
import net.heartsome.cat.ts.ui.extensionpoint.AbstractNewProjectWizardPage;
import net.heartsome.cat.ts.ui.preferencepage.IPreferenceConstants;
import net.heartsome.cat.ts.ui.resource.Messages;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.nebula.jface.tablecomboviewer.TableComboViewer;
import org.eclipse.nebula.widgets.tablecombo.TableCombo;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Listener;

public class NewProjectWizardLanguagePage extends WizardPage {

	private List<Language> languages;
	private TableComboViewer srcLangComboViewer;
	private TargetLangSelect targetLangControl;

	private Validator validator;
	private Language srcLanguage;

	private IPreferenceStore ps = Activator.getDefault().getPreferenceStore();

	/**
	 * Create the wizard.
	 */
	public NewProjectWizardLanguagePage() {
		super("wizardPage");
		setTitle(Messages.getString("wizard.NewProjectWizardLanguagePage.title"));
		setDescription(Messages.getString("wizard.NewProjectWizardLanguagePage.desc"));
		setImageDescriptor(Activator.getImageDescriptor("images/project/new-project-logo.png"));
		setPageComplete(false);

		// 获取语言列表
		languages = new ArrayList<Language>(LocaleService.getDefaultLanguage().values());
		Collections.sort(languages, new Comparator<Language>() {
			public int compare(Language o1, Language o2) {
				return o1.toString().compareTo(o2.toString());
			}
		});
		validator = new Validator();
		targetLangControl = new TargetLangSelect(languages, validator);
	}

	/**
	 * Create contents of the wizard.
	 * @param parent
	 */
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);
		container.setLayout(new GridLayout(1, false));

		// source language control
		Group sourceLanguageGrp = new Group(container, SWT.NONE);
		sourceLanguageGrp.setLayout(new GridLayout(1, false));
		sourceLanguageGrp.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		sourceLanguageGrp.setText(Messages.getString("wizard.NewProjectWizardLanguagePage.sourceLanguageGrp"));

		srcLangComboViewer = new TableComboViewer(sourceLanguageGrp, SWT.READ_ONLY | SWT.BORDER);
		TableCombo tableCombo = srcLangComboViewer.getTableCombo();
		// set options.
		tableCombo.setShowTableLines(false);
		tableCombo.setShowTableHeader(false);
		tableCombo.setDisplayColumnIndex(-1);
		tableCombo.setShowImageWithinSelection(true);
		tableCombo.setShowColorWithinSelection(false);
		tableCombo.setShowFontWithinSelection(false);
		tableCombo.setVisibleItemCount(20);
		srcLangComboViewer.getTableCombo().setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		srcLangComboViewer.setLabelProvider(new LanguageLabelProvider());
		srcLangComboViewer.setContentProvider(new ArrayContentProvider());
		srcLangComboViewer.setInput(languages);
		srcLangComboViewer.addSelectionChangedListener(new ISelectionChangedListener() {

			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection selection = (IStructuredSelection) event.getSelection();
				srcLanguage = (Language) selection.getFirstElement();
				validator.update();
			}
		});
		// initialization remember value
		String rmSrcLangCode = ps.getString(IPreferenceConstants.NEW_PROJECT_SRC_LANG);
		if (rmSrcLangCode != null && !rmSrcLangCode.equals("")) {
			for (Language srcLang : languages) {
				if (srcLang.getCode().equals(rmSrcLangCode)) {
					srcLangComboViewer.setSelection(new StructuredSelection(srcLang));
					break;
				}
			}
		}

		// end source language

		// target language control
		Group targetLanguageGrp = new Group(container, SWT.NONE);
		targetLanguageGrp.setLayout(new GridLayout(3, false));
		targetLanguageGrp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true, 1, 1));
		targetLanguageGrp.setText(Messages.getString("wizard.NewProjectWizardLanguagePage.targetLanguageGrp"));
		targetLangControl.createControl(targetLanguageGrp);
		// end Target language

		setControl(container);
		validator.update();
	}

	/**
	 * 获取源文言
	 * @return ;
	 */
	public Language getSrcLanguage() {
		return this.srcLanguage;
	}

	/**
	 * 获取目标语言
	 * @return 一组语言;
	 */
	public List<Language> getTargetlanguage() {
		List<Language> targetLangList = new ArrayList<Language>();
		List<Object> objList = targetLangControl.getHasSelectedList();
		for (int i = 0; i < objList.size(); i++) {
			targetLangList.add((Language) objList.get(i));
		}
		return targetLangList;
	}

	/**
	 * 处理扩展页面中需要获取项目源语言问题 (non-Javadoc)
	 * @see org.eclipse.jface.wizard.WizardPage#getNextPage()
	 */
	public IWizardPage getNextPage() {
		if (getWizard() == null) {
			return null;
		}
		IWizardPage[] pages = getWizard().getPages();
		for (int i = 0; i < pages.length; i++) {
			if (pages[i] instanceof AbstractNewProjectWizardPage) {
				AbstractNewProjectWizardPage extensoinPage = (AbstractNewProjectWizardPage) pages[i];
				extensoinPage.setProjSourceLang(getSrcLanguage());
			}
		}
		return getWizard().getNextPage(this);
	}

	/**
	 * 目标语言选择器
	 * @author Jason
	 * @version
	 * @since JDK1.6
	 */
	protected final class TargetLangSelect implements Listener {
		private TableViewer canSelectTableViewer;
		private TableViewer hasSelTableViewer;

		private Object canSelectInput;
		private List<Object> hasSelectedList;
		private Validator validator;

		private Button addBtn;
		private Button deleteBtn;
		private Button deleteAllBtn;

		/**
		 * 构造器，创建一个空白的无内容的组件
		 */
		public TargetLangSelect() {
			this(null, null);
		}

		/**
		 * 构造器，创建一个带有可选内容和验证器的组件
		 * @param input
		 *            可以选择的内容
		 * @param validator
		 *            验证器
		 */
		public TargetLangSelect(Object input, Validator validator) {
			this.hasSelectedList = new ArrayList<Object>();
			this.canSelectInput = input;
			this.validator = validator;
		}

		/**
		 * 创建组件
		 * @param comp
		 *            ;
		 */
		public void createControl(Composite comp) {
			Composite canSelectComp = new Composite(comp, SWT.NONE);
			canSelectComp.setLayout(new GridLayout(1, false));
			canSelectComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

			canSelectTableViewer = new TableViewer(canSelectComp, SWT.BORDER | SWT.V_SCROLL | SWT.MULTI
					| SWT.FULL_SELECTION);
			GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
			gridData.widthHint = 100;
			gridData.heightHint = 200;
			canSelectTableViewer.getTable().setLayoutData(gridData);

			canSelectTableViewer.setLabelProvider(new LanguageLabelProvider());
			canSelectTableViewer.setContentProvider(new ArrayContentProvider());
			canSelectTableViewer.setInput(canSelectInput);
			canSelectTableViewer.getTable().addListener(SWT.MouseDoubleClick, this);

			Composite selBtnComp = new Composite(comp, SWT.NONE);
			selBtnComp.setLayout(new GridLayout(1, false));
			selBtnComp.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false, 1, 1));

			addBtn = new Button(selBtnComp, SWT.NONE);
			addBtn.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			addBtn.setText(Messages.getString("wizard.NewProjectWizardLanguagePage.addBtn"));
			addBtn.addListener(SWT.Selection, this);

			deleteBtn = new Button(selBtnComp, SWT.NONE);
			deleteBtn.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			deleteBtn.setText(Messages.getString("wizard.NewProjectWizardLanguagePage.deleteBtn"));
			deleteBtn.addListener(SWT.Selection, this);

			deleteAllBtn = new Button(selBtnComp, SWT.NONE);
			deleteAllBtn.setText(Messages.getString("wizard.NewProjectWizardLanguagePage.deleteAllBtn"));
			deleteAllBtn.addListener(SWT.Selection, this);

			Composite hasSelComp = new Composite(comp, SWT.NONE);
			hasSelComp.setLayout(new GridLayout(1, false));
			hasSelComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));

			hasSelTableViewer = new TableViewer(hasSelComp, SWT.BORDER | SWT.V_SCROLL | SWT.MULTI | SWT.FULL_SELECTION);
			hasSelTableViewer.getTable().setLayoutData(gridData);
			hasSelTableViewer.setLabelProvider(new LanguageLabelProvider());
			hasSelTableViewer.setContentProvider(new ArrayContentProvider());
			hasSelTableViewer.getTable().addListener(SWT.MouseDoubleClick, this);

			// initialization remember target language
			String strTargetLang = ps.getString(IPreferenceConstants.NEW_PROJECT_TGT_LANG);
			if (strTargetLang != null && !strTargetLang.equals("")) {
				String[] langCodes = strTargetLang.split(",");
				List<Language> targetLangList = new ArrayList<Language>();
				for (String code : langCodes) {
					for (Language lang : languages) {
						if (code.equals(lang.getCode())) {
							targetLangList.add(lang);
							break;
						}
					}
				}
				execSelected(new StructuredSelection(targetLangList));
				fireChangeEvent();
			}
		}

		/**
		 * 当状态改变时，触发验证事件 (non-Javadoc)
		 * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
		 */
		public void handleEvent(Event event) {
			if (event.widget == canSelectTableViewer.getTable()) {
				execSelected((IStructuredSelection) canSelectTableViewer.getSelection());
			}
			if (event.widget == addBtn) {
				execSelected((IStructuredSelection) canSelectTableViewer.getSelection());
			}

			// Delete Language
			if (event.widget == hasSelTableViewer.getTable()) {
				IStructuredSelection selection = (IStructuredSelection) hasSelTableViewer.getSelection();
				removeSelected(selection);
			}
			if (event.widget == deleteBtn) {
				IStructuredSelection selection = (IStructuredSelection) hasSelTableViewer.getSelection();
				removeSelected(selection);
			}
			if (event.widget == deleteAllBtn) {
				hasSelTableViewer.getTable().removeAll();
				hasSelectedList.clear();
			}
			fireChangeEvent();
		}

		/**
		 * 当选择器的内容发生变化时，触发验证器 ;
		 */
		private void fireChangeEvent() {
			validator.update();
		}

		/**
		 * 选择选重的项
		 * @param selection
		 *            ;
		 */
		private void execSelected(IStructuredSelection selection) {
			Iterator<?> it = selection.iterator();
			while (it.hasNext()) {
				Object obj = it.next();
				if (!hasSelectedList.contains(obj)) {
					hasSelectedList.add(obj);
					hasSelTableViewer.add(obj);
				}
			}
			hasSelTableViewer.setSelection(selection);
		}

		private void removeSelected(IStructuredSelection selection) {
			hasSelectedList.removeAll(selection.toList());
			hasSelTableViewer.remove(selection.toArray());
		}

		/** @return the canSelectInput */
		public Object getCanSelectInput() {
			return canSelectInput;
		}

		/** @return the hasSelectedList */
		public List<Object> getHasSelectedList() {
			return hasSelectedList;
		}

		/** @return the validator */
		public Validator getValidator() {
			return validator;
		}

		/** @return the canSelectlistViewer */
		public TableViewer getCanSelectTableViewer() {
			return canSelectTableViewer;
		}

		/** @return the hasSelListViewer */
		public TableViewer getHasSelTableViewer() {
			return hasSelTableViewer;
		}

	}

	/** 验证器。验证界面输入 **/
	private final class Validator {

		public void update() {

			if (getSrcLanguage() == null) {
				setErrorMessage(Messages.getString("wizard.NewProjectWizardLanguagePage.msg1"));
				setPageComplete(false);
				return;
			}

			if (getTargetlanguage() == null || getTargetlanguage().size() < 1) {
				setErrorMessage(Messages.getString("wizard.NewProjectWizardLanguagePage.msg2"));
				setPageComplete(false);
				return;
			}

			String srcCode = getSrcLanguage().getCode();
			List<Language> target = getTargetlanguage();
			for (Language tLang : target) {
				String tCode = tLang.getCode();
				if (tCode.equals(srcCode)) {
					setErrorMessage(Messages.getString("wizard.NewProjectWizardLanguagePage.msg3"));
					setPageComplete(false);
					return;
				}
			}

			setPageComplete(true);
			setErrorMessage(null);
			setMessage(null);
		}
	}

}

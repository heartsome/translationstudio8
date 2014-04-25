/**
 * ProjectConfigLanguagePage.java
 *
 * Version information :
 *
 * Date:Nov 28, 2011
 *
 * Copyright notice :
 * 本文件及其附带的相关文件包含机密信息，仅限瀚特盛科技有限公司指定的，与本项目有关的内部人员和客户联络人员查阅和使用。 
 * 如果您不是本保密声明中指定的收件者，请立即销毁本文件，禁止对本文件或根据本文件中的内容采取任何其他行动， 
 * 包括
 */
package net.heartsome.cat.ts.ui.projectsetting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import net.heartsome.cat.common.bean.ProjectInfoBean;
import net.heartsome.cat.common.locale.Language;
import net.heartsome.cat.common.locale.LocaleService;
import net.heartsome.cat.ts.ui.composite.LanguageLabelProvider;
import net.heartsome.cat.ts.ui.resource.Messages;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.jface.databinding.viewers.ViewersObservables;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.IElementComparer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.nebula.jface.tablecomboviewer.TableComboViewer;
import org.eclipse.nebula.widgets.tablecombo.TableCombo;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Listener;

/**
 * @author Jason
 * @version
 * @since JDK1.6
 */
public class ProjectSettingLanguagePage extends PreferencePage {

	private List<Language> languages;
	private TableComboViewer srcLangComboViewer;
	private TargetLangSelect targetLangControl;

	private ProjectInfoBean projCfgBean;

	/**
	 * Create the preference page.
	 */
	public ProjectSettingLanguagePage(ProjectInfoBean bean) {
		setTitle(Messages.getString("projectsetting.ProjectSettingLanguagePage.title"));
		noDefaultAndApplyButton();

		this.projCfgBean = bean;

		// 获取语言列表
		languages = new ArrayList<Language>(LocaleService.getDefaultLanguage().values());
		Collections.sort(languages, new Comparator<Language>() {
			public int compare(Language o1, Language o2) {
				return o1.toString().compareTo(o2.toString());
			}
		});
		targetLangControl = new TargetLangSelect(languages, bean.getTargetLang());
	}

	@Override
	public boolean performOk() {
		return validator() && isValid();
	}

	@Override
	public boolean okToLeave() {
		return validator() && isValid();
	}

	/**
	 * 输入验证器
	 * @return ;
	 */
	public boolean validator() {
		if (projCfgBean.getTargetLang().size() == 0) {
			MessageDialog.openError(getShell(),
					Messages.getString("projectsetting.ProjectSettingLanguagePage.msgTitle"),
					Messages.getString("projectsetting.ProjectSettingLanguagePage.msg1"));
			return false;
		}
		String srcCode = projCfgBean.getSourceLang().getCode();
		List<Language> target = projCfgBean.getTargetLang();
		for (Language tLang : target) {
			String tCode = tLang.getCode();
			if (tCode.equals(srcCode)) {
				MessageDialog.openError(getShell(),
						Messages.getString("projectsetting.ProjectSettingLanguagePage.msgTitle"),
						Messages.getString("projectsetting.ProjectSettingLanguagePage.msg2"));
				return false;
			}
		}
		return true;
	}

	/**
	 * Create contents of the preference page.
	 * @param parent
	 */
	@Override
	public Control createContents(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);
		container.setLayout(new GridLayout(1, false));

		// source language control
		Group sourceLanguageGrp = new Group(container, SWT.NONE);
		sourceLanguageGrp.setLayout(new GridLayout(1, false));
		sourceLanguageGrp.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		sourceLanguageGrp.setText(Messages.getString("projectsetting.ProjectSettingLanguagePage.sourceLanguageGrp"));

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
		srcLangComboViewer.setComparer(elementComparer);
		initDataBindings();
		// end source language

		// target language control
		Group targetLanguageGrp = new Group(container, SWT.NONE);
		targetLanguageGrp.setLayout(new GridLayout(3, false));
		targetLanguageGrp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true, 1, 1));
		targetLanguageGrp.setText(Messages.getString("projectsetting.ProjectSettingLanguagePage.targetLanguageGrp"));
		targetLangControl.createControl(targetLanguageGrp);
		parent.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		return container;
	}

	private void initDataBindings() {
		DataBindingContext ctx = new DataBindingContext();

		IObservableValue widgetValue = ViewersObservables.observeSingleSelection(srcLangComboViewer);
		IObservableValue modelValue = BeanProperties.value(ProjectInfoBean.class, "sourceLang").observe(projCfgBean);
		ctx.bindValue(widgetValue, modelValue, null, null);
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
		private List<Language> hasSelectedList;

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
		 * 构造器，创建一个带有可选内容和已被选内容的组件
		 * @param canSel
		 *            可以选择的内容
		 * @param haveSel
		 *            已被选择的内容
		 */
		public TargetLangSelect(Object canSel, List<Language> haveSel) {
			this.hasSelectedList = haveSel;
			this.canSelectInput = canSel;
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
			gridData.widthHint = 150;
			gridData.heightHint = 180;
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
			addBtn.setText(Messages.getString("projectsetting.ProjectSettingLanguagePage.addBtn"));
			addBtn.addListener(SWT.Selection, this);

			deleteBtn = new Button(selBtnComp, SWT.NONE);
			deleteBtn.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			deleteBtn.setText(Messages.getString("projectsetting.ProjectSettingLanguagePage.deleteBtn"));
			deleteBtn.addListener(SWT.Selection, this);

			deleteAllBtn = new Button(selBtnComp, SWT.NONE);
			deleteAllBtn.setText(Messages.getString("projectsetting.ProjectSettingLanguagePage.deleteAllBtn"));
			deleteAllBtn.addListener(SWT.Selection, this);

			Composite hasSelComp = new Composite(comp, SWT.NONE);
			hasSelComp.setLayout(new GridLayout(1, false));
			hasSelComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));

			hasSelTableViewer = new TableViewer(hasSelComp, SWT.BORDER | SWT.V_SCROLL | SWT.MULTI | SWT.FULL_SELECTION);
			hasSelTableViewer.getTable().setLayoutData(gridData);
			hasSelTableViewer.setLabelProvider(new LanguageLabelProvider());
			hasSelTableViewer.setContentProvider(new ArrayContentProvider());
			hasSelTableViewer.setInput(hasSelectedList);
			hasSelTableViewer.getTable().addListener(SWT.MouseDoubleClick, this);
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
		}

		/**
		 * 执行选择
		 * @param selection
		 *            ;
		 */
		private void execSelected(IStructuredSelection selection) {
			Iterator<?> it = selection.iterator();
			while (it.hasNext()) {
				boolean isContains = false;
				Language selLang = (Language) it.next();
				String selLangCode = selLang.getCode();
				for (int i = 0; i < hasSelectedList.size(); i++) {
					if (selLangCode.equals(hasSelectedList.get(i).getCode())) {
						isContains = true;
						break;
					}
				}
				if (!isContains) {
					hasSelectedList.add(selLang);
					hasSelTableViewer.add(selLang);
				}
			}
			hasSelTableViewer.setSelection(selection);
		}

		private void removeSelected(IStructuredSelection selection) {
			hasSelectedList.removeAll(selection.toList());
			hasSelTableViewer.remove(selection.toArray());
		}

	}

	/**
	 * 用于比较两个Language对象是否相等
	 */
	private IElementComparer elementComparer = new IElementComparer() {

		public int hashCode(Object element) {
			return 0;
		}

		public boolean equals(Object a, Object b) {
			if (a instanceof Language && b instanceof Language) {
				Language al = (Language) a;
				Language bl = (Language) b;
				if (al.getCode().equals(bl.getCode())) {
					return true;
				}
				return false;
			}
			return false;
		}
	};
}

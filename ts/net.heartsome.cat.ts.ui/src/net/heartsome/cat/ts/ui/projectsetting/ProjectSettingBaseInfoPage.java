/**
 * ProjectConfigBaseInfoPage.java
 *
 * Version information :
 *
 * Date:Nov 29, 2011
 *
 * Copyright notice :
 * 本文件及其附带的相关文件包含机密信息，仅限瀚特盛科技有限公司指定的，与本项目有关的内部人员和客户联络人员查阅和使用。 
 * 如果您不是本保密声明中指定的收件者，请立即销毁本文件，禁止对本文件或根据本文件中的内容采取任何其他行动， 
 * 包括但不限于：泄露本文件中的信息、以任何方式制作本文件全部或部分内容之副本、将本文件或其相关副本提供给任何其他人。
 */
package net.heartsome.cat.ts.ui.projectsetting;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.heartsome.cat.common.bean.ProjectInfoBean;
import net.heartsome.cat.common.locale.Language;
import net.heartsome.cat.common.util.TextUtil;
import net.heartsome.cat.ts.ui.resource.Messages;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class ProjectSettingBaseInfoPage extends PreferencePage {

	private Label projectNameLabel;
	private Label sourceLangLabel;
	private Label targetlangLabel;

	private ProjectInfoBean projCfgBean;

	private ArrayList<Text> lstText;

	private ArrayList<Combo> lstCombo;

	/**
	 * Create the preference page.
	 */
	public ProjectSettingBaseInfoPage(ProjectInfoBean bean) {
		setTitle(Messages.getString("projectsetting.ProjectSettingBaseInfoPage.title"));
		noDefaultAndApplyButton();
		this.projCfgBean = bean;

	}

	/**
	 * Create contents of the preference page.
	 * @param parent
	 */
	@Override
	public Control createContents(Composite parent) {
		// GridData fieldData = new GridData();
		// fieldData.heightHint = 10;
		// Composite container = new Composite(parent, SWT.NULL);
		// container.setLayout(new GridLayout());
		// container.setLayoutData(fieldData);
		//
		// ScrolledComposite cmpScrolled = new ScrolledComposite(container, SWT.V_SCROLL);
		// cmpScrolled.setAlwaysShowScrollBars(true);
		// cmpScrolled.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		// cmpScrolled.setExpandHorizontal(true);
		// cmpScrolled.setExpandVertical(true);

		Composite cmpField = new Composite(parent, SWT.None);
		cmpField.setLayout(new GridLayout(2, false));
		// cmpScrolled.setContent(cmpField);
		// cmpScrolled.setMinSize(cmpField.computeSize(SWT.DEFAULT, SWT.DEFAULT));

		Label label = new Label(cmpField, SWT.RIGHT);
		GridDataFactory.swtDefaults().align(SWT.RIGHT, SWT.CENTER).applyTo(label);
		label.setText(Messages.getString("projectsetting.ProjectSettingBaseInfoPage.projectNameLabel"));
		Point namePoint = label.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
		
		projectNameLabel = new Label(cmpField, SWT.NONE);
		// cmpScrolled.setMinSize(cmpField.computeSize(SWT.DEFAULT, SWT.DEFAULT));

		label = new Label(cmpField, SWT.RIGHT);
		GridDataFactory.swtDefaults().align(SWT.RIGHT, SWT.CENTER).applyTo(label);
		label.setText(Messages.getString("projectsetting.ProjectSettingBaseInfoPage.sourceLangLabel"));
		Point srcPoint = label.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);

		sourceLangLabel = new Label(cmpField, SWT.NONE);
		// cmpScrolled.setMinSize(cmpField.computeSize(SWT.DEFAULT, SWT.DEFAULT));

		label = new Label(cmpField, SWT.RIGHT);
		GridDataFactory.swtDefaults().align(SWT.RIGHT, SWT.CENTER).applyTo(label);
		label.setText(Messages.getString("projectsetting.ProjectSettingBaseInfoPage.targetlangLabel"));
		Point tgtPoint = label.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
		
		targetlangLabel = new Label(cmpField, SWT.NONE);
		// cmpScrolled.setMinSize(cmpField.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		initData();
		
		int width = Math.max(namePoint.x, Math.max(srcPoint.x, tgtPoint.x)) + 10;
		
		Map<String, String> mapField = projCfgBean.getMapField();
		if (mapField != null && mapField.size() > 0) {
			lstText = new ArrayList<Text>();
			Iterator<Entry<String, String>> it = mapField.entrySet().iterator();
			while (it.hasNext()) {
				Entry<String, String> entry = (Entry<String, String>) it.next();
				Label lbl = new Label(cmpField, SWT.WRAP);
				String strLbl = TextUtil.xmlToString(entry.getKey()).replaceAll("&", "&&")
						+ Messages.getString("wizards.NewProjectWizardProjInfoPage.colon");
				lbl.setText(strLbl);
				GridDataFactory.swtDefaults().align(SWT.RIGHT, SWT.CENTER).applyTo(lbl);
				GridData gd = (GridData) lbl.getLayoutData();
				Point p = lbl.computeSize(SWT.DEFAULT, SWT.DEFAULT);
				if (p.x > width) {
					gd.widthHint = width;
				}
				Text txt = new Text(cmpField, SWT.BORDER);
				txt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
				txt.setText(TextUtil.xmlToString(entry.getValue()));
				txt.setData(TextUtil.xmlToString(entry.getKey()));
				txt.addModifyListener(new ModifyListener() {
					
					public void modifyText(ModifyEvent e) {
						if (lstText != null && lstText.size() > 0) {
							boolean isValid = true;
							for (Text txt : lstText) {
								String value = txt.getText();
								if (value != null && !value.equals("")) {
									if (value.trim().equals("")) {
										setErrorMessage(Messages.getString("wizard.NewProjectWizardProjInfoPage.msg3"));
										isValid = false;
										setValid(false);
										break;
									} else if (value.trim().length() > 50) {
										setErrorMessage(Messages.getString("wizard.NewProjectWizardProjInfoPage.msg4"));
										setValid(false);
										isValid = false;
										break;
									}
								}
							}
							if (isValid) {
								setErrorMessage(null);
								setValid(true);
							}
						}
					}
				});
				lstText.add(txt);
				// cmpScrolled.setMinSize(cmpField.computeSize(SWT.DEFAULT, SWT.DEFAULT));
			}
		}

		Map<String, Object[]> mapAttr = projCfgBean.getMapAttr();
		if (mapAttr != null && mapAttr.size() > 0) {
			lstCombo = new ArrayList<Combo>();
			Iterator<Entry<String, Object[]>> it = mapAttr.entrySet().iterator();
			while (it.hasNext()) {
				Entry<String, Object[]> entry = (Entry<String, Object[]>) it.next();
				String attrName = TextUtil.xmlToString(entry.getKey());
				String attrSelVal = TextUtil.xmlToString((String) entry.getValue()[0]);
				@SuppressWarnings("unchecked")
				List<String> lstAttrVal = (List<String>) entry.getValue()[1];
				String[] arrAttrVal = new String[lstAttrVal.size()];
				int selIndex = 0;
				for (int i = 0; i < lstAttrVal.size(); i++) {
					arrAttrVal[i] = TextUtil.xmlToString(lstAttrVal.get(i));
					if (attrSelVal.equals(arrAttrVal[i])) {
						selIndex = i;
					}
				}
				Label lbl = new Label(cmpField, SWT.WRAP);
				String strLbl = attrName.replaceAll("&", "&&")
						+ Messages.getString("wizards.NewProjectWizardProjInfoPage.colon");
				lbl.setText(strLbl);
				GridDataFactory.swtDefaults().align(SWT.RIGHT, SWT.CENTER).applyTo(lbl);
				GridData gd = (GridData) lbl.getLayoutData();
				Point p = lbl.computeSize(SWT.DEFAULT, SWT.DEFAULT);
				if (p.x > width) {
					gd.widthHint = width;
				}
				Combo cmb = new Combo(cmpField, SWT.READ_ONLY);
				cmb.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
				cmb.setItems(arrAttrVal);
				cmb.select(selIndex);
				cmb.setData(attrName);
				lstCombo.add(cmb);
				// cmpScrolled.setMinSize(cmpField.computeSize(SWT.DEFAULT, SWT.DEFAULT));
			}
		}
		// cmpScrolled.setMinSize(cmpField.computeSize(SWpT.DEFAULT, SWT.DEFAULT));
		
		return cmpField;
	}

	/**
	 * 初始化界面数据 ;
	 */
	private void initData() {
		projectNameLabel.setText(this.projCfgBean.getProjectName().replaceAll("&", "&&"));
		sourceLangLabel.setText(this.projCfgBean.getSourceLang().getName());
		List<Language> targetLangs = this.projCfgBean.getTargetLang();
		StringBuffer targetlang = new StringBuffer();
		for (int i = 0; i < targetLangs.size(); i++) {
			targetlang.append(targetLangs.get(i).getName()).append(",");
		}
		String tLangs = targetlang.toString();
		targetlangLabel.setText(tLangs.substring(0, tLangs.length() - 1));
	}

	@Override
	public boolean performOk() {
		if (lstText != null) {
			LinkedHashMap<String, String> mapField = new LinkedHashMap<String, String>();
			for (Text txt : lstText) {
				if (!txt.isDisposed()) {
					mapField.put(TextUtil.stringToXML((String) txt.getData()), TextUtil.stringToXML(txt.getText()).trim());
				}
			}
			projCfgBean.setMapField(mapField);
		}

		if (lstCombo != null) {
			LinkedHashMap<String, Object[]> mapAttr = new LinkedHashMap<String, Object[]>();
			for (Combo cmb : lstCombo) {
				if (!cmb.isDisposed()) {
					ArrayList<String> lstAttrValue = new ArrayList<String>();
					for (String attrVal : cmb.getItems()) {
						lstAttrValue.add(TextUtil.stringToXML(attrVal));
					}
					mapAttr.put(TextUtil.stringToXML((String) cmb.getData()),
							new Object[] { TextUtil.stringToXML(cmb.getText()), lstAttrValue });
				}
			}
			projCfgBean.setMapAttr(mapAttr);
		}
		return super.performOk();
	}
	
}

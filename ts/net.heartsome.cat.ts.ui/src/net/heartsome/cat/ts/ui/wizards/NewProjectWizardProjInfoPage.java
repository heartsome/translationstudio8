/**
 * NewProjectWizardOnePage.java
 *
 * Version information :
 *
 * Date:Oct 20, 2011
 *
 * Copyright notice :
 * 本文件及其附带的相关文件包含机密信息，仅限瀚特盛科技有限公司指定的，与本项目有关的内部人员和客户联络人员查阅和使用。 
 * 如果您不是本保密声明中指定的收件者，请立即销毁本文件，禁止对本文件或根据本文件中的内容采取任何其他行动， 
 * 包括但不限于：泄露本文件中的信息、以任何方式制作本文件全部或部分内容之副本、将本文件或其相关副本提供给任何其他人。
 */
package net.heartsome.cat.ts.ui.wizards;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import net.heartsome.cat.common.util.CommonFunction;
import net.heartsome.cat.common.util.TextUtil;
import net.heartsome.cat.ts.core.ValidationUtils;
import net.heartsome.cat.ts.ui.Activator;
import net.heartsome.cat.ts.ui.resource.Messages;
import net.heartsome.cat.ts.ui.util.PreferenceUtil;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

/**
 * 创建项目第一个向导页,用于输入项目名称和设置源语言和目标语言
 * @author Jason
 * @version
 * @since JDK1.6
 */
public class NewProjectWizardProjInfoPage extends WizardPage implements Listener {

	private Text projectNameText;

	private Validator validator;

	private ArrayList<Text> lstText;

	private ArrayList<Combo> lstCombo;

	private Group groupField;

	private ScrolledComposite cmpScrolled;

	private Composite cmpField;

	/**
	 * Create the wizard.
	 */
	public NewProjectWizardProjInfoPage() {
		super("wizardPage");
		setImageDescriptor(Activator.getImageDescriptor("images/project/new-project-logo.png"));
		setTitle(Messages.getString("wizard.NewProjectWizardProjInfoPage.title"));
		setDescription(Messages.getString("wizard.NewProjectWizardProjInfoPage.desc"));
		setPageComplete(false);
		validator = new Validator();
	}

	/**
	 * Create contents of the wizard.
	 * @param parent
	 */
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);
		container.setLayout(new GridLayout(1, false));

		// Project Name Control
		int labelWidth = 100;
		GridData gdLabel = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gdLabel.widthHint = labelWidth;

		Composite projInfoComp = new Composite(container, SWT.NONE);
		projInfoComp.setLayout(new GridLayout(2, false));
		projInfoComp.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		Label label = new Label(projInfoComp, SWT.RIGHT);
		label.setLayoutData(gdLabel);
		label.setText(Messages.getString("wizard.NewProjectWizardProjInfoPage.projectNameText"));

		projectNameText = new Text(projInfoComp, SWT.BORDER);
		projectNameText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		projectNameText.setTextLimit(21);
		projectNameText.addListener(SWT.Modify, this);

		ArrayList<String> lstField = PreferenceUtil.getProjectFieldList();
		LinkedHashMap<String, ArrayList<String>> mapAttr = PreferenceUtil.getProjectAttributeMap();
		if ((lstField != null && lstField.size() > 0) || (mapAttr != null && mapAttr.size() > 0)) {
			groupField = new Group(container, SWT.None);
			groupField.setLayout(new GridLayout());
			GridData fieldData = new GridData(GridData.FILL_HORIZONTAL);
			fieldData.heightHint = 350;
			groupField.setLayoutData(fieldData);
			groupField.setText(Messages.getString("wizards.NewProjectWizardProjInfoPage.groupField"));
			cmpScrolled = new ScrolledComposite(groupField, SWT.V_SCROLL);
			cmpScrolled.setAlwaysShowScrollBars(false);
			cmpScrolled.setLayoutData(new GridData(GridData.FILL_BOTH));
			cmpScrolled.setExpandHorizontal(true);
			cmpScrolled.setExpandVertical(true);

			cmpField = new Composite(cmpScrolled, SWT.None);
			cmpField.setLayout(new GridLayout(2, false));
			cmpScrolled.setContent(cmpField);
			cmpScrolled.setMinSize(cmpField.computeSize(SWT.DEFAULT, SWT.DEFAULT));

			if (lstField != null && lstField.size() > 0) {
				lstText = new ArrayList<Text>();
				for (String strField : lstField) {
					Label lbl = new Label(cmpField, SWT.WRAP);
					String strLbl = strField.replaceAll("&", "&&")
							+ Messages.getString("wizards.NewProjectWizardProjInfoPage.colon");
					lbl.setText(strLbl);
					GridDataFactory.swtDefaults().align(SWT.RIGHT, SWT.CENTER).applyTo(lbl);
					GridData gd = (GridData) lbl.getLayoutData();
					Point p = lbl.computeSize(SWT.DEFAULT, SWT.DEFAULT);
					if (p.x >= 100) {
						gd.widthHint = 100;
					}
					Text txt = new Text(cmpField, SWT.BORDER);
					txt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
					txt.setData(strField);
					txt.addListener(SWT.Modify, this);
					lstText.add(txt);
					cmpScrolled.setMinSize(cmpField.computeSize(SWT.DEFAULT, SWT.DEFAULT));
				}
			}

			if (mapAttr != null && mapAttr.size() > 0) {
				Iterator<Entry<String, ArrayList<String>>> it = mapAttr.entrySet().iterator();
				lstCombo = new ArrayList<Combo>();
				while (it.hasNext()) {
					Entry<String, ArrayList<String>> entry = (Entry<String, ArrayList<String>>) it.next();
					String attrName = entry.getKey();
					ArrayList<String> lstAttrVal = entry.getValue();
					lstAttrVal.add(0, "");
					Label lbl = new Label(cmpField, SWT.WRAP);
					String strLbl = attrName.replaceAll("&", "&&")
							+ Messages.getString("wizards.NewProjectWizardProjInfoPage.colon");
					lbl.setText(strLbl);
					GridDataFactory.swtDefaults().align(SWT.RIGHT, SWT.CENTER).applyTo(lbl);
					GridData gd = (GridData) lbl.getLayoutData();
					Point p = lbl.computeSize(SWT.DEFAULT, SWT.DEFAULT);
					if (p.x >= 100) {
						gd.widthHint = 100;
					}
					Combo cmb = new Combo(cmpField, SWT.READ_ONLY);
					cmb.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
					cmb.setItems(lstAttrVal.toArray(new String[lstAttrVal.size()]));
					cmb.select(0);
					cmb.setData(attrName);
					lstCombo.add(cmb);
					cmpScrolled.setMinSize(cmpField.computeSize(SWT.DEFAULT, SWT.DEFAULT));
				}
			}

			cmpScrolled.setMinSize(cmpField.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		}
		setControl(container);
		validator.update();

	}

	public void reload() {
		if (lstText != null) {
			lstText.clear();
		}
		if (lstCombo != null) {
			lstCombo.clear();
		}
		ArrayList<String> lstField = PreferenceUtil.getProjectFieldList();
		LinkedHashMap<String, ArrayList<String>> mapAttr = PreferenceUtil.getProjectAttributeMap();
		if ((lstField != null && lstField.size() > 0) || (mapAttr != null && mapAttr.size() > 0)) {
			if (groupField == null || groupField.isDisposed()) {
				groupField = new Group((Composite) getControl(), SWT.None);
				groupField.setLayout(new GridLayout());
				GridData fieldData = new GridData(GridData.FILL_BOTH);
				groupField.setLayoutData(fieldData);
				groupField.setText(Messages.getString("wizards.NewProjectWizardProjInfoPage.groupField"));
				cmpScrolled = new ScrolledComposite(groupField, SWT.V_SCROLL);
				cmpScrolled.setAlwaysShowScrollBars(false);
				cmpScrolled.setLayoutData(new GridData(GridData.FILL_BOTH));
				cmpScrolled.setExpandHorizontal(true);
				cmpScrolled.setExpandVertical(true);

				cmpField = new Composite(cmpScrolled, SWT.None);
				cmpField.setLayout(new GridLayout(2, false));
				cmpScrolled.setContent(cmpField);
				cmpScrolled.setMinSize(cmpField.computeSize(SWT.DEFAULT, SWT.DEFAULT));
			}
			if (cmpField != null && !cmpField.isDisposed()) {
				for (Control control : cmpField.getChildren()) {
					control.dispose();
				}
				cmpField.layout();
				cmpScrolled.layout();
				groupField.layout();
				groupField.getParent().layout();
				cmpScrolled.setMinSize(cmpField.computeSize(SWT.DEFAULT, SWT.DEFAULT));
				if (lstField != null && lstField.size() > 0) {
					if (lstText == null) {
						lstText = new ArrayList<Text>();
					}
					for (String strField : lstField) {
						Label lbl = new Label(cmpField, SWT.WRAP);
						String strLbl = strField.replaceAll("&", "&&")
								+ Messages.getString("wizards.NewProjectWizardProjInfoPage.colon");
						lbl.setText(strLbl);
						GridDataFactory.swtDefaults().align(SWT.RIGHT, SWT.CENTER).applyTo(lbl);
						GridData gd = (GridData) lbl.getLayoutData();
						Point p = lbl.computeSize(SWT.DEFAULT, SWT.DEFAULT);
						if (p.x >= 100) {
							gd.widthHint = 100;
						}
						Text txt = new Text(cmpField, SWT.BORDER);
						txt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
						txt.setData(strField);
						txt.addListener(SWT.Modify, this);
						lstText.add(txt);
						cmpScrolled.setMinSize(cmpField.computeSize(SWT.DEFAULT, SWT.DEFAULT));
					}
				}

				if (mapAttr != null && mapAttr.size() > 0) {
					Iterator<Entry<String, ArrayList<String>>> it = mapAttr.entrySet().iterator();
					if (lstCombo == null) {
						lstCombo = new ArrayList<Combo>();
					}
					while (it.hasNext()) {
						Entry<String, ArrayList<String>> entry = (Entry<String, ArrayList<String>>) it.next();
						String attrName = entry.getKey();
						ArrayList<String> lstAttrVal = entry.getValue();
						lstAttrVal.add(0, "");
						Label lbl = new Label(cmpField, SWT.WRAP);
						String strLbl = attrName.replaceAll("&", "&&")
								+ Messages.getString("wizards.NewProjectWizardProjInfoPage.colon");
						lbl.setText(strLbl);
						GridDataFactory.swtDefaults().align(SWT.RIGHT, SWT.CENTER).applyTo(lbl);
						GridData gd = (GridData) lbl.getLayoutData();
						Point p = lbl.computeSize(SWT.DEFAULT, SWT.DEFAULT);
						if (p.x >= 100) {
							gd.widthHint = 100;
						}
						Combo cmb = new Combo(cmpField, SWT.READ_ONLY);
						cmb.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
						cmb.setItems(lstAttrVal.toArray(new String[lstAttrVal.size()]));
						cmb.select(0);
						cmb.setData(attrName);
						lstCombo.add(cmb);
						cmpScrolled.setMinSize(cmpField.computeSize(SWT.DEFAULT, SWT.DEFAULT));
					}
				}
				cmpScrolled.setMinSize(cmpField.computeSize(SWT.DEFAULT, SWT.DEFAULT));
				cmpField.layout();
				cmpScrolled.layout();
				groupField.layout();
				groupField.getParent().layout();
			}
		} else if (groupField != null && !groupField.isDisposed()) {
			Composite cmpParent = groupField.getParent();
			groupField.dispose();
			cmpParent.layout();
		}
	}

	/**
	 * (non-Javadoc)
	 * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
	 */
	public void handleEvent(Event event) {
		// Add target Language
		validator.update();
	}

	/**
	 * Get Project Name
	 * @return ;
	 */
	public String getProjectName() {
		return this.projectNameText.getText().trim();
	}

	/**
	 * 获取项目信息
	 * @return ;
	 */
	public IProject getProject() {
		return ResourcesPlugin.getWorkspace().getRoot().getProject(this.getProjectName());
	}

	/**
	 * 获取项目字段集合
	 * @return key 为字段名称，value 为字段值
	 */
	public LinkedHashMap<String, String> getFieldMap() {
		LinkedHashMap<String, String> mapField = new LinkedHashMap<String, String>();
		if (lstText != null) {
			for (Text txt : lstText) {
				if (!txt.isDisposed()) {
					mapField.put(TextUtil.stringToXML((String) txt.getData()), TextUtil.stringToXML(txt.getText()).trim());
				}
			}
		}
		return mapField;
	}

	/**
	 * 获取项目属性字段集合
	 * @return key 为属性名称，value 中第一个值为选中的属性值，第二个值为该属性对应的所有属性值集合
	 */
	public LinkedHashMap<String, Object[]> getAttributeMap() {
		LinkedHashMap<String, Object[]> mapAttr = new LinkedHashMap<String, Object[]>();
		if (lstCombo != null) {
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
		}
		return mapAttr;
	}

	/**
	 * 验证器。验证界面输入
	 * @author Jason
	 * @version
	 * @since JDK1.6
	 */
	private final class Validator {

		public void update() {
			final IWorkspace workspace = ResourcesPlugin.getWorkspace();
			String projectName = getProjectName();
			String result = ValidationUtils.validateProjectName(projectName);
			if (result != null) {
				setErrorMessage(result);
				setPageComplete(false);
				return;
			}

			// check whether the project name is valid
			final IStatus nameStatus = workspace.validateName(projectName, IResource.PROJECT);
			if (!nameStatus.isOK()) {
				setErrorMessage(nameStatus.getMessage());
				setPageComplete(false);
				return;
			}
			
			//　修改，上一步验证是验证在当前操作系统下的字符，但打包出来的项目名有可能在其他操作系统不支持。故再验证一次	robert	2013-04-25
			String validResult = CommonFunction.validResourceName(projectName);
			if (validResult != null) {
				setErrorMessage(validResult);
				setPageComplete(false);
				return;
				
			}

			final IProject project = getProject();
			if (project.exists()) {
				setErrorMessage(Messages.getString("wizard.NewProjectWizardProjInfoPage.msg1"));
				setPageComplete(false);
				return;
			}

			final IPath projectLocation = ResourcesPlugin.getWorkspace().getRoot().getLocation().append(projectName);
			if (projectLocation.toFile().exists()) {
				setErrorMessage(Messages.getString("wizard.NewProjectWizardProjInfoPage.msg2"));
				setPageComplete(false);
				return;
			}
			
			if (lstText != null && lstText.size() > 0) {
				for (Text txt : lstText) {
					String value = txt.getText();
					if (value != null && !value.equals("")) {
						if (value.trim().equals("")) {
							setErrorMessage(Messages.getString("wizard.NewProjectWizardProjInfoPage.msg3"));
							setPageComplete(false);
							return;
						} else if (value.trim().length() > 50) {
							setErrorMessage(Messages.getString("wizard.NewProjectWizardProjInfoPage.msg4"));
							setPageComplete(false);
							return;
						}
					}
				}
			}

			setPageComplete(true);
			setErrorMessage(null);
			setMessage(null);
		}

	}
}

package net.heartsome.cat.ts.ui.preferencepage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

import net.heartsome.cat.common.ui.HsImageLabel;
import net.heartsome.cat.ts.ui.Activator;
import net.heartsome.cat.ts.ui.dialog.InputDialog;
import net.heartsome.cat.ts.ui.resource.Messages;
import net.heartsome.cat.ts.ui.util.PreferenceUtil;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.util.Util;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * 项目属性设置的首选项页面
 * @author peason
 * @version
 * @since JDK1.6
 */
public class ProjectPropertiesPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

	public static final String ID = "net.heartsome.cat.ts.ui.preferencepage.ProjectPropertiesPreferencePage";

	private IPreferenceStore preferenceStore;

	/** 文本字段 List */
	private List fieldList;

	/** 文本字段添加按钮 */
	private Button btnFieldAdd;

	/** 文本字段编辑按钮 */
	private Button btnFieldEdit;

	/** 文本字段删除按钮 */
	private Button btnFieldDel;

	/** 属性字段 List */
	private List attrNameList;

	/** 属性字段添加按钮 */
	private Button btnAttrNameAdd;

	/** 属性字段编辑按钮 */
	private Button btnAttrNameEdit;

	/** 属性字段删除按钮 */
	private Button btnAttrNameDel;

	/** 属性值字段 List */
	private List attrValList;

	/** 属性值字段添加按钮 */
	private Button btnAttrValAdd;

	/** 属性值字段编辑按钮 */
	private Button btnAttrValEdit;

	/** 属性值字段删除按钮 */
	private Button btnAttrValDel;

	/** key 为属性字段名称， value 为对应的属性值集合，当对 attrNameList 和 attrValList 进行添加，删除操作时，要更新此集合，便于保存 */
	private HashMap<String, ArrayList<String>> mapAttr = new HashMap<String, ArrayList<String>>();

	public ProjectPropertiesPreferencePage() {
		setTitle(Messages.getString("preferencepage.ProjectPropertiesPreferencePage.title"));
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		preferenceStore = getPreferenceStore();
	}

	public void init(IWorkbench workbench) {

	}

	@Override
	protected Control createContents(Composite parent) {
		Composite tparent = new Composite(parent, SWT.NONE);
		tparent.setLayout(new GridLayout());
		tparent.setLayoutData(new GridData(GridData.FILL_BOTH));

		Group groupField = new Group(tparent, SWT.NONE);
		groupField.setLayout(new GridLayout());
		groupField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		groupField.setText(Messages.getString("preferencepage.ProjectPropertiesPreferencePage.groupField"));

		HsImageLabel imageLabel1 = new HsImageLabel(
				Messages.getString("preferencepage.ProjectPropertiesPreferencePage.imageLabel1"),
				Activator.getImageDescriptor("images/preference/projectProperties/field_32.png"));
		Composite cmpField = imageLabel1.createControl(groupField);
		cmpField.setLayout(new GridLayout(2, false));
		cmpField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Label lbl = new Label(cmpField, SWT.None);
		lbl.setText(Messages.getString("preferencepage.ProjectPropertiesPreferencePage.lblField"));
		GridDataFactory.swtDefaults().span(2, 1).applyTo(lbl);
		fieldList = new List(cmpField, SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER);
		GridData listData = new GridData();
		listData.widthHint = 130;
		if (Util.isLinux()) {
			listData.heightHint = 150;
		} else {
			listData.heightHint = 80;
		}
		fieldList.setLayoutData(listData);
		Composite cmpFieldBtn = new Composite(cmpField, SWT.None);
		cmpFieldBtn.setLayout(new GridLayout());
		cmpFieldBtn.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		btnFieldAdd = new Button(cmpFieldBtn, SWT.None);
		btnFieldAdd.setText(Messages.getString("preferencepage.ProjectPropertiesPreferencePage.btnFieldAdd"));
		btnFieldEdit = new Button(cmpFieldBtn, SWT.None);
		btnFieldEdit.setText(Messages.getString("preferencepage.ProjectPropertiesPreferencePage.btnFieldEdit"));
		btnFieldDel = new Button(cmpFieldBtn, SWT.None);
		btnFieldDel.setText(Messages.getString("preferencepage.ProjectPropertiesPreferencePage.btnFieldDel"));
		Point fieldAddPoint = btnFieldAdd.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
		Point fieldEditPoint = btnFieldEdit.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
		Point fieldDelPoint = btnFieldDel.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
		GridData btnData = new GridData();
		int width = Math.max(fieldEditPoint.x, Math.max(fieldAddPoint.x, fieldDelPoint.x));
		btnData.widthHint = width + 10;
		btnFieldAdd.setLayoutData(btnData);
		btnFieldEdit.setLayoutData(btnData);
		btnFieldDel.setLayoutData(btnData);

		Group groupAttr = new Group(tparent, SWT.NONE);
		groupAttr.setLayout(new GridLayout());
		groupAttr.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		groupAttr.setText(Messages.getString("preferencepage.ProjectPropertiesPreferencePage.groupAttr"));

		HsImageLabel imageLabel2 = new HsImageLabel(
				Messages.getString("preferencepage.ProjectPropertiesPreferencePage.imageLabel2"),
				Activator.getImageDescriptor("images/preference/projectProperties/attribute_32.png"));
		Composite cmpAttr = imageLabel2.createControl(groupAttr);
		cmpAttr.setLayout(new GridLayout(4, false));
		cmpAttr.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		lbl = new Label(cmpAttr, SWT.None);
		lbl.setText(Messages.getString("preferencepage.ProjectPropertiesPreferencePage.lblAttrName"));
		GridDataFactory.swtDefaults().span(2, 1).applyTo(lbl);
		lbl = new Label(cmpAttr, SWT.None);
		lbl.setText(Messages.getString("preferencepage.ProjectPropertiesPreferencePage.lblAttrVal"));
		GridDataFactory.swtDefaults().span(2, 1).applyTo(lbl);
		attrNameList = new List(cmpAttr, SWT.SINGLE | SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER);
		attrNameList.setLayoutData(listData);
		Composite cmpAttrNameBtn = new Composite(cmpAttr, SWT.None);
		cmpAttrNameBtn.setLayout(new GridLayout());
		cmpAttrNameBtn.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		btnAttrNameAdd = new Button(cmpAttrNameBtn, SWT.None);
		btnAttrNameAdd.setText(Messages.getString("preferencepage.ProjectPropertiesPreferencePage.btnAttrNameAdd"));
		btnAttrNameEdit = new Button(cmpAttrNameBtn, SWT.None);
		btnAttrNameEdit.setText(Messages.getString("preferencepage.ProjectPropertiesPreferencePage.btnAttrNameEdit"));
		btnAttrNameDel = new Button(cmpAttrNameBtn, SWT.None);
		btnAttrNameDel.setText(Messages.getString("preferencepage.ProjectPropertiesPreferencePage.btnAttrNameDel"));
		Point atrrNameAddPoint = btnAttrNameAdd.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
		Point atrrNameEditPoint = btnAttrNameEdit.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
		Point atrrNameDelPoint = btnAttrNameDel.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
		btnData = new GridData();
		width = Math.max(atrrNameEditPoint.x, Math.max(atrrNameAddPoint.x, atrrNameDelPoint.x));
		btnData.widthHint = width + 10;
		btnAttrNameAdd.setLayoutData(btnData);
		btnAttrNameEdit.setLayoutData(btnData);
		btnAttrNameDel.setLayoutData(btnData);

		attrValList = new List(cmpAttr, SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER);
		attrValList.setLayoutData(listData);
		Composite cmpAttrValBtn = new Composite(cmpAttr, SWT.None);
		cmpAttrValBtn.setLayout(new GridLayout());
		cmpAttrValBtn.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		btnAttrValAdd = new Button(cmpAttrValBtn, SWT.None);
		btnAttrValAdd.setText(Messages.getString("preferencepage.ProjectPropertiesPreferencePage.btnAttrValAdd"));
		btnAttrValEdit = new Button(cmpAttrValBtn, SWT.None);
		btnAttrValEdit.setText(Messages.getString("preferencepage.ProjectPropertiesPreferencePage.btnAttrValEdit"));
		btnAttrValDel = new Button(cmpAttrValBtn, SWT.None);
		btnAttrValDel.setText(Messages.getString("preferencepage.ProjectPropertiesPreferencePage.btnAttrValDel"));
		Point atrrValAddPoint = btnAttrValAdd.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
		Point atrrValEditPoint = btnAttrValEdit.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
		Point atrrValDelPoint = btnAttrValDel.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
		btnData = new GridData();
		width = Math.max(atrrValEditPoint.x, Math.max(atrrValAddPoint.x, atrrValDelPoint.x));
		btnData.widthHint = width + 10;
		btnAttrValAdd.setLayoutData(btnData);
		btnAttrValEdit.setLayoutData(btnData);
		btnAttrValDel.setLayoutData(btnData);

		imageLabel1.computeSize(-100);
		imageLabel2.computeSize(-100);

		initListener();
		setValue(false);
		return parent;
	}

	@Override
	protected void performDefaults() {
		setValue(true);
		super.performDefaults();
	}

	@Override
	public boolean performOk() {
		// 保存文本字段
		String[] arrField = fieldList.getItems();
		preferenceStore.setValue("net.heartsome.cat.ts.ui.preferencepage.ProjectPropertiesPreferencePage.fieldCount",
				arrField.length);
		for (int i = 0; i < arrField.length; i++) {
			preferenceStore.setValue(
					"net.heartsome.cat.ts.ui.preferencepage.ProjectPropertiesPreferencePage.field" + i, arrField[i]);
		}

		// 保存属性字段
		preferenceStore.setValue(
				"net.heartsome.cat.ts.ui.preferencepage.ProjectPropertiesPreferencePage.attrNameCount", mapAttr.size());
		Iterator<Entry<String, ArrayList<String>>> it = mapAttr.entrySet().iterator();
		int nameIndex = 0;
		while (it.hasNext()) {
			Entry<String, ArrayList<String>> entry = (Entry<String, ArrayList<String>>) it.next();
			preferenceStore.setValue("net.heartsome.cat.ts.ui.preferencepage.ProjectPropertiesPreferencePage.attrName"
					+ nameIndex, entry.getKey());
			ArrayList<String> lstValue = entry.getValue();
			preferenceStore.setValue("net.heartsome.cat.ts.ui.preferencepage.ProjectPropertiesPreferencePage.attrName"
					+ nameIndex + ".count", lstValue.size());
			for (int j = 0; j < lstValue.size(); j++) {
				preferenceStore.setValue(
						"net.heartsome.cat.ts.ui.preferencepage.ProjectPropertiesPreferencePage.attrName" + nameIndex
								+ ".attrVal" + j, lstValue.get(j));
			}
			nameIndex++;
		}
		return super.performOk();
	}

	private void setValue(boolean isApplyDefault) {
		if (isApplyDefault) {
			int fieldCount = preferenceStore
					.getDefaultInt("net.heartsome.cat.ts.ui.preferencepage.ProjectPropertiesPreferencePage.fieldCount");
			if (fieldCount > 0) {
				String[] arrField = new String[fieldCount];
				for (int i = 0; i < fieldCount; i++) {
					arrField[i] = preferenceStore
							.getDefaultString("net.heartsome.cat.ts.ui.preferencepage.ProjectPropertiesPreferencePage.field"
									+ i);
				}
				fieldList.setItems(arrField);
			} else {
				fieldList.removeAll();
			}

			int attrNameCount = preferenceStore
					.getDefaultInt("net.heartsome.cat.ts.ui.preferencepage.ProjectPropertiesPreferencePage.attrNameCount");
			if (attrNameCount > 0) {
				String[] arrAttrName = new String[attrNameCount];
				for (int i = 0; i < attrNameCount; i++) {
					arrAttrName[i] = preferenceStore
							.getDefaultString("net.heartsome.cat.ts.ui.preferencepage.ProjectPropertiesPreferencePage.attrName"
									+ i);
					int attrValCount = preferenceStore
							.getDefaultInt("net.heartsome.cat.ts.ui.preferencepage.ProjectPropertiesPreferencePage.attrName"
									+ i + ".count");
					String[] arrAttrVal = new String[attrValCount];
					if (attrValCount > 0) {
						for (int j = 0; j < attrValCount; j++) {
							arrAttrVal[j] = preferenceStore
									.getDefaultString("net.heartsome.cat.ts.ui.preferencepage.ProjectPropertiesPreferencePage.attrName"
											+ i + ".attrVal" + j);
						}
					}
					if (i == 0) {
						attrValList.setItems(arrAttrVal);
						attrValList.select(0);
					}
					mapAttr.put(arrAttrName[i], new ArrayList<String>(Arrays.asList(arrAttrVal)));
				}
				attrNameList.setItems(arrAttrName);
				attrNameList.select(0);
			} else {
				attrNameList.removeAll();
				attrValList.removeAll();
				mapAttr.clear();
			}
		} else {
			ArrayList<String> lstField = PreferenceUtil.getProjectFieldList();
			fieldList.setItems(lstField.toArray(new String[lstField.size()]));

			mapAttr = PreferenceUtil.getProjectAttributeMap();
			Set<String> setAttrName = mapAttr.keySet();
			attrNameList.setItems(setAttrName.toArray(new String[setAttrName.size()]));
			if (setAttrName.size() > 0) {
				attrNameList.select(0);
				ArrayList<String> lstAttrVal = mapAttr.get(attrNameList.getItem(0));
				attrValList.setItems(lstAttrVal.toArray(new String[lstAttrVal.size()]));
				if (lstAttrVal.size() > 0) {
					attrValList.select(0);
				}
			}
		}
	}

	private final int FIELD_ADD = 1;

	private final int ATTRNAME_ADD = 2;

	private final int ATTRVAL_ADD = 3;

	private void initListener() {
		btnFieldAdd.addSelectionListener(new SelectionListener() {

			public void widgetSelected(SelectionEvent e) {
				String[] arrField = fieldList.getItems();
				InputDialog dialog = new InputDialog(getShell(), Messages
						.getString("preferencepage.ProjectPropertiesPreferencePage.AddDialog.title1"), Messages
						.getString("preferencepage.ProjectPropertiesPreferencePage.AddDialog.lbl1"), null,
						new InputValidator(FIELD_ADD, new ArrayList<String>(Arrays.asList(arrField))));
				if (dialog.open() == IDialogConstants.OK_ID) {
					String fieldVal = dialog.getValue();
					fieldList.add(fieldVal.trim());
				}
			}

			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});

		btnFieldEdit.addSelectionListener(new SelectionListener() {

			public void widgetSelected(SelectionEvent e) {
				if (fieldList.getSelectionCount() <= 0) {
					MessageDialog.openInformation(getShell(),
							Messages.getString("preferencepage.ProjectPropertiesPreferencePage.msgTitle2"),
							Messages.getString("preferencepage.ProjectPropertiesPreferencePage.msg10"));
					return;
				} else if (fieldList.getSelectionCount() > 1) {
					MessageDialog.openInformation(getShell(),
							Messages.getString("preferencepage.ProjectPropertiesPreferencePage.msgTitle2"),
							Messages.getString("preferencepage.ProjectPropertiesPreferencePage.msg11"));
					return;
				} else {
					int selIndex = fieldList.getSelectionIndex();
					String selVal = fieldList.getSelection()[0];
					ArrayList<String> lstField = new ArrayList<String>(Arrays.asList(fieldList.getItems()));
					lstField.remove(selVal);
					InputDialog dialog = new InputDialog(getShell(), Messages
							.getString("preferencepage.ProjectPropertiesPreferencePage.AddDialog.title4"), Messages
							.getString("preferencepage.ProjectPropertiesPreferencePage.AddDialog.lbl1"), selVal,
							new InputValidator(FIELD_ADD, lstField));
					if (dialog.open() == IDialogConstants.OK_ID) {
						String fieldVal = dialog.getValue().trim();
						fieldList.remove(selIndex);
						fieldList.add(fieldVal, selIndex);
						fieldList.select(selIndex);
					}
				}
			}

			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});

		btnFieldDel.addSelectionListener(new SelectionListener() {

			public void widgetSelected(SelectionEvent e) {
				if (fieldList.getSelectionCount() == 0) {
					MessageDialog.openInformation(getShell(),
							Messages.getString("preferencepage.ProjectPropertiesPreferencePage.msgTitle2"),
							Messages.getString("preferencepage.ProjectPropertiesPreferencePage.msg8"));
					return;
				}
				if (MessageDialog.openConfirm(getShell(),
						Messages.getString("preferencepage.ProjectPropertiesPreferencePage.msgTitle1"),
						Messages.getString("preferencepage.ProjectPropertiesPreferencePage.msg1"))) {
					fieldList.remove(fieldList.getSelectionIndices());
				}
			}

			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});

		attrNameList.addSelectionListener(new SelectionListener() {

			public void widgetSelected(SelectionEvent e) {
				if (attrNameList.getSelectionCount() > 0) {
					String strSel = attrNameList.getSelection()[0];
					ArrayList<String> lstAttrVal = mapAttr.get(strSel);
					attrValList.removeAll();
					attrValList.setItems(lstAttrVal.toArray(new String[lstAttrVal.size()]));
				}
			}

			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});

		btnAttrNameAdd.addSelectionListener(new SelectionListener() {

			public void widgetSelected(SelectionEvent e) {
				String[] arrAttrName = attrNameList.getItems();
				InputDialog dialog = new InputDialog(getShell(), Messages
						.getString("preferencepage.ProjectPropertiesPreferencePage.AddDialog.title2"), Messages
						.getString("preferencepage.ProjectPropertiesPreferencePage.AddDialog.lbl2"), null,
						new InputValidator(ATTRNAME_ADD, new ArrayList<String>(Arrays.asList(arrAttrName))));
				if (dialog.open() == IDialogConstants.OK_ID) {
					String attrName = dialog.getValue().trim();
					attrNameList.add(attrName);
					mapAttr.put(attrName, new ArrayList<String>());
					attrNameList.setSelection(new String[]{attrName});
					attrValList.removeAll();
				}
			}

			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});

		btnAttrNameEdit.addSelectionListener(new SelectionListener() {

			public void widgetSelected(SelectionEvent e) {
				if (attrNameList.getSelectionCount() <= 0) {
					MessageDialog.openInformation(getShell(),
							Messages.getString("preferencepage.ProjectPropertiesPreferencePage.msgTitle2"),
							Messages.getString("preferencepage.ProjectPropertiesPreferencePage.msg10"));
					return;
				} else if (attrNameList.getSelectionCount() > 1) {
					MessageDialog.openInformation(getShell(),
							Messages.getString("preferencepage.ProjectPropertiesPreferencePage.msgTitle2"),
							Messages.getString("preferencepage.ProjectPropertiesPreferencePage.msg11"));
					return;
				} else {
					int selIndex = attrNameList.getSelectionIndex();
					String selVal = attrNameList.getSelection()[0];
					ArrayList<String> lstAttrVal = mapAttr.get(selVal);
					ArrayList<String> lstAttrName = new ArrayList<String>(Arrays.asList(attrNameList.getItems()));
					lstAttrName.remove(selVal);
					InputDialog dialog = new InputDialog(getShell(), Messages
							.getString("preferencepage.ProjectPropertiesPreferencePage.AddDialog.title5"), Messages
							.getString("preferencepage.ProjectPropertiesPreferencePage.AddDialog.lbl2"), selVal,
							new InputValidator(ATTRNAME_ADD, lstAttrName));
					if (dialog.open() == IDialogConstants.OK_ID) {
						String attrNameVal = dialog.getValue().trim();
						attrNameList.remove(selIndex);
						attrNameList.add(attrNameVal, selIndex);
						attrNameList.select(selIndex);
						mapAttr.remove(selVal);
						mapAttr.put(attrNameVal, lstAttrVal);
					}
				}
			}

			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});

		btnAttrNameDel.addSelectionListener(new SelectionListener() {

			public void widgetSelected(SelectionEvent e) {
				if (attrNameList.getSelectionCount() == 0) {
					MessageDialog.openInformation(getShell(),
							Messages.getString("preferencepage.ProjectPropertiesPreferencePage.msgTitle2"),
							Messages.getString("preferencepage.ProjectPropertiesPreferencePage.msg8"));
					return;
				}
				if (MessageDialog.openConfirm(getShell(),
						Messages.getString("preferencepage.ProjectPropertiesPreferencePage.msgTitle1"),
						Messages.getString("preferencepage.ProjectPropertiesPreferencePage.msg1"))) {
					String[] arrSelName = attrNameList.getSelection();
					for (String attrName : arrSelName) {
						mapAttr.remove(attrName);
					}
					attrNameList.remove(attrNameList.getSelectionIndices());
					if (attrNameList.getSelectionCount() > 0) {
						String strSel = attrNameList.getSelection()[0];
						ArrayList<String> lstAttrVal = mapAttr.get(strSel);
						attrValList.removeAll();
						attrValList.setItems(lstAttrVal.toArray(new String[lstAttrVal.size()]));
					} else {
						attrValList.removeAll();
					}
				}
			}

			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});

		btnAttrValAdd.addSelectionListener(new SelectionListener() {

			public void widgetSelected(SelectionEvent e) {
				if (attrNameList.getSelectionCount() != 1) {
					MessageDialog.openInformation(getShell(),
							Messages.getString("preferencepage.ProjectPropertiesPreferencePage.msgTitle2"),
							Messages.getString("preferencepage.ProjectPropertiesPreferencePage.msg9"));
					return;
				}
				String[] arrAttrVal = attrValList.getItems();
				InputDialog dialog = new InputDialog(getShell(), Messages
						.getString("preferencepage.ProjectPropertiesPreferencePage.AddDialog.title3"), Messages
						.getString("preferencepage.ProjectPropertiesPreferencePage.AddDialog.lbl3"), null,
						new InputValidator(ATTRVAL_ADD, new ArrayList<String>(Arrays.asList(arrAttrVal))));
				if (dialog.open() == IDialogConstants.OK_ID) {
					String attrVal = dialog.getValue().trim();
					attrValList.add(attrVal);
					String attrName = attrNameList.getSelection()[0];
					mapAttr.get(attrName).add(attrVal);
				}
			}

			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});

		btnAttrValEdit.addSelectionListener(new SelectionListener() {

			public void widgetSelected(SelectionEvent e) {
				if (attrValList.getSelectionCount() <= 0) {
					MessageDialog.openInformation(getShell(),
							Messages.getString("preferencepage.ProjectPropertiesPreferencePage.msgTitle2"),
							Messages.getString("preferencepage.ProjectPropertiesPreferencePage.msg10"));
					return;
				} else if (attrValList.getSelectionCount() > 1) {
					MessageDialog.openInformation(getShell(),
							Messages.getString("preferencepage.ProjectPropertiesPreferencePage.msgTitle2"),
							Messages.getString("preferencepage.ProjectPropertiesPreferencePage.msg11"));
					return;
				} else {
					int selIndex = attrValList.getSelectionIndex();
					String selVal = attrValList.getSelection()[0];
					String attrName = attrNameList.getSelection()[0];
					ArrayList<String> lstAttrVal = new ArrayList<String>(Arrays.asList(attrValList.getItems()));
					lstAttrVal.remove(selVal);
					InputDialog dialog = new InputDialog(getShell(), Messages
							.getString("preferencepage.ProjectPropertiesPreferencePage.AddDialog.title6"), Messages
							.getString("preferencepage.ProjectPropertiesPreferencePage.AddDialog.lbl3"), selVal,
							new InputValidator(ATTRVAL_ADD, lstAttrVal));
					if (dialog.open() == IDialogConstants.OK_ID) {
						String attrVal = dialog.getValue().trim();
						attrValList.remove(selIndex);
						attrValList.add(attrVal, selIndex);
						attrValList.select(selIndex);
						lstAttrVal = mapAttr.get(attrName);
						lstAttrVal.set(selIndex, attrVal);
					}
				}
			}

			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});

		btnAttrValDel.addSelectionListener(new SelectionListener() {

			public void widgetSelected(SelectionEvent e) {
				if (attrNameList.getSelectionCount() != 1) {
					MessageDialog.openInformation(getShell(),
							Messages.getString("preferencepage.ProjectPropertiesPreferencePage.msgTitle2"),
							Messages.getString("preferencepage.ProjectPropertiesPreferencePage.msg9"));
					return;
				}
				if (attrValList.getSelectionCount() == 0) {
					MessageDialog.openInformation(getShell(),
							Messages.getString("preferencepage.ProjectPropertiesPreferencePage.msgTitle2"),
							Messages.getString("preferencepage.ProjectPropertiesPreferencePage.msg8"));
					return;
				}
				if (MessageDialog.openConfirm(getShell(),
						Messages.getString("preferencepage.ProjectPropertiesPreferencePage.msgTitle1"),
						Messages.getString("preferencepage.ProjectPropertiesPreferencePage.msg1"))) {
					String attrName = attrNameList.getSelection()[0];
					String[] arrSelVal = attrValList.getSelection();
					mapAttr.get(attrName).removeAll(new ArrayList<String>(Arrays.asList(arrSelVal)));
					attrValList.remove(attrValList.getSelectionIndices());
				}
			}

			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});
	}

	private class InputValidator implements IInputValidator {

		private int type;

		private ArrayList<String> lstValue;

		public InputValidator(int type, ArrayList<String> lstValue) {
			this.type = type;
			this.lstValue = lstValue;
		}

		public String isValid(String newText) {
			if (newText == null || newText.equals("")) {
				return "";
			} else if (newText.trim().equals("")) {
				if (type == FIELD_ADD) {
					return Messages.getString("preferencepage.ProjectPropertiesPreferencePage.msg12");
				} else if (type == ATTRNAME_ADD) {
					return Messages.getString("preferencepage.ProjectPropertiesPreferencePage.msg13");
				} else if (type == ATTRVAL_ADD) {
					return Messages.getString("preferencepage.ProjectPropertiesPreferencePage.msg14");
				}
				return "";
			} else if (newText.trim().length() > 50) {
				return Messages.getString("preferencepage.ProjectPropertiesPreferencePage.msg15");
			} else if (lstValue.contains(newText.trim())) {
				if (type == FIELD_ADD) {
					return Messages.getString("preferencepage.ProjectPropertiesPreferencePage.msg5");
				} else if (type == ATTRNAME_ADD) {
					return Messages.getString("preferencepage.ProjectPropertiesPreferencePage.msg6");
				} else if (type == ATTRVAL_ADD) {
					return Messages.getString("preferencepage.ProjectPropertiesPreferencePage.msg7");
				}
			}
			return null;
		}
	}
}

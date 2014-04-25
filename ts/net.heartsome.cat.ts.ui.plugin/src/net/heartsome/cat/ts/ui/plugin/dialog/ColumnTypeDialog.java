package net.heartsome.cat.ts.ui.plugin.dialog;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import net.heartsome.cat.common.locale.LocaleService;
import net.heartsome.cat.ts.ui.plugin.ColProperties;
import net.heartsome.cat.ts.ui.plugin.resource.Messages;
import net.heartsome.cat.ts.ui.plugin.util.TBXTemplateUtil;
import net.heartsome.xml.vtdimpl.VTDUtils;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ximpleware.AutoPilot;
import com.ximpleware.NavException;
import com.ximpleware.VTDGen;
import com.ximpleware.VTDNav;
import com.ximpleware.XPathEvalException;
import com.ximpleware.XPathParseException;

/**
 * TBXMaker -> 列属性对话框
 * @author peason
 * @version
 * @since JDK1.6
 */
public class ColumnTypeDialog extends Dialog {

	private static final Logger LOGGER = LoggerFactory.getLogger(LoggerFactory.class);

	/** 列数 */
	private int size;

	/** ColProperties 集合 */
	private Vector<ColProperties> colTypes;

	/** Logo 图片路径 */
	private String imgPath;

	/** 语言下拉框集合 */
	private Combo[] arrCmbLangs;

	/** 类型下拉框集合 */
	private Combo[] arrCmbPropsName;

	/** 属性下拉框集合 */
	private Combo[] arrCmbPropsType;

	/** 列类型下拉框集合 */
	private Combo[] arrCmbPropsLevel;

	/** 列类型集合 */
	private String[] levelValues = new String[] { ColProperties.conceptLevel, ColProperties.langLevel };

	/** 列类型为 Concept 的类型集合 */
	private String[] conceptPropValues = new String[] { ColProperties.descripName, ColProperties.noteName };

	/** 列类型为 Tem 的类型集合 */
	private String[] TranslationPropValues = new String[] { ColProperties.termName, ColProperties.termNoteName,
			ColProperties.descripName };

	private String[] conceptPropTypeValues;

	private String[] termDescripPropTypeValues;

	private String[] termTermNotePropTypeValues;

	/** 加载配置按钮 */
	private Button btnLoadConfiguration;

	/** 保存配置按钮 */
	private Button btnSaveConfiguration;

	protected ColumnTypeDialog(Shell parentShell, Vector<ColProperties> colTypes, TBXTemplateUtil template,
			String imgPath) {
		super(parentShell);
		this.colTypes = colTypes;
		size = colTypes.size();
		this.imgPath = imgPath;
		loadPropTypeValue(template);
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(Messages.getString("dialog.ColumnTypeDialog.title"));
		newShell.setImage(new Image(Display.getDefault(), imgPath));
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite tparent = (Composite) super.createDialogArea(parent);
		tparent.setLayout(new GridLayout());
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).hint(750, 400).grab(true, true).applyTo(tparent);
		ScrolledComposite cmpScrolled = new ScrolledComposite(tparent, SWT.V_SCROLL);
		cmpScrolled.setAlwaysShowScrollBars(false);
		cmpScrolled.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL | GridData.FILL_BOTH));
		cmpScrolled.setExpandHorizontal(true);
		cmpScrolled.setShowFocusedControl(true);
		Composite cmpContent = new Composite(cmpScrolled, SWT.None);
		cmpScrolled.setContent(cmpContent);
		cmpContent.setLayout(new GridLayout(5, false));

		arrCmbLangs = new Combo[size];
		arrCmbPropsName = new Combo[size];
		arrCmbPropsType = new Combo[size];
		arrCmbPropsLevel = new Combo[size];

		new Label(cmpContent, SWT.None).setText(Messages.getString("dialog.ColumnTypeDialog.column1"));
		new Label(cmpContent, SWT.None).setText(Messages.getString("dialog.ColumnTypeDialog.column2"));
		new Label(cmpContent, SWT.None).setText(Messages.getString("dialog.ColumnTypeDialog.column3"));
		new Label(cmpContent, SWT.None).setText(Messages.getString("dialog.ColumnTypeDialog.column4"));
		new Label(cmpContent, SWT.None).setText(Messages.getString("dialog.ColumnTypeDialog.column5"));

		for (int i = 0; i < size; i++) {
			ColProperties type = colTypes.get(i);
			new Label(cmpContent, SWT.None).setText(type.getColName() + " : ");

			arrCmbPropsLevel[i] = new Combo(cmpContent, SWT.READ_ONLY);
			arrCmbPropsLevel[i].setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL
					| GridData.FILL_BOTH));

			arrCmbPropsName[i] = new Combo(cmpContent, SWT.READ_ONLY);
			GridData data = new GridData(GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL | GridData.FILL_BOTH);
			data.widthHint = 120;
			arrCmbPropsName[i].setLayoutData(data);

			arrCmbPropsType[i] = new Combo(cmpContent, SWT.READ_ONLY);
			data = new GridData(GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL | GridData.FILL_BOTH);
			data.widthHint = 150;
			arrCmbPropsType[i].setLayoutData(data);

			arrCmbLangs[i] = new Combo(cmpContent, SWT.READ_ONLY);
			arrCmbLangs[i].setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL
					| GridData.FILL_BOTH));

			arrCmbPropsLevel[i].setItems(levelValues);
			arrCmbPropsLevel[i].select(0);

			String propLevel = type.getLevel();
			if (!propLevel.equals("")) { //$NON-NLS-1$
				arrCmbPropsLevel[i].setText(propLevel);
				if (propLevel.equals(ColProperties.conceptLevel)) {
					arrCmbLangs[i].setEnabled(false);
					arrCmbPropsName[i].setItems(conceptPropValues);
					arrCmbPropsName[i].select(0);
					arrCmbPropsType[i].setItems(conceptPropTypeValues);
					arrCmbPropsType[i].select(0);
				}
				if (propLevel.equals(ColProperties.langLevel)) {
					arrCmbLangs[i].setEnabled(true);
					arrCmbPropsName[i].setItems(TranslationPropValues);
					arrCmbPropsName[i].select(0);
					arrCmbPropsType[i].setItems(termDescripPropTypeValues);
					arrCmbPropsType[i].select(0);
				}
			}

			// fixed a bug 2339 by John.
			String propName = type.getPropName();
			if (!propName.equals("")) { //$NON-NLS-1$
				arrCmbPropsName[i].setText(propName);
			}

			// Update content for Prop Type combo
			String propType = type.getPropType();
			if (!propType.equals("")) { //$NON-NLS-1$
				arrCmbPropsType[i].setText(propType);
			}

			if (!propLevel.equals("")) { //$NON-NLS-1$
				if (propLevel.equals(ColProperties.conceptLevel)) {
					arrCmbPropsType[i].setEnabled(propName.equals(ColProperties.descripName));
					arrCmbPropsType[i].setItems(conceptPropTypeValues);
					arrCmbPropsType[i].select(0);
				}
				if (propLevel.equals(ColProperties.langLevel)) {
					arrCmbPropsType[i].setEnabled(!propName.equals(ColProperties.termName));
					if (propName.equals(ColProperties.descripName)) {
						arrCmbPropsType[i].setItems(termDescripPropTypeValues);
					} else {
						arrCmbPropsType[i].setItems(termTermNotePropTypeValues);
					}
					arrCmbPropsType[i].select(0);
				}
			}

			// Update content for Language Combo
			arrCmbLangs[i].setItems(LocaleService.getLanguages());
			arrCmbLangs[i].select(0);
			String lang = type.getLanguage();
			if (!lang.equals("")) { //$NON-NLS-1$
				arrCmbLangs[i].setText(LocaleService.getLanguage(lang));
			}

			final int idx = i;
			arrCmbPropsName[idx].addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					String level = arrCmbPropsLevel[idx].getText();
					String name = arrCmbPropsName[idx].getText();
					if (name.equals(ColProperties.termName)) {
						arrCmbPropsType[idx].setEnabled(false);
						arrCmbPropsType[idx].setItems(conceptPropTypeValues);
						arrCmbPropsType[idx].select(0);
						return;
					}
					if (name.equals(ColProperties.termNoteName)) {
						arrCmbPropsType[idx].setEnabled(true);
						arrCmbPropsType[idx].setItems(termTermNotePropTypeValues);
						arrCmbPropsType[idx].select(0);
						return;
					}
					if (name.equals(ColProperties.noteName)) {
						arrCmbLangs[idx].setEnabled(false);
						arrCmbPropsType[idx].setEnabled(false);
						return;
					}
					if (name.equals(ColProperties.descripName)) {
						arrCmbPropsType[idx].setEnabled(true);
						if (level.equals(ColProperties.conceptLevel)) {
							arrCmbPropsType[idx].setItems(conceptPropTypeValues);
						} else {
							arrCmbPropsType[idx].setItems(termDescripPropTypeValues);
						}
						arrCmbPropsType[idx].select(0);
						return;
					}
					arrCmbPropsType[idx].setEnabled(false);
				}
			});

			arrCmbPropsLevel[idx].addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					String level = arrCmbPropsLevel[idx].getText();
					String name = arrCmbPropsName[idx].getText();
					if (level.equals(ColProperties.conceptLevel)) {
						arrCmbLangs[idx].setEnabled(false);
						arrCmbPropsName[idx].setItems(conceptPropValues);
						arrCmbPropsName[idx].select(0);
						arrCmbPropsType[idx].setEnabled(true);
						arrCmbPropsType[idx].setItems(conceptPropTypeValues);
						arrCmbPropsType[idx].select(0);
					}
					if (level.equals(ColProperties.langLevel)) {
						arrCmbLangs[idx].setEnabled(true);
						arrCmbPropsName[idx].setItems(TranslationPropValues);
						arrCmbPropsName[idx].select(0);
						arrCmbPropsType[idx].setEnabled(false);
						if (name.equals(ColProperties.descripName)) {
							arrCmbPropsType[idx].setItems(termDescripPropTypeValues);
						} else {
							arrCmbPropsType[idx].setItems(termTermNotePropTypeValues);
						}
						arrCmbPropsType[idx].select(0);
					}

				}
			});
		}

		cmpContent.setSize(cmpContent.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		return tparent;
	}

	@Override
	protected boolean isResizable() {
		return true;
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		btnLoadConfiguration = createButton(parent, 22,
				Messages.getString("dialog.ColumnTypeDialog.btnLoadConfiguration"), false);
		btnSaveConfiguration = createButton(parent, 23,
				Messages.getString("dialog.ColumnTypeDialog.btnSaveConfiguration"), false);
		super.createButtonsForButtonBar(parent);
		initListener();
	}

	/**
	 * 初始化加载配置和保存配置按钮的监听 ;
	 */
	private void initListener() {
		btnLoadConfiguration.addSelectionListener(new SelectionListener() {

			public void widgetSelected(SelectionEvent event) {
				FileDialog fd = new FileDialog(getShell(), SWT.OPEN);
				fd.setText(Messages.getString("dialog.ColumnTypeDialog.fdTitle"));
				String extensions[] = { "*.ctc", "*" }; //$NON-NLS-1$ //$NON-NLS-2$
				String[] names = {
						Messages.getString("dialog.ColumnTypeDialog.filterName1"), Messages.getString("dialog.ColumnTypeDialog.filterName2") }; //$NON-NLS-1$ //$NON-NLS-2$
				fd.setFilterExtensions(extensions);
				fd.setFilterNames(names);
				fd.setFilterPath(System.getProperty("user.home"));
				String name = fd.open();
				if (name == null) {
					return;
				}
				try {
					VTDGen vg = new VTDGen();
					if (vg.parseFile(name, true)) {
						VTDNav vn = vg.getNav();
						VTDUtils vu = new VTDUtils(vn);

						AutoPilot ap = new AutoPilot(vn);
						ap.selectXPath("/CSV2TBX-configuration");
						ap.evalXPath();
						int count = vu.getChildElementsCount();
						if (count != arrCmbLangs.length) {
							MessageDialog.openInformation(getShell(),
									Messages.getString("dialog.ColumnTypeDialog.msgTitle"),
									Messages.getString("dialog.ColumnTypeDialog.msg1"));
							return;
						}
						String xpath = "/CSV2TBX-configuration/item";
						ap.selectXPath(xpath);
						int i = 0;
						while (ap.evalXPath() != -1) {
							String propLevel = vu.getCurrentElementAttribut("propLevel", "");
							String propType = vu.getCurrentElementAttribut("propType", ""); //$NON-NLS-1$ //$NON-NLS-2$
							String lang = vu.getCurrentElementAttribut("propLang", ""); //$NON-NLS-1$ //$NON-NLS-2$
							String propName = vu.getCurrentElementAttribut("propName", "");

							arrCmbPropsLevel[i].setItems(levelValues);
							arrCmbPropsLevel[i].select(0);

							// Update contents for Level combo

							if (!propLevel.equals("")) { //$NON-NLS-1$
								arrCmbPropsLevel[i].setText(propLevel);
								if (propLevel.equals(ColProperties.conceptLevel)) {
									arrCmbLangs[i].setEnabled(false);
									arrCmbPropsName[i].setItems(conceptPropValues);
									arrCmbPropsName[i].select(0);
									arrCmbPropsType[i].setItems(conceptPropTypeValues);
									arrCmbPropsType[i].select(0);
								}
								if (propLevel.equals(ColProperties.langLevel)) {
									arrCmbLangs[i].setEnabled(true);
									arrCmbPropsName[i].setItems(TranslationPropValues);
									arrCmbPropsName[i].select(0);
									arrCmbPropsType[i].setItems(termDescripPropTypeValues);
									arrCmbPropsType[i].select(0);
								}
							}

							// Update content for Prop Name combo
							if (!propName.equals("")) { //$NON-NLS-1$
								arrCmbPropsName[i].setText(propName);
							}

							if (!propLevel.equals("")) { //$NON-NLS-1$
								if (propLevel.equals(ColProperties.conceptLevel)) {
									arrCmbPropsType[i].setEnabled(propName.equals(ColProperties.descripName));
									arrCmbPropsType[i].setItems(conceptPropTypeValues);
									arrCmbPropsType[i].select(0);
								}
								if (propLevel.equals(ColProperties.langLevel)) {
									arrCmbPropsType[i].setEnabled(!propName.equals(ColProperties.termName));
									if (propName.equals(ColProperties.descripName)) {
										arrCmbPropsType[i].setItems(termDescripPropTypeValues);
									} else {
										arrCmbPropsType[i].setItems(termTermNotePropTypeValues);
									}
									arrCmbPropsType[i].select(0);
								}
							}

							// Update content for Prop Type combo
							if (!propType.equals("")) { //$NON-NLS-1$
								arrCmbPropsType[i].setText(propType);
							}

							// Update content for Language Combo
							arrCmbLangs[i].setItems(LocaleService.getLanguages());
							arrCmbLangs[i].select(0);
							if (!lang.equals("")) { //$NON-NLS-1$
								arrCmbLangs[i].setText(lang);
							}
							i++;
						}
					}
				} catch (XPathParseException e) {
					LOGGER.error(Messages.getString("dialog.ColumnTypeDialog.logger1"), e);
				} catch (NavException e) {
					LOGGER.error(Messages.getString("dialog.ColumnTypeDialog.logger1"), e);
				} catch (XPathEvalException e) {
					LOGGER.error(Messages.getString("dialog.ColumnTypeDialog.logger1"), e);
				}
			}

			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});

		btnSaveConfiguration.addSelectionListener(new SelectionListener() {

			public void widgetSelected(SelectionEvent e) {
				FileDialog fd = new FileDialog(getShell(), SWT.SAVE);
				fd.setText(Messages.getString("dialog.ColumnTypeDialog.savefdTitle"));
				String extensions[] = { "*.ctc", "*.*" }; //$NON-NLS-1$ //$NON-NLS-2$
				String[] names = {
						Messages.getString("dialog.ColumnTypeDialog.filterName1"), Messages.getString("dialog.ColumnTypeDialog.filterName2") }; //$NON-NLS-1$ //$NON-NLS-2$
				fd.setFilterExtensions(extensions);
				fd.setFilterNames(names);
				fd.setFilterPath(System.getProperty("user.home"));
				String name = fd.open();
				if (name == null) {
					return;
				}
				try {
					FileOutputStream output = new FileOutputStream(name);
					output.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n".getBytes("UTF-8")); //$NON-NLS-1$ //$NON-NLS-2$
					output.write("<CSV2TBX-configuration>\n".getBytes("UTF-8")); //$NON-NLS-1$ //$NON-NLS-2$
					for (int i = 0; i < arrCmbLangs.length; i++) {
						String strItem = "<item propLang=\"" + arrCmbLangs[i].getText() //$NON-NLS-1$
								+ "\" propName=\"" + arrCmbPropsName[i].getText() //$NON-NLS-1$
								+ "\" propType=\"" + arrCmbPropsType[i].getText() //$NON-NLS-1$
								+ "\" propLevel=\"" + arrCmbPropsLevel[i].getText() //$NON-NLS-1$
								+ "\"/>\n"; //$NON-NLS-1$
						output.write(strItem.getBytes("UTF-8")); //$NON-NLS-1$
					}
					output.write("</CSV2TBX-configuration>\n".getBytes("UTF-8")); //$NON-NLS-1$ //$NON-NLS-2$
					output.close();
				} catch (FileNotFoundException e1) {
					LOGGER.error(Messages.getString("dialog.ColumnTypeDialog.logger2"), e);
				} catch (UnsupportedEncodingException e1) {
					LOGGER.error(Messages.getString("dialog.ColumnTypeDialog.logger2"), e);
				} catch (IOException e1) {
					LOGGER.error(Messages.getString("dialog.ColumnTypeDialog.logger2"), e);
				}
			}

			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});
	}

	/**
	 * 根据提供的模板加载各个属性值
	 * @param template
	 *            ;
	 */
	private void loadPropTypeValue(TBXTemplateUtil template) {
		Vector<String> values = new Vector<String>();
		for (int i = 0; i < template.getItemCount(); i++) {
			String itemLevels = template.getItemLevels(i);
			String specName = template.getSpecName(i);
			if ((itemLevels.indexOf(TBXTemplateUtil.lsLevel) >= 0 || itemLevels.trim().equals("")) && //$NON-NLS-1$
					specName.equals(TBXTemplateUtil.descripSpec)) {

				values.add(template.getItemDescription(i));
			}
		}
		termDescripPropTypeValues = new String[values.size()];
		for (int i = 0; i < termDescripPropTypeValues.length; i++) {
			termDescripPropTypeValues[i] = values.get(i);
		}

		values = new Vector<String>();
		for (int i = 0; i < template.getItemCount(); i++) {
			String itemLevels = template.getItemLevels(i);
			String specName = template.getSpecName(i);
			if ((itemLevels.indexOf(TBXTemplateUtil.lsLevel) >= 0 || itemLevels.trim().equals("")) && //$NON-NLS-1$
					specName.equals(TBXTemplateUtil.termNoteSpec)) {

				values.add(template.getItemDescription(i));
			}
		}

		termTermNotePropTypeValues = new String[values.size()];
		for (int i = 0; i < termTermNotePropTypeValues.length; i++) {
			termTermNotePropTypeValues[i] = values.get(i);
		}

		values = new Vector<String>();
		for (int i = 0; i < template.getItemCount(); i++) {
			String itemLevels = template.getItemLevels(i);
			String specName = template.getSpecName(i);
			if ((itemLevels.indexOf(TBXTemplateUtil.teLevel) >= 0 || itemLevels.trim().equals("")) && //$NON-NLS-1$
					specName.equals(TBXTemplateUtil.descripSpec)) {

				values.add(template.getItemDescription(i));
			}
		}

		conceptPropTypeValues = new String[values.size()];
		for (int i = 0; i < conceptPropTypeValues.length; i++) {
			conceptPropTypeValues[i] = values.get(i);
		}

	}

	private String[] getUserLangs() {
		Hashtable<String, String> langTable = new Hashtable<String, String>();
		for (int c = 0; c < size; c++) {
			String propLevel1 = arrCmbPropsLevel[c].getText();
			if (propLevel1.equals(ColProperties.langLevel)) {
				langTable.put(LocaleService.getLanguageCodeByLanguage(arrCmbLangs[c].getText()),
						LocaleService.getLanguageCodeByLanguage(arrCmbLangs[c].getText()));
			}
		}

		String[] result = new String[langTable.size()];
		Enumeration<String> keys = langTable.keys();
		int index = 0;
		while (keys.hasMoreElements()) {
			result[index++] = keys.nextElement();
		}
		return result;
	}

	public boolean verifyColProperties() {
		// Verify Unknown Properties
		for (int i = 0; i < size; i++) {
			String propName = arrCmbPropsName[i].getText();
			if (propName.equals("")) { //$NON-NLS-1$
				MessageDialog.openInformation(getShell(), Messages.getString("dialog.ColumnTypeDialog.msgTitle"),
						Messages.getString("dialog.ColumnTypeDialog.msg2") + propName);
				return false;
			}
		}

		// Verify duplicated columns
		for (int i = 0; i < size; i++) {
			String lang1 = LocaleService.getLanguageCodeByLanguage(arrCmbLangs[i].getText());
			String propName1 = arrCmbPropsName[i].getText();
			String propType1 = arrCmbPropsType[i].getText();
			String level1 = arrCmbPropsLevel[i].getText();
			if (level1.equals(ColProperties.conceptLevel)) {
				continue;
			}
			for (int j = 0; j < size; j++) {
				if (i == j) {
					continue;
				}
				String lang2 = LocaleService.getLanguageCodeByLanguage(arrCmbLangs[j].getText());
				String propName2 = arrCmbPropsName[j].getText();
				String propType2 = arrCmbPropsType[j].getText();
				String level2 = arrCmbPropsLevel[i].getText();
				if (level2.equals(ColProperties.conceptLevel)) {
					continue;
				}

				if (lang1.equals(lang2)) {
					if (propName1.equals(propName2) && propType1.equals(propType2)) {
						MessageDialog.openInformation(getShell(),
								Messages.getString("dialog.ColumnTypeDialog.msgTitle"),
								Messages.getString("dialog.ColumnTypeDialog.msg3") + lang1);
						return false;
					}
				}
			}
		}

		// Verify That a Term was defined for each language
		String[] langs = getUserLangs();

		for (int l = 0; l < langs.length; l++) {
			boolean existTerm = false;
			for (int i = 0; i < size; i++) {
				String lang1 = LocaleService.getLanguageCodeByLanguage(arrCmbLangs[i].getText());
				String propName1 = arrCmbPropsName[i].getText();

				if (lang1.equals(langs[l]) && propName1.equals(ColProperties.termName)) {
					existTerm = true;
					break;
				}
			}
			if (!existTerm) {
				MessageDialog.openInformation(getShell(), Messages.getString("dialog.ColumnTypeDialog.msgTitle"),
						Messages.getString("dialog.ColumnTypeDialog.msg4") + langs[l]);
				return false;
			}
		}
		return true;
	}

	@Override
	protected void okPressed() {
		if (!verifyColProperties()) {
			return;
		}
		for (int i = 0; i < size; i++) {
			ColProperties type = colTypes.get(i);
			String propLevel = arrCmbPropsLevel[i].getText();
			String lang = LocaleService.getLanguageCodeByLanguage(arrCmbLangs[i].getText());
			String propName = arrCmbPropsName[i].getText();
			String propType = arrCmbPropsType[i].getText();
			type.setColumnType(propLevel, lang, propName, propType);
		}
		close();
	}
}

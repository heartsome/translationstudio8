package net.heartsome.cat.ts.ui.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

import net.heartsome.cat.common.ui.dialog.HsPreferenceDialog;
import net.heartsome.cat.common.util.CommonFunction;
import net.heartsome.cat.ts.ui.Activator;
import net.heartsome.cat.ts.ui.preferencepage.SystemPreferencePage;
import net.heartsome.cat.ts.ui.preferencepage.colors.ColorsPreferencePage;
import net.heartsome.cat.ts.ui.preferencepage.languagecode.LanguageCodesPreferencePage;
import net.heartsome.cat.ts.ui.preferencepage.translation.TranslationPreferencePage;
import net.heartsome.cat.ts.ui.resource.Messages;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.preference.IPreferenceNode;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceLabelProvider;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.util.Util;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.osgi.service.datalocation.Location;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IWorkbenchWindow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ximpleware.AutoPilot;
import com.ximpleware.NavException;
import com.ximpleware.VTDGen;
import com.ximpleware.VTDNav;
import com.ximpleware.XPathEvalException;
import com.ximpleware.XPathParseException;

/**
 * 获取首选项值的工具类
 * @author peason
 * @version
 * @since JDK1.6
 */
public final class PreferenceUtil {

	/**
	 * 获取项目属性的文本字段
	 * @return ;
	 */
	public static ArrayList<String> getProjectFieldList() {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		ArrayList<String> lstField = new ArrayList<String>();
		int fieldCount = store
				.getInt("net.heartsome.cat.ts.ui.preferencepage.ProjectPropertiesPreferencePage.fieldCount");
		if (fieldCount > 0) {
			for (int i = 0; i < fieldCount; i++) {
				lstField.add(store
						.getString("net.heartsome.cat.ts.ui.preferencepage.ProjectPropertiesPreferencePage.field" + i));
			}
		}
		// 对中文按拼音排序
		Collator collatorChinese = Collator.getInstance(java.util.Locale.CHINA);
		Collections.sort(lstField, collatorChinese);
		return lstField;
	}

	/**
	 * 获取项目属性的属性字段
	 * @return key 为属性名称，value 为属性值集合
	 */
	public static LinkedHashMap<String, ArrayList<String>> getProjectAttributeMap() {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		HashMap<String, ArrayList<String>> mapAttr = new HashMap<String, ArrayList<String>>();
		int attrNameCount = store
				.getInt("net.heartsome.cat.ts.ui.preferencepage.ProjectPropertiesPreferencePage.attrNameCount");
		// 对中文按拼音排序
		final Collator collatorChinese = Collator.getInstance(java.util.Locale.CHINA);
		LinkedHashMap<String, ArrayList<String>> linkedMapAttr = new LinkedHashMap<String, ArrayList<String>>();
		if (attrNameCount > 0) {
			for (int i = 0; i < attrNameCount; i++) {
				String strAttrName = store
						.getString("net.heartsome.cat.ts.ui.preferencepage.ProjectPropertiesPreferencePage.attrName"
								+ i);
				int attrValCount = store
						.getInt("net.heartsome.cat.ts.ui.preferencepage.ProjectPropertiesPreferencePage.attrName" + i
								+ ".count");
				ArrayList<String> lstAttrVal = new ArrayList<String>();
				if (attrValCount > 0) {
					for (int j = 0; j < attrValCount; j++) {
						lstAttrVal
								.add(store
										.getString("net.heartsome.cat.ts.ui.preferencepage.ProjectPropertiesPreferencePage.attrName"
												+ i + ".attrVal" + j));
					}
				}
				Collections.sort(lstAttrVal, collatorChinese);
				mapAttr.put(strAttrName, lstAttrVal);
			}
			List<Entry<String, ArrayList<String>>> lstAttr = new ArrayList<Entry<String, ArrayList<String>>>(
					mapAttr.entrySet());
			Collections.sort(lstAttr, new Comparator<Entry<String, ArrayList<String>>>() {

				public int compare(Entry<String, ArrayList<String>> arg0, Entry<String, ArrayList<String>> arg1) {
					return collatorChinese.compare(arg0.getKey(), arg1.getKey());
				}
			});

			for (Entry<String, ArrayList<String>> entry : lstAttr) {
				linkedMapAttr.put(entry.getKey(), entry.getValue());
			}
		}

		return linkedMapAttr;
	}

	private static final Logger LOGGER = LoggerFactory.getLogger(PreferenceUtil.class);

	public static void openPreferenceDialog(IWorkbenchWindow window, final String defaultId) {
		PreferenceManager mgr = window.getWorkbench().getPreferenceManager();
		mgr.remove("net.heartsome.cat.ui.preferencePages.Perspectives");
		mgr.remove("org.eclipse.ui.preferencePages.Workbench");
		mgr.remove("org.eclipse.update.internal.ui.preferences.MainPreferencePage");
		mgr.remove("org.eclipse.help.ui.browsersPreferencePage");
		if (CommonFunction.checkEdition("L")) {
			List<String> lstNodeId = new ArrayList<String>();
			lstNodeId.add(SystemPreferencePage.ID);
			lstNodeId.add(LanguageCodesPreferencePage.ID);
			lstNodeId.add(ColorsPreferencePage.ID);
			lstNodeId.add("org.eclipse.ui.preferencePages.Keys");
			@SuppressWarnings("unchecked")
			List<IPreferenceNode> lstNodes = mgr.getElements(PreferenceManager.PRE_ORDER);
			for (IPreferenceNode node : lstNodes) {
				if (!lstNodeId.contains(node.getId())) {
					mgr.remove(node);
				}
			}
		}

		final Object[] defaultNode = new Object[1];
		HsPreferenceDialog dlg = new HsPreferenceDialog(window.getShell(), mgr);
		dlg.create();

		final List<Image> imageList = new ArrayList<Image>();
		dlg.getTreeViewer().setLabelProvider(new PreferenceLabelProvider() {
			public Image getImage(Object element) {
				String id = ((IPreferenceNode) element).getId();
				if (defaultId != null && id.equals(defaultId)) {
					defaultNode[0] = element;
				}
				Image image = null;
				if (SystemPreferencePage.ID.equals(id)) {
					// 系统菜单
					image = Activator.getImageDescriptor("images/preference/system/system.png").createImage();
					imageList.add(image);
					return image;
				} else if (LanguageCodesPreferencePage.ID.equals(id)) {
					// 系统 > 语言代码菜单
					image = Activator.getImageDescriptor("images/preference/system/language.png").createImage();
					imageList.add(image);
					return image;
				} else if (ColorsPreferencePage.ID.equals(id)) {
					// 系统 > 颜色菜单
					image = Activator.getImageDescriptor("images/preference/system/color.png").createImage();
					imageList.add(image);
					return image;
				} else if ("net.heartsome.cat.ts.ui.qa.preference.SpellPage".equals(id)) {
					// 品质检查 > 内置词典菜单
					image = Activator.getImageDescriptor("images/preference/system/dictionary-in.png").createImage();
					imageList.add(image);
					return image;
				} else if ("org.eclipse.ui.preferencePages.Keys".equals(id)) {
					// 系统 > 快捷键菜单
					image = Activator.getImageDescriptor("images/preference/system/keys.png").createImage();
					imageList.add(image);
					return image;
				} else if ("org.eclipse.ui.net.proxy_preference_page_context".equals(id)) {
					// 网络连接
					image = Activator.getImageDescriptor("images/preference/system/network.png").createImage();
					imageList.add(image);
					return image;
				} else if ("net.heartsome.cat.ts.ui.qa.preference.QAPage".equals(id)) {
					// 品质检查菜单
					image = Activator.getImageDescriptor("images/preference/qa/qa.png").createImage();
					imageList.add(image);
					return image;
				} else if ("net.heartsome.cat.ts.ui.qa.preference.NonTranslationQAPage".equals(id)) {
					// 品质检查 > 非译元素菜单
					image = Activator.getImageDescriptor("images/preference/qa/not-trans-set.png").createImage();
					imageList.add(image);
					return image;
				} else if ("net.heartsome.cat.ts.ui.qa.preference.QAInstalPage".equals(id)) {
					// 品质检查 > 批量检查设置菜单
					image = Activator.getImageDescriptor("images/preference/qa/check-set.png").createImage();
					imageList.add(image);
					return image;
				} else if ("net.heartsome.cat.ts.ui.qa.preference.FileAnalysisInstalPage".equals(id)) {
					// 文件分析
					image = Activator.getImageDescriptor("images/preference/qa/fileAnalysis.png").createImage();
					imageList.add(image);
					return image;
				} else if ("net.heartsome.cat.ts.ui.qa.preference.EquivalentPage".equals(id)) {
					// 文件分析 -- ＞ 加权系数设置
					image = Activator.getImageDescriptor("images/preference/tm/equal-parameter.png").createImage();
					imageList.add(image);
					return image;
				} else if (TranslationPreferencePage.ID.equals(id)) {
					// 翻译菜单
					image = Activator.getImageDescriptor("images/preference/translate/translation.png").createImage();
					imageList.add(image);
					return image;
				} else if ("net.heartsome.cat.ts.pretranslation.preferencepage".equals(id)) {
					// 预翻译
					image = Activator.getImageDescriptor("images/preference/translate/pre-translation.png")
							.createImage();
					imageList.add(image);
					return image;
				} else if ("net.heartsome.cat.ts.machinetranslation.prefrence.MachineTranslationPreferencePage".equals(id)) {
					// google
					image = Activator.getImageDescriptor("images/preference/translate/pre-translation.png")
							.createImage();
					imageList.add(image);
					return image;
				} else if ("net.heartsome.cat.ts.bingtrans.preferencepage".equals(id)) {
					// bing
					image = Activator.getImageDescriptor("images/preference/translate/bing-translation.png")
							.createImage();
					imageList.add(image);
					return image;
				} else if ("net.heartsome.cat.database.ui.tm.preference.tmpage".equals(id)) {
					// 记忆库
					image = Activator.getImageDescriptor("images/preference/tm/tm-db.png").createImage();
					imageList.add(image);
					return image;
				} else if ("net.heartsome.cat.database.ui.tb.preference.tbpage".equals(id)) {
					// 术语库菜单
					image = Activator.getImageDescriptor("images/preference/tb/tb-db.png").createImage();
					imageList.add(image);
					return image;
				} else if ("net.heartsome.cat.convert.ui.preference.FileTypePreferencePage".equals(id)) {
					// 文件类型
					image = Activator.getImageDescriptor("images/preference/file/documents.png").createImage();
					imageList.add(image);
					return image;
				} else if ("net.heartsome.cat.converter.msexcel2007.preference.ExcelPreferencePage".equals(id)) {
					// Microsoft Excel 2007
					image = Activator.getImageDescriptor("images/preference/file/excel_16.png").createImage();
					imageList.add(image);
					return image;
				} else if ("net.heartsome.cat.converter.pptx.preference.PPTXPreferencePage".equals(id)) {
					// Microsoft PowerPoint 2007
					image = Activator.getImageDescriptor("images/preference/file/powerpoint_16.png").createImage();
					imageList.add(image);
					return image;
				} else if ("net.heartsome.cat.converter.mif.preference.FrameMakerPreferencePage".equals(id)) {
					// Adobe FrameMaker
					image = Activator.getImageDescriptor("images/preference/file/framemaker_16.png").createImage();
					imageList.add(image);
					return image;
				} else if ("net.heartsome.cat.ts.ui.preferencepage.ProjectPropertiesPreferencePage".equals(id)) {
					// 项目属性
					image = Activator.getImageDescriptor("images/preference/projectProperties.png").createImage();
					imageList.add(image);
					return image;
				}else if ("net.heartsome.cat.ts.websearch.ui.preference.WebSearchPreferencePage".equals(id)) {
					// 项目属性
					image = Activator.getImageDescriptor("images/websearch/websearch16.png").createImage();
					imageList.add(image);
					return image;
				} else {
					return null;
				}
			}
		});

		if (defaultNode[0] != null) {
			dlg.getTreeViewer().setSelection(new StructuredSelection(defaultNode), true);
			dlg.getTreeViewer().getControl().setFocus();
		}

		// 修改BUG 2764， 品质检查--选项弹出的对话框有截断 robert 2012-12-03
		if ("net.heartsome.cat.ts.ui.qa.preference.QAPage".equals(defaultId)) {
			if (dlg.getShell().getSize().x < 860 || dlg.getShell().getSize().y < 716) {
				dlg.getShell().setSize(860, 716);
			}
		} else if ("net.heartsome.cat.ts.ui.preferencepage.ProjectPropertiesPreferencePage".equals(defaultId)) {
			int x = Util.isLinux() ? 839 : 908;
			int y = Util.isLinux() ? 709 : 557;
			dlg.getShell().setSize(Math.max(dlg.getShell().getSize().x, x), Math.max(dlg.getShell().getSize().y, y));
		}
		dlg.open();

		// 清理资源
		for (Image img : imageList) {
			if (img != null && !img.isDisposed()) {
				img.dispose();
			}
		}
		imageList.clear();
	}

	public static void initProductEdition() {
		System.getProperties().put("TSVersion", "89");
		Location configArea = Platform.getInstallLocation();
		if (configArea == null) {
			System.getProperties().put("TSEdition", "");
			System.getProperties().put("TSVersion", "");
			return;
		}

		URL location = null;
		try {
			VTDGen vg = new VTDGen();
			AutoPilot ap = new AutoPilot();
			location = new URL(configArea.getURL().toExternalForm() + "features");
			String featureFolderName = location.getFile();
			File featureFolder = new File(featureFolderName);
			// 遍历 features 目录
			if (featureFolder.isDirectory()) {
				boolean isU = false;
				boolean isF = false;
				boolean isP = false;
				boolean isL = false;
				String strUVersion = null;
				String strFVersion = null;
				String strPVersion = null;
				String strLVersion = null;

				for (File f : featureFolder.listFiles()) {
					String name = f.getName();
					if (name.startsWith("net.heartsome.cat.ts.edition_")) {
						if (vg.parseFile(f.getAbsolutePath() + File.separator + "feature.xml", true)) {
							VTDNav vn = vg.getNav();
							ap.bind(vn);
							ap.selectXPath("/feature");
							if (ap.evalXPath() != -1) {
								int idIndex = vn.getAttrVal("id");
								int versionIndex = vn.getAttrVal("version");
								if (idIndex == -1 || versionIndex == -1) {
									System.getProperties().put("TSEdition", "");
									return;
								}
								String id = vn.toRawString(idIndex);
								String version = vn.toRawString(versionIndex);
								if (name.equals(id + "_" + version)) {
									// 判断是哪个版本 精简：L(lite) 个人：P(personal) 专业：F(Professional) 旗舰：U(Ultimate)
									// 由于 feature 可以包含，因此要遍历所有以 net.heartsome.cat.ts.edition_ 开头的目录，找到版本最高的
									if (id.equals("net.heartsome.cat.ts.edition_ultimate.feature")) {
										isU = true;
										strUVersion = version;
										break;
									} else if (id.equals("net.heartsome.cat.ts.edition_professional.feature")) {
										isF = true;
										strFVersion = version;
										continue;
									} else if (id.equals("net.heartsome.cat.ts.edition_personal.feature")) {
										isP = true;
										strPVersion = version;
										continue;
									} else if (id.equals("net.heartsome.cat.ts.edition_lite.feature")) {
										isL = true;
										strLVersion = version;
										continue;
									}
								} else {
									System.getProperties().put("TSEdition", "");
									return;
								}
							} else {
								System.getProperties().put("TSEdition", "");
							}
						} else {
							System.getProperties().put("TSEdition", "");
						}
					}
				}

				if (isU) {
					System.getProperties().put("TSEdition", "U");
					System.getProperties().put("TSVersionDate", strUVersion);
				} else if (isF) {
					System.getProperties().put("TSEdition", "F");
					System.getProperties().put("TSVersionDate", strFVersion);
				} else if (isP) {
					System.getProperties().put("TSEdition", "P");
					System.getProperties().put("TSVersionDate", strPVersion);
				} else if (isL) {
					System.getProperties().put("TSEdition", "L");
					System.getProperties().put("TSVersionDate", strLVersion);
				} else {
					System.getProperties().put("TSEdition", "");
					System.getProperties().put("TSVersionDate", "");
				}
			} else {
				System.getProperties().put("TSEdition", "");
			}

			location = new URL(configArea.getURL().toExternalForm() + "plugins");
			String pluginsFolderName = location.getFile();
			File pluginsFolder = new File(pluginsFolderName);
			// 遍历 plugins 目录
			if (pluginsFolder.isDirectory()) {
				List<String> lstPluginName = new ArrayList<String>();
				for (File f : pluginsFolder.listFiles()) {
					String name = f.getName();
					if (name.endsWith(".jar")
							&& (name.startsWith("net.heartsome.cat.ts.ui.plugin_")
									|| name.startsWith("net.heartsome.cat.ts.ui.advanced_")
									|| name.startsWith("net.heartsome.cat.ts.fuzzyTranslation_")
									|| name.startsWith("net.heartsome.cat.converter.ui_")
									|| name.startsWith("net.heartsome.cat.ts.ui.docx_")
									|| name.startsWith("net.heartsome.cat.ts.ui.qa_")
									|| name.startsWith("net.heartsome.cat.database.ui_")
									|| name.startsWith("net.heartsome.cat.database.hsql_")
									|| name.startsWith("net.heartsome.cat.database.oracle_")
									|| name.startsWith("net.heartsome.cat.ts.importproject_")
									|| name.startsWith("net.heartsome.cat.ts.exportproject_")
									|| name.startsWith("net.heartsome.cat.ts.handlexlf_")
									|| name.startsWith("net.heartsome.cat.ts.lockrepeat_") || name
										.startsWith("net.heartsome.cat.ts.jumpsegment_"))) {
						String pluginName = name.substring(0, name.indexOf("_"));
						// 更新后原来的插件会保留在 plugins 目录下，应此 lstPluginName 会有重复添加 pluginName 的情况，所以在此处添加判断。
						if (!lstPluginName.contains(pluginName)) {
							lstPluginName.add(pluginName);
						}
					}
				}
				String edition = System.getProperty("TSEdition");
				if (lstPluginName.size() == 14 && lstPluginName.indexOf("net.heartsome.cat.ts.ui.plugin") != -1
						&& lstPluginName.indexOf("net.heartsome.cat.ts.ui.advanced") != -1
						&& lstPluginName.indexOf("net.heartsome.cat.ts.fuzzyTranslation") != -1
						&& lstPluginName.indexOf("net.heartsome.cat.converter.ui") != -1
						&& lstPluginName.indexOf("net.heartsome.cat.ts.ui.docx") != -1
						&& lstPluginName.indexOf("net.heartsome.cat.ts.ui.qa") != -1
						&& lstPluginName.indexOf("net.heartsome.cat.database.ui") != -1
						&& lstPluginName.indexOf("net.heartsome.cat.database.hsql") != -1
						&& lstPluginName.indexOf("net.heartsome.cat.database.oracle") != -1
						&& lstPluginName.indexOf("net.heartsome.cat.ts.importproject") != -1
						&& lstPluginName.indexOf("net.heartsome.cat.ts.exportproject") != -1
						&& lstPluginName.indexOf("net.heartsome.cat.ts.handlexlf") != -1
						&& lstPluginName.indexOf("net.heartsome.cat.ts.lockrepeat") != -1
						&& lstPluginName.indexOf("net.heartsome.cat.ts.jumpsegment") != -1) {
					if (!(edition != null && (edition.equals("U") || edition.equals("F")))) {
						System.getProperties().put("TSEdition", "");
					}
				} else if (lstPluginName.size() == 6 && lstPluginName.indexOf("net.heartsome.cat.converter.ui") != -1
						&& lstPluginName.indexOf("net.heartsome.cat.ts.ui.qa") != -1
						&& lstPluginName.indexOf("net.heartsome.cat.database.ui") != -1
						&& lstPluginName.indexOf("net.heartsome.cat.database.hsql") != -1
						&& lstPluginName.indexOf("net.heartsome.cat.ts.importproject") != -1
						&& lstPluginName.indexOf("net.heartsome.cat.ts.jumpsegment") != -1) {
					if (!(edition != null && edition.equals("P"))) {
						System.getProperties().put("TSEdition", "");
					}
				} else if (lstPluginName.size() == 0) {
					if (!(edition != null && edition.equals("L"))) {
						System.getProperties().put("TSEdition", "");
					}
				} else {
					System.getProperties().put("TSEdition", "");
				}
			} else {
				System.getProperties().put("TSEdition", "");
			}

			// if (System.getProperty("TSEdition").equals("")) {
			// return;
			// }
			// String product = Platform.getProduct().getName();
			// if (Util.isMac()) {
			// location = new URL(configArea.getURL().toExternalForm() + product + ".app" + File.separator
			// + "Contents" + File.separator + "MacOS" + File.separator + product + ".ini");
			// } else {
			// location = new URL(configArea.getURL().toExternalForm() + product + ".ini");
			// }
			// String fileName = location.getFile();
			// BufferedReader in = new BufferedReader(new FileReader(fileName));
			// String line = null;
			// String tsVersion = null;
			// String tsSerial = null;
			// while ((line = in.readLine()) != null) {
			// if (line.startsWith("TSProductVersion")) {
			// tsVersion = line.substring(line.indexOf("=") + 1);
			// }
			// if (line.startsWith("TSProductSerialNumber")) {
			// tsSerial = line.substring(line.indexOf("=") + 1);
			// }
			// }
			// if (tsVersion != null && tsSerial != null && tsVersion.length() == 16 && tsSerial.length() == 16) {
			// String edition = System.getProperty("TSEdition");
			// int[] arrValue = new int[16];
			// for (int i = 0; i < tsVersion.length(); i++) {
			// arrValue[i] = Integer.parseInt(Character.toString(tsVersion.charAt(i)));
			// }
			// int[] arrSerialNum = new int[16];
			// for (int i = 0; i < tsSerial.length(); i++) {
			// arrSerialNum[i] = Integer.parseInt(Character.toString(tsSerial.charAt(i)));
			// }
			// String strFullChar = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
			// boolean isU = ((arrValue[1] + arrValue[8]) * (arrValue[7] - arrValue[14])) == (strFullChar.indexOf("U") +
			// 1);// 验证旗舰版的规则
			// boolean isF = arrValue[4] != 0
			// && (arrValue[2] - arrValue[9] + arrValue[15]) / arrValue[4] == (strFullChar.indexOf("F") + 1);
			// boolean isP = arrValue[5] * (arrValue[3] + arrValue[10] + arrValue[12]) == (strFullChar.indexOf("P") +
			// 1);
			// boolean isL = (arrValue[0] + arrValue[6] + arrValue[11] + arrValue[13]) == (strFullChar.indexOf("L") +
			// 1);
			//
			// boolean isUVersion = arrSerialNum[12] + arrSerialNum[13] == 8 && arrSerialNum[2] + arrSerialNum[3] ==
			// 9;// 验证旗舰版是否满足
			// boolean isFVersion = arrSerialNum[6] * arrSerialNum[15] == 8 && arrSerialNum[2] * arrSerialNum[5] == 9;//
			// 验证专业版是否满足
			// boolean isPVersion = arrSerialNum[3] * arrSerialNum[4] == 8 && arrSerialNum[0] * arrSerialNum[13] == 9;//
			// 验证个人版是否满足
			// boolean isLVersion = arrSerialNum[0] * arrSerialNum[3] == 8 && arrSerialNum[1] == 9;// 验证精简版是否满足
			// if (edition.equals("U")) {
			// // 验证 TSEdition
			// if (!isU) {
			// System.getProperties().put("TSEdition", "");
			// } else {
			// if (isF) {
			// System.getProperties().put("TSEdition", "");
			// } else if (isP) {
			// System.getProperties().put("TSEdition", "");
			// } else if (isL) {
			// System.getProperties().put("TSEdition", "");
			// }
			// }
			// // 验证序列号
			// if (isUVersion) {
			// if (!(isFVersion) && !(isPVersion) && !(isLVersion)) {
			// System.getProperties().put("TSVersion", "89");
			// } else {
			// System.getProperties().put("TSVersion", "");
			// }
			// } else {
			// System.getProperties().put("TSVersion", "");
			// }
			// } else if (edition.equals("F")) {
			// if (!isF) {
			// System.getProperties().put("TSEdition", "");
			// } else {
			// if (isU) {
			// System.getProperties().put("TSEdition", "");
			// } else if (isP) {
			// System.getProperties().put("TSEdition", "");
			// } else if (isL) {
			// System.getProperties().put("TSEdition", "");
			// }
			// }
			// if (isFVersion) {
			// if (!(isUVersion) && !(isPVersion) && !(isLVersion)) {
			// System.getProperties().put("TSVersion", "89");
			// } else {
			// System.getProperties().put("TSVersion", "");
			// }
			// } else {
			// System.getProperties().put("TSVersion", "");
			// }
			// } else if (edition.equals("P")) {
			// if (!isP) {
			// System.getProperties().put("TSEdition", "");
			// } else {
			// if (isU) {
			// System.getProperties().put("TSEdition", "");
			// } else if (isF) {
			// System.getProperties().put("TSEdition", "");
			// } else if (isL) {
			// System.getProperties().put("TSEdition", "");
			// }
			// }
			// if (isPVersion) {
			// if (!(isUVersion) && !(isFVersion) && !(isLVersion)) {
			// System.getProperties().put("TSVersion", "89");
			// } else {
			// System.getProperties().put("TSVersion", "");
			// }
			// } else {
			// System.getProperties().put("TSVersion", "");
			// }
			// } else if (edition.equals("L")) {
			// if (!isL) {
			// System.getProperties().put("TSEdition", "");
			// } else {
			// if (isU) {
			// System.getProperties().put("TSEdition", "");
			// } else if (isF) {
			// System.getProperties().put("TSEdition", "");
			// } else if (isP) {
			// System.getProperties().put("TSEdition", "");
			// }
			// }
			// if (isLVersion) {
			// if (!(isUVersion) && !(isFVersion) && !(isPVersion)) {
			// System.getProperties().put("TSVersion", "89");
			// } else {
			// System.getProperties().put("TSVersion", "");
			// }
			// } else {
			// System.getProperties().put("TSVersion", "");
			// }
			// }
			// } else {
			// System.getProperties().put("TSEdition", "");
			// System.getProperties().put("TSVersion", "");
			// }
		} catch (MalformedURLException e) {
			e.printStackTrace();
			LOGGER.error(Messages.getString("preference.PreferenceUtil.logger1"), e);
		} catch (XPathEvalException e) {
			e.printStackTrace();
			LOGGER.error(Messages.getString("preference.PreferenceUtil.logger1"), e);
		} catch (NavException e) {
			e.printStackTrace();
			LOGGER.error(Messages.getString("preference.PreferenceUtil.logger1"), e);
		} catch (XPathParseException e) {
			e.printStackTrace();
			LOGGER.error(Messages.getString("preference.PreferenceUtil.logger1"), e);
		}
	}

	/**
	 * 检查 osgi.clean 的值，如果为 true，就改为 false
	 * @param locale
	 *            ;
	 */
	public static void checkCleanValue() {
		Location configArea = Platform.getInstallLocation();
		if (configArea == null) {
			return;
		}

		URL location = null;
		try {
			location = new URL(configArea.getURL().toExternalForm() + "configuration" + File.separator + "config.ini");
		} catch (MalformedURLException e) {
			// This should never happen
		}

		try {
			String fileName = location.getFile();
			File file = new File(fileName);
			fileName += ".bak";
			file.renameTo(new File(fileName));
			BufferedReader in = new BufferedReader(new FileReader(fileName));
			BufferedWriter out = null;
			boolean isFind = false;
			try {
				String line = in.readLine();
				StringBuffer sbOut = new StringBuffer();
				while (line != null) {
					if (line.trim().equals("osgi.clean=true")) {
						sbOut.append("osgi.clean=false");
						isFind = true;
					} else {
						sbOut.append(line);
					}
					sbOut.append("\n");
					line = in.readLine();
				}
				if (isFind) {
					out = new BufferedWriter(new FileWriter(location.getFile()));
					out.write(sbOut.toString());
					out.flush();
				}
			} finally {
				if (in != null) {
					try {
						in.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				if (out != null) {
					try {
						out.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				File tmpFile = new File(location.getFile() + ".bak");
				if (isFind) {
					if (tmpFile.exists()) {
						tmpFile.delete();
					}
				} else {
					tmpFile.renameTo(new File(location.getFile()));
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	
	public static boolean checkEdition() {
		String temp = System.getProperty("TSEdition");
		if (!"U".equals(temp) && !"F".equals(temp) && !"P".equals(temp) && !"L".equals(temp)) {
			return false;
		} else {
			return true;
		}
	}
}

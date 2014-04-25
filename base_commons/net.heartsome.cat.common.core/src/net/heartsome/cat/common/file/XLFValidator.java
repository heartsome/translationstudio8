/**
 * XlLFValidator.java
 *
 * Version information :
 *
 * Date:2012-5-25
 *
 * Copyright notice :
 * 本文件及其附带的相关文件包含机密信息，仅限瀚特盛科技有限公司指定的，与本项目有关的内部人员和客户联络人员查阅和使用。 
 * 如果您不是本保密声明中指定的收件者，请立即销毁本文件，禁止对本文件或根据本文件中的内容采取任何其他行动， 
 * 包括但不限于：泄露本文件中的信息、以任何方式制作本文件全部或部分内容之副本、将本文件或其相关副本提供给任何其他人。
 */
package net.heartsome.cat.common.file;

import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import net.heartsome.cat.common.core.Constant;
import net.heartsome.cat.common.core.resource.Messages;
import net.heartsome.cat.common.locale.Language;
import net.heartsome.cat.common.locale.LocaleService;
import net.heartsome.cat.common.resources.ResourceUtils;
import net.heartsome.xml.vtdimpl.VTDUtils;

import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ximpleware.AutoPilot;
import com.ximpleware.ModifyException;
import com.ximpleware.NavException;
import com.ximpleware.VTDGen;
import com.ximpleware.VTDNav;
import com.ximpleware.XMLModifier;
import com.ximpleware.XPathEvalException;
import com.ximpleware.XPathParseException;

/**
 * @author jason
 * @version
 * @since JDK1.6
 */
public class XLFValidator {

	public static Logger logger = LoggerFactory.getLogger(XLFValidator.class);

	private static Shell shell;

	public static boolean blnMsg1 = false;

	public static boolean blnIsOpenConfirmSrc = false;

	/** 选择不再询问时，点击的按钮类型 */
	public static boolean blnIsOpenConfirmSrcY = false;

	public static boolean blnIsOpenConfirmTgt = false;

	/** 选择不再询问时，点击的按钮类型 */
	public static boolean blnIsOpenConfirmTgtY = false;

	private static HashMap<String, Object[]> mapProjectLang = new HashMap<String, Object[]>();

	/**
	 * 验证 XLIFF 文件的语言对是否与项目的语言对一致。
	 * @param iFile
	 *            ;
	 * @throws XPathEvalException
	 * @throws XPathParseException
	 * @throws NavException
	 */
	public static boolean validateXliffFile(IFile iFile) {
		shell = Display.getDefault().getActiveShell();
		try {
			Object[] arrObj = getProjectLang(iFile);
			if (arrObj == null) {
				return false;
			}
			String xlfFolderPath = iFile.getProject().getFullPath().append(Constant.FOLDER_XLIFF).toOSString();
			String xlfFullPath = iFile.getFullPath().toOSString();
			if (!xlfFullPath.startsWith(xlfFolderPath)
					|| iFile.getParent().getFullPath().toOSString().equals(xlfFolderPath)) {
				// 该 XLIFF 文件是 XLIFF 目录的直接子文件或者不在 XLIFF 的目录下
				if (!blnMsg1) {
					MessageDialogWithToggle dialog = MessageDialogWithToggle.openInformation(shell,
							Messages.getString("file.XLFValidator.msgTitle"),
							MessageFormat.format(Messages.getString("file.XLFValidator.msg1"), xlfFullPath),
							Messages.getString("file.XLFValidator.toggleStateMsg"), false, null, null);

					blnMsg1 = dialog.getToggleState();
				}
				return false;
			}
			Language projectSrcLang = (Language) arrObj[0];
			@SuppressWarnings("unchecked")
			List<Language> lstProjectTgtLang = (List<Language>) arrObj[1];
			// /test/XLIFF/zh-CN/split/test.xlf （zh-CN 在第三级目录下）
			String parentName = xlfFullPath.split(System.getProperty("file.separator").replaceAll("\\\\", "\\\\\\\\"))[3];

			Vector<String> languageVector = new Vector<String>();
			languageVector.add(parentName);
			if (LocaleService.verifyLanguages(languageVector)) {
				boolean flag = false;
				for (Language lang : lstProjectTgtLang) {
					if (lang.getCode().equalsIgnoreCase(parentName)) {
						flag = true;
						break;
					}
				}
				if (!flag) {
					if (!blnMsg1) {
						MessageDialogWithToggle dialog = MessageDialogWithToggle.openInformation(shell,
								Messages.getString("file.XLFValidator.msgTitle"),
								MessageFormat.format(Messages.getString("file.XLFValidator.msg2"), xlfFullPath),
								Messages.getString("file.XLFValidator.toggleStateMsg"), false, null, null);
						blnMsg1 = dialog.getToggleState();
					}
					return false;
				}
			} else {
				if (!blnMsg1) {
					MessageDialogWithToggle dialog = MessageDialogWithToggle.openInformation(shell,
							Messages.getString("file.XLFValidator.msgTitle"),
							MessageFormat.format(Messages.getString("file.XLFValidator.msg3"), xlfFullPath),
							Messages.getString("file.XLFValidator.toggleStateMsg"), false, null, null);
					blnMsg1 = dialog.getToggleState();
				}
				return false;
			}

			String xlfSrcLang = null;
			String xlfTgtLang = null;
			VTDGen vg = new VTDGen();
			XMLModifier xm = null;
			boolean isConfirmSrc = false;
			boolean isConfirmTgt = false;
			String fileOsPath = ResourceUtils.iFileToOSPath(iFile);
			boolean result = false;
			try {
				result = vg.parseFile(fileOsPath, true);
			} catch (Exception e) {
			}
			if (!result) {
				MessageDialog.openError(shell, Messages.getString("file.XLFValidator.errorTitle"),
						MessageFormat.format(Messages.getString("file.XLFValidator.parseError"), fileOsPath));
				return false;
			}
			VTDNav vn = vg.getNav();
			VTDUtils vu = new VTDUtils(vn);
			AutoPilot ap = new AutoPilot(vn);
			ap.declareXPathNameSpace("xml", VTDUtils.XML_NAMESPACE_URL);
			ap.selectXPath("/xliff/file");
			String original = null;
			int tempi = ap.evalXPath();
			if (tempi == -1) {
				MessageDialog.openError(shell, Messages.getString("file.XLFValidator.errorTitle"),
						MessageFormat.format(Messages.getString("file.XLFValidator.parseError"), fileOsPath));
				return false;
			}
			do {
				xlfSrcLang = vu.getCurrentElementAttribut("source-language", null);
				xlfTgtLang = vu.getCurrentElementAttribut("target-language", null);
				original = vu.getCurrentElementAttribut("original", null);
				if (original == null || original.trim().isEmpty()) {
					MessageDialog.openWarning(shell, Messages.getString("file.XLFValidator.warningTitle"), 
							MessageFormat.format(Messages.getString("file.XLFValidator.msg10"), xlfFullPath));
					return false;
				}

				String msg = null;
				// XLIFF 源语言为空或与项目源语言不一致；
				if (xlfSrcLang == null || !xlfSrcLang.equalsIgnoreCase(projectSrcLang.getCode())) {
					if (!blnIsOpenConfirmSrc && !isConfirmSrc) {
						if (xlfSrcLang == null) {
							msg = MessageFormat.format(Messages.getString("file.XLFValidator.msg4"), xlfFullPath,
									projectSrcLang.getCode());
						} else {
							msg = MessageFormat.format(Messages.getString("file.XLFValidator.msg5"), xlfFullPath,
									xlfSrcLang.toLowerCase(), projectSrcLang.getCode());
						}
						MessageDialogWithToggle dialog = new MessageDialogWithToggle(shell,
								Messages.getString("file.XLFValidator.msgTitle2"), null, msg, MessageDialog.CONFIRM,
								new String[] { IDialogConstants.YES_LABEL, IDialogConstants.NO_LABEL }, 0,
								Messages.getString("file.XLFValidator.toggleStateMsg"), false);
						int returnCode = dialog.open();
						if (returnCode == IDialogConstants.YES_ID) {
							isConfirmSrc = true;
							blnIsOpenConfirmSrcY = true;
						} else if (returnCode == IDialogConstants.NO_ID) {
							isConfirmSrc = false;
							blnIsOpenConfirmSrcY = false;
						}
						blnIsOpenConfirmSrc = dialog.getToggleState();
					}
					if ((isConfirmSrc || (blnIsOpenConfirmSrc && blnIsOpenConfirmSrcY)) && xlfSrcLang != null) {
						xm = vu.update(null, xm, "/xliff/file[@original=\"" + original + "\"]/@source-language",
								projectSrcLang.getCode(), VTDUtils.CREATE_IF_NOT_EXIST);
					} else if (!isConfirmSrc && !(blnIsOpenConfirmSrc && blnIsOpenConfirmSrcY)) {
						return false;
					}
				}

				// XLIFF 目标语言为空，（且其所在的 XLIFF 一级子目录名称是项目的目标语言代码之一,已在上面验证）直接设置
				if (xlfTgtLang == null || xlfSrcLang == null) {
					// XLIFF 文件中源与目标都为空时，由于 VTD 要求不能在一个位置修改两次，因此使用下面的方式插入源与目标到 file 节点中
					if (xlfTgtLang == null && xlfSrcLang == null) {
						String attrFragment = new StringBuffer(" source-language=\"").append(projectSrcLang.getCode())
								.append("\" target-language=\"").append(parentName).append("\"").toString(); // 构建属性片段，“
																												// attrName="attrValue"
																												// ”
						long i = vn.getOffsetAfterHead(); // 得到开始标记的结束位置
						if (xm == null) {
							xm = new XMLModifier(vn);
						}
						if (vn.getEncoding() < VTDNav.FORMAT_UTF_16BE) {
							xm.insertBytesAt((int) i - 1, attrFragment.getBytes());
						} else {
							xm.insertBytesAt(((int) i - 1) << 1, attrFragment.getBytes());
						}
					} else if (xlfTgtLang == null) {
						xm = vu.update(null, xm, "/xliff/file[@original=\"" + original + "\"]/@target-language",
								parentName, VTDUtils.CREATE_IF_NOT_EXIST);
					} else if (xlfSrcLang == null) {
						xm = vu.update(null, xm, "/xliff/file[@original=\"" + original + "\"]/@source-language",
								projectSrcLang.getCode(), VTDUtils.CREATE_IF_NOT_EXIST);
					}
				}
				if (xlfTgtLang != null) {
					// XLIFF 目标语言非空，但未放在对应的目录下。
					boolean flag = false;
					for (Language lang : lstProjectTgtLang) {
						if (lang.getCode().equalsIgnoreCase(xlfTgtLang)) {
							flag = true;
							break;
						}
					}
					String message = null;
					if (!flag) {
						message = MessageFormat.format(Messages.getString("file.XLFValidator.msg6"), xlfFullPath,
								xlfTgtLang, parentName);
					} else if (!xlfTgtLang.equalsIgnoreCase(parentName)) {
						message = MessageFormat.format(Messages.getString("file.XLFValidator.msg7"), xlfFullPath,
								xlfTgtLang, parentName);
					}
					if (!blnIsOpenConfirmTgt && !isConfirmTgt && message != null) {
						MessageDialogWithToggle dialog = new MessageDialogWithToggle(shell,
								Messages.getString("file.XLFValidator.msgTitle2"), null, message,
								MessageDialog.CONFIRM, new String[] { IDialogConstants.YES_LABEL,
										IDialogConstants.NO_LABEL }, 0,
								Messages.getString("file.XLFValidator.toggleStateMsg"), false);
						int returnCode = dialog.open();
						if (returnCode == IDialogConstants.YES_ID) {
							isConfirmTgt = true;
							blnIsOpenConfirmTgtY = true;
						} else if (returnCode == IDialogConstants.NO_ID) {
							isConfirmTgt = false;
							blnIsOpenConfirmTgtY = false;
						}
						blnIsOpenConfirmTgt = dialog.getToggleState();
					}
					if ((blnIsOpenConfirmTgt && blnIsOpenConfirmTgtY) || isConfirmTgt) {
						xm = vu.update(null, xm, "/xliff/file[@original=\"" + original + "\"]/@target-language",
								parentName, VTDUtils.CREATE_IF_NOT_EXIST | VTDUtils.PILOT_TO_END);
						// vu.bind(xm.outputAndReparse());
					} else if (message != null) {
						return false;
					}
				}

				// Bug #2329：文件语言更改成项目语言时应同时更改 source 节点的 xml:lang 属性值，如果 target 节点有 xml:lang 属性，也要修改成项目语言
				AutoPilot tempAp = new AutoPilot(vn);
				tempAp.declareXPathNameSpace("xml", VTDUtils.XML_NAMESPACE_URL);
				xm = vu.update(tempAp, xm, "/xliff/file[@original=\"" + original
						+ "\"]/body//trans-unit/source/@xml:lang", projectSrcLang.getCode(),
						VTDUtils.CREATE_IF_NOT_EXIST | VTDUtils.PILOT_TO_END);
				xm = vu.update(tempAp, xm, "/xliff/file[@original=\"" + original
						+ "\"]/body//trans-unit/target/@xml:lang", parentName, VTDUtils.PILOT_TO_END);
			} while (ap.evalXPath() != -1);
			if (xm != null) {
				// vu.bind(xm.outputAndReparse());
				vu.bind(vu.updateVTDNav(xm, ResourceUtils.iFileToOSPath(iFile)));
			}
			vg.clear();
			iFile.getProject().refreshLocal(IResource.DEPTH_INFINITE, null);
		} catch (XPathParseException e) {
			e.printStackTrace();
			logger.error(Messages.getString("file.XLFValidator.logger1"), e);
			MessageDialog.openInformation(shell, Messages.getString("file.XLFValidator.msgTitle"),
					Messages.getString("file.XLFValidator.msg8"));
			return false;
		} catch (XPathEvalException e) {
			e.printStackTrace();
			logger.error(Messages.getString("file.XLFValidator.logger1"), e);
			MessageDialog.openInformation(shell, Messages.getString("file.XLFValidator.msgTitle"),
					Messages.getString("file.XLFValidator.msg8"));
			return false;
		} catch (NavException e) {
			e.printStackTrace();
			logger.error(Messages.getString("file.XLFValidator.logger1"), e);
			MessageDialog.openInformation(shell, Messages.getString("file.XLFValidator.msgTitle"),
					Messages.getString("file.XLFValidator.msg8"));
			return false;
		} catch (CoreException e) {
			logger.error("", e);
		} catch (ModifyException e) {
			logger.error("", e);
		}
		return true;
	}

	public static boolean validateXliffFile(String fileLocalPath) {
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IFile file = root.getFileForLocation(URIUtil.toPath(new File(fileLocalPath).toURI()));
		if (file == null) {
			Shell shell = Display.getDefault().getActiveShell();
			MessageDialog.openError(shell, Messages.getString("file.XLFValidator.msgTitle"),
					Messages.getString("file.XLFValidator.msg9"));
			return false;
		}
		return validateXliffFile(file);
	}

	public static boolean validateXlifFileStr(List<String> files) {

		for (String file : files) {
			if (!validateXliffFile(file)) {
				return false;
			}
		}
		return true;
	}

	public static boolean validateXliffFile(File file) {
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IFile ifile = root.getFileForLocation(URIUtil.toPath(file.toURI()));
		if (ifile == null) {
			Shell shell = Display.getDefault().getActiveShell();
			MessageDialog.openError(shell, Messages.getString("file.XLFValidator.msgTitle"),
					Messages.getString("file.XLFValidator.msg9"));
			return false;
		}
		return validateXliffFile(ifile);
	}

	public static boolean validateXlifFiles(List<File> files) {
		for (File file : files) {
			if (!validateXliffFile(file)) {
				return false;
			}
		}
		return true;
	}

	public static boolean validateXlifIFiles(List<IFile> files) {
		for (IFile file : files) {
			if (!validateXliffFile(file)) {
				return false;
			}
		}
		return true;
	}

	public static void resetFlag() {
		blnMsg1 = false;
		blnIsOpenConfirmSrc = false;
		blnIsOpenConfirmSrcY = false;
		blnIsOpenConfirmTgt = false;
		blnIsOpenConfirmTgtY = false;
		mapProjectLang.clear();
	}

	/**
	 * 获取 iFile 所在项目的源语言与目标语言
	 * @param iFile
	 * @return 数组中的第一个值为源语言，类型为 Language；第二个值为目标语言集合，类型为 List<Language>
	 * @throws NavException
	 * @throws XPathParseException
	 * @throws XPathEvalException
	 *             ;
	 */
	private static Object[] getProjectLang(IFile iFile) throws NavException, XPathParseException, XPathEvalException {
		String projectFilePath = iFile.getProject().getLocation().toOSString() + System.getProperty("file.separator")
				+ ".config";
		if (mapProjectLang.containsKey(projectFilePath)) {
			return mapProjectLang.get(projectFilePath);
		}

		VTDGen vg = new VTDGen();
		if (vg.parseFile(projectFilePath, true)) {
			VTDNav vn = vg.getNav();
			VTDUtils vu = new VTDUtils(vn);
			AutoPilot ap = new AutoPilot(vn);
			ap.selectXPath("/projectDescription/hs/language/source");
			Object[] arrObj = new Object[2];
			if (ap.evalXPath() != -1) {
				String code = vu.getCurrentElementAttribut("code", "");
				String name = vu.getElementContent();
				String image = vu.getCurrentElementAttribut("image", "");
				String isBidi = vu.getCurrentElementAttribut("isbidi", "No");
				arrObj[0] = new Language(code, name, image, isBidi.equals("NO") ? false : true);
			}
			ap.selectXPath("/projectDescription/hs/language/target");
			List<Language> targetLangs = new ArrayList<Language>();
			while (ap.evalXPath() != -1) {
				String code = vu.getCurrentElementAttribut("code", "");
				String name = vu.getElementContent();
				String image = vu.getCurrentElementAttribut("image", "");
				String isBidi = vu.getCurrentElementAttribut("isbidi", "false");
				targetLangs.add(new Language(code, name, image, isBidi.equals("false") ? false : true));
			}
			arrObj[1] = targetLangs;
			return arrObj;
		} else {
			return null;
		}
	}
}

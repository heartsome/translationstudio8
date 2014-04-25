/**
 * ImportTbx.java
 *
 * Version information :
 *
 * Date:Nov 18, 2011
 *
 * Copyright notice :
 * 本文件及其附带的相关文件包含机密信息，仅限瀚特盛科技有限公司指定的，与本项目有关的内部人员和客户联络人员查阅和使用。 
 * 如果您不是本保密声明中指定的收件者，请立即销毁本文件，禁止对本文件或根据本文件中的内容采取任何其他行动， 
 * 包括但不限于：泄露本文件中的信息、以任何方式制作本文件全部或部分内容之副本、将本文件或其相关副本提供给任何其他人。
 */
package net.heartsome.cat.document;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.heartsome.cat.common.locale.LocaleService;
import net.heartsome.cat.database.Constants;
import net.heartsome.cat.database.DBOperator;
import net.heartsome.cat.database.Utils;
import net.heartsome.cat.database.resource.Messages;
import net.heartsome.xml.vtdimpl.VTDUtils;

import org.eclipse.core.runtime.OperationCanceledException;

import com.ximpleware.AutoPilot;
import com.ximpleware.EOFException;
import com.ximpleware.EncodingException;
import com.ximpleware.EntityException;
import com.ximpleware.ModifyException;
import com.ximpleware.NavException;
import com.ximpleware.ParseException;
import com.ximpleware.TranscodeException;
import com.ximpleware.VTDGen;
import com.ximpleware.XMLModifier;
import com.ximpleware.XPathEvalException;
import com.ximpleware.XPathParseException;

/**
 * @author Jason
 * @version
 * @since JDK1.6
 */
public class ImportTbx extends ImportAbstract {

	public ImportTbx(DBOperator tmDatabase, int importMode) {
		this.fileType = Constants.TBX;
		this.dbOperator = tmDatabase;
		this.importStrategy = importMode;
	}

	/**
	 * (non-Javadoc)
	 * @throws ParseException
	 * @throws EntityException
	 * @throws EOFException
	 * @throws EncodingException
	 * @throws IOException
	 * @throws ModifyException
	 * @throws TranscodeException
	 * @see net.heartsome.cat.document.ImportAbstract#executeImport(java.lang.String)
	 */
	@Override
	protected void executeImport(String srcLang) throws SQLException, NavException, XPathParseException,
			XPathEvalException, EncodingException, EOFException, EntityException, ParseException, TranscodeException,
			ModifyException, IOException {
		srcLang = Utils.convertLangCode(srcLang);
		if (monitor != null) {
			int task = vu.getChildElementsCount("/martif/text/body") + vu.getChildElementsCount("/martif/text/back")
					+ 10;
			monitor.beginTask(Messages.getString("document.ImportTbx.task1"), task);
		}
		int headerPkId = 0;
		String sourceLang = null;
		AutoPilot ap = new AutoPilot(vu.getVTDNav());
		ap.selectXPath("/martif");
		Map<String, String> martifAttr = new HashMap<String, String>();
		if (ap.evalXPath() != -1) {
			martifAttr = vu.getCurrentElementAttributs();
			sourceLang = martifAttr.get("xml:lang");
			if (sourceLang == null) {
				sourceLang = martifAttr.get("lang");
			}
		}
		if (sourceLang == null || sourceLang.equals("")) {
			sourceLang = srcLang;
		} else {
			sourceLang = Utils.convertLangCode(sourceLang);
		}

		if (sourceLang == null || sourceLang.equals("*all*") || sourceLang.equals("")) {
			if (LocaleService.getLanguage(sourceLang).equals("")) {
				throw new NavException(Messages.getString("document.ImportTbx.msg1"));
			}
		}

		if (monitor != null) {
			monitor.worked(5);
		}
		// 导入Header
		ap.selectXPath("/martif/martifHeader");
		if (ap.evalXPath() != -1) {
			String hContent = vu.getElementFragment();
			if (hContent != null) {
				headerPkId = dbOperator.insertBMartifHeader(hContent, getElementAttribute("id"));
			}
		}
		// TOTO 保存martifAttr到BATTRIBUTE表
		dbOperator.insertBAttribute(martifAttr, "martif", headerPkId);

		ap.selectXPath("/martif/text");
		if (ap.evalXPath() != -1) {
			Map<String, String> textAttr = vu.getCurrentElementAttributs();
			dbOperator.insertBAttribute(textAttr, "text", headerPkId);
		}

		ap.selectXPath("/martif/body");
		if (ap.evalXPath() != -1) {
			Map<String, String> bodyAttr = vu.getCurrentElementAttributs();
			dbOperator.insertBAttribute(bodyAttr, "body", headerPkId);
		}
		if (monitor != null) {
			monitor.worked(5);
		}

		this.saveTermEntry(sourceLang, headerPkId);

		ap.selectXPath("/martif/text/back");
		if (ap.evalXPath() != -1) {
			Map<String, String> backAttr = vu.getCurrentElementAttributs();
			// TODO 保存back节点的属性
			dbOperator.insertBAttribute(backAttr, "back", headerPkId);
			ap.selectXPath("./refObjectList");
			while (ap.evalXPath() != -1) {
				String roblId = getElementAttribute("id");
				String roblContent = vu.getElementFragment();
				// 保存refObjectList内容
				dbOperator.insertBRefobjectlist(roblContent, roblId, headerPkId);
				if (monitor != null) {
					monitor.worked(1);
				}
			}
		}
		if (monitor != null) {
			monitor.done();
		}

	}

	/**
	 * 始终增加策略
	 * @param headerId
	 * @throws XPathParseException
	 * @throws XPathEvalException
	 * @throws NavException
	 * @throws SQLException
	 *             ;
	 */
	private void saveTermEntryWithAdd(int headerId, AutoPilot langSetAp, AutoPilot termAp) throws XPathParseException,
			XPathEvalException, NavException, SQLException {
		int termEntryPkId = dbOperator.insertBTermentry(vu.getElementFragment(), getElementAttribute("id"), headerId); // 将TermEntry整个节点写入库中

		vu.getVTDNav().push();
		while (langSetAp.evalXPath() != -1) {
			String lang = getLang(); // 获取 LangSet节点下的语言
			String pureText = "";
			String fullText = "";
			// 取LangSet节点下的Term节点内容
			vu.getVTDNav().push();
			while (termAp.evalXPath() != -1) {
				pureText = DocUtils.getTmxTbxPureText(vu);
				fullText = vu.getElementContent();
				// TODO 将langSet节点中的term节点内容写入Textdata表中
				String hash = null; // 当pureText为空字符串时，HASH字段留空
				if (pureText != null) {
					hash = "" + pureText.hashCode();
				}
				dbOperator.insertTextData("B", termEntryPkId, hash, pureText, fullText, lang, null, null);

			}
			termAp.resetXPath();
			vu.getVTDNav().pop();
		}
		langSetAp.resetXPath();
		vu.getVTDNav().pop();
		AutoPilot noteAp = new AutoPilot(vu.getVTDNav());
		noteAp.selectXPath("./note");
		vu.getVTDNav().push();
		while (noteAp.evalXPath() != -1) {
			String content = vu.getElementContent();
			String id = vu.getCurrentElementAttribut("id", "");
			if (id.equals("") || content == null || content.equals("")) {
				continue;
			}			
			dbOperator.insertBNode(termEntryPkId, "termEntry", "E", "note", id, content);
		}
		vu.getVTDNav().pop();
	}

	/**
	 * 覆盖或者忽略
	 * @param headerId
	 *            ;
	 * @throws SQLException
	 * @throws NavException
	 * @throws XPathEvalException
	 * @throws XPathParseException
	 * @throws ParseException
	 * @throws EntityException
	 * @throws EOFException
	 * @throws EncodingException
	 * @throws IOException
	 * @throws ModifyException
	 * @throws TranscodeException
	 */
	private void saveTermEntry(String srcLang, int headerId) throws XPathParseException, XPathEvalException,
			NavException, SQLException, EncodingException, EOFException, EntityException, ParseException,
			TranscodeException, ModifyException, IOException {

		AutoPilot ap = new AutoPilot(vu.getVTDNav());
		ap.selectXPath("/martif/text/body/termEntry"); // term entry

		AutoPilot _ap = new AutoPilot(vu.getVTDNav());
		_ap.declareXPathNameSpace("xml", VTDUtils.XML_NAMESPACE_URL);
		_ap.selectXPath("./langSet[@xml:lang='" + srcLang + "']/tig/term|./ntig/termGrp/term"); // langSet == sourceLang

		AutoPilot langSetAp = new AutoPilot(vu.getVTDNav());
		langSetAp.selectXPath("./langSet");

		AutoPilot termAp = new AutoPilot(vu.getVTDNav());
		termAp.selectXPath("./tig/term|./ntig/termGrp/term"); // Term

		List<String> textDataIds = new ArrayList<String>();
		while (ap.evalXPath() != -1) { // 循环termEntry节点
			if (monitor != null && monitor.isCanceled()) {
				throw new OperationCanceledException();
			}

			if (importStrategy == Constants.IMPORT_MODEL_ALWAYSADD) { // 始终增加模式
				saveTermEntryWithAdd(headerId, langSetAp, termAp);
				if (monitor != null) {
					monitor.worked(1);
				}
				continue;
			}

			// 判断该TermEntry节点是否重复
			vu.getVTDNav().push();
			while (_ap.evalXPath() != -1) {
				String pureText = DocUtils.getTmxTbxPureText(vu);
				pureText = pureText.trim();
				textDataIds = dbOperator.getTextDataId(pureText.hashCode(), Utils.convertLangCode(srcLang), "B");
				if (textDataIds.size() > 0) { // 当前TermEntry重复
					break;
				}
			}
			_ap.resetXPath();
			vu.getVTDNav().pop();
			// System.out.println(textDataIds);
			if (textDataIds.size() == 0) { // 在库中没有重复的TermEntry
				saveTermEntryWithAdd(headerId, langSetAp, termAp);
			} else {
				if (importStrategy == Constants.IMPORT_MODEL_IGNORE) { // 忽略当前TermEntry
					if (monitor != null) {
						monitor.worked(1);
					}
					continue;
				} else { // 合并
					List<String> termEntryPks = new ArrayList<String>();
					termEntryPks = dbOperator.getTextDataGroupIdByTextId(textDataIds); // 获取源文相同的TermEntry Pk
					// System.out.println(termEntryPks);
					List<Map<String, String>> dbTermEntrys = dbOperator.getBTermEntryByPk(termEntryPks); // 获取TermEntry的内容
					if (dbTermEntrys.size() == 1) { // 在库中只有一个TermEntry和当前TermEntry重复
						if (importStrategy == Constants.IMPORT_MODEL_MERGE) {
							saveTermEntryWithMerge(dbTermEntrys.get(0), langSetAp, termAp);
						} else if (importStrategy == Constants.IMPORT_MODEL_OVERWRITE) {
							saveTermEntryWithOverwrite(dbTermEntrys.get(0), langSetAp, termAp);
						}
					} else {
						boolean updateflag = false;// 标记是否已经执行过更新
						for (int i = 0; i < dbTermEntrys.size(); i++) {
							Map<String, String> dbTermEntry = dbTermEntrys.get(i);
							if (dbTermEntry.get("CID").equals(getElementAttribute("id"))) {
								updateflag = true;
								if (importStrategy == Constants.IMPORT_MODEL_MERGE) {
									saveTermEntryWithMerge(dbTermEntry, langSetAp, termAp);
								} else if (importStrategy == Constants.IMPORT_MODEL_OVERWRITE) {
									saveTermEntryWithOverwrite(dbTermEntry, langSetAp, termAp);
								}
							}
						}
						if (!updateflag) { // 没有执行更新,即没有在库中找到和当前TermEntry ID一样的
							saveTermEntryWithAdd(headerId, langSetAp, termAp);
						}
					}
				}
			}
			if (monitor != null) {
				monitor.worked(1);
			}
		}
	}

	private void saveTermEntryWithMerge(Map<String, String> dbTermEntry, AutoPilot langSetAp, AutoPilot termAp)
			throws XPathParseException, XPathEvalException, NavException, SQLException, EncodingException,
			EOFException, EntityException, ParseException, TranscodeException, ModifyException, IOException {
		String content = dbTermEntry.get("CONTENT");
		String termEntryPk = dbTermEntry.get("BTEPKID");
		boolean changFlag = false; // content修改标记

		VTDGen vg = new VTDGen();
		VTDUtils vtdUtiles = new VTDUtils();
		vg.setDoc(content.getBytes());
		vg.parse(true);
		vtdUtiles.bind(vg.getNav());

		AutoPilot _ap = new AutoPilot(vtdUtiles.getVTDNav());
		if (!content.equals(vu.getElementFragment())) {
			vu.getVTDNav().push();
			while (langSetAp.evalXPath() != -1) {
				String curlang = getElementAttribute("xml:lang");
				String tigValue = vu.getChildContent("tig");
				String ntigValue = vu.getChildContent("ntig");

				vtdUtiles.getVTDNav().push();
				_ap.selectXPath("/termEntry/langSet[@xml:lang='" + curlang + "']");
				if (_ap.evalXPath() != -1) { // 当语言的langSet在库中是否存在
					vu.getVTDNav().push();
					while (termAp.evalXPath() != -1) {
						String fullText = vu.getElementContent();
						String pureText = DocUtils.getTmxTbxPureText(vu);

						vtdUtiles.getVTDNav().push();
						_ap.selectXPath("./tig[term='" + fullText + "']|./ntig/termGrp[term='" + fullText + "']");
						if (_ap.evalXPath() == -1) {
							XMLModifier xm = null;
							if (tigValue != null) {
								xm = vtdUtiles.insert("/termEntry/langSet[@xml:lang='" + curlang + "']/tig", "<tig>"
										+ tigValue + "</tig>");
							} else if (ntigValue != null) {
								xm = vtdUtiles.insert("/termEntry/langSet[@xml:lang='" + curlang + "']/tig", "<ntig>"
										+ ntigValue + "</ntig>");
							}
							vtdUtiles.bind(xm.outputAndReparse());

							String hash = null; // 当pureText为空字符串时，HASH字段留空
							if (pureText != null) {
								hash = "" + pureText.hashCode();
							}
							dbOperator.insertTextData("B", Integer.parseInt(termEntryPk), hash, pureText, fullText,
									Utils.convertLangCode(curlang), null, null);

							changFlag = true;
						}
						_ap.resetXPath();
						vtdUtiles.getVTDNav().pop();
					}
					termAp.resetXPath();
					vu.getVTDNav().pop();//
				} else {
					// 库中不存在该种语言的langSet,则新增
					XMLModifier xm = vtdUtiles.insert("/termEntry/langSet", vu.getElementFragment());
					vtdUtiles.bind(xm.outputAndReparse());
					changFlag = true;
					vu.getVTDNav().push();
					if (termAp.evalXPath() != -1) {
						String fullText = vu.getElementContent();
						String pureText = DocUtils.getTmxTbxPureText(vu);
						String hash = null; // 当pureText为空字符串时，HASH字段留空
						if (pureText != null) {
							hash = "" + pureText.hashCode();
						}
						dbOperator.insertTextData("B", Integer.parseInt(termEntryPk), hash, pureText, fullText,
								Utils.convertLangCode(curlang), null, null);
					}
					termAp.resetXPath();
					vu.getVTDNav().pop();
				}
				vtdUtiles.getVTDNav().pop();
			}
			langSetAp.resetXPath();
			vu.getVTDNav().pop();

			// 处理note
			List<String> existDbNoteId = new ArrayList<String>();
			List<String> existDbNotePk = new ArrayList<String>();
			List<String> noteIdRm = new ArrayList<String>(); // 用于记录已经处理的noteId,过滤掉文件重复的内容
			StringBuffer noteContentBf = new StringBuffer();
			AutoPilot noteAp = new AutoPilot(vu.getVTDNav());
			vu.getVTDNav().push();
			noteAp.selectXPath("./note");
			while (noteAp.evalXPath() != -1) {
				String noteId = vu.getCurrentElementAttribut("id", "");
				String noteContent = vu.getElementContent();
				if (noteId.equals("") || noteContent == null || noteContent.equals("")) {
					continue;
				}

				String[] ids = noteId.split(",");
				if (ids.length != 2) {
					continue;
				}

				String reNoteId = ids[1] + "," + ids[0];
				if (noteIdRm.contains(noteId) || noteIdRm.contains(reNoteId)) {
					continue; // 重复不重复写入
				}
				noteIdRm.add(noteId); // 记录已经处理的note

				AutoPilot dbNoteAp = new AutoPilot(vtdUtiles.getVTDNav());
				dbNoteAp.selectXPath("/termEntry/note[@id='" + noteId + "' or @id='" + reNoteId + "']");
				while (dbNoteAp.evalXPath() != -1) {
					String dbNoteId = vtdUtiles.getCurrentElementAttribut("id", "");
					if (!existDbNoteId.contains(dbNoteId)) {
						existDbNoteId.add(dbNoteId);
					}
					List<Map<String, String>> result = dbOperator.getBNodeByParent(termEntryPk, "termEntry", "E",
							"note", dbNoteId);
					for (Map<String, String> map : result) {
						existDbNotePk.add(map.get("NPKID"));
					}
				}
				dbOperator.insertBNode(Integer.parseInt(termEntryPk), "termEntry", "E", "note", noteId, noteContent);
				noteContentBf.append(vu.getElementFragment());
			}
			vu.getVTDNav().pop();

			if (existDbNotePk.size() > 0) {
				dbOperator.deleteBNode(existDbNotePk);
			}

			StringBuffer xpathWhere = new StringBuffer();
			for (String dbNodeId : existDbNoteId) {
				xpathWhere.append("@id='" + dbNodeId + "' or ");
			}

			if (xpathWhere.length() > 0) {
				String xpath = xpathWhere.substring(0, xpathWhere.lastIndexOf("or"));
				XMLModifier xm = vtdUtiles.delete("/termEntry/note[" + xpath + "]", VTDUtils.PILOT_TO_END); // 清除文件中的note
				vtdUtiles.bind(xm.outputAndReparse());
				changFlag = true;
			}
			if (noteContentBf.length() != 0) {
				XMLModifier xm = vtdUtiles.insert("/termEntry/text()", noteContentBf.toString());
				vtdUtiles.bind(xm.outputAndReparse());
				changFlag = true;
			}
			if (changFlag) {
				dbOperator.updateTermEntry(vtdUtiles.getElementFragment(), termEntryPk);
			}
		}

	}

	private void saveTermEntryWithOverwrite(Map<String, String> dbTermEntry, AutoPilot langSetAp, AutoPilot termAp)
			throws XPathParseException, XPathEvalException, NavException, SQLException, EncodingException,
			EOFException, EntityException, ParseException, TranscodeException, ModifyException, IOException {

		VTDGen vg = new VTDGen();
		VTDUtils vtdUtiles = new VTDUtils();
		String content = dbTermEntry.get("CONTENT");
		String termEntryPk = dbTermEntry.get("BTEPKID");
		if (!content.equals(vu.getElementFragment())) {
			String prelang = "";
			vu.getVTDNav().push();
			while (langSetAp.evalXPath() != -1) {
				String curlang = getElementAttribute("xml:lang");
				String langSetValue = vu.getElementFragment();
				vg.setDoc(content.getBytes());
				vg.parse(true);
				vtdUtiles.bind(vg.getNav());
				if (!prelang.endsWith(curlang)) {
					prelang = curlang;
					dbOperator.deleteTerm(termEntryPk, curlang);
					AutoPilot ap = new AutoPilot(vtdUtiles.getVTDNav());
					ap.declareXPathNameSpace("xml", vtdUtiles.XML_NAMESPACE_URL);
					XMLModifier xm = new XMLModifier(vtdUtiles.getVTDNav());
					vtdUtiles.delete(ap, xm, "/termEntry/langSet[@xml:lang='" + curlang + "']");					
					vtdUtiles.bind(xm.outputAndReparse());
				}
				XMLModifier xm = vtdUtiles.insert("/termEntry/langSet", langSetValue);
				vtdUtiles.bind(xm.outputAndReparse());

				vu.getVTDNav().push();
				while (termAp.evalXPath() != -1) {
					String pureText = DocUtils.getTmxTbxPureText(vu);
					String fullText = vu.getElementContent();
					String hash = null; // 当pureText为空字符串时，HASH字段留空
					if (pureText != null) {
						hash = "" + pureText.hashCode();
					}
					dbOperator.insertTextData("B", Integer.parseInt(termEntryPk), hash, pureText, fullText,
							Utils.convertLangCode(curlang), null, null);
				}
				termAp.resetXPath();
				vu.getVTDNav().pop();
			}
			langSetAp.resetXPath();
			vu.getVTDNav().pop();

			// 处理note

			List<String> existDbNoteId = new ArrayList<String>();
			List<String> existDbNotePk = new ArrayList<String>();
			List<String> noteIdRm = new ArrayList<String>(); // 用于记录已经处理的noteId,过滤掉文件重复的内容
			StringBuffer noteContentBf = new StringBuffer();
			AutoPilot noteAp = new AutoPilot(vu.getVTDNav());
			vu.getVTDNav().push();
			noteAp.selectXPath("./note");
			while (noteAp.evalXPath() != -1) {
				String noteId = vu.getCurrentElementAttribut("id", "");
				String noteContent = vu.getElementContent();
				if (noteId.equals("") || noteContent == null || noteContent.equals("")) {
					continue;
				}

				String[] ids = noteId.split(",");
				if (ids.length != 2) {
					continue;
				}

				String reNoteId = ids[1] + "," + ids[0];
				if (noteIdRm.contains(noteId) || noteIdRm.contains(reNoteId)) {
					continue; // 重复不重复写入
				}
				noteIdRm.add(noteId); // 记录已经处理的note

				AutoPilot dbNoteAp = new AutoPilot(vtdUtiles.getVTDNav());
				dbNoteAp.selectXPath("/termEntry/note[@id='" + noteId + "' or @id='" + reNoteId + "']");
				while (dbNoteAp.evalXPath() != -1) {
					String dbNoteId = vtdUtiles.getCurrentElementAttribut("id", "");
					if (!existDbNoteId.contains(dbNoteId)) {
						existDbNoteId.add(dbNoteId);
					}
					List<Map<String, String>> result = dbOperator.getBNodeByParent(termEntryPk, "termEntry", "E",
							"note", dbNoteId);
					for (Map<String, String> map : result) {
						existDbNotePk.add(map.get("NPKID"));
					}
				}
				dbOperator.insertBNode(Integer.parseInt(termEntryPk), "termEntry", "E", "note", noteId, noteContent);
				noteContentBf.append(vu.getElementFragment());
			}

			if (existDbNotePk.size() > 0) {
				dbOperator.deleteBNode(existDbNotePk);
			}

			StringBuffer xpathWhere = new StringBuffer();
			for (String dbNodeId : existDbNoteId) {
				xpathWhere.append("@id='" + dbNodeId + "' or ");
			}

			if (xpathWhere.length() > 0) {
				String xpath = xpathWhere.substring(0, xpathWhere.lastIndexOf("or"));
				XMLModifier xm = vtdUtiles.delete("/termEntry/note[" + xpath + "]", VTDUtils.PILOT_TO_END); // 清除文件中的note
				vtdUtiles.bind(xm.outputAndReparse());
			}

			if (noteContentBf.length() != 0) {
				XMLModifier xm = vtdUtiles.insert("/termEntry/text()", noteContentBf.toString());
				vtdUtiles.bind(xm.outputAndReparse());
			}

			content = vtdUtiles.getElementFragment();

			dbOperator.updateTermEntry(content, termEntryPk);

			vu.getVTDNav().pop();
		}
	}

	/**
	 * 获取当前节点属性的值
	 * @param attrName
	 *            属性名称
	 * @return
	 * @throws XPathParseException
	 * @throws XPathEvalException
	 * @throws NavException
	 *             ;
	 */
	private String getElementAttribute(String attrName) throws XPathParseException, XPathEvalException, NavException {
		Map<String, String> attr = vu.getCurrentElementAttributs();
		if (attr != null) {
			String attrValue = attr.get(attrName);
			return attr.get(attrName) == null ? "" : attrValue;
		}
		return "";
	}
}

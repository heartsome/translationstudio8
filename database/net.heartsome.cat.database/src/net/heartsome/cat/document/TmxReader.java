/**
 * TmxFileReader.java
 *
 * Version information :
 *
 * Date:2013-1-25
 *
 * Copyright notice :
 * 本文件及其附带的相关文件包含机密信息，仅限瀚特盛科技有限公司指定的，与本项目有关的内部人员和客户联络人员查阅和使用。 
 * 如果您不是本保密声明中指定的收件者，请立即销毁本文件，禁止对本文件或根据本文件中的内容采取任何其他行动， 
 * 包括但不限于：泄露本文件中的信息、以任何方式制作本文件全部或部分内容之副本、将本文件或其相关副本提供给任何其他人。
 */
package net.heartsome.cat.document;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import net.heartsome.cat.common.bean.TmxContexts;
import net.heartsome.cat.common.bean.TmxHeader;
import net.heartsome.cat.common.bean.TmxNote;
import net.heartsome.cat.common.bean.TmxProp;
import net.heartsome.cat.common.bean.TmxSegement;
import net.heartsome.cat.common.bean.TmxTU;
import net.heartsome.cat.common.util.FileEncodingDetector;
import net.heartsome.cat.database.Utils;
import net.heartsome.cat.database.resource.Messages;
import net.heartsome.xml.vtdimpl.VTDLoader;
import net.heartsome.xml.vtdimpl.VTDUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ximpleware.AutoPilot;
import com.ximpleware.EncodingException;
import com.ximpleware.NavException;
import com.ximpleware.ParseException;
import com.ximpleware.VTDException;
import com.ximpleware.VTDGen;
import com.ximpleware.VTDNav;
import com.ximpleware.XPathEvalException;
import com.ximpleware.XPathParseException;

/**
 * @author Jason
 * @version
 * @since JDK1.6
 */
public class TmxReader {

	private final Logger logger = LoggerFactory.getLogger(TmxReader.class);

	private TmxHeader header;
	private int totalTu;

	/** XML解析封装 */
	private VTDUtils vu;
	private AutoPilot tuAp;

	private TmxFilterInterface tmxFilter = null;

	public TmxReader(File file) throws TmxReadException {
		// 解析文件
		VTDGen vg = null;
		try {
			vg = VTDLoader.loadVTDGen(file, FileEncodingDetector.detectFileEncoding(file));
		} catch (IOException e) {
			logger.error(Messages.getString("document.DocUtils.logger1"), e);
			throw new TmxReadException(Messages.getString("document.TmxReader.parseTmxFileError"));
		} catch (EncodingException e) {
			logger.error(Messages.getString("document.ImportAbstract.logger1"), e);
			String message = Messages.getString("document.ImportAbstract.msg1");
			throw new TmxReadException(message + e.getMessage());
		} catch (ParseException e) {
			logger.error(Messages.getString("document.ImportAbstract.logger3"), e);
			String errMsg = e.getMessage();
			String message;
			if (errMsg.indexOf("invalid encoding") != -1) { // 编码异常
				message = Messages.getString("document.ImportAbstract.msg1");
			} else {
				message = Messages.getString("document.ImportAbstract.msg3");
			}
			throw new TmxReadException(message + e.getMessage());
		}
		if (vg == null) {
			throw new TmxReadException(Messages.getString("document.TmxReader.parseTmxFileError"));
		}
		// 验证TMX ,解析Header XMLElement，将节点导航到Body XMLElement
		header = new TmxHeader();
		validateTmxAndParseHeader(vg);

		tuAp = new AutoPilot(vu.getVTDNav());
		try {
			tuAp.selectXPath("./tu");
		} catch (XPathParseException e) {
			throw new TmxReadException(Messages.getString("document.TmxReader.parseTmxFileError"));
		}
	}

	public TmxReader(String tmxContent) throws TmxReadException {
		// 解析文件
		VTDGen vg = new VTDGen();
		vg.setDoc(tmxContent.getBytes());
		String message = "";
		try {
			vg.parse(true);
		} catch (EncodingException e) {
			logger.error(Messages.getString("document.ImportAbstract.logger1"), e);
			message = Messages.getString("document.ImportAbstract.msg1");
			throw new TmxReadException(message + e.getMessage());
		} catch (ParseException e) {
			logger.error(Messages.getString("document.ImportAbstract.logger3"), e);
			String errMsg = e.getMessage();
			if (errMsg.indexOf("invalid encoding") != -1) { // 编码异常
				message = Messages.getString("document.ImportAbstract.msg1");
			} else {
				message = Messages.getString("document.ImportAbstract.msg3");
			}
			throw new TmxReadException(message + e.getMessage());
		}
		header = new TmxHeader();
		validateTmxAndParseHeader(vg);
		tuAp = new AutoPilot(vu.getVTDNav());
		try {
			tuAp.selectXPath("./tu");
		} catch (XPathParseException e) {
			throw new TmxReadException(Messages.getString("document.TmxReader.parseTmxFileError"));
		}
	}

	public TmxReaderEvent read() {
		TmxTU tu = null;
		try {
			if (tuAp.evalXPath() != -1) {
				tu = new TmxTU();
				readTuElementAttribute(tu);
				readTuNoteElement(tu);
				readTuPropElement(tu);
				readTuTuvElement(tu);
			} else {
				return new TmxReaderEvent(null, TmxReaderEvent.END_FILE);
			}
		} catch (VTDException e) {
			return new TmxReaderEvent(null, TmxReaderEvent.READ_EXCEPTION);
		}
		return new TmxReaderEvent(tu, TmxReaderEvent.NORMAL_READ);
	}

	private void readTuTuvElement(TmxTU tu) throws VTDException {
		VTDNav vn = vu.getVTDNav();
		vn.push();
		AutoPilot ap = new AutoPilot(vn);
		ap.selectXPath("./tuv");
		// TUV 节点下的Note,Prop节点暂时不处理，所以此处暂时不解析
		while (ap.evalXPath() != -1) {
			int inx = vn.getAttrVal("xml:lang");
			inx = inx == -1 ? vn.getAttrVal("lang") : inx;
			String lang = inx != -1 ? vn.toString(inx) : null;
			if (lang == null) {
				continue;
			}

			vn.push();
			if (vu.pilot("./seg") != -1) {
				String fullText = vu.getElementContent().trim();
				String pureText = DocUtils.getTmxTbxPureText(vu).trim();
				if (fullText == null || pureText == null || fullText.equals("") || pureText.equals("")) {
					// fix Bug #2928 by Jason SQLite--导入TMX异常, 导入程序正常退出，但是未完全导入所有内容，此处在continue时应该先调用vn.pop()
					vn.pop();
					continue;
				}
				TmxSegement segment = new TmxSegement();
				segment.setLangCode(Utils.convertLangCode(lang));
				// fix Bug #3406 by yule --xliff中的标记可能与TMX标记不兼容。
				if (tmxFilter == null)
					// segment.setFullText(InnerTagClearUtil.clearXliffTag4Tmx(fullText));
					segment.setFullText(fullText); // 不在导入时清理
				else {
					String text = tmxFilter.clearString(fullText);
					// text = InnerTagClearUtil.clearXliffTag4Tmx(text);// 不在导入时清理
					segment.setFullText(text);
				}
				segment.setPureText(pureText);
				if (lang.equalsIgnoreCase(header.getSrclang())) {
					tu.setSource(segment);
				} else {
					tu.appendSegement(segment);
				}
			}
			vn.pop();
		}
		vn.pop();
	}

	private void readTuPropElement(TmxTU tu) throws VTDException {
		VTDNav vn = vu.getVTDNav();
		vn.push();
		AutoPilot ap = new AutoPilot(vn);
		ap.selectXPath("./prop");
		while (ap.evalXPath() != -1) {
			String content = vu.getElementContent();
			if (content == null) {
				continue;
			}
			int inx = vn.getAttrVal("type");
			String typeValue = inx != -1 ? vn.toString(inx) : null;
			if (typeValue == null) {
				continue;
			}
			if (typeValue.equals(TmxContexts.PRE_CONTEXT_NAME)) {
				tu.appendContext(TmxContexts.PRE_CONTEXT_NAME, content.trim());
			} else if (typeValue.equals(TmxContexts.NEXT_CONTEXT_NAME)) {
				tu.appendContext(TmxContexts.NEXT_CONTEXT_NAME, content.trim());
			} else if (typeValue.equals("x-Context")) {
				// Trados TMX file
				String[] contexts = content.split(",");
				if (contexts.length == 2) {
					tu.appendContext(TmxContexts.PRE_CONTEXT_NAME, contexts[0].trim());
					tu.appendContext(TmxContexts.NEXT_CONTEXT_NAME, contexts[1].trim());
				}
			} else {
				TmxProp p = new TmxProp(typeValue, content);
				tu.appendProp(p);
			}
		}
		vn.pop();
	}

	private void readTuNoteElement(TmxTU tu) throws VTDException {
		VTDNav vn = vu.getVTDNav();

		vn.push();
		AutoPilot ap = new AutoPilot(vn);
		ap.selectXPath("./note");
		while (ap.evalXPath() != -1) {
			String fragment = vu.getElementFragment();
			TmxNote note = new TmxNote();
			note.setContent(fragment);
			int inx = vn.getAttrVal("xml:lang");
			String value = inx != -1 ? vn.toString(inx) : null;
			if (value != null) {
				note.setXmlLang(value);
			}
			inx = vn.getAttrVal("o-encoding");
			value = inx != -1 ? vn.toString(inx) : null;
			if (value != null) {
				note.setXmlLang(value);
			}
			tu.appendNote(note);
		}
		vn.pop();
	}

	private void readTuElementAttribute(TmxTU tu) throws VTDException {
		VTDNav vn = vu.getVTDNav();

		vn.push();
		AutoPilot apAttributes = new AutoPilot(vu.getVTDNav());
		apAttributes.selectXPath("@*");
		int inx = -1;
		while ((inx = apAttributes.evalXPath()) != -1) {
			String name = vn.toString(inx);
			inx = vn.getAttrVal(name);
			String value = inx != -1 ? vn.toString(inx) : "";
			// tuid, o-encoding, datatype, usagecount, lastusagedate, creationtool, creationtoolversion, creationdate,
			// creationid, changedate, segtype, changeid, o-tmf, srclang.
			if (name.equals("tuid")) {
				tu.setTuId(value);
			} else if (name.equals("creationtool")) {
				tu.setCreationTool(value);
			} else if (name.equals("creationtoolversion")) {
				tu.setCreationToolVersion(value);
			} else if (name.equals("creationdate")) {
				tu.setCreationDate(value);
			} else if (name.equals("creationid")) {
				tu.setCreationUser(value);
			} else if (name.equals("changedate")) {
				tu.setChangeDate(value);
			} else if (name.equals("changeid")) {
				tu.setChangeUser(value);
			} else {
				tu.appendAttribute(name, value);
			}
		}
		vn.pop();
	}

	/**
	 * Validate TMX Format,and pilot to Body XMLElement
	 * @param vg
	 * @throws TmxReadException
	 *             ;
	 */
	private void validateTmxAndParseHeader(VTDGen vg) throws TmxReadException {
		VTDNav vn = vg.getNav();
		AutoPilot ap = new AutoPilot(vn);
		String rootPath = "/tmx";
		vu = new VTDUtils();
		try {
			vu.bind(vn);
			ap.selectXPath(rootPath);
			if (ap.evalXPath() == -1) {
				throw new TmxReadException(Messages.getString("document.TmxReader.validateTmxFileError"));
			}
			ap.resetXPath();
			ap.selectXPath("/tmx/header");
			if (ap.evalXPath() == -1) {
				throw new TmxReadException(Messages.getString("document.TmxReader.validateTmxFileError"));
			}
			int id = vu.getVTDNav().getAttrVal("srclang");
			if (id == -1) {
				throw new TmxReadException(Messages.getString("document.TmxReader.validateTmxFileError"));
			}
			header.setSrclang(vu.getVTDNav().toString(id).trim());

			if (vu.pilot("/tmx/body") == -1) {
				throw new TmxReadException(Messages.getString("document.TmxReader.validateTmxFileError"));
			}
			// compute total tu number
			this.totalTu = vu.getChildElementsCount();
		} catch (VTDException e) {
			logger.error("", e);
			throw new TmxReadException(Messages.getString("document.TmxReader.parseTmxFileError") + e.getMessage());
		} finally {
			vg.clear();
		}
	}

	/**
	 * Parse file with VTD-XML
	 * @param file
	 * @return
	 * @throws TmxReadException
	 *             All Exception come from VTDExcetpion;
	 */
	private VTDGen paseFile(File file) throws TmxReadException {
		String encoding = FileEncodingDetector.detectFileEncoding(file);
		VTDGen vg = new VTDGen();
		FileInputStream fis = null;
		String message = "";
		try {
			fis = new FileInputStream(file);
			byte[] bArr = new byte[(int) file.length()];

			int offset = 0;
			int numRead = 0;
			int numOfBytes = 1048576;// I choose this value randomally,
			// any other (not too big) value also can be here.
			if (bArr.length - offset < numOfBytes) {
				numOfBytes = bArr.length - offset;
			}
			while (offset < bArr.length && (numRead = fis.read(bArr, offset, numOfBytes)) >= 0) {
				offset += numRead;
				if (bArr.length - offset < numOfBytes) {
					numOfBytes = bArr.length - offset;
				}
			}

			// clean invalid XML character
			if (!(encoding.equalsIgnoreCase("UTF-16LE") || encoding.equalsIgnoreCase("UTF-16BE"))) {
				byte[] _bArr = new byte[bArr.length];
				int _bArrIndx = 0;
				int type = 0;
				for (int i = 0; i < bArr.length; i++) {
					byte b = bArr[i];
					if ((b >= type && b <= 8) || b == 11 || b == 12 || (b >= 14 && b <= 31)) {
						continue;
					} else if (b == 38 && i + 1 < bArr.length && bArr[i + 1] == 35 && i + 2 < bArr.length) {// &#
						List<Byte> entis = new ArrayList<Byte>();
						entis.add((byte) 38);
						entis.add((byte) 35);
						int j = i + 2;
						if (bArr[j] == 120) {// x
							entis.add((byte) 120);
							while (true) {
								j++;
								if (j >= bArr.length) {
									entis.clear();
									b = bArr[i];
									break;
								}
								b = bArr[j];
								if ((b >= 48 && b <= 57) || (b >= 97 && b <= 102) || (b >= 65 && b <= 70)) {
									entis.add(b);
								} else if (b == 59) {
									entis.add(b);
									i = j;
									break;
								} else if (j - i > 10) {
									entis.clear();
									b = bArr[i];
									break;
								} else {
									entis.clear();
									b = bArr[i];
									break;
								}
							}
						} else {
							while (true) {
								b = bArr[j];
								if ((b >= 48 && b <= 57)) {
									entis.add(b);
								} else if (b == 59) {
									entis.add(b);
									i = j;
									break;
								} else if (j - i > 10) {
									entis.clear();
									b = bArr[i];
									break;
								} else {
									entis.clear();
									b = bArr[i];
									break;
								}
								j++;
								if (j >= bArr.length) {
									entis.clear();
									b = bArr[i];
									break;
								}
							}
						}
						if (!entis.isEmpty()) {
							byte[] t = new byte[entis.size()];
							for (int ti = 0; ti < entis.size(); ti++) {
								t[ti] = entis.get(ti);
							}
							String s = new String(t);
							if (s.matches("((&#[x]?)(([0]?([0-8]|[BbCcEe]))|(1[0-9])|(1[a-fA-F]));)")) {
								continue;
							}
						}
					}
					_bArr[_bArrIndx++] = b;
				}
				bArr = null;
				bArr = Arrays.copyOf(_bArr, _bArrIndx);
			}
			// use vtd parse
			vg.setDoc(bArr);
			vg.parse(true);
		} catch (IOException e) {
			logger.error(Messages.getString("document.DocUtils.logger1"), e);
			throw new TmxReadException(Messages.getString("document.TmxReader.parseTmxFileError"));
		} catch (EncodingException e) {
			logger.error(Messages.getString("document.ImportAbstract.logger1"), e);
			message = Messages.getString("document.ImportAbstract.msg1");
			throw new TmxReadException(message + e.getMessage());
		} catch (ParseException e) {
			logger.error(Messages.getString("document.ImportAbstract.logger3"), e);
			String errMsg = e.getMessage();
			if (errMsg.indexOf("invalid encoding") != -1) { // 编码异常
				message = Messages.getString("document.ImportAbstract.msg1");
			} else {
				message = Messages.getString("document.ImportAbstract.msg3");
			}
			throw new TmxReadException(message + e.getMessage());
		} finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (Exception e) {
				}
			}
		}
		return vg;
	}

	public TmxHeader getTmxHeader() {
		return header;
	}

	public int getTotalTu() {
		return totalTu;
	}

	/**
	 * 此方法用于清除导入TMX时清除第三方标记，如 sdl 2007 中的 ut 标签，清除标记请在以下方法中加入
	 * @param isClear
	 *            ;
	 */
	public void tryToClearTags(boolean isClear) {
		if (isClear) {
			if (header == null || vu == null || tuAp == null) {
				return;
			}
			try {
				String creationtool = vu.getElementAttribute("/tmx/header", "creationtool");
				String creationtoolversion = vu.getElementAttribute("/tmx/header", "creationtoolversion");
				if (creationtool == null || creationtoolversion == null) {
					return;
				}
				if (creationtool.equals(TmxFilterInterface.SDL_2007_FOR_WIN)) {
					tmxFilter = new TmxFilterSDL2007Impl();
				}
			} catch (XPathParseException e) {
				e.printStackTrace();
			} catch (XPathEvalException e) {
				e.printStackTrace();
			} catch (NavException e) {
				e.printStackTrace();
			}

		}

	}

	/**
	 * 获取 tmxfile 中的所有语言
	 * @return
	 */
	public List<String> getLangs() {
		VTDNav vn = vu.getVTDNav();
		vn.push();
		List<String> langs = new LinkedList<String>();
		langs.add(header.getSrclang());
		AutoPilot ap = new AutoPilot(vn);
		try {
			ap.selectXPath("/tmx/body/tu/tuv");
			String lang;
			int index = -1;
			while (ap.evalXPath() != -1) {
				index = vn.getAttrVal("xml:lang");
				if (index == -1)
					continue;
				lang = vn.toRawString(index);
				if (!langs.contains(lang)) {
					langs.add(lang);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		vn.pop();
		return langs;
	}
}

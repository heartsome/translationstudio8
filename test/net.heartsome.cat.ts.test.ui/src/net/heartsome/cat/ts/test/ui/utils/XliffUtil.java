package net.heartsome.cat.ts.test.ui.utils;

import java.util.ArrayList;
import java.util.List;

import net.heartsome.cat.common.innertag.TagStyle;
import net.heartsome.cat.common.ui.utils.InnerTagUtil;
import net.heartsome.cat.ts.core.file.RowIdUtil;
import net.heartsome.xml.vtdimpl.VTDUtils;

import com.ximpleware.AutoPilot;
import com.ximpleware.NavException;
import com.ximpleware.VTDGen;
import com.ximpleware.VTDNav;
import com.ximpleware.XPathEvalException;
import com.ximpleware.XPathParseException;

/**
 * 通过文本段的 RowID 在对应的 XLIFF 文件中读取相关内容。<br/>
 * 使用了 HSCAT8 项目中的 net.heartsome.cat.ts.core.file.RowIdUtil 和 net.heartsome.xml.vtdimpl.VTDUtils 两个类。
 * @author felix_lu
 */
public class XliffUtil {

	private VTDGen vg;
	private VTDUtils vu;
	private String rowID;
	private String xlfFile;
	private String fileXPath;
	private String tuid;

	/**
	 * 直接通过 RowID 来定位，适用于最常见的在界面 NatTable 中显示的文本段。
	 * @param rowID
	 */
	public XliffUtil(String rowID) {
		this.rowID = rowID;
		this.xlfFile = RowIdUtil.getFileNameByRowId(rowID);
		this.fileXPath = RowIdUtil.getFileXpathByRowId(rowID);
		this.tuid = RowIdUtil.getTUIdByRowId(rowID);
		this.vg = new VTDGen();
		if (vg.parseFile(xlfFile, true)) {
			VTDNav vn = vg.getNav();
			try {
				vu = new VTDUtils(vn);
			} catch (NavException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 通过可组成 RowID 的三个参数来定位，适用于不在界面 NatTable 中显示的文本段， 如合并后变为空的 trans-unit 节点。
	 * @param xlfFile
	 *            XLIFF 文件路径
	 * @param originalFile
	 *            源文件路径，建议从相邻文本段中用本类方法获取
	 * @param tuid
	 *            翻译单元 ID
	 */
	public XliffUtil(String xlfFile, String originalFile, String tuid) {
		this.xlfFile = xlfFile;
		this.fileXPath = "/xliff/file[@original=\"" + originalFile + "\"]";
		this.tuid = tuid;
		this.rowID = RowIdUtil.getRowId(xlfFile, originalFile, tuid);
		this.vg = new VTDGen();
		if (vg.parseFile(xlfFile, true)) {
			VTDNav vn = vg.getNav();
			try {
				vu = new VTDUtils(vn);
			} catch (NavException e) {
				e.printStackTrace();
			}
		}
	}

	/* ******** Getter ******** */

	/**
	 * @return ;
	 */
	public String getXlfFile() {
		return xlfFile;
	}

	/**
	 * @return ;
	 */
	public String getFileXPath() {
		return fileXPath;
	}

	/**
	 * @return String 当前实例的 tuid 成员变量值<br/>
	 *         <b>请注意</b>：该值可能和 XLIFF 文件中的实际值不一致，若要获得文件中的属性值，应使用 getAttributeOfTU() 方法
	 */
	public String getTUID() {
		return tuid;
	}

	/**
	 * @return ;
	 */
	public String getRowID() {
		return rowID;
	}

	/* * 一些与 rowID 无关的静态方法 * */

	/**
	 * @param xlfFile
	 *            XLIFF 文件路径
	 * @return VTDUtils
	 */
	public static VTDUtils getVU(String xlfFile) {
		VTDGen vg = new VTDGen();
		if (vg.parseFile(xlfFile, true)) {
			VTDNav vn = vg.getNav();
			try {
				return new VTDUtils(vn);
			} catch (NavException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	/**
	 * @param xlfFile
	 *            XLIFF 文件路径
	 * @return List&lt;String&gt; &lt;file&gt; 节点 original 属性值所组成的 List
	 */
	public static List<String> getOriginalFiles(String xlfFile) {
		return getOriginalFiles(getVU(xlfFile));
	}

	/**
	 * @param vu
	 *            XLIFF 文件对应的 VTDUtils
	 * @return List&lt;String&gt;
	 */
	public static List<String> getOriginalFiles(VTDUtils vu) {
		ArrayList<String> list = new ArrayList<String>();
		String xpath = "/xliff/file";
		AutoPilot ap = new AutoPilot(vu.getVTDNav());
		try {
			ap.selectXPath(xpath);
			while (ap.evalXPath() != -1) {
				list.add(vu.getElementAttribute(".", "original"));
			}
		} catch (XPathParseException e) {
			e.printStackTrace();
		} catch (XPathEvalException e) {
			e.printStackTrace();
		} catch (NavException e) {
			e.printStackTrace();
		}
		return list;
	}

	/**
	 * @param xlfFile
	 *            XLIFF 文件路径
	 * @return int &lt;file&gt; 节点的数量，即源文件数量
	 */
	public static int fileCount(String xlfFile) {
		return getOriginalFiles(xlfFile).size();
	}

	/**
	 * @param xlfFile
	 * @param includingNull
	 *            是否包括空节点
	 * @return int trans-unit 节点数量，包括空节点
	 */
	public static int tuCount(String xlfFile, boolean includingNull) {
		String xpath = "/xliff/file";
		return tuCountOfFile(xlfFile, xpath, includingNull);
	}

	/**
	 * @param xlfFile
	 * @param fileXPath
	 * @param includingNull
	 *            是否包括空节点
	 * @return int 指定 file 节点下的 trans-unit 节点数量
	 */
	public static int tuCountOfFile(String xlfFile, String filePath, boolean includingNull) {
		VTDUtils vu = getVU(xlfFile);
		if (includingNull) {
			String xpath = "/xliff/file[@original=\"" + filePath + "\"]/body//trans-unit";
			return vu.getValues(xpath).size(); // 包括空节点
		} else {
			int count = 0;
			String xpath = "/xliff/file[@original=\"" + filePath + "\"]/body//trans-unit/source/text()";
			List<String> srcTexts = vu.getValues(xpath);
			for (String srcText : srcTexts) {
				if (srcText != null && !srcText.equals("")) {
					count++; // 不含空节点
				}
			}
			return count;
		}
	}

	/**
	 * @param xlfFile
	 * @param srcLang
	 * @param tgtLang
	 * @param includingNull
	 * @return int 指定语言对的 trans-unit 节点数量，以 file 节点的源、目标语言为准
	 */
	public static int tuCountOfLangPair(String xlfFile, String srcLang, String tgtLang, boolean includingNull) {
		VTDUtils vu = getVU(xlfFile);
		List<String> orgFiles = getOriginalFiles(vu);
		int tuCount = 0;
		for (String orgFile : orgFiles) {
			String[] langPair = getLangPairOfFile(xlfFile, orgFile);
			if (srcLang.equals(langPair[0]) && tgtLang.equals(langPair[1])) {
				int count = tuCountOfFile(xlfFile, orgFile, includingNull);
				tuCount += count;
			}
		}
		return tuCount;
	}

	/**
	 * @param xlfFile
	 * @param filePath
	 * @return String[] 第一个元素为源语言，第二个为目标语言
	 */
	public static String[] getLangPairOfFile(String xlfFile, String filePath) {
		VTDUtils vu = getVU(xlfFile);
		String xpath = "/xliff/file[@original=\"" + filePath + "\"]";
		String[] langPair = new String[2];
		try {
			langPair[0] = vu.getElementAttribute(xpath, "source-language");
			langPair[1] = vu.getElementAttribute(xpath, "target-language");
		} catch (XPathParseException e) {
			e.printStackTrace();
		} catch (XPathEvalException e) {
			e.printStackTrace();
		} catch (NavException e) {
			e.printStackTrace();
		}
		return langPair;
	}

	/**
	 * @param xlfFile
	 * @return List&lt;String[]&gt; 由源、目标语言数组组成的 List
	 */
	public static List<String[]> getAllLangPairs(String xlfFile) {
		ArrayList<String[]> langPairs = new ArrayList<String[]>();
		List<String> orgFiles = getOriginalFiles(xlfFile);
		for (String orgFile : orgFiles) { // 查找每个 <file> 节点的源、目标语言
			String[] langPair = getLangPairOfFile(xlfFile, orgFile);
			if (langPairs.size() == 0) { // 若 List 为空，直接添加
				langPairs.add(langPair);
			} else {
				boolean noRepeat = false;
				for (String[] langP : langPairs) { // 对比 List 中已有的每个语言对
					if (langP[0].equals(langPair[0]) && langP[1].equals(langPair[1])) {
						noRepeat = false; // 只要 List 中已有相同的语言对，就标记为重复（可能在上一轮循环中认为无重复）
						break; // 并跳出循环
					} else {
						noRepeat = true;
					}
				}
				if (noRepeat) { // 只有在对比过 List 中所有语言对之后，noRepeat 仍为 true，才能确认该语言对没有重复
					langPairs.add(langPair);
				}
			}
		}
		return langPairs;
	}

	/**
	 * @param text
	 *            纯文本
	 * @return String 简单标记形式显示的文本
	 */
	public static String tagged(String text) {
		StringBuffer t = new StringBuffer(text);
		InnerTagUtil.parseXmlToDisplayValue(t, TagStyle.getDefault()); // TODO: 需要修改相关代码，使之支持不同的标记样式，而不只是默认的
		return t.toString();
	}

	/* ******** <file> 节点相关内容 ******** */

	/**
	 * @return String 源文件路径，从 &lt;file&gt; 节点的 XPath 中截取
	 */
	public String getOriginalFile() {
		int fromIndex = fileXPath.indexOf("\"") + 1;
		int toIndex = fileXPath.indexOf("\"", fromIndex);
		return fileXPath.substring(fromIndex, toIndex);
	}

	/**
	 * @param attribute
	 *            属性名称，可以为：original source-language datatype tool tool-id date xml:space ts category target-language
	 *            product-name product-version build-num
	 * @return String 属性值
	 */
	public String getAttributeOfFile(String attribute) {
		String xpath = RowIdUtil.getFileXpathByRowId(rowID);
		try {
			return vu.getElementAttribute(xpath, attribute);
		} catch (XPathParseException e) {
			e.printStackTrace();
		} catch (XPathEvalException e) {
			e.printStackTrace();
		} catch (NavException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * @return List&lt;String&gt; 当前 XLIFF 文件中所有 &lt;file&gt; 节点的 original 属性
	 */
	public List<String> getOriginalFiles() {
		return getOriginalFiles(vu);
	}

	/**
	 * @return String &lt;file&gt; 节点中的源语言属性值
	 */
	public String getSourceLangOfFile() {
		return getAttributeOfFile("source-language");
	}

	/**
	 * @return String &lt;file&gt; 节点中的目标语言属性值;
	 */
	public String getTargetLangOfFile() {
		return getAttributeOfFile("target-language");
	}

	/**
	 * @return boolean True 表示骨架内嵌
	 */
	public boolean isSkeletonEmbed() {
		String xpath = fileXPath + "/header/skl/internal-file";
		return vu.pilot(xpath) != -1;
	}

	/**
	 * @return String 骨架文件路径
	 */
	public String getSkeletonPath() {
		if (isSkeletonEmbed()) {
			return null;
		} else {
			String xpath = fileXPath + "/header/skl/external-file";
			try {
				return vu.getElementAttribute(xpath, "href");
			} catch (XPathParseException e) {
				e.printStackTrace();
			} catch (XPathEvalException e) {
				e.printStackTrace();
			} catch (NavException e) {
				e.printStackTrace();
			}
			return null;
		}
	}

	/* ******** <trans-unit> 节点相关内容 ******** */

	/**
	 * @param attribute
	 *            trans-unit 属性名称，可以为：id approved translate reformat xml:space datatype ts phase-name restype resname
	 *            extradata help-id menu menu-option menu-name coord font css-style style exstyle extype maxbytes
	 *            minbytes size-unit maxheight minheight maxwidth minwidth charclass
	 * @return String 属性值
	 */

	public String getAttributeOfTU(String attribute) {
		String xpath = fileXPath + "/body//trans-unit[@id=\"" + tuid + "\"]";
		try {
			return vu.getElementAttribute(xpath, attribute);
		} catch (XPathParseException e) {
			e.printStackTrace();
		} catch (XPathEvalException e) {
			e.printStackTrace();
		} catch (NavException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * @return boolean True 表示可翻译
	 */
	public boolean tuIsTranslatable() {
		String value = getAttributeOfTU("translate");
		return "yes".equals(value);
	}

	/**
	 * @return boolean True 表示已批准
	 */
	public boolean tuIsApproved() {
		String value = getAttributeOfTU("approved");
		return "yes".equals(value);
	}

	/**
	 * @return boolean True 可以编辑，即未批准、未锁定
	 */
	public boolean tuIsEditable() {
		return tuIsTranslatable() && !tuIsApproved();
	}

	/**
	 * @return String size 单位
	 */
	public String getTUSizeUnit() {
		return getAttributeOfTU("size-unit");
	}

	/**
	 * @return String 最大宽度，即最大字符长度
	 */
	public String getTUMaxWidth() {
		return getAttributeOfTU("maxwidth");
	}

	/**
	 * @return String 最小宽度，即最少字符长度
	 */
	public String getTUMinWidth() {
		return getAttributeOfTU("minwidth");
	}

	/**
	 * @return String 同一个 <file> 节点中下一个非空 trans-unit 节点的 tuid
	 */
	// public String tuidOfNextNotNullTU() {
	// String nTUID;
	// String xpath = fileXPath + "/body//trans-unit[@id=\"" + tuid + "\"]";
	// AutoPilot ap = new AutoPilot(vu.getVTDNav());
	// try {
	// ap.selectXPath(xpath);
	//
	// } catch (XPathParseException e) {
	// e.printStackTrace();
	// }
	//
	//
	// return nTUID;
	// }

	/**
	 * @return XliffUtil XLIFF 文件中当前文本段的下一文本段对应的 XliffUtil 对象，若无则为 null
	 */
	public XliffUtil getNextXU() {
		List<String> tuids = getAllTuIds();
		int size = tuids.size();
		int index = tuids.indexOf(tuid);
		XliffUtil nextXU = null;
		if (index < size - 1) {
			int newIndex = index + 1;
			nextXU = new XliffUtil(xlfFile, getOriginalFile(), tuids.get(newIndex));
		}
		return nextXU;
	}

	/**
	 * @return XliffUtil XLIFF 文件中当前文本段的下一非空文本段对应的 XliffUtil 对象， 若找到的文本段源文本内容为 null 或空字符串，则继续查找下一个，直到找到非空文本段或 到达 file
	 *         节点末尾为止，若未找到非空文本段则返回 null
	 */
	public XliffUtil getNextNotNullXU() {
		XliffUtil nextXU = getNextXU();
		if (nextXU != null) {
			String nextSourceText = nextXU.getSourceText();
			if (nextSourceText != null && !"".equals(nextSourceText)) {
				return nextXU;
			} else {
				return nextXU.getNextNotNullXU();
			}
		} else {
			return null;
		}
	}

	/**
	 * @return XliffUtil XLIFF 文件中当前文本段的上一文本段对应的 XliffUtil 对象，若无则为 null
	 */
	public XliffUtil getPrevXU() {
		List<String> tuids = getAllTuIds();
		int index = tuids.indexOf(tuid);
		XliffUtil prevXU = null;
		if (index > 0) {
			int newIndex = index - 1;
			prevXU = new XliffUtil(xlfFile, getOriginalFile(), tuids.get(newIndex));
		}
		return prevXU;
	}

	/**
	 * @return XliffUtil XLIFF 文件中当前文本段的上一非空文本段对应的 XliffUtil 对象， 若找到的文本段源文本内容为 null 或空字符串，则继续查找上一个，直到找到非空文本段或 到达 file
	 *         节点起始位置为止，若未找到非空文本段则返回 null
	 */
	public XliffUtil getPrevNotNullXU() {
		XliffUtil prevXU = getPrevXU();
		if (prevXU != null) {
			String prevSourceText = prevXU.getSourceText();
			if (prevSourceText != null && !"".equals(prevSourceText)) {
				return prevXU;
			} else {
				return prevXU.getPrevNotNullXU();
			}
		} else {
			return null;
		}
	}

	/**
	 * @return List&lt;String&gt; 包含当前 file 节点下所有 trans-unit 节点的 id 属性值
	 */
	public List<String> getAllTuIds() {
		String xpath = fileXPath + "/body";
		if (vu.pilot(xpath) != -1) {
			return vu.getValues("descendant::trans-unit/@id");
			// 不能直接用 /body//trans-unit 形式的 XPath，否则在有 group 节点时会打乱找到的trans-unit id 顺序
		}
		return null;
	}

	/* ******** <group> 节点相关内容 ******** */

	/**
	 * @param attribute
	 *            group 属性名称，可以为 id, datatype, xml:space, ts, restype, resname, extradata, help-id, menu, menu-option,
	 *            menu-name, coord, font, css-style, style, exstyle, extype, translate, reformat, maxbytes, minbytes,
	 *            size-unit, maxheight, minheight, maxwidth, minwidth, charclass, merged-trans
	 * @return String 属性值
	 */
	public String getAttributeOfGroup(String attribute) {
		String tuXPath = fileXPath + "/body//trans-unit[@id=\"" + tuid + "\"]";
		AutoPilot ap = new AutoPilot(vu.getVTDNav());
		if (vu.pilot(ap, tuXPath) != -1) {
			return vu.getValue(ap, "parent::group/@" + attribute);
		}
		return null;
	}

	/* ******** <source> <target> 节点相关内容 ******** */

	/**
	 * @param attribute
	 *            &lt;source&gt; 属性名称，可以为：xml:lang ts
	 * @return String 属性值
	 */
	public String getAttributeOfTUSource(String attribute) {
		String xpath = fileXPath + "/body//trans-unit[@id=\"" + tuid + "\"]/source";
		try {
			return vu.getElementAttribute(xpath, attribute);
		} catch (XPathParseException e) {
			e.printStackTrace();
		} catch (XPathEvalException e) {
			e.printStackTrace();
		} catch (NavException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * @param attribute
	 *            &lt;target&gt; 属性名称，可以为：state state-qualifier phase-name xml:lang ts restype resname coord font
	 *            css-style style exstyle equiv-trans
	 * @return String 属性值
	 */
	public String getAttributeOfTUTarget(String attribute) {
		String xpath = fileXPath + "/body//trans-unit[@id=\"" + tuid + "\"]/target";
		try {
			return vu.getElementAttribute(xpath, attribute);
		} catch (XPathParseException e) {
			e.printStackTrace();
		} catch (XPathEvalException e) {
			e.printStackTrace();
		} catch (NavException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * @param status
	 *            状态，可为 final, needs-adaptation, needs-l10n, needs-review-adaptation, needs-review-l10n,
	 *            needs-review-translation, needs-translation, new, signed-off, translated
	 * @return boolean True 表示目标文本状态相符
	 */
	public boolean targetStatusIs(String status) {
		String value = getAttributeOfTUTarget("state");
		return (status == null && value == null || status.equals(value));
	}

	/**
	 * @return String 目标文本状态，无目标文本时为 null
	 */
	public String getTargetStatus() {
		return getAttributeOfTUTarget("state");
	}

	/**
	 * @return String 源文本内容
	 */
	public String getSourceText() {
		String xpath = fileXPath + "/body//trans-unit[@id=\"" + tuid + "\"]/source/text()";
		return vu.getValue(xpath);
	}

	/**
	 * @return String 简单标记形式显示的源文本内容
	 */
	public String getSourceTextTagged() {
		String text = getSourceText();
		if (text == null) {
			return null;
		} else {
			return tagged(text);
		}
	}

	/**
	 * @return String 目标文本内容
	 */
	public String getTargetText() {
		String xpath = fileXPath + "/body//trans-unit[@id=\"" + tuid + "\"]/target/text()";
		return vu.getValue(xpath);
	}

	/**
	 * @return String 简单标记形式显示的目标文本内容
	 */
	public String getTargetTextTagged() {
		String text = getTargetText();
		if (text == null) {
			return null;
		} else {
			return tagged(text);
		}
	}

	/**
	 * @return String 源语言代码
	 */
	public String getTUSourceLang() {
		return getAttributeOfTUSource("xml:lang");
	}

	/**
	 * @return String 目标语言代码
	 */
	public String getTUTargetLang() {
		return getAttributeOfTUTarget("xml:lang");
	}

	/* ******** <alt-trans> 节点相关内容 ******** */

	/**
	 * @param index
	 *            alt-trans 索引号
	 * @param attribute
	 *            属性名称，可以为：mid match-quality tool tool-id crc xml:lang datatype xml:space ts restype resname extradata
	 *            help-id menu menu-option menu-name coord font css-style style exstyle extype origin phase-name
	 *            alttranstype
	 * @return String 属性值
	 */
	public String getAttributeOfAltTrans(int index, String attribute) {
		String xpath = fileXPath + "/body//trans-unit[@id=\"" + tuid + "\"]/alt-trans[" + String.valueOf(index) + "]";
		try {
			return vu.getElementAttribute(xpath, attribute);
		} catch (XPathParseException e) {
			e.printStackTrace();
		} catch (XPathEvalException e) {
			e.printStackTrace();
		} catch (NavException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * @param index
	 *            &lt;alt-trans&gt; 索引号
	 * @return boolean True 表示该匹配来自 TM
	 */
	public boolean altTransIsTMMatch(int index) { // FIXME 实现已改，需要调整
		return !"XLFEditor auto-Quick Translation".equals(getAltTransToolID(index))
				&& !"Google Translate".equals(getAltTransOrigin(index))
				&& !"style:ITALIC".equals(getAltTransFont(index));
	}

	/**
	 * @param index
	 *            &lt;alt-trans&gt; 索引号
	 * @return boolean True 表示该匹配为快速翻译
	 */
	public boolean altTransIsQuickTranslation(int index) { // FIXME 实现已改，需要调整
		return "XLFEditor auto-Quick Translation".equals(getAltTransToolID(index))
				&& !"Google Translate".equals(getAltTransOrigin(index))
				&& !"style:ITALIC".equals(getAltTransFont(index));
	}

	/**
	 * @param index
	 *            &lt;alt-trans&gt; 索引号
	 * @return boolean True 表示该匹配为 Google 机器翻译
	 */
	public boolean altTransIsGoogleTranslation(int index) { // FIXME 实现已改，需要调整
		return "XLFEditor auto-Quick Translation".equals(getAltTransToolID(index))
				&& "Google Translate".equals(getAltTransOrigin(index)) && "style:ITALIC".equals(getAltTransFont(index));
	}

	/**
	 * @param index
	 *            &lt;alt-trans&gt; 索引号
	 * @return String 匹配来源 origin 属性值
	 */
	public String getAltTransOrigin(int index) {
		return getAttributeOfAltTrans(index, "origin");
	}

	/**
	 * @param index
	 *            &lt;alt-trans&gt; 索引号
	 * @return String 工具 tool-id 属性值
	 */
	public String getAltTransToolID(int index) {
		return getAttributeOfAltTrans(index, "tool-id");
	}

	/**
	 * @param index
	 *            &lt;alt-trans&gt; 索引号
	 * @return String 字体 font 属性值
	 */
	public String getAltTransFont(int index) {
		return getAttributeOfAltTrans(index, "font");
	}

	/**
	 * @param index
	 *            &lt;alt-trans&gt; 索引号
	 * @return int 匹配率
	 */
	public int getAltTransMatchQuality(int index) {
		String value = getAttributeOfAltTrans(index, "match-quality");
		if (value.contains("%")) {
			value = value.replace("%", "");
		}
		return Integer.valueOf(value);
	}

	/**
	 * @return int 不指定 &lt;alt-trans&gt; 索引号时，返回第一个匹配率
	 */
	public int getAltTransMatchQuality() {
		return getAltTransMatchQuality(0);
	}

	/**
	 * @return List&lt;String&gt; 翻译匹配源文本
	 */
	public List<String> getAltTransSources() {
		String xpath = fileXPath + "/body//trans-unit[@id=\"" + tuid + "\"]/alt-trans/source/text()";
		return vu.getValues(xpath);
	}

	/**
	 * @param index
	 *            &lt;alt-trans&gt; 索引号
	 * @return String &lt;alt-trans&gt; 源文本内容
	 */
	public String getAltTransSource(int index) {
		String xpath = fileXPath + "/body//trans-unit[@id=\"" + tuid + "\"]/alt-trans[" + String.valueOf(index)
				+ "]/source/text()";
		return vu.getValue(xpath);
	}

	/**
	 * @return String 不指定索引号时，返回第一个 &lt;alt-trans&gt; 源文本内容
	 */
	public String getAltTransSource() {
		return getAltTransSource(0);
	}

	/**
	 * @return List&lt;String&gt; 翻译匹配目标文本
	 */
	public List<String> getAltTransTargets() {
		String xpath = fileXPath + "/body//trans-unit[@id=\"" + tuid + "\"]/alt-trans/target/text()";
		return vu.getValues(xpath);
	}

	/**
	 * @param index
	 *            &lt;alt-trans&gt; 索引号
	 * @return String &lt;alt-trans&gt; 目标文本内容
	 */
	public String getAltTransTarget(int index) {
		String xpath = fileXPath + "/body//trans-unit[@id=\"" + tuid + "\"]/alt-trans[" + String.valueOf(index)
				+ "]/target/text()";
		return vu.getValue(xpath);
	}

	/**
	 * @return String 不指定索引号时，返回第一个 &lt;alt-trans&gt; 目标文本内容
	 */
	public String getAltTransTarget() {
		return getAltTransTarget(0);
	}

	/**
	 * @param mq
	 *            匹配率
	 * @return List&lt;String&gt; 翻译匹配源文本
	 */
	public List<String> getAltTransSourcesByMatchQuality(int mq) {
		String xpath1 = fileXPath + "/body//trans-unit[@id=\"" + tuid + "\"]/alt-trans[@match-quality=\""
				+ String.valueOf(mq) + "\"]/source/text()";
		String xpath2 = fileXPath + "/body//trans-unit[@id=\"" + tuid + "\"]/alt-trans[@match-quality=\""
				+ String.valueOf(mq) + "%\"]/source/text()";
		List<String> list = vu.getValues(xpath1);
		if (list.size() != 0) {
			return list;
		} else {
			return vu.getValues(xpath2);
		}
	}

	/**
	 * @param mq
	 *            匹配率
	 * @return List&lt;String&gt; 翻译匹配目标文本
	 */
	public List<String> getAltTransTargetsByMatchQuality(int mq) {
		String xpath1 = fileXPath + "/body//trans-unit[@id=\"" + tuid + "\"]/alt-trans[@match-quality=\""
				+ String.valueOf(mq) + "\"]/target/text()";
		String xpath2 = fileXPath + "/body//trans-unit[@id=\"" + tuid + "\"]/alt-trans[@match-quality=\""
				+ String.valueOf(mq) + "%\"]/target/text()";
		List<String> list = vu.getValues(xpath1);
		if (list.size() != 0) {
			return list;
		} else {
			return vu.getValues(xpath2);
		}
	}

	/* ******** <note> 节点相关内容 ******** */

	/**
	 * @return List&lt;String&gt; 所有 note 内容
	 */
	public List<String> getTUNotes() {
		String xpath = fileXPath + "/body//trans-unit[@id=\"" + tuid + "\"]/note/text()";
		return vu.getValues(xpath);
	}

	/**
	 * @param noteIndex
	 *            note 索引
	 * @return String 指定索引号的 note 内容
	 */
	public String getTUNote(int noteIndex) {
		String xpath = fileXPath + "/body//trans-unit[@id=\"" + tuid + "\"]/note[" + String.valueOf(noteIndex)
				+ "]/text()";
		return vu.getValue(xpath);
	}

	/**
	 * @return String 不指定 note 索引时，默认返回第一个 note 内容
	 */
	public String getTUNote() {
		return getTUNote(0);
	}
}
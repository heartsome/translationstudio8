package net.heartsome.xml.vtdimpl;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.heartsome.xml.resource.Messages;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ximpleware.AutoPilot;
import com.ximpleware.ModifyException;
import com.ximpleware.NavException;
import com.ximpleware.ParseException;
import com.ximpleware.TranscodeException;
import com.ximpleware.VTDGen;
import com.ximpleware.VTDNav;
import com.ximpleware.XMLModifier;
import com.ximpleware.XPathEvalException;
import com.ximpleware.XPathParseException;

/**
 * @author John
 * @version
 * @since JDK1.6
 */
public class VTDUtils {

	private static final Logger LOGGER = LoggerFactory.getLogger(VTDUtils.class);

	/**
	 * 前缀为“xml”的默认命名空间的 URL。
	 */
	public static final String XML_NAMESPACE_URL = "http://www.w3.org/XML/1998/namespace";

	/** VTD Navigator 对象 **/
	private VTDNav vn;

	/** VTD Generator 对象 **/
	private VTDGen vg;

	/**
	 * 导航该 XPath 语句直到末尾。
	 */
	public static final int PILOT_TO_END = 1 << 1;

	/**
	 * 如果不存在该 XPath 对应的节点则创建。
	 */
	public static final int CREATE_IF_NOT_EXIST = 1 << 2;

	/**
	 * 注意：使用此构造方法，需要手动调用 bind(VTDNav vn) 方法绑定 VTDNav
	 * @see #bind(VTDNav)
	 */
	public VTDUtils() {
	}

	/**
	 * 使用指定的 XML 导航器对象构建一个 VTDUtils 实例。导航器为 null 时无法完成构建。
	 * @param vn
	 *            XML导航器
	 * @throws NavException
	 */
	public VTDUtils(VTDNav vn) throws NavException {
		bind(vn);
	}

	/**
	 * 解析文件
	 * @param filePath
	 *            文件路径
	 * @param useNamespase
	 *            是否需要支持命名空间。如果确定所操作 XML 没有命名空间，可选择不支持（即 false），以提高解析速度。
	 * @throws ParseException
	 * @throws IOException
	 *             ;
	 */
	public void parseFile(String filePath, boolean useNamespase) throws ParseException, IOException {
		if (vg == null) {
			vg = new VTDGen();
		}
		FileInputStream fis = new FileInputStream(filePath);
		try {
			parseFile(fis, useNamespase);
		} finally {
			fis.close();
		}
	}

	/**
	 * 解析文件
	 * @param is
	 *            文件的输入流
	 * @param useNamespase
	 *            是否需要支持命名空间。如果确定所操作 XML 没有命名空间，可选择不支持（即 false），以提高解析速度。
	 * @throws ParseException
	 * @throws IOException
	 *             ;
	 */
	public void parseFile(InputStream is, boolean useNamespase) throws ParseException, IOException {
		if (vg == null) {
			vg = new VTDGen();
		}
		BufferedInputStream bis = new BufferedInputStream(is);
		try {
			int available = bis.available();
			ByteArrayOutputStream out = new ByteArrayOutputStream(available);

			int bufferLength = available > 1048576 ? 10485760 : 1048576; // 1048576 = 1024 * 1024
			byte[] buffer = new byte[bufferLength];
			int len = -1;
			while ((len = bis.read(buffer)) > 0) {
				out.write(buffer, 0, len);
			}

			vg.setDoc(out.toByteArray());
			out.close();

			vg.parse(useNamespase);
			vn = vg.getNav();
		} finally {
			bis.close();
		}
	}

	/**
	 * 将一个 VTDNav 实例附加到此 VTDUtils 实例，以便之后发生的所有操作都是基于这个 VTDNav 实例
	 * @param masterDocument
	 *            VTDNav 实例
	 * @throws NavException
	 */
	public void bind(VTDNav vn) throws NavException {
		if (vn == null) {
			throw new NavException(Messages.getString("vtdimpl.VTDUtils.logger1"));
		}
		this.vn = vn;
	}

	/**
	 * 获取当前元素节点的某个属性值。如果当前节点没有该属性，返回默认值。
	 * @exception NavException
	 */
	public String getCurrentElementAttribut(String AttributName, String defaultValue) throws NavException {
		int index = vn.getAttrVal(AttributName);
		return index != -1 ? vn.toString(index) : defaultValue;
	}

	/**
	 * 获取当前元素节点的全部属性。如果当前节点没有属性，返回 null。
	 * @exception XPathEvalException
	 *                ,XPathParseException,NavException
	 */
	public Hashtable<String, String> getCurrentElementAttributs() throws XPathParseException, XPathEvalException,
			NavException {
		vn.push();
		Hashtable<String, String> attributes = new Hashtable<String, String>();
		AutoPilot apAttributes = new AutoPilot(vn);
		apAttributes.selectXPath("@*");

		int inx = -1;
		while ((inx = apAttributes.evalXPath()) != -1) {
			String name = vn.toString(inx);
			inx = vn.getAttrVal(name);
			String value = inx != -1 ? vn.toString(inx) : "";
			attributes.put(name, value);
		}
		apAttributes.resetXPath();

		if (attributes.isEmpty()) {
			attributes = null;
		}
		vn.pop();
		return attributes;
	}

	/**
	 * 获取所有当前元素节点的全部属性。如果当前节点没有属性，返回 null。
	 * @param nsPrefix
	 *            需声明的名空间前缀，可与XML文件实际前缀不同。
	 * @param nsUrl
	 *            需声明的名空间地址，必须与 XML 文件中声明的地址一致。
	 * @exception XPathEvalException
	 *                ,XPathParseException,NavException
	 */
	public Hashtable<String, String> getCurrentElementAttributs(String nsPrefix, String nsUrl)
			throws XPathParseException, XPathEvalException, NavException {
		Hashtable<String, String> attributes = new Hashtable<String, String>();
		AutoPilot apAttributes = new AutoPilot(vn);
		apAttributes.declareXPathNameSpace(nsPrefix, nsUrl);
		apAttributes.selectXPath("@*");

		int inx = -1;
		while ((inx = apAttributes.evalXPath()) != -1) {
			String name = vn.toString(inx);
			inx = vn.getAttrVal(name);
			String value = inx != -1 ? vn.toString(inx) : "";
			attributes.put(name, value);
		}
		apAttributes.resetXPath();

		if (attributes.isEmpty()) {
			attributes = null;
		}
		return attributes;
	}

	/**
	 * 获取当前节点的名称
	 * @param index
	 *            当前节点所在的索引
	 * @return
	 * @throws NavException
	 */
	public String getCurrentElementName(int index) throws NavException {
		return vn.toString(index);
	}

	/**
	 * 获取当前节点的名称
	 * @return
	 * @throws NavException
	 */
	public String getCurrentElementName() throws NavException {
		return vn.toString(vn.getCurrentIndex());
	}

	/**
	 * 获取所有当前元素节点的全部属性。如果当前节点没有属性，返回 null。
	 * @param ns
	 *            需声明的名空间，键为地址，必须与 XML 文件中声明的地址一致。值为前缀，可与XML文件实际前缀不同。
	 * @exception XPathEvalException
	 *                ,XPathParseException,NavException
	 */
	public Hashtable<String, String> getCurrentElementAttributs(Hashtable<String, String> ns)
			throws XPathParseException, XPathEvalException, NavException {
		Hashtable<String, String> attributes = new Hashtable<String, String>();
		AutoPilot apAttributes = new AutoPilot(vn);
		if (ns != null) {
			Iterator<String> nsIt = ns.keySet().iterator();
			while (nsIt.hasNext()) {
				String nsUrl = nsIt.next();
				String nsPrefix = ns.get(nsUrl);
				apAttributes.declareXPathNameSpace(nsPrefix, nsUrl);
			}
		}

		apAttributes.selectXPath("@*");

		int inx = -1;
		while ((inx = apAttributes.evalXPath()) != -1) {
			String name = vn.toString(inx);
			inx = vn.getAttrVal(name);
			String value = inx != -1 ? vn.toString(inx) : "";
			attributes.put(name, value);
		}
		apAttributes.resetXPath();

		if (attributes.isEmpty()) {
			attributes = null;
		}
		return attributes;
	}

	/**
	 * 得到当前节点的纯文本。实体会被转义。若无文本内容返回 null。
	 * @exception XPathParseException
	 *                ,XPathEvalException,NavException
	 */
	public String getElementPureText() throws XPathParseException, XPathEvalException, NavException {
		String txtNode = "./text()";
		AutoPilot ap = new AutoPilot(vn);
		StringBuilder result = new StringBuilder();
		ap.selectXPath(txtNode);
		int txtIndex = -1;
		boolean isNull = true;
		while ((txtIndex = ap.evalXPath()) != -1) {
			result.append(vn.toString(txtIndex));
			if (isNull) {
				isNull = false;
			}
		}

		return isNull ? null : result.toString();
	}

	/**
	 * 得到当前节点的纯文本。实体不会被转义。若无文本内容返回 null。
	 * @exception XPathParseException
	 *                ,XPathEvalException,NavException
	 */
	public String getElementRawPureText() throws XPathParseException, XPathEvalException, NavException {
		String txtNode = "./text()";
		AutoPilot ap = new AutoPilot(vn);
		StringBuilder result = new StringBuilder();
		ap.selectXPath(txtNode);
		int txtIndex = -1;
		while ((txtIndex = ap.evalXPath()) != -1) {
			result.append(vn.toRawString(txtIndex));
		}

		if (result.length() == 0) {
			return null;
		} else {
			return result.toString();
		}
	}

	/**
	 * 得到当前节点的完整内容，包含子节点及文本。若无文本内容返回 null。
	 * @throws NavException
	 */
	public String getElementContent() throws NavException {
		String result = null;
		long l = vn.getContentFragment();
		if (l == -1) {
			return null;
		}

		int offset = (int) l;
		int len = (int) (l >> 32);
		if (offset != -1 && len != -1) {
			if (vn.getEncoding() > VTDNav.FORMAT_WIN_1258) {
				offset = offset >> 1;
				len = len >> 1;
			}
			result = vn.toRawString(offset, len);
		}
		return result;
	}

	/**
	 * 获取当前节点下所有指定元素名称的文本内容，含内部标记等子节点内容。
	 * @param elementName
	 *            子节点名称
	 * @throws XPathParseException
	 * @throws NavException
	 * @throws XPathEvalException
	 */
	public Vector<String> getChildrenContent(String elementName) throws XPathParseException, XPathEvalException,
			NavException {
		Vector<String> texts = new Vector<String>();
		AutoPilot ap = new AutoPilot(vn);
		ap.selectXPath("./" + elementName);
		while (ap.evalXPath() != -1) {
			vn.push();
			texts.add(getElementContent());
			vn.pop();
		}

		if (texts.isEmpty()) {
			texts = null;
		}
		return texts;
	}

	/**
	 * 获取当前节点下所有指定元素名称的文本内容，含内部标记等子节点内容。
	 * @param elementName
	 *            子节点名称
	 * @return String 指定子节点内容，无内容返回 null。
	 * @throws XPathParseException
	 * @throws NavException
	 * @throws XPathEvalException
	 */
	public String getChildContent(String elementName) throws XPathParseException, XPathEvalException, NavException {
		String text = null;
		AutoPilot ap = new AutoPilot(vn);
		ap.selectXPath("./" + elementName);
		vn.push();
		if (ap.evalXPath() != -1) {
			text = getElementContent();
		}
		vn.pop();
		return text;
	}

	/**
	 * 返回当前节点下一级子节点的个数。
	 * @return
	 * @throws XPathParseException
	 * @throws XPathEvalException
	 * @throws NavException
	 *             ;
	 */
	public int getChildElementsCount() throws XPathParseException, XPathEvalException, NavException {
		int result = 0;
		AutoPilot ap = new AutoPilot(vn);
		ap.selectXPath("./*");
		vn.push();
		while (ap.evalXPath() != -1) {
			result++;
		}
		vn.pop();
		return result;
	}

	/**
	 * 获得指定节点下一级子节点的个数
	 * @param xpath
	 *            指定节点的 XPath
	 * @return
	 * @throws XPathParseException
	 * @throws XPathEvalException
	 * @throws NavException
	 *             ;
	 */
	public int getChildElementsCount(String xpath) throws XPathParseException, XPathEvalException, NavException {
		int result = 0;
		AutoPilot ap = new AutoPilot(vn);
		ap.selectXPath(xpath);
		vn.push();
		if (ap.evalXPath() != -1) {
			result = getChildElementsCount();
		}
		vn.pop();
		return result;
	}

	/**
	 * 得到指定节点的完整内容，包含子节点及文本。若无文本内容返回 null。
	 * @param xpath
	 *            指定节点的XPath。
	 * @throws XPathParseException
	 * @throws XPathEvalException
	 * @throws NavException
	 * @exception XPathParseException
	 *                ,XPathEvalException,NavException
	 */
	public String getElementContent(String xpath) throws NavException, XPathParseException, XPathEvalException {
		String text = null;
		if (xpath != null && !xpath.equals("")) {
			AutoPilot ap = new AutoPilot(vn);
			ap.selectXPath(xpath);
			vn.push();
			if (ap.evalXPath() != -1) {
				text = getElementContent();
			}
			vn.pop();
		}
		return text;
	}

	/**
	 * 获取绑定的 XML 导航器对象。
	 */
	public VTDNav getVTDNav() {
		return vn;
	}

	/**
	 * 获取当前节点下所有指定元素名称的文本内容，含内部标记等子节点内容。
	 * @param elementName
	 *            子节点名称
	 * @return String 指定子节点内容，无内容返回 null。
	 * @throws XPathParseException
	 * @throws XPathParseException
	 * @throws NavException
	 * @throws NavException
	 * @throws XPathEvalException
	 * @throws XPathEvalException
	 */
	public String getChildPureText(String elementName) throws XPathParseException, NavException, XPathEvalException {
		String text = null;
		AutoPilot ap = new AutoPilot(vn);
		ap.selectXPath("./" + elementName);
		vn.push();
		if (ap.evalXPath() != -1) {
			text = getElementPureText();
		}
		vn.pop();
		return text;
	}

	/**
	 * 获得元素的属性值
	 * @param elementXPath
	 *            指定元素的 XPath
	 * @param attributeName
	 *            指定属性的名称
	 * @return
	 * @throws XPathParseException
	 * @throws XPathEvalException
	 * @throws NavException
	 *             ;
	 */
	public String getElementAttribute(String elementXPath, String attributeName) throws XPathParseException,
			XPathEvalException, NavException {
		String text = null;
		AutoPilot ap = new AutoPilot(vn);
		ap.selectXPath(elementXPath);
		vn.push();
		if (ap.evalXPath() != -1) {
			int inx = vn.getAttrVal(attributeName);
			if (inx != -1) {
				text = vn.toString(inx);
			}
		}
		vn.pop();
		return text;
	}

	/**
	 * 获得整个节点的段落
	 * @return 整个节点的段落，例如：&lt;ph id="1"&gt;a&lt;/ph&gt;
	 * @throws NavException
	 *             ;
	 */
	public String getElementFragment() throws NavException {
		long l = vn.getElementFragment();
		int offset = (int) l;
		int len = (int) (l >> 32);

		/* 区别编码。在解析 UTF-16 等部分编码的时候索引会变为2倍。 */
		if (vn.getEncoding() > VTDGen.FORMAT_WIN_1258) {
			offset = offset >> 1;
			len = len >> 1;
		}
		// 处理这种情况下获取内容错误的情况 <ph>...</ph> 1><ph>...</ph> robert 2012-09-13
		String fragment = "";
		try {
			fragment = vn.toRawString(offset, len);
		} catch (NavException e) {
			if (e.getMessage().contains("encoding error")) {
				byte[] doc = vn.getXML().getBytes();
				if (vn.getEncoding() == VTDNav.FORMAT_UTF_16LE || vn.getEncoding() == VTDNav.FORMAT_UTF_16BE) {
					offset = offset << 1;
				}
				String line = formatLineNumber(doc, offset, vn);
				throw new NavException("encoding error:" + line);
			}
			throw e;

		}
		int length = fragment.length();
		int realEndIdx = fragment.indexOf(">", fragment.lastIndexOf("<"));
		if (realEndIdx != length - 1) {
			fragment = fragment.substring(0, fragment.indexOf(">", fragment.lastIndexOf("<")) + 1);
		}
		return fragment;
	}

	private String formatLineNumber(byte[] XMLDoc, int os, VTDNav vn) {
		int so = 0;
		int lineNumber = 0;
		int lineOffset = 0;

		if (vn.getEncoding() < VTDNav.FORMAT_UTF_16BE) {
			while (so <= os - 1) {
				if (XMLDoc[so] == '\n') {
					lineNumber++;
					lineOffset = so;
				}
				// lineOffset++;
				so++;
			}
			lineOffset = os - lineOffset;
		} else if (vn.getEncoding() == VTDNav.FORMAT_UTF_16BE) {
			while (so <= os - 2) {
				if (XMLDoc[so + 1] == '\n' && XMLDoc[so] == 0) {
					lineNumber++;
					lineOffset = so;
				}
				so += 2;
			}
			lineOffset = (os - lineOffset) >> 1;
		} else {
			while (so <= os - 2) {
				if (XMLDoc[so] == '\n' && XMLDoc[so + 1] == 0) {
					lineNumber++;
					lineOffset = so;
				}
				so += 2;
			}
			lineOffset = (os - lineOffset) >> 1;
		}
		return "\nLine Number: " + (lineNumber + 1) + " Offset: " + (lineOffset - 1);
	}

	private static final Pattern PATTERN = Pattern.compile("<[^>]+>");

	/**
	 * 获得节点的头部
	 * @return 节点头部，例如：&lt;ph id="1"&gt;a&lt;/ph&gt;，则返回&lt;ph id="1"&gt;
	 * @throws NavException
	 *             ;
	 */
	public String getElementHead() throws NavException {
		String sourceFragment = getElementFragment(); // 节点的段落
		Matcher matcher = PATTERN.matcher(sourceFragment);
		if (matcher.find()) {
			String elementHead = matcher.group();
			if (elementHead.endsWith("/>")) {
				return elementHead.substring(0, elementHead.length() - 2) + ">";
			}
			return elementHead;
		}
		return null;
	}

	/**
	 * 得到一个节点的XML格式的文本
	 * @param name
	 *            节点名
	 * @param content
	 *            节点内容，值为null时表示独立节点。
	 * @param props
	 *            节点属性
	 * @return ;
	 */
	public static String getNodeXML(String name, String content, Hashtable<String, String> props) {
		if (name == null || "".equals(name)) {
			return null;
		}
		StringBuffer xml = new StringBuffer("<");
		xml.append(name);
		if (props != null && !props.isEmpty()) {
			for (Entry<String, String> entry : props.entrySet()) {
				String key = entry.getKey();
				String value = entry.getValue();
				if (key == null || "".equals(key) || value == null) {
					continue;
				}
				xml.append(" ").append(entry.getKey()).append("=\"").append(entry.getValue()).append("\"");
			}
		}
		if (content == null) {
			xml.append(" />");
		} else {
			xml.append(">").append(content).append("</").append(name).append(">");
		}
		return xml.toString();
	}

	/**
	 * 获取非节点 XPath 表达式计算的值。<br/>
	 * 非节点 XPath：导航到的位置不是简单的 XML 内容。
	 * <p>
	 * 例如“count”等函数的返回值。 <br/>
	 * <code>Double count = getEvalValue(Double.class, "count(//a)");</code>
	 * </p>
	 * @param <T>
	 *            结果类型
	 * @param clazz
	 *            结果类型
	 * @param xpath
	 *            XPath 表达式
	 * @return XPath 表达式计算的值;
	 */
	public <T> T getEvalValue(Class<T> clazz, String xpath) {
		return getEvalValue(clazz, null, xpath);
	}

	/**
	 * 获取非节点 XPath 表达式计算的值。<br/>
	 * 非节点 XPath：导航到的位置不是简单的 XML 内容。
	 * <p>
	 * 例如“count”等函数的返回值。 <br/>
	 * <code>Double count = getEvalValue(Double.class, "count(//a)");</code>
	 * </p>
	 * @param <T>
	 *            结果类型
	 * @param clazz
	 *            结果类型
	 * @param ap
	 *            AutoPilot 实例
	 * @param xpath
	 *            XPath 表达式
	 * @return XPath 表达式计算的值;
	 */
	@SuppressWarnings("unchecked")
	public <T> T getEvalValue(Class<T> clazz, AutoPilot ap, String xpath) {
		if (ap == null) {
			ap = new AutoPilot(vn);
		}
		try {
			vn.push();
			ap.selectXPath(xpath);
			if (String.class.equals(clazz)) {
				String value;
				if ((value = ap.evalXPathToString()) != null) {
					return (T) value;
				}
			} else if (Double.class.equals(clazz)) {
				Double value;
				if ((value = ap.evalXPathToNumber()) != -1) {
					return (T) value;
				}
			} else if (Boolean.class.equals(clazz)) {
				Boolean value;
				if ((value = ap.evalXPathToBoolean()) != false) {
					return (T) value;
				}
			}
		} catch (XPathParseException e) {
			LOGGER.error("", e);
		} finally {
			vn.pop();
		}
		return null;
	}

	/**
	 * <p>
	 * 根据 XPath 取值（XPath 支持相对定位）。<br/>
	 * 使用此方法时，可以结合 {@link #pilot(String)} 或 {@link #pilot(AutoPilot, String)} 方法采用相对定位，可以大大提高导航效率
	 * </p>
	 * <b>注意</b>在XPath基础上，添加关键字“[name]”、“puretext()”的支持： <li>[name]：用于取得指定属性名称，例如，/a/@*[name] 取得 a 节点下所有属性的名字</li> <li>
	 * puretext()：用于取得指定节点的纯文本内容（排除子节点），例如，/a/puretext() 取得 a 节点的纯文本内容</li>
	 * @param xpath
	 *            XPath 表达式
	 * @return XPath 得到的值;
	 */
	public String getValue(String xpath) {
		return getValue(null, xpath, null);
	}

	/**
	 * <p>
	 * 根据 XPath 取值（XPath 支持相对定位）。<br/>
	 * 使用此方法时，可以结合 {@link #pilot(String)} 或 {@link #pilot(AutoPilot, String)} 方法采用相对定位，可以大大提高导航效率
	 * </p>
	 * <b>注意</b>在XPath基础上，添加关键字“[name]”、“puretext()”的支持： <li>[name]：用于取得指定属性名称，例如，/a/@*[name] 取得 a 节点下所有属性的名字</li> <li>
	 * puretext()：用于取得指定节点的纯文本内容（排除子节点），例如，/a/puretext() 取得 a 节点的纯文本内容</li>
	 * @param xpath
	 *            XPath 表达式
	 * @param defaultValue
	 *            默认值
	 * @return XPath 得到的值;
	 */
	public String getValue(String xpath, String defaultValue) {
		return getValue(null, xpath, defaultValue);
	}

	/**
	 * <p>
	 * 根据 XPath 取值（XPath 支持相对定位）。<br/>
	 * 使用此方法时，可以结合 {@link #pilot(String)} 或 {@link #pilot(AutoPilot, String)} 方法采用相对定位，可以大大提高导航效率
	 * </p>
	 * <b>注意</b>在XPath基础上，添加关键字“[name]”、“puretext()”的支持： <li>[name]：用于取得指定属性名称，例如，/a/@*[name] 取得 a 节点下所有属性的名字</li> <li>
	 * puretext()：用于取得指定节点的纯文本内容（排除子节点），例如，/a/puretext() 取得 a 节点的纯文本内容</li>
	 * @param ap
	 *            AutoPilot 实例
	 * @param xpath
	 *            XPath 表达式
	 * @return XPath 得到的值;
	 */
	public String getValue(AutoPilot ap, String xpath) {
		return getValue(ap, xpath, null);
	}

	/**
	 * <p>
	 * 根据 XPath 取值（XPath 支持相对定位）。<br/>
	 * 使用此方法时，可以结合 {@link #pilot(String)} 或 {@link #pilot(AutoPilot, String)} 方法采用相对定位，可以大大提高导航效率
	 * </p>
	 * <b>注意</b>在XPath基础上，添加关键字“[name]”、“puretext()”的支持： <li>[name]：用于取得指定属性名称，例如，/a/@*[name] 取得 a 节点下所有属性的名字</li> <li>
	 * puretext()：用于取得指定节点的纯文本内容（排除子节点），例如，/a/puretext() 取得 a 节点的纯文本内容</li>
	 * @param ap
	 *            AutoPilot 实例
	 * @param xpath
	 *            XPath 表达式
	 * @param defaultValue
	 *            默认值
	 * @return XPath 得到的值;
	 */
	public String getValue(AutoPilot ap, String xpath, String defaultValue) {
		if (ap == null) {
			ap = new AutoPilot(vn);
		}
		String value = null;
		try {
			vn.push();
			if (xpath.endsWith("/puretext()")) {
				xpath = xpath.substring(0, xpath.length() - "/puretext()".length());
				ap.selectXPath(xpath);
				if (ap.evalXPath() != -1) {
					value = getElementPureText();
				}
			} else if (xpath.endsWith("/text()")) {
				xpath = xpath.substring(0, xpath.length() - "/text()".length());
				ap.selectXPath(xpath);
				if (ap.evalXPath() != -1) {
					value = getElementContent();
				}
			} else {
				boolean isAttrName = false; // 是否是取属性名字
				if (xpath.endsWith("[name]")) {
					xpath = xpath.substring(0, xpath.length() - "[name]".length());
					isAttrName = true;
				}

				ap.selectXPath(xpath);
				if (ap.evalXPath() != -1) {
					int type = vn.getTokenType(vn.getCurrentIndex());
					if (type == VTDNav.TOKEN_STARTING_TAG) {
						long l = vn.getElementFragment();
						value = vn.toString((int) l, (int) (l >> 32));
					} else if (type == VTDNav.TOKEN_ATTR_NAME || type == VTDNav.TOKEN_ATTR_NS) {
						if (isAttrName) {
							value = vn.toString(vn.getCurrentIndex());
						} else {
							value = vn.toString(vn.getCurrentIndex() + 1);
						}
					} else {
						value = vn.toString(vn.getCurrentIndex());
					}
				}
			}
		} catch (XPathParseException e) {
			LOGGER.error("", e);
		} catch (XPathEvalException e) {
			LOGGER.error("", e);
		} catch (NavException e) {
			LOGGER.error("", e);
		} finally {
			vn.pop();
		}
		return value == null ? defaultValue : value;
	}

	/**
	 * 根据 XPath 取一个集合的值。<br/>
	 * <b>注意</b>在XPath基础上，添加关键字“[name]”、“puretext()”的支持： <li>[name]：用于取得指定属性名称，例如，/a/@*[name] 取得 a 节点下所有属性的名字</li> <li>
	 * puretext()：用于取得指定节点的纯文本内容（排除子节点），例如，/a/puretext() 取得 a 节点的纯文本内容</li>
	 * @param ap
	 *            AutoPilot 对象
	 * @param xpath
	 *            XPath 表达式
	 * @param isAllowRepeat
	 *            是否允许取值重复。
	 * @return XPath 得到的值,无匹配的值则为 null;
	 */
	public List<String> getValues(AutoPilot ap, String xpath, boolean isAllowRepeat) {
		if (ap == null) {
			ap = new AutoPilot(vn);
		}

		List<String> values = new Vector<String>();
		try {
			vn.push();
			if (xpath.endsWith("/puretext()")) {
				xpath = xpath.substring(0, xpath.length() - "/puretext()".length());
				ap.selectXPath(xpath);
				if (ap.evalXPath() != -1) {
					String strTmpValue = getElementPureText();
					if (isAllowRepeat) {
						values.add(strTmpValue);
					} else {
						if (!values.contains(strTmpValue)) {
							values.add(strTmpValue);
						}
					}
				}
			} else if (xpath.endsWith("/text()")) {
				xpath = xpath.substring(0, xpath.length() - "/text()".length());
				ap.selectXPath(xpath);
				while (ap.evalXPath() != -1) {
					String strTmpValue = getElementContent();
					if (isAllowRepeat) {
						values.add(strTmpValue);
					} else {
						if (!values.contains(strTmpValue)) {
							values.add(strTmpValue);
						}
					}
				}
			} else {
				boolean isAttrName = false;
				if (xpath.endsWith("[name]")) {
					xpath = xpath.substring(0, xpath.length() - "[name]".length());
					isAttrName = true;
				}

				ap.selectXPath(xpath);
				while (ap.evalXPath() != -1) {
					int type = vn.getTokenType(vn.getCurrentIndex());
					if (type == VTDNav.TOKEN_STARTING_TAG) {
						long l = vn.getElementFragment();
						String strTmpValue = vn.toString((int) l, (int) (l >> 32));
						if (isAllowRepeat) {
							values.add(strTmpValue);
						} else {
							if (!values.contains(strTmpValue)) {
								values.add(strTmpValue);
							}
						}
					} else if (type == VTDNav.TOKEN_ATTR_NAME || type == VTDNav.TOKEN_ATTR_NS) {
						String strTmpValue;
						if (isAttrName) {
							strTmpValue = vn.toString(vn.getCurrentIndex());
						} else {
							strTmpValue = vn.toString(vn.getCurrentIndex() + 1);
						}
						if (isAllowRepeat) {
							values.add(strTmpValue);
						} else {
							if (!values.contains(strTmpValue)) {
								values.add(strTmpValue);
							}
						}
					} else {
						String strTmpValue = vn.toString(vn.getCurrentIndex());
						if (isAllowRepeat) {
							values.add(strTmpValue);
						} else {
							if (!values.contains(strTmpValue)) {
								values.add(strTmpValue);
							}
						}
					}
				}
			}
		} catch (XPathParseException e) {
			LOGGER.error("", e);
		} catch (XPathEvalException e) {
			LOGGER.error("", e);
		} catch (NavException e) {
			LOGGER.error("", e);
		} finally {
			vn.pop();
		}

		if (values.size() == 0) {
			values = null;
		}
		return values;
	}

	/**
	 * 根据 XPath 取一个不包含重复值的集合。<br/>
	 * <b>注意</b>在XPath基础上，添加关键字“[name]”、“puretext()”的支持： <li>[name]：用于取得指定属性名称，例如，/a/@*[name] 取得 a 节点下所有属性的名字</li> <li>
	 * puretext()：用于取得指定节点的纯文本内容（排除子节点），例如，/a/puretext() 取得 a 节点的纯文本内容</li>
	 * @param ap
	 *            AutoPilot 对象
	 * @param xpath
	 *            XPath 表达式
	 * @return XPath 得到的值,无匹配的值则为 null;
	 */
	public List<String> getValues(AutoPilot ap, String xpath) {
		return getValues(ap, xpath, false);
	}

	/**
	 * 根据 XPath 取一个不包含重复值的集合。<br/>
	 * <b>注意</b>在XPath基础上，添加关键字“[name]”、“puretext()”的支持： <li>[name]：用于取得指定属性名称，例如，/a/@*[name] 取得 a 节点下所有属性的名字</li> <li>
	 * puretext()：用于取得指定节点的纯文本内容（排除子节点），例如，/a/puretext() 取得 a 节点的纯文本内容</li>
	 * @param xpath
	 *            XPath 表达式
	 * @return XPath 得到的值,无匹配的值则为 null;
	 */
	public List<String> getValues(String xpath) {
		return getValues(null, xpath, false);
	}

	/**
	 * 删除 XPath 表达式所定位到的内容
	 * @param xpath
	 *            xpath表达式
	 * @return XMLModifier 实例;
	 */
	public XMLModifier delete(String xpath) {
		return delete(null, null, xpath);
	}

	/**
	 * 删除 XPath 表达式所定位到的内容
	 * @param xpath
	 *            xpath表达式
	 * @param condition
	 *            本次操作的限定条件
	 * @return XMLModifier 实例;
	 */
	public XMLModifier delete(String xpath, int condition) {
		return delete(null, null, xpath, condition);
	}

	/**
	 * 删除 XPath 表达式所定位到的内容
	 * @param ap
	 *            AutoPilot 实例
	 * @param xm
	 *            XMLModifier 实例
	 * @param xpath
	 *            xpath表达式
	 * @return XMLModifier 实例;
	 */
	public XMLModifier delete(AutoPilot ap, XMLModifier xm, String xpath) {
		return update(ap, xm, xpath, null, 0); // 默认只导航一次
	}

	/**
	 * 删除 XPath 表达式所定位到的内容
	 * @param ap
	 *            AutoPilot 实例
	 * @param xm
	 *            XMLModifier 实例
	 * @param xpath
	 *            xpath表达式
	 * @param condition
	 *            本次操作的限定条件
	 * @return XMLModifier 实例;
	 */
	public XMLModifier delete(AutoPilot ap, XMLModifier xm, String xpath, int condition) {
		return update(ap, xm, xpath, null, condition);
	}

	/**
	 * 修改 XPath 表达式所定位到的内容
	 * @param xpath
	 *            xpath表达式
	 * @param newValue
	 *            要修改的新值
	 * @return XMLModifier 实例;
	 */
	public XMLModifier update(String xpath, String newValue) {
		return update(null, null, xpath, newValue);
	}

	/**
	 * 修改 XPath 表达式所定位到的内容
	 * @param xpath
	 *            xpath表达式
	 * @param newValue
	 *            要修改的新值
	 * @param condition
	 *            本次操作的限定条件
	 * @return XMLModifier 实例;
	 */
	public XMLModifier update(String xpath, String newValue, int condition) {
		return update(null, null, xpath, newValue, condition);
	}

	/**
	 * 修改 XPath 表达式所定位到的内容
	 * @param ap
	 *            AutoPilot 实例
	 * @param xm
	 *            XMLModifier 实例
	 * @param xpath
	 *            xpath表达式
	 * @param newValue
	 *            要修改的新值
	 * @return XMLModifier 实例;
	 */
	public XMLModifier update(AutoPilot ap, XMLModifier xm, String xpath, String newValue) {
		return update(ap, xm, xpath, newValue, 0); // 默认只导航一次
	}

	/**
	 * 修改 XPath 表达式所定位到的内容
	 * @param ap
	 *            AutoPilot 实例
	 * @param xm
	 *            XMLModifier 实例
	 * @param xpath
	 *            xpath表达式
	 * @param newValue
	 *            要修改的新值
	 * @param condition
	 *            本次操作的限定条件
	 * @return XMLModifier 实例;
	 */
	public XMLModifier update(AutoPilot ap, XMLModifier xm, String xpath, String newValue, int condition) {
		return handleXML(ap, xm, xpath, newValue, condition, true);
	}

	/**
	 * 处理 XPath 表达式所定位到的 XML 内容
	 * @param ap
	 *            AutoPilot 实例
	 * @param xm
	 *            XMLModifier 实例
	 * @param xpath
	 *            xpath表达式
	 * @param newValue
	 *            要修改的新值
	 * @param condition
	 *            本次操作的限定条件
	 * @return XMLModifier 实例;
	 */
	private XMLModifier handleXML(AutoPilot ap, XMLModifier xm, String xpath, String newValue, int condition,
			boolean remove) {
		try {
			vn.push();
			if (ap == null) {
				ap = new AutoPilot(vn);
			}
			if (xm == null) {
				xm = new XMLModifier(vn);
			}
			boolean pilotToEnd = (condition & PILOT_TO_END) != 0;
			boolean isContent = false;
			if (xpath.endsWith("/text()")) { // 操作的是内容节点。
				xpath = xpath.substring(0, xpath.length() - "/text()".length());
				isContent = true;
			}
			ap.selectXPath(xpath);
			boolean exist = false;
			while (ap.evalXPath() != -1) {
				exist = true;
				long contentFragment = vn.getContentFragment();

				int currentIndex = vn.getCurrentIndex();
				int type = vn.getTokenType(currentIndex);
				if (remove || newValue == null) { // newValue 为 null，执行移除操作
					if (isContent) {
						if (contentFragment != -1) { // 执行删除
							xm.remove(contentFragment); // 删除内容
						}
					} else {
						// 属性节点不执行删除，除非调用 delete 方法（判断是否调用 delete 方法的依据是，newValue 是否为 null）
						if ((type != VTDNav.TOKEN_ATTR_NAME && type != VTDNav.TOKEN_ATTR_NS) || newValue == null) {
							xm.remove(); // 删除节点
						}
					}
				}

				if (newValue != null) { // 执行修改
					if (isContent) {
						xm.insertBeforeTail(newValue.getBytes(getCharsetByEncoding()));
					} else {
						if (type == VTDNav.TOKEN_STARTING_TAG) {
							xm.insertAfterElement(newValue);
						} else if (type == VTDNav.TOKEN_ATTR_NAME || type == VTDNav.TOKEN_ATTR_NS) {
							xm.updateToken(currentIndex + 1, newValue);
						} else {
							xm.updateToken(currentIndex, newValue.getBytes());
						}
					}
				}

				if (!pilotToEnd) { // 不需要导航到 XML 末尾，停止循环
					break;
				}
			}
			boolean createIfNotExist = (condition & CREATE_IF_NOT_EXIST) != 0;
			if (!exist && createIfNotExist) { // 如果不存在并且需要创建
				int lastSeperator = xpath.lastIndexOf("/");
				String nodeName = xpath.substring(lastSeperator + 1);
				xpath = xpath.substring(0, lastSeperator); // 截掉最后一部分
				if (nodeName.startsWith("@")) {
					nodeName = nodeName.substring(1);
					ap.selectXPath(xpath);
					while (ap.evalXPath() != -1) {
						insertAttribute(xm, nodeName, newValue); // 插入属性
						if (!pilotToEnd) { // 不需要导航到 XML 末尾，停止循环
							break;
						}
					}
				} else {
					if (isContent) { // 如果改动的是节点内容
						newValue = getNodeXML(nodeName, newValue, null);
						ap.selectXPath(xpath);
						while (ap.evalXPath() != -1) {
							xm.insertAfterHead(newValue);
							if (!pilotToEnd) { // 不需要导航到 XML 末尾，停止循环
								break;
							}
						}
					}
				}
			}
		} catch (XPathParseException e) {
			LOGGER.error("", e);
		} catch (XPathEvalException e) {
			LOGGER.error("", e);
		} catch (NavException e) {
			LOGGER.error("", e);
		} catch (ModifyException e) {
			LOGGER.error("", e);
		} catch (UnsupportedEncodingException e) {
			LOGGER.error("", e);
		} finally {
			vn.pop();
		}
		return xm;
	}

	/**
	 * 插入新属性（到最后的位置）。
	 * @param xm
	 * @param attrName
	 * @param attrValue
	 * @throws ModifyException
	 *             ;
	 */
	private void insertAttribute(XMLModifier xm, String attrName, String attrValue) throws ModifyException {
		int startTagIndex = vn.getCurrentIndex();
		int type = vn.getTokenType(startTagIndex);
		if (type != VTDNav.TOKEN_STARTING_TAG)
			throw new ModifyException("Token type is not a starting tag");

		String attrFragment = new StringBuffer(" ").append(attrName).append("=\"").append(attrValue).append("\"")
				.toString(); // 构建属性片段，“ attrName="attrValue" ”
		long i = vn.getOffsetAfterHead(); // 得到开始标记的结束位置
		if (vn.getEncoding() < VTDNav.FORMAT_UTF_16BE) {
			xm.insertBytesAt((int) i - 1, attrFragment.getBytes());
		} else {
			xm.insertBytesAt(((int) i - 1) << 1, attrFragment.getBytes());
		}
	}

	/**
	 * 将新值插入到 XPath 表达式所定位到的位置
	 * @param xpath
	 *            XPath 表达式
	 * @param newValue
	 *            要修改的新值
	 * @return XMLModifier 实例;
	 */
	public XMLModifier insert(String xpath, String newValue) {
		return insert(null, null, xpath, newValue);
	}

	/**
	 * 将新值插入到 XPath 表达式所定位到的位置
	 * @param ap
	 *            AutoPilot 实例
	 * @param xm
	 *            XMLModifier 实例
	 * @param xpath
	 *            XPath 表达式
	 * @param newValue
	 *            要修改的新值
	 * @return XMLModifier 实例;
	 */
	public XMLModifier insert(AutoPilot ap, XMLModifier xm, String xpath, String newValue) {
		return insert(ap, xm, xpath, newValue, 0);
	}

	/**
	 * 将新值插入到 XPath 表达式所定位到的位置
	 * @param xpath
	 *            XPath 表达式
	 * @param newValue
	 *            要修改的新值
	 * @param condition
	 *            本次操作的限定条件
	 * @return XMLModifier 实例;
	 */
	public XMLModifier insert(String xpath, String newValue, int condition) {
		return insert(null, null, xpath, newValue, condition);
	}

	/**
	 * 将新值插入到 XPath 表达式所定位到的位置
	 * @param ap
	 *            AutoPilot 实例
	 * @param xm
	 *            XMLModifier 实例
	 * @param xpath
	 *            XPath 表达式
	 * @param newValue
	 *            要修改的新值
	 * @param condition
	 *            本次操作的限定条件
	 * @return XMLModifier 实例;
	 */
	public XMLModifier insert(AutoPilot ap, XMLModifier xm, String xpath, String newValue, int condition) {
		return handleXML(ap, xm, xpath, newValue, condition | CREATE_IF_NOT_EXIST, false);
	}

	/**
	 * 导航
	 * @param xpath
	 *            XPath 表达式
	 * @return 导航到的位置索引（-1：导航失败）;
	 */
	public int pilot(String xpath) {
		return pilot(null, xpath);
	}

	/**
	 * 导航
	 * @param ap
	 *            AutoPilot 对象
	 * @param xpath
	 *            XPath 表达式
	 * @return 导航到的位置索引（-1：导航失败）;
	 */
	public int pilot(AutoPilot ap, String xpath) {
		if (ap == null) {
			ap = new AutoPilot(vn);
		}
		try {
			ap.selectXPath(xpath);
			return ap.evalXPath();
		} catch (XPathParseException e) {
			LOGGER.error("", e);
		} catch (XPathEvalException e) {
			LOGGER.error("", e);
		} catch (NavException e) {
			LOGGER.error("", e);
		}
		return -1;
	}

	/**
	 * 在 XPath 中转义引号（解决在 XPath 中某个字符串同时含有单引号和双引号的情况）
	 * @param xpath
	 * @return ;
	 */
	public static String dealEscapeQuotes(String xpath) {
		StringBuffer sb = new StringBuffer();
		if (xpath.indexOf('\'') != -1) {
			StringTokenizer st = new StringTokenizer(xpath, "'\"", true);
			sb.append("concat(");
			while (st.hasMoreTokens()) {
				String token = st.nextToken();
				if (token.equalsIgnoreCase("'")) {
					sb.append("\"");
					sb.append(token);
					sb.append("\"");
				} else {
					sb.append("'");
					sb.append(token);
					sb.append("'");
				}
				if (st.countTokens() != 0) {
					sb.append(",");
				}
			}
			sb.append(")");
		} else {
			sb.append("'");
			sb.append(xpath);
			sb.append("'");
		}
		return sb.toString();
	}

	/**
	 * 保存修改并更新 VTDNav 实例
	 * @param xm
	 *            XMLModifier 实例
	 * @param filePath
	 *            文件路径
	 * @return VTDNav 实例
	 */
	public VTDNav updateVTDNav(XMLModifier xm, String filePath) {
		try {
			File file = new File(filePath);
			FileOutputStream fos = new FileOutputStream(file);
			BufferedOutputStream bos = new BufferedOutputStream(fos);
			xm.output(bos);
			bos.close();
			fos.close();

			VTDGen vg = new VTDGen();
			if (vg.parseFile(filePath, true)) { // 重新加载
				vn = vg.getNav();
			}
		} catch (ModifyException e) {
			LOGGER.error("", e);
		} catch (TranscodeException e) {
			LOGGER.error("", e);
		} catch (IOException e) {
			LOGGER.error("", e);
		}
		return vn;
	}

	/**
	 * 根据 vn 的 encoding 获取字符集编码(此方法与 XMLModifier 的 bind 方法中获取字符集的方式一致)
	 * @return
	 * @throws ModifyException
	 *             ;
	 */
	public String getCharsetByEncoding() throws ModifyException {
		int encoding = vn.getEncoding();
		switch (encoding) {
		case VTDNav.FORMAT_ASCII:
			return "ASCII";
		case VTDNav.FORMAT_ISO_8859_1:
			return "ISO8859_1";
		case VTDNav.FORMAT_UTF8:
			return "UTF8";
		case VTDNav.FORMAT_UTF_16BE:
			return "UnicodeBigUnmarked";
		case VTDNav.FORMAT_UTF_16LE:
			return "UnicodeLittleUnmarked";
		case VTDNav.FORMAT_ISO_8859_2:
			return "ISO8859_2";
		case VTDNav.FORMAT_ISO_8859_3:
			return "ISO8859_3";
		case VTDNav.FORMAT_ISO_8859_4:
			return "ISO8859_4";
		case VTDNav.FORMAT_ISO_8859_5:
			return "ISO8859_5";
		case VTDNav.FORMAT_ISO_8859_6:
			return "ISO8859_6";
		case VTDNav.FORMAT_ISO_8859_7:
			return "ISO8859_7";
		case VTDNav.FORMAT_ISO_8859_8:
			return "ISO8859_8";
		case VTDNav.FORMAT_ISO_8859_9:
			return "ISO8859_9";
		case VTDNav.FORMAT_ISO_8859_10:
			return "ISO8859_10";
		case VTDNav.FORMAT_ISO_8859_11:
			return "x-iso-8859-11";
		case VTDNav.FORMAT_ISO_8859_12:
			return "ISO8859_12";
		case VTDNav.FORMAT_ISO_8859_13:
			return "ISO8859_13";
		case VTDNav.FORMAT_ISO_8859_14:
			return "ISO8859_14";
		case VTDNav.FORMAT_ISO_8859_15:
			return "ISO8859_15";

		case VTDNav.FORMAT_WIN_1250:
			return "Cp1250";
		case VTDNav.FORMAT_WIN_1251:
			return "Cp1251";
		case VTDNav.FORMAT_WIN_1252:
			return "Cp1252";
		case VTDNav.FORMAT_WIN_1253:
			return "Cp1253";
		case VTDNav.FORMAT_WIN_1254:
			return "Cp1254";
		case VTDNav.FORMAT_WIN_1255:
			return "Cp1255";
		case VTDNav.FORMAT_WIN_1256:
			return "Cp1256";
		case VTDNav.FORMAT_WIN_1257:
			return "Cp1257";
		case VTDNav.FORMAT_WIN_1258:
			return "Cp1258";
		default:
			throw new ModifyException(Messages.getString("vtdimpl.VTDUtils.logger2"));
		}
	}
}

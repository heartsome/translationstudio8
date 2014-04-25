package net.heartsome.cat.document;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.heartsome.cat.database.resource.Messages;
import net.heartsome.xml.vtdimpl.VTDUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ximpleware.AutoPilot;
import com.ximpleware.EOFException;
import com.ximpleware.EncodingException;
import com.ximpleware.EntityException;
import com.ximpleware.NavException;
import com.ximpleware.ParseException;
import com.ximpleware.VTDGen;
import com.ximpleware.VTDNav;
import com.ximpleware.XPathEvalException;
import com.ximpleware.XPathParseException;

/**
 * 文档工具类
 * @author terry
 * @version
 * @since JDK1.6
 */
public class DocUtils {

	private static final Logger LOGGER = LoggerFactory.getLogger(DocUtils.class);

	/**
	 * 构造函数
	 */
	protected DocUtils() {
		throw new UnsupportedOperationException();
	}

	/**
	 * 判断是否是正确的 TBX 文件
	 * @param fileName
	 *            TBX 文件的全路径
	 * @return 反回null，验证失败
	 * @throws ParseException
	 * @throws EntityException
	 * @throws EOFException
	 * @throws EncodingException
	 * @throws FileNotFoundException
	 */
	public static VTDUtils isTBX(String fileName) throws EncodingException, ParseException, FileNotFoundException {
		VTDGen vg = new VTDGen();
		FileInputStream fis = null;
		File f = null;
		try {
			f = new File(fileName);
			fis = new FileInputStream(f);
			byte[] b = new byte[(int) f.length()];

			int offset = 0;
			int numRead = 0;
			int numOfBytes = 1048576;// I choose this value randomally,
			// any other (not too big) value also can be here.
			if (b.length - offset < numOfBytes) {
				numOfBytes = b.length - offset;
			}
			while (offset < b.length && (numRead = fis.read(b, offset, numOfBytes)) >= 0) {
				offset += numRead;
				if (b.length - offset < numOfBytes) {
					numOfBytes = b.length - offset;
				}
			}
			vg.setDoc(b);
			vg.parse(true);

		} catch (IOException e) {
			LOGGER.error(Messages.getString("document.DocUtils.logger1"), e);
		} finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (Exception e) {
				}
			}
		}
		VTDNav vn = vg.getNav();
		AutoPilot ap = new AutoPilot(vn);
		String rootPath = "/martif";
		VTDUtils vtdUtils = new VTDUtils();
		try {
			vtdUtils.bind(vn);
			ap.selectXPath(rootPath);
			if (ap.evalXPath() == -1) {
				// Map<String, String> map = vtdUtils.getCurrentElementAttributs();
				// if (!"TBX".equalsIgnoreCase(map.get("type"))) {
				// return null;
				// }
				// } else {
				return null;
			}
		} catch (NavException e) {
			LOGGER.error(Messages.getString("document.DocUtils.logger2"), e);
			return null;
		} catch (XPathEvalException e) {
			LOGGER.error(Messages.getString("document.DocUtils.logger2"), e);
			return null;
		} catch (XPathParseException e) {
			LOGGER.error(Messages.getString("document.DocUtils.logger2"), e);
			return null;
		} finally {
			vg.clear();
		}
		return vtdUtils;
	}

	/**
	 * 判断是否是正确的 TMX 文件
	 * @param fileName
	 * @return ;
	 * @throws FileNotFoundException
	 * @throws ParseException
	 * @throws EntityException
	 * @throws EOFException
	 * @throws EncodingException
	 */
	public static VTDUtils isTMX(String fileName) throws FileNotFoundException, EncodingException, ParseException {
		VTDGen vg = new VTDGen();
		FileInputStream fis = null;
		File f = null;
		try {
			f = new File(fileName);
			fis = new FileInputStream(f);
			byte[] b = new byte[(int) f.length()];

			int offset = 0;
			int numRead = 0;
			int numOfBytes = 1048576;// I choose this value randomally,
			// any other (not too big) value also can be here.
			if (b.length - offset < numOfBytes) {
				numOfBytes = b.length - offset;
			}
			while (offset < b.length && (numRead = fis.read(b, offset, numOfBytes)) >= 0) {
				offset += numRead;
				if (b.length - offset < numOfBytes) {
					numOfBytes = b.length - offset;
				}
			}
			vg.setDoc(b);
			vg.parse(true);

		} catch (IOException e) {
			LOGGER.error(Messages.getString("document.DocUtils.logger1"), e);
		} finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (Exception e) {
				}
			}
		}
		VTDNav vn = vg.getNav();
		AutoPilot ap = new AutoPilot(vn);
		String rootPath = "/tmx";
		VTDUtils vu = new VTDUtils();
		try {
			vu.bind(vn);
			ap.selectXPath(rootPath);
			if (ap.evalXPath() == -1) {
				return null;
			}
		} catch (NavException e) {
			LOGGER.error(Messages.getString("document.DocUtils.logger2"), e);
			return null;
		} catch (XPathEvalException e) {
			LOGGER.error(Messages.getString("document.DocUtils.logger2"), e);
			return null;
		} catch (XPathParseException e) {
			LOGGER.error(Messages.getString("document.DocUtils.logger2"), e);
			return null;
		} finally {
			vg.clear();
		}

		return vu;
	}

	/**
	 * 取得在 TMX 规范中，哪些属性是日期类型的
	 * @return ;
	 */
	public static List<String> getTMDateProp() {
		List<String> result = new ArrayList<String>();
		result.add("lastusagedate");
		result.add("creationdate");
		result.add("changedate");
		return result;
	}

	public static String getTmxTbxPureText(VTDUtils vu) throws NavException, XPathParseException, XPathEvalException {
		StringBuilder sb = new StringBuilder();
		VTDNav vn = vu.getVTDNav();
		try {
			vn.push();
			sb.append(vu.getElementContent());
			AutoPilot ap = new AutoPilot(vn);
			// 有子节点，即有内部标记
			if (vu.getChildElementsCount() < 1) {
				return sb.toString();
			}
			ap.resetXPath();
			ap.selectXPath("./*");
			while (ap.evalXPath() != -1) {
				String childNodeName = vu.getCurrentElementName();
				if ("g".equals(childNodeName) || "sub".equals(childNodeName) || "hi".equals(childNodeName)
						|| "mrk".equals(childNodeName) || "foreign".equals(childNodeName)) {
					if (vu.getChildElementsCount() < 1) {
						String childFrag = vu.getElementFragment();
						String childContent = vu.getElementContent();
						childContent = childContent == null ? "" : childContent;
						int start = sb.indexOf(childFrag);
						sb.replace(start, start + childFrag.length(), childContent);
					} else {
						String childFrag = vu.getElementFragment();
						String childContent = getTmxTbxPureText(vu);
						childContent = childContent == null ? "" : childContent;
						int start = sb.indexOf(childFrag);
						sb.replace(start, start + childFrag.length(), childContent);
					}
				} else {
					// ph节点的值为code data或者一个sub节点，因此，要考虑到sub节点的情况
					if (vu.getChildElementsCount() < 1) {
						String childFrag = vu.getElementFragment();
						int start = sb.indexOf(childFrag);
						sb.replace(start, start + childFrag.length(), "");
					} else {
						String childFrag = vu.getElementFragment();
						String childContent = "";
						AutoPilot _ap = new AutoPilot(vn);
						_ap.selectXPath("./*");
						while (_ap.evalXPath() != -1) {
							if (vu.getChildElementsCount() <= 0) {
								childContent += vu.getElementContent();
							} else {
								childContent += getTmxTbxPureText(vu);
							}
						}
						childContent = childContent == null ? "" : childContent;
						int start = sb.indexOf(childFrag);
						sb.replace(start, start + childFrag.length(), childContent);
					}
				}
			}
		} finally {
			vn.pop();
		}
		return sb.toString();
	}
}

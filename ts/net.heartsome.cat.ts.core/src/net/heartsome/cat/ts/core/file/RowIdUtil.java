package net.heartsome.cat.ts.core.file;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.heartsome.cat.ts.core.resource.Messages;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ximpleware.NavException;
import com.ximpleware.VTDNav;
import com.ximpleware.XPathEvalException;

/**
 * RowId的辅助类（RowId：编辑器中唯一标识该行文本段的Id）
 * @author weachy
 * @version 1.0
 * @since JDK1.5
 */
public class RowIdUtil {

	// 日志管理器
	private final static Logger LOGGER = LoggerFactory.getLogger(RowIdUtil.class);

	public static final String SPLIT_MARK = "\uFFF1";

	/**
	 * 通过翻译单元的索引得到编辑器中一行文本段数据的唯一标识
	 * @param vn
	 *            VTDNav对象
	 * @param handler
	 *            XLFHandler对象
	 * @param tuIndex
	 *            翻译单元的索引
	 * @param fileName
	 *            xliff文件名
	 * @return 编辑器中唯一标识该行文本段的Id;
	 */
	// public static String getRowIdByTUIndex(VTDNav vn, XLFHandler handler, int tuIndex, String fileName) {
	// try {
	// vn.push();
	// String tuNode = handler.getTUXPathByTUIndex(tuIndex);
	// AutoPilot ap = new AutoPilot(vn);
	// ap.selectXPath(tuNode);
	// if (ap.evalXPath() != -1) {
	// int idIdx = vn.getAttrVal("id");
	// String tuid = idIdx != -1 ? vn.toString(idIdx) : "";
	//
	// ap.resetXPath();
	// String fileNode = tuNode.split("/body")[0];
	// ap.selectXPath(fileNode);
	// if (ap.evalXPath() != -1) {
	// int originalIdx = vn.getAttrVal("original");
	// String original = originalIdx != -1 ? vn.toString(originalIdx) : "";
	// return fileName + SPLIT_MARK + original + SPLIT_MARK + tuid;
	// }
	// }
	// } catch (NavException e) {
	// e.printStackTrace();
	// } catch (XPathParseException e) {
	// e.printStackTrace();
	// } catch (XPathEvalException e) {
	// e.printStackTrace();
	// } finally {
	// vn.pop();
	// }
	// return null;
	// }

	/**
	 * 得到编辑器中一行文本段数据的唯一标识
	 * @param vn
	 *            VTDNav对象
	 * @param handler
	 *            XLFHandler对象
	 * @param fileName
	 *            xliff文件名
	 * @return 编辑器中唯一标识该行文本段的Id;
	 * @throws XPathEvalException
	 */
	public static String getRowId(VTDNav vn, String fileName) throws XPathEvalException {
		try {
			if (!"trans-unit".equals(vn.toString(vn.getCurrentIndex()))) {
				LOGGER.debug(Messages.getString("file.RowIdUtil.logger1"));
				throw new XPathEvalException(Messages.getString("file.RowIdUtil.msg1"));
			}
			vn.push();
			int idIdx = vn.getAttrVal("id");
			String tuid = idIdx != -1 ? vn.toString(idIdx) : "";

			while (!"file".equals(vn.toString(vn.getCurrentIndex()))) { // 找到file节点
				if (!vn.toElement(VTDNav.PARENT)) { // 到跟节点都没有找到
					LOGGER.error(Messages.getString("file.RowIdUtil.logger2"));
					return null;
				}
			}
			int originalIdx = vn.getAttrVal("original");
			String original = originalIdx != -1 ? vn.toString(originalIdx) : "";
			return fileName + SPLIT_MARK + original + SPLIT_MARK + tuid;
		} catch (NavException e) {
			LOGGER.error("", e);
			e.printStackTrace();
		} finally {
			vn.pop();
		}
		return null;
	}
	
	/**
	 * 获取特殊的　rowId,　file 由　占位符替换	robert	2013-05-15
	 * @param vn
	 * @return
	 * @throws XPathEvalException
	 */
	public static String getSpecialRowId(VTDNav vn) throws XPathEvalException {
		try {
			if (!"trans-unit".equals(vn.toString(vn.getCurrentIndex()))) {
				LOGGER.debug(Messages.getString("file.RowIdUtil.logger1"));
				throw new XPathEvalException(Messages.getString("file.RowIdUtil.msg1"));
			}
			vn.push();
			int idIdx = vn.getAttrVal("id");
			String tuid = idIdx != -1 ? vn.toString(idIdx) : "";

			while (!"file".equals(vn.toString(vn.getCurrentIndex()))) { // 找到file节点
				if (!vn.toElement(VTDNav.PARENT)) { // 到跟节点都没有找到
					LOGGER.error(Messages.getString("file.RowIdUtil.logger2"));
					return null;
				}
			}
			int originalIdx = vn.getAttrVal("original");
			String original = originalIdx != -1 ? vn.toString(originalIdx) : "";
			return "%%%FILE%%%" + SPLIT_MARK + original + SPLIT_MARK + tuid;
		} catch (NavException e) {
			LOGGER.error("", e);
			e.printStackTrace();
		} finally {
			vn.pop();
		}
		return null;
	}

	/**
	 * 得到编辑器中一行文本段数据的唯一标识
	 * @param fileName
	 *            xliff文件名
	 * @param original
	 *            file节点下的original属性值（即xliff文件对应的源文件名）
	 * @param tuid
	 *            翻译单元的id属性的值
	 * @return 编辑器中唯一标识该行文本段的Id;
	 */
	public static String getRowId(String fileName, String original, String tuid) {
		return fileName + SPLIT_MARK + original + SPLIT_MARK + tuid;
	}

	/**
	 * 根据RowId得到定位到该翻译单元的XPath
	 * @param rowId
	 *            编辑器中唯一标识该行文本段的Id
	 * @return 定位到该翻译单元的XPath;
	 */
	public static String parseRowIdToXPath(String rowId) {
		if (rowId == null) {
			return null;
		}
		String[] strs = splitRowId(rowId);
		if (strs != null) {
			// if (strs[1] == null || strs[1].length() == 0) { // 如果没有“original”属性
			// return "/xliff/file[@original=\"\" or not(@original)]/body//trans-unit[@id=\"" + strs[2] + "\"]";
			// }
			return "/xliff/file[@original=\"" + strs[1] + "\"]/body//trans-unit[@id=\"" + strs[2] + "\"]";
		}
		return null;
	}

	/**
	 * 根据RowId得到定位到该翻译单元的XPath
	 * @param rowId
	 *            编辑器中唯一标识该行文本段的Id
	 * @return 定位到该翻译单元的XPath;
	 */
	public static String parseRowIdToXPathWithCondition(String rowId, String condition) {
		if (rowId == null) {
			return null;
		}
		String[] strs = splitRowId(rowId);
		if (strs != null) {
			return "/xliff/file[@original=\"" + strs[1] + "\"]/body//trans-unit[@id=\"" + strs[2] + "\" and "
					+ condition + "]";
		}
		return null;
	}
	
	/**
	 * 获取 Xliff 文件file节点的original属性，根据rowId	robert	2012-05-14
	 */
	public static String getOriginalByRowId(String rowId){
		String[] strs = splitRowId(rowId);
		if (strs != null) {
			return strs[1];
		}
		return null;
	}

	/**
	 * 根据RowId得到定位到被分割的翻译单元所在的group节点的XPath
	 * @param rowId
	 * @return ;
	 */
	public static String parseRowIdToGroupXPath(String rowId) {
		if (rowId == null) {
			return null;
		}
		String[] strs = splitRowId(rowId);
		if (strs != null) {
			return "/xliff/file[@original=\"" + strs[1] + "\"]/body//group[@id=\"" + strs[2] + "\"]";
		}
		return null;
	}

	/**
	 * 拆分RowId
	 * @param rowId
	 *            编辑器中唯一标识该行文本段的Id
	 * @return <b>String[] {fileName, original, tuid}</b>: <br/>
	 *         fileName: xliff文件名<br/>
	 *         original: file节点下的original属性值（即xliff文件对应的源文件名）<br/>
	 *         tuid: 翻译单元的id属性的值;
	 */
	private static String[] splitRowId(String rowId) {
		String[] strs = rowId.split(SPLIT_MARK);
		if (strs.length == 3) {
			return strs;
		}
		LOGGER.debug(MessageFormat.format(Messages.getString("file.RowIdUtil.logger3"), rowId));
		return null;
	}

	/**
	 * 通过RowId得到文件名
	 * @param rowId
	 * @return ;
	 */
	public static String getFileNameByRowId(String rowId) {
		String[] strs = splitRowId(rowId);
		if (strs != null) {
			return strs[0];
		}
		return null;
	}

	public static String getFileXpathByRowId(String rowId) {
		String[] strs = splitRowId(rowId);
		if (strs != null) {
			return "/xliff/file[@original=\"" + strs[1] + "\"]";
		}
		return null;
	}

	/**
	 * 通过RowId得到翻译单元的Id
	 * @param rowId
	 * @return ;
	 */
	public static String getTUIdByRowId(String rowId) {
		String[] strs = splitRowId(rowId);
		if (strs != null) {
			return strs[2];
		}
		return null;
	}

	/**
	 * 按照文件名对RowId进行分组
	 * @param rowIdList
	 *            RowId集合
	 * @return 分组后的RowId。<br/>
	 *         key: 文件名；value: 包含在此文件中的RowId;
	 */
	public static Map<String, List<String>> groupRowIdByFileName(List<String> rowIdList) {
		// 由于这里有排序操作，会影响编辑器中的顺序，所以新建一个
		List<String> list = new ArrayList<String>(rowIdList);
		if (rowIdList.size() > 1) { // 个数为一个以上，则进行排序。
			Collections.sort(list);
		}
		Map<String, List<String>> map = new HashMap<String, List<String>>();
		for (String rowId : list) {
			String fileName = getFileNameByRowId(rowId);
			List<String> rowIds = map.get(fileName);
			if (rowIds == null) {
				rowIds = new ArrayList<String>();
				map.put(fileName, rowIds);
			}
			rowIds.add(rowId);
		}

		return map;
	}

}

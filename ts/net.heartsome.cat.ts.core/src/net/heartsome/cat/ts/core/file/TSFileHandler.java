package net.heartsome.cat.ts.core.file;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;

import net.heartsome.cat.common.core.Constant;
import net.heartsome.cat.common.file.AbstractFileHandler;
import net.heartsome.cat.common.util.CommonFunction;
import net.heartsome.cat.ts.core.Utils;
import net.heartsome.cat.ts.core.bean.AltTransBean;
import net.heartsome.cat.ts.core.bean.NoteBean;
import net.heartsome.cat.ts.core.bean.PropBean;
import net.heartsome.cat.ts.core.bean.PropGroupBean;
import net.heartsome.cat.ts.core.bean.TransUnitBean;
import net.heartsome.cat.ts.core.resource.Messages;
import net.heartsome.xml.vtdimpl.VTDUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ximpleware.AutoPilot;
import com.ximpleware.ModifyException;
import com.ximpleware.NavException;
import com.ximpleware.TranscodeException;
import com.ximpleware.VTDGen;
import com.ximpleware.VTDNav;
import com.ximpleware.XMLModifier;
import com.ximpleware.XPathEvalException;
import com.ximpleware.XPathParseException;

public class TSFileHandler extends AbstractFileHandler {

	// 日志管理器
	private final static Logger logger = LoggerFactory.getLogger(TSFileHandler.class);

	// 文件历史访问列表。键为文件名，值为文本段的索引，空字符串值为默认值，表示第一个文本段。
	private Map<String, String> accessHistory = super.createFileHistory(10, 10);

	// 项目中文件与临时文件的映射表，键为项目中的XILFF文件，值为实际编辑的临时文件
	private LinkedHashMap<String, String> tmpFileMap = new LinkedHashMap<String, String>();

	// 项目中已打开的文件的修改状态。
	private Hashtable<String, Boolean> filesChangeStatus = new Hashtable<String, Boolean>();

	// 项目中的文件属性集合。其中 Key 为文件节点索引，从 1 开始。值为 Hashtable 对象，用于存储文件节点相关属性的键值对。
	private Hashtable<Integer, Hashtable<String, String>> fileAttrs = null;

	// 项目中的翻译单元对象缓冲区
	private Hashtable<String, Vector<TransUnitBean>> transunits = new Hashtable<String, Vector<TransUnitBean>>();

	// 项目各打开文件中实际翻译单元的数目
	private Hashtable<String, Integer> actualTuCount = new Hashtable<String, Integer>();

	// 项目中的翻译单元索引对象缓冲区
	private Hashtable<String, Vector<String>> tuIndexs = new Hashtable<String, Vector<String>>();

	// 项目中翻译单元缓冲区大小
	private static int TU_CACHE_SIZE = 1000;

	// 分析报告文件格式
	public static int REPORT_FORMAT_XML = 1;
	public static int REPORT_FORMAT_HTML = 2;
	public static int REPORT_FORMAT_PDF = 3;
	public static int REPORT_FORMAT_TXT = 4;

	// 可选分析流程定义
	public static int ANALYSIS_MODE_STATUS = 1;
	public static int ANALYSIS_MODE_TRANSLATOR_PROGRESS = 2;
	public static int ANALYSIS_MODE_EDITOR_PROGRESS = 4;

	private final String hsR7NSUrl = "http://www.heartsome.net.cn/2008/XLFExtension";
	private final String hsR8NSUrl = "http://www.heartsome.net/2010/XLFExtension";
	private final String hsNSPrefix = "hs";

	// private final String xmlNSPrefix = "xml";
	// private final String xmlNSUrl = "";

	public LinkedHashMap<String, String> getTmpFileMap() {
		return tmpFileMap;
	}

	public void setTmpFileMap(LinkedHashMap<String, String> tmpFileMap) {
		this.tmpFileMap = tmpFileMap;
	}

	public Hashtable<String, Vector<TransUnitBean>> getTransunits() {
		return transunits;
	}

	public Hashtable<String, Vector<String>> getTuIndexs() {
		return tuIndexs;
	}

	@Override
	public Map<String, Object> closeFile(String filename) {
		File file = new File(filename);
		return closeFile(file);
	}

	@Override
	public Map<String, Object> closeFile(File file) {
		// 验证文件是否存在
		if (file == null || !file.exists()) {
			String errorMsg = Messages.getString("file.TSFileHandler.logger1");
			logger.debug(errorMsg);
			return getErrorResult(errorMsg, null);
		}

		String filename = file.getAbsolutePath();

		// 检查文件是否被修改，返回视图层提示是否要保存。
		if (filesChangeStatus.get(filename)) {
			String msg = MessageFormat.format(Messages.getString("file.TSFileHandler.logger2"), filename);
			logger.warn(msg);
			Map<String, Object> result = getErrorResult(msg, null);
			result.put("FileChanged", true);
			return result;
		}

		// 移除缓存中的翻译单元。
		transunits.remove(filename);

		// 移除缓存中的翻译单元索引。
		tuIndexs.remove(filename);

		// 移除缓存中的翻译单元实际计数。
		actualTuCount.remove(filename);

		// 移除临时文件映射表中的记录。
		tmpFileMap.remove(filename);

		// 删除自动保存的临时文件。
		File autosaveFolder = null;
		if (Utils.getCurrentOS() == Utils.OS_WINDOWS) {
			// TODO 自动保存目录需要加上项目前缀。
			autosaveFolder = new File("autosave");
		} else {
			// TODO 自动保存目录需要加上项目前缀。
			autosaveFolder = new File(".autosave");
		}

		if (autosaveFolder != null && autosaveFolder.exists() && autosaveFolder.isDirectory()) {
			File autosaveFile = new File(autosaveFolder.getAbsolutePath() + Utils.getFileSeparator() + filename
					+ ".autosave");
			if (autosaveFile != null && autosaveFile.exists()) {
				boolean cleanAutoSaveFileResult = autosaveFile.delete();
				if (!cleanAutoSaveFileResult) {
					logger.error(MessageFormat.format(Messages.getString("file.TSFileHandler.logger3"), new Object[] { filename,
							autosaveFile.getAbsolutePath() }));
				}
			}
		}

		// 移除文件修改状态。
		filesChangeStatus.remove(filename);

		return getSuccessResult();
	}

	@Override
	public Map<String, Object> closeFiles(List<String> files) {
		if (files == null || files.isEmpty()) {
			String errorMsg = Messages.getString("file.TSFileHandler.logger4");
			logger.error(errorMsg);
			return getErrorResult(errorMsg, null);
		}

		Iterator<String> it = files.iterator();
		while (it.hasNext()) {
			String filename = it.next();
			Map<String, Object> midResult = closeFile(filename);
			if (midResult.get(Constant.RETURNVALUE_RESULT).equals(Constant.RETURNVALUE_RESULT_FAILURE)) {
				return midResult;
			}
		}

		return getSuccessResult();
	}

	@Override
	protected Map<String, Object> openFile(String filename, int tuCount) {
		return openFile(new File(filename), tuCount);
	}

	@Override
	public Map<String, Object> openFile(File file) {
		return openFile(file, 0);
	}

	@Override
	public Map<String, Object> openFile(String filename) {
		File file = new File(filename);
		return openFile(file, 0);
	}

	@Override
	public Map<String, Object> openFile(File file, int tuCount) {
		long start = System.currentTimeMillis();

		// 验证文件是否存在
		if (file == null || !file.exists()) {
			String errorMsg = Messages.getString("file.TSFileHandler.logger5");
			logger.error(errorMsg);
			return getErrorResult(errorMsg, null);
		}

		// 判断是否还有缓存空间。
		boolean canCache = tuCount < TU_CACHE_SIZE;

		// 当前文件中解析并缓存翻译单元计数器。
		int parsedTuCount = 0;

		// 当前文件未解析缓存的翻译单元计数器。
		int noParseTuCount = 0;

		String filename = file.getAbsolutePath();
		int fileIndex = 1;
		// 解析文件并获取索引
		VTDGen vgRead = new VTDGen();
		if (vgRead.parseFile(filename, true)) {
			VTDNav vnRead = vgRead.getNav();
			VTDUtils vu = null;
			try {
				vu = new VTDUtils(vnRead);
				// 创建临时文件
				File tmpFile = createTmpFile();
				XMLModifier xm = new XMLModifier(vnRead);
				FileOutputStream fos = new FileOutputStream(tmpFile);
				xm.output(fos);
				fos.close();
				tmpFileMap.put(filename, tmpFile.getAbsolutePath());
				filesChangeStatus.put(filename, false);
			} catch (ModifyException e) {
				String errorMsg = MessageFormat.format(Messages.getString("file.TSFileHandler.logger6"), filename);
				logger.error(errorMsg, e);
				return getErrorResult(errorMsg, e);
			} catch (TranscodeException e) {
				String errorMsg = MessageFormat.format(Messages.getString("file.TSFileHandler.logger7"), filename);
				logger.error(errorMsg, e);
				return getErrorResult(errorMsg, e);
			} catch (IOException e) {
				String errorMsg = MessageFormat.format(Messages.getString("file.TSFileHandler.logger8"), filename);
				logger.error(errorMsg, e);
				return getErrorResult(errorMsg, e);
			} catch (NavException e) {
				String errorMsg = Messages.getString("file.TSFileHandler.logger9");
				logger.error(errorMsg, e);
				return getErrorResult(errorMsg, e);
			}

			// 创建翻译单元集合缓存。
			Vector<TransUnitBean> tusCache = new Vector<TransUnitBean>();

			// 创建翻译单元索引集合缓存。
			Vector<String> tuIndexCache = new Vector<String>();

			// 初始化文件节点属性集合。
			fileAttrs = new Hashtable<Integer, Hashtable<String, String>>();

			AutoPilot apFile = new AutoPilot(vnRead);
			String fileNode = "/xliff/file";
			try {
				apFile.selectXPath(fileNode);
				while (apFile.evalXPath() != -1) {
					fileAttrs.put(fileIndex, vu.getCurrentElementAttributs());

					AutoPilot apTU = new AutoPilot(vnRead);
					apTU.selectXPath("body//trans-unit");
					vnRead.push();
					while (apTU.evalXPath() != -1) {

						// 如果缓冲区未满，则解析文件内容并缓存，否则只计数，不解析内容。
						if (canCache) {
							String tuid = "";
							String srcText = "";
							String srcContent = "";
							String tgtText = "";
							String tgtContent = "";
							Hashtable<String, String> srcProps = null;
							Hashtable<String, String> tgtProps = null;

							// 取翻译单元所有属性
							String tmpNode = "";
							vnRead.push();
							Hashtable<String, String> tuProps = vu.getCurrentElementAttributs();
							vnRead.pop();
							tuid = tuProps.get("id");

							// 取翻译单元源节点完整文本，含内部标记。
							vnRead.push();
							tmpNode = "./source";
							srcContent = vu.getElementContent(tmpNode);
							// vnRead.pop();

							// 取翻译单元源文本。
							// vnRead.push();
							srcText = vu.getElementPureText();

							// 取翻译单元源节点属性。
							srcProps = vu.getCurrentElementAttributs();
							vnRead.pop();

							// 取翻译单元目标节点完整文本，含内部标记。
							vnRead.push();
							tmpNode = "./target";
							tgtContent = vu.getElementContent(tmpNode);
							// vnRead.pop();

							// 取翻译单元目标文本。
							// vnRead.push();
							tgtText = vu.getElementPureText();

							// 取翻译单元目标节点属性。
							tgtProps = vu.getCurrentElementAttributs();
							vnRead.pop();

							// 获取所有的 alttrans 匹配节点。
							vnRead.push();
							Vector<AltTransBean> matches = getAltTrans(vu);
							vnRead.pop();

							// 构建翻译单元对象，存储节点信息
							TransUnitBean tub = new TransUnitBean(tuid, srcContent, srcText);
							tub.setTuProps(tuProps);
							tub.setSrcProps(srcProps);
							tub.setTgtContent(tgtContent);
							tub.setTgtText(tgtText);
							tub.setTgtProps(tgtProps);
							tub.setMatches(matches);
							vnRead.push();
							tub.setNotes(getNotes(vu));
							vnRead.pop();

							vnRead.push();
							tub.setPropgroups(getPrpoGroups(vu));
							vnRead.pop();

							tusCache.add(tub);
							tuIndexCache.add(filename + ";" + fileIndex + ";" + tuid);

							// 解析的翻译单元节点计数
							parsedTuCount++;

							if (tuCount + parsedTuCount == TU_CACHE_SIZE) {
								canCache = false;
							}
						} else {
							// 未解析的翻译单元节点计数
							noParseTuCount++;
						}
					}
					vnRead.pop();

					// 文件节点索引计数
					fileIndex++;
				}

				transunits.put(filename, tusCache);
				tuIndexs.put(filename, tuIndexCache);
				actualTuCount.put(filename, parsedTuCount + noParseTuCount);
				accessHistory.put(filename, "");
			} catch (XPathEvalException e) {
				String errorMsg = Messages.getString("file.TSFileHandler.logger10");
				logger.error(errorMsg, e);
				return getErrorResult(errorMsg, e);
			} catch (NavException e) {
				String errorMsg = Messages.getString("file.TSFileHandler.logger11");
				logger.error(errorMsg, e);
				return getErrorResult(errorMsg, e);
			} catch (XPathParseException e) {
				String errorMsg = Messages.getString("file.TSFileHandler.logger12");
				logger.error(errorMsg, e);
				return getErrorResult(errorMsg, e);
			}
		} else {
			String errorMsg = MessageFormat.format(Messages.getString("file.TSFileHandler.logger13"), filename);
			logger.error(errorMsg);
			return getErrorResult(errorMsg, null);
		}

		long end = System.currentTimeMillis();

		// 输出结果
		long resultMS = end - start;
		long resultS = resultMS / 1000;
		long resultM = resultMS / (1000 * 60);
		System.gc();
		logger.info(Messages.getString("file.TSFileHandler.logger14"), new Object[] { resultM, resultS, resultMS });
		Map<String, Object> result = getSuccessResult();
		result.put("CurCachedTuCount", Integer.valueOf(parsedTuCount));
		result.put("TotalCachedTuCount", Integer.valueOf(parsedTuCount + tuCount));
		return result;
	}

	// 获取当前节点的所有批注。
	private Vector<NoteBean> getNotes(VTDUtils vu) throws XPathEvalException, NavException, XPathParseException {
		Vector<NoteBean> notes = new Vector<NoteBean>();
		VTDNav vn = vu.getVTDNav();
		AutoPilot ap = new AutoPilot(vn);
		ap.selectXPath("./note");
		while (ap.evalXPath() != -1) {
			NoteBean note = new NoteBean(vu.getElementContent());

			int attInx = vn.getAttrVal("xml:lang");
			if (attInx != -1) {
				note.setLang(vn.toString(attInx));
			}

			attInx = vn.getAttrVal("from");
			if (attInx != -1) {
				note.setFrom(vn.toString(attInx));
			}

			attInx = vn.getAttrVal("priority");
			if (attInx != -1) {
				note.setPriority(vn.toString(attInx));
			}

			attInx = vn.getAttrVal("annotates");
			if (attInx != -1) {
				note.setAnnotates(vn.toString(attInx));
			}

			notes.add(note);
		}

		if (notes.isEmpty()) {
			notes = null;
		}

		return notes;
	}

	private Vector<AltTransBean> getAltTrans(VTDUtils vu) throws XPathParseException, XPathEvalException, NavException {
		VTDNav vn = vu.getVTDNav();
		Vector<AltTransBean> result = new Vector<AltTransBean>();
		String xpath = "./alt-trans";
		AutoPilot ap = new AutoPilot(vn);
		ap.selectXPath(xpath);
		while (ap.evalXPath() != -1) {
			AltTransBean atb = new AltTransBean();
			AutoPilot apAltTrans = new AutoPilot(vn);

			// 获取当前匹配节点全部属性。
			atb.setMatchProps(vu.getCurrentElementAttributs());

			// 获取源节点内容、属性及纯文本
			xpath = "./source";
			apAltTrans.resetXPath();
			apAltTrans.selectXPath(xpath);
			vn.push();
			if (apAltTrans.evalXPath() != -1) {
				atb.setSrcContent(vu.getElementContent());
				atb.setSrcProps(vu.getCurrentElementAttributs());
				atb.setSrcText(vu.getElementPureText());
			}
			vn.pop();

			// 获取目标节点内容、属性及纯文本
			xpath = "./target";
			apAltTrans.resetXPath();
			apAltTrans.selectXPath(xpath);
			vn.push();
			if (apAltTrans.evalXPath() != -1) {
				atb.setTgtContent(vu.getElementContent());
				atb.setTgtProps(vu.getCurrentElementAttributs());
				atb.setTgtText(vu.getElementPureText());
				vn.pop();
			} else {
				vn.pop();
				continue;
			}

			// 获取匹配节点的属性组集合
			vn.push();
			atb.setPropGroups(getPrpoGroups(vu));
			vn.pop();
			result.add(atb);
		}

		if (result.isEmpty()) {
			return null;
		} else {
			return result;
		}
	}

	// 获取当前节点下的属性组集合。
	private Vector<PropGroupBean> getPrpoGroups(VTDUtils vu) throws XPathParseException, XPathEvalException,
			NavException {
		VTDNav vn = vu.getVTDNav();
		Vector<PropGroupBean> pgs = new Vector<PropGroupBean>();
		AutoPilot ap = new AutoPilot(vn);
		ap.selectXPath("./prop-group");
		while (ap.evalXPath() != -1) {
			vn.push();
			Vector<PropBean> props = getProps(vu);
			vn.pop();
			PropGroupBean pg = new PropGroupBean(props);

			// 获取属性组名称。
			int nameInx = vn.getAttrVal("name");
			if (nameInx != -1) {
				pg.setName(vn.toString(nameInx));
			}

			pgs.add(pg);
		}

		ap.resetXPath();
		ap.declareXPathNameSpace(hsNSPrefix, hsR7NSUrl);
		ap.selectXPath("./hs:prop-group");
		while (ap.evalXPath() != -1) {
			vn.push();
			Vector<PropBean> props = getProps(vu);
			vn.pop();
			PropGroupBean pg = new PropGroupBean(props);

			// 获取属性组名称。
			int nameInx = vn.getAttrVal("name");
			if (nameInx != -1) {
				pg.setName(vn.toString(nameInx));
			}

			pgs.add(pg);
		}

		if (pgs.isEmpty()) {
			pgs = null;
		}
		return pgs;
	}

	// 获取当前节点下的属性元素子节点。
	private Vector<PropBean> getProps(VTDUtils vu) throws XPathParseException, XPathEvalException, NavException {
		VTDNav vn = vu.getVTDNav();
		Vector<PropBean> props = new Vector<PropBean>();
		AutoPilot ap = new AutoPilot(vn);
		ap.selectXPath("./prop");
		while (ap.evalXPath() != -1) {
			String proptype = null;
			String value = null;
			String lang = null;
			int attInx = vn.getAttrVal("prop-type");
			if (attInx != -1) {
				proptype = vn.toString(attInx);
			}

			attInx = vn.getAttrVal("xml:lang");
			if (attInx != -1) {
				lang = vn.toString(attInx);
			}

			value = vu.getElementContent();

			PropBean prop = new PropBean(proptype, value, lang);
			props.add(prop);
		}

		ap.resetXPath();
		ap.declareXPathNameSpace(hsNSPrefix, hsR7NSUrl);
		ap.selectXPath("./hs:prop");
		while (ap.evalXPath() != -1) {
			String proptype = null;
			String value = null;
			String lang = null;
			int attInx = vn.getAttrVal("prop-type");
			if (attInx != -1) {
				proptype = vn.toString(attInx);
			}

			attInx = vn.getAttrVal("xml:lang");
			if (attInx != -1) {
				lang = vn.toString(attInx);
			}

			value = vu.getElementContent();

			PropBean prop = new PropBean(proptype, value, lang);
			props.add(prop);
		}

		if (props.isEmpty()) {
			props = null;
		}
		return props;
	}

	@Override
	public Map<String, Object> openFiles(List<String> files) {
		if (files == null || files.isEmpty()) {
			String errorMsg = Messages.getString("file.TSFileHandler.logger15");
			logger.error(errorMsg);
			return getErrorResult(errorMsg, null);
		}

		boolean canCache = true;
		int tuCount = 0;
		Iterator<String> it = files.iterator();
		while (it.hasNext() && canCache) {
			String filename = it.next();
			Map<String, Object> midResult = openFile(filename, tuCount);
			if (midResult.get(Constant.RETURNVALUE_RESULT).equals(Constant.RETURNVALUE_RESULT_FAILURE)) {
				return midResult;
			}
		}

		return getSuccessResult();
	}

	@Override
	public Map<String, Object> saveFile(String srcFile, String tgtFile) {
		return saveFile(new File(srcFile), new File(tgtFile));
	}

	@Override
	public Map<String, Object> saveFile(File srcFile, File tgtFile) {
		// TODO 视图层要验证另存的目标文件是否存在，如果存在时需提示是否覆盖。

		if (srcFile == null || !srcFile.exists()) {
			String msg = MessageFormat.format(Messages.getString("file.TSFileHandler.logger16"), srcFile.getAbsoluteFile());
			logger.error(msg);
			return getErrorResult(msg, null);
		}

		if (tgtFile == null || !tgtFile.exists()) {
			String msg = MessageFormat.format(Messages.getString("file.TSFileHandler.logger17"), tgtFile.getAbsoluteFile());
			logger.error(msg);
			return getErrorResult(msg, null);
		}

		FileInputStream fis = null;
		BufferedInputStream bis = null;

		try {
			fis = new FileInputStream(srcFile);
			bis = new BufferedInputStream(fis);
		} catch (FileNotFoundException e) {
			String msg = MessageFormat.format(Messages.getString("file.TSFileHandler.logger16"), srcFile.getAbsoluteFile());
			logger.error(msg, e);
			return getErrorResult(msg, e);
		}

		FileOutputStream fos = null;
		BufferedOutputStream bos = null;
		try {
			fos = new FileOutputStream(tgtFile);
			bos = new BufferedOutputStream(fos);
		} catch (FileNotFoundException e) {
			String msg = MessageFormat.format(Messages.getString("file.TSFileHandler.logger17"), tgtFile.getAbsoluteFile());
			logger.error(msg, e);
			return getErrorResult(msg, e);
		}

		byte[] buffer = new byte[2048];

		try {
			while (bis.read(buffer) != -1) {
				bos.write(buffer, 0, buffer.length);
			}

			bos.flush();
			bos.close();
			bis.close();
			fos.close();
			fis.close();
		} catch (IOException e) {
			String msg = null;
			if (srcFile.getAbsolutePath().equals(tgtFile.getAbsolutePath())) {
				msg = MessageFormat.format(Messages.getString("file.TSFileHandler.logger18"), srcFile.getAbsolutePath());
			} else {
				msg = MessageFormat.format(Messages.getString("file.TSFileHandler.logger19"), new Object[] { srcFile.getAbsolutePath(),
						tgtFile.getAbsolutePath() });
			}

			logger.error(msg, e);
			return getErrorResult(msg, e);
		}
		return getSuccessResult(Messages.getString("file.TSFileHandler.logger20"));
	}

	/**
	 * 获取打开的文件数目。
	 * 
	 * */
	public int getOpenedFileCount() {
		return tmpFileMap.size();
	}

	/**
	 * 获取当前缓存的翻译单元数目。
	 * */
	public int getCachedTuCount() {
		int result = 0;
		Iterator<String> it = tuIndexs.keySet().iterator();
		while (it.hasNext()) {
			Vector<String> tuIndex = tuIndexs.get(it.next());
			result += tuIndex.size();
		}
		return result;
	}

	/**
	 * 创建一个用于编辑的临时XLIFF文件，并存放在一个隐藏的项目临时文件夹内。
	 * */
	public File createTmpFile() throws IOException {
		File tmpFile = null;
		File folder = null;
		File curFolder = new File(".");

		if (Utils.OS_WINDOWS == Utils.getCurrentOS()) {
			folder = new File(curFolder.getAbsoluteFile() + Utils.getFileSeparator() + "~$temp");
			if (!folder.exists()) {
				folder.mkdirs();
			}
			folder.deleteOnExit();

			String sets = "attrib +H \"" + folder.getAbsolutePath() + "\"";
			// 输出命令串
			System.out.println(sets);
			// 运行命令串
			Runtime.getRuntime().exec(sets);
		} else {
			folder = new File(curFolder.getAbsoluteFile() + Utils.getFileSeparator() + ".temp");
			if (!folder.exists()) {
				folder.mkdirs();
			}
			folder.deleteOnExit();
		}

		tmpFile = File.createTempFile("tmp", CommonFunction.R8XliffExtension_1, folder);
		tmpFile.deleteOnExit();

		return tmpFile;
	}

	// 获取错误返回值。
	private Map<String, Object> getErrorResult(String msg, Throwable e) {
		if (Constant.MODE_DEBUG == Constant.RUNNING_MODE && e != null) {
			e.printStackTrace();
		}

		Map<String, Object> resultMap = new HashMap<String, Object>();
		resultMap.put(Constant.RETURNVALUE_RESULT, Constant.RETURNVALUE_RESULT_FAILURE);
		resultMap.put(Constant.RETURNVALUE_MSG, msg);
		resultMap.put(Constant.RETURNVALUE_EXCEPTION, e);
		return resultMap;
	}

	// 获取正确返回值并记录消息日志。
	private Map<String, Object> getSuccessResult(String msg) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		resultMap.put(Constant.RETURNVALUE_RESULT, Constant.RETURNVALUE_RESULT_SUCCESSFUL);
		resultMap.put(Constant.RETURNVALUE_MSG, msg);
		return resultMap;
	}

	// 获取正确返回值
	private Map<String, Object> getSuccessResult() {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		resultMap.put(Constant.RETURNVALUE_RESULT, Constant.RETURNVALUE_RESULT_SUCCESSFUL);
		return resultMap;
	}

	/**
	 * 获取最近访问文件列表.
	 * 
	 * */
	public Map<String, String> getAccessHistory() {
		return accessHistory;
	}

	/**
	 * 按指定模式更改指定字符串大小写。
	 * 
	 * @param text
	 *            要处理的字符串。
	 * @param mode
	 *            更改大小写的模式。详见{@linknet.heartsome.cat.bean.Constant}
	 * */
	public String changeCase(String text, int mode) {
		String result = null;
		if (text == null || text.trim().equals("")) {
			return text;
		}

		switch (mode) {
		case Constant.CHANGECASE_LOWER: // 全部小写
			result = text.toLowerCase();
			break;
		case Constant.CHANGECASE_UPPER: // 全部大写
			result = text.toUpperCase();
			break;
		case Constant.CHANGECASE_SENTENCE: // 句子大写
			result = changeToSentenceCase(text);
			break;
		case Constant.CHANGECASE_TOGGLE: // 全部切换
			StringBuilder toggle = new StringBuilder();
			for (int i = 0; i < text.length(); i++) {
				if (Character.isLowerCase(text.charAt(i))) {
					toggle.append(Character.toUpperCase(text.charAt(i)));
				} else {
					toggle.append(Character.toLowerCase(text.charAt(i)));
				}
			}
			result = toggle.toString();
			break;
		default: // 标题大小写
			StringBuilder title = new StringBuilder();
			StringTokenizer tokenizer = new StringTokenizer(text, Constant.SEPARATORS_1, true);
			while (tokenizer.hasMoreElements()) {
				title.append(changeToSentenceCase(tokenizer.nextToken()));
			}
			result = title.toString();
		}

		return result;
	}

	// 以句子模式改变字符串的大小写，首字母大写，其它字母小写。
	private String changeToSentenceCase(String text) {
		String result = "";
		if (text == null || text.trim().equals("")) {
			return text;
		}

		if (text.length() == 1) {
			result = text.toUpperCase();
		} else if (text.length() > 1) {
			result = Character.toUpperCase(text.charAt(0)) + text.substring(1).toLowerCase();
		}

		return result;
	}

	/**
	 * 分析指定名称的 XLIFF 文件或项目文件，获取其进度报告。
	 * 
	 * @param xliff
	 *            要分析的文件名。
	 * @param reportFormat
	 *            报告的格式。
	 * @return Map 中 ReportPath 键值表示最终生成的报告路径。
	 * */
	public Map<String, Object> fileAnalysis(String filename, int reportFormat, int analysisMode, float eqvFactor) {
		File file = new File(filename);
		String filepath = file.getAbsolutePath();
		if (file == null || !file.exists()) {
			String msg = Messages.getString("file.TSFileHandler.logger21");
			logger.error(msg);
			return getErrorResult(msg, null);
		}

		// 解析文件，判断其文件类型，并调用不同的分析方法。
		VTDGen vg = new VTDGen();
		if (vg.parseFile(filepath, true)) {
			VTDNav vn = vg.getNav();
			int inx = vn.getRootIndex();
			try {
				String rootName = vn.toString(inx);
				if (rootName.equals("xliff")) {
					return xliffAnalysis(file, analysisMode, eqvFactor);
				} else if (rootName.equals("hsts-project")) {
					return projectAnalysis(file);
				} else {
					String msg = MessageFormat.format(Messages.getString("file.TSFileHandler.logger22"), filepath);
					logger.error(msg);
					return getErrorResult(msg, null);
				}
			} catch (NavException e) {
				String msg = MessageFormat.format(Messages.getString("file.TSFileHandler.logger23"), filepath);
				logger.error(msg, e);
				return getErrorResult(msg, e);
			} catch (ModifyException e) {
				String msg = Messages.getString("file.TSFileHandler.logger24");
				logger.error(msg, e);
				return getErrorResult(msg, e);
			} catch (UnsupportedEncodingException e) {
				String msg = Messages.getString("file.TSFileHandler.logger25");
				logger.error(msg, e);
				return getErrorResult(msg, e);
			} catch (TranscodeException e) {
				String msg = Messages.getString("file.TSFileHandler.logger26");
				logger.error(msg, e);
				return getErrorResult(msg, e);
			} catch (IOException e) {
				String msg = Messages.getString("file.TSFileHandler.logger27");
				logger.error(msg, e);
				return getErrorResult(msg, e);
			} catch (XPathParseException e) {
				String msg = MessageFormat.format(Messages.getString("file.TSFileHandler.logger23"), filepath);
				logger.error(msg, e);
				return getErrorResult(msg, e);
			} catch (XPathEvalException e) {
				String msg = MessageFormat.format(Messages.getString("file.TSFileHandler.logger23"), filepath);
				logger.error(msg, e);
				return getErrorResult(msg, e);
			}
		} else {
			String msg = MessageFormat.format(Messages.getString("file.TSFileHandler.logger28"), filepath);
			logger.error(msg);
			return getErrorResult(msg, null);
		}
	}

	// 分析文件字数状态。直接返回报告内容的 XML 字符串。
	protected String analysisStatus(VTDNav vn, final String filepath, float eqvFactor) throws NavException,
			XPathParseException, XPathEvalException {
		VTDUtils vu = new VTDUtils(vn);
		StringBuilder sb = new StringBuilder();
		sb.append("\t<status-infos purpose=\"");

		sb.append(Messages.getString("file.TSFileHandler.analysisStatus"));
		sb.append("\" filepath=\"" + filepath + "\">\n");

		int newSegs = 0; // 新文本段。无内外匹配、无内外重复的文本段。
		int innerMatchSegs = 0; // 内部匹配。所有来自文件内互相比较产生的繁殖匹配，即包含有 autoFazzy_
		// 前缀的匹配。
		int outerMatchSegs = 0; // 外部匹配。所有来自记忆库的翻译匹配，即不包含 autoFazzy_ 前缀的匹配。

		int innerRepeatedSegs = 0; // 内部重复。所有在同批分析的其它 XLIFF 文件中的重复。
		int outerRepeatedSegs = 0; // 外部重复。所有来自记忆库匹配的 100％ 匹配，UE 版以下的包括上下文匹配。
		int countSegs = 0;

		int newWords = 0;
		int innerMatchWords = 0;
		int outerMatchWords = 0;
		int innerRepeatedWords = 0;
		int outerRepeatedWords = 0;
		int countWords = 0;

		AutoPilot ap = new AutoPilot(vn);
		ap.selectXPath("//trans-unit");
		while (ap.evalXPath() != -1) {
			String tgtText = vu.getChildPureText("target");
			vn.push();

			// 开始统计分析。
			String srcText = vu.getChildPureText("source");
			String lang = vu.getElementAttribute("source", "xml:lang");
			int words = wordCount(srcText, lang);

			vn.pop();
		}

		sb.append("\t</status-infos>\n");

		return sb.toString();
	}

	// 分析翻译进度。直接返回报告内容的 XML 字符串。
	protected String analysisTranslatorProgress(VTDNav vn, final String filepath, float eqvFactor) throws NavException,
			XPathParseException, XPathEvalException {
		VTDUtils vu = new VTDUtils(vn);
		StringBuilder sb = new StringBuilder();
		sb.append("\t<status-infos purpose=\"");

		sb.append(Messages.getString("file.TSFileHandler.analysisTranslatorProgress"));
		sb.append("\" filepath=\"" + filepath + "\">\n");

		int translatedSegs = 0;
		int untranslatedSegs = 0;
		int translatedWords = 0;
		int untranslatedWords = 0;
		boolean isTranslated = false;

		AutoPilot ap = new AutoPilot(vn);
		ap.selectXPath("//trans-unit");
		while (ap.evalXPath() != -1) {
			String tgtText = vu.getChildPureText("target");

			// 目标文本为空或 null 表示未翻译，反之为已翻译
			if (tgtText == null || tgtText.trim().equals("")) {
				isTranslated = false;
			} else {
				isTranslated = true;
			}

			vn.push();

			// 开始统计分析。
			String srcText = vu.getChildPureText("source");
			String lang = vu.getElementAttribute("source", "xml:lang");
			int words = wordCount(srcText, lang);

			if (isTranslated) {
				translatedSegs++;
				translatedWords += words;
			} else {
				untranslatedSegs++;
				untranslatedWords += words;
			}

			vn.pop();
		}

		sb.append("\t\t<status-info type=\"");
		sb.append(Messages.getString("file.TSFileHandler.translatedSegs"));
		sb.append("\" statisticunits=\"segment\">");
		sb.append(translatedSegs);
		sb.append("</status-info>\n");

		sb.append("\t\t<status-info type=\"");
		sb.append(Messages.getString("file.TSFileHandler.untranslatedSegs"));
		sb.append("\" statisticunits=\"segment\">");
		sb.append(untranslatedSegs);
		sb.append("</status-info>\n");

		sb.append("\t\t<status-info type=\"");
		sb.append(Messages.getString("file.TSFileHandler.percent1"));
		sb.append("\" statisticunits=\"segment\">");
		sb.append(translatedSegs * 100 / (translatedSegs + untranslatedSegs));
		sb.append("%");
		sb.append("</status-info>\n");

		sb.append("\t\t<status-info type=\"");
		sb.append(Messages.getString("file.TSFileHandler.total1"));
		sb.append("\" statisticunits=\"segment\">");
		sb.append(translatedSegs + untranslatedSegs);
		sb.append("</status-info>\n");

		sb.append("\t\t<status-info type=\"");
		sb.append(Messages.getString("file.TSFileHandler.translatedWords"));
		sb.append("\" statisticunits=\"word\">");
		sb.append(translatedWords);
		sb.append("</status-info>\n");

		sb.append("\t\t<status-info type=\"");
		sb.append(Messages.getString("file.TSFileHandler.untranslatedWords"));
		sb.append("\" statisticunits=\"word\">");
		sb.append(untranslatedWords);
		sb.append("</status-info>\n");

		sb.append("\t\t<status-info type=\"");
		sb.append(Messages.getString("file.TSFileHandler.percent2"));
		sb.append("\" statisticunits=\"word\">");
		sb.append(translatedWords * 100 / (translatedWords + untranslatedWords));
		sb.append("%");
		sb.append("</status-info>\n");

		sb.append("\t\t<status-info type=\"");
		sb.append(Messages.getString("file.TSFileHandler.total2"));
		sb.append("\" statisticunits=\"word\">");
		sb.append(translatedWords + untranslatedWords);
		sb.append("</status-info>\n");

		sb.append("\t</status-infos>\n");

		return sb.toString();
	}

	// 分析编辑进度。直接返回报告内容的 XML 字符串。
	protected String analysisEditorProgress(VTDNav vn, final String filepath, float eqvFactor)
			throws XPathParseException, XPathEvalException, NavException {
		VTDUtils vu = new VTDUtils(vn);
		StringBuilder sb = new StringBuilder();
		sb.append("\t<status-infos purpose=\"");

		sb.append(Messages.getString("file.TSFileHandler.analysisEditorProgress"));
		sb.append("\" filepath=\"" + filepath + "\">\n");

		int approvedSegs = 0;
		int unapprovedSegs = 0;
		int approvedWords = 0;
		int unapprovedWords = 0;
		boolean isApproved = false;

		AutoPilot ap = new AutoPilot(vn);
		ap.selectXPath("//trans-unit");
		while (ap.evalXPath() != -1) {
			int inx = vn.getAttrVal("approved");

			// 判断 approved 属性的值。存在取其结果，不存在取默认值 no。
			if (inx != -1) {
				isApproved = Boolean.parseBoolean(vn.toString(inx));
			} else {
				isApproved = false;
			}

			vn.push();

			// 开始统计分析。
			String srcText = vu.getChildPureText("source");
			String lang = vu.getElementAttribute("source", "xml:lang");
			int words = wordCount(srcText, lang);

			if (isApproved) {
				approvedSegs++;
				approvedWords += words;
			} else {
				unapprovedSegs++;
				unapprovedWords += words;
			}

			vn.pop();
		}

		sb.append("\t\t<status-info type=\"");
		sb.append(Messages.getString("file.TSFileHandler.approvedSegs"));
		sb.append("\" statisticunits=\"segment\">");
		sb.append(approvedSegs);
		sb.append("</status-info>\n");

		sb.append("\t\t<status-info type=\"");
		sb.append(Messages.getString("file.TSFileHandler.unapprovedSegs"));
		sb.append("\" statisticunits=\"segment\">");
		sb.append(unapprovedSegs);
		sb.append("</status-info>\n");

		sb.append("\t\t<status-info type=\"");
		sb.append(Messages.getString("file.TSFileHandler.percent1"));
		sb.append("\" statisticunits=\"segment\">");
		sb.append(approvedSegs * 100 / (approvedSegs + unapprovedSegs));
		sb.append("%");
		sb.append("</status-info>\n");

		sb.append("\t\t<status-info type=\"");
		sb.append(Messages.getString("file.TSFileHandler.total1"));
		sb.append("\" statisticunits=\"segment\">");
		sb.append(approvedSegs + unapprovedSegs);
		sb.append("</status-info>\n");

		sb.append("\t\t<status-info type=\"");
		sb.append(Messages.getString("file.TSFileHandler.approvedWords"));
		sb.append("\" statisticunits=\"word\">");
		sb.append(approvedWords);
		sb.append("</status-info>\n");

		sb.append("\t\t<status-info type=\"");
		sb.append(Messages.getString("file.TSFileHandler.unapprovedWords"));
		sb.append("\" statisticunits=\"word\">");
		sb.append(unapprovedWords);
		sb.append("</status-info>\n");

		sb.append("\t\t<status-info type=\"");
		sb.append(Messages.getString("file.TSFileHandler.percent2"));
		sb.append("\" statisticunits=\"word\">");
		sb.append(approvedWords * 100 / (approvedWords + unapprovedWords));
		sb.append("%");
		sb.append("</status-info>\n");

		sb.append("\t\t<status-info type=\"");
		sb.append(Messages.getString("file.TSFileHandler.total2"));
		sb.append("\" statisticunits=\"word\">");
		sb.append(approvedWords + unapprovedWords);
		sb.append("</status-info>\n");

		sb.append("\t</status-infos>\n");

		return sb.toString();
	}

	protected Map<String, Object> xliffAnalysis(File file, int analysisMode, float eqvFactor) throws ModifyException,
			NavException, TranscodeException, IOException, XPathParseException, XPathEvalException {
		String filepath = file.getAbsolutePath();

		// TODO 确定报告生成的地址及文件名。
		String reportFile = "";
		if (file == null || !file.exists()) {
			String msg = Messages.getString("file.TSFileHandler.logger21");
			logger.error(msg);
			return getErrorResult(msg, null);
		}

		// 判断文件是否已打开并已修改，是则提示是否保存。
		if (filesChangeStatus.get(file.getAbsolutePath())) {
			String msg = MessageFormat.format(Messages.getString("file.TSFileHandler.logger29"), filepath);
			logger.warn(msg);
			Map<String, Object> result = getErrorResult(msg, null);
			result.put("FileChanged", true);
			return result;
		}

		VTDGen vg = new VTDGen();
		if (vg.parseFile(filepath, true)) {
			VTDNav vn = vg.getNav();

			// 创建分析报告结构
			vg.setDoc("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n<hsts-analysisreport>\n</hsts-analysisreport>"
					.getBytes());
			VTDNav vnReport = vg.getNav();
			XMLModifier xm = new XMLModifier(vnReport);

			// 分析状态信息
			if (TSFileHandler.ANALYSIS_MODE_STATUS == (analysisMode & TSFileHandler.ANALYSIS_MODE_STATUS)) {
				String xml = analysisStatus(vn.duplicateNav(), filepath, eqvFactor);
				vnReport.toElement(VTDNav.ROOT);
				xm.insertAfterElement(xml);
			}

			// 分析翻译进度
			if (TSFileHandler.ANALYSIS_MODE_TRANSLATOR_PROGRESS == (analysisMode & TSFileHandler.ANALYSIS_MODE_TRANSLATOR_PROGRESS)) {
				String xml = analysisTranslatorProgress(vn.duplicateNav(), filepath, eqvFactor);
				vnReport.toElement(VTDNav.ROOT);
				xm.insertAfterElement(xml);
			}

			// 分析编辑进度
			if (TSFileHandler.ANALYSIS_MODE_EDITOR_PROGRESS == (analysisMode & TSFileHandler.ANALYSIS_MODE_EDITOR_PROGRESS)) {
				String xml = analysisEditorProgress(vn.duplicateNav(), filepath, eqvFactor);
				vnReport.toElement(VTDNav.ROOT);
				xm.insertAfterElement(xml);
			}

			xm.output(reportFile);
		} else {
			String msg = MessageFormat.format(Messages.getString("file.TSFileHandler.logger28"), filepath);
			logger.error(msg);
			return getErrorResult(msg, null);
		}

		Map<String, Object> result = getSuccessResult();
		return result;
	}

	protected Map<String, Object> projectAnalysis(File file) {

		return null;
	}

	public static int wordCount(String str, String lang) {
		if (lang.toLowerCase().startsWith("zh")) { //$NON-NLS-1$
			return chineseCount(str);
		}
		return europeanCount(str);
	}

	private static int chineseCount(String str) {
		// basic idea is that we need to remove unicode that higher than 255
		// and then we count by europeanCount
		// after that remove 0-255 unicode value and just count character
		StringBuffer european = new StringBuffer();
		int chineseCount = 0;
		char[] chars = str.toCharArray();
		for (int i = 0; i < chars.length; i++) {
			char chr = chars[i];
			if (chr <= 255 || chr == '\u00A0' || chr == '\u3001' || chr == '\u3002' || chr == '\uff1a'
					|| chr == '\uff01' || chr == '\uff1f' || chr == '\u4ecb') {
				european.append(chr);
			} else {
				chineseCount++;
			}
		}
		int euroCount = europeanCount(european.toString());
		return euroCount + chineseCount;
	}

	private static int europeanCount(String source) {
		int wordnum = 0;
		StringTokenizer tok = new StringTokenizer(source, " \t\r\n()?\u00A0\u3001\u3002\uff1a\uff01\uff1f\u4ecb"); //$NON-NLS-1$
		String charsInNumber = ".,-/<>"; //$NON-NLS-1$
		while (tok.hasMoreTokens()) {
			String str = tok.nextToken();
			if (charsInNumber.indexOf(str) < 0 && !isFormatNumber(str)) {
				StringTokenizer tok2 = new StringTokenizer(str, charsInNumber);
				while (tok2.hasMoreTokens()) {
					str = tok2.nextToken();
					wordnum++;
				}
			}
		}

		return wordnum;
	}

	public static boolean isFormatNumber(String str) {
		char[] chars = str.toCharArray();
		boolean hasDigit = false;
		for (int i = 0; i < chars.length; i++) {
			if (Character.isDigit(chars[i])) {
				hasDigit = true;
			} else if (chars[i] != '/' && chars[i] != '.' && chars[i] != ',' && chars[i] != '-' && chars[i] != '>'
					&& chars[i] != '<') {
				return false;
			}
		}
		return hasDigit;
	}

	/**
	 * 获取 XLIFF 文件中所有 file 节点的源语言。
	 * 
	 * @return 返回值即为多个文件节点的源语言集合，按文件节点顺序储。
	 * 			若集合为 null 表示产生错误，集合为空集合串表示无此属性。<br/>
	 * */
	public Vector<String> getSourceLanguage() {
		if(fileAttrs == null)
			return null;
		
		Vector<String> srcLangs = new Vector<String>();
		Iterator<Integer> it = fileAttrs.keySet().iterator();
		while(it.hasNext()){
			Hashtable<String,String> tmpAtts = fileAttrs.get(it.next());
			String srcLang = tmpAtts.get("source-language");
			if(srcLang == null){
				srcLang = "";
			}
			
			srcLangs.add(srcLang);
		}
		
		return srcLangs;
	}

	/**
	 * 获取 XLIFF 文件中所有 file 节点的目标语言。
	 * 
	 * @return 返回值即为多个文件节点的目标语言集合，按文件节点顺序储。
	 * 			若集合为 null 表示产生错误，集合为空集合串表示无此属性。<br/>
	 * */
	public Vector<String> getTargetLanguage() {
		if(fileAttrs == null)
			return null;
		
		Vector<String> tgtLangs = new Vector<String>();
		Iterator<Integer> it = fileAttrs.keySet().iterator();
		while(it.hasNext()){
			Hashtable<String,String> tmpAtts = fileAttrs.get(it.next());
			String tgtLang = tmpAtts.get("target-language");
			if(tgtLang == null){
				tgtLang = "";
			}
			
			tgtLangs.add(tgtLang);
		}
		
		return tgtLangs;
	}

	/**
	 * 获取 XLIFF 文件中指定索引的 file 节点的源语言。
	 * 
	 * @param fileIndex
	 *            文件节点的索引。从 1 开始。
	 * @return 返回值即为指定索引的文件节点的源语言。若值为 null
	 *         表示产生错误，为 “” 空字符串表示无此属性。<br/>
	 * */
	public String getSourceLanguage(int fileIndex) {
		if (fileAttrs == null)
			return null;

		Hashtable<String, String> attrs = fileAttrs.get(fileIndex);
		if (attrs == null)
			return null;
		
		return attrs.get("source-language");
	}

	/**
	 * 获取 XLIFF 文件中指定索引的 file 节点的目标语言。
	 * 
	 * @param fileIndex
	 *            文件节点的索引。从 1 开始。
	 * @return 返回值即为指定索引的文件节点的目标语言。若值为 null 表示产生错误，为 “” 空字符串表示无此属性。<br/>
	 * */
	public String getTargetLanguage(int fileIndex) {
		if (fileAttrs == null)
			return null;

		Hashtable<String, String> attrs = fileAttrs.get(fileIndex);
		if (attrs == null)
			return null;
		
		return attrs.get("target-language");
	}

	public static void main(String[] args) throws XPathParseException, XPathEvalException, NavException {
		TSFileHandler ts = new TSFileHandler();
		VTDGen vg = new VTDGen();
		if (vg
				.parseFile(
						"/data/john/Workspaces/Other/net.heartsome.cat.ts.core/testSrc/net/heartsome/cat/ts/core/file/test/Test.txt.xlf",
						true)) {
			ts.openFile("/data/john/Workspaces/Other/net.heartsome.cat.ts.core/testSrc/net/heartsome/cat/ts/core/file/test/Test.txt.xlf");
			System.out.println(ts.getSourceLanguage(3));
			System.out.println(ts.getTargetLanguage(3));
//			System.out
//					.println(ts
//							.analysisTranslatorProgress(
//									vg.getNav(),
//									"/data/john/Workspaces/Other/net.heartsome.cat.ts.core/testSrc/net/heartsome/cat/ts/core/file/test/Test.txt.xlf",
//									0));
		}

	}
}

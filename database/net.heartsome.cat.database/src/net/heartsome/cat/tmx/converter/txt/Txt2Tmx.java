/**
 * Txt2Tmx.java
 *
 * Version information :
 *
 * Date:2013/5/17
 *
 * Copyright notice :
 * 本文件及其附带的相关文件包含机密信息，仅限瀚特盛科技有限公司指定的，与本项目有关的内部人员和客户联络人员查阅和使用。 
 * 如果您不是本保密声明中指定的收件者，请立即销毁本文件，禁止对本文件或根据本文件中的内容采取任何其他行动， 
 * 包括但不限于：泄露本文件中的信息、以任何方式制作本文件全部或部分内容之副本、将本文件或其相关副本提供给任何其他人。
 */

package net.heartsome.cat.tmx.converter.txt;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import net.heartsome.cat.common.util.FileEncodingDetector;
import net.heartsome.cat.database.resource.Messages;
import net.heartsome.cat.tmx.converter.LanguageUtils;
import net.heartsome.cat.tmx.converter.bean.File2TmxConvertBean;
import net.heartsome.cat.tmx.converter.bean.TmxTemple;

import org.eclipse.core.runtime.IProgressMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ximpleware.AutoPilot;
import com.ximpleware.VTDGen;
import com.ximpleware.VTDNav;

public class Txt2Tmx extends net.heartsome.cat.tmx.converter.AbstractFile2Tmx {

	public static Logger LOGGER = LoggerFactory.getLogger(Txt2Tmx.class.getName());
	private long appendOffset = -1;
	private String appendSrclang = null;

	public void doCovnerter(net.heartsome.cat.tmx.converter.bean.File2TmxConvertBean bean, IProgressMonitor monitor)
			throws Exception {
		monitor.beginTask(Messages.getString("converter.common.monitor.start"), 101);
		monitor.setTaskName("");
		monitor.worked(1);

		FileOutputStream fos = null;
		if (bean.appendExistTmxFilePath != null) {
			fos = new FileOutputStream(new File(bean.appendExistTmxFilePath), true);
			getSrclangAndTuOffset(bean.appendExistTmxFilePath);
			FileChannel fc = fos.getChannel();
			fc.truncate(appendOffset);
		} else {
			fos = new FileOutputStream(new File(bean.newTmxFilePath));
		}
		try {
			writeTmx(bean, fos, monitor);
		} catch (Exception e) {
			try {
				fos.close();
			} catch (Exception e1) {
			}
			throw e;
		}
	}

	private void writeTmx(File2TmxConvertBean bean, FileOutputStream fos, IProgressMonitor monitor) throws Exception {

		monitor.setTaskName(Messages.getString("converter.docx2tmx.docx.readdocx"));
		double total = 0;
		double count = 0;
		int worked = 0;
		int tmp = 0;

		InputStreamReader freader = null;
		try {
			File file = new File(bean.sourceFilePath);
			String encoding = FileEncodingDetector.detectFileEncoding(file);
			freader = new InputStreamReader(new FileInputStream(file), encoding);
			total = getLineNumber(file, encoding);
			if (total < 1) {
				throw new Exception(Messages.getString("converter.common.vaild.langcode.error"));
			}
		} catch (FileNotFoundException e) {
			LOGGER.error("无法读取txt文件", e);
			throw e;
		}

		BufferedReader reader = new BufferedReader(freader);
		BufferedOutputStream bos = new BufferedOutputStream(fos);

		try {
			// 语言代码

			String rowStr = reader.readLine();
			count++;
			String[] rowArr = rowStr.split("\t");
			
			if (rowArr == null || rowArr.length < 2) {
				throw new Exception(Messages.getString("converter.common.vaild.langcode.error"));
			}
			
			Set<String> testSet = new HashSet<String>();
			testSet.addAll(Arrays.asList(rowArr));
			if (testSet.size() != rowArr.length) {
				throw new Exception(Messages.getString("converter.common.vaild.duplicatelangcode.error"));
			}
			
			Map<Integer, String> map = new HashMap<Integer, String>();
			for (int i = 0; i < rowArr.length; i++) {
				// check langcode
				if (rowArr[i].trim().isEmpty()) {
					throw new Exception(Messages.getString("converter.common.vaild.langcode.error"));
				}
				map.put(i, LanguageUtils.convertLangCode(rowArr[i]));
			}

			bean.srcLangCode = rowArr[0];
			if (bean.srcLangCode == null || bean.srcLangCode.isEmpty()) {
				Exception e = new Exception(Messages.getString("converter.docx2tmx.docx.invaild"));
				throw e;
			}
			
			if (bean.appendExistTmxFilePath != null) {
				if (!bean.srcLangCode.equals(appendSrclang)) {
					Exception e = new Exception(Messages.getString("converter.common.appendtmx.diffsrcLang.error"));
					throw e;
				}
			} else {
				bos.write(TmxTemple.getDefaultTmxPrefix(bean.srcLangCode).getBytes());
			}

			String tuContent = null;
			while ((rowStr = reader.readLine()) != null) {
				if (monitor.isCanceled()) {
					return;
				}
				// 设置进度
				count++;
				tmp = (int) ((count / total) * 100);
				if (tmp > worked) {
					monitor.worked(tmp - worked);
					worked = tmp;
				}

				rowArr = rowStr.split("\t");
				tuContent = createTu(rowArr, map, bean);
				bos.write('\r');
				bos.write('\n');
				bos.write(tuContent.getBytes());
			}
			bos.write("</body></tmx>".getBytes());
			bos.flush();
			monitor.done();
		} catch (IOException e) {
			throw e;
		} finally {
			try {
				if (reader != null) {
					reader.close();
				}
				if (bos != null) {
					bos.close();
				}
			} catch (IOException e) {
				LOGGER.error("close reader&writer");
			}
		}
	}

	private double getLineNumber(File file, String encoding) throws UnsupportedEncodingException {
		BufferedReader bis = null;
		try {
			bis = new BufferedReader(new InputStreamReader(new FileInputStream(file), encoding));
		} catch (FileNotFoundException e2) {
			e2.printStackTrace();
		}
		int ch = -1;
		int count = 0;
		boolean isline = false;
		try {
			while ((ch = bis.read()) != -1) {
				if (ch == '\r' || ch == '\n') {
					if (!isline) {
						isline = true;
						count++;
					}
				} else {
					isline = false;
				}
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		} finally {
			try {
				bis.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return count;
	}

	private String createTu(String[] rowArr, Map<Integer, String> map, File2TmxConvertBean bean) {
		StringBuilder builder = new StringBuilder();
		builder.append("<tu>");
		for (int i = 0; i < rowArr.length; i++) {
			addTuPropNode(builder, bean.customeAttr);
			if (rowArr[i].trim().length() > 0) {
				builder.append("<tuv ").append("xml:lang=\"").append(map.get(i)).append("\"").append(">")
						.append("<seg>").append(decode(rowArr[i])).append("</seg>").append("</tuv>");
			}
		}
		builder.append("</tu>");
		return builder.toString();
	}

	private void addTuPropNode(StringBuilder builder, Map<String, String> map) {
		if (map == null || map.size() == 0) {
			return;
		}
		for (Entry<String, String> entry : map.entrySet()) {
			builder.append("<prop type=\"").append(entry.getKey()).append("\">").append(entry.getValue())
					.append("</prop>");
		}
	}

	/**
	 * 获取源语言，计算 tmx 文档的 最后 tu 的偏移，以便附加新内容
	 * @param file
	 * @return
	 */
	private void getSrclangAndTuOffset(String file) throws Exception {
		// 检测追加文件的源语言
		String appendFileSrcLang = "";
		VTDGen vg = new VTDGen();
		if (!vg.parseFile(file, true)) {
			Exception e = new Exception(Messages.getString("tmxdata.TmxFileContainer.parseTmxFileError"));
			LOGGER.error(e.getMessage());
			throw e;
		}
		try {
			VTDNav vn = vg.getNav();
			AutoPilot ap = new AutoPilot(vn);
			ap.selectXPath("/tmx/header/@srclang");
			if (ap.evalXPath() != -1) {
				appendFileSrcLang = vn.toString(vn.getCurrentIndex() + 1);
			} else {
				LOGGER.error(Messages.getString("tmxdata.TmxFileContainer.parseTmxFileContentError"));
			}
			ap.resetXPath();
			ap.selectXPath("/tmx/body/tu[last()]");
			if (ap.evalXPath() != -1) {
				long l = vn.getElementFragment();
				int offset = (int) l;
				int length = (int) (l >> 32);
				appendOffset = offset + length;
			}

		} catch (Exception e) {
			LOGGER.error("程序错误", e);
		}
		appendSrclang = appendFileSrcLang;
	}

	private String decode(String str) {
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < str.length(); i++) {
			switch (str.charAt(i)) {
			case '\\':
				if (i + 1 < str.length()) {
					switch (str.charAt(i + 1)) {
					case 'r':
						i++;
						builder.append('\r');
						continue;
					case 't':
						i++;
						builder.append('\t');
						continue;
					case 'n':
						i++;
						builder.append('\n');
						continue;
					case '\\':
						i++;
						builder.append('\\');
						continue;
					default:
						builder.append('\\');
						continue;
					}
				}
			case '<':
				builder.append("&lt;");
				continue;
			case '>':
				builder.append("&gt;");
				continue;
			case '&':
				builder.append("&amp;");
				break;
			case '\r':
			case '\n':
			case '\t':
				builder.append(str.charAt(i));
				break;
			default:
				if (str.charAt(i) >= 0x20) {
					builder.append(str.charAt(i));
				}
			}
		}
		return builder.toString();
	}
 
	/**
	 * 生成 tmx 头，节点至--&gt;&lt;body&gt;
	 * @param bean
	 * @return
	 */
	public static String newTmxHeader(File2TmxConvertBean bean) {
		StringBuilder builder = new StringBuilder();
		builder.append("<?xml version=\"1.0\" ?>").append("<tmx version=\"1.4\">").append("<header \r\n")
				.append("creationtool=\"HS TMEditor\" \r\n").append("creationtoolversion=\"1.0\" \r\n")
				.append("segtype=\"block\" \r\n").append("o-tmf=\"TW4Win 2.0 Format\" \r\n")
				.append("adminlang=\"en-US\" \r\n").append("srclang=\"").append(bean.srcLangCode).append("\" \r\n")
				.append("datatype=\"txt\" \r\n").append("creationdate=\"").append(System.currentTimeMillis())
				.append("\"></header>\r\n").append("<body>");
		return builder.toString();
	}
}

/**
 * XliffReader.java
 *
 * Version information :
 *
 * Date:2012-8-10
 *
 * Copyright notice :
 * 本文件及其附带的相关文件包含机密信息，仅限瀚特盛科技有限公司指定的，与本项目有关的内部人员和客户联络人员查阅和使用。 
 * 如果您不是本保密声明中指定的收件者，请立即销毁本文件，禁止对本文件或根据本文件中的内容采取任何其他行动， 
 * 包括但不限于：泄露本文件中的信息、以任何方式制作本文件全部或部分内容之副本、将本文件或其相关副本提供给任何其他人。
 */
package net.heartsome.cat.converter.msexcel2007.reader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import net.heartsome.cat.converter.msexcel2007.common.InternalFileException;
import net.heartsome.cat.converter.msexcel2007.common.ZipUtil;
import net.heartsome.cat.converter.msexcel2007.document.Cell;
import net.heartsome.cat.converter.msexcel2007.document.DrawingsPart;
import net.heartsome.cat.converter.msexcel2007.document.HeaderFooter;
import net.heartsome.cat.converter.msexcel2007.document.SheetPart;
import net.heartsome.cat.converter.msexcel2007.document.SpreadsheetDocument;
import net.heartsome.cat.converter.msexcel2007.document.WorkBookPart;
import net.heartsome.cat.converter.msexcel2007.document.drawing.CellAnchor;
import net.heartsome.cat.converter.msexcel2007.document.drawing.ShapeParagraph;
import net.heartsome.cat.converter.msexcel2007.document.drawing.ShapeTxBody;
import net.heartsome.cat.converter.msexcel2007.resource.Messages;
import net.heartsome.xml.vtdimpl.VTDUtils;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ximpleware.AutoPilot;
import com.ximpleware.VTDException;
import com.ximpleware.VTDGen;
import com.ximpleware.VTDNav;

/**
 * @author Jason
 * @version
 * @since JDK1.6
 */
public class XliffReader {
	public static final Logger logger = LoggerFactory.getLogger(XliffReader.class);
	private VTDUtils vu;

	public XliffReader() {

	}

	public void read2SpreadsheetDoc(String spreadsheetDocFile, String xliffFile, String sklFile,
			IProgressMonitor monitor) throws InternalFileException {

		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}

		monitor.beginTask(Messages.getString("msexcel.xliff2mse.task1"), 5);

		loadFile(xliffFile);
		monitor.worked(1);

		monitor.setTaskName(Messages.getString("msexcel.xliff2mse.task2"));

		try {
			SpreadsheetDocument.open(sklFile);
			monitor.worked(1);
			WorkBookPart wb = SpreadsheetDocument.workBookPart;
			List<SheetPart> sheetList = wb.getSheetParts();

			readSheet(sheetList, new SubProgressMonitor(monitor, 1));

			monitor.setTaskName(Messages.getString("msexcel.xliff2mse.task3"));
			wb.save();
			monitor.worked(1);
			try {
				ZipUtil.zipFolder(spreadsheetDocFile, SpreadsheetDocument.spreadsheetPackage.getPackageSuperRoot());
			} catch (IOException e) {
				logger.error("", e);
			}
			monitor.worked(1);
		} finally {
			SpreadsheetDocument.close();
			monitor.done();
		}
	}

	private void readSheet(List<SheetPart> sheetList, IProgressMonitor monitor) throws InternalFileException {
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		monitor.beginTask(Messages.getString("msexcel.xliff2mse.task4"), sheetList.size() * 5);
		List<String> sheetNames = new ArrayList<String>();
		for (SheetPart sp : sheetList) {
			monitor.worked(1);
			String name = sp.getName();
			name = processContent(name);

			// 处理重复名称
			int i = 1;
			while (sheetNames.contains(name)) {
				name += i;
				i++;
			}
			sheetNames.add(name);

			sp.setSheetName(name);

			// read headers
			monitor.worked(1);
			List<HeaderFooter> headers = sp.getHeader();
			for (HeaderFooter hf : headers) {
				String content = hf.getContent();
				content = processContent(content);
				hf.setContent(content);
			}
			sp.setHeaderFooter(headers); // update the file

			monitor.worked(1);
			DrawingsPart drawingp = sp.getDrawingsPart();
			if (drawingp != null) {
				List<CellAnchor> aList = drawingp.getCellAnchorList();
				for (CellAnchor a : aList) {
					List<ShapeTxBody> sList = a.getShapeList();
					if (sList.size() == 0) {
						continue;
					}
					for (ShapeTxBody s : sList) {
						List<ShapeParagraph> pList = s.getTxBodyParagraghList();
						for (ShapeParagraph p : pList) {
							String content = p.getXmlContent();
							content = processContent(content);
							p.setXmlContent(content);
						}
					}
				}
				drawingp.updateDrawingObject();
			}
			monitor.worked(1);
			List<Cell> cellList = sp.getCells("s");
			for (Cell c : cellList) {
				String content = c.getFullContent();
				content = processContent(content);
				c.setShareStringItemFullContent(content);
			}

			monitor.worked(1);
			// read footer
			List<HeaderFooter> footers = sp.getFoolter();
			for (HeaderFooter hf : footers) {
				String content = hf.getContent();
				content = processContent(content);
				hf.setContent(content);
			}
			sp.setHeaderFooter(footers); // update the file
		}
		monitor.done();
	}

	private String processContent(String content) throws InternalFileException {
		String result = content;
		int sos = content.indexOf("%%%");
		while (sos != -1) {
			sos += 3;
			int eos = content.indexOf("%%%", sos);
			String code = content.substring(sos, eos);
			eos += 3;
			String t = getTargetById(code);
			result = result.replace("%%%" + code + "%%%", t);
			sos = content.indexOf("%%%", eos);
		}
		return result;
	}

	public static void main(String[] args) throws InternalFileException {
		XliffReader r = new XliffReader();
		r.read2SpreadsheetDoc("d:\\ot1\\test1.xlsx", "d:\\ot1\\test1_zh-CN.xlsx.hsxliff", "d:\\ot1\\test1.xlsx.skl",
				null);
	}

	private String getTargetById(String id) throws InternalFileException {
		String result = null;
		String xpath = "/xliff/file/body/trans-unit[@id='" + id + "']";
		AutoPilot ap = new AutoPilot(vu.getVTDNav());
		try {
			ap.selectXPath(xpath);
			if (ap.evalXPath() != -1) {
				String text = vu.getChildContent("./target");
				if (text == null) {
					text = vu.getChildContent("./target");
					vu.pilot("./source");
				} else {
					vu.pilot("./target");
				}
				Map<Integer, String[]> anaysisRs = anaysisTarget();
				result = converterAnaysisResult(anaysisRs);
			}
		} catch (VTDException e) {
			logger.error("", e);
		}
		if (result == null) {
			throw new InternalFileException(MessageFormat.format(Messages.getString("msexcel.xliff2mse.msg1"), id));
		}
		return result;
	}

	private String converterAnaysisResult(Map<Integer, String[]> targetMap) {
		List<String[]> list = new ArrayList<String[]>();
		Set<Entry<Integer, String[]>> entrys = targetMap.entrySet();
		for (Entry<Integer, String[]> entry : entrys) {
			list.add(entry.getValue());
		}

		StringBuffer bf = new StringBuffer();
		String tAttr = " xml:space=\"preserve\"";
		for (int i = 0; i < list.size(); i++) {
			String[] val = list.get(i);
			// System.out.print(val[0]);
			String text = val[0] == null ? "" : val[0];
			String style = val[1];
			if (style == null) {
				bf.append(text);
				continue;
			}
			style = ReaderUtil.reCleanAttribute(style);
			String ns = val[2];
			String tagr = "r";
			String tagt = "t";
			if (ns != null && ns.length() != 0) {
				tagr = ns + ":" + tagr;
				tagt = ns + ":" + tagt;
			}

			bf.append('<').append(tagr).append('>').append(style); // <r> <rpr></rpr>
			bf.append('<').append(tagt);
			if(!tagt.equals("a:t")){
				bf.append(tAttr);
			}
			bf.append('>').append(text); // <t>text
			while (i + 1 < list.size() && list.get(i + 1)[2] != null && list.get(i + 1)[2].equals(style)) {
				val = list.get(i + 1);
				text = val[0] == null ? "" : val[0];
				bf.append(text);
				i++;
			}
			bf.append('<').append('/').append(tagt).append('>');
			bf.append('<').append('/').append(tagr).append('>');

		}
		// System.out.println(bf);
		return bf.toString();
	}

	private Map<Integer, String[]> anaysisTarget() throws VTDException {
		Map<Integer, String[]> targetMap = new TreeMap<Integer, String[]>();
		AutoPilot ap = new AutoPilot(vu.getVTDNav());
		ap.selectXPath("./node() | text()");
		while (ap.evalXPath() != -1) {
			int index = vu.getVTDNav().getCurrentIndex();
			int tokenType = vu.getVTDNav().getTokenType(index);
			if (tokenType == 0) {
				ananysisTag(targetMap);
			} else if (tokenType == 5) {
				targetMap.put(index, new String[] { vu.getVTDNav().toRawString(index), null, null });
			}
		}
		return targetMap;
	}

	private void ananysisTag(Map<Integer, String[]> targetMap) throws VTDException {
		VTDNav vn = vu.getVTDNav();
		vn.push();
		int idex = vn.getCurrentIndex();
		String tagName = vn.toString(idex);
		if ("g".equals(tagName)) {
			String ctype = vu.getCurrentElementAttribut("ctype", null);
			String rpr = vu.getCurrentElementAttribut("rPr", "");

			AutoPilot ap = new AutoPilot(vn);
			ap.selectXPath("./node() | text()");
			while (ap.evalXPath() != -1) {
				idex = vn.getCurrentIndex();
				int tokenType = vn.getTokenType(idex);
				if (tokenType == 0) {
					String name = vu.getCurrentElementName();
					if ("ph".equals(name)) {
						targetMap.put(idex, new String[] { vu.getElementContent(), rpr, ctype });
					} else if ("g".equals(name)) {
						ananysisTag(targetMap);
					}
				} else if (tokenType == 5) {
					targetMap.put(idex, new String[] { vn.toRawString(idex), rpr, ctype });
				}
			}
		} else if ("ph".equals(tagName)) {
			targetMap.put(idex, new String[] { vu.getElementContent(), null, null });
		} else { // 其他节点，一律当做字符串处理
			targetMap.put(idex, new String[] { vu.getElementFragment(), null, null });
		}
		vn.pop();
	}

	private void loadFile(String file) throws InternalFileException {
		File f = new File(file);

		VTDGen vg = new VTDGen();
		FileInputStream fis = null;
		byte[] b = new byte[(int) f.length()];
		try {
			fis = new FileInputStream(f);
			fis.read(b);
		} catch (IOException e) {
			throw new InternalFileException(Messages.getString("msexcel.converter.exception.msg1"));
		} finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (Exception e) {
					logger.error("", e);
				}
			}
		}
		vg.setDoc(b);
		try {
			vg.parse(true);
			vu = new VTDUtils(vg.getNav());
		} catch (VTDException e) {
			String message = Messages.getString("msexcel.converter.exception.msg1");
			message += "\nFile:" + f.getName() + "\n" + e.getMessage();
			throw new InternalFileException(message);
		}
	}
}

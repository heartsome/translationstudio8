/**
 * XliffReader.java
 *
 * Version information :
 *
 * Date:2012-8-2
 *
 * Copyright notice :
 */
package net.heartsome.cat.converter.msexcel2007.reader;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.heartsome.cat.converter.StringSegmenter;
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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;

/**
 * @author Jason
 * @version
 * @since JDK1.6
 */
public class SpreadsheetReader {

	private BufferedWriter xlfOs;

	/** The source file language */
	private String srcLang;

	/** The XLiff TransUnit index counter */
	private int xliffIdex;

	/** The text segmentter */
	private StringSegmenter segmenter;

	private List<Integer> filterStyleIndex;

	public SpreadsheetReader(String sourceLanguage, StringSegmenter segmenter) {
		this.srcLang = sourceLanguage;
		this.xliffIdex = 1;
		this.segmenter = segmenter;
		this.filterStyleIndex = new ArrayList<Integer>();
	}

	/**
	 * 将内容读入到XLIFF文件
	 * @param xlfOs
	 *            XLIFF文件BufferWriter
	 * @param filterRedCell
	 *            是否需要过滤标记为红色的单元格
	 * @param monitor
	 * @throws InternalFileException
	 * @throws IOException
	 *             ;
	 */

	public void read2XliffFile(String inputFile, BufferedWriter xlfOs, String sklFilePath, boolean filterRedCell,
			IProgressMonitor monitor) throws InternalFileException, IOException {
		this.xlfOs = xlfOs;
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		monitor.beginTask("", 5);
		try {
			monitor.setTaskName(Messages.getString("msexcel.mse2xliff.task2"));
			// open source file
			SpreadsheetDocument.open(inputFile);
			monitor.worked(1);
			
			monitor.setTaskName(Messages.getString("msexcel.mse2xliff.task3"));
			// load current workbook
			WorkBookPart wb = SpreadsheetDocument.workBookPart;
			if (filterRedCell) {
				this.filterStyleIndex = wb.getStylesPart().getFilterCellStyle();
			}
			
			// read worksheet
			List<SheetPart> sheetList = wb.getSheetParts();
			readSheets(sheetList, new SubProgressMonitor(monitor, 2));

			// save current document
			wb.save();
			monitor.worked(1);
			
			monitor.setTaskName(Messages.getString("msexcel.mse2xliff.task4"));
			// generate skl file
			saveSklFile(sklFilePath);
			monitor.worked(1);
		} finally {
			SpreadsheetDocument.close();
		}
		monitor.done();
	}

	private void saveSklFile(String sklPath) {
		try {
			ZipUtil.zipFolder(sklPath, SpreadsheetDocument.spreadsheetPackage.getPackageSuperRoot());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void readSheets(List<SheetPart> sheetList, IProgressMonitor monitor)
			throws InternalFileException, IOException {
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		monitor.beginTask("", sheetList.size() * 5);
		for (SheetPart sp : sheetList) {

			// process sheet name
			String name = sp.getName();
			writeXliffFile(xliffIdex, name);
			sp.setSheetName("%%%" + xliffIdex + "%%%");
			xliffIdex++;
			monitor.worked(1);

			// read headers
			List<HeaderFooter> headers = sp.getHeader();
			readHeaderFooters(headers);
			sp.setHeaderFooter(headers); // update the file
			monitor.worked(1);

			// read drawing part
			DrawingsPart drawingp = sp.getDrawingsPart();
			if (drawingp != null) {
				readDrawings(drawingp);
				drawingp.updateDrawingObject();
			}
			monitor.worked(1);

			List<Cell> cellList = sp.getCells("s");
			for (Cell c : cellList) {
				if (filterStyleIndex.contains(c.getStyleIndex())) {
					// skip this cell
					continue;
				}
				String cellText = c.getValue();
				if(cellText == null || cellText.trim().length() == 0){
					// ignore the cell
					continue;
				}
				List<Object[]> cellStyle = c.getCellCharacterStyles();
				String[] segs = segmenter.segment(cellText);
				StringBuffer bf = new StringBuffer();
				for (String seg : segs) {
					List<Object[]> s = ReaderUtil.getSegStyle(cellStyle, seg, cellText);
					if (s.size() != 0) {
						seg = ReaderUtil.appendSegStyle(seg, s);
					}
					bf.append("%%%").append(xliffIdex++).append("%%%");
					// System.out.println(xliffIdex + ":" + seg);
					writeXliffFile(xliffIdex - 1, seg);
				}
				c.setValue(bf.toString());
			}
			monitor.worked(1);
			
			// read headers
			List<HeaderFooter> footers = sp.getFoolter();
			readHeaderFooters(footers);
			sp.setHeaderFooter(footers); // update the file
			
			monitor.worked(1);
		}
		monitor.done();
	}

	private void readDrawings(DrawingsPart drawingp) throws IOException {
		List<CellAnchor> aList = drawingp.getCellAnchorList();
		for (CellAnchor a : aList) {
			List<ShapeTxBody> sList = a.getShapeList();
			if (sList.size() == 0) {
				continue;
			}
			for (ShapeTxBody s : sList) {
				List<ShapeParagraph> pList = s.getTxBodyParagraghList();
				for (ShapeParagraph p : pList) {
					String txt = p.getParagraghText();
					if (txt.length() == 0) {
						continue;
					}
					List<Object[]> txtStyle = p.getParagraghCharacterStyle();

					String[] segs = segmenter.segment(txt);
					StringBuffer bf = new StringBuffer();
					for (String seg : segs) {
						if (txtStyle.size() > 1) {
							List<Object[]> style = ReaderUtil.getSegStyle(txtStyle, seg, txt);
							if (style.size() != 0) {
								seg = ReaderUtil.appendSegStyle(seg, style);
							}
						}
						bf.append("%%%").append(xliffIdex).append("%%%");
						writeXliffFile(xliffIdex, seg);
						xliffIdex++;
					}
					// System.out.println(bf.toString());
					p.setParagraghText(bf.toString());
				}
			}
		}
	}

	private void readHeaderFooters(List<HeaderFooter> headerFooters) throws IOException {
		for (HeaderFooter hf : headerFooters) {
			String content = ReaderUtil.reCleanTag(hf.getContent());
			String[] lcr = splitHeaderFooter(content);
			StringBuffer skl = new StringBuffer();
			for (String s : lcr) {
				if (s.length() == 0) {
					continue;
				}
				StringBuffer text = new StringBuffer();
				readSplitedContent(s, skl, text);
				if (text.length() == 0) {
					continue;
				}
				writeXliffFile(xliffIdex - 1, text.toString());
			}
			hf.setContent(ReaderUtil.cleanTag(skl.toString()));
		}
	}

	private void readSplitedContent(String s, StringBuffer skl, StringBuffer text) {
		char[] array = s.toCharArray();
		int p = 0;
		for (int i = 0; i < array.length; i++) {
			char ch = array[i];
			if (ch == '&') {
				i++;
				if (array[i] == '"') {
					i++;
					while (array[i] != '"') {
						i++;
					}
				}
			} else {
				p = i;
				break;
			}
		}
		if (p == 0) {
			skl.append(s);
			return;
		}
		skl.append(s.substring(0, p)).append("%%%").append(xliffIdex++).append("%%%");
		String temp = s.substring(p);
		int ep = 0;
		int tp = temp.indexOf('&');
		if (tp != -1) {
			do {
				text.append(temp.substring(ep, tp));
				if (temp.charAt(tp + 1) == '"') {
					int tpp = temp.indexOf('"', tp + 2);
					String t1 = temp.substring(tp, tpp + 1);
					text.append("<ph>").append(ReaderUtil.cleanTag(t1)).append("</ph>");
					ep = tpp + 1;
				} else {
					String t1 = temp.substring(tp, tp + 2);
					if ((tp + 2) == temp.length()) {
						skl.append(t1);
						break;
					} else {
						text.append("<ph>").append(ReaderUtil.cleanTag(t1)).append("</ph>");
						ep = tp + 2;
					}
				}
				int tpp = temp.indexOf('&', tp + 2);
				if (tpp == -1 && (tp + 2) != temp.length()) {
					text.append(temp.substring(ep));
					break;
				}
				tp = tpp;
			} while (tp != -1);
		} else {
			text.append(temp);
		}
	}

	private String[] splitHeaderFooter(String c) {
		String left = "";
		String center = "";
		String right = "";
		int lp = c.indexOf("&L");
		int cp = c.indexOf("&C");
		int rp = c.indexOf("&R");
		if (lp != -1) {
			if (cp != -1) {
				left = c.substring(lp, cp);
			} else if (rp != -1) {
				left = c.substring(lp, rp);
			} else {
				left = c;
			}
		}
		if (cp != -1) {
			if (rp != -1) {
				center = c.substring(cp, rp);
			} else {
				center = c.substring(cp);
			}
		}
		if (rp != -1) {
			right = c.substring(rp);
		}

		String[] result = new String[3];
		result[0] = left;
		result[1] = center;
		result[2] = right;
		return result;
	}

	private void writeXliffFile(int tuId, String sourceSeg) throws IOException {
		xlfOs.write("   <trans-unit id=\"" + tuId++ + "\" xml:space=\"preserve\">\n" + "      <source xml:lang=\""
				+ srcLang + "\">" + sourceSeg + "</source>\n" + "   </trans-unit>\n");
		// System.out.println(tuId + ":" + sourceSeg);
	}
}

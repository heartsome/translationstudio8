/**
 * Excel2007Writer.java
 *
 * Version information :
 *
 * Date:2013-7-8
 *
 * Copyright notice :
 * 本文件及其附带的相关文件包含机密信息，仅限瀚特盛科技有限公司指定的，与本项目有关的内部人员和客户联络人员查阅和使用。 
 * 如果您不是本保密声明中指定的收件者，请立即销毁本文件，禁止对本文件或根据本文件中的内容采取任何其他行动， 
 * 包括但不限于：泄露本文件中的信息、以任何方式制作本文件全部或部分内容之副本、将本文件或其相关副本提供给任何其他人。
 */
package net.heartsome.cat.tmx.converter.xlsx;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.heartsome.cat.common.bean.TmxSegement;
import net.heartsome.cat.common.bean.TmxTU;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author yule
 * @version
 * @since JDK1.6
 */
public class Excel2007Writer {
	private static final Logger LOGGER = LoggerFactory.getLogger(Tmx2xlsx.class);
	private Workbook wb;

	private FileOutputStream out;

	private int lastIndex = 1;

	private CreationHelper createHelper;

	private Sheet sh;
	private Row rowHeader;

	public List<CellStyle> cellStyle_Cache;

	/**
	 * 
	 */
	public Excel2007Writer(File file, int cache_size) {
		wb = new SXSSFWorkbook(cache_size);
		try {
			out = new FileOutputStream(file);
		} catch (FileNotFoundException e) {
			LOGGER.error("", e);
		}
		createHelper = wb.getCreationHelper();
		sh = wb.createSheet();

		rowHeader = sh.createRow(0);
	}

	public void witerTmxTU(List<TmxTU> tus, boolean writeFullText) throws IOException {

		if (null == tus || tus.isEmpty()) {
			return;
		}
		TmxTU tu = null;
		List<TmxSegement> temp = new ArrayList<TmxSegement>(5);
		int i = 0;
		for (; i < tus.size(); i++) {
			temp.clear();
			tu = tus.get(i);
			if (tu.getSource() != null) {
				temp.add(tu.getSource());
			}

			if (tu.getSegments() != null) {
				temp.addAll(tu.getSegments());
			}
			Row row = sh.createRow((lastIndex + i));

			// 为每一行添加数据
			for (TmxSegement segment : temp) {
				int cellIndex = getLangIndex(rowHeader, segment);
				if (-1 == cellIndex) {
					cellIndex = addLangCell(rowHeader, segment);
				}
				RichTextString createRichTextString = null;
				if (writeFullText) {
					createRichTextString = createHelper.createRichTextString(segment.getFullText());
				} else {
					createRichTextString = createHelper.createRichTextString(segment.getPureText());
				}
				Cell createCell = row.createCell(cellIndex);
				createCell.setCellStyle(getWrapedCell());
				createCell.setCellValue(createRichTextString);
			}
		}
		lastIndex = lastIndex + i;
		// :使用固定的列宽
		// 设置宽度:此处比较耗时
		// for(Cell cell : rowHeader){
		// sheet.autoSizeColumn(cell.getColumnIndex());
		// }
		tus.clear();
	}

	public void outZip() {
		try {
			wb.write(out);
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		((SXSSFWorkbook) wb).dispose();
	}

	private int getLangIndex(Row header, TmxSegement segment) {
		for (Cell cell : header) {
			if (segment.getLangCode().equalsIgnoreCase(cell.toString())) {
				return cell.getColumnIndex();
			}
		}
		return -1;
	}

	private CellStyle getWrapedCell() {
		if (cellStyle_Cache == null) {
			cellStyle_Cache = new ArrayList<CellStyle>();
		}
		if (cellStyle_Cache.isEmpty()) {
			final CellStyle cellStyle = wb.createCellStyle();
			cellStyle.setWrapText(true);

		/*	Display.getDefault().syncExec(new Runnable() {
				public void run() {
					org.eclipse.swt.graphics.Font font = JFaceResources.getFontRegistry().get(
							"net.heartsome.cat.te.ui.tmxeditor.font");
					if (null != font) {
						Font createFont = wb.createFont();
						createFont.setFontName(font.getFontData()[0].getName());
						createFont.setFontHeightInPoints((short) font.getFontData()[0].getHeight());
						cellStyle.setFont(createFont);
					}
				}
			});*/
			cellStyle_Cache.add(cellStyle);
		}
		return cellStyle_Cache.get(0);
	}

	private int addLangCell(Row header, TmxSegement segment) {
		int CellNum = header.getLastCellNum();
		if (-1 == CellNum) {
			CellNum = 0;
		}
		Cell createCell = header.createCell(CellNum);
		//CellStyle cellStyle = wb.createCellStyle();
		//XSSFFont headerFont = (XSSFFont) wb.createFont();
		//headerFont.setBold(true);
//		cellStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
//		cellStyle.setFillPattern(XSSFCellStyle.SOLID_FOREGROUND);
//		cellStyle.setFont(headerFont);
		createCell.setCellValue(segment.getLangCode());
//		createCell.setCellStyle(cellStyle);
//		sh.setColumnWidth(CellNum, (100 * 7 + 5) / 7 * 256);
		return CellNum;
	}
}

/**
 * XlsxReader.java
 *
 * Version information :
 *
 * Date:2013-12-16
 *
 * Copyright notice :
 * 本文件及其附带的相关文件包含机密信息，仅限瀚特盛科技有限公司指定的，与本项目有关的内部人员和客户联络人员查阅和使用。 
 * 如果您不是本保密声明中指定的收件者，请立即销毁本文件，禁止对本文件或根据本文件中的内容采取任何其他行动， 
 * 包括但不限于：泄露本文件中的信息、以任何方式制作本文件全部或部分内容之副本、将本文件或其相关副本提供给任何其他人。
 */
package net.heartsome.cat.document.xlsx;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackageAccess;
import org.apache.poi.xssf.eventusermodel.ReadOnlySharedStringsTable;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/**
 * @author Jason
 * @version 1.0
 * @since JDK1.6
 */
public class XlsxRowReader {
	private String xlsxFile;
	private int cacheSize;
	private List<Row> rowCache;
	private IRowHandler rowsHandler;
	/**
	 * 构造器
	 * @param xlsxFile
	 *            文件路径
	 * @param cacheSize
	 *            一次缓存的行数据
	 * @param handler
	 *            缓存满后，对缓存中的数据进行处理
	 */
	public XlsxRowReader(String xlsxFile, int cacheSize, IRowHandler handler) {
		rowCache = new ArrayList<Row>();
		this.xlsxFile = xlsxFile;
		this.cacheSize = cacheSize;
		this.rowsHandler = handler;
	}

	public void readRows(IProgressMonitor monitor) throws ParserConfigurationException, SAXException, IOException, OpenXML4JException {
		monitor.beginTask("", 10);
		monitor.worked(1);
		OPCPackage p = OPCPackage.open(xlsxFile, PackageAccess.READ);
		ReadOnlySharedStringsTable shareString = new ReadOnlySharedStringsTable(p);
		XSSFReader xssfReader = new XSSFReader(p);
		XSSFReader.SheetIterator iter = (XSSFReader.SheetIterator) xssfReader.getSheetsData();
		try {
			while (iter.hasNext()) {
				InputStream stream = iter.next();
				readCells(stream, shareString, new SubProgressMonitor(monitor, 9));
				stream.close();
				// 目前只处理第一个sheet
				break;
			}
		} finally {
			p.close();
			monitor.done();
		}
	}

	private void readCells(InputStream sheetInputStream, ReadOnlySharedStringsTable sharedStringsTable, IProgressMonitor monitor)
			throws ParserConfigurationException, SAXException, IOException {
		InputSource sheetSource = new InputSource(sheetInputStream);
		SAXParserFactory saxFactory = SAXParserFactory.newInstance();
		SAXParser saxParser = saxFactory.newSAXParser();
		XMLReader sheetParser = saxParser.getXMLReader();
		ContentHandler handler = new XSSFHander(sharedStringsTable, monitor);
		sheetParser.setContentHandler(handler);
		sheetParser.parse(sheetSource);
		rowsHandler.handleRows(rowCache);
		rowCache.clear();
	}

	class XSSFHander extends DefaultHandler {

		private final Pattern NUMBER = Pattern.compile("[0-9]");
		private String lastElementName;
		private boolean isSheredString;
		private StringBuilder lastCellContent = new StringBuilder();
		private ReadOnlySharedStringsTable sharedStringsTable;
		private Row cRow;
		private Cell cCell;
		
		private IProgressMonitor monitor;
		
		public XSSFHander(ReadOnlySharedStringsTable sharedStringsTable, IProgressMonitor  monitor) {
			this.sharedStringsTable = sharedStringsTable;
			this.monitor = monitor;
		}

		@Override
		public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
			lastElementName = qName;
			if("dimension".equals(qName)){
				String v = attributes.getValue("ref");
				String[] vs = v.split(":");
				if(vs.length == 2){
					String trs = vs[1].replaceAll("[a-zA-Z]", "");
					try{
						int tr = Integer.parseInt(trs);
						monitor.beginTask("", tr);
					}catch (NumberFormatException e) {
						int m = (int) (new File(xlsxFile).length() / (1024 * 1024));
						if (m == 0) {
							m = 1;
						}
						m = m * 4;
						monitor.beginTask("", m);
					}
				}
			}
			// 开始读取一行数据
			if ("row".equals(qName)) {
				String currRow = attributes.getValue("r");
				cRow = new Row();
				cRow.setRowNumber(Integer.parseInt(currRow));
			}
			if ("c".equals(qName)) {
				String value = attributes.getValue("t");
				if ("inlineStr".equals(value)) {
					isSheredString = false;
				} else {
					isSheredString = true;
				}
				cCell = new Cell();
				cCell.setCellNumber(getCellPosition(attributes.getValue("r")));
				cRow.addCell(cCell);
			}
		}

		/**
		 * (non-Javadoc)
		 * @see org.xml.sax.helpers.DefaultHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
		 */
		@Override
		public void endElement(String uri, String localName, String qName) throws SAXException {
			// 处理excel的行
			if ("row".equals(qName)) {
				if (cacheSize > rowCache.size() && cRow.getCells() != null) {
					rowCache.add(cRow);
				} else {
					rowsHandler.handleRows(rowCache);
					rowCache.clear();
				}
				lastElementName = null;
				if (monitor.isCanceled()) {
					throw new OperationCanceledException();
				}
				monitor.worked(1);
			} else if ("t".equals(qName) && !isSheredString) {
				cCell.setCellConentent(lastCellContent.toString().trim());
				lastCellContent.delete(0, lastCellContent.length());
			} else if ("v".equals(qName)) {
				int idx = -1;
				try {
					idx = Integer.parseInt(lastCellContent.toString().trim());
					XSSFRichTextString rtss = new XSSFRichTextString(sharedStringsTable.getEntryAt(idx));
					cCell.setCellConentent(rtss.toString());
					lastCellContent.delete(0, lastCellContent.length());
				} catch (NumberFormatException e) {
				}
			}
		}

		/**
		 * (non-Javadoc)
		 * @see org.xml.sax.helpers.DefaultHandler#characters(char[], int, int)
		 */
		@Override
		public void characters(char[] ch, int start, int length) throws SAXException {
			if ("v".equals(lastElementName) || ("t".equals(lastElementName) && !isSheredString)) {
				lastCellContent.append(ch, start, length);
			}
		}

		private String getCellPosition(String cellNumber) {
			return NUMBER.matcher(cellNumber).replaceAll("");
		}
	}
}

/**
 * Xlsx2Tmx2.java
 *
 * Version information :
 *
 * Date:2013-7-16
 *
 * Copyright notice :
 * 本文件及其附带的相关文件包含机密信息，仅限瀚特盛科技有限公司指定的，与本项目有关的内部人员和客户联络人员查阅和使用。 
 * 如果您不是本保密声明中指定的收件者，请立即销毁本文件，禁止对本文件或根据本文件中的内容采取任何其他行动， 
 * 包括但不限于：泄露本文件中的信息、以任何方式制作本文件全部或部分内容之副本、将本文件或其相关副本提供给任何其他人。
 */
package net.heartsome.cat.tmx.converter.xlsx;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import net.heartsome.cat.common.bean.TmxTU;
import net.heartsome.cat.common.locale.Language;
import net.heartsome.cat.common.locale.LocaleService;
import net.heartsome.cat.tmx.converter.AbstractWriter;
import net.heartsome.cat.tmx.converter.LanguageUtils;

import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackageAccess;
import org.apache.poi.xssf.eventusermodel.ReadOnlySharedStringsTable;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.eclipse.core.runtime.IProgressMonitor;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/**
 * 采用sax解析xml流
 * @author yule
 * @version
 * @since JDK1.6
 */
public class Xlsx2TmxHelper {

	private AbstractWriter tmxWriter;

	private List<TmxTU> cache = new ArrayList<TmxTU>(cache_size);

	private static final int cache_size = 500;

	// KEY :列位置 (A,B ,C) ，value :语言代码（en-US）
	private Map<String, String> langCodes = new HashMap<String, String>();

	private IProgressMonitor monitor;

	public String getSrcLang() {
		return langCodes.get("A");
	}

	public void parseXlsxFileAndWriteTmxBody(String fileName, AbstractWriter tmxWriter, IProgressMonitor monitor)
			throws ParserConfigurationException, SAXException, IOException, OpenXML4JException {
		this.tmxWriter = tmxWriter;
		this.monitor = monitor;
		File file = new File(fileName);
		long length = file.length();
		monitor.beginTask("", countTotal(length));
		OPCPackage p = OPCPackage.open(fileName, PackageAccess.READ);
		ReadOnlySharedStringsTable strings = new ReadOnlySharedStringsTable(p);
		XSSFReader xssfReader = new XSSFReader(p);
		XSSFReader.SheetIterator iter = (XSSFReader.SheetIterator) xssfReader.getSheetsData();
		try {
			while (iter.hasNext()) {
				InputStream stream = iter.next();
				parse(stream, strings, tmxWriter);
				stream.close();
				// 目前只处理第一个sheet
				break;
			}
		} finally {
			p.close();
		}

		monitor.done();
	}

	public int countTotal(long fileLength) {
		int m = (int) (fileLength / (1024 * 1024));
		if (m == 0) {
			return 1;
		}
		return m * 4;
	}

	public void parse(InputStream sheetInputStream, ReadOnlySharedStringsTable sharedStringsTable,
			AbstractWriter tmxWriter) throws ParserConfigurationException, SAXException, IOException {
		InputSource sheetSource = new InputSource(sheetInputStream);
		SAXParserFactory saxFactory = SAXParserFactory.newInstance();
		SAXParser saxParser = saxFactory.newSAXParser();
		XMLReader sheetParser = saxParser.getXMLReader();
		ContentHandler handler = new XSSFHander(sharedStringsTable);
		sheetParser.setContentHandler(handler);
		sheetParser.parse(sheetSource);		
		if(langCodes.isEmpty()){
			throw new SAXException("EMPTY-LANG-CODE");
		}
		writeEnd();
		
	}

	class XSSFHander extends DefaultHandler {

		private final Pattern NUMBER = Pattern.compile("[0-9]");

		private String lastElementName;

		private boolean isSheredString;

		private StringBuilder lastCellContent = new StringBuilder();

		private ReadOnlySharedStringsTable sharedStringsTable;

		private Row cRow;

		private Cell cCell;

		/**
		 * 每一列的语言代码
		 */

		/**
		 * 
		 */
		public XSSFHander(ReadOnlySharedStringsTable sharedStringsTable) {
			this.sharedStringsTable = sharedStringsTable;

		}

		/**
		 * (non-Javadoc)
		 * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String, java.lang.String, java.lang.String,
		 *      org.xml.sax.Attributes)
		 */
		@Override
		public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
			lastElementName = qName;
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
				// TODO :添加所有列的语言
				//if ("A".equals(cCell.getCellNumber()) || "B".equals(cCell.getCellNumber())) {
					cRow.addCell(cCell);
				//}
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
				// 如果为第一行不添加到缓存中
				if (cRow.getRowNumber() == 1) {
					if (!validateLangs(cRow)) {
						throw new SAXException("LANG-CODE-ERORR");
					}
					if(isHasDuplicateLangCode()){
						throw new SAXException("DUPLICATE-LANG-CODE-ERORR");
					}
					
					if (!validateAppend()) {
						throw new SAXException("DIFF--SRC-LANG-CODE");
					}
					tmxWriter.writeHeader(getSrcLang());
				} else {
					if (cache_size > cache.size()) {
						if (cRow.getCells() != null) {
							cache.add(cRow.toTmxTu());
						}
					} else {
						if (monitor.isCanceled()) {
							throw new SAXException("");
						}
						writeTmxTU(cache, tmxWriter);
						monitor.worked(1);
					}
				}

				lastElementName = null;
			}
			if (!isSheredString) {
				if ("t".equals(qName)) {
					cCell.setCellConentent(lastCellContent.toString().trim());
					cCell.setLangCode(langCodes.get(cCell.getCellNumber()));
					lastCellContent.delete(0, lastCellContent.length());
				}
			} else {
				if ("v".equals(qName)) {
					int idx = -1;
					try {
						idx = Integer.parseInt(lastCellContent.toString().trim());
						XSSFRichTextString rtss = new XSSFRichTextString(sharedStringsTable.getEntryAt(idx));
						cCell.setCellConentent(rtss.toString());
						cCell.setLangCode(langCodes.get(cCell.getCellNumber()));
						lastCellContent.delete(0, lastCellContent.length());
					} catch (NumberFormatException e) {

					}

				}
			}
		}

		/**
		 * (non-Javadoc)
		 * @see org.xml.sax.helpers.DefaultHandler#characters(char[], int, int)
		 */
		@Override
		public void characters(char[] ch, int start, int length) throws SAXException {
			if (!isSheredString) {
				if ("t".equals(lastElementName)) {
					lastCellContent.append(ch, start, length);
				}
			} else {
				if ("v".equals(lastElementName)) {
					lastCellContent.append(ch, start, length);
				}
			}
		}

		public boolean validateLangs(Row row) {

			String lang = null;
			if (null == row || row.getCells() == null) {
				return false;
			}
			for (Cell cell : row.getCells()) {
				lang = cell.getCellConentent();
				String cellPostion = cell.getCellNumber();
				// TODO 初始化语言代码
				if (null != lang) {
					langCodes.put(cellPostion, lang);
				}

			}
			if (langCodes.isEmpty()) {
				return false;
			}

			// for test
			// 验证语言代码
			Map<String, Language> defaultLanguage = LocaleService.getDefaultLanguage();
			Collection<String> values = langCodes.values();
			for (String value : values) {
				String convertLangCode = LanguageUtils.convertLangCode(value);
				if (defaultLanguage.get(convertLangCode) == null) {
					return false;
				}
			}
			return true;
		}

		public boolean isHasDuplicateLangCode(){
			Collection<String> values = langCodes.values();
			Set<String> temp = new HashSet<String>();
			for (String value : values) {
				String convertLangCode = LanguageUtils.convertLangCode(value);
				temp.add(convertLangCode);
			}
			if(values.size()!=temp.size()){
				return true;
			}
			return false;
			
		}
		
		private String getCellPosition(String cellNumber) {
			return NUMBER.matcher(cellNumber).replaceAll("");
		}
	}

	private void writeTmxTU(List<TmxTU> cache, AbstractWriter tmxWriter) {
		if (cache.isEmpty()) {
			return;
		}
		tmxWriter.writeBody(cache);
		cache.clear();
	}

	private boolean validateAppend() {
		/*
		 * if(tmxWriter instanceof AppendTmxWriter){ AppendTmxWriter appendTmxWriter = (AppendTmxWriter) tmxWriter;
		 * if(appendTmxWriter.canAppend(getSrcLang())){ appendTmxWriter.startAppend(); return true; }else{ return false;
		 * }
		 * 
		 * }
		 */
		return true;
	}

	public void writeEnd() {
		writeTmxTU(cache, tmxWriter);
		monitor.done();
	}

}

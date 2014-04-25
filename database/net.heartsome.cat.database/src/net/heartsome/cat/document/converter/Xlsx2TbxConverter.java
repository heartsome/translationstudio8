/**
 * Xlsx2Tbx.java
 *
 * Version information :
 *
 * Date:2013-12-17
 *
 * Copyright notice :
 * 本文件及其附带的相关文件包含机密信息，仅限瀚特盛科技有限公司指定的，与本项目有关的内部人员和客户联络人员查阅和使用。 
 * 如果您不是本保密声明中指定的收件者，请立即销毁本文件，禁止对本文件或根据本文件中的内容采取任何其他行动， 
 * 包括但不限于：泄露本文件中的信息、以任何方式制作本文件全部或部分内容之副本、将本文件或其相关副本提供给任何其他人。
 */
package net.heartsome.cat.document.converter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;

import net.heartsome.cat.common.locale.Language;
import net.heartsome.cat.common.locale.LocaleService;
import net.heartsome.cat.database.resource.Messages;
import net.heartsome.cat.document.xlsx.Cell;
import net.heartsome.cat.document.xlsx.IRowHandler;
import net.heartsome.cat.document.xlsx.Row;
import net.heartsome.cat.document.xlsx.XlsxRowReader;
import net.heartsome.cat.tmx.converter.LanguageUtils;

import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

/**
 * @author Jason
 * @version
 * @since JDK1.6
 */
public class Xlsx2TbxConverter extends AbstractConverter {
	public Logger LOGGER = LoggerFactory.getLogger(Xlsx2TbxConverter.class);
	private String xlsxFile;
	private FileOutputStream out;

	public Xlsx2TbxConverter(String xlsxFile) {
		this.xlsxFile = xlsxFile;
	}

	@Override
	public void doConvert(String targetFile, IProgressMonitor monitor) throws Exception {
		try {
			out = new FileOutputStream(new File(targetFile));
			XlsxRowReader reader = new XlsxRowReader(this.xlsxFile, 20, handler);
			try {
				reader.readRows(monitor);
			} catch (ParserConfigurationException e) {
				LOGGER.error("", e);
				throw new Exception(Messages.getString("converter.xlsx2tmx.parseExcle.error"));
			} catch (SAXException e) {
				LOGGER.error("", e);
				if ("LANG-CODE-ERORR".equals(e.getMessage())) {
					throw new Exception(Messages.getString("converter.common.vaild.langcode.error"));
				} else if ("DIFF--SRC-LANG-CODE".equals(e.getMessage())) {
					throw new Exception(Messages.getString("converter.common.appendtmx.diffsrcLang.error"));
				} else if ("EMPTY-LANG-CODE".equals(e.getMessage())) {
					throw new Exception(Messages.getString("converter.common.vaild.langcode.error"));
				} else if ("DUPLICATE-LANG-CODE-ERORR".equals(e.getMessage())) {
					throw new Exception(Messages.getString("converter.common.vaild.duplicatelangcode.error"));
				}
			} catch (IOException e) {
				LOGGER.error("", e);
				throw new Exception(Messages.getString("converter.xlsx2tmx.parseExcle.error"));
			} catch (OpenXML4JException e) {
				LOGGER.error("", e);
				throw new Exception(Messages.getString("converter.xlsx2tmx.parseExcle.error"));
			}
			String s = generateTbxEnd();
			if(s != null && s.length() != 0){
				writeString(s);
			}
		} catch (FileNotFoundException e) {
			LOGGER.error("", e);
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
				}
			}
		}
	}

	private void writeString(String string) {
		try {
			out.write(string.getBytes("utf-8"));
		} catch (UnsupportedEncodingException e) {
			LOGGER.error("", e);
		} catch (IOException e) {
			LOGGER.error("", e);
		}
	}

	private IRowHandler handler = new IRowHandler() {
		private boolean flg = true;
		/** key-value 列号-表头值 */
		private Map<String, String> headerMap = new HashMap<String, String>();

		public void handleRows(List<Row> rows) throws SAXException {
			if (rows == null || rows.size() == 0) {
				return;
			}
			if (flg) {
				Row headerR = rows.remove(0);
				if (!processHeader(headerR)) {
					throw new SAXException("LANG-CODE-ERORR");
				}
				if (!validateHeader()) {
					throw new SAXException("LANG-CODE-ERORR");
				}
				if (!validateDuplicateHeader()) {
					throw new SAXException("DUPLICATE-LANG-CODE-ERORR");
				}
				flg = false;
				String s = generateTbxHeader(headerR.getCells().get(0).getCellConentent());
				if(s != null && s.length() != 0){
					writeString(s);
				}
			}
			for (Row r : rows) {
				List<Cell> cells = r.getCells();
				if (cells.size() < 2) {
					continue;
				}
				StringBuilder sb = new StringBuilder();
				for (Cell c : cells) {
					String lang = headerMap.get(c.getCellNumber());
					if (lang == null || lang.length() == 0) {
						continue;
					}
					sb.append("<langSet id=\"_" + (System.currentTimeMillis() + 1) + "\" xml:lang=\"" + lang + "\">\n");
					sb.append("<tig>\n");
					sb.append("<term>" + c.getCellConentent().trim() + "</term>\n");
					sb.append("</tig>\n");
					sb.append("</langSet>\n");
				}
				if (sb.length() != 0) {
					writeString("<termEntry id=\"_" + System.currentTimeMillis() + "\">\n");
					writeString(sb.toString());
					writeString("</termEntry>\n");
				}
			}
		}

		/**
		 * 解析第一行数据
		 * @param headerR
		 * @return ;
		 */
		private boolean processHeader(Row headerR) {
			String lang = null;
			if (null == headerR || headerR.getCells() == null) {
				return false;
			}
			for (Cell cell : headerR.getCells()) {
				lang = cell.getCellConentent();
				String cellPostion = cell.getCellNumber();
				lang = LanguageUtils.convertLangCode(lang);
				if (null != lang) {
					headerMap.put(cellPostion, lang);
				}
			}
			if (headerMap.size() < 2) {
				return false;
			}
			return true;
		}

		/**
		 * 验证第一行的数据是否符合要求
		 * @return ;
		 */
		private boolean validateHeader() {
			if (headerMap == null || headerMap.size() == 0) {
				return false;
			}
			Map<String, Language> defaultLanguage = LocaleService.getDefaultLanguage();
			Collection<String> values = headerMap.values();
			for (String value : values) {
				if (defaultLanguage.get(value) == null) {
					return false;
				}
			}
			return true;
		}

		/**
		 * 验证第一行的数据是否重复
		 * @return ;
		 */
		private boolean validateDuplicateHeader() {
			if (headerMap == null || headerMap.size() == 0) {
				return false;
			}
			Collection<String> values = headerMap.values();
			Set<String> temp = new HashSet<String>();
 			for (String value : values) {
				temp.add(value);
			}
			if (values.size() != temp.size()) {
				return false;
			}
			return true;
		}
	};

}

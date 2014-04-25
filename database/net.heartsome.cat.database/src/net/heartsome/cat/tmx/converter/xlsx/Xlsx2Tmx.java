/**
 * Xlsx2Tmx.java
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

package net.heartsome.cat.tmx.converter.xlsx;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import net.heartsome.cat.database.resource.Messages;
import net.heartsome.cat.tmx.converter.AbstractWriter;
import net.heartsome.cat.tmx.converter.Model2String;
import net.heartsome.cat.tmx.converter.TmxWriter;
import net.heartsome.cat.tmx.converter.bean.File2TmxConvertBean;


import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

public class Xlsx2Tmx extends net.heartsome.cat.tmx.converter.AbstractFile2Tmx {
	/**
	 * 记录日志
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(Xlsx2Tmx.class);

	private String attString;

	public void doCovnerter(File2TmxConvertBean file2TmxBean, IProgressMonitor monitor) throws Exception {
		Xlsx2TmxHelper tmxConver = new Xlsx2TmxHelper();
		attString = Model2String.getCustomArributeXML(file2TmxBean.customeAttr);
		AbstractWriter writer = getWriter(file2TmxBean);
		if (writer instanceof TmxWriter) {
			TmxWriter tmxWriter = (TmxWriter) writer;
			tmxWriter.setAttibuteString(attString);

		}
		// writer.writeHeader(file2TmxBean.srcLangCode);
		try {
			tmxConver.parseXlsxFileAndWriteTmxBody(file2TmxBean.sourceFilePath, writer, monitor);
		} catch (ParserConfigurationException e) {
			LOGGER.error("", e);
			throw new Exception(Messages.getString("converter.xlsx2tmx.parseExcle.error"));
		} catch (SAXException e) {
			LOGGER.error("", e);
			if ("LANG-CODE-ERORR".equals(e.getMessage())) {
				throw new Exception(Messages.getString("converter.common.vaild.langcode.error"));
			} else if ("DIFF--SRC-LANG-CODE".equals(e.getMessage())) {
				throw new Exception(Messages.getString("converter.common.appendtmx.diffsrcLang.error"));
			}else if("EMPTY-LANG-CODE".equals(e.getMessage())){
				throw new Exception(Messages.getString("converter.common.vaild.langcode.error"));
			}else if ("DUPLICATE-LANG-CODE-ERORR".equals(e.getMessage())){
				throw new Exception(Messages.getString("converter.common.vaild.duplicatelangcode.error"));
			}
		} catch (IOException e) {
			LOGGER.error("", e);
			throw new Exception(Messages.getString("converter.xlsx2tmx.writeTmx.error"));
		} catch (OpenXML4JException e) {
			LOGGER.error("", e);
			throw new Exception(Messages.getString("converter.xlsx2tmx.parseExcle.error"));
		} finally {
			writer.writeEnd();
			writer.closeOutStream();
			monitor.done();
		}

	}

	/**
	 * 获取正确的Writer
	 * @param file2TmxBean
	 * @return ;
	 */
	public AbstractWriter getWriter(File2TmxConvertBean file2TmxBean) {
		AbstractWriter tmxWriter = null;
		if (file2TmxBean.newTmxFilePath != null && !file2TmxBean.newTmxFilePath.isEmpty()) {
			File file = new File(file2TmxBean.newTmxFilePath);
			if (!file.exists()) {
				try {
					file.createNewFile();
				} catch (IOException e) {
					LOGGER.error("", e);
				}
			}
			try {
				tmxWriter = new TmxWriter(file2TmxBean.newTmxFilePath);
			} catch (FileNotFoundException e) {
				LOGGER.error("", e);

			}
		} else if (file2TmxBean.appendExistTmxFilePath != null && !file2TmxBean.appendExistTmxFilePath.isEmpty()) {
			/*try {
				tmxWriter = new AppendTmxWriter(file2TmxBean.appendExistTmxFilePath);
			} catch (FileNotFoundException e) {
				LOGGER.error("", e);
			} catch (Exception e) {
				LOGGER.error("", e);
			}*/

		}
		return tmxWriter;
	}


}

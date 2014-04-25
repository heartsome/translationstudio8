/**
 * Txt2TbxConverter.java
 *
 * Version information :
 *
 * Date:2013-12-18
 *
 * Copyright notice :
 * 本文件及其附带的相关文件包含机密信息，仅限瀚特盛科技有限公司指定的，与本项目有关的内部人员和客户联络人员查阅和使用。 
 * 如果您不是本保密声明中指定的收件者，请立即销毁本文件，禁止对本文件或根据本文件中的内容采取任何其他行动， 
 * 包括但不限于：泄露本文件中的信息、以任何方式制作本文件全部或部分内容之副本、将本文件或其相关副本提供给任何其他人。
 */
package net.heartsome.cat.document.converter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.heartsome.cat.common.locale.Language;
import net.heartsome.cat.common.locale.LocaleService;
import net.heartsome.cat.common.util.FileEncodingDetector;
import net.heartsome.cat.database.resource.Messages;
import net.heartsome.cat.document.txt.TxtRowReader;
import net.heartsome.cat.tmx.converter.LanguageUtils;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jason
 * @version
 * @since JDK1.6
 */
public class Txt2TbxConverter extends AbstractConverter {
	public Logger LOGGER = LoggerFactory.getLogger(Txt2TbxConverter.class);
	private String txtFile;
	private FileOutputStream out;
	private String[] header = null;

	public Txt2TbxConverter(String txtFile) {
		this.txtFile = txtFile;
	}

	/**
	 * (non-Javadoc)
	 * @see net.heartsome.cat.document.converter.AbstractConverter#doConvert(java.lang.String,
	 *      org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public void doConvert(String targetFile, IProgressMonitor monitor) throws Exception {
		try {
			monitor.beginTask("", 10);
			monitor.worked(2);
			out = new FileOutputStream(new File(targetFile));
			File f = new File(txtFile);
			String encoding = FileEncodingDetector.detectFileEncoding(f);
			TxtRowReader reader = new TxtRowReader(f, encoding);
			int rowsNum = reader.getLineNumber();
			if (rowsNum < 2) {
				throw new Exception(Messages.getString("converter.common.vaild.langcode.error"));
			}
			List<String[]> h = reader.read(1); // read header
			if (h == null || h.size() == 0) {
				throw new Exception(Messages.getString("converter.common.vaild.langcode.error"));
			}
			String[] hv = h.get(0);
			if (hv == null || hv.length < 2) {
				throw new Exception(Messages.getString("converter.common.vaild.langcode.error"));
			}
			Map<String, Language> defaultLanguage = LocaleService.getDefaultLanguage();
			List<String> _temp = new ArrayList<String>();
			for (String s : hv) {
				s = LanguageUtils.convertLangCode(s);
				if (_temp.contains(s)) {
					throw new Exception(Messages.getString("converter.common.vaild.duplicatelangcode.error"));
				}
				if (defaultLanguage.get(s) == null) {
					throw new Exception(Messages.getString("converter.common.vaild.langcode.error"));
				}
				_temp.add(s);
			}
			if (_temp.size() < 2) {
				throw new Exception(Messages.getString("converter.common.vaild.langcode.error"));
			}
			header = _temp.toArray(new String[] {});
			// generate header
			String s = generateTbxHeader(header[0]);
			writeString(s);

			// generate body
			int readSize = 10;
			List<String[]> rs = null;
			if(monitor.isCanceled()){
				throw new OperationCanceledException();
			}
			SubProgressMonitor subMonitor = new SubProgressMonitor(monitor, 8);
			subMonitor.beginTask("", rowsNum / readSize == 0 ? 1 : rowsNum / readSize);
			while ((rs = reader.read(readSize)) != null) {
				for (String[] r : rs) {
					if (r == null || r.length < 2) {
						continue;
					}
					int loopSize = r.length < header.length ? r.length : header.length;
					StringBuilder sb = new StringBuilder();
					for (int i = 0; i < loopSize; i++) {
						String value = r[i];
						String lang = header[i];
						sb.append("<langSet id=\"_" + (System.currentTimeMillis() + 1) + "\" xml:lang=\"" + lang
								+ "\">\n");
						sb.append("<tig>\n");
						sb.append("<term>" + value + "</term>\n");
						sb.append("</tig>\n");
						sb.append("</langSet>\n");
					}
					if (sb.length() != 0) {
						writeString("<termEntry id=\"_" + System.currentTimeMillis() + "\">\n");
						writeString(sb.toString());
						writeString("</termEntry>\n");
					}
					subMonitor.worked(1);
				}
				if(subMonitor.isCanceled()){
					throw new OperationCanceledException();
				}
			}
			// generate end
			s = generateTbxEnd();
			if (s != null && s.length() != 0) {
				writeString(s);
			}
			subMonitor.done();
		} finally {
			out.close();
			monitor.done();
		}
	}

	private void writeString(String s) throws IOException {
		if (s != null && s.length() != 0) {
			out.write(s.getBytes("UTF-8"));
		}
	}
}

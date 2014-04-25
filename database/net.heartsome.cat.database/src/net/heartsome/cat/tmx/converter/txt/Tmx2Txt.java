/**
 * Tmx2Txt.java
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
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.heartsome.cat.common.bean.TmxSegement;
import net.heartsome.cat.database.resource.Messages;
import net.heartsome.cat.document.TmxReadException;
import net.heartsome.cat.document.TmxReader;
import net.heartsome.cat.document.TmxReaderEvent;
import net.heartsome.cat.tmx.converter.AbstractTmx2File;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Tmx2Txt extends AbstractTmx2File {

	public static final String NAME = "txt";

	public static final byte[] SEPARATOR = { 9 };// 间隔符 tab
	public static final byte[] CRLF = { 13, 10 };// LFCR

	public static Logger LOGGER = LoggerFactory.getLogger(Tmx2Txt.class.getName());

	public void doCovnerter(String tmxFile, File targetFile, IProgressMonitor monitor) throws Exception {
		
		//1. 解析文件
		monitor.beginTask(Messages.getString("converter.common.monitor.info.start"), 100);
		monitor.setTaskName(Messages.getString("converter.common.monitor.info.start"));
		if (monitor.isCanceled()) {
			return;
		}
		monitor.worked(1);
		
		try {
			tmxReader = new TmxReader(new File(tmxFile));
		} catch (TmxReadException e) {
			LOGGER.error(Messages.getString("tmxdata.TmxFileContainer.parseTmxFileError"), e);
			throw e;
		}
		
		FileOutputStream fos = null;
		BufferedOutputStream bos = null;
		TmxReaderEvent event = null;
		List<String> langs = tmxReader.getLangs();
		Map<String, TmxSegement> map = new HashMap<String, TmxSegement>();

		if (monitor.isCanceled()) {
			return;
		}
		monitor.worked(9);//done 1

		
		try {
			fos = new FileOutputStream(targetFile, true);
		} catch (FileNotFoundException e) {
			LOGGER.error("", e);
			throw e;
		}
		bos = new BufferedOutputStream(fos);

		try {
			for (String str : langs) {
				bos.write(str.getBytes());
				bos.write(SEPARATOR);
			}
			bos.write(CRLF);
			// 2. 处理内容
			double total = tmxReader.getTotalTu();
			int worked = 0;//已完成
			int count = 0;//计数
			int tmp = 0;
			IProgressMonitor submMonitor = new SubProgressMonitor(monitor, 90);
			submMonitor.beginTask("", 100);
			submMonitor.setTaskName(Messages.getString("converter.common.monitor.info.readtmx"));
			loop: while (true) {
				if (monitor.isCanceled()) {
					return;
				}
				event = tmxReader.read();
				switch (event.getState()) {
				case TmxReaderEvent.END_FILE:
					break loop;
				case TmxReaderEvent.NORMAL_READ:
					map.clear();// 清除
					if (event.getTu().getSource() == null) {
						break;
					}
					map.put(langs.get(0).toLowerCase(), event.getTu().getSource());
					if (event.getTu().getSegments() != null) {
						for (TmxSegement seg : event.getTu().getSegments()) {
							map.put(seg.getLangCode().toLowerCase(), seg);
						}
					}
					// 生成每列
					for (String lang : langs) {
						if (map.containsKey(lang.toLowerCase())) {
							bos.write(encodeTab(map.get(lang.toLowerCase()).getPureText(), null));
						}
						bos.write(SEPARATOR);
					}
					bos.write(CRLF);
					break;
				default:
					continue;
				}
				
				tmp = (int) ((count++ / total) * 100);
				if (tmp > worked) {
					submMonitor.worked(tmp - worked);
					worked = tmp;
				}
			}
			bos.flush();
			bos.close();
			submMonitor.done();
			monitor.done();
		} catch (IOException e) {
			LOGGER.error("", e);
			throw e;
		} finally {
			try {
				fos.close();
				bos.close();
			} catch (IOException e) {
				LOGGER.error("close io");
			}
		}
	}

	private byte[] encodeTab(String str, String enc) {
		StringBuilder builder = new StringBuilder();
		char ch = '\0';
		for (int i = 0; i < str.length(); i++) {
			ch = str.charAt(i);
			switch (ch) {
			case '\\':
				builder.append('\\').append('\\');
				break;
			case '\t':
				builder.append('\\').append('t');
				break;
			case '\r':
				builder.append('\\').append('r');
				break;
			case '\n':
				builder.append('\\').append('n');
				break;
			default:
				builder.append(ch);
			}
		}
		return builder.toString().getBytes();
	}

	public static void main(String[] args) {
//		Tmx2Txt ttt = new Tmx2Txt();
		// ttt.doCovnerter("res/CareMore Health Plan_utf8.tmx", new File("res/_test.txt"));
	}
}

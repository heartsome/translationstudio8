package net.heartsome.cat.tmx.converter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.heartsome.cat.common.bean.TmxTU;

/**
 * 写入文件的抽象类
 * @author yule
 * @version
 * @since JDK1.6
 */
public abstract class AbstractWriter {
	/**
	 * 文件输出流
	 */
	protected FileOutputStream out;
	/**
	 * 记录日志
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractWriter.class);

	public AbstractWriter() {

	}

	public AbstractWriter(String filePath) throws FileNotFoundException {
		out = new FileOutputStream(new File(filePath));
	}

	/**
	 * 得到写入文件的前半部分，该内容不仅仅包含<code><header></header></code>内容 还有body的开始标签
	 * @param srcLang
	 *            ：源语言
	 * @return ;
	 */
	protected abstract String getHeaderXml(String srcLang);

	/**
	 * 得到写入文件的结束内容
	 * @return ;
	 */
	protected abstract String getEndXml();

	/**
	 * 写一个TmxTU
	 * @param tu
	 *            ;
	 */
	protected abstract void writeTmxTU(TmxTU tu);

	/**
	 * 全部内容以"UTF-8"编码写入文件
	 * @param xmlString
	 *            ;
	 */
	protected void writeXmlString(String xmlString) {
		try {
			out.write(xmlString.getBytes(getWriterEnconding()));
		} catch (UnsupportedEncodingException e) {
			LOGGER.error("", e);
		} catch (IOException e) {
			LOGGER.error("", e);
		}
	}

	/**
	 * 将缓存的内容写入文件
	 * @param cache
	 *            ;
	 */
	public void writeBody(List<TmxTU> cache) {
		for (TmxTU tu : cache) {
			writeTmxTU(tu);
		}
		flushContent();
	}

	/**
	 * 写入文件内容前部分内容
	 * @param srcLang
	 *            ;
	 */
	public void writeHeader(String srcLang) {
		String str = getHeaderXml(srcLang);
		int length = str.length();
		if (length > 2048) {
			int beginIndex = 0;
			int endIndex = 2048;
			while (endIndex <= str.length()) {
				writeXmlString(str.substring(beginIndex, endIndex));
				flushContent();
				beginIndex = endIndex;
				endIndex = endIndex + 2048;
			}
			if (endIndex > length) {
				writeXmlString(str.substring(beginIndex, length));
				flushContent();
			}
		} else {
			writeXmlString(str);
			flushContent();
		}
	}

	/**
	 * 写入文件的结束部分 ;
	 */
	public void writeEnd() {
		writeXmlString(getEndXml());
		flushContent();
	}

	/**
	 * 刷新输出流的缓存 ;
	 */
	public void flushContent() {
		try {
			out.flush();
		} catch (IOException e) {
			LOGGER.error("", e);
		}
	}

	/**
	 * 关闭输出流 ;
	 */
	public void closeOutStream() {
		try {
			out.close();
		} catch (IOException e) {
			LOGGER.error("", e);
		}
	}
	
	protected abstract String getWriterEnconding();
	}
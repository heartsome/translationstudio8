/**
 * Base64Utils.java
 *
 * Version information :
 *
 * Date:Jan 13, 2010
 *
 * Copyright notice :
 */
package cn.org.tools.utils.code;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.commons.codec.binary.Base64;

/**
 * 对文件进行 Base64 编码/解码的工具类.
 * @author Terry
 * @version
 * @since JDK1.6
 */
public class Base64Utils {
 
	/** 缓冲区大小，单位是 KB. */
	private int buffer;

	/**
	 * 构造方法.
	 */
	public Base64Utils() {
		this(1);
	}

	/**
	 * 构造方法.
	 * @param buffer
	 *            缓冲区大小
	 */
	public Base64Utils(int buffer) {
		this.buffer = buffer;
	}

	/**
	 * 对文件加密.
	 * @param inFile
	 *            要加密的文件
	 * @param outFile
	 *            加密后的文件
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public void encode(File inFile, File outFile) throws IOException {
		if (!inFile.exists()) {
			return;
		}
		FileInputStream fis;
		fis = new FileInputStream(inFile);
		FileOutputStream fos = new FileOutputStream(outFile);
		encode(fis, fos);
		fis.close();
		fos.close();
	}

	/**
	 * 对文件加密.
	 * @param fis
	 *            文件输入流，用于读入源文件
	 * @param fos
	 *            文件输出流，用于加密后写入文件
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public void encode(FileInputStream fis, FileOutputStream fos) throws IOException {
		byte[] buf = new byte[buffer * 1024 * 3];
		byte[] write = null;
		int i = 0;
		while ((i = fis.read(buf)) != -1) {
			if (i < buf.length) {
				byte[] temp = new byte[i];
				System.arraycopy(buf, 0, temp, 0, i);
				//对字节数组中指定的内容执行Base64 编码
				write = Base64.encodeBase64(temp);
			} else {
				write = Base64.encodeBase64(buf);
			}
			fos.write(write, 0, write.length);
		}
	}

	/**
	 * 对文件解密.
	 * @param inFile
	 *            要解密的文件
	 * @param outFile
	 *            解密后的文件
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public void decode(File inFile, File outFile) throws IOException {
		if (!inFile.exists()) {
			return;
		}
		FileInputStream fis;
		fis = new FileInputStream(inFile);
		FileOutputStream fos = new FileOutputStream(outFile);
		decode(fis, fos);
		fis.close();
		fos.close();
	}

	/**
	 * 对文件解密.
	 * @param fis
	 *            文件输入流，用于读入源文件
	 * @param fos
	 *            文件输出流，用于解密后写入文件
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public void decode(FileInputStream fis, FileOutputStream fos) throws IOException {
		byte[] buf = new byte[buffer * 1024 * 4];
		byte[] write = null;
		int i = 0;
		while ((i = fis.read(buf)) != -1) {
			if (i < buf.length) {
				byte[] temp = new byte[i];
				System.arraycopy(buf, 0, temp, 0, i);
				//对字节数组中指定的内容执行 Base64 解码
				write = Base64.decodeBase64(temp);
			} else {
				write = Base64.decodeBase64(buf);
			}
			fos.write(write, 0, write.length);
		}
	}
}

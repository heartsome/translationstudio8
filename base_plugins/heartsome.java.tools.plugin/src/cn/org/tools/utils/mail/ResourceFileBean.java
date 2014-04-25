/**
 * ResourceFileBean.java
 *
 * Version information :
 *
 * Date:Jan 13, 2010
 *
 * Copyright notice :
 */
package cn.org.tools.utils.mail;

import java.io.InputStream;

/**
 * The Class ResourceFileBean.
 * @author Terry
 * @version
 * @since JDK1.6
 */
public class ResourceFileBean {

	/** 文件名. */
	public String fileName;

	/** 输入流. */
	public InputStream inputStream;

	/**
	 * 构造方法.
	 * @param fileName
	 *            文件名
	 * @param inputStream
	 *            输入流
	 */
	public ResourceFileBean(String fileName, InputStream inputStream) {
		this.fileName = fileName;
		this.inputStream = inputStream;
	}

}

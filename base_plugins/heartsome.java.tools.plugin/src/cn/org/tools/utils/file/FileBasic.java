/**
 * FileBasic.java
 *
 * Version information :
 *
 * Date:Jan 13, 2010
 *
 * Copyright notice :
 */
package cn.org.tools.utils.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * The Class FileBasic.
 * @author Terry
 * @version
 * @since JDK1.6
 */
public class FileBasic {
	
	/**
	 * 构造方法.
	 */
	protected FileBasic() {
        throw new UnsupportedOperationException(); // prevents calls from subclass
    }

	/**
	 * 计算文件的 MD5 码.
	 * @param file
	 *            	文件对象
	 * @return String
	 * 				文件的 MD5 编码
	 * @throws NoSuchAlgorithmException
	 *             the no such algorithm exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public static String getMD5(File file) throws NoSuchAlgorithmException, IOException {
		FileInputStream fis = null;
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			fis = new FileInputStream(file);
			byte[] buffer = new byte[8192];
			int length = -1;
			while ((length = fis.read(buffer)) != -1) {
				md.update(buffer, 0, length);
			}
			return bytesToString(md.digest());
		} finally {
			if (fis != null) {
				fis.close();
			}
		}
	}

	/**
	 * 将字节数组转换为字符串.
	 * @param data
	 *            	字节数组
	 * @return String
	 * 				转换后的字符串
	 */
	public static String bytesToString(byte[] data) {
		char[] hexDigits = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
		char[] temp = new char[data.length * 2];
		for (int i = 0; i < data.length; i++) {
			byte b = data[i];
			temp[i * 2] = hexDigits[b >>> 4 & 0x0f];
			temp[i * 2 + 1] = hexDigits[b & 0x0f];
		}
		return new String(temp);
	}
}

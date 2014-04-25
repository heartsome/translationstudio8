/**
 * Md5Encode.java
 *
 * Version information :
 *
 * Date:Jan 13, 2010
 *
 * Copyright notice :
 */
package cn.org.tools.utils.encrypt;

import java.security.MessageDigest;

/**
 * 使用 MD5 标准对数据进行加密的工具类.
 * @author Terry
 * @version
 * @since JDK1.6
 */
public class Md5Encode {
	
	/**
	 * 构造方法.
	 */
	protected Md5Encode() {
        throw new UnsupportedOperationException(); // prevents calls from subclass
    }

	/**
	 * The main method.
	 * @param args
	 *            the arguments
	 */
	public static void main(String[] args) {
		String tmp = "123fadfafasfafadfafafhajkldfhdasjlkhfjlfasdf;ajs fk;lasjf ;asjf; as";
		System.out.println(encode(tmp));
	}

	/**
	 * 加密.
	 * @param s
	 *            	要加密的字符串
	 * @return String
	 * 				加密后的字符串
	 */
	public static final String encode(String s) {
		char[] hexDigits = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
		try {
			byte[] strTemp = s.getBytes();
			MessageDigest mdTemp = MessageDigest.getInstance("MD5");
			mdTemp.update(strTemp);
			byte[] md = mdTemp.digest();
			int j = md.length;
			char[] str = new char[j * 2];
			int k = 0;
			for (int i = 0; i < j; i++) {
				byte byte0 = md[i];
				str[k++] = hexDigits[byte0 >>> 4 & 0xf];
				str[k++] = hexDigits[byte0 & 0xf];
			}
			return new String(str);
		} catch (Exception e) {
			return null;
		}
	}
}

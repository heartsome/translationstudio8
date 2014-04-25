/**
 * DESImpl.java
 *
 * Version information :
 *
 * Date:Jan 13, 2010
 *
 * Copyright notice :
 */
package cn.org.tools.utils.encrypt;

import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

/**
 * 使用 DES 标准对数据进行加密的工具类.
 * @author Terry
 * @version
 * @since JDK1.6
 */
public class DESImpl {
	
	/**
	 * 构造方法.
	 */
	protected DESImpl() {
        throw new UnsupportedOperationException(); // prevents calls from subclass
    }

	/** The Constant PASSWORD_CRYPT_KEY. */
	private static final String PASSWORD_CRYPT_KEY = "ty7hia89wesyr98sar9d8teg";

	/** The Constant DES. */
	private static final String DES = "DES";

	/**
	 * 加密.
	 * @param src
	 *            数据源
	 * @param key
	 *            密钥，长度必须是8的倍数
	 * @return byte[]
	 * 			  加密后的数据
	 * @throws Exception
	 *             the exception
	 */
	public static byte[] encrypt(byte[] src, byte[] key) throws Exception {
		SecureRandom sr = new SecureRandom();
		DESKeySpec dks = new DESKeySpec(key);
		SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(DES);
		SecretKey securekey = keyFactory.generateSecret(dks);
		Cipher cipher = Cipher.getInstance(DES);
		cipher.init(Cipher.ENCRYPT_MODE, securekey, sr);
		return cipher.doFinal(src);
	}

	/**
	 * 解密.
	 * @param src
	 *            数据源
	 * @param key
	 *            密钥，长度必须是8的倍数
	 * @return byte[]
	 * 			  解密后的原始数据
	 * @throws Exception
	 *             the exception
	 */
	public static byte[] decrypt(byte[] src, byte[] key) throws Exception {
		SecureRandom sr = new SecureRandom();
		DESKeySpec dks = new DESKeySpec(key);
		SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(DES);
		SecretKey securekey = keyFactory.generateSecret(dks);
		Cipher cipher = Cipher.getInstance(DES);
		cipher.init(Cipher.DECRYPT_MODE, securekey, sr);
		return cipher.doFinal(src);
	}

	/**
	 * 密码解密.
	 * @param data
	 *            	数据
	 * @return String
	 * 				解密后的原始数据
	 * @throws Exception
	 */
	public static final String decrypt(String data) {
		try {
			return new String(decrypt(hex2byte(data.getBytes()), PASSWORD_CRYPT_KEY.getBytes()));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 密码加密.
	 * @param password
	 *            密码
	 * @return String
	 * 			  加密后的数据,如果加密过程中出现异常，返回 null
	 */
	public static final String encrypt(String password) {
		try {
			return byte2hex(encrypt(password.getBytes(), PASSWORD_CRYPT_KEY.getBytes()));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 将 byte 数组转化为十六进制字符串.
	 * @param b
	 *            	字节数组
	 * @return String
	 * 				字节数组转换成的字符串
	 */

	public static String byte2hex(byte[] b) {
		String hs = "";
		String stmp = "";
		for (int n = 0; n < b.length; n++) {
			stmp = (java.lang.Integer.toHexString(b[n] & 0XFF));
			if (stmp.length() == 1) {
				hs = hs + "0" + stmp;
			} else {
				hs = hs + stmp;
			}
		}
		return hs.toUpperCase();
	}

	/**
	 * 将十六进制字节数组转化为有符号的整数字节数组
	 * @param b
	 *            字节数组
	 * @return byte[]
	 * 				转化后的有符号的整数字节数组，如果 b 的长度为奇数，抛出异常
	 */
	public static byte[] hex2byte(byte[] b) {
		if ((b.length % 2) != 0) {
			throw new IllegalArgumentException("");
		}
		byte[] b2 = new byte[b.length / 2];
		for (int n = 0; n < b.length; n += 2) {
			String item = new String(b, n, 2);
			b2[n / 2] = (byte) Integer.parseInt(item, 16);
		}
		return b2;
	}

	/**
	 * The main method.
	 * @param args
	 *            the arguments
	 */
	public static void main(String[] args) {
		String a = DESImpl.encrypt("abc");
		System.out.println(a);
		String b = DESImpl.decrypt(a);
		System.out.println(b);
	}
}

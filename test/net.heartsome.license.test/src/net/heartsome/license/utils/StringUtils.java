package net.heartsome.license.utils;

public class StringUtils {
	public static String handle(String key, int interval, int start, int num) {
		char[] temp = new char[] {'0','1','2','3','4','5','6','7','8','9'};
		int size = temp.length;
		StringBuffer bu = new StringBuffer(key);
		int length = key.length();
		for (int i = start; i < length;) {
			for (int j = 0; j < num; j++) {
				int r = (int)(Math.random() * size);
				bu.insert(i, temp[r]);
			}
			i += interval + num;
			length += num;
		}
		
		return bu.toString();

	}
	
	public static String reverse(String key, int interval, int start, int num) {
		StringBuffer bu = new StringBuffer(key);
		int length = key.length();
		for (int i = start; i < length;) {
			for (int j = 0; j < num; j++) {
				bu.deleteCharAt(i);
			}
			i += interval;
			length -= num;
		}
		
		return bu.toString();

	}
	
	public static String removeColon(String str) {
		return str.replaceAll(":", "");
	}
	
	public static String toHexString(byte[] b) {
		StringBuilder sb = new StringBuilder(b.length * 2);
		for (int i = 0; i < b.length; i++) {
			sb.append(HEXCHAR[(b[i] & 0xf0) >>> 4]);
			sb.append(HEXCHAR[b[i] & 0x0f]);
		}
		return sb.toString();
	}

	public static byte[] toBytes(String s) {
		byte[] bytes;
		bytes = new byte[s.length() / 2];
		for (int i = 0; i < bytes.length; i++) {
			bytes[i] = (byte) Integer.parseInt(s.substring(2 * i, 2 * i + 2),
					16);
		}
		return bytes;
	}
	
	private static char[] HEXCHAR = { '0', '1', '2', '3', '4', '5', '6', '7',
		'8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
	
	/**
	 * 许可 KEY，使用标准的 4 个字符一组，然后使用 - 连接。
	 * @return ;
	 */
	public static String groupString(String str) {
		if (str == null || str.length() != 24) {
			return str;
		} else {
			String temp = "";
			for (int i = 0; i < 6; i++) {
				temp += str.substring(i * 4, (i + 1) * 4) + "-";
			}
			return temp.substring(0, temp.length() - 1);
		}
	}
}

package net.heartsome.license.utils;

import net.heartsome.license.constants.Constants;

public class StringUtils {
	public static String handle(String key, int interval, int start, int num) {
		char[] temp = new char[] {'0','1','2','3','4','5','6','7','8','9',
				'g','h','i','j','k','l','m','n','o','T','U','V','W','X','Y',
				'p','q','r','s','t','u','v','w','x','y','z','A','B','C','D',
				'E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S',
				'Z'};
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
	
	public static String getErrorCode(int i) {
		if (i == Constants.EXCEPTION_INT1) {
			return Constants.EXCEPTION_STRING1;
		} else if (i == Constants.EXCEPTION_INT2) {
			return Constants.EXCEPTION_STRING2;
		} else if (i == Constants.EXCEPTION_INT3) {
			return Constants.EXCEPTION_STRING3;
		} else if (i == Constants.EXCEPTION_INT4) {
			return Constants.EXCEPTION_STRING4;
		} else if (i == Constants.EXCEPTION_INT5) {
			return Constants.EXCEPTION_STRING5;
		} else if (i == Constants.EXCEPTION_INT6) {
			return Constants.EXCEPTION_STRING6;
		} else if (i == Constants.EXCEPTION_INT7) {
			return Constants.EXCEPTION_STRING7;
		} else if (i == Constants.EXCEPTION_INT8) {
			return Constants.EXCEPTION_STRING8;
		} else if (i == Constants.EXCEPTION_INT9) {
			return Constants.EXCEPTION_STRING9;
		} else if (i == Constants.EXCEPTION_INT10) {
			return Constants.EXCEPTION_STRING10;
		} else if (i == Constants.EXCEPTION_INT11) {
			return Constants.EXCEPTION_STRING11;
		} else if (i == Constants.EXCEPTION_INT12) {
			return Constants.EXCEPTION_STRING12;
		} else if (i == Constants.EXCEPTION_INT13) {
			return Constants.EXCEPTION_STRING13;
		} else if (i == Constants.EXCEPTION_INT14) {
			return Constants.EXCEPTION_STRING14;
		} else if (i == Constants.EXCEPTION_INT15) {
			return Constants.EXCEPTION_STRING15;
		} else if (i == Constants.EXCEPTION_INT16) {
			return Constants.EXCEPTION_STRING16;
		} else {
			return Constants.EXCEPTION_STRING17;
		}
	}
}

package net.heartsome.cat.common.util;

public class UnicodeConverter {

	/**
	 * 将字符串转成 Unicode 码
	 * @param str
	 *            待转字符串
	 * @return Unicode 码
	 */
	public static String convert(String str) {
		str = (str == null ? "" : str);
		String tmp;
		StringBuffer sb = new StringBuffer(1000);
		char c;
		int i, j;
		sb.setLength(0);
		for (i = 0; i < str.length(); i++) {
			c = str.charAt(i);
			sb.append("\\u");
			j = (c >>> 8); // 取出高8位
			tmp = Integer.toHexString(j);
			if (tmp.length() == 1)
				sb.append("0");
			sb.append(tmp);
			j = (c & 0xFF); // 取出低8位
			tmp = Integer.toHexString(j);
			if (tmp.length() == 1)
				sb.append("0");
			sb.append(tmp);

		}
		return (new String(sb));
	}

	/**
	 * 将 Unicode 码转成字符串
	 * @param unicode
	 *            unicode 码
	 * @return 字符串
	 */
	public static String revert(String unicode) {
		unicode = (unicode == null ? "" : unicode);
		if (unicode.indexOf("\\u") == -1)// 如果不是unicode码则原样返回
			return unicode;

		StringBuffer sb = new StringBuffer(1000);

		for (int i = 0; i < unicode.length(); i += 6) {
			String strTemp = unicode.substring(i, i + 6);
			String value = strTemp.substring(2);
			int c = 0;
			for (int j = 0; j < value.length(); j++) {
				char tempChar = value.charAt(j);
				int t = 0;
				switch (tempChar) {
				case 'a':
					t = 10;
					break;
				case 'b':
					t = 11;
					break;
				case 'c':
					t = 12;
					break;
				case 'd':
					t = 13;
					break;
				case 'e':
					t = 14;
					break;
				case 'f':
					t = 15;
					break;
				default:
					t = tempChar - 48;
					break;
				}

				c += t * ((int) Math.pow(16, (value.length() - j - 1)));
			}
			sb.append((char) c);
		}
		return sb.toString();
	}

	public static void main(String[] args) {
		// int c = 0;
		// for (char i = '\uA000'; i <= '\uA099'; i++) {
		// c++;
		// System.out.println(i);
		// }
		// System.out.println(c);

		System.out.println(revert("\\ua001"));

		System.out.println(convert("ꀀ"));
	}
}

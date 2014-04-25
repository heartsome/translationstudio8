package net.heartsome.cat.converter.idml;


public class ParagraphManagement {

	/**
	 * 将字符串转换为 16 进制，以过滤掉 FFFE 和 FEFF 不可见字符
	 * @param s
	 * @return ;
	 */
	public String toHexString(String s) {
		String str = "";
		for (int i = 0; i < s.length(); i++) {
			int ch = (int) s.charAt(i);
			String s4 = Integer.toHexString(ch);
			str = str + s4;
		}
		return str;// 0x表示十六进制
	}
}

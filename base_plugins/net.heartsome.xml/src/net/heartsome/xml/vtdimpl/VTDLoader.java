/**
 * VTDLoader.java
 *
 * Version information :
 *
 * Date:2013-11-4
 *
 * Copyright notice :
 * 本文件及其附带的相关文件包含机密信息，仅限瀚特盛科技有限公司指定的，与本项目有关的内部人员和客户联络人员查阅和使用。 
 * 如果您不是本保密声明中指定的收件者，请立即销毁本文件，禁止对本文件或根据本文件中的内容采取任何其他行动， 
 * 包括但不限于：泄露本文件中的信息、以任何方式制作本文件全部或部分内容之副本、将本文件或其相关副本提供给任何其他人。
 */
package net.heartsome.xml.vtdimpl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.Arrays;

import com.ximpleware.ParseException;
import com.ximpleware.VTDGen;
import com.ximpleware.parser.XMLChar;

/**
 * @author Jason
 * @version
 * @since JDK1.6
 */
public class VTDLoader {

	/**
	 * 使用 VTD 解析 XML 文件，返回 VTDGen 对象<br>
	 * 解析 XML 时忽略掉了 XML 标准中的非法字符
	 * @param file
	 * @return
	 * @throws ParseException
	 * @throws IOException
	 * @throws Exception
	 *             ;
	 */
	public static VTDGen loadVTDGen(File f, String fileEncoding) throws ParseException, IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(f), fileEncoding));
		try {
			char[] cbuf = new char[8192];
			int count = reader.read(cbuf);
			if (count < 20) {// Error Empty File
				throw new ParseException("Other Error: XML not starting properly");
			}
			StringBuilder _sb = new StringBuilder();
			_sb.append(cbuf, 0, count < 500 ? count : 500); // 取前500个字符进行判断
			int stIdx = _sb.indexOf("<?xml");
			if (stIdx != -1) { // is xml
				int endIdx = _sb.indexOf("?>");
				if (endIdx != -1 && _sb.indexOf("encoding") != -1) {
					endIdx += 2;
					String s = "<?xml version=\"1.0\" ?>";
					char[] temp = new char[cbuf.length - endIdx + s.length()];
					char[] schar = s.toCharArray();
					System.arraycopy(schar, 0, temp, 0, schar.length);
					System.arraycopy(cbuf, endIdx, temp, schar.length, cbuf.length - endIdx);
					cbuf = temp;
					count = count - endIdx + s.length();
				}
			} else {
				throw new ParseException("Other Error: XML not starting properly");
			}
			byte[] bArr = new byte[(int) f.length()];
			int i = 0;
			int idx = 0;
			while (count > 0) {
				int tempIdx = 0;
				char[] tempChar = new char[count];
				for (; i < count; i++) {
					char c = cbuf[i];
					if (!XMLChar.isValidChar(c)) {
						continue;
					} else if (c == '&') {
						StringBuilder sb = new StringBuilder();
						int val = 0;
						int j = i + 1;
						if (j >= count) {
							c = (char) reader.read();
						} else {
							c = cbuf[j++];
						}
						sb.append(c);
						if (c == '#') {
							if (j >= count) {
								c = (char) reader.read();
							} else {
								c = cbuf[j++];
							}
							sb.append(c);
							if (c == 'x') {
								while (true) {
									if (j >= count) {
										c = (char) reader.read();
									} else {
										c = cbuf[j++];
									}
									sb.append(c);
									if (c >= '0' && c <= '9') {
										val = (val << 4) + (c - '0');
									} else if (c >= 'a' && c <= 'f') {
										val = (val << 4) + (c - 'a' + 10);
									} else if (c >= 'A' && c <= 'F') {
										val = (val << 4) + (c - 'A' + 10);
									} else if (c == ';') {
										break;
									} else {
										break;
									}
								}
							} else {
								while (true) {
									if (c >= '0' && c <= '9') {
										val = val * 10 + (c - '0');
									} else if (c == ';') {
										break;
									} else {
										break;
									}
									if (j >= count) {
										c = (char) reader.read();
									} else {
										c = cbuf[j++];
									}
									sb.append(c);
								}
							}
							if (!XMLChar.isValidChar(val)) {
								if (j <= count) {
									i = j;
								}
								System.out.println((char) val + " " + val + " " + sb);
								continue;
							} else {
								c = cbuf[i];
								tempChar[tempIdx++] = c;
								for (int t = 0; t < sb.length(); t++) {
									if (tempIdx >= tempChar.length) {
										tempChar = Arrays.copyOf(tempChar, tempChar.length + 1);
										count = tempChar.length;
									}
									tempChar[tempIdx++] = sb.charAt(t);
									i++;
								}
							}
						} else {
							c = cbuf[i];
							tempChar[tempIdx++] = c;
							for (int t = 0; t < sb.length(); t++) {
								if (tempIdx >= tempChar.length) {
									tempChar = Arrays.copyOf(tempChar, tempChar.length + 1);
									count = tempChar.length;
								}
								tempChar[tempIdx++] = sb.charAt(t);
								i++;
							}
						}
					} else {
						tempChar[tempIdx++] = c;
					}
				}
				if (tempChar.length != 0) {
					byte[] temp = getBytes(tempChar, 0, tempIdx);
					System.arraycopy(temp, 0, bArr, idx, temp.length);
					idx += temp.length;
				}
				count = reader.read(cbuf);
				i = 0;
			}

//			FileOutputStream os = new FileOutputStream("C:\\Users\\Jason\\Desktop\\Trados 2007.tmxsasas");
//			os.write(bArr, 0, idx);
//			os.close();

			VTDGen vg = new VTDGen();
			vg.setDoc(Arrays.copyOf(bArr, idx));
			vg.parse(true);
			return vg;
		} finally {
			reader.close();
		}
	}

	private static byte[] getBytes(char[] chars, int offset, int length) {
		Charset cs = Charset.defaultCharset();
		CharBuffer cb = CharBuffer.wrap(chars, offset, length);
		ByteBuffer bb = cs.encode(cb);
		byte[] ba = bb.array();
		return Arrays.copyOf(ba, bb.limit());
	}
}

package net.heartsome.cat.database;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.Set;

import net.heartsome.cat.common.bean.MetaData;
import net.heartsome.cat.common.core.exception.ImportException;
import net.heartsome.cat.common.util.FileEncodingDetector;
import net.heartsome.cat.database.resource.Messages;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 数据库操作的工具类
 * @author terry
 * @version
 * @since JDK1.6
 */
public class Utils {

	/**
	 * 构造函数
	 */
	protected Utils() {
		throw new UnsupportedOperationException(); // prevents calls from subclass
	}

	/**
	 * 将传入的文本中的某些特殊字符替换成元数据中的值
	 * @param text
	 * @param metaData
	 * @return ;
	 */
	public static String replaceParams(String text, MetaData metaData) {
		if (metaData == null) {
			return text;
		}
		text = text.replace("__SERVER_NAME__", metaData.getServerName()); //$NON-NLS-1$
		text = text.replace("__DATABASE_NAME__", metaData.getDatabaseName()); //$NON-NLS-1$
		text = text.replace("__PORT_NUMBER__", metaData.getPort()); //$NON-NLS-1$
		text = text.replace("__USER__", metaData.getUserName()); //$NON-NLS-1$
		text = text.replace("__PASSWORD__", metaData.getPassword()); //$NON-NLS-1$
		text = text.replace("__INSTANCE__", metaData.getInstance()); //$NON-NLS-1$
		text = text.replace("__PATH__", metaData.getDataPath());
		return text;
	}

	/**
	 * 将传入的 para 中的某些特殊字符替换成元数据中的值
	 * @param para
	 * @param metaData
	 * @return ;
	 */
	public static Properties replaceParams(HashMap<String, String> para, MetaData metaData) {
		Properties result = new Properties();
		Set<String> keySet = para.keySet();
		for (String i : keySet) {
			result.put(i, replaceParams(para.get(i), metaData));
		}
		return result;
	}

	/**
	 * 将标准语言替换为数据库存储的格式
	 * @param lang
	 * @return ;
	 */
	public static String langToCode(String lang) {
		if (lang == null) {
			return null;
		}
		return lang.replaceAll("-", "");
	}

	public static Hashtable<String, String> getHeaderMatch() {
		Hashtable<String, String> re = new Hashtable<String, String>();
		re.put("creationtool", "CREATIONTOOL");
		re.put("creationtoolversion", "CTVERSION");
		re.put("o-tmf", "TMF");
		re.put("srclang", "SRCLANG");
		re.put("adminlang", "ADMINLANG");
		re.put("datatype", "DATATYPE");
		re.put("segtype", "SEGTYPE");
		re.put("creationdate", "CREATIONDATE");
		re.put("creationid", "CREATIONID");
		re.put("changedate", "CHANGEDATE");
		re.put("changeid", "CHANGEID");
		re.put("o-encoding", "ENCODING");
		return re;
	}

	public static Map<String, String> getTMXNotesMatch() {
		Map<String, String> re = new HashMap<String, String>();
		re.put("xml:lang", "LANG");
		re.put("creationdate", "CREATIONDATE");
		re.put("creationid", "CREATIONID");
		re.put("changedate", "CHANGEDATE");
		re.put("changeid", "CHANGEID");
		re.put("o-encoding", "ENCODING");
		return re;
	}

	public static Map<String, String> getTMXPropsMatch() {
		Map<String, String> re = new HashMap<String, String>();
		// 1.4 是type
		re.put("type", "TYPE");
		// 2.0是name
		re.put("name", "TYPE");
		re.put("xml:lang", "LANG");
		re.put("o-encoding", "ENCODING");
		return re;
	}

	public static Map<String, String> getTUMatch() {
		Map<String, String> re = new HashMap<String, String>();
		re.put("headerid", "HEADERID");
		re.put("tuid", "TUID");
		re.put("creationid", "CREATIONID");
		re.put("creationdate", "CREATIONDATE");
		re.put("changeid", "CHANGEID");
		re.put("changedate", "CHANGEDATE");
		re.put("creationtool", "CREATIONTOOL");
		re.put("creationtoolversion", "CTVERSION");
		// TODO 确定以下三个属性匹配是否正确
		re.put("client", "CLIENT");
		re.put("projectref", "PROJECTREF");
		re.put("jobref", "JOBREF");
		return re;
	}

	public static Map<String, String> getTUDbMatchTmx() {
		Map<String, String> re = new HashMap<String, String>();
		re.put("HEADERID", "headerid");
		re.put("TUID", "tuid");
		re.put("CREATIONID", "creationid");
		re.put("CREATIONDATE", "creationdate");
		re.put("CHANGEID", "changeid");
		re.put("CHANGEDATE", "changedate");
		re.put("CREATIONTOOL", "creationtool");
		re.put("CTVERSION", "creationtoolversion");
		// TODO 确定以下三个属性匹配是否正确
		re.put("CLIENT", "client");
		re.put("PROJECTREF", "projectref");
		re.put("JOBREF", "jobref");
		return re;
	}

	/**
	 * 生成任意位数的随机数
	 * @param codeLength
	 *            随机数的位数
	 * @return 随机数
	 */
	public static String validateCode(int codeLength) {
		int count = 0;
		char str[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' };
		StringBuffer pwd = new StringBuffer("");
		Random r = new Random();
		while (count < codeLength) {
			int i = Math.abs(r.nextInt(10));
			if (i >= 0 && i < str.length) {
				pwd.append(str[i]);
				count++;
			}
		}
		return pwd.toString();
	}

	/**
	 * 用字符串 strTgt 替换字符串 text 中的 strSrc 字符串
	 * @param text
	 *            需要做替换处理的字符串
	 * @param strSrc
	 *            替换的字符串
	 * @param strTgt
	 *            新的字符串
	 * @return
	 */
	public static String replaceString(String text, String strSrc, String strTgt) {
		if (text == null || strSrc == null) {
			return null;
		}
		if (strTgt == null) {
			strTgt = "";
		}
		if (!strTgt.equals("")) {
			// 替换特殊字符($)
			strTgt = strTgt.replaceAll("\\$", "\\\\\\$");
		}
		return text.replaceAll(strSrc, strTgt);
	}

	/**
	 * 1为String类型,2为datetime类型 获取匹配条件对应数据库字段
	 * @param tableName
	 *            表名,根据表名将获得对应表中的过滤字段,当前支持 MTU,TEXTDATA,MNOTE
	 * @return ;
	 */
	public static Map<String, Character> getFilterMatchMTU(String tableName) {
		Map<String, Character> match = new HashMap<String, Character>();
		if (tableName.equals("MTU")) {
			match.put("CREATIONDATE", '2');
			match.put("CHANGEDATE", '2');
			match.put("CREATIONID", '1');
			match.put("CHANGEID", '1');
			match.put("PROJECTREF", '1');
			match.put("JOBREF", '1');

		} else if (tableName.equals("TEXTDATA")) {
			match.put("PURE", '1');
		} else if (tableName.equals("MNOTE")) {
			match.put("CONTENT", '1');
		}
		return match;
	}

	public static String convertLangCode(String lang) {
		if (lang == null || lang.equals("")) {
			return lang;
		}
		if (lang.length() == 5) {
			String[] code = lang.split("-");
			if (code.length == 2) {
				return code[0].toLowerCase() + "-" + code[1].toUpperCase();
			} else {
				return lang;
			}
		} else if (lang.length() == 2) {
			return lang.toLowerCase();
		} else {
			return lang;
		}
	}

	public static File clearTmxFile(File f) throws ImportException {
		Logger logger = LoggerFactory.getLogger(Utils.class);
		File tempFile = null;
		BufferedWriter writer = null;
		try {
			tempFile = File.createTempFile("tmxtemp", ".tmx");
			writer = new BufferedWriter(new FileWriter(tempFile));
		} catch (IOException e1) {
			logger.error("", e1);
		}
		
		String encoding = FileEncodingDetector.detectFileEncoding(f);
		FileInputStream in = null;
		try {
			in = new FileInputStream(f);
		} catch (FileNotFoundException e) {
			if (writer != null) {
				try {
					writer.close();
				} catch (IOException e1) {
					logger.error("", e1);
				}
			}
			logger.error("", e);
		}
		InputStreamReader inr = null;
		try {
			inr = new InputStreamReader(in, encoding);
		} catch (UnsupportedEncodingException e1) {
			try {
				if (writer != null) {
					writer.close();
				}
				if (in != null) {
					in.close();
				}
			} catch (IOException e) {
				logger.error("", e);
			}
			throw new ImportException(Messages.getString("database.Utils.msg1"));
		}
		BufferedReader bfr = new BufferedReader(inr);
		try {
			String line = bfr.readLine();
			line = clearXMLEnconding(line);// // Bug #3428
			boolean flg = true;
			while (line != null) {
				if (flg) {
					int tmpos = line.indexOf("<header");
					if (tmpos != -1) {
						// porp started
						StringBuffer headerbf = new StringBuffer();
						headerbf.append(line);
						int tempeos = line.indexOf(">", tmpos);
						while (tempeos == -1) {
							line = bfr.readLine();
							if (line == null) {
								throw new ImportException(Messages.getString("database.Utils.msg2"));
							}
							headerbf.append("\n").append(line);
							tempeos = headerbf.indexOf(">");
						}
						String headerStart = headerbf.substring(0, tempeos + 1);
						writer.write(headerStart);
						int t = line.indexOf("/>");
						if (t != -1) {
							String end = line.substring(t + 2, line.length());
							if (end != null && end.length() != 0) {
								writer.write(end);
							}
							line = bfr.readLine();
							flg = false;
							continue;
						}
						t = line.indexOf("</header>");
						if (t != -1) {
							String end = line.substring(t, line.length());
							writer.write(end);
							line = bfr.readLine();
							flg = false;
							continue;
						}
						// read to </header>
						line = bfr.readLine();
						while (line != null) {
							int endof = line.indexOf("</header>");
							if (endof != -1) {
								line = line.substring(endof, line.length());
								flg = false;
								break;
							}
							line = bfr.readLine();
						}
					}
				}
				writer.write(line);
				line = bfr.readLine();
			}
		} catch (IOException e) {
			logger.error("", e);
		} finally {
			try {
				if (writer != null) {
					writer.close();
				}
				if (bfr != null) {
					bfr.close();
				}
			} catch (IOException e) {
				logger.error("", e);
			}
		}

		return tempFile;
	}

	// Bug #3428
	private static String clearXMLEnconding(String origionDec) {
		if (null == origionDec ||origionDec.trim().isEmpty()) {
			return origionDec;
		}
		return origionDec.replaceFirst("<[?].*?[?]>", "<?xml version=\"1.0\"?>");
		
	}

	public static void main(String arg[]) {
		System.out.println(convertLangCode("en-US"));
	}
}

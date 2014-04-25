/**
 * TmxHeaderTemple.java
 *
 * Version information :
 *
 * Date:2013-9-9
 *
 * Copyright notice :
 * 本文件及其附带的相关文件包含机密信息，仅限瀚特盛科技有限公司指定的，与本项目有关的内部人员和客户联络人员查阅和使用。 
 * 如果您不是本保密声明中指定的收件者，请立即销毁本文件，禁止对本文件或根据本文件中的内容采取任何其他行动， 
 * 包括但不限于：泄露本文件中的信息、以任何方式制作本文件全部或部分内容之副本、将本文件或其相关副本提供给任何其他人。
 */
package net.heartsome.cat.tmx.converter.bean;

import java.util.Locale;

import net.heartsome.cat.common.bean.TmxHeader;
import net.heartsome.cat.tmx.converter.Model2String;

/**
 * @author yule
 * @version
 * @since JDK1.6
 */
public class TmxTemple {

	public final static String LINE_SPLIT = System.getProperty("line.separator");

	public final static String XML_DECLARE = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";

	public final static String TMX_DECLARE = "<!DOCTYPE tmx PUBLIC \"-//LISA OSCAR:1998//DTD for Translation Memory eXchange//EN\" \"tmx14.dtd\" >";

	public static String getHeaderXmlString(TmxHeader tmxHeader) {
		StringBuilder sb = new StringBuilder();
		sb.append("<header");
		sb.append(LINE_SPLIT);
		sb.append("        creationtool=");
		sb.append(wrapTextUseQout(tmxHeader.getCreationtool()));
		sb.append(LINE_SPLIT);
		sb.append("        creationtoolversion=");
		sb.append(wrapTextUseQout(tmxHeader.getCreationtoolversion()));
		sb.append(LINE_SPLIT);
		sb.append("        srclang=");
		sb.append(wrapTextUseQout(tmxHeader.getSrclang()));
		sb.append(LINE_SPLIT);
		sb.append("        adminlang=");
		sb.append(wrapTextUseQout(tmxHeader.getAdminlang()));
		sb.append(LINE_SPLIT);
		sb.append("        datatype=");
		sb.append(wrapTextUseQout(tmxHeader.getDatatype()));
		sb.append(LINE_SPLIT);
		sb.append("        o-tmf=");
		sb.append(wrapTextUseQout(tmxHeader.getOtmf()));
		sb.append(LINE_SPLIT);
		sb.append("        segtype=");
		sb.append(wrapTextUseQout(tmxHeader.getSegtype()));
		sb.append(LINE_SPLIT);
		sb.append("        creationdate=");
		sb.append(wrapTextUseQout(tmxHeader.getCreationdate()));
		sb.append(">");
		sb.append(LINE_SPLIT);
		sb.append("</header>");
		sb.append(LINE_SPLIT);
		return sb.toString();

	}

	/**
	 * 写入TMX文件默认前缀字符串 <br/>
	 * body节点以前的所有内容 (包括tmx body节点的开始标签);
	 */
	public static String getDefaultTmxPrefix(String srcLang) {
		StringBuilder sb = new StringBuilder();
		sb.append(XML_DECLARE);
		sb.append(LINE_SPLIT);

		sb.append(TMX_DECLARE);
		sb.append(LINE_SPLIT);

		sb.append("<tmx version=\"1.4\">");
		sb.append(LINE_SPLIT);

		sb.append(getHeaderXmlString(getDefaultHeader(srcLang)));
		sb.append("<body>");
		sb.append(LINE_SPLIT);
		return sb.toString();
	}

	private static String wrapTextUseQout(String srcText){
		return "\"" +srcText+"\"";
	}
	private static TmxHeader getDefaultHeader(String srcLang) {
		TmxHeader header = new TmxHeader();
		header.setAdminlang(Locale.getDefault().toString().replaceAll("_", "-"));
		header.setSrclang(srcLang);
		header.setCreationdate(Model2String.creationDate());
		header.setCreationtool("HS TMX Editor");
		header.setCreationtoolversion("8.0.0");
		header.setDatatype("xml");
		header.setSegtype("unknown");
		header.setOtmf("block");
		return header;
	}

	private TmxTemple() {

	}
	public static void main(String[] args) {
		System.out.println(getDefaultTmxPrefix("zh-CN"));
	}
}

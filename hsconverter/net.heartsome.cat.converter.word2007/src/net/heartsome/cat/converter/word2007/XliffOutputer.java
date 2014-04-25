package net.heartsome.cat.converter.word2007;

import java.io.FileOutputStream;
import java.io.IOException;

import net.heartsome.cat.converter.Converter;
import net.heartsome.util.CRC16;
import net.heartsome.util.TextUtil;

/**
 * word 2007 转换至 xliff 文件时，xliff 文件的写入操作类。
 * @author robert	2012-08-08
 */
public class XliffOutputer {
	/** xliff 文件的路径 */
	private String xliffPath;
	/** xliff 文件的源语言 */
	private String sourceLanguage;
	/** xliff 文件的目标语言 */
	private String targetLanguage;
	private FileOutputStream output;
	/** trans-unit 节点的id */
	private int segId;
	/** 所有 标记的 id */
	private int tagId;
	
	public XliffOutputer(){}
	
	public XliffOutputer(String xliffPath, String sourceLanguage, String targetLanguage) throws Exception {
		this.xliffPath = xliffPath;
		this.sourceLanguage = sourceLanguage;
		this.targetLanguage = targetLanguage;
		output = new FileOutputStream(this.xliffPath);
		
		
	}
	
	/**
	 * 写下 xliff 文件的头信息
	 * @throws IOException
	 */
	public void writeHeader(String inputFile, String skeletonFile, boolean isSuite, String srcEncoding, String qtToolID) throws IOException {
		writeOut("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n"); 
		writeOut("<xliff version=\"1.2\" xmlns=\"urn:oasis:names:tc:xliff:document:1.2\" " +
				"xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " + "xmlns:hs=\"" + Converter.HSNAMESPACE
				+ "\" " + 
				"xsi:schemaLocation=\"urn:oasis:names:tc:xliff:document:1.2 xliff-core-1.2-transitional.xsd "
				+ Converter.HSSCHEMALOCATION + "\">\n"); 
		if (!targetLanguage.equals("")) {
			writeOut("<file datatype=\"x-msofficeWord2007\" original=\"" + cleanString(inputFile) + "\" source-language=\"" + sourceLanguage + "\" target-language=\"" + targetLanguage + "\">\n"); 
		}else {
			writeOut("<file datatype=\"x-msofficeWord2007\" original=\"" + cleanString(inputFile) + "\" source-language=\"" + sourceLanguage + "\">\n"); 
		}
		writeOut("<header>\n"); 
		writeOut("<skl>\n"); 
		if (isSuite) {
			writeOut("<external-file crc=\"" + CRC16.crc16(TextUtil.cleanString(skeletonFile).getBytes("UTF-8")) + "\" href=\"" + cleanString(skeletonFile) + "\"/>\n"); 
		} else {
			writeOut("<external-file href=\"" + cleanString(skeletonFile) + "\"/>\n"); 
		}
		writeOut("</skl>\n"); 
		writeOut("   <tool tool-id=\"" + qtToolID + "\" tool-name=\"HSStudio\"/>\n"); 
		writeOut("   <hs:prop-group name=\"encoding\"><hs:prop prop-type=\"encoding\">" 
				+ srcEncoding + "</hs:prop></hs:prop-group>\n");
		writeOut("</header>\n<body>\n"); 
		writeOut("\n"); 
	}
	
	
	private void writeOut(String string) throws IOException {
		output.write(string.getBytes("UTF-8")); 
	}
	
	
	/**
	 * 写入xliff文件的结束标记
	 */
	public void outputEndTag() throws Exception{
		writeOut("</body>\n</file>\n</xliff>");
	}
	
	
	/**
	 * 关闭 xliff 的写入流
	 */
	public void close() throws Exception{
		output.close();
	}
	
	
	public String cleanString(String string) {
		string = string.replaceAll("&", "&amp;"); 
		string = string.replaceAll("<", "&lt;"); 
		string = string.replaceAll(">", "&gt;"); 
		string = string.replaceAll("\"", "&quot;"); 
		return string;
	}
	
	
	
	
	public int getSegId() {
		return segId;
	}

	public int getTagId() {
		return tagId;
	}

	/**
	 * 设置phId 步增一值
	 */
	public int useTagId() {
		return tagId++;
	}
	
	public int useSegId(){
		return segId++;
	}
	
	/**
	 * 生成 trans-unit 节点
	 * @param source
	 */
	public String addTransUnit(String source) throws Exception {
		if (source.length() > 0) {
			StringBuffer sb = new StringBuffer();
			sb.append("\t<trans-unit id=\"" + segId + "\" xml:space=\"preserve\">\n");
			sb.append("\t\t<source xml:lang=\""+ sourceLanguage +"\">");
			sb.append(source);
			sb.append("</source>\n");
			sb.append("\t</trans-unit>\n");
			writeOut(sb.toString());
			return "%%%" + (segId++) + "%%%";
		}
		return null;
	}
	
	/**
	 * 生成 trans-unit 节点
	 * @param source
	 */
	public void addTransUnit(String source, String segIdStr) throws Exception {
		if (source.length() > 0) {
			StringBuffer sb = new StringBuffer();
			sb.append("\t<trans-unit id=\"" + segIdStr + "\" xml:space=\"preserve\">\n");
			sb.append("\t\t<source xml:lang=\""+ sourceLanguage +"\">");
			sb.append(source);
			sb.append("</source>\n");
			sb.append("\t</trans-unit>\n");
			writeOut(sb.toString());
		}
	}

	
	public static void main(String[] args) {
//		String a = "<w:fldSimple w:instr=\" DOCPROPERTY  \"Variable test\"  \\* MERGEFORMAT \"><w:r><w:rPr><w:rFonts w:eastAsia=\"宋体\"/><w:sz w:val=\"22\"/><w:szCs w:val=\"22\"/><w:lang w:eastAsia=\"zh-CN\"/></w:rPr><w:t>Irrigation Pump</w:t></w:r></w:fldSimple>";
		String a = "<w:fldSimple w:instr=\"DOCPROPERTY  &quot;Variable test&quot;  \\* MERGEFORMAT \"><w:r><w:rPr><w:rFonts w:eastAsia=\"宋体\"/>"
           +"<w:sz w:val=\"22\"/><w:szCs w:val=\"22\"/><w:lang w:eastAsia=\"zh-CN\"/></w:rPr><w:t>Irrigation Pump</w:t></w:r></w:fldSimple>";
		System.out.println(cleanString11(a));
	
		
		
	}
	
	public static String cleanString11(String string) {
		string = string.replaceAll("&", "&amp;");
		string = string.replaceAll("<", "&lt;"); 
		string = string.replaceAll(">", "&gt;"); 
		string = string.replaceAll("\"", "&quot;"); 
		return string;
	}

}

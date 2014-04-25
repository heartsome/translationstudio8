package net.heartsome.cat.converter.word2007.common;

/**
 * 保存部份需要使用的文件的相对路径，<div style='color:red'>使用之前，当前路径必须为根目录 superRoot</div>
 * @author robert	2012-08-08
 */
public class PathConstant {
	/** 主文档 document.xml的相对路径 */
	public static final String DOCUMENT = "word/document.xml";
	/** 批注 comments.xml的相对路径 */
	public static final String COMMENTS = "word/comments.xml";
	/** 尾注 endnotes.xml的相对路径 */
	public static final String ENDNOTES = "word/endnotes.xml";
	
	public static final String FONTTABLE = "word/fontTable.xml";
	/** 脚注 footnotes.xml的相对路径 */
	public static final String FOOTNOTES = "word/footnotes.xml";
	/** 文档设置 settings.xml的相对路径 */
	public static final String SETTINGS = "word/settings.xml";
	/** 样式定义 styles.xml的相对路径 */
	public static final String STYLES = "word/styles.xml";
	public static final String WEBSETTINGS = "word/webSettings.xml";
	/** 主文档 与其他文件进行关联的关联文件 document.xml.rels */
	public static final String DOCUMENTRELS = "word/_rels/document.xml.rels";
	/** 文件夹 word 的位置，针对于主目录 */
	public static final String WORD_FOLDER = "word";
	/** 文件 interTag.xml 的位置，针对于主目录 */
	public static final String interTag_FILE = "interTag.xml";
}

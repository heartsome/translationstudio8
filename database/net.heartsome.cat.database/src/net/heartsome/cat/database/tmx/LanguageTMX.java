package net.heartsome.cat.database.tmx;

/**
 * 执行相关搜索时需要用到的 Bean
 * @author  peason
 * @version 
 * @since   JDK1.6
 */
public class LanguageTMX {

	/** 语言代码，如 en-US(对应 TEXTDATA 的 LANG) */
	private String languageCode;
	
	/** 节点内容，可能包含标记（对应 TEXTDATA 的 PURE 或 CONTENT） */
	private String text;
	
	public LanguageTMX() {
		
	}
	
	public LanguageTMX(String languageCode, String text) {
		this.languageCode = languageCode;
		this.text = text;
	}

	public String getLanguageCode() {
		return languageCode;
	}

	public void setLanguageCode(String languageCode) {
		this.languageCode = languageCode;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}
	
	

}

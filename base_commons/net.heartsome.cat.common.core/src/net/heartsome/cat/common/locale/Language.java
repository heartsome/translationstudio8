package net.heartsome.cat.common.locale;

import java.util.Locale;

/**
 * 标识为每一个语言设置实体
 * @author cheney
 * @since JDK1.6
 */
public class Language {

	// 语言代码
	private String code;

	// 语言名称
	private String name;

	// 语言图标
	private String imagePath;

	// 是否双向
	private boolean bidi;

	// 此语言对应的 locale
	private Locale locale;

	public Language(String code, String name, String imagePath, boolean bidi) {
		this.code = code;
		this.name = name;
		this.bidi = bidi;
		this.imagePath = imagePath;
	}

	/**
	 * 语言代码
	 * @return 语言代码;
	 */
	public String getCode() {
		return code;
	}

	/**
	 * 语言代码
	 * @param code
	 *            ;
	 */
	public void setCode(String code) {
		this.code = code;
	}

	/**
	 * 语言名称
	 * @return 语言名称;
	 */
	public String getName() {
		return name;
	}

	/**
	 * 语言名称
	 * @param name
	 *            ;
	 */
	public void setName(String name) {
		this.name = name;
	}

	/** @return the imagePath */
	public String getImagePath() {
		return imagePath;
	}

	/**
	 * @param imagePath
	 *            the imagePath to set
	 */
	public void setImagePath(String imagePath) {
		this.imagePath = imagePath;
	}

	/**
	 * 是否双向
	 * @return 是双向则返回 true，否则返回 false;
	 */
	public boolean isBidi() {
		return bidi;
	}

	/**
	 * 是否双向
	 * @param bidi
	 *            是则为 true，否则为 false;
	 */
	public void setBidi(boolean bidi) {
		this.bidi = bidi;
	}

	/**
	 * 返回此语言代码对应的 locale
	 * @return ;
	 */
	public Locale getLocale() {
		if (locale == null) {
			locale = new Locale(code);
		}
		return locale;
	}

	/**
	 * 这个方法的改动，会影响{@link TextUtil}的很多关于语言的方法，注意.	robert
	 */
	@Override
	public String toString() {
//		return new StringBuffer(code).append(" ").append(name).toString();
		// 修改BUG 2766 robert	2012-12-20 
		return new StringBuffer(name).append(" ").append(code).toString();
	}

}

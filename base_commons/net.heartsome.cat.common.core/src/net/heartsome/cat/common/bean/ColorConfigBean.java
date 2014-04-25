/**
 * ViewerColorBean.java
 *
 * Version information :
 *
 * Date:2012-5-2
 *
 * Copyright notice :
 * 本文件及其附带的相关文件包含机密信息，仅限瀚特盛科技有限公司指定的，与本项目有关的内部人员和客户联络人员查阅和使用。 
 * 如果您不是本保密声明中指定的收件者，请立即销毁本文件，禁止对本文件或根据本文件中的内容采取任何其他行动， 
 * 包括但不限于：泄露本文件中的信息、以任何方式制作本文件全部或部分内容之副本、将本文件或其相关副本提供给任何其他人。
 */
package net.heartsome.cat.common.bean;

import org.eclipse.swt.graphics.Color;

/**
 * 视图中的相关颜色封装
 * @author jason
 * @version
 * @since JDK1.6
 */
public class ColorConfigBean {
	private Color ptColor;
	private Color qtColor;
	private Color mtColor;
	private Color tm101Color;
	private Color tm100Color;
	private Color tm90Color;
	private Color tm80Color;
	private Color tm70Color;
	private Color tm0Color;

	private Color highlightedTermColor;

	// 源文不同前景色
	private Color srcDiffFgColor;
	// 源文不同背景色
	private Color srcDiffBgColor;

	// 标记前景色
	private Color tagFgColor;
	// 标记背景色
	private Color tagBgColor;

	/** 错误标记色 */
	private Color wrongTagColor;

	/** 错误单词的颜色 */
	private Color errorWordColor;

	private static ColorConfigBean instance;

	public static ColorConfigBean getInstance() {
		if (instance == null) {
			instance = new ColorConfigBean();
		}
		return instance;
	}

	private ColorConfigBean() {
	}

	public void release() {
		if (ptColor != null && !ptColor.isDisposed()) {
			ptColor.dispose();
		}
		if (mtColor != null && !mtColor.isDisposed()) {
			mtColor.dispose();
		}
		if (qtColor != null && !qtColor.isDisposed()) {
			qtColor.dispose();
		}
		if (tm101Color != null && !tm101Color.isDisposed()) {
			tm101Color.dispose();
		}
		if (tm100Color != null && !tm100Color.isDisposed()) {
			tm100Color.dispose();
		}
		if (tm90Color != null && !tm90Color.isDisposed()) {
			tm90Color.dispose();
		}
		if (tm80Color != null && !tm80Color.isDisposed()) {
			tm80Color.dispose();
		}
		if (tm70Color != null && !tm70Color.isDisposed()) {
			tm70Color.dispose();
		}
		if (tm0Color != null && !tm0Color.isDisposed()) {
			tm0Color.dispose();
		}

		if (srcDiffFgColor != null && !srcDiffFgColor.isDisposed()) {
			srcDiffFgColor.dispose();
		}
		if (srcDiffBgColor != null && !srcDiffBgColor.isDisposed()) {
			srcDiffBgColor.dispose();
		}

		if (tagFgColor != null && !tagFgColor.isDisposed()) {
			tagFgColor.dispose();
		}
		if (tagBgColor != null && !tagBgColor.isDisposed()) {
			tagBgColor.dispose();
		}
		if (wrongTagColor != null && !wrongTagColor.isDisposed()) {
			wrongTagColor.dispose();
		}
	}

	/** @return the ptColor */
	public Color getPtColor() {
		return ptColor;
	}

	/** @return the qtColor */
	public Color getQtColor() {
		return qtColor;
	}

	/** @return the mtColor */
	public Color getMtColor() {
		return mtColor;
	}

	/** @return the tm101Color */
	public Color getTm101Color() {
		return tm101Color;
	}

	/** @return the tm100Color */
	public Color getTm100Color() {
		return tm100Color;
	}

	/** @return the tm90Color */
	public Color getTm90Color() {
		return tm90Color;
	}

	/** @return the tm80Color */
	public Color getTm80Color() {
		return tm80Color;
	}

	/** @return the tm70Color */
	public Color getTm70Color() {
		return tm70Color;
	}

	/** @return the rm0Color */
	public Color getTm0Color() {
		return tm0Color;
	}

	/** @return the srcDiffFgColor */
	public Color getSrcDiffFgColor() {
		return srcDiffFgColor;
	}

	/** @return the srcDiffBgColor */
	public Color getSrcDiffBgColor() {
		return srcDiffBgColor;
	}

	/** @return the tagFgColor */
	public Color getTagFgColor() {
		return tagFgColor;
	}

	/** @return the tagBgColor */
	public Color getTagBgColor() {
		return tagBgColor;
	}

	/** @return the wrongTagColor */
	public Color getWrongTagColor() {
		return wrongTagColor;
	}

	/** @return the highlightedTermColor */
	public Color getHighlightedTermColor() {
		return highlightedTermColor;
	}

	public Color getErrorWordColor() {
		return errorWordColor;
	}

	/**
	 * @param ptColor
	 *            the ptColor to set
	 */
	public void setPtColor(Color ptColor) {
		this.ptColor = ptColor;
	}

	/**
	 * @param qtColor
	 *            the qtColor to set
	 */
	public void setQtColor(Color qtColor) {
		this.qtColor = qtColor;
	}

	/**
	 * @param mtColor
	 *            the mtColor to set
	 */
	public void setMtColor(Color mtColor) {
		this.mtColor = mtColor;
	}

	/**
	 * @param tm101Color
	 *            the tm101Color to set
	 */
	public void setTm101Color(Color tm101Color) {
		this.tm101Color = tm101Color;
	}

	/**
	 * @param tm100Color
	 *            the tm100Color to set
	 */
	public void setTm100Color(Color tm100Color) {
		this.tm100Color = tm100Color;
	}

	/**
	 * @param tm90Color
	 *            the tm90Color to set
	 */
	public void setTm90Color(Color tm90Color) {
		this.tm90Color = tm90Color;
	}

	/**
	 * @param tm80Color
	 *            the tm80Color to set
	 */
	public void setTm80Color(Color tm80Color) {
		this.tm80Color = tm80Color;
	}

	/**
	 * @param tm70Color
	 *            the tm70Color to set
	 */
	public void setTm70Color(Color tm70Color) {
		this.tm70Color = tm70Color;
	}

	/**
	 * @param tm0Color
	 *            the tm0Color to set
	 */
	public void setTm0Color(Color tm0Color) {
		this.tm0Color = tm0Color;
	}

	/**
	 * @param highlightedTermColor
	 *            the highlightedTermColor to set
	 */
	public void setHighlightedTermColor(Color highlightedTermColor) {
		this.highlightedTermColor = highlightedTermColor;
	}

	/**
	 * @param srcDiffFgColor
	 *            the srcDiffFgColor to set
	 */
	public void setSrcDiffFgColor(Color srcDiffFgColor) {
		this.srcDiffFgColor = srcDiffFgColor;
	}

	/**
	 * @param srcDiffBgColor
	 *            the srcDiffBgColor to set
	 */
	public void setSrcDiffBgColor(Color srcDiffBgColor) {
		this.srcDiffBgColor = srcDiffBgColor;
	}

	/**
	 * @param tagFgColor
	 *            the tagFgColor to set
	 */
	public void setTagFgColor(Color tagFgColor) {
		this.tagFgColor = tagFgColor;
	}

	/**
	 * @param tagBgColor
	 *            the tagBgColor to set
	 */
	public void setTagBgColor(Color tagBgColor) {
		this.tagBgColor = tagBgColor;
	}

	/**
	 * @param wrongTagColor
	 *            the wrongTagColor to set
	 */
	public void setWrongTagColor(Color wrongTagColor) {
		this.wrongTagColor = wrongTagColor;
	}

	/**
	 * @param errorWordColor
	 *            the errorWordColor to set
	 */
	public void setErrorWordColor(Color errorWordColor) {
		this.errorWordColor = errorWordColor;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		return sb.append(ptColor.toString()).append("-").append(qtColor.toString()).append("-")
				.append(mtColor.toString()).append("-").append("-").append(tm101Color.toString()).append("-")
				.append(tm100Color.toString()).append("-").append(tm90Color.toString()).append("-")
				.append(tm80Color.toString()).append("-").append(tm70Color.toString()).append("-")
				.append(tm0Color.toString()).append("-").append(srcDiffFgColor.toString()).append("-")
				.append(srcDiffBgColor.toString()).append("-").append(tagFgColor.toString()).append("-")
				.append(tagBgColor.toString()).append("-").append(wrongTagColor.toString()).toString();
	}

}

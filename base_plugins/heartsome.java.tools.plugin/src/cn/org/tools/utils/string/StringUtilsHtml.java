/**
 * StringUtilsHtml.java
 *
 * Version information :
 *
 * Date:Jan 13, 2010
 *
 * Copyright notice :
 */
package cn.org.tools.utils.string;

import java.util.ArrayList;

/**
 * 所有关于 HTML 文档的基本操作.
 * @author Terry
 * @version
 * @since JDK1.6
 */
public class StringUtilsHtml extends StringUtilsBasic {

	/**
	 * 获得 html 格式的单元格，用于设置 table 中的表头。
	 * @param value
	 *            单元格的值
	 * @param align
	 *            对齐方式。较常用的三个值："center","left","right"
	 * @param width
	 *            单元格的宽度
	 * @param colspan
	 *            单元格所占列数
	 * @param rowspan
	 *            单元格所占行数
	 * @return String
	 * 				html 格式的 td 标签
	 */
	public static String getHtmlTdHead(String value, String align, String width, int colspan, int rowspan) {
		StringBuilder htmlStr = new StringBuilder();
		htmlStr.append("<td align='" + align + "' width='" + width + "' bgcolor='#c3cfdf'");
		if (colspan > 1) {
			htmlStr.append("colspan='" + colspan + "'");
		}
		if (rowspan > 1) {
			htmlStr.append("rowspan='" + rowspan + "'");
		}
		htmlStr.append("><p><b>" + value + "</b></p></td>");
		return htmlStr.toString();
	}
	
	/**
	 * 获得 html 格式的单元格，用于设置 table 中的表头，此种方式对创建的单元格设置了样式(若使表头固定，width参数可以传入"0%")。
	 * @param value
	 *            单元格的值
	 * @param align
	 *            对齐方式。较常用的三个值："center","left","right"
	 * @param width
	 *            单元格的宽度
	 * @param colspan
	 *            单元格所占列数
	 * @param rowspan
	 *            单元格所占行数
	 * @return String
	 * 				html 格式的 td 标签
	 */
	public static String getHtmlTdHead2(String value, String align, String width, int colspan, int rowspan) {
		StringBuilder htmlStr = new StringBuilder();
		htmlStr.append("<td align='" + align + "' bgcolor='#c3cfdf'");
		if (Integer.parseInt(width.substring(0, width.length() - 1)) > 0) {
			htmlStr.append(" width='" + width + "'");
		}
		htmlStr.append(" style='BORDER-RIGHT: #555 1px solid;BORDER-TOP: #fff 1px solid;BORDER-BOTTOM: #555 1px solid;BORDER-LEFT: #fff 1px solid;TEXT-ALIGN:center;font-size:10pt'");
		if (colspan > 1) {
			htmlStr.append("colspan='" + colspan + "'");
		}
		if (rowspan > 1) {
			htmlStr.append("rowspan='" + rowspan + "'");
		}
		htmlStr.append(">" + value + "</td>");
		return htmlStr.toString();
	}
	/**
	 * 获得 html 格式的单元格，用于设置 table 中的表头。
	 * @param value
	 *            单元格的值
	 * @param align
	 *            对齐方式。较常用的三个值："center","left","right"
	 * @param width
	 *            单元格的宽度
	 * @param colspan
	 *            单元格所占列数
	 * @param rowspan
	 *            单元格所占行数
	 * @param fontsize
	 *            字体大小
	 * @return String
	 * 				html 格式的 td 标签
	 */
	public static String getHtmlTdHead(String value, String align, String width, int colspan, int rowspan, int fontsize) {
		StringBuilder htmlStr = new StringBuilder();
		htmlStr.append("<td align='" + align + "' width='" + width + "' bgcolor='#c3cfdf'");
		if (colspan > 1) {
			htmlStr.append("colspan='" + colspan + "'");
		}
		if (rowspan > 1) {
			htmlStr.append("rowspan='" + rowspan + "'");
		}
		htmlStr.append("><p style='font-size: " + fontsize + "pt'><b>" + value + "</b></p></td>");
		return htmlStr.toString();
	}

	/**
	 * 获得 html 的头.
	 * @return String
	 * 				html 的头.
	 */
	public static String getHtmlHead() {
		StringBuilder htmlStr = new StringBuilder();
		htmlStr
				.append("<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" /></head><body>");
		return htmlStr.toString();
	}
	
	/**
	 * 获得 html 的头，此方式对 html 文档规定了命名空间
	 * @return the String
	 * 				html 的头.
	 */
	public static String getHtmlHead2() {
		StringBuilder htmlStr = new StringBuilder();
		htmlStr
				.append("<html xmlns=\"http://www.w3.org/1999/xhtml\"><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" />");
		return htmlStr.toString();
	}
	
	/**
	 * 获得 html 的 DOCTYPE 标签
	 * @return String
	 * 				html 的 DOCTYPE 标签
	 */
	public static String getHtmlDoctype() {
		StringBuilder htmlStr = new StringBuilder();
		htmlStr
				.append("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">");
		return htmlStr.toString();
	}

	/**
	 * 获得 html 的 css 样式
	 * @return String
	 * 				字符串形式的 css 样式
	 */
	public static String getHtmlCssStyle() {
		StringBuilder htmlStr = new StringBuilder();
		
		htmlStr.append("<style type=\"text/css\">\n");
		htmlStr.append("<!--\n");
		htmlStr.append(".griddiv{\n");
//		htmlStr.append("overflow-x:hidden;\n");
//		htmlStr.append("border:black 1px solid; \n");
		htmlStr.append("BACKGROUND: #F8F9FC;\n");
		htmlStr.append("position:relative;\n");
		htmlStr.append("text-align:center;\n");
		htmlStr.append("}\n");
		htmlStr.append(".title /* 新建表头样式 */{\n");
		htmlStr.append("height:30px;   \n");
		htmlStr.append("line-height:30px;  \n"); 
		htmlStr.append("overflow:hidden; \n");
		htmlStr.append("text-align:center;\n");
		htmlStr.append("BORDER-RIGHT: #555 1px solid;\n");
		htmlStr.append("BORDER-TOP: #fff 1px solid;\n");
		htmlStr.append("BORDER-BOTTOM: #555 1px solid;\n");
		htmlStr.append("BORDER-LEFT: #fff 1px solid;\n");
//		htmlStr.append("padding:2 1 2 2;\n");用以下4行代替
		htmlStr.append("padding-top:2;\n");
		htmlStr.append("padding-right:1;\n");
		htmlStr.append("padding-bottom:2;\n");
		htmlStr.append("padding-left:1;\n");
		
		htmlStr.append("BACKGROUND: #c3cfdf;\n");
		htmlStr.append("Font-Size:12pt;\n");
		htmlStr.append("Font-Family:Albany AMT,Arial,Lucida Sans;\n");
		htmlStr.append("WHITE-SPACE: nowrap;\n");
		htmlStr.append("position:relative;\n");
		htmlStr.append("display:inline-block;\n");
		htmlStr.append("}\n");
		htmlStr.append("td{\n");
		htmlStr.append("WHITE-SPACE: nowrap;\n");
		htmlStr.append("BORDER: #ddd 1px solid;\n");
		htmlStr.append("}\n");
		htmlStr.append(".cdata {\n");
//		htmlStr.append("padding:1 1 1 2;\n");
		htmlStr.append("padding-top:1;\n");
		htmlStr.append("padding-right:1;\n");
		htmlStr.append("padding-bottom:1;\n");
		htmlStr.append("padding-left:2;\n");
		htmlStr.append("Font-Size:9pt;\n");
		htmlStr.append("}\n");
		htmlStr.append(".noborder{\n");
		htmlStr.append("BORDER: #ddd 0px solid;\n");
		htmlStr.append("}\n");
		htmlStr.append("-->\n");
		htmlStr.append("</style>\n");
		
		return htmlStr.toString();
	}
	/**
	 * 获得 html 格式的 table 标签。
	 * @return String
	 * 				 html 格式的 table 标签。
	 */
	public static String getHtmlTable() {
		StringBuilder htmlStr = new StringBuilder();
		htmlStr.append("<table cellspacing='1' cellpadding='5' "
				+ "style='font-size:10pt;font-family:Albany AMT,Arial,Lucida Sans;margin:5px 30px;' width='100%'>\n");
		return htmlStr.toString();
	}

	/**
	 * 获得 html 格式的单元格，用于设置 table 中的单元格。
	 * @param value
	 *            单元格的值
	 * @param align
	 *            对齐方式。较常用的三个值："center","left","right"
	 * @param width
	 *            单元格的宽度
	 * @param colspan
	 *            单元格所占列数
	 * @param rowspan
	 *            单元格所占行数
	 * @return String
	 * 				html 格式的 td 标签
	 */
	public static String getHtmlTdBody(String value, String align, String width, int colspan, int rowspan) {
		StringBuilder htmlStr = new StringBuilder();
		htmlStr.append("<td align='" + align + "' width='" + width + "' bgcolor='#e9f0fa'");
		if (colspan > 1) {
			htmlStr.append(" colspan='" + colspan + "'");
		}
		if (rowspan > 1) {
			htmlStr.append(" rowspan='" + rowspan + "'");
		}
		htmlStr.append("><p>" + value + "</p></td>\n");
		return htmlStr.toString();
	}
	
	/**
	 * 获得 html 格式的单元格，用于设置 table 中的单元格。
	 * @param value
	 *            单元格的值
	 * @param align
	 *            对齐方式。较常用的三个值："center","left","right"
	 * @param colspan
	 *            单元格所占列数
	 * @param rowspan
	 *            单元格所占行数
	 * @return String
	 * 				html 格式的 td 标签
	 */
	public static String getHtmlTdBody(String value, String align, int colspan, int rowspan) {
		StringBuilder htmlStr = new StringBuilder();
		htmlStr.append("<td align='" + align + "' bgcolor='#e9f0fa'");
		if (colspan > 1) {
			htmlStr.append(" colspan='" + colspan + "'");
		}
		if (rowspan > 1) {
			htmlStr.append(" rowspan='" + rowspan + "'");
		}
		htmlStr.append("><p>" + value + "</p></td>\n");
		return htmlStr.toString();
	}
	
	/**
	 * 获得 html 格式的单元格，用于设置 table 中的单元格。
	 * 该方法在 value 的两端分别加了 num 个空格,目的是在固定表头的情况下保证列宽与表头列宽一致
	 * @param value
	 * 			单元格的值
	 * @param align
	 * 			对齐方式。较常用的三个值："center","left","right"
	 * @param colspan
	 * 			单元格所占列数
	 * @param rowspan
	 * 			单元格所占行数
	 * @param num
	 * 			在 value 两端所加空格的个数
	 * @return String
	 * 				html 格式的 td 标签
	 */
	public static String getHtmlTdBodyAddBlank(String value, String align, int colspan, int rowspan, int num) {
		StringBuilder htmlStr = new StringBuilder();
		htmlStr.append("<td align='" + align + "' bgcolor='#e9f0fa' ");
		htmlStr.append(" style='TEXT-ALIGN:" + align + ";font-size:10pt'");
		if (colspan > 1) {
			htmlStr.append(" colspan='" + colspan + "'");
		}
		if (rowspan > 1) {
			htmlStr.append(" rowspan='" + rowspan + "'");
		}
		htmlStr.append("><p>");
		if (align.equals("center")) {
			if (num > 0) {
				for (int i = 1; i <= num; i++) {
					htmlStr.append("&nbsp;");
				}
			}
			htmlStr.append(value);
			if (num > 0) {
				for (int i = 1; i <= num; i++) {
					htmlStr.append("&nbsp;");
				}
			}
		} else if (align.equals("left")) {
			htmlStr.append(value);
			if (num > 0) {
				for (int i = 1; i <= num * 2; i++) {
					htmlStr.append("&nbsp;");
				}
			}
		} else if (align.equals("right")) {
			if (num > 0) {
				for (int i = 1; i <= num * 2; i++) {
					htmlStr.append("&nbsp;");
				}
			}
			htmlStr.append(value);
		} else {
			htmlStr.append(value);
		}
		htmlStr.append("</p></td>\n");
		return htmlStr.toString();
	}
	
	/**
	 * 获得 html 格式的单元格，用于设置 table 中的单元格。
	 * 该方法在 value 的两端分别加了 num 个空格,目的是在固定表头的情况下保证列宽与表头列宽一致
	 * @param value
	 * 			单元格的值
	 * @param align
	 * 			对齐方式。较常用的三个值："center","left","right"
	 * @param colspan
	 * 			单元格所占列数
	 * @param rowspan
	 * 			单元格所占行数
	 * @param num
	 * 			在 value 两端所加空格的个数
	 * @return String
	 * 				html 格式的 td 标签
	 */
	public static String getHtmlTdBodyAddBlank(String value, String align, int colspan, int rowspan, int num, boolean isWrap) {
		StringBuilder htmlStr = new StringBuilder();
		htmlStr.append("<td align='" + align + "' bgcolor='#e9f0fa' ");
		htmlStr.append(" style='TEXT-ALIGN:" + align + ";font-size:10pt");
		if (isWrap) {
			htmlStr.append(";word-break:break-all");
		}
		htmlStr.append("'");
		if (colspan > 1) {
			htmlStr.append(" colspan='" + colspan + "'");
		}
		if (rowspan > 1) {
			htmlStr.append(" rowspan='" + rowspan + "'");
		}
		htmlStr.append("><p>");
		if (align.equals("center")) {
			if (num > 0) {
				for (int i = 1; i <= num; i++) {
					htmlStr.append("&nbsp;");
				}
			}
			htmlStr.append(value);
			if (num > 0) {
				for (int i = 1; i <= num; i++) {
					htmlStr.append("&nbsp;");
				}
			}
		} else if (align.equals("left")) {
			htmlStr.append(value);
			if (num > 0) {
				for (int i = 1; i <= num * 2; i++) {
					htmlStr.append("&nbsp;");
				}
			}
		} else if (align.equals("right")) {
			if (num > 0) {
				for (int i = 1; i <= num * 2; i++) {
					htmlStr.append("&nbsp;");
				}
			}
			htmlStr.append(value);
		} else {
			htmlStr.append(value);
		}
		htmlStr.append("</p></td>\n");
		return htmlStr.toString();
	}

	/**
	 * 获得 html 格式的单元格，用于设置 table 中的单元格。
	 * @param value
	 *            单元格的值
	 * @param align
	 *            对齐方式。较常用的三个值："center","left","right"
	 * @param width
	 *            单元格的宽度
	 * @param colspan
	 *            单元格所占列数
	 * @param rowspan
	 *            单元格所占行数
	 * @param fontsize
	 *            字体大小
	 * @return String
	 * 				html 格式的 td 标签
	 */
	public static String getHtmlTdBody(String value, String align, String width, int colspan, int rowspan, int fontsize) {
		StringBuilder htmlStr = new StringBuilder();
		htmlStr.append("<td align='" + align + "' width='" + width + "' bgcolor='#e9f0fa'");
		if (colspan > 1) {
			htmlStr.append(" colspan='" + colspan + "'");
		}
		if (rowspan > 1) {
			htmlStr.append(" rowspan='" + rowspan + "'");
		}
		htmlStr.append("><p style='font-size: " + fontsize + "pt'>" + value + "</p></td>\n");
		return htmlStr.toString();
	}

	/**
	 * 获得 html 格式的单元格，用于设置 table 中的单元格。
	 * @param value
	 *            单元格的值
	 * @param align
	 *            对齐方式。较常用的三个值："center","left","right"
	 * @param width
	 *            单元格的宽度
	 * @param colspan
	 *            单元格所占列数
	 * @param rowspan
	 *            单元格所占行数
	 * @param fontsize
	 *            字体大小
	 * @param id
	 *            单元格的 id
	 * @param display
	 *            显示框类型
	 * @return String
	 * 				html 格式的 td 标签
	 */
	public static String getHtmlTdBody(String value, String align, String width, int colspan, int rowspan,
			int fontsize, String id, String display) {
		StringBuilder htmlStr = new StringBuilder();
		htmlStr.append("<td align='" + align + "' width='" + width + "' bgcolor='#e9f0fa' id='" + id
				+ "' style='display:" + display + "; ' ");
		if (colspan > 1) {
			htmlStr.append(" colspan='" + colspan + "'");
		}
		if (rowspan > 1) {
			htmlStr.append(" rowspan='" + rowspan + "'");
		}
		htmlStr.append("><p style='font-size: " + fontsize + "pt'>" + value + "</p></td>\n");
		return htmlStr.toString();
	}

	/**
	 * 获得 html 格式的单元格，用于设置 table 中的单元格。
	 * @param value
	 *            单元格的值
	 * @param align
	 *            对齐方式。较常用的三个值："center","left","right"
	 * @param width
	 *            单元格的宽度
	 * @param color
	 *            单元格的背景颜色
	 * @param colspan
	 *            单元格所占列数
	 * @param rowspan
	 *            单元格所占行数
	 * @param bold
	 *            是否加粗
	 * @return String
	 * 				html 格式的 td 标签
	 */
	public static String getHtmlTdBody(String value, String align, String width, String color, int colspan,
			int rowspan, boolean bold) {
		StringBuilder htmlStr = new StringBuilder();
		htmlStr.append("<td align='" + align + "' width='" + width + "' bgcolor='" + color + "'");
		if (colspan > 1) {
			htmlStr.append(" colspan='" + colspan + "'");
		}
		if (rowspan > 1) {
			htmlStr.append(" rowspan='" + rowspan + "'");
		}
		if (bold) {
			htmlStr.append("><b>" + value + "</b></td>\n");
		} else {
			htmlStr.append("><p>" + value + "</p></td>\n");
		}
		return htmlStr.toString();
	}

	/**
	 * 返回带颜色的字符串.
	 * @param data
	 *            任意字符串
	 * @param color
	 *            颜色名(如“red”),多个颜色之间以逗号分隔
	 * @return String
	 * 				带颜色的字符串.
	 */
	public static String getStrByColor(String data, String color) {
		String result = "";
		if (!StringUtilsBasic.checkNull(color)) {
			return data;
		} else {
			if (color.indexOf(",") > -1) {
				result = "<font color='(" + color + ")'>" + data + "</font>";
			} else {
				result = "<font color='" + color + "'>" + data + "</font>";
			}
		}
		return result;
	}
	
	/**
	 * 返回换行后的字符串，用于表头固定的表格中的某一列
	 * @param value
	 * 			要处理的字符串
	 * @param perRowLen
	 * 			每行的长度
	 * @return ;
	 */
	public static String getWrapString(String value, int perRowLen) {
		int inputLen = value.getBytes().length;
		if (inputLen > perRowLen) {

			String input2 = value.replace("，", "").replace("。", "").replace("！", "").replace("……", "").replace("［", "")
				.replace("］", "").replace("（", "").replace("）", "").replace("“", "").replace("”", "").replace("？", "")
				.replace("、", "").replace("；", "").replace("：", "").replace("‘", "").replace("’", "").replace("《", "")
				.replace("》", "").replace("｛", "").replace("｝", "").replace("——", "");
			if (input2.length() == input2.getBytes().length && value.contains(" ")) {
				//input 为英文
				//将每个单词放进数组中
				String[] inputWord = value.split(" ");
				String text = "";
				//存放每行的数据
				ArrayList<String> txtList = new ArrayList<String>();
				String[] signArr = new String[]{",", "，", ".", "。", "!", "！", "……", "?", "？", "/"};
				for (int i = 0; i < inputWord.length; i++) {
					String str = inputWord[i];
					if (text.length() < perRowLen && text.length() + str.length() > perRowLen) {
						boolean isContainSign = false;
						//判断str是否包含标点符号
						for (String sign : signArr) {
							if (str.contains(sign)) {
								String str1 = str.substring(0, str.lastIndexOf(sign) + 1);
								txtList.add(text + " " + str1);
								text = str.substring(str.lastIndexOf(sign) + 1);
								isContainSign = true;
								break;
							}
						}
						if (!isContainSign && text.length() + str.length() > perRowLen + 5) {
								txtList.add(text);
								text = str;
						} else if (!isContainSign && text.length() == 0) {
								text += str;
								txtList.add(text);
								text = "";
							
						} else if (!isContainSign) {
							text += " " + str;
							txtList.add(text);
							text = "";
						}
					} else if (text.length() < perRowLen) {
						if (text.length() == 0) {
							text += str;
						} else {
							text += " " + str;
						}
						if (i == inputWord.length - 1) {
							txtList.add(text);
							text = "";
						}
					} else {
						boolean isSign = false;
						for (String sign : signArr) {
							if (str.contains(sign)) {
								String str1 = str.substring(0, str.lastIndexOf(sign) + 1);
								txtList.add(text + " " + str1);
								text = str.substring(str.lastIndexOf(sign) + 1);
								isSign = true;
								break;
							}
						}
						if (!isSign) {
							text += " " + str;
							txtList.add(text);
							text = "";
						}
					}
				}
				value = "";
				for (String s : txtList) {
					value = value.concat(s + "<br>");
				}
				if (value.length() > 4) {
					value = value.substring(0, value.length() - 4);
				} 
			} else {
				//input 为中文或者一个无空格的英文字符串
				String subStr = "";
//				if (title.matches("[\\u4e00-\\u9fbb]+")) {
					char[] ch = value.toCharArray();
					double len = 0;
					for (char c : ch) {
						Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
						//如果第一个字符是标点符号，则该标点符号移到上一行
						if (len == 0 && (ub == Character.UnicodeBlock.GENERAL_PUNCTUATION  //判断中文的“号  
								|| ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION  //判断中文的。号  
								|| ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS //判断中文的，号  
								)) {
							if (subStr.length() > 4) {
								subStr = subStr.substring(0, subStr.length() - 4) + c + "<br>";
							} else {
								subStr = c + "";
							}
							continue;
						} else {
							subStr += c;
						}
						if ((int) c >= 0x4E00 && (int) c <= 0x9FA5) {
							len += 2.5;
						} else {
							len += 1;
						}
						if (len >= perRowLen) {
							subStr += "<br>";
							len = 0;
						}
					}
				value = subStr;
			}
		} 
		
		return value;
	}
}

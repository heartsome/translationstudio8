package net.heartsome.cat.converter.msexcel2007.reader;

import java.util.ArrayList;
import java.util.List;

public class ReaderUtil {

	public static String appendSegStyle(String seg, List<Object[]> styleList) {
		StringBuffer bf = new StringBuffer(seg);
		StringBuffer result = new StringBuffer();
		int styledId = 1;
		for (Object[] obj : styleList) {
			int start = (Integer) obj[0];
			int end = (Integer) obj[1];
			String style = (String) obj[2];
			result.append("<g id=\""+ styledId++ +"\" ").append(style).append(">");
			result.append(bf.substring(start, end)).append("</g>");
		}

		return result.toString();

	}

	public static List<Object[]> getSegStyle(List<Object[]> styles, String seg, String content) {
		List<Object[]> result = new ArrayList<Object[]>();

		int segSos = content.indexOf(seg);
		int segEos = segSos + seg.length();
		int baseSos = segSos;

		for (Object[] obj : styles) {
			Object[] temp = obj.clone();
			int styleSos = (Integer) temp[0];
			int styleEos = (Integer) temp[1];

			if (segSos == styleSos && segEos == styleEos) {
				temp[0] = styleSos - baseSos;
				temp[1] = styleEos - baseSos;
				result.add(temp);
				break;
			}
			if (segSos <= styleSos) {
				temp[0] = styleSos - baseSos;
				if (segEos < styleEos) {
					temp[1] = segEos - baseSos;
					result.add(temp);
					break;
				} else if (segEos > styleEos) {
					temp[1] = styleEos - baseSos;
					result.add(temp);
					segSos = styleEos;
				}
			}
			if (segSos > styleSos && segSos < styleEos) {
				temp[0] = segSos - baseSos;
				if (segEos <= styleEos) {
					temp[1] = segEos - baseSos;
					result.add(temp);
					break;
				} else if (styleEos < segEos) {
					temp[1] = styleEos - baseSos;
					result.add(temp);
					segSos = styleEos;
				}
			}

		}
		return result;
	}

	/**
	 * This method cleans the text that will be stored inside <ph>elements in the XLIFF file *.
	 * @param line
	 *            the line
	 * @return the string
	 */
	public static String cleanTag(String line) {
		String s = line;
		int control = s.indexOf("&");
		while (control != -1) {
			s = s.substring(0, control) + "&amp;" + s.substring(control + 1);
			if (control < s.length()) {
				control++;
			}
			control = s.indexOf("&", control);
		}

		control = s.indexOf("<");
		while (control != -1) {
			s = s.substring(0, control) + "&lt;" + s.substring(control + 1);
			if (control < s.length()) {
				control++;
			}
			control = s.indexOf("<", control);
		}

		control = s.indexOf(">");
		while (control != -1) {
			s = s.substring(0, control) + "&gt;" + s.substring(control + 1);
			if (control < s.length()) {
				control++;
			}
			control = s.indexOf(">", control);
		}

		return s;
	}
	
	public static String cleanAttribute(String line){
		String s = cleanTag(line);
		int control = s.indexOf('"');
		while (control != -1) {
			s = s.substring(0, control) + "&quot;" + s.substring(control + 1);
			if (control < s.length()) {
				control++;
			}
			control = s.indexOf('"', control);
		}
		
		return s;
	}
	
	public static String reCleanAttribute(String line){
		String s = line;
		s = s.replaceAll("&lt;", "<");
		s = s.replaceAll("&gt;", ">");
		s = s.replaceAll("&quot;", "\"");
		s = s.replaceAll("&amp;", "&");
		return s;
	}
	
	public static String reCleanTag(String line) {
		String s = line;
		s = s.replaceAll("&lt;", "<");
		s = s.replaceAll("&gt;", ">");
		s = s.replaceAll("&amp;", "&");
		return s;
	}
}

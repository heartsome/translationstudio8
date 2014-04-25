/**
 * StringSegmenter.java
 *
 * Version information :
 *
 * Date:Jan 14, 2010
 *
 * Copyright notice :
 */
package net.heartsome.cat.converter;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;

import net.heartsome.cat.converter.resource.Messages;
import net.heartsome.cat.converter.util.ConverterUtils;
import net.heartsome.xml.Catalogue;
import net.heartsome.xml.Document;
import net.heartsome.xml.Element;
import net.heartsome.xml.SAXBuilder;

import org.xml.sax.SAXException;

/**
 * The Class StringSegmenter.
 * @author John Zhu
 * @version
 * @since JDK1.6
 */
public class StringSegmenter {

	/** The rules. */
	Vector<Element> rules;

	/** The tags. */
	Hashtable<String, String> tags;

	// private StringSegmenter() {
	// // do not allow instantiation without parameters
	// }

	/**
	 * Instantiates a new string segmenter.
	 * @param srxFile
	 *            the srx file
	 * @param language
	 *            the language
	 * @param catalogFile
	 *            the catalog file
	 * @throws SAXException
	 *             the SAX exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws ParserConfigurationException
	 *             the parser configuration exception
	 * @throws ConverterException
	 *             the converter exception
	 */
	public StringSegmenter(String srxFile, String language, String catalogFile) throws SAXException, IOException,
			ParserConfigurationException, ConverterException {
		if (srxFile == null || "".equals(srxFile)) {
			ConverterUtils.throwConverterException(Activator.PLUGIN_ID, Messages.getString("converter.StringSegmenter.msg1"));
		}

		if (catalogFile == null || "".equals(catalogFile)) {
			ConverterUtils.throwConverterException(Activator.PLUGIN_ID, Messages.getString("converter.StringSegmenter.msg2"));
		}

		SAXBuilder builder = new SAXBuilder();
		Catalogue catalogue = new Catalogue(catalogFile);
		builder.setValidating(true);
		builder.setEntityResolver(catalogue);
		Document doc = builder.build(srxFile);
		Element root = doc.getRootElement();
		Element body = root.getChild("body"); //$NON-NLS-1$

		Hashtable<String, String> rulenames = new Hashtable<String, String>();
		rules = new Vector<Element>();

		// check if there are map rules for this language
		Element maprules = body.getChild("maprules"); //$NON-NLS-1$
		if (maprules != null) {
			List<Element> map = maprules.getChildren();
			Iterator<Element> it = map.iterator();
			while (it.hasNext()) {
				Element maprule = it.next();
				List<Element> langmap = maprule.getChildren();
				for (int i = 0; i < langmap.size(); i++) {
					Element languagemap = langmap.get(i);
					if (Pattern.matches(languagemap.getAttributeValue("languagepattern", ""), language)) { //$NON-NLS-1$ //$NON-NLS-2$
						rulenames.put(languagemap.getAttributeValue("languagerulename", "--no value--"), ""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					}
				}
			}
		}

		if (rulenames.size() < 1) {
			// no rules to load
			MessageFormat mf = new MessageFormat(Messages.getString("converter.StringSegmenter.msg3"));
			Object[] args = { language };
			System.err.println(mf.format(args));
			args = null;

			return;
		}

		// now get the rules
		Element languagerules = body.getChild("languagerules"); //$NON-NLS-1$
		if (languagerules != null) {
			List<Element> langrules = languagerules.getChildren("languagerule"); //$NON-NLS-1$
			Iterator<Element> it = langrules.iterator();
			while (it.hasNext()) {
				Element languagerule = it.next();
				if (rulenames.containsKey(languagerule.getAttributeValue("languagerulename", "--no name--"))) { //$NON-NLS-1$ //$NON-NLS-2$
					List<Element> rulelist = languagerule.getChildren("rule"); //$NON-NLS-1$
					for (int i = 0; i < rulelist.size(); i++) {
						rules.add(rulelist.get(i));
					}
				}
			}
		}
	}

	/**
	 * Segment.
	 * @param string
	 *            the string
	 * @return the string[]
	 */
	public String[] segment(String string) {

		if (string.trim().equals("") || rules.size() == 0) { //$NON-NLS-1$
			String[] result = new String[1];
			result[0] = string;
			return result;
		}

		Vector<String> strings = new Vector<String>();
		tags = new Hashtable<String, String>();
		strings.add(prepareString(string));

		// now segment the strings
		int rulessize = rules.size();
		for (int i = 0; i < rulessize; i++) {
			Element rule = rules.get(i);
			boolean breaks = rule.getAttributeValue("break", "yes").equalsIgnoreCase("yes"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			Element before = rule.getChild("beforebreak"); //$NON-NLS-1$
			Element after = rule.getChild("afterbreak"); //$NON-NLS-1$
			String beforexp = ""; //$NON-NLS-1$
			if (before != null) {
				beforexp = before.getText();
			}
			String afterxp = ""; //$NON-NLS-1$
			if (after != null) {
				afterxp = after.getText();
			}
			if (breaks) {
				// This rule tries to break segments
				Vector<String> temp = new Vector<String>();
				for (int j = 0; j < strings.size(); j++) {
					String[] parts = split(strings.get(j), beforexp, afterxp);
					for (int k = 0; k < parts.length; k++) {
						temp.add(parts[k]);
					}
				}
				strings = null;
				strings = temp;
			} else {

				// strings = connect3(strings,beforexp,afterxp);

				// This rule marks exceptions, like abbreviations
				Vector<String> temp = new Vector<String>();
				String current = strings.get(0);
				for (int j = 1; j < strings.size(); j++) {
					String next = strings.get(j);
					if (endsWith(current, beforexp) && startsWith(next, afterxp)) {
						current = current + next;
					} else {
						temp.add(current);
						current = next;
					}
				}
				temp.add(current);
				strings = null;
				strings = temp;

			}
		}

		String[] result = new String[strings.size()];
		for (int h = 0; h < strings.size(); h++) {
			result[h] = cleanup(strings.get(h));
		}
		
		return analysisBlank(result);
	}

	// private Vector<String> connect3(Vector<String> strings, String beforexp,
	// String afterxp) {
	// Vector<String> temp = new Vector<String>();
	// StringBuilder sb = new StringBuilder();
	// ArrayList<String[]> stringsList = new ArrayList<String[]>();
	// String[] stringsData;
	// for (int i = 0; i < strings.size(); i++) {
	// stringsData = new String[2];
	// stringsData[0] = "" + sb.length();
	// stringsData[1] = "" + (sb.length() + strings.get(i).length());
	// sb.append(strings.get(i));
	// stringsList.add(stringsData);
	// }
	// Pattern p = Pattern.compile(beforexp);
	// Matcher m;
	// int start = 0;
	// int curEnd = 0;
	// boolean addFlag = false;
	// for (int j = 0; j < stringsList.size();) {
	// for (int i = strings.size() - 1; i >= 0; i--) {
	// if (Integer.parseInt(stringsList.get(i)[0]) <= start) {
	// break;
	// }
	// curEnd = Integer.parseInt(stringsList.get(i)[1]);
	// m = p.matcher(sb.substring(start, curEnd));
	// if (m.matches()) {
	// int end = start + m.end();
	// if (startsWith(sb.substring(end), afterxp)) {
	// if (end == curEnd) {
	// if (i == strings.size() - 1) {
	// temp.add(sb.substring(start, end));
	// start = curEnd;
	// addFlag = true;
	// j = i + 1;
	// break;
	// } else {
	// temp.add(sb.substring(start, end)
	// + strings.get(i + 1));
	// start = Integer
	// .parseInt(stringsList.get(i + 1)[1]);
	// addFlag = true;
	// j = i + 2;
	// break;
	// }
	// } else {
	// temp.add(sb.substring(start, start + end));
	// if (i == strings.size() - 1) {
	// temp.add(sb.substring(end));
	// addFlag = true;
	// j = i + 1;
	// break;
	// } else {
	// temp.add(sb.substring(end, Integer
	// .parseInt(stringsList.get(i + 1)[0])));
	// addFlag = true;
	// start = Integer
	// .parseInt(stringsList.get(i + 1)[0]);
	// break;
	// }
	// }
	// }
	// }
	// }
	// if (!addFlag) {
	// start = Integer.parseInt(stringsList.get(j)[1]);
	// temp.add(strings.get(j));
	// j++;
	// }
	// addFlag = false;
	// }
	// return temp;
	// }

	/**
	 * Cleanup.
	 * @param string
	 *            the string
	 * @return the string
	 */
	private String cleanup(String string) {
		Enumeration<String> keys = tags.keys();
		while (keys.hasMoreElements()) {
			String key = keys.nextElement();
			int index = string.indexOf(key);
			if (index != -1) {
				string = string.substring(0, index) + tags.get(key) + string.substring(index + 1);
			}
		}
		return string;
	}
	
	/**
	 * 处理分段后，有空格单独成段的问题	--robert	2013-05-24
	 * @param result
	 * @return
	 */
	private String[] analysisBlank(String[] result){
		List<String> resultList = new ArrayList<String>();
		String curStr = "";
		String nextStr = "";
		for (int i = 0; i < result.length; i++) {
			curStr = result[i];
			if (i + 1 < result.length) {
				nextStr = result[i + 1];
				// 若下一个分段为空格，就将之合并至上一个分段。
				if (nextStr.length() > 0 && nextStr.trim().length() == 0) {
					resultList.add(curStr + nextStr);
					i ++;
					continue;
				}
			}
			resultList.add(curStr);
		}
		return resultList.toArray(new String[resultList.size()]);
	}

	/**
	 * Prepare string.
	 * @param string
	 *            the string
	 * @return the string
	 */
	private String prepareString(String string) {

		int start = string.indexOf("<ph"); //$NON-NLS-1$
		int end = string.indexOf("</ph>"); //$NON-NLS-1$

		int k = 0;
		while (start != -1 && end != -1) {
			if (start > end) {
				break;
			}
			String tag = string.substring(start, end + 5);
			string = string.substring(0, start) + (char) ('\uE000' + k) + string.substring(end + 5);
			tags.put("" + (char) ('\uE000' + k), tag); //$NON-NLS-1$
			k++;
			start = string.indexOf("<ph"); //$NON-NLS-1$
			end = string.indexOf("</ph>"); //$NON-NLS-1$
		}

		StringBuffer buffer = new StringBuffer();
		StringBuffer element = new StringBuffer();
		int length = string.length();
		boolean inElement = false;
		for (int i = 0; i < length; i++) {
			char c = string.charAt(i);
			if (c == '<' && string.indexOf(">", i) != -1) { //$NON-NLS-1$
				inElement = true;
				int a = string.indexOf("<", i + 1); //$NON-NLS-1$
				int b = string.indexOf(">", i + 1); //$NON-NLS-1$
				if (a != -1 && a < b) {
					inElement = false;
				}
				if (i < length - 1 && !Character.isLetter(string.charAt(i + 1)) && string.charAt(i + 1) != '/') {
					inElement = false;
				}
			}
			if (inElement) {
				element.append(c);
			} else {
				buffer.append(c);
			}
			if (c == '>' && inElement) {
				inElement = false;
				tags.put("" + (char) ('\uE000' + k), element.toString()); //$NON-NLS-1$
				buffer.append((char) ('\uE000' + k));
				element = null;
				element = new StringBuffer();
				k++;
			}
		}

		return buffer.toString();
	}

	/**
	 * Ends with.
	 * @param string
	 *            the string
	 * @param exp
	 *            the exp
	 * @return true, if successful
	 */
	private boolean endsWith(String string, String exp) {
		Pattern p = Pattern.compile(exp);
		Matcher m = p.matcher(string);
		String[] pieces = split(string, exp);
		// if ( pieces.length == 1 && m.lookingAt()) {
		// return false;
		// }

		if (pieces.length == 1) {
			while (m.find()) {
				if (string.endsWith(m.group())) {
					return true;
				}
			}
			return false;
		}

		String last = pieces[pieces.length - 1];
		String[] parts = string.split(exp);
		if (parts.length == 0) {
			parts = new String[1];
			parts[0] = ""; //$NON-NLS-1$
		}
		String lastPart = parts[parts.length - 1];
		boolean result = !lastPart.equals(last);
		return result;
	}

	/**
	 * Split.
	 * @param string
	 *            the string
	 * @param beforexp
	 *            the beforexp
	 * @param afterxp
	 *            the afterxp
	 * @return the string[]
	 */
	private String[] split(String string, String beforexp, String afterxp) {
		String[] strings = split(string, beforexp);
		if (strings.length == 1 || afterxp.equals("")) { //$NON-NLS-1$
			return strings;
		}
		Vector<String> parts = new Vector<String>();
		String current = strings[0];
		for (int i = 1; i < strings.length; i++) {
			if (startsWith(strings[i], afterxp)) {
				parts.add(current);
				current = strings[i];
			} else {
				current = current + strings[i];
			}
		}
		parts.add(current);
		String[] result = new String[parts.size()];
		for (int i = 0; i < parts.size(); i++) {
			result[i] = parts.get(i);
		}
		return result;
	}

	/**
	 * Starts with.
	 * @param string
	 *            the string
	 * @param exp
	 *            the exp
	 * @return true, if successful
	 */
	private boolean startsWith(String string, String exp) {
		Pattern p = Pattern.compile(exp);
		Matcher m = p.matcher(string);
		if (m.lookingAt()) {
			return true;
		}
		return false;
	}

	/**
	 * Split.
	 * @param string
	 *            the string
	 * @param exp
	 *            the exp
	 * @return the string[]
	 */
	private String[] split(String string, String exp) {
		Pattern p = Pattern.compile(exp);
		if (exp.equals("") || p.split(string).length == 1) { //$NON-NLS-1$
			String[] result = new String[1];
			result[0] = string;
			return result;
		}

		Vector<CharSequence> parts = new Vector<CharSequence>();
		while (p.split(string).length != 1) {
			String[] halves = p.split(string, 2);
			parts.add(string.subSequence(0, string.lastIndexOf(halves[1])));
			string = halves[1];
		}
		if (!string.equals("")) { //$NON-NLS-1$
			parts.add(string);
		}
		String[] result = new String[parts.size()];
		for (int i = 0; i < parts.size(); i++) {
			result[i] = (String) parts.get(i);
		}
		return result;
	}

	/**
	 * The main method.
	 * @param args
	 *            the arguments
	 * @throws SAXException
	 *             the SAX exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws ParserConfigurationException
	 *             the parser configuration exception
	 * @throws ConverterException
	 *             the converter exception
	 */
	public static void main(String[] args) throws SAXException, IOException, ParserConfigurationException,
			ConverterException {
		StringSegmenter segmenter = new StringSegmenter("srx/default_rules.srx", "en", //$NON-NLS-1$ //$NON-NLS-2$
				"catalogue/catalogue.xml"); //$NON-NLS-1$
		//		String[] result = segmenter.segment("The exemplary <ph>distinction of</ph> Heartsome's translation " + //$NON-NLS-1$
		//				"service is rare and unique in the industry. It is intentional and purposefully " + //$NON-NLS-1$
		//				"dedicated to servicing clients that are avowedly discerning in the value of " + //$NON-NLS-1$
		//				"quality translation. These clients expect our services to measurably add value " + //$NON-NLS-1$
		//				"in a manner that will serve to complete their competitive advantage. We are " + //$NON-NLS-1$
		//				"possibly the one and only translation service in the industry capable of adding " + //$NON-NLS-1$
		//				"recognition, credence and value to brand consciousness and exclusivity."); //$NON-NLS-1$
		// for (int i=0 ; i<result.length ; i++) {
		//			System.out.println(i + ". " + result[i]); //$NON-NLS-1$
		// }

		segmenter
				.endsWith(
						"Ks.",
						"RegR|AR|RR|KzlR|KmzlR|KommR|KR|ÖkR|MedR|MR|OMedR|OMR|VetR|Techn\\.R|BauR h\\.c\\.|BergR h\\.c\\.|ForstR h\\.c\\.|HR|Prof\\.|Ksch\\.|Ks\\.|Univ\\.Prof\\.|(OSt|St|OS|S)R");
	}

}

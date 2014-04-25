package net.heartsome.cat.converter.mif.common;

public class ReaderUtil {

	// /** The charmap. */
	// private Hashtable<String, String> charmap;

	public ReaderUtil(String iniFile) /* throws SAXException, IOException */{
		// loadCharMap(iniFile);
	}

	public String cleanString(String s) {
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

		s = s.replace("\\q", "'");
		s = s.replace("\\Q", "`");
		s = s.replace("\\\\", "\\");

		// 以下代码注释，是因为nonstandard ASCII不做转义处理
		// control = s.indexOf("\\x");
		// while (control != -1) {
		// String code = s.substring(control + 2, s.indexOf(" ", control));
		//
		// String character = "" + getCharValue(Integer.valueOf(code, 16).intValue());
		// if (!character.equals("")) {
		// s = s.substring(0, control) + character + s.substring(1 + s.indexOf(" ", control));
		// }
		// control++;
		// control = s.indexOf("\\x", control);
		// }

		return s;
	}

	/**
	 * This method cleans the text that will be stored inside <ph>elements in the XLIFF file *.
	 * @param line
	 *            the line
	 * @return the string
	 */
	public String cleanTag(String line) {
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

	/*
	 * 此部分代码注释，是因为nonstandard ASCII不做转义处理 /** Load char map.
	 * 
	 * @throws SAXException the SAX exception
	 * 
	 * @throws IOException Signals that an I/O exception has occurred.
	 * 
	 * @throws ParserConfigurationException
	 *//*
		 * private void loadCharMap(String iniFile) throws SAXException, IOException { SAXBuilder cbuilder = new
		 * SAXBuilder(); Document cdoc = cbuilder.build(iniFile); charmap = new Hashtable<String, String>(); Element
		 * croot = cdoc.getRootElement(); List<Element> codes = croot.getChildren("char"); //$NON-NLS-1$
		 * Iterator<Element> it = codes.iterator(); while (it.hasNext()) { Element e = it.next();
		 * charmap.put(e.getAttributeValue("code"), e.getText()); //$NON-NLS-1$ e = null; } it = null; codes = null;
		 * cdoc = null; cbuilder = null; }
		 * 
		 * /** Gets the char value.
		 * 
		 * @param value the value
		 * 
		 * @return the char value
		 *//*
			 * private char getCharValue(int value) { switch (value) { case 0x04: return '\u0004'; case 0x05: return
			 * '\u0005'; case 0x08: return '\u0008'; case 0x09: return '\u0009'; case 0x0a: return '\u0010'; case 0x10:
			 * return '\u0016'; case 0x11: return '\u0017'; case 0x12: return '\u0018'; case 0x13: return '\u0019'; case
			 * 0x14: return '\u0020'; case 0x15: return '\u0021'; default: break; } if (value > 0x7f) { String key =
			 * "\\x" + Integer.toHexString(value); //$NON-NLS-1$ if (charmap.containsKey(key)) { String result =
			 * charmap.get(key); if (result.length() > 0) { return result.charAt(0); } result = null; } key = null; }
			 * return (char) value; }
			 */
}

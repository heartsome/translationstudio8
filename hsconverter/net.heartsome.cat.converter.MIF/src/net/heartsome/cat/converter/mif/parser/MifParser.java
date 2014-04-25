package net.heartsome.cat.converter.mif.parser;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.MessageFormat;
import java.util.Stack;

import net.heartsome.cat.converter.mif.bean.Frame;
import net.heartsome.cat.converter.mif.bean.Marker;
import net.heartsome.cat.converter.mif.bean.MifParseBuffer;
import net.heartsome.cat.converter.mif.bean.Page;
import net.heartsome.cat.converter.mif.bean.Table;
import net.heartsome.cat.converter.mif.bean.TextFlow;
import net.heartsome.cat.converter.mif.bean.TextRect;
import net.heartsome.cat.converter.mif.common.MifParseException;
import net.heartsome.cat.converter.mif.common.UnSuportedFileExcetption;
import net.heartsome.cat.converter.mif.resource.Messages;

public class MifParser {
	/** File start offset */
	private int sos;

	/** File end offset */
	private int eos;

	/** The temporary offset */
	private int tos;

	/** File content in char array */
	char[] doc;

	/** The MIF parse buffer */
	MifParseBuffer mpbf;

	/** Current character */
	private char ch;

	/** MIF reader */
	private MifReader r;

	private Stack<String> nameStack;

	public MifParser() {
		r = new MifReader();
		mpbf = new MifParseBuffer();
		nameStack = new Stack<String>();
	}

	public void parseFile(String file, String encoding) throws IOException, MifParseException, UnSuportedFileExcetption {

		File path = new File(file);
		FileInputStream in = new FileInputStream(path);
		InputStreamReader inr = new InputStreamReader(in, encoding);
		BufferedReader bfr = new BufferedReader(inr);

		char[] doc = new char[(int) path.length()];
		bfr.read(doc);

		in.close();
		bfr.close();
		inr.close();

		setDoc(doc);
		parse();
	}

	public void setDoc(char[] doc) throws MifParseException {
		if (doc.length == 0) {
			throw new MifParseException("Invalidate file");
		}
		this.doc = doc;
		sos = 0;
		eos = doc.length;
		decideEncoding();

		validateFile();
	}

	public void parse() throws EOFException, MifParseException {
		ch = r.getCharAfterIgnore();
		int start = 0; // the top level element start offset
		int markerFlg = 0; // the embed element start offset
		while (true) {
			switch (ch) {
			case '<':
				tos = sos - 1; // the statement start offset
				String name = getStatmentName();
				nameStack.push(name);
				// System.out.println(name);
				if (name.equals("aframes")) {
					start = tos;
				} else if (name.equals("tbl")) {
					start = tos;
				} else if (name.equals("page")) {
					start = tos;
				} else if (name.equals("textflow")) {
					start = tos;
				} else if (name.equals("importobject")) {
					// the ImportObject statement individual parse
					// the ImportObject statement contained specification of object data
					parseImportObejct();
				} else if (name.equals("marker")) {
					// parse the marker element and get the index and cross-ref info
					// This implement at 2012-08-15 for new requirement of extract the index text
					markerFlg = tos;
				} else if (name.equals("mtype") && markerFlg != 0) {
					// check marker type
					String v = getContentEndBy('>').trim();
					if (!v.equals("2")) {
						// 9: cross-ref , 2 : index
						markerFlg = 0;
					}
				} else if (name.equals("mtext") && markerFlg != 0) {
					// extract marker text
					ch = r.getCharAfterIgnore();
					if (ch == '`') {
						int strsos = sos;
						String text = getValue();
						int streos = sos;
						if (text.length() != 0) {
							Marker m = new Marker(strsos, streos, text);
							mpbf.appendMarker(m);
							markerFlg = 0;
						}
					}
				}
				break;
			case '`':
				getValue(); // skip the value
				break;
			case '>':
				if (doc[sos - 2] == '\\') {
					break;
				}
				if (nameStack.isEmpty()) {
					throw new MifParseException(Messages.getString("mif.Mif2Xliff.mismatchStartOrEndTag")
							+ formatLineNumber());
				}
				name = nameStack.pop();
				if (name.equals("aframes")) {
					// System.out.println(new String(doc,start,sos - start));
					int tempEos = eos;
					eos = sos;
					sos = start;
					parseAframes();
					eos = tempEos;
					start = 0; // rest the record
				} else if (name.equals("tbl")) {
					int tempEos = eos;
					eos = sos;
					sos = start;
					Table tbl = parseTables();
					if (tbl.validate()) {
						mpbf.appendTbale(tbl);
					}
					eos = tempEos;
					start = 0; // rest the record
				} else if (name.equals("page")) {
					int tempEos = eos;
					eos = sos;
					sos = start;
					Page page = parsePage();
					if (page.validate()) {
						mpbf.appendPage(page);
					}
					eos = tempEos;
					start = 0; // rest the record
				} else if (name.equals("textflow")) {
					int tempEos = eos;
					eos = sos;
					sos = start;
					TextFlow tf = parseTextFlow();
					if (tf.validate()) {
						mpbf.appendTextFlow(tf);
					}
					eos = tempEos;
					start = 0; // rest the record
				}
				break;
			default:

				break;
			}

			if (sos == eos) {
				return;
			}
			ch = r.getChar();
		}
	}

	/**
	 * 目前只处理了UTF-8编码的BOM信息
	 * @throws MifParseException ;
	 */
	private void decideEncoding() throws MifParseException {
		if (doc.length == 0) {
			throw new MifParseException("当前文件是一个空文件");
		}
		StringBuffer bf = new StringBuffer();
		for (int i = 0; i < doc.length; i++) {
			char ch = doc[i];
			if (ch == '\n' && bf.length() != 0) {
				break;
			}
			bf.append(ch);
		}
		byte[] bys = bf.toString().getBytes();

		if (bys[0] == -17) {
			if (bys[1] == -69 && bys[2] == -65) {
				sos += 1;
			}
		}
	}

	private void validateFile() throws MifParseException {
		try {
			ch = r.getCharAfterIgnore();

			if (ch != '<') {
				throw new MifParseException(Messages.getString("mif.Mif2Xliff.fileNotStartProperly")
						+ formatLineNumber());
			}
			tos = sos;
			String name = getContentEndBy(' ').toLowerCase();
			if (!name.equals("miffile")) {
				throw new MifParseException(Messages.getString("mif.Mif2Xliff.fileNotStartProperly")
						+ formatLineNumber(tos));
			}

			tos = sos;
			String version = getContentEndBy('>');
			try {
				float f = Float.parseFloat(version);
				if (f == 7.00 || f == 8.00 || f == 9.00 || f == 10.0) {

				} else {
					throw new MifParseException(MessageFormat.format(Messages.getString("mif.Mif2Xliff.unsuportVersion") + formatLineNumber(tos), f));
				}
			} catch (NumberFormatException e) {
				throw new MifParseException(Messages.getString("mif.Mif2Xliff.invalidateVersionInfo")
						+ formatLineNumber());
			}
			if (r.getChar() != '>') {
				throw new MifParseException(Messages.getString("mif.Mif2Xliff.mismatchStartOrEndTag")
						+ formatLineNumber());
			}
		} catch (EOFException e) {
			throw new MifParseException(Messages.getString("mif.Mif2Xliff.fileEndError"));
		}
	}

	private void parseImportObejct() throws EOFException, MifParseException {
		ch = r.getCharAfterIgnore();
		if (ch != '<') {
			throw new MifParseException(Messages.getString("mif.Mif2Xliff.mismatchStartOrEndTag") + formatLineNumber());
		}
		while (true) {
			switch (ch) {
			case '<':
				String name = getStatmentName();
				nameStack.push(name);
				break;
			case '`':
				getValue(); // skip the content
				break;
			case '=':
				if (nameStack.isEmpty()) {
					throw new MifParseException(Messages.getString("mif.Mif2Xliff.mismatchStartOrEndTag")
							+ formatLineNumber());
				}
				if (nameStack.get(nameStack.size() - 1).equals("importobject")) {
					// check the parent statement, ensure the line is the object data describing line
					String endflag = ""; // end of the data describing the imported object
					do {
						getContentEndBy('\n'); // read to end of line
						ch = r.getCharAfterIgnore();
						if (ch != '&') {
							throw new MifParseException(
									Messages.getString("mif.Mif2Xliff.invalidateDataDescribingOfObject")
											+ formatLineNumber());
						}
						while (ch == '&') { // Data describing line
							getContentEndBy('\n'); // skip it
							ch = r.getCharAfterIgnore();
						}
						if (ch != '=') {
							throw new MifParseException(Messages.getString("mif.Mif2Xliff.statementError")
									+ formatLineNumber());
						}
						endflag = getContentEndBy('\n').trim();
						if (endflag == null) {
							throw new MifParseException(Messages.getString("mif.Mif2Xliff.statementError")
									+ formatLineNumber());
						}
					} while (!endflag.equalsIgnoreCase("EndInset"));
				}
				break;
			case '>':
				if (doc[sos - 2] == '\\') {
					break;
				}
				if (nameStack.isEmpty()) {
					throw new MifParseException(Messages.getString("mif.Mif2Xliff.mismatchStartOrEndTag")
							+ formatLineNumber());
				}
				name = nameStack.pop();
				if (name.equals("importobject")) {
					// End of ImportObject statement
					ch = r.getChar(); // read the next character and return, ensure continue to parse the file
					return;
				}
				break;
			default:
				break;
			}

			ch = r.getChar();
		}
	}

	private void parseAframes() throws EOFException, MifParseException {
		ch = r.getCharAfterIgnore();
		int start = 0;
		while (true) {
			switch (ch) {
			case '<':
				tos = sos - 1;
				String name = getStatmentName();
				nameStack.push(name);
				if (name.equals("frame") && !nameStack.get(nameStack.size() - 2).equals("frame")) {
					start = tos;
				} else if (name.equals("importobject")) {
					// the ImportObject statement individual parse
					// the ImportObject statement contained specification of object data
					parseImportObejct();
				}
				break;
			case '`':
				getValue();
				break;
			case '>':
				if (doc[sos - 2] == '\\') {
					break;
				}
				if (nameStack.isEmpty()) {
					throw new MifParseException(Messages.getString("mif.Mif2Xliff.mismatchStartOrEndTag")
							+ formatLineNumber());
				}
				name = nameStack.pop();
				if (name.equals("frame")) {
					// System.out.println(new String(doc,start,sos - start));
					int temp = nameStack.size() - 1;
					if (temp < 0 || !nameStack.get(temp).equals("frame")) {
						int tempEos = eos;
						eos = sos;
						sos = start;
						Frame fm = parseFrame();
						mpbf.appendFrame(fm);
						eos = tempEos;
					}
				}

				break;
			default:
				break;
			}

			if (sos == eos) {
				return;
			}
			ch = r.getChar();
		}
	}

	private Frame parseFrame() throws EOFException, MifParseException {
		Frame fm = new Frame();
		int tempsos = sos; // the top level frame start sos
		fm.setOffset(sos);
		fm.setEndOffset(eos);
		int start = 0;
		int framestart = 0;
		ch = r.getCharAfterIgnore();
		while (true) {
			switch (ch) {
			case '<':
				tos = sos - 1;
				String name = getStatmentName();
				nameStack.push(name);
				if (name.equals("textrect")) {
					start = tos;
				}
				if (name.equals("frame") && tos != tempsos) {
					framestart = tos;
				}
				if (name.equals("id") && nameStack.get(nameStack.size() - 2).equals("frame")) {
					String content = getContentEndBy('>');
					content = content.trim(); // clear space
					fm.setId(content);
				}
				if (name.equals("importobject")) {
					// the ImportObject statement individual parse
					// the ImportObject statement contained specification of object data
					parseImportObejct();
				}
				break;
			case '`':
				getValue();
				break;
			case '>':
				if (doc[sos - 2] == '\\') {
					break;
				}
				if (nameStack.isEmpty()) {
					throw new MifParseException(Messages.getString("mif.Mif2Xliff.mismatchStartOrEndTag")
							+ formatLineNumber());
				}
				name = nameStack.pop();
				if (name.equals("textrect")) {
					// System.out.println(new String(doc, start, sos - start));
					int tempEos = eos;
					eos = sos;
					sos = start;
					TextRect tr = parseTextRect();
					if (tr.validate()) {
						fm.appendTextRect(tr);
					}
					eos = tempEos;
				} else if (name.equals("frame") && framestart != 0) {
					System.out.println(new String(doc, framestart, sos - framestart));
					int tempEos = eos;
					eos = sos;
					sos = framestart;
					Frame fm1 = parseFrame();
					mpbf.appendFrame(fm1);
					eos = tempEos;
					framestart = 0; // reset for next time use
				}

				break;
			default:
				break;
			}

			if (sos == eos) {
				return fm;
			}
			ch = r.getChar();
		}
	}

	private TextRect parseTextRect() throws EOFException, MifParseException {
		TextRect tr = new TextRect();
		ch = r.getCharAfterIgnore();
		while (true) {
			switch (ch) {
			case '<':
				String name = getStatmentName();
				nameStack.push(name);
				if (name.equals("id")) {
					String content = getContentEndBy('>');
					content = content.trim(); // clear the space
					tr.setId(content);
				}
				if (name.equals("shaperect")) {
					tos = sos;
					String content = getContentEndBy('>');

					char[] c = content.toCharArray();
					String[] r = new String[4];
					StringBuffer bf = new StringBuffer();
					int j = 0;
					for (int i = 0; i < c.length; i++) {
						char m = c[i];
						if ((m >= '0' && m <= '9') || m == '.') {
							bf.append(m);
						}
						if (m == ' ' && bf.length() != 0) {
							r[j++] = bf.toString();
							bf.delete(0, bf.length());
						}
					}

					if (r.length != 4) {
						throw new MifParseException(Messages.getString("mif.Mif2Xliff.statementError")
								+ formatLineNumber(tos));
					}
					String vp = r[1];
					char e = vp.charAt(vp.length() - 1);
					if (e < '0' && e > '9') {
						vp = vp.substring(0, vp.length() - 1);
					}

					tr.setvPosition(vp);
				}
				break;
			case '`':
				getValue();
				break;
			case '>':
				if (doc[sos - 2] == '\\') {
					break;
				}
				if (nameStack.isEmpty()) {
					throw new MifParseException(Messages.getString("mif.Mif2Xliff.mismatchStartOrEndTag")
							+ formatLineNumber());
				}
				nameStack.pop();
				break;
			default:
				break;
			}
			if (sos == eos) {
				return tr;
			}
			ch = r.getChar();
		}
	}

	private Table parseTables() throws EOFException, MifParseException {
		Table tbl = new Table();
		tbl.setOffset(sos);
		tbl.setEndOffset(eos);
		ch = r.getCharAfterIgnore();
		while (true) {
			switch (ch) {
			case '<':
				String name = getStatmentName();
				nameStack.push(name);
				if (name.equals("tblid")) {
					String content = getContentEndBy('>');
					content = content.trim(); // clear the space
					tbl.setId(content);
				}
				break;
			case '`':
				getValue();
				break;
			case '>':
				if (doc[sos - 2] == '\\') {
					break;
				}
				if (nameStack.isEmpty()) {
					throw new MifParseException(Messages.getString("mif.Mif2Xliff.mismatchStartOrEndTag")
							+ formatLineNumber());
				}
				nameStack.pop();
				break;
			default:
				break;
			}
			if (sos == eos) {
				return tbl;
			}
			ch = r.getChar();
		}
	}

	private Page parsePage() throws EOFException, MifParseException {
		Page page = new Page();
		page.setOffset(sos);
		page.setEndOffset(eos);

		int start = 0;
		ch = r.getCharAfterIgnore();
		while (true) {
			switch (ch) {
			case '<':
				tos = sos - 1;
				String name = getStatmentName();
				nameStack.push(name);
				if (name.equals("pagetype")) {
					String content = getContentEndBy('>');
					content = content.trim();
					page.setPageType(content);
				}
				if (name.equals("pagetag")) {
					ch = r.getCharAfterIgnore();
					String pageTag = getValue();
					pageTag = pageTag.trim();
					page.setPageTag(pageTag);
				}
				if (name.equals("textrect")) {
					start = tos;
				}
				if (name.equals("importobject")) {
					// the ImportObject statement individual parse
					// the ImportObject statement contained specification of object data
					parseImportObejct();
				}
				break;
			case '`':
				getValue();
				break;
			case '>':
				if (doc[sos - 2] == '\\') {
					break;
				}
				if (nameStack.isEmpty()) {
					throw new MifParseException(Messages.getString("mif.Mif2Xliff.mismatchStartOrEndTag")
							+ formatLineNumber());
				}
				name = nameStack.pop();
				if (name.equals("textrect")) {
					// System.out.println(new String(doc,start,sos - start));
					int tempEos = eos;
					eos = sos;
					sos = start;
					TextRect tr = parseTextRect();
					if (tr.validate()) {
						page.appendTextRect(tr);
					}
					eos = tempEos;
					start = 0;
				}
				break;
			default:
				break;
			}
			if (sos == eos) {
				return page;
			}
			ch = r.getChar();
		}
	}

	private TextFlow parseTextFlow() throws EOFException, MifParseException {
		TextFlow tf = new TextFlow();
		tf.setOffset(sos);
		tf.setEndOffset(eos);

		ch = r.getCharAfterIgnore();
		while (true) {
			switch (ch) {
			case '<':
				String name = getStatmentName();
				nameStack.push(name);
				if (name.equals("textrectid") && (tf.getTextRectId() == null || tf.getTextRectId().equals(""))) {
					String content = getContentEndBy('>');
					content = content.trim();
					tf.setTextRectId(content);
				}
				break;
			case '`':
				getValue();
				break;
			case '>':
				if (doc[sos - 2] == '\\') {
					break;
				}
				if (nameStack.isEmpty()) {
					throw new MifParseException(Messages.getString("mif.Mif2Xliff.mismatchStartOrEndTag")
							+ formatLineNumber());
				}
				name = nameStack.pop();
				break;
			default:
				break;
			}
			if (sos == eos) {
				return tf;
			}
			ch = r.getChar();
		}
	}

	/**
	 * Get the text before specify character
	 * @return
	 * @throws EOFException
	 * @throws MifParseException
	 */
	private String getContentEndBy(char n) throws EOFException, MifParseException {
		StringBuffer bf = new StringBuffer();
		if (n == '>') {
			// need process escape character '>' represent as '\>'
			char a = r.getCharAfterIgnore();
			char pre = a;
			while (true) {
				if (a == '>' && pre != '\\') {
					break;
				}
				bf.append(a);
				pre = a;
				a = r.getChar();
			}
		} else {
			char a = r.getCharAfterIgnore();
			while (a != n) {
				bf.append(a);
				a = r.getChar();
			}
		}
		sos--;
		return bf.toString();
	}

	/**
	 * Get current statement name
	 * @return
	 * @throws EOFException
	 * @throws MifParseException
	 *             ;
	 */
	private String getStatmentName() throws EOFException, MifParseException {
		StringBuffer bf = new StringBuffer();
		char a = r.getCharAfterIgnore();
		while (true) { // TODO validate '<' and '>'
			if (a == ' ' || a == '\n' || a == '<' || a == '\r' || a == '\t') {
				break;
			}
			bf.append(a);
			a = r.getChar();
		}
		sos--;
		if (bf.length() == 0) {
			throw new MifParseException(Messages.getString("mif.Mif2Xliff.mismatchStartOrEndTag") + formatLineNumber());
		}
		return bf.toString().toLowerCase();
	}

	/**
	 * Get the statement value,the text in `'
	 * @return
	 * @throws MifParseException
	 * @throws EOFException
	 */
	private String getValue() throws MifParseException, EOFException {
		// char n = r.getCharAfterIgnore();
		if (ch != '`') {
			throw new MifParseException(Messages.getString("mif.Mif2Xliff.statementError") + formatLineNumber());
		}
		StringBuffer bf = new StringBuffer();
		char a = r.getChar();
		while (a != '\'') {
			bf.append(a);
			a = r.getChar();
		}
		sos--;
		return bf.toString();
	}

	private String formatLineNumber() {
		return formatLineNumber(sos);
	}

	private String formatLineNumber(int sos) {
		int os = 0;
		int lineNumber = 0;
		int lineos = 0;

		while (os <= sos) {
			if (doc[os] == '\n') {
				lineNumber++;
				lineos = os;
			}
			os++;
		}
		lineos = os - lineos;

		return "\n" + Messages.getString("mif.Mif2Xliff.linenumber") + (lineNumber + 1)
				+ Messages.getString("mif.Mif2Xliff.column") + (lineos - 1);
	}

	class MifReader {

		public MifReader() {
		}

		/**
		 * Get next character
		 * @return Return the next character
		 * @throws MifParseException
		 * @throws EOFException
		 */
		final public char getChar() throws MifParseException, EOFException {
			if (sos >= eos) {
				throw new EOFException(Messages.getString("mif.Mif2Xliff.fileEndError"));
			}
			char n = doc[sos++];
			if (n < 0)
				throw new MifParseException(Messages.getString("mif.Mif2Xliff.characterError") + formatLineNumber());
			return n;
		}

		/**
		 * Get the next char with ignore all \n,\t,\t and space
		 * @return the next none \n,\t,\t and space character
		 * @throws EOFException
		 * @throws MifParseException
		 */
		final public char getCharAfterIgnore() throws EOFException, MifParseException {
			char n;
			do {
				n = getChar();
				if (n < 0) {
					throw new MifParseException(Messages.getString("mif.Mif2Xliff.characterError") + formatLineNumber());
				}
				if ((n == ' ' || n == '\n' || n == '\t' || n == '\r')) {
				} else {
					return n;
				}
				n = getChar();
				if ((n == ' ' || n == '\n' || n == '\t' || n == '\r')) {
				} else {
					return n;
				}
			} while (true);
		}

		/**
		 * Skip the specification character
		 * @param ch
		 *            character
		 * @return if the next character is the specification character ,then skip it and return true; if return false
		 *         ,will do nothing
		 */
		final public boolean skipChar(char ch) {
			if (ch == doc[sos]) {
				sos++;
				return true;
			} else {
				return false;
			}
		}
	}

}

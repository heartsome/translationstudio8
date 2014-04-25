package net.heartsome.cat.converter.mif.parser;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.EOFException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Comparator;
import java.util.List;
import java.util.Stack;

import net.heartsome.cat.converter.StringSegmenter;
import net.heartsome.cat.converter.mif.bean.Frame;
import net.heartsome.cat.converter.mif.bean.Marker;
import net.heartsome.cat.converter.mif.bean.MifParseBuffer;
import net.heartsome.cat.converter.mif.bean.MifReaderBuffer;
import net.heartsome.cat.converter.mif.bean.Page;
import net.heartsome.cat.converter.mif.bean.Table;
import net.heartsome.cat.converter.mif.bean.TextFlow;
import net.heartsome.cat.converter.mif.bean.TextRect;
import net.heartsome.cat.converter.mif.common.MifParseException;
import net.heartsome.cat.converter.mif.common.ReaderUtil;
import net.heartsome.cat.converter.mif.common.UnSuportedFileExcetption;
import net.heartsome.cat.converter.mif.resource.Messages;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;

public class XliffReader {

	/** Current doc */
	private char[] doc;

	/** The MIF parse buffer */
	private MifParseBuffer mpbf;

	/** The MIF file reader buffer */
	private MifReaderBuffer mrbf;

	/** The XLiff TransUnit index counter */
	private int xliffIdex;

	/** The String Segementer */
	private StringSegmenter strSger;

	/** The doc start offset */
	private int sos;

	/** The doc end offset */
	private int eos;

	/** The temporary offset */
	private int tempos;

	private BufferedWriter xlfOs;

	/** Source language code */
	private String srcLang;

	private MifReader r;

	private EOFException eofExp;

	/** The current character of doc */
	private char ch;

	private Stack<String> nameStack;

	/** The Reader Util, provide the string common function */
	private ReaderUtil rUtil;

	private String mifEncoding;

	/**
	 * The constructor of XLIFF reader, the reader response to generate XLIFF file by
	 * {@link #readXliffFile(BufferedWriter, String, boolean, IProgressMonitor)} method and generate skeleton file by
	 * {@link #readSkeletonFile(BufferedWriter, IProgressMonitor)} method <br>
	 * Notice : when create instance of this class, would load the MIF file and parse it by use the @param mifFile
	 * @param mifFile
	 *            MIF file path
	 * @param encoding
	 *            MIF file encoding
	 * @param sourceLanguage
	 *            the language in MIF file
	 * @param strSger
	 *            The string segementer, witch use the SRX to segment the paragraph of text
	 * @throws IOException
	 * @throws MifParseException
	 * @throws UnSuportedFileExcetption
	 */
	public XliffReader(String mifFile, String encoding, String sourceLanguage, StringSegmenter strSger)
			throws IOException, MifParseException, UnSuportedFileExcetption {
		loadFile(mifFile, encoding);

		this.mifEncoding = encoding;
		this.strSger = strSger;
		this.srcLang = sourceLanguage;
		this.xliffIdex = 1;
	}

	private void loadFile(String mifFile, String encoding) throws IOException, MifParseException,
			UnSuportedFileExcetption {
		MifParser parser = new MifParser();
		parser.parseFile(mifFile, encoding);

		this.doc = parser.doc;
		this.mpbf = parser.mpbf;

		this.sos = 0;
		this.eos = doc.length;
		this.tempos = 0;

		this.mrbf = new MifReaderBuffer();
		this.r = new MifReader();
		this.eofExp = new EOFException("End of file");
		this.nameStack = new Stack<String>();
	}

	/**
	 * After parse file, this method read the xliff file from the parse info
	 * @param xlfOs
	 *            the xliff file out put stream
	 * @param iniFile
	 *            the configure file of mif,witch define the ESC
	 * @param isReadMasterPage
	 *            xliff contain the masterpage or not
	 * @param monitor
	 *            the progress monitor
	 * @throws MifParseException
	 * @throws IOException
	 *             ;
	 * @throws UnSuportedFileExcetption
	 */
	public void readXliffFile(BufferedWriter xlfOs, String iniFile, boolean isReadMasterPage, IProgressMonitor monitor)
			throws MifParseException, IOException, UnSuportedFileExcetption {
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}

    	rUtil = new ReaderUtil(iniFile);

		File xf = File.createTempFile("xliftemp", ".temp");

		// first read the index
		List<Marker> markers = mpbf.getMarkers();
		if (markers.size() > 0) {
			File f = File.createTempFile("miftemp", ".tmp");
			BufferedWriter tmpWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f),mifEncoding));
			try {
				this.xlfOs = new BufferedWriter(new FileWriter(xf));

				// generate the xliff content to temporary file xf
				for (Marker m : markers) {
					outputSegmenet(rUtil.cleanString(m.getContent()), m.getOffset(), m.getEndOffset(), 1);
				}

				// generate temporary file ,the file had extracted index content and contains '%%%index%%%'
				readSkeletonFile(tmpWriter, null);
			} finally {
				if (tmpWriter != null) {
					tmpWriter.close();
				}
				if (this.xlfOs != null) {
					this.xlfOs.close();
				}
			}
			// reload the temporary file
			loadFile(f.getAbsolutePath(), mifEncoding);

			// after reload delete the temporary file
			f.delete();
		}

		// second read the content
		this.xlfOs = xlfOs;
		List<Page> pages = mpbf.getPages();
		monitor.setTaskName(Messages.getString("mif.Mif2Xliff.task3"));
		if (pages.size() == 0) {
			// no pages specified ,direct read the Para statement
			monitor.beginTask(Messages.getString("mif.Mif2Xliff.task3"), 1);
			monitor.worked(1);
			readPara();
		} else {
			monitor.beginTask(Messages.getString("mif.Mif2Xliff.task3"), pages.size());
			for (Page page : pages) {
				String pageType = page.getPageType().toLowerCase();
				if (pageType.equals("bodypage")) {
					readPage(page);
				} else if (pageType.indexOf("masterpage") != -1 && isReadMasterPage) {
					readPage(page);
				}
				monitor.worked(1);
			}
		}
		BufferedReader r = null;
		try {
			r = new BufferedReader(new FileReader(xf));
			String line = r.readLine();
			while (line != null) {
				this.xlfOs.write(line + "\n");
				line = r.readLine();
			}
		} finally {
			if (r != null) {
				r.close();
			}
			xf.delete();
		}
		monitor.done();
	}

	/**
	 * After parse file, this method read skeleton file from the parse info
	 * @param sklOs
	 *            The skeleton file out put stream
	 * @param monitor
	 *            the progress monitor
	 * @throws IOException
	 *             ;
	 */
	public void readSkeletonFile(BufferedWriter sklOs, IProgressMonitor monitor) throws IOException {
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		List<Object[]> cbfs = mrbf.getBuffer(new Comparator<Object[]>() {

			public int compare(Object[] o1, Object[] o2) {
				Integer v1 = (Integer) o1[1];
				int v2 = (Integer) o2[1];
				return v1.compareTo(v2);
			}
		});
		int off = 0;
		int len = doc.length;
		monitor.setTaskName(Messages.getString("mif.Mif2Xliff.task4"));
		if (cbfs.size() == 0) {
			monitor.beginTask(Messages.getString("mif.Mif2Xliff.task4"), 1);
			sklOs.write(doc, off, len);
		} else {
			monitor.beginTask(Messages.getString("mif.Mif2Xliff.task4"), cbfs.size() + 1);
			for (Object[] obj : cbfs) {
				int index = (Integer) obj[0];
				int start = (Integer) obj[1];
				int end = (Integer) obj[2];
				int segNum = (Integer) obj[3];
				sklOs.write(doc, off, start - off);
				off = end;
				sklOs.write("%%%" + index + "%%%");
				while (segNum > 1) {
					sklOs.write("\n%%%" + ++index + "%%%");
					segNum--;
				}
				monitor.worked(1);
			}
			sklOs.write(new String(doc, off, len - off).trim());
			monitor.worked(1);
		}
		monitor.done();
	}

	private void readPage(Page page) throws MifParseException, IOException {
		List<TextRect> trs = page.getTextRects();
		readTextRect(trs);
	}

	private void readTextRect(List<TextRect> trs) throws MifParseException, IOException {
		for (TextRect tr : trs) {
			String id = tr.getId();
			TextFlow tf = mpbf.getTextFlow(id);
			if (tf == null) {
				continue;
			}
			readTextFlow(tf);
		}
	}

	private void readTextFlow(TextFlow tf) throws MifParseException, IOException {
		sos = tf.getOffset();
		eos = tf.getEndOffset();
		int parasos = 0;

		ch = r.getCharAfterIgnore();
		while (true) {
			switch (ch) {
			case '<':
				tempos = sos - 1;
				String name = getStatmentName();
				nameStack.push(name);

				if (name.equals("para")) {
					parasos = tempos;
				}

				break;
			case '`':
				getValue();
				break;
			case '>':
				if(doc[sos -2] == '\\'){
					break;
				}
				if (nameStack.isEmpty()) {
					throw new MifParseException(Messages.getString("mif.Mif2Xliff.mismatchStartOrEndTag")
							+ formatLineNumber());
				}
				name = nameStack.pop();
				if (name.equals("para")) {
					int tempEos = eos;
					eos = sos;
					sos = parasos;
					// System.out.println(new String(doc,sos,eos -sos));
					readPara();
					eos = tempEos;
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
	 * The sos and eos is the Para element start and end
	 * @throws MifParseException
	 * @throws IOException
	 */
	private void readPara() throws MifParseException, IOException {
		ch = r.getCharAfterIgnore();
		if (ch != '<') {
			throw new MifParseException(Messages.getString("mif.Mif2Xliff.statementError") + formatLineNumber());
		}
		int plsos = 0; // the first paraline start offset;
		int plCounter = 0;
		while (true) {
			switch (ch) {
			case '<':
				tempos = sos - 1;
				String name = getStatmentName();
				nameStack.push(name);
				if (name.equals("paraline")) {
					// start ParaLine element
					if (plsos == 0) {
						plsos = tempos;
					}
				}
				if (name.equals("incondition")) {
					ch = r.getCharAfterIgnore();
					if (ch != '`') {
						throw new MifParseException(Messages.getString("mif.Mif2Xliff.statementError")
								+ formatLineNumber());
					}
					String val = getValue();
					if (val.equalsIgnoreCase("FM8_TRACK_CHANGES_ADDED")
							|| val.equalsIgnoreCase("FM8_TRACK_CHANGES_ADDED")) {
						throw new MifParseException(Messages.getString("mif.Mif2Xliff.changeBarError"));
					}
				}

				break;
			case '`':
				getValue();
				break;
			case '>':
				if(doc[sos -2] == '\\'){
					break;
				}
				if (nameStack.isEmpty()) {
					throw new MifParseException(Messages.getString("mif.Mif2Xliff.mismatchStartOrEndTag")
							+ formatLineNumber());
				}
				name = nameStack.pop();
				if (name.equals("paraline")) {
					plCounter++;
				}
				if (name.equals("para")) {
					int tempEos = eos;
					int tempsos = sos;
					eos = sos - 1;
					sos = plsos;
					// System.out.println(new String(doc,sos,eos -sos));
					readParalines(plCounter);
					sos = tempsos;
					eos = tempEos;
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

	private void readParalines(int plNum) throws MifParseException, IOException {
		ch = r.getCharAfterIgnore();
		int strsos = 0; // string statement start offset
		int streos = 0; // string statement end offset
		int tagIdex = 1; // the index of tag

		int tagsos = 0;
		int tageos = 0;

		int strCounter = 0; // the string statement counter,Count is greater
							// than 1
							// will segment the content
		StringBuffer extraValBf = new StringBuffer();
		// StringBuffer tagValBf = new StringBuffer();
		while (true) {
			switch (ch) {
			case '<':
				tempos = sos - 1;
				String name = getStatmentName();
				nameStack.push(name);
				if (name.equals("paraline")) {
					// flag = false;
					tagsos = 0;
					tageos = 0;
				}
				if (name.equals("string")) {
					// start to extraction text content
					ch = r.getCharAfterIgnore();
					String text = getValue();

					if (extraValBf.length() == 0) {
						// the first of string statement
						strsos = tempos;
					} else {
						tageos = tempos;
					}
					if (tagsos != 0 && tageos != 0 && tagsos != tageos) {
//						System.out.println(formatLineNumber(tagsos) +" "+ formatLineNumber(tageos));
						String tag = new String(doc, tagsos, tageos - tagsos);
						if (tag.trim().length() != 0) {
							extraValBf.append("<ph id=\"" + tagIdex++ + "\">");
							extraValBf.append(rUtil.cleanTag(tag));
							extraValBf.append("</ph>");
						}
						tagsos = 0;
						tageos = 0;
					}
					extraValBf.append(rUtil.cleanString(text));
					strCounter++;
				}
				if (name.equals("char")) {
					String val = getContentEndBy('>');
					if (val.equals("HardReturn") && extraValBf.length() != 0) {
						outputSegmenet(extraValBf.toString(), strsos, streos, strCounter);
						strsos = 0;
						streos = 0;
						extraValBf.delete(0, extraValBf.length());
						tagsos = 0;
						tageos = 0;
						strCounter = 0;
					}
				}
				if (name.equals("aframe")) {
					String id = getContentEndBy('>');
					if (id == null || id.length() == 0) {
						throw new MifParseException(Messages.getString("mif.Mif2Xliff.statementError")
								+ formatLineNumber());
					}
					Frame fm = mpbf.getFrame(id);
					if (fm == null) {
						throw new MifParseException(Messages.getString("mif.Mif2Xliff.noFrameFind")
								+ formatLineNumber());
					}

					// save extract text
					if (extraValBf.length() != 0) {
						outputSegmenet(extraValBf.toString(), strsos, streos, strCounter);
						strsos = 0;
						streos = 0;
						extraValBf.delete(0, extraValBf.length());
						tagsos = 0;
						tageos = 0;
						strCounter = 0;
					}

					List<TextRect> trs = fm.getTextRects();
					readTextRect(trs);
				}
				if (name.equals("atbl")) {
					String id = getContentEndBy('>');
					if (id == null || id.length() == 0) {
						throw new MifParseException(Messages.getString("mif.Mif2Xliff.statementError")
								+ formatLineNumber());
					}
					Table tbl = mpbf.getTable(id);
					if (tbl == null) {
						throw new MifParseException(Messages.getString("mif.Mif2Xliff.noTableFind")
								+ formatLineNumber());
					}

					// save extract text
					if (extraValBf.length() != 0) {
						outputSegmenet(extraValBf.toString(), strsos, streos, strCounter);
						strsos = 0;
						streos = 0;
						extraValBf.delete(0, extraValBf.length());
						tagsos = 0;
						tageos = 0;
						strCounter = 0;
					}

					int tempsos = sos;
					int tempeos = eos;
					readTable(tbl);
					sos = tempsos;
					eos = tempeos;
				}
				break;
			case '>':
				if(doc[sos -2] == '\\'){
					break;
				}
				if (nameStack.isEmpty()) {
					throw new MifParseException(Messages.getString("mif.Mif2Xliff.mismatchStartOrEndTag")
							+ formatLineNumber());
				}
				name = nameStack.pop();
				if (name.equals("string")) {
					// end of string statement
					streos = sos;
					tagsos = sos;
				}
				if (name.equals("paraline")) {
					tageos = sos - 1;
					plNum--;
					if (tagsos != 0 && tageos != 0 && tagsos != tageos && plNum != 0) {
						// when plNum == 0 is the last ParaLine
						String tag = new String(doc, tagsos, tageos - tagsos);
						if (tag.trim().length() != 0) {
							extraValBf.append("<ph id=\"" + tagIdex++ + "\">");
							extraValBf.append(rUtil.cleanTag(tag));
							extraValBf.append("</ph>");
						}
						tagsos = 0;
						tageos = 0;
					}
				}

			default:
				break;
			}

			if (sos == eos) {
				if (extraValBf.length() != 0) {
					outputSegmenet(extraValBf.toString(), strsos, streos, strCounter);
					strsos = 0;
					streos = 0;
					extraValBf.delete(0, extraValBf.length());
					tagsos = 0;
					tageos = 0;
					strCounter = 0;
				}
				return;
			}
			ch = r.getChar();
		}
	}

	private void readTable(Table tbl) throws MifParseException, IOException {
		sos = tbl.getOffset();
		eos = tbl.getEndOffset();

		int parasos = 0;

		ch = r.getCharAfterIgnore();
		while (true) {
			switch (ch) {
			case '<':
				tempos = sos - 1;
				String name = getStatmentName();
				nameStack.push(name);
				if (name.equals("para")) {
					parasos = tempos;
				}
				break;
			case '`':
				getValue();
				break;
			case '>':
				if(doc[sos -2] == '\\'){
					break;
				}
				if (nameStack.isEmpty()) {
					throw new MifParseException(Messages.getString("mif.Mif2Xliff.mismatchStartOrEndTag")
							+ formatLineNumber());
				}
				name = nameStack.pop();
				if (name.equals("para")) {
					// end of para
					int tempEos = eos;
					eos = sos;
					sos = parasos;
					// System.out.println(new String(doc,sos,eos -sos));
					readPara();
					eos = tempEos;
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

	private void outputSegmenet(String segVal, int segsos, int segeos, int strNum) throws IOException {
		if (segVal.trim().length() == 0) {
			return;
		}
		Object[] ar = new Object[4];
		ar[0] = xliffIdex;
		ar[1] = segsos;
		ar[2] = segeos;
		int segCounter = 0;
		if (strNum > 1) {
			String[] segVals = strSger.segment(segVal.toString());
			// int temp = xliffIdex - 1;
			for (String seg : segVals) {
				if (seg.trim().length() == 0) {
					continue;
				}
				segCounter++;
				// System.out.println("seg(" + ar[0] + "):" + seg);
				writeXliffFile(xliffIdex++, seg);
			}
		} else {
			// System.out.println("sge:" + extraValBf);
			segCounter = 1;
			// System.out.println("seg(" + ar[0] + "):" + segVal);
			writeXliffFile(xliffIdex++, segVal);
		}
		if (segCounter != 0) {
			ar[3] = segCounter;
			mrbf.addBuffer(ar);
		}
	}

	private void writeXliffFile(int tuId, String sourceSeg) throws IOException {
		xlfOs.write("   <trans-unit id=\"" + tuId++ + "\" xml:space=\"preserve\">\n" + "      <source xml:lang=\""
				+ srcLang + "\">" + sourceSeg + "</source>\n" + "   </trans-unit>\n");
	}

	/**
	 * Get the text end by space
	 * @return
	 * @throws EOFException
	 * @throws MifParseException
	 */
	private String getContentEndBy(char n) throws EOFException, MifParseException {
		StringBuffer bf = new StringBuffer();
		if(n == '>'){
			//need process escape character '>' represent as '\>'
			char a = r.getCharAfterIgnore();
			char pre = a;
			while (true) {
				if(a == '>' && pre !='\\' ){
					break;
				}
				bf.append(a);
				pre = a;
				a = r.getChar();
			}
		}else {
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
	 * @throws MifParseException ;
	 */
	private String getStatmentName() throws EOFException, MifParseException{
		StringBuffer bf = new StringBuffer();
		char a = r.getCharAfterIgnore();
		while (true) {
			if(a == ' ' || a == '\n' || a == '<' || a == '\r' || a == '\t' ){
				break;
			}
			bf.append(a);
			a = r.getChar();
		}
		sos--;
		if(bf.length() == 0){
			throw new MifParseException(Messages.getString("mif.Mif2Xliff.mismatchStartOrEndTag")
					+ formatLineNumber());
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
		return "\nLine Number: " + (lineNumber + 1) + " Offset: " + (lineos - 1);
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
				throw eofExp;
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

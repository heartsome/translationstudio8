package net.heartsome.cat.converter.mif.parser;

import java.io.EOFException;
import java.util.Stack;

import net.heartsome.cat.converter.mif.common.MifParseException;

public class ExtractParaline {
	char[] doc;
	int sos;
	int eos;
	int tempos;
	private EOFException eofExp;
	
	Stack<String> nameStack = new Stack<String>();
	char ch;
	
	public ExtractParaline() {
		doc = content.toCharArray();
		sos = 0;
		eos = doc.length;
		
		eofExp = new EOFException("end of file");
	}
	
	public void readParaLine(){
		
	}
	
	public static void main(String[] args){
		
	}
	
	
	class MifReader {

		public MifReader() {
		}

		/**
		 * Get next character
		 * 
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
				throw new MifParseException("read error: invalid Character");
			return n;
		}

		/**
		 * Get the next char with ignore all \n,\t,\t and space
		 * 
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
		 * 
		 * @param ch
		 *            character
		 * @return if the next character is the specification character ,then
		 *         skip it and return true; if return false ,will do nothing
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
	
	private String content = 
//			"<Para \n" + 
//			"	<Unique 1064386>\n" + 
//			"	<PgfTag `SafetyText'>\n" + 
			"	<ParaLine \n"+
			"		<String `This installation guide provides basic guidelines for the Rosemount 644. It does not '>\n" + 
			"	> # end of ParaLine\n" + 
			"	<ParaLine \n" + 
			"		<String `provide instructions for detailed configuration, diagnostics, maintenance, service, '>\n" + 
			"		<Char HardReturn>\n" + 
			"	> # end of ParaLine\n" + 
			"	<ParaLine \n" + 
			"		<String `troubleshooting, or installation. Refer to the 644 Reference Manual (document number '>\n" + 
			"	> # end of ParaLine\n" + 
			"	<ParaLine \n"	+ 
			"		<String `00809-0100-4728) for more instruction. The manual and this QIG are also available '>\n" + 
			"	> # end of ParaLine\n" + 
			"	<ParaLine \n" + 
			"		<String `electronically on www.rosemount.com.'>\n" + 
			"	> # end of ParaLine\n" //+ 
//			"> # end of Para\n"// + 
			//"<para \n" + 
			//"	<ParaLine \n" + 
			//"		<String `T (US) '>\n" + 
			//"		<Char Tab>\n" + 
			//"		<String `(800) 999-9307'>\n" + 
			//"		<Char HardReturn>\n" + 
			//"	> # end of ParaLine\n" + 
			//"> # end of Para"
			;
}

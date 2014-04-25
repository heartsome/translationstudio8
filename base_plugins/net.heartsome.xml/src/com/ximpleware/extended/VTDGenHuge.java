/* 
 * Copyright (C) 2002-2012 XimpleWare, info@ximpleware.com
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */
package com.ximpleware.extended;
import java.io.*;


import com.ximpleware.extended.parser.*;

//import java.io.*;
/**
 * VTDGenHuge implementation supporting extended VTD (256GB file size).
 * Current support built-in entities only
 * It parses DTD, but doesn't resolve declared entities
 */
public class VTDGenHuge {
	// internal parser state

	private final static int STATE_LT_SEEN = 0; // encounter the first <
	private final static int STATE_START_TAG = 1;
	private final static int STATE_END_TAG = 2;
	private final static int STATE_ATTR_NAME = 3;
	private final static int STATE_ATTR_VAL = 4;
	private final static int STATE_TEXT = 5;
	private final static int STATE_DOC_START = 6; // beginning of document
	private final static int STATE_DOC_END = 7; // end of document 
	private final static int STATE_PI_TAG =8;
	private final static int STATE_PI_VAL = 9;
	private final static int STATE_DEC_ATTR_NAME = 10;
	private final static int STATE_COMMENT = 11;
	private final static int STATE_CDATA = 12;
	private final static int STATE_DOCTYPE = 13;
	private final static int STATE_END_COMMENT = 14;
	// comment appear after the last ending tag
	private final static int STATE_END_PI = 15;
	//private final static int STATE_END_PI_VAL = 17;

	public final static int IN_MEMORY = 0;
	public final static int MEM_MAPPED = 1;
	
	// token type
	public final static int TOKEN_STARTING_TAG = 0;
	public final static int TOKEN_ENDING_TAG = 1;
	public final static int TOKEN_ATTR_NAME = 2;
	public final static int TOKEN_ATTR_NS = 3;
	public final static int TOKEN_ATTR_VAL = 4;
	public final static int TOKEN_CHARACTER_DATA = 5;
	public final static int TOKEN_COMMENT = 6;
	public final static int TOKEN_PI_NAME = 7;
	public final static int TOKEN_PI_VAL = 8;
	public final static int TOKEN_DEC_ATTR_NAME = 9;
	public final static int TOKEN_DEC_ATTR_VAL = 10;
	public final static int TOKEN_CDATA_VAL = 11;
	public final static int TOKEN_DTD_VAL = 12;
	public final static int TOKEN_DOCUMENT = 13;

	// encoding format
	public final static int FORMAT_UTF8 = 2;
	public final static int FORMAT_ASCII = 0;

	public final static int FORMAT_ISO_8859_1 = 1;
	public final static int FORMAT_ISO_8859_2 = 3;
	public final static int FORMAT_ISO_8859_3 = 4;
	public final static int FORMAT_ISO_8859_4 = 5;
	public final static int FORMAT_ISO_8859_5 = 6;
	public final static int FORMAT_ISO_8859_6 = 7;
	public final static int FORMAT_ISO_8859_7 = 8;
	public final static int FORMAT_ISO_8859_8 = 9;
	public final static int FORMAT_ISO_8859_9 = 10;
	public final static int FORMAT_ISO_8859_10 = 11;
	public final static int FORMAT_ISO_8859_11 = 12;
	public final static int FORMAT_ISO_8859_12 = 13;
	public final static int FORMAT_ISO_8859_13 = 14;
	public final static int FORMAT_ISO_8859_14 = 15;
	public final static int FORMAT_ISO_8859_15 = 16;
	public final static int FORMAT_ISO_8859_16 = 17;
	
	public final static int FORMAT_WIN_1250 = 18;
	public final static int FORMAT_WIN_1251 = 19;
	public final static int FORMAT_WIN_1252 = 20;
	public final static int FORMAT_WIN_1253 = 21;
	public final static int FORMAT_WIN_1254 = 22;
	public final static int FORMAT_WIN_1255 = 23;
	public final static int FORMAT_WIN_1256 = 24;
	public final static int FORMAT_WIN_1257 = 25;
	public final static int FORMAT_WIN_1258 = 26;
	public final static int FORMAT_UTF_16LE = 64;
	public final static int FORMAT_UTF_16BE = 63;
	//namespace aware flag
	protected boolean ns;
	protected int VTDDepth; // Maximum Depth of VTDs
	protected int encoding;
	private int last_depth;
	private int last_l1_index;
	private int last_l2_index;
	private int last_l3_index;
	private int increment;
	private boolean BOM_detected;
	private boolean must_utf_8;
	private int ch;
	private int ch_temp;
	protected long offset;	// this is byte offset, not char offset as encoded in VTD
	private long temp_offset;
	protected int depth;


	protected long prev_offset;
	protected int rootIndex;
	protected IByteBuffer xb;
	protected FastLongBuffer VTDBuffer;
	protected FastLongBuffer l1Buffer;
	protected FastLongBuffer l2Buffer;
	protected FastIntBuffer l3Buffer;
	protected boolean br; //buffer reuse


	protected long docLen;
	// again, in terms of byte, not char as encoded in VTD
	protected long endOffset;
	protected long[] tag_stack;
	public long[] attr_name_array;
	public final static int MAX_DEPTH = 30; // maximum depth value
	protected long docOffset;

	// attr_name_array size
	private final static int ATTR_NAME_ARRAY_SIZE = 16;
	// tag_stack size
	private final static int TAG_STACK_SIZE = 32;
	// max prefix length
	public final static int MAX_PREFIX_LENGTH = (1<<8) -1;
	// max Qname length
	public final static int MAX_QNAME_LENGTH = (1<<11) -1;
	// max Token length
	public final static int MAX_TOKEN_LENGTH = (1<<17) -1;


	
	class UTF8Reader implements IReader {
		public UTF8Reader() {
		}
		public int getChar()
			throws EOFExceptionHuge, ParseExceptionHuge, EncodingExceptionHuge {
			if (offset >= endOffset)
				throw new EOFExceptionHuge("permature EOF reached, XML document incomplete");
			int temp = xb.byteAt(offset);
			//int a = 0, c = 0, d = 0, val = 0;
			if (temp >= 0) {
				offset++;
				return temp;
			}
			return handleUTF8(temp);

		}
		private int handleUTF8(int temp) throws EncodingExceptionHuge, ParseExceptionHuge{
		    int val,c,d,a,i;
			temp = temp & 0xff;
			switch (UTF8Char.byteCount(temp)) { // handle multi-byte code
			case 2:
				c = 0x1f;
				// A mask determine the val portion of the first byte
				d = 6; // 
				a = 1; //
				break;
			case 3:
				c = 0x0f;
				d = 12;
				a = 2;
				break;
			case 4:
				c = 0x07;
				d = 18;
				a = 3;
				break;
			case 5:
				c = 0x03;
				d = 24;
				a = 4;
				break;
			case 6:
				c = 0x01;
				d = 30;
				a = 5;
				break;
			default:
				throw new ParseExceptionHuge(
						"UTF 8 encoding error: should never happen");
			}
			val = (temp & c) << d;
			i = a - 1;
			while (i >= 0) {
				temp = xb.byteAt(offset + a - i);
				if ((temp & 0xc0) != 0x80)
					throw new ParseExceptionHuge(
							"UTF 8 encoding error: should never happen");
				val = val | ((temp & 0x3f) << ((i << 2) + (i << 1)));
				i--;
			}
			offset += a + 1;
			return val;
		}
		public boolean skipChar(int ch)
			throws EOFExceptionHuge, EncodingExceptionHuge, ParseExceptionHuge {
			//int a = 0, c = 0, d = 0, val = 0;
			int temp = xb.byteAt(offset);
			if (temp >= 0)
				if (ch == temp) {
					offset++;
					return true;
				} else {
					return false;
				}
			return skipUTF8(temp, ch);			
		}
		private boolean skipUTF8(int temp, int ch) throws EncodingExceptionHuge, ParseExceptionHuge{
		    int val, c, d, a, i;
		    temp = temp & 0xff;
			switch (UTF8Char.byteCount(temp)) { // handle multi-byte code
			case 2:
				c = 0x1f;
				// A mask determine the val portion of the first byte
				d = 6; // 
				a = 1; //
				break;
			case 3:
				c = 0x0f;
				d = 12;
				a = 2;
				break;
			case 4:
				c = 0x07;
				d = 18;
				a = 3;
				break;
			case 5:
				c = 0x03;
				d = 24;
				a = 4;
				break;
			case 6:
				c = 0x01;
				d = 30;
				a = 5;
				break;
			default:
				throw new ParseExceptionHuge(
						"UTF 8 encoding error: should never happen");
			}
			val = (temp & c) << d;
			i = a - 1;
			while (i >= 0) {
				temp = xb.byteAt(offset + a - i);
				if ((temp & 0xc0) != 0x80)
					throw new ParseExceptionHuge(
							"UTF 8 encoding error: should never happen");
				val = val | ((temp & 0x3f) << ((i << 2) + (i << 1)));
				i--;
			}
			if (val == ch){
			    offset += a + 1;
			    return true;
			}else
			    return false; 
			
		}

	}
	class UTF16BEReader implements IReader {
		public UTF16BEReader() {
		}
		public int getChar()
			throws EOFExceptionHuge, ParseExceptionHuge, EncodingExceptionHuge {
			int val = 0;
			if (offset >= endOffset)
				throw new EOFExceptionHuge("permature EOF reached, XML document incomplete");
			int temp = (xb.byteAt(offset)&0xff) << 8 | (xb.byteAt(offset+1)&0xff);
			if ((temp < 0xd800) || (temp > 0xdfff)) { // not a high surrogate
				offset += 2;
				return temp;
			} else {
				if (temp<0xd800 || temp>0xdbff)				
					throw new EncodingExceptionHuge("UTF 16 BE encoding error: should never happen");
				val = temp;
				temp = (xb.byteAt(offset+2)&0xff) << 8 | (xb.byteAt(offset+3)&0xff);
				if (temp < 0xdc00 || temp > 0xdfff) {
					// has to be a low surrogate here
					throw new EncodingExceptionHuge("UTF 16 BE encoding error: should never happen");
				}
				val = ((val - 0xd800)<<10) + (temp - 0xdc00) + 0x10000;
				offset += 4;
				return val;
				
			}
		}
		public boolean skipChar(int ch)
			throws EOFExceptionHuge, ParseExceptionHuge, EncodingExceptionHuge {
			// implement UTF-16BE to UCS4 conversion
			int temp = (xb.byteAt(offset)&0xff) << 8 | (xb.byteAt(offset+1)&0xff);
			if ((temp < 0xd800) || (temp > 0xdfff)) { // not a high surrogate
				//offset += 2;
				if (temp == ch) {
					offset += 2;
					return true;
				} else
					return false;
			} else {
				if (temp<0xd800 || temp>0xdbff)				
					throw new EncodingExceptionHuge("UTF 16 BE encoding error: should never happen");
				int val = temp;
				temp = (xb.byteAt(offset+2)&0xff) << 8 | (xb.byteAt(offset+3)&0xff);
				if (temp < 0xdc00 || temp > 0xdfff) {
					// has to be a low surrogate here
					throw new EncodingExceptionHuge("UTF 16 BE encoding error: should never happen");
				}
				val = ((val - 0xd800) << 10) + (temp - 0xdc00) + 0x10000;
				if (val == ch) {
					offset += 4;
					return true;
				} else
					return false;
			}
		}
	}
	class UTF16LEReader implements IReader {

		public UTF16LEReader() {
		}
		public int getChar()
			throws EOFExceptionHuge, ParseExceptionHuge, EncodingExceptionHuge {
			int val = 0;
			if (offset >= endOffset)
				throw new EOFExceptionHuge("permature EOF reached, XML document incomplete");
			int temp = (xb.byteAt(offset+1) &0xff) << 8 | (xb.byteAt(offset)& 0xff);
			if (temp < 0xd800 || temp > 0xdfff) { // check for low surrogate
				offset += 2;
				return temp;
			} else {
				if (temp<0xd800 || temp>0xdbff)				
					throw new EncodingExceptionHuge("UTF 16 LE encoding error: should never happen");
				val = temp;
				temp = (xb.byteAt(offset+3) &0xff) << 8 | (xb.byteAt(offset+2)&0xff);
				if (temp < 0xdc00 || temp > 0xdfff) {
					// has to be high surrogate
					throw new EncodingExceptionHuge("UTF 16 LE encoding error: should never happen");
				}
				val = ((val - 0xd800) <<10) + (temp - 0xdc00) + 0x10000;
				offset += 4;
				return val;
			}
		}
		public boolean skipChar(int ch)
			throws EOFExceptionHuge, EncodingExceptionHuge, ParseExceptionHuge {

			int temp = (xb.byteAt(offset+1)&0xff) << 8 | (xb.byteAt(offset)&0xff);
			if (temp < 0xd800 ||temp > 0xdfff) { // check for low surrogate
				if (temp == ch) {
					offset += 2;
					return true;
				} else {
					return false;
				}
			} else {
				if (temp<0xd800 || temp>0xdbff)				
					throw new EncodingExceptionHuge("UTF 16 LE encoding error: should never happen");
				int val = temp;
				temp = (xb.byteAt(offset+3)&0xff)<< 8 | (xb.byteAt(offset+2)&0xff);
				if (temp < 0xdc00 || temp > 0xdfff) {
					// has to be high surrogate
					throw new EncodingExceptionHuge("UTF 16 LE encoding error: should never happen");
				}
				val = ((val - 0xd800)<<10) + (temp - 0xdc00) + 0x10000;
				if (val == ch) {
					offset += 4;
					return true;
				} else
					return false;
			}

		}
	}

	class ASCIIReader implements IReader {
		public ASCIIReader() {
		}
		public int getChar()
			throws EOFExceptionHuge, ParseExceptionHuge, EncodingExceptionHuge {
			int a;
			if (offset >= endOffset)
				throw new EOFExceptionHuge("permature EOF reached, XML document incomplete");
			a= xb.byteAt(offset++);
			if (a<0)
				throw new ParseExceptionHuge(
				"ASCII encoding error: invalid ASCII Char");
			return a&0x7f;
		}
		public boolean skipChar(int ch)
			throws ParseExceptionHuge, EOFExceptionHuge, EncodingExceptionHuge {

			if (ch == xb.byteAt(offset)) {
				offset++;
				return true;
			} else {
				return false;
			}
		}
		
	}
	class ISO8859_1Reader implements IReader {
		public ISO8859_1Reader() {
		}
		public int getChar()
			throws EOFExceptionHuge, ParseExceptionHuge, EncodingExceptionHuge {

			if (offset >= endOffset)
				throw new EOFExceptionHuge("permature EOF reached, XML document incomplete");
			return xb.byteAt(offset++) & 0xff;
		}
		public boolean skipChar(int ch)
			throws EOFExceptionHuge, ParseExceptionHuge, EncodingExceptionHuge {
			if (ch == xb.byteAt(offset)) {
				offset++;
				return true;
			} else {
				return false;
			}
		}
	}
	
	class ISO8859_2Reader implements IReader {
		public ISO8859_2Reader() {
		}
		public int getChar()
			throws EOFExceptionHuge, ParseExceptionHuge, EncodingExceptionHuge {

			if (offset >= endOffset)
				throw new EOFExceptionHuge("permature EOF reached, XML document incomplete");
			return ISO8859_2.decode(xb.byteAt(offset++));
		}
		public boolean skipChar(int ch)
			throws EOFExceptionHuge, ParseExceptionHuge, EncodingExceptionHuge {
			if (ch == ISO8859_2.decode(xb.byteAt(offset))) {
				offset++;
				return true;
			} else {
				return false;
			}
		}
	}
	class ISO8859_3Reader implements IReader {
		public ISO8859_3Reader() {
		}
		public int getChar()
			throws EOFExceptionHuge, ParseExceptionHuge, EncodingExceptionHuge {

			if (offset >= endOffset)
				throw new EOFExceptionHuge("permature EOF reached, XML document incomplete");
			return ISO8859_3.decode(xb.byteAt(offset++));
		}
		public boolean skipChar(int ch)
			throws EOFExceptionHuge, ParseExceptionHuge, EncodingExceptionHuge {
			if (ch == ISO8859_3.decode(xb.byteAt(offset))) {
				offset++;
				return true;
			} else {
				return false;
			}
		}
	}
	
	class ISO8859_4Reader implements IReader {
		public ISO8859_4Reader() {
		}
		public int getChar()
			throws EOFExceptionHuge, ParseExceptionHuge, EncodingExceptionHuge {

			if (offset >= endOffset)
				throw new EOFExceptionHuge("permature EOF reached, XML document incomplete");
			return ISO8859_4.decode(xb.byteAt(offset++));
		}
		public boolean skipChar(int ch)
			throws EOFExceptionHuge, ParseExceptionHuge, EncodingExceptionHuge {
			if (ch == ISO8859_4.decode(xb.byteAt(offset))) {
				offset++;
				return true;
			} else {
				return false;
			}
		}
	}
	
	class ISO8859_5Reader implements IReader {
		public ISO8859_5Reader() {
		}
		public int getChar()
			throws EOFExceptionHuge, ParseExceptionHuge, EncodingExceptionHuge {

			if (offset >= endOffset)
				throw new EOFExceptionHuge("permature EOF reached, XML document incomplete");
			return ISO8859_5.decode(xb.byteAt(offset++));
		}
		public boolean skipChar(int ch)
			throws EOFExceptionHuge, ParseExceptionHuge, EncodingExceptionHuge {
			if (ch == ISO8859_5.decode(xb.byteAt(offset))) {
				offset++;
				return true;
			} else {
				return false;
			}
		}
	}
	
	class ISO8859_6Reader implements IReader {
		public ISO8859_6Reader() {
		}
		public int getChar()
			throws EOFExceptionHuge, ParseExceptionHuge, EncodingExceptionHuge {

			if (offset >= endOffset)
				throw new EOFExceptionHuge("permature EOF reached, XML document incomplete");
			return ISO8859_6.decode(xb.byteAt(offset++));
		}
		public boolean skipChar(int ch)
			throws EOFExceptionHuge, ParseExceptionHuge, EncodingExceptionHuge {
			if (ch == ISO8859_6.decode(xb.byteAt(offset))) {
				offset++;
				return true;
			} else {
				return false;
			}
		}
	}
	class ISO8859_7Reader implements IReader {
		public ISO8859_7Reader() {
		}
		public int getChar()
			throws EOFExceptionHuge, ParseExceptionHuge, EncodingExceptionHuge {

			if (offset >= endOffset)
				throw new EOFExceptionHuge("permature EOF reached, XML document incomplete");
			return ISO8859_7.decode(xb.byteAt(offset++));
		}
		public boolean skipChar(int ch)
			throws EOFExceptionHuge, ParseExceptionHuge, EncodingExceptionHuge {
			if (ch == ISO8859_7.decode(xb.byteAt(offset))) {
				offset++;
				return true;
			} else {
				return false;
			}
		}
	}
	
	class ISO8859_8Reader implements IReader {
		public ISO8859_8Reader() {
		}
		public int getChar()
			throws EOFExceptionHuge, ParseExceptionHuge, EncodingExceptionHuge {

			if (offset >= endOffset)
				throw new EOFExceptionHuge("permature EOF reached, XML document incomplete");
			return ISO8859_8.decode(xb.byteAt(offset++));
		}
		public boolean skipChar(int ch)
			throws EOFExceptionHuge, ParseExceptionHuge, EncodingExceptionHuge {
			if (ch == ISO8859_8.decode(xb.byteAt(offset))) {
				offset++;
				return true;
			} else {
				return false;
			}
		}
	}

	class ISO8859_9Reader implements IReader {
		public ISO8859_9Reader() {
		}
		public int getChar()
			throws EOFExceptionHuge, ParseExceptionHuge, EncodingExceptionHuge {

			if (offset >= endOffset)
				throw new EOFExceptionHuge("permature EOF reached, XML document incomplete");
			return ISO8859_9.decode(xb.byteAt(offset++));
		}
		public boolean skipChar(int ch)
			throws EOFExceptionHuge, ParseExceptionHuge, EncodingExceptionHuge {
			if (ch == ISO8859_9.decode(xb.byteAt(offset))) {
				offset++;
				return true;
			} else {
				return false;
			}
		}
	}

	
	class ISO8859_10Reader implements IReader {
		public ISO8859_10Reader() {
		}
		public int getChar()
			throws EOFExceptionHuge, ParseExceptionHuge, EncodingExceptionHuge {

			if (offset >= endOffset)
				throw new EOFExceptionHuge("permature EOF reached, XML document incomplete");
			return ISO8859_10.decode(xb.byteAt(offset++));
		}
		public boolean skipChar(int ch)
			throws EOFExceptionHuge, ParseExceptionHuge, EncodingExceptionHuge {
			if (ch == ISO8859_10.decode(xb.byteAt(offset))) {
				offset++;
				return true;
			} else {
				return false;
			}
		}
	}
	
	class ISO8859_11Reader implements IReader {
		public ISO8859_11Reader() {
		}
		public int getChar()
			throws EOFExceptionHuge, ParseExceptionHuge, EncodingExceptionHuge {

			if (offset >= endOffset)
				throw new EOFExceptionHuge("permature EOF reached, XML document incomplete");
			return ISO8859_11.decode(xb.byteAt(offset++));
		}
		public boolean skipChar(int ch)
			throws EOFExceptionHuge, ParseExceptionHuge, EncodingExceptionHuge {
			if (ch == ISO8859_11.decode(xb.byteAt(offset))) {
				offset++;
				return true;
			} else {
				return false;
			}
		}
	}
	
	class ISO8859_13Reader implements IReader {
		public ISO8859_13Reader() {
		}
		public int getChar()
			throws EOFExceptionHuge, ParseExceptionHuge, EncodingExceptionHuge {

			if (offset >= endOffset)
				throw new EOFExceptionHuge("permature EOF reached, XML document incomplete");
			return ISO8859_13.decode(xb.byteAt(offset++));
		}
		public boolean skipChar(int ch)
			throws EOFExceptionHuge, ParseExceptionHuge, EncodingExceptionHuge {
			if (ch == ISO8859_13.decode(xb.byteAt(offset))) {
				offset++;
				return true;
			} else {
				return false;
			}
		}
	}
	
	class ISO8859_14Reader implements IReader {
		public ISO8859_14Reader() {
		}
		public int getChar()
			throws EOFExceptionHuge, ParseExceptionHuge, EncodingExceptionHuge {

			if (offset >= endOffset)
				throw new EOFExceptionHuge("permature EOF reached, XML document incomplete");
			return ISO8859_14.decode(xb.byteAt(offset++));
		}
		public boolean skipChar(int ch)
			throws EOFExceptionHuge, ParseExceptionHuge, EncodingExceptionHuge {
			if (ch == ISO8859_14.decode(xb.byteAt(offset))) {
				offset++;
				return true;
			} else {
				return false;
			}
		}
	}
	
	class ISO8859_15Reader implements IReader {
		public ISO8859_15Reader() {
		}
		public int getChar()
			throws EOFExceptionHuge, ParseExceptionHuge, EncodingExceptionHuge {

			if (offset >= endOffset)
				throw new EOFExceptionHuge("permature EOF reached, XML document incomplete");
			return ISO8859_15.decode(xb.byteAt(offset++));
		}
		public boolean skipChar(int ch)
			throws EOFExceptionHuge, ParseExceptionHuge, EncodingExceptionHuge {
			if (ch == ISO8859_15.decode(xb.byteAt(offset))) {
				offset++;
				return true;
			} else {
				return false;
			}
		}
	}
	class WIN1250Reader implements IReader {
		public WIN1250Reader() {
		}
		public int getChar()
			throws EOFExceptionHuge, ParseExceptionHuge, EncodingExceptionHuge {

			if (offset >= endOffset)
				throw new EOFExceptionHuge("permature EOF reached, XML document incomplete");
			return WIN1250.decode(xb.byteAt(offset++));
		}
		public boolean skipChar(int ch)
			throws EOFExceptionHuge, ParseExceptionHuge, EncodingExceptionHuge {
			if (ch == WIN1250.decode(xb.byteAt(offset))) {
				offset++;
				return true;
			} else {
				return false;
			}
		}
	}
	class WIN1251Reader implements IReader {
		public WIN1251Reader() {
		}
		public int getChar()
			throws EOFExceptionHuge, ParseExceptionHuge, EncodingExceptionHuge {

			if (offset >= endOffset)
				throw new EOFExceptionHuge("permature EOF reached, XML document incomplete");
			return WIN1251.decode(xb.byteAt(offset++));
		}
		public boolean skipChar(int ch)
			throws EOFExceptionHuge, ParseExceptionHuge, EncodingExceptionHuge {
			if (ch == WIN1251.decode(xb.byteAt(offset))) {
				offset++;
				return true;
			} else {
				return false;
			}
		}
	}
	
	
	
	
	class WIN1252Reader implements IReader {
		public WIN1252Reader() {
		}
		public int getChar()
			throws EOFExceptionHuge, ParseExceptionHuge, EncodingExceptionHuge {

			if (offset >= endOffset)
				throw new EOFExceptionHuge("permature EOF reached, XML document incomplete");
			return WIN1252.decode(xb.byteAt(offset++));
		}
		public boolean skipChar(int ch)
			throws EOFExceptionHuge, ParseExceptionHuge, EncodingExceptionHuge {
			if (ch == WIN1252.decode(xb.byteAt(offset))) {
				offset++;
				return true;
			} else {
				return false;
			}
		}
	}
	
	class WIN1253Reader implements IReader {
		public WIN1253Reader() {
		}
		public int getChar()
			throws EOFExceptionHuge, ParseExceptionHuge, EncodingExceptionHuge {

			if (offset >= endOffset)
				throw new EOFExceptionHuge("permature EOF reached, XML document incomplete");
			return WIN1253.decode(xb.byteAt(offset++));
		}
		public boolean skipChar(int ch)
			throws EOFExceptionHuge, ParseExceptionHuge, EncodingExceptionHuge {
			if (ch == WIN1253.decode(xb.byteAt(offset))) {
				offset++;
				return true;
			} else {
				return false;
			}
		}
	}
	
	class WIN1254Reader implements IReader {
		public WIN1254Reader() {
		}
		public int getChar()
			throws EOFExceptionHuge, ParseExceptionHuge, EncodingExceptionHuge {

			if (offset >= endOffset)
				throw new EOFExceptionHuge("permature EOF reached, XML document incomplete");
			return WIN1254.decode(xb.byteAt(offset++));
		}
		public boolean skipChar(int ch)
			throws EOFExceptionHuge, ParseExceptionHuge, EncodingExceptionHuge {
			if (ch == WIN1254.decode(xb.byteAt(offset))) {
				offset++;
				return true;
			} else {
				return false;
			}
		}
	}
	
	class WIN1255Reader implements IReader {
		public WIN1255Reader() {
		}
		public int getChar()
			throws EOFExceptionHuge, ParseExceptionHuge, EncodingExceptionHuge {

			if (offset >= endOffset)
				throw new EOFExceptionHuge("permature EOF reached, XML document incomplete");
			return WIN1255.decode(xb.byteAt(offset++));
		}
		public boolean skipChar(int ch)
			throws EOFExceptionHuge, ParseExceptionHuge, EncodingExceptionHuge {
			if (ch == WIN1255.decode(xb.byteAt(offset))) {
				offset++;
				return true;
			} else {
				return false;
			}
		}
	}
	
	class WIN1256Reader implements IReader {
		public WIN1256Reader() {
		}
		public int getChar()
			throws EOFExceptionHuge, ParseExceptionHuge, EncodingExceptionHuge {

			if (offset >= endOffset)
				throw new EOFExceptionHuge("permature EOF reached, XML document incomplete");
			return WIN1256.decode(xb.byteAt(offset++));
		}
		public boolean skipChar(int ch)
			throws EOFExceptionHuge, ParseExceptionHuge, EncodingExceptionHuge {
			if (ch == WIN1256.decode(xb.byteAt(offset))) {
				offset++;
				return true;
			} else {
				return false;
			}
		}
	}
	
	class WIN1257Reader implements IReader {
		public WIN1257Reader() {
		}
		public int getChar()
			throws EOFExceptionHuge, ParseExceptionHuge, EncodingExceptionHuge {

			if (offset >= endOffset)
				throw new EOFExceptionHuge("permature EOF reached, XML document incomplete");
			return WIN1257.decode(xb.byteAt(offset++));
		}
		public boolean skipChar(int ch)
			throws EOFExceptionHuge, ParseExceptionHuge, EncodingExceptionHuge {
			if (ch == WIN1257.decode(xb.byteAt(offset))) {
				offset++;
				return true;
			} else {
				return false;
			}
		}
	}
	
	class WIN1258Reader implements IReader {
		public WIN1258Reader() {
		}
		public int getChar()
			throws EOFExceptionHuge, ParseExceptionHuge, EncodingExceptionHuge {

			if (offset >= endOffset)
				throw new EOFExceptionHuge("permature EOF reached, XML document incomplete");
			return WIN1258.decode(xb.byteAt(offset++));
		}
		public boolean skipChar(int ch)
			throws EOFExceptionHuge, ParseExceptionHuge, EncodingExceptionHuge {
			if (ch == WIN1258.decode(xb.byteAt(offset))) {
				offset++;
				return true;
			} else {
				return false;
			}
		}
	}
	protected IReader r;
	
	/**
	 * VTDGenHuge constructor method.
	 */
	public VTDGenHuge() {
		attr_name_array = new long[ATTR_NAME_ARRAY_SIZE];
		tag_stack = new long[TAG_STACK_SIZE];
		//scratch_buffer = new int[10];
		VTDDepth = 0;
		r = new UTF8Reader();
		br = false;
	}
	/**
	 * Clear internal states so VTDGEn can process the next file.
	 */
	public void clear() {
	    if (br==false){
	        VTDBuffer = null;
	        l1Buffer = null;
	        l2Buffer = null;
	        l3Buffer = null;
	    }
		xb = null;
		offset = temp_offset =0;
		last_depth = last_l1_index = last_l2_index = 0;
		rootIndex = 0;
		depth = -1;
		increment =1;
		BOM_detected = false;
		must_utf_8 = false;
		ch = ch_temp = 0;

	}
	/**
	 * This method will detect whether the entity is valid or not and increment offset.
	 * @return int
	 * @throws com.ximpleware.extended.ParseExceptionHuge Super class for any exception during parsing.
	 * @throws com.ximpleware.extended.EncodingExceptionHuge UTF/native encoding exception.
	 * @throws com.ximpleware.extended.EOFExceptionHuge End of file exception.
	 */
	private int entityIdentifier() throws EntityExceptionHuge, EncodingExceptionHuge,EOFExceptionHuge, ParseExceptionHuge {
		int ch = r.getChar();
		int val = 0;

		switch (ch) {
		case '#':
			ch = r.getChar();
			if (ch == 'x') {
				while (true) {
					ch = r.getChar();
					if (ch >= '0' && ch <= '9') {
						val = (val << 4) + (ch - '0');
					} else if (ch >= 'a' && ch <= 'f') {
						val = (val << 4) + (ch - 'a' + 10);
					} else if (ch >= 'A' && ch <= 'F') {
						val = (val << 4) + (ch - 'A' + 10);
					} else if (ch == ';') {
						return val;
					} else
						throw new EntityExceptionHuge("Errors in char reference: Illegal char following &#x.");
				}
			} else {
				while (true) {
					if (ch >= '0' && ch <= '9') {
						val = val * 10 + (ch - '0');
					} else if (ch == ';') {
						break;
					} else
						throw new EntityExceptionHuge("Errors in char reference: Illegal char following &#.");
						ch = r.getChar();
				}
			}
			if (!XMLChar.isValidChar(val)) {
				throw new EntityExceptionHuge("Errors in entity reference: Invalid XML char.");
			}
			return val;
			//break;

			case 'a' :
				ch = r.getChar();
				if (ch == 'm') {
					if (r.getChar() == 'p' && r.getChar() == ';') {
						//System.out.println(" entity for &");
						return '&';
					} else
						throw new EntityExceptionHuge("Errors in Entity: Illegal builtin reference");
				} else if (ch == 'p') {
					if (r.getChar() == 'o'
						&& r.getChar() == 's'
						&& r.getChar() == ';') {
						//System.out.println(" entity for ' ");
						return '\'';
					} else
						throw new EntityExceptionHuge("Errors in Entity: Illegal builtin reference");
				} else
					throw new EntityExceptionHuge("Errors in Entity: Illegal builtin reference");

			case 'q' :
				if (r.getChar() == 'u'
					&& r.getChar() == 'o'
					&& r.getChar() == 't'
					&& r.getChar() == ';') {
					return '"';
				} else
					throw new EntityExceptionHuge("Errors in Entity: Illegal builtin reference");
			case 'l' :
				if (r.getChar() == 't' && r.getChar() == ';') {
					return '<';
				} else
					throw new EntityExceptionHuge("Errors in Entity: Illegal builtin reference");
				//break;
			case 'g' :
				if (r.getChar() == 't' && r.getChar() == ';') {
					return '>';
				} else
					throw new EntityExceptionHuge("Errors in Entity: Illegal builtin reference");
			default :
				throw new EntityExceptionHuge("Errors in Entity: Illegal entity char");
		}
		//return val;
	}
	/**
	 * Format the string indicating the position (line number:offset)of the offset if 
	 * there is an exception.
	 * @return java.lang.String indicating the line number and offset of the exception
	 */
	private String formatLineNumber() {
		long so = docOffset;
		int lineNumber = 0;
		long lineOffset = 0;
		//long end = offset;

		if (encoding < FORMAT_UTF_16BE) {
			while (so <= offset-1) {
				if (xb.byteAt(so) == '\n') {
					lineNumber++;
					lineOffset = so;
				}
				//lineOffset++;
				so++;
			}
			lineOffset = offset - lineOffset;
		} else if (encoding == FORMAT_UTF_16BE) {
			while (so <= offset-2) {
				if (xb.byteAt(so + 1) == '\n' && xb.byteAt(so) == 0) {
					lineNumber++;
					lineOffset = so;
				}
				so += 2;
			}
			lineOffset = (offset - lineOffset) >> 1;
		} else {
			while (so <= offset-2) {
				if (xb.byteAt(so) == '\n' && xb.byteAt(so + 1) == 0) {
					lineNumber++;
					lineOffset = so;
				}
				so += 2;
			}
			lineOffset = (offset - lineOffset) >> 1;
		}
		return "\nLine Number: " + (lineNumber+1) + " Offset: " + (lineOffset-1);
	}
	/**
	 * Write the remaining portion of LC info
	 *
	 */
	private void finishUp(){
		if (last_depth == 1) {
			l1Buffer.append(((long) last_l1_index << 32) | 0xffffffffL);
		} else if (last_depth == 2) {
			l2Buffer.append(((long) last_l2_index << 32) | 0xffffffffL);
		}
	}
	
	
	/**
	 * The entity ignorant version of getCharAfterS.
	 * @return int
	 * @throws ParseExceptionHuge 
	 * @throws EncodingExceptionHuge 
	 * @throws com.ximpleware.extended.EOFExceptionHuge 
	 */
	private int getCharAfterS()
		throws ParseExceptionHuge, EncodingExceptionHuge, EOFExceptionHuge {
		int n;
		while (true) {
			n = r.getChar();
			if (n == ' ' || n == '\t' || n == '\n' || n == '\r') {
			} else
				return n;
		}
		//throw new EOFExceptionHuge("should never come here");
	}
	/**
	 * The entity aware version of getCharAfterS
	 * @return int
	 * @throws ParseExceptionHuge Super class for any exception during parsing.
	 * @throws EncodingExceptionHuge UTF/native encoding exception.
	 * @throws com.ximpleware.extended.EOFExceptionHuge End of file exception.
	 */
	private int getCharAfterSe()
		throws ParseExceptionHuge, EncodingExceptionHuge, EOFExceptionHuge {
		int n = 0;
		long temp; //offset saver
		while (true) {
			n = r.getChar();
			if (!XMLChar.isSpaceChar(n)) {
				if (n != '&')
					return n;
				else {
					temp = offset;
					if (!XMLChar.isSpaceChar(entityIdentifier())) {
						offset = temp; // rewind
						return '&';
					}
				}
			}
		}
	}
	/**
	 * This method returns the VTDNavHuge object after parsing, it also cleans 
	 * internal state so VTDGenHuge can process the next file.
	 * @return com.ximpleware.extended.VTDNavHuge
	 */
	public VTDNavHuge getNav() {
		// call VTDNav constructor
		VTDNavHuge vn =
			new VTDNavHuge(
				rootIndex,
				encoding,
				ns,
				VTDDepth,
				xb,
				VTDBuffer,
				l1Buffer,
				l2Buffer,
				l3Buffer,
				docOffset,
				docLen);
		clear();
		return vn;
	}
	/**
	 * Get the offset value of previous character.
	 * @return int
	 * @throws ParseExceptionHuge Super class for exceptions during parsing.
	 */
	private long getPrevOffset() throws ParseExceptionHuge {
		long prevOffset = offset;
		int temp;
		switch (encoding) {
			case FORMAT_UTF8 :
				do {
					prevOffset--;
				} while (xb.byteAt(prevOffset) <0 && 
				        (xb.byteAt(prevOffset) & (byte)0xc0) == (byte)0x80);
				
				return prevOffset;
			case FORMAT_ASCII :
			case FORMAT_ISO_8859_1:
			case FORMAT_ISO_8859_2:
			case FORMAT_ISO_8859_3:
			case FORMAT_ISO_8859_4:
			case FORMAT_ISO_8859_5:
			case FORMAT_ISO_8859_6:
			case FORMAT_ISO_8859_7:
			case FORMAT_ISO_8859_8:
			case FORMAT_ISO_8859_9:
			case FORMAT_ISO_8859_10:
			case FORMAT_WIN_1250:
			case FORMAT_WIN_1251:
			case FORMAT_WIN_1252:
			case FORMAT_WIN_1253:
			case FORMAT_WIN_1254:
			case FORMAT_WIN_1255:
			case FORMAT_WIN_1256:
			case FORMAT_WIN_1257:
			case FORMAT_WIN_1258:
				return offset - 1;
			case FORMAT_UTF_16LE :
			    temp= (xb.byteAt(offset)&0xff) << 8 | (xb.byteAt(offset + 1)&0xff);
				if (temp < 0xd800 || temp > 0xdfff) {
					return offset - 2;
				} else
					return offset - 4;
			case FORMAT_UTF_16BE :
			    temp =(xb.byteAt(offset)&0xff) << 8 | (xb.byteAt(offset + 1)&0xff);
				if (temp < 0xd800 || temp > 0xdfff) {
					return offset - 2;
				} else
					return offset - 4;
			default :
				throw new ParseExceptionHuge("Other Error: Should never happen");
		}
	}

	/**
	 * A private method that detects the BOM and decides document encoding
	 * @throws EncodingExceptionHuge
	 * @throws ParseExceptionHuge
	 */
	private void decide_encoding() throws EncodingExceptionHuge,ParseExceptionHuge {
	    if (xb.length()==0)
	        throw new EncodingExceptionHuge("Document is zero sized ");
		if (xb.byteAt(offset) == -2) {
			increment = 2;
			if (xb.byteAt(offset+1) == -1) {
				offset += 2;
				encoding = FORMAT_UTF_16BE;
				BOM_detected = true;
				r = new UTF16BEReader();
			} else
				throw new EncodingExceptionHuge("Unknown Character encoding: should be 0xff 0xfe");
		} else if (xb.byteAt(offset) == -1) {
			increment = 2;
			if (xb.byteAt(offset+1) == -2) {
				offset += 2;
				encoding = FORMAT_UTF_16LE;
				BOM_detected = true;
				r = new UTF16LEReader();
			} else
				throw new EncodingExceptionHuge("Unknown Character encoding: not UTF-16LE");
		} else if (xb.byteAt(offset) == -17){
		    if (xb.byteAt(offset+1) == -69 && xb.byteAt(offset+2)==-65){
		      offset +=3;
		      must_utf_8= true;
		    }
		    else 
		    	throw new EncodingExceptionHuge("Unknown Character encoding: not UTF-8");
		}
		else if (xb.byteAt(offset)==0){
			if (xb.byteAt(offset+1) == 0x3c 
					&& xb.byteAt(offset+2)== 0 
					&& xb.byteAt(offset+3)== 0x3f){
				encoding = FORMAT_UTF_16BE;
				increment = 2;
				r = new UTF16BEReader();
				}
			else
				throw new EncodingExceptionHuge("Unknown Character encoding: not UTF-16BE");
		}
		else if (xb.byteAt(offset)==0x3c){
			if (xb.byteAt(offset+1) == 0 
					&& xb.byteAt(offset+2) == 0x3f 
					&& xb.byteAt(offset+3) == 0){
				increment = 2;
				encoding = FORMAT_UTF_16LE;				
				r = new UTF16LEReader();
				}			
		}
		// check for max file size exception
		if (encoding < FORMAT_UTF_16BE) {
            if ((offset + (long) docLen) >= 1L << 37)
                throw new ParseExceptionHuge(
                        "Other error: file size too big >=128GB ");
        } else {
            if ((offset + (long) docLen) >= 1L << 37)
                throw new ParseExceptionHuge(
                        "Other error: file size too large >= 128GB");
        }
	}
	
	/**
	 * This method parses the XML file and returns a boolean indicating 
	 * if it is successful or not.
	 * @param fileName
	 * @param ns  namespace aware or not
	 * @param mode indicates whether the file is loaded in memory of memory mapped
	 * @return boolean indicating whether the parseFile is a success
	 *
	 */
	public boolean parseFile(String fileName, boolean ns, int mode){
	    //FileInputStream fis = null;
	    //File f = null;
	    try{
	        if (mode == IN_MEMORY){
	            XMLBuffer xb = new XMLBuffer();
	            xb.readFile(fileName);
	            this.setDoc(xb);
	            this.parse(ns);  // set namespace awareness to true
	            return true;
	        } else if (mode == MEM_MAPPED) {
	            XMLMemMappedBuffer xmb = new XMLMemMappedBuffer();
	            xmb.readFile(fileName);
	            this.setDoc(xmb);
	            this.parse(ns);  // set namespace awareness to true
	            return true;
	        } 
	        return false;
	    }catch(java.io.IOException e){    
	    }catch (ParseExceptionHuge e){
	    }
	    finally{
	    }
	    return false;	    
	}
	
	
	/**
	 * parseFile with default mode set to IN_MEMORY
	 * @param fileName
	 * @param ns
	 * @return boolean indicating whether the parseFile is a success
	 *
	 */
	public boolean parseFile(String fileName, boolean ns){
	    return parseFile(fileName, ns, IN_MEMORY);
	}
	
	
	
	/**
	 * Generating VTD tokens and Location cache info.
	 * @param NS boolean Enable namespace or not
	 * @throws ParseExceptionHuge Super class for any exceptions during parsing.     
	 * @throws EOFExceptionHuge End of file exception.    
	 * @throws EntityExceptionHuge Entity resolution exception.
	 * @throws EncodingExceptionHuge UTF/native encoding exception.
	 */
	public void parse(boolean NS)
		throws EncodingExceptionHuge, EOFExceptionHuge, EntityExceptionHuge, ParseExceptionHuge {

		// define internal variables	
		ns = NS;
		long length1 = 0, length2 = 0;
		int attr_count = 0 /*, ch = 0, ch_temp = 0*/;
		int parser_state = STATE_DOC_START;
		//boolean has_amp = false; 
		boolean is_ns = false;
		encoding = FORMAT_UTF8;
		boolean helper=false;
		//boolean docEnd = false;

		// first check first several bytes to figure out the encoding
		decide_encoding();

		// enter the main finite state machine
		try {
			writeVTD(0,0,TOKEN_DOCUMENT,depth);
			while (true) {
				switch (parser_state) {
					case STATE_LT_SEEN : //if (depth < -1)
						//    throw new ParseExceptionHuge("Other Errors: Invalid depth");
						temp_offset = offset;
						ch = r.getChar();
						if (XMLChar.isNameStartChar(ch)) {
							depth++;
							parser_state = STATE_START_TAG;
						} else {
							switch (ch) {
								case '/' :
									parser_state = STATE_END_TAG;
									break;
								case '?' :
									parser_state = process_qm_seen();
									break;
								case '!' : // three possibility (comment, CDATA, DOCTYPE)
									parser_state = process_ex_seen();
									break;
								default :
									throw new ParseExceptionHuge(
										"Other Error: Invalid char after <"
											+ formatLineNumber());
							}
						}
						break;

					case STATE_START_TAG : //name space is handled by
						while (true) {
							ch = r.getChar();
							if (XMLChar.isNameChar(ch)) {
								if (ch == ':') {
									length2 = offset - temp_offset - increment;
								}
							} else
								break;
						}
						length1 = offset - temp_offset - increment;
						if (depth > MAX_DEPTH) {
							throw new ParseExceptionHuge(
								"Other Error: Depth exceeds MAX_DEPTH"
									+ formatLineNumber());
						}
						//writeVTD(offset, TOKEN_STARTING_TAG, length2:length1, depth)
						long x = ((long) length1 << 38) |temp_offset;
						tag_stack[depth] = x;
						
						// System.out.println(
						//     " " + (temp_offset) + " " + length2 + ":" + length1 + " startingTag " + depth);
						if (depth > VTDDepth)
							VTDDepth = depth;
						if (encoding < FORMAT_UTF_16BE){
							if (length2>MAX_PREFIX_LENGTH
									|| length1 > MAX_QNAME_LENGTH)
								throw new ParseExceptionHuge(
										"Token Length Error: Starting tag prefix or qname length too long"					
										+ formatLineNumber());
							writeVTD(
								(temp_offset),
								(length2 << 10) | length1,
								TOKEN_STARTING_TAG,
								depth);
							}
						else{
							if (length2>(MAX_PREFIX_LENGTH <<1)
									|| length1 > (MAX_QNAME_LENGTH<<1))
								throw new ParseExceptionHuge(
										"Token Length Error: Starting tag prefix or qname length too long"
										+formatLineNumber());
							writeVTD(
								(temp_offset) >> 1,
								(length2 << 9) | (length1 >> 1),
								TOKEN_STARTING_TAG,
								depth);
						}
						//offset += length1;
						length2 = 0;
						if (XMLChar.isSpaceChar(ch)) {
							ch = getCharAfterS();
							if (XMLChar.isNameStartChar(ch)) {
								// seen an attribute here
								temp_offset = getPrevOffset();
								parser_state = STATE_ATTR_NAME;
								break;
							}
						}
						helper = true;
						if (ch == '/') {
							depth--;
							helper = false;
							ch = r.getChar();
						}
						if (ch == '>') {
						if (depth != -1) {
							temp_offset = offset;
							ch = getCharAfterSe(); // consume WSs
							if (ch == '<') {
								parser_state = STATE_LT_SEEN;
								if (r.skipChar('/')) {
									if (helper == true) {
										length1 = offset - temp_offset
												- (increment << 1);
										if (length1 > 0) {
											if (encoding < FORMAT_UTF_16BE)
												writeVTD((temp_offset),
														length1,
														TOKEN_CHARACTER_DATA,
														depth);
											else
												writeVTD((temp_offset) >> 1,
														(length1 >> 1),
														TOKEN_CHARACTER_DATA,
														depth);
										}
									}
									parser_state = STATE_END_TAG;
									break;
								}
							} else if (XMLChar.isContentChar(ch)) {
								//temp_offset = offset;
								parser_state = STATE_TEXT;
							} else if (ch == '&') {
								//has_amp = true;
								//temp_offset = offset;
								entityIdentifier();
								parser_state = STATE_TEXT;
							} else if (ch == ']') {
								if (r.skipChar(']')) {
									while (r.skipChar(']')) {
									}
									if (r.skipChar('>'))
										throw new ParseExceptionHuge(
												"Error in text content: ]]> in text content"
														+ formatLineNumber());
								}
								parser_state = STATE_TEXT;
							} else
								throw new ParseExceptionHuge(
										"Error in text content: Invalid char"
												+ formatLineNumber());
						} else {
							parser_state = STATE_DOC_END;
						}
						break;
					}
					throw new ParseExceptionHuge(
							"Starting tag Error: Invalid char in starting tag"
									+ formatLineNumber());

					case STATE_END_TAG :
						temp_offset = offset;
						long sos =  tag_stack[depth] & 0x1fffffffffL;
						int sl = (int) (tag_stack[depth] >> 38);
						
						offset = temp_offset+sl;
						
						if (offset>= endOffset)
							throw new EOFExceptionHuge("permature EOF reached, XML document incomplete");
						for (int i = 0; i < sl; i++) {
							if (xb.byteAt(sos + i) != xb.byteAt(temp_offset + i))
								throw new ParseExceptionHuge(
									"Ending tag error: Start/ending tag mismatch"
									+ formatLineNumber());
						}
						depth--;
						ch = getCharAfterS();
						if(ch != '>')
							throw new ParseExceptionHuge(
								"Ending tag error: Invalid char in ending tag "
								+ formatLineNumber()); 
						
						if (depth != -1) {
							temp_offset = offset;
							ch = getCharAfterS();
							if (ch == '<')
								parser_state = STATE_LT_SEEN;
							else if (XMLChar.isContentChar(ch)) {
								parser_state = STATE_TEXT;
							} 
							else if (ch == '&') {
								//has_amp = true;
								entityIdentifier();
								parser_state = STATE_TEXT;
							} 
							else if (ch == ']') {
								if (r.skipChar(']')) {
									while (r.skipChar(']')) {
									}
									if (r.skipChar('>'))
										throw new ParseExceptionHuge(
										"Error in text content: ]]> in text content"
										+ formatLineNumber());
								}
									parser_state = STATE_TEXT;
							}else
								throw new ParseExceptionHuge(
									"Other Error: Invalid char in xml"
									+ formatLineNumber());
						} else
							parser_state = STATE_DOC_END;
						break;
						
					case STATE_ATTR_NAME :

						if (ch == 'x') {
							if (r.skipChar('m')
								&& r.skipChar('l')
								&& r.skipChar('n')
								&& r.skipChar('s')) {
								ch = r.getChar();
								if (ch == '='
									|| XMLChar.isSpaceChar(ch)
									|| ch == ':') {
									is_ns = true; //break;
								}
							}
						}
						while (true) {
							if (XMLChar.isNameChar(ch)) {
								if (ch == ':') {
									length2 = offset - temp_offset - increment;
								}
								ch = r.getChar();
							} else
								break;
						}
						length1 = getPrevOffset() - temp_offset;
						// check for uniqueness here
						boolean unique = true;
						boolean unequal;
						for (int i = 0; i < attr_count; i++) {
							unequal = false;
							int prevLen = (int) attr_name_array[i] & 0x0001ffff;
							if (length1 == prevLen) {
								long prevOffset =
									 (attr_name_array[i] >> 17);
								for (int j = 0; j < prevLen; j++) {
									if (xb.byteAt(prevOffset + j)
										!= xb.byteAt(temp_offset + j)) {
										unequal = true;
										break;
									}
								}
							} else
								unequal = true;
							unique = unique && unequal;
						}
						if (!unique && attr_count != 0)
							throw new ParseExceptionHuge(
								"Error in attr: Attr name not unique"
									+ formatLineNumber());
						unique = true;
						if (attr_count < attr_name_array.length) {
							attr_name_array[attr_count] =
								( (temp_offset) << 17) | length1;
							attr_count++;
						} else // grow the attr_name_array by 16
							{
							long[] temp_array = attr_name_array;
							/*System.out.println(
								"size increase from "
									+ temp_array.length
									+ "  to "
									+ (attr_count + 16));*/
							attr_name_array =
								new long[attr_count + ATTR_NAME_ARRAY_SIZE];
							for (int i = 0; i < attr_count; i++) {
								attr_name_array[i] = temp_array[i];
							}
							attr_name_array[attr_count] =
								((temp_offset) << 17) | length1;
							attr_count++;
						}

						// after checking, write VTD
						if (is_ns) {
							if (encoding < FORMAT_UTF_16BE){
								if (length2>MAX_PREFIX_LENGTH
										|| length1 > MAX_QNAME_LENGTH)
									throw new ParseExceptionHuge(
											"Token length overflow error: Attr NS tag prefix or qname length too long"
											+formatLineNumber());
								writeVTD(
									temp_offset,
									(length2 << 10) | length1,
									TOKEN_ATTR_NS,
									depth);
							}
							else{
								if (length2>(MAX_PREFIX_LENGTH << 1)
										|| length1 > (MAX_QNAME_LENGTH <<1))
									throw new ParseExceptionHuge(
											"Token length overflow error: Attr NS prefix or qname length too long"
											+ formatLineNumber());
								writeVTD(
									temp_offset >> 1,
									(length2 << 9) | (length1 >> 1),
									TOKEN_ATTR_NS,
									depth);
							}
							is_ns = false;
						} else {
							if (encoding < FORMAT_UTF_16BE){
								if (length2>MAX_PREFIX_LENGTH
										|| length1 > MAX_QNAME_LENGTH)
									throw new ParseExceptionHuge(
											"Token Length Error: Attr name prefix or qname length too long"
											+ formatLineNumber());
								writeVTD(
									temp_offset,
									(length2 << 10) | length1,
									TOKEN_ATTR_NAME,
									depth);
							}
							else{
								if (length2>(MAX_PREFIX_LENGTH<<1)
										|| length1 > (MAX_QNAME_LENGTH<<1))
									throw new ParseExceptionHuge(
											"Token Length overflow error: Attr name prefix or qname length too long" 
											+ formatLineNumber());
								writeVTD(
									temp_offset >> 1,
									(length2 << 9) | (length1 >> 1),
									TOKEN_ATTR_NAME,
									depth);
							}
						}
						/*System.out.println(
						    " " + temp_offset + " " + length2 + ":" + length1 + " attr name " + depth);*/
						length2 = 0;
						if (XMLChar.isSpaceChar(ch)) {
							ch = getCharAfterS();
						}
						if (ch != '=')
							throw new ParseExceptionHuge(
								"Error in attr: invalid char"
									+ formatLineNumber());
						ch_temp = getCharAfterS();
						if (ch_temp != '"' && ch_temp != '\'')
							throw new ParseExceptionHuge(
								"Error in attr: invalid char (should be ' or \" )"
									+ formatLineNumber());
						temp_offset = offset;
						parser_state = STATE_ATTR_VAL;
						break;
						
					case STATE_ATTR_VAL :
						while (true) {
							ch = r.getChar();
							if (XMLChar.isValidChar(ch) && ch != '<') {
								if (ch == ch_temp)
									break;
								if (ch == '&') {
									// as in vtd spec, we mark attr val with entities
									if (!XMLChar
										.isValidChar(entityIdentifier())) {
										throw new ParseExceptionHuge(
											"Error in attr: Invalid XML char"
												+ formatLineNumber());
									}
								}

							} else
								throw new ParseExceptionHuge(
									"Error in attr: Invalid XML char"
										+ formatLineNumber());
						}

						length1 = offset - temp_offset - increment;
						if (encoding < FORMAT_UTF_16BE){
							if (length1 > MAX_TOKEN_LENGTH)
								  throw new ParseExceptionHuge("Token Length Error:"
											  +" Attr val too long (>0xfffff)"
											  + formatLineNumber());
							writeVTD(
								temp_offset,
								length1,
								TOKEN_ATTR_VAL,
								depth);
						}
						else{
							if (length1 > (MAX_TOKEN_LENGTH <<1))
								  throw new ParseExceptionHuge("Token Length Error:"
											  +" Attr val too long (>0xfffff)"
											  + formatLineNumber());
							writeVTD(
								temp_offset >> 1,
								length1 >> 1,
								TOKEN_ATTR_VAL,
								depth);
						}
						ch = r.getChar();
						if (XMLChar.isSpaceChar(ch)) {
							ch = getCharAfterS();
							if (XMLChar.isNameStartChar(ch)) {
								temp_offset = offset - increment;
								parser_state = STATE_ATTR_NAME;
								break;
							}
						}
						helper = true;
						if (ch == '/') {
							depth--;
							helper = false;
							ch = r.getChar();
						}

						if (ch == '>') {
							attr_count = 0;
							if (depth != -1) {
								temp_offset = offset;
								ch = getCharAfterSe();
								if (ch == '<') {
									parser_state = STATE_LT_SEEN;
									if (r.skipChar('/')) {
										if (helper == true) {
											length1 = offset - temp_offset
													- (increment << 1);
											//if (length1 > 0) {
												if (encoding < FORMAT_UTF_16BE)
													writeVTD((temp_offset),
															length1,
															TOKEN_CHARACTER_DATA,
															depth);
												else
													writeVTD((temp_offset) >> 1,
															(length1 >> 1),
															TOKEN_CHARACTER_DATA,
															depth);
											//}
										}
										parser_state = STATE_END_TAG;
										break;
									}
								} else if (XMLChar.isContentChar(ch)) {
									//temp_offset = offset;
									parser_state = STATE_TEXT;
								} else if (ch == '&') {
									//has_amp = true;
									//temp_offset = offset;
									entityIdentifier();
									parser_state = STATE_TEXT;
								} else if (ch == ']') {
									if (r.skipChar(']')) {
										while (r.skipChar(']')) {
										}
										if (r.skipChar('>'))
											throw new ParseExceptionHuge(
												"Error in text content: ]]> in text content"
													+ formatLineNumber());
									}
									parser_state = STATE_TEXT;
								}else
									throw new ParseExceptionHuge(
										"Error in text content: Invalid char"
											+ formatLineNumber());
							} else {
								parser_state = STATE_DOC_END;
							}
							break;
						}

						throw new ParseExceptionHuge(
							"Starting tag Error: Invalid char in starting tag"
								+ formatLineNumber());
						
					case STATE_TEXT :
						if (depth == -1)
							throw new ParseExceptionHuge(
								"Error in text content: Char data at the wrong place"
									+ formatLineNumber());
						while (true) {
							ch = r.getChar();
							if (XMLChar.isContentChar(ch)) {
							} else if (ch == '&') {
								//has_amp = true;
								if (!XMLChar.isValidChar(entityIdentifier()))
									throw new ParseExceptionHuge(
										"Error in text content: Invalid char in text content "
											+ formatLineNumber());
								//parser_state = STATE_TEXT;
							} else if (ch == '<') {
								break;
							} else if (ch == ']') {
								if (r.skipChar(']')) {
									while (r.skipChar(']')) {
									}
									if (r.skipChar('>'))
										throw new ParseExceptionHuge(
											"Error in text content: ]]> in text content"
												+ formatLineNumber());
								}
							} else
								throw new ParseExceptionHuge(
									"Error in text content: Invalid char in text content "
										+ formatLineNumber());
						}
						length1 = offset - increment - temp_offset;

						if (encoding < FORMAT_UTF_16BE)
							writeVTD(
								temp_offset,
								length1,
								TOKEN_CHARACTER_DATA,
								depth);
						else
							writeVTD(
								temp_offset >> 1,
								length1 >> 1,
								TOKEN_CHARACTER_DATA,
								depth);

						//has_amp = true;
						parser_state = STATE_LT_SEEN;
						break;
					case STATE_DOC_START :
						parser_state = process_start_doc();
						break;
					case STATE_DOC_END :
						//docEnd = true;
						parser_state = process_end_doc();
						break;
					case STATE_PI_TAG :
						parser_state = process_pi_tag();
						break;
						//throw new ParseExceptionHuge("Error in PI: Invalid char");
					case STATE_PI_VAL :
						parser_state = process_pi_val();
						break;

					case STATE_DEC_ATTR_NAME :
						parser_state = process_dec_attr();
						break;
						
					case STATE_COMMENT :
						parser_state = process_comment();
						break;
						
					case STATE_CDATA :
						parser_state = process_cdata();
						break;
						
					case STATE_DOCTYPE :
						parser_state = process_doc_type();
						break;
						
					case STATE_END_COMMENT :
						parser_state = process_end_comment();
						break;

					case STATE_END_PI :
						parser_state = process_end_pi();
						break;
						
					default :
						throw new ParseExceptionHuge(
							"Other error: invalid parser state"
								+formatLineNumber());
				}
			}
		} catch (EOFExceptionHuge e) {
			if (parser_state != STATE_DOC_END)
				throw e;
			finishUp();
		}
	}
	private void matchCPEncoding()throws ParseExceptionHuge{
	    if ((r.skipChar('p') || r.skipChar('P')) && r.skipChar('1')
                && r.skipChar('2') && r.skipChar('5')) {
            if (encoding <= FORMAT_UTF_16LE) {
                if (must_utf_8)
                    throw new EncodingExceptionHuge(
                            "Can't switch from UTF-8"
                                    + formatLineNumber());
                if (r.skipChar('0')){
				    encoding = FORMAT_WIN_1250;
				    r=new WIN1250Reader();
				    writeVTD(temp_offset, 6,
								TOKEN_DEC_ATTR_VAL,
								depth);				    
				}else if (r.skipChar('1')){
				    encoding = FORMAT_WIN_1251;
				    r=new WIN1251Reader();
				    writeVTD(temp_offset, 6,
								TOKEN_DEC_ATTR_VAL,
								depth);				    
				}else if (r.skipChar('2')){
				    encoding = FORMAT_WIN_1252;
				    r=new WIN1252Reader();
				    writeVTD(temp_offset, 6,
								TOKEN_DEC_ATTR_VAL,
								depth);				    
				}else if (r.skipChar('3')){
				    encoding = FORMAT_WIN_1253;
				    r=new WIN1253Reader();
				    writeVTD(temp_offset, 6,
								TOKEN_DEC_ATTR_VAL,
								depth);				    
				}else if (r.skipChar('4')){
				    encoding = FORMAT_WIN_1254;
				    r=new WIN1254Reader();
				    writeVTD(temp_offset, 6,
								TOKEN_DEC_ATTR_VAL,
								depth);				   
				}else if (r.skipChar('5') ){
				    encoding = FORMAT_WIN_1255;
				    r=new WIN1255Reader();
				    writeVTD(temp_offset, 6,
								TOKEN_DEC_ATTR_VAL,
								depth);				    
				}else if (r.skipChar('6')){
				    encoding = FORMAT_WIN_1256;
				    r=new WIN1256Reader();
				    writeVTD(temp_offset, 6,
								TOKEN_DEC_ATTR_VAL,
								depth);
				}else if (r.skipChar('7') ){
				    encoding = FORMAT_WIN_1257;
				    r=new WIN1257Reader();
				    writeVTD(temp_offset, 6,
								TOKEN_DEC_ATTR_VAL,
								depth);
				}else if (r.skipChar('8') ){
				    encoding = FORMAT_WIN_1258;
				    r=new WIN1258Reader();
				    writeVTD(temp_offset, 6,
								TOKEN_DEC_ATTR_VAL,
								depth);
				}else   
				    throw new ParseExceptionHuge(
						"XML decl error: Invalid Encoding"
						+ formatLineNumber());
                if (r.skipChar(ch_temp))
                    return;				
            } else
                throw new ParseExceptionHuge(
                        "XML decl error: Can't switch encoding to ISO-8859"
                                + formatLineNumber());
				
				}
	    throw new ParseExceptionHuge(
				"XML decl error: Invalid Encoding"
						+ formatLineNumber());	    
	}
	
	private void matchWindowsEncoding()throws ParseExceptionHuge{
	    if ((r.skipChar('i') || r.skipChar('I')) 
	            &&(r.skipChar('n') || r.skipChar('N'))
	            &&(r.skipChar('d') || r.skipChar('D'))
	            &&(r.skipChar('o') || r.skipChar('O'))
	            &&(r.skipChar('w') || r.skipChar('W'))
	            &&(r.skipChar('s') || r.skipChar('S'))
	            && r.skipChar('-')
	            && r.skipChar('1')
                && r.skipChar('2') 
                && r.skipChar('5')) {
            if (encoding <= FORMAT_UTF_16LE) {
                if (must_utf_8)
                    throw new EncodingExceptionHuge(
                            "Can't switch from UTF-8"
                                    + formatLineNumber());
                if (r.skipChar('0')){
				    encoding = FORMAT_WIN_1250;
				    r=new WIN1250Reader();
				    writeVTD(temp_offset, 12,
								TOKEN_DEC_ATTR_VAL,
								depth);
				}else if (r.skipChar('1')){
				    encoding = FORMAT_WIN_1251;
				    r=new WIN1251Reader();
				    writeVTD(temp_offset, 12,
								TOKEN_DEC_ATTR_VAL,
								depth);
				}else if (r.skipChar('2')){
				    encoding = FORMAT_WIN_1252;
				    r=new WIN1252Reader();
				    writeVTD(temp_offset, 12,
								TOKEN_DEC_ATTR_VAL,
								depth);
				}else if (r.skipChar('3')){
				    encoding = FORMAT_WIN_1253;
				    r=new WIN1253Reader();
				    writeVTD(temp_offset, 12,
								TOKEN_DEC_ATTR_VAL,
								depth);
				}else if (r.skipChar('4')){
				    encoding = FORMAT_WIN_1254;
				    r=new WIN1254Reader();
				    writeVTD(temp_offset, 12,
								TOKEN_DEC_ATTR_VAL,
								depth);
				}else if (r.skipChar('5')){
				    encoding = FORMAT_WIN_1255;
				    r=new WIN1255Reader();
				    writeVTD(temp_offset, 12,
								TOKEN_DEC_ATTR_VAL,
								depth);
				}else if (r.skipChar('6')){
				    encoding = FORMAT_WIN_1256;
				    r=new WIN1256Reader();
				    writeVTD(temp_offset, 12,
								TOKEN_DEC_ATTR_VAL,
								depth);				   
				}else if (r.skipChar('7')){
				    encoding = FORMAT_WIN_1257;
				    r=new WIN1257Reader();
				    writeVTD(temp_offset, 12,
								TOKEN_DEC_ATTR_VAL,
								depth);
				}else if (r.skipChar('8')){
				    encoding = FORMAT_WIN_1258;
				    r=new WIN1258Reader();
				    writeVTD(temp_offset, 12,
								TOKEN_DEC_ATTR_VAL,
								depth);				   
				}else 
				    throw new ParseExceptionHuge(
							"XML decl error: Invalid Encoding"
									+ formatLineNumber());
                if (r.skipChar(ch_temp))
                    return;
				
            } else
                throw new ParseExceptionHuge(
                        "XML decl error: Can't switch encoding to ISO-8859"
                                + formatLineNumber());				
				}
	    throw new ParseExceptionHuge(
				"XML decl error: Invalid Encoding"
						+ formatLineNumber());
	}
	private void matchUTFEncoding() throws ParseExceptionHuge{
		if ((r.skipChar('s') || r.skipChar('S')))
			if (r.skipChar('-')
					&& (r.skipChar('a') || r.skipChar('A'))
					&& (r.skipChar('s') || r.skipChar('S'))
					&& (r.skipChar('c') || r.skipChar('C'))
					&& (r.skipChar('i') || r.skipChar('I'))
					&& (r.skipChar('i') || r.skipChar('I'))
					&& r.skipChar(ch_temp)) {
				if (encoding != FORMAT_UTF_16LE
						&& encoding != FORMAT_UTF_16BE) {
					if (must_utf_8)
						throw new EncodingExceptionHuge(
								"Can't switch from UTF-8"
										+ formatLineNumber());
					encoding = FORMAT_ASCII;
					r=new ASCIIReader();					
						writeVTD(temp_offset, 8,
								TOKEN_DEC_ATTR_VAL,
								depth);
					
						return;
				} else
					throw new ParseExceptionHuge(
							"XML decl error: Can't switch encoding to US-ASCII"
									+ formatLineNumber());
			} else
				throw new ParseExceptionHuge(
						"XML decl error: Invalid Encoding"
								+ formatLineNumber());

		if ((r.skipChar('t') || r.skipChar('T'))
				&& (r.skipChar('f') || r.skipChar('F'))
				&& r.skipChar('-')) {
			if (r.skipChar('8') && r.skipChar(ch_temp)) {
				if (encoding != FORMAT_UTF_16LE
						&& encoding != FORMAT_UTF_16BE) {
					//encoding = FORMAT_UTF8;
					writeVTD(temp_offset, 5,
								TOKEN_DEC_ATTR_VAL,
								depth);					
						return;
				} else
					throw new ParseExceptionHuge(
							"XML decl error: Can't switch encoding to UTF-8"
									+ formatLineNumber());
			}
			if (r.skipChar('1') && r.skipChar('6')) {
				if (r.skipChar(ch_temp)) {
					if (encoding == FORMAT_UTF_16LE
							|| encoding == FORMAT_UTF_16BE) {
						if (!BOM_detected)
							throw new EncodingExceptionHuge(
									"BOM not detected for UTF-16"
											+ formatLineNumber());
							writeVTD(
									temp_offset >> 1,
									6,
									TOKEN_DEC_ATTR_VAL,
									depth);
						return;
					}
					throw new ParseExceptionHuge(
							"XML decl error: Can't switch encoding to UTF-16"
									+ formatLineNumber());
				} else if ((r.skipChar('l') || r.skipChar('L'))
						&& (r.skipChar('e') || r.skipChar('E'))
						&& r.skipChar(ch_temp)) {
					if (encoding == FORMAT_UTF_16LE) {
						r = new UTF16LEReader();						
							writeVTD(
									temp_offset >> 1,
									8,
									TOKEN_DEC_ATTR_VAL,
									depth);
						return;
					}
					throw new ParseExceptionHuge(
							"XML del error: Can't switch encoding to UTF-16LE"
									+ formatLineNumber());
				} else if ((r.skipChar('b') || r.skipChar('B'))
						&& (r.skipChar('e') || r.skipChar('E'))
						&& r.skipChar(ch_temp)) {
					if (encoding == FORMAT_UTF_16BE) {
						writeVTD(
									temp_offset >> 1,
									8,
									TOKEN_DEC_ATTR_VAL,
									depth);
						return;
					}
					throw new ParseExceptionHuge(
							"XML del error: Can't swtich encoding to UTF-16BE"
									+ formatLineNumber());
				}

				throw new ParseExceptionHuge(
						"XML decl error: Invalid encoding"
								+ formatLineNumber());
			}
		}
	}
	
	private void matchISOEncoding()throws ParseExceptionHuge{
		if ((r.skipChar('s') || r.skipChar('S'))
				&& (r.skipChar('o') || r.skipChar('O'))
				&& r.skipChar('-') && r.skipChar('8')
				&& r.skipChar('8') && r.skipChar('5')
				&& r.skipChar('9') && r.skipChar('-'))
				{
		    if (encoding <= FORMAT_UTF_16LE) {
				if (must_utf_8)
					throw new EncodingExceptionHuge(
							"Can't switch from UTF-8"
									+ formatLineNumber());
				if (r.skipChar('1')){
				 if (r.skipChar(ch_temp)) {
				     encoding = FORMAT_ISO_8859_1;
				     r = new ISO8859_1Reader();
				     writeVTD(temp_offset, 10,
							TOKEN_DEC_ATTR_VAL,
							depth);
				     return;
				 } else if (r.skipChar('0') ){
				     encoding = FORMAT_ISO_8859_10;
				     r = new ISO8859_10Reader();
				     writeVTD(temp_offset, 11,
								TOKEN_DEC_ATTR_VAL,
								depth);
				 } else if (r.skipChar('1') ){
				     encoding = FORMAT_ISO_8859_11;
				     r = new ISO8859_11Reader();
				     writeVTD(temp_offset, 11,
								TOKEN_DEC_ATTR_VAL,
								depth);
				 } else if (r.skipChar('3') ){
				     encoding = FORMAT_ISO_8859_13;
				     r = new ISO8859_13Reader();
				     writeVTD(temp_offset, 11,
								TOKEN_DEC_ATTR_VAL,
								depth);
				 }else if (r.skipChar('4') ){
				     encoding = FORMAT_ISO_8859_14;
				     r = new ISO8859_14Reader();
				     writeVTD(temp_offset, 11,
								TOKEN_DEC_ATTR_VAL,
								depth);
				 }else if (r.skipChar('5') ){
				     encoding = FORMAT_ISO_8859_15;
				     r = new ISO8859_15Reader();
				     writeVTD(temp_offset, 11,
								TOKEN_DEC_ATTR_VAL,
								depth);
				 } 
				 
				}else if (r.skipChar('2') ){
				    encoding = FORMAT_ISO_8859_2;
				    r = new ISO8859_2Reader();
				    writeVTD(temp_offset, 10,
								TOKEN_DEC_ATTR_VAL,
								depth);				    
				}else if (r.skipChar('3')){
				    r = new ISO8859_3Reader();
				    encoding = FORMAT_ISO_8859_3;
				    writeVTD(temp_offset, 10,
								TOKEN_DEC_ATTR_VAL,
								depth);				  
				}else if (r.skipChar('4') ){
				    r = new ISO8859_4Reader();
				    encoding = FORMAT_ISO_8859_4;
				    writeVTD(temp_offset, 10,
								TOKEN_DEC_ATTR_VAL,
								depth);				    
				}else if (r.skipChar('5') ){
				    encoding = FORMAT_ISO_8859_5;
				    r = new ISO8859_5Reader();
				    writeVTD(temp_offset, 10,
								TOKEN_DEC_ATTR_VAL,
								depth);				   
				}else if (r.skipChar('6') ){
				    encoding = FORMAT_ISO_8859_6;
				    r = new ISO8859_6Reader();
				    writeVTD(temp_offset, 10,
								TOKEN_DEC_ATTR_VAL,
								depth);
				}else if (r.skipChar('7') ){
				    encoding = FORMAT_ISO_8859_7;
				    r = new ISO8859_7Reader();
				    writeVTD(temp_offset, 10,
								TOKEN_DEC_ATTR_VAL,
								depth);				   
				}else if (r.skipChar('8') ){
				    encoding = FORMAT_ISO_8859_8;
				    r = new ISO8859_8Reader();
				    writeVTD(temp_offset, 10,
								TOKEN_DEC_ATTR_VAL,
								depth);				   
				}else if (r.skipChar('9')){
				    encoding = FORMAT_ISO_8859_9;
				    r = new ISO8859_9Reader();
				    writeVTD(temp_offset, 10,
								TOKEN_DEC_ATTR_VAL,
								depth);				   
				} else 		
				    throw new ParseExceptionHuge(
							"XML decl error: Invalid Encoding"
									+ formatLineNumber());
				if (r.skipChar(ch_temp))
				    return;				
			} else
				throw new ParseExceptionHuge(
						"XML decl error: Can't switch encoding to ISO-8859"
								+ formatLineNumber());
		}
		throw new ParseExceptionHuge(
				"XML decl error: Invalid Encoding"
						+ formatLineNumber());
	}
	/**
	 * This private method processes declaration attributes
	 * @return the parser state after which the parser loop jumps to
	 * @throws ParseExceptionHuge
	 * @throws EncodingExceptionHuge
	 * @throws EOFExceptionHuge
	 */
	private int process_dec_attr() throws ParseExceptionHuge, EncodingExceptionHuge, EOFExceptionHuge{
		//int length1;
		int parser_state;
		if (ch == 'v'
			&& r.skipChar('e')
			&& r.skipChar('r')
			&& r.skipChar('s')
			&& r.skipChar('i')
			&& r.skipChar('o')
			&& r.skipChar('n')) {
			ch = getCharAfterS();
			if (ch == '=') {
				/*System.out.println(
				    " " + (temp_offset - 1) + " " + 7 + " dec attr name version " + depth);*/
				if (encoding < FORMAT_UTF_16BE)
					writeVTD(
						temp_offset - 1,
						7,
						TOKEN_DEC_ATTR_NAME,
						depth);
				else
					writeVTD(
						(temp_offset -2) >> 1,
						7,
						TOKEN_DEC_ATTR_NAME,
						depth);
			} else
				throw new ParseExceptionHuge(
					"XML decl error: Invalid char"
						+ formatLineNumber());
		} else
			throw new ParseExceptionHuge(
				"XML decl error: should be version"
					+ formatLineNumber());
		ch_temp = getCharAfterS();
		if (ch_temp != '\'' && ch_temp != '"')
			throw new ParseExceptionHuge(
				"XML decl error: Invalid char to start attr name"
					+ formatLineNumber());
		temp_offset = offset;
		// support 1.0 or 1.1
		if (r.skipChar('1')
			&& r.skipChar('.')
			&& (r.skipChar('0') || r.skipChar('1'))) {
			/*System.out.println(
			    " " + temp_offset + " " + 3 + " dec attr val (version)" + depth);*/
			if (encoding < FORMAT_UTF_16BE)
				writeVTD(
					temp_offset,
					3,
					TOKEN_DEC_ATTR_VAL,
					depth);
			else
				writeVTD(
					temp_offset >> 1,
					3,
					TOKEN_DEC_ATTR_VAL,
					depth);
		} else
			throw new ParseExceptionHuge(
				"XML decl error: Invalid version(other than 1.0 or 1.1) detected"
					+ formatLineNumber());
		if (!r.skipChar(ch_temp))
			throw new ParseExceptionHuge(
				"XML decl error: version not terminated properly"
					+ formatLineNumber());
		ch = r.getChar();
		//? space or e 
		if (XMLChar.isSpaceChar(ch)) {
			ch = getCharAfterS();
			temp_offset = offset - increment;
			if (ch == 'e') {
				if (r.skipChar('n')
					&& r.skipChar('c')
					&& r.skipChar('o')
					&& r.skipChar('d')
					&& r.skipChar('i')
					&& r.skipChar('n')
					&& r.skipChar('g')) {
					ch = r.getChar();
					if (XMLChar.isSpaceChar(ch))
						ch = getCharAfterS();
					if (ch == '=') {
						/*System.out.println(
						    " " + (temp_offset) + " " + 8 + " dec attr name (encoding) " + depth);*/
						if (encoding < FORMAT_UTF_16BE)
							writeVTD(
								temp_offset,
								8,
								TOKEN_DEC_ATTR_NAME,
								depth);
						else
							writeVTD(
								temp_offset >> 1,
								8,
								TOKEN_DEC_ATTR_NAME,
								depth);
					} else
						throw new ParseExceptionHuge(
							"XML decl error: Invalid char"
								+ formatLineNumber());
					ch_temp = getCharAfterS();
					if (ch_temp != '"' && ch_temp != '\'')
						throw new ParseExceptionHuge(
							"XML decl error: Invalid char to start attr name"
								+ formatLineNumber());
					temp_offset = offset;
					ch = r.getChar();
					switch (ch) {
						case 'a' :
						case 'A' :
							if ((r.skipChar('s')
								|| r.skipChar('S'))
								&& (r.skipChar('c')
									|| r.skipChar('C'))
								&& (r.skipChar('i')
									|| r.skipChar('I'))
								&& (r.skipChar('i')
									|| r.skipChar('I'))
								&& r.skipChar(ch_temp)) {												
								if (encoding != FORMAT_UTF_16LE
									&& encoding
										!= FORMAT_UTF_16BE) {
									if (must_utf_8)
										throw new EncodingExceptionHuge("Can't switch from UTF-8"
												+ formatLineNumber());
									encoding = FORMAT_ASCII;
									r = new ASCIIReader();
									/*System.out.println(
									    " " + (temp_offset) + " " + 5 + " dec attr val (encoding) " + depth);*/
									
										writeVTD(
											temp_offset,
											5,
											TOKEN_DEC_ATTR_VAL,
											depth);
									
									break;
								} else
									throw new ParseExceptionHuge(
										"XML decl error: Can't switch encoding to ASCII"
											+ formatLineNumber());
							}
							throw new ParseExceptionHuge(
								"XML decl error: Invalid Encoding"
									+ formatLineNumber());
						case 'c':
						case 'C':
						    matchCPEncoding();
						    break;
						case 'i' :
						case 'I' :
						    matchISOEncoding();
						    break;
						case 'u' :
						case 'U' :
						    matchUTFEncoding();
						    break;
							// now deal with windows encoding
						case 'w' :
						case 'W' :
						    matchWindowsEncoding();
						    break;
						default :
							throw new ParseExceptionHuge(
								"XML decl Error: invalid encoding"
									+ formatLineNumber());
					}
					ch = r.getChar();
					if (XMLChar.isSpaceChar(ch))
						ch = getCharAfterS();
					temp_offset = offset - increment;
				} else
					throw new ParseExceptionHuge(
						"XML decl Error: Invalid char"
							+ formatLineNumber());
			}

			if (ch == 's') {
				if (r.skipChar('t')
					&& r.skipChar('a')
					&& r.skipChar('n')
					&& r.skipChar('d')
					&& r.skipChar('a')
					&& r.skipChar('l')
					&& r.skipChar('o')
					&& r.skipChar('n')
					&& r.skipChar('e')) {

					ch = getCharAfterS();
					if (ch != '=')
						throw new ParseExceptionHuge(
							"XML decl error: Invalid char"
								+ formatLineNumber());
					/*System.out.println(
					    " " + temp_offset + " " + 3 + " dec attr name (standalone) " + depth);*/
					if (encoding < FORMAT_UTF_16BE)
						writeVTD(
							temp_offset,
							10,
							TOKEN_DEC_ATTR_NAME,
							depth);
					else
						writeVTD(
							temp_offset >> 1,
							10,
							TOKEN_DEC_ATTR_NAME,
							depth);
					ch_temp = getCharAfterS();
					temp_offset = offset;
					if (ch_temp != '"' && ch_temp != '\'')
						throw new ParseExceptionHuge(
							"XML decl error: Invalid char to start attr name"
								+ formatLineNumber());
					ch = r.getChar();
					if (ch == 'y') {
						if (r.skipChar('e')
							&& r.skipChar('s')
							&& r.skipChar(ch_temp)) {
							/*System.out.println(
							    " " + (temp_offset) + " " + 3 + " dec attr val (standalone) " + depth);*/
							if (encoding < FORMAT_UTF_16BE)
								writeVTD(
									temp_offset,
									3,
									TOKEN_DEC_ATTR_VAL,
									depth);
							else
								writeVTD(
									temp_offset >> 1,
									3,
									TOKEN_DEC_ATTR_VAL,
									depth);
						} else
							throw new ParseExceptionHuge(
								"XML decl error: invalid val for standalone"
									+ formatLineNumber());
					} else if (ch == 'n') {
						if (r.skipChar('o')
							&& r.skipChar(ch_temp)) {
							/*System.out.println(
							    " " + (temp_offset) + " " + 2 + " dec attr val (standalone)" + depth);*/
							if (encoding < FORMAT_UTF_16BE)
								writeVTD(
									temp_offset,
									2,
									TOKEN_DEC_ATTR_VAL,
									depth);
							else
								writeVTD(
									temp_offset >> 1,
									2,
									TOKEN_DEC_ATTR_VAL,
									depth);
						} else
							throw new ParseExceptionHuge(
								"XML decl error: invalid val for standalone"
									+ formatLineNumber());
					} else
						throw new ParseExceptionHuge(
							"XML decl error: invalid val for standalone"
								+ formatLineNumber());
				} else
					throw new ParseExceptionHuge(
						"XML decl error" + formatLineNumber());
				ch = r.getChar();
				if (XMLChar.isSpaceChar(ch))
					ch = getCharAfterS();
			}
		}

		if (ch == '?' && r.skipChar('>')) {
			temp_offset = offset;
			ch = getCharAfterS();
			if (ch == '<') {
				parser_state = STATE_LT_SEEN;
			} else
				throw new ParseExceptionHuge(
					"Other Error: Invalid Char in XML"
						+ formatLineNumber());
		} else
			throw new ParseExceptionHuge(
				"XML decl Error: Invalid termination sequence"
					+ formatLineNumber());
		return parser_state;
	}
	/**
	 * This private method processes PI tag
	 * @return the parser state after which the parser loop jumps to
	 * @throws ParseExceptionHuge
	 * @throws EncodingExceptionHuge
	 * @throws EOFExceptionHuge
	 */
	private int process_pi_tag() throws ParseExceptionHuge, EncodingExceptionHuge, EOFExceptionHuge{
		long length1;
		int parser_state;
		while (true) {
			ch = r.getChar();
			if (!XMLChar.isNameChar(ch))
				break;
		}

		length1 = offset - temp_offset - increment;
		/*System.out.println(
		    ((char) XMLDoc[temp_offset])
		        + " "
		        + (temp_offset)
		        + " "
		        + length1
		        + " PI Target "
		        + depth); */
		if (encoding < FORMAT_UTF_16BE){
			if (length1 > MAX_TOKEN_LENGTH)
				  throw new ParseExceptionHuge("Token Length Error:"
							  +" PI name too long (>0xfffff)"
							  + formatLineNumber());
			writeVTD(
				(temp_offset),
				length1,
				TOKEN_PI_NAME,
				depth);
		}
		else{
			if(length1 > (MAX_TOKEN_LENGTH<<1))
				throw new ParseExceptionHuge("Token Length Error:"
							+" PI name too long (>0xfffff)"
							+ formatLineNumber());
			writeVTD(
				(temp_offset) >> 1,
				(length1 >> 1),
				TOKEN_PI_NAME,
				depth);
		}
		//length1 = 0;
		/*temp_offset = offset;
		if (XMLChar.isSpaceChar(ch)) {
			ch = r.getChar();
		}*/
		if (ch == '?') {
			if (r.skipChar('>')) {
				temp_offset = offset;
				ch = getCharAfterSe();
				if (ch == '<') {
					parser_state = STATE_LT_SEEN;
				} else if (XMLChar.isContentChar(ch)) {
					parser_state = STATE_TEXT;
				} else if (ch == '&') {
					//has_amp = true;
					entityIdentifier();
					parser_state = STATE_TEXT;
				} else if (ch == ']') {
					if (r.skipChar(']')) {
						while (r.skipChar(']')) {
						}
						if (r.skipChar('>'))
							throw new ParseExceptionHuge(
								"Error in text content: ]]> in text content"
									+ formatLineNumber());
					}
					parser_state = STATE_TEXT;
				}else
					throw new ParseExceptionHuge(
						"Error in text content: Invalid char"
							+ formatLineNumber());
				return parser_state;
			} else
				throw new ParseExceptionHuge(
					"Error in PI: invalid termination sequence"
						+ formatLineNumber());
		}
		parser_state = STATE_PI_VAL;
		return parser_state;
	}
	/**
	 * This private method processes PI val 
	 * @return the parser state after which the parser loop jumps to
	 * @throws ParseExceptionHuge
	 * @throws EncodingExceptionHuge
	 * @throws EOFExceptionHuge
	 */
	private int process_pi_val() throws ParseExceptionHuge, EncodingExceptionHuge, EOFExceptionHuge{
		int parser_state;
		long length1;
		if (!XMLChar.isSpaceChar(ch)) 
			throw new ParseExceptionHuge(
					"Error in PI: invalid termination sequence"
						+ formatLineNumber());
		temp_offset = offset;
		ch = r.getChar();
		while (true) {
			if (XMLChar.isValidChar(ch)) {
				//System.out.println(""+(char)ch);
				if (ch == '?')
					if (r.skipChar('>')) {
						break;
					} /*else
						throw new ParseExceptionHuge(
							"Error in PI: invalid termination sequence for PI"
								+ formatLineNumber());*/
			} else
				throw new ParseExceptionHuge(
					"Errors in PI: Invalid char in PI val"
						+ formatLineNumber());
			ch = r.getChar();
		}
		length1 = offset - temp_offset - (increment<<1);
		/*System.out.println(
		    ((char) XMLDoc[temp_offset])
		        + " "
		        + (temp_offset)
		        + " "
		        + length1
		        + " PI val "
		        + depth);*/
		if (encoding < FORMAT_UTF_16BE){
			if (length1 > MAX_TOKEN_LENGTH)
				  throw new ParseExceptionHuge("Token Length Error:"
							  +"PI VAL too long (>0xfffff)"
							  + formatLineNumber());
			writeVTD(temp_offset,
					length1, 
					TOKEN_PI_VAL,
					depth);
		}
		else{
			if (length1 > (MAX_TOKEN_LENGTH<<1))
				  throw new ParseExceptionHuge("Token Length Error:"
							  +"PI VAL too long (>0xfffff)"
							  + formatLineNumber());
			writeVTD(
				temp_offset >> 1,
				length1 >> 1,
				TOKEN_PI_VAL,
				depth);
		}
		//length1 = 0;
		temp_offset = offset;
		ch = getCharAfterSe();
		
		if (ch == '<') {
			parser_state = STATE_LT_SEEN;
		} else if (XMLChar.isContentChar(ch)) {
			//temp_offset = offset;
			parser_state = STATE_TEXT;
		} else if (ch == '&') {
			//has_amp = true;
			//temp_offset = offset;
			entityIdentifier();
			parser_state = STATE_TEXT;
		} else if (ch == ']') {
			if (r.skipChar(']')) {
				while (r.skipChar(']')) {
				}
				if (r.skipChar('>'))
					throw new ParseExceptionHuge(
						"Error in text content: ]]> in text content"
							+ formatLineNumber());
				
			}
			parser_state = STATE_TEXT;
		}else
			throw new ParseExceptionHuge(
				"Error in text content: Invalid char"
					+ formatLineNumber());
		return parser_state;

	}
	/**
	 * This private method process comment
	 * @return the parser state after which the parser loop jumps to
	 * @throws ParseExceptionHuge
	 * @throws EncodingExceptionHuge
	 * @throws EOFExceptionHuge
	 */
	private int process_comment() throws ParseExceptionHuge, EncodingExceptionHuge, EOFExceptionHuge{
		int parser_state;
		long length1;
		while (true) {
			ch = r.getChar();
			if (XMLChar.isValidChar(ch)) {
				if (ch == '-' && r.skipChar('-')) {
					length1 =
						offset - temp_offset -  (increment<<1);
					break;
				}
			} else
				throw new ParseExceptionHuge(
					"Error in comment: Invalid Char"
						+ formatLineNumber());
		}
		if (r.getChar() == '>') {
			//System.out.println(" " + (temp_offset) + " " + length1 + " comment " + depth);
			if (encoding < FORMAT_UTF_16BE)
				writeVTD(
					temp_offset,
					length1,
					TOKEN_COMMENT,
					depth);
			else
				writeVTD(
					temp_offset >> 1,
					length1 >> 1,
					TOKEN_COMMENT,
					depth);
			//length1 = 0;
			temp_offset = offset;
			ch = getCharAfterSe();
			if (ch == '<') {
				parser_state = STATE_LT_SEEN;
			} else if (XMLChar.isContentChar(ch)) {
				//temp_offset = offset;
				parser_state = STATE_TEXT;
			} else if (ch == '&') {
				//has_amp = true;
				//temp_offset = offset;
				entityIdentifier();
				parser_state = STATE_TEXT;
			} else if (ch == ']') {
				if (r.skipChar(']')) {
					while (r.skipChar(']')) {
					}
					if (r.skipChar('>'))
						throw new ParseExceptionHuge(
							"Error in text content: ]]> in text content"
								+ formatLineNumber());
				}
				parser_state = STATE_TEXT;
			}else
				throw new ParseExceptionHuge(
					"Error in text content: Invalid char"
						+ formatLineNumber());
			return parser_state;
		} else
			throw new ParseExceptionHuge(
				"Error in comment: Invalid terminating sequence"
					+ formatLineNumber());
	}
	private int process_end_doc() throws ParseExceptionHuge, EncodingExceptionHuge, EOFExceptionHuge {
	    int parser_state;
		ch = getCharAfterS();
		/* eof exception should be thrown here for premature ending*/
		if (ch == '<') {

			if (r.skipChar('?')) {
				/* processing instruction after end tag of root element*/
				temp_offset = offset;
				parser_state = STATE_END_PI;
				return parser_state;
			} else if (
				r.skipChar('!')
					&& r.skipChar('-')
					&& r.skipChar('-')) {
				// comments allowed after the end tag of the root element
				temp_offset = offset;
				parser_state = STATE_END_COMMENT;
				return parser_state;
			}
		}
		throw new ParseExceptionHuge(
			"Other Error: XML not terminated properly"
				+ formatLineNumber());
	}
	private int process_qm_seen()throws ParseExceptionHuge, EncodingExceptionHuge, EOFExceptionHuge {
	    temp_offset = offset;
		ch = r.getChar();
		if (XMLChar.isNameStartChar(ch)) {
			//temp_offset = offset;
			if ((ch == 'x' || ch == 'X')
				&& (r.skipChar('m')	|| r.skipChar('M'))
				&& (r.skipChar('l')	|| r.skipChar('L'))) {
				ch = r.getChar();
				if (ch == '?'
					|| XMLChar.isSpaceChar(ch))
					throw new ParseExceptionHuge(
						"Error in PI: [xX][mM][lL] not a valid PI targetname"
							+ formatLineNumber());
				offset = getPrevOffset();
			}
			return STATE_PI_TAG;
		}
		throw new ParseExceptionHuge(
			"Other Error: First char after <? invalid"
				+ formatLineNumber());
	}
	
	private int process_ex_seen()throws ParseExceptionHuge, EncodingExceptionHuge, EOFExceptionHuge {
	    int parser_state;
	    boolean hasDTD = false;
	    ch = r.getChar();
		switch (ch) {
			case '-' :
				if (r.skipChar('-')) {
					temp_offset = offset;
					parser_state = STATE_COMMENT;
					break;
				} else
					throw new ParseExceptionHuge(
						"Error in comment: Invalid char sequence to start a comment"
							+ formatLineNumber());
			case '[' :
				if (r.skipChar('C')
					&& r.skipChar('D')
					&& r.skipChar('A')
					&& r.skipChar('T')
					&& r.skipChar('A')
					&& r.skipChar('[')
					&& (depth != -1)) {
					temp_offset = offset;
					parser_state = STATE_CDATA;
					break;
				} else {
					if (depth == -1)
						throw new ParseExceptionHuge(
							"Error in CDATA: Wrong place for CDATA"
								+ formatLineNumber());
					throw new ParseExceptionHuge(
						"Error in CDATA: Invalid char sequence for CDATA"
							+ formatLineNumber());
				}

			case 'D' :
				if (r.skipChar('O')
					&& r.skipChar('C')
					&& r.skipChar('T')
					&& r.skipChar('Y')
					&& r.skipChar('P')
					&& r.skipChar('E')
					&& (depth == -1)
					&& !hasDTD) {
					hasDTD = true;
					temp_offset = offset;
					parser_state = STATE_DOCTYPE;
					break;
				} else {
					if (hasDTD == true)
						throw new ParseExceptionHuge(
							"Error for DOCTYPE: Only DOCTYPE allowed"
								+ formatLineNumber());
					if (depth != -1)
						throw new ParseExceptionHuge(
							"Error for DOCTYPE: DTD at wrong place"
								+ formatLineNumber());
					throw new ParseExceptionHuge(
						"Error for DOCTYPE: Invalid char sequence for DOCTYPE"
							+ formatLineNumber());
				}
			default :
				throw new ParseExceptionHuge(
					"Other Error: Unrecognized char after <!"
						+ formatLineNumber());
		}
		return parser_state;
	}
	private int process_start_doc()throws ParseExceptionHuge, EncodingExceptionHuge, EOFExceptionHuge {
	    
	    int c = r.getChar();
		if (c == '<') {
			temp_offset = offset;
			// xml decl has to be right after the start of the document
			if (r.skipChar('?')
				&& (r.skipChar('x') || r.skipChar('X'))
				&& (r.skipChar('m') || r.skipChar('M'))
				&& (r.skipChar('l') || r.skipChar('L'))) {
				if (r.skipChar(' ')
					|| r.skipChar('\t')
					|| r.skipChar('\n')
					|| r.skipChar('\r')) {
					ch = getCharAfterS();
					temp_offset = offset;
					return STATE_DEC_ATTR_NAME;
				} else if (r.skipChar('?'))
					throw new ParseExceptionHuge(
						"Error in XML decl: Premature ending"
							+ formatLineNumber());
			}
			offset = temp_offset;
			return STATE_LT_SEEN;
		}else if (c==' '||c=='\n'||c=='\r'||c=='\t'){
			if (getCharAfterS()=='<'){
				return STATE_LT_SEEN;
			}
		}
		throw new ParseExceptionHuge(
			"Other Error: XML not starting properly"
				+ formatLineNumber());
	}
	/**
	 * This private method processes CDATA section
	 * @return the parser state after which the parser loop jumps to
	 * @throws ParseExceptionHuge
	 * @throws EncodingExceptionHuge
	 * @throws EOFExceptionHuge
	 */
	private int process_cdata() throws ParseExceptionHuge, EncodingExceptionHuge, EOFExceptionHuge{
		int parser_state;
		long length1;
		while (true) {
			ch = r.getChar();
			if (XMLChar.isValidChar(ch)) {
				if (ch == ']' && r.skipChar(']')) {
					while (r.skipChar(']'));
					if (r.skipChar('>')) {
						break;
					} /*else
						throw new ParseExceptionHuge(
							"Error in CDATA: Invalid termination sequence"
								+ formatLineNumber());*/
				}
			} else
				throw new ParseExceptionHuge(
					"Error in CDATA: Invalid Char"
						+ formatLineNumber());
		}
		length1 = offset - temp_offset -  (increment<<1) - increment;
		if (encoding < FORMAT_UTF_16BE){
			
			writeVTD(
				temp_offset,
				length1,
				TOKEN_CDATA_VAL,
				depth);
		}
		else {
			
			writeVTD(
				temp_offset >> 1,
				length1 >> 1,
				TOKEN_CDATA_VAL,
				depth);
		}
		//System.out.println(" " + (temp_offset) + " " + length1 + " CDATA " + depth);
		ch = getCharAfterSe();
		if (ch == '<') {
			parser_state = STATE_LT_SEEN;
		} else if (XMLChar.isContentChar(ch)) {
			temp_offset = offset-1;
			parser_state = STATE_TEXT;
		} else if (ch == '&') {
			//has_amp = true;
			temp_offset = offset-1;
			entityIdentifier();
			parser_state = STATE_TEXT;
			//temp_offset = offset;
		} else if (ch == ']') {
		    temp_offset = offset-1;
			if (r.skipChar(']')) {
				while (r.skipChar(']')) {
				}
				if (r.skipChar('>'))
					throw new ParseExceptionHuge(
						"Error in text content: ]]> in text content"
							+ formatLineNumber());
			}
			parser_state = STATE_TEXT;
		}else
			throw new ParseExceptionHuge(
				"Other Error: Invalid char in xml"
					+ formatLineNumber());
		return parser_state;
	}
	
	/**
	 * This private method process DTD
	 * @return the parser state after which the parser loop jumps to
	 * @throws ParseExceptionHuge
	 * @throws EncodingExceptionHuge
	 * @throws EOFExceptionHuge
	 */
	private int process_doc_type() throws ParseExceptionHuge,EncodingExceptionHuge, EOFExceptionHuge{
		int z = 1, parser_state;
		long length1;
		while (true) {
			ch = r.getChar();
			if (XMLChar.isValidChar(ch)) {
				if (ch == '>')
					z--;
				else if (ch == '<')
					z++;
				if (z == 0)
					break;
			} else
				throw new ParseExceptionHuge(
					"Error in DOCTYPE: Invalid char"
						+ formatLineNumber());
		}
		length1 = offset - temp_offset - increment;
		/*System.out.println(
		    " " + (temp_offset) + " " + length1 + " DOCTYPE val " + depth);*/
		if (encoding < FORMAT_UTF_16BE){
			if (length1 > MAX_TOKEN_LENGTH)
				  throw new ParseExceptionHuge("Token Length Error:"
							  +" DTD val too long (>0xfffff)"
							  + formatLineNumber());
			writeVTD(
				temp_offset,
				length1,
				TOKEN_DTD_VAL,
				depth);
		}
		else{
			if (length1 > (MAX_TOKEN_LENGTH<<1))
				  throw new ParseExceptionHuge("Token Length Error:"
							  +" DTD val too long (>0xfffff)"
							  + formatLineNumber());
			writeVTD(
				temp_offset >> 1,
				length1 >> 1,
				TOKEN_DTD_VAL,
				depth);
		}
		ch = getCharAfterS();
		if (ch == '<') {
			parser_state = STATE_LT_SEEN;
		} else
			throw new ParseExceptionHuge(
				"Other Error: Invalid char in xml"
					+ formatLineNumber());
		return parser_state;
	}
	
	/**
	 * This private method processes PI after root document 
	 * @return the parser state after which the parser loop jumps to
	 * @throws ParseExceptionHuge
	 * @throws EncodingExceptionHuge
	 * @throws EOFExceptionHuge
	 */
	private int process_end_pi() throws ParseExceptionHuge,EncodingExceptionHuge, EOFExceptionHuge{
		long length1;int parser_state;
		ch = r.getChar();
		if (XMLChar.isNameStartChar(ch)) {
			if ((ch == 'x' || ch == 'X')
				&& (r.skipChar('m') || r.skipChar('M'))
				&& (r.skipChar('l') && r.skipChar('L'))) {
				//temp_offset = offset;
				ch = r.getChar();
				if (XMLChar.isSpaceChar(ch) || ch == '?')
					throw new ParseExceptionHuge(
						"Error in PI: [xX][mM][lL] not a valid PI target"
							+ formatLineNumber());
				//offset = temp_offset;
			}

			while (true) {
				//ch = getChar();
				if (!XMLChar.isNameChar(ch)) {
					break;
				}
				ch = r.getChar();
			}

			length1 = offset - temp_offset - increment;
			/*System.out.println(
			    ""
			        + (char) XMLDoc[temp_offset]
			        + " "
			        + (temp_offset)
			        + " "
			        + length1
			        + " PI Target "
			        + depth);*/
			if (encoding < FORMAT_UTF_16BE){
				if (length1 > MAX_TOKEN_LENGTH)
					  throw new ParseExceptionHuge("Token Length Error:"
								  +"PI name too long (>0xfffff)"
								  + formatLineNumber());
				writeVTD(
					temp_offset,
					length1,
					TOKEN_PI_NAME,
					depth);
			}
			else{
				if (length1 > (MAX_TOKEN_LENGTH<<1))
				  throw new ParseExceptionHuge("Token Length Error:"
						  +"PI name too long (>0xfffff)"
						  + formatLineNumber());
				writeVTD(
					temp_offset >> 1,
					length1 >> 1,
					TOKEN_PI_NAME,
					depth);
			}
			//length1 = 0;
			temp_offset = offset;
			if (XMLChar.isSpaceChar(ch)) {
				ch = getCharAfterS();

				while (true) {
					if (XMLChar.isValidChar(ch)) {
						if (ch == '?')
							if (r.skipChar('>')) {
								parser_state = STATE_DOC_END;
								break;
							} else
								throw new ParseExceptionHuge(
									"Error in PI: invalid termination sequence"
										+ formatLineNumber());
					} else
						throw new ParseExceptionHuge(
							"Error in PI: Invalid char in PI val"
								+ formatLineNumber());
					ch = r.getChar();
				}
				length1 = offset - temp_offset - (increment<<1);
				if (encoding < FORMAT_UTF_16BE){
					if (length1 > MAX_TOKEN_LENGTH)
						  throw new ParseExceptionHuge("Token Length Error:"
									  +"PI val too long (>0xfffff)"
									  + formatLineNumber());
					writeVTD(
						temp_offset,
						length1,
						TOKEN_PI_VAL,
						depth);
				}
				else{
					if (length1 > (MAX_TOKEN_LENGTH<<1))
						  throw new ParseExceptionHuge("Token Length Error:"
									  +"PI val too long (>0xfffff)"
									  + formatLineNumber());
					writeVTD(
						temp_offset >> 1,
						length1 >> 1,
						TOKEN_PI_VAL,
						depth);
				}
				//System.out.println(" " + temp_offset + " " + length1 + " PI val " + depth);
			} else {
				if ((ch == '?') && r.skipChar('>')) {
					parser_state = STATE_DOC_END;
				} else
					throw new ParseExceptionHuge(
						"Error in PI: invalid termination sequence"
							+ formatLineNumber());
			}
			//parser_state = STATE_DOC_END;
		} else
			throw new ParseExceptionHuge("Error in PI: invalid char in PI target"
					+formatLineNumber());
		return parser_state;
	}
	/**
	 * This private method process the comment after the root document
	 * @return the parser state after which the parser loop jumps to
	 * @throws ParseExceptionHuge
	 */
	private int process_end_comment()throws ParseExceptionHuge {
		int parser_state;
		long length1;
		while (true) {
			ch = r.getChar();
			if (XMLChar.isValidChar(ch)) {
				if (ch == '-' && r.skipChar('-')) {
					length1 =
						offset - temp_offset - (increment<<1);
					break;
				}
			} else
				throw new ParseExceptionHuge(
					"Error in comment: Invalid Char"
						+ formatLineNumber());
		}
		if (r.getChar() == '>') {
			//System.out.println(" " + temp_offset + " " + length1 + " comment " + depth);
			if (encoding < FORMAT_UTF_16BE)
				writeVTD(
					temp_offset,
					length1,
					TOKEN_COMMENT,
					depth);
			else
				writeVTD(
					temp_offset >> 1,
					length1 >> 1,
					TOKEN_COMMENT,
					depth);
			//length1 = 0;
			parser_state = STATE_DOC_END;
			return parser_state;
		}
		throw new ParseExceptionHuge(
			"Error in comment: '-->' expected"
				+ formatLineNumber());
	
	}
	/**
	 * Set the XMLDoc container.
	 * @param XMLBuffer xb1
	 */
	public void setDoc(IByteBuffer xb1) {
	    xb = xb1;
	    
		int a;
		br = false;
		depth = -1;
		increment =1;
		BOM_detected = false;
		must_utf_8 = false;
		ch = ch_temp = 0;
		temp_offset = 0;
		
		docOffset = offset = 0;
		//System.out.println("doc length is "+xb.length());
		docLen = xb.length();
		endOffset = xb.length();
		last_l1_index= last_l2_index = last_l3_index = last_depth =0;
		int i1=7,i2=9,i3=11;
		if (docLen <= 1024) {
			//a = 1024; //set the floor
			a = 6; i1=5; i2=5;i3=5;
		} else if (docLen <=4096){
		    a = 7; i1=6; i2=6; i3=6;
		}else if (docLen <=1024*16){
		    a =8; i1 = 7;i2=7;i3=7;
		}else if (docLen <= 1024 * 16 * 4) {
			//a = 2048;
			a = 11;
		} else if (docLen <= 1024 * 256) {
			//a = 1024 * 4;
			a = 12;
		} else if (docLen <= (1<<26)){
			//a = 1 << 15;
		    i1 = i2 = i3 = 12;
			a = 15;
		} else if (docLen <= (1<<30 )){
			//a = 1 << 15;
		    i1 = i2 = i3 = 13;
			a = 19;
		} else {
		    i1 = i2 = i3 = 16;
		    a = 23;
		}
		
		VTDBuffer = new FastLongBuffer(a, (int) (xb.length()>> (a+1)));
		l1Buffer = new FastLongBuffer(i1);
		l2Buffer = new FastLongBuffer(i2);
		l3Buffer = new FastIntBuffer(i3);
	}
	/**
	 * Write the VTD and LC into their storage container.
	 * @param offset int
	 * @param length int
	 * @param token_type int
	 * @param depth int
	 */
	private void writeVTD(long offset, long length, int token_type, int depth) {
	    /*System.out.print(" type "+token_type);
	    System.out.print(" length "+ Long.toHexString(length)+" length "+length);
	    System.out.print(" prefix length " + (length>>10));
	    System.out.print(" qn length " + (length & 0x3ff));
	    System.out.print(" offset "+offset);
	    System.out.println(" depth "+depth);*/
			switch (token_type) {
			case TOKEN_CHARACTER_DATA:
			case TOKEN_CDATA_VAL:
			case TOKEN_COMMENT:

			if (length > MAX_TOKEN_LENGTH) {
				long k;
				long r_offset = offset;
				for (k = length; k > MAX_TOKEN_LENGTH; k = k - MAX_TOKEN_LENGTH) {
					VTDBuffer.append(((long) ((token_type << 22)
							| ((depth & 0x1f) << 17) | MAX_TOKEN_LENGTH) << 38)
							| r_offset);
					/*VTDBuffer.append(((long) ((token_type << 28)
							| ((depth & 0xff) << 20) | MAX_TOKEN_LENGTH) << 32)
							| r_offset);*/
					r_offset += MAX_TOKEN_LENGTH;
				}
				VTDBuffer.append(((long) ((token_type << 22)
						| ((depth & 0x1f) << 17) | k) << 38)
						| r_offset);
				/*VTDBuffer.append(((long) ((token_type << 28)
						| ((depth & 0xff) << 20) | k) << 32)
						| r_offset);*/
			} else {
				VTDBuffer.append(((long) ((token_type << 22)
						| ((depth & 0x1f) << 17) | length) << 38)
						| offset);
				/*VTDBuffer.append(((long) ((token_type << 28)
						| ((depth & 0xff) << 20) | length) << 32)
						| offset);*/
			}
			break;
			
			//case TOKEN_ENDING_TAG: break;
		default:
			VTDBuffer.append(((long) ((token_type << 22)
					| ((depth & 0x1f) << 17) | length) << 38)
					| offset);
			/*VTDBuffer.append(((long) ((token_type << 28)
			        | ((depth & 0xff) << 20) | length) << 32)
			        | offset);*/
		}
		// remember VTD depth start from zero
		if (token_type == TOKEN_STARTING_TAG) {
			switch (depth) {
			case 0:
				rootIndex = VTDBuffer.size() - 1;
				break;
			case 1:
				if (last_depth == 1) {
					l1Buffer.append(((long) last_l1_index << 32) | 0xffffffffL);
				} else if (last_depth == 2) {
					l2Buffer.append(((long) last_l2_index << 32) | 0xffffffffL);
				}
				last_l1_index = VTDBuffer.size() - 1;
				last_depth = 1;
				break;
			case 2:
				if (last_depth == 1) {
					l1Buffer.append(((long) last_l1_index << 32)
							+ l2Buffer.size());
				} else if (last_depth == 2) {
					l2Buffer.append(((long) last_l2_index << 32) | 0xffffffffL);
				}
				last_l2_index = VTDBuffer.size() - 1;
				last_depth = 2;
				break;

			case 3:
				l3Buffer.append(VTDBuffer.size() - 1);
				if (last_depth == 2) {
					l2Buffer.append(((long) last_l2_index << 32)
							+ l3Buffer.size() - 1);
				}
				last_depth = 3;
				break;
			default:
			//rootIndex = VTDBuffer.size() - 1;
			}

		} /*else if (token_type == TOKEN_ENDING_TAG && (depth == 0)) {
			if (last_depth == 1) {
				l1Buffer.append(((long) last_l1_index << 32) | 0xffffffffL);
			} else if (last_depth == 2) {
				l2Buffer.append(((long) last_l2_index << 32) | 0xffffffffL);
			}
		}*/
	}
}

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

/*
 *
 * This class is created to update VTDNav's implementation with 
 * a more thread safe version
 */
package com.ximpleware;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import com.ximpleware.parser.ISO8859_15;
import com.ximpleware.parser.ISO8859_14;
import com.ximpleware.parser.ISO8859_13;
import com.ximpleware.parser.ISO8859_11;
import com.ximpleware.parser.ISO8859_10;
import com.ximpleware.parser.ISO8859_2;
import com.ximpleware.parser.ISO8859_3;
import com.ximpleware.parser.ISO8859_4;
import com.ximpleware.parser.ISO8859_5;
import com.ximpleware.parser.ISO8859_6;
import com.ximpleware.parser.ISO8859_7;
import com.ximpleware.parser.ISO8859_8;
import com.ximpleware.parser.ISO8859_9;
import com.ximpleware.parser.UTF8Char;
import com.ximpleware.parser.WIN1250;
import com.ximpleware.parser.WIN1251;
import com.ximpleware.parser.WIN1252;
import com.ximpleware.parser.WIN1253;
import com.ximpleware.parser.WIN1254;
import com.ximpleware.parser.WIN1255;
import com.ximpleware.parser.WIN1256;
import com.ximpleware.parser.WIN1257;
import com.ximpleware.parser.WIN1258;
/**
 * The VTD Navigator allows one to navigate XML document represented in VTD
 * records and Location caches. There is one and only one cursor that you can
 * navigate to any part of the tree. If a method operating on a node doesn't
 * accept the node as input, by default it refers to the cursor element. The
 * hierarchy consists entirely of elements.
 */
public class VTDNav {
	// Navigation directions
	public final static int ROOT = 0;
	public final static int PARENT = 1;
	public final static int FIRST_CHILD = 2;
	public final static int LAST_CHILD = 3;
	public final static int NEXT_SIBLING = 4;
	public final static int PREV_SIBLING = 5;

	// Navigation directions
	public final static int R = 0;
	public final static int P = 1;
	public final static int FC = 2;
	public final static int LC = 3;
	public final static int NS = 4;
	public final static int PS = 5;

	// token type definitions
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
	public final static int TOKEN_DOCUMENT =13;

	// encoding format definition here
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
	
	// String style
	public final static int STRING_RAW =0;
	public final static int STRING_REGULAR = 1;
	public final static int STRING_NORMALIZED = 2;
	
	// masks for obtaining various fields from a VTD token
	protected final static long MASK_TOKEN_FULL_LEN = 0x000fffff00000000L;
	protected final static long MASK_TOKEN_PRE_LEN = 0x000ff80000000000L;
	protected final static long MASK_TOKEN_QN_LEN = 0x000007ff00000000L;
	protected long MASK_TOKEN_OFFSET = 0x000000003fffffffL;
	protected final static long MASK_TOKEN_TYPE = 0xf000000000000000L;
	protected final static long MASK_TOKEN_DEPTH = 0x0ff0000000000000L;

	// tri-state variable for namespace lookup
	protected final static long MASK_TOKEN_NS_MARK = 0x00000000c0000000L;
	protected short maxLCDepthPlusOne =4;

	protected int rootIndex; // where the root element is at
	protected int nestingLevel;
	protected int[] context; // main navigation tracker aka context object
    protected boolean atTerminal; // this variable is to make vn compatible with
    								// xpath's data model
	
	
	// location cache part
	protected int l2upper;
	protected int l2lower;
	protected int l3upper;
	protected int l3lower;
	protected int l2index;
	protected int l3index;
	protected int l1index;

	// containers
	protected FastLongBuffer vtdBuffer;
	protected FastLongBuffer l1Buffer;
	protected FastLongBuffer l2Buffer;
	protected FastIntBuffer l3Buffer;
	protected IByteBuffer XMLDoc;

	//private int recentNS; // most recently visited NS node, experiment for
    // now
	// Hierarchical representation is an array of integers addressing elements
    // tokens
	protected ContextBuffer contextStack;
	protected ContextBuffer contextStack2;// this is reserved for XPath

	protected int LN; // record txt and attrbute for XPath eval purposes
	// the document encoding
	protected int encoding;
	//protected boolean writeOffsetAdjustment;
	// for string to token comparison
	//protected int currentOffset;
	//protected int currentOffset2;

	// whether the navigation is namespace enabled or not.
	protected boolean ns;

	// intermediate buffer for push and pop purposes
	protected int[] stackTemp;
	protected int docOffset;
	// length of the document
	protected int docLen;
	protected int vtdSize; //vtd record count
	
	protected String name;
	protected int nameIndex;
	
	protected String localName;
	protected int localNameIndex;
	protected FastIntBuffer fib;//for store string value 
	protected boolean shallowDepth;
	protected BookMark currentNode;
	protected String URIName;
	protected int count;
	
	protected VTDNav(){}
	
	/**
     * Initialize the VTD navigation object.
     * 
     * @param RootIndex
     *            int
     * @param maxDepth
     *            int
     * @param encoding
     *            int
     * @param NS
     *            boolean
     * @param x
     *            byte[]
     * @param vtd
     *            com.ximpleware.ILongBuffer
     * @param l1
     *            com.ximpleware.ILongBuffer
     * @param l2
     *            com.ximpleware.ILongBuffer
     * @param l3
     *            com.ximpleware.IIntBuffer
     * @param so
     *            int starting offset of the document(in byte)
     * @param length
     *            int length of the document (in byte)
     */
	protected VTDNav(
		int RootIndex,
		int enc,
		boolean NS,
		int depth,
		IByteBuffer x,
		FastLongBuffer vtd,
		FastLongBuffer l1,
		FastLongBuffer l2,
		FastIntBuffer l3,
		int so, // start offset of the starting offset(in byte)
	int length) // lengnth of the XML document (in byte))
	{
		// initialize all buffers
		if (l1 == null
			|| l2 == null
			|| l3 == null
			|| vtd == null
			|| x == null
			|| depth < 0
			|| RootIndex < 0 //|| encoding <= FORMAT_UTF8
			//|| encoding >= FORMAT_ISO_8859_1
			|| so < 0
			|| length < 0) {
			throw new IllegalArgumentException();
		}
		count=0;
		l1Buffer = l1;
		l2Buffer = l2;
		l3Buffer = l3;
		vtdBuffer = vtd;
		XMLDoc = x;

		encoding = enc;
		//System.out.println("encoding " + encoding);
		rootIndex = RootIndex;
		nestingLevel = depth + 1;
		ns = NS; // namespace aware or not
		if (ns == false)
		    MASK_TOKEN_OFFSET = 0x000000007fffffffL; // this allows xml size to
                                                     // be 2GB
		else // if there is no namespace
		    MASK_TOKEN_OFFSET = 0x000000003fffffffL;
		
		
		atTerminal = false; //this variable will only change value during XPath
                            // eval

		// initialize the context object
		this.context = new int[nestingLevel];
		//depth value is the first entry in the context because root is
        // singular.
		context[0] = 0;
		//set the value to zero
		for (int i = 1; i < nestingLevel; i++) {
			context[i] = -1;
		}
		//currentOffset = 0;
		//contextStack = new ContextBuffer(1024, nestingLevel + 7);
		contextStack = new ContextBuffer(10, nestingLevel + 9);
		contextStack2 = new ContextBuffer(10, nestingLevel+9);
		stackTemp = new int[nestingLevel + 9];

		// initial state of LC variables
		l1index = l2index = l3index = -1;
		l2lower = l3lower = -1;
		l2upper = l3upper = -1;
		docOffset = so;
		docLen = length;
		//System.out.println("offset " + offset + " length " + length);
		//printL2Buffer();
		vtdSize = vtd.size;
		//writeOffsetAdjustment = false;
		//recentNS = -1;
		name  = null;
		nameIndex = -1;
		localName = null;
		localNameIndex = -1;
		fib = new FastIntBuffer(5); // page size is 32 ints
		shallowDepth = true;
	}
	/**
     * Return the attribute count of the element at the cursor position. when ns
     * is false, attr_ns tokens are considered attributes; otherwise, ns tokens
     * are not considered attributes
     * 
     * @return int
     */
	public int getAttrCount() {
	    if (context[0]==-1)return 0;
		int count = 0;
		int index = getCurrentIndex() + 1;
		while (index < vtdSize) {
			int type = getTokenType(index);
			if (type == TOKEN_ATTR_NAME
				|| type == TOKEN_ATTR_VAL
				|| type == TOKEN_ATTR_NS) {
				if (type == TOKEN_ATTR_NAME
					|| (!ns && (type == TOKEN_ATTR_NS))) {
					count++;
				}
			} else
				break;
			index++;
		}
		return count;
	}
	/**
     * Get the token index of the attribute value given an attribute name.
     * 
     * @return int (-1 if no such attribute name exists)
     * @param an
     *            java.lang.String
     * @exception com.ximpleware.NavException
     *                The exception if the underlying byte content contains
     *                various errors. Notice that we are being conservative in
     *                making little assumption on the correctness of underlying
     *                byte content. This is because the VTD etc can be generated
     *                by another machine from a load-balancer.
     * @exception IllegalArguementException
     *                if an is null
     */
	public int getAttrVal(String an) throws NavException {
		//int size = vtdBuffer.size();
		if (context[0]==-1 /*|| atTerminal*/)
			return -1;
		int index = (context[0] != 0) ? context[context[0]] + 1 : rootIndex + 1;
		
		int type;
		if (index<vtdSize)
		   type= getTokenType(index);
		else
			return -1;
		if (ns == false) {
			while ((type == TOKEN_ATTR_NAME || type == TOKEN_ATTR_NS)) {
				if (matchRawTokenString(index,
					an)) { // ns node visible only ns is disabled
					return index + 1;
				}
				index += 2;
				if (index >= vtdSize)
					break;
				type = getTokenType(index);
			}
		} else {
			while ((type == TOKEN_ATTR_NAME || type == TOKEN_ATTR_NS)) {
				if (type == TOKEN_ATTR_NAME
					&& matchRawTokenString(
						index,
						an)) { // ns node visible only ns is disabled
					return index + 1;
				}
				index += 2;
				if (index>=vtdSize)
					break;
				type = getTokenType(index);
			}
		}
		return -1;
	} 
	/**
     * Get the token index of the attribute value of given URL and local name.
     * If ns is not enabled, the lookup will return -1, indicating a no-found.
     * Also namespace nodes are invisible using this method. One can't use * to
     * indicate any name space because * is ambiguous!!
     * 
     * @return int (-1 if no matching attribute found)
     * @param URL
     *            java.lang.String (Name space URL)
     * @param ln
     *            java.lang.String (local name)
     * @exception com.ximpleware.NavException
     *                The exception if the underlying byte content contains
     *                various errors. Notice that we are being conservative in
     *                making little assumption on the correctness of underlying
     *                byte content. This is because the VTD etc can be generated
     *                by another machine from a load-balancer.
     * @exception IllegalArguementException
     *                if s is null
     */
    public int getAttrValNS(String URL, String ln) throws NavException {
    	if (!ns || context[0]==-1 /*|| atTerminal*/)
    		return -1;
    	if (URL!=null && URL.length()==0)
        	URL = null;
    	if (URL == null)
    		return getAttrVal(ln);
    	int size = vtdBuffer.size;
    	int index = (context[0] != 0) ? context[context[0]] + 1 : rootIndex + 1;
    	// point to the token next to the element tag
    	int type;
    	if (index<vtdSize)
    		type = getTokenType(index);
    	else 
    		return -1;
    	while (index < size
    		&& (type == TOKEN_ATTR_NAME || type == TOKEN_ATTR_NS)) {
    		int i = getTokenLength(index);
    		int offset = getTokenOffset(index);
    		int preLen = (i >> 16) & 0xffff;
    		int fullLen = i & 0xffff;
    		if (preLen != 0
    			// attribute name without a prefix is not bound to any
                // namespaces
    			&& matchRawTokenString(
    				offset + preLen + 1,
    				fullLen - preLen - 1,
    				ln)
    			&& resolveNS(URL, offset, preLen)) {
    			return index + 1;
    		} else if (	preLen==3 
    				&& matchRawTokenString(offset, 3,"xml")
    				&& URL.equals("http://www.w3.org/XML/1998/namespace")){
    			// prefix matches "xml"
    			return index+1;
    		}
    		index += 2;
    		if (index>=vtdSize)
    			break;
    		type = getTokenType(index);
    	}
    	return -1;
    }
	private long handle_utf8(long temp, int offset) throws NavException {
        int c, d, a; 
        
        long val;
        switch (UTF8Char.byteCount((int)temp & 0xff)) {
        case 2:
            c = 0x1f;
            d = 6;
            a = 1;
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
            throw new NavException("UTF 8 encoding error: should never happen");
        }

        val = (temp & c) << d;
        int i = a - 1;
        while (i >= 0) {
            temp = XMLDoc.byteAt(offset + a - i);
            if ((temp & 0xc0) != 0x80)
                throw new NavException(
                        "UTF 8 encoding error: should never happen");
            val = val | ((temp & 0x3f) << ((i << 2) + (i << 1)));
            i--;
        }
        //currentOffset += a + 1;
        return val | (((long)(a+1))<<32);
    }


	private long handle_utf16le(int offset) throws NavException {
		// implement UTF-16LE to UCS4 conversion
		int val, temp =
			(XMLDoc.byteAt((offset << 1) + 1 ) & 0xff)
				<< 8 | (XMLDoc.byteAt(offset << 1) & 0xff);
		if (temp < 0xdc00 || temp > 0xdfff) { // check for low surrogate
			if (temp == '\r') {
				if (XMLDoc.byteAt((offset << 1) + 2) == '\n'
					&& XMLDoc.byteAt((offset << 1) + 3) == 0) {
					return '\n' | (2L<<32) ;
				} else {
					return '\n' | (1L<<32);
				}
			}
			return temp | (1L<<32);
		} else {
			if (temp<0xd800 || temp>0xdbff)				
				throw new NavException("UTF 16 LE encoding error: should never happen");
			val = temp;
			temp =
				(XMLDoc.byteAt((offset << 1) + 3)&0xff)
					<< 8 | (XMLDoc.byteAt((offset << 1) + 2) & 0xff);
			if (temp < 0xdc00 || temp > 0xdfff) {
				// has to be high surrogate
				throw new NavException("UTF 16 LE encoding error: should never happen");
			}
			val = ((temp - 0xd800)<<10) + (val - 0xdc00) + 0x10000;
			
			return val | (2L<<32);
		}
		//System.out.println("UTF 16 LE unimplemented for now");
	}

	private long handle_utf16be(int offset) throws NavException{
		long val; 
		
		int temp =
			((XMLDoc.byteAt(offset << 1) & 0xff)	<< 8) 
					|(XMLDoc.byteAt((offset << 1) + 1)& 0xff);
		if ((temp < 0xd800)
			|| (temp > 0xdfff)) { // not a high surrogate
			if (temp == '\r') {
				if (XMLDoc.byteAt((offset << 1) + 3) == '\n'
					&& XMLDoc.byteAt((offset << 1) + 2) == 0) {
					
					return '\n'|(2L<<32);
				} else {
					return '\n'|(1L<<32);
				}
			}
			//currentOffset++;
			return temp| (1L<<32);
		} else {
			if (temp<0xd800 || temp>0xdbff)				
				throw new NavException("UTF 16 BE encoding error: should never happen");
			val = temp;
			temp =
				((XMLDoc.byteAt((offset << 1) + 2) & 0xff)
					<< 8) | (XMLDoc.byteAt((offset << 1 )+ 3) & 0xff);
			if (temp < 0xdc00 || temp > 0xdfff) {
				// has to be a low surrogate here
				throw new NavException("UTF 16 BE encoding error: should never happen");
			}
			val = ((temp - 0xd800) << 10) + (val - 0xdc00) + 0x10000;
			//currentOffset += 2;
			return val | (2L<<32);
		}
	}

	private long getChar4OtherEncoding(int offset) throws NavException{
	    if (encoding <= FORMAT_WIN_1258){
	        int	temp = decode(offset);
	        if (temp == '\r') {
	            if (XMLDoc.byteAt(offset + 1) == '\n') {
	                return '\n'|(2L<<32);
	            } else {
				return '\n'|(1L<<32);
	            }
	        }
	        return temp|(1L<<32);
	    }
	    throw new NavException("Unknown Encoding");
	}
	/**
     * This method decodes the underlying byte array into corresponding UCS2
     * char representation . It doesn't resolves built-in entity and character
     * references. Length will never be zero Creation date: (11/21/03 6:26:17
     * PM)
     * 
     * @return int
     * @exception com.ximpleware.NavException
     *                The exception is thrown if the underlying byte content
     *                contains various errors. Notice that we are being
     *                conservative in making little assumption on the
     *                correctness of underlying byte content. This is because
     *                the VTD can be generated by another machine, e.g. from a
     *                load-balancer.
     */
	private long getChar(int offset) throws NavException {
		long temp = 0;
		//int a, c, d;
		//int val;
		//int ch;
		//int inc;
		//a = c = d = val = 0;

		switch (encoding) {
			case FORMAT_ASCII : // ascii is compatible with UTF-8, the offset
                                // value is bytes
				temp = XMLDoc.byteAt(offset);
				if (temp == '\r') {
					if (XMLDoc.byteAt(offset + 1) == '\n') {
						return '\n'|(2L<<32);
					} else {
						return '\n'|(1L<<32);
					}
				}
				
				return temp|(1L<<32);
				
			case FORMAT_ISO_8859_1 :
				temp = XMLDoc.byteAt(offset);
				if (temp == '\r') {
					if (XMLDoc.byteAt(offset + 1) == '\n') {
						return '\n'|(2L<<32);
					} else {
						return '\n'|(1L<<32);
					}
				}
				
				return (temp & 0xff)|(1L<<32);
				
			case FORMAT_UTF8 :
				temp = XMLDoc.byteAt(offset);
				if (temp>=0){
					if (temp == '\r') {
						if (XMLDoc.byteAt(offset + 1) == '\n') {
							return '\n'|(2L<<32);
						} else {
							return '\n'|(1L<<32);
						}
					}
					//currentOffset++;
					return temp|(1L<<32);
				}				
				return handle_utf8(temp,offset);

			case FORMAT_UTF_16BE :
			    return handle_utf16be(offset);

			case FORMAT_UTF_16LE :
			    return handle_utf16le(offset);

			default :
			    return getChar4OtherEncoding(offset);
				//throw new NavException("Unknown Encoding");
		}
	}
	/*
     * the exact same copy of getChar except it operates on currentOffset2 this
     * is needed to compare VTD tokens directly
     */
	

	/**
     * This method decodes the underlying byte array into corresponding UCS2
     * char representation . Also it resolves built-in entity and character
     * references.
     * 
     * @return int
     * @exception com.ximpleware.NavException
     *                The exception is thrown if the underlying byte content
     *                contains various errors. Notice that we are being
     *                conservative in making little assumption on the
     *                correctness of underlying byte content. This is because
     *                the VTD can be generated by another machine from a
     *                load-balancer.
     */
	private long getCharResolved(int offset) throws NavException {
		int ch = 0;
		int val = 0;
		long inc =2;
		long l = getChar(offset);
		
		ch = (int)l;
		
		if (ch != '&')
			return l;
		
		// let us handle references here
		//currentOffset++;
		offset++;
		ch = getCharUnit(offset);
		offset++;
		switch (ch) {
			case '#' :
			    	
				ch = getCharUnit(offset);

				if (ch == 'x') {
					while (true) {
						offset++;
						inc++;
						ch = getCharUnit(offset);

						if (ch >= '0' && ch <= '9') {
							val = (val << 4) + (ch - '0');
						} else if (ch >= 'a' && ch <= 'f') {
							val = (val << 4) + (ch - 'a' + 10);
						} else if (ch >= 'A' && ch <= 'F') {
							val = (val << 4) + (ch - 'A' + 10);
						} else if (ch == ';') {
							inc++;
							break;
						} else
							throw new NavException("Illegal char in a char reference");
					}
				} else {
					while (true) {

						ch = getCharUnit(offset);
						offset++;
						inc++;
						if (ch >= '0' && ch <= '9') {
							val = val * 10 + (ch - '0');
						} else if (ch == ';') {
							break;
						} else
							throw new NavException("Illegal char in char reference");
					
					}
				}
				break;

			case 'a' :
				ch = getCharUnit(offset);
				if (ch == 'm') {
					if (getCharUnit(offset + 1) == 'p'
						&& getCharUnit(offset + 2) == ';') {
						inc = 5;
						val = '&';
					} else
						throw new NavException("illegal builtin reference");
				} else if (ch == 'p') {
					if (getCharUnit(offset + 1) == 'o'
						&& getCharUnit(offset + 2) == 's'
						&& getCharUnit(offset + 3) == ';') {
						inc = 6;
						val = '\'';
					} else
						throw new NavException("illegal builtin reference");
				} else
					throw new NavException("illegal builtin reference");
				break;

			case 'q' :

				if (getCharUnit(offset) == 'u'
					&& getCharUnit(offset + 1) == 'o'
					&& getCharUnit(offset + 2) == 't'
					&& getCharUnit(offset + 3) ==';') {
					inc = 6;
					val = '\"';
				} else
					throw new NavException("illegal builtin reference");
				break;
			case 'l' :
				if (getCharUnit(offset) == 't'
					&& getCharUnit(offset + 1) == ';') {
					//offset += 2;
					inc = 4;
					val = '<';
				} else
					throw new NavException("illegal builtin reference");
				break;
			case 'g' :
				if (getCharUnit(offset) == 't'
					&& getCharUnit(offset + 1) == ';') {
					inc = 4;
					val = '>';
				} else
					throw new NavException("illegal builtin reference");
				break;

			default :
				throw new NavException("Invalid entity char");

		}

		//currentOffset++;
		return val | (inc << 32);
	}
	

	
	private int decode(int offset){
	    byte ch = XMLDoc.byteAt(offset);
	    switch(encoding){
        case FORMAT_ISO_8859_2:
            return ISO8859_2.decode(ch);
        case FORMAT_ISO_8859_3:
            return ISO8859_3.decode(ch);
        case FORMAT_ISO_8859_4:
            return ISO8859_4.decode(ch);
        case FORMAT_ISO_8859_5:
            return ISO8859_5.decode(ch);
        case FORMAT_ISO_8859_6:
            return ISO8859_6.decode(ch);
        case FORMAT_ISO_8859_7:
            return ISO8859_7.decode(ch);
        case FORMAT_ISO_8859_8:
            return ISO8859_8.decode(ch);
        case FORMAT_ISO_8859_9:
            return ISO8859_9.decode(ch);
        case FORMAT_ISO_8859_10:
            return ISO8859_10.decode(ch);
        case FORMAT_ISO_8859_11:
            return ISO8859_11.decode(ch);
        case FORMAT_ISO_8859_13:
            return ISO8859_13.decode(ch);
        case FORMAT_ISO_8859_14:
            return ISO8859_14.decode(ch);
        case FORMAT_ISO_8859_15:
            return ISO8859_15.decode(ch);
        case FORMAT_WIN_1250:
            return WIN1250.decode(ch);
        case FORMAT_WIN_1251:
            return WIN1251.decode(ch);
        case FORMAT_WIN_1252:
            return WIN1252.decode(ch);
        case FORMAT_WIN_1253:
            return WIN1253.decode(ch);
        case FORMAT_WIN_1254:
            return WIN1254.decode(ch);
        case FORMAT_WIN_1255:
            return WIN1255.decode(ch);
        case FORMAT_WIN_1256:
            return WIN1256.decode(ch);
        case FORMAT_WIN_1257:
            return WIN1257.decode(ch);
		default:
		    return WIN1258.decode(ch);
	    }
	}
	
	/**
     * Dump the in memory XML text into output stream
     * 
     * @param os
     * @throws java.io.IOException
     *  
     */
	public void dumpXML(OutputStream os) throws java.io.IOException{
	    os.write(this.XMLDoc.getBytes(),this.docOffset,this.docLen);
	}
	
	/**
     * Dump the in-memory copy of XML text into a file
     * 
     * @param fileName
     * @throws java.io.IOException
     *  
     */
	public void dumpXML(String fileName) throws java.io.IOException{
	    FileOutputStream fos = new FileOutputStream(fileName);
	    try{
	        dumpXML(fos);
	    }
	    finally{
	        fos.close();
	    }
	}
	/**
     * Get the next char unit which gets decoded automatically
     * 
     * @return int
     */
	private int getCharUnit(int offset) {
		return (encoding <= 2)
			? XMLDoc.byteAt(offset) & 0xff
			: (encoding <= FORMAT_WIN_1258)
			? decode(offset):(encoding == FORMAT_UTF_16BE)
			? (((int)XMLDoc.byteAt(offset << 1))
				<< 8 | XMLDoc.byteAt((offset << 1) + 1))
			: (((int)XMLDoc.byteAt((offset << 1) + 1))
				<< 8 | XMLDoc.byteAt(offset << 1));
	}
	/**
     * Get the depth (>=0) of the current element. Creation date: (11/16/03
     * 6:58:22 PM)
     * 
     * @return int
     */
	final public int getCurrentDepth() {
		return context[0];
	}
	/**
     * Get the index value of the current element. Creation date: (11/16/03
     * 6:40:25 PM)
     * 
     * @return int
     */
	final public int getCurrentIndex() {
	    if (atTerminal)
	        return LN;
		return getCurrentIndex2();
		//return (context[0] == 0) ? rootIndex : context[context[0]];
	}
	
	// this one is used in iterAttr() in autoPilot
	final protected int getCurrentIndex2(){
		switch(context[0]){
		case -1: return 0;
		case 0: return rootIndex;
		default: return context[context[0]];
	}
	}
	/**
     * getElementFragmentNS returns a ns aware version of the element fragment
     * encapsulated in an ElementFragment object
     * 
     * @return an ElementFragment object
     * @throws NavException
     *  
     */
	public ElementFragmentNs getElementFragmentNs() throws NavException{
	     if (this.ns == false)
	        throw new NavException("getElementFragmentNS can only be called when parsing is ns enabled");
	     
	     FastIntBuffer fib = new FastIntBuffer(3); // init size 8
	     
	     //fill the fib with integer
	     // first get the list of name space nodes
	     int[] ia = context;
	     int d =ia[0]; // -1 for document node, 0 for root element;
	     int c = getCurrentIndex2();
	     
	     
	     int len = (c == 0 || c == rootIndex )? 0: 
	         (getTokenLength(c) & 0xffff); // get the length of qualified node
	     
	     // put the neighboring ATTR_NS nodes into the array
	     // and record the total # of them
	     int i = 0;	    
	     int count=0;
	     if (d > 0){ // depth > 0 every node except document and root element
	         int k=getCurrentIndex2()+1;
	         if (k<this.vtdSize){
	             
	             while(k<this.vtdSize){
	                 int type = this.getTokenType(k);
	                 if (type==VTDNav.TOKEN_ATTR_NAME || type==VTDNav.TOKEN_ATTR_NS)
	                 if (type == VTDNav.TOKEN_ATTR_NS){    
	                     fib.append(k);
	                     //System.out.println(" ns name ==>" + toString(k));
	                 }
	                 k+=2;
	                 //type = this.getTokenType(k);
	             }
	         }
	         count = fib.size;
	        d--; 
            while (d >= 0) {                
                // then search for ns node in the vinicity of the ancestor nodes
                if (d > 0) {
                    // starting point
                    k = ia[d]+1;
                } else {
                    // starting point
                    k = this.rootIndex+1;
                }
                if (k<this.vtdSize){
                   
                    while (k < this.vtdSize) {
                        int type = this.getTokenType(k);
                        if (type == VTDNav.TOKEN_ATTR_NAME
                                || type == VTDNav.TOKEN_ATTR_NS) {
                            boolean unique = true;
                            if (type == VTDNav.TOKEN_ATTR_NS) {
                                for (int z = 0; z < fib.size; z++) {
                                    //System.out.println("fib size ==>
                                    // "+fib.size);
                                    //if (fib.size==4);
                                    if (matchTokens(fib.intAt(z), this, k)) {
                                        unique = false;
                                        break;
                                    }
                                }
                                if (unique)
                                    fib.append(k);
                            }
                        }
                        k += 2;
                       // type = this.getTokenType(k);
                    }
                }
                d--;
            }
           // System.out.println("count ===> "+count);
            // then restore the name space node by shifting the array
            int newSz= fib.size-count;
            for (i= 0; i<newSz; i++ ){
                fib.modifyEntry(i,fib.intAt(i+count));                
            }
            fib.resize(newSz);
	     }
	     
	     long l = getElementFragment();
	     return new ElementFragmentNs(this,l,fib,len);
	}
	
	/**
	 * Return the byte offset and length of up to i sibling fragments. If 
	 * there is a i+1 sibling element, the cursor element would 
	 * move to it; otherwise, there is no cursor movement. If the cursor isn't 
	 * positioned at an element (due to XPath evaluation), then -1 will be 
	 * returned
	 * @param i number of silbing elements including the cursor element
	 * @return a long encoding byte offset (bit 31 to bit 0), length (bit 62 
	 * to bit 32) of those fragments 
	 * @throws NavException
	 */
	public long getSiblingElementFragments(int i) throws NavException{
		if (i<=0)
			throw new IllegalArgumentException(" # of sibling can be less or equal to 0");
		// get starting char offset
		if(atTerminal==true)
			return -1L;
		// so is the char offset
		int so = getTokenOffset(getCurrentIndex())-1;
		// char offset to byte offset conversion
		if (encoding>=FORMAT_UTF_16BE)
			so = so<<1;
		BookMark bm = new BookMark(this);
		bm.recordCursorPosition();
		while(i>1 && toElement(VTDNav.NEXT_SIBLING)){
			i--;
		}
		long l= getElementFragment();
		int len = (int)l+(int)(l>>32)-so;
		if (i==1 && toElement(VTDNav.NEXT_SIBLING)){
		}else
			bm.setCursorPosition();
		return (((long)len)<<32)|so;
	}
	
	/**
     * Get the starting offset and length of an element encoded in a long, upper
     * 32 bits is length; lower 32 bits is offset Unit is in byte. Creation
     * date: (3/15/04 1:47:55 PM)
     */
	public long getElementFragment() throws NavException {
		// a little scanning is needed
		// has next sibling case
		// if not
		
		int depth = getCurrentDepth();
//		 document length and offset returned if depth == -1
		if (depth == -1){
		    int i=vtdBuffer.lower32At(0);
		    if (i==0)
		        return ((long)docLen)<<32| docOffset;
		    else
		        return ((long)(docLen-32))| 32;
		}
		int so = getTokenOffset(getCurrentIndex2()) - 1;
		int length = 0;
		

		// for an element with next sibling
		if (toElement(NEXT_SIBLING)) {
			
			int temp = getCurrentIndex();
			int temp2 = temp;
			//boolean b = false;
			// rewind
			while (getTokenDepth(temp) == depth && 
					(getTokenType(temp)==VTDNav.TOKEN_COMMENT || 
							getTokenType(temp)==VTDNav.TOKEN_PI_VAL ||
							getTokenType(temp)==VTDNav.TOKEN_PI_NAME)) {
				
				temp--;
			}
			/*if(temp!=temp2)
				temp++;*/
			//temp++;
			int so2 = getTokenOffset(temp) - 1;
			// look for the first '>'
			while (getCharUnit(so2) != '>') {
				so2--;
			}
			length = so2 - so + 1;
			toElement(PREV_SIBLING);
			if (encoding <= FORMAT_WIN_1258)
				return ((long) length) << 32 | so;
			else
				return ((long) length) << 33 | (so << 1);
		}

		// for root element
		if (depth == 0) {
			int temp = vtdBuffer.size - 1;
			boolean b = false;
			int so2 = 0;
			while (getTokenDepth(temp) == -1) {
				temp--; // backward scan
				b = true;
			}
			if (b == false)
				so2 =
					(encoding <= FORMAT_WIN_1258 )
						? (docOffset + docLen - 1)
						: ((docOffset + docLen) >> 1) - 1;
			else
				so2 = getTokenOffset(temp + 1);
			while (getCharUnit(so2) != '>') {
				so2--;
			}
			length = so2 - so + 1;
			if (encoding <= FORMAT_WIN_1258)
				return ((long) length) << 32 | so;
			else
				return ((long) length) << 33 | (so << 1);
		}
		// for a non-root element with no next sibling
		int temp = getCurrentIndex() + 1;
		int size = vtdBuffer.size;
		// temp is not the last entry in VTD buffer
		if (temp < size) {
			while (temp < size && getTokenDepth(temp) >= depth) {
				temp++;
			}
			if (temp != size) {
				int d =
					depth
						- getTokenDepth(temp)
						+ ((getTokenType(temp) == TOKEN_STARTING_TAG) ? 1 : 0);
				int so2 = getTokenOffset(temp) - 1;
				int i = 0;
				// scan backward
				while (i < d) {
					if (getCharUnit(so2) == '>')
						i++;
					so2--;
				}
				length = so2 - so + 2;
				if (encoding <= FORMAT_WIN_1258)
					return ((long) length) << 32 | so;
				else
					return ((long) length) << 33 | (so << 1);
			}
			/*
             * int so2 = getTokenOffset(temp - 1) - 1; int d = depth -
             * getTokenDepth(temp - 1); int i = 0; while (i < d) { if
             * (getCharUnit(so2) == '>') { i++; } so2--; } length = so2 - so +
             * 2; if (encoding < 3) return ((long) length) < < 32 | so; else
             * return ((long) length) < < 33 | (so < < 1);
             */
		}
		// temp is the last entry
		// scan forward search for /> or </cc>
		
		int so2 =
			(encoding <= FORMAT_WIN_1258)
				? (docOffset + docLen - 1)
				: ((docOffset + docLen) >> 1) - 1;
		int d;
	   
	    d = depth + 1;
	    
	    int i = 0;
        while (i < d) {
            if (getCharUnit(so2) == '>') {
                i++;
            }
            so2--;
        }
	  

		length = so2 - so + 2;

		if (encoding <= FORMAT_WIN_1258)
			return ((long) length) << 32 | so;
		else
			return ((long) length) << 33 | (so << 1);
	}
	/**
     * Get the encoding of the XML document.
     * 
     * @return int
     */
	final public int getEncoding() {
		return encoding;
	}
	/**
     * Get the maximum nesting depth of the XML document (>0). max depth is
     * nestingLevel -1
     * 
     * @return int
     */
	final public int getNestingLevel() {
		return nestingLevel;
	}
	
	/**
	 * Return the charater (not byte) offset after head (the ending bracket of the starting tag, 
	 * not including an empty element, in which case 0xffffffff 00000000 | len  is returned)
	 * @return
	 *
	 */
	final public long getOffsetAfterHead(){
	    
	    int i = getCurrentIndex();
	    if (getTokenType(i)!=VTDNav.TOKEN_STARTING_TAG){
	        return -1;
	    }
	    int j=i+1;
	    while (j<vtdSize && (getTokenType(j)==VTDNav.TOKEN_ATTR_NAME 
	            || getTokenType(j)==VTDNav.TOKEN_ATTR_NS)){
	        j += 2;
	    }
	    
	    int offset; // this is character offset
	    if (i+1==j)
	    {
	        offset = getTokenOffset(i)+ ((ns==false)?getTokenLength(i):(getTokenLength(i)&0xff));	                   
	    }else {
	        offset = getTokenOffset(j-1)+((ns==false)?getTokenLength(j-1):(getTokenLength(j-1)&0xff))+1;	                    
	    }
	    
	    while(getCharUnit(offset)!='>'){
	        offset++;	        
	    }
	    
	    if (getCharUnit(offset-1)=='/')
	        return 0xffffffff00000000L|(offset);
	    else
	        return offset+1;
	}
	
	final protected long getOffsetBeforeTail() throws NavException{
		long l = getElementFragment();
		
		int offset = (int)l;
		int len = (int)(l>>32);
		// byte to char conversion
		if (this.encoding >= VTDNav.FORMAT_UTF_16BE){
			offset <<= 1;
			len <<=1;
		}
		offset += len;
		if (getCharUnit(offset-2)=='/')
			return 0xffffffff00000000L | offset-1;
		else{
			offset -= 2;
			while(getCharUnit(offset)!='/'){
		        offset--;	        
		    }
			return offset -1;
		}
		//return 1;
	}
	/**
     * Get root index value , which is the index val of root element
     * 
     * @return int
     */
	final public int getRootIndex() {
		return rootIndex;
	}
	/**
     * This method returns of the token index of the type character data or
     * CDATA. Notice that it is intended to support data orient XML (not
     * mixed-content XML). return the index of the text token, or -1 if none
     * exists.
     * 
     * @return int
     */
	public int getText() {
		if (context[0]==-1) return -1;
		int index = (context[0] != 0) ? context[context[0]] + 1 : rootIndex + 1;
		int depth = getCurrentDepth();
		int type; 
		if (index<vtdSize || !atTerminal)
			type = getTokenType(index);
		else 
			return -1;

		while (true) {
			if (type == TOKEN_CHARACTER_DATA || type == TOKEN_CDATA_VAL) {
				if (depth == getTokenDepth(index))
					return index;
				else
					return -1;
			} else if (type == TOKEN_ATTR_NS || type == TOKEN_ATTR_NAME) {
				index += 2; // assuming a single token for attr val
			} else if (
				type == TOKEN_PI_NAME
					|| type == TOKEN_PI_VAL
					|| type == TOKEN_COMMENT) {
				if (depth == getTokenDepth(index)) {
					index += 1;
				} else
					return -1;
			} else
				return -1;
			if (index >= vtdSize)
				break;
			type = getTokenType(index);
		}
		return -1;
	}
	/**
     * Get total number of VTD tokens for the current XML document.
     * 
     * @return int
     */
	final public int getTokenCount() {
		return vtdSize;
	}
	/**
     * Get the depth value of a token (>=0).
     * 
     * @return int
     * @param index
     *            int
     */
	final public int getTokenDepth(int index) {
		int i = (int) ((vtdBuffer.longAt(index) & MASK_TOKEN_DEPTH) >> 52);
		if (i != 255)
			return i;
		return -1;
	}
	
	final protected int getTokenLength2(int index){
		return (int)
		((vtdBuffer.longAt(index) & MASK_TOKEN_FULL_LEN) >> 32);
	}
	/**
     * Get the token length at the given index value please refer to VTD spec
     * for more details Length is in terms of the UTF char unit For prefixed
     * tokens, it is the qualified name length. When ns is not enabled, return
     * the full name length for attribute name and element name When ns is
     * enabled, return an int with upper 16 bit for prefix length, lower 16 bit
     * for qname length
     * 
     * @return int
     * @param index
     *            int
     */
	public int getTokenLength(int index) {
		int type = getTokenType(index);
		int depth;
		int len = 0;
		long l;
		int temp=0;
		switch (type) {
			case TOKEN_ATTR_NAME :
			case TOKEN_ATTR_NS :
			case TOKEN_STARTING_TAG :
				l = vtdBuffer.longAt(index);
				return (ns == false)
					? (int) ((l & MASK_TOKEN_QN_LEN) >> 32)
					: ((int) ((l & MASK_TOKEN_QN_LEN)
						>> 32)
						| ((int) ((l & MASK_TOKEN_PRE_LEN)
							>> 32)
							<< 5));
			case TOKEN_CHARACTER_DATA:
			case TOKEN_CDATA_VAL:
			case TOKEN_COMMENT: // make sure this is total length
				depth = getTokenDepth(index);
				do{
					len = len +  (int)
					((vtdBuffer.longAt(index)& MASK_TOKEN_FULL_LEN) >> 32);
					temp =  getTokenOffset(index)+(int)
					((vtdBuffer.longAt(index)& MASK_TOKEN_FULL_LEN) >> 32);
					index++;		
					}
				while(index < vtdSize && depth == getTokenDepth(index) 
						&& type == getTokenType(index) && temp == getTokenOffset(index));
				//if (int k=0)
				return len;
			default :
				return (int)
					((vtdBuffer.longAt(index) & MASK_TOKEN_FULL_LEN) >> 32);
		}

	}
	/**
     * Get the starting offset (unit in native char) of the token at the given
     * index.
     * 
     * @return int
     * @param index
     *            int
     */
	final public int getTokenOffset(int index) {
		//return (context[0] != 0)
		//    ? (int) (vtdBuffer.longAt(context[context[0]]) & MASK_TOKEN_OFFSET)
		//    : (int) (vtdBuffer.longAt(rootIndex) & MASK_TOKEN_OFFSET);
		return (int) (vtdBuffer.longAt(index) & MASK_TOKEN_OFFSET);
	}

	/**
     * Get the XML document
     * 
     * @return IByteBuffer
     */
	final public IByteBuffer getXML() {
		return XMLDoc;
	}
	/**
     * Get the token type of the token at the given index value. Creation date:
     * (11/16/03 6:41:51 PM)
     * 
     * @return int
     * @param index
     *            int
     */
	final public int getTokenType(int index) {
		return (int) ((vtdBuffer.longAt(index) & MASK_TOKEN_TYPE) >> 60) & 0xf;
	}
	/**
     * Test whether current element has an attribute with the matching name. "*"
     * will match any attribute name, therefore is a test whether there is any
     * attribute at all if namespace is disabled, this function will not
     * distinguish between ns declaration and attribute otherwise, ns tokens are
     * invisible Creation date: (11/16/03 5:50:26 PM)
     * 
     * @return boolean (true if such an attribute exists)
     * @param an
     *            java.lang.String
     * @exception com.ximpleware.NavException
     *                The exception if the underlying byte content contains
     *                various errors. Notice that we are being conservative in
     *                making little assumption on the correctness of underlying
     *                byte content. This is because the VTD etc can be generated
     *                by another machine from a load-balancer.
     * @exception IllegalArguementException
     *                if an is null
     */
	final public boolean hasAttr(String an) throws NavException {
	    return getAttrVal(an)!=-1;
	}
	/**
     * Test whether the current element has an attribute with matching namespace
     * URL and localname. If ns is false, return false immediately
     * 
     * @return boolean
     * @param URL
     *            java.lang.String (namespace URL)
     * @param ln
     *            java.lang.String (localname )
     * @exception com.ximpleware.NavException
     *                The exception if the underlying byte content contains
     *                various errors. Notice that we are being conservative in
     *                making little assumption on the correctness of underlying
     *                byte content. This is because the VTD can be generated by
     *                another machine such as a load-balancer.
     * @exception IllegalArguementException
     *                if ln is null
     */
	final public boolean hasAttrNS(String URL, String ln) throws NavException {
		return (getAttrValNS(URL, ln) != -1);
	}
	/**
     * Test the token type, to see if it is a starting tag.
     * 
     * @return boolean
     * @param index
     *            int
     */
	protected final boolean isElement(int index) {
		return (((vtdBuffer.longAt(index) & MASK_TOKEN_TYPE) >> 60) & 0xf)
			== TOKEN_STARTING_TAG;
	}
	
	/**
     * Test the token type, to see if it is a starting tag or document token
     * (introduced in 1.0).
     * 
     * @return boolean
     * @param index
     *            int
     */
	protected final boolean isElementOrDocument(int index){
		long i =(((vtdBuffer.longAt(index) & MASK_TOKEN_TYPE) >> 60) & 0xf);
		return (i==TOKEN_STARTING_TAG||i==TOKEN_DOCUMENT);
	}
	/**
     * Test whether ch is a white space character or not.
     * 
     * @return boolean
     * @param ch
     *            int
     */
	final private boolean isWS(int ch) {
		return (ch == ' ' || ch == '\n' || ch == '\t' || ch == '\r');
	}
	
	/**
     * This function is called by selectElement_P in autoPilot
     * 
     * @param en
     *            element Name
     * @param a
     *            context of current position
     * @param special
     *            whether the test type is node()
     * @return boolean
     * @throws NavException
     */
	protected boolean iterate_preceding(String en, int[] a, int endIndex)
	throws NavException {
		int index = getCurrentIndex() +1;
		int tokenType;
		//int depth = getTokenDepth(index);
		//int size = vtdBuffer.size;
		while (index< endIndex) {
			tokenType = getTokenType(index);
			switch(tokenType){
			case TOKEN_ATTR_NAME:
			case TOKEN_ATTR_NS:
				index = index + 2;
				continue;
			case TOKEN_STARTING_TAG:
			//case TOKEN_DOCUMENT:
				int depth = getTokenDepth(index);
				if (index!=a[depth]){
					if (en.equals("*")||matchRawTokenString(index,en)){
						context[0] = depth;
						if (depth > 0)
							context[depth] = index;
						if (depth < maxLCDepthPlusOne)
							resolveLC();
						atTerminal = false;
						return true;
					}else{
						context[depth] = index;
						index++;
						continue;
					}
				}else{
					context[depth] = index;
					index++;
					continue;
				}
			case TOKEN_CHARACTER_DATA:
			case TOKEN_CDATA_VAL:
			case TOKEN_COMMENT: 
					index++; 
					continue;
			case TOKEN_PI_NAME:
				index+=2;
				continue;
			}
			index++;
		}
		return false;	
	}
	
	/**
     * This function is called by selectElementNS_P in autoPilot
     * 
     * @param URL
     * @param ln
     * @return boolean
     * @throws NavException
     */
	protected boolean iterate_precedingNS(String URL, String ln, int[] a,int endIndex )
	throws NavException {
		int index = getCurrentIndex() - 1;
		int tokenType;
		//int depth = getTokenDepth(index);
		//int size = vtdBuffer.size;
		while (index< endIndex) {
			tokenType = getTokenType(index);
			switch(tokenType){
			case TOKEN_ATTR_NAME:
			case TOKEN_ATTR_NS:
				index = index + 2;
				continue;
			case TOKEN_STARTING_TAG:
			//case TOKEN_DOCUMENT:
				int depth = getTokenDepth(index);
				if (index!=a[depth]){
					context[0] = depth;
					if (depth > 0)
						context[depth] = index;
					if (matchElementNS(ln,URL)){						
						if (depth < maxLCDepthPlusOne)
							resolveLC();
						atTerminal = false;
						return true;
					}else{
						context[depth] = index;
						index++;
						continue;
					}
				}else{
					context[depth] = index;
					index++;
					continue;
				}
			case TOKEN_CHARACTER_DATA:
			case TOKEN_CDATA_VAL:
			case TOKEN_COMMENT: 
					index++; 
					continue;
			case TOKEN_PI_NAME:
				index+=2;
				continue;
			}
			index++;
		}
		return false;
	}
	/**
     * This function is called by selectElement_F in autoPilot
     * 
     * @param en
     *            ElementName
     * @param special
     *            whether it is a node()
     * @return boolean
     * @throws NavException
     */

	protected boolean iterate_following(String en, boolean special) 
	throws NavException{
		int index = getCurrentIndex() + 1;
		int tokenType;
		//int size = vtdBuffer.size;
		while (index < vtdSize) {
			tokenType = getTokenType(index);
			if (tokenType == VTDNav.TOKEN_ATTR_NAME
					|| tokenType == VTDNav.TOKEN_ATTR_NS
					|| tokenType == VTDNav.TOKEN_PI_NAME) {
				index = index + 2;
				continue;
			}
			// if (isElementOrDocument(index)) {
			if (tokenType == VTDNav.TOKEN_STARTING_TAG
					|| tokenType == VTDNav.TOKEN_DOCUMENT) {
				int depth = getTokenDepth(index);
				context[0] = depth;
				if (depth > 0)
					context[depth] = index;
				if (special || matchElement(en)) {
					if (depth < maxLCDepthPlusOne)
						resolveLC();
					return true;
				}
			}
			index++;
		}
		return false;		
	}
	
	
	/**
     * This function is called by selectElementNS_F in autoPilot
     * 
     * @param URL
     * @param ln
     * @return boolean
     * @throws NavException
     */
	protected boolean iterate_followingNS(String URL, String ln) 
	throws NavException{
		int index = getCurrentIndex() + 1;
		int tokenType;
		//int size = vtdBuffer.size;
		while (index < vtdSize) {
			tokenType = getTokenType(index);
			if (tokenType == VTDNav.TOKEN_ATTR_NAME
					|| tokenType == VTDNav.TOKEN_ATTR_NS
					|| tokenType == VTDNav.TOKEN_PI_NAME) {
				index = index + 2;
				continue;
			}
			// if (isElementOrDocument(index)) {
			if (tokenType == VTDNav.TOKEN_STARTING_TAG
					|| tokenType == VTDNav.TOKEN_DOCUMENT) {
				int depth = getTokenDepth(index);
				context[0] = depth;
				if (depth > 0)
					context[depth] = index;
				if (matchElementNS(URL, ln)) {
					if (depth < maxLCDepthPlusOne)
						resolveLC();
					return true;
				}
			}
			index++;
		}
		return false;
	}
	/**
     * This method is similar to getElementByName in DOM except it doesn't
     * return the nodeset, instead it iterates over those nodes. Notice that
     * this method is called by the "iterate" method in the Autopilot class. "*"
     * will match any element Creation date: (12/2/03 2:31:20 PM)
     * 
     * @return boolean
     * @param dp
     *            int (The depth of the starting position before iterating)
     * @param en
     *            java.lang.String
     * @exception com.ximpleware.NavException
     *                The exception is signaled if the underlying byte content
     *                contains various errors. Notice that we are being
     *                conservative in making little assumption on the
     *                correctness of underlying byte content. This is because
     *                VTD records can be generated by another machine from a
     *                load-balancer. null element name allowed represent
     *                node()in XPath;
     */
	protected boolean iterate(int dp, String en, boolean special)
		throws NavException { // the navigation doesn't rely on LC
		// get the current depth
		int index = getCurrentIndex() + 1;
		int tokenType;
		//int size = vtdBuffer.size;
		while (index < vtdSize) {
		    tokenType = getTokenType(index);
			if (tokenType==VTDNav.TOKEN_ATTR_NAME
			        || tokenType == VTDNav.TOKEN_ATTR_NS || tokenType ==VTDNav.TOKEN_PI_NAME){			  
			    index = index+2;
			    continue;
			}
			//if (isElementOrDocument(index)) {
			if (tokenType == VTDNav.TOKEN_STARTING_TAG || tokenType == VTDNav.TOKEN_DOCUMENT){
				int depth = getTokenDepth(index);
				if (depth > dp) {
					context[0] = depth;
					if (depth>0)
						context[depth] = index;
					if (special || matchElement(en)) {
						if (dp< maxLCDepthPlusOne)
						resolveLC();
						return true;
					}
				} else {
					return false;
				}
			}
			index++;

		}
		return false;
	}
	
	//preceding::node()
	protected boolean iterate_preceding_node(int[] a, int endIndex)
	throws NavException {
		int index = getCurrentIndex()+1;
		int tokenType;
		//int t,d;
		//int depth = getTokenDepth(index);
		//int size = vtdBuffer.size;
		while (index <  endIndex) {
			tokenType = getTokenType(index);
			switch(tokenType){
			case TOKEN_ATTR_NAME:
			case TOKEN_ATTR_NS:
				index = index + 2;
				continue;
			case TOKEN_STARTING_TAG:
			//case TOKEN_DOCUMENT:
				int depth = getTokenDepth(index);
				if (depth>0 && (index!=a[depth])){
					context[0] = depth;
					if (depth > 0)
						context[depth] = index;
					if (depth < maxLCDepthPlusOne)
						resolveLC();
					atTerminal = false;
					return true;	
				}else{
					if (depth > 0)
						context[depth] = index;
					if (depth < maxLCDepthPlusOne)
						resolveLC();
					index++;
					atTerminal = false;
					continue;
				}
			
			case TOKEN_CHARACTER_DATA:
			case TOKEN_CDATA_VAL:
			case TOKEN_COMMENT:
			case TOKEN_PI_NAME:
				depth = getTokenDepth(index);
				
				context[0]=depth;
				LN = index;
				atTerminal = true;
				sync(depth,index);
				return true;
			}
			index++;
		}
		return false;	
	}
	
	protected boolean iterate_following_node()
	throws NavException {
		int index = getCurrentIndex() + 1;
		int tokenType;
		//int size = vtdBuffer.size;
		while (index < vtdSize) {
			tokenType = getTokenType(index);
			switch(tokenType){
			case TOKEN_ATTR_NAME:
			case TOKEN_ATTR_NS:
				index = index + 2;
				continue;
			case TOKEN_STARTING_TAG:
			case TOKEN_DOCUMENT:
				int depth = getTokenDepth(index);
				context[0] = depth;
				if (depth > 0)
					context[depth] = index;
				if (depth < maxLCDepthPlusOne)
					resolveLC();
				atTerminal = false;
				return true;	
				
			case TOKEN_CHARACTER_DATA:
			case TOKEN_CDATA_VAL:
			case TOKEN_COMMENT:
			case TOKEN_PI_NAME:
				depth = getTokenDepth(index);
				
				context[0]=depth;
				LN = index;
				atTerminal = true;
				sync(depth,index);
				return true;
			}
			index++;
		}
		return false;		
	}
	/**
	 * 
	 */
	protected boolean iterateNode(int dp)
			throws NavException { // the navigation doesn't rely on LC
		// get the current depth
		
		int index = getCurrentIndex() + 1;
		int tokenType,depth;
		// int size = vtdBuffer.size;
		while (index < vtdSize) {
			tokenType = getTokenType(index);
			switch(tokenType){
			case TOKEN_ATTR_NAME:
			case TOKEN_ATTR_NS:
				index = index + 2;
				continue;
			case TOKEN_STARTING_TAG:
			case TOKEN_DOCUMENT:
				depth = getTokenDepth(index);
				atTerminal = false;
				if (depth > dp) {
					context[0] = depth;
					if (depth > 0)
						context[depth] = index;
					if (dp < maxLCDepthPlusOne)
						resolveLC();
					return true;					
				} else {
					return false;
				}
			case TOKEN_CHARACTER_DATA:
			case TOKEN_COMMENT:
			case TOKEN_PI_NAME:
			case TOKEN_CDATA_VAL:
				depth = getTokenDepth(index);
				
				if (depth >= dp){
					sync(depth,index);
					LN= index;
					context[0]= depth;
					atTerminal = true;
					return true;
				}
				return false;
			default:
				index ++;
			}			
		}
		return false;
	}
	

	/**
     * This method is similar to getElementByName in DOM except it doesn't
     * return the nodeset, instead it iterates over those nodes . When URL is
     * "*" it will match any namespace if ns is false, return false immediately
     * 
     * @return boolean
     * @param dp
     *            int (The depth of the starting position before iterating)
     * @param URL
     *            java.lang.String
     * @param ln
     *            java.lang.String
     * @exception com.ximpleware.NavException
     *                The exception if the underlying byte content contains
     *                various errors. Notice that we are being conservative in
     *                making little assumption on the correctness of underlying
     *                byte content. This is because VTD records can be generated
     *                by another machine from a load-balancer..
     * @exception IllegalArguementException
     *                if ln is null example
     * 
     * int depth = nv.getCurrentDepth() while(iterateNS(depth,
     * "www.url.com","node_name")){ push(); // store the current position //move
     * position safely pop(); // load the position }
     */
	protected boolean iterateNS(int dp, String URL, String ln)
		throws NavException {
		if (ns == false)
			return false;
		int tokenType;
		int index = getCurrentIndex() + 1;
		while (index < vtdSize) {
		    tokenType = getTokenType(index);
			if(tokenType==VTDNav.TOKEN_ATTR_NAME
			        || tokenType == VTDNav.TOKEN_ATTR_NS || tokenType ==VTDNav.TOKEN_PI_NAME){
			    index = index+2;
			    continue;
			}
			//if (isElementOrDocument(index)) {
			if (tokenType == VTDNav.TOKEN_STARTING_TAG || tokenType == VTDNav.TOKEN_DOCUMENT){
				int depth = getTokenDepth(index);
				if (depth > dp) {
					context[0] = depth;
					if (depth>0)
						context[depth] = index;
					if (matchElementNS(URL, ln)) {
						if (dp < maxLCDepthPlusOne)
							resolveLC();
						return true;
					}
				} else {
					return false;
				}
			}
			index++;
		}
		return false;
	}
	

	/**
     * Test if the current element matches the given name. Creation date:
     * (11/26/03 2:09:43 PM)
     * 
     * @return boolean
     * @param en
     *            java.lang.String
     * @exception com.ximpleware.NavException
     *                If the underlying raw char representation has errors.
     */
	final public boolean matchElement(String en) throws NavException {
		
		if (en.equals("*") && context[0]!=-1)
			return true;
		if (context[0]==-1)
			return false;
		return matchRawTokenString(
			(context[0] == 0) ? rootIndex : context[context[0]],
			en);
	}
	/**
     * Test whether the current element matches the given namespace URL and
     * localname. URL, when set to "*", matches any namespace (including null),
     * when set to null, defines a "always-no-match" ln is the localname that,
     * when set to *, matches any localname
     * 
     * @return boolean
     * @param URL
     *            java.lang.String
     * @param ln
     *            java.lang.String
     * @exception com.ximpleware.NavException
     *                When there is any encoding conversion error or unknown
     *                entity.
     * 
     */
    final public boolean matchElementNS(String URL, String ln) throws NavException {
    	if (context[0]==-1)
    		return false;
    	int i =
    		getTokenLength((context[0] != 0) ? context[context[0]] : rootIndex);
    	int offset =
    		getTokenOffset((context[0] != 0) ? context[context[0]] : rootIndex);
    	int preLen = (i >> 16) & 0xffff;
    	int fullLen = i & 0xffff;
        if (URL!=null && URL.length()==0)
        	URL = null;
    	if (ln.equals("*")
    		|| ((preLen != 0)
    			? matchRawTokenString(
    				offset + preLen + 1,
    				fullLen - preLen - 1,
    				ln)
    			: matchRawTokenString(
    				offset,
    				fullLen,
    				ln))) { // no prefix, search for xmlns
    		if (((URL != null) ? URL.equals("*") : false)
    			|| (resolveNS(URL, offset, preLen) == true))
    			return true;
    		if ( preLen==3 
    			&& matchRawTokenString(offset, preLen,"xml")
    			&& URL.equals("http://www.w3.org/XML/1998/namespace"))
    			return true;    			
    	}
    	return false;
    }
    final private boolean matchRawTokenString(int offset, int len, String s)
    throws NavException{
        return compareRawTokenString(offset, len, s)==0;
    }
    
	final protected int compareTokenString(int offset, int len, String s)
            throws NavException {
        int i, l;
        long l1;
        //this.currentOffset = offset;
        int endOffset = offset + len;

        //       System.out.print("currentOffset :" + currentOffset);
        l = s.length();
        //System.out.println(s);
        for (i = 0; i < l && offset < endOffset; i++) {
            l1 = getCharResolved(offset);
            int i1 = s.charAt(i);
            if (i1 < (int) l1)
                return 1;
            if (i1 > (int) l1)
                return -1;
            offset += (int) (l1 >> 32);
        }

        if (i == l && offset < endOffset)
            return 1;
        if (i < l && offset == endOffset)
            return -1;
        return 0;
    }

	/**
     * <em>New in 2.0</em> This method compares two VTD tokens of VTDNav
     * objects The behavior of this method is like compare the strings
     * corresponds to i1 and i2, meaning for text or attribute val, entities
     * will be converted into the corresponding char, return 0 if two tokens
     * are the identical when converted to Unicode String using toString() 
     * respectively
     * 
     * @param i1
     * @param vn2
     * @param i2
     * @return -1,0 (when equal), or 1
     * @throws NavException
     *  
     */
	final public int compareTokens(int i1, VTDNav vn2, int i2) 
	throws NavException{
	    int t1, t2;
	    int ch1, ch2;
	    int endOffset1, endOffset2;
	    long l;

		if ( i1 ==i2 && this == vn2)
			return 0;
		
		t1 = this.getTokenType(i1);
		t2 = vn2.getTokenType(i2);
		
		int offset1 = this.getTokenOffset(i1);
		int offset2 = vn2.getTokenOffset(i2);
		
		int len1 =
			(t1 == TOKEN_STARTING_TAG
				|| t1 == TOKEN_ATTR_NAME
				|| t1 == TOKEN_ATTR_NS)
				? getTokenLength(i1) & 0xffff
				: getTokenLength(i1);
		int len2 = 
		    (t2 == TOKEN_STARTING_TAG
				|| t2 == TOKEN_ATTR_NAME
				|| t2 == TOKEN_ATTR_NS)
				? vn2.getTokenLength(i2) & 0xffff
				: vn2.getTokenLength(i2);
		
		endOffset1 = len1+offset1;
		endOffset2 = len2+ offset2;

		for(;offset1<endOffset1&& offset2< endOffset2;){
		    if(t1 == VTDNav.TOKEN_CHARACTER_DATA
		            || t1== VTDNav.TOKEN_ATTR_VAL){
		        l = this.getCharResolved(offset1);
		    } else {
		        l = this.getChar(offset1);
		    }
	        ch1 = (int)l;
	        offset1 += (int)(l>>32);
		    
		    if(t2 == VTDNav.TOKEN_CHARACTER_DATA
		            || t2== VTDNav.TOKEN_ATTR_VAL){
		        l = vn2.getCharResolved(offset2);
		    } else {
		        l = vn2.getChar(offset2);
		    }
	        ch2 = (int)l;
	        offset2 += (int)(l>>32);
	        
		    if (ch1 > ch2)
		        return 1;
		    if (ch1 < ch2)
		        return -1;
		}
		
		if (offset1 == endOffset1 
		        && offset2 < endOffset2)
			return -1;
		else if (offset1 < endOffset1 
		        && offset2 == endOffset2)
		    return 1;
		else
			return 0;
	}
	/**
     * Lexicographically compare a string against a token with given offset and
     * len, entities doesn't get resolved.
     * 
     * @return int (0 if they are equal, 1 if greater, else -1)
     * 
     * @param offset
     *            int
     * @param len
     *            int
     * @param s
     *            java.lang.String
     * @exception com.ximpleware.NavException
     *                The exception if the underlying byte content contains
     *                various errors. Notice that we are being conservative in
     *                making little assumption on the correctness of underlying
     *                byte content. This is because the VTD can be generated by
     *                another machine such as a load-balancer.
     */
	final protected int compareRawTokenString(int offset, int len, String s)
		throws NavException {
		int i, l;
		long l1;
		//this.currentOffset = offset;
		int endOffset = offset + len;

			
		//       System.out.print("currentOffset :" + currentOffset);
        l = s.length();
        //System.out.println(s);
        for (i = 0; i < l && offset < endOffset; i++) {
            l1 = getChar(offset);
            int i1 = s.charAt(i); 
            if (i1 < (int) l1)                 
                return 1;
            if (i1 > (int) l1)
                return -1;
            offset += (int) (l1 >> 32);
        }
		
		if (i == l && offset < endOffset)
			return 1;
		if (i<l && offset == endOffset)
		    return -1;
		return 0;		
	}
	/**
     * <em>New in 2.0</em> Compare the string against the token at the given
     * index value. When a token is an attribute name or starting tag, qualified
     * name is what gets compared against This method has to take care of the
     * underlying encoding conversion but it <em> doesn't </em> resolve entity
     * reference in the underlying document The behavior is the same as calling
     * toRawString on index, then compare to s
     * 
     * @param index
     * @param s
     * @return the result of lexical comparison
     * @exception NavException
     *  
     */
	final public int compareRawTokenString(int index, String s)
	throws NavException {
		int type = getTokenType(index);
		int len =
			(type == TOKEN_STARTING_TAG
				|| type == TOKEN_ATTR_NAME
				|| type == TOKEN_ATTR_NS)
				? getTokenLength(index) & 0xffff
				: getTokenLength(index);
		// upper 16 bit is zero or for prefix

		//currentOffset = getTokenOffset(index);
		// point currentOffset to the beginning of the token
		// for UTF 8 and ISO, the performance is a little better by avoid
        // calling getChar() everytime
		return compareRawTokenString(getTokenOffset(index), len, s);
	}
	/**
     * Match the string against the token at the given index value. When a token
     * is an attribute name or starting tag, qualified name is what gets matched
     * against This method has to take care of the underlying encoding
     * conversion but it <em> doesn't </em> resolve entity reference in the
     * underlying document
     * 
     * @return boolean
     * @param index
     *            int (index into the VTD token buffer)
     * @param s
     *            java.lang.String
     * @exception com.ximpleware.NavException
     *                When if the underlying byte content contains various
     *                errors. Notice that we are being conservative in making
     *                little assumption on the correctness of underlying byte
     *                content. This is because the VTD can be generated by
     *                another machine such as a load-balancer.
     */
	final public boolean matchRawTokenString(int index, String s)
		throws NavException {
		int type = getTokenType(index);
		int len =
			(type == TOKEN_STARTING_TAG
				|| type == TOKEN_ATTR_NAME
				|| type == TOKEN_ATTR_NS)
				? getTokenLength(index) & 0xffff
				: getTokenLength(index);
		// upper 16 bit is zero or for prefix

		//currentOffset = getTokenOffset(index);
		// point currentOffset to the beginning of the token
		// for UTF 8 and ISO, the performance is a little better by avoid
        // calling getChar() everytime
		return compareRawTokenString(getTokenOffset(index), len, s)==0;
	}
	/**
     * Match a string with a token represented by a long (upper 32 len, lower 32
     * offset).
     * 
     * @return boolean
     * @param l
     *            long
     * @param s
     *            java.lang.String
     * @exception com.ximpleware.NavException
     *                When if the underlying byte content contains various
     *                errors. Notice that we are being conservative in making
     *                little assumption on the correctness of underlying byte
     *                content. This is because the VTD can be generated by
     *                another machine such as a load-balancer.
     *  
     */
	/*final private boolean matchRawTokenString(long l, String s) throws NavException {
		int len = (int) ((l & MASK_TOKEN_FULL_LEN) >> 32);
		// a little hardcode is always bad
		//currentOffset = (int) l;
		return compareRawTokenString((int)l, len, s)==0;
	}*/
	/**
     * Match a string against a token with given offset and len, entities get
     * resolved properly. Creation date: (11/24/03 1:37:42 PM)
     * 
     * @return boolean
     * @param offset
     *            int
     * @param len
     *            int
     * @param s
     *            java.lang.String
     * @exception com.ximpleware.NavException
     *                The exception if the underlying byte content contains
     *                various errors. Notice that we are being conservative in
     *                making little assumption on the correctness of underlying
     *                byte content. This is because the VTD can be generated by
     *                another machine such as a load-balancer.
     * @exception IllegalArguementException
     *                if s is null
     */
	/*final private boolean matchTokenString(int offset, int len, String s)
		throws NavException {
	    return compareTokenString(offset,len,s)==0;
	}*/
	
	/**
     * <em>New in 2.0</em> Compare the string against the token at the given
     * index value. When a token is an attribute name or starting tag, qualified
     * name is what gets matched against This method has to take care of the
     * underlying encoding conversion as well as entity reference comparison
     * 
     * @param index
     * @param s
     * @return int
     * @throws NavException
     *  
     */
	final public int compareTokenString(int index, String s)
	throws NavException{
		int type = getTokenType(index);
		int len =
			(type == TOKEN_STARTING_TAG
				|| type == TOKEN_ATTR_NAME
				|| type == TOKEN_ATTR_NS)
				? getTokenLength(index) & 0xffff
				: getTokenLength(index);
		// upper 16 bit is zero or for prefix

		//currentOffset = getTokenOffset(index);
		// point currentOffset to the beginning of the token
		// for UTF 8 and ISO, the performance is a little better by avoid
        // calling getChar() everytime
				
		if (type == TOKEN_CHARACTER_DATA
				|| type == TOKEN_ATTR_VAL)		
			return compareTokenString(getTokenOffset(index), len, s);
		else
			return compareRawTokenString(getTokenOffset(index), len, s);
					
	}
	/**
     * Match the string against the token at the given index value. When a token
     * is an attribute name or starting tag, qualified name is what gets matched
     * against This method has to take care of the underlying encoding
     * conversion as well as entity reference comparison
     * 
     * @return boolean
     * @param index
     *            int
     * @param s
     *            java.lang.String
     * @exception com.ximpleware.NavException
     *                When if the underlying byte content contains various
     *                errors. Notice that we are being conservative in making
     *                little assumption on the correctness of underlying byte
     *                content. This is because the VTD can be generated by
     *                another machine such as a load-balancer.
     */
	final public boolean matchTokenString(int index, String s) throws NavException {
		int type = getTokenType(index);
		int len =
			(type == TOKEN_STARTING_TAG
				|| type == TOKEN_ATTR_NAME
				|| type == TOKEN_ATTR_NS)
				? getTokenLength(index) & 0xffff
				: getTokenLength(index);
		// upper 16 bit is zero or for prefix

		//currentOffset = getTokenOffset(index);
		// point currentOffset to the beginning of the token
		// for UTF 8 and ISO, the performance is a little better by avoid
        // calling getChar() everytime
		return compareTokenString(getTokenOffset(index), len, s)==0;
	}
	
	/**
	 * Match the string against the token at the given index value. The token will be
     * interpreted as if it is normalized (i.e. all white space char (\r\n\a ) is replaced
     * by a white space, char entities and entity references will be replaced by their correspondin
     * char see xml 1.0 spec interpretation of attribute value normalization) 
	 * @param index
	 * @param s
	 * @return
	 * @throws NavException
	 */
	final protected boolean matchNormalizedTokenString2(int index, String s) throws NavException{
		int type = getTokenType(index);
		int len =
			(type == TOKEN_STARTING_TAG
				|| type == TOKEN_ATTR_NAME
				|| type == TOKEN_ATTR_NS)
				? getTokenLength(index) & 0xffff
				: getTokenLength(index);

		return compareNormalizedTokenString2(getTokenOffset(index), len, s)==0;
	
	}
	
	final protected int compareNormalizedTokenString2(int offset, int len,
			String s) throws NavException {
		int i, l, temp;
		long l1,l2;
		//boolean b = false;
		// this.currentOffset = offset;
		int endOffset = offset + len;

		// System.out.print("currentOffset :" + currentOffset);
		l = s.length();
		// System.out.println(s);
		for (i = 0; i < l && offset < endOffset;) {
			l1 = getCharResolved(offset);
			temp = (int) l1;
			l2 = (l1>>32);
			if (l2<=2 && isWS(temp))
				temp = ' ';
			int i1 = s.charAt(i);
			if (i1 < temp)
				return 1;
			if (i1 > temp)
				return -1;
			i++;			
			offset += (int) (l1 >> 32);
		}

		if (i == l && offset < endOffset)
			return 1;
		if (i < l && offset == endOffset)
			return -1;
		return 0;
		// return -1;
	}
	/**
     * Match a string against a "non-extractive" token represented by a long
     * (upper 32 len, lower 32 offset).
     * 
     * @return boolean
     * @param l
     *            long
     * @param s
     *            java.lang.String
     * @exception com.ximpleware.NavException
     *                When the underlying byte content contains various errors.
     *                Notice that we are being conservative in making little
     *                assumption on the correctness of underlying byte content.
     *                This is because the VTD can be generated by another
     *                machine such as a load-balancer.
     *  
     */

	/*final private boolean matchTokenString(long l, String s) throws NavException {
		int len = (int) (l >> 32) & 0xffff;
		//currentOffset = (int) l;
		return compareTokenString((int) l, len, s)==0;
	}*/


	/**
     * Evaluate the namespace indicator in bit 31 and bit 30. Creation date:
     * (11/27/03 5:38:51 PM)
     * 
     * @return int
     * @param i
     *            int
     */
	final private int NSval(int i) {

		return (int) (vtdBuffer.longAt(i) & MASK_TOKEN_NS_MARK);
	}
	
	/**
     * overWrite is introduced in version 2.0 that allows you to directly
     * overwrite the XML content if the token is long enough If the operation is
     * successful, the new content along with whitespaces will fill the
     * available token space, and there will be no need to regenerate the VTD
     * and LCs !!!
     * <em> The current version (2.0) only allows overwrites on attribute value,
	 * character data, and CDATA</em>
     * 
     * Consider the XML below: <a>good </a> After overwriting the token "good"
     * with "bad," the new XML looks like: <a>bad </a> as you can see, "goo" is
     * replaced with "bad" character-by-character, and the remaining "d" is
     * replace with a white space
     * 
     * @param index
     * @param ba
     *            the byte array contains the new content to be overwritten
     * @return boolean as the status of the overwrite operation
     *  
     */
	final public boolean overWrite(int index, byte[] ba){
	    return overWrite(index,ba,0,ba.length);
	}
	
	
	/**
     * overWrite is introduced in version 2.0 that allows you to directly
     * overwrite the XML content if the token is long enough If the operation is
     * successful, white spaces will be used to fill the available token space,
     * and there will be no need to regenerate the VTD and LCs
     * <em> The current version (2.0) only allows overwrites on attribute value,
	 * character data, and CDATA</em>
     * 
     * Consider the XML below: <a>good </a> After overwriting the token "good"
     * with "bad," the new XML looks like: <a>bad </a> as you can see, "goo" is
     * replaced with "bad", and the remaining "d" is replace with a white space
     * 
     * @param index
     *            the VTD record to which the change will be applied
     * @param ba
     *            the byte array contains the new content to be overwritten
     * @param offset
     * @param len
     * @return boolean as the status of the overwrite operation
     *  
     */
	public boolean overWrite(int index, byte[] ba, int offset, int len){
	    if ( ba == null 
	            || index >= this.vtdSize
	            || offset<0 
	            || offset + len > ba.length)
	        throw new IllegalArgumentException("Illegal argument for overwrite");
	    if (encoding >=VTDNav.FORMAT_UTF_16BE 
	            && (((len & 1)==1 )
	            || ((offset&1)==1))){
	        // for UTF 16, len and offset must be integer multiple
	        // of 2
	        return false;
	    }
	    int t = getTokenType(index);
	    if ( t== VTDNav.TOKEN_CHARACTER_DATA
	            || t==VTDNav.TOKEN_ATTR_VAL 
	            || t==VTDNav.TOKEN_CDATA_VAL){
	        int length = getTokenLength(index);
	        if ( length < len)
	            return false;
	        int os = getTokenOffset(index);
	        int temp = length - len;
	        //System.out.println("temp ==>"+temp);
	        // get XML doc
	        System.arraycopy(ba, offset, XMLDoc.getBytes(),os, len);
	        for (int k=0;k<temp;){
	            //System.out.println("encoding ==>"+encoding);
	            if (encoding < VTDNav.FORMAT_UTF_16BE){
	             // write white spaces
	               // System.out.println("replacing...");
	                (XMLDoc.getBytes())[os+len+k] = ' ';
	                k++;
	            }else{
	                if (encoding == VTDNav.FORMAT_UTF_16BE){
	                    XMLDoc.getBytes()[os+len+k] = 0;
	                    XMLDoc.getBytes()[os+len+k+1] = ' ';
	                }else{	                    
	                    XMLDoc.getBytes()[os+len+k] = ' ';
	                    XMLDoc.getBytes()[os+len+k+1] = 0;
	                }
	                k += 2;
	            }
	        }    
	        return true;
	    }	    
	    return false;
	}
	/**
     * Convert a vtd token into a double. Creation date: (12/8/03 2:28:31 PM)
     * 
     * @return double
     * @exception com.ximpleware.NavException
     *                The exception if the underlying byte content contains
     *                various errors. Notice that we are being conservative in
     *                making little assumption on the correctness of underlying
     *                byte content. This is because the VTD can be generated by
     *                another machine such as a load-balancer.
     */
	public double parseDouble(int index) throws NavException {
		//if (matchTokenString()
		int offset = getTokenOffset(index);
		long l=0;
		int end = offset + getTokenLength(index);
		int t = getTokenType(index);
		boolean b = (t==VTDNav.TOKEN_CHARACTER_DATA )|| (t==VTDNav.TOKEN_ATTR_VAL);
		boolean expneg = false;
		int ch;
		//past the last one by one

		{
		l = b? getCharResolved(offset):getChar(offset);
		ch = (int)l;
	    offset += (int)(l>>32);
		}		

		while (offset < end) { // trim leading whitespaces
			if (!isWS(ch))
				break;
			l = b? getCharResolved(offset):getChar(offset);
			ch = (int)l;
		    offset += (int)(l>>32);
		}

		if (offset > end) // all whitespace
			return Double.NaN;

		boolean neg = (ch == '-');

		if (ch == '-' || ch == '+'){
		    l = b? getCharResolved(offset):getChar(offset);
			ch = (int)l;
		    offset += (int)(l>>32); //get another one if it is sign.
		}
		//left part of decimal
		double left = 0;
		while (offset <= end) {
			//must be <= since we get the next one at last.

			int dig = Character.digit((char) ch, 10); //only consider decimal
			if (dig < 0)
				break;

			left = left * 10 + dig;

			l = b? getCharResolved(offset):getChar(offset);
			ch = (int)l;
		    offset += (int)(l>>32);;
		}

		//right part of decimal
		double right = 0;
		double scale = 1;
		if (ch == '.') {
		    l = b? getCharResolved(offset):getChar(offset);
			ch = (int)l;
		    offset += (int)(l>>32);

			while (offset <= end) {
				//must be <= since we get the next one at last.

				int dig = Character.digit((char) ch, 10);
				//only consider decimal
				if (dig < 0)
					break;

				right = right * 10 + dig;
				scale *= 10;

				l = b? getCharResolved(offset):getChar(offset);
				ch = (int)l;
			    offset += (int)(l>>32);
			}
		}

		//exponent
		long exp = 0;
		if (ch == 'E' || ch == 'e') {
		    l = b? getCharResolved(offset):getChar(offset);
			ch = (int)l;
		    offset += (int)(l>>32);
			expneg = (ch == '-'); //sign for exp
			if (ch == '+' || ch == '-'){
			    l = b? getCharResolved(offset):getChar(offset);
				ch = (int)l;
			    offset += (int)(l>>32); //skip the +/- sign
			}
			int cur = offset;
			//remember the indx, used to find a invalid number like 1.23E

			while (offset <= end) {
				//must be <= since we get the next one at last.

				int dig = Character.digit((char) ch, 10);
				//only consider decimal
				if (dig < 0)
					break;

				exp = exp * 10 + dig;

				l = b? getCharResolved(offset):getChar(offset);
				ch = (int)l;
			    offset += (int)(l>>32);
			}
			if (cur == offset)
			    return Double.NaN;
			//found a invalid number like 1.23E

			//if (expneg)
			//	exp = (-exp);
		}

		//anything left must be space
		while (offset <= end) {
			if (!isWS(ch))
				return Double.NaN;

			l = b? getCharResolved(offset):getChar(offset);
			ch = (int)l;
		    offset += (int)(l>>32);
		}

		double v = (double) left;
		if (right != 0)
			v += ((double) right) / (double) scale;

		if (exp != 0)
			v = (expneg)? v /(Math.pow(10,exp)): v*Math.pow(10,exp);

		return ((neg) ? (-v) : v);
	}

	/**
     * Convert a vtd token into a float. we assume token type to be attr val or
     * character data Creation date: (12/8/03 2:28:18 PM)
     * 
     * @return float
     * @exception com.ximpleware.NavException
     *                The exception if the underlying byte content contains
     *                various errors. Notice that we are being conservative in
     *                making little assumption on the correctness of underlying
     *                byte content. This is because the VTD can be generated by
     *                another machine such as a load-balancer.
     */
	public float parseFloat(int index) throws NavException {

		int offset = getTokenOffset(index);
		int end = offset + getTokenLength(index);
		long l;
		//past the last one by one
		int t = getTokenType(index);
		boolean b = (t==VTDNav.TOKEN_CHARACTER_DATA )|| (t==VTDNav.TOKEN_ATTR_VAL);
		int ch ;
		l = b? getCharResolved(offset):getChar(offset);
		ch = (int)l;
	    offset += (int)(l>>32);

		while (offset <= end) { // trim leading whitespaces
			if (!isWS(ch))
				break;
			l = b? getCharResolved(offset):getChar(offset);
			ch = (int)l;
		    offset += (int)(l>>32);
		}

		if (offset > end) // all whitespace
			throw new NavException("Empty string");

		boolean neg = (ch == '-');

		if (ch == '-' || ch == '+'){
		    l = b? getCharResolved(offset):getChar(offset);
			ch = (int)l;
		    offset += (int)(l>>32); //get another one if it is sign.
		}
		//left part of decimal
		long left = 0;
		while (offset <= end) {
			//must be <= since we get the next one at last.

			int dig = Character.digit((char) ch, 10); //only consider decimal
			if (dig < 0)
				break;

			left = left * 10 + dig;

			l = b? getCharResolved(offset):getChar(offset);
			ch = (int)l;
		    offset += (int)(l>>32);
		}

		//right part of decimal
		long right = 0;
		long scale = 1;
		if (ch == '.') {
		    l = b? getCharResolved(offset):getChar(offset);
			ch = (int)l;
		    offset += (int)(l>>32);

			while (offset <= end) {
				//must be <= since we get the next one at last.

				int dig = Character.digit((char) ch, 10);
				//only consider decimal
				if (dig < 0)
					break;

				right = right * 10 + dig;
				scale *= 10;

				l = b? getCharResolved(offset):getChar(offset);
				ch = (int)l;
			    offset += (int)(l>>32);
			}
		}

		//exponent
		long exp = 0;
		if (ch == 'E' || ch == 'e') {
		    l = b? getCharResolved(offset):getChar(offset);
			ch = (int)l;
		    offset += (int)(l>>32);
			boolean expneg = (ch == '-'); //sign for exp
			if (ch == '+' || ch == '-'){
			    l = b? getCharResolved(offset):getChar(offset);
				ch = (int)l;
			    offset += (int)(l>>32); //skip the +/- sign
			}
			int cur = offset;
			//remember the indx, used to find a invalid number like 1.23E

			while (offset <= end) {
				//must be <= since we get the next one at last.

				int dig = Character.digit((char) ch, 10);
				//only consider decimal
				if (dig < 0)
					break;

				exp = exp * 10 + dig;

				l = b? getCharResolved(offset):getChar(offset);
				ch = (int)l;
			    offset += (int)(l>>32);
			}

			if (cur == offset)
				return Float.NaN;
			//found a invalid number like 1.23E

			if (expneg)
				exp = (-exp);
		}

		//anything left must be space
		while (offset <= end) {
			if (!isWS(ch))
				throw new NavException(toString(index));

			l = b? getCharResolved(offset):getChar(offset);
			ch = (int)l;
		    offset += (int)(l>>32);
		}

		double v = (double) left;
		if (right != 0)
			v += ((double) right) / (double) scale;

		if (exp != 0)
			v = v * Math.pow(10, exp);
		

		float f = (float) v;

		//try to handle overflow/underflow
		if (v >= (double)Float.MAX_VALUE)
			f = Float.MAX_VALUE;
		else if (v <= (double)Float.MIN_VALUE)
			f = Float.MIN_VALUE;
		if (neg)
			f = -f;
		return f;
	}
	/**
     * Convert a vtd token into an int. This method will automatically strip off
     * the leading and trailing we assume token type to be attr val or character
     * data zero, unlike Integer.parseInt(int index)
     * 
     * Creation date: (12/8/03 2:32:22 PM)
     * 
     * @return int
     * @param index
     *            int
     * @exception com.ximpleware.NavException
     *                The exception if the underlying byte content contains
     *                various errors. Notice that we are being conservative in
     *                making little assumption on the correctness of underlying
     *                byte content. This is because the VTD can be generated by
     *                another machine such as a load-balancer.
     */
	public int parseInt(int index) throws NavException {
		return parseInt(index, 10);
	}
	/**
     * Convert a vtd token into an int, with the given radix. we assume token
     * type to be attr val or character data the first char can be either '+' or
     * '-' Creation date: (12/16/03 1:21:20 PM)
     * 
     * @return int
     * @param index
     *            int
     * @param radix
     *            int
     * @exception com.ximpleware.NavException
     *                The exception if the underlying byte content contains
     *                various errors. Notice that we are being conservative in
     *                making little assumption on the correctness of underlying
     *                byte content. This is because the VTD can be generated by
     *                another machine such as a load-balancer.
     */
	protected int parseInt(int index, int radix) throws NavException {
		if (radix < 2 || radix > 36)
			throw new NumberFormatException(
				"radix " + radix + " out of valid range");
		int t = getTokenType(index);
		boolean b = (t==VTDNav.TOKEN_CHARACTER_DATA )|| (t==VTDNav.TOKEN_ATTR_VAL);
		int offset = getTokenOffset(index);
		int endOffset = offset + getTokenLength(index);

		int c;
		long l = b? getCharResolved(offset):getChar(offset);
		c = (int)l;
	    offset += (int)(l>>32);

		// trim leading whitespaces
		while ((c == ' ' || c == '\n' || c == '\t' || c == '\r')
			&& (offset <= endOffset)){
		    l = b? getCharResolved(offset):getChar(offset);
			c = (int)l;
		    offset += (int)(l>>32);
		}
		if (offset > endOffset) // all whitespace
			throw new NumberFormatException(" empty string");

		boolean neg = (c == '-');
		if (neg || c == '+') {
		    l = b? getCharResolved(offset):getChar(offset);
			c = (int)l;
		    offset += (int)(l>>32); //skip sign
		}
		long result = 0;
		while (offset <= endOffset) {
			int digit = Character.digit((char) c, radix);
			if (digit < 0)
				break;

			//Note: for binary we can simply shift to left to improve
            // performance
			result = result * radix + digit;
			//pos *= radix;

			l = b? getCharResolved(offset):getChar(offset);
			c = (int)l;
		    offset += (int)(l>>32);
		}

		if (result > Integer.MAX_VALUE)
			throw new NumberFormatException("Overflow: " + toString(index));

		// take care of the trailing
		while (offset <= endOffset && isWS(c)) {
		    l = b? getCharResolved(offset):getChar(offset);
			c = (int)l;
		    offset += (int)(l>>32);
		}
		if (offset == (endOffset + 1))
			return (int) ((neg) ? (-result) : result);
		else
			throw new NumberFormatException(toString(index));
	}
	/**
     * Convert a vtd token into a long. we assume token type to be attr val or
     * character data Creation date: (12/8/03 2:32:59 PM)
     * 
     * @return long
     * @param index
     *            int
     * @exception com.ximpleware.NavException
     *                The exception if the underlying byte content contains
     *                various errors. Notice that we are being conservative in
     *                making little assumption on the correctness of underlying
     *                byte content. This is because the VTD can be generated by
     *                another machine such as a load-balancer.
     */
	public long parseLong(int index) throws NavException {
		return parseLong(index, 10);
	}
	/**
     * Convert a vtd token into a long, with the given radix. the first char can
     * be either '+' or '-', leading and trailing will be stripped we assume
     * token type to be attr val or character data Creation date: (12/17/03
     * 1:51:06 PM)
     * 
     * @return long
     * @param index
     *            int
     * @param radix
     *            int
     * @exception com.ximpleware.NavException
     *                The exception if the underlying byte content contains
     *                various errors. Notice that we are being conservative in
     *                making little assumption on the correctness of underlying
     *                byte content. This is because the VTD can be generated by
     *                another machine such as a load-balancer.
     */
	protected long parseLong(int index, int radix) throws NavException {
		if (radix < 2 || radix > 36)
			throw new NumberFormatException(
				"radix " + radix + " out of valid range");
		
		int t = getTokenType(index);
		boolean b = (t==VTDNav.TOKEN_CHARACTER_DATA )|| (t==VTDNav.TOKEN_ATTR_VAL);
		
		int offset = getTokenOffset(index);
		int endOffset = offset + getTokenLength(index);

		int c;
		long l;
		l = b? getCharResolved(offset):getChar(offset);
		c = (int)l;
	    offset += (int)(l>>32);

		// trim leading whitespaces
		while ((c == ' ' || c == '\n' || c == '\t' || c == '\r')
			&& (offset <= endOffset)){
		    l = b? getCharResolved(offset):getChar(offset);
			c = (int)l;
		    offset += (int)(l>>32);
		}
		if (offset > endOffset) // all whitespace
			throw new NumberFormatException(" empty string");

		boolean neg = (c == '-');
		if (neg || c == '+'){
		    l = b? getCharResolved(offset):getChar(offset);
			c = (int)l;
		    offset += (int)(l>>32);//skip sign
		}
		long result = 0;
		while (offset <= endOffset) {
			int digit = Character.digit((char) c, radix);
			if (digit < 0)
				break;

			//Note: for binary we can simply shift to left to improve
            // performance
			result = result * radix + digit;
			//pos *= radix;

			l = b? getCharResolved(offset):getChar(offset);
			c = (int)l;
		    offset += (int)(l>>32);;
		}

		if (result > Long.MAX_VALUE)
			throw new NumberFormatException("Overflow: " + toString(index));

		// take care of the trailing
		while (offset <= endOffset && isWS(c)) {
		    l = b? getCharResolved(offset):getChar(offset);
			c = (int)l;
		    offset += (int)(l>>32);
		}
		if (offset == (endOffset + 1))
			return (long) ((neg) ? (-result) : result);
		else
			throw new NumberFormatException(toString(index));
	}
	
	/**
     * Load the context info from ContextBuffer. Info saved including LC and
     * current state of the context
     * 
     * @return boolean
     *  
     */
	public boolean pop() {
		boolean b = contextStack.load(stackTemp);
		if (b == false)
			return false;
		for (int i = 0; i < nestingLevel; i++) {
			context[i] = stackTemp[i];
		}

		l1index = stackTemp[nestingLevel];
		l2index = stackTemp[nestingLevel + 1];
		l3index = stackTemp[nestingLevel + 2];
		l2lower = stackTemp[nestingLevel + 3];
		l2upper = stackTemp[nestingLevel + 4];
		l3lower = stackTemp[nestingLevel + 5];
		l3upper = stackTemp[nestingLevel + 6];
		atTerminal = (stackTemp[nestingLevel + 7] == 1);
		LN = stackTemp[nestingLevel+8];
		return true;
	}
	/**
     * Load the context info from contextStack2. This method is dedicated for
     * XPath evaluation.
     * 
     * @return status of pop2
     */
	
	
	protected boolean pop2(){

		boolean b = contextStack2.load(stackTemp);
		if (b == false)
			return false;
		for (int i = 0; i < nestingLevel; i++) {
			context[i] = stackTemp[i];
		}
		l1index = stackTemp[nestingLevel];
		l2index = stackTemp[nestingLevel + 1];
		l3index = stackTemp[nestingLevel + 2];
		l2lower = stackTemp[nestingLevel + 3];
		l2upper = stackTemp[nestingLevel + 4];
		l3lower = stackTemp[nestingLevel + 5];
		l3upper = stackTemp[nestingLevel + 6];
		atTerminal = (stackTemp[nestingLevel + 7] == 1);
		LN = stackTemp[nestingLevel+8];
		return true;
	}
	
	/**
     * Store the context info into the ContextBuffer. Info saved including LC
     * and current state of the context Creation date: (11/16/03 7:00:27 PM)
     */
	public void push() {
		
		for (int i = 0; i < nestingLevel; i++) {
			stackTemp[i] = context[i];
		}
		stackTemp[nestingLevel] = l1index;
		stackTemp[nestingLevel + 1] = l2index;
		stackTemp[nestingLevel + 2] = l3index;
		stackTemp[nestingLevel + 3] = l2lower;
		stackTemp[nestingLevel + 4] = l2upper;
		stackTemp[nestingLevel + 5] = l3lower;
		stackTemp[nestingLevel + 6] = l3upper;
		if (atTerminal)
			stackTemp[nestingLevel + 7] =1;
		else
			stackTemp[nestingLevel + 7] =0;
		stackTemp[nestingLevel+8] = LN; 
		contextStack.store(stackTemp);
	}
	/**
     * Store the context info into the contextStack2. This method is reserved
     * for XPath Evaluation
     *  
     */
	
	protected void push2() {
		
		for (int i = 0; i < nestingLevel; i++) {
			stackTemp[i] = context[i];
		}
		stackTemp[nestingLevel] = l1index;
		stackTemp[nestingLevel + 1] = l2index;
		stackTemp[nestingLevel + 2] = l3index;
		stackTemp[nestingLevel + 3] = l2lower;
		stackTemp[nestingLevel + 4] = l2upper;
		stackTemp[nestingLevel + 5] = l3lower;
		stackTemp[nestingLevel + 6] = l3upper;
		if (atTerminal)
			stackTemp[nestingLevel + 7] =1;
		else
			stackTemp[nestingLevel + 7] =0;
		stackTemp[nestingLevel+8] = LN; 
		contextStack2.store(stackTemp);
	}
	
	/**
     * clear the contextStack2 after XPath evaluation
     * 
     *  
     */
	final protected void clearStack2(){
		contextStack2.size = 0;
	}
	
	/**
	 * Used by following:: axis
	 * @param depth
	 * @param index
	 */
	protected void sync(int depth, int index){
		// assumption is that this is always at terminal
		int t=-1;
		switch(depth){
		case -1: return;
		case 0: 
			if(l1Buffer.size!=0){
				if (l1index==-1)
					l1index=0;
				
				if (index> l1Buffer.upper32At(l1Buffer.size-1)){
					l1index = l1Buffer.size-1;
					return;
				}
				
				if (index > l1Buffer.upper32At(l1index)){
					while (l1index < l1Buffer.size - 1 && l1Buffer.upper32At(l1index) < index) {
						l1index++;
					}
				}
				else{
					while (l1index >0 && l1Buffer.upper32At(l1index-1) > index) {
						l1index--;
					}
				}
				//assert(index<l1Buffer.upper32At(l1index));
			}
			break;
		case 1:
			if (l1Buffer.lower32At(l1index)!=-1){
				if (l2lower!=l1Buffer.lower32At(l1index)){
					l2lower = l2index=l1Buffer.lower32At(l1index);
					l2upper = l2Buffer.size - 1;
					int size = l1Buffer.size;
					for (int i = l1index + 1; i < size; i++) {
						int temp = l1Buffer.lower32At(i);
						if (temp != 0xffffffff) {
							l2upper = temp - 1;
							break;
						}
					}
					//l2upper = l1Buffer.lower32At(l1index);
				} 
				
				if (index>l2Buffer.upper32At(l2index)){
					while (l2index < l2upper && l2Buffer.upper32At(l2index)< index){
						l2index++;					
					}
				} else {
					while(l2index > l2lower && l2Buffer.upper32At(l2index-1)> index){
						l2index--;
					}
				}
				
				//assert(index<l2Buffer.upper32At(l2index));
			}
			
			break;
		case 2:		
			if (l2Buffer.lower32At(l2index)!=-1){
				if (l3lower!=l2Buffer.lower32At(l2index)){
					l3index = l3lower = l2Buffer.lower32At(l2index);
					l3upper = l3Buffer.size - 1;
					int size = l2Buffer.size;
					for (int i = l2index + 1; i < size; i++) {
						int temp = l2Buffer.lower32At(i);
						if (temp != 0xffffffff) {
							l3upper = temp - 1;
							break;
						}
					}
				}
				if (index>l3Buffer.intAt(l3index)){
					while (l3index < l3upper && l3Buffer.intAt(l3index)<index  ){
						l3index++;
					}
				}else {
					while(l3index > l3lower && l3Buffer.intAt(l3index-1)> index){
						l3index--;
					}
				}
				
				//assert(index<l3Buffer.intAt(l3index));
			}
			break;
		default:
			if (l2Buffer.lower32At(l2index)!=-1){
				if (l3lower!=l2Buffer.lower32At(l2index)){
					l3index = l3lower = l2Buffer.lower32At(l2index);
					l3upper = l3Buffer.size - 1;
					int size = l2Buffer.size;
					for (int i = l2index + 1; i < size; i++) {
						int temp = l2Buffer.lower32At(i);
						if (temp != 0xffffffff) {
							l3upper = temp - 1;
							break;
						}
					}
				}
				//if (context[3]> l3Buffer.intAt(l3index)){
					while (context[3] != l3Buffer.intAt(l3index)){
						l3index++;
					}
				//} else {
				//	while (context[3] != l3Buffer.intAt(l3index)){
				//		l3index--;
				//	}
				//}
				
				//assert(index<l3Buffer.intAt(l3index));
			}
			break;
		}
	}
	
	/**
     * Sync up the current context with location cache. This operation includes
     * finding out l1index, l2index, l3index and restores upper and lower bound
     * info To improve efficieny this method employs some heuristic search
     * algorithm. The result is that it is quite close to direct access.
     * Creation date: (11/16/03 7:44:53 PM)
     * 
     * @return int The index of the NS URL
     */
	protected void resolveLC() {
		if (context[0]<=0)
			return;
		resolveLC_l1();
		if (context[0] == 1)
			return;
		resolveLC_l2();
		if (context[0] == 2)
			return;		
		resolveLC_l3();
	}
	
	/** 
	 * Sync level 1 location cache
	 */
	protected void resolveLC_l1(){
		if (l1index < 0 || l1index >= l1Buffer.size
				|| context[1] != l1Buffer.upper32At(l1index)) {
			if (l1index >= l1Buffer.size || l1index < 0) {
				l1index = 0;
			}
			if (l1index+1<l1Buffer.size && context[1] != l1Buffer.upper32At(l1index+1)) {
				int init_guess = (int) (l1Buffer.size * ((float) context[1] / vtdBuffer
						.size));
				if (l1Buffer.upper32At(init_guess) > context[1]) {
					while (l1Buffer.upper32At(init_guess) != context[1]) {
						init_guess--;
					}
				} else if (l1Buffer.upper32At(init_guess) < context[1]) {
					while (l1Buffer.upper32At(init_guess) != context[1]) {
						init_guess++;
					}
				}
				l1index = init_guess;
			} else{
				if (context[1]>=l1Buffer.upper32At(l1index)){
					while(context[1]!=l1Buffer.upper32At(l1index) 
						&& l1index < l1Buffer.size){
						l1index++;
					}
				}
				else{
					while(context[1]!=l1Buffer.upper32At(l1index) 
							&& l1index >=0){
							l1index--;
						}
				}
			}
		}
	}
	
	/**
	 * Sync Level 2 location cache
	 */
	protected void resolveLC_l2(){
		int temp = l1Buffer.lower32At(l1index);
		if (l2lower != temp) {
			l2lower = temp;
			// l2lower shouldn't be -1 !!!! l2lower and l2upper always get
			// resolved simultaneously
			l2index = l2lower;
			l2upper = l2Buffer.size - 1;
			for (int i = l1index + 1; i < l1Buffer.size; i++) {
				temp = l1Buffer.lower32At(i);
				if (temp != 0xffffffff) {
					l2upper = temp - 1;
					break;
				}
			}
		} // intelligent guess again ??

		if (l2index < 0 || l2index >= l2Buffer.size
				|| context[2] != l2Buffer.upper32At(l2index)) {
			
			if (l2index >= l2Buffer.size || l2index<0)
				l2index = l2lower;
			if (l2index+1< l2Buffer.size&& context[2] == l2Buffer.upper32At(l2index + 1))
				l2index = l2index + 1;
			else if (l2upper - l2lower >= 16) {
				int init_guess = l2lower
						+ (int) ((l2upper - l2lower)
								* ((float) context[2] - l2Buffer
										.upper32At(l2lower)) / (l2Buffer
								.upper32At(l2upper) - l2Buffer
								.upper32At(l2lower)));
				if (l2Buffer.upper32At(init_guess) > context[2]) {
					while (context[2] != l2Buffer.upper32At(init_guess))
						init_guess--;
				} else if (l2Buffer.upper32At(init_guess) < context[2]) {
					while (context[2] != l2Buffer.upper32At(init_guess))
						init_guess++;
				}
				l2index = init_guess;
			} else if (context[2]<l2Buffer.upper32At(l2index)){
				
				while ( context[2] != l2Buffer.upper32At(l2index)) {
					l2index--;
				}
			}
			else { 
				while(context[2]!=l2Buffer.upper32At(l2index))
					l2index++;
			}	
		}
	}
	
	/**
	 * Sync L3 location Cache
	 */
	protected void resolveLC_l3(){
		int temp = l2Buffer.lower32At(l2index);
		if (l3lower != temp) {
			//l3lower and l3upper are always together
			l3lower = temp;
			// l3lower shouldn't be -1
			l3index = l3lower;
			l3upper = l3Buffer.size - 1;
			for (int i = l2index + 1; i < l2Buffer.size; i++) {
				temp = l2Buffer.lower32At(i);
				if (temp != 0xffffffff) {
					l3upper = temp - 1;
					break;
				}
			}
		}

		if (l3index < 0 || l3index >= l3Buffer.size
				|| context[3] != l3Buffer.intAt(l3index)) {
			if (l3index >= l3Buffer.size || l3index <0)
				l3index = l3lower;
			if (l3index+1 < l3Buffer.size &&
					context[3] == l3Buffer.intAt(l3index + 1))
				l3index = l3index + 1;
			else if (l3upper - l3lower >= 16) {
				int init_guess = l3lower
						+ (int) ((l3upper - l3lower) * ((float) (context[3] - l3Buffer
								.intAt(l3lower)) / (l3Buffer.intAt(l3upper) - l3Buffer
								.intAt(l3lower))));
				if (l3Buffer.intAt(init_guess) > context[3]) {
					while (context[3] != l3Buffer.intAt(init_guess))
						init_guess--;
				} else if (l3Buffer.intAt(init_guess) < context[3]) {
					while (context[3] != l3Buffer.intAt(init_guess))
						init_guess++;
				}
				l3index = init_guess;
			} else if (context[3]<l3Buffer.intAt(l3index)){
				while (context[3] != l3Buffer.intAt(l3index)) {
					l3index--;
				}
			} else {
				while (context[3] != l3Buffer.intAt(l3index)) {
					l3index++;
				}
			}
		}
	}
	
	/**
     * Test whether the URL is defined in the scope. Null is allowed to indicate
     * the name space is undefined. Creation date: (11/16/03 7:54:01 PM)
     * 
     * @param URL
     *            java.lang.String
     * @exception com.ximpleware.NavException
     *                When there is any encoding conversion error or unknown
     *                entity.
     */
    final protected int lookupNS() throws NavException {
    	if (context[0]==-1)
    	    throw new NavException("Can't lookup NS for document node");
    	int i =
    		getTokenLength((context[0] != 0) ? context[context[0]] : rootIndex);
    	int offset =
    		getTokenOffset((context[0] != 0) ? context[context[0]] : rootIndex);
    	int preLen = (i >> 16) & 0xffff;
    
    	return lookupNS(offset, preLen);
 
    	//return resolveNS(URL, offset, preLen);
    }
    
    /**
     * This function returns the VTD record index of the namespace that matches
     * the prefix of cursor element
     * 
     * @param URL
     * @return int
     *  
     */
    protected int lookupNS(int offset, int len){
    	long l;
    	boolean hasNS = false;
    	int size = vtdBuffer.size;
    	// look for a match in the current hiearchy and return true
    	for (int i = context[0]; i >= 0; i--) {
    		int s = (i != 0) ? context[i] : rootIndex;
    		switch (NSval(s)) { // checked the ns marking
    			case 0xc0000000 :
    				s = s + 1;
    				if (s>=size)
    					break;
    				int type = getTokenType(s);
    
    				while ((type == TOKEN_ATTR_NAME || type == TOKEN_ATTR_NS)) {
    					if (type == TOKEN_ATTR_NS) {
    						// Get the token length
    						int temp = getTokenLength(s);
    						int preLen = ((temp >> 16) & 0xffff);
    						int fullLen = temp & 0xffff;
    						int os = getTokenOffset(s);
    						// xmlns found
    						if (temp == 5 && len == 0) {
    							return s+1;
    						} else if ((fullLen - preLen - 1) == len) {
    							// prefix length identical to local part of ns
                                // declaration
    							boolean a = true;
    							for (int j = 0; j < len; j++) {
    								if (getCharUnit(os + preLen + 1 + j)
    									!= getCharUnit(offset + j)) {
    									a = false;
    									break;
    								}
    							}
    							if (a == true) {
    								return s+1;
    							}
    						}
    					}
    					//return (URL != null) ? true : false;
    					s += 2;
    					if (s>=size)
    						break;
    					type = getTokenType(s);
    				}
    				break;
    			case 0x80000000 :
    				break;
    			default : // check the ns existence, mark bit 31:30 to 11 or 10
    				int k = s + 1;
    			    if (k>=size)
    			    	break;
    				type = getTokenType(k);
    
    				while ( (type == TOKEN_ATTR_NAME || type == TOKEN_ATTR_NS)) {
    					if (type == TOKEN_ATTR_NS) {
    						// Get the token length
    						hasNS = true;
    						int temp = getTokenLength(k);
    						int preLen = ((temp >> 16) & 0xffff);
    						int fullLen = temp & 0xffff;
    						int os = getTokenOffset(k);
    						// xmlns found
    						if (temp == 5 && len == 0) {
    							l = vtdBuffer.longAt(s);
    							hasNS = false;
    							vtdBuffer.modifyEntry(
    								s,
    								l | 0x00000000c0000000L);
    							
    							return k+1;
    							
    						} else if ((fullLen - preLen - 1) == len) {
    							// prefix length identical to local part of ns
                                // declaration
    							boolean a = true;
    							for (int j = 0; j < len; j++) {
    								if (getCharUnit(os + preLen + 1 + j)
    									!= getCharUnit(offset + j)) {
    									a = false;
    									break;
    								}
    							}
    							if (a == true) {
    								l = vtdBuffer.longAt(s);
    								//hasNS = false;
    								vtdBuffer.modifyEntry(
    									s,
    									l | 0x00000000c0000000L);
    								return k+1;
    							}
    						}
    					}
    					//return (URL != null) ? true : false;
    					k += 2;
    					if (k>=size) 
    						break;
    					type = getTokenType(k);
    				}
    				l = vtdBuffer.longAt(s);
    				if (hasNS) {
    					hasNS = false;
    					vtdBuffer.modifyEntry(s, l | 0x00000000c0000000L);
    				} else {
    					vtdBuffer.modifyEntry(s, l | 0x0000000080000000L);
    				}
    				break;
    		}
    	}
    	return 0;
        //return -1;
    }
    private boolean resolveNS(String URL, int offset, int len)
	throws NavException {
    
        int result = lookupNS(offset, len);
        switch(result){
        case 0: 
            if (URL == null){ 
              return true;
            } else {
               return false;
            }
        default:
            if (URL == null) {
            	if (getTokenLength(result)==0)
            		return true;
		        return false;
            }
		    else {
		        return matchNormalizedTokenString2(result, URL);
		    }
        	
        }
    }
	/**
     * A generic navigation method. Move the cursor to the element according to
     * the direction constants If no such element, no position change and return
     * false. Creation date: (12/2/03 1:43:50 PM) Legal direction constants are
     * 
     * <pre>
     *    			ROOT               0 
     * </pre>	
	 *<pre>
     *  		    PARENT  		   1 
     * </pre>
	 *<pre>
     *        	    FIRST_CHILD		   2 
     * </pre>  
	 *<pre>
     *  		    LAST_CHILD 		   3 
     * </pre>
	 *<pre>
     *     	  	    NEXT_SIBLING       4 
     * </pre>
	 *<pre>
     *       	    PREV_SIBLING       5 
     * </pre>
     * 
     * @return boolean
     * @param direction
     *            int
     * @exception com.ximpleware.NavException
     *                When direction value is illegal.
     */
	public boolean toElement(int direction) throws NavException {
		int size;
		//count++;
		//System.out.println("count ==>"+ count);
		switch (direction) {
			case ROOT : // to document element!
				if (context[0] != 0) {
					/*
                     * for (int i = 1; i <= context[0]; i++) { context[i] =
                     * 0xffffffff; }
                     */
					context[0] = 0;
				}
				atTerminal = false;
				//l1index = l2index = l3index = -1;
				return true;
			case PARENT :
				if (atTerminal == true){
					atTerminal = false;
					return true;
				}
				if (context[0] > 0) {
					//context[context[0]] = context[context[0] + 1] =
                    // 0xffffffff;
					context[context[0]] = -1;
					context[0]--;
					return true;
				}else if (context[0]==0){
					context[0]=-1; //to be compatible with XPath Data model
					return true;
 				}
				else {
					return false;
				}
			case FIRST_CHILD :
			case LAST_CHILD :
				if (atTerminal) return false;
				switch (context[0]) {
				    case -1:
				    	context[0] = 0;
				    	return true;
					case 0 :
						if (l1Buffer.size > 0) {
							context[0] = 1;
							l1index =
								(direction == FIRST_CHILD)
									? 0
									: (l1Buffer.size - 1);
							context[1] = l1Buffer.upper32At(l1index);
							//(int) (vtdToken >> 32);
							return true;
						} else
							return false;
					case 1 :
						l2lower = l1Buffer.lower32At(l1index);
						if (l2lower == -1) {
							return false;
						}
						context[0] = 2;
						l2upper = l2Buffer.size - 1;
						size = l1Buffer.size;
						for (int i = l1index + 1; i < size; i++) {
							int temp = l1Buffer.lower32At(i);
							if (temp != 0xffffffff) {
								l2upper = temp - 1;
								break;
							}
						}
						//System.out.println(" l2 upper: " + l2upper + " l2
                        // lower : " + l2lower);
						l2index =
							(direction == FIRST_CHILD) ? l2lower : l2upper;
						context[2] = l2Buffer.upper32At(l2index);
						return true;

					case 2 :
						l3lower = l2Buffer.lower32At(l2index);
						if (l3lower == -1) {
							return false;
						}
						context[0] = 3;

						l3upper = l3Buffer.size - 1;
						size = l2Buffer.size;
						for (int i = l2index + 1; i < size; i++) {
							int temp = l2Buffer.lower32At(i);
							if (temp != 0xffffffff) {
								l3upper = temp - 1;
								break;
							}
						}
						//System.out.println(" l3 upper : " + l3upper + " l3
                        // lower : " + l3lower);
						l3index =
							(direction == FIRST_CHILD) ? l3lower : l3upper;
						context[3] = l3Buffer.intAt(l3index);

						return true;

					default :
						if (direction == FIRST_CHILD) {
							size = vtdBuffer.size;
							int index = context[context[0]] + 1;
							while (index < size) {
								long temp = vtdBuffer.longAt(index);
								int token_type =
									(int) ((MASK_TOKEN_TYPE & temp) >> 60)
										& 0xf;

								if (token_type == TOKEN_STARTING_TAG) {
									int depth =
										(int) ((MASK_TOKEN_DEPTH & temp) >> 52);
									if (depth <= context[0]) {
										return false;
									} else if (depth == (context[0] + 1)) {
										context[0] += 1;
										context[context[0]] = index;
										return true;
									}
								}

								index++;
							} // what condition
							return false;
						} else {
							int index = context[context[0]] + 1;
							int last_index = -1;
							size = vtdBuffer.size;
							while (index < size) {
								long temp = vtdBuffer.longAt(index);
								int depth =
									(int) ((MASK_TOKEN_DEPTH & temp) >> 52);
								int token_type =
									(int) ((MASK_TOKEN_TYPE & temp) >> 60)
										& 0xf;
								
								if (token_type == TOKEN_STARTING_TAG) {
									if (depth <= context[0]) {
										break;
									} else if (depth == (context[0] + 1)) {
										last_index = index;
									}
								}

								index++;
							}
							if (last_index == -1) {
								return false;
							} else {
								context[0] += 1;
								context[context[0]] = last_index;
								return true;
							}
						}
				}

			case NEXT_SIBLING :
			case PREV_SIBLING :
				if(atTerminal)return nodeToElement(direction);
				switch (context[0]) {
					case -1:
					case 0 :
						return false;
					case 1 :
						if (direction == NEXT_SIBLING) {
							if (l1index + 1 >= l1Buffer.size) {
								return false;
							}

							l1index++; // global incremental
						} else {
							if (l1index - 1 < 0) {
								return false;
							}
							l1index--; // global incremental
						}
						context[1] = l1Buffer.upper32At(l1index);
						return true;
					case 2 :
						if (direction == NEXT_SIBLING) {
							if (l2index + 1 > l2upper) {
								return false;
							}
							l2index++;
						} else {
							if (l2index - 1 < l2lower) {
								return false;
							}
							l2index--;
						}
						context[2] = l2Buffer.upper32At(l2index);
						return true;
					case 3 :
						if (direction == NEXT_SIBLING) {
							if (l3index + 1 > l3upper) {
								return false;
							}
							l3index++;
						} else {
							if (l3index - 1 < l3lower) {
								return false;
							}
							l3index--;
						}
						context[3] = l3Buffer.intAt(l3index);
						return true;
					default :
						//int index = context[context[0]] + 1;

						if (direction == NEXT_SIBLING) {
							int index = context[context[0]] + 1;
							size = vtdBuffer.size;
							while (index < size) {
								long temp = vtdBuffer.longAt(index);
								int token_type =
									(int) ((MASK_TOKEN_TYPE & temp) >> 60)
										& 0xf;

								if (token_type == TOKEN_STARTING_TAG) {
									int depth =
										(int) ((MASK_TOKEN_DEPTH & temp) >> 52);
									if (depth < context[0]) {
										return false;
									} else if (depth == (context[0])) {
										context[context[0]] = index;
										return true;
									}
								}
								index++;
							}
							return false;
						} else {
							int index = context[context[0]] - 1;
							while (index > context[context[0] - 1]) {
								// scan backforward
								long temp = vtdBuffer.longAt(index);
								int token_type =
									(int) ((MASK_TOKEN_TYPE & temp) >> 60)
										& 0xf;

								if (token_type == TOKEN_STARTING_TAG) {
									int depth =
										(int) ((MASK_TOKEN_DEPTH & temp) >> 52);
									/*
                                     * if (depth < context[0]) { return false; }
                                     * else
                                     */
									if (depth == (context[0])) {
										context[context[0]] = index;
										return true;
									}
								}
								index--;
							} // what condition
							return false;
						}
				}

			default :
				throw new NavException("illegal navigation options");
		}

	}
	
	/** the corner case of element to node jump
	 * 
	 * @param direction
	 * @return
	 */
	protected boolean nodeToElement(int direction){
		switch(direction){
		case NEXT_SIBLING:
			switch (context[0]) {
			case 0:
				if (l1index!=-1){
					context[0]=1;
					context[1]=l1Buffer.upper32At(l1index);
					atTerminal=false;
					return true;
				}else
					return false;
			case 1:
				if (l2index!=-1){
					context[0]=2;
					context[2]=l2Buffer.upper32At(l2index);
					atTerminal=false;
					return true;
				}else
					return false;
				
			case 2:
				if (l3index!=-1){
					context[0]=3;
					context[3]=l3Buffer.intAt(l3index);
					atTerminal=false;
					return true;
				}else
					return false;
			default:
				int index = LN + 1;
				int size = vtdBuffer.size;
				while (index < size) {
					long temp = vtdBuffer.longAt(index);
					int token_type =
						(int) ((MASK_TOKEN_TYPE & temp) >> 60)
							& 0xf;

					if (token_type == TOKEN_STARTING_TAG) {
						int depth =
							(int) ((MASK_TOKEN_DEPTH & temp) >> 52);
						if (depth < context[0]) {
							return false;
						} else if (depth == (context[0])) {
							context[context[0]] = index;
							return true;
						}
					}
					index++;
				}
				return false;
				
			}
		case PREV_SIBLING:
			switch (context[0]) {
			case 0:
				if (l1index!=-1 && l1index>0){
					l1index--;
					context[0]=1;
					context[1]=l1Buffer.upper32At(l1index);
					atTerminal=false;
					return true;					
				}else
					return false;
			case 1:
				if (l2index!=-1 && l2index>l2lower){
					l2index--;
					context[0]=2;
					context[2]=l2Buffer.upper32At(l2index);
					atTerminal=false;
					return true;					
				}else
					return false;
			case 2:
				if (l2index!=-1 && l3index>l3lower){
					l3index--;
					context[0]=3;
					context[3]=l3Buffer.intAt(l3index);
					atTerminal=false;
					return true;					
				}else
					return false;
				
			default:
				int index = LN- 1;
				while (index > context[context[0] - 1]) {
					// scan backforward
					long temp = vtdBuffer.longAt(index);
					int token_type =
						(int) ((MASK_TOKEN_TYPE & temp) >> 60)
							& 0xf;

					if (token_type == TOKEN_STARTING_TAG) {
						int depth =
							(int) ((MASK_TOKEN_DEPTH & temp) >> 52);
						/*
                         * if (depth < context[0]) { return false; }
                         * else
                         */
						if (depth == (context[0])) {
							context[context[0]] = index;
							return true;
						}
					}
					index--;
				} // what condition
				return false;
			}
		}
		return false;
	}
	/**
     * A generic navigation method. Move the cursor to the element according to
     * the direction constants and the element name If no such element, no
     * position change and return false. "*" matches any element Creation date:
     * (12/2/03 1:43:50 PM) Legal direction constants are <br>
     * 
     * <pre>
     * 		ROOT            0  
     * </pre>
	 * <pre>
     * 		PARENT          1  
     * </pre>
	 * <pre>
     * 		FIRST_CHILD     2  
     * </pre>
	 * <pre>
     * 		LAST_CHILD      3  
     * </pre>
	 * <pre>
     * 		NEXT_SIBLING    4  
     * </pre>
	 * <pre>
     * 		PREV_SIBLING    5  
     * </pre>
     * 
     * <br>
     * for ROOT and PARENT, element name will be ignored.
     * 
     * @return boolean
     * @param direction
     *            int
     * @param en
     *            String
     * @exception com.ximpleware.NavException
     *                When direction value is illegal. Or there are errors in
     *                underlying byte representation of the document
     * @exception IllegalArguementException
     *                if en is null
     */
	public boolean toElement(int direction, String en) throws NavException {
		int temp=-1;
		int d=-1;
		int val=0;
		boolean b=false;
		if (en == null)
			throw new IllegalArgumentException(" Element name can't be null ");
		if (en.equals("*"))
			return toElement(direction);
		switch (direction) {
			case ROOT :
				return toElement(ROOT);

			case PARENT :
				return toElement(PARENT);

			case FIRST_CHILD :
				if (atTerminal)return false;
				if (toElement(FIRST_CHILD) == false)
					return false;
				// check current element name
				if (matchElement(en) == false) {
					if (toElement(NEXT_SIBLING, en) == true)
						return true;
					else {
						//toParentElement();
						//context[context[0]] = 0xffffffff;
						context[0]--;
						return false;
					}
				} else
					return true;

			case LAST_CHILD :
				if (atTerminal)return false;
				if (toElement(LAST_CHILD) == false)
					return false;
				if (matchElement(en) == false) {
					if (toElement(PREV_SIBLING, en) == true)
						return true;
					else {
						//context[context[0]] = 0xffffffff;
						context[0]--;
						//toParentElement();
						return false;
					}
				} else
					return true;

			case NEXT_SIBLING :
				if (atTerminal){					
					if (nodeToElement(NEXT_SIBLING)){
						b=true;
						if (matchElement(en)){
							return true;
						}					
					}else
						return false;
				}
				
				if (!b){
					d = context[0];
					switch(d)
					{
					case -1:
					case 0: return false;
					case 1: val = l1index; break;
					case 2: val = l2index; break;
					case 3: val = l3index; break;
				  		default:
					}				
					temp = context[d]; // store the current position
				}
				
				while (toElement(NEXT_SIBLING)) {
					if (matchElement(en)) {
						return true;
					}
				}
				if (b){
					context[0]--;//LN value should not change
					atTerminal=true;
					return false;
				} else {
					switch(d)
					{					
					case 1: l1index = val; break;
					case 2: l2index = val; break;
					case 3: l3index = val; break;
				  		default:
					}
					context[d] = temp;
					return false;
				}

			case PREV_SIBLING :
				if (atTerminal){					
					if (nodeToElement(PREV_SIBLING)){
						b=true;
						if (matchElement(en)){
							return true;
						}					
					}else
						return false;
				}				
				if (!b){
					d = context[0];
					switch(d)
					{						
					case -1:
					case 0: return false;
					case 1: val = l1index; break;
					case 2: val = l2index; break;
					case 3: val = l3index; break;
						default:
					}
					temp = context[d]; // store the current position
				}
				
				while (toElement(PREV_SIBLING)) {
					if (matchElement(en)) {
						return true;
					}
				}
				if (b){
					context[0]--;//LN value should not change
					atTerminal=true;
					return false;
				} else{	
					switch(d)
					{
					case 1: l1index = val; break;
					case 2: l2index = val; break;
					case 3: l3index = val; break;
				  		default:
					}
					context[d] = temp;
					return false;
				}

			default :
				throw new NavException("illegal navigation options");
		}
	}
	/**
     * A generic navigation method with namespace support. Move the cursor to
     * the element according to the direction constants and the prefix and local
     * names If no such element, no position change and return false. URL *
     * matches any namespace, including undefined namespaces a null URL means
     * hte namespace prefix is undefined for the element ln * matches any
     * localname Creation date: (12/2/03 1:43:50 PM) Legal direction constants
     * are <br>
     * 
     * <pre>
     * 		ROOT            0  
     * </pre>
	 * <pre>
     * 		PARENT          1  
     * </pre>
	 * <pre>
     * 		FIRST_CHILD     2  
     * </pre>
	 * <pre>
     * 		LAST_CHILD      3  
     * </pre>
	 * <pre>
     * 		NEXT_SIBLING    4  
     * </pre>
	 * <pre>
     * 		PREV_SIBLING    5  
     * </pre>
     * 
     * <br>
     * for ROOT and PARENT, element name will be ignored. If not ns enabled,
     * return false immediately with no position change.
     * 
     * @return boolean
     * @param direction
     *            int
     * @param URL
     *            String
     * @param ln
     *            String
     * @exception com.ximpleware.NavException
     *                When direction value is illegal. Or there are errors in
     *                underlying byte representation of the document
     */
	public boolean toElementNS(int direction, String URL, String ln)
		throws NavException {
		boolean b=false;
		int temp=-1;
		int val=0;
		int d=-1; // temp location
		if (ns == false)
			return false;
		switch (direction) {
			case ROOT :
				return toElement(ROOT);

			case PARENT :
				return toElement(PARENT);

			case FIRST_CHILD :
				if (atTerminal)return false;
				if (toElement(FIRST_CHILD) == false)
					return false;
				// check current element name
				if (matchElementNS(URL, ln) == false) {
					if (toElementNS(NEXT_SIBLING, URL, ln) == true)
						return true;
					else {
						//toParentElement();
						//context[context[0]] = 0xffffffff;
						context[0]--;
						return false;
					}
				} else
					return true;

			case LAST_CHILD :
				if (atTerminal)return false;
				if (toElement(LAST_CHILD) == false)
					return false;
				if (matchElementNS(URL, ln) == false) {
					if (toElementNS(PREV_SIBLING, URL, ln) == true)
						return true;
					else {
						//context[context[0]] = 0xffffffff;
						context[0]--;
						//toParentElement();
						return false;
					}
				} else
					return true;

			case NEXT_SIBLING :
				if (atTerminal){					
					if (nodeToElement(NEXT_SIBLING)){
						b=true;
						if (matchElementNS(URL,ln)){
							return true;
						}					
					}else
						return false;
				}
				if (!b){
					d = context[0];
					temp = context[d]; // store the current position
					switch(d)
					{
					case -1:
					case 0: return false;
					case 1: val = l1index; break;
					case 2: val = l2index; break;
					case 3: val = l3index; break;
				  		default:
					}
				}
				//if (d == 0)
				//	return false;
				while (toElement(NEXT_SIBLING)) {
					if (matchElementNS(URL, ln)) {
						return true;
					}
				}
				if (b){					
					context[0]--;//LN value should not change
					atTerminal=true;
					return false;
				}
				else{
					switch(d)
					{
					case 1: l1index = val; break;
					case 2: l2index = val; break;
					case 3: l3index = val; break;
				  		default:
					}
					context[d] = temp;
					return false;
				}

			case PREV_SIBLING :
				if (atTerminal){
					if (nodeToElement(PREV_SIBLING)){
						b=true;
						if (matchElementNS(URL,ln)){
							return true;
						}					
					}else
						return false;
					}
				if (!b){
					d = context[0];
					temp = context[d]; // store the current position
					switch(d)
					{
						case -1:
						case 0: return false;
						case 1: val = l1index; break;
						case 2: val = l2index; break;
						case 3: val = l3index; break;
						default:
					}
				}
				//if (d == 0)
				//	return false;
				while (toElement(PREV_SIBLING)) {
					if (matchElementNS(URL, ln)) {
						return true;
					}
				}
				if (b){
					context[0]--;//LN value should not change
					atTerminal=true;
					return false;
				}else {
					switch(d)
					{
						case 1: l1index = val; break;
						case 2: l2index = val; break;
						case 3: l3index = val; break;
						default:
					}
					context[d] = temp;
					return false;
				}

			default :
				throw new NavException("illegal navigation options");
		}

	}
	
	/**
	 * (New since version 2.9)
	 * Shallow Normalization follows the rules below to normalize a token into
	 * a string
	 * *#xD#xA gets converted to #xA
	 * *For a character reference, append the referenced character to the normalized value.
	 * *For an entity reference, recursively apply step 3 of this algorithm to the replacement text of the entity.
	 * *For a white space character (#x20, #xD, #xA, #x9), append a space character (#x20) to the normalized value.
	 * *For another character, append the character to the normalized value.
	 * @param index
	 * @return
	 * @throws NavException
	 */
	public String toNormalizedString2(int index) throws NavException{
		int type = getTokenType(index);
		if (type!=TOKEN_CHARACTER_DATA &&
				type!= TOKEN_ATTR_VAL)
			return toRawString(index); 
		long l;
		int len;
		len = getTokenLength(index);
		if (len == 0)
			return "";
		int offset = getTokenOffset(index);
		int endOffset = len + offset - 1; // point to the last character
		StringBuffer sb = new StringBuffer(len);
		
		int ch;

		//boolean d = false;
		while (offset <= endOffset) {
			l = getCharResolved(offset);
			ch = (int)l;
			offset += (int)(l>>32);
			if (isWS(ch) && (l>>32)<=2) {
				//d = true;
				sb.append(' ');
			} else
				sb.append((char) ch);
		}
		return sb.toString();
	}
	/**
     * This method normalizes a token into a string value of character data 
     * and attr val in a way that resembles DOM. The leading and trailing 
     * white space characters will be stripped. The entity and character 
     * references will be resolved Multiple whitespaces char will be collapsed 
     * into one. Whitespaces via entities will nonetheless be preserved. 
     * Creation date: (12/8/03 1:57:10 PM)
     * 
     * @return java.lang.String
     * @param index
     *     int
     * @exception NavException
     *     When the encoding has errors
     */
	public String toNormalizedString(int index) throws NavException {
		int type = getTokenType(index);
		if (type!=TOKEN_CHARACTER_DATA &&
				type!= TOKEN_ATTR_VAL)
			return toRawString(index); 
		long l;
		int len;
		len = getTokenLength(index);
		if (len == 0)
			return "";
		int offset = getTokenOffset(index);
		int endOffset = len + offset - 1; // point to the last character
		StringBuffer sb = new StringBuffer(len);
		
		int ch;

		// trim off the leading whitespaces

		while (true) {
			int temp = offset;
			l = getChar(offset);
			
			ch = (int)l;
			offset += (int)(l>>32);

			if (!isWS(ch)) {
				offset = temp;
				break;
			}
		}

		boolean d = false;
		while (offset <= endOffset) {
			l = getCharResolved(offset);
			ch = (int)l;
			offset += (int)(l>>32);
			if (isWS(ch) && getCharUnit(offset - 1) != ';') {
				d = true;
			} else {
				if (d == false)
					sb.append((char) ch); // java only supports 16 bit unicode
				else {
					sb.append(' ');
					sb.append((char) ch);
					d = false;
				}
			}
		}

		return sb.toString();
	}
	
	
	/**
	 * Get the string length of a token as if it is converted into a normalized UCS string
	 * @param index
	 * @return the string length
	 * @throws NavException
	 *
	 */
	final public int getNormalizedStringLength(int index) throws NavException {
		int type = getTokenType(index);
		if (type!=TOKEN_CHARACTER_DATA &&
				type!= TOKEN_ATTR_VAL)
			return getRawStringLength(index); 
		long l;
		int len,len1=0;
		len = getTokenLength(index);
		if (len == 0)
			return 0;
		int offset = getTokenOffset(index);
		int endOffset = len + offset - 1; // point to the last character
		//StringBuffer sb = new StringBuffer(len);
		
		int ch;
		// trim off the leading whitespaces

		while (true) {
			int temp = offset;
			l = getChar(offset);
			
			ch = (int)l;
			offset += (int)(l>>32);

			if (!isWS(ch)) {
				offset = temp;
				break;
			}
		}

		boolean d = false;
		while (offset <= endOffset) {
			l = getCharResolved(offset);
			ch = (int)l;
			offset += (int)(l>>32);
			if (isWS(ch) && getCharUnit(offset - 1) != ';') {
				d = true;
			} else {
				if (d == false)
					len1++; // java only supports 16 bit unicode
				else {
					len1= len1+2;
					d = false;
				}
			}
		}

		return len1;
	}

	/**
     * Convert a token at the given index to a String, 
     * (entities and char references not expanded).
     * Creation date: (11/16/03 7:28:49 PM)
     * 
     * @return java.lang.String
     * @param index   int
     * @exception NavException When the encoding has errors
     */
	final public String toRawString(int index) throws NavException {
		int type = getTokenType(index);
		int len;
		if (type == TOKEN_STARTING_TAG
			|| type == TOKEN_ATTR_NAME
			|| type == TOKEN_ATTR_NS)
			len = getTokenLength(index) & 0xffff;
		else
			len = getTokenLength(index);
		int offset = getTokenOffset(index);
		return toRawString(offset, len);
	}
	
	final protected void toRawString(StringBuilder sb, int index) 
		throws NavException {		
		int type = getTokenType(index);
		int len;
		if (type == TOKEN_STARTING_TAG
			|| type == TOKEN_ATTR_NAME
			|| type == TOKEN_ATTR_NS)
			len = getTokenLength2(index) & 0xffff;
		else
			len = getTokenLength2(index);
		int offset = getTokenOffset(index);
		toRawString(offset, len,sb);
	}
	/**
	 * Convert a token at the given index to a String, upper case chars
	 * get converted into lower case 
     * (entities and char references not expanded).
	 * @param index
	 * @return
	 * @throws NavException
	 */
	final public String toRawStringLowerCase(int index) throws NavException {
		int type = getTokenType(index);
		int len;
		if (type == TOKEN_STARTING_TAG
			|| type == TOKEN_ATTR_NAME
			|| type == TOKEN_ATTR_NS)
			len = getTokenLength(index) & 0xffff;
		else
			len = getTokenLength(index);
		int offset = getTokenOffset(index);
		return toRawStringLowerCase(offset, len);
	}
	/**
	 * Convert a token at the given index to a String, lower case chars
	 * get converted into upper case 
     * (entities and char references not expanded).
	 * @param index
	 * @return
	 * @throws NavException
	 */
	final public String toRawStringUpperCase(int index) throws NavException {
		int type = getTokenType(index);
		int len;
		if (type == TOKEN_STARTING_TAG
			|| type == TOKEN_ATTR_NAME
			|| type == TOKEN_ATTR_NS)
			len = getTokenLength(index) & 0xffff;
		else
			len = getTokenLength(index);
		int offset = getTokenOffset(index);
		return toRawStringUpperCase(offset, len);
	}
	
	/**
	 * Convert a segment of XML bytes a into string, without entity resolution
	 * @param os (in terms of chars <em>not bytes</em>)
	 * @param len (in terms of chars <em>not bytes</em>)
	 * @return
	 * @throws NavException
	 *
	 */
	final public String toRawString(int os, int len) throws NavException{
	    StringBuffer sb = new StringBuffer(len);	    
	    int offset = os;
	    int endOffset = os + len;
	    //if (encoding> FORMAT_WIN_1258){
	    //	offset = offset>>1;
		//	endOffset = endOffset>>1;
	    //}
	    long l;
	    while (offset < endOffset) {
	        l = getChar(offset);
	        offset += (int)(l>>32);
	        sb.append((char)l);	                
	    }
	    return sb.toString();
	}
	
	final protected void toRawString(int os, int len, StringBuffer sb) throws NavException {
		int offset = os;
	    int endOffset = os + len;
	    long l;
	    while (offset < endOffset) {
	        l = getChar(offset);
	        offset += (int)(l>>32);
	        sb.append((char)l);	                
	    }
	}
	
	final protected void toRawString(int os, int len, StringBuilder sb) throws NavException {
		int offset = os;
	    int endOffset = os + len;
	    long l;
	    while (offset < endOffset) {
	        l = getChar(offset);
	        offset += (int)(l>>32);
	        sb.append((char)l);	                
	    }
	}
	
	final protected void toString(int os, int len, StringBuilder sb) throws NavException {
		int offset = os;
	    int endOffset = os + len;
	    long l;
	    while (offset < endOffset) {
	        l = getCharResolved(offset);
	        offset += (int)(l>>32);
	        sb.append((char)l);	                
	    }
	}
	
	final protected void toStringUpperCase(int os, int len, StringBuilder sb) throws NavException {
	    //StringBuilder sb = new StringBuilder(len);	    
	    int offset = os;
	    int endOffset = os + len;
	    long l;
	    while (offset < endOffset) {
	        l = getCharResolved(offset);
	        offset += (int)(l>>32);
	        if ((int)l>96 && (int)l<123)
	        	sb.append((char)(l-32));
	        else
	        	sb.append((char)l);	                
	    }
	    //return sb.toString();
	}
	
	final protected void toStringLowerCase(int os, int len, StringBuilder sb) throws NavException {
		//StringBuilder sb = new StringBuilder(len);	    
	    int offset = os;
	    int endOffset = os + len;
	    long l;
	    while (offset < endOffset) {
	        l = getCharResolved(offset);
	        offset += (int)(l>>32);
	        if ((int)l>64 && (int)l<91)
	        	sb.append((char)(l+32));
	        else
	        	sb.append((char)l);	                
	    }
	    //return sb.toString();
	}
	
	/**
	 * getStringLength return the string length of a token as if the token is converted into 
	 * a string (entity resolved for character data and attr val)
	 * @param index
	 * @return the string length as if the token is converted to a UCS string (entity resolved)
	 * @throws NavException
	 *
	 */
	final public int getStringLength(int index) throws NavException {
        int type = getTokenType(index);
        if (type != TOKEN_CHARACTER_DATA && type != TOKEN_ATTR_VAL)
            return getRawStringLength(index);
        int len = 0, len1 = 0;
        
        len = getTokenLength(index);
        int offset = getTokenOffset(index);
        int endOffset = offset + len;
        long l;

        while (offset < endOffset) {
            l = getCharResolved(offset);
            offset += (int) (l >> 32);
            len1++;
        }
        return len1;
    }


	/**
	 * Get the string length as if the token is converted into a UCS string (entity not resolved)
	 * @param index
	 * @return
	 * @throws NavException
	 *
	 */
	final public int getRawStringLength(int index) throws NavException {
        int type = getTokenType(index);
        int len = 0, len1 = 0;
        if (type == TOKEN_STARTING_TAG || type == TOKEN_ATTR_NAME
                || type == TOKEN_ATTR_NS)
            len = getTokenLength(index) & 0xffff;
        else
            len = getTokenLength(index);
        if (encoding!=VTDNav.FORMAT_UTF8 &&
        		encoding!=VTDNav.FORMAT_UTF_16BE &&
        		encoding!=VTDNav.FORMAT_UTF_16LE) {
        	return len;
        }
        int offset = getTokenOffset(index);
        int endOffset = offset + len;
        long l;
        while (offset < endOffset) {
            l = getChar(offset);
            offset += (int) (l >> 32);
            len1++;
        }
        return len1;
    }
	
	/**
     * Convert a token at the given index to a String, (entities and char
     * references resolved character data and attr val). An attribute name or 
     * an element name will get the UCS2 string of qualified name 
     * Creation date: (11/16/03 7:27:19 PM)
     * 
     * @return java.lang.String
     * @param index
     * @exception NavException
     */
	public String toString(int index) throws NavException {
		int type = getTokenType(index);
		if (type!=TOKEN_CHARACTER_DATA &&
				type!= TOKEN_ATTR_VAL)
			return toRawString(index); 
		int len;
		len = getTokenLength(index);

		int offset = getTokenOffset(index);
		return toString(offset, len);
	}
	
	/**
	 * Convert a token at the given index to a String and any lower case
	 * character will be converted to upper case, (entities and char
     * references resolved character data and attr val). 
	 * @param index
	 * @return
	 * @throws NavException
	 */
	public String toStringUpperCase(int index) throws NavException {
		int type = getTokenType(index);
		if (type!=TOKEN_CHARACTER_DATA &&
				type!= TOKEN_ATTR_VAL)
			return toRawStringUpperCase(index); 
		int len;
		len = getTokenLength(index);

		int offset = getTokenOffset(index);
		return toStringUpperCase(offset, len);
	}
	
	/**
	 * Convert a token at the given index to a String and any upper case
	 * character will be converted to lower case, (entities and char
     * references resolved for character data and attr val).
	 * @param index
	 * @return
	 * @throws NavException
	 */
	public String toStringLowerCase(int index) throws NavException {
		int type = getTokenType(index);
		if (type!=TOKEN_CHARACTER_DATA &&
				type!= TOKEN_ATTR_VAL)
			return toRawStringLowerCase(index); 
		int len;
		len = getTokenLength(index);

		int offset = getTokenOffset(index);
		return toStringLowerCase(offset, len);
	}
	
	/**
     * Convert the byte content segment (in terms of offset and length) to
     * String (entities are resolved)
     * 
     * @param os 
     *            the char offset of the segment (not byte)
     * @param len  
     *            the length of the segment in char (not byte)
     * @return the corresponding string value
     * @throws NavException
     *  
     */
	final public String toString(int os, int len) throws NavException{
	    StringBuffer sb = new StringBuffer(len);	    
	    int offset = os;
	    int endOffset = os + len;
	    //if (encoding> FORMAT_WIN_1258){
	    //	offset = offset>>1;
		//	endOffset = endOffset>>1;
	    //}
	    	
	    long l;
	    while (offset < endOffset) {
	        l = getCharResolved(offset);
	        offset += (int)(l>>32);
	        sb.append((char)l);	                
	    }
	    return sb.toString();
	}
	
	final protected void toString(StringBuilder sb, int index) 
	throws NavException {	
		/*int type = getTokenType(index);
		if (type!=TOKEN_CHARACTER_DATA &&
				type!= TOKEN_ATTR_VAL)
			toRawString(sb, index);*/ 
		int len,type;
		len = getTokenLength2(index);
		type= getTokenType(index);

		int offset = getTokenOffset(index);
		if (type!=VTDNav.TOKEN_CDATA_VAL)
			toString(offset, len, sb);
		else
			toRawString(offset, len, sb);
	}
	
	
	
	final protected void toStringUpperCase(StringBuilder sb, int index) 
	throws NavException {	
		/*int type = getTokenType(index);
		if (type!=TOKEN_CHARACTER_DATA &&
				type!= TOKEN_ATTR_VAL)
			toRawString(sb, index);*/ 
		int len,type;
		len = getTokenLength2(index);
		type= getTokenType(index);
		
		int offset = getTokenOffset(index);
		if (type!=VTDNav.TOKEN_CDATA_VAL)
			toStringUpperCase(offset, len, sb);
		else
			toRawStringUpperCase(offset, len, sb);
	}
	
	final protected void toStringLowerCase(StringBuilder sb, int index) 
	throws NavException {	
		/*int type = getTokenType(index);
		if (type!=TOKEN_CHARACTER_DATA &&
				type!= TOKEN_ATTR_VAL)
			toRawString(sb, index); */
		int len,type;
		len = getTokenLength2(index);
		type= getTokenType(index);

		int offset = getTokenOffset(index);
		if (type!=VTDNav.TOKEN_CDATA_VAL)
			toStringLowerCase(offset, len, sb);
		else
			toRawStringLowerCase(offset, len, sb);
	}
	
	/**
     * Convert the byte content segment (in terms of offset and length) to
     * String, lower case characters are converted to upper case
     * 
     * @param os
     *            the offset of the segment
     * @param len
     *            the length of the segment
     * @return the corresponding string value
     * @throws NavException
     *  
     */
	final protected String toStringUpperCase(int os, int len) throws NavException{
	    StringBuilder sb = new StringBuilder(len);	    
	    int offset = os;
	    int endOffset = os + len;
	    long l;
	    while (offset < endOffset) {
	        l = getCharResolved(offset);
	        offset += (int)(l>>32);
	        if ((int)l>96 && (int)l<123)
	        	sb.append((char)(l-32));
	        else
	        	sb.append((char)l);	                
	    }
	    return sb.toString();
	}
	/**
     * Convert the byte content segment (in terms of offset and length) to
     * String, upper case characters are converted to lower case
     * 
     * @param os
     *            the offset of the segment
     * @param len
     *            the length of the segment
     * @return the corresponding string value
     * @throws NavException
     *  
     */
	final protected String toStringLowerCase(int os, int len) throws NavException{
	    StringBuilder sb = new StringBuilder(len);	    
	    int offset = os;
	    int endOffset = os + len;
	    long l;
	    while (offset < endOffset) {
	        l = getCharResolved(offset);
	        offset += (int)(l>>32);
	        if ((int)l>64 && (int)l<91)
	        	sb.append((char)(l+32));
	        else
	        	sb.append((char)l);	                
	    }
	    return sb.toString();
	}
	
	
	final protected String toRawStringLowerCase(int os, int len) throws NavException{
	    StringBuilder sb = new StringBuilder(len);	    
	    int offset = os;
	    int endOffset = os + len;
	    long l;
	    while (offset < endOffset) {
	        l = getChar(offset);
	        offset += (int)(l>>32);
	        if ((int)l>64 && (int)l<91)
	        	sb.append((char)(l+32));
	        else
	        	sb.append((char)l);	                
	    }
	    return sb.toString();
	}
	
	final protected void toRawStringLowerCase(int os, int len, StringBuilder sb) throws NavException{
	    //StringBuilder sb = new StringBuilder(len);	    
	    int offset = os;
	    int endOffset = os + len;
	    long l;
	    while (offset < endOffset) {
	        l = getChar(offset);
	        offset += (int)(l>>32);
	        if ((int)l>64 && (int)l<91)
	        	sb.append((char)(l+32));
	        else
	        	sb.append((char)l);	                
	    }
	    //return sb.toString();
	}
	
	
	
	final protected String toRawStringUpperCase(int os, int len) throws NavException{
	    StringBuilder sb = new StringBuilder(len);	    
	    int offset = os;
	    int endOffset = os + len;
	    long l;
	    while (offset < endOffset) {
	        l = getChar(offset);
	        offset += (int)(l>>32);
	        if ((int)l>96 && (int)l<123)
	        	sb.append((char)(l-32));
	        else
	        	sb.append((char)l);	                
	    }
	    return sb.toString();
	}
	
	final protected void toRawStringUpperCase(int os, int len, StringBuilder sb) throws NavException{
	    //StringBuilder sb = new StringBuilder(len);	    
	    int offset = os;
	    int endOffset = os + len;
	    long l;
	    while (offset < endOffset) {
	        l = getChar(offset);
	        offset += (int)(l>>32);
	        if ((int)l>96 && (int)l<123)
	        	sb.append((char)(l-32));
	        else
	        	sb.append((char)l);	                
	    }
	    //return sb.toString();
	}
	
/**
 * This method matches two VTD tokens of VTDNav objects
 * 
 * @param i1
 *            index of the first token
 * @param vn2
 *            the second VTDNav instance
 * @param i2
 *            index of the second token
 * @return boolean true if two tokens are lexically identical
 *  
 */
	final public boolean matchTokens(int i1, VTDNav vn2, int i2) 
	throws NavException{
	    return compareTokens(i1,vn2,i2)==0;
	}
	
	/**
     * Set the value of atTerminal This function only gets called in XPath eval
     * when a step calls for @* or child::text()
     * @param b
     */
	final protected void setAtTerminal(boolean b){
		atTerminal = b;
	}
	
	/**
	 * Test the start of token content at index i matches the content 
	 * of s, notice that this is to save the string allocation cost of
	 * using String's built-in startsWidth 
	 * @param i
	 * @param s
	 * @return
	 */
	final public boolean startsWith(int index, String s) throws NavException{
		int type = getTokenType(index);
		int len =
			(type == TOKEN_STARTING_TAG
				|| type == TOKEN_ATTR_NAME
				|| type == TOKEN_ATTR_NS)
				? getTokenLength(index) & 0xffff
				: getTokenLength(index);
	    int offset = getTokenOffset(index);
	    long l1; 
	    int i,l;
	    int endOffset = offset + len;
	    boolean b = (type == TOKEN_ATTR_VAL
	    		|| type == TOKEN_CHARACTER_DATA);
        //       System.out.print("currentOffset :" + currentOffset);
        l = s.length();
        if (l> len)
        	return false;
        //System.out.println(s);
        for (i = 0; i < l && offset < endOffset; i++) {
        	if (b)
        		l1 = getCharResolved(offset);
        	else
        		l1 = getChar(offset);
            int i1 = s.charAt(i);
            if (i1 != (int) l1)
                return false;
            offset += (int) (l1 >> 32);
        }	    
		return true;
	}
	
	/**
	 * Test the end of token content at index i matches the content 
	 * of s, notice that this is to save the string allocation cost of
	 * using String's built-in endsWidth 
	 * @param i
	 * @return
	 */
	final public boolean endsWith(int index, String s) throws NavException{
		int type = getTokenType(index);
		int len =
			(type == TOKEN_STARTING_TAG
				|| type == TOKEN_ATTR_NAME
				|| type == TOKEN_ATTR_NS)
				? getTokenLength(index) & 0xffff
				: getTokenLength(index);
	    int offset = getTokenOffset(index);
	    long l1; 
	    int i,l, i2;
	    boolean b = (type == TOKEN_ATTR_VAL
	    		|| type == TOKEN_CHARACTER_DATA);
	    //int endOffset = offset + len;
	    
        //       System.out.print("currentOffset :" + currentOffset);
        l = s.length();
        if (l>len)
        	return false;
        if (b == true)
        	i2 = getStringLength(index);
        else 
        	i2 = getRawStringLength(index);
        if (l> i2)
        	return false;
        i2 = i2 - l; // calculate the # of chars to be skipped
        // eat away first several chars
        for (i = 0; i < i2; i++) {
        	if (b== true)
        		l1 = getCharResolved(offset);
        	else 
        		l1 = getChar(offset);
            offset += (int) (l1 >> 32);
        }
        //System.out.println(s);
        for (i = 0; i < l; i++) {
        	if (b==true)
        		l1 = getCharResolved(offset);
        	else
        		l1 = getChar(offset);
            int i1 = s.charAt(i);
            if (i1 != (int) l1)
                return false;
            offset += (int) (l1 >> 32);
        }	    
		return true;
	}
	
	/**
	 * Test whether a given token contains s. notie that this function
	 * directly operates on the byte content of the token to avoid string creation
	 * @param index
	 * @param s
	 * @return
	 * @throws NavException
	 */
	final public boolean contains(int index, String s) throws NavException{
		int type = getTokenType(index);
		int len =
			(type == TOKEN_STARTING_TAG
				|| type == TOKEN_ATTR_NAME
				|| type == TOKEN_ATTR_NS)
				? getTokenLength(index) & 0xffff
				: getTokenLength(index);
	    int offset = getTokenOffset(index);
	    long l1; 
	    int i,l;
	    int endOffset = offset + len;
	    boolean b = (type == TOKEN_ATTR_VAL
	    		|| type == TOKEN_CHARACTER_DATA);
        //       System.out.print("currentOffset :" + currentOffset);
        int gOffset = offset;
        l = s.length();
        if (l> len)
        	return false;
        //System.out.println(s);
        while( offset<endOffset){
        	gOffset = offset;
        	if (endOffset-gOffset< l)
        		return false;
			for (i = 0; i < l && gOffset < endOffset; i++) {
				if (b==true)
					l1 = getCharResolved(gOffset);
				else
					l1 = getChar(gOffset);
				int i1 = s.charAt(i);
				gOffset += (int) (l1 >> 32);
				if (i ==0)
					offset = gOffset;
				if (i1 != (int) l1)
					break;				
			}
			if (i==l)
				return true;
        }
		return false;
	}
	
	/**
	 * Get the value of atTerminal This function only gets called in XPath eval
	 * 
	 * @return boolean
	 */
	final protected boolean getAtTerminal(){
		return atTerminal;
	}
	/**
     * This is for debugging purpose
     * 
     * @param fib
     */
	
	public boolean verifyNodeCorrectness(){
	 	if (atTerminal){
			// check l1 index, l2 index, l2lower, l2upper, l3 index, l3 lower, l3 upper
			if (getTokenDepth(LN)!=context[0])
				return false;
			switch(context[0]){
				case -1: return true;
				case 0: 
					//if (getTokenDepth(LN)!=0)
					//	return false;
					if (l1Buffer.size!=0){
						if (l1index>=l1Buffer.size || l1index<0)
							return false;
						if (l1index != l1Buffer.size-1){
							
							if (l1Buffer.upper32At(l1index)<LN)
								return false;								
						}						
						return true;
					}else
						return true;
					
			case 1:
				if (LN>context[1]){
					//if (getTokenDepth(LN) != 1)
					//	return false;
					if (l1index<0 || l1index>l1Buffer.size)
						return false;
					int i1, i2; // l2lower, l2upper and l2index
					i1 = l1Buffer.lower32At(l1index);
					if (i1 != -1) {
						if (i1 != l2lower)
							return false;
						int tmp = l1index + 1;
						i2 = l2Buffer.size - 1;
						while (tmp < l1Buffer.size) {
							if (l1Buffer.lower32At(tmp) != -1) {
								i2 = l1Buffer.lower32At(tmp) - 1;
								break;
							} else
								tmp++;
						}
						if (l2upper != i2)
							return false;
						if (l2index > l2upper || l2index < l2lower)
							return false;
						if (l2index != l2upper) {
							if (l2Buffer.upper32At(l2index) < LN)
								return false;
						} 
						if (l2index!=l2lower){
							if (l2Buffer.upper32At(l2index-1)>LN)
								return false;
						}
					}
					return true;
				}else
					return false;
			case 2:  
				if (LN>context[2] && context[2]> context[1]){
					//if (getTokenDepth(LN) != 2)
					//	return false;
					if (l1index<0 || l1index>l1Buffer.size)
						return false;
					int i1,i2; //l2lower, l2upper and l2index
					i1 = l1Buffer.lower32At(l1index);
					if(i1==-1)return false;
					if (i1!=l2lower)
						return false;
					int tmp = l1index+1;
					i2 = l2Buffer.size-1;
					while(tmp<l1Buffer.size){
						if (l1Buffer.lower32At(tmp)!=-1){
							i2 = l1Buffer.lower32At(tmp)-1;
							break;
						}else
							tmp++;
					}
					if(context[2]!=l2Buffer.upper32At(l2index)){
						return false;
					}
					if (l2index>l2upper || l2index < l2lower){
						return false;
					}
					//l3 
					i1 = l2Buffer.lower32At(l2index);
					if (i1!=-1){
						if (l3lower!=i1)
							return false;
						i2 = l3Buffer.size-1;
						tmp = l2index+1;
						
						while(tmp<l2Buffer.size){
							if (l2Buffer.lower32At(tmp)!=-1){
								i2 = l2Buffer.lower32At(tmp)-1;
								break;
							}else
								tmp++;
						}
						
						if (l3lower!=i1)
							return false;
						
						if (l3upper!=i2)
							return false;
						
						if (l3index<i1 || l3index>i2)
							return false;
						
						if (l3index != l3upper) {
							if (l3Buffer.intAt(l3index) < LN)
								return false;
						} 		
						if (l3index!=l3lower){
							if (l3Buffer.intAt(l3index-1)>LN)
								return false;
						}
					}
					return true;
				}else 
					return false;
				
			default:  
				//if (getTokenDepth(LN) != 2)
				//	return false;
				if (l1index<0 || l1index>l1Buffer.size)
					return false;
				int i1,i2; //l2lower, l2upper and l2index
				i1 = l1Buffer.lower32At(l1index);
				
				if (i1==-1)return false;
				if (i1!=l2lower)
					return false;
				int tmp = l1index+1;
				i2 = l2Buffer.size-1;
				while(tmp<l1Buffer.size){
					if (l1Buffer.lower32At(tmp)!=-1){
						i2 = l1Buffer.lower32At(tmp)-1;
						break;
					}else
						tmp++;
				}
				if(context[2]!=l2Buffer.upper32At(l2index)){
					return false;
				}
				if (l2index>l2upper || l2index < l2lower){
					return false;
				}
				//l3 
				i1 = l2Buffer.lower32At(l2index);
				if (i1==-1)
					return false;
				if (i1!=l3lower)
					return false;
				i2 = l3Buffer.size-1;
				tmp = l2index+1;
				
				while(tmp<l2Buffer.size){
					if (l2Buffer.lower32At(tmp)!=-1){
						i2 = l2Buffer.lower32At(tmp)-1;
						break;
					}else
						tmp++;
				}
					
				if (l3lower!=i1)
					return false;
					
				if (l3upper!=i2)
					return false;
					
				if (l3index<i1 || l3index>i2)
					return false;
					
				if (context[context[0]]>LN)
					return false;
				
				if (context[0]==3){
					if (l3index!=l3upper){
						if(l3Buffer.intAt(l3index)>LN)
							return false;
					}
					if (l3index+1 <= l3Buffer.size-1){
						if (l3Buffer.intAt(l3index+1)<LN){
							return false;
						}
					}
				}
							
			}
			return true;
				
			
		}else {
			switch(context[0]){
			case -1:
			case 0:
			case 1:
			case 2:
			case 3:
			case 4:
			case 5:
				default:return true;
			}
			
		}
	}
	
	public void sampleState(FastIntBuffer fib){
//		for(int i=0;i<context.)
//			context[i] = -1;
		for (int i=0;i<=context[0];i++)
		   fib.append(context[i]);
		if (atTerminal) fib.append(LN);
		if (context[0]>=1)
			fib.append(l1index);	
		
		if (context[0]>=2){
			fib.append(l2index);
			fib.append(l2lower);
			fib.append(l2upper);				
		} // else return;
		
		if (context[0]>=3){
		   fib.append(l3index);
		   fib.append(l3lower);
		   fib.append(l3upper);
		} //else return;
	}
	
	public void dumpState(){
		System.out.println("l1 index ==>"+l1index);
		System.out.println("l2 index ==>"+l2index);
		System.out.println("l2 lower ==>"+l2lower);
		System.out.println("l2 upper ==>"+l2upper);
		System.out.println("l3 index ==>"+l3index);
		System.out.println("l3 lower ==>"+l3lower);
		System.out.println("l3 upper ==>"+l3upper);
	}
	
	/**
     * Write VTDNav's internal structure into an OutputStream
     * 
     * @param os
     * @throws IndexWriteException
     * @throws IOException
     *  
     */
	public void writeIndex(OutputStream os) throws IndexWriteException, IOException{
	    IndexHandler.writeIndex_L3((byte)1,
	            this.encoding,
	            this.ns,
	            true,
	            this.nestingLevel-1,
	            3,
	            this.rootIndex,
	            this.XMLDoc.getBytes(),
	            this.docOffset,
	            this.docLen,
	            (FastLongBuffer)this.vtdBuffer,
	            (FastLongBuffer)this.l1Buffer,
	            (FastLongBuffer)this.l2Buffer,
	            (FastIntBuffer)this.l3Buffer,
	            os);
	}
	
	/**
	 * Write VTDNav's VTD and LCs into an OutputStream (XML not written out)
	 * @param os
	 * @throws IndexWriteException
	 * @throws IOException
	 *
	 */
	public void writeSeparateIndex(OutputStream os) throws IndexWriteException, IOException{
	    IndexHandler.writeSeparateIndex_L3((byte)2,
	            this.encoding,
	            this.ns,
	            true,
	            this.nestingLevel-1,
	            3,
	            this.rootIndex,
	           // this.XMLDoc.getBytes(),
	            this.docOffset,
	            this.docLen,
	            (FastLongBuffer)this.vtdBuffer,
	            (FastLongBuffer)this.l1Buffer,
	            (FastLongBuffer)this.l2Buffer,
	            (FastIntBuffer)this.l3Buffer,
	            os);
	}
	/**
     * Write VTDNav's internal structure into a VTD+XML file
     * 
     * @param fileName
     * @throws IOException
     * @throws IndexWriteException
     *  
     */
	public void writeIndex(String fileName) throws IOException,IndexWriteException{
	    FileOutputStream fos = new FileOutputStream(fileName);
	    writeIndex(fos);
	    fos.close();
	}
	/**
	 * Write VTDNav's internal structure (VTD and LCs, but not XML) into a file
	 * @param fileName
	 * @throws IOException
	 * @throws IndexWriteException
	 *
	 */
	public void writeSeparateIndex(String fileName) throws IOException,IndexWriteException{
	    FileOutputStream fos = new FileOutputStream(fileName);
	    writeSeparateIndex(fos);
	    fos.close();
	}
	
	/**
     * Precompute the size of VTD+XML index
     * 
     * @return size of the index
     *  
     */
	 
	public long getIndexsize(){
	    int size;
	    if ( (docLen & 7)==0)
	       size = docLen;
	    else
	       size = ((docLen >>3)+1)<<3;
	    
	    size += (vtdBuffer.size<<3)+
	            (l1Buffer.size<<3)+
	            (l2Buffer.size<<3);
	    
	    if ((l3Buffer.size & 1) == 0){ //even
	        size += l3Buffer.size<<2;
	    } else {
	        size += (l3Buffer.size+1)<<2; //odd
	    }
	    return size+64;
	}
	
	/**
	 * Duplicate the VTDNav instance with shared XML, VTD and LC buffers
	 * This method may be useful for parallel XPath evaluation
	 * The node Position is at root element
	 * @return a VTDNav instance
	 *
	 */
	public VTDNav duplicateNav(){
	    return new VTDNav(rootIndex,
	            encoding,
	            ns,
	            nestingLevel-1,
	            XMLDoc,
	            vtdBuffer,
	            l1Buffer,
	            l2Buffer,
	            l3Buffer,
	            docOffset,
	            docLen
	            );
	}
	
	/**
	 * Clone the VTDNav instance to get with shared XML, VTD and LC buffers
	 * The node position is also copied from the original instance
	 * @return a new instance of VTDNav
	 */
	public VTDNav cloneNav(){
		VTDNav vn = new VTDNav(rootIndex,
	            encoding,
	            ns,
	            nestingLevel-1,
	            XMLDoc,
	            vtdBuffer,
	            l1Buffer,
	            l2Buffer,
	            l3Buffer,
	            docOffset,
	            docLen
	            );
		vn.atTerminal = this.atTerminal;
		vn.LN = this.LN;
		if (this.context[0]!=-1)
			System.arraycopy(this.context, 0, vn.context, 0, this.context[0]+1 );
		else 
			vn.context[0]=-1;
		vn.l1index = l1index; 
		if (getCurrentDepth()>1){
			vn.l2index = this.l2index;
			vn.l2upper = l2upper;
			vn.l2lower = l2lower;
		}
		if (getCurrentDepth() > 2) {
			vn.l3lower = l3lower;
			vn.l3index = l3index;
			vn.l3upper = l3upper;
		}
		return vn;
	}
	
	/**
	 * This function return the substring 
	 * @param offset
	 * @return sub string of  
	 */
	/*final public String toSubString(int index, int so){
		int type = getTokenType(index);
		int len =
			(type == TOKEN_STARTING_TAG
				|| type == TOKEN_ATTR_NAME
				|| type == TOKEN_ATTR_NS)
				? getTokenLength(index) & 0xffff
				: getTokenLength(index);
	    int offset = getTokenOffset(index);
	    long l1; 
	    int i,l;
	    int endOffset = offset + len;
	    
        if (so >= len)
        	return "";
        return "";
	}*/
	
	/**
	 * 
	 */
	public static final short XPATH_STRING_MODE_NORMAL = 0;
	public static final short XPATH_STRING_MODE_UPPERCASE = 1;
	public static final short XPATH_STRING_MODE_LOWERCASE = 2;
	
	final public void fillXPathString(FastIntBuffer indexBuffer, FastIntBuffer countBuffer) throws NavException{
		int count = 0;
		int index = getCurrentIndex() + 1;
		int tokenType, depth, t=0, length,i=0;
		int dp = context[0];
		//int size = vtdBuffer.size;
		// store all text tokens underneath the current element node
		while (index < vtdSize) {
		    tokenType = getTokenType(index);
		    depth = getTokenDepth(index);
		    if (depth<dp || 
		    		(depth==dp && tokenType==VTDNav.TOKEN_STARTING_TAG)){
		    	break;
		    }
		    
		    if (tokenType==VTDNav.TOKEN_CHARACTER_DATA
		    		|| tokenType==VTDNav.TOKEN_CDATA_VAL){
		    	length = getTokenLength(index);
		    	t += length;
		    	fib.append(index);
		    	if (length > VTDGen.MAX_TOKEN_LENGTH){
		    		while(length > VTDGen.MAX_TOKEN_LENGTH){
		    			length -=VTDGen.MAX_TOKEN_LENGTH;
		    			i++;
		    		}
		    		index += i+1;
		    	}else
		    		index++;
		    	continue;
		    	//
		    } else if (tokenType==VTDNav.TOKEN_ATTR_NAME
			        || tokenType == VTDNav.TOKEN_ATTR_NS){			  
			    index = index+2;
			    continue;
			}			
			index++;
		}		
	}
	
	final public String getXPathStringVal() throws NavException{
		return getXPathStringVal((short)0);
	}
	/**
	 * Return the String value of an Element Node
	 * @param mode
	 * @return
	 * @throws NavException
	 */
	final public String getXPathStringVal(short mode) throws NavException{
		return getXPathStringVal2(getCurrentIndex(),mode);
		/*int index = getCurrentIndex() + 1;
		int tokenType, depth, t=0;
		int dp = context[0];
		//int size = vtdBuffer.size;
		// store all text tokens underneath the current element node
		while (index < vtdSize) {
		    tokenType = getTokenType(index);
		    depth = getTokenDepth(index);
		    t = t + getTokenLength2(index);
		    if (depth<dp || 
		    		(depth==dp && tokenType==VTDNav.TOKEN_STARTING_TAG)){
		    	break;
		    }
		    switch(tokenType){
		      case VTDNav.TOKEN_ATTR_NAME:
		      case VTDNav.TOKEN_ATTR_NS:	
		      case VTDNav.TOKEN_PI_NAME:
		    	 	index = index+2;
				    continue;
		    
		      case VTDNav.TOKEN_CHARACTER_DATA:
		      case VTDNav.TOKEN_CDATA_VAL:
		    	  	fib.append(index);
		    	  	index++;
		    	  	continue;
		    }
		    /*if (tokenType==VTDNav.TOKEN_CHARACTER_DATA
		    		|| tokenType==VTDNav.TOKEN_CDATA_VAL){
		    	length = getTokenLength(index);
		    	t += length;
		    	fib.append(index);
		    	if (length > VTDGen.MAX_TOKEN_LENGTH){
		    		while(length > VTDGen.MAX_TOKEN_LENGTH){
		    			length -=VTDGen.MAX_TOKEN_LENGTH;
		    			i++;
		    		}
		    		index += i+1;
		    	}else
		    		index++;
		    	continue;
		    	//
		    } else if (tokenType==VTDNav.TOKEN_ATTR_NAME
			        || tokenType == VTDNav.TOKEN_ATTR_NS){			  
			    index = index+2;
			    continue;
			}*/	
		//	index++;
		//}
		
		// calculate the total length
		/*StringBuilder sb = new StringBuilder(t);
		
		for(t=0;t<fib.size;t++ ){
			switch(mode){
				case 0:toString(sb,fib.intAt(t)); break;
				case 1:toStringUpperCase(sb, fib.intAt(t)); break;
				case 2:toStringLowerCase(sb, fib.intAt(t)); break;
				default:throw new NavException("Invaild xpath string val mode");
			}			
			
		}
				
		// clear the fib and return a string
		fib.clear();
		return sb.toString();*/
	}

	final protected String getXPathStringVal2(int j, short mode) throws NavException{
		/*if (j>= vtdSize) throw new NavException("Invalid vtd-xml index, out of range");
		int tokenType = getTokenType(j);
		if (tokenType!= VTDNav.TOKEN_STARTING_TAG && tokenType != VTDNav.TOKEN_DOCUMENT)
			throw new NavException("Node type incorrect for XPath STring val");*/
		int tokenType;
		int index = j + 1;
		int depth, t=0, length,i=0;
		int dp = getTokenDepth(j);
		//int size = vtdBuffer.size;
		// store all text tokens underneath the current element node
		while (index < vtdSize) {
		    tokenType = getTokenType(index);
		    depth = getTokenDepth(index);
		    t=t+getTokenLength2(index);
		    if (depth<dp || 
		    		(depth==dp && tokenType==VTDNav.TOKEN_STARTING_TAG)){
		    	break;
		    }
		    
		    if (tokenType==VTDNav.TOKEN_CHARACTER_DATA
		    		|| tokenType==VTDNav.TOKEN_CDATA_VAL){
		    	length = getTokenLength(index);
		    	t += length;
		    	fib.append(index);
		    	if (length > VTDGen.MAX_TOKEN_LENGTH){
		    		while(length > VTDGen.MAX_TOKEN_LENGTH){
		    			length -=VTDGen.MAX_TOKEN_LENGTH;
		    			i++;
		    		}
		    		index += i+1;
		    	}else
		    		index++;
		    	continue;
		    	//
		    } else if (tokenType==VTDNav.TOKEN_ATTR_NAME
			        || tokenType == VTDNav.TOKEN_ATTR_NS
			        || tokenType == VTDNav.TOKEN_PI_NAME){			  
			    index = index+2;
			    continue;
			}			
			index++;
		}
		
		// calculate the total length
		StringBuilder sb = new StringBuilder(t);
		
		for(t=0;t<fib.size;t++ ){
			switch(mode){
				case 0:toString(sb,fib.intAt(t)); break;
				case 1:toStringUpperCase(sb, fib.intAt(t)); break;
				case 2:toStringLowerCase(sb, fib.intAt(t)); break;
				//case 3:toNormalizedString(sb,fib.intAt(t)); break;
				default:throw new NavException("Invaild xpath string val mode");
			}			
			
		}
				
		// clear the fib and return a string
		fib.clear();
		return sb.toString();
	}
	
	final public boolean XPathStringVal_Contains(int j, String s) throws NavException{
		int tokenType;
		int index = j + 1;
		int depth, t=0, i=0,offset, endOffset,len,type,c;
		long l;
		boolean result=false;
		int dp = getTokenDepth(j);
		//int size = vtdBuffer.size;
		// store all text tokens underneath the current element node
		while (index < vtdSize) {
		    tokenType = getTokenType(index);
		    depth = getTokenDepth(index);
		    //t=t+getTokenLength2(index);
		    if (depth<dp || 
		    		(depth==dp && tokenType==VTDNav.TOKEN_STARTING_TAG)){
		    	break;
		    }
		    
		    if (tokenType==VTDNav.TOKEN_CHARACTER_DATA
		    		|| tokenType==VTDNav.TOKEN_CDATA_VAL){
		    	//length = getTokenLength2(index);
		    	//t += length;
		    	fib.append(index);
		    	index++;
		    	continue;
		    	//
		    } else if (tokenType==VTDNav.TOKEN_ATTR_NAME
			        || tokenType == VTDNav.TOKEN_ATTR_NS
			        || tokenType == VTDNav.TOKEN_PI_NAME){			  
			    index = index+2;
			    continue;
			}			
			index++;
		}
		
		index=0;
		loop:while(index<fib.size){
			type = getTokenType(fib.intAt(index));
			offset = getTokenOffset(fib.intAt(index));
			len = getTokenLength(fib.intAt(index));
			endOffset = offset+len;
			while(offset<endOffset){
				if (type==VTDNav.TOKEN_CHARACTER_DATA)
					l = getCharResolved(offset);
				else
					l = getChar(offset);
				c = (int)l;
				if (c==s.charAt(0)&& matchSubString(offset, endOffset, index, type,s)){
					result=true;
					break loop;
				}else
					offset += (int)(l>>32);
			}			
			index++;
		}
		
		
		fib.clear();
		return result;
	}
	
	private boolean matchSubString(int os, int eos, int index, int t, String s) throws NavException{
		int offset = os, endOffset=eos, type =t, c;long l;
		int i=0;
		boolean b=false;
		while(offset<endOffset){
			if (type==VTDNav.TOKEN_CHARACTER_DATA)
				l = getCharResolved(offset);
			else
				l = getChar(offset);
			c = (int)l;
			if (i<s.length()-1 && c==s.charAt(i)){		
				offset += (int)(l>>32);
				i++;
			}else if(i==s.length()-1)
				return true;
			else
				return false;				
		}
		index++;
		while(index<fib.size){		
			offset = getTokenOffset(fib.intAt(index));
			endOffset = offset + getTokenLength2(fib.intAt(index));
			type = getTokenType(fib.intAt(index));
			while(offset<endOffset){
				if (type==VTDNav.TOKEN_CHARACTER_DATA)
					l = getCharResolved(offset);
				else
					l = getChar(offset);
				c = (int)l;
				if (i<s.length() && c==s.charAt(i)){		
					offset += (int)(l>>32);
					i++;
				}else if(i==s.length())
					return true;
				else
					return false;				
			}
			index++;
		}while(index<fib.size);
		if (i==s.length())
			return true;
		return false;
	}
	
	final public boolean XPathStringVal_StartsWith(int j, String s) throws NavException{
		int tokenType;
		int index = j + 1;
		int depth,length,i=0, offset, endOffset, len,c;
		long l;
		int dp = getTokenDepth(j);
		boolean r = false;//default
		//int size = vtdBuffer.size;
		// store all text tokens underneath the current element node
		while (index < vtdSize) {
		    tokenType = getTokenType(index);
		    depth = getTokenDepth(index);
		    //t=t+getTokenLength2(index);
		    if (depth<dp ||
		    		(depth==dp && tokenType==VTDNav.TOKEN_STARTING_TAG)){
		    	break;
		    }
		    
		    if (tokenType==VTDNav.TOKEN_CHARACTER_DATA ){
		    	//if (!match)
		    	offset = getTokenOffset(index);
		    	len = getTokenLength2(index);
		    	endOffset = offset + len;
		    	while(offset<endOffset){
		    		l = getCharResolved(offset);
		    		c = (int)l;
		    		
		    		if (i< s.length()&& c == s.charAt(i)){
		    			offset += (int)(l>>32);
		    			i++;
		    		}else if (i==s.length())
		    			return true;
		    		else
		    			return false;
		    		
		    	}
		    	index++;
		    	continue;
		    }else if( tokenType==VTDNav.TOKEN_CDATA_VAL){
		    	offset = getTokenOffset(index);
		    	len = getTokenLength2(index);
		    	endOffset = offset + len;
		    	while(offset<endOffset){
		    		l = getChar(offset);
		    		c = (int)l;
		    		
		    		if (i< s.length()&& c == s.charAt(i)){
		    			offset += (int)(l>>32);
		    			i++;
		    		}else if (i==s.length())
		    			return true;
		    		else
		    			return false;
		    		
		    	}
		    	index++;
		    	continue;
		    }else if (tokenType==VTDNav.TOKEN_ATTR_NAME
			        || tokenType == VTDNav.TOKEN_ATTR_NS
			        || tokenType == VTDNav.TOKEN_PI_NAME){			  
			    
		    	index = index+2;
			    continue;
			}			
			index++;
		}
		return false;
	}
	
	final public boolean XPathStringVal_EndsWith(int j,String s) throws NavException{
		int tokenType;
		int index = j + 1;
		int depth, t=0, length,i=0,d=0, offset,endOffset,type;
		boolean b=false;
		long l;
		int dp = getTokenDepth(j);
		//int size = vtdBuffer.size;
		// store all text tokens underneath the current element node
		while (index < vtdSize) {
		    tokenType = getTokenType(index);
		    depth = getTokenDepth(index);
		    //t=t+getStringLength(index);
		    if (depth<dp || 
		    		(depth==dp && tokenType==VTDNav.TOKEN_STARTING_TAG)){
		    	break;
		    }
		    
		    if (tokenType==VTDNav.TOKEN_CHARACTER_DATA
		    		|| tokenType==VTDNav.TOKEN_CDATA_VAL){
		    	length = getTokenLength2(index);
		    	//t += length;
		    	fib.append(index);
		    	index++;
		    	continue;
		    	//
		    } else if (tokenType==VTDNav.TOKEN_ATTR_NAME
			        || tokenType == VTDNav.TOKEN_ATTR_NS
			        || tokenType == VTDNav.TOKEN_PI_NAME){			  
			    index = index+2;
			    continue;
			}			
			index++;
		}
		//if (t<s.length())
		//	return false;
		for (i=fib.size-1;i!=0;i--){
			t+=getStringLength(fib.intAt(i));
			if (t>=s.length()){
				d = t-s.length();//# of chars to be skipped
				break;
			}
		}
		
		if (i<-1)return false;
		type = getTokenType(fib.intAt(i));
		offset = getTokenOffset(fib.intAt(i));
		endOffset = offset+getTokenLength2(fib.intAt(i));
		for(j=0;j<d;j++){
			if (type==VTDNav.TOKEN_CHARACTER_DATA){
				l=getCharResolved(offset);
			}else
				l=getChar(offset);
			offset += (int)(l>>32);
		}
		b =matchSubString(offset, endOffset,i,type,s);
		fib.clear();
		return b;
	}
	
	
	
	/**
	 * Get content fragment returns a long encoding the offset and length of the byte segment of
	 * the content of current element, which is the byte segment between the starting tag and 
	 * ending tag, -1 is returned if the current element is an empty element
	 * 
	 * @return long whose upper 32 bite is length, lower 32 bit is offset
	 */
	public long getContentFragment() throws NavException{
		// a little scanning is needed
		// has next sibling case
		// if not
		
		int depth = getCurrentDepth();
//		 document length and offset returned if depth == -1
		if (depth == -1){
		    int i=vtdBuffer.lower32At(0);
		    if (i==0)
		        return ((long)docLen)<<32| docOffset;
		    else
		        return ((long)(docLen-32))| 32;
		}

		long l = getOffsetAfterHead();
		if (l<0)
			return -1L;
		int so = (int)l;
		
		int length = 0;
		

		// for an element with next sibling
		if (toElement(NEXT_SIBLING)) {

			int temp = getCurrentIndex();
			//temp2=temp;
			// rewind
			while (getTokenDepth(temp) == depth && 
					(getTokenType(temp)==VTDNav.TOKEN_COMMENT || 
							getTokenType(temp)==VTDNav.TOKEN_PI_VAL ||
							getTokenType(temp)==VTDNav.TOKEN_PI_NAME)) {
				
				temp--;
			}
			/*if(temp!=temp2)
				temp++;*/
			//temp++;
			int so2 = getTokenOffset(temp) - 1;
			// look for the first '>'
			while (getCharUnit(so2) != '>') {
				so2--;
			}
			while (getCharUnit(so2) != '/') {
				so2--;
			}
			while (getCharUnit(so2) != '<') {
				so2--;
			}
			
			length = so2 - so;
			toElement(PREV_SIBLING);
			if (encoding <= FORMAT_WIN_1258)
				return ((long) length) << 32 | so;
			else
				return ((long) length) << 33 | (so << 1);
		}

		// for root element
		if (depth == 0) {
			int temp = vtdBuffer.size - 1;
			boolean b = false;
			int so2 = 0;
			while (getTokenDepth(temp) == -1) {
				temp--; // backward scan
				b = true;
			}
			if (b == false)
				so2 =
					(encoding <= FORMAT_WIN_1258 )
						? (docOffset + docLen - 1)
						: ((docOffset + docLen) >> 1) - 1;
			else
				so2 = getTokenOffset(temp + 1);
			while (getCharUnit(so2) != '>') {
				so2--;
			}
			while (getCharUnit(so2) != '/') {
				so2--;
			}
			while (getCharUnit(so2) != '<') {
				so2--;
			}
			length = so2 - so;
			if (encoding <= FORMAT_WIN_1258)
				return ((long) length) << 32 | so;
			else
				return ((long) length) << 33 | (so << 1);
		}
		// for a non-root element with no next sibling
		int temp = getCurrentIndex() + 1;
		int size = vtdBuffer.size;
		// temp is not the last entry in VTD buffer
		if (temp < size) {
			while (temp < size && getTokenDepth(temp) >= depth) {
				temp++;
			}
			if (temp != size) {
				int d =
					depth
						- getTokenDepth(temp)
						+ ((getTokenType(temp) == TOKEN_STARTING_TAG) ? 1 : 0);
				int so2 = getTokenOffset(temp) - 1;
				int i = 0;
				// scan backward
				while (i < d) {
					if (getCharUnit(so2) == '>')
						i++;
					so2--;
				}
				while (getCharUnit(so2) != '/') {
					so2--;
				}
				while (getCharUnit(so2) != '<') {
					so2--;
				}
				length = so2 - so;
				if (encoding <= FORMAT_WIN_1258)
					return ((long) length) << 32 | so;
				else
					return ((long) length) << 33 | (so << 1);
			}
			/*
             * int so2 = getTokenOffset(temp - 1) - 1; int d = depth -
             * getTokenDepth(temp - 1); int i = 0; while (i < d) { if
             * (getCharUnit(so2) == '>') { i++; } so2--; } length = so2 - so +
             * 2; if (encoding < 3) return ((long) length) < < 32 | so; else
             * return ((long) length) < < 33 | (so < < 1);
             */
		}
		// temp is the last entry
		// scan forward search for /> or </cc>
		
		int so2 =
			(encoding <= FORMAT_WIN_1258)
				? (docOffset + docLen - 1)
				: ((docOffset + docLen) >> 1) - 1;
		int d;
	   
	    d = depth + 1;
	    
	    int i = 0;
        while (i < d) {
            if (getCharUnit(so2) == '>') {
                i++;
            }
            so2--;
        }
        while (getCharUnit(so2) != '/') {
			so2--;
		}
		while (getCharUnit(so2) != '<') {
			so2--;
		}

		length = so2 - so;

		if (encoding <= FORMAT_WIN_1258)
			return ((long) length) << 32 | so;
		else
			return ((long) length) << 33 | (so << 1);
	}
	
	/**
	 * This method takes a vtd index, and recover its correspondin
	 * node position, the index can only be of node type element,
	 * document, attribute name, attribute value or character data,
	 * or CDATA
	 * @param index
	 * @throws NavException
	 */
	public void recoverNode(int index) throws NavException{
		if (index <0 || index>=vtdSize )
			throw new NavException("Invalid VTD index");
		
		int type = getTokenType(index);
		if (//type == VTDNav.TOKEN_COMMENT ||
			//	type == VTDNav.TOKEN_PI_NAME ||
				type == VTDNav.TOKEN_PI_VAL ||
				type == VTDNav.TOKEN_DEC_ATTR_NAME ||
				type == VTDNav.TOKEN_DEC_ATTR_VAL ||
				type == VTDNav.TOKEN_ATTR_VAL)
			throw new NavException("Token type not yet supported");
		
		// get depth
		int d = getTokenDepth(index);
		// handle document node;
		switch (d){
		case -1:
			context[0]=-1;
			if (index != 0){
				LN = index;
				atTerminal = true;
				
			}else atTerminal=false;			
			return;
		case 0:
			context[0]=0;
			if (index != rootIndex){
				LN = index;
				atTerminal = true;
				if (type>VTDNav.TOKEN_ATTR_NS)
				 sync(0,index);
			} else
				atTerminal=false;
			return;		
		}
		
		context[0]=d;
		if (type != VTDNav.TOKEN_STARTING_TAG){
			LN = index;
			atTerminal = true;
		}else
			atTerminal = false;
		// search LC level 1
		recoverNode_l1(index);
		
		if (d==1){
			if (atTerminal && type>VTDNav.TOKEN_ATTR_NS)
				 sync(1,index);
			return;
		}
		// search LC level 2
		recoverNode_l2(index);
		if (d==2){
			//resolveLC();
			if (atTerminal && type>VTDNav.TOKEN_ATTR_NS)
				 sync(2,index);
			return;
		}
		// search LC level 3
		recoverNode_l3(index);
		if (d==3){
			//resolveLC();
			if (atTerminal && type>VTDNav.TOKEN_ATTR_NS)
				 sync(3,index);
			return;
		}
		// scan backward
		if ( type == VTDNav.TOKEN_STARTING_TAG ){
			context[d] = index;
		} else{
			int t = index-1;
			while( !(getTokenType(t)==VTDNav.TOKEN_STARTING_TAG && 
					getTokenDepth(t)==d)){
				t--;
			}
			context[d] = t;
		}
		int t = context[d]-1;
		d--;
		while(d>3){
			while( !(getTokenType(t)==VTDNav.TOKEN_STARTING_TAG && 
					getTokenDepth(t)==d)){
				t--;
			}
			context[d] = t;
			d--;
		}
		//resolveLC();		
	}
	
	protected final void recoverNode_l1(int index){
		int i;
		if(context[1]==index){
			
		}
		else if (l1index !=-1 && context[1]>index  
				&& l1index+1<l1Buffer.size
				&& l1Buffer.upper32At(l1index+1)<index){
			
		}
		else {
			i= (index/vtdSize)*l1Buffer.size;
			if (i>=l1Buffer.size)
				i=l1Buffer.size-1;

			if (l1Buffer.upper32At(i)< index) {
				while(i<l1Buffer.size-1 && 
						l1Buffer.upper32At(i)<index){
					i++;
				}
				if (l1Buffer.upper32At(i)>index)
					i--;
			} else {
				while(l1Buffer.upper32At(i)>index){
					i--;
				}
			}	
			context[1] = l1Buffer.upper32At(i);
			l1index = i;
		}
	}
	
	protected final void recoverNode_l2(int index){
		int i = l1Buffer.lower32At(l1index);
		
		if (l2lower != i) {
			l2lower = i;
			// l2lower shouldn't be -1 !!!! l2lower and l2upper always get
			// resolved simultaneously
			//l2index = l2lower;
			l2upper = l2Buffer.size - 1;
			for (int k = l1index + 1; k < l1Buffer.size; k++) {
				i = l1Buffer.lower32At(k);
				if (i != 0xffffffff) {
					l2upper = i - 1;
					break;
				}
			}
		}
		// guess what i would be in l2 cache
		int t1=l2Buffer.upper32At(l2lower);
		int t2=l2Buffer.upper32At(l2upper);
		//System.out.print("   t2  ==>"+t2+"   t1  ==>"+t1);
		i= Math.min(l2lower+ (int)(((float)(index-t1)/(t2-t1+1))*(l2upper-l2lower)),l2upper) ;
		//System.out.print("  i1  "+i);
		while(i<l2Buffer.size-1 && l2Buffer.upper32At(i)<index){
			i++;	
		}
		//System.out.println(" ==== i2    "+i+"    index  ==>  "+index);
		
		while (l2Buffer.upper32At(i)>index && i>0)
			i--;
		context[2] = l2Buffer.upper32At(i);
		l2index = i;
		//System.out.println("l2lower ==>"+l2lower+"  l2upper==>"+l2upper+"   l2index==> "+l2index);
	}
	
	private final void recoverNode_l3(int index){
		int i = l2Buffer.lower32At(l2index);
		
		if (l3lower != i) {
			//l3lower and l3upper are always together
			l3lower = i;
			// l3lower shouldn't be -1
			//l3index = l3lower;
			l3upper = l3Buffer.size - 1;
			for (int k = l2index + 1; k < l2Buffer.size; k++) {
				i = l2Buffer.lower32At(k);
				if (i != 0xffffffff) {
					l3upper = i - 1;
					break;
				}
			}
		}
		int t1=l3Buffer.intAt(l3lower);
		int t2=l3Buffer.intAt(l3upper);
		i= Math.min(l3lower+ (int)(((float)(index-t1)/(t2-t1+1))*(l3upper-l3lower)),l3upper) ;
		while(i<l3Buffer.size-1 && l3Buffer.intAt(i)<index){
			i++;	
		}
		while (l3Buffer.intAt(i)>index && i>0)
			i--;
		//System.out.println(" i ===> "+i);
		context[3] = l3Buffer.intAt(i);
		l3index = i;
	}
	
	/**
	 * Return the prefix of a token as a string if the token 
	 * is of the type of starting tag, attribute name, if the 
	 * the prefix doesn't exist, a null string is returned;
	 * otherwise a null string is returned
	 * 
	 * @param i  VTD index of a token
	 * @return
	 */
	public String getPrefixString(int i) throws NavException{
		if (ns == false)
			return null;
		int type = getTokenType(i);
		if (type != TOKEN_ATTR_NAME && type != TOKEN_STARTING_TAG)
			return null;
		int offset = getTokenOffset(i);
		int preLen = getTokenLength(i) >> 16;
		if (preLen !=0)
			return toRawString(offset,preLen);

		return null;
	}
	
	
	protected void setCurrentNode(){
		if (currentNode == null){
			currentNode = new BookMark(this);
		}else {
			currentNode.recordCursorPosition();
		}
	}
	
	protected void loadCurrentNode(){
		currentNode.setCursorPosition();
	}
	// like toElement, toNode takes an integer that determines the 
	protected boolean toNode(int dir) throws NavException{
		int index,tokenType,depth,lastEntry,tmp;
		//count++;
		//System.out.println("count ==>"+ count);
		switch(dir){
		case ROOT:
			if (context[0] != 0) {
				/*
				 * for (int i = 1; i <= context[0]; i++) { context[i] =
				 * 0xffffffff; }
				 */
				context[0] = 0;
			}
			atTerminal = false;
			//l1index = l2index = l3index = -1;
			return true;
		case PARENT:
			if (atTerminal == true){
				atTerminal = false;
				return true;
			}
			if (context[0] > 0) {
				//context[context[0]] = context[context[0] + 1] =
                // 0xffffffff;
				context[context[0]] = -1;
				context[0]--;
				return true;
			}else if (context[0]==0){
				context[0]=-1; //to be compatible with XPath Data model
				return true;
				}
			else {
				return false;
			}
		case FIRST_CHILD:
			if(atTerminal)return false;
			switch (context[0]) {
			case -1:
				//starting with root element
				//scan backward, if there is a pi | comment node
				index = rootIndex-1;
				loop1:
				while(index >0){
					tokenType = getTokenType(index);
					switch(tokenType){
					case TOKEN_COMMENT: index--; break;
					case TOKEN_PI_VAL:  index-=2;break;
					default:
						break loop1;
					}
				}
				index++; // points to
				if (index!=rootIndex){
					atTerminal = true;
					LN = index;
				}else{
					context[0]=0;
				}
				return true;
			case 0:
				if (l1Buffer.size!=0){
					index = l1Buffer.upper32At(0)-1;
					//rewind
					loop1: while(index>rootIndex){
						tokenType = getTokenType(index);
						switch(tokenType){
						case TOKEN_CHARACTER_DATA:
						case TOKEN_COMMENT:
						case TOKEN_CDATA_VAL:
							index--;
							break;
						case TOKEN_PI_VAL:
							index-=2;
							break;
						default:
							break loop1;
						}
					}
					index++;
					l1index = 0;	
					if(index == l1Buffer.upper32At(0)){
						context[0]=1;
						context[1]= l1Buffer.upper32At(0);
						atTerminal = false;				
					}else {
						atTerminal = true;
						LN = index;						
					}
					return true;
					
				}else{					
					//get to the first non-attr node after the starting tag
					index = rootIndex+1;
					while(index<vtdSize){
						tokenType = getTokenType(index);
						switch(tokenType){
						case TOKEN_ATTR_NAME:
						case TOKEN_ATTR_NS:
							index+=2;
							break;
						default:
							if (getTokenDepth(index)==0){
								atTerminal = true;
								LN = index;
								return true;
							}else
								return false;
								
						}
					}
					return false;
				}
								
			case 1: 
				if (l1Buffer.lower32At(l1index)!=-1){
					// l2upper and l2lower
					l2lower = l1Buffer.lower32At(l1index);
					tmp = l1index+1;
					l2upper = l2Buffer.size-1;
					while(tmp<l1Buffer.size){
						if (l1Buffer.lower32At(tmp)!=-1){
							l2upper = l1Buffer.lower32At(tmp)-1;
							break;
						}else
							tmp++;
					}
					//if (tmp==l1Buffer.size){
					//	l2upper = l2Buffer.size-1;
					//}					
					index = context[1]+1;
					tmp = l2Buffer.upper32At(l2lower);
					while(index<tmp){
						tokenType = getTokenType(index);
						switch(tokenType){
						case TOKEN_ATTR_NAME:
						case TOKEN_ATTR_NS:
							index+=2;
							break;
						default:
							l2index = l2lower;
							atTerminal = true;
							LN = index;
							return true;															
						}
					}
					l2index = l2lower;
					context[0] = 2;
					context[2] = index;
					return true;				
				}else{
					index = context[1]+1;
					while(index<vtdSize){
						tokenType = getTokenType(index);
						switch(tokenType){
						case TOKEN_ATTR_NAME:
						case TOKEN_ATTR_NS:
							index+=2;
							break;
						default:
							if (getTokenDepth(index)==1 && getTokenType(index)!=VTDNav.TOKEN_STARTING_TAG){
								atTerminal = true;
								LN = index;
								return true;
							}else
								return false;
								
						}
					}
					return false;
				}
				
			case 2:
				if (l2Buffer.lower32At(l2index)!=-1){
					// l2upper and l2lower
					l3lower = l2Buffer.lower32At(l2index);
					tmp = l2index+1;
					l3upper = l3Buffer.size-1;
					while(tmp<l2Buffer.size){
						if (l2Buffer.lower32At(tmp)!=-1){
							l3upper = l2Buffer.lower32At(tmp)-1;
							break;
						}else
							tmp++;
					}
					//if (tmp==l2Buffer.size){
					//	l3upper = l3Buffer.size-1;
					//}					
					index = context[2]+1;
					tmp = l3Buffer.intAt(l3lower);
					while(index<tmp){
						tokenType = getTokenType(index);
						switch(tokenType){
						case TOKEN_ATTR_NAME:
						case TOKEN_ATTR_NS:
							index+=2;
							break;
						default:
							l3index = l3lower;
							atTerminal = true;
							LN = index;
							return true;															
						}
					}
					l3index = l3lower;
					context[0] = 3;
					context[3] = index;
					return true;				
				}else{
					index = context[2]+1;
					while(index<vtdSize){
						tokenType = getTokenType(index);
						switch(tokenType){
						case TOKEN_ATTR_NAME:
						case TOKEN_ATTR_NS:
							index+=2;
							break;
						default:
							if (getTokenDepth(index)==2 && getTokenType(index)!=VTDNav.TOKEN_STARTING_TAG){
								atTerminal = true;
								LN = index;
								return true;
							}else
								return false;
								
						}
					}
					return false;
				}				
			default:				
				index = context[context[0]] + 1;
				while (index < vtdBuffer.size) {
					long temp = vtdBuffer.longAt(index);
					tokenType =
						(int) ((MASK_TOKEN_TYPE & temp) >>> 60);
					switch(tokenType){
					case TOKEN_STARTING_TAG:
						depth =
							(int) ((MASK_TOKEN_DEPTH & temp) >> 52);
						if (depth <= context[0]){
							return false;
						}else if (depth == (context[0] + 1)) {
							context[0] += 1;
							context[context[0]] = index;
							return true;
						}else
							throw new NavException("impossible condition");
						
					case TOKEN_ATTR_NAME:
					case TOKEN_ATTR_NS: index+=2;break;
					case TOKEN_CHARACTER_DATA:
					case TOKEN_COMMENT:
					case TOKEN_CDATA_VAL:
						depth =
							(int) ((MASK_TOKEN_DEPTH & temp) >> 52);
						if (depth < context[0]){
							return false;
						}else if (depth == (context[0])) {
							//System.out.println("inside to Node next sibling");
							LN = index;
							atTerminal = true;
							return true;
						} else
							throw new NavException("impossible condition");
					case TOKEN_PI_NAME:
						depth =
							(int) ((MASK_TOKEN_DEPTH & temp) >> 52);
						if (depth < context[0]){
							return false;
						}else if (depth == (context[0])) {
							LN = index;
							atTerminal = true;
							return true;
						} else
							throw new NavException("impossible condition");
 					}
					//index++;
				} // what condition
				return false;
			}
		case LAST_CHILD:
			if(atTerminal)return false;
			return toNode_LastChild();
			
		case NEXT_SIBLING:
			switch (context[0]) {
			case -1:
				if(atTerminal){
					index = LN;
					tokenType = getTokenType(index);
					switch(tokenType){
					case TOKEN_PI_NAME: 
						index+=2;
						break;
						//break loop2;
					case TOKEN_COMMENT:
						index++;
						break;
					}
					
					if (index <vtdSize){
						tokenType = getTokenType(index);
						depth = getTokenDepth(index);
						if (depth == -1){
							LN = index;
							return true;
						}else{
							atTerminal = false;
							context[0]=0;
							return true;
								// depth has to be zero
						}						
					}else
						return false;
					
				}else{
					return false;
				}
				//break;
			case 0:
				if(atTerminal){
					index = LN;
					tokenType = getTokenType(LN);
					if (tokenType==VTDNav.TOKEN_ATTR_NAME)
						return false;
					//index++;
					if (l1Buffer.size!=0){
						if (index < l1Buffer.upper32At(l1index)){
							index++;
							if (tokenType==TOKEN_PI_NAME)
								index++;
							if (index <= l1Buffer.upper32At(l1index)){
								if (index == l1Buffer.upper32At(l1index)){
									atTerminal = false;
									context[0]=1;
									context[1]=index;
									return true;
								}
								depth = getTokenDepth(index);
								if (depth!=0)
									return false;
								LN = index;
								atTerminal = true;
								return true;
							}else{
								return false;
							}
						}else if ( l1index < l1Buffer.size -1){ // whether l1index is the last entry in l1 buffer
							l1index++;
							if (tokenType==TOKEN_PI_NAME)
								index++;
							if (index <= l1Buffer.upper32At(l1index)){
								if (index == l1Buffer.upper32At(l1index)){
									atTerminal = false;
									context[0]=1;
									context[1]=index;
									return true;
								}
								depth = getTokenDepth(index);
								if (depth!=0)
									return false;
								LN = index;
								atTerminal = true;
								return true;
							}else{
								return false;
							}
						}else{
							index++;
							if (tokenType==TOKEN_PI_NAME)
								index++;
							if (index < vtdSize){
								depth = getTokenDepth(index);
								if (depth!=0)
									return false;
								LN = index;
								atTerminal = true;
								return true;
							}else{
								return false;
							}
						}						
					}else{
						index++;
						if (tokenType==TOKEN_PI_NAME)
							index++;
						if (index < vtdSize){
							depth = getTokenDepth(index);
							if (depth!=0)
								return false;
							LN = index;
							atTerminal = true;
							return true;
						}else{
							return false;
						}
					}
					
				}else{
					index = vtdSize-1;
					depth = -2;
					// get to the end, then rewind
					while(index > rootIndex){
						depth = getTokenDepth(index);
						if (depth ==-1){
							index--;
						} else
							break;								
					}			
					index++;
					if (index>=vtdSize )
						return false;
					else{
						context[0]=-1;
						LN = index;
						atTerminal = true;
						return true;
					}
				}
				//break;
			case 1:
				if(atTerminal){
					tokenType=getTokenType(LN);
					if (tokenType==VTDNav.TOKEN_ATTR_NAME)
						return false;
					if (l1Buffer.lower32At(l1index) != -1) {
						if (LN < l2Buffer.upper32At(l2upper)) {
							tmp = l2Buffer.upper32At(l2index);
							index = LN + 1;
							if (tokenType == TOKEN_PI_NAME)
								index++;

							if (index < tmp) {
								LN = index;
								return true;
							} else {
								context[0] = 2;
								context[2] = tmp;
								atTerminal = false;
								return true;
							}
						} else {
							index = LN + 1;
							if (tokenType == TOKEN_PI_NAME)
								index++;
							if (index < vtdSize) {
								depth = getTokenDepth(index);
								if (depth==1 && getTokenType(index)!= TOKEN_STARTING_TAG){
									LN = index;
									atTerminal = true;
									return true;
								}
								return false;
							} else
								return false;
						}						
					}else{
						index= LN+1;
						if (tokenType==TOKEN_PI_NAME)
							index++;
						if (index < vtdSize){
							depth = getTokenDepth(index);
							if (depth==1 && getTokenType(index)!= TOKEN_STARTING_TAG){
								LN = index;
								atTerminal = true;
								return true;
							}
							return false;
						}else{
							return false;
						}
					}					
				}else{
					if (l1index != l1Buffer.size-1){
						// not the last one
						//rewind
						l1index++;
						index = lastEntry = l1Buffer.upper32At(l1index)-1;
						while(getTokenDepth(index)==0){
							index--;
						}
						if (lastEntry==index){
							atTerminal=false;
							context[0]=1;
							context[1]=index+1;
							return true;
						} else {
							atTerminal = true;
							context[0]=0;
							LN = index+1;
							return true;							
						}
					}else{
						index = vtdSize-1;
						while(index > l1Buffer.upper32At(l1index) && getTokenDepth(index)<=0){
							index--;
						}
						
						if (index == vtdSize-1 ){
							if (getTokenDepth(index)==0){
								context[0]=0;
								LN = index;
								atTerminal = true;
								return true;
							}else
								return false;
						}
						index++;
						if (getTokenDepth(index)==0){
							context[0]=0;
							LN = index;
							atTerminal = true;
							return true;
						}else{
							return false;
						}
					}
				}
				
			case 2:
				if(atTerminal){
					tokenType=getTokenType(LN);
					if (tokenType==VTDNav.TOKEN_ATTR_NAME)
						return false;
					if (l2Buffer.lower32At(l2index) != -1) {
						if (LN < l3Buffer.intAt(l3upper)) {
							tmp = l3Buffer.intAt(l3index);
							index = LN + 1;
							if (tokenType == TOKEN_PI_NAME)
								index++;

							if (index < tmp) {
								LN = index;
								return true;
							} else {
								context[0] = 3;
								context[3] = tmp;
								atTerminal = false;
								return true;
							}
						} else {
							index = LN + 1;
							if (tokenType == TOKEN_PI_NAME)
								index++;
							if (index < vtdSize) {
								depth = getTokenDepth(index);
								if (depth==2 && getTokenType(index)!=TOKEN_STARTING_TAG){									
									LN = index;
									return true;
								}
								return false;
							} 
							return false;
						}						
					}else{
						index= LN+1;
						if (tokenType==TOKEN_PI_NAME)
							index++;
						if (index < vtdSize){
							depth = getTokenDepth(index);
							if (depth==2 && getTokenType(index)!= TOKEN_STARTING_TAG){
								LN = index;
								atTerminal = true;
								return true;
							}
							return false;
						}else{
							return false;
						}
					}					
				}else{
					//l2index < l2upper
					if (l2index< l2upper){
						tmp = l2Buffer.upper32At(l2index);
						l2index++;
						lastEntry = index = l2Buffer.upper32At(l2index)-1;
						//rewind
						loop2: while(index>tmp){
							if (getTokenDepth(index)==1){
								tokenType = getTokenType(index);
								switch(tokenType){
								case TOKEN_CHARACTER_DATA:
								case TOKEN_COMMENT:
								case TOKEN_CDATA_VAL:
									index--;
									break;
								case TOKEN_PI_VAL:
									index = index -2;
									break;
								default: break loop2;
								}
							}else
								break loop2;
						}
						if (index == lastEntry){
							context[0]=2;
							context[2] = index+1;
							return true;
						}
						context[0]=1;
						LN = index+1;
						atTerminal = true;
						return true;						
					}else{
						lastEntry = index = vtdSize-1;
						if (l1index!=l1Buffer.size-1){
							lastEntry = index = l1Buffer.upper32At(l1index+1)-1;
						}
						tmp = l2Buffer.upper32At(l2upper);// pointing to last level 2 element
						
						//rewind
						while(index>tmp){
							if (getTokenDepth(index)<2)
								index--;
							else
								break;
						}
						
						if (( /*lastEntry!=index &&*/ getTokenDepth(index)==1)){
							LN = index;
							atTerminal = true;
							context[0]=1;
							return true;
						}
						
						if (/*getTokenDepth(index+1)==1 
								&& getTokenType(index+1)!= TOKEN_STARTING_TAG 
								&&index !=tmp+1*/
							lastEntry!=index && getTokenDepth(index+1)==1){ //index has moved							
							LN = index+1;
							atTerminal = true;
							context[0]=1;
							return true;
						}
						
						return false;
					}
					
				}
				//break;
			case 3:
				if(!atTerminal){
					//l2index < l2upper
					if (l3index< l3upper){
						//System.out.println(l3index+"  "+l3upper+" "+l3lower+" "+l3Buffer.size+" ");
						tmp = l3Buffer.intAt(l3index);
						l3index++;
						//lastEntry = index = vtdSize-1;
						//if (l3index <l3Buffer.size-1){
						lastEntry = index = l3Buffer.intAt(l3index)-1;
						//}
						//rewind
						loop2:while(index>tmp){
							if (getTokenDepth(index)==2){
								tokenType = getTokenType(index);
								switch(tokenType){
								case TOKEN_CHARACTER_DATA:
								case TOKEN_COMMENT:
								case TOKEN_CDATA_VAL:
									index--;
									break;
								case TOKEN_PI_VAL:
									index = index -2;
									break;
								default:
									break loop2;
								}
							}else
								break loop2;
						}
						if (index == lastEntry){
							context[0]=3;
							context[3] = index+1;
							return true;
						}
						context[0]=2;
						LN = index+1;
						atTerminal = true;
						return true;						
					}else{
						lastEntry = index = vtdSize-1;
						
						if (l1index != l1Buffer.size-1){
							lastEntry=index = l1Buffer.upper32At(l1index+1)-1;
						}
						
						if (l2index != l2Buffer.size-1 && l2index != l2upper){
							lastEntry=index = l2Buffer.upper32At(l2index+1)-1;
						}
						// inser here
						tmp = l3Buffer.intAt(l3index);
						
						//rewind
						while(index>tmp){
							if (getTokenDepth(index)<3)
								index--;
							else
								break;
						}
						if ((/*lastEntry==index &&*/ getTokenDepth(index)==2)){
							LN = index;
							atTerminal = true;
							context[0]=2;
							return true;
						}
						
						if (lastEntry!=index && getTokenDepth(index+1)==2){
							LN = index+1;
							atTerminal = true;
							context[0]=2;
							return true;
						}
						
						return false;
					}
					
				}
				//break;
			default:
				if (atTerminal){
					tokenType=getTokenType(LN);
					if (tokenType==VTDNav.TOKEN_ATTR_NAME)
						return false;
					index = LN+1;
					tmp = context[0]+1;
				}
				else{
					index = context[context[0]] + 1;
					tmp = context[0];
				}
				while (index < vtdSize) {
					long temp = vtdBuffer.longAt(index);
					tokenType = (int) ((MASK_TOKEN_TYPE & temp) >>> 60);
					depth = (int) ((MASK_TOKEN_DEPTH & temp) >> 52);
					switch (tokenType) {
					case TOKEN_STARTING_TAG:						
						if (depth < tmp) {
							return false;
						} else if (depth == tmp) {
							context[0]=tmp;
							context[context[0]] = index;
							atTerminal = false;
							return true;
						}else 
							index++;
						break;
					case TOKEN_ATTR_NAME:
					case TOKEN_ATTR_NS:
						index += 2;
						break;
					case TOKEN_CHARACTER_DATA:
					case TOKEN_COMMENT:
					case TOKEN_CDATA_VAL:
						//depth = (int) ((MASK_TOKEN_DEPTH & temp) >> 52);
						if (depth < tmp-1) {
							return false;
						} else if (depth == tmp-1) {
							LN = index;
							context[0]= tmp-1;
							atTerminal = true;
							return true;
						} else
							index++;
						break;
					case TOKEN_PI_NAME:
						//depth = (int) ((MASK_TOKEN_DEPTH & temp) >> 52);
						if (depth < tmp-1) {
							return false;
						} else if (depth == tmp-1) {
							LN = index;
							context[0]= tmp-1;
							atTerminal = true;
							return true;
						} else
							index += 2;
						break;
					default:
						index++;
					}
					
				}
				return false;
			}		
		case PREV_SIBLING:
			return toNode_PrevSibling();
		default :
			throw new NavException("illegal navigation options");
		}	
	}
	
	protected boolean toNode_PrevSibling(){
		int index,tokenType,depth,tmp;
		switch (context[0]) {
		case -1:
			if(atTerminal){
				index = LN-1;
				if (index>0){
					depth = getTokenDepth(index);
					if (depth==-1){
						tokenType = getTokenType(index);
						switch (tokenType) {
						case TOKEN_PI_VAL:
							index--;
						case TOKEN_COMMENT:
							LN = index;
							return true;
						default:
							return false;
						}
					}else{
						context[0] = 0;
						atTerminal = false;
						return true;
					}
				}else{
					return false;
				}
			}else{
				return false;
			}
		
		case 0:
			if(atTerminal){
				if (l1Buffer.size!=0){
					// three cases
					if (LN < l1Buffer.upper32At(l1index)){
						index = LN-1;
						if (index>rootIndex){
							tokenType = getTokenType(index);
							depth = getTokenDepth(index);								
							if (depth == 0){
								switch(tokenType){									
								case TOKEN_CHARACTER_DATA:
								case TOKEN_COMMENT:
								case TOKEN_CDATA_VAL:
									LN = index;
									return true;
								case TOKEN_PI_VAL:
									LN = index -1;
									return true;
								}
							}								
							if (l1index==0)
								return false;
							l1index--;
							atTerminal = false;
							context[0]=1;
							context[1]= l1Buffer.upper32At(l1index);
							return true;
						}else 
							return false;
					} else {
						index = LN -1;
						if (index>l1Buffer.upper32At(l1index)){
							tokenType = getTokenType(index);
							depth = getTokenDepth(index);								
							if (depth == 0){
								switch(tokenType){									
								case TOKEN_CHARACTER_DATA:
								case TOKEN_COMMENT:
								case TOKEN_CDATA_VAL:
									LN = index;
									return true;
								case TOKEN_PI_VAL:
									LN = index -1;
									return true;
								}
							}										
						}
						atTerminal = false;
						context[0]=1;
						context[1]= l1Buffer.upper32At(l1index);
						return true;
					}						
				}else{
					index = LN-1;
					if (index>rootIndex){
						tokenType=getTokenType(index);
						switch (tokenType) {
						case TOKEN_PI_VAL:
							index--;
						case TOKEN_CHARACTER_DATA:
						case TOKEN_COMMENT:
						case TOKEN_CDATA_VAL:
						
							LN = index;
							atTerminal = true;
							context[0]=0;
							return true;
						default:
							return false;
						}
					}
					return false;
				}
				
			}else{
				index = rootIndex-1;
				if (index>0){
					tokenType = getTokenType(index);
					switch (tokenType) {
					case TOKEN_PI_VAL:
						index--;
					case TOKEN_COMMENT:
						LN = index;
						atTerminal = true;
						context[0]=-1;
						return true;
					default:
						return false;
					}
				}else{
					return false;
				}
			}
			//break;
		case 1:
			if(atTerminal){
				if (l1Buffer.lower32At(l1index)!=-1){
					tmp = l2Buffer.upper32At(l2index);
					if (LN > tmp){
						index = LN-1;
						if (getTokenType(index)==TOKEN_PI_VAL){
							index--;
						}
						if (getTokenDepth(index)==1){
							LN = index;
							return true;
						}else{
							atTerminal = false;
							context[0]=2;
							context[2]=tmp;
							return true;
						}
					} else if (l2index!=l2lower){
						l2index--;
						atTerminal = false;
						context[0]=2;
						context[2]=l2Buffer.upper32At(l2index);
						return true;
					} else {
						index = LN-1;
						tokenType = getTokenType(index);
						switch (tokenType) {
						case TOKEN_PI_VAL:
							index--;
						case TOKEN_CHARACTER_DATA:
						case TOKEN_COMMENT:
						case TOKEN_CDATA_VAL:
						
							LN = index;
							atTerminal = true;
							context[0]=1;
							return true;
						default:
							return false;
						}
					}
				}else{
					index= LN-1;
					if (getTokenType(index)==TOKEN_PI_VAL)
						index--;
					if (index > context[1]){
						tokenType = getTokenType(index);
						if (tokenType!= VTDNav.TOKEN_ATTR_VAL){
							LN = index;
							atTerminal = true;
							return true;
						}else
							return false;
					}else{
						return false;
					}
				}					
			}else{
				index = context[1]-1;	
				tokenType = getTokenType(index);
				if (getTokenDepth(index)==0
						&& tokenType!= TOKEN_ATTR_VAL
						&& tokenType!= TOKEN_STARTING_TAG){
					if (tokenType==TOKEN_PI_VAL)
						index--;
					context[0]=0;
					atTerminal = true;
					LN = index;
					return true;
				}else{
					// no more prev sibling element
					if (l1index != 0){
						l1index--;
						context[1] = l1Buffer.upper32At(l1index);
						return true;
					}else
						return false;
				}													
			}
			//break;
		case 2:
			if(atTerminal){
				if (l2Buffer.lower32At(l2index)!=-1){
					tmp = l3Buffer.intAt(l3index);
					if (LN > tmp){
						index = LN-1;
						if (getTokenType(index)==TOKEN_PI_VAL){
							index--;
						}
						if (getTokenDepth(index)==2){
							LN = index;
							return true;
						}else{
							atTerminal = false;
							context[0]=3;
							context[3]=tmp;
							return true;
						}
					} else if (l3index!=l3lower){
						l3index--;
						atTerminal = false;
						context[0]=3;
						context[3]=l3Buffer.intAt(l3index);
						return true;
					} else {
						index = LN-1;
						tokenType = getTokenType(index);
						switch (tokenType) {
						case TOKEN_PI_VAL:
							index--;
						case TOKEN_CHARACTER_DATA:
						case TOKEN_COMMENT:
						case TOKEN_CDATA_VAL:
						
							LN = index;
							atTerminal = true;
							context[0]=2;
							return true;
						default:
							return false;
						}
					}
				}else{
					index= LN-1;
					if (getTokenType(index)==TOKEN_PI_VAL)
						index--;
					if (index > context[2]){
						tokenType = getTokenType(index);
						if (tokenType!= VTDNav.TOKEN_ATTR_VAL){
							LN = index;
							atTerminal = true;
							return true;
						}else
							return false;
					}else{
						return false;
					}
				}	
			}else{
				index = context[2]-1;	
				tokenType = getTokenType(index);
				if (getTokenDepth(index)==1
						&& tokenType!= TOKEN_ATTR_VAL
						&& tokenType!= TOKEN_STARTING_TAG){
					if (tokenType==TOKEN_PI_VAL)
						index--;
					context[0]=1;
					atTerminal = true;
					LN = index;
					return true;
				}else{
					// no more prev sibling element
					if (l2index != l2lower){
						l2index--;
						context[2] = l2Buffer.upper32At(l2index);
						return true;
					}else
						return false;
				}		
			}				
			//break;
		case 3:
			if(!atTerminal){
				index = context[3]-1;	
				tokenType = getTokenType(index);
				if (getTokenDepth(index)==2
						&& tokenType!= TOKEN_ATTR_VAL
						&& tokenType!= TOKEN_STARTING_TAG){
					if (tokenType==TOKEN_PI_VAL)
						index--;
					context[0]=2;
					atTerminal = true;
					LN = index;
					return true;
				}else{
					// no more prev sibling element
					if (l3index != l3lower){
						l3index--;
						context[3] = l3Buffer.intAt(l3index);
						return true;
					}else
						return false;
				}		
			}
			//break;
		default:
			if (atTerminal){
				index = LN-1;
				tmp = context[0]+1;
			}
			else{
				index = context[context[0]] - 1;
				tmp = context[0];
			}
			while (index > context[tmp-1]) {
				long temp = vtdBuffer.longAt(index);
				tokenType = (int) ((MASK_TOKEN_TYPE & temp) >>> 60);
				depth = (int) ((MASK_TOKEN_DEPTH & temp) >> 52);
				switch (tokenType) {
				case TOKEN_STARTING_TAG:
					
					/*if (depth < tmp) {
						return false;
					} else*/ if (depth == tmp) {
						context[0] = depth;
						context[context[0]] = index;
						atTerminal = false;
						return true;
					}else 
						index--;
					break;
				case TOKEN_ATTR_VAL:
				//case TOKEN_ATTR_NS:
					index -= 2;
					break;
				case TOKEN_CHARACTER_DATA:
				case TOKEN_COMMENT:
				case TOKEN_CDATA_VAL:
					//depth = (int) ((MASK_TOKEN_DEPTH & temp) >> 52);
					/*if (depth < tmp-1) {
						return false;
					} else*/ if (depth == tmp-1) {
						context[0] = tmp-1;
						LN = index;
						atTerminal = true;
						return true;
					} else
						index--;
					break;
				case TOKEN_PI_VAL:
					//depth = (int) ((MASK_TOKEN_DEPTH & temp) >> 52);
					/*if (depth < context[0]) {
						return false;
					} else*/
					if (depth == (tmp-1)) {
						context[0] = tmp-1;
						LN = index-1;
						atTerminal = true;
						return true;
					} else
						index -= 2;
					break;
				default:
					index--;
				}
				
			}
			return false;
		}		
	}
	
	protected boolean toNode_LastChild(){
		int depth,index,tokenType,lastEntry,tmp;
		switch (context[0]) {
		case -1:
			index = vtdSize-1;
			tokenType = getTokenType(index);
			depth = getTokenDepth(index);
			if (depth == -1) {
				switch (tokenType) {
					case TOKEN_COMMENT:
						LN = index;
						atTerminal = true;
						return true;							
					case TOKEN_PI_VAL:
						LN = index -1;
						atTerminal = true;
						return true;													
				}	
			}
			context[0]=0;
			return true;
			
		case 0:
			if (l1Buffer.size!=0){
				lastEntry = l1Buffer.upper32At(l1Buffer.size-1);
				index = vtdSize-1;
				while(index > lastEntry){
					depth = getTokenDepth(index);
					if (depth==-1){
						index--;
						continue;
					} else if (depth ==0){
						tokenType = getTokenType(index);
						switch(tokenType){
						case TOKEN_CHARACTER_DATA:
						case TOKEN_COMMENT:
						case TOKEN_CDATA_VAL:
							LN = index;
							atTerminal = true;
							l1index = l1Buffer.size -1;
							return true;
						case TOKEN_PI_VAL:
							LN = index -1;
							atTerminal = true;
							l1index = l1Buffer.size -1;
							return true;
						default:
							return false;
						} 	
					}else {
						l1index = l1Buffer.size -1;
						context[0]= 1;
						context[1]= lastEntry;
						return true;
					}
				}
				l1index = l1Buffer.size -1;
				context[0]= 1;
				context[1]= lastEntry;
				return true;
			}else{
				index = vtdSize - 1;
				while(index>rootIndex){
					depth = getTokenDepth(index);
					if (depth == -1){
						index--;
						continue;
					}
					tokenType = getTokenType(index);
					switch(tokenType){
					case TOKEN_CHARACTER_DATA:
					case TOKEN_COMMENT:
					case TOKEN_CDATA_VAL:
						LN = index;
						atTerminal = true;
						return true;
					case TOKEN_PI_VAL:
						LN = index-1;
						atTerminal = true;
						return true;
					default:
						return false;
					}
				}
				return false;
			}
			
		case 1:
			if (l1Buffer.lower32At(l1index)!=-1){
				l2lower = l1Buffer.lower32At(l1index);
				tmp = l1index+1;
				while(tmp<l1Buffer.size){
					if (l1Buffer.lower32At(tmp)!=-1){
						l2upper = l1Buffer.lower32At(tmp)-1;
						break;
					}else
						tmp++;
				}
				if (tmp==l1Buffer.size){
					l2upper = l2Buffer.size-1;
				}					
				l2index = l2upper;
				index =vtdSize-1;
				if (l1index != l1Buffer.size-1){
					index = l1Buffer.upper32At(l1index+1)-1;
				}
				tmp = l2Buffer.upper32At(l2index);
				// rewind and find the first node of depth 1
				while(index > tmp){
					depth = getTokenDepth(index);
					if (depth <1)
						index--;
					else if (depth == 1){
						tokenType = getTokenType(index);
						if (tokenType == TOKEN_PI_VAL)
							LN = index-1;
						else
							LN = index;
						atTerminal = true;
						//context[0]=1;
						return true;
					}else
						break;							
				}
				context[0]=2;
				context[2]=tmp;
				return true;
				
				
			}else{
				index = context[1]+1;
				loop: while(index<vtdSize){
					tokenType = getTokenType(index);
					switch(tokenType){
					case TOKEN_ATTR_NAME:
					case TOKEN_ATTR_NS:
						index+=2;
						break;
					default: break loop;
					}
				}
									
				if (index< vtdSize && getTokenDepth(index)==1 && getTokenType(index)!=VTDNav.TOKEN_STARTING_TAG){
					lastEntry = index;
					index++;
					//scan forward
					loop2:while(index<vtdSize){
						tokenType = getTokenType(index);
						depth = getTokenDepth(index);
						if (depth == 1){
							switch(tokenType){
							case TOKEN_CHARACTER_DATA:
							case TOKEN_COMMENT:
							case TOKEN_CDATA_VAL:
								lastEntry = index;
								index++;
								break;
							case TOKEN_PI_NAME:
								lastEntry = index;
								index+=2;
								break;
							default:
								break loop2;
							}
						}else
							break loop2;
					}
					LN = lastEntry;
					atTerminal = true;
					return true;
				}else
					return false;					
			}
			
		case 2:		
			if (l2Buffer.lower32At(l2index)!=-1){
				l3lower = l2Buffer.lower32At(l2index);
				tmp = l2index+1;
				while(tmp<l2Buffer.size){
					if (l2Buffer.lower32At(tmp)!=-1){
						l3upper = l2Buffer.lower32At(tmp)-1;
						break;
					}else
						tmp++;
				}
				if (tmp==l2Buffer.size){
					l3upper = l3Buffer.size-1;
				}					
				l3index = l3upper;
				index =vtdSize-1;
				
				if (l1index != l1Buffer.size-1){
					index = l1Buffer.upper32At(l1index+1)-1;
				}
				
				if (l2index != l2Buffer.size-1 && l2index != l2upper){
					index = l2Buffer.upper32At(l2index+1)-1;
				}
				tmp = l3Buffer.intAt(l3index);
				// rewind and find the first node of depth 1
				while(index > tmp){
					depth = getTokenDepth(index);
					if (depth <2)
						index--;
					else if (depth == 2){
						tokenType = getTokenType(index);
						if (tokenType == TOKEN_PI_VAL)
							LN = index-1;
						else
							LN = index;
						atTerminal = true;
						//context[0]=1;
						return true;
					}else
						break;							
				}
				context[0]=3;
				context[3]=tmp;
				return true;					
			}else{
				index = context[2]+1;
				loop: while(index<vtdSize){
					tokenType = getTokenType(index);
					switch(tokenType){
					case TOKEN_ATTR_NAME:
					case TOKEN_ATTR_NS:
						index+=2;
						break;
					default: break loop;
					}
				}
									
				if (index< vtdSize && getTokenDepth(index)==2 && getTokenType(index)!=VTDNav.TOKEN_STARTING_TAG){
					lastEntry = index;
					index++;
					//scan forward
					loop2:while(index<vtdSize){
						tokenType = getTokenType(index);
						depth = getTokenDepth(index);
						if (depth == 2){
							switch(tokenType){
							case TOKEN_CHARACTER_DATA:
							case TOKEN_COMMENT:
							case TOKEN_CDATA_VAL:
								lastEntry = index;
								index++;
								break;
							case TOKEN_PI_NAME:
								lastEntry = index;
								index+=2;
								break;
							default:
								break loop2;
							}
						}else
							break loop2;
					}
					LN = lastEntry;
					atTerminal = true;
					return true;
				}else
					return false;					
			}				
		default:
			index = context[context[0]] + 1;
			lastEntry  = -1; atTerminal = false;
			while (index < vtdBuffer.size) {
				long temp = vtdBuffer.longAt(index);
				tokenType =
					(int) ((MASK_TOKEN_TYPE & temp) >>> 60);
				depth =getTokenDepth(index);
				switch(tokenType){
				case TOKEN_STARTING_TAG:
					if (depth <= context[0]){
						if (lastEntry !=-1){
							if (atTerminal){
								LN = lastEntry;									
							}else{
								context[0]+=1;
								context[context[0]] = lastEntry;
								atTerminal = false;
							}
							return true;									
						} else
							return false;
					}else if (depth == (context[0] + 1)) {
						lastEntry = index;
						atTerminal = false;
					}
					index++;
					break;
				case TOKEN_ATTR_NAME:
				case TOKEN_ATTR_NS: index+=2;break;
				case TOKEN_CHARACTER_DATA:
				case TOKEN_COMMENT:
				case TOKEN_CDATA_VAL:						
					if (depth < context[0]){
						if (lastEntry !=-1){
							if (atTerminal){
								LN = lastEntry;
							}
							else{
								context[0]++;
								context[context[0]]=lastEntry;									
							}
							return true;
						}else
							return false;
					}else if (depth == (context[0])) {
						lastEntry = index;
						atTerminal = true;
					}
					index++;
					break;
				case TOKEN_PI_NAME:
					if (depth < context[0]){
						if (lastEntry !=-1){
							if (atTerminal){
								LN = lastEntry;
							}
							else{
								context[0]++;
								context[context[0]]=lastEntry;									
							}
							return true;
						}else
							return false;
					}else if (depth == (context[0])) {
						lastEntry = index;
						atTerminal = true;
					}
					index+=2;
					break;
					}
				//index++;
			} // what condition
			if (lastEntry !=-1){
				if (atTerminal){
					LN = lastEntry;
				}
				else{
					context[0]++;
					context[context[0]]=lastEntry;									
				}
				return true;
			}else
				return false;
		}
	}

	final public String toNormalizedXPathString(int j) throws NavException{
		// TODO Auto-generated method stub
		int tokenType;
		int index = j + 1;
		int depth, t=0;
		int dp = getTokenDepth(j);
		boolean r = false;//default
		//int size = vtdBuffer.size;
		// store all text tokens underneath the current element node
		while (index < vtdSize) {
		    tokenType = getTokenType(index);
		    depth = getTokenDepth(index);
		    
		    if (depth<dp || 
		    		(depth==dp && tokenType==VTDNav.TOKEN_STARTING_TAG)){
		    	break;
		    }
		    
		    if (tokenType==VTDNav.TOKEN_CHARACTER_DATA || tokenType==VTDNav.TOKEN_CDATA_VAL){
		    	//if (!match)
		    	t=t+getTokenLength2(index);
		    	fib.append(index);
		    	index++;
		    	continue;
		    } else if (tokenType==VTDNav.TOKEN_ATTR_NAME
			        || tokenType == VTDNav.TOKEN_ATTR_NS
			        || tokenType == VTDNav.TOKEN_PI_NAME){			  
			    index = index+2;
			    continue;
			}			
			index++;
		}
		//allocate string buffer
		StringBuilder sb = new StringBuilder(t);
		//now create teh string, leading zeros/trailling zero stripped, multiple ws collapse into one
		index =0;
		int state=0;// start
		while(index<fib.size){
			int offset= getTokenOffset(fib.intAt(index));
			int len = getTokenLength2(fib.intAt(index));
			int endOS= offset + len;
			int c;
			long l;
			
			int type = getTokenType(fib.intAt(index));
			if (type==VTDNav.TOKEN_CHARACTER_DATA){
				while(offset < endOS){
					l = getCharResolved(offset);
					c = (int)l;
					offset += (int)(l>>32);
					
					switch(state){
					case 0:
						if(isWS(c)){
							
						}else{
							sb.append((char)c);
							state =1;
						}
						break;
						
					case 1:
						if(isWS(c)){
							sb.append((char)' ');
							state =2;
						}else{
							sb.append((char)c);
						}
						break;
						
					case 2:
						if (isWS(c)){
							
						}else{
							sb.append((char)c);
							state = 1;
						}
						break;
					
					}
				}
			}else{
				while(offset < endOS){
					l = getChar(offset);
					c = (int)l;
					offset += (int)(l>>32);
					
					switch(state){
					case 0:
						if(isWS(c)){
							
						}else{
							sb.append((char)c);
							state =1;
						}
						break;
					case 1:
						if(isWS(c)){
							sb.append((char)' ');
							state =2;
						}else{
							sb.append((char)c);
						}
						break;
					
					case 2: 
						if (isWS(c)){
							
						}else{
							sb.append((char)c);
							state = 1;
						}
						break;
					
					}
				}
			}			
			
			index++;
			//System.out.println(sb.toString());
		}
		fib.clear();
		//String s =sb.toString();
		//System.out.println();
		return sb.toString();
		
	}
}
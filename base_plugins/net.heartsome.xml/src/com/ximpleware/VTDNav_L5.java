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

package com.ximpleware;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class VTDNav_L5 extends VTDNav {
	
	protected int l4index;
	protected int l5index;
	protected int l4upper;
	protected int l4lower;
	protected int l5upper;
	protected int l5lower;
	
	protected FastLongBuffer l3Buffer;
	protected FastLongBuffer l4Buffer;
	protected FastIntBuffer l5Buffer;
	
	//protected static short maxLCDepth =5;
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
	protected VTDNav_L5(
		int RootIndex,
		int enc,
		boolean NS,
		int depth,
		IByteBuffer x,
		FastLongBuffer vtd,
		FastLongBuffer l1,
		FastLongBuffer l2,
		FastLongBuffer l3,
		FastLongBuffer l4,
		FastIntBuffer l5,
		int so, // start offset of the starting offset(in byte)
		int length) // lengnth of the XML document (in byte))
	{
		//super();
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
		l4Buffer = l4;
		l5Buffer = l5;
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
		contextStack = new ContextBuffer(10, nestingLevel + 15);
		contextStack2 = new ContextBuffer(10, nestingLevel + 15);
		stackTemp = new int[nestingLevel + 15];

		// initial state of LC variables
		l1index = l2index = l3index = l4index = l5index= -1;
		l2lower = l3lower = l4lower = l5lower= -1;
		l2upper = l3upper = l4upper = l5upper= -1;
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
		shallowDepth = false;
		maxLCDepthPlusOne =6;
		
	}
	
	/**
	 * Clone the VTDNav instance to get with shared XML, VTD and LC buffers
	 * The node position is also copied from the original instance
	 * @return a new instance of VTDNav
	 */
	public VTDNav cloneNav(){
		VTDNav_L5 vn = new VTDNav_L5(rootIndex,
	            encoding,
	            ns,
	            nestingLevel-1,
	            XMLDoc,
	            vtdBuffer,
	            l1Buffer,
	            l2Buffer,
	            l3Buffer,
	            l4Buffer,
	            l5Buffer,
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
		if (getCurrentDepth() > 3) {
			vn.l4lower = l4lower;
			vn.l4index = l4index;
			vn.l4upper = l4upper;
		}
		if (getCurrentDepth() > 4) {
			vn.l5lower = l5lower;
			vn.l5index = l5index;
			vn.l5upper = l5upper;
		}
		return vn;
	}
	
	/**
	 * Duplicate the VTDNav instance with shared XML, VTD and LC buffers
	 * This method may be useful for parallel XPath evaluation
	 * The node Position is at root element
	 * @return a VTDNav instance
	 *
	 */
	final public VTDNav duplicateNav(){
	    return new VTDNav_L5(rootIndex,
	            encoding,
	            ns,
	            nestingLevel-1,
	            XMLDoc,
	            vtdBuffer,
	            l1Buffer,
	            l2Buffer,
	            l3Buffer,
	            l4Buffer,
	            l5Buffer,
	            docOffset,
	            docLen
	            );
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
	final protected boolean iterateNS(int dp, String URL, String ln)
		throws NavException {
		if (ns == false)
			return false;
		int tokenType;
		int index = getCurrentIndex() + 1;
		while (index < vtdSize) {
		    tokenType = getTokenType(index);
			if(tokenType==VTDNav.TOKEN_ATTR_NAME
			        || tokenType == VTDNav.TOKEN_ATTR_NS){
			    index = index+2;
			    continue;
			}
			if (isElementOrDocument(index)) {
				int depth = getTokenDepth(index);
				if (depth > dp) {
					context[0] = depth;
					if (depth>0)
						context[depth] = index;
					if (matchElementNS(URL, ln)) {
						if (dp < 6)
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
		if (context[0] == 3)
			return;	
		resolveLC_l4();
		if (context[0] == 4)
			return;	
		resolveLC_l5();
	}
	
	/**
	 * Sync L3 location Cache
	 */
	protected void resolveLC_l3(){
		int temp = l2Buffer.lower32At(l2index);
		if (l3lower != temp) {
			l3lower = temp;
			// l2lower shouldn't be -1 !!!! l2lower and l2upper always get
			// resolved simultaneously
			l3index = l3lower;
			l3upper = l3Buffer.size - 1;
			for (int i = l2index + 1; i < l2Buffer.size; i++) {
				temp = l2Buffer.lower32At(i);
				if (temp != 0xffffffff) {
					l3upper = temp - 1;
					break;
				}
			}
		} // intelligent guess again ??

		if (l3index < 0 || l3index >= l3Buffer.size
				|| context[3] != l3Buffer.upper32At(l3index)) {
			
			if (l3index >= l3Buffer.size || l3index<0)
				l3index = l3lower;
			if (l3index+1< l3Buffer.size&& context[3] == l3Buffer.upper32At(l3index + 1))
				l3index = l3index + 1;
			else if (l3upper - l3lower >= 16) {
				int init_guess = l3lower
						+ (int) ((l3upper - l3lower)
								* ((float) context[3] - l3Buffer
										.upper32At(l3lower)) / (l3Buffer
								.upper32At(l3upper) - l3Buffer
								.upper32At(l3lower)));
				if (l3Buffer.upper32At(init_guess) > context[3]) {
					while (context[3] != l3Buffer.upper32At(init_guess))
						init_guess--;
				} else if (l3Buffer.upper32At(init_guess) < context[3]) {
					while (context[3] != l3Buffer.upper32At(init_guess))
						init_guess++;
				}
				l3index = init_guess;
			} else if (context[3]<l3Buffer.upper32At(l3index)){
				
				while ( context[3] != l3Buffer.upper32At(l3index)) {
					l3index--;
				}
			}
			else { 
				while(context[3]!=l3Buffer.upper32At(l3index))
					l3index++;
			}	
		}
	}
	
	private void resolveLC_l4(){
		int temp = l3Buffer.lower32At(l3index);
		if (l4lower != temp) {
			l4lower = temp;
			// l2lower shouldn't be -1 !!!! l2lower and l2upper always get
			// resolved simultaneously
			l4index = l4lower;
			l4upper = l4Buffer.size - 1;
			for (int i = l3index + 1; i < l3Buffer.size; i++) {
				temp = l3Buffer.lower32At(i);
				if (temp != 0xffffffff) {
					l4upper = temp - 1;
					break;
				}
			}
		} // intelligent guess again ??

		if (l4index < 0 || l4index >= l4Buffer.size
				|| context[4] != l4Buffer.upper32At(l4index)) {
			
			if (l4index >= l4Buffer.size || l4index<0)
				l4index = l4lower;
			if (l4index+1< l4Buffer.size&& context[4] == l4Buffer.upper32At(l4index + 1))
				l4index = l4index + 1;
			else if (l4upper - l4lower >= 16) {
				int init_guess = l4lower
						+ (int) ((l4upper - l4lower)
								* ((float) context[4] - l4Buffer
										.upper32At(l4lower)) / (l4Buffer
								.upper32At(l4upper) - l4Buffer
								.upper32At(l4lower)));
				if (l4Buffer.upper32At(init_guess) > context[4]) {
					while (context[4] != l4Buffer.upper32At(init_guess))
						init_guess--;
				} else if (l4Buffer.upper32At(init_guess) < context[4]) {
					while (context[4] != l4Buffer.upper32At(init_guess))
						init_guess++;
				}
				l4index = init_guess;
			} else if (context[4]<l4Buffer.upper32At(l4index)){
				
				while ( context[4] != l4Buffer.upper32At(l4index)) {
					l4index--;
				}
			}
			else { 
				while(context[4]!=l4Buffer.upper32At(l4index))
					l4index++;
			}	
		}
	}
	/**
	 * Sync L3 location Cache
	 */
	private void resolveLC_l5(){
		int temp = l4Buffer.lower32At(l4index);
		if (l5lower != temp) {
			//l3lower and l3upper are always together
			l5lower = temp;
			// l3lower shouldn't be -1
			l5index = l5lower;
			l5upper = l5Buffer.size - 1;
			for (int i = l4index + 1; i < l4Buffer.size; i++) {
				temp = l4Buffer.lower32At(i);
				if (temp != 0xffffffff) {
					l5upper = temp - 1;
					break;
				}
			}
		}

		if (l5index < 0 || l5index >= l5Buffer.size
				|| context[5] != l5Buffer.intAt(l5index)) {
			if (l5index >= l5Buffer.size || l5index <0)
				l5index = l5lower;
			if (l5index+1 < l5Buffer.size &&
					context[5] == l5Buffer.intAt(l5index + 1))
				l5index = l5index + 1;
			else if (l5upper - l5lower >= 16) {
				int init_guess = l5lower
						+ (int) ((l5upper - l5lower) * ((float) (context[5] - l5Buffer
								.intAt(l5lower)) / (l5Buffer.intAt(l5upper) - l5Buffer
								.intAt(l5lower))));
				if (l5Buffer.intAt(init_guess) > context[5]) {
					while (context[5] != l5Buffer.intAt(init_guess))
						init_guess--;
				} else if (l5Buffer.intAt(init_guess) < context[5]) {
					while (context[5] != l5Buffer.intAt(init_guess))
						init_guess++;
				}
				l5index = init_guess;
			} else if (context[5]<l5Buffer.intAt(l5index)){
				while (context[5] != l5Buffer.intAt(l5index)) {
					l5index--;
				}
			} else {
				while (context[5] != l5Buffer.intAt(l5index)) {
					l5index++;
				}
			}
		}
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
		l4index = stackTemp[nestingLevel + 3];
		l5index = stackTemp[nestingLevel + 4];
		l2lower = stackTemp[nestingLevel + 5];
		l2upper = stackTemp[nestingLevel + 6];
		l3lower = stackTemp[nestingLevel + 7];
		l3upper = stackTemp[nestingLevel + 8];
		l4lower = stackTemp[nestingLevel + 9];
		l4upper = stackTemp[nestingLevel + 10];
		l5lower = stackTemp[nestingLevel + 11];
		l5upper = stackTemp[nestingLevel + 12];
		atTerminal = (stackTemp[nestingLevel + 13] == 1);
		LN = stackTemp[nestingLevel+14];
		return true;
	}
	
	/**
     * Load the context info from contextStack2. This method is dedicated for
     * XPath evaluation.
     * 
     * @return status of pop2
     */
	
	
	final protected boolean pop2(){

		boolean b = contextStack2.load(stackTemp);
		if (b == false)
			return false;
		for (int i = 0; i < nestingLevel; i++) {
			context[i] = stackTemp[i];
		}
		l1index = stackTemp[nestingLevel];
		l2index = stackTemp[nestingLevel + 1];
		l3index = stackTemp[nestingLevel + 2];
		l4index = stackTemp[nestingLevel + 3];
		l5index = stackTemp[nestingLevel + 4];
		l2lower = stackTemp[nestingLevel + 5];
		l2upper = stackTemp[nestingLevel + 6];
		l3lower = stackTemp[nestingLevel + 7];
		l3upper = stackTemp[nestingLevel + 8];
		l4lower = stackTemp[nestingLevel + 9];
		l4upper = stackTemp[nestingLevel + 10];
		l5lower = stackTemp[nestingLevel + 11];
		l5upper = stackTemp[nestingLevel + 12];
		atTerminal = (stackTemp[nestingLevel + 13] == 1);
		LN = stackTemp[nestingLevel+14];
		return true;
	}
	
	/**
     * Store the context info into the ContextBuffer. Info saved including LC
     * and current state of the context Creation date: (11/16/03 7:00:27 PM)
     */
	final public void push() {
		
		for (int i = 0; i < nestingLevel; i++) {
			stackTemp[i] = context[i];
		}
		stackTemp[nestingLevel] = l1index;
		stackTemp[nestingLevel + 1] = l2index;
		stackTemp[nestingLevel + 2] = l3index;
		stackTemp[nestingLevel + 3] = l4index;
		stackTemp[nestingLevel + 4] = l5index;
		stackTemp[nestingLevel + 5] = l2lower;
		stackTemp[nestingLevel + 6] = l2upper;
		stackTemp[nestingLevel + 7] = l3lower;
		stackTemp[nestingLevel + 8] = l3upper;
		stackTemp[nestingLevel + 9] = l4lower;
		stackTemp[nestingLevel + 10] = l4upper;
		stackTemp[nestingLevel + 11] = l5lower;
		stackTemp[nestingLevel + 12] = l5upper;
		
		if (atTerminal)
			stackTemp[nestingLevel + 13] =1;
		else
			stackTemp[nestingLevel + 13] =0;
		stackTemp[nestingLevel+14] = LN; 
		contextStack.store(stackTemp);
	}
	
	/**
     * Store the context info into the contextStack2. This method is reserved
     * for XPath Evaluation
     *  
     */
	
	final protected void push2() {
		
		for (int i = 0; i < nestingLevel; i++) {
			stackTemp[i] = context[i];
		}
		stackTemp[nestingLevel] = l1index;
		stackTemp[nestingLevel + 1] = l2index;
		stackTemp[nestingLevel + 2] = l3index;
		stackTemp[nestingLevel + 3] = l4index;
		stackTemp[nestingLevel + 4] = l5index;
		stackTemp[nestingLevel + 5] = l2lower;
		stackTemp[nestingLevel + 6] = l2upper;
		stackTemp[nestingLevel + 7] = l3lower;
		stackTemp[nestingLevel + 8] = l3upper;
		stackTemp[nestingLevel + 9] = l4lower;
		stackTemp[nestingLevel + 10] = l4upper;
		stackTemp[nestingLevel + 11] = l5lower;
		stackTemp[nestingLevel + 12] = l5upper;
		
		if (atTerminal)
			stackTemp[nestingLevel + 13] =1;
		else
			stackTemp[nestingLevel + 13] =0;
		stackTemp[nestingLevel+14] = LN; 
		contextStack2.store(stackTemp);
	}
	
	
	private final void recoverNode_l3(int index){
		int i = l2Buffer.lower32At(l2index);
		
		if (l3lower != i) {
			l3lower = i;
			// l2lower shouldn't be -1 !!!! l2lower and l2upper always get
			// resolved simultaneously
			//l2index = l2lower;
			l3upper = l3Buffer.size - 1;
			for (int k = l2index + 1; k < l2Buffer.size; k++) {
				i = l2Buffer.lower32At(k);
				if (i != 0xffffffff) {
					l3upper = i - 1;
					break;
				}
			}
		}
		// guess what i would be in l2 cache
		int t1=l3Buffer.upper32At(l3lower);
		int t2=l3Buffer.upper32At(l3upper);
		//System.out.print("   t2  ==>"+t2+"   t1  ==>"+t1);
		i= Math.min(l3lower+ (int)(((float)(index-t1)/(t2-t1+1))*(l3upper-l3lower)),l3upper) ;
		//System.out.print("  i1  "+i);
		while(i<l3Buffer.size-1 && l3Buffer.upper32At(i)<index){
			i++;	
		}
		//System.out.println(" ==== i2    "+i+"    index  ==>  "+index);
		
		while (l3Buffer.upper32At(i)>index && i>0)
			i--;
		context[3] = l3Buffer.upper32At(i);
		l3index = i;
		//System.out.println("l2lower ==>"+l2lower+"  l2upper==>"+l2upper+"   l2index==> "+l2index);
	}
	
	private final void recoverNode_l4(int index){
		int i = l3Buffer.lower32At(l3index);
		
		if (l4lower != i) {
			l4lower = i;
			// l2lower shouldn't be -1 !!!! l2lower and l2upper always get
			// resolved simultaneously
			//l2index = l2lower;
			l4upper = l4Buffer.size - 1;
			for (int k = l3index + 1; k < l3Buffer.size; k++) {
				i = l3Buffer.lower32At(k);
				if (i != 0xffffffff) {
					l4upper = i - 1;
					break;
				}
			}
		}
		// guess what i would be in l2 cache
		int t1=l4Buffer.upper32At(l4lower);
		int t2=l4Buffer.upper32At(l4upper);
		//System.out.print("   t2  ==>"+t2+"   t1  ==>"+t1);
		i= Math.min(l4lower+ (int)(((float)(index-t1)/(t2-t1+1))*(l4upper-l4lower)),l4upper) ;
		//System.out.print("  i1  "+i);
		while(i<l4Buffer.size-1 && l4Buffer.upper32At(i)<index){
			i++;	
		}
		//System.out.println(" ==== i2    "+i+"    index  ==>  "+index);
		
		while (l4Buffer.upper32At(i)>index && i>0)
			i--;
		context[4] = l4Buffer.upper32At(i);
		l4index = i;
		//System.out.println("l2lower ==>"+l2lower+"  l2upper==>"+l2upper+"   l2index==> "+l2index);
	}
	
	private final void recoverNode_l5(int index){ //l3
		int i = l4Buffer.lower32At(l4index);
		
		if (l5lower != i) {
			//l3lower and l3upper are always together
			l5lower = i;
			// l3lower shouldn't be -1
			//l3index = l3lower;
			l5upper = l5Buffer.size - 1;
			for (int k = l4index + 1; k < l4Buffer.size; k++) {
				i = l4Buffer.lower32At(k);
				if (i != 0xffffffff) {
					l5upper = i - 1;
					break;
				}
			}
		}
		int t1=l5Buffer.intAt(l5lower);
		int t2=l5Buffer.intAt(l5upper);
		i= Math.min(l5lower+ (int)(((float)(index-t1)/(t2-t1+1))*(l5upper-l5lower)),l5upper) ;
		while(i<l5Buffer.size-1 && l5Buffer.intAt(i)<index){
			i++;	
		}
		while (l5Buffer.intAt(i)>index && i>0)
			i--;
		//System.out.println(" i ===> "+i);
		context[5] = l5Buffer.intAt(i);
		l5index = i;
	}
	
	protected void sync(int depth, int index){
		// assumption is that this is always at terminal
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
					//l1index--;
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
				if (index>l3Buffer.upper32At(l3index)){
					while (l3index < l3upper && l3Buffer.upper32At(l3index)<index  ){
						l3index++;
					}
				}else {
					while(l3index > l3lower && l3Buffer.upper32At(l3index-1)> index){
						l3index--;
					}
				}
				//assert(index<l3Buffer.intAt(l3index));
			}
			break;
			
		case 3:
			if (l3Buffer.lower32At(l3index)!=-1){
				if (l4lower!=l3Buffer.lower32At(l3index)){
					l4index = l4lower = l3Buffer.lower32At(l3index);
					l4upper = l4Buffer.size - 1;
					int size = l3Buffer.size;
					for (int i = l3index + 1; i < size; i++) {
						int temp = l3Buffer.lower32At(i);
						if (temp != 0xffffffff) {
							l4upper = temp - 1;
							break;
						}
					}
				}
				
				if (index>l4Buffer.upper32At(l4index)){
					while (l4index < l4upper && l4Buffer.upper32At(l4index)< index){
						l4index++;					
					}
				} else {
					while(l4index > l4lower && l4Buffer.upper32At(l4index-1)> index){
						l4index--;
					}
				}
				//assert(index<l3Buffer.intAt(l3index));
			}
			break;
			
		case 4:
			if (l4Buffer.lower32At(l4index)!=-1){
				if (l5lower!=l4Buffer.lower32At(l4index)){
					l5index = l5lower = l4Buffer.lower32At(l4index);
					l5upper = l5Buffer.size - 1;
					int size = l4Buffer.size;
					for (int i = l4index + 1; i < size; i++) {
						int temp = l4Buffer.lower32At(i);
						if (temp != 0xffffffff) {
							l5upper = temp - 1;
							break;
						}
					}
				}
				
				if (index>l5Buffer.intAt(l5index)){
					while (l5index < l5upper && l5Buffer.intAt(l5index)<index  ){
						l5index++;
					}
				}else {
					while(l5index > l5lower && l5Buffer.intAt(l5index-1)> index){
						l5index--;
					}
				}
				//assert(index<l3Buffer.intAt(l3index));
			}
			break;
			
		default:
			if (l4Buffer.lower32At(l4index)!=-1){
				if (l5lower!=l4Buffer.lower32At(l4index)){
					l5index = l5lower = l4Buffer.lower32At(l4index);
					l5upper = l5Buffer.size - 1;
					int size = l4Buffer.size;
					for (int i = l4index + 1; i < size; i++) {
						int temp = l4Buffer.lower32At(i);
						if (temp != 0xffffffff) {
							l5upper = temp - 1;
							break;
						}
					}
				}
				
				//if (context[5]> l5Buffer.intAt(l5index)){
				while (context[5] != l5Buffer.intAt(l5index)){
					l5index++;
				}
				/*} else {
					while (context[5] != l5Buffer.intAt(l5index)){
						l5index--;
					}
				}*/
				
				//assert(index<l3Buffer.intAt(l3index));
			}
			break;
		}
	}
	/**
     * This is for debugging purpose
     * 
     * @param fib
     */
	
	public void sampleState(FastIntBuffer fib){
//		for(int i=0;i<context.)
//			context[i] = -1;
//		fib.append(context);
		if (context[0]>=1)
			fib.append(l1index);
		//else return;
		
		if (context[0]>=2){
			fib.append(l2index);
			fib.append(l2lower);
			fib.append(l2upper);				
		}//else return;
		
		if (context[0]>=3){
		   fib.append(l3index);
		   fib.append(l3lower);
		   fib.append(l3upper);
		}//else return;
		
		if (context[0]>=4){
			   fib.append(l4index);
			   fib.append(l4lower);
			   fib.append(l4upper);	
		}//else return;
		
		if (context[0]>=5){  
			fib.append(l5index);
			fib.append(l5lower);
			fib.append(l5upper);			
		}
	}
	
	public void dumpState(){
		System.out.println("l1 index ==>"+l1index);
		System.out.println("l2 index ==>"+l2index);
		System.out.println("l2 lower ==>"+l2lower);
		System.out.println("l2 upper ==>"+l2upper);
		System.out.println("l3 index ==>"+l3index);
		System.out.println("l3 lower ==>"+l3lower);
		System.out.println("l3 upper ==>"+l3upper);
		System.out.println("l4 index ==>"+l4index);
		System.out.println("l4 lower ==>"+l4lower);
		System.out.println("l4 upper ==>"+l4upper);
		System.out.println("l5 index ==>"+l5index);
		System.out.println("l5 lower ==>"+l5lower);
		System.out.println("l5 upper ==>"+l5upper);
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
				  case 4: val = l4index; break;
				  case 5: val = l5index; break;
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
				}else{
				switch(d)
				{
				  case 1: l1index = val; break;
				  case 2: l2index = val; break;
				  case 3: l3index = val; break;
				  case 4: l4index = val; break;
				  case 5: l5index = val; break;
				  	default:
				}
				context[d] = temp;
				return false;
				}

			case PREV_SIBLING :
				if (atTerminal) {					
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
					case 4: val = l4index; break;
					case 5: val = l5index; break;
				  		default:
					}
					temp = context[d]; // store the current position
				}
			while (toElement(PREV_SIBLING)) {
				if (matchElement(en)) {
					return true;
				}
			}
			if (b) {
				context[0]--;// LN value should not change
				atTerminal = true;
				return false;
			} else {
				switch (d) {
				case 1:
					l1index = val;
					break;
				case 2:
					l2index = val;
					break;
				case 3:
					l3index = val;
					break;
				case 4:
					l4index = val;
					break;
				case 5:
					l5index = val;
					break;
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
				l1index = l2index = l3index = -1;
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
						//System.out.println(" l2 upper: " + l2upper + " l2
                        // lower : " + l2lower);
						l3index =
							(direction == FIRST_CHILD) ? l3lower : l3upper;
						context[3] = l3Buffer.upper32At(l3index);
						return true;
						
					case 3 :
						l4lower = l3Buffer.lower32At(l3index);
						if (l4lower == -1) {
							return false;
						}
						context[0] = 4;
						l4upper = l4Buffer.size - 1;
						size = l3Buffer.size;
						for (int i = l3index + 1; i < size; i++) {
							int temp = l3Buffer.lower32At(i);
							if (temp != 0xffffffff) {
								l4upper = temp - 1;
								break;
							}
						}
						//System.out.println(" l2 upper: " + l2upper + " l2
                        // lower : " + l2lower);
						l4index =
							(direction == FIRST_CHILD) ? l4lower : l4upper;
						context[4] = l4Buffer.upper32At(l4index);
						return true;

					case 4 :
						l5lower = l4Buffer.lower32At(l4index);
						if (l5lower == -1) {
							return false;
						}
						context[0] = 5;

						l5upper = l5Buffer.size - 1;
						size = l4Buffer.size;
						for (int i = l4index + 1; i < size; i++) {
							int temp = l4Buffer.lower32At(i);
							if (temp != 0xffffffff) {
								l5upper = temp - 1;
								break;
							}
						}
						//System.out.println(" l3 upper : " + l3upper + " l3
                        // lower : " + l3lower);
						l5index =
							(direction == FIRST_CHILD) ? l5lower : l5upper;
						context[5] = l5Buffer.intAt(l5index);

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
						context[3] = l3Buffer.upper32At(l3index);
						return true;
						
					case 4 :
						if (direction == NEXT_SIBLING) {
							if (l4index + 1 > l4upper) {
								return false;
							}
							l4index++;
						} else {
							if (l4index - 1 < l4lower) {
								return false;
							}
							l4index--;
						}
						context[4] = l4Buffer.upper32At(l4index);
						return true;
					case 5 :
						if (direction == NEXT_SIBLING) {
							if (l5index + 1 > l5upper) {
								return false;
							}
							l5index++;
						} else {
							if (l5index - 1 < l5lower) {
								return false;
							}
							l5index--;
						}
						context[5] = l5Buffer.intAt(l5index);
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
					context[3]=l3Buffer.upper32At(l3index);
					atTerminal=false;
					return true;
				}else
					return false;
				
			case 3:
				if (l4index!=-1){
					context[0]=4;
					context[4]=l4Buffer.upper32At(l4index);
					atTerminal=false;
					return true;
				}else
					return false;
				
			case 4:
				if (l5index!=-1){
					context[0]=5;
					context[5]=l5Buffer.intAt(l5index);
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
				if (l3index!=-1 && l3index>l3lower){
					l3index--;
					context[0]=3;
					context[3]=l3Buffer.upper32At(l3index);
					atTerminal=false;
					return true;					
				}else
					return false;
			case 3:
				if (l4index!=-1 && l4index>l4lower){
					l4index--;
					context[0]=4;
					context[4]=l4Buffer.upper32At(l4index);
					atTerminal=false;
					return true;					
				}else
					return false;
				
			case 4:
				if (l5index!=-1 && l5index>l5lower){
					l5index--;
					context[0]=5;
					context[5]=l5Buffer.intAt(l5index);
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
		int temp=-1;
		int val=0;
		int d=-1; // temp location
		boolean b=false;
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
				  case 4: val = l4index; break;
				  case 5: val = l5index; break;
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
				}else{
				switch(d)
				{
				  case 1: l1index = val; break;
				  case 2: l2index = val; break;
				  case 3: l3index = val; break;
				  case 4: val = l4index; break;
				  case 5: val = l5index; break;
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
				  case 4: val = l4index; break;
				  case 5: val = l5index; break;
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
				} else {
				switch(d)
				{
				  case 1: l1index = val; break;
				  case 2: l2index = val; break;
				  case 3: l3index = val; break;
				  case 4: val = l4index; break;
				  case 5: val = l5index; break;
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
			}			
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
		}
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
		
		recoverNode_l4(index);
		if (d==4){
			//resolveLC();
			if (atTerminal && type>VTDNav.TOKEN_ATTR_NS)
				 sync(4,index);
			return;
		}
		
		recoverNode_l5(index);
		if (d==5){
			//resolveLC();
			if (atTerminal && type>VTDNav.TOKEN_ATTR_NS)
				 sync(5,index);
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
		while(d>5){
			while( !(getTokenType(t)==VTDNav.TOKEN_STARTING_TAG && 
					getTokenDepth(t)==d)){
				t--;
			}
			context[d] = t;
			d--;
		}
		//resolveLC();		
	}
	
	public void writeIndex(OutputStream os) throws IndexWriteException, IOException{
	    IndexHandler.writeIndex_L5((byte)1,
	            this.encoding,
	            this.ns,
	            true,
	            this.nestingLevel-1,
	            5,
	            this.rootIndex,
	            this.XMLDoc.getBytes(),
	            this.docOffset,
	            this.docLen,
	            (FastLongBuffer)this.vtdBuffer,
	            (FastLongBuffer)this.l1Buffer,
	            (FastLongBuffer)this.l2Buffer,
	            (FastLongBuffer)this.l3Buffer,
	            (FastLongBuffer)this.l4Buffer,
	            (FastIntBuffer)this.l5Buffer,
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
	    IndexHandler.writeSeparateIndex_L5((byte)2,
	            this.encoding,
	            this.ns,
	            true,
	            this.nestingLevel-1,
	            5,
	            this.rootIndex,
	           // this.XMLDoc.getBytes(),
	            this.docOffset,
	            this.docLen,
	            (FastLongBuffer)this.vtdBuffer,
	            (FastLongBuffer)this.l1Buffer,
	            (FastLongBuffer)this.l2Buffer,
	            (FastLongBuffer)this.l3Buffer,
	            (FastLongBuffer)this.l4Buffer,
	            (FastIntBuffer)this.l5Buffer,
	            os);
	}
	
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
			//l1index = l2index = l3index = l2lower=l2upper=l3lower=l3upper=l4lower=l4upper=l5lower=l5upper=-1;
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
					index = context[2]+1;
					tmp = l3Buffer.upper32At(l3lower);
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
			case 3:
				if (l3Buffer.lower32At(l3index)!=-1){
				// l2upper and l2lower
				l4lower = l3Buffer.lower32At(l3index);
				tmp = l3index+1;
				while(tmp<l3Buffer.size){
					if (l3Buffer.lower32At(tmp)!=-1){
						l4upper = l3Buffer.lower32At(tmp)-1;
						break;
					}else
						tmp++;
				}
				if (tmp==l3Buffer.size){
					l4upper = l4Buffer.size-1;
				}					
				index = context[3]+1;
				tmp = l4Buffer.upper32At(l4lower);
				while(index<tmp){
					tokenType = getTokenType(index);
					switch(tokenType){
					case TOKEN_ATTR_NAME:
					case TOKEN_ATTR_NS:
						index+=2;
						break;
					default:
						l4index = l4lower;
						atTerminal = true;
						LN = index;
						return true;															
					}
				}
				l4index = l4lower;
				context[0] = 4;
				context[4] = index;
				return true;				
			}else{
				index = context[3]+1;
				while(index<vtdSize){
					tokenType = getTokenType(index);
					switch(tokenType){
					case TOKEN_ATTR_NAME:
					case TOKEN_ATTR_NS:
						index+=2;
						break;
					default:
						if (getTokenDepth(index)==3 && getTokenType(index)!=VTDNav.TOKEN_STARTING_TAG){
							atTerminal = true;
							LN = index;
							return true;
						}else
							return false;
							
					}
				}
				return false;
			}		
			case 4:
				if (l4Buffer.lower32At(l4index)!=-1){
				// l2upper and l2lower
				l5lower = l4Buffer.lower32At(l4index);
				tmp = l4index+1;
				while(tmp<l4Buffer.size){
					if (l4Buffer.lower32At(tmp)!=-1){
						l5upper = l4Buffer.lower32At(tmp)-1;
						break;
					}else
						tmp++;
				}
				if (tmp==l4Buffer.size){
					l5upper = l5Buffer.size-1;
				}					
				index = context[4]+1;
				tmp = l5Buffer.intAt(l5lower);
				while(index<tmp){
					tokenType = getTokenType(index);
					switch(tokenType){
					case TOKEN_ATTR_NAME:
					case TOKEN_ATTR_NS:
						index+=2;
						break;
					default:
						l5index = l5lower;
						atTerminal = true;
						LN = index;
						return true;															
					}
				}
				l5index = l5lower;
				context[0] = 5;
				context[5] = index;
				return true;				
			}else{
				index = context[4]+1;
				while(index<vtdSize){
					tokenType = getTokenType(index);
					switch(tokenType){
					case TOKEN_ATTR_NAME:
					case TOKEN_ATTR_NS:
						index+=2;
						break;
					default:
						if (getTokenDepth(index)==4 && getTokenType(index)!=VTDNav.TOKEN_STARTING_TAG){
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
							LN = index;
							atTerminal = true;
							return true;
						} else 
							index++;
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
							index+=2;
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
					tokenType=getTokenType(LN);
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
						}else if ( l1index < l1Buffer.size -1){ // whether lindex is the last entry is l1 buffer
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
						if (getTokenType(LN)==TOKEN_PI_NAME)
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
						if (LN < l3Buffer.upper32At(l3upper)) {
							tmp = l3Buffer.upper32At(l3index);
							index = LN + 1;
							if (tokenType== TOKEN_PI_NAME)
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
						loop2:while(index>tmp){
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
						tmp = l2Buffer.upper32At(l2index);
						
						//rewind
						while(index>tmp){
							if (getTokenDepth(index)<2)
								index--;
							else
								break;
						}
						if ((/*lastEntry>=index &&*/ getTokenDepth(index)==1)){
							LN = index;
							atTerminal = true;
							context[0]=1;
							return true;
						}
						
						if (lastEntry!=index && getTokenDepth(index+1)==1 ){
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
				if(atTerminal){
					tokenType=getTokenType(LN);
					if (tokenType==VTDNav.TOKEN_ATTR_NAME)
						return false;
					if (l3Buffer.lower32At(l3index) != -1) {
						if (LN < l4Buffer.upper32At(l4upper)) {
							tmp = l4Buffer.upper32At(l4index);
							index = LN + 1;
							if (tokenType == TOKEN_PI_NAME)
								index++;
							if (index < tmp) {
								LN = index;
								return true;
							} else {
								context[0] = 4;
								context[4] = tmp;
								atTerminal = false;
								return true;
							}
						} else {
							index = LN + 1;
							if (tokenType == TOKEN_PI_NAME)
								index++;
							if (index < vtdSize) {
								depth = getTokenDepth(index);
								if (depth==3 && getTokenType(index)!=TOKEN_STARTING_TAG){									
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
							if (depth==3 && getTokenType(index)!= TOKEN_STARTING_TAG){
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
					if (l3index< l3upper){
						tmp = l3Buffer.upper32At(l3index);
						l3index++;
						lastEntry = index = l3Buffer.upper32At(l3index)-1;
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
							lastEntry = index = l1Buffer.upper32At(l1index+1)-1;
						}
						
						if (l2index != l2Buffer.size-1 && l2index != l2upper){
							lastEntry = index = l2Buffer.upper32At(l2index+1)-1;
						}
						// insert here
						tmp = l3Buffer.upper32At(l3index);
						
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
						
						if (lastEntry!=index && getTokenDepth(index+1)==2 ){
							LN = index+1;
							atTerminal = true;
							context[0]=2;
							return true;
						}						
						return false;
					}					
				}
			case 4:				
				if(atTerminal){
					tokenType=getTokenType(LN);
					if (tokenType==VTDNav.TOKEN_ATTR_NAME)
						return false;
					if (l4Buffer.lower32At(l4index) != -1) {
						if (LN < l5Buffer.intAt(l5upper)) {
							tmp = l5Buffer.intAt(l5index);
							index = LN + 1;
							if (tokenType == TOKEN_PI_NAME)
								index++;
							if (index < tmp) {
								LN = index;
								return true;
							} else {
								context[0] = 5;
								context[5] = tmp;
								atTerminal = false;
								return true;
							}
						} else {
							index = LN + 1;
							if (tokenType == TOKEN_PI_NAME)
								index++;
							if (index < vtdSize) {
								depth = getTokenDepth(index);
								if (depth==4 && getTokenType(index)!=TOKEN_STARTING_TAG){									
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
							if (depth==4 && getTokenType(index)!= TOKEN_STARTING_TAG){
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
					if (l4index< l4upper){
						tmp = l4Buffer.upper32At(l4index);
						l4index++;
						lastEntry = index = l4Buffer.upper32At(l4index)-1;
						//rewind
						loop2:while(index>tmp){
							if (getTokenDepth(index)==3){
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
							context[0] = 4;
							context[4] = index+1;
							return true;
						}
						context[0]=3;
						LN = index+1;
						atTerminal = true;
						return true;						
					}else{						
						lastEntry = index = vtdSize-1;
						
						if (l1index != l1Buffer.size-1){
							lastEntry = index = l1Buffer.upper32At(l1index+1)-1;
						}
						
						if (l2index != l2Buffer.size-1 && l2index != l2upper){
							lastEntry = index = l2Buffer.upper32At(l2index+1)-1;
						}

						if (l3index != l3Buffer.size-1 && l3index != l3upper){
							lastEntry = index = l3Buffer.upper32At(l3index+1)-1;
						}
						// insert here
						tmp = l4Buffer.upper32At(l4index);
						
						//rewind
						while(index>tmp){
							if (getTokenDepth(index)<4)
								index--;
							else
								break;
						}
						if ((/*lastEntry==index &&*/ getTokenDepth(index)==3)){
							LN = index;
							atTerminal = true;
							context[0]=3;
							return true;
						}
						
						if (lastEntry!=index && getTokenDepth(index+1)==3){
							LN = index+1;
							atTerminal = true;
							context[0]=3;
							return true;
						}						
						return false;
					}					
				}
			
			case 5:				
				if(!atTerminal){
				//l2index < l2upper
				if (l5index< l5upper){
					tmp = l5Buffer.intAt(l5index);
					l5index++;
					lastEntry = index = l5Buffer.intAt(l5index)-1;
					//rewind
					loop2:while(index>tmp){
						if (getTokenDepth(index)==4){
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
						context[0]= 5;
						context[5] = index+1;
						return true;
					}
					context[0]=4;
					LN = index+1;
					atTerminal = true;
					return true;						
				}else{
					lastEntry = index = vtdSize-1;
					
					if (l1index != l1Buffer.size-1){
						lastEntry = index = l1Buffer.upper32At(l1index+1)-1;
					}
					
					if (l2index != l2Buffer.size-1 && l2index != l2upper){
						lastEntry = index = l2Buffer.upper32At(l2index+1)-1;
					}
					
					if (l3index != l3Buffer.size-1 && l3index != l3upper){
						lastEntry = index = l3Buffer.upper32At(l3index+1)-1;
					}
					if (l4index != l4Buffer.size-1 && l4index != l4upper){
						lastEntry = index = l4Buffer.upper32At(l4index+1)-1;
					}
					// inser here
					tmp = l5Buffer.intAt(l5index);
					
					//rewind
					while(index>tmp){
						if (getTokenDepth(index)<5)
							index--;
						else
							break;
					}
					if ((/*lastEntry==index &&*/ getTokenDepth(index)==4)){
						LN = index;
						atTerminal = true;
						context[0]=4;
						return true;
					}
					
					if (lastEntry!=index && getTokenDepth(index+1)==4){
						LN = index+1;
						atTerminal = true;
						context[0]=4;
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
						} else if (depth == (tmp-1)) {
							context[0]=tmp-1;
							LN = index;
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
							context[0]=tmp-1;
							LN = index;
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
				}
				return false;
				
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
					tmp = l3Buffer.upper32At(l3index);
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
						context[3]=l3Buffer.upper32At(l3index);
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
			if(atTerminal){
				if (l3Buffer.lower32At(l3index)!=-1){
					tmp = l4Buffer.upper32At(l4index);
					if (LN > tmp){
						index = LN-1;
						if (getTokenType(index)==TOKEN_PI_VAL){
							index--;
						}
						if (getTokenDepth(index)==3){
							LN = index;
							return true;
						}else{
							atTerminal = false;
							context[0]=4;
							context[4]=tmp;
							return true;
						}
					} else if (l4index!=l4lower){
						l4index--;
						atTerminal = false;
						context[0]=4;
						context[4]=l4Buffer.upper32At(l4index);
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
							context[0]=3;
							return true;
						default:
							return false;
						}
					}
				}else{
					index= LN-1;
					if (getTokenType(index)==TOKEN_PI_VAL)
						index--;
					if (index > context[3]){
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
						context[3] = l3Buffer.upper32At(l3index);
						return true;
					}else
						return false;
				}		
			}				
		case 4: 			
			if(atTerminal){
				if (l4Buffer.lower32At(l4index)!=-1){
					tmp = l5Buffer.intAt(l5index);
					if (LN > tmp){
						index = LN-1;
						if (getTokenType(index)==TOKEN_PI_VAL){
							index--;
						}
						if (getTokenDepth(index)==4){
							LN = index;
							return true;
						}else{
							atTerminal = false;
							context[0]=5;
							context[5]=tmp;
							return true;
						}
					} else if (l5index!=l5lower){
						l5index--;
						atTerminal = false;
						context[0]=5;
						context[5]=l5Buffer.intAt(l5index);
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
							context[0]=4;
							return true;
						default:
							return false;
						}
					}
				}else{
					index= LN-1;
					if (getTokenType(index)==TOKEN_PI_VAL)
						index--;
					if (index > context[4]){
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
				index = context[4]-1;	
				tokenType = getTokenType(index);
				if (getTokenDepth(index)==3
						&& tokenType!= TOKEN_ATTR_VAL
						&& tokenType!= TOKEN_STARTING_TAG){
					if (tokenType==TOKEN_PI_VAL)
						index--;
					context[0]=3;
					atTerminal = true;
					LN = index;
					return true;
				}else{
					// no more prev sibling element
					if (l4index != l4lower){
						l4index--;
						context[4] = l4Buffer.upper32At(l4index);
						return true;
					}else
						return false;
				}		
			}				
		case 5: 
			if(!atTerminal){
				index = context[5]-1;	
				tokenType = getTokenType(index);
				if (getTokenDepth(index)==4
						&& tokenType!= TOKEN_ATTR_VAL
						&& tokenType!= TOKEN_STARTING_TAG){
					if (tokenType==TOKEN_PI_VAL)
						index--;
					context[0]=4;
					atTerminal = true;
					LN = index;
					return true;
				}else{
					// no more prev sibling element
					if (l5index != l5lower){
						l5index--;
						context[5] = l5Buffer.intAt(l5index);
						return true;
					}else
						return false;
				}		
			}
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
					if (depth == tmp) {
						context[0] = tmp;
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
					if (depth == tmp-1) {
						context[0]=tmp-1;
						LN = index;
						atTerminal = true;
						return true;
					} else
						index--;
					break;
				case TOKEN_PI_VAL:
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
				tmp = l3Buffer.upper32At(l3index);
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
			
		case 3:
			if (l3Buffer.lower32At(l3index) != -1) {
				l4lower = l3Buffer.lower32At(l3index);
				tmp = l3index + 1;
				while (tmp < l3Buffer.size) {
					if (l3Buffer.lower32At(tmp) != -1) {
						l4upper = l3Buffer.lower32At(tmp) - 1;
						break;
					} else
						tmp++;
				}
				if (tmp == l3Buffer.size) {
					l4upper = l4Buffer.size - 1;
				}
				l4index = l4upper;
				index = vtdSize - 1;

				if (l1index != l1Buffer.size - 1) {
					index = l1Buffer.upper32At(l1index + 1) - 1;
				}

				if (l2index != l2Buffer.size - 1 && l2index != l2upper) {
					index = l2Buffer.upper32At(l2index + 1) - 1;
				}

				if (l3index != l3Buffer.size - 1 && l3index != l3upper) {
					index = l3Buffer.upper32At(l3index + 1) - 1;
				}

				tmp = l4Buffer.upper32At(l4index);
				// rewind and find the first node of depth 1
				while (index > tmp) {
					depth = getTokenDepth(index);
					if (depth < 3)
						index--;
					else if (depth == 3) {
						tokenType = getTokenType(index);
						if (tokenType == TOKEN_PI_VAL)
							LN = index - 1;
						else
							LN = index;
						atTerminal = true;
						// context[0]=1;
						return true;
					} else
						break;
				}
				context[0] = 4;
				context[4] = tmp;
				return true;
			} else {
				index = context[3] + 1;
				loop: while (index < vtdSize) {
					tokenType = getTokenType(index);
					switch (tokenType) {
					case TOKEN_ATTR_NAME:
					case TOKEN_ATTR_NS:
						index += 2;
						break;
					default:
						break loop;
					}
				}

				if (index < vtdSize && getTokenDepth(index) == 3
						&& getTokenType(index) != VTDNav.TOKEN_STARTING_TAG) {
					lastEntry = index;
					index++;
					// scan forward
					loop2: while (index < vtdSize) {
						tokenType = getTokenType(index);
						depth = getTokenDepth(index);
						if (depth == 3) {
							switch (tokenType) {
							case TOKEN_CHARACTER_DATA:
							case TOKEN_COMMENT:
							case TOKEN_CDATA_VAL:
								lastEntry = index;
								index++;
								break;
							case TOKEN_PI_NAME:
								lastEntry = index;
								index += 2;
								break;
							default:
								break loop2;
							}
						} else
							break loop2;
					}
					LN = lastEntry;
					atTerminal = true;
					return true;
				} else
					return false;
			}
		case 4: 
			if (l4Buffer.lower32At(l4index)!=-1){
				l5lower = l4Buffer.lower32At(l4index);
				tmp = l4index+1;
				while(tmp<l4Buffer.size){
					if (l4Buffer.lower32At(tmp)!=-1){
						l5upper = l4Buffer.lower32At(tmp)-1;
						break;
					}else
						tmp++;
				}
				if (tmp==l4Buffer.size){
					l5upper = l5Buffer.size-1;
				}					
				l5index = l5upper;
				index =vtdSize-1;
				
				if (l1index != l1Buffer.size-1){
					index = l1Buffer.upper32At(l1index+1)-1;
				}
				
				if (l2index != l2Buffer.size-1 && l2index != l2upper){
					index = l2Buffer.upper32At(l2index+1)-1;
				}

				if (l3index != l3Buffer.size-1 && l3index != l3upper){
					index = l3Buffer.upper32At(l3index+1)-1;
				}

				if (l4index != l4Buffer.size-1 && l4index != l4upper){
					index = l4Buffer.upper32At(l4index+1)-1;
				}

				tmp = l5Buffer.intAt(l5index);
				// rewind and find the first node of depth 1
				while(index > tmp){
					depth = getTokenDepth(index);
					if (depth <4)
						index--;
					else if (depth == 4){
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
				context[0]=5;
				context[5]=tmp;
				return true;					
			}else{
				index = context[4]+1;
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
									
				if (index< vtdSize && getTokenDepth(index)==4 && getTokenType(index)!=VTDNav.TOKEN_STARTING_TAG){
					lastEntry = index;
					index++;
					//scan forward
					loop2:while(index<vtdSize){
						tokenType = getTokenType(index);
						depth = getTokenDepth(index);
						if (depth == 4){
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
							}
							return true;									
						} else
							return false;
					}else if (depth == (context[0] + 1)) {
						lastEntry = index;
						atTerminal= false;
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
					int i1, i2, i3; // l2lower, l2upper and l2index
					i1 = l1Buffer.lower32At(l1index);
					if (i1 != -1) {
						
						int tmp = l1index + 1;
						i2 = l2Buffer.size - 1;
						while (tmp < l1Buffer.size) {
							if (l1Buffer.lower32At(tmp) != -1) {
								i2 = l1Buffer.lower32At(tmp) - 1;
								break;
							} else
								tmp++;
						}
						if (i1 != l2lower)
							return false;						
						if (l2upper != i2)
							return false;
						if (l2index > l2upper || l2index < l2lower)
							return false;
						if (l2index != l2upper) {
							if (l2Buffer.upper32At(l2index) < LN)
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
					int i1,i2, i3; //l2lower, l2upper and l2index
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
						
						if (l3index > l3upper || l3index < l3lower)
							return false;
						if (l3index != l3upper) {
							if (l3Buffer.upper32At(l3index) < LN)
								return false;
						} 
					}
					return true;
				}else 
					return false;
				
			case 3:
				if (LN>context[3] && context[3]> context[2] && context[2]> context[1]){
					//if (getTokenDepth(LN) != 2)
					//	return false;
					if (l1index<0 || l1index>l1Buffer.size)
						return false;
					int i1,i2, i3; //l2lower, l2upper and l2index
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
					if (i1==-1){return false;}
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
						
					if (l3index > l3upper || l3index < l3lower)
							return false;
					/*if (l3index != l3upper) {
						if (l3Buffer.upper32At(l3index) < LN)
							return false;
					} */
					//l4
					i1 = l3Buffer.lower32At(l3index);
					if (i1!=-1){
						if (l4lower!=i1)
							return false;
						i2 = l4Buffer.size-1;
						tmp = l3index+1;
						
						while(tmp<l3Buffer.size){
							if (l3Buffer.lower32At(tmp)!=-1){
								i2 = l3Buffer.lower32At(tmp)-1;
								break;
							}else
								tmp++;
						}
						
						if (l4lower!=i1)
							return false;
						
						if (l4upper!=i2)
							return false;
						
						if (l4index > l4upper || l4index < l4lower)
							return false;
						if (l4index != l4upper) {
							if (l4Buffer.upper32At(l4index) < LN)
								return false;
						} 
					}
					return true;
				}else 
					return false;
			case 4:
				if (LN>context[3] && context[3]> context[2] && context[2]> context[1]){
					//if (getTokenDepth(LN) != 2)
					//	return false;
					if (l1index<0 || l1index>l1Buffer.size)
						return false;
					int i1,i2, i3; //l2lower, l2upper and l2index
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
					if (i1==-1){return false;}
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
						
					if (l3index > l3upper || l3index < l3lower)
							return false;
					/*if (l3index != l3upper) {
						if (l3Buffer.upper32At(l3index) < LN)
							return false;
					} */
					i1 = l3Buffer.lower32At(l3index);
					if (i1==-1){ return false;}
					if (l4lower!=i1)
						return false;
					i2 = l4Buffer.size-1;
					tmp = l3index+1;
						
					while(tmp<l3Buffer.size){
						if (l3Buffer.lower32At(tmp)!=-1){
							i2 = l3Buffer.lower32At(tmp)-1;
							break;
						}else
							tmp++;
					}
						
					if (l4lower!=i1)
						return false;
						
					if (l4upper!=i2)
						return false;
						
					if (l4index > l4upper || l4index < l4lower)
						return false;
					/*if (l4index != l4upper) {
						if (l4Buffer.upper32At(l4index) < LN)
							return false;
					}*/
					i1=l4Buffer.lower32At(l4index);
					if (i1!=-1){
						if (i1!=l5lower)return false;
						i2 = l5Buffer.size-1;
						tmp = l4index+1;
						
						while(tmp<l4Buffer.size){
							if (l4Buffer.lower32At(tmp)!=-1){
								i2 = l4Buffer.lower32At(tmp)-1;
								break;
							}else
								tmp++;
						}
						
						if (l5lower!=i1)
							return false;
						
						if (l5upper!=i2)
							return false;
						
						if (l5index<i1 || l5index>i2)
							return false;
						
						if (l5index != l5upper) {
							if (l5Buffer.intAt(l5index) < LN)
								return false;
						} 				
					}
					return true;
				}else 
					return false;
				
				
			default:  
				if (l1index<0 || l1index>l1Buffer.size)
					return false;
				int i1,i2,i3; //l2lower, l2upper and l2index
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
				if (i1==-1){return false;}
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
					
				if (l3index > l3upper || l3index < l3lower)
						return false;
				
				i1 = l3Buffer.lower32At(l3index);
				if (i1==-1){ return false;}
				if (l4lower!=i1)
					return false;
				i2 = l4Buffer.size-1;
				tmp = l3index+1;
					
				while(tmp<l3Buffer.size){
					if (l3Buffer.lower32At(tmp)!=-1){
						i2 = l3Buffer.lower32At(tmp)-1;
						break;
					}else
						tmp++;
				}
					
				if (l4lower!=i1)
					return false;
					
				if (l4upper!=i2)
					return false;
					
				if (l4index > l4upper || l4index < l4lower)
					return false;
				/*if (l4index != l4upper) {
					if (l4Buffer.upper32At(l4index) < LN)
						return false;
				}*/
				i1=l4Buffer.lower32At(l4index);
				
				if (i1!=l5lower)return false;
				i2 = l5Buffer.size-1;
				tmp = l4index+1;
					
				while(tmp<l4Buffer.size){
					if (l4Buffer.lower32At(tmp)!=-1){
						i2 = l4Buffer.lower32At(tmp)-1;
						break;
					}else
						tmp++;
				}
					
				if (l5lower!=i1)
					return false;
					
				if (l5upper!=i2)
					return false;
					
				if (l5index<i1 || l5index>i2)
					return false;
					
				if (context[context[0]]>LN)
					return false;
				
				if (context[0]==5){
					if (l5index!=l5upper){
						if(l5Buffer.intAt(l5index)>LN)
							return false;
					}
					if (l5index+1 <= l5Buffer.size-1){
						if (l5Buffer.intAt(l5index+1)<LN){
							return false;
						}
					}
				}
				return true;
			}
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
}
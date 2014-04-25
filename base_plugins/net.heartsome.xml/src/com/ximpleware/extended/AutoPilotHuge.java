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


import com.ximpleware.extended.xpath.*;
import java.util.*;
import java.io.*;
/**
 * XimpleWare's AutoPilotHuge implementation encapsulating node iterator
 * and XPath.
 * AutoPilotHuge is an extended VTD edition of AutoPilot supporting 256 GigByte
 * XML file
 * 
 */
public class AutoPilotHuge {
    private int depth;
    // the depth of the element at the starting point will determine when to stop iteration
    private int iter_type; // see selectElement
    private VTDNavHuge vn; // the navigator object
    private int index; // for iterAttr
    private boolean ft; // a helper variable for 
    private boolean special;   // This helps distinguish between
    				   		   // the case of node() and * for preceding axis
    						   // of xpath evaluation
    private String name; // Store element name after selectElement
    private String localName; // Store local name after selectElemntNS
    private String URL; // Store URL name after selectElementNS
    private int size; // for iterateAttr
    
    private Expr xpe;	// for evalXPath
    
    private int[] contextCopy;  //for preceding axis
    private int stackSize;  // the stack size for xpath evaluation
    static private Hashtable nsHash;
    //private parser p;
    // defines the type of "iteration"
    public final static int UNDEFINED = 0;
    // set the mode corresponding to DOM's getElemetnbyName(string)
    public final static int SIMPLE = 1;
    // set the mode corresponding to DOM's getElementbyNameNS(string)
    public final static int SIMPLE_NS = 2;
    public final static int DESCENDANT = 3;
    public final static int DESCENDANT_NS = 4;
    public final static int FOLLOWING = 5;
    public final static int FOLLOWING_NS=6;
    public final static int PRECEDING = 7;
    public final static int PRECEDING_NS=8;
    public final static int ATTR = 9;
    public final static int ATTR_NS = 10;
    static private Hashtable symbolHash;
    
 protected String getName(){
   	return name;
 }
/**
 * AutoPilotHuge constructor comment.
 * @exception IllegalArgumentException If the VTDNav object is null 
 */
public AutoPilotHuge(VTDNavHuge v) {
    if (v == null)
        throw new IllegalArgumentException(" instance of VTDNav can't be null ");
    name = null;
    vn = v;
    //depth = v.getCurrentDepth();
    iter_type = UNDEFINED; // not defined
    ft = true;
    size = 0;
    special = false;
    xpe = null;
    symbolHash = new Hashtable();
    //p = null;       
}

/**
 * Use this constructor for delayed binding to VTDNavHuge
 * which allows the reuse of XPath expression 
 *
 */
public AutoPilotHuge(){
    name = null;
    //vn = v;
    //depth = v.getCurrentDepth();
    iter_type = UNDEFINED; // not defined
    ft = true;
    size = 0;
    special = false;
    xpe = null;
    symbolHash = new Hashtable();
}
/** This function creates URL ns prefix 
 *  and is intended to be called prior to selectXPath
 *  @param prefix
 *  @param URL
 */

public void declareXPathNameSpace(String prefix, String URL){
    if (nsHash==null)
        nsHash = new Hashtable();
    nsHash.put(prefix, URL);
    //System.out.println(ht); 
}

/**
 * Bind resets the internal state of AutoPilotHuge
 * so one can attach a VTDNavHuge object to the autopilot
 * @param vnv
 *
 */
public void bind (VTDNavHuge vnv){
    name = null;
    if (vnv == null)
        throw new IllegalArgumentException(" instance of VTDNav can't be null ");
    vn = vnv;
    //depth = v.getCurrentDepth();
    iter_type = UNDEFINED; // not defined
    ft = true;
    size = 0;
    special = false;
    //resetXPath();
}
/**
 * Register the binding between a variableExpr name and variableExpr expression 
 * @param varName
 * @param varExpr
 * @throws XPathParseException
 */
public void declareVariableExpr(String varName, String varExpr) throws XPathParseExceptionHuge {
    try{
        parser p = new parser(new StringReader(varExpr));
        p.nsHash = nsHash;
        p.symbolHash = symbolHash;
        xpe = (com.ximpleware.extended.xpath.Expr) p.parse().value;
        symbolHash.put(varName, xpe);
        ft = true;
     }catch(XPathParseExceptionHuge e){
    	 System.out.println("Syntax error after  ==>"+varExpr.substring(0,e.getOffset()));
         throw e;
     }catch(Exception e){
         throw new XPathParseExceptionHuge("Error occurred");
     }
 }
/**
 * Iterate over all the selected element nodes in document order.
 * Null element name allowed, corresponding to node() in xpath
 * Creation date: (12/4/03 5:25:42 PM)
 * @return boolean
 * @exception com.ximpleware.extended.NavExceptionHuge See description in method toElement() in VTDNavHuge class.
 */
public boolean iterate() throws PilotExceptionHuge, NavExceptionHuge {
    switch (iter_type) {
        case SIMPLE :
        	//System.out.println("iterating ---> "+name+ " depth ---> "+depth);
            /*if (elementName == null)
                throw new PilotException(" Element name not set ");*/
        	if (vn.atTerminal)
        	    return false;
            if (ft == false)
                return vn.iterate(depth, name, special);
            else {
            	ft = false;
                if (special || 
                		vn.matchElement(name)) {                	
                    return true;
                } else
                    return vn.iterate(depth, name, special);
            }
            
        case SIMPLE_NS :
        	if (vn.atTerminal)
        	    return false;
            if (ft == false)
                return vn.iterateNS(depth, URL, localName);
            else {
            	ft = false;
                if (vn.matchElementNS(URL, localName)) {
                	return true;
                } else
                    return vn.iterateNS(depth, URL, localName);
            }
            
         case DESCENDANT:
         	if (vn.atTerminal)
         	    return false;
         	return vn.iterate(depth, name, special);
         	
         case DESCENDANT_NS:
         	if (vn.atTerminal)
         	    return false;         	
         	return vn.iterateNS(depth, URL, localName);
         	
         case FOLLOWING:
         	if (vn.atTerminal)
         	    return false;
            if (ft == false)
                return vn.iterate_following(name, special);
            else {
            	ft = false;
            	// find the first next sibling of 
            	while(true){
            		while (vn.toElement(VTDNavHuge.NS)){
            			 if (special || vn.matchElement(name)) {                	
                            return true;
            			 }
            			 return vn.iterate_following(name, special);
            		}
                    if (vn.toElement(VTDNavHuge.P)==false){
                    	//return vn.iterate_following(name, special);
                        return false;
                    } 
            	}
            }
            
         case FOLLOWING_NS:
         	if (vn.atTerminal)
         	    return false;
         	if (ft == false)
                return vn.iterate_followingNS(URL,localName);
            else {
            	ft = false;
            	// find the first next sibling of 
            	while(true){
            		while (vn.toElement(VTDNavHuge.NS)){
            			 if (vn.matchElementNS(URL,localName)) {                	
                            return true;
            			 }
            			 return vn.iterate_followingNS(URL,localName);
            		}
                    if (vn.toElement(VTDNavHuge.P)==false){
                    	return false;
                    } 
            	}
            }
           
         case PRECEDING: 
         	if (vn.atTerminal)
         	    return false;
         	return vn.iterate_preceding(name, contextCopy,special);

         case PRECEDING_NS:
         	if (vn.atTerminal)
         	    return false;
         	return vn.iterate_precedingNS(URL,localName,contextCopy);
                    	
        default :
            throw new PilotExceptionHuge(" iteration action type undefined");
    }
}
/**
 * This method implements the attribute axis for XPath
 * @return the integer of the selected VTD index for attribute name
 * @throws com.ximpleware.extended.PilotException
 */
   protected int iterateAttr() throws PilotExceptionHuge,NavExceptionHuge{
      
   	    switch(iter_type){
   	    	case ATTR:
   	    		if (name.compareTo("*")==0){
   	    			if (ft != false){
   	    				ft = false;
   	    				index = vn.getCurrentIndex2()+1;
   	    			} else
   	    				index +=2;
   	    			if (vn.ns == false){
   	    				while(index<size){
   	    					int type = vn.getTokenType(index);
   	    					if (type == VTDNavHuge.TOKEN_ATTR_NAME
   	    						|| type == VTDNavHuge.TOKEN_ATTR_NS){
   	    					    vn.LN = index;
   	    						return index;
   	    					}else{   	    				
   	    						return -1;
   	    					}
   	    				}
   	    				return -1;
   	    			}else {
   	    				
   	    				while(index<size){
   	    				 int type = vn.getTokenType(index);
	    					if (type == VTDNavHuge.TOKEN_ATTR_NAME
	    						|| type == VTDNavHuge.TOKEN_ATTR_NS){
	    						if (type == VTDNavHuge.TOKEN_ATTR_NAME){
	    						    vn.LN = index;
	    							return index;
	    						}
	    						else 
	    							index += 2;	    						
	    					}else{   	    				
	    						return -1;
	    					}
	    					
   	    				}
   	    				return -1;
   	    			}
   	    		}else{
   	    			if (ft == false){
   	    				return -1;
   	    			} else {
   	    				ft = false;
   	    				int i = vn.getAttrVal(name);
   	    				if(i!=-1){
   	    				    vn.LN = i-1;
   	    					return i-1;
   	    				}
   	    				else 
   	    					return -1;
   	    			}   	    			
   	    		}
   	        case ATTR_NS:
	    			if (ft == false){
   	    				return -1;
   	    			} else {
   	    				ft = false;
   	    				int i = vn.getAttrValNS(URL,localName);
   	    				if(i!=-1){
   	    				    vn.LN = i -1;
   	    					return i-1;
   	    				}
   	    				else 
   	    					return -1;
   	    			} 
   	        default:
   	        	throw new PilotExceptionHuge("invalid iteration type");
   	    }
   	
   }
/**
 * Select the element name before iterating.
 * "*" matches every element
 * Creation date: (12/4/03 5:51:31 PM)
 * @param en java.lang.String
 */
	public void selectElement(String en) {
		if (en == null)
			throw new IllegalArgumentException("element name can't be null");
		iter_type = SIMPLE;
		depth = vn.getCurrentDepth();
		//startIndex = vn.getCurrentIndex();
		name = en;
		ft = true;
	}
/**
 * Select the element name (name space version) before iterating. URL, if set to *,
 * matches every namespace URL, if set to null, indicates the namespace is
 * undefined. localname, if set to *, matches any localname Creation date:
 * (12/4/03 6:05:19 PM)
 * 
 * @param ns_URL String
 * @param ln String
 */
public void selectElementNS(String ns_URL, String ln) {
	if (ln == null)
		throw new IllegalArgumentException("local name can't be null");
    iter_type = SIMPLE_NS;
    depth = vn.getCurrentDepth();
    //startIndex = vn.getCurrentIndex();
    localName = ln;
    URL = ns_URL;
    ft = true;
}

/**
 * Select all descendent elements along the descendent axis, without ns awareness
 * @param en
 */
protected void selectElement_D(String en) {
	if (en == null)
		throw new IllegalArgumentException("element name can't be null");
	iter_type = DESCENDANT;
	depth = vn.getCurrentDepth();
	//startIndex = vn.getCurrentIndex();
	name = en;
	ft = true;
}

/**
 * Select all descendent elements along the Descendent axis, withns awareness
 * @param ns_URL
 * @param ln
 */
protected void selectElementNS_D(String ns_URL, String ln){
	if (ln == null)
		throw new IllegalArgumentException("local name can't be null");
    iter_type = DESCENDANT_NS;
    depth = vn.getCurrentDepth();
    //startIndex = vn.getCurrentIndex();
    localName = ln;
    URL = ns_URL;
    ft = true;
}

/**
 * Select all elements along the following axis, without ns,
 * null selects every elements and documents
 * @param en
 */
protected void selectElement_F(String en) {
	if (en == null)
		throw new IllegalArgumentException("element name can't be null");
	iter_type = FOLLOWING;
	ft = true;
	name = en;
}

/**
 * Select all elements along the preceding axis as defined in XPath
 * The namespace-aware version
 * @param en
 */
protected void selectElementNS_F(String ns_URL, String ln){
	if (ln == null)
		throw new IllegalArgumentException("local name can't be null");
	iter_type = FOLLOWING_NS;
    ft = true;
    localName = ln;
    URL = ns_URL;
}

/**
 * Select all elements along the preceding axis as defined in XPath
 * @param en
 */
protected void selectElement_P(String en) {
	if (en == null)
		throw new IllegalArgumentException("element name can't be null");
	depth = vn.getCurrentDepth();
	iter_type = PRECEDING;
    ft = true;	
    name = en;
    contextCopy = (int[])vn.context.clone();
    for(int i = vn.context[0]+1;i<vn.context.length;i++){
        contextCopy[i]=-1;
    }
    contextCopy[0]=vn.rootIndex;
}

/**
 * Select all elements along the preceding axis as defined in XPath
 * This is the namespace aware version
 * @param ns_URL
 * @param ln
 */
protected void selectElementNS_P(String ns_URL, String ln){
	if (ln == null)
		throw new IllegalArgumentException("local name can't be null");
	depth = vn.getCurrentDepth();
	iter_type = PRECEDING_NS;
    ft = true;
    localName = ln;
    URL = ns_URL;
    contextCopy = (int[])vn.context.clone();
    for(int i = vn.context[0]+1;i<vn.context.length;i++){
        vn.context[i]=-1;
    }
    contextCopy[0]=vn.rootIndex;
}

/**
 * Select an attribute name for iteration, * choose all attributes of an element
 * @param en
 */
protected void selectAttr(String en) {
	if (en == null)
		throw new IllegalArgumentException("attribute name can't be null");
	iter_type = ATTR;
    ft = true;
    size = vn.getTokenCount();
    name = en;
}

/**
 * Select an attribute name, both local part and namespace URL part
 * @param ns_URL
 * @param ln
 */
protected void selectAttrNS(String ns_URL, String ln){
	if (ln == null)
		throw new IllegalArgumentException("local name of an attribute can't be null");
	iter_type = ATTR_NS;
    ft = true;
    localName = ln;
    URL = ns_URL;
}

/**
 * This method selects the string representing XPath expression
 * Usually evalXPath is called afterwards
 * @param s
 * @throws XPathParseException
 */

public void selectXPath(String s) throws XPathParseExceptionHuge {
    try{
       parser p = new parser(new StringReader(s));
       p.nsHash = nsHash;
       p.symbolHash = symbolHash;
       xpe = (com.ximpleware.extended.xpath.Expr) p.parse().value;
       ft = true;
    }catch(XPathParseExceptionHuge e){
    	System.out.println("Syntax error after  ==>"+ s.substring(0,e.getOffset()));
        throw e;
    }catch(Exception e){
        throw new XPathParseExceptionHuge("Error occurred");
    }
}

/**
 * Reset the XPath so the XPath Expression can 
 * be reused and revaluated in anther context position
 *
 */

public void resetXPath(){
	if (xpe!=null && vn!=null){
		xpe.reset(vn);
		ft = true;
		vn.contextStack2.size = stackSize;
	}
}
/**
 * evalXPathToNumber() evaluates the xpath expression to a double
 * @return double
 *
 */
public double evalXPathToNumber(){
    return xpe.evalNumber(vn);
}
/**
 * evalXPathToString() evaluates the xpath expression to a String
 * @return String
 *
 */
public String evalXPathToString(){
    return xpe.evalString(vn);
}
/**
 * evalXPathToBoolean() evaluates the xpath expression to a boolean
 * @return boolean
 *
 */
public boolean evalXPathToBoolean(){
    return xpe.evalBoolean(vn);
}
/**
 * This method returns the next node in the nodeset
 * it returns -1 if there is no more node
 * Afer finishing evaluating, don't forget to <em> reset the xpath </em>
 * @return int corresponding to the VTD index
 */
public int evalXPath() throws XPathEvalExceptionHuge, NavExceptionHuge{
	if (xpe!=null){
	    if (ft == true){
	        if (vn != null){
	            stackSize = vn.contextStack2.size;
	        }
			ft = false;
    		xpe.adjust(vn.getTokenCount());
	    }
	   
		return xpe.evalNodeSet(vn);
	}
	throw new PilotExceptionHuge(" Null XPath expression "); 
}

/**
 * Setspecial is used by XPath evaluator to distinguish between
 * node() and *
 * node() corresponding to b= true;
 * @param b
 */

protected void setSpecial(boolean b ){
	special = b;
}

/**
 * Convert the expression to a string
 * For debugging purpose
 * @return String
 */
public String getExprString(){
	return xpe.toString();
}

/**
 * Remove all declared variable expressions
 */
public void clearVariableExprs(){
	symbolHash.clear();
}

/**
 * Remove all namespaces bindings 
 */
public void clearXPathNameSpaces(){
	nsHash.clear();
}
}

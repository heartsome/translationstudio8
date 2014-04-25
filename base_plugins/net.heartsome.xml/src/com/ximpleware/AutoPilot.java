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


import com.ximpleware.xpath.*;
import java.util.*;
import java.io.*;
/**
 * XimpleWare's AutoPilot implementation encapsulating node iterator
 * and XPath.
 * 
 */
public class AutoPilot {
    protected int depth;
    // the depth of the element at the starting point will determine when to stop iteration
    protected int iter_type; // see selectElement
    protected VTDNav vn; // the navigator object
    protected int index; // for iterAttr
    protected int endIndex;
    protected boolean ft; // a helper variable for 
    protected boolean special;   // This helps distinguish between
    				   		   // the case of node() and * for preceding axis
    						   // of xpath evaluation
    protected String name; // Store element name after selectElement
    protected String name2; // store xmlns:+name
    protected String localName; // Store local name after selectElemntNS
    protected String URL; // Store URL name after selectElementNS
    protected int size; // for iterateAttr
    
    protected Expr xpe;	// for evalXPath
    
    protected int[] contextCopy;  //for preceding axis
    protected int stackSize;  // the stack size for xpath evaluation
    private FastIntBuffer fib; // for namespace axis
    
    protected Hashtable nsHash;
    protected boolean enableCaching;
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
    public final static int NAME_SPACE = 11;
    public final static int SIMPLE_NODE = 12;
    public final static int DESCENDANT_NODE = 13;
    public final static int FOLLOWING_NODE = 14;
    public final static int PRECEDING_NODE = 15;
    
    static private Hashtable symbolHash;
    //static int count=0;
    
 protected String getName(){
   	return name;
 }
/**
 * AutoPilot constructor comment.
 * @exception IllegalArgumentException If the VTDNav object is null 
 */
public AutoPilot(VTDNav v) {
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
    fib = null;
    enableCaching = true;
    //fib = new FastIntBuffer(4);
    //p = null;   
    //count=0;
}

/**
 * Use this constructor for delayed binding to VTDNav
 * which allows the reuse of XPath expression 
 *
 */
public AutoPilot(){
    name = null;
    //vn = v;
    //depth = v.getCurrentDepth();
    iter_type = UNDEFINED; // not defined
    ft = true;
    size = 0;
    special = false;
    xpe = null;
    symbolHash = new Hashtable();
    fib = null;
    enableCaching = true;
    //count=0;
}
/** This function creates URL ns prefix 
 *  and is intended to be called prior to selectXPath
 *  @param prefix
 *  @param URL
 */

final public void declareXPathNameSpace(String prefix, String URL){
    if (nsHash==null)
        nsHash = new Hashtable();
    nsHash.put(prefix, URL);
    //System.out.println(ht); 
}

/**
 * Bind is to replace rebind() and setVTDNav()
 * It resets the internal state of AutoPilot
 * so one can attach a VTDNav object to the autopilot
 * @param vnv
 *
 */
public void bind (VTDNav vnv){
    name = null;
    if (vnv == null)
        throw new IllegalArgumentException(" instance of VTDNav can't be null ");
    vn = vnv;
    //depth = v.getCurrentDepth();
    iter_type = UNDEFINED; // not defined
    ft = true;
    size = 0;
    special = false;
    //count = 0;
    //resetXPath();
}

/**
 * Register the binding between a variableExpr name and variableExpr expression 
 * @param varName
 * @param varExpr
 * @throws XPathParseException
 */
public void declareVariableExpr(String varName, String varExpr) throws XPathParseException {
    try{
        parser p = new parser(new StringReader(varExpr));
        p.nsHash = nsHash;
        p.symbolHash = symbolHash;
        xpe = (Expr) p.parse().value;
        symbolHash.put(varName, xpe);
        ft = true;
     }catch(XPathParseException e){
    	 System.out.println("Syntax error after or around the end of  ==>"+varExpr.substring(0,e.getOffset()));
         throw e;
     }catch(Exception e){
         throw new XPathParseException("Error occurred");
     }
 }

/**
 * Remove all declared variable expressions
 */
final public void clearVariableExprs(){
	symbolHash.clear();
}

/**
 * Remove all namespaces bindings 
 */
final public void clearXPathNameSpaces(){
	nsHash.clear();
}

public boolean iterate2() throws PilotException, NavException {
	//count++;
	//System.out.println("count-=>"+count);
	switch (iter_type) {
		case SIMPLE_NODE:
			if (ft && vn.atTerminal)
				return false;
			if (ft){
				ft =false;
				return true;
			}
			return vn.iterateNode(depth);
			
		case DESCENDANT_NODE:
			if (ft&&vn.atTerminal)
				return false;
			else{
				ft=false;
				return vn.iterateNode(depth);
			}
         	
		case FOLLOWING_NODE:
			if (ft){
				boolean b= false;
				do{
					b = vn.toNode(VTDNav.NEXT_SIBLING);
					if (b){
						ft = false;
						return true;
					}else{
						b = vn.toNode(VTDNav.PARENT);
					}
				}while(b);
				return false;
			}			
			return vn.iterate_following_node();
			
		case PRECEDING_NODE:
			if(ft){
				ft = false;
				vn.toNode(VTDNav.ROOT);
				vn.toNode(VTDNav.P);	
			}
			return vn.iterate_preceding_node(contextCopy,endIndex);
		//case 
		default :
			throw new PilotException(" iteration action type undefined");
	}
}
/**
 * Iterate over all the selected element nodes in document order.
 * Null element name allowed, corresponding to node() in xpath
 * Creation date: (12/4/03 5:25:42 PM)
 * @return boolean
 * @exception com.ximpleware.NavException See description in method toElement() in VTDNav class.
 */
public boolean iterate() throws PilotException, NavException {
	//count++;
	//System.out.println("count-=>"+count);
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
            		while (vn.toElement(VTDNav.NS)){
            			 if (special || vn.matchElement(name)) {                	
                            return true;
            			 }
            			 return vn.iterate_following(name, special);
            		}
                    if (vn.toElement(VTDNav.P)==false){
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
            		while (vn.toElement(VTDNav.NS)){
            			 if (vn.matchElementNS(URL,localName)) {                	
                            return true;
            			 }
            			 return vn.iterate_followingNS(URL,localName);
            		}
                    if (vn.toElement(VTDNav.P)==false){
                    	return false;
                    } 
            	}
            }
           
         case PRECEDING: 
         	if (vn.atTerminal)
         	    return false;
         	if(ft){
         		ft = false;
         		vn.toElement(VTDNav.ROOT);
         	}
         	return vn.iterate_preceding(name, contextCopy,endIndex);

         case PRECEDING_NS:
         	if (vn.atTerminal)
         	    return false;
         	if(ft){
         		ft = false;
         		vn.toElement(VTDNav.ROOT);
         	}
         	return vn.iterate_precedingNS(URL,localName,contextCopy,endIndex);
         	

        default :
            throw new PilotException(" iteration action type undefined");
    }
}

/**
 * This method implements the namespace axis for XPath
 * @return
 * @throws PilotException
 * @throws NavException
 */
	protected int iterateNameSpace() throws PilotException, NavException {
		if (vn.ns == false)
			return -1;
		if (ft != false) {
			ft = false;
			index = vn.getCurrentIndex2() + 1;
		} else
			index += 2;

		while (index < size) {
			int type = vn.getTokenType(index);
			if (type == VTDNav.TOKEN_ATTR_NAME || type == VTDNav.TOKEN_ATTR_NS) {
				if (type == VTDNav.TOKEN_ATTR_NS){ 
				    if  (name.equals("*")  
				    		|| vn.matchRawTokenString(index, name2)
				    ){
				    	// check to see if the namespace has appeared before
				    	if (checkNsUniqueness(index)){
				    		vn.LN = index;
				    		vn.atTerminal = true;
				    		return index;
				    	}
				    }
				} 
				index += 2;
			} else {
				vn.atTerminal = false;
				if (vn.toElement(VTDNav.P) == false) {
					return -1;
				} else {
					index = vn.getCurrentIndex2() + 1;
				}
			}
		}

		return -1;
	}
	
	protected boolean checkNsUniqueness(int i) throws NavException{
		for (int j=0;j<fib.size();j++){
			if (vn.compareTokens(fib.intAt(j), vn, i)==0)
				return false;
		}
			
		fib.append(i);
		return true;
	}
/**
 * This method implements the attribute axis for XPath
 * 
 * @return the integer of the selected VTD index for attribute name
 * @throws PilotException
 */
   protected int iterateAttr2() throws PilotException,NavException{
      
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
   	    					if (type == VTDNav.TOKEN_ATTR_NAME
   	    						|| type == VTDNav.TOKEN_ATTR_NS){
   	    					    vn.LN = index;
   	    					    //vn.atTerminal=true;
   	    						return index;
   	    					}else{   	    						
   	    						return -1;
   	    					}
   	    				}
   	    				return -1;
   	    			}else {   	    				
   	    				while(index<size){
   	    				 int type = vn.getTokenType(index);
	    					if (type == VTDNav.TOKEN_ATTR_NAME
	    						|| type == VTDNav.TOKEN_ATTR_NS){
	    						if (type == VTDNav.TOKEN_ATTR_NAME){
	    						    vn.LN = index;
	    						    //vn.atTerminal=true;
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
   	    				    //vn.atTerminal=true;
   	    					return i-1;
   	    				}
   	    				else {   	    					
   	    					return -1;
   	    				}
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
   	    				    //vn.atTerminal=true;
   	    					return i-1;
   	    				}
   	    				else {
   	    					return -1;
   	    				}
   	    			} 
   	        default:
   	        	throw new PilotException("invalid iteration type");
   	    }
   	
   }
 
   /**
    * This method is meant to be called after calling
    * selectAttr() or selectAttrNs(), it will return the 
    * vtd index attribute name or -1 if there is none left
    * @return vtd index attribute name or -1 if there is none left
    * @throws PilotException
    * @throws NavException
    */
   public int iterateAttr() throws PilotException,NavException{
	      
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
  	    					if (type == VTDNav.TOKEN_ATTR_NAME
  	    						|| type == VTDNav.TOKEN_ATTR_NS){
  	    					    //vn.LN = index;
  	    						return index;
  	    					}else{   	    				
  	    						return -1;
  	    					}
  	    				}
  	    				return -1;
  	    			}else {
  	    				
  	    				while(index<size){
  	    				 int type = vn.getTokenType(index);
	    					if (type == VTDNav.TOKEN_ATTR_NAME
	    						|| type == VTDNav.TOKEN_ATTR_NS){
	    						if (type == VTDNav.TOKEN_ATTR_NAME){
	    						    //vn.LN = index;
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
  	    				    //vn.LN = i-1;
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
  	    				    //vn.LN = i -1;
  	    					return i-1;
  	    				}
  	    				else 
  	    					return -1;
  	    			} 
  	        default:
  	        	throw new PilotException("invalid iteration type");
  	    }
  	
  }
   
   final protected void selectNode(){
	   ft = true;
	   depth = vn.getCurrentDepth();
	   iter_type = SIMPLE_NODE;
   }
   
   
   final protected void selectPrecedingNode(){
	   ft = true;
	   depth = vn.getCurrentDepth();
	   contextCopy = (int[])vn.context.clone();
	   if (contextCopy[0]!=-1){
	   for (int i=contextCopy[0]+1;i<contextCopy.length;i++){
		  contextCopy[i]=0;
	   }
	   }//else{
	   //   for (int i=1;i<contextCopy.length;i++){
	   //	   contextCopy[i]=0;
	   //	   }
	   //}
	   iter_type = PRECEDING_NODE;
	   endIndex = vn.getCurrentIndex();
   }
   
   final protected void selectFollowingNode(){
	   ft = true;
	   depth = vn.getCurrentDepth();
	   iter_type = FOLLOWING_NODE;
	  // contextCopy = (int[])vn.context.clone();
   }
   
   
   final protected void selectDescendantNode(){
	   ft = true;
	   depth = vn.getCurrentDepth();
	   iter_type = DESCENDANT_NODE;
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
 * Select all elements along the following axis as defined in XPath
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
    endIndex = vn.getCurrentIndex2();
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
    endIndex = vn.getCurrentIndex2();
    for(int i = vn.context[0]+1;i<vn.context.length;i++){
        vn.context[i]=-1;
    }
    contextCopy[0]=vn.rootIndex;
}

/**
 * Select the name space nodes as defined in XPath
 * @param en
 */
protected void selectNameSpace(String en){
	if (en == null)
		throw new IllegalArgumentException("namespace name can't be null");
	iter_type = NAME_SPACE;
    ft = true;
    size = vn.getTokenCount();
    name = en;
    if (!en.equals("*"))
    	name2="xmlns:"+en;
    if (fib==null)
    	fib = new FastIntBuffer(4);
    else 
    	fib.clear();
}

/**
 * Select an attribute name for iteration, * choose all attributes of an element
 * @param en
 */
public void selectAttr(String en) {
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
final public void selectAttrNS(String ns_URL, String ln){
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

public void selectXPath(String s) throws XPathParseException {
    try{
       parser p = new parser(new StringReader(s));
       p.nsHash = nsHash;
       p.symbolHash = symbolHash;
       xpe = (com.ximpleware.Expr) p.parse().value;
       ft = true;
       if (enableCaching)
    	   xpe.markCacheable();
    }catch(XPathParseException e){
    	System.out.println("Syntax error after or around the end of ==>"+s.substring(0,e.getOffset()));
        throw e;
    }catch(Exception e){
        throw new XPathParseException("Error occurred");
    }
}

/**
 * Reset the XPath so the XPath Expression can 
 * be reused and revaluated in anther context position
 *
 */

final public void resetXPath(){
	if (xpe!=null && vn!=null){
		xpe.reset(vn);
		ft = true;
		vn.contextStack2.size = stackSize;
		if (enableCaching)
			xpe.clearCache();
	}
}
/**
 * evalXPathToNumber() evaluates the xpath expression to a double
 * @return double
 *
 */
final public double evalXPathToNumber(){
    return xpe.evalNumber(vn);
}
/**
 * evalXPathToString() evaluates the xpath expression to a String
 * @return String
 *
 */
final public String evalXPathToString(){
    return xpe.evalString(vn);
}
/**
 * evalXPathToBoolean() evaluates the xpath expression to a boolean
 * @return boolean
 *
 */
final public boolean evalXPathToBoolean(){
    return xpe.evalBoolean(vn);
}
/**
 * This method returns the next node in the nodeset
 * it returns -1 if there is no more node
 * Afer finishing evaluating, don't forget to <em> reset the xpath </em>
 * @return int corresponding to the VTD index
 */
public int evalXPath() throws XPathEvalException, NavException{
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
	throw new PilotException(" Null XPath expression "); 
}

/**
 * Setspecial is used by XPath evaluator to distinguish between
 * node() and *
 * node() corresponding to b= true;
 * @param b
 */

final protected void setSpecial(boolean b ){
	special = b;
}

/**
 * Convert the expression to a string
 * For debugging purpose
 * @return String
 */
final public String getExprString(){
	return xpe.toString();
}
/**
 * set state to false to disable caching, which by default is enabled
 * @param state
 */
final public void enableCaching(boolean state){
	enableCaching = state;
}
}

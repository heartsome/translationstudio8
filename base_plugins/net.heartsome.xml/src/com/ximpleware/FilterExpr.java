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

import com.ximpleware.xpath.Predicate;

/**
 * 
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class FilterExpr extends Expr {


	public Expr e;
	public Predicate p;
	//FastIntBuffer fib;
	//int stackSize;
	boolean first_time;
	public boolean out_of_range; 
	//public int position;
	
	public FilterExpr(Expr l, Predicate pr){
		e = l;
		p = pr;
		//cacheable =false;
		//stackSize = 0;
		//position = 1;
		//fib = new FastIntBuffer(8);
		first_time = true;
		out_of_range=false;
		pr.fe=this;
	}
	/*public int getPositon(){
		return fib.size();
	}*/
	final public boolean evalBoolean(VTDNav vn) {
	    //if (e.isBoolean())
	    //    return e.evalBoolean(vn);
		boolean a = false;
		vn.push2();
		//record stack size
		int size = vn.contextStack2.size;
	    try{	
			a = (evalNodeSet(vn) != -1);
		}catch (Exception e){
		}
		//rewind stack
		vn.contextStack2.size = size;
		reset(vn);
		vn.pop2();
		return a;
	}


	final public double evalNumber(VTDNav vn) {
		//String s = "";
		double d = Double.NaN;
		int a = -1;
        vn.push2();
        int size = vn.contextStack2.size;
        try {
            a = evalNodeSet(vn);
            if (a != -1) {
            	int t = vn.getTokenType(a);
                if (t == VTDNav.TOKEN_ATTR_NAME) {
                	d = vn.parseDouble(a+1);
                } else if (t == VTDNav.TOKEN_STARTING_TAG || t ==VTDNav.TOKEN_DOCUMENT) {
                    String s = vn.getXPathStringVal();
                    d  = Double.parseDouble(s);
                }else if (t == VTDNav.TOKEN_PI_NAME) {
                	if (a+1 < vn.vtdSize || vn.getTokenType(a+1)==VTDNav.TOKEN_PI_VAL)
	                	//s = vn.toString(a+1); 	
                	d = vn.parseDouble(a+1);                	
                }else 
                	d = vn.parseDouble(a);
            }
        } catch (Exception e) {

        }
        vn.contextStack2.size = size;
        reset(vn);
        vn.pop2();
        //return s;
		return d;
	}

	final public int evalNodeSet(VTDNav vn) 
	throws XPathEvalException, NavException {
	    // if tne predicate require context size
	    // needs to precompute the context size
	    // vn.push2();
	    // computerContext();
	    // set contxt();
	    // vn.pop2()
	    // if the context size is zero
	    // get immediately set teh state to end
	    // or backward
	    if (first_time && p.requireContext){
	        first_time = false;
	        int i = 0;
	        //vn.push2();
	        e.adjust(vn.getTokenCount());
	        while(e.evalNodeSet(vn)!=-1)
	            i++;
	        //vn.pop2();
	        p.setContextSize(i);
	        reset2(vn);
	    }
	    if(out_of_range)
	    	return -1;
		int a = e.evalNodeSet(vn);
		while (a!=-1){
			if (p.eval(vn)==true){
				//p.reset();
				return a;				
			}else {
				//p.reset();
				a = e.evalNodeSet(vn);
			}			
		}
		return -1;		
	}

	final public String evalString(VTDNav vn) {
		String s = "";
		int a = -1;
        vn.push2();
        int size = vn.contextStack2.size;
        try {
            a = evalNodeSet(vn);
            if (a != -1) {
            	int t = vn.getTokenType(a);
            	switch(t){
				case VTDNav.TOKEN_STARTING_TAG:
				case VTDNav.TOKEN_DOCUMENT:
					s = vn.getXPathStringVal();
					break;
				case VTDNav.TOKEN_ATTR_NAME:
					s = vn.toString(a + 1);
					break;
				case VTDNav.TOKEN_PI_NAME:
					//if (a + 1 < vn.vtdSize
					//		|| vn.getTokenType(a + 1) == VTDNav.TOKEN_PI_VAL)
						s = vn.toString(a + 1);
					break;
				default:
					s = vn.toString(a);
					break;
				}
            }
        } catch (Exception e) {

        }
        vn.contextStack2.size = size;
        reset(vn);
        vn.pop2();
        return s;
	}

	final public void reset(VTDNav vn) {
		reset2(vn);
		
		//vn.contextStack2.size = stackSize; 
		//position = 1;
		first_time = true;
		/*cached = false; 
		if (cachedNodeSet != null){
			cachedNodeSet.clear();
		}*/
	}
	
	final public void reset2(VTDNav vn){
		out_of_range=false;
		e.reset(vn);
		p.reset(vn);
		//fib.clear();
	}


	final public String toString() {
		
		return "("+e+") "+p;
	}

	final public boolean isNumerical() {
		
		return false;
	}


	final public boolean isNodeSet() {
		return true;
	}
	
	/*public boolean isUnique(int i){
		int size = fib.size();
		for (int j=0; j<size;j++){
			if (i == fib.intAt(j))
				return false;
		}
		fib.append(i);
		return true;
	}*/
	
	final public boolean isString(){
	    return false;
	}
	
	final public boolean isBoolean(){
	    return false;
	}
	// to support computer context size 
	// needs to add 
	final public boolean requireContextSize(){
	    return false;
	}
	
	final public void setContextSize(int size){	    
	}
	final public void setPosition(int pos){
	    
	}
	final public int adjust(int n){
	    return e.adjust(n);
	    //p.adjust(n);
	}
	final public boolean isFinal(){
		return e.isFinal();
	}
	
	final public void markCacheable2(){
		e.markCacheable2();	
		if (p.expr!=null){
			if (p.expr.isFinal()&&p.expr.isNodeSet()){
				CachedExpr ce = new CachedExpr(p.expr);
				p.expr = ce;
			}
			p.expr.markCacheable2();
		}
	}
	
	final public void markCacheable(){
		e.markCacheable();
		if (p.expr!=null){
			if (p.expr.isFinal()&&p.expr.isNodeSet()){
				CachedExpr ce = new CachedExpr(p.expr);
				p.expr = ce;
			}
			p.expr.markCacheable2();
		}
	}	
	
	final public void clearCache(){
		e.clearCache();
		if (p.expr!=null){
			p.expr.clearCache();
		}
	}
}

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

import com.ximpleware.extended.xpath.Expr;
import com.ximpleware.extended.xpath.Predicate;

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
	//public int position;
	
	public FilterExpr(Expr l, Predicate pr){
		e = l;
		p = pr;
		//stackSize = 0;
		//position = 1;
		//fib = new FastIntBuffer(8);
		first_time = true;
	}
	/*public int getPositon(){
		return fib.size();
	}*/
	public boolean evalBoolean(VTDNavHuge vn) {
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


	public double evalNumber(VTDNavHuge vn) {
	   // if (e.isNumerical())
	   //     return e.evalNumber(vn);
		// double d;
		int a = -1;
		vn.push2();
		int size = vn.contextStack2.size;
	        try {
		  a =evalNodeSet(vn);
		  if (a!=-1){
		  	if (vn.getTokenType(a)== VTDNavHuge.TOKEN_ATTR_NAME){
			  a ++;
		  	}else if (vn.getTokenType(a)== VTDNavHuge.TOKEN_STARTING_TAG) {
			  a = vn.getText();
		  	}
		  }			  
		} catch (Exception e){
			
		}
		vn.contextStack2.size = size;
		reset(vn);
		vn.pop2();
		try{
			if (a!=-1) return vn.parseDouble(a);
		}catch (NavExceptionHuge e){
		}
		return Double.NaN;
	}

	public int evalNodeSet(VTDNavHuge vn) 
	throws XPathEvalExceptionHuge, NavExceptionHuge {
	    // if tne predicate require context size
	    // needs to precompute the context size
	    // vn.push2();
	    // computerContext();
	    // set contxt();
	    // vn.pop2()
	    // if the context size is zero
	    // get immediately set teh state to end
	    // or backward
	    if (first_time && p.requireContextSize()){
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

	public String evalString(VTDNavHuge vn) {
	    //if (e.isString())
	   //     return e.evalString(vn);
		vn.push2();
        int size = vn.contextStack2.size;
        int a = -1;
        try {
            a = evalNodeSet(vn);
            if (a != -1) {
                if (vn.getTokenType(a) == VTDNavHuge.TOKEN_ATTR_NAME) {
                    a++;
                }
                if (vn.getTokenType(a) == VTDNavHuge.TOKEN_STARTING_TAG) {
                    a = vn.getText();
                }
            }
        } catch (Exception e) {
        }
        vn.contextStack2.size = size;
        reset(vn);
        vn.pop2();
        try {
            if (a != -1)
                return vn.toString(a);
        } catch (NavExceptionHuge e) {
        }
        return "";
	}

	public void reset(VTDNavHuge vn) {
		reset2(vn);
		//vn.contextStack2.size = stackSize; 
		//position = 1;
		first_time = true;
	}
	
	public void reset2(VTDNavHuge vn){
		e.reset(vn);
		p.reset(vn);
		//fib.clear();
	}


	public String toString() {
		
		return "("+e+") "+p;
	}

	public boolean isNumerical() {
		
		return false;
	}


	public boolean isNodeSet() {
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
	
	public boolean isString(){
	    return false;
	}
	
	public boolean isBoolean(){
	    return false;
	}
	// to support computer context size 
	// needs to add 
	public boolean requireContextSize(){
	    return false;
	}
	
	public void setContextSize(int size){	    
	}
	public void setPosition(int pos){
	    
	}
	public int adjust(int n){
	    return e.adjust(n);
	    //p.adjust(n);
	}
}

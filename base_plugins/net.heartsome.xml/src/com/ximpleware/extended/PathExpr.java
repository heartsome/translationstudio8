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

/**
 * PathExpr implements the Path expression as defined
 * in XPath spec
 *
 */
public class PathExpr extends Expr {

	Expr fe;
	LocationPathExpr lpe;
	//boolean first_time;
	int evalState;
	//FastIntBuffer fib;
	intHash ih;
	
	//public int getPositon(){
	//	return fib.size();
	//}
	
	public PathExpr(Expr f, LocationPathExpr l){
		fe = f;
		lpe = l;
		//first_time = true;
		evalState = 0;
		//fib = new FastIntBuffer(8);
		ih = new intHash();
	}
	
	public boolean evalBoolean(VTDNavHuge vn) {
		boolean a = false;
		vn.push2();
		// record teh stack size
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

	public int evalNodeSet(VTDNavHuge vn) throws XPathEvalExceptionHuge, NavExceptionHuge {
		int a;
		while (true) {
			switch (evalState) {
			case 0: //this state is teh initial state;
				a = fe.evalNodeSet(vn);
				if (a == -1){
					evalState =4;
				}
				else
					evalState = 1;
				break;
			case 1: // fe returns valid value, then iterate the locationPath
				vn.push2();
				a = lpe.evalNodeSet(vn);
				if (a == -1) {
					lpe.reset(vn);					
					evalState = 3;
				} else {
					evalState = 2;
					if (isUnique(a))
					return a;
				}
				break;
			case 2:
				a = lpe.evalNodeSet(vn);
				if (a == -1) {
					lpe.reset(vn);
					evalState = 3;
				} else{
					if (isUnique(a))
						return a;
					//return a;
				}
				break;
			case 3:
				vn.pop2();
				a = fe.evalNodeSet(vn);
				if (a == -1)
					evalState = 4;
				else{
				    vn.push2();
					evalState = 2;
				}
				break;
			case 4:
				return -1;
			default:
				throw new XPathEvalExceptionHuge(
						"Invalid state evaluating PathExpr");
			}
		}
		//return -1;
	}


	public String evalString(VTDNavHuge vn) {
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
// The improved version, use hashtable to check for uniqueness
	public boolean isUnique(int i){
	    return ih.isUnique(i);
		
	}
	
	public void reset(VTDNavHuge vn) {
		
		fe.reset(vn);
		lpe.reset(vn);
		//fib.clear();
		ih.reset();
		evalState = 0;

	}


	public String toString() {
		
		return "("+fe +")/" + lpe;
	}


	public boolean isNumerical() {
	        return false;
	}


	public boolean isNodeSet() {
	        return true;
	}
	
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
	    int i = fe.adjust(n);
	    lpe.adjust(n);
	    
        if (ih!=null && i==ih.e)
        {}
	    else 
	        ih = new intHash(i);
	    return i;
	}
}

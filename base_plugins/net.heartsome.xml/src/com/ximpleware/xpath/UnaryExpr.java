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
package com.ximpleware.xpath;
import com.ximpleware.*;
/**
 * Parser use this class to represent Unary Expr
 *
 */
public class UnaryExpr extends Expr {

	public int op;
	public Expr operand;
	public UnaryExpr (int o, Expr e){
		op = o;
		operand = e;
		//cacheable =false;
	}
	
	final public String toString(){
		return "-"+operand;
	}

		
	final public boolean evalBoolean(VTDNav vn){
		
		return operand.evalBoolean(vn);
	}

	final public double evalNumber(VTDNav vn){ return -1*operand.evalNumber(vn);}
		
	final public int evalNodeSet(VTDNav vn) throws XPathEvalException{
		
		throw new XPathEvalException("UnaryExpr can't eval to a node set!");
	}
	
    final public String evalString(VTDNav vn){
		double dval = operand.evalNumber(vn);
		if (dval == (int) dval){
			return ""+((int) dval);
		}
		return ""+dval;
	}

	final public void reset(VTDNav vn){
		operand.reset(vn);
	}

	final public boolean  isNodeSet(){
		return false;
	}

	final public boolean  isNumerical(){
		return true;
	}
	
	final public boolean isString(){
	    return false;
	}
	
	final public boolean isBoolean(){
	    return false;
	}
	// to support computer context size 
	// needs to add 
	final public boolean requireContextSize(){
	    return operand.requireContextSize();
	}
	
	final public void setContextSize(int size){	  
	    operand.setContextSize(size);
	}
	
	final public void setPosition (int pos){
	    operand.setPosition(pos);
	}
	final public int adjust(int n){
	    return 0;
	}
	final public boolean isFinal(){
		return operand.isFinal();
	}
	
	final public void markCacheable(){
		operand.markCacheable();
	}
	
	final public void markCacheable2(){
		operand.markCacheable2();
	}
	
	final public void clearCache(){
		operand.clearCache();
	}
}

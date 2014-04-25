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
package com.ximpleware.extended.xpath;
import com.ximpleware.extended.*;
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
	}
	
	public String toString(){
		return "-"+operand;
	}

		
	public boolean evalBoolean(VTDNavHuge vn){
		
		return operand.evalBoolean(vn);
	}

	public double evalNumber(VTDNavHuge vn){ return -1*operand.evalNumber(vn);}
		
	public int evalNodeSet(VTDNavHuge vn) throws XPathEvalExceptionHuge{
		
		throw new XPathEvalExceptionHuge("UnaryExpr can't eval to a node set!");
	}
	
        public String evalString(VTDNavHuge vn){
		double dval = operand.evalNumber(vn);
		if (dval == (int) dval){
			return ""+((int) dval);
		}
		return ""+dval;
	}

	public void reset(VTDNavHuge vn){
		operand.reset(vn);
	}

	public boolean  isNodeSet(){
		return false;
	}

	public boolean  isNumerical(){
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
	    return operand.requireContextSize();
	}
	
	public void setContextSize(int size){	  
	    operand.setContextSize(size);
	}
	
	public void setPosition (int pos){
	    operand.setPosition(pos);
	}
	public int adjust(int n){
	    return 0;
	}
}

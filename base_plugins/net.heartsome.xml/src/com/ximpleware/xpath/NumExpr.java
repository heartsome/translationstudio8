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
 * This class represent an XPath that is a double
 *
 */
public class NumExpr extends Expr{
	public double dval;
	public NumExpr( double d){
		dval = d;
		//cacheable =false;
	}
	final public String toString(){
		if (dval == (long) dval){
			return ""+(long) dval;
		}
		return  ""+dval;
	} 

	final public double eval(){
		return dval;
	}
	final public boolean  isNodeSet(){
		return false;
	}

	final public boolean  isNumerical(){
		return true;
	}
		
	final public boolean evalBoolean(VTDNav vn){
		if (dval == 0.0 || Double.isNaN(dval) )
			return false;
		return true;
	}

	final public double evalNumber(VTDNav vn){ return dval;}
		
	final public int evalNodeSet(VTDNav vn) throws XPathEvalException{
		
		throw new XPathEvalException("NumExpr can't eval to a node set!");
	}
	
    final public String evalString(VTDNav vn){
		if (dval == (int) dval){
			return ""+((int) dval);
		}
		return ""+dval;
	}

	final public void reset(VTDNav vn){};
	
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
	    return 0;
	}
	
	final public boolean isFinal(){
		return true;
	}
	/*final public boolean isConstant(){
		return true;
	}*/
	/*final public void markCacheable(){
		
	}
	
	final public void markCacheable2(){}*/
}

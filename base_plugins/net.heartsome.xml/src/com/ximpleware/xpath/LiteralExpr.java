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
 * This class represents a literal string XPath expression
 * 
 */
public class LiteralExpr extends Expr {
	public String s;
	public LiteralExpr (String st){
		s = st;
		//cacheable =false;
	}	
	final public String toString(){
		boolean b = true;
		for(int i = 0;i<s.length();i++){
			if (s.charAt(i) == '\''){
				b = false;
				break;
			}
		}
		if (b == true)
		  return "\""+s+"\"";
		else 
		  return "'" + s + "'";
	}

	final public boolean evalBoolean(VTDNav vn){
		return s.length() != 0;
	}

	final public double evalNumber(VTDNav vn){
		try {
			double dval = Double.parseDouble(s);
			return dval;
		}catch (NumberFormatException e){
			return Double.NaN;
		}	
	}
		
	final public int evalNodeSet(VTDNav vn) throws XPathEvalException{
		
		throw new XPathEvalException("LiteralExpr can't eval to a node set!");
	}
	
        public String evalString(VTDNav vn){
		return s;
	}

	final public void reset(VTDNav vn){ }

	final public boolean  isNodeSet(){
		return false;
	}

	final public boolean  isNumerical(){
		return false;
	}
	
	final public boolean isString(){
	    return true;
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

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
 * This class represent an XPath that is a double
 *
 */
public class NumExpr extends Expr{
	public double dval;
	public NumExpr( double d){
		dval = d;
	}
	public String toString(){
		if (dval == (long) dval){
			return ""+(long) dval;
		}
		return  ""+dval;
	} 

	public double eval(){
		return dval;
	}
	public boolean  isNodeSet(){
		return false;
	}

	public boolean  isNumerical(){
		return true;
	}
		
	public boolean evalBoolean(VTDNavHuge vn){
		if (dval == 0.0 || Double.isNaN(dval) )
			return false;
		return true;
	}

	public double evalNumber(VTDNavHuge vn){ return dval;}
		
	public int evalNodeSet(VTDNavHuge vn) throws XPathEvalExceptionHuge{
		
		throw new XPathEvalExceptionHuge("NumExpr can't eval to a node set!");
	}
	
        public String evalString(VTDNavHuge vn){
		if (dval == (int) dval){
			return ""+((int) dval);
		}
		return ""+dval;
	}

	public void reset(VTDNavHuge vn){};
	
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
	    return 0;
	}
}

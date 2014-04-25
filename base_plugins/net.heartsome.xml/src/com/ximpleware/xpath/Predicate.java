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
 * LocationPathExpr uses this class to represent a predicate
 *
 */
public class Predicate /*implements LocationPathNode*/{
	double d; // only supports a[1] style of location path for now
	public Predicate nextP;
	public int count;
	public Expr expr;
	public int type;
	public Step s;
	public FilterExpr fe;
	public boolean requireContext;
	public final static int simple=0;
	public final static int complex=1;
	
	
	public Predicate(){
		nextP = (Predicate) null;
		count = 0;
		d = 0;
		requireContext = false;
		type = complex;
	}
	final public boolean eval2(VTDNav vn) {
		boolean b;		
		count++; // increment the position
		expr.setPosition(count);
		if (expr.isNumerical()){		    
			b = (expr.evalNumber(vn)== count);
		}
		else{ 
			b = expr.evalBoolean(vn);
		}
		return b;
	}
	
	final public boolean eval(VTDNav vn){
		count++;
		switch (type){
		case simple:
			if (d<count)
				return false;
			else if(d==count){
				if (s!=null){
					s.out_of_range=true;
				}else
					fe.out_of_range=true;
				
				return true;	
			}
	    default:
			boolean b;
			expr.setPosition(count);
			if (expr.isNumerical()){		    
				b = (expr.evalNumber(vn)== count);
			}
			else{ 
				b = expr.evalBoolean(vn);
			}
			return b;
		}		
	}
	
	final public void setIndex(double index) throws XPathEvalException{
		if (index<=0)
			throw new XPathEvalException("Invalid index number");
		d = (double) index;
	}
	
	final public void reset(VTDNav vn){
		count = 0;
		expr.reset(vn); // is this really needed?
	}
	

	final public String toString(){
		String s = "["+expr+"]";
		if (nextP==null){
			return s;
		} else {
			return s+nextP;
		}
	}
	
	// to support computer context size 
	// needs to add 
	
	final public boolean requireContextSize(){
	    return expr.requireContextSize();
	}
	
	final public void setContextSize(int size){
	    expr.setContextSize(size);
	}
	
	final public void adjust(int n){
		expr.adjust(n);
	}
	
	/*public void markCacheable(){
		expr.markCacheable();
	}*/

}


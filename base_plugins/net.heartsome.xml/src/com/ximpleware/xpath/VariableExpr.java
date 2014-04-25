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

public class VariableExpr extends Expr {
	
	private String exprName;
	private Expr exprVal;
	
	public VariableExpr(String name, Expr e){
		exprName = name;
		exprVal = e;
		//cacheable =false;
	}
	
	
	final public int adjust(int n) {
		// TODO Auto-generated method stub
		return exprVal.adjust(n);
	}


	final public boolean evalBoolean(VTDNav vn) {
		// TODO Auto-generated method stub
		return exprVal.evalBoolean(vn);
	}


	final public int evalNodeSet(VTDNav vn) throws XPathEvalException, NavException {
		// TODO Auto-generated method stub
		return exprVal.evalNodeSet(vn);
	}


	final public double evalNumber(VTDNav vn) {
		// TODO Auto-generated method stub
		return exprVal.evalNumber(vn);
	}


	final public String evalString(VTDNav vn) {
		// TODO Auto-generated method stub
		return exprVal.evalString(vn);
	}


	final public boolean isBoolean() {
		// TODO Auto-generated method stub
		return exprVal.isBoolean();
	}


	final public boolean isNodeSet() {
		// TODO Auto-generated method stub
		return exprVal.isNodeSet();
	}


	final public boolean isNumerical() {
		// TODO Auto-generated method stub
		return exprVal.isNumerical();
	}


	final public boolean isString() {
		// TODO Auto-generated method stub
		return exprVal.isString();
	}


	final public boolean requireContextSize() {
		// TODO Auto-generated method stub
		return exprVal.requireContextSize();
	}


	final public void reset(VTDNav vn) {
		// TODO Auto-generated method stub
		exprVal.reset(vn);
	}


	final public void setContextSize(int size) {
		// TODO Auto-generated method stub
		exprVal.setContextSize(size);
	}


	final public void setPosition(int pos) {
		// TODO Auto-generated method stub
		exprVal.setPosition(pos);
	}


	final public String toString() {
		// TODO Auto-generated method stub
		return "$"+exprName;
	}
	
	final public boolean isFinal(){
		return exprVal.isFinal();
	}

	final public void markCacheable(){
		exprVal.markCacheable();
	}
	
	final public void markCacheable2(){
		exprVal.markCacheable2();		
	}
	
	final public void clearCache(){
		exprVal.clearCache();
	}
}

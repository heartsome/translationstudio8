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

public class CachedExpr extends Expr {
	Expr e;
	boolean cached;
	boolean eb;
	double en;
	String es;
	FastIntBuffer ens;
	int count;
	VTDNav vn1;

	public CachedExpr(Expr e1){
		e=e1;
		cached = false;
		ens=null;
		count=0;
		vn1=null;
	}
	@Override
	public boolean evalBoolean(VTDNav vn) {
		// TODO Auto-generated method stub
		if (cached){
			return eb;
		}else{
			eb = e.evalBoolean(vn);
			return eb;
		}
	}

	@Override
	public double evalNumber(VTDNav vn) {
		// TODO Auto-generated method stub
		if (cached){
			return en;
		}else{
			cached = true;
			en = e.evalNumber(vn);
			return en;
		}
	}

	@Override
	public int evalNodeSet(VTDNav vn) throws XPathEvalException, NavException {
		// TODO Auto-generated method stub
		int i=-1;
		if (cached){
			if (count<ens.size){
				i=ens.intAt(count);
				vn.recoverNode(i);
				count++;
				return i;
			}else
				return -1;

		}else{
			cached = true;
			
			if (ens==null){
				ens = new FastIntBuffer(8);//page size 64
			}
			//record node set
			while((i=e.evalNodeSet(vn))!=-1){
				ens.append(i);
			}
			e.reset(vn);
			if(ens.size>0){
				i=ens.intAt(count);//count should be zero
				vn.recoverNode(i);
				count++;
				return i;
			}else
				return -1;
		}
	}

	
	public String evalString(VTDNav vn) {
		if (cached){
			return es;
		}else{
			cached = true;
			es = e.evalString(vn);
			return es;
		}		
	}

	
	public void reset(VTDNav vn) {
		count=0;
		if (e!=null && vn!=null)
			e.reset(vn);
		/*if (vn1!=vn){
			cached = false;
			if (ens!=null)
				ens.clear();
			e.reset(vn);
		}*/
		// TODO Auto-generated method stub
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return "cached("+e.toString()+")";
	}

	@Override
	public boolean isNumerical() {
		// TODO Auto-generated method stub
		return e.isNumerical();
	}

	@Override
	public boolean isNodeSet() {
		// TODO Auto-generated method stub
		return e.isNodeSet();
	}

	@Override
	public boolean isString() {
		// TODO Auto-generated method stub
		return e.isString();
	}

	@Override
	public boolean isBoolean() {
		// TODO Auto-generated method stub
		return e.isBoolean();
	}

	@Override
	public boolean requireContextSize() {
		// TODO Auto-generated method stub
		return e.requireContextSize();
	}

	@Override
	public void setContextSize(int size) {
		// TODO Auto-generated method stub
		e.setContextSize(size);
	}

	@Override
	public void setPosition(int pos) {
		// TODO Auto-generated method stub
		e.setPosition(pos);
	}

	@Override
	public int adjust(int n) {
		// TODO Auto-generated method stub
		return e.adjust(n);
	}

	@Override
	public boolean isFinal() {
		// TODO Auto-generated method stub
		return e.isFinal();
	}

	
	public void markCacheable() {
		// TODO Auto-generated method stub
		e.markCacheable();
	}

	@Override
	public void markCacheable2() {
		// TODO Auto-generated method stub
		e.markCacheable2();
	}
	
	public void clearCache(){
		cached = false;
		if (ens!=null)
			ens.clear();
		e.clearCache();			
	}

}

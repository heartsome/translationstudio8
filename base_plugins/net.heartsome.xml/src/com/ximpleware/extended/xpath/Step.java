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
import com.ximpleware.extended.xpath.Predicate;
/**
 * A step is a part of location path as defined in 
 * Xpath spec
 * 
 */
public class Step implements LocationPathNode{
	public int axis_type;
	public NodeTest nt;  
	public Predicate p,pt;// linked list
	public Step nextS; // points to next step
	public int position; // position
	public Step prevS; // points to the prev step
	public Object o; //AutoPilot goes here
	boolean ft; // first time
	public Step(){
		nextS = prevS = (Step)null;
		p  = pt = null;
		nt = null;
		ft = true;
		position = 1;
	}
		
	public void reset(VTDNavHuge vn){
		ft = true;
		resetP(vn);
		position = 1;
	}
	
	public void resetP(VTDNavHuge vn){
		Predicate temp = p;
		while(temp!=null){
			temp.reset(vn);
			temp = temp.nextP;
		}
	}
	
	public void resetP(VTDNavHuge vn, Predicate p1){
		Predicate temp = p;
		while(temp!=p1){
			temp.reset(vn);
			temp = temp.nextP;
		}
	}
	
	public void adjust(int n){
		Predicate temp = p;
		while(temp!=null){
			temp.adjust(n);
			temp = temp.nextP;
		}
	}
	public NodeTest getNodeTest(){
		return this.nt;
	}
	public Step getNextStep(){
		return nextS;
	}
		
	public void setNextStep(Step s){
		nextS = s;
	}
		
	public boolean get_ft(){
		return ft;
	}
		
	public void set_ft(boolean b){
		ft = b;
	}
				
	public Step getPrevStep(){
		return prevS;
	}
		
	public void setPrevStep(Step s){
		prevS = s;
	}
		
	public void setNodeTest(NodeTest n){
		nt = n;
	}
		
	public void setPredicate(Predicate p1){
		if (p == null){
			p = pt = p1;
		} else {
			pt.nextP = p1;
			pt = pt.nextP;
		}
	}
	
	public boolean eval(VTDNavHuge vn)throws NavExceptionHuge{
		/*boolean result = this.nt.eval(vn);
		if (result == false)
			return false;
		return evalPredicates(vn);*/
		return nt.eval(vn) && evalPredicates(vn);
	}
	
	public boolean eval(VTDNavHuge vn, Predicate p) throws NavExceptionHuge{
	    return nt.eval(vn) && evalPredicates(vn,p);
	}
	
	public boolean evalPredicates(VTDNavHuge vn) throws NavExceptionHuge {
		Predicate temp = this.p;
		while(temp!=null) {
			if (temp.eval(vn)== false)
				return false;
			temp = temp.nextP;
		}
	
		return true;
	}
		
	public boolean evalPredicates(VTDNavHuge vn, Predicate p) throws NavExceptionHuge {
		Predicate temp = this.p;
		while(temp!=p) {
			if (temp.eval(vn)== false)
				return false;
			temp = temp.nextP;
		}	
		return true;
	}
	
	public void setAxisType(int st){
		axis_type = st;
	}

	public String toString(){
		String s;
		if (p == null)
			s = axisName(axis_type) + nt;
		else 
			s = axisName(axis_type) + nt + " "+ p ;

		if (nextS == null)
			return s;
		else 
			return s+"/"+nextS.toString();
	}

	public String axisName(int i){
		switch(i){
			case AxisType.CHILD: return "child::";
			case AxisType.DESCENDANT: return "descendant::";
			case AxisType.PARENT: return "parent::";
			case AxisType.FOLLOWING_SIBLING: return "following-sibling::";
			case AxisType.PRECEDING_SIBLING: return "preceding-sibling::";
			case AxisType.FOLLOWING: return "following::";
			case AxisType.PRECEDING: return "preceding::";
			case AxisType.ATTRIBUTE: return "attribute::";
			case AxisType.NAMESPACE: return "namespace::";
			case AxisType.SELF: return "self::";
			case AxisType.DESCENDANT_OR_SELF: return "descendant-or-self::";
			case AxisType.ANCESTOR: return "ancestor::";
			default: return "ancestor-or-self::";

		}

	}
	

}


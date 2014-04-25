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
 * A step is a part of location path as defined in 
 * Xpath spec
 * 
 */
public class Step{
	public int axis_type;
	public NodeTest nt;  
	public Predicate p,pt;// linked list
	public Step nextS; // points to next step
	//public int position; // position
	public Step prevS; // points to the prev step
	public Object o; //AutoPilot or TextIter goes here
	public boolean ft; // first time
	public boolean hasPredicate;
	public boolean nt_eval;
	public boolean out_of_range;
	public Step(){
		nextS = prevS = (Step)null;
		p  = pt = null;
		nt = null;
		ft = true;
		hasPredicate =false;
		nt_eval=false;
		out_of_range=false;
		//position = 1;
	}
		
	final public void reset(VTDNav vn){
		ft = true;
		if (hasPredicate)
			resetP(vn);
		//out_of_range=false;
		
		//position = 1;
	}
	
	final public void resetP(VTDNav vn){
		Predicate temp = p;
		while(temp!=null){
			temp.reset(vn);
			temp = temp.nextP;
		}
	}
	final public void setStep4Predicates(){
		Predicate temp = p;
		while(temp!=null){
			temp.s=this;
			temp = temp.nextP;
		}
	}
	
	final public void resetP(VTDNav vn, Predicate p1){
		Predicate temp = p;
		while(temp!=p1){
			temp.reset(vn);
			temp = temp.nextP;
		}
	}
	
	final public void adjust(int n){
		Predicate temp = p;
		while(temp!=null){
			temp.adjust(n);
			temp = temp.nextP;
		}
	}
	final public NodeTest getNodeTest(){
		return this.nt;
	}
	final public Step getNextStep(){
		return nextS;
	}
		
	final public void setNextStep(Step s){
		nextS = s;
	}
		
	final public boolean get_ft(){
		return ft;
	}
		
	final public void set_ft(boolean b){
		ft = b;
	}
				
	final public Step getPrevStep(){
		return prevS;
	}
		
	final public void setPrevStep(Step s){
		prevS = s;
		/*if ((this.axis_type==AxisType.CHILD 
				|| this.axis_type==AxisType.CHILD0
				|| this.axis_type==AxisType.ATTRIBUTE)
			&& this.nt.testType==NodeTest.NAMETEST){
			
		}*/
	}
		
	final public void setNodeTest(NodeTest n){
		nt = n;
		if (axis_type == AxisType.CHILD && n.testType ==NodeTest.NAMETEST ){
			axis_type = AxisType.CHILD0;
		}else if (axis_type == AxisType.DESCENDANT && n.testType ==NodeTest.NAMETEST ){
			axis_type = AxisType.DESCENDANT0;
		}else if (axis_type == AxisType.DESCENDANT_OR_SELF && n.testType ==NodeTest.NAMETEST ){
			axis_type = AxisType.DESCENDANT_OR_SELF0;
		}else if (axis_type == AxisType.FOLLOWING && n.testType ==NodeTest.NAMETEST ){
			axis_type = AxisType.FOLLOWING0;
		}else if (axis_type == AxisType.PRECEDING && n.testType ==NodeTest.NAMETEST ){
			axis_type = AxisType.PRECEDING0;
		}else if (axis_type == AxisType.FOLLOWING_SIBLING && n.testType ==NodeTest.NAMETEST ){
			axis_type = AxisType.FOLLOWING_SIBLING0;
		}else if (axis_type == AxisType.PRECEDING_SIBLING&& n.testType ==NodeTest.NAMETEST ){
			axis_type = AxisType.PRECEDING_SIBLING0;
		}
		if (n.testType== NodeTest.NODE 
				|| (n.testType==NodeTest.NAMETEST && n.nodeName.equals("*"))){
			nt_eval= true;
		}
		
	}
		
	final public void setPredicate(Predicate p1){
		if (p == null){
			p = pt = p1;
		} else {
			pt.nextP = p1;
			pt = pt.nextP;			
		}
		setStep4Predicates();
		if (p1!=null) hasPredicate = true;
	}
	
	final public boolean eval(VTDNav vn)throws NavException{
		/*boolean result = this.nt.eval(vn);
		if (result == false)
			return false;
		return evalPredicates(vn);*/
		return nt.eval(vn) && ((!hasPredicate) || evalPredicates(vn));
	}
	
	final public boolean eval2(VTDNav vn)throws NavException{
		/*boolean result = this.nt.eval(vn);
		if (result == false)
			return false;
		return evalPredicates(vn);*/
		//return nt.eval2(vn) && evalPredicates(vn);
		return nt.eval2(vn) && ((!hasPredicate) || evalPredicates(vn));
	}
	
	final public boolean eval(VTDNav vn, Predicate p) throws NavException{
	    return nt.eval(vn) && evalPredicates(vn,p);
	}
	
	final public boolean eval2(VTDNav vn, Predicate p) throws NavException{
	    return nt.eval2(vn) && evalPredicates(vn,p);
	}
	
	final public boolean evalPredicates(VTDNav vn) throws NavException {
		Predicate temp = this.p;
		while(temp!=null) {
			if (temp.eval(vn)== false)
				return false;
			temp = temp.nextP;
		}
	
		return true;
	}
		
	final public boolean evalPredicates(VTDNav vn, Predicate p) throws NavException {
    	Predicate temp = this.p;
    	while(temp!=p) {
    		if (temp.eval(vn)== false)
    			return false;
    		temp = temp.nextP;
    	}	
    	return true;
    }
	
	final public void setAxisType(int st){
		axis_type = st;
	}

	final public String toString(){
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

	final public String axisName(int i){
		switch(i){
			case AxisType.CHILD0:
			case AxisType.CHILD: return "child::";
			case AxisType.DESCENDANT_OR_SELF0: return "descendant-or-self::";
			case AxisType.DESCENDANT0: return "descendant::";
			case AxisType.PRECEDING0: return "preceding::";
			case AxisType.FOLLOWING0: return "following::";
			case AxisType.DESCENDANT_OR_SELF: return "descendant-or-self::";
			case AxisType.DESCENDANT: return "descendant::";
			case AxisType.PRECEDING: return "preceding::";
			case AxisType.FOLLOWING: return "following::";
			case AxisType.PARENT: return "parent::";
			case AxisType.ANCESTOR: return "ancestor::";
			case AxisType.ANCESTOR_OR_SELF: return "ancestor-or-self::";
			case AxisType.SELF: return "self::";
			case AxisType.FOLLOWING_SIBLING: return "following-sibling::";
			case AxisType.FOLLOWING_SIBLING0: return "following-sibling::";
			case AxisType.PRECEDING_SIBLING: return "preceding-sibling::";
			case AxisType.PRECEDING_SIBLING0: return "preceding-sibling::";
			case AxisType.ATTRIBUTE: return "attribute::";
			default: return "namespace::";

		}

	}
	

}


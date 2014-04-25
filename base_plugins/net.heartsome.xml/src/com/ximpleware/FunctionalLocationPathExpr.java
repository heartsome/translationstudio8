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

import com.ximpleware.xpath.AxisType;
import com.ximpleware.xpath.FuncName;
import com.ximpleware.xpath.Predicate;
import com.ximpleware.xpath.Step;

public class FunctionalLocationPathExpr extends LocationPathExpr {
	public static final int FUNC_PATH=2;
	FuncExpr fne;
	public FunctionalLocationPathExpr(FuncExpr fne1){
		state = START;
		s = null;
		pathType = FUNC_PATH;
		currentStep = null;
		fne = fne1;
		//fib = new FastIntBuffer(8);// page size 256 = 2^ 8
		ih = new intHash();
	}
	public String toString(){
		String st = "";
		Step ts = s;
		st = st + fne;		
		if (ts == null)
			return st;
		else
			return st + '/'+ ts;
	}	
	
	public int evalNodeSet(VTDNav vn) 
    	throws NavException,XPathEvalException{
        int result;
		if (currentStep == null) {
			if (fne.opCode == FuncName.CURRENT) {
				vn.loadCurrentNode();
			}
			currentStep = s;
			if (currentStep == null) {
				if (state == START) {
					state = END;
					return vn.getCurrentIndex2();
				} else {
					return -1;
				}
			}
		}
		
		while (true) {
			switch (currentStep.axis_type) {

			case AxisType.CHILD:
			    if ( (result = process_child(vn))!=-2)
				   return result;
			    break;
			case AxisType.DESCENDANT_OR_SELF:
			case AxisType.DESCENDANT:
			case AxisType.PRECEDING:								
			case AxisType.FOLLOWING:
			    if ((result = process_DDFP(vn))!= -2)
			        return result;
			    break;
			case AxisType.PARENT:
			    if ((result = process_parent(vn))!= -2)
			        return result;
			    break;
			case AxisType.ANCESTOR:
			    if ((result = process_ancestor(vn))!= -2)
			        return result;
			    break;
			case AxisType.ANCESTOR_OR_SELF:
			    if ((result = process_ancestor_or_self(vn))!= -2)
			        return result;
			    break;
			case AxisType.SELF:
			    if ((result = process_self(vn))!= -2)
			        return result;
			    break;
			case AxisType.FOLLOWING_SIBLING:
			    if ((result = process_following_sibling(vn))!= -2)
			        return result;
			    break;
			case AxisType.PRECEDING_SIBLING:
			    if ((result = process_preceding_sibling(vn))!= -2)
			        return result;
			    break;
			case AxisType.ATTRIBUTE:
			    if ((result = process_attribute(vn))!= -2)
			        return result;
			    break;
			default:
				if ((result = process_namespace(vn))!= -2)
			        return result;
			}
		}
        
    }


	
}

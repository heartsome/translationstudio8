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
import com.ximpleware.*;
/**
 * 
 * This is the abstract class on which all XPath expressions 
 * are based
 */

abstract public class Expr {

	abstract public boolean evalBoolean(VTDNav vn);

	abstract public double evalNumber(VTDNav vn);
		
	abstract public int evalNodeSet(VTDNav vn) throws XPathEvalException, NavException;
	
	abstract public String evalString(VTDNav vn);

	abstract public void reset(VTDNav vn);
	abstract public String toString();

	abstract public boolean isNumerical();
	abstract public boolean isNodeSet();
	abstract public boolean isString();
	abstract public boolean isBoolean();
	
	abstract public boolean requireContextSize();
	abstract public void setContextSize(int size);
	
	abstract public void setPosition(int pos);
	abstract public int adjust(int n);
	//protected boolean cacheable;
	
	abstract public boolean isFinal();
	
	
	public void markCacheable(){}
	public void markCacheable2(){}
	//public boolean isConstant(){return false;}
	public void clearCache(){}
	// to support computer context size 
	// needs to add 
	//abstract public boolean needContextSize();
	//abstract public boolean SetContextSize(int contextSize);
    /*final protected int getStringIndex(VTDNav vn){
    	int a = -1;
        vn.push2();
        int size = vn.contextStack2.size;
        try {
            a = evalNodeSet(vn);
            if (a != -1) {
            	int t = vn.getTokenType(a);
                if (t == VTDNav.TOKEN_ATTR_NAME) {
                    a++;
                } else if (vn.getTokenType(a) == VTDNav.TOKEN_STARTING_TAG) {
                    a = -2;
                }else if (t == VTDNav.TOKEN_PI_NAME) {
                    a++;                 
                }
            }
        } catch (Exception e) {

        }
        vn.contextStack2.size = size;
        reset(vn);
        vn.pop2();
        return a;
    }*/
    
    protected int computeDataSize(VTDNav vn){
		int i = vn.context[0];
		if (vn.shallowDepth)
			switch (i) {
			case -1:
			case 0:
				return vn.vtdSize;
			case 1:
				return vn.vtdSize / vn.l1Buffer.size;
			case 2:
				return vn.vtdSize / vn.l2Buffer.size;
			default:
				return vn.vtdSize / vn.l3Buffer.size;
			}
		else {
			VTDNav_L5 vnl = (VTDNav_L5) vn;
			switch (i) {
			case 0:
				return vn.vtdSize;
			case 1:
				return vn.vtdSize / vn.l1Buffer.size;
			case 2:
				return vn.vtdSize / vn.l2Buffer.size;
			case 3:
				return vn.vtdSize / vnl.l3Buffer.size;
			case 4:
				return vnl.vtdSize / vnl.l4Buffer.size;
			default:
				return vnl.vtdSize / vnl.l5Buffer.size;
			}
		}
	}
}

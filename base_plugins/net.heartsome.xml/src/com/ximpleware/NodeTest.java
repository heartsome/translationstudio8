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
//import com.ximpleware.xpath.LocationPathNode;
/**
 * This class is used within LocationPathExpr to represent 
 * Nodetest
 *
 */
public class NodeTest /*implements LocationPathNode*/{
	public String nodeName;
	public String prefix;
	public String localName;
	public String URL;
	boolean nsEnabled;
	public int testType;
	public int type; //0 for *, 1 for node name, 2 for local name +URL
	
	public static final int 
				NAMETEST = 0,
				NODE =1,
				TEXT =2,
				PI0=3,
				PI1 = 4,
				COMMENT = 5;
	public NodeTest(){
		nsEnabled = false;
		localName = null;
	}
	/*public void setNsEnabled(boolean b){
		nsEnabled = b;
	}*/
	final public void setTestType(int t){
		testType = t;
	}
	final public void setNodeName(String s){
		nodeName = s;
		if (s.equals("*"))
			type = 0;
		else
			type = 1;
	}
	final public void setNodeNameNS(String p, String ln){
		prefix = p;
		localName = ln;
		type = 2;
	}
	public boolean eval(VTDNav vn)throws NavException{
		/*if (testType == NODE)
			return true;*/
		//else if(testType == NAMETEST){
		if (vn.atTerminal)
		       return false;
		switch(type){
			case 0: return true;
			case 1: return vn.matchElement(nodeName);
			case 2: return vn.matchElementNS(URL,localName);
		}
		//}
		return false;
	}
	
	public boolean eval2(VTDNav vn)throws NavException{
		switch(testType){
		case NAMETEST:
			if (vn.atTerminal)
		        return false;
			switch(type){
			case 0: return true;
			case 1: return vn.matchElement(nodeName);
			case 2: return vn.matchElementNS(URL,localName);
			}
		case NODE:
			return true;
		case TEXT:
			if (!vn.atTerminal)
		        return false;
			int t = vn.getTokenType(vn.LN);
			if (t== VTDNav.TOKEN_CHARACTER_DATA
					|| t == VTDNav.TOKEN_CDATA_VAL){
				return true;
			}
			return false;
			
		case PI0:
			if (!vn.atTerminal)
				return false;
			if (vn.getTokenType(vn.LN)== VTDNav.TOKEN_PI_NAME){
				return true;
			}
			return false;
		case PI1:
			if (!vn.atTerminal)
				return false;
			if (vn.getTokenType(vn.LN)== VTDNav.TOKEN_PI_NAME){
				return vn.matchTokenString(vn.LN, nodeName);
			}
			return false;
			
		default: // comment
			if (!vn.atTerminal)
				return false;
			if (vn.getTokenType(vn.LN)== VTDNav.TOKEN_COMMENT){
				return true;
			}
			return false;
		}
	}

	final public String toString(){
		switch (testType){
			case NAMETEST :
			    if (localName == null)
			        return nodeName;
			    else 
			        return prefix+":"+localName;
			case NODE: return "node()";
			case TEXT: return "text()";
			case PI0: return "processing-instruction()";
			case PI1: return (nodeName.indexOf('"')>0)?
								"processing-instruction('"+nodeName+"')"
						      :"processing-instruction(\""+nodeName+"\")";
			default:  return "comment()";
		}
	}
	
}

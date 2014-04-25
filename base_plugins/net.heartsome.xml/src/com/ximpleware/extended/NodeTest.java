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
package com.ximpleware.extended;
import com.ximpleware.extended.xpath.LocationPathNode;
/**
 * This class is used within LocationPathExpr to represent 
 * Nodetest
 *
 */
public class NodeTest implements LocationPathNode{
	public String nodeName;
	public String prefix;
	public String localName;
	public String URL;
	boolean nsEnabled;
	public int testType;
	
	public static final int NAMETEST = 0,
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
	public void setTestType(int t){
		testType = t;
	}
	public void setNodeName(String s){
		nodeName = s;
	}
	public void setNodeNameNS(String p, String ln){
		prefix = p;
		localName = ln;
	}
	public boolean eval(VTDNavHuge vn)throws NavExceptionHuge{
		if (testType == NODE)
			return true;
		else if(testType == NAMETEST){
		    if (vn.atTerminal == true)
		        return false;
		    if (localName!=null)
		        return vn.matchElementNS(URL,localName);
		    else 
		        return vn.matchElement(nodeName);
		}
		return false;
	}

	public String toString(){
		switch (testType){
			case NAMETEST :
			    if (localName == null)
			        return nodeName;
			    else 
			        return prefix+":"+localName;
			case NODE: return "node()";
			case TEXT: return "text()";
			case PI0: 
			case PI1: return "processing-instruction()";
			default:  return "comment()";
		}
	}
	
}

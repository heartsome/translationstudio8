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
/**
 * 
 * This class is used by Yylex to detect axis type
 * and transport to the parser
 *
 */
public class AxisType{

	public int i;
	public static final int 		CHILD0=0;// more efficient than child 
	public static final int 		CHILD=1;
	public static final int 		DESCENDANT_OR_SELF0=2;
	public static final int 		DESCENDANT0=3;
	
	public static final int 		PRECEDING0 =4;
	public static final int 		FOLLOWING0 =5;
	public static final int 		DESCENDANT_OR_SELF=6;
	public static final int 		DESCENDANT=7;
	
	public static final int 		PRECEDING =8;
	public static final int 		FOLLOWING =9;
	public static final int 		PARENT= 10;
	public static final int 		ANCESTOR =11;
	
	public static final int 		ANCESTOR_OR_SELF =12;
	public static final int 		SELF	=13;
	public static final int 		FOLLOWING_SIBLING =14;
	public static final int 		FOLLOWING_SIBLING0 =15;
	public static final int 	 	PRECEDING_SIBLING=16;
	public static final int 	 	PRECEDING_SIBLING0=17;
	
	public static final int 	    ATTRIBUTE = 18;	
	public static final int 		NAMESPACE =19;
	
	
	
	

	public AxisType (){
	}
	
	final public String getAxisString(){
	switch (i){
		case CHILD0:
		case CHILD: return "child::";
		case DESCENDANT_OR_SELF0: return "descendant-or-self::";
		case DESCENDANT0: return "descendent::";
		case PRECEDING0: return "preceding::";
		case FOLLOWING0: return "following::";
		case DESCENDANT_OR_SELF: return "descendant-or-self::";
		case DESCENDANT: return "descendent::";
		case PRECEDING: return "preceding::";
		case FOLLOWING: return "following::";
		case PARENT: return "parent::";
		case ANCESTOR: return "ancestor::";
		case ANCESTOR_OR_SELF: return "ancestor-or-self::";
		case SELF: return "self::";
		case FOLLOWING_SIBLING: return "following-sibling::";
		case FOLLOWING_SIBLING0: return "following-sibling::";
		case PRECEDING_SIBLING: return "preceding-sibling::";	
		case PRECEDING_SIBLING0: return "preceding-sibling::";	
		case ATTRIBUTE: return "attribute::";
		default: return "namespace::";			
	}
  }

}

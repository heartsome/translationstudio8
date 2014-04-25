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
 * This class is used by lexer to detect built-in functions
 * then transport to the parser 
 */
public class FuncName{
	public int i;
	public static final int 	LAST = 0;
	public static final int		POSITION =1;
	public static final int		COUNT = 2;
	public static final int		LOCAL_NAME =3;
	public static final int		NAMESPACE_URI =4;
	public static final int		NAME=5;
	public static final int		STRING=6;
	public static final int		CONCAT = 7;
	public static final int		STARTS_WITH = 8;
	public static final int		CONTAINS = 9;
	public static final int		SUBSTRING_BEFORE = 10;
	public static final int		SUBSTRING_AFTER =11;
	public static final int		SUBSTRING = 12;
	public static final int		STRING_LENGTH =13;
	public static final int		NORMALIZE_SPACE =14;
	public static final int		TRANSLATE =15;
	public static final int		BOOLEAN =16;
	public static final int		NOT = 17;
	public static final int		TRUE =18;
	public static final int		FALSE =19;
	public static final int		LANG = 20;
	public static final int		NUMBER = 21;
	public static final int		SUM = 22;
	public static final int		FLOOR =23;
	public static final int		CEILING = 24;
	public static final int		ROUND = 25;
	// added for 2.0
	public static final int     ABS = 26;
	public static final int     ROUND_HALF_TO_EVEN = 27;
	public static final int 	ROUND_HALF_TO_ODD = 28;
	public static final int     CODE_POINTS_TO_STRING = 29;
	public static final int     COMPARE = 30;
	
	public static final int     UPPER_CASE = 31;
	public static final int     LOWER_CASE = 32;
	public static final int     ENDS_WITH = 33;
	public static final int     QNAME = 34;
	public static final int     LOCAL_NAME_FROM_QNAME = 35;
	public static final int     NAMESPACE_URI_FROM_QNAME = 36;
	public static final int     NAMESPACE_URI_FOR_PREFIX = 37;
	public static final int     RESOLVE_QNAME = 38;
	public static final int     IRI_TO_URI = 39;
	public static final int     ESCAPE_HTML_URI = 40;
	public static final int     ENCODE_FOR_URI = 41;
	public static final int     MATCH_NAME = 42;
	public static final int     MATCH_LOCAL_NAME = 43;
	public static final int     NOT_MATCH_NAME = 44;
	public static final int     NOT_MATCH_LOCAL_NAME = 45;
	public static final int		CURRENT =46;
	public static final int 	GENERATE_ID = 47;
	public static final int 	FORMAT_NUMBER = 48;
 	public static final int     KEY = 49;
 	public static final int     ID =50;
 	public static final int     DOCUMENT =51;
 	public static final int     SYSTEM_PROPERTY =52;
 	public static final int 	ELEMENT_AVAILABLE =53;
 	public static final int 	FUNCTION_AVAILABLE = 54;
	
//	String getFuncString(){
//	switch(i){
//	case FuncName.LAST: 			return "last";
//	case FuncName.POSITION: 		return "position";
//	case FuncName.COUNT: 			return "count";
//	case FuncName.LOCAL_NAME: 		return "local-name";
//	case FuncName.NAMESPACE_URI: 	return "namespace-uri";
//	case FuncName.NAME: 			return "name";
//	case FuncName.STRING: 			return "string";
//	case FuncName.CONCAT: 			return "concat";
//	case FuncName.STARTS_WITH:		return "starts-with";
//	case FuncName.CONTAINS: 		return "contains";
//	case FuncName.SUBSTRING_BEFORE: return "substring-before";
//	case FuncName.SUBSTRING_AFTER: 	return "substring-after";
//	case FuncName.SUBSTRING: 		return "substring";
//	case FuncName.STRING_LENGTH: 	return "string-length";
//	case FuncName.NORMALIZE_SPACE: 	return "normalize-space";
//	case FuncName.TRANSLATE:	 	return "translate";
//	case FuncName.BOOLEAN: 			return "boolean";
//	case FuncName.NOT: 				return "not";
//	case FuncName.TRUE: 			return "true";
//	case FuncName.FALSE: 			return "false";
//	case FuncName.LANG: 			return "lang";
//	case FuncName.NUMBER:			return "number";
//	case FuncName.SUM: 			 	return "sum";
//	case FuncName.FLOOR: 			return "floor";
//	case FuncName.CEILING: 			return "ceiling";
//	case FuncName.ROUND:			return "round";
////	 added for 2.0
//	case FuncName.ABS:				return "abs";
//	case FuncName.ROUND_HALF_TO_EVEN :											
//									return "round-half-to-even";
//	case FuncName.ROUND_HALF_TO_ODD:
//	    							return "round-half-to-odd";
//	case FuncName.CODE_POINTS_TO_STRING:
//	    							return "code-points-to-string";
//	case FuncName.COMPARE:			return "compare";
//	case FuncName.UPPER_CASE:		return "upper-case";
//	case FuncName.LOWER_CASE:		return "lower-case";
//	case FuncName.ENDS_WITH:		return "ends-with";
//	case FuncName.QNAME:			return "QName";
//	case FuncName.LOCAL_NAME_FROM_QNAME:
//									return "local-name-from-QName";
//	case FuncName.NAMESPACE_URI_FROM_QNAME:
//									return "namespace-uri-from-QName";
//	case FuncName.NAMESPACE_URI_FOR_PREFIX:
//	    							return "namespace-uri-for-prefix";
//	case FuncName.RESOLVE_QNAME:	return "resolve-QName";
//	case FuncName.IRI_TO_URI:    	return "iri-to-uri";
//	case FuncName.ESCAPE_HTML_URI:	return "escape-html-uri";
//	default:						return "encode-for-uri";
//	}
//}

}

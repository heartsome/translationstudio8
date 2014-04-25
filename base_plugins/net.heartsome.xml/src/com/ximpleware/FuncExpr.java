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
import com.ximpleware.xpath.*;

/**
 * FuncExpr implements the function expression defined
 * in XPath spec
 * 
 */
public class FuncExpr extends Expr{

	public Alist argumentList;
	public int opCode;
	boolean isNumerical;
	boolean isBoolean;
	boolean isString;
	boolean isNodeSet;
	int contextSize;
	VTDNav newVN,xslVN;
	// double d;
	int position;
	int argCount;
	int a;
	int state;
	VTDGen vg;
	String s;
	//VTDNav vn;
	public static final int START = 0, // initial state
			END = 1, // return to begin
			TERMINAL = 2, // no more next step
			FORWARD = 3, //
			BACKWARD = 4;

	VTDNav getNewNav(){return newVN;}
	
	void setXslVn(VTDNav vn1){xslVN=vn1;}
	int argCount(){
		Alist temp = argumentList;
		int count = 0;
		while(temp!=null){
			count++;
			temp = temp.next;
		}
		return count;
	}
	public FuncExpr(int oc , Alist list){
		a = 0;
	  opCode = oc;
	  argumentList = list;
	  isBoolean = false;
	  isString  = false;
	  position = 0;
	  isNodeSet = false;
	  isNumerical = false;
	  argCount=argCount();
	 // cacheable =false;
	  switch(opCode){
			case FuncName.LAST: 			isNumerical = true;break;
			case FuncName.POSITION: 		isNumerical = true;break;
			case FuncName.COUNT: 			isNumerical = true;break;
			
			case FuncName.LOCAL_NAME: 		isString = true; break;
			case FuncName.NAMESPACE_URI: 	isString = true; break;
			case FuncName.NAME: 			isString = true; break;
			case FuncName.STRING: 			isString = true; break;
			case FuncName.CONCAT: 			isString = true; break;
			case FuncName.STARTS_WITH:		isBoolean= true;break;
			case FuncName.CONTAINS: 		isBoolean= true;break;
			case FuncName.SUBSTRING_BEFORE: isString = true; break;
			case FuncName.SUBSTRING_AFTER: 	isString = true; break;
			case FuncName.SUBSTRING: 		isString = true; break;
			case FuncName.STRING_LENGTH: 	isNumerical = true;break;
			case FuncName.NORMALIZE_SPACE: 	isString = true; break;
			case FuncName.TRANSLATE:	 	isString = true;break;
			case FuncName.BOOLEAN: 			isBoolean =true;break;
			case FuncName.NOT: 			    isBoolean =true;break;
			case FuncName.TRUE: 			isBoolean = true;break;
			case FuncName.FALSE: 			isBoolean = true;break;
			case FuncName.LANG: 			isBoolean = true;break;
			case FuncName.NUMBER:			isNumerical = true;break;
			case FuncName.SUM: 			    isNumerical = true;break;
			case FuncName.FLOOR: 			isNumerical = true;break;
			case FuncName.CEILING: 			isNumerical = true;break;
			case FuncName.ROUND:			isNumerical = true;break;
			case FuncName.ABS:				isNumerical = true;break;
			case FuncName.ROUND_HALF_TO_EVEN :
			    							isNumerical = true;break;
			case FuncName.ROUND_HALF_TO_ODD:
			    							isNumerical = true;break;
			case FuncName.CODE_POINTS_TO_STRING:
			    							isString = true; break;
			case FuncName.COMPARE:			isBoolean= true;break;
			case FuncName.UPPER_CASE:		isString = true; break;
			case FuncName.LOWER_CASE:		isString = true; break;
			case FuncName.ENDS_WITH:		isBoolean= true;break;
			case FuncName.QNAME:			isString = true; break;
			case FuncName.LOCAL_NAME_FROM_QNAME:
			    							isString = true; break;
			case FuncName.NAMESPACE_URI_FROM_QNAME:
			    							isString = true; break;
			case FuncName.NAMESPACE_URI_FOR_PREFIX:
			    							isString = true; break;
			case FuncName.RESOLVE_QNAME:	isString = true; break;
			case FuncName.IRI_TO_URI:    	isString = true; break;
			case FuncName.ESCAPE_HTML_URI:	isString = true; break;
			case FuncName.ENCODE_FOR_URI:	isString = true; break;
			case FuncName.MATCH_NAME:		isBoolean =true; break;
			case FuncName.MATCH_LOCAL_NAME: isBoolean=true;break;
			case FuncName.NOT_MATCH_NAME:		isBoolean =true; break;
			case FuncName.NOT_MATCH_LOCAL_NAME: isBoolean=true;break;
			case FuncName.GENERATE_ID : 	isString = true; break;
			case FuncName.FORMAT_NUMBER:  	isString = true;break;
			case FuncName.KEY:				isNodeSet = true; state = START; vg = new VTDGen(); break;
			case FuncName.DOCUMENT:			isNodeSet = true; state = START; vg = new VTDGen();break;
			case FuncName.CURRENT:			isNodeSet = true; state = START; vg = new VTDGen();break;
			case FuncName.SYSTEM_PROPERTY: 	isString = true; break;
			case FuncName.ELEMENT_AVAILABLE: isBoolean = true; break;
			case FuncName.FUNCTION_AVAILABLE: isBoolean = true; break;
			//case FuncName.
			//default:			isNumerical = true; break;
	  }	  
	}
	final public boolean checkArgumentCount(){
		switch(opCode){
		case FuncName.LAST: 			return argCount==0;
		case FuncName.POSITION: 		return argCount==0;
		case FuncName.COUNT: 			return (argCount==1 && argumentList.e.isNodeSet());
		
		case FuncName.LOCAL_NAME: 		return (argCount==0 ||(argCount==1 && argumentList.e.isNodeSet()));
		case FuncName.NAMESPACE_URI: 	return (argCount==0 ||(argCount==1 && argumentList.e.isNodeSet()));
		case FuncName.NAME: 			return (argCount==0 ||(argCount==1 && argumentList.e.isNodeSet()));
		case FuncName.STRING: 			return argCount < 2;
		case FuncName.CONCAT: 			return argCount > 1;
		case FuncName.STARTS_WITH:		return argCount ==2;
		case FuncName.CONTAINS: 		return argCount ==2;
		case FuncName.SUBSTRING_BEFORE: return argCount==2;
		case FuncName.SUBSTRING_AFTER: 	return argCount==2;
		case FuncName.SUBSTRING: 		return argCount==2 || argCount==3;
		case FuncName.STRING_LENGTH: 	return argCount<2;
		case FuncName.NORMALIZE_SPACE: 	return argCount <2;
		case FuncName.TRANSLATE:	 	return argCount ==3;
		case FuncName.BOOLEAN: 			return argCount ==1;
		case FuncName.NOT: 			    return argCount ==1;
		case FuncName.TRUE: 			return argCount ==0;
		case FuncName.FALSE: 			return argCount ==0;
		case FuncName.LANG: 			return (argCount==1);
		case FuncName.NUMBER:			return argCount==1;
		case FuncName.SUM: 			    return (argCount==1 && argumentList.e.isNodeSet());
		case FuncName.FLOOR: 			return argCount==1;
		case FuncName.CEILING: 			return argCount==1;
		case FuncName.ROUND:			return argCount==1;
		case FuncName.ABS:				return argCount==1;
		case FuncName.ROUND_HALF_TO_EVEN :
										return argCount==1 || argCount == 2;
		case FuncName.ROUND_HALF_TO_ODD:
										return argCount==1 || argCount == 2;
		case FuncName.CODE_POINTS_TO_STRING:
		    							break;
		case FuncName.COMPARE:			break;
		case FuncName.UPPER_CASE:		return argCount==1;
		case FuncName.LOWER_CASE:		return argCount==1;
		case FuncName.ENDS_WITH:		return argCount==2;
		case FuncName.QNAME:			break;
		case FuncName.LOCAL_NAME_FROM_QNAME:
		    							break;
		case FuncName.NAMESPACE_URI_FROM_QNAME:
		    							break;
		case FuncName.NAMESPACE_URI_FOR_PREFIX:
		    							break;
		case FuncName.RESOLVE_QNAME:	break;
		case FuncName.IRI_TO_URI:    	break;
		case FuncName.ESCAPE_HTML_URI:	break;
		case FuncName.ENCODE_FOR_URI:	break;
		case FuncName.MATCH_NAME:		return argCount==1 || argCount == 2;
		case FuncName.MATCH_LOCAL_NAME: return argCount==1 || argCount == 2;
		case FuncName.NOT_MATCH_NAME:		return argCount==1 || argCount == 2;
		case FuncName.NOT_MATCH_LOCAL_NAME: return argCount==1 || argCount == 2;
		case FuncName.CURRENT:			return argCount==0;
		case FuncName.GENERATE_ID : 	return argCount==0 || (argCount ==1 && argumentList.e.isNodeSet());
		case FuncName.FORMAT_NUMBER:  	return argCount==2 || argCount == 3;
		case FuncName.KEY:				return argCount==2;
		case FuncName.DOCUMENT:			return argCount==1 || (argCount==2 && argumentList.next.e.isNodeSet());	
		case FuncName.SYSTEM_PROPERTY: 	return argCount==1 && argumentList.e.isString() ;
		case FuncName.ELEMENT_AVAILABLE: return argCount==1 && argumentList.e.isString();
		case FuncName.FUNCTION_AVAILABLE: return argCount==1 && argumentList.e.isString();
		}
		return false;		
	}
	
	private String getSystemProperty(VTDNav vn){
		String s = argumentList.e.evalString(vn);
		return "";
	}

	private boolean isElementAvailable(VTDNav vn){
		String s = argumentList.e.evalString(vn);
		return false;
	}
	
	private boolean isFunctionAvailable(VTDNav vn){
		String s = argumentList.e.evalString(vn);
		return false;
	}
	final public String toString(){
	  if (argumentList == null)
		  return fname()+" ("+")";
	  return fname()+" ("+argumentList +")";
	}
	
	private String formatNumber(VTDNav vn){
		return "";
	}
	
	private String getLocalName(VTDNav vn){
	    if (argCount== 0){
	        try{
	            int index = vn.getCurrentIndex();
	            int type = vn.getTokenType(index);
	            if (vn.ns && (type == VTDNav.TOKEN_STARTING_TAG 
	                    || type == VTDNav.TOKEN_ATTR_NAME)) {
                    int offset = vn.getTokenOffset(index);
                    int length = vn.getTokenLength(index);
                    if (length < 0x10000){
                    	if (vn.localNameIndex != index){
                    		vn.localNameIndex = index;
                    		vn.localName = vn.toRawString(index);
                    	}
                        return vn.localName;
                    }
                    else {
                        int preLen = length >> 16;
                        int QLen = length & 0xffff;
                        if (preLen != 0){
                        	if (vn.localNameIndex != index){
                        		vn.localNameIndex = index;
                        		vn.localName = vn.toRawString(offset + preLen+1, QLen
                                        - preLen - 1);
                        	}
                            return vn.localName;
                        }
                        else {
                        	if (vn.localNameIndex != index){
                        		vn.localNameIndex = index;
                        		vn.localName = vn.toRawString(offset, QLen);
                        	}
                            return vn.localName;
                        }
                    }
                } else if (type == VTDNav.TOKEN_PI_NAME){
                	//int offset = vn.getTokenOffset(index);
                    //int length = vn.getTokenLength(index);
                    if (vn.localNameIndex != index){
                		vn.localNameIndex = index;
                		vn.localName = vn.toRawString(index);
                	}
                    return vn.localName;
                }else
                
                    return "";
	        }catch(NavException e){
	            return ""; // this will almost never occur
	        }
	        
	    } else if (argCount == 1){
	        int a=evalFirstArgumentListNodeSet2(vn);
			if (a == -1 || vn.ns == false)
			    return "";
			int type = vn.getTokenType(a);
			/*if (type!=VTDNav.TOKEN_STARTING_TAG && type!= VTDNav.TOKEN_ATTR_NAME)
			    return "";*/
			if (type == VTDNav.TOKEN_STARTING_TAG || type== VTDNav.TOKEN_ATTR_NAME){
			try {			    
			    int offset = vn.getTokenOffset(a);
			    int length = vn.getTokenLength(a);
			    if (length < 0x10000)
			        return vn.toRawString(a);
			    else {
			        int preLen = length >> 16;
			        int QLen = length & 0xffff;
			        if (preLen != 0)
			            return vn.toRawString(offset + preLen+1, 
			                    QLen - preLen - 1);
			        else {
			            return vn.toRawString(offset, QLen);
			        }
			    }
			} catch (NavException e) {
			    return ""; // this will almost never occur
			}		
			}else if (type == VTDNav.TOKEN_PI_NAME){
				try{
					return vn.toRawString(a);
				}catch(NavException e){
					return "";
				}
			}else 
				return "";
	    } else 
	        throw new IllegalArgumentException
			("local-name()'s argument count is invalid");
	}
	
	private String getNameSpaceURI(VTDNav vn){
	    if (argCount==0){
	        try{
	            int i = vn.getCurrentIndex();
	            int type = vn.getTokenType(i);
	            
                if (vn.ns && (type == VTDNav.TOKEN_STARTING_TAG 
	                    || type == VTDNav.TOKEN_ATTR_NAME)) {
                    int a = vn.lookupNS();
                    if (a == 0)
                        return "";
                    else
                        return vn.toString(a);
                }
	            return "";
	        }catch (Exception e){
	            return "";
	        }
	    }else if (argCount==1 && argumentList.e.isNodeSet()){
	    	vn.push2();
	        int size = vn.contextStack2.size;
	        int a = -1;
	        try {
	            a = argumentList.e.evalNodeSet(vn);	            
	        } catch (Exception e) {
	        }
	        String s="";
	       // return a;
			try {
                if (a == -1 || vn.ns == false)
                   ;
                else {
                    int type = vn.getTokenType(a);
                    if (type == VTDNav.TOKEN_STARTING_TAG
                            || type == VTDNav.TOKEN_ATTR_NAME)
                       s= vn.toString(vn.lookupNS());
                    
                }                
            } catch (Exception e){} ;
            vn.contextStack2.size = size;
	        argumentList.e.reset(vn);
	        vn.pop2();
            return s;
			
	    }else 
	        throw new IllegalArgumentException
			("namespace-uri()'s argument count is invalid");
	}
	
	private String getName(VTDNav vn){
	    int a;
	    if (argCount==0){
	        a = vn.getCurrentIndex();
	        int type = vn.getTokenType(a);
            
            if (type == VTDNav.TOKEN_STARTING_TAG 
                    || type == VTDNav.TOKEN_ATTR_NAME 
                    || type == VTDNav.TOKEN_PI_NAME){
	            try{
	            	if (vn.nameIndex!=a){
	            		vn.name = vn.toRawString(a);
	            		vn.nameIndex = a;	
	            	}
            		return vn.name;	                
	            }catch(Exception e){
	                return "";
	            }            
	        }
	        else 
	            return "";
	    } else if (argCount == 1){
	        a = evalFirstArgumentListNodeSet2(vn);
			try {
                if (a == -1 || vn.ns == false)
                    return "";
                else {
                    int type = vn.getTokenType(a);
                    if (type == VTDNav.TOKEN_STARTING_TAG
                            || type == VTDNav.TOKEN_ATTR_NAME 
                            || type == VTDNav.TOKEN_PI_NAME)
                        return vn.toRawString(a);
                    return "";
                    
                }
            } catch (Exception e) {
            }			
			return "";
	    }else 
	        throw new IllegalArgumentException
			("name()'s argument count is invalid");
	        
	}
	// ISO 639 
	// http://www.loc.gov/standards/iso639-2/php/English_list.php
	// below are defined two-letter words
	// 
	// ab , aa , af, ak, sq, am, ar, an, hy, as, av, ae, ay, az, bm
	// ba , eu , be, bn, bh, bi, nb, bs, br, bg, my, es, ca, km, ch
	// ce , ny , zh, za, cu, cv, kw, co, cr, hr, cs, da, dv, dv, nl
	// dz , en , eo, et, ee, fo, fj, fi, nl, fr, ff, gd, gl, lg, ka
	// de , ki , el, kl, gn, gu, ht, ha, he, hz, hi, ho, hu, is, io
	// ig , id , ia, ie, iu, ik, ga, it, ja, jv, kl, kn, kr, ks, kk
	// ki , rw , ky, kv, kg, ko, kj, ku, kj, ky, lo, la, lv, lb, li
	// ln , lt , lu, lb, mk, mg, ms, ml, dv, mt, gv, mi, mr, mh, mo
	// mn , na , nv, nv, nd, nr, ng, ne, nd, se, no, nb, nn, ii, nn,
	// ie , oc , oj, cu, or, om, os, pi, pa, ps, fa, pl, pt, oc, pa,
	// ps , qu , ro, rm, rn, ru, sm, sa, sc, gd, sr, sn, ii, si, sk,
	// sl , so , st, nr, es, su, sw, ss, sv, tl, ty, tg, ta, tt, te, 
	// th , bo , ti, to, ts, tn, tr, tk, tw, ug, uk, ur, ug, uz, ca,
	// ve , vi , vo, wa, cy, fy, wo, xh, yi, yo, za, zu
	
	private boolean lang(VTDNav vn, String s){
	    // check the length of s 
	    boolean b = false;
	    vn.push2();
        try {
            while (vn.getCurrentDepth() >= 0) {
                int i = vn.getAttrVal("xml:lang");
                if (i!=-1){
                    b = vn.matchTokenString(i,s);
                    break;                    
                }
                vn.toElement(VTDNav.P);
            }
        } catch (NavException e) {

        }
	    vn.pop2();
	    return b;
	}
	private boolean startsWith(VTDNav vn){
		String s2 = argumentList.next.e.evalString(vn);
		if (argumentList.e.isNodeSet()){
			//boolean b = false;
			int a = evalFirstArgumentListNodeSet(vn);
			
	        if (a==-1)
	        	return "".startsWith(s2);
	        else{
	        	try{
	        		int t = vn.getTokenType(a);
	        		if (t!=VTDNav.TOKEN_STARTING_TAG&&t!=VTDNav.TOKEN_DOCUMENT)
	        			return vn.startsWith(a, s2);
	        		else 
	        			return vn.XPathStringVal_StartsWith(a,s2);
	        	}catch(Exception e){
	        	}
	        	return false;
	        }								
		} 
	    String s1 = argumentList.e.evalString(vn);
	    return s1.startsWith(s2); 
	}
	
	private int evalFirstArgumentListNodeSet(VTDNav vn){
		vn.push2();
        int size = vn.contextStack2.size;
        int a = -1;
        try {
            a = argumentList.e.evalNodeSet(vn);
            if (a != -1) {
            	int t = vn.getTokenType(a);
                if (t == VTDNav.TOKEN_ATTR_NAME) {
                    a++;
                }
                /*else if (t == VTDNav.TOKEN_STARTING_TAG) {
                   // a = vn.getText();
                }*/else if (t == VTDNav.TOKEN_PI_NAME){
                	//if (a+1 < vn.vtdSize || vn.getTokenType(a+1)==VTDNav.TOKEN_PI_VAL)
                	a++;
                	//else 
                	//	a=-1;
                }
                 
                //else if (t== VTDNav.T)
            }	            
        } catch (Exception e) {
        }
        vn.contextStack2.size = size;
        argumentList.e.reset(vn);
        vn.pop2();
        return a;
	}
	
	private int evalFirstArgumentListNodeSet2(VTDNav vn){
		vn.push2();
        int size = vn.contextStack2.size;
        int a = -1;
        try {
            a = argumentList.e.evalNodeSet(vn);	            
        } catch (Exception e) {
        }
        vn.contextStack2.size = size;
        argumentList.e.reset(vn);
        vn.pop2();
        return a;
	}
	
	private boolean endsWith(VTDNav vn){
		String s2 = argumentList.next.e.evalString(vn);
		if (argumentList.e.isNodeSet()){
			int a = evalFirstArgumentListNodeSet(vn);
	        if (a==-1)
	        	return "".startsWith(s2);
	        else{
	        	try{
	        		int t=vn.getTokenType(a);
	        		if (t!=VTDNav.TOKEN_STARTING_TAG && t!=VTDNav.TOKEN_DOCUMENT)
	        			return vn.endsWith(a, s2);
	        		else
	        			return vn.XPathStringVal_EndsWith(a, s2);
	        	}catch(Exception e){
	        	}
	        	return false;
	        }								
		}	
	    String s1 = argumentList.e.evalString(vn);
	    return s1.endsWith(s2); 
	}
	
		
	
	private boolean contains(VTDNav vn){
		String s2 = argumentList.next.e.evalString(vn);
		if (argumentList.e.isNodeSet()){
			int a = evalFirstArgumentListNodeSet(vn);
			if (a==-1)
				return false;
			try {
				int t=vn.getTokenType(a);
				if (t!=VTDNav.TOKEN_STARTING_TAG && t!=VTDNav.TOKEN_DOCUMENT)
					return vn.contains(a, s2);
				else
					return vn.XPathStringVal_Contains(a,s2);
			}catch (Exception e){
				return false;
			}				
		} 
	    String s1 = argumentList.e.evalString(vn);
	    //return s1.contains(s2);
	    return s1.indexOf(s2)!=-1;
	    //return (s1.i))
	}
	
	private String subString(VTDNav vn){
	    if (argCount== 2){
	        String s = argumentList.e.evalString(vn);
	        double d1 = Math.floor(argumentList.next.e.evalNumber(vn)+0.5d);
	        if (d1!=d1 || d1>s.length())    
	            return "";
	        return s.substring(Math.max((int)(d1-1),0));
	    } else if (argCount == 3){
	        String s = argumentList.e.evalString(vn);
            double d1 = Math.floor(argumentList.next.e.evalNumber(vn) + 0.5d);
            double d2 = Math
                    .floor(argumentList.next.next.e.evalNumber(vn) + 0.5d);
            //int i1 = Math.max(0, (int) d1 - 1);
            if ((d1 + d2) != (d1 + d2) || d1 > s.length())
                return "";
            return s.substring(Math.max(0, (int) d1 - 1), Math.min(s.length(),
                    (int) (d1 - 1) + (int) d2));
            //(int) argumentList.next.next.e.evalNumber(vn)-1);
	       
	    }
	    throw new IllegalArgumentException
		("substring()'s argument count is invalid");
	}
	private String subStringBefore(VTDNav vn){
	    if (argCount==2){
	        String s1 = argumentList.e.evalString(vn);
	        String s2 = argumentList.next.e.evalString(vn);
	        int len1 = s1.length();
	        int len2 = s2.length();
	        for (int i=0;i<len1;i++){
	            if (s1.regionMatches(i,s2,0,len2))
	                return s1.substring(0,i);
	        }
	        return "";
	    }
	    throw new IllegalArgumentException
		("substring()'s argument count is invalid");
	}
	private String subStringAfter(VTDNav vn){
	    if (argCount==2){
	        String s1 = argumentList.e.evalString(vn);
	        String s2 = argumentList.next.e.evalString(vn);
	        int len1 = s1.length();
	        int len2 = s2.length();
	        for (int i=0;i<len1;i++){
	            if (s1.regionMatches(i,s2,0,len2))
	                return s1.substring(i+len2);
	        }
	        return "";	        
	    }
	    throw new IllegalArgumentException
		("substring()'s argument count is invalid");
	}
	private String translate(VTDNav vn)
	{
		
		//if (argCount == 3) {
			String resultStr = argumentList.e.evalString(vn);
			String indexStr = argumentList.next.e.evalString(vn);

			if (resultStr == null || resultStr.length() == 0
					|| indexStr == null || indexStr.length() == 0)
				return resultStr;
			String replace = argumentList.next.next.e.evalString(vn);
			StringBuilder usedCharStr = new StringBuilder();
			int lenRep = (replace != null) ? replace.length() : 0;
			for (int i = 0; i < indexStr.length(); i++) {
				char idxChar = indexStr.charAt(i);
				if (usedCharStr.indexOf(String.valueOf(idxChar)) < 0) {
					if (i < lenRep) {
						resultStr = resultStr.replace(idxChar,
								replace.charAt(i));
					} else {
						resultStr = resultStr.replaceAll(
								String.valueOf(idxChar), "");
					}
					usedCharStr.append(idxChar);
				}
			}
			return resultStr;
		/*} else {
			throw new IllegalArgumentException(
					"Argument count for translate() is invalid. Expected: 3; Actual: "
							+ argCount);
		}*/
	}
	
	private String normalizeSpace(VTDNav vn){
	    if (argCount== 0){
	        String s =null;
	        try{
	            if (vn.atTerminal){
	                int ttype = vn.getTokenType(vn.LN);
	                if (ttype == VTDNav.TOKEN_CDATA_VAL )
	                    s= vn.toRawString(vn.LN);
	                else if (ttype == VTDNav.TOKEN_ATTR_NAME ||
	                         ttype == VTDNav.TOKEN_ATTR_NS){
	                    s = vn.toNormalizedString(vn.LN+1);
	                } else
	                    s= vn.toNormalizedString(vn.LN);	                
	            }else {
	            	int i = vn.getCurrentIndex();
	            	int t = vn.getTokenType(i);
	            	if (t==VTDNav.TOKEN_STARTING_TAG || t==VTDNav.TOKEN_DOCUMENT){
	            		s = vn.toNormalizedXPathString(i);
	            	}else
	                s= vn.toNormalizedString(i);
	            }
	            return s;
	        }
	    	catch(NavException e){
	    	    return ""; // this will almost never occur
	    	}
	    } else if (argCount == 1){
	    	String s="";
	    	if (argumentList.e.isNodeSet()){
				//boolean b = false;
				int a = evalFirstArgumentListNodeSet(vn);
		        if (a==-1)
		        	return ""; 
		        else {		        	
		        	try{
		        		int t = vn.getTokenType(a);
		        		if (t==VTDNav.TOKEN_STARTING_TAG || t==VTDNav.TOKEN_DOCUMENT){
		        			s =  vn.toNormalizedXPathString(a);
		        		}else
		        			s = vn.toNormalizedString(a); 
		        	} catch (Exception e){
		        	}
		        	return s;	
		        }	    	
	    	}
	    	else {
	    		s = argumentList.e.evalString(vn);
		        return normalize(s);
	    	}
	        
	    }
	    throw new IllegalArgumentException
		("normalize-space()'s argument count is invalid");
	    //return null;
	}
	
	private String normalize(String s){
	    int len = s.length();
        StringBuffer sb = new StringBuffer(len);
        int i=0;
        // strip off leading ws
        for(i=0;i<len;i++){	            
            if (isWS(s.charAt(i))){
            }else{
                break;
            }
        }
        while(i<len){
            char c = s.charAt(i);
            if (!isWS(c)){
                sb.append(c);
                i++;
            } else {
                while(i<len){
                    c = s.charAt(i);
                    if (isWS(c))
                      i++;
                    else 
                        break;
                }
                if (i<len)
                  sb.append(' ');	                    
            }
        }
        return sb.toString();
	}
	
	private boolean isWS(char c){
	    if (c==' ' || c=='\t' || c=='\r'||c=='\n')
	        return true;
	    return false;
	}
	
	private String concat(VTDNav vn){
	    StringBuffer  sb = new StringBuffer();
	    //if (argCount>=2){
			Alist temp = argumentList;
			while(temp!=null){
				sb.append(temp.e.evalString(vn));
				temp = temp.next;
			}
			return sb.toString();
	    //} else 
	    //    throw new IllegalArgumentException
		//("concat()'s argument count is invalid");
	}
	
	private String getString(VTDNav vn){
	    if (argCount== 0)
	        try{
	            if (vn.atTerminal){
	                if (vn.getTokenType(vn.LN) == VTDNav.TOKEN_CDATA_VAL )
	                    return vn.toRawString(vn.LN);
	                return vn.toString(vn.LN);
	            }
	            return vn.getXPathStringVal();
	        }
	    	catch(NavException e){
	    	    return ""; // this will almost never occur
	    	}
	    else if (argCount == 1){
	        return argumentList.e.evalString(vn);
	    } else 
	        throw new IllegalArgumentException
			("String()'s argument count is invalid");
	}
	
	final public String evalString(VTDNav vn) throws UnsupportedException{
	   // int d=0;
	  switch(opCode){
	  		case FuncName.CONCAT:
	  		    return concat(vn);
	  		    //throw new UnsupportedException("Some functions are not supported");
	  		    
			case FuncName.LOCAL_NAME:
			    return getLocalName(vn);

			case FuncName.NAMESPACE_URI: 
			    return getNameSpaceURI(vn);

			case FuncName.NAME: 		
			    return getName(vn);

			case FuncName.STRING:
			    return getString(vn);

			case FuncName.SUBSTRING_BEFORE:	return subStringBefore(vn);	
			case FuncName.SUBSTRING_AFTER: 	return subStringAfter(vn);
			case FuncName.SUBSTRING: 	return subString(vn);	
			case FuncName.TRANSLATE: 	return translate(vn);
			case FuncName.NORMALIZE_SPACE: return normalizeSpace(vn);
			case FuncName.CODE_POINTS_TO_STRING:	throw new com.ximpleware.xpath.UnsupportedException("not yet implemented");		
			case FuncName.UPPER_CASE: return upperCase(vn);
			case FuncName.LOWER_CASE: return lowerCase(vn);
			case FuncName.QNAME:		
			case FuncName.LOCAL_NAME_FROM_QNAME:				
			case FuncName.NAMESPACE_URI_FROM_QNAME:				
			case FuncName.NAMESPACE_URI_FOR_PREFIX:				
			case FuncName.RESOLVE_QNAME:	
			case FuncName.IRI_TO_URI:    	
			case FuncName.ESCAPE_HTML_URI:	
			case FuncName.ENCODE_FOR_URI:
			    throw new com.ximpleware.xpath.UnsupportedException("not yet implemented");
			case FuncName.GENERATE_ID: return generateID(vn);
			case FuncName.FORMAT_NUMBER: return formatNumber(vn);
			case FuncName.SYSTEM_PROPERTY: return getSystemProperty(vn);
			default: if (isBoolean()){
			    		if (evalBoolean(vn)== true)
			    		    return "true";
			    		else 
			    		    return "false";
					 } else {
						 double tmp = evalNumber(vn);
						 if (tmp - ((int)tmp) ==0 )
							 return ""+ (int)tmp;
						 else
							 return ""+ tmp;
					 }
	  }
	}	
	
	
	final public double evalNumber(VTDNav vn){
	    int ac = 0;
	  switch(opCode){
			case FuncName.LAST:  /*if (argCount!=0 )
									throw new IllegalArgumentException
									("floor()'s argument count is invalid");*/
								 return contextSize;			
			case FuncName.POSITION:  /* if (argCount!=0 )
									throw new IllegalArgumentException
									("position()'s argument count is invalid");*/
								 return position;
			case FuncName.COUNT: 	return count(vn);
			case FuncName.NUMBER:   /*if (argCount!=1)
										throw new IllegalArgumentException
										("number()'s argument count is invalid");*/
									return argumentList.e.evalNumber(vn);
									
			case FuncName.SUM:	    return sum(vn);
			case FuncName.FLOOR: 	/*if (argCount!=1 )
			    						throw new IllegalArgumentException("floor ()'s argument count is invalid");*/
			    					return Math.floor(argumentList.e.evalNumber(vn));
			    					
			case FuncName.CEILING:	/*if (argCount!=1 )
			    						throw new IllegalArgumentException("ceiling()'s argument count is invalid");*/
			    					return Math.ceil(argumentList.e.evalNumber(vn));
			    					
			case FuncName.STRING_LENGTH:
			    					ac = argCount;
			    					if (ac == 0){
			    					    try{
			    					        if (vn.atTerminal == true){
			    					            int type = vn.getTokenType(vn.LN);
			    					            if (type == VTDNav.TOKEN_ATTR_NAME 
			    					                || type == VTDNav.TOKEN_ATTR_NS){
			    					                return vn.getStringLength(vn.LN+1);
			    					            } else {
			    					                return vn.getStringLength(vn.LN);
			    					            }
			    					        }else {
			    					            int i = vn.getText();
			    					            if (i==-1)
			    					                return 0;
			    					            else 
			    					                return vn.getStringLength(i);
			    					        }
			    					    }catch (NavException e){
			    					        return 0;
			    					    }
			    					} else if (ac == 1){
			    					    return argumentList.e.evalString(vn).length();
			    					} else {
			    					    throw new IllegalArgumentException("string-length()'s argument count is invalid");
			    					}
			    
			case FuncName.ROUND: 	/*if (argCount!=1 )
			    						throw new IllegalArgumentException("round()'s argument count is invalid");*/
			    					return Math.floor(argumentList.e.evalNumber(vn))+0.5d;
			    					
			case FuncName.ABS:		/*if (argCount!=1 )
										throw new IllegalArgumentException("abs()'s argument count is invalid");*/
									return Math.abs(argumentList.e.evalNumber(vn));		
			case FuncName.ROUND_HALF_TO_EVEN :	return roundHalfToEven(vn);		    							
			case FuncName.ROUND_HALF_TO_ODD:
			    throw new com.ximpleware.xpath.UnsupportedException("not yet implemented");
			
			default: if (isBoolean){
			    		if (evalBoolean(vn))
			    		    return 1;
			    		else
			    		    return 0;
					 }else {					    
					    try {
							double dval = Double.parseDouble(evalString(vn));
							return dval;
						}catch (NumberFormatException e){
							return Double.NaN;
						}	
					 }
	  }
	}

	private double roundHalfToEven(VTDNav vn) {	
		//int numArg = argCount;
	    if (argCount < 1 || argCount > 2){
	    	throw new IllegalArgumentException("Argument count for roundHalfToEven() is invalid. Expected: 1 or 2; Actual: " + argCount);	    	
	    }	    
	    double value = argumentList.e.evalNumber(vn);
	    long precision = (argCount == 2)? (long)Math.floor(argumentList.next.e.evalNumber(vn)+0.5d) : 0;
	    
	    if(value < 0) return -roundHalfToEvenPositive(-value, precision);	    
	    else return roundHalfToEvenPositive(value, precision);
	}
	
	private double roundHalfToEvenPositive(double value, long precision){
		final double ROUNDING_EPSILON  = 0.00000001;
	    long dec = 1;
	    
	    //shif the decimal point by precision
	    long absPre = Math.abs(precision);
	    
	    for(int i = 0; i < absPre; i++){
	    	dec *= 10;
	    }
	    
	    if(precision > 0) value *= dec;
	    else if (precision < 0)value /= dec;
	    
	    double result = 0;
	    long intPart = (long)value;
	    
	    //'value' is exctly halfway between two integers
	    if(Math.abs(value -(intPart +0.5d)) < ROUNDING_EPSILON){
	    	// 'ipart' is even 
	    	if(intPart%2 == 0){
	    		result = intPart;
	    	}else{// nearest even integer
	    		result = (long)Math.ceil( intPart + 0.5d );
	    	}
	    }else{
	    	//use the usual round to closest	    
	    	result = Math.round(value);
	    }
	    
	    //shif the decimal point back to where it was
	    if(precision > 0) result /= dec;
	    else if(precision < 0) result *= dec;
	    
		return result;		
	}
	
	/**
	 * 
	 */
	final public int evalNodeSet(VTDNav vn) throws XPathEvalException {
		switch (opCode) {
		case FuncName.CURRENT:
			if (state == START) {
				vn.loadCurrentNode();
				state = END;
				return vn.getCurrentIndex2();
			} else {
				return -1;
			}
			// break;
		case FuncName.DOCUMENT:
			if (argCount == 1) {
				if (!argumentList.e.isNodeSet()) {
					if (state == START) {
						String s = argumentList.e.evalString(vn);
						if (s.length() == 0) {
							newVN = xslVN;
							newVN.context[0] = -1;
						} else if (vg.parseFile(s, true)) {
							newVN = vg.getNav();
							newVN.context[0] = -1;
							newVN.URIName = s;
						} else {
							state = END;
							return -1;
						}
						state = END;
						return 0;
					} else {
						return -1;
					}
				} else {
					try {
						if (state != END) {
							a = argumentList.e.evalNodeSet(vn);
							if (a != -1) {
								String s = vn.toString(getStringVal(vn,a));
								if (s.length() == 0) {
									newVN = xslVN;
									newVN.context[0] = -1;
								} else if (vg.parseFile(s, true)) {
									newVN = vg.getNav();
									newVN.context[0] = -1;
									newVN.URIName = s;
								} else {
									state = END;
									return -1;
								}
								state = END;
								return 0;
							} else {
								state = END;
								return -1;
							}
						} else
							return -1;
					} catch (NavException e) {

					}
				}
			}

			break;
		case FuncName.KEY:
			throw new XPathEvalException(" key() not yet implemented ");
			// break;
		}
		throw new XPathEvalException(" Function Expr can't eval to node set ");
	}
	
	private int getStringVal(VTDNav vn,int i){
        int i1,t = vn.getTokenType(i);
        if (t == VTDNav.TOKEN_STARTING_TAG){
            i1 = vn.getText();
            return i1;
        }
        else if (t == VTDNav.TOKEN_ATTR_NAME
                || t == VTDNav.TOKEN_ATTR_NS || t==VTDNav.TOKEN_PI_NAME )
        	return i+1;
        else 
            return i;
	}
	
	final public boolean evalBoolean(VTDNav vn){
	  	  switch(opCode){
			case FuncName.STARTS_WITH:
			    //if (argCount!=2){
			    //    throw new IllegalArgumentException("starts-with()'s argument count is invalid");
			    //}
			    return startsWith(vn);
			case FuncName.CONTAINS:
			    /*if (argCount!=2){
			        throw new IllegalArgumentException("contains()'s argument count is invalid");
				}*/
			    return contains(vn);
			case FuncName.TRUE: /*if (argCount!=0){
									throw new IllegalArgumentException("true() doesn't take any argument");
								}*/
								return true;			
			case FuncName.FALSE:/*if (argCount!=0){
									throw new IllegalArgumentException("false() doesn't take any argument");
								}*/
								return false;	
			case FuncName.BOOLEAN: /*if (argCount!=1){
										throw new IllegalArgumentException("boolean() doesn't take any argument");
								   }*/
									return argumentList.e.evalBoolean(vn);	
			case FuncName.NOT:	/*if (argCount!=1){
										throw new IllegalArgumentException("not() doesn't take any argument");
			   					}*/
								return !argumentList.e.evalBoolean(vn);
		    case FuncName.LANG:
		        				/*if (argCount!=1){
		        				    	throw new IllegalArgumentException("lang()'s argument count is invalid");
		        				}*/
								return lang(vn,argumentList.e.evalString(vn));
								
		    case FuncName.COMPARE:throw new com.ximpleware.xpath.UnsupportedException("not yet implemented");
		    case FuncName.ENDS_WITH:
		        /*if (argCount!=2){
		        throw new IllegalArgumentException("ends-with()'s argument count is invalid");
		    }*/
		    return endsWith(vn);
		    
		    case FuncName.MATCH_NAME:return matchName(vn);
		    case FuncName.MATCH_LOCAL_NAME: return matchLocalName(vn);
		    case FuncName.NOT_MATCH_NAME:return !matchName(vn);
		    case FuncName.NOT_MATCH_LOCAL_NAME: return !matchLocalName(vn);
		    case FuncName.ELEMENT_AVAILABLE: return isElementAvailable(vn);
		    case FuncName.FUNCTION_AVAILABLE: return isFunctionAvailable(vn);
		        				
			default: if (isNumerical()){
			    		double d = evalNumber(vn);
			    		if (d==0 || d!=d)
			    		    return false;
			    		return true;
					 }else{
					     return evalString(vn).length()!=0;
					 }			
		  }
	}
	
	final public void reset(VTDNav vn){
	    a = 0;
	    state  = START;
	    //contextSize = 0;
		if (argumentList!=null)
			argumentList.reset(vn);
	}

	public String fname(){
		switch(opCode){
			case FuncName.LAST: 			return "last";
			case FuncName.POSITION: 		return "position";
			case FuncName.COUNT: 			return "count";
			case FuncName.LOCAL_NAME: 		return "local-name";
			case FuncName.NAMESPACE_URI: 	return "namespace-uri";
			case FuncName.NAME: 			return "name";
			case FuncName.STRING: 			return "string";
			case FuncName.CONCAT: 			return "concat";
			case FuncName.STARTS_WITH:		return "starts-with";
			case FuncName.CONTAINS: 		return "contains";
			case FuncName.SUBSTRING_BEFORE: return "substring-before";
			case FuncName.SUBSTRING_AFTER: 	return "substring-after";
			case FuncName.SUBSTRING: 		return "substring";
			case FuncName.STRING_LENGTH: 	return "string-length";
			case FuncName.NORMALIZE_SPACE: 	return "normalize-space";
			case FuncName.TRANSLATE:	 	return "translate";
			case FuncName.BOOLEAN: 			return "boolean";
			case FuncName.NOT: 				return "not";
			case FuncName.TRUE: 			return "true";
			case FuncName.FALSE: 			return "false";
			case FuncName.LANG: 			return "lang";
			case FuncName.NUMBER:			return "number";
			case FuncName.SUM: 			 	return "sum";
			case FuncName.FLOOR: 			return "floor";
			case FuncName.CEILING: 			return "ceiling";
			case FuncName.ROUND:			return "round";
//			 added for 2.0
			case FuncName.ABS:				return "abs";
			case FuncName.ROUND_HALF_TO_EVEN :											
											return "round-half-to-even";
			case FuncName.ROUND_HALF_TO_ODD:
			    							return "round-half-to-odd";
			case FuncName.CODE_POINTS_TO_STRING:
			    							return "code-points-to-string";
			case FuncName.COMPARE:			return "compare";
			case FuncName.UPPER_CASE:		return "upper-case";
			case FuncName.LOWER_CASE:		return "lower-case";
			case FuncName.ENDS_WITH:		return "ends-with";
			case FuncName.QNAME:			return "QName";
			case FuncName.LOCAL_NAME_FROM_QNAME:
											return "local-name-from-QName";
			case FuncName.NAMESPACE_URI_FROM_QNAME:
											return "namespace-uri-from-QName";
			case FuncName.NAMESPACE_URI_FOR_PREFIX:
			    							return "namespace-uri-for-prefix";
			case FuncName.RESOLVE_QNAME:	return "resolve-QName";
			case FuncName.IRI_TO_URI:    	return "iri-to-uri";
			case FuncName.ESCAPE_HTML_URI:	return "escape-html-uri";
			case FuncName.ENCODE_FOR_URI:	return "encode-for-uri";
			case FuncName.MATCH_NAME:		return "match-name";
			case FuncName.MATCH_LOCAL_NAME:	return "match-local-name";
			case FuncName.CURRENT:			return "current";
			case FuncName.GENERATE_ID:		return "generate-id";
			case FuncName.FORMAT_NUMBER:	return "format-number";
			case FuncName.KEY:		return "key";
			default:
				return "document";
				
		}
	}
	final public boolean  isNodeSet(){
		return isNodeSet;
	}

	final public boolean  isNumerical(){
		return isNumerical;
	}
	
	final public boolean isString(){
	    return isString;
	}
	
	final public boolean isBoolean(){
	    return isBoolean;
	}
	
	private int count(VTDNav vn){
	    int a = -1;
	   // if (argCount!=1 || argumentList.e.isNodeSet()==false)
		//	throw new IllegalArgumentException
			//	("Count()'s argument count is invalid");
		vn.push2();
		int size= vn.contextStack2.size ;
		try{
			a = 0;
			argumentList.e.adjust(vn.getTokenCount());
			while(argumentList.e.evalNodeSet(vn)!=-1){
				//System.out.println(" ===>"+vn.getCurrentIndex());
				a ++;
			}
		}catch(Exception e){
			System.out.println("exception in count");
		}
		argumentList.e.reset(vn);
		vn.contextStack2.size = size;
		vn.pop2();
		return a;
	}
	
	private double sum(VTDNav vn){
	    double d=0;
	    /*if (argCount != 1 || argumentList.e.isNodeSet() == false)
	        throw new IllegalArgumentException("sum()'s argument count or type is invalid");*/
    	vn.push2();
    	int size = vn.contextStack2.size;
    	try {
    	    a = 0;
    	    int i1;
    	    while ((a =argumentList.e.evalNodeSet(vn)) != -1) {
    	        int t = vn.getTokenType(a);
                if (t == VTDNav.TOKEN_STARTING_TAG){
                    i1 = vn.getText();
                    if (i1!=-1)
                        d += vn.parseDouble(i1);
                    if (Double.isNaN(d))
                        break;
                }
                else if (t == VTDNav.TOKEN_ATTR_NAME
                        || t == VTDNav.TOKEN_ATTR_NS){
                    d += vn.parseDouble(a+1);
                    if (Double.isNaN(d))
                        break;
                }
                else if (t == VTDNav.TOKEN_CHARACTER_DATA
                        || t == VTDNav.TOKEN_CDATA_VAL || t== VTDNav.TOKEN_COMMENT){
                    d += vn.parseDouble(a);
                    if (Double.isNaN(d))
                        break;
                } else if (t==VTDNav.TOKEN_PI_NAME){
                	if (a+1<vn.vtdSize && vn.getTokenType(a+1)==VTDNav.TOKEN_PI_VAL){
                		d += vn.parseDouble(a+1);
                	} else {
                		d = Double.NaN;
                		break;
                	}
                }
                //    fib1.append(i);
    	    }
    	    argumentList.e.reset(vn);
    	    vn.contextStack2.size = size;
    	    vn.pop2();    	    
    	    return d;
    	} catch (Exception e) {
    	    argumentList.e.reset(vn);
    	    vn.contextStack2.size = size;
    	    vn.pop2();
    	    return Double.NaN;
    	}
	    
	}
	// to support computer context size 
	// needs to add 
	
	final public boolean requireContextSize(){
	    if (opCode == FuncName.LAST)
	        return true;
	    else {
	        Alist temp = argumentList;
	        //boolean b = false;
	        while(temp!=null){
	            if (temp.e.requireContextSize()){
	                return true;
	            }
	            temp = temp.next;
	        }
	    }
	    return false;
	}
	
	final public void setContextSize(int size){	
	    if (opCode == FuncName.LAST){
	        contextSize = size;
	        //System.out.println("contextSize: "+size);
	    } else {
	        Alist temp = argumentList;
	        //boolean b = false;
	        while(temp!=null){
	            temp.e.setContextSize(size);
	            temp = temp.next;
	        }
	    }
	}
	
	public void setPosition(int pos){
	    if (opCode == FuncName.POSITION){
	        position = pos;
	        //System.out.println("PO: "+size);
	    } else {
	        Alist temp = argumentList;
	        //boolean b = false;
	        while(temp!=null){
	            temp.e.setPosition(pos);
	            temp = temp.next;
	        }
	    }
	}
	public int adjust(int n){
	    int i = 0;
	    switch(opCode){
	    	case FuncName.COUNT: 
	        case FuncName.SUM:
	            i = argumentList.e.adjust(n);
	    		break;
	    	default: 
	    }
	    return i;
	}
	
	private String upperCase(VTDNav vn){
		if (argCount==1){
			if (argumentList.e.isNodeSet()){
				int a = evalFirstArgumentListNodeSet(vn);
		        if (a==-1)
		        	return "";
		        else{
		        	try{
		        		int t = vn.getTokenType(a);
		        		if (t!= VTDNav.TOKEN_STARTING_TAG && t!=VTDNav.TOKEN_DOCUMENT)
		        			return vn.toStringUpperCase(a);
		        		return vn.getXPathStringVal2(a, (short)1);
		        	}catch(Exception e){
		        	}
		        	return "";
		        }		
			}else {
				return (argumentList.e.evalString(vn)).toUpperCase();
			}
		}else 
			throw new IllegalArgumentException
			("upperCase()'s argument count is invalid");
		
	}
	
	private String lowerCase(VTDNav vn){
		if (argCount==1){
			if (argumentList.e.isNodeSet()){
				int a = evalFirstArgumentListNodeSet(vn);
		        if (a==-1)
		        	return "";
		        else{
		        	try{
		        		int t = vn.getTokenType(a);
		        		if (t!= VTDNav.TOKEN_STARTING_TAG && t!=VTDNav.TOKEN_DOCUMENT)
		        			return vn.toStringLowerCase(a);
		        		return vn.getXPathStringVal2(a,(short)2);
		        	}catch(Exception e){
		        	}
		        	return "";
		        }		
			}else {
				return (argumentList.e.evalString(vn)).toLowerCase();
			}
		}else 
			throw new IllegalArgumentException
			("lowerCase()'s argument count is invalid");
	}
	
	private boolean matchName(VTDNav vn){	    
		int a;
		if (argCount == 1) {
			a = vn.getCurrentIndex();
			int type = vn.getTokenType(a);
			String s1 = argumentList.e.evalString(vn);
			if (type == VTDNav.TOKEN_STARTING_TAG
					|| type == VTDNav.TOKEN_ATTR_NAME
					|| type == VTDNav.TOKEN_PI_NAME) {
				try {
					return vn.matchRawTokenString(a, s1);
				} catch (Exception e) {
					return false;
				}
			} else
				return false;
		} else if (argCount == 2) {
			a = evalFirstArgumentListNodeSet2(vn);
			String s1 = argumentList.next.e.evalString(vn);
			try {
				if (a == -1 || vn.ns == false)
					return false;
				else {
					int type = vn.getTokenType(a);
					if (type == VTDNav.TOKEN_STARTING_TAG
							|| type == VTDNav.TOKEN_ATTR_NAME
							|| type == VTDNav.TOKEN_PI_NAME)
						return vn.matchRawTokenString(a, s1);
					return false;
				}
			} catch (Exception e) {
			}
			return false;
		} else
			throw new IllegalArgumentException(
					"name()'s argument count is invalid");
	}
	private boolean matchLocalName(VTDNav vn){
	    if (argCount== 1){
	        try{
	            int index = vn.getCurrentIndex();
	            int type = vn.getTokenType(index);
	            String s1 = argumentList.e.evalString(vn);
	            if (vn.ns && (type == VTDNav.TOKEN_STARTING_TAG 
	                    || type == VTDNav.TOKEN_ATTR_NAME)) {
                    int offset = vn.getTokenOffset(index);
                    int length = vn.getTokenLength(index);
                    if (length < 0x10000 || (length>>16)==0){
                    	return (vn.compareRawTokenString(index, s1)==0);//vn.toRawString(index);
                    }
                    else {
                        int preLen = length >> 16;
                        int QLen = length & 0xffff;
                        if (preLen != 0){
                        	 return (vn.compareRawTokenString(offset + preLen+1, QLen
                                        - preLen - 1,s1)==0);                        	
                        }
                    }
                } else if (type == VTDNav.TOKEN_PI_NAME){
                	return vn.compareRawTokenString(index, s1)==0;
                } else 
                    return "".equals(s1);
	        }catch(NavException e){
	        	 return false; // this will never occur
	        }
	        
	    } else if (argCount == 2){
	        int a=evalFirstArgumentListNodeSet2(vn);
	        String s1 = argumentList.next.e.evalString(vn);
			if (a == -1 || vn.ns == false)
			    return "".equals(s1);
			int type = vn.getTokenType(a);
			if (type==VTDNav.TOKEN_STARTING_TAG || type== VTDNav.TOKEN_ATTR_NAME){
			    //return "".equals(s1);
			try {			    
			    int offset = vn.getTokenOffset(a);
			    int length = vn.getTokenLength(a);
			    if (length < 0x10000 || (length>> 16)==0)
			        return vn.compareRawTokenString(a,s1)==0;
			    else {
			        int preLen = length >> 16;
			        int QLen = length & 0xffff;
			        if (preLen != 0)
			            return vn.compareRawTokenString(offset + preLen+1, 
			                    QLen - preLen - 1,s1)==0;
			        /*else {
			            return vn.toRawString(offset, QLen);
			        }*/
			    }
			} catch (NavException e) {
				 return "".equals(s1); // this will almost never occur
			}		
			}else if (type == VTDNav.TOKEN_PI_NAME){
				try{
					return vn.compareRawTokenString(a,s1)==0;
				}catch(NavException e){
					 return "".equals(s1);
				}
			}
			 return "".equals(s1);
	    } else 
	        throw new IllegalArgumentException
			("local-name()'s argument count is invalid");
		return false;
	}
	
	/**
	 * generate-id(nodeset?);
	 * @param vn
	 * @return
	 */
	private String generateID(VTDNav vn){
		if (argCount== 0){
			return "v"+vn.getCurrentIndex2();
		}else if (argCount== 1) {
			int i=evalFirstArgumentListNodeSet2(vn);
			return "v"+i;
		} else 
	        throw new IllegalArgumentException
			("generate-id()'s argument count is invalid");
		
	}
	
	public boolean isFinal(){
			Alist temp = argumentList;
			if (temp ==null)
				return false;
			if (temp.e==null)
				return false;
			boolean s=true;
			while(temp!=null){
			    s= s && temp.e.isFinal();
			    if (!s)
			    	return false;
				temp = temp.next;
			}
			return s;			
	}
	
	public void markCacheable2(){
	    Alist temp =argumentList;
		while(temp!=null ){
			if (temp.e!=null){
				if (temp.e.isFinal() && temp.e.isNodeSet()){
					CachedExpr ce = new CachedExpr(temp.e);
					temp.e = ce;
				}
				temp.e.markCacheable2();
			}
			temp = temp.next; 
		}		
	}
	
	public void markCacheable(){
		 Alist temp =argumentList;
		 while(temp!=null){
			 if (temp.e!=null)
				 temp.e.markCacheable();
			temp = temp.next; 
		}
	}
	
	public void clearCache(){
		Alist temp =argumentList;
		while(temp!=null ){
			if (temp.e!=null){				
				temp.e.clearCache();
			}
			temp = temp.next; 
		}
	}
}

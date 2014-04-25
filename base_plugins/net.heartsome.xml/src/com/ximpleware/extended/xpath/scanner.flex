/* 
 * Copyright (C) 2002-2011 XimpleWare, info@ximpleware.com
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
import java_cup.runtime.Symbol;
import com.ximpleware.extended.parser.*;
import com.ximpleware.extended.*;

%%
%cup
%char
%unicode
%extends sym
%yylexthrow XPathParseExceptionHuge

%{

  private Symbol sym(int sym) {
    return new Symbol(sym);
  }

  private Symbol sym(int sym, Object val) {
    return new Symbol(sym, val);
  }
 
  int isName; 
  NameType name;
  FuncName fname; 
  Ntest ntest;
  AxisType at;
  int len;
  String literal;
  Double number; 
  int colonPosition;

  public int getOffset(){
  	return yychar;
  }
  /* public void report_error(String message, Object info) {
	//throw new XPathParseException("Syntax error during parsing");
  }

  public void report_fatal_error(String message, Object info) throws XPathParseException{
	throw new XPathParseExceptionHuge("Syntax error during parsing: "+ message);
  }

  public void syntax_error(Symbol cur_token) {
	
  }
  
  public void unrecovered_syntax_error(Symbol cur_token) throws XPathParseException{
	throw new XPathParseExceptionHuge("XPath Syntax error: "+cur_token);
  }*/

%}
%init{
	isName = 1;
	colonPosition = -1;
%init}

ws  = 	   [ \t\r\n]
digits	=  [0-9]+
nc	=  ([^\!-/:-@\[-\^ \n\r\t\|]|"#"|"&"|";"|"?"|_|"\\"|"^"|"%"|"-"|".")
nc2	=  ([^\!-/:-@\[-\^ \n\r\t\|0-9]|"#"|"&"|";"|"?"|_|"\\"|"^"|"%"|".")

%%
{ws}+ { /* eat white space */}

"+" 	{isName = 1 ; return sym(ADD);}
- 	{isName = 1 ; return sym(SUB);}
"."	{isName = 0 ; /*System.out.println(". returned ");*/ return sym(DOT);}
".." 	{isName = 0 ; return sym(DDOT);}
"@"	{isName = 1 ; return sym(AT);}

","	{isName = 1 ; return sym(COMMA);}
"("	{isName = 1 ; return sym(LP);}
")"	{isName = 0 ; return sym(RP);}
"["	{isName = 1 ; /*System.out.println( "[ returned");*/ return sym(LB);}
"]"	{isName = 0 ; return sym(RB);}
">"	{isName = 1 ; return sym(GT);}
"<"	{isName = 1 ; return sym(LT);}
">="	{isName = 1 ; return sym(GE);}
"<="	{isName = 1 ; return sym(LE);}
"="	{isName = 1 ; return sym(EQ);}
"!="	{isName = 1 ; return sym(NE);}
"$" {isName = 1; return sym(DOLLAR);}

"*"	{if (isName ==0){
		isName = 1;
		//System.out.println("returned a MULT");
		return  sym(MULT);
	}
	 else {
		isName = 0;
		name = new NameType();
		name.qname = "*";
		return sym(NAME,name);
	 }	 
	}

"/"	{isName = 1 ; 
	 //System.out.println("SLASH returned ");
	 return sym(SLASH);
	}

"//"	{isName = 1 ; 
	 //System.out.println("DSLASH returned "); 
	 return sym(DSLASH);
	}

div	{     if (isName == 0 ) {
		  isName = 1 ;
		 return sym(DIV);
	      } else {
		 isName = 0;
		 name = new NameType();
		 name.qname = "div";
		 return sym(NAME,name);
	      }
	}

mod	{     if (isName == 0) {
		  isName = 1 ;
		 return sym(MOD);
	      } else {
		 isName = 0;
		 name = new NameType();
		 name.qname = "mod";
		 //System.out.println("returned a NAME "+yytext());
		 return sym(NAME,name);
	      }
	}

and	{     if (isName == 0) {
		isName = 1 ;
		 return sym(AND);
	      } else {
		 isName = 0;
		 name = new NameType();
		 name.qname = "add";
		 return sym(NAME,name);
	      }
	}

or	{     if (isName == 0) {	
		 isName = 1 ;
		 return sym(OR);
	      } else {
		 isName = 0;
		 name = new NameType();
		 name.qname = "or";
		 return sym(NAME,name);
	      }
	}

"|"	{isName = 1 ; return sym(UNION) ; }

last{ws}*"("		{  	isName = 1; 
			 	yypushback(1);
				fname = new FuncName();
				fname.i = FuncName.LAST;
				return sym(FNAME,fname);				 
			}
position{ws}*"("	{  	isName = 1; 
			 	yypushback(1);
				//yyparser.yylval.fname.i =  FuncName.POSITION;
				fname = new FuncName();
				fname.i = FuncName.POSITION;
				return sym(FNAME,fname);	
			}
count{ws}*"("		{  	isName = 1; 
			 	yypushback(1);
				//yyparser.yylval.fname.i =  FuncName.COUNT;
				fname = new FuncName();
				fname.i = FuncName.COUNT;
				return sym(FNAME,fname);
			}
local-name{ws}*"("	{  	isName = 1; 
			 	yypushback(1);
				//yyparser.yylval.fname.i =  FuncName.LOCAL_NAME;
				fname = new FuncName();
				fname.i = FuncName.LOCAL_NAME;
				return sym(FNAME,fname);
			}
namespace-uri{ws}*"("	{  	isName = 1; 
			 	yypushback(1);
				//yyparser.yylval.fname.i =  FuncName.NAMESPACE_URI;
				fname = new FuncName();
				fname.i = FuncName.NAMESPACE_URI;
				return sym(FNAME,fname);
			}
name{ws}*"("		{  	isName = 1; 
			 	yypushback(1);
				//yyparser.yylval.fname.i =  FuncName.NAME;
				fname = new FuncName();
				fname.i = FuncName.NAME;
				return sym(FNAME,fname);	
			}

string{ws}*"("		{  	isName = 1; 
			 	yypushback(1);
				//yyparser.yylval.fname.i =  FuncName.STRING;
				fname = new FuncName();
				fname.i = FuncName.STRING;
				return sym(FNAME,fname);	
			}


concat{ws}*"("		{  	isName = 1; 
			 	yypushback(1);
				//yyparser.yylval.fname.i = FuncName.CONCAT;
				fname = new FuncName();
				fname.i = FuncName.CONCAT;
				return sym(FNAME,fname);	
			}

starts-with{ws}*"("	{  	isName = 1; 
			 	yypushback(1);
				//yyparser.yylval.fname.i = FuncName.STARTS_WITH;
				fname = new FuncName();
				fname.i = FuncName.STARTS_WITH;
				return sym(FNAME,fname);	
			}

contains{ws}*"("	{  	isName = 1; 
			 	yypushback(1);
				//yyparser.yylval.fname.i = FuncName.CONTAINS;
				fname = new FuncName();
				fname.i = FuncName.CONTAINS;
				return sym(FNAME,fname);	
			}

substring-before{ws}*"("  {  	isName = 1; 
			 	yypushback(1);
				//yyparser.yylval.fname.i = FuncName.SUBSTRING_BEFORE;
				fname = new FuncName();
				fname.i = FuncName.SUBSTRING_BEFORE;
				return sym(FNAME,fname);	
			}

substring-after{ws}*"(" {  	isName = 1; 
			 	yypushback(1);
				//yyparser.yylval.fname.i = FuncName.SUBSTRING_AFTER;
				fname = new FuncName();
				fname.i = FuncName.SUBSTRING_AFTER;
				return sym(FNAME,fname);	
			} 	

substring{ws}*"("	{  	isName = 1; 
			 	yypushback(1);
				//yyparser.yylval.fname.i = FuncName.SUBSTRING;
				fname = new FuncName();
				fname.i = FuncName.SUBSTRING;
				return sym(FNAME,fname);	
			}

string-length{ws}*"("	{  	isName = 1; 
			 	yypushback(1);
				//yyparser.yylval.fname.i = FuncName.STRING_LENGTH;
				fname = new FuncName();
				fname.i = FuncName.STRING_LENGTH;
				return sym(FNAME,fname);	
			}

normalize-space{ws}*"("	{  	isName =1; 
			 	yypushback(1);
				//yyparser.yylval.fname.i = FuncName.NORMALIZE_SPACE;
				fname = new FuncName();
				fname.i = FuncName.NORMALIZE_SPACE;
				return sym(FNAME,fname);	
			}

translate{ws}*"("	{  	isName = 1; 
			 	yypushback(1);
				//yyparser.yylval.fname.i = FuncName.TRANSLATE;
				fname = new FuncName();
				fname.i = FuncName.TRANSLATE;
				return sym(FNAME,fname);
			}
			
abs{ws}*"("	{  	isName = 1; 
			 	yypushback(1);
				//yyparser.yylval.fname.i = FuncName.ABS;
				fname = new FuncName();
				fname.i = FuncName.ABS;
				return sym(FNAME,fname);
			}
			
round-half-to-even{ws}*"("	{  	isName = 1; 
			 	yypushback(1);
				//yyparser.yylval.fname.i = FuncName.ROUND_HALF_TO_EVEN;
				fname = new FuncName();
				fname.i = FuncName.ROUND_HALF_TO_EVEN;
				return sym(FNAME,fname);
			}
			
round-half-to-odd{ws}*"("	{  	isName = 1; 
			 	yypushback(1);
				//yyparser.yylval.fname.i = FuncName.ROUND_HALF_TO_ODD;
				fname = new FuncName();
				fname.i = FuncName.ROUND_HALF_TO_ODD;
				return sym(FNAME,fname);
			}

code-points-to-string{ws}*"("	{  	isName = 1; 
			 	yypushback(1);
				//yyparser.yylval.fname.i = FuncName.CODE_POINTS_TO_STRING;
				fname = new FuncName();
				fname.i = FuncName.CODE_POINTS_TO_STRING;
				return sym(FNAME,fname);
			}
			
compare{ws}*"("	{  	isName = 1; 
			 	yypushback(1);
				//yyparser.yylval.fname.i = FuncName.COMPARE;
				fname = new FuncName();
				fname.i = FuncName.COMPARE;
				return sym(FNAME,fname);
			}
			
upper-case{ws}*"("	{  	isName = 1; 
			 	yypushback(1);
				//yyparser.yylval.fname.i = FuncName.UPPER_CASE;
				fname = new FuncName();
				fname.i = FuncName.UPPER_CASE;
				return sym(FNAME,fname);
			}
			
lower-case{ws}*"("	{  	isName = 1; 
			 	yypushback(1);
				//yyparser.yylval.fname.i = FuncName.LOWER_CASE;
				fname = new FuncName();
				fname.i = FuncName.LOWER_CASE;
				return sym(FNAME,fname);
			}

ends-with{ws}*"("	{  	isName = 1; 
			 	yypushback(1);
				//yyparser.yylval.fname.i = FuncName.ENDS_WITH;
				fname = new FuncName();
				fname.i = FuncName.ENDS_WITH;
				return sym(FNAME,fname);
			}
			
QName{ws}*"("	{  	isName = 1; 
			 	yypushback(1);
				//yyparser.yylval.fname.i = FuncName.QNAME;
				fname = new FuncName();
				fname.i = FuncName.QNAME;
				return sym(FNAME,fname);
			}
			
local-name-from-QName{ws}*"(" {  	isName = 1; 
			 	yypushback(1);
				//yyparser.yylval.fname.i = FuncName.LOCAL_NAME_FROM_QNAME;
				fname = new FuncName();
				fname.i = FuncName.LOCAL_NAME_FROM_QNAME;
				return sym(FNAME,fname);
			}

namespace-uri-from-QName{ws}*"(" {  	isName = 1; 
			 	yypushback(1);
				//yyparser.yylval.fname.i = FuncName.NAMESPACE_URI_FROM_QNAME;
				fname = new FuncName();
				fname.i = FuncName.NAMESPACE_URI_FROM_QNAME;
				return sym(FNAME,fname);
			}

namespace-uri-for-prefix{ws}*"(" {  	isName = 1; 
			 	yypushback(1);
				//yyparser.yylval.fname.i = FuncName.NAMESPACE_URI_FOR_PREFIX;
				fname = new FuncName();
				fname.i = FuncName.NAMESPACE_URI_FOR_PREFIX;
				return sym(FNAME,fname);
			}			

resolve-QName{ws}*"(" {  	isName = 1; 
			 	yypushback(1);
				//yyparser.yylval.fname.i = FuncName.RESOLVE_QNAME;
				fname = new FuncName();
				fname.i = FuncName.RESOLVE_QNAME;
				return sym(FNAME,fname);
			}

iri-to-uri{ws}*"(" {  	isName = 1; 
			 	yypushback(1);
				//yyparser.yylval.fname.i = FuncName.IRI_TO_URI;
				fname = new FuncName();
				fname.i = FuncName.IRI_TO_URI;
				return sym(FNAME,fname);
			}
			
escape-html-uri{ws}*"(" {  	isName = 1; 
			 	yypushback(1);
				//yyparser.yylval.fname.i = FuncName.ESCAPE_HTML_URI;
				fname = new FuncName();
				fname.i = FuncName.ESCAPE_HTML_URI;
				return sym(FNAME,fname);
			}
			
encode-for-uri{ws}*"(" {  	isName = 1; 
			 	yypushback(1);
				//yyparser.yylval.fname.i = FuncName.ENCODE_FOR_URI;
				fname = new FuncName();
				fname.i = FuncName.ENCODE_FOR_URI;
				return sym(FNAME,fname);
			}

boolean{ws}*"("		{  	isName = 1; 
			 	yypushback(1);
				//yyparser.yylval.fname.i = FuncName.BOOLEAN;
				fname = new FuncName();
				fname.i = FuncName.BOOLEAN;
				return sym(FNAME,fname);	
			}

not{ws}*"("		{  	isName = 1; 
			 	yypushback(1);
				//yyparser.yylval.fname.i = FuncName.NOT;
				fname = new FuncName();
				fname.i = FuncName.NOT;
				return sym(FNAME,fname);	
			}

true{ws}*"("		{  	isName = 1; 
			 	yypushback(1);
				//yyparser.yylval.fname.i = FuncName.TRUE;
				fname = new FuncName();
				fname.i = FuncName.TRUE;
				return sym(FNAME,fname);	
			}

false{ws}*"("		{  	isName = 1; 
			 	yypushback(1);
				//yyparser.yylval.fname.i = FuncName.FALSE;
				fname = new FuncName();
				fname.i = FuncName.FALSE;
				return sym(FNAME,fname);	
			}

lang{ws}*"("		{  	isName = 1; 
			 	yypushback(1);
				//yyparser.yylval.fname.i = FuncName.LANG;
				fname = new FuncName();
				fname.i = FuncName.LANG;
				return sym(FNAME,fname);	
			}

number{ws}*"("		{  	isName = 1; 
			 	yypushback(1);
				//yyparser.yylval.fname.i = FuncName.NUMBER;
				fname = new FuncName();
				fname.i = FuncName.NUMBER;
				return sym(FNAME,fname);	
			}

sum{ws}*"("		{  	isName = 1; 
			 	yypushback(1);
				//yyparser.yylval.fname.i = FuncName.SUM;
				fname = new FuncName();
				fname.i = FuncName.SUM;
				return sym(FNAME,fname);	
			}

floor{ws}*"("		{  	isName = 1; 
			 	yypushback(1);
				//yyparser.yylval.fname.i = FuncName.FLOOR;
				fname = new FuncName();
				fname.i = FuncName.FLOOR;
				return sym(FNAME,fname);	
			}

ceiling{ws}*"("		{  	isName = 1; 
			 	yypushback(1);
				//yyparser.yylval.fname.i = FuncName.CEILING;
				fname = new FuncName();
				fname.i = FuncName.CEILING;
				return sym(FNAME,fname);	
			}

round{ws}*"("		{  	isName = 1; 
			 	yypushback(1);
				//yyparser.yylval.fname.i = FuncName.ROUND;
				fname = new FuncName();
				fname.i = FuncName.ROUND;
				return sym(FNAME,fname);	
			}


\"[^\"]*\" |
'[^']*'			{
				isName = 0;
				len = yytext().length();
				literal = yytext().substring(1, len-1);
				return sym(LITERAL,literal);
			}

{digits}("."{digits}?)? |
"."{digits}		{
				isName = 0;
				number = new Double(yytext());
				//System.out.println("number returned ==> "+ Double.parseDouble(yytext()));
				return sym(NUMBER,number);
			}

text{ws}*"("{ws}*")"	{	
				isName = 0;
				ntest = new Ntest();
				ntest.i = Ntest.TEXT;
				ntest.arg = null;
				return sym(NTEST,ntest);
			}

comment{ws}*"("{ws}*")"	{
				isName = 0;
				ntest = new Ntest();
				ntest.i = Ntest.COMMENT;
				ntest.arg =  null;
				return sym(NTEST,ntest);
			}

node{ws}*"("{ws}*")"	{
				isName = 0;
				ntest = new Ntest();
				ntest.i = Ntest.NODE;
				ntest.arg = null;
				return sym(NTEST,ntest);
			}

processing-instruction{ws}*"("{ws}*")"	{
						isName = 0;
						ntest = new Ntest();
						ntest.i = Ntest.PROCESSING_INSTRUCTION;
						ntest.arg = null;
						return sym(NTEST,ntest);
					}

ancestor{ws}*::		{	isName = 1;
				at = new AxisType();
				at.i = AxisType.ANCESTOR;
				return sym(AXISNAME,at);
			}

ancestor-or-self{ws}*::	{	isName = 1;
				at = new AxisType();
				at.i = AxisType.ANCESTOR_OR_SELF;
				return sym(AXISNAME,at);
			}


attribute{ws}*::	{	isName = 1;
				at = new AxisType();
				at.i = AxisType.ATTRIBUTE;
				return sym(AXISNAME,at);
			}

child{ws}*::		{	isName = 1;
				at = new AxisType();
				at.i = AxisType.CHILD;
				return sym(AXISNAME,at);
			}

descendant{ws}*::	{	isName = 1;
				at = new AxisType();
				at.i = AxisType.DESCENDANT;
				return sym(AXISNAME,at);
			}

descendant-or-self{ws}*:: {	isName = 1;
				at = new AxisType();
				at.i = AxisType.DESCENDANT_OR_SELF;
				return sym(AXISNAME,at);
			}

following{ws}*::	{	isName = 1;
				at = new AxisType();
				at.i = AxisType.FOLLOWING;
				return sym(AXISNAME,at);
			}

following-sibling{ws}*::  {	isName = 1;
				at = new AxisType();
				at.i = AxisType.FOLLOWING_SIBLING;
				return sym(AXISNAME,at);
			}

namespace{ws}*::	{	isName =0;
				at = new AxisType();
				at.i = AxisType.NAMESPACE;
				return sym(AXISNAME,at);
			}

parent{ws}*::		{	isName = 1;
				at = new AxisType();
				at.i = AxisType.PARENT;
				return sym(AXISNAME,at);
			}

preceding{ws}*::	{	isName = 1;
				at = new AxisType();
				at.i = AxisType.PRECEDING;
				return sym(AXISNAME,at);
			}

preceding-sibling{ws}*:: {	isName = 1;
				at = new AxisType();
				at.i = AxisType.PRECEDING_SIBLING;
				return sym(AXISNAME,at);
			}

self{ws}*::		{	isName = 1;
				at = new AxisType();
				at.i = AxisType.SELF;
				//System.out.println("SELF:: returned");
				return sym(AXISNAME,at);
			}

{nc2}{nc}*:"*"  	{	isName = 0;
				len = yytext().length();
				name = new NameType();
                               if (!XMLChar.isNCNameStartChar(yytext().charAt(0)))
					throw new XPathParseExceptionHuge("Invalid char in name token:  "+yytext()+ "@position 0");
				
				for(int i=1;i<len-2;i++){
					if (!XMLChar.isNCNameChar(yytext().charAt(i)))
						throw new XPathParseExceptionHuge("Invalid char in name token:  "+yytext()+ "@position "+i);
				}

				name.prefix = yytext().substring(0,len-2);
				name.localname = "*";
				//System.out.println("NAME "+name+ " returned");
				return sym(NAME,name);
			}

{nc2}{nc}*:{nc}+ |			
{nc2}{nc}*		{	
				
				isName = 0;
				name = new NameType();
				//name.qname = new String(yytext());
				//System.out.println("returned a NAME ==>" + yytext());
				//if (yytext().charAt(0) =='-'){
				//    throw new XPathParseException("Invalid char in name token:"+yytext());
				//}
				
				name.qname = new String(yytext());
				if (!XMLChar.isNCNameStartChar(name.qname.charAt(0)))
					throw new XPathParseExceptionHuge("Invalid char in name token:  "+yytext()+ "@position 0");
				
				for(int i=1;i<name.qname.length();i++){
					if (!XMLChar.isNCNameChar(name.qname.charAt(i)) 
						&& name.qname.charAt(i)!=':' )
						throw new XPathParseExceptionHuge("Invalid char in name token:  "+yytext()+ "@position "+i);
					if (name.qname.charAt(i)==':'){
						colonPosition = i;
					}
				}
		
				if (colonPosition != -1){
					name.prefix = yytext().substring(0,colonPosition);
					name.localname = yytext().substring(colonPosition+1);				
				}
				
				colonPosition = -1;
				return sym(NAME,name);
			}


.		{	
			throw new XPathParseExceptionHuge("Invalid char in XPath Expression");
		}

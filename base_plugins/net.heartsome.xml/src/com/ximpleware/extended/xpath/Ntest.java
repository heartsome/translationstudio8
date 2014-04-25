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
/**
 * Lexer use this class to represent a type of node test
 * 
 *
 */
public class Ntest{
	public int i;
	public String arg;
 	public final static int COMMENT = 5;
	public final static int TEXT = 2;
	public final static int PROCESSING_INSTRUCTION = 3;
	public final static int NODE = 1;
	
//	public String getNtestString(){		
//		switch (i){
//		case 1: return "node(";
//		case 2: return "text(";
//		case 3: return "processing-instruction(";
//		case 5: return "comment(";
//		default: return "invalid node type!!!";
//		}
//	}
}

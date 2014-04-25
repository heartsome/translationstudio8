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
import com.ximpleware.extended.*;
/**
 * 
 * This is the abstract class on which all XPath expressions 
 * are based
 */

abstract public class Expr {

	abstract public boolean evalBoolean(VTDNavHuge vn);

	abstract public double evalNumber(VTDNavHuge vn);
		
	abstract public int evalNodeSet(VTDNavHuge vn) throws XPathEvalExceptionHuge, NavExceptionHuge;
	
	abstract public String evalString(VTDNavHuge vn);

	abstract public void reset(VTDNavHuge vn);
	abstract public String toString();

	abstract public boolean isNumerical();
	abstract public boolean isNodeSet();
	abstract public boolean isString();
	abstract public boolean isBoolean();
	
	abstract public boolean requireContextSize();
	abstract public void setContextSize(int size);
	
	abstract public void setPosition(int pos);
	abstract public int adjust(int n);
	// to support computer context size 
	// needs to add 
	//abstract public boolean needContextSize();
	//abstract public boolean SetContextSize(int contextSize);
}

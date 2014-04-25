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
/**
 * This class is the base class of all the exceptions of autopilot.
 * Creation date: (11/30/03 6:14:43 PM)
 */
public class PilotException extends NavException {
/**
 * PilotException constructor comment.
 */
public PilotException() {
	super();
}
/**
 * PilotException constructor comment.
 * @param s java.lang.String
 */
public PilotException(String s) {
	super(s);
}
}

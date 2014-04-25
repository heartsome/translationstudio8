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
/**
 * This class is the base class of all the exceptions of AutoPilotHuge.
 * It is adapted to support extended VTD (256 max file size) 
 */
public class PilotExceptionHuge extends NavExceptionHuge {
/**
 * PilotException constructor comment.
 */
public PilotExceptionHuge() {
	super();
}
/**
 * PilotException constructor comment.
 * @param s java.lang.String
 */
public PilotExceptionHuge(String s) {
	super(s);
}
}

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

import java.io.IOException;
import java.io.OutputStream;

public class XMLByteOutputStream extends OutputStream {
	
	private byte[] XMLDoc;
	private int offset=0;
	
	final public byte[] getXML(){
		return XMLDoc;
	}

	@Override
	public void write(int b) throws IOException {
		// TODO Auto-generated method stub
		// not yet implemented
		XMLDoc[offset]= (byte)b;
		offset++;
	}
	
	public void write(byte[] ba) throws IOException{
		if (ba.length+offset>XMLDoc.length)
			throw new IOException("XMLDoc size exceeds maximum size");
		System.arraycopy(ba, 0, XMLDoc, offset, ba.length);
		offset+=ba.length;
	}
	
	public void write(byte[] ba, int os, int len) throws IOException{
		if (len+offset>XMLDoc.length)
			throw new IOException("XMLDoc size exceeds maximum size");
		System.arraycopy(ba, os, XMLDoc, offset, len);
		offset +=len;
	}
	
	public void close(){
		offset = 0;
	}
	
	public XMLByteOutputStream(int size){
		XMLDoc = new byte[size];
		offset =0;
	}
	
	public String toString(){
		StringBuilder sb = new StringBuilder(XMLDoc.length);
		for(int i=0;i<XMLDoc.length;i++)
			//sb.charAt(i) = XMLDoc[i];
			sb.append((char) XMLDoc[i]);
			//sb.setCharAt(i,(char) XMLDoc[i] );
		return sb.toString();
		
	}

}

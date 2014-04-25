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
package com.ximpleware.transcode;

import com.ximpleware.TranscodeException;

import java.io.*;
public class ASCII_Coder {
    /**
     * 
     * @param ch
     * @return
     * @throws TranscodeException
     *
     */
    public static final int getLen(int ch) throws TranscodeException{
        if (ch>=128)
            throw new TranscodeException("Invalid UCS char for ASCII format");
        else
            return 1;
    }
    
    /**
     * 
     * @param input
     * @param offset
     * @return a 64-bit integer upper 32 bits is offset value for   
     * 		lower 32 bits is the UCS char
     *
     */
    public static final long decode(byte[] input, int offset ){
        long l = input[offset];
        return (((long)(offset+1))<<32) | l ;
    }
    
    /**
     * 
     * @param output
     * @param offset
     * @param ch
     * @return
     *
     */
    public static final int encode(byte[] output, int offset, int ch ){
        output[offset] = (byte) ch;
        return offset+1;
    }
    
    /**
     * 
     * @param os
     * @param offset
     * @param ch
     *
     */
    public static final void encodeAndWrite(OutputStream os, int ch)
    throws IOException, TranscodeException {
        if (ch>=128)
            throw new TranscodeException("Invalid UCS char for ASCII format");
        os.write(ch);
    }
}

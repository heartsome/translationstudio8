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
import java.io.IOException;
import java.io.OutputStream;

import com.ximpleware.*;
public class UTF8_Coder {
    
    /**
     * Encode a UCS Char to a UTF-8 representation and write it into the output
     * buffer
     * @param output the output byte buffer
     * @param offset offset value to which the UTF-8 bytes are written
     * @param ch the UCS char to be encoded
     * @return the offset value of the next char to be written
     *
     */
    public static int encode(byte[] output, int offset, int ch ){
        if (ch < 128){
            output[offset] = (byte)ch;
            return offset+1;
        }
        if (ch < 0x800){
            output[offset]= (byte)(((ch & 0x7c0) >> 6) | 0xc0);
            output[offset+1] = (byte)((ch & 0x3f) | 0x80);
            return offset+2;
        }
        if (ch < 0xe000){
            output[offset]= (byte)(((ch & 0xf000) >> 12) | 0xe0);
            output[offset+1] = (byte)(((ch & 0xfc) >> 6) | 0x80);
            output[offset+2] = (byte)((ch & 0x3f) | 0x80);
            return offset+3;
        }
        
        output[offset]= (byte)(((ch & 0x1c0000) >> 18) | 0xf0);
        output[offset+1] = (byte)(((ch & 0x3f0) >> 12) | 0x80);
        output[offset+2] = (byte)(((ch & 0xfc) >> 6) | 0x80);
        output[offset+3] = (byte)((ch & 0x3f) | 0x80);

        return offset+4;        
    }
    
    public static final void encodeAndWrite(OutputStream os, int ch)
    throws IOException, TranscodeException {
        if (ch < 128){
            os.write(ch);
            return;
        }
        if (ch < 0x800){
            os.write(((ch & 0x7c0) >> 6) | 0xc0);
            os.write((ch & 0x3f) | 0x80);
            return;
        }
        if (ch < 0xe000){
            os.write(((ch & 0xf000) >> 12) | 0xe0);
            os.write(((ch & 0xfc) >> 6) | 0x80);
            os.write((ch & 0x3f) | 0x80);
            return;
        }
        os.write(((ch & 0x1c0000) >> 18) | 0xf0);
        os.write(((ch & 0x3f0) >> 12) | 0x80);
        os.write(((ch & 0xfc) >> 6) | 0x80);
        os.write((ch & 0x3f) | 0x80);
    }
    /**
     * Decode a UTF-8 char in the input buffer
     * @param input the byte array containing UTF-8 chars
     * @return a long whose lower 32-bits is the char at the given offset  
     *         upper 32-bits is the offset of next char in input
     *
     */
    public static long decode(byte[] input, int offset){
       long l = 0;
       int c=0;
       byte val = input[offset];
       if (val > 0){
           l = offset + 1;
           return (l<<32) | val; 
       }
       
       if ((val & 0xe0 )== 0xc0){
           l = offset + 2;
           c = (((int) (val& 0x1f))<< 6)| (input[offset+1] & 0x3f);
           return (l<<32) | c;
       }
       
       if ((val & 0xf0) == 0xe0){
           l = offset + 3;
           c = (((int) (val& 0x0f))<<12) | 
               (((int)input[offset+1] & 0x3f)<<6) |
               (input[offset+2] & 0x3f);
           return (l<<32) | c;
       }
       
       l = offset+4;
       c = (((int) (val& 0x07))<<18) | 
       	   (((int)input[offset+1] & 0x3f)<<12) |
           (((int)input[offset+2] & 0x3f)<< 6) |
           (input[offset+3] & 0x3f);
       
       return (l<<32) | c;
    }
    
    /**
     * Get the length (in UTF-8 representation) of the UCS char at the offset value 
     * @param input
     * @return a long whose upper 32-bits is next offset value
     * 			    and whose lower 32-bits is length in byte
     *
     */
    public static int getLen(int ch) throws TranscodeException{
        if (ch < 128)
            return 1;
        if (ch < 0x800)
            return 2;
        if (ch < 0xe000)
            return 3;
        if (ch < 0x10000)
            return 4;
        return 5;
    }
}

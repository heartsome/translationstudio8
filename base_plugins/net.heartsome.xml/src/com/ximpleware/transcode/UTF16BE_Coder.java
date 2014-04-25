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

import com.ximpleware.TranscodeException;


public class UTF16BE_Coder {
    public static int encode(byte[] output, int offset,int ch){
        if (ch<0x10000){ 
            output[offset+1] = (byte)(ch & 0xff);
            output[offset] =  (byte)((ch & 0xff00) >> 8);
            return 2 + offset;
        } else {
            int tmp = ch-0x10000;
            int w1 = 0xd800 | (tmp & 0xffc00);
            int w2 = 0xdc00 | (tmp & 0x3ff);
            output[offset] = (byte)((w1 & 0xff00) >> 8);
            output[offset+1] = (byte)(w1 & 0xff);
            output[offset+2] =(byte)((w2 & 0xff00) >> 8);
            output[offset+3] =(byte)((w2 & 0xff));

            return 4 + offset;
        }
    }
    
    public static long decode(byte[] input, int offset ){
        int val; 
        long l;
        int temp = (input[offset] << 8) | input[offset+1];
        if (temp < 0xd800 || temp > 0xdfff) { 
            l = offset+2;
            return (l<<32)|temp;
        }else {
            val = temp;
            temp = (input[offset+2] << 8) | input[offset+3];
            val = ((temp - 0xd800) << 10) + (val - 0xdc00) + 0x10000;
            l = offset+4;
            return (l<<32)|temp;
        }
    }
    
    public static int getLen(int ch){
        if (ch <0x10000)
            return 2;
        else
            return 4;
    }
    
    public static final void encodeAndWrite(OutputStream os, int ch)
    throws IOException, TranscodeException {
        
        if (ch<0x10000){ 
            //output[offset] =  (byte)((ch & 0xff00) >> 8);
            os.write((ch & 0xff00) >> 8);
            //output[offset+1] = (byte)(ch & 0xff);
            os.write(ch);
            //return 2 + offset;
        } else {
            int tmp = ch-0x10000;
            int w1 = 0xd800 | (tmp & 0xffc00);
            int w2 = 0xdc00 | (tmp & 0x3ff);
            os.write((byte)((w1 & 0xff00) >> 8));
            os.write((byte)(w1 & 0xff));
            os.write((byte)((w2 & 0xff00) >> 8));
            os.write((byte)(w2 & 0xff));
        }
    }
    
}

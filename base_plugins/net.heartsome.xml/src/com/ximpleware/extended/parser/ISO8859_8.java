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
package com.ximpleware.extended.parser;

/**
 * this class contains method to map a ISO-8859-8 char
 * into a Unicode char
 * 
 */
public class ISO8859_8 {
    static final char[] chars = new char[0x100];
    static {
        for (int i=0;i<128;i++){
            chars[i] = (char)i;
        }
        for (int i=128;i<256;i++){
            chars[i]=0xfffd;
        }
        chars[0xAA	]=0x00D7;
        chars[0xAB	]=0x00AB;
        chars[0xAC	]=0x00AC;
        chars[0xAD	]=0x00AD;
        chars[0xAE	]=0x00AE;
        chars[0xAF	]=0x203E;
        chars[0xB0	]=0x00B0;
        chars[0xB1	]=0x00B1;
        chars[0xB2	]=0x00B2;
        chars[0xB3	]=0x00B3;
        chars[0xB4	]=0x00B4;
        chars[0xB5	]=0x00B5;
        chars[0xB6	]=0x00B6;
        chars[0xB7	]=0x00B7;
        chars[0xB8	]=0x00B8;
        chars[0xB9	]=0x00B9;
        chars[0xBA	]=0x00F7;
        chars[0xBB	]=0x00BB;
        chars[0xBC	]=0x00BC;
        chars[0xBD	]=0x00BD;
        chars[0xBE	]=0x00BE;
        chars[0xDF	]=0x2017;
        chars[0xE0	]=0x05D0;
        chars[0xE1	]=0x05D1;
        chars[0xE2	]=0x05D2;
        chars[0xE3	]=0x05D3;
        chars[0xE4	]=0x05D4;
        chars[0xE5	]=0x05D5;
        chars[0xE6	]=0x05D6;
        chars[0xE7	]=0x05D7;
        chars[0xE8	]=0x05D8;
        chars[0xE9	]=0x05D9;
        chars[0xEA	]=0x05DA;
        chars[0xEB	]=0x05DB;
        chars[0xEC	]=0x05DC;
        chars[0xED	]=0x05DD;
        chars[0xEE	]=0x05DE;
        chars[0xEF	]=0x05DF;
        chars[0xF0	]=0x05E0;
        chars[0xF1	]=0x05E1;
        chars[0xF2	]=0x05E2;
        chars[0xF3	]=0x05E3;
        chars[0xF4	]=0x05E4;
        chars[0xF5	]=0x05E5;
        chars[0xF6	]=0x05E6;
        chars[0xF7	]=0x05E7;
        chars[0xF8	]=0x05E8;
        chars[0xF9	]=0x05E9;
        chars[0xFA	]=0x05EA;

    }
    
    public static char decode(byte b){
        return chars[b & 0xff];
    }
}

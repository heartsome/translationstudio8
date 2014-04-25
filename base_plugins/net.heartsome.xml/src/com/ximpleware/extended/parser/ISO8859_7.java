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
 * this class contains method to map a ISO-8859-7 char
 * into a Unicode char
 * 
 */
public class ISO8859_7 {
    static final char[] chars = new char[0x100];
    static {
        for (int i=0;i<128;i++){
            chars[i] = (char)i;
        }
        for (int i=128;i<256;i++){
            chars[i]=0xfffd;
        }
        chars[0xA0	]=0x00A0;
        chars[0xA1	]=0x02BD;
        chars[0xA2	]=0x02BC;
        chars[0xA3	]=0x00A3;
        chars[0xA6	]=0x00A6;
        chars[0xA7	]=0x00A7;
        chars[0xA8	]=0x00A8;
        chars[0xA9	]=0x00A9;
        chars[0xAB	]=0x00AB;
        chars[0xAC	]=0x00AC;
        chars[0xAD	]=0x00AD;
        chars[0xAF	]=0x2015;
        chars[0xB0	]=0x00B0;
        chars[0xB1	]=0x00B1;
        chars[0xB2	]=0x00B2;
        chars[0xB3	]=0x00B3;
        chars[0xB4	]=0x0384;
        chars[0xB5	]=0x0385;
        chars[0xB6	]=0x0386;
        chars[0xB7	]=0x00B7;
        chars[0xB8	]=0x0388;
        chars[0xB9	]=0x0389;
        chars[0xBA	]=0x038A;
        chars[0xBB	]=0x00BB;
        chars[0xBC	]=0x038C;
        chars[0xBD	]=0x00BD;
        chars[0xBE	]=0x038E;
        chars[0xBF	]=0x038F;
        chars[0xC0	]=0x0390;
        chars[0xC1	]=0x0391;
        chars[0xC2	]=0x0392;
        chars[0xC3	]=0x0393;
        chars[0xC4	]=0x0394;
        chars[0xC5	]=0x0395;
        chars[0xC6	]=0x0396;
        chars[0xC7	]=0x0397;
        chars[0xC8	]=0x0398;
        chars[0xC9	]=0x0399;
        chars[0xCA	]=0x039A;
        chars[0xCB	]=0x039B;
        chars[0xCC	]=0x039C;
        chars[0xCD	]=0x039D;
        chars[0xCE	]=0x039E;
        chars[0xCF	]=0x039F;
        chars[0xD0	]=0x03A0;
        chars[0xD1	]=0x03A1;
        chars[0xD3	]=0x03A3;
        chars[0xD4	]=0x03A4;
        chars[0xD5	]=0x03A5;
        chars[0xD6	]=0x03A6;
        chars[0xD7	]=0x03A7;
        chars[0xD8	]=0x03A8;
        chars[0xD9	]=0x03A9;
        chars[0xDA	]=0x03AA;
        chars[0xDB	]=0x03AB;
        chars[0xDC	]=0x03AC;
        chars[0xDD	]=0x03AD;
        chars[0xDE	]=0x03AE;
        chars[0xDF	]=0x03AF;
        chars[0xE0	]=0x03B0;
        chars[0xE1	]=0x03B1;
        chars[0xE2	]=0x03B2;
        chars[0xE3	]=0x03B3;
        chars[0xE4	]=0x03B4;
        chars[0xE5	]=0x03B5;
        chars[0xE6	]=0x03B6;
        chars[0xE7	]=0x03B7;
        chars[0xE8	]=0x03B8;
        chars[0xE9	]=0x03B9;
        chars[0xEA	]=0x03BA;
        chars[0xEB	]=0x03BB;
        chars[0xEC	]=0x03BC;
        chars[0xED	]=0x03BD;
        chars[0xEE	]=0x03BE;
        chars[0xEF	]=0x03BF;
        chars[0xF0	]=0x03C0;
        chars[0xF1	]=0x03C1;
        chars[0xF2	]=0x03C2;
        chars[0xF3	]=0x03C3;
        chars[0xF4	]=0x03C4;
        chars[0xF5	]=0x03C5;
        chars[0xF6	]=0x03C6;
        chars[0xF7	]=0x03C7;
        chars[0xF8	]=0x03C8;
        chars[0xF9	]=0x03C9;
        chars[0xFA	]=0x03CA;
        chars[0xFB	]=0x03CB;
        chars[0xFC	]=0x03CC;
        chars[0xFD	]=0x03CD;
        chars[0xFE	]=0x03CE;

    }
    
    public static char decode(byte b){
        return chars[b & 0xff];
    }
    
}

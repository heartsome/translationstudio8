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
 * this class contains method to map a ISO-8859-4 char
 * into a Unicode char
 * 
 */
public class ISO8859_4 {
    static final char[] chars = new char[0x100];
    static {
        for (int i=0;i<128;i++){
            chars[i] = (char)i;
        }
        for (int i=128;i<256;i++){
            chars[i]=0xfffd;
        }
        chars[0xA0	]=0x00A0;
        chars[0xA1	]=0x0104;
        chars[0xA2	]=0x0138;
        chars[0xA3	]=0x0156;
        chars[0xA4	]=0x00A4;
        chars[0xA5	]=0x0128;
        chars[0xA6	]=0x013B;
        chars[0xA7	]=0x00A7;
        chars[0xA8	]=0x00A8;
        chars[0xA9	]=0x0160;
        chars[0xAA	]=0x0112;
        chars[0xAB	]=0x0122;
        chars[0xAC	]=0x0166;
        chars[0xAD	]=0x00AD;
        chars[0xAE	]=0x017D;
        chars[0xAF	]=0x00AF;
        chars[0xB0	]=0x00B0;
        chars[0xB1	]=0x0105;
        chars[0xB2	]=0x02DB;
        chars[0xB3	]=0x0157;
        chars[0xB4	]=0x00B4;
        chars[0xB5	]=0x0129;
        chars[0xB6	]=0x013C;
        chars[0xB7	]=0x02C7;
        chars[0xB8	]=0x00B8;
        chars[0xB9	]=0x0161;
        chars[0xBA	]=0x0113;
        chars[0xBB	]=0x0123;
        chars[0xBC	]=0x0167;
        chars[0xBD	]=0x014A;
        chars[0xBE	]=0x017E;
        chars[0xBF	]=0x014B;
        chars[0xC0	]=0x0100;
        chars[0xC1	]=0x00C1;
        chars[0xC2	]=0x00C2;
        chars[0xC3	]=0x00C3;
        chars[0xC4	]=0x00C4;
        chars[0xC5	]=0x00C5;
        chars[0xC6	]=0x00C6;
        chars[0xC7	]=0x012E;
        chars[0xC8	]=0x010C;
        chars[0xC9	]=0x00C9;
        chars[0xCA	]=0x0118;
        chars[0xCB	]=0x00CB;
        chars[0xCC	]=0x0116;
        chars[0xCD	]=0x00CD;
        chars[0xCE	]=0x00CE;
        chars[0xCF	]=0x012A;
        chars[0xD0	]=0x0110;
        chars[0xD1	]=0x0145;
        chars[0xD2	]=0x014C;
        chars[0xD3	]=0x0136;
        chars[0xD4	]=0x00D4;
        chars[0xD5	]=0x00D5;
        chars[0xD6	]=0x00D6;
        chars[0xD7	]=0x00D7;
        chars[0xD8	]=0x00D8;
        chars[0xD9	]=0x0172;
        chars[0xDA	]=0x00DA;
        chars[0xDB	]=0x00DB;
        chars[0xDC	]=0x00DC;
        chars[0xDD	]=0x0168;
        chars[0xDE	]=0x016A;
        chars[0xDF	]=0x00DF;
        chars[0xE0	]=0x0101;
        chars[0xE1	]=0x00E1;
        chars[0xE2	]=0x00E2;
        chars[0xE3	]=0x00E3;
        chars[0xE4	]=0x00E4;
        chars[0xE5	]=0x00E5;
        chars[0xE6	]=0x00E6;
        chars[0xE7	]=0x012F;
        chars[0xE8	]=0x010D;
        chars[0xE9	]=0x00E9;
        chars[0xEA	]=0x0119;
        chars[0xEB	]=0x00EB;
        chars[0xEC	]=0x0117;
        chars[0xED	]=0x00ED;
        chars[0xEE	]=0x00EE;
        chars[0xEF	]=0x012B;
        chars[0xF0	]=0x0111;
        chars[0xF1	]=0x0146;
        chars[0xF2	]=0x014D;
        chars[0xF3	]=0x0137;
        chars[0xF4	]=0x00F4;
        chars[0xF5	]=0x00F5;
        chars[0xF6	]=0x00F6;
        chars[0xF7	]=0x00F7;
        chars[0xF8	]=0x00F8;
        chars[0xF9	]=0x0173;
        chars[0xFA	]=0x00FA;
        chars[0xFB	]=0x00FB;
        chars[0xFC	]=0x00FC;
        chars[0xFD	]=0x0169;
        chars[0xFE	]=0x016B;
        chars[0xFF	]=0x02D9;
    }
    
    public static char decode(byte b){
        return chars[b & 0xff];
    }
}

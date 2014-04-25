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
package com.ximpleware.parser;

/**
 * this class contains method to map a ISO-8859-5 char
 * into a Unicode char
 * 
 */
public class ISO8859_5 {
    static final char[] chars = new char[0x100];
    static {
        for (int i=0;i<128;i++){
            chars[i] = (char)i;
        }
        for (int i=128;i<256;i++){
            chars[i]=0xfffd;
        }
        chars[0xA0	]=0x00A0;
        chars[0xA1	]=0x0401;
        chars[0xA2	]=0x0402;
        chars[0xA3	]=0x0403;
        chars[0xA4	]=0x0404;
        chars[0xA5	]=0x0405;
        chars[0xA6	]=0x0406;
        chars[0xA7	]=0x0407;
        chars[0xA8	]=0x0408;
        chars[0xA9	]=0x0409;
        chars[0xAA	]=0x040A;
        chars[0xAB	]=0x040B;
        chars[0xAC	]=0x040C;
        chars[0xAD	]=0x00AD;
        chars[0xAE	]=0x040E;
        chars[0xAF	]=0x040F;
        chars[0xB0	]=0x0410;
        chars[0xB1	]=0x0411;
        chars[0xB2	]=0x0412;
        chars[0xB3	]=0x0413;
        chars[0xB4	]=0x0414;
        chars[0xB5	]=0x0415;
        chars[0xB6	]=0x0416;
        chars[0xB7	]=0x0417;
        chars[0xB8	]=0x0418;
        chars[0xB9	]=0x0419;
        chars[0xBA	]=0x041A;
        chars[0xBB	]=0x041B;
        chars[0xBC	]=0x041C;
        chars[0xBD	]=0x041D;
        chars[0xBE	]=0x041E;
        chars[0xBF	]=0x041F;
        chars[0xC0	]=0x0420;
        chars[0xC1	]=0x0421;
        chars[0xC2	]=0x0422;
        chars[0xC3	]=0x0423;
        chars[0xC4	]=0x0424;
        chars[0xC5	]=0x0425;
        chars[0xC6	]=0x0426;
        chars[0xC7	]=0x0427;
        chars[0xC8	]=0x0428;
        chars[0xC9	]=0x0429;
        chars[0xCA	]=0x042A;
        chars[0xCB	]=0x042B;
        chars[0xCC	]=0x042C;
        chars[0xCD	]=0x042D;
        chars[0xCE	]=0x042E;
        chars[0xCF	]=0x042F;
        chars[0xD0	]=0x0430;
        chars[0xD1	]=0x0431;
        chars[0xD2	]=0x0432;
        chars[0xD3	]=0x0433;
        chars[0xD4	]=0x0434;
        chars[0xD5	]=0x0435;
        chars[0xD6	]=0x0436;
        chars[0xD7	]=0x0437;
        chars[0xD8	]=0x0438;
        chars[0xD9	]=0x0439;
        chars[0xDA	]=0x043A;
        chars[0xDB	]=0x043B;
        chars[0xDC	]=0x043C;
        chars[0xDD	]=0x043D;
        chars[0xDE	]=0x043E;
        chars[0xDF	]=0x043F;
        chars[0xE0	]=0x0440;
        chars[0xE1	]=0x0441;
        chars[0xE2	]=0x0442;
        chars[0xE3	]=0x0443;
        chars[0xE4	]=0x0444;
        chars[0xE5	]=0x0445;
        chars[0xE6	]=0x0446;
        chars[0xE7	]=0x0447;
        chars[0xE8	]=0x0448;
        chars[0xE9	]=0x0449;
        chars[0xEA	]=0x044A;
        chars[0xEB	]=0x044B;
        chars[0xEC	]=0x044C;
        chars[0xED	]=0x044D;
        chars[0xEE	]=0x044E;
        chars[0xEF	]=0x044F;
        chars[0xF0	]=0x2116;
        chars[0xF1	]=0x0451;
        chars[0xF2	]=0x0452;
        chars[0xF3	]=0x0453;
        chars[0xF4	]=0x0454;
        chars[0xF5	]=0x0455;
        chars[0xF6	]=0x0456;
        chars[0xF7	]=0x0457;
        chars[0xF8	]=0x0458;
        chars[0xF9	]=0x0459;
        chars[0xFA	]=0x045A;
        chars[0xFB	]=0x045B;
        chars[0xFC	]=0x045C;
        chars[0xFD	]=0x00A7;
        chars[0xFE	]=0x045E;
        chars[0xFF	]=0x045F;

    }
    
    public static char decode(byte b){
        return chars[b & 0xff];
    }
}

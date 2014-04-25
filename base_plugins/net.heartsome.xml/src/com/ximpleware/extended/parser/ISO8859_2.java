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
 * this class contains method to map a ISO-8859-3 char
 * into a Unicode char
 * 
 */
public class ISO8859_2 {
    static final char[] chars = new char[0x100];
    static {
        for (int i=0;i<128;i++){
            chars[i] = (char)i;
        }
        for (int i=128;i<256;i++){
            chars[i]=0xfffd;
        }
        chars[0xA0]=	0x00A0; //	NO-BREAK SPACE
        chars[0xA1]=	0x0104;//	LATIN CAPITAL LETTER A WITH OGONEK
        chars[0xA2]=	0x02D8;//	BREVE
        chars[0xA3]=	0x0141;	//	LATIN CAPITAL LETTER L WITH STROKE
        chars[0xA4]=	0x00A4;	//	CURRENCY SIGN
        chars[0xA5]=	0x013D;	//	LATIN CAPITAL LETTER L WITH CARON
        chars[0xA6]=	0x015A;	//	LATIN CAPITAL LETTER S WITH ACUTE
        chars[0xA7]=	0x00A7;	//	SECTION SIGN
        chars[0xA8]=	0x00A8;	//	DIAERESIS
        chars[0xA9]=	0x0160;	//	LATIN CAPITAL LETTER S WITH CARON
        chars[0xAA]=	0x015E;	//	LATIN CAPITAL LETTER S WITH CEDILLA
        chars[0xAB]=	0x0164;	//	LATIN CAPITAL LETTER T WITH CARON
        chars[0xAC]=	0x0179;	//	LATIN CAPITAL LETTER Z WITH ACUTE
        chars[0xAD]=	0x00AD;	//	SOFT HYPHEN
        chars[0xAE]=	0x017D;	//	LATIN CAPITAL LETTER Z WITH CARON
        chars[0xAF]=	0x017B;	//	LATIN CAPITAL LETTER Z WITH DOT ABOVE
        chars[0xB0]=	0x00B0;	//	DEGREE SIGN
        chars[0xB1]=	0x0105;	//	LATIN SMALL LETTER A WITH OGONEK
        chars[0xB2]=	0x02DB;	//	OGONEK
        chars[0xB3]=	0x0142;	//	LATIN SMALL LETTER L WITH STROKE
        chars[0xB4]=	0x00B4;	//	ACUTE ACCENT
        chars[0xB5]=	0x013E;	//	LATIN SMALL LETTER L WITH CARON
        chars[0xB6]=	0x015B;	//	LATIN SMALL LETTER S WITH ACUTE
        chars[0xB7]=	0x02C7;	//	CARON
        chars[0xB8]=	0x00B8;	//	CEDILLA
        chars[0xB9]=	0x0161;	//	LATIN SMALL LETTER S WITH CARON
        chars[0xBA]=	0x015F;	//	LATIN SMALL LETTER S WITH CEDILLA
        chars[0xBB]=	0x0165;	//	LATIN SMALL LETTER T WITH CARON
        chars[0xBC]=	0x017A;	//	LATIN SMALL LETTER Z WITH ACUTE
        chars[0xBD]=	0x02DD;	//	DOUBLE ACUTE ACCENT
        chars[0xBE]=	0x017E;	//	LATIN SMALL LETTER Z WITH CARON
        chars[0xBF]=	0x017C;	//	LATIN SMALL LETTER Z WITH DOT ABOVE
        chars[0xC0]=	0x0154;	//	LATIN CAPITAL LETTER R WITH ACUTE
        chars[0xC1]=	0x00C1;	//	LATIN CAPITAL LETTER A WITH ACUTE
        chars[0xC2]=	0x00C2;	//	LATIN CAPITAL LETTER A WITH CIRCUMFLEX
        chars[0xC3]=	0x0102;	//	LATIN CAPITAL LETTER A WITH BREVE
        chars[0xC4]=	0x00C4;	//	LATIN CAPITAL LETTER A WITH DIAERESIS
        chars[0xC5]=	0x0139;	//	LATIN CAPITAL LETTER L WITH ACUTE
        chars[0xC6]=	0x0106;	//	LATIN CAPITAL LETTER C WITH ACUTE
        chars[0xC7]=	0x00C7;	//	LATIN CAPITAL LETTER C WITH CEDILLA
        chars[0xC8]=	0x010C;	//	LATIN CAPITAL LETTER C WITH CARON
        chars[0xC9]=	0x00C9;	//	LATIN CAPITAL LETTER E WITH ACUTE
        chars[0xCA]=	0x0118	;	//LATIN CAPITAL LETTER E WITH OGONEK
        chars[0xCB]=	0x00CB;	//	LATIN CAPITAL LETTER E WITH DIAERESIS
        chars[0xCC]=	0x011A;	//	LATIN CAPITAL LETTER E WITH CARON
        chars[0xCD]=	0x00CD;	//	LATIN CAPITAL LETTER I WITH ACUTE
        chars[0xCE]=	0x00CE;	//	LATIN CAPITAL LETTER I WITH CIRCUMFLEX
        chars[0xCF]=	0x010E;	//	LATIN CAPITAL LETTER D WITH CARON
        chars[0xD0]=	0x0110;	//	LATIN CAPITAL LETTER D WITH STROKE
        chars[0xD1]=	0x0143;	//	LATIN CAPITAL LETTER N WITH ACUTE
        chars[0xD2]=	0x0147;	//	LATIN CAPITAL LETTER N WITH CARON
        chars[0xD3]=	0x00D3;	//	LATIN CAPITAL LETTER O WITH ACUTE
        chars[0xD4]=	0x00D4;	//	LATIN CAPITAL LETTER O WITH CIRCUMFLEX
        chars[0xD5]=	0x0150;	//	LATIN CAPITAL LETTER O WITH DOUBLE ACUTE
        chars[0xD6]=	0x00D6;	//	LATIN CAPITAL LETTER O WITH DIAERESIS
        chars[0xD7]=	0x00D7;	//	MULTIPLICATION SIGN
        chars[0xD8]=	0x0158;	//	LATIN CAPITAL LETTER R WITH CARON
        chars[0xD9]=	0x016E;	//	LATIN CAPITAL LETTER U WITH RING ABOVE
        chars[0xDA]=	0x00DA;	//	LATIN CAPITAL LETTER U WITH ACUTE
        chars[0xDB]=	0x0170;	//	LATIN CAPITAL LETTER U WITH DOUBLE ACUTE
        chars[0xDC]=	0x00DC;	//	LATIN CAPITAL LETTER U WITH DIAERESIS
        chars[0xDD]=	0x00DD;	//	LATIN CAPITAL LETTER Y WITH ACUTE
        chars[0xDE]=	0x0162;	//	LATIN CAPITAL LETTER T WITH CEDILLA
        chars[0xDF]=	0x00DF;	//	LATIN SMALL LETTER SHARP S
        chars[0xE0]=	0x0155;	//	LATIN SMALL LETTER R WITH ACUTE
        chars[0xE1]=	0x00E1;	//	LATIN SMALL LETTER A WITH ACUTE
        chars[0xE2]=	0x00E2;	//	LATIN SMALL LETTER A WITH CIRCUMFLEX
        chars[0xE3]=	0x0103;	//	LATIN SMALL LETTER A WITH BREVE
        chars[0xE4]=	0x00E4;	//	LATIN SMALL LETTER A WITH DIAERESIS
        chars[0xE5]=	0x013A;	//	LATIN SMALL LETTER L WITH ACUTE
        chars[0xE6]=	0x0107;	//	LATIN SMALL LETTER C WITH ACUTE
        chars[0xE7]=	0x00E7;	//	LATIN SMALL LETTER C WITH CEDILLA
        chars[0xE8]=	0x010D;	//	LATIN SMALL LETTER C WITH CARON
        chars[0xE9]=	0x00E9;	//	LATIN SMALL LETTER E WITH ACUTE
        chars[0xEA]=	0x0119;	//	LATIN SMALL LETTER E WITH OGONEK
        chars[0xEB]=	0x00EB;	//	LATIN SMALL LETTER E WITH DIAERESIS
        chars[0xEC]=	0x011B;	//	LATIN SMALL LETTER E WITH CARON
        chars[0xED]=	0x00ED;	//	LATIN SMALL LETTER I WITH ACUTE
        chars[0xEE]=	0x00EE;	//	LATIN SMALL LETTER I WITH CIRCUMFLEX
        chars[0xEF]=	0x010F;	//	LATIN SMALL LETTER D WITH CARON
        chars[0xF0]=	0x0111;	//	LATIN SMALL LETTER D WITH STROKE
        chars[0xF1]=	0x0144;	//	LATIN SMALL LETTER N WITH ACUTE
        chars[0xF2]=	0x0148;	//	LATIN SMALL LETTER N WITH CARON
        chars[0xF3]=	0x00F3;	//	LATIN SMALL LETTER O WITH ACUTE
        chars[0xF4]=	0x00F4;	//	LATIN SMALL LETTER O WITH CIRCUMFLEX
        chars[0xF5]=	0x0151;	//	LATIN SMALL LETTER O WITH DOUBLE ACUTE
        chars[0xF6]=	0x00F6;	//	LATIN SMALL LETTER O WITH DIAERESIS
        chars[0xF7]=	0x00F7;	//	DIVISION SIGN
        chars[0xF8]=	0x0159;	//	LATIN SMALL LETTER R WITH CARON
        chars[0xF9]=	0x016F;	//LATIN SMALL LETTER U WITH RING ABOVE
        chars[0xFA]=	0x00FA;	//	LATIN SMALL LETTER U WITH ACUTE
        chars[0xFB]=	0x0171;	//	LATIN SMALL LETTER U WITH DOUBLE ACUTE
        chars[0xFC]=	0x00FC;	//LATIN SMALL LETTER U WITH DIAERESIS
        chars[0xFD]=	0x00FD;	//	LATIN SMALL LETTER Y WITH ACUTE
        chars[0xFE]=	0x0163;	//LATIN SMALL LETTER T WITH CEDILLA
        chars[0xFF]=	0x02D9;	//	DOT ABOVE
    }
    
    public static char decode(byte b){
        return chars[b & 0xff];
    }
}

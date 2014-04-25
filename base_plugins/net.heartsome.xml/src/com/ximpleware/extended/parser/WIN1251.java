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
 * This class contains method to map a windows-1251 char
 * into a Unicode char
 * 
 */
public class WIN1251 {
    static final char[] chars = new char[0x100];
    static {
        for (int i=0;i<128;i++){
            chars[i] = (char)i;
        }
        for (int i=128;i<256;i++){
            chars[i]=0xfffd;
        }
        chars[0x80 ]=0x0402  ;// CYRILLIC CAPITAL LETTER DJE
        chars[0x81 ]=0x0403  ;// CYRILLIC CAPITAL LETTER GJE
        chars[0x82 ]=0x201A  ;// SINGLE LOW-9 QUOTATION MARK
        chars[0x83 ]=0x0453  ;// CYRILLIC SMALL LETTER GJE
        chars[0x84 ]=0x201E  ;// DOUBLE LOW-9 QUOTATION MARK
        chars[0x85 ]=0x2026  ;// HORIZONTAL ELLIPSIS
        chars[0x86 ]=0x2020  ;// DAGGER
        chars[0x87 ]=0x2021  ;// DOUBLE DAGGER
        chars[0x88 ]=0x20AC  ;// EURO SIGN
        chars[0x89 ]=0x2030  ;// PER MILLE SIGN
        chars[0x8A ]=0x0409  ;// CYRILLIC CAPITAL LETTER LJE
        chars[0x8B ]=0x2039  ;// SINGLE LEFT-POINTING ANGLE QUOTATION MARK
        chars[0x8C ]=0x040A  ;// CYRILLIC CAPITAL LETTER NJE
        chars[0x8D ]=0x040C  ;// CYRILLIC CAPITAL LETTER KJE
        chars[0x8E ]=0x040B  ;// CYRILLIC CAPITAL LETTER TSHE
        chars[0x8F ]=0x040F  ;// CYRILLIC CAPITAL LETTER DZHE
        chars[0x90 ]=0x0452  ;// CYRILLIC SMALL LETTER DJE
        chars[0x91 ]=0x2018  ;// LEFT SINGLE QUOTATION MARK
        chars[0x92 ]=0x2019  ;// RIGHT SINGLE QUOTATION MARK
        chars[0x93 ]=0x201C  ;// LEFT DOUBLE QUOTATION MARK
        chars[0x94 ]=0x201D  ;// RIGHT DOUBLE QUOTATION MARK
        chars[0x95 ]=0x2022  ;// BULLET
        chars[0x96 ]=0x2013  ;// EN DASH
        chars[0x97 ]=0x2014  ;// EM DASH
        chars[0x99 ]=0x2122  ;// TRADE MARK SIGN
        chars[0x9A ]=0x0459  ;// CYRILLIC SMALL LETTER LJE
        chars[0x9B ]=0x203A  ;// SINGLE RIGHT-POINTING ANGLE QUOTATION MARK
        chars[0x9C ]=0x045A  ;// CYRILLIC SMALL LETTER NJE
        chars[0x9D ]=0x045C  ;// CYRILLIC SMALL LETTER KJE
        chars[0x9E ]=0x045B  ;// CYRILLIC SMALL LETTER TSHE
        chars[0x9F ]=0x045F  ;// CYRILLIC SMALL LETTER DZHE
        chars[0xA0 ]=0x00A0  ;// NO-BREAK SPACE
        chars[0xA1 ]=0x040E  ;// CYRILLIC CAPITAL LETTER SHORT U
        chars[0xA2 ]=0x045E  ;// CYRILLIC SMALL LETTER SHORT U
        chars[0xA3 ]=0x0408  ;// CYRILLIC CAPITAL LETTER JE
        chars[0xA4 ]=0x00A4  ;// CURRENCY SIGN
        chars[0xA5 ]=0x0490  ;// CYRILLIC CAPITAL LETTER GHE WITH UPTURN
        chars[0xA6 ]=0x00A6  ;// BROKEN BAR
        chars[0xA7 ]=0x00A7  ;// SECTION SIGN
        chars[0xA8 ]=0x0401  ;// CYRILLIC CAPITAL LETTER IO
        chars[0xA9 ]=0x00A9  ;// COPYRIGHT SIGN
        chars[0xAA ]=0x0404  ;// CYRILLIC CAPITAL LETTER UKRAINIAN IE
        chars[0xAB ]=0x00AB  ;// LEFT-POINTING DOUBLE ANGLE QUOTATION MARK
        chars[0xAC ]=0x00AC  ;// NOT SIGN
        chars[0xAD ]=0x00AD  ;// SOFT HYPHEN
        chars[0xAE ]=0x00AE  ;// REGISTERED SIGN
        chars[0xAF ]=0x0407  ;// CYRILLIC CAPITAL LETTER YI
        chars[0xB0 ]=0x00B0  ;// DEGREE SIGN
        chars[0xB1 ]=0x00B1  ;// PLUS-MINUS SIGN
        chars[0xB2 ]=0x0406  ;// CYRILLIC CAPITAL LETTER BYELORUSSIAN-UKRAINIAN I
        chars[0xB3 ]=0x0456  ;// CYRILLIC SMALL LETTER BYELORUSSIAN-UKRAINIAN I
        chars[0xB4 ]=0x0491  ;// CYRILLIC SMALL LETTER GHE WITH UPTURN
        chars[0xB5 ]=0x00B5  ;// MICRO SIGN
        chars[0xB6 ]=0x00B6  ;// PILCROW SIGN
        chars[0xB7 ]=0x00B7  ;// MIDDLE DOT
        chars[0xB8 ]=0x0451  ;// CYRILLIC SMALL LETTER IO
        chars[0xB9 ]=0x2116  ;// NUMERO SIGN
        chars[0xBA ]=0x0454  ;// CYRILLIC SMALL LETTER UKRAINIAN IE
        chars[0xBB ]=0x00BB  ;// RIGHT-POINTING DOUBLE ANGLE QUOTATION MARK
        chars[0xBC ]=0x0458  ;// CYRILLIC SMALL LETTER JE
        chars[0xBD ]=0x0405  ;// CYRILLIC CAPITAL LETTER DZE
        chars[0xBE ]=0x0455  ;// CYRILLIC SMALL LETTER DZE
        chars[0xBF ]=0x0457  ;// CYRILLIC SMALL LETTER YI
        chars[0xC0 ]=0x0410  ;// CYRILLIC CAPITAL LETTER A
        chars[0xC1 ]=0x0411  ;// CYRILLIC CAPITAL LETTER BE
        chars[0xC2 ]=0x0412  ;// CYRILLIC CAPITAL LETTER VE
        chars[0xC3 ]=0x0413  ;// CYRILLIC CAPITAL LETTER GHE
        chars[0xC4 ]=0x0414  ;// CYRILLIC CAPITAL LETTER DE
        chars[0xC5 ]=0x0415  ;// CYRILLIC CAPITAL LETTER IE
        chars[0xC6 ]=0x0416  ;// CYRILLIC CAPITAL LETTER ZHE
        chars[0xC7 ]=0x0417  ;// CYRILLIC CAPITAL LETTER ZE
        chars[0xC8 ]=0x0418  ;// CYRILLIC CAPITAL LETTER I
        chars[0xC9 ]=0x0419  ;// CYRILLIC CAPITAL LETTER SHORT I
        chars[0xCA ]=0x041A  ;// CYRILLIC CAPITAL LETTER KA
        chars[0xCB ]=0x041B  ;// CYRILLIC CAPITAL LETTER EL
        chars[0xCC ]=0x041C  ;// CYRILLIC CAPITAL LETTER EM
        chars[0xCD ]=0x041D  ;// CYRILLIC CAPITAL LETTER EN
        chars[0xCE ]=0x041E  ;// CYRILLIC CAPITAL LETTER O
        chars[0xCF ]=0x041F  ;// CYRILLIC CAPITAL LETTER PE
        chars[0xD0 ]=0x0420  ;// CYRILLIC CAPITAL LETTER ER
        chars[0xD1 ]=0x0421  ;// CYRILLIC CAPITAL LETTER ES
        chars[0xD2 ]=0x0422  ;// CYRILLIC CAPITAL LETTER TE
        chars[0xD3 ]=0x0423  ;// CYRILLIC CAPITAL LETTER U
        chars[0xD4 ]=0x0424  ;// CYRILLIC CAPITAL LETTER EF
        chars[0xD5 ]=0x0425  ;// CYRILLIC CAPITAL LETTER HA
        chars[0xD6 ]=0x0426  ;// CYRILLIC CAPITAL LETTER TSE
        chars[0xD7 ]=0x0427  ;// CYRILLIC CAPITAL LETTER CHE
        chars[0xD8 ]=0x0428  ;// CYRILLIC CAPITAL LETTER SHA
        chars[0xD9 ]=0x0429  ;// CYRILLIC CAPITAL LETTER SHCHA
        chars[0xDA ]=0x042A  ;// CYRILLIC CAPITAL LETTER HARD SIGN
        chars[0xDB ]=0x042B  ;// CYRILLIC CAPITAL LETTER YERU
        chars[0xDC ]=0x042C  ;// CYRILLIC CAPITAL LETTER SOFT SIGN
        chars[0xDD ]=0x042D  ;// CYRILLIC CAPITAL LETTER E
        chars[0xDE ]=0x042E  ;// CYRILLIC CAPITAL LETTER YU
        chars[0xDF ]=0x042F  ;// CYRILLIC CAPITAL LETTER YA
        chars[0xE0 ]=0x0430  ;// CYRILLIC SMALL LETTER A
        chars[0xE1 ]=0x0431  ;// CYRILLIC SMALL LETTER BE
        chars[0xE2 ]=0x0432  ;// CYRILLIC SMALL LETTER VE
        chars[0xE3 ]=0x0433  ;// CYRILLIC SMALL LETTER GHE
        chars[0xE4 ]=0x0434  ;// CYRILLIC SMALL LETTER DE
        chars[0xE5 ]=0x0435  ;// CYRILLIC SMALL LETTER IE
        chars[0xE6 ]=0x0436  ;// CYRILLIC SMALL LETTER ZHE
        chars[0xE7 ]=0x0437  ;// CYRILLIC SMALL LETTER ZE
        chars[0xE8 ]=0x0438  ;// CYRILLIC SMALL LETTER I
        chars[0xE9 ]=0x0439  ;// CYRILLIC SMALL LETTER SHORT I
        chars[0xEA ]=0x043A  ;// CYRILLIC SMALL LETTER KA
        chars[0xEB ]=0x043B  ;// CYRILLIC SMALL LETTER EL
        chars[0xEC ]=0x043C  ;// CYRILLIC SMALL LETTER EM
        chars[0xED ]=0x043D  ;// CYRILLIC SMALL LETTER EN
        chars[0xEE ]=0x043E  ;// CYRILLIC SMALL LETTER O
        chars[0xEF ]=0x043F  ;// CYRILLIC SMALL LETTER PE
        chars[0xF0 ]=0x0440  ;// CYRILLIC SMALL LETTER ER
        chars[0xF1 ]=0x0441  ;// CYRILLIC SMALL LETTER ES
        chars[0xF2 ]=0x0442  ;// CYRILLIC SMALL LETTER TE
        chars[0xF3 ]=0x0443  ;// CYRILLIC SMALL LETTER U
        chars[0xF4 ]=0x0444  ;// CYRILLIC SMALL LETTER EF
        chars[0xF5 ]=0x0445  ;// CYRILLIC SMALL LETTER HA
        chars[0xF6 ]=0x0446  ;// CYRILLIC SMALL LETTER TSE
        chars[0xF7 ]=0x0447  ;// CYRILLIC SMALL LETTER CHE
        chars[0xF8 ]=0x0448  ;// CYRILLIC SMALL LETTER SHA
        chars[0xF9 ]=0x0449  ;// CYRILLIC SMALL LETTER SHCHA
        chars[0xFA ]=0x044A  ;// CYRILLIC SMALL LETTER HARD SIGN
        chars[0xFB ]=0x044B  ;// CYRILLIC SMALL LETTER YERU
        chars[0xFC ]=0x044C  ;// CYRILLIC SMALL LETTER SOFT SIGN
        chars[0xFD ]=0x044D  ;// CYRILLIC SMALL LETTER E
        chars[0xFE ]=0x044E  ;// CYRILLIC SMALL LETTER YU
        chars[0xFF ]=0x044F  ;// CYRILLIC SMALL LETTER YA
    }
    public static char decode(byte b){
        return chars[b & 0xff];
    }
}

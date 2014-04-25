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
 * this class contains method to map a windows-1258 char
 * into a Unicode char
 * 
 */
public class WIN1258 {
    static final char[] chars = new char[0x100];
    static {
        for (int i=0;i<128;i++){
            chars[i] = (char)i;
        }
        for (int i=128;i<256;i++){
            chars[i]=0xfffd;
        }
        chars[0x80 ]=0x20AC  ;// EURO SIGN
        chars[0x82 ]=0x201A  ;// SINGLE LOW-9 QUOTATION MARK
        chars[0x83 ]=0x0192  ;// LATIN SMALL LETTER F WITH HOOK
        chars[0x84 ]=0x201E  ;// DOUBLE LOW-9 QUOTATION MARK
        chars[0x85 ]=0x2026  ;// HORIZONTAL ELLIPSIS
        chars[0x86 ]=0x2020  ;// DAGGER
        chars[0x87 ]=0x2021  ;// DOUBLE DAGGER
        chars[0x88 ]=0x02C6  ;// MODIFIER LETTER CIRCUMFLEX ACCENT
        chars[0x89 ]=0x2030  ;// PER MILLE SIGN
        chars[0x8B ]=0x2039  ;// SINGLE LEFT-POINTING ANGLE QUOTATION MARK
        chars[0x8C ]=0x0152  ;// LATIN CAPITAL LIGATURE OE
        chars[0x91 ]=0x2018  ;// LEFT SINGLE QUOTATION MARK
        chars[0x92 ]=0x2019  ;// RIGHT SINGLE QUOTATION MARK
        chars[0x93 ]=0x201C  ;// LEFT DOUBLE QUOTATION MARK
        chars[0x94 ]=0x201D  ;// RIGHT DOUBLE QUOTATION MARK
        chars[0x95 ]=0x2022  ;// BULLET
        chars[0x96 ]=0x2013  ;// EN DASH
        chars[0x97 ]=0x2014  ;// EM DASH
        chars[0x98 ]=0x02DC  ;// SMALL TILDE
        chars[0x99 ]=0x2122  ;// TRADE MARK SIGN
        chars[0x9B ]=0x203A  ;// SINGLE RIGHT-POINTING ANGLE QUOTATION MARK
        chars[0x9C ]=0x0153  ;// LATIN SMALL LIGATURE OE
        chars[0x9F ]=0x0178  ;// LATIN CAPITAL LETTER Y WITH DIAERESIS
        chars[0xA0 ]=0x00A0  ;// NO-BREAK SPACE
        chars[0xA1 ]=0x00A1  ;// INVERTED EXCLAMATION MARK
        chars[0xA2 ]=0x00A2  ;// CENT SIGN
        chars[0xA3 ]=0x00A3  ;// POUND SIGN
        chars[0xA4 ]=0x00A4  ;// CURRENCY SIGN
        chars[0xA5 ]=0x00A5  ;// YEN SIGN
        chars[0xA6 ]=0x00A6  ;// BROKEN BAR
        chars[0xA7 ]=0x00A7  ;// SECTION SIGN
        chars[0xA8 ]=0x00A8  ;// DIAERESIS
        chars[0xA9 ]=0x00A9  ;// COPYRIGHT SIGN
        chars[0xAA ]=0x00AA  ;// FEMININE ORDINAL INDICATOR
        chars[0xAB ]=0x00AB  ;// LEFT-POINTING DOUBLE ANGLE QUOTATION MARK
        chars[0xAC ]=0x00AC  ;// NOT SIGN
        chars[0xAD ]=0x00AD  ;// SOFT HYPHEN
        chars[0xAE ]=0x00AE  ;// REGISTERED SIGN
        chars[0xAF ]=0x00AF  ;// MACRON
        chars[0xB0 ]=0x00B0  ;// DEGREE SIGN
        chars[0xB1 ]=0x00B1  ;// PLUS-MINUS SIGN
        chars[0xB2 ]=0x00B2  ;// SUPERSCRIPT TWO
        chars[0xB3 ]=0x00B3  ;// SUPERSCRIPT THREE
        chars[0xB4 ]=0x00B4  ;// ACUTE ACCENT
        chars[0xB5 ]=0x00B5  ;// MICRO SIGN
        chars[0xB6 ]=0x00B6  ;// PILCROW SIGN
        chars[0xB7 ]=0x00B7  ;// MIDDLE DOT
        chars[0xB8 ]=0x00B8  ;// CEDILLA
        chars[0xB9 ]=0x00B9  ;// SUPERSCRIPT ONE
        chars[0xBA ]=0x00BA  ;// MASCULINE ORDINAL INDICATOR
        chars[0xBB ]=0x00BB  ;// RIGHT-POINTING DOUBLE ANGLE QUOTATION MARK
        chars[0xBC ]=0x00BC  ;// VULGAR FRACTION ONE QUARTER
        chars[0xBD ]=0x00BD  ;// VULGAR FRACTION ONE HALF
        chars[0xBE ]=0x00BE  ;// VULGAR FRACTION THREE QUARTERS
        chars[0xBF ]=0x00BF  ;// INVERTED QUESTION MARK
        chars[0xC0 ]=0x00C0  ;// LATIN CAPITAL LETTER A WITH GRAVE
        chars[0xC1 ]=0x00C1  ;// LATIN CAPITAL LETTER A WITH ACUTE
        chars[0xC2 ]=0x00C2  ;// LATIN CAPITAL LETTER A WITH CIRCUMFLEX
        chars[0xC3 ]=0x0102  ;// LATIN CAPITAL LETTER A WITH BREVE
        chars[0xC4 ]=0x00C4  ;// LATIN CAPITAL LETTER A WITH DIAERESIS
        chars[0xC5 ]=0x00C5  ;// LATIN CAPITAL LETTER A WITH RING ABOVE
        chars[0xC6 ]=0x00C6  ;// LATIN CAPITAL LETTER AE
        chars[0xC7 ]=0x00C7  ;// LATIN CAPITAL LETTER C WITH CEDILLA
        chars[0xC8 ]=0x00C8  ;// LATIN CAPITAL LETTER E WITH GRAVE
        chars[0xC9 ]=0x00C9  ;// LATIN CAPITAL LETTER E WITH ACUTE
        chars[0xCA ]=0x00CA  ;// LATIN CAPITAL LETTER E WITH CIRCUMFLEX
        chars[0xCB ]=0x00CB  ;// LATIN CAPITAL LETTER E WITH DIAERESIS
        chars[0xCC ]=0x0300  ;// COMBINING GRAVE ACCENT
        chars[0xCD ]=0x00CD  ;// LATIN CAPITAL LETTER I WITH ACUTE
        chars[0xCE ]=0x00CE  ;// LATIN CAPITAL LETTER I WITH CIRCUMFLEX
        chars[0xCF ]=0x00CF  ;// LATIN CAPITAL LETTER I WITH DIAERESIS
        chars[0xD0 ]=0x0110  ;// LATIN CAPITAL LETTER D WITH STROKE
        chars[0xD1 ]=0x00D1  ;// LATIN CAPITAL LETTER N WITH TILDE
        chars[0xD2 ]=0x0309  ;// COMBINING HOOK ABOVE
        chars[0xD3 ]=0x00D3  ;// LATIN CAPITAL LETTER O WITH ACUTE
        chars[0xD4 ]=0x00D4  ;// LATIN CAPITAL LETTER O WITH CIRCUMFLEX
        chars[0xD5 ]=0x01A0  ;// LATIN CAPITAL LETTER O WITH HORN
        chars[0xD6 ]=0x00D6  ;// LATIN CAPITAL LETTER O WITH DIAERESIS
        chars[0xD7 ]=0x00D7  ;// MULTIPLICATION SIGN
        chars[0xD8 ]=0x00D8  ;// LATIN CAPITAL LETTER O WITH STROKE
        chars[0xD9 ]=0x00D9  ;// LATIN CAPITAL LETTER U WITH GRAVE
        chars[0xDA ]=0x00DA  ;// LATIN CAPITAL LETTER U WITH ACUTE
        chars[0xDB ]=0x00DB  ;// LATIN CAPITAL LETTER U WITH CIRCUMFLEX
        chars[0xDC ]=0x00DC  ;// LATIN CAPITAL LETTER U WITH DIAERESIS
        chars[0xDD ]=0x01AF  ;// LATIN CAPITAL LETTER U WITH HORN
        chars[0xDE ]=0x0303  ;// COMBINING TILDE
        chars[0xDF ]=0x00DF  ;// LATIN SMALL LETTER SHARP S
        chars[0xE0 ]=0x00E0  ;// LATIN SMALL LETTER A WITH GRAVE
        chars[0xE1 ]=0x00E1  ;// LATIN SMALL LETTER A WITH ACUTE
        chars[0xE2 ]=0x00E2  ;// LATIN SMALL LETTER A WITH CIRCUMFLEX
        chars[0xE3 ]=0x0103  ;// LATIN SMALL LETTER A WITH BREVE
        chars[0xE4 ]=0x00E4  ;// LATIN SMALL LETTER A WITH DIAERESIS
        chars[0xE5 ]=0x00E5  ;// LATIN SMALL LETTER A WITH RING ABOVE
        chars[0xE6 ]=0x00E6  ;// LATIN SMALL LETTER AE
        chars[0xE7 ]=0x00E7  ;// LATIN SMALL LETTER C WITH CEDILLA
        chars[0xE8 ]=0x00E8  ;// LATIN SMALL LETTER E WITH GRAVE
        chars[0xE9 ]=0x00E9  ;// LATIN SMALL LETTER E WITH ACUTE
        chars[0xEA ]=0x00EA  ;// LATIN SMALL LETTER E WITH CIRCUMFLEX
        chars[0xEB ]=0x00EB  ;// LATIN SMALL LETTER E WITH DIAERESIS
        chars[0xEC ]=0x0301  ;// COMBINING ACUTE ACCENT
        chars[0xED ]=0x00ED  ;// LATIN SMALL LETTER I WITH ACUTE
        chars[0xEE ]=0x00EE  ;// LATIN SMALL LETTER I WITH CIRCUMFLEX
        chars[0xEF ]=0x00EF  ;// LATIN SMALL LETTER I WITH DIAERESIS
        chars[0xF0 ]=0x0111  ;// LATIN SMALL LETTER D WITH STROKE
        chars[0xF1 ]=0x00F1  ;// LATIN SMALL LETTER N WITH TILDE
        chars[0xF2 ]=0x0323  ;// COMBINING DOT BELOW
        chars[0xF3 ]=0x00F3  ;// LATIN SMALL LETTER O WITH ACUTE
        chars[0xF4 ]=0x00F4  ;// LATIN SMALL LETTER O WITH CIRCUMFLEX
        chars[0xF5 ]=0x01A1  ;// LATIN SMALL LETTER O WITH HORN
        chars[0xF6 ]=0x00F6  ;// LATIN SMALL LETTER O WITH DIAERESIS
        chars[0xF7 ]=0x00F7  ;// DIVISION SIGN
        chars[0xF8 ]=0x00F8  ;// LATIN SMALL LETTER O WITH STROKE
        chars[0xF9 ]=0x00F9  ;// LATIN SMALL LETTER U WITH GRAVE
        chars[0xFA ]=0x00FA  ;// LATIN SMALL LETTER U WITH ACUTE
        chars[0xFB ]=0x00FB  ;// LATIN SMALL LETTER U WITH CIRCUMFLEX
        chars[0xFC ]=0x00FC  ;// LATIN SMALL LETTER U WITH DIAERESIS
        chars[0xFD ]=0x01B0  ;// LATIN SMALL LETTER U WITH HORN
        chars[0xFE ]=0x20AB  ;// DONG SIGN
        chars[0xFF ]=0x00FF  ;// LATIN SMALL LETTER Y WITH DIAERESIS
    }
    public static char decode(byte b){
        return chars[b & 0xff];
    }
}

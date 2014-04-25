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
 * this class contains method to map a windows-1255 char
 * into a Unicode char
 * 
 */
public class WIN1255 {
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
        chars[0xA0 ]=0x00A0  ;// NO-BREAK SPACE
        chars[0xA1 ]=0x00A1  ;// INVERTED EXCLAMATION MARK
        chars[0xA2 ]=0x00A2  ;// CENT SIGN
        chars[0xA3 ]=0x00A3  ;// POUND SIGN
        chars[0xA4 ]=0x20AA  ;// NEW SHEQEL SIGN
        chars[0xA5 ]=0x00A5  ;// YEN SIGN
        chars[0xA6 ]=0x00A6  ;// BROKEN BAR
        chars[0xA7 ]=0x00A7  ;// SECTION SIGN
        chars[0xA8 ]=0x00A8  ;// DIAERESIS
        chars[0xA9 ]=0x00A9  ;// COPYRIGHT SIGN
        chars[0xAA ]=0x00D7  ;// MULTIPLICATION SIGN
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
        chars[0xBA ]=0x00F7  ;// DIVISION SIGN
        chars[0xBB ]=0x00BB  ;// RIGHT-POINTING DOUBLE ANGLE QUOTATION MARK
        chars[0xBC ]=0x00BC  ;// VULGAR FRACTION ONE QUARTER
        chars[0xBD ]=0x00BD  ;// VULGAR FRACTION ONE HALF
        chars[0xBE ]=0x00BE  ;// VULGAR FRACTION THREE QUARTERS
        chars[0xBF ]=0x00BF  ;// INVERTED QUESTION MARK
        chars[0xC0 ]=0x05B0  ;// HEBREW POINT SHEVA
        chars[0xC1 ]=0x05B1  ;// HEBREW POINT HATAF SEGOL
        chars[0xC2 ]=0x05B2  ;// HEBREW POINT HATAF PATAH
        chars[0xC3 ]=0x05B3  ;// HEBREW POINT HATAF QAMATS
        chars[0xC4 ]=0x05B4  ;// HEBREW POINT HIRIQ
        chars[0xC5 ]=0x05B5  ;// HEBREW POINT TSERE
        chars[0xC6 ]=0x05B6  ;// HEBREW POINT SEGOL
        chars[0xC7 ]=0x05B7  ;// HEBREW POINT PATAH
        chars[0xC8 ]=0x05B8  ;// HEBREW POINT QAMATS
        chars[0xC9 ]=0x05B9  ;// HEBREW POINT HOLAM
        chars[0xCB ]=0x05BB  ;// HEBREW POINT QUBUTS
        chars[0xCC ]=0x05BC  ;// HEBREW POINT DAGESH OR MAPIQ
        chars[0xCD ]=0x05BD  ;// HEBREW POINT METEG
        chars[0xCE ]=0x05BE  ;// HEBREW PUNCTUATION MAQAF
        chars[0xCF ]=0x05BF  ;// HEBREW POINT RAFE
        chars[0xD0 ]=0x05C0  ;// HEBREW PUNCTUATION PASEQ
        chars[0xD1 ]=0x05C1  ;// HEBREW POINT SHIN DOT
        chars[0xD2 ]=0x05C2  ;// HEBREW POINT SIN DOT
        chars[0xD3 ]=0x05C3  ;// HEBREW PUNCTUATION SOF PASUQ
        chars[0xD4 ]=0x05F0  ;// HEBREW LIGATURE YIDDISH DOUBLE VAV
        chars[0xD5 ]=0x05F1  ;// HEBREW LIGATURE YIDDISH VAV YOD
        chars[0xD6 ]=0x05F2  ;// HEBREW LIGATURE YIDDISH DOUBLE YOD
        chars[0xD7 ]=0x05F3  ;// HEBREW PUNCTUATION GERESH
        chars[0xD8 ]=0x05F4  ;// HEBREW PUNCTUATION GERSHAYIM
        chars[0xE0 ]=0x05D0  ;// HEBREW LETTER ALEF
        chars[0xE1 ]=0x05D1  ;// HEBREW LETTER BET
        chars[0xE2 ]=0x05D2  ;// HEBREW LETTER GIMEL
        chars[0xE3 ]=0x05D3  ;// HEBREW LETTER DALET
        chars[0xE4 ]=0x05D4  ;// HEBREW LETTER HE
        chars[0xE5 ]=0x05D5  ;// HEBREW LETTER VAV
        chars[0xE6 ]=0x05D6  ;// HEBREW LETTER ZAYIN
        chars[0xE7 ]=0x05D7  ;// HEBREW LETTER HET
        chars[0xE8 ]=0x05D8  ;// HEBREW LETTER TET
        chars[0xE9 ]=0x05D9  ;// HEBREW LETTER YOD
        chars[0xEA ]=0x05DA  ;// HEBREW LETTER FINAL KAF
        chars[0xEB ]=0x05DB  ;// HEBREW LETTER KAF
        chars[0xEC ]=0x05DC  ;// HEBREW LETTER LAMED
        chars[0xED ]=0x05DD  ;// HEBREW LETTER FINAL MEM
        chars[0xEE ]=0x05DE  ;// HEBREW LETTER MEM
        chars[0xEF ]=0x05DF  ;// HEBREW LETTER FINAL NUN
        chars[0xF0 ]=0x05E0  ;// HEBREW LETTER NUN
        chars[0xF1 ]=0x05E1  ;// HEBREW LETTER SAMEKH
        chars[0xF2 ]=0x05E2  ;// HEBREW LETTER AYIN
        chars[0xF3 ]=0x05E3  ;// HEBREW LETTER FINAL PE
        chars[0xF4 ]=0x05E4  ;// HEBREW LETTER PE
        chars[0xF5 ]=0x05E5  ;// HEBREW LETTER FINAL TSADI
        chars[0xF6 ]=0x05E6  ;// HEBREW LETTER TSADI
        chars[0xF7 ]=0x05E7  ;// HEBREW LETTER QOF
        chars[0xF8 ]=0x05E8  ;// HEBREW LETTER RESH
        chars[0xF9 ]=0x05E9  ;// HEBREW LETTER SHIN
        chars[0xFA ]=0x05EA  ;// HEBREW LETTER TAV
        chars[0xFD ]=0x200E  ;// LEFT-TO-RIGHT MARK
        chars[0xFE ]=0x200F  ;// RIGHT-TO-LEFT MARK

    }
    public static char decode(byte b){
        return chars[b & 0xff];
    }
}

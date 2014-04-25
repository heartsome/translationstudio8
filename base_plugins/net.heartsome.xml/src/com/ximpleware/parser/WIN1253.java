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
 * this class contains method to map a windows-1253 char
 * into a Unicode char
 * 
 */
public class WIN1253 {
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
        chars[0x89 ]=0x2030  ;// PER MILLE SIGN
        chars[0x8B ]=0x2039  ;// SINGLE LEFT-POINTING ANGLE QUOTATION MARK
        chars[0x91 ]=0x2018  ;// LEFT SINGLE QUOTATION MARK
        chars[0x92 ]=0x2019  ;// RIGHT SINGLE QUOTATION MARK
        chars[0x93 ]=0x201C  ;// LEFT DOUBLE QUOTATION MARK
        chars[0x94 ]=0x201D  ;// RIGHT DOUBLE QUOTATION MARK
        chars[0x95 ]=0x2022  ;// BULLET
        chars[0x96 ]=0x2013  ;// EN DASH
        chars[0x97 ]=0x2014  ;// EM DASH
        chars[0x99 ]=0x2122  ;// TRADE MARK SIGN
        chars[0x9B ]=0x203A  ;// SINGLE RIGHT-POINTING ANGLE QUOTATION MARK
        chars[0xA0 ]=0x00A0  ;// NO-BREAK SPACE
        chars[0xA1 ]=0x0385  ;// GREEK DIALYTIKA TONOS
        chars[0xA2 ]=0x0386  ;// GREEK CAPITAL LETTER ALPHA WITH TONOS
        chars[0xA3 ]=0x00A3  ;// POUND SIGN
        chars[0xA4 ]=0x00A4  ;// CURRENCY SIGN
        chars[0xA5 ]=0x00A5  ;// YEN SIGN
        chars[0xA6 ]=0x00A6  ;// BROKEN BAR
        chars[0xA7 ]=0x00A7  ;// SECTION SIGN
        chars[0xA8 ]=0x00A8  ;// DIAERESIS
        chars[0xA9 ]=0x00A9  ;// COPYRIGHT SIGN
        chars[0xAB ]=0x00AB  ;// LEFT-POINTING DOUBLE ANGLE QUOTATION MARK
        chars[0xAC ]=0x00AC  ;// NOT SIGN
        chars[0xAD ]=0x00AD  ;// SOFT HYPHEN
        chars[0xAE ]=0x00AE  ;// REGISTERED SIGN
        chars[0xAF ]=0x2015  ;// HORIZONTAL BAR
        chars[0xB0 ]=0x00B0  ;// DEGREE SIGN
        chars[0xB1 ]=0x00B1  ;// PLUS-MINUS SIGN
        chars[0xB2 ]=0x00B2  ;// SUPERSCRIPT TWO
        chars[0xB3 ]=0x00B3  ;// SUPERSCRIPT THREE
        chars[0xB4 ]=0x0384  ;// GREEK TONOS
        chars[0xB5 ]=0x00B5  ;// MICRO SIGN
        chars[0xB6 ]=0x00B6  ;// PILCROW SIGN
        chars[0xB7 ]=0x00B7  ;// MIDDLE DOT
        chars[0xB8 ]=0x0388  ;// GREEK CAPITAL LETTER EPSILON WITH TONOS
        chars[0xB9 ]=0x0389  ;// GREEK CAPITAL LETTER ETA WITH TONOS
        chars[0xBA ]=0x038A  ;// GREEK CAPITAL LETTER IOTA WITH TONOS
        chars[0xBB ]=0x00BB  ;// RIGHT-POINTING DOUBLE ANGLE QUOTATION MARK
        chars[0xBC ]=0x038C  ;// GREEK CAPITAL LETTER OMICRON WITH TONOS
        chars[0xBD ]=0x00BD  ;// VULGAR FRACTION ONE HALF
        chars[0xBE ]=0x038E  ;// GREEK CAPITAL LETTER UPSILON WITH TONOS
        chars[0xBF ]=0x038F  ;// GREEK CAPITAL LETTER OMEGA WITH TONOS
        chars[0xC0 ]=0x0390  ;// GREEK SMALL LETTER IOTA WITH DIALYTIKA AND TONOS
        chars[0xC1 ]=0x0391  ;// GREEK CAPITAL LETTER ALPHA
        chars[0xC2 ]=0x0392  ;// GREEK CAPITAL LETTER BETA
        chars[0xC3 ]=0x0393  ;// GREEK CAPITAL LETTER GAMMA
        chars[0xC4 ]=0x0394  ;// GREEK CAPITAL LETTER DELTA
        chars[0xC5 ]=0x0395  ;// GREEK CAPITAL LETTER EPSILON
        chars[0xC6 ]=0x0396  ;// GREEK CAPITAL LETTER ZETA
        chars[0xC7 ]=0x0397  ;// GREEK CAPITAL LETTER ETA
        chars[0xC8 ]=0x0398  ;// GREEK CAPITAL LETTER THETA
        chars[0xC9 ]=0x0399  ;// GREEK CAPITAL LETTER IOTA
        chars[0xCA ]=0x039A  ;// GREEK CAPITAL LETTER KAPPA
        chars[0xCB ]=0x039B  ;// GREEK CAPITAL LETTER LAMDA
        chars[0xCC ]=0x039C  ;// GREEK CAPITAL LETTER MU
        chars[0xCD ]=0x039D  ;// GREEK CAPITAL LETTER NU
        chars[0xCE ]=0x039E  ;// GREEK CAPITAL LETTER XI
        chars[0xCF ]=0x039F  ;// GREEK CAPITAL LETTER OMICRON
        chars[0xD0 ]=0x03A0  ;// GREEK CAPITAL LETTER PI
        chars[0xD1 ]=0x03A1  ;// GREEK CAPITAL LETTER RHO
        chars[0xD3 ]=0x03A3  ;// GREEK CAPITAL LETTER SIGMA
        chars[0xD4 ]=0x03A4  ;// GREEK CAPITAL LETTER TAU
        chars[0xD5 ]=0x03A5  ;// GREEK CAPITAL LETTER UPSILON
        chars[0xD6 ]=0x03A6  ;// GREEK CAPITAL LETTER PHI
        chars[0xD7 ]=0x03A7  ;// GREEK CAPITAL LETTER CHI
        chars[0xD8 ]=0x03A8  ;// GREEK CAPITAL LETTER PSI
        chars[0xD9 ]=0x03A9  ;// GREEK CAPITAL LETTER OMEGA
        chars[0xDA ]=0x03AA  ;// GREEK CAPITAL LETTER IOTA WITH DIALYTIKA
        chars[0xDB ]=0x03AB  ;// GREEK CAPITAL LETTER UPSILON WITH DIALYTIKA
        chars[0xDC ]=0x03AC  ;// GREEK SMALL LETTER ALPHA WITH TONOS
        chars[0xDD ]=0x03AD  ;// GREEK SMALL LETTER EPSILON WITH TONOS
        chars[0xDE ]=0x03AE  ;// GREEK SMALL LETTER ETA WITH TONOS
        chars[0xDF ]=0x03AF  ;// GREEK SMALL LETTER IOTA WITH TONOS
        chars[0xE0 ]=0x03B0  ;// GREEK SMALL LETTER UPSILON WITH DIALYTIKA AND TONOS
        chars[0xE1 ]=0x03B1  ;// GREEK SMALL LETTER ALPHA
        chars[0xE2 ]=0x03B2  ;// GREEK SMALL LETTER BETA
        chars[0xE3 ]=0x03B3  ;// GREEK SMALL LETTER GAMMA
        chars[0xE4 ]=0x03B4  ;// GREEK SMALL LETTER DELTA
        chars[0xE5 ]=0x03B5  ;// GREEK SMALL LETTER EPSILON
        chars[0xE6 ]=0x03B6  ;// GREEK SMALL LETTER ZETA
        chars[0xE7 ]=0x03B7  ;// GREEK SMALL LETTER ETA
        chars[0xE8 ]=0x03B8  ;// GREEK SMALL LETTER THETA
        chars[0xE9 ]=0x03B9  ;// GREEK SMALL LETTER IOTA
        chars[0xEA ]=0x03BA  ;// GREEK SMALL LETTER KAPPA
        chars[0xEB ]=0x03BB  ;// GREEK SMALL LETTER LAMDA
        chars[0xEC ]=0x03BC  ;// GREEK SMALL LETTER MU
        chars[0xED ]=0x03BD  ;// GREEK SMALL LETTER NU
        chars[0xEE ]=0x03BE  ;// GREEK SMALL LETTER XI
        chars[0xEF ]=0x03BF  ;// GREEK SMALL LETTER OMICRON
        chars[0xF0 ]=0x03C0  ;// GREEK SMALL LETTER PI
        chars[0xF1 ]=0x03C1  ;// GREEK SMALL LETTER RHO
        chars[0xF2 ]=0x03C2  ;// GREEK SMALL LETTER FINAL SIGMA
        chars[0xF3 ]=0x03C3  ;// GREEK SMALL LETTER SIGMA
        chars[0xF4 ]=0x03C4  ;// GREEK SMALL LETTER TAU
        chars[0xF5 ]=0x03C5  ;// GREEK SMALL LETTER UPSILON
        chars[0xF6 ]=0x03C6  ;// GREEK SMALL LETTER PHI
        chars[0xF7 ]=0x03C7  ;// GREEK SMALL LETTER CHI
        chars[0xF8 ]=0x03C8  ;// GREEK SMALL LETTER PSI
        chars[0xF9 ]=0x03C9  ;// GREEK SMALL LETTER OMEGA
        chars[0xFA ]=0x03CA  ;// GREEK SMALL LETTER IOTA WITH DIALYTIKA
        chars[0xFB ]=0x03CB  ;// GREEK SMALL LETTER UPSILON WITH DIALYTIKA
        chars[0xFC ]=0x03CC  ;// GREEK SMALL LETTER OMICRON WITH TONOS
        chars[0xFD ]=0x03CD  ;// GREEK SMALL LETTER UPSILON WITH TONOS
        chars[0xFE ]=0x03CE  ;// GREEK SMALL LETTER OMEGA WITH TONOS

    }
    public static char decode(byte b){
        return chars[b & 0xff];
    }
}

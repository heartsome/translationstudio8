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
public class ISO8859_11 {
    static final char[] chars = new char[0x100];
    static {
        for (int i=0;i<128;i++){
            chars[i] = (char)i;
        }
        for (int i=128;i<256;i++){
            chars[i]=0xfffd;
        }
        chars[0xA1	]=0x0E01	;//THAI CHARACTER KO KAI
        chars[0xA2	]=0x0E02	;//THAI CHARACTER KHO KHAI
        chars[0xA3	]=0x0E03	;//THAI CHARACTER KHO KHUAT
        chars[0xA4	]=0x0E04	;//THAI CHARACTER KHO KHWAI
        chars[0xA5	]=0x0E05	;//THAI CHARACTER KHO KHON
        chars[0xA6	]=0x0E06	;//THAI CHARACTER KHO RAKHANG
        chars[0xA7	]=0x0E07	;//THAI CHARACTER NGO NGU
        chars[0xA8	]=0x0E08	;//THAI CHARACTER CHO CHAN
        chars[0xA9	]=0x0E09	;//THAI CHARACTER CHO CHING
        chars[0xAA	]=0x0E0A	;//THAI CHARACTER CHO CHANG
        chars[0xAB	]=0x0E0B	;//THAI CHARACTER SO SO
        chars[0xAC	]=0x0E0C	;//THAI CHARACTER CHO CHOE
        chars[0xAD	]=0x0E0D	;//THAI CHARACTER YO YING
        chars[0xAE	]=0x0E0E	;//THAI CHARACTER DO CHADA
        chars[0xAF	]=0x0E0F	;//THAI CHARACTER TO PATAK
        chars[0xB0	]=0x0E10	;//THAI CHARACTER THO THAN
        chars[0xB1	]=0x0E11	;//THAI CHARACTER THO NANGMONTHO
        chars[0xB2	]=0x0E12	;//THAI CHARACTER THO PHUTHAO
        chars[0xB3	]=0x0E13	;//THAI CHARACTER NO NEN
        chars[0xB4	]=0x0E14	;//THAI CHARACTER DO DEK
        chars[0xB5	]=0x0E15	;//THAI CHARACTER TO TAO
        chars[0xB6	]=0x0E16	;//THAI CHARACTER THO THUNG
        chars[0xB7	]=0x0E17	;//THAI CHARACTER THO THAHAN
        chars[0xB8	]=0x0E18	;//THAI CHARACTER THO THONG
        chars[0xB9	]=0x0E19	;//THAI CHARACTER NO NU
        chars[0xBA	]=0x0E1A	;//THAI CHARACTER BO BAIMAI
        chars[0xBB	]=0x0E1B	;//THAI CHARACTER PO PLA
        chars[0xBC	]=0x0E1C	;//THAI CHARACTER PHO PHUNG
        chars[0xBD	]=0x0E1D	;//THAI CHARACTER FO FA
        chars[0xBE	]=0x0E1E	;//THAI CHARACTER PHO PHAN
        chars[0xBF	]=0x0E1F	;//THAI CHARACTER FO FAN
        chars[0xC0	]=0x0E20	;//THAI CHARACTER PHO SAMPHAO
        chars[0xC1	]=0x0E21	;//THAI CHARACTER MO MA
        chars[0xC2	]=0x0E22	;//THAI CHARACTER YO YAK
        chars[0xC3	]=0x0E23	;//THAI CHARACTER RO RUA
        chars[0xC4	]=0x0E24	;//THAI CHARACTER RU
        chars[0xC5	]=0x0E25	;//THAI CHARACTER LO LING
        chars[0xC6	]=0x0E26	;//THAI CHARACTER LU
        chars[0xC7	]=0x0E27	;//THAI CHARACTER WO WAEN
        chars[0xC8	]=0x0E28	;//THAI CHARACTER SO SALA
        chars[0xC9	]=0x0E29	;//THAI CHARACTER SO RUSI
        chars[0xCA	]=0x0E2A	;//THAI CHARACTER SO SUA
        chars[0xCB	]=0x0E2B	;//THAI CHARACTER HO HIP
        chars[0xCC	]=0x0E2C	;//THAI CHARACTER LO CHULA
        chars[0xCD	]=0x0E2D	;//THAI CHARACTER O ANG
        chars[0xCE	]=0x0E2E	;//THAI CHARACTER HO NOKHUK
        chars[0xCF	]=0x0E2F	;//THAI CHARACTER PAIYANNOI
        chars[0xD0	]=0x0E30	;//THAI CHARACTER SARA A
        chars[0xD1	]=0x0E31	;//THAI CHARACTER MAI HAN-AKAT
        chars[0xD2	]=0x0E32	;//THAI CHARACTER SARA AA
        chars[0xD3	]=0x0E33	;//THAI CHARACTER SARA AM
        chars[0xD4	]=0x0E34	;//THAI CHARACTER SARA I
        chars[0xD5	]=0x0E35	;//THAI CHARACTER SARA II
        chars[0xD6	]=0x0E36	;//THAI CHARACTER SARA UE
        chars[0xD7	]=0x0E37	;//THAI CHARACTER SARA UEE
        chars[0xD8	]=0x0E38	;//THAI CHARACTER SARA U
        chars[0xD9	]=0x0E39	;//THAI CHARACTER SARA UU
        chars[0xDA	]=0x0E3A	;//THAI CHARACTER PHINTHU
        chars[0xDF	]=0x0E3F	;//THAI CURRENCY SYMBOL BAHT
        chars[0xE0	]=0x0E40	;//THAI CHARACTER SARA E
        chars[0xE1	]=0x0E41	;//THAI CHARACTER SARA AE
        chars[0xE2	]=0x0E42	;//THAI CHARACTER SARA O
        chars[0xE3	]=0x0E43	;//THAI CHARACTER SARA AI MAIMUAN
        chars[0xE4	]=0x0E44	;//THAI CHARACTER SARA AI MAIMALAI
        chars[0xE5	]=0x0E45	;//THAI CHARACTER LAKKHANGYAO
        chars[0xE6	]=0x0E46	;//THAI CHARACTER MAIYAMOK
        chars[0xE7	]=0x0E47	;//THAI CHARACTER MAITAIKHU
        chars[0xE8	]=0x0E48	;//THAI CHARACTER MAI EK
        chars[0xE9	]=0x0E49	;//THAI CHARACTER MAI THO
        chars[0xEA	]=0x0E4A	;//THAI CHARACTER MAI TRI
        chars[0xEB	]=0x0E4B	;//THAI CHARACTER MAI CHATTAWA
        chars[0xEC	]=0x0E4C	;//THAI CHARACTER THANTHAKHAT
        chars[0xED	]=0x0E4D	;//THAI CHARACTER NIKHAHIT
        chars[0xEE	]=0x0E4E	;//THAI CHARACTER YAMAKKAN
        chars[0xEF	]=0x0E4F	;//THAI CHARACTER FONGMAN
        chars[0xF0	]=0x0E50	;//THAI DIGIT ZERO
        chars[0xF1	]=0x0E51	;//THAI DIGIT ONE
        chars[0xF2	]=0x0E52	;//THAI DIGIT TWO
        chars[0xF3	]=0x0E53	;//THAI DIGIT THREE
        chars[0xF4	]=0x0E54	;//THAI DIGIT FOUR
        chars[0xF5	]=0x0E55	;//THAI DIGIT FIVE
        chars[0xF6	]=0x0E56	;//THAI DIGIT SIX
        chars[0xF7	]=0x0E57	;//THAI DIGIT SEVEN
        chars[0xF8	]=0x0E58	;//THAI DIGIT EIGHT
        chars[0xF9	]=0x0E59	;//THAI DIGIT NINE
        chars[0xFA	]=0x0E5A	;//THAI CHARACTER ANGKHANKHU
        chars[0xFB	]=0x0E5B	;//THAI CHARACTER KHOMUT
    }
    
    public static char decode(byte b){
        return chars[b & 0xff];
    }
}

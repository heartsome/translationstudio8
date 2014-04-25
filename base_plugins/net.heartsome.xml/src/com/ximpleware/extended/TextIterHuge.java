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
package com.ximpleware.extended;
/**
 * This class iterates through all text nodes of an element.
 * VTDNavHuge has getText() which is inadequate for mixed content style of XML.
 * text nodes include character_data and CDATA.
 * This version is adapted to support extended VTD (256GB max file size)
 */
public class TextIterHuge {

    private int prevLocation; //previous location of text node
    protected int depth;
    protected int index; // this is index for the element

    protected VTDNavHuge vn;

    private int lcIndex;
    private int lcLower;
    private int lcUpper;
    /**
     * TextIter constructor comment.
     */
    public TextIterHuge() {
        super();
        vn = null;
        /*sel_char_data = true;
        sel_comment = true;
        sel_cdata = true;*/
    }
/**
 * Get the index vals for the text nodes in document order.
 * Creation date: (12/5/03 6:11:50 PM)
 * @return int  (-1 if no more left)
 */
public int getNext() {
    if (vn == null)
        throw new IllegalArgumentException(" VTDNav instance can't be null");
    int vtdSize = vn.vtdBuffer.size();
    switch (depth) {
        case -1: return -1;
        case 0 :
            // scan forward, if none found, jump to level 1 element and scan backward until one is found
            // if there isn't a level-one element, jump to the end of vtd buffer and scan backward
            int sp = (prevLocation != -1) ? increment(prevLocation): index + 1;
            if (vn.l1Buffer.size() != 0) {
                int temp1 = vn.l1Buffer.upper32At(0);
                int temp2 = vn.l1Buffer.upper32At(vn.l1Buffer.size() - 1);
                lcIndex = (lcIndex != -1) ? lcIndex : 0;
                while (sp < vtdSize) {
                    if (sp >= temp1 && sp < temp2) {
                        int s = vn.l1Buffer.upper32At(lcIndex);
                        if (sp == s) { // get to the next l1 element then do a rewind
                            lcIndex++;
                            sp = vn.l1Buffer.upper32At(lcIndex)-1;
                            while (vn.getTokenDepth(sp) == 0
                                && vn.getTokenType(sp) != VTDNavHuge.TOKEN_STARTING_TAG) { //probe depth in here
                                sp--;
                            }
                            sp++; // point to the first possible node  
                        }
                        if (isText(sp) == true && vn.getTokenDepth(sp)==0) {
                            prevLocation = sp;
                            return sp;
                        }
                        sp++;
                    } else if (sp < temp1) {
                        if (isText(sp) == true && vn.getTokenDepth(sp)==0) {
                            prevLocation = sp;
                            return sp;
                        }
                        sp++;
                    } else {
                        if (sp == temp2) { // get to the end of the document and do a rewind
                            sp = vn.vtdBuffer.size() - 1;
                            while (vn.getTokenDepth(sp) <= 0) {
                                sp--;
                            }
                            sp++;
                            //continue;
                        }
                        if (sp>=vtdSize)
                            return -1;
                        else if (isText(sp) == true && vn.getTokenDepth(sp)==0) {
                            prevLocation = sp;
                            return sp;
                        }
                        else if (vn.getTokenDepth(sp)>1) {
                            break;
                        }
                        sp++;
                    }
                }
                //prevLocation = vtdSize-1;
                return -1;
                // found nothing
            } else {
                // no child element for root, just scan right forward
                while (sp < vtdSize) {
                    if (isText(sp) == true && vn.getTokenDepth(sp)==0) {
                        prevLocation = sp;
                        return sp;
                    }
                    sp++;
                }
                return -1;
            }
        case 1 :
            if (prevLocation != -1) {
                sp = increment(prevLocation) ;
            } else {
                // fetch lclower and lcupper
                lcLower = vn.l1Buffer.lower32At(vn.l1index);
                if (lcLower != -1) {
                    lcUpper = vn.l2Buffer.size() - 1;
                    int size = vn.l1Buffer.size();
                    for (int i = vn.l1index + 1; i < size ; i++) {
                        int temp = vn.l1Buffer.lower32At(i);
                        if (temp != 0xffffffff) {
                            lcUpper = temp - 1;
                            break;
                        }
                    }
                }
                sp = index + 1;
            } // check for l2lower and l2upper

            if (lcLower != -1) { // have at least one child element
                int temp1 = vn.l2Buffer.upper32At(lcLower);
                int temp2 = vn.l2Buffer.upper32At(lcUpper);
                lcIndex = (lcIndex != -1) ? lcIndex : lcLower;
                while (sp < vtdSize) {
                    int s = vn.l2Buffer.upper32At(lcIndex);
                    if (sp >= temp1 && sp < temp2) {
                        if (sp == s) {
                            lcIndex++;
                            sp = vn.l2Buffer.upper32At(lcIndex) - 1;
                            while (vn.getTokenDepth(sp) == 1) {
                                sp--;
                            }
                            sp++;
                            //continue; 
                        }
                        if (isText(sp) == true && vn.getTokenDepth(sp)==1 ) {
                            prevLocation = sp;
                            return sp;
                        }
                        sp++;
                    } else if (sp < temp1) {
                        if (isText(sp) == true) {
                            prevLocation = sp;
                            return sp;
                        }
                        sp++;
                    } else {
                        //if (sp == temp2) { // last child element
                        //} else
                        
                        if (isText(sp) == true && vn.getTokenDepth(sp) == 1){
                        	//System.out.println("depth ->"+vn.getTokenDepth(sp));
                            prevLocation = sp;
                            return sp;
                        } else if ((vn.getTokenType(sp)==VTDNavHuge.TOKEN_STARTING_TAG
                                && vn.getTokenDepth(sp) < 2 ) || vn.getTokenDepth(sp)<1) {
                            break;
                        }
                        sp++;
                    }                    
                }
                //prevLocation = vtdSize-1;
                return -1;
            } else { // no child element
                if (sp>=vtdSize) return -1;
                int d = vn.getTokenDepth(sp);
                int type = vn.getTokenType(sp);
                while (sp < vtdSize
                    && d >= 1
                    && !(d == 1 && type == VTDNavHuge.TOKEN_STARTING_TAG)) {
                    if (isText(sp) == true) {
                        prevLocation = sp;
                        return sp;
                    }
                    sp++;
                    d = vn.getTokenDepth(sp);
                    type = vn.getTokenType(sp);
                    
                }
                //prevLocation = vtdSize-1;
                return -1;
            }

        case 2 :
            if (prevLocation != -1) {
                sp = increment(prevLocation);
            } else {
                // fetch lclower and lcupper
                lcLower = vn.l2Buffer.lower32At(vn.l2index);
                if (lcLower != -1) {
                    lcUpper = vn.l3Buffer.size() - 1;
                    int size = vn.l2Buffer.size();
                    for (int i = vn.l2index + 1; i < size ; i++) {
                        int temp = vn.l2Buffer.lower32At(i);
                        if (temp != 0xffffffff) {
                            lcUpper = temp - 1;
                            break;
                        }
                    }
                }
                sp = index + 1;
            } // check for l3lower and l3upper

            if (lcLower != -1) { // at least one child element
                int temp1 = vn.l3Buffer.intAt(lcLower);
                int temp2 = vn.l3Buffer.intAt(lcUpper);
                lcIndex = (lcIndex != -1) ? lcIndex : lcLower;
                while (sp < vtdSize) {
                    int s = vn.l3Buffer.intAt(lcIndex);
                    //int s = vn.l2Buffer.upper32At(lcIndex);
                    if (sp >= temp1 && sp < temp2) {
                        if (sp == s) {
                            lcIndex++;
                            sp = vn.l3Buffer.intAt(lcIndex) - 1;
                            while (vn.getTokenDepth(sp) == 2) {
                                sp--;
                            }
                            sp++;
                            //continue;
                        }
                        if (isText(sp) == true && vn.getTokenDepth(sp)==2) {
                            prevLocation = sp;
                            return sp;
                        }
                        sp++;
                    } else if (sp < temp1) {
                        if (isText(sp) == true && vn.getTokenDepth(sp)==2) {
                            prevLocation = sp;
                            return sp;
                        }
                        sp++;
                    } else {
                        //if (sp == temp2) { // last child element
                        //} else                 
                        if ( isText(sp) == true && vn.getTokenDepth(sp) == 2) {
                            prevLocation = sp;
                            return sp;
                        } else if ((vn.getTokenType(sp)==VTDNavHuge.TOKEN_STARTING_TAG
                                && vn.getTokenDepth(sp) < 3 ) || vn.getTokenDepth(sp)<2) {
                            break;
                        }
                        sp++;
                    }
                }
                //prevLocation = vtdSize-1;
                return -1;
            } else { // no child elements
                if (sp>=vtdSize) return -1;
                int d = vn.getTokenDepth(sp);
                int type = vn.getTokenType(sp);
                while (sp < vtdSize
                    && d >= 2
                    && !(d == 2 && type == VTDNavHuge.TOKEN_STARTING_TAG)) {
                    // the last condition indicates the start of the next sibling element
                    if (isText(sp) == true && vn.getTokenDepth(sp)==2) {
                        prevLocation = sp;
                        return sp;
                    }
                    sp++;
                    d = vn.getTokenDepth(sp);
                    type = vn.getTokenType(sp);
                    
                }
                //prevLocation = vtdSize-1;
                return -1;
            }

        default :
            //int curDepth = vn.context[0];
            sp = (prevLocation != -1) ? increment(prevLocation): index + 1;
            if (sp>=vtdSize) return -1;
            int d = vn.getTokenDepth(sp);
            int type = vn.getTokenType(sp);
            while (d >= depth
                && !(d == depth && type == VTDNavHuge.TOKEN_STARTING_TAG)) {
                if (isText(sp) == true && d == depth) {
                    prevLocation = sp;
                    return sp;
                }
                sp++;
                if(sp >= vtdSize)
                   return -1;

                d = vn.getTokenDepth(sp);
                type = vn.getTokenType(sp);                
            }
    }
    //prevLocation = vtdSize-1;
    return -1;
}
/**
 * Test whether a give token type is a TEXT.
 * Creation date: (12/11/03 3:46:10 PM)
 * @return boolean
 * @param type int
 */
final private boolean isText(int index) {
    int type = vn.getTokenType(index);
    return (type == VTDNavHuge.TOKEN_CHARACTER_DATA
    //|| type == vn.TOKEN_COMMENT
    || type == VTDNavHuge.TOKEN_CDATA_VAL);
}
    /**
     * Obtain the current navigation position and element info from VTDNav.
     * So one can instantiate it once and use it for many different elements
     * Creation date: (12/5/03 6:20:44 PM)
     * @param v com.ximpleware.VTDNav
     */
    public void touch(VTDNavHuge v) {
        if (v == null)
            throw new IllegalArgumentException(" VTDNav instance can't be null");

        depth = v.context[0];
        if (depth == -1)
            index = 0;
        else
            index = (depth != 0) ? v.context[depth] : v.rootIndex;

        vn = v;
        prevLocation = -1;
        lcIndex = -1;
        lcUpper = -1;
        lcLower = -1;
    }
   
    private int increment(int sp){
      
      int type = vn.getTokenType(sp);
      int vtdSize = vn.vtdBuffer.size();
      int i=sp+1;
      while(i<vtdSize && 
      		depth == vn.getTokenDepth(i) && 
			type == vn.getTokenType(i)&&
			(vn.getTokenOffset(i-1)+ (int)((vn.vtdBuffer.longAt(i-1) & VTDNavHuge.MASK_TOKEN_FULL_LEN)>>32) 
			        == vn.getTokenOffset(i))
			        ){          
             i++;
      }
      return i;
    }
}

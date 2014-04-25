/* 
 * Copyright (C) 2002-2010 XimpleWare, info@ximpleware.com
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
/**
 * ElementFragement represents a chunk of bytes in an XML document that
 * corresponds to an element, additionally this class includes an integer
 * array corresponds to all the name spaces declarations applicable to the 
 * element fragement
 * So l is the length/offset of the fragment
 * vn is the VTDNav instance to which the fragment belongs
 * fib is the integer array containing the name space declarations applicable
 * to the element fragment 
 */
package com.ximpleware;
import java.io.*;
public class ElementFragment {
    VTDNav vn;
    long l;
    FastIntBuffer fib;
     int stLen; // length of starting tag 
    static byte[] ws; // a few byte patterns for white space and '='
    static{
        ws = new byte[5];
        ws[0]=0;
        ws[1]=(byte)' ';
        ws[2]=0;
        ws[3]='=';
        ws[4]=0;
    }
    public ElementFragment(VTDNav vn1, long l1, FastIntBuffer fib1, int len){
        vn = vn1;
        l = l1;
        fib = fib1;
        stLen = len;        
    }   
    /**
     * Return a byte array with namespace compensation
     * witht the orginal encoding format
     * @return
     *
     */
   public final byte[] toBytes(){
        byte[] ba = new byte[getSize()];
        
        int os = (int)l;
        int len = (int)(l>>32);
        int os1 = 0;
        byte[] xml = vn.getXML().getBytes();
        if (stLen==0){
            System.arraycopy(xml,os,ba,0,len);
            return ba;
        }
        int enc = vn.getEncoding();
        int temp = 0;
                
        switch(enc){
        	case VTDNav.FORMAT_UTF_16BE: 
        	case VTDNav.FORMAT_UTF_16LE: temp= (stLen+1)<<1; break;
        	default:
          	    temp = stLen+1;
        }            
        System.arraycopy(xml,os,ba,0,temp);
        
        //namespace compensation
        os1 += temp;
        
        int tos =0,tlen=0;
        for (int i = 0; i < fib.size(); i++) {
            System.out.println("i ==>"+fib.intAt(i));
            switch (enc) {
            case VTDNav.FORMAT_UTF_16BE:
                //write a 0 and ws
                System.arraycopy(ws,0,ba,os1,2);
            	os1 += 2;
                tos = vn.getTokenOffset(fib.intAt(i))<<1;
            	tlen= (vn.getTokenLength(fib.intAt(i)) & 0xffff)<<1;
            	System.arraycopy(xml,tos,ba,os1,tlen);
            	os1 +=tlen;
            	// write a 0 and =
                System.arraycopy(ws,2,ba,os1,2);
                os1	+= 2;
                tos = (vn.getTokenOffset(fib.intAt(i)+1)-1)<<1;
            	tlen= ((vn.getTokenLength(fib.intAt(i)+1) & 0xffff)+2)<<1;
            	System.arraycopy(xml,tos,ba,os1,tlen);
            	os1 +=tlen;
                break;
            case VTDNav.FORMAT_UTF_16LE:
                // write a ws and 0
                System.arraycopy(ws,1,ba,os1,2);
        		os1 += 2;
        		tos = vn.getTokenOffset(fib.intAt(i))<<1;
            	tlen= (vn.getTokenLength(fib.intAt(i)) & 0xffff)<<1;
            	System.arraycopy(xml,tos,ba,os1,tlen);
            	os1 +=tlen;
            	// 	 write a = and 0
                System.arraycopy(ws,3,ba,os1,2);
                os1	+= 2;
                tos = (vn.getTokenOffset(fib.intAt(i)+1)-1)<<1;
            	tlen= ((vn.getTokenLength(fib.intAt(i)+1) & 0xffff)+2)<<1;
            	System.arraycopy(xml,tos,ba,os1,tlen);
            	os1 +=tlen;
                break;
            default:
                // write a ws
                System.arraycopy(ws, 1, ba, os1, 1);
                os1 ++;
                tos = vn.getTokenOffset(fib.intAt(i));
                tlen = (vn.getTokenLength(fib.intAt(i)) & 0xffff);
                System.arraycopy(xml, tos, ba, os1, tlen);
                os1 +=tlen;
                // 	 write a = 
                System.arraycopy(ws, 3, ba, os1, 1);
                
                os1 ++;
                tos = vn.getTokenOffset(fib.intAt(i) + 1) - 1 ;
                tlen = (vn.getTokenLength(fib.intAt(i) + 1) & 0xffff) + 2;
                System.arraycopy(xml, tos, ba, os1, tlen);
                os1 +=tlen;
            }
        }
        //System.out.println(new String(ba));
        switch(enc){
    		case VTDNav.FORMAT_UTF_16BE: 
    		case VTDNav.FORMAT_UTF_16LE: 
    		    temp= (stLen+1)<<1; break;
    		default:
    		    temp = stLen+1;
        }  
        System.arraycopy(xml, os + temp, ba, os1, len - temp);
        //System.out.println(new String(ba));
        return ba;
    }
   
    /**
     * getSize gets the fragment with ns compensation
     * @return
     *
     */
    public final int getSize(){
        //int encoding = vn.encoding;
        int len = (int) (l >> 32);
        if (stLen != 0)
            for (int i = 0; i < fib.size(); i++) {
                int k = fib.intAt(i);
                len += (vn.getTokenLength(k) & 0xffff)
                        + vn.getTokenLength(k + 1) + 4;
            }
        return len;
    }
    
    /**
     * 
     * 
     *
     */
    public final int writeToByteArray(byte[] ba, int offset){
        return 0;
    }
    
    /**
     * Write ns compensated fragments (bytes in original encoding format) to outputstream
     * @param ost
     * @throws IOException
     *
     */
    public final void writeToOutputStream(OutputStream ost) throws IOException{
        int os = (int)l;
        int len = (int)(l>>32);
        //int os1 = 0;
        byte[] xml = vn.getXML().getBytes();
        if (stLen==0){
            //System.arraycopy(xml,os,ba,0,len);
            ost.write(xml,os,len);
            //return ba;
        }
        int enc = vn.getEncoding();
        int temp = 0;
                
        switch(enc){
        	case VTDNav.FORMAT_UTF_16BE: 
        	case VTDNav.FORMAT_UTF_16LE: temp= (stLen+1)<<1; break;
        	default:
          	    temp = stLen+1;
        }
        ost.write(xml,os,temp);
        //System.arraycopy(xml,os,ba,0,temp);
        
        //namespace compensation
        //os1 += temp;
        
        int tos =0,tlen=0;
        for (int i = 0; i < fib.size(); i++) {
            System.out.println("i ==>"+fib.intAt(i));
            switch (enc) {
            case VTDNav.FORMAT_UTF_16BE:
                //write a 0 and ws
                //System.arraycopy(ws,0,ba,os1,2);
            	ost.write(ws,0,2);
            	//os1 += 2;
                tos = vn.getTokenOffset(fib.intAt(i))<<1;
            	tlen= (vn.getTokenLength(fib.intAt(i)) & 0xffff)<<1;
            	//System.arraycopy(xml,tos,ba,os1,tlen);
            	ost.write(xml,tos,tlen);
            	//os1 +=tlen;
            	// write a 0 and =
                //System.arraycopy(ws,2,ba,os1,2);
                ost.write(ws,2,2);
                //os1	+= 2;
                tos = (vn.getTokenOffset(fib.intAt(i)+1)-1)<<1;
            	tlen= ((vn.getTokenLength(fib.intAt(i)+1) & 0xffff)+2)<<1;
            	//System.arraycopy(xml,tos,ba,os1,tlen);
            	ost.write(xml,tos,tlen);
            	//os1 +=tlen;
                break;
            case VTDNav.FORMAT_UTF_16LE:
                // write a ws and 0
                //System.arraycopy(ws,1,ba,os1,2);
            	ost.write(ws,1,2);
        		//os1 += 2;
        		tos = vn.getTokenOffset(fib.intAt(i))<<1;
            	tlen= (vn.getTokenLength(fib.intAt(i)) & 0xffff)<<1;
            	//System.arraycopy(xml,tos,ba,os1,tlen);
            	ost.write(xml,tos,tlen);
            	//os1 +=tlen;
            	// 	 write a = and 0
                //System.arraycopy(ws,3,ba,os1,2);
                ost.write(ws,3,2);
                //os1	+= 2;
                tos = (vn.getTokenOffset(fib.intAt(i)+1)-1)<<1;
            	tlen= ((vn.getTokenLength(fib.intAt(i)+1) & 0xffff)+2)<<1;
            	//System.arraycopy(xml,tos,ba,os1,tlen);
            	ost.write(xml,tos,tlen);
            	//os1 +=tlen;
                break;
            default:
                // write a ws
                //System.arraycopy(ws, 1, ba, os1, 1);
            	ost.write(ws,1,1);
                //os1 ++;
                tos = vn.getTokenOffset(fib.intAt(i));
                tlen = (vn.getTokenLength(fib.intAt(i)) & 0xffff);
                //System.arraycopy(xml, tos, ba, os1, tlen);
                ost.write(xml,tos,tlen);
                //os1 +=tlen;
                // 	 write a = 
                //System.arraycopy(ws, 3, ba, os1, 1);
                ost.write(ws,3,1);
                //os1 ++;
                tos = vn.getTokenOffset(fib.intAt(i) + 1) - 1 ;
                tlen = (vn.getTokenLength(fib.intAt(i) + 1) & 0xffff) + 2;
                //System.arraycopy(xml, tos, ba, os1, tlen);
                ost.write(xml,tos,tlen);
                //os1 +=tlen;
            }
        }
        //System.out.println(new String(ba));
        switch(enc){
    		case VTDNav.FORMAT_UTF_16BE: 
    		case VTDNav.FORMAT_UTF_16LE: 
    		    temp= (stLen+1)<<1; break;
    		default:
    		    temp = stLen+1;
        }  
        //System.arraycopy(xml, os + temp, ba, os1, len - temp);
        ost.write(xml,os+temp,len-temp);
    }
    /**
     * 
     * @return
     *
     */
    public final long getOffsetLen(){
        return l;
    }
}

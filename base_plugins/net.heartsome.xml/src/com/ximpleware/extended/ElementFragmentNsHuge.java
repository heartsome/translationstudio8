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
import java.io.*;

import com.ximpleware.extended.FastIntBuffer;
//import com.ximpleware.extended.TranscodeException;
import com.ximpleware.extended.VTDNavHuge;
//import com.ximpleware.extended.transcode.Transcoder;
public class ElementFragmentNsHuge {
    VTDNavHuge vn;
    long[] l;
    FastIntBuffer fib;
     long stLen; // length of starting tag 
     int UTF_8_Size;
     int ASCII_Size;
     int ISO_8859_1_Size;
     int UTF_16_BE_Size;
     int UTF_16_LE_Size;
    static byte[] ws; // a few byte patterns for white space and '='
    static{
        ws = new byte[5];
        ws[0]=0;
        ws[1]=(byte)' ';
        ws[2]=0;
        ws[3]='=';
        ws[4]=0;
    }
    /**
     * 
     * @param vn1
     * @param l1
     * @param fib1
     * @param len
     */
    protected ElementFragmentNsHuge(VTDNavHuge vn1, long l1[], FastIntBuffer fib1, long len){
        UTF_8_Size = -1;
        ASCII_Size = -1;
        ISO_8859_1_Size = -1;
        UTF_16_BE_Size = -1;
        UTF_16_LE_Size = -1;
        vn = vn1;
        l = l1;
        fib = fib1;
        stLen = len;        
    }   
    
    /**
     * Transcode the ElementFragmentNS object to a byte array according to the
     * destination encoding format
     * @param encoding
     * @return
     *
     */
/*    public final byte[] toBytes(int dest_encoding) throws TranscodeException{
        if (dest_encoding == vn.encoding){
            return toBytes();
        }
        // find out how long the output bytes are
        byte[] ba = new byte[getSize(dest_encoding)];
        
        int os = (int)l;
        int len = (int)(l>>32);
        int os1 = 0;
        byte[] xml = vn.getXML().getBytes();
        if (stLen==0){
            Transcoder.transcodeAndFill(xml,ba,os,len,vn.encoding,dest_encoding);
            return ba;
        }
        int enc = vn.getEncoding();
        int temp = 0;
        int outPosition = 0;
        // transcode and fill the bytes
        switch (enc) {
        case VTDNav.FORMAT_UTF_16BE:
        case VTDNav.FORMAT_UTF_16LE:
            temp = (stLen + 1) << 1;
            break;
        default:
            temp = stLen + 1;
        }
        // transcode starting length
        outPosition = Transcoder.transcodeAndFill2(outPosition, 
                xml, ba, os, temp, vn.encoding, dest_encoding);
        
        //System.arraycopy(xml, os, ba, 0, temp);
        
        //namespace compensation
        os1 += temp;

        int tos = 0, tlen = 0;
        for (int i = 0; i < fib.size(); i++) {
            //System.out.println("i ==>"+fib.intAt(i));
            switch (enc) {
            case VTDNav.FORMAT_UTF_16BE:
                //write a 0 and ws
                //System.arraycopy(ws, 0, ba, os1, 2);
            	outPosition = Transcoder.transcodeAndFill2(outPosition,
            	        ws,ba,0,2, vn.encoding, dest_encoding);
                os1 += 2;
                tos = vn.getTokenOffset(fib.intAt(i)) << 1;
                tlen = (vn.getTokenLength(fib.intAt(i)) & 0xffff) << 1;
                //System.arraycopy(xml, tos, ba, os1, tlen);
                outPosition = Transcoder.transcodeAndFill2(outPosition,
            	        xml,ba,tos,tlen, vn.encoding, dest_encoding);
                os1 += tlen;
                // write a 0 and =
                //System.arraycopy(ws, 2, ba, os1, 2);
                outPosition = Transcoder.transcodeAndFill2(outPosition,
            	        ws,ba,2,2, vn.encoding, dest_encoding);
                os1 += 2;
                tos = (vn.getTokenOffset(fib.intAt(i) + 1) - 1) << 1;
                tlen = ((vn.getTokenLength(fib.intAt(i) + 1) & 0xffff) + 2) << 1;
                //System.arraycopy(xml, tos, ba, os1, tlen);
                outPosition = Transcoder.transcodeAndFill2(outPosition,
            	        xml, ba, tos, tlen, vn.encoding, dest_encoding);
                os1 += tlen;
                break;
            case VTDNav.FORMAT_UTF_16LE:
                // write a ws and 0
                //System.arraycopy(ws, 1, ba, os1, 2);
                outPosition = Transcoder.transcodeAndFill2(outPosition,
            	        ws, ba, 1, 2, vn.encoding, dest_encoding);
                os1 += 2;
                tos = vn.getTokenOffset(fib.intAt(i)) << 1;
                tlen = (vn.getTokenLength(fib.intAt(i)) & 0xffff) << 1;
                //System.arraycopy(xml, tos, ba, os1, tlen);
                outPosition = Transcoder.transcodeAndFill2(outPosition,
            	        xml, ba, tos, tlen, vn.encoding, dest_encoding);
                os1 += tlen;
                // 	 write a = and 0
                //System.arraycopy(ws, 3, ba, os1, 2);
                outPosition = Transcoder.transcodeAndFill2(outPosition,
            	        ws,ba,3,2, vn.encoding, dest_encoding);
                os1 += 2;
                tos = (vn.getTokenOffset(fib.intAt(i) + 1) - 1) << 1;
                tlen = ((vn.getTokenLength(fib.intAt(i) + 1) & 0xffff) + 2) << 1;
               // System.arraycopy(xml, tos, ba, os1, tlen);
                outPosition = Transcoder.transcodeAndFill2(outPosition,
            	        xml, ba, tos, tlen, vn.encoding, dest_encoding);
                os1 += tlen;
                break;
            default:
                // write a ws
                //System.arraycopy(ws, 1, ba, os1, 1);
            	outPosition = Transcoder.transcodeAndFill2(outPosition,
        	        ws,ba,1,1, vn.encoding, dest_encoding);
                os1++;
                tos = vn.getTokenOffset(fib.intAt(i));
                tlen = (vn.getTokenLength(fib.intAt(i)) & 0xffff);
                //System.arraycopy(xml, tos, ba, os1, tlen);
                outPosition = Transcoder.transcodeAndFill2(outPosition,
            	        xml, ba, tos, tlen, vn.encoding, dest_encoding);
                os1 += tlen;
                // 	 write a =
                //System.arraycopy(ws, 3, ba, os1, 1);
                outPosition = Transcoder.transcodeAndFill2(outPosition,
            	        ws,ba,3,1, vn.encoding, dest_encoding);
                os1++;
                tos = vn.getTokenOffset(fib.intAt(i) + 1) - 1;
                tlen = (vn.getTokenLength(fib.intAt(i) + 1) & 0xffff) + 2;
                //System.arraycopy(xml, tos, ba, os1, tlen);
                outPosition = Transcoder.transcodeAndFill2(outPosition,
            	        xml, ba, tos, tlen, vn.encoding, dest_encoding);
                os1 += tlen;
            }
        }
        //System.out.println(new String(ba));
        switch (enc) {
        case VTDNav.FORMAT_UTF_16BE:
        case VTDNav.FORMAT_UTF_16LE:
            temp = (stLen + 1) << 1;
            break;
        default:
            temp = stLen + 1;
        }
        //System.arraycopy(xml, os + temp, ba, os1, len - temp);
        outPosition = Transcoder.transcodeAndFill2(outPosition,
    	        xml, ba, os + temp, len - temp, vn.encoding, dest_encoding);
        //System.out.println(new String(ba));
        return ba;
    }*/
    
 
    /**
     * getSize gets the byte length of ns compensated fragment in its source
     * encoding format
     * @return the byte length of ns compensated fragment in its source encoding format
     *
     */
    public final long getSize(){
        //int encoding = vn.encoding;
        long len = l[1];
        if (stLen != 0)
            for (int i = 0; i < fib.size(); i++) {
                int k = fib.intAt(i);
                if (vn.encoding < VTDNavHuge.FORMAT_UTF_16BE ){
                    len += (vn.getTokenLength(k) & 0xffff)
                        + vn.getTokenLength(k + 1) + 4;
                }else{
                    len += ((vn.getTokenLength(k) & 0xffff)
                    	+ vn.getTokenLength(k + 1) + 4)<<1;
                }
            }
        return len;
    }
    
    
   /**
    * This method returns the size of the transcoded byte representation of
    * the ns compensated element fragment
    * @param dest_encoding
    * @return
    * @throws TranscodeException
    *
    */
    /*public final int getSize(int dest_encoding) throws TranscodeException{
        //int len = (int) (l >> 32);
        
        //if (stLen != 0)
        //    for (int i = 0; i < fib.size(); i++) {
        //        int k = fib.intAt(i);
        //        len += (vn.getTokenLength(k) & 0xffff)
        //                + vn.getTokenLength(k + 1) + 4;
        //    }        
        if (vn.encoding == dest_encoding)
            return getSize();
        //int src_encoding= vn.encoding;
        byte[] ba = vn.getXML().getBytes();
        int len = Transcoder.getOutLength(ba, (int)l, (int)(l>>32), vn.encoding, dest_encoding );
        
        if (stLen != 0)
            for (int i = 0; i < fib.size(); i++) {
                int k = fib.intAt(i);
                if (vn.encoding < VTDNav.FORMAT_UTF_16BE ){
                    
                    len += Transcoder.getOutLength(ba, vn.getTokenOffset(k),
                            (vn.getTokenLength(k) & 0xffff), vn.encoding,
                            dest_encoding)
                            + Transcoder.getOutLength(ba, vn.getTokenOffset(k+1),
                                    vn.getTokenLength(k + 1), vn.encoding,
                                    dest_encoding) + ((dest_encoding<VTDNav.FORMAT_UTF_16BE)?4:8);
                }else {
                    len += Transcoder.getOutLength(ba, vn.getTokenOffset(k)<<1,
                            (vn.getTokenLength(k) & 0xffff)<<1, vn.encoding,
                            dest_encoding)
                            + Transcoder.getOutLength(ba, vn.getTokenOffset(k+1)<<1,
                                    vn.getTokenLength(k + 1)<<1, vn.encoding,
                                    dest_encoding) + ((dest_encoding<VTDNav.FORMAT_UTF_16BE)?4:8);                
                }
            }
        return len;    
    }*/
    
    
    /**
     * Write ns compensated fragments (bytes in original encoding format) to outputstream
     * @param ost
     * @throws IOException
     *
     */
    public final void writeToFileOutputStream(FileOutputStream ost) throws IOException{
        long os = l[0];
        long len = l[1];
        //int os1 = 0;
        IByteBuffer xml = vn.getXML();
        if (stLen==0){
            //System.arraycopy(xml,os,ba,0,len);
            //ost.write(xml,os,len);
            xml.writeToFileOutputStream(ost, os, len);
            return;
            //return ba;
        }
        int enc = vn.getEncoding();
        long temp = 0;
                
        switch(enc){
        	case VTDNavHuge.FORMAT_UTF_16BE: 
        	case VTDNavHuge.FORMAT_UTF_16LE: temp= (stLen+1)<<1; break;
        	default:
          	    temp = stLen+1;
        }
        //ost.write(xml,os,temp);
        xml.writeToFileOutputStream(ost, os, temp);
        //System.arraycopy(xml,os,ba,0,temp);
        
        //namespace compensation
        //os1 += temp;
        
        long tos =0,tlen=0;
        for (int i = 0; i < fib.size(); i++) {
            //System.out.println("i ==>"+fib.intAt(i));
            switch (enc) {
            case VTDNavHuge.FORMAT_UTF_16BE:
                //write a 0 and ws
                //System.arraycopy(ws,0,ba,os1,2);
            	ost.write(ws,0,2);
            	//os1 += 2;
                tos = vn.getTokenOffset(fib.intAt(i))<<1;
            	tlen= (vn.getTokenLength(fib.intAt(i)) & 0xffff)<<1;
            	//System.arraycopy(xml,tos,ba,os1,tlen);
            	//ost.write(xml,tos,tlen);
            	xml.writeToFileOutputStream(ost, tos, tlen);
            	//os1 +=tlen;
            	// write a 0 and =
                //System.arraycopy(ws,2,ba,os1,2);
                ost.write(ws,2,2);
                //os1	+= 2;
                tos = (vn.getTokenOffset(fib.intAt(i)+1)-1)<<1;
            	tlen= ((vn.getTokenLength(fib.intAt(i)+1) & 0xffff)+2)<<1;
            	//System.arraycopy(xml,tos,ba,os1,tlen);
            	//ost.write(xml,tos,tlen);
            	xml.writeToFileOutputStream(ost, tos, tlen);
            	//os1 +=tlen;
                break;
            case VTDNavHuge.FORMAT_UTF_16LE:
                // write a ws and 0
                //System.arraycopy(ws,1,ba,os1,2);
            	ost.write(ws,1,2);
        		//os1 += 2;
        		tos = vn.getTokenOffset(fib.intAt(i))<<1;
            	tlen= (vn.getTokenLength(fib.intAt(i)) & 0xffff)<<1;
            	//System.arraycopy(xml,tos,ba,os1,tlen);
            	//ost.write(xml,tos,tlen);
            	xml.writeToFileOutputStream(ost, tos, tlen);
            	//os1 +=tlen;
            	// 	 write a = and 0
                //System.arraycopy(ws,3,ba,os1,2);
                ost.write(ws,3,2);
                //os1	+= 2;
                tos = (vn.getTokenOffset(fib.intAt(i)+1)-1)<<1;
            	tlen= ((vn.getTokenLength(fib.intAt(i)+1) & 0xffff)+2)<<1;
            	//System.arraycopy(xml,tos,ba,os1,tlen);
            	//ost.write(xml,tos,tlen);
            	xml.writeToFileOutputStream(ost, tos, tlen);
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
                //ost.write(xml,tos,tlen);
                xml.writeToFileOutputStream(ost, tos, tlen);
                //os1 +=tlen;
                // 	 write a = 
                //System.arraycopy(ws, 3, ba, os1, 1);
                ost.write(ws,3,1);
                //os1 ++;
                tos = vn.getTokenOffset(fib.intAt(i) + 1) - 1 ;
                tlen = (vn.getTokenLength(fib.intAt(i) + 1) & 0xffff) + 2;
                //System.arraycopy(xml, tos, ba, os1, tlen);
                //ost.write(xml,tos,tlen);
                xml.writeToFileOutputStream(ost, tos, tlen);
                //os1 +=tlen;
            }
        }
        //System.out.println(new String(ba));
        switch(enc){
    		case VTDNavHuge.FORMAT_UTF_16BE: 
    		case VTDNavHuge.FORMAT_UTF_16LE: 
    		    temp= (stLen+1)<<1; break;
    		default:
    		    temp = stLen+1;
        }  
        //System.arraycopy(xml, os + temp, ba, os1, len - temp);
        //ost.write(xml,os+temp,len-temp);
        xml.writeToFileOutputStream(ost,os+temp,len-temp);
    }
    
    /**
     * Write the transcode byte representation of an ns-compensated 
     * element fragment to the output stream
     * @param ost
     * @param dest_encoding
     * @throws IOException
     * @throws TranscodeException
     *
     */
   /* public final void writeToOutputStream(OutputStream ost,int dest_encoding) 
    throws IOException, TranscodeException{
        if (vn.encoding == dest_encoding){
            writeToOutputStream(ost);
            return;
        }
        int os = (int)l;
        int len = (int)(l>>32);
        //int os1 = 0;
        byte[] xml = vn.getXML().getBytes();
        if (stLen==0){
            //System.arraycopy(xml,os,ba,0,len);
            //ost.write(xml,os,len);
            Transcoder.transcodeAndWrite(xml,ost, os,len, vn.encoding, dest_encoding );
            return;
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
        //ost.write(xml,os,temp);
        Transcoder.transcodeAndWrite(xml,ost, os, temp, enc,dest_encoding );
        //System.arraycopy(xml,os,ba,0,temp);
        
        //namespace compensation
        //os1 += temp;
        
        int tos =0,tlen=0;
        for (int i = 0; i < fib.size(); i++) {
            //System.out.println("i ==>"+fib.intAt(i));
            switch (enc) {
            case VTDNav.FORMAT_UTF_16BE:
                //write a 0 and ws
                //System.arraycopy(ws,0,ba,os1,2);
            	//ost.write(ws,0,2);
            	Transcoder.transcodeAndWrite(ws,ost,0,2, enc,dest_encoding );
            	//os1 += 2;
                tos = vn.getTokenOffset(fib.intAt(i))<<1;
            	tlen= (vn.getTokenLength(fib.intAt(i)) & 0xffff)<<1;
            	//System.arraycopy(xml,tos,ba,os1,tlen);
            	//ost.write(xml,tos,tlen);
            	Transcoder.transcodeAndWrite(xml,ost,tos,tlen, enc,dest_encoding );
            	//os1 +=tlen;
            	// write a 0 and =
                //System.arraycopy(ws,2,ba,os1,2);
                //ost.write(ws,2,2);
                Transcoder.transcodeAndWrite(ws,ost,2,2, enc,dest_encoding );
                //os1	+= 2;
                tos = (vn.getTokenOffset(fib.intAt(i)+1)-1)<<1;
            	tlen= ((vn.getTokenLength(fib.intAt(i)+1) & 0xffff)+2)<<1;
            	//System.arraycopy(xml,tos,ba,os1,tlen);
            	//ost.write(xml,tos,tlen);
            	Transcoder.transcodeAndWrite(xml,ost,tos,tlen, enc,dest_encoding );
            	//os1 +=tlen;
                break;
            case VTDNav.FORMAT_UTF_16LE:
                // write a ws and 0
                //System.arraycopy(ws,1,ba,os1,2);
            	//ost.write(ws,1,2);
            	Transcoder.transcodeAndWrite(ws,ost,1,2, enc,dest_encoding );
        		//os1 += 2;
        		tos = vn.getTokenOffset(fib.intAt(i))<<1;
            	tlen= (vn.getTokenLength(fib.intAt(i)) & 0xffff)<<1;
            	//System.arraycopy(xml,tos,ba,os1,tlen);
            	//ost.write(xml,tos,tlen);
            	Transcoder.transcodeAndWrite(xml,ost,tos,tlen, enc,dest_encoding );
            	//os1 +=tlen;
            	// 	 write a = and 0
                //System.arraycopy(ws,3,ba,os1,2);
                //ost.write(ws,3,2);
                Transcoder.transcodeAndWrite(ws,ost,3,2, enc,dest_encoding );
                //os1	+= 2;
                tos = (vn.getTokenOffset(fib.intAt(i)+1)-1)<<1;
            	tlen= ((vn.getTokenLength(fib.intAt(i)+1) & 0xffff)+2)<<1;
            	//System.arraycopy(xml,tos,ba,os1,tlen);
            	//ost.write(xml,tos,tlen);
            	Transcoder.transcodeAndWrite(xml,ost,tos,tlen, enc,dest_encoding );
            	//os1 +=tlen;
                break;
            default:
                // write a ws
                //System.arraycopy(ws, 1, ba, os1, 1);
            	//ost.write(ws,1,1);
                Transcoder.transcodeAndWrite(ws,ost,1,1,enc, dest_encoding);
                //os1 ++;
                tos = vn.getTokenOffset(fib.intAt(i));
                tlen = (vn.getTokenLength(fib.intAt(i)) & 0xffff);
                //System.arraycopy(xml, tos, ba, os1, tlen);
                //ost.write(xml,tos,tlen);
                Transcoder.transcodeAndWrite(xml,ost,tos,tlen, enc,dest_encoding );
                //os1 +=tlen;
                // 	 write a = 
                //System.arraycopy(ws, 3, ba, os1, 1);
                //ost.write(ws,3,1);
                Transcoder.transcodeAndWrite(ws,ost,3,1, enc,dest_encoding );
                //os1 ++;
                tos = vn.getTokenOffset(fib.intAt(i) + 1) - 1 ;
                tlen = (vn.getTokenLength(fib.intAt(i) + 1) & 0xffff) + 2;
                //System.arraycopy(xml, tos, ba, os1, tlen);
                //ost.write(xml,tos,tlen);
                Transcoder.transcodeAndWrite(xml,ost,tos,tlen, enc,dest_encoding );
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
        //ost.write(xml,os+temp,len-temp);
        Transcoder.transcodeAndWrite(xml,ost,os+temp,len-temp, enc,dest_encoding );
    }*/
    /**
     * Get the long encoding the len and offset of uncompensated element fragment
     * @return
     *
     */
    public final long[] getOffsetLen(){
        return l;
    }
}

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
package com.ximpleware;
import java.io.*;
import java.nio.*;
/**
 * IndexWrite adjusts the offset so that the XML segment
 * contains only the XML document
 * IndexRead, if from byte array, will move adjust the offset 
 * 
 */
class IndexHandler {
    public static final int OFFSET_ADJUSTMENT =32;
    
    /**
     * Write VTD+XML index to OutputStream
     * @param version
     * @param encodingType
     * @param ns
     * @param byteOrder
     * @param nestDepth
     * @param LCLevel
     * @param rootIndex
     * @param xmlDoc
     * @param docOffset
     * @param docLen
     * @param vtdBuffer
     * @param l1Buffer
     * @param l2Buffer
     * @param l3Buffer
     * @param os
     * @throws IndexWriteException
     * @throws IOException
     *
     */
    public static void writeIndex_L3(byte version,
            int encodingType,
            boolean ns,
            boolean byteOrder, // true is big endien
            int nestDepth,
            int LCLevel,
            int rootIndex,
            byte[] xmlDoc,
            int docOffset,
            int docLen,
            FastLongBuffer vtdBuffer,
            FastLongBuffer l1Buffer,
            FastLongBuffer l2Buffer,
            FastIntBuffer l3Buffer,
            OutputStream os
            ) throws IndexWriteException,
            IOException{
        if ( xmlDoc == null
                || docLen <=0
                || vtdBuffer == null 
                 // impossible to occur
                || l1Buffer == null // setDoc not called
                || l2Buffer == null
                || l3Buffer == null
                || LCLevel !=3
                ){
            throw new IndexWriteException("Invalid VTD index ");
        }
        if (vtdBuffer.size()==0)
            throw new IndexWriteException("VTDBuffer can't be zero length");
        
        int i;
        DataOutputStream dos = new DataOutputStream(os);
        // first 4 bytes
        byte[] ba = new byte[4];
        ba[0] = (byte)version;  // version # is 2 
        ba[1] = (byte)encodingType;
        ba[2] = (byte)(ns? 0xe0 : 0xa0); // big endien
        ba[3] = (byte)nestDepth;
        dos.write(ba);
        // second 4 bytes
        ba[0] = 0;
        ba[1] = 4;
        ba[2] = (byte) ((rootIndex & 0xff00)>> 8 );
        ba[3] = (byte) (rootIndex & 0xff);
        dos.write(ba);
        // 2 reserved 64-bit words set to zero
        ba[1]= ba[2] = ba[3] = 0;
        dos.write(ba);
        dos.write(ba);
        dos.write(ba);
        dos.write(ba);
        // write XML doc in bytes
        dos.writeLong(docLen);
        dos.write(xmlDoc,docOffset,docLen);
        // zero padding to make it integer multiple of 64 bits
        if ((docLen & 0x07) !=0 ){
            int t = (((docLen>>3)+1)<<3) - docLen;
            for (;t>0;t--)
                dos.write(0);
        }
        // write VTD offset adjusted if the start offset is not zero
        dos.writeLong(vtdBuffer.size);
        if (docOffset == 0)
            for (i = 0; i < vtdBuffer.size; i++) {
                dos.writeLong(vtdBuffer.longAt(i));
            }
        else
            for (i = 0; i < vtdBuffer.size; i++) {
                dos.writeLong(adjust(vtdBuffer.longAt(i), 
                        -docOffset));
            }
          
        // write L1 
        dos.writeLong(l1Buffer.size);
        for(i=0;i< l1Buffer.size;i++){
            dos.writeLong(l1Buffer.longAt(i));
        }
        // write L2
        dos.writeLong(l2Buffer.size);
        for(i=0;i< l2Buffer.size;i++){
            dos.writeLong(l2Buffer.longAt(i));
        }
        // write L3
        dos.writeLong(l3Buffer.size);
        for(i=0;i< l3Buffer.size;i++){
            dos.writeInt(l3Buffer.intAt(i));
        }
        // pad zero if # of l3 entry is odd
        if ( (l3Buffer.size & 1) !=0)
            dos.writeInt(0);
        dos.close();
    }
    
    /**
     * Write VTD+XML index to OutputStream
     * @param version
     * @param encodingType
     * @param ns
     * @param byteOrder
     * @param nestDepth
     * @param LCLevel
     * @param rootIndex
     * @param xmlDoc
     * @param docOffset
     * @param docLen
     * @param vtdBuffer
     * @param l1Buffer
     * @param l2Buffer
     * @param l3Buffer
     * @param os
     * @throws IndexWriteException
     * @throws IOException
     *
     */
    public static void writeIndex_L5(byte version,
            int encodingType,
            boolean ns,
            boolean byteOrder, // true is big endien
            int nestDepth,
            int LCLevel,  //has to be 5
            int rootIndex,
            byte[] xmlDoc,
            int docOffset,
            int docLen,
            FastLongBuffer vtdBuffer,
            FastLongBuffer l1Buffer,
            FastLongBuffer l2Buffer,
            FastLongBuffer l3Buffer,
            FastLongBuffer l4Buffer,
            FastIntBuffer l5Buffer,
            OutputStream os
            ) throws IndexWriteException,
            IOException{
        if ( xmlDoc == null
                || docLen <=0
                || vtdBuffer == null 
                 // impossible to occur
                || l1Buffer == null // setDoc not called
                || l2Buffer == null
                || l3Buffer == null
                || l4Buffer == null
                || l5Buffer == null
                || LCLevel !=5
                ){
            throw new IndexWriteException("Invalid VTD index ");
        }
        if (vtdBuffer.size==0)
            throw new IndexWriteException("VTDBuffer can't be zero length");
        
        int i;
        DataOutputStream dos = new DataOutputStream(os);
        // first 4 bytes
        byte[] ba = new byte[4];
        ba[0] = (byte)version;  // version # is 2 
        ba[1] = (byte)encodingType;
        ba[2] = (byte)(ns? 0xe0 : 0xa0); // big endien
        ba[3] = (byte)nestDepth;
        dos.write(ba);
        // second 4 bytes
        ba[0] = 0;
        ba[1] = 6;
        ba[2] = (byte) ((rootIndex & 0xff00)>> 8 );
        ba[3] = (byte) (rootIndex & 0xff);
        dos.write(ba);
        // 2 reserved 64-bit words set to zero
        ba[1]= ba[2] = ba[3] = 0;
        dos.write(ba);
        dos.write(ba);
        dos.write(ba);
        dos.write(ba);
        // write XML doc in bytes
        dos.writeLong(docLen);
        dos.write(xmlDoc,docOffset,docLen);
        // zero padding to make it integer multiple of 64 bits
        if ((docLen & 0x07) !=0 ){
            int t = (((docLen>>3)+1)<<3) - docLen;
            for (;t>0;t--)
                dos.write(0);
        }
        // write VTD offset adjusted if the start offset is not zero
        dos.writeLong(vtdBuffer.size);
        if (docOffset == 0)
            for (i = 0; i < vtdBuffer.size; i++) {
                dos.writeLong(vtdBuffer.longAt(i));
            }
        else
            for (i = 0; i < vtdBuffer.size; i++) {
                dos.writeLong(adjust(vtdBuffer.longAt(i), 
                        -docOffset));
            }
          
        // write L1 
        dos.writeLong(l1Buffer.size);
        for(i=0;i< l1Buffer.size;i++){
            dos.writeLong(l1Buffer.longAt(i));
        }
        // write L2
        dos.writeLong(l2Buffer.size);
        for(i=0;i< l2Buffer.size;i++){
            dos.writeLong(l2Buffer.longAt(i));
        }
        // write L3
        dos.writeLong(l3Buffer.size);
        for(i=0;i< l3Buffer.size;i++){
            dos.writeLong(l3Buffer.longAt(i));
        }
        // write L4
        dos.writeLong(l4Buffer.size);
        for(i=0;i< l4Buffer.size;i++){
            dos.writeLong(l4Buffer.longAt(i));
        }
        // write L5
        dos.writeLong(l5Buffer.size);
        for(i=0;i< l5Buffer.size;i++){
            dos.writeInt(l5Buffer.intAt(i));
        }
        // pad zero if # of l3 entry is odd
        if ( (l5Buffer.size & 1) !=0)
            dos.writeInt(0);
        dos.close();
    }
    
    /**
     * Write VTD and Location cache into output stream (which is separate from XML)
     * Notice that VTD index assumes that XML bytes starts at the beginning
     *  
     * @param version
     * @param encodingType
     * @param ns
     * @param byteOrder
     * @param nestDepth
     * @param LCLevel
     * @param rootIndex
     * @param xmlDoc
     * @param docOffset
     * @param docLen
     * @param vtdBuffer
     * @param l1Buffer
     * @param l2Buffer
     * @param l3Buffer
     * @param os
     * @throws IndexWriteException
     * @throws IOException
     *
     */
    public static void writeSeparateIndex_L3(byte version,
            int encodingType,
            boolean ns,
            boolean byteOrder, // true is big endien
            int nestDepth,
            int LCLevel,
            int rootIndex,
            //byte[] xmlDoc,
            int docOffset,
            int docLen,
            FastLongBuffer vtdBuffer,
            FastLongBuffer l1Buffer,
            FastLongBuffer l2Buffer,
            FastIntBuffer l3Buffer,
            OutputStream os
            ) throws IndexWriteException,
            IOException{
        if ( //xmlDoc == null
                
                docLen <=0
                || vtdBuffer == null 
                 // impossible to occur
                || l1Buffer == null // setDoc not called
                || l2Buffer == null
                || l3Buffer == null
                || LCLevel !=3
                ){
            throw new IndexWriteException("Invalid VTD index ");
        }
        if (vtdBuffer.size==0)
            throw new IndexWriteException("VTDBuffer can't be zero length");
        
        int i;
        DataOutputStream dos = new DataOutputStream(os);
        // first 4 bytes
        byte[] ba = new byte[4];
        ba[0] = (byte)version;  // version # is 2 
        ba[1] = (byte)encodingType;
        ba[2] = (byte)(ns? 0xe0 : 0xa0); // big endien
        ba[3] = (byte)nestDepth;
        dos.write(ba);
        // second 4 bytes
        ba[0] = 0;
        ba[1] = 4;
        ba[2] = (byte) ((rootIndex & 0xff00)>> 8 );
        ba[3] = (byte) (rootIndex & 0xff);
        dos.write(ba);
        // 2 reserved 64-bit words set to zero
        ba[1]= ba[2] = ba[3] = 0;
        dos.write(ba);
        dos.write(ba);
        dos.write(ba);
        dos.write(ba);
        // write XML doc in bytes
        dos.writeLong(docLen);
        // 16 bytes reserved bytes
        dos.write(ba);
        dos.write(ba);
        dos.write(ba);
        dos.write(ba);
        //dos.write(xmlDoc,docOffset,docLen);
        // zero padding to make it integer multiple of 64 bits
        //if ((docLen & 0x07) !=0 ){
        //    int t = (((docLen>>3)+1)<<3) - docLen;
        //    for (;t>0;t--)
        //        dos.write(0);
        //}
        // write VTD
        dos.writeLong(vtdBuffer.size);
        if (docOffset == 0)
            for (i = 0; i < vtdBuffer.size; i++) {
                dos.writeLong(vtdBuffer.longAt(i));
            }
        else
            for (i = 0; i < vtdBuffer.size; i++) {
                dos.writeLong(adjust(vtdBuffer.longAt(i), 
                        -docOffset));
            }
          
        // write L1 
        dos.writeLong(l1Buffer.size);
        for(i=0;i< l1Buffer.size;i++){
            dos.writeLong(l1Buffer.longAt(i));
        }
        // write L2
        dos.writeLong(l2Buffer.size);
        for(i=0;i< l2Buffer.size();i++){
            dos.writeLong(l2Buffer.longAt(i));
        }
        // write L3
        dos.writeLong(l3Buffer.size);
        for(i=0;i< l3Buffer.size;i++){
            dos.writeInt(l3Buffer.intAt(i));
        }
        // pad zero if # of l3 entry is odd
        if ( (l3Buffer.size & 1) !=0)
            dos.writeInt(0);
        dos.close();
    }
    
    /**
     * Write VTD and Location cache into output stream (which is separate from XML)
     * Notice that VTD index assumes that XML bytes starts at the beginning
     *  
     * @param version
     * @param encodingType
     * @param ns
     * @param byteOrder
     * @param nestDepth
     * @param LCLevel
     * @param rootIndex
     * @param xmlDoc
     * @param docOffset
     * @param docLen
     * @param vtdBuffer
     * @param l1Buffer
     * @param l2Buffer
     * @param l3Buffer
     * @param os
     * @throws IndexWriteException
     * @throws IOException
     *
     */
    public static void writeSeparateIndex_L5(byte version,
            int encodingType,
            boolean ns,
            boolean byteOrder, // true is big endien
            int nestDepth,
            int LCLevel,
            int rootIndex,
            //byte[] xmlDoc,
            int docOffset,
            int docLen,
            FastLongBuffer vtdBuffer,
            FastLongBuffer l1Buffer,
            FastLongBuffer l2Buffer,
            FastLongBuffer l3Buffer,
            FastLongBuffer l4Buffer,
            FastIntBuffer l5Buffer,
            OutputStream os
            ) throws IndexWriteException,
            IOException{
        if ( //xmlDoc == null
                
                docLen <=0
                || vtdBuffer == null 
                 // impossible to occur
                || l1Buffer == null // setDoc not called
                || l2Buffer == null
                || l3Buffer == null
                || l4Buffer == null
                || l5Buffer == null
                || LCLevel !=5
                ){
            throw new IndexWriteException("Invalid VTD index ");
        }
        if (vtdBuffer.size==0)
            throw new IndexWriteException("VTDBuffer can't be zero length");
        
        int i;
        DataOutputStream dos = new DataOutputStream(os);
        // first 4 bytes
        byte[] ba = new byte[4];
        ba[0] = (byte)version;  // version # is 2 
        ba[1] = (byte)encodingType;
        ba[2] = (byte)(ns? 0xe0 : 0xa0); // big endien
        ba[3] = (byte)nestDepth;
        dos.write(ba);
        // second 4 bytes
        ba[0] = 0;
        ba[1] = 6;
        ba[2] = (byte) ((rootIndex & 0xff00)>> 8 );
        ba[3] = (byte) (rootIndex & 0xff);
        dos.write(ba);
        // 2 reserved 64-bit words set to zero
        ba[1]= ba[2] = ba[3] = 0;
        dos.write(ba);
        dos.write(ba);
        dos.write(ba);
        dos.write(ba);
        // write XML doc in bytes
        dos.writeLong(docLen);
        // 16 bytes reserved bytes
        dos.write(ba);
        dos.write(ba);
        dos.write(ba);
        dos.write(ba);
        //dos.write(xmlDoc,docOffset,docLen);
        // zero padding to make it integer multiple of 64 bits
        //if ((docLen & 0x07) !=0 ){
        //    int t = (((docLen>>3)+1)<<3) - docLen;
        //    for (;t>0;t--)
        //        dos.write(0);
        //}
        // write VTD
        dos.writeLong(vtdBuffer.size);
        if (docOffset == 0)
            for (i = 0; i < vtdBuffer.size; i++) {
                dos.writeLong(vtdBuffer.longAt(i));
            }
        else
            for (i = 0; i < vtdBuffer.size; i++) {
                dos.writeLong(adjust(vtdBuffer.longAt(i), 
                        -docOffset));
            }
          
        // write L1 
        dos.writeLong(l1Buffer.size);
        for(i=0;i< l1Buffer.size;i++){
            dos.writeLong(l1Buffer.longAt(i));
        }
        // write L2
        dos.writeLong(l2Buffer.size);
        for(i=0;i< l2Buffer.size();i++){
            dos.writeLong(l2Buffer.longAt(i));
        }
        // write L3
        dos.writeLong(l3Buffer.size);
        for(i=0;i< l3Buffer.size;i++){
            dos.writeLong(l3Buffer.longAt(i));
        }
        
        // write L4
        dos.writeLong(l4Buffer.size);
        for(i=0;i< l4Buffer.size;i++){
            dos.writeLong(l4Buffer.longAt(i));
        }
        // write L5
        dos.writeLong(l5Buffer.size);
        for(i=0;i< l5Buffer.size;i++){
            dos.writeInt(l5Buffer.intAt(i));
        }
        // pad zero if # of l3 entry is odd
        if ( (l5Buffer.size & 1) !=0)
            dos.writeInt(0);
        dos.close();
    }
    
    /**
     * The assumption for this function is that when VTD+XML index
     * is loaded into memory (ba), the first 32 bytes are not XML bytes
     * @param ba
     * @param vg
     * @throws IndexReadException
     *
     */
    public static void readIndex(byte[] ba, VTDGen vg)
    throws IndexReadException{
        if (ba == null || vg == null)
            throw new IllegalArgumentException("Invalid argument(s) for readIndex()");

        ByteBuffer bb = ByteBuffer.wrap(ba);
        byte b= bb.get(); // first byte
        if (b!=1) throw new IndexReadException("Invalid version number for readIndex()");
        // no check on version number for now
        // second byte
        vg.encoding = bb.get();
        int adj = OFFSET_ADJUSTMENT;
        if (vg.encoding >= VTDGen.FORMAT_UTF_16BE){
            adj = OFFSET_ADJUSTMENT>>1;
        }
        int intLongSwitch;
        //int ns;
        int endian;
        // third byte
        b= bb.get();
        if ((b&0x80)!=0)
           intLongSwitch = 1; //use ints
        else 
           intLongSwitch = 0;
        if ((b & 0x40)!=0)
            vg.ns = true;
        else
            vg.ns = false;
        if ((b & 0x20) !=0)
            endian = 1;
        else 
            endian = 0;
        if ((b & 0x1f) != 0)
            throw new IndexReadException("Last 5 bits of the third byte should be zero");
        // fourth byte
        vg.VTDDepth =  bb.get();
        
        // 5th and 6th byte
        int LCLevel = (((int)bb.get())<<8) | bb.get();
        if (LCLevel != 4 &&  LCLevel !=6)
            throw new IndexReadException("LC levels must be at least 3");
        // 7th and 8th byte
        if (LCLevel ==4)
        	vg.shallowDepth = true;
        else
        	vg.shallowDepth = false;
        // 7th and 8th byte
        vg.rootIndex = (((int)bb.get())<<8) | bb.get();
        
        // skip a long
        bb.getLong();
        bb.getLong();
        int size = 0;
        // read XML size
        if (endian == 1)
           size = (int)bb.getLong();
        else
           size = (int)reverseLong(bb.getLong());
        // read XML bytes
        //byte[] XMLDoc = new byte[size];
        //bb.get(XMLDoc);
        int t=0;
        if ((size & 0x7)!= 0){
            t = (((size>>3)+1)<<3) - size;            
        }
        
        vg.setDoc_BR(ba,32,size);
        
        bb = ByteBuffer.wrap(ba,32+size+t,ba.length-32-size-t);
        
        if (endian ==1){
            // read vtd records
            int vtdSize = (int)bb.getLong();
            while(vtdSize>0){
                vg.VTDBuffer.append(adjust(bb.getLong(),adj));
                vtdSize--;
            }
            // read L1 LC records
            int l1Size = (int)bb.getLong();
                     
            while(l1Size > 0){
                long l = bb.getLong();
               // System.out.println(" l-==> "+Long.toHexString(l));
                vg.l1Buffer.append(l);
                l1Size--;
            }
            //System.out.println("++++++++++ ");
            // read L2 LC records
            int l2Size = (int)bb.getLong();
            while(l2Size > 0){
                vg.l2Buffer.append(bb.getLong());
                l2Size--;
            }
            //System.out.println("++++++++++ ");   
            // read L3 LC records
            int l3Size = (int)bb.getLong();
			if (vg.shallowDepth) {
				if (intLongSwitch == 1) { // l3 uses ints
					while (l3Size > 0) {
						vg.l3Buffer.append(bb.getInt());
						l3Size--;
					}
				} else {
					while (l3Size > 0) {
						vg.l3Buffer.append((int) (bb.getLong() >> 32));
						l3Size--;
					}
				}
			}else{
				while (l3Size > 0) {
					vg._l3Buffer.append(bb.getLong());
					l3Size--;
				}
				int l4Size = (int) bb.getLong();
				while (l4Size > 0) {
					vg._l4Buffer.append(bb.getLong());
					l4Size--;
				}

				int l5Size = (int) bb.getLong();
				if (intLongSwitch == 1) { // l5 uses ints
					while (l5Size > 0) {
						vg._l5Buffer.append(bb.getInt());
						l5Size--;
					}
				} else {
					while (l5Size > 0) {
						vg._l5Buffer.append((int) (bb.getLong() >> 32));
						l5Size--;
					}
				}
			}
            
        } else {
            // read vtd records
            int vtdSize = (int)reverseLong(bb.getLong());
            while(vtdSize>0){
                vg.VTDBuffer.append(adjust(reverseLong(bb.getLong()),adj));
                vtdSize--;
            }
            // read L1 LC records
            //System.out.println(" ++++++++++ ");
            int l1Size = (int)reverseLong(bb.getLong());
            while(l1Size > 0){
                long l = reverseLong(bb.getLong());
                vg.l1Buffer.append(l);
                l1Size--;
            }
            //System.out.println(" ++++++++++ ");
            // read L2 LC records
            int l2Size = (int)reverseLong(bb.getLong());
            while(l2Size > 0){
                long l = reverseLong(bb.getLong());
                //System.out.println(" l--=->"+Long.toHexString(l));
                vg.l2Buffer.append(l);
                l2Size--;
            }
            //System.out.println(" ++++++++++ ");
            // read L3 LC records
            int l3Size = (int)reverseLong(bb.getLong());
			if (vg.shallowDepth) {
				if (intLongSwitch == 1) { // l3 uses ints
					while (l3Size > 0) {
						vg.l3Buffer.append(reverseInt(bb.getInt()));
						l3Size--;
					}
				} else {
					while (l3Size > 0) {
						vg.l3Buffer
								.append(reverseInt((int) (bb.getLong() >> 32)));
						l3Size--;
					}
				}
            }else{
				while (l3Size > 0) {
					vg._l3Buffer.append(reverseLong(bb.getLong()));
					l3Size--;
				}
				int l4Size = (int) reverseLong(bb.getLong());
				while (l4Size > 0) {
					vg._l4Buffer.append(reverseLong(bb.getLong()));
					l4Size--;
				}

				int l5Size = (int) reverseLong(bb.getLong());
				if (intLongSwitch == 1) { // l5 uses ints
					while (l5Size > 0) {
						vg._l5Buffer.append(reverseInt(bb.getInt()));
						l5Size--;
					}
				} else {
					while (l5Size > 0) {
						vg._l5Buffer.append((reverseInt((int) (bb.getLong() >> 32))));
						l5Size--;
					}
				}
            	
            }
        }
    }
    
    /**
     * Load VTD+XML index from an inputStream
     * @param is
     * @param vg
     * @throws IndexReadException
     * @throws IOException
     *
     */
    public static void readIndex(InputStream is, VTDGen vg) 
    throws IndexReadException,IOException{
        if (is == null || vg == null)
            throw new IndexReadException("Invalid argument(s) for readIndex()");
        DataInputStream dis = new DataInputStream(is);
        byte b= dis.readByte(); // first byte
        if (b!=1) throw new IndexReadException("Invalid version number for readIndex()");
        // no check on version number for now
        // second byte
        vg.encoding = dis.readByte();
        int intLongSwitch;
        //int ns;
        int endian;
        // third byte
        b= dis.readByte();
        if ((b&0x80)!=0)
           intLongSwitch = 1; //use ints
        else 
           intLongSwitch = 0;
        if ((b & 0x40)!=0)
            vg.ns = true;
        else
            vg.ns = false;
        if ((b & 0x20) !=0)
            endian = 1;
        else 
            endian = 0;
        if ((b & 0x1f) != 0)
            throw new IndexReadException("Last 5 bits of the third byte should be zero");
        // fourth byte
        vg.VTDDepth =  dis.readByte();
        
        // 5th and 6th byte
        int LCLevel = (((int)dis.readByte())<<8) | dis.readByte();
        if (LCLevel != 4 &&  LCLevel != 6)
            throw new IndexReadException("LC levels must be at least 3");
        // 7th and 8th byte
        if (LCLevel ==4)
        	vg.shallowDepth = true;
        else
        	vg.shallowDepth = false;
        vg.rootIndex = (((int)dis.readByte())<<8) | dis.readByte();
        
        // skip a long
        dis.readLong();
        dis.readLong();
        int size = 0;
        // read XML size
        if (endian == 1)
           size = (int)dis.readLong();
        else
           size = (int)reverseLong(dis.readLong());
        // read XML bytes
        byte[] XMLDoc = new byte[size];
        dis.read(XMLDoc);
        if ((size & 0x7)!= 0){
            int t = (((size>>3)+1)<<3) - size;
            while(t>0){
                dis.readByte();
                t--;
            }
        }
        
        vg.setDoc(XMLDoc);
        
        if (endian ==1){
            // read vtd records
            int vtdSize = (int)dis.readLong();
            while(vtdSize>0){
                vg.VTDBuffer.append(dis.readLong());
                vtdSize--;
            }
            // read L1 LC records
            int l1Size = (int)dis.readLong();
                     
            while(l1Size > 0){
                long l = dis.readLong();
               // System.out.println(" l-==> "+Long.toHexString(l));
                vg.l1Buffer.append(l);
                l1Size--;
            }
            //System.out.println("++++++++++ ");
            // read L2 LC records
            int l2Size = (int)dis.readLong();
            while(l2Size > 0){
                vg.l2Buffer.append(dis.readLong());
                l2Size--;
            }
            //System.out.println("++++++++++ ");   
            // read L3 LC records
            int l3Size = (int) dis.readLong();
			if (vg.shallowDepth) {				
				if (intLongSwitch == 1) { // l3 uses ints
					while (l3Size > 0) {
						vg.l3Buffer.append(dis.readInt());
						l3Size--;
					}
				} else {
					while (l3Size > 0) {
						vg.l3Buffer.append((int) (dis.readLong() >> 32));
						l3Size--;
					}
				}
			} else {
				while (l3Size > 0) {
					vg._l3Buffer.append(dis.readLong());
					l3Size--;
				}
				
				int l4Size = (int)dis.readLong();
	            while(l4Size > 0){
	                vg._l4Buffer.append(dis.readLong());
	                l4Size--;
	            }
	            
	            int l5Size = (int)dis.readLong();
	            if (intLongSwitch == 1) { // l5 uses ints
	            	while(l5Size > 0){
	            		vg._l5Buffer.append(dis.readInt());
	            		l5Size--;
	            	}
	            }else {
	            	while (l5Size > 0) {
						vg._l5Buffer.append((int) (dis.readLong() >> 32));
						l5Size--;
					}
	            }
			}
        } else {
            // read vtd records
            int vtdSize = (int)reverseLong(dis.readLong());
            while(vtdSize>0){
                vg.VTDBuffer.append(reverseLong(dis.readLong()));
                vtdSize--;
            }
            // read L1 LC records
            //System.out.println(" ++++++++++ ");
            int l1Size = (int)reverseLong(dis.readLong());
            while(l1Size > 0){
                long l = reverseLong(dis.readLong());
                vg.l1Buffer.append(l);
                l1Size--;
            }
            //System.out.println(" ++++++++++ ");
            // read L2 LC records
            int l2Size = (int)reverseLong(dis.readLong());
            while(l2Size > 0){
                long l = reverseLong(dis.readLong());
                //System.out.println(" l--=->"+Long.toHexString(l));
                vg.l2Buffer.append(l);
                l2Size--;
            }
            //System.out.println(" ++++++++++ ");
            // read L3 LC records
            int l3Size = (int)reverseLong(dis.readLong());
			if (vg.shallowDepth) {
				if (intLongSwitch == 1) { // l3 uses ints
					while (l3Size > 0) {
						vg.l3Buffer.append(reverseInt(dis.readInt()));
						l3Size--;
					}
				} else {
					while (l3Size > 0) {
						vg.l3Buffer
								.append(reverseInt((int) (dis.readLong() >> 32)));
						l3Size--;
					}
				}
			}else{
				while (l3Size > 0) {
					vg._l3Buffer.append(reverseLong(dis.readLong()));
					l3Size--;
				}
				int l4Size = (int)reverseLong(dis.readLong());
				while(l4Size > 0){
	                long l = reverseLong(dis.readLong());
	                vg._l4Buffer.append(l);
	                l4Size--;
	            }
				int l5Size = (int)reverseLong(dis.readLong());
				if (intLongSwitch == 1) { // l5 uses ints
	            	while(l5Size > 0){
	            		vg._l5Buffer.append(reverseInt(dis.readInt()));
	            		l5Size--;
	            	}
	            }else {
	            	while (l5Size > 0) {
						vg._l5Buffer.append(reverseInt((int) (dis.readLong() >> 32)));
						l5Size--;
					}
	            }
			}
        }
    }
    
/**
 * read in XML and index file  separately 
 * @param index
 * @param XMLBytes
 * @param XMLSize
 * @param vg
 * @throws IndexReadException
 * @throws IOException
 *
 */
    public static void readSeparateIndex(InputStream index, InputStream XMLBytes, int XMLSize, VTDGen vg) 
    throws IndexReadException,IOException{
        if (index == null || vg == null || XMLBytes == null)
            throw new IndexReadException("Invalid argument(s) for readSeparateIndex()");
        DataInputStream dis = new DataInputStream(index);
        byte b= dis.readByte(); // first byte
        if (b!=2) throw new IndexReadException("Invalid version number for readIndex()");
        // no check on version number for now
        // second byte
        vg.encoding = dis.readByte();
        int intLongSwitch;
        //int ns;
        int endian;
        // third byte
        b= dis.readByte();
        if ((b&0x80)!=0)
           intLongSwitch = 1; //use ints
        else 
           intLongSwitch = 0;
        if ((b & 0x40)!=0)
            vg.ns = true;
        else
            vg.ns = false;
        if ((b & 0x20) !=0)
            endian = 1;
        else 
            endian = 0;
        if ((b & 0x1f) != 0)
            throw new IndexReadException("Last 5 bits of the third byte should be zero");
        // fourth byte
        vg.VTDDepth =  dis.readByte();
        
        // 5th and 6th byte
        int LCLevel = (((int)dis.readByte())<<8) | dis.readByte();
        if (LCLevel != 4 &&  LCLevel !=6)
            throw new IndexReadException("LC levels must be at least 3");
        // 7th and 8th byte
        if (LCLevel ==4)
        	vg.shallowDepth = true;
        else
        	vg.shallowDepth = false;
        // 7th and 8th byte
        vg.rootIndex = (((int)dis.readByte())<<8) | dis.readByte();
        
        // skip two longs
        dis.readLong();
        dis.readLong();
        int size = 0;
        // read XML size
        if (endian == 1)
           size = (int)dis.readLong();
        else
           size = (int)reverseLong(dis.readLong());
        // read XML bytes
        if (size!= XMLSize)
           throw new IndexReadException("XML size mismatch");
        byte[] XMLDoc = new byte[size];
        XMLBytes.read(XMLDoc);
        
        
        vg.setDoc(XMLDoc);
        
        dis.readLong();
        dis.readLong();
        if (endian ==1){
            // read vtd records
            int vtdSize = (int)dis.readLong();
            while(vtdSize>0){
                vg.VTDBuffer.append(dis.readLong());
                vtdSize--;
            }
            // read L1 LC records
            int l1Size = (int)dis.readLong();
                     
            while(l1Size > 0){
                long l = dis.readLong();
               // System.out.println(" l-==> "+Long.toHexString(l));
                vg.l1Buffer.append(l);
                l1Size--;
            }
            //System.out.println("++++++++++ ");
            // read L2 LC records
            int l2Size = (int)dis.readLong();
            while(l2Size > 0){
                vg.l2Buffer.append(dis.readLong());
                l2Size--;
            }
            //System.out.println("++++++++++ ");   
            // read L3 LC records
            int l3Size = (int)dis.readLong();
			if (vg.shallowDepth) {				
				if (intLongSwitch == 1) { // l3 uses ints
					while (l3Size > 0) {
						vg.l3Buffer.append(dis.readInt());
						l3Size--;
					}
				} else {
					while (l3Size > 0) {
						vg.l3Buffer.append((int) (dis.readLong() >> 32));
						l3Size--;
					}
				}
			} else {
				while (l3Size > 0) {
					vg._l3Buffer.append(dis.readLong());
					l3Size--;
				}
				
				int l4Size = (int)dis.readLong();
	            while(l2Size > 0){
	                vg._l4Buffer.append(dis.readLong());
	                l4Size--;
	            }
	            
	            int l5Size = (int)dis.readLong();
	            if (intLongSwitch == 1) { // l5 uses ints
	            	while(l2Size > 0){
	            		vg._l5Buffer.append(dis.readInt());
	            		l5Size--;
	            	}
	            }else {
	            	while (l5Size > 0) {
						vg._l5Buffer.append((int) (dis.readLong() >> 32));
						l5Size--;
					}
	            }
			}
        } else {
            // read vtd records
            int vtdSize = (int)reverseLong(dis.readLong());
            while(vtdSize>0){
                vg.VTDBuffer.append(reverseLong(dis.readLong()));
                vtdSize--;
            }
            // read L1 LC records
            //System.out.println(" ++++++++++ ");
            int l1Size = (int)reverseLong(dis.readLong());
            while(l1Size > 0){
                long l = reverseLong(dis.readLong());
                vg.l1Buffer.append(l);
                l1Size--;
            }
            //System.out.println(" ++++++++++ ");
            // read L2 LC records
            int l2Size = (int)reverseLong(dis.readLong());
            while(l2Size > 0){
                long l = reverseLong(dis.readLong());
                //System.out.println(" l--=->"+Long.toHexString(l));
                vg.l2Buffer.append(l);
                l2Size--;
            }
            //System.out.println(" ++++++++++ ");
            // read L3 LC records
            int l3Size = (int)reverseLong(dis.readLong());
			if (vg.shallowDepth) {
				if (intLongSwitch == 1) { // l3 uses ints
					while (l3Size > 0) {
						vg.l3Buffer.append(reverseInt(dis.readInt()));
						l3Size--;
					}
				} else {
					while (l3Size > 0) {
						vg.l3Buffer
								.append(reverseInt((int) (dis.readLong() >> 32)));
						l3Size--;
					}
				}
			}else{
				while (l3Size > 0) {
					vg._l3Buffer.append(reverseLong(dis.readLong()));
					l3Size--;
				}
				int l4Size = (int)reverseLong(dis.readLong());
				while(l4Size > 0){
	                long l = reverseLong(dis.readLong());
	                vg._l4Buffer.append(l);
	                l4Size--;
	            }
				int l5Size = (int)reverseLong(dis.readLong());
				if (intLongSwitch == 1) { // l5 uses ints
	            	while(l5Size > 0){
	            		vg._l5Buffer.append(reverseInt(dis.readInt()));
	            		l5Size--;
	            	}
	            }else {
	            	while (l5Size > 0) {
						vg._l5Buffer.append(reverseInt((int) (dis.readLong() >> 32)));
						l5Size--;
					}
	            }
			}
        }
    }
    /**
     * reverse a long's endianess
     * @param l
     * @return
     *
     */
    private static long reverseLong(long l){
        long t = ((l & 0xff00000000000000L)>>>56)
        | ((l & 0xff000000000000L)>>40)
        | ((l & 0xff0000000000L)>>24)
        | ((l & 0xff00000000L)>>8)
        | ((l & 0xff000000L)<<8)
        | ((l & 0xff0000L)<<24)
        | ((l & 0xff00L)<<40)
        | ((l & 0xffL)<<56);
        //System.out.println(" t ==> "+Long.toHexString(l));
        return t;
    }
    
    /**
     * reverse the endianess of an int
     * @param i
     * @return
     *
     */
    private static int reverseInt(int i){
        int t = ((i & 0xff000000) >>> 24)
        | ((i & 0xff0000) >> 8)
        | ((i & 0xff00) << 8)
        | ((i & 0xff) << 24);
        return t;
    }
    
    private static long adjust(long l, int i){
        long l1 = (l & 0xffffffffL)+ i;
        long l2 = l & 0xffffffff00000000L;
        return l1|l2;        
    }
}



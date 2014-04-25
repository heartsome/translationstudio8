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
import java.nio.*;
import java.nio.channels.*;
/**
 * 
 * XMLMemMappedBuffer maps an XML document into memory using memory map.
 * It avoids the complete loading of the document in memory thus making it
 * possible to process very large files. But the trade off is potentially 
 * lower performance due to disk IO.
 */

public class XMLMemMappedBuffer implements IByteBuffer {
    MappedByteBuffer input[];
    FileChannel fc;
    RandomAccessFile raf;
    String fn;
    long length;
    public XMLMemMappedBuffer(){
        
    }
    
    public long length(){
        return length;
    }
    
    public byte byteAt(long index){
        return input[(int)(index>>30)].get((int)(index & 0x3fffffff));
    }
    
    public void readFile(String fileName) throws java.io.IOException, ParseExceptionHuge {
        File f = new File(fileName);
        fn = fileName;
        long l = f.length();
        length = l;
        if (l>= (1L<< 38)){
            throw new ParseExceptionHuge("document too big > 256 Gbyte");
        }
        raf = new RandomAccessFile(fileName, "r");
        fc  = raf.getChannel();
        int pageNumber = (int)(l>>30)+(((l & 0x3fffffffL)==0)?0:1);
                
        input = new MappedByteBuffer[pageNumber];
        long l2 =0;
        for (int i=0;i<pageNumber;i++){
            if (i < (pageNumber-1)){
                //bufferArray[i] = new byte[1<<30];
                input[i]= fc.map(FileChannel.MapMode.READ_ONLY, l2 ,1<<30);
                l2 = l2+(1<<30);
            }
            else{
                //bufferArray[i] = new byte[(int)l];
                input[i]= fc.map(FileChannel.MapMode.READ_ONLY, l2, l - ((long)i<<30));
            }
            //input[i] = new RandomAccessFile(fileName, "r").getChannel()
            //.map(FileChannel.MapMode.READ_ONLY, 0,(1<<32)-1);
        }
        //if (fc!=null)
        //fc.close();
        //if (raf!=null)
        //raf.close();
    }
    
	/**
	 * NOt implemented yet
	 */
    public byte[] getBytes(){
	    return null;
	}
	
	/**
	 * not implemented yet
	 */
    public byte[] getBytes(int offset, int len){
	    return (byte[]) null;
	}    
    
    /**
     * write the segment (denoted by its offset and length) into an output file stream
     */
    public void writeToFileOutputStream(java.io.FileOutputStream ost, long os, long len) 
	throws java.io.IOException{
    	
    	FileChannel ostChannel = ost.getChannel();
    	
    	fc.transferTo(os, len, ostChannel);
    	
	}
}

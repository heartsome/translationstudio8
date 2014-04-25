package net.heartsome.license.utils;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import net.heartsome.license.ProtectionFactory;
import net.heartsome.license.constants.Constants;

public class FileUtils {

	public static void writeFile(byte[] b, String fileName) {
		try {
			DataOutputStream out = new DataOutputStream(new FileOutputStream(
					fileName));
			out.write(b);
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static byte[] readFile(String fileName) {
		try {
			DataInputStream in = new DataInputStream(new BufferedInputStream(
					new FileInputStream(fileName)));
			int n = in.available();
			byte[] t = new byte[n];
			int i = 0;
			while (in.available() != 0) {
				t[i] = in.readByte();
				i++;
			}
			in.close();
			return t;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static boolean removeFile() {
		boolean flag1 = false;
		boolean flag2 = false;  
		File file = new File(ProtectionFactory.getFileName(1, Constants.PRODUCTID));  
	    if (file.isFile() && file.exists()) {  
	        file.delete();  
	        flag1 = true;  
	    }  
	    file = new File(ProtectionFactory.getFileName(2, Constants.PRODUCTID));  
	    if (file.isFile() && file.exists()) {  
	        file.delete();  
	        flag2 = true;  
	    }  
	    return flag1 && flag2;  
	}
	
	public static boolean isExsit() {
		boolean flag1 = false;
		boolean flag2 = false;  
		File file = new File(ProtectionFactory.getFileName(1, Constants.PRODUCTID));  
	    if (file.isFile() && file.exists()) {  
	        flag1 = true;  
	    }  
	    file = new File(ProtectionFactory.getFileName(2, Constants.PRODUCTID));  
	    if (file.isFile() && file.exists()) {  
	        flag2 = true;  
	    }  
	    return flag1 && flag2;  
	}
}

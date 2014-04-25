package net.heartsome.license;

import java.io.File;

import net.heartsome.license.encrypt.Encrypt;
import net.heartsome.license.encrypt.EncryptRSA;

import org.eclipse.jface.util.Util;

public class ProtectionFactory {
	
	public static String getSeries() {
		SeriesInterface s;
		if (Util.isWindows()) {
			s = new WindowsSeries();
		} else if (Util.isMac()) {
			s = new MacosxSeries();
		} else {
			s = new LinuxSeries();
		}
		
		return s.getSeries();
	}
	
	public static Encrypt getEncrypt() {
		Encrypt en = new EncryptRSA();
		return en;
	}
	
	public static String getFileName(int type, String productId) {
		String fileName = "";
		if (type == 1) {
			fileName = "file-permission";
		} else if (type == 2) {
			fileName = "install";
		} else {
			fileName = "123";
		}
		
		String folder = "";
		if (Util.isWindows()) {
			folder = System.getenv("windir");
			File f1 = new File(folder);
			if (!f1.exists()) {
				f1.mkdirs();
			}
			return System.getenv("windir") + "\\" + fileName + productId + ".inf";
		} else if (Util.isMac()) {
			folder = System.getProperty("user.home") + "/.local";
			File f1 = new File(folder);
			if (!f1.exists()) {
				f1.mkdirs();
			}
			return System.getProperty("user.home") + "/.local" + "/" + fileName + productId + ".eps";
		} else {
			folder = System.getProperty("user.home") + "/.local";
			File f1 = new File(folder);
			if (!f1.exists()) {
				f1.mkdirs();
			}
			return System.getProperty("user.home") + "/.local" + "/" + fileName + productId + ".msl"; 
		}
	}
	
	public static String getPlatform() {
		if (Util.isWindows()) {
			if ("32".equals(System.getProperty("sun.arch.data.model"))) {
				return "1";
			} else {
				return "2";
			}
		} else if (Util.isMac()) {
			return "5";
		} else {
			if ("32".equals(System.getProperty("sun.arch.data.model"))) {
				return "3";
			} else {
				return "4";
			}
		}
	}
	
	public static String getProduct() {
		if("U".equals(System.getProperty("TSEdition"))) {
			return "28";
		} else if ("F".equals(System.getProperty("TSEdition"))) {
			return "27";
		} else if ("P".equals(System.getProperty("TSEdition"))) {
			return "26";
		} else {
			return "25";
		}
	}
	
	public static String getProduct(String edition) {
		if("U".equals(edition)) {
			return "28";
		} else if ("F".equals(edition)) {
			return "27";
		} else if ("P".equals(edition)) {
			return "26";
		} else {
			return "25";
		}
	}
	
	public static String getVersion() {
		String version = System.getProperty("TSVersionDate");
		version = version.substring(0, version.lastIndexOf("."));
		return version.replaceAll("[.]", "_");
	}
}

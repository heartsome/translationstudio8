package net.heartsome.license;

import java.io.File;

import net.heartsome.license.encrypt.Encrypt;
import net.heartsome.license.encrypt.EncryptRSA;
import net.heartsome.license.utils.OsUtil;

public class ProtectionFactory {

	public static String getSeries() {
		SeriesInterface s;
		if (OsUtil.isWindows()) {
			s = new WindowsSeries();
		} else if (OsUtil.isMac()) {
			s = new MacosxSeries();
		} else {
			s = new LinuxSeries();
		}

		return s.getSeries();
	}

	public static Encrypt getEncrypt() throws Exception {
		Encrypt en = new EncryptRSA();
		return en;
	}

	public static String getFileName(int type, int productId) {
		String fileName = type == 1 ? "test" : "install";
		String folder = "";
		if (OsUtil.isWindows()) {
			folder = System.getenv("windir");
			File f1 = new File(folder);
			if (!f1.exists()) {
				f1.mkdirs();
			}
			return System.getenv("windir") + "\\" + fileName + productId
					+ ".inf";
		} else if (OsUtil.isMac()) {
			folder = System.getProperty("user.home") + "/.local";
			File f1 = new File(folder);
			if (!f1.exists()) {
				f1.mkdirs();
			}
			return System.getProperty("user.home") + "/.local" + "/" + fileName
					+ productId + ".eps";
		} else {
			folder = System.getProperty("user.home") + "/.local";
			File f1 = new File(folder);
			if (!f1.exists()) {
				f1.mkdirs();
			}
			return System.getProperty("user.home") + "/.local" + "/" + fileName
					+ productId + ".msl";
		}
	}
}

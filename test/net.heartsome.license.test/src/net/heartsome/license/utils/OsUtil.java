package net.heartsome.license.utils;

public class OsUtil {
	
	public static String getOS() {
		return System.getProperty("os.name");
	}
	
	public static boolean isWindows() {
		return getOS().startsWith("Windows");
	}
	
	public static boolean isMac() {
		return getOS().startsWith("Mac");
	}
	
	public static boolean isLinux() {
		return getOS().startsWith("Linux");
	}

	public static void main(String[] args) {
		System.out.println(getOS());
	}
	
}

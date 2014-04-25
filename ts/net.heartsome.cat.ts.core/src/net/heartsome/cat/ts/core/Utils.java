package net.heartsome.cat.ts.core;

public class Utils {
	public static final int OS_LINUX = 1;
	public static final int OS_MAC = 2;
	public static final int OS_WINDOWS = 3;

	/**
	 * 得到当前的操作系统。
	 * @return 操作系统，值为 {@link #OS_LINUX}、{@link #OS_MAC}、{@link #OS_WINDOWS};
	 */
	public static int getCurrentOS() {
		if (System.getProperty("file.separator").equals("\\")) { //$NON-NLS-1$ //$NON-NLS-2$
			// Windows
			return OS_WINDOWS;
		} else if (System.getProperty("user.home").startsWith("/Users")) { //$NON-NLS-1$ //$NON-NLS-2$
			// Mac
			return OS_MAC;
		} else {
			// Linux
			return OS_LINUX;
		}
	}

	/**
	 * 得到文件分隔符
	 * @return 在 UNIX 系统值为 <code>'/'</code>; 在 Windows 系统值为 <code>'\'</code>。
	 */
	public static String getFileSeparator() {
		return System.getProperty("file.separator");
	}

	/**
	 * 得到行分隔符
	 * @return Linux 系统值为 <code>'\n'</code>; Mac 系统值为 <code>'\r'</code>；Windows 系统值为 <code>'\r\n'</code>。 
	 */
	public static String getLineSeparator() {
		return System.getProperty("line.separator");
	}
}

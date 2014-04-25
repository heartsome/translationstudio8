package net.sourceforge.nattable.selection;

public class SelectionUtils {

	public static boolean noShiftOrControl(boolean withShiftMask, boolean withControlMask) {
		return !withShiftMask && !withControlMask;
	}

	public static boolean bothShiftAndControl(boolean withShiftMask, boolean withControlMask) {
		return withShiftMask && withControlMask;
	}
	
	public static boolean isControlOnly(boolean withShiftMask, boolean withControlMask) {
		return !withShiftMask && withControlMask;
	}
	
	public static boolean isShiftOnly(boolean withShiftMask, boolean withControlMask) {
		return withShiftMask && !withControlMask;
	}
}

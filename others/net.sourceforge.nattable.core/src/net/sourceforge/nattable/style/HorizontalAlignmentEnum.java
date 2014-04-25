package net.sourceforge.nattable.style;

import org.eclipse.swt.SWT;

public enum HorizontalAlignmentEnum {
	
	LEFT, CENTER, RIGHT;
	
	public static int getSWTStyle(IStyle cellStyle) {
		HorizontalAlignmentEnum horizontalAlignment = cellStyle.getAttributeValue(CellStyleAttributes.HORIZONTAL_ALIGNMENT);
		
		if (horizontalAlignment == null) {
			return SWT.NONE;
		}
		
		switch (horizontalAlignment) {
		case CENTER:
			return SWT.CENTER;
		case LEFT:
			return SWT.LEFT;
		case RIGHT:
			return SWT.RIGHT;
		default:
			return SWT.NONE;
		}
	}

}

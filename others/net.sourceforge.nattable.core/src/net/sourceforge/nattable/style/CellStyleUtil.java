package net.sourceforge.nattable.style;

import org.eclipse.swt.graphics.Rectangle;

import net.sourceforge.nattable.config.IConfigRegistry;
import net.sourceforge.nattable.layer.cell.LayerCell;

public class CellStyleUtil {

	public static IStyle getCellStyle(LayerCell cell, IConfigRegistry configRegistry) {
		return new CellStyleProxy(configRegistry, cell.getDisplayMode(), cell.getConfigLabels().getLabels());
	}

	public static int getHorizontalAlignmentPadding(IStyle cellStyle, Rectangle rectangle, int contentWidth) {
		HorizontalAlignmentEnum horizontalAlignment = cellStyle.getAttributeValue(CellStyleAttributes.HORIZONTAL_ALIGNMENT);
		return getHorizontalAlignmentPadding(horizontalAlignment, rectangle, contentWidth);
	}
	
	/**
	 * Calculate padding needed at the left to align horizontally. Defaults to CENTER horizontal alignment.
	 */
	public static int getHorizontalAlignmentPadding(HorizontalAlignmentEnum horizontalAlignment, Rectangle rectangle, int contentWidth) {
		if (horizontalAlignment == null) {
			horizontalAlignment = HorizontalAlignmentEnum.CENTER;
		}
		
		int padding = 0;
		
		switch (horizontalAlignment) {
		case CENTER:
			padding = (rectangle.width - contentWidth) / 2;
			break;
		case RIGHT:
			padding = rectangle.width - contentWidth;
			break;
		}
		
		if (padding < 0) {
			padding = 0;
		}

		return padding;
	}

	public static int getVerticalAlignmentPadding(IStyle cellStyle, Rectangle rectangle, int contentHeight) {
		VerticalAlignmentEnum verticalAlignment = cellStyle.getAttributeValue(CellStyleAttributes.VERTICAL_ALIGNMENT);
		return getVerticalAlignmentPadding(verticalAlignment, rectangle, contentHeight);
	}
	
	/**
	 * Calculate padding needed at the top to align vertically. Defaults to MIDDLE vertical alignment.
	 */
	public static int getVerticalAlignmentPadding(VerticalAlignmentEnum verticalAlignment, Rectangle rectangle, int contentHeight) {
		if (verticalAlignment == null) {
			verticalAlignment = VerticalAlignmentEnum.MIDDLE;
		}
		
		int padding = 0;

		switch (verticalAlignment) {
		case MIDDLE:
			padding = (rectangle.height - contentHeight) / 2;
			break;
		case BOTTOM:
			padding = rectangle.height - contentHeight;
			break;
		}
		
		if (padding < 0) {
			padding = 0;
		}

		return padding;
	}
	
}

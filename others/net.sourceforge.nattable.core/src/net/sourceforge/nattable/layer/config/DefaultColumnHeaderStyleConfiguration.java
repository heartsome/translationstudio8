package net.sourceforge.nattable.layer.config;

import net.sourceforge.nattable.config.AbstractRegistryConfiguration;
import net.sourceforge.nattable.config.CellConfigAttributes;
import net.sourceforge.nattable.config.IConfigRegistry;
import net.sourceforge.nattable.grid.GridRegion;
import net.sourceforge.nattable.painter.cell.ICellPainter;
import net.sourceforge.nattable.painter.cell.TextPainter;
import net.sourceforge.nattable.painter.cell.decorator.BeveledBorderDecorator;
import net.sourceforge.nattable.style.BorderStyle;
import net.sourceforge.nattable.style.CellStyleAttributes;
import net.sourceforge.nattable.style.DisplayMode;
import net.sourceforge.nattable.style.HorizontalAlignmentEnum;
import net.sourceforge.nattable.style.Style;
import net.sourceforge.nattable.style.VerticalAlignmentEnum;
import net.sourceforge.nattable.util.GUIHelper;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;

/**
 * Sets up column header styling.
 * Added by {@link DefaultColumnHeaderLayerConfiguration}
 */
public class DefaultColumnHeaderStyleConfiguration extends AbstractRegistryConfiguration {

	public Font font = GUIHelper.getFont(new FontData("Verdana", 10, SWT.NORMAL));
	public Color bgColor = GUIHelper.COLOR_WIDGET_BACKGROUND;
	public Color fgColor = GUIHelper.COLOR_WIDGET_FOREGROUND;
	public HorizontalAlignmentEnum hAlign = HorizontalAlignmentEnum.CENTER;
	public VerticalAlignmentEnum vAlign = VerticalAlignmentEnum.MIDDLE;
	public BorderStyle borderStyle = null;

	public ICellPainter cellPainter = new BeveledBorderDecorator(new TextPainter());

	public void configureRegistry(IConfigRegistry configRegistry) {
		configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_PAINTER, cellPainter, DisplayMode.NORMAL, GridRegion.COLUMN_HEADER);
		configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_PAINTER, cellPainter, DisplayMode.NORMAL, GridRegion.CORNER);

		// Normal
		Style cellStyle = new Style();
		cellStyle.setAttributeValue(CellStyleAttributes.BACKGROUND_COLOR, bgColor);
		cellStyle.setAttributeValue(CellStyleAttributes.FOREGROUND_COLOR, fgColor);
		cellStyle.setAttributeValue(CellStyleAttributes.HORIZONTAL_ALIGNMENT, hAlign);
		cellStyle.setAttributeValue(CellStyleAttributes.VERTICAL_ALIGNMENT, vAlign);
		cellStyle.setAttributeValue(CellStyleAttributes.BORDER_STYLE, borderStyle);
		cellStyle.setAttributeValue(CellStyleAttributes.FONT, font);

		configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, cellStyle, DisplayMode.NORMAL, GridRegion.COLUMN_HEADER);
		configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, cellStyle, DisplayMode.NORMAL, GridRegion.CORNER);
	}
}

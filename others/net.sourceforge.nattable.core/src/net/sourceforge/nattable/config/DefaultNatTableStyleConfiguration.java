package net.sourceforge.nattable.config;

import net.sourceforge.nattable.data.convert.DefaultDisplayConverter;
import net.sourceforge.nattable.painter.cell.ICellPainter;
import net.sourceforge.nattable.painter.cell.TextPainter;
import net.sourceforge.nattable.painter.cell.decorator.LineBorderDecorator;
import net.sourceforge.nattable.style.BorderStyle;
import net.sourceforge.nattable.style.CellStyleAttributes;
import net.sourceforge.nattable.style.HorizontalAlignmentEnum;
import net.sourceforge.nattable.style.Style;
import net.sourceforge.nattable.style.VerticalAlignmentEnum;
import net.sourceforge.nattable.util.GUIHelper;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;

public class DefaultNatTableStyleConfiguration extends AbstractRegistryConfiguration {

	public Color bgColor = GUIHelper.COLOR_WHITE;
	public Color fgColor = GUIHelper.COLOR_BLACK;
	public Font font = GUIHelper.DEFAULT_FONT;
	public HorizontalAlignmentEnum hAlign = HorizontalAlignmentEnum.CENTER;
	public VerticalAlignmentEnum vAlign = VerticalAlignmentEnum.MIDDLE;
	public BorderStyle borderStyle = null;

	public ICellPainter cellPainter = new LineBorderDecorator(new TextPainter());
	
	public void configureRegistry(IConfigRegistry configRegistry) {
		configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_PAINTER, cellPainter);

		Style cellStyle = new Style();
		cellStyle.setAttributeValue(CellStyleAttributes.BACKGROUND_COLOR, bgColor);
		cellStyle.setAttributeValue(CellStyleAttributes.FOREGROUND_COLOR, fgColor);
		cellStyle.setAttributeValue(CellStyleAttributes.FONT, font);
		cellStyle.setAttributeValue(CellStyleAttributes.HORIZONTAL_ALIGNMENT, hAlign);
		cellStyle.setAttributeValue(CellStyleAttributes.VERTICAL_ALIGNMENT, vAlign);
		cellStyle.setAttributeValue(CellStyleAttributes.BORDER_STYLE, borderStyle);
		
		configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, cellStyle);
	
		configRegistry.registerConfigAttribute(CellConfigAttributes.DISPLAY_CONVERTER, new DefaultDisplayConverter());
	}
}

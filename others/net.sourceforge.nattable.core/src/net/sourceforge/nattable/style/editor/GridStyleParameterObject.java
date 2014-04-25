package net.sourceforge.nattable.style.editor;

import net.sourceforge.nattable.config.CellConfigAttributes;
import net.sourceforge.nattable.config.IConfigRegistry;
import net.sourceforge.nattable.grid.cell.AlternatingRowConfigLabelAccumulator;
import net.sourceforge.nattable.style.CellStyleAttributes;
import net.sourceforge.nattable.style.DisplayMode;
import net.sourceforge.nattable.style.IStyle;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;

public class GridStyleParameterObject {

	public Font tableFont;
	public Color evenRowColor;
	public Color oddRowColor;
	public Color selectionColor;

	public IStyle evenRowStyle;
	public IStyle oddRowStyle;
	public IStyle selectionStyle;
	public IStyle tableStyle;

	private final IConfigRegistry configRegistry;

	public GridStyleParameterObject(IConfigRegistry configRegistry) {
		this.configRegistry = configRegistry;
		init(configRegistry);
	}

	private void init(IConfigRegistry configRegistry) {
		evenRowStyle = configRegistry.getConfigAttribute(
				CellConfigAttributes.CELL_STYLE, 
				DisplayMode.NORMAL, 
				AlternatingRowConfigLabelAccumulator.EVEN_ROW_CONFIG_TYPE);
		evenRowColor = evenRowStyle.getAttributeValue(CellStyleAttributes.BACKGROUND_COLOR);

		oddRowStyle = configRegistry.getConfigAttribute(
				CellConfigAttributes.CELL_STYLE, 
				DisplayMode.NORMAL, 
				AlternatingRowConfigLabelAccumulator.ODD_ROW_CONFIG_TYPE);
		oddRowColor = oddRowStyle.getAttributeValue(CellStyleAttributes.BACKGROUND_COLOR);

		selectionStyle = configRegistry.getConfigAttribute(CellConfigAttributes.CELL_STYLE, DisplayMode.SELECT);
		selectionColor = selectionStyle.getAttributeValue(CellStyleAttributes.BACKGROUND_COLOR);
		
		tableStyle = configRegistry.getConfigAttribute(CellConfigAttributes.CELL_STYLE, DisplayMode.NORMAL);
		tableFont = tableStyle.getAttributeValue(CellStyleAttributes.FONT);
	}
	
	public IConfigRegistry getConfigRegistry() {
		return configRegistry;
	}

}

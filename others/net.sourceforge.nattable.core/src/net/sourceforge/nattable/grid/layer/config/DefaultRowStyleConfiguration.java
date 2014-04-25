package net.sourceforge.nattable.grid.layer.config;

import net.sourceforge.nattable.config.AbstractRegistryConfiguration;
import net.sourceforge.nattable.config.CellConfigAttributes;
import net.sourceforge.nattable.config.IConfigRegistry;
import net.sourceforge.nattable.grid.cell.AlternatingRowConfigLabelAccumulator;
import net.sourceforge.nattable.style.CellStyleAttributes;
import net.sourceforge.nattable.style.DisplayMode;
import net.sourceforge.nattable.style.Style;
import net.sourceforge.nattable.util.GUIHelper;

import org.eclipse.swt.graphics.Color;

/**
 * Sets up alternate row coloring. Applied by {@link DefaultGridLayerConfiguration}
 */
public class DefaultRowStyleConfiguration extends AbstractRegistryConfiguration {

	public Color evenRowBgColor = GUIHelper.COLOR_WIDGET_BACKGROUND;
	public Color oddRowBgColor = GUIHelper.COLOR_WHITE;

	public void configureRegistry(IConfigRegistry configRegistry) {
		configureOddRowStyle(configRegistry);
		configureEvenRowStyle(configRegistry);
	}

	protected void configureOddRowStyle(IConfigRegistry configRegistry) {
		Style cellStyle = new Style();
		cellStyle.setAttributeValue(CellStyleAttributes.BACKGROUND_COLOR,  oddRowBgColor);
		configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, cellStyle, DisplayMode.NORMAL, AlternatingRowConfigLabelAccumulator.EVEN_ROW_CONFIG_TYPE);
	}

	protected void configureEvenRowStyle(IConfigRegistry configRegistry) {
		Style cellStyle = new Style();
		cellStyle.setAttributeValue(CellStyleAttributes.BACKGROUND_COLOR,  evenRowBgColor);
		configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, cellStyle, DisplayMode.NORMAL, AlternatingRowConfigLabelAccumulator.ODD_ROW_CONFIG_TYPE);
	}
}

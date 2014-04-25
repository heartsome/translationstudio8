package net.sourceforge.nattable.selection.config;

import net.sourceforge.nattable.config.AbstractRegistryConfiguration;
import net.sourceforge.nattable.config.CellConfigAttributes;
import net.sourceforge.nattable.config.IConfigRegistry;
import net.sourceforge.nattable.grid.GridRegion;
import net.sourceforge.nattable.style.BorderStyle;
import net.sourceforge.nattable.style.CellStyleAttributes;
import net.sourceforge.nattable.style.DisplayMode;
import net.sourceforge.nattable.style.SelectionStyleLabels;
import net.sourceforge.nattable.style.Style;
import net.sourceforge.nattable.style.BorderStyle.LineStyleEnum;
import net.sourceforge.nattable.util.GUIHelper;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;

/**
 * Sets up rendering style used for selected areas and the selection anchor.
 */
public class DefaultSelectionStyleConfiguration extends AbstractRegistryConfiguration {

	// Selection style
	public Font selectionFont = GUIHelper.getFont(new FontData("Verdana", 8, SWT.BOLD | SWT.ITALIC));
	public Color selectionBgColor = GUIHelper.COLOR_TITLE_INACTIVE_BACKGROUND;
	public Color selectionFgColor = GUIHelper.COLOR_BLACK;

	// Anchor style
	public Color anchorBorderColor = GUIHelper.COLOR_DARK_GRAY;
	public BorderStyle anchorBorderStyle = new BorderStyle(1, anchorBorderColor, LineStyleEnum.SOLID);
	public Color anchorBgColor = GUIHelper.COLOR_GRAY;
	public Color anchorFgColor = GUIHelper.COLOR_WHITE;

	// Selected headers style
	public Color selectedHeaderBgColor = GUIHelper.COLOR_GRAY;
	public Color selectedHeaderFgColor = GUIHelper.COLOR_WHITE;
	public Font selectedHeaderFont = GUIHelper.getFont(new FontData("Verdana", 10, SWT.BOLD));
	public BorderStyle selectedHeaderBorderStyle = new BorderStyle(-1, selectedHeaderFgColor, LineStyleEnum.SOLID);

	public void configureRegistry(IConfigRegistry configRegistry) {
		configureSelectionStyle(configRegistry);
		configureSelectionAnchorStyle(configRegistry);
		configureHeaderHasSelectionStyle(configRegistry);
		configureHeaderFullySelectedStyle(configRegistry);
	}

	protected void configureSelectionStyle(IConfigRegistry configRegistry) {
		Style cellStyle = new Style();
		cellStyle.setAttributeValue(CellStyleAttributes.FONT, selectionFont);
		cellStyle.setAttributeValue(CellStyleAttributes.BACKGROUND_COLOR, selectionBgColor);
		cellStyle.setAttributeValue(CellStyleAttributes.FOREGROUND_COLOR, selectionFgColor);
		configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, cellStyle, DisplayMode.SELECT);
	}

	protected void configureSelectionAnchorStyle(IConfigRegistry configRegistry) {
		// Selection anchor style for normal display mode
		Style cellStyle = new Style();
		cellStyle.setAttributeValue(CellStyleAttributes.BORDER_STYLE, anchorBorderStyle);
		configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, cellStyle, DisplayMode.NORMAL, SelectionStyleLabels.SELECTION_ANCHOR_STYLE);

		// Selection anchor style for select display mode
		cellStyle = new Style();
		cellStyle.setAttributeValue(CellStyleAttributes.BACKGROUND_COLOR, anchorBgColor);
		cellStyle.setAttributeValue(CellStyleAttributes.FOREGROUND_COLOR, anchorFgColor);
		cellStyle.setAttributeValue(CellStyleAttributes.BORDER_STYLE, anchorBorderStyle);
		configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, cellStyle, DisplayMode.SELECT, SelectionStyleLabels.SELECTION_ANCHOR_STYLE);
	}

	protected void configureHeaderHasSelectionStyle(IConfigRegistry configRegistry) {
		Style cellStyle = new Style();

		cellStyle.setAttributeValue(CellStyleAttributes.FOREGROUND_COLOR, selectedHeaderFgColor);
		cellStyle.setAttributeValue(CellStyleAttributes.BACKGROUND_COLOR, selectedHeaderBgColor);
		cellStyle.setAttributeValue(CellStyleAttributes.FONT, selectedHeaderFont);
		cellStyle.setAttributeValue(CellStyleAttributes.BORDER_STYLE, selectedHeaderBorderStyle);

		configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, cellStyle, DisplayMode.SELECT, GridRegion.COLUMN_HEADER);
		configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, cellStyle, DisplayMode.SELECT, GridRegion.CORNER);
		configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, cellStyle, DisplayMode.SELECT, GridRegion.ROW_HEADER);
	}

	protected void configureHeaderFullySelectedStyle(IConfigRegistry configRegistry) {
		// Header fully selected
		Style cellStyle = new Style() {{
			setAttributeValue(CellStyleAttributes.BACKGROUND_COLOR,  GUIHelper.COLOR_WIDGET_NORMAL_SHADOW);
		}};
		configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, cellStyle, DisplayMode.SELECT, SelectionStyleLabels.COLUMN_FULLY_SELECTED_STYLE);
		configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, cellStyle, DisplayMode.SELECT, SelectionStyleLabels.ROW_FULLY_SELECTED_STYLE);
	}
}

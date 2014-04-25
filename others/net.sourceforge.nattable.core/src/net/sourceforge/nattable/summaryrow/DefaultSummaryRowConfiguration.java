package net.sourceforge.nattable.summaryrow;

import net.sourceforge.nattable.config.AbstractRegistryConfiguration;
import net.sourceforge.nattable.config.CellConfigAttributes;
import net.sourceforge.nattable.config.IConfigRegistry;
import net.sourceforge.nattable.style.BorderStyle;
import net.sourceforge.nattable.style.CellStyleAttributes;
import net.sourceforge.nattable.style.DisplayMode;
import net.sourceforge.nattable.style.Style;
import net.sourceforge.nattable.style.BorderStyle.LineStyleEnum;
import net.sourceforge.nattable.util.GUIHelper;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;

public class DefaultSummaryRowConfiguration extends AbstractRegistryConfiguration {

	public BorderStyle summaryRowBorderStyle = new BorderStyle(0, GUIHelper.COLOR_BLACK, LineStyleEnum.DOTTED);
	public Color summaryRowFgColor = GUIHelper.COLOR_BLACK;
	public Color summaryRowBgColor = GUIHelper.COLOR_WHITE;
	public Font summaryRowFont = GUIHelper.getFont(new FontData("Verdana", 8, SWT.BOLD));

	public void configureRegistry(IConfigRegistry configRegistry) {
		addSummaryRowStyleConfig(configRegistry);
		addSummaryProviderConfig(configRegistry);
	}

	protected void addSummaryRowStyleConfig(IConfigRegistry configRegistry) {
		Style cellStyle = new Style();
		cellStyle.setAttributeValue(CellStyleAttributes.FONT, summaryRowFont);
		cellStyle.setAttributeValue(CellStyleAttributes.BACKGROUND_COLOR, summaryRowBgColor);
		cellStyle.setAttributeValue(CellStyleAttributes.FOREGROUND_COLOR, summaryRowFgColor);
		cellStyle.setAttributeValue(CellStyleAttributes.BORDER_STYLE, summaryRowBorderStyle);
		configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, cellStyle, DisplayMode.NORMAL, SummaryRowLayer.DEFAULT_SUMMARY_ROW_CONFIG_LABEL);
	}

	protected void addSummaryProviderConfig(IConfigRegistry configRegistry) {
		configRegistry.registerConfigAttribute(SummaryRowConfigAttributes.SUMMARY_PROVIDER, ISummaryProvider.DEFAULT, DisplayMode.NORMAL, SummaryRowLayer.DEFAULT_SUMMARY_ROW_CONFIG_LABEL);
	}
}

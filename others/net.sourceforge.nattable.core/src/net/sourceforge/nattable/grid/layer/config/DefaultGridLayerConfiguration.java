package net.sourceforge.nattable.grid.layer.config;

import net.sourceforge.nattable.config.AggregateConfiguration;
import net.sourceforge.nattable.edit.config.DefaultEditBindings;
import net.sourceforge.nattable.edit.config.DefaultEditConfiguration;
import net.sourceforge.nattable.export.excel.config.DefaultExportToExcelBindings;
import net.sourceforge.nattable.grid.GridRegion;
import net.sourceforge.nattable.grid.cell.AlternatingRowConfigLabelAccumulator;
import net.sourceforge.nattable.grid.layer.GridLayer;
import net.sourceforge.nattable.print.config.DefaultPrintBindings;

/**
 * Sets up features handled at the grid level. Added by {@link GridLayer}
 */
public class DefaultGridLayerConfiguration extends AggregateConfiguration {

	public DefaultGridLayerConfiguration(GridLayer gridLayer) {
		addAlternateRowColoringConfig(gridLayer);
		addEditingHandlerConfig();
		addEditingUIConfig();
		addPrintUIBindings();
		addExcelExportUIBindings();
	}

	protected void addExcelExportUIBindings() {
		addConfiguration(new DefaultExportToExcelBindings());
	}

	protected void addPrintUIBindings() {
		addConfiguration(new DefaultPrintBindings());
	}

	protected void addEditingUIConfig() {
		addConfiguration(new DefaultEditBindings());
	}

	protected void addEditingHandlerConfig() {
		addConfiguration(new DefaultEditConfiguration());
	}

	protected void addAlternateRowColoringConfig(GridLayer gridLayer) {
		addConfiguration(new DefaultRowStyleConfiguration());
		gridLayer.setConfigLabelAccumulatorForRegion(GridRegion.BODY, new AlternatingRowConfigLabelAccumulator());
	}

}

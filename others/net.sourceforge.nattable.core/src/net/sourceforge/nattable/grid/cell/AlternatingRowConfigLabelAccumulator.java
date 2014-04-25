package net.sourceforge.nattable.grid.cell;

import net.sourceforge.nattable.grid.GridRegion;
import net.sourceforge.nattable.grid.layer.config.DefaultRowStyleConfiguration;
import net.sourceforge.nattable.layer.LabelStack;
import net.sourceforge.nattable.layer.cell.IConfigLabelAccumulator;

/**
 * Applies 'odd'/'even' labels to all the rows. These labels are
 * the used to apply color to alternate rows.
 *
 * @see DefaultRowStyleConfiguration
 */
public class AlternatingRowConfigLabelAccumulator implements IConfigLabelAccumulator {

	public static final String ODD_ROW_CONFIG_TYPE = "ODD_" + GridRegion.BODY;

	public static final String EVEN_ROW_CONFIG_TYPE = "EVEN_" + GridRegion.BODY;

	public void accumulateConfigLabels(LabelStack configLabels, int columnPosition, int rowPosition) {
		configLabels.addLabel((rowPosition % 2 == 0 ? EVEN_ROW_CONFIG_TYPE : ODD_ROW_CONFIG_TYPE));
	}
}

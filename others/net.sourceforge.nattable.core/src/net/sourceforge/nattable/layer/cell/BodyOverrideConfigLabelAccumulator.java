package net.sourceforge.nattable.layer.cell;

import java.util.Arrays;
import java.util.List;

import net.sourceforge.nattable.layer.LabelStack;

/**
 * Applies the given labels to all the cells in the grid.<br/>
 * Used to apply styles to the entire grid.
 */
public class BodyOverrideConfigLabelAccumulator implements IConfigLabelAccumulator {

	private List<String> configLabels;

	public void accumulateConfigLabels(LabelStack configLabels, int columnPosition, int rowPosition) {
		configLabels.getLabels().addAll(this.configLabels);
	}

	public void registerOverrides(String... configLabels) {
		this.configLabels = Arrays.asList(configLabels);
	}

	public void addOverride(String configLabel) {
		this.configLabels.add(configLabel);
	}

}

package net.sourceforge.nattable.layer.cell;

import net.sourceforge.nattable.layer.LabelStack;

public class SimpleConfigLabelAccumulator implements IConfigLabelAccumulator {

	private final String configLabel;

	public SimpleConfigLabelAccumulator(String configLabel) {
		this.configLabel = configLabel;
	}

	public void accumulateConfigLabels(LabelStack configLabels, int columnPosition, int rowPosition) {
		configLabels.addLabel(configLabel);
	}
	
}

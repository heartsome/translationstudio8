package net.sourceforge.nattable.layer.cell;

import net.sourceforge.nattable.data.IRowDataProvider;
import net.sourceforge.nattable.layer.LabelStack;

/**
 * Adds the Java class name of the cell's data value as a label.   
 */
public class ClassNameConfigLabelAccumulator implements IConfigLabelAccumulator {

	private IRowDataProvider<?> dataProvider;
	
	public ClassNameConfigLabelAccumulator(IRowDataProvider<?> dataProvider) {
		this.dataProvider = dataProvider;
	}
	
	public void accumulateConfigLabels(LabelStack configLabel, int columnPosition, int rowPosition) {
		Object value = dataProvider.getDataValue(columnPosition, rowPosition);
		if (value != null) {
			configLabel.addLabel(value.getClass().getName());
		}
	}

}

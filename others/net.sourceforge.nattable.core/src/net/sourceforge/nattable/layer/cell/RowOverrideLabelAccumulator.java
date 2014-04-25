package net.sourceforge.nattable.layer.cell;

import java.io.Serializable;

import net.sourceforge.nattable.data.IRowDataProvider;
import net.sourceforge.nattable.data.IRowIdAccessor;
import net.sourceforge.nattable.layer.LabelStack;

/**
 * @see ColumnOverrideLabelAccumulator
 * @param <T> type of the bean used as the data source for a row 
 */
public class RowOverrideLabelAccumulator<T> extends AbstractOverrider {
	
	private IRowDataProvider<T> dataProvider;
	private IRowIdAccessor<T> idAccessor;

	public RowOverrideLabelAccumulator(IRowDataProvider<T> dataProvider, IRowIdAccessor<T> idAccessor) {
		this.dataProvider = dataProvider;
		this.idAccessor = idAccessor;
	}
	
	public void accumulateConfigLabels(LabelStack configLabels, int columnPosition, int rowPosition) {
		for (String configLabel : getOverrides(idAccessor.getRowId(dataProvider.getRowObject(rowPosition)))) {
			configLabels.addLabel(configLabel);
		}
	}

	public void registerOverrides(int rowIndex, String...configLabels) {
		Serializable id = idAccessor.getRowId(dataProvider.getRowObject(rowIndex));
		registerOverrides(id, configLabels);
	}
	
}

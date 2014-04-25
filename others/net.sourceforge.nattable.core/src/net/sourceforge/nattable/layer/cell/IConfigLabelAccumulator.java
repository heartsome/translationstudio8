package net.sourceforge.nattable.layer.cell;

import net.sourceforge.nattable.config.IConfigRegistry;
import net.sourceforge.nattable.layer.LabelStack;

/**
 * Resolves the configuration/config label(s) which are tied to a given cell.<br/>
 * Various attributes can be registered in the {@link IConfigRegistry} against this</br>
 * label</br> 
 */
public interface IConfigLabelAccumulator {
	
	/**
	 * Add labels applicable to this cell position
	 * @param configLabels the labels currently applied to the cell. The labels contributed by this 
	 * provider must be <i>added</i> to this stack
	 * @param columnPosition of the cell for which labels are being gathered
	 * @param rowPosition of the cell for which labels are being gathered
	 */
	public void accumulateConfigLabels(LabelStack configLabels, int columnPosition, int rowPosition);
	
}

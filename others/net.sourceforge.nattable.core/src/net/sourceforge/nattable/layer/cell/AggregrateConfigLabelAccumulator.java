package net.sourceforge.nattable.layer.cell;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.sourceforge.nattable.layer.LabelStack;

/**
 * An {@link ICellLabelAccumulator} that can aggregate labels from other <code>ICellLabelAccumulator</code>s.<br/> 
 * All the labels provided by the aggregated accumulators are applied to the cell.<be/>
 */
public class AggregrateConfigLabelAccumulator implements IConfigLabelAccumulator {
    
    private List<IConfigLabelAccumulator> accumulators = new ArrayList<IConfigLabelAccumulator>();
    
    public void add(IConfigLabelAccumulator r) {
        if (r == null) throw new IllegalArgumentException("null");
        accumulators.add(r);
    }

    public void add(IConfigLabelAccumulator... r) {
    	if (r == null) throw new IllegalArgumentException("null");
    	accumulators.addAll(Arrays.asList(r));
    }

    public void accumulateConfigLabels(LabelStack configLabels, int columnPosition, int rowPosition) {
        for (IConfigLabelAccumulator accumulator : accumulators) {
        	accumulator.accumulateConfigLabels(configLabels, columnPosition, rowPosition);
        }
    }

}

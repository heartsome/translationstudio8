package net.sourceforge.nattable.selection.config;

import net.sourceforge.nattable.config.AbstractLayerConfiguration;
import net.sourceforge.nattable.layer.event.ILayerEventHandler;
import net.sourceforge.nattable.selection.MoveRowSelectionCommandHandler;
import net.sourceforge.nattable.selection.SelectionLayer;

/**
 * Configure the move selection behavior so that we always move by a row.<br/>
 * Add {@link ILayerEventHandler} to preserve row selection.<br/>
 * 
 * @see DefaultMoveSelectionConfiguration
 */
public class RowOnlySelectionConfiguration<T> extends AbstractLayerConfiguration<SelectionLayer> {

	@Override
	public void configureTypedLayer(SelectionLayer layer) {
		layer.registerCommandHandler(new MoveRowSelectionCommandHandler(layer));
	}
}

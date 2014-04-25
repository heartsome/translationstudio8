package net.sourceforge.nattable.selection.config;

import net.sourceforge.nattable.config.AbstractLayerConfiguration;
import net.sourceforge.nattable.selection.MoveCellSelectionCommandHandler;
import net.sourceforge.nattable.selection.MoveRowSelectionCommandHandler;
import net.sourceforge.nattable.selection.SelectionLayer;
import net.sourceforge.nattable.selection.command.MoveSelectionCommand;

/**
 * Configure the behavior when the selection is moved. Example: by using arrow keys.<br/>
 * This default configuration moves by cell.<br/>
 * 
 * {@link MoveSelectionCommand} are fired by the {@link DefaultSelectionBindings}.<br/>
 * An suitable handler can be plugged in to handle the move commands as required.<br/>
 * 
 * @see MoveRowSelectionCommandHandler
 */
public class DefaultMoveSelectionConfiguration extends AbstractLayerConfiguration<SelectionLayer>{

	@Override
	public void configureTypedLayer(SelectionLayer layer) {
		layer.registerCommandHandler(new MoveCellSelectionCommandHandler(layer));
	}

}

package net.sourceforge.nattable.resize.command;

import net.sourceforge.nattable.command.AbstractColumnCommand;
import net.sourceforge.nattable.command.ILayerCommand;
import net.sourceforge.nattable.config.IConfigRegistry;
import net.sourceforge.nattable.grid.layer.GridLayer;
import net.sourceforge.nattable.layer.ILayer;
import net.sourceforge.nattable.selection.SelectionLayer;

import org.eclipse.swt.graphics.GC;

/**
 * This command triggers the AutoResizeColumms command. It collects the selected
 * columns from the {@link SelectionLayer} and fires the
 * {@link AutoResizeColumnsCommand} on the {@link GridLayer}
 */

public class InitializeAutoResizeColumnsCommand extends AbstractColumnCommand {

	private final IConfigRegistry configRegistry;
	private final GC gc;
	private final ILayer sourceLayer;
	private int[] selectedColumnPositions = new int[0];

	public InitializeAutoResizeColumnsCommand(ILayer layer, int columnPosition, IConfigRegistry configRegistry, GC gc) {
		super(layer, columnPosition);
		this.configRegistry = configRegistry;
		this.gc = gc;
		this.sourceLayer = layer;
	}

	protected InitializeAutoResizeColumnsCommand(InitializeAutoResizeColumnsCommand command) {
		super(command);
		this.configRegistry = command.configRegistry;
		this.gc = command.gc;
		this.sourceLayer = command.sourceLayer;
	}

	public ILayerCommand cloneCommand() {
		return new InitializeAutoResizeColumnsCommand(this);
	}

	// Accessors

	public GC getGC() {
		return gc;
	}

	public IConfigRegistry getConfigRegistry() {
		return configRegistry;
	}

	public ILayer getSourceLayer() {
		return sourceLayer;
	}

	public void setSelectedColumnPositions(int[] selectedColumnPositions) {
		this.selectedColumnPositions = selectedColumnPositions;
	}

	public int[] getColumnPositions() {
		return selectedColumnPositions;
	}
}
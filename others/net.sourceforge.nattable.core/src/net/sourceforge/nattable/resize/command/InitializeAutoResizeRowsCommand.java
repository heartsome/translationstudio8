package net.sourceforge.nattable.resize.command;

import net.sourceforge.nattable.command.AbstractRowCommand;
import net.sourceforge.nattable.command.ILayerCommand;
import net.sourceforge.nattable.config.IConfigRegistry;
import net.sourceforge.nattable.layer.ILayer;

import org.eclipse.swt.graphics.GC;

/**
 * @see InitializeAutoResizeColumnsCommand
 */

public class InitializeAutoResizeRowsCommand extends AbstractRowCommand {

	private final IConfigRegistry configRegistry;
	private final GC gc;
	private final ILayer sourceLayer;
	private int[] selectedRowPositions = new int[0];

	public InitializeAutoResizeRowsCommand(ILayer layer, int rowPosition, IConfigRegistry configRegistry, GC gc) {
		super(layer, rowPosition);
		this.configRegistry = configRegistry;
		this.gc = gc;
		this.sourceLayer = layer;
	}

	protected InitializeAutoResizeRowsCommand(InitializeAutoResizeRowsCommand command) {
		super(command);
		this.configRegistry = command.configRegistry;
		this.gc = command.gc;
		this.sourceLayer = command.sourceLayer;
	}

	public ILayerCommand cloneCommand() {
		return new InitializeAutoResizeRowsCommand(this);
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

	public void setSelectedRowPositions(int[] selectedRowPositions) {
		this.selectedRowPositions = selectedRowPositions;
	}

	public int[] getRowPositions() {
		return selectedRowPositions;
	}
}
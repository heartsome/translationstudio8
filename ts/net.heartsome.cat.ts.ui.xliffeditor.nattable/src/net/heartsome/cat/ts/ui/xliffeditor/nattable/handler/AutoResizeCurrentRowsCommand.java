package net.heartsome.cat.ts.ui.xliffeditor.nattable.handler;

import net.sourceforge.nattable.command.AbstractMultiRowCommand;
import net.sourceforge.nattable.command.ILayerCommand;
import net.sourceforge.nattable.config.IConfigRegistry;
import net.sourceforge.nattable.layer.ILayer;

/**
 * 自适应当前显示行的高度的Command
 * @author  weachy
 * @version 
 * @since   JDK1.5
 */
public class AutoResizeCurrentRowsCommand extends AbstractMultiRowCommand {

	private final IConfigRegistry configRegistry;
	private int[] rows;

	protected AutoResizeCurrentRowsCommand(AutoResizeCurrentRowsCommand command) {
		super(command);
		this.configRegistry = command.configRegistry;
	}

	public AutoResizeCurrentRowsCommand(ILayer layer, int[] rowPositions, IConfigRegistry configRegistry) {
		super(layer);
		this.configRegistry = configRegistry;
		this.rows = rowPositions;
	}

	public ILayerCommand cloneCommand() {
		return new AutoResizeCurrentRowsCommand(this);
	}

	public IConfigRegistry getConfigRegistry() {
		return configRegistry;
	}

	public int[] getRows() {
		return rows;
	}
}

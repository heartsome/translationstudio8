package net.sourceforge.nattable.print.command;

import net.sourceforge.nattable.command.AbstractLayerCommandHandler;
import net.sourceforge.nattable.grid.layer.GridLayer;
import net.sourceforge.nattable.print.GridLayerPrinter;

public class PrintCommandHandler extends AbstractLayerCommandHandler<PrintCommand> {

	private final GridLayer gridLayer;

	public PrintCommandHandler(GridLayer defaultGridLayer) {
		this.gridLayer = defaultGridLayer;
	}

	public boolean doCommand(PrintCommand command) {
		new GridLayerPrinter(gridLayer, command.getConfigRegistry()).print(command.getShell());
		return true;
	}

	public Class<PrintCommand> getCommandClass() {
		return PrintCommand.class;
	}

}

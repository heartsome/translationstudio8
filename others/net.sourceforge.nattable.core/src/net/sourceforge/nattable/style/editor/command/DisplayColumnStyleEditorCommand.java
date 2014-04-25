package net.sourceforge.nattable.style.editor.command;

import net.sourceforge.nattable.command.AbstractContextFreeCommand;
import net.sourceforge.nattable.config.IConfigRegistry;
import net.sourceforge.nattable.layer.ILayer;

public class DisplayColumnStyleEditorCommand extends AbstractContextFreeCommand {

	public final int columnPosition;
	public final int rowPosition;
	private final ILayer layer;
	private final IConfigRegistry configRegistry;

	public DisplayColumnStyleEditorCommand(ILayer natLayer, IConfigRegistry configRegistry, int columnPosition, int rowPosition) {
		this.layer = natLayer;
		this.configRegistry = configRegistry;
		this.columnPosition = columnPosition;
		this.rowPosition = rowPosition;
	}
	
	public ILayer getNattableLayer() {
		return layer;
	}

	public IConfigRegistry getConfigRegistry() {
		return configRegistry;
	}
}

package net.sourceforge.nattable.selection.command;

import net.sourceforge.nattable.command.AbstractPositionCommand;
import net.sourceforge.nattable.layer.ILayer;

public class SelectColumnCommand extends AbstractPositionCommand {
	
	private final boolean withShiftMask;
	private final boolean withControlMask;
	
	public SelectColumnCommand(ILayer layer, int columnPosition, int rowPosition, boolean withShiftMask, boolean withControlMask) {
		super(layer, columnPosition, rowPosition);
		this.withShiftMask = withShiftMask;
		this.withControlMask = withControlMask;
	}
	
	protected SelectColumnCommand(SelectColumnCommand command) {
		super(command);
		this.withShiftMask = command.withShiftMask;
		this.withControlMask = command.withControlMask;
	}

	public boolean isWithShiftMask() {
		return withShiftMask;
	}

	public boolean isWithControlMask() {
		return withControlMask;
	}
	
	public SelectColumnCommand cloneCommand() {
		return new SelectColumnCommand(this);
	}
	
}

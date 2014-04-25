package net.sourceforge.nattable.viewport.command;

import net.sourceforge.nattable.command.AbstractRowCommand;
import net.sourceforge.nattable.layer.ILayer;

/**
 * Command to select a row.
 * Note: The row position is in top level composite Layer (NatTable) coordinates
 */
public class ViewportSelectRowCommand extends AbstractRowCommand {
	
	private final boolean withShiftMask;
	private final boolean withControlMask;

	public ViewportSelectRowCommand(ILayer layer, int rowPosition, boolean withShiftMask, boolean withControlMask) {
		super(layer, rowPosition);
		this.withShiftMask = withShiftMask;
		this.withControlMask = withControlMask;
	}
	
	protected ViewportSelectRowCommand(ViewportSelectRowCommand command) {
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
	
	public ViewportSelectRowCommand cloneCommand() {
		return new ViewportSelectRowCommand(this);
	}
	
}

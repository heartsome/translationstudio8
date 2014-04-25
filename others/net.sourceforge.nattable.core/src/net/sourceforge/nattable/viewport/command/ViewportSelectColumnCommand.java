package net.sourceforge.nattable.viewport.command;

import net.sourceforge.nattable.command.AbstractColumnCommand;
import net.sourceforge.nattable.layer.ILayer;

public class ViewportSelectColumnCommand extends AbstractColumnCommand {
	
	private final boolean withShiftMask;
	private final boolean withControlMask;
	
	public ViewportSelectColumnCommand(ILayer layer, int columnPosition, boolean withShiftMask, boolean withControlMask) {
		super(layer, columnPosition);
		this.withShiftMask = withShiftMask;
		this.withControlMask = withControlMask;
	}
	
	protected ViewportSelectColumnCommand(ViewportSelectColumnCommand command) {
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
	
	public ViewportSelectColumnCommand cloneCommand() {
		return new ViewportSelectColumnCommand(this);
	}
	
}

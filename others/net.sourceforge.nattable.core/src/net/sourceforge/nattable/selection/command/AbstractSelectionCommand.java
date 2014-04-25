package net.sourceforge.nattable.selection.command;

import net.sourceforge.nattable.command.AbstractContextFreeCommand;

public abstract class AbstractSelectionCommand extends AbstractContextFreeCommand {
	
	private boolean shiftMask;
	private boolean controlMask;

	public AbstractSelectionCommand(boolean shiftMask, boolean controlMask) {
		this.shiftMask = shiftMask;
		this.controlMask = controlMask;
	}
	
	public boolean isShiftMask() {
		return shiftMask;
	}
	
	public boolean isControlMask() {
		return controlMask;
	}
	
}

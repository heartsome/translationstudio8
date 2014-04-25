package net.sourceforge.nattable.selection.command;

import net.sourceforge.nattable.selection.SelectionLayer.MoveDirectionEnum;

public class MoveSelectionCommand extends AbstractSelectionCommand {

	private final MoveDirectionEnum direction;
	private final int stepSize;

	public MoveSelectionCommand(MoveDirectionEnum direction, boolean shiftMask, boolean controlMask) {
		this(direction, 0, shiftMask, controlMask);
	}

	public MoveSelectionCommand(MoveDirectionEnum direction, int stepSize, boolean shiftMask, boolean controlMask) {
		super(shiftMask, controlMask);
		this.direction = direction;
		this.stepSize = stepSize;
	}

	public MoveDirectionEnum getDirection() {
		return direction;
	}

	public int getStepSize() {
		return stepSize;
	}


}

package net.sourceforge.nattable.selection.command;

import net.sourceforge.nattable.selection.SelectionLayer.MoveDirectionEnum;

public class ScrollSelectionCommand extends MoveSelectionCommand {

	public ScrollSelectionCommand(MoveDirectionEnum direction, boolean shiftMask, boolean controlMask) {
		super(direction, shiftMask, controlMask);
	}
	
}

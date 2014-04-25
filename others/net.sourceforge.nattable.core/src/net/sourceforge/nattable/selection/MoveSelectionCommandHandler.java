package net.sourceforge.nattable.selection;

import net.sourceforge.nattable.command.ILayerCommandHandler;
import net.sourceforge.nattable.layer.ILayer;
import net.sourceforge.nattable.selection.SelectionLayer.MoveDirectionEnum;
import net.sourceforge.nattable.selection.command.MoveSelectionCommand;

/**
 * Abstraction of the selection behavior during navigation in the grid.<br/>
 * Implementations of this class specify what to select when the selection moves<br/>
 * by responding to the {@link MoveSelectionCommand}.<br/>
 *
 * @param <T> an instance of the {@link MoveSelectionCommand}
 * @see MoveCellSelectionCommandHandler
 * @see MoveRowSelectionCommandHandler
 */
public abstract class MoveSelectionCommandHandler<T extends MoveSelectionCommand> implements ILayerCommandHandler<T> {

	protected final SelectionLayer selectionLayer;

	public MoveSelectionCommandHandler(SelectionLayer selectionLayer) {
		this.selectionLayer = selectionLayer;
	}

	public boolean doCommand(ILayer targetLayer, T command) {
		if (command.convertToTargetLayer(selectionLayer)) {
			moveSelection(command.getDirection(), command.getStepSize(), command.isShiftMask(), command.isControlMask());
			return true;
		}
		return false;
	}

	protected void moveSelection(MoveDirectionEnum moveDirection, int stepSize, boolean withShiftMask, boolean withControlMask) {
		switch (moveDirection) {
		case UP:
			moveLastSelectedUp(stepSize, withShiftMask, withControlMask);
			break;
		case DOWN:
			moveLastSelectedDown(stepSize, withShiftMask, withControlMask);
			break;
		case LEFT:
			moveLastSelectedLeft(stepSize, withShiftMask, withControlMask);
			break;
		case RIGHT:
			moveLastSelectedRight(stepSize, withShiftMask, withControlMask);
			break;
		default:
			break;
		}
	}

	protected abstract void moveLastSelectedRight(int stepSize, boolean withShiftMask, boolean withControlMask);
	protected abstract void moveLastSelectedLeft(int stepSize, boolean withShiftMask, boolean withControlMask);
	protected abstract void moveLastSelectedUp(int stepSize, boolean withShiftMask, boolean withControlMask);
	protected abstract void moveLastSelectedDown(int stepSize, boolean withShiftMask, boolean withControlMask);

}

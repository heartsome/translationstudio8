package net.sourceforge.nattable.group.command;

import java.util.List;

import net.sourceforge.nattable.layer.ILayer;
import net.sourceforge.nattable.reorder.command.MultiColumnReorderCommand;

/**
 * Reorder single multiple columns and column groups in one shot.
 *    - Needed by the column chooser  
 */
public class ReorderColumnsAndGroupsCommand extends MultiColumnReorderCommand {

	/**
	 * If any of the fromColumnPositions contain a group 
	 *    - the group will be moved.
	 */
	public ReorderColumnsAndGroupsCommand(ILayer layer, List<Integer> fromColumnPositions, int toColumnPositions) {
		super(layer, fromColumnPositions, toColumnPositions);
	}
}

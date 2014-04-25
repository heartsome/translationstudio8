package net.sourceforge.nattable.sort.command;

import net.sourceforge.nattable.command.AbstractColumnCommand;
import net.sourceforge.nattable.layer.ILayer;

public class SortColumnCommand extends AbstractColumnCommand {

	private boolean accumulate;
	
	public SortColumnCommand(ILayer layer, int columnPosition, boolean accumulate) {
		super(layer, columnPosition);
		this.accumulate = accumulate;
	}
	
	protected SortColumnCommand(SortColumnCommand command) {
		super(command);
		this.accumulate = command.accumulate;
	}
	
	public boolean isAccumulate() {
		return accumulate;
	}
	
	public SortColumnCommand cloneCommand() {
		return new SortColumnCommand(this);
	}
	
}

package net.sourceforge.nattable.edit.command;

import net.sourceforge.nattable.command.AbstractPositionCommand;
import net.sourceforge.nattable.layer.ILayer;

public class UpdateDataCommand extends AbstractPositionCommand {

	private Object newValue;

	public UpdateDataCommand(ILayer layer, int columnPosition, int rowPosition, Object newValue) {
		super(layer, columnPosition, rowPosition);
		this.newValue = newValue;
	}
	
	protected UpdateDataCommand(UpdateDataCommand command) {
		super(command);
		this.newValue = command.newValue;
	}
	
	public Object getNewValue() {
//		if (newValue instanceof String) {
//			String value = (String)newValue;
//			value = value.replaceAll("&", "&amp;");
//			value = value.replaceAll("<", "&lt;");
//			value = value.replaceAll(">", "&gt;");
//			this.newValue = value;
//		}
		return newValue;
	}
	
	public UpdateDataCommand cloneCommand() {
		return new UpdateDataCommand(this);
	}

}

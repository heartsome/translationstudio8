package net.heartsome.cat.ts.ui.xliffeditor.nattable.handler;

import net.sourceforge.nattable.command.AbstractPositionCommand;
import net.sourceforge.nattable.layer.ILayer;

public class UpdateDataAndAutoResizeCommand extends AbstractPositionCommand {

	private Object newValue;

	public UpdateDataAndAutoResizeCommand(ILayer layer, int columnPosition, int rowPosition, Object newValue) {
		super(layer, columnPosition, rowPosition);
		this.newValue = newValue;
	}
	
	protected UpdateDataAndAutoResizeCommand(UpdateDataAndAutoResizeCommand command) {
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
	
	public UpdateDataAndAutoResizeCommand cloneCommand() {
		return new UpdateDataAndAutoResizeCommand(this);
	}
	
	@Override
	public boolean convertToTargetLayer(ILayer targetLayer) {
		return true;
	}

}

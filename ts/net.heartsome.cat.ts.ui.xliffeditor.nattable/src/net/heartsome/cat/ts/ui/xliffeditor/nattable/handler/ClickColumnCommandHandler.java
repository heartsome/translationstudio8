package net.heartsome.cat.ts.ui.xliffeditor.nattable.handler;

import net.sourceforge.nattable.command.ILayerCommandHandler;
import net.sourceforge.nattable.layer.ILayer;
import net.sourceforge.nattable.selection.SelectionLayer;
import net.sourceforge.nattable.selection.command.SelectColumnCommand;

/**
 * @author weachy
 * @version
 * @since JDK1.5
 */
public class ClickColumnCommandHandler implements ILayerCommandHandler<SelectColumnCommand> {

	private final SelectionLayer selectionLayer;

	public ClickColumnCommandHandler(SelectionLayer selectionLayer) {
		this.selectionLayer = selectionLayer;
	}

	public boolean doCommand(ILayer targetLayer, SelectColumnCommand command) {
		if (command.convertToTargetLayer(selectionLayer)) {
			// TODO 点击列头时触发，后面可以在这里处理排序

			return true;
		}
		return false;
	}

	public Class<SelectColumnCommand> getCommandClass() {
		return SelectColumnCommand.class;
	}

}
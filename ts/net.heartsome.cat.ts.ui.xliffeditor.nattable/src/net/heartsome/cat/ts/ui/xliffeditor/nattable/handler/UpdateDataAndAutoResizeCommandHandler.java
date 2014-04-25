package net.heartsome.cat.ts.ui.xliffeditor.nattable.handler;

import java.text.MessageFormat;

import net.heartsome.cat.ts.ui.xliffeditor.nattable.resource.Messages;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.undoable.UpdateDataOperation;
import net.sourceforge.nattable.NatTable;
import net.sourceforge.nattable.command.AbstractLayerCommandHandler;
import net.sourceforge.nattable.layer.DataLayer;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.IOperationHistory;
import org.eclipse.core.commands.operations.OperationHistoryFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 修改后保存内容并自适应大小的处理 handler
 * @author weachy
 * @version 1.0
 * @since JDK1.5
 */
public class UpdateDataAndAutoResizeCommandHandler extends AbstractLayerCommandHandler<UpdateDataAndAutoResizeCommand> {

	private static final Logger LOGGER = LoggerFactory.getLogger(UpdateDataAndAutoResizeCommandHandler.class);

	private final NatTable table;

	private final DataLayer dataLayer;

	public UpdateDataAndAutoResizeCommandHandler(NatTable table, DataLayer dataLayer) {
		this.table = table;
		this.dataLayer = dataLayer;
	}

	@Override
	protected boolean doCommand(UpdateDataAndAutoResizeCommand command) {
		try {
			// int columnPosition = command.getColumnPosition();
			// int rowPosition = command.getRowPosition();
			// dataLayer.getDataProvider().setDataValue(columnPosition, rowPosition, command.getNewValue());
			// dataLayer.fireLayerEvent(new CellVisualChangeEvent(dataLayer, columnPosition, rowPosition));
			//
			// int currentRow = command.getRowPosition() + 1; // 修改行在当前一屏显示的几行中的相对位置
			// table.doCommand(new AutoResizeCurrentRowsCommand(table, new int[] { currentRow },
			// table.getConfigRegistry(), new GC(table)));

			IOperationHistory operationHistory = OperationHistoryFactory.getOperationHistory();
			operationHistory.execute(new UpdateDataOperation(table, dataLayer, command), null, null);

			return true;
		} catch (UnsupportedOperationException e) {
			LOGGER.error(
					MessageFormat.format(Messages.getString("handler.UpdateDataAndAutoResizeCommandHandler.logger1"),
							command.getNewValue()), e);
			e.printStackTrace(System.err);
			System.err.println("Failed to update value to: " + command.getNewValue());
		} catch (ExecutionException e) {
			LOGGER.error("", e);
			e.printStackTrace();
		}
		return false;
	}

	public Class<UpdateDataAndAutoResizeCommand> getCommandClass() {
		// TODO Auto-generated method stub
		return UpdateDataAndAutoResizeCommand.class;
	}
}

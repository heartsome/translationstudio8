package net.sourceforge.nattable.export.excel.command;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import net.sourceforge.nattable.command.AbstractLayerCommandHandler;
import net.sourceforge.nattable.export.excel.ExcelExporter;
import net.sourceforge.nattable.grid.layer.GridLayer;
import net.sourceforge.nattable.print.command.PrintEntireGridCommand;
import net.sourceforge.nattable.util.IClientAreaProvider;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.FileDialog;

public class ExportToExcelCommandHandler extends AbstractLayerCommandHandler<ExportToExcelCommand> {

	private final GridLayer gridLayer;

	public ExportToExcelCommandHandler(GridLayer gridLayer) {
		this.gridLayer = gridLayer;
	}

	@Override
	public boolean doCommand(ExportToExcelCommand command) {
		try {
			OutputStream outputStream = getOutputStream(command);
			if (outputStream == null) {
				return true;
			}

			ExcelExporter excelExporter = new ExcelExporter(gridLayer, command.getConfigRegistry());

			// This needs to be done so that the Grid can return all the cells
			// not just the ones visible in the viewport
			setClientAreaToMaximum();
			
			excelExporter.export(command.getShell(), outputStream, getMaximumLayerSize());
		} catch (IOException e) {
			throw new RuntimeException("Failed to export table to excel.", e);
		}
		return true;
	}

	/**
	 * Override this to plugin custom OutputStream.
	 */
	protected OutputStream getOutputStream(ExportToExcelCommand command) throws IOException {
		FileDialog dialog = new FileDialog (command.getShell(), SWT.SAVE);
		dialog.setFilterPath ("/");
		dialog.setOverwrite(true);

		dialog.setFileName ("table_export.xls");
		dialog.setFilterExtensions(new String [] {"Microsoft Office Excel Workbook(.xls)"});
		String fileName = dialog.open();
		if(fileName == null){
			return null;
		}
		return new PrintStream(fileName);
	}

	/**
	 * @return Position rectangle with the max layer size
	 */
	private Rectangle getMaximumLayerSize() {
		final int width = gridLayer.getWidth();
		final int height = gridLayer.getHeight();

		int lastRowPosition = gridLayer.getColumnPositionByX(width - 1);
		int lastColPosition = gridLayer.getRowPositionByY(height - 1);
		return new Rectangle(0, 0, lastRowPosition, lastColPosition);
	}

	private void setClientAreaToMaximum() {
		final Rectangle maxClientArea = new Rectangle(0, 0, gridLayer.getWidth(), gridLayer.getHeight());
		
		gridLayer.setClientAreaProvider(new IClientAreaProvider() {
			public Rectangle getClientArea() {
				return maxClientArea;
			}
		});
		
		gridLayer.doCommand(new PrintEntireGridCommand());
	}

	public Class<ExportToExcelCommand> getCommandClass() {
		return ExportToExcelCommand.class;
	}

}

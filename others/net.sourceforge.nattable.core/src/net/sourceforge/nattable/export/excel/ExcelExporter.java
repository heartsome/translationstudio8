package net.sourceforge.nattable.export.excel;

import static net.sourceforge.nattable.util.ObjectUtils.isNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import net.sourceforge.nattable.config.IConfigRegistry;
import net.sourceforge.nattable.layer.ILayer;
import net.sourceforge.nattable.layer.cell.LayerCell;
import net.sourceforge.nattable.print.command.TurnViewportOnCommand;
import net.sourceforge.nattable.style.CellStyleAttributes;
import net.sourceforge.nattable.style.CellStyleProxy;
import net.sourceforge.nattable.util.IClientAreaProvider;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Shell;

public class ExcelExporter {

	private static final Object DEFAULT = "";
	private static final String EXCEL_HEADER_FILE = "excelExportHeader.txt";
	private final ILayer layer;
	private final IConfigRegistry configRegistry;
	private final IClientAreaProvider originalClientAreaProvider;

	public ExcelExporter(ILayer layer, IConfigRegistry configRegistry) {
		this.layer = layer;
		this.configRegistry = configRegistry;
		originalClientAreaProvider = layer.getClientAreaProvider();
	}

	/**
	 * Create an HTML table suitable for Excel.
	 *
	 * @param outputStream
	 *            to write the output to
	 * @param positionRectangle
	 *            of the layer to export
	 * @throws IOException
	 */
	public void export(final Shell shell, final OutputStream outputStream, final Rectangle positionRectangle) throws IOException {
		final ExcelExportProgessBar progressBar = new ExcelExportProgessBar(shell);

		Runnable exporter = new Runnable() {
			public void run(){
				try {
					int startRow = positionRectangle.y;
					int endRow = startRow + positionRectangle.height;
					progressBar.open(startRow, endRow);

					writeHeader(outputStream);
					outputStream.write(asBytes("<body><table border='1'>"));

					for (int rowPosition = startRow; rowPosition <= endRow; rowPosition++) {
						progressBar.setSelection(rowPosition);
						outputStream.write(asBytes("<tr>\n"));

						int startColumn = positionRectangle.x;
						int endColumn = startColumn + positionRectangle.width;
						for (int colPosition = startColumn; colPosition <= endColumn; colPosition++) {
							LayerCell cell = layer.getCellByPosition(colPosition, rowPosition);
							outputStream.write(asBytes("\t" + getCellAsHTML(cell) + "\n"));
						}
						outputStream.write(asBytes("</tr>\n"));
					}
					outputStream.write(asBytes("</table></body></html>"));
				} catch (Exception e) {
					logError(e);
				} finally {
					try {
						outputStream.close();
					} catch (IOException e) {
						logError(e);
					}

					// These must be fired at the end of the thread execution
					layer.setClientAreaProvider(originalClientAreaProvider);
					layer.doCommand(new TurnViewportOnCommand());
					progressBar.dispose();
				}
			}
		};

		// Run with the SWT display so that the progress bar can paint
		shell.getDisplay().asyncExec(exporter);
	}

	private void logError(Exception e) {
		System.err.println("Excel Exporter failed: " + e.getMessage());
		e.printStackTrace(System.err);
	}

	private void writeHeader(OutputStream outputStream) throws IOException {
		InputStream headerStream = null;
		try {
			headerStream = this.getClass().getResourceAsStream(EXCEL_HEADER_FILE);
			int c;
			while ((c = headerStream.read()) != -1) {
				outputStream.write(c);
			}
		} catch (Exception e) {
			logError(e);
		} finally {
			if (isNotNull(headerStream)) {
				headerStream.close();
			}
		}
	}

	private String getCellAsHTML(LayerCell cell) {
		Object dataValue = cell.getDataValue();

		return String.format("<td %s>%s</td>",
		                     getStyleAsHtmlAttribute(cell),
		                     dataValue != null ? dataValue : DEFAULT);
	}

	private String getStyleAsHtmlAttribute(LayerCell cell) {
		CellStyleProxy cellStyle = new CellStyleProxy(configRegistry, cell.getDisplayMode(), cell.getConfigLabels().getLabels());
		Color fg = cellStyle.getAttributeValue(CellStyleAttributes.FOREGROUND_COLOR);
		Color bg = cellStyle.getAttributeValue(CellStyleAttributes.BACKGROUND_COLOR);
		Font font = cellStyle.getAttributeValue(CellStyleAttributes.FONT);

		return String.format("style='color: %s; background-color: %s; %s;'",
		                     getColorInCSSFormat(fg),
		                     getColorInCSSFormat(bg),
		                     getFontInCSSFormat(font));
	}

	private String getFontInCSSFormat(Font font) {
		FontData fontData = font.getFontData()[0];
		String fontName = fontData.getName();
		int fontStyle = fontData.getStyle();
		String HTML_STYLES[] = new String[] { "NORMAL", "BOLD", "ITALIC" };

		return String.format("font: %s; font-family: %s",
		                     fontStyle <= 2 ? HTML_STYLES[fontStyle] : HTML_STYLES[0],
		                     fontName);
	}

	private String getColorInCSSFormat(Color color) {
		return String.format("rgb(%d,%d,%d)",
		                     Integer.valueOf(color.getRed()),
		                     Integer.valueOf(color.getGreen()),
		                     Integer.valueOf(color.getBlue()));
	}

	private byte[] asBytes(String string) {
		return string.getBytes();
	}
}

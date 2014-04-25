package net.sourceforge.nattable.print;

import java.text.SimpleDateFormat;
import java.util.Date;

import net.sourceforge.nattable.config.IConfigRegistry;
import net.sourceforge.nattable.grid.layer.GridLayer;
import net.sourceforge.nattable.layer.ILayer;
import net.sourceforge.nattable.print.command.PrintEntireGridCommand;
import net.sourceforge.nattable.print.command.TurnViewportOnCommand;
import net.sourceforge.nattable.util.IClientAreaProvider;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Transform;
import org.eclipse.swt.printing.PrintDialog;
import org.eclipse.swt.printing.Printer;
import org.eclipse.swt.printing.PrinterData;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class GridLayerPrinter {

	private final IConfigRegistry configRegistry;
	private final ILayer gridLayer;
	private final IClientAreaProvider originalClientAreaProvider;
	public static final int FOOTER_HEIGHT_IN_PRINTER_DPI = 300;

	final SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, d MMM yyyy HH:mm a");
	private final String footerDate;

	public GridLayerPrinter(GridLayer gridLayer, IConfigRegistry configRegistry) {
		this.gridLayer = gridLayer;
		this.configRegistry = configRegistry;
		this.originalClientAreaProvider = gridLayer.getClientAreaProvider();
		this.footerDate = dateFormat.format(new Date());
	}

	/**
	 * Amount to scale the screen resolution by, to match the printer the
	 * resolution.
	 */
	private Point computeScaleFactor(Printer printer) {
		Point screenDPI = Display.getDefault().getDPI();
		Point printerDPI = printer.getDPI();

		int scaleFactorX = printerDPI.x / screenDPI.x;
		int scaleFactorY = printerDPI.y / screenDPI.y;
		return new Point(scaleFactorX, scaleFactorY);
	}

	/**
	 * Size of the grid to fit all the contents.
	 */
	private Rectangle getTotalGridArea() {
		return new Rectangle(0, 0, gridLayer.getWidth(), gridLayer.getHeight());
	}

	/**
	 * Calculate number of horizontal and vertical pages needed
	 * to print the entire grid.
	 */
	private Point getPageCount(Printer printer){
		Rectangle gridArea = getTotalGridArea();
		Rectangle printArea = computePrintArea(printer);
		Point scaleFactor = computeScaleFactor(printer);

		int numOfHorizontalPages = gridArea.width / (printArea.width / scaleFactor.x);
		int numOfVerticalPages = gridArea.height / (printArea.height / scaleFactor.y);

		// Adjusting for 0 index
		return new Point(numOfHorizontalPages + 1, numOfVerticalPages + 1);
	}

	public void print(final Shell shell) {
		final Printer printer = setupPrinter(shell);
		if (printer == null) {
			return;
		}
		setGridLayerSize(printer.getPrinterData());

		Display.getDefault().asyncExec(new Runnable() {

			public void run() {
				if (printer.startJob("NatTable")) {
					final Rectangle printerClientArea = computePrintArea(printer);
					final Point scaleFactor = computeScaleFactor(printer);
					final Point pageCount = getPageCount(printer);
					GC gc = new GC(printer);

					// Print pages Left to Right and then Top to Down
					int currentPage = 1;
					for (int verticalPageNumber = 0; verticalPageNumber < pageCount.y; verticalPageNumber++) {

						for (int horizontalPageNumber = 0; horizontalPageNumber < pageCount.x; horizontalPageNumber++) {

							// Calculate bounds for the next page
							Rectangle printBounds = new Rectangle((printerClientArea.width / scaleFactor.x) * horizontalPageNumber,
							                                      ((printerClientArea.height - FOOTER_HEIGHT_IN_PRINTER_DPI) / scaleFactor.y) * verticalPageNumber,
							                                      printerClientArea.width / scaleFactor.x,
							                                      (printerClientArea.height - FOOTER_HEIGHT_IN_PRINTER_DPI) / scaleFactor.y);

							if (shouldPrint(printer.getPrinterData(), currentPage)) {
								printer.startPage();

								Transform printerTransform = new Transform(printer);

								// Adjust for DPI difference between display and printer
								printerTransform.scale(scaleFactor.x, scaleFactor.y);

								// Adjust for margins
								printerTransform.translate(printerClientArea.x / scaleFactor.x, printerClientArea.y / scaleFactor.y);

								// Grid will nor automatically print the pages at the left margin.
								// Example: page 1 will print at x = 0, page 2 at x = 100, page 3 at x = 300
								// Adjust to print from the left page margin. i.e x = 0
								printerTransform.translate(-1 * printBounds.x, -1 * printBounds.y);
								gc.setTransform(printerTransform);

								printGrid(gc, printBounds);

								printFooter(gc, currentPage, printBounds);

								printer.endPage();
								printerTransform.dispose();
							}
							currentPage++;
						}
					}

					printer.endJob();

					gc.dispose();
					printer.dispose();
				}
				restoreGridLayerState();
			}

			private void printGrid(GC gc, Rectangle printBounds) {
				gridLayer.getLayerPainter().paintLayer(gridLayer, gc, 0, 0, printBounds, configRegistry);
			}

			private void printFooter(GC gc, int totalPageCount, Rectangle printBounds) {
				gc.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_BLACK));
				gc.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));

				gc.drawLine(printBounds.x,
				            printBounds.y + printBounds.height+10,
				            printBounds.x + printBounds.width,
				            printBounds.y + printBounds.height+10);

				gc.drawText("Page " + totalPageCount,
				            printBounds.x,
				            printBounds.y + printBounds.height + 15);

				// Approximate width of the date string: 140
				gc.drawText(footerDate,
				            printBounds.x + printBounds.width - 140,
				            printBounds.y + printBounds.height + 15);
			}
		});
	}

	/**
	 * Checks if a given page number should be printed.
	 * Page is allowed to print if:
	 * 	  User asked to print all pages or Page in a specified range
	 */
	private boolean shouldPrint(PrinterData printerData, int totalPageCount) {
		if(printerData.scope == PrinterData.PAGE_RANGE){
			return totalPageCount >= printerData.startPage && totalPageCount <= printerData.endPage;
		}
		return true;
	}

	private Printer setupPrinter(final Shell shell) {
		Printer defaultPrinter = new Printer();
		Point pageCount = getPageCount(defaultPrinter);
		defaultPrinter.dispose();

		final PrintDialog printDialog = new PrintDialog(shell);
		printDialog.setStartPage(1);
		printDialog.setEndPage(pageCount.x * pageCount.y);
		printDialog.setScope(PrinterData.ALL_PAGES);

		PrinterData printerData = printDialog.open();
		if(printerData == null){
			return null;
		}
		return new Printer(printerData);
	}

	/**
	 * Expand the client area of the grid such that
	 * all the contents fit in the viewport. This ensures that when the grid prints
	 * we print the <i>entire</i> table.
	 * @param printer
	 */
	private void setGridLayerSize(PrinterData printerData) {
		if (printerData.scope == PrinterData.SELECTION) {
			gridLayer.setClientAreaProvider(originalClientAreaProvider);
			return;
		}

		final Rectangle fullGridSize = getTotalGridArea();

		gridLayer.setClientAreaProvider(new IClientAreaProvider(){
			public Rectangle getClientArea() {
				return fullGridSize;
			}
		});

		gridLayer.doCommand(new PrintEntireGridCommand());
	}

	private void restoreGridLayerState() {
		gridLayer.setClientAreaProvider(originalClientAreaProvider);
		gridLayer.doCommand(new TurnViewportOnCommand());
	}

	  /**
	   * Computes the print area, including margins
	   */
	  private static Rectangle computePrintArea(Printer printer) {
	    // Get the printable area
	    Rectangle rect = printer.getClientArea();

	    // Compute the trim
	    Rectangle trim = printer.computeTrim(0, 0, 0, 0);

	    // Get the printer's DPI
	    Point dpi = printer.getDPI();
	    dpi.x = dpi.x / 2;
	    dpi.y = dpi.y / 2;

	    // Calculate the printable area, using 1 inch margins
	    int left = trim.x + dpi.x;
	    if (left < rect.x) left = rect.x;

	    int right = (rect.width + trim.x + trim.width) - dpi.x;
	    if (right > rect.width) right = rect.width;

	    int top = trim.y + dpi.y;
	    if (top < rect.y) top = rect.y;

	    int bottom = (rect.height + trim.y + trim.height) - dpi.y;
	    if (bottom > rect.height) bottom = rect.height;

	    return new Rectangle(left, top, right - left, bottom - top);
	  }


}
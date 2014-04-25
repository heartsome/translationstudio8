package net.sourceforge.nattable.viewport;

import java.util.ArrayList;
import java.util.List;

import net.sourceforge.nattable.command.ILayerCommand;
import net.sourceforge.nattable.coordinate.PositionCoordinate;
import net.sourceforge.nattable.coordinate.Range;
import net.sourceforge.nattable.grid.command.ClientAreaResizeCommand;
import net.sourceforge.nattable.layer.AbstractLayerTransform;
import net.sourceforge.nattable.layer.ILayer;
import net.sourceforge.nattable.layer.IUniqueIndexLayer;
import net.sourceforge.nattable.layer.LayerUtil;
import net.sourceforge.nattable.layer.event.ILayerEvent;
import net.sourceforge.nattable.layer.event.IStructuralChangeEvent;
import net.sourceforge.nattable.print.command.PrintEntireGridCommand;
import net.sourceforge.nattable.print.command.TurnViewportOffCommand;
import net.sourceforge.nattable.print.command.TurnViewportOnCommand;
import net.sourceforge.nattable.selection.ScrollSelectionCommandHandler;
import net.sourceforge.nattable.selection.SelectionLayer;
import net.sourceforge.nattable.selection.command.MoveSelectionCommand;
import net.sourceforge.nattable.selection.command.ScrollSelectionCommand;
import net.sourceforge.nattable.selection.event.CellSelectionEvent;
import net.sourceforge.nattable.selection.event.ColumnSelectionEvent;
import net.sourceforge.nattable.selection.event.RowSelectionEvent;
import net.sourceforge.nattable.viewport.command.RecalculateScrollBarsCommandHandler;
import net.sourceforge.nattable.viewport.command.ShowCellInViewportCommandHandler;
import net.sourceforge.nattable.viewport.command.ShowColumnInViewportCommandHandler;
import net.sourceforge.nattable.viewport.command.ShowRowInViewportCommandHandler;
import net.sourceforge.nattable.viewport.command.ViewportSelectColumnCommandHandler;
import net.sourceforge.nattable.viewport.command.ViewportSelectRowCommandHandler;
import net.sourceforge.nattable.viewport.event.ScrollEvent;
import net.sourceforge.nattable.viewport.event.ViewportEventHandler;

import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.ScrollBar;

/**
 * Viewport - the visible area of NatTable
 * Places a 'viewport' over the table. Introduces scroll bars over the table and
 * keeps them in sync with the data being displayed. This is typically placed over the
 * {@link SelectionLayer}.
 */
public class ViewportLayer extends AbstractLayerTransform implements IUniqueIndexLayer {

	private HorizontalScrollBarHandler hBarListener;
	private VerticalScrollBarHandler vBarListener;
	private final IUniqueIndexLayer scrollableLayer;

	// The viewport origin, in scrollable position coordinates.
	private final PositionCoordinate origin = new PositionCoordinate(this, 0, 0);
	private final PositionCoordinate minimumOrigin = new PositionCoordinate(this, 0, 0);
	private boolean viewportOff = false;
	private int viewportOffOriginCol, viewportOnOriginRow = 0;

	// Cache
	private List<Integer> cachedColumnIndexOrder;
	private List<Integer> cachedRowIndexOrder;
	private int cachedClientAreaWidth = 0;
	private int cachedClientAreaHeight = 0;
	private int cachedWidth = -1;
	private int cachedHeight = -1;

	public ViewportLayer(IUniqueIndexLayer underlyingLayer) {
		super(underlyingLayer);
		this.scrollableLayer = underlyingLayer;

		registerCommandHandler(new RecalculateScrollBarsCommandHandler(this));
		registerCommandHandler(new ScrollSelectionCommandHandler(this));
		registerCommandHandler(new ShowCellInViewportCommandHandler(this));
		registerCommandHandler(new ShowColumnInViewportCommandHandler(this));
		registerCommandHandler(new ShowRowInViewportCommandHandler(this));
		registerCommandHandler(new ViewportSelectColumnCommandHandler(this));
		registerCommandHandler(new ViewportSelectRowCommandHandler(this));

		registerEventHandler(new ViewportEventHandler(this));
	}

	// Origin

	public int getMinimumOriginColumnPosition() {
		return minimumOrigin.columnPosition;
	}

	public void setMinimumOriginColumnPosition(int minColumnPosition) {
		int previousOriginColumnPosition = origin.columnPosition;

		if (previousOriginColumnPosition == minimumOrigin.columnPosition || getOriginColumnPosition() < minColumnPosition) {
			minimumOrigin.columnPosition = minColumnPosition;
			setOriginColumnPosition(minColumnPosition);
		} else {
			setOriginColumnPosition(getOriginColumnPosition() + minColumnPosition - minimumOrigin.columnPosition);
			minimumOrigin.columnPosition = minColumnPosition;
		}

		if (origin.columnPosition != previousOriginColumnPosition) {
			invalidateHorizontalStructure();
		}

		recalculateHorizontalScrollBar();
	}

	public int getMinimumOriginRowPosition() {
		return minimumOrigin.rowPosition;
	}

	public void setMinimumOriginRowPosition(int minRowPosition) {
		int previousOriginRowPosition = origin.rowPosition;

		if (getOriginRowPosition() < minRowPosition) {
			setOriginRowPosition(minRowPosition);
		} else {
			setOriginRowPosition(getOriginRowPosition() + minRowPosition - minimumOrigin.rowPosition);
		}

		minimumOrigin.rowPosition = minRowPosition;

		if (origin.rowPosition != previousOriginRowPosition) {
			invalidateVerticalStructure();
		}

		recalculateVerticalScrollBar();
	}

	public void setMinimumOriginPosition(int minColumnPosition, int minRowPosition) {
		setMinimumOriginColumnPosition(minColumnPosition);
		setMinimumOriginRowPosition(minRowPosition);
	}

	public int getOriginColumnPosition() {
		return viewportOff ? minimumOrigin.columnPosition : origin.columnPosition;
	}

	public void setOriginColumnPosition(int scrollableColumnPosition) {
		if (scrollableColumnPosition < minimumOrigin.columnPosition) {
			scrollableColumnPosition = minimumOrigin.columnPosition;
		}
		if (scrollableColumnPosition >= getUnderlyingLayer().getColumnCount()) {
			scrollableColumnPosition = getUnderlyingLayer().getColumnCount() - 1;
		}

		int originalOriginColumnPosition = getOriginColumnPosition();
		origin.columnPosition = scrollableColumnPosition;
		int adjustedOriginColumnPosition = adjustColumnOrigin();

		if (adjustedOriginColumnPosition != originalOriginColumnPosition && getUnderlyingLayer().getColumnIndexByPosition(scrollableColumnPosition) >= 0) {
			invalidateHorizontalStructure();
			origin.columnPosition = adjustedOriginColumnPosition;
			fireScrollEvent();
		}
	}

	public int getOriginRowPosition() {
		return viewportOff ? minimumOrigin.rowPosition : origin.rowPosition;
	}

	public void setOriginRowPosition(int scrollableRowPosition) {
		if (scrollableRowPosition < minimumOrigin.rowPosition) {
			scrollableRowPosition = minimumOrigin.rowPosition;
		}

		int originalOriginRowPosition = getOriginRowPosition();
		origin.rowPosition = scrollableRowPosition;
		int adjustedOriginRowPosition = adjustRowOrigin();

		if (adjustedOriginRowPosition != originalOriginRowPosition && getUnderlyingLayer().getRowIndexByPosition(scrollableRowPosition) >= 0) {
			invalidateVerticalStructure();
			origin.rowPosition = adjustedOriginRowPosition;
			fireScrollEvent();
		}
	}

	public void resetOrigin() {
		int previousOriginColumnPosition = origin.columnPosition;
		int previousOriginRowPosition = origin.rowPosition;

		minimumOrigin.columnPosition = 0;
		origin.columnPosition = 0;

		minimumOrigin.rowPosition = 0;
		origin.rowPosition = 0;

		if (origin.columnPosition != previousOriginColumnPosition) {
			invalidateHorizontalStructure();
		}
		if (origin.rowPosition != previousOriginRowPosition) {
			invalidateVerticalStructure();
		}
	}

	// Horizontal features

	// Columns

	/**
	 * @return <i>visible</i> column count
	 *    Note: This takes care of the frozen columns
	 */
	@Override
	public int getColumnCount() {
		if (viewportOff) {
			return scrollableLayer.getColumnCount() - minimumOrigin.columnPosition;
		} else {
			return getColumnIndexes().size();
		}
	}

	public int getColumnPositionByIndex(int columnIndex) {
		return scrollableLayer.getColumnPositionByIndex(columnIndex) - getOriginColumnPosition();
	}

	@Override
	public int localToUnderlyingColumnPosition(int localColumnPosition) {
		return getOriginColumnPosition() + localColumnPosition;
	}

	@Override
	public int underlyingToLocalColumnPosition(ILayer sourceUnderlyingLayer, int underlyingColumnPosition) {
		if (sourceUnderlyingLayer != getUnderlyingLayer()) {
			return -1;
		}

		return underlyingColumnPosition - getOriginColumnPosition();
	}

	private List<Integer> getColumnIndexes() {
		if (cachedColumnIndexOrder == null) {
			int availableWidth = getClientAreaWidth();
			if (availableWidth >= 0) {
				if (getOriginColumnPosition() < minimumOrigin.columnPosition) {
					origin.columnPosition = minimumOrigin.columnPosition;
				}

				recalculateAvailableWidthAndColumnIndexes();
			}
		}

		return cachedColumnIndexOrder;
	}

	// Width

	/**
	 * @return the width of the total number of visible columns
	 */
	@Override
	public int getWidth() {
		if (viewportOff) {
			return scrollableLayer.getWidth() - scrollableLayer.getStartXOfColumnPosition(minimumOrigin.columnPosition);
		}
		if (cachedWidth < 0) {
			recalculateAvailableWidthAndColumnIndexes();
		}
		return cachedWidth;
	}

	// Column resize

	@Override
	public boolean isColumnPositionResizable(int columnPosition) {
		return getUnderlyingLayer().isColumnPositionResizable(getOriginColumnPosition() + columnPosition);
	}

	// X

	@Override
	public int getColumnPositionByX(int x) {
		int originX = getUnderlyingLayer().getStartXOfColumnPosition(getOriginColumnPosition());
		return getUnderlyingLayer().getColumnPositionByX(originX + x) - getOriginColumnPosition();
	}

	@Override
	public int getStartXOfColumnPosition(int columnPosition) {
		return getUnderlyingLayer().getStartXOfColumnPosition(getOriginColumnPosition() + columnPosition) - getUnderlyingLayer().getStartXOfColumnPosition(getOriginColumnPosition());
	}

	// Vertical features

	// Rows

	/**
	 * @return total number of rows visible in the viewport
	 */
	@Override
	public int getRowCount() {
		if (viewportOff) {
			return scrollableLayer.getRowCount() - minimumOrigin.rowPosition;
		}
		return getRowIndexes().size();
	}

	public int getRowPositionByIndex(int rowIndex) {
		return scrollableLayer.getRowPositionByIndex(rowIndex) - getOriginRowPosition();
	}

	@Override
	public int localToUnderlyingRowPosition(int localRowPosition) {
		return getOriginRowPosition() + localRowPosition;
	}

	@Override
	public int underlyingToLocalRowPosition(ILayer sourceUnderlyingLayer, int underlyingRowPosition) {
		if (sourceUnderlyingLayer != getUnderlyingLayer()) {
			return -1;
		}

		return underlyingRowPosition - getOriginRowPosition();
	}
	
	private List<Integer> getRowIndexes() {
		if (cachedRowIndexOrder == null) {
			int availableHeight = getClientAreaHeight();
			if (availableHeight >= 0) {
				if (getOriginRowPosition() < minimumOrigin.rowPosition) {
					origin.rowPosition = minimumOrigin.rowPosition;
				}
				recalculateAvailableHeightAndRowIndexes();
			}
		}

		return cachedRowIndexOrder;
	}

	// Height

	@Override
	public int getHeight() {
		if (viewportOff) {
			return scrollableLayer.getHeight() - scrollableLayer.getStartYOfRowPosition(minimumOrigin.rowPosition);
		}
		if (cachedHeight < 0) {
			recalculateAvailableHeightAndRowIndexes();
		}
		return cachedHeight;
	}

	// Row resize

	// Y

	@Override
	public int getRowPositionByY(int y) {
		int originY = getUnderlyingLayer().getStartYOfRowPosition(getOriginRowPosition());
		return getUnderlyingLayer().getRowPositionByY(originY + y) - getOriginRowPosition();
	}

	@Override
	public int getStartYOfRowPosition(int rowPosition) {
		return getUnderlyingLayer().getStartYOfRowPosition(getOriginRowPosition() + rowPosition) - getUnderlyingLayer().getStartYOfRowPosition(getOriginRowPosition());
	}

	// Cell features

	@Override
	public Rectangle getBoundsByPosition(int columnPosition, int rowPosition) {
		int underlyingColumnPosition = localToUnderlyingColumnPosition(columnPosition);
		int underlyingRowPosition = localToUnderlyingRowPosition(rowPosition);
		Rectangle bounds = getUnderlyingLayer().getBoundsByPosition(underlyingColumnPosition, underlyingRowPosition);
		bounds.x -= getUnderlyingLayer().getStartXOfColumnPosition(getOriginColumnPosition());
		bounds.y -= getUnderlyingLayer().getStartYOfRowPosition(getOriginRowPosition());
		return bounds;
	}

	/**
	 * Clear horizontal caches
	 */
	public void invalidateHorizontalStructure() {
		cachedColumnIndexOrder = null;
		cachedClientAreaWidth = 0;
		cachedWidth = -1;
	}

	/**
	 * Clear vertical caches
	 */
	public void invalidateVerticalStructure() {
		cachedRowIndexOrder = null;
		cachedClientAreaHeight = 0;
		cachedHeight = -1;
	}

	/**
	 * This method will add the indexes of the column which fit in the available
	 * view port width. Every time a column is added, the available width is
	 * reduced by the width of the added column.
	 *
	 * @param availableWidth
	 * @param displayableColumns
	 *            all indexes
	 * @param columnIndex
	 *            to try and add to the displayable columns
	 * @return
	 */
	protected void recalculateAvailableWidthAndColumnIndexes() {
		int availableWidth = getClientAreaWidth();
		ILayer underlyingLayer = getUnderlyingLayer();

		cachedWidth = 0;
		cachedColumnIndexOrder = new ArrayList<Integer>();

		for (int columnPosition = getOriginColumnPosition(); columnPosition < underlyingLayer.getColumnCount() && availableWidth > 0; columnPosition++) {
			int columnIndex = underlyingLayer.getColumnIndexByPosition(columnPosition);
			int width = underlyingLayer.getColumnWidthByPosition(columnPosition);
			availableWidth -= width;
			cachedWidth += width;
			cachedColumnIndexOrder.add(Integer.valueOf(columnIndex));
		}

		int lastColumnPosition = underlyingLayer.getColumnCount() - 1;
		if (origin.columnPosition > lastColumnPosition) {
			origin.columnPosition = lastColumnPosition;
		}
	}

	protected void recalculateAvailableHeightAndRowIndexes() {
		int availableHeight = getClientAreaHeight();
		ILayer underlyingLayer = getUnderlyingLayer();

		cachedHeight = 0;
		cachedRowIndexOrder = new ArrayList<Integer>();

		for (int currentPosition = getOriginRowPosition(); currentPosition < underlyingLayer.getRowCount() && availableHeight > 0; currentPosition++) {
			int rowIndex = underlyingLayer.getRowIndexByPosition(currentPosition);
			int height = underlyingLayer.getRowHeightByPosition(rowIndex);
			availableHeight -= height;
			cachedHeight += height;
			cachedRowIndexOrder.add(Integer.valueOf(rowIndex));
		}

		int lastRowPosition = underlyingLayer.getRowCount() - 1;
		if (origin.rowPosition > lastRowPosition) {
			origin.rowPosition = (lastRowPosition < 0) ? 0 : lastRowPosition;
		}
	}

	/**
	 * Srcolls the table so that the specified cell is visible i.e. in the Viewport
	 * @param scrollableColumnPosition
	 * @param scrollableRowPosition
	 * @param forceEntireCellIntoViewport
	 */
	public void moveCellPositionIntoViewport(int scrollableColumnPosition, int scrollableRowPosition, boolean forceEntireCellIntoViewport) {
		moveColumnPositionIntoViewport(scrollableColumnPosition, forceEntireCellIntoViewport);
		moveRowPositionIntoViewport(scrollableRowPosition, forceEntireCellIntoViewport);
	}

	/**
	 * Scrolls the viewport (if required) so that the specified column is visible.
	 * @param scrollableColumnPosition column position in terms of the Scrollable Layer
	 */
	public void moveColumnPositionIntoViewport(int scrollableColumnPosition, boolean forceEntireCellIntoViewport) {
		ILayer underlyingLayer = getUnderlyingLayer();
		if (underlyingLayer.getColumnIndexByPosition(scrollableColumnPosition) >= 0) {
			if (scrollableColumnPosition >= getMinimumOriginColumnPosition()) {
				int originColumnPosition = getOriginColumnPosition();

				if (scrollableColumnPosition < originColumnPosition) {
					// Move left
					setOriginColumnPosition(scrollableColumnPosition);
				} else {
					int scrollableColumnStartX = underlyingLayer.getStartXOfColumnPosition(scrollableColumnPosition);
					int scrollableColumnEndX = scrollableColumnStartX + underlyingLayer.getColumnWidthByPosition(scrollableColumnPosition);
					int clientAreaWidth = getClientAreaWidth();
					int viewportEndX = underlyingLayer.getStartXOfColumnPosition(getOriginColumnPosition()) + clientAreaWidth;

					if (viewportEndX < scrollableColumnEndX) {
						int targetOriginColumnPosition;
						if (forceEntireCellIntoViewport || isLastColumn(scrollableColumnPosition)) {
							targetOriginColumnPosition = underlyingLayer.getColumnPositionByX(scrollableColumnEndX - clientAreaWidth) + 1;
						} else {
							targetOriginColumnPosition = underlyingLayer.getColumnPositionByX(scrollableColumnStartX - clientAreaWidth) + 1;
						}

						// Move right
						setOriginColumnPosition(targetOriginColumnPosition);
					}
				}
			}
		}
	}

	/**
	 * @see {@link #moveColumnPositionIntoViewport(int, boolean)}
	 */
	public void moveRowPositionIntoViewport(int scrollableRowPosition, boolean forceEntireCellIntoViewport) {
		ILayer underlyingLayer = getUnderlyingLayer();
		if (underlyingLayer.getRowIndexByPosition(scrollableRowPosition) >= 0) {
			if (scrollableRowPosition >= getMinimumOriginRowPosition()) {
				int originRowPosition = getOriginRowPosition();

				if (scrollableRowPosition < originRowPosition) {
					// Move up
					setOriginRowPosition(scrollableRowPosition);
				} else {
					int scrollableRowStartY = underlyingLayer.getStartYOfRowPosition(scrollableRowPosition);
					int scrollableRowEndY = scrollableRowStartY + underlyingLayer.getRowHeightByPosition(scrollableRowPosition);
					int clientAreaHeight = getClientAreaHeight();
					int viewportEndY = underlyingLayer.getStartYOfRowPosition(getOriginRowPosition()) + clientAreaHeight;

					if (viewportEndY < scrollableRowEndY) {
						int targetOriginRowPosition;
						if (forceEntireCellIntoViewport || isLastRow(scrollableRowPosition)) {
							targetOriginRowPosition = underlyingLayer.getRowPositionByY(scrollableRowEndY - clientAreaHeight) + 1;
						} else {
							targetOriginRowPosition = underlyingLayer.getRowPositionByY(scrollableRowStartY - clientAreaHeight) + 1;
						}

						// Move down
						setOriginRowPosition(targetOriginRowPosition);
					}
				}
			}
		}
	}

	private boolean isLastColumn(int scrollableColumnPosition) {
		return scrollableColumnPosition == getUnderlyingLayer().getColumnCount()-1;
	}

	private boolean isLastRow(int scrollableRowPosition) {
		return scrollableRowPosition == getUnderlyingLayer().getRowCount()-1;
	}

	private void fireScrollEvent() {
		fireLayerEvent(new ScrollEvent(this));
	}

	@Override
	public boolean doCommand(ILayerCommand command) {
		if (command instanceof ClientAreaResizeCommand && command.convertToTargetLayer(this)) {
			ClientAreaResizeCommand clientAreaResizeCommand = (ClientAreaResizeCommand) command;
			ScrollBar hBar = clientAreaResizeCommand.getScrollable().getHorizontalBar();
			ScrollBar vBar = clientAreaResizeCommand.getScrollable().getVerticalBar();

			if (hBarListener == null) {
				hBarListener = new HorizontalScrollBarHandler(this, hBar);
			}
			if (vBarListener == null) {
				vBarListener = new VerticalScrollBarHandler(this, vBar);
			}

			handleGridResize();

			return true;
		} else if (command instanceof TurnViewportOffCommand) {
			viewportOffOriginCol = localToUnderlyingColumnPosition(0);
			viewportOnOriginRow = localToUnderlyingRowPosition(0);
			viewportOff = true;
			return true;
		} else if (command instanceof TurnViewportOnCommand) {
			viewportOff = false;
			setOriginColumnPosition(viewportOffOriginCol);
			setOriginRowPosition(viewportOnOriginRow);
			return true;
		} else if (command instanceof PrintEntireGridCommand) {
			moveCellPositionIntoViewport(0, 0, false);
		}
		return super.doCommand(command);
	}

	private void recalculateHorizontalScrollBar() {
		if (hBarListener != null) {
			hBarListener.recalculateScrollBarSize();
		}
	}

	private void recalculateVerticalScrollBar() {
		if (vBarListener != null) {
			vBarListener.recalculateScrollBarSize();
		}
	}

	public void recalculateScrollBars() {
		recalculateHorizontalScrollBar();
		recalculateVerticalScrollBar();
	}

	protected void handleGridResize() {
		setOriginColumnPosition(origin.columnPosition);
		recalculateHorizontalScrollBar();
		setOriginRowPosition(origin.rowPosition);
		recalculateVerticalScrollBar();
	}

	/**
	 * @see #adjustRowOrigin()
	 */
	protected int adjustColumnOrigin() {
		if (getColumnCount() == 0) {
			return 0;
		}

		int availableWidth = getClientAreaWidth() - getWidth();
		if (availableWidth < 0) {
			return getOriginColumnPosition();
		}

		int originColumnPosition = getOriginColumnPosition();
		int previousColPosition = LayerUtil.convertColumnPosition(this, 0, scrollableLayer) - 1;

		while (previousColPosition >= 0) {
			int previousColWidth = getUnderlyingLayer().getColumnWidthByPosition(previousColPosition);

			if (availableWidth >= previousColWidth && originColumnPosition - 1 >= minimumOrigin.columnPosition) {
				originColumnPosition--;
				availableWidth -= previousColWidth;
			} else {
				break;
			}
			previousColPosition--;
		}
		return originColumnPosition;
	}

	/**
	 * If the client area size is greater than the content size,
	 *    calculate number of rows to add to viewport i.e move the origin
	 */
	protected int adjustRowOrigin() {
		if (getRowCount() == 0) {
			return 0;
		}

		int availableHeight = getClientAreaHeight() - getHeight();
		if (availableHeight < 0) {
			return getOriginRowPosition();
		}

		int originRowPosition = getOriginRowPosition();
		int previousRowPosition = LayerUtil.convertRowPosition(this, 0, scrollableLayer) - 1;

		// Can we fit another row ?
		while (previousRowPosition >= 0) {
			int previousRowHeight = getUnderlyingLayer().getRowHeightByPosition(previousRowPosition);

			if (availableHeight >= previousRowHeight && originRowPosition - 1 >= minimumOrigin.rowPosition) {
				originRowPosition--;
				availableHeight -= previousRowHeight;
			} else {
				break;
			}
			previousRowPosition--;
		}
		return originRowPosition;
	}

	public void scrollVerticallyByAPage(ScrollSelectionCommand scrollSelectionCommand) {
		getUnderlyingLayer().doCommand(scrollVerticallyByAPageCommand(scrollSelectionCommand));
	}

	protected MoveSelectionCommand scrollVerticallyByAPageCommand(ScrollSelectionCommand scrollSelectionCommand) {
		return new MoveSelectionCommand(scrollSelectionCommand.getDirection(),
										getRowCount(),
										scrollSelectionCommand.isShiftMask(),
										scrollSelectionCommand.isControlMask());
	}

	protected boolean isLastColumnCompletelyDisplayed() {
		int lastDisplayableColumnIndex = getUnderlyingLayer().getColumnIndexByPosition(getUnderlyingLayer().getColumnCount() - 1);
		int visibleColumnCount = getColumnCount();
		int lastVisibleColumnIndex = getColumnIndexByPosition(visibleColumnCount - 1);

		return (lastVisibleColumnIndex == lastDisplayableColumnIndex) && (getClientAreaWidth() >= getWidth());
	}

	protected boolean isLastRowCompletelyDisplayed() {
		int lastDisplayableRowIndex = getUnderlyingLayer().getRowIndexByPosition(getUnderlyingLayer().getRowCount() - 1);
		int visibleRowCount = getRowCount();
		int lastVisibleRowIndex = getRowIndexByPosition(visibleRowCount - 1);

		return (lastVisibleRowIndex == lastDisplayableRowIndex) && (getClientAreaHeight() >= getHeight());
	}

	// Event handling

	@Override
	public void handleLayerEvent(ILayerEvent event) {
		if (event instanceof IStructuralChangeEvent) {
			IStructuralChangeEvent structuralChangeEvent = (IStructuralChangeEvent) event;
			if (structuralChangeEvent.isHorizontalStructureChanged()) {
				invalidateHorizontalStructure();
			}
			if (structuralChangeEvent.isVerticalStructureChanged()) {
				invalidateVerticalStructure();
			}
		}

		if (event instanceof CellSelectionEvent) {
			processSelection((CellSelectionEvent) event);
		} else if (event instanceof ColumnSelectionEvent) {
			processColumnSelection((ColumnSelectionEvent) event);
		} else if (event instanceof RowSelectionEvent) {
			processRowSelection((RowSelectionEvent) event);
		}

		super.handleLayerEvent(event);
	}

	/**
	 * Handle the {@link CellSelectionEvent}
	 * @param selectionEvent
	 */
	private void processSelection(CellSelectionEvent selectionEvent) {
		moveCellPositionIntoViewport(selectionEvent.getColumnPosition(), selectionEvent.getRowPosition(), selectionEvent.isForcingEntireCellIntoViewport());
		adjustHorizontalScrollBar();
		adjustVerticalScrollBar();
	}

	private void processColumnSelection(ColumnSelectionEvent selectionEvent) {
		for (Range columnPositionRange : selectionEvent.getColumnPositionRanges()) {
			moveColumnPositionIntoViewport(columnPositionRange.end - 1, false);
			adjustHorizontalScrollBar();
		}
	}

	private void processRowSelection(RowSelectionEvent selectionEvent) {
		for (Range columnPositionRange : selectionEvent.getRowPositionRanges()) {
			moveRowPositionIntoViewport(columnPositionRange.end - 1, false);
		}
		adjustVerticalScrollBar();
	}

	private void adjustHorizontalScrollBar() {
		if (hBarListener != null) {
			hBarListener.adjustScrollBar();
		}
	}

	private void adjustVerticalScrollBar() {
		if (vBarListener != null) {
			vBarListener.adjustScrollBar();
		}
	}

	// Accessors

	public int getClientAreaWidth() {
		int clientAreaWidth = getClientAreaProvider().getClientArea().width;
		if (clientAreaWidth != cachedClientAreaWidth) {
			invalidateHorizontalStructure();
			cachedClientAreaWidth = clientAreaWidth;
		}
		return cachedClientAreaWidth;
	}

	public int getClientAreaHeight() {
		int clientAreaHeight = getClientAreaProvider().getClientArea().height;
		if (clientAreaHeight != cachedClientAreaHeight) {
			invalidateVerticalStructure();
			cachedClientAreaHeight = clientAreaHeight;
		}
		return cachedClientAreaHeight;
	}

	public SelectionLayer getSelectionLayer() {
		return (SelectionLayer) getUnderlyingLayer();
	}

	public IUniqueIndexLayer getScrollableLayer() {
		return scrollableLayer;
	}

	@Override
	public String toString() {
		return "Viewport Layer";
	}

}

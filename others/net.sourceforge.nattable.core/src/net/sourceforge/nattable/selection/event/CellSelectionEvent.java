package net.sourceforge.nattable.selection.event;

import net.sourceforge.nattable.layer.event.CellVisualChangeEvent;
import net.sourceforge.nattable.selection.SelectionLayer;

public class CellSelectionEvent extends CellVisualChangeEvent implements ISelectionEvent {

	private final SelectionLayer selectionLayer;
	private boolean forcingEntireCellIntoViewport = false;

	// The state of the keys when the event was raised
	private boolean withShiftMask = false;
	private boolean withControlMask = false;

	public CellSelectionEvent(SelectionLayer selectionLayer, int columnPosition, int rowPosition,
			boolean forcingEntireCellIntoViewport, boolean withShiftMask, boolean withControlMask) {
		super(selectionLayer, columnPosition, rowPosition);
		this.selectionLayer = selectionLayer;
		this.forcingEntireCellIntoViewport = forcingEntireCellIntoViewport;
		this.withControlMask = withControlMask;
		this.withShiftMask = withShiftMask;
	}

	// Copy constructor
	protected CellSelectionEvent(CellSelectionEvent event) {
		super(event);
		this.selectionLayer = event.selectionLayer;
		this.forcingEntireCellIntoViewport = event.forcingEntireCellIntoViewport;
		this.withControlMask = event.withControlMask;
		this.withShiftMask = event.withShiftMask;
	}

	public SelectionLayer getSelectionLayer() {
		return selectionLayer;
	}

	public boolean isForcingEntireCellIntoViewport() {
		return forcingEntireCellIntoViewport;
	}

	@Override
	public CellSelectionEvent cloneEvent() {
		return new CellSelectionEvent(this);
	}

	public boolean isWithShiftMask() {
		return withShiftMask;
	}

	public boolean isWithControlMask() {
		return withControlMask;
	}

}
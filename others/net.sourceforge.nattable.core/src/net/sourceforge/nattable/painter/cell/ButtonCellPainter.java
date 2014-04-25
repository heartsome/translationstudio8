package net.sourceforge.nattable.painter.cell;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import net.sourceforge.nattable.NatTable;
import net.sourceforge.nattable.config.IConfigRegistry;
import net.sourceforge.nattable.layer.ILayer;
import net.sourceforge.nattable.layer.cell.LayerCell;
import net.sourceforge.nattable.layer.event.CellVisualChangeEvent;
import net.sourceforge.nattable.painter.cell.decorator.BeveledBorderDecorator;
import net.sourceforge.nattable.ui.NatEventData;
import net.sourceforge.nattable.ui.action.IMouseAction;

import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;

/**
 * Paints a button and simulates a button click. It also notifies its listeners when it is clicked.  
 */
public class ButtonCellPainter implements ICellPainter, IMouseAction {
	private final ICellPainter buttonRaisedPainter;
	private final ICellPainter buttonPressedPainter;

	private int buttonFlashTime = 150;

	private int columnPosClicked;
	private int rowPosClicked;
	private boolean recentlyClicked;
	private final List<IMouseAction> clickLiseners = new ArrayList<IMouseAction>();

	/**
	 * @param interiorPainter to paint the contents of the cell. 
	 * 	This will be decorated with a button like look and feel. 
	 */
	public ButtonCellPainter(ICellPainter interiorPainter) {
		this.buttonPressedPainter = interiorPainter;
		this.buttonRaisedPainter = new BeveledBorderDecorator(interiorPainter);
	}

	/**
	 * @param buttonRaisedPainter cell painter to use for painting the button raised state.
	 * @param buttonPressedPainter cell painter to use for painting the button pressed state.
	 */
	public ButtonCellPainter(ICellPainter buttonRaisedPainter, ICellPainter buttonPressedPainter) {
		this.buttonRaisedPainter = buttonRaisedPainter;
		this.buttonPressedPainter = buttonPressedPainter;
	}

	public void paintCell(final LayerCell cell, final GC gc, final Rectangle bounds, final IConfigRegistry configRegistry) {
		if (recentlyClicked && columnPosClicked == cell.getColumnPosition() && rowPosClicked == cell.getRowPosition()){
			buttonPressedPainter.paintCell(cell, gc, bounds, configRegistry);
		} else {
			buttonRaisedPainter.paintCell(cell, gc, bounds, configRegistry);
		}
	}

	public int getPreferredHeight(LayerCell cell, GC gc, IConfigRegistry configRegistry) {
		return cell.getBounds().height;
	}

	public int getPreferredWidth(LayerCell cell, GC gc, IConfigRegistry configRegistry) {
		return cell.getBounds().width;
	}

	private TimerTask getButtonFlashTimerTask(final ILayer layer){
		return new TimerTask() {
			@Override
			public void run() {
				recentlyClicked = false;
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						layer.fireLayerEvent(new CellVisualChangeEvent(layer, columnPosClicked, rowPosClicked));
					}
				});
			}
		};
	}

	/**
	 * Respond to mouse click. Simulate button press.
	 */
	public void run(final NatTable natTable, MouseEvent event) {
		NatEventData eventData = (NatEventData) event.data;
		columnPosClicked = eventData.getColumnPosition();
		rowPosClicked = eventData.getRowPosition();
		recentlyClicked = true;

		new Timer().schedule(getButtonFlashTimerTask(natTable), buttonFlashTime);
		natTable.fireLayerEvent(new CellVisualChangeEvent(natTable, columnPosClicked, rowPosClicked));

		for (IMouseAction listener : clickLiseners) {
			listener.run(natTable, event);
		}
	}

	public void addClickListener(IMouseAction mouseAction){
		clickLiseners.add(mouseAction);
	}

	public void removeClickListener(IMouseAction mouseAction){
		clickLiseners.remove(mouseAction);
	}

	public void setButtonFlashTime(int flashTimeInMS) {
		buttonFlashTime = flashTimeInMS;
	}
}

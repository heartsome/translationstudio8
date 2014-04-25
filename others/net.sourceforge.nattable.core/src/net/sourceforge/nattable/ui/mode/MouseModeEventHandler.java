package net.sourceforge.nattable.ui.mode;

import net.sourceforge.nattable.NatTable;
import net.sourceforge.nattable.ui.NatEventData;
import net.sourceforge.nattable.ui.action.DragModeEventHandler;
import net.sourceforge.nattable.ui.action.IDragMode;
import net.sourceforge.nattable.ui.action.IMouseAction;
import net.sourceforge.nattable.ui.util.CancelableRunnable;

import org.eclipse.swt.events.MouseEvent;

public class MouseModeEventHandler extends AbstractModeEventHandler {
	
	private final NatTable natTable;
	
	private MouseEvent initialMouseDownEvent;
	
	private IMouseAction singleClickAction;
	
	private IMouseAction doubleClickAction;
	
	private boolean mouseDown;
	
	private IDragMode dragMode;
	
	private SingleClickRunnable singleClickRunnable;
	
	// TODO Placeholder to enable single/double click disambiguation
	private boolean exclusive = false;
	
	public MouseModeEventHandler(ModeSupport modeSupport, NatTable natTable, MouseEvent initialMouseDownEvent, IMouseAction singleClickAction, IMouseAction doubleClickAction, IDragMode dragMode) {
		super(modeSupport);
		
		this.natTable = natTable;
		
		mouseDown = true;
		
		this.initialMouseDownEvent = initialMouseDownEvent;
		
		this.singleClickAction = singleClickAction;
		this.doubleClickAction = doubleClickAction;
		this.dragMode = dragMode;
	}
	
	@Override
	public void mouseUp(MouseEvent event) {
		mouseDown = false;
		
		if (singleClickAction != null) {
			if (exclusive && doubleClickAction != null) {
				// If a doubleClick action is registered, wait to see if this mouseUp is part of a doubleClick or not.
				singleClickRunnable = new SingleClickRunnable(singleClickAction, event);
				event.display.timerExec(event.display.getDoubleClickTime(), singleClickRunnable);
			} else {
				executeSingleClickAction(singleClickAction, event);
			}
		} else if (doubleClickAction == null) {
			// No single or double click action registered when mouseUp detected. Switch back to normal mode.
			switchMode(Mode.NORMAL_MODE);
		}
	}
	
	@Override
	public void mouseDoubleClick(MouseEvent event) {
		if (doubleClickAction != null) {
			if (singleClickRunnable != null) {
				// Cancel any pending singleClick action.
				singleClickRunnable.cancel();
			}
			
			event.data = NatEventData.createInstanceFromEvent(event);
			doubleClickAction.run(natTable, event);
			// Double click action complete. Switch back to normal mode.
			switchMode(Mode.NORMAL_MODE);
		}
	}
	
	@Override
	public synchronized void mouseMove(MouseEvent event) {
		if (mouseDown && dragMode != null) {
			dragMode.mouseDown(natTable, initialMouseDownEvent);
			switchMode(new DragModeEventHandler(getModeSupport(), natTable, dragMode));
		} else {
			// No drag mode registered when mouseMove detected. Switch back to normal mode.
			switchMode(Mode.NORMAL_MODE);
		}
	}
	
	private void executeSingleClickAction(IMouseAction action, MouseEvent event) {
		event.data = NatEventData.createInstanceFromEvent(event);
		action.run(natTable, event);
		// Single click action complete. Switch back to normal mode.
		switchMode(Mode.NORMAL_MODE);
	}
	
	class SingleClickRunnable extends CancelableRunnable {

		private IMouseAction action;
		
		private MouseEvent event;
		
		public SingleClickRunnable(IMouseAction action, MouseEvent event) {
			this.action = action;
			this.event = event;
		}
		
		public void run() {
			if (!isCancelled()) {
				executeSingleClickAction(action, event);
			}
		}
		
	}
	
}

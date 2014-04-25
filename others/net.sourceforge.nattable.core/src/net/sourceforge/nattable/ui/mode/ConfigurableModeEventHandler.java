package net.sourceforge.nattable.ui.mode;

import net.sourceforge.nattable.NatTable;
import net.sourceforge.nattable.ui.NatEventData;
import net.sourceforge.nattable.ui.action.IDragMode;
import net.sourceforge.nattable.ui.action.IKeyAction;
import net.sourceforge.nattable.ui.action.IMouseAction;
import net.sourceforge.nattable.ui.binding.IUiBindingRegistry;

import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseEvent;

public class ConfigurableModeEventHandler extends AbstractModeEventHandler {

	private final NatTable natTable;
	
	private IUiBindingRegistry uiBindingRegistry;
	
	public ConfigurableModeEventHandler(ModeSupport modeSupport, NatTable natTable) {
		super(modeSupport);
		
		this.natTable = natTable;
		this.uiBindingRegistry = natTable.getUiBindingRegistry();
	}
	
	// Event handling /////////////////////////////////////////////////////////
	
	@Override
	public void keyPressed(KeyEvent event) {
		IKeyAction keyAction = uiBindingRegistry.getKeyEventAction(event);
		if (keyAction != null) {
			natTable.forceFocus();
			keyAction.run(natTable, event);
		}
	}
	
	@Override
	public void mouseDown(MouseEvent event) {
		IMouseAction mouseDownAction = uiBindingRegistry.getMouseDownAction(event);
		if (mouseDownAction != null) {
			event.data = NatEventData.createInstanceFromEvent(event);
			mouseDownAction.run(natTable, event);
		}
		
		IMouseAction singleClickAction = uiBindingRegistry.getSingleClickAction(event);
		IMouseAction doubleClickAction = uiBindingRegistry.getDoubleClickAction(event);
		IDragMode dragMode = uiBindingRegistry.getDragMode(event);
		
		if (singleClickAction != null || doubleClickAction != null || dragMode != null) {
			switchMode(new MouseModeEventHandler(getModeSupport(), natTable, event, singleClickAction, doubleClickAction, dragMode));
		}
	}

	@Override
	public synchronized void mouseMove(MouseEvent event) {
		IMouseAction mouseMoveAction = uiBindingRegistry.getMouseMoveAction(event);
		if (mouseMoveAction != null) {
			event.data = NatEventData.createInstanceFromEvent(event);
			mouseMoveAction.run(natTable, event);
		}
	}

}

package net.sourceforge.nattable.ui.mode;

import java.util.HashMap;
import java.util.Map;

import net.sourceforge.nattable.NatTable;

import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;

/**
 * Modal event handler for NatTable. This class acts as a proxy event listener.
 * It manages a set of IModeEventHandler instances which control the actual
 * event handling for a given mode. This allows the event handling behavior for
 * different modes to be grouped together and isolated from each other.
 */
public class ModeSupport implements KeyListener, MouseListener,
		MouseMoveListener, FocusListener {

	private Map<String, IModeEventHandler> modeEventHandlerMap = new HashMap<String, IModeEventHandler>();

	private IModeEventHandler currentModeEventHandler;

	public ModeSupport(NatTable natTable) {
		natTable.addKeyListener(this);
		natTable.addMouseListener(this);
		natTable.addMouseMoveListener(this);
		natTable.addFocusListener(this);
	}

	/**
	 * Register an event handler to handle events for a given mode.
	 * 
	 * @param mode
	 *            The mode.
	 * @param modeEventHandler
	 *            An IModeEventHandler instance that will handle events in the
	 *            given mode.
	 * 
	 * @see IModeEventHandler
	 */
	public void registerModeEventHandler(String mode,
			IModeEventHandler modeEventHandler) {
		modeEventHandlerMap.put(mode, modeEventHandler);
	}

	/**
	 * Switch to the given mode.
	 * 
	 * @param mode
	 *            The target mode to switch to.
	 */
	public void switchMode(String mode) {
		if (currentModeEventHandler != null) {
			currentModeEventHandler.cleanup();
		}
		currentModeEventHandler = modeEventHandlerMap.get(mode);
	}
	
	public void switchMode(IModeEventHandler modeEventHandler) {
		if (currentModeEventHandler != null) {
			currentModeEventHandler.cleanup();
		}
		currentModeEventHandler = modeEventHandler;
	}

	public void keyPressed(KeyEvent event) {
		currentModeEventHandler.keyPressed(event);
	}

	public void keyReleased(KeyEvent event) {
		currentModeEventHandler.keyReleased(event);
	}

	public void mouseDoubleClick(MouseEvent event) {
		currentModeEventHandler.mouseDoubleClick(event);
	}

	public void mouseDown(MouseEvent event) {
		currentModeEventHandler.mouseDown(event);
	}

	public void mouseUp(MouseEvent event) {
		currentModeEventHandler.mouseUp(event);
	}

	public void mouseMove(MouseEvent event) {
		currentModeEventHandler.mouseMove(event);
	}

	public void focusGained(FocusEvent event) {
		currentModeEventHandler.focusGained(event);
	}

	public void focusLost(FocusEvent event) {
		currentModeEventHandler.focusLost(event);
	}

}

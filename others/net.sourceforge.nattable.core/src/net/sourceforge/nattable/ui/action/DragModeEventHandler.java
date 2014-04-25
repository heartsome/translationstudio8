package net.sourceforge.nattable.ui.action;

import net.sourceforge.nattable.NatTable;
import net.sourceforge.nattable.ui.mode.AbstractModeEventHandler;
import net.sourceforge.nattable.ui.mode.Mode;
import net.sourceforge.nattable.ui.mode.ModeSupport;

import org.eclipse.swt.events.MouseEvent;

public class DragModeEventHandler extends AbstractModeEventHandler {

	private final NatTable natTable;
	
	private final IDragMode dragMode;
	
	public DragModeEventHandler(ModeSupport modeSupport, NatTable natTable, IDragMode dragMode) {
		super(modeSupport);
		
		this.natTable = natTable;
		this.dragMode = dragMode;
	}
	
	@Override
	public void mouseMove(MouseEvent event) {
		dragMode.mouseMove(natTable, event);
	}
	
	@Override
	public void mouseUp(MouseEvent event) {
		dragMode.mouseUp(natTable, event);
		switchMode(Mode.NORMAL_MODE);
	}
	
}

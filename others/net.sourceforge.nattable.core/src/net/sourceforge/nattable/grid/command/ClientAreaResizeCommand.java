package net.sourceforge.nattable.grid.command;

import net.sourceforge.nattable.command.AbstractContextFreeCommand;

import org.eclipse.swt.widgets.Scrollable;

/**
 * Command that gives the layers access to ClientArea and the Scrollable 
 */
public class ClientAreaResizeCommand extends AbstractContextFreeCommand {
	private Scrollable scrollable;
	
	public ClientAreaResizeCommand(Scrollable scrollable) {
		super();
		this.scrollable = scrollable;
	}

	public Scrollable getScrollable() {
		return scrollable;
	}
}

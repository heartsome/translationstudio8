package net.sourceforge.nattable.grid.command;

import net.sourceforge.nattable.command.AbstractContextFreeCommand;

import org.eclipse.swt.widgets.Composite;

/**
 * Command that is propagated when NatTable starts up. This gives every layer a
 * chance to initialize itself and compute its structural caches.
 */
public class InitializeGridCommand extends AbstractContextFreeCommand {

	private final Composite tableComposite;

	public InitializeGridCommand(Composite tableComposite) {
		this.tableComposite = tableComposite;
	}

	public Composite getTableComposite() {
		return tableComposite;
	}

}

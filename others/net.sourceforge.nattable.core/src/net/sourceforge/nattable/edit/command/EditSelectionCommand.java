package net.sourceforge.nattable.edit.command;

import net.sourceforge.nattable.command.AbstractContextFreeCommand;
import net.sourceforge.nattable.config.IConfigRegistry;

import org.eclipse.swt.widgets.Composite;

public class EditSelectionCommand extends AbstractContextFreeCommand {
	
	private final IConfigRegistry configRegistry;
	
	private final Character character;

	private final Composite parent;

	public EditSelectionCommand(Composite parent, IConfigRegistry configRegistry, Character character) {
		this.parent = parent;
		this.configRegistry = configRegistry;
		this.character = character;
	}

	public IConfigRegistry getConfigRegistry() {
		return configRegistry;
	}
	
	public Character getCharacter() {
		return character;
	}
	
	public Composite getParent() {
		return parent;
	}
	
}

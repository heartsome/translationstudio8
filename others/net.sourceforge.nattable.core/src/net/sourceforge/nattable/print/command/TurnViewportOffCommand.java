package net.sourceforge.nattable.print.command;

import net.sourceforge.nattable.command.AbstractContextFreeCommand;

/**
 * This command is handled by the viewport. It essentially causes the viewport
 * to turn off by relaying all dimension requests to the underlying scrollable
 * layer.
 * 
 * This is useful when operations have to be performed on the entire grid
 * including the areas outside the viewport. Example printing, excel export,
 * auto resize all columns etc.
 */
public class TurnViewportOffCommand extends AbstractContextFreeCommand {
}

package net.sourceforge.nattable.command;

import net.sourceforge.nattable.NatTable;

/**
 * Command fired by {@link NatTable} just before it is disposed.<br/>
 * This command can be handled by layers which need to dispose resources (to avoid memory leaks). <br/>
 *
 * @see GlazedListsEventLayer
 */
public class DisposeResourcesCommand extends AbstractContextFreeCommand {

}

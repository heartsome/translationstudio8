package net.sourceforge.nattable.resize.command;

import net.sourceforge.nattable.command.AbstractRowCommand;
import net.sourceforge.nattable.layer.ILayer;

/**
 * Event indicating that a row has been resized.
 */
public class RowResizeCommand extends AbstractRowCommand {
    
    private int newHeight;
    
    public RowResizeCommand(ILayer layer, int rowPosition, int newHeight) {
        super(layer, rowPosition);
        this.newHeight = newHeight;
    }
    
    protected RowResizeCommand(RowResizeCommand command) {
    	super(command);
    	this.newHeight = command.newHeight;
    }

    public int getNewHeight() {
        return newHeight;
    }
    
    public RowResizeCommand cloneCommand() {
    	return new RowResizeCommand(this);
    }
    
}

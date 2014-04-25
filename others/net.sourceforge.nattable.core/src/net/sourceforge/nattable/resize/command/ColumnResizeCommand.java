package net.sourceforge.nattable.resize.command;

import net.sourceforge.nattable.command.AbstractColumnCommand;
import net.sourceforge.nattable.layer.ILayer;

/**
 * Event indicating that a column has been resized.
 */
public class ColumnResizeCommand extends AbstractColumnCommand {
	
    private int newColumnWidth;
    
    public ColumnResizeCommand(ILayer layer, int columnPosition, int newWidth) {
    	super (layer, columnPosition);
    	this.newColumnWidth = newWidth;
    }

    protected ColumnResizeCommand(ColumnResizeCommand command) {
    	super(command);
    	this.newColumnWidth = command.newColumnWidth;
    }
    
    public int getNewColumnWidth() {
        return newColumnWidth;
    }
    
    public ColumnResizeCommand cloneCommand() {
    	return new ColumnResizeCommand(this);
    }
    
}
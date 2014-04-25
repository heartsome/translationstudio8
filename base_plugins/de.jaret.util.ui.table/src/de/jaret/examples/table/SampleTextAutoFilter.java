package de.jaret.examples.table;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;

import de.jaret.util.ui.table.filter.AbstractAutoFilter;
import de.jaret.util.ui.table.model.IRow;

/**
 * Sample autofilter rendering as a textbox and filters everything that does not contain the entered String.
 * 
 * @author kliem
 * @version $Id: SampleTextAutoFilter.java,v 1.1 2012-05-07 01:34:38 jason Exp $
 */
public class SampleTextAutoFilter extends AbstractAutoFilter implements ModifyListener {

    private Text _text;
        
    
    public void dispose() {
       if (_text != null) {
           _text.dispose();
       }
    }

    public Control getControl() {
        return _text;
    }

    public void update() {
        if (_text == null) {
            _text = new Text(_table, SWT.NULL);
            _text.addModifyListener(this);
        }
    }

    public boolean isInResult(IRow row) {
        String filter = _text.getText().trim();
        if (filter.length()>0) {
            Object value = _column.getValue(row);
            String valString = value != null ? value.toString() : "";
            return valString.indexOf(filter) != -1;
        }
        
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public void reset() {
        _text.setText("");
        firePropertyChange("FILTER", null, "x");
    }

    
    public void modifyText(ModifyEvent e) {
        firePropertyChange("FILTER", null, "x");
        
    }

    
    
}

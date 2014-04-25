package de.jaret.examples.table;

import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Control;

import de.jaret.util.ui.table.filter.AbstractAutoFilter;
import de.jaret.util.ui.table.model.IColumn;
import de.jaret.util.ui.table.model.IRow;

/**
 * Sample auto filter implementation for selecting int values <50, >50.
 * 
 * @author kliem
 * @version $Id: SampleIntegerAutoFilter.java,v 1.1 2012-05-07 01:34:39 jason Exp $
 */
public class SampleIntegerAutoFilter extends AbstractAutoFilter implements SelectionListener {
    protected CCombo _combo;

    protected static String FILTER_ALL = "(all)";

    protected static String FILTER_LT50 = "<50";

    protected static String FILTER_GT50 = ">=50";

    public void dispose() {
        if (_combo != null) {
            _combo.dispose();
        }
    }

    public Control getControl() {
        return _combo;
    }

    public boolean isInResult(IRow row) {
        String filter = _combo.getText();
        Object value = _column.getValue(row);
        int intVal = 0;
        if (value != null && value instanceof Integer) {
            intVal = ((Integer)value).intValue();
        }
        if (!filter.equals(FILTER_ALL)) {
            if (filter.equals(FILTER_LT50)) {
                return intVal < 50;
            }
            if (filter.equals(FILTER_GT50)) {
                return intVal >= 50;
            }
        }
        return true;
    }

    public void update() {
        if (_combo == null) {
            _combo = new CCombo(_table, SWT.BORDER | SWT.READ_ONLY);
            _combo.addSelectionListener(this);
        }
        String[] items = new String[3];
        int idx = 0;
        items[idx++] = FILTER_ALL;
        items[idx++] = FILTER_LT50;
        items[idx++] = FILTER_GT50;

        _combo.setItems(items);
        _combo.select(0);
    }

    /**
     * {@inheritDoc}
     */
    public void reset() {
        _combo.select(0);
        firePropertyChange("FILTER", null, "x");
    }

    public void widgetDefaultSelected(SelectionEvent e) {
    }

    public void widgetSelected(SelectionEvent e) {
        firePropertyChange("FILTER", null, "x");
    }

}

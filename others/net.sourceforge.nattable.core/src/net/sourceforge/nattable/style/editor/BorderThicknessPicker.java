package net.sourceforge.nattable.style.editor;

import static org.eclipse.swt.SWT.NONE;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;

/**
 * Control to select the thickness of a border.
 */
public class BorderThicknessPicker extends Composite {

    private Combo combo;

    public BorderThicknessPicker(Composite parent) {
        super(parent, NONE);
        setLayout(new RowLayout());

        combo = new Combo(this, SWT.READ_ONLY | SWT.DROP_DOWN);
        combo.setItems(new String[] { "Thin", "Thick", "Very Thick" });
        combo.select(0);
    }
    
    @Override
    public void setEnabled(boolean b) {
        combo.setEnabled(b);
    }

    public int getSelectedThickness() {
        int idx = combo.getSelectionIndex();
        if (idx == 0) return 1;
        else if (idx == 1) return 3;
        else if (idx == 2) return 6;
        else throw new IllegalStateException("never happen");
    }
    
    public void setSelectedThickness(int thickness) {
        if (thickness < 0) throw new IllegalArgumentException("negative number");
        int idx = 0;
        if (thickness < 3) idx = 0;
        else if (thickness < 6) idx = 1;
        else if (thickness > 6) idx = 2;
        combo.select(idx);
    }
}

package net.sourceforge.nattable.style.editor;

import net.sourceforge.nattable.style.VerticalAlignmentEnum;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;

/**
 * Component that lets the user select an alignment.
 */
public class VerticalAlignmentPicker extends Composite {

    private final Combo combo;

    public VerticalAlignmentPicker(Composite parent, VerticalAlignmentEnum alignment) {
        super(parent, SWT.NONE);
        setLayout(new RowLayout());

        combo = new Combo(this, SWT.READ_ONLY | SWT.DROP_DOWN);
        combo.setItems(new String[] { "Top", "Middle", "Bottom" });

        update(alignment);
    }

    private void update(VerticalAlignmentEnum alignment) {
        if (alignment.equals(VerticalAlignmentEnum.TOP))
            combo.select(0);
        else if (alignment.equals(VerticalAlignmentEnum.MIDDLE))
            combo.select(1);
        else if (alignment.equals(VerticalAlignmentEnum.BOTTOM))
            combo.select(2);
        else
            throw new IllegalArgumentException("bad alignment: " + alignment);
    }

    public VerticalAlignmentEnum getSelectedAlignment() {
        int idx = combo.getSelectionIndex();
        if (idx == 0)
            return VerticalAlignmentEnum.TOP;
        else if (idx == 1)
            return VerticalAlignmentEnum.MIDDLE;
        else if (idx == 2)
            return VerticalAlignmentEnum.BOTTOM;
        else
            throw new IllegalStateException("shouldn't happen");
    }

    public void setSelectedAlignment(VerticalAlignmentEnum verticalAlignment) {
        if (verticalAlignment == null) throw new IllegalArgumentException("null");
        update(verticalAlignment);
    }
}

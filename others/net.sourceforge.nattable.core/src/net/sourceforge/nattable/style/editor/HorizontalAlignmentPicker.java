package net.sourceforge.nattable.style.editor;

import net.sourceforge.nattable.style.HorizontalAlignmentEnum;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;

/**
 * Component that lets the user select an alignment.
 */
public class HorizontalAlignmentPicker extends Composite {

    private final Combo combo;

    public HorizontalAlignmentPicker(Composite parent, HorizontalAlignmentEnum alignment) {
        super(parent, SWT.NONE);
        setLayout(new RowLayout());

        combo = new Combo(this, SWT.READ_ONLY | SWT.DROP_DOWN);
        combo.setItems(new String[] { "Center", "Left", "Right" });

        update(alignment);
    }

    private void update(HorizontalAlignmentEnum alignment) {
        if (alignment.equals(HorizontalAlignmentEnum.CENTER))
            combo.select(0);
        else if (alignment.equals(HorizontalAlignmentEnum.LEFT))
            combo.select(1);
        else if (alignment.equals(HorizontalAlignmentEnum.RIGHT))
            combo.select(2);
        else
            throw new IllegalArgumentException("bad alignment: " + alignment);
    }

    public HorizontalAlignmentEnum getSelectedAlignment() {
        int idx = combo.getSelectionIndex();
        if (idx == 0)
            return HorizontalAlignmentEnum.CENTER;
        else if (idx == 1)
            return HorizontalAlignmentEnum.LEFT;
        else if (idx == 2)
            return HorizontalAlignmentEnum.RIGHT;
        else
            throw new IllegalStateException("shouldn't happen");
    }

    public void setSelectedAlignment(HorizontalAlignmentEnum horizontalAlignment) {
        if (horizontalAlignment == null) throw new IllegalArgumentException("null");
        update(horizontalAlignment);
    }
}

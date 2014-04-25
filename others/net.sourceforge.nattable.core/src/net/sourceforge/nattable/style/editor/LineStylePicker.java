package net.sourceforge.nattable.style.editor;

import static org.eclipse.swt.SWT.NONE;
import net.sourceforge.nattable.style.BorderStyle.LineStyleEnum;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;

/**
 * Component to select a {@link LineStyleEnum}.
 */
public class LineStylePicker extends Composite {
    
    private Combo combo;
    
    public LineStylePicker(Composite parent) {
        super(parent, NONE);
        setLayout(new RowLayout());
        
        combo = new Combo(this, SWT.READ_ONLY | SWT.DROP_DOWN);
        combo.setItems(new String[] { "Solid", "Dashed", "Dotted", "Dashdot", "Dashdotdot" });
        combo.select(0);
    }
    
    @Override
    public void setEnabled(boolean enabled) {
        combo.setEnabled(enabled);
    }
    
    public void setSelectedLineStyle(LineStyleEnum lineStyle) {
        int index = 0;
        if (lineStyle.equals(LineStyleEnum.SOLID)) index = 0;
        else if (lineStyle.equals(LineStyleEnum.DASHED)) index = 1;
        else if (lineStyle.equals(LineStyleEnum.DOTTED)) index = 2;
        else if (lineStyle.equals(LineStyleEnum.DASHDOT)) index = 3;
        else if (lineStyle.equals(LineStyleEnum.DASHDOTDOT)) index = 4;
        combo.select(index);
    }
    
    public LineStyleEnum getSelectedLineStyle() {
        int index = combo.getSelectionIndex();
        if (index == 0) return LineStyleEnum.SOLID;
        else if (index == 1) return LineStyleEnum.DASHED;
        else if (index == 2) return LineStyleEnum.DOTTED;
        else if (index == 3) return LineStyleEnum.DASHDOT;
        else if (index == 4) return LineStyleEnum.DASHDOTDOT;
        else throw new IllegalStateException("never happen");
    }

}

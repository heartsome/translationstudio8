package net.sourceforge.nattable.style.editor;

import static org.eclipse.swt.SWT.CHECK;
import static org.eclipse.swt.SWT.NONE;
import net.sourceforge.nattable.style.BorderStyle;
import net.sourceforge.nattable.style.BorderStyle.LineStyleEnum;
import net.sourceforge.nattable.util.GUIHelper;

import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/**
 * EditorPanel for editing a border style.
 */
public class BorderStyleEditorPanel extends AbstractEditorPanel<BorderStyle> {

    private BorderThicknessPicker thicknessPicker;
    private LineStylePicker lineStylePicker;
    private ColorPicker colorPicker;
    private Button noBordersCheckBox;

    @Override
    public String getEditorName() {
        return "Border Style";
    }

    public BorderStyleEditorPanel(Composite parent, int style) {
        super(parent, style);
        initComponents();
    }

    public void initComponents() {
        GridLayout gridLayout = new GridLayout(2, false);
        gridLayout.marginLeft = 10;
		setLayout(gridLayout);

        new Label(this, NONE).setText("No Border");

        noBordersCheckBox = new Button(this, CHECK);
        noBordersCheckBox.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                    boolean noBorder = noBordersCheckBox.getSelection();
                    colorPicker.setEnabled(!noBorder);
                    thicknessPicker.setEnabled(!noBorder);
                    lineStylePicker.setEnabled(!noBorder);
            }
        });

        new Label(this, NONE).setText("Color");
        colorPicker = new ColorPicker(this, GUIHelper.COLOR_WIDGET_BORDER);

        new Label(this, NONE).setText("Line Style");
        lineStylePicker = new LineStylePicker(this);

        new Label(this, NONE).setText("Thickness");
        thicknessPicker = new BorderThicknessPicker(this);

        // By default, no border is selected and all controls are disabled
        noBordersCheckBox.setSelection(true);
        colorPicker.setEnabled(false);
        thicknessPicker.setEnabled(false);
        lineStylePicker.setEnabled(false);
    }

    private void disableEditing() {
        colorPicker.setEnabled(false);
        thicknessPicker.setEnabled(false);
        lineStylePicker.setEnabled(false);
    }

    public void edit(BorderStyle bstyle) throws Exception {
        if (bstyle != null) {
            noBordersCheckBox.setSelection(false);
            colorPicker.setSelectedColor(bstyle.getColor());
            lineStylePicker.setSelectedLineStyle(bstyle.getLineStyle());
            thicknessPicker.setSelectedThickness(bstyle.getThickness());
        } else {
            noBordersCheckBox.setSelection(true);
            disableEditing();
        }
    }

    public BorderStyle getNewValue() {
        if (!noBordersCheckBox.getSelection()) {
            Color borderColor = colorPicker.getSelectedColor();
            LineStyleEnum lineStyle = lineStylePicker.getSelectedLineStyle();
            int borderThickness = thicknessPicker.getSelectedThickness();
            return new BorderStyle(borderThickness, borderColor, lineStyle);
        }
        return null;
    }
}

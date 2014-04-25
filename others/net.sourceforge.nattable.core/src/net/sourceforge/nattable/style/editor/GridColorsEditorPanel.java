package net.sourceforge.nattable.style.editor;

import net.sourceforge.nattable.config.IConfigRegistry;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class GridColorsEditorPanel extends AbstractEditorPanel<GridStyleParameterObject> {
	private FontPicker fontPicker;
	private ColorPicker evenRowColorPicker;
	private ColorPicker oddRowColorPicker;
	private ColorPicker selectionColorPicker;
	private IConfigRegistry configRegistry;

	public GridColorsEditorPanel(Composite parent, GridStyleParameterObject currentStyle) {
		super(parent, SWT.NONE);
	}

	@Override
	public String getEditorName() {
		return "Grid colors";
	}

	@Override
	public GridStyleParameterObject getNewValue() {
		GridStyleParameterObject newStyle = new GridStyleParameterObject(configRegistry);
		newStyle.tableFont = fontPicker.getSelectedFont();
		newStyle.evenRowColor = evenRowColorPicker.getSelectedColor();
		newStyle.oddRowColor = oddRowColorPicker.getSelectedColor();
		newStyle.selectionColor = selectionColorPicker.getSelectedColor();
		return newStyle;
	}

	@Override
	public void edit(GridStyleParameterObject currentStyle) throws Exception {
		configRegistry = currentStyle.getConfigRegistry();
        GridLayout layout = new GridLayout(2, false);
        layout.marginLeft = 10;
		setLayout(layout);

		new Label(this, SWT.NONE).setText("Font");
		fontPicker = new FontPicker(this, currentStyle.tableFont);
		fontPicker.setLayoutData(new GridData(100, 22));

		new Label(this, SWT.NONE).setText("Even row color");
        evenRowColorPicker = new ColorPicker(this, currentStyle.evenRowColor);

        new Label(this, SWT.NONE).setText("Odd row color");
        oddRowColorPicker = new ColorPicker(this, currentStyle.oddRowColor);

        new Label(this, SWT.NONE).setText("Selection Color");
        selectionColorPicker = new ColorPicker(this, currentStyle.selectionColor);
	}

}

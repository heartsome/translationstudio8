package net.sourceforge.nattable.style.editor;

import net.sourceforge.nattable.util.GUIHelper;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FontDialog;
import org.eclipse.swt.widgets.Shell;

/**
 * A button that displays a font name and allows the user to pick another font. 
 */
public class FontPicker extends Button {
    
    private Font selectedFont;
    private FontData[] fontData = new FontData[1];
    private Font displayFont; 
    
    public FontPicker(final Composite parent, Font originalFont) {
        super(parent, SWT.NONE);
        if (originalFont == null) throw new IllegalArgumentException("null");
        
        update(originalFont.getFontData()[0]);
        
        addSelectionListener(
                new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                    	FontDialog dialog = new FontDialog(new Shell(Display.getDefault(), SWT.SHELL_TRIM));
                        dialog.setFontList(fontData);
                        FontData selected = dialog.open();
                        if (selected != null) {                            
                            update(selected);
                            pack(true);
                        }
                    }
                });
    }
    
    private void update(FontData data) {
        this.fontData[0] = data;
        this.selectedFont = GUIHelper.getFont(data);
        setText(data.getName() + ", " + data.getHeight() + "pt");
        setFont(createDisplayFont(data));
        setAlignment(SWT.CENTER);
        setToolTipText("Click to select font");
    }
    
    private Font createDisplayFont(FontData data) {
        FontData resizedData = new FontData(data.getName(), 8, data.getStyle());
        displayFont = GUIHelper.getFont(resizedData);
        return displayFont;
    }
    
    /**
     * @return Font selected by the user. <em>Note that it is the responsibility of the client to dispose of this
     *         resource.</em>
     */
    public Font getSelectedFont() {
        return selectedFont;
    }
    
    /**
     * Set the selected font. <em>Note that this class will not take ownership of the passed resource. Instead it will
     * create and manage its own internal copy.</em>
     */
    public void setSelectedFont(Font font) {
        if (font == null) throw new IllegalArgumentException("null");
        update(font.getFontData()[0]);
    }

    @Override
    protected void checkSubclass() {
        ; // do nothing
    }
}

    

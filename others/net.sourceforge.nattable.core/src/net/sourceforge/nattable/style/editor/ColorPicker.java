package net.sourceforge.nattable.style.editor;

import net.sourceforge.nattable.util.GUIHelper;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.ColorDialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 * A button that displays a solid block of color and allows the user to pick a color. The user can double click on the
 * button in order to change the selected color which also changes the background color of the button.
 *
 */
public class ColorPicker extends CLabel {

    private Color selectedColor;
	private Image image;

    public ColorPicker(Composite parent, final Color originalColor) {
        super(parent, SWT.SHADOW_OUT);
        if (originalColor == null) throw new IllegalArgumentException("null");
        this.selectedColor = originalColor;
        setImage(getColorImage(originalColor));
        addMouseListener(
                new MouseAdapter() {
                    @Override
                    public void mouseDown(MouseEvent e) {
                        ColorDialog dialog = new ColorDialog(new Shell(Display.getDefault(), SWT.SHELL_TRIM));
                        dialog.setRGB(selectedColor.getRGB());
                        RGB selected = dialog.open();
                        if (selected != null) {
                            update(selected);
                        }
                    }
                });
    }

    private Image getColorImage(Color color){
    	Display display = Display.getCurrent();
		image = new Image(display, new Rectangle(10, 10, 70, 20));
        GC gc = new GC(image);
        gc.setBackground(color);
        gc.fillRectangle(image.getBounds());
        gc.dispose();
        return image;
    }

    private void update(RGB selected) {
        this.selectedColor = GUIHelper.getColor(selected);
        setImage(getColorImage(selectedColor));
    }

    /**
     * @return the Color most recently selected by the user. <em>Note that it is the responsibility of the client to
     *         dispose this resource</em>
     */
    public Color getSelectedColor() {
        return selectedColor;
    }

    /**
     * Set the current selected color that will be displayed by the picker. <em>Note that this class is not responsible
     * for destroying the given Color object. It does not take ownership. Instead it will create its own internal
     * copy of the given Color resource.</em>
     *
     * @param backgroundColor
     */
    public void setSelectedColor(Color backgroundColor) {
        if (backgroundColor == null) throw new IllegalArgumentException("null");
        update(backgroundColor.getRGB());
    }

    @Override
    public void dispose() {
    	super.dispose();
    	image.dispose();
    }
}
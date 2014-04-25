/*
 *  File: DefaultTableHeaderRenderer.java 
 *  Copyright (c) 2004-2007  Peter Kliem (Peter.Kliem@jaret.de)
 *  A commercial license is available, see http://www.jaret.de.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 */
package de.jaret.util.ui.table.renderer;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Transform;
import org.eclipse.swt.printing.Printer;
import org.eclipse.swt.widgets.Display;

import de.jaret.util.swt.SwtGraphicsHelper;
import de.jaret.util.ui.table.model.IColumn;

/**
 * Default header renderer for the jaret table. The header renderer will render a simple header view. The renderer
 * supports rotating the header text from 0 to 90 degrees anti-clock wise. If a rotation is set, the header is drawn
 * using a white background. Several properties allow changing the drawing (always consider writing a specialized
 * renderer!).
 * 
 * @author Peter Kliem
 * @version $Id: DefaultTableHeaderRenderer.java,v 1.1 2012-05-07 01:34:38 jason Exp $
 */
public class DefaultTableHeaderRenderer extends RendererBase implements ITableHeaderRenderer {
    /** Alignment enumeration. */
    public enum Alignment {
        LEFT, CENTER, RIGHT
    };

    /** default background rgb for non rotated drawing. */
    private static RGB DEFAULTBACKGROUND = new RGB(220, 220, 220);

    /** Alignment: default left. */
    protected Alignment _alignment = Alignment.LEFT;

    /** true if the header box should be drawn. */
    protected boolean _drawBox = true;

    /** background rgb value. */
    protected RGB _backgroundRGB = DEFAULTBACKGROUND;
    /** allocated background color. */
    protected Color _bgColor;

    /** FOntadat of the font to use. */
    protected FontData _fontData;
    /** font when aquired. */
    protected Font _font;

    /** rotation of the header text. */
    protected int _rotation = 0;

    /** Transformations for rotated text. */
    protected Transform _transform;
    /** inverse transformation to reset gc. */
    protected Transform _transformInv;

    protected ImageRegistry _imageRegistry;

    /** key for uowards arrow. */
    protected static final String UP = "up";
    /** key for downwards arrow. */
    protected static final String DOWN = "down";
    /** width reserved for the sorting area. */
    protected static final int SORTINGAREAINDICATORWIDTH = 16;

    /** preferred height to use when more space is available. */
    private static final int PREFHEIGHT = 20;

    /**
     * Construct a header renderer for printing.
     * 
     * @param printer printer device
     */
    public DefaultTableHeaderRenderer(Printer printer) {
        super(printer);
    }

    /**
     * Construct header renderer for a display.
     */
    public DefaultTableHeaderRenderer() {
        super(null);
    }

    /**
     * Set the rotation of the header text. Please note that you have to call <code>redraw()</code> on the table
     * yourself if you change the rotation while the table is showing.
     * 
     * @param rotation rotation in degrees anti clockwise between 0 and 90 degrees.
     */
    public void setRotation(int rotation) {
        if (rotation < 0 || rotation > 90) {
            throw new IllegalArgumentException("Rotation range 0..90");
        }
        if (_rotation != rotation) {
            disposeTransformations();
            _rotation = rotation;
            _transform = new Transform(Display.getCurrent());
            _transformInv = new Transform(Display.getCurrent());
            _transform.rotate(-rotation);
            _transformInv.rotate(-rotation);
            _transformInv.invert();
        }
    }

    /**
     * {@inheritDoc}
     */
    public void draw(GC gc, Rectangle drawingArea, IColumn column, int sortingOrder, boolean sortDir, boolean printing) {
        Color bg = gc.getBackground();
        Font font = gc.getFont();
        String label = column.getHeaderLabel();

        if (_fontData != null && _font == null) {
            _font = new Font(gc.getDevice(), _fontData);
        }
        if (_font != null) {
            gc.setFont(_font);
        }

        if (_rotation == 0) {
            // classic rendering

            // allocate color when not allocated
            if (_bgColor == null) {
                _bgColor = new Color(gc.getDevice(), _backgroundRGB);
            }

            gc.setBackground(_bgColor);

            // if the available space is too big, restrict to pref height
            if (drawingArea.height > PREFHEIGHT) {
                drawingArea.y += drawingArea.height - PREFHEIGHT;
                drawingArea.height = PREFHEIGHT;

            }

            gc.fillRectangle(drawingArea);
            if (sortingOrder > 0) {
                Image img = getImageRegistry().get(sortDir ? DOWN : UP);
                gc.drawImage(img, drawingArea.x + 2, drawingArea.y + drawingArea.height - img.getBounds().height - 1);
            }
            // box or line
            if (_drawBox) {
                gc.drawRectangle(drawingArea.x, drawingArea.y, drawingArea.width - 1, drawingArea.height - 1);
            } else {
                gc.drawLine(drawingArea.x, drawingArea.y + drawingArea.height - 1, drawingArea.width - 1, drawingArea.y
                        + drawingArea.height - 1);
            }

            // label
            int offx = column.supportsSorting() ? SORTINGAREAINDICATORWIDTH : 2;
            if (_alignment.equals(Alignment.LEFT)) {
                gc.drawString(label, drawingArea.x + offx, drawingArea.y + scaleY(2));
            } else if (_alignment.equals(Alignment.CENTER)) {
                Rectangle rect = new Rectangle(drawingArea.x + offx, drawingArea.y + scaleY(2), drawingArea.width
                        - offx, drawingArea.height - 2 * scaleY(2));
                SwtGraphicsHelper.drawStringCentered(gc, label, rect);
            } else if (_alignment.equals(Alignment.RIGHT)) {
                SwtGraphicsHelper.drawStringRightAlignedVTop(gc, label, drawingArea.x + drawingArea.width,
                        drawingArea.y + scaleY(2));
            }
        } else {
            // rotated drawing
            gc.setBackground(gc.getDevice().getSystemColor(SWT.COLOR_WHITE));
            Point extent = gc.stringExtent(label);
            float[] cords = {(float) (drawingArea.x + ((drawingArea.width - extent.x / 2) / 2)),
                    (float) (drawingArea.y + drawingArea.height - 9)};
            _transformInv.transform(cords);

            gc.setTransform(_transform);
            gc.drawString(label, (int) cords[0], (int) cords[1]);
            gc.setTransform(null);
        }

        gc.setFont(font);
        gc.setBackground(bg);
    }

    /**
     * {@inheritDoc}
     */
    public boolean disableClipping() {
        // disable clipping when rotated
        return _rotation != 0;
    }

    /**
     * {@inheritDoc}
     */
    public void dispose() {
        disposeTransformations();
        if (_imageRegistry != null) {
            _imageRegistry.dispose();
        }
        if (_bgColor != null) {
            _bgColor.dispose();
        }
        if (_font != null) {
            _font.dispose();
        }

    }

    private ImageRegistry getImageRegistry() {
        if (_imageRegistry == null) {
            _imageRegistry = new ImageRegistry();
            ImageDescriptor imgDesc = new LocalResourceImageDescriptor(
                    "/de/jaret/util/ui/table/resource/smallarrow_down.gif");
            _imageRegistry.put(DOWN, imgDesc.createImage());
            imgDesc = new LocalResourceImageDescriptor("/de/jaret/util/ui/table/resource/smallarrow_up.gif");
            _imageRegistry.put(UP, imgDesc.createImage());
        }
        return _imageRegistry;
    }

    public class LocalResourceImageDescriptor extends ImageDescriptor {
        String rscString;

        /**
         * 
         */
        public LocalResourceImageDescriptor(String rscString) {
            this.rscString = rscString;
        }

        /**
         * {@inheritDoc}
         */
        public ImageData getImageData() {
            Image img = new Image(Display.getCurrent(), this.getClass().getResourceAsStream(rscString));
            return img.getImageData();
        }
    }

    /**
     * Dispose the transformations.
     * 
     */
    private void disposeTransformations() {
        if (_transform != null) {
            _transform.dispose();
        }
        if (_transformInv != null) {
            _transformInv.dispose();
        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean isSortingClick(Rectangle drawingArea, IColumn column, int x, int y) {
        return x - drawingArea.x < SORTINGAREAINDICATORWIDTH;
    }

    /**
     * {@inheritDoc}
     */
    public ITableHeaderRenderer getPrintRenderer(Printer printer) {
        return new DefaultTableHeaderRenderer(printer);
    }

    /**
     * Retrieve the alignment for the header label (only when not rotated).
     * 
     * @return the alignment
     */
    public Alignment getAlignment() {
        return _alignment;
    }

    /**
     * Set the alignment for the header label (not used when rotated).
     * 
     * @param alignment alignment to be used
     */
    public void setAlignment(Alignment alignment) {
        _alignment = alignment;
    }

    /**
     * Retrieve whether the header is drawn boxed.
     * 
     * @return true if a box is drawn around the header
     */
    public boolean getDrawBox() {
        return _drawBox;
    }

    /**
     * Set whether the header should be drawn boxed.
     * 
     * @param drawBox true for boxed drawing
     */
    public void setDrawBox(boolean drawBox) {
        _drawBox = drawBox;
    }

    /**
     * Get the background RGB value of the header (non rotated only).
     * 
     * @return the RGB value for the background
     */
    public RGB getBackgroundRGB() {
        return _backgroundRGB;
    }

    /**
     * Set the background rgb value. The color will be aquired when used. Will only be used when non rotated.
     * 
     * @param backgroundRGB the RGB value
     */
    public void setBackgroundRGB(RGB backgroundRGB) {
        if (_bgColor != null) {
            _bgColor.dispose();
            _bgColor = null;
        }
        _backgroundRGB = backgroundRGB;
    }

    /**
     * Get the fontdata for the font used to render the header label.
     * 
     * @return the fontdata
     */
    public FontData getFontData() {
        return _fontData;
    }

    /**
     * Set the fontdata for the font to render the header. The font will be aquired when used.
     * 
     * @param fontData fontdat ato use
     */
    public void setFontData(FontData fontData) {
        if (_font != null) {
            _font.dispose();
            _font = null;
        }
        _fontData = fontData;
    }

}

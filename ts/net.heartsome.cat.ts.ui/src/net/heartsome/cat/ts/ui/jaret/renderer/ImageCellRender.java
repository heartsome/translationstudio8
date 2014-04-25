package net.heartsome.cat.ts.ui.jaret.renderer;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.printing.Printer;
import org.eclipse.swt.widgets.Display;

import de.jaret.util.ui.ResourceImageDescriptor;
import de.jaret.util.ui.table.JaretTable;
import de.jaret.util.ui.table.model.IColumn;
import de.jaret.util.ui.table.model.IRow;
import de.jaret.util.ui.table.renderer.CellRendererBase;
import de.jaret.util.ui.table.renderer.ICellRenderer;
import de.jaret.util.ui.table.renderer.ICellStyle;
import de.jaret.util.ui.table.renderer.ObjectImageRenderer;

/**
 * 用于对单元格设置图片，并支持点击后切换图片
 * @author  peason
 * @version 
 * @since   JDK1.6
 */
public class ImageCellRender extends CellRendererBase implements ICellRenderer {

	protected Map<Object, String> keyMap = new HashMap<Object, String>();
	private ImageRegistry imageRegistry;

	public ImageCellRender(Printer printer) {
		super(printer);
	}

	public ImageCellRender() {
		super(null);
	}

	/**
	 * Add a mapping between an object instance and an image descriptor.
	 * @param o
	 *            object instance
	 * @param key
	 *            string key (has to be non null an unique for this renderer) to identfy the object
	 * @param imageDescriptor
	 *            image descriptor for the image
	 */
	public void addImageDescriptorMapping(Object o, String key, ImageDescriptor imageDescriptor) {
		getImageRegistry().put(key, imageDescriptor);
		keyMap.put(o, key);
	}

	/**
	 * Add a mapping between object instance and an image ressource.
	 * @param o
	 *            object instance
	 * @param key
	 *            string key (has to be non null an unique for this renderer) to identfy the object
	 * @param ressourceName
	 *            ressource path
	 */
	public void addRessourceNameMapping(Object o, String key, String ressourceName) {
		ImageDescriptor imgDesc = new ResourceImageDescriptor(ressourceName, this.getClass());
		addImageDescriptorMapping(o, key, imgDesc);
	}

	public void draw(GC gc, JaretTable jaretTable, ICellStyle cellStyle, Rectangle drawingArea, IRow row,
			IColumn column, boolean drawFocus, boolean selected, boolean printing) {
		drawBackground(gc, drawingArea, cellStyle, selected, printing);
		Rectangle drect = drawBorder(gc, cellStyle, drawingArea, printing);
		Rectangle rect = applyInsets(drect);
		Object value = column.getValue(row);
		String key = keyMap.get(value);

		if (key != null) {
			Image img = null;
			img = getImageRegistry().get(key);
			int x = rect.x + (rect.width - scaleX(img.getBounds().width)) / 2;
			int y = rect.y + (rect.height - scaleY(img.getBounds().height)) / 2;
			gc.drawImage(img, 0, 0, img.getBounds().width, img.getBounds().height, x, y, scaleX(img.getBounds().width),
					scaleY(img.getBounds().height));
		} else {
			Color bg = gc.getBackground();
			gc.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_MAGENTA));
			gc.fillRectangle(rect);
			gc.setBackground(bg);
		}
		if (drawFocus) {
			drawFocus(gc, drect);
		}
		drawSelection(gc, drawingArea, cellStyle, selected, printing);
	}

	/**
	 * Retrieve the image registry instance.
	 * @return ImageRegistry
	 */
	private synchronized ImageRegistry getImageRegistry() {
		if (imageRegistry == null) {
			imageRegistry = new ImageRegistry();
		}
		return imageRegistry;
	}

	public ICellRenderer createPrintRenderer(Printer printer) {
		ObjectImageRenderer renderer = new ObjectImageRenderer(printer);
		for (Object o : keyMap.keySet()) {
			String key = keyMap.get(o);
			ImageDescriptor imageDesc = getImageRegistry().getDescriptor(key);
			renderer.addObjectImageDescriptorMapping(o, key, imageDesc);
		}
		return renderer;
	}

	public void dispose() {
		if (imageRegistry != null) {
			imageRegistry.dispose();
		}
		keyMap.clear();
	}

}

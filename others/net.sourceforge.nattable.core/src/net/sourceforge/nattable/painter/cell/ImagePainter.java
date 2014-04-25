package net.sourceforge.nattable.painter.cell;

import net.sourceforge.nattable.config.IConfigRegistry;
import net.sourceforge.nattable.layer.cell.LayerCell;
import net.sourceforge.nattable.style.CellStyleAttributes;
import net.sourceforge.nattable.style.CellStyleUtil;
import net.sourceforge.nattable.style.IStyle;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;

/**
 * Paints an image. If no image is provided, it will attempt to look up an image from the cell style.
 */
public class ImagePainter extends BackgroundPainter {

	private final Image image;
	private final boolean paintBg;

	public ImagePainter() {
		this(null);
	}

	public ImagePainter(Image image) {
		this(image, true);
	}

	public ImagePainter(Image image, boolean paintBg) {
		this.image = image;
		this.paintBg = paintBg;
	}

	@Override
	public int getPreferredWidth(LayerCell cell, GC gc, IConfigRegistry configRegistry) {
		Image image = getImage(cell, configRegistry);
		if (image != null) {
			return image.getBounds().width;
		} else {
			return 0;
		}
	}

	@Override
	public int getPreferredHeight(LayerCell cell, GC gc, IConfigRegistry configRegistry) {
		Image image = getImage(cell, configRegistry);
		if (image != null) {
			return image.getBounds().height;
		} else {
			return 0;
		}
	}

	@Override
	public void paintCell(LayerCell cell, GC gc, Rectangle bounds, IConfigRegistry configRegistry) {
		if (paintBg) {
			super.paintCell(cell, gc, bounds, configRegistry);
		}

		Image image = getImage(cell, configRegistry);
		if (image != null) {
			Rectangle imageBounds = image.getBounds();
			IStyle cellStyle = CellStyleUtil.getCellStyle(cell, configRegistry);
			gc.drawImage(image, bounds.x + CellStyleUtil.getHorizontalAlignmentPadding(cellStyle, bounds, imageBounds.width), bounds.y + CellStyleUtil.getVerticalAlignmentPadding(cellStyle, bounds, imageBounds.height));
		}
	}

	protected Image getImage(LayerCell cell, IConfigRegistry configRegistry) {
		if (image != null) {
			return image;
		} else {
			return CellStyleUtil.getCellStyle(cell, configRegistry).getAttributeValue(CellStyleAttributes.IMAGE);
		}
	}

}

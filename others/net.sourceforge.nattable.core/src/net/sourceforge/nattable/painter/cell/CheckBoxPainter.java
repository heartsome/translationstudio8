package net.sourceforge.nattable.painter.cell;

import net.sourceforge.nattable.config.IConfigRegistry;
import net.sourceforge.nattable.layer.cell.LayerCell;
import net.sourceforge.nattable.util.GUIHelper;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;

public class CheckBoxPainter extends ImagePainter {

	private final Image checkedImg;
	private final Image uncheckedImg;

	public CheckBoxPainter() {
		checkedImg = GUIHelper.getImage("checked");
		uncheckedImg = GUIHelper.getImage("unchecked");
	}

	public CheckBoxPainter(Image checkedImg, Image uncheckedImg) {
		super();
		this.checkedImg = checkedImg;
		this.uncheckedImg = uncheckedImg;
	}

	public int getPreferredWidth(boolean checked) {
		return checked ? checkedImg.getBounds().width : uncheckedImg.getBounds().width;
	}

	public int getPreferredHeight(boolean checked) {
		return checked ? checkedImg.getBounds().height : uncheckedImg.getBounds().height;
	}

	public void paintIconImage(GC gc, Rectangle rectangle, int yOffset, boolean checked) {
		Image checkBoxImage = checked ? checkedImg : uncheckedImg;

		// Center image
		int x = rectangle.x + (rectangle.width / 2) - (checkBoxImage.getBounds().width/2);

		gc.drawImage(checkBoxImage, x, rectangle.y + yOffset);
	}

	@Override
	protected Image getImage(LayerCell cell, IConfigRegistry configRegistry) {
		return isChecked(cell, configRegistry) ? checkedImg : uncheckedImg;
	}

	protected boolean isChecked(LayerCell cell, IConfigRegistry configRegistry) {
		return ((Boolean) cell.getDataValue()).booleanValue();
	}
}

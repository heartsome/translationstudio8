package net.sourceforge.nattable.painter.cell;

import net.sourceforge.nattable.util.GUIHelper;

import org.eclipse.swt.graphics.Image;

public class DisabledCheckboxPainter extends CheckBoxPainter {

	public DisabledCheckboxPainter() {
		super(GUIHelper.getImage("checked_disabled"), GUIHelper.getImage("unchecked_disabled"));
	}

	public DisabledCheckboxPainter(Image checkedImg, Image uncheckedImg) {
		super(checkedImg, uncheckedImg);
	}
}

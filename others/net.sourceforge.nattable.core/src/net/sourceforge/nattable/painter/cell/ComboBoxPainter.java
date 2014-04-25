package net.sourceforge.nattable.painter.cell;

import net.sourceforge.nattable.painter.cell.decorator.CellPainterDecorator;
import net.sourceforge.nattable.ui.util.CellEdgeEnum;
import net.sourceforge.nattable.util.GUIHelper;

public class ComboBoxPainter extends CellPainterWrapper {

	public ComboBoxPainter() {
		setWrappedPainter(new CellPainterDecorator(new TextPainter(), CellEdgeEnum.RIGHT, new ImagePainter(GUIHelper.getImage("down_2"))));
	}
	
}

package net.sourceforge.nattable.painter.cell;

import net.sourceforge.nattable.painter.cell.decorator.LineBorderDecorator;
import net.sourceforge.nattable.painter.cell.decorator.PaddingDecorator;
import net.sourceforge.nattable.painter.cell.decorator.PercentageBarDecorator;
import net.sourceforge.nattable.style.BorderStyle;

public class PercentageBarCellPainter extends CellPainterWrapper {

	public PercentageBarCellPainter() {
		super(new PaddingDecorator(new LineBorderDecorator(new PercentageBarDecorator(new TextPainter()), new BorderStyle())));
	}

}

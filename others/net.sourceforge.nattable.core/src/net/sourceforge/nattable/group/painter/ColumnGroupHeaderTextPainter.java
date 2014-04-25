package net.sourceforge.nattable.group.painter;

import net.sourceforge.nattable.config.IConfigRegistry;
import net.sourceforge.nattable.group.ColumnGroupModel;
import net.sourceforge.nattable.layer.cell.LayerCell;
import net.sourceforge.nattable.painter.cell.CellPainterWrapper;
import net.sourceforge.nattable.painter.cell.ICellPainter;
import net.sourceforge.nattable.painter.cell.ImagePainter;
import net.sourceforge.nattable.painter.cell.TextPainter;
import net.sourceforge.nattable.painter.cell.decorator.CellPainterDecorator;
import net.sourceforge.nattable.ui.util.CellEdgeEnum;
import net.sourceforge.nattable.util.GUIHelper;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;

public class ColumnGroupHeaderTextPainter extends CellPainterWrapper {

	/** Needed to query column group cell expand/collapse state */
	private final ColumnGroupModel columnGroupModel;

	public ColumnGroupHeaderTextPainter(ColumnGroupModel columnGroupModel) {
		this.columnGroupModel = columnGroupModel;

		setWrappedPainter(new CellPainterDecorator( new TextPainter(), CellEdgeEnum.RIGHT, new ExpandCollapseImagePainter()));
	}

	/**
	 * @param columnGroupModel Column group model used by the grid
	 * @param interiorPainter for painting the text portion
	 * @param imagePainter for painting the icon image on the right
	 */
	public ColumnGroupHeaderTextPainter(ColumnGroupModel columnGroupModel, ICellPainter interiorPainter) {
		this.columnGroupModel = columnGroupModel;

		setWrappedPainter(new CellPainterDecorator(interiorPainter, CellEdgeEnum.RIGHT, new ExpandCollapseImagePainter()));
	}

	/**
	 * Preferred width is used during auto resize.
	 * Column groups do not participate in auto resize, since auto resizing is
	 * done by the column width. Hence, always return 0
	 */
	@Override
	public int getPreferredWidth(LayerCell cell, GC gc, IConfigRegistry configRegistry) {
		return 0;
	}

	public class ExpandCollapseImagePainter extends ImagePainter {
		final Image rightImg = GUIHelper.getImage("right");
		final Image leftImg = GUIHelper.getImage("left");

		public ExpandCollapseImagePainter() {
			super(null, false);
		}

		@Override
		protected Image getImage(LayerCell cell, IConfigRegistry configRegistry) {
			String cellValue = cell.getDataValue().toString();
			if (columnGroupModel.isAGroup(cellValue)) {
				return columnGroupModel.isCollapsed(cellValue) ? rightImg : leftImg;
			} else {
				return null;
			}
		}

	}

}

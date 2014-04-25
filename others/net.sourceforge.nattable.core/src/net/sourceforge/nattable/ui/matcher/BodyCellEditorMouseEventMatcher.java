package net.sourceforge.nattable.ui.matcher;

import net.sourceforge.nattable.NatTable;
import net.sourceforge.nattable.edit.EditConfigAttributes;
import net.sourceforge.nattable.edit.editor.ICellEditor;
import net.sourceforge.nattable.grid.GridRegion;
import net.sourceforge.nattable.layer.LabelStack;
import net.sourceforge.nattable.layer.cell.LayerCell;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.eclipse.swt.events.MouseEvent;

public class BodyCellEditorMouseEventMatcher implements IMouseEventMatcher {

	private Class<?> cellEditorClass;
	
	public BodyCellEditorMouseEventMatcher(Class<?> cellEditorClass) {
		this.cellEditorClass = cellEditorClass;
	}
	
	public boolean matches(NatTable natTable, MouseEvent event, LabelStack regionLabels) {
		if (regionLabels != null && regionLabels.hasLabel(GridRegion.BODY)) {
			LayerCell cell = natTable.getCellByPosition(natTable.getColumnPositionByX(event.x), natTable.getRowPositionByY(event.y));
			
			ICellEditor cellEditor = natTable.getConfigRegistry().getConfigAttribute(EditConfigAttributes.CELL_EDITOR, cell.getDisplayMode(), cell.getConfigLabels().getLabels());
			if (cellEditorClass.isInstance(cellEditor)) {
				return true;
			}
		}
		return false;
	}
	
	@Override
    public boolean equals(Object obj) {
		if (obj instanceof BodyCellEditorMouseEventMatcher == false) {
			return false;
		}
		
		if (this == obj) {
			return true;
		}
		
		BodyCellEditorMouseEventMatcher rhs = (BodyCellEditorMouseEventMatcher) obj;
		
		return new EqualsBuilder()
			.append(cellEditorClass, rhs.cellEditorClass)
			.isEquals();
	}
	
	@Override
    public int hashCode() {
		return new HashCodeBuilder(43, 21)
			.append(cellEditorClass)
			.toHashCode();
	}

}

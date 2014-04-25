package net.sourceforge.nattable.columnChooser;

import java.util.ArrayList;
import java.util.List;

import net.sourceforge.nattable.columnChooser.gui.ColumnChooserDialog;

import org.apache.commons.lang.builder.HashCodeBuilder;
import org.eclipse.swt.widgets.TreeItem;

/**
 * Object representation of a ColumnGroup in the SWT tree.<br/>
 * NOTE: this is set as the SWT data on the {@link TreeItem}.<br/>
 *
 * @see ColumnChooserDialog#populateModel
 */
public class ColumnGroupEntry {
	private final String label;
	private final Integer firstElementPosition;
	private final Integer firstElementIndex;
	private final boolean isCollapsed;

	public ColumnGroupEntry(String label, Integer firstElementPosition, Integer firstElementIndex, boolean isCollapsed) {
		super();
		this.label = label;
		this.firstElementPosition = firstElementPosition;
		this.firstElementIndex = firstElementIndex;
		this.isCollapsed = isCollapsed;
	}

	public String getLabel() {
		return label;
	}

	public Integer getFirstElementPosition() {
		return firstElementPosition;
	}

	public Integer getFirstElementIndex() {
		return firstElementIndex;
	}

	public boolean isCollapsed() {
		return isCollapsed;
	}

	public static List<Integer> getColumnGroupEntryPositions(List<ColumnGroupEntry> columnEntries) {
		List<Integer> columnGroupEntryPositions = new ArrayList<Integer>();
		for (ColumnGroupEntry ColumnGroupEntry : columnEntries) {
			columnGroupEntryPositions.add(ColumnGroupEntry.getFirstElementPosition());
		}
		return columnGroupEntryPositions;
	}

	@Override
	public String toString() {
		return "ColumnGroupEntry ("+
				 "Label: " + label +
				 ", firstElementPosition: " + firstElementPosition +
				 ", firstElementIndex: " + firstElementIndex +
				 ", collapsed: " + isCollapsed + ")";
	}

	@Override
	public boolean equals(Object obj) {
		return super.equals(obj);
	}

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this);
	}
}
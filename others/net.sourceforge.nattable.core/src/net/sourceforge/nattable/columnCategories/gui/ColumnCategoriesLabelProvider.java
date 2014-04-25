package net.sourceforge.nattable.columnCategories.gui;

import java.util.List;

import net.sourceforge.nattable.columnCategories.Node;
import net.sourceforge.nattable.columnChooser.ColumnChooserUtils;
import net.sourceforge.nattable.columnChooser.ColumnEntry;
import net.sourceforge.nattable.util.ObjectUtils;

import org.eclipse.jface.viewers.LabelProvider;

public class ColumnCategoriesLabelProvider extends LabelProvider {

	List<ColumnEntry> hiddenEntries;
	
	public ColumnCategoriesLabelProvider(List<ColumnEntry> hiddenEntries) {
		this.hiddenEntries = hiddenEntries;
	}

	@Override
	public String getText(Object element) {
		Node node = (Node) element;
		switch (node.getType()) {
		case CATEGORY:
			return node.getData();
		case COLUMN:
			int index = Integer.parseInt(node.getData());
			ColumnEntry columnEntry = ColumnChooserUtils.find(hiddenEntries, index);
			if(ObjectUtils.isNull(columnEntry)){
				System.err.println(
						"Column index " + index + " is present " +
						"in the Column Categories model, " +
						"but not in the underlying data");
				return String.valueOf(index);
			}
			return columnEntry.getLabel();
		default:
			return "Unknown";
		}
	}
}

package net.sourceforge.nattable.columnCategories.gui;

import java.util.List;

import net.sourceforge.nattable.columnChooser.ColumnEntry;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;

/**
 * Provides visible columns as {@link ColumnEntry} objects. 
 */
public class VisibleColumnsProvider extends LabelProvider implements IStructuredContentProvider {

	List<ColumnEntry> visibleColumnsEntries;

	public VisibleColumnsProvider(List<ColumnEntry> visibleColumnsEntries) {
		this.visibleColumnsEntries = visibleColumnsEntries;
	}

	public Object[] getElements(Object inputElement) {
		return visibleColumnsEntries.toArray();
	}

	@Override
	public String getText(Object element) {
		return ((ColumnEntry) element).getLabel();
	}

	public void dispose() {
		// No op.
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		// No op.
	}

}

package net.sourceforge.nattable.columnCategories.gui;

import static net.sourceforge.nattable.util.ObjectUtils.isNull;

import java.util.List;

import net.sourceforge.nattable.columnCategories.ColumnCategoriesModel;
import net.sourceforge.nattable.columnCategories.Node;
import net.sourceforge.nattable.columnChooser.ColumnEntry;
import net.sourceforge.nattable.util.ObjectCloner;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

/**
 * Provides data to the tree viewer representation of Column categories.<br/>
 * Data is in the form of {@link Node} objects exposed from the {@link ColumnCategoriesModel}<br/>
 */
public class AvailableColumnCategoriesProvider implements ITreeContentProvider {

	private final ColumnCategoriesModel model;

	public AvailableColumnCategoriesProvider(ColumnCategoriesModel model) {
		this.model = (ColumnCategoriesModel) ObjectCloner.deepCopy(model);
	}

	/**
	 * Hide the given {@link ColumnEntry} (ies) i.e. do not show them in the viewer. 
	 */
	public void hideEntries(List<ColumnEntry> entriesToHide) {
		for (ColumnEntry hiddenColumnEntry : entriesToHide) {
			model.removeColumnIndex(hiddenColumnEntry.getIndex());
		}
	}

	public Object[] getChildren(Object parentElement) {
		return castToNode(parentElement).getChildren().toArray();
	}

	public Object getParent(Object element) {
		return castToNode(element).getParent();
	}

	public boolean hasChildren(Object element) {
		return castToNode(element).getNumberOfChildren() > 0;
	}

	public Object[] getElements(Object inputElement) {
		return isNull(model.getRootCategory()) 
					? new Object[]{} 
					: model.getRootCategory().getChildren().toArray();
	}

	private Node castToNode(Object element) {
		return (Node) element;
	}

	public void dispose() {
		// No op.
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		// No op.
	}

}

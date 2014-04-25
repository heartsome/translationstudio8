package net.sourceforge.nattable.selection;

import java.util.ArrayList;
import java.util.List;

import net.sourceforge.nattable.data.IRowDataProvider;
import net.sourceforge.nattable.layer.event.ILayerEventHandler;
import net.sourceforge.nattable.layer.event.IVisualChangeEvent;
import net.sourceforge.nattable.layer.event.RowStructuralChangeEvent;
import net.sourceforge.nattable.layer.event.RowStructuralRefreshEvent;
import net.sourceforge.nattable.sort.event.SortColumnEvent;
import net.sourceforge.nattable.util.ObjectUtils;

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;

/**
 * Preserves the selected row when the underlying data changes or column is sorted.<br/>
 *
 * <b>Example:</b>
 * 	Data 'A' is the 1st row. An update comes in and data 'A' moves to the 5th row.
 * 	This class clears current selection and selects row 5.
 *
 * @param <T> Type of row object beans in the underlying data source
 * @deprecated Use SelectionLayer.setSelectionModel(new RowSelectionModel(...)) instead
 */
public class RowSelectionPreserver<T> implements ILayerEventHandler<IVisualChangeEvent> {

	private final SelectionLayer selectionLayer;
	private final RowSelectionProvider<T> selectionProvider;
	private final IRowDataProvider<T> rowDataProvider;

	/** Track the selected objects */
	private List<T> selectedRowObjects = new ArrayList<T>();

	public RowSelectionPreserver(SelectionLayer selectionLayer, IRowDataProvider<T> rowDataProvider) {
		this.selectionLayer = selectionLayer;
		this.rowDataProvider = rowDataProvider;

		selectionProvider = new RowSelectionProvider<T>(selectionLayer, rowDataProvider, true);

		selectionProvider.addSelectionChangedListener(new ISelectionChangedListener() {
			@SuppressWarnings("unchecked")
			public void selectionChanged(SelectionChangedEvent event) {
				selectedRowObjects = ((StructuredSelection) event.getSelection()).toList();
			}
		});
	}

	/**
	 * Checks if all the previously selected objects are available in the data provider.
	 * Previously selected object might have been deleted from the list.
	 */
	private List<T> getValidSelections() {
		List<T> newSelection = new ArrayList<T>();

		for (T rowObj : selectedRowObjects) {
			int index = rowDataProvider.indexOfRowObject(rowObj);
			if (index != -1){
				newSelection.add(rowObj);
			}
		}
		return newSelection;
	}

	/**
	 * On a change in the underlying data:
	 * <ol>
	 * <li>Clears the selection
	 * <li>Re-select the row objects selected earlier.
	 * </ol>
	 */
	public void handleLayerEvent(IVisualChangeEvent event) {
		if(ObjectUtils.isEmpty(selectedRowObjects)){
			return;
		}

		if (event instanceof RowStructuralRefreshEvent
				|| event instanceof RowStructuralChangeEvent
				|| event instanceof SortColumnEvent) {
			selectionLayer.clear();
			selectionProvider.setSelection(new StructuredSelection(getValidSelections()));
		}
	}

	public Class<IVisualChangeEvent> getLayerEventClass() {
		return IVisualChangeEvent.class;
	}
}
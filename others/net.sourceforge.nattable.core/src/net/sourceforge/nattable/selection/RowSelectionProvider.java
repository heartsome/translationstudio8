package net.sourceforge.nattable.selection;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.sourceforge.nattable.coordinate.Range;
import net.sourceforge.nattable.data.IRowDataProvider;
import net.sourceforge.nattable.layer.ILayerListener;
import net.sourceforge.nattable.layer.event.ILayerEvent;
import net.sourceforge.nattable.selection.command.SelectRowsCommand;
import net.sourceforge.nattable.selection.event.ISelectionEvent;
import net.sourceforge.nattable.util.ObjectUtils;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;

public class RowSelectionProvider<T> implements ISelectionProvider, ILayerListener {
	
	private SelectionLayer selectionLayer;
	private final IRowDataProvider<T> rowDataProvider;
	private final boolean fullySelectedRowsOnly;
	private Set<ISelectionChangedListener> listeners = new HashSet<ISelectionChangedListener>();

	public RowSelectionProvider(SelectionLayer selectionLayer, IRowDataProvider<T> rowDataProvider) {
		this(selectionLayer, rowDataProvider, true);
	}
	
	public RowSelectionProvider(SelectionLayer selectionLayer, IRowDataProvider<T> rowDataProvider, boolean fullySelectedRowsOnly) {
		this.selectionLayer = selectionLayer;
		this.rowDataProvider = rowDataProvider;
		this.fullySelectedRowsOnly = fullySelectedRowsOnly;
		
		selectionLayer.addLayerListener(this);
	}
	
	public void addSelectionChangedListener(ISelectionChangedListener listener) {
		listeners.add(listener);
	}

	public ISelection getSelection() {
		return populateRowSelection(selectionLayer, rowDataProvider, fullySelectedRowsOnly);
	}

	public void removeSelectionChangedListener(ISelectionChangedListener listener) {
		listeners.remove(listener);
	}

	@SuppressWarnings("unchecked")
	public void setSelection(ISelection selection) {
		if (selectionLayer != null && selection instanceof IStructuredSelection) {
			selectionLayer.clear();
			List<T> rowObjects = ((IStructuredSelection) selection).toList();
			Set<Integer> rowPositions = new HashSet<Integer>();
			for (T rowObject : rowObjects) {
				int rowIndex = rowDataProvider.indexOfRowObject(rowObject);
				int rowPosition = selectionLayer.getRowPositionByIndex(rowIndex);
				rowPositions.add(Integer.valueOf(rowPosition));
			}
			selectionLayer.doCommand(new SelectRowsCommand(selectionLayer, 0, ObjectUtils.asIntArray(rowPositions), false, true));
		}
	}

	public void handleLayerEvent(ILayerEvent event) {
		if (event instanceof ISelectionEvent) {
			if (fullySelectedRowsOnly && selectionLayer.getFullySelectedRowPositions().length == 0) {
				return;
			}
			
			ISelection selection = getSelection();
			for (ISelectionChangedListener listener : listeners) {
				listener.selectionChanged(new SelectionChangedEvent(this, selection));
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	static StructuredSelection populateRowSelection(SelectionLayer selectionLayer, IRowDataProvider rowDataProvider, boolean fullySelectedRowsOnly) {
		List rows = new ArrayList();

		if (selectionLayer != null) {
			if (fullySelectedRowsOnly) {
				for (int rowPosition : selectionLayer.getFullySelectedRowPositions()) {
					addToSelection(rows, rowPosition, selectionLayer, rowDataProvider);
				}
			} else {
				Set<Range> rowRanges = selectionLayer.getSelectedRows();
				for (Range rowRange : rowRanges) {
					for (int rowPosition = rowRange.start; rowPosition < rowRange.end; rowPosition++) {
						addToSelection(rows, rowPosition, selectionLayer, rowDataProvider);
					}
				}
			}
		}
		
		return new StructuredSelection(rows);
	}
	
	@SuppressWarnings("unchecked")
	private static void addToSelection(List<Object> rows, int rowPosition, SelectionLayer selectionLayer, IRowDataProvider rowDataProvider) {
		int rowIndex = selectionLayer.getRowIndexByPosition(rowPosition);
		Object rowObject = rowDataProvider.getRowObject(rowIndex);
		rows.add(rowObject);
	}

}

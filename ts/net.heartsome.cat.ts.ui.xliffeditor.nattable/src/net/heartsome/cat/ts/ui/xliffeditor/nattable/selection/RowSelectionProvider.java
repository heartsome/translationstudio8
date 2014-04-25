package net.heartsome.cat.ts.ui.xliffeditor.nattable.selection;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.heartsome.cat.ts.ui.xliffeditor.nattable.search.coordinate.ActiveCellRegion;
import net.sourceforge.nattable.layer.ILayerListener;
import net.sourceforge.nattable.layer.event.ILayerEvent;
import net.sourceforge.nattable.selection.SelectionLayer;
import net.sourceforge.nattable.selection.command.SelectRowsCommand;
import net.sourceforge.nattable.selection.event.ISelectionEvent;
import net.sourceforge.nattable.util.ObjectUtils;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;

public class RowSelectionProvider implements ISelectionProvider, ILayerListener {

	private SelectionLayer selectionLayer;
	private final boolean fullySelectedRowsOnly;
	private Set<ISelectionChangedListener> listeners = new HashSet<ISelectionChangedListener>();

	public RowSelectionProvider(SelectionLayer selectionLayer) {
		this(selectionLayer, true);
	}

	public RowSelectionProvider(SelectionLayer selectionLayer, boolean fullySelectedRowsOnly) {
		this.selectionLayer = selectionLayer;
		this.fullySelectedRowsOnly = fullySelectedRowsOnly;

		selectionLayer.addLayerListener(this);
	}

	public void addSelectionChangedListener(ISelectionChangedListener listener) {
		listeners.add(listener);
	}

	public ISelection getSelection() {
		int[] rowPositions = selectionLayer.getFullySelectedRowPositions();
		if (rowPositions.length > 0) {
			Arrays.sort(rowPositions);
			int rowPosition = rowPositions[rowPositions.length - 1];
			int rowIndex = selectionLayer.getRowIndexByPosition(rowPosition);
			return new StructuredSelection(rowIndex);
		}

		return new StructuredSelection();
	}

	public void removeSelectionChangedListener(ISelectionChangedListener listener) {
		listeners.remove(listener);
	}

	@SuppressWarnings("unchecked")
	public void setSelection(ISelection selection) {
		if (selectionLayer != null && selection instanceof IStructuredSelection) {
			selectionLayer.clear();
			List<Integer> rowIndexs = ((IStructuredSelection) selection).toList();
			Set<Integer> rowPositions = new HashSet<Integer>();
			for (Integer rowIndex : rowIndexs) {
				int rowPosition = selectionLayer.getRowPositionByIndex(rowIndex);
				rowPositions.add(Integer.valueOf(rowPosition));
			}
			selectionLayer.doCommand(new SelectRowsCommand(selectionLayer, 0, ObjectUtils.asIntArray(rowPositions),
					false, true));
		}
	}
	private int currentRowPosition = -1;
	public void handleLayerEvent(ILayerEvent event) {
		if (event instanceof ISelectionEvent) {
			int[] rowPositions = selectionLayer.getFullySelectedRowPositions();
			if (fullySelectedRowsOnly && rowPositions.length == 0) {
				ActiveCellRegion.setActiveCellRegion(null);
				return;
			}
			
			Arrays.sort(rowPositions);
			int rowPosition = rowPositions[rowPositions.length - 1];
			if(rowPosition == currentRowPosition){
				return;
			}
			currentRowPosition = rowPosition;
			int rowIndex = selectionLayer.getRowIndexByPosition(rowPosition);
			ISelection selection = new StructuredSelection(rowIndex);
			SelectionChangedEvent e = new SelectionChangedEvent(this, selection);
			for (ISelectionChangedListener listener : listeners) {
				listener.selectionChanged(e);
			}
		}
	}

}

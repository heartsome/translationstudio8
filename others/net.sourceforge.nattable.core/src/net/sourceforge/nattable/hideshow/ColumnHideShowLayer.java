package net.sourceforge.nattable.hideshow;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;

import net.sourceforge.nattable.hideshow.command.ColumnHideCommandHandler;
import net.sourceforge.nattable.hideshow.command.MultiColumnHideCommandHandler;
import net.sourceforge.nattable.hideshow.command.MultiColumnShowCommandHandler;
import net.sourceforge.nattable.hideshow.command.ShowAllColumnsCommandHandler;
import net.sourceforge.nattable.hideshow.event.HideColumnPositionsEvent;
import net.sourceforge.nattable.hideshow.event.ShowColumnPositionsEvent;
import net.sourceforge.nattable.layer.IUniqueIndexLayer;

public class ColumnHideShowLayer extends AbstractColumnHideShowLayer {
	
	public static final String PERSISTENCE_KEY_HIDDEN_COLUMN_INDEXES = ".hiddenColumnIndexes";
	
	private final Set<Integer> hiddenColumnIndexes;

	public ColumnHideShowLayer(IUniqueIndexLayer underlyingLayer) {
		super(underlyingLayer);
		this.hiddenColumnIndexes = new TreeSet<Integer>();
		
		registerCommandHandler(new MultiColumnHideCommandHandler(this));
		registerCommandHandler(new ColumnHideCommandHandler(this));
		registerCommandHandler(new ShowAllColumnsCommandHandler(this));
		registerCommandHandler(new MultiColumnShowCommandHandler(this));
	}

	// Persistence
	
	@Override
	public void saveState(String prefix, Properties properties) {
		if (hiddenColumnIndexes.size() > 0) {
			StringBuilder strBuilder = new StringBuilder();
			for (Integer index : hiddenColumnIndexes) {
				strBuilder.append(index);
				strBuilder.append(',');
			}
			properties.setProperty(prefix + PERSISTENCE_KEY_HIDDEN_COLUMN_INDEXES, strBuilder.toString());
		}
		
		super.saveState(prefix, properties);
	}
	
	@Override
	public void loadState(String prefix, Properties properties) {
		String property = properties.getProperty(prefix + PERSISTENCE_KEY_HIDDEN_COLUMN_INDEXES);
		if (property != null) {
			hiddenColumnIndexes.clear();
			
			StringTokenizer tok = new StringTokenizer(property, ",");
			while (tok.hasMoreTokens()) {
				String index = tok.nextToken();
				hiddenColumnIndexes.add(Integer.valueOf(index));
			}
		}
		
		super.loadState(prefix, properties);
	}
	
	// Hide/show

	@Override
	public boolean isColumnIndexHidden(int columnIndex) {
		return hiddenColumnIndexes.contains(Integer.valueOf(columnIndex));
	}

	@Override
	public Collection<Integer> getHiddenColumnIndexes(){
		return hiddenColumnIndexes;
	}

	public void hideColumnPositions(Collection<Integer> columnPositions) {
		Set<Integer> columnIndexes = new HashSet<Integer>();
		for (Integer columnPosition : columnPositions) {
			columnIndexes.add(Integer.valueOf(getColumnIndexByPosition(columnPosition.intValue())));
		}
		hiddenColumnIndexes.addAll(columnIndexes);
		invalidateCache();
		fireLayerEvent(new HideColumnPositionsEvent(this, columnPositions));
	}

	public void showColumnIndexes(int[] columnIndexes) {
		Set<Integer> columnIndexesSet = new HashSet<Integer>();
		for (int i = 0; i < columnIndexes.length; i++) {
			columnIndexesSet.add(Integer.valueOf(columnIndexes[i]));
		}
		hiddenColumnIndexes.removeAll(columnIndexesSet);
		invalidateCache();
		fireLayerEvent(new ShowColumnPositionsEvent(this, getColumnPositionsByIndexes(columnIndexes)));
	}

	protected void showColumnIndexes(Collection<Integer> columnIndexes) {
		for (int columnIndex : columnIndexes) {
			hiddenColumnIndexes.remove(Integer.valueOf(columnIndex));
		}
		invalidateCache();
		// Since we are exposing this method for showing individual columns, a structure event must be fired here.
		fireLayerEvent(new ShowColumnPositionsEvent(this, columnIndexes));
	}

	public void showAllColumns() {
		Collection<Integer> hiddenColumns = new ArrayList<Integer>(hiddenColumnIndexes);
		hiddenColumnIndexes.clear();
		invalidateCache();
		fireLayerEvent(new ShowColumnPositionsEvent(this, hiddenColumns));
	}
	
	private Collection<Integer> getColumnPositionsByIndexes(int[] columnIndexes) {
		Collection<Integer> columnPositions = new HashSet<Integer>();
		for (int columnIndex : columnIndexes) {
			columnPositions.add(Integer.valueOf(getColumnPositionByIndex(columnIndex)));
		}
		return columnPositions;
	}
	
}

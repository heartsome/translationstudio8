package net.sourceforge.nattable.layer.cell;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import net.sourceforge.nattable.config.IConfigRegistry;
import net.sourceforge.nattable.edit.editor.ICellEditor;
import net.sourceforge.nattable.layer.ILayer;
import net.sourceforge.nattable.layer.LabelStack;
import net.sourceforge.nattable.painter.cell.ICellPainter;
import net.sourceforge.nattable.persistence.IPersistable;
import net.sourceforge.nattable.style.IStyle;

/**
 * Registers/Adds configuration labels for a given column (by index).<br/>
 * Custom {@link ICellEditor}, {@link ICellPainter}, {@link IStyle} can then <br/>
 * be registered in the {@link IConfigRegistry} against these labels. <br/>
 * 
 * Also @see {@link RowOverrideLabelAccumulator} 
 */
public class ColumnOverrideLabelAccumulator extends AbstractOverrider implements IPersistable {
	
	public static final String PERSISTENCE_KEY = ".columnOverrideLabelAccumulator";
	private final ILayer layer;

	public ColumnOverrideLabelAccumulator(ILayer layer) {
		this.layer = layer;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void accumulateConfigLabels(LabelStack configLabels, int columnPosition, int rowPosition) {
		int columnIndex = layer.getColumnIndexByPosition(columnPosition);
		List<String> overrides = getOverrides(Integer.valueOf(columnIndex));
		if (overrides != null) {
			for (String configLabel : overrides) {
				configLabels.addLabel(configLabel);
			}
		}
	}

	/**
	 * Register labels to be contributed a column. This label will be applied to<br/>
	 * all cells in the column.
	 */
	public void registerColumnOverrides(int columnIndex, String... configLabels) {
		super.registerOverrides(Integer.valueOf(columnIndex), configLabels);
	}
	
	/** 
	 * Save the overrides to a properties file. A line is stored for every column.<br/>
	 * 
	 * Example for column 0:
	 * prefix.columnOverrideLabelAccumulator.0 = LABEL1,LABEL2
	 */
	public void saveState(String prefix, Properties properties) {
		Map<Serializable, List<String>> overrides = getOverrides();

		for (Map.Entry<Serializable, List<String>> entry : overrides.entrySet()) {
			StringBuilder strBuilder = new StringBuilder();
			for (String columnLabel : entry.getValue()) {
				strBuilder.append(columnLabel);
				strBuilder.append(VALUE_SEPARATOR);
			}
			//Strip the last comma
			String propertyValue = strBuilder.toString();
			if(propertyValue.endsWith(VALUE_SEPARATOR)){
				propertyValue = propertyValue.substring(0, propertyValue.length() - 1);
			}
			String propertyKey = prefix + PERSISTENCE_KEY + DOT + entry.getKey();
			properties.setProperty(propertyKey, propertyValue);
		}
	}

	/**
	 * Load the overrides state from the given properties file.<br/>
	 * @see CellOverrideLabelAccumulator#saveState() 
	 */
	public void loadState(String prefix, Properties properties) {
		Set<Object> keySet = properties.keySet();
		for (Object key : keySet) {
			String keyString = (String) key;
			if(keyString.contains(PERSISTENCE_KEY)){
				String labelsFromPropertyValue = properties.getProperty(keyString).trim();
				String columnIndexFromKey = keyString.substring(keyString.lastIndexOf(DOT) + 1);
				registerColumnOverrides(Integer.parseInt(columnIndexFromKey), labelsFromPropertyValue.split(VALUE_SEPARATOR));
			}
		}
	}	
}

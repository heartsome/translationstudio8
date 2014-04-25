package net.sourceforge.nattable.style.editor.command;

import static net.sourceforge.nattable.config.CellConfigAttributes.CELL_STYLE;
import static net.sourceforge.nattable.style.DisplayMode.NORMAL;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import net.sourceforge.nattable.command.AbstractLayerCommandHandler;
import net.sourceforge.nattable.config.IConfigRegistry;
import net.sourceforge.nattable.layer.ILayer;
import net.sourceforge.nattable.layer.cell.ColumnOverrideLabelAccumulator;
import net.sourceforge.nattable.persistence.IPersistable;
import net.sourceforge.nattable.persistence.StylePersistor;
import net.sourceforge.nattable.style.CellStyleAttributes;
import net.sourceforge.nattable.style.Style;
import net.sourceforge.nattable.style.editor.ColumnStyleEditorDialog;

import org.eclipse.swt.widgets.Display;

/**
 * 
 * 1. Captures a new style using the <code>StyleEditorDialog</code> 
 * 2. Registers style from step 1 in the <code>ConfigRegistry</code> with a new label 
 * 3. Applies the label from step 2 to all cells in the selected column
 * 
 */
public class DisplayColumnStyleEditorCommandHandler extends AbstractLayerCommandHandler<DisplayColumnStyleEditorCommand> implements IPersistable {
	
	protected static final String PERSISTENCE_PREFIX = "userDefinedColumnStyle";
	protected static final String USER_EDITED_STYLE_LABEL = "USER_EDITED_STYLE_FOR_INDEX_";

	protected ColumnOverrideLabelAccumulator columnLabelAccumulator;
	protected ColumnStyleEditorDialog dialog;
	private final IConfigRegistry configRegistry;
	protected final Map<String, Style> stylesToPersist = new HashMap<String, Style>();

	public DisplayColumnStyleEditorCommandHandler(ColumnOverrideLabelAccumulator labelAccumulator, IConfigRegistry configRegistry) {
		this.columnLabelAccumulator = labelAccumulator;
		this.configRegistry = configRegistry;
	}

	@Override
	public boolean doCommand(DisplayColumnStyleEditorCommand command) {
		ILayer nattableLayer = command.getNattableLayer();
		int columnIndex = nattableLayer.getColumnIndexByPosition(command.columnPosition);
		
		// Column style
		Style slectedCellStyle = (Style) configRegistry.getConfigAttribute(CELL_STYLE, NORMAL, USER_EDITED_STYLE_LABEL + columnIndex);
		
		dialog = new ColumnStyleEditorDialog(Display.getCurrent().getActiveShell(), slectedCellStyle);
		dialog.open();

		if(dialog.isCancelPressed()) {
			return true;
		}
		
		applySelectedStyleToColumn(command, columnIndex);
		return true;
	}

	public Class<DisplayColumnStyleEditorCommand> getCommandClass() {
		return DisplayColumnStyleEditorCommand.class;
	}

	protected void applySelectedStyleToColumn(DisplayColumnStyleEditorCommand command, int columnIndex) {
		// Read the edited styles
		Style newColumnCellStyle = dialog.getNewColumCellStyle(); 
		
		if (newColumnCellStyle == null) {
			stylesToPersist.remove(getConfigLabel(columnIndex));
		} else {
			newColumnCellStyle.setAttributeValue(CellStyleAttributes.BORDER_STYLE, dialog.getNewColumnBorderStyle());
			stylesToPersist.put(getConfigLabel(columnIndex), newColumnCellStyle);
		}
		configRegistry.registerConfigAttribute(CELL_STYLE, newColumnCellStyle, NORMAL, getConfigLabel(columnIndex));
		columnLabelAccumulator.registerColumnOverrides(columnIndex, getConfigLabel(columnIndex));
	}

	protected String getConfigLabel(int columnIndex) {
		return USER_EDITED_STYLE_LABEL + columnIndex;
	}

	public void loadState(String prefix, Properties properties) {
		prefix = prefix + DOT + PERSISTENCE_PREFIX;
		Set<Object> keySet = properties.keySet();

		for (Object key : keySet) {
			String keyString = (String) key;

			// Relevant Key
			if (keyString.contains(PERSISTENCE_PREFIX)) {
				int colIndex = parseColumnIndexFromKey(keyString);

				// Has the config label been processed
				if (!stylesToPersist.keySet().contains(getConfigLabel(colIndex))) {
					Style savedStyle = StylePersistor.loadStyle(prefix + DOT + getConfigLabel(colIndex), properties);

					configRegistry.registerConfigAttribute(CELL_STYLE, savedStyle, NORMAL, getConfigLabel(colIndex));
					stylesToPersist.put(getConfigLabel(colIndex), savedStyle);
				}
			}
		}
	}

	protected int parseColumnIndexFromKey(String keyString) {
		int colLabelStartIndex = keyString.indexOf(USER_EDITED_STYLE_LABEL);
		String columnConfigLabel = keyString.substring(colLabelStartIndex, keyString.indexOf('.', colLabelStartIndex));
		int lastUnderscoreInLabel = columnConfigLabel.lastIndexOf('_', colLabelStartIndex);

		return Integer.parseInt(columnConfigLabel.substring(lastUnderscoreInLabel + 1));
	}

	public void saveState(String prefix, Properties properties) {
		prefix = prefix + DOT + PERSISTENCE_PREFIX;

		for (Map.Entry<String, Style> labelToStyle : stylesToPersist.entrySet()) {
			Style style = labelToStyle.getValue();
			String label = labelToStyle.getKey();

			StylePersistor.saveStyle(prefix + DOT + label, properties, style);
		}
	}
}

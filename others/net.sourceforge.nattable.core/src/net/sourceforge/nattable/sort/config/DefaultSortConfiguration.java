package net.sourceforge.nattable.sort.config;

import net.sourceforge.nattable.config.CellConfigAttributes;
import net.sourceforge.nattable.config.DefaultComparator;
import net.sourceforge.nattable.config.IConfigRegistry;
import net.sourceforge.nattable.config.IConfiguration;
import net.sourceforge.nattable.grid.GridRegion;
import net.sourceforge.nattable.layer.ILayer;
import net.sourceforge.nattable.painter.cell.ICellPainter;
import net.sourceforge.nattable.painter.cell.decorator.BeveledBorderDecorator;
import net.sourceforge.nattable.sort.SortConfigAttributes;
import net.sourceforge.nattable.sort.action.SortColumnAction;
import net.sourceforge.nattable.sort.painter.SortableHeaderTextPainter;
import net.sourceforge.nattable.style.DisplayMode;
import net.sourceforge.nattable.ui.binding.UiBindingRegistry;
import net.sourceforge.nattable.ui.matcher.MouseEventMatcher;

import org.eclipse.swt.SWT;

public class DefaultSortConfiguration implements IConfiguration {
	
	public static final String SORT_DOWN_CONFIG_TYPE = "SORT_DOWN";
	public static final String SORT_UP_CONFIG_TYPE = "SORT_UP";
	
	/** The sort sequence can be appended to this base */
	public static final String SORT_SEQ_CONFIG_TYPE = "SORT_SEQ_";
	
	public void configureLayer(ILayer layer) {}
	
	public void configureRegistry(IConfigRegistry configRegistry) {
		configRegistry.registerConfigAttribute(SortConfigAttributes.SORT_COMPARATOR, new DefaultComparator());
		
		ICellPainter sortableHeaderCellPainter = new BeveledBorderDecorator(new SortableHeaderTextPainter());
		configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_PAINTER, sortableHeaderCellPainter, DisplayMode.NORMAL, SORT_DOWN_CONFIG_TYPE);
		configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_PAINTER, sortableHeaderCellPainter, DisplayMode.NORMAL, SORT_UP_CONFIG_TYPE);
	}

	public void configureUiBindings(UiBindingRegistry uiBindingRegistry) {
		uiBindingRegistry.registerSingleClickBinding(
				new MouseEventMatcher(SWT.ALT, GridRegion.COLUMN_HEADER.toString(), 1),	new SortColumnAction(false));
		
		uiBindingRegistry.registerSingleClickBinding(
				new MouseEventMatcher(SWT.ALT | SWT.SHIFT, GridRegion.COLUMN_HEADER.toString(), 1), new SortColumnAction(true));
	}
	
}

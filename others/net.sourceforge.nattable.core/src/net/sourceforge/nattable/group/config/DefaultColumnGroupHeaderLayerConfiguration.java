package net.sourceforge.nattable.group.config;

import net.sourceforge.nattable.config.CellConfigAttributes;
import net.sourceforge.nattable.config.IConfigRegistry;
import net.sourceforge.nattable.config.IConfiguration;
import net.sourceforge.nattable.grid.GridRegion;
import net.sourceforge.nattable.group.ColumnGroupModel;
import net.sourceforge.nattable.group.action.ColumnGroupExpandCollapseAction;
import net.sourceforge.nattable.group.action.ColumnGroupHeaderReorderDragMode;
import net.sourceforge.nattable.group.action.ColumnHeaderReorderDragMode;
import net.sourceforge.nattable.group.action.CreateColumnGroupAction;
import net.sourceforge.nattable.group.action.UngroupColumnsAction;
import net.sourceforge.nattable.group.painter.ColumnGroupHeaderTextPainter;
import net.sourceforge.nattable.layer.ILayer;
import net.sourceforge.nattable.painter.cell.decorator.BeveledBorderDecorator;
import net.sourceforge.nattable.style.DisplayMode;
import net.sourceforge.nattable.ui.binding.UiBindingRegistry;
import net.sourceforge.nattable.ui.matcher.KeyEventMatcher;
import net.sourceforge.nattable.ui.matcher.MouseEventMatcher;

import org.eclipse.swt.SWT;

public class DefaultColumnGroupHeaderLayerConfiguration implements IConfiguration {

	private final ColumnGroupModel columnGroupModel;

	public DefaultColumnGroupHeaderLayerConfiguration(final ColumnGroupModel columnGroupModel) {
		this.columnGroupModel = columnGroupModel;
	}

	public void configureLayer(ILayer layer) {
		// No op
	}

	public void configureRegistry(IConfigRegistry configRegistry) {
		configRegistry.registerConfigAttribute(
				CellConfigAttributes.CELL_PAINTER,
				new BeveledBorderDecorator(new ColumnGroupHeaderTextPainter(columnGroupModel)),
				DisplayMode.NORMAL,
				GridRegion.COLUMN_GROUP_HEADER
		);
	}

	public void configureUiBindings(UiBindingRegistry uiBindingRegistry) {
		// Column Group Header is a part of the Group Header.
		// Register the 'column group header matcher' first so that it gets
		// picked up before the more general 'column header matcher'.
		uiBindingRegistry.registerMouseDragMode(
                MouseEventMatcher.columnGroupHeaderLeftClick(SWT.NONE),
                new ColumnGroupHeaderReorderDragMode(columnGroupModel));

		uiBindingRegistry.registerMouseDragMode(
                MouseEventMatcher.columnHeaderLeftClick(SWT.NONE),
                new ColumnHeaderReorderDragMode(columnGroupModel));


		uiBindingRegistry.registerDoubleClickBinding(
				MouseEventMatcher.columnGroupHeaderLeftClick(SWT.NONE),
				new ColumnGroupExpandCollapseAction());

		uiBindingRegistry.registerKeyBinding(new KeyEventMatcher(SWT.CTRL, 'g'), new CreateColumnGroupAction());
		uiBindingRegistry.registerKeyBinding(new KeyEventMatcher(SWT.CTRL, 'u'), new UngroupColumnsAction());
	}

}

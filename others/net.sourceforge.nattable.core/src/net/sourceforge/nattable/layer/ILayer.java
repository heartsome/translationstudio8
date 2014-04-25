package net.sourceforge.nattable.layer;

import java.util.Collection;
import java.util.Properties;

import net.sourceforge.nattable.NatTable;
import net.sourceforge.nattable.command.ILayerCommand;
import net.sourceforge.nattable.config.ConfigRegistry;
import net.sourceforge.nattable.coordinate.Range;
import net.sourceforge.nattable.layer.cell.LayerCell;
import net.sourceforge.nattable.layer.event.ILayerEvent;
import net.sourceforge.nattable.layer.event.IVisualChangeEvent;
import net.sourceforge.nattable.painter.layer.ILayerPainter;
import net.sourceforge.nattable.persistence.IPersistable;
import net.sourceforge.nattable.selection.SelectionLayer;
import net.sourceforge.nattable.style.DisplayMode;
import net.sourceforge.nattable.ui.binding.UiBindingRegistry;
import net.sourceforge.nattable.util.IClientAreaProvider;

import org.eclipse.swt.graphics.Rectangle;

/**
 * <p>
 * A Layer is a rectangular region of grid cells. A layer has methods to access its columns, rows, width and height. A
 * layer can be stacked on top of another layer in order to expose a transformed view of its underlying layer's grid
 * cell structure.
 * </p>
 * <p>
 * Columns and rows in a layer are referenced either by <b>position</b> or <b>index</b>. The position of a column/row in
 * a layer corresponds to the physical location of the column/row in the layer. The index of a column/row in a layer
 * corresponds to the location of the column/row in the lowest level layer in the layer stack. These concepts are
 * illustrated by the following example:
 * </p>
 * <pre>
 * Hide Layer C
 * 0 1 2 3 4 <- column positions
 * 1 0 3 4 5 <- column indexes
 *
 * Reorder Layer B
 * 0 1 2 3 4 5 <- column positions
 * 2 1 0 3 4 5 <- column indexes
 *
 * Data Layer A
 * 0 1 2 3 4 5 <- column positions
 * 0 1 2 3 4 5 <- column indexes
 * </pre>
 * <p>
 * In the above example, Hide Layer C is stacked on top of Reorder Layer B, which is in turn stacked on top of Data
 * Layer A. The positions in Data Layer A are the same as its indexes, because it is the lowest level layer in the
 * stack. Reorder Layer B reorders column 0 of its underlying layer (Data Layer A) after column 2 of its underlying
 * layer. Hide Layer C hides the first column of its underlying layer (Reorder Layer B).
 * </p>
 * <p>
 * Layers can also be laterally composed into larger layers. For instance, the standard grid layer is composed of a
 * body layer, column header layer, row header layer, and corner layer:
 * </p>
 * <table border=1>
 *   <tr><td>corner</td><td>column header</td></tr>
 *   <tr><td>row header</td><td>body</td></tr>
 * </table>
 *
 * @see CompositeLayer
 */
public interface ILayer extends ILayerListener, IPersistable {

	// Persistence

	/**
	 * Persistables registered with a layer will have a chance to write their data out to the
	 * {@link Properties} instance when the layer is persisted.
	 */
	public void registerPersistable(IPersistable persistable);

	public void unregisterPersistable(IPersistable persistable);

	// Configuration

	/**
	 * Every layer gets this call back, starting at the top of the stack. This is triggered
	 * by the {@link NatTable#configure()} method. This is an opportunity to add
	 * any key/mouse bindings and other general configuration.
	 *
	 * @param configRegistry instance owned by {@link NatTable}
	 * @param uiBindingRegistry instance owned by {@link NatTable}
	 */
	public void configure(ConfigRegistry configRegistry, UiBindingRegistry uiBindingRegistry);

	// Region

	/**
	 * Layer can apply its own labels to any cell it wishes.
	 */
	public LabelStack getRegionLabelsByXY(int x, int y);

	// Commands

	/**
	 * Opportunity to respond to a command as it flows down the stack. If the layer
	 * is not interested in the command it should allow the command to keep traveling
	 * down the stack.
	 *
	 * Note: Before the layer can process a command it <i>must</i> convert the
	 * command to its local co-ordinates using {@link ILayerCommand#convertToTargetLayer(ILayer)}
	 *
	 * @return true if the command has been handled, false otherwise
	 */
	public boolean doCommand(ILayerCommand command);

	// Events

	/**
	 * Events can be fired to notify other components of the grid.<br/>
	 * Events travel <i>up</i> the layer stack and may cause a repaint.
	 *
	 * Example: When the contents of the grid change {@link IVisualChangeEvent} can be
	 * fired to notify other layers to refresh their caches etc.
	 */
	public void fireLayerEvent(ILayerEvent event);

	public void addLayerListener(ILayerListener listener);

	public void removeLayerListener(ILayerListener listener);

	public ILayerPainter getLayerPainter();

	// Client area

	public IClientAreaProvider getClientAreaProvider();

	public void setClientAreaProvider(IClientAreaProvider clientAreaProvider);

	// Horizontal features

	// Columns

	/**
	 * Returns the number of columns in this coordinate model.
	 */
	public int getColumnCount();

	public int getPreferredColumnCount();

	/**
	 * Gets the underlying non-transformed column index for the given column position.
	 * @param columnPosition a column position relative to this coordinate model
	 * @return an underlying non-transformed column index, or -1 if the given column position does not exist within this
	 * coordinate system
	 */
	public int getColumnIndexByPosition(int columnPosition);

	/**
	 * Convert a column position to the coordinates of the underlying layer.
	 * This is possible since each layer is aware of its underlying layer.
	 * @param localColumnPosition column position in local (the layer's own) coordinates
	 */
	public int localToUnderlyingColumnPosition(int localColumnPosition);

	public int underlyingToLocalColumnPosition(ILayer sourceUnderlyingLayer, int underlyingColumnPosition);
	
	public Collection<Range> underlyingToLocalColumnPositions(ILayer sourceUnderlyingLayer, Collection<Range> underlyingColumnPositionRanges);

	// Width

	/**
	 * Returns the total width of this layer.
	 */
	public int getWidth();

	public int getPreferredWidth();

	/**
	 * Returns the width of the given column.
	 * @param columnPosition the column position relative to the associated coordinate system
	 * @return the width of the column, in pixels
	 */
	public int getColumnWidthByPosition(int columnPosition);

	// Column resize

	public boolean isColumnPositionResizable(int columnPosition);

	// X

	/**
	 * Returns the column position that contains the given x coordinate.
	 * @param x a horizontal pixel location relative to the pixel boundary of this layer
	 * @return a column position relative to the associated coordinate system, or -1 if there is no column that contains x
	 */
	public int getColumnPositionByX(int x);

	/**
	 * @param columnPosition
	 * @return starting X coordinate of the column position
	 */
	public int getStartXOfColumnPosition(int columnPosition);

	// Underlying

	public Collection<ILayer> getUnderlyingLayersByColumnPosition(int columnPosition);

	// Vertical features

	// Rows

	/**
	 * Returns the number of rows in this coordinate model.
	 */
	public int getRowCount();

	public int getPreferredRowCount();

	/**
	 * Gets the underlying non-transformed row index for the given row position.
	 * @param rowPosition a row position relative to this coordinate model
	 * @return an underlying non-transformed row index, or -1 if the given row position does not exist within this
	 * coordinate system
	 */
	public int getRowIndexByPosition(int rowPosition);

	public int localToUnderlyingRowPosition(int localRowPosition);

	public int underlyingToLocalRowPosition(ILayer sourceUnderlyingLayer, int underlyingRowPosition);
	
	public Collection<Range> underlyingToLocalRowPositions(ILayer sourceUnderlyingLayer, Collection<Range> underlyingRowPositionRanges);

	// Height

	/**
	 * Returns the total height of this layer.
	 */
	public int getHeight();

	public int getPreferredHeight();

	/**
	 * Returns the height of the given row.
	 * @param rowPosition the row position relative to the associated coordinate system
	 * @return the height of the row, in pixels
	 */
	public int getRowHeightByPosition(int rowPosition);

	// Row resize

	public boolean isRowPositionResizable(int rowPosition);

	// Y

	/**
	 * Returns the row position that contains the given y coordinate.
	 * @param y a vertical pixel location relative to the pixel boundary of this layer
	 * @return a row position relative to the associated coordinate system, or -1 if there is no row that contains y
	 */
	public int getRowPositionByY(int y);

	public int getStartYOfRowPosition(int rowPosition);

	// Underlying

	public Collection<ILayer> getUnderlyingLayersByRowPosition(int rowPosition);

	// Cell features

	public LayerCell getCellByPosition(int columnPosition, int rowPosition);

	public Rectangle getBoundsByPosition(int columnPosition, int rowPosition);

	/**
	 * @return {@link DisplayMode} for the cell at the given position.<br/>
	 * 	The {@link DisplayMode} affects the settings out of the {@link ConfigRegistry}.
	 * 	Display mode is <i>NORMAL</i> by default.<br/>
	 *
	 *  <b>Example:</b> {@link SelectionLayer} overrides this to return the <i>SELECT</i>
	 *  label for cells which are selected.
	 */
	public String getDisplayModeByPosition(int columnPosition, int rowPosition);

	public LabelStack getConfigLabelsByPosition(int columnPosition, int rowPosition);

	public Object getDataValueByPosition(int columnPosition, int rowPosition);

	public ILayer getUnderlyingLayerByPosition(int columnPosition, int rowPosition);

}

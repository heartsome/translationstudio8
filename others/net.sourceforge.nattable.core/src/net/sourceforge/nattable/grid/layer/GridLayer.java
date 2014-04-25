package net.sourceforge.nattable.grid.layer;

import net.sourceforge.nattable.command.ILayerCommand;
import net.sourceforge.nattable.export.excel.command.ExportToExcelCommandHandler;
import net.sourceforge.nattable.grid.GridRegion;
import net.sourceforge.nattable.grid.command.AutoResizeColumnCommandHandler;
import net.sourceforge.nattable.grid.command.AutoResizeRowCommandHandler;
import net.sourceforge.nattable.grid.layer.config.DefaultGridLayerConfiguration;
import net.sourceforge.nattable.layer.CompositeLayer;
import net.sourceforge.nattable.layer.ILayer;
import net.sourceforge.nattable.print.command.PrintCommandHandler;
import net.sourceforge.nattable.selection.command.SelectCellCommand;

/**
 * Top level layer. It is composed of the smaller child layers: RowHeader,
 * ColumnHeader, Corner and Body It does not have its own coordinate system
 * unlike the other layers. It simply delegates most functions to its child
 * layers.
 */
public class GridLayer extends CompositeLayer {

	public GridLayer(ILayer bodyLayer, ILayer columnHeaderLayer, ILayer rowHeaderLayer, ILayer cornerLayer) {
		this(bodyLayer, columnHeaderLayer, rowHeaderLayer, cornerLayer, true);
	}
	
	public GridLayer(ILayer bodyLayer, ILayer columnHeaderLayer, ILayer rowHeaderLayer, ILayer cornerLayer, boolean useDefaultConfiguration) {
		super(2, 2);
		
		setBodyLayer(bodyLayer);
		setColumnHeaderLayer(columnHeaderLayer);
		setRowHeaderLayer(rowHeaderLayer);
		setCornerLayer(cornerLayer);
		
		init(useDefaultConfiguration);
	}

	protected GridLayer(boolean useDefaultConfiguration) {
		super(2, 2);
		init(useDefaultConfiguration);
	}

	protected void init(boolean useDefaultConfiguration) {
		registerCommandHandlers();
		
		if (useDefaultConfiguration) {
			addConfiguration(new DefaultGridLayerConfiguration(this));
		}
	}

	protected void registerCommandHandlers() {
		registerCommandHandler(new PrintCommandHandler(this));
		registerCommandHandler(new ExportToExcelCommandHandler(this));
		registerCommandHandler(new AutoResizeColumnCommandHandler(this));
		registerCommandHandler(new AutoResizeRowCommandHandler(this));
	}
	
	/**
	 * How the GridLayer processes commands is very important. <strong>Do not
	 * change this unless you know what you are doing and understand the full
	 * ramifications of your change. Otherwise your grid will not behave
	 * correctly!</strong>
	 * 
	 * The Body is always given the first chance to process a command. There are
	 * two reasons for this: (1) most commands (80%) are destined for the body
	 * anyways so it's faster to check there first (2) the other layers all
	 * transitively depend on the body so it's not wise to ask them to do stuff
	 * until after the body has done it. This is especially true of grid
	 * initialization where the body must be initialized before any of its
	 * dependent layers.
	 * 
	 * Because of this, if you want to intercept well-known commands to
	 * implement custom behavior (for example, you want to intercept the
	 * {@link SelectCellCommand}) then <strong>you must inject your special
	 * layer into the body. </strong> An injected column or row header will
	 * never see the command because it will be consumed first by the body. In
	 * practice, it's a good idea to implement all your command-handling logic
	 * in the body.
	 **/
	@Override
	protected boolean doCommandOnChildLayers(ILayerCommand command) {
		if (doCommandOnChildLayer(command, getBodyLayer())) {
			return true;
		} else if (doCommandOnChildLayer(command, getColumnHeaderLayer())) {
			return true;
		} else if (doCommandOnChildLayer(command, getRowHeaderLayer())) {
			return true;
		} else {
			return doCommandOnChildLayer(command, getCornerLayer());
		}
	}
	
	private boolean doCommandOnChildLayer(ILayerCommand command, ILayer childLayer) {
		ILayerCommand childCommand = command.cloneCommand();
		return childLayer.doCommand(childCommand);
	}
	
	// Sub-layer accessors
	
	public ILayer getCornerLayer() {
		return getChildLayerByLayoutCoordinate(0, 0);
	}
	
	public void setCornerLayer(ILayer cornerLayer) {
		setChildLayer(GridRegion.CORNER, cornerLayer, 0, 0);
	}

	public ILayer getColumnHeaderLayer() {
		return getChildLayerByLayoutCoordinate(1, 0);
	}
	
	public void setColumnHeaderLayer(ILayer columnHeaderLayer) {
		setChildLayer(GridRegion.COLUMN_HEADER, columnHeaderLayer, 1, 0);
	}
	
	public ILayer getRowHeaderLayer() {
		return getChildLayerByLayoutCoordinate(0, 1);
	}
	
	public void setRowHeaderLayer(ILayer rowHeaderLayer) {
		setChildLayer(GridRegion.ROW_HEADER, rowHeaderLayer, 0, 1);
	}
	
	public ILayer getBodyLayer() {
		return getChildLayerByLayoutCoordinate(1, 1);
	}
	
	public void setBodyLayer(ILayer bodyLayer) {
		setChildLayer(GridRegion.BODY, bodyLayer, 1, 1);
	}
	
	@Override
	public String toString() {
		return getClass().getSimpleName() + "[corner=" + getCornerLayer() + " columnHeader=" + getColumnHeaderLayer()
				+ " rowHeader=" + getRowHeaderLayer() + " bodyLayer=" + getBodyLayer() + "]";
	}
	
}
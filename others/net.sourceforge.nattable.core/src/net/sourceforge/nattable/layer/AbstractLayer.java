package net.sourceforge.nattable.layer;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import net.sourceforge.nattable.command.ILayerCommand;
import net.sourceforge.nattable.command.ILayerCommandHandler;
import net.sourceforge.nattable.config.ConfigRegistry;
import net.sourceforge.nattable.config.IConfiguration;
import net.sourceforge.nattable.layer.cell.IConfigLabelAccumulator;
import net.sourceforge.nattable.layer.cell.LayerCell;
import net.sourceforge.nattable.layer.event.ILayerEvent;
import net.sourceforge.nattable.layer.event.ILayerEventHandler;
import net.sourceforge.nattable.painter.layer.GridLineCellLayerPainter;
import net.sourceforge.nattable.painter.layer.ILayerPainter;
import net.sourceforge.nattable.persistence.IPersistable;
import net.sourceforge.nattable.style.DisplayMode;
import net.sourceforge.nattable.ui.binding.UiBindingRegistry;
import net.sourceforge.nattable.util.IClientAreaProvider;

import org.eclipse.swt.graphics.Rectangle;

/**
 * Base layer implementation with common methods for managing listeners and caching, etc.
 */
public abstract class AbstractLayer implements ILayer {

	private String regionName;
	protected ILayerPainter layerPainter;
	private IClientAreaProvider clientAreaProvider = IClientAreaProvider.DEFAULT;
	private IConfigLabelAccumulator configLabelAccumulator;

	private final Map<Class<? extends ILayerCommand>, ILayerCommandHandler<? extends ILayerCommand>> commandHandlers = new LinkedHashMap<Class<? extends ILayerCommand>, ILayerCommandHandler<? extends ILayerCommand>>();
	private final Map<Class<? extends ILayerEvent>, ILayerEventHandler<? extends ILayerEvent>> eventHandlers = new HashMap<Class<? extends ILayerEvent>, ILayerEventHandler<? extends ILayerEvent>>();
	
	private final List<IPersistable> persistables = new LinkedList<IPersistable>();
	private final Set<ILayerListener> listeners = new LinkedHashSet<ILayerListener>();
	private final Collection<IConfiguration> configurations = new LinkedList<IConfiguration>();

	// Regions
	
	public LabelStack getRegionLabelsByXY(int x, int y) {
		LabelStack regionLabels = new LabelStack();
		if (regionName != null) {
			regionLabels.addLabel(regionName);
		}
		return regionLabels;
	}
	
	public String getRegionName() {
		return regionName;
	}
	
	public void setRegionName(String regionName) {
		this.regionName = regionName;
	}
	
	// Config lables
	
	public LabelStack getConfigLabelsByPosition(int columnPosition, int rowPosition) {
		LabelStack configLabels = new LabelStack();
		if (configLabelAccumulator != null) {
			configLabelAccumulator.accumulateConfigLabels(configLabels, columnPosition, rowPosition);
		}
		if (regionName != null) {
			configLabels.addLabel(regionName);
		}
		return configLabels;
	}
	
	public IConfigLabelAccumulator getConfigLabelAccumulator() {
		return configLabelAccumulator;
	}
	
	public void setConfigLabelAccumulator(IConfigLabelAccumulator cellLabelAccumulator) {
		this.configLabelAccumulator = cellLabelAccumulator;
	}
	
	// Persistence
	
	public void saveState(String prefix, Properties properties) {
		for (IPersistable persistable : persistables) {
			persistable.saveState(prefix, properties);
		}
	}
	
	public void loadState(String prefix, Properties properties) {
		for (IPersistable persistable : persistables) {
			persistable.loadState(prefix, properties);
		}
	}
	  
	public void registerPersistable(IPersistable persistable){
		persistables.add(persistable);
	}

	public void unregisterPersistable(IPersistable persistable){
		persistables.remove(persistable);
	}
	
	// Configuration
	
	public void addConfiguration(IConfiguration configuration) {
		configurations.add(configuration);
	}

	public void clearConfiguration() {
		configurations.clear();
	}
	
	public void configure(ConfigRegistry configRegistry, UiBindingRegistry uiBindingRegistry) {
		for (IConfiguration configuration : configurations) {
			configuration.configureLayer(this);
			configuration.configureRegistry(configRegistry);
			configuration.configureUiBindings(uiBindingRegistry);
		}
	}
	
	// Commands
	
	@SuppressWarnings("unchecked")
	public boolean doCommand(ILayerCommand command) {
		for (Class<? extends ILayerCommand> commandClass : commandHandlers.keySet()) {
			if (commandClass.isInstance(command)) {
				ILayerCommandHandler commandHandler = commandHandlers.get(commandClass);
				if (commandHandler.doCommand(this, command)) {
					return true;
				}
			}
		}
		
		return false;
	}
	
	// Command handlers
	
	public void registerCommandHandler(ILayerCommandHandler<?> commandHandler) {
		commandHandlers.put(commandHandler.getCommandClass(), commandHandler);
	}

	public void unregisterCommandHandler(Class<? extends ILayerCommand> commandClass) {
		commandHandlers.remove(commandClass);
	}
	
	// Events

	public void addLayerListener(ILayerListener listener) {
		listeners.add(listener);
	}
	
	public void removeLayerListener(ILayerListener listener) {
		listeners.remove(listener);
	}

	/**
	 * Handle layer event notification. Convert it to your context
	 * and propagate <i>UP</i>.
	 *  
	 * If you override this method you <strong>MUST NOT FORGET</strong> to raise
	 * the event up the layer stack by calling <code>super.fireLayerEvent(event)</code>
	 * - unless you plan to eat the event yourself.
	 **/
	@SuppressWarnings("unchecked")
	public void handleLayerEvent(ILayerEvent event) {
		for (Class<? extends ILayerEvent> eventClass : eventHandlers.keySet()) {
			if (eventClass.isInstance(event)) {
				ILayerEventHandler eventHandler = eventHandlers.get(eventClass);
				eventHandler.handleLayerEvent(event);
			}
		}
		
		// Pass on the event to our parent
		if (event.convertToLocal(this)) {
			fireLayerEvent(event);
		}
	}
	
	public void registerEventHandler(ILayerEventHandler<?> eventHandler) {
		eventHandlers.put(eventHandler.getLayerEventClass(), eventHandler);
	}
	
	/**
	 * Pass the event to all the {@link ILayerListener} registered on this layer.
	 * A cloned copy is passed to each listener.
	 */
	public void fireLayerEvent(ILayerEvent event) {
		if (listeners.size() > 0) {
			Iterator<ILayerListener> it = listeners.iterator();
			boolean isLastListener = false;
			do {
				ILayerListener l = it.next();
				isLastListener = !it.hasNext();  // Lookahead
				
				// Fire cloned event to first n-1 listeners; fire original event to last listener
				ILayerEvent eventToFire = isLastListener ? event : event.cloneEvent();
//				System.out.println("eventToFire="+eventToFire);
				l.handleLayerEvent(eventToFire);
			} while (!isLastListener);
		}
	}
	
	/**
	 * @return {@link ILayerPainter}. Defaults to {@link GridLineCellLayerPainter}
	 */
	public ILayerPainter getLayerPainter() {
		if (layerPainter == null) {
			layerPainter = new GridLineCellLayerPainter();
		}
		return layerPainter;
	}
	
	protected void setLayerPainter(ILayerPainter layerPainter) {
		this.layerPainter = layerPainter;
	}

	// Client area
	
	public IClientAreaProvider getClientAreaProvider() {
		return clientAreaProvider;
	}
	
	public void setClientAreaProvider(IClientAreaProvider clientAreaProvider) {
		this.clientAreaProvider = clientAreaProvider;
	}
	
	@Override
	public String toString() {
		return getClass().getSimpleName();
	}
	
	public LayerCell getCellByPosition(int columnPosition, int rowPosition) {
		if (columnPosition < 0 || columnPosition >= getColumnCount()
				|| rowPosition < 0 || rowPosition >= getRowCount()) {
			return null;
		}
		
		return new LayerCell(this, columnPosition, rowPosition);
	}
	
	public Rectangle getBoundsByPosition(int columnPosition, int rowPosition) {
		LayerCell cell = getCellByPosition(columnPosition, rowPosition);
		
		ILayer cellLayer = cell.getLayer();
		int originColumnPosition = cell.getOriginColumnPosition();
		int originRowPosition = cell.getOriginRowPosition();
		
		int x = cellLayer.getStartXOfColumnPosition(originColumnPosition);
		int y = cellLayer.getStartYOfRowPosition(originRowPosition);
		
		int width = 0;
		for (int i = 0; i < cell.getColumnSpan(); i++) {
			width += cellLayer.getColumnWidthByPosition(originColumnPosition + i);
		}

		int height = 0;
		for (int i = 0; i < cell.getRowSpan(); i++) {
			height += cellLayer.getRowHeightByPosition(originRowPosition + i);
		}

		return new Rectangle(x, y, width, height);
	}
	
	public String getDisplayModeByPosition(int columnPosition, int rowPosition) {
		return DisplayMode.NORMAL;
	}
}
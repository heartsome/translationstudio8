package net.sourceforge.nattable.freeze;

import net.sourceforge.nattable.config.IConfigRegistry;
import net.sourceforge.nattable.freeze.command.FreezeCommandHandler;
import net.sourceforge.nattable.freeze.config.DefaultFreezeGridBindings;
import net.sourceforge.nattable.grid.layer.DimensionallyDependentLayer;
import net.sourceforge.nattable.layer.CompositeLayer;
import net.sourceforge.nattable.layer.ILayer;
import net.sourceforge.nattable.painter.layer.ILayerPainter;
import net.sourceforge.nattable.selection.SelectionLayer;
import net.sourceforge.nattable.util.GUIHelper;
import net.sourceforge.nattable.viewport.ViewportLayer;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;

public class CompositeFreezeLayer extends CompositeLayer {

	private final FreezeLayer freezeLayer;
	
	private ILayerPainter layerPainter = new FreezableLayerPainter();
	
	public CompositeFreezeLayer(FreezeLayer freezeLayer, ViewportLayer viewportLayer, SelectionLayer selectionLayer) {
		this(freezeLayer, viewportLayer, selectionLayer, true);
	}
	
	public CompositeFreezeLayer(FreezeLayer freezeLayer, ViewportLayer viewportLayer, SelectionLayer selectionLayer, boolean useDefaultConfiguration) {
		super(2, 2);
		this.freezeLayer = freezeLayer;
		
		setChildLayer("FROZEN_REGION", freezeLayer, 0, 0);
		setChildLayer("FROZEN_ROW_REGION", new DimensionallyDependentLayer(selectionLayer, viewportLayer, freezeLayer), 1, 0);
		setChildLayer("FROZEN_COLUMN_REGION", new DimensionallyDependentLayer(selectionLayer, freezeLayer, viewportLayer), 0, 1);
		setChildLayer("NONFROZEN_REGION", viewportLayer, 1, 1);
		
		registerCommandHandler(new FreezeCommandHandler(freezeLayer, viewportLayer, selectionLayer));

		if (useDefaultConfiguration) {
			addConfiguration(new DefaultFreezeGridBindings());
		}
	}
	
	@Override
	public ILayerPainter getLayerPainter() {
		return layerPainter;
	}
	
	class FreezableLayerPainter extends CompositeLayerPainter {
		@Override
		public void paintLayer(ILayer natLayer, GC gc, int xOffset, int yOffset, Rectangle rectangle, IConfigRegistry configRegistry) {
			super.paintLayer(natLayer, gc, xOffset, yOffset, rectangle, configRegistry);
			
			gc.setClipping(rectangle);
			Color oldFg = gc.getForeground();
			gc.setForeground(GUIHelper.COLOR_BLUE);
			final int freezeWidth = freezeLayer.getWidth() - 1;
			if (freezeWidth > 0) {
				gc.drawLine(xOffset + freezeWidth, yOffset, xOffset + freezeWidth, yOffset + getHeight() - 1);
			}
			final int freezeHeight = freezeLayer.getHeight() - 1;
			if (freezeHeight > 0) {
				gc.drawLine(xOffset, yOffset + freezeHeight, xOffset + getWidth() - 1, yOffset + freezeHeight);
			}
			gc.setForeground(oldFg);
		}
		
	}
	
}

package net.sourceforge.nattable.config;

import net.sourceforge.nattable.NatTable;
import net.sourceforge.nattable.data.validate.IDataValidator;
import net.sourceforge.nattable.layer.AbstractLayer;
import net.sourceforge.nattable.layer.ILayer;
import net.sourceforge.nattable.painter.cell.ICellPainter;
import net.sourceforge.nattable.ui.binding.UiBindingRegistry;

/**
 * Configurations can be added to NatTable/ILayer to modify default behavior.
 * These will be processed when {@link NatTable#configure()} is invoked.
 *
 * Default configurations are added to most layers {@link AbstractLayer#addConfiguration()}.
 * You can turn off default configuration for an {@link ILayer} by setting auto configure to false
 * in the constructor.
 */
public interface IConfiguration {

	public void configureLayer(ILayer layer);

	/**
	 * Configure NatTable's {@link IConfigRegistry} upon receiving this call back.
	 * A mechanism to plug-in custom {@link ICellPainter}, {@link IDataValidator} etc.
	 */
	public void configureRegistry(IConfigRegistry configRegistry);

	/**
	 * Configure NatTable's {@link IConfigRegistry} upon receiving this call back
	 * A mechanism to customize key/mouse bindings.
	 */
	public void configureUiBindings(UiBindingRegistry uiBindingRegistry);

}

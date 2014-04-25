package net.sourceforge.nattable.util;

import net.sourceforge.nattable.NatTable;
import net.sourceforge.nattable.layer.ILayer;

import org.eclipse.swt.graphics.Rectangle;

/**
 * Specifies the rectangular area available to an {@link ILayer}<br/>
 * Note: All layers get the client area from {@link NatTable} which implements this interface. 
 * 
 * @see ILayer#getClientAreaProvider()
 */
public interface IClientAreaProvider {

	IClientAreaProvider DEFAULT = new IClientAreaProvider() {
		public Rectangle getClientArea() {
			return new Rectangle(0, 0, 0, 0);
		}
	};

	public Rectangle getClientArea();
}

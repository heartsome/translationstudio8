package net.sourceforge.nattable.config;

import net.sourceforge.nattable.data.convert.IDisplayConverter;
import net.sourceforge.nattable.painter.cell.ICellPainter;
import net.sourceforge.nattable.style.ConfigAttribute;
import net.sourceforge.nattable.style.IStyle;

public interface CellConfigAttributes {

	public static final ConfigAttribute<ICellPainter> CELL_PAINTER = new ConfigAttribute<ICellPainter>();
	
	public static final ConfigAttribute<IStyle> CELL_STYLE = new ConfigAttribute<IStyle>();
	
	public static final ConfigAttribute<IDisplayConverter> DISPLAY_CONVERTER = new ConfigAttribute<IDisplayConverter>();
	
}

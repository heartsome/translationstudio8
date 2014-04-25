package net.sourceforge.nattable.style;

import java.util.List;

import net.sourceforge.nattable.config.CellConfigAttributes;
import net.sourceforge.nattable.config.IConfigRegistry;

public class CellStyleProxy extends StyleProxy {

	public CellStyleProxy(IConfigRegistry configRegistry, String targetDisplayMode, List<String> configLabels) {
		super(CellConfigAttributes.CELL_STYLE, configRegistry, targetDisplayMode, configLabels);
	}

	public <T> void setAttributeValue(ConfigAttribute<T> styleAttribute, T value) {
		throw new UnsupportedOperationException("Not implmented yet");
	}

}

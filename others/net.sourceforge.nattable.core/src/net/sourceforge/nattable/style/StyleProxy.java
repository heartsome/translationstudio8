package net.sourceforge.nattable.style;

import java.util.List;

import net.sourceforge.nattable.config.IConfigRegistry;

public abstract class StyleProxy implements IStyle {
	
	private final ConfigAttribute<IStyle> styleConfigAttribute;
	private final IConfigRegistry configRegistry;
	private final String targetDisplayMode;
	private final List<String> configLabels;

	public StyleProxy(ConfigAttribute<IStyle> styleConfigAttribute, IConfigRegistry configRegistry, String targetDisplayMode, List<String> configLabels) {
		this.styleConfigAttribute = styleConfigAttribute;
		this.configRegistry = configRegistry;
		this.targetDisplayMode = targetDisplayMode;
		this.configLabels = configLabels;
	}
	
	public <T> T getAttributeValue(ConfigAttribute<T> styleAttribute) {
		T styleAttributeValue = null;
		IDisplayModeOrdering displayModeOrdering = configRegistry.getDisplayModeOrdering();

		for (String displayMode : displayModeOrdering.getDisplayModeOrdering(targetDisplayMode)) {
			for (String configLabel : configLabels) {
				IStyle cellStyle = configRegistry.getSpecificConfigAttribute(styleConfigAttribute, displayMode, configLabel);
				if (cellStyle != null) {
					styleAttributeValue = cellStyle.getAttributeValue(styleAttribute);
					if (styleAttributeValue != null) {
						return styleAttributeValue;
					}
				}
			}

			// default
			IStyle cellStyle = configRegistry.getConfigAttribute(styleConfigAttribute, displayMode);
			if (cellStyle != null) {
				styleAttributeValue = cellStyle.getAttributeValue(styleAttribute);
				if (styleAttributeValue != null) {
					return styleAttributeValue;
				}
			}
		}
		
		return null;
	}
	
}

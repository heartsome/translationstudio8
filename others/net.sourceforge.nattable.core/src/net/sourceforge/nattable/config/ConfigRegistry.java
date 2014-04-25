package net.sourceforge.nattable.config;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sourceforge.nattable.style.ConfigAttribute;
import net.sourceforge.nattable.style.DefaultDisplayModeOrdering;
import net.sourceforge.nattable.style.DisplayMode;
import net.sourceforge.nattable.style.IDisplayModeOrdering;

public class ConfigRegistry implements IConfigRegistry {

	// Map<configAttributeType, Map<displayMode, Map<configLabel, value>>>
	Map<ConfigAttribute<?>, Map<String, Map<String, ?>>> configRegistry = new HashMap<ConfigAttribute<?>, Map<String, Map<String, ?>>>();

	public <T> T getConfigAttribute(ConfigAttribute<T> configAttribute, String targetDisplayMode, String...configLabels) {
		return getConfigAttribute(configAttribute, targetDisplayMode, Arrays.asList(configLabels));
	}
	
	@SuppressWarnings("unchecked")
	public <T> T getConfigAttribute(ConfigAttribute<T> configAttribute, String targetDisplayMode, List<String> configLabels) {
		T attributeValue = null;
		
		Map<String, Map<String, ?>> displayModeConfigAttributeMap = configRegistry.get(configAttribute);
		if (displayModeConfigAttributeMap != null) {
			for (String displayMode : displayModeOrdering.getDisplayModeOrdering(targetDisplayMode)) {
				Map<String, T> configAttributeMap = (Map<String, T>) displayModeConfigAttributeMap.get(displayMode);
				if (configAttributeMap != null) {
					for (String configLabel : configLabels) {
						attributeValue = configAttributeMap.get(configLabel);
						if (attributeValue != null) {
							return attributeValue;
						}
					}
					
					// default config type
					attributeValue = configAttributeMap.get(null);
					if (attributeValue != null) {
						return attributeValue;
					}
				}
			}
		}
		
		return attributeValue;
	}
	
	@SuppressWarnings("unchecked")
	public <T> T getSpecificConfigAttribute(ConfigAttribute<T> configAttribute, String displayMode, String configLabel) {
		T attributeValue = null;
		
		Map<String, Map<String, ?>> displayModeConfigAttributeMap = configRegistry.get(configAttribute);
		if (displayModeConfigAttributeMap != null) {
			Map<String, T> configAttributeMap = (Map<String, T>) displayModeConfigAttributeMap.get(displayMode);
			if (configAttributeMap != null) {
				attributeValue = configAttributeMap.get(configLabel);
				if (attributeValue != null) {
					return attributeValue;
				}
			}
		}
		
		return attributeValue;
	}
	
	public <T> void registerConfigAttribute(ConfigAttribute<T> configAttribute, T attributeValue) {
		registerConfigAttribute(configAttribute, attributeValue, DisplayMode.NORMAL);
	}
	
	public <T> void registerConfigAttribute(ConfigAttribute<T> configAttribute, T attributeValue, String displayMode) {
		registerConfigAttribute(configAttribute, attributeValue, displayMode, null);
	}
	
	@SuppressWarnings("unchecked")
	public <T> void registerConfigAttribute(ConfigAttribute<T> configAttribute, T attributeValue, String displayMode, String configLabel) {
		Map<String, Map<String, ?>> displayModeConfigAttributeMap = configRegistry.get(configAttribute);
		if (displayModeConfigAttributeMap == null) {
			displayModeConfigAttributeMap = new HashMap<String, Map<String, ?>>();
			configRegistry.put(configAttribute, displayModeConfigAttributeMap);
		}
		
		Map<String, T> configAttributeMap = (Map<String, T>) displayModeConfigAttributeMap.get(displayMode);
		if (configAttributeMap == null) {
			configAttributeMap = new HashMap<String, T>();
			displayModeConfigAttributeMap.put(displayMode, configAttributeMap);
		}
		
		configAttributeMap.put(configLabel, attributeValue);
	};
	
	@SuppressWarnings("unchecked")
	public <T> void unregisterConfigAttribute(Class<T> configAttributeType, String displayMode, String configLabel) {
		Map<String, Map<String, ?>> displayModeConfigAttributeMap = configRegistry.get(configAttributeType);
		if (displayModeConfigAttributeMap != null) {
			Map<String, T> configAttributeMap = (Map<String, T>) displayModeConfigAttributeMap.get(displayMode);
			if (configAttributeMap != null) {
				configAttributeMap.remove(configLabel);
			}
		}
	}
	
	// Display mode ordering //////////////////////////////////////////////////

	IDisplayModeOrdering displayModeOrdering = new DefaultDisplayModeOrdering();

	public IDisplayModeOrdering getDisplayModeOrdering() {
		return displayModeOrdering;
	}

	public void setDisplayModeOrdering(IDisplayModeOrdering displayModeOrdering) {
		this.displayModeOrdering = displayModeOrdering;
	}
	
	// Private methods

}

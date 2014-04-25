package net.sourceforge.nattable.layer.cell;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sourceforge.nattable.util.ArrayUtil;

public abstract class AbstractOverrider implements IConfigLabelAccumulator {
	
	private Map<Serializable, List<String>> overrides = new HashMap<Serializable, List<String>>();

	public void removeOverride(Serializable key) {
		overrides.remove(key);
	}

	public void registerOverrides(Serializable key, String...configLabels) {
		List<String> existingOverrides = getOverrides(key);
		if(existingOverrides == null){
			registerOverrides(key, ArrayUtil.asList(configLabels));
		} else {
			existingOverrides.addAll(ArrayUtil.asList(configLabels));
		}
	}
	
	public void registerOverrides(Serializable key, List<String> configLabels) {
		overrides.put(key, configLabels);
	}

	public Map<Serializable, List<String>> getOverrides() {
		return overrides;
	}
	
	public List<String> getOverrides(Serializable key) {
		return overrides.get(key);
	}

	public void addOverrides(Map<Serializable, List<String>> overrides) {
		this.overrides.putAll(overrides);
	}
	
}

package net.sourceforge.nattable.style;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

public class Style implements IStyle {

	private final Map<ConfigAttribute<?>, Object> styleAttributeValueMap = new HashMap<ConfigAttribute<?>, Object>();

	@SuppressWarnings("unchecked")
	public <T> T getAttributeValue(ConfigAttribute<T> styleAttribute) {
		return (T) styleAttributeValueMap.get(styleAttribute);
	}

	public <T> void setAttributeValue(ConfigAttribute<T> styleAttribute, T value) {
		styleAttributeValueMap.put(styleAttribute, value);
	}

	@Override
	public String toString() {
		StringBuilder resultBuilder = new StringBuilder();
		resultBuilder.append(this.getClass().getSimpleName() + ": ");

		Set<Entry<ConfigAttribute<?>, Object>> entrySet = styleAttributeValueMap.entrySet();

		for (Entry<ConfigAttribute<?>, Object> entry : entrySet) {
			resultBuilder.append(entry.getKey() + ": " + entry.getValue() + "\n");
		}

		return resultBuilder.toString();
	}

}

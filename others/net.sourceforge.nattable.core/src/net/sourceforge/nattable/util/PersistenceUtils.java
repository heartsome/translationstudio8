package net.sourceforge.nattable.util;

import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

public class PersistenceUtils {

	/**
	 * Parse the persisted property and create a TreeMap<Integer, String> from it.<br/>
	 * Works in conjunction with the {@link PersistenceUtils#mapAsString(Map)}.<br/>
	 * 
	 * @param property from the properties file.
	 */
	public static Map<Integer, String> parseString(Object property) {
		TreeMap<Integer, String> map = new TreeMap<Integer, String>();
		
		if (property != null) {
			String value = (String) property;
			String[] renamedColumns = value.split("\\|");
	
			for (String token : renamedColumns) {
				String[] split = token.split(":");
				String index = split[0];
				String label = split[1];
				map.put(Integer.valueOf(index), label);
			}
		}
		return map;
	}

	/**
	 * Convert the Map to a String suitable for persisting in the Properties file.
	 * {@link PersistenceUtils#parseString(Object)} can be used to reconstruct this Map object from the String.<br/>
	 */
	public static String mapAsString(Map<Integer, String> map) {
		StringBuffer buffer = new StringBuffer();
		for (Entry<Integer, String> entry : map.entrySet()) {
			buffer.append(entry.getKey() + ":" + entry.getValue() + "|");
		}
		return buffer.toString();
	}

}

package net.sourceforge.nattable.layer;

import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.TreeMap;

import net.sourceforge.nattable.util.GUIHelper;

import org.apache.commons.lang.StringUtils;

public class SizeConfig {

	public static final String PERSISTENCE_KEY_DEFAULT_SIZE = ".defaultSize";
	public static final String PERSISTENCE_KEY_DEFAULT_SIZES = ".defaultSizes";
	public static final String PERSISTENCE_KEY_SIZES = ".sizes";
	public static final String PERSISTENCE_KEY_RESIZABLE_BY_DEFAULT = ".resizableByDefault";
	public static final String PERSISTENCE_KEY_RESIZABLE_INDEXES = ".resizableIndexes";

	private int defaultSize;
	private final Map<Integer, Integer> defaultSizeMap = new TreeMap<Integer, Integer>();
	private final Map<Integer, Integer> sizeMap = new TreeMap<Integer, Integer>();
	private final Map<Integer, Boolean> resizablesMap = new TreeMap<Integer, Boolean>();
	private boolean resizableByDefault = true;

	public SizeConfig() {}

	public SizeConfig(int defaultSize) {
		this.defaultSize = defaultSize;
	}

	// Persistence

	public void saveState(String prefix, Properties properties) {
		properties.put(prefix + PERSISTENCE_KEY_DEFAULT_SIZE, String.valueOf(defaultSize));
		saveMap(defaultSizeMap, prefix + PERSISTENCE_KEY_DEFAULT_SIZES, properties);
		saveMap(sizeMap, prefix + PERSISTENCE_KEY_SIZES, properties);
		properties.put(prefix + PERSISTENCE_KEY_RESIZABLE_BY_DEFAULT, String.valueOf(resizableByDefault));
		saveMap(resizablesMap, prefix + PERSISTENCE_KEY_RESIZABLE_INDEXES, properties);
	}

	private void saveMap(Map<Integer, ?> map, String key, Properties properties) {
		if (map.size() > 0) {
			StringBuilder strBuilder = new StringBuilder();
			for (Integer index : map.keySet()) {
				strBuilder.append(index);
				strBuilder.append(':');
				strBuilder.append(map.get(index));
				strBuilder.append(',');
			}
			properties.setProperty(key, strBuilder.toString());
		}
	}

	public void loadState(String prefix, Properties properties) {
		String persistedDefaultSize = properties.getProperty(prefix + PERSISTENCE_KEY_DEFAULT_SIZE);
		if (!StringUtils.isEmpty(persistedDefaultSize)) {
			defaultSize = Integer.valueOf(persistedDefaultSize).intValue();
		}

		String persistedResizableDefault = properties.getProperty(prefix + PERSISTENCE_KEY_RESIZABLE_BY_DEFAULT);
		if (!StringUtils.isEmpty(persistedResizableDefault)) {
			resizableByDefault = Boolean.valueOf(persistedResizableDefault).booleanValue();
		}

		loadBooleanMap(prefix + PERSISTENCE_KEY_RESIZABLE_INDEXES, properties, resizablesMap);
		loadIntegerMap(prefix + PERSISTENCE_KEY_DEFAULT_SIZES, properties, defaultSizeMap);
		loadIntegerMap(prefix + PERSISTENCE_KEY_SIZES, properties, sizeMap);
	}

	private void loadIntegerMap(String key, Properties properties, Map<Integer, Integer> map) {
		String property = properties.getProperty(key);
		if (property != null) {
			map.clear();

			StringTokenizer tok = new StringTokenizer(property, ",");
			while (tok.hasMoreTokens()) {
				String token = tok.nextToken();
				int separatorIndex = token.indexOf(':');
				map.put(Integer.valueOf(token.substring(0, separatorIndex)), Integer.valueOf(token.substring(separatorIndex + 1)));
			}
		}
	}

	private void loadBooleanMap(String key, Properties properties, Map<Integer, Boolean> map) {
		String property = properties.getProperty(key);
		if (property != null) {
			StringTokenizer tok = new StringTokenizer(property, ",");
			while (tok.hasMoreTokens()) {
				String token = tok.nextToken();
				int separatorIndex = token.indexOf(':');
				map.put(Integer.valueOf(token.substring(0, separatorIndex)), Boolean.valueOf(token.substring(separatorIndex + 1)));
			}
		}
	}

	// Default size

	public void setDefaultSize(int defaultSize) {
		this.defaultSize = defaultSize;
	}

	public void setDefaultSize(int position, int size) {
		defaultSizeMap.put(Integer.valueOf(position), Integer.valueOf(size));
	}

	private int getDefaultSize(int position) {
		int size = getSize(defaultSizeMap, position);
		if (size >= 0) {
			return size;
		} else {
			return defaultSize;
		}
	}

	// Size

	public int getAggregateSize(int position) {
		if (position < 0) {
			return -1;
		} else if (position == 0) {
			return 0;
		} else if (isAllPositionsSameSize()) {
			return position * defaultSize;
		} else {
			int resizeAggregate = 0;

			int resizedColumns = 0;
			for (Integer resizedPosition : sizeMap.keySet()) {
				if (resizedPosition.intValue() < position) {
					resizedColumns++;
					resizeAggregate += sizeMap.get(resizedPosition).intValue();
				} else {
					break;
				}
			}

			return (position * defaultSize) + (resizeAggregate - (resizedColumns * defaultSize));
		}
	}

	public int getSize(int position) {
		int size = getSize(sizeMap, position);

		if (size <= 0 && sizeMap.containsKey(Integer.valueOf(position))) {
			return GUIHelper.DEFAULT_MIN_DISPLAY_SIZE;
		} else if (size >= 0) {
			return size;
		} else {
			return getDefaultSize(position);
		}
	}

	public void setSize(int position, int size) {
		if (isPositionResizable(position)) {
			sizeMap.put(Integer.valueOf(position), Integer.valueOf(size));
		}
	}

	// Resizable

	public boolean isResizableByDefault() {
		return resizableByDefault;
	}

	public boolean isPositionResizable(int position) {
		Boolean resizable = resizablesMap.get(Integer.valueOf(position));
		if (resizable != null) {
			return resizable.booleanValue();
		}
		return resizableByDefault;
	}

	public void setPositionResizable(int position, boolean resizable) {
		resizablesMap.put(Integer.valueOf(position), Boolean.valueOf(resizable));
	}

	public void setResizableByDefault(boolean resizableByDefault) {
		resizablesMap.clear();
		this.resizableByDefault = resizableByDefault;
	}

	// All positions same size

	public boolean isAllPositionsSameSize() {
		return defaultSizeMap.size() == 0 && sizeMap.size() == 0;
	}

	private int getSize(Map<Integer, Integer> map, int position) {
		Integer sizeFromMap = map.get(Integer.valueOf(position));

		if (sizeFromMap != null) {
			return sizeFromMap.intValue();
		}

		return -1;
	}

}

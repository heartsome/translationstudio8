package net.sourceforge.nattable.data;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Convenience class which uses java reflection to get/set property names
 *  from the row bean. It looks for getter methods for reading and setter
 *  methods for writing according to the Java conventions.
 *
 * @param <R> type of the row object/bean
 */
public class ReflectiveColumnPropertyAccessor<R> implements IColumnPropertyAccessor<R> {

	private final List<String> propertyNames;

	private Map<String, PropertyDescriptor> propertyDescriptorMap;

	/**
	 * @param propertyNames of the members of the row bean
	 */
	public ReflectiveColumnPropertyAccessor(String[] propertyNames) {
		this.propertyNames = Arrays.asList(propertyNames);
	}

	public int getColumnCount() {
		return propertyNames.size();
	}

	public Object getDataValue(R rowObj, int columnIndex) {
		try {
			PropertyDescriptor propertyDesc = getPropertyDescriptor(rowObj, columnIndex);
			Method readMethod = propertyDesc.getReadMethod();
			return readMethod.invoke(rowObj);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	public void setDataValue(R rowObj, int columnIndex, Object newValue) {
		try {
			PropertyDescriptor propertyDesc = getPropertyDescriptor(rowObj, columnIndex);
			Method writeMethod = propertyDesc.getWriteMethod();
			if(writeMethod == null) {
				throw new RuntimeException(
					"Setter method not found in backing bean for value at column index: " + columnIndex);
			}
			writeMethod.invoke(rowObj, newValue);
		} catch (IllegalArgumentException ex){
			System.err.println(
				"Data the type being set does not match the data type of the setter method in the backing bean");
		} catch (Exception e) {
			e.printStackTrace(System.err);
			throw new RuntimeException("Error while setting data value");
		}
	};

	public String getColumnProperty(int columnIndex) {
		return propertyNames.get(columnIndex);
	}

	public int getColumnIndex(String propertyName) {
		return propertyNames.indexOf(propertyName);
	}

	private PropertyDescriptor getPropertyDescriptor(R rowObj, int columnIndex) throws IntrospectionException {
		if (propertyDescriptorMap == null) {
			propertyDescriptorMap = new HashMap<String, PropertyDescriptor>();
			PropertyDescriptor[] propertyDescriptors = Introspector.getBeanInfo(rowObj.getClass()).getPropertyDescriptors();
			for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
				propertyDescriptorMap.put(propertyDescriptor.getName(), propertyDescriptor);
			}
		}

		final String propertyName = propertyNames.get(columnIndex);
		return propertyDescriptorMap.get(propertyName);
	}

}

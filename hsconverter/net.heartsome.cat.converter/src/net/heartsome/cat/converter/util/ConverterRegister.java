/**
 * ConverterRegister.java
 *
 * Version information :
 *
 * Date:Jan 14, 2010
 *
 * Copyright notice :
 */
package net.heartsome.cat.converter.util;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Properties;

import net.heartsome.cat.converter.Converter;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

/**
 * The Class ConverterRegister.
 * @author John Zhu
 * @version
 * @since JDK1.5
 */
public final class ConverterRegister {

	/**
	 * Instantiates a new converter register.
	 */
	private ConverterRegister() {
		// prevent instance
	}

	/**
	 * 注册正向转换的转换器帮助类.
	 * @param context
	 *            the context
	 * @param convert
	 *            the convert
	 * @param properties
	 *            the properties
	 * @return the service registration
	 */
	public static ServiceRegistration registerPositiveConverter(BundleContext context, Converter convert,
			Properties properties) {
		properties.put(Converter.ATTR_DIRECTION, Converter.DIRECTION_POSITIVE);
		return context.registerService(Converter.class.getName(), convert, getDictByProperties(properties));
	}

	/**
	 * 注册反向转换的转换器帮助类.
	 * @param context
	 *            the context
	 * @param convert
	 *            the convert
	 * @param properties
	 *            the properties
	 * @return the service registration
	 */
	public static ServiceRegistration registerReverseConverter(BundleContext context, Converter convert,
			Properties properties) {
		properties.put(Converter.ATTR_DIRECTION, Converter.DIRECTION_REVERSE);
		return context.registerService(Converter.class.getName(), convert, getDictByProperties(properties));
	}
	
	private static Dictionary<String, Object> getDictByProperties(Properties properties){
		 Dictionary<String,Object> dict = new Hashtable<String, Object>();
		 for (Object key : properties.keySet()){
			 String strKey = (String)key;
			 Object strValue = properties.get(strKey);
			 dict.put(strKey, strValue);
		 }
		 return dict;
	}

}

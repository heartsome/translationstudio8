/**
 * Inx2Xliff.java
 *
 * Version information :
 *
 * Date:Jan 14, 2010
 *
 * Copyright notice :
 */
package net.heartsome.cat.converter.inx;

import java.util.Map;

import net.heartsome.cat.converter.Converter;
import net.heartsome.cat.converter.ConverterException;
import net.heartsome.cat.converter.inx.resource.Messages;

import org.eclipse.core.runtime.IProgressMonitor;

/**
 * The Class Inx2Xliff.
 * @author John Zhu
 * @version
 * @since JDK1.6
 */
public class Inx2Xliff implements Converter {

	/** The Constant TYPE_VALUE. */
	public static final String TYPE_VALUE = "x-inx";

	/** The Constant TYPE_NAME_VALUE. */
	public static final String TYPE_NAME_VALUE = Messages.getString("inx.TYPE_NAME_VALUE");

	/** The Constant NAME_VALUE. */
	public static final String NAME_VALUE = "INX to XLIFF Conveter";

	// 内部实现所依赖的转换器
	/** The dependant converter. */
	private Converter dependantConverter;

	/**
	 * for test to initialize depend on converter.
	 */
	public Inx2Xliff() {
		dependantConverter = Activator.getXMLConverter(Converter.DIRECTION_POSITIVE);
	}

	/**
	 * 运行时把所依赖的转换器，在初始化的时候通过构造函数注入。.
	 * @param converter
	 *            the converter
	 */
	public Inx2Xliff(Converter converter) {
		dependantConverter = converter;
	}

	/**
	 * (non-Javadoc).
	 * @param args
	 *            the args
	 * @param monitor
	 *            the monitor
	 * @return the map< string, string>
	 * @throws ConverterException
	 *             the converter exception
	 * @see net.heartsome.cat.converter.Converter#convert(java.util.Map, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public Map<String, String> convert(Map<String, String> args, IProgressMonitor monitor) throws ConverterException {
		args.put(Converter.ATTR_IS_INDESIGN, Converter.TRUE);
		args.put(Converter.ATTR_FORMAT, TYPE_VALUE);
		return dependantConverter.convert(args, monitor);
	}

	/**
	 * (non-Javadoc).
	 * @return the name
	 * @see net.heartsome.cat.converter.Converter#getName()
	 */
	public String getName() {
		return NAME_VALUE;
	}

	/**
	 * (non-Javadoc).
	 * @return the type
	 * @see net.heartsome.cat.converter.Converter#getType()
	 */
	public String getType() {
		return TYPE_VALUE;
	}

	/**
	 * (non-Javadoc).
	 * @return the type name
	 * @see net.heartsome.cat.converter.Converter#getTypeName()
	 */
	public String getTypeName() {
		return TYPE_NAME_VALUE;
	}
}
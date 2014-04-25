/**
 * ConverterTracker.java
 *
 * Version information :
 *
 * Date:Jan 14, 2010
 *
 * Copyright notice :
 */
package net.heartsome.cat.converter.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.heartsome.cat.converter.Converter;

import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.Filter;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

/**
 * The Class ConverterTracker. 跟踪目前所支持的转换器列表，并进行动态更新。 客户端需要继承此类，并实现<code>convert</code>方法处理转换和转换结果。在
 * <code>convert</code>方法里，可以通过 <code>getConvert</code>方法得到当前所需的转换器.
 * @author cheney
 * @version
 * @since JDK1.6
 */
public abstract class ConverterTracker extends AbstractModelObject {
	/* set this to true to compile in debug messages */
	/** The Constant DEBUG. */
	protected static final boolean DEBUG = false;

	/** The selected type. */
	protected String selectedType = "";

	/** The support types. */
	private List<ConverterBean> supportTypes;

	/** The converter service tracker. */
	private ServiceTracker converterServiceTracker;

	/** The context. */
	private BundleContext context;

	/** The direction. */
	protected String direction;

	/**
	 * The Constructor.
	 * @param bundleContext
	 *            插件所在的 bundle context
	 * @param direction
	 *            取<code>Converter.DIRECTION_POSITIVE</code>或 <code>Converter.DIRECTION_REVERSE</code>
	 */
	public ConverterTracker(BundleContext bundleContext, String direction) {
		this.direction = direction;
		supportTypes = new ArrayList<ConverterBean>();
		this.context = bundleContext;
		String filterStr = new AndFilter(new EqFilter(Constants.OBJECTCLASS, Converter.class.getName()), new EqFilter(
				Converter.ATTR_DIRECTION, direction)).toString();
		Filter filter = null;
		try {
			filter = context.createFilter(filterStr);
		} catch (InvalidSyntaxException e) {
			// ignore the exception
			e.printStackTrace();
		}
		if (filter != null) {
			converterServiceTracker = new ServiceTracker(context, filter, new ConverterCustomizer());
		}
		converterServiceTracker.open();
	}

	/**
	 * Gets the support types.
	 * @return the support types
	 */
	public List<ConverterBean> getSupportTypes() {
		return supportTypes;
	}

	/**
	 * Sets the support types.
	 * @param supportTypes
	 *            the new support types
	 */
	public void setSupportTypes(List<ConverterBean> supportTypes) {
		this.supportTypes = supportTypes;
		firePropertyChange("supportTypes", null, null);
	}

	/**
	 * Gets the selected type.
	 * @return the selected type
	 */
	public String getSelectedType() {
		return selectedType;
	}

	/**
	 * Sets the selected type.
	 * @param selectedType
	 *            the new selected type
	 */
	public void setSelectedType(String selectedType) {
		String oldReverseSelected = this.selectedType;
		this.selectedType = selectedType;
		firePropertyChange("selectedType", oldReverseSelected, this.selectedType);
		System.out.println("current selected:" + this.selectedType);
	}

	/**
	 * 获得当前转换器的转换方向
	 * @return ;
	 */
	public String getDirection() {
		return direction;
	}

	/**
	 * Close.
	 */
	public void close() {
		// destroy resources
		converterServiceTracker.close();
	}

	/**
	 * Convert.
	 * @param parameters
	 *            转换所需要的参数 Map
	 * @return the map< string, string>
	 */
	public abstract Map<String, String> convert(Map<String, String> parameters);

	/**
	 * 根据当前选择的转换器类型和转换方向值获得对应的转换器。.
	 * @return 返回当前选择的转换器类型，及初始化此 model 的转换方向的值对应的转换器。如果 type 对应的转换器不存在，则返回 <code>NULL</code>
	 */
	public Converter getConverter() {
		return getConverter(selectedType);
	};

	/**
	 * 根据指定的<code>type</code>获得对应的转换器 *.
	 * @param type
	 *            指定的 type
	 * @return 返回 type 值，及初始化此 model 的转换方向的值对应的转换器。如果 type 对应的转换器不存在，则返回 <code>NULL</code>
	 */
	public Converter getConverter(String type) {
		Converter converter = null;
		if (type != null & direction != null) {
			ServiceReference[] serviceReferences = converterServiceTracker.getServiceReferences();
			for (int i = 0; i < serviceReferences.length; i++) {
				String converterType = (String) serviceReferences[i].getProperty(Converter.ATTR_TYPE);
				String converterDirection = (String) serviceReferences[i].getProperty(Converter.ATTR_DIRECTION);
				if (type.equals(converterType) && direction.equals(converterDirection)) {
					converter = (Converter) converterServiceTracker.getService(serviceReferences[i]);
					break;
				}
			}
		}
		return converter;
	}

	/**
	 * The Class ConverterCustomizer.
	 * @author John Zhu
	 * @version
	 * @since JDK1.6
	 */
	private class ConverterCustomizer implements ServiceTrackerCustomizer {

		/**
		 * (non-Javadoc).
		 * @param reference
		 *            the reference
		 * @return the object
		 * @see org.osgi.util.tracker.ServiceTrackerCustomizer#addingService(org.osgi.framework.ServiceReference)
		 */
		public Object addingService(ServiceReference reference) {
			Converter converter = (Converter) context.getService(reference);
			if (DEBUG) {
				System.out.println("-----------------------------------------");
				System.out.println("adding a converter service:" + converter.getName());
			}
			synchronized (supportTypes) {
				String type = converter.getType();
				boolean isExist = false;
				for (ConverterBean bean : supportTypes) {
					if (type.equals(bean.getName())) {
						isExist = true;
						break;
					}
				}
				if (!isExist) {
					ConverterBean bean = new ConverterBean(converter.getType(), converter.getTypeName());
					supportTypes.add(bean);
					setSupportTypes(supportTypes);
				}
			}
			if (DEBUG) {
				System.out.println("-----------------------------------------");
			}
			return converter;
		}

		/**
		 * (non-Javadoc).
		 * @param reference
		 *            the reference
		 * @param service
		 *            the service
		 * @see org.osgi.util.tracker.ServiceTrackerCustomizer#modifiedService(org.osgi.framework.ServiceReference,
		 *      java.lang.Object)
		 */
		public void modifiedService(ServiceReference reference, Object service) {
		}

		/**
		 * (non-Javadoc).
		 * @param reference
		 *            the reference
		 * @param service
		 *            the service
		 * @see org.osgi.util.tracker.ServiceTrackerCustomizer#removedService(org.osgi.framework.ServiceReference,
		 *      java.lang.Object)
		 */
		public void removedService(ServiceReference reference, Object service) {
			Converter converter = (Converter) context.getService(reference);
			if (DEBUG) {
				System.out.println("-----------------------------------------");
				System.out.println("removing a converter service:" + converter.getName());
			}

			synchronized (supportTypes) {
				String type = converter.getType();
				Iterator<ConverterBean> it = supportTypes.iterator();
				while (it.hasNext()) {
					ConverterBean bean = it.next();
					if (type.equals(bean.getName())) {
						it.remove();
						setSupportTypes(supportTypes);
					}
				}
			}
			if (DEBUG) {
				System.out.println("-----------------------------------------");
			}
			context.ungetService(reference);
		}
	}

}

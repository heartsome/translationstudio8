package net.heartsome.cat.convert.ui.model;

import java.util.HashMap;
import java.util.Map;

import net.heartsome.cat.converter.Converter;
import net.heartsome.cat.converter.ConverterException;
import net.heartsome.cat.converter.util.ConverterTracker;

import org.osgi.framework.BundleContext;

public class ConverterViewModel extends ConverterTracker {

	public ConverterViewModel(BundleContext bundleContext, String direction) {
		super(bundleContext, direction);
	}

	@Override
	public Map<String, String> convert(Map<String, String> parameters) {
		Converter converter=getConverter();
		if (converter != null) {
			System.out.println(""+converter.getName());
			try {
				converter.convert(null, null);
			} catch (ConverterException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Map<String,String> result=new HashMap<String, String>(1);
			result.put("name", converter.getName());
			return result;
		}
		return null;
	}
}

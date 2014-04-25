package net.heartsome.cat.convert.ui.model;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IAdapterFactory;

/**
 * IFile 和 FileConversionItem 的适配器工厂
 * @author cheney
 * @since JDK1.6
 */
public class FileConversionItemAdapterFactory implements IAdapterFactory {

	@SuppressWarnings("rawtypes")
	public Object getAdapter(Object adaptableObject, Class adapterType) {
		if (adapterType == IConversionItem.class) {
			return new FileConversionItem((IFile) adaptableObject);
		}
		return null;
	}

	@SuppressWarnings("rawtypes")
	public Class[] getAdapterList() {
		return new Class[] { IConversionItem.class };
	}

}

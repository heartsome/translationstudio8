package net.heartsome.cat.ts.ui.util;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map.Entry;

import net.heartsome.cat.ts.ui.resource.Messages;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ide.FileStoreEditorInput;
import org.eclipse.ui.part.FileEditorInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 用于获取数据源的辅助类 (<code>T</code> 为获取的数据源的类型)
 * @author weachy
 * @since JDK1.5
 */
public class DataSourceHelper<T> {

	private static final Logger LOGGER = LoggerFactory.getLogger(DataSourceHelper.class);

	private QualifiedName name;

	private Class<T> dataSourceClass;

	private static HashMap<String, Object> map = new HashMap<String, Object>();

	public DataSourceHelper(Class<T> dataSourceClass) {
		this.dataSourceClass = dataSourceClass;
		String dataSourcePackageName = dataSourceClass.getPackage().getName();
		String dataSourceClassName = dataSourceClass.getSimpleName();
		name = new QualifiedName(dataSourcePackageName, dataSourceClassName);
	}

	/**
	 * 获取数据源
	 * @param editorPart
	 * @return ;
	 */
	public T getDataSource(IEditorPart editorPart) {
		IEditorInput editorInput = editorPart.getEditorInput();
		return this.getDataSource(editorInput);
	}

	/**
	 * 获取数据源
	 * @param editorInput
	 * @return ;
	 */
	@SuppressWarnings("unchecked")
	public T getDataSource(IEditorInput editorInput) {
		if (editorInput instanceof FileEditorInput) {
			if (editorInput instanceof FileEditorInput) {
				FileEditorInput fileEditorInput = (FileEditorInput) editorInput;
				IFile file = fileEditorInput.getFile();
				return this.getDataSource(file);
			}
		} else if (editorInput instanceof FileStoreEditorInput) {
			FileStoreEditorInput fileStoreEditorInput = (FileStoreEditorInput) editorInput;
			Object obj = map.get(generateKey(fileStoreEditorInput));
			if (dataSourceClass.isInstance(obj)) {
				return (T) obj;
			}
		}
		return null;
	}

	/**
	 * 获取数据源
	 * @param file
	 * @return ;
	 */
	@SuppressWarnings("unchecked")
	private T getDataSource(IFile file) {
		Object obj = null;
		try {
			obj = file.getSessionProperty(name);
		} catch (CoreException e) {
			LOGGER.debug(MessageFormat.format(Messages.getString("util.DataSourceHelper.logger1"),
					dataSourceClass.getName()), e);
			e.printStackTrace();
		}
		return (T) obj;
	}

	/**
	 * 设置数据源
	 * @param file
	 * @param dataSource
	 * @return ;
	 */
	private boolean setDataSource(IFile file, T dataSource) {
		try {
			file.setSessionProperty(name, dataSource);
			return true;
		} catch (CoreException e) {
			LOGGER.debug(MessageFormat.format(Messages.getString("util.DataSourceHelper.logger2"),
					dataSourceClass.getName()), e);
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * 设置数据源
	 * @param editorInput
	 * @param dataSource
	 * @return ;
	 */
	public boolean setDataSource(IEditorInput editorInput, T dataSource) {
		if (editorInput instanceof FileEditorInput) {
			FileEditorInput fileEditorInput = (FileEditorInput) editorInput;
			IFile file = fileEditorInput.getFile();
			return setDataSource(file, dataSource);
		} else if (editorInput instanceof FileStoreEditorInput) {
			FileStoreEditorInput fileStoreEditorInput = (FileStoreEditorInput) editorInput;
			map.put(generateKey(fileStoreEditorInput), dataSource);
			return true;
		}
		return false;
	}

	/**
	 * 设置数据源
	 * @param editorPart
	 * @param dataSource
	 * @return ;
	 */
	public boolean setDataSource(IEditorPart editorPart, T dataSource) {
		IEditorInput editorInput = editorPart.getEditorInput();
		return setDataSource(editorInput, dataSource);
	}

	/**
	 * 生成保存到 Map 的键值
	 * @param fileStoreEditorInput
	 * @return ;
	 */
	private String generateKey(FileStoreEditorInput fileStoreEditorInput) {
		return fileStoreEditorInput.getURI().toString() + "\u00A0" + name.toString();
	}

	/**
	 * 从 oldInput 复制数据源到 newInput
	 * @param oldInput
	 * @param newInput
	 * @return ;
	 */
	@SuppressWarnings("unchecked")
	public static boolean copyDataSource(IEditorInput oldInput, IEditorInput newInput) {
		if (oldInput instanceof FileEditorInput) {
			FileEditorInput fileEditorInput = (FileEditorInput) oldInput;
			IFile file = fileEditorInput.getFile();
			try {
				Collection<Object> values = file.getSessionProperties().values();
				for (Object value : values) {
					DataSourceHelper helper = new DataSourceHelper(value.getClass());
					helper.setDataSource(newInput, value);
				}
				return true;
			} catch (CoreException e) {
				e.printStackTrace();
				return false;
			}
		} else if (oldInput instanceof FileStoreEditorInput) {
			FileStoreEditorInput fileStoreEditorInput = (FileStoreEditorInput) oldInput;
			for (Entry<String, Object> entry : map.entrySet()) {
				String key = entry.getKey();
				if (key.startsWith(fileStoreEditorInput.getURI().toString() + "\u00A0")) {
					Object value = map.get(key);
					DataSourceHelper helper = new DataSourceHelper(value.getClass());
					helper.setDataSource(newInput, value);
				}
			}
			return true;
		}
		return false;
	}
}

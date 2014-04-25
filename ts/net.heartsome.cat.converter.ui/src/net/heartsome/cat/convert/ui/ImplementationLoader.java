package net.heartsome.cat.convert.ui;

import java.text.MessageFormat;

import net.heartsome.cat.convert.ui.resource.Messages;

/**
 * 加载具体的 factory facade 实现
 * @author cheney
 * @since JDK1.6
 */
public final class ImplementationLoader {

	/**
	 * 
	 */
	private ImplementationLoader() {
		// 防止创建实例
	}

	/**
	 * 加载特定实现类的实例
	 * @param type
	 *            特定实现类的接口类型
	 * @return ;
	 */
	public static Object newInstance(final Class type) {
		String name = type.getName();
		Object result = null;
		try {
			result = type.getClassLoader().loadClass(name + "Impl").newInstance();
		} catch (Throwable throwable) {
			String txt = Messages.getString("ui.ImplementationLoader.msg");
			String msg = MessageFormat.format(txt, new Object[] { name });
			throw new RuntimeException(msg, throwable);
		}
		return result;
	}
}

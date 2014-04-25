package net.heartsome.cat.common.ui.shield.workbench;

import java.util.Set;

import net.heartsome.cat.common.ui.shield.AbstractShieldCommandStartup;

/**
 * 在工作台初始化后，移除不需要用到的 workbench（org.eclipse.ui 插件提供） command。
 * @author cheney
 * @since JDK1.6
 */
public class ShieldWorkbenchCommandStartup extends AbstractShieldCommandStartup {
	private final static String CONF_FILE_PATH = "/unusedWorkbenchCommand.ini";

	@Override
	protected Set<String> getUnusedCommandSet() {
		return readUnusedCommandFromFile(CONF_FILE_PATH);
	}

}

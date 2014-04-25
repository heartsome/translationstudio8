package net.heartsome.cat.common.ui.shield;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import net.heartsome.cat.common.ui.shield.resource.Messages;

import org.eclipse.core.commands.Command;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 在工作台初始化后，移除不需要用到的平台默认 command。
 * @author cheney
 * @since JDK1.6
 */
public abstract class AbstractShieldCommandStartup implements IStartup {

	private final static Logger LOGGER = LoggerFactory.getLogger(AbstractShieldCommandStartup.class);

	public void earlyStartup() {
		Set<String> unusedCommand = getUnusedCommandSet();
		IWorkbench workbench = PlatformUI.getWorkbench();
		ICommandService commandService = (ICommandService) workbench.getService(ICommandService.class);
		Command command;
		for (String commandId : unusedCommand) {
			command = commandService.getCommand(commandId);
			command.undefine();
		}
	}

	/**
	 * 不需要用到的平台默认 command id 集合。
	 * @return 不需要用到的平台默认 command id 集合，非 NULL;
	 */
	abstract protected Set<String> getUnusedCommandSet();

	@SuppressWarnings("unchecked")
	protected Set<String> readUnusedCommandFromFile(String relativePath) {
		Set<String> set = Collections.EMPTY_SET;
		File file = ShieldActivator.getFile(relativePath);
		if (file != null) {
			BufferedReader br = null;
			try {
				br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
				String line = null;
				set = new HashSet<String>();
				while ((line = br.readLine()) != null) {
					// 忽略以 # 开头注释行
					if (line.startsWith("#"))
						continue;
					line = line.trim();
					// 忽略空行
					if (line.length() == 0)
						continue;
					set.add(line);
				}
			} catch (FileNotFoundException e) {
				if (LOGGER.isErrorEnabled()) {
					String msg = Messages.getString("shield.AbstractShieldCommandStartup.logger1");
					Object[] args = { file.getAbsolutePath() };
					LOGGER.error(new MessageFormat(msg).format(args), e);
				}
			} catch (IOException e) {
				if (LOGGER.isErrorEnabled()) {
					String msg = Messages.getString("shield.AbstractShieldCommandStartup.logger2");
					Object[] args = { file.getAbsolutePath() };
					LOGGER.error(new MessageFormat(msg).format(args), e);
				}
			} finally {
				if (br != null) {
					try {
						br.close();
					} catch (IOException e) {
						if (LOGGER.isErrorEnabled()) {
							String msg = Messages.getString("shield.AbstractShieldCommandStartup.logger3");
							Object[] args = { file.getAbsolutePath() };
							LOGGER.error(new MessageFormat(msg).format(args), e);
						}
					}
				}
			}
		}
		return set;
	}

}

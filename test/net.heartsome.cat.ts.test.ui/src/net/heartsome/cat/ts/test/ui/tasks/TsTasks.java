package net.heartsome.cat.ts.test.ui.tasks;

import net.heartsome.cat.ts.test.ui.constants.TsUIConstants;
import net.heartsome.test.swtbot.utils.HSBot;

import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;

/**
 * 一些基本任务
 * @author felix_lu
 */
public final class TsTasks {

	/**
	 * 
	 */
	private TsTasks() {
	}

	/**
	 * 解析状态栏中的数据
	 * @param originalText
	 *            状态栏原始文本
	 * @param groupSign
	 *            分组标记
	 * @param delimiter
	 *            分隔符
	 * @param key
	 *            状态栏项的名称
	 * @return String 指定状态栏项的值
	 */
	public static String getStatusValueByKey(String originalText, String groupSign, String delimiter, String key) {
		originalText = originalText.replace("\n", "");
		String[] statusGroup = originalText.split(groupSign);
		String[] statusItem = null;
		for (int i = 0; i < statusGroup.length; i++) {
			String statusText = statusGroup[i];
			if (statusText != null && statusText.contains(key)) {
				statusItem = statusGroup[i].split(delimiter);
				return statusItem[1];
			}
		}
		return null;
	}
	
	/**
	 *  关掉多余的对话框，以避免影响后面执行的用例;
	 */
	public static void closeDialogs() {
		SWTBotShell[] shells = HSBot.bot().shells();
		int len = shells.length;
		if (len > 1) {
			org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences.TIMEOUT = 1000; // 减少等待时间
			for (int i = len - 1; i >= 1; i--) {
				try {
					if (shells[i].bot().button(TsUIConstants.getString("btnCancel")).isActive()) {
						shells[i].bot().button(TsUIConstants.getString("btnCancel")).click();
					}
				} catch (WidgetNotFoundException e1) {
					try {
						if (shells[i].bot().button(TsUIConstants.getString("btnOK")).isActive()) {
							shells[i].bot().button(TsUIConstants.getString("btnOK")).click();
						}
					} catch (WidgetNotFoundException e2) {
						try {
							if (shells[i].bot().button(TsUIConstants.getString("btnClose")).isActive()) {
								shells[i].bot().button(TsUIConstants.getString("btnClose")).click();
							} else {
								shells[i].close();
							}
						} catch (WidgetNotFoundException e3) {
							shells[i].close();
						}
					}
				}
			}
			org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences.TIMEOUT = 5000; // 恢复默认的超时时间
		}
	}
}

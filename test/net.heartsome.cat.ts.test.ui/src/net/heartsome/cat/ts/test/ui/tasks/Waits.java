package net.heartsome.cat.ts.test.ui.tasks;

import net.heartsome.cat.ts.test.ui.constants.TsUIConstants;
import net.heartsome.cat.ts.test.ui.waits.IsProjectExist;
import net.heartsome.cat.ts.test.ui.waits.IsProjectNotExist;
import net.heartsome.test.swtbot.utils.HSBot;
import net.heartsome.test.swtbot.waits.IsProjectDirExist;
import net.heartsome.test.swtbot.waits.IsProjectDirNotExist;
import net.heartsome.test.swtbot.waits.IsShellClosed;

import org.eclipse.swt.widgets.Shell;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.hamcrest.core.IsEqual;

/**
 * @author felix_lu
 * @version
 * @since JDK1.6
 */
public final class Waits {

	private static SWTBot bot = HSBot.bot();

	/**
	 * 
	 */
	private Waits() {
	}

	/**
	 * 等待项目在项目导航树上出现
	 * @param prjName
	 *            项目名称 ;
	 */
	public static void prjExistOnTree(String prjName) {
		bot.waitUntil(new IsProjectExist(prjName));
	}

	/**
	 * 等待项目从项目导航树上消失
	 * @param prjName
	 *            项目名称 ;
	 */
	public static void prjNotExistOnTree(String prjName) {
		bot.waitUntil(new IsProjectNotExist(prjName));
	}

	/**
	 * 等待项目目录在工作空间中出现
	 * @param prjName
	 *            ;
	 */
	public static void prjExistInWorkspace(String prjName) {
		bot.waitUntil(new IsProjectDirExist(prjName));
	}

	/**
	 * 等待项目目录从工作空间中消失
	 * @param prjName
	 *            ;
	 */
	public static void prjNotExistInWorkspace(String prjName) {
		bot.waitUntil(new IsProjectDirNotExist(prjName));
	}

	/**
	 * 等待对话框关闭
	 * @param shell
	 *            对话框 ;
	 */
	public static void shellClosed(SWTBotShell shell) {
		bot.waitUntil(new IsShellClosed(new IsEqual<Shell>(shell.widget)));
	}

	/**
	 *  判断当前激活的对话框是否为进度对话框，若是则等待其关闭，否则不做任何事 ;
	 */
	public static void progressFinished() {
		SWTBotShell dlg = bot.activeShell();
		if (dlg.getText().equals(TsUIConstants.getString("dlgTitleProgressInformation"))) {
			shellClosed(dlg);
		}
	}
}

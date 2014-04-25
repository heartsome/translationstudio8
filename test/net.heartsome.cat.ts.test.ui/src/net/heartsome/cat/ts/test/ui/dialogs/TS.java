package net.heartsome.cat.ts.test.ui.dialogs;

import net.heartsome.cat.ts.test.ui.constants.TsUIConstants;
import net.heartsome.cat.ts.test.ui.editors.XlfEditor;
import net.heartsome.cat.ts.test.ui.tasks.TsTasks;
import net.heartsome.cat.ts.test.ui.views.ProjectTreeItem;
import net.heartsome.cat.ts.test.ui.views.ProjectTreeView;
import net.heartsome.test.swtbot.finders.HsSWTWorkbenchBot;
import net.heartsome.test.swtbot.utils.HSBot;

import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotEditor;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotPerspective;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotMenu;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotToolbarButton;

/**
 * Translation Studio 程序的主界面，单例模式
 * 
 * @author felix_lu
 * @version
 * @since JDK1.6
 */
public final class TS extends SWTBotShell {

	private static HsSWTWorkbenchBot bot = HSBot.bot();
	private static TS ts;

	/**
	 * @param shell
	 */
	private TS(SWTBotShell shell) {
		super(shell.widget);
	}

	/**
	 * @return 当前的 TS 主界面实例;
	 */
	public static TS getInstance() {
		if (ts == null) {
			ts = new TS(bot.shell(TsUIConstants.getString("TsTitle")));
		}
		return ts;
	}

	/* ************ 工具栏按钮 ************ */

	/**
	 * @return 工具栏按钮：改变编辑器布局;
	 */
	public SWTBotToolbarButton tlbBtnWTltChangeEditorLayout() {
		return bot.toolbarButton(TsUIConstants
				.getString("tlbBtnWTltChangeEditorLayout"));
	}

	/* ************ 状态栏 ************ */

	/**
	 * 读取编辑器未获取焦点时状态栏中的信息
	 */
	public String getStatusBarText() {
		// TODO 需要考虑增加其他标识以提高 CLabel 的识别准确性
		return bot.clabel().getText();
	}

	/**
	 * 解析编辑器获取焦点时状态栏中的信息，根据传入的 Key 取得其在状态栏中的值
	 */
	public String getStatusBarValueByKey(String statusBarKey) {
		// TODO 需要考虑增加其他标识以提高 CLabel 的识别准确性
		String text = bot.clabel().getText();
		String groupSign = TsUIConstants.getString("stbGroupSign");
		String delimiter = TsUIConstants.getString("stbDelimiter");
		return TsTasks.getStatusValueByKey(text, groupSign, delimiter,
				statusBarKey);
	}

	/**
	 * 取得状态栏“当前文件”文字标签
	 * 
	 * @return String 状态栏“当前文件”文字标签
	 */
	public String stbiCurrentFile() {
		return getStatusBarValueByKey(TsUIConstants
				.getString("stbiCurrentFile"));
	}

	/**
	 * 取得状态栏“顺序号”文字标签
	 * 
	 * @return String 状态栏“顺序号”文字标签
	 */
	public String stbiSegmentNumber() {
		return getStatusBarValueByKey(TsUIConstants
				.getString("stbiSegmentNumber"));
	}

	/**
	 * 取得状态栏“可见文本段数”文字标签
	 * 
	 * @return String 状态栏“可见文本段数”文字标签
	 */
	public String stbiVisibleSegmentCount() {
		return getStatusBarValueByKey(TsUIConstants
				.getString("stbiVisibleSegmentCount"));
	}

	/**
	 * 取得状态栏“文本段总数”文字标签
	 * 
	 * @return String 状态栏“文本段总数”文字标签
	 */
	public String stbiSegmentTotalCount() {
		return getStatusBarValueByKey(TsUIConstants
				.getString("stbiSegmentTotalCount"));
	}

	/**
	 * 取得状态栏“用户名”文字标签
	 * 
	 * @return String 状态栏“用户名”文字标签
	 */
	public String stbiUsername() {
		return getStatusBarValueByKey(TsUIConstants.getString("stbiUsername"));
	}

	/* ************ 透视图 ************ */

	/**
	 * @return 透视图：默认布局;
	 */
	public SWTBotPerspective psptvBtnDefault() {
		return bot.perspectiveByLabel(TsUIConstants
				.getString("psptvBtnDefault"));
	}

	/* ************ 常用操作 ************ */
	/**
	 * 激活或打开编辑器，仅限合并打开的指定项目中的 XLIFF 文件
	 * 
	 * @param prjName
	 *            要打开的项目名称
	 */
	public void setCurrentFile(String prjName) {
		SWTBotEditor editor = bot.editorByTitle(prjName);
		if (editor != null) {
			editor.show();
		} else {
			ProjectTreeItem.getInstance(prjName).ctxMenuOpenProjectFiles();
		}
		// TODO: 需要完善对同名编辑器的判断和处理
	}

	/**
	 * 激活或打开编辑器，仅限项目 XLIFF 目录中的 XLIFF 文件
	 * 
	 * @param prjName
	 *            XLIFF 文件所在的项目名称
	 * @param xlfFileName
	 *            要打开的 XLIFF 文件名称
	 */
	public void setCurrentFile(String prjName, String xlfFileName) {
		SWTBotEditor editor = bot.editorByTitle(xlfFileName);
		if (editor != null) {
			editor.show();
		} else {
			ProjectTreeView.doubleClickXlfFile(prjName, xlfFileName);
		}
		// TODO: 需要完善对同名编辑器的判断和处理
	}

	/* ************ 编辑器 ************ */
	/**
	 * 不指定文件名时，取得当前激活的编辑器
	 * 
	 * @return XlfEditor 当前激活的编辑器
	 */
	public XlfEditor getXlfEditor() {
		return new XlfEditor(bot.activeEditor());
	}

	/**
	 * 得到指定文件名所在的编辑器
	 * 
	 * @param fileName
	 *            指定的文件名
	 * @return XlfEditor 得到的编辑器
	 */
	public XlfEditor getXlfEditor(String fileName) {
		SWTBotEditor editor = bot.editorByTitle(fileName);
		editor.show();
		return new XlfEditor(editor);
	}

	/* ************ 菜单项 ************ */

	/**
	 * 文件菜单
	 */
	public SWTBotMenu menuFile() {
		return bot.menu(TsUIConstants.getString("menuFile"));
	}

	/**
	 * @return 菜单：文件 > 新建;
	 */
	public SWTBotMenu menuFileNew() {
		return menuFile().menu(TsUIConstants.getString("menuFileNew"));
	}

	/**
	 * @return 菜单：文件 > 新建 > 项目...;
	 */
	public SWTBotMenu menuFileNewProject() {
		return menuFileNew()
				.menu(TsUIConstants.getString("menuFileNewProject"));
	}

	/**
	 * @return 菜单：文件 > 打开;
	 */
	public SWTBotMenu menuFileOpenFile() {
		return menuFile().menu(TsUIConstants.getString("menuFileOpenFile"));
	}

	/**
	 * @return 菜单：文件 > 关闭;
	 */
	public SWTBotMenu menuFileClose() {
		return menuFile().menu(TsUIConstants.getString("menuFileClose"));
	}

	/**
	 * @return 菜单：文件 > 关闭所有;
	 */
	public SWTBotMenu menuFileCloseAll() {
		return menuFile().menu(TsUIConstants.getString("menuFileCloseAll"));
	}

	/**
	 * @return 菜单：文件 > 保存;
	 */
	public SWTBotMenu menuFileSave() {
		return menuFile().menu(TsUIConstants.getString("menuFileSave"));
	}

	/**
	 * @return 菜单：文件 > 另存为;
	 */
	public SWTBotMenu menuFileSaveAs() {
		return menuFile().menu(TsUIConstants.getString("menuFileSaveAs"));
	}

	/**
	 * @return 菜单：文件 > 保存所有;
	 */
	public SWTBotMenu menuFileSaveAll() {
		return menuFile().menu(TsUIConstants.getString("menuFileSaveAll"));
	}

	/**
	 * @return 菜单：文件 > 退出;
	 */
	public SWTBotMenu menuFileExit() {
		return menuFile().menu(TsUIConstants.getString("menuFileExit"));
	}

	/**
	 * 编辑菜单 // TODO
	 * 
	 * @return
	 */
	public SWTBotMenu menuEdit() {
		return bot.menu(TsUIConstants.getString("menuEdit"));
	}

	/**
	 * @return 菜单：编辑 > 删除;
	 */
	public SWTBotMenu menuEditDelete() {
		return menuEdit().menu(TsUIConstants.getString("menuEditDelete"));
	}

	/**
	 * 查看菜单
	 */
	public SWTBotMenu menuView() {
		return bot.menu(TsUIConstants.getString("menuView"));
	}

	/**
	 * @return 菜单：查看 > TM 匹配面板;
	 */
	public SWTBotMenu menuViewTMMatchesPanel() {
		return menuView().menu(
				TsUIConstants.getString("menuViewTMMatchesPanel"));
	}

	/**
	 * @return 菜单：查看 > 快译面板;
	 */
	public SWTBotMenu menuViewQuickTranslationPanel() {
		return menuView().menu(
				TsUIConstants.getString("menuViewQuickTranslationPanel"));
	}

	/**
	 * @return 菜单：查看 > 批注面板;
	 */
	public SWTBotMenu menuViewNotesPanel() {
		return menuView().menu(TsUIConstants.getString("menuViewNotesPanel"));
	}

	/**
	 * @return 菜单：查看 > 术语面板;
	 */
	public SWTBotMenu menuViewTerminologyPanel() {
		return menuView().menu(
				TsUIConstants.getString("menuViewTerminologyPanel"));
	}

	/**
	 * @return 菜单：查看 > 文档属性;
	 */
	public SWTBotMenu menuViewDocumentPropertiesPanel() {
		return menuView().menu(
				TsUIConstants.getString("menuViewDocumentPropertiesPanel"));
	}

	/**
	 * 数据库菜单 // TODO
	 */
	public SWTBotMenu menuDB() {
		return bot.menu(TsUIConstants.getString("menuDB"));
	}

	/**
	 * @return 菜单：数据库 > 记忆库管理;
	 */
	public SWTBotMenu menuDBManagement() {
		return menuDB().menu(TsUIConstants.getString("menuDBManagement"));
	}
	
	/**
	 * @return 菜单：数据库 > 术语库管理;
	 */
	public SWTBotMenu menuTeriDBManagement() {
		return menuDB().menu(TsUIConstants.getString("menuTeriDBManagement"));
	}

	/**
	 * @return 菜单：数据库 > 导入 TMX;
	 */
	public SWTBotMenu menuDBImportTMXFile() {
		return menuDB().menu(TsUIConstants.getString("menuDBImportTMXFile"));
	}

	/**
	 * @return 菜单：数据库 > 导出 TMX;
	 */
	public SWTBotMenu menuDBExportAsTMX() {
		return menuDB().menu(TsUIConstants.getString("menuDBExportAsTMX"));
	}

	/**
	 * @return 菜单：数据库 > 导入 TBX;
	 */
	public SWTBotMenu menuDBImportTBXFile() {
		return menuDB().menu(TsUIConstants.getString("menuDBImportTBXFile"));
	}

	/**
	 * @return 菜单：数据库 > 导出 TBX;
	 */
	public SWTBotMenu menuDBExportAsTBX() {
		return menuDB().menu(TsUIConstants.getString("menuDBExportAsTBX"));
	}

	//
	// /**
	// * @return 菜单：数据库 > 生成 CSV 模板;
	// */
	// public SWTBotMenu menuDBCSVTemplate() {
	// return menuDB().menu(TsUIConstants.getString("menuDBCSVTemplate"));
	// }
	//
	// /**
	// * @return 菜单：数据库 > 导入 CSV 文件;
	// */
	// public SWTBotMenu menuDBImportCSVFile() {
	// return menuDB().menu(TsUIConstants.getString("menuDBImportCSVFile"));
	// }
	//
	// /**
	// * @return 菜单：数据库 > 导出 CSV 文件;
	// */
	// public SWTBotMenu menuDBExportAsCSVFile() {
	// return menuDB().menu(TsUIConstants.getString("menuDBExportAsCSVFile"));
	// }

	/**
	 * 工具菜单 // TODO
	 */

	/**
	 * 翻译菜单 // TODO
	 */
	public SWTBotMenu menuTranslation() {
		return bot.menu(TsUIConstants.getString("menuTranslation"));
	}

	/**
	 * @return 菜单项：翻译 > 预翻译;
	 */
	public SWTBotMenu menuTranslationPreTrans() {
		return menuTranslation().menu(
				TsUIConstants.getString("menuTranslationPreTrans"));
	}

	/**
	 * @return 菜单项：翻译 > 复制来源到目标;
	 */
	public SWTBotMenu menuTranslationCopySourceToTarget() {
		return menuTranslation().menu(
				TsUIConstants.getString("menuTranslationCopySourceToTarget"));
	}

	/**
	 * @return 菜单项：翻译 > 搜索术语库;
	 */
	public SWTBotMenu menuTranslationSearchTermDB() {
		return menuTranslation().menu(
				TsUIConstants.getString("menuTranslationSearchTermDB"));
	}

	/**
	 * @return 菜单项：翻译 > 相关搜索;
	 */
	public SWTBotMenu menuTranslationSearchTMDB() {
		return menuTranslation().menu(
				TsUIConstants.getString("menuTranslationSearchTMDB"));
	}

	/**
	 * @return 菜单项：翻译 > 添加术语到术语库;
	 */
	public SWTBotMenu menuTranslationAddToTermDB() {
		return menuTranslation().menu(
				TsUIConstants.getString("menuTranslationAddToTermDB"));
	}

	/**
	 * @return 菜单项：翻译 > 添加选中文本段到记忆库;
	 */
	public SWTBotMenu menuTranslationAddToTM() {
		return menuTranslation().menu(
				TsUIConstants.getString("menuTranslationAddToTM"));
	}

	/**
	 * @return 菜单项：翻译 > 添加到记忆库并跳转到下一文本段;
	 */
	public SWTBotMenu menuTranslationAddToTMAndNext() {
		return menuTranslation().menu(
				TsUIConstants.getString("menuTranslationAddToTMAndNext"));
	}

	/**
	 * @return 菜单项：翻译 > 添加到记忆库并跳转到下一非完全匹配;
	 */
	public SWTBotMenu menuTranslationAddToTMAndNextNon100Match() {
		return menuTranslation().menu(
				TsUIConstants
						.getString("menuTranslationAddToTMAndNextNon100Match"));
	}

	/**
	 * @return 菜单项：翻译 > 批准文本段;
	 */
	public SWTBotMenu menuTranslationApproveSegment() {
		return menuTranslation().menu(
				TsUIConstants.getString("menuTranslationApproveSegment"));
	}

	/**
	 * @return 菜单项：翻译 > 锁定文本段;
	 */
	public SWTBotMenu menuTranslationLockSegment() {
		return menuTranslation().menu(
				TsUIConstants.getString("menuTranslationLockSegment"));
	}

	/**
	 * @return 菜单项：翻译 > 锁定重复文本段;
	 */
	public SWTBotMenu menuTranslationLockRepetition() {
		return menuTranslation().menu(
				TsUIConstants.getString("menuTranslationLockRepetition"));
	}

	/**
	 * @return 菜单项：翻译 > 分割文本段;
	 */
	public SWTBotMenu menuTranslationSplitSegment() {
		return menuTranslation().menu(
				TsUIConstants.getString("menuTranslationSplitSegment"));
	}

	/**
	 * @return 菜单项：翻译 > 合并文本段;
	 */
	public SWTBotMenu menuTranslationMergeSegments() {
		return menuTranslation().menu(
				TsUIConstants.getString("menuTranslationMergeSegments"));
	}

	/**
	 * @return 菜单项：翻译 > 删除译文;
	 */
	public SWTBotMenu menuTranslationDeleteTranslation() {
		return menuTranslation().menu(
				TsUIConstants.getString("menuTranslationDeleteTranslation"));
	}

	/**
	 * @return 菜单项：翻译 > 删除批注;
	 */
	public SWTBotMenu menuTranslationDeleteNotes() {
		return menuTranslation().menu(
				TsUIConstants.getString("menuTranslationDeleteNotes"));
	}

	/**
	 * @return 菜单项：翻译 > 删除匹配;
	 */
	public SWTBotMenu menuTranslationDeleteMatches() {
		return menuTranslation().menu(
				TsUIConstants.getString("menuTranslationDeleteMatches"));
	}

	/**
	 * @return 菜单项：翻译 > 执行快速翻译;
	 */
	public SWTBotMenu menuTranslationQuickTranslate() {
		return menuTranslation().menu(
				TsUIConstants.getString("menuTranslationQuickTranslate"));
	}

	/**
	 * @return 菜单项：翻译 > 执行繁殖翻译;
	 */
	public SWTBotMenu menuTranslationPropagateTranslate() {
		return menuTranslation().menu(
				TsUIConstants.getString("menuTranslationPropagateTranslate"));
	}

	// /**
	// * @return 菜单：翻译 > 接受所有文本段的 100％ 匹配;
	// */
	// public SWTBotMenu menuTranslationAccept100MatchesForAllSegments() {
	// return
	// menuTranslation().menu(TsUIConstants.getString("menuTranslationAccept100MatchesForAllSegments"));
	// }
	//
	// /**
	// * @return 菜单：翻译 > 接受并批准所有文本段的 100％ 匹配;
	// */
	// public SWTBotMenu
	// menuTranslationAcceptAndApprove100MatchesForAllSegments() {
	// return
	// menuTranslation().menu(TsUIConstants.getString("menuTranslationAcceptAndApprove100MatchesForAllSegments"));
	// }
	//
	// /**
	// * @return 菜单：翻译 > 接受所有文本段的 101％ 匹配;
	// */
	// public SWTBotMenu menuTranslationAccept101MatchesForAllSegments() {
	// return
	// menuTranslation().menu(TsUIConstants.getString("menuTranslationAccept101MatchesForAllSegments"));
	// }
	//
	// /**
	// * @return 菜单：翻译 > 接受并批准所有文本段的 101％ 匹配;
	// */
	// public SWTBotMenu
	// menuTranslationAcceptAndApprove101MatchesForAllSegments() {
	// return
	// menuTranslation().menu(TsUIConstants.getString("menuTranslationAcceptAndApprove101MatchesForAllSegments"));
	// }

	/**
	 * 品质检查菜单 // TODO
	 */

	/**
	 * 项目菜单 // TODO
	 */
	public SWTBotMenu menuProject() {
		return bot.menu(TsUIConstants.getString("menuProject"));
	}

	/**
	 * @return 菜单：项目 > 项目设置;
	 */
	public SWTBotMenu menuProjectSetting() {
		return menuProject()
				.menu(TsUIConstants.getString("menuProjectSetting"));
	}

	/**
	 * @return 菜单：项目 > 文件分析 > 字数分析;
	 */
	public SWTBotMenu menuProjectWordCount() {
		return menuProject().menu(
				TsUIConstants.getString("menuProjectFileAnalysis")).menu(
				TsUIConstants.getString("menuProjectWordCount"));
	}

	/**
	 * @return 菜单：项目 > 文件分析 > 翻译进度分析;
	 */
	public SWTBotMenu menuProjectTransProgress() {
		return menuProject().menu(
				TsUIConstants.getString("menuProjectFileAnalysis")).menu(
				TsUIConstants.getString("menuProjectTransProgress"));
	}

	/**
	 * @return 菜单：项目 > 文件分析 > 编辑进度分析;
	 */
	public SWTBotMenu menuProjectEditProgress() {
		return menuProject().menu(
				TsUIConstants.getString("menuProjectFileAnalysis")).menu(
				TsUIConstants.getString("menuProjectEditProgress"));
	}

	/**
	 * @return 菜单：项目 > 更新记忆库;
	 */
	public SWTBotMenu menuProjectUpdateTM() {
		return menuProject().menu(
				TsUIConstants.getString("menuProjectUpdateTM"));
	}

	/**
	 * 帮助菜单 // TODO
	 */

}

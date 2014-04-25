package net.heartsome.cat.ts.test.ui.views;

import net.heartsome.cat.ts.test.ui.constants.TsUIConstants;
import net.heartsome.test.swtbot.utils.HSBot;

import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotToolbarButton;

/**
 * 视图：快速翻译面板，单例模式
 * @author felix_lu
 * @version
 * @since JDK1.6
 */
public final class QuickTranslationPanelView extends SWTBotView {

	private static QuickTranslationPanelView view;

	/**
	 * 按名称查找视图
	 */
	private QuickTranslationPanelView() {
		super(HSBot.bot().viewByTitle(TsUIConstants.getString("viewTitleQuickTranslationPanel")).getReference(), HSBot
				.bot());
	}

	/**
	 * @return 快速翻译视图实例;
	 */
	public static QuickTranslationPanelView getInstance() {
		if (view == null) {
			view = new QuickTranslationPanelView();
		}
		return view;
	}

	/* 按钮 */

	/**
	 * @return 工具栏按钮：接受匹配;
	 */
	public SWTBotToolbarButton tlbBtnAcceptMatch() {
		return toolbarButton(TsUIConstants.getString("tlbBtnAcceptMatch"));
	}

	/**
	 * @return 工具栏按钮：仅接受文本;
	 */
	public SWTBotToolbarButton tlbBtnAcceptTextOnly() {
		return toolbarButton(TsUIConstants.getString("tlbBtnAcceptTextOnly"));
	}

	// TODO 待实现
}

package net.heartsome.cat.ts.test.ui.views;

import net.heartsome.cat.ts.test.ui.constants.TsUIConstants;
import net.heartsome.test.swtbot.utils.HSBot;

import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotToolbarButton;

/**
 * 视图：术语面板，单例模式
 * @author felix_lu
 * @version
 * @since JDK1.6
 */
public final class TerminologyPanelView extends SWTBotView {

	private static TerminologyPanelView view;

	/**
	 * 按名称查找视图
	 */
	private TerminologyPanelView() {
		super(HSBot.bot().viewByTitle(TsUIConstants.getString("viewTitleTerminologyPanel")).getReference(), HSBot.bot());
	}

	/**
	 * @return 术语视图实例;
	 */
	public static TerminologyPanelView getInstance() {
		if (view == null) {
			view = new TerminologyPanelView();
		}
		return view;
	}

	/**
	 * @return 工具栏按钮：插入术语;
	 */
	public SWTBotToolbarButton tlbBtnInsertTerm() {
		return toolbarButton(TsUIConstants.getString("tlbBtnInsertTerm"));
	}
	// TODO 待实现
}

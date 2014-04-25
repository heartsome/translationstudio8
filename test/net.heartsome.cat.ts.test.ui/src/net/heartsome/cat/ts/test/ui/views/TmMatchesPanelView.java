package net.heartsome.cat.ts.test.ui.views;

import net.heartsome.cat.ts.test.ui.constants.TsUIConstants;
import net.heartsome.cat.ts.test.ui.tasks.TsTasks;
import net.heartsome.test.swtbot.utils.HSBot;
import net.heartsome.test.swtbot.widgets.HsSWTBotStyledText;

import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotToolbarButton;

/**
 * 视图：记忆库匹配面板，单例模式
 * @author felix_lu
 * @version
 * @since JDK1.6
 */
public final class TmMatchesPanelView extends SWTBotView {

	private SWTBot viewBot = bot();
	private static TmMatchesPanelView view;

	/**
	 * 按名称查找视图
	 */
	private TmMatchesPanelView() {
		super(HSBot.bot().viewByTitle(TsUIConstants.getString("viewTitleTmMatchesPanel")).getReference(), HSBot.bot());
	}

	/**
	 * @return 记忆库匹配面板视图实例;
	 */
	public static TmMatchesPanelView getInstance() {
		if (view == null) {
			view = new TmMatchesPanelView();
		}
		return view;
	}

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

	/**
	 * @return 工具栏按钮：在记忆库中标记匹配;
	 */
	public SWTBotToolbarButton tlbBtnFlagMatchInTm() {
		return toolbarButton(TsUIConstants.getString("tlbBtnFlagMatchInTm"));
	}

	/**
	 * @return 工具栏按钮：显示/隐藏匹配属性;
	 */
	public SWTBotToolbarButton tlbBtnToggleMatchDetail() {
		return toolbarButton(TsUIConstants.getString("tlbBtnToggleMatchDetail"));
	}

	/**
	 * @return StyledText：匹配源文本;
	 */
	public HsSWTBotStyledText matchSourceText() {
		return new HsSWTBotStyledText(viewBot.styledText(0).widget);
	}

	/**
	 * @return StyledText：匹配目标文本;
	 */
	public HsSWTBotStyledText matchTargetText() {
		return new HsSWTBotStyledText(viewBot.styledText(1).widget);
	}

	/**
	 * @return 文本：匹配的修改日期值;
	 */
	public String matchDetailModifyDate() {
		return getMatchDetail(TsUIConstants.getString("matchDetailModifyDate"));
	}

	/**
	 * @return 文本：匹配的来源值;
	 */
	public String matchDetailOriginTM() {
		return getMatchDetail(TsUIConstants.getString("matchDetailOriginTM"));
	}

	/**
	 * @return 文本：匹配的修改人值;
	 */
	public String matchDetailJobOwner() {
		return getMatchDetail(TsUIConstants.getString("matchDetailJobOwner"));
	}

	/**
	 * @return 文本：匹配的作业相关信息值;
	 */
	public String matchDetailJobInfo() {
		return getMatchDetail(TsUIConstants.getString("matchDetailJobInfo"));
	}

	/**
	 * @param key
	 * @return 文本：匹配信息中指定 key 对应的值;
	 */
	public String getMatchDetail(String key) {
		String text = viewBot.label().getText();
		String groupSign = TsUIConstants.getString("matchDetailGroupSign");
		String delimiter = TsUIConstants.getString("matchDetailDelimiter");
		return TsTasks.getStatusValueByKey(text, groupSign, delimiter, key);
	}
}

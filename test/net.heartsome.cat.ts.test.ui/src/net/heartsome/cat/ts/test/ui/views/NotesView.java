package net.heartsome.cat.ts.test.ui.views;

import net.heartsome.cat.ts.test.ui.constants.TsUIConstants;
import net.heartsome.test.swtbot.utils.HSBot;

import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTabItem;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotToolbarButton;


/**
 * 视图：批注，单例模式
 * @author  felix_lu
 * @version 
 * @since   JDK1.6
 */
public final class NotesView extends SWTBotView {
	
	private SWTBot viewBot = this.bot();
	private static NotesView view;

	/**
	 * 按名称查找视图
	 */
	private NotesView() {
		super(HSBot.bot().viewByTitle(
				TsUIConstants.getString("viewTitleNotes")).getReference(), HSBot.bot());
	}
	
	/**
	 * @return 批注视图实例;
	 */
	public static NotesView getInstance() {
		if (view == null) {
			view = new NotesView();
		}
		return view;
	}
	
	/**
	 * @return 工具栏按钮：添加批注;
	 */
	public SWTBotToolbarButton tlbBtnAddNote() {
		return toolbarButton(TsUIConstants.getString("tlbBtnAddNote"));
	}
	
	/**
	 * @return 工具栏按钮：编辑批注;
	 */
	public SWTBotToolbarButton tlbBtnEditNote() {
		return toolbarButton(TsUIConstants.getString("tlbBtnEditNote"));
	}
	
	/**
	 * @return 工具栏按钮：删除批注;
	 */
	public SWTBotToolbarButton tlbBtnDeleteNote() {
		return toolbarButton(TsUIConstants.getString("tlbBtnDeleteNote"));
	}
	
	/**
	 * @return 选项卡：批注;
	 */
	public SWTBotTabItem tabNote() {
		return viewBot.tabItem();
	}
	
	/**
	 * @param noteIndex
	 * @return 选项卡：指定序号的批注;
	 */
	public SWTBotTabItem tabNote(int noteIndex) {
		return viewBot.tabItem(noteIndex);
	}
	
	/**
	 * @param noteTitle
	 * @return 选项卡：指定标题的批注;
	 */
	public SWTBotTabItem tabNote(String noteTitle) {
		return viewBot.tabItem(noteTitle);
	}
}

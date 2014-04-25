package net.heartsome.cat.ts.ui.xliffeditor.nattable.menu;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import net.heartsome.cat.common.util.CommonFunction;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.Activator;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.actions.PopupMenuAction;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.editor.XLIFFEditorImplWithNatTable;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.resource.ImageConstant;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.resource.Messages;
import net.sourceforge.nattable.NatTable;
import net.sourceforge.nattable.config.AbstractUiBindingConfiguration;
import net.sourceforge.nattable.grid.GridRegion;
import net.sourceforge.nattable.ui.binding.UiBindingRegistry;
import net.sourceforge.nattable.ui.matcher.MouseEventMatcher;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.menus.CommandContributionItemParameter;

/**
 * BODY区域右键菜单配置
 * @author Leakey,Weachy
 * @version
 * @since JDK1.5
 */
public class BodyMenuConfiguration extends AbstractUiBindingConfiguration {

	/** BODY区域的右键菜单. */
	private Menu bodyMenu;

	/** natTable. */
	private NatTable table;

	/**
	 * 得到NatTable对象
	 * @return ;
	 */
	public NatTable getNatTable() {
		return table;
	}

	/**
	 * @param table
	 */
	public BodyMenuConfiguration(final XLIFFEditorImplWithNatTable xliffEditor) {
		this.table = xliffEditor.getTable();

		createMenu();
		
		// 将 Menu 保存到 NatTable 中，方便获取（可使用 NatTable.getData(Menu.class.getName()) 获取）
		table.setData(Menu.class.getName(), bodyMenu);

		table.addDisposeListener(new DisposeListener() {

			public void widgetDisposed(DisposeEvent e) {
				table.setData(Menu.class.getName(), null);
				if (bodyMenu != null && !bodyMenu.isDisposed()) {
					bodyMenu.dispose();
				}
			}
		});
	}

	/**
	 * (non-Javadoc)
	 * @see net.sourceforge.nattable.config.IConfiguration#configureUiBindings(net.sourceforge.nattable.ui.binding.UiBindingRegistry)
	 */
	public void configureUiBindings(UiBindingRegistry uiBindingRegistry) {
		uiBindingRegistry.registerMouseDownBinding(new MouseEventMatcher(SWT.NONE, GridRegion.BODY,
				MouseEventMatcher.RIGHT_BUTTON), new PopupMenuAction(bodyMenu));
		uiBindingRegistry.unregisterMouseDragMode(new MouseEventMatcher(SWT.NONE, GridRegion.BODY,
				MouseEventMatcher.RIGHT_BUTTON));
	}

	/**
	 * 编辑界面右键菜单
	 * @param menu
	 *            ;
	 */
	private void createMenu() {
		MenuManager menuMgr = new MenuManager();
		bodyMenu = menuMgr.createContextMenu(table.getShell());
		if (!CommonFunction.checkEdition("L")) {
			menuMgr.add(new CommandContributionItem(
					new CommandContributionItemParameter(PlatformUI
							.getWorkbench(), null, "net.heartsome.cat.database.ui.tm.command.ConcordanceSearch",
							Collections.EMPTY_MAP, Activator.getImageDescriptor(ImageConstant.TU_STATE_SEARCHTM), null, null, 
							Messages.getString("menu.BodyMenuConfiguration.searchTM"), null, null, CommandContributionItem.STYLE_PUSH,
							null, false)));
			menuMgr.add(new CommandContributionItem(
					new CommandContributionItemParameter(PlatformUI
							.getWorkbench(), null, "net.heartsome.cat.database.ui.tb.command.TermBaseSearch",
							Collections.EMPTY_MAP, Activator.getImageDescriptor(ImageConstant.TU_STATE_SEARCHTB), null, null, 
							Messages.getString("menu.BodyMenuConfiguration.searchTermItem"), null, null, CommandContributionItem.STYLE_PUSH,
							null, false)));
			menuMgr.add(new Separator());
		}
		// add by yule -webSearch
		menuMgr.add(new Separator());
		menuMgr.add(new CommandContributionItem(
				new CommandContributionItemParameter(PlatformUI
						.getWorkbench(), null, "net.heartsome.cat.ts.websearch.search",
						Collections.EMPTY_MAP, Activator.getImageDescriptor(ImageConstant.WEB_SEARCH), null, null, 
						Messages.getString("menu.BodyMenuConfiguration.webSearch"), null, null, CommandContributionItem.STYLE_PUSH,
						null, false)));
		menuMgr.add(new Separator());
		
		menuMgr.add(new CommandContributionItem(
				new CommandContributionItemParameter(PlatformUI
						.getWorkbench(), null, ActionFactory.CUT.getCommandId(),
						Collections.EMPTY_MAP, null, null, null, 
						Messages.getString("menu.BodyMenuConfiguration.cut"), null, null, CommandContributionItem.STYLE_PUSH,
						null, false)));
		menuMgr.add(new CommandContributionItem(
				new CommandContributionItemParameter(PlatformUI
						.getWorkbench(), null, ActionFactory.COPY.getCommandId(),
						Collections.EMPTY_MAP, null, null, null, 
						Messages.getString("menu.BodyMenuConfiguration.copy"), null, null, CommandContributionItem.STYLE_PUSH,
						null, false)));
		menuMgr.add(new CommandContributionItem(
				new CommandContributionItemParameter(PlatformUI
						.getWorkbench(), null, ActionFactory.PASTE.getCommandId(),
						Collections.EMPTY_MAP, null, null, null, 
						Messages.getString("menu.BodyMenuConfiguration.paste"), null, null, CommandContributionItem.STYLE_PUSH,
						null, false)));
		menuMgr.add(new Separator());
		// 此处显示出来不做任何事情，是因为用户可以通过右键看到当前状态列中显示的图标是什么含义
		// 在状态列中，是无法知道某一图标的具体含义的
		menuMgr.add(new CommandContributionItem(
				new CommandContributionItemParameter(PlatformUI
						.getWorkbench(), null, "net.heartsome.cat.ts.ui.xliffeditor.nattable.command.untranslated",
						Collections.EMPTY_MAP, Activator.getImageDescriptor("images/state/not-translated.png"), null, null, 
						Messages.getString("menu.BodyMenuConfiguration.emptyTranslationItem"), null, null, CommandContributionItem.STYLE_PUSH,
						null, false)));

		menuMgr.add(new CommandContributionItem(
				new CommandContributionItemParameter(PlatformUI
						.getWorkbench(), null, "net.heartsome.cat.ts.ui.xliffeditor.nattable.command.draft",
						Collections.EMPTY_MAP, Activator.getImageDescriptor("images/state/draft.png"), null, null, 
						Messages.getString("menu.BodyMenuConfiguration.draftItem"), null, null, CommandContributionItem.STYLE_PUSH,
						null, false)));

		Map<String, String> mapParameter = new HashMap<String, String>();
		mapParameter.put("addSegmentToTM", "addToTM");
		menuMgr.add(new CommandContributionItem(
				new CommandContributionItemParameter(PlatformUI
						.getWorkbench(), null, "net.heartsome.cat.ts.ui.xliffeditor.menu.translation.command.completeTranslation",
						mapParameter, Activator.getImageDescriptor("images/state/translated.png"), null, null, 
						Messages.getString("menu.BodyMenuConfiguration.translatedItem"), null, null, CommandContributionItem.STYLE_PUSH,
						null, false)));
		
		mapParameter = new HashMap<String, String>();
		mapParameter.put("approveSegment", "approveSelectSegment");
		menuMgr.add(new CommandContributionItem(
				new CommandContributionItemParameter(PlatformUI
						.getWorkbench(), null, "net.heartsome.cat.ts.ui.xliffeditor.nattable.command.ApproveSelectSegment",
						mapParameter, Activator.getImageDescriptor("images/state/approved.png"), null, null, 
						Messages.getString("menu.BodyMenuConfiguration.approveItem"), null, null, CommandContributionItem.STYLE_PUSH,
						null, false)));

		menuMgr.add(new CommandContributionItem(
				new CommandContributionItemParameter(PlatformUI
						.getWorkbench(), null, "net.heartsome.cat.ts.ui.xliffeditor.nattable.command.signedOff",
						Collections.EMPTY_MAP, Activator.getImageDescriptor("images/state/sign-off.png"), null, null, 
						Messages.getString("menu.BodyMenuConfiguration.signItem"), null, null, CommandContributionItem.STYLE_PUSH,
						null, false)));

		menuMgr.add(new Separator());
		
		menuMgr.add(new CommandContributionItem(
				new CommandContributionItemParameter(PlatformUI
						.getWorkbench(), null, "net.heartsome.cat.ts.ui.xliffeditor.menu.translation.lockSegment.command.lockSegment",
						Collections.EMPTY_MAP, Activator.getImageDescriptor("images/state/locked.png"), null, null, 
						Messages.getString("menu.BodyMenuConfiguration.isLockItem"), null, null, CommandContributionItem.STYLE_PUSH,
						null, false)));
		
		menuMgr.add(new CommandContributionItem(
				new CommandContributionItemParameter(PlatformUI
						.getWorkbench(), null, "net.heartsome.cat.ts.ui.xliffeditor.nattable.command.needReview",
						Collections.EMPTY_MAP, Activator.getImageDescriptor("images/state/questioning.png"), null, null, 
						Messages.getString("menu.BodyMenuConfiguration.problemItem"), null, null, CommandContributionItem.STYLE_PUSH,
						null, false)));

		menuMgr.add(new CommandContributionItem(
				new CommandContributionItemParameter(PlatformUI
						.getWorkbench(), null, "net.heartsome.cat.ts.ui.xliffeditor.nattable.command.notSendToTM",
						Collections.EMPTY_MAP, Activator.getImageDescriptor("images/state/not-sent-db.png"), null, null, 
						Messages.getString("menu.BodyMenuConfiguration.disAddToTMItem"), null, null, CommandContributionItem.STYLE_PUSH,
						null, false)));

		menuMgr.add(new Separator());
		
		menuMgr.add(new CommandContributionItem(
				new CommandContributionItemParameter(PlatformUI
						.getWorkbench(), null, "net.heartsome.cat.ts.ui.xliffeditor.nattable.command.AddNote",
						Collections.EMPTY_MAP, Activator.getImageDescriptor(ImageConstant.TU_STATE_ADDNOTE), null, null, 
						Messages.getString("menu.BodyMenuConfiguration.addNoteItem"), null, null, CommandContributionItem.STYLE_PUSH,
						null, false)));

		menuMgr.add(new CommandContributionItem(
				new CommandContributionItemParameter(PlatformUI
						.getWorkbench(), null, "net.heartsome.cat.ts.ui.xliffeditor.nattable.command.EditNote",
						Collections.EMPTY_MAP, Activator.getImageDescriptor(ImageConstant.TU_STATE_EDITNOTE), null, null, 
						Messages.getString("menu.BodyMenuConfiguration.editNoteItem"), null, null, CommandContributionItem.STYLE_PUSH,
						null, false)));

		menuMgr.add(new CommandContributionItem(
				new CommandContributionItemParameter(PlatformUI
						.getWorkbench(), null, "net.heartsome.cat.ts.ui.xliffeditor.menu.translateContent.command.deleteCurrentSegmentNotes",
						Collections.EMPTY_MAP, Activator.getImageDescriptor(ImageConstant.TU_STATE_DELETENOTE), null, null, 
						Messages.getString("menu.BodyMenuConfiguration.deleteNoteItem"), null, null, CommandContributionItem.STYLE_PUSH,
						null, false)));

		menuMgr.add(new Separator());
		
		menuMgr.add(new CommandContributionItem(
				new CommandContributionItemParameter(PlatformUI
						.getWorkbench(), null, "net.heartsome.cat.ts.ui.xliffeditor.menu.translateContent.command.deleteCurrentSegmentTranslations",
						Collections.EMPTY_MAP, Activator.getImageDescriptor(ImageConstant.TU_STATE_DELETETRANS), null, null, 
						Messages.getString("menu.BodyMenuConfiguration.deleteTransItem"), null, null, CommandContributionItem.STYLE_PUSH,
						null, false)));

		if (CommonFunction.checkEdition("F") || CommonFunction.checkEdition("U")) {
			menuMgr.add(new Separator());
			menuMgr.add(new CommandContributionItem(
					new CommandContributionItemParameter(PlatformUI
							.getWorkbench(), null, "net.heartsome.cat.ts.handlexlf.commands.setSplitPointByWordNumCommand",
							Collections.EMPTY_MAP, null, null, null, 
							Messages.getString("menu.BodyMenuConfiguration.setSplitPointByWordNum"), null, null, CommandContributionItem.STYLE_PUSH,
							null, false)));
			menuMgr.add(new CommandContributionItem(
					new CommandContributionItemParameter(PlatformUI
							.getWorkbench(), null, "net.heartsome.cat.ts.handlexlf.commands.nextSplitPointCommand",
							Collections.EMPTY_MAP, null, null, null, 
							Messages.getString("menu.BodyMenuConfiguration.nextSplitPoint"), null, null, CommandContributionItem.STYLE_PUSH,
							null, false)));
			menuMgr.add(new CommandContributionItem(
					new CommandContributionItemParameter(PlatformUI
							.getWorkbench(), null, "net.heartsome.cat.ts.ui.xliffeditor.nattable.command.splitXLIFFPoint",
							Collections.EMPTY_MAP, Activator.getImageDescriptor("images/state/cut-point.png"), null, null, 
							Messages.getString("menu.BodyMenuConfiguration.splitPointItem"), null, null, CommandContributionItem.STYLE_PUSH,
							null, false)));
		}
	}

	public Menu getMenu() {
		return bodyMenu;
	}
}

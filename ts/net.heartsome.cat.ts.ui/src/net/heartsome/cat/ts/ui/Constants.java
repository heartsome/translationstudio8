package net.heartsome.cat.ts.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * TS 中 UI 相关的常量类
 * @author weachy
 * @version
 * @since JDK1.5
 */
public class Constants {
	
	/** symbolic font name for the XLIFF editor */
	public static final String XLIFF_EDITOR_TEXT_FONT = "net.heartsome.cat.ts.ui.xliffeditor.font";
	
	/** symbolic font name for the match viewer TB & TB */
	public static final String MATCH_VIEWER_TEXT_FONT = "net.heartsome.cat.ts.ui.matchViewer.font";
	
	public static final char TAB_CHARACTER = '\u2192';
	public static final char LINE_SEPARATOR_CHARACTER= '\u21B2';
	public static final char SPACE_CHARACTER = '\u2219';	
	public static final Pattern NONPRINTING_PATTERN = Pattern.compile("[\u2192\u21B2\u2219]+");
	/**
	 * NatTable 中每条文本段的内容行间距。
	 */
	public static final int SEGMENT_LINE_SPACING = 1;
	
	public static final int SEGMENT_TOP_MARGIN = 2;
	
	public static final int SEGMENT_BOTTOM_MARGIN = 2;
	
	public static final int SEGMENT_RIGHT_MARGIN = 2;
	
	public static final int SEGMENT_LEFT_MARGIN = 2;
	
	/** 快捷键列表中无用的项 */
	private static final String[] arrRemove = new String[]{
			"org.eclipse.ui.file.export", "org.eclipse.ui.newWizard", "org.eclipse.ui.window.lockToolBar",
			"org.eclipse.ui.window.minimizePart", "org.eclipse.ui.file.print", "org.eclipse.ui.ide.configureColumns",
			"org.eclipse.ui.file.import", "org.eclipse.ui.window.activateEditor", "org.eclipse.ui.project.properties",
			"org.eclipse.ui.navigate.next", "org.eclipse.ui.help.helpSearch", "org.eclipse.ui.window.resetPerspective",
			"org.eclipse.ui.window.previousEditor", "org.eclipse.ui.file.save", "org.eclipse.ui.file.properties", 
			"org.eclipse.ui.window.previousView", "org.eclipse.ui.edit.text.contentAssist.proposals", //"org.eclipse.ui.file.closeAll",
			"org.eclipse.ui.window.showViewMenu", "org.eclipse.ui.window.previousPerspective", "org.eclipse.ui.project.closeUnrelatedProjects",
			"org.eclipse.ui.texteditor.TaskRulerAction", "org.eclipse.ui.texteditor.BookmarkRulerAction", "org.eclipse.ui.navigate.previousSubTab",
			"org.eclipse.ui.help.dynamicHelp", "org.eclipse.ui.navigate.forwardHistory", "org.eclipse.ui.ide.configureFilters",
			"org.eclipse.ui.window.nextPerspective", "org.eclipse.ui.edit.text.contentAssist.contextInformation", "org.eclipse.ui.navigate.nextSubTab",
			"org.eclipse.ui.file.import", "org.eclipse.ui.navigate.collapseAll", "org.eclipse.ui.window.nextEditor",
			"org.eclipse.ui.part.nextPage", "org.eclipse.ui.window.switchToEditor", "org.eclipse.ui.window.showSystemMenu", 
			"org.eclipse.ui.part.previousPage", "org.eclipse.ui.navigate.linkWithEditor", "org.eclipse.ui.file.closeOthers",
			"org.eclipse.ui.navigate.previous", "org.eclipse.ui.file.saveAll", "org.eclipse.ui.help.displayHelp",
			"org.eclipse.ui.window.showKeyAssist", "org.eclipse.ui.help.aboutAction", "org.eclipse.ui.window.nextView",
			"org.eclipse.ui.navigate.backwardHistory", "org.eclipse.ui.file.closeOthers=", "org.eclipse.ui.window.openEditorDropDown",
			"org.eclipse.ui.window.maximizePart", "org.eclipse.ui.window.preferences", "org.eclipse.ui.navigate.removeFromWorkingSet",
			"org.eclipse.ui.edit.text.gotoLastEditPosition", "org.eclipse.ui.edit.text.select.textStart", "org.eclipse.ui.project.buildAll",
			"org.eclipse.ui.browser.openBundleResource", "org.eclipse.ui.edit.text.deletePrevious", "org.eclipse.ui.file.revert", 
			"org.eclipse.ui.window.hideShowEditors", "org.eclipse.ui.edit.text.goto.columnPrevious", "org.eclipse.ui.edit.text.clear.mark", 
			"org.eclipse.ui.edit.text.moveLineUp", "org.eclipse.ui.navigate.showIn", "org.eclipse.ui.edit.text.folding.toggle", 
			"org.eclipse.ui.ide.markCompleted", "org.eclipse.ui.edit.text.select.lineUp", "org.eclipse.ui.edit.addTask",
			"org.eclipse.ui.edit.text.removeTrailingWhitespace", "org.eclipse.ui.edit.text.deleteNext", "org.eclipse.ui.edit.text.goto.windowStart",
			"org.eclipse.ui.window.newEditor", "org.eclipse.ui.update.findAndInstallUpdates", "org.eclipse.ui.edit.text.hippieCompletion",
			"org.eclipse.ui.edit.text.copyLineDown", "org.eclipse.ui.views.showView", "org.eclipse.ui.edit.text.swap.mark",
			"org.eclipse.ui.project.rebuildProject", "org.eclipse.ui.project.buildAutomatically", "org.eclipse.ui.edit.text.smartEnterInverse",
			"org.eclipse.ui.edit.text.select.pageUp", "org.eclipse.ui.navigate.nextTab", "org.eclipse.ui.project.buildProject",
			"org.eclipse.ui.edit.text.select.wordPrevious", "org.eclipse.jdt.ui.edit.text.java.correction.assist.proposals", "org.eclipse.ui.window.spy",
			"org.eclipse.ui.edit.text.select.pageDown", "org.eclipse.ui.edit.text.folding.expand_all", "org.eclipse.ui.window.closePerspective",
			"org.eclipse.ui.edit.text.upperCase", "org.eclipse.ui.edit.text.goto.lineDown", "org.eclipse.ui.views.properties.NewPropertySheetCommand",
			"org.eclipse.ui.edit.text.toggleOverwrite", "org.eclipse.ui.edit.findNext", "org.eclipse.ui.navigate.back",
			"org.eclipse.ui.navigate.up", "org.eclipse.ui.edit.text.delimiter.unix", "org.eclipse.ui.edit.text.showRulerContextMenu",
			"org.eclipse.ui.help.tipsAndTricksAction", "org.eclipse.ui.perspectives.showPerspective", "org.eclipse.quickdiff.toggle",
			"org.eclipse.ui.navigate.expandAll", "org.eclipse.help.ui.closeTray", "AUTOGEN:::org.eclipse.ui.texteditor.ruler.actions/org.eclipse.ui.texteditor.BookmarkRulerAction",
			"org.eclipse.ui.edit.text.delete.line.to.end", "org.eclipse.ui.edit.text.open.hyperlink", "org.eclipse.ui.ToggleCoolbarAction",
			"org.eclipse.ui.edit.text.deleteNextWord", "org.eclipse.ui.window.savePerspective", "org.eclipse.ui.window.newWindow",
			"org.eclipse.ui.edit.text.openLocalFile", "org.eclipse.ui.ide.copyConfigCommand", "org.eclipse.ui.edit.text.join.lines",
			"org.eclipse.ui.edit.text.folding.collapse", "org.eclipse.ui.edit.text.delimiter.macOS9", "org.eclipse.ui.edit.text.goto.lineStart",
			"org.eclipse.ui.file.newQuickMenu", "org.eclipse.ui.editors.revisions.id.toggle", "AUTOGEN:::org.eclipse.ui.texteditor.ruler.context.actions/org.eclipse.ui.texteditor.TaskRulerAction",
			"org.eclipse.ui.edit.text.toggleShowSelectedElementOnly", "org.eclipse.ui.window.pinEditor", "org.eclipse.ui.edit.text.toggleShowWhitespaceCharacters",
			"org.eclipse.ui.edit.text.goto.windowEnd", "org.eclipse.ui.edit.text.copyLineUp", "org.eclipse.ui.edit.text.cut.line.to.end",
			"org.eclipse.ui.edit.text.goto.line", "org.eclipse.ui.navigate.showInQuickMenu", "org.eclipse.ui.edit.text.goto.lineEnd",
			"org.eclipse.ui.edit.text.toggleBlockSelectionMode", "org.eclipse.ui.window.quickAccess", "org.eclipse.ui.edit.text.select.lineEnd",
			"org.eclipse.ui.editors.revisions.rendering.cycle", "org.eclipse.ui.file.openWorkspace", "org.eclipse.ui.edit.text.set.mark",
			"org.eclipse.ui.edit.text.toggleInsertMode", "org.eclipse.ui.edit.text.select.columnNext", "org.eclipse.ui.edit.text.lowerCase",
			"AUTOGEN:::org.eclipse.ui.texteditor.ruler.context.actions/org.eclipse.ui.texteditor.BookmarkRulerAction","org.eclipse.ui.edit.text.folding.restore", "org.eclipse.ui.edit.text.goto.columnNext",
			"org.eclipse.ui.edit.text.recenter", "org.eclipse.ui.edit.text.deletePreviousWord", "org.eclipse.ui.editors.lineNumberToggle",
			"org.eclipse.ui.edit.text.scroll.lineDown", "org.eclipse.ui.file.saveAs", "org.eclipse.ui.edit.text.select.windowEnd",
			"org.eclipse.ui.edit.text.goto.pageDown", "AUTOGEN:::org.eclipse.ui.texteditor.ruler.actions/org.eclipse.ui.texteditor.SelectRulerAction", "org.eclipse.ui.navigate.goToResource",
			"org.eclipse.ui.window.closeAllPerspectives", "org.eclipse.ui.edit.text.select.lineDown", "org.eclipse.ui.edit.text.showRulerAnnotationInformation",
			"org.eclipse.ui.edit.text.goto.textEnd", "org.eclipse.ui.editors.quickdiff.revert", "org.eclipse.ui.edit.text.showChangeRulerInformation",
			"org.eclipse.ui.edit.text.scroll.lineUp", "org.eclipse.ui.navigate.goInto", "org.eclipse.ui.edit.text.folding.expand",
			"org.eclipse.ui.file.restartWorkbench", "org.eclipse.ui.edit.text.smartEnter", "org.eclipse.ui.edit.text.cut.line.to.beginning",
			"org.eclipse.ui.edit.text.goto.pageUp", "org.eclipse.ui.edit.findPrevious", "org.eclipse.ui.update.manageConfiguration",
			"org.eclipse.ui.edit.text.select.lineStart", "org.eclipse.ui.help.installationDialog", "org.eclipse.ui.project.rebuildAll",
			"org.eclipse.ui.project.cleanAction", "org.eclipse.ui.navigate.addToWorkingSet", "org.eclipse.ui.navigate.selectWorkingSets",
			"org.eclipse.ui.edit.text.delimiter.windows", "org.eclipse.ui.editors.revisions.author.toggle", "org.eclipse.ui.window.customizePerspective",
			"org.eclipse.ui.navigate.forward", "org.eclipse.ui.ide.OpenMarkersView", "org.eclipse.ui.edit.text.select.textEnd",
			"org.eclipse.ui.edit.text.goto.wordPrevious", "org.eclipse.ui.edit.text.showInformation", "org.eclipse.ui.ide.copyBuildIdCommand",
			"org.eclipse.ui.edit.text.moveLineDown", "org.eclipse.ui.edit.text.delete.line.to.beginning", "org.eclipse.ui.ide.deleteCompleted",
			"org.eclipse.ui.edit.addBookmark", "org.eclipse.ui.edit.text.select.windowStart", "org.eclipse.ui.navigate.previousTab",
			"org.eclipse.ui.edit.text.select.wordNext", "org.eclipse.ui.project.buildLast", "org.eclipse.ui.edit.findIncremental",
			"org.eclipse.ui.edit.text.select.columnPrevious", "org.eclipse.ui.edit.findIncrementalReverse", "org.eclipse.ui.edit.text.goto.wordNext",
			"org.eclipse.ui.navigate.openResource", "org.eclipse.ui.project.openProject", "org.eclipse.ui.edit.text.shiftLeft",
			"org.eclipse.ui.edit.move", "org.eclipse.ui.file.closePart", "org.eclipse.ui.edit.text.goto.lineUp",
			"org.eclipse.ui.edit.text.delete.line", "org.eclipse.ui.edit.text.goto.textStart", "org.eclipse.ui.help.quickStartAction",
			"org.eclipse.ui.edit.text.shiftRight", "org.eclipse.ui.editors.quickdiff.revertLine", "org.eclipse.help.ui.indexcommand",
			"org.eclipse.ui.edit.text.folding.collapse_all", "org.eclipse.ui.edit.revertToSaved", "org.eclipse.ui.edit.text.cut.line",
			"org.eclipse.ui.project.closeProject", "org.eclipse.ui.cocoa.closeDialog", "org.eclipse.ui.cocoa.zoomWindow",
			"org.eclipse.ui.cocoa.arrangeWindowsInFront", "org.eclipse.ui.cocoa.minimizeWindow", "net.heartsome.cat.ts.command.newMenu.pulldown",
			"net.heartsome.cat.convert.ui.commands.OpenMergedXliffCommand"
	};
	
	public static final List<String> lstRemove = new ArrayList<String>(Arrays.asList(arrRemove));
	
}

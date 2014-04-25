package net.heartsome.cat.ts.test.ui.editors;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.heartsome.cat.common.ui.utils.InnerTagUtil;
import net.heartsome.cat.ts.core.file.XLFHandler;
import net.heartsome.cat.ts.test.ui.constants.TsUIConstants;
import net.heartsome.cat.ts.test.ui.dialogs.TS;
import net.heartsome.cat.ts.test.ui.utils.XliffUtil;
import net.heartsome.cat.ts.test.ui.waits.IsCellEditMode;
import net.heartsome.cat.ts.test.ui.waits.IsEditorLayoutHorizontal;
import net.heartsome.cat.ts.test.ui.waits.IsEditorLayoutVertical;
import net.heartsome.cat.ts.test.ui.waits.IsSegmentSelected;
import net.heartsome.cat.ts.ui.editors.IXliffEditor;
import net.heartsome.test.swtbot.finders.HsSWTWorkbenchBot;
import net.heartsome.test.swtbot.utils.HSBot;
import net.heartsome.test.swtbot.waits.IsCellSelected;
import net.heartsome.test.swtbot.waits.IsComboEquals;
import net.heartsome.test.swtbot.widgets.HsSWTBotStyledText;
import net.heartsome.test.swtbot.widgets.SWTBotNatTable;
import net.heartsome.test.utilities.nattable.LayerUtil;

import org.eclipse.core.runtime.Assert;
import org.eclipse.swtbot.eclipse.finder.finders.WorkbenchContentsFinder;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotEditor;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.SWTBotAssert;
import org.eclipse.swtbot.swt.finder.keyboard.Keystrokes;
import org.eclipse.swtbot.swt.finder.utils.Position;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotButton;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotCombo;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotText;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotToolbarButton;
import org.eclipse.ui.IEditorPart;

/**
 * XLIFF 编辑器
 * @author felix_lu
 * @version
 * @since JDK1.6
 */
public class XlfEditor extends SWTBotEditor {

	private static HsSWTWorkbenchBot bot = HSBot.bot();
	private SWTBot editorBot = this.bot();
	private TS ts = TS.getInstance();
	/** 记录最后输入的跳转行数 */
	private String lineNumLastValue = TsUIConstants.getString("txtLineNumber");


	/**  StyleText 中显示标记用的分隔符，不可见字符 */
	public static final char INVISIBLE_CHAR = InnerTagUtil.INVISIBLE_CHAR;
	/**  编辑器的内容区 NatTable */
	public SWTBotNatTable nattable;

	/**
	 * 按指定的编辑器对象创建
	 * @param editor
	 */
	public XlfEditor(SWTBotEditor editor) {
		super(editor.getReference(), bot);
		getNatTable();
	}

	/**
	 * ************ XLIFF 编辑器的一些基本操作 ************ 包括语言对过滤器、文本段过滤器
	 */

	/**
	 * 得到指定文件名所在的编辑器
	 * @param fileName
	 *            指定的文件名
	 * @return XlfEditor 得到的编辑器
	 */
	// public static XlfEditor getInstance(String fileName) {
	// return new XlfEditor(bot.editorByTitle(fileName));
	// }

	/**
	 * 不指定文件名时，取得当前激活的编辑器
	 * @return XlfEditor 当前激活的编辑器
	 */
	// public static XlfEditor getInstance() {
	// return new XlfEditor(bot.activeEditor());
	// }

	/**
	 * @return SWTBotCombo 编辑器中的语言对下拉列表
	 */
	public SWTBotCombo getLangCombo() {
		Pattern langCode = Pattern.compile("[a-z]{2}(-[A-Z]{2})? -> [a-z]{2}(-[A-Z]{2})?");
		int index = 0;
		while (true) {
			Matcher m = langCode.matcher(editorBot.comboBox(index).getText());
			if (m.find()) {
				return editorBot.comboBox(index);
			}
			index++;
		}
	}

	/**
	 * 选择指定的语言对
	 * @param srcLang
	 *            源语言代码
	 * @param tgtLang
	 *            目标语言代码
	 */
	public void selectLangPair(String srcLang, String tgtLang) {
		String langPair = srcLang + " -> " + tgtLang;
		SWTBotCombo langCombo = getLangCombo();
		langCombo.setSelection(langPair);
		bot.waitUntil(new IsComboEquals(langCombo, langPair));
		SWTBotAssert.assertText(langPair, langCombo);
	}

	/**
	 * 取得当前源语言代码
	 * @return String 当前源语言代码
	 */
	public String getSourceLangCode() {
		return getLangCombo().getText().split(" -> ")[0];
	}

	/**
	 * 取得当前目标语言代码
	 * @return String 当前目标语言代码
	 */
	public String getTargetLangCode() {
		return getLangCombo().getText().split(" -> ")[1];
	}

	/**
	 * 得到文本段过滤器下拉列表
	 * @return SWTBotCombo 文本段过滤器下拉列表
	 */
	public SWTBotCombo getSegFilterCombo() {
		String defaultFilter = TsUIConstants.getString("cmbSegFilterAllSegments");
		int index = 0;
		while (true) {
			SWTBotCombo segFilterCombo = editorBot.comboBox(index);
			if (segFilterCombo.getText().equals(defaultFilter)) {
				return segFilterCombo;
			}
			index++;
		}
	}

	/**
	 * 选择指定的文本段过滤器
	 * @param filterName
	 *            过滤器名称
	 */
	public void selectSegFilter(final String filterName) {
		SWTBotCombo segFilterCombo = getSegFilterCombo();
		segFilterCombo.setSelection(filterName);
		bot.waitUntil(new IsComboEquals(segFilterCombo, filterName));
		SWTBotAssert.assertText(filterName, segFilterCombo);
	}

	/**
	 * 添加自定义过滤器按钮
	 * @return SWTBotButton
	 */
	public SWTBotButton btnAddCustomFilter() {
		return editorBot.button(TsUIConstants.getString("btnAddCustomFilter"));
	}

	/**************
	 * XLIFF 编辑器中的 NatTable 相关操作 ************ 包括切换布局、文本段跳转、源/目标文本的选择等
	 */

	/**
	 * 取得编辑器中的 NatTable
	 * @return SWTBotNatTable 编辑器中的 NatTable
	 */
	public SWTBotNatTable getNatTable() {
		nattable = bot.natTable();
		return nattable;
	}

	/**
	 * 点击指定的右键菜单
	 * @param texts 右键菜单文本，每一级菜单为一个参数;
	 */
	public void clickContextMenu(String... texts) {
		getNatTable().clickContextMenu(texts);
	}

	/**
	 * @return List&lt;String&gt; 当前文件中所有文本段的 RowID，合并、分割文本段后应重新获取 （每个 RowID 由 XLIFF 文件路径、源文件路径、trans-unit ID 组成）
	 */
	public List<String> getAllRowIds() {
		WorkbenchContentsFinder finder = new WorkbenchContentsFinder();
		IEditorPart activateEditor = finder.activeWorkbenchWindow().getActivePage().getActiveEditor();
		IXliffEditor xliffEditor = (IXliffEditor) activateEditor;
		XLFHandler handler = xliffEditor.getXLFHandler();
		return handler.getAllRowIds();
	}

	/**
	 * @return List&lt;String&gt; 当前所有可见文本段的 RowID，合并、分割文本段后应重新获取
	 */
	public ArrayList<String> getRowIds() {
		WorkbenchContentsFinder finder = new WorkbenchContentsFinder();
		IEditorPart activateEditor = finder.activeWorkbenchWindow().getActivePage().getActiveEditor();
		IXliffEditor xliffEditor = (IXliffEditor) activateEditor;
		XLFHandler handler = xliffEditor.getXLFHandler();
		return handler.getRowIds();
	}

	/**
	 * @param segNum
	 *            文本段序号，合并、分割文本段和排序后应重新获取
	 * @return String 文本段 RowID
	 */
	public String rowIdOfSegNum(int segNum) {
		Assert.isTrue(segNum > 0, "Invalid segment number: " + segNum);
		return getRowIds().get(segNum - 1);
	}

	/**
	 * @return String 当前选中的文本段 RowID
	 */
	public String rowIdOfSelectedSeg() {
		getNatTable();
		int colPosOfLineNumHeader = nattable.positionOfColumn(lblNatTableHeaderLineNum());
		int rowPosOfLineSelected = nattable.positionOfSelectedRow(colPosOfLineNumHeader);
		String segNum = nattable.getTextByPosition(rowPosOfLineSelected, colPosOfLineNumHeader);
		return rowIdOfSegNum(Integer.valueOf(segNum) - 1);
	}

	/**
	 * @param segNum
	 *            文本段序号，合并、分割文本段和排序后应重新获取
	 * @return String 文本段的 trans-unit id
	 */
	public String tuidOfSegNum(int segNum) {
		String rowID = rowIdOfSegNum(segNum);
		XliffUtil xu = new XliffUtil(rowID);
		return xu.getAttributeOfTU("id");
	}

	/**
	 * @return String 当前选中的文本段的 trans-unit id
	 */
	public String tuidOfSelectedSeg() {
		String rowID = rowIdOfSelectedSeg();
		XliffUtil xu = new XliffUtil(rowID);
		return xu.getAttributeOfTU("id");
	}

	/**
	 * 判断指定文件的编辑器是否为水平布局
	 * @return boolean true 表示水平布局、false 表示垂直布局
	 */
	public boolean isHorizontalLayout() {
		Pattern columnHeader = Pattern.compile("[a-z]{2}(-[A-Z]{2})? -> [a-z]{2}(-[A-Z]{2})?");
		int columnCount = getNatTable().columnCount();
		for (int i = 0; i < columnCount; i++) {
			Matcher m = columnHeader.matcher(getNatTable().getTextByPosition(0, i));
			if (m.find()) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 修改编辑器布局 将编辑器布局由水平改为垂直，或反之
	 */
	public void changeEditorLayout() {
		XlfEditor xe = new XlfEditor(bot.activeEditor());
		SWTBotToolbarButton changeEditorLayout = TS.getInstance().tlbBtnWTltChangeEditorLayout();

		if (xe.isHorizontalLayout()) {
			changeEditorLayout.click();
			// 修改布局会导致 NatTable 重绘，重新赋值以更新其他地方的引用
			getNatTable();

			bot.waitUntil(new IsEditorLayoutVertical(xe));
		} else {
			changeEditorLayout.click();
			getNatTable();

			bot.waitUntil(new IsEditorLayoutHorizontal(xe));
		}
	}

	/**
	 * 跳到指定文本段
	 * @param segNum
	 *            文本段行号
	 */
	public void gotoSeg(int segNum) {
		getNatTable();
		nattable.click(1, 1);
		int targetRowIndex;
		if (isHorizontalLayout()) {
			targetRowIndex = segNum - 1;
		} else {
			targetRowIndex = (segNum - 1) * 2;
		}
		int selectedRowIndex = nattable.indexOfSelectedRow(positionOfTargetTextColumn());

		// 先判断指定文本段是否已经被选中，若未被选中才继续
		if (segNum != 1 && targetRowIndex != selectedRowIndex) {
			SWTBotText text = editorBot.text(lineNumLastValue);
			text.setText(String.valueOf(segNum));
			text.pressShortcut(Keystrokes.LF);
			lineNumLastValue = String.valueOf(segNum);
			// 确认选中了指定文本段
			bot.waitUntil(new IsSegmentSelected(this, targetRowIndex));
		}
	}

	/**
	 * @return int 当前 NatTable 中的总文本段数量
	 */
	public int segCount() {
		getNatTable();
		LayerUtil.setBodyLayerPosition(0, 1); // 设置 NatTable 的 Body 位置
		// 每次重绘 NatTable 后需要重新获取该 DataLayer
		int lineCount = nattable.rowCountData(); // 计算数据行数
		if (isHorizontalLayout()) {
			return lineCount;
		} else {
			return lineCount / 2;
		}
	}

	/**
	 * @return int 当前编辑器中源语言所在列的 Position
	 */
	public int positionOfSourceTextColumn() {
		getNatTable();
		if (isHorizontalLayout()) {
			return nattable.positionOfColumn(getSourceLangCode());
		} else {
			return nattable.positionOfColumn(getLangCombo().getText());
		}
	}

	/**
	 * @return int 当前编辑器中源语言所在列的 Index
	 */
	public int indexOfSourceTextColumn() {
		getNatTable();
		if (isHorizontalLayout()) {
			return nattable.indexOfColumn(getSourceLangCode());
		} else {
			return nattable.indexOfColumn(getLangCombo().getText());
		}
	}

	/**
	 * @return int 当前编辑器中目标语言所在列的 Position
	 */
	public int positionOfTargetTextColumn() {
		getNatTable();
		if (isHorizontalLayout()) {
			return nattable.positionOfColumn(getTargetLangCode());
		} else {
			return nattable.positionOfColumn(getLangCombo().getText());
		}
	}

	/**
	 * @return int 当前编辑器中目标语言所在列的 Index
	 */
	public int indexOfTargetTextColumn() {
		getNatTable();
		if (isHorizontalLayout()) {
			return nattable.indexOfColumn(getTargetLangCode());
		} else {
			return nattable.indexOfColumn(getLangCombo().getText());
		}
	}

	/**
	 * @param text
	 *            文本内容，其中的内部标记为源代码形式（必须对其中的双引号进行转义）
	 * @return int 从第一个文本段起，源文本内容等于指定内容的文本段序号（精确匹配）
	 */
	public int segNumOfSource(String text) {
		return segNumOfSource(text, 1);
	}

	/**
	 * @param text
	 *            文本内容，其中的内部标记为源代码形式（必须对其中的双引号进行转义）
	 * @param fromSegNum
	 *            查找的起始文本段序号
	 * @return int 从指定序号的文本段起，源文本内容等于指定内容的文本段序号（精确匹配）
	 */
	public int segNumOfSource(String text, int fromSegNum) {
		if (text == null) {
			return -1;
		}
		getNatTable();
		nattable.click(1, 1);
		int columnPosition = positionOfSourceTextColumn();
		int rowPosition;
		String srcText;
		selectSourceCell(fromSegNum);
		for (int i = fromSegNum; i <= segCount(); i++) {
			rowPosition = nattable.positionOfSelectedRow();
			srcText = nattable.getTextByPosition(rowPosition, columnPosition);
			if (text.equals(srcText) || text.equals(XliffUtil.tagged(srcText))) {
				// 考虑标记的两种显示状态
				int columnHeaderPosition = nattable.positionOfColumn(TsUIConstants
						.getString("lblNatTableHeaderLineNum"));
				return Integer.valueOf(nattable.getTextByPosition(rowPosition, columnHeaderPosition));
			}
			nattable.pressShortcut(Keystrokes.DOWN); // 输入向下方向键
		}
		return -1;
	}

	/**
	 * @param text
	 *            文本内容，其中的内部标记为源代码形式（必须对其中的双引号进行转义）
	 * @return int 从第一个文本段起，源文本内容包含指定内容的文本段序号（模糊匹配）
	 */
	public int segNumContainsSource(String text) {
		return segNumContainsSource(text, 1);
	}

	/**
	 * @param text
	 *            文本内容，其中的内部标记为源代码形式（必须对其中的双引号进行转义）
	 * @param fromSegNum
	 *            查找的起始文本段序号
	 * @return int 从指定序号的文本段起，源文本内容包含指定内容的文本段序号（模糊匹配）
	 */
	public int segNumContainsSource(String text, int fromSegNum) {
		if (text == null) {
			return -1;
		}
		getNatTable();
		int columnPosition = positionOfSourceTextColumn();
		int rowPosition;
		String srcText;
		selectSourceCell(fromSegNum);
		for (int i = fromSegNum; i <= segCount(); i++) {
			rowPosition = nattable.positionOfSelectedRow();
			srcText = nattable.getTextByPosition(rowPosition, columnPosition);
			if (srcText != null && (srcText.contains(text) || XliffUtil.tagged(srcText).contains(text))) {
				// 考虑标记的两种显示状态
				int columnHeaderPosition = nattable.positionOfColumn(TsUIConstants
						.getString("lblNatTableHeaderLineNum"));
				return Integer.valueOf(nattable.getTextByPosition(rowPosition, columnHeaderPosition));
			}
			nattable.pressShortcut(Keystrokes.DOWN); // 输入向下方向键
		}
		return -1;
	}

	/**
	 * 选择指定行的源文本
	 * @param segNum
	 *            欲选择的文本段行号
	 */
	public void selectSourceCell(int segNum) {
		getNatTable();
		gotoSeg(segNum); // 先跳转到该文本段，确保其被显示

		int sourceColumnPosition = positionOfSourceTextColumn();
		int selectedRowPosition = nattable.positionOfSelectedRow(sourceColumnPosition);
		// 先点击一下行首，保证目标文本框完全显示在界面上
		nattable.click(selectedRowPosition, 0);
		// 重新取当前选中行的位置，因为刚才的点击可能引起了改变
		selectedRowPosition = nattable.positionOfSelectedRow(sourceColumnPosition);

		// 这两个 Index 用来判断指定的单元格在操作完成后是否确实被选中
		int targetRowIndex = nattable.getRowIndexByPosition(selectedRowPosition);
		int targetColumnIndex = indexOfSourceTextColumn();
		// 点击之后，Position 可能会发生改变，所以必须在点击之前赋以上两个值

		nattable.click(selectedRowPosition, sourceColumnPosition);

		bot.waitUntil(new IsCellSelected(nattable, targetRowIndex, targetColumnIndex));
	}

	/**
	 * 选择指定行的目标文本
	 * @param segNum
	 *            欲选择的文本段行号
	 */
	public void selectTargetCell(int segNum) {
		getNatTable();
		gotoSeg(segNum);

		int targetColumnPosition = positionOfTargetTextColumn();
		int targetRowPosition = nattable.positionOfSelectedRow(targetColumnPosition);
		// 先点击一下行首，保证目标文本框完全显示在界面上
		nattable.click(targetRowPosition, 0);
		// 重新取当前选中行的位置，因为刚才的点击可能引起了改变
		targetRowPosition = nattable.positionOfSelectedRow(targetColumnPosition);
		if (!isHorizontalLayout()) {
			targetRowPosition += 1;
		}

		// 这两个 Index 用来判断指定的单元格在操作完成后是否确实被选中
		int targetColumnIndex = indexOfTargetTextColumn();
		int targetRowIndex = nattable.getRowIndexByPosition(targetRowPosition);
		// 点击之后，Position 可能会发生改变，所以必须在点击之前赋以上两个值

		nattable.click(targetRowPosition, targetColumnPosition);

		bot.waitUntil(new IsCellSelected(nattable, targetRowIndex, targetColumnIndex));
	}

	/**
	 * 进入编辑源文本状态
	 * @param segNum
	 *            欲编辑的文本段行号
	 */
	public void enterEditModeSource(int segNum) {
		getNatTable();
		gotoSeg(segNum); // 先跳转到该文本段，确保其被显示

		int sourceColumnPosition = positionOfSourceTextColumn();
		int selectedRowPosition = nattable.positionOfSelectedRow(sourceColumnPosition);

		// 先点击一下行首，保证目标文本框完全显示在界面上
		nattable.click(selectedRowPosition, 0);
		// 重新取当前选中行的位置，因为刚才的点击可能引起了改变
		selectedRowPosition = nattable.positionOfSelectedRow(sourceColumnPosition);

		String textOfCell = nattable.getTextByPosition(selectedRowPosition, positionOfSourceTextColumn());
		// 该节点没有值时当作空字符串处理
		String textOfCellNotNull = (textOfCell == null ? "" : textOfCell);
		// 点击之后，Position 可能会改变，所以必须在点击之前赋值

		nattable.doubleClick(selectedRowPosition, sourceColumnPosition);

		bot.waitUntil(new IsCellEditMode(this, textOfCellNotNull));
	}

	/**
	 * 进入编辑目标文本状态
	 * @param segNum
	 *            欲编辑的文本段行号
	 */
	public void enterEditModeTarget(int segNum) {
		getNatTable();
		gotoSeg(segNum);

		int targetColumnPosition = positionOfTargetTextColumn();
		int targetRowPosition = nattable.positionOfSelectedRow(targetColumnPosition);

		// 先点击一下行首，保证目标文本框完全显示在界面上
		nattable.click(targetRowPosition, 0);
		// 重新取当前选中行的位置，因为刚才的点击可能引起了改变
		targetRowPosition = nattable.positionOfSelectedRow(targetColumnPosition);

		if (!isHorizontalLayout()) {
			targetRowPosition += 1;
		}

		String textOfCell = nattable.getTextByPosition(targetRowPosition, positionOfTargetTextColumn());
		// 该节点没有值时当作空字符串处理
		final String textOfCellNotNull = (textOfCell == null ? "" : textOfCell);

		// 点击之后，Position 可能会改变，所以必须在点击之前赋值
		nattable.doubleClick(targetRowPosition, targetColumnPosition);

		bot.waitUntil(new IsCellEditMode(this, textOfCellNotNull));
	}

	/**
	 * @return boolean 判断 NatTable 是否按源或目标列排序
	 */
	public boolean isSorted() {
		getNatTable();
		return nattable.sortStatus(positionOfSourceTextColumn()) != 0
				&& nattable.sortStatus(positionOfTargetTextColumn()) != 0;
	}

	/**************
	 * XLIFF 编辑器中的 StyledText 相关操作 ************ 包括将光标放置于源/目标文本中的指定位置、输入或删除源/目标文本 的指定字符/字符串等
	 */

	/**
	 * 返回 NatTable 中的 StyledText 以区分其他地方的相同控件（如记忆库翻译匹配面板、快速翻译面板中）
	 * @return SWTBotStyledText 当前编辑器的 NatTable 中的 StyledText
	 */
	public HsSWTBotStyledText getStyledText() {
		getNatTable();
		return bot.styledText(nattable);
	}

	/**
	 * 将光标定位到文本框首 在样式文本框中移动光标到开始位置
	 */
	public void navigateToBegining() {
		HsSWTBotStyledText styledText = getStyledText();
		styledText.navigateTo(0, 0);
	}

	/**
	 * 将光标定位到文本框末 在样式文本框中移动光标到结束位置
	 */
	public void navigateToEnd() {
		HsSWTBotStyledText styledText = getStyledText();
		int length = styledText.getText().length();
		styledText.navigateTo(styledText.getPositionByIndex(length));
	}

	/**
	 * 将光标定位到指定索引位置
	 * @param index
	 *            目标位置的索引
	 */
	public void navigateToIndex(int index) {
		HsSWTBotStyledText styledText = getStyledText();
		styledText.navigateTo(styledText.getPositionByIndex(index));
	}

	/**
	 * 将光标定位到指定字符串之前
	 * @param beforeText
	 *            指定的字符串，光标将置于其第一个字符之前
	 */
	public void navigateBefore(String beforeText) {
		HsSWTBotStyledText styledText = getStyledText();
		Position targetPos = styledText.positionOf(beforeText);
		Assert.isTrue(!targetPos.equals(new Position(-1, -1)), "Text \"" + beforeText + "\" not found.");
		styledText.navigateTo(targetPos);
	}

	/**
	 * 将光标定位到指定字符串之后
	 * @param afterText
	 *            指定的字符串，光标将置于其最后一个字符之后
	 */
	public void navigateAfter(String afterText) {
		HsSWTBotStyledText styledText = getStyledText();
		Position targetPos = styledText.positionOfFollowing(afterText);
		Assert.isTrue(!targetPos.equals(new Position(-1, -1)), "Text \"" + afterText + "\" not found.");
		styledText.navigateTo(targetPos);
	}

	/**
	 * 将光标定位到指定的两个字符串之间 指定的两个字符串必须相邻
	 * @param afterText
	 *            指定的字符串，光标将置于其最后一个字符之后
	 * @param beforeText
	 *            指定的字符串，光标将置于其第一个字符之前
	 */
	public void navigateBetween(String afterText, String beforeText) {
		HsSWTBotStyledText styledText = getStyledText();
		Position targetPosA = styledText.positionOfFollowing(afterText);
		Assert.isTrue(!targetPosA.equals(new Position(-1, -1)), "Text \"" + afterText + "\" not found.");
		Position targetPosB = styledText.positionOf(beforeText);
		Assert.isTrue(!targetPosB.equals(new Position(-1, -1)), "Text \"" + beforeText + "\" not found.");
		Assert.isTrue(targetPosB.equals(targetPosA), "Text \"" + afterText + beforeText + "\" not found.");
		styledText.navigateTo(targetPosA);
	}

	/**
	 * 在文本框开始位置输入内容
	 * @param text
	 *            要输入的内容
	 */
	public void typeTextBegining(String text) {
		navigateToBegining();
		HsSWTBotStyledText styledText = getStyledText();
		styledText.typeText(text);
	}

	/**
	 * 在文本框结束位置输入内容
	 * @param text
	 *            要输入的内容
	 */
	public void typeTextEnd(String text) {
		navigateToEnd();
		HsSWTBotStyledText styledText = getStyledText();
		styledText.typeText(text);
	}

	/**
	 * 在指定字符串之前输入内容
	 * @param beforeText
	 *            指定的字符串，输入的内容将在其第一个字符之前
	 * @param text
	 *            要输入的内容
	 */
	public void typeTextBefore(String beforeText, String text) {
		HsSWTBotStyledText styledText = getStyledText();
		Position targetPos = styledText.positionOf(beforeText);
		Assert.isTrue(!targetPos.equals(new Position(-1, -1)), "Text \"" + beforeText + "\" not found.");
		styledText.typeText(targetPos.line, targetPos.column, text);
	}

	/**
	 * 在指定字符串之后输入内容
	 * @param afterText
	 *            指定的字符串，输入的内容将在其最后一个字符之后
	 * @param text
	 *            要输入的内容
	 */
	public void typeTextAfter(String afterText, String text) {
		HsSWTBotStyledText styledText = getStyledText();
		Position targetPos = styledText.positionOfFollowing(afterText);
		Assert.isTrue(!targetPos.equals(new Position(-1, -1)), "Text \"" + afterText + "\" not found.");
		styledText.typeText(targetPos.line, targetPos.column, text);
	}

	/**
	 * 在指定的两个字符串之间输入内容
	 * @param afterText
	 *            指定的字符串，输入的内容将在其最后一个字符之后
	 * @param beforeText
	 *            指定的字符串，输入的内容将在其第一个字符之前
	 * @param text
	 *            要输入的内容
	 */
	public void typeTextBetween(String afterText, String beforeText, String text) {
		HsSWTBotStyledText styledText = getStyledText();
		Position targetPosA = styledText.positionOfFollowing(afterText);
		Assert.isTrue(!targetPosA.equals(new Position(-1, -1)), "Text \"" + afterText + "\" not found.");
		Position targetPosB = styledText.positionOf(beforeText);
		Assert.isTrue(!targetPosB.equals(new Position(-1, -1)), "Text \"" + beforeText + "\" not found.");
		Assert.isTrue(targetPosB.equals(targetPosA), "Text \"" + afterText + beforeText + "\" not found.");
		styledText.typeText(targetPosA.line, targetPosA.column, text);
	}

	/**
	 * 删除第一个匹配的内容
	 * @param text
	 *            要删除的内容
	 */
	public void deleteFirstText(String text) {
		HsSWTBotStyledText styledText = getStyledText();
		styledText.setText(styledText.getText().replaceFirst(text, ""));
	}

	/**
	 * 删除所有匹配的内容
	 * @param text
	 *            要删除的内容
	 */
	public void deleteAllText(String text) {
		HsSWTBotStyledText styledText = getStyledText();
		styledText.setText(styledText.getText().replaceAll(text, ""));
	}

	/************** 编辑器相关的状态栏信息读取 ************ */

	/**
	 * @return String 状态栏中的当前编辑器所打开的文件路径
	 */
	public String stbCurrentFile() {
		return ts.getStatusBarValueByKey(ts.stbiCurrentFile());
	}

	/**
	 * @return int 状态栏中的当前文本段序号
	 */
	public int stbSegmentNumber() {
		return Integer.valueOf(ts.getStatusBarValueByKey(ts.stbiSegmentNumber()));
	}

	/**
	 * @return int 状态栏中的可见文本段数量
	 */
	public int stbVisibleSegmentCount() {
		return Integer.valueOf(ts.getStatusBarValueByKey(ts.stbiVisibleSegmentCount()));
	}

	/**
	 * @return int 状态栏中的文本段总数
	 */
	public int stbSegmentTotalCount() {
		return Integer.valueOf(ts.getStatusBarValueByKey(ts.stbiSegmentTotalCount()));
	}

	/**
	 * @return String 状态栏中的当前用户名
	 */
	public String stbUsername() {
		return ts.getStatusBarValueByKey(ts.stbiUsername());
	}

	/************** 界面文本 ************ */

	/**
	 * @return 行号;
	 */
	public String lblNatTableHeaderLineNum() {
		return TsUIConstants.getString("lblNatTableHeaderLineNum");
	}

	/**
	 * @return 状态;
	 */
	public String lblNatTableHeaderStatus() {
		return TsUIConstants.getString("lblNatTableHeaderStatus");
	}

	/**
	 * @return 设置文本段批准状态;
	 */
	public String ctxMenuApproveStatus() {
		return TsUIConstants.getString("ctxMenuApproveStatus");
	}

	/**
	 * @return 批准当前文本段;
	 */
	public String ctxMenuapproveStatusApproveCurrentSeg() {
		return TsUIConstants.getString("approveStatusApproveCurrentSeg");
	}

	/**
	 * @return 取消批准当前文本段;
	 */
	public String ctxMenuapproveStatusUnapproveCurrentSeg() {
		return TsUIConstants.getString("approveStatusUnapproveCurrentSeg");
	}

	/**
	 * @return 设置文本段锁定状态;
	 */
	public String ctxMenuLockStatus() {
		return TsUIConstants.getString("ctxMenuLockStatus");
	}

	/**
	 * @return 锁定当前文本段;
	 */
	public String ctxMenulockStatusLockCurrentSeg() {
		return TsUIConstants.getString("lockStatusLockCurrentSeg");
	}

	/**
	 * @return 取消锁定当前文本段;
	 */
	public String ctxMenulockStatusUnlockCurrentSeg() {
		return TsUIConstants.getString("lockStatusUnlockCurrentSeg");
	}

	/**
	 * @return 锁定重复文本段;
	 */
	public String ctxMenulockStatusLockRepetitionSegs() {
		return TsUIConstants.getString("lockStatusLockRepetitionSegs");
	}

	/**
	 * @return 取消锁定重复文本段;
	 */
	public String ctxMenulockStatusUnlockRepetitionSegs() {
		return TsUIConstants.getString("lockStatusUnlockRepetitionSegs");
	}

	/**
	 * @return 设置目标文本段状态;
	 */
	public String ctxMenuTargetState() {
		return TsUIConstants.getString("ctxMenuTargetState");
	}

//	public String ctxMenuTargetStatenew() {
//		return TsUIConstants.getString("TargetStateNew");
//	}
//
//	public String ctxMenuTargetStatefinal() {
//		return TsUIConstants.getString("TargetStatefinal");
//	}
//
//	public String ctxMenuTargetStatetranslated() {
//		return TsUIConstants.getString("TargetStatetranslated");
//	}
//
//	public String ctxMenuTargetStatesigned_off() {
//		return TsUIConstants.getString("TargetStatesigned-off");
//	}
//
//	public String ctxMenuTargetStateneeds_adaptation() {
//		return TsUIConstants.getString("TargetStateneeds-adaptation");
//	}
//
//	public String ctxMenuTargetStateneeds_reivew_adaptation() {
//		return TsUIConstants.getString("TargetStateneeds-reivew-adaptation");
//	}
//
//	public String ctxMenuTargetStateneeds_l10n() {
//		return TsUIConstants.getString("TargetStateneeds-l10n");
//	}
//
//	public String ctxMenuTargetStateneeds_review_l10n() {
//		return TsUIConstants.getString("TargetStateneeds-review-l10n");
//	}
//
//	public String ctxMenuTargetStateneeds_translation() {
//		return TsUIConstants.getString("TargetStateneeds-translation");
//	}
//
//	public String ctxMenuTargetStateneeds_review_translation() {
//		return TsUIConstants.getString("TargetStateneeds-review-translation");
//	}

	/************** 一些常用的任务 ************ */

	/**
	 * @param expectedText 字符串数组：分割后的文本段内容
	 * @return XliffUtil 数组：分割后的新 XliffUtil 对象;
	 */
	public XliffUtil[] getSplitXliffUtil(String[] expectedText) {
		XliffUtil[] xu = new XliffUtil[2];
		int segNum1 = segNumOfSource(expectedText[0]);
		String rowID1 = rowIdOfSegNum(segNum1);
		xu[0] = new XliffUtil(rowID1);

		int segNum2 = segNumOfSource(expectedText[1]);
		String rowID2 = rowIdOfSegNum(segNum2);
		xu[1] = new XliffUtil(rowID2);

		return xu;
	}

}

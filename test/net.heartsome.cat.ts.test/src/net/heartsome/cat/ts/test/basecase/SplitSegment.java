package net.heartsome.cat.ts.test.basecase;

import static org.junit.Assert.assertTrue;
import net.heartsome.cat.ts.test.ui.constants.TsUIConstants;
import net.heartsome.cat.ts.test.ui.dialogs.TS;
import net.heartsome.cat.ts.test.ui.editors.XlfEditor;
import net.heartsome.cat.ts.test.ui.msgdialogs.InformationDialog;
import net.heartsome.cat.ts.test.ui.tasks.SegmentAsserts;
import net.heartsome.cat.ts.test.ui.utils.XliffUtil;
import net.heartsome.test.swtbot.utils.HSBot;
import net.heartsome.test.swtbot.widgets.SWTBotNatTable;
import net.heartsome.test.swtbot.widgets.HsSWTBotStyledText;
import net.heartsome.test.utilities.common.StringUtil;

import org.eclipse.swtbot.swt.finder.utils.Position;

/**
 * 分割文本段的常用方法
 * @author felix_lu
 *
 */
public class SplitSegment {

	static TS ts = TS.getInstance();
	
	
	/**
	 * 在指定文本段的 index 处分割文本段
	 * @param xe
	 * @param segNum
	 * @param splitIndex
	 */
	public static void splitAt(XlfEditor xe, int segNum, int splitIndex) {
		// 判断该文本段是否可编辑
		String rowID = xe.rowIdOfSegNum(segNum);
		XliffUtil xu = new XliffUtil(rowID);
		SegmentAsserts.segIsEditable(xu);
		
		// 进入编辑模式，方便读取样式文本框中的内容
		xe.enterEditModeSource(segNum);
		
		// 在实际进行分割文本段之前，得到原文本段的 tuid 和分割后两个文本段的预期内容，
		// 用以在分割后验证得到的文本段内容是否符合预期。
		String tuid = xe.tuidOfSegNum(segNum);
		
		// 判断给定的分割点是否可分割，然后在两个分支中分别验证结果
		HsSWTBotStyledText st = xe.getStyledText();
		
		if (SegmentAsserts.indexIsSplitable(st, splitIndex)) {
			String[] expectedText = st.splitTextAt(splitIndex);
			
			// 将光标移到指定位置，并点击相应的菜单项进行分割
			xe.navigateToIndex(splitIndex);
			ts.menuTranslationSplitSegment().click();
			xe.getNatTable();
			
			// 确认文本段已按预期分割
			SegmentAsserts.segIsSplit(tuid, expectedText, xe.getSplitXliffUtil(expectedText));
			System.out.println("[Passed] Segment No.\"" + segNum + "\" was successfully split at Index \"" + splitIndex + "\".");
		}
		else {
			String expectedText = st.getText();
			String position;
			
			// 将光标移到指定位置，并点击相应的菜单项进行分割
			if (splitIndex <= 0) {
				position = "Beginning.";
				xe.navigateToBegining();
			}
			else {
				position = "End.";
				xe.navigateToEnd();
			}
			ts.menuTranslationSplitSegment().click();
			
			// 弹出提示信息
			InformationDialog dialog = new InformationDialog(1, 
					TsUIConstants.getString("msgPlaceCursorToSplit"));
			dialog.lblMessage().isVisible();
			dialog.btnOK().click();
			
			xe.getNatTable();
			
			// 确认文本段没有被分割
			SegmentAsserts.segNotSplit(tuid, expectedText, xu);
			System.out.println("[Passed] Segment No.\"" + segNum + "\" was not split at the " + position);
		}
	}

	/**
	 * @param xe 要操作的 XlfEditor 对象
	 * @param segNum 要分割的文本段序号
	 * @param afterText 在此文本之后分割
	 */
	public static void splitAfter(XlfEditor xe, int segNum, String afterText) {
		SWTBotNatTable nt = xe.getNatTable();
		xe.selectSourceCell(segNum);
		Position pos = nt.positionOfSelectedCell();
		String expectedText = nt.getTextByPosition(pos.line, pos.column);
		int splitIndex = StringUtil.indexAfterWithAssert(expectedText, afterText);
		splitAt(xe, segNum, splitIndex);
	}
	
	/**
	 * @param xe 要操作的 XlfEditor 对象
	 * @param segSourceText 要分割的文本段源文本内容
	 * @param afterText 在此文本之后分割
	 */
	public static void splitAfter(XlfEditor xe, String segSourceText, String afterText) {
		int segNum = xe.segNumOfSource(segSourceText);
		splitAfter(xe, segNum, afterText);
	}
	
	/**
	 * @param xe 要操作的 XlfEditor 对象
	 * @param segNum 要分割的文本段序号
	 * @param beforeText 在此文本之前分割
	 */
	public static void splitBefore(XlfEditor xe, int segNum, String beforeText) {
		SWTBotNatTable nt = xe.getNatTable();
		xe.selectSourceCell(segNum);
		Position pos = nt.positionOfSelectedCell();
		String expectedText = nt.getTextByPosition(pos.line, pos.column);
		int splitIndex = StringUtil.indexBeforeWithAssert(expectedText, beforeText);
		splitAt(xe, segNum, splitIndex);
	}
	
	/**
	 * @param xe 要操作的 XlfEditor 对象
	 * @param segSourceText 要分割的文本段源文本内容
	 * @param beforeText 在此文本之前分割
	 */
	public static void splitBefore(XlfEditor xe, String segSourceText, String beforeText) {
		int segNum = xe.segNumOfSource(segSourceText);
		splitBefore(xe, segNum, beforeText);
	}
	
	/**
	 * @param xe 要操作的 XlfEditor 对象
	 * @param segNum 要分割的文本段序号
	 * @param afterText 在此文本之后分割，此内容在文本段中必须与下一个参数相邻
	 * @param beforeText 在此文本之前分割，此内容在文本段中必须与上一个参数相邻
	 */
	public static void splitBetween(XlfEditor xe, int segNum, String afterText, String beforeText) {
		SWTBotNatTable nt = xe.getNatTable();
		xe.selectSourceCell(segNum);
		Position pos = nt.positionOfSelectedCell();
		String expectedText = nt.getTextByPosition(pos.line, pos.column);
		int splitIndex = StringUtil.indexBetweenWithAssert(expectedText, afterText, beforeText);
		splitAt(xe, segNum, splitIndex);
	}
	
	/**
	 * @param xe 要操作的 XlfEditor 对象
	 * @param segSourceText 要分割的文本段源文本内容
	 * @param afterText 在此文本之后分割，此内容在文本段中必须与下一个参数相邻
	 * @param beforeText 在此文本之前分割，此内容在文本段中必须与上一个参数相邻
	 */
	public static void splitBetween(XlfEditor xe, String segSourceText, String afterText, String beforeText) {
		int segNum = xe.segNumOfSource(segSourceText);
		splitBetween(xe, segNum, afterText, beforeText);
	}
	
	/**
	 * 尝试从段首分割文本段
	 * @param xe 要操作的 XlfEditor 对象
	 * @param segNum 尝试分割的文本段序号
	 */
	public static void splitAtBeginning(XlfEditor xe, int segNum) {
		splitAt(xe, segNum, 0);
	}
	
	/**
	 * 尝试从段末分割文本段
	 * @param xe 要操作的 XlfEditor 对象
	 * @param segNum 尝试分割的文本段序号
	 */
	public static void splitAtEnd(XlfEditor xe, int segNum) {
		SWTBotNatTable nt = xe.getNatTable();
		xe.selectSourceCell(segNum);
		Position pos = nt.positionOfSelectedCell();
		String expectedText = nt.getTextByPosition(pos.line, pos.column);
		int splitIndex = expectedText.length();
		splitAt(xe, segNum, splitIndex);
	}
	
	/**
	 * 尝试不将光标置于源文本中直接分割
	 * @param xe 要操作的 XlfEditor 对象
	 * @param segNum 尝试分割的文本段序号
	 */
	public static void splitWithoutCursor(XlfEditor xe, int segNum) {
		
		// 判断该文本段是否可编辑
		String rowID = xe.rowIdOfSegNum(segNum);
		XliffUtil xu = new XliffUtil(rowID);
		SegmentAsserts.segIsEditable(xu);
		
		// 选中文本段的源文本单元格，而不进入编辑状态
		xe.selectSourceCell(segNum);
		
		// 在实际进行分割文本段之前，得到原文本段的 tuid，用以验证得到的文本段内容是否符合预期。
		String tuid = xe.tuidOfSegNum(segNum);
		
		// 判断给定的分割点是否可分割
		SWTBotNatTable nt = xe.getNatTable();
		Position pos = nt.positionOfSelectedCell();
		String expectedText = nt.getTextByPosition(pos.line, pos.column);

		// 点击相应的菜单项进行分割
		ts.menuTranslationSplitSegment().click();
		
		// 弹出提示信息
		InformationDialog dialog = new InformationDialog(1, 
				TsUIConstants.getString("msgPlaceCursorToSplit"));
		dialog.lblMessage().isVisible();
		dialog.btnOK().click();
		
		xe.getNatTable();
		
		// 确认文本段没有被分割
		SegmentAsserts.segNotSplit(tuid, expectedText, xu);
	}
	
	/**
	 * 尝试分割已批准的文本段
	 * @param xe 要操作的 XlfEditor 对象
	 * @param segNum 尝试分割的文本段序号
	 */
	public static void splitApprovedSeg(XlfEditor xe, int segNum) {

		// 判断该文本段是否已批准
		String rowID = xe.rowIdOfSegNum(segNum);
		XliffUtil xu = new XliffUtil(rowID);
		assertTrue(xu.tuIsApproved());
		
		// 进入编辑模式，方便读取样式文本框中的内容
		xe.enterEditModeSource(segNum);
		
		// 在实际进行分割文本段之前，得到原文本段的 tuid 和分割后两个文本段的预期内容，
		// 用以在分割后验证得到的文本段内容是否符合预期。
		String tuid = xe.tuidOfSegNum(segNum);
		
		// 判断给定的分割点是否可分割
		HsSWTBotStyledText st = xe.getStyledText();
		String expectedText = st.getText();

		// 将光标移到任意位置（这里取 index=1），并点击相应的菜单项进行分割
		xe.navigateToIndex(1);
		ts.menuTranslationSplitSegment().click();
		
		// 弹出提示信息
		InformationDialog dialog = new InformationDialog(1, 
				TsUIConstants.getString("msgCannotMergeApprovedSeg"));
		dialog.lblMessage().isVisible();
		dialog.btnOK().click();
		
		xe.getNatTable();
		
		// 确认文本段没有被分割
		SegmentAsserts.segNotSplit(tuid, expectedText, xu);
	}
	
	/**
	 * 尝试分割已锁定的文本段
	 * @param xe 要操作的 XlfEditor 对象
	 * @param segNum 尝试分割的文本段序号
	 */
	public static void splitUntranslatableSeg(XlfEditor xe, int segNum) {
		// TODO 根据 R8 的实现，可考虑与上一个方法合并，否则还需要单独考虑“已批准且已锁定”文本段的情况
	}

	/**
	 * 未打开文件时，分割文本段功能被禁用
	 */
	public static void splitWithoutFile() {
		assertTrue(HSBot.bot().editors().isEmpty());
		assertTrue(!ts.menuTranslationSplitSegment().isEnabled());
	}
}

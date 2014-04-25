package net.heartsome.cat.ts.test.basecase;

import static org.junit.Assert.assertTrue;
import net.heartsome.cat.ts.test.ui.constants.TsUIConstants;
import net.heartsome.cat.ts.test.ui.dialogs.TS;
import net.heartsome.cat.ts.test.ui.editors.XlfEditor;
import net.heartsome.cat.ts.test.ui.msgdialogs.InformationDialog;
import net.heartsome.cat.ts.test.ui.tasks.SegmentAsserts;
import net.heartsome.cat.ts.test.ui.utils.XliffUtil;

/**
 * 合并文本段的常用方法
 * @author felix_lu
 *
 */
public class MergeSegments {

	static TS ts = TS.getInstance();
	
	/**
	 * 合并两个文本段，仅适合能取到要合并的两个文本段序号的情况
	 * @param xe 要操作的 XliffEditor 对象
	 * @param segNum 由要合并的两个文本段序号组成的 int 数组
	 * @param direction 合并方向：N 表示 Next，即与下一文本段合并；P 表示 Previous，即与上一文本段合并
	 */
	public static void MergeSegs(XlfEditor xe, int[] segNum, String direction) {
		
		// 先判断传入的方向参数是否正确
		assertTrue("Wrong value for parameter \"direction\".", "N".equals(direction) || "P".equals(direction));
		
		String[] rowID = new String[2];
		XliffUtil[] xu = new XliffUtil[2];
		String[] tuid = new String[2];
		String[] srcText = new String[2];
		
		for (int i = 0; i < 2; i++) {
			rowID[i] = xe.rowIdOfSegNum(segNum[i]);
			xu[i] = new XliffUtil(rowID[i]);
			tuid[i] = xu[i].getTUID();
			srcText[i] = xu[i].getSourceText();
		}
		
		// 判断两个文本段符合合并文本段的条件
		if (SegmentAsserts.segsAreMergeable(xe, xu[0], xu[1])) {
			
			// 预先得到合并后的源文本内容
			String tag = "<ph id=\"hs-merge" + tuid[0] + "~" + tuid[1] + "\"/>";
			String expectedText = srcText[0] + tag + srcText[1];
			
			// 跳到指定文本段，执行合并操作
			// 方向为与下一文本段合并
			if (direction.equals("N")) {
				xe.gotoSeg(segNum[0]);
//				ts.menuTranslationMergeWithNextSegment().click(); // FIXME
			}
			
			// 方向为与上一文本段合并
			else {
				xe.gotoSeg(segNum[1]);
//				ts.menuTranslationMergeWithPreivousSegment().click(); // FIXME
			}
			
			// 更新 NatTable
			xe.getNatTable();
			
			// 更新 XliffUtil
			for (int i = 0; i < 2; i++) {
				xu[i] = new XliffUtil(rowID[i]);
			}
			
			// 断言指定的两个文本段已经成功合并
			SegmentAsserts.segsAreMerged(xu, tuid, expectedText);
		}
		
		// 不符合合并条件
		else {
			
			// 跳到指定文本段，尝试执行合并操作
			TryMergeAndVerifyMsg(xe, xu[0], xu[1], segNum[0], segNum[1], direction);

			// 更新 NatTable
			xe.getNatTable();
			
			// 更新 XliffUtil
			for (int i = 0; i < 2; i++) {
				xu[i] = new XliffUtil(rowID[i]);
			}
			
			// 断言指定的两个文本段没有被合并
			SegmentAsserts.segsNotMerged(xu, tuid, srcText);
		}
	}
	
	/**
	 * 指定两个文本段，由第一个文本段与第二个文本段合并
	 * @param xe 要操作的 XlfEditor 对象
	 * @param segNum 由要合并的两个文本段序号组成的 int 数组
	 */
	public static void MergeWithNextSeg(XlfEditor xe, int[] segNum) {
		MergeSegs(xe, segNum, "N");
	}
	
	/**
	 * 指定两个文本段，由第二个文本段与第一个文本段合并
	 * @param xe 要操作的 XlfEditor 对象
	 * @param segNum 由要合并的两个文本段序号组成的 int 数组
	 */
	public static void MergeWithPrevSeg(XlfEditor xe, int[] segNum) {
		MergeSegs(xe, segNum, "P");
	}
	
	/**
	 * 尝试将最后一个文本段与下一个文本段合并
	 * @param xe XlfEditor 编辑器
	 * @param segNum 文本段序号
	 */
	public static void MergeWithNoNext(XlfEditor xe, int segNum) {
		MergeWithNoPartner(xe, segNum, "N");
	}
	
	/**
	 * 尝试将第一个文本段与上一个文本段合并
	 * @param xe XlfEditor 编辑器
	 * @param segNum 文本段序号
	 */
	public static void MergeWithNoPrev(XlfEditor xe, int segNum) {
		MergeWithNoPartner(xe, segNum, "P");
	}
	
	/**
	 * 尝试将文本段与不存在的上/下一个文本段合并
	 * @param xe 编辑器
	 * @param segNum 文本段序号
	 * @param direction 合并方向，只能为 N 或 P
	 */
	private static void MergeWithNoPartner(XlfEditor xe, int segNum, String direction) {
		
		assertTrue("Wrong value for parameter \"direction\".", "N".equals(direction) || "P".equals(direction));
		
		String rowID = xe.rowIdOfSegNum(segNum);
		XliffUtil xu = new XliffUtil(rowID);
		String tuid = xu.getTUID();
		String srcText = xu.getSourceText();
		
		if ("N".equals(direction)) {
			TryMergeAndVerifyMsg(xe, xu, null, segNum, 0, "N");
		}
		else {
			TryMergeAndVerifyMsg(xe, null, xu, 0, segNum, "P");
		}
		
		// 更新 NatTable
		xe.getNatTable();
		
		// 更新 XliffUtil
		xu = new XliffUtil(rowID);
		
		// 断言文本段没有被改动
		SegmentAsserts.segNoChange(xu, tuid, srcText);
	}
	
	/**
	 * 尝试合并不可合并的文本段，并验证出现的信息对话框
	 * @param xe 编辑器
	 * @param xu1 第一个（即欲与下一个文本段合并的）文本段 XliffUtil 对象
	 * @param xu2 第二个（即欲与上一个文本段合并的）文本段 XliffUtil 对象
	 * @param segNum1 第一个文本段序号
	 * @param segNum2 第二个文本段序号
	 * @param direction 合并方向，只允许为 N 或 P
	 */
	private static void TryMergeAndVerifyMsg(XlfEditor xe, XliffUtil xu1, XliffUtil xu2, 
			int segNum1, int segNum2, String direction) {
		
		// 先判断传入的方向参数是否正确
		assertTrue("Wrong value for parameter \"direction\".", "N".equals(direction) || "P".equals(direction));
		
		// 方向为与下一文本段合并
		if ("N".equals(direction)) {
			xe.gotoSeg(segNum1);
//			ts.menuTranslationMergeWithNextSegment().click(); // FIXME

			// 若未找到下一个非空文本段，则验证弹出的信息对话框
			if (xu1.getNextNotNullXU() == null) {
				InformationDialog cnms = new InformationDialog(1, 
						TsUIConstants.getString("msgCannotMergeSegWhenNoNextSeg"));
				cnms.lblMessage().isVisible();
				cnms.btnOK().click();
			}
		}
		
		// 方向为与上一文本段合并
		else {
			xe.gotoSeg(segNum2);
//			ts.menuTranslationMergeWithPreivousSegment().click(); // FIXME
			
			// 若未找到上一个非空文本段，则验证弹出的信息对话框
			if (xu2.getPrevNotNullXU() == null) {
				InformationDialog cnms = new InformationDialog(1, 
						TsUIConstants.getString("msgCannotMergeSegWhenNoPrevSeg"));
				cnms.lblMessage().isVisible();
				cnms.btnOK().click();
			}
		}
		
		// 若两个文本段中任一个为已批准，则验证弹出的信息对话框
		if (xu1.tuIsApproved() || xu2.tuIsApproved()) {
			InformationDialog cnmas = new InformationDialog(1, 
					TsUIConstants.getString("msgCannotMergeApprovedSeg"));
			cnmas.lblMessage().isVisible();
			cnmas.btnOK().click();
		}
		
		// TODO 根据具体的实现决定是否需要增加更多信息对话框相关判断，比如尝试合并锁定文本段时
	}
	
	
}

package net.heartsome.cat.ts.test.ui.tasks;

import static org.junit.Assert.assertTrue;
import net.heartsome.cat.ts.test.ui.editors.XlfEditor;
import net.heartsome.cat.ts.test.ui.utils.XliffUtil;
import net.heartsome.test.swtbot.widgets.HsSWTBotStyledText;

/**
 * TS 的一些断言方法，其中有些是直接断言，有些则是返回布尔值
 * @author felix_lu
 */
public final class SegmentAsserts {

	/**
	 * 
	 */
	private SegmentAsserts() {
	}

	/**
	 * 断言文本段可编辑
	 * @param xu
	 *            文本段对应的 XliffUtil 对象
	 */
	public static void segIsEditable(XliffUtil xu) {
		assertTrue("The segment is not editable.", xu.tuIsEditable());
	}

	/**
	 * 判断指定的文本段可以在给出的 index 处分割（即不在文本段首、末位置）
	 * @param st
	 *            文本段内容对应的 StyledText 对象
	 * @param index
	 *            分割点索引
	 * @return boolean True 表示可以在该处分割
	 */
	public static boolean indexIsSplitable(HsSWTBotStyledText st, int index) {
		int length = st.getText().length(); // TODO 增加分割点在内部标记上的判断
		return index > 0 && index < length;
	}

	/**
	 * 判断给出的两个文本段是否可以合并
	 * @param xe
	 *            文本段所在的 XlfEditor 对象
	 * @param xu1
	 *            第一个文本段对应的 XliffUtil 对象
	 * @param xu2
	 *            第二个文本段对应的 XliffUtil 对象
	 * @return boolean
	 */
	public static boolean segsAreMergeable(XlfEditor xe, XliffUtil xu1, XliffUtil xu2) {

		if (!xe.isSorted()) { // 编辑器未按源或目标文本进行排序，因为排序后显示的顺序与文件中的物理顺序极有可能不同
			if (xu1.tuIsEditable() && xu2.tuIsEditable()) { // 两个文本段均未批准、未锁定
				if (xu1.getXlfFile().equals(xu2.getXlfFile())) { // 两个文本段来自同一个 XLIFF 文件
					if (xu1.getOriginalFile().equals(xu2.getOriginalFile())) { // 两个文本段在 XLIFF 文件中属于同一个 <file> 节点
						if (xu1.getRowID().equals(xu2.getPrevNotNullXU().getRowID())
								&& xu2.getRowID().equals(xu1.getNextNotNullXU().getRowID())) { // 是否为两个连续的非空文本段
							return true;
						}
					}
				}
			}
		}

		return false;
	}

	/**
	 * 断言文本段已被成功分割
	 * @param tuid
	 *            分割前的文本段 trans-unit id
	 * @param expectedText
	 *            由分割前的文本段源文本内容在指定位置分割后得到的两个字符串所组成的数组
	 * @param xu
	 *            分割后的两个文本段对应的 XLIFFUtil 对象所组成的数组
	 */
	public static void segIsSplit(String tuid, String[] expectedText, XliffUtil[] xu) {

		// 分割后的两个新文本段在同一个以分割前文本段的 tuid 为 id 的 group 中
		String groupId = xu[0].getAttributeOfGroup("id");
		assertTrue(groupId.equals(xu[1].getAttributeOfGroup("id")));
		assertTrue(tuid.equals(groupId));
		assertTrue("hs-split".equals(xu[0].getAttributeOfGroup("ts")));

		// 两个新文本段的 tuid
		assertTrue((tuid + "-1").equals(xu[0].getAttributeOfTU("id")));
		assertTrue((tuid + "-2").equals(xu[1].getAttributeOfTU("id")));

		// 源文本内容：可能是标记源代码状态，也可能是简单标记状态
		assertTrue(expectedText[0].equals(xu[0].getSourceText()) || expectedText[0].equals(xu[0].getSourceTextTagged()));
		assertTrue(expectedText[1].equals(xu[1].getSourceText()) || expectedText[1].equals(xu[1].getSourceTextTagged()));

		// 目标文本段状态：目标文本为空时 new、非空时 translated
		targetStatus(xu[0]);
		targetStatus(xu[1]);
	}

	/**
	 * 断言文本段没有被分割
	 * @param tuid
	 *            尝试分割前的文本段 trans-unit id
	 * @param expectedText
	 *            尝试分割前的文本段源文本内容
	 * @param xu
	 *            分割后的文本段对应的 XLIFFUtil 对象
	 */
	public static void segNotSplit(String tuid, String expectedText, XliffUtil xu) {
		assertTrue("Parameters should not be null.", tuid != null && expectedText != null && xu != null);

		String newTUID = xu.getAttributeOfTU("id");
		assertTrue("TUID: " + tuid + " is not the same as the new one.", tuid.equals(newTUID));

		String text = xu.getSourceText();
		assertTrue("Source text: " + expectedText + " is not the same as the new one.", expectedText.equals(text)
				|| expectedText.equals(xu.getSourceTextTagged()));
	}

	/**
	 * 断言指定的两个文本段已经成功合并
	 * @param xu
	 *            两个要合并的文本段对应的 XliffUtil 对象
	 * @param tuid
	 *            两个文本段在合并之前的 trans-unit id 值
	 * @param expectedText
	 *            两个文本段合并后的源文本预期内容
	 */
	public static void segsAreMerged(XliffUtil[] xu, String[] tuid, String expectedText) {

		// 合并前的两个 trans-unit 仍然存在且 id 不变
		// 此处不直接用 getTUID() 方法的原因是该方法不是实时地从 XLIFF 文件中读数据，
		// 而是直接返回通过 rowID 解析得到的成员变量值。
		assertTrue(tuid[0].equals(xu[0].getAttributeOfTU("id")));
		// FIXME assertTrue(tuid[1].equals(xu[1].getAttributeOfTU("id")));

		// 源文本内容
		assertTrue(expectedText.equals(xu[0].getSourceText()));
		assertTrue(xu[1].getSourceText() == null || "".equals(xu[1].getSourceText()));

		// trans-unit 节点的批准和锁定属性
		assertTrue(xu[0].tuIsEditable());
		// FIXME assertTrue(xu[1].tuIsApproved() && !xu[1].tuIsTranslatable());

		// 目标文本段状态
		// FIXME targetStatus(xu[0]);
		// FIXME targetStatus(xu[1]);
	}

	/**
	 * 断言指定的两个文本段没有被合并
	 * @param xu
	 *            两个要合并的文本段对应的 XliffUtil 对象
	 * @param tuid
	 *            两个文本段在合并之前的 trans-unit id 值
	 * @param sourceText
	 *            两个文本段在合并之前的源文本内容
	 */
	public static void segsNotMerged(XliffUtil[] xu, String[] tuid, String[] sourceText) {

		// 尝试合并前的两个 trans-unit 不变
		// 此处不直接用 getTUID() 方法的原因是该方法不是实时地从 XLIFF 文件中读数据，
		// 而是直接返回通过 rowID 解析得到的成员变量值。
		assertTrue(tuid[0].equals(xu[0].getAttributeOfTU("id")));
		// assertTrue(tuid[1].equals(xu[1].getAttributeOfTU("id")));

		// 源文本内容
		assertTrue(sourceText[0].equals(xu[0].getSourceText()));
		assertTrue(sourceText[1].equals(xu[1].getSourceText()));
	}

	/**
	 * 断言指定的文本段没有被改动
	 * @param xu
	 *            XliffUtil 对象
	 * @param tuid
	 *            尝试改动之前的文本段 trans-unit id
	 * @param sourceText
	 *            尝试改动之前的源文本内容
	 */
	public static void segNoChange(XliffUtil xu, String tuid, String sourceText) {
		assertTrue(tuid.equals(xu.getAttributeOfTU("id")));
		assertTrue(sourceText.equals(xu.getSourceText()));
	}

	/**
	 * 断言目标文本状态：当存在 target 节点且目标文本非空时为 translated、 目标文本为空时 new，没有 state 属性时为 null
	 * @param xu
	 *            要验证的文本段对应的 XliffUtil 对象
	 */
	public static void targetStatus(XliffUtil xu) {
		String targetText = xu.getTargetText();
		if (targetText != null) {
			String targetStatus = xu.getTargetStatus();
			if ("".equals(targetText)) {
				assertTrue(targetStatus == null || "new".equals(targetStatus));
			} else {
				assertTrue("translated".equals(targetStatus));
			}
		}
	}
}

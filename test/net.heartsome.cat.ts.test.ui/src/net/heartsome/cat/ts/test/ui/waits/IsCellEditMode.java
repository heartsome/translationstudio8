package net.heartsome.cat.ts.test.ui.waits;

import net.heartsome.cat.ts.test.ui.editors.XlfEditor;
import net.heartsome.cat.ts.test.ui.utils.XliffUtil;

import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;

/**
 * 判断编辑器单元格是否为编辑状态。
 */
public class IsCellEditMode extends DefaultCondition {

	private XlfEditor editor;
	private String styledText;
	private String styledTextNotNull;
	private String expectedTextNotNull;
	// 用 InnerTagUtil 将 NatTable 中直接取到的纯文本转为简单标记形式显示的文本
	private String taggedText;

	/**
	 * @param editor
	 *            XlfEditor 对象，自动从其中得到编辑模式中的 StyledText 内容
	 * @param expectedTextNotNull
	 *            预期的文本，用于对比判断从 XlfEditor 中得到的 StyledText 是否与该预期内容一致
	 */
	public IsCellEditMode(XlfEditor editor, String expectedTextNotNull) {
		this.editor = editor;
		this.expectedTextNotNull = expectedTextNotNull;
		taggedText = XliffUtil.tagged(expectedTextNotNull);
	}

	/** (non-Javadoc)
	 * @see org.eclipse.swtbot.swt.finder.waits.ICondition#test()
	 */
	public boolean test() throws Exception {
		styledText = editor.getStyledText().getText();
		// 该样式文本框没有值时当作空字符串处理
		styledTextNotNull = (styledText == null ? "" : styledText);
		// 若编辑器为显示标记源文本状态，则直接对比从 NatTable 取到的纯文本；
		// 若为显示简单标记状态，则对比按 InnerTagUtil 处理后的样式文本。
		// 从界面上无法直接取到标记的显示状态，所以只要这两个对比结果中任一个成立即可。
		return expectedTextNotNull.equals(styledTextNotNull) || taggedText.equals(styledTextNotNull);
	}

	/** (non-Javadoc)
	 * @see org.eclipse.swtbot.swt.finder.waits.ICondition#getFailureMessage()
	 */
	public String getFailureMessage() {
		return "以下预期内容的单元格未进入编辑模式：" + expectedTextNotNull;
	}
}

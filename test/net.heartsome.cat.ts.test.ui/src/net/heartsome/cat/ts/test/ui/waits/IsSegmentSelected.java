package net.heartsome.cat.ts.test.ui.waits;

import net.heartsome.cat.ts.test.ui.editors.XlfEditor;
import net.heartsome.test.swtbot.widgets.SWTBotNatTable;

import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;

/**
 * 判断编辑器的指定文本段是否被选中。
 */
public class IsSegmentSelected extends DefaultCondition {

	private XlfEditor xlfEditor;
	private SWTBotNatTable nattable;
	private int targetRowIndex;
	private int currentRowIndex;

	/**
	 * @param xlfEditor
	 * @param targetRowIndex
	 */
	public IsSegmentSelected(XlfEditor xlfEditor, int targetRowIndex) {
		this.xlfEditor = xlfEditor;
		nattable = xlfEditor.nattable;
		this.targetRowIndex = targetRowIndex;
	}

	/**
	 * (non-Javadoc)
	 * @see org.eclipse.swtbot.swt.finder.waits.ICondition#test()
	 */
	public boolean test() throws Exception {
		currentRowIndex = nattable.indexOfSelectedRow(xlfEditor.positionOfTargetTextColumn());
		return targetRowIndex == currentRowIndex;
	}

	/**
	 * (non-Javadoc)
	 * @see org.eclipse.swtbot.swt.finder.waits.ICondition#getFailureMessage()
	 */
	public String getFailureMessage() {
		return "以下文本段未被选中：" + targetRowIndex;
	}
}

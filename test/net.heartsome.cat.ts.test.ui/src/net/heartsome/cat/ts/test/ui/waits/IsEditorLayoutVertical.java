package net.heartsome.cat.ts.test.ui.waits;

import net.heartsome.cat.ts.test.ui.editors.XlfEditor;

import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;

/**
 * 判断编辑器是否为垂直布局。
 */
public class IsEditorLayoutVertical extends DefaultCondition {

	private XlfEditor xlfEditor;

	/**
	 * 判断编辑器是否为垂直布局
	 * @param xlfEditor
	 */
	public IsEditorLayoutVertical(XlfEditor xlfEditor) {
		this.xlfEditor = xlfEditor;
	}

	/** (non-Javadoc)
	 * @see org.eclipse.swtbot.swt.finder.waits.ICondition#test()
	 */
	public boolean test() throws Exception {
		return !xlfEditor.isHorizontalLayout();
	}

	/** (non-Javadoc)
	 * @see org.eclipse.swtbot.swt.finder.waits.ICondition#getFailureMessage()
	 */
	public String getFailureMessage() {
		return "该编辑器不是垂直布局：" + xlfEditor.getTitle();
	}
}

package net.heartsome.cat.ts.test.ui.waits;

import static org.junit.Assert.assertTrue;
import net.heartsome.cat.ts.test.ui.views.ProjectTreeView;

import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;

/**
 * 判断项目是否出现在项目导航树上。
 */
public class IsProjectExist extends DefaultCondition {

	private String projectName;

	/**
	 * 判断项目是否出现在项目导航树上。
	 * @param projectName
	 *            从项目树中查找匹配的项目名称。
	 */
	public IsProjectExist(String projectName) {
		assertTrue("项目名称不能为空或 null。", (projectName != null && !"".equals(projectName)));
		this.projectName = projectName;
	}

	/**
	 * (non-Javadoc)
	 * @see org.eclipse.swtbot.swt.finder.waits.ICondition#test()
	 */
	public boolean test() throws Exception {
		boolean result = false;
		SWTBotTreeItem[] items = ProjectTreeView.getTree().getAllItems();
		
		for (SWTBotTreeItem item : items) {
			if (projectName.equals(item.getText())) {
				result = true;
				break;
			}
		}
		return result;
	}

	/**
	 * (non-Javadoc)
	 * @see org.eclipse.swtbot.swt.finder.waits.ICondition#getFailureMessage()
	 */
	public String getFailureMessage() {
		return "未在项目导航树上找到项目：" + projectName;
	}
}

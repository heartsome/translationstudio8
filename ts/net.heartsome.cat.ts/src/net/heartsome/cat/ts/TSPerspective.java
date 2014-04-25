/**
 * Perspective.java
 *
 * Version information :
 *
 * Date:Jan 27, 2010
 *
 * Copyright notice :
 */
package net.heartsome.cat.ts;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

/**
 * .
 * @author Jason
 * @version
 * @since JDK1.6
 */
public class TSPerspective implements IPerspectiveFactory {
	
	public final static String ID = "net.heartsome.cat.ts.perspective";

	/**
	 * TS默认透视图。
	 * @see org.eclipse.ui.IPerspectiveFactory#createInitialLayout(org.eclipse.ui.IPageLayout)
	 */
	public void createInitialLayout(IPageLayout layout) {

		// layout.createFolder() 默认显示。
		// layout.createPlaceholderFolder() 默认不显示。

		String editor = layout.getEditorArea();
//		String rightFirst = "RIGHT_TOP";
		String left = "LEFT";
		String right = "RIGHT";
//		String bottom = "RIGHT_BOTTOM";

		IFolderLayout leftFolder = layout.createFolder(left, IPageLayout.LEFT, 0.20F, editor);
//		IFolderLayout topFirstFolder = layout.createFolder(rightFirst, IPageLayout.TOP, 0.3F, editor);
		IFolderLayout rightFolder = layout.createFolder(right, IPageLayout.RIGHT, 0.70F, editor);

		// 显示术语匹配结果视图
//		IFolderLayout bottomFolder = layout
//				.createFolder(bottom, IPageLayout.TOP, 0.65F, editor);
//		IPlaceholderFolderLayout pLayout = layout.createPlaceholderFolder(bottom, IPageLayout.RIGHT, 0.65F, editor);

		leftFolder.addView("net.heartsome.cat.common.ui.navigator.view"); // 导航视图

		rightFolder.addView("net.heartsome.cat.ts.ui.translation.view.matchview");
//		topFirstFolder.addView("net.heartsome.cat.ts.ui.translation.view.matchview");
//		bottomFolder.addView("net.heartsome.cat.ts.ui.term.view.termView"); // 术语匹配视图
//		pLayout.addPlaceholder("net.heartsome.cat.ts.ui.qa.views.QAResultViewPart");
	
	}
}

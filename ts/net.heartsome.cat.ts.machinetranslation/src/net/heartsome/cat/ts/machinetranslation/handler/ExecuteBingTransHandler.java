/**
 * ExecuteBingTransHandler.java
 *
 * Version information :
 *
 * Date:2012-6-14
 *
 * Copyright notice :
 * 本文件及其附带的相关文件包含机密信息，仅限瀚特盛科技有限公司指定的，与本项目有关的内部人员和客户联络人员查阅和使用。 
 * 如果您不是本保密声明中指定的收件者，请立即销毁本文件，禁止对本文件或根据本文件中的内容采取任何其他行动， 
 * 包括但不限于：泄露本文件中的信息、以任何方式制作本文件全部或部分内容之副本、将本文件或其相关副本提供给任何其他人。
 */
package net.heartsome.cat.ts.machinetranslation.handler;

import net.heartsome.cat.ts.machinetranslation.SimpleMatcherBingImpl;
import net.heartsome.cat.ts.machinetranslation.bean.PrefrenceParameters;
import net.heartsome.cat.ts.machinetranslation.resource.Messages;
import net.heartsome.cat.ts.tm.simpleMatch.ISimpleMatcher;
import net.heartsome.cat.ts.ui.editors.IXliffEditor;
import net.heartsome.cat.ts.ui.translation.view.MatchViewPart;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;
import org.slf4j.LoggerFactory;

/**
 * @author jason
 * @version
 * @since JDK1.6
 */
public class ExecuteBingTransHandler extends AbstractHandler {

	/**
	 * (non-Javadoc)
	 * @see org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.ExecutionEvent)
	 */
	public Object execute(ExecutionEvent event) throws ExecutionException {

		final IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);
		IEditorPart editor = HandlerUtil.getActiveEditor(event);
		if (!(editor instanceof IXliffEditor)) {
			return null;
		}

		// check the google translation state: check the key availability
		PrefrenceParameters ps = PrefrenceParameters.getInstance();
		if (!ps.isBingState()) {
			MessageDialog.openError(window.getShell(), Messages.getString("handler.ExecuteBingTransHandler.msgTitle"),
					Messages.getString("handler.ExecuteBingTransHandler.msg"));
			return null;
		}
		
		final IXliffEditor xliffEditor = (IXliffEditor) editor;

		final int[] selectedRowIndexs = xliffEditor.getSelectedRows();
		if (selectedRowIndexs.length == 0) {
			return null;
		}
		ISimpleMatcher matcher = new SimpleMatcherBingImpl();
		IViewPart viewPart = window.getActivePage().findView(MatchViewPart.ID);
		if (viewPart != null && viewPart instanceof MatchViewPart) {
			MatchViewPart matchView = (MatchViewPart) viewPart;
			matchView.manualExecSimpleTranslation(selectedRowIndexs[0], xliffEditor, matcher);
		}

		return null;
	}

}

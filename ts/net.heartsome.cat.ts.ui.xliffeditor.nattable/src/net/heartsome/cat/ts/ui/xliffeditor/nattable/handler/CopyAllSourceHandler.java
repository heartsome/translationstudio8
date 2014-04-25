/**
 * CopyAllSourceCommand.java
 *
 * Version information :
 *
 * Date:2012-8-26
 *
 * Copyright notice :
 * 本文件及其附带的相关文件包含机密信息，仅限瀚特盛科技有限公司指定的，与本项目有关的内部人员和客户联络人员查阅和使用。 
 * 如果您不是本保密声明中指定的收件者，请立即销毁本文件，禁止对本文件或根据本文件中的内容采取任何其他行动， 
 * 包括但不限于：泄露本文件中的信息、以任何方式制作本文件全部或部分内容之副本、将本文件或其相关副本提供给任何其他人。
 */
package net.heartsome.cat.ts.ui.xliffeditor.nattable.handler;

import net.heartsome.cat.ts.core.file.XLFHandler;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.editor.HsMultiActiveCellEditor;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.editor.HsMultiCellEditorControl;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.editor.XLIFFEditorImplWithNatTable;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * @author Jason
 * @version
 * @since JDK1.6
 */
public class CopyAllSourceHandler extends AbstractHandler {

	/**
	 * (non-Javadoc)
	 * @see org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.ExecutionEvent)
	 */
	public Object execute(ExecutionEvent event) throws ExecutionException {
		final IEditorPart editor = HandlerUtil.getActiveEditor(event);
		if (editor instanceof XLIFFEditorImplWithNatTable) {
			BusyIndicator.showWhile(HandlerUtil.getActiveWorkbenchWindow(event).getShell().getDisplay(), new Runnable() {
				public void run() {
					// fixed Bug #2638 XLIFF 编辑器：复制所有源文到目标时，译文处于编辑模式的文本段“未能”正确复制
					HsMultiActiveCellEditor.commit(true);
					XLIFFEditorImplWithNatTable xliffEditor = (XLIFFEditorImplWithNatTable) editor;
					XLFHandler handler = xliffEditor.getXLFHandler();
					handler.copyAllSource2Target();
					xliffEditor.redraw();
					HsMultiCellEditorControl.activeSourceAndTargetCell(xliffEditor);
				}
			});

		}
		return null;
	}

}

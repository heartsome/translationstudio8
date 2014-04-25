package net.heartsome.cat.ts.ui.xliffeditor.nattable.handler;

import net.heartsome.cat.ts.core.file.XLFHandler;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.editor.XLIFFEditorImplWithNatTable;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * 删除选中文本段匹配 Handler
 * @author  peason
 * @version 
 * @since   JDK1.6
 */
public class DeleteSelectSegmentHandler extends AbstractHandler {

	public Object execute(ExecutionEvent event) throws ExecutionException {
//		IEditorPart editor = HandlerUtil.getActiveEditor(event);
//		if (!(editor instanceof XLIFFEditorImplWithNatTable)) {
//			return null;
//		}
//		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
//		boolean res = MessageDialog.openConfirm(window.getShell(), "删除确认", "确定要删除选中文本段翻译吗？");
//		if (res) {
//			XLIFFEditorImplWithNatTable xliffEditor = (XLIFFEditorImplWithNatTable) editor;
//			try {
//				xliffEditor.updateSegments(xliffEditor.getSelectedRowIds(), xliffEditor.getTgtColumnIndex(), "");
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//		}
		return null;
	}
}

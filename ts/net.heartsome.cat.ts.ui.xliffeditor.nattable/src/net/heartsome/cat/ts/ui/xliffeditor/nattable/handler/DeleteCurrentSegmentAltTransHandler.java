package net.heartsome.cat.ts.ui.xliffeditor.nattable.handler;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

/**
 * 删除当前文本段匹配
 * @author weachy
 * @version
 * @since JDK1.5
 */
public class DeleteCurrentSegmentAltTransHandler extends AbstractHandler {
	public Object execute(final ExecutionEvent event) throws ExecutionException {
//		final IEditorPart editor = HandlerUtil.getActiveEditor(event);
//		if (!(editor instanceof XLIFFEditorImplWithNatTable)) {
//			return null;
//		}
//		final IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
//		boolean res = MessageDialog.openConfirm(HandlerUtil.getActiveShell(event),
//				Messages.getString("handler.DeleteCurrentSegmentAltTransHandler.msgTitle1"),
//				Messages.getString("handler.DeleteCurrentSegmentAltTransHandler.msg1"));
//		if (res) {
//			BusyIndicator.showWhile(Display.getDefault(),
//					new Runnable() {
//						public void run() {
//							final XLIFFEditorImplWithNatTable xliffEditor = (XLIFFEditorImplWithNatTable) editor;
//							XLFHandler handler = xliffEditor.getXLFHandler();
//
//							final int[] rowIndexs = xliffEditor.getSelectedRows();
//							List<String> rowIds = new ArrayList<String>(xliffEditor.getXLFHandler()
//									.getRowIds(rowIndexs));
//
//							handler.deleteAltTrans(rowIds);
//							Display.getDefault().syncExec(new Runnable() {
//								public void run() {
//									IViewPart viewPart = window.getActivePage().findView(
//											"net.heartsome.cat.ts.ui.translation.view.matchview");
//									if (viewPart != null && viewPart instanceof IMatchViewPart && rowIndexs.length != 0) {
//										((IMatchViewPart) viewPart).refreshView(xliffEditor, rowIndexs[rowIndexs.length - 1]);
//									}
//								}
//							});
//						}
//					});
//		}
		return null;
	}

}

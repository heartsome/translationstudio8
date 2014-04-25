package net.heartsome.cat.ts.jumpsegment.handler;

import java.util.Arrays;

import net.heartsome.cat.ts.core.file.XLFHandler;
import net.heartsome.cat.ts.jumpsegment.resource.Messages;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.editor.XLIFFEditorImplWithNatTable;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.propertyTester.DeleteToEndOrToTagPropertyTester;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.utils.NattableUtil;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * 跳转到下一文本段（包括下一未翻译文本段，下一带疑问文本段，下一带标注文本段）的 Handler
 * @author peason
 * @version
 * @since JDK1.6
 */
public class JumpToNextHandler extends AbstractHandler {

	public Object execute(ExecutionEvent event) throws ExecutionException {
		IEditorPart editor = HandlerUtil.getActiveEditor(event);
		if (editor instanceof XLIFFEditorImplWithNatTable) {
			String jumpToNext = event.getParameter("JumpNextSegment");
			if (jumpToNext == null) {
				return null;
			}
			XLIFFEditorImplWithNatTable xliffEditor = (XLIFFEditorImplWithNatTable) editor;
			int[] selectedRows = xliffEditor.getSelectedRows();
			if (selectedRows.length < 1) {
				return null;
			}
			Arrays.sort(selectedRows);
			int lastSelectRow = selectedRows[selectedRows.length - 1];
			XLFHandler handler = xliffEditor.getXLFHandler();
			int nextRow = -1;
			Shell shell = HandlerUtil.getActiveShell(event);
			if (jumpToNext.equalsIgnoreCase("JumpNextNonTranslation")) {
				// 下一未翻译文本段
				nextRow = handler.getNextUntranslatedSegmentIndex(lastSelectRow);
				if (nextRow == -1) {
					MessageDialog.openInformation(shell, Messages.getString("handler.JumpToNextHandler.msgTitle"),
							Messages.getString("handler.JumpToNextHandler.msg1"));
					return null;
				}
			} else if (jumpToNext.equalsIgnoreCase("JumpNextQuestion")) {
				// 下一带疑问文本段
				nextRow = handler.getNextQuestionSegmentIndex(lastSelectRow);
				if (nextRow == -1) {
					MessageDialog.openInformation(shell, Messages.getString("handler.JumpToNextHandler.msgTitle"),
							Messages.getString("handler.JumpToNextHandler.msg2"));
					return null;
				}
			} else if (jumpToNext.equalsIgnoreCase("JumpNextNote")) {
				// 下一带标注文本段
				nextRow = handler.getNextNoteSegmentIndex(lastSelectRow);
				if (nextRow == -1) {
					MessageDialog.openInformation(shell, Messages.getString("handler.JumpToNextHandler.msgTitle"),
							Messages.getString("handler.JumpToNextHandler.msg3"));
					return null;
				}
			}
			xliffEditor.jumpToRow(nextRow);			
		}
		return null;
	}

}

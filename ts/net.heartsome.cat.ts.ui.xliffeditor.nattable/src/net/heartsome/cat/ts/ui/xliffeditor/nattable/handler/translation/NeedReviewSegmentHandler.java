package net.heartsome.cat.ts.ui.xliffeditor.nattable.handler.translation;

import java.util.List;

import net.heartsome.cat.ts.core.file.XLFHandler;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.editor.XLIFFEditorImplWithNatTable;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.utils.NattableUtil;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * 有疑问的 Handler
 * @author  peason
 * @version 
 * @since   JDK1.6
 */
public class NeedReviewSegmentHandler extends AbstractHandler {

	public Object execute(ExecutionEvent event) throws ExecutionException {
		IEditorPart editor = HandlerUtil.getActiveEditor(event);
		if (editor instanceof XLIFFEditorImplWithNatTable) {
			XLIFFEditorImplWithNatTable xliffEditor = (XLIFFEditorImplWithNatTable) editor;
			XLFHandler handler = xliffEditor.getXLFHandler();
			List<String> selectedRowIds = xliffEditor.getSelectedRowIds();
			boolean isNeedReview = true;
			//先判断所有的选择文本段的是否锁定状态
			for(String rowId : selectedRowIds){
				if (!handler.isNeedReview(rowId)) {
					isNeedReview = false;
					break;
				}
			}
			NattableUtil util = NattableUtil.getInstance(xliffEditor);
			util.changIsQuestionState(selectedRowIds, isNeedReview ? "no" : "yes");
		}
		return null;
	}

}

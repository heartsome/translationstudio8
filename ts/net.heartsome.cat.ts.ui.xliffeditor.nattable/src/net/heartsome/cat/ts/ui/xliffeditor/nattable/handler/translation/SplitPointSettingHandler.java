package net.heartsome.cat.ts.ui.xliffeditor.nattable.handler.translation;

import java.util.List;

import net.heartsome.cat.ts.ui.xliffeditor.nattable.editor.XLIFFEditorImplWithNatTable;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.resource.Messages;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.part.FileEditorInput;

/**
 * 设置 XLIFF 分割点
 * @author  peason
 * @version 
 * @since   JDK1.6
 */
public class SplitPointSettingHandler extends AbstractHandler {

	public Object execute(ExecutionEvent event) throws ExecutionException {
		IEditorPart editor = HandlerUtil.getActiveEditor(event);
		if (editor instanceof XLIFFEditorImplWithNatTable) {
			XLIFFEditorImplWithNatTable xliffEditor = (XLIFFEditorImplWithNatTable) editor;
			if (xliffEditor.isMultiFile()) {
				MessageDialog.openInformation(HandlerUtil.getActiveShell(event),
						Messages.getString("menu.BodyMenuConfiguration.msgTitle"),
						Messages.getString("menu.BodyMenuConfiguration.msg7"));
				return null;
			}
			List<String> selectedRowIds = xliffEditor.getSelectedRowIds();
			boolean isSplitPointExist = false;
			if (selectedRowIds != null && selectedRowIds.size() > 0) {
				for (String rowId : selectedRowIds) {
					if (xliffEditor.getSplitXliffPoints().contains(rowId)) {
						isSplitPointExist = true;
					} else {
						isSplitPointExist = false;
						break;
					}
				}
				if (!isSplitPointExist) {
					String xlfPath = ((FileEditorInput) xliffEditor.getEditorInput()).getFile().getLocation()
							.toOSString();
					for (String rowId : selectedRowIds) {
//						String firstTURowId = xliffEditor.getXLFHandler().getRowIdByXpath(xlfPath,
//								"/xliff/file[1]/body/descendant::trans-unit[1]");
//						if (firstTURowId != null && firstTURowId.equals(rowId)) {
//							MessageDialog.openInformation(HandlerUtil.getActiveShell(event),
//									Messages.getString("menu.BodyMenuConfiguration.msgTitle"),
//									Messages.getString("menu.BodyMenuConfiguration.msg8"));
//							continue;
//						}
						String lastTURowid = xliffEditor.getXLFHandler().getRowIdByXpath(xlfPath,
								"/xliff/file[last()]/body/descendant::trans-unit[last()]");
						if (lastTURowid != null && lastTURowid.equals(rowId)) {
							MessageDialog.openInformation(HandlerUtil.getActiveShell(event),
									Messages.getString("menu.BodyMenuConfiguration.msgTitle"),
									Messages.getString("menu.BodyMenuConfiguration.msg9"));
							continue;
						}
						if (!xliffEditor.getSplitXliffPoints().contains(rowId)) {
							// 将切割点的序列号添加到List中
							xliffEditor.getSplitXliffPoints().add(rowId);
							xliffEditor.getTable().redraw();
						}
					}
				} else {
					for (String rowId : selectedRowIds) {
						if (xliffEditor.getSplitXliffPoints().contains(rowId)) {
							// 删除切割点
							int index = xliffEditor.getSplitXliffPoints().indexOf(rowId);
							xliffEditor.getSplitXliffPoints().remove(index);
							xliffEditor.getTable().redraw();
						}
					}
				}
			}
		}
		return null;
	}

}

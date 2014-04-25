package net.heartsome.cat.ts.ui.xliffeditor.nattable.propertyTester;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.heartsome.cat.ts.core.file.RowIdUtil;
import net.heartsome.cat.ts.core.file.XLFHandler;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.editor.HsMultiActiveCellEditor;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.editor.StyledTextCellEditor;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.editor.XLIFFEditorImplWithNatTable;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

/**
 * 完成翻译，批准，草稿的 test
 * @author peason
 * @version
 * @since JDK1.6
 */
public class AddSegmentToTMPropertyTester extends PropertyTester {

	public static final String PROPERTY_NAMESPACE = "table";
	public static final String PROPERTY_ENABLED = "enabled";

	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
//		boolean enabled = false;
//		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
//		if (window != null) {
//			IWorkbenchPage page = window.getActivePage();
//			if (page != null) {
//				IEditorPart editor = page.getActiveEditor();
//				if (editor != null && editor instanceof XLIFFEditorImplWithNatTable) {
//					XLIFFEditorImplWithNatTable xliffEditor = (XLIFFEditorImplWithNatTable) editor;
//					if (xliffEditor != null && xliffEditor.getTable() != null) {
//						long l = System.currentTimeMillis();
//						XLFHandler handler = xliffEditor.getXLFHandler();
//						List<String> selectedRowIds = xliffEditor.getSelectedRowIds();
//						if (selectedRowIds != null && selectedRowIds.size() > 0) {
//							final Map<String, List<String>> tmpGroup = RowIdUtil.groupRowIdByFileName(selectedRowIds);
//							boolean hasNullTgt = true;
//							group: for (Entry<String, List<String>> entry : tmpGroup.entrySet()) {
//								List<String> lstRowIdList = entry.getValue();
//								for (String rowId : lstRowIdList) {
//									String tgtText = handler.getTgtContent(rowId);
//									if (tgtText != null && !tgtText.equals("") && hasNullTgt) {
//										hasNullTgt = false;
//										break group;
//									}
//								}
//							}
//							boolean isLock = true;
//							for (String rowId : selectedRowIds) {
//								if (!handler.isLocked(rowId)) {
//									isLock = false;
//								}
//							}
//							enabled = !hasNullTgt && !isLock;
//
//							if (!enabled) {
//								StyledTextCellEditor cellEditor = HsMultiActiveCellEditor.getTargetStyledEditor();
//								if (cellEditor != null && !cellEditor.isApprovedOrLocked()) {
//									StyledText styledText = cellEditor.getSegmentViewer().getTextWidget();
//									if (styledText != null) {
//										String text = styledText.getText();
//										if (text != null && !text.equals("")) {
//											enabled = true;
//										}
//									}
//								}
//							}
//						}
//						System.out.println(getClass() + ": "+ (System.currentTimeMillis() - l));
//					}
//				}
//			}
//		}
//
//		return enabled;
		return true;
	}

}

package net.heartsome.cat.ts.ui.xliffeditor.nattable.propertyTester;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.heartsome.cat.ts.core.file.RowIdUtil;
import net.heartsome.cat.ts.core.file.XLFHandler;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.editor.XLIFFEditorImplWithNatTable;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

/**
 * 签发的 test
 * @author  peason
 * @version 
 * @since   JDK1.6
 */
public class SignOffPropertyTester extends PropertyTester {

	public static final String PROPERTY_NAMESPACE = "signedOff";
	public static final String PROPERTY_ENABLED = "enabled";
	
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
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
//							boolean isLock = true;
//							boolean isDraft = true;
//							for (String rowId : selectedRowIds) {
//								if (!handler.isDraft(rowId) && isDraft) {
//									isDraft = false;
//									if (!isLock) {
//										break;
//									}
//								}
//								if (!handler.isLocked(rowId) && isLock) {
//									isLock = false;
//									if (!isDraft) {
//										break;
//									}
//								}
//							}
//							if (isLock || isDraft) {
//								return false;
//							}
//							final Map<String, List<String>> tmpGroup = RowIdUtil.groupRowIdByFileName(selectedRowIds);
//							boolean hasNullTgt = true;
//							group:
//							for (Entry<String, List<String>> entry : tmpGroup.entrySet()) {
//								List<String> lstRowIdList = entry.getValue();
//								for (String rowId : lstRowIdList) {
//									String tgtText = handler.getTgtContent(rowId);
//									if (tgtText != null && !tgtText.equals("") && hasNullTgt) {
//										hasNullTgt = false;
//										break group;
//									}
//								}
//							}
//							if (hasNullTgt) {
//								System.out.println(getClass() + ": "+ (System.currentTimeMillis() - l));
//								return false;
//							}
//							System.out.println(getClass() + ": "+ (System.currentTimeMillis() - l));
//							return true;
//						}
//						System.out.println(getClass() + ": "+ (System.currentTimeMillis() - l));
//					}
//				}
//			}
//		}
//		return false;
	return true;
	}

}

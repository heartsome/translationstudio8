package net.heartsome.cat.ts.ui.xliffeditor.nattable.propertyTester;

import java.util.List;

import net.heartsome.cat.ts.ui.xliffeditor.nattable.editor.XLIFFEditorImplWithNatTable;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

/**
 * 验证 XLIFF 编辑器选中状态的类
 * @author peason
 * @version
 * @since JDK1.6
 */
public class XLIFFEditorSelectionPropertyTester extends PropertyTester {

	public static final String PROPERTY_NAMESPACE = "xliffEditor";
	public static final String PROPERTY_ENABLED = "selectionCount";

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
//						List<String> selectedRowIds = xliffEditor.getSelectedRowIds();
//						enabled = (selectedRowIds != null && selectedRowIds.size() > 0);
//					}
//				}
//			}
//		}
//
//		return enabled;
		return true;
	}

}

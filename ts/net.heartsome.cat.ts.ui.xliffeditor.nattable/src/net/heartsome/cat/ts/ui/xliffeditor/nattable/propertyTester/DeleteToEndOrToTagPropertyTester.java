package net.heartsome.cat.ts.ui.xliffeditor.nattable.propertyTester;

import org.eclipse.core.expressions.PropertyTester;

/**
 * 删除光标后内容和删除标记前内容的 test
 * @author peason
 * @version
 * @since JDK1.6
 */
public class DeleteToEndOrToTagPropertyTester extends PropertyTester {

	public static final String PROPERTY_NAMESPACE = "DeleteContent";

	public static final String PROPERTY_ENABLED = "enabled";

	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
//		boolean enabled = false;
//		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
//		if (window != null) {
//			IWorkbenchPage page = window.getActivePage();
//			if (page != null) {
//				IEditorPart editor = page.getActiveEditor();
//				if (editor != null && editor instanceof XLIFFEditorImplWithNatTable) {
//					ICellEditor iCellEditor = ActiveCellEditor.getCellEditor();
//					if (iCellEditor != null) {
//						if (iCellEditor instanceof StyledTextCellEditor) {
//							StyledTextCellEditor cellEditor = (StyledTextCellEditor) iCellEditor;
//							if (!cellEditor.isClosed()) {
//								String type = cellEditor.getCellType();
//								// 只能删除目标文本
//								if (type.equals(NatTableConstant.TARGET)) {
//									enabled = true;
//								}
//							}
//						}
//					}
//				}
//			}
//		}

		return true;
	}

}

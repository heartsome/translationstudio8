/**
 * ShowHidenNonPrintingCharacter.java
 *
 * Version information :
 *
 * Date:2013-4-19
 *
 * Copyright notice :
 * 本文件及其附带的相关文件包含机密信息，仅限瀚特盛科技有限公司指定的，与本项目有关的内部人员和客户联络人员查阅和使用。 
 * 如果您不是本保密声明中指定的收件者，请立即销毁本文件，禁止对本文件或根据本文件中的内容采取任何其他行动， 
 * 包括但不限于：泄露本文件中的信息、以任何方式制作本文件全部或部分内容之副本、将本文件或其相关副本提供给任何其他人。
 */
package net.heartsome.cat.ts.ui.handlers;

import java.util.Map;

import net.heartsome.cat.ts.ui.Activator;
import net.heartsome.cat.ts.ui.editors.IXliffEditor;
import net.heartsome.cat.ts.ui.preferencepage.IPreferenceConstants;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.menus.UIElement;

/**
 * @author  Jason
 * @version 
 * @since   JDK1.6
 */
public class ShowHidenNonPrintingCharacterHandler extends AbstractHandler implements IElementUpdater{

	boolean isSelected =  Activator.getDefault().getPreferenceStore().getBoolean(IPreferenceConstants.XLIFF_EDITOR_SHOWHIDEN_NONPRINTCHARACTER);
	public Object execute(ExecutionEvent event) throws ExecutionException {
		isSelected = !isSelected;				
		try {
			IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
			if (window != null) {
				IWorkbenchPage page = window.getActivePage();
				if (page != null) {
					IEditorPart editor = page.getActiveEditor();
					if (editor != null && editor instanceof IXliffEditor) {
						((IXliffEditor) editor).refreshWithNonprinttingCharacter(isSelected);
					}
				}
			}
		} catch (NullPointerException e) {
			e.printStackTrace();
		}
		
		return null;
	}

	public void updateElement(UIElement element, @SuppressWarnings("rawtypes") Map parameters) {
		element.setChecked(isSelected);
	}

}

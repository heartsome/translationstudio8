package net.heartsome.cat.ts.ui.xliffeditor.nattable.handler;

import java.util.Map;

import net.heartsome.cat.ts.ui.xliffeditor.nattable.Activator;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.editor.XLIFFEditorImplWithNatTable;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.resource.ImageConstant;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.resource.Messages;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.menus.UIElement;

/**
 * 改变基于 NatTable 编辑器的布局 handler
 * @author cheney
 * @since JDK1.6
 */
public class ChangeEditorLayoutHandler extends AbstractHandler implements IElementUpdater {

	public Object execute(ExecutionEvent event) throws ExecutionException {
		IEditorPart editorPart = HandlerUtil.getActiveEditor(event);
		// 改为布局
		if (editorPart != null && editorPart instanceof XLIFFEditorImplWithNatTable) {
			// ActiveCellRegion.setActiveCellRegion(null);
			((XLIFFEditorImplWithNatTable) editorPart).changeLayout();
		}
		return null;
	}

	public void updateElement(UIElement element, Map parameters) {
		String layout = (String) parameters.get("xliffEditor.layout");
		if (XLIFFEditorImplWithNatTable.getCurrent().isHorizontalLayout()) {
			ImageDescriptor horizontalImageDescriptor = Activator
					.getImageDescriptor(layout == null ? ImageConstant.TOOL_LAYOUT_HORIZONTAL
							: "images/view/horizontal.png");
			element.setIcon(horizontalImageDescriptor);
			element.setTooltip(Messages.getString("handler.ChangeEditorLayoutHandler.horizontalTooltip"));
		} else {
			ImageDescriptor verticalImageDescriptor = Activator
					.getImageDescriptor(layout == null ? ImageConstant.TOOL_LAYOUT_VERTICAL
							: "images/view/vertical.png");
			element.setIcon(verticalImageDescriptor);
			element.setTooltip(Messages.getString("handler.ChangeEditorLayoutHandler.verticalTooltip"));
		}
	}

}

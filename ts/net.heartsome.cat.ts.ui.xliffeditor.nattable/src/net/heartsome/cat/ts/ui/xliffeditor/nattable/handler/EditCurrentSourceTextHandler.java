package net.heartsome.cat.ts.ui.xliffeditor.nattable.handler;

import net.heartsome.cat.ts.ui.xliffeditor.nattable.celleditor.EditableManager;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.celleditor.SourceEditMode;

/**
 * 编辑当前源文本的 Handler
 * @author  peason
 * @version 
 * @since   JDK1.6
 */
public class EditCurrentSourceTextHandler extends ChangeSourceEditableHandler {

	@Override
	public SourceEditMode getSourceEditMode(EditableManager editableManager) {
		editableManager.getSourceEditMode();
		return SourceEditMode.ONCE_EDITABLE;
	}
}

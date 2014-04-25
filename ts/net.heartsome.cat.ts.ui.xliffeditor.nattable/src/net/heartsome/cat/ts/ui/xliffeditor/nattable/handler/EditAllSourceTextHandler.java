package net.heartsome.cat.ts.ui.xliffeditor.nattable.handler;

import net.heartsome.cat.ts.ui.xliffeditor.nattable.celleditor.EditableManager;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.celleditor.SourceEditMode;

/**
 * 编辑所有源文本的 Handler
 * @author  peason
 * @version 
 * @since   JDK1.6
 */
public class EditAllSourceTextHandler extends ChangeSourceEditableHandler {

	@Override
	public SourceEditMode getSourceEditMode(EditableManager editableManager) {
		return editableManager.getSourceEditMode().ALWAYS_EDITABLE;
	}

}

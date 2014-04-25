package net.heartsome.cat.ts.ui.xliffeditor.nattable.handler.tags;

import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;

import net.heartsome.cat.ts.ui.xliffeditor.nattable.dialog.TagNumberRequest;

/**
 * 插入标记
 * @author weachy
 * @version
 * @since JDK1.5
 */
public class InsertTagHandler extends AbstractInsertTagHandler {

	@Override
	protected int getTagNum() {
		Shell shell = HandlerUtil.getActiveShell(getEvent());
		TagNumberRequest request = new TagNumberRequest(shell, sourceMaxTagIndex);
		request.show();
		return request.getNumber();
	}
}

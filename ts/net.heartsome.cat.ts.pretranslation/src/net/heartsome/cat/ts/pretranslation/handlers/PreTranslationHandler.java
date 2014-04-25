/**
 * PreTranslationHandler.java
 *
 * Version information :
 *
 * Date:2012-5-8
 *
 * Copyright notice :
 * 本文件及其附带的相关文件包含机密信息，仅限瀚特盛科技有限公司指定的，与本项目有关的内部人员和客户联络人员查阅和使用。 
 * 如果您不是本保密声明中指定的收件者，请立即销毁本文件，禁止对本文件或根据本文件中的内容采取任何其他行动， 
 * 包括但不限于：泄露本文件中的信息、以任何方式制作本文件全部或部分内容之副本、将本文件或其相关副本提供给任何其他人。
 */
package net.heartsome.cat.ts.pretranslation.handlers;

import java.util.List;

import net.heartsome.cat.common.resources.ResourceUtils;
import net.heartsome.cat.common.ui.handlers.AbstractSelectProjectFilesHandler;
import net.heartsome.cat.common.util.CommonFunction;
import net.heartsome.cat.ts.pretranslation.PreTransUitls;
import net.heartsome.cat.ts.ui.editors.IXliffEditor;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.part.FileEditorInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jason
 * @version
 * @since JDK1.6
 */
public class PreTranslationHandler extends AbstractSelectProjectFilesHandler {
	public static final Logger logger = LoggerFactory.getLogger(PreTranslationHandler.class);
	private static final String XLIFF_EDITOR_ID = "net.heartsome.cat.ts.ui.xliffeditor.nattable.editor";
	@Override
	public String[] getLegalFileExtensions() {
		return CommonFunction.xlfExtesionArray;
	}

	@Override
	public Object execute(ExecutionEvent event, List<IFile> list) {
		
		// 首先验证是否是合并打开的文件 --robert 2012-10-17
		if (isEditor) {
			try {
				IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
				IEditorReference[] editorRefe = window.getActivePage().findEditors(new FileEditorInput(list.get(0)),
						XLIFF_EDITOR_ID, IWorkbenchPage.MATCH_INPUT | IWorkbenchPage.MATCH_ID);
				if (editorRefe.length <= 0) {
					return null;
				}

				IXliffEditor xlfEditor = (IXliffEditor) editorRefe[0].getEditor(true);
				// 针对合并打开
				if (xlfEditor.isMultiFile()) {
					list = ResourceUtils.filesToIFiles(xlfEditor.getMultiFileList());
				}
			} catch (ExecutionException e) {
				logger.error("", e);
			}
			
		}
		
		CommonFunction.removeRepeateSelect(list);
		PreTransUitls.executeTranslation(list, shell);
		return null;
	}
}

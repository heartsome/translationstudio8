package net.heartsome.cat.ts.ui.xliffeditor.nattable.handler.export;

import java.io.File;
import java.net.URI;

import net.heartsome.cat.common.ui.utils.OpenEditorUtil;
import net.heartsome.cat.ts.core.file.XLFHandler;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.editor.XLIFFEditorImplWithNatTable;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.ide.FileStoreEditorInput;
import org.eclipse.ui.part.FileEditorInput;

/**
 * 此类未使用，因此未做国际化
 * @author  peason
 * @version 
 * @since   JDK1.6
 */
public class ExportAsTextHandler extends AbstractHandler {

	public Object execute(ExecutionEvent event) throws ExecutionException {
		String elementName = event.getParameter("elementName");

		IEditorPart activeEditor = HandlerUtil.getActiveEditor(event);
		Shell shell = activeEditor.getEditorSite().getShell();
		if (activeEditor == null || !(activeEditor instanceof XLIFFEditorImplWithNatTable)) {
			return null;
		}
		XLIFFEditorImplWithNatTable xliffEditor = (XLIFFEditorImplWithNatTable) activeEditor;
		if (xliffEditor.isMultiFile()) {
			MessageDialog.openInformation(shell, "", "当前编辑器打开了多个文件，无法执行该操作。");
		}

		IEditorInput input = xliffEditor.getEditorInput();
		URI uri = null;
		if (input instanceof FileEditorInput) {
			uri = ((FileEditorInput) input).getURI();
		} else if (input instanceof FileStoreEditorInput) {
			uri = ((FileStoreEditorInput) input).getURI();
		} else {
			return null;
		}
		File xliff = new File(uri);

		FileDialog fd = new FileDialog(shell, SWT.SAVE);
		String[] names = { "Plain Text Files [*.txt]", "All Files [*.*]" };
		String[] extensions = { "*.txt", "*.*" }; //$NON-NLS-1$ //$NON-NLS-2$
		fd.setFilterExtensions(extensions);
		fd.setFilterNames(names);

		fd.setFileName(xliff.getName() + ".txt"); //$NON-NLS-1$
		String out = fd.open();
		if (out == null) {
			return null;
		}

		XLFHandler handler = xliffEditor.getXLFHandler();
		boolean result = handler.saveAsText(xliff.getAbsolutePath(), out, elementName);

		if (result) {
			IWorkbenchPage page = xliffEditor.getEditorSite().getPage();
			OpenEditorUtil.OpenFileWithSystemEditor(page, out);
		} else {
			MessageDialog.openInformation(shell, "", "文件 “" + out + "” 保存失败！请重试。");
		}

		return null;
	}
}

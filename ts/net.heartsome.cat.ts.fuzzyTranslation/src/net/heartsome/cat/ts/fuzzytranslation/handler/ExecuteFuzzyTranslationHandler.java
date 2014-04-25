package net.heartsome.cat.ts.fuzzytranslation.handler;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.heartsome.cat.ts.core.file.RepeatRowSearcher;
import net.heartsome.cat.ts.core.file.XLFHandler;
import net.heartsome.cat.ts.fuzzytranslation.resource.Messages;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.editor.XLIFFEditorImplWithNatTable;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.utils.NattableUtil;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;
/**
 * 执行繁殖翻译(繁殖翻译不求匹配率，不查询数据库，只在文件内部繁殖源文本相同的文本段)
 * @author robert 2012-04-02
 * @version
 * @since JDK1.6
 */
public class ExecuteFuzzyTranslationHandler extends AbstractHandler {
	private static final String XLIFF_EDITOR_ID = "net.heartsome.cat.ts.ui.xliffeditor.nattable.editor";

	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);
		IEditorPart editor = window.getActivePage().getActiveEditor();
		if (!XLIFF_EDITOR_ID.equals(editor.getSite().getId())) {
			return null;
		}
		final XLIFFEditorImplWithNatTable nattable =  (XLIFFEditorImplWithNatTable) editor;
		final NattableUtil util = NattableUtil.getInstance(nattable);

		IRunnableWithProgress runnable = new IRunnableWithProgress() {

			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
//				monitor.beginTask(Messages.getString("translation.ExecuteFuzzyTranslationHandler.task1"), 8);
				// 首选获取有译文的trans-unit
				monitor.beginTask(Messages.getString("translation.ExecuteFuzzyTranslationHandler.task2"), 10);

				XLFHandler handler = nattable.getXLFHandler();
				Map<String, List<String>> rowIdMap = new HashMap<String, List<String>>();
				rowIdMap = RepeatRowSearcher.getRepeateRowsForFuzzy(handler);
				monitor.worked(1);
				
				util.propagateTranslations(rowIdMap, monitor);
				monitor.done();
			}
		};

		try {
			new ProgressMonitorDialog(nattable.getEditorSite().getShell()).run(true, true, runnable);
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		nattable.autoResize();
		return null;
	}
}

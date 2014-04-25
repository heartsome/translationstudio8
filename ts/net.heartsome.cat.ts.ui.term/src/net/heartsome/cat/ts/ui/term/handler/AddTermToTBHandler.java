package net.heartsome.cat.ts.ui.term.handler;

import net.heartsome.cat.ts.tb.importer.TbImporter;
import net.heartsome.cat.ts.ui.editors.IXliffEditor;
import net.heartsome.cat.ts.ui.term.dialog.AddTermToTBDialog;
import net.heartsome.cat.ts.ui.term.resource.Messages;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.part.FileEditorInput;

/**
 * 添加术语到术语库的 Handler
 * @author peason
 * @version
 * @since JDK1.6
 */
public class AddTermToTBHandler extends AbstractHandler {

	public Object execute(ExecutionEvent event) throws ExecutionException {
		IEditorPart editor = HandlerUtil.getActiveEditor(event);
		if (editor instanceof IXliffEditor) {
			IXliffEditor xliffEditor = (IXliffEditor) editor;
			IFile file = ((FileEditorInput) editor.getEditorInput()).getFile();
//			ProjectConfiger projectConfig = ProjectConfigerFactory.getProjectConfiger(file.getProject());
//			List<DatabaseModelBean> lstDatabase = projectConfig.getTermBaseDbs(true);
			TbImporter.getInstance().setProject(file.getProject());
			if (!TbImporter.getInstance().checkImporter()) {
				MessageDialog.openInformation(HandlerUtil.getActiveShell(event),
						Messages.getString("handler.AddTermToTBHandler.msgTitle"),
						Messages.getString("handler.AddTermToTBHandler.msg"));
				return null;
			}

			StringBuffer srcTerm = new StringBuffer();
			StringBuffer tgtTerm = new StringBuffer();
			String srcAllText = xliffEditor.getRowTransUnitBean(xliffEditor.getSelectedRows()[0]).getSrcText();
			xliffEditor.getSelectSrcOrTgtPureText(srcTerm, tgtTerm);

			AddTermToTBDialog dialog = AddTermToTBDialog.getInstance(editor.getSite().getShell(), srcTerm.toString().trim(),
					tgtTerm.toString().trim(),AddTermToTBDialog.ADD_TYPE);
			dialog.setProject(file.getProject());
			dialog.setSrcLang(xliffEditor.getSrcColumnName());
			dialog.setTgtLang(xliffEditor.getTgtColumnName());
			dialog.setSrcAllText(srcAllText);
			dialog.open();
		}
		return null;
	}

}

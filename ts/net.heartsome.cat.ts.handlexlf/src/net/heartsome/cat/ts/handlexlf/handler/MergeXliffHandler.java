package net.heartsome.cat.ts.handlexlf.handler;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import net.heartsome.cat.common.file.XLFValidator;
import net.heartsome.cat.common.ui.wizard.TSWizardDialog;
import net.heartsome.cat.common.util.CommonFunction;
import net.heartsome.cat.ts.handlexlf.resource.Messages;
import net.heartsome.cat.ts.handlexlf.split.SplitOrMergeXlfModel;
import net.heartsome.cat.ts.handlexlf.wizard.MergeXliffWizard;
import net.heartsome.cat.ts.handlexlf.wizard.NattableWizardDialog;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * 合并已经切割的Xliff文件
 * @author robert 2011-10-17
 */
public class MergeXliffHandler extends AbstractHandler {

	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
		final Shell shell = window.getShell();

		ISelection currentSelection = HandlerUtil.getCurrentSelection(event);
		if (currentSelection != null && !currentSelection.isEmpty() && currentSelection instanceof IStructuredSelection) {

			IStructuredSelection structuredSelection = (IStructuredSelection) currentSelection;

			if (structuredSelection.size() < 2) {
				MessageDialog.openInformation(shell, Messages.getString("handler.MergeXliffHandler.msgTitle1"),
						Messages.getString("handler.MergeXliffHandler.msg1"));
				return null;
			}

			Vector<IFile> seleFiles = new Vector<IFile>();
			String notXlfFile = "";
			@SuppressWarnings("rawtypes")
			Iterator selectIt = structuredSelection.iterator();
			while (selectIt.hasNext()) {
				Object object = selectIt.next();
				if (object instanceof IFile) {
					IFile selectFile = (IFile) object;
					String fileExtension = selectFile.getFileExtension();

					// 如果后缀名不是xlf，那么就进行提示
					if (fileExtension == null || !CommonFunction.validXlfExtension(fileExtension)) {
						notXlfFile += selectFile.getFullPath().toOSString() + "，";
					}
					seleFiles.add(selectFile);
				}
			}

			if (notXlfFile.length() > 0) {
				notXlfFile = notXlfFile.substring(0, notXlfFile.length() - 1);
				boolean isSure = MessageDialog.openConfirm(shell, Messages
						.getString("handler.MergeXliffHandler.msgTitle2"), MessageFormat.format(
						Messages.getString("handler.MergeXliffHandler.msg2"), new Object[] { notXlfFile }));
				if (!isSure) {
					return null;
				}
			}
			
			List<IFile> lstFiles = new ArrayList<IFile>();
			XLFValidator.resetFlag();
			for (IFile iFile : seleFiles) {
				if (!XLFValidator.validateXliffFile(iFile)) {
					lstFiles.add(iFile);
				}
			}
			XLFValidator.resetFlag();
			seleFiles.removeAll(lstFiles);
			if (seleFiles.size() == 0) {
				return null;
			}
			
			if (seleFiles.size() > 0) {
				String projectPath = seleFiles.get(0).getProject().getFullPath().toOSString();
				for (int i = 1; i < seleFiles.size(); i++) {
					if (!projectPath.equals(seleFiles.get(i).getProject().getFullPath().toOSString())) {
						MessageDialog.openInformation(shell, Messages.getString("handler.MergeXliffHandler.msgTitle1"),
								Messages.getString("handler.MergeXliffHandler.msg3"));
						return null;
					}
				}
				SplitOrMergeXlfModel model = new SplitOrMergeXlfModel();
				model.setMergeXliffFile(seleFiles);
				MergeXliffWizard wizard = new MergeXliffWizard(model);
				TSWizardDialog dialog = new NattableWizardDialog(shell, wizard);
				dialog.open();
			}
		}
		return null;
	}
}

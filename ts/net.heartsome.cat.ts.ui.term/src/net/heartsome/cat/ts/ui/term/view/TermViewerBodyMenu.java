/**
 * MatchViewerMenuManager.java
 *
 * Version information :
 *
 * Date:2013-3-28
 *
 * Copyright notice :
 * 本文件及其附带的相关文件包含机密信息，仅限瀚特盛科技有限公司指定的，与本项目有关的内部人员和客户联络人员查阅和使用。 
 * 如果您不是本保密声明中指定的收件者，请立即销毁本文件，禁止对本文件或根据本文件中的内容采取任何其他行动， 
 * 包括但不限于：泄露本文件中的信息、以任何方式制作本文件全部或部分内容之副本、将本文件或其相关副本提供给任何其他人。
 */
package net.heartsome.cat.ts.ui.term.view;

import java.sql.SQLException;
import java.util.List;

import net.heartsome.cat.common.bean.DatabaseModelBean;
import net.heartsome.cat.database.DBOperator;
import net.heartsome.cat.database.service.DatabaseService;
import net.heartsome.cat.ts.core.file.ProjectConfiger;
import net.heartsome.cat.ts.core.file.ProjectConfigerFactory;
import net.heartsome.cat.ts.tb.importer.TbImporter;
import net.heartsome.cat.ts.ui.editors.IXliffEditor;
import net.heartsome.cat.ts.ui.term.Activator;
import net.heartsome.cat.ts.ui.term.ImageConstants;
import net.heartsome.cat.ts.ui.term.dialog.AddTermToTBDialog;
import net.heartsome.cat.ts.ui.term.resource.Messages;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.nebula.widgets.grid.GridItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jason
 * @version
 * @since JDK1.6
 */
class TermViewerBodyMenu {

	private static final Logger logger = LoggerFactory.getLogger(TermViewerBodyMenu.class);

	private Menu bodyMenu;

	private TerminologyViewPart termView;

	AddTermAction addAction;

	DeleteTermAction deleteAction;

	EditTermAction editAction;

	public TermViewerBodyMenu(TerminologyViewPart termView) {
		this.termView = termView;
	}

	public Menu getBodyMenu() {
		return this.bodyMenu;
	}

	public void createMenu(Action[] addActions) {
		MenuManager menuMgr = new MenuManager();
		bodyMenu = menuMgr.createContextMenu(this.termView.getGridTable());
		bodyMenu.addListener(SWT.Show, new Listener() {
			public void handleEvent(Event event) {
				updateActionsState();
			}
		});
		if (null != addActions && addActions.length != 0) {
			for (Action action : addActions) {
				if (null != action) {
					menuMgr.add(action);
				}
			}
		}
		addAction = new AddTermAction();
		menuMgr.add(addAction);
		editAction = new EditTermAction();
		menuMgr.add(editAction);
		deleteAction = new DeleteTermAction();
		menuMgr.add(deleteAction);
	}

	void updateActionsState() {
		addAction.updateEnabledState();
		deleteAction.updateEnabledState();
		editAction.updateActionsState();
	}

	class EditTermAction extends Action {
		public EditTermAction() {
			setText(Messages.getString("view.TerminologyViewPart.menu.editterm"));
			setToolTipText(Messages.getString("view.TerminologyViewPart.menu.editterm"));
			setImageDescriptor(Activator.getImageDescriptor(ImageConstants.EDIT_TERM));
		}

		@Override
		public void run() {
			editTerm();
			refreshResources(getCurentProject());
		}

		void updateActionsState() {
			IProject curentProject = getCurentProject();
			GridItem selectItem = getSelectItem();
			if (curentProject != null && null != selectItem) {
				setEnabled(true);
			} else {
				setEnabled(false);
			}
		}
	}

	class DeleteTermAction extends Action {

		public DeleteTermAction() {
			setText(Messages.getString("view.TerminologyViewPart.menu.deleteterm"));
			setToolTipText(Messages.getString("view.TerminologyViewPart.menu.deleteterm"));
			setImageDescriptor(Activator.getImageDescriptor(ImageConstants.DELETE_TERM));
		}

		@Override
		public void run() {
			deleteSelectTerm();
			refreshResources(getCurentProject());
		}

		public void updateEnabledState() {
			IProject curentProject = getCurentProject();
			GridItem selectItem = getSelectItem();
			if (curentProject != null && null != selectItem) {
				setEnabled(true);
			} else {
				setEnabled(false);
			}

		}
	};

	class AddTermAction extends Action {
		public AddTermAction() {
			setText(Messages.getString("view.TerminologyViewPart.menu.addterm"));
			setToolTipText(Messages.getString("view.TerminologyViewPart.menu.addterm"));
			setImageDescriptor(Activator.getImageDescriptor(ImageConstants.ADD_TERM));
		}

		@Override
		public void run() {
			addTerm();
		}

		public void updateEnabledState() {
			if (getCurentProject() == null) {
				setEnabled(false);
			} else {
				setEnabled(true);
			}
		}
	};

	void refreshResources(IProject project) {
		this.termView.refresh();
		try {
			project.refreshLocal(IResource.DEPTH_INFINITE, null);
		} catch (CoreException e) {
			logger.error("refresh " + project.getName() + "error", e);
		}
	}

	IProject getCurentProject() {
		IEditorPart activeEditor = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
				.getActiveEditor();
		if (null == activeEditor) {
			return null;
		}
		if (activeEditor instanceof IXliffEditor) {
			IFile file = ((FileEditorInput) activeEditor.getEditorInput()).getFile();
			return file.getProject();
		}
		return null;
	}

	DBOperator getDbOperator(IProject project) {
		if (null == project) {
			return null;
		}
		ProjectConfiger projectConfiger = ProjectConfigerFactory.getProjectConfiger(project);
		List<DatabaseModelBean> termBaseDbs = projectConfiger.getTermBaseDbs(true);
		if (null == termBaseDbs || termBaseDbs.isEmpty()) {
			return null;
		}
		return DatabaseService.getDBOperator(termBaseDbs.get(0).toDbMetaData());

	}

	GridItem getSelectItem() {
		GridItem[] selection = this.termView.getGridTable().getSelection();
		if (null == selection || selection.length == 0) {
			return null;
		}
		return selection[0];
	}

	String getSrcLang() {
		return this.termView.srcLang;
	}

	String getTgtLang() {
		return this.termView.tgtLang;
	}

	void deleteSelectTerm() {
		IProject project = getCurentProject();
		if (null == project) {
			// show some msg here
			return;
		}
		DBOperator dbOperator = getDbOperator(project);
		if (null == dbOperator) {
			// show error msg here
			MessageDialog.openInformation(this.termView.getSite().getShell(),
					Messages.getString("handler.AddTermToTBHandler.msgTitle"),
					Messages.getString("handler.AddTermToTBHandler.msg"));
			return;
		}

		GridItem selectItem = getSelectItem();
		if (null == selectItem) {
			// show some msg here
			return;
		}

		try {
			dbOperator.start();
			dbOperator.beginTransaction();
			Object id = selectItem.getData("DBID");
			if (id instanceof String) {
				String termId = (String) id;
				dbOperator.deleteTermEntry(termId, getSrcLang(), getTgtLang());
			} 
			dbOperator.commit();

		} catch (SQLException e) {
			MessageDialog.openInformation(this.termView.getSite().getShell(),
					Messages.getString("view.TerminologyViewPart.action.msg"),
					Messages.getString("view.TerminologyViewPart.action.deleteFailed"));
			
			try {
				dbOperator.rollBack();
			} catch (SQLException e1) {
				logger.error("RollBack error",e);
			}
			logger.error("execute sql error",e);
		} catch (ClassNotFoundException e) {
			logger.error("data base driver not found",e);
		} finally {
			if (null != dbOperator) {
				try {
					dbOperator.end();
				} catch (SQLException e) {
					logger.error("",e);
				}
			}
		}

	}

	void addTerm() {
		IEditorPart editor = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
		if (editor instanceof IXliffEditor) {
			IXliffEditor xliffEditor = (IXliffEditor) editor;
			IFile file = ((FileEditorInput) editor.getEditorInput()).getFile();
			TbImporter.getInstance().setProject(file.getProject());
			if (!TbImporter.getInstance().checkImporter()) {
				MessageDialog.openInformation(editor.getSite().getShell(),
						Messages.getString("handler.AddTermToTBHandler.msgTitle"),
						Messages.getString("handler.AddTermToTBHandler.msg"));
				return;
			}

			StringBuffer srcTerm = new StringBuffer();
			StringBuffer tgtTerm = new StringBuffer();
			String srcAllText = xliffEditor.getRowTransUnitBean(xliffEditor.getSelectedRows()[0]).getSrcText();
			xliffEditor.getSelectSrcOrTgtPureText(srcTerm, tgtTerm);

			AddTermToTBDialog dialog = AddTermToTBDialog.getInstance(editor.getSite().getShell(), srcTerm.toString()
					.trim(), tgtTerm.toString().trim(), AddTermToTBDialog.ADD_TYPE);
			dialog.setProject(file.getProject());
			dialog.setSrcLang(xliffEditor.getSrcColumnName());
			dialog.setTgtLang(xliffEditor.getTgtColumnName());
			dialog.setSrcAllText(srcAllText);
			dialog.open();
		}
	}

	void editTerm() {
		IProject curentProject = getCurentProject();
		DBOperator dbOperator = getDbOperator(curentProject);
		try {
			if (dbOperator.getConnection() == null) {
				dbOperator.start();
			}
			GridItem selectItem = getSelectItem();
			String srcTerm = selectItem.getText(1);
			String tgtTerm = selectItem.getText(2);
			String properValue = selectItem.getText(3);
			AddTermToTBDialog dialog = AddTermToTBDialog.getInstance(this.termView.getSite().getShell(), srcTerm
					.toString().trim(), tgtTerm.toString().trim(), AddTermToTBDialog.EDIT_TYPE);
			dialog.setProject(curentProject);
			dialog.setSrcLang(getSrcLang());
			dialog.setTgtLang(getTgtLang());
			dialog.setPropertyValue(properValue);
			dialog.setDbOperator(dbOperator);
			Object id = selectItem.getData("DBID");
			int open = dialog.open();
			if (Dialog.OK == open) {
				if (id instanceof String) {
					String termId = (String) id;
					dbOperator.beginTransaction();
					dbOperator.deleteTermEntry(termId, getSrcLang(), getTgtLang());
				}
				dbOperator.commit();
			}
		} catch (Exception e) {
			MessageDialog.openInformation(this.termView.getSite().getShell(),
					Messages.getString("view.TerminologyViewPart.action.msg"),
					Messages.getString("view.TerminologyViewPart.action.editFailed"));
			try {
				dbOperator.rollBack();
			} catch (SQLException e1) {
				logger.error("RollBack error", e);
			}
			logger.error("", e);

		} finally {
			try {
				dbOperator.end();
			} catch (SQLException e) {
				logger.error("", e);
			}
		}

	}
}

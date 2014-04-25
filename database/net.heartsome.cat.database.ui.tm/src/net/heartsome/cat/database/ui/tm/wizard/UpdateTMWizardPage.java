package net.heartsome.cat.database.ui.tm.wizard;

import java.util.ArrayList;

import net.heartsome.cat.database.ui.tm.Activator;
import net.heartsome.cat.database.ui.tm.resource.Messages;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

/**
 * 更新记忆库页面
 * @author  peason
 * @version 
 * @since   JDK1.6
 */
public class UpdateTMWizardPage extends WizardPage {
	
	private ArrayList<IFile> lstXLIFF;
	
	/** 草稿复选框 */
	private Button btnDraft;

	/** 完成翻译复选框 */
	private Button btnTranslated;

	/** 已批准复选框 */
	private Button btnApproved;

	/** 已签发复选框 */
	private Button btnSignedOff;
	
	/** 锁定复选框 */
	private Button btnLocked;

	protected UpdateTMWizardPage(String pageName, ArrayList<IFile> lstXLIFF) {
		super(pageName);
		this.lstXLIFF = lstXLIFF;
	}

	public void createControl(Composite parent) {
		setTitle(Messages.getString("wizard.UpdateTMWizardPage.title"));
		Composite tparent = new Composite(parent, SWT.None);
		tparent.setLayout(new GridLayout());
		GridDataFactory.fillDefaults().hint(700, 500).grab(true, true).applyTo(tparent);
		createContent(tparent);
		setImageDescriptor(Activator.getImageDescriptor("images/dialog/update-tm-logo.png"));
		setControl(parent);
		setDescription(Messages.getString("wizard.UpdateTMWizardPage.description"));
	}
	
	public void createContent(Composite parent) {
		TableViewer tableViewer = new TableViewer(parent, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
		final Table table = tableViewer.getTable();
		GridData tableData = new GridData(GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL | GridData.FILL_BOTH);
		tableData.heightHint = 160;
		table.setLayoutData(tableData);
		table.setLinesVisible(true);
		table.setHeaderVisible(true);
		
		TableColumn columnNumber = new TableColumn(table, SWT.LEFT);
		columnNumber.setText(Messages.getString("wizard.UpdateTMWizardPage.columnNumber"));
		columnNumber.setWidth(50);
		
		TableColumn columnPath = new TableColumn(table, SWT.LEFT);
		columnPath.setText(Messages.getString("wizard.UpdateTMWizardPage.columnPath"));
		columnPath.setWidth(400);
		
		tableViewer.setLabelProvider(new TableViewerLabelProvider());
		tableViewer.setContentProvider(new ArrayContentProvider());
		tableViewer.setInput(getTableInfo());
		
		Group groupStatus = new Group(parent, SWT.None);
		groupStatus.setLayout(new GridLayout());
		groupStatus.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		groupStatus.setText(Messages.getString("dialog.UpdateTMDialog.lbl"));
		btnDraft = new Button(groupStatus, SWT.CHECK);
		btnDraft.setText(Messages.getString("dialog.UpdateTMDialog.btnDraft"));

		btnTranslated = new Button(groupStatus, SWT.CHECK);
		btnTranslated.setText(Messages.getString("dialog.UpdateTMDialog.btnTranslated"));
		btnTranslated.setSelection(true);

		btnApproved = new Button(groupStatus, SWT.CHECK);
		btnApproved.setText(Messages.getString("dialog.UpdateTMDialog.btnApproved"));
		btnApproved.setSelection(true);

		btnSignedOff = new Button(groupStatus, SWT.CHECK);
		btnSignedOff.setText(Messages.getString("dialog.UpdateTMDialog.btnSignedOff"));
		btnSignedOff.setSelection(true);
		
		btnLocked = new Button(groupStatus, SWT.CHECK);
		btnLocked.setText(Messages.getString("dialog.UpdateTMWizardPage.btnLocked"));
	}
	
	class TableViewerLabelProvider extends LabelProvider implements ITableLabelProvider{
		
		public Image getColumnImage(Object element, int columnIndex) {
			// TODO Auto-generated method stub
			return null;
		}
		public String getColumnText(Object element, int columnIndex) {
			if (element instanceof String[]) {
				String[] array = (String[]) element;
				return array[columnIndex];
			}
			return null;
		}
	}
	
	private String[][] getTableInfo() {
		ArrayList<String[]> tableInfos = new ArrayList<String[]>();
		for (int i = 0; i < lstXLIFF.size(); i++) {
			String[] tableInfo = new String[]{String.valueOf(i + 1), lstXLIFF.get(i).getFullPath().toOSString() };
			tableInfos.add(tableInfo);
		}
		return tableInfos.toArray(new String[][]{});
	}
	
	public boolean isDraft() {
		return btnDraft.getSelection();
	}
	
	public boolean isTranslated() {
		return btnTranslated.getSelection();
	}
	
	public boolean isApproved() {
		return btnApproved.getSelection();
	}
	
	public boolean isSignedOff() {
		return btnSignedOff.getSelection();
	}
	
	public boolean isLocked() {
		return btnLocked.getSelection();
	}
}

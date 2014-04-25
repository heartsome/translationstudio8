package net.heartsome.cat.ts.ui.xliffeditor.nattable.dialog;

import java.util.HashMap;
import java.util.Vector;

import net.heartsome.cat.common.util.TextUtil;
import net.heartsome.cat.ts.core.bean.NoteBean;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.editor.NatTableConstant;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.editor.XLIFFEditorImplWithNatTable;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.resource.Messages;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

/**
 * 编辑批注对话框
 * @author peason
 * @version
 * @since JDK1.6
 */
public class UpdateNoteDialog extends Dialog {

	private XLIFFEditorImplWithNatTable xliffEditor;

	private TableViewer tableViewer;

//	private String rowId;
	private int rowIndex;
	
	private Button btnAdd;

	private Button btnEdit;

	private Button btnDelete;

	public UpdateNoteDialog(Shell parentShell, XLIFFEditorImplWithNatTable xliffEditor, int rowIndex) {
		super(parentShell);
//		this.rowId = rowId;
		this.rowIndex = rowIndex;
		this.xliffEditor = xliffEditor;
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(Messages.getString("dialog.UpdateNoteDialog.title"));
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite tparent = (Composite) super.createDialogArea(parent);
		GridLayoutFactory.fillDefaults().numColumns(2).equalWidth(false).extendedMargins(5, 5, 5, 5).applyTo(tparent);
		GridDataFactory.fillDefaults().hint(620, 250).grab(true, true).applyTo(tparent);

		Group noteGroup = new Group(tparent, SWT.None);
		noteGroup.setText(Messages.getString("dialog.UpdateNoteDialog.noteGroup"));
		GridDataFactory.fillDefaults().grab(true, true).applyTo(noteGroup);
		noteGroup.setLayout(new GridLayout());

		tableViewer = new TableViewer(noteGroup, SWT.BORDER | SWT.SINGLE | SWT.FULL_SELECTION | SWT.H_SCROLL
				| SWT.V_SCROLL);
		Table table = tableViewer.getTable();
		table.setLayoutData(new GridData(GridData.FILL_BOTH));
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		String[] arrColName = new String[] { Messages.getString("dialog.UpdateNoteDialog.tableColumn1"),
				Messages.getString("dialog.UpdateNoteDialog.tableColumn2"),
				Messages.getString("dialog.UpdateNoteDialog.tableColumn3"),
				Messages.getString("dialog.UpdateNoteDialog.tableColumn4"),
				Messages.getString("dialog.UpdateNoteDialog.tableColumn5") };
		int[] arrColWidth = new int[] { 40, 100, 100, 150, 120 };
		for (int i = 0; i < arrColName.length; i++) {
			TableColumn column = new TableColumn(table, SWT.LEFT);
			column.setWidth(arrColWidth[i]);
			column.setText(arrColName[i]);
		}
		tableViewer.setLabelProvider(new TableViewerLabelProvider());
		tableViewer.setContentProvider(new ArrayContentProvider());

		Composite cmpBtn = new Composite(tparent, SWT.None);
		// cmpBtn.setLayout(new GridLayout());
		GridLayoutFactory.fillDefaults().numColumns(1).extendedMargins(0, 0, 35, 5).applyTo(cmpBtn);
		cmpBtn.setLayoutData(new GridData(GridData.FILL_VERTICAL));
		btnAdd = new Button(cmpBtn, SWT.NONE);
		btnAdd.setText(Messages.getString("dialog.UpdateNoteDialog.btnAdd"));
		btnAdd.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		btnEdit = new Button(cmpBtn, SWT.NONE);
		btnEdit.setText(Messages.getString("dialog.UpdateNoteDialog.btnEdit"));
		btnEdit.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		btnDelete = new Button(cmpBtn, SWT.NONE);
		btnDelete.setText(Messages.getString("dialog.UpdateNoteDialog.btnDelete"));
		btnDelete.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		initTable();
		initListener();

		return tparent;
	}

	private void initTable() {		
			Vector<NoteBean> noteBeans = xliffEditor.getRowTransUnitBean(rowIndex).getNotes();
			if (noteBeans != null && noteBeans.size() > 0) {
				String[][] arrTableItem = new String[noteBeans.size()][];
				for (int i = 0; i < noteBeans.size(); i++) {
					NoteBean bean = noteBeans.get(i);
					String strNote = bean.getNoteText();
					String date = "";
					String strText = "";
					if (strNote != null) {
//						Bug #2334:添加批注包含特殊字符保存失败
						strNote = TextUtil.resetSpecialString(strNote);
						if (strNote.indexOf(":") != -1) {
							date = strNote.substring(0, strNote.indexOf(":"));
							if (validData(date)) {
								strText = strNote.substring(strNote.indexOf(":") + 1);
							}else {
								date = "";
								strText = strNote;
							}
						} else {
							strText = strNote;
						}
					}
					String strCurrent = bean.getApplyCurrent();
					if (strCurrent == null || strCurrent.equals("Yes")) {
						strCurrent = NatTableConstant.CURRENT_TEXT;
					} else {
						strCurrent = NatTableConstant.ALL_TEXT;
					}
					arrTableItem[i] = new String[] { String.valueOf(i + 1), bean.getFrom(), date, strText, strCurrent };
				}
				tableViewer.setInput(arrTableItem);
			} else {
				tableViewer.setInput(null);
			}	
	}

	private void initListener() {
		btnAdd.addSelectionListener(new SelectionListener() {

			public void widgetSelected(SelectionEvent e) {
				AddOrUpdateNoteDialog dialog = new AddOrUpdateNoteDialog(getShell(), xliffEditor,
						AddOrUpdateNoteDialog.DIALOG_ADD, null);
				if (dialog.open() == Window.OK) {
					initTable();
				}
			}

			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});

		btnEdit.addSelectionListener(new SelectionListener() {

			public void widgetSelected(SelectionEvent e) {
				if (tableViewer.getTable().getSelectionCount() != 1) {
					MessageDialog.openInformation(getShell(), Messages.getString("dialog.UpdateNoteDialog.msgTitle1"), Messages.getString("dialog.UpdateNoteDialog.msg2"));
					return;
				}
				TableItem item = tableViewer.getTable().getSelection()[0];
				String[] arrNoteItem = new String[] { item.getText(0), item.getText(1), item.getText(2),
						item.getText(3), item.getText(4) };
				AddOrUpdateNoteDialog dialog = new AddOrUpdateNoteDialog(getShell(), xliffEditor,
						AddOrUpdateNoteDialog.DIALOG_EDIT, arrNoteItem);
				if (dialog.open() == Window.OK) {
					initTable();
				}
			}

			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});

		btnDelete.addSelectionListener(new SelectionListener() {

			public void widgetSelected(SelectionEvent e) {
				TableItem[] items = tableViewer.getTable().getSelection();
				if (items.length == 0) {
					MessageDialog.openInformation(getShell(), Messages.getString("dialog.UpdateNoteDialog.msgTitle1"), Messages.getString("dialog.UpdateNoteDialog.msg3"));
					return;
				}
				if (MessageDialog.openConfirm(getShell(), Messages.getString("dialog.UpdateNoteDialog.msgTitle2"), Messages.getString("dialog.UpdateNoteDialog.msg4"))) {
					Vector<NoteBean> lstBeans = new Vector<NoteBean>();
					for (TableItem item : items) {
						String date = item.getText(2);
						String noteText = "";
						if (date != null && !date.equals("")) {
							noteText += date + ":";
						}
						NoteBean bean = new NoteBean(noteText + item.getText(3));
						bean.setFrom(item.getText(1));
						String strApplyCurrent = item.getText(4);
						strApplyCurrent = strApplyCurrent.equals(NatTableConstant.CURRENT_TEXT) ? "Yes" : "No";
						bean.setApplyCurrent(strApplyCurrent);
						lstBeans.add(bean);
					}
					HashMap<String, Vector<NoteBean>> mapNote = new HashMap<String, Vector<NoteBean>>();
					mapNote.put(xliffEditor.getXLFHandler().getRowId(rowIndex), lstBeans);
					xliffEditor.getXLFHandler().deleteNote(mapNote);
					initTable();
				}
			}

			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});
	}

	// @Override
	// protected Control createButtonBar(Composite parent) {
	// Control btnBar = super.createButtonBar(parent);
	// getButton(IDialogConstants.OK_ID).dispose();
	// getButton(IDialogConstants.CANCEL_ID).dispose();
	// parent.layout();
	// return btnBar;
	// }

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		Composite cmp = parent.getParent();
		parent.dispose();
		cmp.layout();
	}
	
	/**
	 * 验证批注里面的时间是否合法。如果不合法返回false。--robert		2012-06-29
	 * @param date
	 * @return
	 */
	private boolean validData(String date){
		String regex = "\\d{4}-\\d{1,2}-\\d{1,2}";
		return date.matches(regex);
	}

	class TableViewerLabelProvider extends LabelProvider implements ITableLabelProvider {

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
}

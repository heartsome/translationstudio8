package net.heartsome.cat.ts.ui.xliffeditor.nattable.dialog;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.heartsome.cat.common.util.DateUtils;
import net.heartsome.cat.common.util.TextUtil;
import net.heartsome.cat.ts.core.file.RowIdUtil;
import net.heartsome.cat.ts.ui.Activator;
import net.heartsome.cat.ts.ui.preferencepage.IPreferenceConstants;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.editor.NatTableConstant;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.editor.XLIFFEditorImplWithNatTable;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.resource.Messages;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * Nattable 右键菜单中添加批注对话框
 * @author peason
 * @version
 * @since JDK1.6
 */
public class AddOrUpdateNoteDialog extends Dialog {

	public static final int DIALOG_ADD = 0;

	public static final int DIALOG_EDIT = 1;

	private XLIFFEditorImplWithNatTable xliffEditor;

	/** 应用范围下拉框 */
	private Combo cmbRange;

	/** 属性 */
	private Text txtNote;

	private int addOrEditDialog;

	private String[] noteItem;

	public AddOrUpdateNoteDialog(Shell parentShell, XLIFFEditorImplWithNatTable xliffEditor, int addOrEditDialog,
			String[] noteItem) {
		super(parentShell);
		this.xliffEditor = xliffEditor;
		this.addOrEditDialog = addOrEditDialog;
		this.noteItem = noteItem;
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		if (addOrEditDialog == DIALOG_ADD) {
			newShell.setText(Messages.getString("dialog.AddOrUpdateNoteDialog.title1"));
		} else if (addOrEditDialog == DIALOG_EDIT) {
			newShell.setText(Messages.getString("dialog.AddOrUpdateNoteDialog.title2"));
		}
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite tparent = (Composite) super.createDialogArea(parent);
		GridLayoutFactory.fillDefaults().numColumns(2).equalWidth(false).extendedMargins(5, 5, 5, 5).applyTo(tparent);
		GridDataFactory.fillDefaults().hint(400, 170).grab(true, true).applyTo(tparent);

		if (addOrEditDialog == DIALOG_ADD) {
			new Label(tparent, SWT.None).setText(Messages.getString("dialog.AddOrUpdateNoteDialog.label"));
			cmbRange = new Combo(tparent, SWT.READ_ONLY);
			GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(cmbRange);
			cmbRange.setItems(new String[] { NatTableConstant.CURRENT_TEXT, NatTableConstant.ALL_TEXT });
			cmbRange.select(0);
		}

		Group noteGroup = new Group(tparent, SWT.None);
		noteGroup.setText(Messages.getString("dialog.AddOrUpdateNoteDialog.noteGroup"));
		GridDataFactory.fillDefaults().span(2, 1).grab(true, true).applyTo(noteGroup);
		// noteGroup.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_BLUE));
		noteGroup.setLayout(new GridLayout());
		txtNote = new Text(noteGroup, SWT.BORDER | SWT.WRAP | SWT.MULTI);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(txtNote);
		if (addOrEditDialog == DIALOG_EDIT) {
			txtNote.setText(noteItem[3]);
		}
		txtNote.forceFocus();
		return tparent;
	}

	@Override
	protected void okPressed() {
		String strNote = txtNote.getText();
		if (strNote == null || strNote.equals("")) {
			MessageDialog.openInformation(getShell(), Messages.getString("dialog.AddOrUpdateNoteDialog.msgTitle"),
					Messages.getString("dialog.AddOrUpdateNoteDialog.msg"));
			return;
		}
		String systemUser = Activator.getDefault().getPreferenceStore().getString(IPreferenceConstants.SYSTEM_USER);
		String date = DateUtils.getStringDateShort();
		List<String> lstRowId = new ArrayList<String>();
//		Bug #2334:添加批注包含特殊字符保存失败
		strNote = TextUtil.cleanSpecialString(strNote);
		if (addOrEditDialog == DIALOG_ADD) {
			int index = cmbRange.getSelectionIndex();
			boolean blnIsApplyCurrent = false;
			if (index == 0) {
				lstRowId = xliffEditor.getSelectedRowIds();
				blnIsApplyCurrent = true;
			} else {
				lstRowId = xliffEditor.getXLFHandler().getRowIds();
			}
			Map<String, List<String>> tmpGroup = RowIdUtil.groupRowIdByFileName(lstRowId);
			xliffEditor.getXLFHandler().addNote(tmpGroup, DateUtils.getStringDateShort() + ":" + strNote,
					systemUser, blnIsApplyCurrent);
		} else if (addOrEditDialog == DIALOG_EDIT) {
			if (noteItem[4].equalsIgnoreCase(NatTableConstant.CURRENT_TEXT)) {
				lstRowId.add(xliffEditor.getSelectedRowIds().get(0));
			} else if (noteItem[4].equalsIgnoreCase(NatTableConstant.ALL_TEXT)) {
				lstRowId = xliffEditor.getXLFHandler().getRowIds();
			}
			StringBuffer oldNote = new StringBuffer();
			if (noteItem[2] != null && !noteItem[2].equals("")) {
				oldNote.append(noteItem[2]).append(":");
			}
			oldNote.append(noteItem[3]);
			Map<String, List<String>> tmpGroup = RowIdUtil.groupRowIdByFileName(lstRowId);
			StringBuffer sbText = new StringBuffer(date);
			sbText.append(":").append(strNote);
			xliffEditor.getXLFHandler().updateNote(tmpGroup, noteItem[1], oldNote.toString(), systemUser, sbText.toString());
		}
		close();
	}
}

package net.heartsome.cat.ts.ui.plugin.dialog;

import java.util.Vector;


import net.heartsome.cat.ts.ui.plugin.resource.Messages;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;

/**
 * TBXMaker -> 删除列对话框
 * @author peason
 * @version
 * @since JDK1.6
 */
public class ColumnRemoveDialog extends Dialog {

	/** 初始列的集合 */
	private Vector<String> allColumnVector;

	/** 删除列后剩余的列集合 */
	private Vector<String> columnVector;

	/** Logo 图片路径 */
	private String imgPath;

	/** 显示列的列表 */
	private List listColumn;

	/**
	 * 构造方法
	 * @param parentShell
	 * @param allColumnVector
	 * @param imgPath
	 */
	protected ColumnRemoveDialog(Shell parentShell, Vector<String> allColumnVector, String imgPath) {
		super(parentShell);
		this.allColumnVector = allColumnVector;
		this.imgPath = imgPath;
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(Messages.getString("dialog.ColumnRemoveDialog.title"));
		newShell.setImage(new Image(Display.getDefault(), imgPath));
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite tparent = (Composite) super.createDialogArea(parent);
		tparent.setLayout(new GridLayout());
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).hint(150, 200).grab(true, true).applyTo(tparent);

		listColumn = new List(tparent, SWT.BORDER | SWT.READ_ONLY | SWT.V_SCROLL | SWT.MULTI);
		listColumn.setLayoutData(new GridData(GridData.FILL_BOTH));
		for (int i = 0; i < allColumnVector.size(); i++) {
			listColumn.add(allColumnVector.get(i));
		}
		Button btnRemove = new Button(tparent, SWT.None);
		btnRemove.setText(Messages.getString("dialog.ColumnRemoveDialog.btnRemove"));
		GridDataFactory.swtDefaults().align(SWT.CENTER, SWT.CENTER).applyTo(btnRemove);
		btnRemove.addSelectionListener(new SelectionListener() {

			public void widgetSelected(SelectionEvent e) {
				int total = listColumn.getItemCount();
				int selCount = listColumn.getSelectionCount();
				if (selCount == 0) {
					MessageDialog.openInformation(getShell(), Messages.getString("dialog.ColumnRemoveDialog.msgTitle"),
							Messages.getString("dialog.ColumnRemoveDialog.msg1"));
					return;
				}
				if ((total - selCount) < 2) {
					MessageDialog.openInformation(getShell(), Messages.getString("dialog.ColumnRemoveDialog.msgTitle"),
							Messages.getString("dialog.ColumnRemoveDialog.msg2"));
					return;
				}
				listColumn.remove(listColumn.getSelectionIndices());
			}

			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});

		return tparent;
	}

	@Override
	protected boolean isResizable() {
		return true;
	}

	@Override
	protected void okPressed() {
		columnVector = new Vector<String>();
		String[] items = listColumn.getItems();
		for (int i = 0; i < items.length; i++) {
			columnVector.add(items[i]);
		}
		close();
	}

	public Vector<String> getColumnVector() {
		return columnVector;
	}

	public void setColumnVector(Vector<String> columnVector) {
		this.columnVector = columnVector;
	}

}

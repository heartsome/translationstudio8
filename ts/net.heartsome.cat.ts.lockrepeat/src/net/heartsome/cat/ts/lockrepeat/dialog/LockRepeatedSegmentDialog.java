package net.heartsome.cat.ts.lockrepeat.dialog;

import java.util.ArrayList;
import java.util.List;

import net.heartsome.cat.common.util.CommonFunction;
import net.heartsome.cat.ts.lockrepeat.Activator;
import net.heartsome.cat.ts.lockrepeat.resource.ImageConstant;
import net.heartsome.cat.ts.lockrepeat.resource.Messages;
import net.heartsome.cat.ts.ui.composite.DialogLogoCmp;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

/**
 * 锁定重复文本段对话框
 * @author weachy
 * @version
 * @since JDK1.5
 */
public class LockRepeatedSegmentDialog extends Dialog {

	/** XLIFF 文件 */
	private List<IFile> xliffFiles;

	/** 文件列表 */
	private TableViewer tableViewer;

	/** 对话框标题 */
	private String title;

	/** 锁定内部重复 */
	private Button btnLockInnerRepeat;

	/** 锁定外部100%匹配 */
	private Button btnLockTM100;

	/** 锁定外部101%匹配 */
	private Button btnLockTM101;

	private Image logoImage = Activator.getImageDescriptor(ImageConstant.TRANSLATE_LOCKREPEATED_LOGO).createImage();
	
	public LockRepeatedSegmentDialog(Shell parentShell, List<IFile> xliffFiles, String title) {
		super(parentShell);
		this.xliffFiles = xliffFiles;
		this.title = title;
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(title);
	}

	@Override
	protected boolean isResizable() {
		return false;
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		super.createButtonsForButtonBar(parent);

		Button btnOK = getButton(IDialogConstants.OK_ID);
		btnOK.setText(Messages.getString("dialog.LockRepeatedSegmentDialog.ok"));
		Button cancelBtn = getButton(IDialogConstants.CANCEL_ID);
		cancelBtn.setText(Messages.getString("dialog.LockRepeatedSegmentDialog.cancel"));
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite tparent = (Composite) super.createDialogArea(parent);
		GridData parentData = new GridData(SWT.FILL, SWT.FILL, true, true);
		parentData.widthHint = 600;
		parentData.heightHint = 350;
		tparent.setLayoutData(parentData);

		GridLayoutFactory.fillDefaults().extendedMargins(-1, -1, -1, 8).numColumns(1).applyTo(tparent);

		createLogoArea(tparent);
		createFileDataGroup(tparent);

		return parent;
	}

	/**
	 * 显示图片区
	 * @param parent
	 */
	public void createLogoArea(Composite parent) {
		new DialogLogoCmp(parent, SWT.NONE, title, Messages.getString("dialog.LockRepeatedSegmentResultDialog.desc"), logoImage);
	}

	/**
	 * @param parent
	 */
	public void createFileDataGroup(Composite parent) {

		Composite parentCmp = new Composite(parent, SWT.NONE);
		GridLayoutFactory.fillDefaults().extendedMargins(9, 9, 0, 0).numColumns(1).applyTo(parentCmp);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(parentCmp);

		tableViewer = new TableViewer(parentCmp, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI
				| SWT.FULL_SELECTION);

		final Table table = tableViewer.getTable();
		GridData tableData = new GridData(GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL | GridData.FILL_BOTH);
		tableData.heightHint = 50;

		table.setLayoutData(tableData);
		table.setLinesVisible(true);
		table.setHeaderVisible(true);

		String[] columnNames = new String[] { Messages.getString("dialog.LockRepeatedSegmentDialog.columnNames1"),
				Messages.getString("dialog.LockRepeatedSegmentDialog.columnNames2") };
		int[] columnAlignments = new int[] { SWT.LEFT, SWT.LEFT };
		for (int i = 0; i < columnNames.length; i++) {
			TableColumn tableColumn = new TableColumn(table, columnAlignments[i]);
			tableColumn.setText(columnNames[i]);
			tableColumn.setWidth(50);
		}

		tableViewer.setLabelProvider(new TableViewerLabelProvider());
		tableViewer.setContentProvider(new ArrayContentProvider());
		tableViewer.setInput(getTableData());
		// 让列表列宽动态变化
		table.addListener(SWT.Resize, new Listener() {
			public void handleEvent(Event event) {
				final Table table = ((Table) event.widget);
				final TableColumn[] columns = table.getColumns();
				event.widget.getDisplay().syncExec(new Runnable() {
					public void run() {
						double[] columnWidths = new double[] { 0.1, 0.8 };
						for (int i = 0; i < columns.length; i++)
							columns[i].setWidth((int) (table.getBounds().width * columnWidths[i]));
					}
				});
			}
		});
		tableViewer.getTable().setFocus();

		btnLockInnerRepeat = new Button(parentCmp, SWT.CHECK);
		btnLockInnerRepeat.setText(Messages.getString("dialog.LockRepeatedSegmentDialog.btnLockInnerRepeat"));
		btnLockInnerRepeat.setSelection(true);

		btnLockTM100 = new Button(parentCmp, SWT.CHECK);
		btnLockTM100.setText(Messages.getString("dialog.LockRepeatedSegmentDialog.btnLockTM100"));
		btnLockTM100.setSelection(true);

		if (CommonFunction.checkEdition("U")) {
			btnLockTM101 = new Button(parentCmp, SWT.CHECK);
			btnLockTM101.setText(Messages.getString("dialog.LockRepeatedSegmentDialog.btnLockTM101"));
			btnLockTM101.setSelection(true);
		}
	}

	@Override
	protected void okPressed() {
		lockInnerRepeatedSegment = btnLockInnerRepeat.getSelection();
		lockTM100Segment = btnLockTM100.getSelection();
		if (CommonFunction.checkEdition("U")) {
			lockTM101Segment = btnLockTM101.getSelection();
		} else {
			lockTM101Segment = false;
		}
		super.okPressed();
	}

	private boolean lockInnerRepeatedSegment;

	private boolean lockTM100Segment;

	private boolean lockTM101Segment;

	/**
	 * 是否锁定内部重复文本段
	 * @return ;
	 */
	public boolean isLockInnerRepeatedSegment() {
		return lockInnerRepeatedSegment;
	}

	/**
	 * 是否锁定记忆库完全匹配
	 * @return ;
	 */
	public boolean isLockTM100Segment() {
		return lockTM100Segment;
	}

	/**
	 * 是否锁定记忆库上下文匹配
	 * @return ;
	 */
	public boolean isLockTM101Segment() {
		return lockTM101Segment;
	}

	/**
	 * 获取tableViewer的填充内容
	 * @return
	 */
	public String[][] getTableData() {
		ArrayList<String[]> tableDataList = new ArrayList<String[]>();
		for (int i = 0; i < xliffFiles.size(); i++) {
			String[] tableInfo = new String[] { "" + (i + 1), xliffFiles.get(i).getFullPath().toOSString() };
			tableDataList.add(tableInfo);
		}
		return tableDataList.toArray(new String[][] {});
	}

	/**
	 * tableViewer的标签提供器
	 */
	class TableViewerLabelProvider extends LabelProvider implements ITableLabelProvider {
		public Image getColumnImage(Object element, int columnIndex) {
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
	
	@Override
	public boolean close() {
		if(logoImage != null && !logoImage.isDisposed()){
			logoImage.dispose();
		}
		return super.close();
	}

}

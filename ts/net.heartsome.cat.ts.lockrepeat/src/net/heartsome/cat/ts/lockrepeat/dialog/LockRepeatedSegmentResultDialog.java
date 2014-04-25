/**
 * PreTranslationResultDialog.java
 *
 * Version information :
 *
 * Date:Oct 20, 2011
 *
 * Copyright notice :
 * 本文件及其附带的相关文件包含机密信息，仅限瀚特盛科技有限公司指定的，与本项目有关的内部人员和客户联络人员查阅和使用。 
 * 如果您不是本保密声明中指定的收件者，请立即销毁本文件，禁止对本文件或根据本文件中的内容采取任何其他行动， 
 * 包括但不限于：泄露本文件中的信息、以任何方式制作本文件全部或部分内容之副本、将本文件或其相关副本提供给任何其他人。
 */
package net.heartsome.cat.ts.lockrepeat.dialog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import net.heartsome.cat.common.resources.ResourceUtils;
import net.heartsome.cat.ts.lockrepeat.resource.Messages;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

/**
 * 锁定重复文本段结果对话框
 * @author weachy
 * @version
 * @since JDK1.5
 */
public class LockRepeatedSegmentResultDialog extends Dialog {
	private TableViewer tableViewer;

	private List<IFile> filesPath;

	private Map<String, Integer> lockedInnerRepeatedResault;
	private Map<String, Integer> lockedFullMatchResult;
	private Map<String, Integer> lockedContextResult;
	private Map<String, Integer> tuNumResult;

	/**
	 * Create the dialog.
	 * @param parentShell
	 */
	public LockRepeatedSegmentResultDialog(Shell parentShell, List<IFile> filesPath) {
		super(parentShell);
		this.filesPath = filesPath;
		lockedInnerRepeatedResault = Collections.EMPTY_MAP;
		lockedFullMatchResult = Collections.EMPTY_MAP;
		lockedContextResult = Collections.EMPTY_MAP;
		tuNumResult = Collections.EMPTY_MAP;
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(Messages.getString("dialog.LockRepeatedSegmentResultDialog.title"));
	}

	/**
	 * Create contents of the dialog.
	 * @param parent
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		container.setLayout(new GridLayout(1, false));

		Composite composite = new Composite(container, SWT.NONE);
		GridLayout gl_composite = new GridLayout(1, false);
		gl_composite.verticalSpacing = 0;
		gl_composite.marginWidth = 0;
		gl_composite.marginHeight = 0;
		composite.setLayout(gl_composite);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		tableViewer = new TableViewer(composite, SWT.BORDER | SWT.FULL_SELECTION | SWT.V_SCROLL | SWT.H_SCROLL);
		Table table = tableViewer.getTable();

		GridData tableGd = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		tableGd.heightHint = 220;
		table.setLayoutData(tableGd);

		table.setLinesVisible(true);
		table.setHeaderVisible(true);

		String[] clmnTitles = new String[] { Messages.getString("dialog.LockRepeatedSegmentResultDialog.clmnTitles1"),
				Messages.getString("dialog.LockRepeatedSegmentResultDialog.clmnTitles2"),
				Messages.getString("dialog.LockRepeatedSegmentResultDialog.clmnTitles3"),
				Messages.getString("dialog.LockRepeatedSegmentResultDialog.clmnTitles4"),
				Messages.getString("dialog.LockRepeatedSegmentResultDialog.clmnTitles5"),
				Messages.getString("dialog.LockRepeatedSegmentResultDialog.clmnTitles6") };
		int[] clmnBounds = { 60, 200, 100, 110, 110, 110 };
		for (int i = 0; i < clmnTitles.length; i++) {
			createTableViewerColumn(tableViewer, clmnTitles[i], clmnBounds[i], i);
		}

		tableViewer.setLabelProvider(new TableViewerLabelProvider());
		tableViewer.setContentProvider(new ArrayContentProvider());
		tableViewer.setInput(this.getTableViewerInput());

		return container;
	}

	/**
	 * Create contents of the button bar.
	 * @param parent
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
	}

	/**
	 * Return the initial size of the dialog.
	 */
	@Override
	protected Point getInitialSize() {
		return getShell().computeSize(SWT.DEFAULT, SWT.DEFAULT);
	}

	private String[][] getTableViewerInput() {
		List<String[]> rows = new ArrayList<String[]>();
		int i = 1;
		for (IFile iFile : filesPath) {
			String workspacePath = iFile.getFullPath().toOSString();

			String xlfPath = ResourceUtils.iFileToOSPath(iFile);
			Object tmp = tuNumResult.get(xlfPath);
			String tuSum = tmp == null ? "0" : tmp.toString();
			tmp = lockedInnerRepeatedResault.get(xlfPath);
			String lockedInnerRepeatedNum = tmp == null ? "0" : tmp.toString();
			tmp = lockedFullMatchResult.get(xlfPath);
			String lockedFullMatchNum = tmp == null ? "0" : tmp.toString();
			tmp = lockedContextResult.get(xlfPath);
			String lockedContextMatchNum = tmp == null ? "0" : tmp.toString();
			String[] row = new String[] { String.valueOf(i++), workspacePath, tuSum, lockedInnerRepeatedNum,
					lockedContextMatchNum, lockedFullMatchNum };
			rows.add(row);
		}
		return rows.toArray(new String[][] {});
	}

	/**
	 * 设置TableViewer 列属性
	 * @param viewer
	 * @param title
	 *            列标题
	 * @param bound
	 *            列宽
	 * @param colNumber
	 *            列序号
	 * @return {@link TableViewerColumn};
	 */
	private TableViewerColumn createTableViewerColumn(TableViewer viewer, String title, int bound, final int colNumber) {
		final TableViewerColumn viewerColumn = new TableViewerColumn(viewer, SWT.NONE | SWT.Resize);
		final TableColumn column = viewerColumn.getColumn();
		column.setText(title);
		column.setWidth(bound);
		column.setResizable(true);
		column.setMoveable(true);
		return viewerColumn;
	}

	/**
	 * tableViewer的标签提供器
	 * @author Jason
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

	/**
	 * @param tableViewer
	 *            the tableViewer to set
	 */
	public void setTableViewer(TableViewer tableViewer) {
		this.tableViewer = tableViewer;
	}

	/**
	 * @param lockedInnerRepeatedResault
	 *            the lockedInnerRepeatedResault to set;
	 */
	public void setLockedInnerRepeatedResault(Map<String, Integer> lockedInnerRepeatedResault) {
		this.lockedInnerRepeatedResault = lockedInnerRepeatedResault;
	}

	/**
	 * @param lockedFullMatchResult
	 *            the lockedFullMatchResult to set
	 */
	public void setLockedFullMatchResult(Map<String, Integer> lockedFullMatchResult) {
		this.lockedFullMatchResult = lockedFullMatchResult;
	}

	/**
	 * @param lockedContextResult
	 *            the lockedContextResult to set
	 */
	public void setLockedContextResult(Map<String, Integer> lockedContextResult) {
		this.lockedContextResult = lockedContextResult;
	}

	/**
	 * @param tuNumResult
	 *            the tuNumResult to set
	 */
	public void setTuNumResult(Map<String, Integer> tuNumResult) {
		this.tuNumResult = tuNumResult;
	}

	public int getLockedContextResult(String filePath){
		if(null ==this.lockedContextResult || this.lockedContextResult.isEmpty() ){
			return -1;
		}
		return this.lockedContextResult.get(filePath);
	}
	
	
	public int getLockedFullMatchResult(String filePath){
		if(null ==this.lockedFullMatchResult || this.lockedFullMatchResult.isEmpty() ){
			return -1;
		}
		return this.lockedFullMatchResult.get(filePath);
	}
	
	public int getLockedInnerRepeatedResault(String filePath){
		if(null ==this.lockedInnerRepeatedResault || this.lockedInnerRepeatedResault.isEmpty()){
			return -1;
		}
		return this.lockedInnerRepeatedResault.get(filePath);
	}
}

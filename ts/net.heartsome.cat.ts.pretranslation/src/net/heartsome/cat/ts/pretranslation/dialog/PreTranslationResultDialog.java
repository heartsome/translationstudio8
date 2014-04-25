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
package net.heartsome.cat.ts.pretranslation.dialog;

import java.util.ArrayList;
import java.util.List;

import net.heartsome.cat.ts.pretranslation.bean.PreTranslationCounter;
import net.heartsome.cat.ts.pretranslation.resource.Messages;

import org.eclipse.core.resources.ResourcesPlugin;
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
 * 预翻译结果显示对话框
 * @author Jason
 * @version
 * @since JDK1.6
 */
public class PreTranslationResultDialog extends Dialog {
	private TableViewer tableViewer;
	List<PreTranslationCounter> preTransResult;

	/**
	 * Create the dialog.
	 * @param parentShell
	 */
	public PreTranslationResultDialog(Shell parentShell, List<PreTranslationCounter> preTransResult) {
		super(parentShell);
		this.preTransResult = preTransResult;
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(Messages.getString("dialog.PreTranslationResultDialog.title"));
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

		String[] clmnTitles = new String[] { Messages.getString("dialog.PreTranslationResultDialog.clmnTitles1"),
				Messages.getString("dialog.PreTranslationResultDialog.clmnTitles2"),
				Messages.getString("dialog.PreTranslationResultDialog.clmnTitles3"),
				Messages.getString("dialog.PreTranslationResultDialog.clmnTitles4"),
				Messages.getString("dialog.PreTranslationResultDialog.clmnTitles5"),
				Messages.getString("dialog.PreTranslationResultDialog.clmnTitles6") };
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
		String wPath = ResourcesPlugin.getWorkspace().getRoot().getLocation().toOSString();
		List<String[]> rows = new ArrayList<String[]>();
		int i = 1;
		for (PreTranslationCounter counter : preTransResult) {
			String iFilePath = counter.getCurrentFile().replace(wPath, "");
			String[] row = new String[] { (i++) + "", iFilePath, counter.getTuNumber() + "",
					counter.getTransTuCount() + "", counter.getLockedContextCount() + "",
					counter.getLockedFullCount() + "" };
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

	public boolean close() {
		this.preTransResult.clear();
		this.preTransResult = null;
		return super.close();
	}
}

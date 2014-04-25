package net.heartsome.cat.ts.ui.qa.dialogs;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import net.heartsome.cat.common.util.CommonFunction;
import net.heartsome.cat.ts.ui.util.PreferenceUtil;
import net.heartsome.cat.ts.ui.composite.DialogLogoCmp;
import net.heartsome.cat.ts.ui.dialog.HelpDialog;
import net.heartsome.cat.ts.ui.qa.Activator;
import net.heartsome.cat.ts.ui.qa.model.QAModel;
import net.heartsome.cat.ts.ui.qa.preference.QAPage;
import net.heartsome.cat.ts.ui.qa.resource.ImageConstant;
import net.heartsome.cat.ts.ui.qa.resource.Messages;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.PlatformUI;

/**
 * 品质检查显示框
 * @author robert
 * @version
 * @since JDK1.6
 */
public class BatchQADialog extends HelpDialog {
	/** 进入首选项设置的设置按钮 */
	private Button installBtn;
	private TableViewer tableViewer;
	private boolean isMultiFile;
	
	private QAModel model;

    private Image logoImage;
	
	public BatchQADialog(Shell parentShell, QAModel model, boolean isMultiFile) {
		super(parentShell);
		this.model = model;
		this.isMultiFile = isMultiFile;
		logoImage =  Activator.getImageDescriptor(ImageConstant.QA_DIALOG_LOGO).createImage();
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(Messages.getString("qa.all.qa"));

	}

	@Override
	protected boolean isResizable() {
		return true;
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		// ROBERTHELP 品质检查
		String language = CommonFunction.getSystemLanguage();
		final String helpUrl = MessageFormat.format(
				"/net.heartsome.cat.ts.ui.help/html/{0}/ch05s04.html#translation-qa-check", language);
		setHelpUrl(helpUrl);
		
		installBtn = createButton(parent, IDialogConstants.CLIENT_ID,
				Messages.getString("qa.dialogs.BatchQADialog.name1"), false);
		
		super.createButtonsForButtonBar(parent);
		Button okBtn = getButton(IDialogConstants.OK_ID);
		okBtn.setText(Messages.getString("qa.all.dialog.ok"));
		Button cancelBtn = getButton(IDialogConstants.CANCEL_ID);
		cancelBtn.setText(Messages.getString("qa.all.dialog.cancel"));
		initLister(parent.getShell());
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite tparent = (Composite) super.createDialogArea(parent);
		GridData parentData = new GridData(SWT.FILL, SWT.FILL, true, true);
		parentData.widthHint = 600;
		parentData.heightHint = 300;
		tparent.setLayoutData(parentData);

		GridLayoutFactory.fillDefaults().extendedMargins(-1, -1, -1, 8).numColumns(1).applyTo(tparent);

		createLogoArea(tparent);
		createFileDataGroup(tparent);
		tableViewer.getTable().setFocus();
		
		return tparent;
	}

	/**
	 * 显示图片区
	 * @param parent
	 */
	public void createLogoArea(Composite parent) {
		new DialogLogoCmp(parent, SWT.NONE, Messages.getString("qa.all.qa.batchQA"), Messages.getString("qa.BatchQADialog.desc"),logoImage);
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

		String[] columnNames = new String[] { Messages.getString("qa.all.dialog.index"),
				Messages.getString("qa.dialogs.BatchQADialog.name2") };
		int[] columnAlignments = new int[] { SWT.LEFT, SWT.LEFT };
		for (int i = 0; i < columnNames.length; i++) {
			TableColumn tableColumn = new TableColumn(table, columnAlignments[i]);
			tableColumn.setText(columnNames[i]);
			tableColumn.setWidth(50);
		}

		tableViewer.setLabelProvider(new TableViewerLabelProvider());
		tableViewer.setContentProvider(new ArrayContentProvider());
		tableViewer.setInput(getQATableInfo());
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
		
		//如果是合并打开的文件，那么创建一个 LABLE 进行提示
		if (isMultiFile) {
			Label remarkLbl = new Label(parentCmp, SWT.WRAP);
			remarkLbl.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			remarkLbl.setText(Messages.getString("qa.dialogs.BatchQADialog.tip1"));
		}
	}

	/**
	 * 获取tableViewer的填充内容
	 * @return
	 */
	public String[][] getQATableInfo() {
		List<IFile> qaXlfList = model.getQaXlfList();
		ArrayList<String[]> qaTableInfoList = new ArrayList<String[]>();
		for (int i = 0; i < qaXlfList.size(); i++) {
			String[] tableInfo = new String[] { "" + (i + 1), qaXlfList.get(i).getFullPath().toOSString() };
			qaTableInfoList.add(tableInfo);
		}
		return qaTableInfoList.toArray(new String[][] {});
	}

	public void initLister(final Shell shell) {
		installBtn.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				openPreference(shell);
			}

			public void widgetDefaultSelected(SelectionEvent e) {
				// openPreference(shell);
			}
		});
	}

	/**
	 * 打开首选项界面
	 * @param shell
	 */
	public void openPreference(Shell shell) {
		PreferenceUtil.openPreferenceDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow(), QAPage.ID);
	}

	/**
	 * tableViewer的标签提供器
	 * @author robert
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

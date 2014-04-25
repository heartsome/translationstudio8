package net.heartsome.cat.ts.ui.qa.dialogs;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import net.heartsome.cat.common.util.CommonFunction;
import net.heartsome.cat.ts.core.qa.FAModel;
import net.heartsome.cat.ts.core.qa.QAConstant;
import net.heartsome.cat.ts.ui.util.PreferenceUtil;
import net.heartsome.cat.ts.ui.composite.DialogLogoCmp;
import net.heartsome.cat.ts.ui.dialog.HelpDialog;
import net.heartsome.cat.ts.ui.qa.Activator;
import net.heartsome.cat.ts.ui.qa.preference.FileAnalysisInstalPage;
import net.heartsome.cat.ts.ui.qa.resource.ImageConstant;
import net.heartsome.cat.ts.ui.qa.resource.Messages;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
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
import org.eclipse.ui.PlatformUI;

/**
 * 文件分析的选择文件的显示框
 * @author robert 2011-12-8
 * @version
 * @since JDK1.6
 */
public class FileAnalysisDialog extends HelpDialog {
	
	private static final String LOCK_EXTER_101 = "net.heartsome.cat.ts.ui.qa.dialogs.FileAnalysisDialog.exter101Btn";
	
	private static final String LOCK_EXTER_100 = "net.heartsome.cat.ts.ui.qa.dialogs.FileAnalysisDialog.exter100Btn";
	
	private static final String LOCK_INTER_REPEAT = "net.heartsome.cat.ts.ui.qa.dialogs.FileAnalysisDialog.interRepeatBtn";
	
	private Image logoImage = Activator.getImageDescriptor(ImageConstant.QA_DIALOG_LOGO).createImage();
	
	/** 进入首选项设置的设置按钮 */
	private Button installBtn;
	private TableViewer tableViewer;
	private FAModel model;
	private String title;
	/** 本次文件分析是否是字数分析 */
	private boolean isWordsFa = false;
	private boolean isTransP = false;
	private boolean isEditP = false;
	/** 锁定外部101%匹配 */
	private Button exter101Btn;
	/** 锁定外部100%匹配 */
	private Button exter100Btn;
	/** 锁内部重复 */
	private Button interRepeatBtn;

	public FileAnalysisDialog(Shell parentShell, FAModel model, String title, String faItemId) {
		super(parentShell);
		this.model = model;
		this.title = title;
		// 如果是字数分析，那么就多出三个可选择的锁定属性
		if (QAConstant.FA_WORDS_ANALYSIS.equals(faItemId)) {
			isWordsFa = true;
		}else if (QAConstant.FA_EDITING_PROGRESS_ANALYSIS.equals(faItemId)) {
			isEditP = true;
		}else if (QAConstant.FA_TRANSLATION_PROGRESS_ANALYSIS.equals(faItemId)) {
			isTransP = true;
		}
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
	
	
//	@Override
//	protected Control createButtonBar(Composite parent) {
//			Composite parentCmp = new Composite(parent, SWT.BORDER);
//			GridDataFactory.fillDefaults().grab(true, false).applyTo(parentCmp);
//			GridLayoutFactory.swtDefaults().numColumns(2).equalWidth(false).applyTo(parentCmp);
//			
//			Composite helpCmp = new Composite(parentCmp, SWT.BORDER);
//			GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(helpCmp);
//			GridLayoutFactory.swtDefaults().numColumns(1).applyTo(helpCmp);
//			
//			ToolBar toolBar = new ToolBar(helpCmp, SWT.FLAT | SWT.CENTER);
//			GridDataFactory.fillDefaults().align(SWT.CENTER, SWT.CENTER).applyTo(toolBar);
//			ToolItem toolItem = new ToolItem(toolBar, SWT.NONE);
//			toolItem.setImage(getImage(DLG_IMG_HELP));
//			GridDataFactory.fillDefaults().align(SWT.CENTER, SWT.CENTER).applyTo(toolBar);
//			toolItem.addSelectionListener(new SelectionAdapter() {
//				@Override
//				public void widgetSelected(SelectionEvent e) {
//					PlatformUI.getWorkbench().getHelpSystem().displayHelpResource("/net.heartsome.cat.ts.ui.help/html/ch01.html#id1170974177259");
//				}
//			});
//			
//			toolItem.addListener(SWT.Selection, new Listener() {
//				
//				public void handleEvent(Event event) {
//					PlatformUI.getWorkbench().getHelpSystem().displayHelpResource("/net.heartsome.cat.ts.ui.help/html/ch01.html#id1170974177259");
//				}
//			});
//			
//			
//			Composite composite = new Composite(parentCmp, SWT.BORDER);
//			// create a layout with spacing and margins appropriate for the font
//			// size.
//			GridLayout layout = new GridLayout();
//			layout.numColumns = 0; // this is incremented by createButton
//			layout.makeColumnsEqualWidth = true;
//			layout.marginWidth = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
//			layout.marginHeight = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
//			layout.horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
//			layout.verticalSpacing = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
//			composite.setLayout(layout);
//			GridData data = new GridData(GridData.HORIZONTAL_ALIGN_END
//					| GridData.VERTICAL_ALIGN_CENTER);
//			composite.setLayoutData(data);
//			composite.setFont(parent.getFont());
//			
//			// Add the buttons to the button bar.
//			createButtonsForButtonBar(composite);
//			return composite;
//	}
	

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		String language = CommonFunction.getSystemLanguage();
		String helpUrl = "";
		if (isWordsFa) {
			// ROBERTHELP 字数分析
			helpUrl = MessageFormat.format(
					"/net.heartsome.cat.ts.ui.help/html/{0}/ch05s03.html#word-analysis", language);
			setHelpUrl(helpUrl);
		}else if (isTransP) {
			// ROBERTHELP 翻译进度分析
			helpUrl = MessageFormat.format(
					"/net.heartsome.cat.ts.ui.help/html/{0}/ch05s04.html#translation-progress-analysis", language);
			setHelpUrl(helpUrl);
		}else if (isEditP) {
			// ROBERTHELP 编辑进度分析
			helpUrl = MessageFormat.format(
					"/net.heartsome.cat.ts.ui.help/html/{0}/ch05s05.html#edit-progress-analysis", language);
			setHelpUrl(helpUrl);
		}
		
		if (isWordsFa) {
			installBtn = createButton(parent, IDialogConstants.CLIENT_ID,
					Messages.getString("qa.dialogs.FileAnalysisDialog.name1"), false);
			installBtn.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					openPreference();
				}
			});
		}
		
		super.createButtonsForButtonBar(parent);

		Button okBtn = getButton(IDialogConstants.OK_ID);
		okBtn.setText(Messages.getString("qa.all.dialog.ok"));
		Button cancelBtn = getButton(IDialogConstants.CANCEL_ID);
		cancelBtn.setText(Messages.getString("qa.all.dialog.cancel"));
	}



	/**
	 * 打开首选项，只针对字数分析
	 */
	public void openPreference() {
		PreferenceUtil.openPreferenceDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow(), FileAnalysisInstalPage.ID);		
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		
		
		Composite tparent = (Composite) super.createDialogArea(parent);
		GridData parentData = new GridData(SWT.FILL, SWT.FILL, true, true);
		int heigth = isWordsFa ? 350 : 300;
		parentData.widthHint = 600;
		parentData.heightHint = heigth;
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
		String title = null;
		String message = null;
		if (isWordsFa) {
			//  字数分析
			title = Messages.getString("qa.FileAnalysisDialog.WordsFaTitle");
			message = Messages.getString("qa.FileAnalysisDialog.WordsFaMsg");
		}else if (isTransP) {
			//  翻译进度分析
			title = Messages.getString("qa.FileAnalysisDialog.TransPTitle");
			message = Messages.getString("qa.FileAnalysisDialog.TransPMsg");
		}else if (isEditP) {
			title = Messages.getString("qa.FileAnalysisDialog.EditPTitle");
			message = Messages.getString("qa.FileAnalysisDialog.EditPMsg");
		}
		new DialogLogoCmp(parent, SWT.NONE, title, message, logoImage);
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
				Messages.getString("qa.dialogs.FileAnalysisDialog.xliffFile") };
		
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

		// 如果是字数分析
		if (isWordsFa) {
			IDialogSettings dialogSettings = Activator.getDefault().getDialogSettings();
			if (CommonFunction.checkEdition("U")) {
				exter101Btn = new Button(parentCmp, SWT.CHECK);
				exter101Btn.setText(Messages.getString("qa.dialogs.FileAnalysisDialog.lockExter101"));
				exter101Btn.setSelection(dialogSettings.getBoolean(LOCK_EXTER_101));
			}

			exter100Btn = new Button(parentCmp, SWT.CHECK);
			exter100Btn.setText(Messages.getString("qa.dialogs.FileAnalysisDialog.lockExter100"));

			interRepeatBtn = new Button(parentCmp, SWT.CHECK);
			interRepeatBtn.setText(Messages.getString("qa.dialogs.FileAnalysisDialog.lockInterRepeat"));
			
			exter100Btn.setSelection(dialogSettings.getBoolean(LOCK_EXTER_100));
			interRepeatBtn.setSelection(dialogSettings.getBoolean(LOCK_INTER_REPEAT));
		}
	}

	@Override
	protected void okPressed() {
		// 传入要锁定重复文本段的参数
		if (isWordsFa) {
			model.setLockExter101(CommonFunction.checkEdition("U") ? exter101Btn.getSelection() : false);
			model.setLockExter100(exter100Btn.getSelection());
			model.setLockInterRepeat(interRepeatBtn.getSelection());
			
			IDialogSettings dialogSettings = Activator.getDefault().getDialogSettings();
			dialogSettings.put(LOCK_EXTER_101, CommonFunction.checkEdition("U") ? exter101Btn.getSelection() : false);
			dialogSettings.put(LOCK_EXTER_100, exter100Btn.getSelection());
			dialogSettings.put(LOCK_INTER_REPEAT, interRepeatBtn.getSelection());
		}
		super.okPressed();
	}

	/**
	 * 获取tableViewer的填充内容
	 * @return
	 */
	public String[][] getQATableInfo() {
		List<IFile> qaXlfList = model.getAnalysisIFileList();
		ArrayList<String[]> qaTableInfoList = new ArrayList<String[]>();
		for (int i = 0; i < qaXlfList.size(); i++) {
			String[] tableInfo = new String[] { "" + (i + 1), qaXlfList.get(i).getFullPath().toOSString() };
			qaTableInfoList.add(tableInfo);
		}
		return qaTableInfoList.toArray(new String[][] {});
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

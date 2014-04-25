package net.heartsome.cat.ts.ui.term.dialog;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import net.heartsome.cat.common.core.exception.ImportException;
import net.heartsome.cat.common.locale.Language;
import net.heartsome.cat.common.util.CommonFunction;
import net.heartsome.cat.database.Constants;
import net.heartsome.cat.database.DBOperator;
import net.heartsome.cat.database.service.DatabaseService;
import net.heartsome.cat.ts.core.file.ProjectConfiger;
import net.heartsome.cat.ts.core.file.ProjectConfigerFactory;
import net.heartsome.cat.ts.core.file.XLFHandler;
import net.heartsome.cat.ts.tb.importer.TbImporter;
import net.heartsome.cat.ts.tb.importer.extension.ITbImporter;
import net.heartsome.cat.ts.ui.composite.LanguageLabelProvider;
import net.heartsome.cat.ts.ui.term.resource.Messages;
import net.heartsome.cat.ts.ui.term.view.TerminologyViewPart;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TrayDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.nebula.jface.tablecomboviewer.TableComboViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.PlatformUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ximpleware.NavException;
import com.ximpleware.XPathEvalException;
import com.ximpleware.XPathParseException;

/**
 * 添加术语到术语库对话框
 * @author peason
 * @version
 * @since JDK1.6
 */
public class AddTermToTBDialog extends TrayDialog {

	private static final Logger LOGGER = LoggerFactory.getLogger(LoggerFactory.class.getName());
	public static final int EDIT_TYPE = 0;
	public static final int ADD_TYPE = 1;

	private static int TYPE = -1;
	/** 源语言 */
	private String srcLang;

	/** 目标语言 */
	private String tgtLang;

	/** 源术语 */
	private String srcText;

	/** 目标术语 */
	private String tgtText;

	/** 选中行的源文本，用于术语匹配 */
	private String srcAllText;

	/** 文件所属的 project 对象 */
	private IProject project;

	/** 源语言下拉框 */
	private TableComboViewer cmbSrcLang;

	/** 目标语言下拉框 */
	private TableComboViewer cmbTgtLang;

	private static AddTermToTBDialog instance;

	/** 源术语文本框 */
	public Text txtSrc;

	/** 目标术语文本框 */
	public Text txtTgt;

	private Text txtProperty;
	
	
	private String propertyValue;

	public String getPropertyValue() {
		return this.propertyValue;
	}

	public void setPropertyValue(String propertyValue) {
		this.propertyValue = propertyValue;
	}

	/**
	 * 由于添加术语到术语库中的源术语和目标术语通过快捷键方式填充，因此使用单例模式， 保证填充术语时是对同一个对话框进行操作
	 * @param parentShell
	 * @param srcText
	 * @param tgtText
	 * @return ;
	 */
	public static synchronized AddTermToTBDialog getInstance(Shell parentShell, String srcText, String tgtText, int type) {
		if (instance == null) {
			instance = new AddTermToTBDialog(parentShell, srcText, tgtText);
		} else {
			// 重新选择源或目标后，对话框中相应的值也要改变
			instance.srcText = srcText;
			instance.tgtText = tgtText;
			instance.initProperty();
		}
		TYPE = type;
		return instance;
	}

	/**
	 * 私有构造方法
	 * @param parentShell
	 * @param srcText
	 *            源术语
	 * @param tgtText
	 *            目标术语
	 */
	private AddTermToTBDialog(Shell parentShell, String srcText, String tgtText) {
		super(parentShell);
		this.srcText = srcText;
		this.tgtText = tgtText;
		setShellStyle(getShellStyle() & ~SWT.APPLICATION_MODAL);
		setHelpAvailable(true);
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(Messages.getString("dialog.AddTermToTBDialog.title"));
	}

	/**
	 * 添加帮助按钮 robert 2012-09-06
	 */
	@Override
	protected Control createHelpControl(Composite parent) {
		// ROBERTHELP 添加术语
		String language = CommonFunction.getSystemLanguage();
		final String helpUrl = MessageFormat.format(
				"/net.heartsome.cat.ts.ui.help/html/{0}/ch05s04.html#add-terminology", language);
		Image helpImage = JFaceResources.getImage(DLG_IMG_HELP);
		ToolBar toolBar = new ToolBar(parent, SWT.FLAT | SWT.NO_FOCUS);
		((GridLayout) parent.getLayout()).numColumns++;
		toolBar.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_CENTER));
		final Cursor cursor = new Cursor(parent.getDisplay(), SWT.CURSOR_HAND);
		toolBar.setCursor(cursor);
		toolBar.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				cursor.dispose();
			}
		});
		ToolItem helpItem = new ToolItem(toolBar, SWT.NONE);
		helpItem.setImage(helpImage);
		helpItem.setToolTipText(JFaceResources.getString("helpToolTip")); //$NON-NLS-1$
		helpItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				PlatformUI.getWorkbench().getHelpSystem().displayHelpResource(helpUrl);
			}
		});
		return toolBar;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite tparent = (Composite) super.createDialogArea(parent);
		GridLayoutFactory.swtDefaults().extendedMargins(5, 5, 10, 0).numColumns(2).equalWidth(true).applyTo(tparent);
		GridData parentData = new GridData(GridData.FILL_BOTH);
		tparent.setLayoutData(parentData);

		Composite cmpTerm = new Composite(tparent, SWT.NONE);
		GridLayoutFactory.fillDefaults().numColumns(2).equalWidth(false).applyTo(cmpTerm);
		GridDataFactory.swtDefaults().applyTo(cmpTerm);
		Label lblSource = new Label(cmpTerm, SWT.NONE);
		lblSource.setText(Messages.getString("dialog.AddTermToTBDialog.lblSource"));
		GridDataFactory.fillDefaults().align(SWT.RIGHT, SWT.CENTER).applyTo(lblSource);
		txtSrc = new Text(cmpTerm, SWT.BORDER);
		GridData txtData = new GridData();
		// 解决在 Windows 下文本框高度太小的问题
		txtData.widthHint = 290;
		txtSrc.setLayoutData(txtData);
		Label lblTarget = new Label(cmpTerm, SWT.NONE);
		lblTarget.setText(Messages.getString("dialog.AddTermToTBDialog.lblTarget"));
		GridDataFactory.fillDefaults().align(SWT.RIGHT, SWT.CENTER).applyTo(lblTarget);
		txtTgt = new Text(cmpTerm, SWT.BORDER);
		txtTgt.setLayoutData(txtData);

		Composite cmpLang = new Composite(tparent, SWT.NONE);
		GridLayoutFactory.fillDefaults().numColumns(2).equalWidth(false).applyTo(cmpLang);
		GridDataFactory.fillDefaults().applyTo(cmpLang);
		Label lblSrcLang = new Label(cmpLang, SWT.NONE);
		lblSrcLang.setText(Messages.getString("dialog.AddTermToTBDialog.lblSrcLang"));
		GridDataFactory.fillDefaults().align(SWT.RIGHT, SWT.CENTER).applyTo(lblSrcLang);
		cmbSrcLang = new TableComboViewer(cmpLang, SWT.READ_ONLY | SWT.BORDER);
		cmbSrcLang.getTableCombo().setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		cmbSrcLang.setContentProvider(new ArrayContentProvider());

		Label lblTgtLang = new Label(cmpLang, SWT.NONE);
		lblTgtLang.setText(Messages.getString("dialog.AddTermToTBDialog.lblTgtLang"));
		GridDataFactory.fillDefaults().align(SWT.RIGHT, SWT.CENTER).applyTo(lblTgtLang);
		cmbTgtLang = new TableComboViewer(cmpLang, SWT.READ_ONLY | SWT.BORDER);
		cmbTgtLang.getTableCombo().setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		cmbTgtLang.setContentProvider(new ArrayContentProvider());
		Composite cmpProperty = new Composite(tparent, SWT.None);
		GridLayoutFactory.fillDefaults().numColumns(2).equalWidth(false).applyTo(cmpProperty);
		GridDataFactory.fillDefaults().span(2, 1).applyTo(cmpProperty);
		Label lblProperty = new Label(cmpProperty, SWT.None);
		lblProperty.setText(Messages.getString("dialog.AddTermToTBDialog.lblProperty"));
		GridDataFactory.fillDefaults().align(SWT.RIGHT, SWT.CENTER).applyTo(lblProperty);
		txtProperty = new Text(cmpProperty, SWT.BORDER);
		txtProperty.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		initProperty();
		tparent.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		return tparent;
	}

	/**
	 * 初始化各个控件的值 ;
	 */
	public void initProperty() {
		if (srcText != null && !srcText.equals("")) {
			txtSrc.setText(srcText);
			txtSrc.setSelection(0, srcText.length());
			txtSrc.setFocus();
		}
		if (tgtText != null && !tgtText.equals("")) {
			txtTgt.setText(tgtText);
			txtTgt.setSelection(0, tgtText.length());
			txtTgt.setFocus();
		}
		
         if(getPropertyValue()!=null){
        	 txtProperty.setText(getPropertyValue());
         }
		
		ProjectConfiger projectConfig = ProjectConfigerFactory.getProjectConfiger(project);
		List<Language> rsLstSrcLangs = new ArrayList<Language>();
		List<Language> rsLstTgtLangs = null;
		try {
			rsLstSrcLangs.add(projectConfig.getSourceLanguage());
			rsLstTgtLangs = projectConfig.getTargetlanguages();
		} catch (XPathParseException e) {
			LOGGER.error(Messages.getString("dialog.AddTermToTBDialog.logger"), e);
		} catch (NavException e) {
			LOGGER.error(Messages.getString("dialog.AddTermToTBDialog.logger"), e);
		} catch (XPathEvalException e) {
			LOGGER.error(Messages.getString("dialog.AddTermToTBDialog.logger"), e);
		}
		ArrayList<String> lstTgtLangs = new ArrayList<String>();
		if (rsLstTgtLangs != null) {
			for (Language lang : rsLstTgtLangs) {
				lstTgtLangs.add(lang.getCode());
			}
		}
		cmbSrcLang.setLabelProvider(new LanguageLabelProvider());
		cmbSrcLang.setInput(rsLstSrcLangs);
		cmbSrcLang.getTableCombo().select(0);
		cmbTgtLang.setLabelProvider(new LanguageLabelProvider());
		cmbTgtLang.setInput(rsLstTgtLangs);
		if (tgtLang != null) {
			cmbTgtLang.getTableCombo().select(lstTgtLangs.indexOf(tgtLang));
		}
	}

	/**
	 * 获取源语言
	 * @return ;
	 */
	public String getSrcLang() {
		return srcLang;
	}

	/**
	 * 设置源语言
	 * @param srcLang
	 *            源语言
	 */
	public void setSrcLang(String srcLang) {
		this.srcLang = srcLang;
	}

	/**
	 * 获取目标语言
	 * @return ;
	 */
	public String getTgtLang() {
		return tgtLang;
	}

	/**
	 * 设置目标语言
	 * @param tgtLang
	 *            目标语言
	 */
	public void setTgtLang(String tgtLang) {
		this.tgtLang = tgtLang;
	}

	/**
	 * 获取 IProject 对象
	 * @return ;
	 */
	public IProject getProject() {
		return project;
	}

	/**
	 * 设置 IProject 对象
	 * @param project
	 *            ;
	 */
	public void setProject(IProject project) {
		this.project = project;
	}

	/**
	 * 获取选中行的源文本
	 * @return ;
	 */
	public String getSrcAllText() {
		return srcAllText;
	}

	/**
	 * 设置选中行的源文本
	 * @param srcAllText
	 *            ;
	 */
	public void setSrcAllText(String srcAllText) {
		this.srcAllText = srcAllText;
	}

	@Override
	protected void okPressed() {
		String srcTerm = cleanString(txtSrc.getText());
		String tgtTerm = cleanString(txtTgt.getText());

		if (srcTerm == null || srcTerm.equals("")) {
			MessageDialog.openInformation(getShell(), Messages.getString("dialog.AddTermToTBDialog.msgTitle"),
					Messages.getString("dialog.AddTermToTBDialog.msg1"));
			return;
		}

		if (tgtTerm == null || tgtTerm.equals("")) {
			MessageDialog.openInformation(getShell(), Messages.getString("dialog.AddTermToTBDialog.msgTitle"),
					Messages.getString("dialog.AddTermToTBDialog.msg2"));
			return;
		}

		// 添加空格不可入库的判断，--robert 2012-11-19
		if (srcTerm.length() > 0 && srcTerm.trim().equals("")) {
			MessageDialog.openInformation(getShell(), Messages.getString("dialog.AddTermToTBDialog.msgTitle"),
					Messages.getString("dialog.AddTermToTBDialog.addTip1"));
			return;
		}
		if (tgtTerm.length() > 0 && tgtTerm.trim().equals("")) {
			MessageDialog.openInformation(getShell(), Messages.getString("dialog.AddTermToTBDialog.msgTitle"),
					Messages.getString("dialog.AddTermToTBDialog.addTip2"));
			return;
		}

		srcTerm = srcTerm.trim();
		tgtTerm = tgtTerm.trim();

		XLFHandler handler = new XLFHandler();
		IStructuredSelection srcSelected = (IStructuredSelection) cmbSrcLang.getSelection();
		Language srcSelectedLang = (Language) srcSelected.getFirstElement();
		String srcLang = srcSelectedLang.getCode();

		IStructuredSelection tgtSelection = (IStructuredSelection) cmbTgtLang.getSelection();
		Language tgtSelectedLang = (Language) tgtSelection.getFirstElement();
		String tgtLang = tgtSelectedLang.getCode();

		String strTBX = handler.generateTBXWithString(srcLang, tgtLang, srcTerm, tgtTerm, txtProperty.getText());
		TbImporter importer = TbImporter.getInstance();
		importer.setProject(project);
		int state = -1;
		try {
			if (TYPE == EDIT_TYPE) {
				if (null != this.dbOperator) {
					state = DatabaseService.importTbxWithString(strTBX, null, this.dbOperator,
							Constants.IMPORT_MODEL_ALWAYSADD, srcLang);
					if(state!=1){
						return;
					}
				}
			} else if (TYPE == ADD_TYPE) {
				state = importer.executeImport(strTBX, srcLang, null);
				if(state == ITbImporter.IMPORT_STATE_FAILED){
					MessageDialog.openInformation(Display.getCurrent().getActiveShell(),
							Messages.getString("dialog.AddTermToTBDialog.msgTitle"),
							Messages.getString("dialog.AddTermToTBDialog.msg3"));
					importer.clearResources();
					return;
				}
			}
		} catch (ImportException e) {
			final String msg = e.getMessage();
			Display.getDefault().syncExec(new Runnable() {

				public void run() {
					MessageDialog.openInformation(Display.getDefault().getActiveShell(),
							Messages.getString("dialog.AddTermToTBDialog.msgTitle"), msg);
				}
			});
			return;
		}
		
		importer.clearResources();

		TerminologyViewPart view = (TerminologyViewPart) PlatformUI.getWorkbench().getActiveWorkbenchWindow()
				.getActivePage().findView(TerminologyViewPart.ID);
		if (view != null) {
			view.refresh();
		}
		setReturnCode(OK);
		if (TYPE == ADD_TYPE) {
			try {
				project.refreshLocal(IResource.DEPTH_INFINITE, null);
			} catch (CoreException e) {
				LOGGER.error("", e);
			}
		}
		close();
	}

	private DBOperator dbOperator;

	public void setDbOperator(DBOperator dbOperator) {
		this.dbOperator = dbOperator;

	}

	/**
	 * 当关闭对话框时，单例对象要置空，否则再次打开对话框时会报异常
	 * @see org.eclipse.jface.dialogs.Dialog#close()
	 */
	@Override
	public boolean close() {
		boolean blnClose = super.close();
		instance = null;
		return blnClose;
	}

	public static String cleanString(String string) {
		string = string.replaceAll("&", "&amp;");
		string = string.replaceAll("<", "&lt;");
		string = string.replaceAll(">", "&gt;");
		// string = string.replaceAll("\"", "&quot;"); // 这里的 引号是不用转义的。--robert 2012-10-26
		return string;
	}
}
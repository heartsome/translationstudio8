/**
 * PreTranslationDialog.java
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

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.heartsome.cat.common.util.CommonFunction;
import net.heartsome.cat.ts.core.bean.XliffBean;
import net.heartsome.cat.ts.pretranslation.Activator;
import net.heartsome.cat.ts.pretranslation.bean.ImageConstants;
import net.heartsome.cat.ts.pretranslation.bean.PreTransParameters;
import net.heartsome.cat.ts.pretranslation.resource.Messages;
import net.heartsome.cat.ts.ui.composite.DialogLogoCmp;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.TrayDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.PlatformUI;

/**
 * 预翻译信息显示对话框,用于展示文件列表
 * @author Jason
 * @version
 * @since JDK1.6
 */
public class PreTranslationDialog extends TrayDialog {

	private TableViewer viewer;

	private Map<String, List<XliffBean>> xliffInofs;

	private PreTransParameters parameters = null;

	private Image logoImage = Activator.getImageDescriptor(ImageConstants.PRE_TRANSLTATION_LOGO).createImage();

	/** 忽略大小写 */
	private Button btnIgnoreCase;

	/** 忽略标记 */
	private Button btnIgnoretag;

	/** 最低匹配率 */
	private Spinner spinner;

	/** 完全匹配 */
	private Button btn101Match;

	/** 上下文匹配 */
	private Button btnContextMatch;

	/** 保留原来译文 */
	private Button btnKeepOld;

	/** 匹配率高于现有译文 */
	private Button btnKeepBestMatch;

	/** 始终覆盖现有译文 */
	private Button btnKeepNew;

	/** 不一致罚分. */
	private Spinner spinnnerPanalty;

	private Label lblTagPenalty;

	private IDialogSettings dialogSettings;

	/**
	 * Create the dialog.
	 * @param parentShell
	 * @param parameters
	 */
	public PreTranslationDialog(Shell parentShell, Map<String, List<XliffBean>> xliffInofs,
			PreTransParameters parameters) {
		super(parentShell);
		dialogSettings = Activator.getDefault().getDialogSettings();
		this.parameters = parameters;
		this.xliffInofs = xliffInofs;
		setHelpAvailable(true);
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(Messages.getString("dialog.PreTranslationDialog.title"));
	}

	/**
	 * 添加帮助按钮 robert 2012-09-06
	 */
	@Override
	protected Control createHelpControl(Composite parent) {
		// ROBERTHELP 预翻译
		String language = CommonFunction.getSystemLanguage();
		final String helpUrl = MessageFormat.format(
				"/net.heartsome.cat.ts.ui.help/html/{0}/ch05s03.html#pre-translation", language);
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

	/**
	 * Create contents of the dialog.
	 * @param parent
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		GridLayoutFactory.fillDefaults().extendedMargins(-1, -1, -1, 8).numColumns(1).applyTo(container);

		createLogoArea(container);

		Composite parentCmp = new Composite(parent, SWT.NONE);
		GridLayoutFactory.fillDefaults().extendedMargins(9, 9, 0, 0).numColumns(1).applyTo(parentCmp);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(parentCmp);

		createPageContent(parentCmp);

		viewer.getTable().setFocus();
		return container;
	}

	/**
	 * Return the initial size of the dialog.
	 */
	@Override
	protected Point getInitialSize() {
		return getShell().computeSize(SWT.DEFAULT, SWT.DEFAULT);
	}

	/**
	 * 创建页面内容
	 * @param parent
	 *            ;
	 */
	private void createPageContent(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout gl_composite = new GridLayout(1, false);
		gl_composite.marginHeight = 0;
		gl_composite.marginWidth = 0;
		gl_composite.verticalSpacing = 0;
		composite.setLayout(gl_composite);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		viewer = new TableViewer(composite, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI | SWT.FULL_SELECTION);
		final Table table = viewer.getTable();
		GridData tableGd = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		tableGd.heightHint = 220;
		table.setLayoutData(tableGd);

		table.setLinesVisible(true);
		table.setHeaderVisible(true);

		String[] clmnTitles = new String[] { Messages.getString("dialog.PreTranslationDialog.clmnTitles1"),
				Messages.getString("dialog.PreTranslationDialog.clmnTitles2"),
				Messages.getString("dialog.PreTranslationDialog.clmnTitles3"),
				Messages.getString("dialog.PreTranslationDialog.clmnTitles4") };
		int[] clmnBounds = { 80, 250, 100, 100 };
		for (int i = 0; i < clmnTitles.length; i++) {
			createTableViewerColumn(viewer, clmnTitles[i], clmnBounds[i], i);
		}

		viewer.setLabelProvider(new TableViewerLabelProvider());
		viewer.setContentProvider(new ArrayContentProvider());
		viewer.setInput(this.getTableViewerInput());

		// 参数面板
		Composite cmpPerTranParam = new Composite(composite, SWT.BORDER);
		GridDataFactory.createFrom(new GridData(GridData.FILL_BOTH)).applyTo(cmpPerTranParam);
		GridLayoutFactory.swtDefaults().numColumns(3).applyTo(cmpPerTranParam);

		// 预翻译参数
		Group groupMatch = new Group(cmpPerTranParam, SWT.NONE);
		GridDataFactory.createFrom(new GridData(GridData.FILL_BOTH)).applyTo(groupMatch);
		GridLayoutFactory.swtDefaults().applyTo(groupMatch);
		groupMatch.setText(Messages.getString("dialog.PreTranslationDialog.pertrans.match"));

		Composite cmpPercent = new Composite(groupMatch, SWT.NONE);
		GridDataFactory.createFrom(new GridData(GridData.FILL_BOTH)).applyTo(cmpPercent);
		GridLayoutFactory.fillDefaults().numColumns(3).applyTo(cmpPercent);
		Label lblLowest = new Label(cmpPercent, SWT.NONE);
		lblLowest.setText(Messages.getString("dialog.PreTranslationDialog.pertrans.lowestmatch"));
		spinner = new Spinner(cmpPercent, SWT.BORDER);
		spinner.setMinimum(1);
		spinner.setIncrement(5);
		spinner.setSelection(70);
		Label lblPercentage = new Label(cmpPercent, SWT.NONE);
		lblPercentage.setText("%");

		btnIgnoreCase = new Button(groupMatch, SWT.CHECK);
		btnIgnoreCase.setSelection(true);
		btnIgnoreCase.setText(Messages.getString("dialog.PreTranslationDialog.pertrans.ignorecase"));
		GridDataFactory.swtDefaults().align(SWT.LEFT, SWT.CENTER).span(2, 1).applyTo(btnIgnoreCase);

		btnIgnoretag = new Button(groupMatch, SWT.CHECK);
		GridDataFactory.swtDefaults().align(SWT.LEFT, SWT.CENTER).span(2, 1).applyTo(btnIgnoretag);
		btnIgnoretag.setSelection(true);
		btnIgnoretag.setText(Messages.getString("dialog.PreTranslationDialog.pertrans.ignoretag"));
		btnIgnoretag.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				setPanalty(!btnIgnoretag.getSelection());
			}
		});

		Composite cmpPenalty = new Composite(groupMatch, SWT.NONE);
		cmpPenalty.setLayoutData(new GridData(GridData.FILL_BOTH));
		GridLayoutFactory.fillDefaults().margins(15, 0).numColumns(2).applyTo(cmpPenalty);
		lblTagPenalty = new Label(cmpPenalty, SWT.NONE);
		lblTagPenalty.setText(Messages.getString("dialog.PreTranslationDialog.pertrans.tagPenalty"));
		spinnnerPanalty = new Spinner(cmpPenalty, SWT.BORDER);
		spinnnerPanalty.setSelection(2);
		setPanalty(false);

		// 锁定参数
		String lockGrpText = Messages.getString("dialog.PreTranslationDialog.pertrans.lock");
		Group groupLockWhenPerTrans = new Group(cmpPerTranParam, SWT.MULTI | SWT.WRAP);
		GridDataFactory.createFrom(new GridData(GridData.FILL_BOTH)).applyTo(groupLockWhenPerTrans);
		GridLayoutFactory.swtDefaults().numColumns(1).applyTo(groupLockWhenPerTrans);
		groupLockWhenPerTrans.setText(lockGrpText);

		btn101Match = new Button(groupLockWhenPerTrans, SWT.CHECK);
		btn101Match.setSelection(true);
		btn101Match.setText(Messages.getString("dialog.PreTranslationDialog.pertrans.fullMatch"));
		btnContextMatch = new Button(groupLockWhenPerTrans, SWT.CHECK);
		btnContextMatch.setSelection(true);
		btnContextMatch.setText(Messages.getString("dialog.PreTranslationDialog.pertrans.contextMatch"));
		
		Label lbl = new Label(groupLockWhenPerTrans, SWT.NONE);
		lbl.setText(lockGrpText);
		lbl.setVisible(false);

		// 覆盖策略
		Group groupHandleOldTarget = new Group(cmpPerTranParam, SWT.NONE);
		GridDataFactory.createFrom(new GridData(GridData.FILL_BOTH)).applyTo(groupHandleOldTarget);
		GridLayoutFactory.swtDefaults().numColumns(1).applyTo(groupHandleOldTarget);
		groupHandleOldTarget.setText(Messages.getString("dialog.PreTranslationDialog.pertrans.handleTargetText"));

		btnKeepOld = new Button(groupHandleOldTarget, SWT.RADIO);
		btnKeepOld.setText(Messages.getString("dialog.PreTranslationDialog.pertrans.keepOld"));

		btnKeepBestMatch = new Button(groupHandleOldTarget, SWT.RADIO);
		btnKeepBestMatch.setText(Messages.getString("dialog.PreTranslationDialog.pertrans.keepBestMatch"));

		btnKeepNew = new Button(groupHandleOldTarget, SWT.RADIO);
		btnKeepNew.setText(Messages.getString("dialog.PreTranslationDialog.pertrans.keepNew"));

		setDefaultValues();
	}

	/**
	 * 重设上次的状态;
	 */
	private void setDefaultValues() {

		if (dialogSettings.getBoolean("hasSetting")) {
			String tmp = null;
			tmp = dialogSettings.get("spinner");
			spinner.setSelection(tmp == null ? 70 : Integer.valueOf(tmp));

			btnIgnoreCase.setSelection(dialogSettings.getBoolean("btnIgnoreCase"));
			btnIgnoretag.setSelection(dialogSettings.getBoolean("btnIgnoretag"));
			setPanalty(!btnIgnoretag.getSelection());

			tmp = dialogSettings.get("spinnnerPanalty");
			spinnnerPanalty.setSelection(tmp == null ? 2 : Integer.valueOf(tmp));

			btn101Match.setSelection(dialogSettings.getBoolean("btn101Match"));
			btnContextMatch.setSelection(dialogSettings.getBoolean("btnContextMatch"));
			tmp = dialogSettings.get("updateStrategy");
			switch (tmp == null ? -11 : Integer.valueOf(tmp)) {
			case PreTransParameters.KEEP_BEST_MATCH_TARGET:
				btnKeepBestMatch.setSelection(true);
				break;
			case PreTransParameters.KEEP_NEW_TARGET:
				btnKeepNew.setSelection(true);
				break;
			case PreTransParameters.KEEP_OLD_TARGET:
				btnKeepOld.setSelection(true);
				break;
			default:
				btnKeepBestMatch.setSelection(true);
				break;
			}
		} else {
			btnIgnoreCase.setSelection(true);
			btnIgnoretag.setSelection(true);
			btn101Match.setSelection(true);
			btnContextMatch.setSelection(true);
			btnKeepBestMatch.setSelection(true);
			spinner.setSelection(70);
			spinnnerPanalty.setSelection(2);
		}
	}

	/**
	 * 从当前的数据库获取需要显示到界面上的数据
	 * @return ;
	 */
	private String[][] getTableViewerInput() {
		String wPath = ResourcesPlugin.getWorkspace().getRoot().getLocation().toOSString();
		Iterator<Entry<String, List<XliffBean>>> it = xliffInofs.entrySet().iterator();
		List<String[]> rows = new ArrayList<String[]>();
		int index = 1;
		while (it.hasNext()) {
			Entry<String, List<XliffBean>> entry = it.next();
			String filePath = entry.getKey();
			String iFilePath = filePath.replace(wPath, ""); // 获取到项目为根的路径

			List<XliffBean> xliffBeans = entry.getValue();

			// for (int i = 0; i < xliffBeans.size(); i++) {
			XliffBean xliffBean = xliffBeans.get(0);
			String srcLang = xliffBean.getSourceLanguage();
			String tagLang = xliffBean.getTargetLanguage();
			String[] rowValue = new String[] { (index++) + "", iFilePath, srcLang, tagLang };
			rows.add(rowValue);
			// }
		}
		return rows.toArray(new String[][] {});
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
	 * 显示图片区
	 * @param parent
	 */
	public void createLogoArea(Composite parent) {
		new DialogLogoCmp(parent, SWT.NONE, Messages.getString("dialog.PreTranslationDialog.logoTitle"),
				Messages.getString("dialog.PreTranslationDialog.desc"), logoImage);
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

	@Override
	protected void okPressed() {
		// 填充预翻译换参数策
		parameters.setIgnoreCase(btnIgnoreCase.getSelection());
		parameters.setIgnoreTag(btnIgnoretag.getSelection());
		if (spinner.getText().isEmpty()) {
			parameters.setLowestMatchPercent(0);
		} else {
			parameters.setLowestMatchPercent(Integer.valueOf(spinner.getText()));
		}
		if (spinnnerPanalty.getText().isEmpty()) {
			parameters.setPanalty(0);
		} else {
			parameters.setPanalty(Integer.valueOf(spinnnerPanalty.getText()));
		}
		parameters.setLockFullMatch(btn101Match.getSelection());
		parameters.setLockContextMatch(btnContextMatch.getSelection());
		if (btnKeepOld.getSelection()) {
			parameters.setUpdateStrategy(PreTransParameters.KEEP_OLD_TARGET);
		} else if (btnKeepBestMatch.getSelection()) {
			parameters.setUpdateStrategy(PreTransParameters.KEEP_BEST_MATCH_TARGET);
		} else if (btnKeepNew.getSelection()) {
			parameters.setUpdateStrategy(PreTransParameters.KEEP_NEW_TARGET);
		}

		dialogSettings.put("hasSetting", true);
		dialogSettings.put("btn101Match", parameters.isLockFullMatch());
		dialogSettings.put("btnContextMatch", parameters.isLockContextMatch());
		dialogSettings.put("btnIgnoreCase", parameters.getIgnoreCase());
		dialogSettings.put("btnIgnoretag", parameters.getIgnoreTag());
		dialogSettings.put("spinner", parameters.getLowestMatchPercent());
		dialogSettings.put("spinnnerPanalty", parameters.getPanalty());
		dialogSettings.put("updateStrategy", parameters.getUpdateStrategy());
		setReturnCode(OK);
		close();
	}

	@Override
	public boolean close() {
		if (logoImage != null && !logoImage.isDisposed()) {
			logoImage.dispose();
		}
		return super.close();
	}

	private void setPanalty(boolean enabled) {
		lblTagPenalty.setEnabled(enabled);
		spinnnerPanalty.setEnabled(enabled);
	}
}

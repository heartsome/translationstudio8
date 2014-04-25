package net.heartsome.cat.convert.ui.wizard;

import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.heartsome.cat.common.core.Constant;
import net.heartsome.cat.common.resources.ResourceUtils;
import net.heartsome.cat.convert.ui.Activator;
import net.heartsome.cat.convert.ui.model.ConversionConfigBean;
import net.heartsome.cat.convert.ui.model.ConverterUtil;
import net.heartsome.cat.convert.ui.model.ConverterViewModel;
import net.heartsome.cat.convert.ui.model.IConversionItem;
import net.heartsome.cat.convert.ui.resource.Messages;
import net.heartsome.cat.convert.ui.utils.ConversionResource;
import net.heartsome.cat.converter.Converter;
import net.heartsome.cat.converter.util.Progress;
import net.heartsome.cat.ts.core.file.DocumentPropertiesKeys;
import net.heartsome.cat.ts.core.file.XLFHandler;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.databinding.beans.BeansObservables;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.core.databinding.property.value.IValueProperty;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.databinding.viewers.ViewerSupport;
import org.eclipse.jface.databinding.wizard.WizardPageSupport;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.layout.LayoutConstants;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 项目逆向转换选项配置页
 * @author weachy
 * @since JDK1.5
 */
public class ReverseConversionWizardPage extends WizardPage {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ReverseConversionWizardPage.class);

	private List<ConverterViewModel> converterViewModels;

	/** 源文件编码列表 */
	private Combo tgtEncCombo;

	/** 如果已经存在，是否要替换 */
	private Button btnReplaceTarget;

	/** 文件列表 */
	private Table filesTable;

	private TableColumn lineNumberColumn;
	
	/** xliff 文件列 */
	private TableColumn xliffColumn;

	/** 目标编码列 */
	private TableColumn tgtEncColumn;

	/** 目标文件列 */
	private TableColumn targetColumn;

	private ArrayList<ConversionConfigBean> conversionConfigBeans;

	private TableViewer tableViewer;

	/**
	 * 正向项目转换配置信息页的构造函数
	 * @param pageName
	 */
	protected ReverseConversionWizardPage(String pageName) {
		super(pageName);
		setTitle(Messages.getString("wizard.ReverseConversionWizardPage.title"));
		setImageDescriptor(Activator.getImageDescriptor("images/dialog/xliff-totarget-logo.png"));
	}

	/**
	 * 初始化本页面需要的数据 ;
	 */
	private void initData() {
		ReverseConversionWizard wizard = (ReverseConversionWizard) getWizard();

		converterViewModels = wizard.getConverterViewModels();
		conversionConfigBeans = new ArrayList<ConversionConfigBean>();
		for (ConverterViewModel converterViewModel : converterViewModels) {
			IConversionItem conversionItem = converterViewModel.getConversionItem();
			String xliff = ResourceUtils.toWorkspacePath(conversionItem.getLocation());

			ConversionConfigBean bean = converterViewModel.getConfigBean();
			bean.setSource(xliff);
			conversionConfigBeans.add(bean);
		}
	}

	public void createControl(Composite parent) {
		initData(); // 先初始化本页面需要的数据

		Composite contents = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		contents.setLayout(layout);
		GridData gridData = new GridData();
		gridData.horizontalAlignment = SWT.FILL;
		gridData.grabExcessHorizontalSpace = true;
		contents.setLayoutData(gridData);

		createFilesGroup(contents); // 文件列表区域
		createPropertiesGroup(contents); // 源文件属性区域组
		createConversionOptionsGroup(contents); // 转换选项组

		bindValue(); // 数据绑定

		try {
			loadFiles(); // 加载文件列表
		} catch (Exception e) {
			e.printStackTrace();
		}

		filesTable.select(0); // 默认选中第一行数据
		filesTable.notifyListeners(SWT.Selection, null);

		Dialog.applyDialogFont(parent);

		Point defaultMargins = LayoutConstants.getMargins();
		GridLayoutFactory.fillDefaults().numColumns(1).margins(defaultMargins.x, defaultMargins.y)
				.generateLayout(contents);

		setControl(contents);

		validate();
	}

	private void createPropertiesGroup(Composite contents) {
		Group langComposite = new Group(contents, SWT.NONE);
		langComposite.setText(Messages.getString("wizard.ReverseConversionWizardPage.langComposite")); //$NON-NLS-1$
		langComposite.setLayout(new GridLayout(2, false));
		langComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Label tgtEncLabel = new Label(langComposite, SWT.NONE);
		tgtEncLabel.setText(Messages.getString("wizard.ReverseConversionWizardPage.tgtEncLabel")); //$NON-NLS-1$
		tgtEncCombo = new Combo(langComposite, SWT.DROP_DOWN | SWT.READ_ONLY);
		tgtEncCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		tgtEncCombo.addSelectionListener(new SelectionAdapter() {
			@SuppressWarnings("unchecked")
			public void widgetSelected(SelectionEvent arg0) {
				ISelection selection = tableViewer.getSelection();
				if (selection != null && !selection.isEmpty() && selection instanceof IStructuredSelection) {
					IStructuredSelection structuredSelection = (IStructuredSelection) selection;
					Iterator<ConversionConfigBean> iter = structuredSelection.iterator();
					while (iter.hasNext()) {
						ConversionConfigBean bean = iter.next();
						bean.setTargetEncoding(tgtEncCombo.getText());
					}

					validate();
				}
			}
		});
	}

	/**
	 * 转换选项组
	 * @param contents
	 *            ;
	 */
	private void createConversionOptionsGroup(Composite contents) {
		Group options = new Group(contents, SWT.NONE);
		options.setText(Messages.getString("wizard.ReverseConversionWizardPage.options"));
		options.setLayout(new GridLayout());
		options.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		btnReplaceTarget = new Button(options, SWT.CHECK);
		btnReplaceTarget.setText(Messages.getString("wizard.ReverseConversionWizardPage.btnReplaceTarget"));
		btnReplaceTarget.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		btnReplaceTarget.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				for (ConversionConfigBean conversionConfigBean : conversionConfigBeans) {
					conversionConfigBean.setReplaceTarget(btnReplaceTarget.getSelection());
				}

				validate();
			}
		});
	}

	/** XLFHandler 对象 */
	XLFHandler xlfHandler = new XLFHandler();

	/** 完成 */
	private boolean complete = false;

	/** 错误信息 */
	private String errorMessage = null;

	/**
	 * 验证方法 ;
	 */
	private void validate() {
		IRunnableWithProgress runnable = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				monitor.setTaskName(Messages.getString("wizard.ReverseConversionWizardPage.task1"));
				monitor.beginTask(Messages.getString("wizard.ReverseConversionWizardPage.task2"), converterViewModels.size());
				IStatus result = Status.OK_STATUS;
				int line = 1;
				for (ConverterViewModel converterViewModel : converterViewModels) {
					String xliff = converterViewModel.getConversionItem().getLocation().toOSString();
					IProgressMonitor subMonitor = Progress.getSubMonitor(monitor, 1);
					result = converterViewModel.validateXliffFile(xliff, xlfHandler, subMonitor);
					if (!result.isOK()) {
						break;
					}
					result = converterViewModel.validate();
					if (!result.isOK()) {
						break;
					}
					line++;
				}
				monitor.done();
				final IStatus validateResult = result;
				final int i = line;
				if (validateResult.isOK()) {
					complete = true;
					errorMessage = null;
				} else {
					complete = false;
					errorMessage = MessageFormat.format(Messages.getString("wizard.ReverseConversionWizardPage.msg1"), i) + validateResult.getMessage();
				}
			}
		};
		try {
			// 验证 xliff 文件比较耗时，需把他放到后台线程进行处理。
			// new ProgressMonitorDialog(shell).run(true, true, runnable);
			getContainer().run(true, true, runnable);

			setErrorMessage(errorMessage);
			setPageComplete(complete);
		} catch (InvocationTargetException e) {
			LOGGER.error("", e);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 创建文件列表区域
	 * @param contents
	 *            ;
	 */
	private Composite createFilesGroup(Composite contents) {
		Composite filesComposite = new Composite(contents, SWT.NONE);
		filesComposite.setLayout(new GridLayout(1, false));
		filesComposite.setLayoutData(new GridData(GridData.FILL_BOTH));

		filesTable = new Table(filesComposite, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI
				| SWT.FULL_SELECTION);

		GridData tableData = new GridData(GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL | GridData.FILL_BOTH);
		tableData.heightHint = 100;
		filesTable.setLayoutData(tableData);
		filesTable.setLinesVisible(true);
		filesTable.setHeaderVisible(true);

		filesTable.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				TableItem[] selected = filesTable.getSelection();
				if (selected.length == 0) {
					return;
				}

				String strTgtEnc = ""; //$NON-NLS-1$

				for (int i = 0; i < selected.length; i++) {
					String curTgtEnc = selected[i].getText(2);
					if (i == 0) {
						strTgtEnc = curTgtEnc;
					} else {
						if (!strTgtEnc.equals(curTgtEnc)) {
							strTgtEnc = ""; //$NON-NLS-1$
							break;
						}
					}
				}

				if (!"".equals(strTgtEnc)) { //$NON-NLS-1$
					tgtEncCombo.setText(strTgtEnc);
				} else {
					tgtEncCombo.deselectAll();
				}
			}
		});
		tableViewer = new TableViewer(filesTable);

		lineNumberColumn = new TableViewerColumn(tableViewer, SWT.NONE).getColumn();
		lineNumberColumn.setText(Messages.getString("wizard.ReverseConversionWizardPage.lineNumberColumn"));
		
		xliffColumn = new TableViewerColumn(tableViewer, SWT.NONE).getColumn();
		xliffColumn.setText(Messages.getString("wizard.ReverseConversionWizardPage.xliffColumn")); //$NON-NLS-1$

		tgtEncColumn = new TableViewerColumn(tableViewer, SWT.NONE).getColumn();
		tgtEncColumn.setText(Messages.getString("wizard.ReverseConversionWizardPage.tgtEncColumn")); //$NON-NLS-1$

		targetColumn = new TableViewerColumn(tableViewer, SWT.NONE).getColumn();
		targetColumn.setText(Messages.getString("wizard.ReverseConversionWizardPage.targetColumn")); //$NON-NLS-1$

		IValueProperty[] valueProperties = BeanProperties.values(ConversionConfigBean.class, new String[] {
				"index","source", "targetEncoding", "target" }); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		ViewerSupport.bind(tableViewer, new WritableList(conversionConfigBeans, ConversionConfigBean.class),
				valueProperties);

		filesComposite.addPaintListener(new PaintListener() {
			public void paintControl(PaintEvent arg0) {
				int width = filesTable.getClientArea().width;
				lineNumberColumn.setWidth(width * 1 / 10);
				targetColumn.setWidth(width * 4 / 10);
				tgtEncColumn.setWidth(width * 1 / 10);
				xliffColumn.setWidth(width * 4 / 10);
			}
		});
		return filesComposite;
	}

	/**
	 * 对 UI 和 View Model 进行绑定 ;
	 */
	private void bindValue() {
		DataBindingContext dbc = new DataBindingContext();
		WizardPageSupport.create(this, dbc);
		ConversionConfigBean configBean = conversionConfigBeans.get(0);

		// bind the target encoding
		dbc.bindList(SWTObservables.observeItems(tgtEncCombo), BeansObservables.observeList(configBean, "pageEncoding")); //$NON-NLS-1$
	}

	/**
	 * 加载文件数据。
	 */
	private void loadFiles() {
		ArrayList<String> xliffs = new ArrayList<String>(conversionConfigBeans.size());
		for (int i = 0; i < conversionConfigBeans.size(); i++) {
			String xliff = conversionConfigBeans.get(i).getSource();
			xliffs.add(ResourceUtils.toWorkspacePath(xliff));
		}
		for (int i = 0; i < conversionConfigBeans.size(); i++) {
			ConversionConfigBean bean = conversionConfigBeans.get(i);
			bean.setIndex((i+1)+"");
			
			// 逆转换时，source 其实是 XLIFF 文件。
			String xliff = bean.getSource();

			String local = ConverterUtil.toLocalPath(xliff);

			// 目标文件
			String target;
			try {
				ConversionResource resource = new ConversionResource(Converter.DIRECTION_REVERSE, local);
				target = resource.getTargetPath();
			} catch (CoreException e) {
				e.printStackTrace();
				target = ""; //$NON-NLS-1$
			}

			// 目标编码
			String tgtEncValue = getTgtEncoding(local);
			if (tgtEncValue == null) {
				tgtEncValue = ""; //$NON-NLS-1$
			}

			bean.setSource(xliff);
			bean.setTargetEncoding(tgtEncValue);
			bean.setTarget(target);
		}
	}

	/**
	 * 取得目标文件的编码
	 * @param xliffPath
	 * @return ;
	 */
	private String getTgtEncoding(String xliffPath) {
		XLFHandler handler = new XLFHandler();

		Map<String, Object> resultMap = handler.openFile(xliffPath);
		if (resultMap == null
				|| Constant.RETURNVALUE_RESULT_SUCCESSFUL != (Integer) resultMap.get(Constant.RETURNVALUE_RESULT)) {
			// 打开文件失败。
			return ""; //$NON-NLS-1$
		}
		List<HashMap<String, String>> documentInfo = handler.getDocumentInfo(xliffPath);
		if (documentInfo.size() > 0) {
			return documentInfo.get(0).get(DocumentPropertiesKeys.ENCODING);
		}
		return "";
	}

	@Override
	public void dispose() {
		super.dispose();
		if (filesTable != null) {
			filesTable.dispose();
		}
		if (conversionConfigBeans != null) {
			conversionConfigBeans.clear();
			conversionConfigBeans = null;
		}
		System.gc();
	}
}

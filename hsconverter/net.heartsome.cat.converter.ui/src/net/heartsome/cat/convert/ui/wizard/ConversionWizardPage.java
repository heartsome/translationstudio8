package net.heartsome.cat.convert.ui.wizard;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.heartsome.cat.common.locale.Language;
import net.heartsome.cat.convert.ui.dialog.FileDialogFactoryFacade;
import net.heartsome.cat.convert.ui.dialog.IConversionItemDialog;
import net.heartsome.cat.convert.ui.model.ConversionConfigBean;
import net.heartsome.cat.convert.ui.model.ConverterContext;
import net.heartsome.cat.convert.ui.model.ConverterUtil;
import net.heartsome.cat.convert.ui.model.ConverterViewModel;
import net.heartsome.cat.convert.ui.model.IConversionItem;
import net.heartsome.cat.convert.ui.utils.ConversionResource;
import net.heartsome.cat.convert.ui.utils.EncodingResolver;
import net.heartsome.cat.convert.ui.utils.FileFormatUtils;
import net.heartsome.cat.converter.Converter;
import net.heartsome.cat.converter.util.ConverterBean;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.databinding.beans.BeansObservables;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.core.databinding.property.value.IValueProperty;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.databinding.viewers.ViewerSupport;
import org.eclipse.jface.databinding.wizard.WizardPageSupport;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.layout.LayoutConstants;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.nebula.jface.tablecomboviewer.TableComboViewer;
import org.eclipse.nebula.widgets.tablecombo.TableCombo;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 项目正向转换选项配置页
 * @author weachy
 * @since JDK1.5
 */
public class ConversionWizardPage extends WizardPage {

	/** 支持的类型 */
	private final List<ConverterBean> supportTypes = FileFormatUtils.getSupportTypes();

	private List<ConverterViewModel> converterViewModels;

	/** 支持的格式列表 */
	private Combo formatCombo;

	/** 源文件编码列表 */
	private Combo srcEncCombo;

	/** 目标语言列表 */
	private TableComboViewer tgtLangComboViewer;

	/** 文件列表 */
	private Table filesTable;

	private TableColumn sourceColumn;
	private TableColumn formatColumn;
	private TableColumn srcEncColumn;
	private TableColumn xliffColumn;

	/** 分段选项 */
	private Text srxFile;

	private ArrayList<ConversionConfigBean> conversionConfigBeans;

	private TableViewer tableViewer;

	/**
	 * 正向项目转换配置信息页的构造函数
	 * @param pageName
	 */
	protected ConversionWizardPage(String pageName, List<ConverterViewModel> converterViewModels,
			ArrayList<ConversionConfigBean> conversionConfigBeans) {
		super(pageName);
		this.converterViewModels = converterViewModels;
		this.conversionConfigBeans = conversionConfigBeans;
		setTitle(Messages.getString("ConversionWizardPage.0")); //$NON-NLS-1$
		setDescription(Messages.getString("ConversionWizardPage.1")); //$NON-NLS-1$
	}

	public void createControl(Composite parent) {
		Composite contents = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		contents.setLayout(layout);
		GridData gridData = new GridData();
		gridData.horizontalAlignment = SWT.FILL;
		gridData.grabExcessHorizontalSpace = true;
		contents.setLayoutData(gridData);

		createFilesGroup(contents); // 文件列表区域
		createPropertiesGroup(contents);// 源文件属性区域组
		createConversionOptionsGroup(contents); // 转换选项组
		createSegmentationGroup(contents); // 分段规则选择区域组

		bindValue(); // 数据绑定

		loadFiles(); // 加载文件列表

		filesTable.select(0); // 默认选中第一行数据
		filesTable.notifyListeners(SWT.Selection, null);

		Dialog.applyDialogFont(parent);

		Point defaultMargins = LayoutConstants.getMargins();
		GridLayoutFactory.fillDefaults().numColumns(1).margins(defaultMargins.x, defaultMargins.y)
				.generateLayout(contents);

		setControl(contents);

		srxFile.setText(ConverterContext.defaultSrx);

		validate();
	}

	private void createPropertiesGroup(Composite contents) {
		Group langComposite = new Group(contents, SWT.NONE);
		langComposite.setText(Messages.getString("ConversionWizardPage.2")); //$NON-NLS-1$
		langComposite.setLayout(new GridLayout(2, false));
		langComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Label formatLabel = new Label(langComposite, SWT.NONE);
		formatLabel.setText(Messages.getString("ConversionWizardPage.3")); //$NON-NLS-1$

		formatCombo = new Combo(langComposite, SWT.READ_ONLY | SWT.DROP_DOWN);
		formatCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		formatCombo.addSelectionListener(new SelectionAdapter() {

			@SuppressWarnings("unchecked")
			public void widgetSelected(SelectionEvent arg0) {
				ISelection selection = tableViewer.getSelection();
				if (selection != null && !selection.isEmpty() && selection instanceof IStructuredSelection) {
					IStructuredSelection structuredSelection = (IStructuredSelection) selection;
					Iterator<ConversionConfigBean> iter = structuredSelection.iterator();
					while (iter.hasNext()) {
						ConversionConfigBean bean = iter.next();
						bean.setFileType(formatCombo.getText());
					}

					String format = getSelectedFormat(formatCombo.getText()); // 得到选中的文件类型
					int[] indices = filesTable.getSelectionIndices();
					for (int index : indices) {
						converterViewModels.get(index).setSelectedType(format);

						String sourcePath = converterViewModels.get(index).getConversionItem().getLocation()
								.toOSString();
						String sourceLocalPath = ConverterUtil.toLocalPath(sourcePath);
						String srcEncValue = EncodingResolver.getEncoding(sourceLocalPath, formatCombo.getText());
						if (srcEncValue != null) {
							conversionConfigBeans.get(index).setSrcEncoding(srcEncValue);
						}
					}

					validate();
				}
			}
		});

		Label srcEncLabel = new Label(langComposite, SWT.NONE);
		srcEncLabel.setText(Messages.getString("ConversionWizardPage.4")); //$NON-NLS-1$
		srcEncCombo = new Combo(langComposite, SWT.DROP_DOWN | SWT.READ_ONLY);
		srcEncCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		srcEncCombo.addSelectionListener(new SelectionAdapter() {
			@SuppressWarnings("unchecked")
			public void widgetSelected(SelectionEvent arg0) {
				ISelection selection = tableViewer.getSelection();
				if (selection != null && !selection.isEmpty() && selection instanceof IStructuredSelection) {
					IStructuredSelection structuredSelection = (IStructuredSelection) selection;
					Iterator<ConversionConfigBean> iter = structuredSelection.iterator();
					while (iter.hasNext()) {
						ConversionConfigBean bean = iter.next();
						bean.setSrcEncoding(srcEncCombo.getText());
					}

					validate();
				}
			}
		});

		// 目标语言框不需要纳入验证，可以不选择
		Label tgtLangLabel = new Label(langComposite, SWT.NONE);
		tgtLangLabel.setText("目标语言"); //$NON-NLS-1$
		tgtLangComboViewer = new TableComboViewer(langComposite, SWT.READ_ONLY|SWT.BORDER);	
		TableCombo tableCombo = tgtLangComboViewer.getTableCombo();
		// set options.
		tableCombo.setShowTableLines(false);
		tableCombo.setShowTableHeader(false);
		tableCombo.setDisplayColumnIndex(-1);
		tableCombo.setShowImageWithinSelection(true);
		tableCombo.setShowColorWithinSelection(false);
		tableCombo.setShowFontWithinSelection(false);
		tableCombo.setVisibleItemCount(20);		
		tableCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		tgtLangComboViewer.setLabelProvider(new LanguageLabelProvider(getShell()));
		tgtLangComboViewer.setContentProvider(new ArrayContentProvider());
		tgtLangComboViewer.setInput(conversionConfigBeans.get(0).getTgtLangList());
		tgtLangComboViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			
			public void selectionChanged(SelectionChangedEvent event) {
				ISelection selection = tableViewer.getSelection();
				if (selection != null && !selection.isEmpty() && selection instanceof IStructuredSelection) {
					IStructuredSelection structuredSelection = (IStructuredSelection) selection;
					@SuppressWarnings("unchecked")
					Iterator<ConversionConfigBean> iter = structuredSelection.iterator();
					while (iter.hasNext()) {
						ConversionConfigBean bean = iter.next();
						String langStr = tgtLangComboViewer.getTableCombo().getText();
						if (langStr != null) {
							for (Language lang : bean.getTgtLangList()) {
								if (lang.toString().equals(langStr)) {
									bean.setTgtLang(lang.getCode());
									break;
								}
							}
						}
					}
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
		options.setText(Messages.getString("ConversionWizardPage.5")); //$NON-NLS-1$
		options.setLayout(new GridLayout(2, false));
		options.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		/* ------- 转换选项 ------- */
		/* 是否按段落分段 */
		final Button segType = new Button(options, SWT.CHECK);
		segType.setText(Messages.getString("ConversionWizardPage.6")); //$NON-NLS-1$
		segType.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		segType.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				for (ConversionConfigBean conversionConfigBean : conversionConfigBeans) {
					conversionConfigBean.setSegByElement(segType.getSelection());
				}

				validate();
			}
		});

		/** 按 CR/LF 分段 */
		final Button useCRLF = new Button(options, SWT.CHECK);
		useCRLF.setText(Messages.getString("ConversionWizardPage.7")); //$NON-NLS-1$
		useCRLF.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		useCRLF.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				for (ConversionConfigBean conversionConfigBean : conversionConfigBeans) {
					conversionConfigBean.setBreakOnCRLF(useCRLF.getSelection());
				}

				validate();
			}
		});

		/* 是否将骨架嵌入 xliff 文件 */
		final Button embedSkl = new Button(options, SWT.CHECK);
		embedSkl.setText(Messages.getString("ConversionWizardPage.8")); //$NON-NLS-1$
		embedSkl.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		embedSkl.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				for (ConversionConfigBean conversionConfigBean : conversionConfigBeans) {
					conversionConfigBean.setEmbedSkl(embedSkl.getSelection());
				}

				validate();
			}
		});

		/* 如果已经存在，是否要替换 */
		final Button btnReplaceTarget = new Button(options, SWT.CHECK);
		btnReplaceTarget.setText(Messages.getString("ConversionWizardPage.9")); //$NON-NLS-1$
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

	/**
	 * 创建分段规则选择组
	 * @param contents
	 *            ;
	 */
	private void createSegmentationGroup(Composite contents) {
		Group segmentation = new Group(contents, SWT.NONE);
		segmentation.setText(Messages.getString("ConversionWizardPage.10")); //$NON-NLS-1$
		segmentation.setLayout(new GridLayout(3, false));
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.widthHint = 500;
		segmentation.setLayoutData(data);

		Label segLabel = new Label(segmentation, SWT.NONE);
		segLabel.setText(Messages.getString("ConversionWizardPage.11")); //$NON-NLS-1$

		srxFile = new Text(segmentation, SWT.BORDER | SWT.READ_ONLY);
		srxFile.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		srxFile.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				for (ConversionConfigBean conversionConfigBean : conversionConfigBeans) {
					conversionConfigBean.setInitSegmenter(srxFile.getText());
				}

				validate();
			}
		});

		final Button segBrowse = new Button(segmentation, SWT.PUSH);
		segBrowse.setText(Messages.getString("ConversionWizardPage.12")); //$NON-NLS-1$
		segBrowse.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent arg0) {
				IConversionItemDialog conversionItemDialog = FileDialogFactoryFacade.createFileDialog(getShell(),
						SWT.NONE);
				int result = conversionItemDialog.open();
				if (result == IDialogConstants.OK_ID) {
					IConversionItem conversionItem = conversionItemDialog.getConversionItem();
					srxFile.setText(conversionItem.getLocation().toOSString());
				}
			}
		});
	}

	/**
	 * 得到选中的类型
	 * @param description
	 *            描述名字
	 * @return 类型名字;
	 */
	private String getSelectedFormat(String description) {
		for (ConverterBean converterBean : supportTypes) {
			if (description.equals(converterBean.getDescription())) {
				return converterBean.getName();
			}
		}
		return ""; //$NON-NLS-1$
	}

	private void validate() {
		IStatus result = Status.OK_STATUS;
		int line = 1;
		for (ConverterViewModel converterViewModel : converterViewModels) {
			result = converterViewModel.validate();
			if (!result.isOK()) {
				break;
			}
			line++;
		}
		if (!result.isOK()) {
			setPageComplete(false);
			setErrorMessage(MessageFormat.format(Messages.getString("ConversionWizardPage.13"), line)
					+ result.getMessage());
		} else {
			setErrorMessage(null);
			setPageComplete(true);
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

				String strSrcFormat = ""; //$NON-NLS-1$
				String strSrcEnc = ""; //$NON-NLS-1$				
				for (int i = 0; i < selected.length; i++) {
					String curFormat = selected[i].getText(1);
					String curSrcEnc = selected[i].getText(2);

					if (i == 0) {
						strSrcFormat = curFormat;
						strSrcEnc = curSrcEnc;
					} else {
						if (!strSrcFormat.equals(curFormat)) {
							strSrcFormat = ""; //$NON-NLS-1$
						}

						if (!strSrcEnc.equals(curSrcEnc)) {
							strSrcEnc = ""; //$NON-NLS-1$
						}
					}
				}

				if (!"".equals(strSrcFormat)) { //$NON-NLS-1$
					formatCombo.setText(strSrcFormat);
				} else {
					formatCombo.deselectAll();
				}

				if (!"".equals(strSrcEnc)) { //$NON-NLS-1$
					srcEncCombo.setText(strSrcEnc);
				} else {
					srcEncCombo.deselectAll();
				}

				// 目标语言下拉框选中判断
				ISelection selection = tableViewer.getSelection();
				if (selection != null && !selection.isEmpty() && selection instanceof IStructuredSelection) {
					IStructuredSelection structuredSelection = (IStructuredSelection) selection;
					@SuppressWarnings("unchecked")
					Iterator<ConversionConfigBean> iter = structuredSelection.iterator();
					int i = 0;
					String tgtLang = "";
					while (iter.hasNext()) {
						ConversionConfigBean bean = iter.next();
						String currLang = bean.getTgtLang();
						for(Language lang :bean.getTgtLangList()){
							if(lang.getCode().equals(currLang)){
								currLang = lang.toString();
								break;
							}
						}
						if (i == 0) {
							tgtLang = currLang;
						}else {
							if(!tgtLang.equals(currLang)){
								tgtLang = "";
								break;
							}
						}
						i++;
					}
					if(!"".equals(tgtLang)){
						tgtLangComboViewer.getTableCombo().setText(tgtLang);
					}else{
						tgtLangComboViewer.getTableCombo().select(-1);
					}
				}
			}
		});
		tableViewer = new TableViewer(filesTable);

		sourceColumn = new TableViewerColumn(tableViewer, SWT.NONE).getColumn();
		sourceColumn.setText(Messages.getString("ConversionWizardPage.14")); //$NON-NLS-1$

		formatColumn = new TableViewerColumn(tableViewer, SWT.NONE).getColumn();
		formatColumn.setText(Messages.getString("ConversionWizardPage.15")); //$NON-NLS-1$

		srcEncColumn = new TableViewerColumn(tableViewer, SWT.NONE).getColumn();
		srcEncColumn.setText(Messages.getString("ConversionWizardPage.16")); //$NON-NLS-1$

		xliffColumn = new TableViewerColumn(tableViewer, SWT.NONE).getColumn();
		xliffColumn.setText(Messages.getString("ConversionWizardPage.17")); //$NON-NLS-1$

		IValueProperty[] valueProperties = BeanProperties.values(ConversionConfigBean.class, new String[] {
				"source", "fileType", "srcEncoding", "target" }); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ViewerSupport.bind(tableViewer, new WritableList(conversionConfigBeans, ConversionConfigBean.class),
				valueProperties);

		filesComposite.addPaintListener(new PaintListener() {
			public void paintControl(PaintEvent arg0) {
				int width = filesTable.getClientArea().width;
				sourceColumn.setWidth(width * 3 / 10);
				formatColumn.setWidth(width * 3 / 10);
				srcEncColumn.setWidth(width * 1 / 10);
				xliffColumn.setWidth(width * 3 / 10);
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

		// bind the format
		dbc.bindList(SWTObservables.observeItems(formatCombo), BeansObservables.observeList(configBean, "fileFormats")); //$NON-NLS-1$
		// final IObservableValue format = BeansObservables.observeValue(selectedModel, "selectedType");
		// dbc.bindValue(SWTObservables.observeSelection(formatCombo), format);

		// bind the source encoding
		dbc.bindList(SWTObservables.observeItems(srcEncCombo), BeansObservables.observeList(configBean, "pageEncoding")); //$NON-NLS-1$

	}

	/**
	 * 加载文件数据。
	 */
	private void loadFiles() {
		for (int i = 0; i < conversionConfigBeans.size(); i++) {
			ConversionConfigBean bean = conversionConfigBeans.get(i);

			String source = bean.getSource();
			String sourceLocalPath = ConverterUtil.toLocalPath(source);
			// 自动识别文件类型
			String format = FileFormatUtils.detectFormat(sourceLocalPath);
			if (format == null) {
				format = ""; //$NON-NLS-1$
			}
			// 自动分析源文件编码
			String srcEncValue = EncodingResolver.getEncoding(sourceLocalPath, format);
			if (srcEncValue == null) {
				srcEncValue = ""; //$NON-NLS-1$
			}

			// XLIFF 文件路径
			String xliff = ""; //$NON-NLS-1$
			// 骨架文件路径
			String skeleton = ""; //$NON-NLS-1$
			try {
				ConversionResource resource = new ConversionResource(Converter.DIRECTION_POSITIVE, sourceLocalPath);
				xliff = resource.getXliffPath();
				skeleton = resource.getSkeletonPath();
			} catch (CoreException e) {
				e.printStackTrace();
			}

			if (!"".equals(format)) { //$NON-NLS-1$
				String name = getSelectedFormat(format);
				if (name != null && !"".equals(name)) { //$NON-NLS-1$
					converterViewModels.get(i).setSelectedType(name); // 添加类型
				}
			}
			bean.setFileType(format);
			bean.setSrcEncoding(srcEncValue);
			bean.setTarget(xliff);
			bean.setSkeleton(ConverterUtil.toLocalPath(skeleton));
		}
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
	
	class LanguageLabelProvider extends LabelProvider {
		private Logger logger = LoggerFactory.getLogger(LanguageLabelProvider.class);
		private Map<String, Image> imageCache = new HashMap<String, Image>();
		private String bundlePath;
		private Shell shell;
		public LanguageLabelProvider(Shell shell) {
			try {
				bundlePath = FileLocator.toFileURL(Platform.getBundle("net.heartsome.cat.ts.ui").getEntry("")).getPath();
			} catch (IOException e) {
				logger.error("在转换器中获取插件路径出错，插件ID：net.heartsome.cat.ts.ui");
				e.printStackTrace();
			}
			this.shell = shell;
		}
		public Image getImage(Object element) {		
			if (element instanceof Language) {
				Language lang = (Language) element;
				String code = lang.getCode();
				String imagePath = lang.getImagePath();
				if (imagePath != null && !imagePath.equals("")) {
					imagePath = bundlePath + imagePath;
					Image image = new Image(shell.getDisplay(), imagePath);
					if (image != null) {
						ImageData data = image.getImageData().scaledTo(16, 12);
						image = new Image(shell.getDisplay(), data);

						// 销毁原来的图片
						Image im = imageCache.remove(code);
						if (im != null && !im.isDisposed()) {
							im.dispose();
						}

						// 添加新的图片
						imageCache.put(code, image);
						return image;
					}
				}
			}
			return null;
		}
		
		public void dispose(){
			for (String code : imageCache.keySet()) {
				Image im = imageCache.get(code);
				if (im != null && !im.isDisposed()) {
					im.dispose();
				}
			}
			imageCache.clear();
			super.dispose();
		}
	}
}

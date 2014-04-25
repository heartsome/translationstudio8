package net.heartsome.cat.convert.ui.wizard;

import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.heartsome.cat.common.core.Constant;
import net.heartsome.cat.common.locale.Language;
import net.heartsome.cat.common.resources.ResourceUtils;
import net.heartsome.cat.common.ui.dialog.FileFolderSelectionDialog;
import net.heartsome.cat.convert.ui.Activator;
import net.heartsome.cat.convert.ui.dialog.FileDialogFactoryFacade;
import net.heartsome.cat.convert.ui.dialog.IConversionItemDialog;
import net.heartsome.cat.convert.ui.model.ConversionConfigBean;
import net.heartsome.cat.convert.ui.model.ConverterContext;
import net.heartsome.cat.convert.ui.model.ConverterUtil;
import net.heartsome.cat.convert.ui.model.ConverterViewModel;
import net.heartsome.cat.convert.ui.model.IConversionItem;
import net.heartsome.cat.convert.ui.resource.Messages;
import net.heartsome.cat.convert.ui.utils.ConversionResource;
import net.heartsome.cat.convert.ui.utils.EncodingResolver;
import net.heartsome.cat.convert.ui.utils.FileFormatUtils;
import net.heartsome.cat.converter.Converter;
import net.heartsome.cat.converter.util.ConverterBean;
import net.heartsome.cat.ts.ui.composite.LanguageLabelProvider;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.databinding.beans.BeansObservables;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.core.databinding.property.value.IValueProperty;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.internal.filesystem.local.LocalFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.databinding.viewers.ViewerSupport;
import org.eclipse.jface.databinding.wizard.WizardPageSupport;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
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
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 项目正向转换选项配置页
 * @author weachy
 * @since JDK1.5
 */
@SuppressWarnings("restriction")
public class ConversionWizardPage extends WizardPage {

	private static final Logger LOGGER = LoggerFactory.getLogger(ConversionWizardPage.class);

	public static final String REPLACE_TARGET = "net.heartsome.cat.convert.ui.wizard.ConversionWizardPage.btnReplaceTarget";

	public static final String OPEN_PRE_TRANS = "net.heartsome.cat.convert.ui.wizard.ConversionWizardPage.btnOpenPreTrans";

	/** 支持的类型 */
	private final List<ConverterBean> supportTypes = FileFormatUtils.getSupportTypes();

	private List<ConverterViewModel> converterViewModels;

	private boolean isOpenPreTrans = false;

	private boolean isReplaceTarget = false;

	/** 支持的格式列表 */
	private Combo formatCombo;

	/** 源文件编码列表 */
	private Combo srcEncCombo;

	/** 目标语言列表 */
	private TableViewer tgtLangViewer;

	/** 文件列表 */
	private Table filesTable;

	private TableColumn lineNumColumn;
	private TableColumn sourceColumn;
	private TableColumn formatColumn;
	private TableColumn srcEncColumn;

	/** 分段选项 */
	private Text srxFile;

	private ArrayList<ConversionConfigBean> conversionConfigBeans;

	private TableViewer tableViewer;

	private IProject currentProject;

	private String srcLang;
	private List<Language> targetlanguage;

	/**
	 * 正向项目转换配置信息页的构造函数
	 * @param pageName
	 */
	protected ConversionWizardPage(String pageName, List<ConverterViewModel> converterViewModels,
			ArrayList<ConversionConfigBean> conversionConfigBeans, IProject currentProject) {
		super(pageName);
		this.converterViewModels = converterViewModels;
		this.conversionConfigBeans = conversionConfigBeans;
		this.currentProject = currentProject;
		setTitle(Messages.getString("wizard.ConversionWizardPage.title")); //$NON-NLS-1$
		setDescription(Messages.getString("wizard.ConversionWizardPage.description")); //$NON-NLS-1$
		setImageDescriptor(Activator.getImageDescriptor("images/dialog/source-toxliff-logo.png"));
		ConversionConfigBean b = conversionConfigBeans.get(0);
		srcLang = b.getSrcLang();
		targetlanguage = b.getTgtLangList();
	}

	public void createControl(Composite parent) {
		Composite contents = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		contents.setLayout(layout);
		contents.setLayoutData(new GridData(GridData.FILL_BOTH));

		createFilesGroup(contents); // 文件列表区域
		createPropertiesGroup(contents);// 源文件属性区域组
		createConversionOptionsGroup(contents); // 转换选项组
		createSegmentationGroup(contents); // 分段规则选择区域组

		bindValue(); // 数据绑定

		loadFiles(); // 加载文件列表

		filesTable.select(0); // 默认选中第一行数据
		filesTable.notifyListeners(SWT.Selection, null);

		Dialog.applyDialogFont(parent);

		setControl(contents);

		srxFile.setText(ConverterContext.defaultSrx);

		validate();
	}

	private void createPropertiesGroup(Composite contents) {
		Group langComposite = new Group(contents, SWT.NONE);
		langComposite.setText(Messages.getString("wizard.ConversionWizardPage.langComposite")); //$NON-NLS-1$
		langComposite.setLayout(new GridLayout(2, false));
		langComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Label formatLabel = new Label(langComposite, SWT.NONE);
		formatLabel.setText(Messages.getString("wizard.ConversionWizardPage.formatLabel")); //$NON-NLS-1$

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
		srcEncLabel.setText(Messages.getString("wizard.ConversionWizardPage.srcEncLabel")); //$NON-NLS-1$
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
	}

	/**
	 * 转换选项组
	 * @param contents
	 *            ;
	 */
	private void createConversionOptionsGroup(Composite contents) {
		Group options = new Group(contents, SWT.NONE);
		options.setText(Messages.getString("wizard.ConversionWizardPage.options")); //$NON-NLS-1$
		options.setLayout(new GridLayout(1, false));
		options.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		/* 如果已经存在，是否要替换 */
		final Button btnReplaceTarget = new Button(options, SWT.CHECK);
		btnReplaceTarget.setText(Messages.getString("wizard.ConversionWizardPage.btnReplaceTarget")); //$NON-NLS-1$
		btnReplaceTarget.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		btnReplaceTarget.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				isReplaceTarget = btnReplaceTarget.getSelection();
				for (ConversionConfigBean conversionConfigBean : conversionConfigBeans) {
					conversionConfigBean.setReplaceTarget(btnReplaceTarget.getSelection());
				}

				validate();
			}
		});

		final Button btnOpenPreTrans = new Button(options, SWT.CHECK);
		btnOpenPreTrans.setText(Messages.getString("wizard.ConversionWizardPage.btnOpenPreTrans"));
		btnOpenPreTrans.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		btnOpenPreTrans.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				isOpenPreTrans = btnOpenPreTrans.getSelection();
			}
		});

		Composite composite = new Composite(options, SWT.NONE);
		GridLayout gd = new GridLayout(1, false);
		gd.marginWidth = 0;
		gd.marginHeight = 0;
		composite.setLayout(gd);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

		Label tgtLangLbl = new Label(composite, SWT.NONE);
		tgtLangLbl.setText(Messages.getString("wizard.ConversionWizardPage.tgtLangLbl"));

		tgtLangViewer = new TableViewer(composite, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
		Table tgtLangTable = tgtLangViewer.getTable();
		GridData gdTgtLangTable = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gdTgtLangTable.heightHint = 80;
		tgtLangTable.setLayoutData(gdTgtLangTable);

		tgtLangViewer.setLabelProvider(new LanguageLabelProvider());
		tgtLangViewer.setContentProvider(new ArrayContentProvider());
		tgtLangViewer.setInput(conversionConfigBeans.get(0).getTgtLangList());
		tgtLangViewer.addSelectionChangedListener(new ISelectionChangedListener() {

			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection sel = (IStructuredSelection) event.getSelection();
				@SuppressWarnings("unchecked")
				List<Language> selectedList = sel.toList();
				for (ConversionConfigBean conversionConfigBean : conversionConfigBeans) {
					conversionConfigBean.setHasSelTgtLangList(selectedList);
				}

				validate();
			}
		});

		tgtLangViewer.getTable().select(0);

		IDialogSettings dialogSettings = Activator.getDefault().getDialogSettings();
		btnReplaceTarget.setSelection(dialogSettings.getBoolean(REPLACE_TARGET));
		btnOpenPreTrans.setSelection(dialogSettings.getBoolean(OPEN_PRE_TRANS));
		this.isOpenPreTrans = btnOpenPreTrans.getSelection();
		isReplaceTarget = btnReplaceTarget.getSelection();
		for (ConversionConfigBean conversionConfigBean : conversionConfigBeans) {
			conversionConfigBean.setReplaceTarget(isReplaceTarget);
		}
		validate();
	}

	/**
	 * 创建分段规则选择组
	 * @param contents
	 *            ;
	 */
	private void createSegmentationGroup(Composite contents) {
		Group segmentation = new Group(contents, SWT.NONE);
		segmentation.setText(Messages.getString("wizard.ConversionWizardPage.segmentation")); //$NON-NLS-1$
		segmentation.setLayout(new GridLayout(3, false));
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.widthHint = 500;
		segmentation.setLayoutData(data);

		Label segLabel = new Label(segmentation, SWT.NONE);
		segLabel.setText(Messages.getString("wizard.ConversionWizardPage.segLabel")); //$NON-NLS-1$

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
		segBrowse.setText(Messages.getString("wizard.ConversionWizardPage.segBrowse")); //$NON-NLS-1$
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
		if (conversionConfigBeans.size() == 0) {
			setPageComplete(false);
			return;
		}
		ConversionConfigBean cb = conversionConfigBeans.get(0);
		if (cb.getHasSelTgtLangList() == null || cb.getHasSelTgtLangList().size() == 0) {
			setPageComplete(false);
			setErrorMessage(Messages.getString("wizard.ConversionWizardPage.msg1"));
			return;
		}
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
			filesTable.setSelection(line - 1);
			filesTable.notifyListeners(SWT.Selection, null);
			setPageComplete(false);
			setErrorMessage(MessageFormat.format(Messages.getString("wizard.ConversionWizardPage.msg2"), line)
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
		GridLayout gd = new GridLayout(2, false);
		gd.marginWidth = 0;
		filesComposite.setLayout(gd);
		filesComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		filesTable = new Table(filesComposite, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI
				| SWT.FULL_SELECTION);

		GridData tableData = new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL);
		tableData.heightHint = 100;
		filesTable.setLayoutData(tableData);
		filesTable.setLinesVisible(true);
		filesTable.setHeaderVisible(true);

		filesTable.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				doSelectedEvent();
			}
		});
		tableViewer = new TableViewer(filesTable);

		lineNumColumn = new TableViewerColumn(tableViewer, SWT.NONE).getColumn();
		lineNumColumn.setText(Messages.getString("wizard.ConversionWizardPage.lineNumColumn"));

		sourceColumn = new TableViewerColumn(tableViewer, SWT.NONE).getColumn();
		sourceColumn.setText(Messages.getString("wizard.ConversionWizardPage.sourceColumn"));

		formatColumn = new TableViewerColumn(tableViewer, SWT.NONE).getColumn();
		formatColumn.setText(Messages.getString("wizard.ConversionWizardPage.formatColumn"));

		srcEncColumn = new TableViewerColumn(tableViewer, SWT.NONE).getColumn();
		srcEncColumn.setText(Messages.getString("wizard.ConversionWizardPage.srcEncColumn"));

		IValueProperty[] valueProperties = BeanProperties.values(ConversionConfigBean.class, new String[] { "index",
				"source", "fileType", "srcEncoding" });
		ViewerSupport.bind(tableViewer, new WritableList(conversionConfigBeans, ConversionConfigBean.class),
				valueProperties);

		filesComposite.addPaintListener(new PaintListener() {
			public void paintControl(PaintEvent arg0) {
				int width = filesTable.getClientArea().width;
				lineNumColumn.setWidth(width * 1 / 10);
				sourceColumn.setWidth(width * 5 / 10);
				formatColumn.setWidth(width * 3 / 10);
				srcEncColumn.setWidth(width * 1 / 10);
			}
		});

		Composite opComp = new Composite(filesComposite, SWT.NONE);
		GridLayout opCompGl = new GridLayout();
		opCompGl.marginWidth = 0;
		opCompGl.marginLeft = 0;
		opCompGl.marginTop = 0;
		opCompGl.marginHeight = 0;
		opComp.setLayout(opCompGl);

		GridData gd_opComp = new GridData();
		gd_opComp.verticalAlignment = SWT.TOP;
		opComp.setLayoutData(gd_opComp);

		Button addBt = new Button(opComp, SWT.NONE);
		addBt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		addBt.setText(Messages.getString("wizard.ConversionWizardPage.addbutton"));
		addBt.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				FileFolderSelectionDialog dialog = new FileFolderSelectionDialog(getShell(), true, IResource.FILE) {
					// 打开对话框时展开树形目录
					public void create() {
						super.create();
						super.getTreeViewer().expandAll();
					}
				};
				dialog.setMessage(Messages.getString("wizard.ConversionWizardPage.selectfiledialog.message"));
				dialog.setTitle(Messages.getString("wizard.ConversionWizardPage.selectfiledialog.title"));
				dialog.setDoubleClickSelects(true);
				try {
					dialog.setInput(EFS.getStore(ResourcesPlugin.getWorkspace().getRoot().getLocationURI()));
				} catch (CoreException e1) {
					LOGGER.error("", e1);
					e1.printStackTrace();
				}
				dialog.addFilter(new ViewerFilter() {
					@Override
					public boolean select(Viewer viewer, Object parentElement, Object element) {
						if (element instanceof LocalFile) {
							LocalFile folder = (LocalFile) element;
							if (folder.getName().equalsIgnoreCase(".hsConfig")
									|| folder.getName().equalsIgnoreCase(".metadata")) {
								return false;
							}
							String projectPath = currentProject.getLocation().toOSString();
							if (projectPath.equals(folder.toString())) {
								return true;
							}
							String xliffFolderPath = folder.toString();
							String path1 = projectPath + File.separator + Constant.FOLDER_SRC;
							if (xliffFolderPath.startsWith(path1)) {
								for (ConversionConfigBean bean : conversionConfigBeans) {
									if (xliffFolderPath.indexOf(bean.getSource()) != -1) {
										return false;
									}
								}
								return true;
							}
						}
						return false;
					}
				});

				dialog.create();
				dialog.open();
				if (dialog.getResult() != null) {
					Object[] selectFiles = dialog.getResult();
					for (Object selectedFile : selectFiles) {
						LocalFile folder = (LocalFile) selectedFile;
						ConverterViewModel model = new ConverterViewModel(Activator.getContext(),
								Converter.DIRECTION_POSITIVE);
						Object adapter = Platform.getAdapterManager().getAdapter(
								ResourcesPlugin.getWorkspace().getRoot()
										.getFileForLocation(Path.fromOSString(folder.toString())),
								IConversionItem.class);
						IConversionItem sourceItem = null;
						if (adapter instanceof IConversionItem) {
							sourceItem = (IConversionItem) adapter;
						}
						model.setConversionItem(sourceItem); // 记住所选择的文件

						ConversionConfigBean bean = model.getConfigBean();
						bean.setSource(ResourceUtils.toWorkspacePath(folder.toString())); // 初始化源文件路径
						bean.setSrcLang(srcLang); // 初始化源语言
						bean.setTgtLangList(targetlanguage);
						if (targetlanguage != null && targetlanguage.size() > 0) {
							List<Language> lang = new ArrayList<Language>();
							lang.add(targetlanguage.get(0));
							bean.setHasSelTgtLangList(lang);
						}
						bean.setReplaceTarget(isReplaceTarget);
						bean.setInitSegmenter(srxFile.getText());
						conversionConfigBeans.add(bean);
						converterViewModels.add(model);
					}
					loadFiles();
					validate();
					tableViewer.refresh();
				}
			}
		});

		Button removeBt = new Button(opComp, SWT.NONE);
		removeBt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		removeBt.setText(Messages.getString("wizard.ConversionWizardPage.removebutton"));
		removeBt.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (conversionConfigBeans.size() == 0) {
					return;
				}
				ISelection sel = tableViewer.getSelection();
				if (sel.isEmpty()) {
					MessageDialog.openError(getShell(),
							Messages.getString("wizard.ConversionWizardPage.removebutton.msg1.title"),
							Messages.getString("wizard.ConversionWizardPage.removebutton.msg1"));
					return;
				}
				if (sel instanceof IStructuredSelection) {
					IStructuredSelection ssel = (IStructuredSelection) sel;
					Object[] objs = ssel.toArray();
					int index = conversionConfigBeans.indexOf(ssel.getFirstElement());
					index -= ssel.size();
					index = index < 0 ? 0 : index;
					for (Object obj : objs) {
						int i = conversionConfigBeans.indexOf(obj);
						conversionConfigBeans.remove(i);
						converterViewModels.remove(i);
					}
					loadFiles();
					tableViewer.refresh();
					if (!conversionConfigBeans.isEmpty()) {
						tableViewer.getTable().select(index);
						doSelectedEvent();
					}
					validate();
				}
			}
		});

		return filesComposite;
	}

	private void doSelectedEvent() {
		TableItem[] selected = filesTable.getSelection();
		if (selected.length == 0) {
			return;
		}

		String strSrcFormat = ""; //$NON-NLS-1$
		String strSrcEnc = ""; //$NON-NLS-1$				
		for (int i = 0; i < selected.length; i++) {
			String curFormat = selected[i].getText(2);
			String curSrcEnc = selected[i].getText(3);

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
			bean.setIndex((i + 1) + "");
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

			bean.setEmbedSkl(FileFormatUtils.canEmbedSkl(format));

			// XLIFF 文件路径
			String xliff = ""; //$NON-NLS-1$
			// 骨架文件路径
			String skeleton = ""; //$NON-NLS-1$
			//XLiff 文件夹
			String xliffDir = "";
			try {
				ConversionResource resource = new ConversionResource(Converter.DIRECTION_POSITIVE, sourceLocalPath);
				xliff = resource.getXliffPath();
				skeleton = resource.getSkeletonPath();
				xliffDir = resource.getXliffDir();
			} catch (CoreException e) {
				LOGGER.error("", e);
			}

			if (!"".equals(format)) { //$NON-NLS-1$
				String name = getSelectedFormat(format);
				if (name != null && !"".equals(name)) { //$NON-NLS-1$
					converterViewModels.get(i).setSelectedType(name); // 添加类型
				}
			}
			bean.setFileType(format);
			bean.setSrcEncoding(srcEncValue);
			bean.setXliffDir(xliffDir);
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

	public boolean isOpenPreTrans() {
		return this.isOpenPreTrans;
	}

	public boolean isReplaceTarget() {
		return isReplaceTarget;
	}
}

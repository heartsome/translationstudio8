package net.heartsome.cat.ts.ui.preferencepage.languagecode;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.heartsome.cat.common.locale.Language;
import net.heartsome.cat.common.locale.LocaleService;
import net.heartsome.cat.common.resources.ResourceUtils;
import net.heartsome.cat.common.ui.HsImageLabel;
import net.heartsome.cat.ts.ui.Activator;
import net.heartsome.cat.ts.ui.resource.ImageConstant;
import net.heartsome.cat.ts.ui.resource.Messages;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferenceConstants;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.FilteredTree;
import org.eclipse.ui.dialogs.PatternFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TS 应用中语言代码设置的首选项页
 * @author cheney
 * @since JDK1.6
 */
public class LanguageCodesPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {
	public LanguageCodesPreferencePage() {
	}

	public static final Logger logger = LoggerFactory.getLogger(LanguageCodesPreferencePage.class);
	public static final String ID = "net.heartsome.cat.ts.ui.preferencepage.languagecode.LanguageCodesPreferencePage";

	// 标识当前操作类型
	private static final int EDIT = 1;
	private static final int REMOVE = 2;

	/**
	 * The number of items to show in the bindings table tree.
	 */
	private static final int LANGUAGE_CODE_COLUMN = 0;
	private static final int LANGUAGE_NAME_COLUMN = 1;
	private static final int LANGUAGE_BIDI_COLUMN = 2;
	private static int NUM_OF_COLUMNS = LANGUAGE_BIDI_COLUMN + 1;

	private Map<String, Image> imageCache = new HashMap<String, Image>();
	private Image isBidiImage = Activator.getImageDescriptor(ImageConstant.LANG_ISBIDI).createImage();
	private Button addBtn;
	private Button editBtn;
	private Button removeBtn;
	private FilteredTree fFilteredTree;

	// 数字格示显示数据的示例 Label
	private Label digitalValue;

	// 货币格示显示数据的示例 Label
	private Label currencyValue;

	// 时间格示显示数据的示例 Label
	private Label timeValue;

	// 短日期格示显示数据的示例 Label
	private Label shortDateValue;

	// 长日期格示显示数据的示例 Label
	private Label longDateValue;
	private LanguageModel languageModel;

	@Override
	protected Control createContents(Composite parent) {
		Composite page = initContents(parent);
		fill();
		// setProperty();
		addListener();
		return page;
	}

	// 对需要添加监听的控件添加相应的监听器
	private void addListener() {
		// 添加语言列表选择事件监听
		fFilteredTree.getViewer().addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				ISelection selection = event.getSelection();
				if (selection.isEmpty()) {
					return;
				}
				// 暂时注释
				// if (selection instanceof IStructuredSelection) {
				// refreshFormatControls((IStructuredSelection) selection);
				// }
			}
		});

		// 注册添加按钮的选择监听器
		addBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				addLanguage();
			}
		});

		// 注册编辑按钮的选择监听器
		editBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				verifyCurrentSelected(EDIT);
			}
		});

		// 注册删除按钮的选择监听器
		removeBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				verifyCurrentSelected(REMOVE);
			}
		});
	}

	// 验证当前的选择是否合法，如果合法，根据操作类型进行后续的操作
	protected void verifyCurrentSelected(int operateType) {
		ISelection selection = fFilteredTree.getViewer().getSelection();
		if (selection.isEmpty()) {
			String messagePattern = Messages.getString("languagecode.LanguageCodesPreferencePage.msg1");
			Object[] values = new String[0];
			if (operateType == EDIT) {
				values = new String[] { Messages.getString("languagecode.LanguageCodesPreferencePage.msg2") };
			} else if (operateType == REMOVE) {
				values = new String[] { Messages.getString("languagecode.LanguageCodesPreferencePage.msg3") };
			}
			String message = MessageFormat.format(messagePattern, values);
			MessageDialog.openInformation(fFilteredTree.getShell(),
					Messages.getString("languagecode.LanguageCodesPreferencePage.msgTitle"), message);
			return;
		} else if (operateType == EDIT && fFilteredTree.getViewer().getTree().getSelectionCount() > 1) {
			MessageDialog.openInformation(fFilteredTree.getShell(),
					Messages.getString("languagecode.LanguageCodesPreferencePage.msgTitle"),
					Messages.getString("languagecode.LanguageCodesPreferencePage.msg4"));
			return;
		}
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection iStructuredSelection = (IStructuredSelection) selection;
			Object object = iStructuredSelection.getFirstElement();
			if (object instanceof Language) {
				if (operateType == EDIT) {
					Language language = (Language) object;
					editLanguage(language);
				} else if (operateType == REMOVE) {
					// 删除所选择的语言
					if(MessageDialog.openConfirm(fFilteredTree.getShell(), Messages.getString("languagecode.LanguageCodesPreferencePage.msgTitle"), 
							Messages.getString("languagecode.LanguageCodesPreferencePage.msg5"))){
						removeLanguage(iStructuredSelection.toList());
					}
				}
			}
		}
	}

	// 删除当前选择的语言
	protected void removeLanguage(List<?> languages) {
		try {
			String bundlePath = FileLocator.toFileURL(Activator.getDefault().getBundle().getEntry("")).getPath();
			for (Object object : languages) {
				if (object instanceof Language) {
					Language lang = (Language) object;
					languageModel.removeLanguage(lang);
					LocaleService.getLanguageConfiger().deleteLanguageByCode(lang.getCode());
					String imgPath = lang.getImagePath();
					if (!imgPath.equals("")) {
						File file = new File(bundlePath + imgPath);
						if (file.exists()) {
							file.delete();
						}
					}
				}
			}
		} catch (IOException e) {
			logger.error(Messages.getString("languagecode.LanguageCodesPreferencePage.logger4"), e);
			e.printStackTrace();
		}
		// refresh the viewer
		Tree tree = fFilteredTree.getViewer().getTree();
		try {
			tree.setRedraw(false);
			fFilteredTree.getViewer().refresh();
		} finally {
			tree.setRedraw(true);
		}

	}

	// 编辑列表中所选择的语言
	protected void editLanguage(Language language) {
		String oldStrCode = language.getCode();
		String oldImagePath = language.getImagePath();
		AddOrUpdateLanguageDialog dialog = new AddOrUpdateLanguageDialog(fFilteredTree.getShell(),
				AddOrUpdateLanguageDialog.DIALOG_EDIT);
		dialog.setStrCode(oldStrCode);
		dialog.setStrName(language.getName());
		dialog.setBlnIsBidi(language.isBidi());
		dialog.setLanguageModel(languageModel);
		dialog.setImagePath(language.getImagePath());
		if (dialog.open() == IDialogConstants.OK_ID) {
			try {
				String bundlePath = FileLocator.toFileURL(Activator.getDefault().getBundle().getEntry("")).getPath();
				String newStrCode = dialog.getStrCode();
				String newImagePath = dialog.getImagePath();
				String resultImagePath = "";

				// ----处理图片开始
				if (newImagePath.equals("") && !oldImagePath.equals("")) {
					File oldImgFile = new File(bundlePath + oldImagePath);
					if (oldImgFile.exists()) {
						oldImgFile.delete();
						Image im = imageCache.remove(newStrCode);
						if (im != null && !im.isDisposed()) {
							im.dispose();
						}
					}
				} else {
					if (!oldImagePath.equals(newImagePath)) {
						if (!oldImagePath.equals("")) {
							File oldImgFile = new File(bundlePath + oldImagePath);
							if (oldImgFile.exists()) {
								oldImgFile.delete();
								Image im = imageCache.remove(newStrCode);
								if (im != null && !im.isDisposed()) {
									im.dispose();
								}
							}
						}
						// 复制新文件
						File newImgFile = new File(newImagePath);
						if (newImgFile.exists()) {
							resultImagePath = "images/lang/" + newStrCode + ".png";
							File rsImgFile = new File(bundlePath + resultImagePath);
							if (!rsImgFile.exists()) {
								rsImgFile.createNewFile();
							}
							ResourceUtils.copyFile(newImgFile, rsImgFile);
						}
					} else {
						resultImagePath = oldImagePath;
					}
				}// ----图片处理结束

				if (!oldStrCode.equals(newStrCode)) { // 修改过代码，需要删除原来的语言配置，重新写入新录入的
					LocaleService.getLanguageConfiger().deleteLanguageByCode(oldStrCode); // 删除配置文件
					languageModel.getLanguagesMap().remove(oldStrCode); // 删除界面中的数据
					language.setCode(newStrCode);
					language.setName(dialog.getStrName());
					language.setBidi(dialog.isBlnIsBidi());
					language.setImagePath(resultImagePath);
					LocaleService.getLanguageConfiger().addLanguage(language); // 新增到配置文件
					languageModel.getLanguagesMap().put(newStrCode, language); // 新增到界面
				} else {
					String newStrName = dialog.getStrName();
					boolean newIsBidi = dialog.isBlnIsBidi();
					// if (!newStrName.equals(oldStrName) || oldIsBidi != newIsBidi \) {
					language.setName(newStrName);
					language.setBidi(newIsBidi);
					language.setImagePath(resultImagePath);
					LocaleService.getLanguageConfiger().updateLanguageByCode(oldStrCode, language); // 更新配置文件
					languageModel.getLanguagesMap().put(oldStrCode, language); // 更新界面
					// }
				}
			} catch (IOException e) {
				logger.error(Messages.getString("languagecode.LanguageCodesPreferencePage.logger3"), e);
				e.printStackTrace();
			}
		}
		// 刷新界面
		Tree tree = fFilteredTree.getViewer().getTree();
		try {
			tree.setRedraw(false);
			fFilteredTree.getViewer().refresh();
		} finally {
			tree.setRedraw(true);
		}
	}

	// 添加新的语言
	protected void addLanguage() {
		AddOrUpdateLanguageDialog dialog = new AddOrUpdateLanguageDialog(fFilteredTree.getShell(),
				AddOrUpdateLanguageDialog.DIALOG_ADD);
		dialog.setLanguageModel(languageModel);
		if (dialog.open() == IDialogConstants.OK_ID) {
			String strCode = dialog.getStrCode();
			String imagePath = dialog.getImagePath();
			String resultImagePath = "";
			if (!imagePath.equals("")) {
				File imgFile = new File(imagePath);
				if (imgFile.exists()) {
					try {
						String bundlePath = FileLocator.toFileURL(Activator.getDefault().getBundle().getEntry(""))
								.getPath();
						String rsImagePath = bundlePath + "images/lang/" + strCode + ".png";
						File rsImgFile = new File(rsImagePath);
						if (!rsImgFile.exists()) {
							rsImgFile.createNewFile();
						}
						ResourceUtils.copyFile(imgFile, rsImgFile);
					} catch (IOException e) {
						logger.error(Messages.getString("languagecode.LanguageCodesPreferencePage.logger2"), e);
						e.printStackTrace();
					}
				}
				resultImagePath = "images/lang/" + strCode + ".png";
			}
			Language language = new Language(strCode, dialog.getStrName(), resultImagePath, dialog.isBlnIsBidi());
			LocaleService.getLanguageConfiger().addLanguage(language);
			languageModel.getLanguages().add(language);
			languageModel.getLanguagesMap().put(strCode, language);
		}
		Tree tree = fFilteredTree.getViewer().getTree();
		try {
			tree.setRedraw(false);
			fFilteredTree.getViewer().refresh();
		} finally {
			tree.setRedraw(true);
		}
	}

	/**
	 * 根据当前选择的语言，更新数字、货币等的显示示例
	 * @param selection
	 *            ;
	 */
	protected void refreshFormatControls(IStructuredSelection selection) {
		Object firstSelected = selection.getFirstElement();
		if (firstSelected instanceof Language) {
			Language language = (Language) firstSelected;
			digitalValue.setText(language.getName());
			digitalValue.setToolTipText(language.getName());

			currencyValue.setText(language.getName());
			currencyValue.setToolTipText(language.getName());

			timeValue.setText(language.getName());
			timeValue.setToolTipText(language.getName());

			shortDateValue.setText(language.getName());
			shortDateValue.setToolTipText(language.getName());

			longDateValue.setText(language.getName());
			longDateValue.setToolTipText(language.getName());
		}
	}

	private void fill() {
		fFilteredTree.getViewer().setInput(languageModel);
	}

	/**
	 * 构建界面显示控件
	 * @param parent
	 *            顶层容器 ;
	 */
	private Composite initContents(Composite parent) {

		final Composite page = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(1, false);
		layout.marginWidth = 0;
		page.setLayout(layout);

		// 不显示过滤文本框
		PlatformUI.getPreferenceStore().setDefault(IWorkbenchPreferenceConstants.SHOW_FILTERED_TEXTS, false);

		Group groupParent = new Group(page, SWT.None);
		groupParent.setLayout(new GridLayout());
		groupParent.setLayoutData(new GridData(GridData.FILL_BOTH));
		groupParent.setText(Messages.getString("languagecode.LanguageCodesPreferencePage.groupParent"));

		HsImageLabel imageLabel = new HsImageLabel(
				Messages.getString("languagecode.LanguageCodesPreferencePage.imageLabel"),
				Activator.getImageDescriptor(ImageConstant.PREFERENCE_SYS_LANG_CODE));
		Composite cmp = imageLabel.createControl(groupParent);
		cmp.setLayout(new GridLayout());
		Composite cmpTemp = (Composite) imageLabel.getControl();
		cmpTemp.setLayoutData(new GridData(GridData.FILL_BOTH));
		Composite cmpContent = new Composite(cmpTemp, SWT.None);
		cmpContent.setLayout(layout);
		GridData data = new GridData(GridData.FILL_BOTH);
		data.horizontalSpan = 2;
		cmpContent.setLayoutData(data);

		// 放置过滤文本框的容器
		createFilterControls(cmpContent);

		// 创建添加、删除按钮
		createLanguageControls(page);

		// 暂注释掉 创建与当前语言区域相关的数字、货币、时间、短日期、长日期的格式显示控件
		// createLocaleFormatControls(page);
		imageLabel.computeSize();
		// cmpContent.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		return page;
	}

	/**
	 * ========================暂注释掉 <br>
	 * 创建与当前语言区域相关的数字、货币、时间、短日期、长日期的格式显示控件
	 * @param parent
	 *            ;
	 */
	// private void createLocaleFormatControls(Composite parent) {
	// Composite formatControls = new Composite(parent, SWT.NONE);
	// formatControls.setLayout(new GridLayout(3, false));
	// formatControls.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	//
	// GridData gridData;
	//
	// // 标题
	// Label title = new Label(formatControls, SWT.NONE);
	// gridData = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
	// gridData.horizontalSpan = 3;
	// title.setLayoutData(gridData);
	// title.setText("使用此格式显示数据的示例：");
	// title.setToolTipText("使用此格式显示数据的示例");
	//
	// // 数字
	// Label digitalLabel = new Label(formatControls, SWT.NONE);
	// gridData = new GridData(SWT.BEGINNING);
	// digitalLabel.setLayoutData(gridData);
	// digitalLabel.setText("数字：");
	// digitalLabel.setToolTipText("数字");
	//
	// digitalValue = new Label(formatControls, SWT.CENTER);
	// gridData = new GridData(SWT.FILL, SWT.BEGINNING, true, false);
	// gridData.horizontalSpan = 2;
	// digitalValue.setLayoutData(gridData);
	//
	// // 货币
	// Label currencyLabel = new Label(formatControls, SWT.NONE);
	// gridData = new GridData(SWT.BEGINNING);
	// currencyLabel.setLayoutData(gridData);
	// currencyLabel.setText("货币：");
	// currencyLabel.setToolTipText("货币");
	//
	// currencyValue = new Label(formatControls, SWT.CENTER);
	// gridData = new GridData(SWT.FILL, SWT.BEGINNING, true, false);
	// gridData.horizontalSpan = 2;
	// currencyValue.setLayoutData(gridData);
	//
	// // 时间
	// Label timeLabel = new Label(formatControls, SWT.NONE);
	// gridData = new GridData(SWT.BEGINNING);
	// timeLabel.setLayoutData(gridData);
	// timeLabel.setText("时间：");
	// timeLabel.setToolTipText("时间");
	//
	// timeValue = new Label(formatControls, SWT.CENTER);
	// gridData = new GridData(SWT.FILL, SWT.BEGINNING, true, false);
	// gridData.horizontalSpan = 2;
	// timeValue.setLayoutData(gridData);
	//
	// // 短日期
	// Label shortDateLabel = new Label(formatControls, SWT.NONE);
	// gridData = new GridData(SWT.BEGINNING);
	// shortDateLabel.setLayoutData(gridData);
	// shortDateLabel.setText("短日期：");
	// shortDateLabel.setToolTipText("短日期");
	//
	// shortDateValue = new Label(formatControls, SWT.CENTER);
	// gridData = new GridData(SWT.FILL, SWT.BEGINNING, true, false);
	// gridData.horizontalSpan = 2;
	// shortDateValue.setLayoutData(gridData);
	//
	// // 长日期
	// Label longDateLabel = new Label(formatControls, SWT.NONE);
	// gridData = new GridData(SWT.BEGINNING);
	// longDateLabel.setLayoutData(gridData);
	// longDateLabel.setText("长日期：");
	// longDateLabel.setToolTipText("长日期");
	//
	// longDateValue = new Label(formatControls, SWT.CENTER);
	// gridData = new GridData(SWT.FILL, SWT.BEGINNING, true, false);
	// gridData.horizontalSpan = 2;
	// longDateValue.setLayoutData(gridData);
	//
	// Button customize = new Button(formatControls, SWT.BORDER);
	// gridData = new GridData(SWT.RIGHT, SWT.BEGINNING, true, false);
	// gridData.horizontalSpan = 3;
	// customize.setLayoutData(gridData);
	// customize.setText("自定义此格式……");
	// customize.setToolTipText("自定义此格式……");
	//
	// }

	/**
	 * 创建添加、删除按钮
	 * @param parent
	 *            项层容器 ;
	 */
	private void createLanguageControls(Composite parent) {
		Composite btnsComposite = new Composite(parent, SWT.NONE);
		btnsComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
		btnsComposite.setLayout(new GridLayout(3, true));

		addBtn = new Button(btnsComposite, SWT.NONE);
		GridData gd_addBtn = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
		addBtn.setLayoutData(gd_addBtn);
		addBtn.setText(Messages.getString("languagecode.LanguageCodesPreferencePage.addBtn"));
		// addBtn.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		editBtn = new Button(btnsComposite, SWT.NONE);
		GridData gd_editBtn = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
		editBtn.setLayoutData(gd_editBtn);
		editBtn.setText(Messages.getString("languagecode.LanguageCodesPreferencePage.editBtn"));
		// editBtn.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		removeBtn = new Button(btnsComposite, SWT.NONE);
		removeBtn.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		removeBtn.setText(Messages.getString("languagecode.LanguageCodesPreferencePage.removeBtn"));
		// removeBtn.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	}

	/**
	 * 创建包含过滤文本的 TreeViewer
	 * @param parent
	 *            顶层容器 ;
	 */
	private void createFilterControls(Composite parent) {

		fFilteredTree = new FilteredTree(parent, SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION, new PatternFilter(), true);

		GridData gridData;
		final GridLayout layout = new GridLayout(1, false);
		layout.marginWidth = 0;
		fFilteredTree.setLayout(layout);
		gridData = new GridData();
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalAlignment = SWT.FILL;
		gridData.verticalAlignment = SWT.FILL;
		fFilteredTree.setLayoutData(gridData);

		final TreeViewer viewer = fFilteredTree.getViewer();
		// 确保 filtered tree 有显示 ITEMS_TO_SHOW 的高度
		final Tree tree = viewer.getTree();
		tree.setHeaderVisible(true);
		tree.setLinesVisible(true);
		final Object layoutData = tree.getLayoutData();
		if (layoutData instanceof GridData) {
			gridData = (GridData) layoutData;
			gridData.heightHint = parent.getSize().y; // 适应大小
		}

		LanguageCodeComparator comparator = new LanguageCodeComparator();

		final TreeColumn codeColumn = new TreeColumn(tree, SWT.LEFT, LANGUAGE_CODE_COLUMN);
		codeColumn.setText(Messages.getString("languagecode.LanguageCodesPreferencePage.codeColumn"));
		tree.setSortColumn(codeColumn);
		tree.setSortColumn(codeColumn);
		tree.setSortDirection(SWT.UP);
		codeColumn.addSelectionListener(new ResortColumn(comparator, codeColumn, viewer, LANGUAGE_CODE_COLUMN));

		final TreeColumn nameColumn = new TreeColumn(tree, SWT.LEFT, LANGUAGE_NAME_COLUMN);
		nameColumn.setText(Messages.getString("languagecode.LanguageCodesPreferencePage.nameColumn"));
		nameColumn.addSelectionListener(new ResortColumn(comparator, nameColumn, viewer, LANGUAGE_NAME_COLUMN));

		final TreeColumn bidiColumn = new TreeColumn(tree, SWT.LEFT, LANGUAGE_BIDI_COLUMN);
		bidiColumn.setText(Messages.getString("languagecode.LanguageCodesPreferencePage.bidiColumn"));
		bidiColumn.addSelectionListener(new ResortColumn(comparator, bidiColumn, viewer, LANGUAGE_BIDI_COLUMN));

		viewer.setContentProvider(new LanguageCodeContentProvider());
		viewer.setLabelProvider(new LanguageCodeLabelProvider());
		viewer.setComparator(comparator);
		viewer.addDoubleClickListener(new IDoubleClickListener() {

			public void doubleClick(DoubleClickEvent event) {
				verifyCurrentSelected(EDIT);
			}
		});

		fFilteredTree.getPatternFilter().setIncludeLeadingWildcard(true);
		final TreeColumn[] columns = viewer.getTree().getColumns();

		columns[LANGUAGE_CODE_COLUMN].setWidth(100);
		columns[LANGUAGE_NAME_COLUMN].setWidth(240);
		columns[LANGUAGE_BIDI_COLUMN].setWidth(100);
	}

	public void init(IWorkbench workbench) {
		languageModel = new LanguageModel();
		noDefaultAndApplyButton();
	}

	@Override
	protected void performDefaults() {
		super.performDefaults();
	}

	@Override
	public boolean performOk() {
		// getPreferenceStore().setValue(IPreferenceConstants.LANGUAGECODE, "");
		return true;
	}

	private class LanguageCodeLabelProvider extends LabelProvider implements ITableLabelProvider {
	
		public Image getColumnImage(Object element, int columnIndex) {
			Language language = (Language) element;
			String code = language.getCode();
			if (columnIndex == 0) {
				String imagePath = language.getImagePath();
				if (imagePath != null && !imagePath.equals("")) {
					ImageDescriptor imageDesc = Activator.getImageDescriptor(imagePath);
					if (imageDesc != null) {
						ImageData data = imageDesc.getImageData().scaledTo(16, 12);
						Image image = new Image(getShell().getDisplay(), data);

						// 销毁原来的图片
						Image im = imageCache.put(code, image);
						if (im != null && !im.isDisposed()) {
							im.dispose();
						}
						
						return image;
					}
				}
			}
			if (columnIndex == 2) {
				if (language.isBidi()) {
					return isBidiImage;
				}
			}
			return null;

		}

		public String getColumnText(Object element, int columnIndex) {
			Language language = (Language) element;
			switch (columnIndex) {
			case 0:
				return language.getCode();
			case 1:
				return language.getName();
			default:
				return " ";
			}
		}
	}

	private class LanguageCodeContentProvider implements ITreeContentProvider {

		public Object[] getChildren(Object parentElement) {
			if (parentElement instanceof LanguageModel) {
				return ((LanguageModel) parentElement).getLanguages().toArray();
			}
			return new Object[0];
		}

		public Object getParent(Object element) {
			return null;
		}

		public boolean hasChildren(Object element) {
			return (element instanceof LanguageModel);
		}

		public Object[] getElements(Object inputElement) {
			return getChildren(inputElement);
		}

		public void dispose() {
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}

	}

	/**
	 * 语言列表比较器
	 * @author cheney
	 * @since JDK1.6
	 */
	private final class LanguageCodeComparator extends ViewerComparator {
		private LinkedList<Integer> sortColumns = new LinkedList<Integer>();
		private boolean ascending = true;

		/**
		 * 初始化语言列表比较器
		 */
		public LanguageCodeComparator() {
			for (int i = 0; i < NUM_OF_COLUMNS; i++) {
				sortColumns.add(new Integer(i));
			}
		}

		/**
		 * 排序的列索引
		 * @return ;
		 */
		public int getSortColumn() {
			return ((Integer) sortColumns.getFirst()).intValue();
		}

		/**
		 * 设置排序的列索引
		 * @param column
		 *            列索引;
		 */
		public void setSortColumn(int column) {
			if (column == getSortColumn()) {
				return;
			}
			Integer sortColumn = new Integer(column);
			sortColumns.remove(sortColumn);
			sortColumns.addFirst(sortColumn);
		}

		/**
		 * @return 是否升序排列.
		 */
		public boolean isAscending() {
			return ascending;
		}

		/**
		 * @param ascending
		 *            设置是否为升序排列.
		 */
		public void setAscending(boolean ascending) {
			this.ascending = ascending;
		}

		@Override
		public int compare(Viewer viewer, Object e1, Object e2) {
			int result = 0;
			Iterator<Integer> i = sortColumns.iterator();
			while (i.hasNext() && result == 0) {
				int column = i.next().intValue();
				result = compareColumn(viewer, e1, e2, column);
			}
			return ascending ? result : (-1) * result;
		}

		@SuppressWarnings("unchecked")
		private int compareColumn(final Viewer viewer, final Object a, final Object b, final int columnNumber) {
			IBaseLabelProvider baseLabel = ((TreeViewer) viewer).getLabelProvider();
			if (baseLabel instanceof ITableLabelProvider) {
				ITableLabelProvider tableProvider = (ITableLabelProvider) baseLabel;
				String e1p = tableProvider.getColumnText(a, columnNumber);
				String e2p = tableProvider.getColumnText(b, columnNumber);
				if (e1p != null && e2p != null) {
					return getComparator().compare(e1p, e2p);
				}
			}
			return 0;
		}
	}

	private final static class ResortColumn extends SelectionAdapter {
		private final LanguageCodeComparator comparator;
		private final TreeColumn treeColumn;
		private final TreeViewer viewer;
		private final int column;

		public ResortColumn(LanguageCodeComparator comparator, TreeColumn treeColumn, TreeViewer viewer, int column) {
			this.comparator = comparator;
			this.treeColumn = treeColumn;
			this.viewer = viewer;
			this.column = column;
		}

		@Override
		public void widgetSelected(SelectionEvent e) {
			if (comparator.getSortColumn() == column) {
				comparator.setAscending(!comparator.isAscending());
				viewer.getTree().setSortDirection(comparator.isAscending() ? SWT.UP : SWT.DOWN);
			} else {
				viewer.getTree().setSortColumn(treeColumn);
				comparator.setSortColumn(column);
			}
			try {
				viewer.getTree().setRedraw(false);
				viewer.refresh();
			} finally {
				viewer.getTree().setRedraw(true);
			}
		}
	}

	/**
	 * (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.DialogPage#dispose()
	 */
	public void dispose() {
		for (String code : imageCache.keySet()) {
			Image im = imageCache.get(code);
			if (im != null && !im.isDisposed()) {
				im.dispose();
			}
		}
		if (isBidiImage != null && !isBidiImage.isDisposed()) {
			isBidiImage.dispose();
		}
		imageCache.clear();
		super.dispose();
	}
}

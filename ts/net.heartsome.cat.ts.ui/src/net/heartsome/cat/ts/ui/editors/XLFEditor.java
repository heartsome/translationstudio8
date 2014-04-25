package net.heartsome.cat.ts.ui.editors;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Slider;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.EditorPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * XLFEditor XLIFF 文件编辑器组件。(此类未使用，因此未做国际化处理)
 */
public class XLFEditor extends EditorPart implements ISelectionProvider {

	private static final int ROWSOFPAGE = 30;

	private static final int STATUS_FLAG_HEIGHT = 6;

	private static final int STATUS_FLAG_WIDTH = 20;

	private static final int COLUMN_INDEX_WIDTH = 40;

	private static final int MINIMUM_ROW_HEIGHT = 25;

	/** 常量，编辑器ID。 */
	public static final String ID = "net.heartsome.cat.ts.ui.editors.XLFEditor";

	/** 常量，日志记录器。 */
	private static final Logger LOGGER = LoggerFactory.getLogger(XLFEditor.class);

	/**
	 * 编辑源文本状态标识。 取值 true 表示可以编辑源文本，取值 false 表示不可以编辑源文本。
	 */
	public boolean canEditSource = false;

	/**
	 * 编辑器模式。 取值 true 表示编辑模式，取值 false 表示选择模式。
	 */
	public boolean isEditModel = false;

	/**
	 * 编辑器当前布局模式。 取值 true 表示为水平布局模式，三列表格布局。源与目标组件采用水平布局方式放置。 取值 false 表示为垂直布局模式，二列表格布局。其中第二列再采用垂直布局方式，放置源与目标组件。
	 */
	public static boolean isHLayoutModel = true;

	// 编辑器左上角的选项卡图标。
	private Image titleImage;

	// 编辑器内部是否被改变状态标识符。
	private boolean isDirty;

	// 列首组件。
	private Label[] lblColumnHeader;

	// 编辑器滚动条
	private Slider editorVBar;

	// 列头容器
	private Composite colHeaderComposite;

	private Combo cmbFilter;

	private Text txtIndex;

	// 行索引组件集合
	private Label[] lblRowIndexs;

	// 行文本编辑器容器组件集合
	private Composite[] txtComposites;

	// 行状态标识组件容器集合
	private Composite[] statusComposites;

	private Composite bottomComposite;

	// 布局锁。
	private boolean layoutLock;

	private ScrolledComposite oScrolledComposite;

	private static final int HEIGHT_SPACE = 3;

	private static final int WIDTH_SPACE = 3;

	private static final String DATAKEY_INDEX = "index";

	private static final String DATAKEY_FOREGROUND = "FGColor";

	private static final String DATAKEY_BACKGROUND = "BGColor";

	private static Color selectedBgColor = null;

	private Composite[] tgtComposites = null;

	private Composite editorContentComposite;

	private MouseListener mouseListener;

	private KeyListener keyListener;

	private Cursor cursorArrow;

	private Cursor cursorIbeam;

	private ModifyListener textModifyListener;

	private MouseTrackListener mouseTrackListener;


	private int lastSelectedIndex = -1;

	private Color selectedFgColor;

	@Override
	public void doSave(IProgressMonitor monitor) {
		// TODO Auto-generated method stub

	}

	@Override
	public void doSaveAs() {
		// TODO Auto-generated method stub

	}

	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("init(IEditorSite site, IEditorInput input)");
		}
		setSite(site);
		setInput(input);

		// 设置Editor标题栏的显示名称，否则名称用plugin.xml中的name属性
		setPartName(input.getName());

		Image oldTitleImage = titleImage;
		if (input != null) {
			IEditorRegistry editorRegistry = PlatformUI.getWorkbench().getEditorRegistry();
			IEditorDescriptor editorDesc = editorRegistry.findEditor(getSite().getId());
			ImageDescriptor imageDesc = editorDesc != null ? editorDesc.getImageDescriptor() : null;
			titleImage = imageDesc != null ? imageDesc.createImage() : null;
		}

		setTitleImage(titleImage);
		if (oldTitleImage != null && !oldTitleImage.isDisposed()) {
			oldTitleImage.dispose();
		}

		getSite().setSelectionProvider(this);

		cursorIbeam = new Cursor(null, SWT.CURSOR_IBEAM);
		cursorArrow = new Cursor(null, SWT.CURSOR_ARROW);

		hookListener();
	}

	/**
	 * 获取指定容器下的源文本框和目标文本框组件。第一个为源文本框，第二个为目标文本框。
	 * @param parent
	 *            每行的 txtComposite
	 * @return ;
	 */
	private StyledText[] getTextWidgets(Composite parent) {
		StyledText[] sts = new StyledText[2];
		Control[] ctrls = parent.getChildren();
		for (Control ctrl : ctrls) {
			if (ctrl instanceof StyledText) {
				sts[0] = (StyledText) ctrl;
			}

			if (ctrl instanceof Composite) {
				Control[] children = ((Composite) ctrl).getChildren();
				for (Control child : children) {
					if (child instanceof StyledText) {
						sts[1] = (StyledText) child;

						return sts;
					}
				}
			}
		}

		return sts;
	}

	/**
	 * 设置选中行的颜色，并还原上一行的颜色。
	 */
	private void setSelectionRowColor(int inx) {
		// 重置上一行的颜色
		if (lastSelectedIndex >= 0 && lastSelectedIndex < txtComposites.length) {
			Color tmpColor = null;
			Object tmpObj = lblRowIndexs[lastSelectedIndex].getData(DATAKEY_BACKGROUND);
			if (tmpObj != null) {
				tmpColor = (Color) tmpObj;
			}
			lblRowIndexs[lastSelectedIndex].setBackground(tmpColor);
			tmpObj = lblRowIndexs[lastSelectedIndex].getData(DATAKEY_FOREGROUND);
			if (tmpObj != null) {
				tmpColor = (Color) tmpObj;
			}
			lblRowIndexs[lastSelectedIndex].setForeground(tmpColor);

			StyledText[] texts = getTextWidgets(txtComposites[lastSelectedIndex]);
			tmpObj = texts[0].getData(DATAKEY_FOREGROUND);
			if (tmpObj != null) {
				tmpColor = (Color) tmpObj;
			}
			texts[0].setForeground(tmpColor);
			tmpObj = texts[0].getData(DATAKEY_BACKGROUND);
			if (tmpObj != null) {
				tmpColor = (Color) tmpObj;
			}
			texts[0].setBackground(tmpColor);

			tmpObj = texts[1].getData(DATAKEY_FOREGROUND);
			if (tmpObj != null) {
				tmpColor = (Color) tmpObj;
			}
			texts[1].setForeground(tmpColor);
			tmpObj = texts[1].getData(DATAKEY_BACKGROUND);
			if (tmpObj != null) {
				tmpColor = (Color) tmpObj;
			}
			texts[1].setBackground(tmpColor);
		}

		// 设置选中的当前行颜色
		if (inx >= 0 && inx < txtComposites.length) {

			Color tmpColor = lblRowIndexs[inx].getBackground();
			lblRowIndexs[inx].setData(DATAKEY_BACKGROUND, tmpColor);
			lblRowIndexs[inx].setBackground(selectedBgColor);
			tmpColor = lblRowIndexs[inx].getForeground();
			lblRowIndexs[inx].setData(DATAKEY_FOREGROUND, tmpColor);
			lblRowIndexs[inx].setForeground(selectedFgColor);

			StyledText[] texts = getTextWidgets(txtComposites[inx]);
			tmpColor = texts[0].getForeground();
			texts[0].setData(DATAKEY_FOREGROUND, tmpColor);
			texts[0].setForeground(selectedFgColor);

			tmpColor = texts[0].getBackground();
			texts[0].setData(DATAKEY_BACKGROUND, tmpColor);
			texts[0].setBackground(selectedBgColor);

			tmpColor = texts[1].getForeground();
			texts[1].setData(DATAKEY_FOREGROUND, tmpColor);
			texts[1].setForeground(selectedFgColor);

			tmpColor = texts[1].getBackground();
			texts[1].setData(DATAKEY_BACKGROUND, tmpColor);
			texts[1].setBackground(selectedBgColor);

			lastSelectedIndex = inx;
		}
	}

	/**
	 * 初始化监听器。
	 */
	private void hookListener() {

		// 添加文本修改监听器，用于调整文本框大小，使其高度自适应文本内容。
		textModifyListener = new ModifyListener() {

			public void modifyText(ModifyEvent e) {
				StyledText st = (StyledText) e.widget;
				GridData gd = (GridData) st.getLayoutData();
				if (gd == null) {
					gd = new GridData(GridData.FILL_BOTH);
				}
				Point computeSize = st.computeSize(st.getSize().x, SWT.DEFAULT, false);

				// 此行代码是为了保持列宽不变。减 10 是因为 computeSize 后宽度每次会增加 5 个像素，
				// 因此需要减去上次和本次增加的共10像素。为什么会这样原因不明。
				gd.widthHint = computeSize.x - 10;
				st.setLayoutData(gd);
				editorContentComposite.layout();
				oScrolledComposite.setMinSize(editorContentComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT));

			}
		};

		// 添加鼠标跟踪监听器，用于监听鼠标位置，根据编辑器模式切换光标样式。
		mouseTrackListener = new MouseTrackListener() {

			private void setArrowCursor(MouseEvent arg0) {
				Widget srcWidget = arg0.widget;

				if (isEditModel && srcWidget instanceof StyledText) {
					((StyledText) srcWidget).setCursor(cursorIbeam);
				} else {
					((StyledText) srcWidget).setCursor(cursorArrow);
				}
			}

			public void mouseEnter(MouseEvent arg0) {
				setArrowCursor(arg0);
			}

			public void mouseExit(MouseEvent arg0) {
				setArrowCursor(arg0);
			}

			public void mouseHover(MouseEvent arg0) {
				setArrowCursor(arg0);
			}

		};

		mouseListener = new MouseListener() {

			public void mouseDoubleClick(MouseEvent arg0) {
				// 切换为编辑模式
				isEditModel = true;
				System.out.println("It's running edit model.");
				Control ctrl = arg0.display.getCursorControl();
				ctrl.setCursor(cursorIbeam);
			}

			public void mouseDown(MouseEvent arg0) {
				int inx = (Integer) arg0.widget.getData(DATAKEY_INDEX);
				setSelectionRowColor(inx);

				// 选中模式下要隐藏光标。
				if (!isEditModel) {
					// 通过为容器组件设置焦点，实现在选中模式时隐藏文本框组件中闪烁的光标
					txtComposites[inx].forceFocus();
				}
			}

			public void mouseUp(MouseEvent arg0) {

			}

		};

		keyListener = new KeyListener() {

			public void keyPressed(KeyEvent arg0) {
				// 按 Esc 键切换到选择模式。
				if (arg0.keyCode == SWT.ESC) {
					isEditModel = false;
					Control ctrl = arg0.display.getCursorControl();
					ctrl.setCursor(cursorArrow);
				}

			}

			public void keyReleased(KeyEvent arg0) {
			}

		};
	}

	@Override
	public boolean isDirty() {
		return isDirty;
	}

	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}

	/**
	 * 初始化过滤器条件
	 */
	private void initFilterData() {
		// 以下是模拟数据
		cmbFilter.add("所有文本段");
		cmbFilter.add("未翻译文本段");
		cmbFilter.add("已翻译文本段");
		cmbFilter.add("未批准文本段");
		cmbFilter.add("已批准文本段");
		cmbFilter.add("有批注文本段");
		cmbFilter.add("锁定文本段");
		cmbFilter.add("未锁定文本段");
		cmbFilter.add("重复文本段");
		cmbFilter.add("疑问文本段");
		cmbFilter.add("上下文匹配文本段");
		cmbFilter.add("完全匹配文本段");
		cmbFilter.add("模糊匹配文本段");
		cmbFilter.add("快速翻译文本段");
		cmbFilter.add("自动繁殖文本段");
		cmbFilter.add("错误标记文本段");
		cmbFilter.add("术语不一致文本段");
		cmbFilter.add("译文不一致文本段");
		cmbFilter.add("带修订标记文本段");

		// TODO 根据首选项设置，按指定顺序加载要用的过滤器列表。
	}

	/**
	 * 创建编辑器顶部过滤器和定位器部分
	 */
	private void createFilterPart(Composite parent) {
		Composite topComposite = new Composite(parent, SWT.NONE);
		GridLayout glTop = new GridLayout(3, false);
		glTop.marginHeight = HEIGHT_SPACE;
		glTop.marginWidth = WIDTH_SPACE;
		glTop.horizontalSpacing = HEIGHT_SPACE;
		glTop.verticalSpacing = WIDTH_SPACE;
		topComposite.setLayout(glTop);
		topComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		txtIndex = new Text(topComposite, SWT.BORDER);
		txtIndex.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_TITLE_INACTIVE_FOREGROUND));
		txtIndex.setText(" 请输入行号进行定位 ");

		cmbFilter = new Combo(topComposite, SWT.BORDER);
		cmbFilter.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		// TODO 完善过滤器数据初始化方法
		initFilterData();

		// 过滤器管理器按钮
		Button btnFilterManager = new Button(topComposite, SWT.BORDER);
		btnFilterManager.setText(" ＋ "); // TODO 考虑换图片

		// 暂时使用该按钮来切换表格布局。
		btnFilterManager.addSelectionListener(new SelectionListener() {

			public void widgetDefaultSelected(SelectionEvent arg0) {
				// TODO Auto-generated method stub

			}

			public void widgetSelected(SelectionEvent arg0) {
				layout();
			}

		});

		topComposite.layout();
	}

	/**
	 * 创建编辑器组件。
	 */
	private void createEditorPart(Composite parent) {
		bottomComposite = new Composite(parent, SWT.BORDER);
		GridLayout glBottomCmp = new GridLayout(2, false);
		glBottomCmp.marginHeight = HEIGHT_SPACE;
		glBottomCmp.marginWidth = WIDTH_SPACE;
		glBottomCmp.horizontalSpacing = 0;
		glBottomCmp.verticalSpacing = 0;
		bottomComposite.setLayout(glBottomCmp);
		bottomComposite.setLayoutData(new GridData(GridData.FILL_BOTH));

		// 左上方，第一行，第一列，即表头容器
		colHeaderComposite = new Composite(bottomComposite, SWT.NONE);
		colHeaderComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		createTableHeader(colHeaderComposite);

		// 右方，第二列，占两行。
		editorVBar = new Slider(bottomComposite, SWT.V_SCROLL | SWT.BORDER);
		GridData gdVBar = new GridData(GridData.FILL_VERTICAL);
		gdVBar.verticalSpan = 2; // 合并两行，使之占满整个自定义表格位置。
		editorVBar.setLayoutData(gdVBar);

		// 左下方，第二行，第一列，编辑器容器
		oScrolledComposite = new ScrolledComposite(bottomComposite, SWT.H_SCROLL);
		GridLayout glScrolledComposite = new GridLayout(1, false);
		glScrolledComposite.horizontalSpacing = 0;
		glScrolledComposite.verticalSpacing = 0;
		glScrolledComposite.marginHeight = 0;
		glScrolledComposite.marginWidth = 0;
		oScrolledComposite.setLayout(glScrolledComposite);
		oScrolledComposite.setExpandHorizontal(true);
		oScrolledComposite.setExpandVertical(true);
		oScrolledComposite.setLayoutData(new GridData(GridData.FILL_BOTH));

		editorContentComposite = new Composite(oScrolledComposite, SWT.NONE);
		GridLayout glEditorContentComposite = new GridLayout(1, true);
		glEditorContentComposite.horizontalSpacing = 0;
		glEditorContentComposite.verticalSpacing = 0;
		glEditorContentComposite.marginHeight = 0;
		glEditorContentComposite.marginWidth = 0;
		editorContentComposite.setLayout(glEditorContentComposite);
		editorContentComposite.setLayoutData(new GridData(GridData.FILL_BOTH));

		oScrolledComposite.setContent(editorContentComposite);

		// TODO 需换成真实数据
		// 初始化编辑器中表格行组件。
		txtComposites = new Composite[ROWSOFPAGE];
		tgtComposites = new Composite[ROWSOFPAGE];
		statusComposites = new Composite[ROWSOFPAGE];
		lblRowIndexs = new Label[ROWSOFPAGE];

		for (int i = 0; i < ROWSOFPAGE; i++) {
			Composite rowComposite = new Composite(editorContentComposite, SWT.NONE);
			GridLayout glRowCmp = new GridLayout(2, false);
			glRowCmp.marginHeight = 0;
			glRowCmp.marginWidth = 0;
			glRowCmp.horizontalSpacing = 0;
			glRowCmp.verticalSpacing = 0;
			rowComposite.setLayout(glRowCmp);
			rowComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

			lblRowIndexs[i] = new Label(rowComposite, SWT.BORDER);
			lblRowIndexs[i].setAlignment(SWT.CENTER);
			lblRowIndexs[i].setText(String.valueOf(i + 1));
			GridData gdIndex = new GridData(GridData.FILL_VERTICAL);
			gdIndex.widthHint = COLUMN_INDEX_WIDTH;

			lblRowIndexs[i].setLayoutData(gdIndex);
			txtComposites[i] = new Composite(rowComposite, SWT.NONE);

			StyledText stSrc = new StyledText(txtComposites[i], SWT.MULTI | SWT.WRAP | SWT.READ_ONLY | SWT.BORDER);
			stSrc.setCursor(cursorArrow);
			stSrc.setData(DATAKEY_INDEX, i);

			stSrc.setText("This is a test." + (i + 1));
			GridData gdStSrc = new GridData(GridData.FILL_BOTH);
			gdStSrc.minimumHeight = MINIMUM_ROW_HEIGHT;
			gdStSrc.grabExcessVerticalSpace = true;
			stSrc.setLayoutData(gdStSrc);
			stSrc.addModifyListener(textModifyListener);
			stSrc.addMouseListener(mouseListener);
			stSrc.addKeyListener(keyListener);
			stSrc.addMouseTrackListener(mouseTrackListener);
			stSrc.setCaretOffset(-1);

			tgtComposites[i] = new Composite(txtComposites[i], SWT.NONE);
			statusComposites[i] = new Composite(tgtComposites[i], SWT.NONE);
			Label lblApprovedStatus = new Label(statusComposites[i], SWT.NONE);
			lblApprovedStatus.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_RED));
			lblApprovedStatus.setToolTipText("翻译单元审批状态：已批准");
			lblApprovedStatus.setLayoutData(new GridData(STATUS_FLAG_WIDTH, STATUS_FLAG_HEIGHT));

			Label lblTranslatableStatus = new Label(statusComposites[i], SWT.NONE);
			lblTranslatableStatus.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_BLUE));
			lblTranslatableStatus.setToolTipText("翻译单元可译状态：可译");
			lblTranslatableStatus.setLayoutData(new GridData(STATUS_FLAG_WIDTH, STATUS_FLAG_HEIGHT));

			Label lblTranslationStatus = new Label(statusComposites[i], SWT.NONE);
			lblTranslationStatus.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_CYAN));
			lblTranslationStatus.setToolTipText("目标文本段状态：已翻译");
			lblTranslationStatus.setLayoutData(new GridData(STATUS_FLAG_WIDTH, STATUS_FLAG_HEIGHT));

			StyledText stTgt = new StyledText(tgtComposites[i], SWT.MULTI | SWT.WRAP | SWT.BORDER);
			stTgt.setCursor(cursorArrow);
			stTgt.setData(DATAKEY_INDEX, i);

			GridData gdStTgt = new GridData(GridData.FILL_BOTH);
			gdStTgt.minimumHeight = MINIMUM_ROW_HEIGHT;
			gdStTgt.grabExcessVerticalSpace = true;
			stTgt.setLayoutData(gdStTgt);
			stTgt.addModifyListener(textModifyListener);
			stTgt.addMouseListener(mouseListener);
			stTgt.addKeyListener(keyListener);
			stTgt.addMouseTrackListener(mouseTrackListener);
			stTgt.setCaretOffset(-1);

		}

		// 模拟的编辑器数据模型
		final List<SimpleModel> models = getModel(1000);

		editorVBar.setIncrement(1);
		editorVBar.setPageIncrement(ROWSOFPAGE);
		editorVBar.setMaximum(models.size());
		editorVBar.setMinimum(1);
		editorVBar.setThumb(ROWSOFPAGE);

		editorVBar.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				int y = editorVBar.getSelection();
				System.out.println("current y value:" + y);
				System.out.println("thumb value:" + editorVBar.getThumb());
				refreshContent(models, editorVBar.getSelection());
			}
		});

		// 初始化界面 UI 的显示内容
		// refreshContent(models, 1);

		// 设置编辑器组件布局
		setEditorLayout();
	}

	/**
	 * 根据用户拖动滚动条的位置，更新界面 UI 的显示内容
	 * @param selection
	 *            滚动条的位置，即界面 UI 最顶层显示的 model 索引 ;
	 */
	protected void refreshContent(List<SimpleModel> models, int selection) {
		for (int i = 0; i < ROWSOFPAGE; i++) {
			// model 的索引从 0 开始，滚动条的最小值从 1 开始
			SimpleModel model = models.get(selection + i - 1);
			// 设置编号
			lblRowIndexs[i].setText(model.getId());
			// 设置源文本
			Control[] children = txtComposites[i].getChildren();
			for (int j = 0; j < children.length; j++) {
				if (children[j] instanceof StyledText) {
					((StyledText) children[j]).setText(model.getSrcText());
					break;
				}
			}
//			txtComposites[i].layout();
			// 设置目标文本
			children = tgtComposites[i].getChildren();
			for (int j = 0; j < children.length; j++) {
				if (children[j] instanceof StyledText) {
					((StyledText) children[j]).setText(model.targetText);
					break;
				}
			}
//			tgtComposites[i].layout();
		}
	}

	/**
	 * 编辑器重新布局。
	 */
	protected synchronized void layout() {
		if (layoutLock) {
			return;
		}
		layoutLock = true;

		isHLayoutModel = !isHLayoutModel;
		createTableHeader(colHeaderComposite);
		setEditorLayout();

		layoutLock = false;
	}

	/**
	 * 设置编辑器布局。在水平布局和垂直布局中切换。
	 */
	private void setEditorLayout() {
		if (isHLayoutModel) {
			GridLayout glTxtCmp = new GridLayout(2, true);
			glTxtCmp.marginHeight = 0;
			glTxtCmp.marginWidth = 0;
			glTxtCmp.verticalSpacing = 0;
			glTxtCmp.horizontalSpacing = 0;

			GridLayout glTgtComposite = new GridLayout(2, false);
			glTgtComposite.horizontalSpacing = 0;
			glTgtComposite.verticalSpacing = 0;
			glTgtComposite.marginHeight = 0;
			glTgtComposite.marginWidth = 0;

			GridLayout glStatusCmp = new GridLayout(1, true);
			glStatusCmp.marginHeight = 0;
			glStatusCmp.marginWidth = 0;
			glStatusCmp.horizontalSpacing = 0;
			glStatusCmp.verticalSpacing = 0;
			for (int i = 0; i < ROWSOFPAGE; i++) {
				txtComposites[i].setLayout(glTxtCmp);
				GridData gdCmpTxt = new GridData(GridData.FILL_HORIZONTAL);
				gdCmpTxt.minimumHeight = MINIMUM_ROW_HEIGHT;
				gdCmpTxt.grabExcessVerticalSpace = true;
				txtComposites[i].setLayoutData(gdCmpTxt);

				tgtComposites[i].setLayout(glTgtComposite);
				GridData gdCmpTgt = new GridData(GridData.FILL_BOTH);
				gdCmpTgt.minimumHeight = MINIMUM_ROW_HEIGHT;
				gdCmpTgt.grabExcessVerticalSpace = true;
				tgtComposites[i].setLayoutData(gdCmpTgt);

				statusComposites[i].setLayout(glStatusCmp);
				statusComposites[i].setLayoutData(new GridData(GridData.FILL_VERTICAL));
			}
		} else {
			GridLayout glTxtCmp = new GridLayout(1, true);
			glTxtCmp.marginHeight = 0;
			glTxtCmp.marginWidth = 0;
			glTxtCmp.verticalSpacing = 0;
			glTxtCmp.horizontalSpacing = 0;

			GridLayout glTgtComposite = new GridLayout(1, false);
			glTgtComposite.horizontalSpacing = 0;
			glTgtComposite.verticalSpacing = 0;
			glTgtComposite.marginHeight = 0;
			glTgtComposite.marginWidth = 0;

			GridLayout glStatusCmp = new GridLayout(3, true);
			glStatusCmp.marginHeight = 0;
			glStatusCmp.marginWidth = 0;
			glStatusCmp.horizontalSpacing = 0;
			glStatusCmp.verticalSpacing = 0;
			for (int i = 0; i < ROWSOFPAGE; i++) {
				txtComposites[i].setLayout(glTxtCmp);

				GridData gdCmpTxt = new GridData(GridData.FILL_HORIZONTAL);
				gdCmpTxt.minimumHeight = MINIMUM_ROW_HEIGHT;
				gdCmpTxt.grabExcessVerticalSpace = true;
				txtComposites[i].setLayoutData(gdCmpTxt);

				tgtComposites[i].setLayout(glTgtComposite);
				tgtComposites[i].setLayoutData(new GridData(GridData.FILL_BOTH));

				statusComposites[i].setLayout(glStatusCmp);
				statusComposites[i].setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			}
		}
		editorContentComposite.layout();
	}

	/**
	 * 创建编辑器表头组件。
	 * @param parent
	 *            列头容器。
	 */
	private void createTableHeader(Composite parent) {
		// parent.setBackground(blue);
		if (parent != null) {
			Control[] headerControls = parent.getChildren();
			if (headerControls != null) {
				for (Control widget : headerControls) {
					if (widget != null && !widget.isDisposed()) {
						widget.dispose();
						widget = null;
					}
				}
			}
		}

		if (lblColumnHeader != null) {
			lblColumnHeader = null;
		}

		GridLayout glParent = new GridLayout(2, false);
		glParent.horizontalSpacing = 0;
		glParent.verticalSpacing = 0;
		glParent.marginHeight = 0;
		glParent.marginWidth = 0;
		parent.setLayout(glParent);

		if (isHLayoutModel) {
			lblColumnHeader = new Label[3];
			GridData[] gdColHeader = new GridData[3];
			gdColHeader[0] = new GridData();
			gdColHeader[0].widthHint = COLUMN_INDEX_WIDTH;
			gdColHeader[0].heightHint = MINIMUM_ROW_HEIGHT;
			gdColHeader[0].grabExcessHorizontalSpace = false;
			gdColHeader[0].grabExcessVerticalSpace = true;

			lblColumnHeader[0] = new Label(colHeaderComposite, SWT.BORDER);
			lblColumnHeader[0].setText("编号");
			lblColumnHeader[0].setLayoutData(gdColHeader[0]);
			lblColumnHeader[0].setAlignment(SWT.CENTER);

			Composite rightCompoiste = new Composite(colHeaderComposite, SWT.NONE);
			GridLayout glRightComposite = new GridLayout(2, true);
			glRightComposite.horizontalSpacing = 0;
			glRightComposite.verticalSpacing = 0;
			glRightComposite.marginHeight = 0;
			glRightComposite.marginWidth = 0;
			rightCompoiste.setLayout(glRightComposite);
			rightCompoiste.setLayoutData(new GridData(GridData.FILL_BOTH));

			gdColHeader[1] = new GridData(GridData.FILL_HORIZONTAL);
			gdColHeader[1].heightHint = MINIMUM_ROW_HEIGHT;
			lblColumnHeader[1] = new Label(rightCompoiste, SWT.BORDER);
			lblColumnHeader[1].setText("源语言");
			lblColumnHeader[1].setLayoutData(gdColHeader[1]);
			lblColumnHeader[1].setAlignment(SWT.CENTER);
			gdColHeader[2] = new GridData(GridData.FILL_HORIZONTAL);
			gdColHeader[2].heightHint = MINIMUM_ROW_HEIGHT;
			lblColumnHeader[2] = new Label(rightCompoiste, SWT.BORDER);
			lblColumnHeader[2].setText("目标语言");
			lblColumnHeader[2].setLayoutData(gdColHeader[2]);
			lblColumnHeader[2].setAlignment(SWT.CENTER);
		} else {
			lblColumnHeader = new Label[2];
			GridData[] gdColHeader = new GridData[2];
			gdColHeader[0] = new GridData();
			gdColHeader[0].widthHint = COLUMN_INDEX_WIDTH;
			gdColHeader[0].heightHint = MINIMUM_ROW_HEIGHT;

			lblColumnHeader[0] = new Label(colHeaderComposite, SWT.BORDER);
			lblColumnHeader[0].setText("编号");
			lblColumnHeader[0].setAlignment(SWT.CENTER);
			lblColumnHeader[0].setLayoutData(gdColHeader[0]);

			gdColHeader[1] = new GridData(GridData.FILL_BOTH);
			gdColHeader[1].heightHint = MINIMUM_ROW_HEIGHT;
			lblColumnHeader[1] = new Label(colHeaderComposite, SWT.BORDER);
			lblColumnHeader[1].setText("源语言 --> 目标语言");
			lblColumnHeader[1].setLayoutData(gdColHeader[1]);
			lblColumnHeader[1].setAlignment(SWT.CENTER);
		}
		parent.layout();
	}

	@Override
	public void createPartControl(Composite parent) {
		initColors(parent);

		GridLayout glParent = new GridLayout(1, false);
		glParent.horizontalSpacing = 0;
		glParent.verticalSpacing = 0;
		glParent.marginHeight = 0;
		glParent.marginWidth = 0;
		parent.setLayout(glParent);
		GridData gdParent = new GridData(GridData.FILL_BOTH);
		gdParent.grabExcessVerticalSpace = true;
		gdParent.grabExcessHorizontalSpace = true;
		parent.setLayoutData(gdParent);

		createFilterPart(parent);
		createEditorPart(parent);
		parent.layout();
	}

	/** 初始化测试用颜色 */
	private void initColors(Composite parent) {
		selectedBgColor = parent.getDisplay().getSystemColor(SWT.COLOR_LIST_SELECTION);
		selectedFgColor = parent.getDisplay().getSystemColor(SWT.COLOR_LIST_SELECTION_TEXT);
	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}

	public void addSelectionChangedListener(ISelectionChangedListener listener) {
		// TODO Auto-generated method stub

	}

	public ISelection getSelection() {
		// TODO Auto-generated method stub
		return null;
	}

	public void removeSelectionChangedListener(ISelectionChangedListener listener) {
		// TODO Auto-generated method stub

	}

	public void setSelection(ISelection selection) {
		// TODO Auto-generated method stub

	}

	/**
	 * 返回模拟的数据模型列表
	 * @param size
	 *            初始化列表大小，如果为负，则返回空列表
	 * @return ;
	 */
	private List<SimpleModel> getModel(int size) {
		List<SimpleModel> result = new ArrayList<SimpleModel>();
		for (int i = 0; i < size; i++) {
			String id = String.valueOf(i + 1);
			result
					.add(new SimpleModel(
							id,
							"The " + id + " source text.",
							"The "
									+ id
									+ " target text.target text.target text.target text.target text.target text.target text.target text.target text.target text.target text.target text.target text.target text.target text.target text.target text.target text.target text.target text.target text.target text.target text.target text.target text.target text.target text.target text.target text.target text.target text.target text.target text.target text.target text.target text.target text.target text.target text.target text.target text.target text.target text.target text.target text.target text.target text.target text.target text.target text.target text.target text.target text.target text.target text.target text.target text.target text.target text.target text.target text.target text.target text.target text.target text.target text.target text.target text.target text.target text.target text.target text.target text.target text.target text.target text.target text.target text.target text.target text.target text.target text.target text.target text.target text.target text.target text.target text.target text.target text.target text.target text.target text.target text.target text.target text.target text.target text.target text.target text.target text.target text.target text.target text.target text.target text.target text.target text.target text.target text.target text.v"));
		}
		return result;
	}

	/**
	 * 模拟每个翻译单元的简单数据对象
	 * @author cheney
	 * @since JDK1.6
	 */
	class SimpleModel {
		String id;
		String srcText;
		String targetText;

		/**
		 * 构建函数
		 * @param id
		 *            编号 ID
		 * @param srcText
		 *            源文本
		 * @param targetText
		 *            目标文本
		 */
		public SimpleModel(String id, String srcText, String targetText) {
			this.id = id;
			this.srcText = srcText;
			this.targetText = targetText;
		}

		/**
		 * 编号 ID
		 * @return ;
		 */
		public String getId() {
			return id;
		}

		/**
		 * 编号 ID
		 * @param id
		 *            ;
		 */
		public void setId(String id) {
			this.id = id;
		}

		/**
		 * 源文本
		 * @return ;
		 */
		public String getSrcText() {
			return srcText;
		}

		/**
		 * 源文本
		 * @param srcText
		 *            ;
		 */
		public void setSrcText(String srcText) {
			this.srcText = srcText;
		}

		/**
		 * 目标文本
		 * @return ;
		 */
		public String getTargetText() {
			return targetText;
		}

		/**
		 * 目标文本
		 * @param targetText
		 *            ;
		 */
		public void setTargetText(String targetText) {
			this.targetText = targetText;
		}
	}

	@Override
	public void dispose() {
		if(titleImage != null && !titleImage.isDisposed()){
			titleImage.dispose();
		}
		if(cursorArrow != null && !cursorArrow.isDisposed()){
			cursorArrow.dispose();
		}
		if(cursorIbeam != null && !cursorIbeam.isDisposed()){
			cursorIbeam.dispose();
		}
		super.dispose();
	}
}

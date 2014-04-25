package net.heartsome.cat.ts.ui.xliffeditor.nattable.editor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import net.heartsome.cat.ts.core.bean.TransUnitBean;
import net.heartsome.cat.ts.ui.Constants;
import net.heartsome.cat.ts.ui.innertag.ISegmentViewer;
import net.heartsome.cat.ts.ui.innertag.SegmentViewer;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.UpdateDataBean;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.celleditor.EditableManager;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.celleditor.SourceEditMode;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.config.VerticalNatTableConfig;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.layer.LayerUtil;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.propertyTester.AddSegmentToTMPropertyTester;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.propertyTester.SignOffPropertyTester;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.propertyTester.UnTranslatedPropertyTester;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.resource.Messages;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.utils.NattableUtil;
import net.sourceforge.nattable.data.convert.IDisplayConverter;
import net.sourceforge.nattable.data.validate.IDataValidator;
import net.sourceforge.nattable.edit.ICellEditHandler;
import net.sourceforge.nattable.edit.editor.EditorSelectionEnum;
import net.sourceforge.nattable.edit.editor.ICellEditor;
import net.sourceforge.nattable.selection.SelectionLayer;
import net.sourceforge.nattable.selection.SelectionLayer.MoveDirectionEnum;
import net.sourceforge.nattable.selection.command.MoveSelectionCommand;
import net.sourceforge.nattable.selection.command.ScrollSelectionCommand;
import net.sourceforge.nattable.style.CellStyleAttributes;
import net.sourceforge.nattable.style.HorizontalAlignmentEnum;
import net.sourceforge.nattable.style.IStyle;
import net.sourceforge.nattable.util.GUIHelper;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ST;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.custom.VerifyKeyListener;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.IME;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;

/**
 * 基于 StyledText 的单元格编辑器。<br>
 * <li>使用 {@link #setText(Object)} 方法设置其值。</li>
 * @author weachy
 * @since JDK1.5
 */
public class StyledTextCellEditor implements DisposeListener, ICellEditor {

	public StyledTextCellEditor(XLIFFEditorImplWithNatTable xliffEditor) {
		this.xliffEditor = xliffEditor;
		this.actionHandler = new XLIFFEditorActionHandler(xliffEditor.getEditorSite().getActionBars());
	}

	private EditorSelectionEnum selectionMode = EditorSelectionEnum.ALL;

	/** 封装StyledText，提供撤销/重做管理器的组件. */
	protected SegmentViewer viewer = null;

	/** XLIFF 编辑器实例 */
	private final XLIFFEditorImplWithNatTable xliffEditor;

	/** 单元格类型。值为 {@link NatTableConstant#SOURCE}、{@link NatTableConstant#TARGET}之一 */
	private String cellType;

	/**
	 * 得到单元格类型
	 * @return 值为 {@link NatTableConstant#SOURCE}、{@link NatTableConstant#TARGET} 之一;
	 */
	public String getCellType() {
		return cellType;
	}

	public final void setSelectionMode(EditorSelectionEnum selectionMode) {
		this.selectionMode = selectionMode;
	}

	public final EditorSelectionEnum getSelectionMode() {
		return selectionMode;
	}

	/**
	 * 用于标识编辑器关闭状态<br/>
	 * @see net.heartsome.cat.ts.ui.xliffeditor.nattable.editor.XLIFFEditorImplWithNatTable#selectedRowChanged()
	 * @see net.heartsome.cat.ts.ui.xliffeditor.nattable.editor.StyledTextCellEditor.addListeners().new IPartListener2()
	 *      {...}.partActivated(IWorkbenchPartReference partRef)
	 */
	private boolean close = true;

	/** 关闭监听器 */
	private HashSet<Listener> closingListeners = new HashSet<Listener>();

	/**
	 * 添加关闭单元格关闭时的监听器
	 * @param closeListener
	 *            关闭监听器 ;
	 */
	public void addClosingListener(Listener closeListener) {
		if (closeListener == null) {
			SWT.error(SWT.ERROR_NULL_ARGUMENT);
		}
		closingListeners.add(closeListener);
	}

	/**
	 * 移除关闭单元格关闭时的监听器
	 * @param closeListener
	 *            关闭监听器 ;
	 */
	public void removeClosingListener(Listener closeListener) {
		for (Listener listener : closingListeners) {
			if (listener != null && listener.equals(closeListener)) {
				closingListeners.remove(listener);
				break;
			}
		}
	}

	/** 绑定全局 Edit 菜单的处理类 */
	private XLIFFEditorActionHandler actionHandler;

	/** 源文本的值 */
	private String source;

	/** 可编辑状态管理器 */
	private EditableManager editableManager = new EditableManager(cellType == NatTableConstant.SOURCE) {
		@Override
		protected void setupReadOnlyMode() {
			checkWidget();

			StyledText text = viewer.getTextWidget();
			if (!close) {
				if (!editable) {
					return;
				}
				text.removeVerifyKeyListener(edit_VerifyKey);
				text.removeTraverseListener(edit_Traverse);
			}
			editable = false;
			text.setEditable(false);
			text.addVerifyKeyListener(readOnly_VerifyKey);
		}

		@Override
		protected void setupEditMode() {
			checkWidget();

			StyledText text = viewer.getTextWidget();
			if (!close) {
				if (editable) {
					return;
				}
				text.removeVerifyKeyListener(readOnly_VerifyKey);
			}
			editable = true;
			text.setEditable(true);
			text.addVerifyKeyListener(edit_VerifyKey);
			text.addTraverseListener(edit_Traverse);
		}

		@Override
		public void judgeEditable() {
			checkWidget();

			if (isApprovedOrLocked()) {
				setupReadOnlyMode();
				setUneditableMessage(Messages.getString("editor.StyledTextCellEditor.msg1"));
			} else {
				if (cellType == NatTableConstant.SOURCE) {
					if (getSourceEditMode() != SourceEditMode.DISEDITABLE) {
						setupEditMode();
					} else {
						setupReadOnlyMode();
						setUneditableMessage(Messages.getString("editor.StyledTextCellEditor.msg2"));
					}
				} else if (cellType == NatTableConstant.TARGET) {
					setupEditMode();
				}
			}
		}

		public void checkWidget() {
			StyledText text = viewer.getTextWidget();
			if (text == null || text.isDisposed()) {
				SWT.error(SWT.ERROR_FAILED_EVALUATE);
			}
		}
	};

	/**
	 * 是否可编辑
	 * @return ;
	 */
	public boolean isEditable() {
		return editableManager.getEditable();
	}

	/**
	 * 是否批准或者锁定
	 * @return ;
	 */
	public boolean isApprovedOrLocked() {
		int rowIndex = hsCellEditor.getRowIndex();
		if (rowIndex == -1) {
			return true;
		}
		if (!xliffEditor.isHorizontalLayout()) { // 是垂直布局
			rowIndex = rowIndex / VerticalNatTableConfig.ROW_SPAN; // 得到实际的行索引
		}
		TransUnitBean tu = xliffEditor.getRowTransUnitBean(rowIndex);
		String translate = tu.getTuProps().get("translate");
		if (translate != null && "no".equalsIgnoreCase(translate)) {
			return true;
		}
		return false;
	}

	/**
	 * 得到可编辑状态管理器
	 * @return ;
	 */
	public EditableManager getEditableManager() {
		return editableManager;
	}

	private HsMultiCellEditor hsCellEditor;

	protected Control activateCell(final Composite parent, HsMultiCellEditor hsCellEditor) {
		this.hsCellEditor = hsCellEditor;
		StyledText text = createTextControl(parent);
		text.setBounds(hsCellEditor.getEditorBounds());
		// analyzeCellType(); // 分析单元格类型。
		this.cellType = hsCellEditor.getType();
		if (cellType == NatTableConstant.TARGET) {
			this.source = HsMultiActiveCellEditor.getSourceEditor().getOriginalCanonicalValue().toString();
			viewer.setSource(source); // 设置原文本，用来解析内部标记
		}

		editableManager.judgeEditable(); // 判断“可编辑”状态;

		// If we have an initial value, then
		Object originalCanonicalValue = this.hsCellEditor.getOriginalCanonicalValue();
		if (originalCanonicalValue != null) {
			setCanonicalValue(new UpdateDataBean(originalCanonicalValue.toString(), null, null));
		} else {
			setCanonicalValue(new UpdateDataBean());
		}

		// 改变关闭状态标识
		close = false;
		xliffEditor.getTable().addDisposeListener(this);

		// text.forceFocus();

		// 初始化撤销/重做管理器，设置步长为 50。
		viewer.initUndoManager(50);
		// 绑定全局 Edit 菜单
		actionHandler.addTextViewer(viewer);
		text.addKeyListener(movedKeyListener);

		// 移除向上和向下键默认事件处理，将此部分实现放到upAndDownKeyListener监听中
		text.setKeyBinding(SWT.ARROW_DOWN, SWT.NULL);
		text.setKeyBinding(SWT.ARROW_UP, SWT.NULL);
		addMouselistener(text);
		return text;
	}

	private  Map<String ,Boolean> mouseState = new HashMap<String, Boolean>();
	
	private  void addMouselistener(StyledText text){
		
		text.addMouseListener(new MouseListener() {
			
			public void mouseUp(MouseEvent e) {
				// TODO Auto-generated method stub
				mouseState.put("mouseDown", false);
			}
			
			public void mouseDown(MouseEvent e) {
				// TODO Auto-generated method stub
				mouseState.put("mouseDown", true);
			}
			
			public void mouseDoubleClick(MouseEvent e) {
			}
		});
	}
	
	public  boolean getMouseState(){
		if( mouseState.get("mouseDown")==null){
			mouseState.put("mouseDown", false);
		}
		return mouseState.get("mouseDown");
	}
	public XLIFFEditorActionHandler getActionHandler() {
		return actionHandler;
	}

	/**
	 * 目标编辑器中需要设置原文本，用来解析内部标记
	 * @param source
	 *            带有内部标记内容的源文本内容 ;
	 */
	public void setSource(String source) {
		this.source = source;
	}

	private VerifyKeyListener readOnly_VerifyKey = new VerifyKeyListener() {
		public void verifyKey(VerifyEvent event) {
			showUneditableMessage();
		}
	};

	private VerifyKeyListener edit_VerifyKey = new VerifyKeyListener() {
		public void verifyKey(VerifyEvent event) {
			NattableUtil.refreshCommand(AddSegmentToTMPropertyTester.PROPERTY_NAMESPACE,
					AddSegmentToTMPropertyTester.PROPERTY_ENABLED);
			NattableUtil.refreshCommand(SignOffPropertyTester.PROPERTY_NAMESPACE,
					SignOffPropertyTester.PROPERTY_ENABLED);
			NattableUtil.refreshCommand(UnTranslatedPropertyTester.PROPERTY_NAMESPACE,
					UnTranslatedPropertyTester.PROPERTY_ENABLED);
		}
	};

	private TraverseListener edit_Traverse = new TraverseListener() {
		public void keyTraversed(TraverseEvent event) {
			// StyledText text = viewer.getTextWidget();
			// text.gettext
		}
	};

	/**
	 * 实现编辑模式上下移动光标到底部或者顶部时自动移动到下一行
	 */
	private KeyListener movedKeyListener = new KeyListener() {
		public void keyReleased(KeyEvent e) {
		}

		public void keyPressed(KeyEvent e) {
			if (e.keyCode == SWT.ARROW_DOWN && e.stateMask == SWT.NONE) {
				StyledText text = viewer.getTextWidget();
				int oldOffset = text.getCaretOffset();
				text.invokeAction(ST.LINE_DOWN);
				int newOffset = text.getCaretOffset();
				if (oldOffset == newOffset) {
					SelectionLayer selectionLayer = LayerUtil.getLayer(xliffEditor.getTable(), SelectionLayer.class);
					int rowPosition = selectionLayer.getLastSelectedCellPosition().rowPosition;
					if (rowPosition != selectionLayer.getRowCount() - 1) { // 减去列头行
						HsMultiActiveCellEditor.commit(true);
						int stepSize = 1;
						if (!xliffEditor.isHorizontalLayout()) {
							stepSize = 2;
						}
						xliffEditor.getTable().doCommand(
								new MoveSelectionCommand(MoveDirectionEnum.DOWN, stepSize, false, false));
						HsMultiCellEditorControl.activeSourceAndTargetCell(xliffEditor);
					}
				}
			} else if (e.keyCode == SWT.ARROW_UP && e.stateMask == SWT.NONE) {
				StyledText text = viewer.getTextWidget();
				int oldOffset = text.getCaretOffset();
				text.invokeAction(ST.LINE_UP);
				int newOffset = text.getCaretOffset();
				if (oldOffset == newOffset) {
					SelectionLayer selectionLayer = LayerUtil.getLayer(xliffEditor.getTable(), SelectionLayer.class);
					int rowPosition = selectionLayer.getLastSelectedCellPosition().rowPosition;
					if (rowPosition != 0) {
						HsMultiActiveCellEditor.commit(true);
						int stepSize = 1;
						if (!xliffEditor.isHorizontalLayout()) {
							stepSize = 2;
						}
						xliffEditor.getTable().doCommand(
								new MoveSelectionCommand(MoveDirectionEnum.UP, stepSize, false, false));
						HsMultiCellEditorControl.activeSourceAndTargetCell(xliffEditor);
					}
				}
			} else if (e.keyCode == SWT.PAGE_UP && e.stateMask == SWT.NONE) {
				SelectionLayer selectionLayer = LayerUtil.getLayer(xliffEditor.getTable(), SelectionLayer.class);
				int rowPosition = selectionLayer.getLastSelectedCellPosition().rowPosition;
				if (rowPosition != 0) {
					HsMultiActiveCellEditor.commit(true);
					xliffEditor.getTable().doCommand(new ScrollSelectionCommand(MoveDirectionEnum.UP, false, false));
					HsMultiCellEditorControl.activeSourceAndTargetCell(xliffEditor);
				}
			} else if (e.keyCode == SWT.PAGE_DOWN && e.stateMask == SWT.NONE) {
				SelectionLayer selectionLayer = LayerUtil.getLayer(xliffEditor.getTable(), SelectionLayer.class);
				int rowPosition = selectionLayer.getLastSelectedCellPosition().rowPosition;
				if (rowPosition != selectionLayer.getRowCount() - 1) {
					HsMultiActiveCellEditor.commit(true);
					xliffEditor.getTable().doCommand(new ScrollSelectionCommand(MoveDirectionEnum.DOWN, false, false));
					HsMultiCellEditorControl.activeSourceAndTargetCell(xliffEditor);
				}
			} else if (e.keyCode == SWT.HOME && e.stateMask == SWT.CTRL) {
				HsMultiActiveCellEditor.commit(true);
				xliffEditor.getTable().doCommand(
						new MoveSelectionCommand(MoveDirectionEnum.UP, SelectionLayer.MOVE_ALL, false, false));
				HsMultiCellEditorControl.activeSourceAndTargetCell(xliffEditor);
			} else if (e.keyCode == SWT.END && e.stateMask == SWT.CTRL) {
				HsMultiActiveCellEditor.commit(true);
				xliffEditor.getTable().doCommand(
						new MoveSelectionCommand(MoveDirectionEnum.DOWN, SelectionLayer.MOVE_ALL, false, false));
				HsMultiCellEditorControl.activeSourceAndTargetCell(xliffEditor);
			} else if ((e.keyCode == SWT.ESC) && e.stateMask == SWT.NONE) {
				HsMultiActiveCellEditor.commit(true);
			}
		}
	};

	private void autoResize() {
		HsMultiActiveCellEditor.synchronizeRowHeight();
	}

	/**
	 * 显示不可编辑信息。
	 */
	public void showUneditableMessage() {
		viewer.setToolTipMessage(editableManager.getUneditableMessage());
	}

	private void selectText() {
		StyledText text = viewer.getTextWidget();
		if (text == null || text.isDisposed()) {
			return;
		}
		// viewer.setSelectedRange(selectionOffset, selectionLength)
		int textLength = text.getText().length();
		if (textLength > 0) {
			EditorSelectionEnum selectionMode = getSelectionMode();
			if (selectionMode == EditorSelectionEnum.ALL) {
				text.setSelection(0, textLength);
			} else if (selectionMode == EditorSelectionEnum.END) {
				text.setSelection(textLength, textLength);
			}
		}
		text.setCaretOffset(textLength);
	}

	protected StyledText createTextControl(Composite parent) {
		TagStyleManager tagStyleManager = xliffEditor.getTagStyleManager();

		IStyle cellStyle = this.hsCellEditor.getCellStyle();
		int styled = HorizontalAlignmentEnum.getSWTStyle(cellStyle);
		styled |= SWT.MULTI | SWT.WRAP;
		viewer = new SegmentViewer(parent, styled, tagStyleManager.getTagStyle());

		// 添加标记样式改变监听
		// addTagStyleChangeListener();

		// 注册标记样式调节器
		net.heartsome.cat.ts.ui.innertag.tagstyle.TagStyleConfigurator.configure(viewer);
		// TagStyleConfigurator.configure(viewer);

		// 将原来直接创建StyledText的方式改为由TextViewer提供
		final StyledText textControl = viewer.getTextWidget();
		initStyle(textControl, cellStyle);
		textControl.addFocusListener(new FocusListener() {

			public void focusLost(FocusEvent e) {
			}

			public void focusGained(FocusEvent e) {
				getActionHandler().updateGlobalActionHandler();
			}
		});

		viewer.getDocument().addDocumentListener(new IDocumentListener() {

			public void documentChanged(DocumentEvent e) {
				// 自动行高
				autoResize();
			}

			public void documentAboutToBeChanged(DocumentEvent event) {

			}
		});

		// 实现编辑模式下添加右键菜单
		// dispose textControl前应去掉右键menu，因为右键menu是和nattable共享的，不能在这儿dispose，说见close()方法
		final Menu menu = (Menu) xliffEditor.getTable().getData(Menu.class.getName());
		textControl.setMenu(menu);

		return textControl;
	}

	/**
	 * 初始化默认颜色、字体等
	 * @param textControl
	 *            ;
	 */
	private void initStyle(final StyledText textControl, IStyle cellStyle) {
		// textControl.setBackground(cellStyle.getAttributeValue(CellStyleAttributes.BACKGROUND_COLOR));
		textControl.setBackground(GUIHelper.getColor(210, 210, 240));
		textControl.setForeground(cellStyle.getAttributeValue(CellStyleAttributes.FOREGROUND_COLOR));

		textControl.setLineSpacing(Constants.SEGMENT_LINE_SPACING);
		textControl.setLeftMargin(Constants.SEGMENT_LEFT_MARGIN);
		textControl.setRightMargin(Constants.SEGMENT_RIGHT_MARGIN);
		textControl.setTopMargin(Constants.SEGMENT_TOP_MARGIN);
		textControl.setBottomMargin(Constants.SEGMENT_TOP_MARGIN);

		// textControl.setLeftMargin(0);
		// textControl.setRightMargin(0);
		// textControl.setTopMargin(0);
		// textControl.setBottomMargin(0);

		textControl.setFont(JFaceResources.getFont(net.heartsome.cat.ts.ui.Constants.XLIFF_EDITOR_TEXT_FONT));
		textControl.setIME(new IME(textControl, SWT.NONE));

	}

	/**
	 * 添加标记样式改变监听 ;
	 */
	/*
	 * private void addTagStyleChangeListener() { final TagStyleManager tagStyleManager =
	 * xliffEditor.getTagStyleManager(); final Listener tagStyleChangeListener = new Listener() { public void
	 * handleEvent(Event event) { if (event.data != null && event.data instanceof TagStyle) {
	 * viewer.setTagStyle((TagStyle) event.data); } } };
	 * tagStyleManager.addTagStyleChangeListener(tagStyleChangeListener);
	 * 
	 * viewer.getTextWidget().addDisposeListener(new DisposeListener() { public void widgetDisposed(DisposeEvent e) {
	 * tagStyleManager.removeTagStyleChangeListener(tagStyleChangeListener); } }); }
	 */

	/**
	 * 得到当前的值（内部标记被转换为 XML 格式）
	 * @return ;
	 */
	public Object getCanonicalValue() {
		String content = viewer.getText();
		if (!content.equals(canonicalValue.getText())) {
			canonicalValue.setMatchType(null);
			canonicalValue.setQuality(null);
		}
		canonicalValue.setText(content);
		return canonicalValue;
	}

	private UpdateDataBean canonicalValue;

	public void setCanonicalValue(Object canonicalValue) {
		this.canonicalValue = (UpdateDataBean) canonicalValue;
		String text = this.canonicalValue == null ? "" : this.canonicalValue.getText(); // 保留原始值
		// 初始化 viewer 内容
		viewer.setText(text);

		selectionMode = EditorSelectionEnum.END;
		selectText();
	}

	/**
	 * 将指定文本添加到光标所在位置。 robert 2011-12-21
	 * @param canonicalValue
	 *            ;
	 */
	public void insertCanonicalValue(Object canonicalValue) {
		StyledText text = viewer.getTextWidget();
		if (text == null || text.isDisposed()) {
			return;
		}

		int offset = text.getCaretOffset();
		text.insert(canonicalValue.toString());
		text.setCaretOffset(offset + canonicalValue.toString().length());
	}

	public void close() {
		if (close) {
			return;
		}

		for (Listener listener : closingListeners) {
			Event event = new Event();
			event.data = this;
			listener.handleEvent(event);
		}

		close = true; // 状态改为已经关闭
		xliffEditor.getTable().removeDisposeListener(this);

		StyledText text = viewer.getTextWidget();
		if (text != null && !text.isDisposed()) {
			actionHandler.removeTextViewer();
			text.setMenu(null); // dispose前应去掉右键menu，因为右键menu是和nattable共享的
			viewer.reset();
			text.dispose();
			text = null;
		}

		// 如果 XLIFF 编辑器仍处于激活状态，则把焦点交给编辑器
		try {
			IWorkbenchPart activepart = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
					.getActivePart();
			if (xliffEditor.equals(activepart)) {
				xliffEditor.setFocus();
			}
		} catch (NullPointerException e) {
		}

		// NatTable table = xliffEditor.getTable();
		// int[] rowPositions = new int[] { hsCellEditor.getRowPosition() };
		// table.doCommand(new AutoResizeCurrentRowsCommand(table, rowPositions, table.getConfigRegistry()));
	}

	/**
	 * 重写关闭状态的判断规则
	 * @see net.sourceforge.nattable.edit.editor.AbstractCellEditor#isClosed()
	 */
	public boolean isClosed() {
		return close;
	}

	/**
	 * 得到实际的光标位置（StyledText中的文本有一部分是已经被转换成内部标记的，与XML文本的分割位置有差异，因此需要此方法得到在XML中实际的分割位置）
	 * @return ;
	 */
	public int getRealSplitOffset() {
		return viewer.getRealSplitOffset();
	}

	/**
	 * 得到实际的光标位置（StyledText中的文本有一部分是已经被转换成内部标记的，与XML文本的分割位置有差异，因此需要此方法得到在XML中实际的分割位置）
	 * @return ;
	 */
	public int getRealSplitOffset(int offset) {
		return viewer.getRealSplitOffset(offset);
	}

	/**
	 * 清除所有内部标记 ;
	 */
	public void clearTags() {
		if (isEditable()) {
			viewer.clearAllInnerTags();
		} else {
			showUneditableMessage();
		}
	}

	/**
	 * 得到 SegmentViewer 组件。
	 * @return ;
	 */
	public ISegmentViewer getSegmentViewer() {
		return viewer;
	}

	public void widgetDisposed(DisposeEvent e) {
		this.close();
	}

	/**
	 * 得到选中的原始文本。
	 * @return XML 中的原始内容;
	 */
	public String getSelectedOriginalText() {
		return viewer.getSelectedOriginalText();
	}

	/**
	 * 得到选中的纯文本内容
	 * @return XML 中的原始内容;
	 */
	public String getSelectedPureText() {
		return viewer.getSelectedPureText();
	}

	public Control activateCell(Composite parent, Object originalCanonicalValue, Character initialEditValue,
			IDisplayConverter displayConverter, IStyle cellStyle, IDataValidator dataValidator,
			ICellEditHandler editHandler, int colIndex, int rowIndex) {

		return null;
	}

	/** @return the columnPosition */
	public int getColumnPosition() {
		return hsCellEditor.getColumnPosition();
	}

	/** @return the rowPosition */
	public int getRowPosition() {
		return hsCellEditor.getRowPosition();
	}

	/** @return the columnIndex */
	public int getColumnIndex() {
		return hsCellEditor.getColumnIndex();
	}

	/** @return the rowIndex */
	public int getRowIndex() {
		return hsCellEditor.getRowIndex();
	}
}

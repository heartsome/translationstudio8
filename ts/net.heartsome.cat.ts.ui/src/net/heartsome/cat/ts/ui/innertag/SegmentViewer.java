package net.heartsome.cat.ts.ui.innertag;

import static net.heartsome.cat.common.innertag.factory.PlaceHolderEditModeBuilder.PATTERN;
import static net.heartsome.cat.ts.ui.Constants.SEGMENT_LINE_SPACING;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;
import java.util.regex.Matcher;

import net.heartsome.cat.common.innertag.InnerTagBean;
import net.heartsome.cat.common.innertag.TagStyle;
import net.heartsome.cat.common.innertag.TagType;
import net.heartsome.cat.common.innertag.factory.IInnerTagFactory;
import net.heartsome.cat.common.innertag.factory.PlaceHolderEditModeBuilder;
import net.heartsome.cat.common.innertag.factory.XliffInnerTagFactory;
import net.heartsome.cat.common.ui.innertag.InnerTag;
import net.heartsome.cat.ts.core.Utils;
import net.heartsome.cat.ts.ui.Constants;
import net.heartsome.cat.ts.ui.bean.XliffEditorParameter;
import net.heartsome.cat.ts.ui.resource.Messages;
import net.sourceforge.nattable.util.GUIHelper;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.text.IUndoManager;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.jface.text.TextViewerUndoManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.PaintObjectEvent;
import org.eclipse.swt.custom.PaintObjectListener;
import org.eclipse.swt.custom.ST;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.TextStyle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

public class SegmentViewer extends TextViewer implements ISegmentViewer {

	private PlaceHolderEditModeBuilder placeHolderBuilder = new PlaceHolderEditModeBuilder();

	private ArrayList<InnerTag> innerTagCacheList = new ArrayList<InnerTag>();

	private String source;

	private int errorTagStart;

	private int sourceMaxTagIndex;

	private TagStyle tagStyle;

	public SegmentViewer(Composite parent, int styles, TagStyle tagStyle) {
		super(parent, styles);
		this.tagStyle = tagStyle;

		this.setDocument(new Document()); // 为TextViewer设置一个Document

		initListeners(); // 初始化监听器
	}

	private void initListeners() {
		StyledText styledText = getTextWidget();

		// 去掉默认的复制、粘贴键绑定，以实现在复制、粘贴前对标记的处理
		styledText.setKeyBinding('V' | SWT.MOD1, SWT.NULL);
		styledText.setKeyBinding(SWT.INSERT | SWT.MOD2, SWT.NULL);
		styledText.setKeyBinding('C' | SWT.MOD1, SWT.NULL);
		styledText.setKeyBinding(SWT.INSERT | SWT.MOD1, SWT.NULL);

		getDocument().addDocumentListener(new IDocumentListener() {

			public void documentChanged(DocumentEvent e) {

			}

			public void documentAboutToBeChanged(DocumentEvent event) {
				String method = belongToUndoOrRedo();
				if (event.getLength() > 0 && method != null) {
					try {
						String text = event.getDocument().get(event.getOffset(), event.getLength());
						Matcher matcher = PATTERN.matcher(text);
						List<InnerTag> cache = new ArrayList<InnerTag>();
						while (matcher.find()) {
							String placeHolder = matcher.group();
							InnerTag innerTag = InnerTagUtil.getInnerTag(getInnerTagCacheList(), placeHolder);
							if (innerTag != null && innerTag.isVisible()) {
								innerTag.setVisible(false);
								if ("undo".equals(method) && innerTag.getInnerTagBean().isWrongTag()) {
									cache.add(innerTag);
								}
							}
						}
						for (InnerTag t : cache) {
							innerTagCacheList.remove(t);
							t.dispose();
						}
					} catch (BadLocationException e) {
						e.printStackTrace();
					}
				}
			}

			/**
			 * 是否属于撤销或者重做的操作。
			 * @return ;
			 */
			private String belongToUndoOrRedo() {
				IUndoManager undoManager = getUndoManager();
				if (undoManager != null) {
					StackTraceElement[] stackTraceElements = new Throwable().getStackTrace();
					for (StackTraceElement stackTraceElement : stackTraceElements) {
						if (undoManager.getClass().getName().equals(stackTraceElement.getClassName())) {
							String methodName = stackTraceElement.getMethodName();
							if ("undo".equals(methodName) || "redo".equals(methodName)) {
								return methodName;
							}
						}
					}
				}
				return null;
			}
		});

		/**
		 * 处理在显示非打印隐藏字符的情况光标移动问题。兼容非打印字符替换符号
		 */
		styledText.addKeyListener(new KeyListener() {

			public void keyReleased(KeyEvent e) {
				if (!XliffEditorParameter.getInstance().isShowNonpirnttingCharacter()) {
					return;
				}
				if (e.stateMask == SWT.NONE && (e.keyCode == SWT.ARROW_UP || e.keyCode == SWT.ARROW_DOWN)) {
					StyledText styledText = (StyledText) e.widget;
					int offset = styledText.getCaretOffset();
					if (offset < 1 || offset >= styledText.getCharCount()) {
						return;
					}
					char c = styledText.getText().charAt(offset);
					char _c = styledText.getText().charAt(offset - 1);
					if (c == '\n' && (_c == Constants.LINE_SEPARATOR_CHARACTER)) {
						styledText.setCaretOffset(offset - 1);
					}
				}
			}

			public void keyPressed(KeyEvent e) {
				if (!XliffEditorParameter.getInstance().isShowNonpirnttingCharacter()) {
					return;
				}
				if (e.stateMask == SWT.NONE && (e.keyCode == SWT.ARROW_LEFT || e.keyCode == SWT.ARROW_RIGHT)) {
					StyledText styledText = (StyledText) e.widget;
					int offset = styledText.getCaretOffset();
					if (offset < 1 || offset >= styledText.getCharCount()) {
						return;
					}
					char c = styledText.getText().charAt(offset);
					char _c = styledText.getText().charAt(offset - 1);
					if ((c == '\u200B' && (_c == Constants.TAB_CHARACTER || _c == Constants.SPACE_CHARACTER))
							|| (c == '\n' && (_c == Constants.LINE_SEPARATOR_CHARACTER))) {
						if (e.keyCode == SWT.ARROW_LEFT) {
							styledText.setCaretOffset(offset - 1);
						} else if (e.keyCode == SWT.ARROW_RIGHT) {
							styledText.setCaretOffset(offset + 1);
						}
					}
				} else if (e.stateMask == SWT.CTRL && (e.keyCode == SWT.ARROW_LEFT || e.keyCode == SWT.ARROW_RIGHT)) {
					// 单独对 ctrl + right ,ctrl + left 换行的处理
					StyledText styledText = (StyledText) e.widget;
					int offset = styledText.getCaretOffset();
					char c = styledText.getText().charAt(offset);
					if (offset < 1 || offset >= styledText.getCharCount()) {
						return;
					}
					char _c = styledText.getText().charAt(offset - 1);
					if (c == '\n' && (_c == Constants.LINE_SEPARATOR_CHARACTER)) {
						if (e.keyCode == SWT.ARROW_LEFT) {
							styledText.setCaretOffset(offset - 1);
						} else if (e.keyCode == SWT.ARROW_RIGHT) {
							styledText.setCaretOffset(offset + 1);
						}
					}
				} else if ((e.stateMask == SWT.SHIFT || e.stateMask == (SWT.SHIFT | SWT.CTRL))
						&& (e.keyCode == SWT.ARROW_LEFT || e.keyCode == SWT.ARROW_RIGHT)) {
					StyledText styledText = (StyledText) e.widget;
					int offset = styledText.getCaretOffset();
					char c = styledText.getText().charAt(offset);
					if (offset < 1 || offset >= styledText.getCharCount()) {
						return;
					}
					char _c = styledText.getText().charAt(offset - 1);
					if ((c == '\u200B' && (_c == Constants.TAB_CHARACTER || _c == Constants.SPACE_CHARACTER))
							|| (c == '\n' && (_c == Constants.LINE_SEPARATOR_CHARACTER))) {
						if (e.keyCode == SWT.ARROW_LEFT) {
							styledText.invokeAction(ST.SELECT_COLUMN_PREVIOUS);
						} else if (e.keyCode == SWT.ARROW_RIGHT) {
							styledText.invokeAction(ST.SELECT_COLUMN_NEXT);
						}
					}
				} else if ((e.stateMask == SWT.SHIFT) && (e.keyCode == SWT.ARROW_UP || e.keyCode == SWT.ARROW_DOWN)) {
					StyledText styledText = (StyledText) e.widget;
					int offset = styledText.getCaretOffset();
					char c = styledText.getText().charAt(offset);
					if (offset < 1 || offset >= styledText.getCharCount()) {
						return;
					}
					char _c = styledText.getText().charAt(offset - 1);
					if (c == '\n' && (_c == Constants.LINE_SEPARATOR_CHARACTER)) {
						if (e.keyCode == SWT.ARROW_UP) {
							styledText.invokeAction(ST.SELECT_COLUMN_PREVIOUS);
						} else if (e.keyCode == SWT.ARROW_DOWN) {
							styledText.invokeAction(ST.SELECT_COLUMN_NEXT);
						}
					}
				}
			}
		});

		/**
		 * 处理在显示非打印隐藏字符的情况光标移动问题。兼容非打印字符替换符号
		 */
		styledText.addMouseListener(new MouseListener() {

			public void mouseUp(MouseEvent e) {
			}

			public void mouseDown(MouseEvent e) {
				if (!XliffEditorParameter.getInstance().isShowNonpirnttingCharacter()) {
					return;
				}
				StyledText styledText = (StyledText) e.widget;
				int offset = styledText.getCaretOffset();
				if (offset < 1 || offset >= styledText.getCharCount()) {
					return;
				}
				char c = styledText.getText().charAt(offset); // hidden character
				char _c = styledText.getText().charAt(offset - 1); // display character
				if ((_c == Constants.LINE_SEPARATOR_CHARACTER || _c == Constants.TAB_CHARACTER || _c == Constants.SPACE_CHARACTER)
						&& (c == '\n' || c == '\u200B')) {
					styledText.setCaretOffset(offset + 1);
				}
			}

			public void mouseDoubleClick(MouseEvent e) {
			}
		});

		/**
		 * 选择内容时对非打印字符的处理
		 */
		styledText.addMouseMoveListener(new MouseMoveListener() {

			public void mouseMove(MouseEvent e) {
				StyledText styledText = (StyledText) e.widget;
				int offset = styledText.getCaretOffset();
				if (offset < 1 || offset >= styledText.getCharCount()) {
					return;
				}
				char c = styledText.getText().charAt(offset);
				char _c = styledText.getText().charAt(offset - 1);
				if ((c == '\u200B' && (_c == Constants.TAB_CHARACTER || _c == Constants.SPACE_CHARACTER))
						|| (c == '\n' && (_c == Constants.LINE_SEPARATOR_CHARACTER))) {
					int caretOffset = styledText.getCaretOffset();
					Point p = styledText.getSelection();
					if (caretOffset == p.x) {
						styledText.invokeAction(ST.SELECT_COLUMN_PREVIOUS);
					} else if (caretOffset == p.y) {
						styledText.invokeAction(ST.SELECT_COLUMN_NEXT);
					}
				}
			}
		});

		styledText.addVerifyListener(new VerifyListener() {

			public void verifyText(final VerifyEvent e) {
				String t = e.text;
				if ((e.start == e.end) || (e.start != e.end && !e.text.equals(""))) { // 添加内容时
					if (XliffEditorParameter.getInstance().isShowNonpirnttingCharacter()) {
						t = t.replace("\t", Constants.TAB_CHARACTER + "\u200B").replace(" ",
								Constants.SPACE_CHARACTER + "\u200B");
						t = t.replace(System.getProperty("line.separator"), "\n");
						StringBuffer bf = new StringBuffer(t);
						int i = bf.indexOf("\n");
						if (i != -1) {
							if (i == 0) {
								bf.insert(i, Constants.LINE_SEPARATOR_CHARACTER);
							} else if (i != 0 && bf.charAt(i - 1) != Constants.LINE_SEPARATOR_CHARACTER) {
								bf.insert(i, Constants.LINE_SEPARATOR_CHARACTER);
							}
							i = bf.indexOf("\n", i + 1);
						}
						e.text = bf.toString();
					}
					return;
				}
				final StyledText styledText = (StyledText) e.widget;
				final String text = styledText.getText(e.start, e.end - 1);
				final Matcher matcher = PATTERN.matcher(text);
				if (matcher.find()) { // 被删除的部分中存在标记的的情况，进行特殊处理。
					if (isSource()) {
						e.doit = false;
						setToolTipMessage(Messages.getString("innertag.SegmentViewer.msg1"));
						return;
					}
					matcher.reset();
					styledText.getDisplay().syncExec(new Runnable() {
						public void run() {
							deleteInnerTagInPairs(e, matcher);
						}
					});
				}
				if (XliffEditorParameter.getInstance().isShowNonpirnttingCharacter()) {
					if (text.length() == 1 && (text.equals("\n") || text.indexOf('\u200B') != -1)) {
						char c = styledText.getText().charAt(e.start - 1);
						if (c == Constants.LINE_SEPARATOR_CHARACTER || c == Constants.SPACE_CHARACTER
								|| c == Constants.TAB_CHARACTER) {
							styledText.replaceTextRange(e.start - 1, 2, "");
							e.doit = false;
						}
					} else if (text.length() == 1
							&& (text.indexOf(Constants.LINE_SEPARATOR_CHARACTER) != -1
									|| text.indexOf(Constants.TAB_CHARACTER) != -1 || text
									.indexOf(Constants.TAB_CHARACTER) != -1)) {
						char c = styledText.getText().charAt(e.start + 1);
						if (c == '\n' || c == '\u200B') {
							styledText.replaceTextRange(e.start, 2, "");
							e.doit = false;
						}
					}
				}
			}

			/**
			 * 成对删除内部标记
			 */
			private void deleteInnerTagInPairs(final VerifyEvent e, Matcher matcher) {
				StyledText styledText = (StyledText) e.widget;
				ArrayList<Integer> tagIndexes = new ArrayList<Integer>(); // 记录被删除的标记的索引。
				while (matcher.find()) {
					String placeHolder = matcher.group();
					InnerTag innerTag = InnerTagUtil.getInnerTag(SegmentViewer.this.getInnerTagCacheList(), placeHolder);
					if (innerTag != null && innerTag.isVisible()) {
						innerTag.setVisible(false);

						// 保存成对标记中未完全删除的标记索引
						TagType tagType = innerTag.getInnerTagBean().getType();
						if (tagType == TagType.START || tagType == TagType.END) { // 处理成对标记的成对删除
							Integer tagIndex = Integer.valueOf(innerTag.getInnerTagBean().getIndex()); // 标记索引
							if (tagIndexes.contains(tagIndex)) { // 如果已经包含此索引，说明成对标记的2个部分都已经删除。
								tagIndexes.remove(tagIndex);
							} else { // 如果未包含此索引，则说明只删除了一个部分（开始或结束）的标记。
								tagIndexes.add(tagIndex);
							}
						}
					}
				}

				if (!tagIndexes.isEmpty()) { // 存在未删除的情况。
					getUndoManager().beginCompoundChange();

					e.doit = false; // 上一步已经修改，取消修改操作。
					styledText.getContent().replaceTextRange(e.start, e.end - e.start, e.text); // 替换改动内容

					for (int i = 0; i < errorTagStart; i++) { // 删除成对标记中未被删除的部分。
						InnerTag innerTag = innerTagCacheList.get(i);
						if (innerTag != null && innerTag.isVisible()) {
							if (tagIndexes.contains(innerTag.getInnerTagBean().getIndex())) {
								innerTag.setVisible(false);
								String placeHolder = placeHolderBuilder.getPlaceHolder(null, i);
								int start = -1;
								if ((start = styledText.getText().indexOf(placeHolder)) != -1) {
									styledText.getContent().replaceTextRange(start, placeHolder.length(), "");
								}

								tagIndexes.remove(Integer.valueOf(innerTag.getInnerTagBean().getIndex()));
								if (tagIndexes.isEmpty()) {
									break;
								}
							}
						}
					}
					getUndoManager().endCompoundChange();

					/**
					 * 通知更新主菜单（actionBar）中“撤销重做”等菜单项的状态，参见
					 * net.heartsome.cat.ts.ui.xliffeditor.nattable.editor.XLIFFEditorActionHandler
					 */
					styledText.notifyListeners(SWT.Selection, null);
				}
			}
		});

		/**
		 * 重绘时，将当前文本段中存在的内部标记对应的控件显示出来
		 */
		styledText.addPaintObjectListener(new PaintObjectListener() {
			public void paintObject(PaintObjectEvent event) {
				StyleRange styleRange = event.style;
				if (styleRange != null) {
					String text = ((StyledText) event.widget).getText();
					int end = styleRange.start + styleRange.length;
					if (text.length() < end) {
						return;
					}
					String styledString = text.substring(styleRange.start, end);
					Matcher matcher = PATTERN.matcher(styledString);
					if (matcher.matches()) {
						InnerTag tag = InnerTagUtil.getInnerTag(SegmentViewer.this.getInnerTagCacheList(), styledString);
						if (tag != null) {
							if (!tag.isVisible()) {
								tag.setVisible(true);
							}

							// int y = event.y + event.ascent - styleRange.metrics.ascent;
							int lineHeight = getTextWidget().getLineHeight();
							// innerTag.setLocation(event.x + SEGMENT_LINE_SPACING / 2, event.y + SEGMENT_LINE_SPACING /
							// 2 /* 行距的一半 */);
							int y = event.y + lineHeight / 2 - tag.getBounds().height / 2;
							tag.setLocation(event.x + SEGMENT_LINE_SPACING, y);
						}
					}
				}
			}
		});

		/**
		 * 鼠标移动时，清除错误消息。
		 */
		styledText.addMouseMoveListener(new MouseMoveListener() {
			public void mouseMove(MouseEvent e) {
				if (getTextWidget().getToolTipText() != null && getTextWidget().getToolTipText().length() > 0) {
					setToolTipMessage("");
				}
			}
		});

		// 处理修改内容时，需要非打印字符添加样式。
		styledText.addListener(SWT.Modify, new Listener() {

			public void handleEvent(Event event) {
				if (!XliffEditorParameter.getInstance().isShowNonpirnttingCharacter()) {
					return;
				}
				String s = event.text;
				Matcher matcher = Constants.NONPRINTING_PATTERN.matcher(s);
				TextStyle style = new TextStyle(null, GUIHelper.getColor(new RGB(100, 100, 100)), null);
				List<StyleRange> ranges = new ArrayList<StyleRange>();
				while (matcher.find()) {
					int start = event.start + matcher.start();
					StyleRange range = new StyleRange(style);
					range.start = start;
					range.length = 1;
					ranges.add(range);
				}
				for (StyleRange range : ranges) {
					getTextWidget().setStyleRange(range);
				}
			}
		});

		styledText.addListener(SWT.Selection, new Listener() {

			public void handleEvent(Event event) {
				for (InnerTag tag : innerTagCacheList) {
					if (tag.isSelected()) {
						tag.setSelected(false);
						tag.redraw();
					}
				}
				String styledString = getTextWidget().getSelectionText();
				Matcher matcher = PATTERN.matcher(styledString);
				while (matcher.find()) {
					String s = matcher.group();
					InnerTag tag = InnerTagUtil.getInnerTag(SegmentViewer.this.getInnerTagCacheList(), s);
					if (tag != null) {
						tag.setSelected(true);
						tag.redraw();
					}
				}
			}
		});
	}

	/**
	 * 得到内部标记缓存集合。此 List 不可排序，排序后会影响显示结果。
	 * @see net.heartsome.cat.ts.ui.innertag.ISegmentViewer#getInnerTagCacheList()
	 */
	public List<InnerTag> getInnerTagCacheList() {
		return innerTagCacheList;
	}

	/**
	 * @see net.heartsome.cat.ts.ui.innertag.ISegmentViewer#getPureText()
	 */
	public String getPureText() {
		if (getTextWidget() == null) {
			return "";
		}
		String text = getTextWidget().getText();
		if (XliffEditorParameter.getInstance().isShowNonpirnttingCharacter()) {
			text = text.replaceAll(Utils.getLineSeparator(), "\n");
			text = text.replaceAll(Constants.LINE_SEPARATOR_CHARACTER + "", "");
			text = text.replaceAll(Constants.TAB_CHARACTER + "\u200B", "\t");
			text = text.replaceAll(Constants.SPACE_CHARACTER + "\u200B", " ");
		}
		return PATTERN.matcher(text).replaceAll("");
	}

	/**
	 * @see net.heartsome.cat.ts.ui.innertag.ISegmentViewer#getText()
	 */
	public String getText() {
		if (getTextWidget() == null) {
			return "";
		}
		String text = getTextWidget().getText();
		if (text == null) {
			return "";
		}
		return convertDisplayTextToOriginalText(cleanRegularString(text));
	}

	/**
	 * 转换显示文本为原始文本。
	 * @param displayText
	 *            显示文本
	 * @return XML 中的原始文本;
	 */
	public String convertDisplayTextToOriginalText(String displayText) {
		if (displayText == null || displayText.length() == 0) {
			return "";
		}
		Matcher matcher = PATTERN.matcher(displayText);
		int offset = 0;
		StringBuffer sb = new StringBuffer(displayText);
		while (matcher.find()) {
			String placeHolder = matcher.group();
			InnerTag innerTag = InnerTagUtil.getInnerTag(innerTagCacheList, placeHolder);
			if (innerTag != null) {
				String tagContent = innerTag.getInnerTagBean().getContent();
				// tagContent = tagContent.replaceAll("&amp;", "&");
				int start = matcher.start() + offset;
				int end = matcher.end() + offset;
				sb.replace(start, end, tagContent);
				offset += tagContent.length() - 1;
			}
		}
		return sb.toString();
	};

	/**
	 * 给TextViewer设置UndoManager
	 * @param textViewer
	 *            ;
	 */
	public void initUndoManager(int undoLevel) {
		// remembers edit commands
		final TextViewerUndoManager undoManager = new TextViewerUndoManager(undoLevel);
		// add listeners
		undoManager.connect(this);
		this.setUndoManager(undoManager);
	}

	/**
	 * @see net.heartsome.cat.ts.ui.innertag.ISegmentViewer#setSource(java.lang.String)
	 */
	public void setSource(String source) {
		this.source = (source != null ? source : "");
	}

	private boolean isSource() {
		return source == null;
	}

	/**
	 * @see net.heartsome.cat.ts.ui.innertag.ISegmentViewer#setText(java.lang.String)
	 */
	public void setText(String text) {
		reset(); // 重置缓存的标记。

		if (isSource()) { // 当前是源文本
			IInnerTagFactory innerTagFactory = new XliffInnerTagFactory(text, placeHolderBuilder);
			List<InnerTagBean> innerTagBeans = innerTagFactory.getInnerTagBeans();
			for (InnerTagBean innerTagBean : innerTagBeans) {
				InnerTag innerTag = InnerTagUtil.createInnerTagControl(getTextWidget(), innerTagBean, tagStyle);
				innerTag.setVisible(false);
				innerTagCacheList.add(innerTag);

				sourceMaxTagIndex = Math.max(sourceMaxTagIndex, innerTagBean.getIndex());
			}
			errorTagStart = innerTagCacheList.size(); // 记录错误标记的起始，source 中所有标记都不是错误标记。

			text = innerTagFactory.getText();
		} else { // 当前是目标文本
			XliffInnerTagFactory innerTagFactory = new XliffInnerTagFactory(source, placeHolderBuilder);
			// source 中包含的内部标记。
			List<InnerTagBean> sourceInnerTagBeans = innerTagFactory.getInnerTagBeans();
			errorTagStart = sourceInnerTagBeans.size(); // 记录错误标记的起始，source 中所有标记都不是错误标记。
			// 如果当前是 Target，就先记录 Source 中的最大索引。
			for (InnerTagBean innerTagBean : sourceInnerTagBeans) { // 记录source中内部标记的最大标记索引。
				sourceMaxTagIndex = Math.max(sourceMaxTagIndex, innerTagBean.getIndex());
			}

			text = innerTagFactory.parseInnerTag(text, true);
			List<InnerTagBean> innerTagBeans = innerTagFactory.getInnerTagBeans();
			for (InnerTagBean innerTagBean : innerTagBeans) {
				InnerTag innerTag = InnerTagUtil.createInnerTagControl(getTextWidget(), innerTagBean, tagStyle);
				innerTag.setVisible(false);
				innerTagCacheList.add(innerTag);
			}
		}
		getTextWidget().setText(resetRegularString(text));
	}

	private String cleanRegularString(String input) {
		input = input.replaceAll("&", "&amp;"); //$NON-NLS-1$ //$NON-NLS-2$
		input = input.replaceAll("<", "&lt;"); //$NON-NLS-1$ //$NON-NLS-2$
		input = input.replaceAll(">", "&gt;"); //$NON-NLS-1$ //$NON-NLS-2$
		if (XliffEditorParameter.getInstance().isShowNonpirnttingCharacter()) {
			input = input.replaceAll(Utils.getLineSeparator(), "\n");
			input = input.replaceAll(Constants.LINE_SEPARATOR_CHARACTER + "", "");
			input = input.replaceAll(Constants.TAB_CHARACTER + "\u200B", "\t");
			input = input.replaceAll(Constants.SPACE_CHARACTER + "\u200B", " ");
		}
		return input;
	}

	private String resetRegularString(String input) {
		input = input.replaceAll("&amp;", "&"); //$NON-NLS-1$ //$NON-NLS-2$
		input = input.replaceAll("&lt;", "<"); //$NON-NLS-1$ //$NON-NLS-2$
		input = input.replaceAll("&gt;", ">"); //$NON-NLS-1$ //$NON-NLS-2$
		input = input.replaceAll(Utils.getLineSeparator(), "\n");
		if (XliffEditorParameter.getInstance().isShowNonpirnttingCharacter()) {
			input = input.replaceAll("\\n", Constants.LINE_SEPARATOR_CHARACTER + "\n");
			input = input.replaceAll("\\t", Constants.TAB_CHARACTER + "\u200B");
			input = input.replaceAll(" ", Constants.SPACE_CHARACTER + "\u200B");
		}
		return input;

	}

	/**
	 * 重置 ;
	 */
	public void reset() {
		StyledText styledText = getTextWidget();
		Control[] children = styledText.getChildren();
		for (Control child : children) {
			if (child != null && !child.isDisposed()) {
				child.dispose();
			}
		}
		innerTagCacheList.clear();
		errorTagStart = 0;
	}

	/**
	 * 得到实际的光标位置（StyledText中的文本有一部分是已经被转换成内部标记的，与XML文本的分割位置有差异，因此需要此方法得到在XML中实际的分割位置）
	 * @return 实际光标所在的位置;
	 */
	public int getRealSplitOffset() {
		StyledText styledText = getTextWidget();
		if (styledText == null) {
			return 0;
		}
		return getRealSplitOffset(styledText.getCaretOffset());
	}

	/**
	 * 根据传入的光标位置得到实际的光标位置（StyledText中的文本有一部分是已经被转换成内部标记的，与XML文本的分割位置有差异，因此需要此方法得到在XML中实际的分割位置）
	 * @return 实际光标所在的位置;
	 */
	public int getRealSplitOffset(int caretOffset) {
		StyledText styledText = getTextWidget();
		if (styledText == null) {
			return 0;
		}
		String text = styledText.getText();

		// 当 caretOffset 之前有转义字符时，caretOffset 要转换成实际位置
		String subText = text.substring(0, caretOffset);
		subText = cleanRegularString(subText);
		// 下面两行不能颠倒
		int offset = caretOffset;
		caretOffset = subText.length();

		Matcher matcher = PATTERN.matcher(styledText.getText());
		while (matcher.find()) {
			String placeHolder = matcher.group();
			int start = matcher.start();
			// 当光标在标记前或后时，获取实际光标位置会有问题，此处添加判断
			if (start >= offset) {
				return caretOffset;
			}
			InnerTag innerTag = InnerTagUtil.getInnerTag(innerTagCacheList, placeHolder);
			if (innerTag != null) {
				String tagContent = innerTag.getInnerTagBean().getContent();
				int length = tagContent.length() - placeHolder.length();
				caretOffset += length;
			}
		}
		return caretOffset;
	}

	/**
	 * @see net.heartsome.cat.ts.ui.innertag.ISegmentViewer#insertInnerTag(int, int)
	 */
	public void insertInnerTag(int tagIndex, int offset) {
		StringBuffer placeHolders = new StringBuffer();
		int caretOffset = offset;
		out: for (int i = 0; i < errorTagStart; i++) {
			InnerTagBean innerTagBean = innerTagCacheList.get(i).getInnerTagBean();
			if (innerTagBean.getIndex() == tagIndex) {
				String placeHolder = placeHolderBuilder.getPlaceHolder(null, i);
				int index = -1;
				if ((index = getTextWidget().getText().indexOf(placeHolder)) > -1) {
					// 使用 StyledTextContent 的 replaceTextRange 方法，不会触发成对删除标记的处理
					getTextWidget().getContent().replaceTextRange(index, placeHolder.length(), "");
					if (index < offset) {
						offset -= placeHolder.length();
						caretOffset -= placeHolder.length();
					}
				}
				boolean isEmpty = placeHolders.length() <= 0;
				switch (innerTagBean.getType()) {
				case START:
					placeHolders.insert(0, placeHolder);
					caretOffset += placeHolder.length();
					if (isEmpty) {
						break;
					} else {
						break out;
					}
				case END:
					placeHolders.append(placeHolder);
					if (isEmpty) {
						break;
					} else {
						break out;
					}
				case STANDALONE:
					placeHolders.append(placeHolder);
					caretOffset += placeHolder.length();
					break out;
				default:
					break;
				}
			}
		}
		if (placeHolders.length() > 0) {
			getUndoManager().beginCompoundChange(); // 撤销/重做控制。

			if (placeHolders.length() == 2) {
				// 成对标记包裹选中内容
				placeHolders.insert(1, getSelectedPureText());
				Point p = getSelectedRange();
				getTextWidget().replaceTextRange(p.x, p.y, placeHolders.toString());
			} else {
				getTextWidget().replaceTextRange(offset, 0, placeHolders.toString());
			}
			getTextWidget().setCaretOffset(caretOffset);

			getUndoManager().endCompoundChange();
		}
	}

	/**
	 * @see net.heartsome.cat.ts.ui.innertag.ISegmentViewer#getErrorTagStart()
	 */
	public int getErrorTagStart() {
		return errorTagStart;
	}

	/**
	 * @see net.heartsome.cat.ts.ui.innertag.ISegmentViewer#getSourceMaxTagIndex()
	 */
	public int getSourceMaxTagIndex() {
		return sourceMaxTagIndex;
	}

	/**
	 * 得到当前显示的所有内部标记控件。此 List 可以排序。
	 * @see net.heartsome.cat.ts.ui.innertag.ISegmentViewer#getCurrentInnerTags()
	 */
	public List<InnerTag> getCurrentInnerTags() {
		if (innerTagCacheList.isEmpty()) {
			return Collections.emptyList();
		}
		ArrayList<InnerTag> currentInnerTags = new ArrayList<InnerTag>();
		for (InnerTag innerTag : innerTagCacheList) {
			if (innerTag.isVisible()) {
				currentInnerTags.add(innerTag);
			}
		}
		return currentInnerTags;
	}

	/**
	 * 删除所有内部标记;
	 */
	public void clearAllInnerTags() {
		if (isSource()) {
			setToolTipMessage(Messages.getString("innertag.SegmentViewer.msg1"));
			return; // Source 中不允许删除标记
		}
		setText(getPureText());
	}

	/**
	 * 设置工具提示信息
	 * @param message
	 *            提示信息;
	 */
	public void setToolTipMessage(String message) {
		if (message == null) {
			message = "";
		}
		getTextWidget().setToolTipText(message);
	}

	/**
	 * 设置标记样式
	 * @param tagStyle
	 *            标记样式;
	 */
	public void setTagStyle(TagStyle tagStyle) {
		if (this.tagStyle == null) {
			this.tagStyle = TagStyle.getDefault();
		}
		if (!this.tagStyle.equals(tagStyle)) {
			this.tagStyle = tagStyle;
			if (!innerTagCacheList.isEmpty()) {
				for (InnerTag innerTag : innerTagCacheList) {
					// innerTag.setStyle(SegmentViewer.this.tagStyle);
					innerTag.pack(); // 重新计算标记大小。
				}
				SegmentViewer.this.invalidateTextPresentation(); // 重新计算并添加样式。
			}
		}
	}

	public TagStyle getTagStyle() {
		return tagStyle;
	}

	/**
	 * 得到选中的原始文本。
	 * @return XML中的原始内容;
	 */
	public String getSelectedOriginalText() {
		if (getTextWidget() == null) {
			return "";
		}
		String selectionText = getTextWidget().getSelectionText();
		if (XliffEditorParameter.getInstance().isShowNonpirnttingCharacter()) {
			selectionText = selectionText.replaceAll(Utils.getLineSeparator(), "\n");
			selectionText = selectionText.replaceAll(Constants.LINE_SEPARATOR_CHARACTER + "", "");
			selectionText = selectionText.replaceAll(Constants.TAB_CHARACTER + "\u200B", "\t");
			selectionText = selectionText.replaceAll(Constants.SPACE_CHARACTER + "\u200B", " ");
		}
		return convertDisplayTextToOriginalText(selectionText);
	}

	/**
	 * 得到选中的纯文本
	 * @return ;
	 */
	public String getSelectedPureText() {
		if (getTextWidget() == null) {
			return "";
		}
		String selectionText = getTextWidget().getSelectionText();
		if (XliffEditorParameter.getInstance().isShowNonpirnttingCharacter()) {
			selectionText = selectionText.replaceAll(Utils.getLineSeparator(), "\n");
			selectionText = selectionText.replaceAll(Constants.LINE_SEPARATOR_CHARACTER + "", "");
			selectionText = selectionText.replaceAll(Constants.TAB_CHARACTER + "\u200B", "\t");
			selectionText = selectionText.replaceAll(Constants.SPACE_CHARACTER + "\u200B", " ");
		}
		return PATTERN.matcher(selectionText).replaceAll("");
	}

	/**
	 * 重载此方法，实现粘贴、复制前对标记的处理。<br>
	 * 已经移出了 默认的复制、粘贴事件键绑定, 参考{@link CellEditorTextViewer#initListener()}
	 * @see org.eclipse.jface.text.TextViewer#doOperation(int)
	 */
	@Override
	public void doOperation(int operation) {
		if (operation == ITextOperationTarget.PASTE) {
			parse();
			return;
		}
		if (operation == ITextOperationTarget.COPY) {
			copy();
			return;
		}
		super.doOperation(operation);
	}

	/**
	 * 执行复制时对标记的处理，复制后在OS系统中不能包含标记占位符 ;
	 */
	private void copy() {
		super.doOperation(ITextOperationTarget.COPY);
		TextTransfer plainTextTransfer = TextTransfer.getInstance();
		XLiffTextTransfer hsTextTransfer = XLiffTextTransfer.getInstance();
		Clipboard clipboard = new Clipboard(getTextWidget().getDisplay());
		String plainText = (String) clipboard.getContents(plainTextTransfer);
		if (plainText == null || plainText.length() == 0) {
			return;
		}
		plainText = plainText.replaceAll(Utils.getLineSeparator(), "\n");
		plainText = plainText.replaceAll(Constants.LINE_SEPARATOR_CHARACTER + "", "");
		plainText = plainText.replaceAll(Constants.TAB_CHARACTER + "", "\t");
		plainText = plainText.replaceAll(Constants.SPACE_CHARACTER + "", " ");
		plainText = plainText.replaceAll("\u200B", "");
		clipboard.clearContents();
		Object[] data = new Object[] { PATTERN.matcher(plainText).replaceAll(""), plainText };
		Transfer[] types = new Transfer[] { plainTextTransfer, hsTextTransfer };

		clipboard.setContents(data, types, DND.CLIPBOARD);
		clipboard.dispose();
	}

	/**
	 * 执行粘贴前对标记的处理 ;
	 */
	private void parse() {
		Clipboard clipboard = null;
		try {
			if (getTextWidget().isDisposed()) {
				return;
			}
			clipboard = new Clipboard(getTextWidget().getDisplay());
			XLiffTextTransfer hsTextTransfer = XLiffTextTransfer.getInstance();
			String hsText = (String) clipboard.getContents(hsTextTransfer);
			String osText = (String) clipboard.getContents(TextTransfer.getInstance());
			if (hsText == null || hsText.length() == 0) {
				if (osText == null || osText.length() == 0) {
					return;
				}
				super.doOperation(ITextOperationTarget.PASTE);
				return;
			}
			String clearedTagText = hsText;
			String selText = getTextWidget().getSelectionText();
			if (selText.equals(hsText)) {
				return;
			}
			if (getTextWidget().getSelectionCount() != getTextWidget().getText().length()) {
				clearedTagText = filterInnerTag(hsText); // 过滤掉系统剪切板中的标记。
			} else {
				StringBuffer bf = new StringBuffer(hsText);
				Matcher matcher = PATTERN.matcher(hsText);
				List<String> needRemove = new ArrayList<String>();
				while (matcher.find()) {
					String placeHolder = matcher.group();
					InnerTag tag = InnerTagUtil.getInnerTag(innerTagCacheList, placeHolder);
					if (tag == null) {
						needRemove.add(placeHolder);
					}
				}
				clearedTagText = bf.toString();
				for (String r : needRemove) {
					clearedTagText = clearedTagText.replaceAll(r, "");
				}
			}

			if (clearedTagText == null || clearedTagText.length() == 0) {
				return;
			}

			if (clearedTagText.equals(osText)) {
				super.doOperation(ITextOperationTarget.PASTE);
				return;
			}

			Object[] data = new Object[] { clearedTagText, hsText };
			Transfer[] types = new Transfer[] { TextTransfer.getInstance(), hsTextTransfer };
			try {
				clipboard.setContents(data, types);
			} catch (Exception e) {
				e.printStackTrace();
			}

			super.doOperation(ITextOperationTarget.PASTE);

			data = new Object[] { osText, hsText };
			try {
				clipboard.setContents(data, types);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} finally {
			if (clipboard != null && !clipboard.isDisposed()) {
				clipboard.dispose();
			}
		}
	}

	/**
	 * 过滤掉系统剪切板中的标记
	 */
	private String filterInnerTag(String contents) {
		if (contents == null) {
			return contents;
		}
		List<InnerTag> cacheTags = getInnerTagCacheList();
		String fullText = getTextWidget().getText();

		Matcher matcher = PATTERN.matcher(contents);
		Stack<InnerTag> stack = new Stack<InnerTag>();
		Stack<String> phStack = new Stack<String>();
		List<String> needRemove = new ArrayList<String>();
		while (matcher.find()) {
			String placeHolder = matcher.group();
			InnerTag tag = InnerTagUtil.getInnerTag(cacheTags, placeHolder);
			if (tag == null) {
				needRemove.add(placeHolder);
				continue;
			}
			if (tag.getInnerTagBean().getType() == TagType.START) {
				stack.push(tag);
				phStack.push(placeHolder);
				continue;
			} else if (tag.getInnerTagBean().getType() == TagType.END) {
				if (stack.isEmpty()) {
					// 只有结束 没有开始
					needRemove.add(placeHolder);
					continue;
				}
				InnerTag _tag = stack.pop();
				String _placeHolder = phStack.pop();
				if (tag.getInnerTagBean().getIndex() != _tag.getInnerTagBean().getIndex()) {
					needRemove.add(placeHolder);
					needRemove.add(_placeHolder);
					continue;
				}
				int start = -1;
				if ((start = fullText.indexOf(_placeHolder)) != -1) {
					getTextWidget().replaceTextRange(start, _placeHolder.length(), "");
					fullText = getTextWidget().getText();
				}
				if ((start = fullText.indexOf(placeHolder)) != -1) {
					getTextWidget().replaceTextRange(start, placeHolder.length(), "");
					fullText = getTextWidget().getText();
				}
			} else {
				int start = -1;
				if ((start = fullText.indexOf(placeHolder)) != -1) {
					getTextWidget().replaceTextRange(start, placeHolder.length(), "");
					fullText = getTextWidget().getText();
				}
			}
		}

		while (!stack.isEmpty()) {
			needRemove.add(InnerTagUtil.getPlaceHolder(getInnerTagCacheList(), stack.pop().getInnerTagBean()));
		}
		for (String r : needRemove) {
			contents = contents.replaceAll(r, "");
		}
		return contents;
	}
}

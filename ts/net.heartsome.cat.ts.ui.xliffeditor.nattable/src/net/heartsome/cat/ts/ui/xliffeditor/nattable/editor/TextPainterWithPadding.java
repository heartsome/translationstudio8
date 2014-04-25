package net.heartsome.cat.ts.ui.xliffeditor.nattable.editor;

import static net.heartsome.cat.ts.ui.Constants.SEGMENT_LINE_SPACING;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.regex.Matcher;

import net.heartsome.cat.common.bean.ColorConfigBean;
import net.heartsome.cat.common.innertag.InnerTagBean;
import net.heartsome.cat.common.innertag.factory.PlaceHolderEditModeBuilder;
import net.heartsome.cat.common.innertag.factory.XliffInnerTagFactory;
import net.heartsome.cat.common.ui.innertag.InnerTagRender;
import net.heartsome.cat.common.ui.utils.InnerTagUtil;
import net.heartsome.cat.ts.ui.Constants;
import net.heartsome.cat.ts.ui.bean.XliffEditorParameter;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.resource.Messages;
import net.sourceforge.nattable.config.CellConfigAttributes;
import net.sourceforge.nattable.config.IConfigRegistry;
import net.sourceforge.nattable.data.convert.IDisplayConverter;
import net.sourceforge.nattable.layer.cell.LayerCell;
import net.sourceforge.nattable.painter.cell.BackgroundPainter;
import net.sourceforge.nattable.style.CellStyleAttributes;
import net.sourceforge.nattable.style.CellStyleUtil;
import net.sourceforge.nattable.style.IStyle;
import net.sourceforge.nattable.util.GUIHelper;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.GlyphMetrics;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.TextLayout;
import org.eclipse.swt.graphics.TextStyle;
import org.eclipse.swt.widgets.Display;

/**
 * 文本绘画器——用于绘制 NatTable 的单元格内容。
 * @author weachy
 * @version
 * @since JDK1.5
 */
public class TextPainterWithPadding extends BackgroundPainter {
	private final int topPadding;
	private final int rightPadding;
	private final int bottomPadding;
	private final int leftPadding;
	private final int tabSize = 4;
	private int tabWidth;

	private PlaceHolderEditModeBuilder placeHolderBuilder = new PlaceHolderEditModeBuilder();
	private InnerTagRender tagRender;

	private static Map<String, Integer> temporaryMap = new WeakHashMap<String, Integer>();

	private static Map<Font, FontData[]> fontDataCache = new WeakHashMap<Font, FontData[]>();

	private final boolean wrapText;

	private XLIFFEditorImplWithNatTable editor;

	private XliffInnerTagFactory innerTagFactory = new XliffInnerTagFactory(placeHolderBuilder);

	private Font font;
	private int ascent, descent;

	public TextPainterWithPadding(XLIFFEditorImplWithNatTable editor) {
		this(true, 0, editor, null);
	}

	public TextPainterWithPadding(boolean wrapText, XLIFFEditorImplWithNatTable editor, Font font) {
		this(wrapText, 0, editor, font);
	}

	public TextPainterWithPadding(boolean wrapText, int padding, XLIFFEditorImplWithNatTable editor, Font font) {
		this(wrapText, padding, padding, padding, padding, editor, font);
	}

	public TextPainterWithPadding(boolean wrapText, int topPadding, int rightPadding, int bottomPadding,
			int leftPadding, final XLIFFEditorImplWithNatTable editor, Font font) {
		Assert.isNotNull(editor.getTable(), Messages.getString("editor.TextPainterWithPadding.msg1"));
		this.wrapText = wrapText;
		this.topPadding = topPadding;
		this.rightPadding = rightPadding;
		this.bottomPadding = bottomPadding;
		this.leftPadding = leftPadding;
		this.editor = editor;
		if (font == null) {
			font = JFaceResources.getFont(net.heartsome.cat.ts.ui.Constants.XLIFF_EDITOR_TEXT_FONT);
		}
		setFont(font);

		tagRender = new InnerTagRender();
	}

	public int getPreferredHeight(LayerCell cell, GC gc, IConfigRegistry configRegistry) {
		if (innerTagFactory == null) {
			innerTagFactory = new XliffInnerTagFactory(placeHolderBuilder);
		}
		innerTagFactory.reset();
		TextLayout layout = getCellTextLayout(cell);

		int counts = layout.getLineCount();
		int contentHeight = 0;
		for (int i = 0; i < counts; i++) {
			contentHeight += layout.getLineBounds(i).height;
		}
		layout.dispose();
		contentHeight += Math.max(counts - 1, 0) * SEGMENT_LINE_SPACING;
		contentHeight += 4;// 加上编辑模式下，StyledTextCellEditor的边框
		contentHeight += topPadding;
		contentHeight += bottomPadding;
		return contentHeight;
	}

	/**
	 * (non-Javadoc)
	 * @see net.sourceforge.nattable.painter.cell.BackgroundPainter#paintCell(net.sourceforge.nattable.layer.cell.LayerCell,
	 *      org.eclipse.swt.graphics.GC, org.eclipse.swt.graphics.Rectangle,
	 *      net.sourceforge.nattable.config.IConfigRegistry)
	 */
	@Override
	public void paintCell(LayerCell cell, GC gc, Rectangle rectangle, IConfigRegistry configRegistry) {
		super.paintCell(cell, gc, rectangle, configRegistry);
		IStyle cellStyle = CellStyleUtil.getCellStyle(cell, configRegistry);
		setupGCFromConfig(gc, cellStyle);

		if (innerTagFactory == null) {
			innerTagFactory = new XliffInnerTagFactory(placeHolderBuilder);
		}
		innerTagFactory.reset();

		int rowIndex = cell.getLayer().getRowIndexByPosition(cell.getRowPosition());
		int columnIndex = cell.getLayer().getColumnIndexByPosition(cell.getColumnPosition());
		if (!editor.isHorizontalLayout()) {
			// 垂直
			if (rowIndex % 2 != 0) {
				LayerCell srcCell = cell.getLayer().getCellByPosition(cell.getColumnPosition(),
						cell.getRowPosition() - 1);
				if (srcCell != null) {
					String sourceVal = (String) srcCell.getDataValue();
					innerTagFactory.parseInnerTag(sourceVal);
				}
			}
		} else {
			// 水平
			if (columnIndex == editor.getTgtColumnIndex()) {
				LayerCell srcCell = cell.getLayer().getCellByPosition(1, cell.getRowPosition());
				if (srcCell != null) {
					String sourceVal = (String) srcCell.getDataValue();
					innerTagFactory.parseInnerTag(sourceVal);
				}
			}
		}
		TextLayout layout = getCellTextLayout(cell);
		int tempIndx = rowIndex;
		if (!editor.isHorizontalLayout()) {
			tempIndx = tempIndx / 2;
		}
		if (tempIndx == editor.getSelectedRows()[0]
				&& (editor.isHorizontalLayout() ? columnIndex == editor.getSrcColumnIndex() : rowIndex % 2 == 0)) {
			List<String> terms = editor.getTermsCache().get(tempIndx);
			if (terms != null && terms.size() > 0) {
				List<StyleRange> ranges = new ArrayList<StyleRange>();
				TextStyle style = new TextStyle(getFont(), null, ColorConfigBean.getInstance()
						.getHighlightedTermColor());
				char[] source = layout.getText().toCharArray();
				for (String term : terms) {
					if (XliffEditorParameter.getInstance().isShowNonpirnttingCharacter()) {
						term = term.replaceAll("\\n", Constants.LINE_SEPARATOR_CHARACTER + "\n");
						term = term.replaceAll("\\t", Constants.TAB_CHARACTER + "\u200B");
						term = term.replaceAll(" ", Constants.SPACE_CHARACTER + "\u200B");
					}
					ranges.addAll(calculateTermsStyleRange(source, term.toCharArray(), style));
				}
				for (StyleRange range : ranges) {
					layout.setStyle(range, range.start, range.start + range.length - 1);
				}
			}
		}
		try {
			String displayText = layout.getText();
			Rectangle bounds = cell.getBounds();
			if (XliffEditorParameter.getInstance().isShowNonpirnttingCharacter()) {
				appendNonprintingStyle(layout);
			}
			layout.draw(gc, bounds.x + leftPadding, bounds.y + topPadding);
			List<InnerTagBean> innerTagBeans = innerTagFactory.getInnerTagBeans();
			for (InnerTagBean innerTagBean : innerTagBeans) {
				String placeHolder = placeHolderBuilder.getPlaceHolder(innerTagBeans,
						innerTagBeans.indexOf(innerTagBean));
				int start = displayText.indexOf(placeHolder);
				if (start == -1) {
					continue;
				}
				Point p = layout.getLocation(start, false);
				int x = bounds.x + p.x + leftPadding;
				x += SEGMENT_LINE_SPACING;

				Point tagSize = tagRender.calculateTagSize(innerTagBean);
				int lineIdx = layout.getLineIndex(start);
				Rectangle r = layout.getLineBounds(lineIdx);
				int y = bounds.y + p.y + topPadding + r.height / 2 - tagSize.y / 2;// -
																					// layout.getLineMetrics(0).getDescent();
				// if (y + r.height > tagSize.y) {
				// FontMetrics fm = layout.getLineMetrics(lineIdx);
				// y = y + r.height - tagSize.y - fm.getDescent();
				// }
				tagRender.draw(gc, innerTagBean, x, y);
			}
		} finally {
			layout.dispose();
		}
	}

	@Override
	public int getPreferredWidth(LayerCell cell, GC gc, IConfigRegistry configRegistry) {
		setupGCFromConfig(gc, CellStyleUtil.getCellStyle(cell, configRegistry));
		return leftPadding + getWidthFromCache(gc, convertDataType(cell, configRegistry)) + rightPadding;
	}

	public void setupGCFromConfig(GC gc, IStyle cellStyle) {
		Color fg = cellStyle.getAttributeValue(CellStyleAttributes.FOREGROUND_COLOR);
		Color bg = cellStyle.getAttributeValue(CellStyleAttributes.BACKGROUND_COLOR);

		gc.setAntialias(GUIHelper.DEFAULT_ANTIALIAS);
		gc.setTextAntialias(GUIHelper.DEFAULT_TEXT_ANTIALIAS);
		gc.setFont(font);
		gc.setForeground(fg != null ? fg : GUIHelper.COLOR_LIST_FOREGROUND);
		gc.setBackground(bg != null ? bg : GUIHelper.COLOR_LIST_BACKGROUND);
	}

	public Font getFont() {
		return font;
	}

	public void setFont(Font font) {
		TextLayout layout = new TextLayout(Display.getDefault());
		try {
			if (font != null) {
				this.font = font;
				Font boldFont = getFont(SWT.BOLD), italicFont = getFont(SWT.ITALIC), boldItalicFont = getFont(SWT.BOLD
						| SWT.ITALIC);
				layout.setText("    ");
				layout.setFont(font);
				layout.setStyle(new TextStyle(font, null, null), 0, 0);
				layout.setStyle(new TextStyle(boldFont, null, null), 1, 1);
				layout.setStyle(new TextStyle(italicFont, null, null), 2, 2);
				layout.setStyle(new TextStyle(boldItalicFont, null, null), 3, 3);
				FontMetrics metrics = layout.getLineMetrics(0);
				ascent = metrics.getAscent() + metrics.getLeading();
				descent = metrics.getDescent();
				boldFont.dispose();
				italicFont.dispose();
				boldItalicFont.dispose();
				boldFont = italicFont = boldItalicFont = null;
			}
			layout.dispose();
			layout = new TextLayout(Display.getDefault());
			layout.setFont(this.font);
			StringBuffer tabBuffer = new StringBuffer(tabSize);
			for (int i = 0; i < tabSize; i++) {
				tabBuffer.append(' ');
			}
			layout.setText(tabBuffer.toString());
			tabWidth = layout.getBounds().width;
			layout.dispose();
		} finally {
			if (layout != null && !layout.isDisposed()) {
				layout.dispose();
			}
		}
	}

	/**
	 * Convert the data value of the cell using the {@link IDisplayConverter} from the {@link IConfigRegistry}
	 */
	protected String convertDataType(LayerCell cell, IConfigRegistry configRegistry) {
		IDisplayConverter displayConverter = configRegistry.getConfigAttribute(CellConfigAttributes.DISPLAY_CONVERTER,
				cell.getDisplayMode(), cell.getConfigLabels().getLabels());
		if (displayConverter instanceof TagDisplayConverter) {
			((TagDisplayConverter) displayConverter).setCell(cell);
		}
		String text = displayConverter != null ? (String) displayConverter.canonicalToDisplayValue(cell.getDataValue())
				: null;
		return (text == null) ? "" : text;
	}

	private TextLayout getCellTextLayout(LayerCell cell) {
		int orientation = editor.getTable().getStyle() & (SWT.LEFT_TO_RIGHT | SWT.RIGHT_TO_LEFT);
		TextLayout layout = new TextLayout(editor.getTable().getDisplay());
		layout.setOrientation(orientation);
		layout.setSpacing(Constants.SEGMENT_LINE_SPACING);
		layout.setFont(font);
		layout.setAscent(ascent);
		layout.setDescent(descent); // 和 StyledTextEditor 同步
		layout.setTabs(new int[] { tabWidth });

		Rectangle rectangle = cell.getBounds();
		int width = rectangle.width - leftPadding - rightPadding;
		width -= 1;
		if (wrapText && width > 0) {
			layout.setWidth(width);
		}

		String displayText = InnerTagUtil.resolveTag(innerTagFactory.parseInnerTag((String) cell.getDataValue()));
		if (XliffEditorParameter.getInstance().isShowNonpirnttingCharacter()) {
			displayText = displayText.replaceAll("\\n", Constants.LINE_SEPARATOR_CHARACTER + "\n");
			displayText = displayText.replaceAll("\\t", Constants.TAB_CHARACTER + "\u200B");
			displayText = displayText.replaceAll(" ", Constants.SPACE_CHARACTER + "\u200B");
		}
		layout.setText(displayText);
		List<InnerTagBean> innerTagBeans = innerTagFactory.getInnerTagBeans();
		for (InnerTagBean innerTagBean : innerTagBeans) {
			String placeHolder = placeHolderBuilder.getPlaceHolder(innerTagBeans, innerTagBeans.indexOf(innerTagBean));
			int start = displayText.indexOf(placeHolder);
			if (start == -1) {
				continue;
			}
			TextStyle style = new TextStyle();
			Point rect = tagRender.calculateTagSize(innerTagBean);
			style.metrics = new GlyphMetrics(rect.y, 0, rect.x + SEGMENT_LINE_SPACING * 2);
			layout.setStyle(style, start, start + placeHolder.length() - 1);
		}

		return layout;
	}

	private void appendNonprintingStyle(TextLayout layout) {
		TextStyle style = new TextStyle(font, GUIHelper.getColor(new RGB(100, 100, 100)), null);
		String s = layout.getText();
		Matcher matcher = Constants.NONPRINTING_PATTERN.matcher(s);
		while (matcher.find()) {
			int start = matcher.start();
			int end = matcher.end();
			// style.metrics = new GlyphMetrics(10, 0, 1);
			layout.setStyle(style, start, end - 1);
		}
	}

	private int getWidthFromCache(GC gc, String text) {
		String originalString = text;
		StringBuilder buffer = new StringBuilder();
		buffer.append(text);
		if (gc.getFont() != null) {
			FontData[] datas = fontDataCache.get(gc.getFont());
			if (datas == null) {
				datas = gc.getFont().getFontData();
				fontDataCache.put(gc.getFont(), datas);
			}
			if (datas != null && datas.length > 0) {
				buffer.append(datas[0].getName());
				buffer.append(",");
				buffer.append(datas[0].getHeight());
				buffer.append(",");
				buffer.append(datas[0].getStyle());
			}
		}
		text = buffer.toString();
		Integer width = temporaryMap.get(text);
		if (width == null) {
			width = Integer.valueOf(gc.textExtent(originalString).x);
			temporaryMap.put(text, width);
		}

		return width.intValue();
	}

	private Font getFont(int style) {
		Device device = Display.getDefault();
		switch (style) {
		case SWT.BOLD:
			return new Font(device, getFontData(style));
		case SWT.ITALIC:
			return new Font(device, getFontData(style));
		case SWT.BOLD | SWT.ITALIC:
			return new Font(device, getFontData(style));
		default:
			return font;
		}

	}

	private FontData[] getFontData(int style) {
		FontData[] fontDatas = font.getFontData();
		for (int i = 0; i < fontDatas.length; i++) {
			fontDatas[i].setStyle(style);
		}
		return fontDatas;
	}

	private List<StyleRange> calculateTermsStyleRange(char[] source, char[] target, TextStyle style) {
		int sourceOffset = 0;
		int sourceCount = source.length;
		int targetOffset = 0, targetCount = target.length;

		char first = target[targetOffset];
		int max = sourceOffset + (sourceCount - targetCount);
		List<StyleRange> rangeList = new ArrayList<StyleRange>();
		for (int i = sourceOffset; i <= max; i++) {
			/* Look for first character. */
			if (source[i] != first) {
				while (++i <= max && source[i] != first)
					;
			}

			/* Found first character, now look at the rest of v2 */
			if (i <= max) {
				List<StyleRange> tempList = new ArrayList<StyleRange>();
				int start = i;
				int j = i + 1;
				int end = j + targetCount - 1;
				for (int k = targetOffset + 1; j < end; j++, k++) {
					Matcher matcher = PlaceHolderEditModeBuilder.PATTERN.matcher(source[j] + "");
					if (matcher.matches()) {
						StyleRange range = new StyleRange(style);
						range.start = start;
						range.length = j - start;
						start = j + 1;
						k--;
						end++;
						if (end > sourceCount) {
							break;
						}
						tempList.add(range);
						continue;
					}
					if (source[j] != target[k]) {
						break;
					}
				}

				if (j == end) {
					/* Found whole string. */
					StyleRange range = new StyleRange(style);
					range.start = start;
					range.length = j - start;
					rangeList.addAll(tempList);
					rangeList.add(range);
				}
			}
		}
		return rangeList;
	}
}

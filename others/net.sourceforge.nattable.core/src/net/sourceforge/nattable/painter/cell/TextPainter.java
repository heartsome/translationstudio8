package net.sourceforge.nattable.painter.cell;

import java.util.Map;
import java.util.WeakHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sourceforge.nattable.config.CellConfigAttributes;
import net.sourceforge.nattable.config.IConfigRegistry;
import net.sourceforge.nattable.data.convert.IDisplayConverter;
import net.sourceforge.nattable.layer.cell.LayerCell;
import net.sourceforge.nattable.style.CellStyleAttributes;
import net.sourceforge.nattable.style.CellStyleUtil;
import net.sourceforge.nattable.style.IStyle;
import net.sourceforge.nattable.util.GUIHelper;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;

public class TextPainter extends BackgroundPainter {

	private static final Pattern endOfPreviousWordPattern = Pattern.compile("\\S\\s+\\S+\\s*$");

	public static final String EMPTY = "";
	public static final String DOT = "...";

	private static Map<String,Integer> temporaryMap = new WeakHashMap<String,Integer>();
	private static Map<org.eclipse.swt.graphics.Font,FontData[]> fontDataCache = new WeakHashMap<org.eclipse.swt.graphics.Font,FontData[]>();

	private final boolean wrapText;
	private final boolean paintBg;

	public TextPainter() {
		this(false, true);
	}

	/**
	 * @param wrapText split text over multiple lines
	 * @param paintBg skips painting the background if is FALSE
	 */
	public TextPainter(boolean wrapText, boolean paintBg) {
		this.wrapText = wrapText;
		this.paintBg = paintBg;
	}

	@Override
	public int getPreferredWidth(LayerCell cell, GC gc, IConfigRegistry configRegistry){
		setupGCFromConfig(gc, CellStyleUtil.getCellStyle(cell, configRegistry));
		return getWidthFromCache(gc, convertDataType(cell, configRegistry));
	}

	@Override
	public int getPreferredHeight(LayerCell cell, GC gc, IConfigRegistry configRegistry) {
		setupGCFromConfig(gc, CellStyleUtil.getCellStyle(cell, configRegistry));
		return gc.textExtent(convertDataType(cell, configRegistry)).y;
	}

	/**
	 * Convert the data value of the cell using the {@link IDisplayConverter} from the {@link IConfigRegistry}
	 */
	protected String convertDataType(LayerCell cell, IConfigRegistry configRegistry) {
		IDisplayConverter displayConverter = configRegistry.getConfigAttribute(CellConfigAttributes.DISPLAY_CONVERTER, cell.getDisplayMode(), cell.getConfigLabels().getLabels());
		String text = displayConverter != null ? (String) displayConverter.canonicalToDisplayValue(cell.getDataValue()) : null;
		text = (text == null) ? "" : text;
		return text;
	}

	public void setupGCFromConfig(GC gc, IStyle cellStyle) {
		Color fg = cellStyle.getAttributeValue(CellStyleAttributes.FOREGROUND_COLOR);
		Color bg = cellStyle.getAttributeValue(CellStyleAttributes.BACKGROUND_COLOR);
		Font font = cellStyle.getAttributeValue(CellStyleAttributes.FONT);

		gc.setAntialias(GUIHelper.DEFAULT_ANTIALIAS);
		gc.setTextAntialias(GUIHelper.DEFAULT_TEXT_ANTIALIAS);
		gc.setFont(font);
		gc.setForeground(fg != null ? fg : GUIHelper.COLOR_LIST_FOREGROUND);
		gc.setBackground(bg != null ? bg : GUIHelper.COLOR_LIST_BACKGROUND);
	}

	@Override
	public void paintCell(LayerCell cell, GC gc, Rectangle rectangle, IConfigRegistry configRegistry) {
		if (paintBg) {
			super.paintCell(cell, gc, rectangle, configRegistry);
		}

		Rectangle originalClipping = gc.getClipping();
		gc.setClipping(rectangle.intersection(originalClipping));

		IStyle cellStyle = CellStyleUtil.getCellStyle(cell, configRegistry);
		setupGCFromConfig(gc, cellStyle);
		String text = convertDataType(cell, configRegistry);

		// Draw Text
		String originalText = text;
		int originalTextWidth = getWidthFromCache(gc, originalText);
		text = getAvailableTextToDisplay(gc, rectangle, text);

		int contentWidth = Math.min(originalTextWidth, rectangle.width);

		int fontHeight = gc.getFontMetrics().getHeight();
		int contentHeight = fontHeight * getNumberOfNewLines(text);

		gc.drawText(
				text,
				rectangle.x + CellStyleUtil.getHorizontalAlignmentPadding(cellStyle, rectangle, contentWidth),
				rectangle.y + CellStyleUtil.getVerticalAlignmentPadding(cellStyle, rectangle, contentHeight),
				true
		);

		gc.setClipping(originalClipping);
	}

	private int getNumberOfNewLines(String text) {
		char[] charArray = text.toCharArray();
		int lineCount = 1;
		for (char c : charArray) {
			if (c == '\n') {
				lineCount++;
			}
		}

		return lineCount;
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

	private String getAvailableTextToDisplay(GC gc, Rectangle bounds, String text) {
		StringBuilder output = new StringBuilder();

		text = text.trim();

		while (text.length() > 0) {
			String line;
			int nextLineBreakIndex;

			int indexOfNewline = text.indexOf('\n');
			if (indexOfNewline > 0) {
				nextLineBreakIndex = indexOfNewline;
				line = text.substring(0, nextLineBreakIndex);
			} else {
				nextLineBreakIndex = -1;
				line = text;
			}

			int textWidth = getWidthFromCache(gc, line);

			if (wrapText) {
				while (textWidth > bounds.width + 1) {
					Matcher matcher = endOfPreviousWordPattern.matcher(line);
					if (matcher.find()) {
						nextLineBreakIndex = matcher.start() + 1;
						line = line.substring(0, nextLineBreakIndex);
						textWidth = getWidthFromCache(gc, line);
					} else {
						nextLineBreakIndex = -1;
						break;
					}
				}
			}

			if (textWidth > bounds.width + 1) {
				int textLen = line.length();
				for (int i = textLen - 1; i >= 0; i--) {
					String temp = line.substring(0, i) + DOT;
					textWidth = getWidthFromCache(gc, temp);
					if (textWidth < bounds.width) {
						line = temp;
						break;
					} else if (i == 0) {
						line = EMPTY;
					}
				}
			}

			output.append(line);

			if (nextLineBreakIndex > 0) {
				text = text.substring(nextLineBreakIndex).trim();

				if (text.length() > 0) {
					output.append("\n");
				}
			} else {
				break;
			}
		}

		return output.toString();
	}
}

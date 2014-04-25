/**
 * MatchViewCellRenderer.java
 *
 * Version information :
 *
 * Date:Dec 26, 2011
 *
 * Copyright notice :
 * 本文件及其附带的相关文件包含机密信息，仅限瀚特盛科技有限公司指定的，与本项目有关的内部人员和客户联络人员查阅和使用。 
 * 如果您不是本保密声明中指定的收件者，请立即销毁本文件，禁止对本文件或根据本文件中的内容采取任何其他行动， 
 * 包括但不限于：泄露本文件中的信息、以任何方式制作本文件全部或部分内容之副本、将本文件或其相关副本提供给任何其他人。
 */
package net.heartsome.cat.ts.ui.translation.view;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;

import net.heartsome.cat.common.innertag.factory.PlaceHolderEditModeBuilder;
import net.heartsome.cat.ts.ui.grid.XGridCellRenderer;
import net.heartsome.cat.ts.ui.innertag.SegmentViewer;
import net.heartsome.cat.ts.ui.translation.comparator.TextDiffMatcher;
import net.heartsome.cat.ts.ui.translation.comparator.TextDiffMatcher.Diff;
import net.heartsome.cat.ts.ui.translation.comparator.TextDiffMatcher.Operation;

import org.eclipse.nebula.widgets.grid.GridItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.TextLayout;
import org.eclipse.swt.graphics.TextStyle;
import org.eclipse.swt.widgets.Display;

/**
 * @author Jason
 * @version
 * @since JDK1.6
 */
public class SourceColunmCellRenderer extends XGridCellRenderer {

	// private TextLayout textLayout;

	private Color diffColor = new Color(Display.getDefault(), new RGB(255, 255, 180));
	private Color inOrDecreaseColor = new Color(Display.getDefault(), new RGB(162, 200, 255));
	private Color positionColor = new Color(Display.getDefault(), new RGB(255, 180, 150));
	private String tuSrcText;
	private SegmentViewer segmentViewer;

	/**
	 * @param tuSrcText
	 *            the tuSrcText to set
	 */
	public void setTuSrcText(String tuSrcText) {
		this.tuSrcText = tuSrcText;
	}

	public void setSegmentViewer(SegmentViewer segmentViewer) {
		this.segmentViewer = segmentViewer;
	}

	/**
	 * {@inheritDoc}
	 */
	public void paint(GC gc, Object value) {
		GridItem item = (GridItem) value;
		gc.setFont(item.getFont(getColumn()));
		boolean drawBackground = true;

		boolean drawAsSelected = isSelected();
		if (isCellSelected()) {
			drawAsSelected = true;
		}
		gc.setForeground(item.getForeground(getColumn()));
		if (drawAsSelected) {
			gc.setBackground((Color) item.getParent().getData("selectedBgColor"));
		} else {
			if (item.getParent().isEnabled()) {
				Color back = item.getBackground(getColumn());
				if (back != null) {
					gc.setBackground(back);
				} else {
					drawBackground = false;
				}
			} else {
				gc.setBackground(getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
			}
		}

		if (drawBackground) {
			gc.fillRectangle(getBounds().x, getBounds().y, getBounds().width, getBounds().height);
		}

		String text = segmentViewer.getTextWidget().getText();

		// 创建 TextLayout
		TextLayout layout = getTextLayout(gc, item, getColumn(), false, false);
		String displayStr = layout.getText();
		// 附加内部标记样式前，先和源文比较，不一样的地方着色实现
		if (!tuSrcText.equals(displayStr)) {
			// TextStyle style = new TextStyle(layout.getFont(), colorConfigBean.getSrcDiffFgColor(),
			// colorConfigBean.getSrcDiffBgColor());
			// List<Position> diff = Comparator.Compare(tuSrcText, displayStr);
			// for (Iterator<Position> iterator = diff.iterator(); iterator.hasNext();) {
			// Position position = iterator.next();
			// layout.setStyle(style, position.offset, position.length);
			// }
			TextStyle diffStyle = new TextStyle(layout.getFont(), null, diffColor);
			TextStyle InOrDecreaseStyle = new TextStyle(layout.getFont(), null, inOrDecreaseColor); // Increase or
																									// decrease
			TextStyle positionStyle = new TextStyle(layout.getFont(), null, positionColor);
			TextDiffMatcher dmp = new TextDiffMatcher();
			LinkedList<Diff> diffs = dmp.diff_main(text, displayStr);
			dmp.diff_cleanupEfficiency(diffs);
			boolean pFlg = positionDiff(diffs, text, displayStr);
			if (pFlg) {
				int insTextStart = 0;
				int delTextStart = 0;
				for (int i = 0; i < diffs.size(); i++) {
					Diff f = diffs.get(i);
					if (f.operation == Operation.EQUAL) {
						int l = f.text.length() - 1;
						insTextStart += l;
						delTextStart += l;
						continue;
					} else if (f.operation == Operation.DELETE && drawAsSelected) {
						String delText = f.text;
						Matcher m = PlaceHolderEditModeBuilder.PATTERN.matcher(delText);
						TextStyle style = positionStyle;
						boolean flg = m.find();
						if (flg && delText.length() != 1) {
							int start = text.indexOf(delText, delTextStart);
							delTextStart = start + delText.length();
							int mark = 0;
							List<StyleRange> rangeList = new ArrayList<StyleRange>();
							do {
								int off = m.start();
								StyleRange range = new StyleRange(style);
								range.start = start + mark;
								range.length = off - mark;
								mark = m.end();
								rangeList.add(range);
							} while (m.find());
							if (mark < delText.length()) {
								StyleRange range = new StyleRange(style);
								range.start = start + mark;
								range.length = delText.length() - mark;
								rangeList.add(range);
							}
							for (StyleRange range : rangeList) {
								segmentViewer.getTextWidget().setStyleRange(range);
							}
						} else if (!flg) {
							int start = text.indexOf(delText, delTextStart);
							delTextStart = start + delText.length();
							StyleRange range = new StyleRange(style);
							range.start = start;
							range.length = delText.length();
							segmentViewer.getTextWidget().setStyleRange(range);
						}
					} else if (f.operation == Operation.INSERT) {
						String insText = f.text;
						int start = displayStr.indexOf(insText, insTextStart);
						insTextStart += insText.length();
						layout.setStyle(positionStyle, start, start + insText.length() - 1);
					}
				}
			} else {
				int insTextStart = 0;
				int delTextStart = 0;
				for (int i = 0; i < diffs.size(); i++) {
					Diff f = diffs.get(i);
					if (f.operation == Operation.EQUAL) {
						int l = f.text.length() - 1;
						insTextStart += l;
						delTextStart += l;
						continue;
					} else if (f.operation == Operation.DELETE) {
						String delText = f.text;
						boolean isDiff = false;
						if (i + 1 < diffs.size() && diffs.get(i + 1).operation == Operation.INSERT) {
							// 库和当前句子存在不一致部分
							i += 1;
							// 处理库
							f = diffs.get(i);
							String insText = f.text;
							int start = displayStr.indexOf(insText, insTextStart);
							layout.setStyle(diffStyle, start, start + insText.length() - 1);
							insTextStart = start + insText.length();
							isDiff = true;
						}
						// 处理当前句子
						if (drawAsSelected) {
							Matcher m = PlaceHolderEditModeBuilder.PATTERN.matcher(delText);
							TextStyle style = isDiff ? diffStyle : InOrDecreaseStyle;
							boolean flg = m.find();
							if (flg && delText.length() != 1) {
								int start = text.indexOf(delText, delTextStart);
								delTextStart = start + delText.length();
								int mark = 0;
								List<StyleRange> rangeList = new ArrayList<StyleRange>();
								do {
									int off = m.start();
									StyleRange range = new StyleRange(style);
									range.start = start + mark;
									range.length = off - mark;
									mark = m.end();
									rangeList.add(range);
								} while (m.find());
								if (mark < delText.length()) {
									StyleRange range = new StyleRange(style);
									range.start = start + mark;
									range.length = delText.length() - mark;
									rangeList.add(range);
								}
								for (StyleRange range : rangeList) {
									segmentViewer.getTextWidget().setStyleRange(range);
								}
							} else if (!flg) {
								int start = text.indexOf(delText, delTextStart);
								delTextStart = start + delText.length();
								StyleRange range = new StyleRange(style);
								range.start = start;
								range.length = delText.length();
								segmentViewer.getTextWidget().setStyleRange(range);
							}
						}
					} else if (f.operation == Operation.INSERT) {
						// 库多出来的
						String insText = f.text;
						int start = displayStr.indexOf(insText, insTextStart);
						insTextStart += insText.length();
						layout.setStyle(InOrDecreaseStyle, start, start + insText.length() - 1);
					}
				}
			}
		}
		// 添加标记样式，并创建标记
		attachInnertTagStyle(gc, layout, false);
		try {
			int y = getBounds().y + textTopMargin + topMargin;
			y += getVerticalAlignmentAdjustment(layout.getBounds().height, getBounds().height);

			if (item.getParent().isAutoHeight()) {
				int textHeight = topMargin + textTopMargin;
				// fix Bug #3116 库匹配面板--显示的匹配有截断 by Jason
				// for (int cnt = 0; cnt < layout.getLineCount(); cnt++)
				// textHeight += layout.getLineBounds(cnt).height;
				textHeight += layout.getBounds().height;
				textHeight += textBottomMargin + bottomMargin;
				item.setData("itemHeight", textHeight);
			}

			Point selection = copyEnable.getSelectionRange(getColumn(), item);
			if (selection == null || selection.x == selection.y) {
				layout.draw(gc, getBounds().x + leftMargin, y);
			} else {
				int x = getBounds().x + leftMargin;
				int start = Math.max(0, selection.x);
				int end = Math.min(displayStr.length(), selection.y);
				layout.draw(gc, x, y, start, end - 1, getDisplay().getSystemColor(SWT.COLOR_LIST_SELECTION_TEXT),
						getDisplay().getSystemColor(SWT.COLOR_LIST_SELECTION));
			}
			drawInnerTag(gc, layout);

			if (item.getParent().getLinesVisible()) {
				if (isCellSelected()) {
					gc.setForeground(getDisplay().getSystemColor(SWT.COLOR_WIDGET_DARK_SHADOW));
				} else {
					gc.setForeground(item.getParent().getLineColor());
				}
				gc.drawLine(getBounds().x, getBounds().y + getBounds().height, getBounds().x + getBounds().width - 1,
						getBounds().y + getBounds().height);
				gc.drawLine(getBounds().x + getBounds().width - 1, getBounds().y,
						getBounds().x + getBounds().width - 1, getBounds().y + getBounds().height);
			}
		} finally {
			if (layout != null) {
				layout.dispose();
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public Point computeSize(GC gc, int wHint, int hHint, Object value) {
		GridItem item = (GridItem) value;

		gc.setFont(item.getFont(getColumn()));

		int x = 0;

		x += leftMargin;

		int y = 0;

		Image image = item.getImage(getColumn());
		if (image != null) {
			y = topMargin + image.getBounds().height + bottomMargin;
		}

		// MOPR-DND
		// MOPR: replaced this code (to get correct preferred height for cells in word-wrap columns)
		//
		// x += gc.stringExtent(item.getText(column)).x + rightMargin;
		//
		// y = Math.max(y,topMargin + gc.getFontMetrics().getHeight() + bottomMargin);
		//
		// with this code:

		int textHeight = 0;
		if (!isWordWrap()) {
			x += gc.textExtent(item.getText(getColumn())).x + rightMargin;

			textHeight = topMargin + textTopMargin + gc.getFontMetrics().getHeight() + textBottomMargin + bottomMargin;
		} else {
			int plainTextWidth;
			if (wHint == SWT.DEFAULT)
				plainTextWidth = getBounds().width - x - rightMargin;
			else
				plainTextWidth = wHint - x - rightMargin;

			TextLayout currTextLayout = new TextLayout(gc.getDevice());
			currTextLayout.setFont(gc.getFont());
			currTextLayout.setText(item.getText(getColumn()));
			currTextLayout.setAlignment(getAlignment());
			currTextLayout.setWidth(plainTextWidth < 1 ? 1 : plainTextWidth);

			x += plainTextWidth + rightMargin;

			textHeight += topMargin + textTopMargin;
			for (int cnt = 0; cnt < currTextLayout.getLineCount(); cnt++)
				textHeight += currTextLayout.getLineBounds(cnt).height;
			textHeight += textBottomMargin + bottomMargin;

			currTextLayout.dispose();
		}

		y = Math.max(y, textHeight);

		return new Point(x, y);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean notify(int event, Point point, Object value) {
		return false;
	}

	private boolean positionDiff(LinkedList<Diff> diffs, String text1, String text2) {
		if (diffs.size() > 2 && text1.length() == text2.length()) { // 判断位置不一样
			LinkedList<Diff> delDiff = new LinkedList<Diff>();
			LinkedList<Diff> insDiff = new LinkedList<Diff>();
			for (int i = 0; i < diffs.size(); i++) {
				Diff f = diffs.get(i);
				if (f.operation == Operation.DELETE) {
					delDiff.add(f);
				} else if (f.operation == Operation.INSERT) {
					insDiff.add(f);
				}
			}
			if (delDiff.size() == 0 || insDiff.size() == 0) {
				return false;
			}
			label: for (int i = 0; i < delDiff.size(); i++) {
				Diff df = delDiff.get(i);
				for (int j = 0; j < insDiff.size(); j++) {
					Diff idf = insDiff.get(j);
					if (df.text.trim().equals(idf.text.trim())) {
						insDiff.remove(j);
						continue label;
					}
				}
				return false;
			}
			return true;
		}
		return false;
	}

	public void dispose() {
		if (!diffColor.isDisposed()) {
			diffColor.dispose();
		}
		if (!inOrDecreaseColor.isDisposed()) {
			inOrDecreaseColor.dispose();
		}
		if (!positionColor.isDisposed()) {
			positionColor.dispose();
		}
	}
}

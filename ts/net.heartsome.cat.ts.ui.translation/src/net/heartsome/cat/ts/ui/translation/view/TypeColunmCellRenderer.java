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

import net.heartsome.cat.ts.ui.grid.XGridCellRenderer;
import net.heartsome.cat.ts.ui.util.TmUtils;

import org.eclipse.nebula.widgets.grid.GridItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.TextLayout;

/**
 * @author Jason
 * @version
 * @since JDK1.6
 */
public class TypeColunmCellRenderer extends XGridCellRenderer {

	private TextLayout textLayout;

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
			// if (backColor == null || backColor.isDisposed()) {
			gc.setBackground((Color) item.getParent().getData("selectedBgColor"));
			// } else {
			// gc.setBackground(backColor);
			// }
		} else {
			// if (item.getParent().isEnabled()) {
			// if (backColor == null || backColor.isDisposed()) {
			// drawBackground = false;
			// } else {
			// gc.setBackground(backColor);
			// }
			// } else {
			gc.setBackground(getDisplay().getSystemColor(SWT.COLOR_WHITE));
			// }
		}

		if (drawBackground) {
			gc.fillRectangle(getBounds().x, getBounds().y, getBounds().width, getBounds().height);
		}
		int x = leftMargin;
		Image image = (Image) item.getData("typeImage");
		if (image != null) {
			int y = getBounds().y;
			y += (getBounds().height - image.getBounds().height) / 2;
			gc.drawImage(image, getBounds().x + x, y);
			x += image.getBounds().width + 3;
		}

		int height = getBounds().height - bottomMargin;
		if (textLayout == null) {
			textLayout = new TextLayout(gc.getDevice());
			item.getParent().addDisposeListener(new DisposeListener() {
				public void widgetDisposed(DisposeEvent e) {
					textLayout.dispose();
				}
			});
		}
		textLayout.setFont(gc.getFont());
		String quality = item.getText(getColumn());
		textLayout.setText(quality);
		textLayout.setAlignment(SWT.LEFT);
		int width = getBounds().width - x - rightMargin;
		textLayout.setWidth(width < 1 ? 1 : width);

		int y = getBounds().y + textTopMargin + topMargin;
		y += getVerticalAlignmentAdjustment(textLayout.getBounds().height, height);

		// textLayout.draw(gc, getBounds().x + x, y);
		String type = (String) item.getData("matchType");
		if (type.equals("TM")) {
			Color backColor = TmUtils.getMatchTypeColor(type, quality);
			if (backColor != null && !backColor.isDisposed()) {
				gc.setBackground(backColor);
			}
			gc.drawText(item.getText(getColumn()), getBounds().x + x, y);
		}
		if (item.getParent().getLinesVisible()) {
			if (isCellSelected()) {
				// XXX: should be user definable?
				gc.setForeground(getDisplay().getSystemColor(SWT.COLOR_WIDGET_DARK_SHADOW));
			} else {
				gc.setForeground(item.getParent().getLineColor());
			}
			gc.drawLine(getBounds().x, getBounds().y + getBounds().height, getBounds().x + getBounds().width - 1,
					getBounds().y + getBounds().height);
			gc.drawLine(getBounds().x + getBounds().width - 1, getBounds().y, getBounds().x + getBounds().width - 1,
					getBounds().y + getBounds().height);
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

			x += image.getBounds().width + 3;
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
	public Rectangle getTextBounds(GridItem item, boolean preferred) {
		int x = leftMargin;

		Image image = item.getImage(getColumn());
		if (image != null) {
			x += image.getBounds().width + 3;
		}

		Rectangle bounds = new Rectangle(x, topMargin + textTopMargin, 0, 0);

		GC gc = new GC(item.getParent());
		gc.setFont(item.getFont(getColumn()));
		Point size = gc.stringExtent(item.getText(getColumn()));

		bounds.height = size.y;

		if (preferred) {
			bounds.width = size.x - 1;
		} else {
			bounds.width = getBounds().width - x - rightMargin;
		}

		gc.dispose();

		return bounds;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean notify(int event, Point point, Object value) {
		return false;
	}

}

package net.heartsome.cat.ts.ui.grid;

import static net.heartsome.cat.ts.ui.Constants.SEGMENT_LINE_SPACING;

import java.util.List;

import net.heartsome.cat.common.innertag.InnerTagBean;
import net.heartsome.cat.common.innertag.factory.PlaceHolderEditModeBuilder;
import net.heartsome.cat.common.innertag.factory.XliffInnerTagFactory;
import net.heartsome.cat.common.ui.innertag.InnerTagRender;
import net.heartsome.cat.common.ui.utils.InnerTagUtil;
import net.heartsome.cat.ts.ui.Constants;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.nebula.widgets.grid.GridCellRenderer;
import org.eclipse.nebula.widgets.grid.GridItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.GlyphMetrics;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.TextLayout;
import org.eclipse.swt.graphics.TextStyle;
import org.eclipse.swt.widgets.Display;

/**
 * 可以设置单元格垂直对齐方式的类
 * @author peason
 * @version
 * @since JDK1.6
 */
public abstract class XGridCellRenderer extends GridCellRenderer {

	protected int leftMargin = 4;

	protected int rightMargin = 4;

	protected int topMargin = 0;

	protected int bottomMargin = 0;

	protected int textTopMargin = 1;

	protected int textBottomMargin = 2;

	private final int tabSize = 4;
	
	protected int tabWidth = 0;
	
	protected Font font;

	/** 垂直对齐方式 默认居上 */
	private int verticalAlignment = SWT.TOP;

	protected PlaceHolderEditModeBuilder placeHolderBuilder = new PlaceHolderEditModeBuilder();

	protected XliffInnerTagFactory innerTagFactory = new XliffInnerTagFactory(placeHolderBuilder);

	protected InnerTagRender tagRender = new InnerTagRender();
	protected GridCopyEnable copyEnable;

	public XGridCellRenderer() {
		setFont(JFaceResources.getFont(net.heartsome.cat.ts.ui.Constants.MATCH_VIEWER_TEXT_FONT));
	}
	public TextLayout getTextLayout(GC gc, GridItem item, int columnIndex, boolean innerTagStyled, boolean drawInnerTag) {
		TextLayout layout = new TextLayout(gc.getDevice());
		layout.setFont(font);
		layout.setTabs(new int[]{tabWidth});
		innerTagFactory.reset();
		
		String displayStr = "";
		try{
			displayStr = InnerTagUtil.resolveTag(innerTagFactory.parseInnerTag(item.getText(columnIndex)));
		}catch (NullPointerException e) {
			return null;
		}
		layout.setText(displayStr);
		int width = getBounds().width - leftMargin - rightMargin;
		layout.setWidth(width < 1 ? 1 : width);
		layout.setSpacing(Constants.SEGMENT_LINE_SPACING);
		layout.setAlignment(SWT.LEFT);
		layout.setOrientation(item.getParent().getOrientation());
		if (displayStr.length() != 0 && innerTagStyled) {
			attachInnertTagStyle(gc, layout, drawInnerTag);
		}
		return layout;
	}

	protected void attachInnertTagStyle(GC gc, TextLayout layout, boolean drawInnerTag) {
		String displayStr = layout.getText();
		List<InnerTagBean> innerTagBeans = innerTagFactory.getInnerTagBeans();
		Rectangle bounds = getBounds();
		for (InnerTagBean innerTagBean : innerTagBeans) {
			String placeHolder = placeHolderBuilder.getPlaceHolder(innerTagBeans, innerTagBeans.indexOf(innerTagBean));
			int start = displayStr.indexOf(placeHolder);
			if (start == -1) {
				continue;
			}
			TextStyle style = new TextStyle();
			Point rect = tagRender.calculateTagSize(innerTagBean);
			style.metrics = new GlyphMetrics(rect.y , 0 , rect.x + SEGMENT_LINE_SPACING * 2);
			layout.setStyle(style, start, start + placeHolder.length() - 1);

			if (drawInnerTag && gc != null) {
				Point p = layout.getLocation(start, false);
				int x = bounds.x + p.x + leftMargin;
				x += SEGMENT_LINE_SPACING;

				Point tagSize = tagRender.calculateTagSize(innerTagBean);
				int lineIdx = layout.getLineIndex(start);
				Rectangle r = layout.getLineBounds(lineIdx);
				int y = bounds.y + p.y + topMargin + r.height / 2 - tagSize.y /2;				
				tagRender.draw(gc, innerTagBean, x, y - layout.getAscent());
			}
		}
	}
	
	protected void drawInnerTag(GC gc, TextLayout layout) {
		String displayStr = layout.getText();
		List<InnerTagBean> innerTagBeans = innerTagFactory.getInnerTagBeans();
		Rectangle bounds = getBounds();
		for (InnerTagBean innerTagBean : innerTagBeans) {
			String placeHolder = placeHolderBuilder.getPlaceHolder(innerTagBeans, innerTagBeans.indexOf(innerTagBean));
			int start = displayStr.indexOf(placeHolder);
			if (start == -1) {
				continue;
			}			
			if (gc != null) {
				Point p = layout.getLocation(start, false);
				int x = bounds.x + p.x + leftMargin;
				x += SEGMENT_LINE_SPACING;

				Point tagSize = tagRender.calculateTagSize(innerTagBean);
				int lineIdx = layout.getLineIndex(start);
				Rectangle r = layout.getLineBounds(lineIdx);
				int y = bounds.y + p.y + topMargin + r.height / 2 - tagSize.y /2;				
				tagRender.draw(gc, innerTagBean, x, y - layout.getAscent());
			}
		}
	}

	/**
	 * 计算单元格的剩余空间
	 * @param textHeight
	 * @param cellHeight
	 * @return ;
	 */
	public int getVerticalAlignmentAdjustment(int textHeight, int cellHeight) {
		if (getVerticalAlignment() == SWT.BOTTOM) {
			if (textHeight < cellHeight) {
				return cellHeight - textHeight;
			}
		} else if (getVerticalAlignment() == SWT.CENTER) {
			if (textHeight < cellHeight) {
				return (cellHeight - textHeight) / 2;
			}
		}
		return 0;
	}

	/**
	 * @param copyEnable
	 *            the copyEnable to set
	 */
	public void setCopyEnable(GridCopyEnable copyEnable) {
		this.copyEnable = copyEnable;
		copyEnable.addCopyAbleColumn(getColumn());
	}

	/**
	 * 获得垂直对齐方式
	 * @return ;
	 */
	public int getVerticalAlignment() {
		return verticalAlignment;
	}

	/**
	 * 设置垂直对齐方式
	 * @param verticalAlignment
	 *            ;
	 */
	public void setVerticalAlignment(int verticalAlignment) {
		this.verticalAlignment = verticalAlignment;
	}

	/**
	 * 设置字体
	 * @param font
	 *            ;
	 */
	public void setFont(Font font) {
		TextLayout layout = new TextLayout(Display.getDefault());
		layout.setFont(font);
		StringBuffer tabBuffer = new StringBuffer(tabSize);
		for (int i = 0; i < tabSize; i++) {
			tabBuffer.append(' ');
		}
		layout.setText(tabBuffer.toString());
		tabWidth = layout.getBounds().width;
		layout.dispose();
		this.font = font;
	}
}

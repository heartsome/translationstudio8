package net.heartsome.cat.ts.ui.xliffeditor.nattable.painter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.heartsome.cat.ts.core.bean.TransUnitBean;
import net.heartsome.cat.ts.ui.util.TmUtils;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.painter.XliffEditorGUIHelper.ImageName;
import net.sourceforge.nattable.config.IConfigRegistry;
import net.sourceforge.nattable.data.IRowDataProvider;
import net.sourceforge.nattable.layer.cell.LayerCell;
import net.sourceforge.nattable.painter.cell.CellPainterWrapper;
import net.sourceforge.nattable.style.CellStyleAttributes;
import net.sourceforge.nattable.style.CellStyleUtil;
import net.sourceforge.nattable.style.IStyle;
import net.sourceforge.nattable.util.GUIHelper;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;

/**
 * TU状态Painter
 * @author Leakey
 * @version
 * @since JDK1.5
 */
public class StatusPainter extends CellPainterWrapper {
	/** 是否画背景. */
	private final boolean paintBg;
	/** bodyDataProvider. */
	private IRowDataProvider<TransUnitBean> bodyDataProvider;

	/**
	 * @param bodyDataProvider
	 *            数据提供者
	 */
	public StatusPainter(IRowDataProvider<TransUnitBean> bodyDataProvider) {
		this.paintBg = true;
		this.bodyDataProvider = bodyDataProvider;
	}

	@Override
	public int getPreferredHeight(LayerCell cell, GC gc, IConfigRegistry configRegistry) {
		return 0;
	}

	/**
	 * 重绘操作
	 */
	@Override
	public void paintCell(LayerCell cell, GC gc, Rectangle bounds, IConfigRegistry configRegistry) {
		List<Map<Integer, Image>> images = getImages(cell, configRegistry);
		Rectangle cellBounds = cell.getBounds();
		IStyle cellStyle = CellStyleUtil.getCellStyle(cell, configRegistry);
		if (paintBg) {
			Color originalBackground = gc.getBackground();
			Color originalForeground = gc.getForeground();
			Color backgroundColor = CellStyleUtil.getCellStyle(cell, configRegistry).getAttributeValue(
					CellStyleAttributes.BACKGROUND_COLOR);
			if (backgroundColor != null) {
				gc.setBackground(backgroundColor);
				gc.fillRectangle(bounds);
			}

			super.paintCell(cell, gc, bounds, configRegistry);
			if (machQuality != null) {
				Font oldFont = gc.getFont();
				Font font = cellStyle.getAttributeValue(CellStyleAttributes.FONT);
				gc.setFont(font);
				if (cellBackground != null) {
					gc.setBackground(cellBackground);
					gc.setForeground(GUIHelper.COLOR_BLACK);
				}
				gc.drawText(machQuality,
						cellBounds.x + 15 + CellStyleUtil.getHorizontalAlignmentPadding(cellStyle, bounds, 15),
						bounds.y + CellStyleUtil.getVerticalAlignmentPadding(cellStyle, bounds, 15));
				gc.setFont(oldFont);
			}
			gc.setForeground(originalForeground);
			gc.setBackground(originalBackground);
			cellBackground = null;
		}

		if (images != null) {
			int x = 0;
			for (Map<Integer, Image> imageMap : images) {
				Iterator<Integer> ps = imageMap.keySet().iterator();
				if (ps.hasNext()) {
					int p = ps.next();
					Image image = imageMap.get(p);
					if (image == null) {
						continue;
					}

					if (x == 0) { // 第一张图片的水平位置以cell的水平位置为准
						x = cellBounds.x;
					} else { // 累加显示过图片的宽度以确定下一张图片的水平位置
						x = cellBounds.x + 20;
						x += 16 * (p - 1);
					}

					// TODO 没有考虑对齐方式
					if (p - 1 == 0) { // 第一张图片的水平位置要加上HorizontalAligmentPadding的宽度和VerticalAlignmentPadding的高度
						gc.drawImage(image, x + CellStyleUtil.getHorizontalAlignmentPadding(cellStyle, bounds, 16),
								bounds.y + CellStyleUtil.getVerticalAlignmentPadding(cellStyle, bounds, 16));
					} else {
						gc.drawImage(image, x,
								bounds.y + CellStyleUtil.getVerticalAlignmentPadding(cellStyle, bounds, 16));
					}
				}
			}
		}
	}

	private Color cellBackground;
	private String machQuality;

	/**
	 * 通过LayerCell得到行号确定所要得到的TU对象，通过TU对象的各种属性确定其状态图片
	 * @param cell
	 * @param configRegistry
	 * @return ;
	 */
	protected List<Map<Integer, Image>> getImages(LayerCell cell, IConfigRegistry configRegistry) {
		List<Map<Integer, Image>> images = new ArrayList<Map<Integer, Image>>();

		int index = cell.getLayer().getRowIndexByPosition(cell.getRowPosition());
		TransUnitBean tu = bodyDataProvider.getRowObject(index);

		String matchType = tu.getTgtProps().get("hs:matchType");
		machQuality = tu.getTgtProps().get("hs:quality");
		if (matchType != null && machQuality != null) {
			if (machQuality.endsWith("%")) {
				machQuality = machQuality.substring(0, machQuality.lastIndexOf("%"));
			}
			cellBackground = TmUtils.getMatchTypeColor(matchType, machQuality);
		}
		String approved = null;
		String translate = null;
		String state = null;
		String sendToTm = null;
		String needReview = null;
		int noteSize = 0;
		if (tu != null && tu.getTuProps() != null) {
			approved = tu.getTuProps().get("approved");
			sendToTm = tu.getTuProps().get("hs:send-to-tm");
			translate = tu.getTuProps().get("translate");
			needReview = tu.getTuProps().get("hs:needs-review");
			if (tu.getTgtProps() != null) {
				state = tu.getTgtProps().get("state");
			}
			if (tu.getNotes() != null) {
				noteSize = tu.getNotes().size();
			}
		}
		if (translate != null && "no".equals(translate)) { // 已锁定
			addImage(images, XliffEditorGUIHelper.getImage(ImageName.LOCKED), 1);
		} else if (state != null && "signed-off".equals(state)) { // 已签发
			addImage(images, XliffEditorGUIHelper.getImage(ImageName.SINGED_OFF), 1);
		} else if (approved != null && "yes".equals(approved)) { // 已批准
			addImage(images, XliffEditorGUIHelper.getImage(ImageName.APPROVE), 1);
		} else if (state != null && "translated".equals(state)) { // 已翻译
			addImage(images, XliffEditorGUIHelper.getImage(ImageName.TRANSLATED), 1);
		} else if (state != null && "new".equals(state)) { // 草稿
			addImage(images, XliffEditorGUIHelper.getImage(ImageName.DRAFT), 1);
		} else {
			addImage(images, XliffEditorGUIHelper.getImage(ImageName.EMPTY), 1);
		}

		if (sendToTm != null && ("no").equals(sendToTm)) {
			addImage(images, XliffEditorGUIHelper.getImage(ImageName.DONT_ADDDB), 2);
		}

		if (needReview != null && "yes".equals(needReview)) {
			addImage(images, XliffEditorGUIHelper.getImage(ImageName.HAS_QUESTION), 3);
		}
		if (noteSize > 0) {
			addImage(images, XliffEditorGUIHelper.getImage(ImageName.HAS_NOTE), 4);
		}
		return images;
	}

	/**
	 * 添加图片（过滤null的Image对象）
	 * @param images
	 * @param image
	 */
	private void addImage(List<Map<Integer, Image>> images, Image image, int position) {
		if (image != null) {
			Map<Integer, Image> tempMap = new HashMap<Integer, Image>();
			tempMap.put(position, image);
			images.add(tempMap);
		}
	}
}

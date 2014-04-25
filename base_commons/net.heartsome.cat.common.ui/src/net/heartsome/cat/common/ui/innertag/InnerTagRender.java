/**
 * InnerTagRender.java
 *
 * Version information :
 *
 * Date:2013-3-7
 *
 * Copyright notice :
 * 本文件及其附带的相关文件包含机密信息，仅限瀚特盛科技有限公司指定的，与本项目有关的内部人员和客户联络人员查阅和使用。 
 * 如果您不是本保密声明中指定的收件者，请立即销毁本文件，禁止对本文件或根据本文件中的内容采取任何其他行动， 
 * 包括但不限于：泄露本文件中的信息、以任何方式制作本文件全部或部分内容之副本、将本文件或其相关副本提供给任何其他人。
 */
package net.heartsome.cat.common.ui.innertag;

import net.heartsome.cat.common.bean.ColorConfigBean;
import net.heartsome.cat.common.innertag.InnerTagBean;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 * 内部标记渲染器
 * @author Jason
 * @version
 * @since JDK1.6
 */
public class InnerTagRender {

	/** 标记字体 */
	private static Font TAG_FONT = new Font(Display.getDefault(), "Arial", 7, SWT.BOLD);

	private static final int MARGIN_H = 2; // 文本的水平边距
	private static final int MARGIN_V = 2; // 文本的垂直边距

	private InnerTag tag;

	public static void main(String[] args) {
		Display ds = Display.getDefault();
		Shell shell = new Shell(ds);
		shell.pack();
		shell.open();
		while (!shell.isDisposed()) {
			if (!ds.readAndDispatch()) {
				ds.sleep();
			}
		}
	}

	public InnerTagRender() {
	}

	public InnerTagRender(InnerTag tag) {
		this.tag = tag;
	}

	public Point calculateTagSize(InnerTagBean innerTagBean) {
		GC gc = new GC(Display.getDefault());
		gc.setFont(TAG_FONT);
		Point tempSize = gc.textExtent(String.valueOf(innerTagBean.getIndex()));
		// System.out.println("textSize: "+ tempSize);
		Point tagSize = new Point(tempSize.x + MARGIN_H * 2, tempSize.y + MARGIN_V * 2);
		int sawtooth = tagSize.y / 2;

		switch (innerTagBean.getType()) {
		case STANDALONE:
			tagSize.x += tagSize.y;
			break;

		default:
			tagSize.x += sawtooth;
			break;
		}
		// System.out.println("TagSize :"+tagSize);
		gc.dispose();
		return tagSize;
	}

	public void draw(GC gc, InnerTagBean innerTagBean, int x, int y) {
		Point tagSize = calculateTagSize(innerTagBean);
		if (tag != null && tag.isSelected()) {
			Color b = gc.getBackground();
			gc.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_LIST_SELECTION));
			gc.fillRectangle(0, 0, tagSize.x, tagSize.y);
			gc.setBackground(b);
		}
		int[] tagAreaPoints = calculateTagArea(tagSize, innerTagBean, x, y);
		String strInx = String.valueOf(innerTagBean.getIndex());
		Color gcBgColor = gc.getBackground();
		Color gcFgColor = gc.getForeground();
		Font gcFont = gc.getFont();
		gc.setFont(TAG_FONT);
		// gc.setBackground(ColorConfigBean.getInstance().getTm90Color());
		// Point p = calculateTagSize(innerTagBean);
		// gc.fillRectangle(x, y, p.x, p.y);
		if (innerTagBean.isWrongTag()) {
			gc.setBackground(ColorConfigBean.getInstance().getWrongTagColor());
		} else {
			gc.setBackground(ColorConfigBean.getInstance().getTagBgColor());
		}
		gc.setForeground(ColorConfigBean.getInstance().getTagFgColor());
		gc.fillPolygon(tagAreaPoints);
		// gc.drawPolygon(tagAreaPoints);
		if (innerTagBean.isWrongTag()) {
			gc.setBackground(ColorConfigBean.getInstance().getWrongTagColor());
		} else {
			gc.setBackground(ColorConfigBean.getInstance().getTagBgColor());
		}
		gc.setForeground(ColorConfigBean.getInstance().getTagFgColor());
		switch (innerTagBean.getType()) {
		case START:
			gc.drawText(strInx, tagAreaPoints[0] + MARGIN_H, tagAreaPoints[1] + MARGIN_V);
			break;
		default:
			gc.drawText(strInx, tagAreaPoints[2] + MARGIN_H, tagAreaPoints[3] + MARGIN_V);
			break;
		}
		gc.setBackground(gcBgColor);
		gc.setForeground(gcFgColor);
		gc.setFont(gcFont);
	}

	private int[] calculateTagArea(Point tagSize, InnerTagBean innerTagBean, int x, int y) {
		int sawtooth = tagSize.y / 2;
		int[] pointArray = null;
		int x1 = x;
		int y1 = y;
		int y2 = y1 + tagSize.y / 2;
		int y3 = y1 + tagSize.y;
		switch (innerTagBean.getType()) {
		case START:
			int x3 = x1 + tagSize.x;
			int x2 = x3 - sawtooth;
			pointArray = new int[] { x1, y1, x2, y1, x3, y2, x2, y3, x1, y3 };
			// System.out.println( ""+ x1 +" "+ y1+", "+ x2+" "+ y1+ ", "+ x3+" "+ y2+", "+ x2+" "+ y3+ ", "+x1+" "+
			// y3);
			// System.out.println( ""+ (x1+MARGIN_H) +" "+ (y1+MARGIN_V));
			break;
		case END:
			x2 = x1 + sawtooth;
			x3 = x1 + tagSize.x;
			// System.out.println( ""+ x1 +" "+ y2+", "+ x2+" "+ y1+ ", "+x3+" "+y1+","+ x3+" "+ y3+", "+ x2+" "+ y3);
			pointArray = new int[] { x1, y2, x2, y1, x3, y1, x3, y3, x2, y3 };
			break;
		case STANDALONE:
			int x4 = x1 + tagSize.x;
			x2 = x1 + sawtooth;
			x3 = x4 - sawtooth;
			// System.out.println( ""+ x1 +" "+ y2+", "+ x2+" "+ y1+ ", "+x3+" "+y1+","+x4+" "+y2+","+ x3+" "+ y3+", "+
			// x2+" "+ y3);
			pointArray = new int[] { x1, y2, x2, y1, x3, y1, x4, y2, x3, y3, x2, y3 };
			break;
		default:
			break;
		}
		return pointArray;
	}

}

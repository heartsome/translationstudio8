/**
 * HSDropDownButton.java
 *
 * Version information :
 *
 * Date:2013-4-22
 *
 * Copyright notice :
 * 本文件及其附带的相关文件包含机密信息，仅限瀚特盛科技有限公司指定的，与本项目有关的内部人员和客户联络人员查阅和使用。 
 * 如果您不是本保密声明中指定的收件者，请立即销毁本文件，禁止对本文件或根据本文件中的内容采取任何其他行动， 
 * 包括但不限于：泄露本文件中的信息、以任何方式制作本文件全部或部分内容之副本、将本文件或其相关副本提供给任何其他人。
 */
package net.heartsome.cat.common.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;

/**
 * @author Jason
 * @version
 * @since JDK1.6
 */
public class HSDropDownButton extends Button {

	private final static int DEFAULT_SPACES = 15;
	private String EMPTY_SPACE = getSpaceByWidth(DEFAULT_SPACES);
	private final static Color COLOR__BLACK = Display.getDefault().getSystemColor(SWT.COLOR_BLACK);
	private Menu menu;

	public HSDropDownButton(Composite parent, int style) {
		super(parent, SWT.PUSH);
		setText("");
		super.addPaintListener(new PaintListener() {

			public void paintControl(PaintEvent e) {
				// draw the arrow
				Rectangle rect = getBounds();
				Color oldForeground = e.gc.getForeground();
				Color oldBackground = e.gc.getBackground();

				int dx = -e.gc.getClipping().x;
				int dy = -e.gc.getClipping().y;

				e.gc.setForeground(COLOR__BLACK);
				e.gc.setBackground(COLOR__BLACK);
				e.gc.fillPolygon(new int[] { e.x + rect.width - 15 + dx, e.y + rect.height / 2 - 1 + dy,
						e.x + rect.width - 8 + dx, e.y + rect.height / 2 - 1 + dy, e.x + rect.width - 12 + dx,
						e.y + rect.height / 2 + 3 + dy });

				e.gc.setForeground(oldForeground);
				e.gc.setBackground(oldBackground);
			}
		});
		super.addListener(SWT.MouseDown, new Listener() {

			public void handleEvent(Event event) {
				Button button = (Button) event.widget;
				Rectangle rect = button.getBounds();
				Point p = button.toDisplay(rect.x, rect.y + rect.height);
				getMenu().setLocation(p.x - rect.x, p.y - rect.y);
				getMenu().setVisible(true);
			}
		});
		menu = new Menu(getShell(), SWT.POP_UP);
	}

	public Menu getMenu() {
		return menu;
	}

	public void setMenu(Menu menu) {
		this.menu = menu;
	}

	@Override
	protected void checkSubclass() {

	}

	@Override
	public void setText(String string) {
		if (string != null) {
			super.setText(string + EMPTY_SPACE);
		}
	}

	@Override
	public String getText() {
		return super.getText().trim();
	}

	public String getSpaceByWidth(int width) {
		GC gc = new GC(Display.getDefault());
		int spaceWidth = gc.getAdvanceWidth(' ');
		gc.dispose();
		int spacecount = width / spaceWidth;
		StringBuilder b = new StringBuilder();
		while (spacecount-- > 0) {
			b.append(" ");
		}
		return b.toString();
	}
}

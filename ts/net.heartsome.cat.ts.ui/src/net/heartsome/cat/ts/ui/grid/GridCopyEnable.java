/**
 * GridCopyEnable.java
 *
 * Version information :
 *
 * Date:2013-3-15
 *
 * Copyright notice :
 * 本文件及其附带的相关文件包含机密信息，仅限瀚特盛科技有限公司指定的，与本项目有关的内部人员和客户联络人员查阅和使用。 
 * 如果您不是本保密声明中指定的收件者，请立即销毁本文件，禁止对本文件或根据本文件中的内容采取任何其他行动， 
 * 包括但不限于：泄露本文件中的信息、以任何方式制作本文件全部或部分内容之副本、将本文件或其相关副本提供给任何其他人。
 */
package net.heartsome.cat.ts.ui.grid;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.nebula.widgets.grid.Grid;
import org.eclipse.nebula.widgets.grid.Grid.GridVisibleRange;
import org.eclipse.nebula.widgets.grid.GridCellRenderer;
import org.eclipse.nebula.widgets.grid.GridColumn;
import org.eclipse.nebula.widgets.grid.GridItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ST;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.TextLayout;
import org.eclipse.swt.widgets.Caret;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

/**
 * @author Jason
 * @version
 * @since JDK1.6
 */
public class GridCopyEnable {

	private Grid gridTable;
	private Caret defaultCaret;

	private int clickCount = 0;
	private int caretOffset = 0;
	int selectionAnchor = 0;
	Point selection = new Point(0, 0);
	TextLayout layout;
	String focusContent;

	int coordinateOffsetX;
	int coordinateOffsetY;
	Rectangle focusCellRect = new Rectangle(0, 0, 0, 0);

	List<Integer> copyAbleColumnIndexs = new ArrayList<Integer>();
	int focusItemIndex = -1;
	int focusColIndex = -1;

	GridItem focusItem = null;

	Clipboard clipboard;

	final static boolean IS_MAC, IS_GTK, IS_MOTIF;
	static {
		String platform = SWT.getPlatform();
		IS_MAC = "carbon".equals(platform) || "cocoa".equals(platform);
		IS_GTK = "gtk".equals(platform);
		IS_MOTIF = "motif".equals(platform);
	}

	/**
	 * @param gridTable
	 */
	public GridCopyEnable(Grid gridTable) {
		Assert.isNotNull(gridTable);
		this.gridTable = gridTable;
		defaultCaret = new Caret(gridTable, SWT.NONE);
		clipboard = new Clipboard(gridTable.getDisplay());
		this.gridTable.setCaret(defaultCaret);
		this.gridTable.setCursor(Display.getDefault().getSystemCursor(SWT.CURSOR_IBEAM));
		initListener();
	}

	public Point getSelectionRange(int colIndex, GridItem item) {
		if (item == focusItem && colIndex == focusColIndex) {
			return selection;
		}
		return null;
	}

	public int getSelectionCount() {
		return selection.y - selection.x;
	}

	public void addCopyAbleColumn(int colIndex) {
		copyAbleColumnIndexs.add(colIndex);
	}

	/**
	 * 调用 {@link Grid#removeAll()} 之前需要调用此方法。解决内容变化时，选中未变化的问题。 ;
	 */
	public void resetSelection() {
		caretOffset = 0;
		clearSelection();
	}

	private void initListener() {
		gridTable.addListener(SWT.MouseDown, new Listener() {

			public void handleEvent(Event e) {
				if ((e.button != 1) || (IS_MAC && (e.stateMask & SWT.MOD4) != 0)) {
					return;
				}
				clickCount = e.count;
				doMouseLocationChange(e.x, e.y, false);
			}
		});

		gridTable.addListener(SWT.MouseUp, new Listener() {

			public void handleEvent(Event e) {
				clickCount = 0;
			}
		});

		gridTable.addMouseMoveListener(new MouseMoveListener() {

			public void mouseMove(MouseEvent e) {
				if (clickCount > 0) {
					gridTable.update();
					doMouseLocationChange(e.x, e.y, true);
				}
			}
		});

		gridTable.addListener(SWT.KeyDown, new Listener() {

			public void handleEvent(Event event) {
				int t = event.keyCode | event.stateMask;
				if (t == ('C' | SWT.MOD1) || t == ('c' | SWT.MOD1)) {
					// copy event
					doAction(ST.COPY);
				}
			}
		});

		gridTable.getVerticalBar().addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				handleVerticalScroll(event);
			}
		});

		gridTable.addDisposeListener(new DisposeListener() {

			public void widgetDisposed(DisposeEvent e) {
				if (clipboard != null) {
					clipboard.dispose();
				}
				if (defaultCaret != null) {
					defaultCaret.dispose();
				}
			}
		});
	}

	public void doAction(int action) {
		switch (action) {
		case ST.COPY:
			copy();
			break;
		default:
			break;
		}
	}

	void copy() {
		if (focusContent == null || focusContent.equals("")) {
			return;
		}
		if (selection.x != selection.y) {
			String plainText = focusContent.substring(selection.x, selection.y);
			Object[] data = new Object[] { plainText };
			TextTransfer plainTextTransfer = TextTransfer.getInstance();
			Transfer[] types = new Transfer[] { plainTextTransfer };
			clipboard.setContents(data, types);
		}
	}

	void doMouseLocationChange(int x, int y, boolean select) {
		if (gridTable.getItems().length == 0) {
			defaultCaret.setVisible(false);
			return;
		}
		Point eventP = new Point(x, y);
		GridItem _focusItem = gridTable.getItem(eventP);
		GridColumn col = gridTable.getColumn(eventP);
		if (_focusItem == null) {
			return;
		}
		GridCellRenderer gcr = col.getCellRenderer();
		int colIndex = gcr.getColumn();
		if (gcr == null || !(gcr instanceof XGridCellRenderer) || !copyAbleColumnIndexs.contains(colIndex)) {
			return;
		}
		XGridCellRenderer cellRender = (XGridCellRenderer) gcr;

		Rectangle cellBounds = _focusItem.getBounds(colIndex);
		GC gc = new GC(Display.getDefault());
		layout = cellRender.getTextLayout(gc, _focusItem, colIndex, true, false);
		if (layout == null) {
			gc.dispose();
			return;
		}
		focusContent = layout.getText();
		coordinateOffsetX = cellBounds.x + cellRender.leftMargin;
		coordinateOffsetY = cellBounds.y + cellRender.topMargin + cellRender.textTopMargin;
		if (!select) {
			focusCellRect.x = cellBounds.x;
			focusCellRect.y = cellBounds.y;
			focusCellRect.height = cellBounds.height;
			focusCellRect.width = cellBounds.width;
			focusColIndex = colIndex;
			focusItemIndex = gridTable.getIndexOfItem(_focusItem);
		}

		int[] trailing = new int[1];
		int newCaretOffset = layout.getOffset(x - coordinateOffsetX, y - coordinateOffsetY, trailing);

		int newCaretLine = layout.getLineIndex(newCaretOffset + trailing[0]);
		int lineStart = layout.getLineOffsets()[newCaretLine];
		Point point = null;
		if (newCaretOffset + trailing[0] == lineStart && trailing[0] != 0) {
			newCaretOffset += trailing[0];
			newCaretOffset = layout.getPreviousOffset(newCaretOffset, SWT.MOVEMENT_CLUSTER);
			point = layout.getLocation(newCaretOffset, true);
		} else {
			newCaretOffset += trailing[0];
			point = layout.getLocation(newCaretOffset, false);
		}

		// check area, only in cell area effective
		boolean vchange = focusCellRect.y <= y && y < focusCellRect.y + focusCellRect.height;
		boolean hchange = focusCellRect.x <= x && x < focusCellRect.x + focusCellRect.width;

		if (vchange && hchange && newCaretOffset != caretOffset) {
			focusItem = _focusItem;
			caretOffset = newCaretOffset;
			if (select) {
				doMouseSelection();
			}
			defaultCaret.setVisible(true);
			Rectangle rc = layout.getLineBounds(newCaretLine);
			defaultCaret.setBounds(point.x + coordinateOffsetX, point.y + coordinateOffsetY, 0, rc.height);
		}
		if (!select) {
			caretOffset = newCaretOffset;
			clearSelection();
		}
		layout.dispose();
		layout = null;
		gc.dispose();
	}

	/**
	 * Updates the selection based on the caret position
	 */
	void doMouseSelection() {
		if (caretOffset <= selection.x
				|| (caretOffset > selection.x && caretOffset < selection.y && selectionAnchor == selection.x)) {
			doSelection(ST.COLUMN_PREVIOUS);// left
		} else {
			doSelection(ST.COLUMN_NEXT); // right
		}
	}

	void doSelection(int direction) {
		int redrawStart = -1;
		int redrawEnd = -1;
		if (selectionAnchor == -1) {
			selectionAnchor = selection.x;
		}
		if (direction == ST.COLUMN_PREVIOUS) {
			if (caretOffset < selection.x) {
				// grow selection
				redrawEnd = selection.x;
				redrawStart = selection.x = caretOffset;
				if (selectionAnchor != selection.y) {
					redrawEnd = selection.y;
					selection.y = selectionAnchor;
				}
			} else if (selectionAnchor == selection.x && caretOffset < selection.y) {
				// caret moved towards selection anchor (left side of selection).
				// shrink selection
				redrawEnd = selection.y;
				redrawStart = selection.y = caretOffset;
			}
		} else {
			if (caretOffset > selection.y) {
				// grow selection
				redrawStart = selection.y;
				redrawEnd = selection.y = caretOffset;
				if (selection.x != selectionAnchor) {
					redrawStart = selection.x;
					selection.x = selectionAnchor;
				}
			} else if (selectionAnchor == selection.y && caretOffset > selection.x) {
				// caret moved towards selection anchor (right side of selection).
				// shrink selection
				redrawStart = selection.x;
				redrawEnd = selection.x = caretOffset;
			}
		}

		if (redrawStart != -1 && redrawEnd != -1) {
			Rectangle rect = layout.getBounds(redrawStart, redrawEnd);
			gridTable.redraw(rect.x + coordinateOffsetX, rect.y + coordinateOffsetY, rect.width, rect.height, false);
		}
	}

	void clearSelection() {
		int start = selection.x;
		int end = selection.y;
		selection.x = selection.y = caretOffset;
		selectionAnchor = -1;
		// redraw old selection, if any
		if (end - start > 0 && gridTable.getItems().length != 0) {
			if (layout == null && focusItemIndex != -1 && focusItemIndex != -1) {
				GridItem item = gridTable.getItem(focusItemIndex);
				GridColumn col = gridTable.getColumn(focusColIndex);
				GridCellRenderer gcr = col.getCellRenderer();
				if (gcr != null && gcr instanceof XGridCellRenderer) {
					GC gc = new GC(gcr.getDisplay());
					layout = ((XGridCellRenderer) gcr).getTextLayout(gc, item, focusColIndex, true, false);
					gc.dispose();
				}
				if (layout == null) {
					return;
				}
			}
			Rectangle rect = layout.getBounds(start, end);
			gridTable.redraw(rect.x + coordinateOffsetX, rect.y + coordinateOffsetY, rect.width, rect.height, false);
		}
	}

	void handleVerticalScroll(Event event) {
		GridVisibleRange visibleR = gridTable.getVisibleRange();
		GridItem[] items = visibleR.getItems();
		boolean itemFlg = false;
		for (GridItem item : items) {
			if (focusItem == item) {
				itemFlg = true;
			}
		}
		boolean columnFlg = false;
		GridColumn[] columns = visibleR.getColumns();
		if (columns.length - 1 >= focusColIndex) {
			columnFlg = true;
		}
		if (!itemFlg || !columnFlg) {
			defaultCaret.setVisible(false);
			return;
		}
		defaultCaret.setVisible(true);

		GridColumn col = gridTable.getColumn(focusColIndex);
		GridCellRenderer gcr = col.getCellRenderer();
		int colIndex = gcr.getColumn();
		if (gcr == null || !(gcr instanceof XGridCellRenderer) || !copyAbleColumnIndexs.contains(colIndex)) {
			return;
		}
		XGridCellRenderer cellRender = (XGridCellRenderer) gcr;

		Rectangle cellBounds = focusItem.getBounds(colIndex);
		GC gc = new GC(Display.getDefault());
		TextLayout layout = null;
		try {
			layout = cellRender.getTextLayout(gc, focusItem, colIndex, true, false);
			if (layout == null) {
				gc.dispose();
				return;
			}
			Point point = layout.getLocation(caretOffset, false);
			coordinateOffsetX = cellBounds.x + cellRender.leftMargin;
			coordinateOffsetY = cellBounds.y + cellRender.topMargin + cellRender.textTopMargin;
			defaultCaret.setLocation(point.x + coordinateOffsetX, point.y + coordinateOffsetY);
		} finally {
			if (layout != null) {
				layout.dispose();
			}
			if (gc != null) {
				gc.dispose();
			}
		}
	}
}

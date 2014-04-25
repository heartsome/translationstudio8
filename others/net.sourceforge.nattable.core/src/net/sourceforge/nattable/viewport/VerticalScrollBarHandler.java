package net.sourceforge.nattable.viewport;

import static net.sourceforge.nattable.selection.SelectionLayer.MoveDirectionEnum.DOWN;
import static net.sourceforge.nattable.selection.SelectionLayer.MoveDirectionEnum.UP;
import net.sourceforge.nattable.layer.LayerUtil;
import net.sourceforge.nattable.selection.SelectionLayer.MoveDirectionEnum;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ScrollBar;

/**
 * Listener for the Vertical scroll bar events.
 */
public class VerticalScrollBarHandler extends ScrollBarHandlerTemplate implements Listener {

	public VerticalScrollBarHandler(ViewportLayer viewportLayer, ScrollBar scrollBar) {
		super(viewportLayer, scrollBar);
	}

	/**
	 * In a normal scenario scroll by the height of the viewport. If the row
	 * being scrolled is wider than above, use the row height
	 */
	@Override
	int pageScrollDistance() {
		int heightOfRowBeingScrolled = scrollableLayer.getRowHeightByPosition(getScrollablePosition());
		int viewportHeight = viewportLayer.getClientAreaHeight();
		return (heightOfRowBeingScrolled > viewportHeight) ? heightOfRowBeingScrolled : viewportHeight;
	}

	@Override
	int getSpanByPosition(int scrollablePosition) {
		return scrollableLayer.getRowHeightByPosition(scrollablePosition);
	}

	/**
	 * Convert Viewport 0 pos -> Scrollable 0 pos
	 * 
	 * @return
	 */
	@Override
	int getScrollablePosition() {
		return LayerUtil.convertRowPosition(viewportLayer, 0, scrollableLayer);
	}

	@Override
	int getStartPixelOfPosition(int position) {
		return scrollableLayer.getStartYOfRowPosition(position);
	}

	@Override
	int getPositionByPixel(int pixelValue) {
		return scrollableLayer.getRowPositionByY(pixelValue);
	}

	@Override
	void setViewportOrigin(int position) {
		viewportLayer.invalidateVerticalStructure();
		viewportLayer.setOriginRowPosition(position);
		scrollBar.setIncrement(viewportLayer.getRowHeightByPosition(0));
	}

	@Override
	MoveDirectionEnum scrollDirectionForEventDetail(int eventDetail) {
		return (eventDetail == SWT.PAGE_UP || eventDetail == SWT.ARROW_UP) ? UP : DOWN;
	}

	@Override
	boolean keepScrolling() {
		return !viewportLayer.isLastRowCompletelyDisplayed();
	}
	
	@Override
	int getViewportWindowSpan() {
		return viewportLayer.getClientAreaHeight();
	}

	@Override
	int getScrollableLayerSpan() {
		return scrollableLayer.getHeight();
	}
}
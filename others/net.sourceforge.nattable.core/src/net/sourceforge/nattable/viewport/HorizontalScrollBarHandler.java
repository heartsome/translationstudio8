package net.sourceforge.nattable.viewport;

import static net.sourceforge.nattable.selection.SelectionLayer.MoveDirectionEnum.LEFT;
import static net.sourceforge.nattable.selection.SelectionLayer.MoveDirectionEnum.RIGHT;
import net.sourceforge.nattable.layer.LayerUtil;
import net.sourceforge.nattable.selection.SelectionLayer.MoveDirectionEnum;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.ScrollBar;

/**
 * Listener for the Horizontal scroll bar events on the Viewport Layer. State is
 * exposed to this class from the viewport, since it works in close conjnuction
 * with it.
 */
public class HorizontalScrollBarHandler extends ScrollBarHandlerTemplate {

	public HorizontalScrollBarHandler(ViewportLayer viewportLayer, ScrollBar scrollBar) {
		super(viewportLayer, scrollBar);
		
	}

	/**
	 * In a normal scenario scroll by the width of the viewport. 
	 * If the col being scrolled is wider than above, use the col width
	 */
	@Override
	int pageScrollDistance() {
		int widthOfColBeingScrolled = scrollableLayer.getColumnWidthByPosition(getScrollablePosition());
		int viewportWidth = viewportLayer.getClientAreaWidth(); 
		int scrollWidth = (widthOfColBeingScrolled > viewportWidth) ? widthOfColBeingScrolled : viewportWidth;
		return scrollWidth;
	}
	
	@Override
	int getSpanByPosition(int scrollablePosition) {
		return scrollableLayer.getColumnWidthByPosition(scrollablePosition);
	}
	
	@Override
	int getScrollablePosition() {
		return LayerUtil.convertColumnPosition(viewportLayer, 0, scrollableLayer);
	}
	
	@Override
	int getStartPixelOfPosition(int position){
		return scrollableLayer.getStartXOfColumnPosition(position);
	}
	
	@Override
	int getPositionByPixel(int pixelValue) {
		return scrollableLayer.getColumnPositionByX(pixelValue);
	}

	@Override
	void setViewportOrigin(int position) {
		viewportLayer.invalidateHorizontalStructure();
		viewportLayer.setOriginColumnPosition(position);
		scrollBar.setIncrement(viewportLayer.getColumnWidthByPosition(0));
	}
	
	@Override
	MoveDirectionEnum scrollDirectionForEventDetail(int eventDetail){
		return (eventDetail == SWT.PAGE_UP || eventDetail == SWT.ARROW_UP )	? LEFT : RIGHT;
	}
	
	@Override
	boolean keepScrolling() {
		return !viewportLayer.isLastColumnCompletelyDisplayed();
	}
	
	@Override
	int getViewportWindowSpan() {
		return viewportLayer.getClientAreaWidth();
	}

	@Override
	int getScrollableLayerSpan() {
		return scrollableLayer.getWidth();
	}
	
}
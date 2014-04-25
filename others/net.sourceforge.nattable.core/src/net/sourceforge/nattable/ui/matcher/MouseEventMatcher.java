package net.sourceforge.nattable.ui.matcher;

import net.sourceforge.nattable.NatTable;
import net.sourceforge.nattable.grid.GridRegion;
import net.sourceforge.nattable.layer.LabelStack;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.eclipse.swt.events.MouseEvent;

public class MouseEventMatcher implements IMouseEventMatcher {

	public static final int LEFT_BUTTON = 1;
	public static final int RIGHT_BUTTON = 3;

	private final int stateMask;
	private final String regionName;
	private final int button;

	public MouseEventMatcher() {
		this(0, null, 0);
	}

	public MouseEventMatcher(String eventRegionName) {
		this(0, eventRegionName, 0);
	}

	public MouseEventMatcher(String eventRegion, int button) {
		this(0, eventRegion, button);
	}

	/**
	 * Constructor
	 * @param stateMask @see "org.eclipse.swt.events.MouseEvent.stateMask"
	 * @param eventRegion {@linkplain net.sourceforge.nattable.grid.GridRegionEnum}
	 * @param button @see "org.eclipse.swt.events.MouseEvent.button"<br/>
	 *  	{@link MouseEventMatcher#LEFT_BUTTON}, {@link MouseEventMatcher#RIGHT_BUTTON}
	 *  	can be used for convenience
	 */
	public MouseEventMatcher(int stateMask, String eventRegion, int button) {
		this.stateMask = stateMask;
		this.regionName = eventRegion;
		this.button = button;
	}

	public boolean matches(NatTable natTable, MouseEvent event, LabelStack regionLabels) {
		if (regionLabels == null) {
			return false;
		}

		boolean stateMaskMatches;
		if (stateMask != 0) {
			stateMaskMatches = (event.stateMask == stateMask) ? true : false;
		} else {
			stateMaskMatches = event.stateMask == 0;
		}

		boolean eventRegionMatches;
		if (this.regionName != null) {
			eventRegionMatches = regionLabels.hasLabel(regionName);
		} else {
			eventRegionMatches = true;
		}

		boolean buttonMatches = button == event.button;

		return stateMaskMatches && eventRegionMatches && buttonMatches;
	}

	@Override
    public boolean equals(Object obj) {
		if (obj instanceof MouseEventMatcher == false) {
			return false;
		}

		if (this == obj) {
			return true;
		}

		MouseEventMatcher rhs = (MouseEventMatcher) obj;

		return new EqualsBuilder()
			.append(stateMask, rhs.stateMask)
			.append(regionName, rhs.regionName)
			.append(button, rhs.button)
			.isEquals();
	}

	@Override
    public int hashCode() {
		return new HashCodeBuilder(43, 21)
			.append(stateMask)
			.append(regionName)
			.append(button)
			.toHashCode();
	}

	public int getStateMask() {
		return stateMask;
	}

	public String getEventRegion() {
		return regionName;
	}

	public int getButton() {
		return button;
	}


	public static MouseEventMatcher columnHeaderLeftClick(int mask) {
	    return new MouseEventMatcher(mask, GridRegion.COLUMN_HEADER, LEFT_BUTTON);
	}

	public static MouseEventMatcher rowHeaderLeftClick(int mask) {
		return new MouseEventMatcher(mask, GridRegion.ROW_HEADER, LEFT_BUTTON);
	}

	public static MouseEventMatcher bodyLeftClick(int mask) {
		return new MouseEventMatcher(mask, GridRegion.BODY, LEFT_BUTTON);
	}

	public static MouseEventMatcher columnGroupHeaderLeftClick(int mask) {
	    return new MouseEventMatcher(mask, GridRegion.COLUMN_GROUP_HEADER, LEFT_BUTTON);
	}

}


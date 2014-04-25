package net.sourceforge.nattable.ui.matcher;

import net.sourceforge.nattable.NatTable;
import net.sourceforge.nattable.layer.LabelStack;
import net.sourceforge.nattable.ui.NatEventData;

import org.eclipse.swt.events.MouseEvent;

/**
 * Matches mouse clicks on cells to which a specified configuration label has been applied.
 */
public class CellLabelMouseEventMatcher extends MouseEventMatcher {

	private final String labelToMatch;

	public CellLabelMouseEventMatcher(String regionName, int button, String labelToMatch) {
		super(regionName, button);
		this.labelToMatch = labelToMatch;
	}

	public CellLabelMouseEventMatcher(int stateMask, String regionName, int button, String labelToMatch) {
		super(stateMask, regionName, button);
		this.labelToMatch = labelToMatch;
	}

	@Override
	public boolean matches(NatTable natTable, MouseEvent event, LabelStack regionLabels) {
		NatEventData eventData = NatEventData.createInstanceFromEvent(event);
		LabelStack customLabels = natTable.getConfigLabelsByPosition(eventData.getColumnPosition(), eventData.getRowPosition());

		return super.matches(natTable, event, regionLabels)	&& customLabels.getLabels().contains(labelToMatch);
	}

}

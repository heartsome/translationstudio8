package net.sourceforge.nattable.grid.data;

import net.sourceforge.nattable.data.IDataProvider;
import net.sourceforge.nattable.grid.layer.RowHeaderLayer;
import net.sourceforge.nattable.summaryrow.SummaryRowLayer;

/**
 * {@link IDataProvider} to use for the {@link RowHeaderLayer} if the {@link SummaryRowLayer} is present in the body layer stack. <br/>
 * This adds an extra row to the row header for displaying the summary row.
 */
public class DefaultSummaryRowHeaderDataProvider extends DefaultRowHeaderDataProvider implements IDataProvider {

	public static final String DEFAULT_SUMMARY_ROW_LABEL = "Summary";
	private final String summaryRowLabel;

	public DefaultSummaryRowHeaderDataProvider(IDataProvider bodyDataProvider) {
		this(bodyDataProvider, DEFAULT_SUMMARY_ROW_LABEL);
	}

	/**
	 * @param summaryRowLabel label to display in the row header for the Summary Row
	 */
	public DefaultSummaryRowHeaderDataProvider(IDataProvider bodyDataProvider, String summaryRowLabel) {
		super(bodyDataProvider);
		this.summaryRowLabel = summaryRowLabel;
	}

	public int getRowCount() {
		return super.getRowCount() + 1;
	}

	@Override
	public Object getDataValue(int columnIndex, int rowIndex) {
		if (rowIndex == super.getRowCount()){
			return summaryRowLabel;
		}
		return super.getDataValue(columnIndex, rowIndex);
	}
}

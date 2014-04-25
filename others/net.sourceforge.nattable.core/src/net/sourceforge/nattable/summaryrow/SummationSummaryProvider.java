package net.sourceforge.nattable.summaryrow;

import net.sourceforge.nattable.data.IDataProvider;

public class SummationSummaryProvider implements ISummaryProvider {

	private IDataProvider dataProvider;
	
	public SummationSummaryProvider(IDataProvider dataProvider) {
		this.dataProvider = dataProvider;
	}

	/**
	 * @return sum of all the numbers in the column (as Floats).
	 * 	DEFAULT_SUMMARY_VALUE for non-numeric columns
	 */
	public Object summarize(int columnIndex) {
		int rowCount = dataProvider.getRowCount();
		float summaryValue = 0;
		
		for (int rowIndex = 0; rowIndex < rowCount; rowIndex++) {
			Object dataValue = dataProvider.getDataValue(columnIndex, rowIndex);

			if (!(dataValue instanceof Number)) {
				return DEFAULT_SUMMARY_VALUE;
			}

			summaryValue = summaryValue + Float.parseFloat(dataValue.toString());
		}
			
		return summaryValue;
	}
}


package net.sourceforge.nattable.summaryrow;

/**
 * Summarizes the values in a column.<br/>
 * Used by the {@link SummaryRowLayer} to calculate summary values.
 */
public interface ISummaryProvider {

	public static final Object DEFAULT_SUMMARY_VALUE = "...";

	/**
	 * @param columnIndex
	 *            for which the summary is required
	 * @return the summary value for the column
	 */
	public Object summarize(int columnIndex);

	/**
	 * Register this instance to indicate that a summary is not required.<br/>
	 * Doing so avoids calls to the {@link ISummaryProvider} and is a performance tweak.
	 */
	public static final ISummaryProvider NONE = new ISummaryProvider() {
		public Object summarize(int columnIndex) {
			return null;
		}
	};

	public static final ISummaryProvider DEFAULT = new ISummaryProvider() {
		public Object summarize(int columnIndex) {
			return DEFAULT_SUMMARY_VALUE;
		}
	};
}

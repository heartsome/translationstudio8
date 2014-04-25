package net.sourceforge.nattable.tickupdate;

public interface ITickUpdateHandler {
	
	public boolean isApplicableFor(Object value);
	
	/**
	 * @param currentValue of the cell
	 * @return new value after INcrementing
	 */
	public Object getIncrementedValue(Object currentValue);

	/**
	 * @param currentValue of the cell
	 * @return new value after DEcrementing
	 */
	public Object getDecrementedValue(Object currentValue);

	// Default implementation
	
	ITickUpdateHandler UPDATE_VALUE_BY_ONE = new ITickUpdateHandler() {

		public boolean isApplicableFor(Object value) {
			return value instanceof Number;
		}
		
		public Object getDecrementedValue(Object currentValue) {
			Number oldValue = (Number) currentValue;
			return Float.valueOf(oldValue.floatValue() - 1);
		}

		public Object getIncrementedValue(Object currentValue) {
			Number oldValue = (Number) currentValue;
			return Float.valueOf(oldValue.floatValue() + 1);
		}
		
	};

}

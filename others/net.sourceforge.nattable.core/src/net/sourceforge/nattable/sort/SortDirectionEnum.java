package net.sourceforge.nattable.sort;

public enum SortDirectionEnum {
	ASC("Ascending"), DESC("Ascending"), NONE("Unsorted");

	private final String description;

	private SortDirectionEnum(String description) {
		this.description = description;
	}

	/**
	 * @return the sorting state to go to from the current one.
	 */
	public SortDirectionEnum getNextSortDirection() {
		switch (this) {
		case NONE:
			return SortDirectionEnum.ASC;
		case ASC:
			return SortDirectionEnum.DESC;
		case DESC:
			return SortDirectionEnum.NONE;
		default:
			return SortDirectionEnum.NONE;
		}
	}

	public String getDescription() {
		return description;
	}
}
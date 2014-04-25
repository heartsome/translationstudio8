package net.sourceforge.nattable.columnChooser;



/**
 * Object representation of a NatTable Column. <br/>
 * This is used in the Column chooser dialogs as a mechanism of preserving
 * meta data on the columns in the dialog.
 * 
 * @see ColumnChooserUtils
 */
public class ColumnEntry {

	private final String label;
	private final Integer index;
	private Integer position;

	public ColumnEntry(String label, Integer index, Integer position) {
		this.label = label;
		this.index = index;
		this.position = position;
	}

	@Override
	public String toString() {
		return label != null ? label : "No Label";
	}

	public Integer getPosition() {
		return position;
	}

	public void setPosition(Integer position) {
		this.position = position;
	}

	public Integer getIndex() {
		return index;
	}

	public String getLabel() {
		return toString();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ColumnEntry) {
			ColumnEntry that = (ColumnEntry) obj;
			return index.intValue() == that.index.intValue();
		}

		return super.equals(obj);
	}
	
	@Override
	public int hashCode() {
		return index.hashCode();
	}
}
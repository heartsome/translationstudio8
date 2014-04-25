package net.heartsome.cat.common.bean;

/**
 * prop
 * @author jason
 * @version
 * @since JDK1.6
 */
public class TmxProp {

	/** The prop node type name */
	private String name;
	
	/** the prop node content */
	private String value;

	public TmxProp() {
	}

	public TmxProp(String name, String value) {
		name = name == null ? "" : name;
		value = value == null ? "" : value;
		this.name = name;
		this.value = value;
	}

	/**
	 * Both name and value equals then return true, otherwise return false;  
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof TmxProp) {
			TmxProp _obj = (TmxProp) obj;
			if (_obj.name.equals(this.name) && _obj.value.equals(this.value)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Set the value of name
	 * @param newVar
	 *            the new value of name
	 */
	public void setName(String newVar) {
		name = newVar;
	}

	/**
	 * Get the value of name
	 * @return the value of name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Set the value of value
	 * @param newVar
	 *            the new value of value
	 */
	public void setValue(String newVar) {
		value = newVar;
	}

	/**
	 * Get the value of value
	 * @return the value of value
	 */
	public String getValue() {
		return value;
	}

}

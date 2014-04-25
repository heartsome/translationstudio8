package net.sourceforge.nattable.blink;

/**
 * This interface is used to determine whether a change requires a blink.  
 * This is a way to add thresholds to blinking.
 */
public interface IBlinkingCellResolver {
	
	/**
	 * @param oldValue
	 * @param newValue
	 * @return Possibly the config type associated with the blinking style.
	 */
	public String[] resolve(Object oldValue, Object newValue);
	
}
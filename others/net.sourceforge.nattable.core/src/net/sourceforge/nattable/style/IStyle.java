package net.sourceforge.nattable.style;

/**
 * Used to store attributes reflecting a (usually display) style.
 */
public interface IStyle {
		
	public <T> T getAttributeValue(ConfigAttribute<T> styleAttribute);
	
	public <T> void setAttributeValue(ConfigAttribute<T> styleAttribute, T value);
}
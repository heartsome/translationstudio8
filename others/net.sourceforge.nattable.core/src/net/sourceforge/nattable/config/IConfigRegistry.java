package net.sourceforge.nattable.config;

import java.util.List;

import net.sourceforge.nattable.style.ConfigAttribute;
import net.sourceforge.nattable.style.DisplayMode;
import net.sourceforge.nattable.style.IDisplayModeOrdering;

/**
 * Holds all the settings, bindings and other configuration for NatTable.</br>
 *
 * @see ConfigRegistry
 * @see ConfigRegistryTest for a better understanding
 */
public interface IConfigRegistry {

	/**
	 * If retrieving registered values<br/>
	 * Example 1:<br/>
	 * 	 configRegistry.getConfigAttribute(attribute, DisplayMode.EDIT);<br/>
	 * <ol>
	 * <li>It will look for an attribute registered using the EDIT display mode</li>
	 * <li>If it can't find that it will try and find an attribute under the NORMAL mode</li>
	 * <li>If it can't find one it will try and find one registered without a display mode {@link #registerConfigAttribute(ConfigAttribute, Object)}</li>
	 * </ol>
	 * Example 2:<br/>
	 *   configRegistry.getConfigAttribute(attribute, DisplayMode.NORMAL, "testLabel", "testLabel_1");<br/>
	 * <ol>
	 * <li>It will look for an attribute registered by display mode NORMAL and "testLabel"<li/>
	 * <li>It will look for an attribute registered by display mode NORMAL and "testLabel_1"<li/>
	 * </ol>
	 * @param <T> Type of the attribute
	 * @param configAttribute to be registered
	 * @param targetDisplayMode display mode the cell needs to be in, for this attribute to be returned
	 * @param configLabels the cell needs to have,  for this attribute to be returned
	 * @return the configAttribute, if the display mode and the configLabels match
	 */
	public <T> T getConfigAttribute(ConfigAttribute<T> configAttribute, String targetDisplayMode, String...configLabels);

	/**
	 * @see #getConfigAttribute(ConfigAttribute, String, String...)
	 */
	public <T> T getConfigAttribute(ConfigAttribute<T> configAttribute, String targetDisplayMode, List<String> configLabels);

	/**
	 * @see #getConfigAttribute(ConfigAttribute, String, String...)
	 */
	public <T> T getSpecificConfigAttribute(ConfigAttribute<T> configAttribute, String displayMode, String configLabel);


	/**
	 * Register a configuration attribute
	 */
	public <T> void registerConfigAttribute(ConfigAttribute<T> configAttribute, T attributeValue);

	/**
	 * Register an attribute against a {@link DisplayMode}.
	 */
	public <T> void registerConfigAttribute(ConfigAttribute<T> configAttribute, T attributeValue, String targetDisplayMode);

	/**
	 * Register an attribute against a {@link DisplayMode} and configuration label (applied to cells)
	 */
	public <T> void registerConfigAttribute(ConfigAttribute<T> configAttribute, T attributeValue, String targetDisplayMode, String configLabel);

	public <T> void unregisterConfigAttribute(Class<T> configAttributeType, String displayMode, String configLabel);

	public IDisplayModeOrdering getDisplayModeOrdering();

}

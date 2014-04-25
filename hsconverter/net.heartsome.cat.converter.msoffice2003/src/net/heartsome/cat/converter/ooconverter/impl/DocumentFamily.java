/**
 * DocumentFamily.java
 *
 * Version information :
 *
 * Date:Jan 14, 2010
 *
 * Copyright notice :
 */
package net.heartsome.cat.converter.ooconverter.impl;

import java.util.HashMap;
import java.util.Map;

import net.heartsome.cat.converter.msoffice2003.resource.Messages;

/**
 * The Class DocumentFamily.
 * @author John Zhu
 * @version
 * @since JDK1.6
 */
@SuppressWarnings("unchecked")
public final class DocumentFamily {

	/** The Constant TEXT. */
	public static final DocumentFamily TEXT = new DocumentFamily("Text"); //$NON-NLS-1$

	/** The Constant SPREADSHEET. */
	public static final DocumentFamily SPREADSHEET = new DocumentFamily("Spreadsheet"); //$NON-NLS-1$

	/** The Constant PRESENTATION. */
	public static final DocumentFamily PRESENTATION = new DocumentFamily("Presentation"); //$NON-NLS-1$

	/** The Constant DRAWING. */
	public static final DocumentFamily DRAWING = new DocumentFamily("Drawing"); //$NON-NLS-1$

	/** The FAMILIES. */
	private static Map mapFamilies = new HashMap();
	static {
		mapFamilies.put(TEXT.name, TEXT);
		mapFamilies.put(SPREADSHEET.name, SPREADSHEET);
		mapFamilies.put(PRESENTATION.name, PRESENTATION);
		mapFamilies.put(DRAWING.name, DRAWING);
	}

	/** The name. */
	private String name;

	/**
	 * Instantiates a new document family.
	 * @param name
	 *            the name
	 */
	private DocumentFamily(String name) {
		this.name = name;
	}

	/**
	 * Gets the name.
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Gets the family.
	 * @param name
	 *            the name
	 * @return the family
	 */
	public static DocumentFamily getFamily(String name) {
		DocumentFamily family = (DocumentFamily) mapFamilies.get(name);
		if (family == null) {
			throw new IllegalArgumentException(Messages.getString("impl.DocumentFamily.0") + name); //$NON-NLS-1$
		}
		return family;
	}
}
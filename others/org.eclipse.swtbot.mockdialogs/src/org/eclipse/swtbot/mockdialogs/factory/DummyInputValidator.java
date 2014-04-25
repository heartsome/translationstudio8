package org.eclipse.swtbot.mockdialogs.factory;

/*******************************************************************************
 * Copyright (c) 2009 Jan Petranek.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 *******************************************************************************/

import org.eclipse.jface.dialogs.IInputValidator;

/**
 * The DummyInputValidator will accept any input.
 * 
 * @author Jan Petranek
 * 
 */
public class DummyInputValidator implements IInputValidator {

	/**
	 * Always accepts the input.
	 * 
	 * @param newText
	 *            text to accept
	 * @return always returns null
	 * @see org.eclipse.jface.dialogs.IInputValidator#isValid(java.lang.String)
	 */
	public String isValid(String newText) {

		return null;
	}

}

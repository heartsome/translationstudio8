package org.eclipse.swtbot.mockdialogs.factory;

/*******************************************************************************
 * Copyright (c) 2009 Jan Petranek.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 *******************************************************************************/

import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

/**
 * The Class NativeDialogFactory is a configurable Dialog factory.
 * 
 * By default, it handles user dialogs with native dialogs (SWT). 
 * When the state is set to  TESTING, it displays stand-in dialogs. 
 * Those stand-ins may be way simpler than the native widgets, 
 * but they are accessible by SWTBot.
 * 
 * @author Jan Petranek
 */
public class NativeDialogFactory {

	/** The Log4j logger instance. */
//	static final Logger logger = Logger.getLogger(NativeDialogFactory.class);

	/**
	 * The Enumeration DialogState, used to indicate the mode of operation.
	 */
	public enum OperationMode {

		/** The DEFAULT state, for normal operation. */
		DEFAULT,
		/** The TESTING state, used to indicate SWTBot-Testing needs stand-in dialogs. */
		TESTING
	};

	/** The mode of operation. */
	private static OperationMode mode = OperationMode.DEFAULT;

	/**
	 * Sets the  operation mode.
	 * 
	 * @param state the desired operation mode
	 */
	public static void setMode(OperationMode mode) {
		NativeDialogFactory.mode = mode;
	}

	/**
	 * Gets the operation mode.
	 * 
	 * @return the current operation mode
	 */
	public static OperationMode getMode() {
		return mode;
	}

	/**
	 * Shows a file selection dialog.
	 * 
	 * In default mode, this displays a native file selection dialog.
	 * In testing mode, a simple InputDialog is displayed, where the path can be entered as a String.
	 * 
	 * @param shell the parent shell
	 * @param text title for the file selection dialog
	 * @param style the style of the file dialog, applies only to native dialogs (SWT.SAVE| SWT.OPEN | SWT.MULTI)
	 * 
	 * @return a String with the selected file name. It is still up to the user to check, if the
	 * filename is valid. When the user has aborted the file selection, this returns null.  
	 * 
	 */
	public static String fileSelectionDialog(Shell shell, String text, int style) {
		OperationMode mode = getMode();
		switch (mode) {
		case DEFAULT: {
			// File standard dialog
			FileDialog fileDialog = new FileDialog(shell, style);
			fileDialog.setText(text);
			return fileDialog.open();
		}
		case TESTING: {
			InputDialog fileDialog = new InputDialog(shell, text,
					"Select a file", "", new DummyInputValidator());
			fileDialog.open();
			return fileDialog.getValue();
		}

		default:
			final String msg = "Reached default case in NativeDialogFactory.fileSelectionDialog, this is a bug, unknown state "
					+ getMode();
//			logger.warn(msg);
			System.err.println(msg);
			throw new RuntimeException(msg);

		}

	}

	/**
	 * Show message box.
	 * 
	 * In default mode, a native MessageBox is used.
	 * In testing mode, we use a MessageDialog, showing the same title and message.
	 * 	  
	 * @param messageText the text of the message
	 * @param title the title
	 * @param iconStyle the icon style
	 * @param shell the parent shell
	 */
	public static void showMessageBox(Shell shell, String messageText,
			final String title, final int iconStyle) {
		if (shell == null) {
//			logger
//					.fatal("Shell not yet instantiated, cannot display error message");
			System.err.println("Shell not yet instantiated, cannot display error message");
		} else {
			switch (getMode()) {
			case DEFAULT: {
				MessageBox messageBox = new MessageBox(shell, iconStyle);
				messageBox.setMessage(messageText);

				messageBox.setText(title);

				messageBox.open();
				break;
			}
			case TESTING: {
				// ignore the iconStyle, this only creates trouble when testing.
				MessageDialog messagDialog = new MessageDialog(shell, title,
						null, messageText, MessageDialog.NONE,
						new String[] { "OK" }, 0);
				messagDialog.open();
				break;
			}
			default:
				final String msg = "Reached default case in NativeDialogFactory, this is a bug, unknown state "
						+ getMode();
//				logger.warn(msg);
				System.err.println(msg);
				throw new RuntimeException(msg);
			}
		}
	}
}

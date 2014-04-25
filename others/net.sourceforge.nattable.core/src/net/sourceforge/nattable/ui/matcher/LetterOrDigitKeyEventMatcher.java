package net.sourceforge.nattable.ui.matcher;

import org.eclipse.swt.events.KeyEvent;

public class LetterOrDigitKeyEventMatcher implements IKeyEventMatcher {
	
	public static boolean isLetterOrDigit(char character) {
		return Character.isLetterOrDigit(character) || character == '.';
	}

	public boolean matches(KeyEvent event) {
		return isLetterOrDigit(event.character);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof LetterOrDigitKeyEventMatcher == false) {
			return false;
		}
		
		return true;
	}
	
	public int hashCode() {
		return 317;
	}

}

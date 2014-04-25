package net.sourceforge.nattable.ui.matcher;


import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.eclipse.swt.events.KeyEvent;

public class KeyEventMatcher implements IKeyEventMatcher {

	private int stateMask;
	
	private int keyCode;
	
	public KeyEventMatcher(int keyCode) {
		this(0, keyCode);
	}
	
	public KeyEventMatcher(int stateMask, int keyCode) {
		this.stateMask = stateMask;
		this.keyCode = keyCode;
	}
	
	public int getStateMask() {
		return stateMask;
	}
	
	public int getKeyCode() {
		return keyCode;
	}
	
	public boolean matches(KeyEvent event) {
		boolean stateMaskMatches = stateMask == event.stateMask;
		
		boolean keyCodeMatches = keyCode == event.keyCode;
		
		return stateMaskMatches && keyCodeMatches;
	}
	
	public boolean equals(Object obj) {
		if (obj instanceof KeyEventMatcher == false) {
			return false;
		}
		
		if (this == obj) {
			return true;
		}
		
		KeyEventMatcher rhs = (KeyEventMatcher) obj;
		
		return new EqualsBuilder()
			.append(stateMask, rhs.stateMask)
			.append(keyCode, rhs.keyCode)
			.isEquals();
	}
	
	public int hashCode() {
		return new HashCodeBuilder(71, 7)
			.append(stateMask)
			.append(keyCode)
			.toHashCode();
	}
	
}

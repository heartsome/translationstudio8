package net.sourceforge.nattable.ui.mode;


import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseEvent;

public class AbstractModeEventHandler implements IModeEventHandler {

	private ModeSupport modeSupport;
	
	public AbstractModeEventHandler(ModeSupport modeSupport) {
		this.modeSupport = modeSupport;
	}
	
	protected ModeSupport getModeSupport() {
		return modeSupport;
	}
	
	protected void switchMode(String mode) {
		modeSupport.switchMode(mode);
	}
	
	protected void switchMode(IModeEventHandler modeEventHandler) {
		modeSupport.switchMode(modeEventHandler);
	}
	
	public void cleanup() {
	}
	
	public void keyPressed(KeyEvent event) {
	}

	public void keyReleased(KeyEvent event) {
	}

	public void mouseDoubleClick(MouseEvent event) {
	}

	public void mouseDown(MouseEvent event) {
	}

	public void mouseUp(MouseEvent event) {
	}

	public void mouseMove(MouseEvent event) {
	}

	public void focusGained(FocusEvent event) {
	}

	public void focusLost(FocusEvent event) {
		switchMode(Mode.NORMAL_MODE);
	}

}

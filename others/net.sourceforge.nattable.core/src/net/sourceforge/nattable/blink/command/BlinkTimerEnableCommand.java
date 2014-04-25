package net.sourceforge.nattable.blink.command;

import net.sourceforge.nattable.command.AbstractContextFreeCommand;

public class BlinkTimerEnableCommand extends AbstractContextFreeCommand {

	private boolean enableBlinkTimer;

	public BlinkTimerEnableCommand(boolean enableBlinkTimer) {
		this.enableBlinkTimer = enableBlinkTimer;
	}
	
	public boolean isEnableBlinkTimer() {
		return enableBlinkTimer;
	}
	
	public void setEnableBlinkTimer(boolean enableBlinkTimer) {
		this.enableBlinkTimer = enableBlinkTimer;
	}
}
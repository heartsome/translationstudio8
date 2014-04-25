package net.sourceforge.nattable.blink.command;

import net.sourceforge.nattable.blink.BlinkLayer;
import net.sourceforge.nattable.command.AbstractLayerCommandHandler;

public class BlinkTimerEnableCommandHandler extends AbstractLayerCommandHandler<BlinkTimerEnableCommand> {

	private final BlinkLayer<?> blinkLayer;

	public BlinkTimerEnableCommandHandler(BlinkLayer<?> blinkLayer) {
		this.blinkLayer = blinkLayer;
	}
	
	public Class<BlinkTimerEnableCommand> getCommandClass() {
		return BlinkTimerEnableCommand.class;
	}

	@Override
	protected boolean doCommand(BlinkTimerEnableCommand command) {
		blinkLayer.setBlinkingEnabled(command.isEnableBlinkTimer());
		return true;
	}

}

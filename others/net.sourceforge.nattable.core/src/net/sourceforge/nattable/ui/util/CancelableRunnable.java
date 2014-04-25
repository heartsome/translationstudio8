package net.sourceforge.nattable.ui.util;

import java.util.concurrent.atomic.AtomicBoolean;

public abstract class CancelableRunnable implements Runnable {
	
	private final AtomicBoolean cancel = new AtomicBoolean(false);
	
	public void cancel() {
		cancel.set(true);
	}
	
	protected final boolean isCancelled() {
		return cancel.get();
	}

}

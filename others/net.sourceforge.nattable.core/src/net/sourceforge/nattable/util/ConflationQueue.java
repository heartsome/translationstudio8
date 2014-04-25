package net.sourceforge.nattable.util;

public class ConflationQueue extends UpdateQueue {
	private static ConflationQueue queue = null;

	public ConflationQueue() {
		sleep = 300;
	}

	public static ConflationQueue getInstance() {
		if (queue == null) {
			queue = new ConflationQueue();
		}
		return queue;
	}

}

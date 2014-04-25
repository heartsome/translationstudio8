package net.sourceforge.nattable.util;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * GUI Update Event Queue
 */
public class UpdateQueue {

	private static final Log log = LogFactory.getLog(UpdateQueue.class);
	
	private ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

	private Map<String, Runnable> runnableMap = new HashMap<String, Runnable>();

	private Thread thread = null;

	private boolean stop = false;

	protected long sleep = 100;

	private static UpdateQueue queue = null;

	protected UpdateQueue() {
		// no-op
	}

	public static UpdateQueue getInstance() {
		if (queue == null) {
			queue = new UpdateQueue();
		}
		return queue;
	}

	private Runnable runnable = new Runnable() {

		public void run() {
			try {
				while (!stop) {

					// Block thread and make sure that we are doing the
					// latest orders only

					lock.writeLock().lock();
					Runnable[] runnables = runnableMap.values().toArray(
							new Runnable[runnableMap.size()]);
					runnableMap.clear();
					lock.writeLock().unlock();

					int len = runnables != null ? runnables.length : 0;

					for (int i = 0; i < len; i++) {
						try {
							runnables[i].run();
						} catch (Exception e) {
							log.error(e);
						}
					}

					if (len > 0) {
						// Allow sleep
						try {
							Thread.sleep(sleep);
						} catch (Exception e) {
							log.error(e);
						}

					} else {
						// Sleep when nothing to do
						synchronized (thread) {
							try {
								thread.wait();
							} catch (Exception e) {
								log.error(e);
							}
						}
					}

				}

			} catch (Exception e) {
				log.error(e);
			}
		}

	};

	/**
	 * Add a new runnable to a map along with a unique id<br>
	 * The last update runnable of an id will be executed only.
	 * 
	 * @param id
	 * @param runnable
	 */
	public void addRunnable(String id, Runnable runnable) {
		try {
			// Block thread, ensure no one is going to update the vector
			lock.writeLock().lock();
			try {
				runnableMap.put(id, runnable);
			} finally {
				lock.writeLock().unlock();
			}
			runInThread();
		} catch (Exception e) {
			log.error(e);
		}
	}

	// public void addRunnable(Runnable runnable) {
	// // Block thread, ensure no one is going to update the vector
	// lock.writeLock().lock();
	// runnableList.add(runnable);
	// lock.writeLock().unlock();
	// runInThread();
	// }

	private void runInThread() {
		try {
			if (thread == null) {
				thread = new Thread(runnable, "GUI Display Delay Queue "
						+ System.nanoTime());
				thread.setDaemon(true);
				thread.start();
			} else {
				synchronized (thread) {
					thread.notify();
				}
			}
		} catch (Exception e) {
			log.error(e);
		}
	}

	public void stopThread() {
		try {
			if (thread != null) {
				stop = true;
				synchronized (thread) {
					thread.notify();
				}
			}
		} catch (Exception e) {
			log.error(e);
		}
	}
}

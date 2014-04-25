/**
 * SelectionProviderAdapter.java
 *
 * Version information :
 *
 * Date:Jan 27, 2010
 *
 * Copyright notice :
 */
package net.heartsome.cat.ts.ui.editors;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;

/**
 * .
 * @author stone
 * @version
 * @since JDK1.6
 */
public class SelectionProviderAdapter implements ISelectionProvider {

	/** 监听器集合。 */
	private List<ISelectionChangedListener> listeners = new ArrayList<ISelectionChangedListener>();

	/** 被选中的对象。 */
	private ISelection theSelection = StructuredSelection.EMPTY;

	/**
	 * 添加响应选中对象改变后事件的监听器。
	 * @param listener
	 *            the listener
	 * @see org.eclipse.jface.viewers.ISelectionProvider#addSelectionChangedListener(org.eclipse.jface.viewers.ISelectionChangedListener)
	 */
	public void addSelectionChangedListener(ISelectionChangedListener listener) {
		listeners.add(listener);
	}

	/**
	 * 获得被选中对象。
	 * @return the selection
	 * @see org.eclipse.jface.viewers.ISelectionProvider#getSelection()
	 */
	public ISelection getSelection() {
		return theSelection;
	}

	/**
	 * 移出响应选中对象改变后事件的监听器。
	 * @param listener
	 *            被移出的监听器。
	 * @see org.eclipse.jface.viewers.ISelectionProvider#removeSelectionChangedListener(org.eclipse.jface.viewers.ISelectionChangedListener)
	 */
	public void removeSelectionChangedListener(ISelectionChangedListener listener) {
		listeners.remove(listener);
	}

	/**
	 * 设置被选中对象，并用安全线程通知监听器。
	 * @param selection
	 *            the selection
	 * @see org.eclipse.jface.viewers.ISelectionProvider#setSelection(org.eclipse.jface.viewers.ISelection)
	 */
	public void setSelection(ISelection selection) {
		theSelection = selection;
		final SelectionChangedEvent e = new SelectionChangedEvent(this, selection);
		Object[] listenersArray = listeners.toArray();
		for (int i = 0; i < listenersArray.length; i++) {
			final ISelectionChangedListener l = (ISelectionChangedListener) listenersArray[i];
			SafeRunner.run(new SafeRunnable() {
				public void run() {
					l.selectionChanged(e);
				}
			});
		}
	}
}

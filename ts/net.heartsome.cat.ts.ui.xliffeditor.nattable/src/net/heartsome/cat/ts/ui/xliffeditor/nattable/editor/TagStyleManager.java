package net.heartsome.cat.ts.ui.xliffeditor.nattable.editor;

import java.util.HashSet;

import net.heartsome.cat.common.innertag.TagStyle;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

/**
 * 标记样式管理器
 * @author weachy
 * @version
 * @since JDK1.5
 */
public class TagStyleManager {

	private TagStyle tagStyle;

	public TagStyleManager() {
		tagStyle = TagStyle.getDefault();
	}

	public TagStyle getTagStyle() {
		return tagStyle;
	}

	public void setTagStyle(TagStyle tagStyle) {
		this.tagStyle = tagStyle;
		TagStyle.setTagStyle(tagStyle);

		Event event = new Event();
		event.data = tagStyle;
		for (Listener listener : tagStyleChangeListeners) {
			listener.handleEvent(event);
		}
	}

	private HashSet<Listener> tagStyleChangeListeners = new HashSet<Listener>();

	/**
	 * 添加关闭单元格关闭时的监听器
	 * @param closeListener
	 *            关闭监听器 ;
	 */
	public void addTagStyleChangeListener(Listener tagStyleChangeListener) {
		if (tagStyleChangeListener == null) {
			SWT.error(SWT.ERROR_NULL_ARGUMENT);
		}
		tagStyleChangeListeners.add(tagStyleChangeListener);
	}

	/**
	 * 移除关闭单元格关闭时的监听器
	 * @param closeListener
	 *            关闭监听器 ;
	 */
	public void removeTagStyleChangeListener(Listener tagStyleChangeListener) {
		for (Listener listener : tagStyleChangeListeners) {
			if (listener != null && listener.equals(tagStyleChangeListener)) {
				tagStyleChangeListeners.remove(listener);
				break;
			}
		}
	}
}

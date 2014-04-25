package net.heartsome.cat.ts.ui.xliffeditor.nattable.handler.tags;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import net.heartsome.cat.common.innertag.InnerTagBean;
import net.heartsome.cat.common.ui.innertag.InnerTag;
import net.heartsome.cat.ts.ui.xliffeditor.nattable.editor.NatTableConstant;

/**
 * 插入下一标记
 * @author weachy
 * @version
 * @since JDK1.5
 */
public class InsertNextTagHandler extends AbstractInsertTagHandler {

	@Override
	protected int getTagNum() {
		if (cellEditor.getCellType() == NatTableConstant.SOURCE) { // 此操作是修改源语言，则退出。
			return -1;
		}
		return getNextTagIndex(); // 得到标记号
	}

	private int getNextTagIndex() {
		List<InnerTag> currentInnerTag = cellEditor.getSegmentViewer().getCurrentInnerTags();
		// 按照标记索引从小到大将当前显示的内部标记排序。
		Collections.sort(currentInnerTag, new Comparator<InnerTag>() {
			public int compare(InnerTag o1, InnerTag o2) {
				InnerTagBean bean1 = o1.getInnerTagBean();
				InnerTagBean bean2 = o2.getInnerTagBean();
				if (bean1.getIndex() != bean2.getIndex()) {
					return bean1.getIndex() - bean2.getIndex();
				} else {
					return bean1.getType().compareTo(bean2.getType());
				}
			}
		});

		int index = 1;
		for (InnerTag innerTag : currentInnerTag) {
			if (innerTag.getInnerTagBean().getIndex() != index) {
				break;
			} else {
				switch (innerTag.getInnerTagBean().getType()) {
				case END:
				case STANDALONE:
					index++;
					break;
				default:
					break;
				}
			}
		}
		// 如果超过了源文本内部标记最大索引，则重置为 -1。
		return index <= sourceMaxTagIndex ? index : -1;
	}
}

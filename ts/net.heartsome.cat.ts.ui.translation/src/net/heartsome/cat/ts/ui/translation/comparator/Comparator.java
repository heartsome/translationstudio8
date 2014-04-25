package net.heartsome.cat.ts.ui.translation.comparator;

import java.util.ArrayList;
import java.util.List;

import net.heartsome.cat.common.bean.ColorConfigBean;
import net.heartsome.cat.common.ui.utils.InnerTagUtil;

import org.eclipse.compare.rangedifferencer.RangeDifference;
import org.eclipse.compare.rangedifferencer.RangeDifferencer;
import org.eclipse.jface.text.Position;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Color;

public class Comparator {	

	public static List<Position> Compare(String referenceText, String targetText) {
		List<Position> differencePosition = new ArrayList<Position>();

		Position[] tagRanges = InnerTagUtil.getStyledTagRanges(targetText);

		TokenComparator left = new TokenComparator(InnerTagUtil.getDisplayValueWithoutTags(targetText));
		TokenComparator right = new TokenComparator(referenceText);

		RangeDifference[] e = RangeDifferencer.findRanges(left, right);
		for (int i = 0; i < e.length; i++) {
			RangeDifference es = e[i];

			int leftStart = es.leftStart();
			int leftEnd = es.leftEnd();

			int lStart = left.getTokenStart(leftStart);
			int lEnd = left.getTokenStart(leftEnd);

			if (es.kind() == RangeDifference.CHANGE) {
				int start = lStart;
				int end = lEnd;
				if (tagRanges.length > 0) {
					for (Position tagRange : tagRanges) {
						int tagStart = tagRange.getOffset();
						int tagEnd = tagRange.getOffset() + tagRange.getLength();
						if (tagStart <= start) {
							start += tagRange.getLength();
							end += tagRange.getLength();
						} else if (start < tagStart && tagStart < end) {
							if (start >= 0 && end - 1 >= 0) {
								Position position = new Position(start, tagStart - 1);
								differencePosition.add(position);
							}
							start = tagEnd;
							end += tagRange.getLength();
						} else {
							break;
						}
					}
				}
				if (start >= 0 && end - 1 >= 0) {
					Position position = new Position(start, end - 1);
					differencePosition.add(position);
				}
			}
		}
		return differencePosition;
	}

	/**
	 * 用于比较的参照文本（不含内部标记）
	 * @param referenceText
	 * @param text
	 *            ;
	 */
	public static void Compare(String referenceText, StyledText text) {
		ColorConfigBean colorCfgBean = ColorConfigBean.getInstance();
		final Color differencefg = colorCfgBean.getSrcDiffFgColor();
		final Color differencebg = colorCfgBean.getSrcDiffBgColor();

		Position[] tagRanges = InnerTagUtil.getStyledTagRanges(text.getText());

		TokenComparator left = new TokenComparator(InnerTagUtil.getDisplayValueWithoutTags(text.getText()));
		TokenComparator right = new TokenComparator(referenceText);
		ArrayList<StyleRange> styleRanges = new ArrayList<StyleRange>();

		RangeDifference[] e = RangeDifferencer.findRanges(left, right);
		for (int i = 0; i < e.length; i++) {
			RangeDifference es = e[i];

			int leftStart = es.leftStart();
			int leftEnd = es.leftEnd();

			int lStart = left.getTokenStart(leftStart);
			int lEnd = left.getTokenStart(leftEnd);

			if (es.kind() == RangeDifference.CHANGE) {
				int start = lStart;
				int end = lEnd;
				if (tagRanges.length > 0) {
					for (Position tagRange : tagRanges) {
						int tagStart = tagRange.getOffset();
						int tagEnd = tagRange.getOffset() + tagRange.getLength();
						if (tagStart <= start) {
							start += tagRange.getLength();
							end += tagRange.getLength();
						} else if (start < tagStart && tagStart < end) {
							StyleRange range = new StyleRange(start, tagStart - start, differencefg, differencebg);
							styleRanges.add(range);

							start = tagEnd;
							end += tagRange.getLength();
						} else {
							break;
						}
					}
				}
				StyleRange range = new StyleRange(start, end - start, differencefg, differencebg);
				styleRanges.add(range);
			}
		}
		for (int i = 0; i < styleRanges.size(); i++) {
			text.setStyleRange(styleRanges.get(i));
			text.addDisposeListener(new DisposeListener() {
				public void widgetDisposed(DisposeEvent e) {
					differencefg.dispose();
					differencebg.dispose();
				}
			});
		}
	}
}

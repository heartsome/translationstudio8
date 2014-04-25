package net.heartsome.cat.ts.ui.innertag;

import java.util.List;

import net.heartsome.cat.common.innertag.InnerTagBean;
import net.heartsome.cat.common.innertag.TagStyle;
import net.heartsome.cat.common.innertag.factory.PlaceHolderEditModeBuilder;
import net.heartsome.cat.common.ui.innertag.InnerTag;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

/**
 * 内部标记工具类。占位符使用 Unicode 码，范围为 {@link #MIN} 到 {@link #MAX}。
 * @author weachy
 * @version
 * @since JDK1.5
 */
public class InnerTagUtil {

	/**
	 * 占位符创建者
	 */
	private static final PlaceHolderEditModeBuilder PLACE_HOLDER_BUILDER = new PlaceHolderEditModeBuilder();

	/**
	 * 根据占位符得到内部标记控件
	 * @param innerTags
	 *            内部标记控件集合
	 * @param placeHolder
	 *            占位符
	 * @return 内部标记控件;
	 */
	public static InnerTag getInnerTag(List<InnerTag> innerTags, String placeHolder) {
		int index = PLACE_HOLDER_BUILDER.getIndex(null, placeHolder);
		if (index > -1 && index < innerTags.size()) {
			return innerTags.get(index);
		}
		return null;
	}

	/**
	 * 根据内部标记实体得到占位符
	 * @param innerTags
	 *            内部标记控件集合
	 * @param innerTagBean
	 *            内部标记实体
	 * @return 占位符;
	 */
	public static String getPlaceHolder(List<InnerTag> innerTags, InnerTagBean innerTagBean) {
		if (innerTagBean == null || innerTags == null || innerTags.size() == 0) {
			return null;
		}
		for (int i = 0; i < innerTags.size(); i++) {
			InnerTagBean bean = innerTags.get(i).getInnerTagBean();
			if (innerTagBean.equals(bean)) {
				return PLACE_HOLDER_BUILDER.getPlaceHolder(null, i);
			}
		}
		return null;
	}

	/** Display 对象 */
	// private static Display display = Display.getDefault();
	// /** 错误标记边框颜色 */
	// private static Color wrongTagBorderColor = ColorConfigBean.getInstance().getWrongTagColor();
	// /** 边框颜色 */
	// private static Color borderColor = new Color(display, 0, 255, 255);
	// /** 文本前景色 */
	// private static Color textFgColor = new Color(display, 0, 104, 139);
	// /** 文本背景色 */
	// private static Color textBgColor = new Color(display, 0, 205, 205);
	// /** 索引前景色 */
	// private static Color inxFgColor = borderColor;
	// /** 索引背景色 */
	// private static Color inxBgColor = new Color(display, 0, 139, 139);

	/**
	 * 创建内部标记控件
	 * @param parent
	 *            父容器
	 * @param innerTagBean
	 *            内部标记实体
	 * @return 内部标记控件;
	 */
	public static InnerTag createInnerTagControl(Composite parent, InnerTagBean innerTagBean, TagStyle tagStyle) {
		final InnerTag innerTag = new InnerTag(parent, SWT.NONE, innerTagBean, tagStyle);
		//--- 此部分配置已经移动到 InnertagRender 中
		// /** 错误标记边框颜色 */
		// ColorConfigBean cbean = ColorConfigBean.getInstance();
		// Color wrongTagBorderColor = cbean.getWrongTagColor();
		// Color tmpBorderColor = innerTagBean.isWrongTag() ? wrongTagBorderColor : cbean.getTagBgColor();
		// Color bgColor = innerTagBean.isWrongTag() ? wrongTagBorderColor : cbean.getTagBgColor();
		// innerTag.initColor(cbean.getTagBgColor(), cbean.getTagFgColor(), cbean.getTagFgColor(), bgColor,
		// tmpBorderColor);
		// innerTag.setFont(net.heartsome.cat.common.ui.utils.InnerTagUtil.tagFont);
		innerTag.pack();

		return innerTag;
	}
}

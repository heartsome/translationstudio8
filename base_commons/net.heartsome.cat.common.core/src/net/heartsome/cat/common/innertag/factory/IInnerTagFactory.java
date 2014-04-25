package net.heartsome.cat.common.innertag.factory;

import java.util.List;

import net.heartsome.cat.common.innertag.InnerTagBean;

/**
 * 内部标记工厂接口
 * @author weachy
 * @version
 * @since JDK1.5
 */
public interface IInnerTagFactory {

	/**
	 * 解析内部标记
	 * @param xml
	 *            ;
	 */
	String parseInnerTag(String xml);

	/**
	 * 得到提取内部标记之后的剩余文本。
	 * @return 提取内部标记之后的剩余文本;
	 */
	String getText();

	/**
	 * 得到内部标记实体集合 <br/>
	 * <br/>
	 * 用法：使用 {@link #getText()} 得到提取标记后的剩余文本，包含占位符。<br/>
	 * 占位符是由 {@link IPlaceHolderBuilder} 接口的实现类创建。
	 * @return 内部标记实体集合。
	 */
	List<InnerTagBean> getInnerTagBeans();

}

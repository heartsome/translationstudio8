package net.heartsome.cat.common.innertag;

import org.apache.commons.lang.builder.EqualsBuilder;

/**
 * 内部标记实体类
 * @author weachy
 * @version
 * @since JDK1.5
 */
public class InnerTagBean {

	/**
	 * 内部标记实体类
	 */
	public InnerTagBean() {
	}

	/**
	 * 内部标记实体类
	 * @param index
	 *            标记索引
	 * @param name
	 *            标记名
	 * @param content
	 *            标记内容
	 * @param type
	 *            标记类型
	 */
	public InnerTagBean(int index, String name, String content, TagType type) {
		this(index, name, content, type, false);
	}

	/**
	 * 内部标记实体类
	 * @param index
	 *            标记索引
	 * @param name
	 *            标记名
	 * @param content
	 *            标记内容
	 * @param type
	 *            标记类型
	 * @param wrongTag
	 *            错误标记标识
	 */
	public InnerTagBean(int index, String name, String content, TagType type, boolean wrongTag) {
		this.index = index;
		this.name = name;
		this.content = content;
		this.type = type;
		this.wrongTag = wrongTag;
	}

	/** 标记索引 */
	private int index;

	/** 标记名 */
	private String name;

	/** 标记内容 */
	private String content;

	/** 标记类型 */
	private TagType type;

	/** 错误标记 */
	private boolean wrongTag;

	/**
	 * 获取标记索引
	 * @return 标记索引;
	 */
	public int getIndex() {
		return index;
	}

	/**
	 * 设置标记索引
	 * @param index
	 *            标记索引;
	 */
	public void setIndex(int index) {
		this.index = index;
	}

	/**
	 * 获取标记名
	 * @return 标记名;
	 */
	public String getName() {
		return name;
	}

	/**
	 * 设置标记名
	 * @param name
	 *            标记名;
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * 获取标记内容
	 * @return 标记内容;
	 */
	public String getContent() {
		return content;
	}

	/**
	 * 设置标记内容
	 * @param content
	 *            标记内容;
	 */
	public void setContent(String content) {
		this.content = content;
	}

	/**
	 * 获取标记类型
	 * @return 标记类型
	 */
	public TagType getType() {
		return type;
	}

	/**
	 * 设置标记类型
	 * @param type
	 *            标记类型
	 */
	public void setType(TagType type) {
		this.type = type;
	}

	/**
	 * 设置为错误标记
	 * @param wrongTag
	 *            错误标记标识 ;
	 */
	public void setWrongTag(boolean wrongTag) {
		this.wrongTag = wrongTag;
	}

	/**
	 * 是否为错误标记
	 */
	public boolean isWrongTag() {
		return wrongTag;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null || !(obj instanceof InnerTagBean)) {
			return false;
		}
		InnerTagBean that = (InnerTagBean) obj;
		return new EqualsBuilder().append(that.content, this.content).append(that.index, this.index).append(that.type,
				this.type).isEquals();
	}
}

package net.heartsome.cat.common.innertag;

public enum TagType {

	/**
	 * 独立标记类型。即可以随意放置的标记。
	 */
	STANDALONE,

	/**
	 * 开始标记类型。即必须放在成对的结束标记前的标记。
	 */
	START,

	/**
	 * 结束标记类型。即必须放在成对的开始标记前的标记。
	 */
	END
}

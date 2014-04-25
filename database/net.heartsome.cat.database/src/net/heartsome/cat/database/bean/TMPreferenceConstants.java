package net.heartsome.cat.database.bean;

import net.heartsome.cat.database.Constants;

/**
 * 记忆库，术语库的常量类
 * @author  peason
 * @version 
 * @since   JDK1.6
 */
public final class TMPreferenceConstants {

	/**
	 * 默认构造函数
	 */
	protected TMPreferenceConstants() {
		throw new UnsupportedOperationException();
	}
	
	/**
	 * 匹配率相同时排序策略
	 */
	public static final String MATCH_PERCENTAGE_SORT_WITH_EQUAL = "net.heartsome.cat.ts.ui.preferencepage.database.2";
	
	/**
	 * 记忆库更新策略
	 */
	public static final String TM_UPDATE = "net.heartsome.cat.ts.ui.preferencepage.database.3";
	
	/**
	 * 查库时是否忽略大小写
	 */
	public static final String CASE_SENSITIVE = "net.heartsome.cat.ts.ui.preferencepage.database.8";

	
	/**
	 * 匹配时是否忽略标记
	 */
	public static final String IGNORE_MARK = "net.heartsome.cat.ts.ui.preferencepage.database.10";

	/**
	 * 上下文匹配
	 */
	public static final String CONTEXT_MATCH = "net.heartsome.cat.ts.ui.preferencepage.database.11";
	
	/**
	 * 最大匹配数
	 */
	public static final String MAX_MATCH_NUMBER = "net.heartsome.cat.ts.ui.preferencepage.database.12";

	/**
	 * 最小匹配率
	 */
	public static final String MIN_MATCH = "net.heartsome.cat.ts.ui.preferencepage.database.13";
	
	public static final String TAG_PENALTY = "net.heartsome.cat.ts.ui.preferencepage.database.TAG_PENALTY";
	
	/**
	 * 匹配率相同时默认库优先
	 */
	public static final int DEFAULT_DB_PRECEDENCE = 0;
	
	/**
	 * 匹配率相同时更新时间倒序排列
	 */
	public static final int DATE_REVERSE_PRECEDENCE = 1;
	
	/**
	 * 记忆库更新策略 > 始终增加
	 */
	public static final int TM_ALWAYS_ADD = Constants.IMPORT_MODEL_ALWAYSADD;
	
	/**
	 * 记忆库更新策略 > 重复覆盖
	 */
	public static final int TM_REPEAT_OVERWRITE = Constants.IMPORT_MODEL_OVERWRITE;
	
	/**
	 * 记忆库更新策略 > 重复忽略
	 */
	public static final int TM_REPEAT_IGNORE = Constants.IMPORT_MODEL_IGNORE;	
	
	
	// 记库库新建向导记住上一次操作信息
	public static final String TM_RM_DBTYPE = "net.heartsome.cat.database.ui.tm.rm.dbtype";
	public static final String TM_RM_INSTANCE= "net.heartsome.cat.database.ui.tm.rm.instance";
	public static final String TM_RM_SERVER= "net.heartsome.cat.database.ui.tm.rm.server";
	public static final String TM_RM_PORT= "net.heartsome.cat.database.ui.tm.rm.prot";
	public static final String TM_RM_PATH ="net.heartsome.cat.database.ui.tm.rm.path";
	public static final String TM_RM_USERNAME="net.heartsome.cat.database.ui.tm.rm.username";
}

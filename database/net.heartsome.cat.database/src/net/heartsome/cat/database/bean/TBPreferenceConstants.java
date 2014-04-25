/**
 * TBPreferenceConstants.java
 *
 * Version information :
 *
 * Date:2012-5-2
 *
 * Copyright notice :
 * 本文件及其附带的相关文件包含机密信息，仅限瀚特盛科技有限公司指定的，与本项目有关的内部人员和客户联络人员查阅和使用。 
 * 如果您不是本保密声明中指定的收件者，请立即销毁本文件，禁止对本文件或根据本文件中的内容采取任何其他行动， 
 * 包括但不限于：泄露本文件中的信息、以任何方式制作本文件全部或部分内容之副本、将本文件或其相关副本提供给任何其他人。
 */
package net.heartsome.cat.database.bean;

import net.heartsome.cat.database.Constants;

/**
 * @author jason
 * @version
 * @since JDK1.6
 */
public final class TBPreferenceConstants {
	/** 术语库更新策略 */
	public static final String TB_UPDATE = "net.heartsome.cat.ts.ui.preferencepage.database.4";

	/** 术语库更新设置 > 始终增加 */
	public static final int TB_ALWAYS_ADD = Constants.IMPORT_MODEL_ALWAYSADD;

	/** 术语库更新设置 > 重复覆盖 */	
	public static final int TB_REPEAT_OVERWRITE = Constants.IMPORT_MODEL_OVERWRITE;

	/** 术语库更新设置 > 重复合并 */
	public static final int TB_REPEAT_MERGE = Constants.IMPORT_MODEL_MERGE;

	/** 术语库更新设置 > 重复忽略 */
	public static final int TB_REPEAT_IGNORE = Constants.IMPORT_MODEL_IGNORE;
	
	/** 术语库匹配是否忽略大小写 */
	public static final String TB_CASE_SENSITIVE = "net.heartsome.cat.database.ui.tb.rm.casesensitive";
	
	
	// 记库库新建向导记住上一次操作信息
	public static final String TB_RM_DBTYPE = "net.heartsome.cat.database.ui.tb.rm.dbtype";
	public static final String TB_RM_INSTANCE= "net.heartsome.cat.database.ui.tb.rm.instance";
	public static final String TB_RM_SERVER= "net.heartsome.cat.database.ui.tb.rm.server";
	public static final String TB_RM_PORT= "net.heartsome.cat.database.ui.tb.rm.prot";
	public static final String TB_RM_PATH ="net.heartsome.cat.database.ui.tb.rm.path";
	public static final String TB_RM_USERNAME="net.heartsome.cat.database.ui.tb.rm.username";
}

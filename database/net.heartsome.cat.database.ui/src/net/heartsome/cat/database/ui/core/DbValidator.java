/**
 * DbValidator.java
 *
 * Version information :
 *
 * Date:2012-8-14
 *
 * Copyright notice :
 * 本文件及其附带的相关文件包含机密信息，仅限瀚特盛科技有限公司指定的，与本项目有关的内部人员和客户联络人员查阅和使用。 
 * 如果您不是本保密声明中指定的收件者，请立即销毁本文件，禁止对本文件或根据本文件中的内容采取任何其他行动， 
 * 包括但不限于：泄露本文件中的信息、以任何方式制作本文件全部或部分内容之副本、将本文件或其相关副本提供给任何其他人。
 */
package net.heartsome.cat.database.ui.core;

import net.heartsome.cat.database.ui.resource.Messages;



/**
 * @author  Jason
 * @version 
 * @since   JDK1.6
 */
public class DbValidator {

	public static String valiateDbName(String dbName){
		if(dbName == null){
			return Messages.getString("dialog.inputdbname.msg2");
		}
		
		if(!dbName.matches("^([a-zA-Z])([a-zA-Z0-9_]){3,14}$")){
			return Messages.getString("dialog.inputdbname.msg1");
		}
		return null;		
	}
	
	public static void main(String[] args) {
		String t = "aaa";
		System.out.println(valiateDbName(t));
	}
}

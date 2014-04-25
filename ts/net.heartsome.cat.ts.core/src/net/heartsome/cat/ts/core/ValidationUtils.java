/**
 * ValidatorUtils.java
 *
 * Version information :
 *
 * Date:2012-5-18
 *
 * Copyright notice :
 * 本文件及其附带的相关文件包含机密信息，仅限瀚特盛科技有限公司指定的，与本项目有关的内部人员和客户联络人员查阅和使用。 
 * 如果您不是本保密声明中指定的收件者，请立即销毁本文件，禁止对本文件或根据本文件中的内容采取任何其他行动， 
 * 包括但不限于：泄露本文件中的信息、以任何方式制作本文件全部或部分内容之副本、将本文件或其相关副本提供给任何其他人。
 */
package net.heartsome.cat.ts.core;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.heartsome.cat.ts.core.resource.Messages;

/**
 * @author jason
 * @version
 * @since JDK1.6
 */
public class ValidationUtils {

	public static String validateProjectName(String projectName) {
		if (projectName.length() == 0) {
			return Messages.getString("core.ValidationUtils.msg1");
		}

		if (projectName.length() > 20) {
			return Messages.getString("core.ValidationUtils.msg2");
		}
		return null;
	}

	public static String validateEmail(String email) {
		final Pattern pattern = Pattern.compile("\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*");
		final Matcher mat = pattern.matcher(email);
		if (!mat.find()) {
			return Messages.getString("core.ValidationUtils.msg6");
		}
		return null;
	}

	public static boolean isNumeric(String str) {
		Pattern pattern = Pattern.compile("[0-9]*");
		Matcher isNum = pattern.matcher(str);
		if (!isNum.matches()) {
			return false;
		}
		return true;
	}
}

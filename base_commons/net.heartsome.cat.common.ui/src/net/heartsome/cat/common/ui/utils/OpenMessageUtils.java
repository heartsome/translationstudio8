/**
 * OpenMessageUtils.java
 *
 * Version information :
 *
 * Date:2013-5-28
 *
 * Copyright notice :
 * 本文件及其附带的相关文件包含机密信息，仅限瀚特盛科技有限公司指定的，与本项目有关的内部人员和客户联络人员查阅和使用。 
 * 如果您不是本保密声明中指定的收件者，请立即销毁本文件，禁止对本文件或根据本文件中的内容采取任何其他行动， 
 * 包括但不限于：泄露本文件中的信息、以任何方式制作本文件全部或部分内容之副本、将本文件或其相关副本提供给任何其他人。
 */
package net.heartsome.cat.common.ui.utils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import net.heartsome.cat.common.ui.Activator;
import net.heartsome.cat.common.ui.resource.Messages;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 * 消息处理工具类，弹出消息提示给用户。
 * @author Jason
 * @version 1.0
 * @since JDK1.6
 */
public final class OpenMessageUtils {

	/**
	 * 弹出错误、警告或消息对话框
	 * @param severity
	 *            必须是 <code>IStatus.ERROR<code>,<code>IStatus.WARNING<code>,<code>IStatus.INFO<code>三者之一 
	 * @param message
	 *            错误、警告或消息的文本信息，不能为null或空串;
	 */
	public static void openMessage(int severity, String message) {
		IStatus status = getIStatus(severity, message);
		String title = getMessageDlgTitle(severity);
		if (title == null) {
			return;
		}
		ErrorDialog.openError(Display.getCurrent().getActiveShell(), title, null, status);
	}

	/**
	 * 弹出　确认对话框	robert	2013-06-18
	 * @param message
	 * @return
	 */
	public static boolean openConfirmMessage(String message){
		return MessageDialog.openConfirm(Display.getDefault().getActiveShell(), 
				Messages.getString("utils.OpenMesssageUtils.messageDialog.confirmTitle"), message);
	}
	
	/**
	 * 弹出错误或警告对话框，需要提供错误或警告的文本原因信息
	 * @param severity
	 *            必须是<code>IStatus.ERROR<code>,<code>IStatus.WARNING<code>
	 * @param message
	 *            错误、警告的文本信息，不能为null或空串;
	 * @param reasonMsg
	 *            错误或警告的文本原因信息，不能为null或空串;
	 */
	public static void openMessageWithReason(int severity, String message, String reasonMsg) {
		Assert.isLegal(severity == IStatus.ERROR || severity == IStatus.WARNING);
		Assert.isLegal(message != null && message.length() > 0);
		IStatus status = getIStatus(severity, reasonMsg);
		String title = getMessageDlgTitle(severity);
		if (title == null) {
			return;
		}
		ErrorDialog.openError(new Shell(Display.getDefault()), title, message, status);
	}

	/**
	 * 弹出错误或警告对话框，需要提供错误或警告的文本原因信息以及详细的异常<code>Throwable<code>
	 * @param message
	 *            错误或警告的文本信息，不能为null或空串
	 * @param reasonMsg
	 *            错误、警告的文本原因信息，不能为null或空串
	 * @param throwable
	 *            <code>Throwable<code> 对象，错误或警告的详细异常信息;
	 */
	public static void openErrorMsgWithDetail(String message, String reasonMsg, Throwable throwable) {
		Assert.isLegal(message != null && message.length() > 0);
		Assert.isLegal(reasonMsg != null && reasonMsg.length() > 0);
		Assert.isLegal(throwable != null);
		IStatus status = throwable2MultiStatus(reasonMsg, throwable);
		String title = getMessageDlgTitle(IStatus.ERROR);
		if (title == null) {
			return;
		}
		ErrorDialog.openError(new Shell(Display.getDefault()), title, message, status);
	}

	private static String getMessageDlgTitle(int severity) {
		String title = null;
		switch (severity) {
		case IStatus.ERROR:
			title = Messages.getString("utils.OpenMesssageUtils.messageDialog.ErrorTitle");
			break;
		case IStatus.WARNING:
			title = Messages.getString("utils.OpenMesssageUtils.messageDialog.Warningtitle");
			break;
		case IStatus.INFO:
			title = Messages.getString("utils.OpenMesssageUtils.messageDialog.Infotitle");
			break;
		default:
			break;
		}
		return title;
	}

	private static IStatus getIStatus(int severity, String message) {
		Assert.isLegal(severity == IStatus.ERROR || severity == IStatus.WARNING || severity == IStatus.INFO);
		Assert.isLegal(message != null && message.length() > 0);
		IStatus status = new Status(severity, Activator.PLUGIN_ID, message);
		return status;
	}

	/**
	 * 将<code>Throwable<code>转化成<code>MultiStatus<code>对象，
	 * 让<code>MultiStatus<code>对象包含详细的<code>Throwable<code>详细的堆栈信息。
	 * @param message
	 *            <code>MultiStatus<code>对象的消息
	 * @param throwable
	 *            异常对象
	 * @return 包含有详细的堆栈信息的<code>MultiStatus<code>对象;
	 */
	public static MultiStatus throwable2MultiStatus(String message, Throwable throwable) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		throwable.printStackTrace(pw);

		// stack trace as a string
		final String trace = sw.toString();

		// Temp holder of child statuses
		List<Status> childStatuses = new ArrayList<Status>();

		// Split output by OS-independend new-line
		String[] lines = trace.split(System.getProperty("line.separator")); //$NON-NLS-N$
		int j = lines.length == 1 ? 0 : 1;
		for (int i = j; i < lines.length; i++) {
			String line = lines[i];
			// build & add status
			childStatuses.add(new Status(IStatus.ERROR, Activator.PLUGIN_ID, line));
		}

		// convert to array of statuses
		MultiStatus ms = new MultiStatus(Activator.PLUGIN_ID, IStatus.ERROR, childStatuses.toArray(new Status[] {}),
				message, throwable);
		return ms;
	}

	/**
	 * Private constructor. Prevent instance
	 */
	private OpenMessageUtils() {

	}
}

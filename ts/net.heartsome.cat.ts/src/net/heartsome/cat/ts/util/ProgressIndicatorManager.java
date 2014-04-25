package net.heartsome.cat.ts.util;

import net.heartsome.cat.ts.Activator;
import net.heartsome.cat.ts.ApplicationWorkbenchAdvisor;
import net.heartsome.cat.ts.ApplicationWorkbenchWindowAdvisor;
import net.heartsome.cat.ts.TsPreferencesConstant;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Display;


/**
 * 状态栏最右侧的后台进度条管理（即控制其显示或隐藏），主要是针对 BUG 2652
 * @author robert	2012-11-07
 */
public class ProgressIndicatorManager {
	/** 能够隐藏后台进度条的标识，如果大于等于1，是不能隐藏的，标志还有其他程序在使用 */
	private static int statusTag = 0;
	
	/**
	 * 当进度条要显示出来时，显示状态栏右下方的后台进度条。
	 */
	public static void displayProgressIndicator(){
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				statusTag ++;

				// 首先检查状态栏是否处于隐藏状态，如果是隐藏的，直接退出
				IPreferenceStore preferenceStore = Activator.getDefault().getPreferenceStore();
				boolean isVisible = preferenceStore.getBoolean(TsPreferencesConstant.TS_statusBar_status);
				
				if (!isVisible) {
					return;
				}
				
				ApplicationWorkbenchWindowAdvisor configurer = ApplicationWorkbenchAdvisor.WorkbenchWindowAdvisor;
		        configurer.setProgressIndicatorVisible(true);
			}
		});
		
	}
	
	/**
	 * 进度条关闭后，隐藏状态栏右下方的后台进度条
	 */
	public static void hideProgressIndicator(){
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				statusTag --;
				
				// 如果 状态标志 大于0，则不允隐藏后台进度条
				if (statusTag > 0) {
					return;
				}
				
				// 首先检查状态栏是否处于隐藏状态，如果是隐藏的，直接退出
				IPreferenceStore preferenceStore = Activator.getDefault().getPreferenceStore();
				boolean isVisible = preferenceStore.getBoolean(TsPreferencesConstant.TS_statusBar_status);
				
				if (!isVisible) {
					return;
				}
				
				ApplicationWorkbenchWindowAdvisor configurer = ApplicationWorkbenchAdvisor.WorkbenchWindowAdvisor;
		        configurer.setProgressIndicatorVisible(false);
			}
		});
	}
	

	
}

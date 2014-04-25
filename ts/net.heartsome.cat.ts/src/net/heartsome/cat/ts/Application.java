package net.heartsome.cat.ts;

import java.io.File;
import java.io.FileOutputStream;

import net.heartsome.cat.common.util.CommonFunction;
import net.heartsome.cat.ts.ui.Activator;
import net.heartsome.cat.ts.ui.preferencepage.IPreferenceConstants;
import net.heartsome.cat.ts.ui.util.PreferenceUtil;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;

import com.ximpleware.AutoPilot;
import com.ximpleware.VTDGen;
import com.ximpleware.VTDNav;
import com.ximpleware.XMLModifier;

/**
 * This class controls all aspects of the application's execution
 */
public class Application implements IApplication {

	public Object start(IApplicationContext context) {
		Display display = PlatformUI.createDisplay();
		try {
			PreferenceUtil.initProductEdition();

			deleteErrorMemoryInfo();

			initSystemLan();
			PreferenceUtil.checkCleanValue();
			int returnCode = PlatformUI.createAndRunWorkbench(display,
					new ApplicationWorkbenchAdvisor());
			if (returnCode == PlatformUI.RETURN_RESTART) {
				return IApplication.EXIT_RESTART;
			}
			return IApplication.EXIT_OK;
		} finally {
			display.dispose();
		}
	}
	
	private void initSystemLan(){
		int lanId = Activator.getDefault().getPreferenceStore().getInt(IPreferenceConstants.SYSTEM_LANGUAGE);
		CommonFunction.setSystemLanguage(lanId == 0 ? "en" : "zh");
	}

	public void stop() {
		final IWorkbench workbench = PlatformUI.getWorkbench();
		if (workbench == null) {
			return;
		}
		final Display display = workbench.getDisplay();
		display.syncExec(new Runnable() {
			public void run() {
				if (!display.isDisposed()) {
					workbench.close();
				}
			}
		});
	}
	
	/**
	 * 删除错误记录文件，以修改产品第一次运行后，存储错误信息导致第二次打不开的情况	robert	2013-05-15
	 */
	private static void deleteErrorMemoryInfo(){
		// 只针对 mac 下的用户
		if (System.getProperty("os.name").indexOf("Mac") == -1) {
			return;
		}
		
		String workbenchXmlPath = ResourcesPlugin.getWorkspace().getRoot().getLocation().append(".metadata/.plugins/org.eclipse.ui.workbench/workbench.xml").toOSString();
		VTDGen vg = new VTDGen();
		boolean result = vg.parseFile(workbenchXmlPath, true);
		if (!result) {
			new File(workbenchXmlPath).delete();
			return;
		}
		VTDNav vn = vg.getNav();
		AutoPilot ap = new AutoPilot(vn);
		FileOutputStream stream = null;
		try {
			XMLModifier xm = new XMLModifier(vn);
			// 删除节点 editor
			ap.selectXPath("/workbench/window/page/editors/editor");
			while (ap.evalXPath() != -1) {
				xm.remove();
			}
			// 删除其他记录
			ap.selectXPath("/workbench/window/page/navigationHistory");
			if (ap.evalXPath() != -1) {
				xm.remove();
			}

			xm.output(workbenchXmlPath);

		} catch (Exception e) {
			// do nothing
		}finally{
			if (stream != null) {
				try {
					stream.close();
				} catch (Exception e2) {
				}
			}
		}
	}
	
}

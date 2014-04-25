package net.heartsome.cat.converter.rap.ui;

import net.heartsome.cat.convert.ui.ApplicationWorkbenchAdvisor;

import org.eclipse.rwt.RWT;
import org.eclipse.rwt.lifecycle.IEntryPoint;
import org.eclipse.rwt.lifecycle.UICallBack;
import org.eclipse.rwt.service.SessionStoreEvent;
import org.eclipse.rwt.service.SessionStoreListener;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.application.WorkbenchAdvisor;

/**
 * This class controls all aspects of the application's execution
 * and is contributed through the plugin.xml.
 */
public class Application implements IEntryPoint {

  public int createUI() {
    final Display display = PlatformUI.createDisplay();
    
    UICallBack.activate(String.valueOf(display.hashCode()));
    RWT.getSessionStore().addSessionStoreListener(new SessionStoreListener() {
		
		public void beforeDestroy(SessionStoreEvent event) {
			UICallBack.deactivate(String.valueOf(display.hashCode()));
		}
	});
    
    WorkbenchAdvisor advisor = new ApplicationWorkbenchAdvisor();
    return PlatformUI.createAndRunWorkbench( display, advisor );
  }
}

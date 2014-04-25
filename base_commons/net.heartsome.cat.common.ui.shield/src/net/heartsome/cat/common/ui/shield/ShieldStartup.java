package net.heartsome.cat.common.ui.shield;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.jface.bindings.Binding;
import org.eclipse.jface.bindings.BindingManager;
import org.eclipse.jface.bindings.Scheme;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.keys.BindingService;
import org.eclipse.ui.keys.IBindingService;

/**
 * 在工作台初始化后，移除平台默认的 scheme
 * @author cheney
 * @since JDK1.6
 */
public class ShieldStartup implements IStartup {

	private final static String platformDefaultScheme = "org.eclipse.ui.defaultAcceleratorConfiguration";
	private final static String platformEmacsScheme = "org.eclipse.ui.emacsAcceleratorConfiguration";

	public void earlyStartup() {
		IWorkbench workbench = PlatformUI.getWorkbench();
		// 在工作台初始化后，移除平台默认的 scheme
		IBindingService bindingService = (IBindingService) workbench.getService(IBindingService.class);

		Scheme[] schemes = bindingService.getDefinedSchemes();
		for (int i = 0; i < schemes.length; i++) {
			String id = schemes[i].getId();
			if (id.equals(platformDefaultScheme) || id.equals(platformEmacsScheme)) {
				schemes[i].undefine();
			}
		}
	}
   
	
}

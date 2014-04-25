package net.heartsome.cat.ts.ui.help;

import java.lang.reflect.Method;

import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;

public class SelfDisplayUtils {
	private static final String HELP_UI_PLUGIN_ID = "org.eclipse.help.ui"; //$NON-NLS-1$
	private static final String LOOP_CLASS_NAME = "org.eclipse.help.ui.internal.HelpUIEventLoop"; //$NON-NLS-1$

	static void runUI() {
		invoke("run"); //$NON-NLS-1$
	}
	static void wakeupUI() {
		invoke("wakeup"); //$NON-NLS-1$
	}

	static void waitForDisplay() {
		invoke("waitFor"); //$NON-NLS-1$
	}

	private static void invoke(String method) {
		try {
			Bundle bundle = Platform.getBundle(HELP_UI_PLUGIN_ID);
			if (bundle == null) {
				return;
			}
			Class c = bundle.loadClass(LOOP_CLASS_NAME);
			Method m = c.getMethod(method, new Class[]{});
			m.invoke(null, new Object[]{});
		} catch (Exception e) {
		}
	}
}

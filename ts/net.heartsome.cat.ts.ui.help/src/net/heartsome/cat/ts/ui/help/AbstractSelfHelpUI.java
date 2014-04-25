package net.heartsome.cat.ts.ui.help;

import java.net.URL;

import org.eclipse.core.runtime.Platform;
import org.eclipse.help.IContext;
import org.eclipse.help.browser.IBrowser;
import org.eclipse.help.internal.base.BaseHelpSystem;
import org.eclipse.help.ui.internal.util.ErrorUtil;
import org.eclipse.osgi.service.environment.Constants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWebBrowser;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;
import org.eclipse.ui.help.AbstractHelpUI;


public class AbstractSelfHelpUI extends AbstractHelpUI {
	private static AbstractSelfHelpUI instance;
	
	class ExternalWorkbenchBrowser implements IBrowser {

		public ExternalWorkbenchBrowser() {
		}

		private IWebBrowser getExternalBrowser() throws PartInitException {
			IWorkbenchBrowserSupport support = PlatformUI.getWorkbench().getBrowserSupport();
			return support.getExternalBrowser();
		}

		public void close() {
		}

		public boolean isCloseSupported() {
			return false;
		}

		public void displayURL(String url) throws Exception {
			try {
				IWebBrowser browser = getExternalBrowser();
				if (browser != null) {
					browser.openURL(new URL(url));
				}
			} catch (PartInitException pie) {
				ErrorUtil.displayErrorDialog(pie.getLocalizedMessage());
			}
		}

		public boolean isSetLocationSupported() {
			return false;
		}

		public boolean isSetSizeSupported() {
			return false;
		}

		public void setLocation(int x, int y) {
		}

		public void setSize(int width, int height) {
		}
	}
	
	
	@SuppressWarnings("restriction")
	public AbstractSelfHelpUI(){
		super();
		BaseHelpSystem.getInstance().setBrowserInstance(new ExternalWorkbenchBrowser());
	}
	
	public static AbstractSelfHelpUI getInstance() {
		return instance;
	}

	@Override
	public void displayHelp() {
		SelfBaseHelpSystem.getHelpDisplay().displayHelp(useExternalBrowser(null));
	}

	@Override
	public void displayContext(IContext context, int x, int y) {
		
	}

	@Override
	public void displayHelpResource(String href) {
		SelfBaseHelpSystem.getHelpDisplay().displayHelpResource(href, useExternalBrowser(href));
	}

	@Override
	public boolean isContextHelpDisplayed() {
		return false;
	}

	
	private boolean useExternalBrowser(String url) {
		// On non Windows platforms, use external when modal window is displayed
		if (!Constants.OS_WIN32.equalsIgnoreCase(Platform.getOS())) {
			Display display = Display.getCurrent();
			if (display != null) {
				if (insideModalParent(display))
					return true;
			}
		}

		// Use external when no help frames are to be displayed, otherwise no
		// navigation buttons.
		if (url != null) {
			if (url.indexOf("?noframes=true") > 0 //$NON-NLS-1$
					|| url.indexOf("&noframes=true") > 0) { //$NON-NLS-1$
				return true;
			}
		}
		return false;
	}
	
	
	private boolean insideModalParent(Display display) {
		return isDisplayModal(display.getActiveShell());
	}

	public static boolean isDisplayModal(Shell activeShell) {
		while (activeShell != null) {
			if ((activeShell.getStyle() & (SWT.APPLICATION_MODAL | SWT.PRIMARY_MODAL | SWT.SYSTEM_MODAL)) > 0)
				return true;
			activeShell = (Shell) activeShell.getParent();
		}
		return false;
	}
}

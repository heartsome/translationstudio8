package net.heartsome.license.handler;

import java.lang.reflect.InvocationTargetException;

import net.heartsome.cat.ts.help.SystemResourceUtil;
import net.heartsome.license.resource.Messages;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Display;

public class LicenseManageHandler extends AbstractHandler{

	public Object execute(ExecutionEvent event) throws ExecutionException {
		ProgressMonitorDialog progress = new ProgressMonitorDialog(Display.getDefault().getActiveShell());
		try {  
			final String[] str = new String[3];
		    progress.run(true, true, new IRunnableWithProgress() {  
		        public void run(IProgressMonitor monitor)  
		                throws InvocationTargetException {  
		        	monitor.beginTask(Messages.getString("license.LicenseManageHandler.progress"), 10);
		        	String[] temp = SystemResourceUtil.load(monitor);
		        	str[0] = temp[0];
		        	str[1] = temp[1];
		        	str[2] = temp[2];
					monitor.done();
		        }  
		    });  
		    SystemResourceUtil.showDialog(str);
		} catch (InvocationTargetException e) {  
		    e.printStackTrace();  
		} catch (InterruptedException e) {  
		  
		}  
		
		return null;
	}

}

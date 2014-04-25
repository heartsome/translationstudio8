package net.heartsome.cat.ts.ui.propertyTester;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;

/**
 * 验证导入/导出 RTF 功能是否可用的类
 * @author  peason
 * @version 
 * @since   JDK1.6
 */
public class RTFEnabledPropertyTester extends PropertyTester {
	
	public static final String PROPERTY_NAMESPACE = "rtf";
	public static final String PROPERTY_ENABLED = "enabled";

	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
		IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
		return projects != null && projects.length > 0;
	}

}

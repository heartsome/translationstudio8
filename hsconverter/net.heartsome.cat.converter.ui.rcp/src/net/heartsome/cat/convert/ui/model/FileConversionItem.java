package net.heartsome.cat.convert.ui.model;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.jobs.ISchedulingRule;

/**
 * IFile 转换项目实现
 * @author cheney
 * @since JDK1.6
 */
public class FileConversionItem extends DefaultConversionItem {

	private IResource resource;

	/**
	 * IFile 构建函数
	 * @param file
	 */
	public FileConversionItem(IFile file) {
		super(file.getLocation());
		this.resource = file;
	}

	/**
	 * IContainer 构建函数
	 * @param folder
	 */
	public FileConversionItem(IContainer folder) {
		super(folder.getLocation());
		this.resource = folder;
	}

	/**
	 * IProject 构建函数
	 * @param project
	 */
	public FileConversionItem(IProject project) {
		super(project.getLocation());
		this.resource = project;
	}

	@Override
	public void refresh() {
		try {
			resource.getProject().refreshLocal(IResource.DEPTH_INFINITE, null);
		} catch (CoreException e) {
			// 忽略
		}
	}

	@Override
	public boolean contains(ISchedulingRule rule) {
		if (this == rule) {
			return true;
		}
		if (rule instanceof FileConversionItem) {
			return resource.contains(((FileConversionItem) rule).resource);
		}
		if (rule instanceof IResource) {
			return resource.contains(rule);
		}
		return super.contains(rule);
	}

	@Override
	public boolean isConflicting(ISchedulingRule rule) {
		if (rule instanceof FileConversionItem) {
			return resource.isConflicting(((FileConversionItem) rule).resource);
		}
		if (rule instanceof IResource) {
			return resource.isConflicting(rule);
		}
		return super.isConflicting(rule);
	}

	@Override
	public IConversionItem getParent() {
		return new FileConversionItem(resource.getParent());
	}

	@Override
	public IConversionItem getProject() {
		return new FileConversionItem(resource.getProject());
	}

}

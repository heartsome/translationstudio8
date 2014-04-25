package net.heartsome.cat.convert.ui.wizard;

import java.util.List;

import net.heartsome.cat.convert.ui.model.ConverterViewModel;
import net.heartsome.cat.convert.ui.resource.Messages;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.wizard.Wizard;

/**
 * 逆向转换向导
 * @author weachy
 * @since JDK1.5
 */
public class ReverseConversionWizard extends Wizard {

	private List<ConverterViewModel> converterViewModels;

	private IProject project;

	/**
	 * 正向转换向导构造函数
	 * @param model
	 */
	public ReverseConversionWizard(List<ConverterViewModel> models, IProject projct) {
		this.converterViewModels = models;
		this.project = projct;
		
		setWindowTitle(Messages.getString("wizard.ReverseConversionWizard.title")); //$NON-NLS-1$
		
		// 需要显示 progress monitor
		setNeedsProgressMonitor(true);
	}

	@Override
	public void addPages() {
		addPage(new ReverseConversionWizardPage(Messages.getString("wizard.ReverseConversionWizard.pageName")));
		// TODO 存储翻译到翻译记译库
	}

	@Override
	public boolean performFinish() {
		return true;
	}

	/**
	 * 返回此向导对应的 view model
	 * @return ;
	 */
	public List<ConverterViewModel> getConverterViewModels() {
		return converterViewModels;
	}

	/**
	 * 返回此向导对应的 Project
	 * @return ;
	 */
	public IProject getProject() {
		return project;
	}

}

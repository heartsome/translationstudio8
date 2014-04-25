package net.heartsome.cat.convert.ui.wizard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import net.heartsome.cat.common.locale.Language;
import net.heartsome.cat.common.resources.ResourceUtils;
import net.heartsome.cat.convert.ui.model.ConversionConfigBean;
import net.heartsome.cat.convert.ui.model.ConverterViewModel;
import net.heartsome.cat.convert.ui.model.IConversionItem;
import net.heartsome.cat.ts.core.file.ProjectConfiger;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.wizard.Wizard;

/**
 * 项目正向转换向导
 * @author weachy
 * @since JDK1.5
 */
public class ConversionWizard extends Wizard {

	private List<ConverterViewModel> converterViewModels;

	private ArrayList<ConversionConfigBean> conversionConfigBeans;

	/**
	 * 正向转换向导构造函数
	 * @param model
	 */
	public ConversionWizard(List<ConverterViewModel> models, IProject project) {
		this.converterViewModels = models;

		String projCfgFile = project.getLocation().append(".project").toOSString();
		ProjectConfiger configer = new ProjectConfiger(projCfgFile);
		Language sourceLanguage = configer.getCurrentProjectConfig().getSourceLang();
		List<Language> targetlanguage = configer.getCurrentProjectConfig().getTargetLang();
		Collections.sort(targetlanguage, new Comparator<Language>() {
			public int compare(Language l1, Language l2) {
				return l1.toString().compareTo(l2.toString());
			}
		});
		
		conversionConfigBeans = new ArrayList<ConversionConfigBean>();
		for (ConverterViewModel converterViewModel : converterViewModels) {
			IConversionItem conversionItem = converterViewModel.getConversionItem();
			String source = ResourceUtils.toWorkspacePath(conversionItem.getLocation());

			ConversionConfigBean bean = converterViewModel.getConfigBean();
			bean.setSource(source); // 初始化源文件路径
			bean.setSrcLang(sourceLanguage.getCode()); // 初始化源语言
			bean.setTgtLangList(targetlanguage);
			conversionConfigBeans.add(bean);
		}
		
		setWindowTitle(Messages.getString("ConversionWizard.0")); //$NON-NLS-1$
	}

	@Override
	public void addPages() {
		String title = Messages.getString("ConversionWizard.1"); //$NON-NLS-1$
		
		ConversionWizardPage page = new ConversionWizardPage(title, converterViewModels, conversionConfigBeans);
		addPage(page);

		// TODO 添加预翻译选项设置
		// addPage(new TranslationWizardPage(Messages.getString("ConversionWizard.2"))); //$NON-NLS-1$
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
}

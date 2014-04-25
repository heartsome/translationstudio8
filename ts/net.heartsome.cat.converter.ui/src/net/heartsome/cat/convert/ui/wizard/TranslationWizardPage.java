package net.heartsome.cat.convert.ui.wizard;

import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * 正向转换的预翻译配置页(该类未被调用，因此未做国际化)
 * @author cheney
 * @since JDK1.6
 */
public class TranslationWizardPage extends WizardPage {

	/**
	 * 预翻译配置页构建函数
	 * @param pageName
	 */
	protected TranslationWizardPage(String pageName) {
		super(pageName);
		setTitle("翻译相关的设置");
		setMessage("翻译相关的设置，待实现。");
	}

	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		Label label = new Label(composite, SWT.NONE);
		label.setText("Test:");
		new Text(composite, SWT.BORDER);
		GridLayoutFactory.swtDefaults().numColumns(2).generateLayout(composite);
		setControl(composite);
	}

}

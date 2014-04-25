/**
 * NewProjectWizardFourthPage.java
 *
 * Version information :
 *
 * Date:Oct 28, 2011
 *
 * Copyright notice :
 * 本文件及其附带的相关文件包含机密信息，仅限瀚特盛科技有限公司指定的，与本项目有关的内部人员和客户联络人员查阅和使用。 
 * 如果您不是本保密声明中指定的收件者，请立即销毁本文件，禁止对本文件或根据本文件中的内容采取任何其他行动， 
 * 包括但不限于：泄露本文件中的信息、以任何方式制作本文件全部或部分内容之副本、将本文件或其相关副本提供给任何其他人。
 */
package net.heartsome.cat.ts.ui.wizards;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.heartsome.cat.ts.ui.Activator;
import net.heartsome.cat.ts.ui.extensionpoint.IConverterCaller;
import net.heartsome.cat.ts.ui.resource.Messages;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;

/**
 * 创建项目向导的第四个页面，用于用户选择源文件
 * @author Jason
 * @version
 * @since JDK1.6
 */
public class NewProjectWizardSourceFilePage extends WizardPage {

	private List<String> srcFileList;
	private ListViewer fileListViewer;
	private boolean isOpenConverter = false;

	private IConverterCaller converterCaller;

	/**
	 * Create the wizard.
	 */
	public NewProjectWizardSourceFilePage() {
		super("wizardPage");
		setTitle(Messages.getString("wizard.NewProjectWizardSourceFilePage.title"));
		setDescription(Messages.getString("wizard.NewProjectWizardSourceFilePage.desc"));
		setImageDescriptor(Activator.getImageDescriptor("images/project/new-project-logo.png"));
		srcFileList = new ArrayList<String>();
	}

	/**
	 * Create contents of the wizard.
	 * @param parent
	 */
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);
		container.setLayout(new GridLayout(1, false));
		setControl(container);

		fileListViewer = new ListViewer(container, SWT.BORDER | SWT.V_SCROLL | SWT.MULTI);
		fileListViewer.getList().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		fileListViewer.setContentProvider(new ArrayContentProvider());
		fileListViewer.setInput(srcFileList);

		if (this.converterCaller != null) {
			final Button btnConvert = new Button(container, SWT.CHECK);
			btnConvert.setText(Messages.getString("wizard.NewProjectWizardSourceFilePage.btnConvert"));
			btnConvert.setSelection(true);
			isOpenConverter = true;
			btnConvert.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					isOpenConverter = btnConvert.getSelection();
				}
			});
		}

		Composite composite = new Composite(container, SWT.NONE);
		composite.setLayout(new GridLayout(2, false));
		composite.setLayoutData(new GridData(SWT.RIGHT, SWT.FILL, false, false, 1, 1));

		Button addBtn = new Button(composite, SWT.NONE);
		addBtn.setText(Messages.getString("wizard.NewProjectWizardSourceFilePage.addBtn"));
		addBtn.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				FileDialog dlg = new FileDialog(getShell(), SWT.MULTI);
				// CONVERTEREXTENTION
				String[] supExtentions = new String[]{"*.mif;*.idml;*.inx;*.xlf;*.rtf;*.po;*.properties;*.js;*.mqxlz;*.doc;*.xls;*.ppt;" +
						"*.docx;*.xlsx;*.pptx;*.odt;*.ods;*.odp;*.odg;*.rtf;*.sdlxliff;*.ttx;*.htm;*.html;*.txt;*.resx;*.rc;*.xml;*.txml", "*.*"};
				dlg.setFilterExtensions(supExtentions);
				if (dlg.open() != null) {
					String[] files = dlg.getFileNames();
					for (int i = 0; i < files.length; i++) {
						StringBuffer buf = new StringBuffer(dlg.getFilterPath());
						buf.append(File.separator);
						buf.append(files[i]);
						String file = buf.toString();
						if (!srcFileList.contains(file)) {
							srcFileList.add(file);
						}
					}
					fileListViewer.refresh();
				}
			}
		});

		Button deleteBtn = new Button(composite, SWT.NONE);
		deleteBtn.setText(Messages.getString("wizard.NewProjectWizardSourceFilePage.deleteBtn"));
		deleteBtn.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				IStructuredSelection selections = (IStructuredSelection) fileListViewer.getSelection();
				Iterator<?> it = selections.iterator();
				while (it.hasNext()) {
					String file = (String) it.next();
					srcFileList.remove(file);
				}
				fileListViewer.refresh();
			}
		});
	}

	/**
	 * 获取选择的源文件
	 * @return 返回一个文件路径的List，返回null 未选择源文件;
	 */
	public List<String> getSrcFiles() {
		return srcFileList.size() == 0 ? null : srcFileList;
	}

	public boolean isOpenConverter() {
		return isOpenConverter;
	}

	// 设置转换器信息
	public void setConvertInfo(IConverterCaller converterCaller) {
		this.converterCaller = converterCaller;
	}
	
	
	public static void main(String[] args) {
		String formant = "MIF;IDML;INX;XLF;RTF;PO;PROPERTIES;JS;MQXLZ;DOC;XLS;PPT;DOCX;XLSX;PPTX;ODT;ODS;ODP;ODG;RTF;SDLXLIFF;TTX;HTM;HTML;TXT;RESX;RC;XML;TXML";
		System.out.println(formant.toLowerCase());
		String newFormat = formant.toLowerCase();
		System.out.println(newFormat.replace(";", ";*."));
		
		
		
		
	}
}

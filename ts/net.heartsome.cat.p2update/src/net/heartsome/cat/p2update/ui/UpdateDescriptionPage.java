package net.heartsome.cat.p2update.ui;

import net.heartsome.cat.common.util.CommonFunction;
import net.heartsome.cat.p2update.util.P2UpdateUtil;

import org.eclipse.equinox.internal.p2.ui.ProvUI;
import org.eclipse.equinox.internal.p2.ui.model.AvailableIUElement;
import org.eclipse.equinox.internal.p2.ui.model.AvailableUpdateElement;
import org.eclipse.equinox.internal.p2.ui.model.IUElementListRoot;
import org.eclipse.equinox.p2.metadata.IInstallableUnit;
import org.eclipse.equinox.p2.operations.Update;
import org.eclipse.equinox.p2.operations.UpdateOperation;
import org.eclipse.equinox.p2.ui.ProvisioningUI;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

/**
 * 更新向导页，用于显示当前更新的文字描述内容
 * @author Jason
 * @version
 * @since JDK1.6
 */
public class UpdateDescriptionPage extends WizardPage {

	IUElementListRoot root;
	ProvisioningUI ui;
	UpdateOperation operation;

	/**
	 * root 参数未使用
	 * @param operation
	 * @param root
	 * @param ui
	 */
	protected UpdateDescriptionPage(UpdateOperation operation, IUElementListRoot root, ProvisioningUI ui) {
		super("MyUpdsateDescriptionPage");
		setTitle(P2UpdateUtil.UI_WIZARD_DESC_PAGE_TITLE);
		setDescription(P2UpdateUtil.UI_WIZARD_DESC_PAGE_DESC);
		this.ui = ui;
		this.root = root;
		this.operation = operation;
	}

	public void createControl(Composite parent) {
		Text text = new Text(parent, SWT.MULTI | SWT.V_SCROLL | SWT.BORDER);
		Color color = text.getBackground();
		text.setEditable(false);
		text.setBackground(color);
		text.setText(getUpdateDescDetailText());
		setControl(text);
	}

	private String getUpdateDescDetailText() {
		StringBuffer descBf = new StringBuffer();
		Update[] updates = operation.getSelectedUpdates();
		if (updates.length == 0) {
			// no udpates;
			setPageComplete(false);
			return P2UpdateUtil.UPDATE_PROMPT_INFO_NO_UPDATE;
		}
		Update update = updates[0];
		AvailableUpdateElement newElement = new AvailableUpdateElement(null, update.replacement, update.toUpdate,
				ui.getProfileId(), ProvUI.getQueryContext(ui.getPolicy()).getShowProvisioningPlanChildren());
		descBf.append(P2UpdateUtil.UI_WIZARD_DESC_PAGE_DESC_DETAIL).append(newElement.getIU().getVersion())
				.append("\n\n");

		newElement.setQueryable(operation.getProvisioningPlan().getAdditions());
		Object[] children = newElement.getChildren(newElement);
		StringBuffer temp = new StringBuffer();
		if (children != null && children.length != 0) {
			AvailableIUElement c = (AvailableIUElement) children[0];
			String detail = c.getIU().getProperty(IInstallableUnit.PROP_DESCRIPTION, null);
			if (detail == null)
				detail = "";
			temp.append(detail);
		}
		String descResult = "";
		if(temp.length() != 0){
			String lang = CommonFunction.getSystemLanguage();
			String szh = "[-zh-]";
			String sen = "[-en-]";
			if(lang.equals("en")){
				descResult = temp.substring(sen.length() + 1, temp.indexOf(szh) - 1);
			}else if(lang.equals("zh")){
				descResult = temp.substring(temp.indexOf(szh) + szh.length() + 1, temp.length());
			}
		}
		return descBf.append(descResult).toString();
		
		// String detail = "";
		// Object[] elements = root.getChildren(root);
		// if(elements.length > 0){
		// AvailableUpdateElement element = (AvailableUpdateElement) elements[0];
		// Object[] children = element.getChildren(element);
		// if(children != null && children.length != 0){
		// AvailableIUElement c = (AvailableIUElement) children[0];
		// IInstallableUnit selectedIU = ElementUtils.elementToIU(c);
		// detail = selectedIU.getProperty(IInstallableUnit.PROP_DESCRIPTION, null);
		// if (detail == null)
		// detail = "";
		// }
		// }
	}

}

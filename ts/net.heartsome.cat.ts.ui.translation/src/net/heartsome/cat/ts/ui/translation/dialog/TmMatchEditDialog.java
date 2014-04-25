/**
 * TmMatchEditDialog.java
 *
 * Version information :
 *
 * Date:2013-3-28
 *
 * Copyright notice :
 * 本文件及其附带的相关文件包含机密信息，仅限瀚特盛科技有限公司指定的，与本项目有关的内部人员和客户联络人员查阅和使用。 
 * 如果您不是本保密声明中指定的收件者，请立即销毁本文件，禁止对本文件或根据本文件中的内容采取任何其他行动， 
 * 包括但不限于：泄露本文件中的信息、以任何方式制作本文件全部或部分内容之副本、将本文件或其相关副本提供给任何其他人。
 */
package net.heartsome.cat.ts.ui.translation.dialog;

import java.text.MessageFormat;

import net.heartsome.cat.common.bean.FuzzySearchResult;
import net.heartsome.cat.common.util.CommonFunction;
import net.heartsome.cat.ts.ui.Activator;
import net.heartsome.cat.ts.ui.Constants;
import net.heartsome.cat.ts.ui.innertag.SegmentViewer;
import net.heartsome.cat.ts.ui.preferencepage.IPreferenceConstants;
import net.heartsome.cat.ts.ui.translation.resource.Messages;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.keys.IBindingService;

/**
 * 匹配面板匹配面编辑对话框。
 * @author Jason
 * @version
 * @since JDK1.6
 */
public class TmMatchEditDialog extends Dialog {
	private SegmentViewer srcSegmentViewer;
	private SegmentViewer tgtSegmentViewer;
	private FuzzySearchResult fuzzyResult;

	/**
	 * Create the dialog.
	 * @param parentShell
	 */
	public TmMatchEditDialog(Shell parentShell, FuzzySearchResult fuzzyResult) {
		super(parentShell);
		this.fuzzyResult = fuzzyResult;
		setShellStyle(getShellStyle() | SWT.RESIZE);
		IBindingService bindingService = (IBindingService) PlatformUI.getWorkbench().getService(IBindingService.class);
		bindingService.setKeyFilterEnabled(false);
	}

	@Override
	protected void okPressed() {
		String srcFullText = srcSegmentViewer.getText();
		String tgtFullText = tgtSegmentViewer.getText();
		String title = Messages.getString("dialog.TmMatchEditDialog.error.title");
		if (srcFullText == null || srcFullText.length() == 0) {
			MessageDialog.openError(getShell(), title, Messages.getString("dialog.TmMatchEditDialog.error.srcNull"));
			return;
		}
		if (tgtFullText == null || tgtFullText.length() == 0) {
			MessageDialog.openError(getShell(), title, Messages.getString("dialog.TmMatchEditDialog.error.tgtNull"));
			return;
		}
		if (srcFullText.equals(fuzzyResult.getTu().getSource().getFullText())
				&& tgtFullText.equals(fuzzyResult.getTu().getTarget().getFullText())) {
			super.cancelPressed();
			return;
		}
		fuzzyResult.getTu().getSource().setFullText(srcFullText);
		fuzzyResult.getTu().getSource().setPureText(srcSegmentViewer.getPureText());
		fuzzyResult.getTu().getTarget().setFullText(tgtFullText);
		fuzzyResult.getTu().getTarget().setPureText(tgtSegmentViewer.getPureText());
		fuzzyResult.getTu().setChangeDate(CommonFunction.retTMXDate());
		fuzzyResult.getTu().setChangeUser(
				Activator.getDefault().getPreferenceStore().getString(IPreferenceConstants.SYSTEM_USER));
		super.okPressed();
	}

	@Override
	public boolean close() {
		IBindingService bindingService = (IBindingService) PlatformUI.getWorkbench().getService(IBindingService.class);
		bindingService.setKeyFilterEnabled(true);
		srcSegmentViewer.reset();
		tgtSegmentViewer.reset();
		return super.close();
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(Messages.getString("dialog.TmMatchEditDialog.title"));
	}

	/**
	 * Create contents of the dialog.
	 * @param parent
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		container.setLayout(new GridLayout(1, false));

		Group srcGroup = new Group(container, SWT.NONE);
		srcGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		srcGroup.setLayout(new GridLayout(1, false));
		String srcGroupTile = Messages.getString("dialog.TmMatchEditDialog.component.src");
		srcGroupTile = MessageFormat.format(srcGroupTile, fuzzyResult.getTu().getSource().getLangCode());
		srcGroup.setText(srcGroupTile);

		srcSegmentViewer = new SegmentViewer(srcGroup, SWT.MULTI | SWT.WRAP | SWT.BORDER | SWT.V_SCROLL, null);
		StyledText srcTextControl = srcSegmentViewer.getTextWidget();
		srcTextControl.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		srcTextControl.setLineSpacing(Constants.SEGMENT_LINE_SPACING);
		srcTextControl.setLeftMargin(Constants.SEGMENT_LEFT_MARGIN);
		srcTextControl.setRightMargin(Constants.SEGMENT_RIGHT_MARGIN);
		srcTextControl.setTopMargin(Constants.SEGMENT_TOP_MARGIN);
		srcTextControl.setBottomMargin(Constants.SEGMENT_TOP_MARGIN);
		srcTextControl.setFont(JFaceResources.getFont(Constants.MATCH_VIEWER_TEXT_FONT));

		Group targetGroup = new Group(container, SWT.NONE);
		targetGroup.setLayout(new GridLayout(1, false));
		targetGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		String tgtGroupTile = Messages.getString("dialog.TmMatchEditDialog.component.tgt");
		tgtGroupTile = MessageFormat.format(tgtGroupTile, fuzzyResult.getTu().getSource().getLangCode());
		targetGroup.setText(tgtGroupTile);

		tgtSegmentViewer = new SegmentViewer(targetGroup, SWT.MULTI | SWT.WRAP | SWT.BORDER | SWT.V_SCROLL, null);
		StyledText tgtTextControl = tgtSegmentViewer.getTextWidget();
		tgtTextControl.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		tgtTextControl.setLineSpacing(Constants.SEGMENT_LINE_SPACING);
		tgtTextControl.setLeftMargin(Constants.SEGMENT_LEFT_MARGIN);
		tgtTextControl.setRightMargin(Constants.SEGMENT_RIGHT_MARGIN);
		tgtTextControl.setTopMargin(Constants.SEGMENT_TOP_MARGIN);
		tgtTextControl.setBottomMargin(Constants.SEGMENT_TOP_MARGIN);
		tgtTextControl.setFont(JFaceResources.getFont(Constants.MATCH_VIEWER_TEXT_FONT));

		net.heartsome.cat.ts.ui.innertag.tagstyle.TagStyleConfigurator.configure(srcSegmentViewer);
		net.heartsome.cat.ts.ui.innertag.tagstyle.TagStyleConfigurator.configure(tgtSegmentViewer);

		TmMatchEditorBodyMenu srcMenu = new TmMatchEditorBodyMenu(srcSegmentViewer);
		srcSegmentViewer.getTextWidget().setMenu(srcMenu.getBodyMenu());

		TmMatchEditorBodyMenu tgtMenu = new TmMatchEditorBodyMenu(tgtSegmentViewer);
		tgtSegmentViewer.getTextWidget().setMenu(tgtMenu.getBodyMenu());

		loadData();

		return container;
	}

	private void loadData() {
		srcSegmentViewer.setText(fuzzyResult.getTu().getSource().getFullText());
		srcSegmentViewer.setSource("");
		srcSegmentViewer.initUndoManager(50);
		tgtSegmentViewer.setText(fuzzyResult.getTu().getTarget().getFullText());
		tgtSegmentViewer.setSource("");
		tgtSegmentViewer.initUndoManager(50);
		tgtSegmentViewer.getTextWidget().setFocus();
		tgtSegmentViewer.getTextWidget().setCaretOffset(tgtSegmentViewer.getText().length());
	}

	/**
	 * Create contents of the button bar.
	 * @param parent
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	}

	/**
	 * Return the initial size of the dialog.
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(700, 300);
	}
}
